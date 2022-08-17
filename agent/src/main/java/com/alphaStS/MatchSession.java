package com.alphaStS;

import com.alphaStS.utils.ScenarioStats;
import com.alphaStS.utils.Utils;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorOutputStream;

import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.sqrt;

public class MatchSession {
    private final static boolean LOG_GAME_USING_LINES_FORMAT = true;
    private final static boolean USE_NEW_SEARCH = false;

    public boolean training;
    public Model compareModel;
    Writer matchLogWriter;
    Writer trainingDataWriter;
    int startingAction = -1;
    String logDir;
    List<MCTS> mcts = new ArrayList<>();
    List<MCTS> mcts2 = new ArrayList<>();
    public int[][] scenariosGroup;
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

    public MatchSession(int numberOfThreads, String dir, String dir2) {
        logDir = dir;
        for (int i = 0; i < numberOfThreads; i++) {
            Model model = new Model(dir);
            var m = new MCTS();
            m.setModel(model);
            mcts.add(m);
        }
        for (int i = 0; i < numberOfThreads; i++) {
            Model model = new Model(dir2);
            var m = new MCTS();
            m.setModel(model);
            mcts2.add(m);
        }
    }

    public Game playGame(GameState origState, MCTS mcts, int nodeCount, boolean USE_NEW_SEARCH2) {
        var steps = new ArrayList<GameStep>();
        var state = origState.clone(false);
        int r = 0;
        int preBattle_r = 0;
        if (state.prop.realMoveRandomGen != null) {
            state.setSearchRandomGen(state.prop.realMoveRandomGen.createWithSeed(state.prop.realMoveRandomGen.nextLong(RandomGenCtx.Misc)));
        } else if (GameState.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION) {
            state.setSearchRandomGen(state.prop.random.createWithSeed(state.prop.random.nextLong(RandomGenCtx.CommonNumberVR)));
        } else {
            state.setSearchRandomGen(state.prop.random);
        }
        if (state.prop.preBattleRandomization != null) {
            state.prop.makingRealMove = true;
            preBattle_r = state.prop.preBattleRandomization.randomize(state);
            state.prop.makingRealMove = false;
        }
        if (startingAction >= 0) {
            state.prop.makingRealMove = true;
            state.doAction(startingAction);
            state.prop.makingRealMove = false;
        }
        state.prop.testNewFeature = USE_NEW_SEARCH2;

        if (true) {
            while (state.isTerminal() == 0) {
                int upto = nodeCount - (state.total_n + (state.policy == null ? 0 : 1));
                for (int i = 0; i < upto; i++) {
                    mcts.searchLine(state, false, true, upto - i);
                    if (mcts.numberOfPossibleActions == 1) {
                        break;
                    }
                }

                if (compareModel != null && false) {
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
                        System.out.println("Chosen: " + state.searchFrontier.getSortedLinesAsStrings(state).get(0));
                        System.out.println("Compared: " + compareState.searchFrontier.getSortedLinesAsStrings(compareState).get(0));
                    }
                }

                for (int action : state.searchFrontier.getBestLine().getActions(state)) {
                    steps.add(new GameStep(state, action));
                    if (state.searchFrontier != null) {
                        steps.get(steps.size() - 1).lines = state.searchFrontier.getSortedLinesAsStrings(state);
                    }
                    if (!training && solver != null) {
                        solver.checkForError(state, action, true);
                    }
                    if (state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
                        state = state.clone(false);
                        state.prop.makingRealMove = true;
                        r = state.doAction(0);
                        state.prop.makingRealMove = false;
                    } else {
                        if (nodeCount == 1) {
                            state = state.clone(false);
                            state.prop.makingRealMove = true;
                            state.doAction(action);
                            state.prop.makingRealMove = false;
                        } else {
                            state.prop.makingRealMove = true;
                            state = getNextState(state, mcts, action, false);
                            state.prop.makingRealMove = false;
                        }
                    }
                    steps.get(steps.size() - 1).state().clearNextStates();
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

                int action = MCTS.getActionWithMaxNodesOrTerminal(state, null);
                steps.add(new GameStep(state, action));
                if (!training && solver != null) {
                    solver.checkForError(state, action, true);
                }
                if (state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
                    state = state.clone(false);
                    state.prop.makingRealMove = true;
                    r = state.doAction(0);
                    state.prop.makingRealMove = false;
                } else {
                    if (nodeCount == 1) {
                        state = state.clone(false);
                        state.prop.makingRealMove = true;
                        state.doAction(action);
                        if (state.actionCtx == GameActionCtx.BEGIN_TURN) {
                            state.doAction(0);
                        }
                        state.prop.makingRealMove = false;
                    } else {
                        state.prop.makingRealMove = true;
                        state = getNextState(state, mcts, action, false);
                        state.prop.makingRealMove = false;
                    }
                }
                steps.get(steps.size() - 1).state().clearNextStates();
            }
        }
        steps.add(new GameStep(state, -1));
        for (int i = 1; i < steps.size(); i++) {
            if (steps.get(i).state().stateDesc != null) {
                steps.get(i - 1).actionDesc = steps.get(i).state().stateDesc;
            }
        }
        return new Game(steps, preBattle_r, r, true);
    }

    public static record Game(List<GameStep> steps, int preBattle_r, int battle_r, boolean noExploration) {}
    public static record GameResult(Game game, int modelCalls, Game game2, int modelCalls2, long seed) {}

    public void playGames(GameState origState, int numOfGames, int nodeCount, boolean printProgress) {
        var seeds = new ArrayList<Long>(numOfGames);
        for (int i = 0; i < numOfGames; i++) {
            seeds.add(origState.prop.random.nextLong(RandomGenCtx.Other));
        }
//        seeds.clear();
//        seeds.add(-3983415241759956846l);
//        numOfGames = 1;
        var deq = new LinkedBlockingDeque<GameResult>();
        var session = this;
        var numToPlay = new AtomicInteger(numOfGames);
        for (int i = 0; i < mcts.size(); i++) {
            int ii = i;
            new Thread(() -> {
                int idx = numToPlay.getAndDecrement();
                while (idx > 0) {
                    var state = origState.clone(false);
                    state.prop = state.prop.clone();
                    state.prop.realMoveRandomGen = new RandomGen.RandomGenByCtx(seeds.get(idx - 1));
                    var prev = mcts.get(ii).model.calls - mcts.get(ii).model.cache_hits;
                    var game1 = session.playGame(state, mcts.get(ii), nodeCount, true);
                    var modelCalls = mcts.get(ii).model.calls - mcts.get(ii).model.cache_hits - prev;
                    Game game2 = null;
                    var modelCalls2 = 0;
                    if (mcts2.size() > 0) {
                        var randomGen= state.prop.realMoveRandomGen;
                        state = origState.clone(false);
                        state.prop = state.prop.clone();
                        randomGen.timeTravelToBeginning();
                        state.prop.realMoveRandomGen = randomGen;
                        prev = mcts2.get(ii).model.calls - mcts2.get(ii).model.cache_hits;
                        game2 = session.playGame(state, mcts2.get(ii), nodeCount, false);
                        modelCalls2 = mcts2.get(ii).model.calls - mcts2.get(ii).model.cache_hits - prev;
                    }
                    try {
                        deq.putLast(new GameResult(game1, modelCalls, game2, modelCalls2, seeds.get(idx - 1)));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    idx = numToPlay.getAndDecrement();
                    com.alphaStS.utils.Utils.sleep(Configuration.SLEEP_PER_GAME);
                }
            }).start();
        }

        var game_i = 0;
        var combinedInfoMap = new HashMap<Integer, GameStateRandomization.Info>();
        Map<Integer, GameStateRandomization.Info> preBattleInfoMap;
        List<Integer> preBattleInfoMapKeys;
        if (origState.prop.preBattleRandomization != null) {
            preBattleInfoMap = origState.prop.preBattleRandomization.listRandomizations();
            preBattleInfoMapKeys = preBattleInfoMap.keySet().stream().sorted().toList();
        } else {
            preBattleInfoMap = new HashMap<>();
            preBattleInfoMap.put(0, new GameStateRandomization.Info(1, ""));
            preBattleInfoMapKeys = List.of(0);
        }
        Map<Integer, GameStateRandomization.Info> battleInfoMap;
        List<Integer> battleInfoMapKeys;
        if (origState.prop.randomization != null) {
            battleInfoMap = origState.prop.randomization.listRandomizations();
            battleInfoMapKeys = battleInfoMap.keySet().stream().sorted().toList();
        } else {
            battleInfoMap = new HashMap<>();
            battleInfoMap.put(0, new GameStateRandomization.Info(1, ""));
            battleInfoMapKeys = List.of(0);
        }
        for (int i = 0; i < preBattleInfoMap.size(); i++) {
            for (int j = 0; j < battleInfoMap.size(); j++) {
                int pr = preBattleInfoMapKeys.get(i);
                int br = battleInfoMapKeys.get(j);
                var chance = preBattleInfoMap.get(pr).chance() * battleInfoMap.get(br).chance();
                String desc;
                if (preBattleInfoMap.get(pr).desc().length() == 0) {
                    desc = battleInfoMap.get(br).desc();
                } else if (battleInfoMap.get(br).desc().length() == 0) {
                    desc = preBattleInfoMap.get(pr).desc();
                } else {
                    desc = preBattleInfoMap.get(pr).desc() + ", " + battleInfoMap.get(br).desc();
                }
                combinedInfoMap.put(pr * battleInfoMap.size() + br, new GameStateRandomization.Info(chance, desc));
            }
        }
        var scenarioStats = new HashMap<Integer, ScenarioStats>();
        var start = System.currentTimeMillis();
        var solverErrorCount = 0;
        var progressInterval = ((int) Math.ceil(numOfGames / 1000f)) * 25;
        while (game_i < numOfGames) {
            GameResult result;
            Game game;
            List<GameStep> steps;
            List<GameStep> steps2;
            try {
                result = deq.takeFirst();
                game = result.game;
                steps = game.steps;
                steps2 = result.game2 == null ? null : result.game2.steps;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            var state = steps.get(steps.size() - 1).state();
            var r = game.preBattle_r * battleInfoMap.size() + game.battle_r;
            if (!training && solver != null) {
                solverErrorCount += solver.checkForError(game);
            }
            if (Configuration.PRINT_MODEL_COMPARE_DIFF && steps2 != null) {
                var turns1 = GameStateUtils.groupByTurns(steps);
                var turns2 = GameStateUtils.groupByTurns(steps2);
                for (int i = 1; i < Math.min(turns1.size(), turns2.size()); i++) {
                    var t1 = turns1.get(i);
                    var t2 = turns2.get(i);
                    var ts1 = t1.get(t1.size() - 1).state().clone(false);
                    var ts2 = t2.get(t2.size() - 1).state().clone(false);
                    if (ts1.actionCtx != GameActionCtx.BEGIN_TURN) {
                        for (int j = 0; j < ts1.getLegalActions().length; j++) {
                            if (ts1.getAction(j).type() == GameActionType.END_TURN) {
                                ts1.doAction(j);
                                break;
                            }
                        }
                    }
                    if (ts2.actionCtx != GameActionCtx.BEGIN_TURN) {
                        for (int j = 0; j < ts2.getLegalActions().length; j++) {
                            if (ts2.getAction(j).type() == GameActionType.END_TURN) {
                                ts2.doAction(j);
                                break;
                            }
                        }
                    }
                    if (!ts1.equals(ts2)) {
                        System.out.println("******************************************** " + result.seed);
                        System.out.println(t1.get(0));
                        System.out.println(t2.get(0));
                        System.out.println(t1.stream().map(GameStep::getActionString).limit(t1.size() - 1).filter((x) -> !x.equals("End Turn"))
                                .collect(Collectors.joining(", ")));
                        System.out.println(t2.stream().map(GameStep::getActionString).limit(t2.size() - 1).filter((x) -> !x.equals("End Turn"))
                                .collect(Collectors.joining(", ")));
                        if (!t1.get(0).state().equals(t2.get(0).state())) {
                            System.out.println("!!!");
                        }
                        break;
                    }
                }
            }
            scenarioStats.computeIfAbsent(r, (k) -> new ScenarioStats()).add(game.steps, result.modelCalls, steps2, result.modelCalls2);
            game_i += 1;
            if (matchLogWriter != null) {
                int damageTaken = state.getPlayeForRead().getOrigHealth() - state.getPlayeForRead().getHealth();
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
                        printGame(matchLogWriter, steps);
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
                if (scenarioStats.size() > 1) {
                    for (var info : combinedInfoMap.entrySet()) {
                        var i = info.getKey();
                        if (scenarioStats.get(i) != null) {
                            System.out.println("Scenario " + info.getKey() + ": " + info.getValue().desc());
                            scenarioStats.get(i).printStats(state, 4);
                        }
                    }
                }
                if (scenariosGroup != null) {
                    for (int i = 0; i < scenariosGroup.length; i++) {
                        System.out.println("Scenario " + IntStream.of(scenariosGroup[i]).mapToObj(String::valueOf).collect(Collectors.joining(", ")) + ": " + ScenarioStats.getCommonString(combinedInfoMap, scenariosGroup[i]));
                        var group = IntStream.of(scenariosGroup[i]).mapToObj(scenarioStats::get).filter(Objects::nonNull).toArray(ScenarioStats[]::new);
                        ScenarioStats.combine(group).printStats(state, 4);
                    }
                }
                ScenarioStats.combine(scenarioStats.values().toArray(new ScenarioStats[0])).printStats(state, 0);
                System.out.println("Time Taken: " + (System.currentTimeMillis() - start));
                for (int i = 0; i < mcts.size(); i++) {
                    var m = mcts.get(i);
                    System.out.println("Time Taken (By Model " + i + "): " + m.model.time_taken);
                    System.out.println("Model " + i + ": cache_size=" + m.model.cache.size() + ", " + m.model.cache_hits + "/" + m.model.calls + " hits (" + (double) m.model.cache_hits / m.model.calls + ")");
                }
                System.out.println("--------------------");
            }
        }
    }


    boolean SLOW_TRAINING_WINDOW;
    boolean POLICY_CAP_ON;
    boolean TRAINING_WITH_LINE;

    private double[] calcExpectedValue(ChanceState cState, GameState generatedState, MCTS mcts, double[] vCur) {
        var stateActual = generatedState == null ? null : cState.addGeneratedState(generatedState);
        while (cState.total_n < 1000 && cState.cache.size() < 100) {
            cState.getNextState(false);
        }
        double[] est = new double[vCur.length];
        double[] out = new double[vCur.length];
        for (ChanceState.Node node : cState.cache.values()) {
            if (node.state != stateActual) {
                if (node.state.policy == null) {
                    node.state.doEval(mcts.model);
                }
                node.state.get_v(out);
                for (int i = 0; i < est.length; i++) {
                    est[i] += out[i] * node.n;
                }
            }
        }
        float p = generatedState == null ? 0 : ((float) cState.getCount(stateActual)) / cState.total_node_n;
        for (int i = 0; i < est.length; i++) {
            est[i] /= cState.total_node_n;
            est[i] = (float) Math.min(vCur[i] * p + est[i], 1);
        }

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
        return est;
    }

    private Game playTrainingGame(GameState origState, int nodeCount, MCTS mcts) {
        var steps = new ArrayList<GameStep>();
        var state = origState.clone(false);
        boolean doNotExplore = state.prop.random.nextFloat(RandomGenCtx.Other) < Configuration.TRAINING_PERCENTAGE_NO_TEMPERATURE;
        int r = 0;
        int preBattle_r = 0;
        if (state.prop.realMoveRandomGen != null) {
            state.setSearchRandomGen(state.prop.realMoveRandomGen.createWithSeed(state.prop.realMoveRandomGen.nextLong(RandomGenCtx.Misc)));
        } else if (GameState.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION) {
            state.setSearchRandomGen(state.prop.random.createWithSeed(state.prop.random.nextLong(RandomGenCtx.CommonNumberVR)));
        } else {
            state.setSearchRandomGen(state.prop.random);
        }
        if (state.prop.preBattleRandomization != null) {
            preBattle_r = state.prop.preBattleRandomization.randomize(state);
        }
        if (!doNotExplore) {
            int rr = state.prop.random.nextInt(3, RandomGenCtx.Other);
            if (rr == 0) {
                nodeCount /= 2;
            } else if (rr == 2) {
                nodeCount += nodeCount / 2;
            }
        }

        state.doEval(mcts.model);
        boolean quickPass = false;
        var bannedActions = new HashSet<GameAction>();
        while (state.isTerminal() == 0) {
            int todo = (quickPass ? nodeCount / 4 : nodeCount) - state.total_n;
//            if (state.actionCtx == GameActionCtx.SELECT_SCENARIO) {
//                todo = (quickPass ? nodeCount * 5 / 4 : nodeCount * 5) - state.total_n;
//            }
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
            if (state.actionCtx == GameActionCtx.SELECT_SCENARIO) {
                action = MCTS.getActionRandomOrTerminalSelectScenario(state);
                greedyAction = MCTS.getActionWithMaxNodesOrTerminal(state, null);
            } else if (doNotExplore || quickPass || state.turnNum >= 100) {
                action = MCTS.getActionWithMaxNodesOrTerminal(state, null);
                greedyAction = action;
            } else {
                action = MCTS.getActionRandomOrTerminal(state, bannedActions);
//                greedyAction = MCTS.getActionWithMaxNodesOrTerminal(state, bannedActions);
//                if (action != greedyAction) {
//                    if (state.prop.random.nextBoolean(RandomGenCtx.Other) && state.getAction(greedyAction).type() != GameActionType.END_TURN) {
//                        bannedActions.add(state.getAction(greedyAction));
//                    }
//                }
                greedyAction = MCTS.getActionWithMaxNodesOrTerminal(state, null);
            }
            var step = new GameStep(state, action);
            step.trainingWriteCount = !quickPass ? 1 : 0;
            step.isExplorationMove = greedyAction != action;
            steps.add(step);
            if (state.getAction(action).type() == GameActionType.END_TURN) {
                quickPass = POLICY_CAP_ON && state.prop.random.nextInt(4, RandomGenCtx.Other, null) > 0;
                bannedActions.clear();
            }
            if (state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
                state = state.clone(false);
                state.prop.makingRealMove = true;
                r = state.doAction(0);
                state.prop.makingRealMove = false;
            } else {
                state.prop.makingRealMove = true;
                state = getNextState(state, mcts, action, !quickPass);
                state.prop.makingRealMove = false;
            }
        }
        steps.add(new GameStep(state, -1));

        // do scoring here before clearing states to reuse nn eval if possible
        var vLen = state.get_v_len();
        double[] vCur = new double[vLen];
        state.get_v(vCur);
        ChanceState lastChanceState = null;
        for (int i = steps.size() - 2; i >= 0; i--) {
            if (steps.get(i).isExplorationMove) {
                ChanceState cState = findBestLineChanceState(steps.get(i).state());
                // taking the max of 2 random variable inflates eval slightly, so we try to make calcExpectedValue have a large sample
                // to reduce this effect, seems ok so far, also check if we are transposing and skip for transposing
                if (cState != null && lastChanceState != null && !cState.equals(lastChanceState)) {
                    double[] ret = calcExpectedValue(cState, null, mcts, new double[vLen]);
                    if (ret[GameState.V_COMB_IDX] > vCur[GameState.V_COMB_IDX]) {
                        vCur = ret;
                        lastChanceState = cState;
                    }
                }
            }
            steps.get(i).v = vCur;
            state = steps.get(i).state();
            state.clearNextStates();
            if (state.isStochastic && i > 0) {
                var prevState = steps.get(i - 1).state();
                var prevAction = steps.get(i - 1).action();
                var cState = (ChanceState) prevState.ns[prevAction];
                lastChanceState = cState;

                if (!SLOW_TRAINING_WINDOW) {
                    vCur = calcExpectedValue(cState, state, mcts, vCur);
                }
            }
        }
        if (steps.get(0).state().actionCtx == GameActionCtx.BEGIN_BATTLE) {
            steps.get(0).trainingWriteCount = 0;
        }

        return new Game(steps, preBattle_r, r, doNotExplore);
    }

    private double calcKld(GameState state) {
        if (state.n == null || state.policy == null) {
            throw new IllegalArgumentException();
        }
        if (state.terminal_action >= 0) {
            return 0;
        }
        var snapShot = new double[state.n.length];
        for (int j = 0; j < state.n.length; j++) {
            snapShot[j] = state.n[j] / (double) state.total_n;
        }
        var kld = 0.0;
        for (int j = 0; j < state.policy.length; j++) {
            if (snapShot[j] > 0) {
                kld += snapShot[j] * Math.log(snapShot[j] / state.policy[j]);
            }
        }
        return Math.abs(kld);
    }

    private ChanceState findBestLineChanceState(State state) {
        while (state instanceof GameState state2) {
            if (state2.isTerminal() != 0) {
                return null;
            }
            int action = MCTS.getActionWithMaxNodesOrTerminal(state2, null);
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
                step.trainingWriteCount = step.state().actionCtx != GameActionCtx.BEGIN_TURN ? 1 : 0;
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
        var vLen = state.get_v_len();
        double[] vCur = new double[vLen];
        state.get_v(vCur);
        for (int i = steps.size() - 1; i >= 0; i--) {
            steps.get(i).v = vCur;
            state = steps.get(i).state();
            state.clearNextStates();
            if (!SLOW_TRAINING_WINDOW && state.isStochastic && i > 0) {
                var prevState = steps.get(i - 1).state();
                var prevAction = steps.get(i - 1).action();
                vCur = calcExpectedValue((ChanceState) prevState.ns[prevAction], state, mcts, vCur);
            }
        }
        if (steps.get(0).state().actionCtx == GameActionCtx.BEGIN_BATTLE) {
            steps.get(0).trainingWriteCount = 0;
        }

        return new Game(steps, preBattle_r, r, true);
    }

    public void playTrainingGames(GameState origState, int numOfGames, int nodeCount, String path) throws IOException {
        File file = new File(path);
        file.delete();
        var stream = new DataOutputStream(new FramedLZ4CompressorOutputStream(new BufferedOutputStream(new FileOutputStream(path))));
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
                    Utils.sleep(Configuration.SLEEP_PER_GAME_TRAINING);
                }
            }).start();
        }

        var trainingGame_i = 0;
        while (trainingGame_i < numOfGames) {
            Game game;
            List<GameStep> steps;
            try {
                game = deq.takeFirst();
                steps = game.steps;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            var state = steps.get(steps.size() - 1).state();
            trainingGame_i += 1;
            if (trainingDataWriter != null && trainingGame_i <= 100) {
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
                    printGame(trainingDataWriter, steps);
                    trainingDataWriter.write("\n");
                    trainingDataWriter.write("\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            writeTrainingData(stream, steps);

            if (TRAINING_WITH_LINE) {
                continue;
            }
            for (GameStep step : steps) {
               if (step.action() < 0 || step.state().terminal_action >= 0) {
                   break;
               }
               if (step.trainingWriteCount <= 0) {
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
                if (calcKld(state) > 0.5) {
//                    step.trainingWriteCount = 2;
                }
            }
        }
        stream.flush();
        stream.close();
    }

    private static void writeTrainingData(DataOutputStream stream, List<GameStep> game) throws IOException {
        for (int i = game.size() - 2; i >= 0; i--) {
            var step = game.get(i);
            for (int write_count = 0; write_count < step.trainingWriteCount; write_count++) {
                var state = game.get(i).state();
                var x = state.getNNInput();
                for (int j = 0; j < x.length; j++) {
                    stream.writeFloat(x[j]);
                }
                for (int j = 1; j < step.v.length; j++) {
                    stream.writeFloat((float) ((step.v[j] * 2) - 1));
                }
                int idx = 0;
                if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                    for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length; action++) {
                        stream.writeFloat(-1);
                    }
                    if (state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null) {
                        for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()].length; action++) {
                            if (false) {
                                if (state.terminal_action == action) {
                                    stream.writeFloat(1);
                                } else {
                                    stream.writeFloat(0);
                                }
                            } else if (state.isActionLegal(action)) {
                                if (idx < state.getLegalActions().length && state.getLegalActions()[idx] == action) {
                                    stream.writeFloat((float) (((double) state.n[idx++]) / state.total_n));
                                } else {
                                    Integer.parseInt(null);
                                }
                            } else {
                                stream.writeFloat(-1);
                            }
                        }
                    }
                    if (state.prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()] != null) {
                        for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()].length; action++) {
                            stream.writeFloat(-1);
                        }
                    }
                } else if (state.actionCtx == GameActionCtx.SELECT_SCENARIO) {
                    for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length; action++) {
                        stream.writeFloat(-1);
                    }
                    if (state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null) {
                        for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()].length; action++) {
                            stream.writeFloat(-1);
                        }
                    }
                    if (state.prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()] != null) {
                        for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()].length; action++) {
                            if (state.isActionLegal(action)) {
                                if (idx < state.getLegalActions().length && state.getLegalActions()[idx] == action) {
                                    stream.writeFloat((float) (((double) state.n[idx++]) / state.total_n));
                                } else {
                                    Integer.parseInt(null);
                                }
                            } else {
                                stream.writeFloat(-1);
                            }
                        }
                    }
                } else {
                    for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length; action++) {
                        if (false) {
                            if (state.terminal_action == action) {
                                stream.writeFloat(1);
                            } else {
                                stream.writeFloat(0);
                            }
                        } else if (state.isActionLegal(action)) {
                            if (idx < state.getLegalActions().length && state.getLegalActions()[idx] == action) {
                                stream.writeFloat((float) (((double) state.n[idx++]) / state.total_n));
                            } else {
                                Integer.parseInt(null);
                            }
                        } else {
                            stream.writeFloat(-1);
                        }
                    }
                    if (state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null) {
                        for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()].length; action++) {
                            stream.writeFloat(-1);
                        }
                    }
                    if (state.prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()] != null) {
                        for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()].length; action++) {
                            stream.writeFloat(-1);
                        }
                    }
                }
            }
        }
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
                var s = newState;
                boolean isStochastic = newState.isStochastic;
                newState = newState.clone(false);
                newState.stateDesc = s.stateDesc;
                newState.isStochastic = isStochastic;
                newState.searchFrontier = null;
            }
        } else {
            newState = (GameState) nextState;
            if (clone) {
                var s = newState;
                newState = newState.clone(false);
                newState.stateDesc = s.stateDesc;
                newState.searchFrontier = null;
            }
        }
        return newState;
    }

    public void setMatchLogFile(String fileName) {
        try {
            File file = new File(logDir + "/" + fileName);
            file.delete();
            matchLogWriter = new OutputStreamWriter(new GzipCompressorOutputStream(new FileOutputStream(logDir + "/" + fileName, true)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setTrainingDataLogFile(String fileName) {
        try {
            File file = new File(logDir + "/" + fileName);
            file.delete();
            trainingDataWriter = new OutputStreamWriter(new GzipCompressorOutputStream(new FileOutputStream(logDir + "/" + fileName, true)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void flushAndCloseFileWriters() {
        try {
            if (matchLogWriter != null) {
                matchLogWriter.flush();
                matchLogWriter.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            if (trainingDataWriter != null) {
                trainingDataWriter.flush();
                trainingDataWriter.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void printGame(Writer writer, List<GameStep> steps) throws IOException {
        for (int i = 0; i < steps.size(); i++) {
            var step = steps.get(i);
            if (step.state().actionCtx == GameActionCtx.BEGIN_TURN) continue;
            writer.write(step.state().toStringReadable() + "\n");
            if (step.v != null) {
                writer.write(Arrays.toString(step.v) + "\n");
            }
            if (step.action() >= 0) {
                var nextStep = steps.get(i + 1);
                writer.write("action=" + step.state().getActionString(step.action()));
                if (nextStep.state().stateDesc != null) {
                    writer.write(" (" + nextStep.state().stateDesc + ")");
                }
                if (step.isExplorationMove) {
                    writer.write(" (Temperature)");
                }
                writer.write("\n");
            }
        }
    }
}
