package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.enums.OrbType;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.eventHandler.OnCardCreationHandler;
import com.alphaStS.eventHandler.OnEnergySpendHandler;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.random.RandomGenCtx;
import com.alphaStS.utils.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CardDefect2 {
    // **************************************************************************************************
    // ********************************************* Basic  *********************************************
    // **************************************************************************************************

    public static class Defend extends Card.Defend {
    }

    public static class DefendP extends Card.DefendP {
    }

    public static class DualCast extends CardDefect.DualCast {
    }

    public static class DualCastP extends CardDefect.DualCastP {
    }

    public static class Strike extends Card.Strike {
    }

    public static class StrikeP extends Card.StrikeP {
    }

    public static class Zap extends CardDefect.Zap {
    }

    public static class ZapP extends CardDefect.ZapP {
    }

    // **************************************************************************************************
    // ********************************************* Common *********************************************
    // **************************************************************************************************

    public static class BallLightning extends CardDefect.BallLightning {
    }

    public static class BallLightningP extends CardDefect.BallLightningP {
    }

    public static class Barrage extends CardDefect._BarrageT {
        public Barrage() {
            super("Barrage", 5);
        }
    }

    public static class BarrageP extends CardDefect._BarrageT {
        public BarrageP() {
            super("Barrage+", 7);
        }
    }

    public static class BeamCell extends CardDefect.BeamCell {
    }

    public static class BeamCellP extends CardDefect.BeamCellP {
    }

    private static abstract class _BoostAwayT extends Card {
        private final int block;

        public _BoostAwayT(String cardName, int block) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            state.addCardToDiscard(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardOther.Dazed());
        }
    }

    public static class BoostAway extends _BoostAwayT {
        public BoostAway() {
            super("Boost Away", 6);
        }
    }

    public static class BoostAwayP extends _BoostAwayT {
        public BoostAwayP() {
            super("Boost Away+", 9);
        }
    }

    public static class ChargeBattery extends CardDefect.ChargeBattery {
    }

    public static class ChargeBatteryP extends CardDefect.ChargeBatteryP {
    }

    // TODO: Claw (Common) - 0 energy, Attack
    //   Effect: Deal 3 damage. Increase the damage of ALL Claw cards by 2 this combat.
    //   Upgraded Effect: Deal 4 damage. Increase the damage of ALL Claw cards by 3 this combat.

    public static class ColdSnap extends CardDefect.ColdSnap {
    }

    public static class ColdSnapP extends CardDefect.ColdSnapP {
    }

    public static class CompileDriver extends CardDefect.CompileDriver {
    }

    public static class CompileDriverP extends CardDefect.CompileDriverP {
    }

    public static class Coolheaded extends CardDefect.Coolheaded {
    }

    public static class CoolheadedP extends CardDefect.CoolheadedP {
    }

    private static abstract class _FocusedStrikeT extends Card {
        private final int damage;
        private final int focus;

        public _FocusedStrikeT(String cardName, int damage, int focus) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.focus = focus;
            entityProperty.selectEnemy = true;
            entityProperty.changePlayerFocus = true;
            entityProperty.changePlayerFocusEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            state.gainFocus(focus);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_FOCUS_EOT, focus);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FocusedStrike extends _FocusedStrikeT {
        public FocusedStrike() {
            super("Focused Strike", 9, 1);
        }
    }

    public static class FocusedStrikeP extends _FocusedStrikeT {
        public FocusedStrikeP() {
            super("Focused Strike+", 11, 2);
        }
    }

    public static class GoForTheEye extends CardDefect.GoForTheEye {
    }

    public static class GoForTheEyeP extends CardDefect.GoForTheEyeP {
    }

    private static abstract class _GunkUpT extends Card {
        private final int damage;

        public _GunkUpT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage);
            state.playerDoDamageToEnemy(enemy, damage);
            state.playerDoDamageToEnemy(enemy, damage);
            state.addCardToDiscard(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardOther.Slime());
        }
    }

    public static class GunkUp extends _GunkUpT {
        public GunkUp() {
            super("Gunk Up", 4);
        }
    }

    public static class GunkUpP extends _GunkUpT {
        public GunkUpP() {
            super("Gunk Up+", 5);
        }
    }

    public static class Hologram extends CardDefect.Hologram {
    }

    public static class HologramP extends CardDefect.HologramP {
    }

    private static abstract class _HotfixT extends Card {
        private final int focus;

        public _HotfixT(String cardName, int focus) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.focus = focus;
            entityProperty.changePlayerFocus = true;
            entityProperty.changePlayerFocusEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainFocus(focus);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_FOCUS_EOT, focus);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Hotfix extends _HotfixT {
        public Hotfix() {
            super("Hotfix", 2);
        }
    }

    public static class HotfixP extends _HotfixT {
        public HotfixP() {
            super("Hotfix+", 3);
        }
    }

    public static class Leap extends CardDefect.Leap {
    }

    public static class LeapP extends CardDefect.LeapP {
    }

    private static abstract class _LightningRodT extends Card {
        private final int block;

        public _LightningRodT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.block = block;
            entityProperty.orbGenerationPossible |= OrbType.LIGHTNING.mask;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            state.getCounterForWrite()[counterIdx] += 2;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("LightningRod", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 4.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("LightningRod", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx]--;
                        state.channelOrb(OrbType.LIGHTNING);
                    }
                }
            });
        }
    }

    public static class LightningRod extends _LightningRodT {
        public LightningRod() {
            super("Lightning Rod", 4);
        }
    }

    public static class LightningRodP extends _LightningRodT {
        public LightningRodP() {
            super("Lightning Rod+", 7);
        }
    }

    private static abstract class _MomentumStrikeT extends Card {
        private final int damage;

        public _MomentumStrikeT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return cards.stream()
                    .filter(c -> c.getBaseCard() instanceof _MomentumStrikeT)
                    .map(c -> c.getPermCostIfPossible(0))
                    .distinct()
                    .toList();
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.momentumStrikeTransformIndexes = new int[state.properties.cardDict.length];
            Arrays.fill(state.properties.momentumStrikeTransformIndexes, -1);
            for (int i = 0; i < state.properties.momentumStrikeTransformIndexes.length; i++) {
                if (state.properties.cardDict[i].getBaseCard() instanceof _MomentumStrikeT
                        && state.properties.cardDict[i].energyCost != 0) {
                    state.properties.momentumStrikeTransformIndexes[i] =
                            state.properties.findCardIndex(state.properties.cardDict[i].getPermCostIfPossible(0));
                }
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop, int cardIdx) {
            return prop.momentumStrikeTransformIndexes[cardIdx];
        }
    }

    public static class MomentumStrike extends _MomentumStrikeT {
        public MomentumStrike() {
            super("Momentum Strike", 10);
        }
    }

    public static class MomentumStrikeP extends _MomentumStrikeT {
        public MomentumStrikeP() {
            super("Momentum Strike+", 13);
        }
    }

    public static class SweepingBeam extends CardDefect.SweepingBeam {
    }

    public static class SweepingBeamP extends CardDefect.SweepingBeamP {
    }

    public static class Turbo extends CardDefect.Turbo {
    }

    public static class TurboP extends CardDefect.TurboP {
    }

    private static abstract class _UproarT extends Card {
        private final int damage;

        public _UproarT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage);
            state.playerDoDamageToEnemy(enemy, damage);
            int attackCount = 0;
            for (int i = 0; i < state.deckArrLen; i++) {
                if (state.properties.cardDict[state.deckArr[i]].cardType == Card.ATTACK) {
                    attackCount++;
                }
            }
            if (attackCount > 0) {
                int r = state.getSearchRandomGen().nextInt(attackCount, RandomGenCtx.RandomCardGen, new Tuple<>(state, null));
                int count = 0;
                for (int i = 0; i < state.deckArrLen; i++) {
                    int cardIdx = state.deckArr[i];
                    if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        if (count == r) {
                            state.removeCardFromDeck(cardIdx, false);
                            state.addCardToHand(cardIdx);
                            break;
                        }
                        count++;
                    }
                }
                if (attackCount > 1) {
                    state.setIsStochastic();
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Uproar extends _UproarT {
        public Uproar() {
            super("Uproar", 5);
        }
    }

    public static class UproarP extends _UproarT {
        public UproarP() {
            super("Uproar+", 7);
        }
    }

    // **************************************************************************************************
    // ********************************************* Uncommon *********************************************
    // **************************************************************************************************

    public static class BootSequence extends CardDefect.BootSequence {
    }

    public static class BootSequenceP extends CardDefect.BootSequenceP {
    }

    private static abstract class _BulkUpT extends Card {
        private final int n;

        public _BulkUpT(String cardName, int n) {
            super(cardName, Card.POWER, 2, Card.UNCOMMON);
            this.n = n;
            entityProperty.changePlayerStrength = true;
            entityProperty.changePlayerDexterity = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainOrbSlot(-1);
            state.getPlayerForWrite().gainStrength(n);
            state.getPlayerForWrite().gainDexterity(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BulkUp extends _BulkUpT {
        public BulkUp() {
            super("Bulk Up", 2);
        }
    }

    public static class BulkUpP extends _BulkUpT {
        public BulkUpP() {
            super("Bulk Up+", 3);
        }
    }

    public static class Capacitor extends CardDefect.Capacitor {
    }

    public static class CapacitorP extends CardDefect.CapacitorP {
    }

    public static class Chaos extends CardDefect.Chaos {
    }

    public static class ChaosP extends CardDefect.ChaosP {
    }

    public static class Chill extends CardDefect.Chill {
    }

    public static class ChillP extends CardDefect._ChillT {
        public ChillP() {
            super("Chill+", false, false);
        }
    }

    private static abstract class _CompactT extends Card {
        private final int block;

        public _CompactT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            var handArr = state.getHandArrForWrite();
            for (int i = 0; i < state.handArrLen; i++) {
                if (state.properties.cardDict[handArr[i]].cardType == Card.STATUS) {
                    handArr[i] = (short) generatedCardIdx;
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Compact extends _CompactT {
        public Compact() {
            super("Compact", 6);
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.Fuel());
        }
    }

    public static class CompactP extends _CompactT {
        public CompactP() {
            super("Compact+", 7);
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.FuelP());
        }
    }

    public static class Darkness extends CardDefect._DarknessT {
        public Darkness() {
            super("Darkness", 1);
        }
    }

    public static class DarknessP extends CardDefect._DarknessT {
        public DarknessP() {
            super("Darkness+", 2);
        }
    }

    public static class DoubleEnergy extends CardDefect.DoubleEnergy {
    }

    public static class DoubleEnergyP extends CardDefect.DoubleEnergyP {
    }

    // No need to implement Energy Surge: Multiplayer

    public static class FTL extends CardDefect.FTL {
    }

    public static class FTLP extends CardDefect.FTLP {
    }

    private static abstract class _FeralT extends Card {
        public _FeralT(String cardName, int energyCost) {
            super(cardName, Card.POWER, energyCost, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            // Upper nibble = stacks (permanent), lower nibble = remaining triggers this turn
            // Add 1 to each: +0x11 = +17
            state.getCounterForWrite()[counterIdx] += 0x11;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Feral", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = (state.getCounterForRead()[counterIdx] >> 4) / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Feral", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int stacks = state.getCounterForRead()[counterIdx] >> 4;
                    state.getCounterForWrite()[counterIdx] = (stacks << 4) | stacks;
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.feralCounterIdx = idx;
        }
    }

    public static class Feral extends _FeralT {
        public Feral() {
            super("Feral", 2);
        }
    }

    public static class FeralP extends _FeralT {
        public FeralP() {
            super("Feral+", 1);
        }
    }

    private static abstract class _FightThroughT extends Card {
        private final int block;

        public _FightThroughT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            state.addCardToDiscard(generatedCardIdx);
            state.addCardToDiscard(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardOther.Wound());
        }
    }

    public static class FightThrough extends _FightThroughT {
        public FightThrough() {
            super("Fight Through", 13);
        }
    }

    public static class FightThroughP extends _FightThroughT {
        public FightThroughP() {
            super("Fight Through+", 17);
        }
    }

    public static class Fusion extends CardDefect.Fusion {
    }

    public static class FusionP extends CardDefect.FusionP {
    }

    public static class Glacier extends CardDefect._GlacierT {
        public Glacier() {
            super("Glacier", 6);
        }
    }

    public static class GlacierP extends CardDefect._GlacierT {
        public GlacierP() {
            super("Glacier+", 9);
        }
    }

    private static abstract class _GlassworkT extends Card {
        private final int block;

        public _GlassworkT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
            entityProperty.orbGenerationPossible |= OrbType.GLASS.mask;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            state.channelOrb(OrbType.GLASS);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Glasswork extends _GlassworkT {
        public Glasswork() {
            super("Glasswork", 5);
        }
    }

    public static class GlassworkP extends _GlassworkT {
        public GlassworkP() {
            super("Glasswork+", 8);
        }
    }

    private static abstract class _HailstormT extends Card {
        private final int damage;

        public _HailstormT(String cardName, int damage) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addEndOfTurnHandler(cardName, new GameEventHandler() {
                @Override public void handle(GameState state) {
                    short[] orbs = state.getOrbs();
                    if (orbs == null) return;
                    for (int i = 0; i < orbs.length; i += 2) {
                        if (orbs[i] == OrbType.FROST.ordinal()) {
                            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                                state.playerDoNonAttackDamageToEnemy(enemy, damage, true);
                            }
                            return;
                        }
                    }
                }
            });
        }
    }

    public static class Hailstorm extends _HailstormT {
        public Hailstorm() {
            super("Hailstorm", 6);
        }
    }

    public static class HailstormP extends _HailstormT {
        public HailstormP() {
            super("Hailstorm+", 8);
        }
    }

    private static abstract class _IterationT extends Card {
        private final int draws;

        public _IterationT(String cardName, int draws) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.draws = draws;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            // counterIdx = total draws (stacks), counterIdx+1 = triggered-this-turn flag
            state.getCounterForWrite()[counterIdx] += draws;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Iteration", this, 2, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    input[idx + 1] = state.getCounterForRead()[counterIdx + 1];
                    return idx + 2;
                }

                @Override public int getInputLenDelta() {
                    return 2;
                }
            });
            state.properties.addOnCardDrawnHandler("Iteration", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.STATUS
                            && state.getCounterForRead()[counterIdx + 1] == 0
                            && state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx + 1] = 1;
                        state.draw(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
            state.properties.addStartOfTurnHandler("Iteration", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx + 1] = 0;
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.iterationCounterIdx = idx;
        }
    }

    public static class Iteration extends _IterationT {
        public Iteration() {
            super("Iteration", 2);
        }
    }

    public static class IterationP extends _IterationT {
        public IterationP() {
            super("Iteration+", 3);
        }
    }

    public static class Loop extends CardDefect.Loop {
    }

    public static class LoopP extends CardDefect.LoopP {
    }

    private static abstract class _NullT extends Card {
        private final int damage;
        private final int weak;

        public _NullT(String cardName, int damage, int weak) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            this.weak = weak;
            entityProperty.selectEnemy = true;
            entityProperty.orbGenerationPossible |= OrbType.DARK.mask;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage);
            enemy.applyDebuff(state, DebuffType.WEAK, weak);
            state.channelOrb(OrbType.DARK);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Null extends _NullT {
        public Null() {
            super("Null", 10, 2);
        }
    }

    public static class NullP extends _NullT {
        public NullP() {
            super("Null+", 13, 3);
        }
    }

    public static class Overclock extends CardDefect.Overclock {
    }

    public static class OverclockP extends CardDefect.OverclockP {
    }

    private static abstract class _RefractT extends Card {
        private final int damage;

        public _RefractT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 3, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            entityProperty.orbGenerationPossible |= OrbType.GLASS.mask;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage);
            state.playerDoDamageToEnemy(enemy, damage);
            state.channelOrb(OrbType.GLASS);
            state.channelOrb(OrbType.GLASS);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Refract extends _RefractT {
        public Refract() {
            super("Refract", 9);
        }
    }

    public static class RefractP extends _RefractT {
        public RefractP() {
            super("Refract+", 12);
        }
    }

    public static class RipAndTear extends CardDefect.RipAndTear {
    }

    public static class RipAndTearP extends CardDefect.RipAndTearP {
    }

    private static abstract class _RocketPunchT extends Card {
        private final int damage;
        private final int draw;

        public _RocketPunchT(String cardName, int damage, int draw) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            this.draw = draw;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            state.draw(draw);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var list = new ArrayList<Card>();
            for (Card card : cards) {
                if (card.getBaseCard() instanceof _RocketPunchT) {
                    list.add(card.getTemporaryCostIfPossible(0));
                }
            }
            return list;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var zeroCostIdx = new int[state.properties.cardDict.length];
            Arrays.fill(zeroCostIdx, -1);
            for (int i = 0; i < state.properties.cardDict.length; i++) {
                if (state.properties.cardDict[i].getBaseCard() instanceof _RocketPunchT) {
                    zeroCostIdx[i] = state.properties.findCardIndex(state.properties.cardDict[i].getTemporaryCostIfPossible(0));
                }
            }
            state.properties.addOnCardCreationHandler("RocketPunch", new OnCardCreationHandler() {
                @Override public void handle(GameState state, int cardIdx) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.STATUS) {
                        for (int i = 0; i < state.handArrLen; i++) {
                            if (zeroCostIdx[state.handArr[i]] >= 0) {
                                state.getHandArrForWrite()[i] = (short) zeroCostIdx[state.handArr[i]];
                            }
                        }
                    }
                }
            });
        }
    }

    public static class RocketPunch extends _RocketPunchT {
        public RocketPunch() {
            super("Rocket Punch", 13, 1);
        }
    }

    public static class RocketPunchP extends _RocketPunchT {
        public RocketPunchP() {
            super("Rocket Punch+", 14, 2);
        }
    }

    private static abstract class _ScavengeT extends Card {
        private final int energy;

        public _ScavengeT(String cardName, int energy) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.energy = energy;
            entityProperty.selectFromHand = true;
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.exhaustCardFromHand(idx);
            state.getCounterForWrite()[counterIdx] += energy;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerEnergyNextTurnCounter(state, this);
        }
    }

    public static class Scavenge extends _ScavengeT {
        public Scavenge() {
            super("Scavenge", 2);
        }
    }

    public static class ScavengeP extends _ScavengeT {
        public ScavengeP() {
            super("Scavenge+", 3);
        }
    }

    public static class Scrape extends CardDefect.Scrape {
    }

    public static class ScrapeP extends CardDefect.ScrapeP {
    }

    private static abstract class _ShadowShieldT extends Card {
        private final int block;

        public _ShadowShieldT(String cardName, int block) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
            this.block = block;
            entityProperty.orbGenerationPossible |= OrbType.DARK.mask;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            state.channelOrb(OrbType.DARK);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ShadowShield extends _ShadowShieldT {
        public ShadowShield() {
            super("Shadow Shield", 11);
        }
    }

    public static class ShadowShieldP extends _ShadowShieldT {
        public ShadowShieldP() {
            super("Shadow Shield+", 15);
        }
    }

    public static class Skim extends CardDefect.Skim {
    }

    public static class SkimP extends CardDefect.SkimP {
    }

    private static abstract class _SmokestackT extends Card {
        private final int n;

        public _SmokestackT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Smokestack", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardCreationHandler("Smokestack", new OnCardCreationHandler() {
                @Override public void handle(GameState state, int cardIdx) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.STATUS) {
                        int dmg = state.getCounterForRead()[counterIdx];
                        if (dmg > 0) {
                            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                                state.playerDoNonAttackDamageToEnemy(enemy, dmg, true);
                            }
                        }
                    }
                }
            });
        }
    }

    public static class Smokestack extends _SmokestackT {
        public Smokestack() {
            super("Smokestack", 5);
        }
    }

    public static class SmokestackP extends _SmokestackT {
        public SmokestackP() {
            super("Smokestack+", 7);
        }
    }

    public static class Storm extends CardDefect.Storm {
    }

    public static class StormP extends CardDefect._StormT {
        public StormP() {
            super("Storm+", false, 2);
        }
    }

    private static abstract class _SubroutineT extends Card {
        public _SubroutineT(String cardName, int energyCost) {
            super(cardName, Card.POWER, energyCost, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnCardPlayedHandler("Subroutine", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.POWER) {
                        state.gainEnergy(1);
                    }
                }
            });
        }
    }

    public static class Subroutine extends _SubroutineT {
        public Subroutine() {
            super("Subroutine", 1);
        }
    }

    public static class SubroutineP extends _SubroutineT {
        public SubroutineP() {
            super("Subroutine+", 0);
        }
    }

    public static class Sunder extends CardDefect.Sunder {
    }

    public static class SunderP extends CardDefect.SunderP {
    }

    private static abstract class _SynchronizeT extends Card {
        public _SynchronizeT(String cardName, boolean exhaust) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.exhaustWhenPlayed = exhaust;
            entityProperty.changePlayerFocus = true;
            entityProperty.changePlayerFocusEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            short[] orbs = state.getOrbs();
            int uniqueCount = 0;
            if (orbs != null) {
                int orbMask = 0;
                for (int i = 0; i < orbs.length; i += 2) {
                    if (orbs[i] != 0) {
                        orbMask |= (1 << orbs[i]);
                    }
                }
                uniqueCount = Integer.bitCount(orbMask);
            }
            int focusGain = 2 * uniqueCount;
            if (focusGain > 0) {
                state.gainFocus(focusGain);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_FOCUS_EOT, focusGain);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Synchronize extends _SynchronizeT {
        public Synchronize() {
            super("Synchronize", true);
        }
    }

    public static class SynchronizeP extends _SynchronizeT {
        public SynchronizeP() {
            super("Synchronize+", false);
        }
    }

    private static abstract class _SynthesisT extends Card {
        private final int damage;

        public _SynthesisT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Synthesis", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("Synthesis", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.POWER
                            && state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx]--;
                        state.gainEnergy(energyUsed);
                    }
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.synthesisCounterIdx = idx;
        }
    }

    public static class Synthesis extends _SynthesisT {
        public Synthesis() {
            super("Synthesis", 12);
        }
    }

    public static class SynthesisP extends _SynthesisT {
        public SynthesisP() {
            super("Synthesis+", 18);
        }
    }

    public static class Tempest extends CardDefect.Tempest {
    }

    public static class TempestP extends CardDefect.TempestP {
    }

    private static abstract class _TeslaCoilT extends Card {
        private final int damage;

        public _TeslaCoilT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            state.triggerLightningOrbsAgainstEnemy(idx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class TeslaCoil extends _TeslaCoilT {
        public TeslaCoil() {
            super("Tesla Coil", 3);
        }
    }

    public static class TeslaCoilP extends _TeslaCoilT {
        public TeslaCoilP() {
            super("Tesla Coil+", 6);
        }
    }

    private static abstract class _ThunderT extends Card {
        private final int damage;

        public _ThunderT(String cardName, int damage) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += damage;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Thunder", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.thunderDamageCounterIdx = idx;
        }
    }

    public static class Thunder extends _ThunderT {
        public Thunder() {
            super("Thunder", 6);
        }
    }

    public static class ThunderP extends _ThunderT {
        public ThunderP() {
            super("Thunder+", 8);
        }
    }

    public static class WhiteNoise extends CardDefect.WhiteNoise {
    }

    public static class WhiteNoiseP extends CardDefect.WhiteNoiseP {
    }

    // **************************************************************************************************
    // *********************************************  Rare  *********************************************
    // **************************************************************************************************

    private static abstract class _AdaptiveStrikeT extends Card {
        private final int damage;

        public _AdaptiveStrikeT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.RARE);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            state.addCardToDiscard(state.createCard(generatedCardIdx));
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(this.getPermCostIfPossible(0));
        }
    }

    public static class AdaptiveStrike extends _AdaptiveStrikeT {
        public AdaptiveStrike() {
            super("Adaptive Strike", 18);
        }
    }

    public static class AdaptiveStrikeP extends _AdaptiveStrikeT {
        public AdaptiveStrikeP() {
            super("Adaptive Strike+", 23);
        }
    }

    public static class AllForOne extends CardDefect.AllForOne {
        public AllForOne(int discardOrderMaxKeepTrackIn10s, int discardOrder0CardMaxCopies) {
            super(discardOrderMaxKeepTrackIn10s, discardOrder0CardMaxCopies);
        }
    }

    public static class AllForOneP extends CardDefect.AllForOneP {
        public AllForOneP(int discardOrderMaxKeepTrackIn10s, int discardOrder0CardMaxCopies) {
            super(discardOrderMaxKeepTrackIn10s, discardOrder0CardMaxCopies);
        }
    }

    public static class Buffer extends CardDefect.Buffer {
    }

    public static class BufferP extends CardDefect.BufferP {
    }

    private static abstract class _ConsumingShadowT extends Card {
        private final int darkOrbs;

        public _ConsumingShadowT(String cardName, int darkOrbs) {
            super(cardName, Card.POWER, 2, Card.RARE);
            this.darkOrbs = darkOrbs;
            entityProperty.orbGenerationPossible |= OrbType.DARK.mask;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < darkOrbs; i++) {
                state.channelOrb(OrbType.DARK);
            }
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("ConsumingShadow", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addEndOfTurnHandler("ConsumingShadow", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int count = state.getCounterForRead()[counterIdx];
                    for (int i = 0; i < count; i++) {
                        state.evokeOrbLeft();
                    }
                }
            });
        }
    }

    public static class ConsumingShadow extends _ConsumingShadowT {
        public ConsumingShadow() {
            super("Consuming Shadow", 2);
        }
    }

    public static class ConsumingShadowP extends _ConsumingShadowT {
        public ConsumingShadowP() {
            super("Consuming Shadow+", 3);
        }
    }

    private static abstract class _CoolantT extends Card {
        private final int blockPerOrb;

        public _CoolantT(String cardName, int blockPerOrb) {
            super(cardName, Card.POWER, 1, Card.RARE);
            this.blockPerOrb = blockPerOrb;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += blockPerOrb;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Coolant", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Coolant", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int n = state.getCounterForRead()[counterIdx];
                    if (n == 0) return;
                    short[] orbs = state.getOrbs();
                    if (orbs == null) return;
                    int orbMask = 0;
                    for (int i = 0; i < orbs.length; i += 2) {
                        if (orbs[i] != 0) {
                            orbMask |= (1 << orbs[i]);
                        }
                    }
                    int uniqueCount = Integer.bitCount(orbMask);
                    if (uniqueCount > 0) {
                        state.getPlayerForWrite().gainBlock(n * uniqueCount);
                    }
                }
            });
        }
    }

    public static class Coolant extends _CoolantT {
        public Coolant() {
            super("Coolant", 2);
        }
    }

    public static class CoolantP extends _CoolantT {
        public CoolantP() {
            super("Coolant+", 3);
        }
    }

    public static class CreativeAI extends CardDefect.CreativeAI {
    }

    public static class CreativeAIP extends CardDefect.CreativeAIP {
    }

    public static class Defragment extends CardDefect.Defragment {
    }

    public static class DefragmentP extends CardDefect.DefragmentP {
    }

    public static class EchoForm extends CardDefect.EchoForm {
    }

    public static class EchoFormP extends CardDefect.EchoFormP {
    }

    private static abstract class _FlakCannonT extends Card {
        private final int damage;

        public _FlakCannonT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.RARE);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int count = 0;
            for (int i = state.handArrLen - 1; i >= 0; i--) {
                if (state.properties.cardDict[state.handArr[i]].cardType == Card.STATUS) {
                    state.exhaustCardFromHandByPosition(i, false);
                    count++;
                }
            }
            state.updateHandArr();
            for (int i = state.discardArrLen - 1; i >= 0; i--) {
                int cardIdx = state.getDiscardArrForRead()[i];
                if (state.properties.cardDict[cardIdx].cardType == Card.STATUS) {
                    state.removeCardFromDiscardByPosition(i);
                    state.exhaustedCardHandle(cardIdx, false);
                    count++;
                }
            }
            int j = 0;
            while (j < state.deckArrLen) {
                int cardIdx = state.getDeckArrForRead()[j];
                if (state.properties.cardDict[cardIdx].cardType == Card.STATUS) {
                    state.removeCardFromDeck(cardIdx, false);
                    state.exhaustedCardHandle(cardIdx, false);
                    count++;
                } else {
                    j++;
                }
            }
            for (int i = 0; i < count; i++) {
                int enemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
                if (enemyIdx >= 0) {
                    state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(enemyIdx), damage, true);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FlakCannon extends _FlakCannonT {
        public FlakCannon() {
            super("Flak Cannon", 8);
        }
    }

    public static class FlakCannonP extends _FlakCannonT {
        public FlakCannonP() {
            super("Flak Cannon+", 11);
        }
    }

    public static class GeneticAlgorithm extends CardDefect.GeneticAlgorithm {
        public GeneticAlgorithm() {
            super(1);
        }
    }

    public static class GeneticAlgorithmP extends CardDefect.GeneticAlgorithmP {
        public GeneticAlgorithmP() {
            super(1);
        }
    }

    private static abstract class _HelixDrillT extends Card {
        private final int damagePerEnergy;

        public _HelixDrillT(String cardName, int damagePerEnergy) {
            super(cardName, Card.ATTACK, 0, Card.RARE);
            this.damagePerEnergy = damagePerEnergy;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int energySpent = state.getCounterForRead()[counterIdx];
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damagePerEnergy * energySpent);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("HelixDrill", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnEnergySpendHandler("HelixDrill", new OnEnergySpendHandler() {
                @Override public void handle(GameState state, int energySpent) {
                    state.getCounterForWrite()[counterIdx] += energySpent;
                }
            });
            state.properties.addStartOfTurnHandler("HelixDrill", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    public static class HelixDrill extends _HelixDrillT {
        public HelixDrill() {
            super("Helix Drill", 3);
        }
    }

    public static class HelixDrillP extends _HelixDrillT {
        public HelixDrillP() {
            super("Helix Drill+", 5);
        }
    }

    public static class HyperBeam extends CardDefect.HyperBeam {
    }

    public static class HyperBeamP extends CardDefect.HyperBeamP {
    }

    private static abstract class _IceLanceT extends Card {
        private final int n;

        public _IceLanceT(String cardName, int n) {
            super(cardName, Card.ATTACK, 3, Card.RARE);
            this.n = n;
            entityProperty.selectEnemy = true;
            entityProperty.orbGenerationPossible |= OrbType.FROST.mask;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.channelOrb(OrbType.FROST);
            state.channelOrb(OrbType.FROST);
            state.channelOrb(OrbType.FROST);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class IceLance extends _IceLanceT {
        public IceLance() {
            super("Ice Lance", 19);
        }
    }

    public static class IceLanceP extends _IceLanceT {
        public IceLanceP() {
            super("Ice Lance+", 24);
        }
    }

    // No need to implement Ignition: Multiplayer

    public static class MachineLearning extends CardDefect.MachineLearning {
    }

    public static class MachineLearningP extends CardDefect.MachineLearningP {
    }

    public static class MeteorStrike extends CardDefect.MeteorStrike {
    }

    public static class MeteorStrikeP extends CardDefect.MeteorStrikeP {
    }

    private static abstract class _ModdedT extends Card {
        private final int energyCostVal;
        private final int drawCount;

        public _ModdedT(String cardName, int energyCostVal, int drawCount) {
            super(cardName, Card.SKILL, energyCostVal, Card.RARE);
            this.energyCostVal = energyCostVal;
            this.drawCount = drawCount;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainOrbSlot(1);
            state.draw(drawCount);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var list = new ArrayList<Card>();
            for (int c = energyCostVal + 1; c <= 5; c++) {
                list.add(drawCount == 1 ? new Modded(c) : new ModdedP(c));
            }
            return list;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            if (state.properties.moddedTransformIndexes == null) {
                state.properties.moddedTransformIndexes = new int[state.properties.cardDict.length];
                Arrays.fill(state.properties.moddedTransformIndexes, -1);
                for (int i = 0; i < state.properties.cardDict.length; i++) {
                    var card = state.properties.cardDict[i].getBaseCard();
                    if (card instanceof _ModdedT m && m.energyCostVal < 5) {
                        Card nextCard = m.drawCount == 1 ? new Modded(m.energyCostVal + 1) : new ModdedP(m.energyCostVal + 1);
                        state.properties.moddedTransformIndexes[i] = state.properties.findCardIndex(state.properties.cardDict[i].wrap(nextCard));
                    }
                }
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop, int cardIdx) {
            return prop.moddedTransformIndexes != null ? prop.moddedTransformIndexes[cardIdx] : -1;
        }
    }

    public static class Modded extends _ModdedT {
        public Modded() {
            this(0);
        }

        public Modded(int cost) {
            super("Modded" + (cost == 0 ? "" : " (" + cost + ")"), cost, 1);
        }
    }

    public static class ModdedP extends _ModdedT {
        public ModdedP() {
            this(0);
        }

        public ModdedP(int cost) {
            super("Modded+" + (cost == 0 ? "" : " (" + cost + ")"), cost, 2);
        }
    }

    public static class MultiCast extends CardDefect.MultiCast {
    }

    public static class MultiCastP extends CardDefect.MultiCastP {
    }

    public static class Rainbow extends CardDefect.Rainbow {
    }

    public static class RainbowP extends CardDefect.RainbowP {
    }

    public static class Reboot extends CardDefect.Reboot {
    }

    public static class RebootP extends CardDefect.RebootP {
    }

    // TODO: Shatter (Rare) - 1 energy, Attack
    //   Effect: Deal 11 damage to ALL enemies. Evoke all of your Orbs.
    //   Upgraded Effect: Deal 15 damage to ALL enemies. Evoke all of your Orbs.

    // TODO: Signal Boost (Rare) - 1 energy, Skill
    //   Effect: The next Power you play is played an additional time. Exhaust.
    //   Upgraded Effect (0 energy): The next Power you play is played an additional time. Exhaust.

    // TODO: Spinner (Rare) - 1 energy, Power
    //   Effect: At the start of your turn, Channel 1 Glass.
    //   Upgraded Effect: Channel 1 Glass. At the start of your turn, Channel 1 Glass.

    // TODO: Supercritical (Rare) - 0 energy, Skill
    //   Effect: Gain 4 energy. Exhaust.
    //   Upgraded Effect: Gain 6energy. Exhaust.

    // TODO: Trash to Treasure (Rare) - 1 energy, Power
    //   Effect: Whenever you create a Status card, Channel 1 random Orb.
    //   Upgraded Effect: Innate. Whenever you create a Status card, Channel 1 random Orb.

    // TODO: Voltaic (Rare) - 2 energy, Skill
    //   Effect: Channel Lightning equal to the Lightning already Channeled this combat. Exhaust.
    //   Upgraded Effect: Channel Lightning equal to the Lightning already Channeled this combat.

    // **************************************************************************************************
    // ********************************************* Event  *********************************************
    // **************************************************************************************************

    public static class HelloWorld extends CardDefect.HelloWorld {
    }

    public static class HelloWorldP extends CardDefect.HelloWorldP {
    }

    public static class Rebound extends CardDefect.Rebound {
    }

    public static class ReboundP extends CardDefect.ReboundP {
    }

    public static class Stack extends CardDefect.Stack {
    }

    public static class StackP extends CardDefect.StackP {
    }

    // **************************************************************************************************
    // ********************************************* Ancient *********************************************
    // **************************************************************************************************

    public static class BiasedCognition extends CardDefect.BiasedCognition {
    }

    public static class BiasedCognitionP extends CardDefect.BiasedCognitionP {
    }

    private static abstract class _QuadcastT extends Card {
        public _QuadcastT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.evokeOrb(4);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Quadcast extends _QuadcastT {
        public Quadcast() {
            super("Quadcast", 1);
        }
    }

    public static class QuadcastP extends _QuadcastT {
        public QuadcastP() {
            super("Quadcast+", 0);
        }
    }
}
