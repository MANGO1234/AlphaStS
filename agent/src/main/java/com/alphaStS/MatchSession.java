package com.alphaStS;

import com.alphaStS.enemy.Enemy;

import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.sqrt;

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
    public boolean training;
    Writer matchLogWriter;
    Writer trainingDataWriter;
    int startingAction = -1;
    String logDir;
    List<MCTS> mcts = new ArrayList<>();
    public GameSolver solver;

    public MatchSession(int numberOfThreads, String dir) {
        logDir = dir;
        for (int i = 0; i < numberOfThreads; i++) {
            Model model = new Model(dir);
            var m = new MCTS();
            m.setModel(model);
            mcts.add(m);
        }
    }

    public Game playGame(GameState origState, MCTS mcts, int nodeCount, int r) {
        var useNewSearch = true;
        var states = new ArrayList<GameStep>();
        var state = origState.clone(false);
        if (state.actionCtx == GameActionCtx.START_GAME) {
            if (state.prop.randomization != null) {
                if (r >= 0) {
                    state.prop.randomization.randomize(state, r);
                } else {
                    r = state.prop.randomization.randomize(state);
                }
            } else {
                r = 0;
            }
            state.doAction(0);
        } else if (startingAction >= 0) {
            state.doAction(startingAction);
        }

        if (useNewSearch) {
            while (state.isTerminal() == 0) {
                int upto = nodeCount - (state.total_n + (state.policy == null ? 0 : 1));
                for (int i = 0; i < upto; i++) {
                    mcts.searchLine(state, false, true, upto - i);
                    if (mcts.numberOfPossibleActions == 1) {
                        break;
                    }
                }
                for (int action : state.searchFrontier.getBestLine().getActions()) {
                    states.add(new GameStep(state, action));
                    if (!training && solver != null) {
                        solver.checkForError(state, action, true);
                    }
                    if (nodeCount == 1) {
                        state = state.clone(false);
                        state.doAction(action);
                    } else {
                        state = getNextState(state, mcts, action, false);
                    }
                    states.get(states.size() - 1).state().clearNextStates();
                }
                state.clearAllSearchInfo();
            }
        } else{
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
                if (!training && solver != null) {
                    solver.checkForError(state, action, true);
                }
                state = getNextState(state, mcts, action, false);
                states.get(states.size() - 1).state().clearNextStates();
            }
        }
        states.add(new GameStep(state, -1));
        return new Game(states, r);
    }

    public static record Game(List<GameStep> steps, int r) {};

    public void playGames(GameState origState, int numOfGames, int nodeCount, int r, boolean printProgress) {
        var deq = new LinkedBlockingDeque<Game>();
        var session = this;
        var numToPlay = new AtomicInteger(numOfGames);
        for (int i = 0; i < mcts.size(); i++) {
            int ii = i;
            new Thread(() -> {
                var state = origState.clone(false);
                while (numToPlay.getAndDecrement() > 0) {
                    try {
                        deq.putLast(session.playGame(state, mcts.get(ii), nodeCount, r));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        var game_i = 0;
        var deathCount = new HashMap<Integer, Integer>();
        var totalDamageTaken = new HashMap<Integer, Integer>();
        var totalDamageTakenNoDeath = new HashMap<Integer, Integer>();
        var numOfGamesByR = new HashMap<Integer, Integer>();
        var damageCount = new HashMap<Integer, Integer>();
        var start = System.currentTimeMillis();
        var solverErrorCount = 0;
        var progressInterval = ((int) Math.ceil(numOfGames / 1000f)) * 25;
        while (game_i < numOfGames) {
            Game game;
            List<GameStep> steps;
            try {
                game = deq.takeFirst();
                steps = game.steps;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            var state = steps.get(steps.size() - 1).state();
            if (!training && solver != null) {
                solverErrorCount += solver.checkForError(game);
            }
            int damageTaken = state.getPlayeForRead().getOrigHealth() - state.getPlayeForRead().getHealth();
            deathCount.putIfAbsent(game.r, 0);
            deathCount.computeIfPresent(game.r, (k, x) -> x + (state.isTerminal() == -1 ? 1 : 0));
            numOfGamesByR.putIfAbsent(game.r, 0);
            numOfGamesByR.computeIfPresent(game.r, (k, x) -> x + 1);
            totalDamageTaken.putIfAbsent(game.r, 0);
            totalDamageTaken.computeIfPresent(game.r, (k, x) -> x + damageTaken);
            totalDamageTakenNoDeath.putIfAbsent(game.r, 0);
            totalDamageTakenNoDeath.computeIfPresent(game.r, (k, x) -> x + (state.isTerminal() == 1 ? damageTaken : 0));
            damageCount.put(damageTaken, damageCount.getOrDefault(damageTaken, 0) + 1);
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
            if ((printProgress && game_i % progressInterval == 0) || game_i == numOfGames) {
                System.out.println("Progress: " + game_i + "/" + numOfGames);
                if (!training && solver != null) {
                    System.out.println("Error Count: " + solverErrorCount);
                }
                if (deathCount.size() == 1) {
                    var i = deathCount.keySet().stream().toList().get(0);
                    System.out.println("Deaths: " + deathCount.get(i) + "/" + numOfGamesByR.get(i) + " (" + String.format("%.2f", 100 * deathCount.get(i) / (float) numOfGamesByR.get(i)).trim() + "%)");
                    System.out.println("Avg Damage: " + ((double) totalDamageTaken.get(i)) / numOfGamesByR.get(i));
                    System.out.println("Avg Damage (Not Including Deaths): " + ((double) totalDamageTakenNoDeath.get(i)) / (numOfGamesByR.get(i) - deathCount.get(i)));
                } else {
                    for (var info : state.prop.randomization.listRandomizations(state).entrySet()) {
                        var i = info.getKey();
                        if (deathCount.get(i) == null) {
                            continue;
                        }
                        System.out.println("Scenario " + info.getKey() + ": " + info.getValue().desc());
                        System.out.println("    Deaths: " + deathCount.get(i) + "/" + numOfGamesByR.get(i) + " (" + String.format("%.2f", 100 * deathCount.get(i) / (float) numOfGamesByR.get(i)).trim() + "%)");
                        System.out.println("    Avg Damage: " + ((double) totalDamageTaken.get(i)) / numOfGamesByR.get(i));
                        System.out.println("    Avg Damage (Not Including Deaths): " + ((double) totalDamageTakenNoDeath.get(i)) / (numOfGamesByR.get(i) - deathCount.get(i)));
                    }
                }
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

    private List<GameStep> playTrainingGame2(GameState origState, int nodeCount, MCTS mcts, boolean curriculumTraining) {
        var states = new ArrayList<GameStep>();
        var state = origState.clone(false);
        if (state.prop.randomization != null) {
            state.prop.randomization.randomize(state);
        }
        if (state.actionCtx == GameActionCtx.START_GAME) {
            state.doAction(0);
        }
        RandomGen random = state.prop.random;
        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
            enemy.randomize(random, curriculumTraining);
        }

        while (state.isTerminal() == 0) {
            int todo = nodeCount - state.total_n;
            for (int i = 0; i < todo; i++) {
                mcts.searchLine(state, true, true, todo - i);
            }
            for (int action : state.searchFrontier.getBestLine().getActions()) {
                var step = new GameStep(state, action);
                step.useForTraining = step.state().actionCtx != GameActionCtx.BEGIN_TURN;
                states.add(step);
                if (nodeCount == 1) {
                    state = state.clone(false);
                    state.doAction(action);
                } else {
                    state = getNextState(state, mcts, action, false);
                }
            }

//            int action = state.searchFrontier.getBestLine().getActions().get(0);
//            var step = new GameStep(state, action);
//            step.useForTraining = true;
//            states.add(step);
//            state = getNextState(state, mcts, action, true);
        }
        states.add(new GameStep(state, -1));

        // do scoring here before clearing states to reuse nn eval if possible
        float v_win = state.isTerminal() == 1 ? 1.0f : 0.0f;
        float v_health = (float) (((double) state.getPlayeForRead().getHealth()) / state.getPlayeForRead().getMaxHealth());
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
                while (cState.total_n < 1000 && cState.cache.size() < 100) {
                    cState.getNextState(false);
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
                est_v_win /= cState.total_node_n;
                est_v_health /= cState.total_node_n;
                float p = ((float) cState.getCount(stateActual)) / cState.total_node_n;
                if (v_win * p + (float) est_v_win > 1.0001) {
                    Integer.parseInt(null);
                }
                v_win = Math.min(v_win * p + (float) est_v_win, 1);
                v_health = Math.min(v_health * p + (float) est_v_health, 1);
            }
        }

        return states;
    }

    private List<GameStep> playTrainingGame(GameState origState, int nodeCount, MCTS mcts, boolean curriculumTraining) {
        var states = new ArrayList<GameStep>();
        var state = origState.clone(false);
        if (state.prop.randomization != null) {
            state.prop.randomization.randomize(state);
        }
        if (state.actionCtx == GameActionCtx.START_GAME) {
            state.doAction(0);
        }
        RandomGen random = state.prop.random;
        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
            enemy.randomize(random, curriculumTraining);
        }

        state.doEval(mcts.model);
        boolean quickPass = POLICY_CAP_ON && state.prop.random.nextInt(4) > 0;
        while (state.isTerminal() == 0) {
            int todo = (quickPass ? nodeCount / 4 : nodeCount) - state.total_n;
            for (int i = 0; i < todo; i++) {
                mcts.search(state, !quickPass, todo - i);
                if (mcts.numberOfPossibleActions == 1 && state.total_n >= 1) {
//                    if (state.isStochastic && states.size() > 0) {
//                        state = getNextState(states.get(states.size() - 1).state(), mcts, states.get(states.size() - 1).action(), false);
//                    }
                    break;
                }
            }

            int action;
            if (state.turnNum >= 0) {
                action = MCTS.getActionWithMaxNodesOrTerminal(state);
            } else {
                action = MCTS.getActionRandomOrTerminal(state);
            }
            var step = new GameStep(state, action);
            step.useForTraining = !quickPass;
            states.add(step);
            quickPass = POLICY_CAP_ON && state.prop.random.nextInt(4) > 0;
            state = getNextState(state, mcts, action, !quickPass);
        }
        states.add(new GameStep(state, -1));

        // do scoring here before clearing states to reuse nn eval if possible
        float v_win = state.isTerminal() == 1 ? 1.0f : 0.0f;
        float v_health = (float) (((double) state.getPlayeForRead().getHealth()) / state.getPlayeForRead().getMaxHealth());
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
                while (cState.total_n < 1000 && cState.cache.size() < 100) {
                    cState.getNextState(false);
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
                est_v_win /= cState.total_node_n;
                est_v_health /= cState.total_node_n;
                float p = ((float) cState.getCount(stateActual)) / cState.total_node_n;
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
                    trainingDataWriter.write("Damage Taken: " + (state.getPlayeForRead().getOrigHealth() - state.getPlayeForRead().getHealth()) + "\n");
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

           for (GameStep step : steps) {
               if (step.action() < 0 || step.state().terminal_action >= 0) {
                   break;
               }
               state = step.state();
               var max_i = -1;
               var max_n = 0;
               for (int i = 0; i < state.getLegalActions().length; i++) {
                   if (state.n[i] > max_n) {
                       max_n = state.n[i];
                       max_i = i;
                   }
               }
               if (max_i != step.action()) {
                   if (state.n[max_i] == state.n[step.action()]) {
                       max_i = step.action();
                   } else {
                       System.out.println(step.state());
                       System.out.println(step.state().getActionString(max_i));
                       System.out.println(step.state().getActionString(step.action()));
                       Integer.parseInt(null);
                   }
               }
               double q = state.q_comb[max_i] / state.n[max_i];
               double u = 0.1 * state.policyMod[max_i] * sqrt(state.total_n) / (1 + state.n[max_i]);
               var max_puct = q + u;
               var del_n = 0;
               for (int i = 0; i < state.getLegalActions().length; i++) {
                   if (i == max_i) {
                       continue;
                   }
                   q = state.q_comb[i] / state.n[i];
                   var new_n = (int) Math.ceil(0.1 * state.policyMod[i] * sqrt(state.total_n) / (max_puct - q) - 1);
                   if (new_n < 0 || state.n[i] == 1) {
                       continue;
                   }
                   var force_n = (int) Math.sqrt(0.5 * state.policyMod[i] * state.total_n);
                   new_n = Math.max(state.n[i] - force_n, new_n);
                   if (state.n[i] > new_n) {
//                        System.out.println(state);
//                        System.out.println(q);
//                        System.out.println(state.q_comb[max_i] / state.n[max_i]);
//                        System.out.println(0.1 * state.policyMod[i] * sqrt(state.total_n) / (1 + state.n[i]));
//                        System.out.println(0.1 * state.policyMod[max_i] * sqrt(state.total_n) / (1 + state.n[max_i]));
//                        System.out.println(q + 0.1 * state.policyMod[i] * sqrt(state.total_n) / (1 + state.n[i]));
//                        System.out.println(q + 0.1 * state.policyMod[i] * sqrt(state.total_n) / (1 + state.n[i] - 1));
//                        System.out.println(q + 0.1 * state.policyMod[i] * sqrt(state.total_n) / (1 + state.n[i] - 2));
//                        System.out.println(q + 0.1 * state.policyMod[i] * sqrt(state.total_n) / (1 + new_n));
//                        System.out.println(max_puct);
//                        System.out.println("Prune " + state.n[i] + " to " + new_n + ":" + force_n);
                       del_n += state.n[i] - new_n;
                       state.n[i] = new_n;
//                        Integer.parseInt(null);
                   }
               }
               state.total_n -= del_n;
           }
        }
        return result;
    }

    private GameState getNextState(GameState state, MCTS mcts, int action, boolean clone) {
        State nextState = state.ns[action];
        GameState newState;
        if (nextState instanceof ChanceState cState) {
            newState = cState.getNextState(false);
            if (newState.policy == null) {
                newState.doEval(mcts.model);
            }
            if (clone) {
                boolean isStochastic = newState.isStochastic;
                newState = newState.clone(false);
                newState.isStochastic = isStochastic;
                newState.searchFrontier = null;
            }
        } else {
            newState = (GameState) nextState;
            if (clone) {
                newState = newState.clone(false);
                newState.searchFrontier = null;
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
