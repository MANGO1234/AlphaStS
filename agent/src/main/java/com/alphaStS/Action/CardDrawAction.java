package com.alphaStS.action;

import com.alphaStS.GameState;

public class CardDrawAction implements GameEnvironmentAction {
    int n;

    public CardDrawAction(int n) {
        this.n = n;
    }

    @Override public void doAction(GameState state) {
        state.draw(n);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CardDrawAction that = (CardDrawAction) o;
        return n == that.n;
    }

    @Override public int hashCode() {
        return n;
    }
}
