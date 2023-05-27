package com.alphaStS;

import com.alphaStS.Action.GameEnvironmentAction;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyReadOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CardSilent {
    public static class Neutralize extends Card {
        public Neutralize() {
            super("Neutralize", Card.ATTACK, 0, Card.COMMON);
            this.weakEnemy = true;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 3);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, 1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class NeutralizeP extends Card {
        public NeutralizeP() {
            super("Neutralize+", Card.ATTACK, 0, Card.COMMON);
            this.weakEnemy = true;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 4);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _SurvivorT extends Card {
        private final int n;

        public _SurvivorT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.selectFromHand = true;
            this.selectFromHandLater = true;
            this.canDiscardAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.getPlayerForWrite().gainBlock(n);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.discardCardFromHand(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class Survivor extends _SurvivorT {
        public Survivor() {
            super("Survivor", Card.SKILL, 1, 8);
        }
    }

    public static class SurvivorP extends _SurvivorT {
        public SurvivorP() {
            super("Survivor+", Card.SKILL, 1, 11);
        }
    }

    private static abstract class _AcrobaticsT extends Card {
        private final int n;

        public _AcrobaticsT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.selectFromHand = true;
            this.selectFromHandLater = true;
            this.canDiscardAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.draw(n);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.discardCardFromHand(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class Acrobatics extends _AcrobaticsT {
        public Acrobatics() {
            super("Acrobatics", Card.SKILL, 1, 3);
        }
    }

    public static class AcrobaticsP extends _AcrobaticsT {
        public AcrobaticsP() {
            super("Acrobatics+", Card.SKILL, 1, 4);
        }
    }

    private static abstract class _BackflipT extends Card {
        private final int n;

        public _BackflipT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            state.draw(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Backflip extends _BackflipT {
        public Backflip() {
            super("Backflip", Card.SKILL, 1, 5);
        }
    }

    public static class BackflipP extends _BackflipT {
        public BackflipP() {
            super("Backflip+", Card.SKILL, 1, 8);
        }
    }

    // Bane

    private static abstract class _BladeDanceT extends Card {
        private final int n;

        public _BladeDanceT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < n; i++) {
                state.addCardToHand(state.prop.shivCardIdx);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new CardColorless.Shiv());
        }
    }

    public static class BladeDance extends _BladeDanceT {
        public BladeDance() {
            super("Blade Dance", Card.SKILL, 1, 3);
        }
    }

    public static class BladeDanceP extends _BladeDanceT {
        public BladeDanceP() {
            super("Blade Dance+", Card.SKILL, 1, 4);
        }
    }

    private static abstract class _CloakAndDaggerT extends Card {
        private final int n;

        public _CloakAndDaggerT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(6);
            for (int i = 0; i < n; i++) {
                state.addCardToHand(state.prop.shivCardIdx);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new CardColorless.Shiv());
        }
    }

    public static class CloakAndDagger extends _CloakAndDaggerT {
        public CloakAndDagger() {
            super("Cloak And Dagger", Card.SKILL, 1, 1);
        }
    }

    public static class CloakAndDaggerP extends _CloakAndDaggerT {
        public CloakAndDaggerP() {
            super("Cloak And Dagger+", Card.SKILL, 1, 2);
        }
    }

    private static abstract class _DaggerSprayT extends Card {
        private final int n;

        public _DaggerSprayT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < 2; i++) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    state.playerDoDamageToEnemy(enemy, n);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DaggerSpray extends _DaggerSprayT {
        public DaggerSpray() {
            super("Dagger Spray", Card.ATTACK, 1, 4);
        }
    }

    public static class DaggerSprayP extends _DaggerSprayT {
        public DaggerSprayP() {
            super("Dagger Spray+", Card.ATTACK, 1, 6);
        }
    }

    // Dagger Throw

    private static abstract class _DeadlyPoisonT extends Card {
        private final int n;

        public _DeadlyPoisonT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.selectEnemy = true;
            this.poisonEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.POISON, n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DeadlyPoison extends _DeadlyPoisonT {
        public DeadlyPoison() {
            super("Deadly Poison", Card.SKILL, 1, 5);
        }
    }

    public static class DeadlyPoisonP extends _DeadlyPoisonT {
        public DeadlyPoisonP() {
            super("Deadly Poison+", Card.SKILL, 1, 7);
        }
    }

    private static abstract class _DeflectT extends Card {
        private final int n;

        public _DeflectT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Deflect extends _DeflectT {
        public Deflect() {
            super("Deflect", Card.SKILL, 0, 4);
        }
    }

    public static class DeflectP extends _DeflectT {
        public DeflectP() {
            super("Deflect+", Card.SKILL, 0, 7);
        }
    }

    private static abstract class _DodgeAndRollT extends Card {
        private final int n;

        public _DodgeAndRollT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += state.getPlayerForWrite().gainBlock(n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("DodgeAndRoll", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addStartOfTurnHandler("DodgeAndRoll", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getPlayerForWrite().gainBlockNotFromCardPlay(state.getCounterForRead()[counterIdx]);
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    public static class DodgeAndRoll extends _DodgeAndRollT {
        public DodgeAndRoll() {
            super("Dodge And Roll", Card.SKILL, 1, 4);
        }
    }

    public static class DodgeAndRollP extends _DodgeAndRollT {
        public DodgeAndRollP() {
            super("Dodge And Roll+", Card.SKILL, 1, 6);
        }
    }

    private static abstract class _FlyingKneeT extends Card {
        private final int n;

        public _FlyingKneeT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.selectEnemy = true;
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.getCounterForWrite()[counterIdx] += 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("EnergyNextTurn", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 15.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addStartOfTurnHandler("EnergyNextTurn", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.gainEnergy(state.getCounterForRead()[counterIdx]);
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    public static class FlyingKnee extends _FlyingKneeT {
        public FlyingKnee() {
            super("Flying Knee", Card.ATTACK, 1, 8);
        }
    }

    public static class FlyingKneeP extends _FlyingKneeT {
        public FlyingKneeP() {
            super("Flying Knee+", Card.ATTACK, 1, 11);
        }
    }

    private static abstract class _OutmaneuverT extends Card {
        private final int n;

        public _OutmaneuverT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("EnergyNextTurn", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 15.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addStartOfTurnHandler("EnergyNextTurn", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.gainEnergy(state.getCounterForRead()[counterIdx]);
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    public static class Outmaneuver extends _OutmaneuverT {
        public Outmaneuver() {
            super("Outmaneuver", Card.SKILL, 1, 1);
        }
    }

    public static class OutmaneuverP extends _OutmaneuverT {
        public OutmaneuverP() {
            super("Outmaneuver+", Card.SKILL, 1, 2);
        }
    }

    private static abstract class _PiercingWailT extends Card {
        private final int n;

        public _PiercingWailT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.affectEnemyStrength = true;
            this.affectEnemyStrengthEot = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PiercingWail extends _PiercingWailT {
        public PiercingWail() {
            super("Piercing Wail", Card.SKILL, 1, 6);
        }
    }

    public static class PiercingWailP extends _PiercingWailT {
        public PiercingWailP() {
            super("Piercing Wail+", Card.SKILL, 1, 8);
        }
    }

    private static abstract class _PoisonedStabT extends Card {
        private final int n;

        public _PoisonedStabT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.selectEnemy = true;
            this.poisonEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, n);
            enemy.applyDebuff(state, DebuffType.POISON, n / 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PoisonedStab extends _PoisonedStabT {
        public PoisonedStab() {
            super("PoisonedStab", Card.ATTACK, 1, 6);
        }
    }

    public static class PoisonedStabP extends _PoisonedStabT {
        public PoisonedStabP() {
            super("PoisonedStab+", Card.ATTACK, 1, 8);
        }
    }

    private static abstract class _PreparedT extends Card {
        private final int n;

        public _PreparedT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.selectFromHand = true;
            this.selectFromHandLater = true;
            this.canDiscardAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.draw(n);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.discardCardFromHand(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class Prepared extends _PreparedT {
        public Prepared() {
            super("Prepared", Card.SKILL, 0, 1);
        }
    }

    public static class PreparedP extends _PreparedT {
        public PreparedP() {
            super("Prepared+", Card.SKILL, 0, 2);
        }
    }

    private static abstract class _QuickSlashT extends Card {
        private final int n;

        public _QuickSlashT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class QuickSlash extends _QuickSlashT {
        public QuickSlash() {
            super("Quick Slash", Card.ATTACK, 1, 8);
        }
    }

    public static class QuickSlashP extends _QuickSlashT {
        public QuickSlashP() {
            super("Quick Slash+", Card.ATTACK, 1, 12);
        }
    }

    private static abstract class _SliceT extends Card {
        private final int n;

        public _SliceT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Slice extends _SliceT {
        public Slice() {
            super("Slice", Card.ATTACK, 0, 6);
        }
    }

    public static class SliceP extends _SliceT {
        public SliceP() {
            super("Slice+", Card.ATTACK, 0, 9);
        }
    }

    private static abstract class _SneakyStrikeT extends Card {
        private final int n;

        public _SneakyStrikeT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            if (state.getCounterForRead()[counterIdx] > 0) {
                state.gainEnergy(2);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("SneakyStrike", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 2.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister() {
                    state.prop.sneakyStrikeCounterIdx = counterIdx;
                }
            });
            state.prop.addEndOfTurnHandler("SneakyStrike", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[state.prop.sneakyStrikeCounterIdx] = 0;
                }
            });
        }
    }

    public static class SneakyStrike extends _SneakyStrikeT {
        public SneakyStrike() {
            super("Sneaky Strike", Card.ATTACK, 2, 12);
        }
    }

    public static class SneakyStrikeP extends _SneakyStrikeT {
        public SneakyStrikeP() {
            super("Sneaky Strike+", Card.ATTACK, 2, 16);
        }
    }

    private static abstract class _SuckerPunchT extends Card {
        private final int n;
        private final int weak;

        public _SuckerPunchT(String cardName, int cardType, int energyCost, int n, int weak) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.weak = weak;
            this.weakEnemy = true;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, weak);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SuckerPunch extends _SuckerPunchT {
        public SuckerPunch() {
            super("Sucker Punch", Card.ATTACK, 1, 7, 1);
        }
    }

    public static class SuckerPunchP extends _SuckerPunchT {
        public SuckerPunchP() {
            super("Sucker Punch+", Card.ATTACK, 1, 9, 2);
        }
    }

    // Accuracy

    private static abstract class _AllOutAttackT extends Card {
        private final int n;

        public _AllOutAttackT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.canDiscardAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, n);
            }
            var hand = GameStateUtils.getCardArrCounts(state.getHandArrForRead(), state.getNumCardsInHand(), state.prop.cardDict.length);
            int diffCards = 0, c = 0;
            for (int i = 0; i < hand.length; i++) {
                c += hand[i];
                diffCards += hand[i] > 0 ? 1 : 0;
            }
            int r = 1;
            if (diffCards > 1) {
                r = state.getSearchRandomGen().nextInt(c, RandomGenCtx.RandomCardHand, state) + 1;
                state.setIsStochastic();
            }
            for (int i = 0; i < hand.length; i++) {
                r -= hand[i];
                if (r <= 0) {
                    state.discardCardFromHand(i);
                    break;
                }
            }
            // if (state.handArrLen > 0) {
            //     int diff = 0;
            //     var seen = new boolean[state.prop.cardDict.length];
            //     for (int i = 0; i < state.handArrLen; i++) {
            //         if (!seen[state.getHandArrForRead()[i]]) {
            //             diff++;
            //             seen[state.getHandArrForRead()[i]] = true;
            //         }
            //     }
            //     if (diff > 1) {
            //         state.setIsStochastic();
            //         int r = state.getSearchRandomGen().nextInt(state.handArrLen, RandomGenCtx.RandomCardHand, state);
            //         state.discardCardFromHand(state.getHandArrForRead()[r]);
            //     } else {
            //         state.discardCardFromHand(state.getHandArrForRead()[0]);
            //     }
            // }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class AllOutAttack extends _AllOutAttackT {
        public AllOutAttack() {
            super("All-Out Attack", Card.ATTACK, 1, 10);
        }
    }

    public static class AllOutAttackP extends _AllOutAttackT {
        public AllOutAttackP() {
            super("All-Out Attack+", Card.ATTACK, 1, 14);
        }
    }

    private static abstract class _BackstabT extends Card {
        private final int n;

        public _BackstabT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.innate = true;
            this.exhaustWhenPlayed = true;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Backstab extends _BackstabT {
        public Backstab() {
            super("Backstab", Card.ATTACK, 0, 11);
        }
    }

    public static class BackstabP extends _BackstabT {
        public BackstabP() {
            super("Backstab+", Card.ATTACK, 0, 15);
        }
    }

    private static abstract class _BlurT extends Card {
        private final int n;

        public _BlurT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Blur", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 8.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister() {
                    state.prop.blurCounterIdx = counterIdx;
                }
            });
        }
    }

    public static class Blur extends _BlurT {
        public Blur() {
            super("Blur", Card.SKILL, 1, 5);
        }
    }

    public static class BlurP extends _BlurT {
        public BlurP() {
            super("Blur+", Card.SKILL, 1, 8);
        }
    }

    private static abstract class _BouncingFlaskT extends Card {
        private final int n;

        public _BouncingFlaskT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.poisonEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < n; i++) {
                idx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
                state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.POISON, 3);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BouncingFlask extends _BouncingFlaskT {
        public BouncingFlask() {
            super("Bouncing Flask", Card.SKILL, 2, 3);
        }
    }

    public static class BouncingFlaskP extends _BouncingFlaskT {
        public BouncingFlaskP() {
            super("Bouncing Flask+", Card.SKILL, 2, 4);
        }
    }

    private static abstract class _CalculatedGambleT extends Card {
        public _CalculatedGambleT(String cardName, int cardType, int energyCost, boolean exhaustWhenPlayed) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.exhaustWhenPlayed = exhaustWhenPlayed;
            this.canDiscardAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.draw(state.discardHand(true));
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class CalculatedGamble extends _CalculatedGambleT {
        public CalculatedGamble() {
            super("Calculated Gamble", Card.SKILL, 0, true);
        }
    }

    public static class CalculatedGambleP extends _CalculatedGambleT {
        public CalculatedGambleP() {
            super("Calculated Gamble+", Card.SKILL, 0, false);
        }
    }

    // Caltrops

    private static abstract class _CatalystT extends Card {
        private final int n;

        public _CatalystT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.poisonEnemy = true;
            this.selectEnemy = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var p = state.getEnemiesForRead().get(idx).getPoison();
            if (p > 0) {
                state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.POISON, p * (n - 1));
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Catalyst extends _CatalystT {
        public Catalyst() {
            super("Catalyst", Card.SKILL, 1, 2);
        }
    }

    public static class CatalystP extends _CatalystT {
        public CatalystP() {
            super("Catalyst+", Card.SKILL, 1, 3);
        }
    }

    // Choke

    private static abstract class _ConcentrateT extends Card {
        private final int n;

        public _ConcentrateT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.selectFromHand = true;
            this.canDiscardAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.discardCardFromHand(idx);
            state.getCounterForWrite()[counterIdx]++;
            if (state.getCounterForRead()[counterIdx] == n) {
                state.gainEnergy(2);
                state.getCounterForWrite()[counterIdx] = 0;
                return GameActionCtx.PLAY_CARD;
            }
            return GameActionCtx.SELECT_CARD_HAND;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Concentrate", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 6.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }
    }

    public static class Concentrate extends _ConcentrateT {
        public Concentrate() {
            super("Concentrate", Card.SKILL, 0, 3);
        }
    }

    public static class ConcentrateP extends _ConcentrateT {
        public ConcentrateP() {
            super("Concentrate+", Card.SKILL, 0, 2);
        }
    }

    private static abstract class _CripplingCloudT extends Card {
        private final int n;

        public _CripplingCloudT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.poisonEnemy = true;
            this.weakEnemy = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.POISON, n);
                enemy.applyDebuff(state, DebuffType.WEAK, 2);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class CripplingCloud extends _CripplingCloudT {
        public CripplingCloud() {
            super("Crippling Cloud", Card.SKILL, 2, 4);
        }
    }

    public static class CripplingCloudP extends _CripplingCloudT {
        public CripplingCloudP() {
            super("Crippling Cloud+", Card.SKILL, 2, 7);
        }
    }

    private static abstract class _DashT extends Card {
        private final int n;

        public _DashT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.getPlayerForWrite().gainBlock(13);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Dash extends _DashT {
        public Dash() {
            super("Dash", Card.ATTACK, 2, 10);
        }
    }

    public static class DashP extends _DashT {
        public DashP() {
            super("Dash+", Card.ATTACK, 2, 13);
        }
    }

    private static abstract class _DistractionT extends Card {
        public _DistractionT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var r = state.getSearchRandomGen().nextInt(state.prop.distractionIndexes.length, RandomGenCtx.CardGeneration);
            state.addCardToHand(state.prop.distractionIndexes[r]);
            state.setIsStochastic();
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            if (state.prop.distractionIndexes == null) {
                var cards = getPossibleGeneratedCards(null);
                state.prop.distractionIndexes = new int[cards.size()];
                for (int i = 0; i < cards.size(); i++) {
                    state.prop.distractionIndexes[i] = state.prop.findCardIndex(cards.get(i));
                }
            }
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
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
    }

    public static class Distraction extends _DistractionT {
        public Distraction() {
            super("Distraction", Card.SKILL, 1);
        }
    }

    public static class DistractionP extends _DistractionT {
        public DistractionP() {
            super("Distraction+", Card.SKILL, 0);
        }
    }

    private static abstract class _EndlessAgonyT extends Card {
        private final int n;
        private boolean[] isEndlessAgony;

        public _EndlessAgonyT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.selectEnemy = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            isEndlessAgony = new boolean[state.prop.cardDict.length];
            for (int i = 0; i < state.prop.cardDict.length; i++) {
                if (state.prop.cardDict[i].cardName.contains("Endless Agony")) {
                    isEndlessAgony[i] = true;
                }
            }
            state.addOnCardDrawnHandler("EndlessAgony", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned) {
                    state.addGameActionToStartOfDeque(new GameEnvironmentAction() {
                        @Override public void doAction(GameState state) {
                            if (isEndlessAgony[cardIdx]) {
                                state.addCardToHand(cardIdx);
                            }
                        }
                        @Override public boolean canHappenInsideCardPlay() {
                            return true;
                        }
                    });
                }
            });
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(this);
        }
    }

    public static class EndlessAgony extends _EndlessAgonyT {
        public EndlessAgony() {
            super("Endless Agony", Card.ATTACK, 0, 4);
        }
    }

    public static class EndlessAgonyP extends _EndlessAgonyT {
        public EndlessAgonyP() {
            super("Endless Agony+", Card.ATTACK, 0, 6);
        }
    }

    private static abstract class _EscapePlanT extends Card {
        private final int n;

        public _EscapePlanT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var cardIdx = state.draw(1);
            if (cardIdx >= 0 && state.prop.cardDict[cardIdx].cardType == Card.SKILL) {
                state.getPlayerForWrite().gainBlock(n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class EscapePlan extends _EscapePlanT {
        public EscapePlan() {
            super("Escape Plan", Card.SKILL, 0, 3);
        }
    }

    public static class EscapePlanP extends _EscapePlanT {
        public EscapePlanP() {
            super("Escape Plan+", Card.SKILL, 0, 5);
        }
    }

    private static abstract class _EviscerateT extends Card {
        private final int n;

        public _EviscerateT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            // todo: hmm doesn't work well with snecko...
            return Math.max(energyCost - state.getCounterForRead()[state.prop.eviscerateCounterIdx], 0);
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Eviscerate", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister() {
                    state.prop.eviscerateCounterIdx = counterIdx;
                }
            });
            state.prop.addEndOfTurnHandler("Eviscerate", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[state.prop.eviscerateCounterIdx] = 0;
                }
            });
        }
    }

    public static class Eviscerate extends _EviscerateT {
        public Eviscerate() {
            super("Eviscerate", Card.ATTACK, 3, 7);
        }
    }

    public static class EviscerateP extends _EviscerateT {
        public EviscerateP() {
            super("Eviscerate+", Card.ATTACK, 3, 9);
        }
    }

    private static abstract class _ExpertiseT extends Card {
        private final int n;

        public _ExpertiseT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int toDraw = n - state.getNumCardsInHand();
            if (toDraw > 0) {
                state.draw(toDraw);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Expertise extends _ExpertiseT {
        public Expertise() {
            super("Expertise", Card.SKILL, 1, 6);
        }
    }

    public static class ExpertiseP extends _ExpertiseT {
        public ExpertiseP() {
            super("Expertise+", Card.SKILL, 1, 7);
        }
    }

    // Finisher

    private static abstract class _FlechetteT extends Card {
        private final int n;

        public _FlechetteT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < state.handArrLen; i++) {
                if (state.prop.cardDict[state.getHandArrForRead()[i]].cardType == Card.SKILL) {
                    state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Flechette extends _FlechetteT {
        public Flechette() {
            super("Flechette", Card.ATTACK, 1, 4);
        }
    }

    public static class FlechetteP extends _FlechetteT {
        public FlechetteP() {
            super("Flechette+", Card.ATTACK, 1, 6);
        }
    }

    private static abstract class _FootworkT extends Card {
        private final int n;

        public _FootworkT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainDexterity(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Footwork extends _FootworkT {
        public Footwork() {
            super("Footwork", Card.POWER, 1, 2);
        }
    }

    public static class FootworkP extends _FootworkT {
        public FootworkP() {
            super("Footwork+", Card.POWER, 1, 3);
        }
    }

    private static abstract class _HeelHookT extends Card {
        private final int n;

        public _HeelHookT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, n);
            if (enemy.getWeak() > 0) {
                state.gainEnergy(1);
                state.draw(1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class HeelHook extends _HeelHookT {
        public HeelHook() {
            super("HeelHook", Card.ATTACK, 1, 5);
        }
    }

    public static class HeelHookP extends _HeelHookT {
        public HeelHookP() {
            super("HeelHook+", Card.ATTACK, 1, 8);
        }
    }

    private static abstract class _InfiniteBladeT extends Card {
        public _InfiniteBladeT(String cardName, int cardType, int energyCost, boolean innate) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.innate = innate;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new CardColorless.Shiv());
        }

        @Override public void startOfGameSetup(GameState state) {
            state.addStartOfTurnHandler("InfiniteBlade", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    for (int i = 0; i < state.getCounterForRead()[counterIdx]; i++) {
                        state.addCardToHand(state.prop.shivCardIdx);
                    }
                }
            });
        }
    }

    public static class InfiniteBlade extends _InfiniteBladeT {
        public InfiniteBlade() {
            super("InfiniteBlade", Card.POWER, 1, false);
        }
    }

    public static class InfiniteBladeP extends _InfiniteBladeT {
        public InfiniteBladeP() {
            super("InfiniteBlade+", Card.POWER, 1, true);
        }
    }

    private static abstract class _LegSweepT extends Card {
        private final int n;
        private final int weak;

        public _LegSweepT(String cardName, int cardType, int energyCost, int n, int weak) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.weak = weak;
            this.weakEnemy = true;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, weak);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class LegSweep extends _LegSweepT {
        public LegSweep() {
            super("Leg Sweep", Card.SKILL, 2, 11, 2);
        }
    }

    public static class LegSweepP extends _LegSweepT {
        public LegSweepP() {
            super("Leg Sweep+", Card.SKILL, 2, 14, 3);
        }
    }

    public static class MasterfulStab extends Card {
        private final int maxEnergyCost;

        public MasterfulStab(int maxEnergyCost) {
            this(0, maxEnergyCost);
        }

        public MasterfulStab(int energyCost, int maxEnergyCost) {
            super("Masterful Stab (" + energyCost + ")", Card.ATTACK, energyCost, Card.UNCOMMON);
            this.maxEnergyCost = maxEnergyCost;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 12);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = 0; i < maxEnergyCost + 1; i++) {
                c.add(new CardSilent.MasterfulStab(i, maxEnergyCost));
            }
            return c;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.masterfulStabIndexes = new int[maxEnergyCost + 1];
            for (int i = 0; i < state.prop.masterfulStabIndexes.length; i++) {
                state.prop.masterfulStabIndexes[i] = state.prop.findCardIndex(new CardSilent.MasterfulStab(i, maxEnergyCost));
            }
            state.prop.masterfulStabTransformIndexes = new int[state.prop.cardDict.length];
            Arrays.fill(state.prop.masterfulStabTransformIndexes, -1);
            for (int i = 0; i < maxEnergyCost; i++) {
                state.prop.masterfulStabTransformIndexes[state.prop.findCardIndex(new CardSilent.MasterfulStab(i, maxEnergyCost))] = state.prop.findCardIndex(new CardSilent.MasterfulStab(i + 1, maxEnergyCost));
            }
            state.addOnDamageHandler("Masterful Stab", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (damageDealt <= 0) return;
                    for (int i = maxEnergyCost; i > 0; i--) {
                        while (state.getDeckForRead()[state.prop.masterfulStabIndexes[i - 1]] > 0) {
                            state.removeCardFromDeck(state.prop.masterfulStabIndexes[i - 1]);
                            state.addCardToDeck(state.prop.masterfulStabIndexes[i]);
                        }
                        if (state.getExhaustForRead()[state.prop.masterfulStabIndexes[i - 1]] > 0) {
                            state.getExhaustForWrite()[state.prop.masterfulStabIndexes[i]] += state.getExhaustForWrite()[state.prop.masterfulStabIndexes[i - 1]];
                            state.getExhaustForWrite()[state.prop.masterfulStabIndexes[i - 1]] = 0;
                        }
                    }
                    state.handArrTransform(state.prop.masterfulStabTransformIndexes);
                    state.discardArrTransform(state.prop.masterfulStabTransformIndexes);
                }
            });
        }
    }

    public static class MasterfulStabP extends Card {
        private final int maxEnergyCost;

        public MasterfulStabP(int maxEnergyCost) {
            this(0, maxEnergyCost);
        }

        public MasterfulStabP(int energyCost, int maxEnergyCost) {
            super("Masterful Stab+ (" + energyCost + ")", Card.ATTACK, energyCost, Card.UNCOMMON);
            this.maxEnergyCost = maxEnergyCost;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 16);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = 0; i < maxEnergyCost + 1; i++) {
                c.add(new CardSilent.MasterfulStabP(i, maxEnergyCost));
            }
            return c;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.masterfulStabPIndexes = new int[maxEnergyCost + 1];
            for (int i = 0; i < state.prop.masterfulStabPIndexes.length; i++) {
                state.prop.masterfulStabPIndexes[i] = state.prop.findCardIndex(new CardSilent.MasterfulStabP(i, maxEnergyCost));
            }
            state.prop.masterfulStabPTransformIndexes = new int[state.prop.cardDict.length];
            Arrays.fill(state.prop.masterfulStabPTransformIndexes, -1);
            for (int i = 0; i < maxEnergyCost; i++) {
                state.prop.masterfulStabPTransformIndexes[state.prop.findCardIndex(new CardSilent.MasterfulStabP(i, maxEnergyCost))] = state.prop.findCardIndex(new CardSilent.MasterfulStabP(i + 1, maxEnergyCost));
            }
            state.addOnDamageHandler("Masterful Stab", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (damageDealt <= 0) return;
                    for (int i = maxEnergyCost; i > 0; i--) {
                        while (state.getDeckForRead()[state.prop.masterfulStabPIndexes[i - 1]] > 0) {
                            state.removeCardFromDeck(state.prop.masterfulStabPIndexes[i - 1]);
                            state.addCardToDeck(state.prop.masterfulStabPIndexes[i]);
                        }
                        if (state.getExhaustForRead()[state.prop.masterfulStabPIndexes[i - 1]] > 0) {
                            state.getExhaustForWrite()[state.prop.masterfulStabPIndexes[i]] += state.getExhaustForWrite()[state.prop.masterfulStabPIndexes[i - 1]];
                            state.getExhaustForWrite()[state.prop.masterfulStabPIndexes[i - 1]] = 0;
                        }
                    }
                    state.handArrTransform(state.prop.masterfulStabPTransformIndexes);
                    state.discardArrTransform(state.prop.masterfulStabPTransformIndexes);
                }
            });
        }
    }

    // Noxious Fumes

    private static abstract class _PredatorT extends Card {
        private final int n;

        public _PredatorT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.getCounterForWrite()[counterIdx] += 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Predator", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addStartOfTurnHandler("Predator", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.draw(state.getCounterForRead()[counterIdx] * 2);
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    public static class Predator extends _PredatorT {
        public Predator() {
            super("Predator", Card.ATTACK, 2, 15);
        }
    }

    public static class PredatorP extends _PredatorT {
        public PredatorP() {
            super("Predator+", Card.ATTACK, 2, 20);
        }
    }

    private static abstract class _ReflexT extends Card {
        private final int n;

        public _ReflexT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void onDiscard(GameState state) {
            state.addGameActionToEndOfDeque(state1 -> state1.draw(n));
        }
    }

    public static class Reflex extends _ReflexT {
        public Reflex() {
            super("Reflex", Card.SKILL, -1, 2);
        }
    }

    public static class ReflexP extends _ReflexT {
        public ReflexP() {
            super("Reflex+", Card.SKILL, -1, 3);
        }
    }

    private static abstract class _RiddleWithHolesT extends Card {
        private final int n;

        public _RiddleWithHolesT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < 5; i++) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class RiddleWithHoles extends _RiddleWithHolesT {
        public RiddleWithHoles() {
            super("Riddle With Holes", Card.ATTACK, 2, 3);
        }
    }

    public static class RiddleWithHolesP extends _RiddleWithHolesT {
        public RiddleWithHolesP() {
            super("Riddle With Holes+", Card.ATTACK, 2, 4);
        }
    }

    private static abstract class _SetupT extends Card {
        public _SetupT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            // todo: implement this when I have time
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Setup extends _SetupT {
        public Setup() {
            super("Setup", Card.SKILL, 1);
        }
    }

    public static class SetupP extends _SetupT {
        public SetupP() {
            super("Setup+", Card.SKILL, 0);
        }
    }

    private static abstract class _SkewerT extends Card {
        private final int n;

        public _SkewerT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.selectEnemy = true;
            this.isXCost = true;
            this.delayUseEnergy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < energyUsed; i++) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            return state.energy;
        }
    }

    public static class Skewer extends _SkewerT {
        public Skewer() {
            super("Skewer", Card.ATTACK, -1, 7);
        }
    }

    public static class SkewerP extends _SkewerT {
        public SkewerP() {
            super("Skewer+", Card.ATTACK, -1, 10);
        }
    }

    private static abstract class TRacticianT extends Card {
        private final int n;

        public TRacticianT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void onDiscard(GameState state) {
            state.gainEnergy(n);
        }
    }

    public static class Tactician extends TRacticianT {
        public Tactician() {
            super("Tactician", Card.SKILL, -1, 1);
        }
    }

    public static class TacticianP extends TRacticianT {
        public TacticianP() {
            super("Tactician+", Card.SKILL, -1, 2);
        }
    }

    private static abstract class _TerrorT extends Card {
        public _TerrorT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.vulnEnemy = true;
            this.selectEnemy = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.VULNERABLE, 99);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Terror extends _TerrorT {
        public Terror() {
            super("Terror", Card.SKILL, 1);
        }
    }

    public static class TerrorP extends _TerrorT {
        public TerrorP() {
            super("Terror+", Card.SKILL, 0);
        }
    }

    private static abstract class _WellLaidPlansT extends Card {
        private final int n;

        public _WellLaidPlansT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.selectFromHand = true;
            this.selectFromHandLater = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.getCounterForWrite()[counterIdx] = Math.min(GameState.HAND_LIMIT, state.getCounterForRead()[counterIdx] + n);
                return GameActionCtx.PLAY_CARD;
            } else {
                state.removeCardFromHand(idx);
                state.addCardToChosenCards(idx);
                state.getCounterForWrite()[counterIdx] += 1 << 5;
                return GameActionCtx.SELECT_CARD_HAND;
            }
        }

        public void startOfGameSetup(GameState state) {
            state.chosenCardsArr = new short[2];
            state.prop.registerCounter("WellLaidPlans", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = (state.getCounterForRead()[counterIdx] & 31) / 10.0f;
                    input[idx + 1] = (state.getCounterForRead()[counterIdx] >> 5) / 10.0f;
                    return idx + 2;
                }

                @Override public int getInputLenDelta() {
                    return 2;
                }

                @Override public String getDisplayString(GameState state) {
                    return String.valueOf(state.getCounterForRead()[counterIdx] & 31);
                }

                @Override public void onRegister() {
                    state.prop.wellLaidPlansCounterIdx = counterIdx;
                }
            });
            state.prop.addEndOfTurnHandler("WellLaidPlans", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] &= 31;
                }
            });
        }
    }

    public static class WellLaidPlans extends _WellLaidPlansT {
        public WellLaidPlans() {
            super("Well-Laid Plans", Card.POWER, 1, 1);
        }
    }

    public static class WellLaidPlansP extends _WellLaidPlansT {
        public WellLaidPlansP() {
            super("Well-Laid Plans+", Card.POWER, 1, 2);
        }
    }

    private static abstract class _AThousandCutsT extends Card {
        private final int n;

        public _AThousandCutsT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("AThousandCuts", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 8.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler("AThousandCuts", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            state.playerDoNonAttackDamageToEnemy(enemy, state.getCounterForRead()[counterIdx], true);
                        }
                    }
                }
            });
        }
    }

    public static class AThousandCuts extends _AThousandCutsT {
        public AThousandCuts() {
            super("A Thousand Cuts", Card.POWER, 2, 1);
        }
    }

    public static class AThousandCutsP extends _AThousandCutsT {
        public AThousandCutsP() {
            super("A Thousand Cuts+", Card.POWER, 2, 2);
        }
    }

    private static abstract class _AdrenalineT extends Card {
        private final int n;

        public _AdrenalineT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(n);
            state.draw(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Adrenaline extends _AdrenalineT {
        public Adrenaline() {
            super("Adrenaline", Card.SKILL, 0, 1);
        }
    }

    public static class AdrenalineP extends _AdrenalineT {
        public AdrenalineP() {
            super("Adrenaline+", Card.SKILL, 0, 2);
        }
    }

    private static abstract class _AfterImageT extends Card {
        public _AfterImageT(String cardName, int cardType, int energyCost, boolean innate) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.innate = innate;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.addGameActionToEndOfDeque(state1 -> state1.getCounterForWrite()[counterIdx]++);
            return GameActionCtx.PLAY_CARD;
        }

        public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("AfterImage", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = Math.abs(state.getCounterForRead()[counterIdx]) / 4.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler("AfterImage", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getPlayerForWrite().gainBlockNotFromCardPlay(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }
    }

    public static class AfterImage extends _AfterImageT {
        public AfterImage() {
            super("After Image", Card.POWER, 1, false);
        }
    }

    public static class AfterImageP extends _AfterImageT {
        public AfterImageP() {
            super("After Image+", Card.POWER, 1, true);
        }
    }

    // Alchemize

    private static abstract class _BulletTimeT extends Card {
        public _BulletTimeT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.handArrTransform(state.prop.tmp0CostCardTransformIdxes);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.NO_MORE_CARD_DRAW, 1);
            return GameActionCtx.PLAY_CARD;
        }

        @Override List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return cards.stream().filter((x) -> !x.isXCost && x.energyCost > 0 && !(x instanceof Card.CardTmpChangeCost)).map((x) -> (Card) new Card.CardTmpChangeCost(x, 0)).toList();
        }
    }

    public static class BulletTime extends CardSilent._BulletTimeT {
        public BulletTime() {
            super("Bullet Time", Card.SKILL, 3);
        }
    }

    public static class BulletTimeP extends CardSilent._BulletTimeT {
        public BulletTimeP() {
            super("BulletTime+", Card.SKILL, 2);
        }
    }

    private static abstract class _BurstT extends Card {
        private final int n;

        public _BurstT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.getCounterForRead()[counterIdx] == 0) {
                state.getCounterForWrite()[counterIdx] |= 1 << 9;
            }
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Burst", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = Math.abs(state.getCounterForRead()[counterIdx]) / 4.0f;
                    input[idx + 1] = (state.getCounterForRead()[counterIdx] & (1 << 8)) > 0 ? 0.5f : 0;
                    return idx + 2;
                }
                @Override public int getInputLenDelta() {
                    return 2;
                }
            });
            state.addStartOfTurnHandler("Burst", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForWrite()[counterIdx] != 0) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
            state.addOnCardPlayedHandler("Burst", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned) {
                    var card = state.prop.cardDict[cardIdx];
                    if (card.cardType != Card.SKILL || state.getCounterForRead()[counterIdx] == 0) {
                        return;
                    }
                    if ((state.getCounterForRead()[counterIdx] & (1 << 9)) != 0) {
                        state.getCounterForWrite()[counterIdx] ^= 1 << 9;
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

    public static class Burst extends CardSilent._BurstT {
        public Burst() {
            super("Burst", Card.SKILL, 1, 1);
        }
    }

    public static class BurstP extends CardSilent._BurstT {
        public BurstP() {
            super("Burst+", Card.SKILL, 1, 2);
        }
    }

    private static abstract class _CorpseExplosionT extends Card {
        private final int n;

        public _CorpseExplosionT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
            this.selectEnemy = true;
            this.poisonEnemy = true;
            this.corpseExplosionEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.POISON, n);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.CORPSE_EXPLOSION, 1);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.addOnEnemyDeathHandler("CorpseExplosion", new GameEventEnemyHandler() {
                @Override public void handle(GameState state, EnemyReadOnly enemy) {
                    if (enemy.getCorpseExplosion() > 0) {
                        var k = enemy.getCorpseExplosion();
                        state.addGameActionToEndOfDeque(new GameEnvironmentAction() {
                            @Override public void doAction(GameState state) {
                                for (Enemy e : state.getEnemiesForWrite().iterateOverAlive()) {
                                    state.playerDoNonAttackDamageToEnemy(e, enemy.property.origHealth * k, true);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    public static class CorpseExplosion extends CardSilent._CorpseExplosionT {
        public CorpseExplosion() {
            super("Corpse Explosion", Card.SKILL, 2, 6);
        }
    }

    public static class CorpseExplosionP extends CardSilent._CorpseExplosionT {
        public CorpseExplosionP() {
            super("Corpse Explosion+", Card.SKILL, 2, 9);
        }
    }

    private static abstract class _DieDieDieT extends Card {
        private final int n;

        public _DieDieDieT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DieDieDie extends _DieDieDieT {
        public DieDieDie() {
            super("Die Die Die", Card.ATTACK, 1, 13);
        }
    }

    public static class DieDieDieP extends _DieDieDieT {
        public DieDieDieP() {
            super("Die Die Die+", Card.ATTACK, 1, 17);
        }
    }

    private static abstract class _DoppelgangerT extends Card {
        private final boolean upgraded;

        public _DoppelgangerT(String cardName, int cardType, int energyCost, boolean upgraded) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.upgraded = upgraded;
            this.isXCost = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += energyUsed + (upgraded ? 1 : 0);
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            return state.energy;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Doppelganger", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addStartOfTurnHandler("Doppelganger", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.gainEnergy(state.getCounterForRead()[counterIdx]);
                        state.draw(state.getCounterForRead()[counterIdx]);
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    public static class Doppelganger extends _DoppelgangerT {
        public Doppelganger() {
            super("Doppelganger", Card.SKILL, -1, false);
        }
    }

    public static class DoppelgangerP extends _DoppelgangerT {
        public DoppelgangerP() {
            super("Doppelganger+", Card.SKILL, -1, true);
        }
    }

    private static abstract class _EnvenomT extends Card {
        public _EnvenomT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Envenom", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister() {
                    state.prop.envenomCounterIdx = counterIdx;
                }
            });
        }
    }

    public static class Envenom extends _EnvenomT {
        public Envenom() {
            super("Envenom", Card.POWER, 2);
        }
    }

    public static class EnvenomP extends _EnvenomT {
        public EnvenomP() {
            super("Envenom+", Card.POWER, 1);
        }
    }

    public static class GlassKnife extends Card {
        public int limit;
        private int dmg;

        public GlassKnife() {
            this(8);
        }

        public GlassKnife(int dmg, int limit) {
            super("GlassKnife (" + dmg + ")", Card.ATTACK, 1, Card.RARE);
            this.dmg = dmg;
            this.limit = limit;
            selectEnemy = true;
        }

        public GlassKnife(int dmg) {
            this(dmg, 0);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = dmg; i >= limit; i -= 2) {
                c.add(new CardSilent.GlassKnife(i, limit));
            }
            return c;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.glassKnifeIndexes = new int[(dmg - limit) / 2 + 1];
            for (int i = 0; i < state.prop.glassKnifeIndexes.length; i++) {
                state.prop.glassKnifeIndexes[i] = state.prop.findCardIndex(new CardSilent.GlassKnife(i * 2));
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop) {
            int i = (dmg - limit) / 2;
            return i > 0 ? prop.glassKnifeIndexes[i - 1] : -1;
        }
    }

    public static class GlassKnifeP extends Card {
        public int limit;
        private int dmg;

        public GlassKnifeP() {
            this(12);
        }

        public GlassKnifeP(int dmg, int limit) {
            super("GlassKnife+ (" + dmg + ")", Card.ATTACK, 1, Card.RARE);
            this.dmg = dmg;
            this.limit = limit;
            selectEnemy = true;
        }

        public GlassKnifeP(int dmg) {
            this(dmg, 0);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = dmg; i >= limit; i -= 2) {
                c.add(new CardSilent.GlassKnifeP(i, limit));
            }
            return c;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.glassKnifePIndexes = new int[(dmg - limit) / 2 + 1];
            for (int i = 0; i < state.prop.glassKnifePIndexes.length; i++) {
                state.prop.glassKnifePIndexes[i] = state.prop.findCardIndex(new CardSilent.GlassKnifeP(i * 2));
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop) {
            int i = (dmg - limit) / 2;
            return i > 0 ? prop.glassKnifePIndexes[i - 1] : -1;
        }
    }

    private static abstract class _GrandFinaleT extends Card {
        private final int n;

        public _GrandFinaleT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.deckArrLen > 0) {
                return GameActionCtx.PLAY_CARD;
            }
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, n);
            }
            return GameActionCtx.PLAY_CARD;
        }


        public int energyCost(GameState state) {
            return state.deckArrLen > 0 ? -1 : 0;
        }
    }

    public static class GrandFinale extends _GrandFinaleT {
        public GrandFinale() {
            super("Grand Finale", Card.ATTACK, 0, 50);
        }
    }

    public static class GrandFinaleP extends _GrandFinaleT {
        public GrandFinaleP() {
            super("Grand Finale+", Card.ATTACK, 0, 60);
        }
    }

    private static abstract class _MalaiseT extends Card {
        private final boolean upgraded;

        public _MalaiseT(String cardName, int cardType, int energyCost, boolean upgraded) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.upgraded = upgraded;
            this.isXCost = true;
            this.exhaustWhenPlayed = true;
            this.weakEnemy = true;
            this.affectEnemyStrength = true;
            this.selectEnemy = true;
            this.delayUseEnergy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int n = energyUsed + (upgraded ? 1 : 0);
            if (n > 0) {
                state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.LOSE_STRENGTH, energyUsed + (upgraded ? 1 : 0));
                state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, energyUsed + (upgraded ? 1 : 0));
            }
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            return state.energy;
        }
    }

    public static class Malaise extends _MalaiseT {
        public Malaise() {
            super("Malaise", Card.SKILL, -1, false);
        }
    }

    public static class MalaiseP extends _MalaiseT {
        public MalaiseP() {
            super("Malaise+", Card.SKILL, -1, true);
        }
    }

    private static abstract class _NightmareT extends Card {
        public _NightmareT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost, Card.RARE);
            selectFromHand = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.addNightmareCard(idx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.nightmareCards = new short[1];
            state.addStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    for (int i = 0; i < state.nightmareCardsLen; i++) {
                        state.addCardToHand(state.nightmareCards[i]);
                        state.addCardToHand(state.nightmareCards[i]);
                        state.addCardToHand(state.nightmareCards[i]);
                    }
                    state.nightmareCardsLen = 0;
                }
            });
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return new ArrayList<>(cards);
        }
    }

    public static class Nightmare extends _NightmareT {
        public Nightmare() {
            super("Nightmare", Card.SKILL, 3);
        }
    }

    public static class NightmareP extends _NightmareT {
        public NightmareP() {
            super("Nightmare+", Card.SKILL, 2);
        }
    }

    private static abstract class _PhantasmalKillerT extends Card {
        public _PhantasmalKillerT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += 1 << 8;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("PhantasmalKiller", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = (state.getCounterForRead()[counterIdx] >> 8) / 10.0f;
                    input[idx + 1] = (state.getCounterForRead()[counterIdx] & ((1 << 8) - 1)) / 2.0f;
                    return idx + 2;
                }

                @Override public int getInputLenDelta() {
                    return 2;
                }

                @Override public void onRegister() {
                    state.prop.phantasmalKillerCounterIdx = counterIdx;
                }
            });
            state.prop.addEndOfTurnHandler("PhantasmalKiller", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if ((state.getCounterForRead()[counterIdx] & ((1 << 8) - 1)) > 0) {
                        state.getCounterForWrite()[counterIdx]--;
                    }
                }
            });
            state.addStartOfTurnHandler("PhantasmalKiller", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if ((state.getCounterForRead()[counterIdx] >> 8) > 0) {
                        state.getCounterForWrite()[counterIdx] -= 1 << 8;
                        state.getCounterForWrite()[counterIdx]++;
                    }
                }
            });
        }
    }

    public static class PhantasmalKiller extends _PhantasmalKillerT {
        public PhantasmalKiller() {
            super("Phantasmal Killer", Card.SKILL, 1);
        }
    }

    public static class PhantasmalKillerP extends _PhantasmalKillerT {
        public PhantasmalKillerP() {
            super("Phantasmal Killer+", Card.SKILL, 0);
        }
    }

    private static abstract class _StormOfSteelT extends Card {
        private final boolean upgraded;

        public _StormOfSteelT(String cardName, int cardType, int energyCost, boolean upgraded) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.upgraded = upgraded;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int c = state.discardHand(true);
            for (int i = 0; i < c; i++) {
                state.addCardToHand(upgraded ? state.prop.shivPCardIdx : state.prop.shivCardIdx);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new CardColorless.Shiv(), new CardColorless.ShivP());
        }
    }

    public static class StormOfSteel extends _StormOfSteelT {
        public StormOfSteel() {
            super("Storm Of Steel", Card.SKILL, 1, false);
        }
    }

    public static class StormOfSteelP extends _StormOfSteelT {
        public StormOfSteelP() {
            super("Storm Of Steel+", Card.SKILL, 1, true);
        }
    }

    // Tools of the Trade
    // Unload

    private static abstract class _WraithFormT extends Card {
        private final int n;

        public _WraithFormT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[state.prop.intangibleCounterIdx] += n;
            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_DEXTERITY_PER_TURN, 1);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerIntangibleCounter();
            state.prop.registerCounter("WraithFormLoseDexterity", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 4.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.prop.addEndOfTurnHandler("WraithForm", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_DEXTERITY, state.getCounterForRead()[counterIdx]);
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.loseDexterityPerTurnCounterIdx = idx;
        }
    }

    public static class WraithForm extends _WraithFormT {
        public WraithForm() {
            super("WraithForm", Card.POWER, 3, 2);
        }
    }

    public static class WraithFormP extends _WraithFormT {
        public WraithFormP() {
            super("WraithForm+", Card.POWER, 3, 3);
        }
    }
}
