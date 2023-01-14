package com.alphaStS;

import com.alphaStS.enemy.Enemy;
import com.alphaStS.utils.Utils;
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

import static com.alphaStS.InteractiveMode.interactiveStart;

enum ServerRequestType {
    PLAY_GAMES,
    PLAY_TRAINING_GAMES,
    UPLOAD_MODEL,
    UPLOAD_MODEL_CMP
}

class ServerRequest {
    public ServerRequestType type;
    public int remainingGames;
    public int nodeCount;
    public byte[] bytes;
}

public class Main {
    public static void main(String[] args) throws IOException {
        var state = TestStates.TestState16b();
        if (args.length > 0 && args[0].equals("--get-lengths")) {
            System.out.print(state.getNNInput().length + "," + state.prop.totalNumOfActions);
            for (int i = 0; i < state.prop.extraTrainingTargets.size(); i++) {
                System.out.print("," + state.prop.extraTrainingTargets.get(i).getNumberOfTargets());
            }
            return;
        }
//        ((RandomGen.RandomGenPlain) state.prop.random).random.setSeed(5);
        System.out.println("Seed: " + state.prop.random.getSeed(null));

        boolean GENERATE_TRAINING_GAMES = false;
        boolean TEST_TRAINING_AGENT = false;
        boolean PLAY_GAMES = false;
        boolean PLAY_A_GAME = false;
        int Z_TRAINING_UPTO_ITERATION = 20;
        boolean CURRICULUM_TRAINING_ON = false;
        boolean TRAINING_WITH_LINE = false;
        boolean GAMES_ADD_ENEMY_RANDOMIZATION = false;
        boolean GAMES_ADD_POTION_RANDOMIZATION = false;
        boolean GAMES_TEST_CHOOSE_SCENARIO_RANDOMIZATION = false;
        int NUMBER_OF_GAMES_TO_PLAY = 5;
        int NUMBER_OF_NODES_PER_TURN = 1000;
        int NUMBER_OF_THREADS = 2;
        String COMPARE_DIR = null;
        String SAVES_DIR = "../saves";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-training")) {
                GENERATE_TRAINING_GAMES = true;
            }
            if (args[i].equals("-tm")) {
                TEST_TRAINING_AGENT = true;
            }
            if (args[i].equals("-t")) {
                NUMBER_OF_THREADS = Integer.parseInt(args[i + 1]);
            }
            if (args[i].equals("-g")) {
                PLAY_GAMES = true;
            }
            if (args[i].equals("-p")) {
                PLAY_A_GAME = true;
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
            if (args[i].equals("-training_with_line")) {
                TRAINING_WITH_LINE = true;
            }
        }

        int iteration = -1;
        if (SAVES_DIR.startsWith("../")) {
            SAVES_DIR = "../saves";
            NUMBER_OF_GAMES_TO_PLAY = 1000;
            GAMES_ADD_ENEMY_RANDOMIZATION = true;
            GAMES_ADD_POTION_RANDOMIZATION = true;
            GAMES_TEST_CHOOSE_SCENARIO_RANDOMIZATION = true;
            NUMBER_OF_NODES_PER_TURN = 100;
//            iteration = 56;
//            COMPARE_DIR = "../saves/iteration60";
//            COMPARE_DIR = SAVES_DIR + "/iteration" + (iteration - 2);
//            COMPARE_DIR = SAVES_DIR + "/iteration60";
        }

        if (!GENERATE_TRAINING_GAMES && GAMES_ADD_ENEMY_RANDOMIZATION) {
            state.prop.randomization = new GameStateRandomization.EnemyRandomization(false, -1, -1).doAfter(state.prop.randomization);
        }
        if (!GENERATE_TRAINING_GAMES && GAMES_ADD_POTION_RANDOMIZATION && state.prop.potions.size() > 0) {
            var s = new ArrayList<Short>();
            for (int i = 0; i < state.prop.potions.size(); i++) {
                s.add((short) 80);
            }
            state.prop.randomization = new GameStateRandomization.PotionsUtilityRandomization(state.prop.potions).doAfter(state.prop.randomization);
        } else if ((GENERATE_TRAINING_GAMES || TEST_TRAINING_AGENT) && state.prop.potions.size() > 0) {
            var s = new ArrayList<Short>();
            for (int i = 0; i < state.prop.potions.size(); i++) {
                s.add((short) 80);
            }
            state.prop.preBattleRandomization = new GameStateRandomization.PotionsUtilityRandomization(state.prop.potions).doAfter(state.prop.preBattleRandomization);
        }
        var preBattleScenarios = state.prop.preBattleScenarios;
        var randomization = state.prop.randomization;
        if ((TEST_TRAINING_AGENT || GAMES_TEST_CHOOSE_SCENARIO_RANDOMIZATION) && state.prop.preBattleScenarios != null) {
            if (state.prop.randomization == null) {
                state.prop.randomization = state.prop.preBattleScenarios;
            } else {
                state.prop.randomization = state.prop.randomization.doAfter(state.prop.preBattleScenarios);
            }
            state.prop.preBattleScenarios = null;
            state.setActionCtx(GameActionCtx.BEGIN_BATTLE, null, false);
        }
//        state.prop.randomization = state.prop.randomization.fixR(0, 2, 4);

        ObjectMapper mapper = new ObjectMapper();
        String curIterationDir = SAVES_DIR + "/iteration0";
        String prevIterationDir = SAVES_DIR + "/iteration0";
        try {
            JsonNode root = mapper.readTree(new File(SAVES_DIR + "/training.json"));
            iteration = iteration < 0 ? root.get("iteration").asInt() : iteration;
            curIterationDir = SAVES_DIR + "/iteration" + (iteration - 1);
            prevIterationDir = SAVES_DIR + "/iteration" + (iteration - 2);
            File f = new File(SAVES_DIR + "/desc.txt");
            if (!f.exists()) {
                writeStateDescription(f, state);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Unable to find neural network.");
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
            if (iteration == 1) {
                minDifficulty = enemiesAlive;
                maxDifficulty = (enemiesAlive + totalDifficulty + 1) / 2;
            } else {
                try {
                    JsonNode root = mapper.readTree(new File(prevIterationDir + "/training.json"));
                    if (root.get("minDifficulty") != null) {
                        minDifficulty = root.get("minDifficulty").asInt();
                        maxDifficulty = root.get("maxDifficulty").asInt();
                    }
                } catch (FileNotFoundException e) {
                }
            }
        }

        if (args.length > 0 && (args[0].equals("--i") || args[0].equals("-i"))) {
            interactiveStart(state, curIterationDir);
            return;
        }

        if (args.length > 0 && args[0].equals("--server")) {
            startServer(state);
            return;
        }

        if (PLAY_A_GAME) {
            MatchSession session = new MatchSession(1, curIterationDir);
            var writer = new OutputStreamWriter(System.out);
            MatchSession.printGame(writer, session.playGame(state, -1, null, session.mcts.get(0), NUMBER_OF_NODES_PER_TURN).steps());
            writer.flush();
        }

        MatchSession session = new MatchSession(NUMBER_OF_THREADS, curIterationDir, COMPARE_DIR);
        if (!TEST_TRAINING_AGENT && !GENERATE_TRAINING_GAMES && state.prop.randomization != null) {
//            session.scenariosGroup = GameStateUtils.getScenarioGroups(state, 4, 3);
        }

        if (TEST_TRAINING_AGENT || PLAY_GAMES) {
            if (!TEST_TRAINING_AGENT) {
//                if (state.prop.randomization != null) {
//                    state.prop.randomization.randomize(state, RANDOMIZATION_SCENARIO);
//                }
//                GameSolver solver = new GameSolver(state);
//                solver.solve();
//                session.solver = solver;
            }
            session.training = TEST_TRAINING_AGENT;
            if (TEST_TRAINING_AGENT && NUMBER_OF_GAMES_TO_PLAY <= 100) {
                session.setMatchLogFile("training_matches.txt.gz");
            } else if (!TEST_TRAINING_AGENT && NUMBER_OF_GAMES_TO_PLAY <= 100) {
                session.setMatchLogFile("matches.txt.gz");
            }
            session.playGames(state, NUMBER_OF_GAMES_TO_PLAY, NUMBER_OF_NODES_PER_TURN, !TEST_TRAINING_AGENT);
        }
        if (GENERATE_TRAINING_GAMES && preBattleScenarios != null) {
            state.prop.preBattleScenarios = preBattleScenarios;
            state.prop.randomization = randomization;
            state.setActionCtx(GameActionCtx.SELECT_SCENARIO, null, false);
        }

        if (GENERATE_TRAINING_GAMES) {
            session.setTrainingDataLogFile("training_data.txt.gz");
            session.USE_Z_TRAINING = iteration <= Z_TRAINING_UPTO_ITERATION;
            session.POLICY_CAP_ON = false;
            if (iteration < 16) {
                Configuration.TRAINING_SKIP_OPENING_TURNS = false;
            }
            if (iteration >= 15) {
                Configuration.TRAINING_POLICY_SURPRISE_WEIGHTING = false;
            }
            session.TRAINING_WITH_LINE = TRAINING_WITH_LINE;
            long start = System.currentTimeMillis();
            state.prop.curriculumTraining = CURRICULUM_TRAINING_ON || automatedCurriculumTraining;
            state.prop.randomization = new GameStateRandomization.EnemyRandomization(state.prop.curriculumTraining, minDifficulty, maxDifficulty).doAfter(state.prop.randomization);
            session.playTrainingGames(state, 200, 100, curIterationDir + "/training_data.bin.lz4");
            if (automatedCurriculumTraining) {
                if (minDifficulty == totalDifficulty) {
                } else if (session.difficulty < minDifficulty) {
                    minDifficulty -= (minDifficulty - session.difficulty) / 4;
                } else {
                    if (session.difficulty == maxDifficulty) {
                        minDifficulty = minDifficulty + (session.difficulty - minDifficulty + 3) / 4;
                        maxDifficulty = session.difficulty + (totalDifficulty - session.difficulty + 3) / 4;
                    } else {
                        maxDifficulty = (session.difficulty + maxDifficulty + 1) / 2;
                    }
                }
                ObjectNode node = mapper.createObjectNode();
                node.put("minDifficulty", minDifficulty);
                node.put("maxDifficulty", maxDifficulty);
                node.put("difficulty", session.difficulty);
                try (var writer = new BufferedWriter(new FileWriter(curIterationDir + "/training.json"))) {
                    writer.write(node.toString());
                }
            }

            long end = System.currentTimeMillis();
            System.out.println("Time Taken: " + (end - start));
            for (int i = 0; i < session.mcts.size(); i++) {
                var m = session.mcts.get(i);
                System.out.println("Time Taken (By Model " + i + "): " + m.model.time_taken);
                System.out.println("Model " + i + ": size=" + m.model.cache.size() + ", " + m.model.cache_hits + "/"
                        + m.model.calls + " hits (" + (double) m.model.cache_hits / m.model.calls + ")");
            }
            System.out.println("--------------------");
        }

        session.flushAndCloseFileWriters();
    }

    private static void startServer(GameState state) throws IOException {
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
                boolean playGamesStopped = false;
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
                            session = new MatchSession(1, modelDir, modelCmpDir);
                            numOfGames = 0;
                            newNumOfGames = 0;
                            playGamesStopped = false;
                        }
                        List<MatchSession.RemoteGameResult> results = new ArrayList<>();
                        int count = session.remoteDeq.size();
                        for (int i = 0; i < count; i++) {
                            results.add(session.playGamesRemote(state, req.remainingGames, req.nodeCount));
                        }
                        if (results.size() == 0) {
                            results.add(session.playGamesRemote(state, req.remainingGames, req.nodeCount));
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
                            session = new MatchSession(1, modelDir, modelCmpDir);
                        } else if (!playGamesStopped) {
                            session.stopPlayGamesRemote();
                            numOfGames = 0;
                            newNumOfGames = 0;
                            playGamesStopped = true;
                        }
                        List<MatchSession.TrainingGameResult> results = new ArrayList<>();
                        int count = session.remoteTrainingDeq.size();
                        for (int i = 0; i < count; i++) {
                            results.add(session.playTrainingGamesRemote(state, req.remainingGames, req.nodeCount));
                        }
                        if (results.size() == 0) {
                            results.add(session.playTrainingGamesRemote(state, req.remainingGames, req.nodeCount));
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

    private static void writeStateDescription(File f, GameState state) throws IOException {
        Writer writer = new BufferedWriter(new FileWriter(f));
        writer.write("************************** NN Description **************************\n");
        writer.write(state.getNNInputDesc());
        if (state.prop.randomization != null || state.prop.preBattleRandomization != null) {
            writer.write("\n************************** Randomizations **************************\n");
            var randomization = state.prop.preBattleRandomization;
            if (randomization == null) {
                randomization = state.prop.randomization;
            } else if (state.prop.randomization != null) {
                randomization = randomization.doAfter(state.prop.randomization);
            }
            int i = 1;
            for (var info : randomization.listRandomizations().values()) {
                writer.write(i + ". (" + Utils.formatFloat(info.chance() * 100) + "%) " + info.desc() + "\n");
                i += 1;
            }
        }
        writer.write("\n************************** Other **************************\n");
        var i = 1;
        for (var enemy : state.getEnemiesForRead()) {
            writer.write("Enemy " + (i++) + ": " + enemy.toString(state) + "\n");
        }
        writer.flush();
        writer.close();
    }
}
