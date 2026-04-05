package com.alphaStS.test;

import com.alphaStS.gui.BattleBuilderJsonWriter;
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
import java.util.Iterator;
import java.util.StringJoiner;

public class TestRunner {

    /** Host and port of the BattleLoaderMod TCP server running inside STS. */
    private static final String BATTLE_LOADER_HOST = "localhost";
    private static final int BATTLE_LOADER_PORT = 2345;

    public void test(String runDataPath) throws Exception {
        Iterator<BattleEntry> runData = new RunDataParser(runDataPath).iterator();

        while (runData.hasNext()) {
            BattleEntry entry = runData.next();
            int runIdx = entry.getRunIdx();
            int battleIdx = entry.getBattleIdx();
            System.out.println("[TestRunner] Run " + runIdx + ", Battle " + battleIdx);

            // Step 1: Send the battle definition JSON to the BattleLoaderMod running in STS.
            // The mod clears the current deck/relics/potions, fills them from the JSON, and
            // restarts the current battle.
            sendBattleDefinition(entry);

            // TODO 2: Using communication mod and a random move bot + player having 10000 hp,
            // make random moves until battle ends (e.g. 30 turns limit).

            // TODO 3: The states, actions, and rng rolls will be logged to .log (via a mod).

            // TODO 4: Use .log, construct a GameState of the same battle, replay from the start
            // of log until the end and check if the states match after each action.

            break;
        }
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
     * @throws IOException if the connection fails or the mod returns an error response
     */
    private void sendBattleDefinition(BattleEntry entry) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String baseJson = BattleBuilderJsonWriter.toJson(entry.getBuilder());
        ObjectNode root;
        try {
            root = (ObjectNode) mapper.readTree(baseJson);
        } catch (Exception e) {
            throw new IOException("[TestRunner] Failed to parse generated battle JSON", e);
        }

        ObjectNode def = (ObjectNode) root.get("battle_definition");
        ArrayNode combatNode = def.putArray("combat");
        combatNode.add(entry.getEnemiesName());

        String json;
        try {
            json = mapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new IOException("[TestRunner] Failed to serialize battle definition with combat", e);
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
                throw new IOException("[TestRunner] Unparseable response from BattleLoaderMod: " + responseStr);
            }

            String result = responseRoot.path("result").asText();
            if ("OK".equals(result)) {
                System.out.println("[TestRunner] BattleLoaderMod applied battle definition successfully.");
                return;
            }

            String errorType = responseRoot.path("errorType").asText();
            switch (errorType) {
                case "GENERAL":
                    throw new IOException("[TestRunner] BattleLoaderMod error: "
                            + responseRoot.path("errorMessage").asText());
                case "LOAD_ERROR": {
                    StringJoiner sj = new StringJoiner(", ");
                    for (JsonNode err : responseRoot.path("errors")) {
                        sj.add(err.asText());
                    }
                    throw new IOException("[TestRunner] BattleLoaderMod load errors: " + sj);
                }
                case "CRASH": {
                    StringJoiner sj = new StringJoiner("\n");
                    for (JsonNode frame : responseRoot.path("stackTrace")) {
                        sj.add(frame.asText());
                    }
                    throw new IOException("[TestRunner] BattleLoaderMod crash:\n" + sj);
                }
                default:
                    throw new IOException("[TestRunner] Unexpected response from BattleLoaderMod: " + responseStr);
            }
        }
    }
}
