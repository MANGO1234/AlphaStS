package com.alphaStS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alphaStS.utils.Utils.formatFloat;

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
            if (line.state instanceof GameState) {
                GameState state = (GameState) line.state;
                if (state.terminalAction == -1234) {
                    maxLine = line;
                    break;
                }
            }
            if (line.n > max_n) {
                maxLine = line;
                max_n = maxLine.n;
            } else if (line.n == max_n) {
                if (line.q_comb / line.n > maxLine.q_comb / maxLine.n) {
                    maxLine = line;
                }
            }
        }
        return maxLine;
    }

    @Override public String toString() {
        var str = "SearchFrontier{total_n=" + total_n + ", lines=[\n    ";
        str += String.join("\n    ", lines.values().stream().sorted((a, b) -> {
            if (a.internal == b.internal) {
                return -Integer.compare(a.n, b.n);
            } else if (a.internal) {
                return 1;
            } else {
                return -1;
            }
        }).map(LineOfPlay::toString).toList());
        str += "\n]}";
        return str;
    }

    public boolean isOneOfBestLine(LineOfPlay line) {
        var bestLine = getBestLine();
//        int i = actions.size() - 1;
//        while (line.parentLines != null) {
//            if (i < 0) {
//                return false;
//            }
//            boolean found = false;
//            for (LineOfPlay.Edge parentLine : line.parentLines) {
//                if (parentLine.action() == actions) {
//
//                }
//            }
//            i--;
//        }
//        if (i >= 0) {
//            return false;
//        }
        return line.state.equals(bestLine.state);
    }

    public List<String> getSortedLinesAsStrings(GameState state) {
        return lines.values().stream().sorted((a, b) -> {
            if (a.internal == b.internal) {
                return -Integer.compare(a.n, b.n);
            } else if (a.internal) {
                return 1;
            } else {
                return -1;
            }
        }).map((x) -> {
            var tmpS = state.clone(false);
            var actions = x.getActions(tmpS);
            var strings = new ArrayList<String>();
            for (var _action : actions) {
                if (tmpS.getActionString(_action).equals("Begin Turn")) {
                    continue;
                }
                strings.add(tmpS.getActionString(_action));
                tmpS.doAction(_action);
            }
            return String.join(", ", strings) + ": n=" + x.n + ", p=" + formatFloat(x.p_cur) + ", q=" + formatFloat(x.q_comb / x.n) +
                    ", q_win=" + formatFloat(x.q_win / x.n) + ", q_health=" + formatFloat(x.q_health / x.n)  +
                    " (" + formatFloat(x.q_health / x.n * state.getPlayeForRead().getMaxHealth()) + ")";
        }).toList();
    }
}
