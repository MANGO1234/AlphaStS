package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.enemy.PredefinedEncounter;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.enums.Stance;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.random.RandomGenCtx;

import java.util.List;

public class CardOther {
    // **********************************************************************************************************
    // ********************************************* Statuses ***************************************************
    // **********************************************************************************************************

    // Burn (Common) - Unplayable, Status
    //   Effect: Unplayable. At the end of your turn, take 2 damage.
    //   Upgraded Effect: Unplayable. At the end of your turn, take 4 damage.
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

    // Dazed (Common) - Unplayable, Status
    //   Effect: Unplayable. Ethereal.
    public static class Dazed extends Card {
        public Dazed() {
            super("Dazed", Card.STATUS, -1, Card.COMMON);
            ethereal = true;
        }
    }

    // Slimed (Common) - 1 energy, Status
    //   Effect: Exhaust.
    public static class Slimed extends Card {
        public Slimed() {
            super("Slimed", Card.STATUS, 1, Card.COMMON);
            exhaustWhenPlayed = true;
        }
    }

    // Wound (Common) - Unplayable, Status
    //   Effect: Unplayable.
    public static class Wound extends Card {
        public Wound() {
            super("Wound", Card.STATUS, -1, Card.COMMON);
        }
    }

    // Void (Common) - Unplayable, Status
    //   Effect: Unplayable. Ethereal. Whenever this card is drawn, lose 1 Energy.
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

    // Ascender's Bane (Curse) - Unplayable, Curse
    //   Effect: Unplayable. Ethereal. Cannot be removed from your deck.
    public static class AscendersBane extends Card {
        public AscendersBane() {
            super("Ascender's Bane", Card.CURSE, -1, Card.COMMON);
            ethereal = true;
            exhaustWhenPlayed = true;
        }
    }

    // Clumsy (Curse) - Unplayable, Curse
    //   Effect: Unplayable. Ethereal.
    public static class Clumsy extends Card {
        public Clumsy() {
            super("Clumsy", Card.CURSE, -1, Card.COMMON);
            ethereal = true;
            exhaustWhenPlayed = true;
        }
    }

    // Necronomicurse (Curse) - Unplayable, Curse
    //   Effect: Unplayable. There is no escape from this curse.
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

    // Decay (Curse) - Unplayable, Curse
    //   Effect: Unplayable. At the end of your turn, take 2 damage.
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

    // Doubt (Curse) - Unplayable, Curse
    //   Effect: Unplayable. At the end of your turn, gain 1 Weak.
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

    // Normality (Curse) - Unplayable, Curse
    //   Effect: Unplayable. While in hand, you cannot play more than 3 cards this turn.
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

    // Pain (Curse) - Unplayable, Curse
    //   Effect: Unplayable. While in hand, lose 1 HP when other cards are played.
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

    // Regret (Curse) - Unplayable, Curse
    //   Effect: Unplayable. At the end of your turn, lose HP equal to the number of cards in your hand.
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

    // Shame (Curse) - Unplayable, Curse
    //   Effect: Unplayable. At the end of your turn, gain 1 Frail.
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

    // Writhe (Curse) - Unplayable, Curse
    //   Effect: Unplayable. Innate.
    public static class Writhe extends Card {
        public Writhe() {
            super("Writhe", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
            innate = true;
        }
    }

    // Parasite (Curse) - Unplayable, Curse
    //   Effect: Unplayable. If transformed or removed from your deck, lose 3 Max HP.
    public static class Parasite extends Card {
        public Parasite() {
            super("Parasite", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
        }
    }

    // Injury (Curse) - Unplayable, Curse
    //   Effect: Unplayable.
    public static class Injury extends Card {
        public Injury() {
            super("Injury", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
        }
    }

    // Curse of the Bell (Curse) - Unplayable, Curse
    //   Effect: Unplayable. Cannot be removed from your deck.
    public static class CurseOfTheBell extends Card {
        public CurseOfTheBell() {
            super("Curse of the Bell", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
        }
    }

    public static class GamblingChips extends Card {
        public GamblingChips() {
            super("Gambling Chips", Card.SKILL, -1, Card.COMMON);
            entityProperty.selectFromHand = true;
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
            entityProperty.selectFromDeck = true;
            this.count = count;
            this.cards = cards;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.START_OF_BATTLE) {
                return GameActionCtx.SELECT_CARD_DECK;
            }
            state.getCounterForWrite()[counterIdx]++;
            state.removeCardFromDeck(idx, false);
            state.addCardToDeckNonRandom(state.properties.findCardIndex(state.properties.cardDict[idx].getUpgrade(state.properties)));
            if (state.getCounterForRead()[counterIdx] >= count) {
                state.getCounterForWrite()[counterIdx] = 0;
                return GameActionCtx.START_OF_BATTLE;
            }
            if (state.properties.inMCTSSearch && state.getSearchRandomGen().nextInt(100, RandomGenCtx.Misc) < 5) {
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
            entityProperty.selectFromDeck = true;
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
            if (state.properties.inMCTSSearch && state.getSearchRandomGen().nextInt(100, RandomGenCtx.Misc) < 5) {
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
            entityProperty.selectFromExhaust = true;
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
                state.addCardToDeckNonRandom(idx);
            }
            state.getCounterForWrite()[counterIdx]++;
            if (state.getCounterForRead()[counterIdx] >= this.cards.size()) {
                state.getCounterForWrite()[counterIdx] = 0;
                return GameActionCtx.START_OF_BATTLE;
            }
            if (state.properties.inMCTSSearch && state.getSearchRandomGen().nextInt(100, RandomGenCtx.Misc) < 5) {
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

    public static class AddCardBeforeBattleStarts2 extends Card {
        public final int count;
        public final List<Card> cards;

        public AddCardBeforeBattleStarts2(List<Card> cards, int count) {
            super("Add Card " + count, Card.STATUS, -1, Card.COMMON);
            if (cards.isEmpty() || count <= 0) {
                throw new IllegalArgumentException();
            }
            entityProperty.selectFromExhaust = true;
            this.count = count;
            this.cards = cards;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.START_OF_BATTLE) {
                addCardsToExhaust(state);
                return GameActionCtx.SELECT_CARD_EXHAUST;
            }
            if (state.properties.cardDict[idx] instanceof AddCardBeforeBattleStarts2) {
                reset(state);
                return GameActionCtx.START_OF_BATTLE;
            }
            state.removeCardFromExhaust(idx);
            state.addCardToDeckNonRandom(idx);
            state.getCounterForWrite()[counterIdx]++;
            if (state.getCounterForRead()[counterIdx] >= count) {
                reset(state);
                return GameActionCtx.START_OF_BATTLE;
            }
            if (state.properties.inMCTSSearch && state.getSearchRandomGen().nextInt(100, RandomGenCtx.Misc) < 5) {
                reset(state);
                return GameActionCtx.START_OF_BATTLE;
            }
            return GameActionCtx.SELECT_CARD_EXHAUST;
        }

        private void addCardsToExhaust(GameState state) {
            for (Card card : cards) {
                state.addCardToExhaust(state.properties.findCardIndex(card));
            }
            state.addCardToExhaust(state.properties.findCardIndex(this));
        }

        private void reset(GameState state) {
            for (Card card : cards) {
                state.removeCardFromExhaust(state.properties.findCardIndex(card));
            }
            state.removeCardFromExhaust(state.properties.findCardIndex(this));
            state.getCounterForWrite()[counterIdx] = 0;
        }

        @Override public boolean canSelectCard(Card card) {
            return card instanceof AddCardBeforeBattleStarts2 || cards.stream().anyMatch((c) -> card.cardName.equals(c.cardName));
        }

        public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Add Card Before Battle 2", this, new GameProperties.NetworkInputHandler() {
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

    public static abstract class _LiveForeverT extends Card {
        private final int armor;

        public _LiveForeverT(String cardName, int armor) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.armor = armor;
            this.select1OutOf3CardEffectCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[state.properties.platedArmorCounterIdx] += armor;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerPlatedArmorCounter();
        }
    }

    public static class LiveForever extends _LiveForeverT {
        public LiveForever() {
            super("I Wish For Armor", 6);
        }
    }

    public static class LiveForeverP extends _LiveForeverT {
        public LiveForeverP() {
            super("I Wish For Armor+", 8);
        }
    }

    public static abstract class _BecomeAlmightyT extends Card {
        private final int strength;

        public _BecomeAlmightyT(String cardName, int strength) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.strength = strength;
            this.entityProperty.changePlayerStrength = true;
            this.select1OutOf3CardEffectCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainStrength(strength);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BecomeAlmighty extends _BecomeAlmightyT {
        public BecomeAlmighty() {
            super("I Wish For Strength", 3);
        }
    }

    public static class BecomeAlmightyP extends _BecomeAlmightyT {
        public BecomeAlmightyP() {
            super("I Wish For Strength+", 4);
        }
    }

    public static abstract class _FameAndFortuneT extends Card {
        private final int gold;
        protected double healthRewardRatio = 0;

        public _FameAndFortuneT(String cardName, int gold, double healthRewardRatio) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.gold = gold;
            this.healthRewardRatio = healthRewardRatio;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += gold;
            this.select1OutOf3CardEffectCard = true;
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
                    @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                        if (isTerminal > 0) {
                            v.setVExtra(vExtraIdx, state.getCounterForRead()[counterIdx] / 100.0);
                        }
                    }

                    @Override public void updateQValues(GameState state, VArray v) {
                        if (!state.isEncounter(PredefinedEncounter.CORRUPT_HEART)) {
                            double vGold = v.getVExtra(vExtraIdx);
                            v.add(GameState.V_HEALTH_IDX, 100 * vGold * healthRewardRatio / state.getPlayerForRead().getMaxHealth());
                        }
                    }
                });
            }
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            counterIdx = idx;
        }
    }

    public static class FameAndFortune extends _FameAndFortuneT {
        public FameAndFortune() { this(2); }

        public FameAndFortune(double healthRewardRatio) {
            super("I Wish For Gold", 25, healthRewardRatio);
        }
    }

    public static class FameAndFortuneP extends _FameAndFortuneT {
        public FameAndFortuneP() { this(2); }

        public FameAndFortuneP(double healthRewardRatio) {
            super("I Wish For Gold+", 30, healthRewardRatio);
        }
    }

    public static class EnterCalm extends Card {
        public EnterCalm() {
            super("Enter Calm", Card.SKILL, 0, Card.COMMON);
            this.select1OutOf3CardEffectCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.changeStance(Stance.CALM);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class EnterWrath extends Card {
        public EnterWrath() {
            super("Enter Wrath", Card.SKILL, 0, Card.COMMON);
            this.select1OutOf3CardEffectCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.changeStance(Stance.WRATH);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class NilroysCodexChoice extends Card {
        public NilroysCodexChoice() {
            super("Nilroy's Codex", Card.SKILL, 0, Card.RARE);
        }

        @Override public GameActionCtx play(GameState state, int idx, int energyUsed) {
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ScryOnShuffle extends Card {
        public ScryOnShuffle() {
            super("Scry On Shuffle", Card.SKILL, 0, Card.COMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            return GameActionCtx.PLAY_CARD;
        }
    }

    // **************************************************************************************************
    // ********************************************* Curse  *********************************************
    // **************************************************************************************************

    // No need to implement Pride: Unplayable special curse
}
