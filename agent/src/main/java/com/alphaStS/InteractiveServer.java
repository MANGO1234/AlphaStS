package com.alphaStS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * HTTP server for interactive mode API access.
 */
public class InteractiveServer {

    /**
     * Starts an HTTP server for interactive mode API access.
     *
     * <h2>Usage</h2>
     * <pre>
     * java -cp &lt;classpath&gt; com.alphaStS.Main --interactive-server [--port &lt;port&gt;]
     * </pre>
     *
     * <h2>Options</h2>
     * <ul>
     *   <li><b>--port &lt;port&gt;</b>: Port to listen on (default: 7999)</li>
     * </ul>
     *
     * <h2>API Endpoint</h2>
     * <b>POST /execute</b>
     *
     * <h3>Request Body (JSON)</h3>
     * <pre>
     * {
     *   "commands": ["cmd1", "cmd2", ...],  // Required: array of commands to execute
     *   "sessionId": "uuid"                  // Optional: session ID to continue existing session
     * }
     * </pre>
     *
     * <h3>Response Body (JSON)</h3>
     * <p>Success:</p>
     * <pre>
     * {
     *   "success": true,
     *   "output": "output of last command",
     *   "sessionId": "uuid"
     * }
     * </pre>
     * <p>Error:</p>
     * <pre>
     * {
     *   "success": false,
     *   "error": "error message"
     * }
     * </pre>
     *
     * <h2>Session Management</h2>
     * <p>Sessions maintain command history across requests. Omit sessionId to create a new session.
     * Include sessionId to continue an existing session with accumulated history.</p>
     */
    public static void start(GameState state, String[] args) throws IOException {
        // Parse port from args (--port <port>), default to 7999
        int port = 7999;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--port") && i + 1 < args.length) {
                port = Integer.parseInt(args[i + 1]);
                break;
            }
        }

        // Session storage: sessionId -> InteractiveSession
        Map<String, InteractiveSession> sessions = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", port), 0);

        server.createContext("/execute", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                if (!"POST".equals(exchange.getRequestMethod())) {
                    String response = "{\"success\":false,\"error\":\"Method not allowed\"}";
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(405, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes(StandardCharsets.UTF_8));
                    }
                    return;
                }

                String responseBody;
                int statusCode;
                try {
                    // Read request body
                    String requestBody;
                    try (InputStream is = exchange.getRequestBody()) {
                        requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    }

                    // Parse JSON request - expect {"commands": ["cmd1", ...], "sessionId": "optional"}
                    JsonNode requestNode = mapper.readTree(requestBody);
                    JsonNode commandsNode = requestNode.get("commands");
                    if (commandsNode == null || !commandsNode.isArray()) {
                        responseBody = "{\"success\":false,\"error\":\"Missing or invalid 'commands' array\"}";
                        statusCode = 400;
                    } else {
                        List<String> commands = new ArrayList<>();
                        for (JsonNode cmdNode : commandsNode) {
                            commands.add(cmdNode.asText());
                        }

                        // Get or create session
                        String sessionId = null;
                        JsonNode sessionIdNode = requestNode.get("sessionId");
                        if (sessionIdNode != null && !sessionIdNode.isNull()) {
                            sessionId = sessionIdNode.asText();
                        }

                        InteractiveSession session;
                        if (sessionId != null && sessions.containsKey(sessionId)) {
                            session = sessions.get(sessionId);
                        } else {
                            // Create new session
                            sessionId = UUID.randomUUID().toString();
                            session = new InteractiveSession(sessionId, state);
                            sessions.put(sessionId, session);
                        }

                        // Execute commands and get output
                        String output = session.executeCommands(commands);

                        ObjectNode responseNode = mapper.createObjectNode();
                        responseNode.put("success", true);
                        responseNode.put("output", output);
                        responseNode.put("sessionId", sessionId);
                        responseBody = mapper.writeValueAsString(responseNode);
                        statusCode = 200;
                    }
                } catch (Exception e) {
                    ObjectNode responseNode = mapper.createObjectNode();
                    responseNode.put("success", false);
                    responseNode.put("error", e.getClass().getName() + ": " + e.getMessage());
                    responseBody = mapper.writeValueAsString(responseNode);
                    statusCode = 500;
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(statusCode, responseBytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Interactive server started on localhost:" + port);
        System.out.println("POST /execute with JSON body: {\"commands\": [\"cmd1\", ...], \"sessionId\": \"optional\"}");
    }

    private static String extractLastCommandOutput(String fullOutput) {
        // Split by the prompt pattern "> " at the start of lines
        // The output format is: "> cmd\n[output]\n> cmd\n[output]\n..."
        String[] parts = fullOutput.split("\n> ");
        if (parts.length < 2) {
            return fullOutput;
        }

        // Get the part corresponding to the last command (before "exit")
        int lastCommandIndex = parts.length - 2;
        if (lastCommandIndex < 0) {
            return fullOutput;
        }

        String lastPart = parts[lastCommandIndex];
        // Remove the command itself (first line)
        int newlineIndex = lastPart.indexOf('\n');
        if (newlineIndex >= 0 && newlineIndex < lastPart.length() - 1) {
            return lastPart.substring(newlineIndex + 1);
        }
        return "";
    }

    private static class InteractiveSession {
        final String sessionId;
        private final InteractiveMode.InteractiveReader inputReader;
        private final CapturingPrintStream outputStream;
        private final InteractiveMode interactiveMode;
        private final Thread workerThread;
        private volatile Throwable error = null;
        private boolean shutdown;

        InteractiveSession(String sessionId, GameState originalState) {
            this.sessionId = sessionId;
            this.outputStream = new CapturingPrintStream();
            this.inputReader = new InteractiveMode.InteractiveReader(new InputStreamReader(InputStream.nullInputStream()));
            this.inputReader.setReadFromQueue();
            this.interactiveMode = new InteractiveMode(outputStream, inputReader);
            this.interactiveMode.setDefaultNumberOfThreads(1);
            this.interactiveMode.setDefaultBatchSize(1);
            this.workerThread = new Thread(() -> {
                try {
                    interactiveMode.interactiveStart(originalState.clone(false), null, null);
                } catch (Exception e) {
                    if (!inputReader.isShutdown()) {
                        error = e;
                    }
                }
            });
            workerThread.setDaemon(true);
            workerThread.start();
        }

        public String executeCommands(List<String> commands) throws IOException, InterruptedException {
            if (error != null) {
                throw new IOException("Worker thread error: " + error.getMessage(), error);
            }
            for (String command : commands) {
                // Wait for worker to be idle before clearing output and sending command
                // This ensures any previous output (including initial state) is discarded
                while (!shutdown) {
                    boolean idle = inputReader.waitUntilIdle(1000);
                    if (idle) {
                        break;
                    }
                    if (error != null) {
                        throw new IOException("Worker thread error: " + error.getMessage(), error);
                    }
                    if (!workerThread.isAlive()) {
                        throw new IOException("Session ended (worker thread terminated)");
                    }
                }
                outputStream.clear();
                inputReader.addCommandsToInputQueue(List.of(command));
                while (!shutdown) {
                    boolean idle = inputReader.waitUntilIdle(1000);
                    if (idle) {
                        break;
                    }
                    if (error != null) {
                        throw new IOException("Worker thread error: " + error.getMessage(), error);
                    }
                    if (!workerThread.isAlive()) {
                        throw new IOException("Session ended (worker thread terminated)");
                    }
                }
            }
            return outputStream.getAndClear();
        }
    }

    /**
     * Tests the interactive server API by connecting to a running server and executing commands.
     *
     * <h2>Usage</h2>
     * <pre>
     * java -cp &lt;classpath&gt; com.alphaStS.Main --interactive-server-test [--port &lt;port&gt;]
     * </pre>
     *
     * <h2>Test Cases</h2>
     * <ol>
     *   <li>Creates a new session and executes 'i' command to get game state info</li>
     *   <li>Continues the session with 'a' command to view deck</li>
     *   <li>Verifies session continuity by checking command history works</li>
     * </ol>
     */
    public static void test(String[] args) throws IOException {
        // Parse port from args (--port <port>), default to 7999
        int port = 7999;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--port") && i + 1 < args.length) {
                port = Integer.parseInt(args[i + 1]);
                break;
            }
        }

        String baseUrl = "http://localhost:" + port;
        ObjectMapper mapper = new ObjectMapper();
        HttpClient client = HttpClient.newHttpClient();

        System.out.println("Testing interactive server at " + baseUrl);
        System.out.println("=".repeat(60));

        try {
            // Test 1: Create new session with 'i' command (show game state info)
            System.out.println("\nTest 1: Create new session and execute 'i' command");
            String requestBody1 = "{\"commands\": [\"i\"]}";
            HttpRequest request1 = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/execute"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody1))
                    .build();

            HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
            JsonNode responseNode1 = mapper.readTree(response1.body());

            System.out.println("Status: " + response1.statusCode());
            System.out.println("Response: " + response1.body());

            if (!responseNode1.get("success").asBoolean()) {
                System.out.println("FAILED: Expected success=true");
                System.exit(1);
            }

            String sessionId = responseNode1.get("sessionId").asText();
            System.out.println("Session ID: " + sessionId);
            System.out.println("PASSED");

            // Test 2: Continue session with 'a' command (show deck)
            System.out.println("\nTest 2: Continue session and execute 'a' command (view deck)");
            ObjectNode requestNode2 = mapper.createObjectNode();
            requestNode2.putArray("commands").add("a");
            requestNode2.put("sessionId", sessionId);

            HttpRequest request2 = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/execute"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestNode2)))
                    .build();

            HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
            JsonNode responseNode2 = mapper.readTree(response2.body());

            System.out.println("Status: " + response2.statusCode());
            System.out.println("Response: " + response2.body());

            if (!responseNode2.get("success").asBoolean()) {
                System.out.println("FAILED: Expected success=true");
                System.exit(1);
            }

            String output2 = responseNode2.get("output").asText();
            if (!output2.contains("Deck")) {
                System.out.println("FAILED: Expected output to contain 'Deck'");
                System.exit(1);
            }
            System.out.println("PASSED");

            // Test 3: Verify same session ID is returned
            System.out.println("\nTest 3: Verify session continuity");
            String returnedSessionId = responseNode2.get("sessionId").asText();
            if (!sessionId.equals(returnedSessionId)) {
                System.out.println("FAILED: Session ID mismatch");
                System.exit(1);
            }
            System.out.println("PASSED");

            // Test 4: Test invalid request (missing commands)
            System.out.println("\nTest 4: Test error handling (missing commands)");
            String requestBody4 = "{\"sessionId\": \"" + sessionId + "\"}";
            HttpRequest request4 = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/execute"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody4))
                    .build();

            HttpResponse<String> response4 = client.send(request4, HttpResponse.BodyHandlers.ofString());
            JsonNode responseNode4 = mapper.readTree(response4.body());

            System.out.println("Status: " + response4.statusCode());
            System.out.println("Response: " + response4.body());

            if (responseNode4.get("success").asBoolean()) {
                System.out.println("FAILED: Expected success=false for missing commands");
                System.exit(1);
            }
            System.out.println("PASSED");

            System.out.println("\n" + "=".repeat(60));
            System.out.println("All tests PASSED!");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Test interrupted", e);
        } catch (Exception e) {
            System.out.println("Test FAILED with exception: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * A PrintStream that captures all output to a buffer.
     */
    public static class CapturingPrintStream extends PrintStream {
        private final ByteArrayOutputStream buffer;

        public CapturingPrintStream() {
            super(new ByteArrayOutputStream(), true, java.nio.charset.StandardCharsets.UTF_8);
            this.buffer = (ByteArrayOutputStream) out;
        }

        public String getAndClear() {
            flush();
            String output = buffer.toString(java.nio.charset.StandardCharsets.UTF_8);
            buffer.reset();
            return output;
        }

        public void clear() {
            buffer.reset();
        }
    }
}
