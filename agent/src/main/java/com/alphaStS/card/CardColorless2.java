package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.gameAction.GameActionCtx;

public class CardColorless2 {
    // **************************************************************************************************
    // ********************************************* Uncommon *********************************************
    // **************************************************************************************************

    // TODO: Automation (Uncommon) - 1 energy, Power
    //   Effect: Every 10 cards you draw, gain energy.
    //   Upgraded Effect (0 energy): Every 10 cards you draw, gain energy.

    // TODO: Believe in You (Uncommon) - 0 energy, Skill
    //   Effect: Another player gains 3 energy.
    //   Upgraded Effect: Another player gains 4 energy.

    // TODO: Catastrophe (Uncommon) - 2 energy, Skill
    //   Effect: Play 2 random cards from your Draw Pile.
    //   Upgraded Effect: Play 3 random cards from your Draw Pile.

    // TODO: Coordinate (Uncommon) - 1 energy, Skill
    //   Effect: Give another player 5 Strength this turn.
    //   Upgraded Effect: Give another player 8 Strength this turn.

    public static class DarkShackles extends CardColorless.DarkShackles {
    }

    public static class DarkShacklesP extends CardColorless.DarkShacklesP {
    }

    public static class Discovery extends CardColorless.Discovery {
    }

    public static class DiscoveryP extends CardColorless.DiscoveryP {
    }

    public static class DramaticEntrance extends CardColorless._DramaticEntranceT {
        public DramaticEntrance() {
            super("Dramatic Entrance", 11);
        }
    }

    public static class DramaticEntranceP extends CardColorless._DramaticEntranceT {
        public DramaticEntranceP() {
            super("Dramatic Entrance+", 15);
        }
    }

    // TODO: Equilibrium (Uncommon) - 2 energy, Skill
    //   Effect: Gain 13 Block. Retain your Hand this turn.
    //   Upgraded Effect: Gain 16 Block. Retain your Hand this turn.

    // TODO: Fasten (Uncommon) - 1 energy, Power
    //   Effect: Gain an additional 5 Block from Defend cards.
    //   Upgraded Effect: Gain an additional 7 Block from Defend cards.

    public static class Finesse extends CardColorless._FinesseT {
        public Finesse() {
            super("Finesse", 4);
        }
    }

    public static class FinesseP extends CardColorless._FinesseT {
        public FinesseP() {
            super("Finesse+", 7);
        }
    }

    // TODO: Fisticuffs (Uncommon) - 1 energy, Attack
    //   Effect: Deal 7 damage. Gain Block equal to damage dealt.
    //   Upgraded Effect: Deal 9 damage. Gain Block equal to damage dealt.

    public static class FlashOfSteel extends CardColorless._FlashOfSteelT {
        public FlashOfSteel() {
            super("Flash Of Steel", 5);
        }
    }

    public static class FlashOfSteelP extends CardColorless._FlashOfSteelT {
        public FlashOfSteelP() {
            super("Flash Of Steel+", 8);
        }
    }

    // TODO: Gang Up (Uncommon) - 1 energy, Attack
    //   Effect: Deal 5 damage. Deals 5 additional damage for each time another player has attacked the enemy this turn.
    //   Upgraded Effect: Deal 5 damage. Deals 7 additional damage for each time another player has attacked the enemy this turn.

    // TODO: Huddle Up (Uncommon) - 1 energy, Skill
    //   Effect: ALL allies draw 2 cards.
    //   Upgraded Effect: ALL allies draw 3 cards.

    public static class Impatience extends CardColorless.Impatience {
    }

    public static class ImpatienceP extends CardColorless.ImpatienceP {
    }

    // TODO: Intercept (Uncommon) - 1 energy, Skill
    //   Effect: Gain 9 Block. Redirect all incoming attacks that would be dealt to another player this turn to you.
    //   Upgraded Effect: Gain 13 Block. Redirect all incoming attacks that would be dealt to another player this turn to you.

    public static class JackOfAllTrades extends CardColorless.JackOfAllTrades {
    }

    public static class JackOfAllTradesP extends CardColorless.JackOfAllTradesP {
    }

    // TODO: Lift (Uncommon) - 1 energy, Skill
    //   Effect: Give another player 11 Block.
    //   Upgraded Effect: Give another player 16 Block.

    public static class MindBlast extends CardColorless._MindBlastT {
        public MindBlast() {
            super("Mind Blast", 1);
        }
    }

    public static class MindBlastP extends CardColorless._MindBlastT {
        public MindBlastP() {
            super("Mind Blast+", 0);
        }
    }

    // TODO: Omnislice (Uncommon) - 0 energy, Attack
    //   Effect: Deal 8 damage. Damage ALL other enemies equal to the damage dealt.
    //   Upgraded Effect: Deal 11 damage. Damage ALL other enemies equal to the damage dealt.

    public static class Panache extends CardColorless.Panache {
    }

    public static class PanacheP extends CardColorless.PanacheP {
    }

    public static class PanicButton extends CardColorless.PanicButton {
    }

    public static class PanicButtonP extends CardColorless.PanicButtonP {
    }

    // TODO: Prep Time (Uncommon) - 1 energy, Power
    //   Effect: At the start of your turn, gain 4 Vigor.
    //   Upgraded Effect: At the start of your turn, gain 6 Vigor.

    // TODO: Production (Uncommon) - 0 energy, Skill
    //   Effect: Gain 2 energy. Exhaust.
    //   Upgraded Effect: Gain 2 energy.

    // TODO: Prolong (Uncommon) - 0 energy, Skill
    //   Effect: Next turn, gain Block equal to your current Block. Exhaust.
    //   Upgraded Effect: Next turn, gain Block equal to your current Block.

    // TODO: Prowess (Uncommon) - 1 energy, Power
    //   Effect: Gain 1 Strength. Gain 1 Dexterity.
    //   Upgraded Effect: Gain 2 Strength. Gain 2 Dexterity.

    public static class Purity extends CardColorless.Purity {
        public Purity() {
            retain = true;
        }
    }

    public static class PurityP extends CardColorless.PurityP {
        public PurityP() {
            retain = true;
        }
    }

    // TODO: Restlessness (Uncommon) - 0 energy, Skill
    //   Effect: Retain. If your Hand is empty, draw 2 cards and gain 2 energy.
    //   Upgraded Effect: Retain. If your Hand is empty, draw 3 cards and gain 3 energy.

    // TODO: Seeker Strike (Uncommon) - 1 energy, Attack
    //   Effect: Deal 6 damage. Choose 1 of 3 cards in your Draw Pile to add into your Hand.
    //   Upgraded Effect: Deal 9 damage. Choose 1 of 3 cards in your Draw Pile to add into your Hand.

    public static class Shockwave extends CardIronclad.Shockwave {
    }

    public static class ShockwaveP extends CardIronclad.ShockwaveP {
    }

    // TODO: Splash (Uncommon) - 1 energy, Skill
    //   Effect: Choose 1 of 3 random Attacks from another character to add into your Hand. It's free to play this turn.
    //   Upgraded Effect: Choose 1 of 3 random Upgraded Attacks from another character to add into your Hand. It's free to play this turn.

    // TODO: Stratagem (Uncommon) - 1 energy, Power
    //   Effect: Whenever you shuffle your Draw Pile, choose a card from it to put into your Hand.
    //   Upgraded Effect (0 energy): Whenever you shuffle your Draw Pile, choose a card from it to put into your Hand.

    // TODO: Tag Team (Uncommon) - 2 energy, Attack
    //   Effect: Deal 11 damage. The next Attack another player plays on the enemy is played an extra time.
    //   Upgraded Effect: Deal 15 damage. The next Attack another player plays on the enemy is played an extra time.

    public static class TheBomb extends CardColorless.TheBomb {
    }

    public static class TheBombP extends CardColorless.TheBombP {
    }

    public static class ThinkingAhead extends CardColorless.ThinkingAhead {
    }

    public static class ThinkingAheadP extends CardColorless.ThinkingAheadP {
    }

    // TODO: Thrumming Hatchet (Uncommon) - 1 energy, Attack
    //   Effect: Deal 11 damage. At the start of your next turn, return this to your Hand.
    //   Upgraded Effect: Deal 14 damage. At the start of your next turn, return this to your Hand.

    private static abstract class _UltimateDefendT extends Card {
        private final int block;

        public _UltimateDefendT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class UltimateDefend extends _UltimateDefendT {
        public UltimateDefend() {
            super("Ultimate Defend", 11);
        }
    }

    public static class UltimateDefendP extends _UltimateDefendT {
        public UltimateDefendP() {
            super("Ultimate Defend+", 15);
        }
    }

    private static abstract class _UltimateStrikeT extends Card {
        private final int damage;

        public _UltimateStrikeT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class UltimateStrike extends _UltimateStrikeT {
        public UltimateStrike() {
            super("Ultimate Strike", 14);
        }
    }

    public static class UltimateStrikeP extends _UltimateStrikeT {
        public UltimateStrikeP() {
            super("Ultimate Strike+", 20);
        }
    }

    // TODO: Volley (Uncommon) - X energy, Attack
    //   Effect: Deal 10 damage to a random enemy X times.
    //   Upgraded Effect: Deal 14 damage to a random enemy X times.

    // **************************************************************************************************
    // *********************************************  Rare  *********************************************
    // **************************************************************************************************

    // TODO: Alchemize (Rare) - 1 energy, Skill
    //   Effect: Procure a random potion. Exhaust.
    //   Upgraded Effect (0 energy): Procure a random potion. Exhaust.

    // TODO: Anointed (Rare) - 1 energy, Skill
    //   Effect: Put every Rare card from your Draw Pile into your Hand. Exhaust.
    //   Upgraded Effect: Retain. Put every Rare card from your Draw Pile into your Hand. Exhaust.

    // TODO: Beacon of Hope (Rare) - 1 energy, Power
    //   Effect: Whenever you gain Block on your turn, other players gain half that much Block.
    //   Upgraded Effect: Innate. Whenever you gain Block on your turn, other players gain half that much Block.

    // TODO: Beat Down (Rare) - 3 energy, Skill
    //   Effect: Play 3 random Attacks from your Discard Pile.
    //   Upgraded Effect: Play 4 random Attacks from your Discard Pile.

    // TODO: Bolas (Rare) - 0 energy, Attack
    //   Effect: Deal 3 damage. At the start of your next turn, return this to your Hand.
    //   Upgraded Effect: Deal 4 damage. At the start of your next turn, return this to your Hand.

    // TODO: Calamity (Rare) - 3 energy, Power
    //   Effect: Whenever you play an Attack, add a random Attack into your Hand.
    //   Upgraded Effect (2 energy): Whenever you play an Attack, add a random Attack into your Hand.

    // TODO: Entropy (Rare) - 1 energy, Power
    //   Effect: At the start of your turn, Transform 1 card in your Hand.
    //   Upgraded Effect: Innate. At the start of your turn, Transform 1 card in your Hand.

    private static abstract class _EternalArmorT extends Card {
        private final int amount;

        public _EternalArmorT(String cardName, int amount) {
            super(cardName, Card.POWER, 3, Card.RARE);
            this.amount = amount;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[state.properties.platingCounterIdx] += amount;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerPlatingCounter();
        }
    }

    public static class EternalArmor extends _EternalArmorT {
        public EternalArmor() {
            super("Eternal Armor", 7);
        }
    }

    public static class EternalArmorP extends _EternalArmorT {
        public EternalArmorP() {
            super("Eternal Armor+", 9);
        }
    }

    // TODO: Gold Axe (Rare) - 1 energy, Attack
    //   Effect: Deal damage equal to the number of cards played this combat.
    //   Upgraded Effect: Retain. Deal damage equal to the number of cards played this combat.

    // TODO: Hand of Greed (Rare) - 2 energy, Attack
    //   Effect: Deal 20 damage. If Fatal, gain 20 Gold.
    //   Upgraded Effect: Deal 25 damage. If Fatal, gain 25 Gold.

    // TODO: Hidden Gem (Rare) - 1 energy, Skill
    //   Effect: A random card in your Draw Pile gains Replay 2.
    //   Upgraded Effect: A random card in your Draw Pile gains Replay 3.

    // TODO: Jackpot (Rare) - 3 energy, Attack
    //   Effect: Deal 25 damage. Add 3 random 0 energy cards into your Hand.
    //   Upgraded Effect: Deal 30 damage. Add 3 random Upgraded 0 energy cards into your Hand.

    // TODO: Knockdown (Rare) - 3 energy, Attack
    //   Effect: Deal 10 damage. The enemy takes double damage from other players this turn.
    //   Upgraded Effect: Deal 14 damage. The enemy takes triple damage from other players this turn.

    public static class MasterOfStrategy extends CardColorless.MasterOfStrategy {
    }

    public static class MasterOfStrategyP extends CardColorless.MasterOfStrategyP {
    }

    public static class Mayhem extends CardColorless.Mayhem {
    }

    public static class MayhemP extends CardColorless.MayhemP {
    }

    // TODO: Mimic (Rare) - 1 energy, Skill
    //   Effect: Gain Block equal to the Block on another player. Exhaust.
    //   Upgraded Effect: Gain Block equal to the Block on another player.

    // TODO: Nostalgia (Rare) - 1 energy, Power
    //   Effect: The first Attack or Skill you play each turn is placed on top of your Draw Pile.
    //   Upgraded Effect (0 energy): The first Attack or Skill you play each turn is placed on top of your Draw Pile.

    // TODO: Rally (Rare) - 2 energy, Skill
    //   Effect: ALL players gain 12 Block.
    //   Upgraded Effect: ALL players gain 17 Block.

    // TODO: Rend (Rare) - 2 energy, Attack
    //   Effect: Deal 15 damage. Deals 5 additional damage for each unique debuff on the enemy.
    //   Upgraded Effect: Deal 18 damage. Deals 8 additional damage for each unique debuff on the enemy.

    // TODO: Rolling Boulder (Rare) - 3 energy, Power
    //   Effect: At the start of your turn, deal 5 damage to ALL enemies and increase this damage by 5.
    //   Upgraded Effect: At the start of your turn, deal 10 damage to ALL enemies and increase this damage by 5.

    // TODO: Salvo (Rare) - 1 energy, Attack
    //   Effect: Deal 12 damage. Retain your Hand this turn.
    //   Upgraded Effect: Deal 16 damage. Retain your Hand this turn.

    // TODO: Scrawl (Rare) - 1 energy, Skill
    //   Effect: Draw cards until your Hand is full. Exhaust.
    //   Upgraded Effect: Retain. Draw cards until your Hand is full. Exhaust.

    public static class SecretTechnique extends CardColorless.SecretTechnique {
    }

    public static class SecretTechniqueP extends CardColorless.SecretTechniqueP {
    }

    public static class SecretWeapon extends CardColorless.SecretWeapon {
    }

    public static class SecretWeaponP extends CardColorless.SecretWeaponP {
    }

    // TODO: The Gambit (Rare) - 0 energy, Skill
    //   Effect: Gain 50 Block. If you take unblocked attack damage this combat, die.
    //   Upgraded Effect: Gain 75 Block. If you take unblocked attack damage this combat, die.

    // **************************************************************************************************
    // ********************************************* Event  *********************************************
    // **************************************************************************************************

    private static abstract class _ByrdSwoopT extends Card {
        private final int damage;

        public _ByrdSwoopT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ByrdSwoop extends _ByrdSwoopT {
        public ByrdSwoop() {
            super("Byrd Swoop", 14);
        }
    }

    public static class ByrdSwoopP extends _ByrdSwoopT {
        public ByrdSwoopP() {
            super("Byrd Swoop+", 18);
        }
    }

    public static class Enlightenment extends CardColorless.Enlightenment {
    }

    public static class EnlightenmentP extends CardColorless.EnlightenmentP {
    }

    private static abstract class _ExterminateT extends Card {
        private final int damage;

        public _ExterminateT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                for (int i = 0; i < 4; i++) {
                    state.playerDoDamageToEnemy(enemy, damage);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Exterminate extends _ExterminateT {
        public Exterminate() {
            super("Exterminate", 3);
        }
    }

    public static class ExterminateP extends _ExterminateT {
        public ExterminateP() {
            super("Exterminate+", 4);
        }
    }

    // TODO: Feeding Frenzy (Event) - 0 energy, Skill
    //   Effect: Gain 5 Strength this turn.
    //   Upgraded Effect: Gain 7 Strength this turn.

    public static class Metamorphosis extends CardColorless.Metamorphosis {
    }

    public static class MetamorphosisP extends CardColorless.MetamorphosisP {
    }

    private static abstract class _PeckT extends Card {
        private final int damage;
        private final int hits;

        public _PeckT(String cardName, int damage, int hits) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
            this.hits = hits;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            for (int i = 0; i < hits; i++) {
                state.playerDoDamageToEnemy(enemy, damage);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Peck extends _PeckT {
        public Peck() {
            super("Peck", 2, 3);
        }
    }

    public static class PeckP extends _PeckT {
        public PeckP() {
            super("Peck+", 2, 4);
        }
    }

    private static abstract class _SquashT extends Card {
        private final int damage;
        private final int vulnerable;

        public _SquashT(String cardName, int damage, int vulnerable) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
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

    public static class Squash extends _SquashT {
        public Squash() {
            super("Squash", 10, 2);
        }
    }

    public static class SquashP extends _SquashT {
        public SquashP() {
            super("Squash+", 12, 3);
        }
    }

    // TODO: Toric Toughness (Event) - 2 energy, Skill
    //   Effect: Gain 5 Block. Gain 5 Block at the start of the next 2 turns.
    //   Upgraded Effect: Gain 7 Block. Gain 7 Block at the start of the next 2 turns.

    // **************************************************************************************************
    // ********************************************* Ancient *********************************************
    // **************************************************************************************************

    public static class Apotheosis extends CardColorless.Apotheosis {
        public Apotheosis() {
            innate = true;
        }
    }

    public static class ApotheosisP extends CardColorless.ApotheosisP {
        public ApotheosisP() {
            innate = true;
        }
    }

    public static class Apparition extends CardColorless.Apparition {
    }

    public static class ApparitionP extends CardColorless.ApparitionP {
    }

    // TODO: Brightest Flame (Ancient) - 0 energy, Skill
    //   Effect: Gain 2 energy. Draw 2 cards. Lose 1 Max HP.
    //   Upgraded Effect: Gain 3 energy. Draw 3 cards. Lose 1 Max HP.

    // TODO: Maul (Ancient) - 1 energy, Attack
    //   Effect: Deal 5 damage twice. Increase the damage of ALL Maul cards by 1 this combat.
    //   Upgraded Effect: Deal 6 damage twice. Increase the damage of ALL Maul cards by 2 this combat.

    // TODO: Neow's Fury (Ancient) - 1 energy, Attack
    //   Effect: Deal 10 damage. Put 2 random cards from your Discard Pile into your Hand. Exhaust.
    //   Upgraded Effect: Deal 14 damage. Put 2 random cards from your Discard Pile into your Hand. Exhaust.

    // TODO: Relax (Ancient) - 3 energy, Skill
    //   Effect: Gain 15 Block. Next turn, draw 2 cards and gain 2 energy. Exhaust.
    //   Upgraded Effect: Gain 17 Block. Next turn, draw 3 cards and gain 3 energy. Exhaust.

    // TODO: Whistle (Ancient) - 3 energy, Attack
    //   Effect: Deal 33 damage. Stun the enemy. Exhaust.
    //   Upgraded Effect: Deal 44 damage. Stun the enemy. Exhaust.

    private static abstract class _WishT extends Card {
        public _WishT(String cardName, boolean retain, boolean exhaustWhenPlayed) {
            super(cardName, Card.SKILL, 0, Card.RARE);
            this.retain = retain;
            this.exhaustWhenPlayed = exhaustWhenPlayed;
            entityProperty.selectFromDeck = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.removeCardFromDeck(idx, false);
            state.addCardToHand(idx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Wish extends _WishT {
        public Wish() {
            super("Wish", false, true);
        }
    }

    public static class WishP extends _WishT {
        public WishP() {
            super("Wish+", true, true);
        }
    }

    // **************************************************************************************************
    // ********************************************* Token  *********************************************
    // **************************************************************************************************

    private static abstract class _FuelT extends Card {
        private final int draws;

        public _FuelT(String cardName, int draws) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.draws = draws;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(1);
            state.draw(draws);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Fuel extends _FuelT {
        public Fuel() {
            super("Fuel", 1);
        }
    }

    public static class FuelP extends _FuelT {
        public FuelP() {
            super("Fuel+", 2);
        }
    }

    private static abstract class _GiantRockT extends Card {
        private final int damage;

        public _GiantRockT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class GiantRock extends _GiantRockT {
        public GiantRock() {
            super("Giant Rock", 16);
        }
    }

    public static class GiantRockP extends _GiantRockT {
        public GiantRockP() {
            super("Giant Rock+", 20);
        }
    }

    // TODO: Luminesce (Token) - 0 energy, Skill
    //   Effect: Retain. Gain 2 energy. Exhaust.
    //   Upgraded Effect: Retain. Gain 3 energy. Exhaust.

    // TODO: Minion Dive Bomb (Token) - 1 energy, Attack
    //   Effect: Deal 13 damage. Exhaust.
    //   Upgraded Effect: Deal 16 damage. Exhaust.

    // TODO: Minion Sacrifice (Token) - 0 energy, Skill
    //   Effect: Gain 9 Block. Exhaust.
    //   Upgraded Effect: Gain 12 Block. Exhaust.

    // TODO: Minion Strike (Token) - 0 energy, Attack
    //   Effect: Deal 7 damage. Draw 1 card. Exhaust.
    //   Upgraded Effect: Deal 10 damage. Draw 1 card. Exhaust.

    // TODO: Shiv (Token) - 0 energy, Attack
    //   Effect: Deal 4 damage. Exhaust.
    //   Upgraded Effect: Deal 6 damage. Exhaust.

    // TODO: Soul (Token) - 0 energy, Skill
    //   Effect: Draw 2 cards. Exhaust.
    //   Upgraded Effect: Draw 3 cards. Exhaust.

    // TODO: Sovereign Blade (Token) - 2 energy, Attack
    //   Effect: Retain. Deal 10 damage.
    //   Upgraded Effect (1 energy): Retain. Deal 10 damage.

    // TODO: Sweeping Gaze (Token) - 0 energy, Attack
    //   Effect: Ethereal. Osty deals 10 damage to a random enemy. Exhaust.
    //   Upgraded Effect: Ethereal. Osty deals 15 damage to a random enemy. Exhaust.
}
