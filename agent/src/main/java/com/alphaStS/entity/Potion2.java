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

    public static class CureAll extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.sacredBark != null && state.properties.sacredBark.isRelicEnabledInScenario(state) ? 2 : 1;
            state.gainEnergy(n);
            state.draw(n * 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Duplicator extends Potion.DuplicationPotion {
    }

    public static class Fortifier extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            int multiplier = state.properties.sacredBark != null && state.properties.sacredBark.isRelicEnabledInScenario(state) ? 5 : 2;
            int currentBlock = state.getPlayerForRead().getBlock();
            state.playerGainBlockNotFromCardPlay(currentBlock * multiplier);
            return GameActionCtx.PLAY_CARD;
        }
    }

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

    public static class GamblersBrew extends Potion.GamblersBrew {
    }

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

    public static class LiquidBronze extends Potion.LiquidBronze {
    }

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

    public static class RegenPotion extends Potion.RegenPotion {
    }

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

    public static class BottledPotential extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            int n = state.properties.sacredBark != null && state.properties.sacredBark.isRelicEnabledInScenario(state) ? 10 : 5;
            state.discardHand(false);
            state.reshuffle();
            state.draw(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DistilledChaos extends Potion.DistilledChaos {
    }

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

    public static class EntropicBrew extends Potion.EntropicBrew {
    }

    public static class FairyInABottle extends Potion.FairyInABottle {
    }

    // No need to implement Fruit Juice

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

    public static class LiquidMemories extends Potion.LiquidMemories {
    }

    public static class LuckyTonic extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.getCounterForWrite()[state.properties.bufferCounterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerBufferCounter(state, this);
        }
    }

    public static class MazalethsGift extends Potion.CultistPotion {
    }

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

    public static class FoulPotion extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoNonAttackDamageToEnemy(enemy, 12, true);
            }
            state.doNonAttackDamageToPlayer(12, false, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

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

    public static class Ashwater extends Potion.Elixir {
    }

    public static class BloodPotion extends Potion.BloodPotion {
    }

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

    public static class KingsCourage extends Potion {
        public KingsCourage() {
            entityProperty.canForge = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.forge(15);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class StarPotion extends Potion {
        @Override public GameActionCtx use(GameState state, int idx) {
            state.gainStar(3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // **************************************************************************************************
    // ********************************************* Necrobinder *********************************************
    // **************************************************************************************************

    public static class BoneBrew extends Potion {
        public BoneBrew() {
            entityProperty.canSummon = true;
        }

        @Override public GameActionCtx use(GameState state, int idx) {
            state.summon(15);
            return GameActionCtx.PLAY_CARD;
        }
    }

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
