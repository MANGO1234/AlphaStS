package com.alphaStS;

import java.util.List;
import java.util.Objects;

public class CardColorless {
    // Bandage Up

    public static class Blind extends Card {
        public Blind() {
            super("Blind", Card.SKILL, 0, Card.UNCOMMON);
            selectEnemy = true;
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, 2);
            return GameActionCtx.PLAY_CARD;
        }
}

    public static class BlindP extends Card {
        public BlindP() {
            super("Blind+", Card.SKILL, 0, Card.UNCOMMON);
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.WEAK, 2);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _DarkShacklesT extends Card {
        private final int n;

        public _DarkShacklesT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.exhaustWhenPlayed = true;
            this.selectEnemy = true;
            this.affectEnemyStrength = true;
            this.affectEnemyStrengthEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, -n)) {
                state.getEnemiesForWrite().getForWrite(idx).gainStrength(-n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DarkShackles extends _DarkShacklesT {
        public DarkShackles() {
            super("Dark Shackles", Card.SKILL, 0, 9);
        }
    }

    public static class DarkShacklesP extends _DarkShacklesT {
        public DarkShacklesP() {
            super("Dark Shackles+", Card.SKILL, 0, 15);
        }
    }

    private static abstract class _DeepBreathT extends Card {
        private final int n;

        public _DeepBreathT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.reshuffle();
            state.draw(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DeepBreath extends _DeepBreathT {
        public DeepBreath() {
            super("Deep Breath", Card.SKILL, 0, 1);
        }
    }

    public static class DeepBreathP extends _DeepBreathT {
        public DeepBreathP() {
            super("Deep Breath+", Card.SKILL, 0, 2);
        }
    }

    // Discovery

    private static abstract class _DramaticEntranceT extends Card {
        private final int n;

        public _DramaticEntranceT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            exhaustWhenPlayed = true;
            innate = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DramaticEntrance extends _DramaticEntranceT {
        public DramaticEntrance() {
            super("Dramatic Entrance", Card.SKILL, 0, 8);
        }
    }

    public static class DramaticEntranceP extends _DramaticEntranceT {
        public DramaticEntranceP() {
            super("Dramatic Entrance+", Card.SKILL, 0, 12);
        }
    }

    // Enlightenment

    private static abstract class _FinesseT extends Card {
        private final int n;

        public _FinesseT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Finesse extends _FinesseT {
        public Finesse() {
            super("Finesse", Card.SKILL, 0, 2);
        }
    }

    public static class FinesseP extends _FinesseT {
        public FinesseP() {
            super("Finesse+", Card.SKILL, 0, 4);
        }
    }

    private static abstract class _FlashOfSteelT extends Card {
        private final int n;

        public _FlashOfSteelT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FlashOfSteel extends _FlashOfSteelT {
        public FlashOfSteel() {
            super("Flash Of Steel", Card.ATTACK, 0, 3);
        }
    }

    public static class FlashOfSteelP extends _FlashOfSteelT {
        public FlashOfSteelP() {
            super("Flash Of Steel+", Card.ATTACK, 0, 5);
        }
    }

    // Forethought

    private static abstract class _GoodInstinctsT extends Card {
        private final int n;

        public _GoodInstinctsT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class GoodInstincts extends _GoodInstinctsT {
        public GoodInstincts() {
            super("Good Instincts", Card.SKILL, 0, 6);
        }
    }

    public static class GoodInstinctsP extends _GoodInstinctsT {
        public GoodInstinctsP() {
            super("Good Instincts+", Card.SKILL, 0, 9);
        }
    }

    private static abstract class _ImpatienceT extends Card {
        private final int n;

        public _ImpatienceT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < state.getHand().length; i++) {
                if (state.getHand()[i] > 0 && state.prop.cardDict[i].cardType == Card.ATTACK) {
                    return GameActionCtx.PLAY_CARD;
                }
            }
            state.draw(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Impatience extends _ImpatienceT {
        public Impatience() {
            super("Impatience", Card.SKILL, 0, 2);
        }
    }

    public static class ImpatienceP extends _ImpatienceT {
        public ImpatienceP() {
            super("Impatience+", Card.SKILL, 0, 3);
        }
    }

    // Jack Of All Trades
    // Madness

    private static abstract class _MindBlastT extends Card {
        public _MindBlastT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            innate = true;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), state.getNumCardsInDeck());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class MindBlast extends _MindBlastT {
        public MindBlast() {
            super("Mind Blast", Card.ATTACK, 2);
        }
    }

    public static class MindBlastP extends _MindBlastT {
        public MindBlastP() {
            super("Mind Blast+", Card.ATTACK, 1);
        }
    }

    private static abstract class _PanaceaT extends Card {
        private final int n;

        public _PanaceaT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainArtifact(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Panacea extends _PanaceaT {
        public Panacea() {
            super("Panacea", Card.SKILL, 0, 1);
        }
    }

    public static class PanaceaP extends _PanaceaT {
        public PanaceaP() {
            super("Panacea+", Card.SKILL, 0, 2);
        }
    }

    // Panic Button
    // Purity

    private static abstract class _SwiftStrikeT extends Card {
        private final int n;

        public _SwiftStrikeT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SwiftStrike extends _SwiftStrikeT {
        public SwiftStrike() {
            super("Swift Strike", Card.ATTACK, 0, 7);
        }
    }

    public static class SwiftStrikeP extends _SwiftStrikeT {
        public SwiftStrikeP() {
            super("Swift Strike+", Card.ATTACK, 0, 10);
        }
    }

    public static class Trip extends Card {
        public Trip() {
            super("Trip", Card.SKILL, 0, Card.UNCOMMON);
            selectEnemy = true;
            vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.VULNERABLE, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class TripP extends Card {
        public TripP() {
            super("Trip+", Card.SKILL, 0, Card.UNCOMMON);
            vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.VULNERABLE, 2);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _ApotheosisT extends Card {
        public _ApotheosisT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost, Card.RARE);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < state.prop.upgradeIdxes.length; i++) {
                if (state.hand[i] > 0 && state.prop.upgradeIdxes[i] >= 0) {
                    state.hand[state.prop.upgradeIdxes[i]] += state.hand[i];
                    state.hand[i] = 0;
                }
                if (state.deck[i] > 0 && state.prop.upgradeIdxes[i] >= 0) {
                    state.deck[state.prop.upgradeIdxes[i]] += state.deck[i];
                    state.deck[i] = 0;
                }
                if (state.discard[i] > 0 && state.prop.upgradeIdxes[i] >= 0) {
                    state.discard[state.prop.upgradeIdxes[i]] += state.discard[i];
                    state.discard[i] = 0;
                }
                if (state.getExhaustForRead()[i] > 0 && state.prop.upgradeIdxes[i] >= 0) {
                    state.getExhaustForWrite()[state.prop.upgradeIdxes[i]] += state.getExhaustForRead()[i];
                    state.getExhaustForWrite()[i] = 0;
                }
            }
            for (int i = 0; i < state.deckArrLen; i++) {
                if (state.prop.upgradeIdxes[state.deckArr[i]] >= 0) {
                    state.deckArr[i] = (short) state.prop.upgradeIdxes[state.deckArr[i]];
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return cards.stream().map((x) -> CardUpgrade.map.get(x)).filter(Objects::nonNull).filter((x) -> !x.cardName.equals(this.cardName)).toList();
        }
    }

    public static class Apotheosis extends CardColorless._ApotheosisT {
        public Apotheosis() {
            super("Apotheosis", Card.SKILL, 2);
        }
    }

    public static class ApotheosisP extends CardColorless._ApotheosisT {
        public ApotheosisP() {
            super("Apotheosis+", Card.SKILL, 1);
        }
    }

    // Chrysalis
    // Hand of Greed
    // Magnetism

    private static abstract class _MasterOfStrategyT extends Card {
        private final int n;

        public _MasterOfStrategyT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.draw(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class MasterOfStrategy extends CardColorless._MasterOfStrategyT {
        public MasterOfStrategy() {
            super("Master Of Strategy", Card.SKILL, 0, 3);
        }
    }

    public static class MasterOfStrategyP extends CardColorless._MasterOfStrategyT {
        public MasterOfStrategyP() {
            super("Master Of Strategy+", Card.SKILL, 0, 4);
        }
    }

    // Mayhem
    // Metamorphosis

    private static abstract class _PanacheT extends Card {
        private final int n;

        public _PanacheT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n << 3;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Panache", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public String getDisplayString(GameState state) {
                    int counter = state.getCounterForRead()[counterIdx];
                    return (counter & ((1 << 16) - 1)) + "/" + (counter >> 16);
                }
            });
            state.addEndOfTurnHandler("Panache", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] &= ~7;
                }
            });
            state.addOnCardPlayedHandler("Panache", new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card, int lastIdx, boolean cloned) {
                    if (state.getCounterForRead()[counterIdx] >> 3 == 0) {
                        return;
                    }
                    var counter = state.getCounterForWrite();
                    counter[counterIdx]++;
                    if ((counter[counterIdx] & 7) == 5) {
                        counter[counterIdx] -= 5;
                        for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            state.playerDoNonAttackDamageToEnemy(enemy, counter[counterIdx] >> 3, true);
                        }
                    }
                }
            });
        }
    }

    public static class Panache extends CardColorless._PanacheT {
        public Panache() {
            super("Panache", Card.POWER, 0, 10);
        }
    }

    public static class PanacheP extends CardColorless._PanacheT {
        public PanacheP() {
            super("Panache+", Card.POWER, 0, 14);
        }
    }

    // Sadistic Nature
    // Secret Technique
    // Secret Weapon
    // The Bomb

    private static abstract class _ThinkingAheadT extends Card {
        public _ThinkingAheadT(String cardName, int cardType, int energyCost, boolean exhaustWhenPlayed) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.exhaustWhenPlayed = exhaustWhenPlayed;
            selectFromHand = true;
            selectFromHandLater = true;
            putCardOnTopDeck = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.draw(2);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.removeCardFromHand(idx);
                state.putCardOnTopOfDeck(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class ThinkingAhead extends CardColorless._ThinkingAheadT {
        public ThinkingAhead() {
            super("Thinking Ahead", Card.SKILL, 0, true);
        }
    }

    public static class ThinkingAheadP extends CardColorless._ThinkingAheadT {
        public ThinkingAheadP() {
            super("Thinking Ahead+", Card.SKILL, 0, false);
        }
    }

    // Transmutation
    // Violence

    private static abstract class _BiteT extends Card {
        private final int n;
        private final int heal;

        public _BiteT(String cardName, int cardType, int energyCost, int n, int heal) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.heal = heal;
            this.selectEnemy = true;
            this.healPlayer = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.getPlayerForWrite().heal(heal);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Bite extends _BiteT {
        public Bite() {
            super("Bite", Card.ATTACK, 1, 7, 2);
        }
    }

    public static class BiteP extends _BiteT {
        public BiteP() {
            super("Bite+", Card.ATTACK, 1, 8, 3);
        }
    }

    private static abstract class _RitualDaggerT extends Card {
        private final int n;
        private final int dmgInc;

        public _RitualDaggerT(String cardName, int cardType, int energyCost, int n, int dmgInc) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.dmgInc = dmgInc;
            this.selectEnemy = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] = state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n) ? 1 : -1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter(cardName, this, null);
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            counterIdx = idx;
            gameProperties.ritualDaggerCounterIdx = counterIdx;
        }
    }

    public static class RitualDagger extends _RitualDaggerT {
        public RitualDagger(int dmg) {
            super("Ritual Dagger", Card.ATTACK, 1, dmg, 3);
        }
    }

    public static class RitualDaggerP extends _RitualDaggerT {
        public RitualDaggerP(int dmg) {
            super("Ritual Dagger+", Card.ATTACK, 1, dmg, 5);
        }
    }
}
