package com.alphaStS;

public abstract class OnDamageHandler {
    public abstract void handle(GameState state, Object source, boolean isAttack, int damageDealt);
}
