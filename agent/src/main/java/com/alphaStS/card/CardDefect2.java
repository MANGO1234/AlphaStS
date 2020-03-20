package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.enums.OrbType;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventHandler;
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

    // TODO: Feral (Uncommon) - 2 energy, Power
    //   Effect: The first time you play a 0 energy Attack each turn, return it to your Hand.
    //   Upgraded Effect (1 energy): The first time you play a 0 energy Attack each turn, return it to your Hand.

    // TODO: Fight Through (Uncommon) - 1 energy, Skill
    //   Effect: Gain 13 Block. Add 2 Wounds into your Discard Pile.
    //   Upgraded Effect: Gain 17 Block. Add 2 Wounds into your Discard Pile.

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

    // TODO: Glasswork (Uncommon) - 1 energy, Skill
    //   Effect: Gain 5 Block. Channel 1 Glass.
    //   Upgraded Effect: Gain 8 Block. Channel 1 Glass.

    // TODO: Hailstorm (Uncommon) - 1 energy, Power
    //   Effect: At the end of your turn, if you have Frost, deal 6 damage to ALL enemies.
    //   Upgraded Effect: At the end of your turn, if you have Frost, deal 8 damage to ALL enemies.

    // TODO: Iteration (Uncommon) - 1 energy, Power
    //   Effect: The first time you draw a Status card each turn, draw 2 cards.
    //   Upgraded Effect: The first time you draw a Status card each turn, draw 3 cards.

    public static class Loop extends CardDefect.Loop {
    }

    public static class LoopP extends CardDefect.LoopP {
    }

    // TODO: Null (Uncommon) - 2 energy, Attack
    //   Effect: Deal 10 damage. Apply 2 Weak. Channel 1 Dark.
    //   Upgraded Effect: Deal 13 damage. Apply 3 Weak. Channel 1 Dark.

    public static class Overclock extends CardDefect.Overclock {
    }

    public static class OverclockP extends CardDefect.OverclockP {
    }

    // TODO: Refract (Uncommon) - 3 energy, Attack
    //   Effect: Deal 9 damage twice. Channel 2 Glass.
    //   Upgraded Effect: Deal 12 damage twice. Channel 2 Glass.

    public static class RipAndTear extends CardDefect.RipAndTear {
    }

    public static class RipAndTearP extends CardDefect.RipAndTearP {
    }

    // TODO: Rocket Punch (Uncommon) - 2 energy, Attack
    //   Effect: Deal 13 damage. Draw 1 card. When a Status card is created, reduce this card's cost to 0 energy this turn.
    //   Upgraded Effect: Deal 14 damage. Draw 2 cards. When a Status card is created, reduce this card's cost to 0 energy this turn.

    // TODO: Scavenge (Uncommon) - 1 energy, Skill
    //   Effect: Exhaust a card. Next turn, gain 2 energy.
    //   Upgraded Effect: Exhaust a card. Next turn, gain 3 energy.

    public static class Scrape extends CardDefect.Scrape {
    }

    public static class ScrapeP extends CardDefect.ScrapeP {
    }

    // TODO: Shadow Shield (Uncommon) - 2 energy, Skill
    //   Effect: Gain 11 Block. Channel 1 Dark.
    //   Upgraded Effect: Gain 15 Block. Channel 1 Dark.

    public static class Skim extends CardDefect.Skim {
    }

    public static class SkimP extends CardDefect.SkimP {
    }

    // TODO: Smokestack (Uncommon) - 1 energy, Power
    //   Effect: Whenever you create a Status, deal 5 damage to ALL enemies.
    //   Upgraded Effect: Whenever you create a Status, deal 7 damage to ALL enemies.

    public static class Storm extends CardDefect.Storm {
    }

    public static class StormP extends CardDefect._StormT {
        public StormP() {
            super("Storm+", false, 2);
        }
    }

    // TODO: Subroutine (Uncommon) - 1 energy, Power
    //   Effect: Whenever you play a Power, gain energy.
    //   Upgraded Effect (0 energy): Whenever you play a Power, gain energy.

    public static class Sunder extends CardDefect.Sunder {
    }

    public static class SunderP extends CardDefect.SunderP {
    }

    // TODO: Synchronize (Uncommon) - 1 energy, Skill
    //   Effect: Gain 2 Focus this turn for each unique Orb you have. Exhaust.
    //   Upgraded Effect: Gain 2 Focus this turn for each unique Orb you have.

    // TODO: Synthesis (Uncommon) - 2 energy, Attack
    //   Effect: Deal 12 damage. The next Power you play costs 0 energy.
    //   Upgraded Effect: Deal 18 damage. The next Power you play costs 0 energy.

    public static class Tempest extends CardDefect.Tempest {
    }

    public static class TempestP extends CardDefect.TempestP {
    }

    // TODO: Tesla Coil (Uncommon) - 0 energy, Attack
    //   Effect: Deal 3 damage. Trigger all Lightning against the enemy.
    //   Upgraded Effect: Deal 6 damage. Trigger all Lightning against the enemy.

    // TODO: Thunder (Uncommon) - 1 energy, Power
    //   Effect: Whenever you Evoke Lightning, deal 6 damage to each enemy hit.
    //   Upgraded Effect: Whenever you Evoke Lightning, deal 8 damage to each enemy hit.

    public static class WhiteNoise extends CardDefect.WhiteNoise {
    }

    public static class WhiteNoiseP extends CardDefect.WhiteNoiseP {
    }

    // **************************************************************************************************
    // *********************************************  Rare  *********************************************
    // **************************************************************************************************

    // TODO: Adaptive Strike (Rare) - 2 energy, Attack
    //   Effect: Deal 18 damage. Add a 0 energy copy of this card into your Discard Pile.
    //   Upgraded Effect: Deal 23 damage. Add a 0 energy copy of this card into your Discard Pile.

    // TODO: All for One (Rare) - 2 energy, Attack
    //   Effect: Deal 10 damage. Put ALL 0 energy cards from your Discard Pile into your Hand.
    //   Upgraded Effect: Deal 14 damage. Put ALL 0 energy cards from your Discard Pile into your Hand.

    public static class Buffer extends CardDefect.Buffer {
    }

    public static class BufferP extends CardDefect.BufferP {
    }

    // TODO: Consuming Shadow (Rare) - 2 energy, Power
    //   Effect: Channel 2 Dark. At the end of your turn, Evoke your leftmost Orb.
    //   Upgraded Effect: Channel 3 Dark. At the end of your turn, Evoke your leftmost Orb.

    // TODO: Coolant (Rare) - 1 energy, Power
    //   Effect: At the start of your turn, gain 2 Block for each unique Orb you have.
    //   Upgraded Effect: At the start of your turn, gain 3 Block for each unique Orb you have.

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

    // TODO: Flak Cannon (Rare) - 2 energy, Attack
    //   Effect: Exhaust ALL your Status cards. Deal 8 damage to a random enemy for each card Exhausted.
    //   Upgraded Effect: Exhaust ALL your Status cards. Deal 11 damage to a random enemy for each card Exhausted.

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

    // TODO: Helix Drill (Rare) - 0 energy, Attack
    //   Effect: Deal 3 damage for each energy previously spent this turn.
    //   Upgraded Effect: Deal 5 damage for each energy previously spent this turn.

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

    // TODO: Ignition (Rare) - 1 energy, Skill
    //   Effect: Another player Channels Plasma. Exhaust.
    //   Upgraded Effect: Another player Channels Plasma.

    public static class MachineLearning extends CardDefect.MachineLearning {
    }

    public static class MachineLearningP extends CardDefect.MachineLearningP {
    }

    public static class MeteorStrike extends CardDefect.MeteorStrike {
    }

    public static class MeteorStrikeP extends CardDefect.MeteorStrikeP {
    }

    // TODO: Modded (Rare) - 0 energy, Skill
    //   Effect: Gain 1 Orb Slot. Draw 1 card. Increase this card's cost by 1.
    //   Upgraded Effect: Gain 1 Orb Slot. Draw 2 cards. Increase this card's cost by 1.

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
