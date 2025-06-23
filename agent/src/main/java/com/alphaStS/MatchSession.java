package com.alphaStS;

import com.alphaStS.model.Model;
import com.alphaStS.model.ModelExecutor;
import com.alphaStS.model.ModelPlain;
import com.alphaStS.utils.ScenarioStats;
import com.alphaStS.utils.Tuple;
import com.alphaStS.utils.Tuple3;
import com.alphaStS.utils.Utils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorOutputStream;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Math.sqrt;

public class MatchSession {
    private final static boolean LOG_GAME_USING_LINES_FORMAT = true;

    public enum PrintDamageLevel { NONE, ALL_SCENARIOS_COMBINED, GROUPED_SCENARIOS, INDIVIDUAL_SCENARIO }
    private PrintDamageLevel printDamageLevel = PrintDamageLevel.NONE;
    public void setPrintDamageLevel(PrintDamageLevel printDamageLevel) {
        this.printDamageLevel = printDamageLevel;
    }

    public HashMap<Integer, Integer> difficultyReachedByScenario = new HashMap<>();
    Writer matchLogWriter;
    Writer trainingDataWriter;
    List<MCTS> mcts = new ArrayList<>();
    List<MCTS> mctsCmp = new ArrayList<>();
    public int[][] scenariosGroup;
    String modelDir;
    ModelExecutor modelExecutor;
    String modelCmpDir;
    ModelExecutor modelExecutorCmp;
    GameState stateToCompare;
    int stateToCompareStartingAction = -1;
    private int producersCount;

    public MatchSession(String dir) {
        modelDir = dir;
        modelExecutor = new ModelExecutor(modelDir);
    }

    public void setModelComparison(String dirCmp, GameState state, int startingAction) {
        if (dirCmp != null) {
            modelCmpDir = dirCmp;
            modelExecutorCmp = new ModelExecutor(modelCmpDir);
            stateToCompare = state.clone(false);
            stateToCompare.properties = stateToCompare.properties.clone();
            stateToCompare.properties.biasedCognitionLimitCache = new ConcurrentHashMap<>(stateToCompare.properties.biasedCognitionLimitCache);
            stateToCompareStartingAction = startingAction;
        }
    }

    private void allocateThreadMCTS(int numberOfThreads, int batchSize) {
        producersCount = ModelExecutor.getNumberOfProducers(numberOfThreads, batchSize);
        if (modelCmpDir != null && !modelCmpDir.equals(modelDir)) {
            producersCount += ModelExecutor.getNumberOfProducers(numberOfThreads, batchSize);
        }
        if (producersCount >= mcts.size()) {
            for (int i = mcts.size(); i < producersCount; i++) {
                mcts.add(new MCTS(null));
                if (modelCmpDir != null) {
                    mctsCmp.add(new MCTS(null));
                }
            }
        }
        for (int i = 0; i < producersCount; i++) {
            Model model = modelExecutor.getModelForProducer(i);
            mcts.get(i).setModel(model);
            if (modelCmpDir != null) {
                mctsCmp.get(i).setModel(modelDir.equals(modelCmpDir) ? model : modelExecutorCmp.getModelForProducer(i));
            }
        }
    }

    int startingAction = -1;

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
        var refGameIdx = refGame == null ? 0 : findNextChanceAction(refGame, 1);
        if (state.properties.realMoveRandomGen != null) {
            state.setSearchRandomGen(state.properties.realMoveRandomGen.createWithSeed(state.properties.realMoveRandomGen.nextLong(RandomGenCtx.Misc)));
        } else {
            throw new RuntimeException("Bad State");
        }
        if (startingAction >= 0) {
            state.properties.makingRealMove = true;
            state = state.doAction(startingAction);
            state.properties.makingRealMove = false;
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
                    state.properties.makingRealMove = true;
                    if (state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
                        state = state.clone(false);
                        state = state.doAction(0);
                    } else if (state.actionCtx == GameActionCtx.BEGIN_PRE_BATTLE) {
                        state = state.clone(false);
                        state = state.doAction(0);
                    } else {
                        if (nodeCount == 1) {
                            state = state.clone(false);
                            state = state.doAction(action);
                        } else {
                            state = getNextState(state, mcts, action, steps.get(steps.size() - 1), false);
                        }
                    }
                    state.properties.makingRealMove = false;
                    RefRet ret = syncWithRef(refGame, refGameIdx, steps, state, action);
                    if (ret != null) {
                        state = ret.state;
                        refGameIdx = ret.refGameIdx;
                    }
                    steps.get(steps.size() - 1).state().clearNextStates();
                }
                if (state.actionCtx == GameActionCtx.BEGIN_TURN) {
                    steps.add(new GameStep(state, 0));
                    state = state.clone(false);
                    state = state.doAction(0);
                    RefRet ret = syncWithRef(refGame, refGameIdx, steps, state, 0);
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

                int action = MCTS.getActionWithMaxNodesOrTerminal(state);
                steps.add(new GameStep(state, action));
                state.properties.makingRealMove = true;
                if (state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
                    state = state.clone(false);
                    state = state.doAction(0);
                } else if (state.actionCtx == GameActionCtx.BEGIN_PRE_BATTLE) {
                    state = state.clone(false);
                    state = state.doAction(0);
                } else {
                    if (nodeCount == 1) {
                        state = state.clone(false);
                        state = state.doAction(action);
                        if (state.actionCtx == GameActionCtx.BEGIN_TURN) {
                            state = state.doAction(0);
                        }
                    } else {
                        state = getNextState(state, mcts, action, steps.get(steps.size() - 1), false);
                    }
                }
                state.properties.makingRealMove = false;
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
        return new Game(steps, state.preBattleRandomizationIdxChosen, state.battleRandomizationIdxChosen, null, 0, 0, null);
    }

    // when comparing game searchRandomGen can get out of sync, make sure it's synced as much as possible
    private RefRet syncWithRef(Game refGame, int refGameIdx, ArrayList<GameStep> steps, GameState state, int action) {
        if (!Configuration.NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION && state.isStochastic && refGame != null && refGameIdx < refGame.steps().size()) {
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
                state.properties = prevState.properties;
                state.clearAllSearchInfo();
                state.setSearchRandomGen(state.getSearchRandomGen().createWithSeed(refState.getSearchRandomGen().getStartingSeed()));
            }
            refGameIdx = findNextChanceAction(refGame, refGameIdx + 2);
            return new RefRet(state, refGameIdx);
        }
        return null;
    }

    public static record Game(List<GameStep> steps, int preBattle_r, int battle_r, List<GameStep> augmentedSteps, int noTemperatureTurn, int difficulty, Tuple3<Long, Long, Long> aa) {}
    public static record GameResult(Game game, int modelCalls, Game game2, int modelCalls2, long seed, List<GameResult> reruns, String remoteServer, int remoteR, ScenarioStats remoteStats, String remoteGameRecord) {}
    public static record TrainingGameResult(Game game, String remoteServer, String remoteTrainingGameRecord, byte[] remoteTrainingData) {}

    private boolean playGamesPause;
    private boolean playGamesStop;

    public List<Game> playGames(GameState origState, int numOfGames, int nodeCount, int numOfThreads, int batchCount, boolean printProgress, boolean returnGames) throws IOException {
        var seeds = new ArrayList<Long>(numOfGames);
        for (int i = 0; i < numOfGames; i++) {
            seeds.add(origState.properties.random.nextLong(RandomGenCtx.Other));
        }
        var deq = new LinkedBlockingDeque<GameResult>();
        var numToPlay = new AtomicInteger(numOfGames);
        modelExecutor.start(numOfThreads, batchCount);
        if (modelExecutorCmp != null) {
            modelExecutorCmp.start(numOfThreads, batchCount);
        }
        allocateThreadMCTS(numOfThreads, batchCount);
        for (int i = 0; i < producersCount; i++) {
            startPlayGameThread(origState, nodeCount, seeds, deq, numToPlay, i);
        }
        var remoteServerGames = new HashMap<String, Integer>();
        for (Tuple<String, Integer> server : getRemoteServers()) {
            if (nodeCount > 1 || server.v1().startsWith("192.168.1.")) {
                remoteServerGames.putIfAbsent(server.v1() + ":" + server.v2(), 0);
                startRemotePlayGameThread(server.v1(), server.v2(), modelDir, modelCmpDir, nodeCount, numToPlay, deq);
            }
        }

        var game_i = new AtomicInteger(0);

        new Thread(() -> {
            var reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                while (game_i.get() < numToPlay.get()) {
                    if (!reader.ready()) {
                        Utils.sleep(200);
                        continue;
                    }
                    var line = reader.readLine();
                    if (line.equals("exit")) {
                        System.out.println("Stop");
                        playGamesStop = true;
                        break;
                    } else if (line.equals("pause")) {
                        System.out.println("Paused");
                        playGamesPause = true;
                    } else if (line.equals("start")) {
                        System.out.println("Restarted");
                        playGamesPause = false;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        var ret = getInfoMaps(origState.properties);
        var combinedInfoMap = ret.v1();
        var battleInfoMap = ret.v2();
        var scenarioStats = new HashMap<Integer, ScenarioStats>();
        var chanceNodeStats = new HashMap<GameState, ScenarioStats>();
        var games = new ArrayList<Game>();
        var start = System.currentTimeMillis();
        var progressInterval = ((int) Math.ceil(numOfGames / 1000f)) * 25;
        var lastPrintTime = System.currentTimeMillis();
        while (game_i.get() < numOfGames && !playGamesStop) {
            GameResult result;
            try {
                result = deq.takeFirst();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            int r = 0;
            if (result.remoteStats == null) {
                Game game = result.game;
                if (returnGames) {
                    games.add(game);
                }
                List<GameStep> steps = game.steps;
                List<GameStep> steps2 = result.game2 == null ? null : result.game2.steps;
                if (game.preBattle_r >= 0) {
                    r = game.preBattle_r;
                }
                if (game.battle_r >= 0) {
                    r = r * battleInfoMap.size() + game.battle_r ;
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
                                    ts1 = ts1.doAction(j);
                                    break;
                                }
                            }
                        }
                        if (ts2.actionCtx != GameActionCtx.BEGIN_TURN) {
                            for (int j = 0; j < ts2.getLegalActions().length; j++) {
                                if (ts2.getAction(j).type() == GameActionType.END_TURN) {
                                    ts2 = ts2.doAction(j);
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
                scenarioStats.computeIfAbsent(r, (k) -> new ScenarioStats(origState.properties)).add(game.steps, result.modelCalls);
                scenarioStats.get(r).add(game.steps, steps2, result.modelCalls2, result.reruns);
                if (scenarioStats.size() == 1 && startingAction >= 0 && game.steps.get(0).state().isStochastic) {
                    chanceNodeStats.computeIfAbsent(game.steps.get(0).state(), (k) -> new ScenarioStats(origState.properties)).add(game.steps, result.modelCalls);
                    chanceNodeStats.get(game.steps.get(0).state()).add(game.steps, steps2, result.modelCalls2, result.reruns);
                }
                game_i.incrementAndGet();
                if (matchLogWriter != null) {
                    matchLogWriter.write("*** Match " + game_i + " ***\n");
                    writeGameRecord(matchLogWriter, result, steps, combinedInfoMap, r);
                }
            } else {
                r = result.remoteR;
                scenarioStats.computeIfAbsent(r, (k) -> new ScenarioStats(origState.properties)).add(result.remoteStats);
                remoteServerGames.computeIfPresent(result.remoteServer, (k, x) -> x + 1);
                if (matchLogWriter != null) {
                    matchLogWriter.write("*** Match " + game_i + " (Remote) ***\n");
                    matchLogWriter.write(result.remoteGameRecord);
                }
                game_i.incrementAndGet();
            }

            if ((printProgress && game_i.get() % progressInterval == 0) || game_i.get() == numOfGames || System.currentTimeMillis() - lastPrintTime > 60 * 1000 || playGamesStop) {
                lastPrintTime = System.currentTimeMillis();
                System.out.println("Progress: " + game_i + "/" + numOfGames);
                remoteServerGames.forEach((key, value) -> System.out.printf("Server %s: %d games\n", key, value));
                if (scenarioStats.size() > 1) {
                    for (var info : combinedInfoMap.entrySet()) {
                        var i = info.getKey();
                        if (scenarioStats.get(i) != null) {
                            System.out.println("Scenario " + info.getKey() + ": " + info.getValue().desc());
                            scenarioStats.get(i).printStats(origState, printDamageLevel.compareTo(PrintDamageLevel.INDIVIDUAL_SCENARIO) >= 0 && game_i.get() == numOfGames , 4);
                        }
                    }
                } else if (startingAction >= 0 && chanceNodeStats.size() > 1 && chanceNodeStats.size() < 10) {
                    for (var stats : chanceNodeStats.entrySet()) {
                        System.out.println("Random Event: " + stats.getKey().getStateDesc());
                        stats.getValue().printStats(origState, printDamageLevel.compareTo(PrintDamageLevel.INDIVIDUAL_SCENARIO) >= 0 && game_i.get() == numOfGames , 4);
                    }
                }
                if (scenariosGroup != null) {
                    for (int i = 0; i < scenariosGroup.length; i++) {
                        System.out.println("Scenario " + IntStream.of(scenariosGroup[i]).mapToObj(String::valueOf).collect(Collectors.joining(", ")) + ": " + ScenarioStats.getCommonString(combinedInfoMap, scenariosGroup[i]));
                        var group = IntStream.of(scenariosGroup[i]).mapToObj(scenarioStats::get).filter(Objects::nonNull).toArray(ScenarioStats[]::new);
                        ScenarioStats.combine(origState.properties, group).printStats(origState, printDamageLevel.compareTo(PrintDamageLevel.GROUPED_SCENARIOS) >= 0 && game_i.get() == numOfGames , 4);
                    }
                }
                ScenarioStats.combine(origState.properties, scenarioStats.values().toArray(new ScenarioStats[0])).printStats(origState, printDamageLevel.compareTo(PrintDamageLevel.ALL_SCENARIOS_COMBINED) >= 0 && game_i.get() == numOfGames, 0);
                System.out.println("Time Taken: " + (System.currentTimeMillis() - start));
                var modelsPrint = modelExecutor.getExecutorModels();
                for (int i = 0; i < modelsPrint.size(); i++) {
                    var m = (ModelPlain) modelsPrint.get(i);
                    System.out.println("Time Taken (By Model " + i + "): " + m.time_taken);
                    System.out.println("Model " + i + ": cache_size=" + m.cache.size() + ", " + m.cache_hits + "/" + m.calls + " hits (" + (double) m.cache_hits / m.calls + ")");
                }
                System.out.println("--------------------");
            }
        }
        if (modelExecutorCmp != null) {
            modelExecutorCmp.setToNotRunning();
        }
        modelExecutor.stop();
        if (modelExecutorCmp != null) {
            modelExecutorCmp.stop();
        }
        return games;
    }

    private void writeGameRecord(Writer writer, GameResult result, List<GameStep> steps, Map<Integer, GameStateRandomization.Info> combinedInfoMap, int r) throws IOException {
        var state = steps.get(steps.size() - 1).state();
        int damageTaken = state.getPlayeForRead().getOrigHealth() - state.getPlayeForRead().getHealth();
        if (state.properties.randomization != null) {
            if (combinedInfoMap.size() > 1) {
                writer.write("Scenario: " + combinedInfoMap.get(r).desc() + "\n");
            }
        }
        writer.write("Result: " + (state.isTerminal() == 1 ? "Win" : "Loss") + "\n");
        writer.write("Damage Taken: " + damageTaken + "\n");
        writer.write("Seed: " + result.seed + "\n");
        boolean usingLine = steps.stream().anyMatch((s) -> s.lines != null);
        if (usingLine && LOG_GAME_USING_LINES_FORMAT) {
            for (GameStep step : steps) {
                if (step.state().actionCtx == GameActionCtx.BEGIN_TURN) continue;
                if (step.lines != null) {
                    writer.write(step.state().toString() + "\n");
                    for (int i = 0; i < Math.min(step.lines.size(), 5); i++) {
                        writer.write("  " + (i + 1) + ". " + step.lines.get(i) + "\n");
                    }
                }
            }
        } else {
            printGame(writer, steps);
        }
        writer.write("\n");
        writer.write("\n");
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
                            deq.putLast(new GameResult(null, 0, null, 0, 0, null, ip + ":" + port, ret[idx].r, ret[idx].stats, ret[idx].gameRecord));
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

    public static record RemoteGameResult(int r, ScenarioStats stats, String gameRecord) {}

    AtomicInteger remoteNumOfGames = new AtomicInteger(-123456);
    BlockingDeque<GameResult> remoteDeq = new LinkedBlockingDeque<>();

    public RemoteGameResult playGamesRemote(GameState origState, int numOfGames, int nodeCount, int numOfThreads, int batchCount) throws IOException {
        if (remoteNumOfGames.get() == -123456) {
            remoteNumOfGames.set(numOfGames);
            var seeds = new ArrayList<Long>(numOfGames);
            for (int i = 0; i < numOfGames; i++) {
                seeds.add(origState.properties.random.nextLong(RandomGenCtx.Other));
            }
            modelExecutor.start(numOfThreads, batchCount);
            allocateThreadMCTS(numOfThreads, batchCount);
            for (int i = 0; i < producersCount; i++) {
                startPlayGameThread(origState, nodeCount, seeds, remoteDeq, remoteNumOfGames, i);
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
        var stat = new ScenarioStats(origState.properties);
        stat.add(steps, result.modelCalls);
        stat.add(steps, steps2, result.modelCalls2, result.reruns);
        var ret = getInfoMaps(origState.properties);
        var battleInfoMap = ret.v2();
        var r = game.preBattle_r * battleInfoMap.size() + game.battle_r;
        var gameRecordWriter = new StringWriter();
        writeGameRecord(gameRecordWriter, result, steps, ret.v1(), r);
        return new RemoteGameResult(r, stat, gameRecordWriter.toString());
    }

    public void stopPlayGamesRemote() {
        remoteNumOfGames.set(-123456);
        modelExecutor.stop();
        remoteDeq.clear();
    }

    public void close() {
        modelExecutor.close();
        if (modelExecutorCmp != null && !modelDir.equals(modelCmpDir)) {
            modelExecutorCmp.close();
        }
    }

    private void startPlayGameThread(GameState origState, int nodeCount, ArrayList<Long> seeds, BlockingDeque<GameResult> deq, AtomicInteger numToPlay, int threadIdx) {
        var session = this;
        modelExecutor.addAndStartProducerThread(() -> {
            int idx = numToPlay.getAndDecrement();
            try {
                while (idx > 0) {
                    if (playGamesStop) break;
                    if (deq.size() >= (nodeCount == 1 ? 1000 : 40) || playGamesPause) {
                        if (numToPlay.get() <= -123456) {
                            break; // only when this a remote server thread
                        }
                        modelExecutor.sleep(200);
                        continue;
                    }
                    var state = origState.clone(false);
                    state.properties = state.properties.clone();
                    state.properties.currentMCTS = mcts.get(threadIdx);
                    state.properties.doingComparison = mctsCmp.size() > 0;
                    var randomGen = new RandomGen.RandomGenByCtx(seeds.get(idx - 1));
                    state.properties.realMoveRandomGen = randomGen;
                    randomGen.useNewCommonNumberVR = true;
                    state.properties.testNewFeature = true;
                    mcts.get(threadIdx).model.startRecordCalls();
                    var game1 = session.playGame(state, startingAction, null, mcts.get(threadIdx), nodeCount);
                    var modelCalls = mcts.get(threadIdx).model.endRecordCalls();
                    Game game2 = null;
                    var modelCalls2 = 0;
                    List<GameResult> reruns = null;
                    if (mctsCmp.size() > 0) {
                        randomGen.timeTravelToBeginning();
                        var state2 = stateToCompare.clone(false);
                        state2.properties = state2.properties.clone();
                        state2.properties.currentMCTS = mcts.get(threadIdx);
                        state2.properties.doingComparison = true;
                        state2.properties.realMoveRandomGen = randomGen;
                        randomGen.useNewCommonNumberVR = false;
                        state2.properties.testNewFeature = false;
                        mctsCmp.get(threadIdx).model.startRecordCalls();
                        game2 = session.playGame(state2, stateToCompareStartingAction, game1, mctsCmp.get(threadIdx), nodeCount);
                        modelCalls2 = mctsCmp.get(threadIdx).model.endRecordCalls();

                        var turns1 = GameStateUtils.groupByTurns(game1.steps);
                        var turns2 = GameStateUtils.groupByTurns(game2.steps);
                        for (int turnI = 1; turnI < Math.min(turns1.size(), turns2.size()); turnI++) {
                            var t1 = turns1.get(turnI);
                            var t2 = turns2.get(turnI);
                            var ts1 = t1.get(t1.size() - 1).state().clone(false);
                            var ts2 = t2.get(t2.size() - 1).state().clone(false);
                            for (int j = 0; j < ts1.getLegalActions().length; j++) {
                                if (ts1.getAction(j).type() == GameActionType.END_TURN) {
                                    ts1 = ts1.doAction(j);
                                    break;
                                }
                            }
                            for (int j = 0; j < ts2.getLegalActions().length; j++) {
                                if (ts2.getAction(j).type() == GameActionType.END_TURN) {
                                    ts2 = ts2.doAction(j);
                                    break;
                                }
                            }
                            if (!ts1.equals(ts2)) {
                                reruns = new ArrayList<>();
                                for (int j = 0; j < Configuration.CMP_DEVIATION_NUM_RERUN; j++) {
                                    var rerunState = ts1.clone(false);
                                    rerunState.properties = rerunState.properties.clone();
                                    randomGen = new RandomGen.RandomGenByCtx(state.properties.realMoveRandomGen.nextLong(RandomGenCtx.Misc));
                                    rerunState.properties.realMoveRandomGen = randomGen;
                                    randomGen.useNewCommonNumberVR = true;
                                    var rerunGame1 = session.playGame(rerunState, -1, null, mcts.get(threadIdx), nodeCount);
                                    Game rerunGame2 = null;
                                    if (mctsCmp.size() > 0) {
                                        randomGen.useNewCommonNumberVR = false;
                                        randomGen.timeTravelToBeginning();
                                        var rerunState2 = ts2.clone(false);
                                        rerunState2.properties = rerunState2.properties.clone();
                                        rerunState2.properties.realMoveRandomGen = randomGen;
                                        rerunGame2 = session.playGame(rerunState2, -1, game1, mctsCmp.get(threadIdx), nodeCount);
                                    }
                                    reruns.add(new GameResult(rerunGame1, 0, rerunGame2, 0, 0, null, null,0, null, null));
                                }
                                break;
                            }
                        }
                    }

                    deq.putLast(new GameResult(game1, modelCalls, game2, modelCalls2, seeds.get(idx - 1), reruns, null,0, null, null));
                    idx = numToPlay.getAndDecrement();
                    if (nodeCount > 1) {
                        modelExecutor.sleep(Configuration.SLEEP_PER_GAME);
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                System.out.println("Seed failed on: " + seeds.get(idx - 1));
                throw e;
            }
            waitForBatchExecutorToFinish(threadIdx);
        });
    }

    private void waitForBatchExecutorToFinish(int threadIdx) {
        while (!modelExecutor.producerWaitForClose(threadIdx) || (modelExecutorCmp != null && !modelDir.equals(modelCmpDir) && !modelExecutorCmp.producerWaitForClose(threadIdx)));
    }

    public Tuple<Map<Integer, GameStateRandomization.Info>, Map<Integer, GameStateRandomization.Info>> getInfoMaps(GameProperties properties) {
        Map<Integer, GameStateRandomization.Info> preBattleInfoMap = new HashMap<>();
        if (properties.preBattleRandomization != null) {
            preBattleInfoMap = properties.preBattleRandomization.listRandomizations();
        } else {
            preBattleInfoMap.put(0, new GameStateRandomization.Info(1, ""));
        }
        Map<Integer, GameStateRandomization.Info> battleInfoMap = new HashMap<>();
        if (properties.randomization != null) {
            battleInfoMap = properties.randomization.listRandomizations();
        } else {
            battleInfoMap.put(0, new GameStateRandomization.Info(1, ""));
        }

        var combinedInfoMap = new HashMap<Integer, GameStateRandomization.Info>();
        for (int preBattleScenarioIndex : preBattleInfoMap.keySet().stream().mapToInt(x -> x).sorted().toArray()) {
            for (int battleScenarioIndex : battleInfoMap.keySet().stream().mapToInt(x -> x).sorted().toArray()) {
                var chance = preBattleInfoMap.get(preBattleScenarioIndex).chance() * battleInfoMap.get(battleScenarioIndex).chance();
                String desc;
                if (preBattleInfoMap.get(preBattleScenarioIndex).desc().length() == 0) {
                    desc = battleInfoMap.get(battleScenarioIndex).desc();
                } else if (battleInfoMap.get(battleScenarioIndex).desc().length() == 0) {
                    desc = preBattleInfoMap.get(preBattleScenarioIndex).desc();
                } else {
                    desc = preBattleInfoMap.get(preBattleScenarioIndex).desc() + ", " + battleInfoMap.get(battleScenarioIndex).desc();
                }
                combinedInfoMap.put(preBattleScenarioIndex * battleInfoMap.size() + battleScenarioIndex, new GameStateRandomization.Info(chance, desc));
            }
        }
        return new Tuple<>(combinedInfoMap, battleInfoMap);
    }

    boolean USE_Z_TRAINING;
    boolean POLICY_CAP_ON;

    private VArray calcExpectedValue(ChanceState cState, GameState generatedState, MCTS mcts, VArray vCur) {
        var stateActual = generatedState == null ? null : cState.addGeneratedState(generatedState);
        while (cState.total_n < 10000 && cState.cache.size() < 200) {
            cState.getNextState(false, -1);
        }

        VArray est = new VArray(vCur.length());
        for (ChanceState.Node node : cState.cache.values()) {
            if (node.state != stateActual) {
                if (node.state.policy == null) {
                    node.state.doEval(mcts.model);
                }
                var out = node.state.getVArrayCached();
                est.trainingSetExpectedValueStage1(out, node.n);
            }
        }
        float p = generatedState == null ? 0 : ((float) cState.getCount(stateActual)) / cState.total_node_n;
        est.trainingSetExpectedValueStage2(cState.total_node_n, vCur, p);
        return est;
    }

    private Game playTrainingGame(GameState origState, int nodeCount, MCTS mcts, long seed) {
        var steps = new ArrayList<GameStep>();
        var augmentedSteps = new ArrayList<GameStep>();
        var state = origState.clone(false);
        state.properties = state.properties.clone();
        state.properties.currentMCTS = mcts;
        boolean noMoreExplore = false;
        state.properties.realMoveRandomGen = new RandomGen.RandomGenByCtx(seed);
        state.setSearchRandomGen(state.properties.realMoveRandomGen.createWithSeed(state.properties.realMoveRandomGen.nextLong(RandomGenCtx.Misc)));
        boolean doNotExplore = state.getSearchRandomGen(true).nextFloat(RandomGenCtx.Other) < Configuration.TRAINING_PERCENTAGE_NO_TEMPERATURE;
        int noTemperatureTurn = doNotExplore ? 0 : -1;

        state.doEval(mcts.model);
        boolean quickPass = false;
        int turnsToSkip = -1;
        if (Configuration.TRAINING_SKIP_OPENING_TURNS && Configuration.TRAINING_SKIP_OPENING_TURNS_UPTO > 0) {
            turnsToSkip = state.getSearchRandomGen(true).nextInt(Configuration.TRAINING_SKIP_OPENING_TURNS_UPTO + 1, RandomGenCtx.Other);
        }
        int prevTurnNum = 1;
        var trainingGameStart = System.currentTimeMillis();
        while (state.isTerminal() == 0) {
            int turnNodeCount = quickPass ? nodeCount / 4 : nodeCount;
            int todo = turnNodeCount - state.total_n;
            if (!doNotExplore && state.realTurnNum <= turnsToSkip) {
                todo = 1;
            }
            if (!doNotExplore && !noMoreExplore && state.realTurnNum != prevTurnNum && state.getSearchRandomGen(true).nextFloat(RandomGenCtx.Other) < 0.02) {
                noTemperatureTurn = prevTurnNum;
                noMoreExplore = true;
            }
            RandomGen randomGenClone = state.getSearchRandomGen().getCopy();
            prevTurnNum = state.realTurnNum;
            int[] nWhenSearchHalfDone = null;
            for (int i = 0; i < todo; i++) {
                mcts.search(state, !quickPass, todo - i);
                if (mcts.numberOfPossibleActions == 1 && state.total_n >= 1) {
//                    if (state.isStochastic && states.size() > 0) {
//                        state = getNextState(states.get(states.size() - 1).state(), mcts, states.get(states.size() - 1).action(), false);
//                    }
                    break;
                }
//                if (state.total_n == turnNodeCount / 2 && state.n != null) {
//                    nWhenSearchHalfDone = Arrays.copyOf(state.n, state.n.length);
//                    if (Utils.calcKLDivergence(Utils.nArrayToPolicy(nWhenSearchHalfDone), state.policy) < 0.04) {
//                        break;
//                    }
//                }
            }

            int action;
            int greedyAction;
            if (!doNotExplore && Configuration.TRAINING_SKIP_OPENING_TURNS && state.realTurnNum <= turnsToSkip) {
                double t = state.getSearchRandomGen(true).nextFloat(RandomGenCtx.Other);
                double k = 0;
                int i;
                for (i = 0; i < state.policy.length; i++) {
                    k += state.policy[i];
                    if (k >= t) {
                        break;
                    }
                }
                action = i;
                greedyAction = i;
            } else if (state.actionCtx == GameActionCtx.SELECT_SCENARIO) {
                action = MCTS.getActionRandomOrTerminalSelectScenario(state);
                greedyAction = MCTS.getActionWithMaxNodesOrTerminal(state);
            } else if (doNotExplore || noMoreExplore || quickPass || state.realTurnNum >= 100) {
                action = MCTS.getActionWithMaxNodesOrTerminal(state);
                greedyAction = action;
            } else {
                action = mcts.getActionRandomOrTerminal(state, false);
                greedyAction = MCTS.getActionWithMaxNodesOrTerminal(state);
            }
            var step = new GameStep(state, action);
            step.trainingWriteCount = !quickPass ? 1 : 0;
            step.isExplorationMove = greedyAction != action;
            step.searchRandomGenMCTS = randomGenClone;
            step.nWhenSearchHalfDone = nWhenSearchHalfDone;
            if (!doNotExplore && state.realTurnNum <= turnsToSkip) {
                step.trainingWriteCount = 0;
                step.trainingSkipOpening = true;
            }
            steps.add(step);
            if (state.getAction(action).type() == GameActionType.END_TURN) {
                quickPass = POLICY_CAP_ON && state.getSearchRandomGen(true).nextInt(4, RandomGenCtx.Other, null) > 0;
            }
            if (state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
                state = state.clone(false);
                state.properties.makingRealMove = true;
                state = state.doAction(0);
                state.properties.makingRealMove = false;
            } else if (state.actionCtx == GameActionCtx.BEGIN_PRE_BATTLE) {
                state = state.clone(false);
                state.properties.makingRealMove = true;
                state = state.doAction(0);
                state.properties.makingRealMove = false;
            } else {
                state.properties.makingRealMove = true;
                state = getNextState(state, mcts, action, !quickPass);
                state.properties.makingRealMove = false;
            }
        }
        steps.add(new GameStep(state, -1));
        var trainingGameEnd = System.currentTimeMillis();

        // do scoring here before clearing states to reuse nn eval if possible
        var vLen = state.properties.v_total_len;
        VArray vCur = new VArray(vLen);
        VArray vPro = new VArray(vLen);
        state.getVArray(vCur);
        state.getVArray(vPro);
        if (state.isStochastic) {
            var prevStep = steps.get(steps.size() - 2);
            vCur = calcExpectedValue((ChanceState) prevStep.state().ns[prevStep.action()], null, mcts, vCur);
            vPro = vCur.copy();
        }
        ChanceState lastChanceState = null;
        for (int i = steps.size() - 2; i >= 0; i--) {
            int isBetter = 0;
            if (!USE_Z_TRAINING && steps.get(i).isExplorationMove) {
                List<GameStep> extraSteps = new ArrayList<>();
                ChanceState cState = findBestLineChanceState(steps.get(i).state(), nodeCount, mcts, extraSteps);
                // taking the max of 2 random variable inflates eval slightly, so we try to make calcExpectedValue have a large sample
                // to reduce this effect, seems ok so far, also check if we are transposing and skip for transposing
                if (cState != null && !cState.equals(lastChanceState)) {
                    VArray ret = calcExpectedValue(cState, null, mcts, new VArray(vLen));
                    if (lastChanceState != null || ret.get(GameState.V_COMB_IDX) > vCur.get(GameState.V_COMB_IDX)) {
                        if (ret.get(GameState.V_COMB_IDX) > vCur.get(GameState.V_COMB_IDX)) {
                            isBetter = -1;
                        }
                        state = steps.get(i).state().clone(false);
                        state.setSearchRandomGen(steps.get(i).searchRandomGenMCTS);
                        state.policyMod = steps.get(i).state().policyMod;
                        mcts.exploredActions = new HashMap<>();
                        mcts.exploredActions.put(steps.get(i).action(), vCur);
                        mcts.exploredActions.put(MCTS.getActionWithMaxNodesOrTerminal(steps.get(i).state()), ret);
                        for (int j = 0; j < nodeCount; j++) {
                            mcts.search(state, true, nodeCount - j);
                        }
                        mcts.exploredActions = null;
                        state.isStochastic = steps.get(i).state().isStochastic;
                        steps.get(i).setState(state);

                        vCur = ret;
                        vPro = ret.copy();
                        lastChanceState = cState;
                    } else {
                        isBetter = 1;
                    }
//                    for (GameStep step : extraSteps) {
//                        step.v = ret;
//                        step.trainingWriteCount = 1;
//                    }
//                    augmentedSteps.addAll(extraSteps);
                }
            }
            steps.get(i).v = vPro.copy();
            if (Configuration.TRAINING_EXPERIMENT_USE_UNCERTAINTY_FOR_EXPLORATION) {
                if (!USE_Z_TRAINING && steps.get(i).isExplorationMove && steps.get(i + 1).v != null) {
                    var vWin = steps.get(i + 1).v.getVExtra(state.properties.qwinVExtraIdx) - (-10);
                    if (isBetter < 0) {
                        steps.get(i + 1).v.setVExtra(state.properties.qwinVExtraIdx, 0.75 * vWin);
                    } else if (isBetter > 0) {
                        steps.get(i + 1).v.setVExtra(state.properties.qwinVExtraIdx, 1 - 0.75 * (1 - vWin));
                    }
                }
                steps.get(i).v.setVExtra(state.properties.qwinVExtraIdx, -10 + steps.get(i).state().getVExtra(state.properties.qwinVExtraIdx));
            }
            state = steps.get(i).state();
            state.clearNextStates();
            if (state.isStochastic && i > 0) {
                if (steps.get(i - 1).trainingSkipOpening || steps.get(i - 1).state().getActionCtx() == GameActionCtx.BEGIN_PRE_BATTLE) {
                    break;
                }
                var prevState = steps.get(i - 1).state();
                var prevAction = steps.get(i - 1).action();
                var cState = (ChanceState) prevState.ns[prevAction];
                lastChanceState = new ChanceState(null, cState.parentState, cState.parentAction);

                if (!USE_Z_TRAINING) {
                    vCur = calcExpectedValue(cState, state, mcts, vCur);
                    vPro.trainingDiscountReward(vCur);
                }
            }
        }
        if (steps.get(0).state().actionCtx == GameActionCtx.BEGIN_BATTLE || steps.get(0).state().actionCtx == GameActionCtx.BEGIN_PRE_BATTLE) {
            steps.get(0).trainingWriteCount = 0;
        }
        var postProcessingEnd = System.currentTimeMillis();

        state = steps.get(steps.size() - 1).state();
        return new Game(steps, state.preBattleRandomizationIdxChosen, state.battleRandomizationIdxChosen, augmentedSteps, noTemperatureTurn, state.properties.difficultyChosen, new Tuple3<>(trainingGameStart, trainingGameEnd, postProcessingEnd));
    }

    private double calcKld(GameState state) {
        if (state.n == null || state.policy == null) {
            throw new IllegalArgumentException();
        }
        return state.terminalAction >= 0 ? 0 : Math.abs(Utils.calcKLDivergence(Utils.nArrayToPolicy(state.n), state.policy));
    }

    private ChanceState findBestLineChanceState(GameState state, int nodeCount, MCTS mcts, List<GameStep> augmentedSteps) {
        ChanceState cs1 = null;
        GameState ss = state;
        while (true) {
            if (ss == null || ss.isTerminal() != 0) {
                break;
            }
            int action = MCTS.getActionWithMaxNodesOrTerminal(ss);
            if (ss.ns == null) {
                break;
            }
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
            if (Configuration.BAN_TRANSPOSITION_IN_TREE) {
                state.clearAllSearchInfo();
            }
            int todo = nodeCount - state.total_n;
            for (int i = 0; i < todo; i++) {
                mcts.search(state, false, todo - i);
                if (mcts.numberOfPossibleActions == 1 && state.total_n >= 1) {
                    break;
                }
            }

            int action = MCTS.getActionWithMaxNodesOrTerminal(state);
            if (!firstState) {
                augmentedSteps.add(new GameStep(state, action));
            }
            firstState = false;
            if (state.ns[action] instanceof ChanceState cs) {
                cs2 = new ChanceState(null, cs.parentState, cs.parentAction);
                break;
            }
            state = (GameState) state.ns[action];
        }
        return cs2;
    }

    public void playTrainingGames(GameState origState, int numOfGames, int nodeCount, int numOfThreads, int batchCount, String path) throws IOException {
        File file = new File(path);
        file.delete();
        if (Configuration.TRAINING_SKIP_OPENING_TURNS) {
            numOfGames = (int) (numOfGames * Configuration.TRAINING_SKIP_OPENING_GAMES_INCREASE_RATIO);
        }
        var seeds = new ArrayList<Long>(numOfGames);
        for (int i = 0; i < numOfGames; i++) {
            seeds.add(origState.properties.random.nextLong(RandomGenCtx.Other));
        }
        var stream = new DataOutputStream(new FramedLZ4CompressorOutputStream(new BufferedOutputStream(new FileOutputStream(path))));
        var deq = new LinkedBlockingDeque<TrainingGameResult>();
        var session = this;
        var numToPlay = new AtomicInteger(numOfGames);
        modelExecutor.start(numOfThreads, batchCount);
        allocateThreadMCTS(numOfThreads, batchCount);
        for (int i = 0; i < producersCount; i++) {
            int _i = i;
            modelExecutor.addAndStartProducerThread(() -> {
                var state = origState.clone(false);
                int idx = numToPlay.getAndDecrement();
                while (idx > 0) {
                    try {
                        deq.putLast(new TrainingGameResult(session.playTrainingGame(state, nodeCount, mcts.get(_i), seeds.get(idx - 1)), null, null, null));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        System.out.println("Seed failed on: " + seeds.get(idx - 1));
                        throw e;
                    }
                    modelExecutor.sleep(Configuration.SLEEP_PER_GAME_TRAINING);
                    idx = numToPlay.getAndDecrement();
                }
                waitForBatchExecutorToFinish(_i);
            });
        }
        var remoteServerGames = new HashMap<String, Integer>();
        for (Tuple<String, Integer> server : getRemoteServers()) {
            remoteServerGames.putIfAbsent(server.v1() + ":" + server.v2(), 0);
            startRemotePlayTrainingGameThread(server.v1(), server.v2(), nodeCount, numToPlay, deq, origState);
        }

        var trainingGame_i = 0;
        var positionsCount = 0;
        var trainingGameTime = 0;
        var postProcessingTime = 0;
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
                if (steps.get(steps.size() - 1).state().isTerminal() > 0) {
                    var scenarioIdx = steps.get(steps.size() - 1).state().getScenarioIdxChosen();
                    difficultyReachedByScenario.merge(scenarioIdx, game.difficulty, Math::max);
                }
                changeWritingCountBasedOnPolicySurprise(steps);
                if (trainingDataWriter != null && trainingGame_i <= 100) {
                    trainingDataWriter.write("*** Match " + trainingGame_i + " ***\n");
                    writeTrainingGameRecord(trainingDataWriter, origState, game, steps);
                }
                if (Configuration.TRAINING_USE_FORCED_PLAYOUT) {
                    pruneForcedPlayouts(steps);
                }
                positionsCount += writeTrainingData(stream, steps);
                positionsCount += writeTrainingData(stream, game.augmentedSteps);
                trainingGameTime += game.aa.v2() - game.aa.v1();
                postProcessingTime += game.aa.v3() - game.aa.v2();
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
        System.out.println("Training Time: " + trainingGameTime + "/" + postProcessingTime);
        System.out.println(positionsCount + " training positions generated.");
        modelExecutor.stop();
        stream.flush();
        stream.close();
        remoteServerGames.forEach((key, value) -> System.out.printf("Server %s: %d games\n", key, value));
    }

    private void changeWritingCountBasedOnPolicySurprise(List<GameStep> steps) {
        if (!Configuration.TRAINING_POLICY_SURPRISE_WEIGHTING) {
            return;
        }
        var totalKLD = steps.stream().filter((x) -> x.trainingWriteCount > 0).map((x) -> (calcKld(x.state()) + 0.05)).mapToDouble(Double::doubleValue).sum();
        var totalCount = steps.stream().filter((x) -> x.trainingWriteCount > 0).count();
        steps.stream().filter((x) -> x.trainingWriteCount > 0).forEach((x) -> {
            x.trainingWriteCount = (calcKld(x.state()) + 0.05) / totalKLD * totalCount;
        });
    }

    private void writeTrainingGameRecord(Writer writer, GameState origState, Game game, List<GameStep> steps) {
        try {
            var state = steps.get(steps.size() - 1).state();
            if (origState.properties.preBattleRandomization != null) {
                var info = origState.properties.preBattleRandomization.listRandomizations();
                if (info.size() > 1) {
                    writer.write("Pre-Battle Randomization: " + info.get(game.preBattle_r).desc() + "\n");
                }
            }
            if (origState.properties.randomization != null) {
                var info = origState.properties.randomization.listRandomizations();
                if (info.size() > 1) {
                    writer.write("Battle Randomization: " + info.get(game.battle_r).desc() + "\n");
                }
            }
            if (game.noTemperatureTurn >= 0) {
                writer.write("No Temperature Moves From Turn " + game.noTemperatureTurn + "\n");
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

    private void startRemotePlayTrainingGameThread(String ip, int port, int nodeCount, AtomicInteger numToPlay, BlockingDeque<TrainingGameResult> deq, GameState state) {
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
                    req.zTraining = USE_Z_TRAINING;
                    req.curriculumTraining = state.properties.curriculumTraining;
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

    public TrainingGameResult playTrainingGamesRemote(GameState oState, ServerRequest req, int numOfThreads, int batchCount) throws IOException {
        var origState = oState.clone(false);
        if (remoteNumOfTrainingGames.get() == -123456) {
            remoteNumOfTrainingGames.set(req.remainingGames);
            var seeds = new ArrayList<Long>(req.remainingGames);
            for (int i = 0; i < req.remainingGames; i++) {
                seeds.add(origState.properties.random.nextLong(RandomGenCtx.Other));
            }
            var session = this;
            origState.properties = origState.properties.clone();
            session.USE_Z_TRAINING = req.zTraining;
            Configuration.USE_Z_TRAINING = req.zTraining;
            origState.properties.curriculumTraining = req.curriculumTraining;
            origState.properties.randomization = new GameStateRandomization.EnemyHealthRandomization(origState.properties.curriculumTraining, null).doAfter(origState.properties.randomization);
            modelExecutor.start(numOfThreads, batchCount);
            allocateThreadMCTS(numOfThreads, batchCount);
            for (int i = 0; i < producersCount; i++) {
                int _i = i;
                modelExecutor.addAndStartProducerThread(() -> {
                    var state = origState.clone(false);
                    int idx = remoteNumOfTrainingGames.getAndDecrement();
                    while (idx > 0) {
                        if (remoteTrainingDeq.size() >= 30) {
                            if (remoteNumOfTrainingGames.get() <= -123456) {
                                break;
                            }
                            modelExecutor.sleep(200);
                            continue;
                        }
                        try {
                            remoteTrainingDeq.putLast(session.playTrainingGame(state, req.nodeCount, mcts.get(_i), seeds.get(idx - 1)));
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        idx = remoteNumOfTrainingGames.getAndDecrement();
                    }
                    waitForBatchExecutorToFinish(_i);
                });
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
        changeWritingCountBasedOnPolicySurprise(steps);
        writeTrainingGameRecord(gameRecordWriter, origState, game, steps);
        if (Configuration.TRAINING_USE_FORCED_PLAYOUT) {
            pruneForcedPlayouts(steps);
        }
        var byteStream = new ByteArrayOutputStream();
        writeTrainingData(new DataOutputStream(byteStream), steps);
        writeTrainingData(new DataOutputStream(byteStream), game.augmentedSteps);
        return new TrainingGameResult(null, null, gameRecordWriter.toString(), byteStream.toByteArray());
    }

    public void stopPlayTrainingGamesRemote() {
        remoteNumOfTrainingGames.set(-123456);
        modelExecutor.stop();
        remoteTrainingDeq.clear();
    }

    private void pruneForcedPlayouts(List<GameStep> steps) {
        GameState state;
        for (GameStep step : steps) {
            if (step.action() < 0 || step.state().terminalAction >= 0) {
                break;
            }
            if (step.trainingWriteCount <= 0 || step.state().policyMod == null) {
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
            double q = state.getChildQ(max_i, GameState.V_COMB_IDX) / state.n[max_i];
            double u = 0.1 * state.policyMod[max_i] * sqrt(state.total_n) / (1 + state.n[max_i]);
            var max_puct = q + u;
            var del_n = 0;
            for (int i = 0; i < state.getLegalActions().length; i++) {
                if (i == max_i) {
                    continue;
                }
                q = state.getChildQ(i, GameState.V_COMB_IDX) / state.n[i];
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
        }
    }

    private static int writeTrainingData(DataOutputStream stream, List<GameStep> game) throws IOException {
        int count = 0;
        for (int i = game.size() - 2; i >= 0; i--) {
            var step = game.get(i);
            if (step.trainingWriteCount > 0) {
                count += 1;
            }
            if (step.trainingWriteCount <= 0) {
                continue;
            }
            int totalWriteCount = ((int) step.trainingWriteCount);
            if (step.trainingWriteCount != Math.floor((int) step.trainingWriteCount) && (step.trainingWriteCount - (int) step.trainingWriteCount) < step.state().properties.random.nextFloat(null)) {
                totalWriteCount += 1;
            }
            for (int writeCount = 0; writeCount < totalWriteCount; writeCount++) {
                var state = game.get(i).state();
                var x = state.getNNInput();
                for (int j = 0; j < x.length; j++) {
                    stream.writeFloat(x[j]);
                }
                for (int j = 1; j < GameState.V_EXTRA_IDX_START; j++) {
                    stream.writeFloat((float) ((step.v.get(j) * 2) - 1));
                }
                int v_idx = GameState.V_EXTRA_IDX_START;
                int k = 0;
                for (var target : state.properties.extraTrainingTargets) {
                    int n = target.getNumberOfTargets();
                    if (n == 1) {
                        if (state.properties.extraTrainingTargetsLabel.get(k).startsWith("Z") && false) {
                            stream.writeFloat((float) (step.v.get(v_idx)));
                        } else if (state.properties.extraTrainingTargetsLabel.get(k).equals("TurnsLeft")) {
                            stream.writeFloat((float) ((((step.v.get(v_idx) - step.state().realTurnNum / state.properties.maxPossibleRealTurnsLeft) * 2) - 1)));
                        } else if (state.properties.extraTrainingTargetsLabel.get(k).equals("ZeroDmgProb")) {
                            stream.writeFloat((float) (step.v.getVZeroDmg(step.state().getPlayeForRead().getAccumulatedDamage())));
                        } else {
                            stream.writeFloat((float) ((step.v.get(v_idx) * 2) - 1));
                        }
                    } else {
                        for (int j = 0; j < n; j++) {
                            stream.writeFloat((float) step.v.get(v_idx + j));
                        }
                    }
                    k += 1;
                    v_idx += n;
                }
                int idx = 0;
                if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                    for (int action = 0; action < state.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length; action++) {
                        stream.writeFloat(-1);
                    }
                    if (state.properties.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null) {
                        var order = state.getEnemyOrder();
                        for (int actionIdx = 0; actionIdx < state.properties.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()].length; actionIdx++) {
                            int action = order != null ? order[actionIdx] : actionIdx;
                            if (state.isActionLegal(action)) {
                                if (order != null) {
                                    for (int idx2 = 0; idx2 < state.getLegalActions().length; idx2++) {
                                        if (state.getLegalActions()[idx2] == action) {
                                            if (state.terminalAction >= 0) {
                                                if (state.terminalAction == idx2) {
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
                                        if (state.terminalAction >= 0) {
                                            if (state.terminalAction == idx++) {
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
                    if (state.properties.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()] != null) {
                        for (int action = 0; action < state.properties.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()].length; action++) {
                            stream.writeFloat(-1);
                        }
                    }
                } else if (state.actionCtx == GameActionCtx.SELECT_SCENARIO) {
                    for (int action = 0; action < state.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length; action++) {
                        stream.writeFloat(-1);
                    }
                    if (state.properties.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null) {
                        for (int action = 0; action < state.properties.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()].length; action++) {
                            stream.writeFloat(-1);
                        }
                    }
                    if (state.properties.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()] != null) {
                        for (int action = 0; action < state.properties.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()].length; action++) {
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
                    for (int action = 0; action < state.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length; action++) {
                        if (state.isActionLegal(action)) {
                            if (idx < state.getLegalActions().length && state.getLegalActions()[idx] == action) {
                                if (state.terminalAction >= 0) {
                                    if (state.terminalAction == idx++) {
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
                    if (state.properties.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null) {
                        for (int action = 0; action < state.properties.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()].length; action++) {
                            stream.writeFloat(-1);
                        }
                    }
                    if (state.properties.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()] != null) {
                        for (int action = 0; action < state.properties.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()].length; action++) {
                            stream.writeFloat(-1);
                        }
                    }
                }
            }
        }
        return count;
    }

    private GameState getNextState(GameState state, MCTS mcts, int action, boolean clone) {
        return getNextState(state, mcts, action, null, clone);
    }

    private GameState getNextState(GameState state, MCTS mcts, int action, GameStep prevStep, boolean clone) {
        State nextState = state.ns[action];
        GameState newState;
        if (nextState instanceof ChanceState cState) {
            newState = cState.getNextState(false, -1);
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
            if (Configuration.DO_NOT_USE_CACHED_STATE_WHEN_MAKING_REAL_MOVE) {
                nextState = null;
            }
            if (nextState == null) {
                newState = state.clone(false);
                newState = newState.doAction(action);
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
        }
        newState.bannedActions = null;
        return newState;
    }

    public void setMatchLogFile(String fileName) {
        try {
            File file = new File(modelDir + "/" + fileName);
            file.delete();
            matchLogWriter = new OutputStreamWriter(new GzipCompressorOutputStream(new FileOutputStream(modelDir + "/" + fileName, true)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class GameRecord {
        List<GameStep> game;
        int dmgTaken;
        boolean result;
        List<String> stateDescription;
    }

    public static void readMatchLogFile(String path, String modelPath, GameState origState) {
        try {
            List<List<GameStep>> games = new ArrayList<>();
            var reader = new BufferedReader(new InputStreamReader(new GzipCompressorInputStream(new FileInputStream(path))));
            String l = reader.readLine();
            List<GameStep> game = null;
            GameState state = null;
            String stateStr = null;
            while (l != null) {
                if (l.startsWith("Seed: ")) {
                    if (game != null) {
                        game.add(new GameStep(state, -1));
                        games.add(game);
                    }
                    game = new ArrayList<>();
                    state = origState.clone(false);
                    state.properties = state.properties.clone();
                    var randomGen = new RandomGen.RandomGenByCtx(Long.parseLong(l.substring(6)));
                    state.properties.realMoveRandomGen = randomGen;
                    randomGen.useNewCommonNumberVR = true;
                    state.properties.testNewFeature = true;
                    state.properties.makingRealMove = true;
                } else if (l.startsWith("{")) {
                    stateStr = l;
                } else if (l.startsWith("action=")) {
                    var action = l.substring(7);
                    if (action.indexOf('[') >= 0) {
                        action = action.substring(0, action.indexOf('[') - 1);
                    }
                    int idx = -1;
                    if (state.actionCtx == GameActionCtx.BEGIN_TURN && !action.equals("Begin Turn")) {
                        state = state.clone(false);
                        state = state.doAction(0);
                    }
                    for (int i = 0; i < state.getLegalActions().length; i++) {
                        if (state.getActionString(i).equals(action)) {
                            idx = i;
                            break;
                        }
                    }
                    if (idx < 0) {
                        System.out.println(games.size());
                        System.out.println(game);
                        System.out.println(state);
                        System.out.println(action);
                        System.out.println(state.actionCtx);
                        throw new IllegalStateException();
                    }
                    game.add(new GameStep(state, idx));
                    game.get(game.size() - 1).stateStr = stateStr;
                    state = state.clone(false);
                    state = state.doAction(idx);
                }
                l = reader.readLine();
            }
            if (game != null) {
                game.add(new GameStep(state, -1));
                games.add(game);
            }

            System.out.println("Game 1-" + games.size());
            reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.print("> ");
                String line = reader.readLine();
                if (line.equals("exit")) {
                    break;
                }
                try {
                    while (true) {
                        int i = Integer.parseInt(line);
                        if (i > 0 && i <= games.size()) {
                            new InteractiveMode().interactiveStart(games.get(i - 1), modelPath);
                            break;
                        }
                    }
                } catch (NumberFormatException e) {}
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setTrainingDataLogFile(String fileName) {
        try {
            File file = new File(modelDir + "/" + fileName);
            file.delete();
            trainingDataWriter = new OutputStreamWriter(new GzipCompressorOutputStream(new FileOutputStream(modelDir + "/" + fileName, true)));
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
            if (step.state().actionCtx == GameActionCtx.BEGIN_TURN && !step.state().isStochastic && step.state().isTerminal() == 0) continue;
            writer.write(step.state() + "\n");
            if (step.v != null && !step.trainingSkipOpening) {
                int bracketEnd = -1;
                for (int j = 0; j < step.state().properties.v_total_len; j++) {
                    var r = step.state().properties.trainingTargetsRegistrantVIdxMap.get(j - GameState.V_EXTRA_IDX_START);
                    if (r != null) {
                        writer.write(r.v1() + ": ");
                        if (r.v2() > 1) {
                            bracketEnd = j + r.v2() - 1;
                        }
                    }
                    if (j == GameState.V_EXTRA_IDX_START + step.state().properties.zeroDmgProbVExtraIdx) {
                        writer.write(String.valueOf(step.v.getVZeroDmg(step.state().getPlayeForRead().getAccumulatedDamage())));
                    } else {
                        writer.write(String.valueOf(step.v.get(j)));
                    }
                    if (j == bracketEnd) {
                        writer.write(")");
                    }
                    writer.write(j == step.v.length() - 1 ? " | " : ", ");
                }
                writer.write("writeCount=" + step.trainingWriteCount + "\n");
            }
            if (step.action() >= 0) {
                var nextStep = steps.get(i + 1);
                writer.write("action=" + step.state().getActionString(step.action()));
                if (nextStep.state().stateDesc != null) {
                    writer.write(" [" + nextStep.state().stateDesc + "]");
                }
                if (step.isExplorationMove) {
                    writer.write(" [Temperature]");
                }
                writer.write("\n");
            }
        }
    }

    // custom code that I modified if I want to check if/how fast network learned a strategy in a training run
    public static void getTrainingRunStats(String savesDir, GameState origState, int numOfGames, int nodeCount, int numOfThreads, int batchCount) {
        int iter = 0;
        do {
            System.out.println("**************************************");
            System.out.printf("Iteration %d\n", iter);
            MatchSession session = new MatchSession(Path.of(savesDir, "iteration" + iter).toString());
            session.playGamesForStat(origState, numOfGames, nodeCount, numOfThreads, batchCount);
            iter++;
        } while (Files.exists(Path.of(savesDir, "iteration" + iter)));
    }

    public void playGamesForStat(GameState origState, int numOfGames, int nodeCount, int numOfThreads, int batchCount) {
        var seeds = new ArrayList<Long>(numOfGames);
        for (int i = 0; i < numOfGames; i++) {
            seeds.add(origState.properties.random.nextLong(RandomGenCtx.Other));
        }
        var deq = new LinkedBlockingDeque<GameResult>();
        var numToPlay = new AtomicInteger(numOfGames);
        modelExecutor.start(numOfThreads, batchCount);
        allocateThreadMCTS(numOfThreads, batchCount);
        for (int i = 0; i < producersCount; i++) {
            startPlayGameThread(origState, nodeCount, seeds, deq, numToPlay, i);
        }

        var game_i = 0;
        var start = System.currentTimeMillis();
        var progressInterval = ((int) Math.ceil(numOfGames / 1000f)) * 25;
        var averageQ = 0.0;
        // bucket idx -> (wins, games)
        var winRateBucket = new HashMap<Integer, Tuple<Integer, Integer>>();
        for (int i = 0; i < 100 + 1; i++) {
            winRateBucket.put(i, new Tuple<>(0, 0));
        }
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
                averageQ += state.calcQValue();

                for (int i = 1; i < steps.size() - 1; i++) {
                    var step = steps.get(i);
                    var bidx = (int) ((step.state().v_win + (1.0 / 100 / 2)) * 100);
                    bidx = (int) ((step.state().getTotalQ(GameState.V_WIN_IDX) / (step.state().total_n + 1) + (1.0 / 100 / 2)) * 100);
                    if (state.isTerminal() > 0) {
                        var prev = winRateBucket.get(bidx);
                        winRateBucket.put(bidx, new Tuple<>(prev.v1() + 1, prev.v2() + 1));
                    } else {
                        var prev = winRateBucket.get(bidx);
                        winRateBucket.put(bidx, new Tuple<>(prev.v1(), prev.v2() + 1));
                    }
                }
            }
            game_i += 1;
            if ((game_i % progressInterval == 0) || game_i == numOfGames) {
                winRateBucket.forEach((k, v) -> {
                    System.out.println((k / 100.0) + ": " + v.v1() + "/" + v.v2() + " (" + ((double) v.v1()) / v.v2() + "%)");
                });
                System.out.println("Progress: " + game_i + "/" + numOfGames);
            }
        }
        System.out.println("Time Taken: " + (System.currentTimeMillis() - start));
        System.out.println("Average Q: " + averageQ / numOfGames);
        modelExecutor.stop();
    }
}
