package com.alphaStS;

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

        public GameActionCtx play(GameState state, int idx) {
            if (state.getEnemiesForWrite().getForWrite(idx).applyDebuff(DebuffType.LOSE_STRENGTH_EOT, -n)) {
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

        public GameActionCtx play(GameState state, int idx) {
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
}
