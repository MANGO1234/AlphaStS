package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyReadOnly;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventEnemyDebuffHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.random.RandomGenCtx;

import java.util.List;

public class CardSilent2 {
    // **************************************************************************************************
    // ********************************************* Basic  *********************************************
    // **************************************************************************************************

    public static class Defend extends Card.Defend {
    }

    public static class DefendP extends Card.DefendP {
    }

    public static class Neutralize extends CardSilent.Neutralize {
    }

    public static class NeutralizeP extends CardSilent.NeutralizeP {
    }

    public static class Strike extends Card.Strike {
    }

    public static class StrikeP extends Card.StrikeP {
    }

    public static class Survivor extends CardSilent.Survivor {
    }

    public static class SurvivorP extends CardSilent.SurvivorP {
    }

    // **************************************************************************************************
    // ********************************************* Common *********************************************
    // **************************************************************************************************

    public static class Acrobatics extends CardSilent.Acrobatics {
    }

    public static class AcrobaticsP extends CardSilent.AcrobaticsP {
    }

    private static abstract class _AnticipateT extends Card {
        private final int n;

        public _AnticipateT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.n = n;
            entityProperty.changePlayerDexterityEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainDexterity(n);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_DEXTERITY_EOT, n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Anticipate extends _AnticipateT {
        public Anticipate() {
            super("Anticipate", 3);
        }
    }

    public static class AnticipateP extends _AnticipateT {
        public AnticipateP() {
            super("Anticipate+", 5);
        }
    }

    public static class Backflip extends CardSilent.Backflip {
    }

    public static class BackflipP extends CardSilent.BackflipP {
    }

    public static class BladeDance extends CardSilent._BladeDanceT {
        public BladeDance() {
            super("Blade Dance", 3);
            this.exhaustWhenPlayed = true;
        }
    }

    public static class BladeDanceP extends CardSilent._BladeDanceT {
        public BladeDanceP() {
            super("Blade Dance+", 4);
            this.exhaustWhenPlayed = true;
        }
    }

    public static class CloakAndDagger extends CardSilent.CloakAndDagger {
    }

    public static class CloakAndDaggerP extends CardSilent.CloakAndDaggerP {
    }

    public static class DaggerSpray extends CardSilent.DaggerSpray {
    }

    public static class DaggerSprayP extends CardSilent.DaggerSprayP {
    }

    public static class DaggerThrow extends CardSilent.DaggerThrow {
    }

    public static class DaggerThrowP extends CardSilent.DaggerThrowP {
    }

    public static class DeadlyPoison extends CardSilent.DeadlyPoison {
    }

    public static class DeadlyPoisonP extends CardSilent.DeadlyPoisonP {
    }

    public static class Deflect extends CardSilent.Deflect {
    }

    public static class DeflectP extends CardSilent.DeflectP {
    }

    public static class DodgeAndRoll extends CardSilent.DodgeAndRoll {
    }

    public static class DodgeAndRollP extends CardSilent.DodgeAndRollP {
    }

    private static abstract class _FlickFlackT extends Card {
        private final int n;

        public _FlickFlackT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.n = n;
            entityProperty.sly = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FlickFlack extends _FlickFlackT {
        public FlickFlack() {
            super("Flick-Flack", 7);
        }
    }

    public static class FlickFlackP extends _FlickFlackT {
        public FlickFlackP() {
            super("Flick-Flack+", 9);
        }
    }

    private static abstract class _LeadingStrikeT extends Card {
        private final int n;

        public _LeadingStrikeT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.addCardToHand(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Shiv());
        }
    }

    public static class LeadingStrike extends _LeadingStrikeT {
        public LeadingStrike() {
            super("Leading Strike", 7);
        }
    }

    public static class LeadingStrikeP extends _LeadingStrikeT {
        public LeadingStrikeP() {
            super("Leading Strike+", 10);
        }
    }

    public static class PiercingWail extends CardSilent.PiercingWail {
    }

    public static class PiercingWailP extends CardSilent.PiercingWailP {
    }

    public static class PoisonedStab extends CardSilent.PoisonedStab {
    }

    public static class PoisonedStabP extends CardSilent.PoisonedStabP {
    }

    public static class Prepared extends CardSilent.Prepared {
    }

    public static class PreparedP extends CardSilent.PreparedP {
    }

    private static abstract class _RicochetT extends Card {
        private final int n;

        public _RicochetT(String cardName, int n) {
            super(cardName, Card.ATTACK, 2, Card.COMMON);
            this.n = n;
            entityProperty.sly = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < n; i++) {
                int enemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
                if (enemyIdx >= 0) {
                    state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(enemyIdx), 3);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Ricochet extends _RicochetT {
        public Ricochet() {
            super("Ricochet", 4);
        }
    }

    public static class RicochetP extends _RicochetT {
        public RicochetP() {
            super("Ricochet+", 5);
        }
    }

    public static class Slice extends CardSilent.Slice {
    }

    public static class SliceP extends CardSilent.SliceP {
    }

    private static abstract class _SnakebiteT extends Card {
        private final int n;

        public _SnakebiteT(String cardName, int n) {
            super(cardName, Card.SKILL, 2, Card.COMMON);
            this.n = n;
            this.retain = true;
            entityProperty.selectEnemy = true;
            entityProperty.poisonEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.POISON, n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Snakebite extends _SnakebiteT {
        public Snakebite() {
            super("Snakebite", 7);
        }
    }

    public static class SnakebiteP extends _SnakebiteT {
        public SnakebiteP() {
            super("Snakebite+", 10);
        }
    }

    public static class SuckerPunch extends CardSilent._SuckerPunchT {
        public SuckerPunch() {
            super("Sucker Punch", 8, 1);
        }
    }

    public static class SuckerPunchP extends CardSilent._SuckerPunchT {
        public SuckerPunchP() {
            super("Sucker Punch+", 10, 2);
        }
    }

    private static abstract class _UntouchableT extends Card {
        private final int n;

        public _UntouchableT(String cardName, int n) {
            super(cardName, Card.SKILL, 2, Card.COMMON);
            this.n = n;
            entityProperty.sly = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Untouchable extends _UntouchableT {
        public Untouchable() {
            super("Untouchable", 9);
        }
    }

    public static class UntouchableP extends _UntouchableT {
        public UntouchableP() {
            super("Untouchable+", 12);
        }
    }

    // **************************************************************************************************
    // ********************************************* Uncommon *******************************************
    // **************************************************************************************************

    public static class Accuracy extends CardSilent.Accuracy {
    }

    public static class AccuracyP extends CardSilent.AccuracyP {
    }

    public static class Backstab extends CardSilent.Backstab {
    }

    public static class BackstabP extends CardSilent.BackstabP {
    }

    public static class Blur extends CardSilent.Blur {
    }

    public static class BlurP extends CardSilent.BlurP {
    }

    public static class BouncingFlask extends CardSilent.BouncingFlask {
    }

    public static class BouncingFlaskP extends CardSilent.BouncingFlaskP {
    }

    private static abstract class _BubbleBubbleT extends Card {
        private final int n;

        public _BubbleBubbleT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
            entityProperty.poisonEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.getEnemiesForRead().get(idx).getPoison() > 0) {
                state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.POISON, n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BubbleBubble extends _BubbleBubbleT {
        public BubbleBubble() {
            super("Bubble Bubble", 9);
        }
    }

    public static class BubbleBubbleP extends _BubbleBubbleT {
        public BubbleBubbleP() {
            super("Bubble Bubble+", 12);
        }
    }

    public static class CalculatedGamble extends CardSilent.CalculatedGamble {
    }

    public static class CalculatedGambleP extends CardSilent._CalculatedGambleT {
        public CalculatedGambleP() {
            super("Calculated Gamble+", true);
            this.retain = true;
        }
    }

    public static class Dash extends CardSilent.Dash {
    }

    public static class DashP extends CardSilent.DashP {
    }

    public static class EscapePlan extends CardSilent.EscapePlan {
    }

    public static class EscapePlanP extends CardSilent.EscapePlanP {
    }

    public static class Expertise extends CardSilent.Expertise {
    }

    public static class ExpertiseP extends CardSilent.ExpertiseP {
    }

    private static abstract class _ExposeT extends Card {
        private final int n;

        public _ExposeT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.n = n;
            this.exhaustWhenPlayed = true;
            entityProperty.selectEnemy = true;
            entityProperty.vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            enemy.setBlock(0);
            enemy.setArtifact(0);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Expose extends _ExposeT {
        public Expose() {
            super("Expose", 2);
        }
    }

    public static class ExposeP extends _ExposeT {
        public ExposeP() {
            super("Expose+", 3);
        }
    }

    public static class Finisher extends CardSilent.Finisher {
    }

    public static class FinisherP extends CardSilent.FinisherP {
    }

    // No need to implement Flanking: Multiplayer

    public static class Flechettes extends CardSilent._FlechetteT {
        public Flechettes() {
            super("Flechettes", 5);
        }
    }

    public static class FlechettesP extends CardSilent._FlechetteT {
        public FlechettesP() {
            super("Flechettes+", 7);
        }
    }

    private static abstract class _FollowThroughT extends Card {
        private final int dmg;
        private final int weak;

        public _FollowThroughT(String cardName, int dmg, int weak) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.dmg = dmg;
            this.weak = weak;
            this.needsLastCardType = true;
            entityProperty.weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, dmg);
            }
            if (state.getLastCardPlayedType() == Card.SKILL) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemy.applyDebuff(state, DebuffType.WEAK, weak);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FollowThrough extends _FollowThroughT {
        public FollowThrough() {
            super("Follow Through", 6, 1);
        }
    }

    public static class FollowThroughP extends _FollowThroughT {
        public FollowThroughP() {
            super("Follow Through+", 8, 2);
        }
    }

    public static class Footwork extends CardSilent.Footwork {
    }

    public static class FootworkP extends CardSilent.FootworkP {
    }

    // TODO: Hand Trick (Uncommon) - 1 energy, Skill
    //   Effect: Gain 7 Block. Add Sly to a Skill in your Hand this turn.
    //   Upgraded Effect: Gain 10 Block. Add Sly to a Skill in your Hand this turn.

    private static abstract class _HazeT extends Card {
        private final int n;

        public _HazeT(String cardName, int n) {
            super(cardName, Card.SKILL, 3, Card.UNCOMMON);
            this.n = n;
            entityProperty.sly = true;
            entityProperty.poisonEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.POISON, n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Haze extends _HazeT {
        public Haze() {
            super("Haze", 4);
        }
    }

    public static class HazeP extends _HazeT {
        public HazeP() {
            super("Haze+", 6);
        }
    }

    private static abstract class _HiddenDaggersT extends Card {
        public _HiddenDaggersT(String cardName) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            entityProperty.selectFromHand = true;
            this.canDiscardAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.discardCardFromHand(idx);
            state.getCounterForWrite()[counterIdx]++;
            if (state.getCounterForRead()[counterIdx] < 2) {
                return GameActionCtx.SELECT_CARD_HAND;
            }
            state.getCounterForWrite()[counterIdx] = 0;
            state.addCardToHand(generatedCardIdx);
            state.addCardToHand(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("HiddenDaggers", this, new GameProperties.NetworkInputHandler() {
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

    public static class HiddenDaggers extends _HiddenDaggersT {
        public HiddenDaggers() {
            super("Hidden Daggers");
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Shiv());
        }
    }

    public static class HiddenDaggersP extends _HiddenDaggersT {
        public HiddenDaggersP() {
            super("Hidden Daggers+");
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.ShivP());
        }
    }

    public static class InfiniteBlades extends CardSilent.InfiniteBlade {
    }

    public static class InfiniteBladeP extends CardSilent.InfiniteBladeP {
    }

    public static class LegSweep extends CardSilent.LegSweep {
    }

    public static class LegSweepP extends CardSilent.LegSweepP {
    }

    private static abstract class _MementoMoriT extends Card {
        private final int baseDmg;
        private final int bonusPerDiscard;

        public _MementoMoriT(String cardName, int baseDmg, int bonusPerDiscard) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.baseDmg = baseDmg;
            this.bonusPerDiscard = bonusPerDiscard;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int discarded = state.getCounterForRead()[state.properties.cardsDiscardedThisTurnCounterIdx];
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), baseDmg + bonusPerDiscard * discarded);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("MementoMoriCardsDiscarded", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int counterIdx) {
                    state.properties.cardsDiscardedThisTurnCounterIdx = counterIdx;
                }
            });
            state.properties.addStartOfTurnHandler("MementoMoriCardsDiscarded", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[state.properties.cardsDiscardedThisTurnCounterIdx] = 0;
                }
            });
        }
    }

    public static class MementoMori extends _MementoMoriT {
        public MementoMori() {
            super("Memento Mori", 8, 4);
        }
    }

    public static class MementoMoriP extends _MementoMoriT {
        public MementoMoriP() {
            super("Memento Mori+", 10, 5);
        }
    }

    private static abstract class _MirageT extends Card {
        public _MirageT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.UNCOMMON);
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int totalPoison = 0;
            for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                var e = state.getEnemiesForRead().get(i);
                if (e.isAlive()) {
                    totalPoison += e.getPoison();
                }
            }
            state.getPlayerForWrite().gainBlock(totalPoison);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Mirage extends _MirageT {
        public Mirage() {
            super("Mirage", 1);
        }
    }

    public static class MirageP extends _MirageT {
        public MirageP() {
            super("Mirage+", 0);
        }
    }

    public static class NoxiousFumes extends CardSilent.NoxiousFume {
    }

    public static class NoxiousFumesP extends CardSilent.NoxiousFumeP {
    }

    private static abstract class _OutbreakT extends Card {
        private final int n;

        public _OutbreakT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] = 0;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Outbreak", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnEnemyDebuffHandler("Outbreak", new GameEventEnemyDebuffHandler() {
                @Override public void handle(GameState state, EnemyReadOnly enemy, DebuffType type, int amount) {
                    if (type == DebuffType.POISON) {
                        state.getCounterForWrite()[counterIdx]++;
                        if (state.getCounterForRead()[counterIdx] % 3 == 0) {
                            for (Enemy e : state.getEnemiesForWrite().iterateOverAlive()) {
                                state.playerDoNonAttackDamageToEnemy(e, n, true);
                            }
                        }
                    }
                }
            });
        }
    }

    public static class Outbreak extends _OutbreakT {
        public Outbreak() {
            super("Outbreak", 11);
        }
    }

    public static class OutbreakP extends _OutbreakT {
        public OutbreakP() {
            super("Outbreak+", 15);
        }
    }

    // TODO: Phantom Blades (Uncommon) - 1 energy, Power
    //   Effect: Shivs gain Retain. The first Shiv you play each turn deals 9 additional damage.
    //   Upgraded Effect: Shivs gain Retain. The first Shiv you play each turn deals 12 additional damage.

    private static abstract class _PinpointT extends Card {
        private final int n;

        public _PinpointT(String cardName, int n) {
            super(cardName, Card.ATTACK, 3, Card.UNCOMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public int energyCost(GameState state) {
            if (state == null) return 3;
            return Math.max(0, 3 - state.getCounterForRead()[state.properties.skillsPlayedThisTurnCounterIdx]);
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerSkillsPlayedThisTurnCounter();
        }
    }

    public static class Pinpoint extends _PinpointT {
        public Pinpoint() {
            super("Pinpoint", 17);
        }
    }

    public static class PinpointP extends _PinpointT {
        public PinpointP() {
            super("Pinpoint+", 22);
        }
    }

    private static abstract class _PounceT extends Card {
        private final int n;

        public _PounceT(String cardName, int n) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Pounce", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("Pounce", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.SKILL && state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx]--;
                        state.gainEnergy(energyUsed);
                    }
                }
            });
        }
    }

    public static class Pounce extends _PounceT {
        public Pounce() {
            super("Pounce", 12);
        }
    }

    public static class PounceP extends _PounceT {
        public PounceP() {
            super("Pounce+", 18);
        }
    }

    private static abstract class _PreciseCutT extends Card {
        private final int n;

        public _PreciseCutT(String cardName, int n) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int dmg = Math.max(0, n - 2 * state.handArrLen);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PreciseCut extends _PreciseCutT {
        public PreciseCut() {
            super("Precise Cut", 13);
        }
    }

    public static class PreciseCutP extends _PreciseCutT {
        public PreciseCutP() {
            super("Precise Cut+", 16);
        }
    }

    public static class Predator extends CardSilent.Predator {
    }

    public static class PredatorP extends CardSilent.PredatorP {
    }

    private static abstract class _ReflexT extends Card {
        private final int n;

        public _ReflexT(String cardName, int n) {
            super(cardName, Card.SKILL, 3, Card.UNCOMMON);
            this.n = n;
            entityProperty.sly = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.draw(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

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

    public static class Skewer extends CardSilent.Skewer {
    }

    public static class SkewerP extends CardSilent.SkewerP {
    }

    private static abstract class _SpeedsterT extends Card {
        private final int n;

        public _SpeedsterT(String cardName, int n) {
            super(cardName, Card.POWER, 2, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Speedster", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.registerIsPlayerTurnCounter();
            state.properties.addOnCardDrawnHandler("Speedster", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int stacks = state.getCounterForRead()[counterIdx];
                    if (stacks > 0 && state.getCounterForRead()[state.properties.isPlayerTurnCounterIdx] > 0) {
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            state.playerDoNonAttackDamageToEnemy(enemy, stacks * n, true);
                        }
                    }
                }
            });
        }
    }

    public static class Speedster extends _SpeedsterT {
        public Speedster() {
            super("Speedster", 2);
        }
    }

    public static class SpeedsterP extends _SpeedsterT {
        public SpeedsterP() {
            super("Speedster+", 3);
        }
    }

    private static abstract class _StrangleT extends Card {
        private final int dmg;
        private final int choke;

        public _StrangleT(String cardName, int dmg, int choke) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.dmg = dmg;
            this.choke = choke;
            entityProperty.selectEnemy = true;
            entityProperty.chokeEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, dmg);
            enemy.applyDebuff(state, DebuffType.CHOKE, choke);
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

    public static class Strangle extends _StrangleT {
        public Strangle() {
            super("Strangle", 8, 2);
        }
    }

    public static class StrangleP extends _StrangleT {
        public StrangleP() {
            super("Strangle+", 10, 3);
        }
    }

    private static abstract class _TacticianT extends Card {
        private final int n;

        public _TacticianT(String cardName, int n) {
            super(cardName, Card.SKILL, 3, Card.UNCOMMON);
            this.n = n;
            entityProperty.sly = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

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

    public static class UpMySleeve extends Card {
        public UpMySleeve(int energyCost) {
            super("Up My Sleeve (" + energyCost + ")", Card.SKILL, energyCost, Card.UNCOMMON);
        }

        public UpMySleeve() {
            this(2);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < 3; i++) {
                state.addCardToHand(generatedCardIdx);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Shiv(), new UpMySleeve(2), new UpMySleeve(1), new UpMySleeve(0));
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.upMySleeveIndexes = new int[3];
            for (int i = 0; i < 3; i++) {
                state.properties.upMySleeveIndexes[i] = state.properties.findCardIndex(new UpMySleeve(i));
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop, int cardIdx) {
            return energyCost > 0 ? prop.upMySleeveIndexes[energyCost - 1] : -1;
        }

        @Override public Card getPermCostIfPossible(int permCost) {
            return permCost <= 3 ? new UpMySleeve(permCost) : super.getPermCostIfPossible(permCost);
        }
    }

    public static class UpMySleeveP extends Card {
        public UpMySleeveP(int energyCost) {
            super("Up My Sleeve+ (" + energyCost + ")", Card.SKILL, energyCost, Card.UNCOMMON);
        }

        public UpMySleeveP() {
            this(2);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < 4; i++) {
                state.addCardToHand(generatedCardIdx);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Shiv(), new UpMySleeveP(2), new UpMySleeveP(1), new UpMySleeveP(0));
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.upMySleeveIndexesP = new int[3];
            for (int i = 0; i < 3; i++) {
                state.properties.upMySleeveIndexesP[i] = state.properties.findCardIndex(new UpMySleeveP(i));
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop, int cardIdx) {
            return energyCost > 0 ? prop.upMySleeveIndexesP[energyCost - 1] : -1;
        }

        @Override public Card getPermCostIfPossible(int permCost) {
            return permCost <= 3 ? new UpMySleeveP(permCost) : super.getPermCostIfPossible(permCost);
        }
    }

    public static class WellLaidPlans extends CardSilent.WellLaidPlans {
    }

    public static class WellLaidPlansP extends CardSilent.WellLaidPlansP {
    }

    // **************************************************************************************************
    // *********************************************  Rare  *********************************************
    // **************************************************************************************************

    private static abstract class _AbrasiveT extends Card {
        private final int thorns;

        public _AbrasiveT(String cardName, int thorns) {
            super(cardName, Card.POWER, 3, Card.RARE);
            this.thorns = thorns;
            entityProperty.sly = true;
            entityProperty.changePlayerDexterity = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainDexterity(1);
            state.getCounterForWrite()[counterIdx] += thorns;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerThornCounter(state, this);
        }
    }

    public static class Abrasive extends _AbrasiveT {
        public Abrasive() {
            super("Abrasive", 4);
        }
    }

    public static class AbrasiveP extends _AbrasiveT {
        public AbrasiveP() {
            super("Abrasive+", 6);
        }
    }

    private static abstract class _AccelerantT extends Card {
        private final int n;

        public _AccelerantT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Accelerant", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int cIdx) {
                    state.properties.accelerantCounterIdx = cIdx;
                }
            });
        }
    }

    public static class Accelerant extends _AccelerantT {
        public Accelerant() {
            super("Accelerant", 1);
        }
    }

    public static class AccelerantP extends _AccelerantT {
        public AccelerantP() {
            super("Accelerant+", 2);
        }
    }

    public static class Adrenaline extends CardSilent.Adrenaline {
    }

    public static class AdrenalineP extends CardSilent.AdrenalineP {
    }

    public static class AfterImage extends CardSilent.AfterImage {
    }

    public static class AfterImageP extends CardSilent.AfterImageP {
    }

    private static abstract class _AssassinateT extends Card {
        private final int dmg;
        private final int vul;

        public _AssassinateT(String cardName, int dmg, int vul) {
            super(cardName, Card.ATTACK, 0, Card.RARE);
            this.dmg = dmg;
            this.vul = vul;
            this.innate = true;
            this.exhaustWhenPlayed = true;
            entityProperty.selectEnemy = true;
            entityProperty.vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, dmg);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, vul);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Assassinate extends _AssassinateT {
        public Assassinate() {
            super("Assassinate", 10, 1);
        }
    }

    public static class AssassinateP extends _AssassinateT {
        public AssassinateP() {
            super("Assassinate+", 13, 2);
        }
    }

    private static abstract class _BladeOfInkT extends Card {
        private final int strength;

        public _BladeOfInkT(String cardName, int strength) {
            super(cardName, Card.SKILL, 1, Card.RARE);
            this.strength = strength;
            entityProperty.changePlayerStrength = true;
            entityProperty.changePlayerStrengthEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += strength;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("BladeOfInk", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("BladeOfInk", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK &&
                            state.getCounterForRead()[counterIdx] > 0) {
                        int str = state.getCounterForRead()[counterIdx];
                        state.getPlayerForWrite().gainStrength(str);
                        state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, str);
                    }
                }
            });
        }
    }

    public static class BladeOfInk extends _BladeOfInkT {
        public BladeOfInk() {
            super("Blade of Ink", 2);
        }
    }

    public static class BladeOfInkP extends _BladeOfInkT {
        public BladeOfInkP() {
            super("Blade of Ink+", 3);
        }
    }

    public static class BulletTime extends CardSilent.BulletTime {
    }

    public static class BulletTimeP extends CardSilent.BulletTimeP {
    }

    public static class Burst extends CardSilent.Burst {
    }

    public static class BurstP extends CardSilent.BurstP {
    }

    private static abstract class _CorrosiveWaveT extends Card {
        private final int poison;

        public _CorrosiveWaveT(String cardName, int poison) {
            super(cardName, Card.SKILL, 1, Card.RARE);
            this.poison = poison;
            entityProperty.poisonEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += poison;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("CorrosiveWave", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int cIdx) {
                    state.properties.corrosiveWaveCounterIdx = cIdx;
                }
            });
            state.properties.addOnCardDrawnHandler("CorrosiveWave", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        int poisonAmt = state.getCounterForRead()[counterIdx];
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            enemy.applyDebuff(state, DebuffType.POISON, poisonAmt);
                        }
                    }
                }
            });
            state.properties.addEndOfTurnHandler("CorrosiveWave", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    public static class CorrosiveWave extends _CorrosiveWaveT {
        public CorrosiveWave() {
            super("Corrosive Wave", 3);
        }
    }

    public static class CorrosiveWaveP extends _CorrosiveWaveT {
        public CorrosiveWaveP() {
            super("Corrosive Wave+", 4);
        }
    }

    private static abstract class _EchoingSlashT extends Card {
        private final int dmg;

        public _EchoingSlashT(String cardName, int dmg) {
            super(cardName, Card.ATTACK, 1, Card.RARE);
            this.dmg = dmg;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int numKills;
            do {
                numKills = 0;
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    int prevHp = enemy.getHealth();
                    state.playerDoDamageToEnemy(enemy, dmg);
                    if (prevHp > 0 && !enemy.isAlive()) {
                        numKills++;
                    }
                }
            } while (numKills > 0);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class EchoingSlash extends _EchoingSlashT {
        public EchoingSlash() {
            super("Echoing Slash", 10);
        }
    }

    public static class EchoingSlashP extends _EchoingSlashT {
        public EchoingSlashP() {
            super("Echoing Slash+", 13);
        }
    }

    public static class Envenom extends CardSilent.Envenom {
    }

    public static class EnvenomP extends CardSilent._EnvenomT {
        public EnvenomP() {
            super("Envenom+", 2, 2);
        }
    }

    private static abstract class _FanOfKnivesT extends Card {
        private final int n;

        public _FanOfKnivesT(String cardName, int n) {
            super(cardName, Card.POWER, 2, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += 1;
            for (int i = 0; i < n; i++) {
                state.addCardToHand(generatedCardIdx);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("FanOfKnives", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int cIdx) {
                    state.properties.fanOfKnivesCounterIdx = cIdx;
                }
            });
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Shiv());
        }
    }

    public static class FanOfKnives extends _FanOfKnivesT {
        public FanOfKnives() {
            super("Fan of Knives", 4);
        }
    }

    public static class FanOfKnivesP extends _FanOfKnivesT {
        public FanOfKnivesP() {
            super("Fan of Knives+", 5);
        }
    }

    public static class GrandFinale extends CardSilent.GrandFinale {
    }

    public static class GrandFinaleP extends CardSilent.GrandFinaleP {
    }

    private static abstract class _KnifeTrapT extends Card {
        private final boolean upgraded;

        public _KnifeTrapT(String cardName, boolean upgraded) {
            super(cardName, Card.SKILL, 2, Card.RARE);
            this.upgraded = upgraded;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (upgraded && state.properties.shivUpgradeIdxes != null) {
                state.exhaustArrTransform(state.properties.shivUpgradeIdxes);
            }
            int len = state.exhaustArrLen;
            int[] shivCardIdxes = new int[len];
            int shivCount = 0;
            for (int i = 0; i < len; i++) {
                int cardIdx = state.exhaustArr[i];
                var base = state.properties.cardDict[cardIdx].getBaseCard();
                if (base instanceof CardColorless.Shiv || base instanceof CardColorless.ShivP) {
                    shivCardIdxes[shivCount++] = cardIdx;
                }
            }
            for (int i = 0; i < shivCount; i++) {
                state.removeCardFromExhaust(shivCardIdxes[i]);
                var action = state.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][shivCardIdxes[i]];
                if (action != null) {
                    state.playCard(action, -1, true, null, false, true, -1, -1);
                    while (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                        state.playCard(action, idx, true, null, false, true, -1, -1);
                    }
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            if (upgraded) {
                int[] arr = new int[state.properties.cardDict.length];
                for (int i = 0; i < arr.length; i++) arr[i] = -1;
                int shivIdx = state.properties.findCardIndex(new CardColorless.Shiv());
                int shivPIdx = state.properties.findCardIndex(new CardColorless.ShivP());
                if (shivIdx >= 0 && shivPIdx >= 0) {
                    arr[shivIdx] = shivPIdx;
                }
                state.properties.shivUpgradeIdxes = arr;
            }
        }
    }

    public static class KnifeTrap extends _KnifeTrapT {
        public KnifeTrap() {
            super("Knife Trap", false);
        }
    }

    public static class KnifeTrapP extends _KnifeTrapT {
        public KnifeTrapP() {
            super("Knife Trap+", true);
        }
    }

    public static class Malaise extends CardSilent.Malaise {
    }

    public static class MalaiseP extends CardSilent.MalaiseP {
    }

    // TODO: Master Planner (Rare) - 2 energy, Power
    //   Effect: When you play a Skill, it gains Sly.
    //   Upgraded Effect (1 energy): When you play a Skill, it gains Sly.

    private static abstract class _MurderT extends Card {
        public _MurderT(String cardName, int cost) {
            super(cardName, Card.ATTACK, cost, Card.RARE);
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 1 + state.getCounterForRead()[counterIdx]);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("CardsDrawnThisCombat", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 50.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int cIdx) {
                    state.properties.cardsDrawnThisCombatCounterIdx = cIdx;
                }
            });
            state.properties.addOnCardDrawnHandler("CardsDrawnThisCombat", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    state.getCounterForWrite()[counterIdx]++;
                }
            });
        }
    }

    public static class Murder extends _MurderT {
        public Murder() {
            super("Murder", 3);
        }
    }

    public static class MurderP extends _MurderT {
        public MurderP() {
            super("Murder+", 2);
        }
    }

    public static class Nightmare extends CardSilent.Nightmare {
    }

    public static class NightmareP extends CardSilent.NightmareP {
    }

    private static abstract class _SerpentFormT extends Card {
        private final int n;

        public _SerpentFormT(String cardName, int n) {
            super(cardName, Card.POWER, 3, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("SerpentForm", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("SerpentForm", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        if (state.properties.cardDict[cardIdx].cardName.startsWith("Serpent Form")) {
                            return;
                        }
                        var enemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
                        if (enemyIdx >= 0) {
                            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(enemyIdx), state.getCounterForRead()[counterIdx], true);
                        }
                    }
                }
            });
        }
    }

    public static class SerpentForm extends _SerpentFormT {
        public SerpentForm() {
            super("Serpent Form", 4);
        }
    }

    public static class SerpentFormP extends _SerpentFormT {
        public SerpentFormP() {
            super("Serpent Form+", 5);
        }
    }

    private static abstract class _ShadowStepT extends Card {
        public _ShadowStepT(String cardName, int cost) {
            super(cardName, Card.SKILL, cost, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.discardHand(true);
            state.getCounterForWrite()[counterIdx] += 1 << 8;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerNextTurnDoubleAttackCounter(state, this, "ShadowStep",
                    (cIdx) -> state.properties.shadowStepCounterIdx = cIdx);
        }
    }

    public static class ShadowStep extends _ShadowStepT {
        public ShadowStep() {
            super("Shadow Step", 1);
        }
    }

    public static class ShadowStepP extends _ShadowStepT {
        public ShadowStepP() {
            super("Shadow Step+", 0);
        }
    }

    private static abstract class _ShadowmeldT extends Card {
        public _ShadowmeldT(String cardName, int cost) {
            super(cardName, Card.SKILL, cost, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Shadowmeld", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int cIdx) {
                    state.properties.shadowmeldCounterIdx = cIdx;
                }
            });
        }
    }

    public static class Shadowmeld extends _ShadowmeldT {
        public Shadowmeld() {
            super("Shadowmeld", 1);
        }
    }

    public static class ShadowmeldP extends _ShadowmeldT {
        public ShadowmeldP() {
            super("Shadowmeld+", 0);
        }
    }

    // No need to implement Sneaky: Multiplayer

    public static class StormOfSteel extends CardSilent.StormOfSteel {
    }

    public static class StormOfSteelP extends CardSilent.StormOfSteelP {
    }

    private static abstract class _TheHuntT extends Card {
        private final int dmg;
        protected double healthRewardRatio;

        public _TheHuntT(String cardName, int dmg, double healthRewardRatio) {
            super(cardName, Card.ATTACK, 1, Card.RARE);
            this.dmg = dmg;
            this.healthRewardRatio = healthRewardRatio;
            entityProperty.selectEnemy = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            int prevHp = enemy.getHealth();
            state.playerDoDamageToEnemy(enemy, dmg);
            if (prevHp > 0 && !enemy.isAlive() && !enemy.properties.isMinion) {
                state.getCounterForWrite()[counterIdx]++;
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("TheHunt", this, healthRewardRatio == 0 ? null : new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            if (healthRewardRatio > 0) {
                state.properties.addExtraTrainingTarget("TheHunt", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                        if (isTerminal > 0) {
                            v.setVExtra(vExtraIdx, state.getCounterForRead()[counterIdx] / 10.0);
                        }
                    }

                    @Override public void updateQValues(GameState state, VArray v) {
                        v.add(GameState.V_HEALTH_IDX, 10 * v.getVExtra(vExtraIdx) * healthRewardRatio / state.getPlayeForRead().getMaxHealth());
                    }
                });
            }
        }
    }

    public static class TheHunt extends _TheHuntT {
        public TheHunt(double healthRewardRatio) {
            super("The Hunt", 10, healthRewardRatio);
        }
    }

    public static class TheHuntP extends _TheHuntT {
        public TheHuntP(double healthRewardRatio) {
            super("The Hunt+", 15, healthRewardRatio);
        }
    }

    public static class ToolsOfTheTrade extends CardSilent.ToolsOfTheTrade {
    }

    public static class ToolsOfTheTradeP extends CardSilent.ToolsOfTheTradeP {
    }

    private static abstract class _TrackingT extends Card {
        public _TrackingT(String cardName, int cost) {
            super(cardName, Card.POWER, cost, Card.RARE);
            entityProperty.weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Tracking", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int cIdx) {
                    state.properties.trackingCounterIdx = cIdx;
                }
            });
        }
    }

    public static class Tracking extends _TrackingT {
        public Tracking() {
            super("Tracking", 2);
        }
    }

    public static class TrackingP extends _TrackingT {
        public TrackingP() {
            super("Tracking+", 1);
        }
    }

    // **************************************************************************************************
    // ********************************************* Event  *********************************************
    // **************************************************************************************************

    public static class Caltrops extends CardSilent.Caltrops {
    }

    public static class CaltropsP extends CardSilent.CaltropsP {
    }

    public static class Distraction extends CardSilent.Distraction {
    }

    public static class DistractionP extends CardSilent.DistractionP {
    }

    public static class Outmaneuver extends CardSilent.Outmaneuver {
    }

    public static class OutmaneuverP extends CardSilent.OutmaneuverP {
    }

    // **************************************************************************************************
    // ********************************************* Ancient *********************************************
    // **************************************************************************************************

    private static abstract class _SuppressT extends Card {
        private final int dmg;
        private final int weak;

        public _SuppressT(String cardName, int dmg, int weak) {
            super(cardName, Card.ATTACK, 0, Card.RARE);
            this.dmg = dmg;
            this.weak = weak;
            this.innate = true;
            entityProperty.selectEnemy = true;
            entityProperty.weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, dmg);
            enemy.applyDebuff(state, DebuffType.WEAK, weak);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Suppress extends _SuppressT {
        public Suppress() {
            super("Suppress", 11, 3);
        }
    }

    public static class SuppressP extends _SuppressT {
        public SuppressP() {
            super("Suppress+", 17, 5);
        }
    }

    public static class WraithForm extends CardSilent.WraithForm {
    }

    public static class WraithFormP extends CardSilent.WraithFormP {
    }
}
