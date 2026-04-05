package com.alphaStS.test;

import com.alphaStS.GameStateBuilder;

/**
 * Holds all data for a single battle extracted from run data.
 */
public class BattleEntry {

    private final int runIdx;
    private final int battleIdx;
    private final GameStateBuilder builder;
    private final String enemiesName;

    public BattleEntry(int runIdx, int battleIdx, GameStateBuilder builder, String enemiesName) {
        this.runIdx = runIdx;
        this.battleIdx = battleIdx;
        this.builder = builder;
        this.enemiesName = enemiesName;
    }

    public int getRunIdx() {
        return runIdx;
    }

    public int getBattleIdx() {
        return battleIdx;
    }

    public GameStateBuilder getBuilder() {
        return builder;
    }

    public String getEnemiesName() {
        return enemiesName;
    }
}
