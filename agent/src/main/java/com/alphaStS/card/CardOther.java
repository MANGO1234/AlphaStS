package com.alphaStS.card;

import com.alphaStS.*;

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
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
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
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
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
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
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
}
