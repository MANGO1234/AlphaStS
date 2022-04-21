package com.alphaStS;

public class CardNeutral {
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

}
