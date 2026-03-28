package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.eventHandler.OnCardCreationHandler;
import com.alphaStS.eventHandler.OnEnergySpendHandler;
import com.alphaStS.eventHandler.OnStarChangeHandler;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.random.RandomGenCtx;
import com.alphaStS.utils.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CardRegent2 {
    // **************************************************************************************************
    // ********************************************* Basic  *********************************************
    // **************************************************************************************************

    public static class Defend extends Card.Defend {
    }

    public static class DefendP extends Card.DefendP {
    }

    private static abstract class _FallingStarT extends Card {
        private final int dmg;

        public _FallingStarT(String cardName, int dmg) {
            super(cardName, Card.ATTACK, 0, Card.COMMON);
            this.dmg = dmg;
            this.starCost = 2;
            entityProperty.selectEnemy = true;
            entityProperty.weakEnemy = true;
            entityProperty.vulnEnemy = true;
            entityProperty.hasStarCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, dmg);
            enemy.applyDebuff(state, DebuffType.WEAK, 1);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, 1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FallingStar extends _FallingStarT {
        public FallingStar() {
            super("Falling Star", 7);
        }
    }

    public static class FallingStarP extends _FallingStarT {
        public FallingStarP() {
            super("Falling Star+", 11);
        }
    }

    public static class Strike extends Card.Strike {
    }

    public static class StrikeP extends Card.StrikeP {
    }

    private static abstract class _VenerateT extends Card {
        private final int n;

        public _VenerateT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainStar(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Venerate extends _VenerateT {
        public Venerate() {
            super("Venerate", 2);
        }
    }

    public static class VenerateP extends _VenerateT {
        public VenerateP() {
            super("Venerate+", 3);
        }
    }

    // **************************************************************************************************
    // ********************************************* Common *********************************************
    // **************************************************************************************************

    private static abstract class _AstralPulseT extends Card {
        private final int dmg;

        public _AstralPulseT(String cardName, int dmg) {
            super(cardName, Card.ATTACK, 0, Card.COMMON);
            this.dmg = dmg;
            this.starCost = 3;
            entityProperty.hasStarCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, dmg);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class AstralPulse extends _AstralPulseT {
        public AstralPulse() {
            super("Astral Pulse", 14);
        }
    }

    public static class AstralPulseP extends _AstralPulseT {
        public AstralPulseP() {
            super("Astral Pulse+", 18);
        }
    }

    private static abstract class _BegoneT extends Card {
        private final int dmg;

        public _BegoneT(String cardName, int dmg) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.dmg = dmg;
            entityProperty.selectEnemy = true;
            entityProperty.selectFromHand = true;
            selectFromHandLater = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
                return state.getNumCardsInHand() > 0 ? GameActionCtx.SELECT_CARD_HAND : GameActionCtx.PLAY_CARD;
            } else {
                var hand = state.getHandArrForRead();
                for (int i = 0; i < state.getNumCardsInHand(); i++) {
                    if (hand[i] == idx) {
                        state.transformCard(state.getHandArrForWrite(), i, generatedCardIdx);
                        break;
                    }
                }
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class Begone extends _BegoneT {
        public Begone() {
            super("BEGONE!", 4);
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.MinionDiveBomb());
        }
    }

    public static class BegoneP extends _BegoneT {
        public BegoneP() {
            super("BEGONE!+", 5);
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.MinionDiveBombP());
        }
    }

    private static abstract class _CelestialMightT extends Card {
        private final int dmg;

        public _CelestialMightT(String cardName, int dmg) {
            super(cardName, Card.ATTACK, 2, Card.COMMON);
            this.dmg = dmg;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            for (int i = 0; i < 3; i++) {
                state.playerDoDamageToEnemy(enemy, dmg);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class CelestialMight extends _CelestialMightT {
        public CelestialMight() {
            super("Celestial Might", 6);
        }
    }

    public static class CelestialMightP extends _CelestialMightT {
        public CelestialMightP() {
            super("Celestial Might+", 8);
        }
    }

    private static abstract class _CloakOfStarsT extends Card {
        private final int block;

        public _CloakOfStarsT(String cardName, int block) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.block = block;
            this.starCost = 1;
            entityProperty.hasStarCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class CloakOfStars extends _CloakOfStarsT {
        public CloakOfStars() {
            super("Cloak of Stars", 7);
        }
    }

    public static class CloakOfStarsP extends _CloakOfStarsT {
        public CloakOfStarsP() {
            super("Cloak of Stars+", 10);
        }
    }

    private static abstract class _CollisionCourseT extends Card {
        private final int dmg;

        public _CollisionCourseT(String cardName, int dmg) {
            super(cardName, Card.ATTACK, 0, Card.COMMON);
            this.dmg = dmg;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            state.addCardToHand(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardOther2.Debris());
        }
    }

    public static class CollisionCourse extends _CollisionCourseT {
        public CollisionCourse() {
            super("Collision Course", 9);
        }
    }

    public static class CollisionCourseP extends _CollisionCourseT {
        public CollisionCourseP() {
            super("Collision Course+", 12);
        }
    }

    private static abstract class _CosmicIndifferenceT extends Card {
        private final int block;

        public _CosmicIndifferenceT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.block = block;
            entityProperty.selectFromDiscard = true;
            entityProperty.putCardOnTopDeck = true;
            selectFromDiscardLater = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.playerGainBlock(block);
                return state.getNumCardsInDiscard() > 0 ? GameActionCtx.SELECT_CARD_DISCARD : GameActionCtx.PLAY_CARD;
            } else {
                state.removeCardFromDiscard(idx);
                state.addCardOnTopOfDeck(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class CosmicIndifference extends _CosmicIndifferenceT {
        public CosmicIndifference() {
            super("Cosmic Indifference", 6);
        }
    }

    public static class CosmicIndifferenceP extends _CosmicIndifferenceT {
        public CosmicIndifferenceP() {
            super("Cosmic Indifference+", 9);
        }
    }

    private static abstract class _CrescentSpearT extends Card {
        private final int baseDmg;
        private final int bonusDmg;

        public _CrescentSpearT(String cardName, int baseDmg, int bonusDmg) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.baseDmg = baseDmg;
            this.bonusDmg = bonusDmg;
            this.starCost = 1;
            entityProperty.selectEnemy = true;
            entityProperty.hasStarCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int count = 0;
            var hand = state.getHandArrForRead();
            for (int i = 0; i < state.getNumCardsInHand(); i++) {
                if (state.properties.cardDict[hand[i]].starCost > 0) count++;
            }
            var deck = state.getDeckArrForRead();
            for (int i = 0; i < state.getNumCardsInDeck(); i++) {
                if (state.properties.cardDict[deck[i]].starCost > 0) count++;
            }
            var discard = state.getDiscardArrForRead();
            for (int i = 0; i < state.getNumCardsInDiscard(); i++) {
                if (state.properties.cardDict[discard[i]].starCost > 0) count++;
            }
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), baseDmg + bonusDmg * count);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class CrescentSpear extends _CrescentSpearT {
        public CrescentSpear() {
            super("Crescent Spear", 6, 2);
        }
    }

    public static class CrescentSpearP extends _CrescentSpearT {
        public CrescentSpearP() {
            super("Crescent Spear+", 6, 3);
        }
    }

    private static abstract class _CrushUnderT extends Card {
        private final int dmg;
        private final int strengthLoss;

        public _CrushUnderT(String cardName, int dmg, int strengthLoss) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.dmg = dmg;
            this.strengthLoss = strengthLoss;
            entityProperty.affectEnemyStrength = true;
            entityProperty.affectEnemyStrengthEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, dmg);
                enemy.applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, strengthLoss);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class CrushUnder extends _CrushUnderT {
        public CrushUnder() {
            super("Crush Under", 7, 1);
        }
    }

    public static class CrushUnderP extends _CrushUnderT {
        public CrushUnderP() {
            super("Crush Under+", 8, 2);
        }
    }

    private static abstract class _GatherLightT extends Card {
        private final int block;

        public _GatherLightT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.gainStar(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class GatherLight extends _GatherLightT {
        public GatherLight() {
            super("Gather Light", 7);
        }
    }

    public static class GatherLightP extends _GatherLightT {
        public GatherLightP() {
            super("Gather Light+", 10);
        }
    }

    private static abstract class _GlitterstreamT extends Card {
        private final int block;
        private final int nextBlock;

        public _GlitterstreamT(String cardName, int block, int nextBlock) {
            super(cardName, Card.SKILL, 2, Card.COMMON);
            this.block = block;
            this.nextBlock = nextBlock;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.getCounterForWrite()[counterIdx] += nextBlock;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerBlockNextTurnCounter(this);
        }
    }

    public static class Glitterstream extends _GlitterstreamT {
        public Glitterstream() {
            super("Glitterstream", 11, 4);
        }
    }

    public static class GlitterstreamP extends _GlitterstreamT {
        public GlitterstreamP() {
            super("Glitterstream+", 13, 6);
        }
    }

    private static abstract class _GlowT extends Card {
        private final int stars;

        public _GlowT(String cardName, int stars) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.stars = stars;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainStar(stars);
            state.draw(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Glow extends _GlowT {
        public Glow() {
            super("Glow", 1);
        }
    }

    public static class GlowP extends _GlowT {
        public GlowP() {
            super("Glow+", 2);
        }
    }

    private static abstract class _GuidingStarT extends Card {
        private final int dmg;
        private final int drawCount;

        public _GuidingStarT(String cardName, int dmg, int drawCount) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.dmg = dmg;
            this.drawCount = drawCount;
            this.starCost = 2;
            entityProperty.selectEnemy = true;
            entityProperty.hasStarCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            state.getCounterForWrite()[counterIdx] += drawCount;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerDrawNextTurnCounter(this);
        }
    }

    public static class GuidingStar extends _GuidingStarT {
        public GuidingStar() {
            super("Guiding Star", 12, 2);
        }
    }

    public static class GuidingStarP extends _GuidingStarT {
        public GuidingStarP() {
            super("Guiding Star+", 13, 3);
        }
    }

    private static abstract class _HiddenCacheT extends Card {
        private final int nextStar;

        public _HiddenCacheT(String cardName, int nextStar) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.nextStar = nextStar;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainStar(1);
            state.getCounterForWrite()[counterIdx] += nextStar;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerStarNextTurnCounter(this);
        }
    }

    public static class HiddenCache extends _HiddenCacheT {
        public HiddenCache() {
            super("Hidden Cache", 3);
        }
    }

    public static class HiddenCacheP extends _HiddenCacheT {
        public HiddenCacheP() {
            super("Hidden Cache+", 4);
        }
    }

    private static abstract class _KnowThyPlaceT extends Card {
        public _KnowThyPlaceT(String cardName, boolean exhaust) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.exhaustWhenPlayed = exhaust;
            entityProperty.weakEnemy = true;
            entityProperty.vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.WEAK, 1);
                enemy.applyDebuff(state, DebuffType.VULNERABLE, 1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class KnowThyPlace extends _KnowThyPlaceT {
        public KnowThyPlace() {
            super("Know Thy Place", true);
        }
    }

    public static class KnowThyPlaceP extends _KnowThyPlaceT {
        public KnowThyPlaceP() {
            super("Know Thy Place+", false);
        }
    }

    private static abstract class _PatterT extends Card {
        private final int block;
        private final int vigor;

        public _PatterT(String cardName, int block, int vigor) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.block = block;
            this.vigor = vigor;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.getCounterForWrite()[state.properties.vigorCounterIdx] += vigor;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerVigorCounter(this);
        }
    }

    public static class Patter extends _PatterT {
        public Patter() {
            super("Patter", 8, 2);
        }
    }

    public static class PatterP extends _PatterT {
        public PatterP() {
            super("Patter+", 10, 3);
        }
    }

    private static abstract class _PhotonCutT extends Card {
        private final int dmg;
        private final int drawCount;

        public _PhotonCutT(String cardName, int dmg, int drawCount) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.dmg = dmg;
            this.drawCount = drawCount;
            entityProperty.selectEnemy = true;
            entityProperty.selectFromHand = true;
            entityProperty.putCardOnTopDeck = true;
            selectFromHandLater = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
                state.draw(drawCount);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.removeCardFromHand(idx);
                state.addCardOnTopOfDeck(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class PhotonCut extends _PhotonCutT {
        public PhotonCut() {
            super("Photon Cut", 10, 1);
        }
    }

    public static class PhotonCutP extends _PhotonCutT {
        public PhotonCutP() {
            super("Photon Cut+", 13, 2);
        }
    }

    private static abstract class _RefineBladeT extends Card {
        private final int forgeAmount;

        public _RefineBladeT(String cardName, int forgeAmount) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.forgeAmount = forgeAmount;
            entityProperty.canForge = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.forge(forgeAmount);
            state.getCounterForWrite()[counterIdx] += 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerEnergyNextTurnCounter(state, this);
        }
    }

    public static class RefineBladeCard extends _RefineBladeT {
        public RefineBladeCard() {
            super("Refine Blade", 6);
        }
    }

    public static class RefineBladeCardP extends _RefineBladeT {
        public RefineBladeCardP() {
            super("Refine Blade+", 10);
        }
    }

    private static abstract class _SolarStrikeT extends Card {
        private final int dmg;
        private final int stars;

        public _SolarStrikeT(String cardName, int dmg, int stars) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.dmg = dmg;
            this.stars = stars;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            state.gainStar(stars);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SolarStrike extends _SolarStrikeT {
        public SolarStrike() {
            super("Solar Strike", 8, 1);
        }
    }

    public static class SolarStrikeP extends _SolarStrikeT {
        public SolarStrikeP() {
            super("Solar Strike+", 9, 2);
        }
    }

    private static abstract class _SpoilsOfBattleT extends Card {
        private final int forgeAmount;

        public _SpoilsOfBattleT(String cardName, int forgeAmount) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.forgeAmount = forgeAmount;
            entityProperty.canForge = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.forge(forgeAmount);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SpoilsOfBattle extends _SpoilsOfBattleT {
        public SpoilsOfBattle() {
            super("Spoils of Battle", 10);
        }
    }

    public static class SpoilsOfBattleP extends _SpoilsOfBattleT {
        public SpoilsOfBattleP() {
            super("Spoils of Battle+", 15);
        }
    }

    private static abstract class _WroughtInWarT extends Card {
        private final int dmg;
        private final int forgeAmount;

        public _WroughtInWarT(String cardName, int dmg, int forgeAmount) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.dmg = dmg;
            this.forgeAmount = forgeAmount;
            entityProperty.selectEnemy = true;
            entityProperty.canForge = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            state.forge(forgeAmount);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class WroughtInWar extends _WroughtInWarT {
        public WroughtInWar() {
            super("Wrought in War", 7, 5);
        }
    }

    public static class WroughtInWarP extends _WroughtInWarT {
        public WroughtInWarP() {
            super("Wrought in War+", 9, 7);
        }
    }

    // **************************************************************************************************
    // ********************************************* Uncommon *********************************************
    // **************************************************************************************************

    private static abstract class _AlignmentT extends Card {
        private final int energyGain;

        public _AlignmentT(String cardName, int energyGain) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.energyGain = energyGain;
            this.starCost = 2;
            entityProperty.hasStarCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(energyGain);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Alignment extends _AlignmentT {
        public Alignment() {
            super("Alignment", 2);
        }
    }

    public static class AlignmentP extends _AlignmentT {
        public AlignmentP() {
            super("Alignment+", 3);
        }
    }

    private static abstract class _BlackHoleT extends Card {
        private final int dmg;

        public _BlackHoleT(String cardName, int dmg) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.dmg = dmg;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += dmg;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("BlackHole", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnStarChangeHandler("BlackHole", new OnStarChangeHandler() {
                @Override public void handle(GameState state, int amount) {
                    int totalDmg = state.getCounterForRead()[counterIdx];
                    if (totalDmg > 0) {
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            state.playerDoDamageToEnemy(enemy, totalDmg);
                        }
                    }
                }
            });
        }
    }

    public static class BlackHole extends _BlackHoleT {
        public BlackHole() {
            super("Black Hole", 3);
        }
    }

    public static class BlackHoleP extends _BlackHoleT {
        public BlackHoleP() {
            super("Black Hole+", 4);
        }
    }

    private static abstract class _BulwarkT extends Card {
        private final int block;
        private final int forgeAmount;

        public _BulwarkT(String cardName, int block, int forgeAmount) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
            this.block = block;
            this.forgeAmount = forgeAmount;
            entityProperty.canForge = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.forge(forgeAmount);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Bulwark extends _BulwarkT {
        public Bulwark() {
            super("Bulwark", 13, 10);
        }
    }

    public static class BulwarkP extends _BulwarkT {
        public BulwarkP() {
            super("Bulwark+", 16, 13);
        }
    }

    private static abstract class _ChargeT extends Card {
        public _ChargeT(String cardName) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            entityProperty.selectFromDeck = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var deck = state.getDeckArrForRead();
            for (int i = 0; i < state.getNumCardsInDeck(); i++) {
                if (deck[i] == idx) {
                    state.transformCard(state.getDeckArrForWrite(), i, generatedCardIdx);
                    break;
                }
            }
            state.getCounterForWrite()[counterIdx]++;
            if (state.getCounterForWrite()[counterIdx] >= 2) {
                state.getCounterForWrite()[counterIdx] = 0;
                return GameActionCtx.PLAY_CARD;
            }
            return GameActionCtx.SELECT_CARD_DECK;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Charge", this, new GameProperties.NetworkInputHandler() {
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

    public static class Charge extends _ChargeT {
        public Charge() {
            super("CHARGE!!");
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.MinionStrike());
        }
    }

    public static class ChargeP extends _ChargeT {
        public ChargeP() {
            super("CHARGE!!+");
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.MinionStrikeP());
        }
    }

    private static abstract class _ChildOfTheStarsT extends Card {
        private final int blockPerStar;

        public _ChildOfTheStarsT(String cardName, int blockPerStar) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.blockPerStar = blockPerStar;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += blockPerStar;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("ChildOfTheStars", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnStarChangeHandler("ChildOfTheStars", new OnStarChangeHandler() {
                @Override public void handle(GameState state, int amount) {
                    int blockPerStarAmt = state.getCounterForRead()[counterIdx];
                    if (blockPerStarAmt > 0 && amount < 0) {
                        state.playerGainBlock(blockPerStarAmt * (-amount));
                    }
                }
            });
        }
    }

    public static class ChildOfTheStars extends _ChildOfTheStarsT {
        public ChildOfTheStars() {
            super("Child of the Stars", 2);
        }
    }

    public static class ChildOfTheStarsP extends _ChildOfTheStarsT {
        public ChildOfTheStarsP() {
            super("Child of the Stars+", 3);
        }
    }

    private static abstract class _ConquerorT extends Card {
        private final int forgeAmount;

        public _ConquerorT(String cardName, int forgeAmount) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.forgeAmount = forgeAmount;
            entityProperty.canForge = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.forge(forgeAmount);
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("ConquerorMultiplier", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int cIdx) {
                    state.properties.conquerorCounterIdx = cIdx;
                }
            });
            state.properties.addEndOfTurnHandler("ConquerorMultiplier", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    public static class Conqueror extends _ConquerorT {
        public Conqueror() {
            super("Conqueror", 3);
        }
    }

    public static class ConquerorP extends _ConquerorT {
        public ConquerorP() {
            super("Conqueror+", 5);
        }
    }

    private static abstract class _ConvergenceT extends Card {
        private final int starAmount;
        private final GameProperties.LocalCounterRegistrant energyRegistrant = new GameProperties.LocalCounterRegistrant();
        private final GameProperties.LocalCounterRegistrant starRegistrant = new GameProperties.LocalCounterRegistrant();

        public _ConvergenceT(String cardName, int starAmount) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.starAmount = starAmount;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[energyRegistrant.getCounterIdx(state.properties)] += 1;
            state.getCounterForWrite()[starRegistrant.getCounterIdx(state.properties)] += starAmount;
            state.getCounterForWrite()[counterIdx] += 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerEnergyNextTurnCounter(state, energyRegistrant);
            state.properties.registerStarNextTurnCounter(starRegistrant);
            state.properties.registerRetainHandCounter(this);
        }
    }

    public static class Convergence extends _ConvergenceT {
        public Convergence() {
            super("Convergence", 1);
        }
    }

    public static class ConvergenceP extends _ConvergenceT {
        public ConvergenceP() {
            super("Convergence+", 2);
        }
    }

    private static abstract class _DevastateT extends Card {
        private final int dmg;

        public _DevastateT(String cardName, int dmg) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.dmg = dmg;
            this.starCost = 4;
            entityProperty.hasStarCost = true;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Devastate extends _DevastateT {
        public Devastate() {
            super("Devastate", 30);
        }
    }

    public static class DevastateP extends _DevastateT {
        public DevastateP() {
            super("Devastate+", 40);
        }
    }

    private static abstract class _FurnaceT extends Card {
        private final int forgeAmount;

        public _FurnaceT(String cardName, int forgeAmount) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.forgeAmount = forgeAmount;
            entityProperty.canForge = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += forgeAmount;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Furnace", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Furnace", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.forge(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }
    }

    public static class Furnace extends _FurnaceT {
        public Furnace() {
            super("Furnace", 4);
        }
    }

    public static class FurnaceP extends _FurnaceT {
        public FurnaceP() {
            super("Furnace+", 6);
        }
    }

    private static abstract class _GammaBlastT extends Card {
        private final int dmg;

        public _GammaBlastT(String cardName, int dmg) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.dmg = dmg;
            this.starCost = 3;
            entityProperty.hasStarCost = true;
            entityProperty.selectEnemy = true;
            entityProperty.weakEnemy = true;
            entityProperty.vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, dmg);
            enemy.applyDebuff(state, DebuffType.WEAK, 2);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class GammaBlast extends _GammaBlastT {
        public GammaBlast() {
            super("Gamma Blast", 13);
        }
    }

    public static class GammaBlastP extends _GammaBlastT {
        public GammaBlastP() {
            super("Gamma Blast+", 18);
        }
    }

    private static abstract class _GlimmerT extends Card {
        private final int drawCount;

        public _GlimmerT(String cardName, int drawCount) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.drawCount = drawCount;
            entityProperty.selectFromHand = true;
            entityProperty.putCardOnTopDeck = true;
            selectFromHandLater = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.draw(drawCount);
                return state.getNumCardsInHand() > 0 ? GameActionCtx.SELECT_CARD_HAND : GameActionCtx.PLAY_CARD;
            } else {
                state.removeCardFromHand(idx);
                state.addCardOnTopOfDeck(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class Glimmer extends _GlimmerT {
        public Glimmer() {
            super("Glimmer", 3);
        }
    }

    public static class GlimmerP extends _GlimmerT {
        public GlimmerP() {
            super("Glimmer+", 4);
        }
    }

    private static abstract class _HegemonyT extends Card {
        private final int dmg;
        private final int energyAmount;

        public _HegemonyT(String cardName, int dmg, int energyAmount) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.dmg = dmg;
            this.energyAmount = energyAmount;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            state.getCounterForWrite()[counterIdx] += energyAmount;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerEnergyNextTurnCounter(state, this);
        }
    }

    public static class Hegemony extends _HegemonyT {
        public Hegemony() {
            super("Hegemony", 15, 2);
        }
    }

    public static class HegemonyP extends _HegemonyT {
        public HegemonyP() {
            super("Hegemony+", 18, 3);
        }
    }

    private static abstract class _KinglyKickT extends Card {
        private final int dmg;
        final int defaultCost;

        public _KinglyKickT(String cardName, int cost, int dmg) {
            super(cardName, Card.ATTACK, cost, Card.UNCOMMON);
            this.dmg = dmg;
            this.defaultCost = cost;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class KinglyKick extends _KinglyKickT {
        public KinglyKick() {
            this(4);
        }

        public KinglyKick(int cost) {
            super("Kingly Kick (" + cost + ")", cost, 24);
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = defaultCost - 1; i >= 0; i--) {
                c.add(new KinglyKick(i));
            }
            return c;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            if (state.properties.kinglyKickTransformIndexes == null) {
                state.properties.kinglyKickTransformIndexes = new int[state.properties.cardDict.length];
                Arrays.fill(state.properties.kinglyKickTransformIndexes, -1);
                for (int i = 0; i < state.properties.cardDict.length; i++) {
                    var base = state.properties.cardDict[i].getBaseCard();
                    if (base instanceof KinglyKick kk && kk.defaultCost > 0) {
                        state.properties.kinglyKickTransformIndexes[i] = state.properties.findCardIndex(new KinglyKick(kk.defaultCost - 1));
                    }
                }
            }
            state.properties.addOnCardDrawnHandler("KinglyKick", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int newCardIdx = state.properties.kinglyKickTransformIndexes[cardIdx];
                    if (newCardIdx >= 0) {
                        for (int i = state.handArrLen - 1; i >= 0; i--) {
                            if (state.handArr[i] == cardIdx) {
                                state.getHandArrForWrite()[i] = (short) newCardIdx;
                                break;
                            }
                        }
                    }
                }
            });
        }
    }

    public static class KinglyKickP extends _KinglyKickT {
        public KinglyKickP() {
            this(4);
        }

        public KinglyKickP(int cost) {
            super("Kingly Kick+ (" + cost + ")", cost, 30);
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = defaultCost - 1; i >= 0; i--) {
                c.add(new KinglyKickP(i));
            }
            return c;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            if (state.properties.kinglyKickPTransformIndexes == null) {
                state.properties.kinglyKickPTransformIndexes = new int[state.properties.cardDict.length];
                Arrays.fill(state.properties.kinglyKickPTransformIndexes, -1);
                for (int i = 0; i < state.properties.cardDict.length; i++) {
                    var base = state.properties.cardDict[i].getBaseCard();
                    if (base instanceof KinglyKickP kk && kk.defaultCost > 0) {
                        state.properties.kinglyKickPTransformIndexes[i] = state.properties.findCardIndex(new KinglyKickP(kk.defaultCost - 1));
                    }
                }
            }
            state.properties.addOnCardDrawnHandler("KinglyKickP", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int newCardIdx = state.properties.kinglyKickPTransformIndexes[cardIdx];
                    if (newCardIdx >= 0) {
                        for (int i = state.handArrLen - 1; i >= 0; i--) {
                            if (state.handArr[i] == cardIdx) {
                                state.getHandArrForWrite()[i] = (short) newCardIdx;
                                break;
                            }
                        }
                    }
                }
            });
        }
    }

    public static class KinglyPunch extends Card {
        final int dmg;

        public KinglyPunch() {
            this(8);
        }

        public KinglyPunch(int dmg) {
            super("Kingly Punch (" + dmg + ")", Card.ATTACK, 1, Card.UNCOMMON);
            this.dmg = dmg;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = dmg + 3; i <= properties.kinglyPunchLimit; i += 3) {
                c.add(new KinglyPunch(i));
            }
            return c;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            if (state.properties.kinglyPunchTransformIndexes == null) {
                state.properties.kinglyPunchTransformIndexes = new int[state.properties.cardDict.length];
                Arrays.fill(state.properties.kinglyPunchTransformIndexes, -1);
                for (int i = 0; i < state.properties.cardDict.length; i++) {
                    var base = state.properties.cardDict[i].getBaseCard();
                    if (base instanceof KinglyPunch kp && kp.dmg < state.properties.kinglyPunchLimit) {
                        state.properties.kinglyPunchTransformIndexes[i] = state.properties.findCardIndex(new KinglyPunch(kp.dmg + 3));
                    }
                }
            }
            state.properties.addOnCardDrawnHandler("KinglyPunch", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int newCardIdx = state.properties.kinglyPunchTransformIndexes[cardIdx];
                    if (newCardIdx >= 0) {
                        for (int i = state.handArrLen - 1; i >= 0; i--) {
                            if (state.handArr[i] == cardIdx) {
                                state.getHandArrForWrite()[i] = (short) newCardIdx;
                                break;
                            }
                        }
                    }
                }
            });
        }
    }

    public static class KinglyPunchP extends Card {
        final int dmg;

        public KinglyPunchP() {
            this(8);
        }

        public KinglyPunchP(int dmg) {
            super("Kingly Punch+ (" + dmg + ")", Card.ATTACK, 1, Card.UNCOMMON);
            this.dmg = dmg;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = dmg + 5; i <= properties.kinglyPPunchLimit; i += 5) {
                c.add(new KinglyPunchP(i));
            }
            return c;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            if (state.properties.kinglyPPunchTransformIndexes == null) {
                state.properties.kinglyPPunchTransformIndexes = new int[state.properties.cardDict.length];
                Arrays.fill(state.properties.kinglyPPunchTransformIndexes, -1);
                for (int i = 0; i < state.properties.cardDict.length; i++) {
                    var base = state.properties.cardDict[i].getBaseCard();
                    if (base instanceof KinglyPunchP kp && kp.dmg < state.properties.kinglyPPunchLimit) {
                        state.properties.kinglyPPunchTransformIndexes[i] = state.properties.findCardIndex(new KinglyPunchP(kp.dmg + 5));
                    }
                }
            }
            state.properties.addOnCardDrawnHandler("KinglyPunchP", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int newCardIdx = state.properties.kinglyPPunchTransformIndexes[cardIdx];
                    if (newCardIdx >= 0) {
                        for (int i = state.handArrLen - 1; i >= 0; i--) {
                            if (state.handArr[i] == cardIdx) {
                                state.getHandArrForWrite()[i] = (short) newCardIdx;
                                break;
                            }
                        }
                    }
                }
            });
        }
    }

    private static abstract class _KnockoutBlowT extends Card {
        private final int dmg;

        public _KnockoutBlowT(String cardName, int dmg) {
            super(cardName, Card.ATTACK, 3, Card.UNCOMMON);
            this.dmg = dmg;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, dmg);
            if (state.getEnemiesForRead().get(idx).getHealth() <= 0) {
                state.gainStar(5);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class KnockoutBlow extends _KnockoutBlowT {
        public KnockoutBlow() {
            super("Knockout Blow", 30);
        }
    }

    public static class KnockoutBlowP extends _KnockoutBlowT {
        public KnockoutBlowP() {
            super("Knockout Blow+", 38);
        }
    }

    // No need to implement Largesse: Multiplayer

    private static abstract class _LunarBlastT extends Card {
        private final int dmgPerSkill;

        public _LunarBlastT(String cardName, int dmgPerSkill) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.dmgPerSkill = dmgPerSkill;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int dmg = dmgPerSkill * state.getCounterForRead()[state.properties.skillsPlayedThisTurnCounterIdx];
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerSkillsPlayedThisTurnCounter();
        }
    }

    public static class LunarBlast extends _LunarBlastT {
        public LunarBlast() {
            super("Lunar Blast", 4);
        }
    }

    public static class LunarBlastP extends _LunarBlastT {
        public LunarBlastP() {
            super("Lunar Blast+", 5);
        }
    }

    private static abstract class _ManifestAuthorityT extends Card {
        private final int block;

        public _ManifestAuthorityT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            int randomIdx = state.getSearchRandomGen().nextInt(generatedCardIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, generatedCardIdxes));
            state.addCardToHand(state.createCard(generatedCardIdxes[randomIdx]));
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return CardManager.getColorlessCards(false);
        }
    }

    public static class ManifestAuthority extends _ManifestAuthorityT {
        public ManifestAuthority() {
            super("Manifest Authority", 7);
        }
    }

    public static class ManifestAuthorityP extends _ManifestAuthorityT {
        public ManifestAuthorityP() {
            super("Manifest Authority+", 8);
        }
    }

    private static abstract class _MonologueT extends Card {
        public _MonologueT(String cardName, boolean retain) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.retain = retain;
            entityProperty.changePlayerStrength = true;
            entityProperty.changePlayerStrengthEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Monologue", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("Monologue", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int n = state.getCounterForRead()[counterIdx];
                    if (n > 0) {
                        state.getPlayerForWrite().gainStrength(n);
                        state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, n);
                    }
                }
            });
            state.properties.addStartOfTurnHandler("Monologue", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    public static class Monologue extends _MonologueT {
        public Monologue() {
            super("Monologue", false);
        }
    }

    public static class MonologueP extends _MonologueT {
        public MonologueP() {
            super("Monologue+", true);
        }
    }

    private static abstract class _OrbitT extends Card {
        public _OrbitT(String cardName, int cost) {
            super(cardName, Card.POWER, cost, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Orbit", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 4.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnEnergySpendHandler("Orbit", new OnEnergySpendHandler() {
                @Override public void handle(GameState state, int energySpent) {
                    int carry = state.getCounterForRead()[counterIdx] + energySpent;
                    int gains = carry / 4;
                    if (gains > 0) {
                        state.gainEnergy(gains);
                    }
                    state.getCounterForWrite()[counterIdx] = carry % 4;
                }
            });
        }
    }

    public static class Orbit extends _OrbitT {
        public Orbit() {
            super("Orbit", 2);
        }
    }

    public static class OrbitP extends _OrbitT {
        public OrbitP() {
            super("Orbit+", 1);
        }
    }

    private static abstract class _PaleBlueDotT extends Card {
        private final int drawCount;
        private final GameProperties.LocalCounterRegistrant drawNextTurnRegistrant = new GameProperties.LocalCounterRegistrant();

        public _PaleBlueDotT(String cardName, int drawCount) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.drawCount = drawCount;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += drawCount;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("PaleBlueDot", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.registerDrawNextTurnCounter(drawNextTurnRegistrant);
            state.properties.registerCardsPlayedThisTurnCounter();
            state.properties.addEndOfTurnHandler("PaleBlueDot", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int draws = state.getCounterForRead()[counterIdx];
                    if (draws > 0 && state.getCounterForRead()[state.properties.cardsPlayedThisTurnCounterIdx] >= 5) {
                        state.getCounterForWrite()[drawNextTurnRegistrant.getCounterIdx(state.properties)] += draws;
                    }
                }
            });
        }
    }

    public static class PaleBlueDot extends _PaleBlueDotT {
        public PaleBlueDot() {
            super("Pale Blue Dot", 1);
        }
    }

    public static class PaleBlueDotP extends _PaleBlueDotT {
        public PaleBlueDotP() {
            super("Pale Blue Dot+", 2);
        }
    }

    private static abstract class _ParryT extends Card {
        private final int block;

        public _ParryT(String cardName, int block) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += block;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Parry", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("Parry", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int n = state.getCounterForRead()[counterIdx];
                    if (n > 0) {
                        var base = state.properties.cardDict[cardIdx].getBaseCard();
                        if (base instanceof CardColorless2.SovereignBlade || base instanceof CardColorless2.SovereignBladeP) {
                            state.playerGainBlock(n);
                        }
                    }
                }
            });
        }
    }

    public static class Parry extends _ParryT {
        public Parry() {
            super("Parry", 6);
        }
    }

    public static class ParryP extends _ParryT {
        public ParryP() {
            super("Parry+", 9);
        }
    }

    private static abstract class _ParticleWallT extends Card {
        private final int block;

        public _ParticleWallT(String cardName, int block) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.block = block;
            this.starCost = 2;
            entityProperty.hasStarCost = true;
            this.returnToHandWhenPlay = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ParticleWall extends _ParticleWallT {
        public ParticleWall() {
            super("Particle Wall", 9);
        }
    }

    public static class ParticleWallP extends _ParticleWallT {
        public ParticleWallP() {
            super("Particle Wall+", 12);
        }
    }

    private static abstract class _PillarOfCreationT extends Card {
        private final int block;

        public _PillarOfCreationT(String cardName, int block) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += block;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("PillarOfCreation", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardCreationHandler("PillarOfCreation", new OnCardCreationHandler() {
                @Override public void handle(GameState state, int cardIdx) {
                    int n = state.getCounterForRead()[counterIdx];
                    if (n > 0) {
                        state.playerGainBlock(n);
                    }
                }
            });
        }
    }

    public static class PillarOfCreation extends _PillarOfCreationT {
        public PillarOfCreation() {
            super("Pillar of Creation", 3);
        }
    }

    public static class PillarOfCreationP extends _PillarOfCreationT {
        public PillarOfCreationP() {
            super("Pillar of Creation+", 4);
        }
    }

    private static abstract class _ProphesizeT extends Card {
        private final int drawCount;

        public _ProphesizeT(String cardName, int drawCount) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
            this.drawCount = drawCount;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.draw(drawCount);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Prophesize extends _ProphesizeT {
        public Prophesize() {
            super("Prophesize", 6);
        }
    }

    public static class ProphesizeP extends _ProphesizeT {
        public ProphesizeP() {
            super("Prophesize+", 9);
        }
    }

    private static abstract class _QuasarT extends Card {
        private final boolean upgraded;

        public _QuasarT(String cardName, boolean upgraded) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.upgraded = upgraded;
            this.starCost = 2;
            this.exhaustWhenPlayed = true;
            entityProperty.hasStarCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.setSelect1OutOf3Idxes(generatedCardIdxes);
            return GameActionCtx.SELECT_CARD_1_OUT_OF_3;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties gameProperties, List<Card> cards) {
            return getPossibleSelect1OutOf3Cards(gameProperties);
        }

        @Override public List<Card> getPossibleSelect1OutOf3Cards(GameProperties gameProperties) {
            var colorlessCards = CardManager.getColorlessCardsTmp0Cost(false);
            return upgraded ? colorlessCards.stream().map(Card::getUpgrade).filter(java.util.Objects::nonNull).toList() : colorlessCards;
        }
    }

    public static class Quasar extends _QuasarT {
        public Quasar() {
            super("Quasar", false);
        }
    }

    public static class QuasarP extends _QuasarT {
        public QuasarP() {
            super("Quasar+", true);
        }
    }

    private static abstract class _RadiateT extends Card {
        private final int dmgPerStar;

        public _RadiateT(String cardName, int dmgPerStar) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.dmgPerStar = dmgPerStar;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int starGained = state.getCounterForRead()[counterIdx];
            if (starGained > 0) {
                int dmg = starGained * dmgPerStar;
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    state.playerDoDamageToEnemy(enemy, dmg);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("RadiateStarGained", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnStarChangeHandler("RadiateStarGained", new OnStarChangeHandler() {
                @Override public void handle(GameState state, int amount) {
                    if (amount > 0) {
                        state.getCounterForWrite()[counterIdx] += amount;
                    }
                }
            });
            state.properties.addStartOfTurnHandler("RadiateStarGained", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    public static class Radiate extends _RadiateT {
        public Radiate() {
            super("Radiate", 3);
        }
    }

    public static class RadiateP extends _RadiateT {
        public RadiateP() {
            super("Radiate+", 4);
        }
    }

    private static abstract class _ReflectT extends Card {
        private final int block;

        public _ReflectT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
            this.starCost = 3;
            entityProperty.hasStarCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Reflect", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }

                @Override public void onRegister(int cIdx) {
                    state.properties.reflectCounterIdx = cIdx;
                }
            });
            state.properties.addStartOfTurnHandler("Reflect", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx]--;
                    }
                }
            });
        }
    }

    public static class Reflect extends _ReflectT {
        public Reflect() {
            super("Reflect", 17);
        }
    }

    public static class ReflectP extends _ReflectT {
        public ReflectP() {
            super("Reflect+", 21);
        }
    }

    private static abstract class _ResonanceT extends Card {
        private final int playerStrength;

        public _ResonanceT(String cardName, int playerStrength) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.playerStrength = playerStrength;
            this.starCost = 3;
            entityProperty.hasStarCost = true;
            entityProperty.changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainStrength(playerStrength);
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.LOSE_STRENGTH, 1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Resonance extends _ResonanceT {
        public Resonance() {
            super("Resonance", 1);
        }
    }

    public static class ResonanceP extends _ResonanceT {
        public ResonanceP() {
            super("Resonance+", 2);
        }
    }

    private static abstract class _RoyalGambleT extends Card {
        public _RoyalGambleT(String cardName, boolean retain) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.starCost = 5;
            this.exhaustWhenPlayed = true;
            this.retain = retain;
            entityProperty.hasStarCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainStar(9);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class RoyalGamble extends _RoyalGambleT {
        public RoyalGamble() {
            super("Royal Gamble", false);
        }
    }

    public static class RoyalGambleP extends _RoyalGambleT {
        public RoyalGambleP() {
            super("Royal Gamble+", true);
        }
    }

    private static abstract class _ShiningStrikeT extends Card {
        private final int dmg;

        public _ShiningStrikeT(String cardName, int dmg) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.dmg = dmg;
            this.returnToTopOfDeckWhenPlay = true;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            state.gainStar(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ShiningStrike extends _ShiningStrikeT {
        public ShiningStrike() {
            super("Shining Strike", 8);
        }
    }

    public static class ShiningStrikeP extends _ShiningStrikeT {
        public ShiningStrikeP() {
            super("Shining Strike+", 11);
        }
    }

    public static class SpectrumShift extends CardColorless._MagnetismT {
        public SpectrumShift() {
            super("Spectrum Shift", 2, Card.UNCOMMON);
        }
    }

    public static class SpectrumShiftP extends CardColorless._MagnetismT {
        public SpectrumShiftP() {
            super("Spectrum Shift+", 1, Card.UNCOMMON);
        }
    }

    private static abstract class _StardustT extends Card {
        private final int dmg;

        public _StardustT(String cardName, int dmg) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.dmg = dmg;
            entityProperty.hasStarCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int x = state.starResource;
            if (x > 0) {
                state.starResource = 0;
                for (int i = 0; i < state.properties.onStarChangeHandlers.size(); i++) {
                    state.properties.onStarChangeHandlers.get(i).handle(state, -x);
                }
                for (int i = 0; i < x; i++) {
                    int enemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
                    if (enemyIdx < 0) break;
                    state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(enemyIdx), dmg);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Stardust extends _StardustT {
        public Stardust() {
            super("Stardust", 5);
        }
    }

    public static class StardustP extends _StardustT {
        public StardustP() {
            super("Stardust+", 7);
        }
    }

    private static abstract class _SummonForthT extends Card {
        private final int forgeAmount;

        public _SummonForthT(String cardName, int forgeAmount) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.forgeAmount = forgeAmount;
            entityProperty.canForge = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.forge(forgeAmount);
            for (int i = state.deckArrLen - 1; i >= 0; i--) {
                var base = state.properties.cardDict[state.getDeckArrForRead()[i]].getBaseCard();
                if (base instanceof CardColorless2.SovereignBlade || base instanceof CardColorless2.SovereignBladeP) {
                    int foundIdx = state.getDeckArrForRead()[i];
                    state.removeCardFromDeck(foundIdx, false);
                    state.addCardToHand(foundIdx);
                }
            }
            for (int i = state.discardArrLen - 1; i >= 0; i--) {
                var base = state.properties.cardDict[state.getDiscardArrForRead()[i]].getBaseCard();
                if (base instanceof CardColorless2.SovereignBlade || base instanceof CardColorless2.SovereignBladeP) {
                    int foundIdx = state.getDiscardArrForRead()[i];
                    state.removeCardFromDiscard(foundIdx);
                    state.addCardToHand(foundIdx);
                }
            }
            for (int i = state.exhaustArrLen - 1; i >= 0; i--) {
                var base = state.properties.cardDict[state.getExhaustArrForRead()[i]].getBaseCard();
                if (base instanceof CardColorless2.SovereignBlade || base instanceof CardColorless2.SovereignBladeP) {
                    int foundIdx = state.getExhaustArrForRead()[i];
                    state.removeCardFromExhaust(foundIdx);
                    state.addCardToHand(foundIdx);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SummonForth extends _SummonForthT {
        public SummonForth() {
            super("Summon Forth", 8);
        }
    }

    public static class SummonForthP extends _SummonForthT {
        public SummonForthP() {
            super("Summon Forth+", 11);
        }
    }

    private static abstract class _SupermassiveT extends Card {
        private final int baseDmg;
        private final int dmgPerCard;

        public _SupermassiveT(String cardName, int baseDmg, int dmgPerCard) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.baseDmg = baseDmg;
            this.dmgPerCard = dmgPerCard;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int cardsCreated = state.getCounterForRead()[counterIdx];
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), baseDmg + cardsCreated * dmgPerCard);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("SupermassiveCreatedCards", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardCreationHandler("SupermassiveCreatedCards", new OnCardCreationHandler() {
                @Override public void handle(GameState state, int cardIdx) {
                    state.getCounterForWrite()[counterIdx]++;
                }
            });
        }
    }

    public static class Supermassive extends _SupermassiveT {
        public Supermassive() {
            super("Supermassive", 5, 3);
        }
    }

    public static class SupermassiveP extends _SupermassiveT {
        public SupermassiveP() {
            super("Supermassive+", 5, 4);
        }
    }

    private static abstract class _TerraformingT extends Card {
        private final int vigor;

        public _TerraformingT(String cardName, int vigor) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.vigor = vigor;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[state.properties.vigorCounterIdx] += vigor;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerVigorCounter(this);
        }
    }

    public static class Terraforming extends _TerraformingT {
        public Terraforming() {
            super("Terraforming", 6);
        }
    }

    public static class TerraformingP extends _TerraformingT {
        public TerraformingP() {
            super("Terraforming+", 8);
        }
    }

    // **************************************************************************************************
    // *********************************************  Rare  *********************************************
    // **************************************************************************************************

    // TODO: Arsenal (Rare) - 1 energy, Power
    //   Effect: Whenever you play a Colorless card, gain 1 Strength.
    //   Upgraded Effect: Whenever you play a Colorless card, gain 2 Strength.

    // TODO: Beat into Shape (Rare) - 1 energy, Attack
    //   Effect: Deal 5 damage. Forge 5. Forges an additional 5 for every other time you've hit the enemy this turn.
    //   Upgraded Effect: Deal 7 damage. Forge 7. Forges an additional 7 for every other time you've hit the enemy this turn.

    // TODO: Big Bang (Rare) - 0 energy, Skill
    //   Effect: Draw 1 card. Gain energy. Gain star. Forge 5. Exhaust.
    //   Upgraded Effect: Innate. Draw 1 card. Gain energy. Gain star. Forge 5. Exhaust.

    // TODO: Bombardment (Rare) - 3 energy, Attack
    //   Effect: Deal 18 damage. At the start of your turn, plays from the Exhaust Pile. Exhaust.
    //   Upgraded Effect: Deal 24 damage. At the start of your turn, plays from the Exhaust Pile. Exhaust.

    // TODO: Bundle of Joy (Rare) - 2 energy, Skill
    //   Effect: Add 3 random Colorless cards into your Hand. Exhaust.
    //   Upgraded Effect: Add 4 random Colorless cards into your Hand. Exhaust.

    // TODO: Comet (Rare) - 0 energy, 5 star, Attack
    //   Effect: Deal 33 damage. Apply 3 Weak. Apply 3 Vulnerable.
    //   Upgraded Effect: Deal 44 damage. Apply 3 Weak. Apply 3 Vulnerable.

    // TODO: Crash Landing (Rare) - 1 energy, Attack
    //   Effect: Deal 21 damage to ALL enemies. Fill your Hand with Debris.
    //   Upgraded Effect: Deal 26 damage to ALL enemies. Fill your Hand with Debris.

    // TODO: Decisions, Decisions (Rare) - 0 energy, 6 star, Skill
    //   Effect: Draw 3 cards. Choose a Skill in your Hand and play it 3 times. Exhaust.
    //   Upgraded Effect: Draw 5 cards. Choose a Skill in your Hand and play it 3 times. Exhaust.

    // TODO: Dying Star (Rare) - 1 energy, 3 star, Attack
    //   Effect: Ethereal. Deal 9 damage to ALL enemies. ALL enemies lose 9 Strength this turn.
    //   Upgraded Effect: Ethereal. Deal 11 damage to ALL enemies. ALL enemies lose 11 Strength this turn.

    // TODO: Foregone Conclusion (Rare) - 1 energy, Skill
    //   Effect: Next turn, put 2 cards from your Draw Pile into your Hand.
    //   Upgraded Effect: Next turn, put 3 cards from your Draw Pile into your Hand.

    // TODO: GUARDS!!! (Rare) - 2 energy, Skill
    //   Effect: Transform any number of cards in your Hand into Minion Sacrifice. Exhaust.
    //   Upgraded Effect: Transform any number of cards in your Hand into Minion Sacrifice+. Exhaust.

    // TODO: Genesis (Rare) - 2 energy, Power
    //   Effect: At the start of your turn, gain 2 star.
    //   Upgraded Effect: At the start of your turn, gain 3 star.

    // TODO: Hammer Time (Rare) - 2 energy, Power
    //   Effect: Whenever you Forge, all allies Forge as well.
    //   Upgraded Effect (1 energy): Whenever you Forge, all allies Forge as well.

    // TODO: Heavenly Drill (Rare) - X energy, Attack
    //   Effect: Deal 8 damage X times. Double X if it's 4 or more.
    //   Upgraded Effect: Deal 10 damage X times. Double X if it's 4 or more.

    // TODO: Heirloom Hammer (Rare) - 2 energy, Attack
    //   Effect: Deal 17 damage. Choose a Colorless card in your Hand. Add a copy of that card into your Hand.
    //   Upgraded Effect: Deal 22 damage. Choose a Colorless card in your Hand. Add a copy of that card into your Hand.

    // TODO: I Am Invincible (Rare) - 1 energy, Skill
    //   Effect: Gain 9 Block. At the end of your turn, if this is on top of your Draw Pile, play it.
    //   Upgraded Effect: Gain 12 Block. At the end of your turn, if this is on top of your Draw Pile, play it.

    // TODO: Make It So (Rare) - 0 energy, Attack
    //   Effect: Deal 6 damage. Every 3 Skills you play in a turn, put this into your Hand.
    //   Upgraded Effect: Deal 9 damage. Every 3 Skills you play in a turn, put this into your Hand.

    // TODO: Monarch's Gaze (Rare) - 3 energy, Power
    //   Effect: Whenever you attack an enemy, it loses 1 Strength this turn.
    //   Upgraded Effect (2 energy): Whenever you attack an enemy, it loses 1 Strength this turn.

    // TODO: Neutron Aegis (Rare) - 1 energy, 5 star, Power
    //   Effect: Gain 8 Plating.
    //   Upgraded Effect: Gain 11 Plating.

    // TODO: Royalties (Rare) - 1 energy, Power
    //   Effect: At the end of combat, gain 30 Gold.
    //   Upgraded Effect: At the end of combat, gain 35 Gold.

    // TODO: Seeking Edge (Rare) - 1 energy, Power
    //   Effect: Forge 7. Sovereign Blade now deals damage to ALL enemies.
    //   Upgraded Effect: Forge 11. Sovereign Blade now deals damage to ALL enemies.

    // TODO: Seven Stars (Rare) - 2 energy, 7 star, Attack
    //   Effect: Deal 7 damage to ALL enemies 7 times.
    //   Upgraded Effect (1 energy): Deal 7 damage to ALL enemies 7 times.

    // TODO: Sword Sage (Rare) - 2 energy, Power
    //   Effect: Increase the cost of Sovereign Blade by 1. Sovereign Blade now hits an additional time.
    //   Upgraded Effect (1 energy): Increase the cost of Sovereign Blade by 1. Sovereign Blade now hits an additional time.

    // TODO: The Smith (Rare) - 1 energy, 4 star, Skill
    //   Effect: Forge 30.
    //   Upgraded Effect: Forge 40.

    // TODO: Tyranny (Rare) - 1 energy, Power
    //   Effect: At the start of your turn, draw 1 card and Exhaust 1 card from your Hand.
    //   Upgraded Effect: Innate. At the start of your turn, draw 1 card and Exhaust 1 card from your Hand.

    // TODO: Void Form (Rare) - 3 energy, Power
    //   Effect: End your turn. The first 2 cards you play each turn are free to play.
    //   Upgraded Effect: End your turn. The first 3 cards you play each turn are free to play.

    // **************************************************************************************************
    // ********************************************* Ancient *********************************************
    // **************************************************************************************************

    // TODO: Meteor Shower (Ancient) - 0 energy, 2 star, Attack
    //   Effect: Deal 14 damage to ALL enemies. Apply 2 Weak and Vulnerable to ALL enemies.
    //   Upgraded Effect: Deal 21 damage to ALL enemies. Apply 2 Weak and Vulnerable to ALL enemies.

    // TODO: The Sealed Throne (Ancient) - 1 energy, 3 star, Power
    //   Effect: Whenever you play a card, gain star.
    //   Upgraded Effect: Innate. Whenever you play a card, gain star.
}
