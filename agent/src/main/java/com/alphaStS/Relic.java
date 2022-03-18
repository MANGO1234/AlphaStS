package com.alphaStS;

abstract class GameTrigger {
    abstract void act(GameState state);
}

abstract class onCardPlayedHandler {
    abstract void handle(GameState state, Card card);
}

public abstract class Relic {
    public void startOfGameSetup(GameState state) {}


    // **********************************************************************************************************************************************
    // ************************************************************* Common Relics ******************************************************************
    // **********************************************************************************************************************************************

    public static class Akabeko extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.buffs |= PlayerBuffs.AKABEKO;
            state.addOnCardPlayedHandler(new onCardPlayedHandler() {
                @Override void handle(GameState state, Card card) {
                    if (card.cardType == Card.ATTACK) {
                        state.buffs &= ~PlayerBuffs.AKABEKO;
                    }
                }
            });
        }
    }

    public static class Anchor extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.player.gainBlockNotFromCardPlay(10);
        }
    }

    public static class AncientTeaSet extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.energy += 2;
        }
    }

    // todo: Art of War

    public static class BagOfMarbles extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            for (Enemy enemy : state.enemies) {
                enemy.applyDebuff(DebuffType.VULNERABLE, 1);
            }
        }
    }

    public static class BagOfPreparation extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addStartOfTurnTrigger(new GameTrigger() {
                @Override void act(GameState state) {
                    if (state.turnNum == 1) {
                        state.draw(2);
                    }
                }
            });
        }
    }

    // Blood Vial: No need to implement

    public static class BronzeScales extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.thorn += 3;
        }
    }

    public static class CentennialPuzzle extends Relic {
        @Override public void startOfGameSetup(GameState state) {
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

    // Ceramic Fish: No need to implement
    // Dream Catcher: No need to implement

    // todo: Happy Flower

    // Juzu Bracelet: No need to implement

    public static class Lantern extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.energy += 1;
        }
    }

    // Maw Bank: No need to implement
    // Meal Ticket: No need to implement

    // todo: Nunchaku

    // Omamori: No need to implement

    public static class Orichalcum extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addPreEndOfTurnTrigger(new GameTrigger() {
                @Override void act(GameState state) {
                    if (state.player.block == 0) {
                        state.player.gainBlockNotFromCardPlay(6);
                    }
                }
            });
        }
    }

    // todo: Pen Nib

    // Potion Belt: No need to implement

    // todo: Preserved Insect: implement?

    // Regal Pillow: No need to implement
    // Smiling Mask: No need to implement
    // Strawberry: No need to implement

    // todo: The Boot

    // Tiny Chest: No need to implement

    // todo: Toy Ornithopter

    public static class Vajira extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.player.gainStrength(1);
        }
    }

    // War Paint: No need to implement
    // Whetstone: No need to implement

    // **********************************************************************************************************************************************
    // ************************************************************ Uncommon Relics *****************************************************************
    // **********************************************************************************************************************************************

    // todo: Blue Candle
    // todo: Bottled Flame
    // todo: Bottled Lightning
    // todo: Bottled Storm

    // Darkstone Periapt: No need to implement
    // Eternal Feather: No need to implement
    // Frozen Egg: No need to implement

    // todo: Gremlin Horn
    // todo: Horn Cleat
    // todo: Ink Bottle
    // todo: Kunai
    // todo: Letter Opener

    // Matryoshka: No need to implement

    // todo: Meat on the Bone

    public static class MercuryHourglass extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addStartOfTurnTrigger(new GameTrigger() {
                @Override void act(GameState state) {
                    for (Enemy enemy : state.enemies) {
                        enemy.nonAttackDamage(3, true, state);
                    }
                }
            });
        }
    }

    // Molten Egg: No need to implement

    // todo: Mummified Egg
    // todo: Ornamental Fan

    // Pantograph: No need to implement
    // Pear: No need to implement
    // Question Card: No need to implement

    // todo: Shuriken

    // Singing Bowl: No need to implement

    // todo: Strike Dummy
    // todo: Sundial

    // The Courier: No need to implement
    // Toxic Egg: No need to implement
    // White Beast Statue: No need to implement

    // **********************************************************************************************************************************************
    // ************************************************************** Rare Relics *******************************************************************
    // **********************************************************************************************************************************************

    // todo: Bird Faced Urn

    public static class Calipers extends Relic {

        @Override public void startOfGameSetup(GameState state) {
            state.buffs |= PlayerBuffs.CALIPERS;
        }
    }

    public static class OddlySmoothStone extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.player.gainDexterity(1);
        }
    }

    // todo: Captain Wheel
    // todo: Dead Branch
    // todo: Du-Vu Doll
    // todo: Fossilized Helix
    // todo: Gambling Chip
    // todo: Ginger
    // todo: Girya
    // todo: Ice Cream
    // todo: Incense Burner
    // todo: Lizard Tail
    // Mango: No need to implement
    // Old Coin: No need to implement
    // Peace Pipe: No need to implement
    // todo: Pocketwatch
    // Prayer Wheel: No need to implement
    // Shovel: No need to implement
    // todo: Stone Calendar
    // todo: Threadand Needle
    // todo: Torii
    // todo: Tungsten Rod
    // todo: Turnip
    // todo: Unceasing Top

    // **********************************************************************************************************************************************
    // ************************************************************** Shop Relics *******************************************************************
    // **********************************************************************************************************************************************
    // Winged Greaves: No need to implement
    // Cauldron: No need to implement
    // todo: Chemical X
    // todo: Clockwork Souvenir
    // Dollys Mirror: No need to implement
    // todo: Frozen Eye
    // todo: Hand Drill
    // Lee's Waffle: No need to implement
    // todo: Medical Kit
    // Membership Card: No need to implement
    // todo: Orange Pellets
    // Orrery: No need to implement
    // todo: Prismatic Shard: implement?!?
    // todo: Sling of Courage
    // todo: Strange Spoon
    // todo: The Abacus
    // todo: Toolbox


    // **********************************************************************************************************************************************
    // ************************************************************** Boss Relics *******************************************************************
    // **********************************************************************************************************************************************
    // Astrolabe: No need to implement
    // Black Star: No need to implement

    public static class BustedCrown extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.energyRefill += 1;
        }
    }

    // Calling Bell: No need to implement
    // todo: Coffee Dripper

    public static class CursedKey extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.energyRefill += 1;
        }
    }

    public static class Ectoplasm extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.energyRefill += 1;
        }
    }

    // Empty Cage: No need to implement

    public static class FusionHammer extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.energyRefill += 1;
        }
    }

    // Pandora's Box: No need to implement
    // todo: Philosopher's Stone
    // todo: Runic Dome
    // todo: Runic Pyramid
    // todo: Sacred Bark
    // todo: Slavers Collar
    // todo: Snecko Eye

    public static class Sozu extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.energyRefill += 1;
        }
    }

    // Tiny House: No need to implement
    // todo: Velvet Choker

    // **********************************************************************************************************************************************
    // ************************************************************** Event Relics ******************************************************************
    // **********************************************************************************************************************************************

    // Bloody Idol: No need to implement, well can get gold in combat...
    // Cultist Mask: No need to implement
    // todo: Enchiridion
    // Face Of Cleric: No need to implement
    // Golden Idol: No need to implement
    // todo: Gremlin Visage
    // todo: Mark of The Bloom
    // todo: Mutagenic Strength
    // Nloth's Gift: No need to implement
    // Nloth's Mask: No need to implement
    // todo: Necronomicon
    // Neows Blessing: No need to implement
    // todo: Nilrys Codex
    // todo: Odd Mushroom
    // todo: Red Mask
    // Spirit Poop: No need to implement
    // Ssserpent Head: No need to implement
    // todo: Warped Tongs
}
