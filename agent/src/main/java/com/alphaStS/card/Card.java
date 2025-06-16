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
    public boolean vulnEnemy;
    public boolean weakEnemy;
    public boolean chokeEnemy;
    public boolean poisonEnemy;
    public boolean corpseExplosionEnemy;
    public boolean affectEnemyStrength;
    public boolean affectEnemyStrengthEot;
    public boolean putCardOnTopDeck;
    public boolean healPlayer;
    public boolean scry;
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
    public List<Card> getPossibleGeneratedCards(List<Card> cards) { return List.of(); }
    public List<Card> getPossibleTransformTmpCostCards(List<Card> cards) { return List.of(); }
    public int onPlayTransformCardIdx(GameProperties prop) { return -1; }
    public boolean canSelectCard(Card card) { return true; }
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

    public static class CardTmpChangeCost extends Card {
        public final Card card;

        public CardTmpChangeCost(Card card, int energyCost) {
            super(card.cardName + " (Tmp " + energyCost + ")", card.cardType, energyCost, card.rarity);
            this.card = card;
            ethereal = card.ethereal;
            innate = card.innate;
            exhaustWhenPlayed = card.exhaustWhenPlayed;
            exhaustNonAttacks = card.exhaustNonAttacks;
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
        public List<Card> getPossibleGeneratedCards(List<Card> cards) { return card.getPossibleGeneratedCards(cards); }
        public List<Card> getPossibleTransformTmpCostCards(List<Card> cards) { return card.getPossibleTransformTmpCostCards(cards); }
        public int onPlayTransformCardIdx(GameProperties prop) { return card.onPlayTransformCardIdx(prop); }
        public boolean canSelectCard(Card card2) { return card.canSelectCard(card); }
        public void gamePropertiesSetup(GameState state) { card.gamePropertiesSetup(state); }
        public int realEnergyCost() {
            return card.realEnergyCost();
        }
        public Card getUpgrade() {
            var upgrade = card.getUpgrade();
            if (upgrade == null) {
                return null;
            }
            if (upgrade.energyCost == 0) {
                return null;
            }
            // todo BloodForBlood + Tmp
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
        public List<Card> getPossibleGeneratedCards(List<Card> cards) { return card.getPossibleGeneratedCards(cards); }
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
        public Card getUpgrade() {
            var upgrade = card.getUpgrade();
            if (upgrade == null) {
                return null;
            }
            if (upgrade.energyCost == 0) {
                return null;
            }
            // todo BloodForBlood + Tmp
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
        public List<Card> getPossibleGeneratedCards(List<Card> cards) { return card.getPossibleGeneratedCards(cards); }
        public List<Card> getPossibleTransformTmpCostCards(List<Card> cards) { return card.getPossibleTransformTmpCostCards(cards); }
        public int onPlayTransformCardIdx(GameProperties prop) { return card.onPlayTransformCardIdx(prop); }
        public boolean canSelectCard(Card card2) { return card.canSelectCard(card); }
        public void gamePropertiesSetup(GameState state) { card.gamePropertiesSetup(state); }
        public Card getUpgrade() {
            var upgrade = card.getUpgrade();
            if (upgrade == null) {
                return null;
            }
            if (upgrade.energyCost == energyCost || (upgrade.energyCost < energyCost && card.energyCost < upgrade.energyCost)) {
                return upgrade;
            }
            // todo BloodForBlood + Perm
            return new CardPermChangeCost(upgrade, energyCost);
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
