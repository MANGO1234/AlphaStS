package com.alphaStS;

import com.alphaStS.enemy.Enemy;
import java.util.List;

public class CardSilent {
    public static class Neutralize extends Card {
        public Neutralize() {
            super("Neutralize", Card.ATTACK, 0, Card.COMMON);
            this.weakEnemy = true;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 3);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, 1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class NeutralizeP extends Card {
        public NeutralizeP() {
            super("Neutralize+", Card.ATTACK, 0, Card.COMMON);
            this.weakEnemy = true;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 4);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _SurvivorT extends Card {
        private final int n;

        public _SurvivorT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.selectFromHand = true;
            this.selectFromHandLater = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.getPlayerForWrite().gainBlock(n);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.removeCardFromHand(idx);
                state.addCardToDiscard(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class Survivor extends _SurvivorT {
        public Survivor() {
            super("Survivor", Card.SKILL, 1, 8);
        }
    }

    public static class SurvivorP extends _SurvivorT {
        public SurvivorP() {
            super("Survivor+", Card.SKILL, 1, 11);
        }
    }

    private static abstract class _AcrobaticsT extends Card {
        private final int n;

        public _AcrobaticsT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.selectFromHand = true;
            this.selectFromHandLater = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.draw(n);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.removeCardFromHand(idx);
                state.addCardToDiscard(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class Acrobatics extends _AcrobaticsT {
        public Acrobatics() {
            super("Acrobatics", Card.SKILL, 1, 3);
        }
    }

    public static class AcrobaticsP extends _AcrobaticsT {
        public AcrobaticsP() {
            super("Acrobatics+", Card.SKILL, 1, 4);
        }
    }

    private static abstract class _BackflipT extends Card {
        private final int n;

        public _BackflipT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            state.draw(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Backflip extends _BackflipT {
        public Backflip() {
            super("Backflip", Card.SKILL, 1, 5);
        }
    }

    public static class BackflipP extends _BackflipT {
        public BackflipP() {
            super("Backflip+", Card.SKILL, 1, 8);
        }
    }

    // Bane

    private static abstract class _BladeDanceT extends Card {
        private final int n;

        public _BladeDanceT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < n; i++) {
                state.addCardToHand(state.prop.shivCardIdx);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new CardColorless.Shiv());
        }
    }

    public static class BladeDance extends _BladeDanceT {
        public BladeDance() {
            super("Blade Dance", Card.SKILL, 1, 3);
        }
    }

    public static class BladeDanceP extends _BladeDanceT {
        public BladeDanceP() {
            super("Blade Dance+", Card.SKILL, 1, 4);
        }
    }

    private static abstract class _CloakAndDaggerT extends Card {
        private final int n;

        public _CloakAndDaggerT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(6);
            for (int i = 0; i < n; i++) {
                state.addCardToHand(state.prop.shivCardIdx);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new CardColorless.Shiv());
        }
    }

    public static class CloakAndDagger extends _CloakAndDaggerT {
        public CloakAndDagger() {
            super("Cloak And Dagger", Card.SKILL, 1, 1);
        }
    }

    public static class CloakAndDaggerP extends _CloakAndDaggerT {
        public CloakAndDaggerP() {
            super("Cloak And Dagger+", Card.SKILL, 1, 2);
        }
    }

    private static abstract class _DaggerSprayT extends Card {
        private final int n;

        public _DaggerSprayT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < 2; i++) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    state.playerDoDamageToEnemy(enemy, n);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DaggerSpray extends _DaggerSprayT {
        public DaggerSpray() {
            super("Dagger Spray", Card.ATTACK, 1, 4);
        }
    }

    public static class DaggerSprayP extends _DaggerSprayT {
        public DaggerSprayP() {
            super("Dagger Spray+", Card.ATTACK, 1, 6);
        }
    }

    // Dagger Throw

    private static abstract class _DeadlyPoisonT extends Card {
        private final int n;

        public _DeadlyPoisonT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.selectEnemy = true;
            this.poisonEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.POISON, n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DeadlyPoison extends _DeadlyPoisonT {
        public DeadlyPoison() {
            super("Deadly Poison", Card.SKILL, 1, 5);
        }
    }

    public static class DeadlyPoisonP extends _DeadlyPoisonT {
        public DeadlyPoisonP() {
            super("Deadly Poison+", Card.SKILL, 1, 7);
        }
    }

    private static abstract class _DeflectT extends Card {
        private final int n;

        public _DeflectT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Deflect extends _DeflectT {
        public Deflect() {
            super("Deflect", Card.SKILL, 0, 4);
        }
    }

    public static class DeflectP extends _DeflectT {
        public DeflectP() {
            super("Deflect+", Card.SKILL, 0, 7);
        }
    }

    // Dodge and Roll
    // Flying Knee
    // Outmaneuver

    private static abstract class _PiercingWailT extends Card {
        private final int n;

        public _PiercingWailT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PiercingWail extends _PiercingWailT {
        public PiercingWail() {
            super("Piercing Wail", Card.SKILL, 1, 6);
        }
    }

    public static class PiercingWailP extends _PiercingWailT {
        public PiercingWailP() {
            super("Piercing Wail+", Card.SKILL, 1, 8);
        }
    }

    private static abstract class _PoisonedStabT extends Card {
        private final int n;

        public _PoisonedStabT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.selectEnemy = true;
            this.poisonEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, n);
            enemy.applyDebuff(state, DebuffType.POISON, n / 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PoisonedStab extends _PoisonedStabT {
        public PoisonedStab() {
            super("PoisonedStab", Card.ATTACK, 1, 6);
        }
    }

    public static class PoisonedStabP extends _PoisonedStabT {
        public PoisonedStabP() {
            super("PoisonedStab+", Card.ATTACK, 1, 8);
        }
    }

    // Prepared

    private static abstract class _QuickSlashT extends Card {
        private final int n;

        public _QuickSlashT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class QuickSlash extends _QuickSlashT {
        public QuickSlash() {
            super("Quick Slash", Card.SKILL, 1, 8);
        }
    }

    public static class QuickSlashP extends _QuickSlashT {
        public QuickSlashP() {
            super("Quick Slash+", Card.SKILL, 1, 12);
        }
    }

    private static abstract class _SliceT extends Card {
        private final int n;

        public _SliceT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Slice extends _SliceT {
        public Slice() {
            super("Slice", Card.SKILL, 0, 6);
        }
    }

    public static class SliceP extends _SliceT {
        public SliceP() {
            super("Slice+", Card.SKILL, 0, 9);
        }
    }

    // Sneaky Strike

    private static abstract class _SuckerPunchT extends Card {
        private final int n;
        private final int weak;

        public _SuckerPunchT(String cardName, int cardType, int energyCost, int n, int weak) {
            super(cardName, cardType, energyCost, Card.COMMON);
            this.n = n;
            this.weak = weak;
            this.weakEnemy = true;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, weak);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SuckerPunch extends _SuckerPunchT {
        public SuckerPunch() {
            super("Sucker Punch", Card.ATTACK, 1, 7, 1);
        }
    }

    public static class SuckerPunchP extends _SuckerPunchT {
        public SuckerPunchP() {
            super("Sucker Punch+", Card.ATTACK, 1, 9, 2);
        }
    }

    // Accuracy
    // All-Out Attack

    private static abstract class _BackstabT extends Card {
        private final int n;

        public _BackstabT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.innate = true;
            this.exhaustWhenPlayed = true;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Backstab extends _BackstabT {
        public Backstab() {
            super("Backstab", Card.ATTACK, 0, 11);
        }
    }

    public static class BackstabP extends _BackstabT {
        public BackstabP() {
            super("Backstab+", Card.ATTACK, 0, 15);
        }
    }

    // Blur
    // Bouncing Flask
    // Calculated Gamble
    // Caltrops
    // Catalyst
    // Choke
    // Concentrate
    // Crippling Cloud

    private static abstract class _DashT extends Card {
        private final int n;

        public _DashT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            state.getPlayerForWrite().gainBlock(13);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Dash extends _DashT {
        public Dash() {
            super("Dash", Card.ATTACK, 2, 10);
        }
    }

    public static class DashP extends _DashT {
        public DashP() {
            super("Dash+", Card.ATTACK, 2, 13);
        }
    }

    // Distraction
    // Endless Agony
    // Escape Plan
    // Eviscerate

    private static abstract class _ExpertiseT extends Card {
        private final int n;

        public _ExpertiseT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int toDraw = n - state.getNumCardsInHand();
            if (toDraw > 0) {
                state.draw(toDraw);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Expertise extends _ExpertiseT {
        public Expertise() {
            super("Expertise", Card.SKILL, 1, 6);
        }
    }

    public static class ExpertiseP extends _ExpertiseT {
        public ExpertiseP() {
            super("Expertise+", Card.SKILL, 1, 7);
        }
    }

    // Finisher

    private static abstract class _FlechetteT extends Card {
        private final int n;

        public _FlechetteT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < state.getHand().length; i++) {
                if (state.prop.cardDict[i].cardType == Card.SKILL) {
                    state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Flechette extends _FlechetteT {
        public Flechette() {
            super("Flechette", Card.ATTACK, 1, 4);
        }
    }

    public static class FlechetteP extends _FlechetteT {
        public FlechetteP() {
            super("Flechette+", Card.ATTACK, 1, 6);
        }
    }

    private static abstract class _FootworkT extends Card {
        private final int n;

        public _FootworkT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainDexterity(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Footwork extends _FootworkT {
        public Footwork() {
            super("Footwork", Card.POWER, 1, 2);
        }
    }

    public static class FootworkP extends _FootworkT {
        public FootworkP() {
            super("Footwork+", Card.POWER, 1, 3);
        }
    }

    private static abstract class _HeelHookT extends Card {
        private final int n;

        public _HeelHookT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, n);
            if (enemy.getWeak() > 0) {
                state.gainEnergy(1);
                state.draw(1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class HeelHook extends _HeelHookT {
        public HeelHook() {
            super("HeelHook", Card.ATTACK, 1, 5);
        }
    }

    public static class HeelHookP extends _HeelHookT {
        public HeelHookP() {
            super("HeelHook+", Card.ATTACK, 1, 8);
        }
    }

    private static abstract class _InfiniteBladeT extends Card {
        public _InfiniteBladeT(String cardName, int cardType, int energyCost, boolean innate) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.innate = innate;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new CardColorless.Shiv());
        }

        @Override public void startOfGameSetup(GameState state) {
            state.addStartOfTurnHandler("InfiniteBlade", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    for (int i = 0; i < state.getCounterForRead()[counterIdx]; i++) {
                        state.addCardToHand(state.prop.shivCardIdx);
                    }
                }
            });
        }
    }

    public static class InfiniteBlade extends _InfiniteBladeT {
        public InfiniteBlade() {
            super("InfiniteBlade", Card.POWER, 1, false);
        }
    }

    public static class InfiniteBladeP extends _InfiniteBladeT {
        public InfiniteBladeP() {
            super("InfiniteBlade+", Card.POWER, 1, true);
        }
    }

    private static abstract class _LegSweepT extends Card {
        private final int n;
        private final int weak;

        public _LegSweepT(String cardName, int cardType, int energyCost, int n, int weak) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.weak = weak;
            this.weakEnemy = true;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(n);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, weak);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class LegSweep extends _LegSweepT {
        public LegSweep() {
            super("Leg Sweep", Card.SKILL, 2, 11, 2);
        }
    }

    public static class LegSweepP extends _LegSweepT {
        public LegSweepP() {
            super("Leg Sweep+", Card.SKILL, 2, 14, 3);
        }
    }

    // Masterful Stab
    // Noxious Fumes
    // Predator
    // Reflex

    private static abstract class _RiddleWithHolesT extends Card {
        private final int n;

        public _RiddleWithHolesT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < 5; i++) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class RiddleWithHoles extends _RiddleWithHolesT {
        public RiddleWithHoles() {
            super("Riddle With Holes", Card.ATTACK, 2, 3);
        }
    }

    public static class RiddleWithHolesP extends _RiddleWithHolesT {
        public RiddleWithHolesP() {
            super("Riddle With Holes+", Card.ATTACK, 2, 4);
        }
    }

    // Setup

    private static abstract class _SkewerT extends Card {
        private final int n;

        public _SkewerT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.n = n;
            this.selectEnemy = true;
            this.isXCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < energyUsed; i++) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            return state.energy;
        }
    }

    public static class Skewer extends _SkewerT {
        public Skewer() {
            super("Skewer", Card.ATTACK, -1, 7);
        }
    }

    public static class SkewerP extends _SkewerT {
        public SkewerP() {
            super("Skewer+", Card.ATTACK, -1, 10);
        }
    }

    // Tactician

    private static abstract class _TerrorT extends Card {
        public _TerrorT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            this.vulnEnemy = true;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.VULNERABLE, 99);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Terror extends _TerrorT {
        public Terror() {
            super("Terror", Card.SKILL, 1);
        }
    }

    public static class TerrorP extends _TerrorT {
        public TerrorP() {
            super("Terror+", Card.SKILL, 0);
        }
    }

    // Well-Laid Plans

    private static abstract class _AThousandCutsT extends Card {
        private final int n;

        public _AThousandCutsT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        public void startOfGameSetup(GameState state) {
            state.addOnCardPlayedHandler("AThousandCuts", new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card, int lastIdx, boolean cloned) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            state.playerDoNonAttackDamageToEnemy(enemy, state.getCounterForRead()[counterIdx], true);
                        }
                    }
                }
            });
        }
    }

    public static class AThousandCuts extends _AThousandCutsT {
        public AThousandCuts() {
            super("A Thousand Cuts", Card.POWER, 2, 1);
        }
    }

    public static class AThousandCutsP extends _AThousandCutsT {
        public AThousandCutsP() {
            super("A Thousand Cuts+", Card.POWER, 2, 2);
        }
    }

    private static abstract class _AdrenalineT extends Card {
        private final int n;

        public _AdrenalineT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(n);
            state.draw(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Adrenaline extends _AdrenalineT {
        public Adrenaline() {
            super("Adrenaline", Card.SKILL, 0, 1);
        }
    }

    public static class AdrenalineP extends _AdrenalineT {
        public AdrenalineP() {
            super("Adrenaline+", Card.SKILL, 0, 2);
        }
    }

    private static abstract class _AfterImageT extends Card {
        private final boolean innate;

        public _AfterImageT(String cardName, int cardType, int energyCost, boolean innate) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.innate = innate;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        public void startOfGameSetup(GameState state) {
            state.addOnCardPlayedHandler("AfterImage", new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card, int lastIdx, boolean cloned) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getPlayerForWrite().gainBlockNotFromCardPlay(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }
    }

    public static class AfterImage extends _AfterImageT {
        public AfterImage() {
            super("After Image", Card.POWER, 1, false);
        }
    }

    public static class AfterImageP extends _AfterImageT {
        public AfterImageP() {
            super("After Image+", Card.POWER, 1, true);
        }
    }

    // Alchemize
    // Bullet Time
    // Burst
    // Corpse Explosion

    private static abstract class _DieDieDieT extends Card {
        private final int n;

        public _DieDieDieT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, n);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DieDieDie extends _DieDieDieT {
        public DieDieDie() {
            super("Die Die Die", Card.ATTACK, 1, 13);
        }
    }

    public static class DieDieDieP extends _DieDieDieT {
        public DieDieDieP() {
            super("Die Die Die+", Card.ATTACK, 1, 17);
        }
    }

    // Doppelganger
    // Envenom
    // Glass Knife

    private static abstract class _GrandFinaleT extends Card {
        private final int n;

        public _GrandFinaleT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < state.hand.length; i++) {
                if (state.hand[i] > 0) {
                    return GameActionCtx.PLAY_CARD;
                }
            }
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, n);
            }
            return GameActionCtx.PLAY_CARD;
        }


        public int energyCost(GameState state) {
            for (int i = 0; i < state.hand.length; i++) {
                if (state.hand[i] > 0) {
                    return -1;
                }
            }
            return 0;
        }
    }

    public static class GrandFinale extends _GrandFinaleT {
        public GrandFinale() {
            super("Grand Finale", Card.ATTACK, 0, 50);
        }
    }

    public static class GrandFinaleP extends _GrandFinaleT {
        public GrandFinaleP() {
            super("Grand Finale+", Card.ATTACK, 0, 60);
        }
    }

    private static abstract class _MalaiseT extends Card {
        private final boolean upgraded;

        public _MalaiseT(String cardName, int cardType, int energyCost, boolean upgraded) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.upgraded = upgraded;
            this.isXCost = true;
            this.exhaustWhenPlayed = true;
            this.weakEnemy = true;
            this.affectEnemyStrength = true;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, energyUsed + (upgraded ? 1 : 0));
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.LOSE_STRENGTH, energyUsed + (upgraded ? 1 : 0));
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            return state.energy;
        }
    }

    public static class Malaise extends _MalaiseT {
        public Malaise() {
            super("Malaise", Card.SKILL, -1, false);
        }
    }

    public static class MalaiseP extends _MalaiseT {
        public MalaiseP() {
            super("Malaise+", Card.SKILL, -1, true);
        }
    }

    // Nightmare
    // Phantasmal Killer
    // Storm of Steel
    // Tools of the Trade
    // Unload

    private static abstract class _WraithFormT extends Card {
        private final int n;

        public _WraithFormT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[state.prop.intangibleCounterIdx] += n;
            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_DEXTERITY_PER_TURN, 1);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerIntangibleCounter();
            state.prop.registerCounter("WraithFormLoseDexterity", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 4.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.prop.addEndOfTurnHandler("LoseDexterityPerTurn", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_DEXTERITY, state.getCounterForRead()[counterIdx]);
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.loseDexterityPerTurnCounterIdx = idx;
        }
    }

    public static class WraithForm extends _WraithFormT {
        public WraithForm() {
            super("WraithForm", Card.SKILL, 3, 2);
        }
    }

    public static class WraithFormP extends _WraithFormT {
        public WraithFormP() {
            super("WraithForm+", Card.POWER, 3, 3);
        }
    }
}
