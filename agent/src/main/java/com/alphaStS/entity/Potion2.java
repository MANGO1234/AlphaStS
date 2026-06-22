package com.alphaStS.entity;

import com.alphaStS.GameProperties;
import com.alphaStS.GameState;
import com.alphaStS.card.Card;
import com.alphaStS.card.CardColorless2;
import com.alphaStS.card.CardManager;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.random.RandomGenCtx;
import com.alphaStS.utils.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Potion2 {
    // **************************************************************************************************
    // ********************************************* Common *********************************************
    // **************************************************************************************************

    // Attack Potion (Common)
    //   Effect: Choose 1 of 3 random Attack cards to add into your Hand. It's free to play this turn.
    public static class AttackPotion extends Potion.AttackPotion {
    }

    // Block Potion (Common)
    //   Effect: Gain 12 Block.
    public static class BlockPotion extends Potion.BlockPotion {
    }

    // Colorless Potion (Common)
    //   Effect: Choose 1 of 3 random Colorless cards to add into your Hand. It's free to play this turn.
    public static class ColorlessPotion extends Potion.ColorlessPotion {
    }

    // Dexterity Potion (Common)
    //   Effect: Gain 2 Dexterity.
    public static class DexterityPotion extends Potion.DexterityPotion {
    }

    // Energy Potion (Common)
    //   Effect: Gain 2 energy.
    public static class EnergyPotion extends Potion.EnergyPotion {
    }

    // Explosive Ampoule (Common)
    //   Effect: Deal 10 damage to ALL enemies.
    public static class ExplosiveAmpoule extends Potion.ExplosivePotion {
    }

    // Fire Potion (Common)
    //   Effect: Deal 20 damage.
    public static class FirePotion extends Potion.FirePotion {
    }

    // Flex Potion (Common)
    //   Effect: Gain 5 Strength. At the end of your turn, lose 5 Strength.
    public static class FlexPotion extends Potion.FlexPotion {
    }

    // Power Potion (Common)
    //   Effect: Choose 1 of 3 random Power cards to add into your Hand. It's free to play this turn.
    public static class PowerPotion extends Potion.PowerPotion {
    }

    // Skill Potion (Common)
    //   Effect: Choose 1 of 3 random Skill cards to add into your Hand. It's free to play this turn.
    public static class SkillPotion extends Potion.SkillPotion {
    }

    // Speed Potion (Common)
    //   Effect: Gain 5 Dexterity. At the end of your turn, lose 5 Dexterity.
    public static class SpeedPotion extends Potion.SpeedPotion {
    }

    // Strength Potion (Common)
    //   Effect: Gain 2 Strength.
    public static class StrengthPotion extends Potion.StrengthPotion {
    }

    // Swift Potion (Common)
    //   Effect: Draw 3 cards.
    public static class SwiftPotion extends Potion.SwiftPotion {
    }

    // Vulnerable Potion (Common)
    //   Effect: Apply 3 Vulnerable.
    public static class VulnerablePotion extends Potion.FearPotion {
    }

    // Weak Potion (Common)
    //   Effect: Apply 3 Weak.
    public static class WeakPotion extends Potion.WeakPotion {
    }

    // **************************************************************************************************
    // ********************************************* Uncommon *********************************************
    // **************************************************************************************************

    // Blessing of the Forge (Uncommon)
    //   Effect: Upgrade all cards in your Hand for the rest of combat.
    public static class BlessingOfTheForge extends Potion.BlessingOfTheForge {
    }

    // Clarity Extract (Uncommon)
    //   Effect: Draw 1 card. At the start of your next 3 turns, draw 1 additional card.
    public static class ClarityExtract extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.sacredBark != null && state.properties.sacredBark.isRelicEnabledInScenario(state) ? 2 : 1;
            state.draw(n);
            state.getCounterForWrite()[counterIdx] += n * 3;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("ClarityExtract", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 6.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("ClarityExtract", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.draw(1);
                        state.getCounterForWrite()[counterIdx]--;
                    }
                }
            });
        }
    }

    // Cure All (Uncommon)
    //   Effect: Gain energy. Draw 2 cards.
    public static class CureAll extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.sacredBark != null && state.properties.sacredBark.isRelicEnabledInScenario(state) ? 2 : 1;
            state.gainEnergy(n);
            state.draw(n * 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Duplicator (Uncommon)
    //   Effect: This turn, your next card is played an extra time.
    public static class Duplicator extends Potion.DuplicationPotion {
    }

    // Fortifier (Uncommon)
    //   Effect: Triple your Block.
    public static class Fortifier extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            int multiplier = state.properties.sacredBark != null && state.properties.sacredBark.isRelicEnabledInScenario(state) ? 5 : 2;
            int currentBlock = state.getPlayerForRead().getBlock();
            state.playerGainBlockNotFromCardPlay(currentBlock * multiplier);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Fysh Oil (Uncommon)
    //   Effect: Gain 1 Strength and 1 Dexterity.
    public static class FyshOil extends Potion {
        public FyshOil() {
            entityProperty.changePlayerStrength = true;
            entityProperty.changePlayerDexterity = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.sacredBark != null && state.properties.sacredBark.isRelicEnabledInScenario(state) ? 2 : 1;
            state.getPlayerForWrite().gainStrength(n);
            state.getPlayerForWrite().gainDexterity(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Gambler's Brew (Uncommon)
    //   Effect: Discard any number of cards, then draw that many.
    public static class GamblersBrew extends Potion.GamblersBrew {
    }

    // Heart of Iron (Uncommon)
    //   Effect: Gain 7 Plating.
    public static class HeartOfIron extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            int platingAmount = state.properties.sacredBark != null && state.properties.sacredBark.isRelicEnabledInScenario(state) ? 14 : 7;
            state.getCounterForWrite()[state.properties.platingCounterIdx] += platingAmount;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerPlatingCounter();
        }
    }

    // Liquid Bronze (Uncommon)
    //   Effect: Gain 3 Thorns.
    public static class LiquidBronze extends Potion.LiquidBronze {
    }

    // Potion of Binding (Uncommon)
    //   Effect: Apply 1 Weak and 1 Vulnerable to ALL enemies.
    public static class PotionOfBinding extends Potion {
        public PotionOfBinding() {
            entityProperty.weakEnemy = true;
            entityProperty.vulnEnemy = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.sacredBark != null && state.properties.sacredBark.isRelicEnabledInScenario(state) ? 2 : 1;
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.WEAK, n);
                enemy.applyDebuff(state, DebuffType.VULNERABLE, n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Powdered Demise (Uncommon)
    //   Effect: Enemy loses 9 HP at the end of each of its turns.
    public static class PowderedDemise extends Potion {
        public PowderedDemise() {
            entityProperty.selectEnemy = true;
            entityProperty.powderedDemiseEnemy = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.sacredBark != null && state.properties.sacredBark.isRelicEnabledInScenario(state) ? 18 : 9;
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.POWDERED_DEMISE, n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Radiant Tincture (Uncommon)
    //   Effect: Gain energy. Gain an additional energy at the start of your next 3 turns.
    public static class RadiantTincture extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.sacredBark != null && state.properties.sacredBark.isRelicEnabledInScenario(state) ? 2 : 1;
            state.gainEnergy(n);
            state.getCounterForWrite()[counterIdx] += n * 3;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("RadiantTincture", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 6.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("RadiantTincture", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.gainEnergy(1);
                        state.getCounterForWrite()[counterIdx]--;
                    }
                }
            });
        }
    }

    // Regen Potion (Uncommon)
    //   Effect: Gain 5 Regen.
    public static class RegenPotion extends Potion.RegenPotion {
    }

    // Stable Serum (Uncommon)
    //   Effect: Retain your Hand for 2 turns.
    public static class StableSerum extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.sacredBark != null && state.properties.sacredBark.isRelicEnabledInScenario(state) ? 4 : 2;
            state.getCounterForWrite()[state.properties.retainHandCounterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerRetainHandCounter(this);
        }
    }

    // Touch of Insanity (Uncommon)
    //   Effect: Choose a card in your Hand. It is free to play this combat.
    public static class TouchOfInsanity extends Potion {
        public TouchOfInsanity() {
            entityProperty.selectFromHand = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            if (idx < 0 || idx >= state.properties.cardDict.length) {
                return GameActionCtx.PLAY_CARD;
            }
            if (state.properties.tmp0CostCardTransformIdxes[idx] >= 0) {
                state.removeCardFromHand(idx);
                state.addCardToHand(state.properties.tmp0CostCardTransformIdxes[idx]);
            }
            if (state.properties.sacredBark != null && state.properties.sacredBark.isRelicEnabledInScenario(state)) {
                state.getCounterForWrite()[counterIdx]++;
                if (state.getCounterForRead()[counterIdx] == 2) {
                    state.getCounterForWrite()[counterIdx] = 0;
                    return GameActionCtx.PLAY_CARD;
                }
                return GameActionCtx.SELECT_CARD_HAND;
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            return cards.stream().filter((x) -> !x.isXCost && x.energyCost > 0).map((x) -> (Card) x.getTemporaryCostIfPossible(0)).toList();
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("TouchOfInsanity", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 2.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }
    }

    // **************************************************************************************************
    // *********************************************  Rare  *********************************************
    // **************************************************************************************************

    // Beetle Juice (Rare)
    //   Effect: Enemy's attacks deal 30% less damage for the next 4 turns.
    public static class BeetleJuice extends Potion {
        public BeetleJuice() {
            entityProperty.selectEnemy = true;
            entityProperty.beetleJuiceEnemy = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.sacredBark != null && state.properties.sacredBark.isRelicEnabledInScenario(state) ? 8 : 4;
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.BEETLE_JUICE, n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Bottled Potential (Rare)
    //   Effect: Shuffle ALL your cards into your Draw Pile. Draw 5 cards.
    public static class BottledPotential extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.sacredBark != null && state.properties.sacredBark.isRelicEnabledInScenario(state) ? 10 : 5;
            state.discardHand(false);
            state.reshuffle();
            state.draw(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Distilled Chaos (Rare)
    //   Effect: Play the top 3 cards of your Draw Pile.
    public static class DistilledChaos extends Potion.DistilledChaos {
    }

    // Droplet of Precognition (Rare)
    //   Effect: Choose a card in your Draw Pile and add it into your Hand.
    public static class DropletOfPrecognition extends Potion {
        public DropletOfPrecognition() {
            entityProperty.selectFromDeck = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            if (idx < 0) {
                state.getCounterForWrite()[counterIdx] = 0;
                return GameActionCtx.PLAY_CARD;
            }
            state.removeCardFromDeck(idx, false);
            state.addCardToHand(idx);
            if (state.properties.sacredBark != null && state.properties.sacredBark.isRelicEnabledInScenario(state)) {
                state.getCounterForWrite()[counterIdx]++;
                if (state.getCounterForRead()[counterIdx] == 2 || state.getNumCardsInDeck() == 0) {
                    state.getCounterForWrite()[counterIdx] = 0;
                    return GameActionCtx.PLAY_CARD;
                }
                return GameActionCtx.SELECT_CARD_DECK;
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("DropletOfPrecognition", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 2.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }
    }

    // Entropic Brew (Rare)
    //   Effect: Fill all your empty potion slots with random potions.
    public static class EntropicBrew extends Potion.EntropicBrew {
    }

    // Fairy in a Bottle (Rare)
    //   Effect: When you would die, instead this potion is discarded and you heal to 30% of your Max HP.
    public static class FairyInABottle extends Potion.FairyInABottle {
    }

    // No need to implement Fruit Juice

    // Gigantification Potion (Rare)
    //   Effect: The next Attack you play deals triple damage.
    public static class GigantificationPotion extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.getCounterForWrite()[state.properties.gigantificationCounterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Gigantification", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 2.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int counterIdx) {
                    state.properties.gigantificationCounterIdx = counterIdx;
                }
            });
        }
    }

    // Liquid Memories (Rare)
    //   Effect: Put a card from your Discard Pile into your Hand. It costs 0 energy this turn.
    public static class LiquidMemories extends Potion.LiquidMemories {
    }

    // Lucky Tonic (Rare)
    //   Effect: Gain 1 Buffer.
    public static class LuckyTonic extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.getCounterForWrite()[state.properties.bufferCounterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerBufferCounter(state, this);
        }
    }

    // Mazaleth's Gift (Rare)
    //   Effect: Gain 1 Ritual.
    public static class MazalethsGift extends Potion.CultistPotion {
    }

    // Orobic Acid (Rare)
    //   Effect: Add a random Attack, Skill, and Power into your Hand. They're free to play this turn.
    public static class OrobicAcid extends Potion {
        private int[] attackIdxes;
        private int[] skillIdxes;
        private int[] powerIdxes;

        @Override public GameActionCtx use(GameState state, int idx) {
            state.setIsStochastic();
            int r = state.getSearchRandomGen().nextInt(attackIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, attackIdxes));
            state.addCardToHand(state.createCard(attackIdxes[r]));
            r = state.getSearchRandomGen().nextInt(skillIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, skillIdxes));
            state.addCardToHand(state.createCard(skillIdxes[r]));
            r = state.getSearchRandomGen().nextInt(powerIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, powerIdxes));
            state.addCardToHand(state.createCard(powerIdxes[r]));
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties props, List<Card> cards) {
            var all = new ArrayList<Card>();
            all.addAll(CardManager.getCharacterCardsByTypeTmp0Cost(props.character, Card.ATTACK, false));
            all.addAll(CardManager.getCharacterCardsByTypeTmp0Cost(props.character, Card.SKILL, false));
            all.addAll(CardManager.getCharacterCardsByTypeTmp0Cost(props.character, Card.POWER, false));
            return all;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var atkList = new ArrayList<Integer>();
            var sklList = new ArrayList<Integer>();
            var pwrList = new ArrayList<Integer>();
            for (int i : generatedCardIdxes) {
                int type = state.properties.cardDict[i].getBaseCard().cardType;
                if (type == Card.ATTACK) atkList.add(i);
                else if (type == Card.SKILL) sklList.add(i);
                else if (type == Card.POWER) pwrList.add(i);
            }
            attackIdxes = atkList.stream().mapToInt(x -> x).toArray();
            skillIdxes = sklList.stream().mapToInt(x -> x).toArray();
            powerIdxes = pwrList.stream().mapToInt(x -> x).toArray();
        }
    }

    // Shackling Potion (Rare)
    //   Effect: ALL enemies lose 7 Strength this turn.
    public static class ShacklingPotion extends Potion {
        public ShacklingPotion() {
            entityProperty.affectEnemyStrength = true;
            entityProperty.affectEnemyStrengthEot = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, 7);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Ship in a Bottle (Rare)
    //   Effect: Gain 10 Block. Next turn, gain 10 Block.
    public static class ShipInABottle extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.playerGainBlockNotFromCardPlay(10);
            state.getCounterForWrite()[counterIdx] += 10;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerBlockNextTurnCounter(this);
        }
    }

    // STS2 Snecko Oil draws 7 cards (up from 5 in STS1).
    // Snecko Oil (Rare)
    //   Effect: Draw 7 cards. Randomize the cost of cards in your Hand this turn.
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

    // Foul Potion (Event)
    //   Effect: Deal 12 damage to EVERYONE. Can be thrown at the Merchant for 100 Gold instead.
    public static class FoulPotion extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoNonAttackDamageToEnemy(enemy, 12, true);
            }
            state.doNonAttackDamageToPlayer(12, false, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Glowwater Potion (Event)
    //   Effect: Exhaust your Hand. Draw 10 cards.
    public static class GlowwaterPotion extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            for (int i = state.handArrLen - 1; i >= 0; i--) {
                state.exhaustCardFromHandByPosition(i, false);
            }
            state.updateHandArr();
            state.draw(10);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // **************************************************************************************************
    // ********************************************* Token  *********************************************
    // **************************************************************************************************

    // Potion-Shaped Rock (Token)
    //   Effect: Deal 15 damage.
    public static class PotionShapedRock extends Potion {
        public PotionShapedRock() {
            entityProperty.selectEnemy = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 15, true);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // **************************************************************************************************
    // ********************************************* Ironclad *********************************************
    // **************************************************************************************************

    // Ashwater (Uncommon)
    //   Effect: Exhaust any number of cards in your Hand.
    public static class Ashwater extends Potion.Elixir {
    }

    // Blood Potion (Common)
    //   Effect: Heal for 20% of your Max HP.
    public static class BloodPotion extends Potion.BloodPotion {
    }

    // Soldier's Stew (Rare)
    //   Effect: All cards containing Strike gain 1 Replay this combat.
    public static class SoldiersStew extends Potion {
        private int[] spiralStrikeTransformIdxes;

        @Override public GameActionCtx use(GameState state, int idx) {
            if (spiralStrikeTransformIdxes != null) {
                state.handArrTransform(spiralStrikeTransformIdxes);
                state.deckArrTransform(spiralStrikeTransformIdxes);
                state.discardArrTransform(spiralStrikeTransformIdxes);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return cards.stream().filter(CardManager::isStrike).map(c -> c.enchantSpiral(1)).toList();
        }

        @Override public void gamePropertiesSetup(GameState state) {
            spiralStrikeTransformIdxes = new int[state.properties.cardDict.length];
            java.util.Arrays.fill(spiralStrikeTransformIdxes, -1);
            for (int i = 0; i < state.properties.cardDict.length; i++) {
                if (state.properties.isStrikeCard[i]) {
                    int enchantedIdx = state.properties.findCardIndex(state.properties.cardDict[i].enchantSpiral(1));
                    if (enchantedIdx >= 0) {
                        spiralStrikeTransformIdxes[i] = enchantedIdx;
                    }
                }
            }
        }
    }

    // **************************************************************************************************
    // ********************************************* Silent *********************************************
    // **************************************************************************************************

    // Cunning Potion (Uncommon)
    //   Effect: Add 3 Upgraded Shivs into your Hand.
    public static class CunningPotion extends Potion.CunningPotion {
    }

    // Ghost in a Jar (Rare)
    //   Effect: Gain 1 Intangible.
    public static class GhostInAJar extends Potion.GhostInAJar {
    }

    // Poison Potion (Common)
    //   Effect: Apply 6 Poison.
    public static class PoisonPotion extends Potion.PoisonPotion {
    }

    // **************************************************************************************************
    // ********************************************* Defect *********************************************
    // **************************************************************************************************

    // Essence of Darkness (Rare)
    //   Effect: Channel a Dark for each of your Orb Slots.
    public static class EssenceOfDarkness extends Potion.EssenceOfDarkness {
    }

    // Focus Potion (Common)
    //   Effect: Gain 2 Focus.
    public static class FocusPotion extends Potion.FocusPotion {
    }

    // Potion of Capacity (Uncommon)
    //   Effect: Gain 2 Orb Slots.
    public static class PotionOfCapacity extends Potion.PotionOfCapacity {
    }

    // **************************************************************************************************
    // ********************************************* Regent *********************************************
    // **************************************************************************************************

    // Cosmic Concoction (Rare)
    //   Effect: Add 3 Upgraded Colorless cards into your Hand.
    public static class CosmicConcoction extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.setIsStochastic();
            for (int i = 0; i < 3; i++) {
                int r = state.getSearchRandomGen().nextInt(generatedCardIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, generatedCardIdxes));
                state.addCardToHand(state.createCard(generatedCardIdxes[r]));
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties props, List<Card> cards) {
            return CardManager.getColorlessCards(false).stream()
                    .map(Card::getUpgrade).filter(Objects::nonNull).toList();
        }
    }

    // King's Courage (Uncommon)
    //   Effect: Forge 15.
    public static class KingsCourage extends Potion {
        public KingsCourage() {
            entityProperty.canForge = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.forge(15);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Star Potion (Common)
    //   Effect: Gain 3 star
    public static class StarPotion extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.gainStar(3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // **************************************************************************************************
    // ********************************************* Necrobinder *********************************************
    // **************************************************************************************************

    // Bone Brew (Uncommon)
    //   Effect: Summon 15.
    public static class BoneBrew extends Potion {
        public BoneBrew() {
            entityProperty.canSummon = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.summon(15);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Pot of Ghouls (Rare)
    //   Effect: Add 2 Souls into your Hand.
    public static class PotOfGhouls extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.addCardToHand(generatedCardIdx);
            state.addCardToHand(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties props, List<Card> cards) {
            return List.of(new CardColorless2.Soul());
        }
    }

    // Potion of Doom (Common)
    //   Effect: Apply 33 Doom.
    public static class PotionOfDoom extends Potion {
        public PotionOfDoom() {
            entityProperty.selectEnemy = true;
            entityProperty.doomEnemy = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.DOOM, 33);
            return GameActionCtx.PLAY_CARD;
        }
    }
}
