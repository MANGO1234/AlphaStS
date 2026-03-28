package com.alphaStS.action;

import com.alphaStS.GameState;

public class AddCardToDiscardAction implements GameEnvironmentAction {
    int cardIdx;
    GameState state;

    public AddCardToDiscardAction(int cardIdx, GameState state) {
        this.cardIdx = cardIdx;
        this.state = state;
    }

    @Override public void doAction(GameState state) {
        state.addCardToDiscard(cardIdx);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AddCardToDiscardAction that = (AddCardToDiscardAction) o;
        return cardIdx == that.cardIdx;
    }

    @Override public int hashCode() {
        return cardIdx;
    }

    @Override public String toString() {
        return "AddCardToDiscardAction(" + state.properties.cardDict[cardIdx].cardName + ")";
    }
}
