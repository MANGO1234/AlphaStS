package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyReadOnly;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventEnemyDebuffHandler;
import com.alphaStS.eventHandler.GameEventEnemyHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.eventHandler.OnOtsyDamageHandler;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.random.RandomGenCtx;
import com.alphaStS.utils.CounterStat;
import com.alphaStS.utils.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
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

    // Bodyguard (Basic) - 1 energy, Skill
    //   Effect: Summon 5.
    //   Upgraded Effect: Summon 7.
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

    // Defend (Necrobinder) (Basic) - 1 energy, Skill
    //   Effect: Gain 5 Block.
    //   Upgraded Effect: Gain 8 Block.
    public static class Defend extends Card.Defend {
    }

    public static class DefendP extends Card.DefendP {
    }

    // Strike (Necrobinder) (Basic) - 1 energy, Attack
    //   Effect: Deal 6 damage.
    //   Upgraded Effect: Deal 9 damage.
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
            entityProperty.otsyAttack = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int otsyHP = state.properties.otsyHPCounterIdx >= 0 ? state.getCounterForRead()[state.properties.otsyHPCounterIdx] : 0;
            state.otsyDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage + otsyHP);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Unleash (Basic) - 1 energy, Attack
    //   Effect: Osty deals 6 damage. Deals additional damage equal to Osty's current HP.
    //   Upgraded Effect: Osty deals 9 damage. Deals additional damage equal to Osty's current HP.
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

    // Afterlife (Common) - 1 energy, Skill
    //   Effect: Summon 6. Exhaust.
    //   Upgraded Effect: Summon 9. Exhaust.
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
            int dmgDealt = state.playerDoDamageToEnemy(enemy, damage, this);
            if (dmgDealt > 0 && enemy.isAlive()) {
                enemy.applyDebuff(state, DebuffType.DOOM, dmgDealt);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Blight Strike (Common) - 1 energy, Attack
    //   Effect: Deal 8 damage. Apply Doom equal to damage dealt.
    //   Upgraded Effect: Deal 10 damage. Apply Doom equal to damage dealt.
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Defile (Common) - 1 energy, Attack
    //   Effect: Ethereal. Deal 13 damage.
    //   Upgraded Effect: Ethereal. Deal 17 damage.
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

    // Defy (Common) - 1 energy, Skill
    //   Effect: Ethereal. Gain 6 Block. Apply 1 Weak.
    //   Upgraded Effect: Ethereal. Gain 7 Block. Apply 2 Weak.
    // TODO CHANGED: Defy (Common) - 1 energy, Skill
    //   Effect: Ethereal. Gain 6 Block. Apply 1 Weak.
    //   Upgraded Effect: Ethereal. Gain 9 Block. Apply 1 Weak.
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
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

    // Drain Power (Common) - 1 energy, Attack
    //   Effect: Deal 10 damage. Upgrade 2 random cards in your Discard Pile.
    //   Upgraded Effect: Deal 12 damage. Upgrade 3 random cards in your Discard Pile.
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
            state.playerDoDamageToEnemy(enemy, damage, this);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, vuln);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Fear (Common) - 1 energy, Attack
    //   Effect: Ethereal. Deal 7 damage. Apply 1 Vulnerable.
    //   Upgraded Effect: Ethereal. Deal 8 damage. Apply 2 Vulnerable.
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
            entityProperty.otsyAttack = true;
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

    // Flatten (Common) - 2 energy, Attack
    //   Effect: Osty deals 12 damage. This card costs 0 energy if Osty has attacked this turn.
    //   Upgraded Effect: Osty deals 16 damage. This card costs 0 energy if Osty has attacked this turn.
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
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
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

    // Graveblast (Common) - 1 energy, Attack
    //   Effect: Deal 4 damage. Put a card from your Discard Pile into your Hand. Exhaust.
    //   Upgraded Effect: Deal 6 damage. Put a card from your Discard Pile into your Hand.
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

    // Grave Warden (Common) - 1 energy, Skill
    //   Effect: Gain 8 Block. Add a Soul into your Draw Pile.
    //   Upgraded Effect: Gain 10 Block. Add a Soul+ into your Draw Pile.
    // TODO CHANGED: Grave Warden (Common) - 1 energy, Skill
    //   Effect: Gain 8 Block. Add a Soul into your Draw Pile.
    //   Upgraded Effect: Gain 11 Block. Add a Soul into your Draw Pile.
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

    // Invoke (Common) - 1 energy, Skill
    //   Effect: Next turn, Summon 2 and gain 2 energy.
    //   Upgraded Effect: Next turn, Summon 3 and gain 3 energy.
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

    // Negative Pulse (Common) - 1 energy, Skill
    //   Effect: Gain 5 Block. Apply 7 Doom to ALL enemies.
    //   Upgraded Effect: Gain 6 Block. Apply 11 Doom to ALL enemies.
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
            entityProperty.otsyAttack = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.otsyDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Poke (Common) - 0 energy, Attack
    //   Effect: Osty deals 6 damage.
    //   Upgraded Effect: Osty deals 9 damage.
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

    // Pull Aggro (Common) - 2 energy, Skill
    //   Effect: Summon 4. Gain 7 Block.
    //   Upgraded Effect: Summon 5. Gain 9 Block.
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Reap (Common) - 3 energy, Attack
    //   Effect: Retain. Deal 27 damage.
    //   Upgraded Effect: Retain. Deal 33 damage.
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            state.addCardToDeck(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Reave (Common) - 1 energy, Attack
    //   Effect: Deal 9 damage. Add a Soul into your Draw Pile.
    //   Upgraded Effect: Deal 11 damage. Add a Soul+ into your Draw Pile.
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

    // Scourge (Common) - 1 energy, Skill
    //   Effect: Apply 13 Doom. Draw 1 card.
    //   Upgraded Effect: Apply 16 Doom. Draw 2 cards.
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

    private static abstract class _SculptingStrikeT extends Card {
        private final int damage;
        private int[] permEtherealTransformIdxes;

        public _SculptingStrikeT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            entityProperty.selectFromHand = true;
            selectFromHandLater = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
                for (int i = 0; i < state.handArrLen; i++) {
                    if (!state.properties.cardDict[state.handArr[i]].ethereal()) {
                        return GameActionCtx.SELECT_CARD_HAND;
                    }
                }
                return GameActionCtx.PLAY_CARD;
            } else {
                if (permEtherealTransformIdxes != null && permEtherealTransformIdxes[idx] >= 0) {
                    var handArr = state.getHandArrForWrite();
                    for (int i = 0; i < state.handArrLen; i++) {
                        if (handArr[i] == idx) {
                            state.transformCard(handArr, i, permEtherealTransformIdxes[idx]);
                            break;
                        }
                    }
                }
                return GameActionCtx.PLAY_CARD;
            }
        }

        @Override public boolean canSelectCard(Card card) { return !card.ethereal(); }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return cards.stream().filter(c -> !c.ethereal()).map(Card::getPermEthereal).toList();
        }

        @Override public void gamePropertiesSetup(GameState state) {
            permEtherealTransformIdxes = new int[state.properties.cardDict.length];
            Arrays.fill(permEtherealTransformIdxes, -1);
            for (int i = 0; i < state.properties.cardDict.length; i++) {
                if (!state.properties.cardDict[i].ethereal()) {
                    permEtherealTransformIdxes[i] = state.properties.findCardIndex(state.properties.cardDict[i].getPermEthereal());
                }
            }
        }
    }

    // Sculpting Strike (Common) - 1 energy, Attack
    //   Effect: Deal 8 damage. Add Ethereal to a card in your Hand.
    //   Upgraded Effect: Deal 11 damage. Add Ethereal to a card in your Hand.
    // TODO CHANGED: Sculpting Strike (Common) - 1 energy, Attack
    //   Effect: Deal 9 damage. Add Ethereal to a card in your Hand.
    //   Upgraded Effect: Deal 12 damage. Add Ethereal to a card in your Hand.
    public static class SculptingStrike extends _SculptingStrikeT {
        public SculptingStrike() { super("Sculpting Strike", 8); }
    }

    public static class SculptingStrikeP extends _SculptingStrikeT {
        public SculptingStrikeP() { super("Sculpting Strike+", 11); }
    }

    private static abstract class _SnapT extends Card {
        private final int damage;
        private int[] permRetainTransformIdxes;

        public _SnapT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            entityProperty.otsyAttack = true;
            entityProperty.selectFromHand = true;
            selectFromHandLater = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.otsyDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
                for (int i = 0; i < state.handArrLen; i++) {
                    if (!state.properties.cardDict[state.handArr[i]].retain()) {
                        return GameActionCtx.SELECT_CARD_HAND;
                    }
                }
                return GameActionCtx.PLAY_CARD;
            } else {
                if (permRetainTransformIdxes != null && permRetainTransformIdxes[idx] >= 0) {
                    var handArr = state.getHandArrForWrite();
                    for (int i = 0; i < state.handArrLen; i++) {
                        if (handArr[i] == idx) {
                            state.transformCard(handArr, i, permRetainTransformIdxes[idx]);
                            break;
                        }
                    }
                }
                return GameActionCtx.PLAY_CARD;
            }
        }

        @Override public boolean canSelectCard(Card card) { return !card.retain(); }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return cards.stream().filter(c -> !c.retain()).map(Card::getPermRetain).toList();
        }

        @Override public void gamePropertiesSetup(GameState state) {
            permRetainTransformIdxes = new int[state.properties.cardDict.length];
            Arrays.fill(permRetainTransformIdxes, -1);
            for (int i = 0; i < state.properties.cardDict.length; i++) {
                if (!state.properties.cardDict[i].retain()) {
                    permRetainTransformIdxes[i] = state.properties.findCardIndex(state.properties.cardDict[i].getPermRetain());
                }
            }
        }
    }

    // Snap (Common) - 1 energy, Attack
    //   Effect: Osty deals 7 damage. Add Retain to a card in your Hand.
    //   Upgraded Effect: Osty deals 10 damage. Add Retain to a card in your Hand.
    public static class Snap extends _SnapT {
        public Snap() { super("Snap", 7); }
    }

    public static class SnapP extends _SnapT {
        public SnapP() { super("Snap+", 10); }
    }

    private static abstract class _SowT extends Card {
        private final int damage;

        public _SowT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.retain = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, damage, this);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Sow (Common) - 1 energy, Attack
    //   Effect: Retain. Deal 8 damage to ALL enemies.
    //   Upgraded Effect: Retain. Deal 11 damage to ALL enemies.
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

    // Wisp (Common) - 0 energy, Skill
    //   Effect: Gain energy. Exhaust.
    //   Upgraded Effect: Retain. Gain energy. Exhaust.
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
            entityProperty.otsyAttack = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int otsyHP = state.properties.otsyHPCounterIdx >= 0 ? state.getCounterForRead()[state.properties.otsyHPCounterIdx] : 0;
            if (otsyHP > 0) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    state.otsyDoDamageToEnemy(enemy, n);
                }
                state.playerGainBlock(n);
                state.dealDamageToOtsy(otsyHP);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Bone Shards (Uncommon) - 1 energy, Attack
    //   Effect: If Osty is alive, he deals 9 damage to ALL enemies and you gain 9 Block. Osty dies.
    //   Upgraded Effect: If Osty is alive, he deals 12 damage to ALL enemies and you gain 12 Block. Osty dies.
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

    // Borrowed Time (Uncommon) - 0 energy, Skill
    //   Effect: Apply 3 Doom to yourself. Gain energy.
    //   Upgraded Effect: Apply 3 Doom to yourself. Gain 2 energy.
    // TODO CHANGED: Borrowed Time (Uncommon) - 1 energy, Skill
    //   Effect: Gain 4 energy. Cards cost an additional energy this turn.
    //   Upgraded Effect: Gain 6 energy. Cards cost an additional energy this turn.
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Bury (Uncommon) - 4 energy, Attack
    //   Effect: Deal 52 damage.
    //   Upgraded Effect: Deal 63 damage.
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

    // Calcify (Uncommon) - 1 energy, Power
    //   Effect: Osty's attacks deal 4 additional damage.
    //   Upgraded Effect: Osty's attacks deal 6 additional damage.
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

    // Capture Spirit (Uncommon) - 1 energy, Skill
    //   Effect: Enemy loses 3 HP. Add 3 Souls into your Draw Pile.
    //   Upgraded Effect: Enemy loses 4 HP. Add 4 Souls into your Draw Pile.
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

    // Cleanse (Uncommon) - 1 energy, Skill
    //   Effect: Summon 3. Exhaust 1 card from your Draw Pile.
    //   Upgraded Effect: Summon 5. Exhaust 1 card from your Draw Pile.
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

    // Countdown (Uncommon) - 1 energy, Power
    //   Effect: At the start of your turn, apply 6 Doom to a random enemy.
    //   Upgraded Effect: At the start of your turn, apply 9 Doom to a random enemy.
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

    // Danse Macabre (Uncommon) - 1 energy, Power
    //   Effect: Whenever you play a card that costs 2 energy or more, gain 3 Block.
    //   Upgraded Effect: Whenever you play a card that costs 2 energy or more, gain 4 Block.
    // TODO CHANGED: Danse Macabre (Uncommon) - 1 energy, Power
    //   Effect: Whenever you play a card that costs 2 energy or more, gain 4 Block.
    //   Upgraded Effect: Whenever you play a card that costs 2 energy or more, gain 6 Block.
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage + bonus * drawn, this);
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

    // Death March (Uncommon) - 1 energy, Attack
    //   Effect: Deal 8 damage. Deals 3 additional damage for each card drawn during your turn.
    //   Upgraded Effect: Deal 9 damage. Deals 4 additional damage for each card drawn during your turn.
    // TODO CHANGED: Death March (Uncommon) - 1 energy, Attack
    //   Effect: Deal 8 damage. Deals 4 additional damage for each card drawn during your turn.
    //   Upgraded Effect: Deal 9 damage. Deals 6 additional damage for each card drawn during your turn.
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

    // Death's Door (Uncommon) - 1 energy, Skill
    //   Effect: Gain 6 Block. If you applied Doom this turn, gain Block 2 additional times.
    //   Upgraded Effect: Gain 7 Block. If you applied Doom this turn, gain Block 2 additional times.
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

    // Deathbringer (Uncommon) - 2 energy, Skill
    //   Effect: Apply 21 Doom and 1 Weak to ALL enemies.
    //   Upgraded Effect: Apply 26 Doom and 1 Weak to ALL enemies.
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
            state.playerDoDamageToEnemy(enemy, damage, this);
            enemy.applyDebuff(state, DebuffType.DEBILITATE, turns);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Debilitate (Uncommon) - 1 energy, Attack
    //   Effect: Deal 7 damage. Vulnerable and Weak are twice as effective against the enemy for the next 3 turns.
    //   Upgraded Effect: Deal 9 damage. Vulnerable and Weak are twice as effective against the enemy for the next 4 turns.
    // TODO CHANGED: Debilitate (Uncommon) - 1 energy, Attack
    //   Effect: Deal 10 damage. Vulnerable and Weak are twice as effective against the enemy for the next 2 turns.
    //   Upgraded Effect: Deal 12 damage. Vulnerable and Weak are twice as effective against the enemy for the next 3 turns.
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

    // Delay (Uncommon) - 2 energy, Skill
    //   Effect: Gain 11 Block. Next turn, gain energy.
    //   Upgraded Effect: Gain 13 Block. Next turn, gain 2 energy.
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

    // Dirge (Uncommon) - X energy, Skill
    //   Effect: Summon 3 X times. Add X Souls into your Draw Pile.
    //   Upgraded Effect: Summon 4 X times. Add X Souls+ into your Draw Pile.
    // TODO CHANGED: Dirge (Uncommon) - X energy, Skill
    //   Effect: Summon 3 X times. Add X Souls into your Draw Pile. Exhaust.
    //   Upgraded Effect: Summon 4 X times. Add X Souls+ into your Draw Pile. Exhaust.
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

    // Dredge (Uncommon) - 1 energy, Skill
    //   Effect: [etain. ]Put 3 cards from your Discard Pile into your Hand. Exhaust.
    // TODO CHANGED: Dredge (Uncommon) - 1 energy, Skill
    //   Effect: Put 3 cards from your Discard Pile into your Hand. Exhaust.
    //   Upgraded Effect: Retain. Put 3 cards from your Discard Pile into your Hand. Exhaust.
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

    // Enfeebling Touch (Uncommon) - 1 energy, Skill
    //   Effect: Ethereal. Enemy loses 8 Strength this turn.
    //   Upgraded Effect: Ethereal. Enemy loses 11 Strength this turn.
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

    private static abstract class _FetchT extends Card {
        private final int damage;

        public _FetchT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            entityProperty.otsyAttack = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.otsyDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop, int cardIdx) {
            return generatedCardIdx;
        }
    }

    // Fetch (Uncommon) - 0 energy, Attack
    //   Effect: Osty deals 3 damage. If this is the first time this card has been played this turn, draw 1 card.
    //   Upgraded Effect: Osty deals 6 damage. If this is the first time this card has been played this turn, draw 1 card.
    public static class Fetch extends _FetchT {
        public Fetch() { super("Fetch", 3); }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new FetchUsed());
        }
    }

    public static class FetchP extends _FetchT {
        public FetchP() { super("Fetch+", 6); }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new FetchUsedP());
        }
    }

    private static abstract class _FetchUsedT extends Card {
        private final int damage;

        public _FetchUsedT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            entityProperty.otsyAttack = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.otsyDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            int[] revertTransform = new int[state.properties.cardDict.length];
            Arrays.fill(revertTransform, -1);
            for (int i = 0; i < state.properties.cardDict.length; i++) {
                var base = state.properties.cardDict[i].getBaseCard();
                if (base instanceof FetchUsed) {
                    revertTransform[i] = state.properties.findCardIndex(new Fetch());
                } else if (base instanceof FetchUsedP) {
                    revertTransform[i] = state.properties.findCardIndex(new FetchP());
                }
            }
            state.properties.addEndOfTurnHandler("FetchUsedRevert", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.handArrTransform(revertTransform);
                    state.deckArrTransform(revertTransform);
                    state.discardArrTransform(revertTransform);
                }
            });
        }
    }

    public static class FetchUsed extends _FetchUsedT {
        public FetchUsed() { super("Fetch (Used)", 3); }
    }

    public static class FetchUsedP extends _FetchUsedT {
        public FetchUsedP() { super("Fetch+ (Used)", 6); }
    }

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

    // Friendship (Uncommon) - 1 energy, Power
    //   Effect: Lose 2 Strength. Gain energy at the start of each turn.
    //   Upgraded Effect: Lose 1 Strength. Gain energy at the start of each turn.
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

    // Haunt (Uncommon) - 1 energy, Power
    //   Effect: Whenever you play a Soul, a random enemy loses 6 HP.
    //   Upgraded Effect: Whenever you play a Soul, a random enemy loses 8 HP.
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
            entityProperty.otsyAttack = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.otsyDoDamageToEnemy(enemy, damage);
                enemy.applyDebuff(state, DebuffType.VULNERABLE, vuln);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // High Five (Uncommon) - 2 energy, Attack
    //   Effect: Osty deals 11 damage and applies 2 Vulnerable to ALL enemies.
    //   Upgraded Effect: Osty deals 13 damage and applies 3 Vulnerable to ALL enemies.
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

    // Lethality (Uncommon) - 1 energy, Power
    //   Effect: Ethereal. The first Attack each turn deals 50% additional damage.
    //   Upgraded Effect: Ethereal. The first Attack each turn deals 75% additional damage.
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

    // Melancholy (Uncommon) - 3 energy, Skill
    //   Effect: Gain 13 Block. Reduce this card's cost by energy whenever ANYONE dies.
    //   Upgraded Effect: Gain 17 Block. Reduce this card's cost by energy whenever ANYONE dies.
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

    // No Escape (Uncommon) - 1 energy, Skill
    //   Effect: Apply 10 Doom, plus an additional 5 Doom for every 10 Doom already on this enemy.
    //   Upgraded Effect: Apply 15 Doom, plus an additional 5 Doom for every 10 Doom already on this enemy.
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
                    if (stacks > 0 && state.properties.cardDict[cardIdx].ethereal()) {
                        state.draw(stacks);
                    }
                }
            });
        }
    }

    // Pagestorm (Uncommon) - 1 energy, Power
    //   Effect: Whenever you draw an Ethereal card, draw 1 card.
    //   Upgraded Effect (0 energy): Whenever you draw an Ethereal card, draw 1 card.
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

    // Parse (Uncommon) - 1 energy, Skill
    //   Effect: Ethereal. Draw 3 cards.
    //   Upgraded Effect: Ethereal. Draw 4 cards.
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmgPerCard * count, this);
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
                    if (state.properties.cardDict[cardIdx].ethereal()) {
                        state.getCounterForWrite()[etherealCountRegistrant.getCounterIdx(state.properties)]++;
                    }
                }
            });
        }
    }

    // Pull from Below (Uncommon) - 1 energy, Attack
    //   Effect: Deal 5 damage for each Ethereal card played this combat.
    //   Upgraded Effect: Deal 7 damage for each Ethereal card played this combat.
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

    // Putrefy (Uncommon) - 1 energy, Skill
    //   Effect: Apply 2 Weak. Apply 2 Vulnerable. Exhaust.
    //   Upgraded Effect: Apply 3 Weak. Apply 3 Vulnerable. Exhaust.
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

    private static abstract class _RattleT extends Card {
        private final int damage;

        public _RattleT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            entityProperty.otsyAttack = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            int priorAttacks = state.properties.otsyAttackedThisTurnCounterIdx >= 0 ?
                    state.getCounterForRead()[state.properties.otsyAttackedThisTurnCounterIdx] : 0;
            for (int i = 0; i <= priorAttacks; i++) {
                state.otsyDoDamageToEnemy(enemy, damage);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerOtsyAttackedThisTurnCounter();
        }
    }

    // Rattle (Uncommon) - 1 energy, Attack
    //   Effect: Osty deals 7 damage. Hits an additional time for each other time he has attacked this turn.
    //   Upgraded Effect: Osty deals 9 damage. Hits an additional time for each other time he has attacked this turn.
    public static class Rattle extends _RattleT {
        public Rattle() {
            super("Rattle", 7);
        }
    }

    public static class RattleP extends _RattleT {
        public RattleP() {
            super("Rattle+", 9);
        }
    }

    private static abstract class _RightHandHandT extends Card {
        private final int damage;

        public _RightHandHandT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            entityProperty.otsyAttack = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.otsyDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnCardPlayedHandler("RightHandHand", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (energyUsed >= 2) {
                        for (int i = 0; i < state.discardArrLen && state.handArrLen < GameState.HAND_LIMIT; i++) {
                            int cIdx = state.getDiscardArrForRead()[i];
                            if (state.properties.cardDict[cIdx].getBaseCard() instanceof _RightHandHandT) {
                                state.removeCardFromDiscardByPosition(i);
                                i--;
                                state.addCardToHand(cIdx);
                            }
                        }
                    }
                }
            });
        }
    }

    // Right Hand Hand (Uncommon) - 0 energy, Attack
    //   Effect: Osty deals 4 damage. Whenever you play a card that costs 2 energy or more, return this to your Hand from the Discard Pile.
    //   Upgraded Effect: Osty deals 6 damage. Whenever you play a card that costs 2 energy or more, return this to your Hand from the Discard Pile.
    public static class RightHandHand extends _RightHandHandT {
        public RightHandHand() {
            super("Right Hand Hand", 4);
        }
    }

    public static class RightHandHandP extends _RightHandHandT {
        public RightHandHandP() {
            super("Right Hand Hand+", 6);
        }
    }

    private static abstract class _SeveranceT extends Card {
        private final int damage;

        public _SeveranceT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            state.addCardToDeck(generatedCardIdx);
            state.addCardToHand(generatedCardIdx);
            state.addCardToDiscard(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Severance (Uncommon) - 2 energy, Attack
    //   Effect: Deal 13 damage. Add a Soul into your Draw Pile, Hand, and Discard Pile.
    //   Upgraded Effect: Deal 18 damage. Add a Soul into your Draw Pile, Hand, and Discard Pile.
    public static class Severance extends _SeveranceT {
        public Severance() {
            super("Severance", 13);
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.Soul());
        }
    }

    public static class SeveranceP extends _SeveranceT {
        public SeveranceP() {
            super("Severance+", 18);
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.SoulP());
        }
    }

    private static abstract class _ShroudT extends Card implements GameProperties.CounterRegistrant {
        private final int block;

        public _ShroudT(String cardName, int block) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += block;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Shroud", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnEnemyDebuffHandler("Shroud", new GameEventEnemyDebuffHandler() {
                @Override public void handle(GameState state, EnemyReadOnly enemy, DebuffType type, int amount) {
                    if (type == DebuffType.DOOM) {
                        int blockAmt = state.getCounterForRead()[counterIdx];
                        if (blockAmt > 0) {
                            state.playerGainBlockNotFromCardPlay(blockAmt);
                        }
                    }
                }
            });
        }
    }

    // Shroud (Uncommon) - 1 energy, Power
    //   Effect: Whenever you apply Doom, gain 2 Block.
    //   Upgraded Effect: Whenever you apply Doom, gain 3 Block.
    public static class Shroud extends _ShroudT {
        public Shroud() {
            super("Shroud", 2);
        }
    }

    public static class ShroudP extends _ShroudT {
        public ShroudP() {
            super("Shroud+", 3);
        }
    }

    private static abstract class _SicEmT extends Card {
        private final int damage;
        private final int summon;

        public _SicEmT(String cardName, int damage, int summon) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
            this.summon = summon;
            entityProperty.selectEnemy = true;
            entityProperty.sicEmEnemy = true;
            entityProperty.canSummon = true;
            entityProperty.otsyAttack = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.otsyDoDamageToEnemy(enemy, damage);
            enemy.applyDebuff(state, DebuffType.SIC_EM, summon);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("SicEm", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        enemy.setSicEm(0);
                    }
                }
            });
        }
    }

    // Sic 'Em (Uncommon) - 1 energy, Attack
    //   Effect: Osty deals 5 damage. Whenever Osty hits this enemy this turn, Summon 2.
    //   Upgraded Effect: Osty deals 6 damage. Whenever Osty hits this enemy this turn, Summon 3.
    // TODO CHANGED: Sic 'Em (Uncommon) - 1 energy, Attack
    //   Effect: Osty deals 5 damage. Whenever Osty hits this enemy this turn, Summon 3.
    //   Upgraded Effect: Osty deals 6 damage. Whenever Osty hits this enemy this turn, Summon 4.
    public static class SicEm extends _SicEmT {
        public SicEm() {
            super("Sic 'Em", 5, 2);
        }
    }

    public static class SicEmP extends _SicEmT {
        public SicEmP() {
            super("Sic 'Em+", 6, 3);
        }
    }

    // Sleight of Flesh (Uncommon) - 2 energy, Power
    //   Effect: Whenever you apply a debuff to an enemy, they take 9 damage.
    //   Upgraded Effect: Whenever you apply a debuff to an enemy, they take 13 damage.
    public static class SleightOfFlesh extends CardColorless._SadisticNatureT {
        public SleightOfFlesh() {
            super("Sleight of Flesh", 2, Card.UNCOMMON, 9);
        }
    }

    public static class SleightOfFleshP extends CardColorless._SadisticNatureT {
        public SleightOfFleshP() {
            super("Sleight of Flesh+", 2, Card.UNCOMMON, 13);
        }
    }

    private static abstract class _SpurT extends Card {
        private final int summon;
        private final int heal;

        public _SpurT(String cardName, int summon, int heal) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.summon = summon;
            this.heal = heal;
            this.retain = true;
            entityProperty.canSummon = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.summon(summon);
            if (state.properties.otsyHPCounterIdx >= 0) {
                int hp = state.getCounterForRead()[state.properties.otsyHPCounterIdx];
                int maxHp = state.getCounterForRead()[state.properties.otsyMaxHPCounterIdx];
                state.getCounterForWrite()[state.properties.otsyHPCounterIdx] = Math.min(hp + heal, maxHp);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Spur (Uncommon) - 1 energy, Skill
    //   Effect: Retain. Summon 3. Osty heals 5 HP.
    //   Upgraded Effect: Retain. Summon 5. Osty heals 7 HP.
    public static class Spur extends _SpurT {
        public Spur() {
            super("Spur", 3, 5);
        }
    }

    public static class SpurP extends _SpurT {
        public SpurP() {
            super("Spur+", 5, 7);
        }
    }

    private static abstract class _VeilpiercerT extends Card {
        private final int damage;

        public _VeilpiercerT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("NextEtherealCostZero", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] > 0 ? 1.0f : 0.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("NextEtherealCostZero", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].ethereal() && state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx]--;
                    }
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.nextEtherealCostZeroCounterIdx = idx;
        }
    }

    // Veilpiercer (Uncommon) - 1 energy, Attack
    //   Effect: Deal 10 damage. The next Ethereal card you play costs 0 energy.
    //   Upgraded Effect: Deal 13 damage. The next Ethereal card you play costs 0 energy.
    public static class Veilpiercer extends _VeilpiercerT {
        public Veilpiercer() {
            super("Veilpiercer", 10);
        }
    }

    public static class VeilpiercerP extends _VeilpiercerT {
        public VeilpiercerP() {
            super("Veilpiercer+", 13);
        }
    }

    // **************************************************************************************************
    // *********************************************  Rare  *********************************************
    // **************************************************************************************************

    private static abstract class _BansheeCryT extends Card {
        private final int damage;

        public _BansheeCryT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 6, Card.RARE);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, damage, this);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public int energyCost(GameState state) {
            if (state == null) return 6;
            return Math.max(0, 6 - 2 * state.getCounterForRead()[counterIdx]);
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("EtherealCardsPlayedThisCombat", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("EtherealCardsPlayedThisCombat", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].ethereal()) {
                        state.getCounterForWrite()[counterIdx]++;
                    }
                }
            });
        }
    }

    public static class BansheeCry extends _BansheeCryT {
        public BansheeCry() {
            super("Banshee's Cry", 33);
        }
    }

    public static class BansheeCryP extends _BansheeCryT {
        public BansheeCryP() {
            super("Banshee's Cry+", 39);
        }
    }

    private static abstract class _CallOfTheVoidT extends Card {
        public _CallOfTheVoidT(String cardName, boolean innate) {
            super(cardName, Card.POWER, 1, Card.RARE);
            this.innate = innate;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return CardManager.getCharacterCards(CharacterEnum.NECROBINDER, false).stream()
                    .map(Card::getPermEthereal)
                    .toList();
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("CallOfTheVoid", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.setIsStochastic();
                    int randomIdx = state.getSearchRandomGen().nextInt(generatedCardIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, generatedCardIdxes));
                    state.addCardToHand(generatedCardIdxes[randomIdx]);
                }
            });
        }
    }

    // Call of the Void (Rare) - 1 energy, Power
    //   Effect: At the start of your turn, add 1 random card into your Hand. It gains Ethereal.
    //   Upgraded Effect: Innate. At the start of your turn, add 1 random card into your Hand. It gains Ethereal.
    public static class CallOfTheVoid extends _CallOfTheVoidT {
        public CallOfTheVoid() { super("Call of the Void", false); }
    }

    public static class CallOfTheVoidP extends _CallOfTheVoidT {
        public CallOfTheVoidP() { super("Call of the Void+", true); }
    }

    private static abstract class _DemesneT extends Card {
        public _DemesneT(String cardName, int cost) {
            super(cardName, Card.POWER, cost, Card.RARE);
            this.ethereal = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Demesne", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Demesne", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int stacks = state.getCounterForRead()[counterIdx];
                    if (stacks > 0) {
                        state.gainEnergy(stacks);
                        state.draw(stacks);
                    }
                }
            });
        }
    }

    // Demesne (Rare) - 3 energy, Power
    //   Effect: Ethereal. At the start of your turn, gain energy and draw 1 additional card.
    //   Upgraded Effect (2 energy): Ethereal. At the start of your turn, gain energy and draw 1 additional card.
    public static class Demesne extends _DemesneT {
        public Demesne() {
            super("Demesne", 3);
        }
    }

    public static class DemesneP extends _DemesneT {
        public DemesneP() {
            super("Demesne+", 2);
        }
    }

    private static abstract class _DevourLifeT extends Card {
        private final int n;

        public _DevourLifeT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.RARE);
            this.n = n;
            entityProperty.canSummon = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("DevourLife", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("DevourLife", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    var base = state.properties.cardDict[cardIdx].getBaseCard();
                    if (base instanceof CardColorless2.Soul || base instanceof CardColorless2.SoulP) {
                        int summonAmt = state.getCounterForRead()[counterIdx];
                        if (summonAmt > 0) {
                            state.summon(summonAmt);
                        }
                    }
                }
            });
        }
    }

    // Devour Life (Rare) - 1 energy, Power
    //   Effect: Whenever you play a Soul, Summon 1.
    //   Upgraded Effect: Whenever you play a Soul, Summon 2.
    public static class DevourLife extends _DevourLifeT {
        public DevourLife() {
            super("Devour Life", 1);
        }
    }

    public static class DevourLifeP extends _DevourLifeT {
        public DevourLifeP() {
            super("Devour Life+", 2);
        }
    }

    private static abstract class _EidolonT extends Card {
        public _EidolonT(String cardName, int cost) {
            super(cardName, Card.SKILL, cost, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int handSize = state.handArrLen;
            for (int i = handSize - 1; i >= 0; i--) {
                state.exhaustCardFromHandByPosition(i, false);
            }
            state.updateHandArr();
            if (handSize >= 9) {
                state.getCounterForWrite()[state.properties.intangibleCounterIdx]++;
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerIntangibleCounter();
        }
    }

    // Eidolon (Rare) - 2 energy, Skill
    //   Effect: Exhaust your Hand. If 9 cards were Exhausted this way, gain 1 Intangible.
    //   Upgraded Effect (1 energy): Exhaust your Hand. If 9 cards were Exhausted this way, gain 1 Intangible.
    public static class Eidolon extends _EidolonT {
        public Eidolon() {
            super("Eidolon", 2);
        }
    }

    public static class EidolonP extends _EidolonT {
        public EidolonP() {
            super("Eidolon+", 1);
        }
    }

    private static abstract class _EndOfDaysT extends Card {
        private final int doom;

        public _EndOfDaysT(String cardName, int doom) {
            super(cardName, Card.SKILL, 3, Card.RARE);
            this.doom = doom;
            entityProperty.doomEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.DOOM, doom);
            }
            for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                var enemy = state.getEnemiesForRead().get(i);
                if (enemy.isAlive() && enemy.getDoom() > 0 && enemy.getHealth() <= enemy.getDoom()) {
                    state.killEnemy(i, true);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // End of Days (Rare) - 3 energy, Skill
    //   Effect: Apply 29 Doom to ALL enemies. Kill enemies with at least as much Doom as HP.
    //   Upgraded Effect: Apply 37 Doom to ALL enemies. Kill enemies with at least as much Doom as HP.
    public static class EndOfDays extends _EndOfDaysT {
        public EndOfDays() {
            super("End of Days", 29);
        }
    }

    public static class EndOfDaysP extends _EndOfDaysT {
        public EndOfDaysP() {
            super("End of Days+", 37);
        }
    }

    private static abstract class _EradicateT extends Card {
        private final int damage;

        public _EradicateT(String cardName, int damage) {
            super(cardName, Card.ATTACK, -1, Card.RARE);
            this.damage = damage;
            this.isXCost = true;
            this.retain = true;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            for (int i = 0; i < energyUsed; i++) {
                state.playerDoDamageToEnemy(enemy, damage, this);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public int energyCost(GameState state) {
            return state.energy;
        }
    }

    // Eradicate (Rare) - X energy, Attack
    //   Effect: Retain. Deal 11 damage X times.
    //   Upgraded Effect: Retain. Deal 14 damage X times.
    public static class Eradicate extends _EradicateT {
        public Eradicate() {
            super("Eradicate", 11);
        }
    }

    public static class EradicateP extends _EradicateT {
        public EradicateP() {
            super("Eradicate+", 14);
        }
    }

    // No need to implement Glimpse Beyond: Multiplayer

    private static abstract class _HangT extends Card {
        private final int damage;

        public _HangT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.RARE);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            entityProperty.hangEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            int scaledDamage = damage * (1 << enemy.getHang());
            state.playerDoDamageToEnemy(enemy, scaledDamage, this);
            enemy.applyDebuff(state, DebuffType.HANG, 1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Hang (Rare) - 1 energy, Attack
    //   Effect: Deal 10 damage. Double the damage ALL Hang cards deal to this enemy.
    //   Upgraded Effect: Deal 13 damage. Double the damage ALL Hang cards deal to this enemy.
    public static class Hang extends _HangT {
        public Hang() {
            super("Hang", 10);
        }
    }

    public static class HangP extends _HangT {
        public HangP() {
            super("Hang+", 13);
        }
    }

    private static abstract class _MiseryT extends Card {
        private final int damage;

        public _MiseryT(String cardName, int damage, boolean retain) {
            super(cardName, Card.ATTACK, 0, Card.RARE);
            this.damage = damage;
            this.retain = retain;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var target = state.getEnemiesForRead().get(idx);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                if (i == idx || !state.getEnemiesForRead().get(i).isAlive()) continue;
                state.getEnemiesForWrite().getForWrite(i).applyDebuffsFrom(state, target);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Misery (Rare) - 0 energy, Attack
    //   Effect: Deal 7 damage. Apply any debuffs on the enemy to ALL other enemies.
    //   Upgraded Effect: Retain. Deal 9 damage. Apply any debuffs on the enemy to ALL other enemies.
    public static class Misery extends _MiseryT {
        public Misery() {
            super("Misery", 7, false);
        }
    }

    public static class MiseryP extends _MiseryT {
        public MiseryP() {
            super("Misery+", 9, true);
        }
    }

    private static abstract class _NecroMasteryT extends Card {
        private final int summonAmt;

        public _NecroMasteryT(String cardName, int summonAmt) {
            super(cardName, Card.POWER, 2, Card.RARE);
            this.summonAmt = summonAmt;
            entityProperty.canSummon = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.summon(summonAmt);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnOtsyLosesHPHandler("NecroMastery", new OnOtsyDamageHandler() {
                @Override public void handle(GameState state, int amount) {
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        state.playerDoNonAttackDamageToEnemy(enemy, amount, false);
                    }
                }
            });
        }
    }

    // Necro Mastery (Rare) - 2 energy, Power
    //   Effect: Summon 5. Whenever Osty loses HP, ALL enemies lose that much HP as well.
    //   Upgraded Effect: Summon 8. Whenever Osty loses HP, ALL enemies lose that much HP as well.
    public static class NecroMastery extends _NecroMasteryT {
        public NecroMastery() {
            super("Necro Mastery", 5);
        }
    }

    public static class NecroMasteryP extends _NecroMasteryT {
        public NecroMasteryP() {
            super("Necro Mastery+", 8);
        }
    }

    private static abstract class _NeurosurgeT extends Card {
        private final int energy;

        public _NeurosurgeT(String cardName, int energy) {
            super(cardName, Card.POWER, 0, Card.RARE);
            this.energy = energy;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(energy);
            state.draw(2);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.DOOM_PER_TURN, 3);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerPlayerDoomCounter();
            state.properties.registerCounter("NeurosurgeDoomPerTurn", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("NeurosurgeDoomPerTurn", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int doom = state.getCounterForRead()[counterIdx];
                    if (doom > 0) {
                        state.getPlayerForWrite().applyDebuff(state, DebuffType.DOOM, doom);
                    }
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.doomPerTurnCounterIdx = idx;
        }
    }

    // Neurosurge (Rare) - 0 energy, Power
    //   Effect: Gain 3 energy. Draw 2 cards. At the start of your turn, apply 3 Doom to yourself.
    //   Upgraded Effect: Gain 4 energy. Draw 2 cards. At the start of your turn, apply 3 Doom to yourself.
    public static class Neurosurge extends _NeurosurgeT {
        public Neurosurge() {
            super("Neurosurge", 3);
        }
    }

    public static class NeurosurgeP extends _NeurosurgeT {
        public NeurosurgeP() {
            super("Neurosurge+", 4);
        }
    }

    private static abstract class _OblivionT extends Card {
        private final int doomPerCard;

        public _OblivionT(String cardName, int doomPerCard) {
            super(cardName, Card.SKILL, 0, Card.RARE);
            this.doomPerCard = doomPerCard;
            entityProperty.selectEnemy = true;
            entityProperty.doomEnemy = true;
            entityProperty.doomPerCardEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.DOOM_PER_CARD, doomPerCard);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnCardPlayedHandler("OblivionDoomPerCard", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                        var enemy = state.getEnemiesForRead().get(i);
                        if (enemy.isAlive() && enemy.getDoomPerCard() > 0) {
                            state.getEnemiesForWrite().getForWrite(i).applyDebuff(state, DebuffType.DOOM, enemy.getDoomPerCard());
                        }
                    }
                }
            });
        }
    }

    // Oblivion (Rare) - 0 energy, Skill
    //   Effect: Whenever you play a card this turn, apply 3 Doom to the enemy.
    //   Upgraded Effect: Whenever you play a card this turn, apply 4 Doom to the enemy.
    public static class Oblivion extends _OblivionT {
        public Oblivion() {
            super("Oblivion", 3);
        }
    }

    public static class OblivionP extends _OblivionT {
        public OblivionP() {
            super("Oblivion+", 4);
        }
    }

    private static abstract class _ReanimateT extends Card {
        private final int summonAmt;

        public _ReanimateT(String cardName, int summonAmt) {
            super(cardName, Card.SKILL, 3, Card.RARE);
            this.summonAmt = summonAmt;
            this.exhaustWhenPlayed = true;
            entityProperty.canSummon = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.summon(summonAmt);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Reanimate (Rare) - 3 energy, Skill
    //   Effect: Summon 20. Exhaust.
    //   Upgraded Effect: Summon 25. Exhaust.
    public static class Reanimate extends _ReanimateT {
        public Reanimate() {
            super("Reanimate", 20);
        }
    }

    public static class ReanimateP extends _ReanimateT {
        public ReanimateP() {
            super("Reanimate+", 25);
        }
    }

    private static abstract class _ReaperFormT extends Card {
        public _ReaperFormT(String cardName, boolean retain) {
            super(cardName, Card.POWER, 3, Card.RARE);
            this.retain = retain;
            entityProperty.possibleBuffs |= PlayerBuff.REAPER_FORM.mask();
            entityProperty.doomEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.buffs |= PlayerBuff.REAPER_FORM.mask();
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Reaper Form (Rare) - 3 energy, Power
    //   Effect: Whenever Attacks deal damage, they also apply that much Doom.
    //   Upgraded Effect: Retain. Whenever Attacks deal damage, they also apply that much Doom.
    public static class ReaperForm extends _ReaperFormT {
        public ReaperForm() {
            super("Reaper Form", false);
        }
    }

    public static class ReaperFormP extends _ReaperFormT {
        public ReaperFormP() {
            super("Reaper Form+", true);
        }
    }

    private static abstract class _SacrificeT extends Card {
        public _SacrificeT(String cardName, int cost) {
            super(cardName, Card.SKILL, cost, Card.RARE);
            this.retain = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.properties.otsyHPCounterIdx >= 0) {
                int otsyHP = state.getCounterForRead()[state.properties.otsyHPCounterIdx];
                if (otsyHP > 0) {
                    int maxHP = state.getCounterForRead()[state.properties.otsyMaxHPCounterIdx];
                    state.dealDamageToOtsy(otsyHP);
                    state.playerGainBlock(2 * maxHP);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Sacrifice (Rare) - 1 energy, Skill
    //   Effect: Retain. If Osty is alive, he dies and you gain Block equal to double his Max HP.
    //   Upgraded Effect (0 energy): Retain. If Osty is alive, he dies and you gain Block equal to double his Max HP.
    public static class Sacrifice extends _SacrificeT {
        public Sacrifice() {
            super("Sacrifice", 1);
        }
    }

    public static class SacrificeP extends _SacrificeT {
        public SacrificeP() {
            super("Sacrifice+", 0);
        }
    }

    private static abstract class _SeanceT extends Card {
        public _SeanceT(String cardName) {
            super(cardName, Card.SKILL, 0, Card.RARE);
            this.ethereal = true;
            entityProperty.selectFromDeck = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < state.deckArrLen; i++) {
                if (state.deckArr[i] == idx) {
                    state.transformCard(state.deckArr, i, generatedCardIdx);
                    break;
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Seance (Rare) - 0 energy, Skill
    //   Effect: Ethereal. Transform a card in your Draw Pile into Soul.
    //   Upgraded Effect: Ethereal. Transform a card in your Draw Pile into Soul+.
    // TODO CHANGED: Seance (Rare) - 1 energy, Skill
    //   Effect: Ethereal. Transform a card in your Draw Pile into Soul.
    //   Upgraded Effect (0 energy): Ethereal. Transform a card in your Draw Pile into Soul.
    public static class Seance extends _SeanceT {
        public Seance() {
            super("Seance");
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.Soul());
        }
    }

    public static class SeanceP extends _SeanceT {
        public SeanceP() {
            super("Seance+");
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.SoulP());
        }
    }

    private static abstract class _SentryModeT extends Card {
        public _SentryModeT(String cardName, int cost) {
            super(cardName, Card.POWER, cost, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("SentryMode", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.addCardToHand(generatedCardIdx);
                }
            });
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.SweepingGaze());
        }
    }

    // Sentry Mode (Rare) - 2 energy, Power
    //   Effect: At the start of your turn, add 1 Sweeping Gaze into your Hand.
    //   Upgraded Effect (1 energy): At the start of your turn, add 1 Sweeping Gaze into your Hand.
    public static class SentryMode extends _SentryModeT {
        public SentryMode() {
            super("Sentry Mode", 2);
        }
    }

    public static class SentryModeP extends _SentryModeT {
        public SentryModeP() {
            super("Sentry Mode+", 1);
        }
    }

    private static abstract class _SharedFateT extends Card {
        private final int enemyStrLoss;

        public _SharedFateT(String cardName, int enemyStrLoss) {
            super(cardName, Card.SKILL, 0, Card.RARE);
            this.enemyStrLoss = enemyStrLoss;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainStrength(-2);
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.LOSE_STRENGTH, enemyStrLoss);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Shared Fate (Rare) - 0 energy, Skill
    //   Effect: Lose 2 Strength. Enemy loses 2 Strength. Exhaust.
    //   Upgraded Effect: Lose 2 Strength. Enemy loses 3 Strength. Exhaust.
    public static class SharedFate extends _SharedFateT {
        public SharedFate() {
            super("Shared Fate", 2);
        }
    }

    public static class SharedFateP extends _SharedFateT {
        public SharedFateP() {
            super("Shared Fate+", 3);
        }
    }

    private static abstract class _SoulStormT extends Card {
        private final int baseDamage;
        private final int dmgPerSoul;

        public _SoulStormT(String cardName, int baseDamage, int dmgPerSoul) {
            super(cardName, Card.ATTACK, 1, Card.RARE);
            this.baseDamage = baseDamage;
            this.dmgPerSoul = dmgPerSoul;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int soulCount = 0;
            for (int i = 0; i < state.exhaustArrLen; i++) {
                var base = state.properties.cardDict[state.exhaustArr[i]].getBaseCard();
                if (base instanceof CardColorless2.Soul || base instanceof CardColorless2.SoulP) {
                    soulCount++;
                }
            }
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), baseDamage + dmgPerSoul * soulCount, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Soul Storm (Rare) - 1 energy, Attack
    //   Effect: Deal 9 damage. Deals 2 additional damage for each Soul in your Exhaust Pile.
    //   Upgraded Effect: Deal 9 damage. Deals 3 additional damage for each Soul in your Exhaust Pile.
    public static class SoulStorm extends _SoulStormT {
        public SoulStorm() {
            super("Soul Storm", 9, 2);
        }
    }

    public static class SoulStormP extends _SoulStormT {
        public SoulStormP() {
            super("Soul Storm+", 9, 3);
        }
    }

    private static abstract class _SpiritOfAshT extends Card {
        private final int blockPerEthereal;

        public _SpiritOfAshT(String cardName, int blockPerEthereal) {
            super(cardName, Card.POWER, 1, Card.RARE);
            this.blockPerEthereal = blockPerEthereal;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += blockPerEthereal;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("SpiritOfAsh", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("SpiritOfAsh", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int block = state.getCounterForRead()[counterIdx];
                    if (block > 0 && state.properties.cardDict[cardIdx].ethereal()) {
                        state.playerGainBlockNotFromCardPlay(block);
                    }
                }
            });
        }
    }

    // Spirit of Ash (Rare) - 1 energy, Power
    //   Effect: Whenever you play an Ethereal card, gain 4 Block.
    //   Upgraded Effect: Whenever you play an Ethereal card, gain 5 Block.
    public static class SpiritOfAsh extends _SpiritOfAshT {
        public SpiritOfAsh() {
            super("Spirit of Ash", 4);
        }
    }

    public static class SpiritOfAshP extends _SpiritOfAshT {
        public SpiritOfAshP() {
            super("Spirit of Ash+", 5);
        }
    }

    private static abstract class _SqueezeT extends Card {
        private final int damage;
        private final int bonusPerOtsy;
        private boolean[] isOtsyAttack;

        public _SqueezeT(String cardName, int damage, int bonusPerOtsy) {
            super(cardName, Card.ATTACK, 3, Card.RARE);
            this.damage = damage;
            this.bonusPerOtsy = bonusPerOtsy;
            entityProperty.selectEnemy = true;
            entityProperty.otsyAttack = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int count = 0;
            for (int i = 0; i < state.handArrLen; i++) {
                if (isOtsyAttack[state.handArr[i]]) count++;
            }
            for (int i = 0; i < state.deckArrLen; i++) {
                if (isOtsyAttack[state.deckArr[i]]) count++;
            }
            for (int i = 0; i < state.discardArrLen; i++) {
                if (isOtsyAttack[state.discardArr[i]]) count++;
            }
            count--;
            state.otsyDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage + count * bonusPerOtsy);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            isOtsyAttack = new boolean[state.properties.cardDict.length];
            for (int i = 0; i < state.properties.cardDict.length; i++) {
                isOtsyAttack[i] = state.properties.cardDict[i].entityProperty.otsyAttack;
            }
        }
    }

    // Squeeze (Rare) - 3 energy, Attack
    //   Effect: Osty deals 25 damage. Deals 5 additional damage for ALL your other Osty Attacks.
    //   Upgraded Effect: Osty deals 30 damage. Deals 6 additional damage for ALL your other Osty Attacks.
    public static class Squeeze extends _SqueezeT {
        public Squeeze() { super("Squeeze", 25, 5); }
    }

    public static class SqueezeP extends _SqueezeT {
        public SqueezeP() { super("Squeeze+", 30, 6); }
    }

    private static abstract class _TheScytheT extends Card {
        protected final int n;
        private final int dmgInc;
        protected final int healthRewardRatio;

        public _TheScytheT(String cardName, int n, int dmgInc, int healthRewardRatio) {
            super(cardName, Card.ATTACK, 2, Card.RARE);
            this.n = n;
            this.dmgInc = dmgInc;
            this.healthRewardRatio = healthRewardRatio;
            entityProperty.selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int bonus = state.getCounterForRead()[counterIdx];
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n + bonus, this);
            state.getCounterForWrite()[counterIdx] += dmgInc;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("TheScythe", this, healthRewardRatio == 0 ? null : new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 50.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() { return 1; }
            });
            if (healthRewardRatio > 0) {
                state.properties.addExtraTrainingTarget("TheScythe", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                        if (isTerminal > 0) {
                            v.setVExtra(vExtraIdx, state.getCounterForRead()[counterIdx] / 10.0);
                        } else if (isTerminal == 0) {
                            double vScythe = Math.max(state.getCounterForRead()[counterIdx] / 10.0, v.getVExtra(vExtraIdx));
                            v.setVExtra(vExtraIdx, vScythe);
                        }
                    }
                    @Override public void updateQValues(GameState state, VArray v) {
                        double vScythe = Math.max(state.getCounterForRead()[counterIdx] / 10.0, v.getVExtra(vExtraIdx));
                        v.add(GameState.V_HEALTH_IDX, 10 * vScythe * healthRewardRatio / state.getPlayerForRead().getMaxHealth());
                    }
                });
            }
        }

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, "The Scythe").setShowFrequency(true);
        }
    }

    // The Scythe (Rare) - 2 energy, Attack
    //   Effect: Deal 13 damage. Permanently increase this card's damage by 3. Exhaust.
    //   Upgraded Effect: Deal 13 damage. Permanently increase this card's damage by 4. Exhaust.
    // TODO CHANGED: The Scythe (Rare) - 2 energy, Attack
    //   Effect: Deal 13 damage. Permanently increase this card's damage by 4. Exhaust.
    //   Upgraded Effect: Deal 13 damage. Permanently increase this card's damage by 5. Exhaust.
    public static class TheScythe extends _TheScytheT {
        public TheScythe() { this(13, 2); }
        public TheScythe(int n, int healthRewardRatio) { super("The Scythe (" + n + ")", n, 3, healthRewardRatio); }
    }

    public static class TheScytheP extends _TheScytheT {
        public TheScytheP() { this(13, 2); }
        public TheScytheP(int n, int healthRewardRatio) { super("The Scythe+ (" + n + ")", n, 4, healthRewardRatio); }
    }

    private static abstract class _TimesUpT extends Card {
        public _TimesUpT(String cardName, boolean retain) {
            super(cardName, Card.ATTACK, 2, Card.RARE);
            this.retain = retain;
            entityProperty.selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int doom = state.getEnemiesForRead().get(idx).getDoom();
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), doom, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Time's Up (Rare) - 2 energy, Attack
    //   Effect: Deal damage equal to the enemy's Doom. Exhaust.
    //   Upgraded Effect: Retain. Deal damage equal to the enemy's Doom. Exhaust.
    public static class TimesUp extends _TimesUpT {
        public TimesUp() { super("Time's Up", false); }
    }

    public static class TimesUpP extends _TimesUpT {
        public TimesUpP() { super("Time's Up+", true); }
    }

    private static abstract class _TransfigureT extends Card {
        private int[] spiralPlusOneCostTransformIdxes;

        public _TransfigureT(String cardName, boolean exhaust) {
            super(cardName, Card.SKILL, 1, Card.RARE);
            this.exhaustWhenPlayed = exhaust;
            entityProperty.selectFromHand = true;
            selectFromHandLater = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                if (spiralPlusOneCostTransformIdxes != null && spiralPlusOneCostTransformIdxes[idx] >= 0) {
                    var handArr = state.getHandArrForWrite();
                    for (int i = 0; i < state.handArrLen; i++) {
                        if (handArr[i] == idx) {
                            state.transformCard(handArr, i, spiralPlusOneCostTransformIdxes[idx]);
                            break;
                        }
                    }
                }
                return GameActionCtx.PLAY_CARD;
            }
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return cards.stream()
                    .map(c -> c.enchantSpiral(1))
                    .filter(Objects::nonNull)
                    .map(c -> c.getPermCostIfPossible(c.energyCost + 1))
                    .filter(Objects::nonNull)
                    .toList();
        }

        @Override public void gamePropertiesSetup(GameState state) {
            spiralPlusOneCostTransformIdxes = new int[state.properties.cardDict.length];
            Arrays.fill(spiralPlusOneCostTransformIdxes, -1);
            for (int i = 0; i < state.properties.cardDict.length; i++) {
                var spiraled = state.properties.cardDict[i].enchantSpiral(1);
                if (spiraled != null) {
                    var withCost = spiraled.getPermCostIfPossible(spiraled.energyCost + 1);
                    if (withCost != null) {
                        spiralPlusOneCostTransformIdxes[i] = state.properties.findCardIndex(withCost);
                    }
                }
            }
        }
    }

    // Transfigure (Rare) - 1 energy, Skill
    //   Effect: Add Replay to a card in your Hand. It costs an extra energy. Exhaust.
    //   Upgraded Effect: Add Replay to a card in your Hand. It costs an extra energy.
    public static class Transfigure extends _TransfigureT {
        public Transfigure() { super("Transfigure", true); }
    }

    public static class TransfigureP extends _TransfigureT {
        public TransfigureP() { super("Transfigure+", false); }
    }

    private static abstract class _UndeathT extends Card {
        private final int block;

        public _UndeathT(String cardName, int block) {
            super(cardName, Card.SKILL, 0, Card.RARE);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.addCardToDiscard(state.currentlyPlayedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Undeath (Rare) - 0 energy, Skill
    //   Effect: Gain 7 Block. Add a copy of this card into your Discard Pile.
    //   Upgraded Effect: Gain 9 Block. Add a copy of this card into your Discard Pile.
    public static class Undeath extends _UndeathT {
        public Undeath() { super("Undeath", 7); }
    }

    public static class UndeathP extends _UndeathT {
        public UndeathP() { super("Undeath+", 9); }
    }

    // **************************************************************************************************
    // ********************************************* Ancient *********************************************
    // **************************************************************************************************

    private static abstract class _ForbiddenGrimoireT extends Card {
        public _ForbiddenGrimoireT(String cardName, int cost) {
            super(cardName, Card.POWER, cost, Card.RARE);
            returnToDeckWhenPlay = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("ForbiddenGrimoire", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() { return 1; }
            });
        }

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, "Forbidden Grimoire").setShowFrequency(true);
        }
    }

    // Forbidden Grimoire (Ancient) - 2 energy, Power
    //   Effect: At the end of combat, you may remove a card from your Deck. Eternal.
    //   Upgraded Effect (1 energy): At the end of combat, you may remove a card from your Deck. Eternal.
    public static class ForbiddenGrimoire extends _ForbiddenGrimoireT {
        public ForbiddenGrimoire() { super("Forbidden Grimoire", 2); }
    }

    public static class ForbiddenGrimoireP extends _ForbiddenGrimoireT {
        public ForbiddenGrimoireP() { super("Forbidden Grimoire+", 1); }
    }

    private static abstract class _ProtectorT extends Card {
        private final int damage;

        public _ProtectorT(String cardName, int cost, int damage) {
            super(cardName, Card.ATTACK, cost, Card.RARE);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            entityProperty.otsyAttack = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int bonus = state.properties.otsyMaxHPCounterIdx >= 0 ? state.getCounterForRead()[state.properties.otsyMaxHPCounterIdx] : 0;
            state.otsyDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage + bonus);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Protector (Ancient) - 1 energy, Attack
    //   Effect: Osty deals 10 damage. Deals additional damage equal to Osty's Max HP.
    //   Upgraded Effect (0 energy): Osty deals 15 damage. Deals additional damage equal to Osty's Max HP.
    public static class Protector extends _ProtectorT {
        public Protector() { super("Protector", 1, 10); }
    }

    public static class ProtectorP extends _ProtectorT {
        public ProtectorP() { super("Protector+", 0, 15); }
    }
}
