package com.alphaStS;

public interface TrainingTarget {
    void fillVArray(GameState state, VArray v, int isTerminal);
    void updateQValues(GameState state, VArray v);
    default int getNumberOfTargets() {
        return 1;
    }
}
