package com.alphaStS;

import com.alphaStS.card.Card;
import com.alphaStS.card.CardManager;
import com.alphaStS.enums.CharacterEnum;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class CardManagerTest {
    @Test
    public void getCharacterRareCardsMatchesFilteredCharacterCards() {
        for (CharacterEnum character : CharacterEnum.values()) {
            assertRareCardsMatchFilteredCharacterCards(character, false);
            assertRareCardsMatchFilteredCharacterCards(character, true);
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
}
