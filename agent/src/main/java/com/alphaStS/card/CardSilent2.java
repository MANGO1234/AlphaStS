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

    // Defend (Silent) (Basic) - 1 energy, Skill
    //   Effect: Gain 5 Block.
    //   Upgraded Effect: Gain 8 Block.
    public static class Defend extends Card.Defend {
    }

    public static class DefendP extends Card.DefendP {
    }

    // Neutralize (Basic) - 0 energy, Attack
    //   Effect: Deal 3 damage. Apply 1 Weak.
    //   Upgraded Effect: Deal 4 damage. Apply 2 Weak.
    public static class Neutralize extends CardSilent.Neutralize {
    }

    public static class NeutralizeP extends CardSilent.NeutralizeP {
    }

    // Strike (Silent) (Basic) - 1 energy, Attack
    //   Effect: Deal 6 damage.
    //   Upgraded Effect: Deal 9 damage.
    public static class Strike extends Card.Strike {
    }

    public static class StrikeP extends Card.StrikeP {
    }

    // Survivor (Basic) - 1 energy, Skill
    //   Effect: Gain 8 Block. Discard 1 card.
    //   Upgraded Effect: Gain 11 Block. Discard 1 card.
    public static class Survivor extends CardSilent.Survivor {
    }

    public static class SurvivorP extends CardSilent.SurvivorP {
    }

    // **************************************************************************************************
    // ********************************************* Common *********************************************
    // **************************************************************************************************

    // Acrobatics (Common) - 1 energy, Skill
    //   Effect: Draw 3 cards. Discard 1 card.
    //   Upgraded Effect: Draw 4 cards. Discard 1 card.
    // TODO CHANGED: Acrobatics (Uncommon) - 1 energy, Skill
    //   Effect: Draw 3 cards. Discard 1 card.
    //   Upgraded Effect: Draw 4 cards. Discard 1 card.
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

    // Anticipate (Common) - 0 energy, Skill
    //   Effect: Gain 3 Dexterity this turn.
    //   Upgraded Effect: Gain 5 Dexterity this turn.
    // TODO CHANGED: Anticipate (Common) - 0 energy, Skill
    //   Effect: Gain 2 Dexterity this turn.
    //   Upgraded Effect: Gain 3 Dexterity this turn.
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

    // Backflip (Common) - 1 energy, Skill
    //   Effect: Gain 5 Block. Draw 2 cards.
    //   Upgraded Effect: Gain 8 Block. Draw 2 cards.
    public static class Backflip extends CardSilent.Backflip {
    }

    public static class BackflipP extends CardSilent.BackflipP {
    }

    // Blade Dance (Common) - 1 energy, Skill
    //   Effect: Add 3 Shivs into your Hand. Exhaust.
    //   Upgraded Effect: Add 4 Shivs into your Hand. Exhaust.
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

    // Cloak and Dagger (Common) - 1 energy, Skill
    //   Effect: Gain 6 Block. Add 1 Shiv into your Hand.
    //   Upgraded Effect: Gain 6 Block. Add 2 Shivs into your Hand.
    public static class CloakAndDagger extends CardSilent.CloakAndDagger {
    }

    public static class CloakAndDaggerP extends CardSilent.CloakAndDaggerP {
    }

    // Dagger Spray (Common) - 1 energy, Attack
    //   Effect: Deal 4 damage to ALL enemies twice.
    //   Upgraded Effect: Deal 6 damage to ALL enemies twice.
    public static class DaggerSpray extends CardSilent.DaggerSpray {
    }

    public static class DaggerSprayP extends CardSilent.DaggerSprayP {
    }

    // Dagger Throw (Common) - 1 energy, Attack
    //   Effect: Deal 9 damage. Draw 1 card. Discard 1 card.
    //   Upgraded Effect: Deal 12 damage. Draw 1 card. Discard 1 card.
    public static class DaggerThrow extends CardSilent.DaggerThrow {
    }

    public static class DaggerThrowP extends CardSilent.DaggerThrowP {
    }

    // Deadly Poison (Common) - 1 energy, Skill
    //   Effect: Apply 5 Poison.
    //   Upgraded Effect: Apply 7 Poison.
    public static class DeadlyPoison extends CardSilent.DeadlyPoison {
    }

    public static class DeadlyPoisonP extends CardSilent.DeadlyPoisonP {
    }

    // Deflect (Common) - 0 energy, Skill
    //   Effect: Gain 4 Block.
    //   Upgraded Effect: Gain 7 Block.
    public static class Deflect extends CardSilent.Deflect {
    }

    public static class DeflectP extends CardSilent.DeflectP {
    }

    // Dodge and Roll (Common) - 1 energy, Skill
    //   Effect: Gain 4 Block. Next turn, gain 4 Block.
    //   Upgraded Effect: Gain 6 Block. Next turn, gain 6 Block.
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
                state.playerDoDamageToEnemy(enemy, n, this);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Flick-Flack (Common) - 1 energy, Attack
    //   Effect: Sly. Deal 7 damage to ALL enemies.
    //   Upgraded Effect: Sly. Deal 9 damage to ALL enemies.
    // TODO CHANGED: Flick-Flack (Common) - 1 energy, Attack
    //   Effect: Sly. Deal 6 damage to ALL enemies.
    //   Upgraded Effect: Sly. Deal 8 damage to ALL enemies.
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            state.addCardToHand(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Shiv());
        }
    }

    // Leading Strike (Common) - 1 energy, Attack
    //   Effect: Deal 7 damage. Add 1 Shiv into your Hand.
    //   Upgraded Effect: Deal 10 damage. Add 1 Shiv into your Hand.
    // TODO CHANGED: Leading Strike (Common) - 1 energy, Attack
    //   Effect: Deal 3 damage. Add 2 Shivs into your Hand.
    //   Upgraded Effect: Deal 6 damage. Add 2 Shivs into your Hand.
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

    // Piercing Wail (Common) - 1 energy, Skill
    //   Effect: ALL enemies lose 6 Strength this turn. Exhaust.
    //   Upgraded Effect: ALL enemies lose 8 Strength this turn. Exhaust.
    public static class PiercingWail extends CardSilent.PiercingWail {
    }

    public static class PiercingWailP extends CardSilent.PiercingWailP {
    }

    // Poisoned Stab (Common) - 1 energy, Attack
    //   Effect: Deal 6 damage. Apply 3 Poison.
    //   Upgraded Effect: Deal 8 damage. Apply 4 Poison.
    public static class PoisonedStab extends CardSilent.PoisonedStab {
    }

    public static class PoisonedStabP extends CardSilent.PoisonedStabP {
    }

    // Prepared (Common) - 0 energy, Skill
    //   Effect: Draw 1 card. Discard 1 card.
    //   Upgraded Effect: Draw 2 cards. Discard 2 cards.
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
                    state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(enemyIdx), 3, this);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Ricochet (Common) - 2 energy, Attack
    //   Effect: Sly. Deal 3 damage to a random enemy 4 times.
    //   Upgraded Effect: Sly. Deal 3 damage to a random enemy 5 times.
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

    // Slice (Common) - 0 energy, Attack
    //   Effect: Deal 6 damage.
    //   Upgraded Effect: Deal 9 damage.
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

    // Snakebite (Common) - 2 energy, Skill
    //   Effect: Retain. Apply 7 Poison.
    //   Upgraded Effect: Retain. Apply 10 Poison.
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

    // Sucker Punch (Common) - 1 energy, Attack
    //   Effect: Deal 8 damage. Apply 1 Weak.
    //   Upgraded Effect: Deal 10 damage. Apply 2 Weak.
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
            state.playerGainBlock(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Untouchable (Common) - 2 energy, Skill
    //   Effect: Sly. Gain 9 Block.
    //   Upgraded Effect: Sly. Gain 12 Block.
    // TODO CHANGED: Untouchable (Common) - 2 energy, Skill
    //   Effect: Sly. Gain 6 Block.
    //   Upgraded Effect: Sly. Gain 9 Block.
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

    // Accuracy (Uncommon) - 1 energy, Power
    //   Effect: Shivs deal 4 additional damage.
    //   Upgraded Effect: Shivs deal 6 additional damage.
    public static class Accuracy extends CardSilent.Accuracy {
    }

    public static class AccuracyP extends CardSilent.AccuracyP {
    }

    // Backstab (Uncommon) - 0 energy, Attack
    //   Effect: Innate. Deal 11 damage. Exhaust.
    //   Upgraded Effect: Innate. Deal 15 damage. Exhaust.
    public static class Backstab extends CardSilent.Backstab {
    }

    public static class BackstabP extends CardSilent.BackstabP {
    }

    // Blur (Uncommon) - 1 energy, Skill
    //   Effect: Gain 5 Block. Block is not removed at the start of your next turn.
    //   Upgraded Effect: Gain 8 Block. Block is not removed at the start of your next turn.
    public static class Blur extends CardSilent.Blur {
    }

    public static class BlurP extends CardSilent.BlurP {
    }

    // Bouncing Flask (Uncommon) - 2 energy, Skill
    //   Effect: Apply 3 Poison to a random enemy 3 times.
    //   Upgraded Effect: Apply 3 Poison to a random enemy 4 times.
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

    // Bubble Bubble (Uncommon) - 1 energy, Skill
    //   Effect: If the enemy has Poison, apply 9 Poison.
    //   Upgraded Effect: If the enemy has Poison, apply 12 Poison.
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

    // Calculated Gamble (Uncommon) - 0 energy, Skill
    //   Effect: Discard your Hand, then draw that many cards. Exhaust.
    //   Upgraded Effect: Retain. Discard your Hand, then draw that many cards. Exhaust.
    public static class CalculatedGamble extends CardSilent.CalculatedGamble {
    }

    public static class CalculatedGambleP extends CardSilent._CalculatedGambleT {
        public CalculatedGambleP() {
            super("Calculated Gamble+", true);
            this.retain = true;
        }
    }

    // Dash (Uncommon) - 2 energy, Attack
    //   Effect: Gain 10 Block. Deal 10 damage.
    //   Upgraded Effect: Gain 13 Block. Deal 13 damage.
    public static class Dash extends CardSilent.Dash {
    }

    public static class DashP extends CardSilent.DashP {
    }

    // Escape Plan (Uncommon) - 0 energy, Skill
    //   Effect: Draw 1 card. If you draw a Skill, gain 3 Block.
    //   Upgraded Effect: Draw 1 card. If you draw a Skill, gain 5 Block.
    public static class EscapePlan extends CardSilent.EscapePlan {
    }

    public static class EscapePlanP extends CardSilent.EscapePlanP {
    }

    // Expertise (Uncommon) - 1 energy, Skill
    //   Effect: Draw cards until you have 6 in your Hand.
    //   Upgraded Effect: Draw cards until you have 7 in your Hand.
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

    // Expose (Uncommon) - 0 energy, Skill
    //   Effect: Remove all Artifact and Block from the enemy. Apply 2 Vulnerable. Exhaust.
    //   Upgraded Effect: Remove all Artifact and Block from the enemy. Apply 3 Vulnerable. Exhaust.
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

    // Finisher (Uncommon) - 1 energy, Attack
    //   Effect: Deal 6 damage for each Attack already played this turn.
    //   Upgraded Effect: Deal 8 damage for each Attack already played this turn.
    public static class Finisher extends CardSilent.Finisher {
    }

    public static class FinisherP extends CardSilent.FinisherP {
    }

    // No need to implement Flanking: Multiplayer

    // Flechettes (Uncommon) - 1 energy, Attack
    //   Effect: Deal 5 damage for each Skill in your Hand.
    //   Upgraded Effect: Deal 7 damage for each Skill in your Hand.
    public static class Flechettes extends CardSilent._FlechettesT {
        public Flechettes() {
            super("Flechettes", 5);
        }
    }

    public static class FlechettesP extends CardSilent._FlechettesT {
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
                state.playerDoDamageToEnemy(enemy, dmg, this);
            }
            if (state.getLastCardPlayedType() == Card.SKILL) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemy.applyDebuff(state, DebuffType.WEAK, weak);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Follow Through (Uncommon) - 1 energy, Attack
    //   Effect: Deal 6 damage to ALL enemies. If the last card you played this turn was a Skill, apply 1 Weak to ALL enemies.
    //   Upgraded Effect: Deal 8 damage to ALL enemies. If the last card you played this turn was a Skill, apply 2 Weak to ALL enemies.
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

    // Footwork (Uncommon) - 1 energy, Power
    //   Effect: Gain 2 Dexterity.
    //   Upgraded Effect: Gain 3 Dexterity.
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

    // Haze (Uncommon) - 3 energy, Skill
    //   Effect: Sly. Apply 4 Poison to ALL enemies.
    //   Upgraded Effect: Sly. Apply 6 Poison to ALL enemies.
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

    // Hidden Daggers (Uncommon) - 0 energy, Skill
    //   Effect: Discard 2 cards. Add 2 Shivs into your Hand.
    //   Upgraded Effect: Discard 2 cards. Add 2 Shivs+ into your Hand.
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

    // Infinite Blades (Uncommon) - 1 energy, Power
    //   Effect: At the start of your turn, add 1 Shiv into your Hand.
    //   Upgraded Effect: Innate. At the start of your turn, add 1 Shiv into your Hand.
    public static class InfiniteBlades extends CardSilent.InfiniteBlades {
    }

    public static class InfiniteBladesP extends CardSilent.InfiniteBladesP {
    }

    // Leg Sweep (Uncommon) - 2 energy, Skill
    //   Effect: Apply 2 Weak. Gain 11 Block.
    //   Upgraded Effect: Apply 3 Weak. Gain 14 Block.
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), baseDmg + bonusPerDiscard * discarded, this);
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

    // Memento Mori (Uncommon) - 1 energy, Attack
    //   Effect: Deal 8 damage. Deals 4 additional damage for each card discarded this turn.
    //   Upgraded Effect: Deal 10 damage. Deals 5 additional damage for each card discarded this turn.
    // TODO CHANGED: Memento Mori (Uncommon) - 1 energy, Attack
    //   Effect: Deal 9 damage. Deals 4 additional damage for each card discarded this turn.
    //   Upgraded Effect: Deal 11 damage. Deals 5 additional damage for each card discarded this turn.
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
            state.playerGainBlock(totalPoison);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Mirage (Uncommon) - 1 energy, Skill
    //   Effect: Gain Block equal to Poison on ALL enemies. Exhaust.
    //   Upgraded Effect (0 energy): Gain Block equal to Poison on ALL enemies. Exhaust.
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

    // Noxious Fumes (Uncommon) - 1 energy, Power
    //   Effect: At the start of your turn, apply 2 Poison to ALL enemies.
    //   Upgraded Effect: At the start of your turn, apply 3 Poison to ALL enemies.
    public static class NoxiousFumes extends CardSilent.NoxiousFumes {
    }

    public static class NoxiousFumesP extends CardSilent.NoxiousFumesP {
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

    // Outbreak (Uncommon) - 1 energy, Power
    //   Effect: Every 3 times you apply Poison, deal 11 damage to ALL enemies.
    //   Upgraded Effect: Every 3 times you apply Poison, deal 15 damage to ALL enemies.
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
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

    // Pinpoint (Uncommon) - 3 energy, Attack
    //   Effect: Deal 17 damage. Costs 1 less energy for each Skill played this turn.
    //   Upgraded Effect: Deal 22 damage. Costs 1 less energy for each Skill played this turn.
    // TODO CHANGED: Pinpoint (Uncommon) - 3 energy, Attack
    //   Effect: Deal 15 damage. Costs 1 less energy for each Skill played this turn.
    //   Upgraded Effect: Deal 19 damage. Costs 1 less energy for each Skill played this turn.
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
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

    // Pounce (Uncommon) - 2 energy, Attack
    //   Effect: Deal 12 damage. The next Skill you play costs 0 energy.
    //   Upgraded Effect: Deal 18 damage. The next Skill you play costs 0 energy.
    // TODO CHANGED: Pounce (Uncommon) - 2 energy, Attack
    //   Effect: Deal 14 damage. The next Skill you play costs 0 energy.
    //   Upgraded Effect: Deal 20 damage. The next Skill you play costs 0 energy.
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Precise Cut (Uncommon) - 0 energy, Attack
    //   Effect: Deal 13 damage. Deals 2 less damage for each other card in your Hand.
    //   Upgraded Effect: Deal 16 damage. Deals 2 less damage for each other card in your Hand.
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

    // Predator (Uncommon) - 2 energy, Attack
    //   Effect: Deal 15 damage. Next turn, draw 2 cards.
    //   Upgraded Effect: Deal 20 damage. Next turn, draw 2 cards.
    // TODO CHANGED: Predator (Common) - 2 energy, Attack
    //   Effect: Deal 15 damage. Next turn, draw 2 cards.
    //   Upgraded Effect: Deal 20 damage. Next turn, draw 2 cards.
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

    // Reflex (Uncommon) - 3 energy, Skill
    //   Effect: Sly. Draw 2 cards.
    //   Upgraded Effect: Sly. Draw 3 cards.
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

    // Skewer (Uncommon) - X energy, Attack
    //   Effect: Deal 7 damage X times.
    //   Upgraded Effect: Deal 10 damage X times.
    // TODO CHANGED: Skewer (Uncommon) - X energy, Attack
    //   Effect: Deal 8 damage X times.
    //   Upgraded Effect: Deal 11 damage X times.
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

    // Speedster (Uncommon) - 2 energy, Power
    //   Effect: Whenever you draw a card during your turn, deal 2 damage to ALL enemies.
    //   Upgraded Effect: Whenever you draw a card during your turn, deal 3 damage to ALL enemies.
    // TODO CHANGED: Speedster (Uncommon) - 2 energy, Power
    //   Effect: Whenever you draw a card during your turn, deal 2 damage to ALL enemies.
    //   Upgraded Effect: Innate. Whenever you draw a card during your turn, deal 2 damage to ALL enemies.
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
            state.playerDoDamageToEnemy(enemy, dmg, this);
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

    // Strangle (Uncommon) - 1 energy, Attack
    //   Effect: Deal 8 damage. Whenever you play a card this turn, the enemy loses 2 HP.
    //   Upgraded Effect: Deal 10 damage. Whenever you play a card this turn, the enemy loses 3 HP.
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

    // Tactician (Uncommon) - 3 energy, Skill
    //   Effect: Sly. Gain energy.
    //   Upgraded Effect: Sly. Gain 2 energy.
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

    // Up My Sleeve (Uncommon) - 2 energy, Skill
    //   Effect: Add 3 Shivs into your Hand. Reduce this card's cost by 1.
    //   Upgraded Effect: Add 4 Shivs into your Hand. Reduce this card's cost by 1.
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

    // Well-Laid Plans (Uncommon) - 1 energy, Power
    //   Effect: At the end of your turn, Retain up to 1 card.
    //   Upgraded Effect: At the end of your turn, Retain up to 2 cards.
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

    // Abrasive (Rare) - 3 energy, Power
    //   Effect: Sly. Gain 1 Dexterity. Gain 4 Thorns.
    //   Upgraded Effect: Sly. Gain 1 Dexterity. Gain 6 Thorns.
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

    // Accelerant (Rare) - 1 energy, Power
    //   Effect: Poison is triggered 1 additional time.
    //   Upgraded Effect: Poison is triggered 2 additional times.
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

    // Adrenaline (Rare) - 0 energy, Skill
    //   Effect: Gain energy. Draw 2 cards. Exhaust.
    //   Upgraded Effect: Gain 2 energy. Draw 2 cards. Exhaust.
    public static class Adrenaline extends CardSilent.Adrenaline {
    }

    public static class AdrenalineP extends CardSilent.AdrenalineP {
    }

    // Afterimage (Rare) - 1 energy, Power
    //   Effect: Whenever you play a card, gain 1 Block.
    //   Upgraded Effect: Innate. Whenever you play a card, gain 1 Block.
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
            state.playerDoDamageToEnemy(enemy, dmg, this);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, vul);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Assassinate (Rare) - 0 energy, Attack
    //   Effect: Innate. Deal 10 damage. Apply 1 Vulnerable. Exhaust.
    //   Upgraded Effect: Innate. Deal 13 damage. Apply 2 Vulnerable. Exhaust.
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

    // Blade of Ink (Rare) - 1 energy, Skill
    //   Effect: This turn, whenever you play an Attack, gain 2 Strength this turn.
    //   Upgraded Effect: This turn, whenever you play an Attack, gain 3 Strength this turn.
    // TODO CHANGED: Blade of Ink (Rare) - 1 energy, Skill
    //   Effect: Add 2 Inky Shivs into your Hand.
    //   Upgraded Effect: Add 3 Inky Shivs into your Hand.
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

    // Bullet Time (Rare) - 3 energy, Skill
    //   Effect: You cannot draw additional cards this turn. ALL cards in your Hand are free to play this turn.
    //   Upgraded Effect (2 energy): You cannot draw additional cards this turn. ALL cards in your Hand are free to play this turn.
    public static class BulletTime extends CardSilent.BulletTime {
    }

    public static class BulletTimeP extends CardSilent.BulletTimeP {
    }

    // Burst (Rare) - 1 energy, Skill
    //   Effect: This turn, your next Skill is played an extra time.
    //   Upgraded Effect: This turn, your next 2 Skills are played an extra time.
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

    // Corrosive Wave (Rare) - 1 energy, Skill
    //   Effect: Whenever you draw a card this turn, apply 3 Poison to ALL enemies.
    //   Upgraded Effect: Whenever you draw a card this turn, apply 4 Poison to ALL enemies.
    // TODO CHANGED: Corrosive Wave (Rare) - 1 energy, Skill
    //   Effect: Whenever you draw a card this turn, apply 2 Poison to ALL enemies.
    //   Upgraded Effect: Whenever you draw a card this turn, apply 3 Poison to ALL enemies.
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
                    state.playerDoDamageToEnemy(enemy, dmg, this);
                    if (prevHp > 0 && !enemy.isAlive()) {
                        numKills++;
                    }
                }
            } while (numKills > 0);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Echoing Slash (Rare) - 1 energy, Attack
    //   Effect: Deal 10 damage to ALL enemies. Repeat this effect for each enemy killed.
    //   Upgraded Effect: Deal 13 damage to ALL enemies. Repeat this effect for each enemy killed.
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

    // Envenom (Rare) - 2 energy, Power
    //   Effect: Whenever an Attack deals unblocked damage, apply 1 Poison.
    //   Upgraded Effect: Whenever an Attack deals unblocked damage, apply 2 Poison.
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

    // Fan of Knives (Rare) - 2 energy, Power
    //   Effect: Shivs now hit ALL enemies. Add 4 Shivs into your Hand.
    //   Upgraded Effect: Shivs now hit ALL enemies. Add 5 Shivs into your Hand.
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

    // Grand Finale (Rare) - 0 energy, Attack
    //   Effect: Can only be played if there are no cards in your Draw Pile. Deal 50 damage to ALL enemies.
    //   Upgraded Effect: Can only be played if there are no cards in your Draw Pile. Deal 60 damage to ALL enemies.
    // TODO CHANGED: Grand Finale (Rare) - 0 energy, Attack
    //   Effect: Can only be played if there are no cards in your Draw Pile. Deal 60 damage to ALL enemies.
    //   Upgraded Effect: Can only be played if there are no cards in your Draw Pile. Deal 75 damage to ALL enemies.
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

    // Knife Trap (Rare) - 2 energy, Skill
    //   Effect: Play every Shiv in your Exhaust Pile on the enemy.
    //   Upgraded Effect: Upgrade and play every Shiv in your Exhaust Pile on the enemy.
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

    // Malaise (Rare) - X energy, Skill
    //   Effect: Enemy loses X Strength. Apply X Weak. Exhaust.
    //   Upgraded Effect: Enemy loses X+1 Strength. Apply X+1 Weak. Exhaust.
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 1 + state.getCounterForRead()[counterIdx], this);
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

    // Murder (Rare) - 3 energy, Attack
    //   Effect: Deal 1 damage. Deals 1 additional damage for each card drawn this combat.
    //   Upgraded Effect (2 energy): Deal 1 damage. Deals 1 additional damage for each card drawn this combat.
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

    // Nightmare (Rare) - 3 energy, Skill
    //   Effect: Choose a card. Next turn, add 3 copies of that card into your Hand. Exhaust.
    //   Upgraded Effect (2 energy): Choose a card. Next turn, add 3 copies of that card into your Hand. Exhaust.
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

    // Serpent Form (Rare) - 3 energy, Power
    //   Effect: Whenever you play a card, deal 4 damage to a random enemy.
    //   Upgraded Effect: Whenever you play a card, deal 5 damage to a random enemy.
    // TODO CHANGED: Serpent Form (Rare) - 3 energy, Power
    //   Effect: Whenever you play a card, deal 4 damage to a random enemy.
    //   Upgraded Effect: Whenever you play a card, deal 6 damage to a random enemy.
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

    // Shadow Step (Rare) - 1 energy, Skill
    //   Effect: Discard your Hand. Next turn, Attacks deal double damage.
    //   Upgraded Effect (0 energy): Discard your Hand. Next turn, Attacks deal double damage.
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
            state.properties.addEndOfTurnHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    // Shadowmeld (Rare) - 1 energy, Skill
    //   Effect: Double your Block gain this turn.
    //   Upgraded Effect (0 energy): Double your Block gain this turn.
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

    // Storm of Steel (Rare) - 1 energy, Skill
    //   Effect: Discard your Hand. Add 1 Shiv into your Hand for each card discarded.
    //   Upgraded Effect: Discard your Hand. Add 1 Shiv+ into your Hand for each card discarded.
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
            state.playerDoDamageToEnemy(enemy, dmg, this);
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
                        v.add(GameState.V_HEALTH_IDX, 10 * v.getVExtra(vExtraIdx) * healthRewardRatio / state.getPlayerForRead().getMaxHealth());
                    }
                });
            }
        }
    }

    // The Hunt (Rare) - 1 energy, Attack
    //   Effect: Deal 10 damage. If Fatal, gain an additional card reward. Exhaust.
    //   Upgraded Effect: Deal 15 damage. If Fatal, gain an additional card reward. Exhaust.
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

    // Tools of the Trade (Rare) - 1 energy, Power
    //   Effect: At the start of your turn, draw 1 card and discard 1 card.
    //   Upgraded Effect (0 energy): At the start of your turn, draw 1 card and discard 1 card.
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

    // Tracking (Rare) - 2 energy, Power
    //   Effect: Weak enemies take double damage from Attacks.
    //   Upgraded Effect (1 energy): Weak enemies take double damage from Attacks.
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

    // Caltrops (Event) - 1 energy, Power
    //   Effect: Whenever you are attacked, deal 3 damage back.
    //   Upgraded Effect: Whenever you are attacked, deal 5 damage back.
    public static class Caltrops extends CardSilent.Caltrops {
    }

    public static class CaltropsP extends CardSilent.CaltropsP {
    }

    // Distraction (Event) - 1 energy, Skill
    //   Effect: Add a random Skill into your Hand. It's free to play this turn. Exhaust.
    //   Upgraded Effect (0 energy): Add a random Skill into your Hand. It's free to play this turn. Exhaust.
    public static class Distraction extends CardSilent.Distraction {
    }

    public static class DistractionP extends CardSilent.DistractionP {
    }

    // Outmaneuver (Event) - 1 energy, Skill
    //   Effect: Next turn, gain 2 energy.
    //   Upgraded Effect: Next turn, gain 3 energy.
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
            state.playerDoDamageToEnemy(enemy, dmg, this);
            enemy.applyDebuff(state, DebuffType.WEAK, weak);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Suppress (Ancient) - 0 energy, Attack
    //   Effect: Innate. Deal 11 damage. Apply 3 Weak.
    //   Upgraded Effect: Innate. Deal 17 damage. Apply 5 Weak.
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

    // Wraith Form (Ancient) - 3 energy, Power
    //   Effect: Gain 2 Intangible. At the start of your turn, lose 1 Dexterity.
    //   Upgraded Effect: Gain 3 Intangible. At the start of your turn, lose 1 Dexterity.
    public static class WraithForm extends CardSilent.WraithForm {
    }

    public static class WraithFormP extends CardSilent.WraithFormP {
    }

    // **************************************************************************************************
    // ********************************************* Uncommon *********************************************
    // **************************************************************************************************

    // TODO: Scare (Uncommon) - 0 energy, Skill
    //   Effect: Apply 1 Weak to ALL enemies. Exhaust.
    //   Upgraded Effect: Apply 1 Weak to ALL enemies.
}
