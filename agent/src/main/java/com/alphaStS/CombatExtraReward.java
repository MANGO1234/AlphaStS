package com.alphaStS;

import com.alphaStS.enemy.PredefinedEncounter;
import com.alphaStS.utils.CounterStat;

public class CombatExtraReward {
    public enum CombatExtraRewardType {
        GOLD_REWARD
    }

    private double[] rewardScaling = new double[CombatExtraRewardType.values().length];

    public void setExtraReward(CombatExtraRewardType rewardType, double scaling) {
        double currentScaling = rewardScaling[rewardType.ordinal()];
        if (currentScaling != 0 && currentScaling != scaling) {
            throw new IllegalArgumentException("Conflicting combat extra reward scaling for " + rewardType);
        }
        rewardScaling[rewardType.ordinal()] = scaling;
    }

    public double getExtraReward(CombatExtraRewardType rewardType) {
        return rewardScaling[rewardType.ordinal()];
    }

    public void gamePropertiesSetup(GameState state) {
        if (state.properties.anyEntityProperty.possibleCombatRewards.contains(CombatExtraRewardType.GOLD_REWARD)) {
            setupGoldReward(state);
        }
    }

    private void setupGoldReward(GameState state) {
        double healthRewardRatio = getExtraReward(CombatExtraRewardType.GOLD_REWARD);
        state.properties.registerCounter("GoldReward", new GameProperties.CounterRegistrant() {
            @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
                gameProperties.goldRewardCounterIdx = idx;
            }

            @Override public int getCounterIdx(GameProperties gameProperties) {
                return gameProperties.goldRewardCounterIdx;
            }

            @Override public CounterStat getCounterStat() {
                return new CounterStat(state.properties.goldRewardCounterIdx, "Gold Reward").setShowFrequency(true);
            }
        }, new GameProperties.NetworkInputHandler() {
            @Override public int addToInput(GameState state, float[] input, int idx) {
                input[idx] = state.getCounterForRead()[state.properties.goldRewardCounterIdx] / 100.0f;
                return idx + 1;
            }

            @Override public int getInputLenDelta() {
                return 1;
            }
        });
        state.properties.addExtraTrainingTarget("GoldReward", new GameProperties.TrainingTargetRegistrant() {
            @Override public void setVExtraIdx(GameProperties properties, int idx) {
                properties.goldRewardVExtraIdx = idx;
            }
        }, new TrainingTarget() {
            @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                if (isTerminal > 0) {
                    v.setVExtra(state.properties.goldRewardVExtraIdx, getCurrentGoldReward(state) / 100.0);
                } else if (isTerminal == 0) {
                    double vGold = clampGoldRewardPrediction(state, state.getVExtra(state.properties.goldRewardVExtraIdx));
                    v.setVExtra(state.properties.goldRewardVExtraIdx, vGold);
                }
            }

            @Override public void updateQValues(GameState state, VArray v) {
                if (!state.isEncounter(PredefinedEncounter.CORRUPT_HEART)) {
                    double vGold = clampGoldRewardPrediction(state, v.getVExtra(state.properties.goldRewardVExtraIdx));
                    v.add(GameState.V_HEALTH_IDX, 100 * vGold * healthRewardRatio / 20.0 / state.getPlayerForRead().getMaxHealth());
                }
            }
        });
    }

    private static double clampGoldRewardPrediction(GameState state, double value) {
        int minGoldReward = getCurrentGoldReward(state);
        int maxGoldReward = getMaxPossibleGoldReward(state, true);
        return Math.max(minGoldReward / 100.0, Math.min(maxGoldReward / 100.0, value));
    }

    public static int getCurrentGoldReward(GameState state) {
        if (state.properties.goldRewardCounterIdx < 0) {
            return 0;
        }
        return state.getCounterForRead()[state.properties.goldRewardCounterIdx];
    }

    public static int getMaxPossibleGoldReward(GameState state, boolean checkEndOfGame) {
        if (state.properties.goldRewardCounterIdx < 0) {
            return 0;
        }
        if (checkEndOfGame && state.isTerminal() != 0) {
            return getCurrentGoldReward(state);
        }
        return getCurrentGoldReward(state) + getMaxPossibleGoldRewardRemaining(state);
    }

    public static int getMaxPossibleGoldRewardRemaining(GameState state) {
        return getMaxPossibleHandOfGreedRemaining(state, false);
    }

    public static int getMaxPossibleHandOfGreedRemaining(GameState state, boolean checkEndOfGame) {
        if (state.properties.goldRewardCounterIdx < 0) {
            return 0;
        }
        if (checkEndOfGame && state.isTerminal() != 0) {
            return 0;
        }
        int nonUpgrade = state.getNonExhaustCount("Hand of Greed");
        boolean canUpgrade = false; // todo
        if (!canUpgrade) {
            int upgrade = state.getNonExhaustCount("Hand of Greed+");
            if (upgrade > 0) {
                canUpgrade = true;
            }
        }
        if (nonUpgrade == 0) {
            return 0;
        }
        return canUpgrade ? 25 * state.enemiesAlive : 20 * state.enemiesAlive;
    }
}
