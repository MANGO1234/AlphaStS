package com.alphaStS;

import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

final class GameStep {
    private final GameState state;
    private final int action;
    public boolean useForTraining;
    public float v_win;
    public float v_health;

    GameStep(GameState state, int action) {
        this.state = state;
        this.action = action;
    }

    public GameState state() {
        return state;
    }

    public int action() {
        return action;
    }

    @Override public String toString() {
        return "GameStep{" +
                "state=" + state +
                ", action=" + action +
                '}';
    }
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
            state = getNextState(state, mcts, action, false);
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
                System.out.println("Deaths: " + deathCount + "/" + numOfGames + " (" + String.format("%.2f", 100 * deathCount / (float) game_i).trim() + "%)");
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
                System.out.println("--------------------");
            }
        }
    }

    boolean SLOW_TRAINING_WINDOW;
    boolean POLICY_CAP_ON;

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
        boolean quickPass = POLICY_CAP_ON && state.prop.random.nextInt(4) > 0;
        while (state.isTerminal() == 0) {
            int todo = (quickPass ? nodeCount / 4 : nodeCount) - state.total_n;
            for (int i = 0; i < todo; i++) {
                mcts.search(state, !quickPass, todo - i);
                if (mcts.numberOfPossibleActions == 1 && state.total_n >= 1) {
                    if (!state.isStochastic && states.size() > 0) {
                        state = getNextState(states.get(states.size() - 1).state(), mcts, states.get(states.size() - 1).action(), false);
                    }
                    break;
                }
            }

            int action;
            if (state.turnNum >= 0) {
                action = MCTS.getActionWithMaxNodesOrTerminal(state);
            } else {
                action = MCTS.getActionRandomOrTerminal(state);
            }
            var step = new GameStep(state, action) ;
            step.useForTraining = !quickPass;
            states.add(step);
            quickPass = POLICY_CAP_ON && state.prop.random.nextInt(4) > 0;
            state = getNextState(state, mcts, action, !quickPass);
        }
        states.add(new GameStep(state, -1));

        // do scoring here before clearing states to reuse nn eval if possible
        float v_win = state.isTerminal() == 1 ? 1.0f : 0.0f;
        float v_health = (float) (((double) state.player.health) / state.player.maxHealth);
        for (int i = states.size() - 1; i >= 0; i--) {
            states.get(i).v_win = v_win;
            states.get(i).v_health = v_health;
            state = states.get(i).state();
            state.clearNextStates();
            if (!SLOW_TRAINING_WINDOW && state.isStochastic && i > 0) {
                var prevState = states.get(i - 1).state();
                var prevAction = states.get(i - 1).action();
                var cState = (ChanceState) prevState.ns[prevAction];
                var stateActual = cState.addGeneratedState(state);
                while (cState.total_real_n < 1000 && cState.cache.size() < 100) {
                    cState.getNextState();
                }
                double est_v_win = 0;
                double est_v_health = 0;
                double[] out = new double[3];
                for (ChanceState.Node node : cState.cache.values()) {
                    if (node.state != stateActual) {
                        if (node.state.policy == null) {
                            node.state.doEval(mcts.model);
                        }
                        node.state.get_v(out);
                        est_v_win += out[0] * node.n;
                        est_v_health += out[1] * node.n;
                    }
                }
                est_v_win /= cState.total_real_n;
                est_v_health /= cState.total_real_n;
                float p = ((float) cState.getCount(stateActual)) / cState.total_real_n;
                if (v_win * p + (float) est_v_win > 1.0001) {
                    Integer.parseInt(null);
                }
                v_win = Math.min(v_win * p + (float) est_v_win, 1);
                v_health = Math.min(v_health * p + (float) est_v_health, 1);

                //  var prevSize = cState.cache.size();
                //  for (int j = 1; j < 900; j++) {
                //      cState.getNextState(prevState, prevAction);
                //  }
                //  est_v_win = 0;
                //  est_v_health = 0;
                //  for (ChanceState.Node node : cState.cache.values()) {
                //      if (node.state != state) {
                //          node.state.doEval(session.mcts.get(_ii).model);
                //          node.state.get_v(out);
                //          est_v_win += out[0] * node.n;
                //          est_v_health += out[1] * node.n;
                //      }
                //  }
                //  est_v_win /= cState.total_n;
                //  est_v_health /= cState.total_n;
                //  p = ((float) cState.getCount(state)) / cState.total_n;
                //  var v_win2 = Math.min(v_win * p + (float) est_v_win, 1);
                //  var v_health2 = Math.min(v_health * p + (float) est_v_health, 1);
                //  if ((v_win - v_win2) > 0.02 || (v_health - v_health2) > 0.0) {
                //      System.out.println((v_win - v_win2) + "," + (v_health - v_health2) + "," + cState.cache.size() + "," + prevSize);
                //  }
            }
        }

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
            if (trainingDataWriter != null && numOfGames <= 200) {
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

    private GameState getNextState(GameState state, MCTS mcts, int action, boolean clone) {
        State nextState = state.ns[action];
        GameState newState;
        if (nextState instanceof ChanceState cState) {
            newState = cState.getNextState();
            if (newState.policy == null) {
                newState.doEval(mcts.model);
            }
            if (clone) {
                boolean isStochastic = newState.isStochastic;
                newState = newState.clone(false);
                newState.isStochastic = isStochastic;
            }
        } else {
            newState = (GameState) nextState;
            if (clone) {
                newState = newState.clone(false);
            }
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
