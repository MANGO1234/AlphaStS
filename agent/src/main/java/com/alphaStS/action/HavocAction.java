package com.alphaStS.action;

import com.alphaStS.GameActionCtx;
import com.alphaStS.GameState;
import com.alphaStS.GameStateUtils;
import com.alphaStS.RandomGenCtx;

public class HavocAction implements GameEnvironmentAction {
    int cardIdx;

    public HavocAction(int cardIdx) {
        this.cardIdx = cardIdx;
    }

    @Override public void doAction(GameState state) {
        var action = state.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
        state.playCard(action, -1, true, null, false, true, -1, -1);
        while (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
            int enemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
            if (state.properties.makingRealMove || state.properties.stateDescOn) {
                state.getStateDesc().append(" -> ").append(state.getEnemiesForRead().get(enemyIdx).getName()).append(" (").append(enemyIdx).append(")");
            }
            state.playCard(action, enemyIdx, true, null, false, true, -1, -1);
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HavocAction that = (HavocAction) o;
        return cardIdx == that.cardIdx;
    }

    @Override public int hashCode() {
        return cardIdx;
    }

    @Override public String toString() {
        return "HavocAction(" + cardIdx + ")";
    }
}
