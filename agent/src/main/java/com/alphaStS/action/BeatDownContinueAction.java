package com.alphaStS.action;

import com.alphaStS.GameState;
import com.alphaStS.card.Card;
import com.alphaStS.random.RandomGenCtx;
import com.alphaStS.utils.Tuple;

import java.util.ArrayList;

public class BeatDownContinueAction implements GameEnvironmentAction {
    @Override public void doAction(GameState state) {
        state.getCounterForWrite()[state.properties.beatDownCounterIdx]--;
        var attackIdxes = new ArrayList<Integer>();
        for (int i = 0; i < state.discardArrLen; i++) {
            if (state.properties.cardDict[state.discardArr[i]].cardType == Card.ATTACK) {
                attackIdxes.add(i);
            }
        }
        if (attackIdxes.isEmpty()) {
            state.getCounterForWrite()[state.properties.beatDownCounterIdx] = 0;
            return;
        }
        int pos;
        if (attackIdxes.size() > 1) {
            state.setIsStochastic();
            int target = state.getSearchRandomGen().nextInt(attackIdxes.size(), RandomGenCtx.RandomCardDiscard, new Tuple<>(state, attackIdxes));
            pos = attackIdxes.get(target);
        } else {
            pos = attackIdxes.get(0);
        }
        int cardIdx = state.discardArr[pos];
        if (state.properties.makingRealMove || state.properties.stateDescOn) {
            if (!state.getStateDesc().isEmpty()) {
                state.getStateDesc().append(", ");
            }
            state.getStateDesc().append(state.properties.cardDict[cardIdx].cardName);
        }
        state.removeCardFromDiscardByPosition(pos);
        state.addGameActionToEndOfDeque(new BeatDownAction(cardIdx));
        if (state.getCounterForRead()[state.properties.beatDownCounterIdx] > 0) {
            state.addGameActionToEndOfDeque(new BeatDownContinueAction());
        }
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
