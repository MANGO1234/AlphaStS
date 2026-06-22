package com.alphaStS.card;

import com.alphaStS.EntityProperty;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.GameProperties;
import com.alphaStS.GameState;
import com.alphaStS.GameStateUtils;

import java.util.ArrayList;
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

    public boolean innate() {
        return innate;
    }

    public boolean retain() {
        return retain;
    }

    public boolean ethereal() {
        return ethereal;
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
        if ((energyCost <= temporaryCost && !(temporaryCost == 0 && starCost > 0)) || isXCost) {
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

    public Card getPermEthereal() {
        var mod = new CardModification();
        mod.permEthereal = true;
        return new Card.CardWrapper(this, mod);
    }

    public Card getPermRetain() {
        var mod = new CardModification();
        mod.permRetain = true;
        return new Card.CardWrapper(this, mod);
    }

    public Card getTemporaryCostUntilPlayedIfPossible(int temporaryCost) {
        if ((energyCost <= temporaryCost && !(temporaryCost == 0 && starCost > 0)) || isXCost) {
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

    public Card enchantAdroit(int adroit) {
        var mod = new CardModification();
        mod.adroit = adroit;
        return new CardWrapper(this, mod);
    }

    public Card enchantCorrupted() {
        var mod = new CardModification();
        mod.corrupted = true;
        return new CardWrapper(this, mod);
    }

    public Card enchantGlam() {
        var mod = new CardModification();
        mod.glam = true;
        return new CardWrapper(this, mod);
    }

    public Card enchantInky() {
        var mod = new CardModification();
        mod.inky = true;
        return new CardWrapper(this, mod);
    }

    public Card enchantInstinct() {
        var mod = new CardModification();
        mod.instinct = true;
        return new CardWrapper(this, mod);
    }

    public Card enchantNimble(int nimble) {
        var mod = new CardModification();
        mod.nimble = nimble;
        return new CardWrapper(this, mod);
    }

    public Card enchantPerfectFit() {
        var mod = new CardModification();
        mod.perfectFit = true;
        return new CardWrapper(this, mod);
    }

    public Card enchantRoyallyApproved() {
        var mod = new CardModification();
        mod.royallyApproved = true;
        return new CardWrapper(this, mod);
    }

    public Card enchantSharp(int sharp) {
        var mod = new CardModification();
        mod.sharp = sharp;
        return new CardWrapper(this, mod);
    }

    public Card enchantSoulsPower() {
        var mod = new CardModification();
        mod.soulsPower = true;
        return new CardWrapper(this, mod);
    }

    public Card enchantSpiral(int spiral) {
        var mod = new CardModification();
        mod.spiral = spiral;
        return new CardWrapper(this, mod);
    }

    public Card enchantSteady() {
        var mod = new CardModification();
        mod.steady = true;
        return new CardWrapper(this, mod);
    }

    public Card enchantSwift(int swift) {
        var mod = new CardModification();
        mod.swift = swift;
        return new CardWrapper(this, mod);
    }

    public Card enchantSown() {
        var mod = new CardModification();
        mod.sown = true;
        return new CardWrapper(this, mod);
    }

    public Card enchantVigorous(int vigorous) {
        var mod = new CardModification();
        mod.vigorous = vigorous;
        return new CardWrapper(this, mod);
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
        public boolean permEthereal = false;
        public boolean permRetain = false;
        public int adroit = 0;
        public boolean corrupted = false;
        public boolean glam = false;
        public boolean inky = false;
        public boolean instinct = false;
        public int nimble = 0;
        public boolean perfectFit = false;
        public boolean royallyApproved = false;
        public int sharp = 0;
        public boolean soulsPower = false;
        public int spiral = 0;
        public boolean steady = false;
        public int swift = 0;
        public boolean sown = false;
        public int vigorous = 0;

        public CardModification clone() {
            var copy = new CardModification();
            copy.tmpChangeCost = tmpChangeCost;
            copy.tmpUntilPlayedCost = tmpUntilPlayedCost;
            copy.permChangeCost = permChangeCost;
            copy.tmpRetain = tmpRetain;
            copy.permEthereal = permEthereal;
            copy.permRetain = permRetain;
            copy.adroit = adroit;
            copy.corrupted = corrupted;
            copy.glam = glam;
            copy.inky = inky;
            copy.instinct = instinct;
            copy.nimble = nimble;
            copy.perfectFit = perfectFit;
            copy.royallyApproved = royallyApproved;
            copy.sharp = sharp;
            copy.soulsPower = soulsPower;
            copy.spiral = spiral;
            copy.steady = steady;
            copy.swift = swift;
            copy.sown = sown;
            copy.vigorous = vigorous;
            return copy;
        }
    }

    public static class CardWrapper extends Card {
        private final Card card;
        public final CardModification mod;
        private int afterFirstPlayTransformIdx = Integer.MIN_VALUE;

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
            if (mod.permEthereal) {
                sb.append(" (Ethereal)");
            }
            if (mod.permRetain) {
                sb.append(" (Retain)");
            }
            if (mod.adroit > 0) {
                sb.append(" (Adroit ").append(mod.adroit).append(")");
            }
            if (mod.corrupted) {
                sb.append(" (Corrupted)");
            }
            if (mod.inky) {
                sb.append(" (Inky)");
            }
            if (mod.instinct) {
                sb.append(" (Instinct)");
            }
            if (mod.royallyApproved) {
                sb.append(" (Royally Approved)");
            }
            if (mod.sharp > 0) {
                sb.append(" (Sharp ").append(mod.sharp).append(")");
            }
            if (mod.swift > 0) {
                sb.append(" (Swift ").append(mod.swift).append(")");
            }
            if (mod.glam) {
                sb.append(" (Glam)");
            }
            if (mod.nimble > 0) {
                sb.append(" (Nimble ").append(mod.nimble).append(")");
            }
            if (mod.perfectFit) {
                sb.append(" (Perfect Fit)");
            }
            if (mod.soulsPower) {
                sb.append(" (Soul's Power)");
            }
            if (mod.spiral > 0) {
                sb.append(" (Spiral ").append(mod.spiral).append(")");
            }
            if (mod.sown) {
                sb.append(" (Sown)");
            }
            if (mod.steady) {
                sb.append(" (Steady)");
            }
            if (mod.vigorous > 0) {
                sb.append(" (Vigorous ").append(mod.vigorous).append(")");
            }
            return sb.toString();
        }

        private void copyCardProperties(Card card) {
            starCost = isFreeToPlay() ? 0 : card.starCost;
            ethereal = card.ethereal();
            innate = card.innate;
            exhaustWhenPlayed = mod.soulsPower ? false : card.exhaustWhenPlayed;
            exhaustNonAttacks = card.exhaustNonAttacks;
            alwaysDiscard = card.alwaysDiscard;
            returnToDeckWhenPlay = card.returnToDeckWhenPlay;
            returnToTopOfDeckWhenPlay = card.returnToTopOfDeckWhenPlay;
            returnToHandWhenPlay = card.returnToHandWhenPlay;
            retain = card.retain();
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

        private boolean isFreeToPlay() {
            return mod.tmpChangeCost == 0 || mod.tmpUntilPlayedCost == 0;
        }

        @Override public boolean ethereal() { return super.ethereal() || mod.permEthereal; }

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
            var result = card.play(state, idx, energyUsed);
            if (mod.adroit > 0) {
                state.playerGainBlock(mod.adroit);
            }
            if (mod.swift > 0) {
                state.draw(mod.swift);
            }
            if (mod.sown) {
                state.gainEnergy(1);
            }
            if (mod.corrupted) {
                state.doNonAttackDamageToPlayer(2, false, this);
            }
            return result;
        }

        @Override
        public void onExhaust(GameState state) {
            card.onExhaust(state);
        }

        @Override
        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            boolean hasTempMods = mod.tmpChangeCost != -1 || mod.tmpUntilPlayedCost != -1
                    || mod.tmpRetain || mod.swift > 0 || mod.sown || mod.glam || mod.vigorous > 0;
            if (hasTempMods) {
                var result = new ArrayList<>(card.getPossibleGeneratedCards(properties, cards));
                result.add(card);
                return result;
            }
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
            if (afterFirstPlayTransformIdx == Integer.MIN_VALUE) {
                afterFirstPlayTransformIdx = card.onPlayTransformCardIdx(prop, cardIdx);
                boolean needsStrip = mod.tmpChangeCost != -1 || mod.tmpUntilPlayedCost != -1
                        || mod.tmpRetain || mod.swift > 0 || mod.sown || mod.glam || mod.vigorous > 0;
                if (needsStrip) {
                    // use the inner-card transform target if available, otherwise the inner card itself
                    Card baseCard = afterFirstPlayTransformIdx >= 0
                            ? prop.cardDict[afterFirstPlayTransformIdx] : card;
                    var newMod = mod.clone();
                    newMod.tmpChangeCost = -1;
                    newMod.tmpUntilPlayedCost = -1;
                    newMod.tmpRetain = false;
                    newMod.swift = 0;
                    newMod.sown = false;
                    newMod.glam = false;
                    newMod.vigorous = 0;
                    afterFirstPlayTransformIdx = prop.findCardIndex(wrap(baseCard, newMod));
                }
            }
            return afterFirstPlayTransformIdx;
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
            if (mod.glam || mod.spiral > 0) {
                state.properties.addOnCardPlayedHandler("Replay", new GameEventCardHandler(GameEventCardHandler.CLONE_CARD_PRIORITY) {
                    @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                        if (cloneSource != null) return;
                        if (!(state.properties.cardDict[cardIdx] instanceof CardWrapper cw)) return;
                        if (!cw.mod.glam && cw.mod.spiral <= 0) return;
                        state.addGameActionToStartOfDeque(curState -> {
                            var action = curState.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                            if (curState.playCard(action, lastIdx, false, CardWrapper.class, false, false, energyUsed, cloneParentLocation)) {
                                curState.runActionsInQueueIfNonEmpty();
                            }
                        });
                    }
                });
            }
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
        public boolean innate() {
            return mod.royallyApproved || card.innate;
        }

        @Override
        public boolean retain() {
            return mod.tmpRetain || mod.steady || mod.royallyApproved || mod.permRetain || card.retain;
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
            if (newMod.adroit > 0) { resultMod.adroit = newMod.adroit; modified = true; }
            if (newMod.corrupted) { resultMod.corrupted = true; modified = true; }
            if (newMod.glam) { resultMod.glam = true; modified = true; }
            if (newMod.inky) { resultMod.inky = true; modified = true; }
            if (newMod.instinct) { resultMod.instinct = true; modified = true; }
            if (newMod.nimble > 0) { resultMod.nimble = newMod.nimble; modified = true; }
            if (newMod.perfectFit) { resultMod.perfectFit = true; modified = true; }
            if (!newCard.retain && newMod.royallyApproved) { resultMod.royallyApproved = true; modified = true; }
            if (newMod.sharp > 0) { resultMod.sharp = newMod.sharp; modified = true; }
            if (newMod.soulsPower) { resultMod.soulsPower = true; modified = true; }
            if (newMod.spiral > 0) { resultMod.spiral = newMod.spiral; modified = true; }
            if (!newCard.retain && newMod.steady) { resultMod.steady = true; modified = true; }
            if (newMod.swift > 0) { resultMod.swift = newMod.swift; modified = true; }
            if (newMod.sown) { resultMod.sown = true; modified = true; }
            if (newMod.vigorous > 0) { resultMod.vigorous = newMod.vigorous; modified = true; }
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
            var newMod = mod.clone();
            newMod.tmpChangeCost = -1;
            newMod.tmpUntilPlayedCost = -1;
            newMod.tmpRetain = false;
            newMod.swift = 0;
            newMod.sown = false;
            newMod.glam = false;
            newMod.vigorous = 0;
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

        @Override
        public Card enchantAdroit(int adroit) {
            var newMod = mod.clone();
            newMod.adroit = adroit;
            return new CardWrapper(card, newMod);
        }

        @Override
        public Card enchantCorrupted() {
            var newMod = mod.clone();
            newMod.corrupted = true;
            return new CardWrapper(card, newMod);
        }

        @Override
        public Card enchantGlam() {
            var newMod = mod.clone();
            newMod.glam = true;
            return new CardWrapper(card, newMod);
        }

        @Override
        public Card enchantInky() {
            var newMod = mod.clone();
            newMod.inky = true;
            return new CardWrapper(card, newMod);
        }

        @Override
        public Card enchantInstinct() {
            var newMod = mod.clone();
            newMod.instinct = true;
            return new CardWrapper(card, newMod);
        }

        @Override
        public Card enchantNimble(int nimble) {
            var newMod = mod.clone();
            newMod.nimble = nimble;
            return new CardWrapper(card, newMod);
        }

        @Override
        public Card enchantPerfectFit() {
            var newMod = mod.clone();
            newMod.perfectFit = true;
            return new CardWrapper(card, newMod);
        }

        @Override
        public Card enchantRoyallyApproved() {
            var newMod = mod.clone();
            newMod.royallyApproved = true;
            return new CardWrapper(card, newMod);
        }

        @Override
        public Card enchantSharp(int sharp) {
            var newMod = mod.clone();
            newMod.sharp = sharp;
            return new CardWrapper(card, newMod);
        }

        @Override
        public Card enchantSoulsPower() {
            var newMod = mod.clone();
            newMod.soulsPower = true;
            return new CardWrapper(card, newMod);
        }

        @Override
        public Card enchantSpiral(int spiral) {
            var newMod = mod.clone();
            newMod.spiral = spiral;
            return new CardWrapper(card, newMod);
        }

        @Override
        public Card enchantSteady() {
            var newMod = mod.clone();
            newMod.steady = true;
            return new CardWrapper(card, newMod);
        }

        @Override
        public Card enchantSwift(int swift) {
            var newMod = mod.clone();
            newMod.swift = swift;
            return new CardWrapper(card, newMod);
        }

        @Override
        public Card enchantSown() {
            var newMod = mod.clone();
            newMod.sown = true;
            return new CardWrapper(card, newMod);
        }

        @Override
        public Card enchantVigorous(int vigorous) {
            var newMod = mod.clone();
            newMod.vigorous = vigorous;
            return new CardWrapper(card, newMod);
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
