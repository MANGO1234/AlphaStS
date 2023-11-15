package com.alphaStS;

import com.alphaStS.Action.CardDrawAction;
import com.alphaStS.Action.GameEnvironmentAction;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyReadOnly;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.enums.OrbType;
import com.alphaStS.utils.CounterStat;
import com.alphaStS.utils.Tuple;
import com.alphaStS.utils.Tuple3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Relic implements GameProperties.CounterRegistrant, GameProperties.TrainingTargetRegistrant {
    public boolean changePlayerStrength;
    public boolean changePlayerDexterity;
    public boolean vulnEnemy;
    public boolean weakEnemy;
    public boolean healPlayer;
    public boolean selectCard1OutOf3;

    public void gamePropertiesSetup(GameState state) {}
    List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) { return List.of(); }
    List<Card> getPossibleSelect3OutOf1Cards(GameProperties gameProperties) { return List.of(); }

    int counterIdx = -1;

    @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
        counterIdx = idx;
    }

    @Override public int getCounterIdx(GameProperties gameProperties) {
        return counterIdx;
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

    private static boolean isBossFight(GameState state) {
        for (var enemy : state.getEnemiesForRead()) {
            if (enemy.property.isBoss) {
                return true;
            }
        }
        return false;
    }

    // ************************************************************* Common Relics ******************************************************************
    // **********************************************************************************************************************************************

    public static class Akabeko extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.buffs |= PlayerBuff.AKABEKO.mask();
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.prop.cardDict[cardIdx].cardType == Card.ATTACK) {
                        state.buffs &= ~PlayerBuff.AKABEKO.mask();
                    }
                }
            });
        }
    }

    public static class Anchor extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.getPlayerForWrite().gainBlockNotFromCardPlay(10);
        }
    }

    public static class AncientTeaSet extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.energy += 2;
                }
            });
        }
    }

    public static class ArtOfWar extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.prop.cardDict[cardIdx].cardType == Card.ATTACK) {
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
        @Override public void gamePropertiesSetup(GameState state) {
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
        @Override public void gamePropertiesSetup(GameState state) {
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
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.registerThornCounter(state, this);
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] += 3;
                }
            });
        }
    }

    public static class CentennialPuzzle extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.buffs |= PlayerBuff.CENTENNIAL_PUZZLE.mask();
            state.addOnDamageHandler(new OnDamageHandler() {
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
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = n;
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
            if (healthReward > 0) {
                state.prop.addExtraTrainingTarget("HappyFlower", this, new TrainingTarget() {
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

        @Override public void gamePropertiesSetup(GameState state) {
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
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.prop.cardDict[cardIdx].cardType == Card.ATTACK) {
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
            if (healthReward > 0) {
                state.prop.addExtraTrainingTarget("Nunchaku", this, new TrainingTarget() {
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
            state.addPreEndOfTurnHandler(new GameEventHandler(0) {
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

        @Override public void gamePropertiesSetup(GameState state) {
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
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.prop.cardDict[cardIdx].cardType == Card.ATTACK) {
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

    // Potion Belt: No need to implement

    public static class PreservedInsect extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
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
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasBoot = true;
        }
    }

    // Tiny Chest: No need to implement


    public static class ToyOrnithopter extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasToyOrniphopter = true;
            // technically heals, but we don't actually want to wait for it
        }
    }

    public static class Vajira extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.getPlayerForWrite().gainStrength(1);
        }
    }

    // War Paint: No need to implement
    // Whetstone: No need to implement

    // **********************************************************************************************************************************************
    // ************************************************************ Uncommon Relics *****************************************************************
    // **********************************************************************************************************************************************

    public static class BlueCandle extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasBlueCandle = true;
        }
    }

    private static class _BottledRelic extends Relic {
        private final Card card;

        public _BottledRelic(Card card) {
            this.card = card;
        }

        @Override public void gamePropertiesSetup(GameState state) {
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
        @Override public void gamePropertiesSetup(GameState state) {
            state.addOnEnemyDeathHandler("GremlinHorn", new GameEventEnemyHandler() {
                @Override public void handle(GameState state, EnemyReadOnly enemy) {
                    state.gainEnergy(1);
                    state.draw(1);
                }
            });
        }
    }

    public static class HornCleat extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
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
                        state.getPlayerForWrite().gainBlockNotFromCardPlay(14);
                    }
                }
            });
        }
    }

    public static class InkBottle extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
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
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
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
        @Override public void gamePropertiesSetup(GameState state) {
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
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.prop.cardDict[cardIdx].cardType == Card.ATTACK) {
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
        @Override public void gamePropertiesSetup(GameState state) {
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
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.prop.cardDict[cardIdx].cardType == Card.SKILL) {
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
        @Override public void gamePropertiesSetup(GameState state) {
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
        @Override public void gamePropertiesSetup(GameState state) {
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

    public static class MummifiedHand extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.prop.cardDict[cardIdx].cardType == Card.POWER) {
                        int possibleCards = 0, diff = 0, idx = -1;
                        var hand = GameStateUtils.getCardArrCounts(state.getHandArrForRead(), state.handArrLen, state.prop.cardDict.length);
                        for (int i = 0; i < state.prop.realCardsLen; i++) {
                            if (hand[i] > 0 && !state.prop.cardDict[i].isXCost && state.prop.cardDict[i].energyCost > 0) {
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
                            for (int i = 0; i < state.prop.realCardsLen; i++) {
                                if (hand[i] > 0 && !state.prop.cardDict[i].isXCost && state.prop.cardDict[i].energyCost > 0) {
                                    acc += hand[i];
                                    if (acc > r) {
                                        idx = i;
                                        break;
                                    }
                                }
                            }
                        }
                        state.removeCardFromHand(idx);
                        state.addCardToHand(state.prop.tmp0CostCardTransformIdxes[idx]);
                    }
//                    if (state.prop.cardDict[cardIdx].cardType != Card.POWER) {
//                        return;
//                    }
//                    int possibleCards = 0, diff = 0;
//                    var seen = new boolean[state.prop.cardDict.length];
//                    for (int i = 0; i < state.handArrLen; i++) {
//                        var idx = state.getHandArrForRead()[i];
//                        if (!state.prop.cardDict[idx].isXCost && state.prop.cardDict[idx].energyCost > 0) {
//                            possibleCards++;
//                            if (!seen[state.getHandArrForRead()[i]]) {
//                                diff++;
//                                seen[state.getHandArrForRead()[i]] = true;
//                            }
//                        }
//                    }
//                    if (possibleCards == 0) {
//                        return;
//                    }
//                    int r = 0;
//                    if (diff > 1) {
//                        state.setIsStochastic();
//                        r = state.getSearchRandomGen().nextInt(possibleCards, RandomGenCtx.RandomCardHandMummifiedHand, state);
//                    }
//                    for (int i = 0; i < state.handArrLen; i++) {
//                        var idx = state.getHandArrForRead()[i];
//                        if (!state.prop.cardDict[idx].isXCost && state.prop.cardDict[idx].energyCost > 0) {
//                            if (r == 0) {
//                                state.getHandArrForWrite()[i] = (short) state.prop.tmp0CostCardTransformIdxes[state.getHandArrForRead()[i]];
//                                break;
//                            }
//                            r--;
//                        }
//                    }
                }
            });
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            if (cards.stream().allMatch((c) -> c.cardType != Card.POWER)) {
                return List.of();
            }
            return cards.stream().filter((x) -> !x.isXCost && x.energyCost > 0 && !(x instanceof Card.CardTmpChangeCost)).map((x) -> (Card) new Card.CardTmpChangeCost(x, 0)).toList();
        }
    }

    public static class OrnamentalFan extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
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
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.prop.cardDict[cardIdx].cardType == Card.ATTACK) {
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
        @Override public void gamePropertiesSetup(GameState state) {
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
            state.addOnCardPlayedHandler("Shuriken", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.prop.cardDict[cardIdx].cardType == Card.ATTACK) {
                        var counter = state.getCounterForWrite();
                        counter[counterIdx]++;
                        if (counter[counterIdx] == 3) {
                            counter[counterIdx] = 0;
                            state.getPlayerForWrite().gainStrength(1);
                        }
                    }
                }
            });
            state.addPreEndOfTurnHandler("Shuriken", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    // Singing Bowl: No need to implement

    public static class StrikeDummy extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasStrikeDummy = true;
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
            state.prop.registerCounter("Sundial", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public void onRegister(int counterIdx) {
                    state.prop.sundialCounterIdx = counterIdx;
                }
            });
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = n;
                }
            });
            if (healthReward > 0) {
                state.prop.addExtraTrainingTarget("Sundial", this, new TrainingTarget() {
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
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.prop.cardDict[cardIdx].cardType == Card.POWER) {
                        state.healPlayer(2);
                    }
                }
            });
        }
    }

    public static class Calipers extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasCaliper = true;
        }
    }

    public static class OddlySmoothStone extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.getPlayerForWrite().gainDexterity(1);
        }
    }

    public static class CaptainsWheel extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.addNNInputHandler("CaptainsWheel", new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.turnNum <= 3 ? state.turnNum / 3.0f : -0.5f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addStartOfTurnHandler("CaptainsWheel", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 3) {
                        state.getPlayerForWrite().gainBlockNotFromCardPlay(18);
                    }
                }
            });
        }
    }

    public static class DeadBranch extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            var cards = getPossibleCards(state.prop);
            state.prop.deadBranchCardsIdxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.prop.deadBranchCardsIdxes[i] = state.prop.findCardIndex(cards.get(i));
            }
            state.addOnExhaustHandler("DeadBranch", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.addGameActionToEndOfDeque(new GameEnvironmentAction() {
                        @Override public void doAction(GameState state) {
                            var idx = state.getSearchRandomGen().nextInt(state.prop.deadBranchCardsIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, state.prop.deadBranchCardsIdxes));
                            idx = state.addCardToHandGeneration(state.prop.deadBranchCardsIdxes[idx]);
                            state.setIsStochastic();
                            if (state.getStateDesc().length() > 0) state.stateDesc.append(", ");
                            state.getStateDesc().append("Dead Branch -> ").append(state.prop.cardDict[idx].cardName);
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
                        new CardDefect.Equilibirum(),
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
                        new CardDefect.MachineLearningP(),
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
            for (int i : state.getDeckArrForRead()) {
                if (state.prop.cardDict[i].cardType == Card.CURSE) {
                    state.getPlayerForWrite().gainStrength(1);
                }
            }
        }
    }

    public static class FossilizedHelix extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.registerBufferCounter(state, this);
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[state.prop.bufferCounterIdx]++;
                }
            });
        }
    }

    public static class GamblingChip extends Relic {
        @Override List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            return List.of(new Card.GamblingChips());
        }
    }

    public static class Ginger extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasGinger = true;
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
            state.prop.hasIceCream = true;
        }
    }

    public static class IncenseBurner extends Relic {
        public static int DEFAULT_REWARD = 0;
        public static int SHIELD_AND_SPEAR_REWARD = 1;
        public static int HEART_REWARD = 2;
        public static int NO_REWARD = 3;

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
            state.prop.registerCounter("IncenseBurner", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[counter[counterIdx]] = 1;
                    return idx + 6;
                }
                @Override public int getInputLenDelta() {
                    return 6;
                }
            });
            state.prop.registerIntangibleCounter();
            state.addStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx]++;
                    if (state.getCounterForWrite()[counterIdx] == 6) {
                        state.getCounterForWrite()[counterIdx] = 0;
                        state.getCounterForWrite()[state.prop.intangibleCounterIdx]++;
                    }
                }
            });
            state.prop.incenseBurnerRewardType = rewardType;
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = n;
                }
            });
            if (rewardType == DEFAULT_REWARD) {
                state.prop.addExtraTrainingTarget("IncenseBurner", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                        if (isTerminal > 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getCounterForRead()[counterIdx] / 5.0;
                        } else if (isTerminal == 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getVOther(vArrayIdx);
                        }
                    }

                    @Override public void updateQValues(GameState state, double[] v) {
                        v[GameState.V_HEALTH_IDX] += 10 * v[GameState.V_OTHER_IDX_START + vArrayIdx] / state.getPlayeForRead().getMaxHealth();
                    }
                });
            } else if (rewardType == SHIELD_AND_SPEAR_REWARD) {
                state.prop.addExtraTrainingTarget("IncenseBurner", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                        if (isTerminal > 0) {
                            for (int i = 0; i < 7; i++) {
                                v[GameState.V_OTHER_IDX_START + vArrayIdx + i] = 0;
                            }
                            v[GameState.V_OTHER_IDX_START + vArrayIdx + state.getCounterForRead()[counterIdx]] = 1;
                        } else if (isTerminal == 0) {
                            for (int i = 0; i < 7; i++) {
                                v[GameState.V_OTHER_IDX_START + vArrayIdx + i] = state.getVOther(vArrayIdx + i);
                            }
                        }
                    }

                    @Override public void updateQValues(GameState state, double[] v) {
                        v[GameState.V_HEALTH_IDX] += 0.05 * v[GameState.V_OTHER_IDX_START + vArrayIdx];
                        v[GameState.V_HEALTH_IDX] += 0.05 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 1];
                        v[GameState.V_HEALTH_IDX] += 0.01 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 2];
                        v[GameState.V_HEALTH_IDX] += 0.2 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 3];
                        v[GameState.V_HEALTH_IDX] += 0.3 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 4];
                        v[GameState.V_HEALTH_IDX] += 0.01 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 5];
                    }

                    @Override public int getNumberOfTargets() {
                        return 7;
                    }
                });
            } else if (rewardType == HEART_REWARD) {
                state.prop.addExtraTrainingTarget("IncenseBurner", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                        if (isTerminal > 0) {
                            for (int i = 0; i < 7; i++) {
                                v[GameState.V_OTHER_IDX_START + vArrayIdx + i] = 0;
                            }
                            v[GameState.V_OTHER_IDX_START + vArrayIdx + state.getCounterForRead()[counterIdx]] = 1;
                        } else if (isTerminal == 0) {
                            for (int i = 0; i < 7; i++) {
                                v[GameState.V_OTHER_IDX_START + vArrayIdx + i] = state.getVOther(vArrayIdx + i);
                            }
                        }
                    }

                    @Override public void updateQValues(GameState state, double[] v) {
                        v[GameState.V_HEALTH_IDX] += 0.02 * v[GameState.V_OTHER_IDX_START + vArrayIdx];
                        v[GameState.V_HEALTH_IDX] += 0.02 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 1];
                        v[GameState.V_HEALTH_IDX] += 0.01 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 2];
                        v[GameState.V_HEALTH_IDX] += 0.2 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 3];
                        v[GameState.V_HEALTH_IDX] += v[GameState.V_OTHER_IDX_START + vArrayIdx + 4];
                        v[GameState.V_HEALTH_IDX] += 0.01 * v[GameState.V_OTHER_IDX_START + vArrayIdx + 5];
                    }

                    @Override public int getNumberOfTargets() {
                        return 7;
                    }
                });
            } else if (rewardType == NO_REWARD) {
            } else {
                throw new IllegalStateException();
            }
        }

        @Override public void setCounterIdx(GameProperties properties, int counterIdx) {
            super.setCounterIdx(properties, counterIdx);
            properties.incenseBurnerCounterIdx = counterIdx;
        }
    }

    // todo: Lizard Tail
    // Mango: No need to implement
    // Old Coin: No need to implement
    // Peace Pipe: No need to implement
    // todo: Pocketwatch
    // Prayer Wheel: No need to implement
    // Shovel: No need to implement
    // todo: Stone Calendar

    public static class ThreadAndNeedle extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.playerPlatedArmorCanChange = true;
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getPlayerForWrite().gainPlatedArmor(4);
                }
            });
        }
    }

    public static class Torii extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasTorri = true;
        }
    }

    public static class TungstenRod extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasTungstenRod = true;
        }
    }

    public static class Turnip extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasTurnip = true;
        }
    }

    public static class UnceasingTop extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasUnceasingTop = true;
        }
    }

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
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasMedicalKit = true;
        }
    }

    // Membership Card: No need to implement

    public static class OrangePellets extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.registerCounter("OrangePellets", this, new GameProperties.NetworkInputHandler() {
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
            state.addOnCardPlayedHandler("OrangePellets", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.getCounterForRead()[counterIdx] == 0b111) {
                        return;
                    }
                    if (state.prop.cardDict[cardIdx].cardType == Card.ATTACK) {
                        state.getCounterForWrite()[counterIdx] |= 1;
                    } else if (state.prop.cardDict[cardIdx].cardType == Card.SKILL) {
                        state.getCounterForWrite()[counterIdx] |= 1 << 1;
                    } else if (state.prop.cardDict[cardIdx].cardType == Card.POWER) {
                        state.getCounterForWrite()[counterIdx] |= 1 << 2;
                    }
                    if (state.getCounterForRead()[counterIdx] == 0b111) {
                        state.getPlayerForWrite().removeAllDebuffs(state);
                    }
                }
            });
            state.prop.addEndOfTurnHandler("OrangePellets", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
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
            if (isEliteFight(state)) {
                state.getPlayerForWrite().gainStrength(2);
            }
        }
    }

    public static class StrangeSpoon extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasStrangeSpoon = true;
        }
    }

    // todo: The Abacus

    public static class Toolbox extends Relic {
        public Toolbox() {
            selectCard1OutOf3 = true;
        }

        public static void changeToSelectionCtx(GameState state) {
            boolean interactive = state.getSearchRandomGen() instanceof InteractiveMode.RandomGenInteractive;
            int idx1 = state.getSearchRandomGen().nextInt(state.prop.toolboxIdxes.length, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + 255, state.prop.toolboxIdxes) : null);
            int idx2 = state.getSearchRandomGen().nextInt(state.prop.toolboxIdxes.length - 1, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + idx1, state.prop.toolboxIdxes) : null);
            int idx3 = state.getSearchRandomGen().nextInt(state.prop.toolboxIdxes.length - 2, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (idx2 << 8) + idx1, state.prop.toolboxIdxes) : null);
            if (idx2 >= idx1) {
                idx2++;
            }
            if (idx3 >= Math.min(idx1, idx2)) {
                idx3++;
            }
            if (idx3 >= Math.max(idx1, idx2)) {
                idx3++;
            }
            state.setSelect1OutOf3Idxes(state.prop.toolboxIdxes[idx1], state.prop.toolboxIdxes[idx2], state.prop.toolboxIdxes[idx3]);
            state.setIsStochastic();
            state.setActionCtx(GameActionCtx.SELECT_CARD_1_OUT_OF_3, null, false);
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            var c = getPossibleSelect3OutOf1Cards(gameProperties);
            var l = new ArrayList<Card>(c);
            for (Card card : c) {
                if (card instanceof Card.CardTmpChangeCost t) {
                    l.add(t.card);
                }
            }
            return l;
        }

        @Override List<Card> getPossibleSelect3OutOf1Cards(GameProperties gameProperties) {
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
                    new CardColorless.ToBeImplemented("5"),
                    new CardColorless.ToBeImplemented("6"),
                    new CardColorless.SwiftStrike(),
                    new CardColorless.Trip(),
                    new CardColorless.Apotheosis(),
                    new CardColorless.ToBeImplemented("7"),
                    new CardColorless.HandOfGreed(0.1),
                    new CardColorless.ToBeImplemented("8"),
                    new CardColorless.MasterOfStrategy(),
                    new CardColorless.ToBeImplemented("9"),
                    new CardColorless.ToBeImplemented("10"),
                    new CardColorless.Panacea(),
                    new CardColorless.ToBeImplemented("11"),
                    new CardColorless.SecretTechnique(),
                    new CardColorless.SecretWeapon(),
                    new CardColorless.ToBeImplemented("12"),
                    new CardColorless.ThinkingAhead(),
                    new CardColorless.ToBeImplemented("13"),
                    new CardColorless.ToBeImplemented("14")
            );
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasToolbox = true;
            var cards = getPossibleSelect3OutOf1Cards(state.prop);
            state.prop.toolboxIdxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.prop.toolboxIdxes[i] = state.prop.select1OutOf3CardsReverseIdxes[state.prop.findCardIndex(cards.get(i))];
            }
        }
    }

    // **********************************************************************************************************************************************
    // ************************************************************** Boss Relics *******************************************************************
    // **********************************************************************************************************************************************
    // Astrolabe: No need to implement
    // Black Star: No need to implement

    public static class BustedCrown extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.energyRefill += 1;
        }
    }

    // Calling Bell: No need to implement

    public static class CoffeeDripper extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.energyRefill += 1;
        }
    }

    public static class CursedKey extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.energyRefill += 1;
        }
    }

    public static class Ectoplasm extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.energyRefill += 1;
        }
    }

    // Empty Cage: No need to implement

    public static class FusionHammer extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.energyRefill += 1;
        }
    }

    // Pandora's Box: No need to implement

    public static class PhilosophersStone extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.energyRefill += 1;
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    var enemies = state.getEnemiesForWrite();
                    for (int i = 0; i < enemies.size(); i++) {
                        enemies.getForWrite(i).gainStrength(1);
                    }
                }
            });
        }
    }

    public static class RunicDome extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.energyRefill += 1;
            state.prop.hasRunicDome = true;
        }
    }

    public static class RunicPyramid extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasRunicPyramid = true;
        }
    }

    public static class SacredBark extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasSacredBark = true;
        }
    }

    public static class SlaversCollar extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            if (isEliteFight(state) || isBossFight(state)) {
                state.energyRefill += 1;
            }
        }
    }

    public static class SneckoEye extends Relic {
        @Override List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return GameProperties.generateSneckoCards(cards);
        }

        public void gamePropertiesSetup(GameState state) {
            state.prop.setupSneckoIndexes();
            state.prop.registerSneckoDebuffCounter();
            state.prop.hasSneckoEye = true;
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getPlayerForWrite().applyDebuff(state, DebuffType.SNECKO, 1);
                }
            });
        }
    }

    public static class Sozu extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.energyRefill += 1;
        }
    }

    // Tiny House: No need to implement
    public static class VelvetChoker extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.energyRefill += 1;
            state.prop.registerCounter("VelvetChoker", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx + state.getCounterForRead()[counterIdx]] = 0.5f;
                    return idx + 7;
                }
                @Override public int getInputLenDelta() {
                    return 7;
                }
            });
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.getCounterForRead()[counterIdx] < 6) {
                        state.getCounterForWrite()[counterIdx]++;
                    }
                }
            });
            state.addStartOfTurnHandler(new GameEventHandler() {
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

    // Bloody Idol: No need to implement, well can get gold in combat...
    // Cultist Mask: No need to implement
    // todo: Enchiridion
    // Face Of Cleric: No need to implement
    // Golden Idol: No need to implement

    public static class GremlinVisage extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
                }
            });
        }
    }

    // todo: Mark of The Bloom
    // todo: Mutagenic Strength
    // Nloth's Gift: No need to implement
    // Nloth's Mask: No need to implement
    // todo: Necronomicon
    // Neows Blessing: No need to implement
    // todo: Nilrys Codex

    public static class OddMushroom extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasOddMushroom = true;
        }
    }

    public static class RedMask extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
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
        @Override public void gamePropertiesSetup(GameState state) {
            state.addStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int possibleCards = 0, diff = 0;
                    var seen = new boolean[state.prop.cardDict.length];
                    for (int i = 0; i < state.handArrLen; i++) {
                        if (state.prop.upgradeIdxes[state.getHandArrForRead()[i]] >= 0) {
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
                        if (state.prop.upgradeIdxes[state.getHandArrForRead()[i]] >= 0) {
                            if (r == 0) {
                                state.getHandArrForWrite()[i] = (short) state.prop.upgradeIdxes[state.getHandArrForRead()[i]];
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
            state.prop.hasBurningBlood = true;
            state.addEndOfBattleHandler("BurningBlood", new GameEventHandler(1) {
                @Override public void handle(GameState state) {
                    state.healPlayer(6);
                }
            });
        }
    }

    public static class RingOfSerpent extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.addStartOfTurnHandler("RingOfSerpant", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.turnNum == 1) {
                        state.draw(2);
                    }
                }
            });
        }
    }

    public static class RedSkull extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
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
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasPaperPhrog = true;
        }
    }

    public static class ChampionBelt extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasChampionBelt = true;
        }
    }

    // **********************************************************************************************************************************************
    // ********************************************************** Snecko Specific Relics ************************************************************
    // **********************************************************************************************************************************************

    public static class PaperCrane extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasPaperCrane = true;
        }
    }

    public static class SneckoSkull extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasSneckoSkull = true;
        }
    }

    public static class Tingsha extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasTingsha = true;
        }
    }

    public static class ToughBandages extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.hasToughBandages = true;
        }
    }

    // **********************************************************************************************************************************************
    // ********************************************************** Defect Specific Relics ************************************************************
    // **********************************************************************************************************************************************

    public static class CrackedOrb extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.channelOrb(OrbType.LIGHTNING);
                }
            });
        }
    }

    public static class DataDisk extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.gainFocus(1);
                }
            });
        }
    }

    public static class EmotionChip extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.registerCounter("EmotionChip", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = counter[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnDamageHandler(new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (damageDealt > 0) {
                        state.getCounterForWrite()[counterIdx] = 1;
                    }
                }
            });
            state.addStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
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
            state.prop.hasGoldPlatedCable = true;
        }
    }

    public static class NuclearBattery extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.channelOrb(OrbType.PLASMA);
                }
            });
        }
    }

    public static class RunicCapacitor extends Relic {
        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.maxNumOfOrbs = Math.min(state.prop.maxNumOfOrbs + 3, 10);
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.gainOrbSlot(3);
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
            state.prop.maxNumOfOrbs = 10;
            state.prop.registerCounter("Inserter", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] + 1) / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public void onRegister(int counterIdx) {
                    state.prop.inserterCounterIdx = counterIdx;
                }
            });
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = n;
                }
            });
            state.addStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx]++;
                    if (state.getCounterForWrite()[counterIdx] == 2) {
                        state.getCounterForWrite()[counterIdx] = 0;
                        state.gainOrbSlot(1);
                    }
                }
            });
            if (healthReward > 0) {
                state.prop.addExtraTrainingTarget("Inserter", this, new TrainingTarget() {
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
}
