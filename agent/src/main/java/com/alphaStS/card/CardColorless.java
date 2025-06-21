package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyBeyond;
import com.alphaStS.enemy.EnemyEnding;
import com.alphaStS.utils.Tuple;
import com.alphaStS.utils.Utils;

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

    public static class ToBeImplemented extends Card {
        public ToBeImplemented(String a) {
            super("ToBeImplemented" + a, Card.SKILL, 0, Card.COMMON);
        }
    }

    public static class Blind extends Card {
        public Blind() {
            super("Blind", Card.SKILL, 0, Card.UNCOMMON);
            selectEnemy = true;
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BlindP extends Card {
        public BlindP() {
            super("Blind+", Card.SKILL, 0, Card.UNCOMMON);
            weakEnemy = true;
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

        public _DarkShacklesT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.exhaustWhenPlayed = true;
            this.selectEnemy = true;
            this.affectEnemyStrength = true;
            this.affectEnemyStrengthEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DarkShackles extends _DarkShacklesT {
        public DarkShackles() {
            super("Dark Shackles", Card.SKILL, 0, 9);
        }
    }

    public static class DarkShacklesP extends _DarkShacklesT {
        public DarkShacklesP() {
            super("Dark Shackles+", Card.SKILL, 0, 15);
        }
    }

    private static abstract class _DeepBreathT extends Card {
        private final int n;

        public _DeepBreathT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.reshuffle();
            state.draw(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DeepBreath extends _DeepBreathT {
        public DeepBreath() {
            super("Deep Breath", Card.SKILL, 0, 1);
        }
    }

    public static class DeepBreathP extends _DeepBreathT {
        public DeepBreathP() {
            super("Deep Breath+", Card.SKILL, 0, 2);
        }
    }

    private static abstract class _DiscoveryT extends Card {
        protected final boolean generateCard;

        public _DiscoveryT(String cardName, boolean exhausts, boolean generateCard) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.exhaustWhenPlayed = exhausts;
            this.generateCard = generateCard;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (!generateCard) {
                return GameActionCtx.PLAY_CARD;
            }
            state.setSelect1OutOf3Idxes(state.properties.discoveryIdxes);
            return GameActionCtx.SELECT_CARD_1_OUT_OF_3;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            if (!generateCard) {
                return;
            }
            var cards = CardManager.getCharacterCardsTmp0Cost(state.properties.character, false);
            state.properties.discoveryIdxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.properties.discoveryIdxes[i] = state.properties.select1OutOf3CardsReverseIdxes[state.properties.findCardIndex(cards.get(i))];
            }
        }

        public List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            if (!generateCard) {
                return null;
            }
            return CardManager.getCharacterCardsTmp0Cost(gameProperties.character, false);
        }
    }

    public static class Discovery extends _DiscoveryT {
        public Discovery(boolean generateCard) {
            super("Discovery", true, generateCard);
        }

        @Override public Card getUpgrade() {
            return new DiscoveryP(generateCard);
        }
    }

    public static class DiscoveryP extends _DiscoveryT {
        public DiscoveryP(boolean generateCard) {
            super("Discovery+", false, generateCard);
        }
    }

    private static abstract class _DramaticEntranceT extends Card {
        private final int n;

        public _DramaticEntranceT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            exhaustWhenPlayed = true;
            innate = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DramaticEntrance extends _DramaticEntranceT {
        public DramaticEntrance() {
            super("Dramatic Entrance", Card.ATTACK, 0, 8);
        }
    }

    public static class DramaticEntranceP extends _DramaticEntranceT {
        public DramaticEntranceP() {
            super("Dramatic Entrance+", Card.ATTACK, 0, 12);
        }
    }

    private static abstract class _EnlightenmentT extends Card {
        private final boolean upgraded;

        public _EnlightenmentT(String cardName, int cardType, int energyCost, boolean upgraded) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.upgraded = upgraded;
        }

        public GameActionCtx play(GameState state, int _idx, int energyUsed) {
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
            if (upgraded) {
                return cards.stream().map((card) -> card.getPermCostIfPossible(1)).toList();
            } else {
                return cards.stream().map((card) -> card.getTemporaryCostIfPossible(1)).toList();
            }
        }
    }

    public static class Enlightenment extends _EnlightenmentT {
        public Enlightenment() {
            super("Enlightenment", Card.SKILL, 0, false);
        }
    }

    public static class EnlightenmentP extends _EnlightenmentT {
        public EnlightenmentP() {
            super("Enlightenment+", Card.SKILL, 0, true);
        }
    }

    private static abstract class _FinesseT extends Card {
        private final int n;

        public _FinesseT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Finesse extends _FinesseT {
        public Finesse() {
            super("Finesse", Card.SKILL, 0, 2);
        }
    }

    public static class FinesseP extends _FinesseT {
        public FinesseP() {
            super("Finesse+", Card.SKILL, 0, 4);
        }
    }

    private static abstract class _FlashOfSteelT extends Card {
        private final int n;

        public _FlashOfSteelT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FlashOfSteel extends _FlashOfSteelT {
        public FlashOfSteel() {
            super("Flash Of Steel", Card.ATTACK, 0, 3);
        }
    }

    public static class FlashOfSteelP extends _FlashOfSteelT {
        public FlashOfSteelP() {
            super("Flash Of Steel+", Card.ATTACK, 0, 5);
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

        public _GoodInstinctsT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class GoodInstincts extends _GoodInstinctsT {
        public GoodInstincts() {
            super("Good Instincts", Card.SKILL, 0, 6);
        }
    }

    public static class GoodInstinctsP extends _GoodInstinctsT {
        public GoodInstinctsP() {
            super("Good Instincts+", Card.SKILL, 0, 9);
        }
    }

    private static abstract class _ImpatienceT extends Card {
        private final int n;

        public _ImpatienceT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
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

    public static class Impatience extends _ImpatienceT {
        public Impatience() {
            super("Impatience", Card.SKILL, 0, 2);
        }
    }

    public static class ImpatienceP extends _ImpatienceT {
        public ImpatienceP() {
            super("Impatience+", Card.SKILL, 0, 3);
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
            var colorlessCardIdxes = state.properties.colorlessCardIdxes;
            for (int i = 0; i < numCards; i++) {
                int randomIdx = state.getSearchRandomGen().nextInt(colorlessCardIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, colorlessCardIdxes));
                int cardIdx = colorlessCardIdxes[randomIdx];
                state.addCardToHand(cardIdx);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            return CardManager.getColorlessCards(false);
        }
    }

    public static class JackOfAllTrades extends _JackOfAllTradesT {
        public JackOfAllTrades() {
            super("Jack Of All Trades", 1);
        }
    }

    public static class JackOfAllTradesP extends _JackOfAllTradesT {
        public JackOfAllTradesP() {
            super("Jack Of All Trades+", 2);
        }
    }

    private static abstract class _MadnessT extends Card {
        public _MadnessT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int _idx, int energyUsed) {
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
            state.addCardToHand(state.properties.findCardIndex(state.properties.cardDict[idx].getPermCostIfPossible(0))); // todo
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return cards.stream().map((card) -> card.getPermCostIfPossible(0)).toList();
        }
    }

    public static class Madness extends _MadnessT {
        public Madness() {
            super("Madness", Card.SKILL, 1);
        }
    }

    public static class MadnessP extends _MadnessT {
        public MadnessP() {
            super("Madness+", Card.SKILL, 0);
        }
    }

    private static abstract class _MindBlastT extends Card {
        public _MindBlastT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            innate = true;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), state.getNumCardsInDeck());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class MindBlast extends _MindBlastT {
        public MindBlast() {
            super("Mind Blast", Card.ATTACK, 2);
        }
    }

    public static class MindBlastP extends _MindBlastT {
        public MindBlastP() {
            super("Mind Blast+", Card.ATTACK, 1);
        }
    }

    private static abstract class _PanaceaT extends Card {
        private final int n;

        public _PanaceaT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            changePlayerArtifact = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainArtifact(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Panacea extends _PanaceaT {
        public Panacea() {
            super("Panacea", Card.SKILL, 0, 1);
        }
    }

    public static class PanaceaP extends _PanaceaT {
        public PanaceaP() {
            super("Panacea+", Card.SKILL, 0, 2);
        }
    }

    private static abstract class _PanicButtonT extends Card {
        private final int n;

        public _PanicButtonT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.NO_BLOCK_FROM_CARDS, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PanicButton extends _PanicButtonT {
        public PanicButton() {
            super("Panic Button", Card.SKILL, 0, 30);
        }
    }

    public static class PanicButtonP extends _PanicButtonT {
        public PanicButtonP() {
            super("Panic Button+", Card.SKILL, 0, 40);
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

        public _SwiftStrikeT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SwiftStrike extends _SwiftStrikeT {
        public SwiftStrike() {
            super("Swift Strike", Card.ATTACK, 0, 7);
        }
    }

    public static class SwiftStrikeP extends _SwiftStrikeT {
        public SwiftStrikeP() {
            super("Swift Strike+", Card.ATTACK, 0, 10);
        }
    }

    public static class Trip extends Card {
        public Trip() {
            super("Trip", Card.SKILL, 0, Card.UNCOMMON);
            selectEnemy = true;
            vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.VULNERABLE, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class TripP extends Card {
        public TripP() {
            super("Trip+", Card.SKILL, 0, Card.UNCOMMON);
            vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.VULNERABLE, 2);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _ApotheosisT extends Card {
        public _ApotheosisT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost, Card.RARE);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.handArrTransform(state.properties.upgradeIdxes);
            state.discardArrTransform(state.properties.upgradeIdxes);
            state.deckArrTransform(state.properties.upgradeIdxes);
            state.exhaustArrTransform(state.properties.upgradeIdxes);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return cards.stream().map(Card::getUpgrade).filter(Objects::nonNull).filter((x) -> !x.cardName.equals(this.cardName)).toList();
        }
    }

    public static class Apotheosis extends CardColorless._ApotheosisT {
        public Apotheosis() {
            super("Apotheosis", Card.SKILL, 2);
        }
    }

    public static class ApotheosisP extends CardColorless._ApotheosisT {
        public ApotheosisP() {
            super("Apotheosis+", Card.SKILL, 1);
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
            var characterSkillPerm0Idxes = state.properties.characterSkillPerm0Idxes;
            for (int i = 0; i < skillCount; i++) {
                state.setIsStochastic();
                int randomIdx = state.getSearchRandomGen().nextInt(characterSkillPerm0Idxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, characterSkillPerm0Idxes));
                int cardIdx = characterSkillPerm0Idxes[randomIdx];
                state.addCardToDeck(cardIdx);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var cards = CardManager.getCharacterCardsByType(state.properties.character, Card.SKILL, false);
            state.properties.characterSkillPerm0Idxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.properties.characterSkillPerm0Idxes[i] = state.properties.findCardIndex(cards.get(i).getPermCostIfPossible(0));
            }
        }

        public List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            return CardManager.getCharacterCardsByType(gameProperties.character, Card.SKILL, false)
                    .stream().map((card) -> card.getPermCostIfPossible(0)).toList();
        }
    }

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

        public _HandOfGreedT(String cardName, int cardType, int n, double healthRewardRatio) {
            super(cardName, cardType, 2, Card.RARE);
            this.n = n;
            this.selectEnemy = true;
            this.healthRewardRatio = healthRewardRatio;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
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
                    @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                        if (isTerminal > 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getCounterForRead()[counterIdx] / 100.0;
                        } else if (isTerminal == 0) {
                            int minFeed = state.getCounterForRead()[counterIdx];
                            int maxFeedRemaining = getMaxPossibleHandOfGreedRemaining(state, true);
                            double vFeed = Math.max(minFeed / 100.0, Math.min((minFeed + maxFeedRemaining) / 100.0, state.getVOther(vArrayIdx)));
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = vFeed;
                        }
                    }

                    @Override public void updateQValues(GameState state, double[] v) {
                        int minFeed = state.getCounterForRead()[counterIdx];
                        int maxFeedRemaining = getMaxPossibleHandOfGreedRemaining(state, true);
                        double vFeed = Math.max(minFeed / 100.0, Math.min((minFeed + maxFeedRemaining) / 100.0, v[GameState.V_OTHER_IDX_START + vArrayIdx]));
                        v[GameState.V_HEALTH_IDX] += 100 * vFeed * healthRewardRatio / state.getPlayeForRead().getMaxHealth();
                    }
                });
            }
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            counterIdx = idx;
            gameProperties.feedCounterIdx = idx; // todo: tmp for stats
            gameProperties.handOfGreedCounterIdx = idx;
        }

        public static int getMaxPossibleHandOfGreedRemaining(GameState state, boolean checkEndOfGame) {
            if (state.properties.handOfGreedCounterIdx < 0) {
                return 0;
            }
            if (checkEndOfGame && state.isTerminal() != 0) {
                return 0;
            }
            int nonUpgrade = state.getNonExhaustCount("Hand Of Greed");
            boolean canUpgrade = false; // todo
            if (!canUpgrade) {
                int upgrade = state.getNonExhaustCount("Hand Of Greed+");
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

    public static class HandOfGreed extends CardColorless._HandOfGreedT {
        public HandOfGreed(double healthRewardRatio) {
            super("Hand Of Greed", Card.ATTACK, 20, healthRewardRatio);
        }

        public Card getUpgrade() {
            return new CardColorless.HandOfGreedP(healthRewardRatio);
        }
    }

    public static class HandOfGreedP extends CardColorless._HandOfGreedT {
        public HandOfGreedP(double healthRewardRatio) {
            super("Hand Of Greed+", Card.ATTACK, 25, healthRewardRatio);
        }
    }

    private static abstract class _MagnetismT extends Card {
        public _MagnetismT(String cardName, int energyCost) {
            super(cardName, Card.POWER, energyCost, Card.RARE);
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
                    var colorlessCardIdxes = state.properties.colorlessCardIdxes;
                    for (int i = 0; i < state.getCounterForRead()[counterIdx]; i++) {
                        state.setIsStochastic();
                        int randomIdx = state.getSearchRandomGen().nextInt(colorlessCardIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, colorlessCardIdxes));
                        int cardIdx = colorlessCardIdxes[randomIdx];
                        state.addCardToHand(cardIdx);
                    }
                }
            });
        }

        public List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            return CardManager.getColorlessCards(false);
        }
    }

    public static class Magnetism extends _MagnetismT {
        public Magnetism() {
            super("Magnetism", 2);
        }
    }

    public static class MagnetismP extends _MagnetismT {
        public MagnetismP() {
            super("Magnetism+", 1);
        }
    }

    private static abstract class _MasterOfStrategyT extends Card {
        private final int n;

        public _MasterOfStrategyT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.draw(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class MasterOfStrategy extends CardColorless._MasterOfStrategyT {
        public MasterOfStrategy() {
            super("Master Of Strategy", Card.SKILL, 0, 3);
        }
    }

    public static class MasterOfStrategyP extends CardColorless._MasterOfStrategyT {
        public MasterOfStrategyP() {
            super("Master Of Strategy+", Card.SKILL, 0, 4);
        }
    }

    private static abstract class _MayhemT extends Card {
        public _MayhemT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost, Card.RARE);
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

    public static class Mayhem extends CardColorless._MayhemT {
        public Mayhem() {
            super("Mayhem", Card.POWER, 2);
        }
    }

    public static class MayhemP extends CardColorless._MayhemT {
        public MayhemP() {
            super("Mayhem+", Card.POWER, 1);
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
            var characterAttackPerm0Idxes = state.properties.characterAttackPerm0Idxes;
            for (int i = 0; i < attackCount; i++) {
                state.setIsStochastic();
                int randomIdx = state.getSearchRandomGen().nextInt(characterAttackPerm0Idxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, characterAttackPerm0Idxes));
                int cardIdx = characterAttackPerm0Idxes[randomIdx];
                state.addCardToDeck(cardIdx);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var cards = CardManager.getCharacterCardsByType(state.properties.character, Card.ATTACK, false);
            state.properties.characterAttackPerm0Idxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                state.properties.characterAttackPerm0Idxes[i] = state.properties.findCardIndex(cards.get(i).getPermCostIfPossible(0));
            }
        }

        public List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            return CardManager.getCharacterCardsByType(gameProperties.character, Card.ATTACK, false)
                    .stream().map((card) -> card.getPermCostIfPossible(0)).toList();
        }
    }

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

        public _PanacheT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
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

    public static class Panache extends CardColorless._PanacheT {
        public Panache() {
            super("Panache", Card.POWER, 0, 10);
        }
    }

    public static class PanacheP extends CardColorless._PanacheT {
        public PanacheP() {
            super("Panache+", Card.POWER, 0, 14);
        }
    }

    private static abstract class _SadisticNatureT extends Card implements GameProperties.CounterRegistrant {
        private final int damage;

        public _SadisticNatureT(String cardName, int damage) {
            super(cardName, Card.POWER, 0, Card.RARE);
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
        public _SecretTechniqueT(String cardName, int cardType, int energyCost, boolean exhaustWhenPlayed) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.exhaustWhenPlayed = exhaustWhenPlayed;
            selectFromDeck = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.removeCardFromDeck(idx, false);
            state.addCardToHand(idx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public boolean canSelectCard(Card card) {
            return card.cardType == Card.SKILL;
        }
    }

    public static class SecretTechnique extends CardColorless._SecretTechniqueT {
        public SecretTechnique() {
            super("Secret Technique", Card.SKILL, 0, true);
        }
    }

    public static class SecretTechniqueP extends CardColorless._SecretTechniqueT {
        public SecretTechniqueP() {
            super("Secret Technique+", Card.SKILL, 0, false);
        }
    }

    private static abstract class _SecretWeaponT extends Card {
        public _SecretWeaponT(String cardName, int cardType, int energyCost, boolean exhaustWhenPlayed) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.exhaustWhenPlayed = exhaustWhenPlayed;
            selectFromDeck = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.removeCardFromDeck(idx, false);
            state.addCardToHand(idx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public boolean canSelectCard(Card card) {
            return card.cardType == Card.ATTACK;
        }
    }

    public static class SecretWeapon extends CardColorless._SecretWeaponT {
        public SecretWeapon() {
            super("Secret Weapon", Card.SKILL, 0, true);
        }
    }

    public static class SecretWeaponP extends CardColorless._SecretWeaponT {
        public SecretWeaponP() {
            super("Secret Weapon+", Card.SKILL, 0, false);
        }
    }

    private static abstract class _TheBombT extends Card {
        private final int n;

        public _TheBombT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
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

    public static class TheBomb extends CardColorless._TheBombT {
        public TheBomb() {
            super("The Bomb", Card.SKILL, 2, 40);
        }
    }

    public static class TheBombP extends CardColorless._TheBombT {
        public TheBombP() {
            super("The Bomb+", Card.SKILL, 2, 50);
        }
    }

    private static abstract class _ThinkingAheadT extends Card {
        public _ThinkingAheadT(String cardName, int cardType, int energyCost, boolean exhaustWhenPlayed) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.exhaustWhenPlayed = exhaustWhenPlayed;
            selectFromHand = true;
            selectFromHandLater = true;
            putCardOnTopDeck = true;
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

    public static class ThinkingAhead extends CardColorless._ThinkingAheadT {
        public ThinkingAhead() {
            super("Thinking Ahead", Card.SKILL, 0, true);
        }
    }

    public static class ThinkingAheadP extends CardColorless._ThinkingAheadT {
        public ThinkingAheadP() {
            super("Thinking Ahead+", Card.SKILL, 0, false);
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
            var colorlessIdxes = upgraded ? state.properties.colorlessUpgradedTmp0Idxes : state.properties.colorlessTmp0Idxes;
            for (int i = 0; i < energyUsed; i++) {
                int randomIdx = state.getSearchRandomGen().nextInt(colorlessIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, colorlessIdxes));
                int cardIdx = colorlessIdxes[randomIdx];
                state.addCardToHand(cardIdx);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            return state.energy;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            return CardManager.getColorlessCardsTmp0Cost(false);
        }
    }

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

        public _ViolenceT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
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
                    state.addCardToHand(possibleIndexes[len - 1 - i]);
                }
            } else {
                Utils.shuffle(state, possibleIndexes, len, len - 3, state.getSearchRandomGen());
                for (int i = 0; i < n; i++) {
                    state.removeCardFromDeck(possibleIndexes[len - 1 - i], true);
                    state.addCardToHand(possibleIndexes[len - 1 - i]);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Violence extends _ViolenceT {
        public Violence() {
            super("Violence", Card.SKILL, 0, 3);
        }
    }

    public static class ViolenceP extends _ViolenceT {
        public ViolenceP() {
            super("Violence+", Card.SKILL, 0, 5);
        }
    }

    private static abstract class _BiteT extends Card {
        private final int n;
        private final int heal;

        public _BiteT(String cardName, int cardType, int energyCost, int n, int heal) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.heal = heal;
            this.selectEnemy = true;
            this.healPlayer = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.healPlayer(heal);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Bite extends _BiteT {
        public Bite() {
            super("Bite", Card.ATTACK, 1, 7, 2);
        }
    }

    public static class BiteP extends _BiteT {
        public BiteP() {
            super("Bite+", Card.ATTACK, 1, 8, 3);
        }
    }

    private static abstract class _RitualDaggerT extends Card {
        protected final int n;
        private final int dmgInc;
        protected final int healthRewardRatio;

        public _RitualDaggerT(String cardName, int cardType, int energyCost, int n, int dmgInc, int healthRewardRatio) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.dmgInc = dmgInc;
            this.selectEnemy = true;
            this.exhaustWhenPlayed = true;
            this.healthRewardRatio = healthRewardRatio;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
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
                    @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                        if (isTerminal > 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getCounterForRead()[counterIdx] / 10.0;
                        } else if (isTerminal == 0) {
                            int minValue = state.getCounterForRead()[counterIdx];
                            int maxRemaining = getMaxPossibleRitualDaggerRemaining(state, true);
                            double vFeed = Math.max(minValue / 10.0, Math.min((minValue + maxRemaining) / 10.0, state.getVOther(vArrayIdx)));
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = vFeed;
                        }
                    }

                    @Override public void updateQValues(GameState state, double[] v) {
                        int minValue = state.getCounterForRead()[counterIdx];
                        int maxRemaining = getMaxPossibleRitualDaggerRemaining(state, true);
                        double vFeed = Math.max(minValue / 10.0, Math.min((minValue + maxRemaining) / 10.0, v[GameState.V_OTHER_IDX_START + vArrayIdx]));
                        v[GameState.V_HEALTH_IDX] += 10 * vFeed * healthRewardRatio / state.getPlayeForRead().getMaxHealth();
                    }
                });
            }
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            counterIdx = idx;
            gameProperties.ritualDaggerCounterIdx = counterIdx;
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

    public static class RitualDagger extends _RitualDaggerT {
        public RitualDagger(int dmg, int healthReward) {
            super("Ritual Dagger", Card.ATTACK, 1, dmg, 3, healthReward);
        }

        @Override public Card getUpgrade() {
            return new RitualDaggerP(this.n, healthRewardRatio);
        }
    }

    public static class RitualDaggerP extends _RitualDaggerT {
        public RitualDaggerP(int dmg, int healthReward) {
            super("Ritual Dagger+", Card.ATTACK, 1, dmg, 5, healthReward);
        }
    }

    private static abstract class _ShivT extends Card {
        private final int n;

        public _ShivT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.selectEnemy = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, true);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Shiv extends _ShivT {
        public Shiv() {
            super("Shiv", Card.ATTACK, 0, 4);
        }
    }

    public static class ShivP extends _ShivT {
        public ShivP() {
            super("Shiv+", Card.ATTACK, 0, 6);
        }
    }

    private static abstract class _ApparitionT extends Card {
        public _ApparitionT(String cardName, int cardType, int energyCost, boolean ethereal) {
            super(cardName, cardType, energyCost, Card.COMMON);
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

    public static class Apparition extends _ApparitionT {
        public Apparition() {
            super("Apparition", Card.SKILL, 1, true);
        }
    }

    public static class ApparitionP extends _ApparitionT {
        public ApparitionP() {
            super("Apparition+", Card.SKILL, 1, false);
        }
    }

    private static abstract class _SmiteT extends Card {
        private final int damage;

        public _SmiteT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.selectEnemy = true;
            this.retain = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
            return GameActionCtx.PLAY_CARD;
        }
    }

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
            state.getPlayerForWrite().gainBlock(block);
            return GameActionCtx.PLAY_CARD;
        }
    }

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
            this.selectEnemy = true;
            this.retain = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
            return GameActionCtx.PLAY_CARD;
        }
    }

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
            state.addCardToDeck(state.properties.omegaCardIdx);
            return GameActionCtx.PLAY_CARD;
        }
    }

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
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < xValue; i++) {
                state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Expunger extends _ExpungerT {
        public Expunger(int xValue) {
            super("Expunger (" + xValue + ")", 9, xValue);
        }

        @Override public Card getUpgrade() {
            return new ExpungerP(xValue);
        }
    }

    public static class ExpungerP extends _ExpungerT {
        public ExpungerP(int xValue) {
            super("Expunger+ (" + xValue + ")", 15, xValue);
        }
    }
}
