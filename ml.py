# format

import random as rand
import math
import copy
import time
import os
import tensorflow as tf
import numpy as np
from tensorflow import keras
from tensorflow.keras import layers
import tf2onnx
import onnxruntime as rt
from scipy.special import softmax
from misc import getFlag, getFlagValue

# rand.seed(5)
# np.random.seed(5)
# tf.random.set_seed(5)

DO_TRAINING = getFlag('-t')
SKIP_TRAINING_MATCHES = getFlag('-s')
PLAY_A_GAME = getFlag('-p')
PLAY_MATCHES = getFlag('-m')
ITERATION_COUNT = int(getFlagValue('-c', 5))
NODE_COUNT = int(getFlagValue('-n', 1000))
# tf.config.threading.set_inter_op_parallelism_threads(6)
# tf.config.threading.set_intra_op_parallelism_threads(6)

HAS_HEALING = False


class EvalCache:

    def __init__(self, input_len):
        self.cache = {}
        self.model = None
        self.calls = 0
        self.hits = 0
        self.input_len = input_len
        self.output_names = None
        self.onxx_session = None

    def set_cache_model(self, model):
        self.cache = {}
        self.model = model
        self.calls = 0
        self.hits = 0
        spec = (tf.TensorSpec((1, self.input_len), tf.float32, name="input"),)
        output_path = "tmp/model.onnx"
        model_proto, _ = tf2onnx.convert.from_keras(model, input_signature=spec, opset=13, output_path=output_path)
        self.output_names = [n.name for n in model_proto.graph.output]
        providers = ['CPUExecutionProvider']
        self.onxx_session = rt.InferenceSession(output_path, providers=providers)

    def get_eval(self, state):
        self.calls += 1
        x = state.get_input()
        s = tuple(x)
        if s in self.cache:
            self.hits += 1
            return self.cache[s]
        y = self.onxx_session.run(self.output_names, {"input": [x]})
        if len(y) == 2:
            v_health = y[0][0][0]
            v_win = 1
            n = y[1][0]
        else:
            v_health = y[0][0][0]
            v_win = y[1][0][0]
            n = y[2][0]
        for i in range(0, len(n)):
            if not state.is_action_legal()[i]:
                n[i] = -1000
        policy = softmax(n)
        self.cache[s] = (v_health, v_win, policy)
        return self.cache[s]

    def printStats(self):
        print(f'Eval Cache: size={len(self.cache)}, calls={self.calls}, hits={self.hits} ({self.hits / self.calls if self.calls > 0 else 0.0:.2f})')


class GameState:

    def __init__(self, enemy, player, cards):
        cards.sort(key=lambda x: x[0].card_name)
        self.deck_arr = []
        self.card_dict = []
        self.deck = []
        self.hand = []
        self.discard = []
        for idx, (card, c) in enumerate(cards):
            for i in range(0, c):
                self.deck_arr.append(idx)
            self.card_dict.append(card)
            self.deck.append(c)
            self.hand.append(0)
            self.discard.append(0)
        self.energy = 3
        self.energy_refill = 3
        self.enemy = enemy
        self.player = player
        self.action_cache = None
        self.v_win = 0
        self.v_health = 0
        self.q = []
        self.n = []
        self.ns = []
        self.total_n = 0
        self.policy = None
        for i in range(0, len(self.card_dict) + 1):
            self.q.append(0)
            self.n.append(0)
            self.ns.append(None)

    def draw(self):
        if len(self.deck_arr) == 0:
            for i, c in enumerate(self.discard):
                for _ in range(0, c):
                    self.deck_arr.append(i)
                self.discard[i] = 0
                self.deck[i] = c
        if len(self.deck_arr) == 0:
            return
        i = rand.randint(0, len(self.deck_arr) - 1)
        self.deck[self.deck_arr[i]] -= 1
        self.hand[self.deck_arr[i]] += 1
        if len(self.deck_arr) > 1:
            self.deck_arr[i] = self.deck_arr[len(self.deck_arr) - 1]
        self.deck_arr.pop()

    def play_card(self, i):
        assert self.card_dict[i].energyCost(self) > 0
        assert self.energy >= self.card_dict[i].energyCost(self)
        assert self.hand[i] > 0
        self.hand[i] -= 1
        self.energy -= self.card_dict[i].energyCost(self)
        self.card_dict[i].play(self)
        self.discard[i] += 1
        self.enemy.react(self.card_dict[i])

    def startTurn(self):
        self.enemy.next_move()
        self.energy = self.energy_refill
        self.player.block = 0
        for i in range(0, 5):
            self.draw()

    def endTurn(self):
        for i, c in enumerate(self.discard):
            if not hasattr(self.card_dict[i], 'exhaustEndOfTurn'):
                self.discard[i] += self.hand[i]
            self.hand[i] = 0
        self.enemy.endTurn()
        self.player.endTurn()

    def do_action(self, action):
        if action == len(self.policy) - 1:
            self.enemy.do_move(self.player)
            self.endTurn()
            self.startTurn()
        else:
            self.play_card(action)
        self.policy = None
        self.v_win = 0
        self.v_health = 0

    def clone(self):
        clone = copy.copy(self)
        clone.deck_arr = self.deck_arr.copy()
        clone.deck = self.deck.copy()
        clone.hand = self.hand.copy()
        clone.discard = self.discard.copy()
        clone.enemy = copy.copy(self.enemy)
        clone.player = copy.copy(self.player)
        clone.action_cache = None
        clone.q = []
        clone.n = []
        clone.ns = []
        clone.total_n = 0
        for i in range(0, len(clone.card_dict) + 1):
            clone.q.append(0)
            clone.n.append(0)
            clone.ns.append(None)
        return clone

    def get_input(self):
        x = []
        for i in range(0, len(self.card_dict)):
            x.append(self.deck[i] / 10)
        for i in range(0, len(self.card_dict)):
            x.append(self.hand[i] / 10)
        # for i in range(0, len(self.card_dict)):
        #     x.append(self.discard[i] / 10)
        x.append(self.energy / 10)
        x.append(self.player.health / self.player.max_health)
        x.append(self.player.block / 40)
        x.append(self.player.vulnerable / 5)
        x.append(self.enemy.health / self.enemy.max_health)
        x.append(self.enemy.vulnerable / 10)
        x.append(self.enemy.strength / 20)
        x.append(self.enemy.move / self.enemy.num_of_moves)
        x.append(self.enemy.last_move / self.enemy.num_of_moves)
        x.append(self.enemy.last_move_2 / self.enemy.num_of_moves)
        return x

    def do_eval(self):
        self.v_health, self.v_win, self.policy = evalCache.get_eval(self)

    def is_action_legal(self):
        if self.action_cache is not None:
            return self.action_cache
        actions = np.zeros(len(self.card_dict) + 1, dtype=bool)
        actions[len(self.card_dict)] = True
        for i in range(len(self.card_dict)):
            if self.hand[i] > 0:
                cost = self.card_dict[i].energyCost(self)
                if cost >= 0 and cost <= self.energy:
                    actions[i] = True
        self.action_cache = actions
        return actions

    def get_v(self):
        is_terminal = self.is_terminal()
        if is_terminal == 1:
            return self.player.health / self.player.orig_health
        elif is_terminal == -1:
            return 0
        return self.v_health

    def is_terminal(self):
        if self.player.health <= 0:
            return -1
        if self.enemy.health <= 0:
            return 1
        return 0

    def __repr__(self):
        q_norm = self.q.copy()
        for i in range(len(q_norm)):
            if self.n[i] > 0:
                q_norm[i] /= self.n[i]
        return f"hand={self.hand}, deck={self.deck}, discard={self.discard}, {self.player}, {self.enemy}, p={self.policy}, v={(self.v_win, self.v_health)}, q={q_norm}, n={self.n}"


class ChanceState:

    class Node:

        def __init__(self, state, n):
            self.state = state
            self.n = n

    def __init__(self, parent_state, action):
        self.cache = {}
        self.parent_state = parent_state
        self.action = action
        self.total_n = 0

    def get_next_state(self):
        state = self.parent_state.clone()
        state.do_action(self.action)
        x = tuple(state.get_input())
        self.total_n += 1
        if x in self.cache:
            node = self.cache[x]
            node.n += 1
            return node.state
        self.cache[x] = ChanceState.Node(state, 1)
        return state


class Player:

    def __init__(self, cur_health, max_health):
        self.orig_health = cur_health
        self.health = cur_health
        self.max_health = max_health
        self.block = 0
        self.strength = 0
        self.vulnerable = 0

    def damage(self, dmg):
        if self.vulnerable > 0:
            dmg = dmg + (dmg // 2)
        self.health -= max(0, dmg - self.block)
        self.block = max(0, self.block - dmg)
        if self.health < 0:
            self.health = 0

    def add_block(self, block):
        self.block += block
        if self.block > 999:
            self.block = 999

    def do_attack(self, enemy, dmg):
        enemy.damage(dmg + self.strength)

    def endTurn(self):
        if self.vulnerable > 0:
            self.vulnerable -= 1

    def __repr__(self):
        return f"Player(health={self.health}, block={self.block}, vuln={self.vulnerable})"


class Enemy:

    def __init__(self, health):
        self.health = health
        self.max_health = health
        self.strength = 0
        self.vulnerable = 0
        self.block = 0

    def damage(self, dmg):
        if self.vulnerable > 0:
            dmg = dmg + (dmg // 2)
        self.health -= max(0, dmg - self.block)
        self.block = max(0, self.block - dmg)
        if self.health < 0:
            self.health = 0

    def endTurn(self):
        if self.vulnerable > 0:
            self.vulnerable -= 1

    def __repr__(self):
        return f"Enemy(health={self.health}, strength={self.strength}, vuln={self.vulnerable})"


class GremlinNob(Enemy):

    def __init__(self, health):
        super().__init__(health)
        self.num_of_moves = 3
        self.move = -1
        self.turn = 0
        self.last_move = -1
        self.last_move_2 = -1

    def do_move(self, player):
        if self.move == 1:
            player.damage(8 + self.strength)
            player.vulnerable += 3
        elif self.move == 2:
            player.damage(16 + self.strength)

    def next_move(self):
        self.last_move_2 = self.last_move
        self.last_move = self.move
        if self.turn == 0:
            self.move = 0
            self.turn = 1
            return
        m = self.turn % 3
        if m == 1:
            self.move = 1
        else:
            self.move = 2
        self.turn += 1

    def react(self, card):
        if card.card_type == Card.SKILL and self.turn > 1:
            self.strength += 3


class Card:
    ATTACK = 0
    SKILL = 1
    POWER = 2
    CURSE = 3
    STATUS = 4

    def __init__(self, card_name, card_type):
        self.card_type = card_type
        self.card_name = card_name
        self.idx = -1

    def __repr__(self):
        return self.card_name


class CardStrike(Card):

    def __init__(self):
        super().__init__("Strike", Card.ATTACK)

    def play(self, state):
        state.player.do_attack(state.enemy, 6)

    def energyCost(self, state):
        return 1


class CardDefend(Card):

    def __init__(self):
        super().__init__("Defend", Card.SKILL)

    def play(self, state):
        state.player.add_block(5)

    def energyCost(self, state):
        return 1


class CardBash(Card):

    def __init__(self):
        super().__init__("Bash", Card.ATTACK)

    def play(self, state):
        state.player.do_attack(state.enemy, 8)
        state.enemy.vulnerable += 2

    def energyCost(self, state):
        return 2


class CardBash1(Card):

    def __init__(self):
        super().__init__("Bash+", Card.ATTACK)

    def play(self, state):
        state.player.do_attack(state.enemy, 10)
        state.enemy.vulnerable += 3

    def energyCost(self, state):
        return 2


class CardAscendersBane(Card):

    def __init__(self):
        super().__init__("Ascender's Bane", Card.CURSE)
        self.exhaustEndOfTurn = True

    def energyCost(self, state):
        return -1


class CardThunderClap(Card):

    def __init__(self):
        super().__init__("Thunder Clap", Card.ATTACK)

    def play(self, state):
        state.player.do_attack(state.enemy, 4)
        state.enemy.vulnerable += 1

    def energyCost(self, state):
        return 1


class CardWound(Card):

    def __init__(self):
        super().__init__("Wound", Card.STATUS)

    def energyCost(self, state):
        return -1


class MCTS:

    def __init__(self, state, model):
        self.state = state
        self.model = model
        state.startTurn()
        state.do_eval()

    def search(self, state, training=False, remaining_calls=10000000):
        is_terminal = state.is_terminal()
        if is_terminal != 0:
            return state.get_v(), None
        action = 0
        max_so_far = -1000000
        num_of_actions = 0
        if training:
            policy = np.array(state.policy, copy=True)
            action_n = 0
            for i in range(0, len(policy)):
                if state.is_action_legal()[i]:
                    action_n += 1
            noise = np.random.dirichlet([0.2] * action_n)
            k = 0
            for i in range(0, len(policy)):
                if state.is_action_legal()[i]:
                    policy[i] = noise[k] * 0.25 + policy[i] * 0.75
                    k += 1
        else:
            policy = state.policy
        max_n = max(state.n)
        for i in range(0, len(policy)):
            if not state.is_action_legal()[i]:
                continue
            if max_n - state.n[i] > remaining_calls:
                continue
            num_of_actions += 1
            u = (state.q[i] / state.n[i] if state.n[i] > 0 else 0) + 3 * policy[i] * math.sqrt(state.total_n) / (1 + state.n[i])
            if u > max_so_far:
                action = i
                max_so_far = u
        next_state = state.ns[action]
        if next_state is None:
            if action == len(policy) - 1 and len(state.deck_arr) != 5:
                chance_state = ChanceState(state, action)
                state.ns[action] = chance_state
                next_state = chance_state.get_next_state()
            else:
                next_state = state.clone()
                state.ns[action] = next_state
                next_state.do_action(action)
            if next_state.policy is None:
                next_state.do_eval()
            v = next_state.get_v()
        else:
            if isinstance(next_state, ChanceState):
                next_state = next_state.get_next_state()
                if next_state.policy is None:
                    next_state.do_eval()
                    v = next_state.get_v()
                else:
                    v, _ = self.search(next_state)
            else:
                v, _ = self.search(next_state)
        state.q[action] += v
        state.n[action] += 1
        state.total_n += 1
        return v, num_of_actions

    def print_tree_h(self, state, depth, indent):
        if depth == 0:
            return
        if isinstance(state, ChanceState):
            chance_state = state
            for _, node in state.cache.items():
                state = node.state
                print(f'{indent}Chance Node ({node.n}/{chance_state.total_n}): {state}')
                for i in range(0, len(state.policy)):
                    if state.ns[i] is not None:
                        if depth > 1:
                            print(f'{indent}  - action={i}')
                        self.print_tree_h(state.ns[i], depth - 1, indent + '    ')
        else:
            print(f'{indent}Normal Node: {state}')
            for i in range(0, len(state.policy)):
                if state.ns[i] is not None:
                    if depth > 1:
                        print(f'{indent}  - action={i}')
                    self.print_tree_h(state.ns[i], depth - 1, indent + '    ')

    def print_tree(self, depth):
        self.print_tree_h(self.state, depth, '')

orig_state = GameState(GremlinNob(90), Player(68, 75), [(CardThunderClap(), 1), (CardAscendersBane(), 1), (CardBash1(), 1), (CardStrike(), 5), (CardDefend(), 4)])
input_len = len(orig_state.get_input())
evalCache = EvalCache(input_len)


def softmax_cross_entropy_with_logits(y_true, y_pred):
    p = y_pred
    pi = y_true
    loss = tf.nn.softmax_cross_entropy_with_logits(labels=pi, logits=p)
    return loss

if os.path.exists('saves/model'):
    custom_objects = {"softmax_cross_entropy_with_logits": softmax_cross_entropy_with_logits}
    with keras.utils.custom_object_scope(custom_objects):
        model = tf.keras.models.load_model('saves/model')
else:
    inputs = keras.Input(shape=(input_len,))
    x = layers.Dense(input_len, activation="linear", use_bias=True, name="layer1")(inputs)
    x = layers.BatchNormalization(axis=1)(x)
    x = layers.LeakyReLU()(x)
    x = layers.Dense(input_len, activation="linear", use_bias=True, name="layer2")(x)
    x = layers.BatchNormalization(axis=1)(x)
    x = layers.LeakyReLU()(x)
    x1 = layers.Dense(input_len, activation="linear", use_bias=True, name="layer3")(x)
    x1 = layers.BatchNormalization(axis=1)(x1)
    x1 = layers.LeakyReLU()(x1)
    x2 = layers.Dense(input_len, activation="linear", use_bias=True, name="layer4")(x)
    x2 = layers.BatchNormalization(axis=1)(x2)
    x2 = layers.LeakyReLU()(x2)
    x3 = layers.Dense(input_len, activation="linear", use_bias=True, name="layer5")(x)
    x3 = layers.BatchNormalization(axis=1)(x3)
    x3 = layers.LeakyReLU()(x3)
    exp_win_head = layers.Dense(1, name="exp_win_head", use_bias=True, activation='sigmoid')(x1)
    exp_dmg_head = layers.Dense(1, name="exp_dmg_head", use_bias=True, activation='sigmoid')(x2)
    policy_head = layers.Dense(len(orig_state.card_dict) + 1, use_bias=True, activation='linear', name="policy_head")(x3)
    model = keras.Model(inputs=[inputs], outputs=[exp_dmg_head, exp_win_head, policy_head])
    model.compile(loss={
        'exp_dmg_head': 'mean_squared_error',
        'exp_win_head': 'mean_squared_error',
        'policy_head': softmax_cross_entropy_with_logits
    },
        optimizer=tf.keras.optimizers.SGD(learning_rate=0.1, momentum=0.9),
        # optimizer='adam',
        # loss_weights={'exp_dmg_head': 0.5, 'policy_head': 0.5}
        loss_weights={'exp_dmg_head': 0.33, 'policy_head': 0.33, 'exp_win_head': 0.33}
    )

evalCache.set_cache_model(model)

start = time.time()

np.set_printoptions(threshold=np.inf)


def playMatches(match_count, training_f=None, node_count=0):
    num_deaths = 0
    total_dmg = 0
    node_count = NODE_COUNT if node_count == 0 else node_count
    if training_f is None:
        try:
            os.remove('./tmp/matches.txt')
        except:
            pass
    for game_i in range(0, match_count):
        state = orig_state.clone()
        state.enemy.health = 90
        mcts = MCTS(state, model)
        states = []
        while not state.is_terminal():
            upto = node_count - state.total_n
            for i in range(0, upto):
                _, num_of_actions = mcts.search(mcts.state, remaining_calls=upto - i)
                if num_of_actions == 1:
                    break
            next_state = None
            max_n = 0
            action = 0
            m = max(state.n)
            max_action = -1
            # for i in range(0, len(state.policy)):
            #     if m == state.n[i] and max_action < 0:
            #         max_action = i
            #     if state.n[i] > 0 and state.q[i] / m >= max_n:
            #         action = i
            #         next_state = state.ns[i]
            #         max_n = state.q[i] / m
            # if max_action != action:
            #     print(state)
            #     print(f'{max_action} != {action}')
            for i in range(0, len(state.policy)):
                if state.n[i] > max_n:
                    action = i
                    next_state = state.ns[i]
                    max_n = state.n[i]
            if isinstance(next_state, ChanceState):
                next_state = next_state.get_next_state()
                if next_state.policy is None:
                    next_state.do_eval()
            states.append((state, action))
            mcts.state = next_state
            state = mcts.state
        if state.is_terminal() == 1:
            total_dmg += orig_state.player.orig_health - state.player.health
        if state.is_terminal() == -1:
            num_deaths += 1
        if training_f is None:
            with open('./tmp/matches.txt', 'a+') as f:
                f.write(f'*** Match {game_i + 1} ***\n')
                f.write(f'Result: {"Win" if state.is_terminal() == 1 else "Loss"}\n')
                f.write(f'Damage Taken: {orig_state.player.orig_health - state.player.health}\n')
                for state, action in states:
                    f.write(f'{state}\n')
                    f.write(f'action={action}\n')
                f.write('\n')
                f.write('\n')
            print(game_i)
    print(f'Avg Dmg={total_dmg / (match_count - num_deaths) if num_deaths != match_count else "N/A"}')
    print(f'Deaths={num_deaths}')
    if training_f is not None:
        training_f.write(f'Avg Dmg={total_dmg / (match_count - num_deaths) if num_deaths != match_count else "N/A"}\n')
        training_f.write(f'Deaths={num_deaths}\n')


if DO_TRAINING:
    training_f = open('./tmp/training.txt', 'a+')
    for _iterations in range(0, ITERATION_COUNT):
        training_f.write(f'Iteration {_iterations}\n')
        start2 = start
        states_pair = []
        for _ in range(0, 200):
            state = orig_state.clone()
            b = rand.randint(1, 20)
            if b < 20:
                state.enemy.health = round(orig_state.enemy.health * b / 20)
            else:
                state.enemy.health = rand.randint(85, 90)
            mcts = MCTS(state, model)
            states = []
            turn_count = 0
            while not state.is_terminal():
                states.append(state)
                for i in range(0, 50):
                    mcts.search(mcts.state, True)
                if turn_count >= 0:
                    next_state = None
                    max_n = 0
                    m = max(state.n)
                    # for i in range(0, len(state.policy)):
                    #     if state.n[i] > 0 and state.q[i] / m >= max_n:
                    #         next_state = state.ns[i]
                    #         max_n = state.q[i] / m
                    for i in range(0, len(state.policy)):
                        if state.n[i] > max_n:
                            next_state = state.ns[i]
                            max_n = state.n[i]
                else:
                    acc = 0
                    r = rand.randint(0, state.total_n - 1)
                    for i in range(0, len(state.policy)):
                        acc += state.n[i]
                        if acc > r:
                            next_state = state.ns[i]
                            action = i
                            break
                    if action + 1 == len(state.policy):
                        turn_count += 1
                if isinstance(next_state, ChanceState):
                    next_state = next_state.get_next_state()
                    if next_state.policy is None:
                        next_state.do_eval()
                        mcts.state = next_state
                    else:
                        mcts.state = next_state.clone()
                else:
                    mcts.state = next_state.clone()
                state = mcts.state
            e_dmg = [state.player.health / state.player.orig_health]
            e_win = [1 if state.player.health > 0 else 0]
            for s in states:
                states_pair.append((s, e_dmg, e_win))
        print(time.time() - start)
        print(len(states_pair))
        training_f.write(f'Time Iteration: {time.time() - start2}\n')
        training_f.write(f'Time Accumulated: {time.time() - start}\n')
        training_f.write(f'States Accumulated: {len(states_pair)}\n')
        evalCache.printStats()

        for i in range(10):
            minibatch = rand.sample(states_pair, len(states_pair) // 20)
            minibatch = states_pair
            x_train = []
            exp_dmg_head_train = []
            exp_win_head_train = []
            policy_head_train = []
            for s, e_dmg, e_win in minibatch:
                x_train.append(np.asarray(s.get_input()) / 1.0)
                exp_dmg_head_train.append(np.asarray(e_dmg).reshape(1, 1))
                exp_win_head_train.append(np.asarray(e_win).reshape(1, 1))
                p = []
                for n in s.n:
                    p.append(n / s.total_n)
                policy_head_train.append(np.asarray(p).reshape(1, len(s.n)))
            x_train = np.asarray(x_train)
            exp_dmg_head_train = np.asarray(exp_dmg_head_train)
            exp_win_head_train = np.asarray(exp_win_head_train)
            policy_head_train = np.asarray(policy_head_train)
            model.fit(np.asarray(x_train), [exp_dmg_head_train, exp_win_head_train, policy_head_train], epochs=2)
        evalCache.set_cache_model(model)
        model.save('saves/model')
        if not SKIP_TRAINING_MATCHES:
            playMatches(100, training_f)
        training_f.write(f'Time Accumulated: {time.time() - start}\n')
        for i in range(5, 91, 5):
            state = orig_state.clone()
            state.enemy.health = i
            state.startTurn()
            state.do_eval()
            training_f.write(f'Health {i}: {state}\n')
        training_f.flush()
    training_f.close()

if DO_TRAINING:
    for i in range(5, 91, 5):
        state = orig_state.clone()
        state.enemy.health = i
        state.startTurn()
        state.do_eval()
        print(state)

if PLAY_A_GAME:
    state = orig_state.clone()
    state.enemy.health = 85
    mcts = MCTS(state, model)
    states = []
    depth = 0
    while not state.is_terminal():
        states.append(state)
        upto = NODE_COUNT - state.total_n
        for i in range(0, upto):
            _, num_of_actions = mcts.search(mcts.state, remaining_calls=upto - i)
            if num_of_actions == 1:
                break
        next_state = None
        max_n = 0
        action = 0
        for i in range(0, len(state.policy)):
            if state.n[i] > max_n:
                action = i
                next_state = state.ns[i]
                max_n = state.n[i]
        if isinstance(next_state, ChanceState):
            next_state = next_state.get_next_state()
            if next_state.policy is None:
                next_state.do_eval()
        mcts.print_tree(1)
        print(f'action={action}')
        mcts.state = next_state
        state = mcts.state
        depth += 1
    print(time.time() - start)
    evalCache.printStats()


if PLAY_MATCHES:
    playMatches(ITERATION_COUNT)
    print(time.time() - start)
    evalCache.printStats()
