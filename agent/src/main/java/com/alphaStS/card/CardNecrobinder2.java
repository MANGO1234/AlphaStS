package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyReadOnly;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventEnemyDebuffHandler;
import com.alphaStS.eventHandler.GameEventEnemyHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.random.RandomGenCtx;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CardNecrobinder2 {
    // **************************************************************************************************
    // ********************************************* Basic  *********************************************
    // **************************************************************************************************

    private static abstract class _BodyguardT extends Card {
        private final int n;

        public _BodyguardT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.n = n;
            entityProperty.canSummon = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.summon(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Bodyguard extends _BodyguardT {
        public Bodyguard() {
            super("Bodyguard", 5);
        }
    }

    public static class BodyguardP extends _BodyguardT {
        public BodyguardP() {
            super("Bodyguard+", 7);
        }
    }

    public static class Defend extends Card.Defend {
    }

    public static class DefendP extends Card.DefendP {
    }

    public static class Strike extends Card.Strike {
    }

    public static class StrikeP extends Card.StrikeP {
    }

    private static abstract class _UnleashT extends Card {
        private final int damage;

        public _UnleashT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int otsyHP = state.properties.otsyHPCounterIdx >= 0 ? state.getCounterForRead()[state.properties.otsyHPCounterIdx] : 0;
            state.otsyDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage + otsyHP);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Unleash extends _UnleashT {
        public Unleash() {
            super("Unleash", 6);
        }
    }

    public static class UnleashP extends _UnleashT {
        public UnleashP() {
            super("Unleash+", 9);
        }
    }

    // **************************************************************************************************
    // ********************************************* Common *********************************************
    // **************************************************************************************************

    private static abstract class _AfterlifeT extends Card {
        private final int n;

        public _AfterlifeT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.n = n;
            this.exhaustWhenPlayed = true;
            entityProperty.canSummon = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.summon(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Afterlife extends _AfterlifeT {
        public Afterlife() {
            super("Afterlife", 6);
        }
    }

    public static class AfterlifeP extends _AfterlifeT {
        public AfterlifeP() {
            super("Afterlife+", 9);
        }
    }

    private static abstract class _BlightStrikeT extends Card {
        private final int damage;

        public _BlightStrikeT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            entityProperty.doomEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            int dmgDealt = state.playerDoDamageToEnemy(enemy, damage);
            if (dmgDealt > 0 && enemy.isAlive()) {
                enemy.applyDebuff(state, DebuffType.DOOM, dmgDealt);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BlightStrike extends _BlightStrikeT {
        public BlightStrike() {
            super("Blight Strike", 8);
        }
    }

    public static class BlightStrikeP extends _BlightStrikeT {
        public BlightStrikeP() {
            super("Blight Strike+", 10);
        }
    }

    private static abstract class _DefileT extends Card {
        private final int damage;

        public _DefileT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.ethereal = true;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Defile extends _DefileT {
        public Defile() {
            super("Defile", 13);
        }
    }

    public static class DefileP extends _DefileT {
        public DefileP() {
            super("Defile+", 17);
        }
    }

    private static abstract class _DefyT extends Card {
        private final int block;
        private final int weak;

        public _DefyT(String cardName, int block, int weak) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.block = block;
            this.weak = weak;
            this.ethereal = true;
            entityProperty.selectEnemy = true;
            entityProperty.weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, weak);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Defy extends _DefyT {
        public Defy() {
            super("Defy", 6, 1);
        }
    }

    public static class DefyP extends _DefyT {
        public DefyP() {
            super("Defy+", 7, 2);
        }
    }

    private static abstract class _DrainPowerT extends Card {
        private final int damage;
        private final int numUpgrades;
        private boolean canUpgrade = true;

        public _DrainPowerT(String cardName, int damage, int numUpgrades) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.numUpgrades = numUpgrades;
            entityProperty.selectEnemy = true;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            canUpgrade = (state.properties.generateCardOptions & GameProperties.GENERATE_CARD_DRAIN_POWER) != 0;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            if (!canUpgrade) {
                return GameActionCtx.PLAY_CARD;
            }
            var discardArr = state.getDiscardArrForRead();
            int discardLen = state.getNumCardsInDiscard();
            var upgradeablePositions = new ArrayList<Integer>();
            for (int i = 0; i < discardLen; i++) {
                if (state.properties.upgradeIdxes[discardArr[i]] >= 0) {
                    upgradeablePositions.add(i);
                }
            }
            int upgrades = Math.min(numUpgrades, upgradeablePositions.size());
            if (upgrades == 0) {
                return GameActionCtx.PLAY_CARD;
            }
            if (upgradeablePositions.size() > numUpgrades) {
                state.setIsStochastic();
                for (int i = 0; i < upgrades; i++) {
                    int pick = i + state.getSearchRandomGen().nextInt(upgradeablePositions.size() - i, RandomGenCtx.RandomCardDiscardAggression);
                    int temp = upgradeablePositions.get(i);
                    upgradeablePositions.set(i, upgradeablePositions.get(pick));
                    upgradeablePositions.set(pick, temp);
                }
            }
            var writeArr = state.getDiscardArrForWrite();
            for (int i = 0; i < upgrades; i++) {
                int pos = upgradeablePositions.get(i);
                state.transformCard(writeArr, pos, state.properties.upgradeIdxes[discardArr[pos]]);
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            if ((properties.generateCardOptions & GameProperties.GENERATE_CARD_DRAIN_POWER) == 0) {
                return List.of();
            }
            return cards.stream().map(Card::getUpgrade).filter(Objects::nonNull).toList();
        }
    }

    public static class DrainPower extends _DrainPowerT {
        public DrainPower() {
            super("Drain Power", 10, 2);
        }
    }

    public static class DrainPowerP extends _DrainPowerT {
        public DrainPowerP() {
            super("Drain Power+", 12, 3);
        }
    }

    private static abstract class _FearT extends Card {
        private final int damage;
        private final int vuln;

        public _FearT(String cardName, int damage, int vuln) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.vuln = vuln;
            this.ethereal = true;
            entityProperty.selectEnemy = true;
            entityProperty.vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, vuln);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Fear extends _FearT {
        public Fear() {
            super("Fear", 7, 1);
        }
    }

    public static class FearP extends _FearT {
        public FearP() {
            super("Fear+", 8, 2);
        }
    }

    private static abstract class _FlattenT extends Card {
        private final int damage;

        public _FlattenT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.otsyDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public int energyCost(GameState state) {
            return state.properties.otsyAttackedThisTurnCounterIdx >= 0 &&
                   state.getCounterForRead()[state.properties.otsyAttackedThisTurnCounterIdx] > 0 ? 0 : 2;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerOtsyAttackedThisTurnCounter();
        }
    }

    public static class Flatten extends _FlattenT {
        public Flatten() {
            super("Flatten", 12);
        }
    }

    public static class FlattenP extends _FlattenT {
        public FlattenP() {
            super("Flatten+", 16);
        }
    }

    private static abstract class _GraveblastT extends Card {
        private final int damage;

        public _GraveblastT(String cardName, int damage, boolean exhaustWhenPlayed) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.exhaustWhenPlayed = exhaustWhenPlayed;
            entityProperty.selectEnemy = true;
            entityProperty.selectFromDiscard = true;
            selectFromDiscardLater = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
                return GameActionCtx.SELECT_CARD_DISCARD;
            } else {
                if (state.getNumCardsInHand() < GameState.HAND_LIMIT) {
                    state.removeCardFromDiscard(idx);
                    state.addCardToHand(idx);
                }
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class Graveblast extends _GraveblastT {
        public Graveblast() {
            super("Graveblast", 4, true);
        }
    }

    public static class GraveblastP extends _GraveblastT {
        public GraveblastP() {
            super("Graveblast+", 6, false);
        }
    }

    private static abstract class _GraveWardenT extends Card {
        private final int block;

        public _GraveWardenT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.addCardToDeck(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class GraveWarden extends _GraveWardenT {
        public GraveWarden() {
            super("Grave Warden", 8);
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.Soul());
        }
    }

    public static class GraveWardenP extends _GraveWardenT {
        public GraveWardenP() {
            super("Grave Warden+", 10);
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.SoulP());
        }
    }

    private static abstract class _InvokeT extends Card {
        private final int n;
        private final GameProperties.LocalCounterRegistrant energyRegistrant = new GameProperties.LocalCounterRegistrant();

        public _InvokeT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.n = n;
            entityProperty.canSummon = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            state.getCounterForWrite()[energyRegistrant.getCounterIdx(state.properties)] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerSummonNextTurnCounter(state, this);
            state.properties.registerEnergyNextTurnCounter(state, energyRegistrant);
        }
    }

    public static class Invoke extends _InvokeT {
        public Invoke() {
            super("Invoke", 2);
        }
    }

    public static class InvokeP extends _InvokeT {
        public InvokeP() {
            super("Invoke+", 3);
        }
    }

    private static abstract class _NegativePulseT extends Card {
        private final int block;
        private final int doom;

        public _NegativePulseT(String cardName, int block, int doom) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.block = block;
            this.doom = doom;
            entityProperty.doomEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.DOOM, doom);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class NegativePulse extends _NegativePulseT {
        public NegativePulse() {
            super("Negative Pulse", 5, 7);
        }
    }

    public static class NegativePulseP extends _NegativePulseT {
        public NegativePulseP() {
            super("Negative Pulse+", 6, 11);
        }
    }

    private static abstract class _PokeT extends Card {
        private final int damage;

        public _PokeT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.otsyDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Poke extends _PokeT {
        public Poke() {
            super("Poke", 6);
        }
    }

    public static class PokeP extends _PokeT {
        public PokeP() {
            super("Poke+", 9);
        }
    }

    private static abstract class _PullAggroT extends Card {
        private final int n;
        private final int block;

        public _PullAggroT(String cardName, int n, int block) {
            super(cardName, Card.SKILL, 2, Card.COMMON);
            this.n = n;
            this.block = block;
            entityProperty.canSummon = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.summon(n);
            state.playerGainBlock(block);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PullAggro extends _PullAggroT {
        public PullAggro() {
            super("Pull Aggro", 4, 7);
        }
    }

    public static class PullAggroP extends _PullAggroT {
        public PullAggroP() {
            super("Pull Aggro+", 5, 9);
        }
    }

    private static abstract class _ReapT extends Card {
        private final int damage;

        public _ReapT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 3, Card.COMMON);
            this.damage = damage;
            this.retain = true;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Reap extends _ReapT {
        public Reap() {
            super("Reap", 27);
        }
    }

    public static class ReapP extends _ReapT {
        public ReapP() {
            super("Reap+", 33);
        }
    }

    private static abstract class _ReaveT extends Card {
        private final int damage;

        public _ReaveT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            state.addCardToDeck(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Reave extends _ReaveT {
        public Reave() {
            super("Reave", 9);
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.Soul());
        }
    }

    public static class ReaveP extends _ReaveT {
        public ReaveP() {
            super("Reave+", 11);
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.SoulP());
        }
    }

    private static abstract class _ScourgeT extends Card {
        private final int doom;
        private final int draws;

        public _ScourgeT(String cardName, int doom, int draws) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.doom = doom;
            this.draws = draws;
            entityProperty.selectEnemy = true;
            entityProperty.doomEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.DOOM, doom);
            state.draw(draws);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Scourge extends _ScourgeT {
        public Scourge() {
            super("Scourge", 13, 1);
        }
    }

    public static class ScourgeP extends _ScourgeT {
        public ScourgeP() {
            super("Scourge+", 16, 2);
        }
    }

    // TODO: Sculpting Strike (Common) - 1 energy, Attack
    //   Effect: Deal 8 damage. Add Ethereal to a card in your Hand.
    //   Upgraded Effect: Deal 11 damage. Add Ethereal to a card in your Hand.

    // TODO: Snap (Common) - 1 energy, Attack
    //   Effect: Osty deals 7 damage. Add Retain to a card in your Hand.
    //   Upgraded Effect: Osty deals 10 damage. Add Retain to a card in your Hand.

    private static abstract class _SowT extends Card {
        private final int damage;

        public _SowT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.retain = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, damage);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Sow extends _SowT {
        public Sow() {
            super("Sow", 8);
        }
    }

    public static class SowP extends _SowT {
        public SowP() {
            super("Sow+", 11);
        }
    }

    private static abstract class _WispT extends Card {
        public _WispT(String cardName, boolean retain) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.exhaustWhenPlayed = true;
            this.retain = retain;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Wisp extends _WispT {
        public Wisp() {
            super("Wisp", false);
        }
    }

    public static class WispP extends _WispT {
        public WispP() {
            super("Wisp+", true);
        }
    }

    // **************************************************************************************************
    // ********************************************* Uncommon *********************************************
    // **************************************************************************************************

    private static abstract class _BoneShardsT extends Card {
        private final int n;

        public _BoneShardsT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int otsyHP = state.properties.otsyHPCounterIdx >= 0 ? state.getCounterForRead()[state.properties.otsyHPCounterIdx] : 0;
            if (otsyHP > 0) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    state.otsyDoDamageToEnemy(enemy, n);
                }
                state.playerGainBlock(n);
                state.getCounterForWrite()[state.properties.otsyHPCounterIdx] = 0;
                state.getCounterForWrite()[state.properties.otsyMaxHPCounterIdx] = 0;
                for (var handler : state.properties.onOtsyDeathHandlers) {
                    handler.handle(state);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BoneShards extends _BoneShardsT {
        public BoneShards() {
            super("Bone Shards", 9);
        }
    }

    public static class BoneShardsP extends _BoneShardsT {
        public BoneShardsP() {
            super("Bone Shards+", 12);
        }
    }

    private static abstract class _BorrowedTimeT extends Card {
        private final int energy;

        public _BorrowedTimeT(String cardName, int energy) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.energy = energy;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().applyDebuff(state, DebuffType.DOOM, 3);
            state.gainEnergy(energy);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerPlayerDoomCounter();
        }
    }

    public static class BorrowedTime extends _BorrowedTimeT {
        public BorrowedTime() {
            super("Borrowed Time", 1);
        }
    }

    public static class BorrowedTimeP extends _BorrowedTimeT {
        public BorrowedTimeP() {
            super("Borrowed Time+", 2);
        }
    }

    private static abstract class _BuryT extends Card {
        private final int damage;

        public _BuryT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 4, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Bury extends _BuryT {
        public Bury() {
            super("Bury", 52);
        }
    }

    public static class BuryP extends _BuryT {
        public BuryP() {
            super("Bury+", 63);
        }
    }

    private static abstract class _CalcifyT extends Card {
        private final int n;

        public _CalcifyT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("CalcifyBonus", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.otsyDamageBonusCounterIdx = idx;
        }
    }

    public static class Calcify extends _CalcifyT {
        public Calcify() {
            super("Calcify", 4);
        }
    }

    public static class CalcifyP extends _CalcifyT {
        public CalcifyP() {
            super("Calcify+", 6);
        }
    }

    private static abstract class _CaptureSpiritT extends Card {
        private final int hpLoss;
        private final int souls;

        public _CaptureSpiritT(String cardName, int hpLoss, int souls) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.hpLoss = hpLoss;
            this.souls = souls;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), hpLoss, false);
            for (int i = 0; i < souls; i++) {
                state.addCardToDeck(generatedCardIdx);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class CaptureSpirit extends _CaptureSpiritT {
        public CaptureSpirit() {
            super("Capture Spirit", 3, 3);
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.Soul());
        }
    }

    public static class CaptureSpiritP extends _CaptureSpiritT {
        public CaptureSpiritP() {
            super("Capture Spirit+", 4, 4);
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.SoulP());
        }
    }

    private static abstract class _CleanseT extends Card {
        private final int n;

        public _CleanseT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.n = n;
            entityProperty.canSummon = true;
            entityProperty.selectFromDeck = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.summon(n);
            state.removeCardFromDeck(idx, false);
            state.exhaustedCardHandle(idx, false);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Cleanse extends _CleanseT {
        public Cleanse() {
            super("Cleanse", 3);
        }
    }

    public static class CleanseP extends _CleanseT {
        public CleanseP() {
            super("Cleanse+", 5);
        }
    }

    private static abstract class _CountdownT extends Card {
        private final int n;

        public _CountdownT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.n = n;
            entityProperty.doomEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Countdown", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Countdown", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int doom = state.getCounterForRead()[counterIdx];
                    if (doom > 0) {
                        int enemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
                        if (enemyIdx >= 0) {
                            state.getEnemiesForWrite().getForWrite(enemyIdx).applyDebuff(state, DebuffType.DOOM, doom);
                        }
                    }
                }
            });
        }
    }

    public static class Countdown extends _CountdownT {
        public Countdown() {
            super("Countdown", 6);
        }
    }

    public static class CountdownP extends _CountdownT {
        public CountdownP() {
            super("Countdown+", 9);
        }
    }

    private static abstract class _DanseMacabreT extends Card {
        private final int block;

        public _DanseMacabreT(String cardName, int block) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += block;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("DanseMacabre", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("DanseMacabre", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int blockAmt = state.getCounterForRead()[counterIdx];
                    if (blockAmt > 0 && energyUsed >= 2) {
                        state.playerGainBlockNotFromCardPlay(blockAmt);
                    }
                }
            });
        }
    }

    public static class DanseMacabre extends _DanseMacabreT {
        public DanseMacabre() {
            super("Danse Macabre", 3);
        }
    }

    public static class DanseMacabreP extends _DanseMacabreT {
        public DanseMacabreP() {
            super("Danse Macabre+", 4);
        }
    }

    private static abstract class _DeathMarchT extends Card {
        private final int damage;
        private final int bonus;
        private final GameProperties.LocalCounterRegistrant drawnRegistrant = new GameProperties.LocalCounterRegistrant();

        public _DeathMarchT(String cardName, int damage, int bonus) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
            this.bonus = bonus;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int drawn = state.getCounterForRead()[drawnRegistrant.getCounterIdx(state.properties)];
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage + bonus * drawn);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("CardsDrawnThisTurn", drawnRegistrant, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[drawnRegistrant.getCounterIdx(state.properties)] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("CardsDrawnThisTurn", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[drawnRegistrant.getCounterIdx(state.properties)] = 0;
                }
            });
            state.properties.addOnCardDrawnHandler("CardsDrawnThisTurn", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    state.getCounterForWrite()[drawnRegistrant.getCounterIdx(state.properties)]++;
                }
            });
        }
    }

    public static class DeathMarch extends _DeathMarchT {
        public DeathMarch() {
            super("Death March", 8, 3);
        }
    }

    public static class DeathMarchP extends _DeathMarchT {
        public DeathMarchP() {
            super("Death March+", 9, 4);
        }
    }

    private static abstract class _DeathsDoorT extends Card {
        private final int block;
        private final GameProperties.LocalCounterRegistrant doomAppliedRegistrant = new GameProperties.LocalCounterRegistrant();

        public _DeathsDoorT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            if (state.getCounterForRead()[doomAppliedRegistrant.getCounterIdx(state.properties)] > 0) {
                state.playerGainBlock(block);
                state.playerGainBlock(block);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("DoomAppliedThisTurn", doomAppliedRegistrant, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[doomAppliedRegistrant.getCounterIdx(state.properties)] > 0 ? 1.0f : 0.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnEnemyDebuffHandler("DoomAppliedThisTurn", new GameEventEnemyDebuffHandler() {
                @Override public void handle(GameState state, EnemyReadOnly enemy, DebuffType type, int amount) {
                    if (type == DebuffType.DOOM) {
                        state.getCounterForWrite()[doomAppliedRegistrant.getCounterIdx(state.properties)] = 1;
                    }
                }
            });
            state.properties.addStartOfTurnHandler("DoomAppliedThisTurn", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[doomAppliedRegistrant.getCounterIdx(state.properties)] = 0;
                }
            });
        }
    }

    public static class DeathsDoor extends _DeathsDoorT {
        public DeathsDoor() {
            super("Death's Door", 6);
        }
    }

    public static class DeathsDoorP extends _DeathsDoorT {
        public DeathsDoorP() {
            super("Death's Door+", 7);
        }
    }

    private static abstract class _DeathbringerT extends Card {
        private final int doom;

        public _DeathbringerT(String cardName, int doom) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
            this.doom = doom;
            entityProperty.doomEnemy = true;
            entityProperty.weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.DOOM, doom);
                enemy.applyDebuff(state, DebuffType.WEAK, 1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Deathbringer extends _DeathbringerT {
        public Deathbringer() {
            super("Deathbringer", 21);
        }
    }

    public static class DeathbringerP extends _DeathbringerT {
        public DeathbringerP() {
            super("Deathbringer+", 26);
        }
    }

    private static abstract class _DebilitateT extends Card {
        private final int damage;
        private final int turns;

        public _DebilitateT(String cardName, int damage, int turns) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
            this.turns = turns;
            entityProperty.selectEnemy = true;
            entityProperty.debilitateEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage);
            enemy.applyDebuff(state, DebuffType.DEBILITATE, turns);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Debilitate extends _DebilitateT {
        public Debilitate() {
            super("Debilitate", 7, 3);
        }
    }

    public static class DebilitateP extends _DebilitateT {
        public DebilitateP() {
            super("Debilitate+", 9, 4);
        }
    }

    private static abstract class _DelayT extends Card {
        private final int block;
        private final int energyGain;
        private final GameProperties.LocalCounterRegistrant energyRegistrant = new GameProperties.LocalCounterRegistrant();

        public _DelayT(String cardName, int block, int energyGain) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
            this.block = block;
            this.energyGain = energyGain;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.getCounterForWrite()[energyRegistrant.getCounterIdx(state.properties)] += energyGain;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerEnergyNextTurnCounter(state, energyRegistrant);
        }
    }

    public static class Delay extends _DelayT {
        public Delay() {
            super("Delay", 11, 1);
        }
    }

    public static class DelayP extends _DelayT {
        public DelayP() {
            super("Delay+", 13, 2);
        }
    }

    private static abstract class _DirgeT extends Card {
        private final int summonPerX;

        public _DirgeT(String cardName, int summonPerX) {
            super(cardName, Card.SKILL, -1, Card.UNCOMMON);
            this.summonPerX = summonPerX;
            this.isXCost = true;
            this.delayUseEnergy = true;
            entityProperty.canSummon = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (energyUsed > 0) {
                state.summon(summonPerX * energyUsed);
                for (int i = 0; i < energyUsed; i++) {
                    state.addCardToDeck(generatedCardIdx);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public int energyCost(GameState state) {
            return state.energy;
        }
    }

    public static class Dirge extends _DirgeT {
        public Dirge() {
            super("Dirge", 3);
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.Soul());
        }
    }

    public static class DirgeP extends _DirgeT {
        public DirgeP() {
            super("Dirge+", 4);
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.SoulP());
        }
    }

    private static abstract class _DredgeT extends Card {
        public _DredgeT(String cardName, boolean retain) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.retain = retain;
            this.exhaustWhenPlayed = true;
            entityProperty.selectFromDiscard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.removeCardFromDiscard(idx);
            if (state.handArrLen < GameState.HAND_LIMIT) {
                state.addCardToHand(idx);
            }
            state.getCounterForWrite()[counterIdx]++;
            if (state.getCounterForWrite()[counterIdx] < 3) {
                return GameActionCtx.SELECT_CARD_DISCARD;
            } else {
                state.getCounterForWrite()[counterIdx] = 0;
                return GameActionCtx.PLAY_CARD;
            }
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Dredge", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }
    }

    public static class Dredge extends _DredgeT {
        public Dredge() {
            super("Dredge", false);
        }
    }

    public static class DredgeP extends _DredgeT {
        public DredgeP() {
            super("Dredge+", true);
        }
    }

    private static abstract class _EnfeeblingTouchT extends Card {
        private final int n;

        public _EnfeeblingTouchT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.n = n;
            this.ethereal = true;
            entityProperty.selectEnemy = true;
            entityProperty.affectEnemyStrengthEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class EnfeeblingTouch extends _EnfeeblingTouchT {
        public EnfeeblingTouch() {
            super("Enfeebling Touch", 8);
        }
    }

    public static class EnfeeblingTouchP extends _EnfeeblingTouchT {
        public EnfeeblingTouchP() {
            super("Enfeebling Touch+", 11);
        }
    }

    // TODO: Fetch (Uncommon) - 0 energy, Attack
    //   Effect: Osty deals 3 damage. If this is the first time this card has been played this turn, draw 1 card.
    //   Upgraded Effect: Osty deals 6 damage. If this is the first time this card has been played this turn, draw 1 card.
    //   Hint: every instance of fetch counts separately -> to properly make it work, have a FetchUsed/FetchUsedP card
    //   that's transform on play, and then transform back to Fetch end of turn

    private static abstract class _FriendshipT extends Card {
        private final int strengthLoss;

        public _FriendshipT(String cardName, int strengthLoss) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.strengthLoss = strengthLoss;
            entityProperty.changePlayerStrength = true;
            entityProperty.changeEnergyRefill = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_STRENGTH, strengthLoss);
            state.energyRefill += 1;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Friendship extends _FriendshipT {
        public Friendship() {
            super("Friendship", 2);
        }
    }

    public static class FriendshipP extends _FriendshipT {
        public FriendshipP() {
            super("Friendship+", 1);
        }
    }

    private static abstract class _HauntT extends Card {
        private final int n;

        public _HauntT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Haunt", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            var isSoul = new boolean[state.properties.cardDict.length];
            for (int i = 0; i < state.properties.cardDict.length; i++) {
                var base = state.properties.cardDict[i].getBaseCard();
                isSoul[i] = base instanceof CardColorless2.Soul || base instanceof CardColorless2.SoulP;
            }
            state.properties.addOnCardPlayedHandler("Haunt", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int dmg = state.getCounterForRead()[counterIdx];
                    if (dmg > 0 && isSoul[cardIdx]) {
                        int enemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
                        if (enemyIdx >= 0) {
                            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(enemyIdx), dmg, false);
                        }
                    }
                }
            });
        }
    }

    public static class Haunt extends _HauntT {
        public Haunt() {
            super("Haunt", 6);
        }
    }

    public static class HauntP extends _HauntT {
        public HauntP() {
            super("Haunt+", 8);
        }
    }

    private static abstract class _HighFiveT extends Card {
        private final int damage;
        private final int vuln;

        public _HighFiveT(String cardName, int damage, int vuln) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            this.vuln = vuln;
            entityProperty.vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.otsyDoDamageToEnemy(enemy, damage);
                enemy.applyDebuff(state, DebuffType.VULNERABLE, vuln);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class HighFive extends _HighFiveT {
        public HighFive() {
            super("High Five", 11, 2);
        }
    }

    public static class HighFiveP extends _HighFiveT {
        public HighFiveP() {
            super("High Five+", 13, 3);
        }
    }

    // No need to implement Legion of Bone: Multiplayer

    private static abstract class _LethalityT extends Card {
        private final int bonus;

        public _LethalityT(String cardName, int bonus) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.bonus = bonus;
            this.ethereal = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += bonus;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Lethality", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 100.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.registerAttacksPlayedThisTurnCounter();
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.lethalityCounterIdx = idx;
        }
    }

    public static class Lethality extends _LethalityT {
        public Lethality() {
            super("Lethality", 50);
        }
    }

    public static class LethalityP extends _LethalityT {
        public LethalityP() {
            super("Lethality+", 75);
        }
    }

    private static abstract class _MelancholyT extends Card {
        private final int block;

        public _MelancholyT(String cardName, int block) {
            super(cardName, Card.SKILL, 3, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public int energyCost(GameState state) {
            if (state == null) return 3;
            return Math.max(0, 3 - state.getCounterForRead()[counterIdx]);
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("MelancholyDeathCount", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnEnemyDeathHandler("MelancholyDeathCount", new GameEventEnemyHandler() {
                @Override public void handle(GameState state, EnemyReadOnly enemy) {
                    state.getCounterForWrite()[counterIdx]++;
                }
            });
            state.properties.addOnOtsyDeathHandler("MelancholyDeathCount", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx]++;
                }
            });
        }
    }

    public static class Melancholy extends _MelancholyT {
        public Melancholy() {
            super("Melancholy", 13);
        }
    }

    public static class MelancholyP extends _MelancholyT {
        public MelancholyP() {
            super("Melancholy+", 17);
        }
    }

    private static abstract class _NoEscapeT extends Card {
        private final int baseDoom;

        public _NoEscapeT(String cardName, int baseDoom) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.baseDoom = baseDoom;
            entityProperty.selectEnemy = true;
            entityProperty.doomEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            int doom = baseDoom + (enemy.getDoom() / 10) * 5;
            enemy.applyDebuff(state, DebuffType.DOOM, doom);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class NoEscape extends _NoEscapeT {
        public NoEscape() {
            super("No Escape", 10);
        }
    }

    public static class NoEscapeP extends _NoEscapeT {
        public NoEscapeP() {
            super("No Escape+", 15);
        }
    }

    private static abstract class _PagestormT extends Card {
        public _PagestormT(String cardName, int cost) {
            super(cardName, Card.POWER, cost, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Pagestorm", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardDrawnHandler("Pagestorm", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int stacks = state.getCounterForRead()[counterIdx];
                    if (stacks > 0 && state.properties.cardDict[cardIdx].ethereal) {
                        state.draw(stacks);
                    }
                }
            });
        }
    }

    public static class Pagestorm extends _PagestormT {
        public Pagestorm() {
            super("Pagestorm", 1);
        }
    }

    public static class PagestormP extends _PagestormT {
        public PagestormP() {
            super("Pagestorm+", 0);
        }
    }

    private static abstract class _ParseT extends Card {
        private final int n;

        public _ParseT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.n = n;
            this.ethereal = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.draw(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Parse extends _ParseT {
        public Parse() {
            super("Parse", 3);
        }
    }

    public static class ParseP extends _ParseT {
        public ParseP() {
            super("Parse+", 4);
        }
    }

    private static abstract class _PullFromBelowT extends Card {
        private final int dmgPerCard;
        private final GameProperties.LocalCounterRegistrant etherealCountRegistrant = new GameProperties.LocalCounterRegistrant();

        public _PullFromBelowT(String cardName, int dmgPerCard) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.dmgPerCard = dmgPerCard;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int count = state.getCounterForRead()[etherealCountRegistrant.getCounterIdx(state.properties)];
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmgPerCard * count);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("EtherealCardsPlayedThisCombat", etherealCountRegistrant, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[etherealCountRegistrant.getCounterIdx(state.properties)] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("EtherealCardsPlayedThisCombat", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].ethereal) {
                        state.getCounterForWrite()[etherealCountRegistrant.getCounterIdx(state.properties)]++;
                    }
                }
            });
        }
    }

    public static class PullFromBelow extends _PullFromBelowT {
        public PullFromBelow() {
            super("Pull from Below", 5);
        }
    }

    public static class PullFromBelowP extends _PullFromBelowT {
        public PullFromBelowP() {
            super("Pull from Below+", 7);
        }
    }

    private static abstract class _PutrefyT extends Card {
        private final int n;

        public _PutrefyT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.n = n;
            this.exhaustWhenPlayed = true;
            entityProperty.selectEnemy = true;
            entityProperty.weakEnemy = true;
            entityProperty.vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            enemy.applyDebuff(state, DebuffType.WEAK, n);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Putrefy extends _PutrefyT {
        public Putrefy() {
            super("Putrefy", 2);
        }
    }

    public static class PutrefyP extends _PutrefyT {
        public PutrefyP() {
            super("Putrefy+", 3);
        }
    }

    // TODO: Rattle (Uncommon) - 1 energy, Attack
    //   Effect: Osty deals 7 damage. Hits an additional time for each other time he has attacked this turn.
    //   Upgraded Effect: Osty deals 9 damage. Hits an additional time for each other time he has attacked this turn.

    // TODO: Right Hand Hand (Uncommon) - 0 energy, Attack
    //   Effect: Osty deals 4 damage. Whenever you play a card that costs 2 energy or more, return this to your Hand from the Discard Pile.
    //   Upgraded Effect: Osty deals 6 damage. Whenever you play a card that costs 2 energy or more, return this to your Hand from the Discard Pile.

    // TODO: Severance (Uncommon) - 2 energy, Attack
    //   Effect: Deal 13 damage. Add a Soul into your Draw Pile, Hand, and Discard Pile.
    //   Upgraded Effect: Deal 18 damage. Add a Soul into your Draw Pile, Hand, and Discard Pile.

    // TODO: Shroud (Uncommon) - 1 energy, Power
    //   Effect: Whenever you apply Doom, gain 2 Block.
    //   Upgraded Effect: Whenever you apply Doom, gain 3 Block.

    // TODO: Sic 'Em (Uncommon) - 1 energy, Attack
    //   Effect: Osty deals 5 damage. Whenever Osty hits this enemy this turn, Summon 2.
    //   Upgraded Effect: Osty deals 6 damage. Whenever Osty hits this enemy this turn, Summon 3.

    // TODO: Sleight of Flesh (Uncommon) - 2 energy, Power
    //   Effect: Whenever you apply a debuff to an enemy, they take 9 damage.
    //   Upgraded Effect: Whenever you apply a debuff to an enemy, they take 13 damage.

    // TODO: Spur (Uncommon) - 1 energy, Skill
    //   Effect: Retain. Summon 3. Osty heals 5 HP.
    //   Upgraded Effect: Retain. Summon 5. Osty heals 7 HP.

    // TODO: Veilpiercer (Uncommon) - 1 energy, Attack
    //   Effect: Deal 10 damage. The next Ethereal card you play costs 0 energy.
    //   Upgraded Effect: Deal 13 damage. The next Ethereal card you play costs 0 energy.

    // **************************************************************************************************
    // *********************************************  Rare  *********************************************
    // **************************************************************************************************

    // TODO: Banshee's Cry (Rare) - 6 energy, Attack
    //   Effect: Deal 33 damage to ALL enemies. Costs 2 energy less for each Ethereal card played this combat.
    //   Upgraded Effect: Deal 39 damage to ALL enemies. Costs 2 energy less for each Ethereal card played this combat.

    // TODO: Call of the Void (Rare) - 1 energy, Power
    //   Effect: At the start of your turn, add 1 random card into your Hand. It gains Ethereal.
    //   Upgraded Effect: Innate. At the start of your turn, add 1 random card into your Hand. It gains Ethereal.

    // TODO: Demesne (Rare) - 3 energy, Power
    //   Effect: Ethereal. At the start of your turn, gain energy and draw 1 additional card.
    //   Upgraded Effect (2 energy): Ethereal. At the start of your turn, gain energy and draw 1 additional card.

    // TODO: Devour Life (Rare) - 1 energy, Power
    //   Effect: Whenever you play a Soul, Summon 1.
    //   Upgraded Effect: Whenever you play a Soul, Summon 2.

    // TODO: Eidolon (Rare) - 2 energy, Skill
    //   Effect: Exhaust your Hand. If 9 cards were Exhausted this way, gain 1 Intangible.
    //   Upgraded Effect (1 energy): Exhaust your Hand. If 9 cards were Exhausted this way, gain 1 Intangible.

    // TODO: End of Days (Rare) - 3 energy, Skill
    //   Effect: Apply 29 Doom to ALL enemies. Kill enemies with at least as much Doom as HP.
    //   Upgraded Effect: Apply 37 Doom to ALL enemies. Kill enemies with at least as much Doom as HP.

    // TODO: Eradicate (Rare) - X energy, Attack
    //   Effect: Retain. Deal 11 damage X times.
    //   Upgraded Effect: Retain. Deal 14 damage X times.

    // TODO: Glimpse Beyond (Rare) - 1 energy, Skill
    //   Effect: ALL players add 3 Souls into their Draw Pile. Exhaust.
    //   Upgraded Effect: ALL players add 4 Souls into their Draw Pile. Exhaust.

    // TODO: Hang (Rare) - 1 energy, Attack
    //   Effect: Deal 10 damage. Double the damage ALL Hang cards deal to this enemy.
    //   Upgraded Effect: Deal 13 damage. Double the damage ALL Hang cards deal to this enemy.

    // TODO: Misery (Rare) - 0 energy, Attack
    //   Effect: Deal 7 damage. Apply any debuffs on the enemy to ALL other enemies.
    //   Upgraded Effect: Retain. Deal 9 damage. Apply any debuffs on the enemy to ALL other enemies.

    // TODO: Necro Mastery (Rare) - 2 energy, Power
    //   Effect: Summon 5. Whenever Osty loses HP, ALL enemies lose that much HP as well.
    //   Upgraded Effect: Summon 8. Whenever Osty loses HP, ALL enemies lose that much HP as well.

    // TODO: Neurosurge (Rare) - 0 energy, Power
    //   Effect: Gain 3 energy. Draw 2 cards. At the start of your turn, apply 3 Doom to yourself.
    //   Upgraded Effect: Gain 4 energy. Draw 2 cards. At the start of your turn, apply 3 Doom to yourself.

    // TODO: Oblivion (Rare) - 0 energy, Skill
    //   Effect: Whenever you play a card this turn, apply 3 Doom to the enemy.
    //   Upgraded Effect: Whenever you play a card this turn, apply 4 Doom to the enemy.

    // TODO: Reanimate (Rare) - 3 energy, Skill
    //   Effect: Summon 20. Exhaust.
    //   Upgraded Effect: Summon 25. Exhaust.

    // TODO: Reaper Form (Rare) - 3 energy, Power
    //   Effect: Whenever Attacks deal damage, they also apply that much Doom.
    //   Upgraded Effect: Retain. Whenever Attacks deal damage, they also apply that much Doom.

    // TODO: Sacrifice (Rare) - 1 energy, Skill
    //   Effect: Retain. If Osty is alive, he dies and you gain Block equal to double his Max HP.
    //   Upgraded Effect (0 energy): Retain. If Osty is alive, he dies and you gain Block equal to double his Max HP.

    // TODO: Seance (Rare) - 0 energy, Skill
    //   Effect: Ethereal. Transform a card in your Draw Pile into Soul.
    //   Upgraded Effect: Ethereal. Transform a card in your Draw Pile into Soul+.

    // TODO: Sentry Mode (Rare) - 2 energy, Power
    //   Effect: At the start of your turn, add 1 Sweeping Gaze into your Hand.
    //   Upgraded Effect (1 energy): At the start of your turn, add 1 Sweeping Gaze into your Hand.

    // TODO: Shared Fate (Rare) - 0 energy, Skill
    //   Effect: Lose 2 Strength. Enemy loses 2 Strength. Exhaust.
    //   Upgraded Effect: Lose 2 Strength. Enemy loses 3 Strength. Exhaust.

    // TODO: Soul Storm (Rare) - 1 energy, Attack
    //   Effect: Deal 9 damage. Deals 2 additional damage for each Soul in your Exhaust Pile.
    //   Upgraded Effect: Deal 9 damage. Deals 3 additional damage for each Soul in your Exhaust Pile.

    // TODO: Spirit of Ash (Rare) - 1 energy, Power
    //   Effect: Whenever you play an Ethereal card, gain 4 Block.
    //   Upgraded Effect: Whenever you play an Ethereal card, gain 5 Block.

    // TODO: Squeeze (Rare) - 3 energy, Attack
    //   Effect: Osty deals 25 damage. Deals 5 additional damage for ALL your other Osty Attacks.
    //   Upgraded Effect: Osty deals 30 damage. Deals 6 additional damage for ALL your other Osty Attacks.

    // TODO: The Scythe (Rare) - 2 energy, Attack
    //   Effect: Deal 13 damage. Permanently increase this card's damage by 3. Exhaust.
    //   Upgraded Effect: Deal 13 damage. Permanently increase this card's damage by 4. Exhaust.

    // TODO: Time's Up (Rare) - 2 energy, Attack
    //   Effect: Deal damage equal to the enemy's Doom. Exhaust.
    //   Upgraded Effect: Retain. Deal damage equal to the enemy's Doom. Exhaust.

    // TODO: Transfigure (Rare) - 1 energy, Skill
    //   Effect: Add Replay to a card in your Hand. It costs an extra energy. Exhaust.
    //   Upgraded Effect: Add Replay to a card in your Hand. It costs an extra energy.

    // TODO: Undeath (Rare) - 0 energy, Skill
    //   Effect: Gain 7 Block. Add a copy of this card into your Discard Pile.
    //   Upgraded Effect: Gain 9 Block. Add a copy of this card into your Discard Pile.

    // **************************************************************************************************
    // ********************************************* Ancient *********************************************
    // **************************************************************************************************

    // TODO: Forbidden Grimoire (Ancient) - 2 energy, Power
    //   Effect: At the end of combat, you may remove a card from your Deck. Eternal.
    //   Upgraded Effect (1 energy): At the end of combat, you may remove a card from your Deck. Eternal.

    // TODO: Protector (Ancient) - 1 energy, Attack
    //   Effect: Osty deals 10 damage. Deals additional damage equal to Osty's Max HP.
    //   Upgraded Effect (0 energy): Osty deals 15 damage. Deals additional damage equal to Osty's Max HP.
}
