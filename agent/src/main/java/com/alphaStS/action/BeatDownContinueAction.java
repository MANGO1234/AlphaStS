package com.alphaStS.action;

import com.alphaStS.GameState;
import com.alphaStS.card.Card;
import com.alphaStS.random.RandomGenCtx;

public class BeatDownContinueAction implements GameEnvironmentAction {
    @Override public void doAction(GameState state) {
        state.getCounterForWrite()[state.properties.beatDownCounterIdx]--;
        int attackCount = 0;
        for (int i = 0; i < state.discardArrLen; i++) {
            if (state.properties.cardDict[state.discardArr[i]].cardType == Card.ATTACK) {
                attackCount++;
            }
        }
        if (attackCount == 0) {
            state.getCounterForWrite()[state.properties.beatDownCounterIdx] = 0;
            return;
        }
        state.setIsStochastic();
        int target = state.getSearchRandomGen().nextInt(attackCount, RandomGenCtx.RandomCardGen, state);
        int found = 0;
        int cardIdx = -1;
        int pos = -1;
        for (int i = 0; i < state.discardArrLen; i++) {
            if (state.properties.cardDict[state.discardArr[i]].cardType == Card.ATTACK) {
                if (found == target) {
                    cardIdx = state.discardArr[i];
                    pos = i;
                    break;
                }
                found++;
            }
        }
        if (state.properties.makingRealMove || state.properties.stateDescOn) {
            if (!state.getStateDesc().isEmpty()) {
                state.getStateDesc().append(", ");
            }
            state.getStateDesc().append(state.properties.cardDict[cardIdx].cardName);
        }
        state.removeCardFromDiscardByPosition(pos);
        if (state.getCounterForRead()[state.properties.beatDownCounterIdx] > 0) {
            state.addGameActionToStartOfDeque(new BeatDownContinueAction());
        }
        state.addGameActionToStartOfDeque(new BeatDownAction(cardIdx));
    }

    @Override public boolean equals(Object o) {
        return o instanceof BeatDownContinueAction;
    }

    @Override public int hashCode() {
        return BeatDownContinueAction.class.hashCode();
    }

    @Override public String toString() {
        return "BeatDownContinueAction";
    }
}
