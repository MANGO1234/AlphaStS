package com.alphaStS;

import java.util.Map;

public interface GameStateRandomization {
    int randomize(GameState state);
    void reset(GameState state);
    void randomize(GameState state, int r);
    Map<Integer, Info> listRandomizations(GameState state);

    record Info(double chance, String desc) {};
}
