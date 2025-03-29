package com.alphaStS;

import com.alphaStS.card.*;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.enums.OrbType;
import com.alphaStS.utils.Tuple;
import com.alphaStS.utils.Tuple3;

import java.util.*;

public abstract class Potion implements GameProperties.CounterRegistrant {
    boolean selectCard1OutOf3;
    boolean vulnEnemy;
    boolean weakEnemy;
    boolean changePlayerStrength;
    boolean changePlayerStrengthEot;
    boolean changePlayerFocus;
    boolean changePlayerDexterity;
    boolean changePlayerDexterityEot;
    boolean changePlayerArtifact;
    boolean selectEnemy;
    boolean poisonEnemy;
    boolean healPlayer;
    boolean selectFromHand;
    boolean selectFromDiscard;
    boolean isGenerated;
    int generatedIdx;
    int counterIdx = -1;
    protected short basePenaltyRatio = 80;
    private int penaltyRatioSteps = 1;

    public short getBasePenaltyRatio() {
        return basePenaltyRatio;
    }

    public Potion setBasePenaltyRatio(int basePenaltyRatio) {
        this.basePenaltyRatio = (short) basePenaltyRatio;
        return this;
    }

    public int getPenaltyRatioSteps() {
        return penaltyRatioSteps;
    }

    public Potion setPenaltyRatioSteps(int penaltyRatioSteps) {
        this.penaltyRatioSteps = penaltyRatioSteps;
        return this;
    }

    public Potion setIsGenerated(boolean isGenerated, int idx) {
        this.isGenerated = isGenerated;
        this.generatedIdx = idx;
        return this;
    }

    public boolean getIsGenerated() {
        return isGenerated;
    }

    public void setCounterIdx(GameProperties gameProperties, int idx) {
        counterIdx = idx;
    }

    public int getCounterIdx(GameProperties gameProperties) {
        return counterIdx;
    }

    public abstract GameActionCtx use(GameState state, int idx);
    List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) { return List.of(); }
    List<Card> getPossibleSelect3OutOf1Cards(GameProperties gameProperties) { return List.of(); }
    public void gamePropertiesSetup(GameState state) {}


    public static class FearPotion extends Potion {
        public FearPotion() {
            vulnEnemy = true;
            selectEnemy = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.hasSacredBark ? 6 : 3;
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.VULNERABLE, n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Fear Potion";
        }
    }

    public static class WeakPotion extends Potion {
        public WeakPotion() {
            weakEnemy = true;
            selectEnemy = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.hasSacredBark ? 6 : 3;
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
            int n = state.properties.hasSacredBark ? 40 : 20;
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, true);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Fire Potion";
        }
    }

    public static class ExplosivePotion extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.hasSacredBark ? 20 : 10;
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, n);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Explosive Potion";
        }
    }

    public static class CunningPotion extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.hasSacredBark ? 6 : 3;
            for (int i = 0; i < n; i++) {
                state.addCardToHand(state.properties.shivPCardIdx);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            return List.of(new CardColorless.ShivP());
        }

        @Override public String toString() {
            return "Cunning Potion";
        }
    }

    public static class PoisonPotion extends Potion {
        public PoisonPotion() {
            selectEnemy = true;
            poisonEnemy = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.hasSacredBark ? 12 : 6;
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.POISON, n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Poison Potion";
        }
    }

    public static class StrengthPotion extends Potion {
        public StrengthPotion() {
            changePlayerStrength = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.hasSacredBark ? 4 : 2;
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
            int n = state.properties.hasSacredBark ? 4 : 2;
            state.getPlayerForWrite().gainDexterity(n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Dexterity Potion";
        }
    }

    public static class FlexPotion extends Potion {
        public FlexPotion() {
            changePlayerStrength = true;
            changePlayerStrengthEot = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.hasSacredBark ? 10 : 5;
            state.getPlayerForWrite().gainStrength(n);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Flex Potion";
        }
    }

    public static class SpeedPotion extends Potion {
        public SpeedPotion() {
            changePlayerDexterity = true;
            changePlayerDexterityEot = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.hasSacredBark ? 10 : 5;
            state.getPlayerForWrite().gainDexterity(n);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_DEXTERITY_EOT, n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Speed Potion";
        }
    }

    public static class BlockPotion extends Potion {
        public int getBlockAmount(GameState state) {
            return state.properties.hasSacredBark ? 24 : 12;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.getPlayerForWrite().gainBlockNotFromCardPlay(getBlockAmount(state));
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Block Potion";
        }
    }

    public static class DuplicationPotion extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += state.properties.hasSacredBark ? 2 : 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("DuplicationPotion", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = Math.abs(state.getCounterForRead()[counterIdx]) / 4.0f;
                    input[idx + 1] = (state.getCounterForRead()[counterIdx] & (1 << 8)) > 0 ? 0.5f : 0;
                    return idx + 2;
                }
                @Override public int getInputLenDelta() {
                    return 2;
                }
            });
            state.properties.addStartOfTurnHandler("DuplicationPotion", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForWrite()[counterIdx] != 0) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
            state.properties.addOnCardPlayedHandler("DuplicationPotion", new GameEventCardHandler(GameEventCardHandler.CLONE_CARD_PRIORITY) {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.getCounterForRead()[counterIdx] == 0) {
                        return;
                    }
                    if (cloneSource != null) {
                        if ((state.getCounterForRead()[counterIdx] & (1 << 8)) > 0) {
                            state.getCounterForWrite()[counterIdx] ^= 1 << 8;
                        }
                    } else {
                        var counters = state.getCounterForWrite();
                        counters[counterIdx]--;
                        counters[counterIdx] |= 1 << 8;
                        state.addGameActionToEndOfDeque(curState -> {
                            var action = curState.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                            if (curState.playCard(action, lastIdx, true, DuplicationPotion.class, false, false, energyUsed, cloneParentLocation)) {
                            } else {
                                curState.getCounterForWrite()[counterIdx] ^= 1 << 8;
                            }
                        });
                    }
                }
            });
        }

        @Override public String toString() {
            return "Duplication Potion";
        }
    }

    public static class DrawPotion extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.draw(3);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Draw Potion";
        }
    }

    public static class PotionOfCapacity extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.gainOrbSlot(state.properties.hasSacredBark ? 4 : 2);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Potion Of Capacity";
        }

        public void gamePropertiesSetup(GameState state) {
            state.properties.maxNumOfOrbs = Math.min(state.properties.maxNumOfOrbs + (state.properties.hasSacredBark ? 4 : 2), 10);
        }
    }

    public static class EssenceOfDarkness extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            if (state.getOrbs() != null) {
                var count = state.getOrbs().length / 2;
                for (int i = 0; i < count; i++) {
                    state.channelOrb(OrbType.DARK);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Essence of Darkness";
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
            int n = state.properties.hasSacredBark ? 4 : 2;
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

    public static class RegenerationPotion extends Potion {
        public RegenerationPotion() {
            healPlayer = true;
        }

        public static int getRegenerationAmount(GameState state) {
            return state.properties.hasSacredBark ? 10 : 5;
        }

        public int getHealAmount(GameState state) {
            int n = state.properties.hasSacredBark ? 10 : 5;
            return n * (n + 1) / 2;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += state.properties.hasSacredBark ? 10 : 5;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Regeneration Potion";
        }

        public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Regeneration", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    int counter = state.getCounterForRead()[counterIdx];
                    input[idx] = counter / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public void onRegister(int counterIdx) {
                    state.properties.regenerationCounterIdx = counterIdx;
                }
            });
            // todo: when's timing of regeneration with regard to burn etc.
            state.properties.addPreEndOfTurnHandler("Regeneration", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getPlayerForWrite().heal(state.getCounterForWrite()[counterIdx]--);
                    }
                }
            });
        }
    }

    public static class AncientPotion extends Potion {
        public AncientPotion() {
            changePlayerArtifact = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.hasSacredBark ? 2 : 1;
            state.getPlayerForWrite().gainArtifact(n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Ancient Potion";
        }
    }

    public static class EnergyPotion extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.hasSacredBark ? 4 : 2;
            state.gainEnergy(n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Energy Potion";
        }
    }

    public static class SwiftPotion extends Potion {
        public SwiftPotion() {
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.hasSacredBark ? 6 : 3;
            state.draw(n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Swift Potion";
        }
    }

    public static class LiquidBronze extends Potion {
        public LiquidBronze() {
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += 3;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Liquid Bronze";
        }

        public void gamePropertiesSetup(GameState state) {
            state.properties.registerThornCounter(state, this);
        }
    }

    public static class LiquidMemory extends Potion {
        public LiquidMemory() {
            selectFromDiscard = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            if (state.handArrLen >= GameState.HAND_LIMIT || idx < 0) {
                return GameActionCtx.PLAY_CARD; // tested, potion is wasted
            }
            state.removeCardFromDiscard(idx);
            if (state.properties.tmp0CostCardTransformIdxes[idx] >= 0) {
                state.addCardToHand(state.properties.tmp0CostCardTransformIdxes[idx]);
            } else {
                state.addCardToHand(idx);
            }
            if (state.properties.hasSacredBark) {
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

        @Override List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            return cards.stream().filter((x) -> !x.isXCost && x.energyCost > 0 && !(x instanceof Card.CardTmpChangeCost)).map((x) -> (Card) new Card.CardTmpChangeCost(x, 0)).toList();
        }

        @Override public String toString() {
            return "Liquid Memory";
        }

        public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("LiquidMemory", this, new GameProperties.NetworkInputHandler() {
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
            state.getCounterForWrite()[counterIdx] += state.properties.hasSacredBark ? 6 : 3;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("DistilledChaos", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 6.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public void onRegister(int counterIdx) {
                    state.properties.distilledChaosCounterIdx = counterIdx;
                }
            });
        }
        @Override public String toString() {
            return "Distilled Chaos";
        }
    }

    public static class BlessingOfTheForge extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.handArrTransform(state.properties.upgradeIdxes);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            return cards.stream().map(Card::getUpgrade).filter(Objects::nonNull).toList();
        }

        @Override public String toString() {
            return "Blessing of the Forge";
        }
    }

    public static class EssenceOfSteel extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += state.properties.hasSacredBark ? 8 : 4;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Essence Of Steel";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Metallicize", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public void onRegister(int counterIdx) {
                    state.properties.registerMetallicizeHandler(state, counterIdx);
                }
            });
        }
    }

    public static class AttackPotion extends Potion {
        public AttackPotion() {
            selectCard1OutOf3 = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            boolean interactive = state.getSearchRandomGen() instanceof InteractiveMode.RandomGenInteractive;
            int idx1 = state.getSearchRandomGen().nextInt(state.properties.attackPotionIdxes.length, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + 255, state.properties.attackPotionIdxes) : null);
            int idx2 = state.getSearchRandomGen().nextInt(state.properties.attackPotionIdxes.length - 1, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + idx1, state.properties.attackPotionIdxes) : null);
            int idx3 = state.getSearchRandomGen().nextInt(state.properties.attackPotionIdxes.length - 2, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (idx2 << 8) + idx1, state.properties.attackPotionIdxes) : null);
            if (idx2 >= idx1) {
                idx2++;
            }
            if (idx3 >= Math.min(idx1, idx2)) {
                idx3++;
            }
            if (idx3 >= Math.max(idx1, idx2)) {
                idx3++;
            }
            state.setSelect1OutOf3Idxes(state.properties.attackPotionIdxes[idx1], state.properties.attackPotionIdxes[idx2], state.properties.attackPotionIdxes[idx3]);
            state.setIsStochastic();
            return GameActionCtx.SELECT_CARD_1_OUT_OF_3;
        }

        @Override public String toString() {
            return "Attack Potion";
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
            if (gameProperties.character == CharacterEnum.IRONCLAD) {
            } else if (gameProperties.character == CharacterEnum.SILENT) {
                return List.of(
                        new CardSilent.Bane().getTemporaryCostIfPossible(0),
                        new CardSilent.DaggerSpray().getTemporaryCostIfPossible(0),
                        new CardSilent.DaggerThrow().getTemporaryCostIfPossible(0),
                        new CardSilent.FlyingKnee().getTemporaryCostIfPossible(0),
                        new CardSilent.PoisonedStab().getTemporaryCostIfPossible(0),
                        new CardSilent.QuickSlash().getTemporaryCostIfPossible(0),
                        new CardSilent.Slice().getTemporaryCostIfPossible(0),
                        new CardSilent.SneakyStrike().getTemporaryCostIfPossible(0),
                        new CardSilent.SuckerPunch().getTemporaryCostIfPossible(0),
                        new CardSilent.AllOutAttack().getTemporaryCostIfPossible(0),
                        new CardSilent.Backstab().getTemporaryCostIfPossible(0),
                        new CardSilent.Choke().getTemporaryCostIfPossible(0),
                        new CardSilent.Dash().getTemporaryCostIfPossible(0),
                        new CardSilent.EndlessAgony().getTemporaryCostIfPossible(0),
                        new CardSilent.Eviscerate().getTemporaryCostIfPossible(0),
                        new CardSilent.Finisher().getTemporaryCostIfPossible(0),
                        new CardSilent.Flechette().getTemporaryCostIfPossible(0),
                        new CardSilent.HeelHook().getTemporaryCostIfPossible(0),
                        new CardSilent.MasterfulStab(6).getTemporaryCostIfPossible(0),
                        new CardSilent.Predator().getTemporaryCostIfPossible(0),
                        new CardSilent.RiddleWithHoles().getTemporaryCostIfPossible(0),
                        new CardSilent.Skewer().getTemporaryCostIfPossible(0),
                        new CardSilent.DieDieDie().getTemporaryCostIfPossible(0),
                        new CardSilent.GlassKnife().getTemporaryCostIfPossible(0),
                        new CardSilent.GrandFinale().getTemporaryCostIfPossible(0),
                        new CardSilent.Unload().getTemporaryCostIfPossible(0)
                );
            } else if (gameProperties.character == CharacterEnum.DEFECT) {
                return List.of(
                        new CardDefect.BallLightning().getTemporaryCostIfPossible(0),
                        new CardDefect.Barrage().getTemporaryCostIfPossible(0),
                        new CardDefect.BeamCell().getTemporaryCostIfPossible(0),
                        new CardDefect.Claw().getTemporaryCostIfPossible(0),
                        new CardDefect.ColdSnap().getTemporaryCostIfPossible(0),
                        new CardDefect.CompiledDriver().getTemporaryCostIfPossible(0),
                        new CardDefect.GoForTheEye().getTemporaryCostIfPossible(0),
                        new CardDefect.Rebound().getTemporaryCostIfPossible(0),
                        new CardDefect.Streamline().getTemporaryCostIfPossible(0),
                        new CardDefect.SweepingBeam().getTemporaryCostIfPossible(0),
                        new CardDefect.Blizzard().getTemporaryCostIfPossible(0),
                        new CardDefect.BullsEye().getTemporaryCostIfPossible(0),
                        new CardDefect.DoomAndGloom().getTemporaryCostIfPossible(0),
                        new CardDefect.FTL().getTemporaryCostIfPossible(0),
                        new CardDefect.Melter().getTemporaryCostIfPossible(0),
                        new CardDefect.RipAndTear().getTemporaryCostIfPossible(0),
                        new CardDefect.Scrape().getTemporaryCostIfPossible(0),
                        new CardDefect.Sunder().getTemporaryCostIfPossible(0),
                        new CardDefect.AllForOne(0, 0).getTemporaryCostIfPossible(0),
                        new CardDefect.CoreSurge().getTemporaryCostIfPossible(0),
                        new CardDefect.HyperBeam().getTemporaryCostIfPossible(0),
                        new CardDefect.MeteorStrike().getTemporaryCostIfPossible(0),
                        new CardDefect.ThunderStrike().getTemporaryCostIfPossible(0)
                );
            }
            throw new IllegalStateException();
        }

        public void gamePropertiesSetup(GameState state) {
            var cards = getPossibleSelect3OutOf1Cards(state.properties);
            state.properties.attackPotionIdxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.properties.attackPotionIdxes[i] = state.properties.select1OutOf3CardsReverseIdxes[state.properties.findCardIndex(cards.get(i))];
            }
        }
    }

    public static class SkillPotion extends Potion {
        public SkillPotion() {
            selectCard1OutOf3 = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            boolean interactive = state.getSearchRandomGen() instanceof InteractiveMode.RandomGenInteractive;
            int idx1 = state.getSearchRandomGen().nextInt(state.properties.skillPotionIdxes.length, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + 255, state.properties.skillPotionIdxes) : null);
            int idx2 = state.getSearchRandomGen().nextInt(state.properties.skillPotionIdxes.length - 1, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + idx1, state.properties.skillPotionIdxes) : null);
            int idx3 = state.getSearchRandomGen().nextInt(state.properties.skillPotionIdxes.length - 2, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (idx2 << 8) + idx1, state.properties.skillPotionIdxes) : null);
            if (idx2 >= idx1) {
                idx2++;
            }
            if (idx3 >= Math.min(idx1, idx2)) {
                idx3++;
            }
            if (idx3 >= Math.max(idx1, idx2)) {
                idx3++;
            }
            state.setSelect1OutOf3Idxes(state.properties.skillPotionIdxes[idx1], state.properties.skillPotionIdxes[idx2], state.properties.skillPotionIdxes[idx3]);
            state.setIsStochastic();
            return GameActionCtx.SELECT_CARD_1_OUT_OF_3;
        }

        @Override public String toString() {
            return "Skill Potion";
        }

        @Override List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            var c = getPossibleSelect3OutOf1Cards(gameProperties);
            var l = new ArrayList<>(c);
            for (Card card : c) {
                if (card instanceof Card.CardTmpChangeCost t) {
                    l.add(t.card);
                    if (t.card instanceof CardDefect.ForceField forceField) {
                        for (Card possibleGeneratedCard : forceField.getPossibleGeneratedCards(cards)) {
                            if (possibleGeneratedCard.energyCost > 0) {
                                l.add(new Card.CardTmpChangeCost(possibleGeneratedCard, 0));
                            }
                        }
                    }
                }
            }
            return l;
        }

        @Override List<Card> getPossibleSelect3OutOf1Cards(GameProperties gameProperties) {
            if (gameProperties.character == CharacterEnum.IRONCLAD) {
                return List.of(
                        new CardIronclad.Armanent().getTemporaryCostIfPossible(0),
                        new CardIronclad.Flex().getTemporaryCostIfPossible(0),
                        new CardIronclad.Havoc().getTemporaryCostIfPossible(0),
                        new CardIronclad.ShrugItOff().getTemporaryCostIfPossible(0),
                        new CardIronclad.TrueGrit().getTemporaryCostIfPossible(0),
                        new CardIronclad.Warcry().getTemporaryCostIfPossible(0),
                        new CardIronclad.BattleTrance().getTemporaryCostIfPossible(0),
                        new CardIronclad.Bloodletting().getTemporaryCostIfPossible(0),
                        new CardIronclad.BurningPact().getTemporaryCostIfPossible(0),
                        new CardIronclad.Disarm().getTemporaryCostIfPossible(0),
                        new CardIronclad.DualWield().getTemporaryCostIfPossible(0),
                        new CardIronclad.Entrench().getTemporaryCostIfPossible(0),
                        new CardIronclad.FlameBarrier().getTemporaryCostIfPossible(0),
                        new CardIronclad.GhostlyArmor().getTemporaryCostIfPossible(0),
                        new CardIronclad.InfernalBlade().getTemporaryCostIfPossible(0),
                        new CardIronclad.Intimidate().getTemporaryCostIfPossible(0),
                        new CardIronclad.PowerThrough().getTemporaryCostIfPossible(0),
                        new CardIronclad.Rage().getTemporaryCostIfPossible(0),
                        new CardIronclad.SecondWind().getTemporaryCostIfPossible(0),
                        new CardIronclad.SeeingRed().getTemporaryCostIfPossible(0),
                        new CardIronclad.Sentinel().getTemporaryCostIfPossible(0),
                        new CardIronclad.Shockwave().getTemporaryCostIfPossible(0),
                        new CardIronclad.SpotWeakness().getTemporaryCostIfPossible(0),
                        new CardIronclad.DoubleTap().getTemporaryCostIfPossible(0),
                        new CardIronclad.Exhume().getTemporaryCostIfPossible(0),
                        new CardIronclad.Impervious().getTemporaryCostIfPossible(0),
                        new CardIronclad.LimitBreak().getTemporaryCostIfPossible(0),
                        new CardIronclad.Offering().getTemporaryCostIfPossible(0)
                );
            } else if (gameProperties.character == CharacterEnum.SILENT) {
                return List.of(
                        new CardSilent.Acrobatics().getTemporaryCostIfPossible(0),
                        new CardSilent.Backflip().getTemporaryCostIfPossible(0),
                        new CardSilent.BladeDance().getTemporaryCostIfPossible(0),
                        new CardSilent.CloakAndDagger().getTemporaryCostIfPossible(0),
                        new CardSilent.DeadlyPoison().getTemporaryCostIfPossible(0),
                        new CardSilent.Deflect().getTemporaryCostIfPossible(0),
                        new CardSilent.DodgeAndRoll().getTemporaryCostIfPossible(0),
                        new CardSilent.Outmaneuver().getTemporaryCostIfPossible(0),
                        new CardSilent.PiercingWail().getTemporaryCostIfPossible(0),
                        new CardSilent.Prepared().getTemporaryCostIfPossible(0),
                        new CardSilent.Blur().getTemporaryCostIfPossible(0),
                        new CardSilent.BouncingFlask().getTemporaryCostIfPossible(0),
                        new CardSilent.CalculatedGamble().getTemporaryCostIfPossible(0),
                        new CardSilent.Catalyst().getTemporaryCostIfPossible(0),
                        new CardSilent.Concentrate().getTemporaryCostIfPossible(0),
                        new CardSilent.CripplingCloud().getTemporaryCostIfPossible(0),
                        new CardSilent.Distraction().getTemporaryCostIfPossible(0),
                        new CardSilent.EscapePlan().getTemporaryCostIfPossible(0),
                        new CardSilent.Expertise().getTemporaryCostIfPossible(0),
                        new CardSilent.LegSweep().getTemporaryCostIfPossible(0),
                        new CardSilent.Reflex().getTemporaryCostIfPossible(0),
                        new CardSilent.Setup(true).getTemporaryCostIfPossible(0),
                        new CardSilent.Tactician().getTemporaryCostIfPossible(0),
                        new CardSilent.Terror().getTemporaryCostIfPossible(0),
                        new CardSilent.Adrenaline().getTemporaryCostIfPossible(0),
                        new CardSilent.Alchemize(0, 0).getTemporaryCostIfPossible(0),
                        new CardSilent.BulletTime().getTemporaryCostIfPossible(0),
                        new CardSilent.Burst().getTemporaryCostIfPossible(0),
                        new CardSilent.CorpseExplosion().getTemporaryCostIfPossible(0),
                        new CardSilent.Doppelganger().getTemporaryCostIfPossible(0),
                        new CardSilent.Malaise().getTemporaryCostIfPossible(0),
                        new CardSilent.Nightmare().getTemporaryCostIfPossible(0),
                        new CardSilent.PhantasmalKiller().getTemporaryCostIfPossible(0),
                        new CardSilent.StormOfSteel().getTemporaryCostIfPossible(0)
                );
            } else if (gameProperties.character == CharacterEnum.DEFECT) {
                return List.of(
                        new CardDefect.ChargeBattery().getTemporaryCostIfPossible(0),
                        new CardDefect.Coolheaded().getTemporaryCostIfPossible(0),
                        new CardDefect.Hologram().getTemporaryCostIfPossible(0),
                        new CardDefect.Leap().getTemporaryCostIfPossible(0),
                        new CardDefect.Recursion().getTemporaryCostIfPossible(0),
                        new CardDefect.Stack().getTemporaryCostIfPossible(0),
                        new CardDefect.SteamBarrier().getTemporaryCostIfPossible(0),
                        new CardDefect.Turbo().getTemporaryCostIfPossible(0),
                        new CardDefect.Aggregate().getTemporaryCostIfPossible(0),
                        new CardDefect.AutoShields().getTemporaryCostIfPossible(0),
                        new CardDefect.BootSequence().getTemporaryCostIfPossible(0),
                        new CardDefect.Chaos().getTemporaryCostIfPossible(0),
                        new CardDefect.Chill().getTemporaryCostIfPossible(0),
                        new CardDefect.Consume().getTemporaryCostIfPossible(0),
                        new CardDefect.Darkness().getTemporaryCostIfPossible(0),
                        new CardDefect.DoubleEnergy().getTemporaryCostIfPossible(0),
                        new CardDefect.Equilibrium().getTemporaryCostIfPossible(0),
                        new CardDefect.ForceField().getTemporaryCostIfPossible(0),
                        new CardDefect.Fusion().getTemporaryCostIfPossible(0),
                        new CardDefect.GeneticAlgorithm(1, 0).getTemporaryCostIfPossible(0),
                        new CardDefect.Glacier().getTemporaryCostIfPossible(0),
                        new CardDefect.Overclock().getTemporaryCostIfPossible(0),
                        new CardDefect.Recycle().getTemporaryCostIfPossible(0),
                        new CardDefect.ReinforcedBody().getTemporaryCostIfPossible(0),
                        new CardDefect.Reprogram().getTemporaryCostIfPossible(0),
                        new CardDefect.Skim().getTemporaryCostIfPossible(0),
                        new CardDefect.Tempest().getTemporaryCostIfPossible(0),
                        new CardDefect.WhiteNoise().getTemporaryCostIfPossible(0),
                        new CardDefect.Amplify().getTemporaryCostIfPossible(0),
                        new CardDefect.Fission().getTemporaryCostIfPossible(0),
                        new CardDefect.MultiCast().getTemporaryCostIfPossible(0),
                        new CardDefect.Rainbow().getTemporaryCostIfPossible(0),
                        new CardDefect.Reboot().getTemporaryCostIfPossible(0),
                        new CardDefect.Seek().getTemporaryCostIfPossible(0)
                );
            } else {
                throw new IllegalArgumentException();
            }
        }

        public void gamePropertiesSetup(GameState state) {
            var cards = getPossibleSelect3OutOf1Cards(state.properties);
            state.properties.skillPotionIdxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.properties.skillPotionIdxes[i] = state.properties.select1OutOf3CardsReverseIdxes[state.properties.findCardIndex(cards.get(i))];
            }
        }
    }

    public static class PowerPotion extends Potion {
        public PowerPotion() {
            selectCard1OutOf3 = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            boolean interactive = state.getSearchRandomGen() instanceof InteractiveMode.RandomGenInteractive;
            int idx1 = state.getSearchRandomGen().nextInt(state.properties.powerPotionIdxes.length, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + 255, state.properties.powerPotionIdxes) : null);
            int idx2 = state.getSearchRandomGen().nextInt(state.properties.powerPotionIdxes.length - 1, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + idx1, state.properties.powerPotionIdxes) : null);
            int idx3 = state.getSearchRandomGen().nextInt(state.properties.powerPotionIdxes.length - 2, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (idx2 << 8) + idx1, state.properties.powerPotionIdxes) : null);
            if (idx2 >= idx1) {
                idx2++;
            }
            if (idx3 >= Math.min(idx1, idx2)) {
                idx3++;
            }
            if (idx3 >= Math.max(idx1, idx2)) {
                idx3++;
            }
            state.setSelect1OutOf3Idxes(state.properties.powerPotionIdxes[idx1], state.properties.powerPotionIdxes[idx2], state.properties.powerPotionIdxes[idx3]);
            state.setIsStochastic();
            return GameActionCtx.SELECT_CARD_1_OUT_OF_3;
        }

        @Override public String toString() {
            return "Power Potion";
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
            if (gameProperties.character == CharacterEnum.IRONCLAD) {
            } else if (gameProperties.character == CharacterEnum.SILENT) {
                return List.of(
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
                        new CardSilent.WraithForm().getTemporaryCostIfPossible(0)
                );
            } else if (gameProperties.character == CharacterEnum.DEFECT) {
                return List.of(
                        new CardDefect.Capacitor().getTemporaryCostIfPossible(0),
                        new CardDefect.Defragment().getTemporaryCostIfPossible(0),
                        new CardDefect.Heatsinks().getTemporaryCostIfPossible(0),
                        new CardDefect.HelloWorld().getTemporaryCostIfPossible(0),
                        new CardDefect.Loop().getTemporaryCostIfPossible(0),
                        new CardDefect.StaticDischarge().getTemporaryCostIfPossible(0),
                        new CardDefect.Storm().getTemporaryCostIfPossible(0),
                        new CardDefect.BiasedCognition().getTemporaryCostIfPossible(0),
                        new CardDefect.Buffer().getTemporaryCostIfPossible(0),
                        new CardDefect.CreativeAI().getTemporaryCostIfPossible(0),
                        new CardDefect.EchoForm().getTemporaryCostIfPossible(0),
                        new CardDefect.Electrodynamics().getTemporaryCostIfPossible(0),
                        new CardDefect.MachineLearning().getTemporaryCostIfPossible(0)
                );
            }
            throw new IllegalArgumentException();
        }

        public void gamePropertiesSetup(GameState state) {
            var cards = getPossibleSelect3OutOf1Cards(state.properties);
            state.properties.powerPotionIdxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.properties.powerPotionIdxes[i] = state.properties.select1OutOf3CardsReverseIdxes[state.properties.findCardIndex(cards.get(i))];
            }
        }
    }

    public static class ColorlessPotion extends Potion {
        public ColorlessPotion() {
            selectCard1OutOf3 = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            boolean interactive = state.getSearchRandomGen() instanceof InteractiveMode.RandomGenInteractive;
            int idx1 = state.getSearchRandomGen().nextInt(state.properties.colorlessPotionIdxes.length, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + 255, state.properties.colorlessPotionIdxes) : null);
            int idx2 = state.getSearchRandomGen().nextInt(state.properties.colorlessPotionIdxes.length - 1, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + idx1, state.properties.colorlessPotionIdxes) : null);
            int idx3 = state.getSearchRandomGen().nextInt(state.properties.colorlessPotionIdxes.length - 2, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (idx2 << 8) + idx1, state.properties.colorlessPotionIdxes) : null);
            if (idx2 >= idx1) {
                idx2++;
            }
            if (idx3 >= Math.min(idx1, idx2)) {
                idx3++;
            }
            if (idx3 >= Math.max(idx1, idx2)) {
                idx3++;
            }
            state.setSelect1OutOf3Idxes(state.properties.colorlessPotionIdxes[idx1], state.properties.colorlessPotionIdxes[idx2], state.properties.colorlessPotionIdxes[idx3]);
            state.setIsStochastic();
            return GameActionCtx.SELECT_CARD_1_OUT_OF_3;
        }

        @Override public String toString() {
            return "Colorless Potion";
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
                    new Card.CardTmpChangeCost(new CardColorless.ToBeImplemented("0"), 0),
                    new CardColorless.DramaticEntrance(),
                    new Card.CardTmpChangeCost(new CardColorless.ToBeImplemented("1"), 0),
                    new CardColorless.Finesse(),
                    new CardColorless.FlashOfSteel(),
                    new Card.CardTmpChangeCost(new CardColorless.ToBeImplemented("2"), 0),
                    new CardColorless.GoodInstincts(),
                    new CardColorless.Impatience(),
                    new Card.CardTmpChangeCost(new CardColorless.ToBeImplemented("3"), 0),
                    new Card.CardTmpChangeCost(new CardColorless.ToBeImplemented("4"), 0),
                    new Card.CardTmpChangeCost(new CardColorless.MindBlast(), 0),
                    new CardColorless.Panacea(),
                    new Card.CardTmpChangeCost(new CardColorless.ToBeImplemented("5"), 0),
                    new Card.CardTmpChangeCost(new CardColorless.ToBeImplemented("6"), 0),
                    new CardColorless.SwiftStrike(),
                    new CardColorless.Trip(),
                    new Card.CardTmpChangeCost(new CardColorless.Apotheosis(), 0),
                    new Card.CardTmpChangeCost(new CardColorless.ToBeImplemented("7"), 0),
                    new Card.CardTmpChangeCost(new CardColorless.HandOfGreed(0.1), 0),
                    new Card.CardTmpChangeCost(new CardColorless.ToBeImplemented("8"), 0),
                    new CardColorless.MasterOfStrategy(),
                    new Card.CardTmpChangeCost(new CardColorless.ToBeImplemented("9"), 0),
                    new Card.CardTmpChangeCost(new CardColorless.ToBeImplemented("10"), 0),
                    new CardColorless.Panacea(),
                    new Card.CardTmpChangeCost(new CardColorless.ToBeImplemented("11"), 0),
                    new CardColorless.SecretTechnique(),
                    new CardColorless.SecretWeapon(),
                    new Card.CardTmpChangeCost(new CardColorless.ToBeImplemented("12"), 0),
                    new CardColorless.ThinkingAhead(),
                    new Card.CardTmpChangeCost(new CardColorless.ToBeImplemented("13"), 0),
                    new Card.CardTmpChangeCost(new CardColorless.Violence(), 0)
            );
        }

        public void gamePropertiesSetup(GameState state) {
            var cards = getPossibleSelect3OutOf1Cards(state.properties);
            state.properties.colorlessPotionIdxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.properties.colorlessPotionIdxes[i] = state.properties.select1OutOf3CardsReverseIdxes[state.properties.findCardIndex(cards.get(i))];
            }
        }
    }

    public static class SneckoOil extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.draw(5);
            for (int i = 0; i < state.handArrLen; i++) {
                var snecko = state.properties.sneckoIdxes[state.getHandArrForRead()[i]];
                if (snecko[0] > 1) {
                    state.getHandArrForWrite()[i] = (short) snecko[state.getSearchRandomGen().nextInt(snecko[0], RandomGenCtx.Snecko, new Tuple<>(state, i)) + 1];
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            return gameProperties.generateSneckoCards(cards);
        }

        public void gamePropertiesSetup(GameState state) {
            state.properties.setupSneckoIndexes();
        }

        @Override public String toString() {
            return "Snecko Oil";
        }
    }

    public static class CultistPotion extends Potion {
        public CultistPotion() {
            changePlayerStrength = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += state.properties.hasSacredBark ? 2 : 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Cultist Potion";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("CultistPotion", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("CultistPotion", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getPlayerForWrite().gainStrength(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }
    }

    public static class GamblersBrew extends Potion {
        public GamblersBrew() {
            selectFromHand = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            if (idx >= 0 && idx < state.properties.cardDict.length) {
                state.discardCardFromHand(idx);
                state.getCounterForWrite()[counterIdx]++;
                return GameActionCtx.SELECT_CARD_HAND;
            }
            state.draw(state.getCounterForRead()[counterIdx]);
            state.getCounterForWrite()[counterIdx] = 0;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Gambler's Brew";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("GamblersBrew", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }
    }

    public static class FairyInABottle extends Potion {
        protected final int playerMaxHp;

        public FairyInABottle(int playerMaxHP) {
            this.playerMaxHp = playerMaxHP;
        }

        public int getHealAmount(GameState state) {
            return (int) ((state.properties.hasSacredBark ? 0.6 : 0.3) * this.playerMaxHp);
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.getPlayerForWrite().heal(getHealAmount(state));
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Fairy In A Bottle";
        }
    }

    public static class LizardTail extends FairyInABottle {
        public LizardTail(int playerMaxHP) {
            super(playerMaxHP);
        }

        public int getHealAmount(GameState state) {
            return (int) (0.5 * this.playerMaxHp);
        }

        @Override public String toString() {
            return "Lizard Tail";
        }
    }

    public static class FocusPotion extends Potion {
        public FocusPotion() {
            changePlayerFocus = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.gainFocus(state.properties.hasSacredBark ? 4 : 2);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Focus Potion";
        }
    }

    public static class SmokeBomb extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            for (int i = 0; i < state.getEnemiesForWrite().size(); i++) {
                if (state.getEnemiesForWrite().get(i).isAlive()) {
                    state.killEnemy(i, false);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Smoke Bomb";
        }
    }

    public static class EntropicBrew extends Potion {
        int possibleGeneratedPotions;

        public EntropicBrew() {
        }

        public EntropicBrew(int possibleGeneratedPotions) {
            this.possibleGeneratedPotions = possibleGeneratedPotions;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int numPotions = state.getPotionCount();
            for (int i = numPotions; i < state.properties.numOfPotionSlots; i++) {
                state.properties.potionsGenerator.generatePotion(state);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Entropic Brew";
        }
    }

    public static class PotionGenerator {
        public static final int UPRGADE_POTIONS = 1;
        public static final int CARD_GENERATION_POTIONS = 1 << 1;
        public static final int COLORLESS_POTION = 1 << 2;
        public static final int SNECKO_OIL = 1 << 3;
        public static final int POWER_POTION = 1 << 4;
        public static final int ATTACK_POTION = 1 << 5;
        public static final int SKILL_POTION = 1 << 6;

        int possibleGeneratedPotions;

        public List<List<Potion>> commonPotions = new ArrayList<>();
        public List<List<Potion>> uncommonPotions = new ArrayList<>();
        public List<List<Potion>> rarePotions = new ArrayList<>();

        public PotionGenerator(int possibleGeneratedPotions) {
            this.possibleGeneratedPotions = possibleGeneratedPotions;
        }

        public void generatePotion(GameState state) {
            state.setIsStochastic();
            int r = state.getSearchRandomGen().nextInt(100, RandomGenCtx.EntropicBrew, new Tuple<>(this, -1));
            Potion potion;
            if (r < 65) {
                int r2 = state.getSearchRandomGen().nextInt(commonPotions.size(), RandomGenCtx.EntropicBrew, new Tuple<>(this, r));
                potion = commonPotions.get(r2).get(state.getSearchRandomGen().nextInt(commonPotions.get(r2).size(), RandomGenCtx.EntropicBrew));
            } else if (r < 90) {
                int r2 = state.getSearchRandomGen().nextInt(uncommonPotions.size(), RandomGenCtx.EntropicBrew, new Tuple<>(this, r));
                potion = uncommonPotions.get(r2).get(state.getSearchRandomGen().nextInt(uncommonPotions.get(r2).size(), RandomGenCtx.EntropicBrew));
            } else {
                int r2 = state.getSearchRandomGen().nextInt(rarePotions.size(), RandomGenCtx.EntropicBrew, new Tuple<>(this, r));
                potion = rarePotions.get(r2).get(state.getSearchRandomGen().nextInt(rarePotions.get(r2).size(), RandomGenCtx.EntropicBrew));
            }
            for (int j = 0; j < state.properties.potions.size(); j++) {
                if (!state.properties.potions.get(j).isGenerated) {
                    continue;
                }
                if (state.properties.potions.get(j).getClass().equals(potion.getClass()) && !state.potionUsable(j)) {
                    state.setPotionUsable(j);
                    break;
                }
            }
        }

        public void initPossibleGeneratedPotions(CharacterEnum character, int maxHealth, int basePenaltyRatio) {
            if ((possibleGeneratedPotions & CARD_GENERATION_POTIONS) != 0 || (possibleGeneratedPotions & ATTACK_POTION) != 0) {
                commonPotions.add(List.of(
                        new AttackPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new AttackPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new AttackPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new AttackPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
            } else {
                commonPotions.add(List.of(
                        new EmptyPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
            }
            if ((possibleGeneratedPotions & UPRGADE_POTIONS) != 0) {
                commonPotions.add(List.of(
                        new BlessingOfTheForge().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new BlessingOfTheForge().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new BlessingOfTheForge().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new BlessingOfTheForge().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
            } else {
                commonPotions.add(List.of(
                        new EmptyPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
            }
            commonPotions.add(List.of(
                    new BlockPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new BlockPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new BlockPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new BlockPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            if ((possibleGeneratedPotions & COLORLESS_POTION) != 0) {
                commonPotions.add(List.of(
                        new ColorlessPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new ColorlessPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new ColorlessPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new ColorlessPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
            } else {
                commonPotions.add(List.of(
                        new EmptyPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
            }
            commonPotions.add(List.of(
                    new DexterityPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new DexterityPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new DexterityPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new DexterityPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            commonPotions.add(List.of(
                    new EnergyPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new EnergyPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new EnergyPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new EnergyPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            commonPotions.add(List.of(
                    new ExplosivePotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new ExplosivePotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new ExplosivePotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new ExplosivePotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            commonPotions.add(List.of(
                    new FearPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new FearPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new FearPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new FearPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            commonPotions.add(List.of(
                    new FirePotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new FirePotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new FirePotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new FirePotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            commonPotions.add(List.of(
                    new FlexPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new FlexPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new FlexPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new FlexPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            if ((possibleGeneratedPotions & CARD_GENERATION_POTIONS) != 0 || (possibleGeneratedPotions & POWER_POTION) != 0) {
                commonPotions.add(List.of(
                        new PowerPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new PowerPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new PowerPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new PowerPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
            } else {
                commonPotions.add(List.of(
                        new EmptyPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
            }
            if ((possibleGeneratedPotions & CARD_GENERATION_POTIONS) != 0 || (possibleGeneratedPotions & SKILL_POTION) != 0) {
                commonPotions.add(List.of(
                        new SkillPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new SkillPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new SkillPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new SkillPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
            } else {
                commonPotions.add(List.of(
                        new EmptyPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
            }
            commonPotions.add(List.of(
                    new SpeedPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new SpeedPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new SpeedPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new SpeedPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            commonPotions.add(List.of(
                    new StrengthPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new StrengthPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new StrengthPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new StrengthPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            commonPotions.add(List.of(
                    new SwiftPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new SwiftPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new SwiftPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new SwiftPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            commonPotions.add(List.of(
                    new WeakPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new WeakPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new WeakPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new WeakPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            uncommonPotions.add(List.of(
                    new AncientPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new AncientPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new AncientPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new AncientPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            uncommonPotions.add(List.of(
                    new DistilledChaos().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new DistilledChaos().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new DistilledChaos().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new DistilledChaos().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            uncommonPotions.add(List.of(
                    new DuplicationPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new DuplicationPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new DuplicationPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new DuplicationPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            uncommonPotions.add(List.of(
                    new EssenceOfSteel().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new EssenceOfSteel().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new EssenceOfSteel().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new EssenceOfSteel().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            uncommonPotions.add(List.of(
                    new GamblersBrew().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new GamblersBrew().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new GamblersBrew().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new GamblersBrew().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            uncommonPotions.add(List.of(
                    new LiquidBronze().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new LiquidBronze().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new LiquidBronze().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new LiquidBronze().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            uncommonPotions.add(List.of(
                    new LiquidMemory().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new LiquidMemory().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new LiquidMemory().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new LiquidMemory().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            uncommonPotions.add(List.of(
                    new RegenerationPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new RegenerationPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new RegenerationPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new RegenerationPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            rarePotions.add(List.of(
                    new CultistPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new CultistPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new CultistPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new CultistPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            rarePotions.add(List.of(
                    new EntropicBrew(possibleGeneratedPotions).setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new EntropicBrew(possibleGeneratedPotions).setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new EntropicBrew(possibleGeneratedPotions).setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new EntropicBrew(possibleGeneratedPotions).setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            rarePotions.add(List.of(
                    new FairyInABottle(maxHealth).setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new FairyInABottle(maxHealth).setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new FairyInABottle(maxHealth).setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new FairyInABottle(maxHealth).setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            rarePotions.add(List.of(
                    new SmokeBomb().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                    new SmokeBomb().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                    new SmokeBomb().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                    new SmokeBomb().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
            ));
            if ((possibleGeneratedPotions & SNECKO_OIL) != 0) {
                rarePotions.add(List.of(
                        new SneckoOil().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new SneckoOil().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new SneckoOil().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new SneckoOil().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
            } else {
                commonPotions.add(List.of(
                        new EmptyPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new EmptyPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
            }
            if (character == CharacterEnum.IRONCLAD) {
                commonPotions.add(List.of(
                        new Potion.BloodPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new Potion.BloodPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new Potion.BloodPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new Potion.BloodPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
                // Elixir
                // HeartOfIron
            } else if (character == CharacterEnum.SILENT) {
                commonPotions.add(List.of(
                        new Potion.PoisonPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new Potion.PoisonPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new Potion.PoisonPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new Potion.PoisonPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
                uncommonPotions.add(List.of(
                        new CunningPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new CunningPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new CunningPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new CunningPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
                rarePotions.add(List.of(
                        new GhostInAJar().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new GhostInAJar().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new GhostInAJar().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new GhostInAJar().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
            } else if (character == CharacterEnum.DEFECT) {
                commonPotions.add(List.of(
                        new Potion.FocusPotion().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new Potion.FocusPotion().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new Potion.FocusPotion().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new Potion.FocusPotion().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
                uncommonPotions.add(List.of(
                        new PotionOfCapacity().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new PotionOfCapacity().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new PotionOfCapacity().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new PotionOfCapacity().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
                uncommonPotions.add(List.of(
                        new EssenceOfDarkness().setIsGenerated(true, 0).setBasePenaltyRatio(basePenaltyRatio),
                        new EssenceOfDarkness().setIsGenerated(true, 1).setBasePenaltyRatio(basePenaltyRatio),
                        new EssenceOfDarkness().setIsGenerated(true, 2).setBasePenaltyRatio(basePenaltyRatio),
                        new EssenceOfDarkness().setIsGenerated(true, 3).setBasePenaltyRatio(basePenaltyRatio)
                ));
            } else if (character == CharacterEnum.WATCHER) {
                // BottledMiracle
                // StancePotion
                // Ambrosia
            }
        }
    }

    public static class GhostInAJar extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.getCounterForWrite()[state.properties.intangibleCounterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerIntangibleCounter();
        }

        @Override public String toString() {
            return "Ghost In A jar";
        }
    }

    public static class EmptyPotion extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Empty Potion";
        }
    }
}
