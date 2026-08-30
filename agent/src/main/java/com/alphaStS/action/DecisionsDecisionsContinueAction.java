package com.alphaStS.action;

import com.alphaStS.GameState;

public class DecisionsDecisionsContinueAction implements GameEnvironmentAction {
    int cardIdx;

    public DecisionsDecisionsContinueAction(int cardIdx) {
        this.cardIdx = cardIdx;
    }

    @Override public void doAction(GameState state) {
        if (state.getCounterForRead()[state.properties.decisionsDecisionsCounterIdx] == 0) {
            state.handleCardFinishPlayingLocation(null, -1, cardIdx, 0, -1);
            return;
        }
        state.getCounterForWrite()[state.properties.decisionsDecisionsCounterIdx]--;
        state.addGameActionToStartOfDeque(new DecisionsDecisionsContinueAction(cardIdx));
        state.addGameActionToStartOfDeque(new DecisionsDecisionsAction(cardIdx));
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionsDecisionsContinueAction that = (DecisionsDecisionsContinueAction) o;
        return cardIdx == that.cardIdx;
    }

    @Override public int hashCode() {
        return cardIdx;
    }

    @Override public String toString() {
        return "DecisionsDecisionsContinueAction(" + cardIdx + ")";
    }
}
