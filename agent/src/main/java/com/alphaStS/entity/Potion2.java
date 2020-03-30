package com.alphaStS.entity;

import com.alphaStS.GameProperties;
import com.alphaStS.GameState;
import com.alphaStS.card.Card;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.random.RandomGenCtx;
import com.alphaStS.utils.Tuple;

import java.util.List;

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

    // TODO: Gigantification Potion (Rare)
    //   Effect: The next Attack you play deals triple damage.

    public static class LiquidMemories extends Potion.LiquidMemories {
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
    //   Effect: Gain 10 Block. Next turn, gain 10 Block.

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
    //   Effect: Add 3 Upgraded Colorless cards into your Hand.

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
