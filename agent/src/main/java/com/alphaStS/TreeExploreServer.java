package com.alphaStS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Embedded HTTP server that renders the MCTS search tree as an interactive DAG in the browser.
 * Uses Cytoscape.js + cytoscape-dagre (via CDN) served from tree_explorer.html resource.
 */
public class TreeExploreServer {

    private static final int PORT = 7777;
    private static final int DEFAULT_DEPTH = 3;
    private static final int DEFAULT_MAX_CHANCE_CHILDREN = 5;

    private final GameState root;
    private final ObjectMapper mapper = new ObjectMapper();

    // node_id -> State (GameState or ChanceState)
    private final ConcurrentHashMap<Integer, State> idToState = new ConcurrentHashMap<>();
    // identity hash -> node_id (avoids calling equals on game states)
    private final ConcurrentHashMap<Integer, Integer> identityToId = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(0);

    private HttpServer server;

    public TreeExploreServer(GameState root) {
        this.root = root;
        register(root);
    }

    private int register(State state) {
        int identity = System.identityHashCode(state);
        return identityToId.computeIfAbsent(identity, k -> {
            int id = nextId.getAndIncrement();
            idToState.put(id, state);
            return id;
        });
    }

    public int start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);

        server.createContext("/", exchange -> {
            if (!exchange.getRequestURI().getPath().equals("/")) {
                send404(exchange);
                return;
            }
            byte[] body = loadHtmlResource();
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        server.createContext("/api/tree", this::handleTreeApi);

        server.createContext("/api/root", exchange -> {
            ObjectNode resp = mapper.createObjectNode();
            resp.put("root_id", 0);
            sendJson(exchange, 200, mapper.writeValueAsString(resp));
        });

        server.setExecutor(null);
        server.start();
        return PORT;
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private byte[] loadHtmlResource() throws IOException {
        try (InputStream is = TreeExploreServer.class.getResourceAsStream("/tree_explorer.html")) {
            if (is == null) {
                return "<h1>tree_explorer.html resource not found</h1>".getBytes(StandardCharsets.UTF_8);
            }
            return is.readAllBytes();
        }
    }

    private void handleTreeApi(HttpExchange exchange) throws IOException {
        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());

        String nodeIdStr = params.get("node_id");
        int nodeId = (nodeIdStr == null || nodeIdStr.equals("null")) ? 0 : parseInt(nodeIdStr, 0);
        int depth = parseInt(params.get("depth"), DEFAULT_DEPTH);
        int maxChanceChildren = parseInt(params.get("max_chance_children"), DEFAULT_MAX_CHANCE_CHILDREN);

        State rootState = idToState.get(nodeId);
        if (rootState == null) {
            sendJson(exchange, 404, "{\"error\":\"node_id not found\"}");
            return;
        }

        ObjectNode resp = mapper.createObjectNode();
        resp.put("root_id", nodeId);
        ArrayNode nodesArr = mapper.createArrayNode();
        ArrayNode edgesArr = mapper.createArrayNode();

        buildDag(rootState, nodeId, depth, maxChanceChildren, true, nodesArr, edgesArr, new HashSet<>());

        Set<Integer> reachable = reachableIds(nodeId, edgesArr);
        resp.set("nodes", filterNodes(reachable, nodesArr));
        resp.set("edges", filterEdges(reachable, edgesArr));
        sendJson(exchange, 200, mapper.writeValueAsString(resp));
    }

    private void buildDag(State state, int stateId, int depthLeft, int maxChanceChildren, boolean isRoot,
                          ArrayNode nodesArr, ArrayNode edgesArr, Set<Integer> visited) {
        if (!visited.add(stateId)) {
            return;
        }

        if (state instanceof GameState gs) {
            ObjectNode node = mapper.createObjectNode();
            node.put("id", stateId);
            node.put("type", "game");
            node.put("label", buildGameStateLabel(gs));
            node.put("tooltip", gs.toString());
            node.put("visits", gs.total_n + 1);
            node.put("terminal", gs.isTerminal() != 0);
            nodesArr.add(node);

            if (depthLeft <= 0 || gs.ns == null) {
                return;
            }

            int[] legalActions = gs.getLegalActions();
            for (int i = 0; i < legalActions.length; i++) {
                if (gs.ns[i] == null) {
                    continue;
                }
                State child = gs.ns[i];
                int childId = register(child);

                ObjectNode edge = mapper.createObjectNode();
                edge.put("source", stateId);
                edge.put("target", childId);
                edge.put("label", buildEdgeLabel(gs, i));
                edgesArr.add(edge);

                buildDag(child, childId, depthLeft - 1, maxChanceChildren, false, nodesArr, edgesArr, visited);
            }
        } else if (state instanceof ChanceState cs) {
            ObjectNode node = mapper.createObjectNode();
            node.put("id", stateId);
            node.put("type", "chance");
            node.put("label", buildChanceStateLabel(cs));
            node.put("tooltip", cs.toString());
            node.put("visits", cs.total_n);
            node.put("terminal", false);
            nodesArr.add(node);

            if (depthLeft <= 0) {
                return;
            }

            var outcomes = cs.cache.values().stream()
                    .sorted(Comparator.comparingLong((ChanceState.Node n) -> n.n).reversed())
                    .toList();

            if (!isRoot && outcomes.size() > maxChanceChildren) {
                return;
            }

            for (ChanceState.Node outcome : outcomes) {
                GameState child = outcome.state;
                int childId = register(child);

                String desc = child.getStateDesc().length() > 0
                        ? child.getStateDesc().toString()
                        : child.toString();
                String edgeLabel = truncate(desc, 40) + "\n(" + outcome.n + "/" + cs.total_n + ")";

                ObjectNode edge = mapper.createObjectNode();
                edge.put("source", stateId);
                edge.put("target", childId);
                edge.put("label", edgeLabel);
                edgesArr.add(edge);

                buildDag(child, childId, depthLeft - 1, maxChanceChildren, false, nodesArr, edgesArr, visited);
            }
        }
    }

    private String buildGameStateLabel(GameState gs) {
        int realVisits = gs.total_n + 1;
        double qComb = gs.getTotalQ(GameState.V_COMB_IDX) / realVisits;
        double qWin = gs.getTotalQ(GameState.V_WIN_IDX) / realVisits;
        double qHealth = gs.getTotalQ(GameState.V_HEALTH_IDX) / realVisits;
        return String.format("visits=%d\nq_comb=%.3f\nq_win=%.3f\nq_health=%.3f", realVisits, qComb, qWin, qHealth);
    }

    private String buildChanceStateLabel(ChanceState cs) {
        String desc = cs.parentState.getStateDesc().toString().trim();
        if (!desc.isEmpty()) {
            return truncate(desc.lines().findFirst().orElse(""), 50);
        }
        return "Unknown";
    }

    private String buildEdgeLabel(GameState parent, int actionIdx) {
        String actionName = parent.getActionString(actionIdx);
        double visitPct = parent.total_n > 0
                ? parent.n[actionIdx] * 100.0 / parent.total_n
                : 0.0;
        double childQ = parent.n[actionIdx] > 0
                ? parent.getChildQ(actionIdx, GameState.V_WIN_IDX) / parent.n[actionIdx]
                : 0.0;
        return String.format("%s\n(%.1f%%, Q=%.2f)", actionName, visitPct, childQ);
    }

    private Set<Integer> reachableIds(int rootId, ArrayNode edgesArr) {
        Map<Integer, List<Integer>> adj = new HashMap<>();
        for (var e : edgesArr) {
            adj.computeIfAbsent(e.get("source").asInt(), k -> new ArrayList<>()).add(e.get("target").asInt());
        }
        Set<Integer> reachable = new HashSet<>();
        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(rootId);
        while (!queue.isEmpty()) {
            int cur = queue.poll();
            if (reachable.add(cur)) {
                adj.getOrDefault(cur, List.of()).forEach(queue::add);
            }
        }
        return reachable;
    }

    private ArrayNode filterNodes(Set<Integer> reachable, ArrayNode nodesArr) {
        ArrayNode filtered = mapper.createArrayNode();
        for (var n : nodesArr) {
            if (reachable.contains(n.get("id").asInt())) {
                filtered.add(n);
            }
        }
        return filtered;
    }

    private ArrayNode filterEdges(Set<Integer> reachable, ArrayNode edgesArr) {
        ArrayNode filtered = mapper.createArrayNode();
        for (var e : edgesArr) {
            if (reachable.contains(e.get("source").asInt()) && reachable.contains(e.get("target").asInt())) {
                filtered.add(e);
            }
        }
        return filtered;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        s = s.replace('\n', ' ').trim();
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                params.put(pair.substring(0, eq), pair.substring(eq + 1));
            }
        }
        return params;
    }

    private static int parseInt(String s, int defaultVal) {
        if (s == null) return defaultVal;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private static void send404(HttpExchange exchange) throws IOException {
        byte[] body = "Not found".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(404, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }
}
