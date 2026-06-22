package com.alphaStS.entity;

import com.alphaStS.*;
import com.alphaStS.card.CardColorless2;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.enums.OrbType;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.eventHandler.OnCardCreationHandler;
import com.alphaStS.eventHandler.OnDamageHandler;
import com.alphaStS.eventHandler.OnStarChangeHandler;
import com.alphaStS.random.RandomGenCtx;
import com.alphaStS.utils.CounterStat;

import com.alphaStS.card.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Relic2 {
    // **************************************************************************************************
    // ********************************************* Common *********************************************
    // **************************************************************************************************

    // No need to implement Amethyst Aubergine: Enemies drop 10 additional Gold.

    // Anchor (Common)
    //   Effect: Start each combat with 10 Block.
    public static class Anchor extends Relic.Anchor {
    }

    // Bag of Preparation (Common)
    //   Effect: At the start of each combat, draw 2 additional cards.
    public static class BagOfPreparation extends Relic.BagOfPreparation {
    }

    // Blood Vial (Common)
    //   Effect: At the start of each combat, heal 2 HP.
    public static class BloodVial extends Relic.BloodVial {
    }

    // No need to implement Book of Five Rings: Every 5 cards you add to your Deck, heal 15 HP.

    // Bronze Scales (Common)
    //   Effect: Start each combat with 3 Thorns.
    public static class BronzeScales extends Relic.BronzeScales {
    }

    // Centennial Puzzle (Common)
    //   Effect: The first time you lose HP each combat, draw 3 cards.
    public static class CentennialPuzzle extends Relic.CentennialPuzzle {
    }

    // Festive Popper (Common)
    //   Effect: At the start of each combat, deal 9 damage to ALL enemies.
    public static class FestivePopper extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("FestivePopper", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            state.playerDoNonAttackDamageToEnemy(enemy, 9, true);
                        }
                    }
                }
            });
        }
    }

    // Gorget (Common)
    //   Effect: At the start of each combat, gain 4 Plating.
    public static class Gorget extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerPlatingCounter();
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getCounterForWrite()[state.properties.platingCounterIdx] += 4;
                    }
                }
            });
        }
    }

    // Happy Flower (Common)
    //   Effect: Every 3 turns, gain energy.
    public static class HappyFlower extends Relic.HappyFlower {
        public HappyFlower(int n, int healthReward) {
            super(n, healthReward);
        }
    }

    // No need to implement Juzu Bracelet: Regular enemy combats are no longer encountered in ? rooms.

    // Lantern (Common)
    //   Effect: Start each combat with an additional energy.
    public static class Lantern extends Relic.Lantern {
    }

    // No need to implement Meal Ticket: Whenever you enter a shop room, heal 15 HP.

    // Oddly Smooth Stone (Common)
    //   Effect: Start each combat with 1 Dexterity.
    public static class OddlySmoothStone extends Relic.OddlySmoothStone {
    }

    // Pendulum (Common)
    //   Effect: Whenever you shuffle your Draw Pile, draw a card.
    public static class Pendulum extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnShuffleHandler("Pendulum", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.draw(1);
                    }
                }
            });
        }
    }

    // Permafrost (Common)
    //   Effect: The first time you play a Powers each combat, gain 6 Block.
    public static class Permafrost extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Permafrost", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx];
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (!isRelicEnabledInScenario(state)) {
                        return;
                    }
                    if (state.properties.cardDict[cardIdx].cardType == Card.POWER && state.getCounterForRead()[counterIdx] == 0) {
                        state.getCounterForWrite()[counterIdx] = 1;
                        state.playerGainBlockNotFromCardPlay(6);
                    }
                }
            });
        }
    }

    // Potion Belt (Common)
    //   Effect: Upon pickup, gain 2 potion slots.
    public static class PotionBelt extends Relic.PotionBelt {
    }

    // No need to implement Regal Pillow: Whenever you Rest, heal an additional 15 HP.

    // No need to implement Strawberry: Upon pickup, raise your Max HP by 7.

    // Strike Dummy (Common)
    //   Effect: Cards containing “Strike” deal 3 additional damage.
    public static class StrikeDummy extends Relic.StrikeDummy {
    }

    // No need to implement Tiny Mailbox: Whenever you Rest, procure a random potion.

    // Vajra (Common)
    //   Effect: Start each combat with 1 Strength.
    public static class Vajra extends Relic.Vajra {
    }

    // Venerable Tea Set (Common)
    //   Effect: Whenever you enter a Rest Site, start the next combat with an additional 2 energy.
    public static class VenerableTeaSet extends Relic.AncientTeaSet {
    }

    // War Paint (Common)
    //   Effect: Upon pickup, Upgrade 2 random Skills.
    public static class WarPaint extends Relic.WarPaint {
    }

    // Whetstone (Common)
    //   Effect: Upon pickup, Upgrade 2 random Attacks.
    public static class Whetstone extends Relic.WhetStone {
    }

    // **************************************************************************************************
    // ********************************************* Uncommon *********************************************
    // **************************************************************************************************

    // Akabeko (Uncommon)
    //   Effect: At the start of each combat, gain 8 Vigor.
    public static class Akabeko extends Relic.Akabeko {
    }

    // Bag of Marbles (Uncommon)
    //   Effect: At the start of each combat, apply 1 Vulnerable to ALL enemies.
    public static class BagOfMarbles extends Relic.BagOfMarbles {
    }

    // Bellows (Uncommon)
    //   Effect: The first Hand you draw each combat is Upgraded.
    public static class Bellows extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Bellows", this, null);
            state.properties.addStartOfTurnHandler("Bellows", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state) && state.turnNum == 1) {
                        state.getCounterForWrite()[counterIdx] = 1;
                    }
                }
            });
            state.properties.addOnCardDrawnHandler("Bellows", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (!isRelicEnabledInScenario(state) || state.getCounterForRead()[counterIdx] != 0) {
                        return;
                    }
                    int upgIdx = state.properties.upgradeIdxes[cardIdx];
                    if (upgIdx >= 0) {
                        for (int i = state.handArrLen - 1; i >= 0; i--) {
                            if (state.handArr[i] == cardIdx) {
                                state.getHandArrForWrite()[i] = (short) upgIdx;
                                break;
                            }
                        }
                    }
                }
            });
        }
    }

    // No need to implement Bowler Hat: Gain 20% additional Gold.

    // Candelabra (Uncommon)
    //   Effect: At the start of your 2nd turn, gain 2 energy.
    public static class Candelabra extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addNNInputHandler("Candelabra", new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.turnNum <= 2 ? state.turnNum / 2.0f : -0.5f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Candelabra", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 2 && isRelicEnabledInScenario(state)) {
                        state.gainEnergy(2);
                    }
                }
            });
        }
    }

    // No need to implement Eternal Feather: For every 5 cards in your Deck, heal 3 HP whenever you enter a Rest Site.

    // Gremlin Horn (Uncommon)
    //   Effect: Whenever an enemy dies, gain energy and draw 1 card.
    public static class GremlinHorn extends Relic.GremlinHorn {
    }

    // Horn Cleat (Uncommon)
    //   Effect: At the start of your 2nd turn, gain 14 Block.
    public static class HornCleat extends Relic.HornCleat {
    }

    // Joss Paper (Uncommon)
    //   Effect: Every 5 times you Exhaust a card, draw 1 card.
    public static class JossPaper extends Relic {
        int healthReward;

        public JossPaper(int healthReward) {
            this.healthReward = healthReward;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("JossPaper", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            }, true);
            state.properties.addOnExhaustHandler("JossPaper", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state)) {
                        return;
                    }
                    var counter = state.getCounterForWrite();
                    counter[counterIdx]++;
                    if (counter[counterIdx] == 5) {
                        counter[counterIdx] = 0;
                        state.draw(1);
                    }
                }
            });
            if (healthReward > 0) {
                state.properties.addExtraTrainingTarget("JossPaper", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                        if (isTerminal > 0) {
                            v.setVExtra(vExtraIdx, state.getCounterForRead()[counterIdx] / 4.0);
                        } else if (isTerminal == 0) {
                            v.setVExtra(vExtraIdx, state.getVExtra(vExtraIdx));
                        }
                    }

                    @Override public void updateQValues(GameState state, VArray v) {
                        v.add(GameState.V_HEALTH_IDX, healthReward * v.getVExtra(vExtraIdx) / state.getPlayerForRead().getMaxHealth());
                    }
                });
            }
        }

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, "Joss Paper");
        }
    }

    // Kusarigama (Uncommon)
    //   Effect: Every time you play 3 Attacks in a single turn, deal 6 damage to a random enemy.
    public static class Kusarigama extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Kusarigama", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (!isRelicEnabledInScenario(state)) {
                        return;
                    }
                    if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 3) {
                            counter[counterIdx] = 0;
                            int randomEnemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
                            if (randomEnemyIdx >= 0) {
                                state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(randomEnemyIdx), 6, true);
                                state.setIsStochastic();
                            }
                        }
                    }
                }
            });
            state.properties.addPreEndOfTurnHandler("Kusarigama", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, "Kusarigama");
        }
    }

    // Letter Opener (Uncommon)
    //   Effect: Every time you play 3 Skills in a single turn, deal 5 damage to ALL enemies.
    public static class LetterOpener extends Relic.LetterOpener {
    }

    // No need to implement Lucky Fysh: Whenever you add a card to your Deck, gain 15 Gold.

    // Mercury Hourglass (Uncommon)
    //   Effect: At the start of your turn, deal 3 damage to ALL enemies.
    public static class MercuryHourglass extends Relic.MercuryHourglass {
    }

    // Miniature Cannon (Uncommon)
    //   Effect: Upgraded Attacks deal 3 additional damage.
    public static class MiniatureCannon extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.miniatureCannon = this;
        }
    }

    // Nunchaku (Uncommon)
    //   Effect: Every time you play 10 Attacks, gain energy.
    public static class Nunchaku extends Relic.Nunchaku {
        public Nunchaku(int n, int healthReward) {
            super(n, healthReward);
        }
    }

    // Orichalcum (Uncommon)
    //   Effect: If you end your turn without Block, gain 6 Block.
    public static class Orichalcum extends Relic.Orichalcum {
    }

    // Ornamental Fan (Uncommon)
    //   Effect: Every time you play 3 Attacks in a single turn, gain 4 Block.
    public static class OrnamentalFan extends Relic.OrnamentalFan {
    }

    // Pantograph (Uncommon)
    //   Effect: At the start of each Boss combat, heal 25 HP.
    public static class Pantograph extends Relic.Pantograph {
    }

    // Parrying Shield (Uncommon)
    //   Effect: If you end a turn with at least 10 Block, deal 6 damage to a random enemy.
    public static class ParryingShield extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addPreEndOfTurnHandler("ParryingShield", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state)) {
                        return;
                    }
                    if (state.getPlayerForRead().getBlock() >= 10) {
                        int enemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
                        if (enemyIdx >= 0) {
                            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(enemyIdx), 6, true);
                        }
                    }
                }
            });
        }
    }

    // No need to implement Pear: Upon pickup, raise your Max HP by 10.

    // Pen Nib (Uncommon)
    //   Effect: Every 10th Attack you play deals double damage.
    public static class PenNib extends Relic.PenNib {
        public PenNib(int n, int healthReward) {
            super(n, healthReward);
        }
    }

    // TODO: Petrified Toad (Uncommon)
    //   Effect: At the start of each combat, procure a Potion-Shaped Rock.

    // No need to implement Planisphere: Whenever you enter a ? room, heal 4 HP.

    // Red Mask (Uncommon)
    //   Effect: At the start of each combat, apply 1 Weak to ALL enemies.
    public static class RedMask extends Relic.RedMask {
    }

    // Reptile Trinket (Uncommon)
    //   Effect: Whenever you use a potion, gain 3 Strength this turn.
    public static class ReptileTrinket extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnPotionUseHandler("ReptileTrinket", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getPlayerForWrite().gainStrength(3);
                        state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, 3);
                    }
                }
            });
        }
    }

    // Ripple Basin (Uncommon)
    //   Effect: If you did not play any Attacks during your turn, gain 4 Block.
    public static class RippleBasin extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("RippleBasin", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx];
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("RippleBasin", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (isRelicEnabledInScenario(state) && state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        state.getCounterForWrite()[counterIdx] = 1;
                    }
                }
            });
            state.properties.addPreEndOfTurnHandler("RippleBasin", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state) && state.getCounterForRead()[counterIdx] == 0) {
                        state.playerGainBlockNotFromCardPlay(4);
                    }
                }
            });
        }
    }

    // Sparkling Rouge (Uncommon)
    //   Effect: At the start of your 3rd turn, gain 1 Strength and 1 Dexterity.
    public static class SparklingRouge extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addNNInputHandler("SparklingRouge", new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.turnNum <= 3 ? state.turnNum / 3.0f : -0.5f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("SparklingRouge", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 3 && isRelicEnabledInScenario(state)) {
                        state.getPlayerForWrite().gainStrength(1);
                        state.getPlayerForWrite().gainDexterity(1);
                    }
                }
            });
        }
    }

    // Stone Cracker (Uncommon)
    //   Effect: At the start of Boss combats, Upgrade 3 random cards in your Draw Pile for the rest of combat.
    public static class StoneCracker extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("StoneCracker", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state) || !isBossFight(state)) {
                        return;
                    }
                    int count = 0;
                    int[] positions = new int[state.deckArrLen];
                    for (int i = 0; i < state.deckArrLen; i++) {
                        if (state.properties.upgradeIdxes[state.getDeckArrForRead()[i]] >= 0) {
                            positions[count++] = i;
                        }
                    }
                    if (count == 0) {
                        return;
                    }
                    int numToUpgrade = Math.min(count, 3);
                    if (count > 3) {
                        state.setIsStochastic();
                        for (int i = 0; i < 3; i++) {
                            int r = i + state.getSearchRandomGen().nextInt(count - i, RandomGenCtx.Other, state);
                            int tmp = positions[i];
                            positions[i] = positions[r];
                            positions[r] = tmp;
                        }
                    }
                    for (int i = 0; i < numToUpgrade; i++) {
                        int pos = positions[i];
                        state.getDeckArrForWrite()[pos] = (short) state.properties.upgradeIdxes[state.getDeckArrForRead()[pos]];
                    }
                }
            });
        }
    }

    // Tuning Fork (Uncommon)
    //   Effect: Every time you play 10 Skills, gain 7 Block.
    public static class TuningFork extends Relic {
        int n;
        int healthReward;

        public TuningFork(int n, int healthReward) {
            this.n = n;
            this.healthReward = healthReward;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("TuningFork", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            }, true);
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getCounterForWrite()[counterIdx] = n;
                    }
                }
            });
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (!isRelicEnabledInScenario(state)) {
                        return;
                    }
                    if (state.properties.cardDict[cardIdx].cardType == Card.SKILL) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 10) {
                            counter[counterIdx] = 0;
                            state.playerGainBlockNotFromCardPlay(7);
                        }
                    }
                }
            });
            if (healthReward > 0) {
                state.properties.addExtraTrainingTarget("TuningFork", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                        if (isTerminal > 0) {
                            v.setVExtra(vExtraIdx, state.getCounterForRead()[counterIdx] / 9.0);
                        } else if (isTerminal == 0) {
                            v.setVExtra(vExtraIdx, state.getVExtra(vExtraIdx));
                        }
                    }

                    @Override public void updateQValues(GameState state, VArray v) {
                        v.add(GameState.V_HEALTH_IDX, healthReward * v.getVExtra(vExtraIdx) / state.getPlayerForRead().getMaxHealth());
                    }
                });
            }
        }

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, "Tuning Fork");
        }
    }

    // Vambrace (Uncommon)
    //   Effect: The first time you gain Block from a card each combat, double the amount gained.
    public static class Vambrace extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.vambrace = this;
            state.properties.registerCounter("Vambrace", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx];
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int counterIdx) {
                    state.properties.vambraceCounterIdx = counterIdx;
                }
            });
            state.properties.addStartOfBattleHandler("Vambrace", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    // **************************************************************************************************
    // *********************************************  Rare  *********************************************
    // **************************************************************************************************

    // Art of War (Rare)
    //   Effect: If you do not play any Attacks during your turn, gain an additional energy next turn.
    public static class ArtOfWar extends Relic.ArtOfWar {
    }

    // Beating Remnant (Rare)
    //   Effect: You cannot lose more than 20 HP in a single turn.
    public static class BeatingRemnant extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.beatingRemnant = this;
            state.properties.registerCounter("BeatingRemnant", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int counterIdx) {
                    state.properties.beatingRemnantCounterIdx = counterIdx;
                }
            });
            state.properties.addStartOfTurnHandler("BeatingRemnant", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    // Captain's Wheel (Rare)
    //   Effect: At the start of your 3rd turn, gain 18 Block.
    public static class CaptainsWheel extends Relic.CaptainsWheel {
    }

    // Chandelier (Rare)
    //   Effect: At the start of your 3rd turn, gain 3 energy.
    public static class Chandelier extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addNNInputHandler("Chandelier", new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.turnNum <= 3 ? state.turnNum / 3.0f : -0.5f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Chandelier", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 3 && isRelicEnabledInScenario(state)) {
                        state.gainEnergy(3);
                    }
                }
            });
        }
    }

    // Cloak Clasp (Rare)
    //   Effect: At the end of your turn, gain 1 Block for each card in your Hand.
    public static class CloakClasp extends Relic.CloakClasp {
    }

    // No need to implement Frozen Egg: Whenever you add a Powers into your Deck, Upgrade it.

    // Gambling Chip (Rare)
    //   Effect: At the start of each combat, discard any number of cards then draw that many.
    public static class GamblingChip extends Relic.GamblingChip {
    }

    // Game Piece (Rare)
    //   Effect: Whenever you play a Power, draw 1 card.
    public static class GamePiece extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (isRelicEnabledInScenario(state) && state.properties.cardDict[cardIdx].cardType == Card.POWER) {
                        state.draw(1);
                    }
                }
            });
        }
    }

    // Girya (Rare)
    //   Effect: You can now gain Strength at Rest Sites. (3 times max)
    public static class Girya extends Relic.Girya {
        public Girya(int strength) {
            super(strength);
        }
    }

    // Ice Cream (Rare)
    //   Effect: Energy is now conserved between turns.
    public static class IceCream extends Relic.IceCream {
    }

    // Intimidating Helmet (Rare)
    //   Effect: Whenever you play a card that costs 2 energy or more, gain 4 Block.
    public static class IntimidatingHelmet extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (isRelicEnabledInScenario(state) && energyUsed >= 2) {
                        state.playerGainBlockNotFromCardPlay(4);
                    }
                }
            });
        }
    }

    // Kunai (Rare)
    //   Effect: Every time you play 3 Attacks in a single turn, gain 1 Dexterity.
    public static class Kunai extends Relic.Kunai {
    }

    // No need to implement Lasting Candy: Every other combat, your card rewards gain an additional Power.

    // Lizard Tail (Rare)
    //   Effect: When you would die, heal to 50% of your Max HP instead (works once).
    public static class LizardTail extends Relic.LizardTail {
    }

    // No need to implement Mango: Upon pickup, raise your Max HP by 14.

    // Meat on the Bone (Rare)
    //   Effect: If your HP is at or below 50% at the end of combat, heal 12 HP.
    public static class MeatOnTheBone extends Relic.MeatOnTheBone {
    }

    // No need to implement Molten Egg: Whenever you add an Attack card to your Deck, Upgrade it.

    // Mummified Hand (Rare)
    //   Effect: Whenever you play a Power, a random card in your Hand is free to play that turn.
    public static class MummifiedHand extends Relic.MummifiedHand {
    }

    // No need to implement Old Coin: Upon pickup, gain 300 Gold.

    // Pocketwatch (Rare)
    //   Effect: Whenever you play 3 or fewer cards during your turn, draw 3 additional cards at the start of your next turn.
    public static class Pocketwatch extends Relic.Pocketwatch {
    }

    // No need to implement Prayer Wheel: Normal enemies drop an additional card reward.

    // Rainbow Ring (Rare)
    //   Effect: The first time you play an Attack, Skill, and Powers each turn, gain 1 Strength and 1 Dexterity.
    public static class RainbowRing extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("RainbowRing", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead()[counterIdx];
                    input[idx] = (counter & 1) != 0 ? 0.1f : 0.0f;
                    input[idx + 1] = (counter & (1 << 1)) != 0 ? 0.1f : 0.0f;
                    input[idx + 2] = (counter & (1 << 2)) != 0 ? 0.1f : 0.0f;
                    return idx + 3;
                }

                @Override public int getInputLenDelta() {
                    return 3;
                }
            });
            state.properties.addOnCardPlayedHandler("RainbowRing", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (!isRelicEnabledInScenario(state) || state.getCounterForRead()[counterIdx] == 0b111) {
                        return;
                    }
                    var cardType = state.properties.cardDict[cardIdx].cardType;
                    if (cardType == Card.ATTACK) {
                        state.getCounterForWrite()[counterIdx] |= 1;
                    } else if (cardType == Card.SKILL) {
                        state.getCounterForWrite()[counterIdx] |= 1 << 1;
                    } else if (cardType == Card.POWER) {
                        state.getCounterForWrite()[counterIdx] |= 1 << 2;
                    }
                    if (state.getCounterForRead()[counterIdx] == 0b111) {
                        state.getPlayerForWrite().gainStrength(1);
                        state.getPlayerForWrite().gainDexterity(1);
                    }
                }
            });
            state.properties.addEndOfTurnHandler("RainbowRing", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    // Razor Tooth (Rare)
    //   Effect: Every time you play an Attack or Skill, Upgrade it for the remainder of combat.
    public static class RazorTooth extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.razorTooth = this;
        }
    }

    // No need to implement Shovel: You can now dig at Rest Sites to obtain a random Relic.

    // Shuriken (Rare)
    //   Effect: Every time you play 3 Attacks in a single turn, gain 1 Strength.
    public static class Shuriken extends Relic.Shuriken {
    }

    // Stone Calendar (Rare)
    //   Effect: At the end of turn 7, deal 52 damage to ALL enemies.
    public static class StoneCalendar extends Relic.StoneCalendar {
    }

    // Sturdy Clamp (Rare)
    //   Effect: Up to 10 Block persists across turns.
    public static class SturdyClamp extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.sturdyClamp = this;
        }
    }

    // No need to implement The Courier: The merchant no longer runs out of cards, relics, or potions and his prices are reduced by 20%.

    // No need to implement Toxic Egg: Whenever you add a Skill into your Deck, Upgrade it.

    // Tungsten Rod (Rare)
    //   Effect: Whenever you would lose HP, lose 1 less.
    public static class TungstenRod extends Relic.TungstenRod {
    }

    // Unceasing Top (Rare)
    //   Effect: Whenever you have no cards in Hand during your turn, draw a card.
    public static class UnceasingTop extends Relic.UnceasingTop {
    }

    // Unsettling Lamp (Rare)
    //   Effect: Each combat, the first time you play a card that Debuffs an enemy, double its effect.
    public static class UnsettlingLamp extends Relic {
        public UnsettlingLamp() {
            entityProperty.possibleBuffs |= PlayerBuff.UNSETTLING_LAMP.mask();
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.unsettlingLamp = this;
            state.properties.addStartOfBattleHandler("UnsettlingLamp", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.buffs |= PlayerBuff.UNSETTLING_LAMP.mask();
                    }
                }
            });
        }
    }

    // TODO: Vexing Puzzlebox (Rare)
    //   Effect: At the start of each combat, add a random card into your Hand. It costs 0 energy.

    // No need to implement White Beast Statue: Potions always appear in combat rewards.

    // No need to implement White Star: Elites drop an additional Rare card reward.

    // **************************************************************************************************
    // *********************************************  Shop  *********************************************
    // **************************************************************************************************

    // Belt Buckle (Shop)
    //   Effect: While you have no potions, you have 2 additional Dexterity.
    public static class BeltBuckle extends Relic {
        public BeltBuckle() {
            entityProperty.changePlayerDexterity = true;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("BeltBuckle", this, null);
            state.properties.addStartOfBattleHandler("BeltBuckle", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state)) {
                        return;
                    }
                    if (state.getPotionCount() == 0) {
                        state.getPlayerForWrite().gainDexterity(2);
                        state.getCounterForWrite()[counterIdx] = 1;
                    } else {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
            state.properties.addOnPotionUseHandler("BeltBuckle", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state) && state.getCounterForRead()[counterIdx] == 0 && state.getPotionCount() == 1) {
                        state.getPlayerForWrite().gainDexterity(2);
                        state.getCounterForWrite()[counterIdx] = 1;
                    }
                }
            });
            state.properties.addOnCardPlayedHandler("BeltBuckle", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (isRelicEnabledInScenario(state) && state.getCounterForRead()[counterIdx] == 1 && state.getPotionCount() > 0) {
                        state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_DEXTERITY, 2);
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    // Bread (Shop)
    //   Effect: At the start of your first turn, lose 2 energy. At the start of all other turns, gain energy.
    public static class Bread extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("Bread", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state)) {
                        return;
                    }
                    if (state.turnNum == 1) {
                        state.gainEnergy(-2);
                    } else {
                        state.gainEnergy(1);
                    }
                }
            });
        }
    }

    // Burning Sticks (Shop)
    //   Effect: The first time each combat you Exhaust a Skill, add a copy of it into your Hand.
    public static class BurningSticks extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("BurningSticks", this, null);
            state.properties.addStartOfBattleHandler("BurningSticks", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
            state.properties.addOnExhaustHandler("BurningSticks", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state) || state.getCounterForRead()[counterIdx] != 0) {
                        return;
                    }
                    int exhaustedCardIdx = state.exhaustArr[state.exhaustArrLen - 1];
                    if (state.properties.cardDict[exhaustedCardIdx].cardType == Card.SKILL) {
                        state.getCounterForWrite()[counterIdx] = 1;
                        state.addCardToHand(exhaustedCardIdx);
                    }
                }
            });
        }
    }

    // No need to implement Cauldron: Upon pickup, brews 5 random potions.

    // Chemical X (Shop)
    //   Effect: The effects of your cost X cards are increased by 2.
    public static class ChemicalX extends Relic.ChemicalX {
    }

    // No need to implement Dingy Rug: Card rewards can now contain Colorless cards.

    // No need to implement Dolly's Mirror: Upon pickup, obtain an additional copy of a card in your Deck.

    // No need to implement Dragon Fruit: Whenever you gain Gold, raise your Max HP by 1.

    // TODO: Ghost Seed (Shop)
    //   Effect: Strikes and Defends gain Ethereal.

    // TODO: Gnarled Hammer (Shop)
    //   Effect: Upon pickup, Enchant up to 3 Attacks with Sharp 3.

    // TODO: Kifuda (Shop)
    //   Effect: Upon pickup, Enchant up to 3 cards with Adroit.

    // No need to implement Lava Lamp: At the end of combat, Upgrade all card rewards if you took no damage.

    // No need to implement Lee's Waffle: Upon pickup, raise your Max HP by 7 and heal all of your HP.

    // No need to implement Membership Card: 50% discount on all products!

    // No need to implement Miniature Tent: You may choose any number of options at Rest Sites.

    // TODO: Mystic Lighter (Shop)
    //   Effect: Enchanted Attacks deal 9 additional damage.

    // No need to implement Orrery: Upon pickup, gain 5 card rewards.

    // TODO: Punch Dagger (Shop)
    //   Effect: Upon pickup, Enchant an Attack with Momentum 5.

    // Ringing Triangle (Shop)
    //   Effect: Retain your Hand on the first turn of combat.
    public static class RingingTriangle extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerRetainHandCounter(this);
            state.properties.addStartOfBattleHandler("RingingTriangle", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getCounterForWrite()[counterIdx] = 1;
                    }
                }
            });
        }
    }

    // TODO: Royal Stamp (Shop)
    //   Effect: Upon pickup, choose an Attack or Skill in your Deck to Enchant with Royally Approved.

    // Screaming Flagon (Shop)
    //   Effect: If you end your turn with no cards in your Hand, deal 20 damage to ALL enemies.
    public static class ScreamingFlagon extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addPreEndOfTurnHandler("ScreamingFlagon", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state) && state.handArrLen == 0) {
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            state.playerDoNonAttackDamageToEnemy(enemy, 20, true);
                        }
                    }
                }
            });
        }
    }

    // Sling of Courage (Shop)
    //   Effect: Start each Elite combat with 2 Strength.
    public static class SlingOfCourage extends Relic.SlingOfCourage {
    }

    // The Abacus (Shop)
    //   Effect: Whenever you shuffle your Draw Pile, gain 6 Block.
    public static class TheAbacus extends Relic.TheAbacus {
    }

    // Toolbox (Shop)
    //   Effect: At the start of each combat, choose 1 of 3 random Colorless cards and add the chosen card into your Hand.
    public static class Toolbox extends Relic.Toolbox {
    }

    // TODO: Wing Charm (Shop)
    //   Effect: A random card in each card reward is Enchanted with Swift 1.

    // **************************************************************************************************
    // ********************************************* Event  *********************************************
    // **************************************************************************************************

    // Anchor??? (Event)
    //   Effect: Start each combat with 4 Block.
    public static class AnchorQQQ extends Relic.Anchor {
        public AnchorQQQ() {
            super(4);
        }
    }

    // Big Mushroom (Event)
    //   Effect: Upon pickup, raise your Max HP by 20. At the start of each combat, draw 2 fewer cards.
    public static class BigMushroom extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.bigMushroom = this;
        }
    }

    // No need to implement Bing Bong: Whenever you add a card to your Deck, add one additional copy.

    // Blood Vial??? (Event)
    //   Effect: At the start of each combat, heal 1 HP.
    public static class BloodVialQQQ extends Relic.BloodVial {
        public BloodVialQQQ() {
            super(1);
        }
    }

    // Bone Tea (Event)
    //   Effect: At the start of the next combat, Upgrade your starting hand.
    public static class BoneTea extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("BoneTea", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 1 && isRelicEnabledInScenario(state)) {
                        state.handArrTransform(state.properties.upgradeIdxes);
                    }
                }
            });
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return cards.stream().map(Card::getUpgrade).filter(Objects::nonNull).toList();
        }
    }

    // Byrdpip (Event)
    //   Effect: Upon pickup, gain the card Byrd Swoop. A Byrdpip will accompany you in battles.
    public static class Byrdpip extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("Byrdpip", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.addCardToDeck(generatedCardIdx, false);
                    }
                }
            });
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.ByrdSwoop());
        }
    }

    // No need to implement Darkstone Periapt: Whenever you obtain a Curse, raise your Max HP by 6.

    // Daughter of the Wind (Event)
    //   Effect: Whenever you play an Attack, gain 1 Block.
    public static class DaughterOfTheWind extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnCardPlayedHandler("DaughterOfTheWind", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (isRelicEnabledInScenario(state) && state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        state.playerGainBlockNotFromCardPlay(1);
                    }
                }
            });
        }
    }

    // No need to implement Dream Catcher: Whenever you Rest, you may add a card to your Deck.

    // Ember Tea (Event)
    //   Effect: At the start of the next 5 combats, gain 2 Strength.
    public static class EmberTea extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("EmberTea", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 1 && isRelicEnabledInScenario(state)) {
                        state.getPlayerForWrite().gainStrength(2);
                    }
                }
            });
        }
    }

    // Forgotten Soul (Event)
    //   Effect: Whenever you Exhaust a card, deal 1 damage to a random enemy.
    public static class ForgottenSoul extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnExhaustHandler("ForgottenSoul", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state)) {
                        return;
                    }
                    int enemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
                    if (enemyIdx >= 0) {
                        state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(enemyIdx), 1, true);
                    }
                }
            });
        }
    }

    // No need to implement Fragrant Mushroom: Upon pickup, lose 15 HP and Upgrade 3 random cards.

    // TODO: Fresnel Lens (Event)
    //   Effect: Whenever you add a card that gains Block to your Deck, Enchant it with Nimble 2.

    // Hand Drill (Event)
    //   Effect: Whenever you break an enemy's Block, apply 2 Vulnerable.
    public static class HandDrill extends Relic.HandDrill {
    }

    // Happy Flower??? (Event)
    //   Effect: Every 5 turns, gain energy.
    public static class HappyFlowerQQQ extends Relic.HappyFlower {
        public HappyFlowerQQQ() {
            super(0, 0, 5);
            counterName = "HappyFlowerQQQ";
        }
    }

    // TODO: History Course (Event)
    //   Effect: At the start of your turn, play a copy of your last played Attack or Skill.

    // No need to implement Lee's Waffle???: Upon pickup, heal 10% of your HP.

    // Lost Wisp (Event)
    //   Effect: Whenever you play a Power, deal 8 damage to ALL enemies.
    public static class LostWisp extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnCardPlayedHandler("LostWisp", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (isRelicEnabledInScenario(state) && state.properties.cardDict[cardIdx].cardType == Card.POWER) {
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            state.playerDoNonAttackDamageToEnemy(enemy, 8, true);
                        }
                    }
                }
            });
        }
    }

    // No need to implement Mango???: Upon pickup, raise your Max HP by 3.

    // No need to implement Maw Bank: Whenever you climb a floor, gain 12 Gold. No longer works when you spend any Gold at the shop.

    // Mr. Struggles (Event)
    //   Effect: At the start of your turn, deal damage equal to the turn number to ALL enemies.
    public static class MrStruggles extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("MrStruggles", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            state.playerDoNonAttackDamageToEnemy(enemy, state.turnNum, true);
                        }
                    }
                }
            });
        }
    }

    // Orichalcum??? (Event)
    //   Effect: If you end your turn without Block, gain 3 Block.
    public static class OrichalcumQQQ extends Relic.Orichalcum {
        public OrichalcumQQQ() {
            super(3);
        }
    }

    // Pollinous Core (Event)
    //   Effect: Every 4 turns, draw 2 additional cards.
    public static class PollinousCore extends Relic {
        private final int healthReward;

        public PollinousCore() {
            this(0);
        }

        public PollinousCore(int healthReward) {
            this.healthReward = healthReward;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("PollinousCore", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 4.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            }, true);
            state.properties.addStartOfTurnHandler("PollinousCore", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state)) {
                        return;
                    }
                    var counter = state.getCounterForWrite();
                    counter[counterIdx]++;
                    if (counter[counterIdx] == 4) {
                        counter[counterIdx] = 0;
                        state.draw(2);
                    }
                }
            });
            if (healthReward > 0) {
                state.properties.addExtraTrainingTarget("PollinousCore", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                        if (isTerminal > 0) {
                            v.setVExtra(vExtraIdx, state.getCounterForRead()[counterIdx] / 3.0);
                        } else if (isTerminal == 0) {
                            v.setVExtra(vExtraIdx, state.getVExtra(vExtraIdx));
                        }
                    }

                    @Override public void updateQValues(GameState state, VArray v) {
                        v.add(GameState.V_HEALTH_IDX, healthReward * v.getVExtra(vExtraIdx) / state.getPlayerForRead().getMaxHealth());
                    }
                });
            }
        }

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, "Pollinous Core");
        }
    }

    // Royal Poison (Event)
    //   Effect: At the start of each combat, lose 4 HP.
    public static class RoyalPoison extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("RoyalPoison", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.doNonAttackDamageToPlayer(4, false, this);
                    }
                }
            });
        }
    }

    // Snecko Eye??? (Event)
    //   Effect: Start each combat Confused.
    public static class SneckoEyeQQQ extends Relic.SneckoEye {
    }

    // Strike Dummy??? (Event)
    //   Effect: Cards containing “Strike” deal 1 additional damage.
    public static class StrikeDummyQQQ extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.strikeDummyQQQ = this;
        }
    }

    // Sword of Jade (Event)
    //   Effect: Start each combat with 3 Strength.
    public static class SwordOfJade extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("SwordOfJade", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getPlayerForWrite().gainStrength(3);
                    }
                }
            });
        }
    }

    // No need to implement Sword of Stone: Transforms into a powerful Relic after defeating 5 Elites.

    // Tea of Discourtesy (Event)
    //   Effect: At the start of the next combat, shuffle 2 Dazed into your Draw Pile.
    public static class TeaOfDiscourtesy extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("TeaOfDiscourtesy", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.addCardToDeck(generatedCardIdx, false);
                        state.addCardToDeck(generatedCardIdx, false);
                    }
                }
            });
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardOther2.Dazed());
        }
    }

    // The Boot (Event)
    //   Effect: Whenever you would deal 4 or less unblocked attack damage, increase it to 5.
    public static class TheBoot extends Relic.TheBoot {
    }

    // No need to implement The Chosen Cheese: At the end of combat, gain 1 Max HP.

    // No need to implement The Merchant's Rug???: Poor imitation. Does nothing.

    // Venerable Tea Set??? (Event)
    //   Effect: Whenever you enter a Rest Site, start the next combat with an additional energy.
    public static class VenerableTeaSetQQQ extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("VenerableTeaSetQQQ", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.energyRefill += 1;
                    }
                }
            });
        }
    }

    // No need to implement Wongo Customer Appreciation Badge: Does nothing.

    // No need to implement Wongo's Mystery Ticket: Receive 3 random Relics after 5 combats.

    // **************************************************************************************************
    // ********************************************* Ancient *********************************************
    // **************************************************************************************************

    // No need to implement Alchemical Coffer: Upon pickup, gain 4 potion slots filled with random potions.

    // No need to implement Arcane Scroll: Upon pickup, obtain a random Rare Card to add to your Deck.

    // No need to implement Archaic Tooth: Upon pickup, Transform a starter card with an ancient version.

    // Astrolabe (Ancient)
    //   Effect: Upon pickup, Transform 3 cards, then Upgrade them.
    public static class Astrolabe extends Relic.Astrolabe {
    }

    // TODO: Beautiful Bracelet (Ancient)
    //   Effect: Upon pickup, choose 3 cards in your Deck. Enchant them with Swift 3.

    // Biiig Hug (Ancient)
    //   Effect: Upon pickup, remove 4 cards from your Deck. Whenever you shuffle your Draw Pile, add a Soot into your Draw Pile.
    public static class BiiigHug extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnShuffleHandler("BiiigHug", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.addCardToDeck(generatedCardIdx, false);
                    }
                }
            });
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardOther2.Soot());
        }
    }

    // No need to implement Black Star: Elites drop an additional Relic when defeated.

    // Blessed Antler (Ancient)
    //   Effect: Gain energy at the start of each turn. At the start of each combat, shuffle 3 Dazed into your Draw Pile.
    public static class BlessedAntler extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("BlessedAntler", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.energy += 1;
                    }
                }
            });
            state.properties.addStartOfBattleHandler("BlessedAntler", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.addCardToDeck(generatedCardIdx, false);
                        state.addCardToDeck(generatedCardIdx, false);
                        state.addCardToDeck(generatedCardIdx, false);
                    }
                }
            });
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardOther2.Dazed());
        }
    }

    // Blood-Soaked Rose (Ancient)
    //   Effect: Upon pickup, add 1 Enthralled to your Deck. Gain energy at the start of each turn.
    public static class BloodSoakedRose extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("BloodSoakedRose", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.energy += 1;
                    }
                }
            });
            state.properties.addStartOfBattleHandler("BloodSoakedRose", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.addCardToDeck(generatedCardIdx, false);
                    }
                }
            });
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardOther2.Enthralled());
        }
    }

    // Booming Conch (Ancient)
    //   Effect: At the start of Elite combats, draw 2 additional cards.
    public static class BoomingConch extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("BoomingConch", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 1 && isRelicEnabledInScenario(state) && isEliteFight(state)) {
                        state.draw(2);
                    }
                }
            });
        }
    }

    // Brilliant Scarf (Ancient)
    //   Effect: The 5th card you play each turn is free.
    public static class BrilliantScarf extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("BrilliantScarf", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("BrilliantScarf", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getCounterForWrite()[counterIdx]++;
                    }
                }
            });
            state.properties.addStartOfTurnHandler("BrilliantScarf", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.brilliantScarfCounterIdx = idx;
        }
    }

    // No need to implement Calling Bell: Upon pickup, obtain a unique Curse and 3 Relics.

    // TODO: Choices Paradox (Ancient)
    //   Effect: At the start of each combat, add 1 of 5 random cards into your Hand. Add Retain to the chosen card.

    // No need to implement Claws: Upon pickup, Transform up to 6 cards into Maul.

    // Crossbow (Ancient)
    //   Effect: At the start of your turn, add a random Attack into your Hand. It costs 0 energy this turn.
    public static class Crossbow extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("Crossbow", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.setIsStochastic();
                        int r = state.getSearchRandomGen().nextInt(generatedCardIdxes.length, RandomGenCtx.RandomCardGen);
                        state.addCardToHand(generatedCardIdxes[r]);
                    }
                }
            });
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return CardManager.getCharacterCardsByTypeTmp0Cost(properties.character, Card.ATTACK, false);
        }
    }

    // No need to implement Cursed Pearl: Upon pickup, receive Greed. Gain 333 Gold.

    // TODO: Delicate Frond (Ancient)
    //   Effect: At the start of each combat, fill all empty potion slots with random potions.

    // Diamond Diadem (Ancient)
    //   Effect: Whenever you play 2 or fewer cards in a turn, take half damage from enemies.
    public static class DiamondDiadem extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.diamondDiadem = this;
            state.properties.registerCardsPlayedThisTurnCounter();
        }
    }

    // No need to implement Distinguished Cape: Upon pickup, lose 9 Max HP. Add 3 Apparitions to your Deck.

    // No need to implement Driftwood: You may reroll each card reward once.

    // No need to implement Dusty Tome: Upon pickup, obtain an Ancient Card.

    // Ectoplasm (Ancient)
    //   Effect: You can no longer gain Gold. Gain energy at the start of each turn.
    public static class Ectoplasm extends Relic.Ectoplasm {
    }

    // TODO: Electric Shrymp (Ancient)
    //   Effect: Upon pickup, Enchant a Skill with Imbued.

    // No need to implement Empty Cage: Upon pickup, remove 2 cards from your Deck.

    // Fiddle (Ancient)
    //   Effect: At the start of each turn, draw 2 additional cards. You may not draw cards during your turn.
    public static class Fiddle extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.fiddle = this;
            state.properties.addStartOfTurnHandler("Fiddle", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.draw(2);
                    }
                }
            });
        }
    }

    // No need to implement Fur Coat: Upon pickup, mark 7 random combats. Enemies in those rooms have 1 HP.

    // No need to implement Glass Eye: Upon pickup, obtain 2 Common cards, 2 Uncommon cards, and 1 Rare card.

    // TODO: Glitter (Ancient)
    //   Effect: Enchant all card rewards with Glam.

    // No need to implement Golden Compass: Upon pickup, replace the Act 2 Map with a single special path.

    // No need to implement Golden Pearl: Upon pickup, gain 150 Gold.

    // Iron Club (Ancient)
    //   Effect: Every 4 cards you play, draw 1 card.
    public static class IronClub extends Relic {
        int n;
        int healthReward;

        public IronClub() {
            this(0, 0);
        }

        public IronClub(int n, int healthReward) {
            this.n = n;
            this.healthReward = healthReward;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("IronClub", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = (state.getCounterForRead()[counterIdx] + 1) / 4.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            }, true);
            state.properties.addOnCardPlayedHandler("IronClub", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (!isRelicEnabledInScenario(state)) {
                        return;
                    }
                    int c = state.getCounterForRead()[counterIdx] + 1;
                    if (c == 4) {
                        c = 0;
                        state.draw(1);
                    }
                    state.getCounterForWrite()[counterIdx] = c;
                }
            });
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getCounterForWrite()[counterIdx] = n;
                    }
                }
            });
            if (healthReward > 0) {
                state.properties.addExtraTrainingTarget("IronClub", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                        if (isTerminal > 0) {
                            v.setVExtra(vExtraIdx, state.getCounterForRead()[counterIdx] / 3.0);
                        } else if (isTerminal == 0) {
                            v.setVExtra(vExtraIdx, state.getVExtra(vExtraIdx));
                        }
                    }

                    @Override public void updateQValues(GameState state, VArray v) {
                        v.add(GameState.V_HEALTH_IDX, healthReward * v.getVExtra(vExtraIdx) / state.getPlayerForRead().getMaxHealth());
                    }
                });
            }
        }
    }

    // Jeweled Mask (Ancient)
    //   Effect: At the start of combat put a random Powers from your Draw Pile into your Hand, it's free to play.
    public static class JeweledMask extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("JeweledMask", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state)) {
                        return;
                    }
                    int powerCount = 0;
                    for (int i = 0; i < state.deckArrLen; i++) {
                        if (state.properties.cardDict[state.deckArr[i]].cardType == Card.POWER) {
                            powerCount++;
                        }
                    }
                    if (powerCount == 0) {
                        return;
                    }
                    int r;
                    if (powerCount > 1) {
                        state.setIsStochastic();
                        r = state.getSearchRandomGen().nextInt(powerCount, RandomGenCtx.RandomCardGen);
                    } else {
                        r = 0;
                    }
                    int k = 0, chosenPos = -1;
                    for (int i = 0; i < state.deckArrLen; i++) {
                        if (state.properties.cardDict[state.deckArr[i]].cardType == Card.POWER) {
                            if (k == r) {
                                chosenPos = i;
                                break;
                            }
                            k++;
                        }
                    }
                    int chosenCardIdx = state.deckArr[chosenPos];
                    int permCostCardIdx = state.properties.findCardIndex(state.properties.cardDict[chosenCardIdx].getPermCostIfPossible(0));
                    state.getDeckArrForWrite()[chosenPos] = state.deckArr[state.deckArrLen - 1];
                    state.deckArrLen--;
                    state.addCardToHand(permCostCardIdx);
                }
            });
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var result = new java.util.ArrayList<Card>();
            for (Card c : cards) {
                if (c.cardType == Card.POWER) {
                    result.add(c.getPermCostIfPossible(0));
                }
            }
            return result;
        }
    }

    // No need to implement Jewelry Box: Upon pickup, add 1 Apotheosis to your Deck.

    // No need to implement Large Capsule: Upon pickup, obtain 2 random Relics. Add an additional Strike and Defend to your Deck.

    // No need to implement Lava Rock: The Act 1 Boss drops 2 Relics.

    // No need to implement Lead Paperweight: Upon pickup, choose 1 of 2 Colorless cards to add to your Deck.

    // No need to implement Leafy Poultice: Upon pickup, Transform 1 of your Strikes and 1 of your Defends and lose 10 Max HP.

    // No need to implement Looming Fruit: Upon pickup, raise your Max HP by 31.

    // No need to implement Lord's Parasol: When you encounter the Merchant, immediately obtain EVERYTHING he sells.

    // No need to implement Lost Coffer: Upon pickup, gain 1 card reward and procure 1 random potion.

    // No need to implement Massive Scroll: Upon pickup, choose 1 of 3 Multiplayer Colorless Cards to add to your Deck.

    // No need to implement Meat Cleaver: You may Cook at Rest Sites.

    // TODO: Music Box (Ancient)
    //   Effect: Create an Ethereal copy of the first Attack you play each turn.

    // No need to implement Neow's Torment: Upon pickup, add 1 Neow's Fury to your Deck.

    // No need to implement New Leaf: Upon pickup, Transform 1 card.

    // No need to implement Nutritious Oyster: Upon pickup, raise your Max HP by 11.

    // TODO: Nutritious Soup (Ancient)
    //   Effect: Upon pickup, Enchant all Strikes in your Deck with Tezcatara's Ember.

    // Pael's Blood (Ancient)
    //   Effect: At the start of your turn, draw 1 additional card.
    public static class PaelsBlood extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("PaelsBlood", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.draw(1);
                    }
                }
            });
        }
    }

    // TODO: Pael's Claw (Ancient)
    //   Effect: Upon pickup, Enchant all Defends with Goopy.

    // TODO: Pael's Eye (Ancient)
    //   Effect: The first time each combat you end your turn without playing cards, Exhaust your Hand, and take an extra turn.

    // Pael's Flesh (Ancient)
    //   Effect: Gain an additional energy at the start of your 3rd turn, and every turn after that.
    public static class PaelsFlesh extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addNNInputHandler("PaelsFlesh", new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = Math.min(state.turnNum, 3) / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("PaelsFlesh", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state) && state.turnNum >= 3) {
                        state.energy += 1;
                    }
                }
            });
        }
    }

    // No need to implement Pael's Growth: Upon pickup, Enchant a card with Clone.

    // No need to implement Pael's Horn: Upon pickup, add 2 Relax to your Deck.

    // Pael's Legion (Ancient)
    //   Effect: Doubles Block gained from a card, then goes to sleep for 2 turns.
    public static class PaelsLegion extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.paelsLegion = this;
            state.properties.registerCounter("PaelsLegion", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 2.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int counterIdx) {
                    state.properties.paelsLegionCounterIdx = counterIdx;
                }
            });
            state.properties.addStartOfTurnHandler("PaelsLegion", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state) && state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx]--;
                    }
                }
            });
        }
    }

    // Pael's Tears (Ancient)
    //   Effect: If you end your turn with unspent energy, gain an additional 2 energy next turn.
    public static class PaelsTears extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("PaelsTears", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx];
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addPreEndOfTurnHandler("PaelsTears", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getCounterForWrite()[counterIdx] = state.energy > 0 ? 1 : 0;
                    }
                }
            });
            state.properties.addStartOfTurnHandler("PaelsTears", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state) && state.getCounterForRead()[counterIdx] == 1) {
                        state.getCounterForWrite()[counterIdx] = 0;
                        state.energy += 2;
                    }
                }
            });
        }
    }

    // No need to implement Pael's Tooth: Upon pickup, remove 5 cards from your Deck. After each combat, randomly add 1 back Upgrade.

    // No need to implement Pael's Wing: You may sacrifice card rewards to Pael. Every 2 sacrifices, obtain a Relic.

    // Pandora's Box (Ancient)
    //   Effect: Transform ALL Strikes and Defends.
    public static class PandorasBox extends Relic.PandorasBox {
        public PandorasBox(int n) {
            super(n);
        }
    }

    // Philosopher's Stone (Ancient)
    //   Effect: Gain energy at the start of each turn. ALL enemies start combat with 1 Strength.
    public static class PhilosophersStone extends Relic.PhilosophersStone {
    }

    // No need to implement Pomander: Upon pickup, Upgrade a card.

    // No need to implement Precarious Shears: Upon pickup, remove 2 cards from your Deck and take 13 damage.

    // No need to implement Precise Scissors: Upon pickup, remove 1 card from your Deck.

    // No need to implement Preserved Fog: Upon pickup, remove 5 cards from your Deck. Add Folly to your Deck.

    // Prismatic Gem (Ancient)
    //   Effect: Gain energy at the start of each turn. Card rewards now contain cards from other colors.
    public static class PrismaticGem extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("PrismaticGem", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.energyRefill += 1;
                    }
                }
            });
        }
    }

    // Pumpkin Candle (Ancient)
    //   Effect: Gain energy at the start of each turn. Extinguishes at the start of Act 3.
    public static class PumpkinCandle extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("PumpkinCandle", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.energyRefill += 1;
                    }
                }
            });
        }
    }

    // Radiant Pearl (Ancient)
    //   Effect: At the start of each combat, add 1 Luminesce into your Hand.
    public static class RadiantPearl extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("RadiantPearl", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.addCardToHand(generatedCardIdx);
                    }
                }
            });
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.Luminesce());
        }
    }

    // Runic Pyramid (Ancient)
    //   Effect: At the end of your turn, you no longer discard your Hand.
    public static class RunicPyramid extends Relic.RunicPyramid {
    }

    // Sai (Ancient)
    //   Effect: At the start of your turn, gain 7 Block.
    public static class Sai extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("Sai", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.playerGainBlockNotFromCardPlay(7);
                    }
                }
            });
        }
    }

    // TODO: Sand Castle (Ancient)
    //   Effect: Upon pickup, Upgrade 6 random cards.

    // No need to implement Scroll Boxes: Upon pickup, lose all Gold and choose 1 of 2 packs of cards to add to your Deck.

    // No need to implement Sea Glass: See 15 cards from another character. Choose any number of them to add to your Deck.

    // Seal of Gold (Ancient)
    //   Effect: At the start of your turn, spend 5 Gold to gain energy.
    public static class SealOfGold extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("SealOfGold", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state) || state.properties.currentGoldCounterIdx < 0) {
                        return;
                    }
                    if (state.getCounterForRead()[state.properties.currentGoldCounterIdx] >= 5) {
                        state.getCounterForWrite()[state.properties.currentGoldCounterIdx] -= 5;
                        state.energy += 1;
                    }
                }
            });
        }
    }

    // Sere Talon (Ancient)
    //   Effect: Upon pickup, add 2 random Curses and 3 Wishes to your Deck.
    public static class SereTalon extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("SereTalon", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state) || !state.isFirstEncounter()) {
                        return;
                    }
                    int numCurses = generatedCardIdxes.length - 1;
                    int wishIdx = generatedCardIdxes[numCurses];
                    if (numCurses > 1) {
                        state.setIsStochastic();
                    }
                    for (int i = 0; i < 2; i++) {
                        int r = numCurses > 1 ? state.getSearchRandomGen().nextInt(numCurses, RandomGenCtx.RandomCardGen) : 0;
                        state.addCardToDeck(generatedCardIdxes[r], false);
                    }
                    state.addCardToDeck(wishIdx, false);
                    state.addCardToDeck(wishIdx, false);
                    state.addCardToDeck(wishIdx, false);
                }
            });
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var result = new ArrayList<>(CardManager.getCurseCards());
            result.add(new CardColorless2.Wish());
            return result;
        }
    }

    // No need to implement Signet Ring: Upon pickup, gain 999 Gold.

    // No need to implement Silver Crucible: The first 3 card rewards you see are Upgrade. The first Treasure Chest you open is empty.

    // No need to implement Small Capsule: Upon pickup, obtain a random Relic.

    // Snecko Eye (Ancient)
    //   Effect: At the start of your turn, draw 2 additional cards. Start each combat Confused.
    public static class SneckoEye extends Relic.SneckoEye {
    }

    // Sozu (Ancient)
    //   Effect: Gain energy at the start of each turn. You can no longer obtain potions.
    public static class Sozu extends Relic.Sozu {
    }

    // Spiked Gauntlets (Ancient)
    //   Effect: Gain energy at the start of each turn. Powers cost 1 more energy.
    public static class SpikedGauntlets extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.spikedGauntlets = this;
            state.properties.addStartOfBattleHandler("SpikedGauntlets", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.energyRefill += 1;
                    }
                }
            });
        }
    }

    // No need to implement Stone Humidifier: Whenever you Rest at a Rest Site, raise your Max HP by 5.

    // No need to implement Storybook: Upon pickup, add 1 Brightest Flame to your Deck.

    // No need to implement Tanx's Whistle: Upon pickup, add 1 Whistle to your Deck.

    // TODO: Throwing Axe (Ancient)
    //   Effect: The first card you play each combat is played an extra time.

    // Toasty Mittens (Ancient)
    //   Effect: At the start of your turn, Exhaust the top card of your Draw Pile and gain 1 Strength.
    public static class ToastyMittens extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("ToastyMittens", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state)) {
                        return;
                    }
                    int cardIdx = state.drawOneCardSpecial();
                    if (cardIdx >= 0) {
                        state.exhaustedCardHandle(cardIdx, false);
                    }
                    state.getPlayerForWrite().gainStrength(1);
                }
            });
        }
    }

    // No need to implement Touch of Orobas: Upon pickup, replace your starter Relic with an Ancient version.

    // No need to implement Toy Box: Upon pickup, obtain 4 Wax Relics. Every 3 combats, your left-most Wax Relic will melt away.

    // TODO: Tri-Boomerang (Ancient)
    //   Effect: Choose 3 Attacks in your Deck. Enchant them with Instinct.

    // Velvet Choker (Ancient)
    //   Effect: Gain energy at the start of each turn. You cannot play more than 6 cards per turn.
    public static class VelvetChoker extends Relic.VelvetChoker {
    }

    // Very Hot Cocoa (Ancient)
    //   Effect: Start each combat with an additional 4 energy.
    public static class VeryHotCocoa extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("VeryHotCocoa", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.gainEnergy(4);
                    }
                }
            });
        }
    }

    // No need to implement War Hammer: Whenever you kill an Elite, Upgrade 4 random cards.

    // TODO: Whispering Earring (Ancient)
    //   Effect: Gain energy at the start of each turn. Vakuu plays your first turn for you.

    // No need to implement Yummy Cookie: Upon pickup, Upgrade 4 cards.

    // **************************************************************************************************
    // ********************************************* Special *********************************************
    // **************************************************************************************************

    // No need to implement Circlet: It's a circlet.

    // **************************************************************************************************
    // ********************************************* Ironclad *********************************************
    // **************************************************************************************************

    // Black Blood (Starter)
    //   Effect: At the end of combat, heal 12 HP.
    public static class BlackBlood extends Relic.BlackBlood {
    }

    // Brimstone (Shop)
    //   Effect: At the start of your turn, gain 2 Strength and ALL enemies gain 1 Strength.
    public static class Brimstone extends Relic.Brimstone {
    }

    // Burning Blood (Starter)
    //   Effect: At the end of combat, heal 6 HP.
    public static class BurningBlood extends Relic.BurningBlood {
    }

    // Charon's Ashes (Rare)
    //   Effect: Whenever you Exhaust a card, deal 3 damage to ALL enemies.
    public static class CharonsAshes extends Relic.CharonsAshes {
    }

    // Demon Tongue (Rare)
    //   Effect: The first time you lose HP on your turn, heal HP equal to the amount lost.
    public static class DemonTongue extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("DemonTongue", this, null);
            state.properties.registerIsPlayerTurnCounter();
            state.properties.addStartOfBattleHandler("DemonTongue", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
            state.properties.addOnDamageHandler("DemonTongue", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (!isRelicEnabledInScenario(state)) return;
                    if (state.getCounterForRead()[counterIdx] != 0) return;
                    if (damageDealt <= 0) return;
                    if (state.properties.isPlayerTurnCounterIdx < 0 || state.getCounterForRead()[state.properties.isPlayerTurnCounterIdx] == 0) return;
                    state.getCounterForWrite()[counterIdx] = 1;
                    state.healPlayer(damageDealt);
                }
            });
        }
    }

    // Paper Phrog (Uncommon)
    //   Effect: Enemies with Vulnerable take 75% more damage rather than 50%.
    public static class PaperPhrog extends Relic.PaperPhrog {
    }

    // Red Skull (Common)
    //   Effect: While your HP is at or below 50%, you have 3 additional Strength.
    public static class RedSkull extends Relic.RedSkull {
    }

    // TODO: Ruined Helmet (Rare)
    //   Effect: The first time you gain Strength each combat, double the amount gained.

    // Self-Forming Clay (Uncommon)
    //   Effect: Whenever you lose HP in combat, gain 3 Block next turn.
    public static class SelfFormingClay extends Relic.SelfFormingClay {
    }

    // **************************************************************************************************
    // ********************************************* Silent *********************************************
    // **************************************************************************************************

    // Helical Dart (Rare)
    //   Effect: Whenever you play a Shiv, gain 1 Dexterity this turn.
    public static class HelicalDart extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            entityProperty.changePlayerDexterity = true;
            state.properties.addOnCardPlayedHandler("HelicalDart", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (!isRelicEnabledInScenario(state)) return;
                    if (state.properties.cardDict[cardIdx].getBaseCard() instanceof CardColorless.Shiv) {
                        state.getPlayerForWrite().gainDexterity(1);
                        state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_DEXTERITY_EOT, 1);
                    }
                }
            });
        }
    }

    // Ninja Scroll (Shop)
    //   Effect: At the start of each combat, add 3 Shivs into your Hand.
    public static class NinjaScroll extends Relic.NinjaScroll {
    }

    // Paper Krane (Rare)
    //   Effect: Enemies with Weak deal 40% less damage to you rather than 25%.
    public static class PaperKrane extends Relic.PaperKrane {
    }

    // Ring of the Drake (Starter)
    //   Effect: At the start of your first 3 turns, draw 2 additional cards.
    public static class RingOfTheDrake extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("RingOfTheDrake", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum <= 3 && isRelicEnabledInScenario(state)) {
                        state.draw(2);
                    }
                }
            });
        }
    }

    // Ring of the Snake (Starter)
    //   Effect: At the start of each combat, draw 2 additional cards.
    public static class RingOfTheSnake extends Relic.RingOfTheSnake {
    }

    // Snecko Skull (Common)
    //   Effect: Whenever you apply Poison, apply an additional 1 Poison.
    public static class SneckoSkull extends Relic.SneckoSkull {
    }

    // Tingsha (Uncommon)
    //   Effect: Whenever you discard a card during your turn, deal 3 damage to a random enemy for each card discarded.
    public static class Tingsha extends Relic.Tingsha {
    }

    // Tough Bandages (Rare)
    //   Effect: Whenever you discard a card during your turn, gain 3 Block.
    public static class ToughBandages extends Relic.ToughBandages {
    }

    // Twisted Funnel (Uncommon)
    //   Effect: At the start of each combat, apply 4 Poison to ALL enemies.
    public static class TwistedFunnel extends Relic.TwistedFunnel {
    }

    // **************************************************************************************************
    // ********************************************* Defect *********************************************
    // **************************************************************************************************

    // Cracked Core (Starter)
    //   Effect: At the start of each combat, Channel 1 Lightning.
    public static class CrackedCore extends Relic.CrackedCore {
    }

    // Data Disk (Common)
    //   Effect: Start each combat with 1 Focus.
    public static class DataDisk extends Relic.DataDisk {
    }

    // Emotion Chip (Rare)
    //   Effect: If you lost HP during the previous turn, trigger the passive ability of all Orbs at the start of your turn.
    public static class EmotionChip extends Relic.EmotionChip {
    }

    // Gold-Plated Cables (Uncommon)
    //   Effect: Your rightmost Orb triggers its passive an additional time.
    public static class GoldPlatedCables extends Relic.GoldPlatedCables {
    }

    // Infused Core (Starter)
    //   Effect: At the start of each combat, Channel 3 Lightning.
    public static class InfusedCore extends Relic {
        public InfusedCore() {
            entityProperty.orbGenerationPossible |= OrbType.LIGHTNING.mask;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.channelOrb(OrbType.LIGHTNING);
                        state.channelOrb(OrbType.LIGHTNING);
                        state.channelOrb(OrbType.LIGHTNING);
                    }
                }
            });
        }
    }

    // Metronome (Rare)
    //   Effect: The first time you Channel 7 Orbs each combat, deal 30 damage to ALL enemies.
    public static class Metronome extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Metronome", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 7.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() { return 1; }
            });
            state.properties.addStartOfBattleHandler("Metronome", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.metronomeCounterIdx = idx;
        }
    }

    // Power Cell (Rare)
    //   Effect: At the start of each combat, add 2 zero-cost cards from your Draw Pile into your Hand.
    public static class PowerCell extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("PowerCell", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state)) return;
                    var deck = state.getDeckArrForRead();
                    int count = 0;
                    int[] candidates = new int[state.deckArrLen];
                    for (int i = 0; i < state.deckArrLen; i++) {
                        if (state.properties.cardDict[deck[i]].energyCost == 0) {
                            candidates[count++] = deck[i];
                        }
                    }
                    if (count == 0) return;
                    int[] chosen = new int[Math.min(count, 2)];
                    if (count <= 2) {
                        System.arraycopy(candidates, 0, chosen, 0, chosen.length);
                    } else {
                        state.setIsStochastic();
                        for (int i = 0; i < 2; i++) {
                            int r = state.getSearchRandomGen().nextInt(count - i, RandomGenCtx.CardDraw);
                            chosen[i] = candidates[r];
                            candidates[r] = candidates[count - i - 1];
                        }
                    }
                    for (int i = 0; i < chosen.length; i++) {
                        state.addCardToHand(chosen[i]);
                        state.removeCardFromDeck(chosen[i], false);
                    }
                }
            });
        }
    }

    // Runic Capacitor (Shop)
    //   Effect: Start each combat with 3 additional Orb Slots.
    public static class RunicCapacitor extends Relic.RunicCapacitor {
    }

    // Symbiotic Virus (Uncommon)
    //   Effect: At the start of each combat, Channel 1 Dark.
    public static class SymbioticVirus extends Relic.SymbioticVirus {
    }

    // **************************************************************************************************
    // ********************************************* Regent *********************************************
    // **************************************************************************************************

    // Divine Destiny (Starter)
    //   Effect: At the start of each combat, gain 6 star.
    public static class DivineDestiny extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("DivineDestiny", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.gainStar(6);
                    }
                }
            });
        }
    }

    // Divine Right (Starter)
    //   Effect: At the start of each combat, gain 3 star.
    public static class DivineRight extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("DivineRight", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.gainStar(3);
                    }
                }
            });
        }
    }

    // Fencing Manual (Common)
    //   Effect: At the start of each combat, Forge 10.
    public static class FencingManual extends Relic {
        public FencingManual() {
            entityProperty.canForge = true;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("FencingManual", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.forge(10);
                    }
                }
            });
        }
    }

    // Galactic Dust (Uncommon)
    //   Effect: For every 10 star spent, gain 10 Block.
    public static class GalacticDust extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("GalacticDust", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() { return 1; }
            });
            state.properties.addStartOfBattleHandler("GalacticDust", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
            state.properties.addOnStarChangeHandler("GalacticDust", new OnStarChangeHandler() {
                @Override public void handle(GameState state, int amount) {
                    if (!isRelicEnabledInScenario(state) || amount >= 0) return;
                    int prev = state.getCounterForRead()[counterIdx];
                    state.getCounterForWrite()[counterIdx] += (-amount);
                    int next = state.getCounterForRead()[counterIdx];
                    int blocks = next / 10 - prev / 10;
                    if (blocks > 0) {
                        state.playerGainBlockNotFromCardPlay(blocks * 10);
                    }
                }
            });
        }
    }

    // Lunar Pastry (Rare)
    //   Effect: At the end of your turn, gain star.
    public static class LunarPastry extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addEndOfTurnHandler("LunarPastry", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.gainStar(1);
                    }
                }
            });
        }
    }

    // Mini Regent (Rare)
    //   Effect: The first time you spend star each turn, gain 1 Strength.
    public static class MiniRegent extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("MiniRegent", this, null);
            state.properties.addStartOfTurnHandler("MiniRegent", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
            state.properties.addOnStarChangeHandler("MiniRegent", new OnStarChangeHandler() {
                @Override public void handle(GameState state, int amount) {
                    if (!isRelicEnabledInScenario(state) || amount >= 0) return;
                    if (state.getCounterForRead()[counterIdx] == 0) {
                        state.getCounterForWrite()[counterIdx] = 1;
                        state.getPlayerForWrite().gainStrength(1);
                    }
                }
            });
        }
    }

    // Orange Dough (Rare)
    //   Effect: At the start of each combat, add 2 random Colorless cards into your Hand.
    public static class OrangeDough extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("OrangeDough", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state)) return;
                    state.setIsStochastic();
                    for (int i = 0; i < 2; i++) {
                        int r = state.getSearchRandomGen().nextInt(generatedCardIdxes.length, RandomGenCtx.RandomCardGen);
                        state.addCardToHand(state.createCard(generatedCardIdxes[r]));
                    }
                }
            });
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return CardManager.getColorlessCards(false);
        }
    }

    // Regalite (Uncommon)
    //   Effect: Whenever you create a Colorless card, gain 2 Block.
    public static class Regalite extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnCardCreationHandler("Regalite", new OnCardCreationHandler() {
                @Override public void handle(GameState state, int cardIdx) {
                    if (isRelicEnabledInScenario(state) && CardManager.isColorlessCard(state.properties.cardDict[cardIdx])) {
                        state.playerGainBlockNotFromCardPlay(2);
                    }
                }
            });
        }
    }

    // Vitruvian Minion (Shop)
    //   Effect: Cards containing “Minion” deal double damage and gain double Block.
    public static class VitruvianMinion extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.vitruvianMinion = this;
        }
    }

    // **************************************************************************************************
    // ********************************************* Necrobinder *********************************************
    // **************************************************************************************************

    // TODO: Big Hat (Rare)
    //   Effect: At the start of each combat, add 2 random Ethereal cards into your Hand.

    // Bone Flute (Common)
    //   Effect: Whenever Osty attacks, gain 2 Block.
    public static class BoneFlute extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnOtsyAttackHandler("BoneFlute", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.playerGainBlockNotFromCardPlay(2);
                    }
                }
            });
        }
    }

    // TODO: Book Repair Knife (Uncommon)
    //   Effect: Whenever a non-Minion enemy dies to Doom, heal 3 HP.

    // TODO: Bookmark (Rare)
    //   Effect: At the end of each turn, lower the cost of a random Retained card by 1 until played.

    // Bound Phylactery (Starter)
    //   Effect: At the start of your turn, Summon 1.
    public static class BoundPhylactery extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("BoundPhylactery", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.summon(1);
                    }
                }
            });
        }
    }

    // Funerary Mask (Uncommon)
    //   Effect: At the start of each combat, add 3 Souls into your Draw Pile.
    public static class FuneraryMask extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("FuneraryMask", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state)) return;
                    for (int i = 0; i < 3; i++) {
                        state.addCardToDeck(generatedCardIdx);
                    }
                }
            });
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.Soul());
        }
    }

    // Ivory Tile (Rare)
    //   Effect: Whenever you play a card that costs 3 energy or more, gain energy.
    public static class IvoryTile extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnCardPlayedHandler("IvoryTile", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (isRelicEnabledInScenario(state) && energyUsed >= 3) {
                        state.gainEnergy(1);
                    }
                }
            });
        }
    }

    // Phylactery Unbound (Starter)
    //   Effect: At the start of each combat, Summon 5. At the start of your turn, Summon 2.
    public static class PhylacteryUnbound extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("PhylacteryUnbound", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.summon(5);
                    }
                }
            });
            state.properties.addStartOfTurnHandler("PhylacteryUnbound", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.summon(2);
                    }
                }
            });
        }
    }

    public static class UndyingSignil extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.undyingSigil = this;
        }
    }
}
