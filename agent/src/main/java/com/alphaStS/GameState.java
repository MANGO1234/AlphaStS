package com.alphaStS;

import com.alphaStS.utils.DrawOrder;

import java.util.*;

class GameProperties {
    boolean playerStrengthCanChange;
    boolean playerDexterityCanChange;
    boolean playerCanGetVuln;
    boolean playerCanGetWeakened;
    boolean playerCanGetFrailed;
    boolean playerThornCanChange;
    boolean enemyCanGetVuln;
    boolean enemyCanGetWeakened;
    long possibleBuffs;
    boolean needDeckOrderMemory;
    Random random;
    Card[] cardDict;
    int maxNumOfActions;
    int totalNumOfActions;
    GameAction[][] actionsByCtx;

    // cached card indexes
    int[] discardIdxes; // cards that can change in number of copies during a fight
    int[] upgradeIdxes;
    int angerCardIdx = -1;
    int angerPCardIdx = -1;
    int[] strikeCardIdxes;
    int burnCardIdx = -1;
    int dazedCardIdx = -1;
    int slimeCardIdx = -1;
    int woundCardIdx = -1;

    List<GameTrigger> preEndTurnTrigger;
}

enum GameActionCtx {
    START_GAME,
    PLAY_CARD,
    SELECT_ENEMY,
    SELECT_CARD_DISCARD,
    SELECT_CARD_HAND,
    SELECT_POTION,
}

enum GameActionType {
    START_GAME,
    PLAY_CARD,
    SELECT_ENEMY,
    SELECT_CARD_DISCARD,
    SELECT_CARD_HAND,
    SELECT_POTION,
    END_TURN,
}

record GameAction(GameActionType type, int cardIdx, int enemyIdx) {
}

public class GameState implements State {
    private static final int HAND_LIMIT = 10;
    private static final int MAX_AGENT_DECK_ORDER_MEMORY = 1;

    boolean isStochastic;
    Map<State, State> transpositions;
    boolean[] transpositions_policy_mask;
    GameProperties prop;
    private boolean[] actionsCache;
    GameActionCtx actionCtx;

    int[] deck;
    int[] hand;
    int[] discard;
    int[] deckArr;
    int deckArrLen;
    int energy;
    int energyRefill;
    List<Enemy> enemies;
    int enemiesAlive;
    Player player;
    Card previousCard;
    int previousCardIdx;
    short turn_num;
    private DrawOrder drawOrder;

    // various other buffs/debuffs
    long buffs;
    int thorn;
    int thornLoseEOT;

    double v_win;
    double v_health;
    double[] q;
    int[] n;
    State[] ns;
    int total_n;
    double total_q;
    float[] policy;

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GameState gameState = (GameState) o;
        return energy == gameState.energy && energyRefill == gameState.energyRefill && enemiesAlive == gameState.enemiesAlive && previousCardIdx == gameState.previousCardIdx && buffs == gameState.buffs && thorn == gameState.thorn && thornLoseEOT == gameState.thornLoseEOT && actionCtx == gameState.actionCtx && Arrays.equals(deck, gameState.deck) && Arrays.equals(hand, gameState.hand) && Arrays.equals(discard, gameState.discard) && Objects.equals(enemies, gameState.enemies) && Objects.equals(player, gameState.player) && Objects.equals(previousCard, gameState.previousCard) && Objects.equals(drawOrder, gameState.drawOrder);
    }

    @Override public int hashCode() {
        int result = Objects.hash(actionCtx, energy, energyRefill, enemies, enemiesAlive, player, previousCard, previousCardIdx, drawOrder, buffs, thorn, thornLoseEOT);
        result = 31 * result + Arrays.hashCode(deck);
        result = 31 * result + Arrays.hashCode(hand);
        result = 31 * result + Arrays.hashCode(discard);
        return result;
    }

    public void gotoActionCtx(GameActionCtx ctx, Card card, int card_idx) {
        if (ctx == GameActionCtx.PLAY_CARD) {
            assert actionCtx != GameActionCtx.PLAY_CARD;
            actionCtx = ctx;
            if (previousCard != null) {
                if (!previousCard.exhaustWhenPlayed) {
                    discard[previousCardIdx]++;
                }
                previousCard = null;
            }
        } else if (ctx == GameActionCtx.SELECT_ENEMY) {
            assert actionCtx != GameActionCtx.SELECT_ENEMY && enemiesAlive > 0;
            previousCard = card;
            previousCardIdx = card_idx;
            actionCtx = ctx;
        } else if (ctx == GameActionCtx.SELECT_CARD_HAND) {
            previousCard = card;
            previousCardIdx = card_idx;
            actionCtx = ctx;
        } else if (ctx == GameActionCtx.SELECT_CARD_DISCARD) {
            previousCard = card;
            previousCardIdx = card_idx;
            actionCtx = ctx;
        }
        actionsCache = null;
    }

    public GameState(List<Enemy> enemies, Player player, List<CardCount> cards, List<Relic> relics) {
        // game properties (shared)
        prop = new GameProperties();
        prop.random = new Random();
        prop.preEndTurnTrigger = new ArrayList<>();

        cards = collectAllPossibleCards(cards, enemies);
        cards.sort(Comparator.comparing(a -> a.card().cardName));
        prop.cardDict = new Card[cards.size()];
        for (int i = 0; i < cards.size(); i++) {
            prop.cardDict[i] = cards.get(i).card();
        }

        List<Integer> strikeIdxes = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).card().cardName.equals("Slime")) {
                prop.slimeCardIdx = i;
            } else if (cards.get(i).card().cardName.equals("Wound")) {
                prop.woundCardIdx = i;
            } else if (cards.get(i).card().cardName.equals("Burn")) {
                prop.burnCardIdx = i;
            } else if (cards.get(i).card().cardName.equals("Dazed")) {
                prop.dazedCardIdx = i;
            } else if (cards.get(i).card().cardName.equals("Anger")) {
                prop.angerCardIdx = i;
            } else if (cards.get(i).card().cardName.equals("Anger+")) {
                prop.angerPCardIdx = i;
            } else if (cards.get(i).card().cardName.contains("Strike")) {
                strikeIdxes.add(i);
            }
        }
        prop.strikeCardIdxes = strikeIdxes.stream().mapToInt(Integer::intValue).toArray();
        prop.upgradeIdxes = findUpgradeIdxes(cards);
        prop.discardIdxes = findDiscardToKeepTrackOf(cards, enemies);

        // start of game actions
        prop.actionsByCtx = new GameAction[GameActionCtx.values().length][];
        prop.actionsByCtx[GameActionCtx.START_GAME.ordinal()] = new GameAction[] { new GameAction(GameActionType.START_GAME, 0, 0) };

        // play card actions
        prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()] = new GameAction[cards.size() + 1];
        for (int i = 0; i < cards.size(); i++) {
            prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][i] = new GameAction(GameActionType.PLAY_CARD, i, 0);
        }
        prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cards.size()] = new GameAction(GameActionType.END_TURN, 0, 0);

        // select enemy actions
        if (enemies.size() > 1) {
            prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] = new GameAction[enemies.size()];
            for (int i = 0; i < enemies.size(); i++) {
                prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()][i] = new GameAction(GameActionType.SELECT_ENEMY, 0, i);
            }
        }

        // select hand actions
        if (cards.stream().anyMatch((x) -> x.card().selectFromHand)) {
            var actions = new ArrayList<GameAction>();
            for (int i = 0; i < cards.size(); i++) {
                for (CardCount c : cards) {
                    if (c.card().selectFromHand && c.card().canSelectFromHand(cards.get(i).card())) {
                        actions.add(new GameAction(GameActionType.SELECT_CARD_HAND, i, 0));
                        break;
                    }
                }
            }
            prop.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()] = actions.toArray(new GameAction[0]);
        }

        // select from discard actions
        if (cards.stream().anyMatch((x) -> x.card().selectFromDiscard)) {
            prop.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()] = new GameAction[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()][i] = new GameAction(GameActionType.SELECT_CARD_DISCARD, i, 0);
            }
        }

        for (int i = 0; i < prop.actionsByCtx.length; i++) {
            if (prop.actionsByCtx[i] != null) {
                prop.maxNumOfActions = Math.max(prop.maxNumOfActions, prop.actionsByCtx[i].length);
                prop.totalNumOfActions += prop.actionsByCtx[i].length;
            }
        }

        prop.playerStrengthCanChange = cards.stream().anyMatch((x) -> x.card().changePlayerStrength);
        prop.playerStrengthCanChange |= enemies.stream().anyMatch((x) -> x.canAffectPlayerStrength);
        prop.playerDexterityCanChange = cards.stream().anyMatch((x) -> x.card().changePlayerDexterity);
        prop.playerDexterityCanChange |= enemies.stream().anyMatch((x) -> x.canAffectPlayerDexterity);
        prop.playerCanGetVuln = enemies.stream().anyMatch((x) -> x.canVulnerable);
        prop.playerCanGetWeakened = enemies.stream().anyMatch((x) -> x.canWeaken);
        prop.playerCanGetFrailed = enemies.stream().anyMatch((x) -> x.canFrail);
        prop.enemyCanGetVuln = cards.stream().anyMatch((x) -> x.card().vulnEnemy);
        prop.enemyCanGetWeakened = cards.stream().anyMatch((x) -> x.card().weakEnemy);
        prop.playerThornCanChange = cards.stream().anyMatch((x) -> x.card().cardName.contains("Flame Barrier"));
        prop.possibleBuffs |= cards.stream().anyMatch((x) -> x.card().cardName.contains("Corruption")) ? PlayerBuffs.CORRUPTION : 0;
        prop.needDeckOrderMemory = cards.stream().anyMatch((x) -> x.card().putCardOnTopDeck);

        // game state
        actionCtx = GameActionCtx.START_GAME;
        deck = new int[cards.size()];
        hand = new int[cards.size()];
        discard = new int[cards.size()];
        for (int i = 0; i < cards.size(); i++) {
            deck[i] = cards.get(i).count();
            hand[i] = 0;
            discard[i] = 0;
            deckArrLen += deck[i];
        }
        deckArr = new int[deckArrLen + 60];
        int idx = 0;
        for (int i = 0; i < cards.size(); i++) {
            for (int j = 0; j < cards.get(i).count(); j++) {
                deckArr[idx++] = i;
            }
        }
        energy = 3;
        energyRefill = 3;
        this.enemies = enemies;
        enemiesAlive = (int) enemies.stream().filter((x) -> x.health > 0).count();
        this.player = player;
        drawOrder = new DrawOrder(10);

        // mcts related fields
        policy = null;
        q = new double[prop.maxNumOfActions];
        n = new int[prop.maxNumOfActions];
        ns = new State[prop.maxNumOfActions];
        transpositions_policy_mask = new boolean[prop.maxNumOfActions];
        transpositions = new HashMap<>();

        for (Relic relic : relics) {
            relic.startOfGame(this);
        }
    }

    private int[] findUpgradeIdxes(List<CardCount> cards) {
        if (!cards.stream().anyMatch((x) -> x.card().cardName.contains("Armanent"))) {
            return null;
        }
        int[] r = new int[cards.size() - 1];
        Arrays.fill(r, -1);
        for (int i = 0; i < r.length; i++) {
            var upgrade = CardUpgrade.map.get(cards.get(i).card());
            if (upgrade != null && upgrade.equals(cards.get(i + 1).card())) {
                r[i] = i + 1;
            }
        }
        return r;
    }

    private int[] findDiscardToKeepTrackOf(List<CardCount> cards, List<Enemy> enemies) {
        Set<Integer> l = new HashSet<>();
        for (Enemy enemy : enemies) {
            if (enemy.canDaze) {
                l.add(prop.dazedCardIdx);
            } else if (enemy.canSlime) {
                l.add(prop.slimeCardIdx);
            }
        }
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).card().exhaustWhenPlayed) {
                l.add(i);
            } else if (cards.get(i).card().exhaustNonAttacks) {
                for (int j = 0; j < cards.size(); j++) {
                    if (cards.get(j).card().cardType != Card.ATTACK && !cards.get(j).card().exhaustEndOfTurn &&
                        getCardEnergyCost(j) >= 0) {
                        l.add(j);
                    }
                }
            } else if (cards.get(i).card().selectFromDiscard) {
                for (int j = 0; j < cards.size(); j++) {
                    l.add(j);
                }
            }
        }
        if (prop.upgradeIdxes != null) {
            for (int i = 0; i < prop.upgradeIdxes.length; i++) {
                if (prop.upgradeIdxes[i] >= 0) {
                    l.add(i);
                    l.add(prop.upgradeIdxes[i]);
                }
            }
        }
        return l.stream().sorted().mapToInt(Integer::intValue).toArray();
    }

    private List<CardCount> collectAllPossibleCards(List<CardCount> cards, List<Enemy> enemies) {
        var set = new HashSet<CardCount>();
        set.addAll(cards);
        if (enemies.stream().anyMatch((x) -> x.canSlime)) {
            set.add(new CardCount(new Card.Slime(), 0));
        }
        if (enemies.stream().anyMatch((x) -> x.canDaze)) {
            set.add(new CardCount(new Card.Dazed(), 0));
        }
        do {
            var newSet = new HashSet<CardCount>(set);
            for (CardCount c : set) {
                var possibleCards = c.card().getPossibleGeneratedCards(set.stream().map((x) -> x.card()).toList());
                if (possibleCards != null) {
                    for (Card possibleCard : possibleCards) {
                        CardCount cc = new CardCount(possibleCard, 0);
                        if (!newSet.contains(cc)) {
                            newSet.add(cc);
                        }
                    }
                }
            }
            if (set.size() == newSet.size()) {
                break;
            }
            set = newSet;
        } while (true);
        return new ArrayList<>(set);
    }

    public GameState(GameState other) {
        prop = other.prop;

        actionCtx = other.actionCtx;
        deck = Arrays.copyOf(other.deck, other.deck.length);
        hand = Arrays.copyOf(other.hand, other.hand.length);
        discard = Arrays.copyOf(other.discard, other.discard.length);
        deckArr = Arrays.copyOf(other.deckArr, other.deckArr.length);
        deckArrLen = other.deckArrLen;
        energy = other.energy;
        turn_num = other.turn_num;
        energyRefill = other.energyRefill;
        player = new Player(other.player);
        enemies = new ArrayList<>();
        for (int i = 0; i < other.enemies.size(); i++) {
            enemies.add(other.enemies.get(i).copy());
        }
        enemiesAlive = other.enemiesAlive;
        previousCard = other.previousCard;
        previousCardIdx = other.previousCardIdx;
        drawOrder = new DrawOrder(other.drawOrder);

        buffs = other.buffs;
        thorn = other.thorn;
        thornLoseEOT = other.thornLoseEOT;

        policy = null;
        q = new double[prop.maxNumOfActions];
        n = new int[prop.maxNumOfActions];
        ns = new State[prop.maxNumOfActions];
        transpositions_policy_mask = new boolean[prop.maxNumOfActions];
        transpositions = other.transpositions;
    }

    void draw(int count) {
        if (deckArrLen != count) { // todo: add discard count too
            isStochastic = true;
        }
        int cardsInHand = 0;
        for (int i = 0; i < hand.length; i++) {
            cardsInHand += hand[i];
        }
        count = Math.min(GameState.HAND_LIMIT - cardsInHand, count);
        for (int c = 0; c < count; c++) {
            if (deckArrLen == 0) {
                reshuffle();
            }
            if (deckArrLen == 0) {
                return;
            }
            int i;
            if (drawOrder != null && drawOrder.size() > 0) {
                i = drawOrder.drawTop();
                assert deck[i] > 0;
                drawCardByIdx(i);
            } else {
                i = prop.random.nextInt(this.deckArrLen);
                deck[deckArr[i]] -= 1;
                hand[deckArr[i]] += 1;
                deckArr[i] = deckArr[deckArrLen - 1];
                deckArrLen -= 1;
            }
        }
    }

    private int getCardEnergyCost(int cardIdx) {
        if ((buffs & PlayerBuffs.CORRUPTION) != 0) {
            if (prop.cardDict[cardIdx].cardType == Card.SKILL) {
                return 0;
            }
        }
        return prop.cardDict[cardIdx].energyCost(this);
    }

    private void playCard(int i) {
        assert getCardEnergyCost(i) >= 0;
        assert energy >= getCardEnergyCost(i);
        assert hand[i] > 0;
        hand[i] -= 1;
        energy -= getCardEnergyCost(i);
        if (prop.cardDict[i].selectEnemy) {
            if (enemiesAlive > 1) {
                gotoActionCtx(GameActionCtx.SELECT_ENEMY, prop.cardDict[i], i);
            } else {
                for (int j = 0; j < enemies.size(); j++) {
                    if (enemies.get(j).health > 0) {
                        prop.cardDict[i].play(this, j);
                    }
                }
                if (prop.cardDict[i].secondActionCtx == GameActionCtx.SELECT_CARD_DISCARD) {
                    int found = -1;
                    for (int idx = 0; idx < discard.length; idx++) {
                        if (discard[idx] > 0) {
                            if (found == -1) {
                                found = idx;
                            } else {
                                gotoActionCtx(GameActionCtx.SELECT_CARD_DISCARD, prop.cardDict[i], i);
                                found = -1;
                                break;
                            }
                        }
                    }
                    if (found != -1) {
                        prop.cardDict[i].playPart2(this, found); // todo better
                    }
                }
            }
        } else if (prop.cardDict[i].selectFromHand) {
            gotoActionCtx(GameActionCtx.SELECT_CARD_HAND, prop.cardDict[i], i);
        } else {
            prop.cardDict[i].play(this, -1);
            for (Enemy enemy : enemies) {
                enemy.react(prop.cardDict[i]);
            }
        }
        if (prop.cardDict[i].exhaustWhenPlayed) {
            exhaustedCardHandle(i);
        } else if ((buffs & PlayerBuffs.CORRUPTION) != 0 && prop.cardDict[i].cardType == Card.SKILL) {
            exhaustedCardHandle(i);
        } else if (prop.cardDict[i].cardType != Card.POWER && actionCtx == GameActionCtx.PLAY_CARD) {
            discard[i] += 1;
        }
    }

    void startTurn() {
        for (Enemy enemy : enemies) {
            if (enemy.health > 0) {
                enemy.nextMove(prop.random);
            }
            enemy.startTurn();
        }
        energy = energyRefill;
        draw(5);
    }

    private void endTurn() {
        for (GameTrigger gameTrigger : prop.preEndTurnTrigger) {
            gameTrigger.act(this);
        }
        for (Enemy enemy : enemies) {
            if (enemy.health > 0) {
                enemy.doMove(this);
            }
        }
        for (int i = 0; i < hand.length; i++) {
            if (!prop.cardDict[i].exhaustEndOfTurn) {
                if (hand[i] > 0) {
                    for (int j = 0; j < hand[i]; j++) {
                        prop.cardDict[i].onDiscard(this);
                    }
                    discard[i] += hand[i];
                }
            }
            hand[i] = 0;
        }
        for (Enemy enemy : enemies) {
            if (enemy.health > 0) {
                enemy.endTurn();
            }
        }
        thorn -= thornLoseEOT;
        player.endTurn();
    }

    void doAction(int actionIdx) {
        GameAction action = prop.actionsByCtx[actionCtx.ordinal()][actionIdx];
        if (action.type() == GameActionType.START_GAME) {
            for (Enemy enemy : enemies) {
                enemy.startOfGameSetup(prop.random);
            }
            startTurn();
            actionCtx = GameActionCtx.PLAY_CARD;
            turn_num++;
        } else if (action.type() == GameActionType.END_TURN) {
            endTurn();
            startTurn();
            turn_num++;
        } else if (action.type() == GameActionType.PLAY_CARD) {
            playCard(action.cardIdx());
        } else if (action.type() == GameActionType.SELECT_ENEMY) {
            previousCard.play(this, action.enemyIdx());
            for (Enemy enemy : enemies) {
                enemy.react(prop.cardDict[previousCardIdx]);
            }
            if (previousCard.secondActionCtx == GameActionCtx.SELECT_CARD_DISCARD) {
                int found = -2;
                for (int i = 0; i < discard.length; i++) {
                    if (discard[i] > 0) {
                        if (found == -2) {
                            found = i;
                        } else {
                            gotoActionCtx(GameActionCtx.SELECT_CARD_DISCARD, previousCard, previousCardIdx);
                            found = -1;
                            break;
                        }
                    }
                }
                if (found >= 0) {
                    previousCard.playPart2(this, found); // todo better
                    gotoActionCtx(GameActionCtx.PLAY_CARD, null, -1);
                } else if (found == -2) {
                    gotoActionCtx(GameActionCtx.PLAY_CARD, null, -1);
                }
            } else {
                gotoActionCtx(GameActionCtx.PLAY_CARD, null, -1);
            }
        } else if (action.type() == GameActionType.SELECT_CARD_HAND) {
            previousCard.play(this, action.cardIdx());
            gotoActionCtx(GameActionCtx.PLAY_CARD, null, -1);
        } else if (action.type() == GameActionType.SELECT_CARD_DISCARD) {
            previousCard.playPart2(this, action.cardIdx()); // todo better
            gotoActionCtx(GameActionCtx.PLAY_CARD, null, -1);
        }
        actionsCache = null;
        policy = null;
        v_win = 0;
        v_health = 0;
        if (isStochastic) {
            transpositions = new HashMap<>();
        }
    }

    boolean isActionLegal(int action) {
        if (actionsCache != null && action < actionsCache.length) {
            return actionsCache[action];
        }
        if (actionCtx == GameActionCtx.START_GAME) {
            return action == 0;
        } else if (actionCtx == GameActionCtx.PLAY_CARD) {
            boolean[] actions = new boolean[prop.maxNumOfActions];
            GameAction[] a = prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()];
            if (action >= a.length) {
                return false;
            }
            for (int i = 0; i < a.length; i++) {
                if (a[i].type() == GameActionType.END_TURN) {
                    actions[i] = true;
                } else if (hand[a[i].cardIdx()] > 0) {
                    int cost = getCardEnergyCost(a[i].cardIdx());
                    if (cost >= 0 && cost <= energy) {
                        actions[i] = true;
                    }
                }
            }
            actionsCache = actions;
            return actions[action];
        } else if (actionCtx == GameActionCtx.SELECT_ENEMY) {
            GameAction[] a = prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()];
            if (action >= a.length) {
                return false;
            }
            return enemies.get(a[action].enemyIdx()).health > 0;
        } else if (actionCtx == GameActionCtx.SELECT_CARD_HAND) {
            GameAction[] a = prop.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()];
            if (action >= a.length) {
                return false;
            }
            return hand[a[action].cardIdx()] > 0;
        } else if (actionCtx == GameActionCtx.SELECT_CARD_DISCARD) {
            GameAction[] a = prop.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()];
            if (action >= a.length) {
                return false;
            }
            return discard[a[action].cardIdx()] > 0;
        }
        return false;
    }

    double get_v() {
        if (player.health <= 0 || turn_num > 30) {
            return 0;
        } else {
            if (enemies.stream().allMatch((x) -> x.health <= 0)) {
               return 0.5 + 0.5 * ((double) player.health) / player.maxHealth;
//                  return ((double) player.health) / player.maxHealth;
            }
        }
       return 0.5 * v_win + 0.5 * v_health;
//          return v_health;
    }

    int isTerminal() {
        if (player.health <= 0 || turn_num > 30) {
            return -1;
        } else {
            return enemies.stream().allMatch((x) -> x.health <= 0) ? 1 : 0;
        }
    }

    private String format_float(double f) {
        if (f == 0) {
            return "0";
        } else if (f < 0.001) {
            return String.format("%6.3e", f);
        } else {
            return String.format("%6.3f", f);
        }
    }

    @Override public String toString() {
        StringBuilder str = new StringBuilder("deck=" + Arrays.toString(deck) +
                ", hand=" + Arrays.toString(hand) +
                ", discard=" + Arrays.toString(discard) +
                ", energy=" + energy +
                ", ctx=" + actionCtx +
                ", " + player);
        str.append(", [");
        int ii = 0;
        for (Enemy enemy : enemies) {
            if (enemy.health > 0) {
                str.append(enemy.toString(this));
                if (ii++ < enemiesAlive) {
                    str.append(", ");
                }
            }
        }
        str.append("]");
        str.append(", v=(").append(format_float(v_win)).append(", ").append(format_float(v_health)).append("/").append(format_float(v_health * player.maxHealth)).append(")");
        if (policy != null) {
            str.append(", p=[");
            for (int i = 0; i < policy.length; i++) {
                str.append(format_float(policy[i]));
                if (i != policy.length - 1) {
                    str.append(", ");
                }
            }
            str.append(']');
        }
        str.append(", q=[");
        for (int i = 0; i < q.length; i++) {
            str.append(format_float(n[i] == 0 ? 0 : q[i] / n[i]));
            if (i != q.length - 1) {
                str.append(", ");
            }
        }
        str.append(']');
        str.append(", n=").append(Arrays.toString(n)).append('}');
        return str.toString();
    }

    public String toStringReadable() {
        boolean first;
        StringBuilder str = new StringBuilder("{");
        str.append("deck=[");
        first = true;
        for (int i = 0; i < deck.length; i++) {
            if (deck[i] > 0) {
                if (!first) {
                    str.append(", ");
                }
                first = false;
                str.append(deck[i]).append(" ").append(prop.cardDict[i].cardName);
            }
        }
        str.append("]");
        str.append(", hand=[");
        first = true;
        for (int i = 0; i < hand.length; i++) {
            if (hand[i] > 0) {
                if (!first) {
                    str.append(", ");
                }
                first = false;
                str.append(hand[i]).append(" ").append(prop.cardDict[i].cardName);
            }
        }
        str.append("]");
        str.append(", discard=[");
        first = true;
        for (int i = 0; i < discard.length; i++) {
            if (discard[i] > 0) {
                if (!first) {
                    str.append(", ");
                }
                first = false;
                str.append(discard[i]).append(" ").append(prop.cardDict[i].cardName);
            }
        }
        str.append("]");
        str.append(", energy=").append(energy).append(", ctx=").append(actionCtx).append(", ").append(player);
        str.append(", [");
        int ii = 0;
        for (Enemy enemy : enemies) {
            if (enemy.health > 0) {
                str.append(enemy.toString(this));
                if (ii++ < enemiesAlive) {
                    str.append(", ");
                }
            }
        }
        str.append("]");
        str.append(", v=(").append(format_float(v_win)).append(", ").append(format_float(v_health)).append("/").append(format_float(v_health * player.maxHealth)).append(")");
        str.append(", p/q/n=[");
        first = true;
        for (int i = 0; i < q.length; i++) {
            var p_str = policy != null ? format_float(policy[i]) : "0";
            var q_str = format_float(n[i] == 0 ? 0 : q[i] / n[i]);
            if (isActionLegal(i)) {
                if (!first) {
                    str.append(", ");
                }
                first = false;
                str.append(p_str).append('/').append(q_str).append('/').append(n[i]);
                str.append(" (").append(getActionString(i)).append(")");
            }
        }
        str.append(']');
        str.append('}');
        return str.toString();
    }

    public void doEval(Model model) {
        Output output = model.eval(this);
        this.policy = output.policy();
        this.v_health = Math.min(output.v_health(), this.player.health / (float) this.player.maxHealth);
        this.v_win = output.v_win();
    }

    public float[] getInput() {
        int input_len = 0;
        input_len += deck.length;
        input_len += hand.length;
        input_len += prop.discardIdxes.length;
        if (MAX_AGENT_DECK_ORDER_MEMORY > 0 && prop.needDeckOrderMemory) {
            input_len += hand.length * MAX_AGENT_DECK_ORDER_MEMORY;
        }
        int non_play_card_ctx_count = 0;
        for (int i = 2; i < prop.actionsByCtx.length; i++) {
            if (prop.actionsByCtx[i] != null) {
                non_play_card_ctx_count++;
            }
        }
        if (non_play_card_ctx_count > 0) {
            input_len += non_play_card_ctx_count;
        }
        input_len += 1; // energy
        input_len += 1; // player health
        input_len += 1; // player block
        if (prop.playerStrengthCanChange) {
            input_len += 1; // player strength
        }
        if (prop.playerDexterityCanChange) {
            input_len += 1; // player dexterity
        }
        if (prop.playerCanGetVuln) {
            input_len += 1; // player vulnerable
        }
        if (prop.playerCanGetWeakened) {
            input_len += 1; // player weak
        }
        if (prop.playerCanGetFrailed) {
            input_len += 1; // player weak
        }
        if (prop.playerThornCanChange) {
            input_len += 1; // player thorn
        }
        if ((prop.possibleBuffs & PlayerBuffs.CORRUPTION) != 0) {
            input_len += 1; // player thorn
        }
        // cards currently selecting enemies
        if (prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null ||
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()] != null ||
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()] != null) {
            for (GameAction action : prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (prop.cardDict[action.cardIdx()].selectEnemy ||
                        prop.cardDict[action.cardIdx()].selectFromHand ||
                        prop.cardDict[action.cardIdx()].selectFromDiscard) {
                    input_len += 1;
                }
            }
        }
        for (Enemy enemy : enemies) {
            input_len += 1; // enemy health
            if (prop.enemyCanGetVuln) {
                input_len += 1; // enemy vulnerable
            }
            if (prop.enemyCanGetWeakened) {
                input_len += 1; // enemy weak
            }
            if (enemy.canGainBlock) {
                input_len += 1; // enemy block
            }
            if (enemy.canGainStrength) {
                input_len += 1; // enemy strength
            }
            if (enemy.hasArtifact) {
                input_len += 1; // enemy artifact
            }
            input_len += enemy.numOfMoves; // enemy moves
            if (enemy.moveHistory != null) {
                for (int move : enemy.moveHistory) {
                    input_len += enemy.numOfMoves;
                }
            }
            input_len += 1; // enemy move
            if (enemy.moveHistory != null) {
                input_len += enemy.moveHistory.length;
            }
            if (enemy instanceof Enemy.RedLouse || enemy instanceof Enemy.GreenLouse) {
                input_len += 1;
            }
        }

        int idx = 0;
        var x = new float[input_len];
        for (int j : deck) {
            x[idx++] = j / (float) 10.0;
        }
        for (int j : hand) {
            x[idx++] = j / (float) 10.0;
        }
        for (int i = 0; i < prop.discardIdxes.length; i++) {
            x[idx++] = discard[prop.discardIdxes[i]] / (float) 10.0;
        }
        if (MAX_AGENT_DECK_ORDER_MEMORY > 0 && prop.needDeckOrderMemory) {
            for (int i = 0; i < Math.min(MAX_AGENT_DECK_ORDER_MEMORY, drawOrder.size()); i++) {
                for (int j = 0; j < hand.length; j++) {
                    if (j == drawOrder.ithCardFromTop(i)) {
                        x[idx++] = 1f;
                    } else {
                        x[idx++] = 0f;
                    }
                }
            }
            for (int i = 0; i < MAX_AGENT_DECK_ORDER_MEMORY - drawOrder.size(); i++) {
                for (int j = 0; j < hand.length; j++) {
                    x[idx++] = 0f;
                }
            }
        }
        if (non_play_card_ctx_count > 0) {
            for (int i = 2; i < prop.actionsByCtx.length; i++) {
                if (prop.actionsByCtx[i] != null) {
                    x[idx++] = actionCtx.ordinal() == i ? 0.5f : -0.5f;
                }
            }
        }
        x[idx++] = energy / (float) 10;
        x[idx++] = player.health / (float) player.maxHealth;
        x[idx++] = player.block / (float) 40.0;
        if (prop.playerStrengthCanChange) {
            x[idx++] = player.strength / (float) 10.0;
        }
        if (prop.playerDexterityCanChange) {
            x[idx++] = player.dexterity / (float) 10.0;
        }
        if (prop.playerCanGetVuln) {
            x[idx++] = player.vulnerable / (float) 10.0;
        }
        if (prop.playerCanGetWeakened) {
            x[idx++] = player.weak / (float) 10.0;
        }
        if (prop.playerCanGetFrailed) {
            x[idx++] = player.frail / (float) 10.0;
        }
        if (prop.playerThornCanChange) {
            x[idx++] = thorn / 10.0f;
        }
        if ((prop.possibleBuffs & PlayerBuffs.CORRUPTION) != 0) {
            x[idx++] = (buffs & PlayerBuffs.CORRUPTION) != 0 ? 0.5f : -0.5f;
        }
        if (prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null ||
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()] != null ||
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()] != null) {
            for (GameAction action : prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (prop.cardDict[action.cardIdx()].selectEnemy ||
                        prop.cardDict[action.cardIdx()].selectFromHand ||
                        prop.cardDict[action.cardIdx()].selectFromDiscard) {
                    x[idx++] = previousCard == prop.cardDict[action.cardIdx()] ? 0.6f : -0.6f;
                }
            }
        }
        for (Enemy enemy : enemies) {
            if (enemy.health > 0) {
                x[idx++] = enemy.health / (float) enemy.maxHealth;
                if (prop.enemyCanGetVuln) {
                    x[idx++] = enemy.vulnerable / (float) 10.0;
                }
                if (prop.enemyCanGetWeakened) {
                    x[idx++] = enemy.weak / (float) 10.0;
                }
                if (enemy.canGainStrength) {
                    x[idx++] = enemy.strength / (float) 20.0;
                }
                if (enemy.canGainBlock) {
                    x[idx++] = enemy.block / (float) 20.0;
                }
                if (enemy.hasArtifact) {
                    x[idx++] = enemy.artifact / 3.0f;
                }
                for (int i = 0; i < enemy.numOfMoves; i++) {
                    if (enemy.move == i) {
                        x[idx++] = 0.5f;
                    } else {
                        x[idx++] = -0.5f;
                    }
                }
                if (enemy.moveHistory != null) {
                    for (int move : enemy.moveHistory) {
                        for (int i = 0; i < enemy.numOfMoves; i++) {
                            if (move == i) {
                                x[idx++] = 0.5f;
                            } else {
                                x[idx++] = -0.5f;
                            }
                        }
                    }
                }
                if (enemy instanceof Enemy.RedLouse louse) {
                    x[idx++] = (louse.curlUpAmount - 10) / 2.0f;
                } else if (enemy instanceof Enemy.GreenLouse louse) {
                    x[idx++] = (louse.curlUpAmount - 10) / 2.0f;
                }
            } else {
                x[idx++] = enemy.health / (float) enemy.maxHealth;
                if (prop.enemyCanGetVuln) {
                    x[idx++] = -0.1f;
                }
                if (prop.enemyCanGetWeakened) {
                    x[idx++] = -0.1f;
                }
                if (enemy.canGainStrength) {
                    x[idx++] = -0.1f;
                }
                if (enemy.canGainBlock) {
                    x[idx++] = -0.1f;
                }
                if (enemy.hasArtifact) {
                    x[idx++] = -0.1f;
                }
                for (int i = 0; i < enemy.numOfMoves; i++) {
                    x[idx++] = -0.1f;
                }
                if (enemy.moveHistory != null) {
                    for (int move : enemy.moveHistory) {
                        for (int i = 0; i < enemy.numOfMoves; i++) {
                            x[idx++] = -0.1f;
                        }
                    }
                }
                if (enemy instanceof Enemy.RedLouse || enemy instanceof Enemy.GreenLouse) {
                    x[idx++] = -0.1f;
                }
            }
        }
        return x;
    }

    void exhaustCardFromHand(int cardIdx) {
        assert hand[cardIdx] > 0;
        hand[cardIdx] -= 1;
        exhaustedCardHandle(cardIdx);
    }

    private void exhaustedCardHandle(int cardIdx) {
    }
    
    public DrawOrder getDrawOrder() {
        return drawOrder;
    }

    public boolean drawCardByIdx(int card_idx) {
        if (deck[card_idx] > 0) {
            deck[card_idx] -= 1;
            hand[card_idx] += 1;
            for (int i = 0; i < deckArrLen; i++) {
                if (deckArr[i] == card_idx) {
                    deckArr[i] = deckArr[deckArrLen - 1];
                    deckArrLen -= 1;
                    return true;
                }
            }
        }
        return false;
    }

    public void undrawCardByIdx(int card_idx) {
        if (hand[card_idx] > 0) {
            deck[card_idx] += 1;
            hand[card_idx] -= 1;
            deckArr[deckArrLen] = card_idx;
            deckArrLen += 1;
        }
    }

    public void undrawAll() {
        for (int i = 0; i < hand.length; i++) {
            while (hand[i] != 0) {
                undrawCardByIdx(i);
            }
        }
    }

    public void discardHand() {
        for (int i = 0; i < hand.length; i++) {
            discard[i] += hand[i];
            hand[i] = 0;
        }
    }

    public void reshuffle() {
        for (int i = 0; i < discard.length; i++) {
            //            System.out.println(Arrays.toString(deckArr));
            //            System.out.println(Arrays.toString(deck));
            //            System.out.println(deckLen);
            for (int j = 0; j < discard[i]; j++) {
                deckArr[deckArrLen++] = i;
            }
            deck[i] = discard[i];
            discard[i] = 0;
        }
    }

    public String getActionString(int i) {
        GameAction action = getAction(i);
        if (action.type() == GameActionType.START_GAME) {
            return "Start Game";
        } else if (action.type() == GameActionType.PLAY_CARD) {
            return prop.cardDict[action.cardIdx()].cardName;
        } else if (action.type() == GameActionType.END_TURN) {
            return "End Turn";
        } else if (action.type() == GameActionType.SELECT_ENEMY) {
            return "Attack " + enemies.get(action.enemyIdx()).getName();
        } else if (action.type() == GameActionType.SELECT_CARD_HAND) {
            return "Select " + prop.cardDict[action.cardIdx()].cardName + " From Hand";
        } else if (action.type() == GameActionType.SELECT_CARD_DISCARD) {
            return "Select " + prop.cardDict[action.cardIdx()].cardName + " From Discard";
        }
        return "Unknown";
    }

    public GameAction getAction(int i) {
        return prop.actionsByCtx[actionCtx.ordinal()][i];
    }

    public void addPreEndOfTurnTrigger(GameTrigger gameTrigger) {
        prop.preEndTurnTrigger.add(gameTrigger);
    }

    public void clearNextStates() { // oom during training due to holding too many states
        for (int i = 0; i < ns.length; i++) {
            ns[i] = null;
        }
    }

    public void gainEnergy(int n) {
        energy += n;
    }

    public void addCardToHand(int idx) {
        int cardsInHand = 0;
        for (int i = 0; i < hand.length; i++) {
            cardsInHand += hand[i];
        }
        if (cardsInHand >= GameState.HAND_LIMIT) {
            discard[idx]++;
        } else {
            hand[idx]++;
        }
    }

    public void addCardToDiscard(int idx) {
        discard[idx]++;
    }

    public void addCardToDeck(int idx) {
        deck[idx]++;
        deckArr[deckArrLen++] = idx;
    }

    public void putCardOnTopOfDeck(int idx) {
        deck[idx]++;
        deckArr[deckArrLen++] = idx;
        drawOrder.pushOnTop(idx);
    }

    public void enemyDoDamageToPlayer(Enemy enemy, int d) {
        player.damage(d + enemy.strength);
        if (thorn > 0) {
            enemy.nonAttackDamage(thorn, false, this);
        }
    }

    public void enemyDoNonAttackDamageToPlayer(Enemy enemy, int d, boolean blockable, boolean addStrength) {
        player.nonAttackDamage(d + (addStrength ? enemy.strength : 0), blockable);
    }
}

interface State {
}

class InputHash {
    float[] input;
    int hashCode;

    public InputHash(float[] input) {
        this.input = input;
        hashCode = Arrays.hashCode(input);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        InputHash inputHash = (InputHash) o;
        return Arrays.equals(input, inputHash.input);
    }

    @Override public int hashCode() {
        return hashCode;
    }
}

class ChanceState implements State {
    static class Node {
        GameState state;
        long n = 1;

        public Node(GameState state) {
            this.state = state;
        }
    }

    Hashtable<GameState, Node> cache;
    long total_n;
    double v_health = -1;

    public ChanceState() {
        cache = new Hashtable<>();
    }

    public ChanceState(GameState initState) {
        cache = new Hashtable<>();
        cache.put(initState, new Node(initState));
    }

    GameState getNextState(GameState parentState, int action) {
        var state = new GameState(parentState);
        state.doAction(action);
        total_n += 1;
        var node = cache.get(state);
        if (node != null) {
            node.n += 1;
            return node.state;
        }
        cache.put(state, new Node(state));
        return state;
    }

    @Override public String toString() {
        String s = "";
        for (Node node : cache.values()) {
            s += "- (" + node.n + "/" + total_n + ") " + node.state + "\n";
        }
        return s;
    }
}

record CardCount(Card card, int count) {
    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CardCount cardCount = (CardCount) o;
        return card.cardName.equals(cardCount.card.cardName);
    }

    @Override public int hashCode() {
        return card.cardName.hashCode();
    }
}
