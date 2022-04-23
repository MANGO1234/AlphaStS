package com.alphaStS;

public abstract class Potion {
    boolean vulnEnemy;
    boolean weakEnemy;
    boolean changePlayerStrength;
    boolean changePlayerDexterity;
    boolean changePlayerArtifact;
    boolean selectEnemy;
    boolean selectFromHand;
    boolean selectFromDiscard;

    public abstract GameActionCtx use(GameState state, int idx);
    public abstract GameActionCtx useDouble(GameState state, int idx);

    public static class VulnerablePotion extends Potion {
        public VulnerablePotion() {
            vulnEnemy = true;
            selectEnemy = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(DebuffType.VULNERABLE, 3);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public GameActionCtx useDouble(GameState state, int idx) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(DebuffType.VULNERABLE, 6);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Vulnerable Potion";
        }
    }

    public static class WeakPotion extends Potion {
        public WeakPotion() {
            weakEnemy = true;
            selectEnemy = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(DebuffType.WEAK, 3);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public GameActionCtx useDouble(GameState state, int idx) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(DebuffType.WEAK, 6);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Weak Potion";
        }
    }

    public static class StrengthPotion extends Potion {
        public StrengthPotion() {
            changePlayerStrength = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.getPlayerForWrite().gainStrength(2);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public GameActionCtx useDouble(GameState state, int idx) {
            state.getPlayerForWrite().gainStrength(4);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Strength Potion";
        }
    }

    public static class DexterityPotion extends Potion {
        public DexterityPotion() {
            changePlayerDexterity = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.getPlayerForWrite().gainDexterity(2);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public GameActionCtx useDouble(GameState state, int idx) {
            state.getPlayerForWrite().gainDexterity(4);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Dexterity Potion";
        }
    }

    public static class AncientPotion extends Potion {
        public AncientPotion() {
            changePlayerArtifact = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.getPlayerForWrite().gainArtifact(1);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public GameActionCtx useDouble(GameState state, int idx) {
            state.getPlayerForWrite().gainArtifact(2);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Ancient Potion";
        }
    }
}
