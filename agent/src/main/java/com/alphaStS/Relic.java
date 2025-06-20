package com.alphaStS;

import com.alphaStS.action.CardDrawAction;
import com.alphaStS.action.GameEnvironmentAction;
import com.alphaStS.card.*;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyEncounter;
import com.alphaStS.enemy.EnemyReadOnly;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.enums.OrbType;
import com.alphaStS.enums.Stance;
import com.alphaStS.utils.CounterStat;
import com.alphaStS.utils.Tuple;
import com.alphaStS.utils.Tuple3;
import one.util.streamex.IntStreamEx;

import java.util.*;
import java.util.stream.Stream;

public abstract class Relic implements GameProperties.CounterRegistrant, GameProperties.TrainingTargetRegistrant {
    public boolean changePlayerStrength;
    public boolean changePlayerDexterity;
    public boolean vulnEnemy;
    public boolean weakEnemy;
    public boolean healPlayer;
    public boolean selectCard1OutOf3;
    public int[] preBattleScenariosEnabled;
    public Card startOfBattleAction;

    public void gamePropertiesSetup(GameState state) {}
    List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) { return List.of(); }
    List<Card> getPossibleTransformTmpCostCards(GameProperties properties, List<Card> cards) { return List.of(); }
    List<Card> getPossibleSelect1OutOf3Cards(GameProperties gameProperties) { return List.of(); }
    public Relic setPreBattleScenariosEnabled(int... preBattleScenariosEnabled) {
        this.preBattleScenariosEnabled = preBattleScenariosEnabled;
        Arrays.sort(this.preBattleScenariosEnabled);
        return this;
    }

    public boolean isRelicEnabledInScenario(int scenario) {
        return preBattleScenariosEnabled == null || Arrays.binarySearch(preBattleScenariosEnabled, scenario) >= 0;
    }

    int counterIdx = -1;

    @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
        counterIdx = idx;
    }

    @Override public int getCounterIdx(GameProperties gameProperties) {
        return counterIdx;
    }

    int vArrayIdx = -1;

    @Override public void setVArrayIdx(GameProperties properties, int idx) {
        vArrayIdx = idx;
    }

    public static boolean isEliteFight(GameState state) {
        for (var enemy : state.getEnemiesForRead()) {
            if (enemy.isAlive() && enemy.properties.isElite) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBossFight(GameState state) {
        for (var enemy : state.getEnemiesForRead()) {
            if (enemy.isAlive() && enemy.properties.isBoss) {
                return true;
            }
        }
        return false;
    }

    // ************************************************************* Common Relics ******************************************************************
    // **********************************************************************************************************************************************

    public static class Akabeko extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.buffs |= PlayerBuff.AKABEKO.mask();
                    }
                }
            });
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        state.buffs &= ~PlayerBuff.AKABEKO.mask();
                    }
                }
            });
        }
    }

    public static class Anchor extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("Anchor", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 1 && isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getPlayerForWrite().gainBlockNotFromCardPlay(10);
                    }
                }
            });
        }
    }

    public static class AncientTeaSet extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.energy += 2;
                    }
                }
            });
        }
    }

    public static class ArtOfWar extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        state.buffs &= ~PlayerBuff.ART_OF_WAR.mask();
                    }
                }
            });
            state.properties.addStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    if (state.turnNum > 1 && (state.buffs & PlayerBuff.ART_OF_WAR.mask()) != 0) {
                        state.gainEnergy(1);
                    }
                    state.buffs |= PlayerBuff.ART_OF_WAR.mask();
                }
            });
        }
    }

    public static class BagOfMarbles extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            vulnEnemy = true;
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            enemy.applyDebuff(state, DebuffType.VULNERABLE, 1);
                        }
                    }
                }
            });
        }
    }

    public static class BagOfPreparation extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("BagOfPreparation", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 1 && isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.draw(2);
                    }
                }
            });
        }
    }

    public static class BloodVial extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasBloodVial = true;
            state.properties.addEndOfBattleHandler("BloodVial", new GameEventHandler(1) {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx) && state.currentEncounter != EnemyEncounter.EncounterEnum.CORRUPT_HEART) {
                        state.healPlayer(2);
                    }
                }
            });
        }
    }

    public static class BronzeScales extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerThornCounter(state, this);
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getCounterForWrite()[counterIdx] += 3;
                    }
                }
            });
        }
    }

    public static class CentennialPuzzle extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.buffs |= PlayerBuff.CENTENNIAL_PUZZLE.mask();
                    }
                }
            });
            state.properties.addOnDamageHandler(new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (damageDealt <= 0) return;
                    if ((state.buffs & PlayerBuff.CENTENNIAL_PUZZLE.mask()) != 0) {
                        state.buffs &= ~PlayerBuff.CENTENNIAL_PUZZLE.mask();
                        state.addGameActionToEndOfDeque(new CardDrawAction(3));
                    }
                }
            });
        }
    }

    // Ceramic Fish: No need to implement
    // Dream Catcher: No need to implement

    public static class HappyFlower extends Relic {
        int n;
        int healthReward;

        public HappyFlower(int n, int healthReward) {
            this.n = n;
            this.healthReward = healthReward;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("HappyFlower", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] == 0 ? 3 : counter[counterIdx]) / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getCounterForWrite()[counterIdx] = n;
                    }
                }
            });
            state.properties.addStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    var counter = state.getCounterForWrite();
                    counter[counterIdx]++;
                    if (counter[counterIdx] == 3) {
                        counter[counterIdx] = 0;
                        state.gainEnergy(1);
                    }
                }
            });
            if (healthReward > 0) {
                state.properties.addExtraTrainingTarget("HappyFlower", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                        if (isTerminal > 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getCounterForRead()[counterIdx] / 3.0;
                        } else if (isTerminal == 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getVOther(vArrayIdx);
                        }
                    }

                    @Override public void updateQValues(GameState state, double[] v) {
                        v[GameState.V_HEALTH_IDX] += healthReward * v[GameState.V_OTHER_IDX_START + vArrayIdx] / state.getPlayeForRead().getMaxHealth();
                    }
                });
            }
        }

        @Override public void setCounterIdx(GameProperties properties, int counterIdx) {
            super.setCounterIdx(properties, counterIdx);
            properties.happyFlowerCounterIdx = counterIdx;
        }

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, "Happy Flower");
        }
    }

    // Juzu Bracelet: No need to implement

    public static class Lantern extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("BagOfPreparation", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.gainEnergy(1);
                    }
                }
            });
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

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Nunchaku", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            }, true);
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 10) {
                            counter[counterIdx] = 0;
                            state.gainEnergy(1);
                        }
                    }
                }
            });
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getCounterForWrite()[counterIdx] = n;
                    }
                }
            });
            if (healthReward > 0) {
                state.properties.addExtraTrainingTarget("Nunchaku", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                        if (isTerminal > 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getCounterForRead()[counterIdx] / 9.0;
                        } else if (isTerminal == 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getVOther(vArrayIdx);
                        }
                    }

                    @Override public void updateQValues(GameState state, double[] v) {
                        v[GameState.V_HEALTH_IDX] += healthReward * v[GameState.V_OTHER_IDX_START + vArrayIdx] / state.getPlayeForRead().getMaxHealth();
                    }
                });
            }
        }

        @Override public void setCounterIdx(GameProperties properties, int counterIdx) {
            super.setCounterIdx(properties, counterIdx);
            properties.nunchakuCounterIdx = counterIdx;
        }

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, "Nunchaku");
        }
    }

    // Omamori: No need to implement

    public static class Orichalcum extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addPreEndOfTurnHandler(new GameEventHandler(0) {
                @Override public void handle(GameState state) {
                    if (state.getPlayeForRead().getBlock() == 0 && isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
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

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("PenNib", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            }, true);
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 10) {
                            counter[counterIdx] = 0;
                        }
                    }
                }
            });
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx) && state.isFirstEncounter()) {
                        state.getCounterForWrite()[counterIdx] = n;
                    }
                }
            });
            if (healthReward > 0) {
                state.properties.addExtraTrainingTarget("PenNib", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                        if (isTerminal > 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getCounterForRead()[counterIdx] / 9.0;
                        } else if (isTerminal == 0) {
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

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, "PenNib");
        }
    }

    public static class PotionBelt extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.numOfPotionSlots = 4;
        }
    }

    public static class PreservedInsect extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isEliteFight(state) && isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
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
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasBoot = true;
        }
    }

    // Tiny Chest: No need to implement

    public static class ToyOrnithopter extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasToyOrniphopter = true;
            // technically heals, but we don't actually want to wait for it
        }
    }

    public static class Vajira extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("OddlySmoothStone", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 1 && isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getPlayerForWrite().gainStrength(1);
                    }
                }
            });
        }
    }

    public static class WarPaint extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    var cards = IntStreamEx.of(state.getDeckArrForRead()).mapToObj((cardIdx) -> new Tuple<>(cardIdx, state.properties.cardDict[cardIdx].getUpgrade())).filter((t) -> t.v2() != null).filter((t) -> t.v2().cardType == Card.SKILL).toList();
                    if (cards.size() == 1) {
                        var r1 = state.getSearchRandomGen().nextInt(cards.size(), RandomGenCtx.Misc);
                        state.removeCardFromDeck(cards.get(r1).v1(), false);
                        state.addCardToDeck(state.properties.findCardIndex(cards.get(r1).v2()), false);
                        state.getStateDesc().append("War Paint: ").append(cards.get(r1).v2().cardName);
                    } else if (cards.size() > 1) {
                        var r1 = state.getSearchRandomGen().nextInt(cards.size(), RandomGenCtx.Misc);
                        var r2 = state.getSearchRandomGen().nextInt(cards.size() - 1, RandomGenCtx.Misc);
                        if (r2 >= r1) {
                            r2 += 1;
                        }
                        state.removeCardFromDeck(cards.get(r1).v1(), false);
                        state.addCardToDeck(state.properties.findCardIndex(cards.get(r1).v2()), false);
                        state.removeCardFromDeck(cards.get(r2).v1(), false);
                        state.addCardToDeck(state.properties.findCardIndex(cards.get(r2).v2()), false);
                        state.getStateDesc().append("War Paint: ").append(cards.get(r1).v2().cardName).append(", ").append(cards.get(r2).v2().cardName);
                    }
                }
            });
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return cards.stream().map(Card::getUpgrade).filter(Objects::nonNull).distinct().toList();
        }
    }

    public static class WhetStone extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    var cards = IntStreamEx.of(state.getDeckArrForRead()).mapToObj((cardIdx) -> new Tuple<>(cardIdx, state.properties.cardDict[cardIdx].getUpgrade())).filter((t) -> t.v2() != null).filter((t) -> t.v2().cardType == Card.ATTACK).toList();
                    if (cards.size() == 1) {
                        var r1 = state.getSearchRandomGen().nextInt(cards.size(), RandomGenCtx.Misc);
                        state.removeCardFromDeck(cards.get(r1).v1(), false);
                        state.addCardToDeck(state.properties.findCardIndex(cards.get(r1).v2()), false);
                        state.getStateDesc().append("Whet Stone: ").append(cards.get(r1).v2().cardName);
                    } else if (cards.size() > 1) {
                        var r1 = state.getSearchRandomGen().nextInt(cards.size(), RandomGenCtx.Misc);
                        var r2 = state.getSearchRandomGen().nextInt(cards.size() - 1, RandomGenCtx.Misc);
                        if (r2 >= r1) {
                            r2 += 1;
                        }
                        state.removeCardFromDeck(cards.get(r1).v1(), false);
                        state.addCardToDeck(state.properties.findCardIndex(cards.get(r1).v2()), false);
                        state.removeCardFromDeck(cards.get(r2).v1(), false);
                        state.addCardToDeck(state.properties.findCardIndex(cards.get(r2).v2()), false);
                        state.getStateDesc().append("Whet Stone: ").append(cards.get(r1).v2().cardName).append(", ").append(cards.get(r2).v2().cardName);
                    }
                }
            });
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return cards.stream().map(Card::getUpgrade).filter(Objects::nonNull).filter((card) -> card.cardType == Card.ATTACK).distinct().toList();
        }
    }

    // **********************************************************************************************************************************************
    // ************************************************************ Uncommon Relics *****************************************************************
    // **********************************************************************************************************************************************

    public static class BlueCandle extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasBlueCandle = true;
        }
    }

    public static class _BottledRelic extends Relic {
        public final Card card;

        public _BottledRelic(Card card) {
            this.card = card;
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

    public static class BottledTornado extends _BottledRelic {
        public BottledTornado(Card card) {
            super(card);
        }
    }

    // Darkstone Periapt: No need to implement
    // Eternal Feather: No need to implement
    // Frozen Egg: No need to implement

    public static class GremlinHorn extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnEnemyDeathHandler("GremlinHorn", new GameEventEnemyHandler() {
                @Override public void handle(GameState state, EnemyReadOnly enemy) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.gainEnergy(1);
                        state.draw(1);
                    }
                }
            });
        }
    }

    public static class HornCleat extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addNNInputHandler("HornCleat", new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.turnNum <= 2 ? state.turnNum / 2.0f : -0.5f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("HornCleat", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 2 && isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getPlayerForWrite().gainBlockNotFromCardPlay(14);
                    }
                }
            });
        }
    }

    public static class InkBottle extends Relic {
        int n;
        int healthReward;

        public InkBottle(int n, int healthReward) {
            this.n = n;
            this.healthReward = healthReward;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("InkBottle", this, new GameProperties.NetworkInputHandler() {
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
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    state.getCounterForWrite()[counterIdx] = n;
                }
            });
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    var counter = state.getCounterForWrite();
                    counter[counterIdx]++;
                    if (counter[counterIdx] == 10) {
                        state.draw(1);
                        counter[counterIdx] = 0;
                    }
                }
            });
            if (healthReward > 0) {
                state.properties.addExtraTrainingTarget("InkBottle", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                        if (isTerminal > 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getCounterForRead()[counterIdx] / 9.0;
                        } else if (isTerminal == 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getVOther(vArrayIdx);
                        }
                    }

                    @Override public void updateQValues(GameState state, double[] v) {
                        v[GameState.V_HEALTH_IDX] += healthReward * v[GameState.V_OTHER_IDX_START + vArrayIdx] / state.getPlayeForRead().getMaxHealth();
                    }
                });
            }
        }

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, "Ink Bottle");
        }

        @Override public void setCounterIdx(GameProperties properties, int counterIdx) {
            super.setCounterIdx(properties, counterIdx);
            properties.inkBottleCounterIdx = counterIdx;
        }
    }

    public static class Kunai extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            changePlayerDexterity = true;
            state.properties.registerCounter("Kunai", this, new GameProperties.NetworkInputHandler() {
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
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 3) {
                            counter[counterIdx] = 0;
                            state.getPlayerForWrite().gainDexterity(1);
                        }
                    }
                }
            });
            state.properties.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    public static class LetterOpener extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("LetterOpener", this, new GameProperties.NetworkInputHandler() {
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
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    if (state.properties.cardDict[cardIdx].cardType == Card.SKILL) {
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
            state.properties.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    // Matryoshka: No need to implement

    public static class MeatOnTheBone extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasMeatOnBone = true;
            state.properties.addEndOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx) && state.getPlayeForRead().getHealth() <= state.getPlayeForRead().getMaxHealth() / 2) {
                        state.healPlayer(12);
                    }
                }
            });
        }
    }

    public static class MercuryHourglass extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            state.playerDoNonAttackDamageToEnemy(enemy, 3, true);
                        }
                    }
                }
            });
        }
    }

    // Molten Egg: No need to implement

    public static class MummifiedHand extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (!state.properties.getRelic(Relic.MummifiedHand.class).isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    if (state.properties.cardDict[cardIdx].cardType == Card.POWER) {
                        int possibleCards = 0, diff = 0, idx = -1;
                        var hand = GameStateUtils.getCardArrCounts(state.getHandArrForRead(), state.handArrLen, state.properties.cardDict.length);
                        for (int i = 0; i < state.properties.realCardsLen; i++) {
                            if (hand[i] > 0 && !state.properties.cardDict[i].isXCost && state.properties.cardDict[i].energyCost > 0) {
                                possibleCards += hand[i];
                                diff += 1;
                                idx = i;
                            }
                        }
                        if (possibleCards == 0) {
                            return;
                        }
                        if (diff > 1) {
                            state.setIsStochastic();
                            var r = state.getSearchRandomGen().nextInt(possibleCards, RandomGenCtx.RandomCardHandMummifiedHand, state);
                            var acc = 0;
                            for (int i = 0; i < state.properties.realCardsLen; i++) {
                                if (hand[i] > 0 && !state.properties.cardDict[i].isXCost && state.properties.cardDict[i].energyCost > 0) {
                                    acc += hand[i];
                                    if (acc > r) {
                                        idx = i;
                                        break;
                                    }
                                }
                            }
                        }
                        state.removeCardFromHand(idx);
                        state.addCardToHand(state.properties.tmp0CostCardTransformIdxes[idx]);
                    }
                }
            });
        }

        @Override List<Card> getPossibleTransformTmpCostCards(GameProperties properties, List<Card> cards) {
            if (cards.stream().allMatch((c) -> c.cardType != Card.POWER)) {
                return List.of();
            }
            return cards.stream().filter((x) -> !x.isXCost && x.energyCost > 0 && !(x instanceof Card.CardTmpChangeCost)).map((x) -> (Card) new Card.CardTmpChangeCost(x, 0)).toList();
        }
    }

    public static class OrnamentalFan extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("OrnamentalFan", this, new GameProperties.NetworkInputHandler() {
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
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 3) {
                            counter[counterIdx] = 0;
                            state.getPlayerForWrite().gainBlockNotFromCardPlay(4);
                        }
                    }
                }
            });
            state.properties.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    // Pantograph: No need to implement
    // Pear: No need to implement
    // Question Card: No need to implement

    public static class Shuriken extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            changePlayerStrength = true;
            state.properties.registerCounter("Shuriken", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("Shuriken", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 3) {
                            counter[counterIdx] = 0;
                            state.getPlayerForWrite().gainStrength(1);
                        }
                    }
                }
            });
            state.properties.addPreEndOfTurnHandler("Shuriken", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    // Singing Bowl: No need to implement

    public static class StrikeDummy extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasStrikeDummy = true;
        }
    }

    public static class Sundial extends Relic {
        private final int n;
        private final int healthReward;

        public Sundial(int n, int healthReward) {
            this.n = n;
            this.healthReward = healthReward;
        }

        public Sundial(int n) {
            this(n, 0);
        }

        public Sundial() {
            this(0, 0);
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Sundial", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public void onRegister(int counterIdx) {
                    state.properties.sundialCounterIdx = counterIdx;
                }
            });
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getCounterForWrite()[counterIdx] = n;
                    }
                }
            });
            state.properties.addOnShuffleHandler("Sundial", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getCounterForWrite()[counterIdx] += 1;
                        if (state.getCounterForRead()[counterIdx] == 3) {
                            state.getCounterForWrite()[counterIdx] = 0;
                            state.gainEnergy(2);
                        }
                    }
                }
            });

            if (healthReward > 0) {
                state.properties.addExtraTrainingTarget("Sundial", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                        if (isTerminal > 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getCounterForRead()[counterIdx] / 2.0;
                        } else if (isTerminal == 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getVOther(vArrayIdx);
                        }
                    }

                    @Override public void updateQValues(GameState state, double[] v) {
                        v[GameState.V_HEALTH_IDX] += healthReward * v[GameState.V_OTHER_IDX_START + vArrayIdx] / 2.0 / state.getPlayeForRead().getMaxHealth();
                    }
                });
            }
        }

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, "Sundial");
        }
    }

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

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasBirdFacedUrn = true;
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    if (state.properties.cardDict[cardIdx].cardType == Card.POWER) {
                        state.healPlayer(2);
                    }
                }
            });
        }
    }

    public static class Calipers extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasCaliper = true;
        }
    }

    public static class OddlySmoothStone extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("OddlySmoothStone", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 1 && isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getPlayerForWrite().gainDexterity(1);
                    }
                }
            });
        }
    }

    public static class CaptainsWheel extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addNNInputHandler("CaptainsWheel", new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.turnNum <= 3 ? state.turnNum / 3.0f : -0.5f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("CaptainsWheel", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 3 && isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getPlayerForWrite().gainBlockNotFromCardPlay(18);
                    }
                }
            });
        }
    }

    public static class DeadBranch extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            var cards = getPossibleCards(state.properties);
            state.properties.deadBranchCardsIdxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.properties.deadBranchCardsIdxes[i] = state.properties.findCardIndex(cards.get(i));
            }
            state.properties.addOnExhaustHandler("DeadBranch", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.addGameActionToEndOfDeque(new GameEnvironmentAction() {
                        @Override public void doAction(GameState state) {
                            if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                                return;
                            }
                            var idx = state.getSearchRandomGen().nextInt(state.properties.deadBranchCardsIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, state.properties.deadBranchCardsIdxes));
                            idx = state.addCardToHandGeneration(state.properties.deadBranchCardsIdxes[idx]);
                            state.setIsStochastic();
                            if (state.getStateDesc().length() > 0) state.stateDesc.append(", ");
                            state.getStateDesc().append("Dead Branch -> ").append(state.properties.cardDict[idx].cardName);
                        }
                    });
                }
            });
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            if (properties.character == CharacterEnum.DEFECT) {
                return getPossibleCards(properties);
            }
            return null;
        }

        List<Card> getPossibleCards(GameProperties gameProperties) {
            if (gameProperties.character == CharacterEnum.DEFECT) {
                return List.of(
                        new CardDefect.Aggregate(),
                        new CardDefect.AllForOne(0, 0),
                        new CardDefect.Amplify(),
                        new CardDefect.AutoShields(),
                        new CardDefect.BallLightning(),
                        new CardDefect.Barrage(),
                        new CardDefect.BeamCell(),
                        new CardDefect.BiasedCognition(),
                        new CardDefect.Blizzard(),
                        new CardDefect.BootSequence(),
                        new CardDefect.Buffer(),
                        new CardDefect.BullsEye(),
                        new CardDefect.Capacitor(),
                        new CardDefect.Chaos(),
                        new CardDefect.ChargeBattery(),
                        new CardDefect.Chill(),
                        new CardDefect.Claw(3),
                        new CardDefect.ColdSnap(),
                        new CardDefect.CompiledDriver(),
                        new CardDefect.Consume(),
                        new CardDefect.Coolheaded(),
                        new CardDefect.CoreSurge(),
                        new CardDefect.CreativeAI(),
                        new CardDefect.Darkness(),
                        new CardDefect.Defragment(),
                        new CardDefect.DoomAndGloom(),
                        new CardDefect.DoubleEnergy(),
                        new CardDefect.EchoForm(),
                        new CardDefect.Electrodynamics(),
                        new CardDefect.Equilibrium(),
                        new CardDefect.Fission(),
                        new CardDefect.ForceField(),
                        new CardDefect.FTL(),
                        new CardDefect.Fusion(),
                        new CardDefect.GeneticAlgorithm(1, 0),
                        new CardDefect.Glacier(),
                        new CardDefect.GoForTheEye(),
                        new CardDefect.Heatsinks(),
                        new CardDefect.HelloWorld(),
                        new CardDefect.Hologram(),
                        new CardDefect.HyperBeam(),
                        new CardDefect.Leap(),
                        new CardDefect.Loop(),
                        new CardDefect.MachineLearning(),
                        new CardDefect.Melter(),
                        new CardDefect.MeteorStrike(),
                        new CardDefect.MultiCast(),
                        new CardDefect.Overclock(),
                        new CardDefect.Rainbow(),
                        new CardDefect.Reboot(),
                        new CardDefect.Rebound(),
                        new CardDefect.Recursion(),
                        new CardDefect.Recycle(),
                        new CardDefect.ReinforcedBody(),
                        new CardDefect.Reprogram(),
                        new CardDefect.RipAndTear(),
                        new CardDefect.Scrape(),
                        new CardDefect.Seek(),
                        // new CardDefect.SelfRepair(),
                        new CardDefect.Skim(),
                        new CardDefect.Stack(),
                        new CardDefect.StaticDischarge(),
                        new CardDefect.SteamBarrier(6),
                        new CardDefect.Storm(),
                        new CardDefect.Streamline(),
                        new CardDefect.Sunder(),
                        new CardDefect.SweepingBeam(),
                        new CardDefect.Tempest(),
                        new CardDefect.ThunderStrike(),
                        new CardDefect.Turbo(),
                        new CardDefect.WhiteNoise()
                );
            }
            throw new IllegalArgumentException();
        }
    }

    public static class DuVuDoll extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        for (int i : state.getDeckArrForRead()) {
                            if (state.properties.cardDict[i].cardType == Card.CURSE) {
                                state.getPlayerForWrite().gainStrength(1);
                            }
                        }
                    }
                }
            });
        }
    }

    public static class FossilizedHelix extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerBufferCounter(state, this);
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    state.getCounterForWrite()[state.properties.bufferCounterIdx]++;
                }
            });
        }
    }

    public static class GamblingChip extends Relic {
        @Override List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            return List.of(new CardOther.GamblingChips());
        }
    }

    public static class Ginger extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasGinger = true;
        }
    }

    public static class Girya extends Relic {
        private final int strength;

        public Girya(int strength) {
            this.strength = strength;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.getPlayerForWrite().gainStrength(strength);
        }
    }

    public static class IceCream extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasIceCream = true;
        }
    }

    public static class IncenseBurner extends Relic {
        public static int DEFAULT_REWARD = 0;
        public static int NEXT_FIGHT_IS_SPEAR_AND_SHIELD_REWARD = 0;
        public static int NO_REWARD = 1;

        int n;
        int rewardType = DEFAULT_REWARD;

        public IncenseBurner(int n) {
            this.n = n;
        }

        public IncenseBurner(int n, int rewardType) {
            this.n = n;
            this.rewardType = rewardType;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("IncenseBurner", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[counter[counterIdx]] = 1;
                    return idx + 6;
                }
                @Override public int getInputLenDelta() {
                    return 6;
                }
            }, true);
            state.properties.registerIntangibleCounter();
            state.properties.addStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    state.getCounterForWrite()[counterIdx]++;
                    if (state.getCounterForWrite()[counterIdx] == 6) {
                        state.getCounterForWrite()[counterIdx] = 0;
                        state.getCounterForWrite()[state.properties.intangibleCounterIdx]++;
                    }
                }
            });
            if (state.properties.enemiesEncounters.size() == 1 && state.properties.enemiesEncounters.get(0).encounterEnum == EnemyEncounter.EncounterEnum.CORRUPT_HEART) {
                rewardType = NO_REWARD;
            }
            state.properties.incenseBurnerRewardType = rewardType;
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx) && (!state.properties.isHeartGauntlet || !state.properties.isHeartFight(state))) {
                        state.getCounterForWrite()[counterIdx] = n;
                    }
                }
            });
            if (rewardType != NO_REWARD) {
                state.properties.addExtraTrainingTarget("IncenseBurner", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                        if (isTerminal != 0) {
                            for (int i = 0; i < 6; i++) {
                                v[GameState.V_OTHER_IDX_START + vArrayIdx + i] = 0;
                            }
                            v[GameState.V_OTHER_IDX_START + vArrayIdx + state.getCounterForRead()[counterIdx]] = 1;
                        } else if (isTerminal == 0) {
                            for (int i = 0; i < 6; i++) {
                                v[GameState.V_OTHER_IDX_START + vArrayIdx + i] = state.getVOther(vArrayIdx + i);
                            }
                        }
                    }

                    @Override public void updateQValues(GameState state, double[] v) {
                        if (state.currentEncounter == EnemyEncounter.EncounterEnum.CORRUPT_HEART) {
                            // final fight, no reward
                        } else if (state.currentEncounter == EnemyEncounter.EncounterEnum.SPEAR_AND_SHIELD) {
                            // next fight is heart: reward ending on 4 or 5
                            v[GameState.V_HEALTH_IDX] += 0.05 * v[GameState.V_OTHER_IDX_START + vArrayIdx];
                            v[GameState.V_HEALTH_IDX] += 0.05 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 1];
                            v[GameState.V_HEALTH_IDX] += 0.01 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 2];
                            v[GameState.V_HEALTH_IDX] += 0.2 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 3];
                            v[GameState.V_HEALTH_IDX] += 0.3 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 4];
                            v[GameState.V_HEALTH_IDX] += 0.01 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 5];
                        } else if (rewardType == NEXT_FIGHT_IS_SPEAR_AND_SHIELD_REWARD) {
                            // next fight is spear and shield: end at 4 with minor reward for 3
                            v[GameState.V_HEALTH_IDX] += 0.02 * v[GameState.V_OTHER_IDX_START + vArrayIdx];
                            v[GameState.V_HEALTH_IDX] += 0.02 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 1];
                            v[GameState.V_HEALTH_IDX] += 0.01 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 2];
                            v[GameState.V_HEALTH_IDX] += 0.2 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 3];
                            v[GameState.V_HEALTH_IDX] += v[GameState.V_OTHER_IDX_START + vArrayIdx + 4];
                            v[GameState.V_HEALTH_IDX] += 0.01 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 5];
                        } else {
                            // higher is better
                            v[GameState.V_HEALTH_IDX] += 0.05 / 6.0 * v[GameState.V_OTHER_IDX_START + vArrayIdx];
                            v[GameState.V_HEALTH_IDX] += 0.05 / 6.0 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 1];
                            v[GameState.V_HEALTH_IDX] += 0.1 / 6.0 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 2];
                            v[GameState.V_HEALTH_IDX] += 0.2 / 6.0 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 3];
                            v[GameState.V_HEALTH_IDX] += 0.3 / 6.0 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 4];
                            v[GameState.V_HEALTH_IDX] += 0.3 / 6.0 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 5];
                        }
                    }

                    @Override public int getNumberOfTargets() {
                        return 6;
                    }
                });
            }
        }

        @Override public void setCounterIdx(GameProperties properties, int counterIdx) {
            super.setCounterIdx(properties, counterIdx);
            properties.incenseBurnerCounterIdx = counterIdx;
        }

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, 6, "Incense Burner");
        }
    }

    public static class LizardTail extends Relic {
        private final int maxHp;

        public LizardTail(int maxHp) {
            this.maxHp = maxHp;
        }

        public int getMaxHp() {
            return maxHp;
        }
    }

    // Mango: No need to implement
    // Old Coin: No need to implement
    // Peace Pipe: No need to implement

    public static class Pocketwatch extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Pocketwatch", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = counter[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });

            state.properties.addOnCardPlayedHandler("Pocketwatch", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getCounterForWrite()[counterIdx]++;
                    }
                }
            });

            state.properties.addStartOfTurnHandler("Pocketwatch", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        // Check if previous turn had 3 or fewer cards played
                        if (state.getCounterForRead()[counterIdx] <= 3 && state.getCounterForRead()[counterIdx] > 0) {
                            state.draw(3);
                        }
                        // Reset counter for this turn only if it's not zero
                        if (state.getCounterForRead()[counterIdx] != 0) {
                            state.getCounterForWrite()[counterIdx] = 0;
                        }
                    }
                }
            });
        }

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, "Pocketwatch");
        }
    }

    // Prayer Wheel: No need to implement
    // Shovel: No need to implement

    public static class StoneCalendar extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addNNInputHandler("StoneCalendar", new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.turnNum <= 7 ? state.turnNum / 7.0f : -0.5f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addPreEndOfTurnHandler("StoneCalendar", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 7 && isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            state.playerDoNonAttackDamageToEnemy(enemy, 52, true);
                        }
                    }
                }
            });
        }
    }

    public static class ThreadAndNeedle extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.playerPlatedArmorCanChange = true;
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getPlayerForWrite().gainPlatedArmor(4);
                    }
                }
            });
        }
    }

    public static class Torii extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasTorri = true;
        }
    }

    public static class TungstenRod extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasTungstenRod = true;
        }
    }

    public static class Turnip extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasTurnip = true;
        }
    }

    public static class UnceasingTop extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasUnceasingTop = true;
        }
    }

    // **********************************************************************************************************************************************
    // ************************************************************** Shop Relics *******************************************************************
    // **********************************************************************************************************************************************
    // Winged Greaves: No need to implement
    // Cauldron: No need to implement
    public static class ChemicalX extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasChemicalX = true;
        }
    }

    public static class ClockworkSouvenir extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler("ClockworkSouvenir", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getPlayerForWrite().gainArtifact(1);
                    }
                }
            });
        }
    }

    // Dollys Mirror: No need to implement

    public static class FrozenEye extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasFrozenEye = true;
//            state.properties.needDeckOrderMemory = true;
        }
    }

    public static class HandDrill extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasHandDrill = true;
        }
    }

    // Lee's Waffle: No need to implement

    public static class MedicalKit extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasMedicalKit = true;
        }
    }

    // Membership Card: No need to implement

    public static class OrangePellets extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("OrangePellets", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] & 1) != 0 ? 0.1f : 0.0f;
                    input[idx + 1] = (counter[counterIdx] & (1 << 1)) != 0 ? 0.1f : 0.0f;
                    input[idx + 2] = (counter[counterIdx] & (1 << 2)) != 0 ? 0.1f : 0.0f;
                    return idx + 3;
                }
                @Override public int getInputLenDelta() {
                    return 3;
                }
            });
            state.properties.addOnCardPlayedHandler("OrangePellets", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    if (state.getCounterForRead()[counterIdx] == 0b111) {
                        return;
                    }
                    if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        state.getCounterForWrite()[counterIdx] |= 1;
                    } else if (state.properties.cardDict[cardIdx].cardType == Card.SKILL) {
                        state.getCounterForWrite()[counterIdx] |= 1 << 1;
                    } else if (state.properties.cardDict[cardIdx].cardType == Card.POWER) {
                        state.getCounterForWrite()[counterIdx] |= 1 << 2;
                    }
                    if (state.getCounterForRead()[counterIdx] == 0b111) {
                        state.getPlayerForWrite().removeAllDebuffs(state);
                    }
                }
            });
            state.properties.addEndOfTurnHandler("OrangePellets", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx) && state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    // Orrery: No need to implement
    // Prismatic Shard: No need to implement

    public static class SlingOfCourage extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("SlingOfCourage", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 1 && isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        if (isEliteFight(state)) {
                            state.getPlayerForWrite().gainStrength(2);
                        }
                    }
                }
            });
        }
    }

    public static class StrangeSpoon extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasStrangeSpoon = true;
        }
    }

    public static class TheAbacus extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnShuffleHandler("TheAbacus", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getPlayerForWrite().gainBlockNotFromCardPlay(6);
                    }
                }
            });
        }
    }

    public static class Toolbox extends Relic {
        public Toolbox() {
            selectCard1OutOf3 = true;
        }

        public static void changeToSelectionCtx(GameState state) {
            state.setSelect1OutOf3Idxes(state.properties.toolboxIdxes);
            state.setActionCtx(GameActionCtx.SELECT_CARD_1_OUT_OF_3, null, null);
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            var c = getPossibleSelect1OutOf3Cards(gameProperties);
            var l = new ArrayList<Card>(c);
            for (Card card : c) {
                if (card instanceof Card.CardTmpChangeCost t) {
                    l.add(t.card);
                }
            }
            return l;
        }

        @Override List<Card> getPossibleSelect1OutOf3Cards(GameProperties gameProperties) {
            return List.of(
                    new CardColorless.Blind(),
                    new CardColorless.DarkShackles(),
                    new CardColorless.DeepBreath(),
                    new CardColorless.ToBeImplemented("0"),
                    new CardColorless.DramaticEntrance(),
                    new CardColorless.ToBeImplemented("1"),
                    new CardColorless.Finesse(),
                    new CardColorless.FlashOfSteel(),
                    new CardColorless.ToBeImplemented("2"),
                    new CardColorless.GoodInstincts(),
                    new CardColorless.Impatience(),
                    new CardColorless.ToBeImplemented("3"),
                    new CardColorless.ToBeImplemented("4"),
                    new CardColorless.MindBlast(),
                    new CardColorless.Panacea(),
                    new CardColorless.PanicButton(),
                    new CardColorless.ToBeImplemented("6"),
                    new CardColorless.SwiftStrike(),
                    new CardColorless.Trip(),
                    new CardColorless.Apotheosis(),
                    new CardColorless.ToBeImplemented("7"),
                    new CardColorless.HandOfGreed(0.1),
                    new CardColorless.ToBeImplemented("8"),
                    new CardColorless.MasterOfStrategy(),
                    new CardColorless.Mayhem(),
                    new CardColorless.ToBeImplemented("10"),
                    new CardColorless.Panacea(),
                    new CardColorless.ToBeImplemented("11"),
                    new CardColorless.SecretTechnique(),
                    new CardColorless.SecretWeapon(),
                    new CardColorless.TheBomb(),
                    new CardColorless.ThinkingAhead(),
                    new CardColorless.ToBeImplemented("13"),
                    new CardColorless.ToBeImplemented("14")
            );
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasToolbox = true;
            var cards = getPossibleSelect1OutOf3Cards(state.properties);
            state.properties.toolboxIdxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.properties.toolboxIdxes[i] = state.properties.select1OutOf3CardsReverseIdxes[state.properties.findCardIndex(cards.get(i))];
            }
        }
    }

    // **********************************************************************************************************************************************
    // ************************************************************** Boss Relics *******************************************************************
    // **********************************************************************************************************************************************
    public static class Astrolabe extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            var cards = getPossibleCards(state.properties);
            state.properties.astrolabeCardsIdxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.properties.astrolabeCardsIdxes[i] = state.properties.findCardIndex(cards.get(i));
            }
            state.properties.addStartOfBattleHandler("Astrolabe", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    if (state.properties.makingRealMove) {
                        state.properties.astrolabeCardsTransformed = new int[3];
                    }
                    state.setIsStochastic();
                    if (state.getStateDesc().length() > 0) state.stateDesc.append(", ");
                    state.getStateDesc().append("Astrolabe -> ");
                    for (int i = 0; i < 3; i++) {
                        var idx = state.getSearchRandomGen().nextInt(state.properties.astrolabeCardsIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, state.properties.astrolabeCardsIdxes));
                        state.addCardToDeck(state.properties.astrolabeCardsIdxes[idx], false);
                        if (i > 0) state.stateDesc.append(" + ");
                        state.getStateDesc().append(state.properties.cardDict[state.properties.astrolabeCardsIdxes[idx]].cardName);
                        if (state.properties.makingRealMove) {
                            state.properties.astrolabeCardsTransformed[i] = idx;
                        }
                    }
                }
            });
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            if (properties.character == CharacterEnum.DEFECT) {
                return getPossibleCards(properties);
            }
            return null;
        }

        List<Card> getPossibleCards(GameProperties gameProperties) {
            if (gameProperties.character == CharacterEnum.DEFECT) {
                return List.of(
                        new CardDefect.AggregateP(),
                        new CardDefect.AllForOneP(0, 0),
                        new CardDefect.AmplifyP(),
                        new CardDefect.AutoShieldsP(),
                        new CardDefect.BallLightningP(),
                        new CardDefect.BarrageP(),
                        new CardDefect.BeamCellP(),
                        new CardDefect.BiasedCognitionP(),
                        new CardDefect.BlizzardP(),
                        new CardDefect.BootSequenceP(),
                        new CardDefect.BufferP(),
                        new CardDefect.BullsEyeP(),
                        new CardDefect.CapacitorP(),
                        new CardDefect.ChaosP(),
                        new CardDefect.ChargeBatteryP(),
                        new CardDefect.ChillP(),
                        new CardDefect.ClawP(5),
                        new CardDefect.ColdSnapP(),
                        new CardDefect.CompiledDriverP(),
                        new CardDefect.ConsumeP(),
                        new CardDefect.CoolheadedP(),
                        new CardDefect.CoreSurgeP(),
                        new CardDefect.CreativeAIP(),
                        new CardDefect.DarknessP(),
                        new CardDefect.DefragmentP(),
                        new CardDefect.DoomAndGloomP(),
                        new CardDefect.DoubleEnergyP(),
                        new CardDefect.EchoFormP(),
                        new CardDefect.ElectrodynamicsP(),
                        new CardDefect.EquilibriumP(),
                        new CardDefect.FissionP(),
                        new CardDefect.ForceFieldP(),
                        new CardDefect.FTLP(),
                        new CardDefect.FusionP(),
                        new CardDefect.GeneticAlgorithmP(19, 3),
                        new CardDefect.GlacierP(),
                        new CardDefect.GoForTheEyeP(),
                        new CardDefect.HeatsinksP(),
                        new CardDefect.HelloWorldP(),
                        new CardDefect.HologramP(),
                        new CardDefect.HyperBeamP(),
                        new CardDefect.LeapP(),
                        new CardDefect.LoopP(),
                        new CardDefect.MachineLearningP(),
                        new CardDefect.MelterP(),
                        new CardDefect.MeteorStrikeP(),
                        new CardDefect.MultiCastP(),
                        new CardDefect.OverclockP(),
                        new CardDefect.RainbowP(),
                        new CardDefect.RebootP(),
                        new CardDefect.ReboundP(),
                        new CardDefect.RecursionP(),
                        new CardDefect.RecycleP(),
                        new CardDefect.ReinforcedBodyP(),
                        new CardDefect.ReprogramP(),
                        new CardDefect.RipAndTearP(),
                        new CardDefect.ScrapeP(),
                        new CardDefect.SeekP(),
                        new CardDefect.SelfRepairP(),
                        new CardDefect.SkimP(),
                        new CardDefect.StackP(),
                        new CardDefect.StaticDischargeP(),
                        new CardDefect.SteamBarrierP(),
                        new CardDefect.StormP(),
                        new CardDefect.StreamlineP(),
                        new CardDefect.SunderP(),
                        new CardDefect.SweepingBeamP(),
                        new CardDefect.TempestP(),
                        new CardDefect.ThunderStrikeP(),
                        new CardDefect.TurboP(),
                        new CardDefect.WhiteNoiseP()
                );
            }
            throw new IllegalArgumentException();
        }
    }

    // Black Star: No need to implement

    public static class BustedCrown extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.energyRefill += 1;
                    }
                }
            });
        }
    }

    // Calling Bell: No need to implement

    public static class CoffeeDripper extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.energyRefill += 1;
                    }
                }
            });
        }
    }

    public static class CursedKey extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.energyRefill += 1;
                    }
                }
            });
        }
    }

    public static class Ectoplasm extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.energyRefill += 1;
                    }
                }
            });
        }
    }

    // Empty Cage: No need to implement

    public static class FusionHammer extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.energyRefill += 1;
                    }
                }
            });
        }
    }

    public static class PandorasBox extends Relic {
        private final int n;
        private boolean upgradeSkill = false;
        private boolean upgradeAttack = false;
        private boolean upgradePower = false;

        public PandorasBox(int n) {
            this.n = n;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var cards = getPossibleCards(state.properties);
            state.properties.pandorasBoxCardsIdxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.properties.pandorasBoxCardsIdxes[i] = state.properties.findCardIndex(cards.get(i));
            }
            state.properties.addStartOfBattleHandler("PandorasBox" + n, new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    if (state.properties.makingRealMove) {
                        state.properties.pandorasBoxCardsTransformed = new int[n];
                    }
                    state.setIsStochastic();
                    if (state.getStateDesc().length() > 0)
                        state.stateDesc.append(", ");
                    state.getStateDesc().append("Pandora's Box -> ");
                    for (int i = 0; i < n; i++) {
                        var idx = state.getSearchRandomGen().nextInt(state.properties.pandorasBoxCardsIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, state.properties.pandorasBoxCardsIdxes));
                        state.addCardToDeck(state.properties.pandorasBoxCardsIdxes[idx], false);
                        if (i > 0)
                            state.stateDesc.append(" + ");
                        state.getStateDesc().append(state.properties.cardDict[state.properties.pandorasBoxCardsIdxes[idx]].cardName);
                        if (state.properties.makingRealMove) {
                            state.properties.pandorasBoxCardsTransformed[i] = idx;
                        }
                    }
                }
            });
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return getPossibleCards(properties);
        }

        List<Card> getPossibleCards(GameProperties gameProperties) {
            List<Card> cards = new ArrayList<>();
            if (gameProperties.character == CharacterEnum.SILENT) {
                cards.add(new CardSilent.Bane());
                cards.add(new CardSilent.DaggerSpray());
                cards.add(new CardSilent.DaggerThrow());
                cards.add(new CardSilent.FlyingKnee());
                cards.add(new CardSilent.PoisonedStab());
                cards.add(new CardSilent.QuickSlash());
                cards.add(new CardSilent.Slice());
                cards.add(new CardSilent.SneakyStrike());
                cards.add(new CardSilent.SuckerPunch());
                cards.add(new CardSilent.AllOutAttack());
                cards.add(new CardSilent.Backstab());
                cards.add(new CardSilent.Choke());
                cards.add(new CardSilent.Dash());
                cards.add(new CardSilent.EndlessAgony());
                cards.add(new CardSilent.Eviscerate());
                cards.add(new CardSilent.Finisher());
                cards.add(new CardSilent.Flechette());
                cards.add(new CardSilent.HeelHook());
                cards.add(new CardSilent.MasterfulStab(6));
                cards.add(new CardSilent.Predator());
                cards.add(new CardSilent.RiddleWithHoles());
                cards.add(new CardSilent.Skewer());
                cards.add(new CardSilent.DieDieDie());
                cards.add(new CardSilent.GlassKnife());
                cards.add(new CardSilent.GrandFinale());
                cards.add(new CardSilent.Unload());
                cards.add(new CardSilent.Acrobatics());
                cards.add(new CardSilent.Backflip());
                cards.add(new CardSilent.BladeDance());
                cards.add(new CardSilent.CloakAndDagger());
                cards.add(new CardSilent.DeadlyPoison());
                cards.add(new CardSilent.Deflect());
                cards.add(new CardSilent.DodgeAndRoll());
                cards.add(new CardSilent.Outmaneuver());
                cards.add(new CardSilent.PiercingWail());
                cards.add(new CardSilent.Prepared());
                cards.add(new CardSilent.Blur());
                cards.add(new CardSilent.BouncingFlask());
                cards.add(new CardSilent.CalculatedGamble());
                cards.add(new CardSilent.Catalyst());
                cards.add(new CardSilent.Concentrate());
                cards.add(new CardSilent.CripplingCloud());
                cards.add(new CardSilent.Distraction());
                cards.add(new CardSilent.EscapePlan());
                cards.add(new CardSilent.Expertise());
                cards.add(new CardSilent.LegSweep());
                cards.add(new CardSilent.Reflex());
                cards.add(new CardSilent.Setup(true));
                cards.add(new CardSilent.Tactician());
                cards.add(new CardSilent.Terror());
                cards.add(new CardSilent.Adrenaline());
                cards.add(new CardSilent.Alchemize(0, 0, 0));
                cards.add(new CardSilent.BulletTime());
                cards.add(new CardSilent.Burst());
                cards.add(new CardSilent.CorpseExplosion());
                cards.add(new CardSilent.Doppelganger());
                cards.add(new CardSilent.Malaise());
                cards.add(new CardSilent.Nightmare());
                cards.add(new CardSilent.PhantasmalKiller());
                cards.add(new CardSilent.StormOfSteel());
                cards.add(new CardSilent.Accuracy());
                cards.add(new CardSilent.Caltrops());
                cards.add(new CardSilent.Footwork());
                cards.add(new CardSilent.InfiniteBlade());
                cards.add(new CardSilent.NoxiousFume());
                cards.add(new CardSilent.WellLaidPlans());
                cards.add(new CardSilent.AThousandCuts());
                cards.add(new CardSilent.AfterImage());
                cards.add(new CardSilent.Envenom());
                cards.add(new CardSilent.ToolsOfTheTrade());
                cards.add(new CardSilent.WraithForm());
            } else if (gameProperties.character == CharacterEnum.DEFECT) {
                cards.add(new CardDefect.Aggregate());
                cards.add(new CardDefect.AllForOne(0, 0));
                cards.add(new CardDefect.Amplify());
                cards.add(new CardDefect.AutoShields());
                cards.add(new CardDefect.BallLightning());
                cards.add(new CardDefect.Barrage());
                cards.add(new CardDefect.BeamCell());
                cards.add(new CardDefect.BiasedCognition());
                cards.add(new CardDefect.Blizzard());
                cards.add(new CardDefect.BootSequence());
                cards.add(new CardDefect.Buffer());
                cards.add(new CardDefect.BullsEye());
                cards.add(new CardDefect.Capacitor());
                cards.add(new CardDefect.Chaos());
                cards.add(new CardDefect.ChargeBattery());
                cards.add(new CardDefect.Chill());
                cards.add(new CardDefect.Claw(5));
                cards.add(new CardDefect.ColdSnap());
                cards.add(new CardDefect.CompiledDriver());
                cards.add(new CardDefect.Consume());
                cards.add(new CardDefect.Coolheaded());
                cards.add(new CardDefect.CoreSurge());
                cards.add(new CardDefect.CreativeAI());
                cards.add(new CardDefect.Darkness());
                cards.add(new CardDefect.Defragment());
                cards.add(new CardDefect.DoomAndGloom());
                cards.add(new CardDefect.DoubleEnergy());
                cards.add(new CardDefect.EchoForm());
                cards.add(new CardDefect.Electrodynamics());
                cards.add(new CardDefect.Equilibrium());
                cards.add(new CardDefect.Fission());
                cards.add(new CardDefect.ForceField());
                cards.add(new CardDefect.FTL());
                cards.add(new CardDefect.Fusion());
                cards.add(new CardDefect.GeneticAlgorithm(19, 3));
                cards.add(new CardDefect.Glacier());
                cards.add(new CardDefect.GoForTheEye());
                cards.add(new CardDefect.Heatsinks());
                cards.add(new CardDefect.HelloWorld());
                cards.add(new CardDefect.Hologram());
                cards.add(new CardDefect.HyperBeam());
                cards.add(new CardDefect.Leap());
                cards.add(new CardDefect.Loop());
                cards.add(new CardDefect.MachineLearning());
                cards.add(new CardDefect.Melter());
                cards.add(new CardDefect.MeteorStrike());
                cards.add(new CardDefect.MultiCast());
                cards.add(new CardDefect.Overclock());
                cards.add(new CardDefect.Rainbow());
                cards.add(new CardDefect.Reboot());
                cards.add(new CardDefect.Rebound());
                cards.add(new CardDefect.Recursion());
                cards.add(new CardDefect.Recycle());
                cards.add(new CardDefect.ReinforcedBody());
                cards.add(new CardDefect.Reprogram());
                cards.add(new CardDefect.RipAndTear());
                cards.add(new CardDefect.Scrape());
                cards.add(new CardDefect.Seek());
                cards.add(new CardDefect.SelfRepair());
                cards.add(new CardDefect.Skim());
                cards.add(new CardDefect.Stack());
                cards.add(new CardDefect.StaticDischarge());
                cards.add(new CardDefect.SteamBarrier());
                cards.add(new CardDefect.Storm());
                cards.add(new CardDefect.Streamline());
                cards.add(new CardDefect.Sunder());
                cards.add(new CardDefect.SweepingBeam());
                cards.add(new CardDefect.Tempest());
                cards.add(new CardDefect.ThunderStrike());
                cards.add(new CardDefect.Turbo());
                cards.add(new CardDefect.WhiteNoise());
            } else {
                throw new IllegalArgumentException();
            }
            if (upgradeSkill) {
                for (int i = 0; i < cards.size(); i++) {
                    if (cards.get(i).cardType == Card.SKILL) {
                        cards.set(i, cards.get(i).getUpgrade());
                    }
                }
            }
            return cards;
        }
    }

    public static class PhilosophersStone extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.energyRefill += 1;
                        var enemies = state.getEnemiesForWrite();
                        for (int i = 0; i < enemies.size(); i++) {
                            enemies.getForWrite(i).gainStrength(1);
                        }
                    }
                }
            });
        }
    }

    public static class RunicDome extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.energyRefill += 1;
                    }
                }
            });
            state.properties.hasRunicDome = true;
        }
    }

    public static class RunicPyramid extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasRunicPyramid = true;
        }
    }

    public static class SacredBark extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasSacredBark = true;
        }
    }

    public static class SlaversCollar extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        if (isEliteFight(state) || isBossFight(state)) {
                            state.energyRefill += 1;
                        }
                    }
                }
            });
        }
    }

    public static class SneckoEye extends Relic {
        @Override List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return GameProperties.generateSneckoCards(cards);
        }

        public void gamePropertiesSetup(GameState state) {
            state.properties.setupSneckoIndexes();
            state.properties.registerSneckoDebuffCounter();
            state.properties.hasSneckoEye = true;
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getPlayerForWrite().applyDebuff(state, DebuffType.SNECKO, 1);
                    }
                }
            });
        }
    }

    public static class Sozu extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.energyRefill += 1;
                    }
                }
            });
        }
    }

    // Tiny House: No need to implement
    public static class VelvetChoker extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.energyRefill += 1;
                    }
                }
            });
            state.properties.registerCounter("VelvetChoker", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx + state.getCounterForRead()[counterIdx]] = 0.5f;
                    return idx + 7;
                }

                @Override public int getInputLenDelta() {
                    return 7;
                }
            });
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.getCounterForRead()[counterIdx] < 6 && isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getCounterForWrite()[counterIdx]++;
                    }
                }
            });
            state.properties.addStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }

        @Override public void setCounterIdx(GameProperties properties, int counterIdx) {
            super.setCounterIdx(properties, counterIdx);
            properties.velvetChokerCounterIndexIdx = counterIdx;
        }
    }

    // **********************************************************************************************************************************************
    // ************************************************************** Event Relics ******************************************************************
    // **********************************************************************************************************************************************

    public static class BloodyIdol extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasBloodyIdol = true;
            state.properties.addEndOfBattleHandler("BloodyIdol", new GameEventHandler(1) {
                @Override public void handle(GameState state) {
                    state.healPlayer(5);
                }
            });
        }
    }

    // Cultist Mask: No need to implement

    public static class Enchiridion extends Relic {
        public Enchiridion() {}

        public Enchiridion(Card filter) {
            this.filter = filter;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var c = getPossibleGeneratedCards(state.properties);
            cardsIdx = new int[tmpCardsLen];
            for (int i = 0; i < tmpCardsLen; i++) {
                cardsIdx[i] = state.properties.findCardIndex(c.get(i));
            }
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    state.setIsStochastic();
                    var r = state.getSearchRandomGen().nextInt(cardsIdx.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, cardsIdx));
                    state.addCardToHand(cardsIdx[r]);
                }
            });
        }

        private static List<Card> cards;
        private static int tmpCardsLen;
        private static Card filter;
        private static int[] cardsIdx;

        private static List<Card> getPossibleGeneratedCards(GameProperties properties) {
            if (cards == null) {
                if (properties.character == CharacterEnum.DEFECT) {
                    cards = Stream.of(
                            new CardDefect.BiasedCognition().getTemporaryCostIfPossible(0),
                            new CardDefect.Buffer().getTemporaryCostIfPossible(0),
                            new CardDefect.Capacitor().getTemporaryCostIfPossible(0),
                            new CardDefect.CreativeAI().getTemporaryCostIfPossible(0),
                            new CardDefect.Defragment().getTemporaryCostIfPossible(0),
                            new CardDefect.EchoForm().getTemporaryCostIfPossible(0),
                            new CardDefect.Electrodynamics().getTemporaryCostIfPossible(0),
                            new CardDefect.Heatsinks().getTemporaryCostIfPossible(0),
                            new CardDefect.HelloWorld().getTemporaryCostIfPossible(0),
                            new CardDefect.Loop().getTemporaryCostIfPossible(0),
                            new CardDefect.MachineLearning().getTemporaryCostIfPossible(0),
                            new CardDefect.StaticDischarge().getTemporaryCostIfPossible(0),
                            new CardDefect.Storm().getTemporaryCostIfPossible(0),
                            new CardDefect.BiasedCognition(),
                            new CardDefect.Buffer(),
                            new CardDefect.Capacitor(),
                            new CardDefect.CreativeAI(),
                            new CardDefect.Defragment(),
                            new CardDefect.EchoForm(),
                            new CardDefect.Electrodynamics(),
                            new CardDefect.Heatsinks(),
                            new CardDefect.HelloWorld(),
                            new CardDefect.Loop(),
                            new CardDefect.MachineLearning(),
                            new CardDefect.StaticDischarge(),
                            new CardDefect.Storm()
                    ).filter((x) -> filter == null || filter.getTemporaryCostIfPossible(0).equals(x) || filter.equals(x)).toList();
                    tmpCardsLen = cards.size() / 2;
                } else if (properties.character == CharacterEnum.SILENT) {
                    cards = Stream.of(
                            new CardSilent.Accuracy().getTemporaryCostIfPossible(0),
                            new CardSilent.Caltrops().getTemporaryCostIfPossible(0),
                            new CardSilent.Footwork().getTemporaryCostIfPossible(0),
                            new CardSilent.InfiniteBlade().getTemporaryCostIfPossible(0),
                            new CardSilent.NoxiousFume().getTemporaryCostIfPossible(0),
                            new CardSilent.WellLaidPlans().getTemporaryCostIfPossible(0),
                            new CardSilent.AThousandCuts().getTemporaryCostIfPossible(0),
                            new CardSilent.AfterImage().getTemporaryCostIfPossible(0),
                            new CardSilent.Envenom().getTemporaryCostIfPossible(0),
                            new CardSilent.ToolsOfTheTrade().getTemporaryCostIfPossible(0),
                            new CardSilent.WraithForm().getTemporaryCostIfPossible(0),
                            new CardSilent.Accuracy(),
                            new CardSilent.Caltrops(),
                            new CardSilent.Footwork(),
                            new CardSilent.InfiniteBlade(),
                            new CardSilent.NoxiousFume(),
                            new CardSilent.WellLaidPlans(),
                            new CardSilent.AThousandCuts(),
                            new CardSilent.AfterImage(),
                            new CardSilent.Envenom(),
                            new CardSilent.ToolsOfTheTrade(),
                            new CardSilent.WraithForm()
                    ).filter((x) -> filter == null || filter.getTemporaryCostIfPossible(0).equals(x) || filter.equals(x)).toList();
                    tmpCardsLen = cards.size() / 2;
                } else {
                        throw new IllegalArgumentException("Unsupported character: " + properties.character);
                }
            }
            return cards;
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return getPossibleGeneratedCards(properties);
        }
    }

    // Face Of Cleric: No need to implement
    // Golden Idol: No need to implement

    public static class GremlinVisage extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
                }
            });
        }
    }

    public static class MarkOfTheBloom extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasMarkOfTheBloom = true;
        }
    }

    public static class MutagenicStrength extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        var player = state.getPlayerForWrite();
                        player.gainStrength(3);
                        player.applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, 3);
                    }
                }
            });
        }
    }

    // Nloth's Gift: No need to implement
    // Nloth's Mask: No need to implement

    public static class Necronomicon extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.addCardToDeck(state.properties.findCardIndex(new CardOther.Necronomicurse()), false);
                    }
                }
            });
            state.properties.addStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.buffs |= PlayerBuff.NECRONOMICON.mask();
                    }
                }
            });
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType != Card.ATTACK) {
                        return;
                    }
                    if (energyUsed >= 2 && (state.buffs & PlayerBuff.NECRONOMICON.mask()) != 0) {
                        state.buffs &= ~PlayerBuff.NECRONOMICON.mask();
                        state.addGameActionToEndOfDeque(curState -> {
                            var action = curState.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                            curState.playCard(action, lastIdx, true, Relic.Necronomicon.class, false, false, energyUsed, cloneParentLocation);
                        });

                    }
                }
            });
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardOther.Necronomicurse());
        }
    }

    // Neows Blessing: No need to implement
    // todo: Nilrys Codex

    public static class OddMushroom extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasOddMushroom = true;
        }
    }

    public static class RedMask extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            weakEnemy = true;
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            enemy.applyDebuff(state, DebuffType.WEAK, 1);
                        }
                    }
                }
            });
        }
    }

    // Spirit Poop: No need to implement
    // Ssserpent Head: No need to implement

    public static class WarpedTongs extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler(new GameEventHandler(100) {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    int possibleCards = 0, diff = 0;
                    var seen = new boolean[state.properties.cardDict.length];
                    for (int i = 0; i < state.handArrLen; i++) {
                        if (state.properties.upgradeIdxes[state.getHandArrForRead()[i]] >= 0) {
                            possibleCards++;
                            if (!seen[state.getHandArrForRead()[i]]) {
                                diff++;
                                seen[state.getHandArrForRead()[i]] = true;
                            }
                        }
                    }
                    if (possibleCards == 0) {
                        return;
                    }
                    int r = 0;
                    if (diff > 1) {
                        state.setIsStochastic();
                        r = state.getSearchRandomGen().nextInt(possibleCards, RandomGenCtx.RandomCardHandWarpedTongs, state);
                    }
                    for (int i = 0; i < state.handArrLen; i++) {
                        if (state.properties.upgradeIdxes[state.getHandArrForRead()[i]] >= 0) {
                            if (r == 0) {
                                state.getStateDesc().append(state.getStateDescStr().length() > 0 ? "; " : "").append("Warped Tongs: ").append(state.properties.cardDict[state.getHandArrForRead()[i]].cardName);
                                state.getHandArrForWrite()[i] = (short) state.properties.upgradeIdxes[state.getHandArrForRead()[i]];
                                break;
                            }
                            r--;
                        }
                    }
                }
            });
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return cards.stream().map(Card::getUpgrade).filter(Objects::nonNull).toList();
        }
    }

    // **********************************************************************************************************************************************
    // ******************************************************** Ironclad Specific Relics ************************************************************
    // **********************************************************************************************************************************************

    public static class BurningBlood extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasBurningBlood = true;
            state.properties.addEndOfBattleHandler("BurningBlood", new GameEventHandler(1) {
                @Override public void handle(GameState state) {
                    state.healPlayer(6);
                }
            });
        }
    }

    public static class RingOfSerpent extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("RingOfSerpant", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 1 && isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.draw(2);
                    }
                }
            });
        }
    }

    public static class RedSkull extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnDamageHandler(new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (state.getPlayeForRead().getHealth() <= state.getPlayeForRead().getMaxHealth() / 2) {
                        if (state.getPlayeForRead().getHealth() + damageDealt > state.getPlayeForRead().getMaxHealth() / 2) {
                            state.getPlayerForWrite().gainStrength(3);
                        }
                    }
                }
            });
            state.properties.addOnHealHandler(new OnDamageHandler() {
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
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasPaperPhrog = true;
        }
    }

    public static class ChampionBelt extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasChampionBelt = true;
        }
    }

    // **********************************************************************************************************************************************
    // ********************************************************** Snecko Specific Relics ************************************************************
    // **********************************************************************************************************************************************

    public static class NinjaScroll extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("NinjaScroll", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 1 && isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.addCardToHand(state.properties.shivCardIdx);
                        state.addCardToHand(state.properties.shivCardIdx);
                        state.addCardToHand(state.properties.shivCardIdx);
                    }
                }
            });
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Shiv());
        }
    }

    public static class PaperCrane extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasPaperCrane = true;
        }
    }

    public static class SneckoSkull extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasSneckoSkull = true;
        }
    }

    public static class Tingsha extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasTingsha = true;
        }
    }

    public static class ToughBandages extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasToughBandages = true;
        }
    }

    public static class HoveringKite extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Hovering Kite", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = counter[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public void onRegister(int counterIdx) {
                    state.properties.hoveringKiteCounterIdx = counterIdx;
                }
            });
        }
    }

    // **********************************************************************************************************************************************
    // ********************************************************** Defect Specific Relics ************************************************************
    // **********************************************************************************************************************************************

    public static class CrackedOrb extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.channelOrb(OrbType.LIGHTNING);
                    }
                }
            });
        }
    }

    public static class DataDisk extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.gainFocus(1);
                    }
                }
            });
        }
    }

    public static class EmotionChip extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("EmotionChip", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = counter[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnDamageHandler(new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx) && damageDealt > 0) {
                        state.getCounterForWrite()[counterIdx] = 1;
                    }
                }
            });
            state.properties.addStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx] = 0;
                        state.triggerAllOrbsPassive();
                    }
                }
            });
        }
    }

    public static class GoldPlatedCable extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.hasGoldPlatedCable = true;
        }
    }

    public static class NuclearBattery extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.channelOrb(OrbType.PLASMA);
                    }
                }
            });
        }
    }

    public static class RunicCapacitor extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.maxNumOfOrbs = Math.min(state.properties.maxNumOfOrbs + 3, 10);
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.gainOrbSlot(3);
                    }
                }
            });
        }
    }

    public static class Inserter extends Relic {
        int n;
        int healthReward;

        public Inserter(int n, int healthReward) {
            this.n = n;
            this.healthReward = healthReward;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.maxNumOfOrbs = 10;
            state.properties.registerCounter("Inserter", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public void onRegister(int counterIdx) {
                    state.properties.inserterCounterIdx = counterIdx;
                }
            });
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getCounterForWrite()[counterIdx] = n;
                    }
                }
            });
            state.properties.addStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.getCounterForWrite()[counterIdx]++;
                        if (state.getCounterForWrite()[counterIdx] == 2) {
                            state.getCounterForWrite()[counterIdx] = 0;
                            state.gainOrbSlot(1);
                        }
                    }
                }
            });
            if (healthReward > 0) {
                state.properties.addExtraTrainingTarget("Inserter", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                        if (isTerminal > 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getCounterForRead()[counterIdx];
                        } else if (isTerminal == 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getVOther(vArrayIdx);
                        }
                    }

                    @Override public void updateQValues(GameState state, double[] v) {
                        v[GameState.V_HEALTH_IDX] += healthReward * v[GameState.V_OTHER_IDX_START + vArrayIdx] / state.getPlayeForRead().getMaxHealth();
                    }
                });
            }
        }

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, "Inserter");
        }
    }

    public static class BiasedCognitionLimit extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("BiasedCognitionLimit", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 100.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public void onRegister(int counterIdx) {
                    state.properties.biasedCognitionLimitCounterIdx = counterIdx;
                }
            });
        }
    }

    public static class ShiningLight extends Relic {
        private final int dmg;

        public ShiningLight(int dmg) {
            this.dmg = dmg;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (!isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        return;
                    }
                    state.getPlayerForWrite().setHealth(state.getPlayeForRead().getHealth() - dmg);
                    var cards = IntStreamEx.of(state.getDeckArrForRead()).mapToObj((cardIdx) -> new Tuple<>(cardIdx, state.properties.cardDict[cardIdx].getUpgrade())).filter((t) -> t.v2() != null).toList();
                    if (cards.size() == 1) {
                        var r1 = state.getSearchRandomGen().nextInt(cards.size(), RandomGenCtx.Misc);
                        state.removeCardFromDeck(cards.get(r1).v1(), false);
                        state.addCardToDeck(state.properties.findCardIndex(cards.get(r1).v2()), false);
                        state.getStateDesc().append("Shining Light Event: ").append(cards.get(r1).v2().cardName);
                    } else if (cards.size() > 1) {
                        var r1 = state.getSearchRandomGen().nextInt(cards.size(), RandomGenCtx.Misc);
                        var r2 = state.getSearchRandomGen().nextInt(cards.size() - 1, RandomGenCtx.Misc);
                        if (r2 >= r1) {
                            r2 += 1;
                        }
                        state.removeCardFromDeck(cards.get(r1).v1(), false);
                        state.addCardToDeck(state.properties.findCardIndex(cards.get(r1).v2()), false);
                        state.removeCardFromDeck(cards.get(r2).v1(), false);
                        state.addCardToDeck(state.properties.findCardIndex(cards.get(r2).v2()), false);
                        state.getStateDesc().append("Shining Light Event: ").append(cards.get(r1).v2().cardName).append(", ").append(cards.get(r2).v2().cardName);
                    }
                }
            });
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return cards.stream().map(Card::getUpgrade).filter(Objects::nonNull).distinct().toList();
        }
    }

    // todo: not working well with MAKE_PRE_BATTLE_SCENARIOS_RANDOM yet
    public static class UpgradeCardBeforeBattleStarts extends Relic {
        private final List<Card> cards;

        public UpgradeCardBeforeBattleStarts(int count, List<Card> cards) {
            cards = cards.stream().filter((card) -> card.getUpgrade() != null).toList();
            startOfBattleAction = new CardOther.UpgradeCardBeforeBattleStarts(cards, count);
            this.cards = cards;
        }

        public UpgradeCardBeforeBattleStarts(List<Card> cards) {
            this(1, cards);
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            cards = new ArrayList<>();
            cards.add(startOfBattleAction);
            cards.addAll(this.cards.stream().map((c) -> c.getUpgrade()).toList());
            return cards;
        }
    }

    public static class RemoveCardBeforeBattleStarts extends Relic {
        private final List<Card> cards;

        public RemoveCardBeforeBattleStarts(int count, List<Card> cards) {
            cards = cards.stream().filter((card) -> card.getClass() != CardOther.AscendersBane.class).toList();
            startOfBattleAction = new CardOther.RemoveCardBeforeBattleStarts(cards, count);
            this.cards = cards;
        }

        public RemoveCardBeforeBattleStarts(List<Card> cards) {
            this(1, cards);
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            return List.of(new CardOther.RemoveCardBeforeBattleStarts(this.cards, ((CardOther.RemoveCardBeforeBattleStarts) startOfBattleAction).count));
        }
    }

    public static class AddCardBeforeBattleStarts extends Relic {
        private final List<List<Card>> cards;

        public AddCardBeforeBattleStarts(List<List<Card>> cards) {
            startOfBattleAction = new CardOther.AddCardBeforeBattleStarts(cards);
            this.cards = cards;
            this.selectCard1OutOf3 = true;
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            cards = new ArrayList<>();
            cards.add(startOfBattleAction);
            cards.addAll(this.cards.stream().flatMap(Collection::stream).toList());
            return cards;
        }
    }

    public static class PotionMax extends Relic {
        private int n;

        public PotionMax(int n) {
            this.n = n;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.numOfPotionSlots = n;
        }
    }

    // **********************************************************************************************************************************************
    // ********************************************************** Watcher Specific Relics ***********************************************************
    // **********************************************************************************************************************************************

    public static class PureWater extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.addCardToHand(state.properties.miracleCardIdx);
                    }
                }
            });
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Miracle());
        }
    }

    public static class Damaru extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("Damaru", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.gainMantra(1);
                    }
                }
            });
        }
    }

    // todo: Duality

    public static class TeardropLocket extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.changeStance(Stance.CALM);
                    }
                }
            });
        }
    }

    public static class CloakClasp extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addEndOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        int handSize = state.handArr.length;
                        if (handSize > 0) {
                            state.getPlayerForWrite().gainBlockNotFromCardPlay(handSize);
                        }
                    }
                }
            });
        }
    }

    // todo: Golden Eye
    // todo: Melange

    public static class HolyWater extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (isRelicEnabledInScenario(state.preBattleScenariosChosenIdx)) {
                        state.addCardToHand(state.properties.miracleCardIdx);
                        state.addCardToHand(state.properties.miracleCardIdx);
                        state.addCardToHand(state.properties.miracleCardIdx);
                    }
                }
            });
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Miracle());
        }
    }

    // todo: Violet Lotus
}
