package com.alphaStS;

abstract class GameTrigger {
    abstract void act(GameState state);
}

public abstract class Relic {
    public void startOfGame(GameState state) {}

    // Akkebeko

    public static class Anchor extends Relic {
        @Override public void startOfGame(GameState state) {
            state.player.gainBlockNoDex(10);
        }
    }

    // Ancient Tea Set
    // Art of War
    // Bag of Marbles

    public static class BagOfPreparation extends Relic {
        @Override public void startOfGame(GameState state) {
            state.addStartOfTurnTrigger(new GameTrigger() {
                @Override void act(GameState state) {
                    if (state.turnNum == 1) {
                        state.draw(2);
                    }
                }
            });
        }
    }

    // Blood Vial: No Need to implement

    public static class BronzeScales extends Relic {
        @Override public void startOfGame(GameState state) {
            state.thorn += 3;
        }
    }

    public static class CentennialPuzzle extends Relic {
        @Override public void startOfGame(GameState state) {
            state.buffs |= PlayerBuffs.CENTENNIAL_PUZZLE;
            state.addOnDamageTrigger(new GameTrigger() {
                @Override void act(GameState state) {
                    if ((state.buffs & PlayerBuffs.CENTENNIAL_PUZZLE) != 0) {
                        state.buffs &= ~PlayerBuffs.CENTENNIAL_PUZZLE;
                        state.draw(3);
                    }
                }
            });
        }
    }

    // Ceramic Fish: No Need to implement
    // Dream Catcher: No Need to implement

    // Happy Flower

    // Juzu Bracelet: No Need to implement

    // Lantern

    // Maw Bank: No Need to implement
    // Meal Ticket: No Need to implement

    // Nunchaku

    public static class Calipers extends Relic {

        @Override public void startOfGame(GameState state) {
            state.buffs |= PlayerBuffs.CALIPERS;
        }
    }

    public static class OddlySmoothStone extends Relic {
        @Override public void startOfGame(GameState state) {
            state.player.gainDexterity(1);
        }
    }

    // Omamori: No Need to implement

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

    // Pen Nib

    // Potion Belt: No Need to implement

    // Preserved Insect: implement?

    // Regal Pillow: No Need to implement
    // Smiling Mask: No Need to implement
    // Strawberry: No Need to implement

    // The Boot

    // Tiny Chest: No Need to implement

    // Toy Ornithopter

    public static class Vajira extends Relic {
        @Override public void startOfGame(GameState state) {
            state.player.gainStrength(1);
        }
    }

    // War Paint: No Need to implement
    // Whetstone: No Need to implement

    // Blue Candle
    // Bottled Flame
    // Bottled Lightning
    // Bottled Storm

    // Darkstone Periapt: No Need to implement
    // Eternal Feather: No Need to implement
    // Frozen Egg: No Need to implement

    // Gremlin Horn
    // Horn Cleat
    // Ink Bottle
    // Kunai
    // Letter Opener

    // Matryoshka: No Need to implement

    // Meat on the Bone
    // Mercury Hourglass

    // Molten Egg: No Need to implement
}
