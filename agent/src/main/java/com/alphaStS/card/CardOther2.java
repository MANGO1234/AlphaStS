package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.gameAction.GameActionCtx;

public class CardOther2 {
    // **************************************************************************************************
    // ********************************************* Status *********************************************
    // **************************************************************************************************

    public static class Beckon extends Card {
        public Beckon() {
            super("Beckon", Card.STATUS, 1, Card.COMMON);
            alwaysDiscard = true;
        }

        @Override public void onDiscardEndOfTurn(GameState state, int numCardsInHand) {
            state.doNonAttackDamageToPlayer(6, false, this);
        }
    }

    public static class Burn extends CardOther.Burn {}

    public static class Dazed extends CardOther.Dazed {}

    public static class Debris extends Card {
        public Debris() {
            super("Debris", Card.STATUS, 1, Card.COMMON);
            exhaustWhenPlayed = true;
        }
    }

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

    public static class Infection extends Card {
        public Infection() {
            super("Infection", Card.STATUS, -1, Card.COMMON);
            alwaysDiscard = true;
        }

        @Override public void onDiscardEndOfTurn(GameState state, int numCardsInHand) {
            state.doNonAttackDamageToPlayer(3, true, this);
        }
    }

    // TODO: Mind Rot (Status) - Unplayable energy, Status
    //   Effect: Draw 1 fewer card each turn.
    //   No upgrade.

    // Slimed draws 1 card when played, unlike STS1 which only exhausts.
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

    // TODO: Sloth (Status) - Unplayable energy, Status
    //   Effect: You cannot play more than 3 cards each turn.
    //   No upgrade.

    public static class Soot extends Card {
        public Soot() {
            super("Soot", Card.STATUS, -1, Card.COMMON);
        }
    }

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

    public static class Void extends CardOther.Void {}

    // TODO: Waste Away (Status) - Unplayable energy, Status
    //   Effect: Gain 1 less energy per turn.
    //   No upgrade.

    public static class Wound extends CardOther.Wound {}

    // **************************************************************************************************
    // ********************************************* Curse  *********************************************
    // **************************************************************************************************

    public static class AscendersBane extends CardOther.AscendersBane {}

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

    public static class Clumsy extends CardOther.Clumsy {}

    public static class CurseOfTheBell extends CardOther.CurseOfTheBell {}

    public static class Decay extends CardOther.Decay {}

    // TODO: Debt (Curse) - Unplayable energy, Curse
    //   Effect: Unplayable. At the end of your turn, if this is in your Hand, lose 10 Gold.
    //   No upgrade.

    public static class Doubt extends CardOther.Doubt {}

    // TODO: Enthralled (Curse) - 2 energy, Curse
    //   Effect: If this is in your Hand, it must be played before other cards. Eternal.
    //   No upgrade.

    public static class Folly extends Card {
        public Folly() {
            super("Folly", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
            innate = true;
        }
    }

    public static class Greed extends Card {
        public Greed() {
            super("Greed", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
        }
    }

    // TODO: Guilty (Curse) - Unplayable energy, Curse
    //   Effect: Unplayable. Removed from your Deck after 5 combats.
    //   No upgrade.

    public static class Injury extends CardOther.Injury {}

    public static class Normality extends CardOther.Normality {}

    public static class PoorSleep extends Card {
        public PoorSleep() {
            super("Poor Sleep", Card.CURSE, -1, Card.COMMON);
            exhaustWhenPlayed = true;
            retain = true;
        }
    }

    public static class Regret extends CardOther.Regret {}

    public static class Shame extends CardOther.Shame {}

    public static class SporesMind extends Card {
        public SporesMind() {
            super("Spore Mind", Card.CURSE, 1, Card.COMMON);
            exhaustWhenPlayed = true;
        }
    }

    public static class Writhe extends CardOther.Writhe {}

    // **************************************************************************************************
    // ********************************************* Quest  *********************************************
    // **************************************************************************************************

    // TODO: Byrdonis Egg (Quest) - Unplayable energy, Quest
    //   Effect: Unplayable. Can be hatched at a Rest Site.
    //   No upgrade.

    // TODO: Lantern Key (Quest) - Unplayable energy, Quest
    //   Effect: Unplayable. Unlocks a special event in the next Act.
    //   No upgrade.

    // TODO: Spoils Map (Quest) - Unplayable energy, Quest
    //   Effect: Unplayable. Marks a site of 600 extra Gold in the next Act.
    //   No upgrade.
}
