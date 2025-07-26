package com.alphaStS.action;

import com.alphaStS.GameActionCtx;
import com.alphaStS.GameState;
import com.alphaStS.GameStateUtils;
import com.alphaStS.RandomGenCtx;
import com.alphaStS.card.Card;

public class HavocExhaustAction implements GameEnvironmentAction {
    int cardIdx;

    public HavocExhaustAction(int cardIdx) {
        this.cardIdx = cardIdx;
    }

    @Override public void doAction(GameState state) {
        if (state.properties.cardDict[cardIdx].cardType != Card.POWER) {
            state.exhaustedCardHandle(cardIdx, true);
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HavocExhaustAction that = (HavocExhaustAction) o;
        return cardIdx == that.cardIdx;
    }

    @Override public int hashCode() {
        return cardIdx;
    }

    @Override public String toString() {
        return "HavocExhaustAction(" + cardIdx + ")";
    }
}