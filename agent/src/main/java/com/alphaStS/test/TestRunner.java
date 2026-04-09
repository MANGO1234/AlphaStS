package com.alphaStS.test;

import com.alphaStS.GameState;
import com.alphaStS.GameStateBuilder;
import com.alphaStS.enemy.PredefinedEncounter;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.gui.BattleBuilderJsonWriter;
import com.alphaStS.random.RandomGen;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringJoiner;

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

    public void test(String runDataPath, int upto) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        int k = 0;
        for (BattleEntry entry : new RunDataParser(runDataPath)) {
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
            Files.createDirectories(runFilePath.getParent());
            Files.writeString(runFilePath, mapper.writeValueAsString(header) + "\n");
            System.out.println("[TestRunner] Wrote run file header: " + runFilePath);

            // Step 2: Play random moves via CommunicationMod until battle ends.
            playRandomMoves(rng);

            // Move the completed .run file to tests/<runIdx>_<battleIdx>_<seed>.run
            Files.createDirectories(Paths.get(TESTS_DIR));
            Path dest = Paths.get(TESTS_DIR, entry.getPlayId() + "_" + battleIdx + "_" + seed + ".run");
            Files.move(runFilePath, dest, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[TestRunner] Run file saved to: " + dest.toAbsolutePath());

            // Step 4: Replay the log through a GameState and assert states match after each action.
            try {
                replayLog(entry, dest);
                System.out.println("[TestRunner] Replay passed for run " + runIdx + ", battle " + battleIdx);
            } catch (Exception e) {
                System.err.println("[TestRunner] Replay failed for run " + runIdx + ", battle " + battleIdx + ": " + e.getMessage());
            }

            if (++k >= upto) {
                break;
            }
        }
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
    private void replayLog(BattleEntry entry, Path logFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<String> lines = Files.readAllLines(logFile);

        // Pre-pass: load replay events from the log into the queue.
        Queue<String[]> replayEventQueue = new LinkedList<>();
        for (String line : lines) {
            JsonNode node = mapper.readTree(line);
            String type = node.path("_type").asText();
            if ("event:shuffle".equals(type)) {
                JsonNode orderNode = node.path("deckOrder");
                String[] order = new String[orderNode.size()];
                for (int i = 0; i < orderNode.size(); i++) {
                    order[i] = orderNode.get(i).asText();
                }
                replayEventQueue.add(order);
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
        state.properties.random = new RandomGen.RandomGenPlain();

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
                TestReplay.compareStateFloor(line, state);
            } else if ("action:play_card".equals(type)) {
                // TODO: apply play_card action to state using card name and target_index from log
            } else if ("action:end_turn".equals(type)) {
                // TODO: apply end_turn action to state, advance through BEGIN_TURN
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
