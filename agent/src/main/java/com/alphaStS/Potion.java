package com.alphaStS;

import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyReadOnly;
import com.alphaStS.enums.CharacterEnum;
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
    int counterIdx = -1;
    private short basePenaltyRatio = 80;
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

    public static class ExplosivePotion extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.prop.hasSacredBark ? 20 : 10;
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
            int n = state.prop.hasSacredBark ? 6 : 3;
            for (int i = 0; i < n; i++) {
                state.addCardToHand(state.prop.shivPCardIdx);
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
            int n = state.prop.hasSacredBark ? 12 : 6;
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

    public static class FlexPotion extends Potion {
        public FlexPotion() {
            changePlayerStrength = true;
            changePlayerStrengthEot = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.prop.hasSacredBark ? 10 : 5;
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
            int n = state.prop.hasSacredBark ? 10 : 5;
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
            return state.prop.hasSacredBark ? 24 : 12;
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
            state.getCounterForWrite()[counterIdx] += state.prop.hasSacredBark ? 2 : 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.registerCounter("DuplicationPotion", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = Math.abs(state.getCounterForRead()[counterIdx]) / 4.0f;
                    input[idx + 1] = (state.getCounterForRead()[counterIdx] & (1 << 8)) > 0 ? 0.5f : 0;
                    return idx + 2;
                }
                @Override public int getInputLenDelta() {
                    return 2;
                }
            });
            state.addStartOfTurnHandler("DuplicationPotion", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForWrite()[counterIdx] != 0) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
            state.addOnCardPlayedHandler("DuplicationPotion", new GameEventCardHandler(GameEventCardHandler.CLONE_CARD_PRIORITY) {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.getCounterForRead()[counterIdx] == 0) {
                        return;
                    }
                    if (cloned) {
                        if ((state.getCounterForRead()[counterIdx] & (1 << 8)) > 0) {
                            state.getCounterForWrite()[counterIdx] ^= 1 << 8;
                        }
                    } else {
                        var counters = state.getCounterForWrite();
                        counters[counterIdx]--;
                        counters[counterIdx] |= 1 << 8;
                        state.addGameActionToEndOfDeque(curState -> {
                            var action = curState.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                            if (curState.playCard(action, lastIdx, true, true, false, false, energyUsed, cloneParentLocation)) {
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
            state.gainOrbSlot(state.prop.hasSacredBark ? 4 : 2);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Potion Of Capacity";
        }

        public void gamePropertiesSetup(GameState state) {
            state.prop.maxNumOfOrbs = Math.min(state.prop.maxNumOfOrbs + 2, 10);
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

    public static class RegenerationPotion extends Potion {
        public RegenerationPotion() {
            healPlayer = true;
        }

        public int getRegenerationAmount(GameState state) {
            return state.prop.hasSacredBark ? 10 : 5;
        }

        public int getHealAmount(GameState state) {
            int n = state.prop.hasSacredBark ? 10 : 5;
            return n * (n + 1) / 2;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += state.prop.hasSacredBark ? 10 : 5;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Regeneration Potion";
        }

        public void gamePropertiesSetup(GameState state) {
            state.prop.registerCounter("Regeneration", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    int counter = state.getCounterForRead()[counterIdx];
                    input[idx] = counter / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public void onRegister(int counterIdx) {
                    state.prop.regenerationCounterIdx = counterIdx;
                }
            });
            // todo: when's timing of regeneration with regard to burn etc.
            state.addPreEndOfTurnHandler("Regeneration", new GameEventHandler() {
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

    public static class SwiftPotion extends Potion {
        public SwiftPotion() {
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.prop.hasSacredBark ? 6 : 3;
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
            state.prop.registerThornCounter(state, this);
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
            if (state.prop.tmp0CostCardTransformIdxes[idx] >= 0) {
                state.addCardToHand(state.prop.tmp0CostCardTransformIdxes[idx]);
            } else {
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

        @Override List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
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
                    curState.playCard(action, -1, true,false, false, false, -1, -1);
                    while (curState.actionCtx == GameActionCtx.SELECT_ENEMY) {
                        int enemyIdx = GameStateUtils.getRandomEnemyIdx(curState, RandomGenCtx.RandomEnemyGeneral);
                        if (curState.prop.makingRealMove || state.prop.stateDescOn) {
                            curState.getStateDesc().append(" -> ").append(enemyIdx < 0 ? "None" : curState.getEnemiesForRead().get(enemyIdx).getName())
                                    .append(" (").append(enemyIdx).append(")");
                        }
                        curState.playCard(action, enemyIdx, true,false, false, false, -1, -1);
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
            state.handArrTransform(state.prop.upgradeIdxes);
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
                @Override public void onRegister(int counterIdx) {
                    state.prop.registerMetallicizeHandler(state, counterIdx);
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
            int idx1 = state.getSearchRandomGen().nextInt(state.prop.attackPotionIdxes.length, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + 255, state.prop.attackPotionIdxes) : null);
            int idx2 = state.getSearchRandomGen().nextInt(state.prop.attackPotionIdxes.length - 1, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + idx1, state.prop.attackPotionIdxes) : null);
            int idx3 = state.getSearchRandomGen().nextInt(state.prop.attackPotionIdxes.length - 2, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (idx2 << 8) + idx1, state.prop.attackPotionIdxes) : null);
            if (idx2 >= idx1) {
                idx2++;
            }
            if (idx3 >= Math.min(idx1, idx2)) {
                idx3++;
            }
            if (idx3 >= Math.max(idx1, idx2)) {
                idx3++;
            }
            state.setSelect1OutOf3Idxes(state.prop.attackPotionIdxes[idx1], state.prop.attackPotionIdxes[idx2], state.prop.attackPotionIdxes[idx3]);
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
            if (gameProperties.character == CharacterEnum.DEFECT) {
                return List.of(
                        new Card.CardTmpChangeCost(new CardDefect.BallLightning(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.Barrage(), 0),
                        new CardDefect.BeamCell(),
                        new CardDefect.Claw(),
                        new Card.CardTmpChangeCost(new CardDefect.ColdSnap(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.CompiledDriver(), 0),
                        new CardDefect.GoForTheEye(),
                        new Card.CardTmpChangeCost(new CardDefect.Rebound(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.Streamline(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.SweepingBeam(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.Blizzard(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.BullsEye(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.DoomAndGloom(), 0),
                        new CardDefect.FTL(),
                        new Card.CardTmpChangeCost(new CardDefect.Melter(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.RipAndTear(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.Scrape(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.Sunder(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.AllForOne(0, 0), 0),
                        new Card.CardTmpChangeCost(new CardDefect.CoreSurge(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.HyperBeam(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.MeteorStrike(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.ThunderStrike(), 0)
                );
            }
            throw new IllegalStateException();
        }

        public void gamePropertiesSetup(GameState state) {
            var cards = getPossibleSelect3OutOf1Cards(state.prop);
            state.prop.attackPotionIdxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.prop.attackPotionIdxes[i] = state.prop.select1OutOf3CardsReverseIdxes[state.prop.findCardIndex(cards.get(i))];
            }
        }
    }

    public static class SkillPotion extends Potion {
        public SkillPotion() {
            selectCard1OutOf3 = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            boolean interactive = state.getSearchRandomGen() instanceof InteractiveMode.RandomGenInteractive;
            int idx1 = state.getSearchRandomGen().nextInt(state.prop.skillPotionIdxes.length, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + 255, state.prop.skillPotionIdxes) : null);
            int idx2 = state.getSearchRandomGen().nextInt(state.prop.skillPotionIdxes.length - 1, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + idx1, state.prop.skillPotionIdxes) : null);
            int idx3 = state.getSearchRandomGen().nextInt(state.prop.skillPotionIdxes.length - 2, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (idx2 << 8) + idx1, state.prop.skillPotionIdxes) : null);
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
            if (gameProperties.character == CharacterEnum.SILENT) {
                return List.of(
                        new Card.CardTmpChangeCost(new CardSilent.Acrobatics(), 0),
                        new Card.CardTmpChangeCost(new CardSilent.Backflip(), 0),
                        new Card.CardTmpChangeCost(new CardSilent.BladeDance(), 0),
                        new Card.CardTmpChangeCost(new CardSilent.CloakAndDagger(), 0),
                        new Card.CardTmpChangeCost(new CardSilent.DeadlyPoison(), 0),
                        new CardSilent.Deflect(),
                        new Card.CardTmpChangeCost(new CardSilent.DodgeAndRoll(), 0),
                        new Card.CardTmpChangeCost(new CardSilent.Outmaneuver(), 0),
                        new Card.CardTmpChangeCost(new CardSilent.PiercingWail(), 0),
                        new CardSilent.Prepared(),
                        new Card.CardTmpChangeCost(new CardSilent.Blur(), 0),
                        new Card.CardTmpChangeCost(new CardSilent.BouncingFlask(), 0),
                        new CardSilent.CalculatedGamble(),
                        new Card.CardTmpChangeCost(new CardSilent.Catalyst(), 0),
                        new CardSilent.Concentrate(),
                        new Card.CardTmpChangeCost(new CardSilent.CripplingCloud(), 0),
                        new Card.CardTmpChangeCost(new CardSilent.Distraction(), 0),
                        new CardSilent.EscapePlan(),
                        new Card.CardTmpChangeCost(new CardSilent.Expertise(), 0),
                        new Card.CardTmpChangeCost(new CardSilent.LegSweep(), 0),
                        new CardSilent.Reflex(),
                        new Card.CardTmpChangeCost(new CardSilent.Setup(), 0),
                        new CardSilent.Tactician(),
                        new Card.CardTmpChangeCost(new CardSilent.Terror(), 0),
                        new CardSilent.Adrenaline(),
                        new Card.CardTmpChangeCost(new CardSilent.BulletTime(), 0),
                        new Card.CardTmpChangeCost(new CardSilent.Burst(), 0),
                        new Card.CardTmpChangeCost(new CardSilent.CorpseExplosion(), 0),
                        new CardSilent.Doppelganger(),
                        new CardSilent.Malaise(),
                        new Card.CardTmpChangeCost(new CardSilent.Nightmare(), 0),
                        new Card.CardTmpChangeCost(new CardSilent.PhantasmalKiller(), 0),
                        new Card.CardTmpChangeCost(new CardSilent.StormOfSteel(), 0)
                );
            }
            // todo: need to pass in character
            return List.of(
                    new Card.CardTmpChangeCost(new Card.Armanent(), 0),
                    new Card.Flex(),
                    new Card.CardTmpChangeCost(new Card.Havoc(), 0),
                    new Card.CardTmpChangeCost(new Card.ShrugItOff(), 0),
                    new Card.CardTmpChangeCost(new Card.TrueGrit(), 0),
                    new Card.Warcry(),
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
            var cards = getPossibleSelect3OutOf1Cards(state.prop);
            state.prop.skillPotionIdxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.prop.skillPotionIdxes[i] = state.prop.select1OutOf3CardsReverseIdxes[state.prop.findCardIndex(cards.get(i))];
            }
        }
    }

    public static class PowerPotion extends Potion {
        public PowerPotion() {
            selectCard1OutOf3 = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            boolean interactive = state.getSearchRandomGen() instanceof InteractiveMode.RandomGenInteractive;
            int idx1 = state.getSearchRandomGen().nextInt(state.prop.powerPotionIdxes.length, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + 255, state.prop.potions) : null);
            int idx2 = state.getSearchRandomGen().nextInt(state.prop.powerPotionIdxes.length - 1, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + idx1, state.prop.potions) : null);
            int idx3 = state.getSearchRandomGen().nextInt(state.prop.powerPotionIdxes.length - 2, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (idx2 << 8) + idx1, state.prop.potions) : null);
            if (idx2 >= idx1) {
                idx2++;
            }
            if (idx3 >= Math.min(idx1, idx2)) {
                idx3++;
            }
            if (idx3 >= Math.max(idx1, idx2)) {
                idx3++;
            }
            state.setSelect1OutOf3Idxes(state.prop.powerPotionIdxes[idx1], state.prop.powerPotionIdxes[idx2], state.prop.powerPotionIdxes[idx3]);
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
            if (gameProperties.character == CharacterEnum.DEFECT) {
                return List.of(
                        new Card.CardTmpChangeCost(new CardDefect.Capacitor(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.Defragment(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.Heatsinks(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.HelloWorld(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.Loop(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.StaticDischarge(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.Storm(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.BiasedCognition(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.Buffer(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.CreativeAI(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.EchoForm(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.Electrodynamics(), 0),
                        new Card.CardTmpChangeCost(new CardDefect.MachineLearning(), 0)
                );
            }
            throw new IllegalArgumentException();
        }

        public void gamePropertiesSetup(GameState state) {
            var cards = getPossibleSelect3OutOf1Cards(state.prop);
            state.prop.powerPotionIdxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.prop.powerPotionIdxes[i] = state.prop.select1OutOf3CardsReverseIdxes[state.prop.findCardIndex(cards.get(i))];
            }
        }
    }

    public static class ColorlessPotion extends Potion {
        public ColorlessPotion() {
            selectCard1OutOf3 = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            boolean interactive = state.getSearchRandomGen() instanceof InteractiveMode.RandomGenInteractive;
            int idx1 = state.getSearchRandomGen().nextInt(state.prop.colorlessPotionIdxes.length, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + 255, state.prop.colorlessPotionIdxes) : null);
            int idx2 = state.getSearchRandomGen().nextInt(state.prop.colorlessPotionIdxes.length - 1, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (255 << 8) + idx1, state.prop.colorlessPotionIdxes) : null);
            int idx3 = state.getSearchRandomGen().nextInt(state.prop.colorlessPotionIdxes.length - 2, RandomGenCtx.SelectCard1OutOf3,
                    interactive ? new Tuple3<>(state, (idx2 << 8) + idx1, state.prop.colorlessPotionIdxes) : null);
            if (idx2 >= idx1) {
                idx2++;
            }
            if (idx3 >= Math.min(idx1, idx2)) {
                idx3++;
            }
            if (idx3 >= Math.max(idx1, idx2)) {
                idx3++;
            }
            state.setSelect1OutOf3Idxes(state.prop.colorlessPotionIdxes[idx1], state.prop.colorlessPotionIdxes[idx2], state.prop.colorlessPotionIdxes[idx3]);
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
                    new Card.CardTmpChangeCost(new CardColorless.ToBeImplemented("14"), 0)
            );
        }

        public void gamePropertiesSetup(GameState state) {
            var cards = getPossibleSelect3OutOf1Cards(state.prop);
            state.prop.colorlessPotionIdxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.prop.colorlessPotionIdxes[i] = state.prop.select1OutOf3CardsReverseIdxes[state.prop.findCardIndex(cards.get(i))];
            }
        }
    }

    public static class SneckoPotion extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.draw(5);
            for (int i = 0; i < state.handArrLen; i++) {
                var snecko = state.prop.sneckoIdxes[state.getHandArrForRead()[i]];
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
            state.prop.setupSneckoIndexes();
        }

        @Override public String toString() {
            return "Snecko Potion";
        }
    }

    public static class CultistPotion extends Potion {
        public CultistPotion() {
            changePlayerStrength = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += state.prop.hasSacredBark ? 2 : 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Cultist Potion";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.registerCounter("CultistPotion", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addStartOfTurnHandler("CultistPotion", new GameEventHandler() {
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
            if (idx >= 0 && idx < state.prop.cardDict.length) {
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
            state.prop.registerCounter("GamblersBrew", this, new GameProperties.NetworkInputHandler() {
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
        private final int playerMaxHp;

        public FairyInABottle(int playerMaxHP) {
            this.playerMaxHp = playerMaxHP;
        }

        public int getHealAmount(GameState state) {
            return (int) ((state.prop.hasSacredBark ? 0.6 : 0.3) * this.playerMaxHp);
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.getPlayerForWrite().heal(getHealAmount(state));
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Fairy In A Bottle";
        }
    }

    public static class FocusPotion extends Potion {
        public FocusPotion() {
            changePlayerFocus = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.gainFocus(state.prop.hasSacredBark ? 4 : 2);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Focus Potion";
        }
    }

    public static class SmokeBomb extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            boolean gameEnd = false;
            for (EnemyReadOnly enemyReadOnly : state.getEnemiesForRead()) {
                if (!enemyReadOnly.isAlive()) {
                    gameEnd = true;
                }
            }
            if (gameEnd) {
                for (int i = 0; i < state.getEnemiesForWrite().size(); i++) {
                    if (state.getEnemiesForWrite().get(i).isAlive()) {
                        state.killEnemy(i, false);
                    }
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public String toString() {
            return "Smoke Bomb";
        }
    }
}
