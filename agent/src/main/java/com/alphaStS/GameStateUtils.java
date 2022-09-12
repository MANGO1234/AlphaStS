package com.alphaStS;

import com.alphaStS.utils.Utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GameStateUtils {

    public static int getRandomEnemyIdx(GameState state, RandomGenCtx ctx) {
        int enemyIdx;
        if (state.enemiesAlive == 0) {
            return -1;
        } else if (state.enemiesAlive == 1) {
            enemyIdx = 0;
        } else {
            enemyIdx = state.getSearchRandomGen().nextInt(state.enemiesAlive, ctx, state);
            state.isStochastic = true;
        }
        for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
            if (state.getEnemiesForRead().get(i).isAlive()) {
                enemyIdx--;
                if (enemyIdx < 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static List<List<GameStep>> groupByTurns(List<GameStep> steps) {
        List<List<GameStep>> turns = new ArrayList<>();
        List<GameStep> current = null;
        int lastTurn = -1;
        for (int i = 0; i < steps.size(); i++) {
            if (lastTurn != steps.get(i).state().turnNum) {
                if (current != null) {
                    turns.add(current);
                }
                current = new ArrayList<>();
            }
            lastTurn = steps.get(i).state().turnNum;
            current.add(steps.get(i));
        }
        turns.add(current);
        return turns;
    }

    public static int[][] getScenarioGroups(GameState state, int groupSize, int stride) {
        int size = state.prop.randomization.listRandomizations().size();
        if (size % (groupSize * stride) != 0) {
            return null;
        }
        int[][] groups = new int[size / groupSize][groupSize];
        int offset = 0;
        int idx = 0;
        while (offset < size) {
            for (int i = 0; i < stride; i++) {
                for (int j = 0; j < groupSize; j++) {
                    groups[idx][j] = offset + i + j * stride;
                }
                idx++;
            }
            offset += stride * groupSize;
        }
        return groups;
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
        for (int i = 0; i < from.getExhaustForRead().length; i++) {
            if (from.getExhaustForRead()[i] != to.getExhaustForRead()[i]) {
                cardMap.get(from.prop.cardDict[i]).exhaust = to.getExhaustForRead()[i] - from.getExhaustForRead()[i];
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
                writer.write(indent + "Chance Node (" + node.n + "/" + state.total_node_n + "): " + node.state.toString() + "\n");
                if (node.state.n == null) {
                    return;
                }
                var list = new ArrayList<int[]>();
                for (int i = 0; i < node.state.n.length; i++) {
                    list.add(new int[] { node.state.n[i], i });
                }
                list.sort((a, b) -> b[0] != a[0] ? Integer.compare(b[0], a[0]) : Integer.compare(a[1], b[1]));
                for (var x : list) {
                    int i = x[1];
                    if (node.state.ns[i] != null && depth > 1) {
                        writer.write(indent + "  - action=" + node.state.getActionString(i) + "(" + i + ")\n");
                    }
                    printTreeH(node.state.ns[i], depth - 1, writer, indent + "    ");
                }
            }
        } else if (s instanceof GameState state) {
            writer.write(indent + "Normal Node: " + state.toString() + "\n");
            if (state.n == null) {
                return;
            }
            var list = new ArrayList<int[]>();
            for (int i = 0; i < state.n.length; i++) {
                list.add(new int[] { state.n[i], i });
            }
            list.sort((a, b) -> b[0] != a[0] ? Integer.compare(b[0], a[0]) : Integer.compare(a[1], b[1]));
            for (var x : list) {
                int i = x[1];
                if (state.ns[i] != null && depth > 1) {
                    writer.write(indent + "  - action=" + state.getActionString(i) + "(" + i + ")\n");
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
            writer.write(indent + parentState.toString() + "\n");
            for (ChanceState.Node node : cState.cache.values().stream().sorted((a, b) -> Long.compare(b.n, a.n)).toList()) {
                writer.write(indent + "Chance Node (" + node.n + "/" + cState.total_node_n + "): " + diffGameState(parentState, node.state) + "\n");
                var state = node.state;
                var n = node.state.total_n + 1;
                var q_comb = node.state.total_q_comb;
                var q_win = node.state.total_q_win;
                var q_health = node.state.total_q_health;
                writer.write(indent + "n=" + n + ", q=" + Utils.formatFloat(q_comb / n) + ", q_win=" + Utils.formatFloat(q_win / n) + ", q_health=" + Utils.formatFloat(q_health / n) + " (" + Utils.formatFloat(q_health / n * state.getPlayeForRead().getMaxHealth()) + ") v=(" + Utils.formatFloat(state.v_win) + "/" + Utils.formatFloat(state.v_health) + "(" + Utils.formatFloat(state.v_health * state.getPlayeForRead().getMaxHealth()) + "))\n");
                printTreeH2(parentState, parentAction, node.state, depth, writer, indent);
            }
        } else if (s instanceof GameState state) {
            var list = new ArrayList<int[]>();
            for (int i = 0; i < state.n.length; i++) {
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
                    writer.write("n=" + n + " p=" + Utils.formatFloat(state.policy[i]) + ", q=" + Utils.formatFloat(q_comb / n) + ", q_win=" + Utils.formatFloat(q_win / n) + ", q_health=" + Utils.formatFloat(q_health / n) + " (" + Utils.formatFloat(q_health / n * state.getPlayeForRead().getMaxHealth()) + ") v=(" + Utils.formatFloat(state.v_win) + "/" + Utils.formatFloat(state.v_health) + "(" + Utils.formatFloat(state.v_health * state.getPlayeForRead().getMaxHealth()) + "))\n");
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

    static private String lineBreak(String s, int length) {
        var b = new StringBuilder();
        var token = s.split("[\s/]");
        var split = s.split("[^\s/]+");
        var l = 0;
        for (int i = 0; i < token.length; i++) {
            int to_write = token[i].length();
            if (i < token.length - 1) {
                if (split[i + 1].equals("/")) {
                    to_write += 1;
                } else if (l > 0 && split[i].equals(" ")) {
                    to_write += 1;
                }
            }
            if (l + to_write > length) {
                b.append("\\n");
                l = 0;
            }
            if (i < token.length - 1) {
                if (l > 0 && split[i].equals(" ")) {
                    b.append(" ");
                }
            }
            b.append(token[i]);
            if (i < token.length - 1) {
                if (split[i + 1].equals("/")) {
                    b.append("/");
                }
            }
            l += token[i].length();
        }
        return b.toString();
    }

    static private void printDagGraphvizH(State s, int depth, Writer writer, HashSet<StateIdentity> writtenNodes, boolean stopAtChanceNode) throws IOException {
        if (writtenNodes.contains(new StateIdentity(s))) {
            return;
        }
        writtenNodes.add(new StateIdentity(s));
        if (s instanceof ChanceState cs) {
            writer.write(String.format("    \"%s\" [shape=\"box\", label=\"Chance State (n=%d, node_n=%d)\"]\n", System.identityHashCode(s) + ":" + s.hashCode(), cs.total_n, cs.total_node_n));
        } else {
            var str = s.toString();
            str = str.substring(1, str.length() - 1);
            writer.write(String.format("    \"%s\" [shape=\"box\", label=\"%s\"]\n", System.identityHashCode(s) + ":" + s.hashCode(), lineBreak(str, 60)));
        }
        if (depth < 0) {
            return;
        }
        if (s instanceof ChanceState cState) {
            for (ChanceState.Node node : cState.cache.values().stream().sorted((a, b) -> Long.compare(b.n, a.n)).toList()) {
                var n = node.state.total_n + 1;
                var q_comb = node.state.total_q_comb;
                var q_win = node.state.total_q_win;
                var q_health = node.state.total_q_health;
                var chanceStr = node.state.getStateDescStr();
                var label = "n=%d/%d, q=%s/%s/%s".formatted(n, node.n, Utils.formatFloat(q_comb / n), Utils.formatFloat(q_win / n), Utils.formatFloat(q_health / n));
                if (chanceStr.length() > 0) {
                    label = chanceStr + ", " + label;
                }
                writer.write(String.format("    \"%s\" -> \"%s\" [label=\"%s\"]\n", System.identityHashCode(s) + ":" + s.hashCode(), System.identityHashCode(node.state) + ":" + node.state.hashCode(), lineBreak(label, 40)));
                printDagGraphvizH(node.state, depth, writer, writtenNodes, stopAtChanceNode);
            }
        } else if (s instanceof GameState state) {
            var list = new ArrayList<int[]>();
            for (int i = 0; i < state.n.length; i++) {
                list.add(new int[] { state.n[i], i });
            }
            list.sort((a, b) -> b[0] != a[0] ? Integer.compare(b[0], a[0]) : Integer.compare(a[1], b[1]));
            for (var x : list) {
                var i = x[1];
                if (state.ns[i] != null && depth > 0) {
                    var n = state.n[i];
                    var q_comb = state.q_comb[i];
                    var q_win = state.q_win[i];
                    var q_health = state.q_health[i];
                    var label = "%s, n=%d, q=%s/%s/%s".formatted(state.getActionString(i), n, Utils.formatFloat(q_comb / n), Utils.formatFloat(q_win / n), Utils.formatFloat(q_health / n));
                    writer.write(String.format("    \"%s\" -> \"%s\" [label=\"%s\"]\n", System.identityHashCode(s) + ":" + s.hashCode(), System.identityHashCode(state.ns[i]) + ":" + state.ns[i].hashCode(), lineBreak(label, 40)));
                    printDagGraphvizH(state.ns[i], depth - 1, writer, writtenNodes, stopAtChanceNode);
                }
            }
        }
    }

    private static class StateIdentity {
        State st;

        public StateIdentity(State s) {
            st = s;
        }

        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            StateIdentity that = (StateIdentity) o;
            return st == that.st;
        }

        @Override public int hashCode() {
            return System.identityHashCode(st);
        }
    }

    static void printDagGraphviz(State state, Writer writer, int depth) {
        try {
            if (writer == null) {
                writer = new OutputStreamWriter(System.out);
            }
            writer.write("digraph G {\n");
            var writtenNodes = new HashSet<StateIdentity>();
            printDagGraphvizH(state, depth, writer, writtenNodes, false);
            writer.write("}");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
