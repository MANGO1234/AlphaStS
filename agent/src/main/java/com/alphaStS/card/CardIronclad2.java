package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.gameAction.GameActionCtx;

public class CardIronclad2 {
    // **************************************************************************************************
    // ********************************************* Basic  *********************************************
    // **************************************************************************************************

    public static class Bash extends CardIronclad.Bash {
    }

    public static class BashP extends CardIronclad.BashP {
    }

    public static class Defend extends Card.Defend {
    }

    public static class DefendP extends Card.DefendP {
    }

    public static class Strike extends Card.Strike {
    }

    public static class StrikeP extends Card.StrikeP {
    }

    // **************************************************************************************************
    // ********************************************* Common *********************************************
    // **************************************************************************************************

    public static class Anger extends CardIronclad.Anger {
    }

    public static class AngerP extends CardIronclad.AngerP {
    }

    public static class Armaments extends CardIronclad.Armanent {
    }

    public static class ArmamentsP extends CardIronclad.ArmanentP {
    }

    private static abstract class _BloodWallT extends Card {
        private final int block;

        public _BloodWallT(String cardName, int block) {
            super(cardName, Card.SKILL, 2, Card.COMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.doNonAttackDamageToPlayer(2, false, this);
            state.getPlayerForWrite().gainBlock(block);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BloodWall extends _BloodWallT {
        public BloodWall() {
            super("Blood Wall", 16);
        }
    }

    public static class BloodWallP extends _BloodWallT {
        public BloodWallP() {
            super("Blood Wall+", 20);
        }
    }

    public static class Bloodletting extends CardIronclad._BloodlettingT {
        public Bloodletting() {
            super("Bloodletting", 2, Card.COMMON);
        }
    }

    public static class BloodlettingP extends CardIronclad._BloodlettingT {
        public BloodlettingP() {
            super("Bloodletting+", 3, Card.COMMON);
        }
    }

    public static class BodySlam extends CardIronclad.BodySlam {
    }

    public static class BodySlamP extends CardIronclad.BodySlamP {
    }

    private static abstract class _BreakthroughT extends Card {
        private final int damage;

        public _BreakthroughT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.doNonAttackDamageToPlayer(1, false, this);
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, damage);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Breakthrough extends _BreakthroughT {
        public Breakthrough() {
            super("Breakthrough", 9);
        }
    }

    public static class BreakthroughP extends _BreakthroughT {
        public BreakthroughP() {
            super("Breakthrough+", 13);
        }
    }

    private static abstract class _CinderT extends Card {
        private final int damage;

        public _CinderT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            int cardIdx = state.drawOneCardSpecial();
            if (cardIdx >= 0) {
                state.exhaustedCardHandle(cardIdx, true);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Cinder extends _CinderT {
        public Cinder() {
            super("Cinder", 17);
        }
    }

    public static class CinderP extends _CinderT {
        public CinderP() {
            super("Cinder+", 22);
        }
    }

    public static class Havoc extends CardIronclad.Havoc {
    }

    public static class HavocP extends CardIronclad.HavocP {
    }

    public static class Headbutt extends CardIronclad.Headbutt {
    }

    public static class HeadbuttP extends CardIronclad.HeadbuttP {
    }

    public static class IronWave extends CardIronclad.IronWave {
    }

    public static class IronWaveP extends CardIronclad.IronWaveP {
    }

    private static abstract class _MoltenFistT extends Card {
        private final int damage;

        public _MoltenFistT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, enemy.getVulnerable());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class MoltenFist extends _MoltenFistT {
        public MoltenFist() {
            super("Molten Fist", 10);
        }
    }

    public static class MoltenFistP extends _MoltenFistT {
        public MoltenFistP() {
            super("Molten Fist+", 14);
        }
    }

    public static class PerfectedStrike extends CardIronclad.PerfectedStrike {
    }

    public static class PerfectedStrikeP extends CardIronclad.PerfectedStrikeP {
    }

    public static class PommelStrike extends CardIronclad.PommelStrike {
    }

    public static class PommelStrikeP extends CardIronclad.PommelStrikeP {
    }

    private static abstract class _SetupStrikeT extends Card {
        private final int damage;
        private final int strength;

        public _SetupStrikeT(String cardName, int damage, int strength) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.strength = strength;
            entityProperty.selectEnemy = true;
            entityProperty.changePlayerStrength = true;
            entityProperty.changePlayerStrengthEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            var player = state.getPlayerForWrite();
            player.gainStrength(strength);
            player.applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, strength);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SetupStrike extends _SetupStrikeT {
        public SetupStrike() {
            super("Setup Strike", 7, 2);
        }
    }

    public static class SetupStrikeP extends _SetupStrikeT {
        public SetupStrikeP() {
            super("Setup Strike+", 9, 3);
        }
    }

    public static class ShrugItOff extends CardIronclad.ShrugItOff {
    }

    public static class ShrugItOffP extends CardIronclad.ShrugItOffP {
    }

    public static class SwordBoomerang extends CardIronclad.SwordBoomerang {
    }

    public static class SwordBoomerangP extends CardIronclad.SwordBoomerangP {
    }

    public static class Thunderclap extends CardIronclad.Thunderclap {
    }

    public static class ThunderclapP extends CardIronclad.ThunderclapP {
    }

    private static abstract class _TrembleT extends Card {
        private final int vulnerable;

        public _TrembleT(String cardName, int vulnerable) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.vulnerable = vulnerable;
            entityProperty.selectEnemy = true;
            entityProperty.vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.VULNERABLE, vulnerable);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Tremble extends _TrembleT {
        public Tremble() {
            super("Tremble", 2);
        }
    }

    public static class TrembleP extends _TrembleT {
        public TrembleP() {
            super("Tremble+", 3);
        }
    }

    public static class TrueGrit extends CardIronclad.TrueGrit {
    }

    public static class TrueGritP extends CardIronclad.TrueGritP {
    }

    public static class TwinStrike extends CardIronclad.TwinStrike {
    }

    public static class TwinStrikeP extends CardIronclad.TwinStrikeP {
    }

    // **************************************************************************************************
    // ********************************************* Uncommon *******************************************
    // **************************************************************************************************

    // TODO: Ashen Strike (Uncommon) - 1 energy, Attack
    //   Effect: Deal 6 damage. Deals 3 additional damage for each card in your Exhaust Pile.
    //   Upgraded Effect: Deal 6 damage. Deals 4 additional damage for each card in your Exhaust Pile.

    public static class BattleTrance extends CardIronclad.BattleTrance {
    }

    public static class BattleTranceP extends CardIronclad.BattleTranceP {
    }

    public static class Bludgeon extends CardIronclad.Bludgeon {
    }

    public static class BludgeonP extends CardIronclad.BludgeonP {
    }

    // TODO: Bully (Uncommon) - 0 energy, Attack
    //   Effect: Deal 4 damage. Deals 2 additional damage for each Vulnerable on the enemy.
    //   Upgraded Effect: Deal 4 damage. Deals 3 additional damage for each Vulnerable on the enemy.

    public static class BurningPact extends CardIronclad.BurningPact {
    }

    public static class BurningPactP extends CardIronclad.BurningPactP {
    }

    // No need to implement Demonic Shield: Multiplayer

    // TODO: Dismantle (Uncommon) - 1 energy, Attack
    //   Effect: Deal 8 damage. If the enemy is Vulnerable, hits twice.
    //   Upgraded Effect: Deal 10 damage. If the enemy is Vulnerable, hits twice.

    // TODO: Dominate (Uncommon) - 1 energy, Skill
    //   Effect: Gain 1 Strength for each Vulnerable on the enemy. Exhaust.
    //   Upgraded Effect: Gain 1 Strength for each Vulnerable on the enemy.

    // TODO: Drum of Battle (Uncommon) - 0 energy, Power
    //   Effect: Draw 2 cards. At the start of your turn, Exhaust the top card of your Draw Pile.
    //   Upgraded Effect: Draw 3 cards. At the start of your turn, Exhaust the top card of your Draw Pile.

    // TODO: Evil Eye (Uncommon) - 1 energy, Skill
    //   Effect: Gain 8 Block. Gain another 8 Block if you have Exhausted a card this turn.
    //   Upgraded Effect: Gain 11 Block. Gain another 11 Block if you have Exhausted a card this turn.

    // TODO: Expect a Fight (Uncommon) - 2 energy, Skill
    //   Effect: Gain energy for each Attack in your Hand.
    //   Upgraded Effect (1 energy): Gain energy for each Attack in your Hand.

    public static class FeelNoPain extends CardIronclad.FeelNoPain {
    }

    public static class FeelNoPainP extends CardIronclad.FeelNoPainP {
    }

    // TODO: Fight Me! (Uncommon) - 2 energy, Attack
    //   Effect: Deal 5 damage twice. Gain 2 Strength. The enemy gains 1 Strength.
    //   Upgraded Effect: Deal 6 damage twice. Gain 3 Strength. The enemy gains 1 Strength.

    public static class FlameBarrier extends CardIronclad.FlameBarrier {
    }

    public static class FlameBarrierP extends CardIronclad.FlameBarrierP {
    }

    // TODO: Forgotten Ritual (Uncommon) - 1 energy, Skill
    //   Effect: If you Exhausted a card this turn, gain 3 energy.
    //   Upgraded Effect: If you Exhausted a card this turn, gain 4 energy.

    // TODO: Grapple (Uncommon) - 1 energy, Attack
    //   Effect: Deal 7 damage. Whenever you gain Block this turn, deal 5 damage to the enemy.
    //   Upgraded Effect: Deal 9 damage. Whenever you gain Block this turn, deal 7 damage to the enemy.

    public static class Hemokinesis extends CardIronclad._HemokinesisT {
        public Hemokinesis() {
            super("Hemokinesis", 14);
        }
    }

    public static class HemokinesisP extends CardIronclad._HemokinesisT {
        public HemokinesisP() {
            super("Hemokinesis+", 19);
        }
    }

    // TODO: Howl from Beyond (Uncommon) - 3 energy, Attack
    //   Effect: Deal 16 damage to ALL enemies. At the start of your turn, plays from the Exhaust Pile.
    //   Upgraded Effect: Deal 21 damage to ALL enemies. At the start of your turn, plays from the Exhaust Pile.

    public static class InfernalBlade extends CardIronclad.InfernalBlade {
    }

    public static class InfernalBladeP extends CardIronclad.InfernalBladeP {
    }

    // TODO: Inferno (Uncommon) - 1 energy, Power
    //   Effect: At the start of your turn, lose 1 HP. Whenever you lose HP on your turn, deal 6 damage to ALL enemies.
    //   Upgraded Effect: At the start of your turn, lose 1 HP. Whenever you lose HP on your turn, deal 9 damage to ALL enemies.

    public static class Inflame extends CardIronclad.Inflame {
    }

    public static class InflameP extends CardIronclad.InflameP {
    }

    // TODO: Juggling (Uncommon) - 1 energy, Power
    //   Effect: Add a copy of the third Attack you play each turn into your Hand.
    //   Upgraded Effect: Innate. Add a copy of the third Attack you play each turn into your Hand.

    // TODO: Pillage (Uncommon) - 1 energy, Attack
    //   Effect: Deal 6 damage. Draw cards until you draw a non-Attack card.
    //   Upgraded Effect: Deal 9 damage. Draw cards until you draw a non-Attack card.

    public static class Rage extends CardIronclad.Rage {
    }

    public static class RageP extends CardIronclad.RageP {
    }

    // TODO: Rampage (Uncommon) - 1 energy, Attack
    //   Effect: Deal 9 damage. Increase this card's damage by 5 this combat.
    //   Upgraded Effect: Deal 9 damage. Increase this card's damage by 9 this combat.

    // TODO: Rupture (Uncommon) - 1 energy, Power
    //   Effect: Whenever you lose HP on your turn, gain 1 Strength.
    //   Upgraded Effect: Whenever you lose HP on your turn, gain 2 Strength.

    public static class SecondWind extends CardIronclad.SecondWind {
    }

    public static class SecondWindP extends CardIronclad.SecondWindP {
    }

    // TODO: Spite (Uncommon) - 0 energy, Attack
    //   Effect: Deal 6 damage. If you lost HP this turn, draw 1 card.
    //   Upgraded Effect: Deal 9 damage. If you lost HP this turn, draw 1 card.

    // TODO: Stampede (Uncommon) - 2 energy, Power
    //   Effect: At the end of your turn, 1 random Attack in your Hand is played against a random enemy.
    //   Upgraded Effect (1 energy): At the end of your turn, 1 random Attack in your Hand is played against a random enemy.

    // TODO: Stomp (Uncommon) - 3 energy, Attack
    //   Effect: Deal 12 damage to ALL enemies. Costs 1 less energy for each Attack played this turn.
    //   Upgraded Effect: Deal 15 damage to ALL enemies. Costs 1 less energy for each Attack played this turn.

    // TODO: Stone Armor (Uncommon) - 1 energy, Power
    //   Effect: Gain 4 Plating.
    //   Upgraded Effect: Gain 6 Plating.

    private static abstract class _TauntT extends Card {
        private final int block;
        private final int vulnerable;

        public _TauntT(String cardName, int block, int vulnerable) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
            this.vulnerable = vulnerable;
            entityProperty.selectEnemy = true;
            entityProperty.vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.VULNERABLE, vulnerable);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Taunt extends _TauntT {
        public Taunt() {
            super("Taunt", 7, 1);
        }
    }

    public static class TauntP extends _TauntT {
        public TauntP() {
            super("Taunt+", 8, 2);
        }
    }

    // TODO: Unrelenting (Uncommon) - 2 energy, Attack
    //   Effect: Deal 12 damage. The next Attack you play costs 0 energy.
    //   Upgraded Effect: Deal 18 damage. The next Attack you play costs 0 energy.

    public static class Uppercut extends CardIronclad.Uppercut {
    }

    public static class UppercutP extends CardIronclad.UppercutP {
    }

    // TODO: Vicious (Uncommon) - 1 energy, Power
    //   Effect: Whenever you apply Vulnerable, draw 1 card.
    //   Upgraded Effect: Whenever you apply Vulnerable, draw 2 cards.

    public static class Whirlwind extends CardIronclad.Whirlwind {
    }

    public static class WhirlwindP extends CardIronclad.WhirlwindP {
    }

    // **************************************************************************************************
    // *********************************************  Rare  *********************************************
    // **************************************************************************************************

    // TODO: Aggression (Rare) - 1 energy, Power
    //   Effect: At the start of your turn, put a random Attack from your Discard Pile into your Hand and Upgrade it.
    //   Upgraded Effect: Innate. At the start of your turn, put a random Attack from your Discard Pile into your Hand and Upgrade it.

    public static class Barricade extends CardIronclad.Barricade {
    }

    public static class BarricadeP extends CardIronclad.BarricadeP {
    }

    // TODO: Brand (Rare) - 0 energy, Skill
    //   Effect: Lose 1 HP. Exhaust 1 card. Gain 1 Strength.
    //   Upgraded Effect: Lose 1 HP. Exhaust 1 card. Gain 2 Strength.

    // TODO: Cascade (Rare) - X energy, Skill
    //   Effect: Play the top X cards of your Draw Pile.
    //   Upgraded Effect: Play the top X+1 cards of your Draw Pile.

    // TODO: Colossus (Rare) - 1 energy, Skill
    //   Effect: Gain 5 Block. You receive 50% less damage from Vulnerable enemies this turn.
    //   Upgraded Effect: Gain 8 Block. You receive 50% less damage from Vulnerable enemies this turn.

    // TODO: Conflagration (Rare) - 1 energy, Attack
    //   Effect: Deal 8 damage to ALL enemies. Deals 2 additional damage for each other Attack you've played this turn.
    //   Upgraded Effect: Deal 9 damage to ALL enemies. Deals 3 additional damage for each other Attack you've played this turn.

    // TODO: Crimson Mantle (Rare) - 1 energy, Power
    //   Effect: At the start of your turn, lose 1 HP and gain 8 Block.
    //   Upgraded Effect: At the start of your turn, lose 1 HP and gain 10 Block.

    // TODO: Cruelty (Rare) - 1 energy, Power
    //   Effect: Vulnerable enemies take an additional 25% damage.
    //   Upgraded Effect: Vulnerable enemies take an additional 50% damage.

    public static class DarkEmbrace extends CardIronclad.DarkEmbrace {
    }

    public static class DarkEmbraceP extends CardIronclad.DarkEmbraceP {
    }

    public static class DemonForm extends CardIronclad.DemonForm {
    }

    public static class DemonFormP extends CardIronclad.DemonFormP {
    }

    public static class Feed extends CardIronclad.Feed {
    }

    public static class FeedP extends CardIronclad.FeedP {
    }

    public static class FiendFire extends CardIronclad.FiendFire {
    }

    public static class FiendFireP extends CardIronclad.FiendFireP {
    }

    // TODO: Hellraiser (Rare) - 2 energy, Power
    //   Effect: Whenever you draw a card containing "Strike", it is played against a random enemy.
    //   Upgraded Effect (1 energy): Whenever you draw a card containing "Strike", it is played against a random enemy.

    public static class Impervious extends CardIronclad.Impervious {
    }

    public static class ImperviousP extends CardIronclad.ImperviousP {
    }

    public static class Juggernaut extends CardIronclad.Juggernaut {
    }

    public static class JuggernautP extends CardIronclad.JuggernautP {
    }

    // TODO: Mangle (Rare) - 3 energy, Attack
    //   Effect: Deal 15 damage. Enemy loses 10 Strength this turn.
    //   Upgraded Effect: Deal 20 damage. Enemy loses 15 Strength this turn.

    public static class Offering extends CardIronclad.Offering {
    }

    public static class OfferingP extends CardIronclad.OfferingP {
    }

    // TODO: One-Two Punch (Rare) - 1 energy, Skill
    //   Effect: This turn, your next Attack is played an extra time.
    //   Upgraded Effect: This turn, your next 2 Attacks are played an extra time.

    // TODO: Pact's End (Rare) - 0 energy, Attack
    //   Effect: Can only be played if you have 3 or more cards in your Exhaust Pile. Deal 17 damage to ALL enemies.
    //   Upgraded Effect: Can only be played if you have 3 or more cards in your Exhaust Pile. Deal 23 damage to ALL enemies.

    // TODO: Primal Force (Rare) - 0 energy, Skill
    //   Effect: Transform all Attacks in your Hand into Giant Rock.
    //   Upgraded Effect: Transform all Attacks in your Hand into Giant Rock+.

    // TODO: Pyre (Rare) - 2 energy, Power
    //   Effect: Gain energy at the start of each turn.
    //   Upgraded Effect: Gain 2 energy at the start of each turn.

    // TODO: Stoke (Rare) - 1 energy, Skill
    //   Effect: Exhaust your Hand. Draw a card for each card Exhausted. Exhaust.
    //   Upgraded Effect (0 energy): Exhaust your Hand. Draw a card for each card Exhausted. Exhaust.

    // TODO: Tank (Rare) - 1 energy, Power
    //   Effect: Take double damage from enemies. Allies take half damage from enemies.
    //   Upgraded Effect (0 energy): Take double damage from enemies. Allies take half damage from enemies.

    // TODO: Tear Asunder (Rare) - 2 energy, Attack
    //   Effect: Deal 5 damage. Hits an additional time for each time you lost HP this combat.
    //   Upgraded Effect: Deal 7 damage. Hits an additional time for each time you lost HP this combat.

    // TODO: Thrash (Rare) - 1 energy, Attack
    //   Effect: Deal 4 damage twice. Exhaust a random Attack in your Hand and add its damage to this card.
    //   Upgraded Effect: Deal 6 damage twice. Exhaust a random Attack in your Hand and add its damage to this card.

    // TODO: Unmovable (Rare) - 2 energy, Power
    //   Effect: The first time you gain Block from a card each turn, double the amount gained.
    //   Upgraded Effect (1 energy): The first time you gain Block from a card each turn, double the amount gained.

    // **************************************************************************************************
    // ********************************************* Event  *********************************************
    // **************************************************************************************************

    public static class Clash extends CardIronclad.Clash {
    }

    public static class ClashP extends CardIronclad.ClashP {
    }

    public static class DualWield extends CardIronclad.DualWield {
    }

    public static class DualWieldP extends CardIronclad.DualWieldP {
    }

    public static class Entrench extends CardIronclad.Entrench {
    }

    public static class EntrenchP extends CardIronclad.EntrenchP {
    }

    // **************************************************************************************************
    // ********************************************* Ancient *********************************************
    // **************************************************************************************************

    private static abstract class _BreakT extends Card {
        private final int damage;
        private final int vulnerable;

        public _BreakT(String cardName, int damage, int vulnerable) {
            super(cardName, Card.ATTACK, 2, Card.RARE);
            this.damage = damage;
            this.vulnerable = vulnerable;
            entityProperty.selectEnemy = true;
            entityProperty.vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, vulnerable);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Break extends _BreakT {
        public Break() {
            super("Break", 20, 5);
        }
    }

    public static class BreakP extends _BreakT {
        public BreakP() {
            super("Break+", 25, 7);
        }
    }

    public static class Corruption extends CardIronclad.Corruption {
    }

    public static class CorruptionP extends CardIronclad.CorruptionP {
    }
}
