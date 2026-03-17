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

    // TODO: Molten Fist (Common) - 1 energy, Attack
    //   Effect: Deal 10 damage. Double the enemy's Vulnerable. Exhaust.
    //   Upgraded Effect: Deal 14 damage. Double the enemy's Vulnerable. Exhaust.

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
    // ********************************************* Uncommon *********************************************
    // **************************************************************************************************

    // TODO: Ashen Strike (Uncommon) - 1 energy, Attack
    //   Effect: Deal 6 damage. Deals 3 additional damage for each card in your Exhaust Pile.
    //   Upgraded Effect: Deal 6 damage. Deals 4 additional damage for each card in your Exhaust Pile.

    // TODO: Battle Trance (Uncommon) - 0 energy, Skill
    //   Effect: Draw 3 cards. You cannot draw additional cards this turn.
    //   Upgraded Effect: Draw 4 cards. You cannot draw additional cards this turn.

    // TODO: Bludgeon (Uncommon) - 3 energy, Attack
    //   Effect: Deal 32 damage.
    //   Upgraded Effect: Deal 42 damage.

    // TODO: Bully (Uncommon) - 0 energy, Attack
    //   Effect: Deal 4 damage. Deals 2 additional damage for each Vulnerable on the enemy.
    //   Upgraded Effect: Deal 4 damage. Deals 3 additional damage for each Vulnerable on the enemy.

    // TODO: Burning Pact (Uncommon) - 1 energy, Skill
    //   Effect: Exhaust 1 card. Draw 2 cards.
    //   Upgraded Effect: Exhaust 1 card. Draw 3 cards.

    // TODO: Demonic Shield (Uncommon) - 0 energy, Skill
    //   Effect: Lose 1 HP. Give another player Block equal to your Block. Exhaust.
    //   Upgraded Effect: Lose 1 HP. Give another player Block equal to your Block.

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

    // TODO: Feel No Pain (Uncommon) - 1 energy, Power
    //   Effect: Whenever a card is Exhausted, gain 3 Block.
    //   Upgraded Effect: Whenever a card is Exhausted, gain 4 Block.

    // TODO: Fight Me! (Uncommon) - 2 energy, Attack
    //   Effect: Deal 5 damage twice. Gain 2 Strength. The enemy gains 1 Strength.
    //   Upgraded Effect: Deal 6 damage twice. Gain 3 Strength. The enemy gains 1 Strength.

    // TODO: Flame Barrier (Uncommon) - 2 energy, Skill
    //   Effect: Gain 12 Block. Whenever you are attacked this turn, deal 4 damage back.
    //   Upgraded Effect: Gain 16 Block. Whenever you are attacked this turn, deal 6 damage back.

    // TODO: Forgotten Ritual (Uncommon) - 1 energy, Skill
    //   Effect: If you Exhausted a card this turn, gain 3 energy.
    //   Upgraded Effect: If you Exhausted a card this turn, gain 4 energy.

    // TODO: Grapple (Uncommon) - 1 energy, Attack
    //   Effect: Deal 7 damage. Whenever you gain Block this turn, deal 5 damage to the enemy.
    //   Upgraded Effect: Deal 9 damage. Whenever you gain Block this turn, deal 7 damage to the enemy.

    // TODO: Hemokinesis (Uncommon) - 1 energy, Attack
    //   Effect: Lose 2 HP. Deal 14 damage.
    //   Upgraded Effect: Lose 2 HP. Deal 19 damage.

    // TODO: Howl from Beyond (Uncommon) - 3 energy, Attack
    //   Effect: Deal 16 damage to ALL enemies. At the start of your turn, plays from the Exhaust Pile.
    //   Upgraded Effect: Deal 21 damage to ALL enemies. At the start of your turn, plays from the Exhaust Pile.

    // TODO: Infernal Blade (Uncommon) - 1 energy, Skill
    //   Effect: Add a random Attack into your Hand. It's free to play this turn. Exhaust.
    //   Upgraded Effect (0 energy): Add a random Attack into your Hand. It's free to play this turn. Exhaust.

    // TODO: Inferno (Uncommon) - 1 energy, Power
    //   Effect: At the start of your turn, lose 1 HP. Whenever you lose HP on your turn, deal 6 damage to ALL enemies.
    //   Upgraded Effect: At the start of your turn, lose 1 HP. Whenever you lose HP on your turn, deal 9 damage to ALL enemies.

    // TODO: Inflame (Uncommon) - 1 energy, Power
    //   Effect: Gain 2 Strength.
    //   Upgraded Effect: Gain 3 Strength.

    // TODO: Juggling (Uncommon) - 1 energy, Power
    //   Effect: Add a copy of the third Attack you play each turn into your Hand.
    //   Upgraded Effect: Innate. Add a copy of the third Attack you play each turn into your Hand.

    // TODO: Pillage (Uncommon) - 1 energy, Attack
    //   Effect: Deal 6 damage. Draw cards until you draw a non-Attack card.
    //   Upgraded Effect: Deal 9 damage. Draw cards until you draw a non-Attack card.

    // TODO: Rage (Uncommon) - 0 energy, Skill
    //   Effect: Whenever you play an Attack this turn, gain 3 Block.
    //   Upgraded Effect: Whenever you play an Attack this turn, gain 5 Block.

    // TODO: Rampage (Uncommon) - 1 energy, Attack
    //   Effect: Deal 9 damage. Increase this card's damage by 5 this combat.
    //   Upgraded Effect: Deal 9 damage. Increase this card's damage by 9 this combat.

    // TODO: Rupture (Uncommon) - 1 energy, Power
    //   Effect: Whenever you lose HP on your turn, gain 1 Strength.
    //   Upgraded Effect: Whenever you lose HP on your turn, gain 2 Strength.

    // TODO: Second Wind (Uncommon) - 1 energy, Skill
    //   Effect: Exhaust all non-Attack cards in your Hand. Gain 5 Block for each card Exhausted.
    //   Upgraded Effect: Exhaust all non-Attack cards in your Hand. Gain 7 Block for each card Exhausted.

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

    // TODO: Taunt (Uncommon) - 1 energy, Skill
    //   Effect: Gain 7 Block. Apply 1 Vulnerable.
    //   Upgraded Effect: Gain 8 Block. Apply 2 Vulnerable.

    // TODO: Unrelenting (Uncommon) - 2 energy, Attack
    //   Effect: Deal 12 damage. The next Attack you play costs 0 energy.
    //   Upgraded Effect: Deal 18 damage. The next Attack you play costs 0 energy.

    // TODO: Uppercut (Uncommon) - 2 energy, Attack
    //   Effect: Deal 13 damage. Apply 1 Weak. Apply 1 Vulnerable.
    //   Upgraded Effect: Deal 13 damage. Apply 2 Weak. Apply 2 Vulnerable.

    // TODO: Vicious (Uncommon) - 1 energy, Power
    //   Effect: Whenever you apply Vulnerable, draw 1 card.
    //   Upgraded Effect: Whenever you apply Vulnerable, draw 2 cards.

    // TODO: Whirlwind (Uncommon) - X energy, Attack
    //   Effect: Deal 5 damage to ALL enemies X times.
    //   Upgraded Effect: Deal 8 damage to ALL enemies X times.

    // **************************************************************************************************
    // *********************************************  Rare  *********************************************
    // **************************************************************************************************

    // TODO: Aggression (Rare) - 1 energy, Power
    //   Effect: At the start of your turn, put a random Attack from your Discard Pile into your Hand and Upgrade it.
    //   Upgraded Effect: Innate. At the start of your turn, put a random Attack from your Discard Pile into your Hand and Upgrade it.

    // TODO: Barricade (Rare) - 3 energy, Power
    //   Effect: Block is not removed at the start of your turn.
    //   Upgraded Effect (2 energy): Block is not removed at the start of your turn.

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

    // TODO: Dark Embrace (Rare) - 2 energy, Power
    //   Effect: Whenever a card is Exhausted, draw 1 card.
    //   Upgraded Effect (1 energy): Whenever a card is Exhausted, draw 1 card.

    // TODO: Demon Form (Rare) - 3 energy, Power
    //   Effect: At the start of your turn, gain 2 Strength.
    //   Upgraded Effect: At the start of your turn, gain 3 Strength.

    // TODO: Feed (Rare) - 1 energy, Attack
    //   Effect: Deal 10 damage. If Fatal, raise your Max HP by 3. Exhaust.
    //   Upgraded Effect: Deal 12 damage. If Fatal, raise your Max HP by 4. Exhaust.

    // TODO: Fiend Fire (Rare) - 2 energy, Attack
    //   Effect: Exhaust your Hand. Deal 7 damage for each card Exhausted. Exhaust.
    //   Upgraded Effect: Exhaust your Hand. Deal 10 damage for each card Exhausted. Exhaust.

    // TODO: Hellraiser (Rare) - 2 energy, Power
    //   Effect: Whenever you draw a card containing "Strike", it is played against a random enemy.
    //   Upgraded Effect (1 energy): Whenever you draw a card containing "Strike", it is played against a random enemy.

    // TODO: Impervious (Rare) - 2 energy, Skill
    //   Effect: Gain 30 Block. Exhaust.
    //   Upgraded Effect: Gain 40 Block. Exhaust.

    // TODO: Juggernaut (Rare) - 2 energy, Power
    //   Effect: Whenever you gain Block, deal 5 damage to a random enemy.
    //   Upgraded Effect: Whenever you gain Block, deal 7 damage to a random enemy.

    // TODO: Mangle (Rare) - 3 energy, Attack
    //   Effect: Deal 15 damage. Enemy loses 10 Strength this turn.
    //   Upgraded Effect: Deal 20 damage. Enemy loses 15 Strength this turn.

    // TODO: Offering (Rare) - 0 energy, Skill
    //   Effect: Lose 6 HP. Gain 2 energy. Draw 3 cards. Exhaust.
    //   Upgraded Effect: Lose 6 HP. Gain 2 energy. Draw 5 cards. Exhaust.

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

    // TODO: Clash (Event) - 0 energy, Attack
    //   Effect: Can only be played if every card in your Hand is an Attack. Deal 14 damage.
    //   Upgraded Effect: Can only be played if every card in your Hand is an Attack. Deal 18 damage.

    // TODO: Dual Wield (Event) - 1 energy, Skill
    //   Effect: Choose an Attack or Power card. Add a copy of that card into your Hand.
    //   Upgraded Effect: Choose an Attack or Power card. Add 2 copies of that card into your Hand.

    // TODO: Entrench (Event) - 2 energy, Skill
    //   Effect: Double your Block.
    //   Upgraded Effect (1 energy): Double your Block.

    // **************************************************************************************************
    // ********************************************* Ancient *********************************************
    // **************************************************************************************************

    // TODO: Break (Ancient) - 2 energy, Attack
    //   Effect: Deal 20 damage. Apply 5 Vulnerable.
    //   Upgraded Effect: Deal 25 damage. Apply 7 Vulnerable.

    // TODO: Corruption (Ancient) - 3 energy, Power
    //   Effect: Skills cost 0 energy. Whenever you play a Skill, Exhaust it.
    //   Upgraded Effect (2 energy): Skills cost 0 energy. Whenever you play a Skill, Exhaust it.
}
