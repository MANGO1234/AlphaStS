package com.alphaStS;

import com.alphaStS.card.Card;
import com.alphaStS.enemy.EnemyEnding;
import com.alphaStS.enemy.EnemyReadOnly;
import com.alphaStS.utils.Utils;

import java.io.*;
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
            state.setIsStochastic();
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
        int size = state.properties.randomization.listRandomizations().size();
        if (state.properties.preBattleRandomization != null) {
            size *= state.properties.preBattleRandomization.listRandomizations().size();
        }
        if (stride >= 1) {
            if (size % (groupSize * stride) != 0) {
                return null;
            }
        } else {
            if (size % groupSize != 0) {
                return null;
            }
            stride = size / groupSize;
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

    public static void writeStateDescription(GameState state, File f) throws IOException {
        var buf = new BufferedWriter(new FileWriter(f));
        writeStateDescription(state, buf);
        buf.close();
    }

    public static void writeStateDescription(GameState state, Writer writer) throws IOException {
        writer.write("************************** NN Description **************************\n");
        writer.write(state.getNNInputDesc());
        if (state.properties.randomization != null || state.properties.preBattleRandomization != null) {
            writer.write("\n************************** Randomizations **************************\n");
            var randomization = state.properties.preBattleRandomization;
            if (randomization == null) {
                randomization = state.properties.randomization;
            } else if (state.properties.randomization != null) {
                randomization = randomization.doAfter(state.properties.randomization);
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
    }

    public static boolean isSurrounded(GameState state) {
        boolean spireShield = false;
        for (EnemyReadOnly enemy : state.getEnemiesForRead()) {
            if (enemy.isAlive() && enemy instanceof EnemyEnding.SpireShield) {
                 spireShield = true;
            } else if (enemy.isAlive() && enemy instanceof EnemyEnding.SpireSpear) {
                if (spireShield) {
                    return true;
                }
            }
        }
        return false;
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
        for (Card card : from.properties.cardDict) {
            cardMap.put(card, new CardChanges());
        }
        var fromGetHandForRead = GameStateUtils.getCardArrCounts(from.getHandArrForRead(), from.getNumCardsInHand(), from.properties.cardDict.length);
        var toGetHandForRead = GameStateUtils.getCardArrCounts(to.getHandArrForRead(), to.getNumCardsInHand(), to.properties.cardDict.length);
        for (int i = 0; i < fromGetHandForRead.length; i++) {
            if (fromGetHandForRead[i] != toGetHandForRead[i]) {
                cardMap.get(from.properties.cardDict[i]).hand = toGetHandForRead[i] - fromGetHandForRead[i];
            }
        }
        var fromGetDeckForRead = GameStateUtils.getCardArrCounts(from.getDeckArrForRead(), from.getNumCardsInDeck(), from.properties.cardDict.length);
        var toGetDeckForRead = GameStateUtils.getCardArrCounts(to.getDeckArrForRead(), to.getNumCardsInDeck(), to.properties.cardDict.length);
        for (int i = 0; i < fromGetDeckForRead.length; i++) {
            if (fromGetDeckForRead[i] != toGetDeckForRead[i]) {
                cardMap.get(from.properties.cardDict[i]).deck = toGetDeckForRead[i] - fromGetDeckForRead[i];
            }
        }
        var fromGetDiscardForRead = GameStateUtils.getCardArrCounts(from.getDiscardArrForRead(), from.getNumCardsInDiscard(), from.properties.cardDict.length);
        var toGetDiscardForRead = GameStateUtils.getCardArrCounts(to.getDiscardArrForRead(), to.getNumCardsInDiscard(), to.properties.cardDict.length);
        for (int i = 0; i < fromGetDiscardForRead.length; i++) {
            if (fromGetDiscardForRead[i] != toGetDiscardForRead[i]) {
                cardMap.get(from.properties.cardDict[i]).discard = toGetDiscardForRead[i] - fromGetDiscardForRead[i];
            }
        }
        var fromGetExhaustForRead = GameStateUtils.getCardArrCounts(from.getExhaustArrForRead(), from.getNumCardsInExhaust(), from.properties.cardDict.length);
        var toGetExhaustForRead = GameStateUtils.getCardArrCounts(to.getExhaustArrForRead(), to.getNumCardsInExhaust(), to.properties.cardDict.length);
        for (int i = 0; i < fromGetExhaustForRead.length; i++) {
            if (fromGetExhaustForRead[i] != toGetExhaustForRead[i]) {
                cardMap.get(from.properties.cardDict[i]).exhaust = toGetExhaustForRead[i] - fromGetExhaustForRead[i];
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

    static private void printTreeH(GameState parentState, int parentAction, State s, int depth, Writer writer, String indent) throws IOException {
        if (depth < 0) {
            return;
        }
        if (s instanceof ChanceState cState) {
            writer.write(indent + parentState.toString() + "\n");
            for (ChanceState.Node node : cState.cache.values().stream().sorted((a, b) -> Long.compare(b.n, a.n)).toList()) {
                writer.write(indent + "Chance Node (" + node.n + "/" + cState.total_node_n + "): " + diffGameState(parentState, node.state) + "\n");
                var state = node.state;
                var n = node.state.total_n + 1;
                var q_comb = node.state.q[GameState.V_COMB_IDX];
                var q_win = node.state.q[GameState.V_WIN_IDX];
                var q_health = node.state.q[GameState.V_HEALTH_IDX];
                writer.write(indent + "n=" + n + ", q=" + Utils.formatFloat(q_comb / n) + ", q_win=" + Utils.formatFloat(q_win / n) + ", q_health=" + Utils.formatFloat(q_health / n) + " (" + Utils.formatFloat(q_health / n * state.getPlayeForRead().getMaxHealth()) + ") v=(" + Utils.formatFloat(state.v_win) + "/" + Utils.formatFloat(state.v_health) + "(" + Utils.formatFloat(state.v_health * state.getPlayeForRead().getMaxHealth()) + "))\n");
                printTreeH(parentState, parentAction, node.state, depth, writer, indent);
            }
        } else if (s instanceof GameState state) {
            var list = new ArrayList<int[]>();
            for (int i = 0; i < (state.n == null ? 0 : state.n.length); i++) {
                list.add(new int[] { state.n[i], i });
            }
            list.sort((a, b) -> b[0] != a[0] ? Integer.compare(b[0], a[0]) : Integer.compare(a[1], b[1]));
            for (var x : list) {
                var i = x[1];
                if (state.ns[i] != null && depth > 0) {
                    writer.write(indent + "  - action=" + state.getActionString(i) + " (" + i + ")");
                    var n = state.n[i];
                    var q_comb = state.q[(i + 1) * state.properties.v_total_len + GameState.V_COMB_IDX];
                    var q_win = state.q[(i + 1) * state.properties.v_total_len + GameState.V_WIN_IDX];
                    var q_health = state.q[(i + 1) * state.properties.v_total_len + GameState.V_HEALTH_IDX];
                    int l = (state.getActionString(i) + " (" + i + ")").length();
                    for (int j = 0; j < 24 - l; j++) {
                        writer.write(' ');
                    }
                    writer.write("n=" + n + " p=" + Utils.formatFloat(state.policy[i]) + ", q=" + Utils.formatFloat(q_comb / n) + ", q_win=" + Utils.formatFloat(q_win / n) + ", q_health=" + Utils.formatFloat(q_health / n) + " (" + Utils.formatFloat(q_health / n * state.getPlayeForRead().getMaxHealth()) + ") v=(" + Utils.formatFloat(state.v_win) + "/" + Utils.formatFloat(state.v_health) + "(" + Utils.formatFloat(state.v_health * state.getPlayeForRead().getMaxHealth()) + "))\n");
                }
                printTreeH(state, i, state.ns[i], depth - 1, writer, indent + "    ");
            }
        }
    }

    static void printTree(State state, Writer writer, int depth) {
        try {
            if (writer == null) {
                writer = new OutputStreamWriter(System.out);
            }
            printTreeH(null, -1, state, depth, writer, "");
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
                var q_comb = node.state.q[GameState.V_COMB_IDX];
                var q_win = node.state.q[GameState.V_WIN_IDX];
                var q_health = node.state.q[GameState.V_HEALTH_IDX];
                var chanceStr = node.state.getStateDescStr();
                var label = "n=%d/%d, q=%s/%s/%s".formatted(n, node.n, Utils.formatFloat(q_comb / n), Utils.formatFloat(q_win / n), Utils.formatFloat(q_health / n));
                if (chanceStr.length() > 0) {
                    label = chanceStr + ", " + label;
                }
                writer.write(String.format("    \"%s\" -> \"%s\" [label=\"%s\"]\n", System.identityHashCode(s) + ":" + s.hashCode(), System.identityHashCode(node.state) + ":" + node.state.hashCode(), lineBreak(label, 40)));
                printDagGraphvizH(node.state, depth, writer, writtenNodes, stopAtChanceNode);
            }
        } else if (s instanceof GameState state && state.n != null) {
            var list = new ArrayList<int[]>();
            for (int i = 0; i < state.n.length; i++) {
                list.add(new int[] { state.n[i], i });
            }
            list.sort((a, b) -> b[0] != a[0] ? Integer.compare(b[0], a[0]) : Integer.compare(a[1], b[1]));
            for (var x : list) {
                var i = x[1];
                if (state.ns[i] != null && depth > 0) {
                    var n = state.n[i];
                    var q_comb = state.q[(i + 1) * state.properties.v_total_len + GameState.V_COMB_IDX];
                    var q_win = state.q[(i + 1) * state.properties.v_total_len + GameState.V_WIN_IDX];
                    var q_health = state.q[(i + 1) * state.properties.v_total_len + GameState.V_HEALTH_IDX];
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

    public static byte[] getCardArrCounts(short[] cards, int cardsLen, int len) {
        byte[] counts = new byte[len];
        for (int i = 0; i < cardsLen; i++) {
            counts[cards[i]]++;
        }
        return counts;
    }

    public static int getCardsCount(short[] cards, int len, boolean[] want) {
        int count = 0;
        for (int i = 0; i < len; i++) {
            if (want[cards[i]]) {
                count++;
            }
        }
        return count;
    }

    public static int getCardCount(short[] cards, int len, int idx) {
        int count = 0;
        for (int i = 0; i < len; i++) {
            if (cards[i] == idx) {
                count++;
            }
        }
        return count;
    }
}
