package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.eventHandler.GameEventCardHandler;
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

    // TODO: Anticipate (Common) - 0 energy, Skill
    //   Effect: Gain 3 Dexterity this turn.
    //   Upgraded Effect: Gain 5 Dexterity this turn.

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

    // TODO: Flick-Flack (Common) - 1 energy, Attack
    //   Effect: Sly. Deal 7 damage to ALL enemies.
    //   Upgraded Effect: Sly. Deal 9 damage to ALL enemies.

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

    // TODO: Ricochet (Common) - 2 energy, Attack
    //   Effect: Sly. Deal 3 damage to a random enemy 4 times.
    //   Upgraded Effect: Sly. Deal 3 damage to a random enemy 5 times.

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

    // TODO: Untouchable (Common) - 2 energy, Skill
    //   Effect: Sly. Gain 9 Block.
    //   Upgraded Effect: Sly. Gain 12 Block.

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

    // TODO: Expose (Uncommon) - 0 energy, Skill
    //   Effect: Remove all Artifact and Block from the enemy. Apply 2 Vulnerable. Exhaust.
    //   Upgraded Effect: Remove all Artifact and Block from the enemy. Apply 3 Vulnerable. Exhaust.

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

    // TODO: Haze (Uncommon) - 3 energy, Skill
    //   Effect: Sly. Apply 4 Poison to ALL enemies.
    //   Upgraded Effect: Sly. Apply 6 Poison to ALL enemies.

    // TODO: Hidden Daggers (Uncommon) - 0 energy, Skill
    //   Effect: Discard 2 cards. Add 2 Shivs into your Hand.
    //   Upgraded Effect: Discard 2 cards. Add 2 Shivs+ into your Hand.

    public static class InfiniteBlades extends CardSilent.InfiniteBlade {
    }

    public static class InfiniteBladeP extends CardSilent.InfiniteBladeP {
    }

    public static class LegSweep extends CardSilent.LegSweep {
    }

    public static class LegSweepP extends CardSilent.LegSweepP {
    }

    // TODO: Memento Mori (Uncommon) - 1 energy, Attack
    //   Effect: Deal 8 damage. Deals 4 additional damage for each card discarded this turn.
    //   Upgraded Effect: Deal 10 damage. Deals 5 additional damage for each card discarded this turn.

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

    // TODO: Outbreak (Uncommon) - 1 energy, Power
    //   Effect: Every 3 times you apply Poison, deal 11 damage to ALL enemies.
    //   Upgraded Effect: Every 3 times you apply Poison, deal 15 damage to ALL enemies.

    // TODO: Phantom Blades (Uncommon) - 1 energy, Power
    //   Effect: Shivs gain Retain. The first Shiv you play each turn deals 9 additional damage.
    //   Upgraded Effect: Shivs gain Retain. The first Shiv you play each turn deals 12 additional damage.

    // TODO: Pinpoint (Uncommon) - 3 energy, Attack
    //   Effect: Deal 17 damage. Costs 1 less energy for each Skill played this turn.
    //   Upgraded Effect: Deal 22 damage. Costs 1 less energy for each Skill played this turn.

    // TODO: Pounce (Uncommon) - 2 energy, Attack
    //   Effect: Deal 12 damage. The next Skill you play costs 0 energy.
    //   Upgraded Effect: Deal 18 damage. The next Skill you play costs 0 energy.

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

    // TODO: Reflex (Uncommon) - 3 energy, Skill
    //   Effect: Sly. Draw 2 cards.
    //   Upgraded Effect: Sly. Draw 3 cards.

    public static class Skewer extends CardSilent.Skewer {
    }

    public static class SkewerP extends CardSilent.SkewerP {
    }

    // TODO: Speedster (Uncommon) - 2 energy, Power
    //   Effect: Whenever you draw a card during your turn, deal 2 damage to ALL enemies.
    //   Upgraded Effect: Whenever you draw a card during your turn, deal 3 damage to ALL enemies.

    // TODO: Strangle (Uncommon) - 1 energy, Attack
    //   Effect: Deal 8 damage. Whenever you play a card this turn, the enemy loses 2 HP.
    //   Upgraded Effect: Deal 10 damage. Whenever you play a card this turn, the enemy loses 3 HP.

    // TODO: Tactician (Uncommon) - 3 energy, Skill
    //   Effect: Sly. Gain energy.
    //   Upgraded Effect: Sly. Gain 2 energy.

    // TODO: Up My Sleeve (Uncommon) - 2 energy, Skill
    //   Effect: Add 3 Shivs into your Hand. Reduce this card's cost by 1.
    //   Upgraded Effect: Add 4 Shivs into your Hand. Reduce this card's cost by 1.

    public static class WellLaidPlans extends CardSilent.WellLaidPlans {
    }

    public static class WellLaidPlansP extends CardSilent.WellLaidPlansP {
    }

    // **************************************************************************************************
    // *********************************************  Rare  *********************************************
    // **************************************************************************************************

    // TODO: Abrasive (Rare) - 3 energy, Power
    //   Effect: Sly. Gain 1 Dexterity. Gain 4 Thorns.
    //   Upgraded Effect: Sly. Gain 1 Dexterity. Gain 6 Thorns.

    // TODO: Accelerant (Rare) - 1 energy, Power
    //   Effect: Poison is triggered 1 additional time.
    //   Upgraded Effect: Poison is triggered 2 additional times.

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

    // TODO: Blade of Ink (Rare) - 1 energy, Skill
    //   Effect: This turn, whenever you play an Attack, gain 2 Strength this turn.
    //   Upgraded Effect: This turn, whenever you play an Attack, gain 3 Strength this turn.

    public static class BulletTime extends CardSilent.BulletTime {
    }

    public static class BulletTimeP extends CardSilent.BulletTimeP {
    }

    public static class Burst extends CardSilent.Burst {
    }

    public static class BurstP extends CardSilent.BurstP {
    }

    // TODO: Corrosive Wave (Rare) - 1 energy, Skill
    //   Effect: Whenever you draw a card this turn, apply 3 Poison to ALL enemies.
    //   Upgraded Effect: Whenever you draw a card this turn, apply 4 Poison to ALL enemies.

    // TODO: Echoing Slash (Rare) - 1 energy, Attack
    //   Effect: Deal 10 damage to ALL enemies. Repeat this effect for each enemy killed.
    //   Upgraded Effect: Deal 13 damage to ALL enemies. Repeat this effect for each enemy killed.

    public static class Envenom extends CardSilent.Envenom {
    }

    public static class EnvenomP extends CardSilent._EnvenomT {
        public EnvenomP() {
            super("Envenom+", 2, 2);
        }
    }

    // TODO: Fan of Knives (Rare) - 2 energy, Power
    //   Effect: Shivs now hit ALL enemies. Add 4 Shivs into your Hand.
    //   Upgraded Effect: Shivs now hit ALL enemies. Add 5 Shivs into your Hand.

    public static class GrandFinale extends CardSilent.GrandFinale {
    }

    public static class GrandFinaleP extends CardSilent.GrandFinaleP {
    }

    // TODO: Knife Trap (Rare) - 2 energy, Skill
    //   Effect: Play every Shiv in your Exhaust Pile on the enemy.
    //   Upgraded Effect: Upgrade and play every Shiv in your Exhaust Pile on the enemy.

    public static class Malaise extends CardSilent.Malaise {
    }

    public static class MalaiseP extends CardSilent.MalaiseP {
    }

    // TODO: Master Planner (Rare) - 2 energy, Power
    //   Effect: When you play a Skill, it gains Sly.
    //   Upgraded Effect (1 energy): When you play a Skill, it gains Sly.

    // TODO: Murder (Rare) - 3 energy, Attack
    //   Effect: Deal 1 damage. Deals 1 additional damage for each card drawn this combat.
    //   Upgraded Effect (2 energy): Deal 1 damage. Deals 1 additional damage for each card drawn this combat.

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

    // TODO: Shadow Step (Rare) - 1 energy, Skill
    //   Effect: Discard your Hand. Next turn, Attacks deal double damage.
    //   Upgraded Effect (0 energy): Discard your Hand. Next turn, Attacks deal double damage.

    // TODO: Shadowmeld (Rare) - 1 energy, Skill
    //   Effect: Double your Block gain this turn.
    //   Upgraded Effect (0 energy): Double your Block gain this turn.

    // TODO: Sneaky (Rare) - 2 energy, Power
    //   Effect: Sly. Whenever another player attacks an enemy, gain 1 Block.
    //   Upgraded Effect: Sly. Whenever another player attacks an enemy, gain 2 Block.

    public static class StormOfSteel extends CardSilent.StormOfSteel {
    }

    public static class StormOfSteelP extends CardSilent.StormOfSteelP {
    }

    // TODO: The Hunt (Rare) - 1 energy, Attack
    //   Effect: Deal 10 damage. If Fatal, gain an additional card reward. Exhaust.
    //   Upgraded Effect: Deal 15 damage. If Fatal, gain an additional card reward. Exhaust.

    public static class ToolsOfTheTrade extends CardSilent.ToolsOfTheTrade {
    }

    public static class ToolsOfTheTradeP extends CardSilent.ToolsOfTheTradeP {
    }

    // TODO: Tracking (Rare) - 2 energy, Power
    //   Effect: Weak enemies take double damage from Attacks.
    //   Upgraded Effect (1 energy): Weak enemies take double damage from Attacks.

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
