package com.alphaStS;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

public class GameStateUtils {
    public static String formatFloat(double f) {
        if (f == 0) {
            return "0";
        } else if (f < 0.001) {
            return String.format("%6.3e", f).trim();
        } else {
            return String.format("%6.3f", f).trim();
        }
    }

    private static class CardChanges {
        int discard;
        int deck;
        int hand;
        int exhaust;
    }

    public static String diffGameState(GameState from, GameState to) {
        StringBuilder desc = new StringBuilder();
        boolean first = true, first2;
        var cardMap = new HashMap<Card, CardChanges>();
        for (Card card : from.prop.cardDict) {
            cardMap.put(card, new CardChanges());
        }
        for (int i = 0; i < from.hand.length; i++) {
            if (from.hand[i] != to.hand[i]) {
                cardMap.get(from.prop.cardDict[i]).hand = to.hand[i] - from.hand[i];
            }
        }
        for (int i = 0; i < from.deck.length; i++) {
            if (from.deck[i] != to.deck[i]) {
                cardMap.get(from.prop.cardDict[i]).deck = to.deck[i] - from.deck[i];
            }
        }
        for (int i = 0; i < from.discard.length; i++) {
            if (from.discard[i] != to.discard[i]) {
                cardMap.get(from.prop.cardDict[i]).discard = to.discard[i] - from.discard[i];
            }
        }
        for (int i = 0; i < from.exhaust.length; i++) {
            if (from.exhaust[i] != to.exhaust[i]) {
                cardMap.get(from.prop.cardDict[i]).exhaust = to.exhaust[i] - from.exhaust[i];
            }
        }
        first2 = true;
        for (var entry : cardMap.entrySet()) {
            var v = entry.getValue();
            int drawn = 0;
            if (v.deck < 0) {
                drawn = -v.deck;
            }
            if (drawn > 0) {
                desc.append(first2 ? "Draw " : ", ").append(drawn == 1 ? "" : drawn + " ").append(entry.getKey().cardName);
                first2 = false;
            }
        }
        first2 = true;
        for (var entry : cardMap.entrySet()) {
            var v = entry.getValue();
            if (v.deck == 0 && v.hand > 0) {
                if (first) {
                    desc.append(", ");
                    first = false;
                }
                desc.append(first2 ? "+" : ", +").append(v.hand).append(' ').append(entry.getKey().cardName);
                first2 = false;
            }
        }
        first2 = true;
        for (var entry : cardMap.entrySet()) {
            var v = entry.getValue();
            if (v.hand < 0 && v.discard <= -v.hand) {
                if (first) {
                    desc.append(", ");
                    first = false;
                }
                desc.append(first2 ? "Discard " : ", ").append(v.hand == -1 ? "" : -v.hand + " ").append(entry.getKey().cardName);
                first2 = false;
            }
        }
        return desc.toString();
    }

    static private void printTreeH(State s, int depth, Writer writer, String indent) throws IOException {
        if (depth == 0) {
            return;
        }
        if (s instanceof ChanceState state) {
            for (ChanceState.Node node : state.cache.values()) {
                writer.write(indent + "Chance Node (" + node.n + "/" + state.total_n + "): " + node.state.toStringReadable() + "\n");
                for (int i = 0; i < node.state.prop.maxNumOfActions; i++) {
                    if (node.state.ns[i] != null) {
                        if (depth > 1) {
                            writer.write(indent + "  - action=" + node.state.getActionString(i) + "(" + i + ")\n");
                        }
                    }
                    printTreeH(node.state.ns[i], depth - 1, writer, indent + "    ");
                }
            }
        } else if (s instanceof GameState state) {
            writer.write(indent + "Normal Node: " + state.toStringReadable() + "\n");
            for (int i = 0; i < state.prop.maxNumOfActions; i++) {
                if (state.ns[i] != null) {
                    if (depth > 1) {
                        writer.write(indent + "  - action=" + state.getActionString(i) + "(" + i + ")\n");
                    }
                }
                printTreeH(state.ns[i], depth - 1, writer, indent + "    ");
            }
        }
    }

    static void printTree(State state, Writer writer, int depth) {
        try {
            if (writer == null) {
                writer = new OutputStreamWriter(System.out);
            }
            printTreeH(state, depth, writer, "");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static private void printTreeH2(GameState parentState, int parentAction, State s, int depth, Writer writer, String indent) throws IOException {
        if (depth < 0) {
            return;
        }
        if (s instanceof ChanceState cState) {
            writer.write(indent + parentState.toStringReadable() + "\n");
            for (ChanceState.Node node : cState.cache.values().stream().sorted((a, b) -> Long.compare(b.n, a.n)).toList()) {
                writer.write(indent + "Chance Node (" + node.n + "/" + cState.total_n + "): " + diffGameState(parentState, node.state) + "\n");
                var state = node.state;
                var n = node.state.total_n + 1;
                var q_comb = node.state.total_q_comb;
                var q_win = node.state.total_q_win;
                var q_health = node.state.total_q_health;
                writer.write(indent + "n=" + n + ", q=" + formatFloat(q_comb / n) + ", q_win=" + formatFloat(q_win / n) + ", q_health=" + formatFloat(q_health / n) + " (" + formatFloat(q_health / n * state.player.maxHealth) + ") v=(" + formatFloat(state.v_win) + "/" + formatFloat(state.v_health) + "(" + formatFloat(state.v_health * state.player.maxHealth) + "))\n");
                printTreeH2(parentState, parentAction, node.state, depth, writer, indent);
            }
        } else if (s instanceof GameState state) {
            var list = new ArrayList<int[]>();
            for (int i = 0; i < state.prop.maxNumOfActions; i++) {
                list.add(new int[] { state.n[i], i });
            }
            list.sort((a, b) -> b[0] != a[0] ? Integer.compare(b[0], a[0]) : Integer.compare(a[1], b[1]));
            for (var x : list) {
                var i = x[1];
                if (state.ns[i] != null && depth > 0) {
                    writer.write(indent + "  - action=" + state.getActionString(i) + " (" + i + ")");
                    var n = state.n[i];
                    var q_comb = state.q_comb[i];
                    var q_win = state.q_win[i];
                    var q_health = state.q_health[i];
                    int l = (state.getActionString(i) + " (" + i + ")").length();
                    for (int j = 0; j < 24 - l; j++) {
                        writer.write(' ');
                    }
                    writer.write("n=" + n + " p=" + formatFloat(state.policy[i]) + ", q=" + formatFloat(q_comb / n) + ", q_win=" + formatFloat(q_win / n) + ", q_health=" + formatFloat(q_health / n) + " (" + formatFloat(q_health / n * state.player.maxHealth) + ") v=(" + formatFloat(state.v_win) + "/" + formatFloat(state.v_health) + "(" + formatFloat(state.v_health * state.player.maxHealth) + "))\n");
                }
                printTreeH2(state, i, state.ns[i], depth - 1, writer, indent + "    ");
            }
        }
    }

    static void printTree2(State state, Writer writer, int depth) {
        try {
            if (writer == null) {
                writer = new OutputStreamWriter(System.out);
            }
            printTreeH2(null, -1, state, depth, writer, "");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
