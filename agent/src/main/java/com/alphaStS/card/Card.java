package com.alphaStS.card;

import com.alphaStS.EntityProperty;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.GameProperties;
import com.alphaStS.GameState;
import com.alphaStS.GameStateUtils;

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
    public int starCost = 0;
    public final int rarity;
    public boolean ethereal = false;
    public boolean innate = false;
    public boolean exhaustWhenPlayed = false;
    public boolean exhaustNonAttacks = false;
    public boolean alwaysDiscard = false;
    public boolean returnToDeckWhenPlay = false;
    public boolean returnToTopOfDeckWhenPlay = false;
    public boolean returnToHandWhenPlay = false;
    public boolean retain = false;
    public boolean delayUseEnergy;
    public boolean isXCost;
    public boolean selectFromDiscardLater;
    public boolean selectFromHandLater;
    public boolean exhaustSkill;
    public boolean canExhaustAnyCard;
    public boolean discardNonAttack;
    public boolean canDiscardAnyCard;
    public EntityProperty entityProperty = new EntityProperty();
    public boolean needsLastCardType;
    public boolean healPlayer;
    public boolean scry;
    public boolean select1OutOf3CardEffectCard;
    public int generatedCardIdx = -1; // when getPossibleGeneratedCards return 1 card, this is the card index for it
    public int[] generatedCardIdxes; // when getPossibleGeneratedCards returns non-empty list, this is the card indexes for each card in the order of the list
    public int[] generatedCardReverseIdxes; // given a cardIdx, return the index of it in generatedCardIdxes (-1 otherwise)
    int counterIdx = -1;
    int vExtraIdx = -1;

    public void setCounterIdx(GameProperties gameProperties, int idx) {
        counterIdx = idx;
    }

    public int getCounterIdx(GameProperties gameProperties) {
        return counterIdx;
    }

    public void setVExtraIdx(GameProperties gameProperties, int idx) {
        vExtraIdx = idx;
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

    public boolean retain() {
        return retain;
    }

    public boolean isTmpModifiedCard() {
        return false;
    }

    public GameActionCtx play(GameState state, int idx, int energyUsed) { return GameActionCtx.PLAY_CARD; }
    public void onExhaust(GameState state) {}
    public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) { return List.of(); }
    public List<Card> getPossibleSelect1OutOf3Cards(GameProperties gameProperties) { return List.of(); }
    public List<Card> getPossibleTransformTmpCostCards(List<Card> cards) { return List.of(); }
    public int onPlayTransformCardIdx(GameProperties prop, int cardIdx) { return -1; }
    public boolean canSelectCard(Card card) { return true; }

    public void setupGeneratedCardIndexes(GameProperties properties) {
        List<Card> possibleCards = getPossibleGeneratedCards(properties, List.of(properties.cardDict));
        var result = GameStateUtils.setupGeneratedCardIndexes(possibleCards, properties);
        generatedCardIdx = result.v1();
        generatedCardIdxes = result.v2();
        generatedCardReverseIdxes = result.v3();
    }

    public void gamePropertiesSetup(GameState state) {}
    public void onDiscard(GameState state) {}
    public void onDiscardEndOfTurn(GameState state, int numCardsInHand) {}
    public Card getUpgrade() { return CardUpgrade.map.get(this); }

    public Card getTemporaryCostIfPossible(int temporaryCost) {
        if (energyCost <= temporaryCost || isXCost) {
            return this;
        }
        var mod = new CardModification();
        mod.tmpChangeCost = temporaryCost;
        return new Card.CardWrapper(this, mod);
    }

    public Card getTmpRetainIfPossible() {
        if (retain) {
            return this;
        }
        var mod = new CardModification();
        mod.tmpRetain = true;
        return new Card.CardWrapper(this, mod);
    }

    public Card getTemporaryCostUntilPlayedIfPossible(int temporaryCost) {
        if (energyCost <= temporaryCost || isXCost) {
            return this;
        }
        var mod = new CardModification();
        mod.tmpUntilPlayedCost = temporaryCost;
        return new Card.CardWrapper(this, mod);
    }

    public Card getPermCostIfPossible(int permCost) {
        if (energyCost < 0 || energyCost == permCost || isXCost) {
            return this;
        }
        var mod = new CardModification();
        mod.permChangeCost = permCost;
        return new Card.CardWrapper(this, mod);
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

    public Card wrapAfterPlay(Card card) {
        return card;
    }

    public static class CardModification {
        public int tmpChangeCost = -1;
        public int tmpUntilPlayedCost = -1;
        public int permChangeCost = -1;
        public boolean tmpRetain = false;

        public CardModification clone() {
            var copy = new CardModification();
            copy.tmpChangeCost = tmpChangeCost;
            copy.tmpUntilPlayedCost = tmpUntilPlayedCost;
            copy.permChangeCost = permChangeCost;
            copy.tmpRetain = tmpRetain;
            return copy;
        }
    }

    public static class CardWrapper extends Card {
        private final Card card;
        private final CardModification mod;

        public CardWrapper(Card card, CardModification mod) {
            super(generateCardName(card, mod), card.cardType, getEffectiveEnergyCost(card, mod), card.rarity);
            this.card = card;
            this.mod = mod;
            copyCardProperties(card);
        }

        private static int getEffectiveEnergyCost(Card card, CardModification mod) {
            if (mod.tmpChangeCost != -1) return mod.tmpChangeCost;
            if (mod.tmpUntilPlayedCost != -1) return mod.tmpUntilPlayedCost;
            if (mod.permChangeCost != -1) return mod.permChangeCost;
            return card.energyCost;
        }

        private static String generateCardName(Card card, CardModification mod) {
            StringBuilder sb = new StringBuilder(card.cardName);
            if (mod.tmpChangeCost != -1) {
                sb.append(" (Tmp ").append(mod.tmpChangeCost).append(")");
            }
            if (mod.tmpUntilPlayedCost != -1) {
                sb.append(" (Tmp Until Played ").append(mod.tmpUntilPlayedCost).append(")");
            }
            if (mod.permChangeCost != -1) {
                sb.append(" (Perm ").append(mod.permChangeCost).append(")");
            }
            if (mod.tmpRetain) {
                sb.append(" (Tmp Retain)");
            }
            return sb.toString();
        }

        private void copyCardProperties(Card card) {
            ethereal = card.ethereal;
            innate = card.innate;
            exhaustWhenPlayed = card.exhaustWhenPlayed;
            exhaustNonAttacks = card.exhaustNonAttacks;
            alwaysDiscard = card.alwaysDiscard;
            returnToDeckWhenPlay = card.returnToDeckWhenPlay;
            returnToTopOfDeckWhenPlay = card.returnToTopOfDeckWhenPlay;
            returnToHandWhenPlay = card.returnToHandWhenPlay;
            retain = card.retain;
            delayUseEnergy = card.delayUseEnergy;
            isXCost = card.isXCost;
            selectFromDiscardLater = card.selectFromDiscardLater;
            selectFromHandLater = card.selectFromHandLater;
            exhaustSkill = card.exhaustSkill;
            canExhaustAnyCard = card.canExhaustAnyCard;
            discardNonAttack = card.discardNonAttack;
            canDiscardAnyCard = card.canDiscardAnyCard;
            entityProperty = card.entityProperty;
            needsLastCardType = card.needsLastCardType;
            healPlayer = card.healPlayer;
            scry = card.scry;
            select1OutOf3CardEffectCard = card.select1OutOf3CardEffectCard;
        }

        @Override
        public void setCounterIdx(GameProperties gameProperties, int idx) {
            card.setCounterIdx(gameProperties, idx);
        }

        @Override
        public int getCounterIdx(GameProperties gameProperties) {
            return card.getCounterIdx(gameProperties);
        }

        @Override
        public void setVExtraIdx(GameProperties gameProperties, int idx) {
            card.setVExtraIdx(gameProperties, idx);
        }

        @Override
        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            return card.play(state, idx, energyUsed);
        }

        @Override
        public void onExhaust(GameState state) {
            card.onExhaust(state);
        }

        @Override
        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return card.getPossibleGeneratedCards(properties, cards);
        }

        @Override
        public List<Card> getPossibleSelect1OutOf3Cards(GameProperties gameProperties) {
            return card.getPossibleSelect1OutOf3Cards(gameProperties);
        }

        @Override
        public List<Card> getPossibleTransformTmpCostCards(List<Card> cards) {
            return card.getPossibleTransformTmpCostCards(cards);
        }

        @Override
        public int onPlayTransformCardIdx(GameProperties prop, int cardIdx) {
            if (mod.tmpUntilPlayedCost != -1 || mod.tmpRetain) {
                int idx = card.onPlayTransformCardIdx(prop, cardIdx);
                return idx == -1 ? cardIdx : idx;
            }
            return card.onPlayTransformCardIdx(prop, cardIdx);
        }

        @Override
        public boolean canSelectCard(Card card2) {
            return card.canSelectCard(card2);
        }

        @Override
        public void setupGeneratedCardIndexes(GameProperties properties) {
            card.setupGeneratedCardIndexes(properties);
        }

        @Override
        public void gamePropertiesSetup(GameState state) {
            card.gamePropertiesSetup(state);
        }

        @Override
        public int realEnergyCost() {
            return mod.permChangeCost >= 0 ? mod.permChangeCost : card.realEnergyCost();
        }

        @Override
        public int energyCost(GameState state) {
            if (mod.tmpChangeCost != -1) return mod.tmpChangeCost;
            if (mod.tmpUntilPlayedCost != -1) return mod.tmpUntilPlayedCost;
            if (mod.permChangeCost != -1) return mod.permChangeCost;
            return card.energyCost;
        }

        @Override
        public boolean retain() {
            return mod.tmpRetain || card.retain;
        }

        @Override
        public boolean isTmpModifiedCard() {
            return isTmpChangeCost() || isTmpUntilPlayedCost();
        }

        @Override
        public Card getBaseCard() {
            return card.getBaseCard();
        }

        private Card wrap(Card newCard, CardModification newMod) {
            var resultMod = new CardModification();
            var modified = false;
            var cardEnergyCost = newCard.energyCost;
            if (newMod.permChangeCost >= 0 && cardEnergyCost != newMod.permChangeCost) {
                cardEnergyCost = newMod.permChangeCost;
                var permCard = newCard.getPermCostIfPossible(newMod.permChangeCost);
                if (permCard instanceof CardWrapper) {
                    resultMod.permChangeCost = newMod.permChangeCost;
                    modified = true;
                } else {
                    newCard = permCard;
                }
            }
            if (newMod.tmpChangeCost >= 0 && !(cardEnergyCost <= newMod.tmpChangeCost || newCard.isXCost)) {
                resultMod.tmpChangeCost = newMod.tmpChangeCost;
                modified = true;
            }
            if (newMod.tmpUntilPlayedCost >= 0 && !(cardEnergyCost <= newMod.tmpUntilPlayedCost || newCard.isXCost)) {
                resultMod.tmpUntilPlayedCost = newMod.tmpUntilPlayedCost;
                modified = true;
            }
            if (!newCard.retain && newMod.tmpRetain) {
                resultMod.tmpRetain = true;
                modified = true;
            }
            if (!modified) {
                return newCard;
            }
            return new CardWrapper(newCard, resultMod);
        }

        @Override
        public Card wrap(Card newCard) {
            return wrap(newCard, mod);
        }

        @Override
        public Card wrapAfterPlay(Card newCard) {
            var newMod = new CardModification();
            newMod.permChangeCost = mod.permChangeCost;
            return wrap(newCard, newMod);
        }

        @Override
        public Card getPermCostIfPossible(int permCost) {
            var newMod = mod.clone();
            newMod.permChangeCost = permCost;
            return wrap(card, newMod);
        }

        @Override
        public Card getTemporaryCostUntilPlayedIfPossible(int newCost) {
            var newMod = mod.clone();
            newMod.tmpUntilPlayedCost = newCost;
            return wrap(card, newMod);
        }

        @Override
        public Card getTemporaryCostIfPossible(int newCost) {
            var newMod = mod.clone();
            newMod.tmpChangeCost = newCost;
            return wrap(card, newMod);
        }

        @Override
        public Card getTmpRetainIfPossible() {
            var newMod = mod.clone();
            newMod.tmpRetain = true;
            return wrap(card, newMod);
        }

        public boolean isTmpChangeCost() {
            return mod.tmpChangeCost != -1;
        }

        public boolean isTmpUntilPlayedCost() {
            return mod.tmpUntilPlayedCost != -1;
        }

        public boolean isPermChangeCost() {
            return mod.permChangeCost != -1;
        }

        public boolean isTmpRetain() {
            return mod.tmpRetain;
        }

        @Override
        public void onDiscard(GameState state) {
            card.onDiscard(state);
        }

        @Override
        public void onDiscardEndOfTurn(GameState state, int numCardsInHand) {
            card.onDiscardEndOfTurn(state, numCardsInHand);
        }

        @Override
        public Card getUpgrade() {
            var upgrade = card.getUpgrade();
            if (upgrade == null) {
                return null;
            }
            var newMod = mod.clone();
            if (upgrade.energyCost < mod.permChangeCost && card.energyCost < upgrade.energyCost) {
                newMod.permChangeCost = -1;
            }
            return wrap(upgrade, newMod);
        }
    }

    public static class Strike extends Card {
        public Strike() {
            super("Strike", Card.ATTACK, 1, Card.COMMON);
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 6, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class StrikeP extends Card {
        public StrikeP() {
            super("Strike+", Card.ATTACK, 1, Card.COMMON);
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 9, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Defend extends Card {
        public Defend() {
            super("Defend", Card.SKILL, 1,  Card.COMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int bonus = state.properties.fastenCounterIdx >= 0 ? state.getCounterForRead()[state.properties.fastenCounterIdx] : 0;
            state.playerGainBlock(5 + bonus);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DefendP extends Card {
        public DefendP() {
            super("Defend+", Card.SKILL, 1, Card.COMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int bonus = state.properties.fastenCounterIdx >= 0 ? state.getCounterForRead()[state.properties.fastenCounterIdx] : 0;
            state.playerGainBlock(8 + bonus);
            return GameActionCtx.PLAY_CARD;
        }
    }
}
