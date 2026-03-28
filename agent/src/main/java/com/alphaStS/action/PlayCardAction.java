package com.alphaStS.action;

import com.alphaStS.GameState;
import com.alphaStS.GameStateUtils;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.random.RandomGenCtx;

public class PlayCardAction implements GameEnvironmentAction {
    int cardIdx;
    GameState state;

    public PlayCardAction(int cardIdx, GameState state) {
        this.cardIdx = cardIdx;
        this.state = state;
    }

    @Override public void doAction(GameState state) {
        var action = state.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
        state.playCard(action, -1, true, null, false, true, -1, -1);
        while (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
            int enemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
            state.playCard(action, enemyIdx, true, null, false, true, -1, -1);
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PlayCardAction that = (PlayCardAction) o;
        return cardIdx == that.cardIdx;
    }

    @Override public int hashCode() {
        return cardIdx;
    }

    @Override public String toString() {
        return "PlayCardAction(" + state.properties.cardDict[cardIdx].cardName + ")";
    }
}
