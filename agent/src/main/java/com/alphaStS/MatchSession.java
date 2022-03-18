package com.alphaStS;

import java.io.*;
import java.util.*;

record GameStep(GameState state, int action) {
}

public class MatchSession {
    long totalDamageTaken = 0;
    long deathCount = 0;
    Writer matchLogWriter;
    Writer trainingDataWriter;
    int startingAction = -1;
    int game_i = 0;
    int trainingGame_i = 0;
    MCTS mcts;
    String logDir;
    GameState origState;
    Map<Integer, Integer> damageCount = new HashMap<>();
    List<GameStep> states = new ArrayList<>();

    public MatchSession(GameState state, String dir) {
        Model model = new Model(dir);
        mcts = new MCTS();
        mcts.setModel(model);
        this.origState = state;
        logDir = dir;
    }

    public void playGame(int nodeCount) {
        states.clear();
        var state = origState.clone(false);
        if (state.actionCtx == GameActionCtx.START_GAME) {
            state.doAction(0);
        } else if (startingAction >= 0) {
            state.doAction(startingAction);
        }

        state.doEval(mcts.model);
        while (state.isTerminal() == 0) {
            int upto = nodeCount - state.total_n;
            for (int i = 0; i < upto; i++) {
                mcts.search(state, false, upto - i);
                if (mcts.numberOfPossibleActions == 1) {
                    break;
                }
            }

            int action = MCTS.getActionWithMaxNodesOrTerminal(state);
            states.add(new GameStep(state, action));
            state = getNextState(state, action);
            states.get(states.size() - 1).state().clearNextStates();
        }
        states.add(new GameStep(state, -1));

        deathCount += state.isTerminal() == -1 ? 1 : 0;
        int damageTaken = state.player.origHealth - state.player.health;
        damageCount.put(damageTaken, damageCount.getOrDefault(damageTaken, 0) + 1);
        totalDamageTaken += damageTaken;
        game_i += 1;
        if (matchLogWriter != null) {
            try {
                matchLogWriter.write("*** Match " + game_i + " ***\n");
                matchLogWriter.write("Result: " + (state.isTerminal() == 1 ? "Win" : "Loss") + "\n");
                matchLogWriter.write("Damage Taken: " + damageTaken + "\n");
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

    public void printProgress(long start_time, int matchCount) {
        System.out.println("Progress: " + game_i + "/" + matchCount);;
        System.out.println("Deaths: " + deathCount);
        System.out.println("Avg Damage: " + ((double) totalDamageTaken) / game_i);
        System.out.println("Avg Damage (Not Including Deaths): " + ((double) (totalDamageTaken - origState.player.origHealth * deathCount)) / (game_i - deathCount));
        System.out.println("Time Taken: " + (System.currentTimeMillis() - start_time));
        System.out.println("Time Taken (By Model): " + mcts.model.time_taken);
        System.out.println("Model: cache_size=" + mcts.model.cache.size() + ", " + mcts.model.cache_hits + "/" + mcts.model.calls + " hits (" + (double) mcts.model.cache_hits / mcts.model.calls + ")");
        System.out.println("--------------------");
    }

    private GameState getNextState(GameState state, int action) {
        State nextState = state.ns[action];
        GameState newState;
        if (nextState instanceof ChanceState cState) {
            newState = cState.getNextState(state, action);
            if (newState.policy == null) {
                newState.doEval(mcts.model);
            }
        } else {
            newState = (GameState) nextState;
        }
        return newState;
    }

    public void playTrainingGame(int nodeCount, boolean curriculumTraining) {
        states.clear();
        var state = origState.clone(false);
        if (state.actionCtx == GameActionCtx.START_GAME) {
            state.doAction(0);
        }
        Random random = state.prop.random;
        for (Enemy enemy : state.enemies) {
            if (enemy.health > 0) {
                enemy.randomize(random, curriculumTraining);
            }
        }

        state.doEval(mcts.model);
        while (state.isTerminal() == 0) {
            for (int i = 0; i < nodeCount; i++) {
                mcts.search(state, true, -1);
            }

            int action;
            if (state.turnNum >= 0) {
                action = MCTS.getActionWithMaxNodesOrTerminal(state);
            } else {
                action = MCTS.getActionRandomOrTerminal(state);
            }
            states.add(new GameStep(state, action));
            state = getNextState(state, action).clone(false);
            states.get(states.size() - 1).state().clearNextStates();
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
