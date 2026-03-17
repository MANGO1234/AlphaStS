package com.alphaStS.eventHandler;

import com.alphaStS.GameState;

public abstract class OnDamageHandler {
    public abstract void handle(GameState state, Object source, boolean isAttack, int damageDealt);
}
