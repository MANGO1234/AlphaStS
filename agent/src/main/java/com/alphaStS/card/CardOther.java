package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.enemy.EnemyBeyond;
import com.alphaStS.enums.Stance;

import java.util.List;

public class CardOther {
    // **********************************************************************************************************
    // ********************************************* Statuses ***************************************************
    // **********************************************************************************************************

    public static class Burn extends Card {
        public Burn() {
            super("Burn", Card.STATUS, -1, Card.COMMON);
            alwaysDiscard = true;
        }

        @Override public void onDiscardEndOfTurn(GameState state, int numCardsInHand) {
            state.doNonAttackDamageToPlayer(2, true, this);
        }
    }

    public static class BurnP extends Card {
        public BurnP() {
            super("Burn+", Card.STATUS, -1, Card.COMMON);
            alwaysDiscard = true;
        }

        @Override public void onDiscardEndOfTurn(GameState state, int numCardsInHand) {
            state.doNonAttackDamageToPlayer(4, true, this);
        }
    }

    public static class Dazed extends Card {
        public Dazed() {
            super("Dazed", Card.STATUS, -1, Card.COMMON);
            ethereal = true;
        }
    }

    public static class Slime extends Card {
        public Slime() {
            super("Slimed", Card.STATUS, 1, Card.COMMON);
            exhaustWhenPlayed = true;
        }
    }

    public static class Wound extends Card {
        public Wound() {
            super("Wound", Card.STATUS, -1, Card.COMMON);
        }
    }

    public static class Void extends Card {
        public Void() {
            super("Void", Card.STATUS, -1, Card.COMMON);
            ethereal = true;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnCardDrawnHandler("Void", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx] instanceof CardOther.Void) {
                        state.gainEnergy(-1);
                    }
                }
            });
        }
    }

    // **********************************************************************************************************
    // ********************************************** Curses ****************************************************
    // **********************************************************************************************************

    public static class AscendersBane extends Card {
        public AscendersBane() {
            super("Ascender's Bane", Card.CURSE, -1, Card.COMMON);
            ethereal = true;
            exhaustWhenPlayed = true;
        }
    }

    public static class Clumsy extends Card {
        public Clumsy() {
            super("Clumsy", Card.CURSE, -1, Card.COMMON);
            ethereal = true;
            exhaustWhenPlayed = true;
        }
    }

    public static class Necronomicurse extends Card {
        public Necronomicurse() {
            super("Necronomicurse", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
        }

        int cardIndex = -1;

        @Override public void gamePropertiesSetup(GameState state) {
            cardIndex = state.properties.findCardIndex(this);
        }

        @Override public void onExhaust(GameState state) {
            state.addCardToHand(cardIndex);
        }
    }

    public static class Decay extends Card {
        public Decay() {
            super("Decay", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
            alwaysDiscard = true;
        }

        @Override public void onDiscardEndOfTurn(GameState state, int numCardsInHand) {
            state.doNonAttackDamageToPlayer(2, true, this);
        }
    }

    public static class Doubt extends Card {
        public Doubt() {
            super("Doubt", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
            alwaysDiscard = true;
        }

        @Override public void onDiscardEndOfTurn(GameState state, int numCardsInHand) {
            state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
        }
    }

    public static class Normality extends Card {
        public Normality() {
            super("Normality", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Normality", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            // todo: wrong if multiple normality in hand
            state.properties.addOnCardPlayedHandler("Normality", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    state.getCounterForWrite()[counterIdx] += 1;
                }
            });
            state.properties.addPreEndOfTurnHandler("Normality", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }

        public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.normalityCounterIdx = idx;
        }
    }

    public static class Pain extends Card {
        public Pain() {
            super("Pain", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var _this = this;
            state.properties.addOnCardPlayedHandler("Pain", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    for (int i = 0; i < state.handArrLen; i++) {
                        if (state.properties.cardDict[state.getHandArrForRead()[i]] instanceof CardOther.Pain) {
                            state.doNonAttackDamageToPlayer(1, false, _this);
                        }
                    }
                }
            });
        }
    }

    public static class Regret extends Card {
        public Regret() {
            super("Regret", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
            alwaysDiscard = true;
        }

        @Override public void onDiscardEndOfTurn(GameState state, int numCardsInHand) {
            state.doNonAttackDamageToPlayer(numCardsInHand, false, this);
        }
    }

    public static class Shame extends Card {
        public Shame() {
            super("Shame", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
            alwaysDiscard = true;
        }

        @Override public void onDiscardEndOfTurn(GameState state, int numCardsInHand) {
            state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 1);
        }
    }

    public static class Writhe extends Card {
        public Writhe() {
            super("Writhe", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
            innate = true;
        }
    }

    public static class Parasite extends Card {
        public Parasite() {
            super("Parasite", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
        }
    }

    public static class Injury extends Card {
        public Injury() {
            super("Injury", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
        }
    }

    public static class CurseOfTheBell extends Card {
        public CurseOfTheBell() {
            super("Curse of The Bell", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
        }
    }

    private abstract static class _FakeInfernalBladeT extends Card {
        public _FakeInfernalBladeT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.addCardToHand(state.properties.findCardIndex(new Card.CardTmpChangeCost(new Card.Strike(), 0)));
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new Card.CardTmpChangeCost(new Card.Strike(), 0));
        }
    }

    public static class FakeInfernalBlade extends _FakeInfernalBladeT {
        public FakeInfernalBlade() {
            super("Fake Infernal Blade", Card.ATTACK, 1);
        }
    }

    public static class FakeInfernalBladeP extends _FakeInfernalBladeT {
        public FakeInfernalBladeP() {
            super("Fake Infernal Blade+", Card.SKILL, 0);
        }
    }

    public static class GamblingChips extends Card {
        public GamblingChips() {
            super("Gambling Chips", Card.SKILL, -1, Card.COMMON);
            selectFromHand = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.draw(state.getCounterForRead()[counterIdx]);
                state.getCounterForWrite()[counterIdx] = 0;
                return GameActionCtx.PLAY_CARD;
            } else {
                state.discardCardFromHand(idx);
                state.getCounterForWrite()[counterIdx] += 1;
                return GameActionCtx.SELECT_CARD_HAND;
            }
        }

        public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Gambling Chips", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }
    }

    public static class UpgradeCardBeforeBattleStarts extends Card {
        public final int count;
        public final List<Card> cards;

        public UpgradeCardBeforeBattleStarts(List<Card> cards, int count) {
            super("Upgrade Card " + count, Card.STATUS, -1, Card.COMMON);
            if (count <= 0) {
                throw new IllegalArgumentException();
            }
            selectFromDeck = true;
            this.count = count;
            this.cards = cards;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.START_OF_BATTLE) {
                return GameActionCtx.SELECT_CARD_DECK;
            }
            state.getCounterForWrite()[counterIdx]++;
            state.removeCardFromDeck(idx, false);
            state.addCardToDeck(state.properties.findCardIndex(state.properties.cardDict[idx].getUpgrade()), false);
            if (state.getCounterForRead()[counterIdx] >= count) {
                state.getCounterForWrite()[counterIdx] = 0;
                return GameActionCtx.START_OF_BATTLE;
            }
            return GameActionCtx.SELECT_CARD_DECK;
        }

        @Override public boolean canSelectCard(Card card) {
            return cards.stream().anyMatch((c) -> card.cardName.equals(c.cardName));
        }

        public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Upgrade Card Before Battle", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }
    }

    public static class RemoveCardBeforeBattleStarts extends Card {
        public final int count;
        public final List<Card> cards;

        public RemoveCardBeforeBattleStarts(List<Card> cards, int count) {
            super("Remove Card " + count, Card.STATUS, -1, Card.COMMON);
            if (count <= 0) {
                throw new IllegalArgumentException();
            }
            selectFromDeck = true;
            this.count = count;
            this.cards = cards;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.START_OF_BATTLE) {
                return GameActionCtx.SELECT_CARD_DECK;
            }
            state.getCounterForWrite()[counterIdx]++;
            state.removeCardFromDeck(idx, false);
            if (state.getCounterForRead()[counterIdx] >= count) {
                state.getCounterForWrite()[counterIdx] = 0;
                return GameActionCtx.START_OF_BATTLE;
            }
            return GameActionCtx.SELECT_CARD_DECK;
        }

        @Override public boolean canSelectCard(Card card) {
            return cards.stream().anyMatch((c) -> card.cardName.equals(c.cardName));
        }

        public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Remove Card Before Battle", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }
    }

    public static class AddCardBeforeBattleStarts extends Card {
        public final List<List<Card>> cards;

        public AddCardBeforeBattleStarts(List<List<Card>> cards) {
            super("Add Card " + cards.size(), Card.STATUS, -1, Card.COMMON);
            if (cards.isEmpty()) {
                throw new IllegalArgumentException();
            }
            this.cards = cards;
            this.selectFromExhaust = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.START_OF_BATTLE) {
                var cards = this.cards.get(state.getCounterForWrite()[counterIdx]);
                for (Card card : cards) {
                    state.addCardToExhaust(state.properties.findCardIndex(card));
                }
                state.addCardToExhaust(state.properties.findCardIndex(this));
                return GameActionCtx.SELECT_CARD_EXHAUST;
            }
            var cards = this.cards.get(state.getCounterForWrite()[counterIdx]);
            for (Card card : cards) {
                state.removeCardFromExhaust(state.properties.findCardIndex(card));
            }
            state.removeCardFromExhaust(state.properties.findCardIndex(this));
            if (!(state.properties.cardDict[idx] instanceof AddCardBeforeBattleStarts)) {
                state.addCardToDeck(idx);
            }
            state.getCounterForWrite()[counterIdx]++;
            if (state.getCounterForRead()[counterIdx] >= this.cards.size()) {
                state.getCounterForWrite()[counterIdx] = 0;
                return GameActionCtx.START_OF_BATTLE;
            }
            cards = this.cards.get(state.getCounterForWrite()[counterIdx]);
            for (Card card : cards) {
                state.addCardToExhaust(state.properties.findCardIndex(card));
            }
            state.addCardToExhaust(state.properties.findCardIndex(this));
            return GameActionCtx.SELECT_CARD_EXHAUST;
        }

        public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Add Card Before Battle", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }
    }

    // **********************************************************************************************************
    // ********************************************* Wish Effects **********************************************
    // **********************************************************************************************************

    public static abstract class _WishPlatedArmorT extends Card {
        private final int armor;

        public _WishPlatedArmorT(String cardName, int armor) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.armor = armor;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(armor);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class WishPlatedArmor extends _WishPlatedArmorT {
        public WishPlatedArmor() {
            super("I Wish For Armor", 6);
        }
    }

    public static class WishPlatedArmorP extends _WishPlatedArmorT {
        public WishPlatedArmorP() {
            super("I Wish For Armor+", 8);
        }
    }

    public static abstract class _WishStrengthT extends Card {
        private final int strength;

        public _WishStrengthT(String cardName, int strength) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.strength = strength;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainStrength(strength);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class WishStrength extends _WishStrengthT {
        public WishStrength() {
            super("I Wish For Strength", 3);
        }
    }

    public static class WishStrengthP extends _WishStrengthT {
        public WishStrengthP() {
            super("I Wish For Strength+", 4);
        }
    }

    public static abstract class _WishGoldT extends Card {
        private final int gold;
        protected double healthRewardRatio = 0;

        public _WishGoldT(String cardName, int gold, double healthRewardRatio) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.gold = gold;
            this.healthRewardRatio = healthRewardRatio;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += gold;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("WishGold", this, healthRewardRatio == 0 ? null : new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 100.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            if (healthRewardRatio > 0) {
                state.properties.addExtraTrainingTarget("WishGold", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                        if (isTerminal > 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getCounterForRead()[counterIdx] / 100.0;
                        }
                    }

                    @Override public void updateQValues(GameState state, double[] v) {
                        double vGold = v[GameState.V_OTHER_IDX_START + vArrayIdx];
                        v[GameState.V_HEALTH_IDX] += 100 * vGold * healthRewardRatio / state.getPlayeForRead().getMaxHealth();
                    }
                });
            }
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            counterIdx = idx;
        }
    }

    public static class WishGold extends _WishGoldT {
        public WishGold(double healthRewardRatio) {
            super("I Wish For Gold", 25, healthRewardRatio);
        }
    }

    public static class WishGoldP extends _WishGoldT {
        public WishGoldP(double healthRewardRatio) {
            super("I Wish For Gold+", 30, healthRewardRatio);
        }
    }

    public static class EnterCalm extends Card {
        public EnterCalm() {
            super("Enter Calm", Card.SKILL, 0, Card.COMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.changeStance(Stance.CALM);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class EnterWrath extends Card {
        public EnterWrath() {
            super("Enter Wrath", Card.SKILL, 0, Card.COMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.changeStance(Stance.WRATH);
            return GameActionCtx.PLAY_CARD;
        }
    }
}
