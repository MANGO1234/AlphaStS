package com.alphaStS.eventHandler;

import com.alphaStS.GameState;

public abstract class OnStarChangeHandler {
    public abstract void handle(GameState state, int amount);
}
