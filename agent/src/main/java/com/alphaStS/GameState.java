package com.alphaStS;

import com.alphaStS.player.Player;
import com.alphaStS.player.PlayerReadOnly;
import com.alphaStS.utils.DrawOrder;
import com.alphaStS.utils.DrawOrderReadOnly;

import java.util.*;

import static com.alphaStS.GameStateUtils.formatFloat;

abstract class GameEventHandler implements Comparable<GameEventHandler> {
    private int priority;

    protected GameEventHandler(int priority) {
        this.priority = priority;
    }

    protected GameEventHandler() {}

    abstract void handle(GameState state);

    @Override public int compareTo(GameEventHandler other) {
        return Integer.compare(other.priority, priority);
    }
}

abstract class GameEventCardHandler {
    abstract void handle(GameState state, Card card);
}

abstract class OnDamageHandler {
    abstract void handle(GameState state, Object source, boolean isAttack, int damageDealt);
}

interface GameEnvironmentAction {
    void doAction(GameState state);
}

enum GameActionCtx {
    START_GAME,
    PLAY_CARD,
    SELECT_ENEMY,
    SELECT_CARD_DISCARD,
    SELECT_CARD_HAND,
    SELECT_CARD_EXHAUST,
    SELECT_POTION,
    BEGIN_TURN,
}

enum GameActionType {
    START_GAME,
    PLAY_CARD,
    SELECT_ENEMY,
    SELECT_CARD_DISCARD,
    SELECT_CARD_HAND,
    SELECT_CARD_EXHAUST,
    END_TURN,
    SELECT_POTION,
    BEGIN_TURN,
}

record GameAction(GameActionType type, int cardIdx, int enemyIdx) {
}

public class GameState implements State {
    private static final int HAND_LIMIT = 10;
    private static final int MAX_AGENT_DECK_ORDER_MEMORY = 1;

    boolean isStochastic;
    public GameProperties prop;
    private boolean[] actionsCache;
    GameActionCtx actionCtx;

    int[] deck;
    int[] hand;
    int[] discard;
    private boolean exhaustCloned;
    private int[] exhaust;
    int[] deckArr;
    int deckArrLen;
    List<Enemy> enemies;
    int enemiesAlive;
    private boolean playerCloned;
    private Player player;
    private boolean drawOrderCloned;
    private DrawOrder drawOrder;
    private boolean counterCloned;
    private int[] counter;

    private Deque<GameEnvironmentAction> gameActionDeque;
    int energy;
    int energyRefill;
    Card previousCard;
    int previousCardIdx;
    short turnNum;
    int playerTurnStartHealth;

    // various other buffs/debuffs
    public long buffs;
    short thorn;
    int lastEnemySelected;

    double v_win; // if terminal, 1.0 or -1.0, else from NN
    double v_health; // if terminal, player_health/player_max_health, else from NN
    double[] q_win; // total v_win value propagated from each child
    double[] q_health; // total v_health value propagated from each child
    double[] q_comb; // total q value propagated from each child
    int[] n; // visit count for each child
    State[] ns; // the state object for each child (either GameState or ChanceState)
    int total_n; // sum of n array
    double total_q_win; // sum of q_win array
    double total_q_health; // sum of q_health _array
    double total_q_comb; // sum of q_win array
    float[] policy; // policy from NN
    float[] policyMod; // used in training (with e.g. Dirichlet noise applied or futile pruning applied)
    Map<GameState, State> transpositions; // detect transposition within a "deterministic turn" (i.e. no stochastic transition occurred like drawing)
    boolean[] transpositionsPolicyMask; // true if the associated action is a transposition
    int terminal_action; // detected a win from child, no need to waste more time search

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GameState gameState = (GameState) o;
        return energy == gameState.energy && energyRefill == gameState.energyRefill && enemiesAlive == gameState.enemiesAlive && previousCardIdx == gameState.previousCardIdx && buffs == gameState.buffs && lastEnemySelected == gameState.lastEnemySelected && Arrays.equals(counter, gameState.counter) && thorn == gameState.thorn && actionCtx == gameState.actionCtx && Arrays.equals(deck, gameState.deck) && Arrays.equals(hand, gameState.hand) && Arrays.equals(discard, gameState.discard) && Arrays.equals(exhaust, gameState.exhaust) && Objects.equals(enemies, gameState.enemies) && Objects.equals(player, gameState.player) && Objects.equals(previousCard, gameState.previousCard) && Objects.equals(drawOrder, gameState.drawOrder) && Objects.equals(gameActionDeque, gameState.gameActionDeque);
    }

    @Override public int hashCode() {
        // actionCtx, energy, energyRefill, hand, enemies health, previousCardIdx, drawOrder, buffs should cover most
        int result = Objects.hash(actionCtx, energy, energyRefill, previousCardIdx, drawOrder, buffs);
        for (Enemy enemy : enemies) {
            result = 31 * result + enemy.health;
        }
        result = 31 * result + Arrays.hashCode(hand);
        return result;
    }

    public GameState(List<Enemy> enemies, Player player, List<CardCount> cards, List<Relic> relics) {
        // game properties (shared)
        prop = new GameProperties();

        cards = collectAllPossibleCards(cards, enemies, relics);
        cards.sort(Comparator.comparing(a -> a.card().cardName));
        prop.cardDict = new Card[cards.size()];
        for (int i = 0; i < cards.size(); i++) {
            prop.cardDict[i] = cards.get(i).card();
        }

        List<Integer> strikeIdxes = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).card().cardName.equals("Burn")) {
                prop.burnCardIdx = i;
            } else if (cards.get(i).card().cardName.equals("Dazed")) {
                prop.dazedCardIdx = i;
            } if (cards.get(i).card().cardName.equals("Slime")) {
                prop.slimeCardIdx = i;
            } else if (cards.get(i).card().cardName.equals("Wound")) {
                prop.woundCardIdx = i;
            } else if (cards.get(i).card().cardName.equals("Anger")) {
                prop.angerCardIdx = i;
            } else if (cards.get(i).card().cardName.equals("Anger+")) {
                prop.angerPCardIdx = i;
            } else if (cards.get(i).card().cardName.contains("Strike")) {
                strikeIdxes.add(i);
            }
        }
        prop.strikeCardIdxes = strikeIdxes.stream().mapToInt(Integer::intValue).toArray();
        prop.upgradeIdxes = findUpgradeIdxes(cards, relics);
        prop.discardIdxes = findDiscardToKeepTrackOf(cards, enemies);

        // start of game actions
        prop.actionsByCtx = new GameAction[GameActionCtx.values().length][];
        prop.actionsByCtx[GameActionCtx.START_GAME.ordinal()] = new GameAction[] { new GameAction(GameActionType.START_GAME, 0, 0) };
        prop.actionsByCtx[GameActionCtx.BEGIN_TURN.ordinal()] = new GameAction[] { new GameAction(GameActionType.BEGIN_TURN, 0, 0) };

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
            prop.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()] = new GameAction[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()][i] = new GameAction(GameActionType.SELECT_CARD_HAND, i, 0);
            }
        }

        // select from discard actions
        if (cards.stream().anyMatch((x) -> x.card().selectFromDiscard)) {
            prop.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()] = new GameAction[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()][i] = new GameAction(GameActionType.SELECT_CARD_DISCARD, i, 0);
            }
        }

        // select from exhaust actions
        if (cards.stream().anyMatch((x) -> x.card().selectFromExhaust)) {
            prop.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()] = new GameAction[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()][i] = new GameAction(GameActionType.SELECT_CARD_EXHAUST, i, 0);
            }
        }

        for (int i = 0; i < prop.actionsByCtx.length; i++) {
            if (prop.actionsByCtx[i] != null) {
                prop.maxNumOfActions = Math.max(prop.maxNumOfActions, prop.actionsByCtx[i].length);
                if (i == GameActionCtx.PLAY_CARD.ordinal() || i == GameActionCtx.SELECT_ENEMY.ordinal()) {
                    prop.totalNumOfActions += prop.actionsByCtx[i].length;
                }
            }
        }

        // game state
        actionCtx = GameActionCtx.START_GAME;
        deck = new int[cards.size()];
        hand = new int[cards.size()];
        discard = new int[cards.size()];
        exhaust = new int[cards.size()];
        for (int i = 0; i < cards.size(); i++) {
            deck[i] = cards.get(i).count();
            deckArrLen += deck[i];
        }
        deckArr = new int[deckArrLen + 60];
        int idx = 0;
        for (int i = 0; i < cards.size(); i++) {
            for (int j = 0; j < cards.get(i).count(); j++) {
                deckArr[idx++] = i;
            }
        }
        energyRefill = 3;
        this.enemies = enemies;
        enemiesAlive = (int) enemies.stream().filter((x) -> x.health > 0).count();
        this.player = player;
        drawOrder = new DrawOrder(10);
        for (int i = 0; i < deck.length; i++) { // todo: edge case more innate than first turn draw
            if (deck[i] > 0 && prop.cardDict[deck[i]].innate) {
                drawOrder.pushOnTop(i);
            }
        }

        for (Relic relic : relics) {
            relic.startOfGameSetup(this);
        }
        for (Card card : prop.cardDict) {
            card.startOfGameSetup(this);
        }
        prop.compileCounterInfo();
        if (prop.counterNames.length > 0) {
            counter = new int[prop.counterNames.length];
        }
        Collections.sort(prop.startOfTurnHandlers);
        Collections.sort(prop.preEndTurnHandlers);
        Collections.sort(prop.onBlockHandlers);
        Collections.sort(prop.onExhaustHandlers);
        Collections.sort(prop.onBlockHandlers);

        prop.playerStrengthCanChange = cards.stream().anyMatch((x) -> x.card().changePlayerStrength);
        prop.playerStrengthCanChange |= enemies.stream().anyMatch((x) -> x.changePlayerStrength);
        prop.playerStrengthCanChange |= relics.stream().anyMatch((x) -> x.changePlayerStrength);
        prop.playerDexterityCanChange = cards.stream().anyMatch((x) -> x.card().changePlayerDexterity);
        prop.playerDexterityCanChange |= enemies.stream().anyMatch((x) -> x.changePlayerDexterity);
        prop.playerDexterityCanChange |= relics.stream().anyMatch((x) -> x.changePlayerDexterity);
        prop.playerStrengthEotCanChange = cards.stream().anyMatch((x) -> x.card().changePlayerStrengthEot);
        prop.playerCanGetVuln = enemies.stream().anyMatch((x) -> x.canVulnerable);
        prop.playerCanGetWeakened = enemies.stream().anyMatch((x) -> x.canWeaken);
        prop.playerCanGetFrailed = enemies.stream().anyMatch((x) -> x.canFrail);
        prop.playerCanHeal = cards.stream().anyMatch((x) -> x.card().healPlayer) || relics.stream().anyMatch((x) -> x.healPlayer);
        prop.enemyCanGetVuln = cards.stream().anyMatch((x) -> x.card().vulnEnemy) || relics.stream().anyMatch((x) -> x.vulnEnemy);
        prop.enemyCanGetWeakened = cards.stream().anyMatch((x) -> x.card().weakEnemy) || relics.stream().anyMatch((x) -> x.weakEnemy);;
        prop.possibleBuffs |= cards.stream().anyMatch((x) -> x.card().cardName.contains("Corruption")) ? PlayerBuff.CORRUPTION.mask() : 0;
        prop.possibleBuffs |= cards.stream().anyMatch((x) -> x.card().cardName.contains("Barricade")) ? PlayerBuff.BARRICADE.mask() : 0;
        prop.possibleBuffs |= relics.stream().anyMatch((x) -> x instanceof Relic.Akabeko) ? PlayerBuff.AKABEKO.mask() : 0;
        prop.possibleBuffs |= relics.stream().anyMatch((x) -> x instanceof Relic.CentennialPuzzle) ? PlayerBuff.CENTENNIAL_PUZZLE.mask() : 0;
        prop.needDeckOrderMemory = cards.stream().anyMatch((x) -> x.card().putCardOnTopDeck);
        prop.selectFromExhaust = cards.stream().anyMatch((x) -> x.card().selectFromExhaust);
        prop.battleTranceExist = cards.stream().anyMatch((x) -> x.card().cardName.contains("Battle Trance"));
        prop.energyRefillCanChange = cards.stream().anyMatch((x) -> x.card().cardName.contains("Berserk"));
        prop.inputLen = getNNInputLen();

        // mcts related fields
        policy = null;
        q_win = new double[prop.maxNumOfActions];
        q_health = new double[prop.maxNumOfActions];
        q_comb = new double[prop.maxNumOfActions];
        n = new int[prop.maxNumOfActions];
        ns = new State[prop.maxNumOfActions];
        transpositionsPolicyMask = new boolean[prop.maxNumOfActions];
        terminal_action = -100;
        transpositions = new HashMap<>();
    }

    private int[] findUpgradeIdxes(List<CardCount> cards, List<Relic> relics) {
        if (cards.stream().noneMatch((x) -> x.card().cardName.contains("Armanent")) &&
            relics.stream().noneMatch((x) -> x instanceof Relic.WarpedTongs)) {
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
                l.add(prop.findCardIndex(new Card.Dazed()));
            }
        }
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).card().exhaustWhenPlayed && getCardEnergyCost(i) >= 0) {
                l.add(i);
            }
            if (cards.get(i).card().cardType == Card.POWER) {
                l.add(i);
            }
            if (cards.get(i).card().ethereal && getCardEnergyCost(i) >= 0) {
                l.add(i);
            }
            if (cards.get(i).card().exhaustNonAttacks) {
                for (int j = 0; j < cards.size(); j++) {
                    if (cards.get(j).card().cardType != Card.ATTACK) {
                        l.add(j);
                    }
                }
            }
            if (cards.get(i).card().exhaustSkill) {
                for (int j = 0; j < cards.size(); j++) {
                    if (cards.get(j).card().cardType == Card.SKILL) {
                        l.add(j);
                    }
                }
            }
            if (cards.get(i).card().selectFromDiscard || cards.get(i).card().canExhaustAnyCard) {
                for (int j = 0; j < cards.size(); j++) {
                    l.add(j);
                }
            }
            var gen = cards.get(i).card().getPossibleGeneratedCards(cards.stream().map(CardCount::card).toList());
            l.addAll(gen.stream().map((x) -> prop.findCardIndex(x)).toList());
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

    private List<CardCount> collectAllPossibleCards(List<CardCount> cards, List<Enemy> enemies, List<Relic> relics) {
        var set = new HashSet<>(cards);
        if (enemies.stream().anyMatch((x) -> x.canSlime)) {
            set.add(new CardCount(new Card.Slime(), 0));
        }
        if (enemies.stream().anyMatch((x) -> x.canDaze)) {
            set.add(new CardCount(new Card.Dazed(), 0));
        }
        do {
            var newSet = new HashSet<>(set);
            for (CardCount c : set) {
                for (Card possibleCard : c.card().getPossibleGeneratedCards(set.stream().map(CardCount::card).toList())) {
                    newSet.add(new CardCount(possibleCard, 0));
                }
            }
            for (Relic relic : relics) {
                for (Card possibleCard : relic.getPossibleGeneratedCards(set.stream().map(CardCount::card).toList())) {
                    newSet.add(new CardCount(possibleCard, 0));
                }
            }
            if (set.size() == newSet.size()) {
                break;
            }
            set = newSet;
        } while (true);
        return new ArrayList<>(set);
    }

    private GameState(GameState other) {
        prop = other.prop;

        actionCtx = other.actionCtx;
        deck = Arrays.copyOf(other.deck, other.deck.length);
        hand = Arrays.copyOf(other.hand, other.hand.length);
        discard = Arrays.copyOf(other.discard, other.discard.length);
        exhaust = other.exhaust;
        deckArr = Arrays.copyOf(other.deckArr, other.deckArr.length);
        deckArrLen = other.deckArrLen;
        energy = other.energy;
        turnNum = other.turnNum;
        playerTurnStartHealth = other.playerTurnStartHealth;
        energyRefill = other.energyRefill;
        player = other.player;
        enemies = new ArrayList<>();
        for (int i = 0; i < other.enemies.size(); i++) {
            enemies.add(other.enemies.get(i).copy());
        }
        enemiesAlive = other.enemiesAlive;
        previousCard = other.previousCard;
        previousCardIdx = other.previousCardIdx;
        drawOrder = other.drawOrder;
        if (other.gameActionDeque != null && other.gameActionDeque.size() > 0) {
            gameActionDeque = new ArrayDeque<>(other.gameActionDeque);
        }

        buffs = other.buffs;
        counter = other.counter;
        lastEnemySelected = other.lastEnemySelected;
        thorn = other.thorn;

        policy = null;
        q_health = new double[prop.maxNumOfActions];
        q_comb = new double[prop.maxNumOfActions];
        q_win = new double[prop.maxNumOfActions];
        n = new int[prop.maxNumOfActions];
        ns = new State[prop.maxNumOfActions];
        transpositionsPolicyMask = new boolean[prop.maxNumOfActions];
        terminal_action = -100;
    }

    public GameState clone(boolean keepTranspositions) {
        GameState clone = new GameState(this);
        if (keepTranspositions) {
            clone.transpositions = transpositions;
        } else {
            clone.transpositions = new HashMap<>();
        }
        return clone;
    }

    void draw(int count) {
        if (getPlayeForRead().cannotDrawCard()) {
            return;
        }
//        if (deckArrLen != count) { // todo: add discard count too, enemy nextMove should also set isStochastic
            isStochastic = true;
//        }
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
            if (drawOrder.size() > 0) {
                i = getDrawOrderForWrite().drawTop();
                assert deck[i] > 0;
                drawCardByIdx(i, true);
            } else {
                i = prop.random.nextInt(this.deckArrLen);
                deck[deckArr[i]] -= 1;
                hand[deckArr[i]] += 1;
                deckArr[i] = deckArr[deckArrLen - 1];
                deckArrLen -= 1;
            }
        }
    }

    int drawOneCardSpecial() {
        isStochastic = true;
        if (deckArrLen == 0) {
            reshuffle();
        }
        if (deckArrLen == 0) {
            return -1;
        }
        int i;
        if (drawOrder.size() > 0) {
            i = getDrawOrderForWrite().drawTop();
            drawCardByIdx(i, false);
        } else {
            i = prop.random.nextInt(this.deckArrLen);
            deck[deckArr[i]] -= 1;
            deckArr[i] = deckArr[deckArrLen - 1];
            deckArrLen -= 1;
        }
        return i;
    }

    private int getCardEnergyCost(int cardIdx) {
        if ((buffs & PlayerBuff.CORRUPTION.mask()) != 0 && prop.cardDict[cardIdx].cardType == Card.SKILL) {
            return 0;
        } else if (prop.hasBlueCandle && prop.cardDict[cardIdx].cardType == Card.CURSE) {
            return 0;
        } else if (prop.hasMedicalKit && prop.cardDict[cardIdx].cardType == Card.STATUS) {
            return 0;
        }
        return prop.cardDict[cardIdx].energyCost(this);
    }

    public void gotoActionCtx(GameActionCtx ctx, Card card, int card_idx) {
        switch (ctx) {
        case PLAY_CARD -> {
            previousCard = null;
            previousCardIdx = -1;
            lastEnemySelected = -1;
            actionCtx = ctx;
        }
        case SELECT_ENEMY, SELECT_CARD_HAND, SELECT_CARD_DISCARD, SELECT_CARD_EXHAUST -> {
            previousCard = card;
            previousCardIdx = card_idx;
            actionCtx = ctx;
        }
        case BEGIN_TURN -> actionCtx = ctx;
        }
        actionsCache = null;
    }

    void playCard(int cardIdx, int selectIdx, boolean cloned, boolean useEnergy) {
        if (actionCtx == GameActionCtx.PLAY_CARD) {
            hand[cardIdx] -= 1;
            if (useEnergy) {
                energy -= getCardEnergyCost(cardIdx);
            }
            if (prop.cardDict[cardIdx].selectEnemy) {
                gotoActionCtx(GameActionCtx.SELECT_ENEMY, prop.cardDict[cardIdx], cardIdx);
            } else if (prop.cardDict[cardIdx].selectFromHand && !prop.cardDict[cardIdx].selectFromHandLater) {
                gotoActionCtx(GameActionCtx.SELECT_CARD_HAND, prop.cardDict[cardIdx], cardIdx);
            } else if (prop.cardDict[cardIdx].selectFromDiscard && !prop.cardDict[cardIdx].selectFromDiscardLater) {
                gotoActionCtx(GameActionCtx.SELECT_CARD_DISCARD, prop.cardDict[cardIdx], cardIdx);
            } else if (prop.cardDict[cardIdx].selectFromExhaust) {
                gotoActionCtx(GameActionCtx.SELECT_CARD_EXHAUST, prop.cardDict[cardIdx], cardIdx);
            }
        }

        do {
            if (actionCtx == GameActionCtx.SELECT_ENEMY) {
                if (enemiesAlive == 1) {
                    for (int i = 0; i < enemies.size(); i++) {
                        if (enemies.get(i).health > 0) {
                            lastEnemySelected = i;
                            gotoActionCtx(prop.cardDict[cardIdx].play(this, i), prop.cardDict[cardIdx], cardIdx);
                        }
                    }
                } else if (selectIdx >= 0) {
                    lastEnemySelected = selectIdx;
                    gotoActionCtx(prop.cardDict[cardIdx].play(this, selectIdx), prop.cardDict[cardIdx], cardIdx);
                    selectIdx = -1;
                } else {
                    break;
                }
            } else if (actionCtx == GameActionCtx.SELECT_CARD_DISCARD) {
                int possibleChoicesCount = 0, lastIdx = 0;
                for (int j = 0; j < discard.length; j++) {
                    possibleChoicesCount += discard[j] > 0 ? 1 : 0;
                    lastIdx = discard[j] > 0 ? j : lastIdx;
                }
                if (possibleChoicesCount == 0) {
                    gotoActionCtx(GameActionCtx.PLAY_CARD, prop.cardDict[cardIdx], cardIdx);
                } else if (possibleChoicesCount == 1) {
                    gotoActionCtx(prop.cardDict[cardIdx].play(this, lastIdx), prop.cardDict[cardIdx], cardIdx);
                } else if (selectIdx >= 0) {
                    gotoActionCtx(prop.cardDict[cardIdx].play(this, selectIdx), prop.cardDict[cardIdx], cardIdx);
                    selectIdx = -1;
                } else {
                    break;
                }
            } else if (actionCtx == GameActionCtx.SELECT_CARD_HAND) {
                int possibleChoicesCount = 0, lastIdx = 0;
                for (int j = 0; j < hand.length; j++) {
                    if (hand[j] > 0 && prop.cardDict[cardIdx].canSelectFromHand(prop.cardDict[j])) {
                        possibleChoicesCount += 1;
                        lastIdx = j;
                    }
                }
                if (possibleChoicesCount == 0) {
                    gotoActionCtx(GameActionCtx.PLAY_CARD, prop.cardDict[cardIdx], cardIdx);
                } else if (possibleChoicesCount == 1) {
                    gotoActionCtx(prop.cardDict[cardIdx].play(this, lastIdx), prop.cardDict[cardIdx], cardIdx);
                } else if (selectIdx >= 0) {
                    gotoActionCtx(prop.cardDict[cardIdx].play(this, selectIdx), prop.cardDict[cardIdx], cardIdx);
                    selectIdx = -1;
                } else {
                    break;
                }
            } else if (actionCtx == GameActionCtx.SELECT_CARD_EXHAUST) {
                int possibleChoicesCount = 0, lastIdx = 0;
                for (int j = 0; j < exhaust.length; j++) {
                    possibleChoicesCount += exhaust[j] > 0 ? 1 : 0;
                    lastIdx = exhaust[j] > 0 ? j : lastIdx;
                }
                if (possibleChoicesCount == 0) {
                    gotoActionCtx(GameActionCtx.PLAY_CARD, prop.cardDict[cardIdx], cardIdx);
                } else if (possibleChoicesCount == 1) {
                    gotoActionCtx(prop.cardDict[cardIdx].play(this, lastIdx), prop.cardDict[cardIdx], cardIdx);
                } else if (selectIdx >= 0) {
                    gotoActionCtx(prop.cardDict[cardIdx].play(this, selectIdx), prop.cardDict[cardIdx], cardIdx);
                    selectIdx = -1;
                } else {
                    break;
                }
            } else if (actionCtx == GameActionCtx.PLAY_CARD) {
                gotoActionCtx(prop.cardDict[cardIdx].play(this, -1), prop.cardDict[cardIdx], cardIdx);
            }
        } while (actionCtx != GameActionCtx.PLAY_CARD);

        if (actionCtx == GameActionCtx.PLAY_CARD) {
            for (Enemy enemy : enemies) {
                enemy.react(this, prop.cardDict[cardIdx]);
            }
            for (var handler : prop.onCardPlayedHandlers) {
                handler.handle(this, prop.cardDict[cardIdx]);
            }
            if (!cloned) {
                if (prop.cardDict[cardIdx].exhaustWhenPlayed) {
                    exhaustedCardHandle(cardIdx, true);
                } else if ((buffs & PlayerBuff.CORRUPTION.mask()) != 0 && prop.cardDict[cardIdx].cardType == Card.SKILL) {
                    exhaustedCardHandle(cardIdx, true);
                } else if (prop.cardDict[cardIdx].cardType != Card.POWER) {
                    discard[cardIdx] += 1;
                }
            }
            if (prop.normalityCounterIdx < 0 || counter[prop.normalityCounterIdx] < 3) {
                if (gameActionDeque != null && gameActionDeque.size() > 0) {
                    gameActionDeque.pollFirst().doAction(this);
                }
            }
        }
    }

    void startTurn() {
        turnNum++;
        playerTurnStartHealth = getPlayeForRead().getHealth();
        gainEnergy(energyRefill);
        for (Enemy enemy : enemies) {
            if (enemy.health > 0) {
                enemy.nextMove(prop.random);
            }
            enemy.startTurn();
        }
        draw(5);
        for (GameEventHandler handler : prop.startOfTurnHandlers) {
            handler.handle(this);
        }
    }

    private void endTurn() {
        for (GameEventHandler handler : prop.preEndTurnHandlers) {
            handler.handle(this);
        }
        for (int i = 0; i < hand.length; i++) {
            if (hand[i] > 0) {
                if (prop.cardDict[i].ethereal) {
                    for (int count = 0; count < hand[i]; count++) {
                        exhaustedCardHandle(i, false);
                    }
                    hand[i] = 0;
                } else if (!prop.hasRunicPyramid) {
                    discard[i] += hand[i];
                    hand[i] = 0;
                }
            }
        }
        for (Enemy enemy : enemies) {
            if (enemy.health > 0) {
                enemy.doMove(this);
                enemy.endTurn();
            }
        }
        getPlayerForWrite().endTurn(this);
        if (!prop.hasIceCream) {
            energy = 0;
        }
    }

    void doAction(int actionIdx) {
        GameAction action = prop.actionsByCtx[actionCtx.ordinal()][actionIdx];
        if (action.type() == GameActionType.START_GAME) {
            startTurn();
            gotoActionCtx(GameActionCtx.PLAY_CARD, null, -1);
        } else if (action.type() == GameActionType.END_TURN) {
            endTurn();
//            startTurn();
            gotoActionCtx(GameActionCtx.BEGIN_TURN, null, -1);
        } else if (action.type() == GameActionType.PLAY_CARD) {
            playCard(action.cardIdx(), -1, false, true);
        } else if (action.type() == GameActionType.SELECT_ENEMY) {
            playCard(previousCardIdx, action.enemyIdx(), false, true);
        } else if (action.type() == GameActionType.SELECT_CARD_HAND) {
            playCard(previousCardIdx, action.cardIdx(), false, true);
        } else if (action.type() == GameActionType.SELECT_CARD_DISCARD) {
            playCard(previousCardIdx, action.cardIdx(), false, true);
        } else if (action.type() == GameActionType.SELECT_CARD_EXHAUST) {
            playCard(previousCardIdx, action.cardIdx(), false, true);
        } else if (action.type() == GameActionType.BEGIN_TURN) {
            startTurn();
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
        if (actionsCache != null) {
            if (action >=0 && action < actionsCache.length) {
                return actionsCache[action];
            }
            return false;
        }
        if (actionCtx == GameActionCtx.START_GAME || actionCtx == GameActionCtx.BEGIN_TURN) {
            return action == 0;
        } else if (actionCtx == GameActionCtx.PLAY_CARD) {
            if (prop.normalityCounterIdx >= 0 && counter[prop.normalityCounterIdx] >= 3) {
                return false;
            }
            boolean[] actions = new boolean[prop.maxNumOfActions];
            GameAction[] a = prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()];
            if (action < 0 || action >= a.length) {
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
            if (action < 0 || action >= a.length) {
                return false;
            }
            return enemies.get(a[action].enemyIdx()).health > 0;
        } else if (actionCtx == GameActionCtx.SELECT_CARD_HAND) {
            GameAction[] a = prop.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()];
            if (action < 0 || action >= a.length) {
                return false;
            }
            return hand[a[action].cardIdx()] > 0 && previousCard.canSelectFromHand(prop.cardDict[action]);
        } else if (actionCtx == GameActionCtx.SELECT_CARD_DISCARD) {
            GameAction[] a = prop.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()];
            if (action < 0 || action >= a.length) {
                return false;
            }
            return discard[a[action].cardIdx()] > 0;
        } else if (actionCtx == GameActionCtx.SELECT_CARD_EXHAUST) {
            GameAction[] a = prop.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()];
            if (action < 0 || action >= a.length) {
                return false;
            }
            return exhaust[a[action].cardIdx()] > 0;
        }
        return false;
    }

    void get_v(double[] out) {
        var player = getPlayeForRead();
        if (player.getHealth() <= 0 || turnNum > 30) {
            out[0] = 0;
            out[1] = 0;
            out[2] = 0;
            return;
        } else {
            if (enemies.stream().allMatch((x) -> x.health <= 0)) {
                out[0] = 1;
                out[1] = ((double) player.getHealth()) / player.getMaxHealth();
                out[2] = calc_q(out[0], out[1]);
                return;
            }
        }
        out[0] = v_win;
        out[1] = v_health;
        out[2] = calc_q(out[0], out[1]);
    }

    int isTerminal() {
        if (getPlayeForRead().getHealth() <= 0 || turnNum > 30) {
            return -1;
        } else {
            return enemies.stream().allMatch((x) -> x.health <= 0) ? 1 : 0;
        }
    }

    @Override public String toString() {
        return toStringReadable();
    }

    public static double calc_q(double q_win, double q_health) {
        return q_win * 0.5 + q_win * q_win * q_health * 0.5;
//        return q_win * 0.5 + q_health * 0.5;
    }

    public String toStringReadable() {
        boolean first;
        StringBuilder str = new StringBuilder("{");
        str.append("hand=[");
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
        str.append(", deck=[");
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
        if (prop.selectFromExhaust) {
            str.append(", exhaust=[");
            first = true;
            for (int i = 0; i < exhaust.length; i++) {
                if (exhaust[i] > 0) {
                    if (!first) {
                        str.append(", ");
                    }
                    first = false;
                    str.append(exhaust[i]).append(" ").append(prop.cardDict[i].cardName);
                }
            }
            str.append("]");
        }
        str.append(", energy=").append(energy).append(", ctx=").append(actionCtx).append(", ").append(getPlayeForRead());
        str.append(", [");
        int eAlive = 0;
        for (Enemy enemy : enemies) {
            if (enemy.health > 0) {
                str.append(enemy.toString(this));
                if (++eAlive < enemiesAlive) {
                    str.append(", ");
                }
            }
        }
        str.append("]");
        if (buffs > 0) {
            str.append(", buffs=[");
            first = true;
            for (PlayerBuff buff : PlayerBuff.BUFFS) {
                if ((buffs & buff.mask()) != 0) {
                    str.append(first ? "" : ", ").append(buff.name());
                    first = false;
                }
            }
            str.append("]");
        }
        if (prop.counterHandlersNonNull.length > 0) {
            StringBuilder tmp = new StringBuilder();
            first = true;
            for (int i = 0; i < prop.counterHandlers.length; i++) {
                if (prop.counterHandlers[i] != null && getCounterForRead()[i] != 0) {
                    tmp.append(first ? "" : ", ").append(prop.counterNames[i]).append('=').append(getCounterForRead()[i]);
                    first = false;
                }
            }
            if (tmp.length() > 0) {
                str.append(", other=[").append("tmp").append("]");
            }
        }
        str.append(", v=(").append(formatFloat(v_win)).append(", ").append(formatFloat(v_health)).append("/").append(formatFloat(v_health * getPlayeForRead().getMaxHealth())).append(")");
        str.append(", q/p/n=[");
        first = true;
        for (int i = 0; i < q_win.length; i++) {
            var p_str = policy != null ? formatFloat(policy[i]) : "0";
            var p_str2 = policyMod != null ? formatFloat(policyMod[i]) : null;
            var q_win_str = formatFloat(n[i] == 0 ? 0 : q_win[i] / n[i]);
            var q_health_str = formatFloat(n[i] == 0 ? 0 : q_health[i] / n[i]);
            var q_str = formatFloat(n[i] == 0 ? 0 : q_comb[i] / n[i]);
            if (isActionLegal(i)) {
                if (!first) {
                    str.append(", ");
                }
                first = false;
                if (p_str2 == null) {
                    str.append(q_str).append('/').append(q_win_str).append('/').append(q_health_str).append('/').append(p_str).append('/').append(n[i]);
                } else {
                    str.append(q_str).append('/').append(q_win_str).append('/').append(q_health_str).append('/').append(p_str).append('/').append(p_str2).append('/').append(n[i]);
                }
                str.append(" (").append(getActionString(i)).append(")");
            }
        }
        str.append(']');
        str.append('}');
        return str.toString();
    }

    public void doEval(Model model) {
        NNOutput output = model.eval(this);
        this.policy = output.policy();
        this.v_health = Math.min(output.v_health(), getPlayeForRead().getHealth() / (float) getPlayeForRead().getMaxHealth());
        this.v_win = output.v_win();
    }

    private int getNNInputLen() {
        int inputLen = 0;
        inputLen += deck.length;
        inputLen += hand.length;
        inputLen += prop.discardIdxes.length;
        if (prop.selectFromExhaust) {
            inputLen += exhaust.length;
        }
        if (MAX_AGENT_DECK_ORDER_MEMORY > 0 && prop.needDeckOrderMemory) {
            inputLen += hand.length * MAX_AGENT_DECK_ORDER_MEMORY;
        }
        for (int i = 2; i < prop.actionsByCtx.length; i++) {
            if (prop.actionsByCtx[i] != null && i != GameActionCtx.BEGIN_TURN.ordinal()) {
                inputLen += 1;
            }
        }
        inputLen += 1; // energy
        inputLen += 1; // player health
        inputLen += 1; // player block
        inputLen += 1; // player artifact
        if (prop.playerStrengthCanChange) {
            inputLen += 1; // player strength
        }
        if (prop.playerDexterityCanChange) {
            inputLen += 1; // player dexterity
        }
        if (prop.playerStrengthEotCanChange) {
            inputLen += 1; // player lose strength eot
        }
        if (prop.playerDexterityEotCanChange) {
            inputLen += 1; // player lose dexterity eot
        }
        if (prop.playerCanGetVuln) {
            inputLen += 1; // player vulnerable
        }
        if (prop.playerCanGetWeakened) {
            inputLen += 1; // player weak
        }
        if (prop.playerCanGetFrailed) {
            inputLen += 1; // player weak
        }
        if (prop.battleTranceExist) {
            inputLen += 1; // battle trance
        }
        if (prop.energyRefillCanChange) {
            inputLen += 1; // berserk
        }
        for (PlayerBuff buff : PlayerBuff.BUFFS) {
            if ((prop.possibleBuffs & buff.mask()) != 0) {
                inputLen += 1; // barricade in deck
            }
        }
        for (var handler : prop.counterHandlersNonNull) {
            inputLen += handler.getInputLenDelta();
        }
        // cards currently selecting enemies
        if (prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null ||
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()] != null ||
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()] != null ||
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()] != null) {
            for (GameAction action : prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (prop.cardDict[action.cardIdx()].selectEnemy ||
                        prop.cardDict[action.cardIdx()].selectFromHand ||
                        prop.cardDict[action.cardIdx()].selectFromDiscard ||
                        prop.cardDict[action.cardIdx()].selectFromExhaust) {
                    inputLen += 1;
                }
            }
        }
        for (Enemy enemy : enemies) {
            inputLen += 1; // enemy health
            if (prop.enemyCanGetVuln) {
                inputLen += 1; // enemy vulnerable
            }
            if (prop.enemyCanGetWeakened) {
                inputLen += 1; // enemy weak
            }
            if (enemy.canGainBlock) {
                inputLen += 1; // enemy block
            }
            if (enemy.canGainStrength) {
                inputLen += 1; // enemy strength
            }
            if (enemy.hasArtifact) {
                inputLen += 1; // enemy artifact
            }
            inputLen += enemy.numOfMoves; // enemy moves
            if (enemy.moveHistory != null) {
                for (int move : enemy.moveHistory) {
                    inputLen += enemy.numOfMoves;
                }
            }
            if (enemy instanceof Enemy.RedLouse || enemy instanceof Enemy.GreenLouse) {
                inputLen += 1;
            } else if (enemy instanceof Enemy.TheGuardian guardian) {
                inputLen += 2;
            }
        }
        return inputLen;
    }

    public String getNNInputDesc() {
        var str = "Possible Cards:\n";
        for (Card card : prop.cardDict) {
            str += "    " + card.cardName + "\n";
        }
        str += "Cards That Can Change In Number:\n";
        for (int discardIdx : prop.discardIdxes) {
            str += "    " + prop.cardDict[discardIdx].cardName + "\n";
        }
        str += "Neural Network Input Breakdown (" + prop.inputLen + " parameters):\n";
        str += "    " + deck.length + " parameters for cards in deck\n";
        str += "    " + hand.length + " parameters for cards in hand\n";
        str += "    " + prop.discardIdxes.length + " parameters to keep track of cards in discard\n";
        if (prop.selectFromExhaust) {
            str += "    " + exhaust.length + " parameters for cards in exhaust\n";
        }
        if (MAX_AGENT_DECK_ORDER_MEMORY > 0 && prop.needDeckOrderMemory) {
            str += "    " + hand.length * MAX_AGENT_DECK_ORDER_MEMORY + " parameters to keep track of known card at top of deck\n";
        }
        for (int i = 2; i < prop.actionsByCtx.length; i++) {
            if (prop.actionsByCtx[i] != null && i != GameActionCtx.BEGIN_TURN.ordinal()) {
                str += "    1 parameter to keep track of ctx " + GameActionCtx.values()[i] + "\n";
            }
        }
        str += "    1 parameter to keep track of energy\n";
        str += "    1 parameter to keep track of player health\n";
        str += "    1 parameter to keep track of player block\n";
        if (prop.playerStrengthCanChange) {
            str += "    1 parameter to keep track of player strength\n";
        }
        if (prop.playerDexterityCanChange) {
            str += "    1 parameter to keep track of player dexterity\n";
        }
        if (prop.playerStrengthEotCanChange) {
            str += "    1 parameter to keep track of player lose strength eot debuff\n";
        }
        if (prop.playerDexterityEotCanChange) {
            str += "    1 parameter to keep track of player lose dexterity eot debuff\n";
        }
        if (prop.playerCanGetVuln) {
            str += "    1 parameter to keep track of player vulnerable\n";
        }
        if (prop.playerCanGetWeakened) {
            str += "    1 parameter to keep track of player weak\n";
        }
        if (prop.playerCanGetFrailed) {
            str += "    1 parameter to keep track of player frail\n";
        }
        if (prop.battleTranceExist) {
            str += "    1 parameter to keep track of battle trance cannot draw card debuff\n";
        }
        if (prop.energyRefillCanChange) {
            str += "    1 parameter to keep track of berserk\n";
        }
        for (PlayerBuff buff : PlayerBuff.BUFFS) {
            if ((prop.possibleBuffs & buff.mask()) != 0) {
                str += "    1 parameter to keep track of buff " + buff.name() + "\n";
            }
        }
        for (int i = 0; i < prop.counterHandlers.length; i++) {
            if (prop.counterHandlers[i] != null) {
                str += "    " + prop.counterHandlers[i].getInputLenDelta() + " parameter to keep track of counter for " + prop.counterNames[i] + "\n";
            }
        }
        // cards currently selecting enemies
        if (prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null ||
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()] != null ||
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()] != null ||
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()] != null) {
            for (GameAction action : prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (prop.cardDict[action.cardIdx()].selectEnemy ||
                        prop.cardDict[action.cardIdx()].selectFromHand ||
                        prop.cardDict[action.cardIdx()].selectFromDiscard ||
                        prop.cardDict[action.cardIdx()].selectFromExhaust) {
                    str += "    1 parameter to keep track of currently played card " + prop.cardDict[action.cardIdx()].cardName + "\n";
                }
            }
        }
        for (Enemy enemy : enemies) {
            str += "    *** " + enemy.getName() + " ***\n";
            str += "        1 parameter to keep track of health\n";
            if (prop.enemyCanGetVuln) {
                str += "        1 parameter to keep track of vulnerable\n";
            }
            if (prop.enemyCanGetWeakened) {
                str += "        1 parameter to keep track of weak\n";
            }
            if (enemy.canGainBlock) {
                str += "        1 parameter to keep track of block\n";
            }
            if (enemy.canGainStrength) {
                str += "        1 parameter to keep track of strength\n";
            }
            if (enemy.hasArtifact) {
                str += "        1 parameter to keep track of artifact\n";
            }
            str += "        " + enemy.numOfMoves + " parameters to keep track of current move from enemy\n";
            if (enemy.moveHistory != null) {
                str += "        " + enemy.numOfMoves + "*" + enemy.moveHistory.length + " parameters to keep track of move history from enemy\n";
            }
            if (enemy instanceof Enemy.RedLouse || enemy instanceof Enemy.GreenLouse) {
                str += "        1 parameter to keep track of louse damage\n";
            } else if (enemy instanceof Enemy.TheGuardian guardian) {
                str += "        2 parameter to keep track of current and max guardian mode shift damage\n";
            }
        }
        return str;
    }

    public float[] getNNInput() {
        var player = getPlayeForRead();
        int idx = 0;
        var x = new float[prop.inputLen];
        for (int j : deck) {
            x[idx++] = j / (float) 10.0;
        }
        for (int j : hand) {
            x[idx++] = j / (float) 10.0;
        }
        for (int i = 0; i < prop.discardIdxes.length; i++) {
            x[idx++] = discard[prop.discardIdxes[i]] / (float) 10.0;
        }
        if (prop.selectFromExhaust) {
            for (int j : exhaust) {
                x[idx++] = j / (float) 10.0;
            }
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
        for (int i = 2; i < prop.actionsByCtx.length; i++) {
            if (prop.actionsByCtx[i] != null && i != GameActionCtx.BEGIN_TURN.ordinal()) {
                x[idx++] = actionCtx.ordinal() == i ? 0.5f : -0.5f;
            }
        }
        x[idx++] = energy / (float) 10;
        x[idx++] = player.getHealth() / (float) player.getMaxHealth();
        x[idx++] = player.getBlock() / (float) 40.0;
        x[idx++] = player.getArtifact() / (float) 1.0;
        if (prop.playerStrengthCanChange) {
            x[idx++] = player.getStrength() / (float) 10.0;
        }
        if (prop.playerDexterityCanChange) {
            x[idx++] = player.getDexterity() / (float) 10.0;
        }
        if (prop.playerStrengthEotCanChange) {
            x[idx++] = player.getLoseStrengthEot() / (float) 10.0;
        }
        if (prop.playerDexterityEotCanChange) {
            x[idx++] = player.getLoseDexterityEot() / (float) 10.0;
        }
        if (prop.playerCanGetVuln) {
            x[idx++] = player.getVulnerable() / (float) 10.0;
        }
        if (prop.playerCanGetWeakened) {
            x[idx++] = player.getWeak() / (float) 10.0;
        }
        if (prop.playerCanGetFrailed) {
            x[idx++] = player.getFrail() / (float) 10.0;
        }
        if (prop.battleTranceExist) {
            x[idx++] = player.cannotDrawCard() ? 0.5f : -0.5f;
        }
        if (prop.energyRefillCanChange) {
            x[idx++] = (energyRefill - 5) / 2f;
        }
        for (PlayerBuff buff : PlayerBuff.BUFFS) {
            if ((prop.possibleBuffs & buff.mask()) != 0) {
                x[idx++] = (buffs & buff.mask()) != 0 ? 0.5f : -0.5f;
            }
        }
        for (var handler : prop.counterHandlersNonNull) {
            idx = handler.addToInput(this, x, idx);
        }
        if (prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null ||
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()] != null ||
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()] != null ||
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()] != null) {
            for (GameAction action : prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (prop.cardDict[action.cardIdx()].selectEnemy ||
                        prop.cardDict[action.cardIdx()].selectFromHand ||
                        prop.cardDict[action.cardIdx()].selectFromDiscard ||
                        prop.cardDict[action.cardIdx()].selectFromExhaust) {
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
                } else if (enemy instanceof Enemy.TheGuardian guardian) {
                    x[idx++] = (guardian.modeShiftDmg - 50) / 20f;
                    x[idx++] = (guardian.maxModeShiftDmg - 50) / 20f;
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
                } else if (enemy instanceof Enemy.TheGuardian) {
                    x[idx++] = -0.1f;
                    x[idx++] = -0.1f;
                }
            }
        }
        return x;
    }

    void addGameActionToEndOfDeque(GameEnvironmentAction action) {
        if (gameActionDeque == null) {
            gameActionDeque = new ArrayDeque<>();
        }
        gameActionDeque.addLast(action);
    }

    void exhaustCardFromHand(int cardIdx) {
        assert hand[cardIdx] > 0;
        hand[cardIdx] -= 1;
        exhaustedCardHandle(cardIdx, false);
    }

    void exhaustedCardHandle(int cardIdx, boolean fromCardPlay) {
        if (fromCardPlay && prop.hasStrangeSpoon && prop.random.nextBoolean()) {
            discard[cardIdx] += 1;
            return;
        }
        prop.cardDict[cardIdx].onExhaust(this);
        getExhaustForWrite()[cardIdx] += 1;
        for (int i = 0; i < prop.onExhaustHandlers.size(); i++) {
            prop.onExhaustHandlers.get(i).handle(this);
        }
    }

    public boolean drawCardByIdx(int card_idx, boolean addToHand) {
        if (deck[card_idx] > 0) {
            deck[card_idx] -= 1;
            if (addToHand) hand[card_idx] += 1;
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

    public void discardHand() {
        for (int i = 0; i < hand.length; i++) {
            discard[i] += hand[i];
            hand[i] = 0;
        }
    }

    public void reshuffle() {
        for (int i = 0; i < discard.length; i++) {
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
            if (enemiesAlive > 1) {
                return "Attack " + enemies.get(action.enemyIdx()).getName() + "(" + action.enemyIdx() + ")";
            } else {
                return "Attack " + enemies.get(action.enemyIdx()).getName();
            }
        } else if (action.type() == GameActionType.SELECT_CARD_HAND) {
            return "Select " + prop.cardDict[action.cardIdx()].cardName + " From Hand";
        } else if (action.type() == GameActionType.SELECT_CARD_DISCARD) {
            return "Select " + prop.cardDict[action.cardIdx()].cardName + " From Discard";
        } else if (action.type() == GameActionType.SELECT_CARD_EXHAUST) {
            return "Select " + prop.cardDict[action.cardIdx()].cardName + " From Exhaust";
        }
        return "Unknown";
    }

    public GameAction getAction(int i) {
        return prop.actionsByCtx[actionCtx.ordinal()][i];
    }

    public void addStartOfTurnHandler(GameEventHandler handler) {
        prop.startOfTurnHandlers.add(handler);
    }

    public void addStartOfTurnHandler(String handlerName, GameEventHandler handler) {
        if (!prop.gameEventHandlers.contains(handlerName + "StartOfTurn")) {
            prop.gameEventHandlers.add(handlerName + "StartOfTurn");
            prop.startOfTurnHandlers.add(handler);
        }
    }

    public void addPreEndOfTurnHandler(GameEventHandler handler) {
        prop.preEndTurnHandlers.add(handler);
    }

    public void addPreEndOfTurnHandler(String handlerName, GameEventHandler handler) {
        if (!prop.gameEventHandlers.contains(handlerName + "PreEndOfTurn")) {
            prop.gameEventHandlers.add(handlerName + "PreEndOfTurn");
            prop.preEndTurnHandlers.add(handler);
        }
    }

    public void addOnExhaustHandler(GameEventHandler handler) {
        prop.onExhaustHandlers.add(handler);
    }

    public void addOnExhaustHandler(String handlerName, GameEventHandler handler) {
        if (!prop.gameEventHandlers.contains(handlerName + "OnExhaust")) {
            prop.gameEventHandlers.add(handlerName + "OnExhaust");
            prop.onExhaustHandlers.add(handler);
        }
    }

    public void addOnBlockHandler(GameEventHandler handler) {
        prop.onBlockHandlers.add(handler);
    }

    public void addOnBlockHandler(String handlerName, GameEventHandler handler) {
        if (!prop.gameEventHandlers.contains(handlerName + "OnBlock")) {
            prop.gameEventHandlers.add(handlerName + "OnBlock");
            prop.onBlockHandlers.add(handler);
        }
    }

    public void addOnDamageHandler(OnDamageHandler handler) {
        prop.onDamageHandlers.add(handler);
    }

    public void addOnDamageHandler(String handlerName, OnDamageHandler handler) {
        if (!prop.gameEventHandlers.contains(handlerName + "OnDamage")) {
            prop.gameEventHandlers.add(handlerName + "OnDamage");
            prop.onDamageHandlers.add(handler);
        }
    }

    public void addOnCardPlayedHandler(GameEventCardHandler handler) {
        prop.onCardPlayedHandlers.add(handler);
    }

    public void addOnCardPlayedHandler(String handlerName, GameEventCardHandler handler) {
        if (!prop.gameEventHandlers.contains(handlerName + "OnCardPlayed")) {
            prop.gameEventHandlers.add(handlerName + "OnCardPlayed");
            prop.onCardPlayedHandlers.add(handler);
        }
    }

    public void addOnCardDrawnHandler(GameEventCardHandler handler) {
        prop.onCardDrawnHandlers.add(handler);
    }

    public void addOnCardDrawnHandler(String handlerName, GameEventCardHandler handler) {
        if (!prop.gameEventHandlers.contains(handlerName + "OnCardDrawn")) {
            prop.gameEventHandlers.add(handlerName + "OnCardDrawn");
            prop.onCardDrawnHandlers.add(handler);
        }
    }

    public void clearNextStates() { // oom during training due to holding too many states
        Arrays.fill(ns, null);
        transpositions = new HashMap<>();
    }

    public void clearAllSearchInfo() {
        policy = null;
        q_health = new double[prop.maxNumOfActions];
        q_comb = new double[prop.maxNumOfActions];
        q_win = new double[prop.maxNumOfActions];
        n = new int[prop.maxNumOfActions];
        ns = new State[prop.maxNumOfActions];
        transpositionsPolicyMask = new boolean[prop.maxNumOfActions];
        transpositions = new HashMap<>();
        terminal_action = -100;
        total_n = 0;
        total_q_win = 0;
        total_q_health = 0;
        total_q_comb = 0;
        v_health = 0;
        v_win = 0;
        actionsCache = null;
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

    public void removeCardFromHand(int idx) {
        if (hand[idx] > 0) {
            hand[idx]--;
        }
    }

    public void putCardOnTopOfDeck(int idx) {
        deck[idx]++;
        deckArr[deckArrLen++] = idx;
        getDrawOrderForWrite().pushOnTop(idx);
    }

    public void playerDoDamageToEnemy(Enemy enemy, int dmg) {
        var player = getPlayeForRead();
        if ((buffs & PlayerBuff.AKABEKO.mask()) != 0) {
            dmg += 8;
        }
        dmg += player.getStrength();
        if (prop.penNibCounterIdx >= 0 && counter[prop.penNibCounterIdx] == 9) {
            dmg *= 2;
        }
        if (enemy.vulnerable > 0) {
            dmg = dmg + dmg / 2;
        }
        if (player.getWeak() > 0) {
            dmg = dmg * 3 / 4;
        }
        if (prop.hasBoot && dmg < 5) {
            dmg = 5;
        }
        if (enemy.health > 0) {
            enemy.damage(dmg, this);
        }
    }

    public void playerDoNonAttackDamageToEnemy(Enemy enemy, int dmg, boolean blockable) {
        if (enemy.health > 0) {
            enemy.nonAttackDamage(dmg, blockable, this);
        }
    }

    public void enemyDoDamageToPlayer(Enemy enemy, int dmg, int times) {
        int move = enemy.move;
        var player = getPlayerForWrite();
        for (int i = 0; i < times; i++) {
            if (enemy.health <= 0 || enemy.move != move) { // dead or interrupted
                return;
            }
            dmg += enemy.strength;
            if (player.getVulnerable() > 0) {
                dmg = dmg + dmg / (prop.hasOddMushroom ? 4 : 2);
            }
            if (enemy.weak > 0) {
                dmg = dmg * 3 / 4;
            }
            int dmgDealt = player.damage(dmg);
            if (thorn > 0) {
                enemy.nonAttackDamage(thorn, false, this);
            }
            if (dmg > 0) {
                for (OnDamageHandler handler : prop.onDamageHandlers) {
                    handler.handle(this, enemy, true, dmgDealt);
                }
            }
        }
    }

    public int enemyCalcDamageToPlayer(Enemy enemy, int d) {
        d += enemy.strength;
        if (enemy.weak > 0) {
            d = d * 3 / 4;
        }
        if (getPlayeForRead().getVulnerable() > 0) {
            d = d + d / 2;
        }
        return d;
    }

    public void doNonAttackDamageToPlayer(int dmg, boolean blockable, Object source) {
        var damageDealt = getPlayerForWrite().nonAttackDamage(dmg, blockable);
        if (dmg > 0) {
            for (OnDamageHandler handler : prop.onDamageHandlers) {
                handler.handle(this, source, false, damageDealt);
            }
        }
    }

    public int[] getExhaustForRead() {
        return exhaust;
    }

    public int[] getExhaustForWrite() {
        if (!exhaustCloned) {
            exhaust = Arrays.copyOf(exhaust, exhaust.length);
            exhaustCloned = true;
        }
        return exhaust;
    }

    public int[] getCounterForRead() {
        return counter;
    }

    public int[] getCounterForWrite() {
        if (!counterCloned) {
            counter = Arrays.copyOf(counter, counter.length);
            counterCloned = true;
        }
        return counter;
    }

    public PlayerReadOnly getPlayeForRead() {
        return player;
    }

    public Player getPlayerForWrite() {
        if (!playerCloned) {
            player = new Player(player);
            playerCloned = true;
        }
        return player;
    }

    public DrawOrderReadOnly getDrawOrderForRead() {
        return drawOrder;
    }

    public DrawOrder getDrawOrderForWrite() {
        if (!drawOrderCloned) {
            drawOrder = new DrawOrder(drawOrder);
            drawOrderCloned = true;
        }
        return drawOrder;
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
    public long getCount(GameState state) {
        return cache.get(state).n;
    }

    static class Node {
        GameState state;
        long n;
        double prev_q_comb;
        double prev_q_win;
        double prev_q_health;
        boolean revisit = false;

        public Node(GameState state) {
            this.state = state;
            n = 1;
        }
    }

    Hashtable<GameState, Node> cache;
    long total_node_n; // actual n, sum of nodes' n in cache
    long total_n; // n called from parent
    double total_q_win;
    double total_q_health;
    double total_q_comb;
    List<GameState> queue = new ArrayList<>();
    boolean queueDone;
    GameState parentState;
    int parentAction;

    // useless
    double v_health = -1;

    public ChanceState() {
        cache = new Hashtable<>();
    }

    public ChanceState(GameState initState, GameState parentState, int action) {
        cache = new Hashtable<>();
        cache.put(initState, new Node(initState));
        total_node_n = 1;
        this.parentState = parentState;
        this.parentAction = action;
        var tmpQueue = new ArrayList<GameState>();
//        for (int i = 1; i < 20; i++) {
//            tmpQueue.add(getNextState(false));
//        }
        queueDone = true;
        total_n = 1;
        queue = tmpQueue;
    }

    public void correctV(GameState state2, double[] v) {
        var node = cache.get(state2);
        if (node.revisit) {
//            var new_total_q_comb = 0.0;
//            var new_total_q_win = 0.0;
//            var new_total_q_health = 0.0;
//            for (Map.Entry<GameState, Node> entry : cache.entrySet()) {
//                new_total_q_comb += entry.getValue().state.total_q_comb / (entry.getValue().state.total_n + 1) * entry.getValue().n;
//                new_total_q_win += entry.getValue().state.total_q_win / (entry.getValue().state.total_n + 1) * entry.getValue().n;
//                new_total_q_health += entry.getValue().state.total_q_health / (entry.getValue().state.total_n + 1) * entry.getValue().n;
//            }
//            new_total_q_comb = new_total_q_comb / total_node_n * total_n;
//            new_total_q_win = new_total_q_win / total_node_n * total_n;
//            new_total_q_health = new_total_q_health / total_node_n * total_n;
//            v[0] = new_total_q_win - total_q_win;
//            v[1] = new_total_q_health - total_q_health;
//            v[2] = new_total_q_comb - total_q_comb;

            var node_cur_q_comb = node.state.total_q_comb / (node.state.total_n + 1);
            var node_cur_q_win = node.state.total_q_win / (node.state.total_n + 1);
            var node_cur_q_health = node.state.total_q_health / (node.state.total_n + 1);
            var prev_q_comb = total_q_comb / (total_n - 1);
            var prev_q_win = total_q_win / (total_n - 1);
            var prev_q_health = total_q_health / (total_n - 1);
            var new_q_comb = prev_q_comb - node.prev_q_comb * node.n / total_node_n + node_cur_q_comb * node.n / total_node_n ;
            var new_q_win = prev_q_win - node.prev_q_win * node.n / total_node_n + node_cur_q_win * node.n / total_node_n ;
            var new_q_health = prev_q_health - node.prev_q_health * node.n / total_node_n +  node_cur_q_health * node.n / total_node_n ;
            node.prev_q_comb = node_cur_q_comb;
            node.prev_q_win = node_cur_q_win;
            node.prev_q_health = node_cur_q_health;
            v[0] = new_q_win * total_n - total_q_win;
            v[1] = new_q_health * total_n - total_q_health;
            v[2] = new_q_comb * total_n - total_q_comb;
//            if (new_q_win * total_n - (total_q_win + v[0]) > 0.00000001) {
//                System.out.println(prev_q_win + "," + total_n + "," + total_q_win / total_n);
//            }
        } else {
//            var new_total_q_comb = 0.0;
//            var new_total_q_win = 0.0;
//            var new_total_q_health = 0.0;
//            var nnn = 0;
//            for (Map.Entry<GameState, Node> entry : cache.entrySet()) {
//                if (entry.getValue().state.policy != null) {
//                    new_total_q_comb += entry.getValue().state.total_q_comb / (entry.getValue().state.total_n + 1) * entry.getValue().n;
//                    new_total_q_win += entry.getValue().state.total_q_win / (entry.getValue().state.total_n + 1) * entry.getValue().n;
//                    new_total_q_health += entry.getValue().state.total_q_health / (entry.getValue().state.total_n + 1) * entry.getValue().n;
//                    nnn += entry.getValue().n;
//                }
//            }
//            if (nnn == 0) {
//                new_total_q_comb = 0;
//                new_total_q_win = 0;
//                new_total_q_health = 0;
//            } else {
//                new_total_q_comb = new_total_q_comb / nnn * total_n;
//                new_total_q_win = new_total_q_win / nnn * total_n;
//                new_total_q_health = new_total_q_health / nnn * total_n;
//            }
//            v[0] = new_total_q_win - total_q_win;
//            v[1] = new_total_q_health - total_q_health;
//            v[2] = new_total_q_comb - total_q_comb;

//            var node_cur_q_comb = node.state.total_q_comb / (node.state.total_n + 1);
//            var node_cur_q_win = node.state.total_q_win / (node.state.total_n + 1);
//            var node_cur_q_health = node.state.total_q_health / (node.state.total_n + 1);
//            var prev_q_comb = total_n == 1 ? 0 : total_q_comb / (total_n - 1);
//            var prev_q_win = total_n == 1 ? 0 : total_q_win / (total_n - 1);
//            var prev_q_health = total_n == 1 ? 0 :total_q_health / (total_n - 1);
//            var new_q_comb = (prev_q_comb * (total_node_n - 1) - node.prev_q_comb * (node.n - 1))  / total_node_n + node_cur_q_comb * node.n / total_node_n;
//            var new_q_win = (prev_q_win * (total_node_n - 1) - node.prev_q_win * (node.n - 1)) / total_node_n + node_cur_q_win * node.n / total_node_n;
//            var new_q_health = (prev_q_health * (total_node_n - 1) - node.prev_q_health * (node.n - 1)) / total_node_n + node_cur_q_health * node.n / total_node_n;
//            node.prev_q_comb = node_cur_q_comb;
//            node.prev_q_win = node_cur_q_win;
//            node.prev_q_health = node_cur_q_health;
//            v[0] = new_q_win * total_n - total_q_win;
//            v[1] = new_q_health * total_n - total_q_health;
//            v[2] = new_q_comb * total_n - total_q_comb;
//            if (new_q_win * total_n - (total_q_win + v[0]) > 0.00000001) {
//                System.out.println(prev_q_win + "," + total_n + "," + total_q_win / total_n);
//            }
        }
        total_q_win += v[0];
        total_q_health += v[1];
        total_q_comb += v[2];
    }

    GameState getNextState(boolean calledFromMCTS) {
        total_n += 1;
        if (queue.size() > 0) {
            return queue.remove(queue.size() - 1);
        }
        if (calledFromMCTS && cache.size() > 10 && false) {
            // instead of generating new nodes, revisit node, need testing
            if (parentState.prop.random.nextFloat() < 1.0 - 1.0 / (cache.size() - 10)) {
                var r = (long) parentState.prop.random.nextInt((int) total_node_n);
                var acc = 0;
                for (Map.Entry<GameState, Node> entry : cache.entrySet()) {
                    acc += entry.getValue().n;
                    if (acc > r) {
                        entry.getValue().revisit = true;
                        return entry.getValue().state;
                    }
                }
                Integer.parseInt(null);
            }
        }
        var state = parentState.clone(false);
        state.doAction(parentAction);
        if (state.actionCtx == GameActionCtx.BEGIN_TURN) {
            state.doAction(0);
        }
        total_node_n += 1;
        var node = cache.get(state);
        if (node != null) {
            node.n += 1;
            node.revisit = false;
            return node.state;
        }
        cache.put(state, new Node(state));
        return state;
    }

    public GameState addGeneratedState(GameState state) {
        var node = cache.get(state);
        if (node == null) {
            cache.put(state, new Node(state));
            total_n += 1;
            total_node_n += 1;
            return state;
        }
        return node.state;
    }

    @Override public String toString() {
        String s = "";
        for (Node node : cache.values()) {
            s += "- (" + node.n + "/" + total_node_n + ") " + node.state + "\n";
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
