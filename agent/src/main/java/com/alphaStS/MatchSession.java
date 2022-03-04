package com.alphaStS;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

record GameStep(GameState state, int action) {
}

public class MatchSession {
    long totalDamageTaken = 0;
    long deathCount = 0;
    boolean logGame;
    int game_i = 0;
    MCTS mcts;
    String logDir;
    GameState origState;
    List<GameStep> states;

    public MatchSession(GameState state, String tmpDir) {
        Model model = new Model(tmpDir);
        mcts = new MCTS();
        mcts.setModel(model);
        this.origState = state;
        states = new ArrayList<GameStep>();
        logDir = tmpDir;
    }

    public void playGame(int nodeCount) {
        var state = new GameState(origState);
        if (state.actionCtx == GameActionCtx.START_GAME) {
            state.doAction(0);
        }
        state.doEval(mcts.model);
        states.clear();
        while (state.isTerminal() == 0) {
            int upto = nodeCount - state.total_n;
            for (int i = 0; i < upto; i++) {
                mcts.search(state, false, upto - i);
                if (mcts.numberOfPossibleActions == 1) {
                    break;
                }
            }

            State nextState = null;
            int max_n = 0;
            int action = 0;
            for (int i = 0; i < state.prop.maxNumOfActions; i++) {
                if (state.n[i] > max_n) {
                    action = i;
                    nextState = state.ns[i];
                    max_n = state.n[i];
                }
            }

            GameState newState;
            if (nextState instanceof ChanceState cState) {
                newState = cState.getNextState(state, action);
                if (newState.policy == null) {
                    newState.doEval(mcts.model);
                }
            } else {
                newState = (GameState) nextState;
            }
            states.add(new GameStep(state, action));
            state = newState;
        }
        states.add(new GameStep(state, -1));
        if (state.isTerminal() == -1) {
            deathCount += 1;
        }
        totalDamageTaken += state.player.origHealth - state.player.health;
        game_i += 1;
        if (logGame) {
            try (FileWriter writer = new FileWriter(logDir + "/matches.txt", true)) {
                writer.write("*** Match " + game_i + " ***\n");
                writer.write("Result: " + (state.isTerminal() == 1 ? "Win" : "Loss") + "\n");
                writer.write("Damage Taken: " + (origState.player.origHealth - state.player.health) + "\n");
                for (GameStep step : states) {
                    writer.write(step.state().toStringReadable() + "\n");
                    if (step.action() >= 0) {
                        writer.write("action=" + step.state().getActionString(step.action()) + " (" + step.action() + ")\n");
                    }
                }
                writer.write("\n");
                writer.write("\n");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void playTrainingGame(int nodeCount) throws IOException {
        var state = new GameState(origState);
        if (state.actionCtx == GameActionCtx.START_GAME) {
            state.doAction(0);
        }
        Random random = state.prop.random;
        for (Enemy enemy : state.enemies) {
            enemy.randomize(random);
        }

        state.doEval(mcts.model);
        states.clear();
        while (state.isTerminal() == 0) {
            for (int i = 0; i < nodeCount; i++) {
                mcts.search(state, true, -1);
            }

            State nextState = null;
            int max_n = 0;
            int action = 0;
            int turnCount = 0;
            if (turnCount >= 0) {
                for (int i = 0; i < state.prop.maxNumOfActions; i++) {
                    if (state.transpositions_policy_mask[i]) {
                        continue;
                    }
                    if (state.n[i] > max_n) {
                        action = i;
                        nextState = state.ns[i];
                        max_n = state.n[i];
                    }
                }
            } else {
                int r = random.nextInt(state.total_n);
                int acc = 0;
                for (int i = 0; i < state.policy.length; i++) {
                    if (state.transpositions_policy_mask[i]) {
                        continue;
                    }
                    acc += state.n[i];
                    if (acc > r) {
                        nextState = state.ns[i];
                        action = i;
                        break;
                    }
                }
            }

            GameState newState;
            if (nextState instanceof ChanceState cState) {
                newState = cState.getNextState(state, action);
                if (newState.policy == null) {
                    newState.doEval(mcts.model);
                }
            } else {
                newState = (GameState) nextState;
            }
            states.add(new GameStep(state, action));
            if (newState == null) {
                System.out.println(state);
                System.out.println(Arrays.toString(state.transpositions_policy_mask));
                System.out.println(state.transpositions);
                System.out.println(action);
                System.out.println(mcts.numberOfPossibleActions);
            }
            state = newState;
        }
        states.add(new GameStep(state, -1));
    }
}
