package com.alphaStS;

import com.alphaStS.player.PlayerReadOnly;
import com.alphaStS.utils.Tuple;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class Potion implements GameProperties.CounterRegistrant {
    boolean selectCard1OutOf3;
    boolean vulnEnemy;
    boolean weakEnemy;
    boolean changePlayerStrength;
    boolean changePlayerDexterity;
    boolean changePlayerDexterityEot;
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
    List<Card> getPossibleSelect3OutOf1Cards(List<Card> cards) { return List.of(); }
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

    public static class SpeedPotion extends Potion {
        public SpeedPotion() {
            changePlayerDexterity = true;
            changePlayerDexterityEot = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.prop.hasSacredBark ? 10 : 5;
            state.getPlayerForWrite().gainDexterity(n);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_DEXTERITY_EOT, n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Speed Potion";
        }
    }

    public static class BloodPotion extends Potion {
        int heal;

        public BloodPotion(int healHp) {
            healPlayer = true;
            heal = healHp;
        }

        public BloodPotion() {
            healPlayer = true;
        }

        public int getHealAmount(GameState state) {
            int n = state.prop.hasSacredBark ? 4 : 2;
            return heal == 0 ? state.getPlayeForRead().getMaxHealth() * n / 10 : heal;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.healPlayer(getHealAmount(state));
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
        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.prop.hasSacredBark ? 4 : 2;
            state.gainEnergy(n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Energy Potion";
        }
    }

    public static class DrawPotion extends Potion {
        public DrawPotion() {
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.prop.hasSacredBark ? 6 : 3;
            state.draw(n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Draw Potion";
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
        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.prop.hasSacredBark ? 6 : 3;
            for (int i = 0; i < n; i++) {
                state.addGameActionToStartOfDeque(curState -> {
                    int cardIdx = curState.drawOneCardSpecial();
                    if (cardIdx < 0) {
                        return;
                    }
                    if (state.prop.makingRealMove || state.prop.stateDescOn) {
                        if (state.getStateDesc().length() > 0) state.stateDesc.append(", ");
                        state.getStateDesc().append(state.prop.cardDict[cardIdx].cardName);
                    }
                    var action = curState.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                    curState.playCard(action, -1, true,false, false, false);
                    while (curState.actionCtx == GameActionCtx.SELECT_ENEMY) {
                        int enemyIdx = GameStateUtils.getRandomEnemyIdx(curState, RandomGenCtx.RandomEnemyGeneral);
                        if (curState.prop.makingRealMove || state.prop.stateDescOn) {
                            curState.getStateDesc().append(" -> ").append(enemyIdx < 0 ? "None" : curState.getEnemiesForRead().get(enemyIdx).getName())
                                    .append(" (").append(enemyIdx).append(")");
                        }
                        curState.playCard(action, enemyIdx, true,false, false, false);
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

    public static class EssenceOfSteel extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += state.prop.hasSacredBark ? 8 : 4;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Essence Of Steel";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.registerCounter("Metallicize", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addPreEndOfTurnHandler("Metallicize", new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.getPlayerForWrite().gainBlockNotFromCardPlay(state.getCounterForRead()[counterIdx]);
                }
            });
        }
    }

    public static class SkillPotion extends Potion {
        public SkillPotion() {
            selectCard1OutOf3 = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            boolean interactive = state.getSearchRandomGen() instanceof InteractiveMode.RandomGenInteractive;
            int idx1 = state.getSearchRandomGen().nextInt(state.prop.skillPotionIdxes.length, RandomGenCtx.SkillPotion,
                    interactive ? new Tuple<>(state, (255 << 8) + 255) : null);
            int idx2 = state.getSearchRandomGen().nextInt(state.prop.skillPotionIdxes.length - 1, RandomGenCtx.SkillPotion,
                    interactive ? new Tuple<>(state, (255 << 8) + idx1) : null);
            int idx3 = state.getSearchRandomGen().nextInt(state.prop.skillPotionIdxes.length - 2, RandomGenCtx.SkillPotion,
                    interactive ? new Tuple<>(state, (idx2 << 8) + idx1) : null);
            if (idx2 >= idx1) {
                idx2++;
            }
            if (idx3 >= Math.min(idx1, idx2)) {
                idx3++;
            }
            if (idx3 >= Math.max(idx1, idx2)) {
                idx3++;
            }
            state.setSelect1OutOf3Idxes(state.prop.skillPotionIdxes[idx1], state.prop.skillPotionIdxes[idx2], state.prop.skillPotionIdxes[idx3]);
            state.setIsStochastic();
            return GameActionCtx.SELECT_CARD_1_OUT_OF_3;
        }

        @Override public String toString() {
            return "Skill Potion";
        }

        @Override List<Card> getPossibleGeneratedCards(List<Card> cards) {
            // todo: need to pass in character
            return List.of(
                    new Card.Armanent(),
                    new Card.Flex(),
                    new Card.Havoc(),
                    new Card.ShrugItOff(),
                    new Card.TrueGrit(),
                    new Card.WarCry(),
                    new Card.BattleTrance(),
                    new Card.Bloodletting(),
                    new Card.BurningPact(),
                    new Card.Disarm(),
                    new Card.DualWield(),
                    new Card.Entrench(),
                    new Card.FlameBarrier(),
                    new Card.GhostlyArmor(),
                    new Card.FakeInfernalBlade(),
                    new Card.Intimidate(),
                    new Card.PowerThrough(),
                    new Card.Rage(),
                    new Card.SecondWind(),
                    new Card.SeeingRed(),
                    new Card.Sentinel(),
                    new Card.Shockwave(),
                    new Card.SpotWeakness(),
                    new Card.DoubleTap(),
                    new Card.Exhume(),
                    new Card.Impervious(),
                    new Card.LimitBreak(),
                    new Card.Offering(),
                    new Card.CardTmpChangeCost(new Card.Armanent(), 0),
                    new Card.CardTmpChangeCost(new Card.Havoc(), 0),
                    new Card.CardTmpChangeCost(new Card.ShrugItOff(), 0),
                    new Card.CardTmpChangeCost(new Card.TrueGrit(), 0),
                    new Card.CardTmpChangeCost(new Card.BurningPact(), 0),
                    new Card.CardTmpChangeCost(new Card.Disarm(), 0),
                    new Card.CardTmpChangeCost(new Card.DualWield(), 0),
                    new Card.CardTmpChangeCost(new Card.Entrench(), 0),
                    new Card.CardTmpChangeCost(new Card.FlameBarrier(), 0),
                    new Card.CardTmpChangeCost(new Card.GhostlyArmor(), 0),
                    new Card.CardTmpChangeCost(new Card.FakeInfernalBlade(), 0),
                    new Card.CardTmpChangeCost(new Card.PowerThrough(), 0),
                    new Card.CardTmpChangeCost(new Card.SecondWind(), 0),
                    new Card.CardTmpChangeCost(new Card.Sentinel(), 0),
                    new Card.CardTmpChangeCost(new Card.Shockwave(), 0),
                    new Card.CardTmpChangeCost(new Card.SpotWeakness(), 0),
                    new Card.CardTmpChangeCost(new Card.DoubleTap(), 0),
                    new Card.CardTmpChangeCost(new Card.Exhume(), 0),
                    new Card.CardTmpChangeCost(new Card.Impervious(), 0),
                    new Card.CardTmpChangeCost(new Card.LimitBreak(), 0)
            );
        }

        @Override List<Card> getPossibleSelect3OutOf1Cards(List<Card> cards) {
            // todo: need to pass in character
            return List.of(
                    new Card.CardTmpChangeCost(new Card.Armanent(), 0),
                    new Card.Flex(),
                    new Card.CardTmpChangeCost(new Card.Havoc(), 0),
                    new Card.CardTmpChangeCost(new Card.ShrugItOff(), 0),
                    new Card.CardTmpChangeCost(new Card.TrueGrit(), 0),
                    new Card.WarCry(),
                    new Card.BattleTrance(),
                    new Card.Bloodletting(),
                    new Card.CardTmpChangeCost(new Card.BurningPact(), 0),
                    new Card.CardTmpChangeCost(new Card.Disarm(), 0),
                    new Card.CardTmpChangeCost(new Card.DualWield(), 0),
                    new Card.CardTmpChangeCost(new Card.Entrench(), 0),
                    new Card.CardTmpChangeCost(new Card.FlameBarrier(), 0),
                    new Card.CardTmpChangeCost(new Card.GhostlyArmor(), 0),
                    new Card.CardTmpChangeCost(new Card.FakeInfernalBlade(), 0),
                    new Card.Intimidate(),
                    new Card.CardTmpChangeCost(new Card.PowerThrough(), 0),
                    new Card.Rage(),
                    new Card.CardTmpChangeCost(new Card.SecondWind(), 0),
                    new Card.SeeingRed(),
                    new Card.CardTmpChangeCost(new Card.Sentinel(), 0),
                    new Card.CardTmpChangeCost(new Card.Shockwave(), 0),
                    new Card.CardTmpChangeCost(new Card.SpotWeakness(), 0),
                    new Card.CardTmpChangeCost(new Card.DoubleTap(), 0),
                    new Card.CardTmpChangeCost(new Card.Exhume(), 0),
                    new Card.CardTmpChangeCost(new Card.Impervious(), 0),
                    new Card.CardTmpChangeCost(new Card.LimitBreak(), 0),
                    new Card.Offering()
            );
        }

        public void gamePropertiesSetup(GameState state) {
            var cards = getPossibleSelect3OutOf1Cards(Arrays.asList(state.prop.cardDict));
            state.prop.skillPotionIdxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.prop.skillPotionIdxes[i] = state.prop.select1OutOf3CardsReverseIdxes[state.prop.findCardIndex(cards.get(i))];
            }
        }
    }
}
