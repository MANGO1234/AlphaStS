package com.alphaStS;

import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

record GameStep(GameState state, int action) {
}

public class MatchSession {
    Writer matchLogWriter;
    Writer trainingDataWriter;
    int startingAction = -1;
    String logDir;
    List<MCTS> mcts = new ArrayList<>();

    public MatchSession(int numberOfThreads, String dir) {
        logDir = dir;
        for (int i = 0; i < numberOfThreads; i++) {
            Model model = new Model(dir);
            var m = new MCTS();
            m.setModel(model);
            mcts.add(m);
        }
    }

    public List<GameStep> playGame(GameState origState, MCTS mcts, int nodeCount) {
        var states = new ArrayList<GameStep>();
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
            state = getNextState(state, mcts, action);
            states.get(states.size() - 1).state().clearNextStates();
        }
        states.add(new GameStep(state, -1));
        return states;
    }

    public void playGames(GameState origState, int numOfGames, int nodeCount, boolean printProgress) {
        var deq = new LinkedBlockingDeque<List<GameStep>>();
        var session = this;
        var numToPlay = new AtomicInteger(numOfGames);
        for (int i = 0; i < mcts.size(); i++) {
            int ii = i;
            new Thread(() -> {
                var state = origState.clone(false);
                while (numToPlay.getAndDecrement() > 0) {
                    try {
                        deq.putLast(session.playGame(state, mcts.get(ii), nodeCount));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        var game_i = 0;
        var deathCount = 0;
        var totalDamageTaken = 0;
        var damageCount = new HashMap<Integer, Integer>();
        var start = System.currentTimeMillis();
        while (game_i < numOfGames) {
            List<GameStep> steps;
            try {
                steps = deq.takeFirst();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            var state = steps.get(steps.size() - 1).state();
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
                    for (GameStep step : steps) {
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
            if ((printProgress && game_i % 25 == 0) || game_i == numOfGames) {
                System.out.println("Progress: " + game_i + "/" + numOfGames);
                System.out.println("Deaths: " + deathCount + "/" + numOfGames + " (" + String.format("%.2f", 100 * deathCount / (float) numOfGames).trim() + "%)");
                System.out.println("Avg Damage: " + ((double) totalDamageTaken) / game_i);
                System.out.println("Avg Damage (Not Including Deaths): " + ((double) (totalDamageTaken - state.player.origHealth * deathCount)) / (game_i - deathCount));
                System.out.println("Time Taken: " + (System.currentTimeMillis() - start));
                for (int i = 0; i < mcts.size(); i++) {
                    var m = mcts.get(i);
                    System.out.println("Time Taken (By Model " + i + "): " + m.model.time_taken);
                    System.out.println("Model " + i + ": cache_size=" + m.model.cache.size() + ", " + m.model.cache_hits + "/" + m.model.calls + " hits (" + (double) m.model.cache_hits / m.model.calls + ")");
                }
//                if (game_i == numOfGames && printProgress) {
//                    System.out.println(damageCount.entrySet().stream().sorted(Map.Entry.comparingByKey()).map((e) -> e.getKey() + ": " + e.getValue()).reduce("", (acc, x) -> acc + "\n" + x));
//                }
                System.out.println("--------------------");            }
        }
    }

    private List<GameStep> playTrainingGame(GameState origState, int nodeCount, MCTS mcts, boolean curriculumTraining) {
        var states = new ArrayList<GameStep>();
        var state = origState.clone(false);
        if (state.actionCtx == GameActionCtx.START_GAME) {
            state.doAction(0);
        }
        RandomGen random = state.prop.random;
        for (Enemy enemy : state.enemies) {
            if (enemy.health > 0) {
                enemy.randomize(random, curriculumTraining);
            }
        }

        state.doEval(mcts.model);
        while (state.isTerminal() == 0) {
            for (int i = 0; i < nodeCount; i++) {
                mcts.search(state, true, -1);
                if (mcts.numberOfPossibleActions == 1) {
                    break;
                }
            }

            int action;
            if (state.turnNum >= 0) {
                action = MCTS.getActionWithMaxNodesOrTerminal(state);
            } else {
                action = MCTS.getActionRandomOrTerminal(state);
            }
            states.add(new GameStep(state, action));
            state = getNextState(state, mcts, action).clone(false);
            states.get(states.size() - 1).state().clearNextStates();
        }
        states.add(new GameStep(state, -1));
        return states;
    }

    public List<List<GameStep>> playTrainingGames(GameState origState, int numOfGames, int nodeCount, boolean curriculumTraining) {
        var deq = new LinkedBlockingDeque<List<GameStep>>();
        var session = this;
        var numToPlay = new AtomicInteger(numOfGames);
        for (int i = 0; i < mcts.size(); i++) {
            int ii = i;
            new Thread(() -> {
                var state = origState.clone(false);
                while (numToPlay.getAndDecrement() > 0) {
                    try {
                        deq.putLast(session.playTrainingGame(state, nodeCount, mcts.get(ii), curriculumTraining));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        var trainingGame_i = 0;
        var result = new ArrayList<List<GameStep>>();
        while (trainingGame_i < numOfGames) {
            List<GameStep> steps;
            try {
                steps = deq.takeFirst();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            result.add(steps);
            var state = steps.get(steps.size() - 1).state();
            trainingGame_i += 1;
            if (trainingDataWriter != null) {
                try {
                    trainingDataWriter.write("*** Match " + trainingGame_i + " ***\n");
                    trainingDataWriter.write("Result: " + (state.isTerminal() == 1 ? "Win" : "Loss") + "\n");
                    trainingDataWriter.write("Damage Taken: " + (origState.player.origHealth - state.player.health) + "\n");
                    for (GameStep step : steps) {
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
        return result;
    }

    private GameState getNextState(GameState state, MCTS mcts, int action) {
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
