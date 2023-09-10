package com.alphaStS;

public record GameAction(GameActionType type, int idx) { // idx is either cardIdx, enemyIdx, potionIdx, etc.
}
