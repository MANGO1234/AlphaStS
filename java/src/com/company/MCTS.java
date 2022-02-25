package com.company;

import static java.lang.Math.sqrt;

public class MCTS {
    Model model;
    GameState root;
    int numberOfActions;

    void setModel(Model model) {
        this.model = model;
    }

    public void setRoot(GameState root) {
        this.root = root;
    }

    double search(GameState state, boolean training, int remainingCalls) {
        if (state.isTerminal() != 0) {
            return state.get_v();
        }

        double[] policy = null;
        if (state.policy == null) {
            state.doEval(model);
        }
        if (training) {
            //            policy = np.array(state.policy, copy=True)
            //            action_n = 0
            //            for i in range(0, len(policy)):
            //                if state.is_action_legal()[i]:
            //                    action_n += 1
            //            noise = np.random.dirichlet([0.2] * action_n)
            //            k = 0
            //            for i in range(0, len(policy)):
            //                if state.is_action_legal()[i]:
            //                    policy[i] = noise[k] * 0.25 + policy[i] * 0.75
            //                    k += 1
        } else {
            policy = state.policy;
        }

        int action = 0;
        double maxU = -1000000;
        int numberOfActions = 0;
        int max_n = Utils.max(state.n);
        for (int i = 0; i < state.numOfActions; i++) {
            if (!state.is_action_legal()[i]) {
                continue;
            }
            if (remainingCalls > 0 && max_n - state.n[i] > remainingCalls) {
                continue;
            }
            numberOfActions += 1;
            double u = (state.n[i] > 0 ? state.q[i] / state.n[i] : 0) + 3 * policy[i] * sqrt(state.total_n) / (1 + state.n[i]);
            if (u > maxU) {
                action = i;
                maxU = u;
            }
        }
        State nextState = state.ns[action];
        GameState state2;
        double v;
        if (nextState == null) {
            if (action == state.numOfActions - 1 && state.deckArr.length != 5) {
                var cState = new ChanceState();
                state.ns[action] = cState;
                state2 = cState.getNextState(state, action);
            } else {
                state2 = new GameState((GameState) state);
                state2.doAction(action);
                state.ns[action] = state2;
            }
            if (state2.policy == null) {
                state2.doEval(model);
            }
            v = state2.get_v();
        } else {
            if (nextState instanceof ChanceState cState) {
                state2 = cState.getNextState(state, action);
                if (state2.policy == null) {
                    state2.doEval(model);
                    v = state2.get_v();
                } else {
                    v = this.search(state2, training, remainingCalls);
                }
            } else {
                v = this.search((GameState) nextState, training, remainingCalls);
            }
        }
        state.q[action] += v;
        state.n[action] += 1;
        state.total_n += 1;
        this.numberOfActions = numberOfActions;
        return v;
    }

    void printTreeH(State s, int depth, String indent) {
        if (depth == 0) {
            return;
        }
        if (s instanceof ChanceState state) {
            state.cache.forEach((_a, node) -> {
                System.out.println(indent + "Chance Node (" + node.n / state.total_n + ": " + node.state);
                for (int i = 0; i < node.state.policy.length; i++) {
                    if (node.state.ns[i] != null) {
                        if (depth > 1) {
                            System.out.println(indent + "  - action=" + i);
                        }
                    }
                    this.printTreeH(node.state.ns[i], depth - 1, indent + "    ");
                }
            });
        } else if (s instanceof GameState state) {
            System.out.println(indent + "Normal Node: " + state);
            for (int i = 0; i < state.policy.length; i++) {
                if (state.ns[i] != null) {
                    if (depth > 1) {
                        System.out.println(indent + "  - action=" + i);
                    }
                }
                this.printTreeH(state.ns[i], depth - 1, indent + "    ");
            }
        }
    }

    void printTree(int depth) {
        this.printTreeH(root, depth, "");
    }
}
