package com.alphaStS.eventHandler;

import com.alphaStS.GameState;

public abstract class OnEnergySpendHandler {
    public abstract void handle(GameState state, int energySpent);
}
