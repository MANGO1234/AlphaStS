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

    // Barrage

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

    // Compile Driver

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

    // Go for the Eyes
    // Hologram
    // Leap
    // Rebound
    // Recursion
    // Stack
    // Steam Barrier
    // Streamline
    // Sweeping Beam

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

    // Aggregate
    // Auto-Shields
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
    // Chaos
    // Chill

    private static abstract class _ConsumeT extends Card {
        private final int n;

        public _ConsumeT(String cardName, int n) {
            super(cardName, Card.SKILL, 2);
            this.n = n;
            this.changePlayerFocus = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainOrbSlot(-1);
            state.gainFocus(2);
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
    // Defragment
    // Doom and Gloom
    // Double Energy
    // Equilibrium
    // FTL
    // Force Field
    // Fusion
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

    // Overclock
    // Recycle
    // Reinforced Body
    // Reprogram
    // Rip and Tear
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

    // Tempest
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
            super("BiasedCognition", 4);
        }
    }

    public static class BiasedCognitionP extends CardDefect._BiasedCognitionT {
        public BiasedCognitionP() {
            super("BiasedCognition+", 5);
        }
    }

    // Buffer
    // Core Surge
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
    // Hyperbeam
    // Machine Learning
    // Meteor Strike
    // Multi-Cast
    // Rainbow
    // Reboot
    // Seek
    // Thunder Strike
}
