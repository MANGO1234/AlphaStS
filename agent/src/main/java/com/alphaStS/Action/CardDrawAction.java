package com.alphaStS.Action;

import com.alphaStS.GameState;

public class CardDrawAction implements GameEnvironmentAction {
    int n;

    public CardDrawAction(int n) {
        this.n = n;
    }

    @Override public void doAction(GameState state) {
        state.draw(n);
    }
}
