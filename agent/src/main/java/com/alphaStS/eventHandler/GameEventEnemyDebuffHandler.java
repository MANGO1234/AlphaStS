package com.alphaStS.eventHandler;

import com.alphaStS.GameState;
import com.alphaStS.enemy.EnemyReadOnly;
import com.alphaStS.enums.DebuffType;

public abstract class GameEventEnemyDebuffHandler {
    public abstract void handle(GameState state, EnemyReadOnly enemy, DebuffType type, int amount);
}
