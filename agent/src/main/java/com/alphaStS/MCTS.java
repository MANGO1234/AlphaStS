package com.alphaStS;

import java.io.Writer;
import java.io.IOException;
import java.util.Arrays;

import cc.mallet.types.Dirichlet;
import com.alphaStS.utils.Utils;

import static java.lang.Math.sqrt;

public class MCTS {
    Model model;
    int numberOfPossibleActions;
    double[] v;
    double terminal_v_win;
    double terminal_v_health;

    void setModel(Model model) {
        v = new double[3];
        this.model = model;
    }

    void search(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        search2(state, training, remainingCalls, isRoot);
    }

    void search1(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        if (state.terminal_v_win == 1) {
            v[0] = state.terminal_v_win;
            v[1] = state.terminal_v_health;
            terminal_v_win = -100; // don't propagate upward
            return;
        }
        if (state.isTerminal() != 0) {
            state.get_v(v);
//            if (v[0] == 1) {
//                terminal_v_win = v[0];
//                terminal_v_health = v[1];
//            } else {
                terminal_v_win = -100;
//            }
//            return;
        }
        if (state.policy == null) {
            terminal_v_win = -100;
            state.doEval(model);
            state.get_v(v);
            state.total_q_win = v[0];
            state.total_q_health = v[1];
            state.total_q_comb = v[2];
            return;
        }

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
            policy = state.policy;
        }
        if (!training) {
            policy = applyFutileSearchPruning(state, policy, remainingCalls);
        }

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
                double q = state.n[i] > 0 ? GameState.calc_q(state.q_win[i] / state.n[i], state.q_health[i] / state.n[i]) : 0;
                double u = q + 1 * policy[i] * sqrt(state.total_n) / (1 + state.n[i]);
                if (u > maxU) {
                    action = i;
                    maxU = u;
                }
            }

            if (numberOfActions == 0) {
                terminal_v_win = -100;
                return;
            }

            State nextState = state.ns[action];
            GameState state2;
            if (nextState == null) {
                state2 = state.clone(true);
                state2.doAction(action);
                if (state2.isStochastic) {
                    var cState = new ChanceState(state2);
                    state.ns[action] = cState;
                    this.search1(state2, training, remainingCalls, false);
                } else {
                    if (state.transpositions.get(state2) == null) {
                        state.transpositions.put(state2, state);
                        state.ns[action] = state2;
                        this.search1(state2, training, remainingCalls, false);
                    }
                }
            } else {
                if (nextState instanceof ChanceState cState) {
                    state2 = cState.getNextState(state, action);
                    this.search1(state2, training, remainingCalls, false);
                } else {
                    this.search1((GameState) nextState, training, remainingCalls, false);
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
        int max_n_i = 0;
        if (terminal_v_win > 0.5) {
            state.terminal_v_win = terminal_v_win;
            state.terminal_v_health = terminal_v_health;
            if (state.isStochastic) {
                terminal_v_win = -100;
            }
            max_n_i = action;
        } else {
            float max_n = -1000;
            for (int i = 0; i < state.prop.maxNumOfActions; i++) {
                if (state.isActionLegal(i)) {
                    if (state.n[i] > max_n) {
                        max_n = state.n[i];
                        max_n_i = i;
                    }
                }
            }
        }
        double q_win_total = state.q_win[max_n_i] / state.n[max_n_i] * (state.total_n + 1);
        double q_health_total = state.q_health[max_n_i] / state.n[max_n_i] * (state.total_n + 1);
        double q_comb_total = state.q_comb[max_n_i] / state.n[max_n_i] * (state.total_n + 1);
        v[0] = q_win_total - state.total_q_win;
        v[1] = q_health_total - state.total_q_health;
        v[2] = q_comb_total - state.total_q_comb;
        state.total_q_win += v[0];
        state.total_q_health += v[1];
        state.total_q_comb += v[2];
        this.numberOfPossibleActions = numberOfActions;
    }

    void search2(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        if (state.terminal_v_win == 1) {
            v[0] = state.terminal_v_win;
            v[1] = state.terminal_v_health;
            terminal_v_win = -100; // don't propagate upward
            return;
        }
        if (state.isTerminal() != 0) {
            state.get_v(v);
//            if (v[0] == 1) {
//                terminal_v_win = v[0];
//                terminal_v_health = v[1];
//            } else {
                terminal_v_win = -100;
//            }
            return;
        }
        if (state.policy == null) {
            terminal_v_win = -100;
            state.doEval(model);
            state.get_v(v);
            state.total_q_win = v[0];
            state.total_q_health = v[1];
            state.total_q_comb = v[2];
            return;
        }

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
            policy = state.policy;
        }
        if (!training) {
            policy = applyFutileSearchPruning(state, policy, remainingCalls);
        }

        int action = 0;
        int numberOfActions = 0;
        double maxU = -1000000;
        for (int i = 0; i < state.prop.maxNumOfActions; i++) {
            if (policy[i] <= 0) {
                continue;
            }
            numberOfActions += 1;
            double q = state.n[i] > 0 ? GameState.calc_q(state.q_win[i] / state.n[i], state.q_health[i] / state.n[i]) : 0;
            double u = q + 1 * policy[i] * sqrt(1 + state.total_n) / (1 + state.n[i]);
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
                var cState = new ChanceState(state2);
                state.ns[action] = cState;
                this.search2(state2, training, remainingCalls, false);
            } else {
                if (state.transpositions.get(state2) == null) {
                    state.ns[action] = state2;
                    state.transpositions.put(state2, state2);
                    this.search2(state2, training, remainingCalls, false);
                } else {
                    state.ns[action] = state.transpositions.get(state2);
                    var s = ((GameState) state.ns[action]);
                    s.get_v(v);
                    v[0] = (s.total_q_win + v[0]) / (s.total_n + 1);
                    v[1] = (s.total_q_health + v[1]) / (s.total_n + 1);
                    v[2] = (s.total_q_comb + v[2]) / (s.total_n + 1);
                    state.transpositionsPolicyMask[action] = true;
                }
            }
        } else {
            if (nextState instanceof ChanceState cState) {
                state2 = cState.getNextState(state, action);
                this.search2(state2, training, remainingCalls, false);
            } else {
                if (state.transpositionsPolicyMask[action] && state.n[action] < ((GameState) state.ns[action]).total_n + 1) {
                    var s = ((GameState) state.ns[action]);
                    s.get_v(v);
                    v[0] = (s.total_q_win + v[0]) / (s.total_n + 1) * (state.n[action] + 1) - state.q_win[action];
                    v[1] = (s.total_q_health + v[1]) / (s.total_n + 1) * (state.n[action] + 1) - state.q_health[action];
                    v[2] = (s.total_q_comb + v[1]) / (s.total_n + 1) * (state.n[action] + 1) - state.q_comb[action];
                } else {
                    this.search2((GameState) nextState, training, remainingCalls, false);
                }
            }
        }

        state.q_win[action] += v[0];
        state.q_health[action] += v[1];
        state.q_comb[action] += v[2];
        state.n[action] += 1;
        state.total_n += 1;
        if (terminal_v_win > 0.5) {
            state.terminal_v_win = terminal_v_win;
            state.terminal_v_health = terminal_v_health;
            if (state.isStochastic) {
                terminal_v_win = -100;
            }
//            double q_win_total = state.q_win[action] / state.n[action] * (state.total_n + 1);
//            double q_health_total = state.q_health[action] / state.n[action] * (state.total_n + 1);
//            v[0] = q_win_total - state.total_q_win;
//            v[1] = q_health_total - state.total_q_health;
        }
        state.total_q_win += v[0];
        state.total_q_health += v[1];
        state.total_q_comb += v[2];
        this.numberOfPossibleActions = numberOfActions;
    }

    void search3(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        if (state.terminal_v_win == 1) {
            v[0] = state.terminal_v_win;
            v[1] = state.terminal_v_health;
            terminal_v_win = -100; // don't propagate upward
            return;
        }
        if (state.isTerminal() != 0) {
            state.get_v(v);
            //            if (v[0] == 1) {
            //                terminal_v_win = v[0];
            //                terminal_v_health = v[1];
            //            } else {
            terminal_v_win = -100;
            //            }
            return;
        }
        if (state.policy == null) {
            terminal_v_win = -100;
            state.doEval(model);
            state.get_v(v);
            state.total_q_win = v[0];
            state.total_q_health = v[1];
            state.total_q_comb = v[2];
            return;
        }

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
            policy = state.policy;
        }
        if (!training) {
            policy = applyFutileSearchPruning(state, policy, remainingCalls);
        }

        int action = 0;
        int numberOfActions = 0;
        double maxU = -1000000;
        for (int i = 0; i < state.prop.maxNumOfActions; i++) {
            if (policy[i] <= 0) {
                continue;
            }
            numberOfActions += 1;
            double q = state.n[i] > 0 ? GameState.calc_q(state.q_win[i] / state.n[i], state.q_health[i] / state.n[i]) : 0;
            double u = q + 1 * policy[i] * sqrt(1 + state.total_n) / (1 + state.n[i]);
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
                var cState = new ChanceState(state2);
                state.ns[action] = cState;
                this.search3(state2, training, remainingCalls, false);
            } else {
                if (state.transpositions.get(state2) == null) {
                    state.ns[action] = state2;
                    state.transpositions.put(state2, state2);
                    this.search3(state2, training, remainingCalls, false);
                } else {
                    state.ns[action] = state.transpositions.get(state2);
                    var s = ((GameState) state.ns[action]);
                    s.get_v(v);
                    v[0] = (s.total_q_win + v[0]) / (s.total_n + 1);
                    v[1] = (s.total_q_health + v[1]) / (s.total_n + 1);
                    v[2] = (s.total_q_comb + v[2]) / (s.total_n + 1);
                }
                state.transpositionsPolicyMask[action] = true;
            }
        } else {
            if (nextState instanceof ChanceState cState) {
                state2 = cState.getNextState(state, action);
                this.search3(state2, training, remainingCalls, false);
            } else {
                if (state.transpositionsPolicyMask[action] && state.n[action] < ((GameState) state.ns[action]).total_n + 1) {
                    var s = ((GameState) state.ns[action]);
                    if ((s.total_q_comb + v[1]) / (s.total_n + 1) - state.q_comb[action] / (state.n[action]) < 0.001) {
                        this.search3(s, training, remainingCalls, false);
                    }
                    s.get_v(v);
                    v[0] = (s.total_q_win + v[0]) / (s.total_n + 1) * (state.n[action] + 1) - state.q_win[action];
                    v[1] = (s.total_q_health + v[1]) / (s.total_n + 1) * (state.n[action] + 1) - state.q_health[action];
                    v[2] = (s.total_q_comb + v[1]) / (s.total_n + 1) * (state.n[action] + 1) - state.q_comb[action];
//                    System.out.println((s.total_q_win + v[0]) / (s.total_n + 1) - state.q_win[action] / (state.n[action]));
                } else {
                    this.search3((GameState) nextState, training, remainingCalls, false);
                }
            }
        }

        state.q_win[action] += v[0];
        state.q_health[action] += v[1];
        state.q_comb[action] += v[2];
        state.n[action] += 1;
        state.total_n += 1;
        if (terminal_v_win > 0.5) {
            state.terminal_v_win = terminal_v_win;
            state.terminal_v_health = terminal_v_health;
            if (state.isStochastic) {
                terminal_v_win = -100;
            }
            //            double q_win_total = state.q_win[action] / state.n[action] * (state.total_n + 1);
            //            double q_health_total = state.q_health[action] / state.n[action] * (state.total_n + 1);
            //            v[0] = q_win_total - state.total_q_win;
            //            v[1] = q_health_total - state.total_q_health;
        }
        state.total_q_win += v[0];
        state.total_q_health += v[1];
        state.total_q_comb += v[2];
        this.numberOfPossibleActions = numberOfActions;
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

    static private void printTreeH(State s, int depth, Writer writer, String indent) throws IOException {
        if (depth == 0) {
            return;
        }
        if (s instanceof ChanceState state) {
            for (ChanceState.Node node : state.cache.values()) {
                writer.write(indent + "Chance Node (" + node.n + "/" + state.total_n + "): " + node.state + "\n");
                for (int i = 0; i < node.state.prop.maxNumOfActions; i++) {
                    if (node.state.ns[i] != null) {
                        if (depth > 1) {
                            writer.write(indent + "  - action=" + i + "\n");
                        }
                    }
                    printTreeH(node.state.ns[i], depth - 1, writer, indent + "    ");
                }
            }
        } else if (s instanceof GameState state) {
            writer.write(indent + "Normal Node: " + state + "\n");
            for (int i = 0; i < state.prop.maxNumOfActions; i++) {
                if (state.ns[i] != null) {
                    if (depth > 1) {
                        writer.write(indent + "  - action=" + i + "\n");
                    }
                }
                printTreeH(state.ns[i], depth - 1, writer, indent + "    ");
            }
        }
    }

    static void printTree(State state,Writer writer, int depth) throws IOException {
        printTreeH(state, depth, writer, "");
        writer.flush();
    }
}
