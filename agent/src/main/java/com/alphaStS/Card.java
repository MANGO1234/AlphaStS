package com.alphaStS;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

abstract class Card {
    public static int ATTACK = 0;
    public static int SKILL = 1;
    public static int POWER = 2;
    public static int CURSE = 3;
    public static int STATUS = 4;

    int cardType;
    String cardName;
    public GameActionCtx secondActionCtx;
    public boolean exhaustEndOfTurn = false;
    public boolean exhaustWhenPlayed = false;
    public boolean exhaustNonAttacks = false;
    public boolean selectEnemy;
    public boolean selectFromDiscard;
    public boolean selectFromExhaust;
    public boolean selectFromHand;
    public boolean changePlayerStrength;
    public boolean changePlayerDexterity;
    public boolean vulnEnemy;
    public boolean weakEnemy;
    public boolean affectEnemyStrength;
    public boolean putCardOnTopDeck;

    public Card(String cardName, int cardType) {
        this.cardType = cardType;
        this.cardName = cardName;
    }

    abstract void play(GameState state, int idx);
    void playPart2(GameState state, int idx) {};
    abstract int energyCost(GameState state);
    void onDiscard(GameState state) {}
    List<Card> getPossibleGeneratedCards(List<Card> cards) { return null; }
    public boolean canSelectFromHand(Card card) { return false; };

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
            super("Bash", Card.ATTACK);
            selectEnemy = true;
            vulnEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 8);
            state.enemies.get(idx).applyDebuff(DebuffType.VULNERABLE, 2);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class BashP extends Card {
        public BashP() {
            super("Bash+", Card.ATTACK);
            selectEnemy = true;
            vulnEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 10);
            state.enemies.get(idx).applyDebuff(DebuffType.VULNERABLE, 3);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class Strike extends Card {
        public Strike() {
            super("Strike", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 6);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class StrikeP extends Card {
        public StrikeP() {
            super("Strike+", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 9);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class Defend extends Card {
        public Defend() {
            super("Defend", Card.SKILL);
        }

        public void play(GameState state, int idx) {
            state.player.gainBlock(5);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class DefendP extends Card {
        public DefendP() {
            super("Defend+", Card.SKILL);
        }

        public void play(GameState state, int idx) {
            state.player.gainBlock(8);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class Anger extends Card {
        public Anger() {
            super("Anger", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 6);
            state.discard[state.prop.angerCardIdx] += 1;
        }

        public int energyCost(GameState state) {
            return 0;
        }
    }

    public static class AngerP extends Card {
        public AngerP() {
            super("Anger+", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 8);
            state.discard[state.prop.angerPCardIdx] += 1;
        }

        public int energyCost(GameState state) {
            return 0;
        }
    }

    public static class Armanent extends Card {
        public Armanent() {
            super("Armanent", Card.SKILL);
            selectFromHand = true;
        }

        public void play(GameState state, int idx) {
            state.player.gainBlock(5);
            state.hand[state.prop.upgradeIdxes[idx]] += 1;
            state.hand[idx] -= 1;
        }

        public int energyCost(GameState state) {
            return 1;
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
            super("Armanent+", Card.SKILL);
        }

        public void play(GameState state, int idx) {
            state.player.gainBlock(5);
            for (int i = 0; i < state.prop.upgradeIdxes.length; i++) {
                if (state.hand[i] > 0 && state.prop.upgradeIdxes[i] >= 0) {
                    state.hand[state.prop.upgradeIdxes[i]] += state.hand[i];
                    state.hand[i] = 0;
                }
            }
        }

        public int energyCost(GameState state) {
            return 1;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return cards.stream().map((x) -> CardUpgrade.map.get(x)).filter(Objects::nonNull).toList();
        }
    }

    public static class BodySlam extends Card {
        public BodySlam() {
            super("Body Slam", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), state.player.block);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class BodySlamP extends Card {
        public BodySlamP() {
            super("Body Slam+", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), state.player.block);
        }

        public int energyCost(GameState state) {
            return 0;
        }
    }

    public static class Clash extends Card {
        public Clash() {
            super("Clash", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 14);
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
            super("Clash+", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 18);
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
            super("Cleave", Card.ATTACK);
        }

        public void play(GameState state, int idx) {
            for (Enemy enemy : state.enemies) {
                state.player.doDamageToEnemy(state, enemy, 8);
            }
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class CleaveP extends Card {
        public CleaveP() {
            super("Cleave+", Card.ATTACK);
        }

        public void play(GameState state, int idx) {
            for (Enemy enemy : state.enemies) {
                state.player.doDamageToEnemy(state, enemy, 11);
            }
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class Clothesline extends Card {
        public Clothesline() {
            super("Clothesline", Card.ATTACK);
            selectEnemy = true;
            weakEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 12);
            state.enemies.get(idx).applyDebuff(DebuffType.WEAK, 2);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class ClotheslineP extends Card {
        public ClotheslineP() {
            super("Clothesline+", Card.ATTACK);
            selectEnemy = true;
            weakEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 14);
            state.enemies.get(idx).applyDebuff(DebuffType.WEAK, 3);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    // todo: Flex
    // todo: Havoc

    public static class Headbutt extends Card {
        public Headbutt() {
            super("Headbutt", Card.ATTACK);
            selectEnemy = true;
            selectFromDiscard = true;
            secondActionCtx = GameActionCtx.SELECT_CARD_DISCARD;
            putCardOnTopDeck = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 9);
        }

        @Override public void playPart2(GameState state, int idx) {
            state.discard[idx] -= 1;
            state.putCardOnTopOfDeck(idx);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class HeadbuttP extends Card {
        public HeadbuttP() {
            super("Headbutt+", Card.ATTACK);
            selectEnemy = true;
            selectFromDiscard = true;
            secondActionCtx = GameActionCtx.SELECT_CARD_DISCARD;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 12);
        }

        @Override public void playPart2(GameState state, int idx) {
            state.discard[idx] -= 1;
            state.putCardOnTopOfDeck(idx);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class HeavyBlade extends Card {
        public HeavyBlade() {
            super("Heavy Blade", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 14 + state.player.strength * 2);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class HeavyBladeP extends Card {
        public HeavyBladeP() {
            super("Heavy Blade+", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 14 + state.player.strength * 4);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class IronWave extends Card {
        public IronWave() {
            super("Iron Wave", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 5);
            state.player.gainBlock(5);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class IronWaveP extends Card {
        public IronWaveP() {
            super("Iron Wave+", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 7);
            state.player.gainBlock(7);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class PerfectedStrike extends Card {
        public PerfectedStrike() {
            super("Perfected Strike", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            int count = 0;
            for (int i = 0; i < state.prop.strikeCardIdxes.length; i++) {
                count += state.hand[state.prop.strikeCardIdxes[i]];
                count += state.discard[state.prop.strikeCardIdxes[i]];
                count += state.deck[state.prop.strikeCardIdxes[i]];
            }
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 6 + 2 * count);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class PerfectedStrikeP extends Card {
        public PerfectedStrikeP() {
            super("Perfected Strike+", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            int count = 0;
            for (int i = 0; i < state.prop.strikeCardIdxes.length; i++) {
                count += state.hand[state.prop.strikeCardIdxes[i]];
                count += state.discard[state.prop.strikeCardIdxes[i]];
                count += state.deck[state.prop.strikeCardIdxes[i]];
            }
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 6 + 3 * count);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class PommelStrike extends Card {
        public PommelStrike() {
            super("Pommel Strike", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 9);
            state.draw(1);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class PommelStrikeP extends Card {
        public PommelStrikeP() {
            super("Pommel Strike+", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 10);
            state.draw(2);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class ShrugItOff extends Card {
        public ShrugItOff() {
            super("Shrug It Off", Card.SKILL);
        }

        public void play(GameState state, int idx) {
            state.player.gainBlock(8);
            state.draw(1);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class ShrugItOffP extends Card {
        public ShrugItOffP() {
            super("Shrug It Off+", Card.SKILL);
        }

        public void play(GameState state, int idx) {
            state.player.gainBlock(11);
            state.draw(1);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class SwordBoomerang extends Card {
        public SwordBoomerang() {
            super("Sword Boomerang", Card.ATTACK);
        }

        public void play(GameState state, int idx) {
            for (int i = 0; i < 3; i++) {
                int enemy_j = state.prop.random.nextInt(state.enemiesAlive);
                int j = 0;
                for (Enemy enemy : state.enemies) {
                    if (enemy.health >= 0) {
                        if (j == enemy_j) {
                            state.player.doDamageToEnemy(state, enemy, 3);
                            break;
                        }
                        enemy_j += 1;
                    }
                }
            }
            if (state.enemiesAlive > 1) {
                state.isStochastic = true;
            }
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class SwordBoomerangP extends Card {
        public SwordBoomerangP() {
            super("Sword Boomerang+", Card.ATTACK);
        }

        public void play(GameState state, int idx) {
            for (int i = 0; i < 4; i++) {
                int enemy_j = state.prop.random.nextInt(state.enemiesAlive);
                int j = 0;
                for (Enemy enemy : state.enemies) {
                    if (enemy.health >= 0) {
                        if (j == enemy_j) {
                            state.player.doDamageToEnemy(state, enemy, 3);
                            break;
                        }
                        enemy_j += 1;
                    }
                }
            }
            if (state.enemiesAlive > 1) {
                state.isStochastic = true;
            }
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class Thunderclap extends Card {
        public Thunderclap() {
            super("Thunderclap", Card.ATTACK);
            vulnEnemy = true;
        }

        public void play(GameState state, int idx) {
            for (Enemy enemy : state.enemies) {
                state.player.doDamageToEnemy(state, enemy, 4);
                enemy.applyDebuff(DebuffType.VULNERABLE, 1);
            }
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class ThunderclapP extends Card {
        public ThunderclapP() {
            super("Thunderclap+", Card.ATTACK);
            vulnEnemy = true;
        }

        public void play(GameState state, int idx) {
            for (Enemy enemy : state.enemies) {
                state.player.doDamageToEnemy(state, enemy, 7);
                enemy.applyDebuff(DebuffType.VULNERABLE, 1);
            }
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    // todo: true grit

    public static class TwinStrike extends Card {
        public TwinStrike() {
            super("Twin Strike", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 5);
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 5);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class TwinStrikeP extends Card {
        public TwinStrikeP() {
            super("Twin Strike+", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 7);
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 7);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    // todo: warcry

    public static class WildStrike extends Card {
        public WildStrike() {
            super("Wild Strike", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 12);
            state.addCardToDeck(state.prop.woundCardIdx);
        }

        public int energyCost(GameState state) {
            return 1;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return Arrays.asList(new Card.Wound());
        }
    }

    public static class WildStrikeP extends Card {
        public WildStrikeP() {
            super("Wild Strike+", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 12);
            state.addCardToDeck(state.prop.woundCardIdx);
        }

        public int energyCost(GameState state) {
            return 1;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return Arrays.asList(new Card.Wound());
        }
    }

    // todo: battle trance
    // todo: blood for blood

    public static class Bloodletting extends Card {
        public Bloodletting() {
            super("Bloodletting", Card.SKILL);
        }

        public void play(GameState state, int idx) {
            state.player.nonAttackDamage(3, false);
            state.gainEnergy(2);
        }

        public int energyCost(GameState state) {
            return 0;
        }
    }

    public static class BloodlettingP extends Card {
        public BloodlettingP() {
            super("Bloodletting+", Card.SKILL);
        }

        public void play(GameState state, int idx) {
            state.player.nonAttackDamage(3, false);
            state.gainEnergy(3);
        }

        public int energyCost(GameState state) {
            return 0;
        }
    }

    // todo: burning pact

    public static class Carnage extends Card {
        public Carnage() {
            super("Carnage", Card.ATTACK);
            selectEnemy = true;
            exhaustEndOfTurn = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 20);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class CarnageP extends Card {
        public CarnageP() {
            super("Carnage+", Card.ATTACK);
            selectEnemy = true;
            exhaustEndOfTurn = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 28);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    // todo: combust
    // todo: dark embrace

    public static class Disarm extends Card {
        public Disarm() {
            super("Disarm", Card.SKILL);
            exhaustWhenPlayed = true;
            selectEnemy = true;
            affectEnemyStrength = true;
        }

        public void play(GameState state, int idx) {
            state.enemies.get(idx).strength -= 2;
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class DisarmP extends Card {
        public DisarmP() {
            super("Disarm+", Card.SKILL);
            exhaustWhenPlayed = true;
            selectEnemy = true;
            affectEnemyStrength = true;
        }

        public void play(GameState state, int idx) {
            state.enemies.get(idx).strength -= 2;
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class Dropkick extends Card {
        public Dropkick() {
            super("Dropkick", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 5);
            if (state.enemies.get(idx).vulnerable > 0) {
                state.energy += 1;
                state.draw(1);
            }
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class DropkickP extends Card {
        public DropkickP() {
            super("Dropkick+", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 8);
            if (state.enemies.get(idx).vulnerable > 0) {
                state.energy += 1;
                state.draw(1);
            }
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    // todo: dual wield

    public static class Entrench extends Card {
        public Entrench() {
            super("Entrench", Card.SKILL);
        }

        public void play(GameState state, int idx) {
            state.player.gainBlockNoDex(state.player.block);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class EntrenchP extends Card {
        public EntrenchP() {
            super("Entrench+", Card.SKILL);
        }

        public void play(GameState state, int idx) {
            state.player.gainBlockNoDex(state.player.block);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    // todo: evolve
    // todo: feel no pain
    // todo: fire breathing

    public static class FlameBarrier extends Card {
        public FlameBarrier() {
            super("Flame Barrier", Card.SKILL);
        }

        public void play(GameState state, int idx) {
            state.player.gainBlock(12);
            state.thorn += 4;
            state.thornLoseEOT += 4;
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class FlameBarrierP extends Card {
        public FlameBarrierP() {
            super("Flame Barrier+", Card.SKILL);
        }

        public void play(GameState state, int idx) {
            state.player.gainBlock(16);
            state.thorn += 6;
            state.thornLoseEOT += 6;
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class GhostlyArmor extends Card {
        public GhostlyArmor() {
            super("Ghostly Armor", Card.SKILL);
            exhaustEndOfTurn = true;
        }

        public void play(GameState state, int idx) {
            state.player.gainBlock(10);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class GhostlyArmorP extends Card {
        public GhostlyArmorP() {
            super("Ghostly Armor+", Card.SKILL);
            exhaustEndOfTurn = true;
        }

        public void play(GameState state, int idx) {
            state.player.gainBlock(13);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class Hemokinesis extends Card {
        public Hemokinesis() {
            super("Hemokinesis", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.nonAttackDamage(2, false);
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 15);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class HemokinesisP extends Card {
        public HemokinesisP() {
            super("Hemokinesis+", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.nonAttackDamage(2, false);
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 20);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    // todo: infernal blade

    public static class Inflame extends Card {
        public Inflame() {
            super("Inflame", Card.POWER);
            changePlayerStrength = true;
        }

        public void play(GameState state, int idx) {
            state.player.gainStrength(2);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class InflameP extends Card {
        public InflameP() {
            super("Inflame+", Card.POWER);
            changePlayerStrength = true;
        }

        public void play(GameState state, int idx) {
            state.player.gainStrength(3);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class Intimidate extends Card {
        public Intimidate() {
            super("Intimidate", Card.SKILL);
            weakEnemy = true;
        }

        public void play(GameState state, int idx) {
            for (Enemy enemy : state.enemies) {
                state.enemies.get(idx).applyDebuff(DebuffType.WEAK, 1);
            }
        }

        public int energyCost(GameState state) {
            return 0;
        }
    }

    public static class IntimidateP extends Card {
        public IntimidateP() {
            super("Intimidate+", Card.SKILL);
            weakEnemy = true;
        }

        public void play(GameState state, int idx) {
            for (Enemy enemy : state.enemies) {
                state.enemies.get(idx).applyDebuff(DebuffType.WEAK, 2);
            }
        }

        public int energyCost(GameState state) {
            return 0;
        }
    }

    // todo: metallicize

    public static class PowerThrough extends Card {
        public PowerThrough() {
            super("Power Through", Card.ATTACK);
        }

        public void play(GameState state, int idx) {
            state.player.gainBlock(15);
            state.addCardToHand(state.prop.woundCardIdx);
            state.addCardToHand(state.prop.woundCardIdx);
        }

        public int energyCost(GameState state) {
            return 1;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return Arrays.asList(new Card.Wound());
        }
    }

    public static class PowerThroughP extends Card {
        public PowerThroughP() {
            super("Power Through+", Card.ATTACK);
        }

        public void play(GameState state, int idx) {
            state.player.gainBlock(20);
            state.addCardToHand(state.prop.woundCardIdx);
            state.addCardToHand(state.prop.woundCardIdx);
        }

        public int energyCost(GameState state) {
            return 1;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return Arrays.asList(new Card.Wound());
        }
    }

    public static class Pummel extends Card {
        public Pummel() {
            super("Pummel", Card.ATTACK);
            selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public void play(GameState state, int idx) {
            for (int i = 0; i < 4; i++) {
                state.player.doDamageToEnemy(state, state.enemies.get(idx), 2);
            }
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class PummelP extends Card {
        public PummelP() {
            super("Pummel+", Card.ATTACK);
            selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public void play(GameState state, int idx) {
            for (int i = 0; i < 5; i++) {
                state.player.doDamageToEnemy(state, state.enemies.get(idx), 2);
            }
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    // todo: rage
    // todo: rampage

    public static class RecklessCharge extends Card {
        public RecklessCharge() {
            super("Reckless Charge", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 7);
            state.addCardToDeck(state.prop.dazedCardIdx);
        }

        public int energyCost(GameState state) {
            return 0;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return Arrays.asList(new Card.Dazed());
        }
    }

    public static class RecklessChargeP extends Card {
        public RecklessChargeP() {
            super("Reckless Charge+", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 10);
            state.addCardToDeck(state.prop.dazedCardIdx);
        }

        public int energyCost(GameState state) {
            return 0;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return Arrays.asList(new Card.Dazed());
        }
    }

    // todo: rupture
    // todo: searing blow

    public static class SecondWind extends Card {
        public SecondWind() {
            super("Second Wind", Card.SKILL);
            exhaustNonAttacks = true;
        }

        public void play(GameState state, int idx) {
            for (int i = 0; i < state.hand.length; i++) {
                if (state.hand[i] > 0 && state.prop.cardDict[i].cardType != Card.ATTACK) {
                    while (state.hand[i] > 0) {
                        state.exhaustCardFromHand(i);
                        state.player.gainBlock(5);
                    }
                }
            }
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class SecondWindP extends Card {
        public SecondWindP() {
            super("Second Wind+", Card.SKILL);
        }

        public void play(GameState state, int idx) {
            for (int i = 0; i < state.hand.length; i++) {
                if (state.hand[i] > 0 && state.prop.cardDict[i].cardType != Card.ATTACK) {
                    while (state.hand[i] > 0) {
                        state.exhaustCardFromHand(i);
                        state.player.gainBlock(5);
                    }
                }
            }
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class SeeingRed extends Card {
        public SeeingRed() {
            super("Seeing Red", Card.SKILL);
            exhaustWhenPlayed = true;
        }

        public void play(GameState state, int idx) {
            state.gainEnergy(2);
        }

        public int energyCost(GameState state) {
            return 0;
        }
    }

    public static class SeeingRedP extends Card {
        public SeeingRedP() {
            super("Seeing Red+", Card.SKILL);
            exhaustWhenPlayed = true;
        }

        public void play(GameState state, int idx) {
            state.gainEnergy(3);
        }

        public int energyCost(GameState state) {
            return 0;
        }
    }

    // todo: sentinel

    public static class SeverSoul extends Card {
        public SeverSoul() {
            super("Sever Soul", Card.ATTACK);
            selectEnemy = true;
            exhaustNonAttacks = true;
        }

        public void play(GameState state, int idx) {
            for (int i = 0; i < state.hand.length; i++) {
                if (state.hand[i] > 0 && state.prop.cardDict[i].cardType != Card.ATTACK) {
                    while (state.hand[i] > 0) {
                        state.exhaustCardFromHand(i);
                    }
                }
            }
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 16);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class SeverSoulP extends Card {
        public SeverSoulP() {
            super("Sever Soul+", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            for (int i = 0; i < state.hand.length; i++) {
                if (state.hand[i] > 0 && state.prop.cardDict[i].cardType != Card.ATTACK) {
                    while (state.hand[i] > 0) {
                        state.exhaustCardFromHand(i);
                    }
                }
            }
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 22);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class Shockwave extends Card {
        public Shockwave() {
            super("Shockwave", Card.SKILL);
            exhaustWhenPlayed = true;
            vulnEnemy = true;
            weakEnemy = true;
        }

        public void play(GameState state, int idx) {
            for (Enemy enemy : state.enemies) {
                enemy.applyDebuff(DebuffType.WEAK, 3);
                enemy.applyDebuff(DebuffType.VULNERABLE, 3);
            }
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class ShockwaveP extends Card {
        public ShockwaveP() {
            super("Shockwave+", Card.SKILL);
            exhaustWhenPlayed = true;
            vulnEnemy = true;
            weakEnemy = true;
        }

        public void play(GameState state, int idx) {
            for (Enemy enemy : state.enemies) {
                enemy.applyDebuff(DebuffType.WEAK, 5);
                enemy.applyDebuff(DebuffType.VULNERABLE, 5);
            }
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class SpotWeakness extends Card {
        public SpotWeakness() {
            super("Spot Weakness", Card.SKILL);
            selectEnemy = true;
            changePlayerStrength = true;
        }

        public void play(GameState state, int idx) {
            if (state.enemies.get(idx).getMoveString(state).contains("Attack")) {
                state.player.gainStrength(3);
            }
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class SpotWeaknessP extends Card {
        public SpotWeaknessP() {
            super("Spot Weakness+", Card.SKILL);
            selectEnemy = true;
            changePlayerStrength = true;
        }

        public void play(GameState state, int idx) {
            if (state.enemies.get(idx).getMoveString(state).contains("Attack")) {
                state.player.gainStrength(4);
            }
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class Uppercut extends Card {
        public Uppercut() {
            super("Uppercut", Card.ATTACK);
            selectEnemy = true;
            vulnEnemy = true;
            weakEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 13);
            state.enemies.get(idx).applyDebuff(DebuffType.WEAK, 1);
            state.enemies.get(idx).applyDebuff(DebuffType.VULNERABLE, 1);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class UppercutP extends Card {
        public UppercutP() {
            super("Uppercut+", Card.ATTACK);
            selectEnemy = true;
            vulnEnemy = true;
            weakEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 13);
            state.enemies.get(idx).applyDebuff(DebuffType.WEAK, 2);
            state.enemies.get(idx).applyDebuff(DebuffType.VULNERABLE, 2);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class Whirlwind extends Card {
        public Whirlwind() {
            super("Whirlwind", Card.ATTACK);
        }

        public void play(GameState state, int idx) {
            for (int i = 0; i < state.energy; i++) {
                state.player.doDamageToEnemy(state, state.enemies.get(idx), 5);
            }
        }

        public int energyCost(GameState state) {
            return state.energy;
        }
    }

    public static class WhirlwindP extends Card {
        public WhirlwindP() {
            super("Whirlwind+", Card.ATTACK);
        }

        public void play(GameState state, int idx) {
            for (int i = 0; i < state.energy; i++) {
                state.player.doDamageToEnemy(state, state.enemies.get(idx), 8);
            }
        }

        public int energyCost(GameState state) {
            return state.energy;
        }
    }

    // todo: barricade
    // todo: berserk

    public static class Bludgeon extends Card {
        public Bludgeon() {
            super("Bludgeon", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 32);
        }

        public int energyCost(GameState state) {
            return 3;
        }
    }

    public static class BludgeonP extends Card {
        public BludgeonP() {
            super("Bludgeon+", Card.ATTACK);
            selectEnemy = true;
        }

        public void play(GameState state, int idx) {
            state.player.doDamageToEnemy(state, state.enemies.get(idx), 42);
        }

        public int energyCost(GameState state) {
            return 3;
        }
    }

    // todo: brutality

    public static class Corruption extends Card {
        public Corruption() {
            super("Corruption", Card.POWER);
        }

        public void play(GameState state, int idx) {
            state.buffs |= PlayerBuffs.CORRUPTION;
        }

        public int energyCost(GameState state) {
            return 3;
        }
    }

    public static class CorruptionP extends Card {
        public CorruptionP() {
            super("Corruption+", Card.POWER);
        }

        public void play(GameState state, int idx) {
            state.buffs |= PlayerBuffs.CORRUPTION;
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    // todo: demon form

    // todo: double tap

    public static class Exhume extends Card {
        public Exhume() {
            super("Exhume", Card.SKILL);
            selectFromExhaust = true;
            exhaustWhenPlayed = true;
        }

        public void play(GameState state, int idx) {
            state.exhaust[idx] -= 1;
            state.addCardToHand(idx);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class ExhumeP extends Card {
        public ExhumeP() {
            super("Exhume+", Card.SKILL);
            selectFromExhaust = true;
            exhaustWhenPlayed = true;
        }

        public void play(GameState state, int idx) {
            state.exhaust[idx] -= 1;
            state.addCardToHand(idx);
        }

        public int energyCost(GameState state) {
            return 0;
        }
    }

    // todo: feed

    public static class FiendFire extends Card {
        public FiendFire() {
            super("Fiend Fire", Card.ATTACK);
            selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public void play(GameState state, int idx) {
            for (int i = 0; i < state.hand.length; i++) {
                while (state.hand[i] > 0) {
                    state.exhaustCardFromHand(i);
                    state.player.doDamageToEnemy(state, state.enemies.get(idx), 7);
                }
            }
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class FiendFireP extends Card {
        public FiendFireP() {
            super("Fiend Fire+", Card.ATTACK);
            selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public void play(GameState state, int idx) {
            for (int i = 0; i < state.hand.length; i++) {
                while (state.hand[i] > 0) {
                    state.exhaustCardFromHand(i);
                    state.player.doDamageToEnemy(state, state.enemies.get(idx), 10);
                }
            }
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class Immolate extends Card {
        public Immolate() {
            super("Immolate", Card.ATTACK);
        }

        public void play(GameState state, int idx) {
            for (Enemy enemy : state.enemies) {
                state.player.doDamageToEnemy(state, enemy, 21);
            }
            state.addCardToDiscard(state.prop.burnCardIdx);
        }

        public int energyCost(GameState state) {
            return 2;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return Arrays.asList(new Card.Burn());
        }
    }

    public static class ImmolateP extends Card {
        public ImmolateP() {
            super("Immolate+", Card.ATTACK);
        }

        public void play(GameState state, int idx) {
            for (Enemy enemy : state.enemies) {
                state.player.doDamageToEnemy(state, enemy, 28);
            }
            state.addCardToDiscard(state.prop.burnCardIdx);
        }

        public int energyCost(GameState state) {
            return 2;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return Arrays.asList(new Card.Burn());
        }
    }

    public static class Impervious extends Card {
        public Impervious() {
            super("Impervious", Card.SKILL);
            exhaustWhenPlayed = true;
        }

        public void play(GameState state, int idx) {
            state.player.gainBlock(30);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class ImperviousP extends Card {
        public ImperviousP() {
            super("Impervious+", Card.SKILL);
            exhaustWhenPlayed = true;
        }

        public void play(GameState state, int idx) {
            state.player.gainBlock(40);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    // todo: juggarnaut

    public static class LimitBreak extends Card {
        public LimitBreak() {
            super("Limit Break", Card.SKILL);
            exhaustWhenPlayed = true;
            changePlayerStrength = true;
        }

        public void play(GameState state, int idx) {
            state.player.gainStrength(state.player.strength);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class LimitBreakP extends Card {
        public LimitBreakP() {
            super("Limit Break+", Card.SKILL);
            changePlayerStrength = true;
        }

        public void play(GameState state, int idx) {
            state.player.gainStrength(state.player.strength);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class Offering extends Card {
        public Offering() {
            super("Offering", Card.SKILL);
            exhaustWhenPlayed = true;
        }

        public void play(GameState state, int idx) {
            state.player.nonAttackDamage(6, false);
            state.draw(3);
        }

        public int energyCost(GameState state) {
            return 0;
        }
    }

    public static class OfferingP extends Card {
        public OfferingP() {
            super("Offering+", Card.SKILL);
            exhaustWhenPlayed = true;
        }

        public void play(GameState state, int idx) {
            state.player.nonAttackDamage(6, false);
            state.draw(5);
        }

        public int energyCost(GameState state) {
            return 0;
        }
    }

    public static class Reaper extends Card {
        public Reaper() {
            super("Reaper", Card.ATTACK);
            exhaustWhenPlayed = true;
        }

        public void play(GameState state, int idx) {
            int amount = 0;
            for (Enemy enemy : state.enemies) {
                int prevHp = enemy.health;
                state.player.doDamageToEnemy(state, enemy, 4);
                amount += enemy.health - prevHp;
            }
            state.player.heal(amount);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    public static class ReaperP extends Card {
        public ReaperP() {
            super("Reaper+", Card.ATTACK);
            exhaustWhenPlayed = true;
        }

        public void play(GameState state, int idx) {
            int amount = 0;
            for (Enemy enemy : state.enemies) {
                int prevHp = enemy.health;
                state.player.doDamageToEnemy(state, enemy, 5);
                amount += enemy.health - prevHp;
            }
            state.player.heal(amount);
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }

    // **********************************************************************************************************
    // ********************************************* Statuses ***************************************************
    // **********************************************************************************************************

    public static class Wound extends Card {
        public Wound() {
            super("Wound", Card.STATUS);
        }

        void play(GameState state, int idx) {}

        public int energyCost(GameState state) {
            return -1;
        }
    }

    public static class Burn extends Card {
        public Burn() {
            super("Burn", Card.STATUS);
        }

        void play(GameState state, int idx) {}

        void onDiscard(GameState state) {
            state.player.nonAttackDamage(2, true);
        }

        public int energyCost(GameState state) {
            return -1;
        }
    }

    public static class Dazed extends Card {
        public Dazed() {
            super("Dazed", Card.STATUS);
            exhaustEndOfTurn = true;
        }

        void play(GameState state, int idx) {}

        public int energyCost(GameState state) {
            return -1;
        }
    }

    public static class Slime extends Card {
        public Slime() {
            super("Slime", Card.STATUS);
            exhaustWhenPlayed = true;
        }

        void play(GameState state, int idx) {}

        public int energyCost(GameState state) {
            return 1;
        }
    }

    // **********************************************************************************************************
    // ********************************************** Curses ****************************************************
    // **********************************************************************************************************

    // clumsy
    public static class AscendersBane extends Card {
        public AscendersBane() {
            super("Ascender's Bane", Card.CURSE);
            exhaustEndOfTurn = true;
        }

        void play(GameState state, int idx) {}

        public int energyCost(GameState state) {
            return -1;
        }
    }

    // todo: Necronomicurse
    // todo: Decay
    // todo: Doubt
    // todo: Normality
    // todo: Pain
    // todo: Regret
    // todo: Shame
    // todo: Writh
    // todo: Pride

    // Curse of the Bell
    // Injury
    // Parasite
    public static class UnplayableCurse extends Card {
        public UnplayableCurse() {
            super("Unplayable Curse", Card.CURSE);
        }

        void play(GameState state, int idx) {}

        public int energyCost(GameState state) {
            return -1;
        }
    }
}
