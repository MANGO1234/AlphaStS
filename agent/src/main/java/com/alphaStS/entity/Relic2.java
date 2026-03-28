package com.alphaStS.entity;

import com.alphaStS.*;
import com.alphaStS.card.Card;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.enums.OrbType;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.random.RandomGenCtx;
import com.alphaStS.utils.CounterStat;

public class Relic2 {
    // **************************************************************************************************
    // ********************************************* Common *********************************************
    // **************************************************************************************************

    // No need to implement Amethyst Aubergine: Enemies drop 10 additional Gold.

    public static class Anchor extends Relic.Anchor {
    }

    public static class BagOfPreparation extends Relic.BagOfPreparation {
    }

    public static class BloodVial extends Relic.BloodVial {
    }

    // No need to implement Book of Five Rings: Every 5 cards you add to your Deck, heal 15 HP.

    public static class BronzeScales extends Relic.BronzeScales {
    }

    public static class CentennialPuzzle extends Relic.CentennialPuzzle {
    }

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

    public static class HappyFlower extends Relic.HappyFlower {
        public HappyFlower(int n, int healthReward) {
            super(n, healthReward);
        }
    }

    // No need to implement Juzu Bracelet: Regular enemy combats are no longer encountered in ? rooms.

    public static class Lantern extends Relic.Lantern {
    }

    // No need to implement Meal Ticket: Whenever you enter a shop room, heal 15 HP.

    public static class OddlySmoothStone extends Relic.OddlySmoothStone {
    }

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

    public static class PotionBelt extends Relic.PotionBelt {
    }

    // No need to implement Regal Pillow: Whenever you Rest, heal an additional 15 HP.

    // No need to implement Strawberry: Upon pickup, raise your Max HP by 7.

    public static class StrikeDummy extends Relic.StrikeDummy {
    }

    // No need to implement Tiny Mailbox: Whenever you Rest, procure a random potion.

    public static class Vajra extends Relic.Vajira {
    }

    public static class VenerableTeaSet extends Relic.AncientTeaSet {
    }

    public static class WarPaint extends Relic.WarPaint {
    }

    public static class Whetstone extends Relic.WhetStone {
    }

    // **************************************************************************************************
    // ********************************************* Uncommon *********************************************
    // **************************************************************************************************

    public static class Akabeko extends Relic.Akabeko {
    }

    public static class BagOfMarbles extends Relic.BagOfMarbles {
    }

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

    public static class GremlinHorn extends Relic.GremlinHorn {
    }

    public static class HornCleat extends Relic.HornCleat {
    }

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

    public static class LetterOpener extends Relic.LetterOpener {
    }

    // No need to implement Lucky Fysh: Whenever you add a card to your Deck, gain 15 Gold.

    public static class MercuryHourglass extends Relic.MercuryHourglass {
    }

    public static class MiniatureCannon extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.miniatureCannon = this;
        }
    }

    public static class Nunchaku extends Relic.Nunchaku {
        public Nunchaku(int n, int healthReward) {
            super(n, healthReward);
        }
    }

    public static class Orichalcum extends Relic.Orichalcum {
    }

    public static class OrnamentalFan extends Relic.OrnamentalFan {
    }

    public static class Pantograph extends Relic.Pantograph {
    }

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

    public static class PenNib extends Relic.PenNib {
        public PenNib(int n, int healthReward) {
            super(n, healthReward);
        }
    }

    // TODO: Petrified Toad (Uncommon)
    //   Effect: At the start of each combat, procure a  Potion-Shaped Rock.

    // No need to implement Planisphere: Whenever you enter a ? room, heal 4 HP.

    public static class RedMask extends Relic.RedMask {
    }

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

    public static class ArtOfWar extends Relic.ArtOfWar {
    }

    // TODO: Beating Remnant (Rare)
    //   Effect: You cannot lose more than 20 HP in a single turn.

    public static class CaptainsWheel extends Relic.CaptainsWheel {
    }

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

    public static class CloakClasp extends Relic.CloakClasp {
    }

    // No need to implement Frozen Egg: Whenever you add a Powers into your Deck, Upgrade it.

    public static class GamblingChip extends Relic.GamblingChip {
    }

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

    public static class Girya extends Relic.Girya {
        public Girya(int strength) {
            super(strength);
        }
    }

    public static class IceCream extends Relic.IceCream {
    }

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

    public static class Kunai extends Relic.Kunai {
    }

    // No need to implement Lasting Candy: Every other combat, your card rewards gain an additional Power.

    public static class LizardTail extends Relic.LizardTail {
    }

    // No need to implement Mango: Upon pickup, raise your Max HP by 14.

    public static class MeatOnTheBone extends Relic.MeatOnTheBone {
    }

    // No need to implement Molten Egg: Whenever you add an Attack card to your Deck, Upgrade it.

    public static class MummifiedHand extends Relic.MummifiedHand {
    }

    // No need to implement Old Coin: Upon pickup, gain 300 Gold.

    public static class Pocketwatch extends Relic.Pocketwatch {
    }

    // No need to implement Prayer Wheel: Normal enemies drop an additional card reward.

    // TODO: Rainbow Ring (Rare)
    //   Effect: The first time you play an Attack, Skill, and Power each turn, gain 1 Strength and 1 Dexterity.

    // TODO: Razor Tooth (Rare)
    //   Effect: Every time you play an Attack or Skill, Upgrade it for the remainder of combat.

    // No need to implement Shovel: You can now dig at Rest Sites to obtain a random Relic.

    public static class Shuriken extends Relic.Shuriken {
    }

    public static class StoneCalendar extends Relic.StoneCalendar {
    }

    // TODO: Sturdy Clamp (Rare)
    //   Effect: Up to 10 Block persists across turns.

    // No need to implement The Courier: The merchant no longer runs out of cards, relics, or potions and his prices are reduced by 20%.

    // No need to implement Toxic Egg: Whenever you add a Skill into your Deck, Upgrade it.

    public static class TungstenRod extends Relic.TungstenRod {
    }

    public static class UnceasingTop extends Relic.UnceasingTop {
    }

    // TODO: Unsettling Lamp (Rare)
    //   Effect: Each combat, the first time you play a card that Debuffs an enemy, double its effect.

    // TODO: Vexing Puzzlebox (Rare)
    //   Effect: At the start of each combat, add a random card into your Hand. It costs 0 energy.

    // No need to implement White Beast Statue: Potions always appear in combat rewards.

    // No need to implement White Star: Elites drop an additional Rare card reward.

    // **************************************************************************************************
    // *********************************************  Shop  *********************************************
    // **************************************************************************************************

    // TODO: Belt Buckle (Shop)
    //   Effect: While you have no potions, you have 2 additional Dexterity.

    // TODO: Bread (Shop)
    //   Effect: At the start of your first turn, lose 2 energy. At the start of all other turns, gain energy.

    // TODO: Burning Sticks (Shop)
    //   Effect: The first time each combat you Exhaust a Skill, add a copy of it into your Hand.

    // No need to implement Cauldron: Upon pickup, brews 5 random potions.

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

    // TODO: Ringing Triangle (Shop)
    //   Effect: Retain your Hand on the first turn of combat.

    // TODO: Royal Stamp (Shop)
    //   Effect: Upon pickup, choose an Attack or Skill in your Deck to Enchant with Royally Approved.

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

    public static class SlingOfCourage extends Relic.SlingOfCourage {
    }

    public static class TheAbacus extends Relic.TheAbacus {
    }

    public static class Toolbox extends Relic.Toolbox {
    }

    // TODO: Wing Charm (Shop)
    //   Effect: A random card in each card reward is Enchanted with Swift 1.

    // **************************************************************************************************
    // ********************************************* Event  *********************************************
    // **************************************************************************************************

    public static class AnchorQQQ extends Relic.Anchor {
        public AnchorQQQ() {
            super(4);
        }
    }

    // TODO: Big Mushroom (Event)
    //   Effect: Upon pickup, raise your Max HP by 20. At the start of each combat, draw 2 fewer cards.

    // No need to implement Bing Bong: Whenever you add a card to your Deck, add one additional copy.

    public static class BloodVialQQQ extends Relic.BloodVial {
        public BloodVialQQQ() {
            super(1);
        }
    }

    // TODO: Bone Tea (Event)
    //   Effect: At the start of the next combat, Upgrade your starting hand.

    // TODO: Byrdpip (Event)
    //   Effect: Upon pickup, gain the card  Byrd Swoop. A Byrdpip will accompany you in battles.

    // No need to implement Darkstone Periapt: Whenever you obtain a Curse, raise your Max HP by 6.

    // TODO: Daughter of the Wind (Event)
    //   Effect: Whenever you play an Attack, gain 1 Block.

    // No need to implement Dream Catcher: Whenever you Rest, you may add a card to your Deck.

    // TODO: Ember Tea (Event)
    //   Effect: At the start of the next 5 combats, gain 2 Strength.

    // TODO: Forgotten Soul (Event)
    //   Effect: Whenever you Exhaust a card, deal 1 damage to a random enemy.

    // TODO: Fragrant Mushroom (Event)
    //   Effect: Upon pickup, lose 15 HP and Upgrade 3 random cards.

    // TODO: Fresnel Lens (Event)
    //   Effect: Whenever you add a card that gains Block to your Deck, Enchant it with Nimble 2.

    public static class HandDrill extends Relic.HandDrill {
    }

    public static class HappyFlowerQQQ extends Relic.HappyFlower {
        public HappyFlowerQQQ() {
            super(0, 0, 5);
            counterName = "HappyFlowerQQQ";
        }
    }

    // TODO: History Course (Event)
    //   Effect: At the start of your turn, play a copy of your last played Attack or Skill.

    // No need to implement Lee's Waffle???: Upon pickup, heal 10% of your HP.

    // TODO: Lost Wisp (Event)
    //   Effect: Whenever you play a Power, deal 8 damage to ALL enemies.

    // No need to implement Mango???: Upon pickup, raise your Max HP by 3.

    // No need to implement Maw Bank: Whenever you climb a floor, gain 12 Gold. No longer works when you spend any Gold at the shop.

    // TODO: Mr. Struggles (Event)
    //   Effect: At the start of your turn, deal damage equal to the turn number to ALL enemies.

    public static class OrichalcumQQQ extends Relic.Orichalcum {
        public OrichalcumQQQ() {
            super(3);
        }
    }

    // TODO: Pollinous Core (Event)
    //   Effect: Every 4 turns, draw 2 additional cards.

    // TODO: Royal Poison (Event)
    //   Effect: At the start of each combat, lose 4 HP.

    // TODO: Snecko Eye??? (Event)
    //   Effect: Start each combat Confused.

    // TODO: Strike Dummy??? (Event)
    //   Effect: Cards containing "Strike" deal 1 additional damage.

    // TODO: Sword of Jade (Event)
    //   Effect: Start each combat with 3 Strength.

    // No need to implement Sword of Stone: Transforms into a powerful Relic after defeating 5 Elites.

    // TODO: Tea of Discourtesy (Event)
    //   Effect: At the start of the next combat, shuffle 2 Dazed into your Draw Pile.

    public static class TheBoot extends Relic.TheBoot {
    }

    // No need to implement The Chosen Cheese: At the end of combat, gain 1 Max HP.

    // No need to implement The Merchant's Rug???: Poor imitation. Does nothing.

    // TODO: Venerable Tea Set??? (Event)
    //   Effect: Whenever you enter a Rest Site, start the next combat with an additional energy.

    // No need to implement Wongo Customer Appreciation Badge: Does nothing.

    // No need to implement Wongo's Mystery Ticket: Receive 3 random Relics after 5 combats.

    // **************************************************************************************************
    // ********************************************* Ancient *********************************************
    // **************************************************************************************************

    // No need to implement Alchemical Coffer: Upon pickup, gain 4 potion slots filled with random potions.

    // TODO: Arcane Scroll (Ancient)
    //   Effect: Upon pickup, obtain a random Rare Card to add to your Deck.

    // TODO: Archaic Tooth (Ancient)
    //   Effect: Upon pickup, Transform a starter card with an ancient version.

    public static class Astrolabe extends Relic.Astrolabe {
    }

    // TODO: Beautiful Bracelet (Ancient)
    //   Effect: Upon pickup, choose 3 cards in your Deck. Enchant them with Swift 3.

    // TODO: Biiig Hug (Ancient)
    //   Effect: Upon pickup, remove 4 cards from your Deck. Whenever you shuffle your Draw Pile, add a  Soot into your Draw Pile.

    // No need to implement Black Star: Elites drop an additional Relic when defeated.

    // TODO: Blessed Antler (Ancient)
    //   Effect: Gain energy at the start of each turn. At the start of each combat, shuffle 3  Dazed into your Draw Pile.

    // TODO: Blood-Soaked Rose (Ancient)
    //   Effect: Upon pickup, add 1  Enthralled to your Deck. Gain energy at the start of each turn.

    // TODO: Booming Conch (Ancient)
    //   Effect: At the start of Elite combats, draw 2 additional cards.

    // TODO: Brilliant Scarf (Ancient)
    //   Effect: The 5th card you play each turn is free.

    // No need to implement Calling Bell: Upon pickup, obtain a unique Curse and 3 Relics.

    // TODO: Choices Paradox (Ancient)
    //   Effect: At the start of each combat, add 1 of 5 random cards into your Hand. Add Retain to the chosen card.

    // No need to implement Claws: Upon pickup, Transform up to 6 cards into Maul.

    // TODO: Crossbow (Ancient)
    //   Effect: At the start of your turn, add a random Attack into your Hand. It costs 0 energy this turn.

    // No need to implement Cursed Pearl: Upon pickup, receive Greed. Gain 333 Gold.

    // TODO: Delicate Frond (Ancient)
    //   Effect: At the start of each combat, fill all empty potion slots with random potions.

    // TODO: Diamond Diadem (Ancient)
    //   Effect: Whenever you play 2 or fewer cards in a turn, take half damage from enemies.

    // No need to implement Distinguished Cape: Upon pickup, lose 9 Max HP. Add 3 Apparitions to your Deck.

    // No need to implement Driftwood: You may reroll each card reward once.

    // TODO: Dusty Tome (Ancient)
    //   Effect: Upon pickup, obtain an Ancient Card.

    public static class Ectoplasm extends Relic.Ectoplasm {
    }

    // TODO: Electric Shrymp (Ancient)
    //   Effect: Upon pickup, Enchant a Skill with Imbued.

    // No need to implement Empty Cage: Upon pickup, remove 2 cards from your Deck.

    // TODO: Fiddle (Ancient)
    //   Effect: At the start of each turn, draw 2 additional cards. You may not draw cards during your turn.

    // No need to implement Fur Coat: Upon pickup, mark 7 random combats. Enemies in those rooms have 1 HP.

    // No need to implement Glass Eye: Upon pickup, obtain 2 Common cards, 2 Uncommon cards, and 1 Rare card.

    // TODO: Glitter (Ancient)
    //   Effect: Enchant all card rewards with Glam.

    // No need to implement Golden Compass: Upon pickup, replace the Act 2 Map with a single special path.

    // No need to implement Golden Pearl: Upon pickup, gain 150 Gold.

    // TODO: Iron Club (Ancient)
    //   Effect: Every 4 cards you play, draw 1 card.

    // TODO: Jeweled Mask (Ancient)
    //   Effect: At the start of combat put a random Powers from your Draw Pile into your Hand, it's free to play.

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
    //   Effect: Upon pickup, Enchant all Strikes in your Deck with Tezcatara.

    // TODO: Pael's Blood (Ancient)
    //   Effect: At the start of your turn, draw 1 additional card.

    // TODO: Pael's Claw (Ancient)
    //   Effect: Upon pickup, Enchant all Defends with Goopy.

    // TODO: Pael's Eye (Ancient)
    //   Effect: The first time each combat you end your turn without playing cards, Exhaust your Hand, and take an extra turn.

    // TODO: Pael's Flesh (Ancient)
    //   Effect: Gain an additional energy at the start of your 3rd turn, and every turn after that.

    // TODO: Pael's Growth (Ancient)
    //   Effect: Upon pickup, Enchant a card with Clone.

    // No need to implement Pael's Horn: Upon pickup, add 2 Relax to your Deck.

    // TODO: Pael's Legion (Ancient)
    //   Effect: Doubles Block gained from a card, then goes to sleep for 2 turns.

    // TODO: Pael's Tears (Ancient)
    //   Effect: If you end your turn with unspent energy, gain an additional 2 energy next turn.

    // No need to implement Pael's Tooth: Upon pickup, remove 5 cards from your Deck. After each combat, randomly add 1 back Upgrade.

    // No need to implement Pael's Wing: You may sacrifice card rewards to Pael. Every 2 sacrifices, obtain a Relic.

    public static class PandorasBox extends Relic.PandorasBox {
        public PandorasBox(int n) {
            super(n);
        }
    }

    public static class PhilosophersStone extends Relic.PhilosophersStone {
    }

    // No need to implement Pomander: Upon pickup, Upgrade a card.

    // No need to implement Precarious Shears: Upon pickup, remove 2 cards from your Deck and take 13 damage.

    // No need to implement Precise Scissors: Upon pickup, remove 1 card from your Deck.

    // No need to implement Preserved Fog: Upon pickup, remove 5 cards from your Deck. Add Folly to your Deck.

    // TODO: Prismatic Gem (Ancient)
    //   Effect: Gain energy at the start of each turn. Card rewards now contain cards from other colors.

    // TODO: Pumpkin Candle (Ancient)
    //   Effect: Gain energy at the start of each turn. Extinguishes at the start of Act 3.

    // TODO: Radiant Pearl (Ancient)
    //   Effect: At the start of each combat, add 1  Luminesce into your Hand.

    public static class RunicPyramid extends Relic.RunicPyramid {
    }

    // TODO: Sai (Ancient)
    //   Effect: At the start of your turn, gain 7 Block.

    // TODO: Sand Castle (Ancient)
    //   Effect: Upon pickup, Upgrade 6 random cards.

    // No need to implement Scroll Boxes: Upon pickup, lose all Gold and choose 1 of 2 packs of cards to add to your Deck.

    // No need to implement Sea Glass: See 15 cards from another character. Choose any number of them to add to your Deck.

    // TODO: Seal of Gold (Ancient)
    //   Effect: At the start of your turn, spend 5 Gold to gain energy.

    // TODO: Sere Talon (Ancient)
    //   Effect: Upon pickup, add 2 random Curses and 3 Wishes to your Deck.

    // No need to implement Signet Ring: Upon pickup, gain 999 Gold.

    // No need to implement Silver Crucible: The first 3 card rewards you see are Upgrade. The first Treasure Chest you open is empty.

    // No need to implement Small Capsule: Upon pickup, obtain a random Relic.

    public static class SneckoEye extends Relic.SneckoEye {
    }

    public static class Sozu extends Relic.Sozu {
    }

    // TODO: Spiked Gauntlets (Ancient)
    //   Effect: Gain energy at the start of each turn. Powers cost 1 more energy.

    // No need to implement Stone Humidifier: Whenever you Rest at a Rest Site, raise your Max HP by 5.

    // No need to implement Storybook: Upon pickup, add 1 Brightest Flame to your Deck.

    // No need to implement Tanx's Whistle: Upon pickup, add 1 Whistle to your Deck.

    // TODO: Throwing Axe (Ancient)
    //   Effect: The first card you play each combat is played an extra time.

    // TODO: Toasty Mittens (Ancient)
    //   Effect: At the start of your turn, Exhaust the top card of your Draw Pile and gain 1 Strength.

    // No need to implement Touch of Orobas: Upon pickup, replace your starter Relic with an Ancient version.

    // No need to implement Toy Box: Upon pickup, obtain 4 Wax Relics. Every 3 combats, your left-most Wax Relic will melt away.

    // TODO: Tri-Boomerang (Ancient)
    //   Effect: Choose 3 Attacks in your Deck. Enchant them with Instinct.

    public static class VelvetChoker extends Relic.VelvetChoker {
    }

    // TODO: Very Hot Cocoa (Ancient)
    //   Effect: Start each combat with an additional 4 energy.

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

    public static class BlackBlood extends Relic.BlackBlood {
    }

    public static class Brimstone extends Relic.Brimstone {
    }

    public static class BurningBlood extends Relic.BurningBlood {
    }

    public static class CharonsAshes extends Relic.CharonsAshes {
    }

    // TODO: Demon Tongue (Rare)
    //   Effect: The first time you lose HP on your turn, heal HP equal to the amount lost.

    public static class PaperPhrog extends Relic.PaperPhrog {
    }

    public static class RedSkull extends Relic.RedSkull {
    }

    // TODO: Ruined Helmet (Rare)
    //   Effect: The first time you gain Strength each combat, double the amount gained.

    public static class SelfFormingClay extends Relic.SelfFormingClay {
    }

    // **************************************************************************************************
    // ********************************************* Silent *********************************************
    // **************************************************************************************************

    // TODO: Helical Dart (Rare)
    //   Effect: Whenever you play a Shiv, gain 1 Dexterity this turn.

    public static class NinjaScroll extends Relic.NinjaScroll {
    }

    public static class PaperKrane extends Relic.PaperCrane {
    }

    // TODO: Ring of the Drake (Starter)
    //   Effect: At the start of your first 3 turns, draw 2 additional cards.

    public static class RingOfTheSnake extends Relic.RingOfSnake {
    }

    public static class SneckoSkull extends Relic.SneckoSkull {
    }

    public static class Tingsha extends Relic.Tingsha {
    }

    public static class ToughBandages extends Relic.ToughBandages {
    }

    public static class TwistedFunnel extends Relic.TwistedFunnel {
    }

    // **************************************************************************************************
    // ********************************************* Defect *********************************************
    // **************************************************************************************************

    public static class CrackedCore extends Relic.CrackedOrb {
    }

    public static class DataDisk extends Relic.DataDisk {
    }

    public static class EmotionChip extends Relic.EmotionChip {
    }

    public static class GoldPlatedCables extends Relic.GoldPlatedCable {
    }

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

    // TODO: Metronome (Rare)
    //   Effect: The first time you Channel 7 Orbs each combat, deal 30 damage to ALL enemies.

    // TODO: Power Cell (Rare)
    //   Effect: At the start of each combat, add 2 zero-cost cards from your Draw Pile into your Hand.

    public static class RunicCapacitor extends Relic.RunicCapacitor {
    }

    public static class SymbioticVirus extends Relic {
        public SymbioticVirus() {
            entityProperty.orbGenerationPossible |= OrbType.DARK.mask;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state)) {
                        state.channelOrb(OrbType.DARK);
                    }
                }
            });
        }
    }

    // **************************************************************************************************
    // ********************************************* Regent *********************************************
    // **************************************************************************************************

    // TODO: Divine Destiny (Starter)
    //   Effect: At the start of each combat, gain 6 star.

    // TODO: Divine Right (Starter)
    //   Effect: At the start of each combat, gain 3 star.

    // TODO: Fencing Manual (Common)
    //   Effect: At the start of each combat, Forge 10.

    // TODO: Galactic Dust (Uncommon)
    //   Effect: For every 10 star spent, gain 10 Block.

    // TODO: Lunar Pastry (Rare)
    //   Effect: At the end of your turn, gain star.

    // TODO: Mini Regent (Rare)
    //   Effect: The first time you spend star each turn, gain 1 Strength.

    // TODO: Orange Dough (Rare)
    //   Effect: At the start of each combat, add 2 random Colorless cards into your Hand.

    // TODO: Regalite (Uncommon)
    //   Effect: Whenever you create a Colorless card, gain 2 Block.

    // TODO: Vitruvian Minion (Shop)
    //   Effect: Cards containing "Minion" deal double damage and gain double Block.

    // **************************************************************************************************
    // ********************************************* Necrobinder *********************************************
    // **************************************************************************************************

    // TODO: Big Hat (Rare)
    //   Effect: At the start of each combat, add 2 random Ethereal cards into your Hand.

    // TODO: Bone Flute (Common)
    //   Effect: Whenever Osty attacks, gain 2 Block.

    // TODO: Book Repair Knife (Uncommon)
    //   Effect: Whenever a non-Minion enemy dies to Doom, heal 3 HP.

    // TODO: Bookmark (Rare)
    //   Effect: At the end of each turn, lower the cost of a random Retain card by 1 until played.

    // TODO: Bound Phylactery (Starter)
    //   Effect: At the start of your turn, Summon 1.

    // TODO: Funerary Mask (Uncommon)
    //   Effect: At the start of each combat, add 3 Souls into your Draw Pile.

    // TODO: Ivory Tile (Rare)
    //   Effect: Whenever you play a card that costs 3 energy or more, gain energy.

    // TODO: Phylactery Unbound (Starter)
    //   Effect: At the start of each combat, Summon 5. At the start of your turn, Summon 2.

    // TODO: Undying Sigil (Shop)
    //   Effect: Enemies with at least as much Doom as HP deal 50% less damage.
}
