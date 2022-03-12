package com.alphaStS;

import java.io.OutputStreamWriter;
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

    void setModel(Model model) {
        v = new double[3];
        this.model = model;
    }

    void search(GameState state, boolean training, int remainingCalls) {
        search1(state, training, remainingCalls);
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
            if (v[0] > 0.5) {
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
            policy = applyFutileSearchPruning(state, state.policy, remainingCalls);
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
                double q = state.n[i] > 0 ? state.q_comb[i] / state.n[i] : 0;
                double u = state.total_n > 0 ? q + 1 * policy[i] * sqrt(state.total_n) / (1 + state.n[i]) : policy[i];
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
                if (state2.isStochastic) {
                    state.ns[action] = new ChanceState(state2);
                    this.search1_r(state2, training, remainingCalls, false);
                } else {
                    if (state.transpositions.get(state2) == null) {
                        state.transpositions.put(state2, state);
                        state.ns[action] = state2;
                        this.search1_r(state2, training, remainingCalls, false);
                    }
                }
            } else {
                if (nextState instanceof ChanceState cState) {
                    state2 = cState.getNextState(state, action);
                    this.search1_r(state2, training, remainingCalls, false);
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
            if (v[0] > 0.5) {
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
            policy = applyFutileSearchPruning(state, state.policy, remainingCalls);
        }

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
                state.ns[action] = new ChanceState(state2);
                this.search2_r(state2, training, remainingCalls, false);
            } else {
                var s = state.transpositions.get(state2);
                if (s == null) {
                    state.ns[action] = state2;
                    state.transpositions.put(state2, state2);
                    this.search2_r(state2, training, remainingCalls, false);
                } else {
                    state.ns[action] = s;
                    v[0] = s.total_q_win / (s.total_n + 1);
                    v[1] = s.total_q_health / (s.total_n + 1);
                    v[2] = s.total_q_comb / (s.total_n + 1);
                }
            }
        } else {
            if (nextState instanceof ChanceState cState) {
                state2 = cState.getNextState(state, action);
                this.search2_r(state2, training, remainingCalls, false);
            } else {
                if (state.n[action] < ((GameState) state.ns[action]).total_n + 1) {
                    var s = ((GameState) state.ns[action]);
//                    this.search2_r(s, training, remainingCalls, false);
                    v[0] = s.total_q_win / (s.total_n + 1) * (state.n[action] + 1) - state.q_win[action];
//                    System.out.println(v[0] + "|" + s.total_q_win + "|" + (s.total_n + 1) + "|" + (state.n[action] + 1) + "|" + state.q_win[action] + "|" + state.toStringReadable());
                    v[1] = s.total_q_health / (s.total_n + 1) * (state.n[action] + 1) - state.q_health[action];
                    v[2] = s.total_q_comb / (s.total_n + 1) * (state.n[action] + 1) - state.q_comb[action];
                } else {
                    this.search2_r((GameState) nextState, training, remainingCalls, false);
                }
            }
        }

        state.q_win[action] += v[0];
        state.q_health[action] += v[1];
        state.q_comb[action] += v[2];
        state.n[action] += 1;
        state.total_n += 1;
        if (state.q_win[action] / state.n[action] > 1.0001) {
            System.out.println(state.toStringReadable() + "|" + v[0] + "|" + (state.q_win[action] / state.n[action] - 1) + "|" + state.transpositionsPolicyMask[action]);
            MCTS.printTree(state.ns[action], null, 3);
            Integer.parseInt(null);
        }
        if (state.q_health[action] / state.n[action] > 1.0001) {
            System.out.println(state.toStringReadable() + "|" + v[1] + "|" + (state.q_health[action] / state.n[action] - 1));
            MCTS.printTree(state.ns[action], null, 3);
            Integer.parseInt(null);
        }
        if (state.q_comb[action] / state.n[action] > 1.0001) {
            System.out.println(state.toStringReadable() + "|" + v[2] + "|" + (state.q_comb[action] / state.n[action] - 1));
            MCTS.printTree(state.ns[action], null, 3);
            Integer.parseInt(null);
        }
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
//            System.out.println(v[0] + "|" + q_win_total + "|" + state.total_q_win + "|" + state.toStringReadable());
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

    public static int getActionWithMaxNodesOrTerminal(GameState state) {
        if (state.terminal_action >= 0) {
            return state.terminal_action;
        }
        int actionToPropagate = -1;
        float max_n = -1000;
        for (int i = 0; i < state.prop.maxNumOfActions; i++) {
            if (state.isActionLegal(i)) {
                if (state.n[i] > max_n) {
                    max_n = state.n[i];
                    actionToPropagate = i;
                }
            }
        }
        return actionToPropagate;
    }

    static private void printTreeH(State s, int depth, Writer writer, String indent) throws IOException {
        if (depth == 0) {
            return;
        }
        if (s instanceof ChanceState state) {
            for (ChanceState.Node node : state.cache.values()) {
                writer.write(indent + "Chance Node (" + node.n + "/" + state.total_n + "): " + node.state.toStringReadable() + "\n");
                for (int i = 0; i < node.state.prop.maxNumOfActions; i++) {
                    if (node.state.ns[i] != null) {
                        if (depth > 1) {
                            writer.write(indent + "  - action=" + node.state.getActionString(i) + "(" + i + ")\n");
                        }
                    }
                    printTreeH(node.state.ns[i], depth - 1, writer, indent + "    ");
                }
            }
        } else if (s instanceof GameState state) {
            writer.write(indent + "Normal Node: " + state.toStringReadable() + "\n");
            for (int i = 0; i < state.prop.maxNumOfActions; i++) {
                if (state.ns[i] != null) {
                    if (depth > 1) {
                        writer.write(indent + "  - action=" + state.getActionString(i) + "(" + i + ")\n");
                    }
                }
                printTreeH(state.ns[i], depth - 1, writer, indent + "    ");
            }
        }
    }

    static void printTree(State state, Writer writer, int depth) {
        try {
            if (writer == null) {
                writer = new OutputStreamWriter(System.out);
            }
            printTreeH(state, depth, writer, "");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
