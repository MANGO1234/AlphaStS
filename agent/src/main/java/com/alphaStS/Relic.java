package com.alphaStS;

abstract class GameTrigger {
    abstract void act(GameState state);
}

public abstract class Relic {
    public void startOfGame(GameState state) {}

    public static class Orichalcum extends Relic {
        @Override public void startOfGame(GameState state) {
            state.addPreEndOfTurnTrigger(new GameTrigger() {
                @Override void act(GameState state) {
                    if (state.player.block == 0) {
                        state.player.gainBlock(6);
                    }
                }
            });
        }
    }

    public static class Vajira extends Relic {
        @Override public void startOfGame(GameState state) {
            state.player.gainStrength(1);
        }
    }

    public static class Anchor extends Relic {
        @Override public void startOfGame(GameState state) {
            state.player.gainBlockNoDex(10);
        }
    }

    public static class BronzeScales extends Relic {
        @Override public void startOfGame(GameState state) {
            state.thorn += 3;
        }
    }

    public static class RunicPyramid extends Relic {
        @Override public void startOfGame(GameState state) {
            state.buffs |= PlayerBuffs.RUNIC_PYRAMID;
            state.addOnDamageTrigger(new GameTrigger() {
                @Override void act(GameState state) {
                    if ((state.buffs & PlayerBuffs.RUNIC_PYRAMID) != 0) {
                        state.buffs &= ~PlayerBuffs.RUNIC_PYRAMID;
                        state.draw(3);
                    }
                }
            });
        }
    }
}
