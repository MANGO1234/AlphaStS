package com.alphaStS;

public abstract class Potion {
    boolean canVulnerable;
    boolean changePlayerStrength;
    boolean changePlayerDexterity;
    boolean selectEnemy;

    public abstract void use(GameState state, int enemyIdx);
    public abstract void useDouble(GameState state, int enemyIdx);

    public static class VulnerablePotion extends Potion {
        public VulnerablePotion() {
            canVulnerable = true;
            selectEnemy = true;
        }

        @Override public void use(GameState state, int enemyIdx) {
            state.getEnemiesForWrite().getForWrite(enemyIdx).applyDebuff(DebuffType.VULNERABLE, 3);
        }

        @Override public void useDouble(GameState state, int enemyIdx) {
            state.getEnemiesForWrite().getForWrite(enemyIdx).applyDebuff(DebuffType.VULNERABLE, 6);
        }
    }

    public static class StrengthPotion extends Potion {
        public StrengthPotion() {
            changePlayerStrength = true;
        }

        @Override public void use(GameState state, int enemyIdx) {
            state.getPlayerForWrite().gainStrength(2);
        }

        @Override public void useDouble(GameState state, int enemyIdx) {
            state.getPlayerForWrite().gainStrength(4);
        }

        @Override public String toString() {
            return "Strength Potion";
        }
    }

    public static class DexterityPotion extends Potion {
        public DexterityPotion() {
            changePlayerDexterity = true;
        }

        @Override public void use(GameState state, int enemyIdx) {
            state.getPlayerForWrite().gainDexterity(2);
        }

        @Override public void useDouble(GameState state, int enemyIdx) {
            state.getPlayerForWrite().gainDexterity(4);
        }

        @Override public String toString() {
            return "Dexterity Potion";
        }
    }
}
