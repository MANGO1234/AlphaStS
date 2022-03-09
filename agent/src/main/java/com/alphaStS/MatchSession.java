package com.alphaStS;

import java.io.*;
import java.util.*;

record GameStep(GameState state, int action) {
}

public class MatchSession {
    boolean randomize_enemy;
    long totalDamageTaken = 0;
    long deathCount = 0;
    Writer matchLogWriter;
    Writer trainingDataWriter;
    int startingAction;
    int game_i = 0;
    int trainingGame_i = 0;
    MCTS mcts;
    String logDir;
    GameState origState;
    List<GameStep> states;

    public MatchSession(GameState state, String dir) {
        Model model = new Model(dir);
        mcts = new MCTS();
        mcts.setModel(model);
        this.origState = state;
        states = new ArrayList<GameStep>();
        logDir = dir;
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
        if (matchLogWriter != null) {
            try {
                matchLogWriter.write("*** Match " + game_i + " ***\n");
                matchLogWriter.write("Result: " + (state.isTerminal() == 1 ? "Win" : "Loss") + "\n");
                matchLogWriter.write("Damage Taken: " + (origState.player.origHealth - state.player.health) + "\n");
                for (GameStep step : states) {
                    matchLogWriter.write(step.state().toStringReadable() + "\n");
                    if (step.action() >= 0) {
                        matchLogWriter.write("action=" + step.state().getActionString(step.action()) + " (" + step.action() + ")\n");
                    }
                }
                matchLogWriter.write("\n");
                matchLogWriter.write("\n");
            } catch (IOException e) {
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
                enemy.randomize(random, randomize_enemy);
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
            state = newState.clone(false);
        }
        states.add(new GameStep(state, -1));
        trainingGame_i += 1;
        if (trainingDataWriter != null) {
            try {
                trainingDataWriter.write("*** Match " + trainingGame_i + " ***\n");
                trainingDataWriter.write("Result: " + (state.isTerminal() == 1 ? "Win" : "Loss") + "\n");
                trainingDataWriter.write("Damage Taken: " + (origState.player.origHealth - state.player.health) + "\n");
                for (GameStep step : states) {
                    trainingDataWriter.write(step.state().toStringReadable() + "\n");
                    if (step.action() >= 0) {
                        trainingDataWriter.write("action=" + step.state().getActionString(step.action()) + " (" + step.action() + ")\n");
                    }
                }
                trainingDataWriter.write("\n");
                trainingDataWriter.write("\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setMatchLogFile(String fileName) {
        try {
            File file = new File(logDir + "/" + fileName);
            file.delete();
            matchLogWriter = new BufferedWriter(new FileWriter(logDir + "/" + fileName, true));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setTrainingDataLogFile(String fileName) {
        try {
            File file = new File(logDir + "/" + fileName);
            file.delete();
            trainingDataWriter = new BufferedWriter(new FileWriter(logDir + "/" + fileName, true));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void flushFileWriters() {
        try {
            if (matchLogWriter != null) {
                matchLogWriter.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            if (trainingDataWriter != null) {
                trainingDataWriter.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
