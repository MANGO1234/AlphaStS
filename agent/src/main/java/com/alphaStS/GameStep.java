package com.alphaStS;

import java.util.List;

public final class GameStep {
    private final GameState state;
    private final int action;
    public int trainingWriteCount;
    public double[] v;
    public List<String> lines;
    public boolean isExplorationMove;

    GameStep(GameState state, int action) {
        this.state = state;
        this.action = action;
    }

    public GameState state() {
        return state;
    }

    public int action() {
        return action;
    }

    @Override public String toString() {
        return "GameStep{" +
                "state=" + state +
                ", action=" + action +
                '}';
    }
}
