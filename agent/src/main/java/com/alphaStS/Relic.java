package com.alphaStS;

import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyReadOnly;
import com.alphaStS.enums.OrbType;

import java.util.List;
import java.util.Objects;

public abstract class Relic implements GameProperties.CounterRegistrant, GameProperties.TrainingTargetRegistrant {
    public boolean changePlayerStrength;
    public boolean changePlayerDexterity;
    public boolean vulnEnemy;
    public boolean weakEnemy;
    public boolean healPlayer;

    public void startOfGameSetup(GameState state) {}
    List<Card> getPossibleGeneratedCards(List<Card> cards) { return List.of(); }

    int counterIdx = -1;
    @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
        counterIdx = idx;
    }
    int vArrayIdx = -1;
    @Override public void setVArrayIdx(int idx) {
        vArrayIdx = idx;
    }

    private static boolean isEliteFight(GameState state) {
        for (var enemy : state.getEnemiesForRead()) {
            if (enemy.property.isElite) {
                return true;
            }
        }
        return false;
    }

    // ************************************************************* Common Relics ******************************************************************
    // **********************************************************************************************************************************************

    public static class Akabeko extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.buffs |= PlayerBuff.AKABEKO.mask();
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card, int lastIdx, boolean cloned) {
                    if (card.cardType == Card.ATTACK) {
                        state.buffs &= ~PlayerBuff.AKABEKO.mask();
                    }
                }
            });
        }
    }

    public static class Anchor extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.getPlayerForWrite().gainBlockNotFromCardPlay(10);
        }
    }

    public static class AncientTeaSet extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.energy += 2;
                }
            });
        }
    }

    public static class ArtOfWar extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card, int lastIdx, boolean cloned) {
                    if (card.cardType == Card.ATTACK) {
                        state.buffs &= ~PlayerBuff.ART_OF_WAR.mask();
                    }
                }
            });
            state.addStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum > 1 && (state.buffs & PlayerBuff.ART_OF_WAR.mask()) != 0) {
                        state.gainEnergy(1);
                    }
                    state.buffs |= PlayerBuff.ART_OF_WAR.mask();
                }
            });
        }
    }

    public static class BagOfMarbles extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            vulnEnemy = true;
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        enemy.applyDebuff(state, DebuffType.VULNERABLE, 1);
                    }
                }
            });
        }
    }

    public static class BagOfPreparation extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addStartOfTurnHandler("BagOfPreparation", new GameEventHandler() {
                @Override public void handle(GameState state) {
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
            state.prop.registerCounter("Thorn", this, null);
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] += 3;
                }
            });
            state.addOnDamageHandler(new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (isAttack && source instanceof EnemyReadOnly enemy2) {
                        var idx = state.getEnemiesForRead().find(enemy2);
                        var enemy = state.getEnemiesForWrite().getForWrite(idx);
                        state.playerDoNonAttackDamageToEnemy(enemy, state.getCounterForRead()[counterIdx], true);
                    }
                }
            });
        }
    }

    public static class CentennialPuzzle extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.buffs |= PlayerBuff.CENTENNIAL_PUZZLE.mask();
            state.addOnDamageHandler(new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (damageDealt <= 0) return;
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
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] == 0 ? 3 : counter[counterIdx]) / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
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
        int n;
        int healthReward;

        public Nunchaku(int n, int healthReward) {
            this.n = n;
            this.healthReward = healthReward;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Nunchaku", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card, int lastIdx, boolean cloned) {
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
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = n;
                }
            });
            state.prop.addExtraTrainingTarget("Nunchaku", this, new TrainingTarget() {
                @Override public void fillVArray(GameState state, double[] v, boolean enemiesAllDead) {
                    if (enemiesAllDead) {
                        v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getCounterForRead()[counterIdx] / 9.0;
                    } else {
                        v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getVOther(vArrayIdx);
                    }
                }

                @Override public void updateQValues(GameState state, double[] v) {
                    v[GameState.V_HEALTH_IDX] += healthReward * v[GameState.V_OTHER_IDX_START + vArrayIdx] / state.getPlayeForRead().getMaxHealth();
                }
            });
        }

        @Override public void setCounterIdx(GameProperties properties, int counterIdx) {
            super.setCounterIdx(properties, counterIdx);
            properties.nunchakuCounterIdx = counterIdx;
        }
    }

    // Omamori: No need to implement

    public static class Orichalcum extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getPlayeForRead().getBlock() == 0) {
                        state.getPlayerForWrite().gainBlockNotFromCardPlay(6);
                    }
                }
            });
        }
    }

    public static class PenNib extends Relic {
        int n;
        int healthReward;

        public PenNib(int n, int healthReward) {
            this.n = n;
            this.healthReward = healthReward;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("PenNib", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card, int lastIdx, boolean cloned) {
                    if (card.cardType == Card.ATTACK) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 10) {
                            counter[counterIdx] = 0;
                        }
                    }
                }
            });
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = n;
                }
            });
            if (healthReward > 0) {
                state.prop.addExtraTrainingTarget("PenNib", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, double[] v, boolean enemiesAllDead) {
                        if (enemiesAllDead) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getCounterForRead()[counterIdx] / 9.0;
                        } else {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getVOther(vArrayIdx);
                        }
                    }

                    @Override public void updateQValues(GameState state, double[] v) {
                        v[GameState.V_HEALTH_IDX] += healthReward * v[GameState.V_OTHER_IDX_START + vArrayIdx] / state.getPlayeForRead().getMaxHealth();
                    }
                });
            }
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.penNibCounterIdx = idx;
        }
    }

    // Potion Belt: No need to implement

    public static class PreservedInsect extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isEliteFight(state)) {
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            enemy.setHealth(enemy.getHealth() * 3 / 4);
                        }
                    }
                }
            });
        }
    }

    // Regal Pillow: No need to implement
    // Smiling Mask: No need to implement
    // Strawberry: No need to implement

    public static class TheBoot extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasBoot = true;
        }
    }

    // Tiny Chest: No need to implement


    public static class ToyOrnithopter extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasToyOrniphopter = true;
            // technically heals, but we don't actually want to wait for it
        }
    }

    public static class Vajira extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.getPlayerForWrite().gainStrength(1);
        }
    }

    // War Paint: No need to implement
    // Whetstone: No need to implement

    // **********************************************************************************************************************************************
    // ************************************************************ Uncommon Relics *****************************************************************
    // **********************************************************************************************************************************************

    public static class BlueCandle extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasBlueCandle = true;
        }
    }

    private static class _BottledRelic extends Relic {
        private final Card card;

        public _BottledRelic(Card card) {
            this.card = card;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getDrawOrderForWrite().pushOnTop(state.prop.findCardIndex(card));
                }
            });
        }
    }

    public static class BottledFlame extends _BottledRelic {
        public BottledFlame(Card card) {
            super(card);
        }
    }

    public static class BottledLightning extends _BottledRelic {
        public BottledLightning(Card card) {
            super(card);
        }
    }

    public static class BottledStorm extends _BottledRelic {
        public BottledStorm(Card card) {
            super(card);
        }
    }

    // Darkstone Periapt: No need to implement
    // Eternal Feather: No need to implement
    // Frozen Egg: No need to implement

    public static class GremlinHorn extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addOnEnemyDeathHandler("GremlinHorn", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.gainEnergy(1);
                    state.draw(1);
                }
            });
        }
    }

    public static class HornCleat extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.addNNInputHandler("HornCleat", new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.turnNum <= 2 ? state.turnNum / 2.0f : -0.5f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addStartOfTurnHandler("HornCleat", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 2) {
                        state.getPlayerForWrite().gainBlock(14);
                    }
                }
            });
        }
    }

    public static class InkBottle extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("InkBottle", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card, int lastIdx, boolean cloned) {
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
        @Override public void startOfGameSetup(GameState state) {
            changePlayerDexterity = true;
            state.prop.registerCounter("Kunai", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card, int lastIdx, boolean cloned) {
                    if (card.cardType == Card.ATTACK) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 3) {
                            counter[counterIdx] = 0;
                            state.getPlayerForWrite().gainDexterity(1);
                        }
                    }
                }
            });
            state.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    public static class LetterOpener extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("LetterOpener", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card, int lastIdx, boolean cloned) {
                    if (card.cardType == Card.SKILL) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 3) {
                            counter[counterIdx] = 0;
                            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                                state.playerDoNonAttackDamageToEnemy(enemy, 5, true);
                            }
                        }
                    }
                }
            });
            state.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    // Matryoshka: No need to implement

    public static class MeatOnTheBone extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasMeatOnBone = true;
            state.addEndOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getPlayeForRead().getHealth() <= state.getPlayeForRead().getMaxHealth() / 2) {
                        state.getPlayerForWrite().heal(12);
                    }
                }
            });
        }
    }

    public static class MercuryHourglass extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        state.playerDoNonAttackDamageToEnemy(enemy, 3, true);
                    }
                }
            });
        }
    }

    // Molten Egg: No need to implement

    // todo: Mummified Egg

    public static class OrnamentalFan extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("OrnamentalFan", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card, int lastIdx, boolean cloned) {
                    if (card.cardType == Card.ATTACK) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 3) {
                            counter[counterIdx] = 0;
                            state.getPlayerForWrite().gainBlockNotFromCardPlay(4);
                        }
                    }
                }
            });
            state.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    // Pantograph: No need to implement
    // Pear: No need to implement
    // Question Card: No need to implement

    public static class Shuriken extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            changePlayerStrength = true;
            state.prop.registerCounter("Shuriken", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card, int lastIdx, boolean cloned) {
                    if (card.cardType == Card.ATTACK) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 3) {
                            counter[counterIdx] = 0;
                            state.getPlayerForWrite().gainStrength(1);
                        }
                    }
                }
            });
            state.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
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

    public static class BirdFacedUrn extends Relic {
        public BirdFacedUrn() {
            healPlayer = true;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card, int lastIdx, boolean cloned) {
                    if (card.cardType == Card.POWER) {
                        state.healPlayer(2);
                    }
                }
            });
        }
    }

    public static class Calipers extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasCaliper = true;
        }
    }

    public static class OddlySmoothStone extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.getPlayerForWrite().gainDexterity(1);
        }
    }

    // todo: Captain Wheel
    // todo: Dead Branch

    public static class DuVuDoll extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            for (int i : state.deckArr) {
                if (state.prop.cardDict[i].cardType == Card.CURSE) {
                    state.getPlayerForWrite().gainStrength(1);
                }
            }
        }
    }

    // todo: Fossilized Helix
    // todo: Gambling Chip

    public static class Ginger extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasGinger = true;
        }
    }

    public static class Girya extends Relic {
        private final int strength;

        public Girya(int strength) {
            this.strength = strength;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.getPlayerForWrite().gainStrength(strength);
        }
    }

    public static class IceCream extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasIceCream = true;
        }
    }

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

    public static class Torii extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasTorri = true;
        }
    }

    public static class TungstenRod extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasTungstenRod = true;
        }
    }

    public static class Turnip extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasTurnip = true;
        }
    }

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

    public static class MedicalKit extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasMedicalKit = true;
        }
    }

    // Membership Card: No need to implement
    // todo: Orange Pellets
    // Orrery: No need to implement
    // todo: Prismatic Shard: implement?!?

    public static class SlingOfCourage extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            if (isEliteFight(state)) {
                state.getPlayerForWrite().gainStrength(2);
            }
        }
    }

    public static class StrangeSpoon extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasStrangeSpoon = true;
        }
    }

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
            var enemies = state.getEnemiesForWrite();
            for (int i = 0; i < enemies.size(); i++) {
                enemies.getForWrite(i).gainStrength(1);
            }
        }
    }

    public static class RunicDome extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.energyRefill += 1;
            state.prop.hasRunicDome = true;
        }
    }

    public static class RunicPyramid extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasRunicPyramid = true;
        }
    }


    public static class SacredBark extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasSacredBark = true;
        }
    }

    public static class SlaversCollar extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            if (isEliteFight(state)) {
                state.energyRefill += 1;
            }
        }
    }

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

    public static class OddMushroom extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasOddMushroom = true;
        }
    }

    public static class RedMask extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            weakEnemy = true;
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        enemy.applyDebuff(state, DebuffType.WEAK, 1);
                    }
                }
            });
        }
    }

    // Spirit Poop: No need to implement
    // Ssserpent Head: No need to implement

    public static class WarpedTongs extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int nonUpgradedCardCount = 0;
                    for (int i = 0; i < state.prop.upgradeIdxes.length; i++) {
                        if (state.hand[i] > 0 && state.prop.upgradeIdxes[i] >= 0) {
                            nonUpgradedCardCount += state.hand[i];
                        }
                    }
                    if (nonUpgradedCardCount == 0) {
                        return;
                    }
                    int r = state.getSearchRandomGen().nextInt(nonUpgradedCardCount, RandomGenCtx.WarpedTongs, state);
                    int acc = 0;
                    for (int i = 0; i < state.prop.upgradeIdxes.length; i++) {
                        if (state.hand[i] > 0 && state.prop.upgradeIdxes[i] >= 0) {
                            acc += state.hand[i];
                            if (acc > r) {
                                if (state.hand[i] != nonUpgradedCardCount) {
                                    state.setIsStochastic();
                                }
                                state.removeCardFromHand(i);
                                state.addCardToHand(state.prop.upgradeIdxes[i]);
                                break;
                            }
                        }
                    }
                }
            });
        }

        @Override List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return cards.stream().map((x) -> CardUpgrade.map.get(x)).filter(Objects::nonNull).toList();
        }
    }

    // **********************************************************************************************************************************************
    // ******************************************************** Ironclad Specific Relics ************************************************************
    // **********************************************************************************************************************************************

    public static class BurningBlood extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasBurningBlood = true;
            state.addEndOfBattleHandler("BurningBlood", new GameEventHandler(1) {
                @Override public void handle(GameState state) {
                    state.healPlayer(6);
                }
            });
        }
    }

    public static class RingOfSerpant extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addStartOfTurnHandler("RingOfSerpant", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 1) {
                        var idx = state.prop.findCardIndex(new CardSilent.Survivor());
                        if (idx >= 0 && (state.hand[idx] > 0 || state.deck[idx] > 0)) {
                            state.draw(2);
                        }
                    }
                }
            });
        }
    }

    public static class RedSkull extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addOnDamageHandler(new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (state.getPlayeForRead().getHealth() <= state.getPlayeForRead().getMaxHealth() / 2) {
                        if (state.getPlayeForRead().getHealth() + damageDealt > state.getPlayeForRead().getMaxHealth() / 2) {
                            state.getPlayerForWrite().gainStrength(3);
                        }
                    }
                }
            });
            state.addOnHealHandler(new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int healed) {
                    if (state.getPlayeForRead().getHealth() > state.getPlayeForRead().getMaxHealth() / 2) {
                        if (state.getPlayeForRead().getHealth() - healed <= state.getPlayeForRead().getMaxHealth() / 2) {
                            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_STRENGTH, 3);
                        }
                    }
                }
            });
        }
    }

    public static class PaperPhrog extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasPaperPhrog = true;
        }
    }

    public static class ChampionBelt extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.hasChampionBelt = true;
        }
    }

    // **********************************************************************************************************************************************
    // ********************************************************** Defect Specific Relics ************************************************************
    // **********************************************************************************************************************************************

    public static class CrackedOrb extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.channelOrb(OrbType.LIGHTNING);
                }
            });
        }
    }

    public static class DataDisk extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.gainFocus(1);
                }
            });
        }
    }

    public static class RunicCapacitor extends Relic {
        @Override public void startOfGameSetup(GameState state) {
            state.prop.maxNumOfOrbs = Math.max(state.prop.maxNumOfOrbs, 6);
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.gainOrbSlot(3);
                }
            });
        }
    }
}
