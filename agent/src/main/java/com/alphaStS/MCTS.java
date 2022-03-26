package com.alphaStS;

import java.util.Arrays;

import cc.mallet.types.Dirichlet;
import com.alphaStS.utils.Utils;

import static java.lang.Math.sqrt;

public class MCTS {
    Model model;
    int numberOfPossibleActions;
    double[] v;
    double terminal_v_win;

    void setModel(Model model) {
        v = new double[3];
        this.model = model;
    }

    void search(GameState state, boolean training, int remainingCalls) {
        search3(state, training, remainingCalls);
    }

    void search1(GameState state, boolean training, int remainingCalls) {
        terminal_v_win = -100;
        search1_r(state, training, remainingCalls, true);
    }

    void search1_r(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        if (state.terminal_action >= 0) {
            v[0] = state.q_win[state.terminal_action] / state.n[state.terminal_action];
            v[1] = state.q_health[state.terminal_action] / state.n[state.terminal_action];
            v[2] = state.q_comb[state.terminal_action] / state.n[state.terminal_action];
            return;
        }
        if (state.isTerminal() != 0) {
            state.get_v(v);
            if (v[0] > 0.5 && state.playerTurnStartHealth == state.player.health && !state.prop.playerCanHeal) {
                terminal_v_win = v[0];
            }
            return;
        }
        if (state.policy == null) {
            state.doEval(model);
            state.get_v(v);
            state.total_q_win = v[0];
            state.total_q_health = v[1];
            state.total_q_comb = v[2];
            numberOfPossibleActions = 0;
            for (int i = 0; i < state.prop.maxNumOfActions; i++) {
                if (state.policy[i] > 0) {
                    numberOfPossibleActions++;
                }
            }
            return;
        }

        float[] policy = getPolicy(state, training, remainingCalls, isRoot);

        int numberOfActions;
        int action;
        do {
            numberOfActions = 0;
            action = -1;
            double maxU = -1000000;
            v[0] = -1000000;

            for (int i = 0; i < state.prop.maxNumOfActions; i++) {
                if (policy[i] <= 0 || state.transpositionsPolicyMask[i]) {
                    continue;
                }
                numberOfActions += 1;
//                double q = state.n[i] > 0 ? GameState.calc_q(state.q_win[i] / state.n[i], state.q_health[i] / state.n[i]) : GameState.calc_q(state.total_q_win / (state.total_n + 1), state.total_q_health / (state.total_n + 1));
                double q = state.n[i] > 0 ? state.q_comb[i] / state.n[i] : 0;
                double u = state.total_n > 0 ? q + 1 * policy[i] * sqrt(state.total_n) / (1 + state.n[i]) : policy[i];
//                double u = state.total_n > 0 ? q + (Math.log((state.total_n + 18000 + 1) / 18000) + 1) * policy[i] * sqrt(state.total_n) / (1 + state.n[i]) : policy[i];
                if (u > maxU) {
                    action = i;
                    maxU = u;
                }
            }

            if (numberOfActions == 0) {
                return;
            }

            State nextState = state.ns[action];
            GameState state2;
            if (nextState == null) {
                state2 = state.clone(true);
                state2.doAction(action);
                while (true) {
                    if (state2.isStochastic) {
                        state.ns[action] = new ChanceState(state2, state, action);
                        this.search1_r(state2, training, remainingCalls, false);
                        ((ChanceState) (state.ns[action])).correctV(state2, v);
                        break;
                    } else {
                        if (state.transpositions.get(state2) == null) {
                            state.transpositions.put(state2, state2);
                            if (state2.actionCtx == GameActionCtx.BEGIN_TURN) {
                                state2 = state2.clone(true);
                                state2.doAction(0);
                                continue;
                            }
                            state.ns[action] = state2;
                            this.search1_r(state2, training, remainingCalls, false);
                        }
                        break;
                    }
                }
            } else {
                if (nextState instanceof ChanceState cState) {
                    state2 = cState.getNextState();
                    this.search1_r(state2, training, remainingCalls, false);
                    cState.correctV(state2, v);
                } else {
                    this.search1_r((GameState) nextState, training, remainingCalls, false);
                }
            }

            if (v[0] > -1000000) {
                break;
            } else {
                state.transpositionsPolicyMask[action] = true;
            }
        } while (true);

        state.q_win[action] += v[0];
        state.q_health[action] += v[1];
        state.q_comb[action] += v[2];
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
            actionToPropagate = getActionWithMaxNodesOrTerminal(state);
        }
        double q_win_total = state.q_win[actionToPropagate] / state.n[actionToPropagate] * (state.total_n + 1);
        double q_health_total = state.q_health[actionToPropagate] / state.n[actionToPropagate] * (state.total_n + 1);
        double q_comb_total = state.q_comb[actionToPropagate] / state.n[actionToPropagate] * (state.total_n + 1);
        v[0] = q_win_total - state.total_q_win;
        v[1] = q_health_total - state.total_q_health;
        v[2] = q_comb_total - state.total_q_comb;
        state.total_q_win += v[0];
        state.total_q_health += v[1];
        state.total_q_comb += v[2];
        this.numberOfPossibleActions = numberOfActions;
    }

    void search2(GameState state, boolean training, int remainingCalls) {
        terminal_v_win = -100;
        search2_r(state, training, remainingCalls, true);
    }

    void search2_r(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        if (state.terminal_action >= 0) {
            v[0] = state.q_win[state.terminal_action] / state.n[state.terminal_action];
            v[1] = state.q_health[state.terminal_action] / state.n[state.terminal_action];
            v[2] = state.q_comb[state.terminal_action] / state.n[state.terminal_action];
            return;
        }
        if (state.isTerminal() != 0) {
            state.get_v(v);
            if (v[0] > 0.5 && state.playerTurnStartHealth == state.player.health && !state.prop.playerCanHeal) {
                terminal_v_win = v[0];
            }
            return;
        }
        if (state.policy == null) {
            state.doEval(model);
            state.get_v(v);
            state.total_q_win = v[0];
            state.total_q_health = v[1];
            state.total_q_comb = v[2];
            numberOfPossibleActions = 0;
            for (int i = 0; i < state.prop.maxNumOfActions; i++) {
                if (state.policy[i] > 0) {
                    numberOfPossibleActions++;
                }
            }
            return;
        }

        float[] policy = getPolicy(state, training, remainingCalls, isRoot);
        int action = 0;
        int numberOfActions = 0;
        double maxU = -1000000;
        for (int i = 0; i < state.prop.maxNumOfActions; i++) {
            if (policy[i] <= 0) {
                continue;
            }
            numberOfActions += 1;
            //                double q = state.n[i] > 0 ? GameState.calc_q(state.q_win[i] / state.n[i], state.q_health[i] / state.n[i]) : GameState.calc_q(state.total_q_win / (state.total_n + 1), state.total_q_health / (state.total_n + 1));
            double q = state.n[i] > 0 ? state.q_comb[i] / state.n[i] : 0;
            double u = state.total_n > 0 ? q + 1 * policy[i] * sqrt(state.total_n) / (1 + state.n[i]) : policy[i];
            if (u > maxU) {
                action = i;
                maxU = u;
            }
        }

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
                        var parentState = state2.clone(false);
                        state2.doAction(0);
                        var cState = new ChanceState(state2, state, action);
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
                    v[0] = ns.total_q_win / (ns.total_n + 1);
                    v[1] = ns.total_q_health / (ns.total_n + 1);
                    v[2] = ns.total_q_comb / (ns.total_n + 1);
                } else if (s instanceof ChanceState ns) {
                    state.ns[action] = ns;
                    v[0] = ns.total_q_win / ns.total_n;
                    v[1] = ns.total_q_health / ns.total_n;
                    v[2] = ns.total_q_comb / ns.total_n;
                }
            }
        } else {
            if (nextState instanceof ChanceState cState) {
                if (state.n[action] < cState.total_n) {
                    state2 = cState.getNextState();
                    this.search2_r(state2, training, remainingCalls, false);
                    cState.correctV(state2, v);
                    v[0] = cState.total_q_win / cState.total_n * (state.n[action] + 1) - state.q_win[action];
                    v[1] = cState.total_q_health / cState.total_n * (state.n[action] + 1) - state.q_health[action];
                    v[2] = cState.total_q_comb / cState.total_n * (state.n[action] + 1) - state.q_comb[action];
                } else {
                    state2 = cState.getNextState();
                    this.search2_r(state2, training, remainingCalls, false);
                    cState.correctV(state2, v);
                }
            } else if (nextState instanceof GameState nState) {
                if (state.n[action] < nState.total_n + 1) {
                    this.search2_r(nState, training, remainingCalls, false);
                    v[0] = nState.total_q_win / (nState.total_n + 1) * (state.n[action] + 1) - state.q_win[action];
                    v[1] = nState.total_q_health / (nState.total_n + 1) * (state.n[action] + 1) - state.q_health[action];
                    v[2] = nState.total_q_comb / (nState.total_n + 1) * (state.n[action] + 1) - state.q_comb[action];
                } else {
                    this.search2_r(nState, training, remainingCalls, false);
                }
            }
        }

        state.q_win[action] += v[0];
        state.q_health[action] += v[1];
        state.q_comb[action] += v[2];
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
            v[0] = q_win_total - state.total_q_win;
            v[1] = q_health_total - state.total_q_health;
            v[2] = q_comb_total - state.total_q_comb;
        }
        state.total_q_win += v[0];
        state.total_q_health += v[1];
        state.total_q_comb += v[2];
        this.numberOfPossibleActions = numberOfActions;
    }

    void search3(GameState state, boolean training, int remainingCalls) {
        terminal_v_win = -100;
        search3_r(state, training, remainingCalls, true);
    }

    void search3_r(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        if (state.terminal_action >= 0) {
            v[0] = state.q_win[state.terminal_action] / state.n[state.terminal_action];
            v[1] = state.q_health[state.terminal_action] / state.n[state.terminal_action];
            v[2] = state.q_comb[state.terminal_action] / state.n[state.terminal_action];
            return;
        }
        if (state.isTerminal() != 0) {
            state.get_v(v);
            if (v[0] > 0.5 && state.playerTurnStartHealth == state.player.health && !state.prop.playerCanHeal) {
                terminal_v_win = v[0];
            }
            return;
        }
        if (state.policy == null) {
            state.doEval(model);
            state.get_v(v);
            state.total_q_win = v[0];
            state.total_q_health = v[1];
            state.total_q_comb = v[2];
            numberOfPossibleActions = 0;
             for (int i = 0; i < state.prop.maxNumOfActions; i++) {
                if (state.policy[i] > 0) {
                    numberOfPossibleActions++;
                }
            }
            return;
        }

        float[] policy = getPolicy(state, training, remainingCalls, isRoot);
        int action = 0;
        int numberOfActions = 0;
        double maxU = -1000000;
        for (int i = 0; i < state.prop.maxNumOfActions; i++) {
            if (policy[i] <= 0) {
                continue;
            }
            numberOfActions += 1;
            //                double q = state.n[i] > 0 ? GameState.calc_q(state.q_win[i] / state.n[i], state.q_health[i] / state.n[i]) : GameState.calc_q(state.total_q_win / (state.total_n + 1), state.total_q_health / (state.total_n + 1));
            double q = state.n[i] > 0 ? state.q_comb[i] / state.n[i] : 0;
            double u = state.total_n > 0 ? q + 1 * policy[i] * sqrt(state.total_n) / (1 + state.n[i]) : policy[i];
            if (u > maxU) {
                action = i;
                maxU = u;
            }
        }

        State nextState = state.ns[action];
        GameState state2;
        if (nextState == null) {
            state2 = state.clone(true);
            state2.doAction(action);
            if (state2.isStochastic) {
                state.ns[action] = new ChanceState(state2,state, action);
                this.search3_r(state2, training, remainingCalls, false);
                ((ChanceState) state.ns[action]).correctV(state2, v);
            } else {
                var s = state.transpositions.get(state2);
                if (s == null) {
                    if (state2.actionCtx == GameActionCtx.BEGIN_TURN) {
                        var parentState = state2.clone(false);
                        state2.doAction(0);
                        var cState = new ChanceState(state2, state, action);
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
                    v[0] = ns.total_q_win / (ns.total_n + 1);
                    v[1] = ns.total_q_health / (ns.total_n + 1);
                    v[2] = ns.total_q_comb / (ns.total_n + 1);
                } else if (s instanceof ChanceState ns) {
                    state.ns[action] = ns;
                    v[0] = ns.total_q_win / ns.total_n;
                    v[1] = ns.total_q_health / ns.total_n;
                    v[2] = ns.total_q_comb / ns.total_n;
                }
            }
        } else {
            if (nextState instanceof ChanceState cState) {
                if (state.n[action] < cState.total_n) {
                    state2 = cState.getNextState();
                    this.search3_r(state2, training, remainingCalls, false);
                    cState.correctV(state2, v);
                    v[0] = cState.total_q_win / cState.total_n * (state.n[action] + 1) - state.q_win[action];
                    v[1] = cState.total_q_health / cState.total_n * (state.n[action] + 1) - state.q_health[action];
                    v[2] = cState.total_q_comb / cState.total_n * (state.n[action] + 1) - state.q_comb[action];
                } else {
                    state2 = cState.getNextState();
                    this.search3_r(state2, training, remainingCalls, false);
                    cState.correctV(state2, v);
                }
            } else if (nextState instanceof GameState nState) {
                if (state.n[action] < nState.total_n + 1) {
                    this.search3_r(nState, training, remainingCalls, false);
                    v[0] = nState.total_q_win / (nState.total_n + 1) * (state.n[action] + 1) - state.q_win[action];
                    v[1] = nState.total_q_health / (nState.total_n + 1) * (state.n[action] + 1) - state.q_health[action];
                    v[2] = nState.total_q_comb / (nState.total_n + 1) * (state.n[action] + 1) - state.q_comb[action];
                } else {
                    this.search3_r(nState, training, remainingCalls, false);
                }
            }
        }

        state.q_win[action] += v[0];
        state.q_health[action] += v[1];
        state.q_comb[action] += v[2];
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
            actionToPropagate = getActionWithMaxNodesOrTerminal(state);
        }
        double q_win_total = state.q_win[actionToPropagate] / state.n[actionToPropagate] * (state.total_n + 1);
        double q_health_total = state.q_health[actionToPropagate] / state.n[actionToPropagate] * (state.total_n + 1);
        double q_comb_total = state.q_comb[actionToPropagate] / state.n[actionToPropagate] * (state.total_n + 1);
        v[0] = q_win_total - state.total_q_win;
        v[1] = q_health_total - state.total_q_health;
        v[2] = q_comb_total - state.total_q_comb;
        state.total_q_win += v[0];
        state.total_q_health += v[1];
        state.total_q_comb += v[2];
        this.numberOfPossibleActions = numberOfActions;
    }

    void searchPlain(GameState state, boolean training, int remainingCalls) {
        searchPlain_r(state, training, remainingCalls, true);
    }

    void searchPlain_r(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        if (state.isTerminal() != 0) {
            state.get_v(v);
            return;
        }
        if (state.policy == null) {
            state.doEval(model);
            state.get_v(v);
            state.total_q_win = v[0];
            state.total_q_health = v[1];
            state.total_q_comb = v[2];
            return;
        }

        float[] policy = getPolicy(state, training, remainingCalls, isRoot);
        int action = 0;
        int numberOfActions = 0;
        double maxU = -1000000;
        for (int i = 0; i < state.prop.maxNumOfActions; i++) {
            if (policy[i] <= 0) {
                continue;
            }
            numberOfActions += 1;
            //                double q = state.n[i] > 0 ? GameState.calc_q(state.q_win[i] / state.n[i], state.q_health[i] / state.n[i]) : GameState.calc_q(state.total_q_win / (state.total_n + 1), state.total_q_health / (state.total_n + 1));
            double q = state.n[i] > 0 ? state.q_comb[i] / state.n[i] : 0;
            double u = state.total_n > 0 ? q + 1 * policy[i] * sqrt(state.total_n) / (1 + state.n[i]) : policy[i];
            if (u > maxU) {
                action = i;
                maxU = u;
            }
        }

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
                state2 = cState.getNextState();
                this.searchPlain_r(state2, training, remainingCalls, false);
                cState.correctV(state2, v);
            } else if (nextState instanceof GameState nState) {
                this.searchPlain_r(nState, training, remainingCalls, false);
            }
        }

        state.q_win[action] += v[0];
        state.q_health[action] += v[1];
        state.q_comb[action] += v[2];
        state.n[action] += 1;
        state.total_n += 1;
        state.total_q_win += v[0];
        state.total_q_health += v[1];
        state.total_q_comb += v[2];
        this.numberOfPossibleActions = numberOfActions;
    }

    private float[] getPolicy(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        float[] policy;
        if (training) {
            if (state.policyMod == null) {
                if (isRoot) {
                    state.policyMod = applyDirichletNoiseToPolicy(state, state.policy);
                } else {
                    state.policyMod = state.policy;
                }
            }
            policy = state.policyMod;
        } else {
            policy = applyFutileSearchPruning(state, state.policy, remainingCalls);
        }
        return policy;
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

    private float[] applyDirichletNoiseToPolicy(GameState state, float[] policy) {
        int actionCount = 0;
        for (int i = 0; i < policy.length; i++) {
            if (policy[i] > 0) {
                actionCount += 1;
            }
        }
        if (actionCount > 1) {
            policy = Arrays.copyOf(policy, policy.length);
            var param = new double[actionCount];
            Arrays.fill(param, 2);
            var noiseGen = new Dirichlet(param);
            var noise = noiseGen.nextDistribution(); // todo move out
            int k = 0;
            for (int i = 0; i < policy.length; i++) {
                if (state.isActionLegal(i)) {
                    policy[i] = (float) noise[k] * 0.25f + policy[i] * 0.75f;
                    k += 1;
                }
            }
        }
        return policy;
    }

    public static int getActionRandomOrTerminal(GameState state) {
        if (state.terminal_action >= 0) {
            return state.terminal_action;
        }
        int r = state.prop.random.nextInt(state.total_n);
        int acc = 0;
        int action = -1;
        for (int i = 0; i < state.policy.length; i++) {
            acc += state.n[i];
            if (acc > r) {
                action = i;
                break;
            }
        }
        return action;
    }

    public static int getActionWithMaxNodesOrTerminal(GameState state) {
        if (state.terminal_action >= 0) {
            return state.terminal_action;
        }
        int actionToPropagate = -1;
        float max_n = -1000;
        for (int i = 0; i < state.prop.maxNumOfActions; i++) {
            if (state.isActionLegal(i) && !state.transpositionsPolicyMask[i]) {
                if (state.n[i] > max_n) {
                    max_n = state.n[i];
                    actionToPropagate = i;
                }
            }
        }
        if (actionToPropagate == -1) {
            System.out.println("!!!!!!!|| " + state.toStringReadable());
            System.out.println(Arrays.toString(state.transpositionsPolicyMask));
        }
        return actionToPropagate;
    }
}
