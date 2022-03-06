package com.alphaStS;

import java.util.Arrays;
import java.util.HashMap;

public class GameSolver {
    GameState origState;
    HashMap<InputHash, GameState> nodes;

    public GameSolver(GameState origState) {
        this.origState = origState.clone(false);
        nodes = new HashMap<>();
    }

    private GameState solveH(GameState state) {
        InputHash hash = new InputHash(state.getInput());
        GameState node = nodes.get(hash);
        if (node != null) {
            return node;
        }
        if (state.isTerminal() != 0) {
            state.v_health = state.player.health;
            return state;
        }
        if (state.actionCtx == GameActionCtx.START_GAME) {
            ChanceState cState = new ChanceState();
            generateAllPossibilities(cState, state, 0);
            state.v_health = calcExpectedHealth(cState);
            cState.v_health = state.v_health;
            state.ns[0] = cState;
        } else {
            double maxHealth = 0;
            boolean hasOtherAction = false;
            for (int i = 0; i < state.prop.maxNumOfActions; i++) {
                if (!state.isActionLegal(i)) {
                    continue;
                }
                var action = state.getAction(i);
                if (action.type() == GameActionType.PLAY_CARD && state.prop.cardDict[action.cardIdx()].cardType == Card.ATTACK) {
                    hasOtherAction = true;
                } else if (action.type() == GameActionType.END_TURN) {
                    if (hasOtherAction) {
                        continue;
                    }
                }
                GameState s = state.clone(false);
                s.doAction(i);
                if (s.isStochastic) {
                    ChanceState cState = new ChanceState();
                    generateAllPossibilities(cState, state, i);
                    cState.v_health = calcExpectedHealth(cState);
                    maxHealth = Math.max(maxHealth, cState.v_health);
                    state.ns[i] = cState;
                } else {
                    s = solveH(s);
                    maxHealth = Math.max(maxHealth, s.v_health);
//                    if (s.isTerminal && s.player.health == state.player.health) {
//                        state.isTerminal = true;
//                        state.v_health = state.player.health;
//                        nodes.put(hash, state);
//                        return state;
//                    }
                    state.ns[i] = s;
                }
            }
            state.v_health = maxHealth;
        }
        nodes.put(hash, state);
        return state;
    }

    private double calcExpectedHealth(ChanceState cState) {
        double e = 0;
        for (ChanceState.Node node : cState.cache.values()) {
            e += ((double) node.n) / cState.total_n * node.state.v_health;
        }
        return e;
    }

    //cache of the factorials from 1 to 20 (20! is the maximum a long can hold)
    //Use in the method factorial(int n) -> faster and no calculation
    private final static long[] factorials = {1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880, 3628800,
            39916800, 479001600,  6227020800l, 87178291200l, 1307674368000l,
            20922789888000l, 355687428096000l, 6402373705728000l,
            121645100408832000l, 2432902008176640000l};

    public static long nCr(int n, int r) {
        int difference = n - r;
        if (difference > r) {
            difference = r;
        }
        long product = 1;
        for (int i = 1; i <= difference; ++i, --n) {
            product *= n;
            product /= i;
        }
        return product;
    }

    private void generateAllPossibilities(ChanceState cState, GameState state, int action) {
        if (state.getAction(action).type() == GameActionType.END_TURN || state.actionCtx == GameActionCtx.START_GAME) {
            var m_state = state.clone(false);
            m_state.discardHand();
            int toDraw = 5;
            if (m_state.deckArrLen < toDraw) {
                m_state.draw(m_state.deckArrLen);
                toDraw -= m_state.deckArrLen;
                m_state.reshuffle();
            }
            var mm = m_state.clone(false);
            cState.total_n = factorials[m_state.deckArrLen] / factorials[m_state.deckArrLen - toDraw] / factorials[toDraw];
            gen_draw(cState, state, m_state, toDraw, action, 0, 1);
            if (cState.cache.size() == 0) {
                throw new RuntimeException();
            }
        } else {
            throw new RuntimeException();
        }
//        for (int i = 0; i < 5000; i++) {
//            var nextState = cState.getNextState(state, action);
//            solveH(nextState);
//        }
    }

    private void gen_draw(ChanceState cState, GameState state, GameState m_state, int toDraw, int action, int i, long n) {
        if (toDraw == 0) {
            var newState = state.clone(false);
            newState.doAction(action);
            newState.hand = Arrays.copyOf(m_state.hand, m_state.hand.length);
            newState.deck = Arrays.copyOf(m_state.deck, m_state.deck.length);
            newState.discard = Arrays.copyOf(m_state.discard, m_state.discard.length);
            newState.deckArr = Arrays.copyOf(m_state.deckArr, m_state.deckArr.length);
            newState.deckArrLen = m_state.deckArrLen;
            newState = solveH(newState);
            var node = new ChanceState.Node(newState);
            node.n = n;
            cState.cache.put(newState, node);
            return;
        }
        if (i >= m_state.deck.length) {
            return;
        }
        gen_draw(cState, m_state, m_state, toDraw, action, i + 1, n);
        int upto = Math.min(toDraw, m_state.deck[i]);
        int deck_i = m_state.deck[i];
        int start_len = m_state.deckArrLen;;
        for (int j = 1; j <= upto; j++) {
            m_state.drawCardByIdx(i);
            gen_draw(cState, m_state, m_state, toDraw - j, action, i + 1, n * nCr(deck_i, j));
        }
        for (int j = 1; j <= upto; j++) {
            m_state.undrawCardByIdx(i);
        }
        if (m_state.deckArrLen != start_len) {
            throw new RuntimeException();
        }
    }


    public void solve() {
        solveH(origState);
    }
}
