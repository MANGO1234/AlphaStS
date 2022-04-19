package com.alphaStS;

import com.alphaStS.utils.BigRational;
import com.alphaStS.utils.Utils;

import java.util.*;

public class GameSolver {
    GameState origState;
    HashMap<GameState, GameState> nodes;

    public GameSolver(GameState origState) {
        this.origState = origState.clone(false);
        nodes = new HashMap<>();
    }

    private GameState solveH(GameState state) {
        var cachedState = nodes.get(state);
        if (cachedState != null) {
            return cachedState;
        }
        if (state.isTerminal() != 0) {
            state.e_health = BigRational.valueOf(state.getPlayeForRead().getHealth());
            state.e_win = state.isTerminal() == 1 ? BigRational.ONE : BigRational.ZERO;
            nodes.put(state, state);
            return state;
        }
        if (state.actionCtx == GameActionCtx.START_GAME) {
            ChanceState cState = new ChanceState(null, state, 0);
            generateAllPossibilities(cState);
            calcExpectedHealth(cState);
            state.e_health = cState.e_health;
            state.e_win = cState.e_win;
            state.ns = new State[1];
            state.ns[0] = cState;
        } else {
            BigRational maxHealth = BigRational.ZERO, maxWin = BigRational.ZERO;
            boolean hasOtherAction = false;
            for (int i = 0; i < state.getLegalActions().length; i++) {
                var action = state.getAction(i);
                if (action.type() == GameActionType.PLAY_CARD && state.prop.cardDict[action.cardIdx()].cardType == Card.ATTACK) {
//                if (action.type() == GameActionType.PLAY_CARD) {
                    hasOtherAction = true;
                } else if (action.type() == GameActionType.END_TURN) {
                    if (hasOtherAction) {
                        continue;
                    }
                }
                GameState s = state.clone(false);
                s.doAction(i);
                if (s.isStochastic) {
                    ChanceState cState = new ChanceState(null, state, i);
                    generateAllPossibilities(cState);
                    calcExpectedHealth(cState);
                    int c = cState.e_win.compareTo(maxWin) ;
                    if (c > 0) {
                        maxWin = cState.e_win;
                        maxHealth = cState.e_health;
                    } else if (c == 0 && cState.e_health.compareTo(maxHealth) > 0) {
                        maxHealth = cState.e_health;
                    }
                    state.ns = new State[state.getLegalActions().length];
                    state.ns[i] = cState;
                } else {
                    s = solveH(s);
                    int c = s.e_win.compareTo(maxWin);
                    if (c > 0) {
                        maxWin = s.e_win;
                        maxHealth = s.e_health;
                    } else if (c == 0 && s.e_health.compareTo(maxHealth) > 0) {
                        maxHealth = s.e_health;
                    }
                    if (state.ns == null) {
                        state.ns = new State[state.getLegalActions().length];
                    }
                    state.ns[i] = s;
                }
            }
            state.e_health = maxHealth;
            state.e_win = maxWin;
        }
        nodes.put(state, state);
        return state;
    }

    private void calcExpectedHealth(ChanceState cState) {
        BigRational e_health = BigRational.ZERO, e_win = BigRational.ZERO;
        for (ChanceState.Node node : cState.cache.values()) {
            e_health = e_health.add(new BigRational(node.n, cState.total_node_n).multiply(node.state.e_health));
            e_win = e_win.add(new BigRational(node.n, cState.total_node_n).multiply(node.state.e_win));
        }
        cState.e_health = e_health;
        cState.e_win = e_win;
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

    private void generateAllPossibilities(ChanceState cState) {
        var state = cState.parentState;
        var action = cState.parentAction;
        if (state.getAction(action).type() == GameActionType.BEGIN_TURN || state.actionCtx == GameActionCtx.START_GAME) {
            var modState = state.clone(false);
            modState.discardHand();
            int toDraw = 5;
//            if (state.actionCtx == GameActionCtx.START_GAME && (state.deck[state.prop.findCardIndex(new CardSilent.Survivor())] > 0)) {
//                toDraw = 7;
//            }
            if (modState.deckArrLen < toDraw) {
                modState.draw(modState.deckArrLen);
                toDraw -= modState.deckArrLen;
                modState.reshuffle();
            }
            cState.total_node_n = factorials[modState.deckArrLen] / factorials[modState.deckArrLen - toDraw] / factorials[toDraw];
            generateDraw(cState, modState, toDraw, 0, 1);
            var n = 0;
            for (var e : cState.cache.entrySet()) {
                n += e.getValue().n;
            }
            if (n != cState.total_node_n) {
                System.out.println(n + "," + cState);
                throw new RuntimeException();
            }
            if (cState.cache.size() == 0) {
                throw new RuntimeException();
            }
        } else {
            throw new RuntimeException();
        }
//        for (int i = 0; i < 100; i++) {
//            var nextState = cState.getNextState(false);
//            solveH(nextState);
//        }
    }

    private void generateDraw(ChanceState cState, GameState modState, int toDraw, int i, long n) {
        if (toDraw == 0) {
            var newState = cState.parentState.clone(false);
            newState.doAction(cState.parentAction);
            newState.hand = Arrays.copyOf(modState.hand, modState.hand.length);
            newState.deck = Arrays.copyOf(modState.deck, modState.deck.length);
            newState.discard = Arrays.copyOf(modState.discard, modState.discard.length);
            newState.deckArr = Arrays.copyOf(modState.deckArr, modState.deckArr.length);
            newState.deckArrLen = modState.deckArrLen;
            newState = solveH(newState);
            var node = new ChanceState.Node(newState);
            node.n = n;
            cState.cache.put(newState, node);
            return;
        }
        if (i >= modState.deck.length) {
            return;
        }
        generateDraw(cState, modState, toDraw, i + 1, n);
        int upto = Math.min(toDraw, modState.deck[i]);
        int deck_i = modState.deck[i];
        int start_len = modState.deckArrLen;;
        for (int j = 1; j <= upto; j++) {
            modState.drawCardByIdx(i, true);
            generateDraw(cState, modState, toDraw - j, i + 1, n * nCr(deck_i, j));
        }
        for (int j = 1; j <= upto; j++) {
            modState.undrawCardByIdx(i);
        }
        if (modState.deckArrLen != start_len) {
            throw new RuntimeException();
        }
    }


    public void solve() {
        solveH(origState);
    }

    public void printResult() {
        var e_win = origState.e_win.toDouble();
        var e_health = origState.e_health.toDouble();
        var e_winString = origState.e_win.getNumerator() + "/" + origState.e_win.getDenominator();
        var e_healthString = origState.e_health.getNumerator() + "/" + origState.e_health.getDenominator();
        System.out.println(origState + ": " + Utils.formatFloat(e_win) + " (" + e_winString + "), " + Utils.formatFloat(e_health) + " (" + e_healthString + ")");
    }

    public List<Integer> isBestAction(GameState stateArg, int action) {
        var state = nodes.get(stateArg);
        if (state == null) {
            return null;
        }
        BigRational maxWin = BigRational.ZERO, maxHealth = BigRational.ZERO;
        for (int i = 0; i < state.getLegalActions().length; i++) {
            if (state.ns[i] instanceof GameState state2) {
                int c = state2.e_win.compareTo(maxWin);
                if (c > 0) {
                    maxWin = state2.e_win;
                    maxHealth = state2.e_health;
                } else if (c == 0 && state2.e_health.compareTo(maxHealth) > 0) {
                    maxWin = state2.e_win;
                    maxHealth = state2.e_health;
                }
            } else if (state.ns[i] instanceof ChanceState cState) {
                int c = cState.e_win.compareTo(maxWin);
                if (c > 0) {
                    maxWin = cState.e_win;
                    maxHealth = cState.e_health;
                } else if (c == 0 && cState.e_health.compareTo(maxHealth) > 0) {
                    maxWin = cState.e_win;
                    maxHealth = cState.e_health;
                }
            } else if (state.getAction(i).type() != GameActionType.END_TURN) {
                throw new RuntimeException();
            }
        }

        BigRational e_win = BigRational.ZERO, e_health = BigRational.ZERO;
        if (state.ns[action] instanceof GameState state2) {
            e_win = state2.e_win;
            e_health = state2.e_health;
        } else if (state.ns[action] instanceof ChanceState cState) {
            e_win = cState.e_win;
            e_health = cState.e_health;
        } else if (state.getAction(action).type() != GameActionType.END_TURN) {
            throw new RuntimeException();
        }
        if (e_win.compareTo(maxWin) == 0 && e_health.compareTo(maxHealth) == 0) {
            return null;
        }

        var ls = new ArrayList<Integer>();
        for (int i = 0; i < state.getLegalActions().length; i++) {
            if (state.ns[i] instanceof GameState state2) {
                if (state2.e_win.compareTo(maxWin) == 0 && state2.e_health.compareTo(maxHealth) == 0) {
                    ls.add(i);
                }
            } else if (state.ns[i] instanceof ChanceState cState) {
                if (cState.e_win.compareTo(maxWin) == 0 && cState.e_health.compareTo(maxHealth) == 0) {
                    ls.add(i);
                }
            } else if (state.getAction(i).type() != GameActionType.END_TURN) {
                throw new RuntimeException();
            }
        }
        return ls;
    }

    public int checkForError(MatchSession.Game game) {
        int count = 0;
        for (int i = 0; i < game.steps().size() - 1; i++) {
            count += checkForError(game.steps().get(i).state(), game.steps().get(i).action(), false) ? 0 : 1;
        }
        return count;
    }

    public boolean checkForError(GameState state, int action, boolean print) {
        var optimalAction = isBestAction(state, action);
        if (optimalAction == null) {
            return true;
        }
        if (print) {
            System.out.println(state);
            System.out.println("Chosen Action=" + state.getActionString(action));
            var s = optimalAction.stream().map(state::getActionString).toList();
            System.out.println("Optimal Action(s)=" + String.join(", ", s));

            var cachedState = nodes.get(state);
            var child = cachedState.ns[action];
            BigRational e_win = BigRational.ZERO, e_health = BigRational.ZERO;
            if (child instanceof GameState state2) {
                e_win = state2.e_win;
                e_health = state2.e_health;
            } else if (child instanceof ChanceState cState) {
                e_win = cState.e_win;
                e_health = cState.e_health;
            } else if (cachedState.getAction(action).type() != GameActionType.END_TURN){
                throw new RuntimeException();
            }
            var e_winString = e_win.getNumerator() + "/" + e_win.getDenominator();
            var e_healthString = e_health.getNumerator() + "/" + e_health.getDenominator();
            System.out.println("Chosen Action E: " + Utils.formatFloat(e_win.toDouble()) + " (" + e_winString + "), " + Utils.formatFloat(e_health.toDouble()) + " (" + e_healthString + ")");

            child = cachedState.ns[optimalAction.get(0)];
            if (child instanceof GameState state2) {
                e_win = state2.e_win;
                e_health = state2.e_health;
            } else if (child instanceof ChanceState cState) {
                e_win = cState.e_win;
                e_health = cState.e_health;
            } else {
                throw new RuntimeException();
            }
            e_winString = e_win.getNumerator() + "/" + e_win.getDenominator();
            e_healthString = e_health.getNumerator() + "/" + e_health.getDenominator();
            System.out.println("Optimal Action E: " + Utils.formatFloat(e_win.toDouble()) + " (" + e_winString + "), " + Utils.formatFloat(e_health.toDouble()) + " (" + e_healthString + ")");


            if (e_healthString.equals("20/1")) {
//                System.out.println(state.searchFrontier);
//                GameStateUtils.printTree(state, null, 5);
//                Integer.parseInt(null);
            }
        }
        return false;
    }
}
