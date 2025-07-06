package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.action.CardDrawAction;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyBeyond;
import com.alphaStS.enemy.EnemyReadOnly;
import com.alphaStS.utils.Tuple;
import com.alphaStS.utils.CounterStat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.alphaStS.enums.CharacterEnum;
import java.util.Objects;

public class CardIronclad {
    private static abstract class _BashT extends Card {
        private final int damage;
        private final int vulnerable;

        public _BashT(String cardName, int damage, int vulnerable) {
            super(cardName, Card.ATTACK, 2, Card.COMMON);
            this.damage = damage;
            this.vulnerable = vulnerable;
            selectEnemy = true;
            vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, vulnerable);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Bash extends _BashT {
        public Bash() {
            super("Bash", 8, 2);
        }
    }

    public static class BashP extends _BashT {
        public BashP() {
            super("Bash+", 10, 3);
        }
    }

    public static class Anger extends Card {
        public Anger() {
            super("Anger", Card.ATTACK, 0, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 6);
            state.addCardToDiscard(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new Anger());
        }
    }

    public static class AngerP extends Card {
        public AngerP() {
            super("Anger+", Card.ATTACK, 0, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 8);
            state.addCardToDiscard(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new AngerP());
        }
    }

    public static class Armanent extends Card {
        public Armanent() {
            super("Armanent", Card.SKILL, 1, Card.COMMON);
            selectFromHand = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(5);
            if (idx >= 0) {
                state.removeCardFromHand(idx);
                state.addCardToHand(state.properties.upgradeIdxes[idx]);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public boolean canSelectCard(Card card) {
            return card.getUpgrade() != null;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return cards.stream().map(Card::getUpgrade).filter(Objects::nonNull).toList();
        }
    }

    public static class ArmanentP extends Card {
        public ArmanentP() {
            super("Armanent+", Card.SKILL, 1, Card.COMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(5);
            state.handArrTransform(state.properties.upgradeIdxes);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return cards.stream().map(Card::getUpgrade).filter(Objects::nonNull).toList();
        }
    }

    private static abstract class _BodySlamT extends Card {
        public _BodySlamT(String cardName, int energyCost) {
            super(cardName, Card.ATTACK, energyCost, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), state.getPlayeForRead().getBlock());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BodySlam extends _BodySlamT {
        public BodySlam() {
            super("Body Slam", 1);
        }
    }

    public static class BodySlamP extends _BodySlamT {
        public BodySlamP() {
            super("Body Slam+", 0);
        }
    }

    private static abstract class _ClashT extends Card {
        private final int damage;

        public _ClashT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.COMMON);
            this.damage = damage;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            for (int i = 0; i < state.handArrLen; i++) {
                if (state.properties.cardDict[state.getHandArrForRead()[i]].cardType != Card.ATTACK) {
                    return -1;
                }
            }
            return energyCost;
        }
    }

    public static class Clash extends _ClashT {
        public Clash() {
            super("Clash", 14);
        }
    }

    public static class ClashP extends _ClashT {
        public ClashP() {
            super("Clash+", 18);
        }
    }

    private static abstract class _CleaveT extends Card {
        private final int damage;

        public _CleaveT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, damage);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Cleave extends _CleaveT {
        public Cleave() {
            super("Cleave", 8);
        }
    }

    public static class CleaveP extends _CleaveT {
        public CleaveP() {
            super("Cleave+", 11);
        }
    }

    private static abstract class _ClotheslineT extends Card {
        private final int damage;
        private final int weak;

        public _ClotheslineT(String cardName, int damage, int weak) {
            super(cardName, Card.ATTACK, 2, Card.COMMON);
            this.damage = damage;
            this.weak = weak;
            selectEnemy = true;
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage);
            enemy.applyDebuff(state, DebuffType.WEAK, weak);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Clothesline extends _ClotheslineT {
        public Clothesline() {
            super("Clothesline", 12, 2);
        }
    }

    public static class ClotheslineP extends _ClotheslineT {
        public ClotheslineP() {
            super("Clothesline+", 14, 3);
        }
    }

    private static abstract class _FlexT extends Card {
        private final int strength;

        public _FlexT(String cardName, int strength) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.strength = strength;
            changePlayerStrength = true;
            changePlayerStrengthEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var player = state.getPlayerForWrite();
            player.gainStrength(strength);
            player.applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, strength);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Flex extends _FlexT {
        public Flex() {
            super("Flex", 2);
        }
    }

    public static class FlexP extends _FlexT {
        public FlexP() {
            super("Flex+", 4);
        }
    }

    private static abstract class _HavocT extends Card {
        public _HavocT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.COMMON);
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int cardIdx = state.drawOneCardSpecial();
            if (cardIdx < 0) {
                return GameActionCtx.PLAY_CARD;
            }
            if (state.properties.makingRealMove || state.properties.stateDescOn) {
                if (!state.getStateDesc().isEmpty()) state.getStateDesc().append(", ");
                state.getStateDesc().append(state.properties.cardDict[cardIdx].cardName);
            }
            state.addGameActionToStartOfDeque(curState -> {
                if (curState.properties.cardDict[cardIdx].cardType != Card.POWER) {
                    curState.exhaustedCardHandle(cardIdx, true);
                }
            });
            state.addGameActionToStartOfDeque(curState -> {
                var action = curState.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                curState.playCard(action, -1, true, null, false, true, -1, -1);
                while (curState.actionCtx == GameActionCtx.SELECT_ENEMY) {
                    int enemyIdx = GameStateUtils.getRandomEnemyIdx(curState, RandomGenCtx.RandomEnemyGeneral);
                    if (curState.properties.makingRealMove || curState.properties.stateDescOn) {
                        curState.getStateDesc().append(" -> ").append(curState.getEnemiesForRead().get(enemyIdx).getName()).append(" (").append(enemyIdx).append(")");
                    }
                    curState.playCard(action, enemyIdx, true, null, false, true, -1, -1);
                }
            });
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Havoc extends _HavocT {
        public Havoc() {
            super("Havoc", 1);
        }
    }

    public static class HavocP extends _HavocT {
        public HavocP() {
            super("Havoc+", 0);
        }
    }

    private static abstract class _HeadbuttT extends Card {
        private final int damage;

        public _HeadbuttT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            selectEnemy = true;
            selectFromDiscard = true;
            selectFromDiscardLater = true;
            putCardOnTopDeck = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
                return GameActionCtx.SELECT_CARD_DISCARD;
            } else {
                state.removeCardFromDiscard(idx);
                state.addCardOnTopOfDeck(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class Headbutt extends _HeadbuttT {
        public Headbutt() {
            super("Headbutt", 9);
        }
    }

    public static class HeadbuttP extends _HeadbuttT {
        public HeadbuttP() {
            super("Headbutt+", 12);
        }
    }

    private static abstract class _HeavyBladeT extends Card {
        private final int baseDamage;
        private final int strengthMultiplier;

        public _HeavyBladeT(String cardName, int strengthMultiplier) {
            super(cardName, Card.ATTACK, 2, Card.COMMON);
            this.baseDamage = 14;
            this.strengthMultiplier = strengthMultiplier;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), baseDamage + state.getPlayeForRead().getStrength() * strengthMultiplier);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class HeavyBlade extends _HeavyBladeT {
        public HeavyBlade() {
            super("Heavy Blade", 2); // 3-1 because strength is added once by GameState
        }
    }

    public static class HeavyBladeP extends _HeavyBladeT {
        public HeavyBladeP() {
            super("Heavy Blade+", 4); // 5-1 because strength is added once by GameState
        }
    }

    private static abstract class _IronWaveT extends Card {
        private final int damageAndBlock;

        public _IronWaveT(String cardName, int damageAndBlock) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damageAndBlock = damageAndBlock;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damageAndBlock);
            state.getPlayerForWrite().gainBlock(damageAndBlock);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class IronWave extends _IronWaveT {
        public IronWave() {
            super("Iron Wave", 5);
        }
    }

    public static class IronWaveP extends _IronWaveT {
        public IronWaveP() {
            super("Iron Wave+", 7);
        }
    }

    public static class PerfectedStrike extends Card {
        public PerfectedStrike() {
            super("Perfected Strike", Card.ATTACK, 2, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int count = 1; // 1 because the played card is not in hand anymore
            var strikes = new boolean[state.properties.cardDict.length];
            for (int i = 0; i < state.properties.strikeCardIdxes.length; i++) {
                strikes[state.properties.strikeCardIdxes[i]] = true;
            }
            count += GameStateUtils.getCardsCount(state.getHandArrForRead(), state.getNumCardsInHand(), strikes);
            count += GameStateUtils.getCardsCount(state.getDiscardArrForRead(), state.getNumCardsInDiscard(), strikes);
            count += GameStateUtils.getCardsCount(state.getDeckArrForRead(), state.getNumCardsInDeck(), strikes);
            int dmg = 6 + 2 * count + (state.properties.strikeDummy != null && state.properties.strikeDummy.isRelicEnabledInScenario(state.preBattleScenariosChosenIdx) ? 3 : 0);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PerfectedStrikeP extends Card {
        public PerfectedStrikeP() {
            super("Perfected Strike+", Card.ATTACK, 2, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int count = 1; // 1 because the played card is not in hand anymore
            var strikes = new boolean[state.properties.cardDict.length];
            for (int i = 0; i < state.properties.strikeCardIdxes.length; i++) {
                strikes[state.properties.strikeCardIdxes[i]] = true;
            }
            count += GameStateUtils.getCardsCount(state.getHandArrForRead(), state.getNumCardsInHand(), strikes);
            count += GameStateUtils.getCardsCount(state.getDiscardArrForRead(), state.getNumCardsInDiscard(), strikes);
            count += GameStateUtils.getCardsCount(state.getDeckArrForRead(), state.getNumCardsInDeck(), strikes);
            int dmg = 6 + 3 * count + (state.properties.strikeDummy != null && state.properties.strikeDummy.isRelicEnabledInScenario(state.preBattleScenariosChosenIdx) ? 3 : 0);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _PommelStrikeT extends Card {
        private final int baseDamage;
        private final int draw;

        public _PommelStrikeT(String cardName, int baseDamage, int draw) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.baseDamage = baseDamage;
            this.draw = draw;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), baseDamage + (state.properties.strikeDummy != null && state.properties.strikeDummy.isRelicEnabledInScenario(state.preBattleScenariosChosenIdx) ? 3 : 0));
            state.draw(draw);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PommelStrike extends _PommelStrikeT {
        public PommelStrike() {
            super("Pommel Strike", 9, 1);
        }
    }

    public static class PommelStrikeP extends _PommelStrikeT {
        public PommelStrikeP() {
            super("Pommel Strike+", 10, 2);
        }
    }

    private static abstract class _ShrugItOffT extends Card {
        private final int block;

        public _ShrugItOffT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ShrugItOff extends _ShrugItOffT {
        public ShrugItOff() {
            super("Shrug It Off", 8);
        }
    }

    public static class ShrugItOffP extends _ShrugItOffT {
        public ShrugItOffP() {
            super("Shrug It Off+", 11);
        }
    }

    private static abstract class _SwordBoomerangT extends Card {
        private final int n;

        public _SwordBoomerangT(String cardName, int n) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            boolean moreThan1Enemy = state.enemiesAlive > 1;
            if (moreThan1Enemy) {
                state.setIsStochastic();
            }
            for (int _i = 0; _i < n; _i++) {
                int enemy_j = 0;
                if (state.enemiesAlive > 1) {
                    enemy_j = state.getSearchRandomGen().nextInt(state.enemiesAlive, RandomGenCtx.RandomEnemySwordBoomerang, state);
                }
                int j = 0, enemyIdx = -1;
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAll()) {
                    enemyIdx++;
                    if (!enemy.isAlive()) continue;
                    if (j == enemy_j) {
                        state.playerDoDamageToEnemy(enemy, 3);
                        if (moreThan1Enemy && (state.properties.makingRealMove || state.properties.stateDescOn)) {
                            state.getStateDesc().append(state.getStateDesc().length() > 0 ? ", " : "").append(enemy.getName())
                                    .append("(").append(enemyIdx).append(")");
                        }
                        break;
                    }
                    j += 1;
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SwordBoomerang extends _SwordBoomerangT {
        public SwordBoomerang() {
            super("Sword Boomerang", 3);
        }
    }

    public static class SwordBoomerangP extends _SwordBoomerangT {
        public SwordBoomerangP() {
            super("Sword Boomerang+", 4);
        }
    }

    private static abstract class _ThunderclapT extends Card {
        private final int damage;

        public _ThunderclapT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, damage);
                enemy.applyDebuff(state, DebuffType.VULNERABLE, 1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Thunderclap extends _ThunderclapT {
        public Thunderclap() {
            super("Thunderclap", 4);
        }
    }

    public static class ThunderclapP extends _ThunderclapT {
        public ThunderclapP() {
            super("Thunderclap+", 7);
        }
    }

    public static class TrueGrit extends Card {
        public TrueGrit() {
            super("True Grit", Card.SKILL, 1, Card.COMMON);
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(7);
            if (state.handArrLen > 0) {
                int diff = 0;
                var seen = new boolean[state.properties.cardDict.length];
                for (int i = 0; i < state.handArrLen; i++) {
                    if (!seen[state.getHandArrForRead()[i]]) {
                        diff++;
                        seen[state.getHandArrForRead()[i]] = true;
                    }
                }
                if (diff > 1) {
                    state.setIsStochastic();
                    int r = state.getSearchRandomGen().nextInt(state.handArrLen, RandomGenCtx.RandomCardHand, state);
                    state.exhaustCardFromHand(state.getHandArrForRead()[r]);
                } else {
                    state.exhaustCardFromHand(state.getHandArrForRead()[0]);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class TrueGritP extends Card {
        public TrueGritP() {
            super("True Grit+", Card.SKILL, 1, Card.COMMON);
            selectFromHand = true;
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(9);
            state.exhaustCardFromHand(idx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _TwinStrikeT extends Card {
        private final int damage;

        public _TwinStrikeT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            int actualDamage = damage + (state.properties.strikeDummy != null && state.properties.strikeDummy.isRelicEnabledInScenario(state.preBattleScenariosChosenIdx) ? 3 : 0);
            state.playerDoDamageToEnemy(enemy, actualDamage);
            state.playerDoDamageToEnemy(enemy, actualDamage);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class TwinStrike extends _TwinStrikeT {
        public TwinStrike() {
            super("Twin Strike", 5);
        }
    }

    public static class TwinStrikeP extends _TwinStrikeT {
        public TwinStrikeP() {
            super("Twin Strike+", 7);
        }
    }

    private static abstract class _WarcryT extends Card {
        private final int n;

        public _WarcryT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.n = n;
            exhaustWhenPlayed = true;
            selectFromHand = true;
            selectFromHandLater = true;
            putCardOnTopDeck = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.draw(n);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.removeCardFromHand(idx);
                state.addCardOnTopOfDeck(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class Warcry extends _WarcryT {
        public Warcry() {
            super("Warcry", 1);
        }
    }

    public static class WarcryP extends _WarcryT {
        public WarcryP() {
            super("Warcry+", 2);
        }
    }

    private static abstract class _WildStrikeT extends Card {
        private final int damage;

        public _WildStrikeT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage + (state.properties.strikeDummy != null && state.properties.strikeDummy.isRelicEnabledInScenario(state.preBattleScenariosChosenIdx) ? 3 : 0));
            state.addCardToDeck(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardOther.Wound());
        }
    }

    public static class WildStrike extends _WildStrikeT {
        public WildStrike() {
            super("Wild Strike", 12);
        }
    }

    public static class WildStrikeP extends _WildStrikeT {
        public WildStrikeP() {
            super("Wild Strike+", 17);
        }
    }

    private static abstract class _BattleTranceT extends Card {
        private final int draw;

        public _BattleTranceT(String cardName, int draw) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.draw = draw;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.draw(draw);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.NO_MORE_CARD_DRAW, 1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BattleTrance extends _BattleTranceT {
        public BattleTrance() {
            super("Battle Trance", 3);
        }
    }

    public static class BattleTranceP extends _BattleTranceT {
        public BattleTranceP() {
            super("Battle Trance+", 4);
        }
    }

    public static class BloodForBlood extends Card {

        public BloodForBlood() {
            this(4);
        }

        private BloodForBlood(int energyCost) {
            super("Blood For Blood (" + energyCost + ")", Card.ATTACK, energyCost, Card.UNCOMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 18);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var l = new ArrayList<Card>();
            for (int i = 0; i < cards.size(); i++) {
                if (cards.get(i).getBaseCard() instanceof BloodForBlood) {
                    l.add(cards.get(i).wrap(new BloodForBlood(3)));
                    l.add(cards.get(i).wrap(new BloodForBlood(2)));
                    l.add(cards.get(i).wrap(new BloodForBlood(1)));
                    l.add(cards.get(i).wrap(new BloodForBlood(0)));
                }
            }
            return l;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.bloodForBloodTransformIndexes = new int[state.properties.cardDict.length];
            Arrays.fill(state.properties.bloodForBloodTransformIndexes, -1);
            for (int i = 0; i < state.properties.cardDict.length; i++) {
                var card = state.properties.cardDict[i];
                if (card.getBaseCard() instanceof BloodForBlood && card.getBaseCard().energyCost > 0) {
                    var toIdx = state.properties.findCardIndex(card.wrap(new BloodForBlood(card.getBaseCard().energyCost - 1)));
                    state.properties.bloodForBloodTransformIndexes[i] = toIdx;
                }
            }
            state.properties.addOnDamageHandler("Blood For Blood", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (damageDealt <= 0) return;
                    state.handArrTransform(state.properties.bloodForBloodTransformIndexes);
                    state.discardArrTransform(state.properties.bloodForBloodTransformIndexes);
                    state.deckArrTransform(state.properties.bloodForBloodTransformIndexes);
                    state.exhaustArrTransform(state.properties.bloodForBloodTransformIndexes);
                }
            });
        }
    }

    public static class BloodForBloodP extends Card {

        public BloodForBloodP() {
            this(3);
        }

        private BloodForBloodP(int energyCost) {
            super("Blood For Blood+ (" + energyCost + ")", Card.ATTACK, energyCost, Card.UNCOMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 22);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var l = new ArrayList<Card>();
            for (int i = 0; i < cards.size(); i++) {
                if (cards.get(i).getBaseCard() instanceof BloodForBlood) {
                    l.add(cards.get(i).wrap(new BloodForBloodP(2)));
                    l.add(cards.get(i).wrap(new BloodForBloodP(1)));
                    l.add(cards.get(i).wrap(new BloodForBloodP(0)));
                }
            }
            return l;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.bloodForBloodPTransformIndexes = new int[state.properties.cardDict.length];
            Arrays.fill(state.properties.bloodForBloodPTransformIndexes, -1);
            for (int i = 0; i < state.properties.cardDict.length; i++) {
                var card = state.properties.cardDict[i];
                if (card.getBaseCard() instanceof BloodForBloodP && card.getBaseCard().energyCost > 0) {
                    var toIdx = state.properties.findCardIndex(card.wrap(new BloodForBloodP(card.getBaseCard().energyCost - 1)));
                    state.properties.bloodForBloodPTransformIndexes[i] = toIdx;
                }
            }
            state.properties.addOnDamageHandler("Blood For Blood+", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (damageDealt <= 0) return;
                    state.handArrTransform(state.properties.bloodForBloodPTransformIndexes);
                    state.discardArrTransform(state.properties.bloodForBloodPTransformIndexes);
                    state.deckArrTransform(state.properties.bloodForBloodPTransformIndexes);
                    state.exhaustArrTransform(state.properties.bloodForBloodPTransformIndexes);
                }
            });
        }
    }

    private static abstract class _BloodlettingT extends Card {
        private final int energy;

        public _BloodlettingT(String cardName, int energy) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.energy = energy;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.doNonAttackDamageToPlayer(3, false, this);
            state.gainEnergy(energy);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Bloodletting extends _BloodlettingT {
        public Bloodletting() {
            super("Bloodletting", 2);
        }
    }

    public static class BloodlettingP extends _BloodlettingT {
        public BloodlettingP() {
            super("Bloodletting+", 3);
        }
    }

    private static abstract class _BurningPactT extends Card {
        private final int draw;

        public _BurningPactT(String cardName, int draw) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.draw = draw;
            selectFromHand = true;
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.exhaustCardFromHand(idx);
            state.draw(draw);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BurningPact extends _BurningPactT {
        public BurningPact() {
            super("Burning Pact", 2);
        }
    }

    public static class BurningPactP extends _BurningPactT {
        public BurningPactP() {
            super("Burning Pact+", 3);
        }
    }

    private static abstract class _CarnageT extends Card {
        private final int damage;

        public _CarnageT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            selectEnemy = true;
            ethereal = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Carnage extends _CarnageT {
        public Carnage() {
            super("Carnage", 20);
        }
    }

    public static class CarnageP extends _CarnageT {
        public CarnageP() {
            super("Carnage+", 28);
        }
    }

    private static abstract class _CombustT extends Card {
        private final int n;

        public _CombustT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            // we split counter into two component, 1 for self damage, and 1 for enemy damage
            state.getCounterForWrite()[counterIdx] += 1 << 16;
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var _this = this;
            var name = cardName.endsWith("+") ? cardName.substring(0, cardName.length() - 1) : cardName ;
            state.properties.registerCounter(name, this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    int counter = state.getCounterForRead()[counterIdx];
                    input[idx] = (counter >> 16) / 2.0f;
                    input[idx + 1] = (counter & ((1 << 16) - 1)) / 14.0f;
                    return idx + 2;
                }
                @Override public int getInputLenDelta() {
                    return 2;
                }
            });
            state.properties.addPreEndOfTurnHandler(name, new GameEventHandler() {
                @Override public void handle(GameState state) {
                    var counter = state.getCounterForRead()[counterIdx];
                    var selfDmg = counter >> 16;
                    var enemyDmg = counter & ((1 << 16) - 1);
                    state.doNonAttackDamageToPlayer(selfDmg, false, _this);
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        state.playerDoNonAttackDamageToEnemy(enemy, enemyDmg, true);
                    }
                }
            });
        }
    }

    public static class Combust extends _CombustT {
        public Combust() {
            super("Combust", 5);
        }
    }

    public static class CombustP extends _CombustT {
        public CombustP() {
            super("Combust+", 7);
        }
    }

    private static abstract class _DarkEmbraceT extends Card {
        public _DarkEmbraceT(String cardName, int energyCost) {
            super(cardName, Card.POWER, energyCost, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("DarkEmbrace", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 2.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnExhaustHandler("DarkEmbrace", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.addGameActionToEndOfDeque(new CardDrawAction(state.getCounterForRead()[counterIdx]));
                }
            });
        }
    }

    public static class DarkEmbrace extends _DarkEmbraceT {
        public DarkEmbrace() {
            super("Dark Embrace", 2);
        }
    }

    public static class DarkEmbraceP extends _DarkEmbraceT {
        public DarkEmbraceP() {
            super("Dark Embrace+", 1);
        }
    }

    private static abstract class _DisarmT extends Card {
        private final int strengthLoss;

        public _DisarmT(String cardName, int strengthLoss) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.strengthLoss = strengthLoss;
            exhaustWhenPlayed = true;
            selectEnemy = true;
            affectEnemyStrength = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.LOSE_STRENGTH, strengthLoss);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Disarm extends _DisarmT {
        public Disarm() {
            super("Disarm", 2);
        }
    }

    public static class DisarmP extends _DisarmT {
        public DisarmP() {
            super("Disarm+", 3);
        }
    }

    private static abstract class _DropkickT extends Card {
        private final int damage;

        public _DropkickT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage);
            if (enemy.getVulnerable() > 0) {
                state.energy += 1;
                state.draw(1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Dropkick extends _DropkickT {
        public Dropkick() {
            super("Dropkick", 5);
        }
    }

    public static class DropkickP extends _DropkickT {
        public DropkickP() {
            super("Dropkick+", 8);
        }
    }

    private static abstract class _DualWieldT extends Card {
        private final int copies;

        public _DualWieldT(String cardName, int copies) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.copies = copies;
            selectFromHand = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < copies; i++) {
                state.addCardToHand(idx);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public boolean canSelectCard(Card card) {
            return card.cardType == Card.ATTACK;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return cards.stream().filter(card -> card.cardType == Card.ATTACK).toList();
        }
    }

    public static class DualWield extends _DualWieldT {
        public DualWield() {
            super("Dual Wield", 1);
        }
    }

    public static class DualWieldP extends _DualWieldT {
        public DualWieldP() {
            super("Dual Wield+", 2);
        }
    }

    private static abstract class _EntrenchT extends Card {
        public _EntrenchT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var player = state.getPlayerForWrite();
            player.gainBlockNotFromCardPlay(player.getBlock());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Entrench extends _EntrenchT {
        public Entrench() {
            super("Entrench", 2);
        }
    }

    public static class EntrenchP extends _EntrenchT {
        public EntrenchP() {
            super("Entrench+", 1);
        }
    }

    private static abstract class _EvolveT extends Card {
        private final int n;

        public _EvolveT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Evolve", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardDrawnHandler("Evolve", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.STATUS) {
                        state.addGameActionToEndOfDeque(new CardDrawAction(state.getCounterForRead()[counterIdx]));
                    }
                }
            });
        }
    }

    public static class Evolve extends _EvolveT {
        public Evolve() {
            super("Evolve", 1);
        }
    }

    public static class EvolveP extends _EvolveT {
        public EvolveP() {
            super("Evolve+", 2);
        }
    }

    private static abstract class _FeelNoPainT extends Card {
        private final int blockAmount;

        public _FeelNoPainT(String cardName, int blockAmount) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.blockAmount = blockAmount;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += blockAmount;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("FNP", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 8.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnExhaustHandler("FNP", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getPlayerForWrite().gainBlockNotFromCardPlay(state.getCounterForRead()[counterIdx]);
                }
            });
        }
    }

    public static class FeelNoPain extends _FeelNoPainT {
        public FeelNoPain() {
            super("Feel No Pain", 3);
        }
    }

    public static class FeelNoPainP extends _FeelNoPainT {
        public FeelNoPainP() {
            super("Feel No Pain+", 4);
        }
    }

    private abstract static class _FireBreathingT extends Card {
        private final int n;

        public _FireBreathingT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("FireBreathing", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardDrawnHandler("FireBreathing", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    var card = state.properties.cardDict[cardIdx];
                    if (card.cardType == Card.STATUS || card.cardType == Card.CURSE) {
                        int dmg = state.getCounterForRead()[counterIdx];
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            state.playerDoNonAttackDamageToEnemy(enemy, dmg, true);
                        }
                    }
                }
            });
        }
    }

    public static class FireBreathing extends _FireBreathingT {
        public FireBreathing() {
            super("Fire Breathing", 6);
        }
    }

    public static class FireBreathingP extends _FireBreathingT {
        public FireBreathingP() {
            super("Fire Breathing+", 10);
        }
    }

    private static abstract class _FlameBarrierT extends Card {
        private final int block;
        private final int counter;

        public _FlameBarrierT(String cardName, int block, int counter) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
            this.block = block;
            this.counter = counter;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            state.getCounterForWrite()[counterIdx] += counter;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("FlameBarrier", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 12.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnDamageHandler("FlameBarrier", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (isAttack && source instanceof Enemy enemy) {
                        state.playerDoNonAttackDamageToEnemy(enemy, state.getCounterForRead()[counterIdx], true);
                    }
                }
            });
            state.properties.addStartOfTurnHandler("FlameBarrier", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    public static class FlameBarrier extends _FlameBarrierT {
        public FlameBarrier() {
            super("Flame Barrier", 12, 4);
        }
    }

    public static class FlameBarrierP extends _FlameBarrierT {
        public FlameBarrierP() {
            super("Flame Barrier+", 16, 6);
        }
    }

    private static abstract class _GhostlyArmorT extends Card {
        private final int block;

        public _GhostlyArmorT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
            ethereal = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class GhostlyArmor extends _GhostlyArmorT {
        public GhostlyArmor() {
            super("Ghostly Armor", 10);
        }
    }

    public static class GhostlyArmorP extends _GhostlyArmorT {
        public GhostlyArmorP() {
            super("Ghostly Armor+", 13);
        }
    }

    private static abstract class _HemokinesisT extends Card {
        private final int damage;

        public _HemokinesisT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.doNonAttackDamageToPlayer(2, false, this);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Hemokinesis extends _HemokinesisT {
        public Hemokinesis() {
            super("Hemokinesis", 15);
        }
    }

    public static class HemokinesisP extends _HemokinesisT {
        public HemokinesisP() {
            super("Hemokinesis+", 20);
        }
    }

    private abstract static class _InfernalBladeT extends Card {
        public _InfernalBladeT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.UNCOMMON);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if ((state.properties.generateCardOptions & GameProperties.GENERATE_CARD_INFERNAL_BLADE) == 0) {
                state.addCardToHand(state.properties.findCardIndex(new Card.Strike().getTemporaryCostIfPossible(0)));
                return GameActionCtx.PLAY_CARD;
            }
            var r = state.getSearchRandomGen().nextInt(generatedCardIdxes.length, RandomGenCtx.RandomCardGen, new Tuple<>(state, generatedCardIdxes));
            state.addCardToHand(generatedCardIdxes[r]);
            state.setIsStochastic();
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            if ((properties.generateCardOptions & GameProperties.GENERATE_CARD_INFERNAL_BLADE) == 0) {
                return List.of(new Card.Strike().getTemporaryCostIfPossible(0));
            }
            return CardManager.getCharacterCardsByTypeTmp0Cost(CharacterEnum.IRONCLAD, Card.ATTACK, false);
        }
    }

    public static class InfernalBlade extends _InfernalBladeT {
        public InfernalBlade() {
            super("Infernal Blade", 1);
        }
    }

    public static class InfernalBladeP extends _InfernalBladeT {
        public InfernalBladeP() {
            super("Infernal Blade+", 0);
        }
    }

    private static abstract class _InflameT extends Card {
        private final int strength;

        public _InflameT(String cardName, int strength) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.strength = strength;
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainStrength(strength);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Inflame extends _InflameT {
        public Inflame() {
            super("Inflame", 2);
        }
    }

    public static class InflameP extends _InflameT {
        public InflameP() {
            super("Inflame+", 3);
        }
    }

    private static abstract class _IntimidateT extends Card {
        private final int weak;

        public _IntimidateT(String cardName, int weak) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.weak = weak;
            weakEnemy = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.WEAK, weak);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Intimidate extends _IntimidateT {
        public Intimidate() {
            super("Intimidate", 1);
        }
    }

    public static class IntimidateP extends _IntimidateT {
        public IntimidateP() {
            super("Intimidate+", 2);
        }
    }

    private static abstract class _MetallicizeT extends Card {
        private final int amount;

        public _MetallicizeT(String cardName, int amount) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.amount = amount;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[state.properties.metallicizeCounterIdx] += amount;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerMetallicizeCounter();
        }
    }

    public static class Metallicize extends _MetallicizeT {
        public Metallicize() {
            super("Metallicize", 3);
        }
    }

    public static class MetallicizeP extends _MetallicizeT {
        public MetallicizeP() {
            super("Metallicize+", 4);
        }
    }

    private static abstract class _PowerThroughT extends Card {
        private final int block;

        public _PowerThroughT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            state.addCardToHand(generatedCardIdx);
            state.addCardToHand(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardOther.Wound());
        }
    }

    public static class PowerThrough extends _PowerThroughT {
        public PowerThrough() {
            super("Power Through", 15);
        }
    }

    public static class PowerThroughP extends _PowerThroughT {
        public PowerThroughP() {
            super("Power Through+", 20);
        }
    }

    private static abstract class _PummelT extends Card {
        private final int hits;

        public _PummelT(String cardName, int hits) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.hits = hits;
            selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < hits; i++) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 2);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Pummel extends _PummelT {
        public Pummel() {
            super("Pummel", 4);
        }
    }

    public static class PummelP extends _PummelT {
        public PummelP() {
            super("Pummel+", 5);
        }
    }

    private static abstract class _RageT extends Card {
        private final int n;

        public _RageT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Rage", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("Rage", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        state.getPlayerForWrite().gainBlockNotFromCardPlay(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
            state.properties.addPreEndOfTurnHandler("Rage", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    public static class Rage extends _RageT {
        public Rage() {
            super("Rage", 3);
        }
    }

    public static class RageP extends _RageT {
        public RageP() {
            super("Rage+", 5);
        }
    }

    public static class Rampage extends Card {
        public int limit;
        private int dmg;
        private boolean upgradable;

        public Rampage() {
            this(8, 33);
        }

        public Rampage(int dmg, int limit) {
            super("Rampage (" + dmg + ")", Card.ATTACK, 1, Card.UNCOMMON);
            this.dmg = dmg;
            this.limit = limit;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var c = new ArrayList<Card>();
            for (Card card : cards) {
                if (card.getBaseCard() instanceof Rampage) {
                    for (int j = dmg + 5; j <= limit; j += 5) {
                        c.add(card.wrapAfterPlay(new Rampage(j, limit)));
                    }
                }
            }
            return c;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            upgradable = (state.properties.generateCardOptions & GameProperties.GENERATE_CARD_RAMPAGE_UPGRADE) != 0;
            state.properties.rampageTransformIndexes = new int[state.properties.cardDict.length];
            for (int i = 0; i < state.properties.rampageTransformIndexes.length; i++) {
                var card = state.properties.cardDict[i].getBaseCard();
                if (card instanceof Rampage rampage) {
                    state.properties.rampageTransformIndexes[i] = state.properties.findCardIndex(state.properties.cardDict[i].wrap(new Rampage(rampage.dmg + 5, limit)));
                }
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop, int cardIdx) {
            return prop.rampageTransformIndexes[cardIdx];
        }

        public Card getUpgrade() {
            return upgradable ? new RampageP(dmg, limit + 25) : null;
        }
    }

    public static class RampageP extends Card {
        public int limit;
        private int dmg;

        public RampageP() {
            this(8, 58);
        }

        public RampageP(int dmg, int limit) {
            super("Rampage+ (" + dmg + ")", Card.ATTACK, 1, Card.UNCOMMON);
            this.dmg = dmg;
            this.limit = limit;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var c = new ArrayList<Card>();
            for (Card card : cards) {
                if (card.getBaseCard() instanceof Rampage) {
                    for (int j = dmg + 8; j <= limit; j += 8) {
                        c.add(card.wrapAfterPlay(new RampageP(j, limit)));
                    }
                }
            }
            return c;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.rampagePTransformIndexes = new int[state.properties.cardDict.length];
            for (int i = 0; i < state.properties.rampagePTransformIndexes.length; i++) {
                var card = state.properties.cardDict[i].getBaseCard();
                if (card instanceof Rampage rampage) {
                    state.properties.rampagePTransformIndexes[i] = state.properties.findCardIndex(state.properties.cardDict[i].wrap(new RampageP(rampage.dmg + 8, limit)));
                }
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop, int cardIdx) {
            return prop.rampagePTransformIndexes[cardIdx];
        }
    }

    private static abstract class _RecklessChargeT extends Card {
        private final int damage;

        public _RecklessChargeT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.damage = damage;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            state.addCardToDeck(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardOther.Dazed());
        }
    }

    public static class RecklessCharge extends _RecklessChargeT {
        public RecklessCharge() {
            super("Reckless Charge", 7);
        }
    }

    public static class RecklessChargeP extends _RecklessChargeT {
        public RecklessChargeP() {
            super("Reckless Charge+", 10);
        }
    }

    private static abstract class _RuptureT extends Card {
        private final int n;

        public _RuptureT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            changePlayerStrength = true;
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Rupture", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 4.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnDamageHandler("Rupture", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (damageDealt > 0 && source instanceof Card) {
                        state.getPlayerForWrite().gainStrength(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }
    }

    public static class Rupture extends _RuptureT {
        public Rupture() {
            super("Rupture", 1);
        }
    }

    public static class RuptureP extends _RuptureT {
        public RuptureP() {
            super("Rupture+", 2);
        }
    }

    public static class SearingBlow extends Card {
        private final static int MAX_UPGRADES = 10;
        private final int n;

        public SearingBlow(int numberOfUpgrades) {
            super(numberOfUpgrades == 0 ? "Searing Blow" : "Searing Blow+" + numberOfUpgrades, Card.ATTACK, 2, Card.UNCOMMON);
            n = numberOfUpgrades;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n * (n + 7) / 2 + 12);
            return GameActionCtx.PLAY_CARD;
        }

        public Card getUpgrade() {
            return n >= MAX_UPGRADES ? null : new SearingBlow(n + 1);
        }
    }

    private static abstract class _SecondWindT extends Card {
        private final int blockPerCard;

        public _SecondWindT(String cardName, int blockPerCard) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.blockPerCard = blockPerCard;
            exhaustNonAttacks = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int c = state.handArrLen;
            for (int i = 0; i < c; i++) {
                state.exhaustCardFromHandByPosition(i, false);
            }
            state.updateHandArr();
            for (int i = 0; i < c; i++) {
                state.getPlayerForWrite().gainBlock(blockPerCard);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SecondWind extends _SecondWindT {
        public SecondWind() {
            super("Second Wind", 5);
        }
    }

    public static class SecondWindP extends _SecondWindT {
        public SecondWindP() {
            super("Second Wind+", 7);
        }
    }

    private static abstract class _SeeingRedT extends Card {
        public _SeeingRedT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.UNCOMMON);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SeeingRed extends _SeeingRedT {
        public SeeingRed() {
            super("Seeing Red", 1);
        }
    }

    public static class SeeingRedP extends _SeeingRedT {
        public SeeingRedP() {
            super("Seeing Red+", 0);
        }
    }

    private static abstract class _SentinelT extends Card {
        private final int block;
        private final int energy;

        public _SentinelT(String cardName, int block, int energy) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
            this.energy = energy;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            return GameActionCtx.PLAY_CARD;
        }

        public void onExhaust(GameState state) {
            state.energy += energy;
        }
    }

    public static class Sentinel extends _SentinelT {
        public Sentinel() {
            super("Sentinel", 5, 2);
        }
    }

    public static class SentinelP extends _SentinelT {
        public SentinelP() {
            super("Sentinel+", 8, 3);
        }
    }

    private static abstract class _SeverSoulT extends Card {
        private final int damage;

        public _SeverSoulT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            selectEnemy = true;
            exhaustNonAttacks = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < state.handArrLen; i++) {
                if (state.properties.cardDict[state.getHandArrForRead()[i]].cardType != Card.ATTACK) {
                    state.exhaustCardFromHandByPosition(i, false);
                }
            }
            state.updateHandArr();
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SeverSoul extends _SeverSoulT {
        public SeverSoul() {
            super("Sever Soul", 16);
        }
    }

    public static class SeverSoulP extends _SeverSoulT {
        public SeverSoulP() {
            super("Sever Soul+", 22);
        }
    }

    private static abstract class _ShockwaveT extends Card {
        private final int debuffAmount;

        public _ShockwaveT(String cardName, int debuffAmount) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
            this.debuffAmount = debuffAmount;
            exhaustWhenPlayed = true;
            vulnEnemy = true;
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.WEAK, debuffAmount);
                enemy.applyDebuff(state, DebuffType.VULNERABLE, debuffAmount);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Shockwave extends _ShockwaveT {
        public Shockwave() {
            super("Shockwave", 3);
        }
    }

    public static class ShockwaveP extends _ShockwaveT {
        public ShockwaveP() {
            super("Shockwave+", 5);
        }
    }

    private static abstract class _SpotWeaknessT extends Card {
        private final int strength;

        public _SpotWeaknessT(String cardName, int strength) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.strength = strength;
            selectEnemy = true;
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.getEnemiesForRead().get(idx).getMoveString(state).contains("Attack")) {
                state.getPlayerForWrite().gainStrength(strength);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SpotWeakness extends _SpotWeaknessT {
        public SpotWeakness() {
            super("Spot Weakness", 3);
        }
    }

    public static class SpotWeaknessP extends _SpotWeaknessT {
        public SpotWeaknessP() {
            super("Spot Weakness+", 4);
        }
    }

    private static abstract class _UppercutT extends Card {
        private final int debuffAmount;

        public _UppercutT(String cardName, int debuffAmount) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.debuffAmount = debuffAmount;
            selectEnemy = true;
            vulnEnemy = true;
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 13);
            enemy.applyDebuff(state, DebuffType.WEAK, debuffAmount);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, debuffAmount);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Uppercut extends _UppercutT {
        public Uppercut() {
            super("Uppercut", 1);
        }
    }

    public static class UppercutP extends _UppercutT {
        public UppercutP() {
            super("Uppercut+", 2);
        }
    }

    private static abstract class _WhirlwindT extends Card {
        private final int damage;

        public _WhirlwindT(String cardName, int damage) {
            super(cardName, Card.ATTACK, -1, Card.UNCOMMON);
            this.damage = damage;
            isXCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < energyUsed; i++) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    state.playerDoDamageToEnemy(enemy, damage);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            return state.energy;
        }
    }

    public static class Whirlwind extends _WhirlwindT {
        public Whirlwind() {
            super("Whirlwind", 5);
        }
    }

    public static class WhirlwindP extends _WhirlwindT {
        public WhirlwindP() {
            super("Whirlwind+", 8);
        }
    }

    private static abstract class _BarricadeT extends Card {
        public _BarricadeT(String cardName, int energyCost) {
            super(cardName, Card.POWER, energyCost, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.buffs |= PlayerBuff.BARRICADE.mask();
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Barricade extends _BarricadeT {
        public Barricade() {
            super("Barricade", 3);
        }
    }

    public static class BarricadeP extends _BarricadeT {
        public BarricadeP() {
            super("Barricade+", 2);
        }
    }

    private static abstract class _BerserkT extends Card {
        private final int vulnerable;

        public _BerserkT(String cardName, int vulnerable) {
            super(cardName, Card.POWER, 0, Card.RARE);
            this.vulnerable = vulnerable;
            this.changePlayerVulnerable = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, vulnerable);
            state.energyRefill += 1;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Berserk extends _BerserkT {
        public Berserk() {
            super("Berserk", 2);
        }
    }

    public static class BerserkP extends _BerserkT {
        public BerserkP() {
            super("Berserk+", 1);
        }
    }

    private static abstract class _BludgeonT extends Card {
        private final int damage;

        public _BludgeonT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 3, Card.RARE);
            this.damage = damage;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Bludgeon extends _BludgeonT {
        public Bludgeon() {
            super("Bludgeon", 32);
        }
    }

    public static class BludgeonP extends _BludgeonT {
        public BludgeonP() {
            super("Bludgeon+", 42);
        }
    }

    private static abstract class _BrutalityT extends Card {
        public _BrutalityT(String cardName, boolean innate) {
            super(cardName, Card.POWER, 0, Card.RARE);
            this.innate = innate;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var _this = this;
            state.properties.registerCounter("Brutality", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 2.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Brutality", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int n = state.getCounterForRead()[counterIdx];
                    state.doNonAttackDamageToPlayer(n, false, _this);
                    state.draw(n);
                }
            });
        }
    }

    public static class Brutality extends _BrutalityT {
        public Brutality() {
            super("Brutality", false);
        }
    }

    public static class BrutalityP extends _BrutalityT {
        public BrutalityP() {
            super("Brutality+", true);
        }
    }

    private static abstract class _CorruptionT extends Card {
        public _CorruptionT(String cardName, int energyCost) {
            super(cardName, Card.POWER, energyCost, Card.RARE);
            exhaustSkill = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.buffs |= PlayerBuff.CORRUPTION.mask();
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Corruption extends _CorruptionT {
        public Corruption() {
            super("Corruption", 3);
        }
    }

    public static class CorruptionP extends _CorruptionT {
        public CorruptionP() {
            super("Corruption+", 2);
        }
    }

    private static abstract class _DemonFormT extends Card {
        private final int n;

        public _DemonFormT(String cardName, int n) {
            super(cardName, Card.POWER, 3, Card.RARE);
            changePlayerStrength = true;
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("DemonForm", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 6.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("DemonForm", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getPlayerForWrite().gainStrength(state.getCounterForRead()[counterIdx]);
                }
            });
        }
    }

    public static class DemonForm extends _DemonFormT {
        public DemonForm() {
            super("Demon Form", 2);
        }
    }

    public static class DemonFormP extends _DemonFormT {
        public DemonFormP() {
            super("Demon Form+", 3);
        }
    }

    private static abstract class _DoubleTapT extends Card {
        private final int n;

        public _DoubleTapT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("DoubleTap", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = Math.abs(state.getCounterForRead()[counterIdx]) / 4.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("DoubleTap", new GameEventCardHandler(GameEventCardHandler.CLONE_CARD_PRIORITY) {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    var card = state.properties.cardDict[cardIdx];
                    if (cloneSource != null || card.cardType != Card.ATTACK) {
                        return;
                    } else if (state.getCounterForRead()[counterIdx] == 0) {
                        return;
                    }
                    var counters = state.getCounterForWrite();
                    counters[counterIdx] -= 1;
                    state.addGameActionToEndOfDeque(curState -> {
                        var action = curState.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                        curState.playCard(action, lastIdx, true, DoubleTap.class, false, false, energyUsed, cloneParentLocation);
                    });
                }
            });
        }
    }

    // todo: test
    public static class DoubleTap extends _DoubleTapT {
        public DoubleTap() {
            super("Double Tap", 1);
        }
    }

    public static class DoubleTapP extends _DoubleTapT {
        public DoubleTapP() {
            super("Double Tap+", 2);
        }
    }

    private static abstract class _ExhumeT extends Card {
        public _ExhumeT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.RARE);
            selectFromExhaust = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.removeCardFromExhaust(idx);
            state.addCardToHand(idx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public boolean canSelectCard(Card card) {
            return !(card.getBaseCard() instanceof _ExhumeT);
        }
    }

    public static class Exhume extends _ExhumeT {
        public Exhume() {
            super("Exhume", 1);
        }
    }

    public static class ExhumeP extends _ExhumeT {
        public ExhumeP() {
            super("Exhume+", 0);
        }
    }

    public static abstract class _FeedT extends Card {
        private final int n;
        private final int hpInc;
        protected final double healthRewardRatio;

        public _FeedT(String cardName, int n, int hpInc, double healthRewardRatio) {
            super(cardName, Card.ATTACK, 1, Card.RARE);
            this.n = n;
            this.hpInc = hpInc;
            this.healthRewardRatio = healthRewardRatio;
            healPlayer = true;
            exhaustWhenPlayed = true;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            if (!state.getEnemiesForRead().get(idx).properties.isMinion && !GameProperties.isHeartFight(state) && state.getEnemiesForRead().get(idx).getHealth() <= 0) {
                if (state.getEnemiesForRead().get(idx) instanceof EnemyBeyond.Darkling ||
                        state.getEnemiesForRead().get(idx) instanceof EnemyBeyond.AwakenedOne) {
                    if (state.isTerminal() > 0) {
                        state.healPlayer(hpInc);
                        state.getCounterForWrite()[state.properties.feedCounterIdx] += hpInc;
                        state.getPlayerForWrite().setInBattleMaxHealth(state.getPlayerForWrite().getMaxHealth() + hpInc);
                    }
                } else {
                    state.healPlayer(hpInc);
                    state.getCounterForWrite()[state.properties.feedCounterIdx] += hpInc;
                    state.getPlayerForWrite().setInBattleMaxHealth(state.getPlayerForWrite().getMaxHealth() + hpInc);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Feed", this, healthRewardRatio == 0 ? null : new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 16.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            if (healthRewardRatio > 0) {
                state.properties.addExtraTrainingTarget("Feed", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                        if (isTerminal > 0) {
                            v.setVExtra(vExtraIdx, state.getCounterForRead()[counterIdx] / 16.0);
                        } else if (isTerminal == 0) {
                            int minFeed = state.getCounterForRead()[counterIdx];
                            int maxFeedRemaining = getMaxPossibleFeedRemaining(state);
                            double vFeed = Math.max(minFeed / 16.0, Math.min((minFeed + maxFeedRemaining) / 16.0, state.getVExtra(vExtraIdx)));
                            v.setVExtra(vExtraIdx, vFeed);
                        }
                    }

                    @Override public void updateQValues(GameState state, VArray v) {
                        int minFeed = state.getCounterForRead()[counterIdx];
                        int maxFeedRemaining = getMaxPossibleFeedRemaining(state);
                        double vFeed = Math.max(minFeed / 16.0, Math.min((minFeed + maxFeedRemaining) / 16.0, v.getVExtra(vExtraIdx)));
                        v.add(GameState.V_HEALTH_IDX, 16 * vFeed * healthRewardRatio / state.getPlayeForRead().getMaxHealth());
                    }
                });
            }
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            counterIdx = idx;
            gameProperties.feedCounterIdx = idx;
        }

        @Override public CounterStat getCounterStat() {
            return new CounterStat(counterIdx, "Feed").setShowFrequency(true);
        }

        private static int getCardCount(GameState state, int idx) {
            int count = 0;
            count += GameStateUtils.getCardCount(state.getHandArrForRead(), state.getNumCardsInHand(), idx);
            if (idx < state.properties.realCardsLen) {
                count += GameStateUtils.getCardCount(state.getDiscardArrForRead(), state.getNumCardsInDiscard(), idx);
                count += GameStateUtils.getCardCount(state.getDeckArrForRead(), state.getNumCardsInDeck(), idx);
            }
            return count;
        }

        private static boolean canUpgradeFeed(GameState state) {
            var idxes = new int[5];
            boolean canUpgrade = false;
            state.properties.findCardIndex(idxes, "Armanent", "Armanent (Tmp 0)", "Armanent (Perm 0)", "Armanent (Perm 2)", "Armanent (Perm 3)");
            for (int i = 0; i < idxes.length; i++) {
                if (idxes[i] > 0 && getCardCount(state, idxes[i]) > 0) {
                    canUpgrade = true;
                }
            }
            state.properties.findCardIndex(idxes, "Armanent+", "Armanent+ (Tmp 0)", "Armanent+ (Perm 0)", "Armanent+ (Perm 2)", "Armanent+ (Perm 3)");
            for (int i = 0; i < idxes.length; i++) {
                if (idxes[i] > 0 && getCardCount(state, idxes[i]) > 0) {
                    canUpgrade = true;
                }
            }
            state.properties.findCardIndex(idxes, "Apotheosis", "Apotheosis (Tmp 0)", "Apotheosis (Perm 0)", "Apotheosis (Perm 2)", "Apotheosis (Perm 3)");
            for (int i = 0; i < idxes.length; i++) {
                if (idxes[i] > 0 && getCardCount(state, idxes[i]) > 0) {
                    canUpgrade = true;
                }
            }
            state.properties.findCardIndex(idxes, "Apotheosis+", "Apotheosis+ (Tmp 0)", "Apotheosis+ (Perm 0)", "Apotheosis+ (Perm 2)", "Apotheosis+ (Perm 3)");
            for (int i = 0; i < idxes.length; i++) {
                if (idxes[i] > 0 && getCardCount(state, idxes[i]) > 0) {
                    canUpgrade = true;
                }
            }
            return canUpgrade;
        }

        public static int getMaxPossibleFeedRemaining(GameState state) {
            if (GameProperties.isHeartFight(state)) {
                return 0;
            }
            // todo: very very hacky, need to create a generate card dependency graph and use that to get if a card can be generated
            var remain = 0;
            var idxes = new int[5];
            var canUpgrade = canUpgradeFeed(state);
            int maxFeedable = 0;
            int maxFeedableP = 0;
            state.properties.findCardIndex(idxes, "Feed", "Feed (Tmp 0)", "Feed (Perm 0)", "Feed (Perm 2)", "Feed (Perm 3)");
            for (int i = 0; i < idxes.length; i++) {
                if (idxes[i] < 0) {
                    continue;
                }
                if (canUpgrade) {
                    maxFeedableP += getCardCount(state, idxes[i]);
                } else {
                    maxFeedable += getCardCount(state, idxes[i]);
                }
                var curAction = state.getCurrentAction();
                if (curAction != null && curAction.type() == GameActionType.PLAY_CARD && curAction.idx() == idxes[i]) {
                    maxFeedable += 1;
                }
            }
            state.properties.findCardIndex(idxes, "Feed+", "Feed+ (Tmp 0)", "Feed+ (Perm 0)", "Feed+ (Perm 2)", "Feed+ (Perm 3)");
            for (int i = 0; i < idxes.length; i++) {
                if (idxes[i] < 0) {
                    continue;
                }
                maxFeedableP += getCardCount(state, idxes[i]);
                var curAction = state.getCurrentAction();
                if (curAction != null && curAction.type() == GameActionType.PLAY_CARD && curAction.idx() == idxes[i]) {
                    maxFeedableP += 4;
                }
            }

            state.properties.findCardIndex(idxes, "Exhume", "Exhume (Tmp 0)", "Exhume (Perm 0)", "Exhume (Perm 2)", "Exhume (Perm 3)");
            var exhumableFeeds = 0;
            for (int i = 0; i < idxes.length; i++) {
                if (idxes[i] > 0) {
                    exhumableFeeds += getCardCount(state, idxes[i]);
                }
            }
            state.properties.findCardIndex(idxes, "Exhume+", "Exhume+ (Tmp 0)", "Exhume+ (Perm 0)", "Exhume+ (Perm 2)", "Exhume+ (Perm 3)");
            for (int i = 0; i < idxes.length; i++) {
                if (idxes[i] > 0) {
                    exhumableFeeds += getCardCount(state, idxes[i]);
                }
            }

            if (canUpgrade) {
                maxFeedableP += exhumableFeeds;
            } else {
                maxFeedable += exhumableFeeds;
            }
            int enemiesAlive = 0;
            for (EnemyReadOnly enemy : state.getEnemiesForRead()) {
                if (!enemy.properties.isMinion) {
                    enemiesAlive++;
                }
            }
            remain += Math.min(enemiesAlive, maxFeedableP) * 4;
            if (enemiesAlive > maxFeedableP) {
                remain += Math.min(enemiesAlive - maxFeedableP, maxFeedable) * 3;
            }
            return remain;
        }
    }

    public static class Feed extends _FeedT {
        public Feed() {
            super("Feed", 10, 3, 2);
        }

        public Feed(double healthRewardRatio) {
            super("Feed", 10, 3, healthRewardRatio);
        }

        public Card getUpgrade() {
            return new CardIronclad.FeedP(healthRewardRatio);
        }
    }

    public static class FeedP extends _FeedT {
        public FeedP() {
            super("Feed+", 12, 4, 2);
        }

        public FeedP(double healthRewardRatio) {
            super("Feed+", 12, 4, healthRewardRatio);
        }
    }

    private static abstract class _FiendFireT extends Card {
        private final int damagePerCard;

        public _FiendFireT(String cardName, int damagePerCard) {
            super(cardName, Card.ATTACK, 2, Card.RARE);
            this.damagePerCard = damagePerCard;
            selectEnemy = true;
            exhaustWhenPlayed = true;
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int c = state.handArrLen;
            for (int i = 0; i < c; i++) {
                state.exhaustCardFromHandByPosition(i, false);
            }
            state.updateHandArr();
            for (int i = 0; i < c; i++) {
                if (state.getEnemiesForWrite().get(idx).isAlive()) {
                    state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damagePerCard);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FiendFire extends _FiendFireT {
        public FiendFire() {
            super("Fiend Fire", 7);
        }
    }

    public static class FiendFireP extends _FiendFireT {
        public FiendFireP() {
            super("Fiend Fire+", 10);
        }
    }

    private static abstract class _ImmolateT extends Card {
        private final int damage;

        public _ImmolateT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.RARE);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, damage);
            }
            state.addCardToDiscard(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardOther.Burn());
        }
    }

    public static class Immolate extends _ImmolateT {
        public Immolate() {
            super("Immolate", 21);
        }
    }

    public static class ImmolateP extends _ImmolateT {
        public ImmolateP() {
            super("Immolate+", 28);
        }
    }

    private static abstract class _ImperviousT extends Card {
        private final int block;

        public _ImperviousT(String cardName, int block) {
            super(cardName, Card.SKILL, 2, Card.RARE);
            this.block = block;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Impervious extends _ImperviousT {
        public Impervious() {
            super("Impervious", 30);
        }
    }

    public static class ImperviousP extends _ImperviousT {
        public ImperviousP() {
            super("Impervious+", 40);
        }
    }

    private static abstract class _JuggernautT extends Card {
        private final int n;

        public _JuggernautT(String cardName, int n) {
            super(cardName, Card.POWER, 2, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Juggernaut", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 14.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnBlockHandler("Juggernaut", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int i = 0;
                    if (state.enemiesAlive > 1) {
                        i = state.getSearchRandomGen().nextInt(state.enemiesAlive, RandomGenCtx.RandomEnemyJuggernaut, state);
                        state.setIsStochastic();
                    }
                    int enemy_j = 0;
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        if (i == enemy_j) {
                            state.playerDoNonAttackDamageToEnemy(enemy, state.getCounterForRead()[counterIdx], true);
                        }
                        enemy_j ++;
                    }
                }
            });
        }
    }

    public static class Juggernaut extends _JuggernautT {
        public Juggernaut() {
            super("Juggernaut", 5);
        }
    }

    public static class JuggernautP extends _JuggernautT {
        public JuggernautP() {
            super("Juggernaut+", 7);
        }
    }

    private static abstract class _LimitBreakT extends Card {
        public _LimitBreakT(String cardName, boolean exhaustWhenPlayedFlag) {
            super(cardName, Card.SKILL, 1, Card.RARE);
            changePlayerStrength = true;
            exhaustWhenPlayed = exhaustWhenPlayedFlag;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var player = state.getPlayerForWrite();
            player.gainStrength(player.getStrength());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class LimitBreak extends _LimitBreakT {
        public LimitBreak() {
            super("Limit Break", true);
        }
    }

    public static class LimitBreakP extends _LimitBreakT {
        public LimitBreakP() {
            super("Limit Break+", false);
        }
    }

    private static abstract class _OfferingT extends Card {
        private final int draw;

        public _OfferingT(String cardName, int draw) {
            super(cardName, Card.SKILL, 0, Card.RARE);
            this.draw = draw;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.doNonAttackDamageToPlayer(6, false, this);
            state.draw(draw);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Offering extends _OfferingT {
        public Offering() {
            super("Offering", 3);
        }
    }

    public static class OfferingP extends _OfferingT {
        public OfferingP() {
            super("Offering+", 5);
        }
    }

    private static abstract class _ReaperT extends Card {
        private final int damage;

        public _ReaperT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.RARE);
            this.damage = damage;
            healPlayer = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int amount = 0;
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                int prevHp = enemy.getHealth();
                state.playerDoDamageToEnemy(enemy, damage);
                amount += prevHp - enemy.getHealth();
            }
            state.healPlayer(amount);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Reaper extends _ReaperT {
        public Reaper() {
            super("Reaper", 4);
        }
    }

    public static class ReaperP extends _ReaperT {
        public ReaperP() {
            super("Reaper+", 5);
        }
    }
}
