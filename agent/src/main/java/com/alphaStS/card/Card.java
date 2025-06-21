package com.alphaStS.card;

import com.alphaStS.GameActionCtx;
import com.alphaStS.GameProperties;
import com.alphaStS.GameState;
import com.alphaStS.Relic;

import java.util.List;
import java.util.Objects;

public abstract class Card implements GameProperties.CounterRegistrant, GameProperties.TrainingTargetRegistrant {
    public static int ATTACK = 0;
    public static int SKILL = 1;
    public static int POWER = 2;
    public static int CURSE = 3;
    public static int STATUS = 4;

    public static int COMMON = 0;
    public static int UNCOMMON = 1;
    public static int RARE = 2;

    public final int cardType;
    public final String cardName;
    public final int energyCost;
    public final int rarity;
    public boolean ethereal = false;
    public boolean innate = false;
    public boolean exhaustWhenPlayed = false;
    public boolean exhaustNonAttacks = false;
    public boolean alwaysDiscard = false;
    public boolean returnToDeckWhenPlay = false;
    public boolean retain = false;
    public boolean selectEnemy;
    public boolean delayUseEnergy;
    public boolean selectFromDiscard;
    public boolean selectFromExhaust;
    public boolean selectFromDeck;
    public boolean selectFromHand;
    public boolean isXCost;
    public boolean selectFromDiscardLater;
    public boolean selectFromHandLater;
    public boolean exhaustSkill;
    public boolean canExhaustAnyCard;
    public boolean discardNonAttack;
    public boolean canDiscardAnyCard;
    public boolean changePlayerStrength;
    public boolean changePlayerStrengthEot;
    public boolean changePlayerDexterity;
    public boolean changePlayerFocus;
    public boolean changePlayerArtifact;
    public boolean changePlayerVulnerable;
    public boolean vulnEnemy;
    public boolean weakEnemy;
    public boolean chokeEnemy;
    public boolean poisonEnemy;
    public boolean corpseExplosionEnemy;
    public boolean lockOnEnemy;
    public boolean talkToTheHandEnemy;
    public boolean markEnemy;
    public boolean affectEnemyStrength;
    public boolean affectEnemyStrengthEot;
    public boolean needsLastCardType;
    public boolean putCardOnTopDeck;
    public boolean healPlayer;
    public boolean scry;
    public boolean select1OutOf3CardEffectCard;
    public int generatedCardIdx = -1; // when getPossibleGeneratedCards return 1 card, this is the card index for it
    public int[] generatedCardIdxes; // when getPossibleGeneratedCards returns non-empty list, this is the card indexes for each card in the order of the list
    public int[] generatedCardReverseIdxes; // given a cardIdx, return the index of it in generatedCardIdxes (-1 otherwise)
    int counterIdx = -1;
    int vArrayIdx = -1;

    public void setCounterIdx(GameProperties gameProperties, int idx) {
        counterIdx = idx;
    }

    public int getCounterIdx(GameProperties gameProperties) {
        return counterIdx;
    }

    public void setVArrayIdx(GameProperties gameProperties, int idx) {
        vArrayIdx = idx;
    }

    public Card(String cardName, int cardType, int energyCost, int rarity) {
        this.cardType = cardType;
        this.cardName = cardName;
        this.energyCost = energyCost;
        this.rarity = rarity;
    }

    public int energyCost(GameState state) {
        return energyCost;
    }

    public Card setInnate(boolean innate) {
        this.innate = innate;
        return this;
    }

    public GameActionCtx play(GameState state, int idx, int energyUsed) { return GameActionCtx.PLAY_CARD; }
    public void onExhaust(GameState state) {}
    public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) { return List.of(); }
    public List<Card> getPossibleSelect1OutOf3Cards(GameProperties gameProperties) { return List.of(); }
    public List<Card> getPossibleTransformTmpCostCards(List<Card> cards) { return List.of(); }
    public int onPlayTransformCardIdx(GameProperties prop) { return -1; }
    public boolean canSelectCard(Card card) { return true; }

    public void setupGeneratedCardIndexes(GameProperties properties) {
        List<Card> possibleCards = getPossibleGeneratedCards(properties, List.of(properties.cardDict));
        if (possibleCards.isEmpty()) {
            return;
        }

        if (possibleCards.size() == 1) {
            generatedCardIdx = properties.findCardIndex(possibleCards.get(0));
        }

        generatedCardIdxes = new int[possibleCards.size()];
        for (int i = 0; i < possibleCards.size(); i++) {
            generatedCardIdxes[i] = properties.findCardIndex(possibleCards.get(i));
        }

        // Create reverse index mapping
        generatedCardReverseIdxes = new int[properties.cardDict.length];
        for (int i = 0; i < generatedCardReverseIdxes.length; i++) {
            generatedCardReverseIdxes[i] = -1;
        }
        for (int i = 0; i < generatedCardIdxes.length; i++) {
            generatedCardReverseIdxes[generatedCardIdxes[i]] = i;
        }
    }

    public void gamePropertiesSetup(GameState state) {}
    public void onDiscard(GameState state) {}
    public void onDiscardEndOfTurn(GameState state, int numCardsInHand) {}
    public Card getUpgrade() { return CardUpgrade.map.get(this); }
    public Card getTemporaryCostIfPossible(int temporaryCost) {
        if (energyCost < 0 || energyCost == temporaryCost || isXCost) {
            return this;
        }
        return new Card.CardTmpChangeCost(this, temporaryCost);
    }
    public Card getTmpRetainIfPossible() {
        if (retain) {
            return this;
        }
        return new Card.CardTmpRetain(this);
    }
    public Card getTemporaryCostUntilPlayedIfPossible(int temporaryCost) {
        var card = this;
        if (this instanceof Card.CardTmpChangeCost c) {
            card = c.card;
        }
        if (card.energyCost < 0 || card.energyCost == temporaryCost || card.isXCost) {
            return this;
        }
        return new Card.CardTmpUntilPlayedCost(card, temporaryCost);
    }

    public Card getPermCostIfPossible(int permCost) {
        if (energyCost < 0 || energyCost <= permCost || isXCost) {
            return this;
        }
        return new Card.CardPermChangeCost(this, permCost);
    }

    @Override public String toString() {
        return "Card{" +
                "cardName='" + cardName + '\'' +
                '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Card card = (Card) o;
        return Objects.equals(cardName, card.cardName);
    }

    @Override public int hashCode() {
        return Objects.hash(cardName);
    }

    public int realEnergyCost() {
        return energyCost;
    }

    public Card getBaseCard() {
        return this;
    }

    public Card wrap(Card card) {
        return card;
    }

    public static class CardTmpChangeCost extends Card {
        public final Card card;

        public CardTmpChangeCost(Card card, int energyCost) {
            super(card.cardName + " (Tmp " + energyCost + ")", card.cardType, energyCost, card.rarity);
            this.card = card;
            ethereal = card.ethereal;
            innate = card.innate;
            exhaustWhenPlayed = card.exhaustWhenPlayed;
            exhaustNonAttacks = card.exhaustNonAttacks;
            returnToDeckWhenPlay = card.returnToDeckWhenPlay;
            retain = card.retain;
            selectEnemy = card.selectEnemy;
            selectFromDiscard = card.selectFromDiscard;
            selectFromExhaust = card.selectFromExhaust;
            selectFromDeck = card.selectFromDeck;
            selectFromHand = card.selectFromHand;
            selectFromDiscardLater = card.selectFromDiscardLater;
            selectFromHandLater = card.selectFromHandLater;
            exhaustSkill = card.exhaustSkill;
            canExhaustAnyCard = card.canExhaustAnyCard;
            changePlayerStrength = card.changePlayerStrength;
            changePlayerStrengthEot = card.changePlayerStrengthEot;
            changePlayerDexterity = card.changePlayerDexterity;
            changePlayerFocus = card.changePlayerFocus;
            changePlayerArtifact = card.changePlayerArtifact;
            changePlayerVulnerable = card.changePlayerVulnerable;
            vulnEnemy = card.vulnEnemy;
            weakEnemy = card.weakEnemy;
            chokeEnemy = card.chokeEnemy;
            poisonEnemy = card.poisonEnemy;
            corpseExplosionEnemy = card.corpseExplosionEnemy;
            lockOnEnemy = card.lockOnEnemy;
            talkToTheHandEnemy = card.talkToTheHandEnemy;
            markEnemy = card.markEnemy;
            affectEnemyStrength = card.affectEnemyStrength;
            affectEnemyStrengthEot = card.affectEnemyStrengthEot;
            needsLastCardType = card.needsLastCardType;
            putCardOnTopDeck = card.putCardOnTopDeck;
            healPlayer = card.healPlayer;
        }

        public void setCounterIdx(GameProperties gameProperties, int idx) {
            card.setCounterIdx(gameProperties, idx);
        }

        public int getCounterIdx(GameProperties gameProperties) {
            return card.getCounterIdx(gameProperties);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) { return card.play(state, idx, energyUsed); }
        public void onExhaust(GameState state) { card.onExhaust(state); }
        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) { return card.getPossibleGeneratedCards(properties, cards); }
        public List<Card> getPossibleTransformTmpCostCards(List<Card> cards) { return card.getPossibleTransformTmpCostCards(cards); }
        public int onPlayTransformCardIdx(GameProperties prop) { return card.onPlayTransformCardIdx(prop); }
        public boolean canSelectCard(Card card2) { return card.canSelectCard(card); }
        public void gamePropertiesSetup(GameState state) { card.gamePropertiesSetup(state); }
        public int realEnergyCost() {
            return card.realEnergyCost();
        }
        public Card getBaseCard() {
            return card.getBaseCard();
        }
        public Card wrap(Card newCard) {
            return new CardTmpChangeCost(card.wrap(newCard), energyCost);
        }
        public Card getUpgrade() {
            var upgrade = card.getUpgrade();
            if (upgrade == null) {
                return null;
            }
            if (upgrade.energyCost == 0) {
                return null;
            }
            return new CardTmpChangeCost(upgrade, 0);
        }
    }

    public static class CardTmpUntilPlayedCost extends Card {
        public final Card card;
        private int cardIdx;

        public CardTmpUntilPlayedCost(Card card, int energyCost) {
            super(card.cardName + " (Tmp Until Played " + energyCost + ")", card.cardType, energyCost, card.rarity);
            this.card = card;
            ethereal = card.ethereal;
            innate = card.innate;
            exhaustWhenPlayed = card.exhaustWhenPlayed;
            exhaustNonAttacks = card.exhaustNonAttacks;
            returnToDeckWhenPlay = card.returnToDeckWhenPlay;
            retain = card.retain;
            selectEnemy = card.selectEnemy;
            selectFromDiscard = card.selectFromDiscard;
            selectFromExhaust = card.selectFromExhaust;
            selectFromDeck = card.selectFromDeck;
            selectFromHand = card.selectFromHand;
            selectFromDiscardLater = card.selectFromDiscardLater;
            selectFromHandLater = card.selectFromHandLater;
            exhaustSkill = card.exhaustSkill;
            canExhaustAnyCard = card.canExhaustAnyCard;
            changePlayerStrength = card.changePlayerStrength;
            changePlayerStrengthEot = card.changePlayerStrengthEot;
            changePlayerDexterity = card.changePlayerDexterity;
            changePlayerFocus = card.changePlayerFocus;
            changePlayerArtifact = card.changePlayerArtifact;
            changePlayerVulnerable = card.changePlayerVulnerable;
            vulnEnemy = card.vulnEnemy;
            weakEnemy = card.weakEnemy;
            chokeEnemy = card.chokeEnemy;
            affectEnemyStrength = card.affectEnemyStrength;
            affectEnemyStrengthEot = card.affectEnemyStrengthEot;
            putCardOnTopDeck = card.putCardOnTopDeck;
            healPlayer = card.healPlayer;
        }

        public void setCounterIdx(GameProperties gameProperties, int idx) {
            card.setCounterIdx(gameProperties, idx);
        }

        public int getCounterIdx(GameProperties gameProperties) {
            return card.getCounterIdx(gameProperties);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) { return card.play(state, idx, energyUsed); }
        public void onExhaust(GameState state) { card.onExhaust(state); }
        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) { return card.getPossibleGeneratedCards(properties, cards); }
        public List<Card> getPossibleTransformTmpCostCards(List<Card> cards) { return card.getPossibleTransformTmpCostCards(cards); }
        public int onPlayTransformCardIdx(GameProperties prop) {
            int idx = card.onPlayTransformCardIdx(prop);
            return idx == -1 ? cardIdx : idx;
        }
        public boolean canSelectCard(Card card2) { return card.canSelectCard(card); }
        public void gamePropertiesSetup(GameState state) {
            card.gamePropertiesSetup(state);
            cardIdx = state.properties.findCardIndex(card);
        }
        public int realEnergyCost() {
            return card.realEnergyCost();
        }
        public Card getBaseCard() {
            return card.getBaseCard();
        }
        public Card wrap(Card newCard) {
            return new CardTmpUntilPlayedCost(card.wrap(newCard), energyCost);
        }
        public Card getUpgrade() {
            var upgrade = card.getUpgrade();
            if (upgrade == null) {
                return null;
            }
            if (upgrade.energyCost == 0) {
                return null;
            }
            return new CardTmpChangeCost(upgrade, 0);
        }
    }

    public static class CardPermChangeCost extends Card {
        public final Card card;

        public CardPermChangeCost(Card card, int energyCost) {
            super(card.cardName + " (Perm " + energyCost + ")", card.cardType, energyCost, card.rarity);
            this.card = card;
            ethereal = card.ethereal;
            innate = card.innate;
            exhaustWhenPlayed = card.exhaustWhenPlayed;
            exhaustNonAttacks = card.exhaustNonAttacks;
            returnToDeckWhenPlay = card.returnToDeckWhenPlay;
            retain = card.retain;
            selectEnemy = card.selectEnemy;
            selectFromDiscard = card.selectFromDiscard;
            selectFromExhaust = card.selectFromExhaust;
            selectFromDeck = card.selectFromDeck;
            selectFromHand = card.selectFromHand;
            selectFromDiscardLater = card.selectFromDiscardLater;
            selectFromHandLater = card.selectFromHandLater;
            exhaustSkill = card.exhaustSkill;
            canExhaustAnyCard = card.canExhaustAnyCard;
            changePlayerStrength = card.changePlayerStrength;
            changePlayerStrengthEot = card.changePlayerStrengthEot;
            changePlayerDexterity = card.changePlayerDexterity;
            changePlayerFocus = card.changePlayerFocus;
            changePlayerArtifact = card.changePlayerArtifact;
            changePlayerVulnerable = card.changePlayerVulnerable;
            vulnEnemy = card.vulnEnemy;
            weakEnemy = card.weakEnemy;
            chokeEnemy = card.chokeEnemy;
            affectEnemyStrength = card.affectEnemyStrength;
            affectEnemyStrengthEot = card.affectEnemyStrengthEot;
            putCardOnTopDeck = card.putCardOnTopDeck;
            healPlayer = card.healPlayer;
        }

        public void setCounterIdx(GameProperties gameProperties, int idx) {
            card.setCounterIdx(gameProperties, idx);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) { return card.play(state, idx, energyUsed); }
        public void onExhaust(GameState state) { card.onExhaust(state); }
        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) { return card.getPossibleGeneratedCards(properties, cards); }
        public List<Card> getPossibleTransformTmpCostCards(List<Card> cards) { return card.getPossibleTransformTmpCostCards(cards); }
        public int onPlayTransformCardIdx(GameProperties prop) { return card.onPlayTransformCardIdx(prop); }
        public boolean canSelectCard(Card card2) { return card.canSelectCard(card); }
        public void gamePropertiesSetup(GameState state) { card.gamePropertiesSetup(state); }
        public Card getBaseCard() {
            return card.getBaseCard();
        }
        public Card wrap(Card newCard) {
            return new CardPermChangeCost(card.wrap(newCard), energyCost);
        }
        public Card getUpgrade() {
            var upgrade = card.getUpgrade();
            if (upgrade == null) {
                return null;
            }
            if (upgrade.energyCost == energyCost || (upgrade.energyCost < energyCost && card.energyCost < upgrade.energyCost)) {
                return upgrade;
            }
            return new CardPermChangeCost(upgrade, energyCost);
        }
    }

    public static class CardTmpRetain extends Card {
        public final Card card;
        private int cardIdx;

        public CardTmpRetain(Card card) {
            super(card.cardName + " (Tmp Retain)", card.cardType, card.energyCost, card.rarity);
            this.card = card;
            ethereal = card.ethereal;
            innate = card.innate;
            exhaustWhenPlayed = card.exhaustWhenPlayed;
            exhaustNonAttacks = card.exhaustNonAttacks;
            returnToDeckWhenPlay = card.returnToDeckWhenPlay;
            retain = true; // This is the key - force retain to true, but only temporarily
            selectEnemy = card.selectEnemy;
            selectFromDiscard = card.selectFromDiscard;
            selectFromExhaust = card.selectFromExhaust;
            selectFromDeck = card.selectFromDeck;
            selectFromHand = card.selectFromHand;
            selectFromDiscardLater = card.selectFromDiscardLater;
            selectFromHandLater = card.selectFromHandLater;
            exhaustSkill = card.exhaustSkill;
            canExhaustAnyCard = card.canExhaustAnyCard;
            changePlayerStrength = card.changePlayerStrength;
            changePlayerStrengthEot = card.changePlayerStrengthEot;
            changePlayerDexterity = card.changePlayerDexterity;
            changePlayerFocus = card.changePlayerFocus;
            changePlayerArtifact = card.changePlayerArtifact;
            changePlayerVulnerable = card.changePlayerVulnerable;
            vulnEnemy = card.vulnEnemy;
            weakEnemy = card.weakEnemy;
            chokeEnemy = card.chokeEnemy;
            affectEnemyStrength = card.affectEnemyStrength;
            affectEnemyStrengthEot = card.affectEnemyStrengthEot;
            putCardOnTopDeck = card.putCardOnTopDeck;
            healPlayer = card.healPlayer;
        }
        public void setCounterIdx(GameProperties gameProperties, int idx) {
            card.setCounterIdx(gameProperties, idx);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) { return card.play(state, idx, energyUsed); }
        public void onExhaust(GameState state) { card.onExhaust(state); }
        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) { return card.getPossibleGeneratedCards(properties, cards); }
        public List<Card> getPossibleTransformTmpCostCards(List<Card> cards) { return card.getPossibleTransformTmpCostCards(cards); }
        public int onPlayTransformCardIdx(GameProperties prop) {
            int idx = card.onPlayTransformCardIdx(prop);
            return idx == -1 ? cardIdx : idx;
        }
        public boolean canSelectCard(Card card2) { return card.canSelectCard(card); }
        public void gamePropertiesSetup(GameState state) {
            card.gamePropertiesSetup(state);
            cardIdx = state.properties.findCardIndex(card);
        }
        public int realEnergyCost() {
            return card.realEnergyCost();
        }
        public Card getBaseCard() {
            return card.getBaseCard();
        }
        public Card wrap(Card newCard) {
            return new CardTmpRetain(card.wrap(newCard));
        }
        public Card getUpgrade() {
            var upgrade = card.getUpgrade();
            if (upgrade == null) {
                return null;
            }
            return new CardTmpRetain(upgrade);
        }
    }

    public static class Strike extends Card {
        public Strike() {
            super("Strike", Card.ATTACK, 1, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 6 + (state.properties.hasStrikeDummy && state.properties.getRelic(Relic.StrikeDummy.class).isRelicEnabledInScenario(state.preBattleScenariosChosenIdx) ? 3 : 0));
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class StrikeP extends Card {
        public StrikeP() {
            super("Strike+", Card.ATTACK, 1, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 9 + (state.properties.hasStrikeDummy && state.properties.getRelic(Relic.StrikeDummy.class).isRelicEnabledInScenario(state.preBattleScenariosChosenIdx) ? 3 : 0));
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Defend extends Card {
        public Defend() {
            super("Defend", Card.SKILL, 1,  Card.COMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(5);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DefendP extends Card {
        public DefendP() {
            super("Defend+", Card.SKILL, 1, Card.COMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(8);
            return GameActionCtx.PLAY_CARD;
        }
    }
}
