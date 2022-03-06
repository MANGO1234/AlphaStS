package com.alphaStS;

import java.io.*;
import java.util.*;

record GameStep(GameState state, int action) {
}

public class MatchSession {
    long totalDamageTaken = 0;
    long deathCount = 0;
    boolean logGame;
    int startingAction;
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
        startingAction = -1;
    }

    public void playGame(int nodeCount) {
        var state = origState.clone(false);
        state.transpositions = new HashMap<>();
        if (state.actionCtx == GameActionCtx.START_GAME) {
            state.doAction(0);
        } else if (state.actionCtx == GameActionCtx.PLAY_CARD && startingAction >= 0) {
            state.doAction(startingAction);
        }
        state.doEval(mcts.model);
        states.clear();
        while (state.isTerminal() == 0) {
            int upto = nodeCount - state.total_n;
            for (int i = 0; i < upto; i++) {
                mcts.search(state, false, upto - i, true);
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
        var state = origState.clone(false);
        if (state.actionCtx == GameActionCtx.START_GAME) {
            state.doAction(0);
        }
        Random random = state.prop.random;
        for (Enemy enemy : state.enemies) {
            if (enemy.health > 0) {
                enemy.randomize(random);
            }
        }

        state.doEval(mcts.model);
        states.clear();
        while (state.isTerminal() == 0) {
            for (int i = 0; i < nodeCount; i++) {
                mcts.search(state, true, -1, true);
            }

            State nextState = null;
            int max_n = 0;
            int action = 0;
            int turnCount = 0;
            if (turnCount >= 0) {
                for (int i = 0; i < state.prop.maxNumOfActions; i++) {
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
            state = newState;
        }
        states.add(new GameStep(state, -1));
    }
}
