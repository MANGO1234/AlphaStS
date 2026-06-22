package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.gameAction.GameActionCtx;

public class CardOther2 {
    // **************************************************************************************************
    // ********************************************* Status *********************************************
    // **************************************************************************************************

    // Beckon (Status) - 1 energy, Status
    //   Effect: At the end of your turn, if this is in your Hand, lose 6 HP.
    public static class Beckon extends Card {
        public Beckon() {
            super("Beckon", Card.STATUS, 1, Card.COMMON);
            alwaysDiscard = true;
        }

        @Override public void onDiscardEndOfTurn(GameState state, int numCardsInHand) {
            state.doNonAttackDamageToPlayer(6, false, this);
        }
    }

    // Burn (Status) - Unplayable, Status
    //   Effect: Unplayable. At the end of your turn, if this is in your Hand, take 2 damage.
    public static class Burn extends CardOther.Burn {}

    // Dazed (Status) - Unplayable, Status
    //   Effect: Unplayable. Ethereal.
    public static class Dazed extends CardOther.Dazed {}

    // Debris (Status) - 1 energy, Status
    //   Effect: Exhaust.
    public static class Debris extends Card {
        public Debris() {
            super("Debris", Card.STATUS, 1, Card.COMMON);
            exhaustWhenPlayed = true;
        }
    }

    // Disintegration (Status) - Unplayable, Status
    //   Effect: At the end of your turn, take 6 damage.
    // TODO CHANGED: Disintegration (Status) - Unplayable, Status
    //   Effect: At the end of your turn, take 6/7/8 damage.
    //   No upgrade.
    public static class Disintegration extends Card {
        public Disintegration() {
            super("Disintegration", Card.STATUS, -1, Card.COMMON);
            alwaysDiscard = true;
        }

        @Override public void onDiscardEndOfTurn(GameState state, int numCardsInHand) {
            state.doNonAttackDamageToPlayer(6, true, this);
        }
    }

    // TODO: Frantic Escape (Status) - 1 energy, Status
    //   Effect: Get farther away. Increase Sandpit by 1. Increase the cost of this card by 1.
    //   No upgrade.

    // Infection (Status) - Unplayable, Status
    //   Effect: Unplayable. At the end of your turn, if this is in your Hand, take 3 damage.
    public static class Infection extends Card {
        public Infection() {
            super("Infection", Card.STATUS, -1, Card.COMMON);
            alwaysDiscard = true;
        }

        @Override public void onDiscardEndOfTurn(GameState state, int numCardsInHand) {
            state.doNonAttackDamageToPlayer(3, true, this);
        }
    }

    // TODO: Mind Rot (Status) - Unplayable, Status
    //   Effect: Draw 1 fewer card each turn.
    //   No upgrade.

    // Slimed draws 1 card when played, unlike STS1 which only exhausts.
    // Slimed (Status) - 1 energy, Status
    //   Effect: Draw 1 card. Exhaust.
    public static class Slimed extends Card {
        public Slimed() {
            super("Slimed", Card.STATUS, 1, Card.COMMON);
            exhaustWhenPlayed = true;
        }

        @Override public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // TODO: Sloth (Status) - Unplayable, Status
    //   Effect: You cannot play more than 3 cards each turn.
    //   No upgrade.

    // Soot (Status) - Unplayable, Status
    //   Effect: Unplayable.
    public static class Soot extends Card {
        public Soot() {
            super("Soot", Card.STATUS, -1, Card.COMMON);
        }
    }

    // Toxic (Status) - 1 energy, Status
    //   Effect: At the end of your turn, if this is in your Hand, take 5 damage. Exhaust.
    public static class Toxic extends Card {
        public Toxic() {
            super("Toxic", Card.STATUS, 1, Card.COMMON);
            exhaustWhenPlayed = true;
            alwaysDiscard = true;
        }

        @Override public void onDiscardEndOfTurn(GameState state, int numCardsInHand) {
            state.doNonAttackDamageToPlayer(5, true, this);
        }
    }

    // Void (Status) - Unplayable, Status
    //   Effect: Unplayable. Ethereal. Whenever you draw this card, lose energy.
    public static class Void extends CardOther.Void {}

    // TODO: Waste Away (Status) - Unplayable, Status
    //   Effect: Gain 1 less energy per turn.
    //   No upgrade.

    // Wound (Status) - Unplayable, Status
    //   Effect: Unplayable.
    public static class Wound extends CardOther.Wound {}

    // TODO: Wither (Status) - Unplayable, Status
    //   Effect: Unplayable. At the end of your turn, if this is in your Hand, take 3 damage.
    //   Upgraded Effect: Unplayable. At the end of your turn, if this is in your Hand, take 6 damage.

    // **************************************************************************************************
    // ********************************************* Curse  *********************************************
    // **************************************************************************************************

    // Ascender's Bane (Curse) - Unplayable, Curse
    //   Effect: Unplayable. Ethereal. Eternal.
    public static class AscendersBane extends CardOther.AscendersBane {}

    // Bad Luck (Curse) - Unplayable, Curse
    //   Effect: Unplayable. At the end of your turn, if this is in your Hand, lose 13 HP. Eternal.
    public static class BadLuck extends Card {
        public BadLuck() {
            super("Bad Luck", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
            alwaysDiscard = true;
        }

        @Override public void onDiscardEndOfTurn(GameState state, int numCardsInHand) {
            state.doNonAttackDamageToPlayer(13, false, this);
        }
    }

    // Clumsy (Curse) - Unplayable, Curse
    //   Effect: Unplayable. Ethereal.
    public static class Clumsy extends CardOther.Clumsy {}

    // Curse of the Bell (Curse) - Unplayable, Curse
    //   Effect: Unplayable. Eternal.
    public static class CurseOfTheBell extends CardOther.CurseOfTheBell {}

    // Decay (Curse) - Unplayable, Curse
    //   Effect: Unplayable. At the end of your turn, if this is in your Hand, take 2 damage.
    public static class Decay extends CardOther.Decay {}

    // TODO: Debt (Curse) - Unplayable, Curse
    //   Effect: Unplayable. At the end of your turn, if this is in your Hand, lose 10 Gold.
    //   No upgrade.

    // Doubt (Curse) - Unplayable, Curse
    //   Effect: Unplayable. At the end of your turn, if this is in your Hand, gain 1 Weak.
    public static class Doubt extends CardOther.Doubt {}

    // Enthralled (Curse) - 2 energy, Curse
    //   Effect: If this is in your Hand, it must be played before other cards. Eternal.
    public static class Enthralled extends Card {
        public Enthralled() {
            super("Enthralled", Card.CURSE, 2, Card.COMMON);
        }
    }

    // Folly (Curse) - Unplayable, Curse
    //   Effect: Unplayable. Innate. Eternal.
    // TODO CHANGED: Folly (Curse) - Unplayable, Curse
    //   Effect: Unplayable. Ethereal. Innate. Eternal.
    //   No upgrade.
    public static class Folly extends Card {
        public Folly() {
            super("Folly", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
            innate = true;
        }
    }

    // Greed (Curse) - Unplayable, Curse
    //   Effect: Unplayable. Eternal.
    public static class Greed extends Card {
        public Greed() {
            super("Greed", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
        }
    }

    // TODO: Guilty (Curse) - Unplayable, Curse
    //   Effect: Unplayable. Removed from your Deck after 5 combats.
    //   No upgrade.

    // Injury (Curse) - Unplayable, Curse
    //   Effect: Unplayable.
    public static class Injury extends CardOther.Injury {}

    // Normality (Curse) - Unplayable, Curse
    //   Effect: Unplayable. You cannot play more than 3 cards this turn.
    public static class Normality extends CardOther.Normality {}

    // Poor Sleep (Curse) - Unplayable, Curse
    //   Effect: Unplayable. Retain.
    public static class PoorSleep extends Card {
        public PoorSleep() {
            super("Poor Sleep", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
            retain = true;
        }
    }

    // Regret (Curse) - Unplayable, Curse
    //   Effect: Unplayable. At the end of your turn, if this is in your Hand, lose 1 HP for each card in your Hand.
    public static class Regret extends CardOther.Regret {}

    // Shame (Curse) - Unplayable, Curse
    //   Effect: Unplayable. At the end of your turn, if this is in your Hand, gain 1 Frail.
    public static class Shame extends CardOther.Shame {}

    public static class SporesMind extends Card {
        public SporesMind() {
            super("Spore Mind", Card.CURSE, 1, Card.COMMON);
            exhaustWhenPlayed = true;
        }
    }

    // Writhe (Curse) - Unplayable, Curse
    //   Effect: Unplayable. Innate.
    public static class Writhe extends CardOther.Writhe {}

    // **************************************************************************************************
    // ********************************************* Quest  *********************************************
    // **************************************************************************************************

    // TODO: Byrdonis Egg (Quest) - Unplayable, Quest
    //   Effect: Unplayable. Can be hatched at a Rest Site.
    //   No upgrade.

    // TODO: Lantern Key (Quest) - Unplayable, Quest
    //   Effect: Unplayable. Unlocks a special event in the next Act.
    //   No upgrade.

    // TODO: Spoils Map (Quest) - Unplayable, Quest
    //   Effect: Unplayable. Marks a site of 600 extra Gold in the next Act.
    //   No upgrade.
}
