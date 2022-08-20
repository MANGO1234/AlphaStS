package com.alphaStS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.alphaStS.utils.Utils.formatFloat;

public class LineOfPlay {
    State state;
    int n;
    double p_total;
    double p_cur;
    double q_win;
    double q_health;
    double q_comb;
    boolean internal;
    int numberOfActions;
    List<Edge> parentLines;

    public LineOfPlay(State state, double p, LineOfPlay parentLine, int action) {
        this.state = state;
        this.p_cur = p;
        this.p_total = p;
        if (state instanceof GameState s) {
            numberOfActions = s.getLegalActions().length;
        } else {
            numberOfActions = 1;
        }
        if (parentLine != null) {
            parentLines = new ArrayList<>();
            parentLines.add(new Edge(parentLine, action));
        }
    }

    public List<Integer> getActions(GameState s) {
        var rand = s.prop.random;
        var actions = new ArrayList<Integer>();
        var line = this;
        if (line.parentLines == null) {
            var state = (GameState) line.state;
            var maxP = 0.0;
            var maxAction = -1;
            for (int i = 0; i < state.policy.length; i++) {
                if (state.policy[i] > maxP) {
                    maxAction = i;
                    maxP = state.policy[i];
                }
            }
            actions.add(maxAction);
            return actions;
        }
        while (line.parentLines != null) {
            var r = line.parentLines.size() == 1 ? 0 : rand.nextInt(line.parentLines.size(), RandomGenCtx.Other);
            if (((GameState) line.parentLines.get(r).line.state).getAction(line.parentLines.get(r).action).type() != GameActionType.BEGIN_TURN) {
                actions.add(line.parentLines.get(r).action);
            }
            line = line.parentLines.get(r).line;
        }
        Collections.reverse(actions);
        return actions;
    }

    @Override public String toString() {
        return "LineOfPlay{" +
                "n=" + n +
                ", p=" + formatFloat(p_cur) + "/" + formatFloat(p_total) +
                ", q_comb=" + formatFloat(q_comb / n) +
                ", " + state +
                ", " + numberOfActions +
                '}';
    }

    static record Edge(LineOfPlay line, int action) {}
}
