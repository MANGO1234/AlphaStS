package com.alphaStS;

import com.alphaStS.card.Card;
import com.alphaStS.card.CardColorless2;
import com.alphaStS.card.CardRegent2;
import com.alphaStS.enemy.EnemyExordium;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.player.Player;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class BoundCardSelectionTest {
    @Test
    public void boundSelectFromHandCardCanFinishResolving() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.REGENT);
        builder.setPlayer(new Player(60, 60));
        builder.addCard(new CardRegent2.Begone(), 1);
        builder.addCard(new CardRegent2.Begone().getBoundIfPossible(), 1);
        builder.addCard(new CardColorless2.MinionStrike(), 1);
        builder.addCard(new Card.Strike(), 1);
        builder.addCard(new Card.Defend(), 1);
        builder.addEnemyEncounter(new EnemyExordium.Cultist());
        var state = new GameState(builder);
        int begoneIdx = state.properties.findCardIndex(new CardRegent2.Begone().getBoundIfPossible());
        int strikeIdx = state.properties.findCardIndex(new Card.Strike());
        int defendIdx = state.properties.findCardIndex(new Card.Defend());
        int minionStrikeIdx = state.properties.findCardIndex(new CardColorless2.MinionStrike());

        state.addCardToHand(begoneIdx);
        state.addCardToHand(strikeIdx);
        state.addCardToHand(defendIdx);
        state.setActionCtx(GameActionCtx.PLAY_CARD, null, null);
        state.energy = 1;
        state.doAction(actionPosition(state.getLegalActions(), begoneIdx));

        assertEquals(GameActionCtx.SELECT_CARD_HAND, state.getActionCtx());

        state.doAction(actionPosition(state.getLegalActions(), strikeIdx));

        assertEquals(GameActionCtx.PLAY_CARD, state.getActionCtx());
        assertEquals(1, GameStateUtils.getCardCount(state.getHandArrForRead(), state.getNumCardsInHand(), minionStrikeIdx));
        assertFalse(containsAction(state.getLegalActions(), begoneIdx));
    }

    private static boolean containsAction(int[] actions, int action) {
        for (int a : actions) {
            if (a == action) {
                return true;
            }
        }
        return false;
    }

    private static int actionPosition(int[] actions, int action) {
        for (int i = 0; i < actions.length; i++) {
            if (actions[i] == action) {
                return i;
            }
        }
        throw new AssertionError("Action not found: " + action);
    }
}
