package com.alphaStS;

public abstract class GameEventCardHandler implements Comparable<GameEventCardHandler> {
    // todo: figure out how the game handle priority properly...
    public static int CLONE_CARD_PRIORITY = -100;
    public static int HEARTBEAT_PRIORITY = -10;

    private int priority;

    public GameEventCardHandler(int priority) {
        this.priority = priority;
    }

    public GameEventCardHandler() {
    }

    public abstract void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned);

    @Override public int compareTo(GameEventCardHandler other) {
        return Integer.compare(other.priority, priority);
    }
}
