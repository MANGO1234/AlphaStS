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

    void setModel(Model model) {
        this.model = model;
    }

    double search(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        if (state.isTerminal() != 0) {
            return state.get_v();
        }

        if (state.policy == null) {
            state.doEval(model);
        }
        float[] policy = state.policy;
//        if (!training) {
            policy = applyFutileSearchPruning(state, policy, remainingCalls);
//        }
        if (isRoot && training) {
            policy = applyDirichletNoiseToPolicy(state, policy);
        }

        int action = 0;
        int numberOfActions = 0;
        double v;
        double maxU = -1000000;
        for (int i = 0; i < state.prop.maxNumOfActions; i++) {
            if (policy[i] <= 0) {
                continue;
            }
            numberOfActions += 1;
            double u = (state.n[i] > 0 ? state.q[i] / state.n[i] : 0) + 1 * policy[i] * sqrt(1 + state.total_n) / (1 + state.n[i]);
            if (u > maxU) {
                action = i;
                maxU = u;
            }
        }

        State nextState = state.ns[action];
        GameState state2;
        if (nextState == null) {
            state2 = new GameState((GameState) state);
            state2.doAction(action);
            if (state2.isStochastic) {
                var cState = new ChanceState(state2);
                state.ns[action] = cState;
                if (state2.policy == null) {
                    state2.doEval(model);
                }
                v = state2.get_v();
            } else {
//                    state.ns[action] = state2;
//                    if (state2.policy == null) {
//                        state2.doEval(model);
//                    }
//                    v = state2.get_v();
//                    state.transpositions.put(state2, state2);
                if (state.transpositions.get(state2) == null) {
                    state.ns[action] = state2;
                    if (state2.policy == null) {
                        state2.doEval(model);
                    }
                    v = state2.get_v();
                    state.transpositions.put(state2, state2);
                } else {
                    state.ns[action] = state.transpositions.get(state2);
                    var s = ((GameState) state.ns[action]);
                    v = (s.total_q + s.get_v()) / (s.total_n + 1);
                    state.transpositions_policy_mask[action] = true;
                }
            }
        } else {
            if (nextState instanceof ChanceState cState) {
                state2 = cState.getNextState(state, action);
                if (state2.policy == null) {
                    state2.doEval(model);
                    v = state2.get_v();
                } else {
                    v = this.search(state2, training, remainingCalls, false);
                }
            } else {
                if (state.transpositions_policy_mask[action] && state.n[action] < ((GameState) state.ns[action]).total_n + 1) {
                    var s = ((GameState) state.ns[action]);
                    v = (s.total_q + s.get_v()) / (s.total_n + 1);
                } else {
                    v = this.search((GameState) nextState, training, remainingCalls, false);
                }
            }
        }

        state.q[action] += v;
        state.n[action] += 1;
        state.total_n += 1;
        state.total_q += v;
        this.numberOfPossibleActions = numberOfActions;
        return v;
    }

    private float[] applyFutileSearchPruning(GameState state, float[] policy, int remainingCalls) {
        if (remainingCalls <= 0) {
            return policy;
        }
        int max_n = Utils.max(state.n);
        float[] newPolicy = policy;
        float sumP = 1.0f;
        float pp = 0;
        for (int i = 0; i < policy.length; i++) {
            if (policy[i] > 0 && max_n - state.n[i] > remainingCalls) {
                if (newPolicy == policy) {
                    newPolicy = Arrays.copyOf(policy, policy.length);
                }
                sumP -= newPolicy[i];
                newPolicy[i] = 0;
            }
            pp += policy[i];
        }
        assert pp == 1.0f;
        if (newPolicy != policy) {
//            for (int i = 0; i < newPolicy.length; i++) {
//                if (newPolicy[i] > 0) {
//                    newPolicy[i] = newPolicy[i] / sumP;
//                }
//            }
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
