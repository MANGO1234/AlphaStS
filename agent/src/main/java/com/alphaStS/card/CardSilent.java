package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.action.GameEnvironmentAction;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyReadOnly;
import com.alphaStS.enemy.PredefinedEncounter;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.enums.EnemyDeathCause;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventEnemyHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.eventHandler.OnDamageHandler;
import com.alphaStS.random.RandomGenCtx;
import com.alphaStS.utils.CounterStat;
import com.alphaStS.utils.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.alphaStS.enums.CharacterEnum;

public class CardSilent {
    // Neutralize (Basic) - 0 energy, Attack
    //   Effect: Deal 3 damage. Apply 1 Weak.
    //   Upgraded Effect: Deal 4 damage. Apply 2 Weak.
    public static class Neutralize extends Card {
        public Neutralize() {
            super("Neutralize", Card.ATTACK, 0, Card.COMMON);
            this.entityProperty.weakEnemy = true;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 3, this);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, 1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class NeutralizeP extends Card {
        public NeutralizeP() {
            super("Neutralize+", Card.ATTACK, 0, Card.COMMON);
            this.entityProperty.weakEnemy = true;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 4, this);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _SurvivorT extends Card {
        private final int n;

        public _SurvivorT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.n = n;
            entityProperty.selectFromHand = true;
            this.selectFromHandLater = true;
            this.canDiscardAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.playerGainBlock(n);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.discardCardFromHand(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    // Survivor (Basic) - 1 energy, Skill
    //   Effect: Gain 8 Block. Discard 1 card.
    //   Upgraded Effect: Gain 11 Block. Discard 1 card.
    public static class Survivor extends _SurvivorT {
        public Survivor() {
            super("Survivor", 8);
        }
    }

    public static class SurvivorP extends _SurvivorT {
        public SurvivorP() {
            super("Survivor+", 11);
        }
    }

    private static abstract class _AcrobaticsT extends Card {
        private final int n;

        public _AcrobaticsT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.n = n;
            entityProperty.selectFromHand = true;
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

    // Acrobatics (Common) - 1 energy, Skill
    //   Effect: Draw 3 cards. Discard 1 card.
    //   Upgraded Effect: Draw 4 cards. Discard 1 card.
    public static class Acrobatics extends _AcrobaticsT {
        public Acrobatics() {
            super("Acrobatics", 3);
        }
    }

    public static class AcrobaticsP extends _AcrobaticsT {
        public AcrobaticsP() {
            super("Acrobatics+", 4);
        }
    }

    private static abstract class _BackflipT extends Card {
        private final int n;

        public _BackflipT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(n);
            state.draw(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Backflip (Common) - 1 energy, Skill
    //   Effect: Gain 5 Block. Draw 2 cards.
    //   Upgraded Effect: Gain 8 Block. Draw 2 cards.
    public static class Backflip extends _BackflipT {
        public Backflip() {
            super("Backflip", 5);
        }
    }

    public static class BackflipP extends _BackflipT {
        public BackflipP() {
            super("Backflip+", 8);
        }
    }

    private static abstract class _BaneT extends Card {
        private final int n;

        public _BaneT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            entityProperty.selectEnemy = true;
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            if (state.getEnemiesForRead().get(idx).getPoison() > 0) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Bane (Common) - 1 energy, Attack
    //   Effect: Deal 7 damage. If the enemy has Poison, deal 7 damage again.
    //   Upgraded Effect: Deal 10 damage. If the enemy has Poison, deal 10 damage again.
    public static class Bane extends _BaneT {
        public Bane() {
            super("Bane", 7);
        }
    }

    public static class BaneP extends _BaneT {
        public BaneP() {
            super("Bane+", 10);
        }
    }

    static abstract class _BladeDanceT extends Card {
        private final int n;

        public _BladeDanceT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < n; i++) {
                state.addCardToHand(generatedCardIdx, true);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Shiv());
        }
    }

    // Blade Dance (Common) - 1 energy, Skill
    //   Effect: Add 3 Shivs into your hand.
    //   Upgraded Effect: Add 4 Shivs into your hand.
    public static class BladeDance extends _BladeDanceT {
        public BladeDance() {
            super("Blade Dance", 3);
        }
    }

    public static class BladeDanceP extends _BladeDanceT {
        public BladeDanceP() {
            super("Blade Dance+", 4);
        }
    }

    private static abstract class _CloakAndDaggerT extends Card {
        private final int n;

        public _CloakAndDaggerT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(6);
            for (int i = 0; i < n; i++) {
                state.addCardToHand(generatedCardIdx, true);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Shiv());
        }
    }

    // Cloak and Dagger (Common) - 1 energy, Skill
    //   Effect: Gain 6 Block. Add 1 Shiv into your hand.
    //   Upgraded Effect: Gain 6 Block. Add 2 Shivs into your hand.
    public static class CloakAndDagger extends _CloakAndDaggerT {
        public CloakAndDagger() {
            super("Cloak and Dagger", 1);
        }
    }

    public static class CloakAndDaggerP extends _CloakAndDaggerT {
        public CloakAndDaggerP() {
            super("Cloak and Dagger+", 2);
        }
    }

    private static abstract class _DaggerSprayT extends Card {
        private final int n;

        public _DaggerSprayT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < 2; i++) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    state.playerDoDamageToEnemy(enemy, n, this);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Dagger Spray (Common) - 1 energy, Attack
    //   Effect: Deal 4 damage to ALL enemies twice.
    //   Upgraded Effect: Deal 6 damage to ALL enemies twice.
    public static class DaggerSpray extends _DaggerSprayT {
        public DaggerSpray() {
            super("Dagger Spray", 4);
        }
    }

    public static class DaggerSprayP extends _DaggerSprayT {
        public DaggerSprayP() {
            super("Dagger Spray+", 6);
        }
    }

    private static abstract class _DaggerThrowT extends Card {
        private final int n;

        public _DaggerThrowT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
            entityProperty.selectFromHand = true;
            this.selectFromHandLater = true;
            this.canDiscardAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
                state.draw(1);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.discardCardFromHand(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    // Dagger Throw (Common) - 1 energy, Attack
    //   Effect: Deal 9 damage. Draw 1 card. Discard 1 card.
    //   Upgraded Effect: Deal 12 damage. Draw 1 card. Discard 1 card.
    public static class DaggerThrow extends _DaggerThrowT {
        public DaggerThrow() {
            super("Dagger Throw", 9);
        }
    }

    public static class DaggerThrowP extends _DaggerThrowT {
        public DaggerThrowP() {
            super("Dagger Throw+", 12);
        }
    }

    private static abstract class _DeadlyPoisonT extends Card {
        private final int n;

        public _DeadlyPoisonT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
            this.entityProperty.poisonEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.POISON, n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Deadly Poison (Common) - 1 energy, Skill
    //   Effect: Apply 5 Poison.
    //   Upgraded Effect: Apply 7 Poison.
    public static class DeadlyPoison extends _DeadlyPoisonT {
        public DeadlyPoison() {
            super("Deadly Poison", 5);
        }
    }

    public static class DeadlyPoisonP extends _DeadlyPoisonT {
        public DeadlyPoisonP() {
            super("Deadly Poison+", 7);
        }
    }

    private static abstract class _DeflectT extends Card {
        private final int n;

        public _DeflectT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Deflect (Common) - 0 energy, Skill
    //   Effect: Gain 4 Block.
    //   Upgraded Effect: Gain 7 Block.
    public static class Deflect extends _DeflectT {
        public Deflect() {
            super("Deflect", 4);
        }
    }

    public static class DeflectP extends _DeflectT {
        public DeflectP() {
            super("Deflect+", 7);
        }
    }

    private static abstract class _DodgeAndRollT extends Card {
        private final int n;

        public _DodgeAndRollT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(n);
            state.playerGainBlockNextTurn(n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerBlockNextTurnCounter(this);
        }
    }

    // Dodge and Roll (Common) - 1 energy, Skill
    //   Effect: Gain 4 Block. Next turn, gain 4 Block.
    //   Upgraded Effect: Gain 6 Block. Next turn, gain 6 Block.
    public static class DodgeAndRoll extends _DodgeAndRollT {
        public DodgeAndRoll() {
            super("Dodge and Roll", 4);
        }
    }

    public static class DodgeAndRollP extends _DodgeAndRollT {
        public DodgeAndRollP() {
            super("Dodge and Roll+", 6);
        }
    }

    private static abstract class _FlyingKneeT extends Card {
        private final int n;

        public _FlyingKneeT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            entityProperty.selectEnemy = true;
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            state.getCounterForWrite()[counterIdx] += 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("EnergyNextTurn", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 15.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("EnergyNextTurn", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.gainEnergy(state.getCounterForRead()[counterIdx]);
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    // Flying Knee (Common) - 1 energy, Attack
    //   Effect: Deal 8 damage. Next turn, gain.
    //   Upgraded Effect: Deal 11 damage. Next turn, gain.
    public static class FlyingKnee extends _FlyingKneeT {
        public FlyingKnee() {
            super("Flying Knee", 8);
        }
    }

    public static class FlyingKneeP extends _FlyingKneeT {
        public FlyingKneeP() {
            super("Flying Knee+", 11);
        }
    }

    private static abstract class _OutmaneuverT extends Card {
        private final int n;

        public _OutmaneuverT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerEnergyNextTurnCounter(state, this);
        }
    }

    // Outmaneuver (Common) - 1 energy, Skill
    //   Effect: Next turn, gain.
    public static class Outmaneuver extends _OutmaneuverT {
        public Outmaneuver() {
            super("Outmaneuver", 2);
        }
    }

    public static class OutmaneuverP extends _OutmaneuverT {
        public OutmaneuverP() {
            super("Outmaneuver+", 3);
        }
    }

    private static abstract class _PiercingWailT extends Card {
        private final int n;

        public _PiercingWailT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.n = n;
            this.entityProperty.affectEnemyStrength = true;
            this.entityProperty.affectEnemyStrengthEot = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Piercing Wail (Common) - 1 energy, Skill
    //   Effect: ALL enemies lose 6 Strength this turn. Exhaust.
    //   Upgraded Effect: ALL enemies lose 8 Strength this turn. Exhaust.
    public static class PiercingWail extends _PiercingWailT {
        public PiercingWail() {
            super("Piercing Wail", 6);
        }
    }

    public static class PiercingWailP extends _PiercingWailT {
        public PiercingWailP() {
            super("Piercing Wail+", 8);
        }
    }

    private static abstract class _PoisonedStabT extends Card {
        private final int n;

        public _PoisonedStabT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
            this.entityProperty.poisonEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, n, this);
            enemy.applyDebuff(state, DebuffType.POISON, n / 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Poisoned Stab (Common) - 1 energy, Attack
    //   Effect: Deal 6 damage. Apply 3 Poison.
    //   Upgraded Effect: Deal 8 damage. Apply 4 Poison.
    public static class PoisonedStab extends _PoisonedStabT {
        public PoisonedStab() {
            super("Poisoned Stab", 6);
        }
    }

    public static class PoisonedStabP extends _PoisonedStabT {
        public PoisonedStabP() {
            super("Poisoned Stab+", 8);
        }
    }

    private static abstract class _PreparedT extends Card {
        private final int n;

        public _PreparedT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.n = n;
            entityProperty.selectFromHand = true;
            this.selectFromHandLater = true;
            this.canDiscardAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.draw(n);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                if (n >= 2) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx] = 0;
                        state.discardCardFromHand(idx);
                        return GameActionCtx.PLAY_CARD;
                    } else {
                        state.getCounterForWrite()[counterIdx] = 1;
                        state.discardCardFromHand(idx);
                        return GameActionCtx.SELECT_CARD_HAND;
                    }
                } else {
                    state.discardCardFromHand(idx);
                    return GameActionCtx.PLAY_CARD;
                }
            }
        }

        public void gamePropertiesSetup(GameState state) {
            if (n >= 2) {
                state.properties.registerCounter("Prepared", this, new GameProperties.NetworkInputHandler() {
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
    }

    // Prepared (Common) - 0 energy, Skill
    //   Effect: Draw 1 card. Discard 1 card.
    //   Upgraded Effect: Draw 2 cards. Discard 2 cards.
    public static class Prepared extends _PreparedT {
        public Prepared() {
            super("Prepared", 1);
        }
    }

    public static class PreparedP extends _PreparedT {
        public PreparedP() {
            super("Prepared+", 2);
        }
    }

    private static abstract class _QuickSlashT extends Card {
        private final int n;

        public _QuickSlashT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Quick Slash (Common) - 1 energy, Attack
    //   Effect: Deal 8 damage. Draw 1 card.
    //   Upgraded Effect: Deal 12 damage. Draw 1 card.
    public static class QuickSlash extends _QuickSlashT {
        public QuickSlash() {
            super("Quick Slash", 8);
        }
    }

    public static class QuickSlashP extends _QuickSlashT {
        public QuickSlashP() {
            super("Quick Slash+", 12);
        }
    }

    private static abstract class _SliceT extends Card {
        private final int n;

        public _SliceT(String cardName, int n) {
            super(cardName, Card.ATTACK, 0, Card.COMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Slice (Common) - 0 energy, Attack
    //   Effect: Deal 6 damage.
    //   Upgraded Effect: Deal 9 damage.
    public static class Slice extends _SliceT {
        public Slice() {
            super("Slice", 6);
        }
    }

    public static class SliceP extends _SliceT {
        public SliceP() {
            super("Slice+", 9);
        }
    }

    private static abstract class _SneakyStrikeT extends Card {
        private final int n;

        public _SneakyStrikeT(String cardName, int n) {
            super(cardName, Card.ATTACK, 2, Card.COMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            if (state.getCounterForRead()[counterIdx] > 0) {
                state.gainEnergy(2);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("SneakyStrike", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 2.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int counterIdx) {
                    state.properties.sneakyStrikeCounterIdx = counterIdx;
                }
            });
            state.properties.addEndOfTurnHandler("SneakyStrike", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[state.properties.sneakyStrikeCounterIdx] = 0;
                }
            });
        }
    }

    // Sneaky Strike (Common) - 2 energy, Attack
    //   Effect: Deal 12 damage. If you have discarded a card this turn, gain.
    //   Upgraded Effect: Deal 16 damage. If you have discarded a card this turn, gain.
    public static class SneakyStrike extends _SneakyStrikeT {
        public SneakyStrike() {
            super("Sneaky Strike", 12);
        }
    }

    public static class SneakyStrikeP extends _SneakyStrikeT {
        public SneakyStrikeP() {
            super("Sneaky Strike+", 16);
        }
    }

    static abstract class _SuckerPunchT extends Card {
        private final int n;
        private final int weak;

        public _SuckerPunchT(String cardName, int n, int weak) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.n = n;
            this.weak = weak;
            this.entityProperty.weakEnemy = true;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, weak);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Sucker Punch (Common) - 1 energy, Attack
    //   Effect: Deal 7 damage. Apply 1 Weak.
    //   Upgraded Effect: Deal 9 damage. Apply 2 Weak.
    public static class SuckerPunch extends _SuckerPunchT {
        public SuckerPunch() {
            super("Sucker Punch", 7, 1);
        }
    }

    public static class SuckerPunchP extends _SuckerPunchT {
        public SuckerPunchP() {
            super("Sucker Punch+", 9, 2);
        }
    }

    private static abstract class _AccuracyT extends Card {
        private final int n;

        public _AccuracyT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Accuracy", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = Math.abs(state.getCounterForRead()[counterIdx]) / 20.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int counterIdx) {
                    state.properties.accuracyCounterIdx = counterIdx;
                }
            });
        }
    }

    // Accuracy (Uncommon) - 1 energy, Power
    //   Effect: Shivs deal 4 additional damage.
    //   Upgraded Effect: Shivs deal 6 additional damage.
    public static class Accuracy extends _AccuracyT {
        public Accuracy() {
            super("Accuracy", 4);
        }
    }

    public static class AccuracyP extends _AccuracyT {
        public AccuracyP() {
            super("Accuracy+", 6);
        }
    }

    private static abstract class _AllOutAttackT extends Card {
        private final int n;

        public _AllOutAttackT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.n = n;
            this.canDiscardAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, n, this);
            }
            var hand = GameStateUtils.getCardArrCounts(state.getHandArrForRead(), state.getNumCardsInHand(), state.properties.cardDict.length);
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
            return GameActionCtx.PLAY_CARD;
        }
    }

    // All-Out Attack (Uncommon) - 1 energy, Attack
    //   Effect: Deal 10 damage to ALL enemies. Discard 1 card at random.
    //   Upgraded Effect: Deal 14 damage to ALL enemies. Discard 1 card at random.
    public static class AllOutAttack extends _AllOutAttackT {
        public AllOutAttack() {
            super("All-Out Attack", 10);
        }
    }

    public static class AllOutAttackP extends _AllOutAttackT {
        public AllOutAttackP() {
            super("All-Out Attack+", 14);
        }
    }

    private static abstract class _BackstabT extends Card {
        private final int n;

        public _BackstabT(String cardName, int n) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.n = n;
            this.innate = true;
            this.exhaustWhenPlayed = true;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Backstab (Uncommon) - 0 energy, Attack
    //   Effect: Innate. Deal 11 damage. Exhaust.
    //   Upgraded Effect: Innate. Deal 15 damage. Exhaust.
    public static class Backstab extends _BackstabT {
        public Backstab() {
            super("Backstab", 11);
        }
    }

    public static class BackstabP extends _BackstabT {
        public BackstabP() {
            super("Backstab+", 15);
        }
    }

    private static abstract class _BlurT extends Card {
        private final int n;

        public _BlurT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(n);
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Blur", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 8.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int counterIdx) {
                    state.properties.blurCounterIdx = counterIdx;
                }
            });
        }
    }

    // Blur (Uncommon) - 1 energy, Skill
    //   Effect: Gain 5 Block. Block is not removed at the start of your next turn.
    //   Upgraded Effect: Gain 8 Block. Block is not removed at the start of your next turn.
    public static class Blur extends _BlurT {
        public Blur() {
            super("Blur", 5);
        }
    }

    public static class BlurP extends _BlurT {
        public BlurP() {
            super("Blur+", 8);
        }
    }

    private static abstract class _BouncingFlaskT extends Card {
        private final int n;

        public _BouncingFlaskT(String cardName, int n) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
            this.n = n;
            this.entityProperty.poisonEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < n; i++) {
                idx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
                if (idx >= 0) {
                    state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.POISON, 3);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Bouncing Flask (Uncommon) - 2 energy, Skill
    //   Effect: Apply 3 Poison to a random enemy 3 times.
    //   Upgraded Effect: Apply 3 Poison to a random enemy 4 times.
    public static class BouncingFlask extends _BouncingFlaskT {
        public BouncingFlask() {
            super("Bouncing Flask", 3);
        }
    }

    public static class BouncingFlaskP extends _BouncingFlaskT {
        public BouncingFlaskP() {
            super("Bouncing Flask+", 4);
        }
    }

    static abstract class _CalculatedGambleT extends Card {
        public _CalculatedGambleT(String cardName, boolean exhaustWhenPlayed) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.exhaustWhenPlayed = exhaustWhenPlayed;
            this.canDiscardAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.draw(state.discardHand(true));
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Calculated Gamble (Uncommon) - 0 energy, Skill
    //   Effect: Discard your hand, then draw that many cards. Exhaust.
    //   Upgraded Effect: Discard your hand, then draw that many cards.
    public static class CalculatedGamble extends _CalculatedGambleT {
        public CalculatedGamble() {
            super("Calculated Gamble", true);
        }
    }

    public static class CalculatedGambleP extends _CalculatedGambleT {
        public CalculatedGambleP() {
            super("Calculated Gamble+", false);
        }
    }

    private static abstract class _CaltropsT extends Card {
        private final int n;

        public _CaltropsT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerThornCounter(state, this);
        }
    }

    // Caltrops (Uncommon) - 1 energy, Power
    //   Effect: Whenever you are attacked, deal 3 damage back.
    //   Upgraded Effect: Whenever you are attacked, deal 5 damage back.
    public static class Caltrops extends _CaltropsT {
        public Caltrops() {
            super("Caltrops", 3);
        }
    }

    public static class CaltropsP extends _CaltropsT {
        public CaltropsP() {
            super("Caltrops+", 5);
        }
    }

    private static abstract class _CatalystT extends Card {
        private final int n;

        public _CatalystT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.n = n;
            this.entityProperty.poisonEnemy = true;
            entityProperty.selectEnemy = true;
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

    // Catalyst (Uncommon) - 1 energy, Skill
    //   Effect: Double the enemy's Poison. Exhaust.
    //   Upgraded Effect: Triple the enemy's Poison. Exhaust.
    public static class Catalyst extends _CatalystT {
        public Catalyst() {
            super("Catalyst", 2);
        }
    }

    public static class CatalystP extends _CatalystT {
        public CatalystP() {
            super("Catalyst+", 3);
        }
    }

    private static abstract class _ChokeT extends Card {
        private final int n;

        public _ChokeT(String cardName, int n) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.n = n;
            this.entityProperty.chokeEnemy = true;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 12, this);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.CHOKE, n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnPreCardPlayedHandler("Choke", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                        if (state.getEnemiesForRead().get(i).getChoke() > 0) {
                            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(i), state.getEnemiesForRead().get(i).getChoke(), false);
                        }
                    }
                }
            });
        }
    }

    // Choke (Uncommon) - 2 energy, Attack
    //   Effect: Deal 12 damage. Whenever you play a card this turn, the enemy loses 3 HP.
    //   Upgraded Effect: Deal 12 damage. Whenever you play a card this turn, the enemy loses 5 HP.
    public static class Choke extends _ChokeT {
        public Choke() {
            super("Choke", 3);
        }
    }

    public static class ChokeP extends _ChokeT {
        public ChokeP() {
            super("Choke+", 5);
        }
    }

    private static abstract class _ConcentrateT extends Card {
        private final int n;

        public _ConcentrateT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.n = n;
            entityProperty.selectFromHand = true;
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

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Concentrate", this, new GameProperties.NetworkInputHandler() {
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

    // Concentrate (Uncommon) - 0 energy, Skill
    //   Effect: Discard 3 cards. Gain.
    //   Upgraded Effect: Discard 2 cards. Gain.
    public static class Concentrate extends _ConcentrateT {
        public Concentrate() {
            super("Concentrate", 3);
        }
    }

    public static class ConcentrateP extends _ConcentrateT {
        public ConcentrateP() {
            super("Concentrate+", 2);
        }
    }

    private static abstract class _CripplingCloudT extends Card {
        private final int n;

        public _CripplingCloudT(String cardName, int n) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
            this.n = n;
            this.entityProperty.poisonEnemy = true;
            this.entityProperty.weakEnemy = true;
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

    // Crippling Cloud (Uncommon) - 2 energy, Skill
    //   Effect: Apply 4 Poison and 2 Weak to ALL enemies. Exhaust.
    //   Upgraded Effect: Apply 7 Poison and 2 Weak to ALL enemies. Exhaust.
    public static class CripplingCloud extends _CripplingCloudT {
        public CripplingCloud() {
            super("Crippling Cloud", 4);
        }
    }

    public static class CripplingCloudP extends _CripplingCloudT {
        public CripplingCloudP() {
            super("Crippling Cloud+", 7);
        }
    }

    private static abstract class _DashT extends Card {
        private final int n;

        public _DashT(String cardName, int n) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            state.playerGainBlock(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Dash (Uncommon) - 2 energy, Attack
    //   Effect: Gain 10 Block. Deal 10 damage.
    //   Upgraded Effect: Gain 13 Block. Deal 13 damage.
    public static class Dash extends _DashT {
        public Dash() {
            super("Dash", 10);
        }
    }

    public static class DashP extends _DashT {
        public DashP() {
            super("Dash+", 13);
        }
    }

    private static abstract class _DistractionT extends Card {
        public _DistractionT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.UNCOMMON);
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var r = state.getSearchRandomGen().nextInt(generatedCardIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, generatedCardIdxes));
            state.addCardToHand(generatedCardIdxes[r], true);
            state.setIsStochastic();
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return CardManager.getCharacterCardsByTypeTmp0Cost(CharacterEnum.SILENT, Card.SKILL, false);
        }
    }

    // Distraction (Uncommon) - 1 energy, Skill
    //   Effect: Add a random Skill into your hand. It costs 0 this turn. Exhaust.
    //   Upgraded Effect (0 energy): Add a random Skill into your hand. It costs 0 this turn. Exhaust.
    public static class Distraction extends _DistractionT {
        public Distraction() {
            super("Distraction", 1);
        }
    }

    public static class DistractionP extends _DistractionT {
        public DistractionP() {
            super("Distraction+", 0);
        }
    }

    private static abstract class _EndlessAgonyT extends Card {
        private final int n;
        private boolean[] isEndlessAgony;

        public _EndlessAgonyT(String cardName, int n) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            isEndlessAgony = new boolean[state.properties.cardDict.length];
            for (int i = 0; i < state.properties.cardDict.length; i++) {
                if (state.properties.cardDict[i].cardName.contains("Endless Agony")) {
                    isEndlessAgony[i] = true;
                }
            }
            state.properties.addOnCardDrawnHandler("EndlessAgony", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    state.addGameActionToStartOfDeque(new GameEnvironmentAction() {
                        @Override public void doAction(GameState state) {
                            if (isEndlessAgony[cardIdx]) {
                                state.addCardToHand(cardIdx, true);
                            }
                        }
                        @Override public boolean canHappenInsideCardPlay() {
                            return true;
                        }
                    });
                }
            });
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(this);
        }
    }

    // Endless Agony (Uncommon) - 0 energy, Attack
    //   Effect: Whenever you draw this card, add a copy of it into your hand. Deal 4 damage. Exhaust.
    //   Upgraded Effect: Whenever you draw this card, add a copy of it into your hand. Deal 6 damage. Exhaust.
    public static class EndlessAgony extends _EndlessAgonyT {
        public EndlessAgony() {
            super("Endless Agony", 4);
        }
    }

    public static class EndlessAgonyP extends _EndlessAgonyT {
        public EndlessAgonyP() {
            super("Endless Agony+", 6);
        }
    }

    private static abstract class _EscapePlanT extends Card {
        private final int n;

        public _EscapePlanT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var cardIdx = state.draw(1);
            if (cardIdx >= 0 && state.properties.cardDict[cardIdx].cardType == Card.SKILL) {
                state.playerGainBlock(n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Escape Plan (Uncommon) - 0 energy, Skill
    //   Effect: Draw 1 card. If you draw a Skill, gain 3 Block.
    //   Upgraded Effect: Draw 1 card. If you draw a Skill, gain 5 Block.
    public static class EscapePlan extends _EscapePlanT {
        public EscapePlan() {
            super("Escape Plan", 3);
        }
    }

    public static class EscapePlanP extends _EscapePlanT {
        public EscapePlanP() {
            super("Escape Plan+", 5);
        }
    }

    private static abstract class _EviscerateT extends Card {
        private final int n;

        public _EviscerateT(String cardName, int n) {
            super(cardName, Card.ATTACK, 3, Card.UNCOMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            // todo: hmm doesn't work well with snecko...
            return Math.max(energyCost - state.getCounterForRead()[state.properties.eviscerateCounterIdx], 0);
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Eviscerate", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int counterIdx) {
                    state.properties.eviscerateCounterIdx = counterIdx;
                }
            });
            state.properties.addEndOfTurnHandler("Eviscerate", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[state.properties.eviscerateCounterIdx] = 0;
                }
            });
        }
    }

    // Eviscerate (Uncommon) - 3 energy, Attack
    //   Effect: Costs 1 less for each card discarded this turn. Deal 7 damage 3 times.
    //   Upgraded Effect: Costs 1 less for each card discarded this turn. Deal 9 damage 3 times.
    public static class Eviscerate extends _EviscerateT {
        public Eviscerate() {
            super("Eviscerate", 7);
        }
    }

    public static class EviscerateP extends _EviscerateT {
        public EviscerateP() {
            super("Eviscerate+", 9);
        }
    }

    private static abstract class _ExpertiseT extends Card {
        private final int n;

        public _ExpertiseT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
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

    // Expertise (Uncommon) - 1 energy, Skill
    //   Effect: Draw cards until you have 6 in your hand.
    //   Upgraded Effect: Draw cards until you have 7 in your hand.
    public static class Expertise extends _ExpertiseT {
        public Expertise() {
            super("Expertise", 6);
        }
    }

    public static class ExpertiseP extends _ExpertiseT {
        public ExpertiseP() {
            super("Expertise+", 7);
        }
    }

    private static abstract class _FinisherT extends Card {
        private final int n;

        public _FinisherT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < state.getCounterForRead()[counterIdx]; i++) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Finisher", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = Math.abs(state.getCounterForRead()[counterIdx]) / 20.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
           state.properties.addEndOfTurnHandler(new GameEventHandler() {
               @Override public void handle(GameState state) {
                   if (state.getCounterForRead()[counterIdx] > 0) {
                       state.getCounterForWrite()[counterIdx] = 0;
                   }
               }
           });
            state.properties.addOnCardPlayedHandler("Finisher", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        state.getCounterForWrite()[counterIdx]++;
                    }
                }
            });
        }
    }

    // Finisher (Uncommon) - 1 energy, Attack
    //   Effect: Deal 6 damage for each Attack played this turn.
    //   Upgraded Effect: Deal 8 damage for each Attack played this turn.
    public static class Finisher extends _FinisherT {
        public Finisher() {
            super("Finisher", 6);
        }
    }

    public static class FinisherP extends _FinisherT {
        public FinisherP() {
            super("Finisher+", 8);
        }
    }

    static abstract class _FlechettesT extends Card {
        private final int n;

        public _FlechettesT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < state.handArrLen; i++) {
                if (state.properties.cardDict[state.getHandArrForRead()[i]].cardType == Card.SKILL) {
                    state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Flechettes (Uncommon) - 1 energy, Attack
    //   Effect: Deal 4 damage for each Skill in your hand.
    //   Upgraded Effect: Deal 6 damage for each Skill in your hand.
    public static class Flechettes extends _FlechettesT {
        public Flechettes() {
            super("Flechettes", 4);
        }
    }

    public static class FlechettesP extends _FlechettesT {
        public FlechettesP() {
            super("Flechettes+", 6);
        }
    }

    private static abstract class _FootworkT extends Card {
        private final int n;

        public _FootworkT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainDexterity(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Footwork (Uncommon) - 1 energy, Power
    //   Effect: Gain 2 Dexterity.
    //   Upgraded Effect: Gain 3 Dexterity.
    public static class Footwork extends _FootworkT {
        public Footwork() {
            super("Footwork", 2);
        }
    }

    public static class FootworkP extends _FootworkT {
        public FootworkP() {
            super("Footwork+", 3);
        }
    }

    private static abstract class _HeelHookT extends Card {
        private final int n;

        public _HeelHookT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, n, this);
            if (enemy.getWeak() > 0) {
                state.gainEnergy(1);
                state.draw(1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Heel Hook (Uncommon) - 1 energy, Attack
    //   Effect: Deal 5 damage. If the enemy has Weak, gain and draw 1 card.
    //   Upgraded Effect: Deal 8 damage. If the enemy has Weak, gain and draw 1 card.
    public static class HeelHook extends _HeelHookT {
        public HeelHook() {
            super("Heel Hook", 5);
        }
    }

    public static class HeelHookP extends _HeelHookT {
        public HeelHookP() {
            super("Heel Hook+", 8);
        }
    }

    private static abstract class _InfiniteBladeT extends Card {
        public _InfiniteBladeT(String cardName, boolean innate) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.innate = innate;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Shiv());
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("InfiniteBlade", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addPreStartOfTurnHandler("InfiniteBlade", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    for (int i = 0; i < state.getCounterForRead()[counterIdx]; i++) {
                        state.addCardToHand(generatedCardIdx, true);
                    }
                }
            });
        }
    }

    // Infinite Blades (Uncommon) - 1 energy, Power
    //   Effect: At the start of your turn, add a Shiv into your hand.
    //   Upgraded Effect: Innate. At the start of your turn, add a Shiv into your hand.
    public static class InfiniteBlades extends _InfiniteBladeT {
        public InfiniteBlades() {
            super("Infinite Blades", false);
        }
    }

    public static class InfiniteBladesP extends _InfiniteBladeT {
        public InfiniteBladesP() {
            super("Infinite Blades+", true);
        }
    }

    private static abstract class _LegSweepT extends Card {
        private final int n;
        private final int weak;

        public _LegSweepT(String cardName, int n, int weak) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
            this.n = n;
            this.weak = weak;
            this.entityProperty.weakEnemy = true;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(n);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, weak);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Leg Sweep (Uncommon) - 2 energy, Skill
    //   Effect: Apply 2 Weak. Gain 11 Block.
    //   Upgraded Effect: Apply 3 Weak. Gain 14 Block.
    public static class LegSweep extends _LegSweepT {
        public LegSweep() {
            super("Leg Sweep", 11, 2);
        }
    }

    public static class LegSweepP extends _LegSweepT {
        public LegSweepP() {
            super("Leg Sweep+", 14, 3);
        }
    }

    // Masterful Stab (Uncommon) - 0 energy, Attack
    //   Effect: Costs 1 additional for each time you lose HP this combat. Deal 12 damage.
    //   Upgraded Effect: Costs 1 additional for each time you lose HP this combat. Deal 16 damage.
    public static class MasterfulStab extends Card {
        private final int maxEnergyCost;

        public MasterfulStab() { this(0); }

        public MasterfulStab(int maxEnergyCost) {
            this(0, maxEnergyCost);
        }

        public MasterfulStab(int energyCost, int maxEnergyCost) {
            super("Masterful Stab (" + energyCost + ")", Card.ATTACK, energyCost, Card.UNCOMMON);
            this.maxEnergyCost = maxEnergyCost;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 12, this);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = 0; i < cards.size(); i++) {
                if (cards.get(i).getBaseCard() instanceof MasterfulStab ms) {
                    for (int j = 0; j <= ms.maxEnergyCost; j++) {
                        c.add(cards.get(i).wrap(new MasterfulStab(j, ms.maxEnergyCost)));
                    }
                }
            }
            return c;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.masterfulStabTransformIndexes = new int[state.properties.cardDict.length];
            Arrays.fill(state.properties.masterfulStabTransformIndexes, -1);
            for (int i = 0; i < state.properties.cardDict.length; i++) {
                var card = state.properties.cardDict[i];
                if (card.getBaseCard() instanceof MasterfulStab ms && ms.energyCost < ms.maxEnergyCost) {
                    var toIdx = state.properties.findCardIndex(card.wrap(new MasterfulStab(ms.energyCost + 1, ms.maxEnergyCost)));
                    state.properties.masterfulStabTransformIndexes[i] = toIdx;
                }
            }
            state.properties.addOnDamageHandler("Masterful Stab", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (damageDealt <= 0) return;
                    state.handArrTransform(state.properties.masterfulStabTransformIndexes);
                    state.discardArrTransform(state.properties.masterfulStabTransformIndexes);
                    state.deckArrTransform(state.properties.masterfulStabTransformIndexes);
                    state.exhaustArrTransform(state.properties.masterfulStabTransformIndexes);
                }
            });
        }

        @Override public Card getUpgrade() {
            return new CardSilent.MasterfulStab(energyCost, maxEnergyCost);
        }
    }

    public static class MasterfulStabP extends Card {
        private final int maxEnergyCost;

        public MasterfulStabP() { this(0); }

        public MasterfulStabP(int maxEnergyCost) {
            this(0, maxEnergyCost);
        }

        public MasterfulStabP(int energyCost, int maxEnergyCost) {
            super("Masterful Stab+ (" + energyCost + ")", Card.ATTACK, energyCost, Card.UNCOMMON);
            this.maxEnergyCost = maxEnergyCost;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 16, this);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = 0; i < cards.size(); i++) {
                if (cards.get(i).getBaseCard() instanceof MasterfulStabP ms) {
                    for (int j = 0; j <= ms.maxEnergyCost; j++) {
                        c.add(cards.get(i).wrap(new MasterfulStabP(j, ms.maxEnergyCost)));
                    }
                }
            }
            return c;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.masterfulStabPTransformIndexes = new int[state.properties.cardDict.length];
            Arrays.fill(state.properties.masterfulStabPTransformIndexes, -1);
            for (int i = 0; i < state.properties.cardDict.length; i++) {
                var card = state.properties.cardDict[i];
                if (card.getBaseCard() instanceof MasterfulStabP ms && ms.energyCost < ms.maxEnergyCost) {
                    var toIdx = state.properties.findCardIndex(card.wrap(new MasterfulStabP(ms.energyCost + 1, ms.maxEnergyCost)));
                    state.properties.masterfulStabPTransformIndexes[i] = toIdx;
                }
            }
            state.properties.addOnDamageHandler("Masterful Stab+", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (damageDealt <= 0) return;
                    state.handArrTransform(state.properties.masterfulStabPTransformIndexes);
                    state.discardArrTransform(state.properties.masterfulStabPTransformIndexes);
                    state.deckArrTransform(state.properties.masterfulStabPTransformIndexes);
                    state.exhaustArrTransform(state.properties.masterfulStabPTransformIndexes);
                }
            });
        }

        @Override public Card getUpgrade() {
            return new CardSilent.MasterfulStabP(energyCost, maxEnergyCost);
        }

        @Override public Card getDowngrade() {
            return new CardSilent.MasterfulStab(energyCost, maxEnergyCost);
        }
    }

    private static abstract class _NoxiousFumesT extends Card {
        private final int n;

        public _NoxiousFumesT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            entityProperty.poisonEnemy = true;
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("NoxiousFumes", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 9.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("NoxiousFumes", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            enemy.applyDebuff(state, DebuffType.POISON, state.getCounterForRead()[counterIdx]);
                        }
                    }
                }
            });
        }
    }

    // Noxious Fumes (Uncommon) - 1 energy, Power
    //   Effect: At the start of your turn, apply 2 Poison to ALL enemies.
    //   Upgraded Effect: At the start of your turn, apply 3 Poison to ALL enemies.
    public static class NoxiousFumes extends _NoxiousFumesT {
        public NoxiousFumes() {
            super("Noxious Fumes", 2);
        }
    }

    public static class NoxiousFumesP extends _NoxiousFumesT {
        public NoxiousFumesP() {
            super("Noxious Fumes+", 3);
        }
    }

    private static abstract class _PredatorT extends Card {
        private final int n;

        public _PredatorT(String cardName, int n) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            state.getCounterForWrite()[counterIdx] += 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Predator", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Predator", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.draw(state.getCounterForRead()[counterIdx] * 2);
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    // Predator (Uncommon) - 2 energy, Attack
    //   Effect: Deal 15 damage. Next turn, draw 2 additional cards.
    //   Upgraded Effect: Deal 20 damage. Next turn, draw 2 additional cards.
    public static class Predator extends _PredatorT {
        public Predator() {
            super("Predator", 15);
        }
    }

    public static class PredatorP extends _PredatorT {
        public PredatorP() {
            super("Predator+", 20);
        }
    }

    private static abstract class _ReflexT extends Card {
        private final int n;

        public _ReflexT(String cardName, int n) {
            super(cardName, Card.SKILL, -1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void onDiscard(GameState state) {
            state.addGameActionToEndOfDeque(state1 -> state1.draw(n));
        }
    }

    // Reflex (Uncommon) - Unplayable, Skill
    //   Effect: Unplayable. If this card is discarded from your hand, draw 2 cards.
    //   Upgraded Effect: Unplayable. If this card is discarded from your hand, draw 3 cards.
    public static class Reflex extends _ReflexT {
        public Reflex() {
            super("Reflex", 2);
        }
    }

    public static class ReflexP extends _ReflexT {
        public ReflexP() {
            super("Reflex+", 3);
        }
    }

    private static abstract class _RiddleWithHolesT extends Card {
        private final int n;

        public _RiddleWithHolesT(String cardName, int n) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < 5; i++) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Riddle with Holes (Uncommon) - 2 energy, Attack
    //   Effect: Deal 3 damage 5 times.
    //   Upgraded Effect: Deal 4 damage 5 times.
    public static class RiddleWithHoles extends _RiddleWithHolesT {
        public RiddleWithHoles() {
            super("Riddle with Holes", 3);
        }
    }

    public static class RiddleWithHolesP extends _RiddleWithHolesT {
        public RiddleWithHolesP() {
            super("Riddle with Holes+", 4);
        }
    }

    private static abstract class _SetupT extends Card {
        public boolean doNothing = false;

        public _SetupT(String cardName, int energyCost, boolean doNothing) {
            this(cardName + (doNothing ? "(N/A)" : ""), energyCost);
            this.doNothing = doNothing;
            entityProperty.selectFromHand = !doNothing;
            entityProperty.putCardOnTopDeck = !doNothing;
        }

        public _SetupT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (doNothing) {
                return GameActionCtx.PLAY_CARD;
            }
            state.removeCardFromHand(idx);
            state.addCardOnTopOfDeck(state.properties.findCardIndex(state.properties.cardDict[idx].getTemporaryCostUntilPlayedIfPossible(0)));
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return doNothing ? List.of() : cards.stream().map(card -> card.getTemporaryCostUntilPlayedIfPossible(0)).toList();
        }
    }

    // Setup (Uncommon) - 1 energy, Skill
    //   Effect: Put a card from your hand on top of your draw pile. It costs 0 until played.
    //   Upgraded Effect (0 energy): Put a card from your hand on top of your draw pile. It costs 0 until played.
    public static class Setup extends _SetupT {
        public Setup() { this(true); }

        public Setup(boolean doNothing) {
            super("Setup", 1, doNothing);
        }

       @Override public Card getUpgrade() {
           return new CardSilent.Setup(doNothing);
       }
    }

    public static class SetupP extends _SetupT {
        public SetupP() { this(true); }

        public SetupP(boolean doNothing) {
            super("Setup+", 0, doNothing);
        }

        @Override public Card getUpgrade() {
            return new CardSilent.SetupP(doNothing);
        }

        @Override public Card getDowngrade() {
            return new CardSilent.Setup(doNothing);
        }
    }

    private static abstract class _SkewerT extends Card {
        private final int n;

        public _SkewerT(String cardName, int n) {
            super(cardName, Card.ATTACK, -1, Card.UNCOMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
            this.isXCost = true;
            this.delayUseEnergy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < energyUsed; i++) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            return state.energy;
        }
    }

    // Skewer (Uncommon) - X energy, Attack
    //   Effect: Deal 7 damage X times.
    //   Upgraded Effect: Deal 10 damage X times.
    public static class Skewer extends _SkewerT {
        public Skewer() {
            super("Skewer", 7);
        }
    }

    public static class SkewerP extends _SkewerT {
        public SkewerP() {
            super("Skewer+", 10);
        }
    }

    private static abstract class _TacticianT extends Card {
        private final int n;

        public _TacticianT(String cardName, int n) {
            super(cardName, Card.SKILL, -1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void onDiscard(GameState state) {
            state.gainEnergy(n);
        }
    }

    // Tactician (Uncommon) - Unplayable, Skill
    //   Effect: Unplayable. If this card is discarded from your hand, gain.
    public static class Tactician extends _TacticianT {
        public Tactician() {
            super("Tactician", 1);
        }
    }

    public static class TacticianP extends _TacticianT {
        public TacticianP() {
            super("Tactician+", 2);
        }
    }

    private static abstract class _TerrorT extends Card {
        public _TerrorT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.UNCOMMON);
            this.entityProperty.vulnEnemy = true;
            entityProperty.selectEnemy = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.VULNERABLE, 99);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Terror (Uncommon) - 1 energy, Skill
    //   Effect: Apply 99 Vulnerable. Exhaust.
    //   Upgraded Effect (0 energy): Apply 99 Vulnerable. Exhaust.
    public static class Terror extends _TerrorT {
        public Terror() {
            super("Terror", 1);
        }
    }

    public static class TerrorP extends _TerrorT {
        public TerrorP() {
            super("Terror+", 0);
        }
    }

    private static abstract class _WellLaidPlansT extends Card {
        private final int n;

        public _WellLaidPlansT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.n = n;
            entityProperty.selectFromHand = true;
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

        public void gamePropertiesSetup(GameState state) {
            state.chosenCardsArr = new short[2];
            state.properties.registerCounter("WellLaidPlans", this, new GameProperties.NetworkInputHandler() {
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

                @Override public void onRegister(int counterIdx) {
                    state.properties.wellLaidPlansCounterIdx = counterIdx;
                }
            });
            state.properties.addEndOfTurnHandler("WellLaidPlans", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] &= 31;
                }
            });
        }
    }

    // Well-Laid Plans (Uncommon) - 1 energy, Power
    //   Effect: At the end of your turn, Retain up to 1 card.
    //   Upgraded Effect: At the end of your turn, Retain up to 2 cards.
    public static class WellLaidPlans extends _WellLaidPlansT {
        public WellLaidPlans() {
            super("Well-Laid Plans", 1);
        }
    }

    public static class WellLaidPlansP extends _WellLaidPlansT {
        public WellLaidPlansP() {
            super("Well-Laid Plans+", 2);
        }
    }

    private static abstract class _AThousandCutsT extends Card {
        protected final int n;

        public _AThousandCutsT(String cardName, int n) {
            super(cardName, Card.POWER, 2, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("AThousandCuts", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 8.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("AThousandCuts", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        var dmg = state.getCounterForRead()[counterIdx];
                        if (state.properties.cardDict[cardIdx].cardName.startsWith("A Thousand")) {
                            dmg -= ((_AThousandCutsT) state.properties.cardDict[cardIdx]).n;
                        }
                        if (dmg > 0) {
                            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                                state.playerDoNonAttackDamageToEnemy(enemy, dmg, true);
                            }
                        }
                    }
                }
            });
        }
    }

    // A Thousand Cuts (Rare) - 2 energy, Power
    //   Effect: Whenever you play a card, deal 1 damage to ALL enemies.
    //   Upgraded Effect: Whenever you play a card, deal 2 damage to ALL enemies.
    public static class AThousandCuts extends _AThousandCutsT {
        public AThousandCuts() {
            super("A Thousand Cuts", 1);
        }
    }

    public static class AThousandCutsP extends _AThousandCutsT {
        public AThousandCutsP() {
            super("A Thousand Cuts+", 2);
        }
    }

    private static abstract class _AdrenalineT extends Card {
        private final int n;

        public _AdrenalineT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.RARE);
            this.n = n;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(n);
            state.draw(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Adrenaline (Rare) - 0 energy, Skill
    //   Effect: Gain. Draw 2 cards. Exhaust.
    public static class Adrenaline extends _AdrenalineT {
        public Adrenaline() {
            super("Adrenaline", 1);
        }
    }

    public static class AdrenalineP extends _AdrenalineT {
        public AdrenalineP() {
            super("Adrenaline+", 2);
        }
    }

    private static abstract class _AfterImageT extends Card {
        public _AfterImageT(String cardName, boolean innate) {
            super(cardName, Card.POWER, 1, Card.RARE);
            this.innate = innate;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.addGameActionToEndOfDeque(state1 -> state1.getCounterForWrite()[counterIdx]++);
            return GameActionCtx.PLAY_CARD;
        }

        public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("AfterImage", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = Math.abs(state.getCounterForRead()[counterIdx]) / 4.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("AfterImage", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.playerGainBlockNotFromCardPlay(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }
    }

    // After Image (Rare) - 1 energy, Power
    //   Effect: Whenever you play a card, gain 1 Block.
    //   Upgraded Effect: Innate. Whenever you play a card, gain 1 Block.
    public static class AfterImage extends _AfterImageT {
        public AfterImage() {
            super("After Image", false);
        }
    }

    public static class AfterImageP extends _AfterImageT {
        public AfterImageP() {
            super("After Image+", true);
        }
    }

    public static abstract class _AlchemizeT extends Card {
        public final int basePenaltyRatio;
        public final int possibleGeneratedPotions;
        public final int healthReward;

        public _AlchemizeT(String cardName, int energyCost, int basePenaltyRatio, int possibleGeneratedPotions, int healthReward) {
            super(cardName, Card.SKILL, energyCost, Card.RARE);
            this.exhaustWhenPlayed = true;
            this.basePenaltyRatio = basePenaltyRatio;
            this.possibleGeneratedPotions = possibleGeneratedPotions;
            this.healthReward = healthReward;
        }

        @Override public void setVExtraIdx(GameProperties gameProperties, int idx) {
            super.setVExtraIdx(gameProperties, idx);
            gameProperties.alchemizeVExtraIdx = idx;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int potions = state.getPotionCount();
            if (basePenaltyRatio == 0) {
                potions += state.getCounterForRead()[counterIdx];
            }
            if (potions < state.properties.numOfPotionSlots) {
                if (state.getCounterForRead()[counterIdx] < 4) { // 4 empty potion slot at most
                    state.getCounterForWrite()[counterIdx]++;
                    if (state.properties.potionsGenerator != null) {
                        state.properties.potionsGenerator.generatePotion(state);
                    }
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Alchemize", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[counter[counterIdx]] = 1;
                    return idx + 5;
                }
                @Override public int getInputLenDelta() {
                    return 5;
                }
            });

            state.properties.addExtraTrainingTarget("Alchemize", this, new TrainingTarget() {
                @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                    if (isTerminal != 0) {
                        for (int i = 0; i < 5; i++) {
                            v.setVExtra(vExtraIdx + i, 0);
                        }
                        v.setVExtra(vExtraIdx + state.getCounterForRead()[counterIdx], 1);
                    } else if (isTerminal == 0) {
                        for (int i = 0; i < 5; i++) {
                            v.setVExtra(vExtraIdx + i, state.getVExtra(vExtraIdx + i));
                        }
                    }
                }

                @Override public void updateQValues(GameState state, VArray v) {
                    if (!state.isEncounter(PredefinedEncounter.CORRUPT_HEART)) {
                        for (int i = 0; i < 5; i++) {
                            v.add(GameState.V_HEALTH_IDX, i * healthReward * v.getVExtra(vExtraIdx) / state.getPlayerForRead().getMaxHealth());
                        }
                    }
                }

                @Override public int getNumberOfTargets() {
                    return 5;
                }
            });

            if (basePenaltyRatio > 0 && state.properties.alchemizeCardIdxes == null) {
                state.properties.alchemizeCardIdxes = new ArrayList<>();
                for (int i = 0; i < state.properties.cardDict.length; i++) {
                    if (state.properties.cardDict[i].cardName.startsWith("Alchemize")) {
                        state.properties.alchemizeCardIdxes.add(i);
                    }
                }
            }
        }

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, 5, "Alchemize");
        }

        private static int getCardCount(GameState state, int idx) {
            int count = 0;
            count += GameStateUtils.getCardCount(state.getHandArrForRead(), state.handArrLen, idx);
            if (idx < state.properties.realCardsLen) {
                count += GameStateUtils.getCardCount(state.getDiscardArrForRead(), state.discardArrLen, idx);
                count += GameStateUtils.getCardCount(state.getDeckArrForRead(), state.deckArrLen, idx);
            }
            return count;
        }

        public static int getPossibleAlchemize(GameState state) {
            if (state.properties.alchemizeCardIdxes == null) {
                return 0;
            }
            int total = 0;
            for (int i = 0; i < state.properties.alchemizeCardIdxes.size(); i++) {
                total += getCardCount(state, state.properties.alchemizeCardIdxes.get(i));
            }
            return total;
        }
    }

    // Alchemize (Rare) - 1 energy, Skill
    //   Effect: Obtain a random potion. Exhaust.
    //   Upgraded Effect (0 energy): Obtain a random potion. Exhaust.
    public static class Alchemize extends CardSilent._AlchemizeT {
        public Alchemize() { this(100, 0, 0); }

        public Alchemize(int basePenaltyRatio, int possibleGeneratedPotions, int healthReward) {
            super("Alchemize", 1, basePenaltyRatio, possibleGeneratedPotions, healthReward);
        }

        @Override public Card getUpgrade() {
            return new CardSilent.AlchemizeP(basePenaltyRatio, possibleGeneratedPotions, healthReward);
        }
    }

    public static class AlchemizeP extends CardSilent._AlchemizeT {
        public AlchemizeP() { this(100, 0, 0); }

        public AlchemizeP(int basePenaltyRatio, int possibleGeneratedPotions, int healthReward) {
            super("Alchemize+", 0, basePenaltyRatio, possibleGeneratedPotions, healthReward);
        }

        @Override public Card getDowngrade() {
            return new CardSilent.Alchemize(basePenaltyRatio, possibleGeneratedPotions, healthReward);
        }
    }

    private static abstract class _BulletTimeT extends Card {
        public _BulletTimeT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.RARE);
            this.entityProperty.addPossibleBuff(PlayerBuff.NO_CARD_DRAW_FOR_THE_TURN);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.handArrTransform(state.properties.tmp0CostCardTransformIdxes);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.NO_MORE_CARD_DRAW, 1);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleTransformTmpCostCards(List<Card> cards) {
            return cards.stream().filter((x) -> !x.isXCost && x.energyCost > 0).map((x) -> x.getTemporaryCostIfPossible(0)).toList();
        }
    }

    // Bullet Time (Rare) - 3 energy, Skill
    //   Effect: You cannot draw additional cards this turn. Reduce the cost of all cards in your hand to 0 this turn.
    //   Upgraded Effect (2 energy): You cannot draw additional cards this turn. Reduce the cost of all cards in your hand to 0 this turn.
    public static class BulletTime extends _BulletTimeT {
        public BulletTime() {
            super("Bullet Time", 3);
        }
    }

    public static class BulletTimeP extends _BulletTimeT {
        public BulletTimeP() {
            super("Bullet Time+", 2);
        }
    }

    private static abstract class _BurstT extends Card {
        private final int n;

        public _BurstT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.getCounterForRead()[counterIdx] == 0) {
                state.getCounterForWrite()[counterIdx] |= 1 << 9;
            }
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Burst", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = Math.abs(state.getCounterForRead()[counterIdx]) / 4.0f;
                    input[idx + 1] = (state.getCounterForRead()[counterIdx] & (1 << 8)) > 0 ? 0.5f : 0;
                    return idx + 2;
                }
                @Override public int getInputLenDelta() {
                    return 2;
                }
            });
            state.properties.addStartOfTurnHandler("Burst", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForWrite()[counterIdx] != 0) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
            state.properties.addOnCardPlayedHandler("Burst", new GameEventCardHandler(GameEventCardHandler.CLONE_CARD_PRIORITY) {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    var card = state.properties.cardDict[cardIdx];
                    if (card.cardType != Card.SKILL || state.getCounterForRead()[counterIdx] == 0) {
                        return;
                    }
                    if ((state.getCounterForRead()[counterIdx] & (1 << 9)) != 0) {
                        state.getCounterForWrite()[counterIdx] ^= 1 << 9;
                        return;
                    }
                    if (cloneSource != null && (state.getCounterForRead()[counterIdx] & (1 << 8)) > 0) {
                        state.getCounterForWrite()[counterIdx] ^= 1 << 8;
                    } else {
                        var counters = state.getCounterForWrite();
                        counters[counterIdx]--;
                        counters[counterIdx] |= 1 << 8;
                        state.addGameActionToEndOfDeque(curState -> {
                            var action = curState.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                            if (curState.playCard(action, lastIdx, true, Burst.class, false, false, energyUsed, cloneParentLocation)) {
                            } else {
                                curState.getCounterForWrite()[counterIdx] ^= 1 << 8;
                            }
                        });
                    }
                }
            });
        }
    }

    // Burst (Rare) - 1 energy, Skill
    //   Effect: This turn, your next Skill is played twice.
    //   Upgraded Effect: This turn, your next 2 Skills are played twice.
    public static class Burst extends _BurstT {
        public Burst() {
            super("Burst", 1);
        }
    }

    public static class BurstP extends _BurstT {
        public BurstP() {
            super("Burst+", 2);
        }
    }

    private static abstract class _CorpseExplosionT extends Card {
        private final int n;

        public _CorpseExplosionT(String cardName, int n) {
            super(cardName, Card.SKILL, 2, Card.RARE);
            this.n = n;
            entityProperty.selectEnemy = true;
            this.entityProperty.poisonEnemy = true;
            this.entityProperty.corpseExplosionEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.POISON, n);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.CORPSE_EXPLOSION, 1);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnEnemyDeathHandler("CorpseExplosion", new GameEventEnemyHandler() {
                @Override public void handle(GameState state, EnemyReadOnly enemy, EnemyDeathCause cause) {
                    if (enemy.getCorpseExplosion() > 0) {
                        var k = enemy.getCorpseExplosion();
                        state.addGameActionToEndOfDeque(new GameEnvironmentAction() {
                            @Override public void doAction(GameState state) {
                                for (Enemy e : state.getEnemiesForWrite().iterateOverAlive()) {
                                    state.playerDoNonAttackDamageToEnemy(e, enemy.getMaxHealthInBattle() * k, true);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    // Corpse Explosion (Rare) - 2 energy, Skill
    //   Effect: Apply 6 Poison. When the enemy dies, deal damage equal to its max HP to ALL enemies.
    //   Upgraded Effect: Apply 9 Poison. When the enemy dies, deal damage equal to its max HP to ALL enemies.
    public static class CorpseExplosion extends _CorpseExplosionT {
        public CorpseExplosion() {
            super("Corpse Explosion", 6);
        }
    }

    public static class CorpseExplosionP extends _CorpseExplosionT {
        public CorpseExplosionP() {
            super("Corpse Explosion+", 9);
        }
    }

    private static abstract class _DieDieDieT extends Card {
        private final int n;

        public _DieDieDieT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.RARE);
            this.n = n;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, n, this);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Die Die Die (Rare) - 1 energy, Attack
    //   Effect: Deal 13 damage to ALL enemies. Exhaust.
    //   Upgraded Effect: Deal 17 damage to ALL enemies. Exhaust.
    public static class DieDieDie extends _DieDieDieT {
        public DieDieDie() {
            super("Die Die Die", 13);
        }
    }

    public static class DieDieDieP extends _DieDieDieT {
        public DieDieDieP() {
            super("Die Die Die+", 17);
        }
    }

    private static abstract class _DoppelgangerT extends Card {
        private final boolean upgraded;

        public _DoppelgangerT(String cardName, boolean upgraded) {
            super(cardName, Card.SKILL, -1, Card.RARE);
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

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Doppelganger", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Doppelganger", new GameEventHandler() {
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

    // Doppelganger (Rare) - X energy, Skill
    //   Effect: Next turn, draw X cards and gain X. Exhaust.
    //   Upgraded Effect: Next turn, draw X+1 cards and gain X+1. Exhaust.
    public static class Doppelganger extends _DoppelgangerT {
        public Doppelganger() {
            super("Doppelganger", false);
        }
    }

    public static class DoppelgangerP extends _DoppelgangerT {
        public DoppelgangerP() {
            super("Doppelganger+", true);
        }
    }

    static abstract class _EnvenomT extends Card {
        final int n;

        public _EnvenomT(String cardName, int energyCost, int n) {
            super(cardName, Card.POWER, energyCost, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Envenom", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int counterIdx) {
                    state.properties.envenomCounterIdx = counterIdx;
                }
            });
        }
    }

    // Envenom (Rare) - 2 energy, Power
    //   Effect: Whenever an Attack deals unblocked damage, apply 1 Poison.
    //   Upgraded Effect (1 energy): Whenever an Attack deals unblocked damage, apply 1 Poison.
    public static class Envenom extends _EnvenomT {
        public Envenom() {
            super("Envenom", 2, 1);
        }
    }

    public static class EnvenomP extends _EnvenomT {
        public EnvenomP() {
            super("Envenom+", 1, 1);
        }
    }

    // Glass Knife (Rare) - 1 energy, Attack
    //   Effect: Deal 8 damage twice. Decrease the damage of this card by 2 this combat.
    //   Upgraded Effect: Deal 12 damage twice. Decrease the damage of this card by 2 this combat.
    public static class GlassKnife extends Card {
        public int limit;
        private int dmg;

        public GlassKnife() {
            this(8);
        }

        public GlassKnife(int dmg, int limit) {
            super(dmg == 8 ? "Glass Knife" : "Glass Knife (" + dmg + ")", Card.ATTACK, 1, Card.RARE);
            this.dmg = dmg;
            this.limit = limit;
            entityProperty.selectEnemy = true;
        }

        public GlassKnife(int dmg) {
            this(dmg, 0);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg, this);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg, this);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var c = new ArrayList<Card>();
            for (Card card : cards) {
                if (card.getBaseCard() instanceof GlassKnife) {
                    for (int i = dmg - 2; i >= limit; i -= 2) {
                        c.add(card.wrapAfterPlay(new GlassKnife(i, limit)));
                    }
                }
            }
            return c;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.glassKnifeTransformIndexes = new int[state.properties.cardDict.length];
            for (int i = 0; i < state.properties.glassKnifeTransformIndexes.length; i++) {
                var card = state.properties.cardDict[i].getBaseCard();
                if (card instanceof GlassKnife glassKnife && glassKnife.dmg > glassKnife.limit) {
                    state.properties.glassKnifeTransformIndexes[i] = state.properties.findCardIndex(state.properties.cardDict[i].wrapAfterPlay(new GlassKnife(glassKnife.dmg - 2, glassKnife.limit)));
                }
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop, int cardIdx) {
            return prop.glassKnifeTransformIndexes[cardIdx];
        }
    }

    public static class GlassKnifeP extends Card {
        public int limit;
        private int dmg;

        public GlassKnifeP() {
            this(12);
        }

        public GlassKnifeP(int dmg, int limit) {
            super(dmg == 12 ? "Glass Knife+" : "Glass Knife+ (" + dmg + ")", Card.ATTACK, 1, Card.RARE);
            this.dmg = dmg;
            this.limit = limit;
            entityProperty.selectEnemy = true;
        }

        public GlassKnifeP(int dmg) {
            this(dmg, 0);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg, this);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg, this);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var c = new ArrayList<Card>();
            for (Card card : cards) {
                if (card.getBaseCard() instanceof GlassKnife) {
                    for (int i = dmg - 2; i >= limit; i -= 2) {
                        c.add(card.wrapAfterPlay(new GlassKnifeP(i, limit)));
                    }
                }
            }
            return c;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.glassKnifePTransformIndexes = new int[state.properties.cardDict.length];
            for (int i = 0; i < state.properties.glassKnifePTransformIndexes.length; i++) {
                var card = state.properties.cardDict[i].getBaseCard();
                if (card instanceof GlassKnifeP glassKnife && glassKnife.dmg > glassKnife.limit) {
                    state.properties.glassKnifePTransformIndexes[i] = state.properties.findCardIndex(state.properties.cardDict[i].wrapAfterPlay(new GlassKnifeP(glassKnife.dmg - 2, glassKnife.limit)));
                }
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop, int cardIdx) {
            return prop.glassKnifePTransformIndexes[cardIdx];
        }
    }

    public static abstract class _GrandFinaleT extends Card {
        private final int n;

        public _GrandFinaleT(String cardName, int n) {
            super(cardName, Card.ATTACK, 0, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.deckArrLen > 0) {
                return GameActionCtx.PLAY_CARD;
            }
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, n, this);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            return state.deckArrLen > 0 ? -1 : 0;
        }
    }

    // Grand Finale (Rare) - 0 energy, Attack
    //   Effect: Can only be played if there are no cards in your draw pile. Deal 50 damage to ALL enemies.
    //   Upgraded Effect: Can only be played if there are no cards in your draw pile. Deal 60 damage to ALL enemies.
    public static class GrandFinale extends _GrandFinaleT {
        public GrandFinale() {
            super("Grand Finale", 50);
        }
    }

    public static class GrandFinaleP extends _GrandFinaleT {
        public GrandFinaleP() {
            super("Grand Finale+", 60);
        }
    }

    private static abstract class _MalaiseT extends Card {
        private final boolean upgraded;

        public _MalaiseT(String cardName, boolean upgraded) {
            super(cardName, Card.SKILL, -1, Card.RARE);
            this.upgraded = upgraded;
            this.isXCost = true;
            this.exhaustWhenPlayed = true;
            this.entityProperty.weakEnemy = true;
            this.entityProperty.affectEnemyStrength = true;
            entityProperty.selectEnemy = true;
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

    // Malaise (Rare) - X energy, Skill
    //   Effect: Enemy loses X Strength. Apply X Weak. Exhaust.
    //   Upgraded Effect: Enemy loses X+1 Strength. Apply X+1 Weak. Exhaust.
    public static class Malaise extends _MalaiseT {
        public Malaise() {
            super("Malaise", false);
        }
    }

    public static class MalaiseP extends _MalaiseT {
        public MalaiseP() {
            super("Malaise+", true);
        }
    }

    private static abstract class _NightmareT extends Card {
        public _NightmareT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.RARE);
            entityProperty.selectFromHand = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.addNightmareCard(idx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.nightmareCards = new short[1];
            state.properties.addPreStartOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    for (int i = 0; i < state.nightmareCardsLen; i++) {
                        state.addCardToHand(state.nightmareCards[i], true);
                        state.addCardToHand(state.nightmareCards[i], true);
                        state.addCardToHand(state.nightmareCards[i], true);
                    }
                    state.nightmareCardsLen = 0;
                }
            });
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return new ArrayList<>(cards);
        }
    }

    // Nightmare (Rare) - 3 energy, Skill
    //   Effect: Choose a card. Next turn, add 3 copies of that card into your hand. Exhaust.
    //   Upgraded Effect (2 energy): Choose a card. Next turn, add 3 copies of that card into your hand. Exhaust.
    public static class Nightmare extends _NightmareT {
        public Nightmare() {
            super("Nightmare", 3);
        }
    }

    public static class NightmareP extends _NightmareT {
        public NightmareP() {
            super("Nightmare+", 2);
        }
    }

    private static abstract class _PhantasmalKillerT extends Card {
        public _PhantasmalKillerT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += 1 << 8;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerNextTurnDoubleAttackCounter(state, this, "PhantasmalKiller",
                    (cIdx) -> state.properties.phantasmalKillerCounterIdx = cIdx);
        }
    }

    // Phantasmal Killer (Rare) - 1 energy, Skill
    //   Effect: Next turn, your Attacks deal double damage.
    //   Upgraded Effect (0 energy): Next turn, your Attacks deal double damage.
    public static class PhantasmalKiller extends _PhantasmalKillerT {
        public PhantasmalKiller() {
            super("Phantasmal Killer", 1);
        }
    }

    public static class PhantasmalKillerP extends _PhantasmalKillerT {
        public PhantasmalKillerP() {
            super("Phantasmal Killer+", 0);
        }
    }

    private static abstract class _StormOfSteelT extends Card {
        private final boolean upgraded;

        public _StormOfSteelT(String cardName, boolean upgraded) {
            super(cardName, Card.SKILL, 1, Card.RARE);
            this.upgraded = upgraded;
            this.canDiscardAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int c = state.discardHand(true);
            for (int i = 0; i < c; i++) {
                state.addCardToHand(upgraded ? generatedCardIdxes[1] : generatedCardIdxes[0], true);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Shiv(), new CardColorless.ShivP());
        }
    }

    // Storm of Steel (Rare) - 1 energy, Skill
    //   Effect: Discard your hand. Add 1 Shiv into your hand for each card discarded.
    //   Upgraded Effect: Discard your hand. Add 1 Shiv+ into your hand for each card discarded.
    public static class StormOfSteel extends _StormOfSteelT {
        public StormOfSteel() {
            super("Storm of Steel", false);
        }
    }

    public static class StormOfSteelP extends _StormOfSteelT {
        public StormOfSteelP() {
            super("Storm of Steel+", true);
        }
    }

    private static abstract class _ToolsOfTheTradeT extends Card {
        public _ToolsOfTheTradeT(String cardName, int energyCost) {
            super(cardName, Card.POWER, energyCost, Card.RARE);
            entityProperty.selectFromHand = true;
            this.selectFromHandLater = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.getCounterForWrite()[counterIdx]++;
                return GameActionCtx.PLAY_CARD;
            } else {
                state.discardCardFromHand(idx);
                state.getCounterForWrite()[counterIdx] -= 1 << 16;
                return state.getCounterForRead()[counterIdx] < (1 << 16) ? GameActionCtx.PLAY_CARD : GameActionCtx.SELECT_CARD_HAND;
            }
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("ToolsOfTheTrade", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] & 0xffff;
                    input[idx + 1] = state.getCounterForRead()[counterIdx] >> 16;
                    return idx + 2;
                }
                @Override public int getInputLenDelta() {
                    return 2;
                }
                @Override public void onRegister(int counterIdx) {
                    state.properties.toolsOfTheTradeCounterIdx = counterIdx;
                }
            });
        }
    }

    // Tools of the Trade (Rare) - 1 energy, Power
    //   Effect: At the start of your turn, draw 1 card and discard 1 card.
    //   Upgraded Effect (0 energy): At the start of your turn, draw 1 card and discard 1 card.
    public static class ToolsOfTheTrade extends _ToolsOfTheTradeT {
        public ToolsOfTheTrade() {
            super("Tools of the Trade", 1);
        }
    }

    public static class ToolsOfTheTradeP extends _ToolsOfTheTradeT {
        public ToolsOfTheTradeP() {
            super("Tools of the Trade+", 0);
        }
    }

    private static abstract class _UnloadT extends Card {
        private final int n;

        public _UnloadT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.RARE);
            entityProperty.selectEnemy = true;
            this.discardNonAttack = true;
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            for (int i = 0; i < state.handArrLen; i++) {
                if (state.properties.cardDict[state.getHandArrForRead()[i]].cardType != Card.ATTACK) {
                    state.discardCardFromHandByPosition(i, false);
                }
            }
            state.updateHandArr();
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Unload (Rare) - 1 energy, Attack
    //   Effect: Deal 14 damage. Discard all non-Attack cards in your hand.
    //   Upgraded Effect: Deal 18 damage. Discard all non-Attack cards in your hand.
    public static class Unload extends _UnloadT {
        public Unload() {
            super("Unload", 14);
        }
    }

    public static class UnloadP extends _UnloadT {
        public UnloadP() {
            super("Unload+", 18);
        }
    }

    private static abstract class _WraithFormT extends Card {
        private final int n;

        public _WraithFormT(String cardName, int n) {
            super(cardName, Card.POWER, 3, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[state.properties.intangibleCounterIdx] += n;
            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_DEXTERITY_PER_TURN, 1);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerIntangibleCounter();
            state.properties.registerCounter("WraithFormLoseDexterity", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 4.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addEndOfTurnHandler("WraithForm", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_DEXTERITY, state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.loseDexterityPerTurnCounterIdx = idx;
        }
    }

    // Wraith Form (Rare) - 3 energy, Power
    //   Effect: Gain 2 Intangible. At the end of your turn, lose 1 Dexterity.
    //   Upgraded Effect: Gain 3 Intangible. At the end of your turn, lose 1 Dexterity.
    public static class WraithForm extends _WraithFormT {
        public WraithForm() {
            super("Wraith Form", 2);
        }
    }

    public static class WraithFormP extends _WraithFormT {
        public WraithFormP() {
            super("Wraith Form+", 3);
        }
    }
}
