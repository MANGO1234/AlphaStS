package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.action.BeatDownContinueAction;
import com.alphaStS.action.CatastropheContinueAction;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.eventHandler.OnDamageHandler;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.random.RandomGenCtx;
import com.alphaStS.utils.Tuple;
import com.alphaStS.utils.Utils;

public class CardColorless2 {
    // **************************************************************************************************
    // ********************************************* Uncommon *********************************************
    // **************************************************************************************************

    private static abstract class _AutomationT extends Card {
        public _AutomationT(String cardName, int energyCost) {
            super(cardName, Card.POWER, energyCost, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Automation", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardDrawnHandler("Automation", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    state.getCounterForWrite()[counterIdx]++;
                    if (state.getCounterForRead()[counterIdx] == 10) {
                        state.gainEnergy(1);
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    // Automation (Uncommon) - 1 energy, Power
    //   Effect: Every 10 cards you draw, gain energy.
    //   Upgraded Effect (0 energy): Every 10 cards you draw, gain energy.
    public static class Automation extends _AutomationT {
        public Automation() {
            super("Automation", 1);
        }
    }

    public static class AutomationP extends _AutomationT {
        public AutomationP() {
            super("Automation+", 0);
        }
    }

    // No need to implement Believe in You: Multiplayer

    private static abstract class _CatastropheT extends Card {
        private final int numCards;

        public _CatastropheT(String cardName, int numCards) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
            this.numCards = numCards;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] = numCards;
            state.addGameActionToStartOfDeque(new CatastropheContinueAction());
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Catastrophe", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int counterIdx) {
                    state.properties.catastropheCounterIdx = counterIdx;
                }
            });
        }
    }

    // Catastrophe (Uncommon) - 2 energy, Skill
    //   Effect: Play 2 random cards from your Draw Pile.
    //   Upgraded Effect: Play 3 random cards from your Draw Pile.
    public static class Catastrophe extends _CatastropheT {
        public Catastrophe() {
            super("Catastrophe", 2);
        }
    }

    public static class CatastropheP extends _CatastropheT {
        public CatastropheP() {
            super("Catastrophe+", 3);
        }
    }

    // No need to implement Coordinate: Multiplayer

    // Dark Shackles (Uncommon) - 0 energy, Skill
    //   Effect: Enemy loses 9 Strength this turn. Exhaust.
    //   Upgraded Effect: Enemy loses 15 Strength this turn. Exhaust.
    public static class DarkShackles extends CardColorless.DarkShackles {
    }

    public static class DarkShacklesP extends CardColorless.DarkShacklesP {
    }

    // Discovery (Uncommon) - 1 energy, Skill
    //   Effect: Choose 1 of 3 random cards to add into your Hand. It's free to play this turn. Exhaust.
    //   Upgraded Effect: Choose 1 of 3 random cards to add into your Hand. It's free to play this turn.
    public static class Discovery extends CardColorless.Discovery {
    }

    public static class DiscoveryP extends CardColorless.DiscoveryP {
    }

    // Dramatic Entrance (Uncommon) - 0 energy, Attack
    //   Effect: Innate. Deal 11 damage to ALL enemies. Exhaust.
    //   Upgraded Effect: Innate. Deal 15 damage to ALL enemies. Exhaust.
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

    // Equilibrium (Uncommon) - 2 energy, Skill
    //   Effect: Gain 13 Block. Retain your Hand this turn.
    //   Upgraded Effect: Gain 16 Block. Retain your Hand this turn.
    public static class Equilibrium extends CardDefect.Equilibrium {
    }

    public static class EquilibriumP extends CardDefect.EquilibriumP {
    }

    private static abstract class _FastenT extends Card {
        private final int bonus;

        public _FastenT(String cardName, int bonus) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.bonus = bonus;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] = bonus;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Fasten", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 14.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int counterIdx) {
                    state.properties.fastenCounterIdx = counterIdx;
                }
            });
        }
    }

    // Fasten (Uncommon) - 1 energy, Power
    //   Effect: Gain an additional 4 Block from Defend cards.
    //   Upgraded Effect: Gain an additional 6 Block from Defend cards.
    public static class Fasten extends _FastenT {
        public Fasten() {
            super("Fasten", 4);
        }
    }

    public static class FastenP extends _FastenT {
        public FastenP() {
            super("Fasten+", 6);
        }
    }

    // Finesse (Uncommon) - 0 energy, Skill
    //   Effect: Gain 4 Block. Draw 1 card.
    //   Upgraded Effect: Gain 7 Block. Draw 1 card.
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

    private static abstract class _FisticuffsT extends Card {
        private final int damage;

        public _FisticuffsT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            int dmgDone = state.playerDoDamageToEnemy(enemy, damage, this);
            if (dmgDone > 0) {
                state.playerGainBlock(dmgDone);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Fisticuffs (Uncommon) - 1 energy, Attack
    //   Effect: Deal 7 damage. Gain Block equal to damage dealt.
    //   Upgraded Effect: Deal 9 damage. Gain Block equal to damage dealt.
    public static class Fisticuffs extends _FisticuffsT {
        public Fisticuffs() {
            super("Fisticuffs", 7);
        }
    }

    public static class FisticuffsP extends _FisticuffsT {
        public FisticuffsP() {
            super("Fisticuffs+", 9);
        }
    }

    // Flash of Steel (Uncommon) - 0 energy, Attack
    //   Effect: Deal 5 damage. Draw 1 card.
    //   Upgraded Effect: Deal 8 damage. Draw 1 card.
    public static class FlashOfSteel extends CardColorless._FlashOfSteelT {
        public FlashOfSteel() {
            super("Flash of Steel", 5);
        }
    }

    public static class FlashOfSteelP extends CardColorless._FlashOfSteelT {
        public FlashOfSteelP() {
            super("Flash of Steel+", 8);
        }
    }

    // No need to implement Gang Up: Multiplayer

    // No need to implement Huddle Up: Multiplayer

    // Impatience (Uncommon) - 0 energy, Skill
    //   Effect: If you have no Attacks in your Hand, draw 2 cards.
    //   Upgraded Effect: If you have no Attacks in your Hand, draw 3 cards.
    public static class Impatience extends CardColorless.Impatience {
    }

    public static class ImpatienceP extends CardColorless.ImpatienceP {
    }

    // No need to implement Intercept: Multiplayer

    // Jack of All Trades (Uncommon) - 0 energy, Skill
    //   Effect: Add 1 random Colorless card into your Hand. Exhaust.
    //   Upgraded Effect: Add 2 random Colorless cards into your Hand. Exhaust.
    public static class JackOfAllTrades extends CardColorless.JackOfAllTrades {
    }

    public static class JackOfAllTradesP extends CardColorless.JackOfAllTradesP {
    }

    // No need to implement Lift: Multiplayer

    // Mind Blast (Uncommon) - 1 energy, Attack
    //   Effect: Innate. Deal damage equal to the number of cards in your Draw Pile.
    //   Upgraded Effect (0 energy): Innate. Deal damage equal to the number of cards in your Draw Pile.
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

    private static abstract class _OmnisliceT extends Card {
        private final int damage;

        public _OmnisliceT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var primary = state.getEnemiesForWrite().getForWrite(idx);
            int dmgDone = state.playerDoDamageToEnemy(primary, damage, this);
            if (dmgDone > 0) {
                for (int i = 0; i < state.getEnemiesForWrite().size(); i++) {
                    if (i == idx) {
                        continue;
                    }
                    var other = state.getEnemiesForWrite().getForWrite(i);
                    if (other.isAlive() && other.getHealth() > 0) {
                        state.playerDoDamageToEnemy(other, dmgDone, this);
                    }
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Omnislice (Uncommon) - 0 energy, Attack
    //   Effect: Deal 8 damage. Damage ALL other enemies equal to the damage dealt.
    //   Upgraded Effect: Deal 11 damage. Damage ALL other enemies equal to the damage dealt.
    public static class Omnislice extends _OmnisliceT {
        public Omnislice() {
            super("Omnislice", 8);
        }
    }

    public static class OmnisliceP extends _OmnisliceT {
        public OmnisliceP() {
            super("Omnislice+", 11);
        }
    }

    // Panache (Uncommon) - 0 energy, Power
    //   Effect: Every time you play 5 cards in a single turn, deal 10 damage to ALL enemies.
    //   Upgraded Effect: Every time you play 5 cards in a single turn, deal 14 damage to ALL enemies.
    public static class Panache extends CardColorless.Panache {
    }

    public static class PanacheP extends CardColorless.PanacheP {
    }

    // Panic Button (Uncommon) - 0 energy, Skill
    //   Effect: Gain 30 Block. You cannot gain Block from cards for 2 turns. Exhaust.
    //   Upgraded Effect: Gain 40 Block. You cannot gain Block from cards for 2 turns. Exhaust.
    public static class PanicButton extends CardColorless.PanicButton {
    }

    public static class PanicButtonP extends CardColorless.PanicButtonP {
    }

    private static abstract class _PrepTimeT extends Card {
        private final int vigor;

        public _PrepTimeT(String cardName, int vigor) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.vigor = vigor;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerVigorCounter(this);
            state.properties.addStartOfTurnHandler("PrepTime", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[state.properties.vigorCounterIdx] += vigor;
                }
            });
        }
    }

    // Prep Time (Uncommon) - 1 energy, Power
    //   Effect: At the start of your turn, gain 4 Vigor.
    //   Upgraded Effect: At the start of your turn, gain 6 Vigor.
    public static class PrepTime extends _PrepTimeT {
        public PrepTime() {
            super("Prep Time", 4);
        }
    }

    public static class PrepTimeP extends _PrepTimeT {
        public PrepTimeP() {
            super("Prep Time+", 6);
        }
    }

    private static abstract class _ProductionT extends Card {
        private final int energy;

        public _ProductionT(String cardName, int energy) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.energy = energy;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(energy);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Production (Uncommon) - 0 energy, Skill
    //   Effect: Gain 2 energy. Exhaust.
    //   Upgraded Effect: Gain 3 energy. Exhaust.
    public static class Production extends _ProductionT {
        public Production() {
            super("Production", 2);
        }
    }

    public static class ProductionP extends _ProductionT {
        public ProductionP() {
            super("Production+", 3);
        }
    }

    private static abstract class _ProlongT extends Card {
        public _ProlongT(String cardName, boolean exhaust) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            exhaustWhenPlayed = exhaust;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] = state.getPlayerForRead().getBlock();
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerBlockNextTurnCounter(this);
        }
    }

    // Prolong (Uncommon) - 0 energy, Skill
    //   Effect: Next turn, gain Block equal to your current Block. Exhaust.
    //   Upgraded Effect: Next turn, gain Block equal to your current Block.
    public static class Prolong extends _ProlongT {
        public Prolong() {
            super("Prolong", true);
        }
    }

    public static class ProlongP extends _ProlongT {
        public ProlongP() {
            super("Prolong+", false);
        }
    }

    private static abstract class _ProwessT extends Card {
        private final int n;

        public _ProwessT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.n = n;
            entityProperty.changePlayerStrength = true;
            entityProperty.changePlayerDexterity = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainStrength(n);
            state.getPlayerForWrite().gainDexterity(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Prowess (Uncommon) - 1 energy, Power
    //   Effect: Gain 1 Strength. Gain 1 Dexterity.
    //   Upgraded Effect: Gain 2 Strength. Gain 2 Dexterity.
    public static class Prowess extends _ProwessT {
        public Prowess() {
            super("Prowess", 1);
        }
    }

    public static class ProwessP extends _ProwessT {
        public ProwessP() {
            super("Prowess+", 2);
        }
    }

    // Purity (Uncommon) - 0 energy, Skill
    //   Effect: Retain. Exhaust up to 3 cards in your Hand. Exhaust.
    //   Upgraded Effect: Retain. Exhaust up to 5 cards in your Hand. Exhaust.
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

    private static abstract class _RestlessnessT extends Card {
        private final int amount;

        public _RestlessnessT(String cardName, int amount) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.amount = amount;
            this.retain = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.handArrLen == 1) {
                state.draw(amount);
                state.gainEnergy(amount);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Restlessness (Uncommon) - 0 energy, Skill
    //   Effect: Retain. If your Hand is empty, draw 2 cards and gain 2 energy.
    //   Upgraded Effect: Retain. If your Hand is empty, draw 3 cards and gain 3 energy.
    public static class Restlessness extends _RestlessnessT {
        public Restlessness() {
            super("Restlessness", 2);
        }
    }

    public static class RestlessnessP extends _RestlessnessT {
        public RestlessnessP() {
            super("Restlessness+", 3);
        }
    }

    // TODO: Seeker Strike (Uncommon) - 1 energy, Attack
    //   Effect: Deal 9 damage. Choose 1 of 3 cards in your Draw Pile to add into your Hand.
    //   Upgraded Effect: Deal 12 damage. Choose 1 of 3 cards in your Draw Pile to add into your Hand.

    // Shockwave (Uncommon) - 2 energy, Skill
    //   Effect: Apply 3 Weak and Vulnerable to ALL enemies. Exhaust.
    //   Upgraded Effect: Apply 5 Weak and Vulnerable to ALL enemies. Exhaust.
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

    // No need to implement Tag Team: Multiplayer

    // The Bomb (Uncommon) - 2 energy, Skill
    //   Effect: At the end of 3 turns, deal 40 damage to ALL enemies.
    //   Upgraded Effect: At the end of 3 turns, deal 50 damage to ALL enemies.
    public static class TheBomb extends CardColorless.TheBomb {
    }

    public static class TheBombP extends CardColorless.TheBombP {
    }

    // Thinking Ahead (Uncommon) - 0 energy, Skill
    //   Effect: Draw 2 cards. Put 1 card from your Hand on top of your Draw Pile. Exhaust.
    //   Upgraded Effect: Draw 2 cards. Put 1 card from your Hand on top of your Draw Pile.
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
            state.playerGainBlock(block);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Ultimate Defend (Uncommon) - 1 energy, Skill
    //   Effect: Gain 11 Block.
    //   Upgraded Effect: Gain 15 Block.
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Ultimate Strike (Uncommon) - 1 energy, Attack
    //   Effect: Deal 14 damage.
    //   Upgraded Effect: Deal 20 damage.
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

    private static abstract class _VolleyT extends Card {
        private final int damage;

        public _VolleyT(String cardName, int damage) {
            super(cardName, Card.ATTACK, -1, Card.UNCOMMON);
            this.damage = damage;
            isXCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < energyUsed; i++) {
                int enemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
                if (enemyIdx < 0) {
                    break;
                }
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(enemyIdx), damage, this);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            return state.energy;
        }
    }

    // Volley (Uncommon) - X energy, Attack
    //   Effect: Deal 10 damage to a random enemy X times.
    //   Upgraded Effect: Deal 14 damage to a random enemy X times.
    public static class Volley extends _VolleyT {
        public Volley() {
            super("Volley", 10);
        }
    }

    public static class VolleyP extends _VolleyT {
        public VolleyP() {
            super("Volley+", 14);
        }
    }

    // **************************************************************************************************
    // *********************************************  Rare  *********************************************
    // **************************************************************************************************

    // Alchemize (Rare) - 1 energy, Skill
    //   Effect: Procure a random potion. Exhaust.
    //   Upgraded Effect (0 energy): Procure a random potion. Exhaust.
    public static class Alchemize extends CardSilent._AlchemizeT {
        public Alchemize(int basePenaltyRatio, int possibleGeneratedPotions, int healthReward) {
            super("Alchemize", 1, basePenaltyRatio, possibleGeneratedPotions, healthReward);
        }

        @Override public Card getUpgrade() {
            return new AlchemizeP(basePenaltyRatio, possibleGeneratedPotions, healthReward);
        }
    }

    public static class AlchemizeP extends CardSilent._AlchemizeT {
        public AlchemizeP(int basePenaltyRatio, int possibleGeneratedPotions, int healthReward) {
            super("Alchemize+", 0, basePenaltyRatio, possibleGeneratedPotions, healthReward);
        }
    }

    private static abstract class _AnointedT extends Card {
        public _AnointedT(String cardName, boolean retain) {
            super(cardName, Card.SKILL, 1, Card.RARE);
            exhaustWhenPlayed = true;
            this.retain = retain;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            short[] rareIdxes = new short[state.deckArrLen];
            int len = 0;
            for (int i = 0; i < state.deckArrLen; i++) {
                if (state.properties.cardDict[state.deckArr[i]].rarity == Card.RARE) {
                    rareIdxes[len++] = state.deckArr[i];
                }
            }
            if (len > 1) {
                state.setIsStochastic();
                Utils.shuffle(state, rareIdxes, len, state.getSearchRandomGen());
            }
            for (int i = len - 1; i >= 0; i--) {
                state.removeCardFromDeck(rareIdxes[i], false);
                state.addCardToHand(rareIdxes[i]);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Anointed (Rare) - 1 energy, Skill
    //   Effect: Put every Rare card from your Draw Pile into your Hand. Exhaust.
    //   Upgraded Effect: Retain. Put every Rare card from your Draw Pile into your Hand. Exhaust.
    public static class Anointed extends _AnointedT {
        public Anointed() {
            super("Anointed", false);
        }
    }

    public static class AnointedP extends _AnointedT {
        public AnointedP() {
            super("Anointed+", true);
        }
    }

    // No need to implement Beacon of Hope: Multiplayer

    private static abstract class _BeatDownT extends Card {
        private final int numAttacks;

        public _BeatDownT(String cardName, int numAttacks) {
            super(cardName, Card.SKILL, 3, Card.RARE);
            this.numAttacks = numAttacks;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] = numAttacks;
            state.addGameActionToStartOfDeque(new BeatDownContinueAction());
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("BeatDown", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 4.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int counterIdx) {
                    state.properties.beatDownCounterIdx = counterIdx;
                }
            });
        }
    }

    // Beat Down (Rare) - 3 energy, Skill
    //   Effect: Play 3 random Attacks from your Discard Pile.
    //   Upgraded Effect: Play 4 random Attacks from your Discard Pile.
    public static class BeatDown extends _BeatDownT {
        public BeatDown() {
            super("Beat Down", 3);
        }
    }

    public static class BeatDownP extends _BeatDownT {
        public BeatDownP() {
            super("Beat Down+", 4);
        }
    }

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

    // Eternal Armor (Rare) - 3 energy, Power
    //   Effect: Gain 9 Plating.
    //   Upgraded Effect: Gain 12 Plating.
    public static class EternalArmor extends _EternalArmorT {
        public EternalArmor() {
            super("Eternal Armor", 9);
        }
    }

    public static class EternalArmorP extends _EternalArmorT {
        public EternalArmorP() {
            super("Eternal Armor+", 12);
        }
    }

    private static abstract class _GoldAxeT extends Card {
        public _GoldAxeT(String cardName, boolean retain) {
            super(cardName, Card.ATTACK, 1, Card.RARE);
            this.retain = retain;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            // +1 to include Gold Axe itself (onCardPlayedHandlers fires after play())
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), state.getCounterForRead()[counterIdx] + 1, this);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("CardsPlayedCombat", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 50.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("GoldAxe", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    state.getCounterForWrite()[counterIdx]++;
                }
            });
        }
    }

    // Gold Axe (Rare) - 1 energy, Attack
    //   Effect: Deal damage equal to the number of cards played this combat.
    //   Upgraded Effect: Retain. Deal damage equal to the number of cards played this combat.
    public static class GoldAxe extends _GoldAxeT {
        public GoldAxe() {
            super("Gold Axe", false);
        }
    }

    public static class GoldAxeP extends _GoldAxeT {
        public GoldAxeP() {
            super("Gold Axe+", true);
        }
    }

    // Hand of Greed (Rare) - 2 energy, Attack
    //   Effect: Deal 20 damage. If Fatal, gain 20 Gold.
    //   Upgraded Effect: Deal 25 damage. If Fatal, gain 25 Gold.
    public static class HandOfGreed extends CardColorless.HandOfGreed {
        public HandOfGreed(double healthRewardRatio) {
            super(healthRewardRatio);
        }
    }

    public static class HandOfGreedP extends CardColorless.HandOfGreedP {
        public HandOfGreedP(double healthRewardRatio) {
            super(healthRewardRatio);
        }
    }

    // TODO: Hidden Gem (Rare) - 1 energy, Skill
    //   Effect: A random card in your Draw Pile without Replay gains Replay 2.
    //   Upgraded Effect: A random card in your Draw Pile without Replay gains Replay 3.

    // TODO: Jackpot (Rare) - 3 energy, Attack
    //   Effect: Deal 25 damage. Add 3 random 0 energy cards into your Hand.
    //   Upgraded Effect: Deal 30 damage. Add 3 random Upgraded 0 energy cards into your Hand.

    // No need to implement Knockdown: Multiplayer

    // Master of Strategy (Rare) - 0 energy, Skill
    //   Effect: Draw 3 cards. Exhaust.
    //   Upgraded Effect: Draw 4 cards. Exhaust.
    public static class MasterOfStrategy extends CardColorless.MasterOfStrategy {
    }

    public static class MasterOfStrategyP extends CardColorless.MasterOfStrategyP {
    }

    // Mayhem (Rare) - 2 energy, Power
    //   Effect: At the start of your turn, play the top card of your Draw Pile.
    //   Upgraded Effect (1 energy): At the start of your turn, play the top card of your Draw Pile.
    public static class Mayhem extends CardColorless.Mayhem {
    }

    public static class MayhemP extends CardColorless.MayhemP {
    }

    // No need to implement Mimic: Multiplayer

    private static abstract class _NostalgiaT extends Card {
        public _NostalgiaT(String cardName, int energyCost) {
            super(cardName, Card.POWER, energyCost, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Nostalgia", this, 2, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    input[idx + 1] = state.getCounterForRead()[counterIdx + 1] / 3.0f;
                    return idx + 2;
                }

                @Override public int getInputLenDelta() {
                    return 2;
                }

                @Override public void onRegister(int counterIdx) {
                    state.properties.nostalgiaCounterIdx = counterIdx + 1;
                }
            });
            state.properties.addStartOfTurnHandler("Nostalgia", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx + 1] = state.getCounterForRead()[counterIdx];
                }
            });
        }
    }

    // Nostalgia (Rare) - 1 energy, Power
    //   Effect: The first Attack or Skill you play each turn is placed on top of your Draw Pile.
    //   Upgraded Effect (0 energy): The first Attack or Skill you play each turn is placed on top of your Draw Pile.
    public static class Nostalgia extends _NostalgiaT {
        public Nostalgia() {
            super("Nostalgia", 1);
        }
    }

    public static class NostalgiaP extends _NostalgiaT {
        public NostalgiaP() {
            super("Nostalgia+", 0);
        }
    }

    // No need to implement Rally: Multiplayer

    // TODO: Rend (Rare) - 2 energy, Attack
    //   Effect: Deal 15 damage. Deals 5 additional damage for each unique debuff on the enemy.
    //   Upgraded Effect: Deal 18 damage. Deals 8 additional damage for each unique debuff on the enemy.

    private static abstract class _RollingBoulderT extends Card {
        private final int initialDamage;

        public _RollingBoulderT(String cardName, int initialDamage) {
            super(cardName, Card.POWER, 3, Card.RARE);
            this.initialDamage = initialDamage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] = initialDamage;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("RollingBoulder", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 100.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("RollingBoulder", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int dmg = state.getCounterForRead()[counterIdx];
                    if (dmg > 0) {
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            state.playerDoNonAttackDamageToEnemy(enemy, dmg, true);
                        }
                        state.getCounterForWrite()[counterIdx] += 5;
                    }
                }
            });
        }
    }

    // Rolling Boulder (Rare) - 3 energy, Power
    //   Effect: At the start of your turn, deal 5 damage to ALL enemies and increase this damage by 5.
    //   Upgraded Effect: At the start of your turn, deal 10 damage to ALL enemies and increase this damage by 5.
    public static class RollingBoulder extends _RollingBoulderT {
        public RollingBoulder() {
            super("Rolling Boulder", 5);
        }
    }

    public static class RollingBoulderP extends _RollingBoulderT {
        public RollingBoulderP() {
            super("Rolling Boulder+", 10);
        }
    }

    private static abstract class _SalvoT extends Card {
        private final int damage;

        public _SalvoT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.RARE);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerRetainHandCounter(this);
        }
    }

    // Salvo (Rare) - 1 energy, Attack
    //   Effect: Deal 12 damage. Retain your Hand this turn.
    //   Upgraded Effect: Deal 16 damage. Retain your Hand this turn.
    public static class Salvo extends _SalvoT {
        public Salvo() {
            super("Salvo", 12);
        }
    }

    public static class SalvoP extends _SalvoT {
        public SalvoP() {
            super("Salvo+", 16);
        }
    }

    // Scrawl (Rare) - 1 energy, Skill
    //   Effect: Draw cards until your Hand is full. Exhaust.
    //   Upgraded Effect: Retain. Draw cards until your Hand is full. Exhaust.
    public static class Scrawl extends CardWatcher._ScrawlT {
        public Scrawl() {
            super("Scrawl", 1);
        }
    }

    public static class ScrawlP extends CardWatcher._ScrawlT {
        public ScrawlP() {
            super("Scrawl+", 0);
            retain = true;
        }
    }

    // Secret Technique (Rare) - 0 energy, Skill
    //   Effect: Put a Skill from your Draw Pile into your Hand. Exhaust.
    //   Upgraded Effect: Put a Skill from your Draw Pile into your Hand.
    public static class SecretTechnique extends CardColorless.SecretTechnique {
    }

    public static class SecretTechniqueP extends CardColorless.SecretTechniqueP {
    }

    // Secret Weapon (Rare) - 0 energy, Skill
    //   Effect: Put an Attack from your Draw Pile into your Hand. Exhaust.
    //   Upgraded Effect: Put an Attack from your Draw Pile into your Hand.
    public static class SecretWeapon extends CardColorless.SecretWeapon {
    }

    public static class SecretWeaponP extends CardColorless.SecretWeaponP {
    }

    private static abstract class _TheGambitT extends Card {
        private final int block;

        public _TheGambitT(String cardName, int block) {
            super(cardName, Card.SKILL, 0, Card.RARE);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.getCounterForWrite()[counterIdx] = 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("TheGambit", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx];
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnDamageHandler("TheGambit", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (isAttack && damageDealt > 0 && state.getCounterForRead()[counterIdx] > 0) {
                        state.getPlayerForWrite().setHealth(0);
                    }
                }
            });
        }
    }

    // The Gambit (Rare) - 0 energy, Skill
    //   Effect: Gain 50 Block. If you take unblocked attack damage this combat, die.
    //   Upgraded Effect: Gain 75 Block. If you take unblocked attack damage this combat, die.
    public static class TheGambit extends _TheGambitT {
        public TheGambit() {
            super("The Gambit", 50);
        }
    }

    public static class TheGambitP extends _TheGambitT {
        public TheGambitP() {
            super("The Gambit+", 75);
        }
    }

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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Byrd Swoop (Event) - 0 energy, Attack
    //   Effect: Deal 14 damage.
    //   Upgraded Effect: Deal 18 damage.
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

    // Enlightenment (Event) - 0 energy, Skill
    //   Effect: Reduce the cost of ALL cards in your Hand to 1 this turn. Exhaust.
    //   Upgraded Effect: Reduce the cost of ALL cards in your Hand to 1 this combat. Exhaust.
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
                    state.playerDoDamageToEnemy(enemy, damage, this);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Exterminate (Event) - 1 energy, Attack
    //   Effect: Deal 3 damage 4 times to ALL enemies.
    //   Upgraded Effect: Deal 4 damage 4 times to ALL enemies.
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

    private static abstract class _FeedingFrenzyT extends Card {
        private final int strength;

        public _FeedingFrenzyT(String cardName, int strength) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.strength = strength;
            entityProperty.changePlayerStrength = true;
            entityProperty.changePlayerStrengthEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainStrength(strength);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, strength);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Feeding Frenzy (Event) - 0 energy, Skill
    //   Effect: Gain 5 Strength this turn.
    //   Upgraded Effect: Gain 7 Strength this turn.
    public static class FeedingFrenzy extends _FeedingFrenzyT {
        public FeedingFrenzy() {
            super("Feeding Frenzy", 5);
        }
    }

    public static class FeedingFrenzyP extends _FeedingFrenzyT {
        public FeedingFrenzyP() {
            super("Feeding Frenzy+", 7);
        }
    }

    // Metamorphosis (Event) - 2 energy, Skill
    //   Effect: Add 3 random Attacks into your Draw Pile. They're free to play this combat. Exhaust.
    //   Upgraded Effect: Add 5 random Attacks into your Draw Pile. They're free to play this combat. Exhaust.
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
                state.playerDoDamageToEnemy(enemy, damage, this);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Peck (Event) - 1 energy, Attack
    //   Effect: Deal 2 damage 3 times.
    //   Upgraded Effect: Deal 2 damage 4 times.
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
            state.playerDoDamageToEnemy(enemy, damage, this);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, vulnerable);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Squash (Event) - 1 energy, Attack
    //   Effect: Deal 10 damage. Apply 2 Vulnerable.
    //   Upgraded Effect: Deal 12 damage. Apply 3 Vulnerable.
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

    private static abstract class _ToricToughnessT extends Card {
        private final int block;

        public _ToricToughnessT(String cardName, int block) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.getCounterForWrite()[state.properties.blockNextTurnCounterIdx] += block;
            state.getCounterForWrite()[counterIdx] += block;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerBlockNextTurnCounter(new GameProperties.LocalCounterRegistrant());
            state.properties.registerBlockNextNextTurnCounter(this);
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.blockNextNextTurnCounterIdx = idx;
        }
    }

    // Toric Toughness (Event) - 2 energy, Skill
    //   Effect: Gain 5 Block. Gain 5 Block at the start of the next 2 turns.
    //   Upgraded Effect: Gain 7 Block. Gain 7 Block at the start of the next 2 turns.
    public static class ToricToughness extends _ToricToughnessT {
        public ToricToughness() {
            super("Toric Toughness", 5);
        }
    }

    public static class ToricToughnessP extends _ToricToughnessT {
        public ToricToughnessP() {
            super("Toric Toughness+", 7);
        }
    }

    // TODO: Mad Science (Event) - 1 energy, Skill
    //   Effect: This card can be created and customized at the event Tinker Time.
    //   Upgraded Effect: Innate This card can be created and customized at the event Tinker Time.

    // **************************************************************************************************
    // ********************************************* Ancient *********************************************
    // **************************************************************************************************

    // Apotheosis (Ancient) - 2 energy, Skill
    //   Effect: Innate. Upgrade ALL your cards. Exhaust.
    //   Upgraded Effect (1 energy): Innate. Upgrade ALL your cards. Exhaust.
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

    // Apparition (Ancient) - 1 energy, Skill
    //   Effect: Ethereal. Gain 1 Intangible. Exhaust.
    //   Upgraded Effect: Gain 1 Intangible. Exhaust.
    public static class Apparition extends CardColorless.Apparition {
    }

    public static class ApparitionP extends CardColorless.ApparitionP {
    }

    private static abstract class _BrightestFlameT extends Card {
        private final int amount;

        public _BrightestFlameT(String cardName, int amount) {
            super(cardName, Card.SKILL, 0, Card.RARE);
            this.amount = amount;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(amount);
            state.draw(amount);
            var player = state.getPlayerForWrite();
            if (player.getHealth() == player.getInBattleMaxHealth()) {
                player.setInBattleMaxHealth(player.getInBattleMaxHealth() - 1);
                player.setHealth(player.getHealth() - 1);
            } else {
                player.setInBattleMaxHealth(player.getInBattleMaxHealth() - 1);
            }
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("BrightestFlame", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addExtraTrainingTarget("BrightestFlame", this, new TrainingTarget() {
                @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                    if (isTerminal > 0) {
                        v.setVExtra(vExtraIdx, state.getCounterForRead()[counterIdx] / 10.0);
                    } else if (isTerminal == 0) {
                        double vBF = Math.max(state.getCounterForRead()[counterIdx] / 10.0, state.getVExtra(vExtraIdx));
                        v.setVExtra(vExtraIdx, vBF);
                    }
                }

                @Override public void updateQValues(GameState state, VArray v) {
                    double vBF = v.getVExtra(vExtraIdx);
                    v.add(GameState.V_HEALTH_IDX, -10.0 * vBF / state.getPlayerForRead().getMaxHealth());
                }
            });
        }
    }

    // Brightest Flame (Ancient) - 0 energy, Skill
    //   Effect: Gain 2 energy. Draw 2 cards. Lose 1 Max HP.
    //   Upgraded Effect: Gain 3 energy. Draw 3 cards. Lose 1 Max HP.
    public static class BrightestFlame extends _BrightestFlameT {
        public BrightestFlame() {
            super("Brightest Flame", 2);
        }
    }

    public static class BrightestFlameP extends _BrightestFlameT {
        public BrightestFlameP() {
            super("Brightest Flame+", 3);
        }
    }

    // TODO: Maul (Ancient) - 1 energy, Attack
    //   Effect: Deal 5 damage twice. Increase the damage of ALL Maul cards by 1 this combat.
    //   Upgraded Effect: Deal 6 damage twice. Increase the damage of ALL Maul cards by 2 this combat.

    private static abstract class _NeowsFuryT extends Card {
        private final int damage;

        public _NeowsFuryT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.RARE);
            this.damage = damage;
            this.exhaustWhenPlayed = true;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            if (state.discardArrLen > 2) {
                state.setIsStochastic();
            }
            for (int i = 0; i < 2; i++) {
                if (state.discardArrLen > 0) {
                    int randIdx = state.getSearchRandomGen().nextInt(state.discardArrLen, RandomGenCtx.RandomCardGen, new Tuple<>(state, state.getDiscardArrForRead()));
                    int cardIdx = state.getDiscardArrForRead()[randIdx];
                    state.removeCardFromDiscardByPosition(randIdx);
                    state.addCardToHand(cardIdx);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Neow's Fury (Ancient) - 1 energy, Attack
    //   Effect: Deal 10 damage. Put 2 random cards from your Discard Pile into your Hand. Exhaust.
    //   Upgraded Effect: Deal 14 damage. Put 2 random cards from your Discard Pile into your Hand. Exhaust.
    // TODO CHANGED: Neow's Fury (Ancient) - 1 energy, Attack
    //   Effect: Deal 10 damage. Put 2 random cards from your Discard Pile into your Hand. Exhaust.
    //   Upgraded Effect: Deal 14 damage. Put 3 random cards from your Discard Pile into your Hand. Exhaust.
    public static class NeowsFury extends _NeowsFuryT {
        public NeowsFury() {
            super("Neow's Fury", 10);
        }
    }

    public static class NeowsFuryP extends _NeowsFuryT {
        public NeowsFuryP() {
            super("Neow's Fury+", 14);
        }
    }

    private static abstract class _RelaxT extends Card {
        private final int block;
        private final int amount;
        private final GameProperties.LocalCounterRegistrant energyRegistrant = new GameProperties.LocalCounterRegistrant();

        public _RelaxT(String cardName, int block, int amount) {
            super(cardName, Card.SKILL, 3, Card.RARE);
            this.block = block;
            this.amount = amount;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.getCounterForWrite()[counterIdx] += amount;
            state.getCounterForWrite()[energyRegistrant.getCounterIdx(state.properties)] += amount;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerDrawNextTurnCounter(this);
            state.properties.registerEnergyNextTurnCounter(state, energyRegistrant);
        }
    }

    // Relax (Ancient) - 3 energy, Skill
    //   Effect: Gain 15 Block. Next turn, draw 2 cards and gain 2 energy. Exhaust.
    //   Upgraded Effect: Gain 17 Block. Next turn, draw 3 cards and gain 3 energy. Exhaust.
    public static class Relax extends _RelaxT {
        public Relax() {
            super("Relax", 15, 2);
        }
    }

    public static class RelaxP extends _RelaxT {
        public RelaxP() {
            super("Relax+", 17, 3);
        }
    }

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

    // Wish (Ancient) - 0 energy, Skill
    //   Effect: Put a card from your Draw Pile into your Hand. Exhaust.
    //   Upgraded Effect: Retain. Put a card from your Draw Pile into your Hand. Exhaust.
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

    // Fuel (Token) - 0 energy, Skill
    //   Effect: Gain energy. Draw 1 card. Exhaust.
    //   Upgraded Effect: Gain energy. Draw 2 cards. Exhaust.
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Giant Rock (Token) - 1 energy, Attack
    //   Effect: Deal 16 damage.
    //   Upgraded Effect: Deal 20 damage.
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

    private static abstract class _LuminesceT extends Card {
        private final int energy;

        public _LuminesceT(String cardName, int energy) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.energy = energy;
            retain = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(energy);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Luminesce (Token) - 0 energy, Skill
    //   Effect: Retain. Gain 2 energy. Exhaust.
    //   Upgraded Effect: Retain. Gain 3 energy. Exhaust.
    public static class Luminesce extends _LuminesceT {
        public Luminesce() {
            super("Luminesce", 2);
        }
    }

    public static class LuminesceP extends _LuminesceT {
        public LuminesceP() {
            super("Luminesce+", 3);
        }
    }

    private static abstract class _MinionDiveBombT extends Card {
        private final int damage;

        public _MinionDiveBombT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Minion Dive Bomb (Token) - 0 energy, Attack
    //   Effect: Deal 13 damage. Exhaust.
    //   Upgraded Effect: Deal 16 damage. Exhaust.
    public static class MinionDiveBomb extends _MinionDiveBombT {
        public MinionDiveBomb() {
            super("Minion Dive Bomb", 13);
        }
    }

    public static class MinionDiveBombP extends _MinionDiveBombT {
        public MinionDiveBombP() {
            super("Minion Dive Bomb+", 16);
        }
    }

    private static abstract class _MinionSacrificeT extends Card {
        private final int block;

        public _MinionSacrificeT(String cardName, int block) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.block = block;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Minion Sacrifice (Token) - 0 energy, Skill
    //   Effect: Gain 8 Block. Exhaust.
    //   Upgraded Effect: Gain 11 Block. Exhaust.
    public static class MinionSacrifice extends _MinionSacrificeT {
        public MinionSacrifice() {
            super("Minion Sacrifice", 8);
        }
    }

    public static class MinionSacrificeP extends _MinionSacrificeT {
        public MinionSacrificeP() {
            super("Minion Sacrifice+", 11);
        }
    }

    private static abstract class _MinionStrikeT extends Card {
        private final int damage;

        public _MinionStrikeT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Minion Strike (Token) - 0 energy, Attack
    //   Effect: Deal 6 damage. Draw 1 card. Exhaust.
    //   Upgraded Effect: Deal 9 damage. Draw 1 card. Exhaust.
    public static class MinionStrike extends _MinionStrikeT {
        public MinionStrike() {
            super("Minion Strike", 6);
        }
    }

    public static class MinionStrikeP extends _MinionStrikeT {
        public MinionStrikeP() {
            super("Minion Strike+", 9);
        }
    }

    // Shiv (Token) - 0 energy, Attack
    //   Effect: Deal 4 damage. Exhaust.
    //   Upgraded Effect: Deal 6 damage. Exhaust.
    public static class Shiv extends CardColorless.Shiv {
    }

    public static class ShivP extends CardColorless.ShivP {
    }

    private static abstract class _SoulT extends Card {
        private final int draws;

        public _SoulT(String cardName, int draws) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.draws = draws;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.draw(draws);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Soul (Token) - 0 energy, Skill
    //   Effect: Draw 2 cards. Exhaust.
    //   Upgraded Effect: Draw 3 cards. Exhaust.
    public static class Soul extends _SoulT {
        public Soul() {
            super("Soul", 2);
        }
    }

    public static class SoulP extends _SoulT {
        public SoulP() {
            super("Soul+", 3);
        }
    }

    private static abstract class _SovereignBladeT extends Card {
        public _SovereignBladeT(String cardName, int energyCost) {
            super(cardName, Card.ATTACK, energyCost, Card.COMMON);
            this.retain = true;
            entityProperty.selectEnemy = true;
        }

        @Override public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int dmg = 10 + (state.properties.forgeCounterIdx >= 0
                ? state.getCounterForRead()[state.properties.forgeCounterIdx]
                : 0);
            if (state.properties.conquerorCounterIdx >= 0 && state.getCounterForRead()[state.properties.conquerorCounterIdx] > 0) {
                dmg <<= state.getCounterForRead()[state.properties.conquerorCounterIdx];
            }
            if (state.hasBuff(PlayerBuff.SEEKING_EDGE)) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    state.playerDoDamageToEnemy(enemy, dmg, this);
                }
            } else {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg, this);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Sovereign Blade (Token) - 2 energy, Attack
    //   Effect: Retain. Deal 10 damage.
    //   Upgraded Effect (1 energy): Retain. Deal 10 damage.
    public static class SovereignBlade extends _SovereignBladeT {
        public SovereignBlade() {
            super("Sovereign Blade", 2);
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerForgeCounter();
            state.properties.sovereignBladeCardIdx = state.properties.findCardIndex(this);
        }
    }

    public static class SovereignBladeP extends _SovereignBladeT {
        public SovereignBladeP() {
            super("Sovereign Blade+", 1);
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerForgeCounter();
        }
    }

    private static abstract class _SweepingGazeT extends Card {
        private final int damage;

        public _SweepingGazeT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.COMMON);
            this.damage = damage;
            this.ethereal = true;
            this.exhaustWhenPlayed = true;
            entityProperty.otsyAttack = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int enemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
            if (enemyIdx >= 0) {
                state.otsyDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(enemyIdx), damage);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Sweeping Gaze (Token) - 0 energy, Attack
    //   Effect: Ethereal. Osty deals 10 damage to a random enemy. Exhaust.
    //   Upgraded Effect: Ethereal. Osty deals 15 damage to a random enemy. Exhaust.
    public static class SweepingGaze extends _SweepingGazeT {
        public SweepingGaze() {
            super("Sweeping Gaze", 10);
        }
    }

    public static class SweepingGazeP extends _SweepingGazeT {
        public SweepingGazeP() {
            super("Sweeping Gaze+", 15);
        }
    }
}
