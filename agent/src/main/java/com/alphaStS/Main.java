package com.alphaStS;

import com.alphaStS.enemy.Enemy;
import com.alphaStS.model.ModelPlain;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

enum ServerRequestType {
    PLAY_GAMES,
    PLAY_TRAINING_GAMES,
    UPLOAD_MODEL,
    UPLOAD_MODEL_CMP
}

public class Main {
    public static void main(String[] args) throws IOException {
        var state = TestStates.TestState();
//        ((RandomGen.RandomGenPlain) state.prop.random).random.setSeed(5);

        if (args.length > 0 && args[0].equals("--get-lengths")) {
            System.out.print(state.getNNInput().length + "," + state.properties.totalNumOfActions);
            for (int i = 0; i < state.properties.extraTrainingTargets.size(); i++) {
                System.out.print("," + state.properties.extraTrainingTargetsLabel.get(i) + "," + state.properties.extraTrainingTargets.get(i).getNumberOfTargets());
            }
        } else if (args.length > 0 && (args[0].equals("--play") || args[0].equals("-p"))) {
            System.out.println("Seed: " + state.properties.random.getSeed(null));
            playGames(state, args);
        } else if (args.length > 0 && (args[0].equals("--play-1-game") || args[0].equals("-pg"))) {
            System.out.println("Seed: " + state.properties.random.getSeed(null));
            playGameAndViewGame(state, args);
        } else if (args.length > 0 && (args[0].equals("--training") || args[0].equals("-t"))) {
            System.out.println("Seed: " + state.properties.random.getSeed(null));
            playTrainingGames(state, args);
        } else if (args.length > 0 && (args[0].equals("--interactive") || args[0].equals("-i"))) {
            System.out.println("Seed: " + state.properties.random.getSeed(null));
            interactiveMode(state, args);
        } else if (args.length > 0 && args[0].equals("--server")) {
            System.out.println("Seed: " + state.properties.random.getSeed(null));
            startServer(state, args);
        } else {
            System.out.println("Invalid arguments");
        }
    }

    private static int Z_TRAINING_UPTO_ITERATION = 20;
    private static boolean CURRICULUM_TRAINING_ON = false;
    private static int NUMBER_OF_GAMES_TO_PLAY = 5;
    private static int NUMBER_OF_NODES_PER_TURN = 1000;
    private static int NUMBER_OF_THREADS = 1;
    private static int BATCH_SIZE = 1;
    private static boolean WRITE_MATCHES = false;
    private static boolean PRINT_DMG = false;
    private static boolean TEST_TRAINING_AGENT_ONLY = false;
    private static String COMPARE_DIR = null;
    private static final int[][] SCENARIO_GROUPS = null;
    private static String SAVES_DIR = "../saves";
    private static String CUR_ITER_DIRECTORY;
    private static String PREV_ITER_DIRECTORY;
    private static int ITERATION = -1;

    private static void parseCommonArgs(GameState state, String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-tm")) {
                TEST_TRAINING_AGENT_ONLY = true;
            }
            if (args[i].equals("-t")) {
                NUMBER_OF_THREADS = Integer.parseInt(args[i + 1]);
            }
            if (args[i].equals("-b")) {
                BATCH_SIZE = Integer.parseInt(args[i + 1]);
            }
            if (args[i].equals("-c")) {
                NUMBER_OF_GAMES_TO_PLAY = Integer.parseInt(args[i + 1]);
                i++;
            }
            if (args[i].equals("-n")) {
                NUMBER_OF_NODES_PER_TURN = Integer.parseInt(args[i + 1]);
                i++;
            }
            if (args[i].equals("-dir")) {
                SAVES_DIR = args[i + 1];
                i++;
            }
            if (args[i].equals("-z_train")) {
                Z_TRAINING_UPTO_ITERATION = Integer.parseInt(args[i + 1]);
                i++;
            }
            if (args[i].equals("-curriculum_training")) {
                CURRICULUM_TRAINING_ON = true;
            }
        }

        if (SAVES_DIR.startsWith("../")) {
            SAVES_DIR = "../saves";
            WRITE_MATCHES = true;
            PRINT_DMG = true;
            NUMBER_OF_GAMES_TO_PLAY = 1000;
            NUMBER_OF_NODES_PER_TURN = 100;
//            SCENARIO_GROUPS = GameStateUtils.getScenarioGroups(state, 4, 1);
//            iteration = 56;
//            COMPARE_DIR = "../saves/iteration60";
//            COMPARE_DIR = SAVES_DIR + "/iteration" + (iteration - 2);
//            COMPARE_DIR = SAVES_DIR + "/iteration60";
        }

        ObjectMapper mapper = new ObjectMapper();
        CUR_ITER_DIRECTORY = SAVES_DIR + "/iteration0";
        PREV_ITER_DIRECTORY = SAVES_DIR + "/iteration0";
        try {
            JsonNode root = mapper.readTree(new File(SAVES_DIR + "/training.json"));
            ITERATION = ITERATION < 0 ? root.get("iteration").asInt() : ITERATION;
            CUR_ITER_DIRECTORY = SAVES_DIR + "/iteration" + (ITERATION - 1);
            PREV_ITER_DIRECTORY = SAVES_DIR + "/iteration" + (ITERATION - 2);
            File f = new File(SAVES_DIR + "/desc.txt");
            if (!f.exists()) {
                GameStateUtils.writeStateDescription(state, f);
            }
        } catch (IOException e) {
            System.out.println("Unable to find neural network.");
        }
    }

    private static void interactiveMode(GameState state, String[] args) throws IOException {
        parseCommonArgs(state, args);
//        MatchSession.readMatchLogFile(CUR_ITER_DIRECTORY + "/matches.txt.gz", CUR_ITER_DIRECTORY, state);
        new InteractiveMode().interactiveStart(state, SAVES_DIR, CUR_ITER_DIRECTORY);
    }

    private static void playGames(GameState state, String[] args) throws IOException {
        parseCommonArgs(state, args);
        state.properties.randomization = new GameStateRandomization.EnemyRandomization(false, -1, -1).doAfter(state.properties.randomization);
//        state.properties.randomization = state.properties.randomization.fixR(0, 2, 4);
        MatchSession session = new MatchSession(CUR_ITER_DIRECTORY, COMPARE_DIR);
        if (NUMBER_OF_GAMES_TO_PLAY <= 100 || WRITE_MATCHES) {
            session.setMatchLogFile("matches.txt.gz");
        }
        if (state.properties.randomization != null) {
            session.scenariosGroup = SCENARIO_GROUPS;
        }
        session.playGames(state, NUMBER_OF_GAMES_TO_PLAY, NUMBER_OF_NODES_PER_TURN, NUMBER_OF_THREADS, BATCH_SIZE, true, PRINT_DMG, false);
        session.flushAndCloseFileWriters();
    }

    private static void playGameAndViewGame(GameState state, String[] args) throws IOException {
        parseCommonArgs(state, args);
        state.properties.randomization = new GameStateRandomization.EnemyRandomization(false, -1, -1).doAfter(state.properties.randomization);
//        state.properties.randomization = state.properties.randomization.fixR(0, 2, 4);
        MatchSession session = new MatchSession(CUR_ITER_DIRECTORY);
        var writer = new OutputStreamWriter(System.out);
        var game = session.playGames(state, 1, NUMBER_OF_NODES_PER_TURN, 1, 1, false, false, true).get(0).steps();
        MatchSession.printGame(writer, game);
        writer.flush();
        new InteractiveMode().interactiveStart(game, CUR_ITER_DIRECTORY);
    }

    private static void playTrainingGames(GameState state, String[] args) throws IOException {
        parseCommonArgs(state, args);
        Configuration.CPUCT_SCALING = false;
        Configuration.USE_PROGRESSIVE_WIDENING = false;
        Configuration.TRANSPOSITION_ACROSS_CHANCE_NODE = false;
        var preBattleScenarios = state.properties.preBattleScenarios;
        var randomization = state.properties.randomization;
        if (state.properties.preBattleScenarios != null && state.properties.endOfPreBattleHandler == null) {
            state.properties.preBattleScenarios = null;
            if (state.properties.randomization == null) {
                state.properties.randomization = preBattleScenarios;
            } else {
                state.properties.randomization = state.properties.randomization.doAfter(preBattleScenarios);
            }
            if (state.properties.preBattleRandomization == null) {
                state.setActionCtx(GameActionCtx.BEGIN_BATTLE, null, false);
            }
        }

        int minDifficulty = -1;
        int maxDifficulty = -1;
        boolean automatedCurriculumTraining = true;
        int totalDifficulty = 0;
        int enemiesAlive = 0;
        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
            if (enemy.getMaxRandomizeDifficulty() <= 0) {
                automatedCurriculumTraining = false;
                break;
            }
            totalDifficulty += enemy.getMaxRandomizeDifficulty();
            enemiesAlive++;
        }
        if (automatedCurriculumTraining) {
            if (ITERATION == 1) {
                minDifficulty = enemiesAlive;
                maxDifficulty = (enemiesAlive + totalDifficulty + 1) / 2;
            } else {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    JsonNode root = mapper.readTree(new File(PREV_ITER_DIRECTORY + "/training.json"));
                    if (root.get("minDifficulty") != null) {
                        minDifficulty = root.get("minDifficulty").asInt();
                        maxDifficulty = root.get("maxDifficulty").asInt();
                    }
                } catch (FileNotFoundException e) {
                }
            }
        }

        MatchSession session = new MatchSession(CUR_ITER_DIRECTORY, COMPARE_DIR);
        if (state.properties.randomization != null) {
            session.scenariosGroup = SCENARIO_GROUPS;
        }
        if (NUMBER_OF_GAMES_TO_PLAY <= 100) {
            session.setMatchLogFile("training_matches.txt.gz");
        }
        session.training = true;
        session.playGames(state, NUMBER_OF_GAMES_TO_PLAY, NUMBER_OF_NODES_PER_TURN, NUMBER_OF_THREADS, BATCH_SIZE, false, PRINT_DMG, false);

        if (preBattleScenarios != null && state.properties.endOfPreBattleHandler == null) {
            state.properties.preBattleScenarios = preBattleScenarios;
            state.properties.randomization = randomization;
            if (state.properties.preBattleRandomization == null) {
                state.setActionCtx(GameActionCtx.SELECT_SCENARIO, null, false);
            }
        }

        if (TEST_TRAINING_AGENT_ONLY) {
            return;
        }

        session.setTrainingDataLogFile("training_data.txt.gz");
        session.USE_Z_TRAINING = ITERATION <= Z_TRAINING_UPTO_ITERATION;
        Configuration.USE_Z_TRAINING = session.USE_Z_TRAINING;
        session.POLICY_CAP_ON = false;
        if (ITERATION < 16) {
            Configuration.TRAINING_SKIP_OPENING_TURNS = false;
        }
        if (ITERATION >= 15) {
            Configuration.TRAINING_POLICY_SURPRISE_WEIGHTING = false;
        }
        long start = System.currentTimeMillis();
        state.properties.curriculumTraining = CURRICULUM_TRAINING_ON || automatedCurriculumTraining;
        state.properties.minDifficulty = minDifficulty;
        state.properties.maxDifficulty = maxDifficulty;
        state.properties.randomization = new GameStateRandomization.EnemyRandomization(state.properties.curriculumTraining, minDifficulty, maxDifficulty).doAfter(state.properties.randomization);
        session.playTrainingGames(state, 200, 100, NUMBER_OF_THREADS, BATCH_SIZE, CUR_ITER_DIRECTORY + "/training_data.bin.lz4");
        if (automatedCurriculumTraining) {
            if (minDifficulty == totalDifficulty) {
            } else if (session.difficulty < minDifficulty) {
                minDifficulty -= (minDifficulty + 7) / 8;
            } else {
                if (session.difficulty == maxDifficulty) {
                    minDifficulty = minDifficulty + (session.difficulty - minDifficulty + 3) / 4;
                    maxDifficulty = session.difficulty + (totalDifficulty - session.difficulty + 3) / 4;
                } else {
                    maxDifficulty = (session.difficulty + maxDifficulty + 1) / 2;
                }
            }
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode node = mapper.createObjectNode();
            node.put("minDifficulty", minDifficulty);
            node.put("maxDifficulty", maxDifficulty);
            node.put("difficulty", session.difficulty);
            try (var writer = new BufferedWriter(new FileWriter(CUR_ITER_DIRECTORY + "/training.json"))) {
                writer.write(node.toString());
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("Time Taken: " + (end - start));
        var debugModels = session.modelExecutor.getExecutorModels();
        for (int i = 0; i < debugModels.size(); i++) {
            var m = (ModelPlain) debugModels.get(i);
            System.out.println("Time Taken (By Model " + i + "): " + m.time_taken);
            System.out.println("Model " + i + ": size=" + m.cache.size() + ", " + m.cache_hits + "/"
                    + m.calls + " hits (" + (double) m.cache_hits / m.calls + ")");
        }
        System.out.println("--------------------");

        session.flushAndCloseFileWriters();
    }

    private static void startServer(GameState state, String[] args) throws IOException {
        parseCommonArgs(state, args);
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        jsonFactory.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        ObjectMapper mapper = new ObjectMapper(jsonFactory);
        MatchSession session = null;
        ServerSocket serverSocket = new ServerSocket(4000);
        String modelDir = null, modelCmpDir = null;
        while (serverSocket.isBound()) {
            Socket socket = serverSocket.accept();
            try {
                var in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                int numOfGames = 0;
                int newNumOfGames = 0;
                while (socket.isConnected()) {
                    int len = in.readInt();
                    byte[] bytes = in.readNBytes(len);
                    ServerRequest req = mapper.readValue(bytes, ServerRequest.class);
                    switch (req.type) {
                    case PLAY_GAMES -> {
                        if (req.remainingGames == 0) {
                            break;
                        }
                        if (session == null) {
                            session = new MatchSession(modelDir, modelCmpDir);
                            numOfGames = 0;
                            newNumOfGames = 0;
                        }
                        List<MatchSession.RemoteGameResult> results = new ArrayList<>();
                        int count = session.remoteDeq.size();
                        for (int i = 0; i < count; i++) {
                            results.add(session.playGamesRemote(state, req.remainingGames, req.nodeCount, NUMBER_OF_THREADS, BATCH_SIZE));
                        }
                        if (results.size() == 0) {
                            results.add(session.playGamesRemote(state, req.remainingGames, req.nodeCount, NUMBER_OF_THREADS, BATCH_SIZE));
                        }
                        mapper.writeValue(socket.getOutputStream(), results);
                        newNumOfGames += results.size();
                        if (newNumOfGames - numOfGames > 20) {
                            System.out.printf("Requested %d games\n", newNumOfGames);
                            numOfGames = newNumOfGames;
                        }
                    }
                    case PLAY_TRAINING_GAMES -> {
                        if (req.remainingGames == 0) {
                            break;
                        }
                        if (session == null) {
                            session = new MatchSession(modelDir, modelCmpDir);
                        } else if (session.remoteNumOfGames.get() > -123456 && session.modelExecutor.isRunning()) {
                            session.stopPlayGamesRemote();
                            numOfGames = 0;
                            newNumOfGames = 0;
                        }
                        List<MatchSession.TrainingGameResult> results = new ArrayList<>();
                        int count = session.remoteTrainingDeq.size();
                        for (int i = 0; i < count; i++) {
                            results.add(session.playTrainingGamesRemote(state, req, NUMBER_OF_THREADS, BATCH_SIZE));
                        }
                        if (results.size() == 0) {
                            results.add(session.playTrainingGamesRemote(state, req, NUMBER_OF_THREADS, BATCH_SIZE));
                        }
                        mapper.writeValue(socket.getOutputStream(), results);
                        newNumOfGames += results.size();
                        if (newNumOfGames - numOfGames > 10) {
                            System.out.printf("Requested %d training games\n", newNumOfGames);
                            numOfGames = newNumOfGames;
                        }
                    }
                    case UPLOAD_MODEL -> {
                        System.out.println(req.type);
                        File f = new File(System.getProperty("user.home") + "/tmp/model");
                        f.mkdirs();
                        new File(System.getProperty("user.home") + "/tmp/model/model.onnx").delete();
                        var s = new FileOutputStream(System.getProperty("user.home") + "/tmp/model/model.onnx");
                        s.write(req.bytes);
                        s.flush();
                        s.close();
                        modelDir = System.getProperty("user.home") + "/tmp/model";
                        modelCmpDir = null;
                    }
                    case UPLOAD_MODEL_CMP -> {
                        System.out.println(req.type);
                        if (req.bytes == null) {
                            modelCmpDir = System.getProperty("user.home") + "/tmp/model";
                        } else {
                            File f = new File(System.getProperty("user.home") + "/tmp/model_cmp");
                            f.mkdirs();
                            new File(System.getProperty("user.home") + "/tmp/model_cmp/model.onnx").delete();
                            var s = new FileOutputStream(System.getProperty("user.home") + "/tmp/model_cmp/model.onnx");
                            s.write(req.bytes);
                            s.flush();
                            s.close();
                            modelCmpDir = System.getProperty("user.home") + "/tmp/model_cmp";
                        }
                    }
                    }
                }
            } catch (IOException exception) {
                exception.printStackTrace(System.out);
            }
            socket.close();
            if (session != null) {
                session.stopPlayGamesRemote();
                session.stopPlayTrainingGamesRemote();
                session.close();
                session = null;
            }
        }
    }

}
