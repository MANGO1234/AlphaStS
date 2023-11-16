package com.alphaStS.action;

import com.alphaStS.GameState;

public interface GameEnvironmentAction {
    void doAction(GameState state);
    default boolean canHappenInsideCardPlay() { return false; };
}
