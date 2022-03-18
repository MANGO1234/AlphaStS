package com.alphaStS;

import com.alphaStS.utils.DrawOrder;

import java.util.*;

class GameProperties {
    boolean playerStrengthCanChange;
    boolean playerDexterityCanChange;
    boolean playerCanGetVuln;
    boolean playerCanGetWeakened;
    boolean playerCanGetFrailed;
    boolean playerCanGetMetallicize;
    boolean playerThornCanChange;
    boolean enemyCanGetVuln;
    boolean enemyCanGetWeakened;
    long possibleBuffs;
    boolean needDeckOrderMemory;
    boolean selectFromExhaust;
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
    boolean battleTranceExist;
    boolean feelNoPainExist;
    boolean darkEmbraceExist;
    boolean energyRefillCanChange;

    int inputLen;
    List<GameTrigger> startOfTurnTrigger;
    List<GameTrigger> preEndTurnTrigger;
    List<GameTrigger> onPlayerDamageTrigger;
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
    GameProperties prop;
    private boolean[] actionsCache;
    GameActionCtx actionCtx;

    int[] deck;
    int[] hand;
    int[] discard;
    int[] exhaust;
    int[] deckArr;
    int deckArrLen;
    int energy;
    int energyRefill;
    List<Enemy> enemies;
    int enemiesAlive;
    Player player;
    Card previousCard;
    int previousCardIdx;
    short turnNum;
    int playerTurnStartHealth;
    private DrawOrder drawOrder;

    // various other buffs/debuffs
    long buffs;
    short thorn;
    short thornLoseEOT;
    short metallicize;
    short feelNotPain;
    short darkEmbrace;

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
        return energy == gameState.energy && energyRefill == gameState.energyRefill && enemiesAlive == gameState.enemiesAlive && previousCardIdx == gameState.previousCardIdx && buffs == gameState.buffs && metallicize == gameState.metallicize && feelNotPain == gameState.feelNotPain && darkEmbrace == gameState.darkEmbrace && thorn == gameState.thorn && thornLoseEOT == gameState.thornLoseEOT && actionCtx == gameState.actionCtx && Arrays.equals(deck, gameState.deck) && Arrays.equals(hand, gameState.hand) && Arrays.equals(discard, gameState.discard) && Arrays.equals(exhaust, gameState.exhaust) && Objects.equals(enemies, gameState.enemies) && Objects.equals(player, gameState.player) && Objects.equals(previousCard, gameState.previousCard) && Objects.equals(drawOrder, gameState.drawOrder);
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
        prop.random = new Random();
        prop.startOfTurnTrigger = new ArrayList<>();
        prop.preEndTurnTrigger = new ArrayList<>();
        prop.onPlayerDamageTrigger = new ArrayList<>();

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

        prop.playerStrengthCanChange = cards.stream().anyMatch((x) -> x.card().changePlayerStrength);
        prop.playerStrengthCanChange |= enemies.stream().anyMatch((x) -> x.canAffectPlayerStrength);
        prop.playerDexterityCanChange = cards.stream().anyMatch((x) -> x.card().changePlayerDexterity);
        prop.playerDexterityCanChange |= enemies.stream().anyMatch((x) -> x.canAffectPlayerDexterity);
        prop.playerCanGetVuln = enemies.stream().anyMatch((x) -> x.canVulnerable);
        prop.playerCanGetWeakened = enemies.stream().anyMatch((x) -> x.canWeaken);
        prop.playerCanGetFrailed = enemies.stream().anyMatch((x) -> x.canFrail);
        prop.enemyCanGetVuln = cards.stream().anyMatch((x) -> x.card().vulnEnemy);
        prop.enemyCanGetWeakened = cards.stream().anyMatch((x) -> x.card().weakEnemy);
        prop.playerCanGetMetallicize = cards.stream().anyMatch((x) -> x.card().cardName.contains("Metallicize"));
        prop.playerThornCanChange = cards.stream().anyMatch((x) -> x.card().cardName.contains("Flame Barrier"));
        prop.possibleBuffs |= cards.stream().anyMatch((x) -> x.card().cardName.contains("Corruption")) ? PlayerBuffs.CORRUPTION : 0;
        prop.possibleBuffs |= cards.stream().anyMatch((x) -> x.card().cardName.contains("Barricade")) ? PlayerBuffs.BARRICADE : 0;
        prop.possibleBuffs |= relics.stream().anyMatch((x) -> x instanceof Relic.CentennialPuzzle) ? PlayerBuffs.CENTENNIAL_PUZZLE : 0;
        prop.possibleBuffs |= relics.stream().anyMatch((x) -> x instanceof Relic.Calipers) ? PlayerBuffs.CALIPERS : 0;
        prop.needDeckOrderMemory = cards.stream().anyMatch((x) -> x.card().putCardOnTopDeck);
        prop.selectFromExhaust = cards.stream().anyMatch((x) -> x.card().selectFromExhaust);
        prop.battleTranceExist = cards.stream().anyMatch((x) -> x.card().cardName.contains("Battle Trance"));
        prop.feelNoPainExist = cards.stream().anyMatch((x) -> x.card().cardName.contains("Feel No Pain"));
        prop.darkEmbraceExist = cards.stream().anyMatch((x) -> x.card().cardName.contains("Dark Embrace"));
        prop.energyRefillCanChange = cards.stream().anyMatch((x) -> x.card().cardName.contains("Berserk"));

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
        energy = 3;
        energyRefill = 3;
        this.enemies = enemies;
        enemiesAlive = (int) enemies.stream().filter((x) -> x.health > 0).count();
        this.player = player;
        drawOrder = new DrawOrder(10);

        // mcts related fields
        prop.inputLen = getInputLen();
        policy = null;
        q_win = new double[prop.maxNumOfActions];
        q_health = new double[prop.maxNumOfActions];
        q_comb = new double[prop.maxNumOfActions];
        n = new int[prop.maxNumOfActions];
        ns = new State[prop.maxNumOfActions];
        transpositionsPolicyMask = new boolean[prop.maxNumOfActions];
        terminal_action = -100;
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
            if (cards.get(i).card().exhaustWhenPlayed || cards.get(i).card().cardType == Card.POWER) {
                l.add(i);
            } else if (cards.get(i).card().exhaustEndOfTurn && getCardEnergyCost(i) >= 0) {
                l.add(i);
            } else if (cards.get(i).card().exhaustNonAttacks) {
                for (int j = 0; j < cards.size(); j++) {
                    if (cards.get(j).card().cardType != Card.ATTACK) {
                        l.add(j);
                    }
                }
            } else if (cards.get(i).card().exhaustSkill) {
                for (int j = 0; j < cards.size(); j++) {
                    if (cards.get(j).card().cardType == Card.SKILL) {
                        l.add(j);
                    }
                }
            } else if (cards.get(i).card().selectFromDiscard || cards.get(i).card().exhaustCardFromHand) {
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

    private GameState(GameState other) {
        prop = other.prop;

        actionCtx = other.actionCtx;
        deck = Arrays.copyOf(other.deck, other.deck.length);
        hand = Arrays.copyOf(other.hand, other.hand.length);
        discard = Arrays.copyOf(other.discard, other.discard.length);
        exhaust = Arrays.copyOf(other.exhaust, other.exhaust.length);
        deckArr = Arrays.copyOf(other.deckArr, other.deckArr.length);
        deckArrLen = other.deckArrLen;
        energy = other.energy;
        turnNum = other.turnNum;
        playerTurnStartHealth = other.playerTurnStartHealth;
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
        metallicize = other.metallicize;
        feelNotPain = other.feelNotPain;
        darkEmbrace = other.darkEmbrace;

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
        if (player.cannotDrawCard) {
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

    public void gotoActionCtx(GameActionCtx ctx, Card card, int card_idx) {
        switch (ctx) {
        case PLAY_CARD -> {
            previousCard = null;
            previousCardIdx = -1;
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

    private void playCard(int cardIdx, int selectIdx) {
        if (actionCtx == GameActionCtx.PLAY_CARD) {
            hand[cardIdx] -= 1;
            energy -= getCardEnergyCost(cardIdx);
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
                            gotoActionCtx(prop.cardDict[cardIdx].play(this, i), prop.cardDict[cardIdx], cardIdx);
                        }
                    }
                } else if (selectIdx >= 0) {
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
                    possibleChoicesCount += hand[j] > 0 ? 1 : 0;
                    lastIdx = hand[j] > 0 ? j : lastIdx;
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
            if (prop.cardDict[cardIdx].exhaustWhenPlayed) {
                exhaustedCardHandle(cardIdx);
            } else if ((buffs & PlayerBuffs.CORRUPTION) != 0 && prop.cardDict[cardIdx].cardType == Card.SKILL) {
                exhaustedCardHandle(cardIdx);
            } else if (prop.cardDict[cardIdx].cardType != Card.POWER) {
                discard[cardIdx] += 1;
            }
        }
    }

    void startTurn() {
        turnNum++;
        playerTurnStartHealth = player.health;
        for (Enemy enemy : enemies) {
            if (enemy.health > 0) {
                enemy.nextMove(prop.random);
            }
            enemy.startTurn();
        }
        draw(5);
        for (GameTrigger gameTrigger : prop.startOfTurnTrigger) {
            gameTrigger.act(this);
        }
    }

    private void endTurn() {
        for (GameTrigger gameTrigger : prop.preEndTurnTrigger) {
            gameTrigger.act(this);
        }
        if (metallicize > 0) player.gainBlockNotFromCardPlay(metallicize);
        for (int i = 0; i < hand.length; i++) {
            if (hand[i] > 0) {
                if (!prop.cardDict[i].exhaustEndOfTurn) {
                    for (int j = 0; j < hand[i]; j++) {
                        prop.cardDict[i].onDiscard(this);
                    }
                    discard[i] += hand[i];
                } else {
                    for (int count = 0; count < hand[i]; count++) {
                        exhaustedCardHandle(i);
                    }
                }
            }
            hand[i] = 0;
        }
        for (Enemy enemy : enemies) {
            if (enemy.health > 0) {
                enemy.doMove(this);
                enemy.endTurn();
            }
        }
        thorn -= thornLoseEOT;
        thornLoseEOT = 0;
        player.endTurn(this);
        energy = energyRefill;
    }

    void doAction(int actionIdx) {
        GameAction action = prop.actionsByCtx[actionCtx.ordinal()][actionIdx];
        if (action.type() == GameActionType.START_GAME) {
            for (Enemy enemy : enemies) {
                enemy.startOfGameSetup(prop.random);
            }
            startTurn();
            gotoActionCtx(GameActionCtx.PLAY_CARD, null, -1);
        } else if (action.type() == GameActionType.END_TURN) {
            endTurn();
//            startTurn();
            gotoActionCtx(GameActionCtx.BEGIN_TURN, null, -1);
        } else if (action.type() == GameActionType.PLAY_CARD) {
            playCard(action.cardIdx(), -1);
        } else if (action.type() == GameActionType.SELECT_ENEMY) {
            playCard(previousCardIdx, action.enemyIdx());
        } else if (action.type() == GameActionType.SELECT_CARD_HAND) {
            playCard(previousCardIdx, action.cardIdx());
        } else if (action.type() == GameActionType.SELECT_CARD_DISCARD) {
            playCard(previousCardIdx, action.cardIdx());
        } else if (action.type() == GameActionType.SELECT_CARD_EXHAUST) {
            playCard(previousCardIdx, action.cardIdx());
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
            return hand[a[action].cardIdx()] > 0;
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
        if (player.health <= 0 || turnNum > 30) {
            out[0] = 0;
            out[1] = 0;
            out[2] = 0;
            return;
        } else {
            if (enemies.stream().allMatch((x) -> x.health <= 0)) {
                out[0] = 1;
                out[1] = ((double) player.health) / player.maxHealth;
                out[2] = calc_q(out[0], out[1]);
                return;
            }
        }
        out[0] = v_win;
        out[1] = v_health;
        out[2] = calc_q(out[0], out[1]);
    }

    int isTerminal() {
        if (player.health <= 0 || turnNum > 30) {
            return -1;
        } else {
            return enemies.stream().allMatch((x) -> x.health <= 0) ? 1 : 0;
        }
    }

    private String formatFloat(double f) {
        if (f == 0) {
            return "0";
        } else if (f < 0.001) {
            return String.format("%6.3e", f).trim();
        } else {
            return String.format("%6.3f", f).trim();
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
        str.append(", energy=").append(energy).append(", ctx=").append(actionCtx).append(", ").append(player);
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
        if (metallicize > 0) {
            str.append(", metal=").append(metallicize);
        }
        if (feelNotPain > 0) {
            str.append(", fnp=").append(feelNotPain);
        }
        if (darkEmbrace > 0) {
            str.append(", darkEmbr=").append(darkEmbrace);
        }
        if (buffs > 0) {
            str.append(", buffs=[");
            first = true;
            if ((buffs & PlayerBuffs.CORRUPTION) != 0) {
                str.append(first ? "" : ", ").append("Corruption");
                first = false;
            }
            if ((buffs & PlayerBuffs.CENTENNIAL_PUZZLE) != 0) {
                str.append(first ? "" : ", ").append("Centennial Puzzle");
                first = false;
            }
            if ((buffs & PlayerBuffs.BARRICADE) != 0) {
                str.append(first ? "" : ", ").append("Barricade");
                first = false;
            }
            str.append("]");
        }
        str.append(", v=(").append(formatFloat(v_win)).append(", ").append(formatFloat(v_health)).append("/").append(formatFloat(v_health * player.maxHealth)).append(")");
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
        Output output = model.eval(this);
        this.policy = output.policy();
        this.v_health = Math.min(output.v_health(), this.player.health / (float) this.player.maxHealth);
        this.v_win = output.v_win();
    }

    private int getInputLen() {
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
        if (prop.playerStrengthCanChange) {
            inputLen += 1; // player strength
        }
        if (prop.playerDexterityCanChange) {
            inputLen += 1; // player dexterity
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
        if (prop.playerCanGetMetallicize) {
            inputLen += 1; // player metallicize
        }
        if (prop.playerThornCanChange) {
            inputLen += 1; // player thorn
        }
        if (prop.battleTranceExist) {
            inputLen += 1; // battle trance
        }
        if (prop.feelNoPainExist) {
            inputLen += 1; // feel no pain
        }
        if (prop.darkEmbraceExist) {
            inputLen += 1; // dark embrace
        }
        if (prop.energyRefillCanChange) {
            inputLen += 1; // berserk
        }
        if ((prop.possibleBuffs & PlayerBuffs.BARRICADE) != 0) {
            inputLen += 1; // barricade in deck
        }
        if ((prop.possibleBuffs & PlayerBuffs.CORRUPTION) != 0) {
            inputLen += 1; // corruption in deck
        }
        if ((prop.possibleBuffs & PlayerBuffs.CALIPERS) != 0) {
            inputLen += 1; // has runic pyramid
        }
        if ((prop.possibleBuffs & PlayerBuffs.CENTENNIAL_PUZZLE) != 0) {
            inputLen += 1; // has runic pyramid
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
            inputLen += 1; // enemy move
            if (enemy.moveHistory != null) {
                inputLen += enemy.moveHistory.length;
            }
            if (enemy instanceof Enemy.RedLouse || enemy instanceof Enemy.GreenLouse) {
                inputLen += 1;
            } else if (enemy instanceof Enemy.TheGuardian guardian) {
                inputLen += 2;
            }
        }
        return inputLen;
    }

    public float[] getInput() {
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
        if (prop.playerCanGetMetallicize) {
            x[idx++] = metallicize / 10.0f;
        }
        if (prop.playerThornCanChange) {
            x[idx++] = thorn / 10.0f;
        }
        if (prop.battleTranceExist) {
            x[idx++] = player.cannotDrawCard ? 0.5f : -0.5f;
        }
        if (prop.feelNoPainExist) {
            x[idx++] = (feelNotPain - 3) / 3.0f;
        }
        if (prop.darkEmbraceExist) {
            x[idx++] = darkEmbrace / 2.0f;
        }
        if (prop.energyRefillCanChange) {
            x[idx++] = (energyRefill - 5) / 2f;
        }
        if ((prop.possibleBuffs & PlayerBuffs.BARRICADE) != 0) {
            x[idx++] = (buffs & PlayerBuffs.BARRICADE) != 0 ? 0.5f : -0.5f;
        }
        if ((prop.possibleBuffs & PlayerBuffs.CORRUPTION) != 0) {
            x[idx++] = (buffs & PlayerBuffs.CORRUPTION) != 0 ? 0.5f : -0.5f;
        }
        if ((prop.possibleBuffs & PlayerBuffs.CALIPERS) != 0) {
            x[idx++] = (buffs & PlayerBuffs.CALIPERS) != 0 ? 0.5f : -0.5f;
        }
        if ((prop.possibleBuffs & PlayerBuffs.CENTENNIAL_PUZZLE) != 0) {
            x[idx++] = (buffs & PlayerBuffs.CENTENNIAL_PUZZLE) != 0 ? 0.5f : -0.5f;
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

    void exhaustCardFromHand(int cardIdx) {
        assert hand[cardIdx] > 0;
        hand[cardIdx] -= 1;
        exhaustedCardHandle(cardIdx);
    }

    private void exhaustedCardHandle(int cardIdx) {
        prop.cardDict[cardIdx].onExhaust(this);
        exhaust[cardIdx] += 1;
        if (feelNotPain > 0) {
            player.gainBlockNotFromCardPlay(feelNotPain);
        }
        if (darkEmbrace > 0) {
            draw(darkEmbrace);
        }
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

    public void addStartOfTurnTrigger(GameTrigger gameTrigger) {
        prop.startOfTurnTrigger.add(gameTrigger);
    }

    public void addPreEndOfTurnTrigger(GameTrigger gameTrigger) {
        prop.preEndTurnTrigger.add(gameTrigger);
    }

    public void addOnDamageTrigger(GameTrigger gameTrigger) {
        prop.onPlayerDamageTrigger.add(gameTrigger);
    }

    public void clearNextStates() { // oom during training due to holding too many states
        for (int i = 0; i < ns.length; i++) {
            ns[i] = null;
        }
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
        drawOrder.pushOnTop(idx);
    }

    public void enemyDoDamageToPlayer(Enemy enemy, int dmg, int times) {
        int move = enemy.move;
        for (int i = 0; i < times; i++) {
            if (enemy.health <= 0 || enemy.move != move) {
                return;
            }
            dmg += enemy.strength;
            if (enemy.weak > 0) {
                dmg = dmg * 3 / 4;
            }
            player.damage(dmg);
            if (thorn > 0) {
                enemy.nonAttackDamage(thorn, false, this);
            }
            if (dmg > 0) {
                for (GameTrigger trigger : prop.onPlayerDamageTrigger) {
                    trigger.act(this);
                }
            }
        }
    }

    public int enemyCalcDamageToPlayer(Enemy enemy, int d) {
        d += enemy.strength;
        if (enemy.weak > 0) {
            d = d * 3 / 4;
        }
        if (player.vulnerable > 0) {
            d = d + d / 2;
        }
        return d;
    }

    public void doNonAttackDamageToPlayer(int dmg, boolean blockable) {
        player.nonAttackDamage(dmg, blockable);
        if (dmg > 0) {
            for (GameTrigger trigger : prop.onPlayerDamageTrigger) {
                trigger.act(this);
            }
        }
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

        public Node(GameState state) {
            this.state = state;
            n = 1;
        }
    }

    Hashtable<GameState, Node> cache;
    long total_n;
    double total_q_win;
    double total_q_health;
    double total_q_comb;
    double v_health = -1;

    public ChanceState() {
        cache = new Hashtable<>();
    }

    public ChanceState(GameState initState) {
        cache = new Hashtable<>();
        cache.put(initState, new Node(initState));
        total_n = 1;
    }

    GameState getNextState(GameState parentState, int action) {
        var state = parentState.clone(false);
        state.doAction(action);
//        if (parentState.actionCtx == GameActionCtx.PLAY_CARD && parentState.getActionString(action).contains("End")) {
//            System.out.println("!!!" + state.toStringReadable());
//        }
        if (state.actionCtx == GameActionCtx.BEGIN_TURN) {
            state.doAction(0);
        }
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
