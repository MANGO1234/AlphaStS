package com.alphaStS;

import com.alphaStS.card.Card;
import com.alphaStS.card.CardManager;
import com.alphaStS.card.CardOther;
import com.alphaStS.enemy.EnemyExordium;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.entity.Relic2;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.player.Player;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CardManagerTest {
    @Test
    public void getCharacterRareCardsMatchesFilteredCharacterCards() {
        for (CharacterEnum character : CharacterEnum.values()) {
            assertRareCardsMatchFilteredCharacterCards(character, false);
            assertRareCardsMatchFilteredCharacterCards(character, true);
        }
    }

    @Test
    public void getCharacterCommonCardsMatchesFilteredCharacterCards() {
        for (CharacterEnum character : CharacterEnum.values()) {
            List<String> commonCards = CardManager.getCharacterCommonCards(character).stream()
                    .map((card) -> card.cardName)
                    .toList();
            List<String> filteredCommonCards = CardManager.getCharacterCards(character, true).stream()
                    .filter((card) -> card.rarity == Card.COMMON)
                    .map((card) -> card.cardName)
                    .toList();

            assertEquals(character.toString(), filteredCommonCards, commonCards);
        }
    }

    private static void assertRareCardsMatchFilteredCharacterCards(CharacterEnum character, boolean generateHealingCard) {
        List<String> oldRareCards = CardManager.getCharacterRareCards(character, generateHealingCard).stream()
                .map((card) -> card.cardName)
                .toList();
        List<String> filteredRareCards = CardManager.getCharacterCards(character, generateHealingCard).stream()
                .filter((card) -> card.rarity == Card.RARE)
                .map((card) -> card.cardName)
                .toList();

        assertEquals(character + " generateHealingCard=" + generateHealingCard, oldRareCards, filteredRareCards);
    }

    private static boolean containsAction(int[] actions, int action) {
        for (int a : actions) {
            if (a == action) {
                return true;
            }
        }
        return false;
    }

    private static int getNonExhaustCardCount(GameState state, int cardIdx) {
        return GameStateUtils.getCardCount(state.getDeckArrForRead(), state.getNumCardsInDeck(), cardIdx)
                + GameStateUtils.getCardCount(state.getHandArrForRead(), state.getNumCardsInHand(), cardIdx)
                + GameStateUtils.getCardCount(state.getDiscardArrForRead(), state.getNumCardsInDiscard(), cardIdx);
    }
}
