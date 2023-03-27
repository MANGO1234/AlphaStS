package com.alphaStS;

import com.alphaStS.enemy.Enemy;
import com.alphaStS.enums.OrbType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CardDefect {
    public static class DualCast extends Card {
        public DualCast() {
            super("Dual Cast", Card.SKILL, 1, Card.COMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.evokeOrb(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DualCastP extends Card {
        public DualCastP() {
            super("Dual Cast+", Card.SKILL, 0, Card.COMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.evokeOrb(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Zap extends Card {
        public Zap() {
            super("Zap", Card.SKILL, 1, Card.COMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.channelOrb(OrbType.LIGHTNING);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ZapP extends Card {
        public ZapP() {
            super("Zap+", Card.SKILL, 0, Card.COMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.channelOrb(OrbType.LIGHTNING);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _BallLightningT extends Card {
        private final int n;

        public _BallLightningT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
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
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.n = n;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var orbs = state.getOrbs();
            if (orbs != null) {
                var enemy = state.getEnemiesForWrite().getForWrite(idx);
                for (int i = 0; i < orbs.length && orbs[i] > 0; i += 2) {
                    state.playerDoDamageToEnemy(enemy, n);
                }
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
            super(cardName, Card.ATTACK, 0, Card.COMMON);
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
            super(cardName, Card.SKILL, 1, Card.COMMON);
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
                @Override public void handle(GameState state) {
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

    public static class Claw extends Card {
        public int limit;
        private int dmg;

        public Claw() {
            this(3);
        }

        public Claw(int dmg, int limit) {
            super("Claw (" + dmg + ")", Card.ATTACK, 0, Card.COMMON);
            this.dmg = dmg;
            this.limit = limit;
            selectEnemy = true;
        }

        public Claw(int dmg) {
            this(dmg, 7);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            var exhaust = state.getExhaustForWrite();
            for (int i = limit - 1; i > 0; i--) {
                for (int j = 0; j < state.getDeckForRead()[state.prop.clawIndexes[i - 1]]; j++) {
                    state.addCardToDeck(state.prop.clawIndexes[i]);
                }
                state.setCardCountInDeck(state.prop.clawIndexes[i - 1], 0);
                state.handArrTransform(state.prop.clawTransformIndexes);
                state.discardArrTransform(state.prop.clawTransformIndexes);
                exhaust[state.prop.clawIndexes[i]] += exhaust[state.prop.clawIndexes[i - 1]];
                exhaust[state.prop.clawIndexes[i - 1]] = 0;
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = 0; i < limit; i++) {
                c.add(new Claw(3 + i * 2, limit));
            }
            return c;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.clawIndexes = new int[limit];
            for (int i = 0; i < state.prop.clawIndexes.length; i++) {
                state.prop.clawIndexes[i] = state.prop.findCardIndex(new Claw(3 + i * 2));
            }
            state.prop.clawTransformIndexes = new int[state.prop.cardDict.length];
            Arrays.fill(state.prop.clawTransformIndexes, -1);
            for (int i = 0; i < limit - 1; i++) {
                state.prop.clawTransformIndexes[state.prop.findCardIndex(new Claw(3 + i * 2))] = state.prop.findCardIndex(new Claw(3 + (i + 1) * 2));
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop) {
            int i = (dmg - 3) / 2;
            return i < limit - 1 ? prop.clawIndexes[i + 1] : -1;
        }
    }

    public static class ClawP extends Card {
        public int limit;
        private int dmg;

        public ClawP() {
            this(5);
        }

        public ClawP(int dmg, int limit) {
            super("Claw+ (" + dmg + ")", Card.ATTACK, 0, Card.COMMON);
            this.dmg = dmg;
            this.limit = limit;
            selectEnemy = true;
        }

        public ClawP(int dmg) {
            this(dmg, 7);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            var exhaust = state.getExhaustForWrite();
            for (int i = limit - 1; i > 0; i--) {
                for (int j = 0; j < state.getDeckForRead()[state.prop.clawPIndexes[i - 1]]; j++) {
                    state.addCardToDeck(state.prop.clawPIndexes[i]);
                }
                state.setCardCountInDeck(state.prop.clawPIndexes[i - 1], 0);
                state.handArrTransform(state.prop.clawPTransformIndexes);
                state.discardArrTransform(state.prop.clawPTransformIndexes);
                exhaust[state.prop.clawPIndexes[i]] += exhaust[state.prop.clawPIndexes[i - 1]];
                exhaust[state.prop.clawPIndexes[i - 1]] = 0;
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = 0; i < limit; i++) {
                c.add(new Claw(5 + i * 2, limit));
            }
            return c;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.clawPIndexes = new int[limit];
            for (int i = 0; i < state.prop.clawPIndexes.length; i++) {
                state.prop.clawPIndexes[i] = state.prop.findCardIndex(new ClawP(5 + i * 2));
            }
            state.prop.clawPTransformIndexes = new int[state.prop.cardDict.length];
            Arrays.fill(state.prop.clawPTransformIndexes, -1);
            for (int i = 0; i < limit - 1; i++) {
                state.prop.clawPTransformIndexes[state.prop.findCardIndex(new ClawP(5 + i * 2))] = state.prop.findCardIndex(new ClawP(5 + (i + 1) * 2));
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop) {
            int i = (dmg - 5) / 2;
            return i < limit - 1 ? prop.clawPIndexes[i + 1] : -1;
        }
    }

    private static class _ColdSnapT extends Card {
        private final int n;

        public _ColdSnapT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
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
            super(cardName, Card.ATTACK, 1, Card.COMMON);
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
            super(cardName, Card.SKILL, 1, Card.COMMON);
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
            super(cardName, Card.ATTACK, 0, Card.COMMON);
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
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.n = n;
            this.exhaustWhenPlayed = exhaustWhenPlayed;
            selectFromDiscard = true;
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
            super(cardName, Card.SKILL, 1, Card.COMMON);
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

    private static abstract class _ReboundT extends Card { // todo: echo form/dup rebound?
        private final int n;

        public _ReboundT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.n = n;
            selectEnemy = true;
            putCardOnTopDeck = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.getCounterForWrite()[counterIdx]++;
            state.getCounterForWrite()[counterIdx] |= 1 << 8;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Rebound", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public void onRegister() {
                    state.prop.reboundCounterIdx = counterIdx;
                }
            });
            state.prop.addEndOfTurnHandler("Rebound", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    public static class Rebound extends CardDefect._ReboundT {
        public Rebound() {
            super("Rebound", 9);
        }
    }

    public static class ReboundP extends CardDefect._ReboundT {
        public ReboundP() {
            super("Rebound+", 12);
        }
    }

    private static class _RecursionT extends Card {
        public _RecursionT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.COMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.triggerRightmostOrbActive();
            state.rotateOrbToBack();
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Recursion extends CardDefect._RecursionT {
        public Recursion() {
            super("Recursion", 1);
        }
    }

    public static class RecursionP extends CardDefect._RecursionT {
        public RecursionP() {
            super("Recursion+", 0);
        }
    }

    private static class _StackT extends Card {
        private int n;

        public _StackT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(state.getNumCardsInDiscard() + n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.cardInDiscardInNNInput = true;
        }
    }

    public static class Stack extends CardDefect._StackT {
        public Stack() {
            super("Stack", 0);
        }
    }

    public static class StackP extends CardDefect._StackT {
        public StackP() {
            super("Stack+", 3);
        }
    }

    public static class SteamBarrier extends Card {
        public int limit;
        private int n;

        public SteamBarrier() {
            this(6);
        }

        public SteamBarrier(int n, int limit) {
            super("Steam Barrier (" + n + ")", Card.SKILL, 0, Card.COMMON);
            this.n = n;
            this.limit = limit;
        }

        public SteamBarrier(int n) {
            this(n, 0);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = n; i >= limit; i--) {
                c.add(new SteamBarrier(i, limit));
            }
            return c;
        }

        @Override public void startOfGameSetup(GameState state) {
            if (state.prop.steamBarrierIndexes != null && state.prop.steamBarrierIndexes.length > n - limit + 1) {
                return;
            }
            state.prop.steamBarrierIndexes = new int[n - limit + 1];
            for (int i = limit; i <= n; i++) {
                state.prop.steamBarrierIndexes[i - limit] = state.prop.findCardIndex(new SteamBarrier(i, limit));
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop) {
            return n > limit ? prop.steamBarrierIndexes[n - limit - 1] : -1;
        }

        public Card getUpgrade() {
            return new SteamBarrierP(n + 2, Math.min(limit + 2, 0));
        }
    }

    public static class SteamBarrierP extends Card {
        public int limit;
        private int n;

        public SteamBarrierP() {
            this(8);
        }

        public SteamBarrierP(int n, int limit) {
            super("Steam Barrier+ (" + n + ")", Card.SKILL, 0, Card.COMMON);
            this.n = n;
            this.limit = limit;
        }

        public SteamBarrierP(int n) {
            this(n, 0);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = n; i >= limit; i--) {
                c.add(new SteamBarrierP(i, limit));
            }
            return c;
        }

        @Override public void startOfGameSetup(GameState state) {
            if (state.prop.steamBarrierPIndexes != null && state.prop.steamBarrierPIndexes.length > n - limit + 1) {
                return;
            }
            state.prop.steamBarrierPIndexes = new int[n - limit + 1];
            for (int i = limit; i <= n; i++) {
                state.prop.steamBarrierPIndexes[i - limit] = state.prop.findCardIndex(new SteamBarrierP(i, limit));
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop) {
            return n > limit ? prop.steamBarrierPIndexes[n - limit - 1] : -1;
        }
    }

    public static class Streamline extends Card {
        private Streamline(int energyCost) {
            super("Streamline (" + energyCost + ")", Card.ATTACK, energyCost, Card.COMMON);
            selectEnemy = true;
        }

        public Streamline() {
            this(2);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 15);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new Streamline(2), new Streamline(1), new Streamline(0));
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.streamlineIndexes = new int[3];
            for (int i = 0; i < 3; i++) {
                state.prop.streamlineIndexes[i] = state.prop.findCardIndex(new Streamline(i));
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop) {
            return energyCost > 0 ? prop.streamlineIndexes[energyCost - 1] : -1;
        }
    }

    public static class StreamlineP extends Card {
        private StreamlineP(int energyCost) {
            super("Streamline+ (" + energyCost + ")", Card.ATTACK, energyCost, Card.COMMON);
            selectEnemy = true;
        }

        public StreamlineP() {
            this(2);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 20);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new StreamlineP(2), new StreamlineP(1), new StreamlineP(0));
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.streamlinePIndexes = new int[3];
            for (int i = 0; i < 3; i++) {
                state.prop.streamlinePIndexes[i] = state.prop.findCardIndex(new StreamlineP(i));
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop) {
            return energyCost > 0 ? prop.streamlinePIndexes[energyCost - 1] : -1;
        }
    }

    private static class _SweepingBeamT extends Card {
        private final int n;

        public _SweepingBeamT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
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
            super(cardName, Card.SKILL, 0, Card.COMMON);
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
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
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
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
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

    private static class _BlizzardT extends Card {
        private final int n;

        public _BlizzardT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int c = state.getCounterForRead()[counterIdx];
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, n * c);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Blizzard", this, new GameProperties.NetworkInputHandler() {
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
            gameProperties.blizzardCounterIdx = idx;
        }
    }

    public static class Blizzard extends CardDefect._BlizzardT {
        public Blizzard() {
            super("Blizzard", 2);
        }
    }

    public static class BlizzardP extends CardDefect._BlizzardT {
        public BlizzardP() {
            super("Blizzard+", 3);
        }
    }

    private static class _BootSequenceT extends Card {
        private final int n;

        public _BootSequenceT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
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
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
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
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.innate = innate;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var count = 0;
            for (var enemy : state.getEnemiesForRead()) {
                if (enemy.isAlive()) {
                    count++;
                }
            }
            for (int i = 0; i < count; i++) {
                state.channelOrb(OrbType.FROST);
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
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
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

    private static abstract class _DarknessT extends Card {
        private final boolean triggerDarkPassive;

        public _DarknessT(String cardName, boolean triggerDarkPassive) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.triggerDarkPassive = triggerDarkPassive;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.channelOrb(OrbType.DARK);
            if (triggerDarkPassive) {
                state.triggerDarkPassive();
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Darkness extends CardDefect._DarknessT {
        public Darkness() {
            super("Darkness", false);
        }
    }

    public static class DarknessP extends CardDefect._DarknessT {
        public DarknessP() {
            super("Darkness+", true);
        }
    }

    private static abstract class _DefragmentT extends Card {
        private final int n;

        public _DefragmentT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.n = n;
            this.changePlayerFocus = true;
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
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
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
            super(cardName, Card.SKILL, n, Card.UNCOMMON);
            this.exhaustWhenPlayed = true;
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

    private static abstract class _EquilibirumT extends Card {
        private final int n;

        public _EquilibirumT(String cardName, int n) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Equilibrium", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.prop.addEndOfTurnHandler("Equilibirum", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx]--;
                    }
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.equilibriumCounterIdx = idx;
        }
    }

    public static class Equilibirum extends CardDefect._EquilibirumT {
        public Equilibirum() {
            super("Equilibirum", 13);
        }
    }

    public static class EquilibirumP extends CardDefect._EquilibirumT {
        public EquilibirumP() {
            super("Equilibirum+", 16);
        }
    }

    // FTL
    // Force Field

    private static abstract class _FusionT extends Card {
        public _FusionT(String cardName, int n) {
            super(cardName, Card.SKILL, n, Card.UNCOMMON);
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

    // todo: echo form
    private static abstract class _GeneticAlgorithmT extends Card {
        protected final int block;
        private final int blockInc;
        protected final double healthRewardRatio;

        public _GeneticAlgorithmT(String cardName, int block, int blockInc, double healthRewardRatio) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
            this.blockInc = blockInc;
            this.healthRewardRatio = healthRewardRatio;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            state.getCounterForWrite()[counterIdx] += blockInc;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("GeneticAlgorithm", this, healthRewardRatio == 0 ? null : new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 16.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            if (healthRewardRatio > 0) {
                state.prop.addExtraTrainingTarget("GeneticAlgorithm", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, double[] v, boolean enemiesAllDead) {
                        if (enemiesAllDead) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getCounterForRead()[counterIdx] / 16.0;
                        } else {
                            int minGA = state.getCounterForRead()[counterIdx];
                            int maxGARemaining = getMaxPossibleGARemaining(state);
                            double vGA = Math.max(minGA / 16.0, Math.min((minGA + maxGARemaining) / 16.0, state.getVOther(vArrayIdx)));
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = vGA;
                        }
                    }

                    @Override public void updateQValues(GameState state, double[] v) {
                        int minGA = state.getCounterForRead()[counterIdx];
                        int maxGARemaining = getMaxPossibleGARemaining(state);
                        double vGA = Math.max(minGA / 16.0, Math.min((minGA + maxGARemaining) / 16.0, v[GameState.V_OTHER_IDX_START + vArrayIdx]));
                        if (true) {
                            v[GameState.V_HEALTH_IDX] += 16 * vGA * healthRewardRatio / state.getPlayeForRead().getMaxHealth();
                        } else {
                            v[GameState.V_HEALTH_IDX] *= (0.8 + vGA / 0.25 * 0.2);
                        }
                    }
                });
            }
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            gameProperties.geneticAlgorithmCounterIdx = idx;
            counterIdx = idx;
        }

        private static int getCardCount(GameState state, int idx) {
            int count = 0;
            count += GameStateUtils.getCardCount(state.getHandArrForRead(), state.getNumCardsInHand(), idx);
            if (idx < state.prop.realCardsLen) {
                count += GameStateUtils.getCardCount(state.getDiscardArrForRead(), state.getNumCardsInDiscard(), idx);
                count += state.getDeckForRead()[idx];
            }
            return count;
        }

        public static int getMaxPossibleGARemaining(GameState state) {
            if (state.isTerminal() != 0) {
                return 0;
            }
            // todo: very very hacky
            var idxes = new int[5];

            boolean canUpgrade = false;
            state.prop.findCardIndex(idxes, "Apotheosis", "Apotheosis (Tmp 0)", "Apotheosis (Perm 0)", "Apotheosis (Perm 2)", "Apotheosis (Perm 3)");
            for (int i = 0; i < idxes.length; i++) {
                if (idxes[i] > 0 && getCardCount(state, idxes[i]) > 0) {
                    canUpgrade = true;
                }
            }
            state.prop.findCardIndex(idxes, "Apotheosis+", "Apotheosis+ (Tmp 0)", "Apotheosis+ (Perm 0)", "Apotheosis+ (Perm 2)", "Apotheosis+ (Perm 3)");
            for (int i = 0; i < idxes.length; i++) {
                if (idxes[i] > 0 && getCardCount(state, idxes[i]) > 0) {
                    canUpgrade = true;
                }
            }
            for (int i = 0; i < state.prop.potions.size(); i++) {
                if (state.prop.potions.get(i) instanceof Potion.BlessingOfTheForge) {
                    if (state.potionUsable(i)) {
                        canUpgrade = true;
                    }
                }
            }

            int maxGA = 0;
            int maxGAP = 0;
            for (int i = 0; i < state.prop.cardDict.length; i++) {
                if (state.prop.cardDict[i].cardName.startsWith("Genetic Algorithm")) {
                    if (state.prop.cardDict[i].cardName.startsWith("Genetic Algorithm+")) {
                        maxGAP += getCardCount(state, i);
                    } else {
                        if (canUpgrade) {
                            maxGAP += getCardCount(state, i);
                        } else {
                            maxGA += getCardCount(state, i);
                        }
                    }
                }
            }
            return maxGAP * 3 + maxGA * 2;
        }
    }

    public static class GeneticAlgorithm extends CardDefect._GeneticAlgorithmT {
        public GeneticAlgorithm(int block) {
            super("Genetic Algorithm (" + block + ")", block, 2, 2);
        }

        public GeneticAlgorithm(int block, double healthRewardRatio) {
            super("Genetic Algorithm (" + block + ")", block, 2, healthRewardRatio);
        }

        public Card getUpgrade() {
            return new CardDefect.GeneticAlgorithmP(block, healthRewardRatio);
        }
    }

    public static class GeneticAlgorithmP extends CardDefect._GeneticAlgorithmT {
        public GeneticAlgorithmP(int block) {
            super("Genetic Algorithm+ (" + block + ")", block, 3, 2);
        }

        public GeneticAlgorithmP(int block, double healthRewardRatio) {
            super("Genetic Algorithm+ (" + block + ")", block, 3, healthRewardRatio);
        }
    }

    private static abstract class _GlacierT extends Card {
        private final int n;

        public _GlacierT(String cardName, int n) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
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

    private static abstract class _LoopT extends Card {
        private final int n;

        public _LoopT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Loop", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public void onRegister() {
                    state.prop.loopCounterIdx = counterIdx;
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.loseFocusPerTurnCounterIdx = idx;
        }
    }

    public static class Loop extends CardDefect._LoopT {
        public Loop() {
            super("Loop", 1);
        }
    }

    public static class LoopP extends CardDefect._LoopT {
        public LoopP() {
            super("Loop+", 2);
        }
    }

    private static abstract class _MelterT extends Card {
        private final int n;

        public _MelterT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
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
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
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
            super(cardName, Card.SKILL, n, Card.UNCOMMON);
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
            super(cardName, Card.SKILL, -1, Card.UNCOMMON);
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
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.n = n;
            changePlayerStrength = true;
            changePlayerDexterity = true;
            changePlayerFocus = true;
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
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
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
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
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
                @Override public void handle(GameState state) {
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
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
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
            super(cardName, Card.ATTACK, 3, Card.UNCOMMON);
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
            super(cardName, Card.SKILL, -1, Card.UNCOMMON);
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

    private static abstract class _AllForOneT extends Card {
        private final int n;
        private final int discardOrderMaxKeepTrackIn10s;
        private final int discardOrder0CardMaxCopies;

        public _AllForOneT(String cardName, int cardType, int energyCost, int n, int discardOrderMaxKeepTrackIn10s, int discardOrder0CardMaxCopies) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
            this.selectEnemy = true;
            this.discardOrderMaxKeepTrackIn10s = discardOrderMaxKeepTrackIn10s;
            this.discardOrder0CardMaxCopies = discardOrder0CardMaxCopies;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            for (int i = 0; i < state.discardArrLen && state.handArrLen < GameState.HAND_LIMIT; i++) {
                var cardIdx = (int) state.getDiscardArrForRead()[i];
                if (state.prop.cardDict[cardIdx].energyCost == 0) {
                    state.removeCardFromDiscardByPosition(i);
                    i--;
                    state.addCardToHand(cardIdx);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.discard0CardOrderMatters = true;
            state.prop.discardOrderMaxKeepTrackIn10s = discardOrderMaxKeepTrackIn10s;
            state.prop.discardOrder0CardMaxCopies = discardOrder0CardMaxCopies;
            state.prop.discardOrder0CardReverseIdx = new int[state.prop.realCardsLen];
            int k = 0;
            for (int i = 0; i < state.prop.realCardsLen; i++) {
                if (state.prop.cardDict[i].realEnergyCost() == 0) {
                    state.prop.discardOrder0CardReverseIdx[i] = k++;
                }
            }
            state.prop.discardOrder0CostNumber = k;
        }
    }

    public static class AllForOne extends CardDefect._AllForOneT {
        public AllForOne(int discardOrderMaxKeepTrackIn10s, int discardOrder0CardMaxCopies) {
            super("All For One", Card.ATTACK, 2, 10, discardOrderMaxKeepTrackIn10s, discardOrder0CardMaxCopies);
        }
    }

    public static class AllForOneP extends CardDefect._AllForOneT {
        public AllForOneP(int discardOrderMaxKeepTrackIn10s, int discardOrder0CardMaxCopies) {
            super("All For One+", Card.ATTACK, 2, 14, discardOrderMaxKeepTrackIn10s, discardOrder0CardMaxCopies);
        }
    }

    private static abstract class _AmplifyT extends Card {
        private final int n;

        public _AmplifyT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Amplify", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = Math.abs(state.getCounterForRead()[counterIdx]) / 4.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addStartOfTurnHandler("Amplify", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForWrite()[counterIdx] != 0) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
            state.addOnCardPlayedHandler("Amplify", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned) {
                    var card = state.prop.cardDict[cardIdx];
                    if (card.cardType != Card.POWER || state.getCounterForRead()[counterIdx] == 0) {
                        return;
                    }
                    if (cloned && (state.getCounterForRead()[counterIdx] & (1 << 8)) > 0) {
                        state.getCounterForWrite()[counterIdx] ^= 1 << 8;
                    } else {
                        var counters = state.getCounterForWrite();
                        counters[counterIdx]--;
                        counters[counterIdx] |= 1 << 8;
                        state.addGameActionToEndOfDeque(curState -> {
                            var action = curState.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                            if (curState.playCard(action, lastIdx, true, true, false, false, energyUsed)) {
                            } else {
                                state.getCounterForWrite()[counterIdx] ^= 1 << 8;
                            }
                        });
                    }
                }
            });
        }
    }

    public static class Amplify extends CardDefect._AmplifyT {
        public Amplify() {
            super("Amplify", Card.SKILL, 1, 1);
        }
    }

    public static class AmplifyP extends CardDefect._AmplifyT {
        public AmplifyP() {
            super("Amplify+", Card.SKILL, 1, 2);
        }
    }

    private static abstract class _BiasedCognitionT extends Card {
        private final int n;

        public _BiasedCognitionT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.RARE);
            this.changePlayerFocus = true;
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
            state.prop.addEndOfTurnHandler("LoseFocusPerTurn", new GameEventHandler() {
                @Override public void handle(GameState state) {
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
            super(cardName, Card.ATTACK, 1, Card.RARE);
            this.n = n;
            selectEnemy = true;
            changePlayerArtifact = true;
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
            super(cardName, Card.POWER, 3, Card.RARE);
            this.ethereal = ethereal;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            // we split counter into three component, 1 for number of duplicated cards per turn and 1 for the number of cards played this turn (up to the max)
            // and negative means the current played card is echoed (todo: register multiple counter so code is clearer and not a mess)
            if (state.getCounterForWrite()[counterIdx] < 0) {
                state.getCounterForWrite()[counterIdx] = -(-state.getCounterForWrite()[counterIdx] + (1 << 16));
            } else {
                state.getCounterForWrite()[counterIdx] += 1 << 16;
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("EchoForm", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    int counter = state.getCounterForRead()[counterIdx];
                    float playingClonedCard = 0.0f;
                    if (counter < 0) {
                        counter *= -1;
                        playingClonedCard = 0.5f;
                    }
                    input[idx] = (counter >> 16) / 8.0f;
                    input[idx + 1] = Math.min((counter >> 16), (counter & ((1 << 16) - 1))) / 8.0f;
                    input[idx + 2] = playingClonedCard;
                    return idx + 3;
                }
                @Override public int getInputLenDelta() {
                    return 3;
                }
                @Override public String getDisplayString(GameState state) {
                    int counter = state.getCounterForRead()[counterIdx];
                    if (counter < 0) {
                        return (-counter & ((1 << 16) - 1)) + "/" + (-counter >> 16) + "/echoingCard";
                    } else {
                        return (counter & ((1 << 16) - 1)) + "/" + (counter >> 16);
                    }
                }
            });
            state.addOnCardPlayedHandler("EchoForm", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned) {
                    if (state.getCounterForRead()[counterIdx] < 0) {
                        state.getCounterForWrite()[counterIdx] = -state.getCounterForWrite()[counterIdx];
                    }
                    if (cloned) { // todo: think only echo formed card doesn't count toward card played
                        return;
                    }
                    boolean isEcho = state.prop.cardDict[cardIdx] instanceof CardDefect.EchoForm;
                    if (isEcho) {
                        state.getCounterForWrite()[counterIdx]++;
                    }
                    int cardsPlayThisTurn = (state.getCounterForRead()[counterIdx] & ((1 << 16) - 1));
                    if (cardsPlayThisTurn < state.getCounterForRead()[counterIdx] >> 16) {
                        state.addGameActionToStartOfDeque(curState -> {
                            var action = curState.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                            curState.getCounterForWrite()[counterIdx] = -curState.getCounterForWrite()[counterIdx];
                            if (curState.playCard(action, lastIdx, false,true, false, false, energyUsed)) {
                                curState.runActionsInQueueIfNonEmpty();
                            } else {
                                curState.getCounterForWrite()[counterIdx] = -curState.getCounterForWrite()[counterIdx];
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
                @Override public void handle(GameState state) {
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
            super(cardName, Card.ATTACK, 2, Card.RARE);
            this.n = n;
            this.changePlayerFocus = true;
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
            super(cardName, Card.ATTACK, 5, Card.RARE);
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
            super(cardName, Card.SKILL, 2, Card.RARE);
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
            super(cardName, Card.SKILL, 0, Card.RARE);
            exhaustWhenPlayed = true;
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.discardHand(false);
            if (!state.prop.isInteractive) {
                state.getDrawOrderForWrite().clear();
            }
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

    public static class Seek extends Card {
        public Seek() {
            super("Seek", Card.SKILL, 0, Card.RARE);
            exhaustWhenPlayed = true;
            selectFromDeck = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.removeCardFromDeck(idx);
            state.addCardToHand(idx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SeekP extends Card {
        public SeekP() {
            super("Seek+", Card.SKILL, 0, Card.RARE);
            exhaustWhenPlayed = true;
            selectFromDeck = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.removeCardFromDeck(idx);
            state.addCardToHand(idx);
            state.getCounterForWrite()[counterIdx]++;
            if (state.getCounterForWrite()[counterIdx] == 2) {
                state.getCounterForWrite()[counterIdx] = 0;
                return GameActionCtx.PLAY_CARD;
            }
            return GameActionCtx.SELECT_CARD_DECK;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Seek+", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 2.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }
    }

    private static class _ThunderStrikeT extends Card {
        private final int n;

        public _ThunderStrikeT(String cardName, int n) {
            super(cardName, Card.ATTACK, 3, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int c = state.getCounterForRead()[counterIdx];
            for (int i = 0; i < c; i++) {
                var j = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemySwordBoomerang);
                var enemy = state.getEnemiesForWrite().getForWrite(j);
                state.playerDoDamageToEnemy(enemy, n);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("ThunderStrike", this, new GameProperties.NetworkInputHandler() {
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
            gameProperties.thunderStrikeCounterIdx = idx;
        }
    }

    public static class ThunderStrike extends CardDefect._ThunderStrikeT {
        public ThunderStrike() {
            super("Thunder Strike", 7);
        }
    }

    public static class ThunderStrikeP extends CardDefect._ThunderStrikeT {
        public ThunderStrikeP() {
            super("Thunder Strike+", 9);
        }
    }
}
