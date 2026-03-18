package com.alphaStS.entity;

import com.alphaStS.GameState;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.random.RandomGenCtx;
import com.alphaStS.utils.Tuple;

public class Potion2 {
    // **************************************************************************************************
    // ********************************************* Common *********************************************
    // **************************************************************************************************

    public static class AttackPotion extends Potion.AttackPotion {
    }

    public static class BlockPotion extends Potion.BlockPotion {
    }

    public static class ColorlessPotion extends Potion.ColorlessPotion {
    }

    public static class DexterityPotion extends Potion.DexterityPotion {
    }

    public static class EnergyPotion extends Potion.EnergyPotion {
    }

    public static class ExplosiveAmpoule extends Potion.ExplosivePotion {
    }

    public static class FirePotion extends Potion.FirePotion {
    }

    public static class FlexPotion extends Potion.FlexPotion {
    }

    public static class PowerPotion extends Potion.PowerPotion {
    }

    public static class SkillPotion extends Potion.SkillPotion {
    }

    public static class SpeedPotion extends Potion.SpeedPotion {
    }

    public static class StrengthPotion extends Potion.StrengthPotion {
    }

    public static class SwiftPotion extends Potion.SwiftPotion {
    }

    public static class VulnerablePotion extends Potion.FearPotion {
    }

    public static class WeakPotion extends Potion.WeakPotion {
    }

    // **************************************************************************************************
    // ********************************************* Uncommon *********************************************
    // **************************************************************************************************

    public static class BlessingOfTheForge extends Potion.BlessingOfTheForge {
    }

    // TODO: Clarity Extract (Uncommon)
    //   Effect: Draw 1 card. At the start of your next 3 turns, draw 1 additional card.

    // TODO: Cure All (Uncommon)
    //   Effect: Gain energy. Draw 2 cards.

    public static class Duplicator extends Potion.DuplicationPotion {
    }

    // TODO: Fortifier (Uncommon)
    //   Effect: Triple your Block.

    // TODO: Fysh Oil (Uncommon)
    //   Effect: Gain 1 Strength and 1 Dexterity.

    public static class GamblersBrew extends Potion.GamblersBrew {
    }

    // TODO: Heart of Iron (Uncommon)
    //   Effect: Gain 7 Plating.

    public static class LiquidBronze extends Potion.LiquidBronze {
    }

    // TODO: Potion of Binding (Uncommon)
    //   Effect: Apply 1 Weak and 1 Vulnerable to ALL enemies.

    // TODO: Powdered Demise (Uncommon)
    //   Effect: Enemy loses 9 HP at the end of each of its turns.

    // TODO: Radiant Tincture (Uncommon)
    //   Effect: Gain 2 energy. Gain an additional energy at the start of your next 3 turns.

    public static class RegenPotion extends Potion.RegenerationPotion {
    }

    // TODO: Stable Serum (Uncommon)
    //   Effect: Retain your Hand for 2 turns.

    // TODO: Touch of Insanity (Uncommon)
    //   Effect: Choose a card in your Hand. It is free to play this combat.

    // **************************************************************************************************
    // *********************************************  Rare  *********************************************
    // **************************************************************************************************

    // TODO: Beetle Juice (Rare)
    //   Effect: Enemy's attacks deal 30% less damage for the next 4 turns.

    // TODO: Bottled Potential (Rare)
    //   Effect: Shuffle ALL your cards into your Draw Pile. Draw 5 cards.

    public static class DistilledChaos extends Potion.DistilledChaos {
    }

    // TODO: Droplet of Precognition (Rare)
    //   Effect: Choose a card in your Draw Pile and add it into your Hand.

    public static class EntropicBrew extends Potion.EntropicBrew {
    }

    public static class FairyInABottle extends Potion.FairyInABottle {
    }

    // TODO: Fruit Juice (Rare)
    //   Effect: Gain 5 Max HP.

    // TODO: Gigantification Potion (Rare)
    //   Effect: The next Attack you play deals triple damage.

    public static class LiquidMemories extends Potion.LiquidMemory {
    }

    // TODO: Lucky Tonic (Rare)
    //   Effect: Gain 1 Buffer.

    // TODO: Mazaleth's Gift (Rare)
    //   Effect: Gain 1 Ritual.

    // TODO: Orobic Acid (Rare)
    //   Effect: Add a random Attack, Skill, and Power into your Hand. They're free to play this turn.

    // TODO: Shackling Potion (Rare)
    //   Effect: ALL enemies lose 7 Strength this turn.

    // TODO: Ship in a Bottle (Rare)
    //   Effect: Gain 10 Block. Next turn, gain 10 more Block.

    // STS2 Snecko Oil draws 7 cards (up from 5 in STS1).
    public static class SneckoOil extends Potion.SneckoOil {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.draw(7);
            for (int i = 0; i < state.handArrLen; i++) {
                var snecko = state.properties.sneckoIdxes[state.getHandArrForRead()[i]];
                if (snecko[0] > 1) {
                    state.getHandArrForWrite()[i] = (short) snecko[state.getSearchRandomGen().nextInt(snecko[0], RandomGenCtx.Snecko, new Tuple<>(state, state.getHandArrForRead()[i])) + 1];
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // **************************************************************************************************
    // ********************************************* Event  *********************************************
    // **************************************************************************************************

    // TODO: Foul Potion (Event)
    //   Effect: Deal 12 damage to EVERYONE. Can be thrown at the Merchant for 100 Gold instead.

    // TODO: Glowwater Potion (Event)
    //   Effect: Exhaust your Hand. Draw 10 cards.

    // **************************************************************************************************
    // ********************************************* Token  *********************************************
    // **************************************************************************************************

    // TODO: Potion-Shaped Rock (Token)
    //   Effect: Deal 15 damage.

    // **************************************************************************************************
    // ********************************************* Ironclad *********************************************
    // **************************************************************************************************

    // TODO: Ashwater (Uncommon)
    //   Effect: Exhaust any number of cards in your Hand.

    public static class BloodPotion extends Potion.BloodPotion {
    }

    // TODO: Soldier's Stew (Rare)
    //   Effect: All cards containing Strike gain 1 Replay this combat.

    // **************************************************************************************************
    // ********************************************* Silent *********************************************
    // **************************************************************************************************

    public static class CunningPotion extends Potion.CunningPotion {
    }

    public static class GhostInAJar extends Potion.GhostInAJar {
    }

    public static class PoisonPotion extends Potion.PoisonPotion {
    }

    // **************************************************************************************************
    // ********************************************* Defect *********************************************
    // **************************************************************************************************

    public static class EssenceOfDarkness extends Potion.EssenceOfDarkness {
    }

    public static class FocusPotion extends Potion.FocusPotion {
    }

    public static class PotionOfCapacity extends Potion.PotionOfCapacity {
    }

    // **************************************************************************************************
    // ********************************************* Regent *********************************************
    // **************************************************************************************************

    // TODO: Cosmic Concoction (Rare)
    //   Effect: Add 3 Upgrade Colorless cards into your Hand.

    // TODO: King's Courage (Uncommon)
    //   Effect: Forge 15.

    // TODO: Star Potion (Common)
    //   Effect: Gain 3 star

    // **************************************************************************************************
    // ********************************************* Necrobinder *********************************************
    // **************************************************************************************************

    // TODO: Bone Brew (Uncommon)
    //   Effect: Summon 15.

    // TODO: Pot of Ghouls (Rare)
    //   Effect: Add 2 Souls into your Hand.

    // TODO: Potion of Doom (Common)
    //   Effect: Apply 33 Doom.
}
