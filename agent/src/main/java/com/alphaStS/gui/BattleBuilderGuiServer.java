package com.alphaStS.gui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executors;

/**
 * HTTP server that serves a web GUI for building battle definitions.
 * Listens on {@code localhost:4000} by default.
 *
 * <h2>Usage</h2>
 * <pre>
 * java -cp &lt;classpath&gt; com.alphaStS.Main --gui-server [--port &lt;port&gt;]
 * </pre>
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li>GET  /                 — serve the GUI HTML page</li>
 *   <li>GET  /images/*              — serve card/relic/potion images from classpath</li>
 *   <li>GET  /api/cards             — return card list JSON (query param: character)</li>
 *   <li>GET  /api/relics            — return relic list JSON</li>
 *   <li>GET  /api/potions           — return potion list JSON</li>
 *   <li>GET  /api/images/cache      — return all images as base64 data-URIs (query param: character)</li>
 *   <li>POST /api/validate          — validate a battle definition JSON body</li>
 * </ul>
 */
public class BattleBuilderGuiServer {

    private static final int DEFAULT_PORT = 4000;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ----------------------------- Entry Point ----------------------------

    /**
     * Starts the GUI server. Blocks until the JVM exits.
     *
     * @param args command-line arguments forwarded from Main
     */
    public static void start(String[] args) throws IOException {
        int port = DEFAULT_PORT;

        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals("--port")) {
                port = Integer.parseInt(args[i + 1]);
            }
        }

        JsonNode cardsJson   = loadJsonFromClasspath("/sts_cards.json");
        JsonNode relicsJson  = loadJsonFromClasspath("/sts_relics.json");
        JsonNode potionsJson = loadJsonFromClasspath("/sts_potions.json");

        System.out.println("[BattleGuiServer] cards=" + (cardsJson.isArray() ? cardsJson.size() : 0)
                + " relics=" + (relicsJson.isArray() ? relicsJson.size() : 0)
                + " potions=" + (potionsJson.isArray() ? potionsJson.size() : 0));

        // Verify GUI resource is accessible
        try (InputStream test = BattleBuilderGuiServer.class.getResourceAsStream("/gui/index.html")) {
            if (test == null) {
                System.err.println("[BattleGuiServer] WARNING: /gui/index.html not found on classpath!");
            } else {
                System.out.println("[BattleGuiServer] GUI resource found on classpath (" + test.available() + " bytes)");
            }
        } catch (IOException e) {
            System.err.println("[BattleGuiServer] Error probing GUI resource: " + e.getMessage());
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/cards",        exchange -> safeHandle("api/cards",        exchange, ex -> handleApiCards(ex, cardsJson)));
        server.createContext("/api/relics",       exchange -> safeHandle("api/relics",       exchange, ex -> handleApiRelics(ex, relicsJson)));
        server.createContext("/api/potions",      exchange -> safeHandle("api/potions",      exchange, ex -> handleApiPotions(ex, potionsJson)));
        server.createContext("/api/images/cache", exchange -> safeHandle("api/images/cache", exchange, ex -> handleApiImagesCache(ex, cardsJson, relicsJson, potionsJson)));
        server.createContext("/api/validate",     exchange -> safeHandle("api/validate",     exchange, BattleBuilderGuiServer::handleApiValidate));
        server.createContext("/images/",          exchange -> safeHandle("images",           exchange, BattleBuilderGuiServer::handleImages));
        server.createContext("/",                 exchange -> safeHandle("root",             exchange, BattleBuilderGuiServer::handleRoot));

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("[BattleGuiServer] Started at http://localhost:" + port + "/");
        System.out.flush();
    }

    // ----------------------------- Safe Wrapper ---------------------------

    @FunctionalInterface
    private interface Handler {
        void handle(HttpExchange exchange) throws IOException;
    }

    private static void safeHandle(String name, HttpExchange exchange, Handler handler) {
        String method = exchange.getRequestMethod();
        String path   = exchange.getRequestURI().toString();
        System.out.println("[BattleGuiServer] " + method + " " + path + " -> " + name);
        System.out.flush();
        try {
            handler.handle(exchange);
        } catch (Exception e) {
            System.err.println("[BattleGuiServer] ERROR in " + name + ": " + e);
            e.printStackTrace(System.err);
            System.err.flush();
            try {
                byte[] body = ("Internal server error: " + e.getMessage()).getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(500, body.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body);
                }
            } catch (Exception ignored) {}
            try { exchange.close(); } catch (Exception ignored) {}
        }
    }

    // ----------------------------- Request Handlers -----------------------

    private static void handleRoot(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "text/plain", "Method Not Allowed".getBytes(StandardCharsets.UTF_8));
            return;
        }
        InputStream is = BattleBuilderGuiServer.class.getResourceAsStream("/gui/index.html");
        if (is == null) {
            System.err.println("[BattleGuiServer] /gui/index.html not found on classpath");
            sendResponse(exchange, 404, "text/plain", "GUI not found".getBytes(StandardCharsets.UTF_8));
            return;
        }
        byte[] body;
        try (is) {
            body = is.readAllBytes();
        }
        System.out.println("[BattleGuiServer] Serving index.html (" + body.length + " bytes)");
        addCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
        exchange.close();
    }

    private static void handleImages(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String resourcePath = URLDecoder.decode(path.replaceFirst("^/images/", ""), StandardCharsets.UTF_8);

        InputStream is = BattleBuilderGuiServer.class.getResourceAsStream("/" + resourcePath);
        if (is == null) {
            System.err.println("[BattleGuiServer] Image not found on classpath: " + resourcePath);
            sendResponse(exchange, 404, "text/plain", ("Image not found: " + resourcePath).getBytes(StandardCharsets.UTF_8));
            return;
        }
        byte[] body;
        try (is) {
            body = is.readAllBytes();
        }
        String mime = guessMimeType(resourcePath);
        addCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", mime);
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
        exchange.close();
    }

    private static void handleApiCards(HttpExchange exchange, JsonNode cardsJson) throws IOException {
        if (handlePreflight(exchange)) return;
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "text/plain", "Method Not Allowed".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        String characterFilter = null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2 && kv[0].equalsIgnoreCase("character")) {
                    characterFilter = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                }
            }
        }

        ArrayNode result = MAPPER.createArrayNode();
        if (cardsJson.isArray()) {
            for (JsonNode card : cardsJson) {
                if (characterFilter == null) {
                    result.add(card);
                } else {
                    String cardChar = card.has("character") ? card.get("character").asText() : "";
                    if (cardChar.equalsIgnoreCase(characterFilter) || cardChar.equalsIgnoreCase("Colorless")) {
                        result.add(card);
                    }
                }
            }
        }

        sendJson(exchange, 200, MAPPER.writeValueAsString(result));
    }

    private static void handleApiRelics(HttpExchange exchange, JsonNode relicsJson) throws IOException {
        if (handlePreflight(exchange)) return;
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "text/plain", "Method Not Allowed".getBytes(StandardCharsets.UTF_8));
            return;
        }
        sendJson(exchange, 200, MAPPER.writeValueAsString(relicsJson));
    }

    private static void handleApiPotions(HttpExchange exchange, JsonNode potionsJson) throws IOException {
        if (handlePreflight(exchange)) return;
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "text/plain", "Method Not Allowed".getBytes(StandardCharsets.UTF_8));
            return;
        }
        sendJson(exchange, 200, MAPPER.writeValueAsString(potionsJson));
    }

    private static void handleApiImagesCache(HttpExchange exchange,
            JsonNode cardsJson, JsonNode relicsJson, JsonNode potionsJson) throws IOException {
        if (handlePreflight(exchange)) return;
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "text/plain", "Method Not Allowed".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        String characterFilter = null;
        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2 && kv[0].equalsIgnoreCase("character")) {
                    characterFilter = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                }
            }
        }

        Set<String> imagePaths = new LinkedHashSet<>();

        if (cardsJson.isArray()) {
            for (JsonNode card : cardsJson) {
                String cardChar = card.has("character") ? card.get("character").asText() : "";
                boolean include = characterFilter == null
                        || cardChar.equalsIgnoreCase(characterFilter)
                        || cardChar.equalsIgnoreCase("Colorless");
                if (include) {
                    addImagePath(imagePaths, card, "image_path");
                    addImagePath(imagePaths, card, "upgraded_image_path");
                }
            }
        }

        if (relicsJson.isArray()) {
            for (JsonNode relic : relicsJson) {
                addImagePath(imagePaths, relic, "image_path");
            }
        }

        if (potionsJson.isArray()) {
            for (JsonNode potion : potionsJson) {
                addImagePath(imagePaths, potion, "image_path");
            }
        }

        ObjectNode result = MAPPER.createObjectNode();
        for (String path : imagePaths) {
            InputStream is = BattleBuilderGuiServer.class.getResourceAsStream("/" + path);
            if (is == null) continue;
            try (is) {
                byte[] data = is.readAllBytes();
                String mime = guessMimeType(path);
                result.put(path, "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(data));
            }
        }

        sendJson(exchange, 200, MAPPER.writeValueAsString(result));
    }

    private static void addImagePath(Set<String> paths, JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull() && !node.get(field).asText().isEmpty()) {
            paths.add(node.get(field).asText());
        }
    }

    private static void handleApiValidate(HttpExchange exchange) throws IOException {
        if (handlePreflight(exchange)) return;
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "text/plain", "Method Not Allowed".getBytes(StandardCharsets.UTF_8));
            return;
        }

        String body;
        try (InputStream is = exchange.getRequestBody()) {
            body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        BattleBuilderJsonReader.ValidationResult result = BattleBuilderJsonReader.validate(body);

        ObjectNode response = MAPPER.createObjectNode();
        response.put("valid", result.valid());
        if (result.error() != null) {
            response.put("error", result.error());
        }
        if (!result.ignoredItems().isEmpty()) {
            ArrayNode arr = response.putArray("ignoredItems");
            for (String item : result.ignoredItems()) {
                arr.add(item);
            }
        }

        sendJson(exchange, 200, MAPPER.writeValueAsString(response));
    }

    // ----------------------------- Utilities ------------------------------

    /**
     * Handles an OPTIONS preflight request. Returns true if the request was handled.
     */
    private static boolean handlePreflight(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            addCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendResponse(HttpExchange exchange, int status, String contentType, byte[] body) throws IOException {
        addCorsHeaders(exchange);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
        exchange.close();
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        sendResponse(exchange, status, "application/json; charset=utf-8", json.getBytes(StandardCharsets.UTF_8));
    }

    private static String guessMimeType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }

    private static JsonNode loadJsonFromClasspath(String resourcePath) {
        try (InputStream is = BattleBuilderGuiServer.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("[BattleGuiServer] Warning: classpath resource not found: "
                        + resourcePath + " — using empty array");
                return MAPPER.createArrayNode();
            }
            return MAPPER.readTree(is);
        } catch (IOException e) {
            System.err.println("[BattleGuiServer] Warning: Failed to read classpath resource "
                    + resourcePath + ": " + e.getMessage() + " — using empty array");
            return MAPPER.createArrayNode();
        }
    }
}
