package com.alphaStS;

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
    public List<String> lines;
    public boolean isExplorationMove;

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
    private final static boolean LOG_GAME_USING_LINES_FORMAT = true;
    private final static boolean USE_NEW_SEARCH = true;

    public boolean training;
    public Model compareModel;
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

    public Game playGame(GameState origState, MCTS mcts, int nodeCount) {
        var states = new ArrayList<GameStep>();
        var state = origState.clone(false);
        int r = 0;
        int preBattle_r = 0;
        if (state.prop.preBattleRandomization != null) {
            preBattle_r = state.prop.preBattleRandomization.randomize(state);
        }

        if (USE_NEW_SEARCH) {
            while (state.isTerminal() == 0) {
                int upto = nodeCount - (state.total_n + (state.policy == null ? 0 : 1));
                for (int i = 0; i < upto; i++) {
                    mcts.searchLine(state, false, true, upto - i);
                    if (mcts.numberOfPossibleActions == 1) {
                        break;
                    }
                }

                if (compareModel != null) {
                    var compareState = state.clone(false);
                    for (int i = 0; i < nodeCount; i++) {
                        var oldModel = mcts.model;
                        mcts.model = compareModel;
                        mcts.searchLine(compareState, false, true, nodeCount - i);
                        mcts.model = oldModel;
                        if (mcts.numberOfPossibleActions == 1) {
                            break;
                        }
                    }
                    if (!state.searchFrontier.isOneOfBestLine(compareState.searchFrontier.getBestLine())) {
                        System.out.println("---------------------------------------------------------");
                        System.out.println(state);
                        System.out.println(state.searchFrontier.getSortedLinesAsStrings(state).get(0));
                        System.out.println(compareState.searchFrontier.getSortedLinesAsStrings(compareState).get(0));
                    }
                }

                for (int action : state.searchFrontier.getBestLine().getActions(state)) {
                    states.add(new GameStep(state, action));
                    if (state.searchFrontier != null) {
                        states.get(states.size() - 1).lines = state.searchFrontier.getSortedLinesAsStrings(state);
                    }
                    if (!training && solver != null) {
                        solver.checkForError(state, action, true);
                    }
                    if (state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
                        state = state.clone(false);
                        r = state.doAction(0);
                    } else {
                        if (nodeCount == 1) {
                            state = state.clone(false);
                            state.doAction(action);
                        } else {
                            state = getNextState(state, mcts, action, false);
                        }
                    }
                    states.get(states.size() - 1).state().clearNextStates();
                }
                state.clearAllSearchInfo();
            }
        } else {
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
                if (state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
                    state = state.clone(false);
                    r = state.doAction(0);
                } else {
                    state = getNextState(state, mcts, action, false);
                }
                states.get(states.size() - 1).state().clearNextStates();
            }
        }
        states.add(new GameStep(state, -1));
        return new Game(states, preBattle_r, r, true);
    }

    public static record Game(List<GameStep> steps, int preBattle_r, int battle_r, boolean noExploration) {}

    public void playGames(GameState origState, int numOfGames, int nodeCount, boolean printProgress) {
        var deq = new LinkedBlockingDeque<Game>();
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
        var combinedInfoMap = new HashMap<Integer, GameStateRandomization.Info>();
        Map<Integer, GameStateRandomization.Info> preBattleInfoMap;
        if (origState.prop.preBattleRandomization != null) {
            preBattleInfoMap = origState.prop.preBattleRandomization.listRandomizations();
        } else {
            preBattleInfoMap = new HashMap<Integer, GameStateRandomization.Info>();
            preBattleInfoMap.put(0, new GameStateRandomization.Info(1, ""));
        }
        Map<Integer, GameStateRandomization.Info> battleInfoMap;
        if (origState.prop.randomization != null) {
            battleInfoMap = origState.prop.randomization.listRandomizations();
        } else {
            battleInfoMap = new HashMap<Integer, GameStateRandomization.Info>();
            battleInfoMap.put(0, new GameStateRandomization.Info(1, ""));
        }
        for (int i = 0; i < preBattleInfoMap.size(); i++) {
            for (int j = 0; j < battleInfoMap.size(); j++) {
                var chance = preBattleInfoMap.get(i).chance() * battleInfoMap.get(j).chance();
                String desc;
                if (preBattleInfoMap.get(i).desc().length() == 0) {
                    desc = battleInfoMap.get(j).desc();
                } else if (battleInfoMap.get(j).desc().length() == 0) {
                    desc = preBattleInfoMap.get(i).desc();
                } else {
                    desc = preBattleInfoMap.get(i).desc() + ", " + battleInfoMap.get(j).desc();
                }
                combinedInfoMap.put(i * battleInfoMap.size() + j, new GameStateRandomization.Info(chance, desc));
            }
        }
        var deathCount = new HashMap<Integer, Integer>();
        var totalDamageTaken = new HashMap<Integer, Integer>();
        var totalDamageTakenNoDeath = new HashMap<Integer, Integer>();
        var numOfGamesByR = new HashMap<Integer, Integer>();
        var damageCount = new HashMap<Integer, HashMap<Integer, Integer>>();
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
            var r = game.preBattle_r * battleInfoMap.size() + game.battle_r;
            if (!training && solver != null) {
                solverErrorCount += solver.checkForError(game);
            }
            int damageTaken = state.getPlayeForRead().getOrigHealth() - state.getPlayeForRead().getHealth();
            deathCount.putIfAbsent(r, 0);
            deathCount.computeIfPresent(r, (k, x) -> x + (state.isTerminal() == -1 ? 1 : 0));
            numOfGamesByR.putIfAbsent(r, 0);
            numOfGamesByR.computeIfPresent(r, (k, x) -> x + 1);
            totalDamageTaken.putIfAbsent(r, 0);
            totalDamageTaken.computeIfPresent(r, (k, x) -> x + damageTaken);
            totalDamageTakenNoDeath.putIfAbsent(r, 0);
            totalDamageTakenNoDeath.computeIfPresent(r, (k, x) -> x + (state.isTerminal() == 1 ? damageTaken : 0));
            damageCount.computeIfAbsent(r, (x) -> new HashMap<>());
            damageCount.compute(r, (k, v) -> { v.put(damageTaken, v.getOrDefault(damageTaken, 0) + 1); return v; });
            game_i += 1;
            if (matchLogWriter != null) {
                try {
                    matchLogWriter.write("*** Match " + game_i + " ***\n");
                    if (origState.prop.randomization != null) {
                        if (combinedInfoMap.size() > 1) {
                            matchLogWriter.write("Scenario: " + combinedInfoMap.get(r).desc() + "\n");
                        }
                    }
                    matchLogWriter.write("Result: " + (state.isTerminal() == 1 ? "Win" : "Loss") + "\n");
                    matchLogWriter.write("Damage Taken: " + damageTaken + "\n");
                    boolean usingLine = steps.stream().anyMatch((s) -> s.lines != null);
                    if (usingLine && LOG_GAME_USING_LINES_FORMAT) {
                        for (GameStep step : steps) {
                            if (step.state().actionCtx == GameActionCtx.BEGIN_TURN) continue;
                            if (step.lines != null) {
                                matchLogWriter.write(step.state().toStringReadable() + "\n");
                                for (int i = 0; i < Math.min(step.lines.size(), 5); i++) {
                                    matchLogWriter.write("  " + (i + 1) + ". " + step.lines.get(i) + "\n");
                                }
                            }
                        }
                    } else {
                        for (GameStep step : steps) {
                            if (step.state().actionCtx == GameActionCtx.BEGIN_TURN) continue;
                            matchLogWriter.write(step.state().toStringReadable() + "\n");
                            if (step.action() >= 0) {
                                matchLogWriter.write("action=" + step.state().getActionString(step.action()) + "\n");
                            }
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
                    System.out.println("Avg Damage: " + String.format("%.2f", ((double) totalDamageTaken.get(i)) / numOfGamesByR.get(i)));
                    System.out.println("Avg Damage (Not Including Deaths): " + String.format("%.2f", ((double) totalDamageTakenNoDeath.get(i)) / (numOfGamesByR.get(i) - deathCount.get(i))));
                } else {
                    for (var info : state.prop.randomization.listRandomizations().entrySet()) {
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
                if (game_i == numOfGames && printProgress) {
                    if (state.prop.randomization != null) {
                        for (var info : state.prop.randomization.listRandomizations().entrySet()) {
                            if (damageCount.get(info.getKey()) == null) {
                                continue;
                            }
                            System.out.println("Scenario " + info.getKey() + ": " + info.getValue().desc());
                            System.out.println(String.join("\n", damageCount.get(info.getKey()).entrySet().stream().sorted(Map.Entry.comparingByKey()).map((e) -> e.getKey() + ": " + e.getValue()).toList()));
                        }
                    }
                }
                System.out.println("--------------------");
            }
        }
    }


    boolean SLOW_TRAINING_WINDOW;
    boolean POLICY_CAP_ON;
    boolean TRAINING_WITH_LINE;

    private float[] calcExpectedValue(ChanceState cState, GameState generatedState, MCTS mcts, float v_win, float v_health, float v_comb) {
        var stateActual = generatedState == null ? null : cState.addGeneratedState(generatedState);
        while (cState.total_n < 1000 && cState.cache.size() < 100) {
            cState.getNextState(false);
        }
        double est_v_win = 0;
        double est_v_health = 0;
        double est_v_comb = 0;
        double[] out = new double[3];
        for (ChanceState.Node node : cState.cache.values()) {
            if (node.state != stateActual) {
                if (node.state.policy == null) {
                    node.state.doEval(mcts.model);
                }
                node.state.get_v(out);
                est_v_win += out[0] * node.n;
                est_v_health += out[1] * node.n;
                est_v_comb += out[2] * node.n;
            }
        }
        est_v_win /= cState.total_node_n;
        est_v_health /= cState.total_node_n;
        est_v_comb /= cState.total_node_n;
        float p = generatedState == null ? 0 : ((float) cState.getCount(stateActual)) / cState.total_node_n;
        if (v_win * p + (float) est_v_win > 1.0001) {
            Integer.parseInt(null);
        }
        v_win = Math.min(v_win * p + (float) est_v_win, 1);
        v_health = Math.min(v_health * p + (float) est_v_health, 1);
        v_comb = Math.min(v_comb * p + (float) est_v_comb, 1);

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
        return new float[] {v_win, v_health, v_comb};
    }

    private Game playTrainingGame(GameState origState, int nodeCount, MCTS mcts) {
        var steps = new ArrayList<GameStep>();
        var state = origState.clone(false);
        boolean doNotExplore = state.prop.random.nextFloat() < 0.2;
        int r = 0;
        int preBattle_r = 0;
        if (state.prop.preBattleRandomization != null) {
            preBattle_r = state.prop.preBattleRandomization.randomize(state);
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
            int greedyAction;
            if (doNotExplore || state.turnNum >= 100) {
                action = MCTS.getActionWithMaxNodesOrTerminal(state);
                greedyAction = action;
            } else {
                action = MCTS.getActionRandomOrTerminal(state);
                greedyAction = MCTS.getActionWithMaxNodesOrTerminal(state);
            }
            var step = new GameStep(state, action);
            step.useForTraining = !quickPass;
            step.isExplorationMove = greedyAction != action;
            steps.add(step);
            quickPass = POLICY_CAP_ON && state.prop.random.nextInt(4) > 0;
            if (state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
                state = state.clone(false);
                r = state.doAction(0);
            } else {
                state = getNextState(state, mcts, action, !quickPass);
            }
        }
        steps.add(new GameStep(state, -1));

        // do scoring here before clearing states to reuse nn eval if possible
        float v_win = state.isTerminal() == 1 ? 1.0f : 0.0f;
        float v_health = (float) (((double) state.getPlayeForRead().getHealth()) / state.getPlayeForRead().getMaxHealth());
        float v_comb = (float) state.calc_q(v_win, v_health);
        ChanceState lastChanceState = null;
        for (int i = steps.size() - 1; i >= 0; i--) {
            if (steps.get(i).isExplorationMove) {
                ChanceState cState = findBestLineChanceState(steps.get(i).state());
                // taking the max of 2 random variable inflates eval slightly, so we try to make calcExpectedValue have a large sample
                // to reduce this effect, seems ok so far, also check if we are transposing and skip for transposing
                if (cState != null && lastChanceState != null && !cState.equals(lastChanceState)) {
                    float[] ret = calcExpectedValue(cState, null, mcts, 0, 0, 0);
                    float v_win2 = ret[0];
                    float v_health2 = ret[1];
                    float v_comb2 = ret[2];
                    if (v_comb2 > v_comb) {
//                        if (v_comb2 < v_comb) {
//                            System.out.println(cState);
//                            System.out.println((float) (states.get(i).state().total_q_win / (states.get(i).state().total_n + 1)) + ", " + (float) (states.get(i).state().total_q_health / (states.get(i).state().total_n + 1)) + ", " + (float) (states.get(i).state().total_q_comb / (states.get(i).state().total_n + 1)));
//                            System.out.println(v_win2 + ", " + v_health2 + ", " + v_comb2);
//                            System.out.println(lastChanceState);
//                            System.out.println(cState.equals(lastChanceState));
//                            System.out.println(cState.parentState.equals(lastChanceState.parentState));
//                            System.out.println(v_win + ", " + v_health + ", " + v_comb);
//                            System.out.println("--------------------------");
//                        }
//                        v_win = (float) (states.get(i).state().total_q_win / (states.get(i).state().total_n + 1));
//                        v_health = (float) (states.get(i).state().total_q_health / (states.get(i).state().total_n + 1));
//                        v_comb = (float) (states.get(i).state().total_q_comb / (states.get(i).state().total_n + 1));
                        v_win = v_win2;
                        v_health = v_health2;
                        v_comb = v_comb2;
                        lastChanceState = cState;
                    }
                }
            }
            steps.get(i).v_win = v_win;
            steps.get(i).v_health = v_health;
            state = steps.get(i).state();
            state.clearNextStates();
            if (state.isStochastic && i > 0) {
                var prevState = steps.get(i - 1).state();
                var prevAction = steps.get(i - 1).action();
                var cState = (ChanceState) prevState.ns[prevAction];
                lastChanceState = cState;

                if (!SLOW_TRAINING_WINDOW) {
                    float[] ret = calcExpectedValue(cState, state, mcts, v_win, v_health, v_comb);
                    v_win = ret[0];
                    v_health = ret[1];
                    v_comb = ret[2];
                }
            }
        }
        if (steps.get(0).state().actionCtx == GameActionCtx.BEGIN_BATTLE) {
            steps.get(0).useForTraining = false;
        }

        return new Game(steps, preBattle_r, r, doNotExplore);
    }

    private ChanceState findBestLineChanceState(State state) {
        while (state instanceof GameState state2) {
            int action = MCTS.getActionWithMaxNodesOrTerminal(state2);
            state = state2.ns[action];
        }
        return (ChanceState) state;
    }

    private Game playTrainingGame2(GameState origState, int nodeCount, MCTS mcts) {
        var steps = new ArrayList<GameStep>();
        var state = origState.clone(false);
        var r = 0;
        int preBattle_r = 0;
        if (state.prop.preBattleRandomization != null) {
            preBattle_r = state.prop.preBattleRandomization.randomize(state);
        }

        while (state.isTerminal() == 0) {
            int todo = nodeCount - state.total_n;
            for (int i = 0; i < todo; i++) {
                mcts.searchLine(state, true, true, todo - i);
            }
            for (int action : state.searchFrontier.getBestLine().getActions(state)) {
                var step = new GameStep(state, action);
                step.useForTraining = step.state().actionCtx != GameActionCtx.BEGIN_TURN;
                steps.add(step);
                if (state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
                    state = state.clone(false);
                    r = state.doAction(0);
                } else {
                    if (nodeCount == 1) {
                        state = state.clone(false);
                        state.doAction(action);
                    } else {
                        state = getNextState(state, mcts, action, false);
                    }
                }
            }
            state = state.clone(true);

//            int action = state.searchFrontier.getBestLine().getActions(state).get(0);
//            var step = new GameStep(state, action);
//            step.useForTraining = step.state().actionCtx != GameActionCtx.BEGIN_TURN;
//            states.add(step);
//            state = getNextState(state, mcts, action, true);
        }
        steps.add(new GameStep(state, -1));

        // do scoring here before clearing states to reuse nn eval if possible
        float v_win = state.isTerminal() == 1 ? 1.0f : 0.0f;
        float v_health = (float) (((double) state.getPlayeForRead().getHealth()) / state.getPlayeForRead().getMaxHealth());
        float v_comb = (float) state.calc_q(v_win, v_health);
        for (int i = steps.size() - 1; i >= 0; i--) {
            steps.get(i).v_win = v_win;
            steps.get(i).v_health = v_health;
            state = steps.get(i).state();
            state.clearNextStates();
            if (!SLOW_TRAINING_WINDOW && state.isStochastic && i > 0) {
                var prevState = steps.get(i - 1).state();
                var prevAction = steps.get(i - 1).action();
                float[] ret = calcExpectedValue((ChanceState) prevState.ns[prevAction], state, mcts, v_win, v_health, v_comb);
                v_win = ret[0];
                v_health = ret[1];
                v_comb = ret[2];
            }
        }
        if (steps.get(0).state().actionCtx == GameActionCtx.BEGIN_BATTLE) {
            steps.get(0).useForTraining = false;
        }

        return new Game(steps, preBattle_r, r, true);
    }

    public List<List<GameStep>> playTrainingGames(GameState origState, int numOfGames, int nodeCount) {
        var deq = new LinkedBlockingDeque<Game>();
        var session = this;
        var numToPlay = new AtomicInteger(numOfGames);
        for (int i = 0; i < mcts.size(); i++) {
            int ii = i;
            new Thread(() -> {
                var state = origState.clone(false);
                while (numToPlay.getAndDecrement() > 0) {
                    try {
                        if (TRAINING_WITH_LINE) {
                            deq.putLast(session.playTrainingGame2(state, nodeCount, mcts.get(ii)));
                        } else {
                            deq.putLast(session.playTrainingGame(state, nodeCount, mcts.get(ii)));
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        }

        var trainingGame_i = 0;
        var result = new ArrayList<List<GameStep>>();
        while (trainingGame_i < numOfGames) {
            Game game;
            List<GameStep> steps;
            try {
                game = deq.takeFirst();
                steps = game.steps;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            result.add(steps);
            var state = steps.get(steps.size() - 1).state();
            trainingGame_i += 1;
            if (trainingDataWriter != null && numOfGames <= 300) {
                try {
                    trainingDataWriter.write("*** Match " + trainingGame_i + " ***\n");
                    if (origState.prop.preBattleRandomization != null) {
                        var info = origState.prop.preBattleRandomization.listRandomizations();
                        if (info.size() > 1) {
                            trainingDataWriter.write("Pre-Battle Randomization: " + info.get(game.preBattle_r).desc() + "\n");
                        }
                    }
                    if (origState.prop.randomization != null) {
                        var info = origState.prop.randomization.listRandomizations();
                        if (info.size() > 1) {
                            trainingDataWriter.write("Battle Randomization: " + info.get(game.battle_r).desc() + "\n");
                        }
                    }
                    if (!TRAINING_WITH_LINE && game.noExploration) {
                        trainingDataWriter.write("No Temperature Moves\n");
                    }
                    trainingDataWriter.write("Result: " + (state.isTerminal() == 1 ? "Win" : "Loss") + "\n");
                    trainingDataWriter.write("Damage Taken: " + (state.getPlayeForRead().getOrigHealth() - state.getPlayeForRead().getHealth()) + "\n");
                    for (GameStep step : steps) {
                        trainingDataWriter.write(step.state().toStringReadable() + "\n");
                        if (step.action() >= 0) {
                            trainingDataWriter.write("action=" + step.state().getActionString(step.action()) + "\n");
                        }
                    }
                    trainingDataWriter.write("\n");
                    trainingDataWriter.write("\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if (TRAINING_WITH_LINE) {
                continue;
            }
            for (GameStep step : steps) {
               if (step.action() < 0 || step.state().terminal_action >= 0) {
                   break;
               }
               if (!step.useForTraining) {
                   continue;
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
