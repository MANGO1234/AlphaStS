package com.alphaStS;

import com.alphaStS.enemy.EnemyReadOnly;

public abstract class GameEventEnemyHandler implements Comparable<GameEventEnemyHandler> {
    private int priority;

    public GameEventEnemyHandler(int priority) {
        this.priority = priority;
    }

    public GameEventEnemyHandler() {
    }

    public abstract void handle(GameState state, EnemyReadOnly enemy);

    @Override public int compareTo(GameEventEnemyHandler other) {
        return Integer.compare(other.priority, priority);
    }
}