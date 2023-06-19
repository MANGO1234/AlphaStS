package com.alphaStS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import cc.mallet.types.Dirichlet;
import com.alphaStS.utils.Tuple;
import com.alphaStS.utils.Utils;
import org.apache.commons.math3.stat.interval.ClopperPearsonInterval;

import static java.lang.Math.sqrt;

public class MCTS {
    public Map<Integer, double[]> exploredActions = null;
    public double[] exploredV;
    Model model;
    int numberOfPossibleActions;
    private final double[] v = new double[20];
    private final double[] realV = new double[20];
    private final int[] ret = new int[2];
    private double terminal_v_win;
    public int forceRootAction = -1;

    void setModel(Model model) {
        this.model = model;
    }

    void search(GameState state, boolean training, int remainingCalls) {
        exploredV = null;
        search2(state, training, remainingCalls);
    }

    void search2(GameState state, boolean training, int remainingCalls) {
        terminal_v_win = -100;
        search2_r(state, training, remainingCalls, true, 0);
    }

    void search2_r(GameState state, boolean training, int remainingCalls, boolean isRoot, int level) {
        if (exploredV != null) {
            for (int i = 0; i < state.prop.v_total_len; i++) {
                state.initSearchInfo2();
                v[i] = exploredV[i];
                state.q[i] += v[i];
            }
            return;
        }
        if (state.terminal_action >= 0) {
            for (int i = 0; i < state.prop.v_total_len; i++) {
                v[i] = state.q[(state.terminal_action + 1) * state.prop.v_total_len + i] / state.n[state.terminal_action];
                realV[i] = v[i];
            }
            numberOfPossibleActions = 1;
            return;
        }
        if (state.isTerminal() != 0) {
            state.get_v(v);
            System.arraycopy(v, 0, realV, 0, state.prop.v_total_len);
            state.initSearchInfo2();
            for (int i = 0; i < state.prop.v_total_len; i++) {
                state.q[i] = v[i];
            }
            if (v[GameState.V_WIN_IDX] > 0.5 && cannotImproveState(state)) {
                terminal_v_win = v[GameState.V_WIN_IDX];
            }
            return;
        }
        if (state.policy == null) {
            state.doEval(model);
            state.get_v(v);
            System.arraycopy(v, 0, realV, 0, state.prop.v_total_len);
            state.initSearchInfo2();
            for (int i = 0; i < state.prop.v_total_len; i++) {
                state.q[i] = v[i];
            }
            numberOfPossibleActions = state.getLegalActions().length;
            state.varianceM = v[GameState.V_COMB_IDX];
            state.varianceS = 0;
            return;
        }
        if (state.n == null) {
            state.initSearchInfo();
        }

        float[] policy = getPolicy(state, training, remainingCalls, isRoot);
        selectAction(state, policy, training, isRoot);
        int action = ret[0];
        int numberOfActions = ret[1];

        State nextState = state.ns[action];
        GameState state2;
        if (isRoot && exploredActions != null && exploredActions.containsKey(action)) {
            exploredV = exploredActions.get(action);
        }
        if (nextState == null) {
            state2 = state.clone(true);
            state2.doAction(action);
            if (state2.isStochastic) {
                if (Configuration.TRANSPOSITION_ACROSS_CHANCE_NODE && (!Configuration.TEST_TRANSPOSITION_ACROSS_CHANCE_NODE || state.prop.testNewFeature)) {
                    var s = state.transpositions.get(state2);
                    if (s == null || s instanceof ChanceState) {
                        if (Configuration.COMBINE_END_AND_BEGIN_TURN_FOR_STOCHASTIC_BEGIN && state2.actionCtx == GameActionCtx.BEGIN_TURN) {
                            state2.doAction(0);
                        } else {
                            state.transpositions.put(state2, state2);
                            if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.prop.testNewFeature)) {
                                if (s == null) {
                                    var parents = new ArrayList<Tuple<GameState, Integer>>();
                                    state.transpositionsParent.put(state2, parents);
                                    parents.add(new Tuple<>(state, action));
                                } else {
                                    state.transpositionsParent.get(state2).add(new Tuple<>(state, action));
                                }
                            }
                        }
                    } else {
                        state2 = (GameState) s;
                        if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.prop.testNewFeature)) {
                            state.transpositionsParent.get(state2).add(new Tuple<>(state, action));
                        }
                    }
                    state.ns[action] = new ChanceState(state2, state, action);
                    this.search2_r(state2, training, remainingCalls, false, level + 1);
                    ((ChanceState) (state.ns[action])).correctV(state2, v, realV);
                } else {
                    state.ns[action] = new ChanceState(state2, state, action);
                    this.search2_r(state2, training, remainingCalls, false, level + 1);
                    ((ChanceState) (state.ns[action])).correctV(state2, v, realV);
                }
            } else {
                var s = state.transpositions.get(state2);
                if (s == null) {
                    if (state2.actionCtx == GameActionCtx.BEGIN_TURN && state2.isTerminal() == 0) {
                        var parentState = state2;
                        state2 = parentState.clone(false);
                        state2.doAction(0);
                        var cState = new ChanceState(state2, parentState, 0);
                        state.ns[action] = cState;
                        state.transpositions.put(parentState, cState);
                        if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.prop.testNewFeature)) {
                            var parents = new ArrayList<Tuple<GameState, Integer>>();
                            state.transpositionsParent.put(parentState, parents);
                            parents.add(new Tuple<>(state, action));
                        }
                        this.search2_r(state2, training, remainingCalls, false, level + 1);
                        cState.correctV(state2, v, realV);
                    } else {
                        state.ns[action] = state2;
                        state.transpositions.put(state2, state2);
                        if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.prop.testNewFeature)) {
                            var parents = new ArrayList<Tuple<GameState, Integer>>();
                            state.transpositionsParent.put(state2, parents);
                            parents.add(new Tuple<>(state, action));
                        }
                        this.search2_r(state2, training, remainingCalls, false, level);
                    }
                } else if (s instanceof GameState ns) {
                    if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.prop.testNewFeature)) {
                        state.transpositionsParent.get(state2).add(new Tuple<>(state, action));
                    }
                    if (Configuration.isTranspositionAlwaysExpandNewNodeOn(state)) {
                        this.search2_r(ns, training, remainingCalls, false, level);
                    }
                    state.ns[action] = ns;
                    for (int i = 0; i < state.prop.v_total_len; i++) {
                        v[i] = ns.q[i] / (ns.total_n + 1);
                    }
                } else if (s instanceof ChanceState ns) {
                    if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.prop.testNewFeature)) {
                        state.transpositionsParent.get(state2).add(new Tuple<>(state, action));
                    }
                    if (Configuration.isTranspositionAlwaysExpandNewNodeOn(state)) {
                        state2 = ns.getNextState(true, level);
                        this.search2_r(state2, training, remainingCalls, false, level + 1);
                        ns.correctV(state2, v, realV);
                    }
                    state.ns[action] = ns;
                    for (int i = 0; i < state.prop.v_total_len; i++) {
                        v[i] = ns.total_q[i] / ns.total_n;
                    }
                }
            }
        } else {
            if (nextState instanceof ChanceState cState) {
                if (state.n[action] < cState.total_n) {
                    if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.prop.testNewFeature)) {
                        state2 = cState.getNextState(true, level);
                        this.search2_r(state2, training, remainingCalls, false, level + 1);
                        cState.correctV(state2, v, realV);
//                        if (state.getAction(action).type() == GameActionType.END_TURN) {
                        if (true) {
                            var newS = state.clone(true);
                            newS.doAction(action);
                            updateTranspositions(newS, state2, state, action);
                        } else {
                            Integer.parseInt(null);
                        }
                    }
                    if (Configuration.isTranspositionAlwaysExpandNewNodeOn(state)) {
                        state2 = cState.getNextState(true, level);
                        this.search2_r(state2, training, remainingCalls, false, level + 1);
                        cState.correctV(state2, v, realV);
                    }
                    for (int i = 0; i < state.prop.v_total_len; i++) {
                        v[i] = cState.total_q[i] / cState.total_n * (state.n[action] + 1) - state.q[(action + 1) * state.prop.v_total_len + i];
                    }
                } else {
                    state2 = cState.getNextState(true, level);
                    this.search2_r(state2, training, remainingCalls, false, level + 1);
                    cState.correctV(state2, v, realV);
                    if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.prop.testNewFeature)) {
                        var parents = state2.transpositionsParent.get(state2);
                        if (parents != null && parents.size() > 1) {
                            updateTranspositions(state2, state2, state, action);
                        }
                    }
                }
            } else if (nextState instanceof GameState nState) {
                if (state.n[action] < nState.total_n + 1) {
                    if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.prop.testNewFeature)) {
                        this.search2_r(nState, training, remainingCalls, false, level);
                        updateTranspositions(nState, nState, state, action);
                    }
                    if (Configuration.isTranspositionAlwaysExpandNewNodeOn(state)) {
                        this.search2_r(nState, training, remainingCalls, false, level);
                    }
                    for (int i = 0; i < state.prop.v_total_len; i++) {
                        v[i] = nState.q[i] / (nState.total_n + 1) * (state.n[action] + 1) - state.q[(action + 1) * state.prop.v_total_len + i];
                    }
                } else {
                    this.search2_r(nState, training, remainingCalls, false, level);
                }
            }
        }

        for (int i = 0; i < state.prop.v_total_len; i++) {
            state.q[(action + 1) * state.prop.v_total_len + i] += v[i];
        }
        state.n[action] += 1;
        state.total_n += 1;
        var newVarianceM = state.varianceM + (realV[GameState.V_COMB_IDX] - state.varianceM) / (state.total_n + 1);
        var newVarianceS = state.varianceS + (realV[GameState.V_COMB_IDX] - state.varianceM) * (realV[GameState.V_COMB_IDX] - newVarianceM);
        state.varianceM = newVarianceM;
        state.varianceS = newVarianceS;
        if (terminal_v_win > 0.5) {
            if (state.ns[action] instanceof ChanceState) {
                terminal_v_win = -100;
            } else {
                state.terminal_action = action;
                for (int i = 0; i < state.prop.v_total_len; i++) {
                    double q_total = state.q[(action + 1) * state.prop.v_total_len + i] / state.n[action] * (state.total_n + 1);
                    v[i] = q_total - state.q[i];
                }
            }
        }
        for (int i = 0; i < state.prop.v_total_len; i++) {
            state.q[i] += v[i];
        }
        numberOfPossibleActions = numberOfActions;
    }

    public boolean cannotImproveState(GameState state) {
        if (state.playerTurnStartMaxPossibleHealth != state.getPlayeForRead().getHealth()) {
            return false;
        }
        if (state.prop.isHeartFight) {
            return true;
        }
        if (state.playerTurnStartPotionCount != state.getPotionCount()) {
            return false;
        }
        if (state.prop.nunchakuCounterIdx >= 0 && state.getCounterForRead()[state.prop.nunchakuCounterIdx] < 9) {
            return false;
        }
        if (state.prop.penNibCounterIdx >= 0 && state.getCounterForRead()[state.prop.penNibCounterIdx] < 9) {
            return false;
        }
        if (state.prop.happyFlowerCounterIdx >= 0 && state.getCounterForRead()[state.prop.happyFlowerCounterIdx] < 2) {
            return false;
        }
        if (state.prop.geneticAlgorithmCounterIdx >= 0 && CardDefect.GeneticAlgorithm.getMaxPossibleGARemaining(state) != 0) {
            return false;
        }
        if (state.prop.handOfGreedCounterIdx >= 0) {
            if (CardColorless.HandOfGreed.getMaxPossibleHandOfGreenRemaining(state, false) > 0) {
                return false;
            }
        }
        if (state.prop.incenseBurnerCounterIdx >= 0) {
            if (state.prop.incenseBurnerRewardType == Relic.IncenseBurner.DEFAULT_REWARD) {
                if (state.getCounterForRead()[state.prop.incenseBurnerCounterIdx] < 5) {
                    return false;
                }
            } else if (state.prop.incenseBurnerRewardType == Relic.IncenseBurner.SHIELD_AND_SPEAR_REWARD ||
                    state.prop.incenseBurnerRewardType == Relic.IncenseBurner.HEART_REWARD) {
                if (state.getCounterForRead()[state.prop.incenseBurnerCounterIdx] != 4) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateTranspositions(GameState transpositionKey, GameState state, GameState curParentState, int action) {
        var parents = transpositionKey.transpositionsParent.get(transpositionKey);
        if (parents == null) {
            return;
        }
        for (var parent : parents) {
            if (parent.v1().equals(curParentState) && parent.v2() == action) {
                continue;
            }
            if (parent.v1().ns[parent.v2()] instanceof GameState) {
                for (int i = 0; i < state.prop.v_total_len; i++) {
                    var vChange = state.q[i] / (state.total_n + 1) * parent.v1().n[parent.v2()] - parent.v1().q[(parent.v2() + 1) * state.prop.v_total_len + i];
                    parent.v1().q[(parent.v2() + 1) * state.prop.v_total_len + i] += vChange;
                    parent.v1().q[i] += vChange;
                }
                updateTranspositions(parent.v1(), parent.v1(), null, -1);
            } else if (parent.v1().ns[parent.v2()] instanceof ChanceState cs) {
                if (cs.cache.get(state) == null) {
                    // in some states -> end turn lead to same states even though they have differences
                    // so the begin turn chance state may be different and have different states
                    continue;
                }
                var v = new double[20];
                cs.correctV(state, v, realV);
                for (int i = 0; i < state.prop.v_total_len; i++) {
                    var vChange = cs.total_q[i] / cs.total_n * parent.v1().n[parent.v2()] - parent.v1().q[(parent.v2() + 1) * state.prop.v_total_len + i];
                    parent.v1().q[(parent.v2() + 1) * state.prop.v_total_len + i] += vChange;
                    parent.v1().q[i] += vChange;
                }
                updateTranspositions(parent.v1(), parent.v1(), null, -1);
            }
        }
    }

    void search3(GameState state, boolean training, int remainingCalls) {
        terminal_v_win = -100;
        search3_r(state, training, remainingCalls, true);
    }

    void search3_r(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        if (state.terminal_action >= 0) {
            for (int i = 0; i < state.prop.v_total_len; i++) {
                v[i] = state.q[(state.terminal_action + 1) * state.prop.v_total_len + i] / state.n[state.terminal_action];
                realV[i] = v[i];
            }
            numberOfPossibleActions = 1;
            return;
        }
        if (state.isTerminal() != 0) {
            state.get_v(v);
            System.arraycopy(v, 0, realV, 0, state.prop.v_total_len);
            state.initSearchInfo2();
            for (int i = 0; i < state.prop.v_total_len; i++) {
                state.q[i] = v[i];
                if (state.q[i] == Double.POSITIVE_INFINITY) {
                    Integer.parseInt(null);
                }
            }
            if (v[GameState.V_WIN_IDX] > 0.5 && cannotImproveState(state)) {
                terminal_v_win = v[GameState.V_WIN_IDX];
            }
            return;
        }
        if (state.policy == null) {
            state.doEval(model);
            state.get_v(v);
            System.arraycopy(v, 0, realV, 0, state.prop.v_total_len);
            state.initSearchInfo2();
            for (int i = 0; i < state.prop.v_total_len; i++) {
                state.q[i] = v[i];
                if (state.q[i] == Double.POSITIVE_INFINITY) {
                    Integer.parseInt(null);
                }
            }
            numberOfPossibleActions = state.getLegalActions().length;
            state.varianceM = v[GameState.V_COMB_IDX];
            state.varianceS = 0;
            return;
        }
        if (state.n == null) {
            state.initSearchInfo();
        }

        float[] policy = getPolicy(state, training, remainingCalls, isRoot);
        selectAction(state, policy, training, isRoot);
        int action = ret[0];
        int numberOfActions = ret[1];

        State nextState = state.ns[action];
        GameState state2;
        if (isRoot && exploredActions != null && exploredActions.containsKey(action)) {
            exploredV = exploredActions.get(action);
        }
        if (nextState == null) {
            state2 = state.clone(true);
            state2.doAction(action);
            if (state2.isStochastic) {
                if (Configuration.TRANSPOSITION_ACROSS_CHANCE_NODE && (!Configuration.TEST_TRANSPOSITION_ACROSS_CHANCE_NODE || state.prop.testNewFeature)) {
                    var s = state.transpositions.get(state2);
                    if (s == null) {
                        state.ns[action] = new ChanceState(state2, state, action);
                        state.transpositions.put(state2, state2);
                        this.search3_r(state2, training, remainingCalls, false);
                        ((ChanceState) (state.ns[action])).correctV(state2, v, realV);
                    } else {
                        state2 = (GameState) s;
                        state.ns[action] = new ChanceState(state2, state, action);
                        this.search3_r(state2, training, remainingCalls, false);
                        ((ChanceState) (state.ns[action])).correctV(state2, v, realV);
                    }
                } else {
                    state.ns[action] = new ChanceState(state2, state, action);
                    this.search3_r(state2, training, remainingCalls, false);
                    ((ChanceState) (state.ns[action])).correctV(state2, v, realV);
                }
            } else {
                var s = state.transpositions.get(state2);
                if (s == null) {
                    if (state2.actionCtx == GameActionCtx.BEGIN_TURN && state2.isTerminal() == 0) {
                        var parentState = state2;
                        state2 = parentState.clone(false);
                        state2.doAction(0);
                        var cState = new ChanceState(state2, parentState, 0);
                        state.ns[action] = cState;
                        state.transpositions.put(parentState, cState);
                        this.search3_r(state2, training, remainingCalls, false);
                        cState.correctV(state2, v, realV);
                    } else {
                        state.ns[action] = state2;
                        state.transpositions.put(state2, state2);
                        this.search3_r(state2, training, remainingCalls, false);
                    }
                } else if (s instanceof GameState ns) {
                    state.ns[action] = ns;
                    for (int i = 0; i < state.prop.v_total_len; i++) {
                        v[i] = ns.q[i] / (ns.total_n + 1);
                    }
                } else if (s instanceof ChanceState ns) {
                    state.ns[action] = ns;
                    for (int i = 0; i < state.prop.v_total_len; i++) {
                        v[i] = ns.total_q[i] / ns.total_n;
                    }
                }
            }
        } else {
            if (nextState instanceof ChanceState cState) {
                if (state.n[action] < cState.total_n) {
//                    state2 = cState.getNextState(true);
//                    this.search3_r(state2, training, remainingCalls, false);
//                    cState.correctV(state2, v, realV);
                    for (int i = 0; i < state.prop.v_total_len; i++) {
                        v[i] = cState.total_q[i] / cState.total_n * (state.n[action] + 1) - state.q[(action + 1) * state.prop.v_total_len + i];
                    }
                } else {
                    state2 = cState.getNextState(true, -1);
                    this.search3_r(state2, training, remainingCalls, false);
                    cState.correctV(state2, v, realV);
                }
            } else if (nextState instanceof GameState nState) {
                if (state.n[action] < nState.total_n + 1) {
//                    this.search3_r(nState, training, remainingCalls, false);
                    for (int i = 0; i < state.prop.v_total_len; i++) {
                        v[i] = nState.q[i] / (nState.total_n + 1) * (state.n[action] + 1) - state.q[(action + 1) * state.prop.v_total_len + i];
                    }
                } else {
                    this.search3_r(nState, training, remainingCalls, false);
                }
            }
        }

        for (int i = 0; i < state.prop.v_total_len; i++) {
            state.q[(action + 1) * state.prop.v_total_len + i] += v[i];
        }
        state.n[action] += 1;
        state.total_n += 1;
        int actionToPropagate;
        if (terminal_v_win > 0.5) {
            if (state.ns[action] instanceof ChanceState) {
                terminal_v_win = -100;
                actionToPropagate = getActionWithMaxNodesOrTerminal(state, null);
            } else {
                state.terminal_action = action;
                actionToPropagate = action;
            }
        } else {
            actionToPropagate = getActionWithMaxNodesOrTerminal(state, null);
        }
        for (int i = 0; i < state.prop.v_total_len; i++) {
            double qTotal = state.q[(actionToPropagate + 1) * state.prop.v_total_len + i] / state.n[actionToPropagate] * (state.total_n + 1);
            if (state.ns[actionToPropagate] instanceof ChanceState) {
                qTotal = state.q[(actionToPropagate + 1) * state.prop.v_total_len + i] / state.n[actionToPropagate] * state.total_n + state.get_q();
            }
            v[i] = qTotal - state.q[i];
            state.q[i] += v[i];
        }
        numberOfPossibleActions = numberOfActions;
    }

    void searchPlain(GameState state, boolean training, int remainingCalls) {
        searchPlain_r(state, training, remainingCalls, true);
    }

    void searchPlain_r(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        if (state.isTerminal() != 0) {
            state.get_v(v);
            for (int i = 0; i < state.prop.v_total_len; i++) {
                state.q[i] = v[i];
            }
            numberOfPossibleActions = 1;
            return;
        }
        if (state.policy == null) {
            state.doEval(model);
            state.get_v(v);
            for (int i = 0; i < state.prop.v_total_len; i++) {
                state.q[i] = v[i];
            }
            numberOfPossibleActions = state.getLegalActions().length;
            return;
        }
        if (state.n == null) {
            state.initSearchInfo();
        }

        float[] policy = getPolicy(state, training, remainingCalls, isRoot);
        selectAction(state, policy, training, isRoot);
        int action = ret[0];
        int numberOfActions = ret[1];

        State nextState = state.ns[action];
        GameState state2;
        if (nextState == null) {
            state2 = state.clone(true);
            state2.doAction(action);
            if (state2.actionCtx == GameActionCtx.BEGIN_TURN) {
                state2.doAction(0);
            }
            if (state2.isStochastic) {
                state.ns[action] = new ChanceState(state2, state, action);
                this.searchPlain_r(state2, training, remainingCalls, false);
                ((ChanceState) (state.ns[action])).correctV(state2, v, realV);
            } else {
                state.ns[action] = state2;
                this.searchPlain_r(state2, training, remainingCalls, false);
            }
        } else {
            if (nextState instanceof ChanceState cState) {
                state2 = cState.getNextState(true,-1);
                this.searchPlain_r(state2, training, remainingCalls, false);
                cState.correctV(state2, v, realV);
            } else if (nextState instanceof GameState nState) {
                this.searchPlain_r(nState, training, remainingCalls, false);
            }
        }


        for (int i = 0; i < state.prop.v_total_len; i++) {
            state.q[(action + 1) * state.prop.v_total_len + i] += v[i];
        }
        state.n[action] += 1;
        state.total_n += 1;
        for (int i = 0; i < state.prop.v_total_len; i++) {
            state.q[i] += v[i];
        }
        numberOfPossibleActions = numberOfActions;
    }

    void searchLine(GameState state, boolean training, boolean isRoot, int remainingCalls) {
        if (state.terminal_action >= 0) {
            for (int i = 0; i < state.prop.v_total_len; i++) {
                v[i] = state.q[(state.terminal_action + 1) * state.prop.v_total_len + i] / state.n[state.terminal_action];;
            }
            return;
        }
        if (state.searchFrontier == null) {
            state.searchFrontier = new SearchFrontier();
            state.searchFrontier.addLine(new LineOfPlay(state, 1, null, 0));
        }
        terminal_v_win = -100;

        int max_n = 0;
        var lines = state.searchFrontier.lines.values();
        if (!training) {
            for (var line : lines) {
                if (line.numberOfActions == 0) {
                    continue;
                }
                if (line.n > max_n) {
                    max_n = line.n;
                }
            }
        }

        LineOfPlay maxLine = null;
        double maxU = 0.0;
        if (isRoot) numberOfPossibleActions = 0;
        double p_uniform = 1.0 / state.searchFrontier.lines.size();
        double ratio = Math.min(((double) state.total_n) * state.total_n / 1000000.0, 1);
        int frontierNodes = 0;
        for (var line : lines) {
            if (line.numberOfActions == 0) {
                continue;
            }
            if (line.internal) {
                continue;
            }
            frontierNodes++;
        }
        boolean skipInternalNodes = false;
        if (frontierNodes >= Math.ceil(Math.pow(state.total_n + 1, 0.5))) {
            skipInternalNodes = true;
        }
        for (var line : lines) {
            if (line.numberOfActions == 0) {
                continue;
            }
            if (remainingCalls > 0 && max_n - line.n > remainingCalls) {
                continue;
            }
            if (skipInternalNodes && line.internal) {
                continue;
            }
            if (isRoot) numberOfPossibleActions += 1; // todo: why this?
            if (isRoot && remainingCalls > 0 && max_n < remainingCalls) {
                numberOfPossibleActions += line.numberOfActions;
            }
            var parentLine = line.parentLines == null ? null : line.parentLines.get(0).line();
            double q = line.n > 0 ? line.q_comb / line.n : parentLine != null && parentLine.n > 0 ? parentLine.q_comb / parentLine.n : 0;
//            double u = state.searchFrontier.total_n > 0 ? q + (0.125 + Math.log((state.searchFrontier.total_n + 10000f + 1) / 10000) / 10) * line.p_cur * sqrt(state.searchFrontier.total_n) / (1 + line.n) : line.p_cur;
            double p_prime = line.p_cur * (1 - ratio) + p_uniform * ratio;
            double cpuct = state.prop.cpuct;
            if (Configuration.CPUCT_SCALING && (!Configuration.TEST_CPUCT_SCALING || state.prop.testNewFeature)) {
                cpuct = cpuct + 0.1 * Math.log((state.total_n + 1 + 5000) / 5000.0);
            }
            double u = state.searchFrontier.total_n > 0 ? q + cpuct * p_prime * sqrt(state.searchFrontier.total_n) / (1 + line.n) : line.p_cur;
//            double u = state.searchFrontier.total_n > 0 ? q + 0.125 * p_prime * sqrt(state.searchFrontier.total_n) / (1 + line.n) : line.p_cur;
//            if (training && isRoot) {
//                var force_n = (int) Math.sqrt(0.5 * line.p_cur * state.searchFrontier.total_n);
//                if (line.n < force_n) {
//                    maxU = Integer.MAX_VALUE;
//                    maxLine = line;
//                    continue;
//                }
//            }
            if (u > maxU) {
                maxU = u;
                maxLine = line;
            }
        }
        searchLine_r(state, maxLine, training, isRoot);
        searchLinePropagate(state, maxLine);
        terminal_v_win = -100;
    }

    private void searchLinePropagate(GameState parentState, LineOfPlay line) {
        while (line.parentLines != null) {
            LineOfPlay.Edge edge;
            if (line.parentLines.size() == 1) {
                edge = line.parentLines.get(0);
            } else {
                edge = line.parentLines.get(parentState.prop.random.nextInt(line.parentLines.size(), RandomGenCtx.Other));
            }
            GameState state = (GameState) edge.line().state;
            state.n[edge.action()] += 1;
            for (int i = 0; i < state.prop.v_total_len; i++) {
                state.q[(edge.action() + 1) * state.prop.v_total_len + i] += v[i];
            }
            state.total_n += 1;
            var newVarianceM = state.varianceM + (v[GameState.V_COMB_IDX] - state.varianceM) / (state.total_n + 1);
            var newVarianceS = state.varianceS + (v[GameState.V_COMB_IDX] - state.varianceM) * (v[GameState.V_COMB_IDX] - newVarianceM);
            state.varianceM = newVarianceM;
            state.varianceS = newVarianceS;
            if (line.state instanceof GameState childState && childState.terminal_action >= 0) {
                state.terminal_action = edge.action();
                for (int i = 0; i < state.prop.v_total_len; i++) {
                    double qTotal = childState.q[i] / (childState.total_n + 1) * (state.total_n + 1);
                    v[i] = qTotal - state.q[i];
                }
            }
            for (int i = 0; i < state.prop.v_total_len; i++) {
                state.q[i] += v[i];
            }
            line = edge.line();
        }
    }

    private void searchLine_r(GameState parentState, LineOfPlay curLine, boolean training, boolean isRoot) {
        if (curLine.state instanceof ChanceState cState) {
            var nextState = cState.getNextState(true, -1);
            searchLine(nextState, training, false, -1);
            cState.correctV(nextState, v, realV);
            curLine.n += 1;
            curLine.q_comb += v[GameState.V_COMB_IDX];
            curLine.q_win += v[GameState.V_WIN_IDX];
            curLine.q_health += v[GameState.V_HEALTH_IDX];
            parentState.searchFrontier.total_n += 1;
            return;
        }
        GameState state = (GameState) curLine.state;
        if (state.isTerminal() != 0) {
            state.get_v(v);
            state.initSearchInfo2();
            for (int i = 0; i < state.prop.v_total_len; i++) {
                state.q[i] += v[i];
            }
            curLine.n += 1;
            curLine.q_comb += v[GameState.V_COMB_IDX];
            curLine.q_win += v[GameState.V_WIN_IDX];
            curLine.q_health += v[GameState.V_HEALTH_IDX];
            parentState.searchFrontier.total_n += 1;
            if (v[GameState.V_WIN_IDX] > 0.5 && cannotImproveState(state)) {
                state.terminal_action = -1234;
                terminal_v_win = v[GameState.V_WIN_IDX];
            }
            return;
        }
        if (state.policy == null) {
            state.doEval(model);
            state.get_v(v);
            state.initSearchInfo2();
            for (int i = 0; i < state.prop.v_total_len; i++) {
                state.q[i] += v[i];
            }
            if (training) {
                state.policyMod = applyDirichletNoiseToPolicy(state.policy, 0.5f);
            } else {
                state.policyMod = state.policy;
            }
            curLine.n = 1;
            curLine.q_comb = v[GameState.V_COMB_IDX];
            curLine.q_win = v[GameState.V_WIN_IDX];
            curLine.q_health = v[GameState.V_HEALTH_IDX];
            parentState.searchFrontier.total_n += 1;
            state.varianceM = v[GameState.V_COMB_IDX];
            state.varianceS = 0;
            return;
        }
        if (state.n == null) {
            state.initSearchInfo();
        }

        float[] policy = state.policyMod;
        int action = 0;
        int numberOfActions = 0;
        double maxU = -1000000;
        for (int i = 0; i < state.getLegalActions().length; i++) {
            if (state.n[i] > 0) {
                continue;
            }
            numberOfActions += 1;
            double u = policy[i];
            if (u > maxU) {
                action = i;
                maxU = u;
            }
        }
        curLine.internal = true;
        if (numberOfActions > 0) {
            curLine.numberOfActions -= 1;
        }
        if (numberOfActions == 1) {
            curLine.q_comb = 0;
        } else if (numberOfActions == 0) {
            for (int i = 0; i < state.getLegalActions().length; i++) {
                var line = parentState.searchFrontier.getLine(state.ns[i]);
                if (line == null) {
                    continue;
                }
                double u = line.q_comb / line.n;
                if (u > maxU) {
                    action = i;
                    maxU = u;
                }
            }
        }

        var nextState = state.clone(true);
        nextState.doAction(action);
        if (nextState.isStochastic || nextState.actionCtx == GameActionCtx.BEGIN_TURN) {
            var cState = nextState.isStochastic ? new ChanceState(null, state, action) : null;
            if (!nextState.isStochastic) {
                cState = new ChanceState(null, nextState, 0);
                nextState = nextState.clone(false);
                nextState.doAction(0);
            }
            var transposedLine = parentState.searchFrontier.getLine(cState);
            if (transposedLine == null) {
                cState.addToQueue(nextState);
                var newLine = new LineOfPlay(cState, curLine.p_total * policy[action], curLine, action);
                curLine.p_cur -= curLine.p_total * policy[action];
                parentState.searchFrontier.addLine(newLine);
                searchLine_r(parentState, newLine, training, false);
                if (state.n[action] == 0) {
                    state.ns[action] = newLine.state;
                }
            } else {
                if (state.n[action] == 0) {
                    searchLineTransposePropagatePolicy(parentState, transposedLine, curLine.p_total * policy[action]);
                    curLine.p_cur -= curLine.p_total * policy[action];
                    state.ns[action] = transposedLine.state;
                    transposedLine.parentLines.add(new LineOfPlay.Edge(curLine, action));
                }
                searchLine_r(parentState, transposedLine, training, false);
            }
        } else {
            var transposedLine = parentState.searchFrontier.getLine(nextState);
            if (transposedLine == null) {
                var newLine = new LineOfPlay(nextState, curLine.p_total * policy[action], curLine, action);
                curLine.p_cur -= curLine.p_total * policy[action];
                parentState.searchFrontier.addLine(newLine);
                searchLine_r(parentState, newLine, training, false);
                if (state.n[action] == 0) {
                    state.ns[action] = newLine.state;
                }
            } else {
                if (state.n[action] == 0) {
                    searchLineTransposePropagatePolicy(parentState, transposedLine, curLine.p_total * policy[action]);
                    curLine.p_cur -= curLine.p_total * policy[action];
                    state.ns[action] = transposedLine.state;
                    transposedLine.parentLines.add(new LineOfPlay.Edge(curLine, action));
                }
                searchLine_r(parentState, transposedLine, training, false);
            }
        }
        state.n[action] += 1;
        for (int i = 0; i < state.prop.v_total_len; i++) {
            state.q[(action + 1) * state.prop.v_total_len + i] += v[i];
        }
        state.total_n += 1;
        if (terminal_v_win > 0.5) {
            state.terminal_action = action;
            for (int i = 0; i < state.prop.v_total_len; i++) {
                double qTotal = state.q[(action + 1) * state.prop.v_total_len + i] / state.n[action] * (state.total_n + 1);
                v[i] = qTotal - state.q[i];
            }
        }
        for (int i = 0; i < state.prop.v_total_len; i++) {
            state.q[i] += v[i];
        }
    }

    private void searchLineTransposePropagatePolicy(GameState parentState, LineOfPlay line, double p) {
        if (line.state instanceof GameState state) {
            if (state.isTerminal() == 0) {
                if (state.n == null) {
                    state.initSearchInfo();
                }
                for (int i = 0; i < state.n.length; i++) {
                    if (state.n[i] > 0) {
                        var nextLine = parentState.searchFrontier.getLine(state.ns[i]);
                        searchLineTransposePropagatePolicy(parentState, nextLine, p * state.policyMod[i]);
                    }
                }
            }
        }
        line.p_cur += p * (line.p_cur / line.p_total);
        line.p_total += p;
    }

    private float[] getPolicy(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        float[] policy;
        if (training) {
            if (isRoot) {
                if (state.policyMod == null) {
                    state.policyMod = Model.softmax(state.policy, 1.25f);
                    state.policyMod = applyDirichletNoiseToPolicy(state.policy, 0.25f);
                }
                policy = state.policyMod;
            } else {
               // if (state.policyMod == null) {
               //     state.policyMod = applyDirichletNoiseToPolicy(state.policy, 0.25f);
               // }
               // policy = state.policyMod;
               policy = state.policy;
            }
        } else {
            policy = applyFutileSearchPruning(state, state.policy, remainingCalls);
        }
        return policy;
    }

    private void selectAction(GameState state, float[] policy, boolean training, boolean isRoot) {
        if (isRoot && forceRootAction >= 0) {
            ret[0] = forceRootAction;
            ret[1] = 1;
            return;
        }
        int action = 0;
        double maxU = -1000000;
        boolean hasForcedMove = false;
        int numberOfActions = 0;
        double p = 1.0 / state.getLegalActions().length;
        double ratio = Math.min(((double) state.total_n) * state.total_n / 100000000.0, 1);
        boolean allBelow1Per = Configuration.USE_FIGHT_PROGRESS_WHEN_LOSING;
        if (Configuration.USE_FIGHT_PROGRESS_WHEN_LOSING) {
            for (int i = 0; i < state.getLegalActions().length; i++) {
                if (policy[i] <= 0) {
                    continue;
                }
                double q_win = state.n[i] > 0 ? state.q[(i + 1) * state.prop.v_total_len + GameState.V_WIN_IDX] / state.n[i] : 1;
                if (q_win >= 0.01) {
                    allBelow1Per = false;
                    break;
                }
            }
        }
        double maxQ = -1000000;
        for (int i = 0; i < state.getLegalActions().length; i++) {
            if (policy[i] <= 0) {
                continue;
            }
            numberOfActions += 1;
            double p_prime = policy[i] * (1 - ratio) + p * ratio;
            double q = state.n[i] > 0 ? state.q[(i + 1) * state.prop.v_total_len + GameState.V_COMB_IDX] / state.n[i] : Math.max(state.q[GameState.V_COMB_IDX] / (state.total_n + 1), 0);
            if (allBelow1Per) {
                q = state.n[i] > 0 ? state.q[(i + 1) * state.prop.v_total_len + state.prop.fightProgressVIdx] / state.n[i] : Math.max(state.q[state.prop.fightProgressVIdx] / (state.total_n + 1), 0);
            }
            double cpuct = state.prop.cpuct;
            if (Configuration.CPUCT_SCALING && (!Configuration.TEST_CPUCT_SCALING || state.prop.testNewFeature)) {
                cpuct = cpuct + 0.1 * Math.log((state.total_n + 1 + 5000) / 5000.0);
            }
            var std_err = 0.0;
            if (Configuration.isUseUtilityStdErrForPuctOn(state)) {
                std_err = Math.sqrt(state.varianceS / state.total_n) / Math.sqrt(state.total_n + 1);
            }
            double u = state.total_n > 0 ? q + cpuct * policy[i] * (sqrt(state.total_n) / (1 + state.n[i]) + 5 * std_err): policy[i];
            if (training && isRoot) {
                var force_n = (int) Math.sqrt(0.5 * policy[i] * state.total_n);
                if (state.n[i] < force_n) {
                    if (!hasForcedMove) {
                        action = i;
                        maxU = u;
                        hasForcedMove = true;
                    } else if (u > maxU) {
                        action = i;
                        maxU = u;
                    }
                    continue;
                }
            }
           if (!hasForcedMove && u > maxU) {
                action = i;
                maxU = u;
            }
        }
        ret[0] = action;
        ret[1] = numberOfActions;
    }

    private float[] applyFutileSearchPruning(GameState state, float[] policy, int remainingCalls) {
        if (remainingCalls <= 0) {
            return policy;
        }
        int max_n = Utils.max(state.n);
        float[] newPolicy = policy;
        for (int i = 0; i < policy.length; i++) {
            if (policy[i] > 0 && max_n - state.n[i] > remainingCalls) {
                if (newPolicy == policy) {
                    newPolicy = Arrays.copyOf(policy, policy.length);
                }
                newPolicy[i] = 0;
            }
        }
        return newPolicy;
    }

    private float[] applyDirichletNoiseToPolicy(float[] policy, float proportion) {
        policy = Arrays.copyOf(policy, policy.length);
        var param = new double[policy.length];
        Arrays.fill(param, 2);
        var noiseGen = new Dirichlet(param);
        var noise = noiseGen.nextDistribution(); // todo move out
        for (int i = 0; i < policy.length; i++) {
            policy[i] = (float) noise[i] * proportion + policy[i] * (1 - proportion);
        }
        return policy;
    }

    public static int getActionRandomOrTerminal(GameState state) {
        if (state.terminal_action >= 0) {
            return state.terminal_action;
        }
        var total_n = 0;
        for (int i = 0; i < state.policy.length; i++) {
            if (state.n[i] > 0 && (state.ns[i] instanceof ChanceState || ((GameState) state.ns[i]).isTerminal() >= 0)) {
                total_n += state.n[i];
            }
        }
        if (total_n == 0) {
            total_n = state.total_n;
        }
        int r = state.prop.random.nextInt(total_n, RandomGenCtx.Other);
        int acc = 0;
        int action = -1;
        for (int i = 0; i < state.policy.length; i++) {
            if (total_n == state.total_n || (state.n[i] > 0 && (state.ns[i] instanceof ChanceState || ((GameState) state.ns[i]).isTerminal() >= 0))) {
                acc += state.n[i];
                if (acc > r) {
                    action = i;
                    break;
                }
            }
        }
        return action;
    }

    public static int getActionRandomOrTerminalSelectScenario(GameState state) {
        if (state.terminal_action >= 0) {
            return state.terminal_action;
        }
        var total_n = 0;
        for (int i = 0; i < state.policy.length; i++) {
            if (state.n[i] > 0) {
                total_n += Math.pow(state.n[i], Configuration.PRE_BATTLE_SCENARIO_TEMP);
            }
        }
        int r = state.prop.random.nextInt(total_n, RandomGenCtx.Other);
        int acc = 0;
        int action = -1;
        for (int i = 0; i < state.policy.length; i++) {
            if (state.n[i] > 0) {
                acc += Math.pow(state.n[i], Configuration.PRE_BATTLE_SCENARIO_TEMP);
                if (acc > r) {
                    action = i;
                    break;
                }
            }
        }
        return action;
    }

    public static int getActionWithMaxNodesOrTerminal(GameState state, HashSet<GameAction> bannedActions) {
        if (state.terminal_action >= 0) {
            return state.terminal_action;
        }
        if (state.total_n == 0) {
            int actionToPropagate = -1;
            float max_p = -1000.0f;
            for (int i = 0; i < state.getLegalActions().length; i++) {
                if (state.policy[i] > max_p) {
                    max_p = state.policy[i];
                    actionToPropagate = i;
                }
            }
            return actionToPropagate;
        } else {
            int actionToPropagate = -1;
            int max_n = -1000;
            for (int i = 0; i < state.getLegalActions().length; i++) {
                if (bannedActions == null || !bannedActions.contains(state.getAction(i))) {
                    if (state.n[i] > max_n) {
                        max_n = state.n[i];
                        actionToPropagate = i;
                    }
                }
            }
            return actionToPropagate;
        }
    }

    public static int getActionWithMaxLCBOrTerminal(GameState state, HashSet<GameAction> bannedActions) {
        if (state.terminal_action >= 0) {
            return state.terminal_action;
        }
        ClopperPearsonInterval interval = new ClopperPearsonInterval();
        if (state.total_n == 0) {
            int actionToPropagate = -1;
            float max_p = -1000.0f;
            for (int i = 0; i < state.getLegalActions().length; i++) {
                if (state.policy[i] > max_p) {
                    max_p = state.policy[i];
                    actionToPropagate = i;
                }
            }
            return actionToPropagate;
        } else {
            int actionToPropagate = -1;
            double max_n = -1000;
            for (int i = 0; i < state.getLegalActions().length; i++) {
                if (state.n[i] > 0 && (bannedActions == null || !bannedActions.contains(state.getAction(i)))) {
                    var nS = (int) Math.floor(state.q[(i + 1) * state.prop.v_total_len + GameState.V_COMB_IDX]);
                    var k = -10.0;
                    if (state.n[i] / (double) state.total_n >= 0.15 && nS > 0) {
                        k = interval.createInterval(state.n[i], nS, 0.00001).getLowerBound();
                    }
                    if (k > max_n) {
                        max_n = k;
                        actionToPropagate = i;
                    }
                }
            }
            if (max_n == -10.0) {
                return getActionWithMaxNodesOrTerminal(state, bannedActions);
            }
            return actionToPropagate;
        }
    }
}
