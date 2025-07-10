package com.alphaStS.card;

import com.alphaStS.GameActionCtx;
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
        return new Card.CardWrapper(this, temporaryCost, -1, -1, false);
    }

    public Card getTmpRetainIfPossible() {
        if (retain) {
            return this;
        }
        return new Card.CardWrapper(this, -1, -1, -1, true);
    }

    public Card getTemporaryCostUntilPlayedIfPossible(int temporaryCost) {
        if (energyCost <= temporaryCost || isXCost) {
            return this;
        }
        return new Card.CardWrapper(this, -1, temporaryCost, -1, false);
    }

    public Card getPermCostIfPossible(int permCost) {
        if (energyCost < 0 || energyCost == permCost || isXCost) {
            return this;
        }
        return new Card.CardWrapper(this, -1, -1, permCost, false);
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

    public static class CardWrapper extends Card {
        private final Card card;
        private final int tmpChangeCost;
        private final int tmpUntilPlayedCost;
        private final int permChangeCost;
        private final boolean tmpRetain;
        private int cardIdx;

        public CardWrapper(Card card, int tmpChangeCost, int tmpUntilPlayedCost, int permChangeCost, boolean tmpRetain) {
            super(generateCardName(card, tmpChangeCost, tmpUntilPlayedCost, permChangeCost, tmpRetain), card.cardType, getEffectiveEnergyCost(card, tmpChangeCost, tmpUntilPlayedCost, permChangeCost), card.rarity);
            this.card = card;
            this.tmpChangeCost = tmpChangeCost;
            this.tmpUntilPlayedCost = tmpUntilPlayedCost;
            this.permChangeCost = permChangeCost;
            this.tmpRetain = tmpRetain;
            copyCardProperties(card);
        }

        private static int getEffectiveEnergyCost(Card card, int tmpChangeCost, int tmpUntilPlayedCost, int permChangeCost) {
            if (tmpChangeCost != -1) return tmpChangeCost;
            if (tmpUntilPlayedCost != -1) return tmpUntilPlayedCost;
            if (permChangeCost != -1) return permChangeCost;
            return card.energyCost;
        }

        private static String generateCardName(Card card, int tmpChangeCost, int tmpUntilPlayedCost, int permChangeCost, boolean tmpRetain) {
            StringBuilder sb = new StringBuilder(card.cardName);
            if (tmpChangeCost != -1) {
                sb.append(" (Tmp ").append(tmpChangeCost).append(")");
            }
            if (tmpUntilPlayedCost != -1) {
                sb.append(" (Tmp Until Played ").append(tmpUntilPlayedCost).append(")");
            }
            if (permChangeCost != -1) {
                sb.append(" (Perm ").append(permChangeCost).append(")");
            }
            if (tmpRetain) {
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
            retain = card.retain;
            selectEnemy = card.selectEnemy;
            delayUseEnergy = card.delayUseEnergy;
            selectFromDiscard = card.selectFromDiscard;
            selectFromExhaust = card.selectFromExhaust;
            selectFromDeck = card.selectFromDeck;
            selectFromHand = card.selectFromHand;
            isXCost = card.isXCost;
            selectFromDiscardLater = card.selectFromDiscardLater;
            selectFromHandLater = card.selectFromHandLater;
            exhaustSkill = card.exhaustSkill;
            canExhaustAnyCard = card.canExhaustAnyCard;
            discardNonAttack = card.discardNonAttack;
            canDiscardAnyCard = card.canDiscardAnyCard;
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
            if (tmpUntilPlayedCost != -1 || tmpRetain) {
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
            if (tmpUntilPlayedCost != -1 || tmpRetain) {
                cardIdx = state.properties.findCardIndex(card);
            }
        }

        @Override
        public int realEnergyCost() {
            return permChangeCost >= 0 ? permChangeCost : card.realEnergyCost();
        }

        @Override
        public int energyCost(GameState state) {
            return permChangeCost >= 0 ? permChangeCost : card.energyCost(state);
        }

        @Override
        public boolean retain() {
            return tmpRetain || card.retain;
        }

        @Override
        public boolean isTmpModifiedCard() {
            return isTmpChangeCost() || isTmpUntilPlayedCost();
        }

        @Override
        public Card getBaseCard() {
            return card.getBaseCard();
        }

        private Card wrap(Card newCard, int tmpChangeCost, int tmpUntilPlayedCost, int permChangeCost, boolean tmpRetain) {
            int newTmpChangeCost = -1;
            int newTmpUntilPlayedCost = -1;
            int newPermChangeCost = -1;
            boolean newTmpRetain = false;
            var modified = false;
            var cardEnergyCost = newCard.energyCost;
            if (permChangeCost >= 0 && cardEnergyCost != permChangeCost) {
                cardEnergyCost = permChangeCost;
                var permCard = newCard.getPermCostIfPossible(permChangeCost);
                if (permCard instanceof CardWrapper) {
                    newPermChangeCost = permChangeCost;
                    modified = true;
                } else {
                    newCard = permCard;
                }
            }
            if (tmpChangeCost >= 0 && !(cardEnergyCost <= tmpChangeCost || newCard.isXCost)) {
                newTmpChangeCost = tmpChangeCost;
                modified = true;
            }
            if (tmpUntilPlayedCost >= 0 && !(cardEnergyCost <= tmpUntilPlayedCost || newCard.isXCost)) {
                newTmpUntilPlayedCost = tmpUntilPlayedCost;
                modified = true;
            }
            if (!newCard.retain && tmpRetain) {
                newTmpRetain = true;
                modified = true;
            }
            if (!modified) {
                return newCard;
            }
            return new CardWrapper(newCard, newTmpChangeCost, newTmpUntilPlayedCost, newPermChangeCost, newTmpRetain);
        }

        @Override
        public Card wrap(Card newCard) {
            return wrap(newCard, tmpChangeCost, tmpUntilPlayedCost, permChangeCost, tmpRetain);
        }

        @Override
        public Card wrapAfterPlay(Card newCard) {
            return wrap(newCard, -1, -1, permChangeCost, false);
        }

        @Override
        public Card getPermCostIfPossible(int permCost) {
            return wrap(card, tmpChangeCost, tmpUntilPlayedCost, permCost, tmpRetain);
        }

        @Override
        public Card getTemporaryCostUntilPlayedIfPossible(int newCost) {
            return wrap(card, tmpChangeCost, newCost, permChangeCost, tmpRetain);
        }

        @Override
        public Card getTemporaryCostIfPossible(int newCost) {
            return wrap(card, newCost, tmpUntilPlayedCost, permChangeCost, tmpRetain);
        }

        @Override
        public Card getTmpRetainIfPossible() {
            return wrap(card, tmpChangeCost, tmpUntilPlayedCost, permChangeCost, true);
        }

        public boolean isTmpChangeCost() {
            return tmpChangeCost != -1;
        }

        public boolean isTmpUntilPlayedCost() {
            return tmpUntilPlayedCost != -1;
        }

        public boolean isPermChangeCost() {
            return permChangeCost != -1;
        }

        public boolean isTmpRetain() {
            return tmpRetain;
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
            int newPermCost = permChangeCost;
            if (upgrade.energyCost < permChangeCost && card.energyCost < upgrade.energyCost) {
                newPermCost = -1;
            }
            return wrap(upgrade, tmpChangeCost, tmpUntilPlayedCost, newPermCost, tmpRetain);
        }
    }

    public static class Strike extends Card {
        public Strike() {
            super("Strike", Card.ATTACK, 1, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 6 + (state.properties.strikeDummy != null && state.properties.strikeDummy.isRelicEnabledInScenario(state) ? 3 : 0));
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class StrikeP extends Card {
        public StrikeP() {
            super("Strike+", Card.ATTACK, 1, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 9 + (state.properties.strikeDummy != null && state.properties.strikeDummy.isRelicEnabledInScenario(state) ? 3 : 0));
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
