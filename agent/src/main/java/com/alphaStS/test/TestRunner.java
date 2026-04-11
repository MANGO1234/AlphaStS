package com.alphaStS.test;

import com.alphaStS.GameState;
import com.alphaStS.GameStateBuilder;
import com.alphaStS.enemy.PredefinedEncounter;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.gameAction.GameActionType;
import com.alphaStS.gui.BattleBuilderJsonReader;
import com.alphaStS.gui.BattleBuilderJsonWriter;
import com.alphaStS.random.RandomGen;
import com.alphaStS.random.RandomGenTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.function.Predicate;

public class TestRunner {

    /** Host and port of the BattleLoaderMod TCP server running inside STS. */
    private final String BATTLE_LOADER_HOST;
    private static final int BATTLE_LOADER_PORT = 2345;

    public TestRunner() {
        this.BATTLE_LOADER_HOST = "localhost";
        this.COMM_MOD_HOST = "localhost";
    }

    public TestRunner(String host) {
        this.BATTLE_LOADER_HOST = host;
        this.COMM_MOD_HOST = host;
    }

    /** Host and port of the sts-comm-mod-server TCP proxy (bridges CommunicationMod). */
    private final String COMM_MOD_HOST;
    private static final int COMM_MOD_PORT = 2346;

    /** STS save files, one per character — deleted and recreated before each battle. */
    private static final String STS_SAVES_DIR = isWsl()
            ? "/mnt/f/SteamLibrary/steamapps/common/SlayTheSpire/saves/"
            : "F:\\SteamLibrary\\steamapps\\common\\SlayTheSpire\\saves\\";
    private static final String RUN_FILE_IRONCLAD = STS_SAVES_DIR + "1_IRONCLAD.run.log";
    private static final String RUN_FILE_SILENT   = STS_SAVES_DIR + "1_SILENT.run.log";
    private static final String RUN_FILE_DEFECT   = STS_SAVES_DIR + "1_DEFECT.run.log";
    private static final String RUN_FILE_WATCHER  = STS_SAVES_DIR + "1_WATCHER.run.log";

    /** Maximum number of turns before the random-move bot aborts the battle. */
    private static final int MAX_TURNS = 30;

    /** Output directory for completed run files. */
    private static final String TESTS_DIR = "tests";

    /**
     * Dispatches the {@code --replay-test} command and its subcommands.
     */
    public static void replayTest(String[] args) {
        String subCmd = args.length > 1 ? args[1] : "--parse-historical-data";
        if (subCmd.equals("--parse-historical-data")) {
            if (args.length < 3) {
                System.err.println("Usage: --replay-test --parse-historical-data <historical-data-path> [--filter <spec>]");
                System.err.println("  --filter <spec>  comma-separated list of {run}:{battle} selectors");
                System.err.println("                   run/battle may each be *, N, or N-M (inclusive)");
                System.err.println("                   e.g. --filter 0:*,1:2-5,*:0");
                return;
            }
            String path = args[2];
            Predicate<BattleEntry> filter = null;
            for (int i = 3; i < args.length; i++) {
                if (args[i].equals("--filter") && i + 1 < args.length) {
                    filter = parseFilter(args[i + 1]);
                    i++;
                }
            }
            parseHistoricalData(path, filter);
        } else if (subCmd.equals("--generate-runs")) {
            if (args.length < 3) {
                System.err.println("Usage: --replay-test --generate-runs <historical-data-path> [--filter <spec>] [--ip host] [--replay]");
                System.err.println("  --filter <spec>  comma-separated list of {run}:{battle} selectors");
                System.err.println("                   run/battle may each be *, N, or N-M (inclusive)");
                System.err.println("                   e.g. --filter 0:*,1:2-5,*:0");
                return;
            }
            String path = args[2];
            Predicate<BattleEntry> filter = null;
            String ip = "localhost";
            boolean doReplay = false;
            for (int i = 3; i < args.length; i++) {
                if (args[i].equals("--filter") && i + 1 < args.length) {
                    filter = parseFilter(args[i + 1]);
                    i++;
                } else if (args[i].equals("--ip") && i + 1 < args.length) {
                    ip = args[i + 1];
                    i++;
                } else if (args[i].equals("--replay")) {
                    doReplay = true;
                }
            }
            try {
                new TestRunner(ip).generateRuns(path, filter, doReplay);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (subCmd.equals("--replay-run")) {
            if (args.length < 3) {
                System.err.println("Usage: --replay-test --replay-run <run-log-path> [--verbose]");
                return;
            }
            String runLogPath = args[2];
            boolean verbose = false;
            for (int i = 3; i < args.length; i++) {
                if (args[i].equals("--verbose")) verbose = true;
            }
            try {
                new TestRunner().replayRunFile(Paths.get(runLogPath), verbose);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            System.err.println("Unknown --replay-test subcommand: " + subCmd);
            System.err.println("Valid subcommands: --parse-historical-data, --generate-runs, --replay-run");
        }
    }

    /**
     * Parses a historical run data file and prints a summary of each matching battle entry.
     */
    public static void parseHistoricalData(String path, Predicate<BattleEntry> filter) {
        System.out.println("Parsing run data from: " + path);
        RunDataParser parser = new RunDataParser(path);
        if (filter != null) {
            parser = parser.withFilter(filter);
        }
        int totalBattles = 0;
        List<BattleEntry> runs = new ArrayList<>();
        parser.iterator().forEachRemaining(runs::add);
        for (var run : runs) {
            int runIdx = run.getRunIdx();
            int battleIdx = run.getBattleIdx();
            GameStateBuilder builder = run.getBuilder();
            var cards = builder.getCards();
            var relics = builder.getRelics();
            var potions = builder.getPotions();
            System.out.printf("Run %d, Battle %d \u2014 %d cards, %d relics, %d potions, HP %d/%d%n",
                runIdx,
                battleIdx,
                cards.size(),
                relics.size(),
                potions.size(),
                builder.getPlayer().getHealth(),
                builder.getPlayer().getMaxHealth());
            totalBattles++;
        }
        System.out.println("Total battles: " + totalBattles);
    }

    /**
     * Generates replay run files from a historical run data file by playing random moves in STS.
     * Step 4 (replay validation) is optional and controlled by {@code doReplay}.
     */
    public void generateRuns(String runDataPath, Predicate<BattleEntry> filter, boolean doReplay) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        RunDataParser parser = new RunDataParser(runDataPath);
        if (filter != null) {
            parser = parser.withFilter(filter);
        }
        for (BattleEntry entry : parser) {
            int runIdx = entry.getRunIdx();
            int battleIdx = entry.getBattleIdx();
            System.out.println("[TestRunner] Run " + runIdx + ", Battle " + battleIdx);

            // Step 1: Send the battle definition JSON to the BattleLoaderMod running in STS.
            entry.getBuilder().getPlayer().setInBattleMaxHealth(entry.getBuilder().getPlayer().getMaxHealth() + 50000);
            entry.getBuilder().getPlayer().setHealth(entry.getBuilder().getPlayer().getMaxHealth() + 50000);
            boolean ok = sendBattleDefinition(entry);
            if (!ok) {
                System.err.println("[TestRunner] Skipping battle due to failed setup.");
                continue;
            }

            // Delete the old .run save file and create a new one with the battle header.
            CharacterEnum character = entry.getBuilder().getCharacter();
            Path runFilePath = Paths.get(getRunFilePath(character));
            Files.deleteIfExists(runFilePath);

            RandomGen rng = new RandomGen.RandomGenPlain();
            long seed = rng.getStartingSeed();
            ObjectNode header = mapper.createObjectNode();
            header.put("play_id", entry.getPlayId());
            header.put("battle_idx", battleIdx);
            header.put("seed", seed);
            JsonNode battleDefJson = mapper.readTree(BattleBuilderJsonWriter.toJson(entry.getBuilder()));
            header.set("battle_definition", battleDefJson.get("battle_definition"));
            header.put("encounter", entry.getEnemiesName());
            Files.createDirectories(runFilePath.getParent());
            Files.writeString(runFilePath, mapper.writeValueAsString(header) + "\n");
            System.out.println("[TestRunner] Wrote run file header: " + runFilePath);

            // Step 2: Play random moves via CommunicationMod until battle ends.
            playRandomMoves(rng);

            // Move the completed .run file to tests/<play_id>_<battleIdx>_<seed>.run
            Files.createDirectories(Paths.get(TESTS_DIR));
            Path dest = Paths.get(TESTS_DIR, entry.getPlayId() + "_" + battleIdx + "_" + seed + ".run");
            Files.move(runFilePath, dest, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[TestRunner] Run file saved to: " + dest.toAbsolutePath());

            // Step 4 (optional): Replay the log through a GameState and assert states match after each action.
            if (doReplay) {
                try {
                    replayLog(entry, dest, false);
                    System.out.println("[TestRunner] Replay passed for run " + runIdx + ", battle " + battleIdx);
                } catch (Exception e) {
                    System.err.println("[TestRunner] Replay failed for run " + runIdx + ", battle " + battleIdx + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Replays a run log file, reading the battle setup from the embedded header and
     * validating the simulated game state after each action.
     */
    public void replayRunFile(Path logFile, boolean verbose) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<String> lines = Files.readAllLines(logFile);
        if (lines.isEmpty()) {
            throw new ReplayException("Empty run log file: " + logFile, null, null);
        }
        JsonNode header = mapper.readTree(lines.get(0));
        String playId = header.path("play_id").asText();
        int battleIdx = header.path("battle_idx").asInt(-1);

        JsonNode battleDefNode = header.path("battle_definition");
        if (battleDefNode.isMissingNode()) {
            throw new ReplayException("Run log header missing 'battle_definition'", null, null);
        }
        String enemiesName = header.path("encounter").asText();
        if (enemiesName.isEmpty()) {
            throw new ReplayException("Run log header missing 'encounter'", null, null);
        }

        ObjectNode wrappedDef = mapper.createObjectNode();
        wrappedDef.set("battle_definition", battleDefNode);
        GameStateBuilder builder = BattleBuilderJsonReader.fromJson(mapper.writeValueAsString(wrappedDef));

        BattleEntry entry = new BattleEntry(0, battleIdx, builder, enemiesName, playId);
        replayLog(entry, logFile, verbose);
        System.out.println("[TestRunner] Replay passed for play_id=" + playId + ", battle_idx=" + battleIdx);
    }

    /**
     * Plays random moves via the sts-comm-mod-server TCP proxy until the battle ends or the
     * turn limit is reached.
     *
     * <p>The server sends a newline-delimited JSON game state after each command. The bot
     * dispatches on {@code game_state.screen_type} to decide what action to take.
     *
     * @param rng   the RNG to use for all random choices (seed already recorded by caller)
     */
    private void playRandomMoves(RandomGen rng) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        try (Socket socket = new Socket(COMM_MOD_HOST, COMM_MOD_PORT)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            String stateLine = reader.readLine();
            boolean waitingOnReady = true;
            while (stateLine != null) {
                if (waitingOnReady) {
                    ObjectNode cmdNode = mapper.createObjectNode();
                    cmdNode.put("command", "STATE");
                    writer.println(mapper.writeValueAsString(cmdNode));
                    stateLine = reader.readLine();
                } else {
                    stateLine = reader.readLine();
                }

                JsonNode state = mapper.readTree(stateLine);
                if (!state.path("ready_for_command").asBoolean(false)) {
                    waitingOnReady = true;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }
                waitingOnReady = false;

                JsonNode gameState = state.get("game_state");
                if (gameState == null) {
                    System.out.println("[TestRunner] No game_state in response; ending bot.");
                    break;
                }

                String roomPhase = gameState.path("room_phase").asText("");
                if ("COMPLETE".equals(roomPhase) || "GAME_OVER".equals(roomPhase)) {
                    System.out.println("[TestRunner] Battle complete (room_phase=" + roomPhase + ").");
                    break;
                }

                int turn = gameState.path("combat_state").path("turn").asInt(0);
                if (turn > MAX_TURNS) {
                    System.out.println("[TestRunner] Turn limit reached (" + MAX_TURNS + "); ending bot.");
                    break;
                }

                String screenType = gameState.path("screen_type").asText("NONE");
                String command = chooseCommand(gameState, screenType, rng);

                if (command == null) {
                    System.out.println("[TestRunner] No command chosen; ending bot.");
                    break;
                }

                System.out.println("[TestRunner] Sending: " + command);
                ObjectNode cmdNode = mapper.createObjectNode();
                cmdNode.put("command", command);
                writer.println(mapper.writeValueAsString(cmdNode));
            }
        }
    }

    /**
     * Replays a completed {@code .run} log file through an alphaStS {@link GameState}, comparing
     * each logged {@code state:floor} against the simulated state after the corresponding action.
     *
     * @throws Exception if the enemies name is unknown, or if a state mismatch is detected
     */
    private void replayLog(BattleEntry entry, Path logFile, boolean verbose) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<String> lines = Files.readAllLines(logFile);

        // Pre-pass: load all replay events into a single ordered queue.
        Queue<TestReplayEvent> replayEventQueue = new LinkedList<>();
        for (String line : lines) {
            JsonNode node = mapper.readTree(line);
            String type = node.path("_type").asText();
            if ("event:shuffle".equals(type)) {
                JsonNode orderNode = node.path("deckOrder");
                String[] order = new String[orderNode.size()];
                for (int i = 0; i < orderNode.size(); i++) {
                    order[i] = orderNode.get(i).asText();
                }
                replayEventQueue.add(new TestReplayEvent.ShuffleEvent(order));
            } else if ("event:enemy_hp".equals(type)) {
                replayEventQueue.add(new TestReplayEvent.EnemyHpEvent(
                    node.path("min_hp").asInt(),
                    node.path("max_hp").asInt(),
                    node.path("chosen_hp").asInt()));
            }
        }

        // Build the GameState for this battle.
        GameStateBuilder builder = entry.getBuilder();
        boolean found = PredefinedEncounter.addToGameStateBuilder(builder, entry.getEnemiesName());
        if (!found) {
            throw new ReplayException("Unknown enemies name: " + entry.getEnemiesName(), null, null);
        }
        GameState state = new GameState(builder);
        state.properties.testingReplayMode = true;
        state.properties.replayEventQueue = replayEventQueue;
        state.properties.random = new RandomGenTest(replayEventQueue);

        // The first ShuffleEvent in the queue is the initial deck order STS chose before the first
        // draw. EnemyHpEvents precede it in the queue and are left in place — RandomGenTest will
        // consume them during pre-battle advancement. We use an iterator to remove only the
        // ShuffleEvent without disturbing the EnemyHpEvents ahead of it.
        TestReplayEvent.ShuffleEvent initialShuffle = null;
        Iterator<TestReplayEvent> queueIter = ((LinkedList<TestReplayEvent>) replayEventQueue).iterator();
        while (queueIter.hasNext()) {
            TestReplayEvent evt = queueIter.next();
            if (evt instanceof TestReplayEvent.ShuffleEvent se) {
                initialShuffle = se;
                queueIter.remove();
                break;
            }
        }
        if (initialShuffle != null) {
            state.deckArr = new short[initialShuffle.deckOrder.length + 2];
            state.deckArrLen = 0;
            for (String cardName : initialShuffle.deckOrder) {
                for (int i = 0; i < state.properties.cardDict.length; i++) {
                    if (state.properties.cardDict[i].cardName.equals(cardName)) {
                        state.deckArr[state.deckArrLen++] = (short) i;
                        break;
                    }
                }
            }
            state.deckArrFixedDrawLen = state.deckArrLen;
        }

        // Advance through pre-battle contexts to reach PLAY_CARD.
        while (state.actionCtx == GameActionCtx.BEGIN_PRE_BATTLE ||
               state.actionCtx == GameActionCtx.BEGIN_BATTLE ||
               state.actionCtx == GameActionCtx.BEGIN_TURN ||
               state.actionCtx == GameActionCtx.AFTER_RANDOMIZATION) {
            state = state.doAction(0);
        }

        // Replay: compare state:floor entries and apply actions.
        for (String line : lines) {
            JsonNode node = mapper.readTree(line);
            String type = node.path("_type").asText();

            if ("state:floor".equals(type)) {
                if (verbose) {
                    System.out.println("-------------------------");
                    System.out.println("Sim: " + state);
                    System.out.println("Log: " + line);
                    System.out.println("-------------------------");
                }
                TestReplay.compareStateFloor(line, state);
            } else if ("action:play_card".equals(type)) {
                if (verbose) System.out.println(line);
                String cardName = node.path("card").asText();
                int targetIndex = node.path("target_index").asInt(-1);

                int[] legal = state.getLegalActions();
                int actionIdx = -1;
                for (int i = 0; i < legal.length; i++) {
                    var a = state.properties.actionsByCtx[state.actionCtx.ordinal()][legal[i]];
                    if (a.type() == GameActionType.PLAY_CARD
                            && state.properties.cardDict[a.idx()].cardName.equals(cardName)) {
                        actionIdx = i;
                        break;
                    }
                }
                if (actionIdx < 0) {
                    throw new ReplayException("Card not found in legal actions: " + cardName, state, line);
                }
                state = state.doAction(actionIdx);

                if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                    int[] enemyLegal = state.getLegalActions();
                    int enemyPos = -1;
                    for (int i = 0; i < enemyLegal.length; i++) {
                        if (enemyLegal[i] == targetIndex) {
                            enemyPos = i;
                            break;
                        }
                    }
                    if (enemyPos < 0) {
                        throw new ReplayException(
                            "Target enemy " + targetIndex + " not in legal actions", state, line);
                    }
                    state = state.doAction(enemyPos);
                }
            } else if ("action:end_turn".equals(type)) {
                if (verbose) System.out.println(line);
                // END_TURN is always the last legal action in PLAY_CARD context.
                state = state.doAction(state.getLegalActions().length - 1);
                // Advance through BEGIN_TURN to reach PLAY_CARD.
                while (state.actionCtx == GameActionCtx.BEGIN_TURN) {
                    state = state.doAction(0);
                }
            } else if (verbose) {
                System.out.println(line);
            }
        }
    }

    /**
     * Chooses a CommunicationMod command string based on the current game state and screen type.
     *
     * @return the command string (e.g. {@code "PLAY 1 0"}, {@code "END"}, {@code "CHOOSE 2"}),
     *         or {@code null} if no action could be determined
     */
    private String chooseCommand(JsonNode gameState, String screenType, RandomGen rng) {
        switch (screenType) {
            case "NONE": {
                return chooseNoneScreenCommand(gameState, rng);
            }

            case "HAND_SELECT": {
                JsonNode screenState = gameState.path("screen_state");
                JsonNode hand = screenState.path("hand");
                int handSize = hand.size();
                if (handSize == 0) {
                    return "CONFIRM";
                }
                // Pick a random index from the hand to select.
                int pick = rng.nextInt(handSize, null);
                return "CHOOSE " + pick;
            }

            case "GRID": {
                JsonNode screenState = gameState.path("screen_state");
                JsonNode cards = screenState.path("cards");
                int cardCount = cards.size();
                if (cardCount == 0) {
                    return "CONFIRM";
                }
                boolean anyNumber = screenState.path("any_number").asBoolean(false);
                boolean confirmUp = screenState.path("confirm_up").asBoolean(false);

                if (anyNumber) {
                    // Each card has a 50% chance — pick the first unselected card that passes.
                    // We send one CHOOSE at a time; track nothing across turns (stateless approach:
                    // just pick one random card per call; the screen loops until confirmed).
                    List<Integer> candidates = new ArrayList<>();
                    JsonNode selected = screenState.path("selected_cards");
                    for (int i = 0; i < cardCount; i++) {
                        candidates.add(i);
                    }
                    // Remove already-selected cards from candidates.
                    for (JsonNode sel : selected) {
                        String selUuid = sel.path("uuid").asText();
                        for (int i = 0; i < cardCount; i++) {
                            if (cards.get(i).path("uuid").asText().equals(selUuid)) {
                                candidates.remove(Integer.valueOf(i));
                                break;
                            }
                        }
                    }
                    if (candidates.isEmpty() || confirmUp) {
                        return "CONFIRM";
                    }
                    // 50% chance to pick a card vs confirm.
                    if (rng.nextBoolean(null)) {
                        return "CHOOSE " + candidates.get(rng.nextInt(candidates.size(), null));
                    } else {
                        return "CONFIRM";
                    }
                } else {
                    int numCards = screenState.path("num_cards").asInt(1);
                    JsonNode selected = screenState.path("selected_cards");
                    int alreadySelected = selected.size();
                    if (alreadySelected >= numCards) {
                        return "CONFIRM";
                    }
                    // Build list of unselected card indices.
                    List<Integer> unselected = new ArrayList<>();
                    for (int i = 0; i < cardCount; i++) {
                        unselected.add(i);
                    }
                    for (JsonNode sel : selected) {
                        String selUuid = sel.path("uuid").asText();
                        for (int i = 0; i < cardCount; i++) {
                            if (cards.get(i).path("uuid").asText().equals(selUuid)) {
                                unselected.remove(Integer.valueOf(i));
                                break;
                            }
                        }
                    }
                    if (unselected.isEmpty()) {
                        return "CONFIRM";
                    }
                    return "CHOOSE " + unselected.get(rng.nextInt(unselected.size(), null));
                }
            }

            case "CARD_REWARD": {
                JsonNode screenState = gameState.path("screen_state");
                JsonNode cards = screenState.path("cards");
                boolean skipAvailable = screenState.path("skip_available").asBoolean(false);
                int cardCount = cards.size();

                if (cardCount == 0) {
                    return "RETURN";
                }

                if (skipAvailable) {
                    // 4% skip, 96% pick a card.
                    if (rng.nextInt(100, null) < 4) {
                        return "RETURN";
                    }
                }
                return "CHOOSE " + rng.nextInt(cardCount, null);
            }

            default: {
                System.out.println("[TestRunner] Unhandled screen_type: " + screenType + "; sending CONFIRM.");
                return "CONFIRM";
            }
        }
    }

    /**
     * Builds a weighted action list for the NONE screen (standard combat) and picks one.
     *
     * <p>Each playable card and usable potion has weight 19; END has weight 1.
     */
    private String chooseNoneScreenCommand(JsonNode gameState, RandomGen rng) {
        JsonNode combatState = gameState.path("combat_state");
        JsonNode hand = combatState.path("hand");
        JsonNode potions = gameState.path("potions");
        JsonNode monsters = combatState.path("monsters");

        // Collect live monster indices for targeted actions.
        List<Integer> liveMonsters = new ArrayList<>();
        for (int i = 0; i < monsters.size(); i++) {
            if (!monsters.get(i).path("is_gone").asBoolean(false)) {
                liveMonsters.add(i);
            }
        }

        // Build weighted action list: (command string, weight).
        List<String> actions = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();

        for (int i = 0; i < hand.size(); i++) {
            JsonNode card = hand.get(i);
            if (!card.path("is_playable").asBoolean(false)) {
                continue;
            }
            boolean hasTarget = card.path("has_target").asBoolean(false);
            String cmd;
            if (hasTarget && !liveMonsters.isEmpty()) {
                int targetIdx = liveMonsters.get(rng.nextInt(liveMonsters.size(), null));
                cmd = "PLAY " + (i + 1) + " " + targetIdx;
            } else if (hasTarget) {
                // No live target available; skip this card.
                continue;
            } else {
                cmd = "PLAY " + (i + 1);
            }
            actions.add(cmd);
            weights.add(19);
        }

        for (int i = 0; i < potions.size(); i++) {
            JsonNode potion = potions.get(i);
            if (!potion.path("can_use").asBoolean(false)) {
                continue;
            }
            boolean requiresTarget = potion.path("requires_target").asBoolean(false);
            String cmd;
            if (requiresTarget && !liveMonsters.isEmpty()) {
                int targetIdx = liveMonsters.get(rng.nextInt(liveMonsters.size(), null));
                cmd = "POTION Use " + i + " " + targetIdx;
            } else if (requiresTarget) {
                continue;
            } else {
                cmd = "POTION Use " + i;
            }
            actions.add(cmd);
            weights.add(19);
        }

        // END always available with weight 1.
        actions.add("END");
        weights.add(1);

        // Weighted random selection.
        int total = weights.stream().mapToInt(Integer::intValue).sum();
        int roll = rng.nextInt(total, null);
        int cumulative = 0;
        for (int i = 0; i < actions.size(); i++) {
            cumulative += weights.get(i);
            if (roll < cumulative) {
                return actions.get(i);
            }
        }

        return "END";
    }

    /**
     * Parses a comma-separated filter spec into a predicate over BattleEntry.
     * Each token has the form {@code {run}:{battle}} where each part is
     * {@code *}, a single index {@code N}, or an inclusive range {@code N-M}.
     * Examples: {@code 0:*}, {@code 1:2-5}, {@code *:0}, {@code 0-2:*}
     */
    static Predicate<BattleEntry> parseFilter(String spec) {
        Predicate<BattleEntry> result = e -> false;
        for (String token : spec.split(",")) {
            token = token.trim();
            int colon = token.indexOf(':');
            if (colon < 0) {
                throw new IllegalArgumentException("Invalid filter token (missing ':'): " + token);
            }
            Predicate<Integer> runPred    = parseIndexSpec(token.substring(0, colon));
            Predicate<Integer> battlePred = parseIndexSpec(token.substring(colon + 1));
            result = result.or(e -> runPred.test(e.getRunIdx()) && battlePred.test(e.getBattleIdx()));
        }
        return result;
    }

    /** Parses a single index spec: {@code *}, {@code N}, or {@code N-M} (inclusive). */
    private static Predicate<Integer> parseIndexSpec(String spec) {
        if (spec.equals("*")) {
            return i -> true;
        }
        int dash = spec.indexOf('-');
        if (dash > 0) {
            int start = Integer.parseInt(spec.substring(0, dash));
            int end   = Integer.parseInt(spec.substring(dash + 1));
            return i -> i >= start && i <= end;
        }
        int exact = Integer.parseInt(spec);
        return i -> i == exact;
    }

    /**
     * Returns the save-file path for the given character.
     */
    private static boolean isWsl() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("linux")) return false;
        try {
            String procVersion = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("/proc/version")));
            return procVersion.toLowerCase().contains("microsoft");
        } catch (Exception e) {
            return false;
        }
    }

    private static String getRunFilePath(CharacterEnum character) {
        return switch (character) {
            case IRONCLAD -> RUN_FILE_IRONCLAD;
            case SILENT   -> RUN_FILE_SILENT;
            case DEFECT   -> RUN_FILE_DEFECT;
            case WATCHER  -> RUN_FILE_WATCHER;
        };
    }

    /**
     * Sends a battle definition JSON to the BattleLoaderMod TCP server and waits for
     * the response before returning.
     *
     * <p>Builds the JSON from the entry's {@link com.alphaStS.GameStateBuilder} and
     * injects a {@code "combat"} array (containing the enemies name) so the mod can
     * restart the correct encounter.
     *
     * <p>The mod responds with a JSON object whose {@code result} field is {@code "OK"} on
     * success, or {@code "ERROR"} with an {@code errorType} of {@code GENERAL},
     * {@code LOAD_ERROR}, or {@code CRASH} on failure.
     *
     * @param entry the battle entry produced by {@link RunDataParser}
     * @return {@code true} if the mod applied the definition successfully; {@code false} otherwise
     */
    private boolean sendBattleDefinition(BattleEntry entry) {
        ObjectMapper mapper = new ObjectMapper();
        String baseJson = BattleBuilderJsonWriter.toJson(entry.getBuilder());
        ObjectNode root;
        try {
            root = (ObjectNode) mapper.readTree(baseJson);
        } catch (Exception e) {
            System.err.println("[TestRunner] Failed to parse generated battle JSON: " + e.getMessage());
            return false;
        }

        ObjectNode def = (ObjectNode) root.get("battle_definition");
        ArrayNode combatNode = def.putArray("combat");
        combatNode.add(entry.getEnemiesName());

        String json;
        try {
            json = mapper.writeValueAsString(root);
        } catch (Exception e) {
            System.err.println("[TestRunner] Failed to serialize battle definition with combat: " + e.getMessage());
            return false;
        }

        try (Socket socket = new Socket(BATTLE_LOADER_HOST, BATTLE_LOADER_PORT)) {
            // Send JSON then close our write end so the mod sees EOF.
            PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            writer.println(json);
            System.out.println("[TestRunner] Sent battle definition: " + json);
            socket.shutdownOutput();

            // Read the response (single line of JSON).
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            String responseStr = response.toString();
            JsonNode responseRoot;
            try {
                responseRoot = mapper.readTree(responseStr);
            } catch (Exception e) {
                System.err.println("[TestRunner] Unparseable response from BattleLoaderMod: " + responseStr);
                return false;
            }

            String result = responseRoot.path("result").asText();
            if ("OK".equals(result)) {
                System.out.println("[TestRunner] BattleLoaderMod applied battle definition successfully.");
                return true;
            }

            String errorType = responseRoot.path("errorType").asText();
            switch (errorType) {
                case "GENERAL":
                    System.err.println("[TestRunner] BattleLoaderMod error: "
                            + responseRoot.path("errorMessage").asText());
                    break;
                case "LOAD_ERROR": {
                    StringJoiner sj = new StringJoiner(", ");
                    for (JsonNode err : responseRoot.path("errors")) {
                        sj.add(err.asText());
                    }
                    System.err.println("[TestRunner] BattleLoaderMod load errors: " + sj);
                    break;
                }
                case "CRASH": {
                    StringJoiner sj = new StringJoiner("\n");
                    for (JsonNode frame : responseRoot.path("stackTrace")) {
                        sj.add(frame.asText());
                    }
                    System.err.println("[TestRunner] BattleLoaderMod crash:\n" + sj);
                    break;
                }
                default:
                    System.err.println("[TestRunner] Unexpected response from BattleLoaderMod: " + responseStr);
            }
            return false;

        } catch (IOException e) {
            System.err.println("[TestRunner] Connection to BattleLoaderMod failed: " + e.getMessage());
            return false;
        }
    }
}
