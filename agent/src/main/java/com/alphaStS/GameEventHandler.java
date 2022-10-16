package com.alphaStS;

public abstract class GameEventHandler implements Comparable<GameEventHandler> {
    private int priority;

    protected GameEventHandler(int priority) {
        this.priority = priority;
    }

    protected GameEventHandler() {
    }

    public abstract void handle(GameState state);

    @Override public int compareTo(GameEventHandler other) {
        return Integer.compare(priority, other.priority);
    }
}
