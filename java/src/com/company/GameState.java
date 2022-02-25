package com.company;

import java.util.*;

public class GameState implements State {
    int[] deckArr;
    int deckLen;
    Card[] cardDict;
    int[] deck;
    int[] hand;
    int[] discard;
    int energy;
    int energyRefill;
    Enemy enemy;
    Player player;
    Random random;
    double v_win;
    double v_health;
    boolean[] actionsCache;
    double[] q;
    int[] n;
    State[] ns;
    int total_n;
    int numOfActions;
    double[] policy;

    public GameState(Enemy enemy, Player player, List<CardCount> cards) {
        cards.sort(Comparator.comparing(a -> a.card().cardName));
        numOfActions = cards.size() + 1;
        cardDict = new Card[cards.size()];
        deck = new int[cards.size()];
        hand = new int[cards.size()];
        discard = new int[cards.size()];
        for (int i = 0; i < cards.size(); i++) {
            cardDict[i] = cards.get(i).card();
            deck[i] = cards.get(i).count();
            hand[i] = 0;
            discard[i] = 0;
            deckLen += deck[i];
        }
        deckArr = new int[deckLen];
        int idx = 0;
        for (int i = 0; i < cards.size(); i++) {
            for (int j = 0; j < cards.get(i).count(); j++) {
                deckArr[idx++] = i;
            }
        }
        energy = 3;
        energyRefill = 3;
        this.enemy = enemy;
        this.player = player;
        random = new Random();

        policy = null;
        q = new double[numOfActions];
        n = new int[numOfActions];
        ns = new State[numOfActions];
    }

    public GameState(GameState other) {
        numOfActions = other.numOfActions;
        cardDict = other.cardDict;
        deckArr = Arrays.copyOf(other.deckArr, other.deckArr.length);
        deck = Arrays.copyOf(other.deck, other.deck.length);
        hand = Arrays.copyOf(other.hand, other.hand.length);
        discard = Arrays.copyOf(other.discard, other.discard.length);
        energy = other.energy;
        energyRefill = other.energyRefill;
        enemy = other.enemy.copy();
        player = new Player(other.player);
        random = other.random;

        policy = null;
        q = new double[numOfActions];
        n = new int[numOfActions];
        ns = new State[numOfActions];
    }

    void draw() {
        if (deckLen == 0) {
            for (int i = 0; i < discard.length; i++) {
                for (int j = 0; j < discard[i]; j++) {
                    deckArr[deckLen++] = i;
                }
                deck[i] = discard[i];
                discard[i] = 0;
            }
        }
        if (deckLen == 0) {
            return;
        }
        int i = random.nextInt(this.deckLen);
        deck[deckArr[i]] -= 1;
        hand[deckArr[i]] += 1;
        deckArr[i] = deckArr[deckLen - 1];
        deckLen -= 1;
    }

    void playCard(int i) {
        assert cardDict[i].energyCost(this) > 0;
        assert energy >= cardDict[i].energyCost(this);
        assert hand[i] > 0;
        hand[i] -= 1;
        energy -= cardDict[i].energyCost(this);
        cardDict[i].play(this);
        discard[i] += 1;
    }

    void startTurn() {
        this.enemy.nextMove();
        this.energy = this.energyRefill;
        for (int i = 0; i < 5; i++) {
            this.draw();
        }
    }

    private void endTurn() {
        enemy.doMove(player);
        for (int i = 0; i < discard.length; i++) {
            if (!cardDict[i].exhaustEndOfTurn) {
                discard[i] += hand[i];
            }
            hand[i] = 0;
        }
        enemy.endTurn();
        player.endTurn();
    }

    void doAction(int action) {
        if (action == numOfActions - 1) {
            endTurn();
            startTurn();
        } else {
            playCard(action);
        }
        policy = null;
        v_win = 0;
        v_health = 0;
    }


    boolean[] is_action_legal() {
        if (actionsCache != null) {
            return actionsCache;
        }
        boolean[] actions = new boolean[cardDict.length + 1];
        actions[actions.length - 1] = true;
        for (int i = 0; i < cardDict.length; i++) {
            if (hand[i] > 0) {
                int cost = cardDict[i].energyCost(this);
                if (cost >= 0 && cost <= energy) {
                    actions[i] = true;
                }
            }
        }
        actionsCache = actions;
        return actions;
                }

    double get_v() {
        if (player.health <= 0) {
            return ((double) player.health) / player.origHealth;
        }  else if (enemy.health <= 0) {
            return 0;
        }
        return v_health;
    }

    int isTerminal() {
        if (player.health <= 0) {
            return -1;
        }  else if (enemy.health <= 0) {
            return 1;
        }
        return 0;
    }

    @Override public String toString() {
        return "deck=" + Arrays.toString(deck) +
                ", hand=" + Arrays.toString(hand) +
                ", discard=" + Arrays.toString(discard) +
                ", " + player +
                ", " + enemy +
                ", " + enemy +
                ", v=(" + v_win + ", " + v_health + ")" +
                ", p=" + Arrays.toString(policy) +
                ", q=" + Arrays.toString(q) +
                ", n=" + Arrays.toString(n) +
                '}';
    }

    public void doEval(Model model) {
        this.policy = new double[numOfActions];
        Arrays.fill(this.policy, 1.0 / numOfActions);
        this.v_health = player.health / player.origHealth;
        this.v_win = 1 - enemy.health / enemy.maxHealth;
    }
}

interface State {}

class IntHash {

}

class ChanceState implements State {
    class Node {
        GameState state;
        int n;

        public Node(GameState state, int n) {
            this.state = state;
            this.n = n;
        }
    }

    Hashtable<IntHash, Node> cache;
    int total_n;

    public ChanceState() {
        cache = new Hashtable<>();
    }

    GameState getNextState(GameState parentState, int action) {
        var state = new GameState(parentState);
        state.doAction(action);
        //            x = tuple(state.get_input())
        //            self.total_n += 1
        //            if x in self.cache:
        //            node = self.cache[x]
        //            node.n += 1
        //            return node.state
        //            self.cache[x] = ChanceState.Node(state, 1)
        return state;
    }
}

record CardCount(Card card, int count) { }
