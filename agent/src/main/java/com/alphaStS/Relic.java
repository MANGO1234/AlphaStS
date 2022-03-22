package com.alphaStS;

public abstract class Relic implements GameProperties.CounterRegistrant {
    public boolean changePlayerStrength;
    public boolean changePlayerDexterity;
    public boolean vulnEnemy;
    public boolean weakEnemy;

    public void startOfGameSetup(GameState state) {}

    int counterIdx = -1;
    @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
        counterIdx = idx;
    }

    // **********************************************************************************************************************************************
    // ************************************************************* Common Relics ******************************************************************
    // **********************************************************************************************************************************************

    public static class Akabeko extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.buffs |= PlayerBuff.AKABEKO.mask();
            state.addOnCardPlayedHandler(new OnCardPlayedHandler() {
                @Override void handle(GameState state, Card card) {
                    if (card.cardType == Card.ATTACK) {
                        state.buffs &= ~PlayerBuff.AKABEKO.mask();
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

    public static class ArtOfWar extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addOnCardPlayedHandler(new OnCardPlayedHandler() {
                @Override void handle(GameState state, Card card) {
                    if (card.cardType == Card.ATTACK) {
                        state.buffs &= ~PlayerBuff.ART_OF_WAR.mask();
                    }
                }
            });
            state.addStartOfTurnHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
                    if (state.turnNum > 1 && (state.buffs & PlayerBuff.ART_OF_WAR.mask()) != 0) {
                        state.gainEnergy(1);
                    }
                    state.buffs |= PlayerBuff.ART_OF_WAR.mask();
                }
            });
        }
    }

    public static class BagOfMarbles extends Relic {
        public boolean vulnEnemy = true;

        @Override public void startOfGameSetup(GameState state) {
            for (Enemy enemy : state.enemies) {
                enemy.applyDebuff(DebuffType.VULNERABLE, 1);
            }
        }
    }

    public static class BagOfPreparation extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addStartOfTurnHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
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
            state.buffs |= PlayerBuff.CENTENNIAL_PUZZLE.mask();
            state.addOnDamageHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
                    if ((state.buffs & PlayerBuff.CENTENNIAL_PUZZLE.mask()) != 0) {
                        state.buffs &= ~PlayerBuff.CENTENNIAL_PUZZLE.mask();
                        state.draw(3);
                    }
                }
            });
        }
    }

    // Ceramic Fish: No need to implement
    // Dream Catcher: No need to implement

    public static class HappyFlower extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("HappyFlower", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] == 0 ? 3 : counter[counterIdx]) / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addStartOfTurnHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
                    var counter = state.getCounterForWrite();
                    counter[counterIdx]++;
                    if (counter[counterIdx] == 3) {
                        counter[counterIdx] = 0;
                        state.gainEnergy(1);
                    }
                }
            });
        }
    }

    // Juzu Bracelet: No need to implement

    public static class Lantern extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.energy += 1;
        }
    }

    // Maw Bank: No need to implement
    // Meal Ticket: No need to implement

    public static class Nunchaku extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Nunchaku", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler(new OnCardPlayedHandler() {
                @Override void handle(GameState state, Card card) {
                    if (card.cardType == Card.ATTACK) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 10) {
                            counter[counterIdx] = 0;
                            state.gainEnergy(1);
                        }
                    }
                }
            });
        }
    }

    // Omamori: No need to implement

    public static class Orichalcum extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
                    if (state.player.block == 0) {
                        state.player.gainBlockNotFromCardPlay(6);
                    }
                }
            });
        }
    }

    public static class PenNib extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("PenNib", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler(new OnCardPlayedHandler() {
                @Override void handle(GameState state, Card card) {
                    if (card.cardType == Card.ATTACK) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 10) {
                            counter[counterIdx] = 0;
                        }
                    }
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.penNibCounterIdx = idx;
        }
    }

    // Potion Belt: No need to implement

    // todo: Preserved Insect: implement?

    // Regal Pillow: No need to implement
    // Smiling Mask: No need to implement
    // Strawberry: No need to implement

    public static class TheBoot extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasBoot = true;
        }
    }

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

    public static class InkBottle extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("InkBottle", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler(new OnCardPlayedHandler() {
                @Override void handle(GameState state, Card card) {
                    var counter = state.getCounterForWrite();
                    counter[counterIdx]++;
                    if (counter[counterIdx] == 10) {
                        state.draw(1);
                        counter[counterIdx] = 0;
                    }
                }
            });
        }
    }

    public static class Kunai extends Relic {
        public boolean playerDexterityCanChange = true;

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Kunai", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler(new OnCardPlayedHandler() {
                @Override void handle(GameState state, Card card) {
                    if (card.cardType == Card.ATTACK) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 3) {
                            counter[counterIdx] = 0;
                            state.player.gainDexterity(1);
                        }
                    }
                }
            });
            state.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    public static class LetterOpener extends Relic {
        public boolean playerStrengthCanChange = true;

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("LetterOpener", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler(new OnCardPlayedHandler() {
                @Override void handle(GameState state, Card card) {
                    if (card.cardType == Card.SKILL) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 3) {
                            counter[counterIdx] = 0;
                            for (Enemy enemy : state.enemies) {
                                state.playerDoNonAttackDamageToEnemy(enemy, 5, true);
                            }
                        }
                    }
                }
            });
            state.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    // Matryoshka: No need to implement

    // todo: Meat on the Bone

    public static class MercuryHourglass extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addStartOfTurnHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
                    for (Enemy enemy : state.enemies) {
                        enemy.nonAttackDamage(3, true, state);
                    }
                }
            });
        }
    }

    // Molten Egg: No need to implement

    // todo: Mummified Egg

    public static class OrnamentalFan extends Relic {
        public boolean playerStrengthCanChange = true;

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("OrnamentalFan", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler(new OnCardPlayedHandler() {
                @Override void handle(GameState state, Card card) {
                    if (card.cardType == Card.ATTACK) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 3) {
                            counter[counterIdx] = 0;
                            state.player.gainBlock(4);
                        }
                    }
                }
            });
            state.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    // Pantograph: No need to implement
    // Pear: No need to implement
    // Question Card: No need to implement

    public static class Shuriken extends Relic {
        public boolean playerStrengthCanChange = true;

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Shuriken", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler(new OnCardPlayedHandler() {
                @Override void handle(GameState state, Card card) {
                    if (card.cardType == Card.ATTACK) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 3) {
                            counter[counterIdx] = 0;
                            state.player.gainStrength(1);
                        }
                    }
                }
            });
            state.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

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
            state.prop.hasCaliper = true;
        }
    }

    public static class OddlySmoothStone extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.player.gainDexterity(1);
        }
    }

    // todo: Captain Wheel
    // todo: Dead Branch

    public static class DuVuDoll extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            for (int i : state.deckArr) {
                if (state.prop.cardDict[i].cardType == Card.CURSE) {
                    state.player.gainStrength(1);
                }
            }
        }
    }

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

    public static class CoffeeDripper extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.energyRefill += 1;
        }
    }

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

    public static class PhilosophersStone extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.energyRefill += 1;
            for (Enemy enemy : state.enemies) {
                enemy.strength += 1;
            }
        }
    }

    // todo: Runic Dome

    public static class RunicPyramid extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasRunicPyramid = true;
        }
    }

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

    public static class RedMask extends Relic {
        public boolean weakEnemy = true;

        @Override public void startOfGameSetup(GameState state) {
            for (Enemy enemy : state.enemies) {
                enemy.applyDebuff(DebuffType.WEAK, 1);
            }
        }
    }

    // Spirit Poop: No need to implement
    // Ssserpent Head: No need to implement
    // todo: Warped Tongs
}
