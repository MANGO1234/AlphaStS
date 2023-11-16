package com.alphaStS;

import com.alphaStS.Action.CardDrawAction;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyBeyond;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CardIronclad {
    public static class Bash extends Card {
        public Bash() {
            super("Bash", Card.ATTACK, 2, Card.COMMON);
            selectEnemy = true;
            vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 8);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BashP extends Card {
        public BashP() {
            super("Bash+", Card.ATTACK, 2, Card.COMMON);
            selectEnemy = true;
            vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 10);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, 3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Anger extends Card {
        public Anger() {
            super("Anger", Card.ATTACK, 0, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 6);
            state.addCardToDiscard(state.properties.angerCardIdx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class AngerP extends Card {
        public AngerP() {
            super("Anger+", Card.ATTACK, 0, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 8);
            state.addCardToDiscard(state.properties.angerPCardIdx);
            return GameActionCtx.PLAY_CARD;
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

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
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

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return cards.stream().map(Card::getUpgrade).filter(Objects::nonNull).toList();
        }
    }

    public static class BodySlam extends Card {
        public BodySlam() {
            super("Body Slam", Card.ATTACK, 1, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), state.getPlayeForRead().getBlock());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BodySlamP extends Card {
        public BodySlamP() {
            super("Body Slam+", Card.ATTACK, 0, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), state.getPlayeForRead().getBlock());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Clash extends Card {
        public Clash() {
            super("Clash", Card.ATTACK, 0, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 14);
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

    public static class ClashP extends Card {
        public ClashP() {
            super("Clash+", Card.ATTACK, 0, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 18);
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

    public static class Cleave extends Card {
        public Cleave() {
            super("Cleave", Card.ATTACK, 1, Card.COMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, 8);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class CleaveP extends Card {
        public CleaveP() {
            super("Cleave+", Card.ATTACK, 1, Card.COMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, 11);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Clothesline extends Card {
        public Clothesline() {
            super("Clothesline", Card.ATTACK, 2, Card.COMMON);
            selectEnemy = true;
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 12);
            enemy.applyDebuff(state, DebuffType.WEAK, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ClotheslineP extends Card {
        public ClotheslineP() {
            super("Clothesline+", Card.ATTACK, 2, Card.COMMON);
            selectEnemy = true;
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 14);
            enemy.applyDebuff(state, DebuffType.WEAK, 3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Flex extends Card {
        public Flex() {
            super("Flex", Card.SKILL, 0, Card.COMMON);
            changePlayerStrength = true;
            changePlayerStrengthEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var player = state.getPlayerForWrite();
            player.gainStrength(2);
            player.applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FlexP extends Card {
        public FlexP() {
            super("Flex+", Card.SKILL, 0, Card.COMMON);
            changePlayerStrength = true;
            changePlayerStrengthEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var player = state.getPlayerForWrite();
            player.gainStrength(4);
            player.applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, 4);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // todo: test
    private static abstract class _HavocT extends Card {
        public _HavocT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost, Card.COMMON);
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int cardIdx = state.drawOneCardSpecial();
            if (cardIdx < 0) {
                return GameActionCtx.PLAY_CARD;
            }
            if (state.properties.makingRealMove || state.properties.stateDescOn) {
                if (state.getStateDesc().length() > 0) state.getStateDesc().append(", ");
                state.getStateDesc().append(state.properties.cardDict[cardIdx].cardName);
            }
            state.addGameActionToStartOfDeque(curState -> {
                if (curState.properties.cardDict[cardIdx].cardType != Card.POWER) {
                    curState.exhaustedCardHandle(cardIdx, true);
                }
            });
            state.addGameActionToStartOfDeque(curState -> {
                var action = curState.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                curState.playCard(action, -1, true, false, false, true, -1, -1);
                while (curState.actionCtx == GameActionCtx.SELECT_ENEMY) {
                    int enemyIdx = GameStateUtils.getRandomEnemyIdx(curState, RandomGenCtx.RandomEnemyGeneral);
                    if (curState.properties.makingRealMove || curState.properties.stateDescOn) {
                        curState.getStateDesc().append(" -> ").append(curState.getEnemiesForRead().get(enemyIdx).getName()).append(" (").append(enemyIdx).append(")");
                    }
                    curState.playCard(action, enemyIdx, true, false, false, true, -1, -1);
                }
            });
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Havoc extends _HavocT {
        public Havoc() {
            super("Havoc", Card.SKILL, 1);
        }
    }

    public static class HavocP extends _HavocT {
        public HavocP() {
            super("Havoc+", Card.SKILL, 0);
        }
    }

    public static class Headbutt extends Card {
        public Headbutt() {
            super("Headbutt", Card.ATTACK, 1, Card.COMMON);
            selectEnemy = true;
            selectFromDiscard = true;
            selectFromDiscardLater = true;
            putCardOnTopDeck = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 9);
                return GameActionCtx.SELECT_CARD_DISCARD;
            } else {
                state.removeCardFromDiscard(idx);
                state.putCardOnTopOfDeck(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class HeadbuttP extends Card {
        public HeadbuttP() {
            super("Headbutt+", Card.ATTACK, 1, Card.COMMON);
            selectEnemy = true;
            selectFromDiscard = true;
            selectFromDiscardLater = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 9);
                return GameActionCtx.SELECT_CARD_DISCARD;
            } else {
                state.removeCardFromDiscard(idx);
                state.putCardOnTopOfDeck(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class HeavyBlade extends Card {
        public HeavyBlade() {
            super("Heavy Blade", Card.ATTACK, 2, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 14 + state.getPlayeForRead().getStrength() * 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class HeavyBladeP extends Card {
        public HeavyBladeP() {
            super("Heavy Blade+", Card.ATTACK, 2, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 14 + state.getPlayeForRead().getStrength() * 4);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class IronWave extends Card {
        public IronWave() {
            super("Iron Wave", Card.ATTACK, 1, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 5);
            state.getPlayerForWrite().gainBlock(5);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class IronWaveP extends Card {
        public IronWaveP() {
            super("Iron Wave+", Card.ATTACK, 1, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 7);
            state.getPlayerForWrite().gainBlock(7);
            return GameActionCtx.PLAY_CARD;
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
            for (int i = 0; i < state.properties.strikeCardIdxes.length; i++) {
                if (state.properties.strikeCardIdxes[i] < state.properties.realCardsLen) {
                    count += state.getDeckForRead()[state.properties.strikeCardIdxes[i]];
                }
            }
            int dmg = 6 + 2 * count + (state.properties.hasStrikeDummy ? 3 : 0);
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
            for (int i = 0; i < state.properties.strikeCardIdxes.length; i++) {
                if (state.properties.strikeCardIdxes[i] < state.properties.realCardsLen) {
                    count += state.getDeckForRead()[state.properties.strikeCardIdxes[i]];
                }
            }
            int dmg = 6 + 3 * count + (state.properties.hasStrikeDummy ? 3 : 0);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), dmg);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PommelStrike extends Card {
        public PommelStrike() {
            super("Pommel Strike", Card.ATTACK, 1, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 9 + (state.properties.hasStrikeDummy ? 3 : 0));
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PommelStrikeP extends Card {
        public PommelStrikeP() {
            super("Pommel Strike+", Card.ATTACK, 1, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 10 + (state.properties.hasStrikeDummy ? 3 : 0));
            state.draw(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ShrugItOff extends Card {
        public ShrugItOff() {
            super("Shrug It Off", Card.SKILL, 1, Card.COMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(8);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ShrugItOffP extends Card {
        public ShrugItOffP() {
            super("Shrug It Off+", Card.SKILL, 1, Card.COMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(11);
            state.draw(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _SwordBoomerangT extends Card {
        private final int n;

        public _SwordBoomerangT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
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
            super("Thunderclap", Card.ATTACK, 1, Card.COMMON);
            vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, 4);
                enemy.applyDebuff(state, DebuffType.VULNERABLE, 1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ThunderclapP extends Card {
        public ThunderclapP() {
            super("Thunderclap+", Card.ATTACK, 1, Card.COMMON);
            vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, 7);
                enemy.applyDebuff(state, DebuffType.VULNERABLE, 1);
            }
            return GameActionCtx.PLAY_CARD;
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

    public static class TwinStrike extends Card {
        public TwinStrike() {
            super("Twin Strike", Card.ATTACK, 1, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 5 + (state.properties.hasStrikeDummy ? 3 : 0));
            state.playerDoDamageToEnemy(enemy, 5 + (state.properties.hasStrikeDummy ? 3 : 0));
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class TwinStrikeP extends Card {
        public TwinStrikeP() {
            super("Twin Strike+", Card.ATTACK, 1, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 7 + (state.properties.hasStrikeDummy ? 3 : 0));
            state.playerDoDamageToEnemy(enemy, 7 + (state.properties.hasStrikeDummy ? 3 : 0));
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _WarcryT extends Card {
        private final int n;

        public _WarcryT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.COMMON);
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
                state.putCardOnTopOfDeck(idx);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class Warcry extends _WarcryT {
        public Warcry() {
            super("Warcry", Card.SKILL, 0, 1);
        }
    }

    public static class WarcryP extends _WarcryT {
        public WarcryP() {
            super("Warcry+", Card.SKILL, 0, 2);
        }
    }

    public static class WildStrike extends Card {
        public WildStrike() {
            super("Wild Strike", Card.ATTACK, 1, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 12 + (state.properties.hasStrikeDummy ? 3 : 0));
            state.addCardToDeck(state.properties.woundCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new CardOther.Wound());
        }
    }

    public static class WildStrikeP extends Card {
        public WildStrikeP() {
            super("Wild Strike+", Card.ATTACK, 1, Card.COMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 17 + (state.properties.hasStrikeDummy ? 3 : 0));
            state.addCardToDeck(state.properties.woundCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new CardOther.Wound());
        }
    }

    public static class BattleTrance extends Card {
        public BattleTrance() {
            super("Battle Trance", Card.SKILL, 0, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.draw(3);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.NO_MORE_CARD_DRAW, 1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BattleTranceP extends Card {
        public BattleTranceP() {
            super("Battle Trance+", Card.SKILL, 0, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
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
            super("Blood For Blood (" + energyCost + ")", Card.ATTACK, energyCost, Card.UNCOMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 18);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new BloodForBlood(3), new BloodForBlood(2), new BloodForBlood(1), new BloodForBlood(0));
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.bloodForBloodIndexes = new int[5];
            for (int i = 0; i < 5; i++) {
                state.properties.bloodForBloodIndexes[i] = state.properties.findCardIndex(new BloodForBlood(i));
            }
            state.properties.bloodForBloodTransformIndexes = new int[state.properties.cardDict.length];
            Arrays.fill(state.properties.bloodForBloodTransformIndexes, -1);
            for (int i = 0; i < 4; i++) {
                state.properties.bloodForBloodTransformIndexes[state.properties.bloodForBloodIndexes[i + 1]] = state.properties.bloodForBloodIndexes[i];
            }
            state.properties.addOnDamageHandler("Blood For Blood", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (damageDealt <= 0) return;
                    for (int i = 0; i < 4; i++) {
                        if (state.getDeckForRead()[state.properties.bloodForBloodIndexes[i + 1]] > 0) {
                            state.getDeckForWrite()[state.properties.bloodForBloodIndexes[i]] += state.getDeckForWrite()[state.properties.bloodForBloodIndexes[i + 1]];
                            state.getDeckForWrite()[state.properties.bloodForBloodIndexes[i + 1]] = 0;
                        }
                        var exhaust = state.getExhaustForWrite();
                        exhaust[state.properties.bloodForBloodIndexes[i]] += exhaust[state.properties.bloodForBloodIndexes[i + 1]];
                        exhaust[state.properties.bloodForBloodIndexes[i + 1]] = 0;
                    }
                    state.handArrTransform(state.properties.bloodForBloodTransformIndexes);
                    state.discardArrTransform(state.properties.bloodForBloodTransformIndexes);
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

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new BloodForBloodP(2), new BloodForBloodP(1), new BloodForBloodP(0));
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.bloodForBloodPIndexes = new int[4];
            for (int i = 0; i < 4; i++) {
                state.properties.bloodForBloodPIndexes[i] = state.properties.findCardIndex(new BloodForBloodP(i));
            }
            state.properties.bloodForBloodPTransformIndexes = new int[state.properties.cardDict.length];
            Arrays.fill(state.properties.bloodForBloodPTransformIndexes, -1);
            for (int i = 0; i < 4; i++) {
                state.properties.bloodForBloodPTransformIndexes[state.properties.bloodForBloodPIndexes[i + 1]] = state.properties.bloodForBloodPIndexes[i];
            }
            state.properties.addOnDamageHandler("Blood For Blood+", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (damageDealt <= 0) return;
                    for (int i = 0; i < 3; i++) {
                        if (state.getDeckForRead()[state.properties.bloodForBloodPIndexes[i + 1]] > 0) {
                            state.getDeckForWrite()[state.properties.bloodForBloodPIndexes[i]] += state.getDeckForWrite()[state.properties.bloodForBloodPIndexes[i + 1]];
                            state.getDeckForWrite()[state.properties.bloodForBloodPIndexes[i + 1]] = 0;
                        }
                        var exhaust = state.getExhaustForWrite();
                        exhaust[state.properties.bloodForBloodPIndexes[i]] += exhaust[state.properties.bloodForBloodPIndexes[i + 1]];
                        exhaust[state.properties.bloodForBloodPIndexes[i + 1]] = 0;
                    }
                    state.handArrTransform(state.properties.bloodForBloodPTransformIndexes);
                    state.discardArrTransform(state.properties.bloodForBloodPTransformIndexes);
                }
            });
        }
    }

    public static class Bloodletting extends Card {
        public Bloodletting() {
            super("Bloodletting", Card.SKILL, 0, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.doNonAttackDamageToPlayer(3, false, this);
            state.gainEnergy(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BloodlettingP extends Card {
        public BloodlettingP() {
            super("Bloodletting+", Card.SKILL, 0, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.doNonAttackDamageToPlayer(3, false, this);
            state.gainEnergy(3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BurningPact extends Card {
        public BurningPact() {
            super("Burning Pact", Card.SKILL, 1, Card.UNCOMMON);
            selectFromHand =  true;
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.exhaustCardFromHand(idx);
            state.draw(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BurningPactP extends Card {
        public BurningPactP() {
            super("Burning Pact+", Card.SKILL, 1, Card.UNCOMMON);
            selectFromHand =  true;
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.exhaustCardFromHand(idx);
            state.draw(3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Carnage extends Card {
        public Carnage() {
            super("Carnage", Card.ATTACK, 2, Card.UNCOMMON);
            selectEnemy = true;
            ethereal = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 20);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class CarnageP extends Card {
        public CarnageP() {
            super("Carnage+", Card.ATTACK, 2, Card.UNCOMMON);
            selectEnemy = true;
            ethereal = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 28);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _CombustT extends Card {
        private final int n;

        public _CombustT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
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
            super("Dark Embrace", Card.POWER, 2, Card.UNCOMMON);
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

    public static class DarkEmbraceP extends Card {
        public DarkEmbraceP() {
            super("Dark Embrace+", Card.POWER, 1, Card.UNCOMMON);
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

    public static class Disarm extends Card {
        public Disarm() {
            super("Disarm", Card.SKILL, 1, Card.UNCOMMON);
            exhaustWhenPlayed = true;
            selectEnemy = true;
            affectEnemyStrength = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.LOSE_STRENGTH, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class DisarmP extends Card {
        public DisarmP() {
            super("Disarm+", Card.SKILL, 1, Card.UNCOMMON);
            exhaustWhenPlayed = true;
            selectEnemy = true;
            affectEnemyStrength = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.LOSE_STRENGTH, 3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Dropkick extends Card {
        public Dropkick() {
            super("Dropkick", Card.ATTACK, 1, Card.UNCOMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
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
            super("Dropkick+", Card.ATTACK, 1, Card.UNCOMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
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
            super("Dual Wield", Card.SKILL, 1, Card.UNCOMMON);
            selectFromHand = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.addCardToHand(idx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public boolean canSelectCard(Card card) {
            return card.cardType == Card.ATTACK;
        }

        @Override List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return cards.stream().filter(card -> card.cardType == Card.ATTACK).toList();
        }
    }

    public static class DualWieldP extends Card {
        public DualWieldP() {
            super("Dual Wield+", Card.SKILL, 1, Card.UNCOMMON);
            selectFromHand = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.addCardToHand(idx);
            state.addCardToHand(idx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public boolean canSelectCard(Card card) {
            return card.cardType == Card.ATTACK;
        }

        @Override List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return cards.stream().filter(card -> card.cardType == Card.ATTACK).toList();
        }
    }

    public static class Entrench extends Card {
        public Entrench() {
            super("Entrench", Card.SKILL, 2, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var player = state.getPlayerForWrite();
            player.gainBlockNotFromCardPlay(player.getBlock());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class EntrenchP extends Card {
        public EntrenchP() {
            super("Entrench+", Card.SKILL, 1, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var player = state.getPlayerForWrite();
            player.gainBlockNotFromCardPlay(player.getBlock());
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _EvolveT extends Card {
        private final int n;

        public _EvolveT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
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
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.STATUS) {
                        state.addGameActionToEndOfDeque(new CardDrawAction(state.getCounterForRead()[counterIdx]));
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
            super("Feel No Pain", Card.POWER, 1, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += 3;
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

    public static class FeelNoPainP extends Card {
        public FeelNoPainP() {
            super("Feel No Pain+", Card.POWER, 1, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += 4;
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

    private abstract static class _FireBreathingT extends Card {
        private final int n;

        public _FireBreathingT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
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
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
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
            super("Flame Barrier", Card.SKILL, 2, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(12);
            state.getCounterForWrite()[counterIdx] += 4;
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

    public static class FlameBarrierP extends Card {
        public FlameBarrierP() {
            super("Flame Barrier+", Card.SKILL, 2, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(16);
            state.getCounterForWrite()[counterIdx] += 6;
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

    public static class GhostlyArmor extends Card {
        public GhostlyArmor() {
            super("Ghostly Armor", Card.SKILL, 1, Card.UNCOMMON);
            ethereal = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(10);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class GhostlyArmorP extends Card {
        public GhostlyArmorP() {
            super("Ghostly Armor+", Card.SKILL, 1, Card.UNCOMMON);
            ethereal = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(13);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Hemokinesis extends Card {
        public Hemokinesis() {
            super("Hemokinesis", Card.ATTACK, 1, Card.UNCOMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.doNonAttackDamageToPlayer(2, false, this);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 15);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class HemokinesisP extends Card {
        public HemokinesisP() {
            super("Hemokinesis+", Card.ATTACK, 1, Card.UNCOMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.doNonAttackDamageToPlayer(2, false, this);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 20);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private abstract static class _InfernalBladeT extends Card {
        public _InfernalBladeT(String cardName, int cardType, int energyCost) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var r = state.getSearchRandomGen().nextInt(state.properties.infernalBladeIndexes.length, RandomGenCtx.CardGeneration);
            state.addCardToHand(state.properties.infernalBladeIndexes[r]);
            state.setIsStochastic();
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            if (state.properties.infernalBladeIndexes == null) {
                state.properties.infernalBladeIndexes = new int[29];
                state.properties.infernalBladeIndexes[0] = state.properties.findCardIndex(new CardIronclad.Anger());
                state.properties.infernalBladeIndexes[1] = state.properties.findCardIndex(new CardIronclad.BodySlam());
                state.properties.infernalBladeIndexes[2] = state.properties.findCardIndex(new CardIronclad.Clash());
                state.properties.infernalBladeIndexes[3] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.Cleave(), 0));
                state.properties.infernalBladeIndexes[4] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.Clothesline(), 0));
                state.properties.infernalBladeIndexes[5] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.Headbutt(), 0));
                state.properties.infernalBladeIndexes[6] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.HeavyBlade(), 0));
                state.properties.infernalBladeIndexes[7] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.IronWave(), 0));
                state.properties.infernalBladeIndexes[8] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.PerfectedStrike(), 0));
                state.properties.infernalBladeIndexes[9] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.PommelStrike(), 0));
                state.properties.infernalBladeIndexes[10] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.SwordBoomerang(), 0));
                state.properties.infernalBladeIndexes[11] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.Thunderclap(), 0));
                state.properties.infernalBladeIndexes[12] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.TwinStrike(), 0));
                state.properties.infernalBladeIndexes[13] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.WildStrike(), 0));
                state.properties.infernalBladeIndexes[14] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.BloodForBlood(), 0));
                state.properties.infernalBladeIndexes[15] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.Carnage(), 0));
                state.properties.infernalBladeIndexes[16] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.Dropkick(), 0));
                state.properties.infernalBladeIndexes[17] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.Hemokinesis(), 0));
                state.properties.infernalBladeIndexes[18] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.Pummel(), 0));
                state.properties.infernalBladeIndexes[19] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.Rampage(), 0));
                state.properties.infernalBladeIndexes[20] = state.properties.findCardIndex(new CardIronclad.RecklessCharge());
                state.properties.infernalBladeIndexes[21] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.SearingBlow(0), 0));
                state.properties.infernalBladeIndexes[22] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.SeverSoul(), 0));
                state.properties.infernalBladeIndexes[23] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.Uppercut(), 0));
                state.properties.infernalBladeIndexes[24] = state.properties.findCardIndex(new CardIronclad.Whirlwind());
                state.properties.infernalBladeIndexes[25] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.Bludgeon(), 0));
                state.properties.infernalBladeIndexes[26] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.FiendFire(), 0));
                state.properties.infernalBladeIndexes[27] = state.properties.findCardIndex(new Card.CardTmpChangeCost(new CardIronclad.Immolate(), 0));
            }
        }

        @Override public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(
                    new CardIronclad.Anger(),
                    new CardIronclad.BodySlam(),
                    new CardIronclad.Clash(),
                    new Card.CardTmpChangeCost(new CardIronclad.Cleave(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.Clothesline(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.Headbutt(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.HeavyBlade(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.IronWave(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.PerfectedStrike(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.PommelStrike(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.SwordBoomerang(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.Thunderclap(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.TwinStrike(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.WildStrike(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.BloodForBlood(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.Carnage(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.Dropkick(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.Hemokinesis(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.Pummel(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.Rampage(), 0),
                    new CardIronclad.RecklessCharge(),
                    new Card.CardTmpChangeCost(new CardIronclad.SearingBlow(0), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.SeverSoul(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.Uppercut(), 0),
                    new CardIronclad.Whirlwind(),
                    new Card.CardTmpChangeCost(new CardIronclad.Bludgeon(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.FiendFire(), 0),
                    new Card.CardTmpChangeCost(new CardIronclad.Immolate(), 0)
            );
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
            super("Inflame", Card.POWER, 1, Card.UNCOMMON);
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainStrength(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class InflameP extends Card {
        public InflameP() {
            super("Inflame+", Card.POWER, 1, Card.UNCOMMON);
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainStrength(3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Intimidate extends Card {
        public Intimidate() {
            super("Intimidate", Card.SKILL, 0, Card.UNCOMMON);
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.WEAK, 1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class IntimidateP extends Card {
        public IntimidateP() {
            super("Intimidate+", Card.SKILL, 0, Card.UNCOMMON);
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.WEAK, 2);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Metallicize extends Card {
        public Metallicize() {
            super("Metallicize", Card.POWER, 1, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += 3;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Metallicize", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public void onRegister(int counterIdx) {
                    state.properties.registerMetallicizeHandler(state, counterIdx);
                }
            });
        }
    }

    public static class MetallicizeP extends Card {
        public MetallicizeP() {
            super("Metallicize+", Card.POWER, 1, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += 4;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Metallicize", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public void onRegister(int counterIdx) {
                    state.properties.registerMetallicizeHandler(state, counterIdx);
                }
            });
        }
    }

    public static class PowerThrough extends Card {
        public PowerThrough() {
            super("Power Through", Card.SKILL, 1, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(15);
            state.addCardToHand(state.properties.woundCardIdx);
            state.addCardToHand(state.properties.woundCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new CardOther.Wound());
        }
    }

    public static class PowerThroughP extends Card {
        public PowerThroughP() {
            super("Power Through+", Card.SKILL, 1, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(20);
            state.addCardToHand(state.properties.woundCardIdx);
            state.addCardToHand(state.properties.woundCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new CardOther.Wound());
        }
    }

    public static class Pummel extends Card {
        public Pummel() {
            super("Pummel", Card.ATTACK, 1, Card.UNCOMMON);
            selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < 4; i++) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 2);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PummelP extends Card {
        public PummelP() {
            super("Pummel+", Card.ATTACK, 1, Card.UNCOMMON);
            selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < 5; i++) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 2);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _RageT extends Card {
        private final int n;

        public _RageT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
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
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        state.getPlayerForWrite().gainBlock(state.getCounterForRead()[counterIdx]);
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
            super("Rampage", Card.ATTACK, 1, Card.UNCOMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 8);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class RampageP extends Card {
        public RampageP() {
            super("Rampage+", Card.ATTACK, 1, Card.UNCOMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 8);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class RecklessCharge extends Card {
        public RecklessCharge() {
            super("Reckless Charge", Card.ATTACK, 0, Card.UNCOMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 7);
            state.addCardToDeck(state.properties.dazedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new CardOther.Dazed());
        }
    }

    public static class RecklessChargeP extends Card {
        public RecklessChargeP() {
            super("Reckless Charge+", Card.ATTACK, 0, Card.UNCOMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 10);
            state.addCardToDeck(state.properties.dazedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new CardOther.Dazed());
        }
    }

    private static abstract class _RuptureT extends Card {
        private final int n;

        public _RuptureT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.UNCOMMON);
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
            // todo: test
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
            super("Rupture", Card.POWER, 1, 1);
        }
    }

    public static class RuptureP extends _RuptureT {
        public RuptureP() {
            super("Rupture", Card.POWER, 1, 2);
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

    public static class SecondWind extends Card {
        public SecondWind() {
            super("Second Wind", Card.SKILL, 1, Card.UNCOMMON);
            exhaustNonAttacks = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int c = state.handArrLen;
            for (int i = 0; i < c; i++) {
                state.exhaustCardFromHandByPosition(i, false);
            }
            state.updateHandArr();
            for (int i = 0; i < c; i++) {
                state.getPlayerForWrite().gainBlock(5);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SecondWindP extends Card {
        public SecondWindP() {
            super("Second Wind+", Card.SKILL, 1, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int c = state.handArrLen;
            for (int i = 0; i < c; i++) {
                state.exhaustCardFromHandByPosition(i, false);
            }
            state.updateHandArr();
            for (int i = 0; i < c; i++) {
                state.getPlayerForWrite().gainBlock(7);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SeeingRed extends Card {
        public SeeingRed() {
            super("Seeing Red", Card.SKILL, 1, Card.UNCOMMON);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SeeingRedP extends Card {
        public SeeingRedP() {
            super("Seeing Red+", Card.SKILL, 0, Card.UNCOMMON);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Sentinel extends Card {
        public Sentinel() {
            super("Sentinel", Card.SKILL, 1, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(5);
            return GameActionCtx.PLAY_CARD;
        }

        public void onExhaust(GameState state) {
            state.energy += 2;
        }
    }

    public static class SentinelP extends Card {
        public SentinelP() {
            super("Sentinel+", Card.SKILL, 1, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(8);
            return GameActionCtx.PLAY_CARD;
        }

        public void onExhaust(GameState state) {
            state.energy += 3;
        }
    }

    public static class SeverSoul extends Card {
        public SeverSoul() {
            super("Sever Soul", Card.ATTACK, 2, Card.UNCOMMON);
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 16);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SeverSoulP extends Card {
        public SeverSoulP() {
            super("Sever Soul+", Card.ATTACK, 2, Card.UNCOMMON);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < state.handArrLen; i++) {
                if (state.properties.cardDict[state.getHandArrForRead()[i]].cardType != Card.ATTACK) {
                    state.exhaustCardFromHandByPosition(i, false);
                }
            }
            state.updateHandArr();
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 22);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Shockwave extends Card {
        public Shockwave() {
            super("Shockwave", Card.SKILL, 2, Card.UNCOMMON);
            exhaustWhenPlayed = true;
            vulnEnemy = true;
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.WEAK, 3);
                enemy.applyDebuff(state, DebuffType.VULNERABLE, 3);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ShockwaveP extends Card {
        public ShockwaveP() {
            super("Shockwave+", Card.SKILL, 2, Card.UNCOMMON);
            exhaustWhenPlayed = true;
            vulnEnemy = true;
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.applyDebuff(state, DebuffType.WEAK, 5);
                enemy.applyDebuff(state, DebuffType.VULNERABLE, 5);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SpotWeakness extends Card {
        public SpotWeakness() {
            super("Spot Weakness", Card.SKILL, 1, Card.UNCOMMON);
            selectEnemy = true;
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.getEnemiesForRead().get(idx).getMoveString(state).contains("Attack")) {
                state.getPlayerForWrite().gainStrength(3);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SpotWeaknessP extends Card {
        public SpotWeaknessP() {
            super("Spot Weakness+", Card.SKILL, 1, Card.UNCOMMON);
            selectEnemy = true;
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.getEnemiesForRead().get(idx).getMoveString(state).contains("Attack")) {
                state.getPlayerForWrite().gainStrength(4);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Uppercut extends Card {
        public Uppercut() {
            super("Uppercut", Card.ATTACK, 2, Card.UNCOMMON);
            selectEnemy = true;
            vulnEnemy = true;
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 13);
            enemy.applyDebuff(state, DebuffType.WEAK, 1);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, 1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class UppercutP extends Card {
        public UppercutP() {
            super("Uppercut+", Card.ATTACK, 2, Card.UNCOMMON);
            selectEnemy = true;
            vulnEnemy = true;
            weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, 13);
            enemy.applyDebuff(state, DebuffType.WEAK, 2);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, 2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Whirlwind extends Card {
        public Whirlwind() {
            super("Whirlwind", Card.ATTACK, -1, Card.UNCOMMON);
            isXCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < energyUsed; i++) {
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
            super("Whirlwind+", Card.ATTACK, -1, Card.UNCOMMON);
            isXCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < energyUsed; i++) {
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
            super("Barricade", Card.POWER, 3, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.buffs |= PlayerBuff.BARRICADE.mask();
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BarricadeP extends Card {
        public BarricadeP() {
            super("Barricade+", Card.POWER, 2, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.buffs |= PlayerBuff.BARRICADE.mask();
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Berserk extends Card {
        public Berserk() {
            super("Berserk", Card.POWER, 0, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
            state.energyRefill += 1;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BerserkP extends Card {
        public BerserkP() {
            super("Berserk+", Card.POWER, 0, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 1);
            state.energyRefill += 1;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Bludgeon extends Card {
        public Bludgeon() {
            super("Bludgeon", Card.ATTACK, 3, Card.RARE);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 32);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BludgeonP extends Card {
        public BludgeonP() {
            super("Bludgeon+", Card.ATTACK, 3, Card.RARE);
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 42);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _BrutalityT extends Card {
        public _BrutalityT(String cardName, int cardType, int energyCost, boolean innate) {
            super(cardName, cardType, energyCost, Card.RARE);
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
            super("Corruption", Card.POWER, 3, Card.RARE);
            exhaustSkill = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.buffs |= PlayerBuff.CORRUPTION.mask();
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class CorruptionP extends Card {
        public CorruptionP() {
            super("Corruption+", Card.POWER, 2, Card.RARE);
            exhaustSkill = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.buffs |= PlayerBuff.CORRUPTION.mask();
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _DemonFormT extends Card {
        private final int n;

        public _DemonFormT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
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
            super(cardName, cardType, energyCost, Card.RARE);
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
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    var card = state.properties.cardDict[cardIdx];
                    if (cloned || card.cardType != Card.ATTACK) {
                        return;
                    } else if (state.getCounterForRead()[counterIdx] == 0) {
                        return;
                    }
                    var counters = state.getCounterForWrite();
                    counters[counterIdx] -= 1;
                    state.addGameActionToEndOfDeque(curState -> {
                        var action = curState.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                        curState.playCard(action, lastIdx, true, true, false, false, energyUsed, cloneParentLocation);
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
            super("Exhume", Card.SKILL, 1, Card.RARE);
            selectFromExhaust = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getExhaustForWrite()[idx] -= 1;
            state.addCardToHand(idx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ExhumeP extends Card {
        public ExhumeP() {
            super("Exhume+", Card.SKILL, 0, Card.RARE);
            selectFromExhaust = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getExhaustForWrite()[idx] -= 1;
            state.addCardToHand(idx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static abstract class _FeedT extends Card {
        private final int n;
        private final int hpInc;
        protected final double healthRewardRatio;

        public _FeedT(String cardName, int cardType, int energyCost, int n, int hpInc, double healthRewardRatio) {
            super(cardName, cardType, energyCost, Card.RARE);
            this.n = n;
            this.hpInc = hpInc;
            this.healthRewardRatio = healthRewardRatio;
            healPlayer = true;
            exhaustWhenPlayed = true;
            selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n);
            if (!state.getEnemiesForRead().get(idx).properties.isMinion && !state.properties.isHeartFight && state.getEnemiesForRead().get(idx).getHealth() <= 0) {
                if (state.getEnemiesForRead().get(idx) instanceof EnemyBeyond.Darkling ||
                        state.getEnemiesForRead().get(idx) instanceof EnemyBeyond.AwakenedOne) {
                    if (state.isTerminal() > 0) {
                        state.getPlayerForWrite().heal(hpInc);
                        state.getCounterForWrite()[state.properties.feedCounterIdx] += hpInc;
                    }
                } else {
                    state.getPlayerForWrite().heal(hpInc);
                    state.getCounterForWrite()[state.properties.feedCounterIdx] += hpInc;
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
                    @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                        if (isTerminal > 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getCounterForRead()[counterIdx] / 16.0;
                        } else if (isTerminal == 0) {
                            int minFeed = state.getCounterForRead()[counterIdx];
                            int maxFeedRemaining = getMaxPossibleFeedRemaining(state);
                            double vFeed = Math.max(minFeed / 16.0, Math.min((minFeed + maxFeedRemaining) / 16.0, state.getVOther(vArrayIdx)));
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = vFeed;
                        }
                    }

                    @Override public void updateQValues(GameState state, double[] v) {
                        int minFeed = state.getCounterForRead()[counterIdx];
                        int maxFeedRemaining = getMaxPossibleFeedRemaining(state);
                        double vFeed = Math.max(minFeed / 16.0, Math.min((minFeed + maxFeedRemaining) / 16.0, v[GameState.V_OTHER_IDX_START + vArrayIdx]));
                        if (true) {
                            v[GameState.V_HEALTH_IDX] += 16 * vFeed * healthRewardRatio / state.getPlayeForRead().getMaxHealth();
                        } else {
                            v[GameState.V_HEALTH_IDX] *= (0.8 + vFeed / 0.25 * 0.2);
                        }
                    }
                });
            }
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            counterIdx = idx;
            gameProperties.feedCounterIdx = idx;
        }

        private static int getCardCount(GameState state, int idx) {
            int count = 0;
            count += GameStateUtils.getCardCount(state.getHandArrForRead(), state.getNumCardsInHand(), idx);
            if (idx < state.properties.realCardsLen) {
                count += GameStateUtils.getCardCount(state.getDiscardArrForRead(), state.getNumCardsInDiscard(), idx);
                count += state.getDeckForRead()[idx];
            }
            return count;
        }

        public static int getMaxPossibleFeedRemaining(GameState state) {
            if (state.isTerminal() != 0 || state.properties.isHeartFight) {
                return 0;
            }
            // todo: very very hacky
            var remain = 0;
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
            remain += Math.min(state.enemiesAlive, maxFeedableP) * 4;
            if (state.enemiesAlive > maxFeedableP) {
                remain += Math.min(state.enemiesAlive - maxFeedableP, maxFeedable) * 3;
            }
            return remain;
        }
    }

    public static class Feed extends _FeedT {
        public Feed() {
            super("Feed", Card.ATTACK, 1, 10, 3, 2);
        }

        public Feed(double healthRewardRatio) {
            super("Feed", Card.ATTACK, 1, 10, 3, healthRewardRatio);
        }

        public Card getUpgrade() {
            return new CardIronclad.FeedP(healthRewardRatio);
        }
    }

    public static class FeedP extends _FeedT {
        public FeedP() {
            super("Feed+", Card.ATTACK, 1, 12, 4, 2);
        }

        public FeedP(double healthRewardRatio) {
            super("Feed+", Card.ATTACK, 1, 12, 4, healthRewardRatio);
        }
    }

    public static class FiendFire extends Card {
        public FiendFire() {
            super("Fiend Fire", Card.ATTACK, 2, Card.RARE);
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
                    state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 7);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FiendFireP extends Card {
        public FiendFireP() {
            super("Fiend Fire+", Card.ATTACK, 2, Card.RARE);
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
                    state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 10);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Immolate extends Card {
        public Immolate() {
            super("Immolate", Card.ATTACK, 2, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, 21);
            }
            state.addCardToDiscard(state.properties.burnCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new CardOther.Burn());
        }
    }

    public static class ImmolateP extends Card {
        public ImmolateP() {
            super("Immolate+", Card.ATTACK, 2, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, 28);
            }
            state.addCardToDiscard(state.properties.burnCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
            return List.of(new CardOther.Burn());
        }
    }

    public static class Impervious extends Card {
        public Impervious() {
            super("Impervious", Card.SKILL, 2, Card.RARE);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(30);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ImperviousP extends Card {
        public ImperviousP() {
            super("Impervious+", Card.SKILL, 2, Card.RARE);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(40);
            return GameActionCtx.PLAY_CARD;
        }
    }

    private static abstract class _JuggernautT extends Card {
        private final int n;

        public _JuggernautT(String cardName, int cardType, int energyCost, int n) {
            super(cardName, cardType, energyCost, Card.RARE);
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
            super("Limit Break", Card.SKILL, 1, Card.RARE);
            exhaustWhenPlayed = true;
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var player = state.getPlayerForWrite();
            player.gainStrength(player.getStrength());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class LimitBreakP extends Card {
        public LimitBreakP() {
            super("Limit Break+", Card.SKILL, 1, Card.RARE);
            changePlayerStrength = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var player = state.getPlayerForWrite();
            player.gainStrength(player.getStrength());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Offering extends Card {
        public Offering() {
            super("Offering", Card.SKILL, 0, Card.RARE);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.doNonAttackDamageToPlayer(6, false, this);
            state.draw(3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class OfferingP extends Card {
        public OfferingP() {
            super("Offering+", Card.SKILL, 0, Card.RARE);
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.doNonAttackDamageToPlayer(6, false, this);
            state.draw(5);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Reaper extends Card {
        public Reaper() {
            super("Reaper", Card.ATTACK, 2, Card.RARE);
            healPlayer = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int amount = 0;
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                int prevHp = enemy.getHealth();
                state.playerDoDamageToEnemy(enemy, 4);
                amount += prevHp - enemy.getHealth();
            }
            state.healPlayer(amount);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ReaperP extends Card {
        public ReaperP() {
            super("Reaper+", Card.ATTACK, 2, Card.RARE);
            healPlayer = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int amount = 0;
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                int prevHp = enemy.getHealth();
                state.playerDoDamageToEnemy(enemy, 5);
                amount += prevHp - enemy.getHealth();
            }
            state.healPlayer(amount);
            return GameActionCtx.PLAY_CARD;
        }
    }
}
