package com.alphaStS;

public class CardSilent {
    public static class Neutralize extends Card {
        public Neutralize() {
            super("Neutralize", Card.ATTACK, 0);
            this.weakEnemy = true;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 3);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(DebuffType.WEAK, 1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class NeutralizeP extends Card {
        public NeutralizeP() {
            super("Neutralize+", Card.ATTACK, 0);
            this.weakEnemy = true;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 4);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(DebuffType.WEAK, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _SurvivorT extends Card {
        private final int n;

        public _SurvivorT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost);
            this.n = n;
            this.selectFromHand = true;
            this.selectFromHandLater = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.getPlayerForWrite().gainBlock(n);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.removeCardFromHand(idx);
                state.addCardToDiscard(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class Survivor extends _SurvivorT {
        public Survivor() {
            super("Survivor", Card.SKILL, 1, 8);
        }
    }

    public static class SurvivorP extends _SurvivorT {
        public SurvivorP() {
            super("Survivor+", Card.SKILL, 1, 11);
        }
    }

    private static abstract class _AcrobaticsT extends Card {
        private final int n;

        public _AcrobaticsT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost);
            this.n = n;
            this.selectFromHand = true;
            this.selectFromHandLater = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.draw(n);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.removeCardFromHand(idx);
                state.addCardToDiscard(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class Acrobatics extends _AcrobaticsT {
        public Acrobatics() {
            super("Acrobatics", Card.SKILL, 1, 3);
        }
    }

    public static class AcrobaticsP extends _AcrobaticsT {
        public AcrobaticsP() {
            super("Acrobatics+", Card.SKILL, 1, 4);
        }
    }
}
