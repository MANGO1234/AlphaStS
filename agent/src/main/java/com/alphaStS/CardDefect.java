package com.alphaStS;

import com.alphaStS.enemy.Enemy;
import com.alphaStS.enums.OrbType;

import java.util.List;

public class CardDefect {
    public static class DualCast extends Card {
        public DualCast() {
            super("Dual Cast", Card.SKILL, 1);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.evokeOrb(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DualCastP extends Card {
        public DualCastP() {
            super("Dual Cast+", Card.SKILL, 0);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.evokeOrb(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Zap extends Card {
        public Zap() {
            super("Zap", Card.SKILL, 1);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.channelOrb(OrbType.LIGHTNING);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ZapP extends Card {
        public ZapP() {
            super("Zap+", Card.SKILL, 0);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.channelOrb(OrbType.LIGHTNING);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _BallLightningT extends Card {
        private final int n;

        public _BallLightningT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1);
            this.n = n;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.channelOrb(OrbType.LIGHTNING);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BallLightning extends CardDefect._BallLightningT {
        public BallLightning() {
            super("Ball Lightning", 7);
        }
    }

    public static class BallLightningP extends CardDefect._BallLightningT {
        public BallLightningP() {
            super("Ball Lightning+", 10);
        }
    }

    private static abstract class _BarrageT extends Card {
        private final int n;

        public _BarrageT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1);
            this.n = n;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var orbs = state.getOrbs();
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            for (int i = 0; i < orbs.length && orbs[i] > 0; i += 2) {
                state.playerDoDamageToEnemy(enemy, n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Barrage extends CardDefect._BarrageT {
        public Barrage() {
            super("Barrage", 4);
        }
    }

    public static class BarrageP extends CardDefect._BarrageT {
        public BarrageP() {
            super("Barrage+", 6);
        }
    }

    private static abstract class _BeamCellT extends Card {
        private final int n1;
        private final int n2;

        public _BeamCellT(String cardName, int n1, int n2) {
            super(cardName, Card.ATTACK, 0);
            this.n1 = n1;
            this.n2 = n2;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n1);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.VULNERABLE, n2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BeamCell extends CardDefect._BeamCellT {
        public BeamCell() {
            super("Beam Cell", 3, 1);
        }
    }

    public static class BeamCellP extends CardDefect._BeamCellT {
        public BeamCellP() {
            super("Beam Cell+", 4, 2);
        }
    }

    private static class _ChargeBatteryT extends Card {
        private final int n;

        public _ChargeBatteryT(String cardName, int n) {
            super(cardName, Card.SKILL, 1);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("ChargeBattery", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addStartOfTurnHandler("ChargeBattery", new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.gainEnergy(state.getCounterForWrite()[counterIdx]);
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    public static class ChargeBattery extends CardDefect._ChargeBatteryT {
        public ChargeBattery() {
            super("Charge Battery", 7);
        }
    }

    public static class ChargeBatteryP extends CardDefect._ChargeBatteryT {
        public ChargeBatteryP() {
            super("Charge Battery", 10);
        }
    }

    // Claw

    private static class _ColdSnapT extends Card {
        private final int n;

        public _ColdSnapT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1);
            this.n = n;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.channelOrb(OrbType.FROST);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ColdSnap extends CardDefect._ColdSnapT {
        public ColdSnap() {
            super("Cold Snap", 6);
        }
    }

    public static class ColdSnapP extends CardDefect._ColdSnapT {
        public ColdSnapP() {
            super("Cold Snap+", 9);
        }
    }

    private static abstract class _CompiledDriverT extends Card {
        private final int n;

        public _CompiledDriverT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1);
            this.n = n;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            var orbs = state.getOrbs();
            int c = 0;
            for (int i = 0; i < orbs.length && orbs[i] > 0; i += 2) {
                c |= 1 << orbs[i];
            }
            state.draw(Integer.bitCount(c));
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class CompiledDriver extends CardDefect._CompiledDriverT {
        public CompiledDriver() {
            super("Compiled Driver", 7);
        }
    }

    public static class CompiledDriverP extends CardDefect._CompiledDriverT {
        public CompiledDriverP() {
            super("Compiled Driver+", 10);
        }
    }

    private static abstract class _CoolheadedT extends Card {
        private final int n;

        public _CoolheadedT(String cardName, int n) {
            super(cardName, Card.SKILL, 1);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.channelOrb(OrbType.FROST);
            state.draw(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Coolheaded extends CardDefect._CoolheadedT {
        public Coolheaded() {
            super("Coolheaded", 1);
        }
    }

    public static class CoolheadedP extends CardDefect._CoolheadedT {
        public CoolheadedP() {
            super("Coolheaded+", 2);
        }
    }

    private static abstract class _GoForTheEyeT extends Card {
        private final int n1;
        private final int n2;

        public _GoForTheEyeT(String cardName, int n1, int n2) {
            super(cardName, Card.ATTACK, 0);
            this.n1 = n1;
            this.n2 = n2;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.getEnemiesForWrite().get(idx).getMoveString(state).contains("Attack")) {
                state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, n2);
            }
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class GoForTheEye extends CardDefect._GoForTheEyeT {
        public GoForTheEye() {
            super("Go For The Eye", 3, 1);
        }
    }

    public static class GoForTheEyeP extends CardDefect._GoForTheEyeT {
        public GoForTheEyeP() {
            super("Go For The Eye+", 4, 2);
        }
    }

    private static abstract class _HologramT extends Card {
        private final int n;

        public _HologramT(String cardName, int n, boolean exhaustWhenPlayed) {
            super(cardName, Card.SKILL, 1);
            this.n = n;
            this.exhaustWhenPlayed = exhaustWhenPlayed;
            selectFromDiscardLater = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.getPlayerForWrite().gainBlock(n);
                return GameActionCtx.SELECT_CARD_DISCARD;
            } else {
                if (state.getNumCardsInHand() < GameState.HAND_LIMIT) {
                    state.removeCardFromDiscard(idx);
                    state.addCardToHand(idx);
                }
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class Hologram extends CardDefect._HologramT {
        public Hologram() {
            super("Hologram", 3, true);
        }
    }

    public static class HologramP extends CardDefect._HologramT {
        public HologramP() {
            super("Hologram+", 5, false);
        }
    }

    private static class _LeapT extends Card {
        private final int n;

        public _LeapT(String cardName, int n) {
            super(cardName, Card.SKILL, 1);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Leap extends CardDefect._LeapT {
        public Leap() {
            super("Leap", 9);
        }
    }

    public static class LeapP extends CardDefect._LeapT {
        public LeapP() {
            super("Leap+", 12);
        }
    }

    // Rebound
    // Recursion
    // Stack
    // Steam Barrier
    // Streamline

    private static class _SweepingBeamT extends Card {
        private final int n;

        public _SweepingBeamT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, n);
            }
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SweepingBeam extends CardDefect._SweepingBeamT {
        public SweepingBeam() {
            super("Sweeping Beam", 6);
        }
    }

    public static class SweepingBeamP extends CardDefect._SweepingBeamT {
        public SweepingBeamP() {
            super("Sweeping Beam+", 9);
        }
    }

    private static class _TurboT extends Card {
        private final int n;

        public _TurboT(String cardName, int n) {
            super(cardName, Card.SKILL, 0);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(n);
            state.addCardToDiscard(state.prop.voidCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new Card.Void());
        }
    }

    public static class Turbo extends CardDefect._TurboT {
        public Turbo() {
            super("TURBO", 2);
        }
    }

    public static class TurboP extends CardDefect._TurboT {
        public TurboP() {
            super("TURBO+", 3);
        }
    }

    private static abstract class _AggregateT extends Card {
        private final int n;

        public _AggregateT(String cardName, int n) {
            super(cardName, Card.SKILL, 1);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(state.getNumCardsInDeck() / n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Aggregate extends CardDefect._AggregateT {
        public Aggregate() {
            super("Aggregate", 4);
        }
    }

    public static class AggregateP extends CardDefect._AggregateT {
        public AggregateP() {
            super("Aggregate+", 3);
        }
    }

    private static class _AutoShieldsT extends Card {
        private final int n;

        public _AutoShieldsT(String cardName, int n) {
            super(cardName, Card.SKILL, 1);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.getPlayeForRead().getBlock() == 0) {
                state.getPlayerForWrite().gainBlock(n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class AutoShields extends CardDefect._AutoShieldsT {
        public AutoShields() {
            super("Auto Shields", 11);
        }
    }

    public static class AutoShieldsP extends CardDefect._AutoShieldsT {
        public AutoShieldsP() {
            super("Auto Shields+", 15);
        }
    }

    // Blizzard

    private static class _BootSequenceT extends Card {
        private final int n;

        public _BootSequenceT(String cardName, int n) {
            super(cardName, Card.SKILL, 0);
            this.exhaustWhenPlayed = true;
            this.innate = true;
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BootSequence extends CardDefect._BootSequenceT {
        public BootSequence() {
            super("Boot Sequence", 10);
        }
    }

    public static class BootSequenceP extends CardDefect._BootSequenceT {
        public BootSequenceP() {
            super("Boot Sequence+", 13);
        }
    }

    // Bullseye
    // Capacitor

    private static abstract class _ChaosT extends Card {
        private final int n;

        public _ChaosT(String cardName, int n) {
            super(cardName, Card.SKILL, 1);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.channelOrb(OrbType.getRandom(state));
            if (n > 1) {
                state.channelOrb(OrbType.getRandom(state));
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Chaos extends CardDefect._ChaosT {
        public Chaos() {
            super("Chaos", 1);
        }
    }

    public static class ChaosP extends CardDefect._ChaosT {
        public ChaosP() {
            super("Chaos+", 2);
        }
    }

    private static abstract class _ChillT extends Card {
        public _ChillT(String cardName, boolean innate) {
            super(cardName, Card.SKILL, 0);
            this.innate = innate;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (var enemy : state.getEnemiesForRead()) {
                if (enemy.isAlive()) {
                    state.channelOrb(OrbType.FROST);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Chill extends CardDefect._ChillT {
        public Chill() {
            super("Chill", false);
        }
    }

    public static class ChillP extends CardDefect._ChillT {
        public ChillP() {
            super("Chill+", true);
        }
    }

    private static abstract class _ConsumeT extends Card {
        private final int n;

        public _ConsumeT(String cardName, int n) {
            super(cardName, Card.SKILL, 2);
            this.n = n;
            this.changePlayerFocus = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainOrbSlot(-1);
            state.gainFocus(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Consume extends CardDefect._ConsumeT {
        public Consume() {
            super("Consume", 2);
        }
    }

    public static class ConsumeP extends CardDefect._ConsumeT {
        public ConsumeP() {
            super("Consume+", 3);
        }
    }

    // Darkness

    private static abstract class _DefragmentT extends Card {
        private final int n;

        public _DefragmentT(String cardName, int n) {
            super(cardName, Card.POWER, 1);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainFocus(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Defragment extends CardDefect._DefragmentT {
        public Defragment() {
            super("Defragment", 1);
        }
    }

    public static class DefragmentP extends CardDefect._DefragmentT {
        public DefragmentP() {
            super("Defragment+", 2);
        }
    }

    private static abstract class _DoomAndGloomT extends Card {
        private final int n;

        public _DoomAndGloomT(String cardName, int n) {
            super(cardName, Card.ATTACK, 2);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, n);
            }
            state.channelOrb(OrbType.DARK);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DoomAndGloom extends CardDefect._DoomAndGloomT {
        public DoomAndGloom() {
            super("Doom And Gloom", 10);
        }
    }

    public static class DoomAndGloomP extends CardDefect._DoomAndGloomT {
        public DoomAndGloomP() {
            super("Doom And Gloom+", 14);
        }
    }

    private static abstract class _DoubleEnergyT extends Card {
        public _DoubleEnergyT(String cardName, int n) {
            super(cardName, Card.SKILL, n);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(state.energy);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DoubleEnergy extends CardDefect._DoubleEnergyT {
        public DoubleEnergy() {
            super("Double Energy", 1);
        }
    }

    public static class DoubleEnergyP extends CardDefect._DoubleEnergyT {
        public DoubleEnergyP() {
            super("Double Energy+", 0);
        }
    }

    // Equilibrium
    // FTL
    // Force Field

    private static abstract class _FusionT extends Card {
        public _FusionT(String cardName, int n) {
            super(cardName, Card.SKILL, n);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.channelOrb(OrbType.PLASMA);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Fusion extends CardDefect._FusionT {
        public Fusion() {
            super("Fusion", 2);
        }
    }

    public static class FusionP extends CardDefect._FusionT {
        public FusionP() {
            super("Fusion+", 1);
        }
    }

    // Genetic Algorithm

    private static abstract class _GlacierT extends Card {
        private final int n;

        public _GlacierT(String cardName, int n) {
            super(cardName, Card.SKILL, 2);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            state.channelOrb(OrbType.FROST);
            state.channelOrb(OrbType.FROST);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Glacier extends CardDefect._GlacierT {
        public Glacier() {
            super("Glacier", 7);
        }
    }

    public static class GlacierP extends CardDefect._GlacierT {
        public GlacierP() {
            super("Glacier+", 10);
        }
    }

    // Heatsinks
    // Hello World
    // Loop

    private static abstract class _MelterT extends Card {
        private final int n;

        public _MelterT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1);
            this.n = n;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            Enemy enemy = state.getEnemiesForWrite().getForWrite(idx);
            enemy.setBlock(0);
            state.playerDoDamageToEnemy(enemy, n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Melter extends CardDefect._MelterT {
        public Melter() {
            super("Melter", 10);
        }
    }

    public static class MelterP extends CardDefect._MelterT {
        public MelterP() {
            super("Melter+", 14);
        }
    }

    private static abstract class _OverclockT extends Card {
        private final int n;

        public _OverclockT(String cardName, int n) {
            super(cardName, Card.SKILL, 0);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.draw(n);
            state.addCardToDiscard(state.prop.burnCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new Burn());
        }
    }

    public static class Overclock extends CardDefect._OverclockT {
        public Overclock() {
            super("Overclock", 2);
        }
    }

    public static class OverclockP extends CardDefect._OverclockT {
        public OverclockP() {
            super("Overclock+", 3);
        }
    }

    private static abstract class _RecycleT extends Card {
        public _RecycleT(String cardName, int n) {
            super(cardName, Card.SKILL, n);
            selectFromHand = true;
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int cost = Math.max(0, state.prop.cardDict[idx].energyCost(state));
            state.exhaustCardFromHand(idx);
            state.gainEnergy(cost);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Recycle extends CardDefect._RecycleT {
        public Recycle() {
            super("Recycle", 1);
        }
    }

    public static class RecycleP extends CardDefect._RecycleT {
        public RecycleP() {
            super("Recycle+", 0);
        }
    }

    private static abstract class _ReinforcedBodyT extends Card {
        private final int n;

        public _ReinforcedBodyT(String cardName, int n) {
            super(cardName, Card.SKILL, -1);
            this.n = n;
            isXCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var player = state.getPlayerForWrite();
            for (int i = 0; i < energyUsed; i++) {
                player.gainBlock(n);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            return state.energy;
        }
    }

    public static class ReinforcedBody extends CardDefect._ReinforcedBodyT {
        public ReinforcedBody() {
            super("Reinforced Body", 7);
        }
    }

    public static class ReinforcedBodyP extends CardDefect._ReinforcedBodyT {
        public ReinforcedBodyP() {
            super("Reinforced Body+", 9);
        }
    }

    private static abstract class _ReprogramT extends Card {
        private final int n;

        public _ReprogramT(String cardName, int n) {
            super(cardName, Card.SKILL, 1);
            this.n = n;
            changePlayerStrength = true;
            changePlayerDexterity = true;
            // todo: changePlayerFocus
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_FOCUS, n);
            state.getPlayerForWrite().gainStrength(n);
            state.getPlayerForWrite().gainDexterity(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Reprogram extends CardDefect._ReprogramT {
        public Reprogram() {
            super("Reprogram", 1);
        }
    }

    public static class ReprogramP extends CardDefect._ReprogramT {
        public ReprogramP() {
            super("Reprogram+", 2);
        }
    }

    private static abstract class _RipAndTearT extends Card {
        private final int n;

        public _RipAndTearT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            boolean stochastic = state.isStochastic;
            var i = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyRipAndTear);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(i), n);
            if (state.prop.makingRealMove && !stochastic && state.isStochastic) {
                state.getStateDesc().append(cardName).append(" hit ").append(state.getEnemiesForRead().get(i).getName()).append(" (").append(i).append(")");
            }
            i = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyRipAndTear);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(i), n);
            if (state.prop.makingRealMove && !stochastic && state.isStochastic) {
                state.getStateDesc().append(cardName).append(" hit ").append(state.getEnemiesForRead().get(i).getName()).append(" (").append(i).append(")");
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class RipAndTear extends CardDefect._RipAndTearT {
        public RipAndTear() {
            super("Rip And Tear", 7);
        }
    }

    public static class RipAndTearP extends CardDefect._RipAndTearT {
        public RipAndTearP() {
            super("Rip And Tear+", 9);
        }
    }

    // Scrape

    private static abstract class _SelfRepairT extends Card {
        private final int n;

        public _SelfRepairT(String cardName, int n) {
            super(cardName, Card.POWER, 1);
            this.n = n;
            healPlayer = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("SelfRepair", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addEndOfBattleHandler("SelfRepair", new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.getPlayerForWrite().heal(state.getCounterForRead()[counterIdx]);
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.selfRepairCounterIdx = idx;
        }
    }

    public static class SelfRepair extends CardDefect._SelfRepairT {
        public SelfRepair() {
            super("Self Repair", 7);
        }
    }

    public static class SelfRepairP extends CardDefect._SelfRepairT {
        public SelfRepairP() {
            super("Self Repair+", 10);
        }
    }

    private static abstract class _SkimT extends Card {
        private final int n;

        public _SkimT(String cardName, int n) {
            super(cardName, Card.SKILL, 1);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.draw(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Skim extends CardDefect._SkimT {
        public Skim() {
            super("Skim", 3);
        }
    }

    public static class SkimP extends CardDefect._SkimT {
        public SkimP() {
            super("Skim+", 4);
        }
    }

    // Static Discharge
    // Storm

    private static abstract class _SunderT extends Card {
        private final int n;

        public _SunderT(String cardName, int n) {
            super(cardName, Card.ATTACK, 3);
            this.n = n;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            if (state.getEnemiesForRead().get(idx).getHealth() <= 0) {
                state.gainEnergy(3);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Sunder extends CardDefect._SunderT {
        public Sunder() {
            super("Sunder", 24);
        }
    }

    public static class SunderP extends CardDefect._SunderT {
        public SunderP() {
            super("Sunder+", 32);
        }
    }

    private static abstract class _TempestT extends Card {
        private final int n;

        public _TempestT(String cardName, int n) {
            super(cardName, Card.SKILL, -1);
            this.n = n;
            isXCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < energyUsed + n; i++) {
                state.channelOrb(OrbType.LIGHTNING);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            return state.energy;
        }
    }

    public static class Tempest extends CardDefect._TempestT {
        public Tempest() {
            super("Tempest", 0);
        }
    }

    public static class TempestP extends CardDefect._TempestT {
        public TempestP() {
            super("Tempest+", 1);
        }
    }

    // White Noise
    // All for One
    // Amplify

    private static abstract class _BiasedCognitionT extends Card {
        private final int n;

        public _BiasedCognitionT(String cardName, int n) {
            super(cardName, Card.POWER, 1);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainFocus(n);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_FOCUS_PER_TURN, 1);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("BiasedLoseFocus", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 4.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addEndOfTurnHandler("LoseFocusPerTurn", new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_FOCUS, state.getCounterForRead()[counterIdx]);
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.loseFocusPerTurnCounterIdx = idx;
        }
    }

    public static class BiasedCognition extends CardDefect._BiasedCognitionT {
        public BiasedCognition() {
            super("Biased Cognition", 4);
        }
    }

    public static class BiasedCognitionP extends CardDefect._BiasedCognitionT {
        public BiasedCognitionP() {
            super("Biased Cognition+", 5);
        }
    }

    // Buffer

    private static abstract class _CoreSurgeT extends Card {
        private final int n;

        public _CoreSurgeT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1);
            this.n = n;
            selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.getPlayerForWrite().gainArtifact(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class CoreSurge extends CardDefect._CoreSurgeT {
        public CoreSurge() {
            super("Core Surge", 11);
        }
    }

    public static class CoreSurgeP extends CardDefect._CoreSurgeT {
        public CoreSurgeP() {
            super("Core Surge+", 15);
        }
    }

    // Creative AI

    private static abstract class _EchoFormT extends Card {
        public _EchoFormT(String cardName, boolean ethereal) {
            super(cardName, Card.POWER, 3);
            this.ethereal = ethereal;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            // we split counter into two component, 1 for number of duplicated cards per turn and 1 for the number of cards played this turn (up to the max)
            state.getCounterForWrite()[counterIdx] += 1 << 16;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("EchoForm", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    int counter = state.getCounterForRead()[counterIdx];
                    input[idx] = (counter >> 16) / 8.0f;
                    input[idx + 1] = Math.min((counter >> 16), (counter & ((1 << 16) - 1))) / 8.0f;
                    return idx + 2;
                }
                @Override public int getInputLenDelta() {
                    return 2;
                }
                @Override public String getDisplayString(GameState state) {
                    int counter = state.getCounterForRead()[counterIdx];
                    return (counter & ((1 << 16) - 1)) + "/" + (counter >> 16);
                }
            });
            state.addOnCardPlayedHandler("EchoForm", new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card, int lastIdx, boolean cloned) {
                    if (cloned) { // todo: think only echo formed card doesn't count toward card played
                        return;
                    }
                    boolean isEcho = card instanceof CardDefect.EchoForm;
                    if (isEcho) {
                        state.getCounterForWrite()[counterIdx]++;
                    }
                    int cardsPlayThisTurn = (state.getCounterForRead()[counterIdx] & ((1 << 16) - 1));
                    if (cardsPlayThisTurn < state.getCounterForRead()[counterIdx] >> 16) {
                        state.addGameActionToStartOfDeque(curState -> {
                            var cardIdx = curState.prop.findCardIndex(card);
                            var action = curState.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                            if (curState.playCard(action, lastIdx, false,true, false, false)) {
                                curState.runActionsInQueueIfNonEmpty();
                            } else {
                                curState.getCounterForWrite()[counterIdx]--;
                            }
                        });
                        if (!isEcho) {
                            state.getCounterForWrite()[counterIdx]++;
                        }
                    }
                }
            });
            state.addStartOfTurnHandler("EchoForm", new GameEventHandler() {
                @Override void handle(GameState state) {
                    int counter = state.getCounterForRead()[counterIdx];
                    state.getCounterForWrite()[counterIdx] = counter & (((1 << 16) - 1) << 16);
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.echoFormCounterIdx = idx;
        }
    }

    public static class EchoForm extends CardDefect._EchoFormT {
        public EchoForm() {
            super("Echo Form", true);
        }
    }

    public static class EchoFormP extends CardDefect._EchoFormT {
        public EchoFormP() {
            super("Echo Form+", false);
        }
    }

    // Electrodynamics
    // Fission

    private static abstract class _HyperBeamT extends Card {
        private final int n;

        public _HyperBeamT(String cardName, int n) {
            super(cardName, Card.ATTACK, 2);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, n);
            }
            state.gainFocus(-3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class HyperBeam extends CardDefect._HyperBeamT {
        public HyperBeam() {
            super("Hyperbeam", 26);
        }
    }

    public static class HyperBeamP extends CardDefect._HyperBeamT {
        public HyperBeamP() {
            super("Hyperbeam+", 34);
        }
    }

    // Machine Learning

    private static abstract class _MeteorStrikeT extends Card {
        private final int n;

        public _MeteorStrikeT(String cardName, int n) {
            super(cardName, Card.ATTACK, 5);
            this.n = n;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.channelOrb(OrbType.PLASMA);
            state.channelOrb(OrbType.PLASMA);
            state.channelOrb(OrbType.PLASMA);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class MeteorStrike extends CardDefect._MeteorStrikeT {
        public MeteorStrike() {
            super("Meteor Strike", 24);
        }
    }

    public static class MeteorStrikeP extends CardDefect._MeteorStrikeT {
        public MeteorStrikeP() {
            super("Meteor Strike+", 30);
        }
    }

    // Multi-Cast

    private static abstract class _RainbowT extends Card {
        public _RainbowT(String cardName, boolean exhaustWhenPlayed) {
            super(cardName, Card.SKILL, 2);
            this.exhaustWhenPlayed = exhaustWhenPlayed;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.channelOrb(OrbType.LIGHTNING);
            state.channelOrb(OrbType.FROST);
            state.channelOrb(OrbType.DARK);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Rainbow extends CardDefect._RainbowT {
        public Rainbow() {
            super("Rainbow", true);
        }
    }

    public static class RainbowP extends CardDefect._RainbowT {
        public RainbowP() {
            super("Rainbow+", false);
        }
    }

    private static abstract class _RebootT extends Card {
        private final int n;

        public _RebootT(String cardName, int n) {
            super(cardName, Card.SKILL, 0);
            exhaustWhenPlayed = true;
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.discardHand();
            state.reshuffle();
            state.draw(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Reboot extends CardDefect._RebootT {
        public Reboot() {
            super("Reboot", 4);
        }
    }

    public static class RebootP extends CardDefect._RebootT {
        public RebootP() {
            super("Reboot+", 6);
        }
    }

    // Seek
    // Thunder Strike
}
