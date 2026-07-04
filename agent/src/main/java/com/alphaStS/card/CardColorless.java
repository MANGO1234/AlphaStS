package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.enemy.*;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.random.RandomGenCtx;
import com.alphaStS.utils.Tuple;
import com.alphaStS.utils.Utils;
import com.alphaStS.utils.CounterStat;

import java.util.List;
import java.util.Objects;

public class CardColorless {
    private static abstract class _BandageUpT extends Card {
        private final int heal;

        public _BandageUpT(String cardName, int heal) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.heal = heal;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.healPlayer(heal);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Bandage Up (Uncommon) - 0 energy, Skill
    //   Effect: Heal 4 HP. Exhaust.
    //   Upgraded Effect: Heal 6 HP. Exhaust.
    public static class BandageUp extends _BandageUpT {
        public BandageUp() {
            super("Bandage Up", 4);
        }
    }

    public static class BandageUpP extends _BandageUpT {
        public BandageUpP() {
            super("Bandage Up+", 6);
        }
    }

    // Blind (Uncommon) - 0 energy, Skill
    //   Effect: Apply 2 Weak.
    //   Upgraded Effect: Apply 2 Weak to ALL enemies.
    public static class Blind extends Card {
        public Blind() {
            super("Blind", Card.SKILL, 0, Card.UNCOMMON);
            entityProperty.selectEnemy = true;
            entityProperty.weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BlindP extends Card {
        public BlindP() {
            super("Blind+", Card.SKILL, 0, Card.UNCOMMON);
            entityProperty.weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.WEAK, 2);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _DarkShacklesT extends Card {
        private final int n;

        public _DarkShacklesT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.n = n;
            this.exhaustWhenPlayed = true;
            entityProperty.selectEnemy = true;
            this.entityProperty.affectEnemyStrength = true;
            this.entityProperty.affectEnemyStrengthEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Dark Shackles (Uncommon) - 0 energy, Skill
    //   Effect: Enemy loses 9 Strength this turn. Exhaust.
    //   Upgraded Effect: Enemy loses 15 Strength this turn. Exhaust.
    public static class DarkShackles extends _DarkShacklesT {
        public DarkShackles() {
            super("Dark Shackles", 9);
        }
    }

    public static class DarkShacklesP extends _DarkShacklesT {
        public DarkShacklesP() {
            super("Dark Shackles+", 15);
        }
    }

    private static abstract class _DeepBreathT extends Card {
        private final int n;

        public _DeepBreathT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.reshuffle();
            state.draw(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Deep Breath (Uncommon) - 0 energy, Skill
    //   Effect: Shuffle your discard pile into your draw pile. Draw 1 card.
    //   Upgraded Effect: Shuffle your discard pile into your draw pile. Draw 2 cards.
    public static class DeepBreath extends _DeepBreathT {
        public DeepBreath() {
            super("Deep Breath", 1);
        }
    }

    public static class DeepBreathP extends _DeepBreathT {
        public DeepBreathP() {
            super("Deep Breath+", 2);
        }
    }

    private static abstract class _DiscoveryT extends Card {
        public _DiscoveryT(String cardName, boolean exhausts) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.exhaustWhenPlayed = exhausts;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if ((state.properties.generateCardOptions & GameProperties.GENERATE_CARD_DISCOVERY) == 0) {
                return GameActionCtx.PLAY_CARD;
            }
            state.setSelect1OutOf3Idxes(generatedCardIdxes);
            return GameActionCtx.SELECT_CARD_1_OUT_OF_3;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            return getPossibleSelect1OutOf3Cards(gameProperties);
        }

        public List<Card> getPossibleSelect1OutOf3Cards(GameProperties gameProperties) {
            if ((gameProperties.generateCardOptions & GameProperties.GENERATE_CARD_DISCOVERY) == 0) {
                return List.of();
            }
            return CardManager.getCharacterCardsTmp0Cost(gameProperties.character, false);
        }
    }

    // Discovery (Uncommon) - 1 energy, Skill
    //   Effect: Choose 1 of 3 random cards to add into your hand. It costs 0 this turn. Exhaust.
    //   Upgraded Effect: Choose 1 of 3 random cards to add into your hand. It costs 0 this turn.
    public static class Discovery extends _DiscoveryT {
        public Discovery() {
            super("Discovery", true);
        }
    }

    public static class DiscoveryP extends _DiscoveryT {
        public DiscoveryP() {
            super("Discovery+", false);
        }
    }

    static abstract class _DramaticEntranceT extends Card {
        private final int n;

        public _DramaticEntranceT(String cardName, int n) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.n = n;
            exhaustWhenPlayed = true;
            innate = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, n, this);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Dramatic Entrance (Uncommon) - 0 energy, Attack
    //   Effect: Innate. Deal 8 damage to ALL enemies. Exhaust.
    //   Upgraded Effect: Innate. Deal 12 damage to ALL enemies. Exhaust.
    public static class DramaticEntrance extends _DramaticEntranceT {
        public DramaticEntrance() {
            super("Dramatic Entrance", 8);
        }
    }

    public static class DramaticEntranceP extends _DramaticEntranceT {
        public DramaticEntranceP() {
            super("Dramatic Entrance+", 12);
        }
    }

    private static abstract class _EnlightenmentT extends Card {
        private final boolean upgraded;

        public _EnlightenmentT(String cardName, boolean upgraded) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.upgraded = upgraded;
        }

        public GameActionCtx play(GameState state, int _idx, int energyUsed) {
            if ((state.properties.generateCardOptions & GameProperties.GENERATE_CARD_ENLIGHTENMENT) == 0) {
                return GameActionCtx.PLAY_CARD;
            }
            for (int i = 0; i < state.getNumCardsInHand(); i++) {
                int cardIdx = state.getHandArrForRead()[i];
                if (!state.properties.cardDict[cardIdx].isXCost && state.properties.cardDict[cardIdx].energyCost > 1) {
                    if (upgraded) {
                        state.modifyCardInHandByPosition(i, state.properties.findCardIndex(state.properties.cardDict[cardIdx].getPermCostIfPossible(1))); // todo
                    } else {
                        state.modifyCardInHandByPosition(i, state.properties.findCardIndex(state.properties.cardDict[cardIdx].getTemporaryCostIfPossible(1))); // todo
                    }
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            if ((properties.generateCardOptions & GameProperties.GENERATE_CARD_ENLIGHTENMENT) == 0) {
                return List.of();
            }
            if (upgraded) {
                return cards.stream().map((card) -> card.getPermCostIfPossible(1)).toList();
            } else {
                return cards.stream().map((card) -> card.getTemporaryCostIfPossible(1)).toList();
            }
        }
    }

    // Enlightenment (Uncommon) - 0 energy, Skill
    //   Effect: Reduce the cost of all cards in your hand to 1 this turn.
    //   Upgraded Effect: Reduce the cost of all cards in your hand to 1 this combat.
    public static class Enlightenment extends _EnlightenmentT {
        public Enlightenment() {
            super("Enlightenment", false);
        }
    }

    public static class EnlightenmentP extends _EnlightenmentT {
        public EnlightenmentP() {
            super("Enlightenment+", true);
        }
    }

    static abstract class _FinesseT extends Card {
        private final int n;

        public _FinesseT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(n);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Finesse (Uncommon) - 0 energy, Skill
    //   Effect: Gain 2 Block. Draw 1 card.
    //   Upgraded Effect: Gain 4 Block. Draw 1 card.
    public static class Finesse extends _FinesseT {
        public Finesse() {
            super("Finesse", 2);
        }
    }

    public static class FinesseP extends _FinesseT {
        public FinesseP() {
            super("Finesse+", 4);
        }
    }

    static abstract class _FlashOfSteelT extends Card {
        private final int n;

        public _FlashOfSteelT(String cardName, int n) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Flash of Steel (Uncommon) - 0 energy, Attack
    //   Effect: Deal 3 damage. Draw 1 card.
    //   Upgraded Effect: Deal 6 damage. Draw 1 card.
    public static class FlashOfSteel extends _FlashOfSteelT {
        public FlashOfSteel() {
            super("Flash of Steel", 3);
        }
    }

    public static class FlashOfSteelP extends _FlashOfSteelT {
        public FlashOfSteelP() {
            super("Flash of Steel+", 5);
        }
    }

    private static abstract class _ForethoughtT extends Card {
        private final int cardCount;

        public _ForethoughtT(String cardName, int cardCount) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.cardCount = cardCount;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            // TODO: Implement placing card(s) from hand on bottom of draw pile with 0 cost
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Forethought (Uncommon) - 0 energy, Skill
    //   Effect: Put a card from your hand to the bottom of your draw pile. It costs 0 until played.
    //   Upgraded Effect: Put any number of cards from your hand to the bottom of your draw pile. They cost 0 until played.
    public static class Forethought extends _ForethoughtT {
        public Forethought() {
            super("Forethought", 1);
        }
    }

    public static class ForethoughtP extends _ForethoughtT {
        public ForethoughtP() {
            super("Forethought+", 2);
        }
    }

    private static abstract class _GoodInstinctsT extends Card {
        private final int n;

        public _GoodInstinctsT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Good Instincts (Uncommon) - 0 energy, Skill
    //   Effect: Gain 6 Block.
    //   Upgraded Effect: Gain 9 Block.
    public static class GoodInstincts extends _GoodInstinctsT {
        public GoodInstincts() {
            super("Good Instincts", 6);
        }
    }

    public static class GoodInstinctsP extends _GoodInstinctsT {
        public GoodInstinctsP() {
            super("Good Instincts+", 9);
        }
    }

    private static abstract class _ImpatienceT extends Card {
        private final int n;

        public _ImpatienceT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < state.handArrLen; i++) {
                if (state.properties.cardDict[state.getHandArrForRead()[i]].cardType == Card.ATTACK) {
                    return GameActionCtx.PLAY_CARD;
                }
            }
            state.draw(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Impatience (Uncommon) - 0 energy, Skill
    //   Effect: If you have no Attacks in your hand, draw 2 cards.
    //   Upgraded Effect: If you have no Attacks in your hand, draw 3 cards.
    public static class Impatience extends _ImpatienceT {
        public Impatience() {
            super("Impatience", 2);
        }
    }

    public static class ImpatienceP extends _ImpatienceT {
        public ImpatienceP() {
            super("Impatience+", 3);
        }
    }

    private static abstract class _JackOfAllTradesT extends Card {
        private final int numCards;

        public _JackOfAllTradesT(String cardName, int numCards) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.numCards = numCards;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < numCards; i++) {
                int randomIdx = state.getSearchRandomGen().nextInt(generatedCardIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, generatedCardIdxes));
                int cardIdx = generatedCardIdxes[randomIdx];
                state.addCardToHand(cardIdx, true);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            return CardManager.getColorlessCards(false);
        }
    }

    // Jack of All Trades (Uncommon) - 0 energy, Skill
    //   Effect: Add 1 random Colorless card into your hand. Exhaust.
    //   Upgraded Effect: Add 2 random Colorless cards into your hand. Exhaust.
    public static class JackOfAllTrades extends _JackOfAllTradesT {
        public JackOfAllTrades() {
            super("Jack of All Trades", 1);
        }
    }

    public static class JackOfAllTradesP extends _JackOfAllTradesT {
        public JackOfAllTradesP() {
            super("Jack of All Trades+", 2);
        }
    }

    private static abstract class _MadnessT extends Card {
        public _MadnessT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int _idx, int energyUsed) {
            if ((state.properties.generateCardOptions & GameProperties.GENERATE_CARD_MADNESS) == 0) {
                return GameActionCtx.PLAY_CARD;
            }
            int possibleCards = 0, diff = 0, idx = -1;
            var hand = GameStateUtils.getCardArrCounts(state.getHandArrForRead(), state.handArrLen, state.properties.cardDict.length);
            for (int i = 0; i < state.properties.realCardsLen; i++) {
                if (hand[i] > 0 && !state.properties.cardDict[i].isXCost && state.properties.cardDict[i].energyCost > 0) {
                    possibleCards += hand[i];
                    diff += 1;
                    idx = i;
                }
            }
            if (possibleCards == 0) {
                return GameActionCtx.PLAY_CARD;
            }
            if (diff > 1) {
                state.setIsStochastic();
                var r = state.getSearchRandomGen().nextInt(possibleCards, RandomGenCtx.RandomCardHandMummifiedHand, state);
                var acc = 0;
                for (int i = 0; i < state.properties.realCardsLen; i++) {
                    if (hand[i] > 0 && !state.properties.cardDict[i].isXCost && state.properties.cardDict[i].energyCost > 0) {
                        acc += hand[i];
                        if (acc > r) {
                            idx = i;
                            break;
                        }
                    }
                }
            }
            state.removeCardFromHand(idx);
            state.addCardToHand(state.properties.findCardIndex(state.properties.cardDict[idx].getPermCostIfPossible(0)), false); // todo
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            if ((properties.generateCardOptions & GameProperties.GENERATE_CARD_MADNESS) == 0) {
                return List.of();
            }
            return cards.stream().map((card) -> card.getPermCostIfPossible(0)).toList();
        }
    }

    // Madness (Uncommon) - 1 energy, Skill
    //   Effect: Reduce the cost of a random card in your hand to 0 this combat. Exhaust.
    //   Upgraded Effect (0 energy): Reduce the cost of a random card in your hand to 0 this combat. Exhaust.
    public static class Madness extends _MadnessT {
        public Madness() {
            super("Madness", 1);
        }
    }

    public static class MadnessP extends _MadnessT {
        public MadnessP() {
            super("Madness+", 0);
        }
    }

    static abstract class _MindBlastT extends Card {
        public _MindBlastT(String cardName, int energyCost) {
            super(cardName, Card.ATTACK, energyCost, Card.UNCOMMON);
            innate = true;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), state.getNumCardsInDeck(), this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Mind Blast (Uncommon) - 2 energy, Attack
    //   Effect: Innate. Deal damage equal to the number of cards in your draw pile.
    //   Upgraded Effect (1 energy): Innate. Deal damage equal to the number of cards in your draw pile.
    public static class MindBlast extends _MindBlastT {
        public MindBlast() {
            super("Mind Blast", 2);
        }
    }

    public static class MindBlastP extends _MindBlastT {
        public MindBlastP() {
            super("Mind Blast+", 1);
        }
    }

    private static abstract class _PanaceaT extends Card {
        private final int n;

        public _PanaceaT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.n = n;
            entityProperty.changePlayerArtifact = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainArtifact(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Panacea (Uncommon) - 0 energy, Skill
    //   Effect: Gain 1 Artifact. Exhaust.
    //   Upgraded Effect: Gain 2 Artifact. Exhaust.
    public static class Panacea extends _PanaceaT {
        public Panacea() {
            super("Panacea", 1);
        }
    }

    public static class PanaceaP extends _PanaceaT {
        public PanaceaP() {
            super("Panacea+", 2);
        }
    }

    private static abstract class _PanicButtonT extends Card {
        private final int n;

        public _PanicButtonT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.n = n;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(n);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.NO_BLOCK_FROM_CARDS, 2);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerNoMoreBlockFromCardsCounter();
        }
    }

    // Panic Button (Uncommon) - 0 energy, Skill
    //   Effect: Gain 30 Block. You cannot gain Block from cards for 2 turns. Exhaust.
    //   Upgraded Effect: Gain 40 Block. You cannot gain Block from cards for 2 turns. Exhaust.
    public static class PanicButton extends _PanicButtonT {
        public PanicButton() {
            super("Panic Button", 30);
        }
    }

    public static class PanicButtonP extends _PanicButtonT {
        public PanicButtonP() {
            super("Panic Button+", 40);
        }
    }

    private static abstract class _PurityT extends Card implements GameProperties.CounterRegistrant {
        private final int maxExhaust;

        public _PurityT(String cardName, int maxExhaust) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.maxExhaust = maxExhaust;
            this.exhaustWhenPlayed = true;
            this.selectFromHandLater = true;
            this.canExhaustAnyCard = true;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Purity", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = counter[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                // Initial play - start selecting if hand has cards
                state.getCounterForWrite()[counterIdx] = 0;
                return state.getNumCardsInHand() > 0 ? GameActionCtx.SELECT_CARD_HAND : GameActionCtx.PLAY_CARD;
            } else if (idx >= 0 && idx < state.properties.cardDict.length) {
                // A card was selected - exhaust it
                state.exhaustCardFromHand(idx);
                state.getCounterForWrite()[counterIdx]++;

                // If we've exhausted max cards, or no more cards in hand, finish
                if (state.getCounterForRead()[counterIdx] >= maxExhaust || state.getNumCardsInHand() == 0) {
                    state.getCounterForWrite()[counterIdx] = 0;
                    return GameActionCtx.PLAY_CARD;
                }

                // Continue selecting more cards
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                // "End Selection" was chosen - finish the card
                state.getCounterForWrite()[counterIdx] = 0;
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    // Purity (Uncommon) - 0 energy, Skill
    //   Effect: Exhaust up to 3 cards in your hand. Exhaust.
    //   Upgraded Effect: Exhaust up to 5 cards in your hand. Exhaust.
    public static class Purity extends _PurityT {
        public Purity() {
            super("Purity", 3);
        }
    }

    public static class PurityP extends _PurityT {
        public PurityP() {
            super("Purity+", 5);
        }
    }

    private static abstract class _SwiftStrikeT extends Card {
        private final int n;

        public _SwiftStrikeT(String cardName, int n) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Swift Strike (Uncommon) - 0 energy, Attack
    //   Effect: Deal 7 damage.
    //   Upgraded Effect: Deal 10 damage.
    public static class SwiftStrike extends _SwiftStrikeT {
        public SwiftStrike() {
            super("Swift Strike", 7);
        }
    }

    public static class SwiftStrikeP extends _SwiftStrikeT {
        public SwiftStrikeP() {
            super("Swift Strike+", 10);
        }
    }

    // Trip (Uncommon) - 0 energy, Skill
    //   Effect: Apply 2 Vulnerable.
    //   Upgraded Effect: Apply 2 Vulnerable to ALL enemies.
    public static class Trip extends Card {
        public Trip() {
            super("Trip", Card.SKILL, 0, Card.UNCOMMON);
            entityProperty.selectEnemy = true;
            entityProperty.vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.VULNERABLE, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class TripP extends Card {
        public TripP() {
            super("Trip+", Card.SKILL, 0, Card.UNCOMMON);
            entityProperty.vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.VULNERABLE, 2);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _ApotheosisT extends Card {
        private boolean canUpgrade = true;

        public _ApotheosisT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.RARE);
            exhaustWhenPlayed = true;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            canUpgrade = (state.properties.generateCardOptions & GameProperties.GENERATE_CARD_APOTHEOSIS) != 0;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (!canUpgrade) {
                return GameActionCtx.PLAY_CARD;
            }
            state.handArrTransform(state.properties.upgradeIdxes);
            state.discardArrTransform(state.properties.upgradeIdxes);
            state.deckArrTransform(state.properties.upgradeIdxes);
            state.exhaustArrTransform(state.properties.upgradeIdxes);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            if ((properties.generateCardOptions & GameProperties.GENERATE_CARD_APOTHEOSIS) == 0) {
                return List.of();
            }
            return cards.stream().map(Card::getUpgrade).filter(Objects::nonNull).filter((x) -> !x.cardName.equals(this.cardName)).toList();
        }
    }

    // Apotheosis (Rare) - 2 energy, Skill
    //   Effect: Upgrade ALL your cards for the rest of combat. Exhaust.
    //   Upgraded Effect (1 energy): Upgrade ALL your cards for the rest of combat. Exhaust.
    public static class Apotheosis extends CardColorless._ApotheosisT {
        public Apotheosis() {
            super("Apotheosis", 2);
        }
    }

    public static class ApotheosisP extends CardColorless._ApotheosisT {
        public ApotheosisP() {
            super("Apotheosis+", 1);
        }
    }

    private static abstract class _ChrysalisT extends Card {
        private final int skillCount;

        public _ChrysalisT(String cardName, int skillCount) {
            super(cardName, Card.SKILL, 2, Card.RARE);
            this.skillCount = skillCount;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if ((state.properties.generateCardOptions & GameProperties.GENERATE_CARD_CHRYSALIS) == 0) {
                return GameActionCtx.PLAY_CARD;
            }
            for (int i = 0; i < skillCount; i++) {
                state.setIsStochastic();
                int randomIdx = state.getSearchRandomGen().nextInt(generatedCardIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, generatedCardIdxes));
                int cardIdx = generatedCardIdxes[randomIdx];
                state.addCardToDeckRandom(cardIdx);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            if ((gameProperties.generateCardOptions & GameProperties.GENERATE_CARD_CHRYSALIS) == 0) {
                return List.of();
            }
            return CardManager.getCharacterCardsByType(gameProperties.character, Card.SKILL, false)
                    .stream().map((card) -> card.getPermCostIfPossible(0)).toList();
        }
    }

    // Chrysalis (Rare) - 2 energy, Skill
    //   Effect: Shuffle 3 random Skills into your draw pile. They cost 0 this combat. Exhaust.
    //   Upgraded Effect: Shuffle 5 random Skills into your draw pile. They cost 0 this combat. Exhaust.
    public static class Chrysalis extends _ChrysalisT {
        public Chrysalis() {
            super("Chrysalis", 3);
        }
    }

    public static class ChrysalisP extends _ChrysalisT {
        public ChrysalisP() {
            super("Chrysalis+", 5);
        }
    }

    private static abstract class _HandOfGreedT extends Card {
        private int n;
        protected double healthRewardRatio = 0;

        public _HandOfGreedT(String cardName, int n, double healthRewardRatio) {
            super(cardName, Card.ATTACK, 2, Card.RARE);
            this.n = n;
            entityProperty.selectEnemy = true;
            this.healthRewardRatio = healthRewardRatio;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            if (!state.getEnemiesForRead().get(idx).properties.isMinion && state.getEnemiesForRead().get(idx).getHealth() <= 0) {
                if (state.getEnemiesForRead().get(idx) instanceof EnemyBeyond.Darkling ||
                        state.getEnemiesForRead().get(idx) instanceof EnemyBeyond.AwakenedOne) {
                    if (state.isTerminal() > 0) {
                        state.getCounterForWrite()[counterIdx] += n;
                    }
                } else if (!(state.getEnemiesForRead().get(idx) instanceof EnemyEnding.CorruptHeart)) {
                    state.getCounterForWrite()[counterIdx] += n;
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("HandOFGreed", this, healthRewardRatio == 0 ? null : new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 100.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            if (healthRewardRatio > 0) {
                state.properties.addExtraTrainingTarget("HandOFGreed", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                        if (isTerminal > 0) {
                            v.setVExtra(vExtraIdx, state.getCounterForRead()[counterIdx] / 100.0);
                        } else if (isTerminal == 0) {
                            int minHandOfGreed = state.getCounterForRead()[counterIdx];
                            int maxHandOfGreedRemaining = getMaxPossibleHandOfGreedRemaining(state, true);
                            double vHandOfGreed = Math.max(minHandOfGreed / 100.0, Math.min((minHandOfGreed + maxHandOfGreedRemaining) / 100.0, state.getVExtra(vExtraIdx)));
                            v.setVExtra(vExtraIdx, vHandOfGreed);
                        }
                    }

                    @Override public void updateQValues(GameState state, VArray v) {
                        int minHandOfGreed = state.getCounterForRead()[counterIdx];
                        int maxHandOfGreedRemaining = getMaxPossibleHandOfGreedRemaining(state, true);
                        double vHandOfGreed = Math.max(minHandOfGreed / 100.0, Math.min((minHandOfGreed + maxHandOfGreedRemaining) / 100.0, v.getVExtra(vExtraIdx)));
                        if (!state.isEncounter(PredefinedEncounter.CORRUPT_HEART)) {
                            v.add(GameState.V_HEALTH_IDX, 100 * vHandOfGreed * healthRewardRatio / 20.0 / state.getPlayerForRead().getMaxHealth());
                        }
                    }
                });
            }
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            counterIdx = idx;
            gameProperties.handOfGreedCounterIdx = idx;
        }

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, "Hand of Greed").setShowFrequency(true);
        }

        public static int getMaxPossibleHandOfGreedRemaining(GameState state, boolean checkEndOfGame) {
            if (state.properties.handOfGreedCounterIdx < 0) {
                return 0;
            }
            if (checkEndOfGame && state.isTerminal() != 0) {
                return 0;
            }
            int nonUpgrade = state.getNonExhaustCount("Hand of Greed");
            boolean canUpgrade = false; // todo
            if (!canUpgrade) {
                int upgrade = state.getNonExhaustCount("Hand of Greed+");
                if (upgrade > 0) {
                    canUpgrade = true;
                }
            }
            if (nonUpgrade == 0) {
                return 0;
            }
            return canUpgrade ? 25 * state.enemiesAlive : 20 * state.enemiesAlive;
        }

        public static int getMaxPossibleHandOfGreed(GameState state) {
            if (state.properties.handOfGreedCounterIdx < 0) {
                return 0;
            }
            return state.getCounterForRead()[state.properties.handOfGreedCounterIdx] + getMaxPossibleHandOfGreedRemaining(state, false);
        }
    }

    // Hand of Greed (Rare) - 2 energy, Attack
    //   Effect: Deal 20 damage. If Fatal, gain 20 Gold.
    //   Upgraded Effect: Deal 25 damage. If Fatal, gain 25 Gold.
    public static class HandOfGreed extends CardColorless._HandOfGreedT {
        public HandOfGreed() { this(2); }

        public HandOfGreed(double healthRewardRatio) {
            super("Hand of Greed", 20, healthRewardRatio);
        }

        public Card getUpgrade() {
            return new CardColorless.HandOfGreedP(healthRewardRatio);
        }
    }

    public static class HandOfGreedP extends CardColorless._HandOfGreedT {
        public HandOfGreedP() { this(2); }

        public HandOfGreedP(double healthRewardRatio) {
            super("Hand of Greed+", 25, healthRewardRatio);
        }

        @Override public Card getDowngrade() {
            return new CardColorless.HandOfGreed(healthRewardRatio);
        }
    }

    static abstract class _MagnetismT extends Card {
        public _MagnetismT(String cardName, int energyCost, int rarity) {
            super(cardName, Card.POWER, energyCost, rarity);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Magnetism", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });

            state.properties.addStartOfTurnHandler("Magnetism", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    for (int i = 0; i < state.getCounterForRead()[counterIdx]; i++) {
                        state.setIsStochastic();
                        int randomIdx = state.getSearchRandomGen().nextInt(generatedCardIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, generatedCardIdxes));
                        int cardIdx = generatedCardIdxes[randomIdx];
                        state.addCardToHand(cardIdx, true);
                    }
                }
            });
        }

        public List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            return CardManager.getColorlessCards(false);
        }
    }

    // Magnetism (Rare) - 2 energy, Power
    //   Effect: At the start of your turn, add a random Colorless card into your hand.
    //   Upgraded Effect (1 energy): At the start of your turn, add a random Colorless card into your hand.
    public static class Magnetism extends _MagnetismT {
        public Magnetism() {
            super("Magnetism", 2, Card.RARE);
        }
    }

    public static class MagnetismP extends _MagnetismT {
        public MagnetismP() {
            super("Magnetism+", 1, Card.RARE);
        }
    }

    private static abstract class _MasterOfStrategyT extends Card {
        private final int n;

        public _MasterOfStrategyT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.RARE);
            this.n = n;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.draw(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Master of Strategy (Rare) - 0 energy, Skill
    //   Effect: Draw 3 cards. Exhaust.
    //   Upgraded Effect: Draw 4 cards. Exhaust.
    public static class MasterOfStrategy extends CardColorless._MasterOfStrategyT {
        public MasterOfStrategy() {
            super("Master of Strategy", 3);
        }
    }

    public static class MasterOfStrategyP extends CardColorless._MasterOfStrategyT {
        public MasterOfStrategyP() {
            super("Master of Strategy+", 4);
        }
    }

    private static abstract class _MayhemT extends Card {
        public _MayhemT(String cardName, int energyCost) {
            super(cardName, Card.POWER, energyCost, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerPlayCardOnTopOfDeckCounter();
            state.properties.registerCounter("Mayhem", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Mayhem", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] == 0) {
                        return;
                    }
                    state.getCounterForWrite()[state.properties.playCardOnTopOfDeckCounterIdx] += state.getCounterForRead()[counterIdx];
                }
            });
        }
    }

    // Mayhem (Rare) - 2 energy, Power
    //   Effect: At the start of your turn, play the top card of your draw pile.
    //   Upgraded Effect (1 energy): At the start of your turn, play the top card of your draw pile.
    public static class Mayhem extends CardColorless._MayhemT {
        public Mayhem() {
            super("Mayhem", 2);
        }
    }

    public static class MayhemP extends CardColorless._MayhemT {
        public MayhemP() {
            super("Mayhem+", 1);
        }
    }

    private static abstract class _MetamorphosisT extends Card {
        private final int attackCount;

        public _MetamorphosisT(String cardName, int attackCount) {
            super(cardName, Card.SKILL, 2, Card.RARE);
            this.attackCount = attackCount;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if ((state.properties.generateCardOptions & GameProperties.GENERATE_CARD_METAMORPHOSIS) == 0) {
                return GameActionCtx.PLAY_CARD;
            }
            for (int i = 0; i < attackCount; i++) {
                state.setIsStochastic();
                int randomIdx = state.getSearchRandomGen().nextInt(generatedCardIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, generatedCardIdxes));
                int cardIdx = generatedCardIdxes[randomIdx];
                state.addCardToDeckRandom(cardIdx);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            if ((gameProperties.generateCardOptions & GameProperties.GENERATE_CARD_METAMORPHOSIS) == 0) {
                return List.of();
            }
            return CardManager.getCharacterCardsByType(gameProperties.character, Card.ATTACK, false)
                    .stream().map((card) -> card.getPermCostIfPossible(0)).toList();
        }
    }

    // Metamorphosis (Rare) - 2 energy, Skill
    //   Effect: Shuffle 3 random Attacks into your draw pile. They cost 0 this combat. Exhaust.
    //   Upgraded Effect: Shuffle 5 random Attacks into your draw pile. They cost 0 this combat. Exhaust.
    public static class Metamorphosis extends _MetamorphosisT {
        public Metamorphosis() {
            super("Metamorphosis", 3);
        }
    }

    public static class MetamorphosisP extends _MetamorphosisT {
        public MetamorphosisP() {
            super("Metamorphosis+", 5);
        }
    }

    private static abstract class _PanacheT extends Card {
        private final int n;

        public _PanacheT(String cardName, int n) {
            super(cardName, Card.POWER, 0, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n << 3;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Panache", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public String getDisplayString(GameState state) {
                    int counter = state.getCounterForRead()[counterIdx];
                    return (counter & ((1 << 16) - 1)) + "/" + (counter >> 16);
                }
            });
            state.properties.addEndOfTurnHandler("Panache", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] &= ~7;
                }
            });
            state.properties.addOnCardPlayedHandler("Panache", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.getCounterForRead()[counterIdx] >> 3 == 0) {
                        return;
                    }
                    var counter = state.getCounterForWrite();
                    counter[counterIdx]++;
                    if ((counter[counterIdx] & 7) == 5) {
                        counter[counterIdx] -= 5;
                        for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            state.playerDoNonAttackDamageToEnemy(enemy, counter[counterIdx] >> 3, true);
                        }
                    }
                }
            });
        }
    }

    // Panache (Rare) - 0 energy, Power
    //   Effect: Every time you play 5 cards in a single turn, deal 10 damage to ALL enemies.
    //   Upgraded Effect: Every time you play 5 cards in a single turn, deal 14 damage to ALL enemies.
    public static class Panache extends CardColorless._PanacheT {
        public Panache() {
            super("Panache", 10);
        }
    }

    public static class PanacheP extends CardColorless._PanacheT {
        public PanacheP() {
            super("Panache+", 14);
        }
    }

    static abstract class _SadisticNatureT extends Card implements GameProperties.CounterRegistrant {
        private final int damage;

        public _SadisticNatureT(String cardName, int damage) {
            this(cardName, 0, Card.RARE, damage);
        }

        public _SadisticNatureT(String cardName, int energyCost, int rarity, int damage) {
            super(cardName, Card.POWER, energyCost, rarity);
            this.damage = damage;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("SadisticNature", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = counter[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public void onRegister(int counterIdx) {
                    state.properties.sadisticNatureCounterIdx = counterIdx;
                }
            });
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += damage;
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Sadistic Nature (Rare) - 0 energy, Power
    //   Effect: Whenever you apply a debuff to an enemy, they take 5 damage.
    //   Upgraded Effect: Whenever you apply a debuff to an enemy, they take 7 damage.
    public static class SadisticNature extends _SadisticNatureT {
        public SadisticNature() {
            super("Sadistic Nature", 5);
        }
    }

    public static class SadisticNatureP extends _SadisticNatureT {
        public SadisticNatureP() {
            super("Sadistic Nature+", 7);
        }
    }

    private static abstract class _SecretTechniqueT extends Card {
        public _SecretTechniqueT(String cardName, boolean exhaustWhenPlayed) {
            super(cardName, Card.SKILL, 0, Card.RARE);
            this.exhaustWhenPlayed = exhaustWhenPlayed;
            entityProperty.selectFromDeck = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.removeCardFromDeck(idx, false);
            state.addCardToHand(idx, false);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public boolean canSelectCard(Card card) {
            return card.cardType == Card.SKILL;
        }
    }

    // Secret Technique (Rare) - 0 energy, Skill
    //   Effect: Put a Skill from your draw pile into your hand. Exhaust.
    //   Upgraded Effect: Put a Skill from your draw pile into your hand.
    public static class SecretTechnique extends CardColorless._SecretTechniqueT {
        public SecretTechnique() {
            super("Secret Technique", true);
        }
    }

    public static class SecretTechniqueP extends CardColorless._SecretTechniqueT {
        public SecretTechniqueP() {
            super("Secret Technique+", false);
        }
    }

    private static abstract class _SecretWeaponT extends Card {
        public _SecretWeaponT(String cardName, boolean exhaustWhenPlayed) {
            super(cardName, Card.SKILL, 0, Card.RARE);
            this.exhaustWhenPlayed = exhaustWhenPlayed;
            entityProperty.selectFromDeck = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.removeCardFromDeck(idx, false);
            state.addCardToHand(idx, false);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public boolean canSelectCard(Card card) {
            return card.cardType == Card.ATTACK;
        }
    }

    // Secret Weapon (Rare) - 0 energy, Skill
    //   Effect: Put an Attack from your draw pile into your hand. Exhaust.
    //   Upgraded Effect: Put an Attack from your draw pile into your hand.
    public static class SecretWeapon extends CardColorless._SecretWeaponT {
        public SecretWeapon() {
            super("Secret Weapon", true);
        }
    }

    public static class SecretWeaponP extends CardColorless._SecretWeaponT {
        public SecretWeaponP() {
            super("Secret Weapon+", false);
        }
    }

    private static abstract class _TheBombT extends Card {
        private final int n;

        public _TheBombT(String cardName, int n) {
            super(cardName, Card.SKILL, 2, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("TheBomb", this, 3, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    input[idx + 1] = state.getCounterForRead()[counterIdx + 1] / 5.0f;
                    input[idx + 2] = state.getCounterForRead()[counterIdx + 2] / 5.0f;
                    return idx + 3;
                }
                @Override public int getInputLenDelta() {
                    return 3;
                }
            });
            state.properties.addEndOfTurnHandler("TheBomb", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx + 2] > 0) {
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            state.playerDoNonAttackDamageToEnemy(enemy, n, true);
                        }
                        state.getCounterForWrite()[counterIdx + 2] = 0;
                    }
                    if (state.getCounterForRead()[counterIdx + 1] > 0) {
                        state.getCounterForWrite()[counterIdx + 2] = state.getCounterForRead()[counterIdx + 1];
                        state.getCounterForWrite()[counterIdx + 1] = 0;
                    }
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx + 1] = state.getCounterForRead()[counterIdx];
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }

        public int countersNeeded(GameState state) {
            return 3;
        }
    }

    // The Bomb (Rare) - 2 energy, Skill
    //   Effect: At the end of 3 turns, deal 40 damage to ALL enemies.
    //   Upgraded Effect: At the end of 3 turns, deal 50 damage to ALL enemies.
    public static class TheBomb extends CardColorless._TheBombT {
        public TheBomb() {
            super("The Bomb", 40);
        }
    }

    public static class TheBombP extends CardColorless._TheBombT {
        public TheBombP() {
            super("The Bomb+", 50);
        }
    }

    private static abstract class _ThinkingAheadT extends Card {
        public _ThinkingAheadT(String cardName, boolean exhaustWhenPlayed) {
            super(cardName, Card.SKILL, 0, Card.RARE);
            this.exhaustWhenPlayed = exhaustWhenPlayed;
            entityProperty.selectFromHand = true;
            selectFromHandLater = true;
            entityProperty.putCardOnTopDeck = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.draw(2);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.removeCardFromHand(idx);
                state.addCardOnTopOfDeck(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    // Thinking Ahead (Rare) - 0 energy, Skill
    //   Effect: Draw 2 cards. Put a card from your hand on top of your draw pile. Exhaust.
    //   Upgraded Effect: Draw 2 cards. Put a card from your hand on top of your draw pile.
    public static class ThinkingAhead extends CardColorless._ThinkingAheadT {
        public ThinkingAhead() {
            super("Thinking Ahead", true);
        }
    }

    public static class ThinkingAheadP extends CardColorless._ThinkingAheadT {
        public ThinkingAheadP() {
            super("Thinking Ahead+", false);
        }
    }

    private static abstract class _TransmutationT extends Card {
        private final boolean upgraded;

        public _TransmutationT(String cardName, boolean upgraded) {
            super(cardName, Card.SKILL, -1, Card.RARE);
            this.upgraded = upgraded;
            this.exhaustWhenPlayed = true;
            this.isXCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < energyUsed; i++) {
                int randomIdx = state.getSearchRandomGen().nextInt(generatedCardIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, generatedCardIdxes));
                int cardIdx = generatedCardIdxes[randomIdx];
                state.addCardToHand(cardIdx, true);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            return state.energy;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            var colorlessCards = CardManager.getColorlessCardsTmp0Cost(false);
            return upgraded ? colorlessCards.stream().map(Card::getUpgrade).toList() : colorlessCards;
        }
    }

    // Transmutation (Rare) - X energy, Skill
    //   Effect: Add X random Colorless cards into your hand. They cost 0 this turn. Exhaust.
    //   Upgraded Effect: Add X random Upgraded Colorless cards into your hand. They cost 0 this turn. Exhaust.
    public static class Transmutation extends _TransmutationT {
        public Transmutation() {
            super("Transmutation", false);
        }
    }

    public static class TransmutationP extends _TransmutationT {
        public TransmutationP() {
            super("Transmutation+", true);
        }
    }

    private static abstract class _ViolenceT extends Card {
        private final int n;

        public _ViolenceT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.n = n;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var possibleIndexes = new short[state.deckArrLen];
            var len = 0;
            for (int i = 0; i < state.deckArrLen; i++) {
                if (state.properties.cardDict[state.deckArr[i]].cardType == Card.ATTACK) {
                    possibleIndexes[len++] = state.deckArr[i];
                }
            }
            if (len < n) {
                for (int i = 0; i < len; i++) {
                    state.removeCardFromDeck(possibleIndexes[len - 1 - i], false);
                    state.addCardToHand(possibleIndexes[len - 1 - i], false);
                }
            } else {
                state.setIsStochastic();
                Utils.shuffle(state, possibleIndexes, len, len - 3, state.getSearchRandomGen());
                for (int i = 0; i < n; i++) {
                    state.removeCardFromDeck(possibleIndexes[len - 1 - i], true);
                    state.addCardToHand(possibleIndexes[len - 1 - i], false);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Violence (Rare) - 0 energy, Skill
    //   Effect: Put 3 random Attacks from your draw pile into your hand. Exhaust.
    //   Upgraded Effect: Put 4 random Attacks from your draw pile into your hand. Exhaust.
    public static class Violence extends _ViolenceT {
        public Violence() {
            super("Violence", 3);
        }
    }

    public static class ViolenceP extends _ViolenceT {
        public ViolenceP() {
            super("Violence+", 5);
        }
    }

    private static abstract class _BiteT extends Card {
        private final int n;
        private final int heal;

        public _BiteT(String cardName, int n, int heal) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.n = n;
            this.heal = heal;
            entityProperty.selectEnemy = true;
            this.healPlayer = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            state.healPlayer(heal);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Bite (Special) - 1 energy, Attack
    //   Effect: Deal 7 damage. Heal 2 HP.
    //   Upgraded Effect: Deal 8 damage. Heal 3 HP.
    public static class Bite extends _BiteT {
        public Bite() {
            super("Bite", 7, 2);
        }
    }

    public static class BiteP extends _BiteT {
        public BiteP() {
            super("Bite+", 8, 3);
        }
    }

    private static abstract class _RitualDaggerT extends Card {
        protected final int n;
        private final int dmgInc;
        protected final int healthRewardRatio;

        public _RitualDaggerT(String cardName, int n, int dmgInc, int healthRewardRatio) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.n = n;
            this.dmgInc = dmgInc;
            entityProperty.selectEnemy = true;
            this.exhaustWhenPlayed = true;
            this.healthRewardRatio = healthRewardRatio;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            if (!state.getEnemiesForRead().get(idx).properties.isMinion && state.getEnemiesForRead().get(idx).getHealth() <= 0) {
                if (state.getEnemiesForRead().get(idx) instanceof EnemyBeyond.Darkling ||
                        state.getEnemiesForRead().get(idx) instanceof EnemyBeyond.AwakenedOne) {
                    if (state.isTerminal() > 0) {
                        state.getCounterForWrite()[counterIdx] += dmgInc;
                    }
                } else if (!(state.getEnemiesForRead().get(idx) instanceof EnemyEnding.CorruptHeart)) {
                    state.getCounterForWrite()[counterIdx] += dmgInc;
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("RitualDagger", this, healthRewardRatio == 0 ? null : new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 100.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            if (healthRewardRatio > 0) {
                state.properties.addExtraTrainingTarget("RitualDagger", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                        if (isTerminal > 0) {
                            v.setVExtra(vExtraIdx, state.getCounterForRead()[counterIdx] / 10.0);
                        } else if (isTerminal == 0) {
                            int minValue = state.getCounterForRead()[counterIdx];
                            int maxRemaining = getMaxPossibleRitualDaggerRemaining(state, true);
                            double vDagger = Math.max(minValue / 10.0, Math.min((minValue + maxRemaining) / 10.0, state.getVExtra(vExtraIdx)));
                            v.setVExtra(vExtraIdx, vDagger);
                        }
                    }

                    @Override public void updateQValues(GameState state, VArray v) {
                        if (!state.isEncounter(PredefinedEncounter.CORRUPT_HEART)) {
                            int minValue = state.getCounterForRead()[counterIdx];
                            int maxRemaining = getMaxPossibleRitualDaggerRemaining(state, true);
                            double vDagger = Math.max(minValue / 10.0, Math.min((minValue + maxRemaining) / 10.0, v.getVExtra(vExtraIdx)));
                            v.add(GameState.V_HEALTH_IDX, 10 * vDagger * healthRewardRatio / state.getPlayerForRead().getMaxHealth());
                        }
                    }
                });
            }
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            counterIdx = idx;
            gameProperties.ritualDaggerCounterIdx = counterIdx;
        }

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, "Ritual Dagger").setShowFrequency(true);
        }

        public static int getMaxPossibleRitualDaggerRemaining(GameState state, boolean checkEndOfGame) {
            if (state.properties.ritualDaggerCounterIdx < 0) {
                return 0;
            }
            if (checkEndOfGame && state.isTerminal() != 0) {
                return 0;
            }
            int nonUpgrade = state.getNonExhaustCount("Ritual Dagger");
            boolean canUpgrade = false; // todo
            if (!canUpgrade) {
                int upgrade = state.getNonExhaustCount("Ritual Dagger+");
                if (upgrade > 0) {
                    canUpgrade = true;
                }
            }
            if (nonUpgrade == 0) {
                return 0;
            }
            return canUpgrade ? 5 : 3;
        }

        public static int getMaxPossibleRitualDagger(GameState state) {
            if (state.properties.ritualDaggerCounterIdx < 0) {
                return 0;
            }
            return state.getCounterForRead()[state.properties.ritualDaggerCounterIdx] + getMaxPossibleRitualDaggerRemaining(state, false);
        }
    }

    // Ritual Dagger (Special) - 1 energy, Attack
    //   Effect: Deal 15 damage. If Fatal, permanently increase this card's damage by 3. Exhaust.
    //   Upgraded Effect: Deal 15 damage. If Fatal, permanently increase this card's damage by 5. Exhaust.
    public static class RitualDagger extends _RitualDaggerT {
        public RitualDagger() { this(15, 2); }

        public RitualDagger(int dmg, int healthReward) {
            super("Ritual Dagger", dmg, 3, healthReward);
        }

        @Override public Card getUpgrade() {
            return new RitualDaggerP(this.n, healthRewardRatio);
        }
    }

    public static class RitualDaggerP extends _RitualDaggerT {
        public RitualDaggerP() { this(15, 2); }

        public RitualDaggerP(int dmg, int healthReward) {
            super("Ritual Dagger+", dmg, 5, healthReward);
        }

        @Override public Card getDowngrade() {
            return new RitualDagger(this.n, healthRewardRatio);
        }
    }

    private static abstract class _ShivT extends Card {
        private final int n;

        public _ShivT(String cardName, int n) {
            super(cardName, Card.ATTACK, 0, Card.COMMON);
            this.n = n;
            entityProperty.selectEnemy = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int damage = n;
            if (state.properties.phantomBladesCounterIdx >= 0
                    && state.getCounterForRead()[state.properties.phantomBladesCounterIdx] > 0
                    && state.getCounterForRead()[state.properties.phantomBladesCounterIdx + 1] == 0) {
                damage += state.getCounterForRead()[state.properties.phantomBladesCounterIdx];
                state.getCounterForWrite()[state.properties.phantomBladesCounterIdx + 1] = 1;
            }
            if (state.properties.fanOfKnivesCounterIdx >= 0 && state.getCounterForRead()[state.properties.fanOfKnivesCounterIdx] > 0) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    state.playerDoDamageToEnemy(enemy, damage, this);
                }
            } else {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public boolean retain(GameState state) {
            return super.retain(state) || (state.properties.phantomBladesCounterIdx >= 0
                    && state.getCounterForRead()[state.properties.phantomBladesCounterIdx] > 0);
        }
    }

    // Shiv (Special) - 0 energy, Attack
    //   Effect: Deal 4 damage. Exhaust.
    //   Upgraded Effect: Deal 6 damage. Exhaust.
    public static class Shiv extends _ShivT {
        public Shiv() {
            super("Shiv", 4);
        }
    }

    public static class ShivP extends _ShivT {
        public ShivP() {
            super("Shiv+", 6);
        }
    }

    private static abstract class _ApparitionT extends Card {
        public _ApparitionT(String cardName, boolean ethereal) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.ethereal = ethereal;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[state.properties.intangibleCounterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerIntangibleCounter();
        }
    }

    // Apparition (Special) - 1 energy, Skill
    //   Effect: Ethereal. Gain 1 Intangible. Exhaust.
    //   Upgraded Effect: Gain 1 Intangible. Exhaust.
    public static class Apparition extends _ApparitionT {
        public Apparition() {
            super("Apparition", true);
        }
    }

    public static class ApparitionP extends _ApparitionT {
        public ApparitionP() {
            super("Apparition+", false);
        }
    }

    private static abstract class _SmiteT extends Card {
        private final int damage;

        public _SmiteT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            this.retain = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Smite (Special) - 1 energy, Attack
    //   Effect: Retain. Deal 12 damage. Exhaust.
    //   Upgraded Effect: Retain. Deal 16 damage. Exhaust.
    public static class Smite extends _SmiteT {
        public Smite() {
            super("Smite", 12);
        }
    }

    public static class SmiteP extends _SmiteT {
        public SmiteP() {
            super("Smite+", 16);
        }
    }

    private static abstract class _InsightT extends Card {
        private final int drawCount;

        public _InsightT(String cardName, int drawCount) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.drawCount = drawCount;
            this.retain = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.draw(drawCount);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Insight (Special) - 0 energy, Skill
    //   Effect: Retain. Draw 2 cards. Exhaust.
    //   Upgraded Effect: Retain. Draw 3 cards. Exhaust.
    public static class Insight extends _InsightT {
        public Insight() {
            super("Insight", 2);
        }
    }

    public static class InsightP extends _InsightT {
        public InsightP() {
            super("Insight+", 3);
        }
    }

    private static abstract class _SafetyT extends Card {
        private final int block;

        public _SafetyT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.block = block;
            this.retain = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Safety (Special) - 1 energy, Skill
    //   Effect: Retain. Gain 12 Block. Exhaust.
    //   Upgraded Effect: Retain. Gain 16 Block. Exhaust.
    public static class Safety extends _SafetyT {
        public Safety() {
            super("Safety", 12);
        }
    }

    public static class SafetyP extends _SafetyT {
        public SafetyP() {
            super("Safety+", 16);
        }
    }

    private static abstract class _ThroughViolenceT extends Card {
        private final int damage;

        public _ThroughViolenceT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            this.retain = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Through Violence (Special) - 0 energy, Attack
    //   Effect: Retain. Deal 20 damage. Exhaust.
    //   Upgraded Effect: Retain. Deal 30 damage. Exhaust.
    public static class ThroughViolence extends _ThroughViolenceT {
        public ThroughViolence() {
            super("Through Violence", 20);
        }
    }

    public static class ThroughViolenceP extends _ThroughViolenceT {
        public ThroughViolenceP() {
            super("Through Violence+", 30);
        }
    }

    private static abstract class _MiracleT extends Card {
        private final int energyGain;

        public _MiracleT(String cardName, int energyGain) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.energyGain = energyGain;
            this.exhaustWhenPlayed = true;
            this.retain = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(energyGain);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Miracle (Special) - 0 energy, Skill
    //   Effect: Retain. Gain. Exhaust.
    public static class Miracle extends _MiracleT {
        public Miracle() {
            super("Miracle", 1);
        }
    }

    public static class MiracleP extends _MiracleT {
        public MiracleP() {
            super("Miracle+", 2);
        }
    }

    private static abstract class _BetaT extends Card {
        public _BetaT(String cardName, int cost) {
            super(cardName, Card.SKILL, cost, Card.COMMON);
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.addCardToDeckRandom(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new Omega());
        }
    }

    // Beta (Special) - 2 energy, Skill
    //   Effect: Shuffle an Omega into draw pile. Exhaust.
    //   Upgraded Effect (1 energy): Shuffle an Omega into draw pile. Exhaust.
    public static class Beta extends _BetaT {
        public Beta() {
            super("Beta", 2);
        }
    }

    public static class BetaP extends _BetaT {
        public BetaP() {
            super("Beta+", 1);
        }
    }

    private static abstract class _OmegaT extends Card {
        private final int damage;

        public _OmegaT(String cardName, int damage) {
            super(cardName, Card.POWER, 3, Card.COMMON);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += damage;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Omega", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 100.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addEndOfTurnHandler("Omega", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            state.playerDoNonAttackDamageToEnemy(enemy, state.getCounterForRead()[counterIdx], true);
                        }
                    }
                }
            });
        }
    }

    // Omega (Special) - 3 energy, Power
    //   Effect: At the end of your turn, deal 50 damage to ALL enemies.
    //   Upgraded Effect: At the end of your turn, deal 60 damage to ALL enemies.
    public static class Omega extends _OmegaT {
        public Omega() {
            super("Omega", 50);
        }
    }

    public static class OmegaP extends _OmegaT {
        public OmegaP() {
            super("Omega+", 60);
        }
    }

    private static abstract class _ExpungerT extends Card {
        private final int damage;
        protected final int xValue;

        public _ExpungerT(String cardName, int damage, int xValue) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.xValue = xValue;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < xValue; i++) {
                state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Expunger (Special) - X energy, Attack
    //   Effect: Deal 9 damage X times.
    //   Upgraded Effect: Deal 15 damage X times.
    public static class Expunger extends _ExpungerT {
        public Expunger() { this(1); }

        public Expunger(int xValue) {
            super("Expunger (" + xValue + ")", 9, xValue);
        }

        @Override public Card getUpgrade() {
            return new ExpungerP(xValue);
        }
    }

    public static class ExpungerP extends _ExpungerT {
        public ExpungerP() { this(1); }

        public ExpungerP(int xValue) {
            super("Expunger+ (" + xValue + ")", 15, xValue);
        }

        @Override public Card getDowngrade() {
            return new Expunger(xValue);
        }
    }

    // **************************************************************************************************
    // ********************************************* Special *********************************************
    // **************************************************************************************************

    private static abstract class _JAXT extends Card {
        private final int strength;

        public _JAXT(String cardName, int strength) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.strength = strength;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.doNonAttackDamageToPlayer(3, false, this);
            state.playerGainStrength(strength);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // J.A.X. (Special) - 0 energy, Skill
    //   Effect: Lose 3 HP. Gain 2 Strength.
    //   Upgraded Effect: Lose 3 HP. Gain 3 Strength.
    public static class JAX extends _JAXT {
        public JAX() {
            super("J.A.X.", 2);
        }
    }

    public static class JAXP extends _JAXT {
        public JAXP() {
            super("J.A.X.+", 3);
        }
    }
}
