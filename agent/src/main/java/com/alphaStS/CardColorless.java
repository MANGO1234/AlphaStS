package com.alphaStS;

import java.util.List;
import java.util.Objects;

public class CardColorless {
    // Bandage Up
    // Blind

    private static abstract class _DarkShacklesT extends Card {
        private final int n;

        public _DarkShacklesT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost);
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

    // Deep Breath
    // Discovery
    // Dramatic Entrance
    // Enlightenment
    // Finesse
    // Flash of Steel
    // Forethought
    // Good Instincts
    // Impatience
    // Jack Of All Trades
    // Madness
    // Mind Blast
    // Panacea
    // Panic Button
    // Purity
    // Swift Strike
    // Trip
    // Apotheosis
    // Chrysalis
    // Hand of Greed
    // Magnetism
    // Master Of Strategy
    // Mayhem
    // Metamorphosis
    // Panache
    // Sadistic Nature
    // Secret Technique
    // Secret Weapon
    // The Bomb
    // Thinking Ahead
    // Transmutation
    // Violence

    private static abstract class _BiteT extends Card {
        private final int n;
        private final int heal;

        public _BiteT(String cardName, int cardType, int energyCost, int n, int heal) {
            super(cardName, cardType, energyCost);
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
            super(cardName, cardType, energyCost);
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

    public static class Finess extends Card {
        public Finess() {
            super("Finess", Card.SKILL, 0);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(2);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FinessP extends Card {
        public FinessP() {
            super("Finess+", Card.SKILL, 0);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(4);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _ApotheosisT extends Card {
        public _ApotheosisT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost);
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
}
