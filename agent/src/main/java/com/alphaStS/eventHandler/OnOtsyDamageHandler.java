package com.alphaStS.eventHandler;

import com.alphaStS.GameState;

public abstract class OnOtsyDamageHandler {
    public abstract void handle(GameState state, int amount);
}
