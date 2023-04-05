package com.alphaStS;

import java.util.List;

public final class GameStep {
    private GameState state;
    private final int action;
    public boolean takenFromChanceStateCache;
    public double trainingWriteCount;
    public boolean trainingSkipOpening;
    public double[] v;
    public List<String> lines;
    public boolean isExplorationMove;
    public RandomGen searchRandomGenMCTS;
    public StringBuilder actionDesc;
    public String stateStr;

    GameStep(GameState state, int action) {
        this.state = state;
        this.action = action;
    }

    public GameState state() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public int action() {
        return action;
    }

    @Override public String toString() {
        return "GameStep{" +
                "state=" + state +
                ", action=" + getActionString() +
                '}';
    }

    public GameAction getAction() {
        if (action < 0) {
            return null;
        }
        return state().getAction(action());
    }

    public String getActionString() {
        if (action < 0) {
            return null;
        }
        return state().getActionString(action()) + (actionDesc == null ? "" : (" (" + actionDesc + ")"));
    }
}
