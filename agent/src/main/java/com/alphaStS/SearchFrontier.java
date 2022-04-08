package com.alphaStS;

import java.util.HashMap;
import java.util.Map;

public class SearchFrontier {
    Map<State, LineOfPlay> lines = new HashMap<>();
    int total_n;

    public void addLine(LineOfPlay line) {
        lines.put(line.state, line);
    }

    public void removeLine(LineOfPlay line) {
        lines.remove(line.state);
    }

    public LineOfPlay getLine(State state) {
        return lines.get(state);
    }

    public LineOfPlay getBestLine() {
        var max_n = 0;
        LineOfPlay maxLine = null;
        for (var line : lines.values()) {
            if (line.internal) {
                continue;
            }
            if (line.n > max_n) {
                maxLine = line;
                max_n = maxLine.n;
            } else if (line.n == max_n) {
                if (maxLine == null) {
                    System.out.println(this);
                }
                if (line.q_comb / line.n > maxLine.q_comb / maxLine.n) {
                    maxLine = line;
                }
            }
        }
        return maxLine;
    }

    @Override public String toString() {
        var str = "SearchFrontier{total_n=" + total_n + ", lines=[\n    ";
        str += String.join("\n    ", lines.values().stream().map(LineOfPlay::toString).toList());
        str += "\n]}";
        return str;
    }
}
