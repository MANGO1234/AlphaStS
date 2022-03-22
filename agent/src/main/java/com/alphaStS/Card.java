package com.alphaStS;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

abstract class Card implements GameProperties.CounterRegistrant {
    public static int ATTACK = 0;
    public static int SKILL = 1;
    public static int POWER = 2;
    public static int CURSE = 3;
    public static int STATUS = 4;

    int cardType;
    String cardName;
    int energyCost;
    public GameActionCtx secondActionCtx;
    public boolean ethereal = false;
    public boolean exhaustWhenPlayed = false;
    public boolean exhaustNonAttacks = false;
    public boolean selectEnemy;
    public boolean selectFromDiscard;
    public boolean selectFromExhaust;
    public boolean selectFromHand;
    public boolean selectFromDiscardLater;
    public boolean selectFromHandLater;
    public boolean exhaustSkill;
    public boolean exhaustCardFromHand;
    public boolean changePlayerStrength;
    public boolean changePlayerDexterity;
    public boolean vulnEnemy;
    public boolean weakEnemy;
    public boolean affectEnemyStrength;
    public boolean putCardOnTopDeck;
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
    List<Card> getPossibleGeneratedCards(List<Card> cards) { return null; }
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 8);
            state.enemies.get(idx).applyDebuff(DebuffType.VULNERABLE, 2);
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 10);
            state.enemies.get(idx).applyDebuff(DebuffType.VULNERABLE, 3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Strike extends Card {
        public Strike() {
            super("Strike", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.enemies.get(idx), 6);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class StrikeP extends Card {
        public StrikeP() {
            super("Strike+", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.enemies.get(idx), 9);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Defend extends Card {
        public Defend() {
            super("Defend", Card.SKILL, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainBlock(5);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DefendP extends Card {
        public DefendP() {
            super("Defend+", Card.SKILL, 1);
            energyCost = 1;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainBlock(8);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Anger extends Card {
        public Anger() {
            super("Anger", Card.ATTACK, 0);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.enemies.get(idx), 6);
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 8);
            state.addCardToDiscard(state.prop.angerCardIdx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Armanent extends Card {
        public Armanent() {
            super("Armanent", Card.SKILL, 1);
            selectFromHand = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainBlock(5);
            state.addCardToHand(state.prop.upgradeIdxes[idx]);
            state.removeCardFromHand(idx);
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
            state.player.gainBlock(5);
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), state.player.block);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BodySlamP extends Card {
        public BodySlamP() {
            super("Body Slam+", Card.ATTACK, 0);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.enemies.get(idx), state.player.block);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Clash extends Card {
        public Clash() {
            super("Clash", Card.ATTACK, -1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.enemies.get(idx), 14);
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 18);
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
            for (Enemy enemy : state.enemies) {
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
            for (Enemy enemy : state.enemies) {
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 12);
            state.enemies.get(idx).applyDebuff(DebuffType.WEAK, 2);
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 14);
            state.enemies.get(idx).applyDebuff(DebuffType.WEAK, 3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Flex extends Card {
        public Flex() {
            super("Flex", Card.ATTACK, 0);
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainStrength(2);
            // todo
//            state.player.applyDebuff(DebuffType.LOSE_STRENGTH_EOT, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FlexP extends Card {
        public FlexP() {
            super("Flex+", Card.ATTACK, 0);
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainStrength(4);
            // todo
//            state.player.applyDebuff(DebuffType.LOSE_STRENGTH_EOT, 4);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // todo: Havoc

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
                state.playerDoDamageToEnemy(state.enemies.get(idx), 9);
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
                state.playerDoDamageToEnemy(state.enemies.get(idx), 9);
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 14 + state.player.strength * 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class HeavyBladeP extends Card {
        public HeavyBladeP() {
            super("Heavy Blade+", Card.ATTACK, 2);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.enemies.get(idx), 14 + state.player.strength * 4);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class IronWave extends Card {
        public IronWave() {
            super("Iron Wave", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.enemies.get(idx), 5);
            state.player.gainBlock(5);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class IronWaveP extends Card {
        public IronWaveP() {
            super("Iron Wave+", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.enemies.get(idx), 7);
            state.player.gainBlock(7);
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 6 + 2 * count);
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 6 + 3 * count);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PommelStrike extends Card {
        public PommelStrike() {
            super("Pommel Strike", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.enemies.get(idx), 9);
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 10);
            state.draw(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ShrugItOff extends Card {
        public ShrugItOff() {
            super("Shrug It Off", Card.SKILL, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainBlock(8);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ShrugItOffP extends Card {
        public ShrugItOffP() {
            super("Shrug It Off+", Card.SKILL, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainBlock(11);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SwordBoomerang extends Card {
        public SwordBoomerang() {
            super("Sword Boomerang", Card.ATTACK, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            for (int i = 0; i < 3; i++) {
                int enemy_j = state.prop.random.nextInt(state.enemiesAlive);
                int j = 0;
                for (Enemy enemy : state.enemies) {
                    if (enemy.health >= 0) {
                        if (j == enemy_j) {
                            state.playerDoDamageToEnemy(enemy, 3);
                            break;
                        }
                        enemy_j += 1;
                    }
                }
            }
            if (state.enemiesAlive > 1) {
                state.isStochastic = true;
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SwordBoomerangP extends Card {
        public SwordBoomerangP() {
            super("Sword Boomerang+", Card.ATTACK, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            for (int i = 0; i < 4; i++) {
                int enemy_j = state.prop.random.nextInt(state.enemiesAlive);
                int j = 0;
                for (Enemy enemy : state.enemies) {
                    if (enemy.health >= 0) {
                        if (j == enemy_j) {
                            state.playerDoDamageToEnemy(enemy, 3);
                            break;
                        }
                        enemy_j += 1;
                    }
                }
            }
            if (state.enemiesAlive > 1) {
                state.isStochastic = true;
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Thunderclap extends Card {
        public Thunderclap() {
            super("Thunderclap", Card.ATTACK, 1);
            vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            for (Enemy enemy : state.enemies) {
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
            for (Enemy enemy : state.enemies) {
                state.playerDoDamageToEnemy(enemy, 7);
                enemy.applyDebuff(DebuffType.VULNERABLE, 1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class TrueGrit extends Card {
        public TrueGrit() {
            super("True Grit", Card.SKILL, 1);
            exhaustCardFromHand = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainBlock(7);
            int c = 0;
            for (int cardIdx = 0; cardIdx < state.hand.length; cardIdx++) {
                c += state.hand[cardIdx];
            }
            int r = state.prop.random.nextInt(c);
            for (int cardIdx = 0; cardIdx < state.hand.length; cardIdx++) {
                if (r <= 0) {
                    state.exhaustCardFromHand(cardIdx);
                }
                r -= state.hand[cardIdx];
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class TrueGritP extends Card {
        public TrueGritP() {
            super("True Grit+", Card.SKILL, 1);
            selectFromHand = true;
            exhaustCardFromHand = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainBlock(9);
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 5);
            state.playerDoDamageToEnemy(state.enemies.get(idx), 5);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class TwinStrikeP extends Card {
        public TwinStrikeP() {
            super("Twin Strike+", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.enemies.get(idx), 7);
            state.playerDoDamageToEnemy(state.enemies.get(idx), 7);
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 12);
            state.addCardToDeck(state.prop.woundCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return Arrays.asList(new Card.Wound());
        }
    }

    public static class WildStrikeP extends Card {
        public WildStrikeP() {
            super("Wild Strike+", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.enemies.get(idx), 12);
            state.addCardToDeck(state.prop.woundCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return Arrays.asList(new Card.Wound());
        }
    }

    public static class BattleTrance extends Card {
        public BattleTrance() {
            super("Battle Trance", Card.SKILL, 0);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.draw(3);
            state.player.applyDebuff(DebuffType.NO_MORE_CARD_DRAW, 1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BattleTranceP extends Card {
        public BattleTranceP() {
            super("Battle Trance+", Card.SKILL, 0);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.draw(4);
            state.player.applyDebuff(DebuffType.NO_MORE_CARD_DRAW, 1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // todo: blood for blood

    public static class Bloodletting extends Card {
        public Bloodletting() {
            super("Bloodletting", Card.SKILL, 0);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.doNonAttackDamageToPlayer(3, false);
            state.gainEnergy(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BloodlettingP extends Card {
        public BloodlettingP() {
            super("Bloodletting+", Card.SKILL, 0);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.doNonAttackDamageToPlayer(3, false);
            state.gainEnergy(3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BurningPact extends Card {
        public BurningPact() {
            super("Burning Pact", Card.SKILL, 1);
            selectFromHand =  true;
            selectFromHandLater = true;
            exhaustCardFromHand = true;
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
            exhaustCardFromHand = true;
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 20);
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 28);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // todo: combust

    public static class DarkEmbrace extends Card {
        public DarkEmbrace() {
            super("Dark Embrace", Card.POWER, 2);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.darkEmbrace += 1;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DarkEmbraceP extends Card {
        public DarkEmbraceP() {
            super("Dark Embrace+", Card.POWER, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.darkEmbrace += 1;
            return GameActionCtx.PLAY_CARD;
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
            state.enemies.get(idx).strength -= 2;
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
            state.enemies.get(idx).strength -= 2;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Dropkick extends Card {
        public Dropkick() {
            super("Dropkick", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.enemies.get(idx), 5);
            if (state.enemies.get(idx).vulnerable > 0) {
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 8);
            if (state.enemies.get(idx).vulnerable > 0) {
                state.energy += 1;
                state.draw(1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // todo: need to keep track of discard
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
    }

    public static class Entrench extends Card {
        public Entrench() {
            super("Entrench", Card.SKILL, 2);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainBlockNotFromCardPlay(state.player.block);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class EntrenchP extends Card {
        public EntrenchP() {
            super("Entrench+", Card.SKILL, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainBlockNotFromCardPlay(state.player.block);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // todo: evolve

    public static class FeelNoPain extends Card {
        public FeelNoPain() {
            super("Feel No Pain", Card.POWER, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.feelNotPain += 3;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FeelNoPainP extends Card {
        public FeelNoPainP() {
            super("Feel No Pain+", Card.POWER, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.feelNotPain += 4;
            return GameActionCtx.PLAY_CARD;
        }
    }

    // todo: fire breathing

    public static class FlameBarrier extends Card {
        public FlameBarrier() {
            super("Flame Barrier", Card.SKILL, 2);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainBlock(12);
            state.thorn += 4;
            state.thornLoseEOT += 4;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FlameBarrierP extends Card {
        public FlameBarrierP() {
            super("Flame Barrier+", Card.SKILL, 2);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainBlock(16);
            state.thorn += 6;
            state.thornLoseEOT += 6;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class GhostlyArmor extends Card {
        public GhostlyArmor() {
            super("Ghostly Armor", Card.SKILL, 1);
            ethereal = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainBlock(10);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class GhostlyArmorP extends Card {
        public GhostlyArmorP() {
            super("Ghostly Armor+", Card.SKILL, 1);
            ethereal = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainBlock(13);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Hemokinesis extends Card {
        public Hemokinesis() {
            super("Hemokinesis", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.doNonAttackDamageToPlayer(2, false);
            state.playerDoDamageToEnemy(state.enemies.get(idx), 15);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class HemokinesisP extends Card {
        public HemokinesisP() {
            super("Hemokinesis+", Card.ATTACK, 1);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.doNonAttackDamageToPlayer(2, false);
            state.playerDoDamageToEnemy(state.enemies.get(idx), 20);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // todo: infernal blade

    public static class Inflame extends Card {
        public Inflame() {
            super("Inflame", Card.POWER, 1);
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainStrength(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class InflameP extends Card {
        public InflameP() {
            super("Inflame+", Card.POWER, 1);
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainStrength(3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Intimidate extends Card {
        public Intimidate() {
            super("Intimidate", Card.SKILL, 0);
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            for (Enemy enemy : state.enemies) {
                state.enemies.get(idx).applyDebuff(DebuffType.WEAK, 1);
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
            for (Enemy enemy : state.enemies) {
                state.enemies.get(idx).applyDebuff(DebuffType.WEAK, 2);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Metallicize extends Card {
        public Metallicize() {
            super("Metallicize", Card.POWER, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.metallicize += 3;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class MetallicizeP extends Card {
        public MetallicizeP() {
            super("Metallicize", Card.POWER, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.metallicize += 4;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PowerThrough extends Card {
        public PowerThrough() {
            super("Power Through", Card.SKILL, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainBlock(15);
            state.addCardToHand(state.prop.woundCardIdx);
            state.addCardToHand(state.prop.woundCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return Arrays.asList(new Card.Wound());
        }
    }

    public static class PowerThroughP extends Card {
        public PowerThroughP() {
            super("Power Through+", Card.SKILL, 1);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainBlock(20);
            state.addCardToHand(state.prop.woundCardIdx);
            state.addCardToHand(state.prop.woundCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return Arrays.asList(new Card.Wound());
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
                state.playerDoDamageToEnemy(state.enemies.get(idx), 2);
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
                state.playerDoDamageToEnemy(state.enemies.get(idx), 2);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // todo: rage
    // todo: rampage

    public static class RecklessCharge extends Card {
        public RecklessCharge() {
            super("Reckless Charge", Card.ATTACK, 0);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.enemies.get(idx), 7);
            state.addCardToDeck(state.prop.dazedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return Arrays.asList(new Card.Dazed());
        }
    }

    public static class RecklessChargeP extends Card {
        public RecklessChargeP() {
            super("Reckless Charge+", Card.ATTACK, 0);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.enemies.get(idx), 10);
            state.addCardToDeck(state.prop.dazedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return Arrays.asList(new Card.Dazed());
        }
    }

    // todo: rupture
    // todo: searing blow

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
                        state.player.gainBlock(5);
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
                        state.player.gainBlock(5);
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
            state.player.gainBlock(5);
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
            state.player.gainBlock(8);
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 16);
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 22);
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
            for (Enemy enemy : state.enemies) {
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
            for (Enemy enemy : state.enemies) {
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
            if (state.enemies.get(idx).getMoveString(state).contains("Attack")) {
                state.player.gainStrength(3);
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
            if (state.enemies.get(idx).getMoveString(state).contains("Attack")) {
                state.player.gainStrength(4);
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 13);
            state.enemies.get(idx).applyDebuff(DebuffType.WEAK, 1);
            state.enemies.get(idx).applyDebuff(DebuffType.VULNERABLE, 1);
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 13);
            state.enemies.get(idx).applyDebuff(DebuffType.WEAK, 2);
            state.enemies.get(idx).applyDebuff(DebuffType.VULNERABLE, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Whirlwind extends Card {
        public Whirlwind() {
            super("Whirlwind", Card.ATTACK, -1);
        }

        public GameActionCtx play(GameState state, int idx) {
            for (int i = 0; i < state.energy; i++) {
                state.playerDoDamageToEnemy(state.enemies.get(idx), 5);
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
                state.playerDoDamageToEnemy(state.enemies.get(idx), 8);
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
            state.player.applyDebuff(DebuffType.VULNERABLE, 2);
            state.energyRefill += 1;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BerserkP extends Card {
        public BerserkP() {
            super("Berserk+", Card.POWER, 0);
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.applyDebuff(DebuffType.VULNERABLE, 1);
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
            state.playerDoDamageToEnemy(state.enemies.get(idx), 32);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BludgeonP extends Card {
        public BludgeonP() {
            super("Bludgeon+", Card.ATTACK, 3);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.playerDoDamageToEnemy(state.enemies.get(idx), 42);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // todo: brutality

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

    public static class DemonForm extends Card {
        public DemonForm() {
            super("Demon Form", Card.POWER, 3);
            changePlayerStrength = true;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("DemonForm", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] == 0 ? 6 : counter[counterIdx]) / 6.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addStartOfTurnHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
                    var counter = state.getCounterForRead();
                    state.player.gainStrength(counter[counterIdx]);
                }
            });
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += 2;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DemonFormP extends Card {
        public DemonFormP() {
            super("Demon Form+", Card.POWER, 3);
            changePlayerStrength = true;
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("DemonForm", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] == 0 ? 6 : counter[counterIdx]) / 6.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addStartOfTurnHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
                    var counter = state.getCounterForRead();
                    state.player.gainStrength(counter[counterIdx]);
                }
            });
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += 3;
            return GameActionCtx.PLAY_CARD;
        }
    }

    // todo: double tap

    public static class Exhume extends Card {
        public Exhume() {
            super("Exhume", Card.SKILL, 1);
            selectFromExhaust = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.exhaust[idx] -= 1;
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
            state.exhaust[idx] -= 1;
            state.addCardToHand(idx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // todo: feed

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
                    state.playerDoDamageToEnemy(state.enemies.get(idx), 7);
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
                    state.playerDoDamageToEnemy(state.enemies.get(idx), 10);
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
            for (Enemy enemy : state.enemies) {
                state.playerDoDamageToEnemy(enemy, 21);
            }
            state.addCardToDiscard(state.prop.burnCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return Arrays.asList(new Card.Burn());
        }
    }

    public static class ImmolateP extends Card {
        public ImmolateP() {
            super("Immolate+", Card.ATTACK, 2);
        }

        public GameActionCtx play(GameState state, int idx) {
            for (Enemy enemy : state.enemies) {
                state.playerDoDamageToEnemy(enemy, 28);
            }
            state.addCardToDiscard(state.prop.burnCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return Arrays.asList(new Card.Burn());
        }
    }

    public static class Impervious extends Card {
        public Impervious() {
            super("Impervious", Card.SKILL, 2);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainBlock(30);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ImperviousP extends Card {
        public ImperviousP() {
            super("Impervious+", Card.SKILL, 2);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainBlock(40);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Juggernaut extends Card {
        public Juggernaut() {
            super("Juggernaut", Card.POWER, 2);
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Juggernaut", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] == 0 ? 14 : counter[counterIdx]) / 14.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }

        // todo: implement the effect

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += 5;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class JuggernautP extends Card {
        public JuggernautP() {
            super("Juggernaut+", Card.POWER, 2);
        }

        @Override public void startOfGameSetup(GameState state) {
            state.prop.registerCounter("Juggernaut", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(float[] input, int idx) {
                    var counter = state.getCounterForRead();
                    input[idx] = (counter[counterIdx] == 0 ? 14 : counter[counterIdx]) / 14.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }

        public GameActionCtx play(GameState state, int idx) {
            state.getCounterForWrite()[counterIdx] += 7;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class LimitBreak extends Card {
        public LimitBreak() {
            super("Limit Break", Card.SKILL, 1);
            exhaustWhenPlayed = true;
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainStrength(state.player.strength);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class LimitBreakP extends Card {
        public LimitBreakP() {
            super("Limit Break+", Card.SKILL, 1);
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.player.gainStrength(state.player.strength);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Offering extends Card {
        public Offering() {
            super("Offering", Card.SKILL, 0);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            state.doNonAttackDamageToPlayer(6, false);
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
            state.doNonAttackDamageToPlayer(6, false);
            state.draw(5);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Reaper extends Card {
        public Reaper() {
            super("Reaper", Card.ATTACK, 2);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            int amount = 0;
            for (Enemy enemy : state.enemies) {
                int prevHp = enemy.health;
                state.playerDoDamageToEnemy(enemy, 4);
                amount += enemy.health - prevHp;
            }
            state.player.heal(amount);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ReaperP extends Card {
        public ReaperP() {
            super("Reaper+", Card.ATTACK, 2);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx) {
            int amount = 0;
            for (Enemy enemy : state.enemies) {
                int prevHp = enemy.health;
                state.playerDoDamageToEnemy(enemy, 5);
                amount += enemy.health - prevHp;
            }
            state.player.heal(amount);
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
            state.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
                    for (int i = 0; i < state.hand[cardIndex]; i++) {
                        state.doNonAttackDamageToPlayer(2, true);
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
            state.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
                    for (int i = 0; i < state.hand[cardIndex]; i++) {
                        state.doNonAttackDamageToPlayer(4, true);
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

    // todo: Void

    // **********************************************************************************************************
    // ********************************************** Curses ****************************************************
    // **********************************************************************************************************

    public static class AscendersBane extends Card {
        public AscendersBane() {
            super("Ascender's Bane", Card.CURSE, -1);
            ethereal = true;
        }
    }

    public static class Clumsy extends Card {
        public Clumsy() {
            super("Clumsy", Card.CURSE, -1);
            ethereal = true;
        }
    }

    public static class Necronomicurse extends Card {
        public Necronomicurse() {
            super("Necronomicurse", Card.CURSE, -1);
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
        }

        @Override public void startOfGameSetup(GameState state) {
            var cardIndex = state.prop.findCardIndex(this);
            state.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
                    for (int i = 0; i < state.hand[cardIndex]; i++) {
                        state.doNonAttackDamageToPlayer(2, true);
                    }
                }
            });
        }
    }

    public static class Doubt extends Card {
        public Doubt() {
            super("Doubt", Card.CURSE, -1);
        }

        @Override public void startOfGameSetup(GameState state) {
            var cardIndex = state.prop.findCardIndex(this);
            state.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
                    for (int i = 0; i < state.hand[cardIndex]; i++) {
                        state.player.applyDebuff(DebuffType.WEAK, 1);
                    }
                }
            });
        }
    }

    // todo: Normality

    public static class Pain extends Card {
        public Pain() {
            super("Pain", Card.CURSE, -1);
        }

        @Override public void startOfGameSetup(GameState state) {
            var cardIndex = state.prop.findCardIndex(this);
            state.addOnCardPlayedHandler(new OnCardPlayedHandler() {
                @Override void handle(GameState state, Card card) {
                    for (int i = 0; i < state.hand[cardIndex]; i++) {
                        state.doNonAttackDamageToPlayer(1, false);
                    }
                }
            });
        }
    }

    public static class Regret extends Card {
        public Regret() {
            super("Regret", Card.CURSE, -1);
        }

        @Override public void startOfGameSetup(GameState state) {
            var cardIndex = state.prop.findCardIndex(this);
            state.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
                    int numOfCards = 0;
                    for (int n : state.hand) {
                        numOfCards += n;
                    }
                    for (int i = 0; i < state.hand[cardIndex]; i++) {
                        state.doNonAttackDamageToPlayer(numOfCards, false);
                    }
                }
            });
        }
    }

    public static class Shame extends Card {
        public Shame() {
            super("Shame", Card.CURSE, -1);
        }

        @Override public void startOfGameSetup(GameState state) {
            var cardIndex = state.prop.findCardIndex(this);
            state.addPreEndOfTurnHandler(new GameEventHandler() {
                @Override void handle(GameState state) {
                    for (int i = 0; i < state.hand[cardIndex]; i++) {
                        state.player.applyDebuff(DebuffType.FRAIL, 1);
                    }
                }
            });
        }
    }

    public static class Writhe extends Card {
        public Writhe() {
            super("Writhe", Card.CURSE, -1);
//            innate = true;
        }
    }

    public static class Parasite extends Card {
        public Parasite() {
            super("Parasite", Card.CURSE, -1);
        }
    }

    public static class Injury extends Card {
        public Injury() {
            super("Injury", Card.CURSE, -1);
        }
    }

    public static class CurseOfTheBell extends Card {
        public CurseOfTheBell() {
            super("Curse of The Bell", Card.CURSE, -1);
        }
    }
}
