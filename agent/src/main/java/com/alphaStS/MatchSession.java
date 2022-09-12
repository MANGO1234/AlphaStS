package com.alphaStS;

import com.alphaStS.utils.ScenarioStats;
import com.alphaStS.utils.Tuple;
import com.alphaStS.utils.Utils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorOutputStream;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.sqrt;

public class MatchSession {
    private final static boolean LOG_GAME_USING_LINES_FORMAT = true;
    private final static boolean USE_NEW_SEARCH = false;

    public boolean training;
    Writer matchLogWriter;
    Writer trainingDataWriter;
    String logDir;
    List<MCTS> mcts = new ArrayList<>();
    List<MCTS> mcts2 = new ArrayList<>();
    public int[][] scenariosGroup;
    public GameSolver solver;
    String modelDir;
    String modelCmpDir;

    public MatchSession(int numberOfThreads, String dir) {
        this(numberOfThreads, dir, null);
    }

    public MatchSession(int numberOfThreads, String dir, String dir2) {
        logDir = dir;
        modelDir = dir;
        modelCmpDir = dir2;
        for (int i = 0; i < numberOfThreads; i++) {
            Model model = new Model(dir);
            var m = new MCTS();
            m.setModel(model);
            mcts.add(m);

            if (dir2 == null) {
                continue;
            }
            if (!dir.equals(dir2)) {
                model = new Model(dir2);
            }
            m = new MCTS();
            m.setModel(model);
            mcts2.add(m);
        }
    }

    GameState origStateCmp;
    int startingAction = -1;
    int startingActionCmp = -1;

    private int findNextChanceAction(Game game, int startIdx) {
        while (startIdx < game.steps.size()) {
            if (game.steps.get(startIdx).state().isStochastic) {
                return startIdx - 1;
            }
            startIdx++;
        }
        return game.steps.size();
    }

    public static record RefRet(GameState state, int refGameIdx) {}

    public Game playGame(GameState origState, int startingAction, Game refGame, MCTS mcts, int nodeCount) {
        var steps = new ArrayList<GameStep>();
        var state = origState.clone(false);
        int r = 0;
        int preBattle_r = 0;
        var refGameIdx = refGame == null ? 0 : findNextChanceAction(refGame, 1);
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

        if (false) {
            while (state.isTerminal() == 0) {
                int upto = nodeCount - (state.total_n + (state.policy == null ? 0 : 1));
                for (int i = 0; i < upto; i++) {
                    mcts.searchLine(state, false, true, upto - i);
                    if (mcts.numberOfPossibleActions == 1 && state.total_n > 0) {
                        break;
                    }
                }

                for (int action : state.searchFrontier.getBestLine().getActions(state)) {
                    steps.add(new GameStep(state, action));
                    if (state.searchFrontier != null) {
                        steps.get(steps.size() - 1).lines = state.searchFrontier.getSortedLinesAsStrings(state);
                    }
                    state.prop.makingRealMove = true;
                    if (state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
                        state = state.clone(false);
                        r = state.doAction(0);
                    } else {
                        var doStartTurn = state.getAction(action).type() == GameActionType.END_TURN;
                        if (nodeCount == 1) {
                            state = state.clone(false);
                            state.doAction(action);
                        } else {
                            state = getNextState(state, mcts, action, steps.get(steps.size() - 1), false);
                        }
                        if (doStartTurn) {
                            state.doAction(0);
                        }
                    }
                    state.prop.makingRealMove = false;
                    RefRet ret = syncWithRef(refGame, refGameIdx, steps, state, action);
                    if (ret != null) {
                        state = ret.state;
                        refGameIdx = ret.refGameIdx;
                    }
                    steps.get(steps.size() - 1).state().clearNextStates();
                }
                state.clearAllSearchInfo();
            }
        } else {
            state.doEval(mcts.model);
            while (state.isTerminal() == 0) {
                int upto = nodeCount - (state.total_n + (state.policy == null ? 0 : 1));
                for (int i = 0; i < upto; i++) {
                    mcts.search(state, false, upto - i);
                    if (mcts.numberOfPossibleActions == 1 && state.total_n > 0) {
                        break;
                    }
                }

                int action = MCTS.getActionWithMaxNodesOrTerminal(state, null);
                steps.add(new GameStep(state, action));
                state.prop.makingRealMove = true;
                if (state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
                    state = state.clone(false);
                    r = state.doAction(0);
                } else {
                    if (nodeCount == 1) {
                        state = state.clone(false);
                        state.doAction(action);
                        if (state.actionCtx == GameActionCtx.BEGIN_TURN) {
                            state.doAction(0);
                        }
                    } else {
                        state = getNextState(state, mcts, action, steps.get(steps.size() - 1), false);
                    }
                }
                state.prop.makingRealMove = false;
                RefRet ret = syncWithRef(refGame, refGameIdx, steps, state, action);
                if (ret != null) {
                    state = ret.state;
                    refGameIdx = ret.refGameIdx;
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
        return new Game(steps, preBattle_r, r, null, true);
    }

    // when comparing game searchRandomGen can get out of sync, make sure it's synced as much as possible
    private RefRet syncWithRef(Game refGame, int refGameIdx, ArrayList<GameStep> steps, GameState state, int action) {
        if (state.isStochastic && refGame != null && refGameIdx < refGame.steps().size()) {
            var prevState = steps.get(steps.size() - 1).state();
            boolean check = false;
            if (refGame.steps.get(refGameIdx).getAction().type() == GameActionType.END_TURN) {
                if (prevState.getAction(action).type() == GameActionType.END_TURN) {
                    if (prevState.equals(refGame.steps.get(refGameIdx).state())) {
                        check = true;
                    }
                }
            } else {
                if (prevState.getAction(action).type() == GameActionType.END_TURN) {
                    do {
                        refGameIdx = findNextChanceAction(refGame, refGameIdx + 2);
                    } while (refGameIdx < refGame.steps.size() && refGame.steps.get(refGameIdx).getAction().type() != GameActionType.END_TURN);
                    if (refGameIdx < refGame.steps.size()) {
                        if (prevState.equals(refGame.steps.get(refGameIdx).state())) {
                            check = true;
                        }
                    }
                } else {
                    if (prevState.equals(refGame.steps.get(refGameIdx).state()) && action == refGame.steps().get(refGameIdx).action()) {
                        check = true;
                    }
                }
            }
            if (!check) {
                return null;
            }
            var refState = refGame.steps().get(refGameIdx + 1).state();
            if ((refGame.steps.get(refGameIdx).takenFromChanceStateCache && !steps.get(steps.size() - 1).takenFromChanceStateCache) ||
                    (!refGame.steps.get(refGameIdx).takenFromChanceStateCache && steps.get(steps.size() - 1).takenFromChanceStateCache) ||
                    refState.getSearchRandomGen().getStartingSeed() != state.getSearchRandomGen().getStartingSeed()) {
                state = refState.clone(false);
                state.prop = prevState.prop;
                state.clearAllSearchInfo();
                state.setSearchRandomGen(state.getSearchRandomGen().createWithSeed(refState.getSearchRandomGen().getStartingSeed()));
            }
            refGameIdx = findNextChanceAction(refGame, refGameIdx + 2);
            return new RefRet(state, refGameIdx);
        }
        return null;
    }

    public static record Game(List<GameStep> steps, int preBattle_r, int battle_r, List<GameStep> augmentedSteps, boolean noExploration) {}
    public static record GameResult(Game game, int modelCalls, Game game2, int modelCalls2, long seed, List<GameResult> reruns, String remoteServer, int remoteR, ScenarioStats remoteStats) {}
    public static record TrainingGameResult(Game game, String remoteServer, String remoteTrainingGameRecord, byte[] remoteTrainingData) {}

    public void playGames(GameState origState, int numOfGames, int nodeCount, boolean printProgress) {
        var seeds = new ArrayList<Long>(numOfGames);
        for (int i = 0; i < numOfGames; i++) {
            seeds.add(origState.prop.random.nextLong(RandomGenCtx.Other));
        }
        var deq = new LinkedBlockingDeque<GameResult>();
        var numToPlay = new AtomicInteger(numOfGames);
        for (int i = 0; i < mcts.size(); i++) {
            startPlayGameThread(origState, nodeCount, seeds, deq, numToPlay, i);
        }
        var remoteServerGames = new HashMap<String, Integer>();
        if (nodeCount > 1) {
            for (Tuple<String, Integer> server : getRemoteServers()) {
                remoteServerGames.putIfAbsent(server.v1() + ":" + server.v2(), 0);
                startRemotePlayGameThread(server.v1(), server.v2(), modelDir, modelCmpDir, nodeCount, numToPlay, deq);
            }
        }

        var game_i = 0;
        var ret = getInfoMaps(origState);
        var combinedInfoMap = ret.v1();
        var battleInfoMap = ret.v2();
        var scenarioStats = new HashMap<Integer, ScenarioStats>();
        var start = System.currentTimeMillis();
        var solverErrorCount = 0;
        var progressInterval = ((int) Math.ceil(numOfGames / 1000f)) * 25;
        while (game_i < numOfGames) {
            GameResult result;
            try {
                result = deq.takeFirst();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            int r;
            if (result.remoteStats == null) {
                Game game = result.game;
                List<GameStep> steps = game.steps;
                List<GameStep> steps2 = result.game2 == null ? null : result.game2.steps;
                var state = steps.get(steps.size() - 1).state();
                r = game.preBattle_r * battleInfoMap.size() + game.battle_r;
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
                scenarioStats.computeIfAbsent(r, (k) -> new ScenarioStats()).add(game.steps, result.modelCalls);
                scenarioStats.get(r).add(game.steps, steps2, result.modelCalls2, result.reruns);
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
                                    matchLogWriter.write(step.state().toString() + "\n");
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
            } else {
                r = result.remoteR;
                scenarioStats.computeIfAbsent(r, (k) -> new ScenarioStats()).add(result.remoteStats, origState);
                remoteServerGames.computeIfPresent(result.remoteServer, (k, x) -> x + 1);
                game_i += 1;
            }

            if ((printProgress && game_i % progressInterval == 0) || game_i == numOfGames) {
                System.out.println("Progress: " + game_i + "/" + numOfGames);
                if (!training && solver != null) {
                    System.out.println("Error Count: " + solverErrorCount);
                }
                remoteServerGames.forEach((key, value) -> System.out.printf("Server %s: %d games\n", key, value));
                if (scenarioStats.size() > 1) {
                    for (var info : combinedInfoMap.entrySet()) {
                        var i = info.getKey();
                        if (scenarioStats.get(i) != null) {
                            System.out.println("Scenario " + info.getKey() + ": " + info.getValue().desc());
                            scenarioStats.get(i).printStats(origState, 4);
                        }
                    }
                }
                if (scenariosGroup != null) {
                    for (int i = 0; i < scenariosGroup.length; i++) {
                        System.out.println("Scenario " + IntStream.of(scenariosGroup[i]).mapToObj(String::valueOf).collect(Collectors.joining(", ")) + ": " + ScenarioStats.getCommonString(combinedInfoMap, scenariosGroup[i]));
                        var group = IntStream.of(scenariosGroup[i]).mapToObj(scenarioStats::get).filter(Objects::nonNull).toArray(ScenarioStats[]::new);
                        ScenarioStats.combine(group).printStats(origState, 4);
                    }
                }
                ScenarioStats.combine(scenarioStats.values().toArray(new ScenarioStats[0])).printStats(origState, 0);
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

    private List<Tuple<String, Integer>> getRemoteServers() {
        if (!Configuration.USE_REMOTE_SERVERS) {
            return List.of();
        }
        try {
            List<Tuple<String, Integer>> l = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(System.getProperty("user.home") + "/alphaStSServers.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                var s = line.split(":");
                l.add(new Tuple<>(s[0], Integer.parseInt(s[1])));
            }
            return l;
        } catch (FileNotFoundException e) {
            return List.of();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startRemotePlayGameThread(String ip, int port, String modelDir, String modelCmpDir, int nodeCount, AtomicInteger numToPlay, BlockingDeque<GameResult> deq) {
        new Thread(() -> {
            while (numToPlay.get() > 0) {
                try {
                    Socket socket = new Socket(ip, port);
                    JsonFactory jsonFactory = new JsonFactory();
                    jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
                    jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
                    ObjectMapper mapper = new ObjectMapper(jsonFactory);
                    ServerRequest req = new ServerRequest();
                    req.type = ServerRequestType.UPLOAD_MODEL;
                    req.bytes = new FileInputStream(modelDir + "/model.onnx").readAllBytes();
                    var out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    var w = new StringWriter();
                    mapper.writeValue(w, req);
                    var reqStr = w.toString();
                    System.out.printf("Uploading model to %s:%d...\n", ip, port);
                    out.writeInt(reqStr.length());
                    out.writeBytes(reqStr);
                    out.flush();
                    if (modelCmpDir != null) {
                        req.type = ServerRequestType.UPLOAD_MODEL_CMP;
                        req.bytes = null;
                        if (!modelDir.equals(modelCmpDir)) {
                            req.bytes = new FileInputStream(modelCmpDir + "/model.onnx").readAllBytes();
                        }
                        w = new StringWriter();
                        mapper.writeValue(w, req);
                        reqStr = w.toString();
                        System.out.printf("Uploading model for comparison to %s:%d...\n", ip, port);
                        out.writeInt(reqStr.length());
                        out.writeBytes(reqStr);
                        out.flush();
                    }
                    req.type = ServerRequestType.PLAY_GAMES;
                    req.nodeCount = nodeCount;
                    System.out.printf("Start requesting games from %s:%d...\n", ip, port);
                    while (numToPlay.get() > 0) {
                        req.remainingGames = numToPlay.get();
                        w = new StringWriter();
                        mapper.writeValue(w, req);
                        reqStr = w.toString();
                        out.writeInt(reqStr.length());
                        out.writeBytes(reqStr);
                        out.flush();
                        var ret = mapper.readValue(socket.getInputStream(), RemoteGameResult[].class);
                        int idx = 0;
                        while (idx < ret.length && numToPlay.getAndDecrement() > 0) {
                            deq.putLast(new GameResult(null, 0, null, 0, 0, null, ip + ":" + port, ret[idx].r, ret[idx].stats));
                            idx++;
                        }
                    }
                    System.out.printf("Stop requesting games from %s:%d...\n", ip, port);
                    req.remainingGames = 0;
                    w = new StringWriter();
                    mapper.writeValue(w, req);
                    reqStr = w.toString();
                    out.writeInt(reqStr.length());
                    out.writeBytes(reqStr);
                    out.flush();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace(System.out);
                    Utils.sleep(5000);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public static record RemoteGameResult(int r, ScenarioStats stats) {}

    AtomicInteger remoteNumOfGames = new AtomicInteger(-123456);
    BlockingDeque<GameResult> remoteDeq = new LinkedBlockingDeque<>();
    List<Thread> remoteThreads = new ArrayList<>();

    public RemoteGameResult playGamesRemote(GameState origState, int numOfGames, int nodeCount) {
        if (remoteNumOfGames.get() == -123456) {
            remoteNumOfGames.set(numOfGames);
            var seeds = new ArrayList<Long>(numOfGames);
            for (int i = 0; i < numOfGames; i++) {
                seeds.add(origState.prop.random.nextLong(RandomGenCtx.Other));
            }
            for (int i = 0; i < mcts.size(); i++) {
                remoteThreads.add(startPlayGameThread(origState, nodeCount, seeds, remoteDeq, remoteNumOfGames, i));
            }
        }

        GameResult result;
        Game game;
        List<GameStep> steps;
        List<GameStep> steps2;
        try {
            result = remoteDeq.takeFirst();
            game = result.game;
            steps = game.steps;
            steps2 = result.game2 == null ? null : result.game2.steps;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        var stat = new ScenarioStats();
        stat.add(steps, result.modelCalls);
        stat.add(steps, steps2, result.modelCalls2, result.reruns);
        var ret = getInfoMaps(origState);
        var battleInfoMap = ret.v2();
        var r = game.preBattle_r * battleInfoMap.size() + game.battle_r;
        return new RemoteGameResult(r, stat);
    }

    public void stopPlayGamesRemote() {
        remoteNumOfGames.set(-123456);
        for (int i = 0; i < remoteThreads.size(); i++) {
            try {
                remoteThreads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        remoteThreads.clear();
        remoteDeq.clear();
    }

    public void close() {
        for (int i = 0; i < mcts.size(); i++) {
            mcts.get(i).model.close();
        }
        for (int i = 0; i < mcts2.size(); i++) {
            if (!modelDir.equals(modelCmpDir)) {
                mcts2.get(i).model.close();
            }
        }
    }

    private Thread startPlayGameThread(GameState origState, int nodeCount, ArrayList<Long> seeds, BlockingDeque<GameResult> deq, AtomicInteger numToPlay, int threadIdx) {
        var session = this;
        var t = new Thread(() -> {
            int idx = numToPlay.getAndDecrement();
            while (idx > 0) {
                if (deq.size() >= (nodeCount == 1 ? 1000 : 40)) {
                    if (numToPlay.get() < 0) {
                        break;
                    }
                    Utils.sleep(200);
                    continue;
                }
                var state = origState.clone(false);
                state.prop = state.prop.clone();
                state.prop.doingComparison = mcts2.size() > 0;
                state.prop.realMoveRandomGen = new RandomGen.RandomGenByCtx(seeds.get(idx - 1));
                state.prop.testNewFeature = true;
                var prev = mcts.get(threadIdx).model.calls - mcts.get(threadIdx).model.cache_hits;
                var game1 = session.playGame(state, startingAction, null, mcts.get(threadIdx), nodeCount);
                var modelCalls = mcts.get(threadIdx).model.calls - mcts.get(threadIdx).model.cache_hits - prev;
                Game game2 = null;
                var modelCalls2 = 0;
                List<GameResult> reruns = null;
                if (mcts2.size() > 0) {
                    var randomGen= state.prop.realMoveRandomGen;
                    randomGen.timeTravelToBeginning();
                    var state2 = (origStateCmp != null ? origStateCmp : origState).clone(false);
                    state2.prop = state2.prop.clone();
                    state2.prop.doingComparison = true;
                    state2.prop.realMoveRandomGen = randomGen;
                    state2.prop.testNewFeature = false;
                    prev = mcts2.get(threadIdx).model.calls - mcts2.get(threadIdx).model.cache_hits;
                    game2 = session.playGame(state2, startingActionCmp, game1, mcts2.get(threadIdx), nodeCount);
                    modelCalls2 = mcts2.get(threadIdx).model.calls - mcts2.get(threadIdx).model.cache_hits - prev;

                    var turns1 = GameStateUtils.groupByTurns(game1.steps);
                    var turns2 = GameStateUtils.groupByTurns(game2.steps);
                    for (int turnI = 1; turnI < Math.min(turns1.size(), turns2.size()); turnI++) {
                        var t1 = turns1.get(turnI);
                        var t2 = turns2.get(turnI);
                        var ts1 = t1.get(t1.size() - 1).state().clone(false);
                        var ts2 = t2.get(t2.size() - 1).state().clone(false);
                        for (int j = 0; j < ts1.getLegalActions().length; j++) {
                            if (ts1.getAction(j).type() == GameActionType.END_TURN) {
                                ts1.doAction(j);
                                break;
                            }
                        }
                        for (int j = 0; j < ts2.getLegalActions().length; j++) {
                            if (ts2.getAction(j).type() == GameActionType.END_TURN) {
                                ts2.doAction(j);
                                break;
                            }
                        }
                        if (!ts1.equals(ts2)) {
                            reruns = new ArrayList<>();
                            for (int j = 0; j < Configuration.CMP_DEVIATION_NUM_RERUN; j++) {
                                var rerunState = ts1.clone(false);
                                rerunState.prop = rerunState.prop.clone();
                                rerunState.prop.realMoveRandomGen = new RandomGen.RandomGenByCtx(state.prop.realMoveRandomGen.nextLong(RandomGenCtx.Misc));
                                prev = mcts.get(threadIdx).model.calls - mcts.get(threadIdx).model.cache_hits;
                                var rerunGame1 = session.playGame(rerunState, 0, null, mcts.get(threadIdx), nodeCount);
                                var rerunModelCalls = mcts.get(threadIdx).model.calls - mcts.get(threadIdx).model.cache_hits - prev;
                                Game rerunGame2 = null;
                                var rerunModelCalls2 = 0;
                                if (mcts2.size() > 0) {
                                    randomGen= rerunState.prop.realMoveRandomGen;
                                    randomGen.timeTravelToBeginning();
                                    var rerunState2 = ts2.clone(false);
                                    rerunState2.prop = rerunState2.prop.clone();
                                    rerunState2.prop.realMoveRandomGen = randomGen;
                                    prev = mcts2.get(threadIdx).model.calls - mcts2.get(threadIdx).model.cache_hits;
                                    rerunGame2 = session.playGame(rerunState2, 0, game1, mcts2.get(threadIdx), nodeCount);
                                    rerunModelCalls2 = mcts2.get(threadIdx).model.calls - mcts2.get(threadIdx).model.cache_hits - prev;
                                }
                                reruns.add(new GameResult(rerunGame1, rerunModelCalls, rerunGame2, rerunModelCalls2, 0, null, null,0, null));
                            }
                            break;
                        }
                    }
                }

                try {
                    deq.putLast(new GameResult(game1, modelCalls, game2, modelCalls2, seeds.get(idx - 1), reruns, null,0, null));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                idx = numToPlay.getAndDecrement();
                if (nodeCount > 1) {
                    Utils.sleep(Configuration.SLEEP_PER_GAME);
                }
            }
        });
        t.start();
        return t;
    }

    public Tuple<Map<Integer, GameStateRandomization.Info>, Map<Integer, GameStateRandomization.Info>> getInfoMaps(GameState state) {
        var combinedInfoMap = new HashMap<Integer, GameStateRandomization.Info>();
        Map<Integer, GameStateRandomization.Info> preBattleInfoMap;
        List<Integer> preBattleInfoMapKeys;
        if (state.prop.preBattleRandomization != null) {
            preBattleInfoMap = state.prop.preBattleRandomization.listRandomizations();
            preBattleInfoMapKeys = preBattleInfoMap.keySet().stream().sorted().toList();
        } else {
            preBattleInfoMap = new HashMap<>();
            preBattleInfoMap.put(0, new GameStateRandomization.Info(1, ""));
            preBattleInfoMapKeys = List.of(0);
        }
        Map<Integer, GameStateRandomization.Info> battleInfoMap;
        List<Integer> battleInfoMapKeys;
        if (state.prop.randomization != null) {
            battleInfoMap = state.prop.randomization.listRandomizations();
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
        return new Tuple<>(combinedInfoMap, battleInfoMap);
    }

    boolean SLOW_TRAINING_WINDOW;
    boolean POLICY_CAP_ON;
    boolean TRAINING_WITH_LINE;

    private double[] calcExpectedValue2(ChanceState cState, GameState generatedState, MCTS mcts, double[] vCur) {
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
        return est;
    }

    private double[] calcExpectedValue(ChanceState cState, GameState generatedState, MCTS mcts, double[] vCur) {
        return calcExpectedValue2(cState, generatedState, mcts, vCur);
//        var stateActual = generatedState == null ? null : cState.addGeneratedState(generatedState);
//        while (cState.total_n < 300) {
//            mcts.search(cState.getNextState(false), false, 1000);
//        }
//        double[] est = new double[vCur.length];
//        double[] out = new double[vCur.length];
//        for (ChanceState.Node node : cState.cache.values()) {
//            if (node.state != stateActual) {
//                out[GameState.V_COMB_IDX] = node.state.total_q_comb / (node.state.total_n + 1);
//                out[GameState.V_WIN_IDX] = node.state.total_q_win / (node.state.total_n + 1);
//                out[GameState.V_HEALTH_IDX] = node.state.total_q_health / (node.state.total_n + 1);
//                for (int i = 0; i < est.length; i++) {
//                    est[i] += out[i] * node.n;
//                }
//            }
//        }
//        float p = generatedState == null ? 0 : ((float) cState.getCount(stateActual)) / cState.total_node_n;
//        for (int i = 0; i < est.length; i++) {
//            est[i] /= cState.total_node_n;
//            est[i] = (float) Math.min(vCur[i] * p + est[i], 1);
//        }
////        System.out.println(cState);
////        System.out.println(Arrays.toString(est));
////        cState = new ChanceState(null, cState.parentState, cState.parentAction);
////        System.out.println(Arrays.toString(calcExpectedValue2(cState, generatedState, mcts, vCur)));
//
//        //  var prevSize = cState.cache.size();
//        //  for (int j = 1; j < 900; j++) {
//        //      cState.getNextState(prevState, prevAction);
//        //  }
//        //  est_v_win = 0;
//        //  est_v_health = 0;
//        //  for (ChanceState.Node node : cState.cache.values()) {
//        //      if (node.state != state) {
//        //          node.state.doEval(session.mcts.get(_ii).model);
//        //          node.state.get_v(out);
//        //          est_v_win += out[0] * node.n;
//        //          est_v_health += out[1] * node.n;
//        //      }
//        //  }
//        //  est_v_win /= cState.total_n;
//        //  est_v_health /= cState.total_n;
//        //  p = ((float) cState.getCount(state)) / cState.total_n;
//        //  var v_win2 = Math.min(v_win * p + (float) est_v_win, 1);
//        //  var v_health2 = Math.min(v_health * p + (float) est_v_health, 1);
//        //  if ((v_win - v_win2) > 0.02 || (v_health - v_health2) > 0.0) {
//        //      System.out.println((v_win - v_win2) + "," + (v_health - v_health2) + "," + cState.cache.size() + "," + prevSize);
//        //  }
//        return est;
    }

    private Game playTrainingGame(GameState origState, int nodeCount, MCTS mcts) {
        var steps = new ArrayList<GameStep>();
        var augmentedSteps = new ArrayList<GameStep>();
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
            if (!SLOW_TRAINING_WINDOW && steps.get(i).isExplorationMove) {
                List<GameStep> extraSteps = new ArrayList<>();
                ChanceState cState = findBestLineChanceState(steps.get(i).state(), nodeCount, mcts, extraSteps);
                // taking the max of 2 random variable inflates eval slightly, so we try to make calcExpectedValue have a large sample
                // to reduce this effect, seems ok so far, also check if we are transposing and skip for transposing
                if (cState != null && lastChanceState != null && !cState.equals(lastChanceState)) {
                    double[] ret = calcExpectedValue(cState, null, mcts, new double[vLen]);
                    if (ret[GameState.V_COMB_IDX] > vCur[GameState.V_COMB_IDX]) {
                        vCur = ret;
                        lastChanceState = cState;
                    }
                    for (GameStep step : extraSteps) {
                        step.v = ret;
                        step.trainingWriteCount = 1;
                    }
                    augmentedSteps.addAll(extraSteps);
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

        return new Game(steps, preBattle_r, r, augmentedSteps, doNotExplore);
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

    private ChanceState findBestLineChanceState(GameState state, int nodeCount, MCTS mcts, List<GameStep> augmentedSteps) {
        ChanceState cs1 = null;
        GameState ss = state;
        while (true) {
            if (ss == null || ss.isTerminal() != 0) {
                break;
            }
            int action = MCTS.getActionWithMaxNodesOrTerminal(ss, null);
            if (ss.ns[action] instanceof ChanceState cs) {
                cs1 = cs;
                break;
            }
            ss = (GameState) ss.ns[action];
        }
        if (!Configuration.TRAINING_RESCORE_SEARCH_FOR_BEST_LINE) {
            return cs1;
        }

        ChanceState cs2 = null;
        boolean firstState = true;
        while (true) {
            if (state.isTerminal() != 0) {
                break;
            }
            int todo = nodeCount - state.total_n;
            for (int i = 0; i < todo; i++) {
                mcts.search(state, false, todo - i);
                if (mcts.numberOfPossibleActions == 1 && state.total_n >= 1) {
                    break;
                }
            }

            int action = MCTS.getActionWithMaxNodesOrTerminal(state, null);
            if (!firstState) {
                augmentedSteps.add(new GameStep(state, action));
            }
            firstState = false;
            if (state.ns[action] instanceof ChanceState cs) {
                cs2 = cs;
                break;
            }
            state = (GameState) state.ns[action];
        }
        return cs2;
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

        return new Game(steps, preBattle_r, r, null, true);
    }

    public void playTrainingGames(GameState origState, int numOfGames, int nodeCount, String path) throws IOException {
        File file = new File(path);
        file.delete();
        var stream = new DataOutputStream(new FramedLZ4CompressorOutputStream(new BufferedOutputStream(new FileOutputStream(path))));
        var deq = new LinkedBlockingDeque<TrainingGameResult>();
        var session = this;
        var numToPlay = new AtomicInteger(numOfGames);
        for (int i = 0; i < mcts.size(); i++) {
            int ii = i;
            new Thread(() -> {
                var state = origState.clone(false);
                while (numToPlay.getAndDecrement() > 0) {
                    try {
                        deq.putLast(new TrainingGameResult(session.playTrainingGame(state, nodeCount, mcts.get(ii)), null, null, null));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Utils.sleep(Configuration.SLEEP_PER_GAME_TRAINING);
                }
            }).start();
        }
        var remoteServerGames = new HashMap<String, Integer>();
        for (Tuple<String, Integer> server : getRemoteServers()) {
            remoteServerGames.putIfAbsent(server.v1() + ":" + server.v2(), 0);
            startRemotePlayTrainingGameThread(server.v1(), server.v2(), nodeCount, numToPlay, deq);
        }

        var trainingGame_i = 0;
        while (trainingGame_i < numOfGames) {
            TrainingGameResult result;
            try {
                result = deq.takeFirst();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            trainingGame_i += 1;
            if (result.game != null) {
                var game = result.game;
                var steps = game.steps;
                if (trainingDataWriter != null && trainingGame_i <= 100) {
                    trainingDataWriter.write("*** Match " + trainingGame_i + " ***\n");
                    writeTrainingGameRecord(trainingDataWriter, origState, game, steps);
                }
                pruneForcedPlayouts(steps);
                writeTrainingData(stream, steps);
                writeTrainingData(stream, game.augmentedSteps);
            } else {
                if (trainingDataWriter != null && trainingGame_i <= 100) {
                    trainingDataWriter.write("*** Match " + trainingGame_i + " (Remote) ***\n");
                    trainingDataWriter.write(result.remoteTrainingGameRecord);
                }
                stream.write(result.remoteTrainingData);
                remoteServerGames.computeIfPresent(result.remoteServer, (k, x) -> x + 1);
            }
            if (trainingGame_i % 20 == 0) {
                System.out.println(trainingGame_i + " games finished...");
            }
        }
        stream.flush();
        stream.close();
        remoteServerGames.forEach((key, value) -> System.out.printf("Server %s: %d games\n", key, value));
    }

    private void writeTrainingGameRecord(Writer writer, GameState origState, Game game, List<GameStep> steps) {
        try {
            var state = steps.get(steps.size() - 1).state();
            if (origState.prop.preBattleRandomization != null) {
                var info = origState.prop.preBattleRandomization.listRandomizations();
                if (info.size() > 1) {
                    writer.write("Pre-Battle Randomization: " + info.get(game.preBattle_r).desc() + "\n");
                }
            }
            if (origState.prop.randomization != null) {
                var info = origState.prop.randomization.listRandomizations();
                if (info.size() > 1) {
                    writer.write("Battle Randomization: " + info.get(game.battle_r).desc() + "\n");
                }
            }
            if (!TRAINING_WITH_LINE && game.noExploration) {
                writer.write("No Temperature Moves\n");
            }
            writer.write("Result: " + (state.isTerminal() == 1 ? "Win" : "Loss") + "\n");
            writer.write("Damage Taken: " + (state.getPlayeForRead().getOrigHealth() - state.getPlayeForRead().getHealth()) + "\n");
            printGame(writer, steps);
            writer.write("\n");
            writer.write("\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void startRemotePlayTrainingGameThread(String ip, int port, int nodeCount, AtomicInteger numToPlay, BlockingDeque<TrainingGameResult> deq) {
        new Thread(() -> {
            while (numToPlay.get() > 0) {
                try {
                    var socket = new Socket(ip, port);
                    var jsonFactory = new JsonFactory();
                    jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
                    jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
                    var mapper = new ObjectMapper(jsonFactory);
                    var out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    var req = new ServerRequest();
                    req.type = ServerRequestType.UPLOAD_MODEL;
                    req.bytes = new FileInputStream(modelDir + "/model.onnx").readAllBytes();
                    var w = new StringWriter();
                    mapper.writeValue(w, req);
                    var reqStr = w.toString();
                    System.out.printf("Uploading model to %s:%d...\n", ip, port);
                    out.writeInt(reqStr.length());
                    out.writeBytes(reqStr);
                    out.flush();
                    req.type = ServerRequestType.PLAY_TRAINING_GAMES;
                    req.nodeCount = nodeCount;
                    System.out.printf("Start requesting training games from %s:%d...\n", ip, port);
                    while (numToPlay.get() > 0) {
                        req.remainingGames = numToPlay.get();
                        w = new StringWriter();
                        mapper.writeValue(w, req);
                        reqStr = w.toString();
                        out.writeInt(reqStr.length());
                        out.writeBytes(reqStr);
                        out.flush();
                        var ret = mapper.readValue(socket.getInputStream(), MatchSession.TrainingGameResult[].class);
                        int idx = 0;
                        while (idx < ret.length && numToPlay.getAndDecrement() > 0) {
                            var result = new TrainingGameResult(null, ip + ":" + port, ret[idx].remoteTrainingGameRecord, ret[idx].remoteTrainingData);
                            deq.putLast(result);
                            idx++;
                        }
                    }
                    System.out.printf("Stop requesting training games from %s:%d...\n", ip, port);
                    req.remainingGames = 0;
                    w = new StringWriter();
                    mapper.writeValue(w, req);
                    reqStr = w.toString();
                    out.writeInt(reqStr.length());
                    out.writeBytes(reqStr);
                    out.flush();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace(System.out);
                    Utils.sleep(5000);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    AtomicInteger remoteNumOfTrainingGames = new AtomicInteger(-123456);
    BlockingDeque<Game> remoteTrainingDeq = new LinkedBlockingDeque<>();
    List<Thread> remoteTrainingThreads = new ArrayList<>();

    public TrainingGameResult playTrainingGamesRemote(GameState origState, int numOfGames, int nodeCount) throws IOException {
        if (remoteNumOfTrainingGames.get() == -123456) {
            remoteNumOfTrainingGames.set(numOfGames);
            var session = this;
            for (int i = 0; i < mcts.size(); i++) {
                int ii = i;
                remoteTrainingThreads.add(new Thread(() -> {
                    var state = origState.clone(false);
                    int idx = remoteNumOfTrainingGames.getAndDecrement();
                    while (idx > 0) {
                        if (remoteTrainingDeq.size() >= 30) {
                            if (remoteNumOfTrainingGames.get() <= 0) {
                                break;
                            }
                            Utils.sleep(200);
                            continue;
                        }
                        try {
                            remoteTrainingDeq.putLast(session.playTrainingGame(state, nodeCount, mcts.get(ii)));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        idx = remoteNumOfTrainingGames.getAndDecrement();
                    }
                }));
                remoteTrainingThreads.get(remoteTrainingThreads.size() - 1).start();
            }
        }

        Game game;
        List<GameStep> steps;
        try {
            game = remoteTrainingDeq.takeFirst();
            steps = game.steps;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        var gameRecordWriter = new StringWriter();
        writeTrainingGameRecord(gameRecordWriter, origState, game, steps);
        pruneForcedPlayouts(steps);
        var byteStream = new ByteArrayOutputStream();
        writeTrainingData(new DataOutputStream(byteStream), steps);
        writeTrainingData(new DataOutputStream(byteStream), game.augmentedSteps);
        return new TrainingGameResult(null, null, gameRecordWriter.toString(), byteStream.toByteArray());
    }

    public void stopPlayTrainingGamesRemote() {
        remoteNumOfTrainingGames.set(-123456);
        for (int i = 0; i < remoteTrainingThreads.size(); i++) {
            try {
                remoteTrainingThreads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        remoteTrainingThreads.clear();
        remoteTrainingDeq.clear();
    }

    private void pruneForcedPlayouts(List<GameStep> steps) {
        GameState state;
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
                    del_n += state.n[i] - new_n;
                    state.n[i] = new_n;
                }
            }
            state.total_n -= del_n;
            if (calcKld(state) > 0.5) {
                //                    step.trainingWriteCount = 2;
            }
        }
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
                        var order = state.getEnemyOrder();
                        for (int actionIdx = 0; actionIdx < state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()].length; actionIdx++) {
                            int action = order != null ? order[actionIdx] : actionIdx;
                            if (state.isActionLegal(action)) {
                                if (order != null) {
                                    for (int idx2 = 0; idx2 < state.getLegalActions().length; idx2++) {
                                        if (state.getLegalActions()[idx2] == action) {
                                            if (state.terminal_action >= 0) {
                                                if (state.terminal_action == idx2) {
                                                    stream.writeFloat(1);
                                                } else {
                                                    stream.writeFloat(0);
                                                }
                                            } else {
                                                stream.writeFloat((float) (((double) state.n[idx2]) / state.total_n));
                                            }
                                        }
                                    }
                                } else {
                                    if (idx < state.getLegalActions().length && state.getLegalActions()[idx] == action) {
                                        if (state.terminal_action >= 0) {
                                            if (state.terminal_action == idx++) {
                                                stream.writeFloat(1);
                                            } else {
                                                stream.writeFloat(0);
                                            }
                                        } else {
                                            stream.writeFloat((float) (((double) state.n[idx++]) / state.total_n));
                                        }
                                    } else {
                                        Integer.parseInt(null);
                                    }
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
                        if (state.isActionLegal(action)) {
                            if (idx < state.getLegalActions().length && state.getLegalActions()[idx] == action) {
                                if (state.terminal_action >= 0) {
                                    if (state.terminal_action == idx++) {
                                        stream.writeFloat(1);
                                    } else {
                                        stream.writeFloat(0);
                                    }
                                } else {
                                    stream.writeFloat((float) (((double) state.n[idx++]) / state.total_n));
                                }
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
        return getNextState(state, mcts, action, null, clone);
    }

    private GameState getNextState(GameState state, MCTS mcts, int action, GameStep prevStep, boolean clone) {
        State nextState = state.ns[action];
        GameState newState;
        if (nextState instanceof ChanceState cState) {
            newState = cState.getNextState(false);
            if (prevStep != null) {
                prevStep.takenFromChanceStateCache = newState.policy != null;
            }
            if (newState.policy == null) {
                newState.doEval(mcts.model);
            }
            if (clone) {
                var s = newState;
                newState = newState.clone(false);
                newState.stateDesc = s.stateDesc;
                newState.searchFrontier = null;
            }
            newState.isStochastic = true;
        } else {
            newState = (GameState) nextState;
            if (clone) {
                var s = newState;
                newState = newState.clone(false);
                newState.stateDesc = s.stateDesc;
                newState.searchFrontier = null;
            }
            newState.isStochastic = false;
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
            writer.write(step.state() + "\n");
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

    public void tune(GameState state) {
        var cpucts = new ArrayList<Double>();
        for (int i = 0; i < 16; i++) {
            cpucts.add(0.07 + i * 0.06 / 16);
        }
        var rand = new Random();
        for (int i = 0; i < 20; i++) {
            List<Tuple<Double, Double>> r = tuneH(state, 500, 50, cpucts);
            r.sort(Comparator.comparingDouble(Tuple::v2));
            System.out.println(r);
            cpucts = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
               cpucts.add(r.get(12 + j).v1() * (rand.nextBoolean() ? 0.8 : 1.2));
            }
            for (int j = 4; j < 16; j++) {
                cpucts.add(r.get(j).v1());
            }
            cpucts.sort(Comparator.comparingDouble(Double::valueOf));
        }
    }

    private Thread startPlayGameThread2(GameState origState, int nodeCount, ArrayList<Long> seeds, BlockingDeque<GameResult> deq, AtomicInteger numToPlay, int threadIdx) {
        var session = this;
        var t = new Thread(() -> {
            int idx = numToPlay.getAndDecrement();
            while (idx > 0) {
                if (deq.size() >= (nodeCount == 1 ? 1000 : 40)) {
                    if (numToPlay.get() < 0) {
                        break;
                    }
                    Utils.sleep(200);
                    continue;
                }
                var state = origState.clone(false);
                state.prop = state.prop.clone();
                state.prop.doingComparison = true;
                state.prop.realMoveRandomGen = new RandomGen.RandomGenByCtx(seeds.get(idx - 1));
                state.prop.testNewFeature = true;
                state.prop.cpuct = cpucts.get(0);
                var prev = mcts.get(threadIdx).model.calls - mcts.get(threadIdx).model.cache_hits;
                var game1 = session.playGame(state, startingAction, null, mcts.get(threadIdx), nodeCount);
                var modelCalls = mcts.get(threadIdx).model.calls - mcts.get(threadIdx).model.cache_hits - prev;
                results.set(0, results.get(0) + game1.steps().get(game1.steps.size() - 1).state().get_q());
                for (int i = 1; i < cpucts.size(); i++) {
                    state.prop.realMoveRandomGen = new RandomGen.RandomGenByCtx(seeds.get(idx - 1));
                    state = origState.clone(false);
                    state.prop = state.prop.clone();
                    state.prop.doingComparison = true;
                    state.prop.realMoveRandomGen = new RandomGen.RandomGenByCtx(seeds.get(idx - 1));
                    state.prop.testNewFeature = true;
                    state.prop.cpuct = cpucts.get(i);
                    var game2 = session.playGame(state, startingAction, game1, mcts.get(threadIdx), nodeCount);
                    results.set(i, results.get(i) + game2.steps().get(game2.steps.size() - 1).state().get_q());
                }
                try {
                    deq.putLast(new GameResult(game1, modelCalls, null, 0, seeds.get(idx - 1), null, null,0, null));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                idx = numToPlay.getAndDecrement();
                if (nodeCount > 1) {
                    Utils.sleep(Configuration.SLEEP_PER_GAME);
                }
            }
        });
        t.start();
        return t;
    }

    ArrayList<Double> cpucts;
    ArrayList<Double> results;
    public List<Tuple<Double, Double>> tuneH(GameState origState, int numOfGames, int nodeCount, ArrayList<Double> cpucts) {
        this.cpucts = cpucts;
        this.results = new ArrayList<>();
        for (int i = 0; i < cpucts.size(); i++) {
            results.add(0.0);
        }
        var seeds = new ArrayList<Long>(numOfGames);
        for (int i = 0; i < numOfGames; i++) {
            seeds.add(origState.prop.random.nextLong(RandomGenCtx.Other));
        }
        var deq = new LinkedBlockingDeque<GameResult>();
        var numToPlay = new AtomicInteger(numOfGames);
        for (int i = 0; i < mcts.size(); i++) {
            startPlayGameThread2(origState, nodeCount, seeds, deq, numToPlay, i);
        }
//        var remoteServerGames = new HashMap<String, Integer>();
//        if (nodeCount > 1) {
//            for (Tuple<String, Integer> server : getRemoteServers()) {
//                remoteServerGames.putIfAbsent(server.v1() + ":" + server.v2(), 0);
//                startRemotePlayGameThread(server.v1(), server.v2(), modelDir, modelCmpDir, nodeCount, numToPlay, deq);
//            }
//        }

        var game_i = 0;
        var ret = getInfoMaps(origState);
        var combinedInfoMap = ret.v1();
        var battleInfoMap = ret.v2();
        var scenarioStats = new HashMap<Integer, ScenarioStats>();
        var start = System.currentTimeMillis();
        var solverErrorCount = 0;
        var progressInterval = ((int) Math.ceil(numOfGames / 1000f)) * 25;
        while (game_i < numOfGames) {
            GameResult result;
            try {
                result = deq.takeFirst();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            int r;
            if (result.remoteStats == null) {
                Game game = result.game;
                List<GameStep> steps = game.steps;
                List<GameStep> steps2 = result.game2 == null ? null : result.game2.steps;
                var state = steps.get(steps.size() - 1).state();
                r = game.preBattle_r * battleInfoMap.size() + game.battle_r;
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
                scenarioStats.computeIfAbsent(r, (k) -> new ScenarioStats()).add(game.steps, result.modelCalls);
                scenarioStats.get(r).add(game.steps, steps2, result.modelCalls2, result.reruns);
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
                                    matchLogWriter.write(step.state() + "\n");
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
            } else {
                r = result.remoteR;
                scenarioStats.computeIfAbsent(r, (k) -> new ScenarioStats()).add(result.remoteStats, origState);
//                remoteServerGames.computeIfPresent(result.remoteServer, (k, x) -> x + 1);
                game_i += 1;
            }

            if ((true && game_i % progressInterval == 0) || game_i == numOfGames) {
                System.out.println("Progress: " + game_i + "/" + numOfGames);
                if (!training && solver != null) {
                    System.out.println("Error Count: " + solverErrorCount);
                }
//                remoteServerGames.forEach((key, value) -> System.out.printf("Server %s: %d games\n", key, value));
                if (scenarioStats.size() > 1) {
                    for (var info : combinedInfoMap.entrySet()) {
                        var i = info.getKey();
                        if (scenarioStats.get(i) != null) {
                            System.out.println("Scenario " + info.getKey() + ": " + info.getValue().desc());
                            scenarioStats.get(i).printStats(origState, 4);
                        }
                    }
                }
                if (scenariosGroup != null) {
                    for (int i = 0; i < scenariosGroup.length; i++) {
                        System.out.println("Scenario " + IntStream.of(scenariosGroup[i]).mapToObj(String::valueOf).collect(Collectors.joining(", ")) + ": " + ScenarioStats.getCommonString(combinedInfoMap, scenariosGroup[i]));
                        var group = IntStream.of(scenariosGroup[i]).mapToObj(scenarioStats::get).filter(Objects::nonNull).toArray(ScenarioStats[]::new);
                        ScenarioStats.combine(group).printStats(origState, 4);
                    }
                }
                ScenarioStats.combine(scenarioStats.values().toArray(new ScenarioStats[0])).printStats(origState, 0);
                System.out.println("Time Taken: " + (System.currentTimeMillis() - start));
                for (int i = 0; i < mcts.size(); i++) {
                    var m = mcts.get(i);
                    System.out.println("Time Taken (By Model " + i + "): " + m.model.time_taken);
                    System.out.println("Model " + i + ": cache_size=" + m.model.cache.size() + ", " + m.model.cache_hits + "/" + m.model.calls + " hits (" + (double) m.model.cache_hits / m.model.calls + ")");
                }
                System.out.println("--------------------");
            }
        }

        var r = new ArrayList<Tuple<Double, Double>>();
        for (int i = 0; i < 16; i++) {
            r.add(new Tuple<>(cpucts.get(i), results.get(i)));
        }
        cpucts = null;
        results = null;
        return r;
    }

    // custom code that I modified if I want to check if/how fast network learned a strategy in a training run
    public static void getTrainingRunStats(String savesDir, GameState origState, int numOfGames, int nodeCount) {
        int iter = 0;
        do {
            System.out.println("**************************************");
            System.out.printf("Iteration %d\n", iter);
            MatchSession session = new MatchSession(1, Path.of(savesDir, "iteration" + iter).toString());
            session.playGamesForStat(origState, numOfGames, nodeCount);
            iter++;
        } while (Files.exists(Path.of(savesDir, "iteration" + iter)));
    }

    public void playGamesForStat(GameState origState, int numOfGames, int nodeCount) {
        var seeds = new ArrayList<Long>(numOfGames);
        for (int i = 0; i < numOfGames; i++) {
            seeds.add(origState.prop.random.nextLong(RandomGenCtx.Other));
        }
        var deq = new LinkedBlockingDeque<GameResult>();
        var numToPlay = new AtomicInteger(numOfGames);
        for (int i = 0; i < mcts.size(); i++) {
            startPlayGameThread(origState, nodeCount, seeds, deq, numToPlay, i);
        }

        var game_i = 0;
        var start = System.currentTimeMillis();
        var progressInterval = ((int) Math.ceil(numOfGames / 100f)) * 25;
        var averageQ = 0.0;
        while (game_i < numOfGames) {
            GameResult result;
            try {
                result = deq.takeFirst();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (result.remoteStats == null) {
                Game game = result.game;
                List<GameStep> steps = game.steps;
                var state = steps.get(steps.size() - 1).state();
                averageQ += state.get_q();
            }
            game_i += 1;
            if ((game_i % progressInterval == 0) || game_i == numOfGames) {
                System.out.println("Progress: " + game_i + "/" + numOfGames);
            }
        }
        System.out.println("Time Taken: " + (System.currentTimeMillis() - start));
        System.out.println("Average Q: " + averageQ / numOfGames);
    }
}
