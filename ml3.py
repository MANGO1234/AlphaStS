# format

import random as rand
import math
import time
import os
import tensorflow as tf
import numpy as np
from tensorflow import keras
from tensorflow.keras import layers
from misc import getFlag, getFlagValue

# rand.seed(5)
# np.random.seed(5)
# tf.random.set_seed(5)

do_play_only = False
play_matches = True
training_iterations = 5
# tf.config.threading.set_inter_op_parallelism_threads(6)
# tf.config.threading.set_intra_op_parallelism_threads(6)

tttt = 0


class EvalCache:

    def __init__(self):
        self.cache = {}
        self.model = None
        self.calls = 0
        self.hits = 0

    def set_cache_model(self, model):
        self.cache = {}
        self.model = model
        self.calls = 0
        self.hits = 0

    def get_eval(self, state):
        global tttt
        self.calls += 1
        x = state.get_input()
        s = tuple(x)
        if s in self.cache:
            self.hits += 1
            return self.cache[s]
        start2 = time.time()
        e = self.model(tf.constant([x]))
        tttt += time.time() - start2
        self.cache[s] = e
        return e


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

    def enemyTurn(self):
        self.enemy.do_move(self.player)

    def do_action(self, action):
        if action == len(self.policy) - 1:
            self.enemyTurn()
            self.endTurn()
            self.startTurn()
        else:
            self.play_card(action)
        self.policy = None
        self.v_win = 0
        self.v_health = 0

    def clone(self):
        class Empty:
            pass
        clone = Empty()
        clone.__class__ = self.__class__
        clone.card_dict = self.card_dict
        clone.deck_arr = self.deck_arr.copy()
        clone.deck = self.deck.copy()
        clone.hand = self.hand.copy()
        clone.discard = self.discard.copy()
        clone.energy = self.energy
        clone.energy_refill = self.energy_refill
        clone.enemy = self.enemy.clone()
        clone.player = self.player.clone()

        clone.v_win = self.v_win
        clone.v_health = self.v_health
        clone.policy = self.policy
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
        for i in range(0, len(self.card_dict)):
            x.append(self.discard[i] / 10)
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

    def do_eval(self, model):
        # self.v = 0.5
        y = evalCache.get_eval(self)
        # self.v_win = y[0][0].numpy()[0]
        self.v_win = 1
        self.v_health = y[0][0].numpy()[0]
        n = y[1][0].numpy()
        for i in range(0, len(n)):
            if not self.is_action_legal(i):
                n[i] = -1000
        self.policy = tf.nn.softmax(tf.constant(n)).numpy()

    def is_action_legal(self, action):
        if action < len(self.card_dict):
            if self.hand[action] == 0:
                return False
            cost = self.card_dict[action].energyCost(self)
            if cost < 0 or cost > self.energy:
                return False
        return True

    def get_v(self):
        is_terminal = self.is_terminal()
        if is_terminal == 1:
            return self.player.health / self.player.orig_health
        elif is_terminal == -1:
            return 0
        return self.v_win * self.v_health

    def is_terminal(self):
        if self.player.health <= 0:
            return -1
        if self.enemy.health <= 0:
            return 1
        return 0

    def __repr__(self):
        return f"discard={self.discard}, hand={self.hand}, deck={self.deck}, player={self.player}, enemy={self.enemy}, p={self.policy}, v={(self.v_win, self.v_health)}, q={state.q}, n={state.n}"


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

    def clone(self):
        class Empty:
            pass
        clone = Empty()
        clone.__class__ = self.__class__
        clone.orig_health = self.orig_health
        clone.max_health = self.max_health
        clone.health = self.health
        clone.strength = self.strength
        clone.vulnerable = self.vulnerable
        clone.block = self.block
        return clone

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

    def clone(self):
        class Empty:
            pass
        clone = Empty()
        clone.__class__ = self.__class__
        clone.health = self.health
        clone.max_health = self.max_health
        clone.strength = self.strength
        clone.vulnerable = self.vulnerable
        clone.block = self.block
        return clone

    def __repr__(self):
        return f"Enemy(health={self.health}, vuln={self.vulnerable})"


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

    def clone(self):
        clone = super().clone()
        clone.num_of_moves = self.num_of_moves
        clone.move = self.move
        clone.turn = self.turn
        clone.last_move = self.last_move
        clone.last_move_2 = self.last_move_2
        return clone

    def react(self, card):
        if card.card_type == Card.SKILL and self.turn > 1:
            self.strength += 3


class Card:
    ATTACK = 0
    SKILL = 1
    POWER = 2
    CURSE = 3

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


class MCTS:

    def __init__(self, state, model):
        self.state = state
        self.model = model
        state.startTurn()
        state.do_eval(model)

    def search(self, state, training=False, remaining_calls=10000000):
        is_terminal = state.is_terminal()
        if is_terminal != 0:
            return state.get_v(), None
        action = 0
        max_so_far = -1000000
        num_of_actions = 0
        if training:
            policy = np.random.dirichlet([0.2] * len(state.policy))
            policy = policy * 0.25 + state.policy * 0.75
        else:
            policy = state.policy
        max_n = max(state.n)
        for i in range(0, len(policy)):
            if not state.is_action_legal(i):
                continue
            if max_n - state.n[i] > remaining_calls:
                continue
            num_of_actions += 1
            u = state.q[i] + 3 * policy[i] * math.sqrt(state.total_n) / (1 + state.n[i])
            if u > max_so_far:
                action = i
                max_so_far = u
        next_state = state.ns[action]
        if next_state is None:
            if action == len(policy) - 1:
                chance_state = ChanceState(state, action)
                state.ns[action] = chance_state
                next_state = chance_state.get_next_state()
            else:
                next_state = state.clone()
                state.ns[action] = next_state
                next_state.do_action(action)
            if next_state.policy is None:
                next_state.do_eval(self.model)
            v = next_state.get_v()
        else:
            if isinstance(next_state, ChanceState):
                next_state = next_state.get_next_state()
                if next_state.policy is None:
                    next_state.do_eval(self.model)
                    v = next_state.get_v()
                else:
                    v, _ = self.search(next_state)
            else:
                v, _ = self.search(next_state)
        state.q[action] = (state.q[action] * state.n[action] + v) / (state.n[action] + 1)
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

evalCache = EvalCache()
orig_state = GameState(GremlinNob(90), Player(68, 75), [(CardBash1(), 1), (CardStrike(), 5), (CardDefend(), 4)])
input_len = len(orig_state.get_input())


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
    x = layers.Dense((input_len + 1) // 2, activation="linear", use_bias=True, name="layer1")(inputs)
    # x = layers.BatchNormalization(axis=1)(x)
    x = layers.LeakyReLU()(x)
    x = layers.Dense((input_len + 1) // 2, activation="linear", use_bias=True, name="layer2")(x)
    # x = layers.BatchNormalization(axis=1)(x)
    x = layers.LeakyReLU()(x)
    # x1 = layers.Dense((input_len + 1) // 2, activation="linear", use_bias=True, name="layer3")(x)
    # x1 = layers.BatchNormalization(axis=1)(x1)
    # x1 = layers.LeakyReLU()(x1)
    # x2 = layers.Dense((input_len + 1) // 2, activation="linear", use_bias=True, name="layer4")(x)
    # x2 = layers.BatchNormalization(axis=1)(x2)
    # x2 = layers.LeakyReLU()(x2)
    value_head = layers.Dense(1, name="value_head", use_bias=True, activation='sigmoid')(x)
    policy_head = layers.Dense(len(orig_state.card_dict) + 1, use_bias=True, activation='linear', name="policy_head")(x)
    model = keras.Model(inputs=[inputs], outputs=[value_head, policy_head])
    model.compile(loss={
        'value_head': 'mean_squared_error',
        'policy_head': softmax_cross_entropy_with_logits
    },
        optimizer=tf.keras.optimizers.SGD(learning_rate=0.1, momentum=0.9),
        # optimizer='adam',
        loss_weights={'value_head': 0.5, 'policy_head': 0.5}
    )

evalCache.set_cache_model(model)

start = time.time()

# print(orig_state.get_input())
# print(model(orig_state.get_input()))
# print(model(orig_state.get_input())[1][0] print(list(tf.nn.softmax(model(orig_state.get_input())[1][0]).numpy()))
np.set_printoptions(threshold=np.inf)

if not do_play_only:
    for _iterations in range(0, training_iterations):
        states_pair = []
        for _ in range(0, 300):
            state = orig_state.clone()
            state.enemy.health = rand.randint(1, orig_state.enemy.health)
            # for i in range(0, range.randint(0, state.numberOfCards)):
            #     state.draw()
            #     state.discardHand()
            mcts = MCTS(state, model)
            states = []
            turn_count = 0
            while not state.is_terminal():
                states.append(state)
                for i in range(0, 50):
                    mcts.search(mcts.state, True)
                # mcts.print_tree(1)
                if turn_count >= 0:
                    next_state = None
                    max_n = 0
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
                        next_state.do_eval(model)
                        mcts.state = next_state
                    else:
                        mcts.state = next_state.clone()
                else:
                    mcts.state = next_state.clone()
                state = mcts.state
            # v = [1 if state.player.health > 0 else 0, state.player.health / state.player.orig_health]
            v = [state.player.health / state.player.orig_health]
            for s in states:
                states_pair.append((s, v))
        print(tttt)
        print(time.time() - start)
        print(len(states_pair))
        print(evalCache.calls, evalCache.hits)
        # print(np.asarray(x_train))
        # print(value_head_train)
        # print(policy_head_train)

        for i in range(1):
            # minibatch = rand.sample(states_pair, len(states_pair) // 20)
            minibatch = states_pair
            x_train = []
            value_head_train = []
            policy_head_train = []
            for s, v in minibatch:
                x_train.append(np.asarray(s.get_input()) / 1.0)
                value_head_train.append(np.asarray(v).reshape(1, 1))
                p = []
                for n in s.n:
                    p.append(n / s.total_n)
                policy_head_train.append(np.asarray(p).reshape(1, len(s.n)))
            x_train = np.asarray(x_train)
            value_head_train = np.asarray(value_head_train)
            policy_head_train = np.asarray(policy_head_train)
            model.fit(np.asarray(x_train), [value_head_train, policy_head_train], epochs=2)
        evalCache.set_cache_model(model)
        model.save('saves/model')


for i in range(10, 90):
    state = orig_state.clone()
    state.enemy.health = i
    state.startTurn()
    state.do_eval(model)
    print(state)

state = orig_state.clone()
state.enemy.health = 85
mcts = MCTS(state, model)
states = []
depth = 0
while not state.is_terminal():
    states.append(state)
    upto = 3000 - state.total_n
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
            next_state.do_eval(model)
    mcts.print_tree(1)
    print(f'action={action}')
    mcts.state = next_state
    state = mcts.state
    depth += 1
print(tttt)
print(time.time() - start)
print(evalCache.calls, evalCache.hits)

if play_matches:
    dmg = 0
    death = 0
    match_count = 100
    for game_i in range(0, match_count):
        state = orig_state.clone()
        state.enemy.health = 85
        mcts = MCTS(state, model)
        states = []
        while not state.is_terminal():
            states.append(state)
            upto = 3000 - state.total_n
            for i in range(0, upto):
                _, num_of_actions = mcts.search(mcts.state, remaining_calls=upto - i)
                if num_of_actions == 1:
                    break
            # mcts.print_tree(1)
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
                    next_state.do_eval(model)
            # print(f'action={action}')
            mcts.state = next_state
            state = mcts.state
        if state.is_terminal() == 1:
            dmg += orig_state.player.orig_health - state.player.health
        if state.is_terminal() == -1:
            death += 1
        print(game_i)
    print(f'Deaths={death}')
    print(f'Avg Dmg={dmg / (match_count - death)}')
    print(evalCache.calls, evalCache.hits)

print(tttt)
print(time.time() - start)
print(evalCache.calls, evalCache.hits)
