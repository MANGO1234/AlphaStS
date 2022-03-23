package com.alphaStS;

public abstract class Potion {
    boolean canVulnerable;
    boolean canAffectPlayerStrength;
    boolean canAffectPlayerDexterity;
    boolean selectEnemy;

    public abstract void use(GameState state, int enemyIdx);
    public abstract void useDouble(GameState state, int enemyIdx);

    public static class VulnerablePotion extends Potion {
        public VulnerablePotion() {
            canVulnerable = true;
            selectEnemy = true;
        }

        @Override public void use(GameState state, int enemyIdx) {
            state.enemies.get(enemyIdx).applyDebuff(DebuffType.VULNERABLE, 3);
        }

        @Override public void useDouble(GameState state, int enemyIdx) {
            state.enemies.get(enemyIdx).applyDebuff(DebuffType.VULNERABLE, 6);
        }
    }

    public static class StrengthPotion extends Potion {
        public StrengthPotion() {
            canAffectPlayerStrength = true;
        }

        @Override public void use(GameState state, int enemyIdx) {
            state.player.gainStrength(2);
        }

        @Override public void useDouble(GameState state, int enemyIdx) {
            state.player.gainStrength(4);
        }
    }

    public static class DexterityPotion extends Potion {
        public DexterityPotion() {
            canAffectPlayerDexterity = true;
        }

        @Override public void use(GameState state, int enemyIdx) {
            state.player.gainDexterity(2);
        }

        @Override public void useDouble(GameState state, int enemyIdx) {
            state.player.gainDexterity(4);
        }
    }
}
