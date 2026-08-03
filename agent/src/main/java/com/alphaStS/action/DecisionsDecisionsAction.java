package com.alphaStS.action;

import com.alphaStS.GameState;
import com.alphaStS.GameStateUtils;
import com.alphaStS.card.CardRegent2;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.random.RandomGenCtx;

public class DecisionsDecisionsAction implements GameEnvironmentAction {
    int cardIdx;

    public DecisionsDecisionsAction(int cardIdx) {
        this.cardIdx = cardIdx;
    }

    @Override public void doAction(GameState state) {
        var action = state.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
        state.playCard(action, -1, true, CardRegent2.DecisionsDecisions.class, false, false, -1, -1);
        while (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
            int enemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
            state.playCard(action, enemyIdx, true, CardRegent2.DecisionsDecisions.class, false, false, -1, -1);
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DecisionsDecisionsAction that = (DecisionsDecisionsAction) o;
        return cardIdx == that.cardIdx;
    }

    @Override public int hashCode() {
        return cardIdx;
    }

    @Override public String toString() {
        return "DecisionsDecisionsAction(" + cardIdx + ")";
    }
}
