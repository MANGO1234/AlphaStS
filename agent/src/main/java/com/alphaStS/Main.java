package com.alphaStS;

import com.alphaStS.utils.Utils;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
       var state = TestStates.TestState17();
//        ((RandomGen.RandomGenPlain) state.prop.random).random.setSeed(5);

        if (args.length > 0 && args[0].equals("--get-lengths")) {
            System.out.print(state.getNNInput().length + "," + state.prop.totalNumOfActions + "," + state.prop.extraOutputLen);
            return;
        }

        boolean GENERATE_TRAINING_GAMES = false;
        boolean TEST_TRAINING_AGENT = false;
        boolean PLAY_GAMES = false;
        boolean PLAY_A_GAME = false;
        boolean SLOW_TRAINING_WINDOW = false;
        boolean CURRICULUM_TRAINING_ON = false;
        boolean TRAINING_WITH_LINE = false;
        boolean GAMES_ADD_ENEMY_RANDOMIZATION = false;
        boolean GAMES_ADD_POTION_RANDOMIZATION = false;
        boolean GAMES_TEST_CHOOSE_SCENARIO_RANDOMIZATION = false;
        int POTION_STEPS = 1;
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
            if (args[i].equals("-slow")) {
                SLOW_TRAINING_WINDOW = true;
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
            NUMBER_OF_GAMES_TO_PLAY = 2400;
            GAMES_ADD_ENEMY_RANDOMIZATION = true;
            GAMES_ADD_POTION_RANDOMIZATION = true;
            GAMES_TEST_CHOOSE_SCENARIO_RANDOMIZATION = true;
            NUMBER_OF_NODES_PER_TURN = 200;
            iteration = 51;
            COMPARE_DIR = "../saves/iteration50";
//            COMPARE_DIR = SAVES_DIR + "/iteration" + (iteration - 2);
//            COMPARE_DIR = SAVES_DIR + "/iteration60";
        }
        NUMBER_OF_THREADS = 1;

        if (!GENERATE_TRAINING_GAMES && GAMES_ADD_ENEMY_RANDOMIZATION) {
            state.prop.randomization = new GameStateRandomization.EnemyRandomization(false).doAfter(state.prop.randomization);
        }
        if (!GENERATE_TRAINING_GAMES && GAMES_ADD_POTION_RANDOMIZATION && state.prop.potions.size() > 0) {
            var s = new ArrayList<Short>();
            for (int i = 0; i < state.prop.potions.size(); i++) {
                s.add((short) 90);
            }
            state.prop.randomization = new GameStateRandomization.PotionsUtilityRandomization(state.prop.potions, POTION_STEPS, s).fixR(1).doAfter(state.prop.randomization);
        } else if ((GENERATE_TRAINING_GAMES || TEST_TRAINING_AGENT) && state.prop.potions.size() > 0) {
            var s = new ArrayList<Short>();
            for (int i = 0; i < state.prop.potions.size(); i++) {
                s.add((short) 90);
            }
            state.prop.preBattleRandomization = new GameStateRandomization.PotionsUtilityRandomization(state.prop.potions, POTION_STEPS, s).fixR(1).doAfter(state.prop.preBattleRandomization);
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
            state.setActionCtx(GameActionCtx.BEGIN_BATTLE, null);
        }
//        state.prop.randomization = state.prop.randomization.fixR(0, 2, 4);

        ObjectMapper mapper = new ObjectMapper();
        String curIterationDir = SAVES_DIR + "/iteration0";
        try {
            JsonNode root = mapper.readTree(new File(SAVES_DIR + "/training.json"));
            iteration = iteration < 0 ? root.get("iteration").asInt() : iteration;
            curIterationDir = SAVES_DIR + "/iteration" + (iteration - 1);
            File f = new File(SAVES_DIR + "/desc.txt");
            if (!f.exists()) {
                writeStateDescription(f, state);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Unable to find neural network.");
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
        if (!TEST_TRAINING_AGENT && !GENERATE_TRAINING_GAMES) {
            session.scenariosGroup = GameStateUtils.getScenarioGroups(state, 4, 3);
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
            } else if (NUMBER_OF_GAMES_TO_PLAY <= 100) {
                session.setMatchLogFile("matches.txt.gz");
            }
            session.playGames(state, NUMBER_OF_GAMES_TO_PLAY, NUMBER_OF_NODES_PER_TURN, !TEST_TRAINING_AGENT);
        }
        if (GENERATE_TRAINING_GAMES && preBattleScenarios != null) {
            state.prop.preBattleScenarios = preBattleScenarios;
            state.prop.randomization = randomization;
            state.setActionCtx(GameActionCtx.SELECT_SCENARIO, null);
        }

        if (GENERATE_TRAINING_GAMES) {
            session.setTrainingDataLogFile("training_data.txt.gz");
            session.SLOW_TRAINING_WINDOW = SLOW_TRAINING_WINDOW;
            session.POLICY_CAP_ON = false;
            session.TRAINING_WITH_LINE = TRAINING_WITH_LINE;
            long start = System.currentTimeMillis();
            state.prop.randomization = new GameStateRandomization.EnemyRandomization(CURRICULUM_TRAINING_ON).doAfter(state.prop.randomization);
            session.playTrainingGames(state, 300, 100, curIterationDir + "/training_data.bin.lz4");
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
