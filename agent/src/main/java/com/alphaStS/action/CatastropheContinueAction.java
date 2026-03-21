package com.alphaStS.action;

import com.alphaStS.GameState;

public class CatastropheContinueAction implements GameEnvironmentAction {
    @Override public void doAction(GameState state) {
        state.getCounterForWrite()[state.properties.catastropheCounterIdx]--;
        int cardIdx = state.takeRandomCardFromDeck();
        if (cardIdx < 0) {
            state.getCounterForWrite()[state.properties.catastropheCounterIdx] = 0;
            return;
        }
        if (state.properties.makingRealMove || state.properties.stateDescOn) {
            if (!state.getStateDesc().isEmpty()) {
                state.getStateDesc().append(", ");
            }
            state.getStateDesc().append(state.properties.cardDict[cardIdx].cardName);
        }
        if (state.getCounterForRead()[state.properties.catastropheCounterIdx] > 0) {
            state.addGameActionToStartOfDeque(new CatastropheContinueAction());
        }
        state.addGameActionToStartOfDeque(new CatastropheAction(cardIdx));
    }

    @Override public boolean equals(Object o) {
        return o instanceof CatastropheContinueAction;
    }

    @Override public int hashCode() {
        return CatastropheContinueAction.class.hashCode();
    }

    @Override public String toString() {
        return "CatastropheContinueAction";
    }
}
