package com.alphaStS;

import java.util.Arrays;
import java.util.HashSet;

import cc.mallet.types.Dirichlet;
import com.alphaStS.utils.Utils;

import static java.lang.Math.sqrt;

public class MCTS {
    Model model;
    int numberOfPossibleActions;
    private final double[] v = new double[20];
    private final int[] ret = new int[2];
    private double terminal_v_win;

    void setModel(Model model) {
        this.model = model;
    }

    void search(GameState state, boolean training, int remainingCalls) {
        search2(state, training, remainingCalls);
    }

    void search2(GameState state, boolean training, int remainingCalls) {
        terminal_v_win = -100;
        search2_r(state, training, remainingCalls, true);
    }

    void search2_r(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        if (state.terminal_action >= 0) {
            v[GameState.V_COMB_IDX] = state.q_comb[state.terminal_action] / state.n[state.terminal_action];
            v[GameState.V_WIN_IDX] = state.q_win[state.terminal_action] / state.n[state.terminal_action];
            v[GameState.V_HEALTH_IDX] = state.q_health[state.terminal_action] / state.n[state.terminal_action];
            numberOfPossibleActions = 1;
            return;
        }
        if (state.isTerminal() != 0) {
            state.get_v(v);
            if (!state.isStochastic && v[GameState.V_WIN_IDX] > 0.5) {
                if (state.playerTurnStartHealth == state.getPlayeForRead().getHealth() && !state.prop.playerCanHeal &&
                    state.playerTurnStartPotionCount == state.getPotionCount()) {
                    terminal_v_win = v[GameState.V_WIN_IDX];
                }
            }
            return;
        }
        if (state.policy == null) {
            state.doEval(model);
            state.get_v(v);
            state.total_q_comb = v[GameState.V_COMB_IDX];
            state.total_q_win = v[GameState.V_WIN_IDX];
            state.total_q_health = v[GameState.V_HEALTH_IDX];
            numberOfPossibleActions = state.getLegalActions().length;
            return;
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
            if (state2.isStochastic) {
                state.ns[action] = new ChanceState(state2, state, action);
                this.search2_r(state2, training, remainingCalls, false);
                ((ChanceState) (state.ns[action])).correctV(state2, v);
            } else {
                var s = state.transpositions.get(state2);
                if (s == null) {
                    if (state2.actionCtx == GameActionCtx.BEGIN_TURN) {
                        var parentState = state2;
                        state2 = parentState.clone(false);
                        state2.doAction(0);
                        var cState = new ChanceState(state2, parentState, 0);
                        state.ns[action] = cState;
                        state.transpositions.put(parentState, cState);
                        this.search2_r(state2, training, remainingCalls, false);
                        cState.correctV(state2, v);
                    } else {
                        state.ns[action] = state2;
                        state.transpositions.put(state2, state2);
                        this.search2_r(state2, training, remainingCalls, false);
                    }
                } else if (s instanceof GameState ns) {
                    state.ns[action] = ns;
                    v[GameState.V_COMB_IDX] = ns.total_q_comb / (ns.total_n + 1);
                    v[GameState.V_WIN_IDX] = ns.total_q_win / (ns.total_n + 1);
                    v[GameState.V_HEALTH_IDX] = ns.total_q_health / (ns.total_n + 1);
                } else if (s instanceof ChanceState ns) {
                    state.ns[action] = ns;
                    v[GameState.V_COMB_IDX] = ns.total_q_comb / ns.total_n;
                    v[GameState.V_WIN_IDX] = ns.total_q_win / ns.total_n;
                    v[GameState.V_HEALTH_IDX] = ns.total_q_health / ns.total_n;
                }
            }
        } else {
            if (nextState instanceof ChanceState cState) {
                if (state.n[action] < cState.total_n) {
//                    state2 = cState.getNextState(true);
//                    this.search2_r(state2, training, remainingCalls, false);
//                    cState.correctV(state2, v);
                    v[GameState.V_COMB_IDX] = cState.total_q_comb / cState.total_n * (state.n[action] + 1) - state.q_comb[action];
                    v[GameState.V_WIN_IDX] = cState.total_q_win / cState.total_n * (state.n[action] + 1) - state.q_win[action];
                    v[GameState.V_HEALTH_IDX] = cState.total_q_health / cState.total_n * (state.n[action] + 1) - state.q_health[action];
                } else {
                    state2 = cState.getNextState(true);
                    this.search2_r(state2, training, remainingCalls, false);
                    cState.correctV(state2, v);
                }
            } else if (nextState instanceof GameState nState) {
                if (state.n[action] < nState.total_n + 1) {
//                    this.search2_r(nState, training, remainingCalls, false);
                    v[GameState.V_COMB_IDX] = nState.total_q_comb / (nState.total_n + 1) * (state.n[action] + 1) - state.q_comb[action];
                    v[GameState.V_WIN_IDX] = nState.total_q_win / (nState.total_n + 1) * (state.n[action] + 1) - state.q_win[action];
                    v[GameState.V_HEALTH_IDX] = nState.total_q_health / (nState.total_n + 1) * (state.n[action] + 1) - state.q_health[action];
                } else {
                    this.search2_r(nState, training, remainingCalls, false);
                }
            }
        }

        state.q_comb[action] += v[GameState.V_COMB_IDX];
        state.q_win[action] += v[GameState.V_WIN_IDX];
        state.q_health[action] += v[GameState.V_HEALTH_IDX];
        state.n[action] += 1;
        state.total_n += 1;
        if (terminal_v_win > 0.5) {
            state.terminal_action = action;
            if (state.isStochastic) {
                terminal_v_win = -100;
            }
            double q_win_total = state.q_win[action] / state.n[action] * (state.total_n + 1);
            double q_health_total = state.q_health[action] / state.n[action] * (state.total_n + 1);
            double q_comb_total = state.q_comb[action] / state.n[action] * (state.total_n + 1);
            v[GameState.V_COMB_IDX] = q_comb_total - state.total_q_comb;
            v[GameState.V_WIN_IDX] = q_win_total - state.total_q_win;
            v[GameState.V_HEALTH_IDX] = q_health_total - state.total_q_health;
        }
        state.total_q_comb += v[GameState.V_COMB_IDX];
        state.total_q_win += v[GameState.V_WIN_IDX];
        state.total_q_health += v[GameState.V_HEALTH_IDX];
        numberOfPossibleActions = numberOfActions;
    }

    void search3(GameState state, boolean training, int remainingCalls) {
        terminal_v_win = -100;
        search3_r(state, training, remainingCalls, true);
    }

    void search3_r(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        if (state.terminal_action >= 0) {
            v[GameState.V_COMB_IDX] = state.q_comb[state.terminal_action] / state.n[state.terminal_action];
            v[GameState.V_WIN_IDX] = state.q_win[state.terminal_action] / state.n[state.terminal_action];
            v[GameState.V_HEALTH_IDX] = state.q_health[state.terminal_action] / state.n[state.terminal_action];
            numberOfPossibleActions = 1;
            return;
        }
        if (state.isTerminal() != 0) {
            state.get_v(v);
            if (v[GameState.V_WIN_IDX] > 0.5) {
                if (state.playerTurnStartHealth == state.getPlayeForRead().getHealth() && !state.prop.playerCanHeal &&
                        state.playerTurnStartPotionCount == state.getPotionCount()) {
                    terminal_v_win = v[GameState.V_WIN_IDX];
                }
            }
            return;
        }
        if (state.policy == null) {
            state.doEval(model);
            state.get_v(v);
            state.total_q_comb = v[GameState.V_COMB_IDX];
            state.total_q_win = v[GameState.V_WIN_IDX];
            state.total_q_health = v[GameState.V_HEALTH_IDX];
            numberOfPossibleActions = state.getLegalActions().length;
            return;
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
            if (state2.isStochastic) {
                state.ns[action] = new ChanceState(state2, state, action);
                this.search3_r(state2, training, remainingCalls, false);
                ((ChanceState) (state.ns[action])).correctV(state2, v);
            } else {
                var s = state.transpositions.get(state2);
                if (s == null) {
                    if (state2.actionCtx == GameActionCtx.BEGIN_TURN) {
                        var parentState = state2;
                        state2 = parentState.clone(false);
                        state2.doAction(0);
                        var cState = new ChanceState(state2, parentState, 0);
                        state.ns[action] = cState;
                        state.transpositions.put(parentState, cState);
                        this.search3_r(state2, training, remainingCalls, false);
                        cState.correctV(state2, v);
                    } else {
                        state.ns[action] = state2;
                        state.transpositions.put(state2, state2);
                        this.search3_r(state2, training, remainingCalls, false);
                    }
                } else if (s instanceof GameState ns) {
                    state.ns[action] = ns;
                    v[GameState.V_COMB_IDX] = ns.total_q_comb / (ns.total_n + 1);
                    v[GameState.V_WIN_IDX] = ns.total_q_win / (ns.total_n + 1);
                    v[GameState.V_HEALTH_IDX] = ns.total_q_health / (ns.total_n + 1);
                } else if (s instanceof ChanceState ns) {
                    state.ns[action] = ns;
                    v[GameState.V_COMB_IDX] = ns.total_q_comb / ns.total_n;
                    v[GameState.V_WIN_IDX] = ns.total_q_win / ns.total_n;
                    v[GameState.V_HEALTH_IDX] = ns.total_q_health / ns.total_n;
                }
            }
        } else {
            if (nextState instanceof ChanceState cState) {
                if (state.n[action] < cState.total_n) {
//                    state2 = cState.getNextState(true);
//                    this.search3_r(state2, training, remainingCalls, false);
//                    cState.correctV(state2, v);
                    v[GameState.V_COMB_IDX] = cState.total_q_comb / cState.total_n * (state.n[action] + 1) - state.q_comb[action];
                    v[GameState.V_WIN_IDX] = cState.total_q_win / cState.total_n * (state.n[action] + 1) - state.q_win[action];
                    v[GameState.V_HEALTH_IDX] = cState.total_q_health / cState.total_n * (state.n[action] + 1) - state.q_health[action];
                } else {
                    state2 = cState.getNextState(true);
                    this.search3_r(state2, training, remainingCalls, false);
                    cState.correctV(state2, v);
                }
            } else if (nextState instanceof GameState nState) {
                if (state.n[action] < nState.total_n + 1) {
//                    this.search3_r(nState, training, remainingCalls, false);
                    v[GameState.V_COMB_IDX] = nState.total_q_comb / (nState.total_n + 1) * (state.n[action] + 1) - state.q_comb[action];
                    v[GameState.V_WIN_IDX] = nState.total_q_win / (nState.total_n + 1) * (state.n[action] + 1) - state.q_win[action];
                    v[GameState.V_HEALTH_IDX] = nState.total_q_health / (nState.total_n + 1) * (state.n[action] + 1) - state.q_health[action];
                } else {
                    this.search3_r(nState, training, remainingCalls, false);
                }
            }
        }

        state.q_comb[action] += v[GameState.V_COMB_IDX];
        state.q_win[action] += v[GameState.V_WIN_IDX];
        state.q_health[action] += v[GameState.V_HEALTH_IDX];
        state.n[action] += 1;
        state.total_n += 1;
        int actionToPropagate;
        if (terminal_v_win > 0.5) {
            state.terminal_action = action;
            if (state.isStochastic) {
                terminal_v_win = -100;
            }
            actionToPropagate = action;
        } else {
            actionToPropagate = getActionWithMaxNodesOrTerminal(state, null);
        }
        double q_comb_total = state.q_comb[actionToPropagate] / state.n[actionToPropagate] * (state.total_n + 1);
        double q_win_total = state.q_win[actionToPropagate] / state.n[actionToPropagate] * (state.total_n + 1);
        double q_health_total = state.q_health[actionToPropagate] / state.n[actionToPropagate] * (state.total_n + 1);
        if (state.ns[actionToPropagate] instanceof ChanceState) {
            q_comb_total = state.q_comb[actionToPropagate] / state.n[actionToPropagate] * state.total_n + state.get_q();
            q_win_total = state.q_win[actionToPropagate] / state.n[actionToPropagate] * state.total_n + state.v_win;
            q_health_total = state.q_health[actionToPropagate] / state.n[actionToPropagate] * state.total_n + state.v_health;
        }
        v[GameState.V_COMB_IDX] = q_comb_total - state.total_q_comb;
        v[GameState.V_WIN_IDX] = q_win_total - state.total_q_win;
        v[GameState.V_HEALTH_IDX] = q_health_total - state.total_q_health;
        state.total_q_comb += v[GameState.V_COMB_IDX];
        state.total_q_win += v[GameState.V_WIN_IDX];
        state.total_q_health += v[GameState.V_HEALTH_IDX];
        numberOfPossibleActions = numberOfActions;
    }

    void searchPlain(GameState state, boolean training, int remainingCalls) {
        searchPlain_r(state, training, remainingCalls, true);
    }

    void searchPlain_r(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        if (state.isTerminal() != 0) {
            state.get_v(v);
            numberOfPossibleActions = 1;
            return;
        }
        if (state.policy == null) {
            state.doEval(model);
            state.get_v(v);
            state.total_q_comb = v[GameState.V_COMB_IDX];
            state.total_q_win = v[GameState.V_WIN_IDX];
            state.total_q_health = v[GameState.V_HEALTH_IDX];
            numberOfPossibleActions = state.getLegalActions().length;
            return;
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
                ((ChanceState) (state.ns[action])).correctV(state2, v);
            } else {
                state.ns[action] = state2;
                this.searchPlain_r(state2, training, remainingCalls, false);
            }
        } else {
            if (nextState instanceof ChanceState cState) {
                state2 = cState.getNextState(true);
                this.searchPlain_r(state2, training, remainingCalls, false);
                cState.correctV(state2, v);
            } else if (nextState instanceof GameState nState) {
                this.searchPlain_r(nState, training, remainingCalls, false);
            }
        }

        state.q_comb[action] += v[GameState.V_COMB_IDX];
        state.q_win[action] += v[GameState.V_WIN_IDX];
        state.q_health[action] += v[GameState.V_HEALTH_IDX];
        state.n[action] += 1;
        state.total_n += 1;
        state.total_q_comb += v[GameState.V_COMB_IDX];
        state.total_q_win += v[GameState.V_WIN_IDX];
        state.total_q_health += v[GameState.V_HEALTH_IDX];
        numberOfPossibleActions = numberOfActions;
    }

    void searchLine(GameState state, boolean training, boolean isRoot, int remainingCalls) {
        if (state.terminal_action >= 0) {
            v[GameState.V_COMB_IDX] = state.q_comb[state.terminal_action] / state.n[state.terminal_action];
            v[GameState.V_WIN_IDX] = state.q_win[state.terminal_action] / state.n[state.terminal_action];
            v[GameState.V_HEALTH_IDX] = state.q_health[state.terminal_action] / state.n[state.terminal_action];
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
        double p = 1.0 / state.searchFrontier.lines.size();
        double ratio = Math.min(((double) state.total_n) * state.total_n / 100000000.0, 1);
        for (var line : lines) {
            if (line.numberOfActions == 0) {
                continue;
            }
            if (remainingCalls > 0 && max_n - line.n > remainingCalls) {
                continue;
            }
            if (isRoot) numberOfPossibleActions += 1; // todo: why this?
            if (isRoot && remainingCalls > 0 && max_n < remainingCalls) {
                numberOfPossibleActions += line.numberOfActions;
            }
            double q = line.n > 0 ? line.q_comb / line.n : 0;
//            double u = state.searchFrontier.total_n > 0 ? q + (0.125 + Math.log((state.searchFrontier.total_n + 10000f + 1) / 10000) / 10) * line.p_cur * sqrt(state.searchFrontier.total_n) / (1 + line.n) : line.p_cur;
            double p_prime = line.p_cur * (1 - ratio) + p * ratio;
            double u = state.searchFrontier.total_n > 0 ? q + 0.125 * line.p_cur * sqrt(state.searchFrontier.total_n) / (1 + line.n) : line.p_cur;
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
        assert maxLine != null;
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
            state.q_comb[edge.action()] += v[GameState.V_COMB_IDX];
            state.q_win[edge.action()] += v[GameState.V_WIN_IDX];
            state.q_health[edge.action()] += v[GameState.V_HEALTH_IDX];
            state.total_n += 1;
            if (line.state instanceof GameState childState && childState.terminal_action >= 0) {
                state.terminal_action = edge.action();
                double q_win_total = childState.total_q_win / (childState.total_n + 1) * (state.total_n + 1);
                double q_health_total = childState.total_q_health / (childState.total_n + 1) * (state.total_n + 1);
                double q_comb_total = childState.total_q_comb / (childState.total_n + 1) * (state.total_n + 1);
                v[GameState.V_COMB_IDX] = q_comb_total - state.total_q_comb;
                v[GameState.V_WIN_IDX] = q_win_total - state.total_q_win;
                v[GameState.V_HEALTH_IDX] = q_health_total - state.total_q_health;
            }
            state.total_q_comb += v[GameState.V_COMB_IDX];
            state.total_q_win += v[GameState.V_WIN_IDX];
            state.total_q_health += v[GameState.V_HEALTH_IDX];
            line = edge.line();
        }
    }

    private void searchLine_r(GameState parentState, LineOfPlay curLine, boolean training, boolean isRoot) {
        if (curLine.state instanceof ChanceState cState) {
            var nextState = cState.getNextState(true);
            searchLine(nextState, training, false, -1);
            cState.correctV(nextState, v);
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
            state.total_q_comb += v[GameState.V_COMB_IDX];
            state.total_q_win += v[GameState.V_WIN_IDX];
            state.total_q_health += v[GameState.V_HEALTH_IDX];
            curLine.n += 1;
            curLine.q_comb += v[GameState.V_COMB_IDX];
            curLine.q_win += v[GameState.V_WIN_IDX];
            curLine.q_health += v[GameState.V_HEALTH_IDX];
            parentState.searchFrontier.total_n += 1;
            if (v[GameState.V_WIN_IDX] > 0.5 && state.playerTurnStartHealth == state.getPlayeForRead().getHealth() && !state.prop.playerCanHeal) {
//                terminal_v_win = v[GameState.V_WIN_IDX];
            }
            return;
        }
        if (state.policy == null && state.actionCtx != GameActionCtx.BEGIN_TURN) {
            state.doEval(model);
            state.get_v(v);
            state.total_q_comb += v[GameState.V_COMB_IDX];
            state.total_q_win += v[GameState.V_WIN_IDX];
            state.total_q_health += v[GameState.V_HEALTH_IDX];
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
            return;
        }
        if (state.policy == null && state.actionCtx == GameActionCtx.BEGIN_TURN) {
            state.policy = new float[] {1};
            state.policyMod = state.policy;
            state.n = new int[] {0};
            state.ns = new State[] {null};
            state.q_win = new double[] {0};
            state.q_health = new double[] {0};
            state.q_comb = new double[] {0};
            curLine.n = 1;
            curLine.q_comb = 0;
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
        if (nextState.isStochastic) {
            var cState = new ChanceState(null, state, action);
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
        state.q_comb[action] += v[GameState.V_COMB_IDX];
        state.q_win[action] += v[GameState.V_WIN_IDX];
        state.q_health[action] += v[GameState.V_HEALTH_IDX];
        state.total_n += 1;
        if (terminal_v_win > 0.5) {
            state.terminal_action = action;
//            System.out.println(state);
//            System.out.println(state.q_win[action]);
//            System.out.println(state.n[action]);
//            System.out.println(state.total_n);
//            System.out.println(state.total_q_win);
//            System.out.println(Arrays.toString(v));
            double q_win_total = state.q_win[action] / state.n[action] * (state.total_n + 1);
            double q_health_total = state.q_health[action] / state.n[action] * (state.total_n + 1);
            double q_comb_total = state.q_comb[action] / state.n[action] * (state.total_n + 1);
            v[GameState.V_COMB_IDX] = q_comb_total - state.total_q_comb;
            v[GameState.V_WIN_IDX] = q_win_total - state.total_q_win;
            v[GameState.V_HEALTH_IDX] = q_health_total - state.total_q_health;
        }
        state.total_q_comb += v[GameState.V_COMB_IDX];
        state.total_q_win += v[GameState.V_WIN_IDX];
        state.total_q_health += v[GameState.V_HEALTH_IDX];
    }

    private void searchLineTransposePropagatePolicy(GameState parentState, LineOfPlay line, double p) {
        if (line.state instanceof GameState state) {
            if (state.isTerminal() == 0) {
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
        int action = 0;
        double maxU = -1000000;
        boolean hasForcedMove = false;
        int numberOfActions = 0;
        double p = 1.0 / state.getLegalActions().length;
        double ratio = Math.min(((double) state.total_n) * state.total_n / 100000000.0, 1);
        for (int i = 0; i < state.getLegalActions().length; i++) {
            if (policy[i] <= 0) {
                continue;
            }
            numberOfActions += 1;
            double p_prime = policy[i] * (1 - ratio) + p * ratio;
            double q = state.n[i] > 0 ? state.q_comb[i] / state.n[i] : Math.max(state.total_q_comb / (state.total_n + 1), 0);
            //            double q = state.n[i] > 0 ? state.q_comb[i] / state.n[i] : 0;
            double u = state.total_n > 0 ? q + 0.1 * policy[i] * sqrt(state.total_n) / (1 + state.n[i]) : policy[i];
//            double u = state.total_n > 0 ? q + 0.1 * p_prime * sqrt(state.total_n) / (1 + state.n[i]) : policy[i];
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
        int max_i = -1;
        for (int i = 0; i < state.n.length; i++) {
            if (state.n[i] == max_n) {
                max_i = i;
                break;
            }
        }
        float[] newPolicy = policy;
        for (int i = 0; i < policy.length; i++) {
//            if (policy[i] > 0 && max_n - state.n[i] <= remainingCalls) {
//                var kq = new double[2];
//                var kn = new int[2];
//                kq[0] = state.q_comb[max_i];
//                kn[0] = state.n[max_i];
//                kq[1] = state.q_comb[i];
//                kn[1] = state.n[i];
//                for (int j = 0; j < remainingCalls; j++) {
//                    double q1 = kn[0] > 0 ? kq[0] / kn[0] : Math.max(state.total_q_comb / ((state.total_n + j) + 1), 0);
//                    double u1 = (state.total_n + j) > 0 ? q1 + 0.1 * state.policy[max_i] * sqrt((state.total_n + j)) / (1 + kn[0]) : state.policy[max_i];
//                    double q2 = kn[1] > 0 ? kq[1] / kn[1] : Math.max(state.total_q_comb / ((state.total_n + j) + 1), 0);
//                    double u2 = (state.total_n + j) > 0 ? q2 + 0.1 * state.policy[i] * sqrt((state.total_n + j)) / (1 + kn[1]) : state.policy[i];
//                    if (q1 + u1 > q2 + u2) {
////                        kq[0] += (kq[0] / kn[0]);
//                        kn[0]++;
//                    } else {
//                        kq[1] += state.calc_q(1, state.getPlayeForRead().getOrigHealth() / (double) state.getPlayeForRead().getMaxHealth());
//                        kn[1]++;
//                        //                        kq[1];
//                    }
//                    if (kn[1] >= kn[0]) {
//                        break;
//                    }
//                }
//                if (kn[0] > kn[1]) {
//                    if (newPolicy == policy) {
//                        newPolicy = Arrays.copyOf(policy, policy.length);
//                    }
//                    newPolicy[i] = 0;
////                    System.out.println("Pruned " + state.getActionString(i) + "(" + state.n[max_i] + "," + state.n[i] + "," + remainingCalls + "): " + state);
//                } else {
////                    System.out.println("Not Pruned " + state.getActionString(i) + "(" + kn[0] + "," + kn[1] + "," + state.n[max_i] +"," + state.n[i] +"," + remainingCalls + "): " + state);
//                }
//            }
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

    public static int getActionRandomOrTerminal(GameState state, HashSet<GameAction> bannedActions) {
        if (state.terminal_action >= 0) {
            return state.terminal_action;
        }
        var total_n = 0;
        for (int i = 0; i < state.policy.length; i++) {
            if (!bannedActions.contains(state.getAction(i))) {
                total_n += state.n[i];
            }
        }
        if (total_n == 0) { // happens when the non banned action has such low prio that it has 0 visit
            total_n = state.total_n;
        }
        int r = state.prop.random.nextInt(total_n, RandomGenCtx.Other);
        int acc = 0;
        int action = -1;
        for (int i = 0; i < state.policy.length; i++) {
            if (total_n == state.total_n || !bannedActions.contains(state.getAction(i))) {
                acc += state.n[i];
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
}
