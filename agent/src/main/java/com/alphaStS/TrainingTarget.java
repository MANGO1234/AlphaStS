package com.alphaStS;

public interface TrainingTarget {
    void fillVArray(GameState state, double[] v, boolean enemiesAllDead);
    void updateQValues(GameState state, double[] v);
    default int getNumberOfTargets() {
        return 1;
    }
}
