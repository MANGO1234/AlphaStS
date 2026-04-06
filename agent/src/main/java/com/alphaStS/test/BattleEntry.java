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
    private final String playId;

    public BattleEntry(int runIdx, int battleIdx, GameStateBuilder builder, String enemiesName, String playId) {
        this.runIdx = runIdx;
        this.battleIdx = battleIdx;
        this.builder = builder;
        this.enemiesName = enemiesName;
        this.playId = playId;
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

    public String getPlayId() {
        return playId;
    }
}
