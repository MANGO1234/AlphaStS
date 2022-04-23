package com.alphaStS;

import com.alphaStS.enemy.Enemy;

import java.util.List;
import java.util.Objects;

public abstract class Card implements GameProperties.CounterRegistrant {
    public static int ATTACK = 0;
    public static int SKILL = 1;
    public static int POWER = 2;
    public static int CURSE = 3;
    public static int STATUS = 4;

    public final int cardType;
    String cardName;
    int energyCost;
    public GameActionCtx secondActionCtx;
    public boolean ethereal = false;
    public boolean innate = false;
    public boolean exhaustWhenPlayed = false;
    public boolean exhaustNonAttacks = false;
    public boolean selectEnemy;
    public boolean selectFromDiscard;
    public boolean selectFromExhaust;
    public boolean selectFromHand;
    public boolean selectFromDiscardLater;
    public boolean selectFromHandLater;
    public boolean exhaustSkill;
    public boolean canExhaustAnyCard;
    public boolean changePlayerStrength;
    public boolean changePlayerStrengthEot;
    public boolean changePlayerDexterity;
    public boolean vulnEnemy;
    public boolean weakEnemy;
    public boolean affectEnemyStrength;
    public boolean affectEnemyStrengthEot;
    public boolean putCardOnTopDeck;
    public boolean healPlayer;
    int counterIdx = -1;

    public void setCounterIdx(GameProperties gameProperties, int idx) {
        counterIdx = idx;
    }

    public Card(String cardName, int cardType, int energyCost) {
        this.cardType = cardType;
        this.cardName = cardName;
        this.energyCost = energyCost;
    }

    public int energyCost(GameState state) {
        return energyCost;
    }

    GameActionCtx play(GameState state, int idx) { return GameActionCtx.PLAY_CARD; }
    void onExhaust(GameState state) {}
    List<Card> getPossibleGeneratedCards(List<Card> cards) { return List.of(); }
    public boolean canSelectFromHand(Card card) { return true; }
    public void startOfGameSetup(GameState state) {}

    @Override public String toString() {
        return "Card{" +
                "cardName='" + cardName + '\'' +
                '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Card card = (Card) o;
        return Objects.equals(cardName, card.cardName);
    }

    @Override public int hashCode() {
        return Objects.hash(cardName);
    }


    public static class Bash extends Card {
        public Bash() {
            super("Bash", Card.ATTACK, 2);
            selectEnemy = true;
            vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 8);
            enemy.applyDebuff(DebuffType.VULNERABLE, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BashP extends Card {
        public BashP() {
            super("Bash+", Card.ATTACK, 2);
            selectEnemy = true;
            vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 10);
            enemy.applyDebuff(DebuffType.VULNERABLE, 3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Strike extends Card {
        public Strike() {
            super("Strike", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 6);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class StrikeP extends Card {
        public StrikeP() {
            super("Strike+", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 9);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Defend extends Card {
        public Defend() {
            super("Defend", Card.SKILL, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(5);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DefendP extends Card {
        public DefendP() {
            super("Defend+", Card.SKILL, 1);
            energyCost = 1;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(8);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Anger extends Card {
        public Anger() {
            super("Anger", Card.ATTACK, 0);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 6);
            state.addCardToDiscard(state.prop.angerCardIdx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class AngerP extends Card {
        public AngerP() {
            super("Anger+", Card.ATTACK, 0);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 8);
            state.addCardToDiscard(state.prop.angerPCardIdx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Armanent extends Card {
        public Armanent() {
            super("Armanent", Card.SKILL, 1);
            selectFromHand = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(5);
            if (idx >= 0) {
                state.removeCardFromHand(idx);
                state.addCardToHand(state.prop.upgradeIdxes[idx]);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public boolean canSelectFromHand(Card card) {
            return CardUpgrade.map.get(card) != null;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return cards.stream().map((x) -> CardUpgrade.map.get(x)).filter(Objects::nonNull).toList();
        }
    }

    public static class ArmanentP extends Card {
        public ArmanentP() {
            super("Armanent+", Card.SKILL, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(5);
            for (int i = 0; i < state.prop.upgradeIdxes.length; i++) {
                if (state.hand[i] > 0 && state.prop.upgradeIdxes[i] >= 0) {
                    state.hand[state.prop.upgradeIdxes[i]] += state.hand[i];
                    state.hand[i] = 0;
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return cards.stream().map((x) -> CardUpgrade.map.get(x)).filter(Objects::nonNull).toList();
        }
    }

    public static class BodySlam extends Card {
        public BodySlam() {
            super("Body Slam", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), state.getPlayeForRead().getBlock());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BodySlamP extends Card {
        public BodySlamP() {
            super("Body Slam+", Card.ATTACK, 0);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), state.getPlayeForRead().getBlock());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Clash extends Card {
        public Clash() {
            super("Clash", Card.ATTACK, -1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 14);
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            for (int i = 0; i < state.hand.length; i++) {
                if (state.hand[i] > 0 && state.prop.cardDict[i].cardType != Card.ATTACK) {
                    return -1;
                }
            }
            return 0;
        }
    }

    public static class ClashP extends Card {
        public ClashP() {
            super("Clash+", Card.ATTACK, -1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 18);
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            for (int i = 0; i < state.hand.length; i++) {
                if (state.hand[i] > 0 && state.prop.cardDict[i].cardType != Card.ATTACK) {
                    return -1;
                }
            }
            return 0;
        }
    }

    public static class Cleave extends Card {
        public Cleave() {
            super("Cleave", Card.ATTACK, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, 8);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class CleaveP extends Card {
        public CleaveP() {
            super("Cleave+", Card.ATTACK, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, 11);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Clothesline extends Card {
        public Clothesline() {
            super("Clothesline", Card.ATTACK, 2);
            selectEnemy = true;
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 12);
            enemy.applyDebuff(DebuffType.WEAK, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ClotheslineP extends Card {
        public ClotheslineP() {
            super("Clothesline+", Card.ATTACK, 2);
            selectEnemy = true;
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 14);
            enemy.applyDebuff(DebuffType.WEAK, 3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Flex extends Card {
        public Flex() {
            super("Flex", Card.SKILL, 0);
            changePlayerStrength = true;
            changePlayerStrengthEot = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            var player = state.getPlayerForWrite();
            player.gainStrength(2);
            player.applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FlexP extends Card {
        public FlexP() {
            super("Flex+", Card.SKILL, 0);
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            var player = state.getPlayerForWrite();
            player.gainStrength(4);
            player.applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, 4);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // todo: test
    private static abstract class _HavocT extends Card {
        public _HavocT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost);
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            int cardIdx = state.drawOneCardSpecial();
            var _this = this;
            state.addGameActionToEndOfDeque(curState -> {
                var action = curState.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                state.playCard(action, -1, false, false);
            });
            state.addGameActionToEndOfDeque(curState -> {
                state.exhaustedCardHandle(cardIdx, true);
            });
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Havoc extends _HavocT {
        public Havoc(String cardName, int cardType, int energyCost) {
            super("Havoc", Card.SKILL, 1);
        }
    }

    public static class HavocP extends _HavocT {
        public HavocP(String cardName, int cardType, int energyCost) {
            super("Havoc+", Card.SKILL, 0);
        }
    }

    public static class Headbutt extends Card {
        public Headbutt() {
            super("Headbutt", Card.ATTACK, 1);
            selectEnemy = true;
            selectFromDiscard = true;
            selectFromDiscardLater = true;
            secondActionCtx = GameActionCtx.SELECT_CARD_DISCARD;
            putCardOnTopDeck = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 9);
                return GameActionCtx.SELECT_CARD_DISCARD;
            } else {
                state.discard[idx] -= 1;
                state.putCardOnTopOfDeck(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class HeadbuttP extends Card {
        public HeadbuttP() {
            super("Headbutt+", Card.ATTACK, 1);
            selectEnemy = true;
            selectFromDiscard = true;
            selectFromDiscardLater = true;
            secondActionCtx = GameActionCtx.SELECT_CARD_DISCARD;
        }

        public GameActionCtx play(GameState state, int idx) {
            if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 9);
                return GameActionCtx.SELECT_CARD_DISCARD;
            } else {
                state.discard[idx] -= 1;
                state.putCardOnTopOfDeck(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class HeavyBlade extends Card {
        public HeavyBlade() {
            super("Heavy Blade", Card.ATTACK, 2);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 14 + state.getPlayeForRead().getStrength() * 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class HeavyBladeP extends Card {
        public HeavyBladeP() {
            super("Heavy Blade+", Card.ATTACK, 2);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 14 + state.getPlayeForRead().getStrength() * 4);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class IronWave extends Card {
        public IronWave() {
            super("Iron Wave", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 5);
            state.getPlayerForWrite().gainBlock(5);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class IronWaveP extends Card {
        public IronWaveP() {
            super("Iron Wave+", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 7);
            state.getPlayerForWrite().gainBlock(7);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PerfectedStrike extends Card {
        public PerfectedStrike() {
            super("Perfected Strike", Card.ATTACK, 2);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            int count = 0;
            for (int i = 0; i < state.prop.strikeCardIdxes.length; i++) {
                count += state.hand[state.prop.strikeCardIdxes[i]];
                count += state.discard[state.prop.strikeCardIdxes[i]];
                count += state.deck[state.prop.strikeCardIdxes[i]];
            }
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 6 + 2 * count);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PerfectedStrikeP extends Card {
        public PerfectedStrikeP() {
            super("Perfected Strike+", Card.ATTACK, 2);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            int count = 0;
            for (int i = 0; i < state.prop.strikeCardIdxes.length; i++) {
                count += state.hand[state.prop.strikeCardIdxes[i]];
                count += state.discard[state.prop.strikeCardIdxes[i]];
                count += state.deck[state.prop.strikeCardIdxes[i]];
            }
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 6 + 3 * count);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PommelStrike extends Card {
        public PommelStrike() {
            super("Pommel Strike", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 9);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PommelStrikeP extends Card {
        public PommelStrikeP() {
            super("Pommel Strike+", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 10);
            state.draw(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ShrugItOff extends Card {
        public ShrugItOff() {
            super("Shrug It Off", Card.SKILL, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(8);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ShrugItOffP extends Card {
        public ShrugItOffP() {
            super("Shrug It Off+", Card.SKILL, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(11);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _SwordBoomerangT extends Card {
        private final int n;

        public _SwordBoomerangT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx) {
            for (int _i = 0; _i < n; _i++) {
                int enemy_j = state.prop.random.nextInt(state.enemiesAlive);
                int j = 0;
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    if (j == enemy_j) {
                        state.playerDoDamageToEnemy(enemy, 3);
                        break;
                    }
                    enemy_j += 1;
                }
            }
            if (state.enemiesAlive > 1) {
                state.isStochastic = true;
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SwordBoomerang extends _SwordBoomerangT {
        public SwordBoomerang() {
            super("Sword Boomerang", Card.ATTACK, 1, 3);
        }
    }

    public static class SwordBoomerangP extends _SwordBoomerangT {
        public SwordBoomerangP() {
            super("Sword Boomerang+", Card.ATTACK, 1, 4);
        }
    }

    public static class Thunderclap extends Card {
        public Thunderclap() {
            super("Thunderclap", Card.ATTACK, 1);
            vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, 4);
                enemy.applyDebuff(DebuffType.VULNERABLE, 1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ThunderclapP extends Card {
        public ThunderclapP() {
            super("Thunderclap+", Card.ATTACK, 1);
            vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, 7);
                enemy.applyDebuff(DebuffType.VULNERABLE, 1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class TrueGrit extends Card {
        public TrueGrit() {
            super("True Grit", Card.SKILL, 1);
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(7);
            int c = 0;
            int diffCards = 0;
            for (int cardIdx = 0; cardIdx < state.hand.length; cardIdx++) {
                c += state.hand[cardIdx];
                diffCards += state.hand[cardIdx] > 0 ? 1 : 0;
            }
            int r = state.prop.random.nextInt(c);
            for (int cardIdx = 0; cardIdx < state.hand.length; cardIdx++) {
                if (r <= 0) {
                    state.exhaustCardFromHand(cardIdx);
                }
                r -= state.hand[cardIdx];
            }
            if (diffCards > 1) {
                state.isStochastic = true;
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class TrueGritP extends Card {
        public TrueGritP() {
            super("True Grit+", Card.SKILL, 1);
            selectFromHand = true;
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(9);
            state.exhaustCardFromHand(idx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class TwinStrike extends Card {
        public TwinStrike() {
            super("Twin Strike", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 5);
            state.playerDoDamageToEnemy(enemy, 5);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class TwinStrikeP extends Card {
        public TwinStrikeP() {
            super("Twin Strike+", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 7);
            state.playerDoDamageToEnemy(enemy, 7);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class WarCry extends Card {
        public WarCry() {
            super("War Cry", Card.SKILL, 0);
            selectFromHand = true;
            selectFromHandLater = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.draw(1);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.removeCardFromHand(idx);
                state.putCardOnTopOfDeck(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class WarCryP extends Card {
        public WarCryP() {
            super("War Cry+", Card.SKILL, 0);
            selectFromHand = true;
            selectFromHandLater = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.draw(2);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.removeCardFromHand(idx);
                state.putCardOnTopOfDeck(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class WildStrike extends Card {
        public WildStrike() {
            super("Wild Strike", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 12);
            state.addCardToDeck(state.prop.woundCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new Wound());
        }
    }

    public static class WildStrikeP extends Card {
        public WildStrikeP() {
            super("Wild Strike+", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 12);
            state.addCardToDeck(state.prop.woundCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new Wound());
        }
    }

    public static class BattleTrance extends Card {
        public BattleTrance() {
            super("Battle Trance", Card.SKILL, 0);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.draw(3);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.NO_MORE_CARD_DRAW, 1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BattleTranceP extends Card {
        public BattleTranceP() {
            super("Battle Trance+", Card.SKILL, 0);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.draw(4);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.NO_MORE_CARD_DRAW, 1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BloodForBlood extends Card {
        public BloodForBlood() {
            this(4);
        }

        private BloodForBlood(int energyCost) {
            super("Blood For Blood (" + energyCost + ")", Card.ATTACK, energyCost);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 18);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new BloodForBlood(3), new BloodForBlood(2), new BloodForBlood(1), new BloodForBlood(0));
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.bloodForBloodIndexes = new int[5];
            for (int i = 0; i < 5; i++) {
                state.prop.bloodForBloodIndexes[i] = state.prop.findCardIndex(new BloodForBlood(i));
            }
            state.addOnDamageHandler("Blood For Blood", new OnDamageHandler() {
                @Override void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (damageDealt <= 0) return;
                    for (int i = 0; i < 4; i++) {
                        state.deck[state.prop.bloodForBloodIndexes[i]] += state.deck[state.prop.bloodForBloodIndexes[i + 1]];
                        state.deck[state.prop.bloodForBloodIndexes[i + 1]] = 0;
                        state.hand[state.prop.bloodForBloodIndexes[i]] += state.hand[state.prop.bloodForBloodIndexes[i + 1]];
                        state.hand[state.prop.bloodForBloodIndexes[i + 1]] = 0;
                        state.discard[state.prop.bloodForBloodIndexes[i]] += state.discard[state.prop.bloodForBloodIndexes[i + 1]];
                        state.discard[state.prop.bloodForBloodIndexes[i + 1]] = 0;
                        var exhaust = state.getExhaustForWrite();
                        exhaust[state.prop.bloodForBloodIndexes[i]] += exhaust[state.prop.bloodForBloodIndexes[i + 1]];
                        exhaust[state.prop.bloodForBloodIndexes[i + 1]] = 0;
                    }
                }
            });
        }
    }

    public static class BloodForBloodP extends Card {
        public BloodForBloodP() {
            this(3);
        }

        private BloodForBloodP(int energyCost) {
            super("Blood For Blood+ (" + energyCost + ")", Card.ATTACK, energyCost);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 22);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new BloodForBloodP(2), new BloodForBloodP(1), new BloodForBloodP(0));
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.bloodForBloodPIndexes = new int[4];
            for (int i = 0; i < 4; i++) {
                state.prop.bloodForBloodPIndexes[i] = state.prop.findCardIndex(new BloodForBloodP(i));
            }
            state.addOnDamageHandler("Blood For Blood+", new OnDamageHandler() {
                @Override void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (damageDealt <= 0) return;
                    for (int i = 0; i < 3; i++) {
                        state.deck[state.prop.bloodForBloodPIndexes[i]] += state.deck[state.prop.bloodForBloodPIndexes[i + 1]];
                        state.deck[state.prop.bloodForBloodPIndexes[i + 1]] = 0;
                        state.hand[state.prop.bloodForBloodPIndexes[i]] += state.hand[state.prop.bloodForBloodPIndexes[i + 1]];
                        state.hand[state.prop.bloodForBloodPIndexes[i + 1]] = 0;
                        state.discard[state.prop.bloodForBloodPIndexes[i]] += state.discard[state.prop.bloodForBloodPIndexes[i + 1]];
                        state.discard[state.prop.bloodForBloodPIndexes[i + 1]] = 0;
                        var exhaust = state.getExhaustForWrite();
                        exhaust[state.prop.bloodForBloodPIndexes[i]] += exhaust[state.prop.bloodForBloodPIndexes[i + 1]];
                        exhaust[state.prop.bloodForBloodPIndexes[i + 1]] = 0;
                    }
                }
            });
        }
    }

    public static class Bloodletting extends Card {
        public Bloodletting() {
            super("Bloodletting", Card.SKILL, 0);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.doNonAttackDamageToPlayer(3, false, this);
            state.gainEnergy(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BloodlettingP extends Card {
        public BloodlettingP() {
            super("Bloodletting+", Card.SKILL, 0);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.doNonAttackDamageToPlayer(3, false, this);
            state.gainEnergy(3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BurningPact extends Card {
        public BurningPact() {
            super("Burning Pact", Card.SKILL, 1);
            selectFromHand =  true;
            selectFromHandLater = true;
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.draw(2);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.exhaustCardFromHand(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class BurningPactP extends Card {
        public BurningPactP() {
            super("Burning Pact+", Card.SKILL, 1);
            selectFromHand =  true;
            selectFromHandLater = true;
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.draw(3);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.exhaustCardFromHand(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class Carnage extends Card {
        public Carnage() {
            super("Carnage", Card.ATTACK, 2);
            selectEnemy = true;
            ethereal = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 20);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class CarnageP extends Card {
        public CarnageP() {
            super("Carnage+", Card.ATTACK, 2);
            selectEnemy = true;
            ethereal = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 28);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _CombustT extends Card {
        private final int n;

        public _CombustT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx) {
            // we split counter into two component, 1 for self damage, and 1 for enemy damage
            state.getCounterForWrite()[counterIdx] += 1 << 16;
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            var _this = this;
            state.prop.registerCounter(this.cardName, this, new GameProperties.NetworkInputHandler() {
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
            state.addPreEndOfTurnHandler(this.cardName, new GameEventHandler() {
                @Override void handle(GameState state) {
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
            super("Combust", Card.POWER, 1, 5);
        }
    }

    public static class CombustP extends _CombustT {
        public CombustP() {
            super("Combust+", Card.POWER, 1, 7);
        }
    }

    public static class DarkEmbrace extends Card {
        public DarkEmbrace() {
            super("Dark Embrace", Card.POWER, 2);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("DarkEmbrace", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 2.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnExhaustHandler("DarkEmbrace", new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.draw(state.getCounterForRead()[counterIdx]);
                }
            });
        }
    }

    public static class DarkEmbraceP extends Card {
        public DarkEmbraceP() {
            super("Dark Embrace+", Card.POWER, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("DarkEmbrace", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 2.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnExhaustHandler("DarkEmbrace", new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.draw(state.getCounterForRead()[counterIdx]);
                }
            });
        }
    }

    public static class Disarm extends Card {
        public Disarm() {
            super("Disarm", Card.SKILL, 1);
            exhaustWhenPlayed = true;
            selectEnemy = true;
            affectEnemyStrength = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(DebuffType.LOSE_STRENGTH, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DisarmP extends Card {
        public DisarmP() {
            super("Disarm+", Card.SKILL, 1);
            exhaustWhenPlayed = true;
            selectEnemy = true;
            affectEnemyStrength = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(DebuffType.LOSE_STRENGTH, 3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Dropkick extends Card {
        public Dropkick() {
            super("Dropkick", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 5);
            if (enemy.getVulnerable() > 0) {
                state.energy += 1;
                state.draw(1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DropkickP extends Card {
        public DropkickP() {
            super("Dropkick+", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 8);
            if (enemy.getVulnerable() > 0) {
                state.energy += 1;
                state.draw(1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DualWield extends Card {
        public DualWield() {
            super("Dual Wield", Card.SKILL, 1);
            selectFromHand = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.addCardToHand(idx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public boolean canSelectFromHand(Card card) {
            return card.cardType == Card.ATTACK;
        }

        @Override List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return cards.stream().filter(card -> card.cardType == Card.ATTACK).toList();
        }
    }

    public static class DualWieldP extends Card {
        public DualWieldP() {
            super("Dual Wield+", Card.SKILL, 1);
            selectFromHand = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.addCardToHand(idx);
            state.addCardToHand(idx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public boolean canSelectFromHand(Card card) {
            return card.cardType == Card.ATTACK;
        }

        @Override List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return cards.stream().filter(card -> card.cardType == Card.ATTACK).toList();
        }
    }

    public static class Entrench extends Card {
        public Entrench() {
            super("Entrench", Card.SKILL, 2);
        }

        public GameActionCtx play(GameState state, int idx) {
            var player = state.getPlayerForWrite();
            player.gainBlockNotFromCardPlay(player.getBlock());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class EntrenchP extends Card {
        public EntrenchP() {
            super("Entrench+", Card.SKILL, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            var player = state.getPlayerForWrite();
            player.gainBlockNotFromCardPlay(player.getBlock());
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _EvolveT extends Card {
        private final int n;

        public _EvolveT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Evolve", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardDrawnHandler("Evolve", new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card) {
                    if (card.cardType == Card.STATUS) {
                        state.draw(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }
    }

    public static class Evolve extends _EvolveT {
        public Evolve() {
            super("Evolve", Card.POWER, 1, 1);
        }
    }

    public static class EvolveP extends _EvolveT {
        public EvolveP() {
            super("Evolve+", Card.POWER, 1, 2);
        }
    }

    public static class FeelNoPain extends Card {
        public FeelNoPain() {
            super("Feel No Pain", Card.POWER, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += 3;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("FNP", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 8.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnExhaustHandler("FNP", new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.getPlayerForWrite().gainBlockNotFromCardPlay(state.getCounterForRead()[counterIdx]);
                }
            });
        }
    }

    public static class FeelNoPainP extends Card {
        public FeelNoPainP() {
            super("Feel No Pain+", Card.POWER, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += 4;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("FNP", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 8.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnExhaustHandler("FNP", new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.getPlayerForWrite().gainBlockNotFromCardPlay(state.getCounterForRead()[counterIdx]);
                }
            });
        }
    }

    private abstract static class _FireBreathingT extends Card {
        private final int n;

        public _FireBreathingT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("FireBreathing", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardDrawnHandler("FireBreathing", new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card) {
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
            super("Fire Breathing", Card.POWER, 1, 6);
        }
    }

    public static class FireBreathingP extends _FireBreathingT {
        public FireBreathingP() {
            super("Fire Breathing+", Card.POWER, 1, 10);
        }
    }

    public static class FlameBarrier extends Card {
        public FlameBarrier() {
            super("Flame Barrier", Card.SKILL, 2);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(12);
            state.getCounterForWrite()[counterIdx] += 4;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("FlameBarrier", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 12.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnDamageHandler("FlameBarrier", new OnDamageHandler() {
                @Override void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (isAttack && source instanceof Enemy enemy) {
                        state.playerDoNonAttackDamageToEnemy(enemy, state.getCounterForRead()[counterIdx], true);
                    }
                }
            });
            state.addStartOfTurnHandler("FlameBarrier", new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    public static class FlameBarrierP extends Card {
        public FlameBarrierP() {
            super("Flame Barrier+", Card.SKILL, 2);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(16);
            state.getCounterForWrite()[counterIdx] += 6;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("FlameBarrier", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 12.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnDamageHandler("FlameBarrier", new OnDamageHandler() {
                @Override void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (isAttack && source instanceof Enemy enemy) {
                        state.playerDoNonAttackDamageToEnemy(enemy, state.getCounterForRead()[counterIdx], true);
                    }
                }
            });
            state.addStartOfTurnHandler("FlameBarrier", new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    public static class GhostlyArmor extends Card {
        public GhostlyArmor() {
            super("Ghostly Armor", Card.SKILL, 1);
            ethereal = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(10);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class GhostlyArmorP extends Card {
        public GhostlyArmorP() {
            super("Ghostly Armor+", Card.SKILL, 1);
            ethereal = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(13);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Hemokinesis extends Card {
        public Hemokinesis() {
            super("Hemokinesis", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.doNonAttackDamageToPlayer(2, false, this);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 15);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class HemokinesisP extends Card {
        public HemokinesisP() {
            super("Hemokinesis+", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.doNonAttackDamageToPlayer(2, false, this);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 20);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private abstract static class _InfernalBladeT extends Card {
        public _InfernalBladeT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            var r = state.prop.random.nextInt(state.prop.infernalBladeIndexes.length);
            state.addCardToHand(state.prop.infernalBladeIndexes[r]);
            state.isStochastic = true;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            if (state.prop.infernalBladeIndexes == null) {
                state.prop.infernalBladeIndexes = new int[29];
                state.prop.infernalBladeIndexes[0] = state.prop.findCardIndex(new Card.Anger());
                state.prop.infernalBladeIndexes[1] = state.prop.findCardIndex(new Card.BodySlam());
                state.prop.infernalBladeIndexes[2] = state.prop.findCardIndex(new Card.Clash());
                state.prop.infernalBladeIndexes[3] = state.prop.findCardIndex(new Card.Cleave());
                state.prop.infernalBladeIndexes[4] = state.prop.findCardIndex(new Card.Clothesline());
                state.prop.infernalBladeIndexes[5] = state.prop.findCardIndex(new Card.Headbutt());
                state.prop.infernalBladeIndexes[6] = state.prop.findCardIndex(new Card.HeavyBlade());
                state.prop.infernalBladeIndexes[7] = state.prop.findCardIndex(new Card.IronWave());
                state.prop.infernalBladeIndexes[8] = state.prop.findCardIndex(new Card.PerfectedStrike());
                state.prop.infernalBladeIndexes[9] = state.prop.findCardIndex(new Card.PommelStrike());
                state.prop.infernalBladeIndexes[10] = state.prop.findCardIndex(new Card.SwordBoomerang());
                state.prop.infernalBladeIndexes[11] = state.prop.findCardIndex(new Card.Thunderclap());
                state.prop.infernalBladeIndexes[12] = state.prop.findCardIndex(new Card.TwinStrike());
                state.prop.infernalBladeIndexes[13] = state.prop.findCardIndex(new Card.WildStrike());
                state.prop.infernalBladeIndexes[14] = state.prop.findCardIndex(new Card.BloodForBlood());
                state.prop.infernalBladeIndexes[15] = state.prop.findCardIndex(new Card.Carnage());
                state.prop.infernalBladeIndexes[16] = state.prop.findCardIndex(new Card.Dropkick());
                state.prop.infernalBladeIndexes[17] = state.prop.findCardIndex(new Card.Hemokinesis());
                state.prop.infernalBladeIndexes[18] = state.prop.findCardIndex(new Card.Pummel());
                state.prop.infernalBladeIndexes[19] = state.prop.findCardIndex(new Card.Rampage());
                state.prop.infernalBladeIndexes[20] = state.prop.findCardIndex(new Card.RecklessCharge());
                state.prop.infernalBladeIndexes[21] = state.prop.findCardIndex(new Card.SearingBlow(0));
                state.prop.infernalBladeIndexes[22] = state.prop.findCardIndex(new Card.SeverSoul());
                state.prop.infernalBladeIndexes[23] = state.prop.findCardIndex(new Card.Uppercut());
                state.prop.infernalBladeIndexes[24] = state.prop.findCardIndex(new Card.Whirlwind());
                state.prop.infernalBladeIndexes[25] = state.prop.findCardIndex(new Card.Bludgeon());
                state.prop.infernalBladeIndexes[26] = state.prop.findCardIndex(new Card.FiendFire());
                state.prop.infernalBladeIndexes[27] = state.prop.findCardIndex(new Card.Immolate());
            }
        }

        @Override public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new Card.Anger(), new Card.BodySlam(), new Card.Clash(), new Card.Cleave(), new Card.Clothesline(), new Card.Headbutt(),
                    new Card.HeavyBlade(), new Card.IronWave(), new Card.PerfectedStrike(), new Card.PommelStrike(), new Card.SwordBoomerang(),
                    new Card.Thunderclap(), new Card.TwinStrike(), new Card.WildStrike(), new Card.BloodForBlood(), new Card.Carnage(),
                    new Card.Dropkick(), new Card.Hemokinesis(), new Card.Pummel(), new Card.Rampage(), new Card.RecklessCharge(),
                    new Card.SearingBlow(0), new Card.SeverSoul(), new Card.Uppercut(), new Card.Whirlwind(),
                    new Card.Bludgeon(), new Card.FiendFire(), new Card.Immolate());
        }
    }

    public static class InfernalBlade extends _InfernalBladeT {
        public InfernalBlade() {
            super("Infernal Blade", Card.SKILL, 1);
        }
    }

    public static class InfernalBladeP extends _InfernalBladeT {
        public InfernalBladeP() {
            super("Infernal Blade+", Card.SKILL, 0);
        }
    }

    public static class Inflame extends Card {
        public Inflame() {
            super("Inflame", Card.POWER, 1);
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainStrength(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class InflameP extends Card {
        public InflameP() {
            super("Inflame+", Card.POWER, 1);
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainStrength(3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Intimidate extends Card {
        public Intimidate() {
            super("Intimidate", Card.SKILL, 0);
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(DebuffType.WEAK, 1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class IntimidateP extends Card {
        public IntimidateP() {
            super("Intimidate+", Card.SKILL, 0);
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(DebuffType.WEAK, 2);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Metallicize extends Card {
        public Metallicize() {
            super("Metallicize", Card.POWER, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += 3;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Metallicize", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addPreEndOfTurnHandler("Metallicize", new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.getPlayerForWrite().gainBlockNotFromCardPlay(state.getCounterForRead()[counterIdx]);
                }
            });
        }
    }

    public static class MetallicizeP extends Card {
        public MetallicizeP() {
            super("Metallicize+", Card.POWER, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += 4;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Metallicize", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addPreEndOfTurnHandler("Metallicize", new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.getPlayerForWrite().gainBlockNotFromCardPlay(state.getCounterForRead()[counterIdx]);
                }
            });
        }
    }

    public static class PowerThrough extends Card {
        public PowerThrough() {
            super("Power Through", Card.SKILL, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(15);
            state.addCardToHand(state.prop.woundCardIdx);
            state.addCardToHand(state.prop.woundCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new Wound());
        }
    }

    public static class PowerThroughP extends Card {
        public PowerThroughP() {
            super("Power Through+", Card.SKILL, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(20);
            state.addCardToHand(state.prop.woundCardIdx);
            state.addCardToHand(state.prop.woundCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new Wound());
        }
    }

    public static class Pummel extends Card {
        public Pummel() {
            super("Pummel", Card.ATTACK, 1);
            selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            for (int i = 0; i < 4; i++) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 2);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PummelP extends Card {
        public PummelP() {
            super("Pummel+", Card.ATTACK, 1);
            selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            for (int i = 0; i < 5; i++) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 2);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _RageT extends Card {
        private final int n;

        public _RageT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Rage", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler("Rage", new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card) {
                    if (card.cardType == Card.ATTACK) {
                        state.getPlayerForWrite().gainBlock(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }
    }

    public static class Rage extends _RageT {
        public Rage() {
            super("Rage", Card.SKILL, 0, 3);
        }
    }

    public static class RageP extends _RageT {
        public RageP() {
            super("Rage+", Card.SKILL, 0, 5);
        }
    }

    // todo: need to think of a way to implement rampage (with card generation -> nn input being fixed)
    public static class Rampage extends Card {
        public Rampage() {
            super("Rampage", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 8);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class RampageP extends Card {
        public RampageP() {
            super("Rampage+", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 8);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class RecklessCharge extends Card {
        public RecklessCharge() {
            super("Reckless Charge", Card.ATTACK, 0);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 7);
            state.addCardToDeck(state.prop.dazedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new Dazed());
        }
    }

    public static class RecklessChargeP extends Card {
        public RecklessChargeP() {
            super("Reckless Charge+", Card.ATTACK, 0);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 10);
            state.addCardToDeck(state.prop.dazedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new Dazed());
        }
    }

    private static abstract class _RuptureT extends Card {
        private final int n;

        public _RuptureT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost);
            changePlayerStrength = true;
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Rupture", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 4.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            // todo: test
            state.addOnDamageHandler("Rupture", new OnDamageHandler() {
                @Override void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (damageDealt > 0 && source instanceof Card) {
                        state.getPlayerForWrite().gainStrength(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }
    }

    public static class Rupture extends _RuptureT {
        public Rupture() {
            super("Rupture", Card.POWER, 1, 1);
        }
    }

    public static class RuptureP extends _RuptureT {
        public RuptureP() {
            super("Rupture", Card.POWER, 1, 2);
        }
    }

    public static class SearingBlow extends Card {
        private final int n;

        public SearingBlow(int numberOfUpgrades) {
            super("Searing Blow", Card.ATTACK, 2);
            n = numberOfUpgrades;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n * (n + 7) / 2 + 12);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SecondWind extends Card {
        public SecondWind() {
            super("Second Wind", Card.SKILL, 1);
            exhaustNonAttacks = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            for (int i = 0; i < state.hand.length; i++) {
                if (state.hand[i] > 0 && state.prop.cardDict[i].cardType != Card.ATTACK) {
                    while (state.hand[i] > 0) {
                        state.exhaustCardFromHand(i);
                        state.getPlayerForWrite().gainBlock(5);
                    }
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SecondWindP extends Card {
        public SecondWindP() {
            super("Second Wind+", Card.SKILL, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            for (int i = 0; i < state.hand.length; i++) {
                if (state.hand[i] > 0 && state.prop.cardDict[i].cardType != Card.ATTACK) {
                    while (state.hand[i] > 0) {
                        state.exhaustCardFromHand(i);
                        state.getPlayerForWrite().gainBlock(5);
                    }
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SeeingRed extends Card {
        public SeeingRed() {
            super("Seeing Red", Card.SKILL, 1);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.gainEnergy(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SeeingRedP extends Card {
        public SeeingRedP() {
            super("Seeing Red+", Card.SKILL, 0);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.gainEnergy(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Sentinel extends Card {
        public Sentinel() {
            super("Sentinel", Card.SKILL, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(5);
            return GameActionCtx.PLAY_CARD;
        }

        public void onExhaust(GameState state) {
            state.energy += 2;
        }
    }

    public static class SentinelP extends Card {
        public SentinelP() {
            super("Sentinel+", Card.SKILL, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(8);
            return GameActionCtx.PLAY_CARD;
        }

        public void onExhaust(GameState state) {
            state.energy += 3;
        }
    }

    public static class SeverSoul extends Card {
        public SeverSoul() {
            super("Sever Soul", Card.ATTACK, 2);
            selectEnemy = true;
            exhaustNonAttacks = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            for (int i = 0; i < state.hand.length; i++) {
                if (state.hand[i] > 0 && state.prop.cardDict[i].cardType != Card.ATTACK) {
                    while (state.hand[i] > 0) {
                        state.exhaustCardFromHand(i);
                    }
                }
            }
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 16);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SeverSoulP extends Card {
        public SeverSoulP() {
            super("Sever Soul+", Card.ATTACK, 2);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            for (int i = 0; i < state.hand.length; i++) {
                if (state.hand[i] > 0 && state.prop.cardDict[i].cardType != Card.ATTACK) {
                    while (state.hand[i] > 0) {
                        state.exhaustCardFromHand(i);
                    }
                }
            }
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 22);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Shockwave extends Card {
        public Shockwave() {
            super("Shockwave", Card.SKILL, 2);
            exhaustWhenPlayed = true;
            vulnEnemy = true;
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(DebuffType.WEAK, 3);
                enemy.applyDebuff(DebuffType.VULNERABLE, 3);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ShockwaveP extends Card {
        public ShockwaveP() {
            super("Shockwave+", Card.SKILL, 2);
            exhaustWhenPlayed = true;
            vulnEnemy = true;
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(DebuffType.WEAK, 5);
                enemy.applyDebuff(DebuffType.VULNERABLE, 5);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SpotWeakness extends Card {
        public SpotWeakness() {
            super("Spot Weakness", Card.SKILL, 1);
            selectEnemy = true;
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            if (state.getEnemiesForRead().get(idx).getMoveString(state).contains("Attack")) {
                state.getPlayerForWrite().gainStrength(3);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SpotWeaknessP extends Card {
        public SpotWeaknessP() {
            super("Spot Weakness+", Card.SKILL, 1);
            selectEnemy = true;
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            if (state.getEnemiesForRead().get(idx).getMoveString(state).contains("Attack")) {
                state.getPlayerForWrite().gainStrength(4);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Uppercut extends Card {
        public Uppercut() {
            super("Uppercut", Card.ATTACK, 2);
            selectEnemy = true;
            vulnEnemy = true;
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 13);
            enemy.applyDebuff(DebuffType.WEAK, 1);
            enemy.applyDebuff(DebuffType.VULNERABLE, 1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class UppercutP extends Card {
        public UppercutP() {
            super("Uppercut+", Card.ATTACK, 2);
            selectEnemy = true;
            vulnEnemy = true;
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 13);
            enemy.applyDebuff(DebuffType.WEAK, 2);
            enemy.applyDebuff(DebuffType.VULNERABLE, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Whirlwind extends Card {
        public Whirlwind() {
            super("Whirlwind", Card.ATTACK, -1);
        }

        public GameActionCtx play(GameState state, int idx) {
            for (int i = 0; i < state.energy; i++) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    state.playerDoDamageToEnemy(enemy, 5);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            return state.energy;
        }
    }

    public static class WhirlwindP extends Card {
        public WhirlwindP() {
            super("Whirlwind+", Card.ATTACK, -1);
        }

        public GameActionCtx play(GameState state, int idx) {
            for (int i = 0; i < state.energy; i++) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    state.playerDoDamageToEnemy(enemy, 8);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            return state.energy;
        }
    }

    public static class Barricade extends Card {
        public Barricade() {
            super("Barricade", Card.POWER, 3);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.buffs |= PlayerBuff.BARRICADE.mask();
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BarricadeP extends Card {
        public BarricadeP() {
            super("Barricade+", Card.POWER, 2);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.buffs |= PlayerBuff.BARRICADE.mask();
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Berserk extends Card {
        public Berserk() {
            super("Berserk", Card.POWER, 0);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
            state.energyRefill += 1;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BerserkP extends Card {
        public BerserkP() {
            super("Berserk+", Card.POWER, 0);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 1);
            state.energyRefill += 1;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Bludgeon extends Card {
        public Bludgeon() {
            super("Bludgeon", Card.ATTACK, 3);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 32);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BludgeonP extends Card {
        public BludgeonP() {
            super("Bludgeon+", Card.ATTACK, 3);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 42);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _BrutalityT extends Card {
        public _BrutalityT(String cardName, int cardType, int energyCost, boolean innate) {
            super(cardName, cardType, energyCost);
            this.innate = innate;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += 1;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            var _this = this;
            state.prop.registerCounter("Brutality", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 2.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addStartOfTurnHandler("Brutality", new GameEventHandler() {
                @Override void handle(GameState state) {
                    int n = state.getCounterForRead()[counterIdx];
                    state.doNonAttackDamageToPlayer(n, false, _this);
                    state.draw(n);
                }
            });
        }
    }

    public static class Brutality extends _BrutalityT {
        public Brutality() {
            super("Brutality", Card.POWER, 0, false);
        }
    }

    public static class BrutalityP extends _BrutalityT {
        public BrutalityP() {
            super("Brutality+", Card.POWER, 0, true);
        }
    }

    public static class Corruption extends Card {
        public Corruption() {
            super("Corruption", Card.POWER, 3);
            exhaustSkill = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.buffs |= PlayerBuff.CORRUPTION.mask();
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class CorruptionP extends Card {
        public CorruptionP() {
            super("Corruption+", Card.POWER, 2);
            exhaustSkill = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.buffs |= PlayerBuff.CORRUPTION.mask();
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _DemonFormT extends Card {
        private final int n;

        public _DemonFormT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost);
            changePlayerStrength = true;
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("DemonForm", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 6.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addStartOfTurnHandler("DemonForm", new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.getPlayerForWrite().gainStrength(state.getCounterForRead()[counterIdx]);
                }
            });
        }
    }

    public static class DemonForm extends _DemonFormT {
        public DemonForm() {
            super("Demon Form", Card.POWER, 3, 2);
        }
    }

    public static class DemonFormP extends _DemonFormT {
        public DemonFormP() {
            super("Demon Form+", Card.POWER, 3, 3);
        }
    }

    private static abstract class _DoubleTapT extends Card {
        private final int n;

        public _DoubleTapT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("DemonForm", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = Math.abs(state.getCounterForRead()[counterIdx]) / 4.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler("DoubleTap", new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card) {
                    if (card.cardType != Card.ATTACK) {
                        return;
                    } else if (state.getCounterForRead()[counterIdx] == 0) {
                        return;
                    } else if (state.getCounterForRead()[counterIdx] < 0) {
                        var counters = state.getCounterForWrite();
                        counters[counterIdx] = -counters[counterIdx];
                        return;
                    }
                    var counters = state.getCounterForWrite();
                    counters[counterIdx] -= 1;
                    counters[counterIdx] = -counters[counterIdx]; // prevent double tap from duplicating the cloned attack
                    var enemyIdx = state.lastEnemySelected;
                    state.addGameActionToEndOfDeque(curState -> {
                        var cardIdx = curState.prop.findCardIndex(card);
                        var action = curState.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                        curState.playCard(action, enemyIdx, true, false);
                    });
                }
            });
        }
    }

    // todo: test
    public static class DoubleTap extends _DoubleTapT {
        public DoubleTap() {
            super("Double Tap", Card.SKILL, 1, 1);
        }
    }

    public static class DoubleTapP extends _DoubleTapT {
        public DoubleTapP() {
            super("Double Tap+", Card.SKILL, 1, 2);
        }
    }

    public static class Exhume extends Card {
        public Exhume() {
            super("Exhume", Card.SKILL, 1);
            selectFromExhaust = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getExhaustForWrite()[idx] -= 1;
            state.addCardToHand(idx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ExhumeP extends Card {
        public ExhumeP() {
            super("Exhume+", Card.SKILL, 0);
            selectFromExhaust = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getExhaustForWrite()[idx] -= 1;
            state.addCardToHand(idx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // todo: nn reward + on enemy death handler
    public static class Feed extends Card {
        public Feed() {
            super("Feed", Card.ATTACK, 1);
            healPlayer = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 10);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FeedP extends Card {
        public FeedP() {
            super("Feed+", Card.ATTACK, 1);
            healPlayer = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 12);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FiendFire extends Card {
        public FiendFire() {
            super("Fiend Fire", Card.ATTACK, 2);
            selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            for (int i = 0; i < state.hand.length; i++) {
                while (state.hand[i] > 0) {
                    state.exhaustCardFromHand(i);
                    state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 7);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FiendFireP extends Card {
        public FiendFireP() {
            super("Fiend Fire+", Card.ATTACK, 2);
            selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            for (int i = 0; i < state.hand.length; i++) {
                while (state.hand[i] > 0) {
                    state.exhaustCardFromHand(i);
                    state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 10);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Immolate extends Card {
        public Immolate() {
            super("Immolate", Card.ATTACK, 2);
        }

        public GameActionCtx play(GameState state, int idx) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, 21);
            }
            state.addCardToDiscard(state.prop.burnCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new Burn());
        }
    }

    public static class ImmolateP extends Card {
        public ImmolateP() {
            super("Immolate+", Card.ATTACK, 2);
        }

        public GameActionCtx play(GameState state, int idx) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, 28);
            }
            state.addCardToDiscard(state.prop.burnCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new Burn());
        }
    }

    public static class Impervious extends Card {
        public Impervious() {
            super("Impervious", Card.SKILL, 2);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(30);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ImperviousP extends Card {
        public ImperviousP() {
            super("Impervious+", Card.SKILL, 2);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getPlayerForWrite().gainBlock(40);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _JuggernautT extends Card {
        private final int n;

        public _JuggernautT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Juggernaut", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 14.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnBlockHandler("Juggernaut", new GameEventHandler() {
                @Override void handle(GameState state) {
                    int i = state.prop.random.nextInt(state.enemiesAlive);
                    int enemy_j = 0;
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        if (i == enemy_j) {
                            state.playerDoNonAttackDamageToEnemy(enemy, state.getCounterForRead()[counterIdx], true);
                        }
                        enemy_j ++;
                    }
                    if (state.enemiesAlive > 1) {
                        state.isStochastic = true;
                    }
                }
            });
        }
    }

    public static class Juggernaut extends _JuggernautT {
        public Juggernaut() {
            super("Juggernaut", Card.POWER, 2, 5);
        }
    }

    public static class JuggernautP extends _JuggernautT {
        public JuggernautP() {
            super("Juggernaut+", Card.POWER, 2, 7);
        }
    }

    public static class LimitBreak extends Card {
        public LimitBreak() {
            super("Limit Break", Card.SKILL, 1);
            exhaustWhenPlayed = true;
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            var player = state.getPlayerForWrite();
            player.gainStrength(player.getStrength());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class LimitBreakP extends Card {
        public LimitBreakP() {
            super("Limit Break+", Card.SKILL, 1);
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            var player = state.getPlayerForWrite();
            player.gainStrength(player.getStrength());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Offering extends Card {
        public Offering() {
            super("Offering", Card.SKILL, 0);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.doNonAttackDamageToPlayer(6, false, this);
            state.draw(3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class OfferingP extends Card {
        public OfferingP() {
            super("Offering+", Card.SKILL, 0);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.doNonAttackDamageToPlayer(6, false, this);
            state.draw(5);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Reaper extends Card {
        public Reaper() {
            super("Reaper", Card.ATTACK, 2);
            healPlayer = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            int amount = 0;
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                int prevHp = enemy.getHealth();
                state.playerDoDamageToEnemy(enemy, 4);
                amount += enemy.getHealth() - prevHp;
            }
            state.getPlayerForWrite().heal(amount);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ReaperP extends Card {
        public ReaperP() {
            super("Reaper+", Card.ATTACK, 2);
            healPlayer = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            int amount = 0;
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                int prevHp = enemy.getHealth();
                state.playerDoDamageToEnemy(enemy, 5);
                amount += enemy.getHealth() - prevHp;
            }
            state.getPlayerForWrite().heal(amount);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // **********************************************************************************************************
    // ********************************************* Statuses ***************************************************
    // **********************************************************************************************************

    public static class Burn extends Card {
        public Burn() {
            super("Burn", Card.STATUS, -1);
        }

        @Override public void startOfGameSetup(GameState state) {
            var cardIndex = state.prop.findCardIndex(this);
            var _this = this;
            state.addPreEndOfTurnHandler(new GameEventHandler(1) {
                @Override void handle(GameState state) {
                    for (int i = 0; i < state.hand[cardIndex]; i++) {
                        state.doNonAttackDamageToPlayer(2, true, _this);
                    }
                }
            });
        }
    }

    public static class BurnP extends Card {
        public BurnP() {
            super("Burn+", Card.STATUS, -1);
        }

        @Override public void startOfGameSetup(GameState state) {
            var cardIndex = state.prop.findCardIndex(this);
            var _this = this;
            state.addPreEndOfTurnHandler(new GameEventHandler(1) {
                @Override void handle(GameState state) {
                    for (int i = 0; i < state.hand[cardIndex]; i++) {
                        state.doNonAttackDamageToPlayer(4, true, _this);
                    }
                }
            });
        }
    }

    public static class Dazed extends Card {
        public Dazed() {
            super("Dazed", Card.STATUS, -1);
            ethereal = true;
        }
    }

    public static class Slime extends Card {
        public Slime() {
            super("Slime", Card.STATUS, 1);
            exhaustWhenPlayed = true;
        }
    }

    public static class Wound extends Card {
        public Wound() {
            super("Wound", Card.STATUS, -1);
        }
    }

    public static class Void extends Card {
        public Void() {
            super("Void", Card.STATUS, -1);
        }

        @Override public void startOfGameSetup(GameState state) {
            state.addOnCardDrawnHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card) {
                    if (card instanceof Card.Void) {
                        state.gainEnergy(-1);
                    }
                }
            });
        }
    }

    // **********************************************************************************************************
    // ********************************************** Curses ****************************************************
    // **********************************************************************************************************

    public static class AscendersBane extends Card {
        public AscendersBane() {
            super("Ascender's Bane", Card.CURSE, -1);
            ethereal = true;
            exhaustWhenPlayed = true;
        }
    }

    public static class Clumsy extends Card {
        public Clumsy() {
            super("Clumsy", Card.CURSE, -1);
            ethereal = true;
            exhaustWhenPlayed = true;
        }
    }

    public static class Necronomicurse extends Card {
        public Necronomicurse() {
            super("Necronomicurse", Card.CURSE, -1);
            exhaustWhenPlayed = true;
        }

        int cardIndex = -1;

        @Override public void startOfGameSetup(GameState state) {
            cardIndex = state.prop.findCardIndex(this);
        }

        @Override void onExhaust(GameState state) {
            state.addCardToHand(cardIndex);
        }
    }

    public static class Decay extends Card {
        public Decay() {
            super("Decay", Card.CURSE, -1);
            exhaustWhenPlayed = true;
        }

        @Override public void startOfGameSetup(GameState state) {
            var cardIndex = state.prop.findCardIndex(this);
            var _this = this;
            state.addPreEndOfTurnHandler(new GameEventHandler(1) {
                @Override void handle(GameState state) {
                    for (int i = 0; i < state.hand[cardIndex]; i++) {
                        state.doNonAttackDamageToPlayer(2, true, _this);
                    }
                }
            });
        }
    }

    public static class Doubt extends Card {
        public Doubt() {
            super("Doubt", Card.CURSE, -1);
            exhaustWhenPlayed = true;
        }

        @Override public void startOfGameSetup(GameState state) {
            var cardIndex = state.prop.findCardIndex(this);
            state.addPreEndOfTurnHandler(new GameEventHandler(1) {
                @Override void handle(GameState state) {
                    for (int i = 0; i < state.hand[cardIndex]; i++) {
                        state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
                    }
                }
            });
        }
    }

    public static class Normality extends Card {
        public Normality() {
            super("Normality", Card.CURSE, -1);
            exhaustWhenPlayed = true;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Normality", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addOnCardPlayedHandler("Normality", new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card) {
                    state.getCounterForWrite()[counterIdx] += 1;
                }
            });
            state.addPreEndOfTurnHandler("Normality", new GameEventHandler() {
                @Override void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }

        public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.normalityCounterIdx = idx;
        }
    }

    public static class Pain extends Card {
        public Pain() {
            super("Pain", Card.CURSE, -1);
            exhaustWhenPlayed = true;
        }

        @Override public void startOfGameSetup(GameState state) {
            var cardIndex = state.prop.findCardIndex(this);
            var _this = this;
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card) {
                    for (int i = 0; i < state.hand[cardIndex]; i++) {
                        state.doNonAttackDamageToPlayer(1, false, _this);
                    }
                }
            });
        }
    }

    public static class Regret extends Card {
        public Regret() {
            super("Regret", Card.CURSE, -1);
            exhaustWhenPlayed = true;
        }

        @Override public void startOfGameSetup(GameState state) {
            var cardIndex = state.prop.findCardIndex(this);
            var _this = this;
            state.addPreEndOfTurnHandler(new GameEventHandler(1) {
                @Override void handle(GameState state) {
                    int numOfCards = 0;
                    for (int n : state.hand) {
                        numOfCards += n;
                    }
                    for (int i = 0; i < state.hand[cardIndex]; i++) {
                        state.doNonAttackDamageToPlayer(numOfCards, false, _this);
                    }
                }
            });
        }
    }

    public static class Shame extends Card {
        public Shame() {
            super("Shame", Card.CURSE, -1);
            exhaustWhenPlayed = true;
        }

        @Override public void startOfGameSetup(GameState state) {
            var cardIndex = state.prop.findCardIndex(this);
            state.addPreEndOfTurnHandler(new GameEventHandler(1) {
                @Override void handle(GameState state) {
                    for (int i = 0; i < state.hand[cardIndex]; i++) {
                        state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 1);
                    }
                }
            });
        }
    }

    public static class Writhe extends Card {
        public Writhe() {
            super("Writhe", Card.CURSE, -1);
            exhaustWhenPlayed = true;
            innate = true;
        }
    }

    public static class Parasite extends Card {
        public Parasite() {
            super("Parasite", Card.CURSE, -1);
            exhaustWhenPlayed = true;
        }
    }

    public static class Injury extends Card {
        public Injury() {
            super("Injury", Card.CURSE, -1);
            exhaustWhenPlayed = true;
        }
    }

    public static class CurseOfTheBell extends Card {
        public CurseOfTheBell() {
            super("Curse of The Bell", Card.CURSE, -1);
            exhaustWhenPlayed = true;
        }
    }
}
