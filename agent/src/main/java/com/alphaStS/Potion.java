package com.alphaStS;

import com.alphaStS.enemy.Enemy;

import java.util.List;
import java.util.Objects;

public abstract class Potion implements GameProperties.CounterRegistrant {
    boolean vulnEnemy;
    boolean weakEnemy;
    boolean changePlayerStrength;
    boolean changePlayerDexterity;
    boolean changePlayerArtifact;
    boolean selectEnemy;
    boolean healPlayer;
    boolean selectFromHand;
    boolean selectFromDiscard;
    int counterIdx = -1;

    public void setCounterIdx(GameProperties gameProperties, int idx) {
        counterIdx = idx;
    }

    public abstract GameActionCtx use(GameState state, int idx);
    List<Card> getPossibleGeneratedCards(List<Card> cards) { return List.of(); }
    public void gamePropertiesSetup(GameState state) {}

    public static class VulnerablePotion extends Potion {
        public VulnerablePotion() {
            vulnEnemy = true;
            selectEnemy = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.prop.hasSacredBark ? 6 : 3;
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.VULNERABLE, n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Vulnerable Potion";
        }
    }

    public static class WeakPotion extends Potion {
        public WeakPotion() {
            weakEnemy = true;
            selectEnemy = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.prop.hasSacredBark ? 6 : 3;
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Weak Potion";
        }
    }

    public static class FirePotion extends Potion {
        public FirePotion() {
            selectEnemy = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.prop.hasSacredBark ? 40 : 20;
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, true);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Fire Potion";
        }
    }

    public static class StrengthPotion extends Potion {
        public StrengthPotion() {
            changePlayerStrength = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.prop.hasSacredBark ? 4 : 2;
            state.getPlayerForWrite().gainStrength(n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Strength Potion";
        }
    }

    public static class DexterityPotion extends Potion {
        public DexterityPotion() {
            changePlayerDexterity = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.prop.hasSacredBark ? 4 : 2;
            state.getPlayerForWrite().gainDexterity(n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Dexterity Potion";
        }
    }

    public static class BloodPotion extends Potion {
        public BloodPotion() {
            healPlayer = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.prop.hasSacredBark ? 4 : 2;
            state.healPlayer(state.getPlayeForRead().getMaxHealth() * n / 10);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Blood Potion";
        }
    }

    public static class AncientPotion extends Potion {
        public AncientPotion() {
            changePlayerArtifact = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.prop.hasSacredBark ? 2 : 1;
            state.getPlayerForWrite().gainArtifact(n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Ancient Potion";
        }
    }

    public static class EnergyPotion extends Potion {
        public EnergyPotion() {
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.prop.hasSacredBark ? 4 : 2;
            state.gainEnergy(n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Energy Potion";
        }
    }

    public static class LiquidMemory extends Potion {
        public LiquidMemory() {
            selectFromDiscard = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int cardsInHand = 0;
            for (int i = 0; i < state.hand.length; i++) {
                cardsInHand += state.hand[i];
            }
            if (cardsInHand >= GameState.HAND_LIMIT) {
                return GameActionCtx.PLAY_CARD; // tested, potion is wasted
            }
            if (state.prop.tmpCostCardIdxes[idx] >= 0) {
                state.removeCardFromDiscard(idx);
                state.addCardToHand(state.prop.tmpCostCardIdxes[idx]);
            } else {
                state.removeCardFromDiscard(idx);
                state.addCardToHand(idx);
            }
            if (state.prop.hasSacredBark) {
                state.getCounterForWrite()[counterIdx]++;
                if (state.getCounterForWrite()[counterIdx] == 2) {
                    state.getCounterForWrite()[counterIdx] = 0;
                    return GameActionCtx.PLAY_CARD;
                }
                return GameActionCtx.SELECT_CARD_DISCARD;
            } else {
                return GameActionCtx.PLAY_CARD;
            }
        }

        @Override List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return cards.stream().filter((x) -> !x.isXCost && x.energyCost > 0 && !(x instanceof Card.CardTmpChangeCost)).map((x) -> (Card) new Card.CardTmpChangeCost(x, 0)).toList();
        }

        @Override public String toString() {
            return "Liquid Memory";
        }

        public void gamePropertiesSetup(GameState state) {
            state.prop.registerCounter("LiquidMemory", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    int counter = state.getCounterForRead()[counterIdx];
                    input[idx] = counter / 2.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }
    }

    public static class DistilledChaos extends Potion {
        public DistilledChaos() {
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.prop.hasSacredBark ? 6 : 3;
            for (int i = 0; i < n; i++) {
                state.addGameActionToStartOfDeque(curState -> {
                    int cardIdx = curState.drawOneCardSpecial();
                    if (cardIdx < 0) {
                        return;
                    }
                    if (state.prop.makingRealMove) {
                        if (state.getStateDesc().length() > 0) state.stateDesc.append(", ");
                        state.getStateDesc().append(state.prop.cardDict[cardIdx].cardName);
                    }
                    var action = curState.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                    curState.playCard(action, -1, false, false, false);
                    while (curState.actionCtx == GameActionCtx.SELECT_ENEMY) {
                        int enemyIdx = GameStateUtils.getRandomEnemyIdx(curState, RandomGenCtx.RandomEnemyGeneral);
                        if (curState.prop.makingRealMove) {
                            curState.getStateDesc().append(" -> ").append(curState.getEnemiesForRead().get(enemyIdx).getName()).append(" (").append(enemyIdx).append(")");
                        }
                        curState.playCard(action, enemyIdx, false, false, false);
                    }
                });
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Distilled Chaos";
        }
    }

    public static class BlessingOfTheForge extends Potion {
        public BlessingOfTheForge() {
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            for (int i = 0; i < state.prop.upgradeIdxes.length; i++) {
                if (state.hand[i] > 0 && state.prop.upgradeIdxes[i] >= 0) {
                    state.hand[state.prop.upgradeIdxes[i]] += state.hand[i];
                    state.hand[i] = 0;
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return cards.stream().map((x) -> CardUpgrade.map.get(x)).filter(Objects::nonNull).toList();
        }

        @Override public String toString() {
            return "Blessing of the Forge";
        }
    }
}
