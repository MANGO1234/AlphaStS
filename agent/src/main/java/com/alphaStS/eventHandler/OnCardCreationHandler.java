package com.alphaStS.eventHandler;

import com.alphaStS.GameState;

public abstract class OnCardCreationHandler {
    public abstract void handle(GameState state, int cardIdx);
}
