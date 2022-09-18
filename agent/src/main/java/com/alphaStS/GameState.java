package com.alphaStS;

import com.alphaStS.Action.GameEnvironmentAction;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyList;
import com.alphaStS.enemy.EnemyListReadOnly;
import com.alphaStS.enemy.EnemyReadOnly;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.enums.OrbType;
import com.alphaStS.player.Player;
import com.alphaStS.player.PlayerReadOnly;
import com.alphaStS.utils.*;

import java.util.*;

import static com.alphaStS.utils.Utils.formatFloat;

abstract class GameEventHandler implements Comparable<GameEventHandler> {
    private int priority;

    protected GameEventHandler(int priority) {
        this.priority = priority;
    }

    protected GameEventHandler() {}

    abstract void handle(GameState state);

    @Override public int compareTo(GameEventHandler other) {
        return Integer.compare(priority, other.priority);
    }
}

enum GameActionCtx {
    BEGIN_BATTLE,
    PLAY_CARD,
    SELECT_ENEMY,
    SELECT_CARD_DISCARD,
    SELECT_CARD_HAND,
    SELECT_CARD_EXHAUST,
    SELECT_CARD_1_OUT_OF_3,
    SELECT_SCENARIO,
    BEGIN_TURN,
}

enum GameActionType {
    BEGIN_BATTLE,
    PLAY_CARD,
    SELECT_ENEMY,
    SELECT_CARD_DISCARD,
    SELECT_CARD_HAND,
    SELECT_CARD_EXHAUST,
    SELECT_CARD_1_OUT_OF_3,
    END_TURN,
    USE_POTION,
    SELECT_SCENARIO,
    BEGIN_TURN,
}

record GameAction(GameActionType type, int idx) { // idx is either cardIdx, enemyIdx, potionIdx, etc.
}

public class GameState implements State {
    public static final int HAND_LIMIT = 10;
    private static final int MAX_AGENT_DECK_ORDER_MEMORY = 1;
    public static final boolean COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION = true;
    public static final int V_COMB_IDX = 0;
    public static final int V_WIN_IDX = 1;
    public static final int V_HEALTH_IDX = 2;
    public static final int V_OTHER_IDX_START = 3;

    public boolean isStochastic;
    StringBuilder stateDesc;
    public GameProperties prop;
    GameActionCtx actionCtx;

    byte[] deck;
    byte[] hand;
    byte[] discard;
    private boolean exhaustCloned;
    private byte[] exhaust;
    short[] deckArr;
    int deckArrLen;
    private boolean enemiesCloned;
    private EnemyList enemies;
    public int enemiesAlive;
    private boolean playerCloned;
    private Player player;
    private boolean drawOrderCloned;
    private DrawOrder drawOrder;
    private boolean counterCloned;
    private int[] counter;
    int select1OutOf3CardsIdxes; // 3 bytes inside an int
    short[] potionsState;
    public boolean searchRandomGenCloned;
    public RandomGen searchRandomGen;

    CircularArray<GameEnvironmentAction> gameActionDeque;
    int energy;
    int energyRefill;
    GameAction currentAction;
    public short turnNum;
    int playerTurnStartMaxPossibleHealth;
    byte playerTurnStartPotionCount;

    // various other buffs/debuffs
    public long buffs;

    // Defect specific
    private short[] orbs;
    private short focus;

    // search related fields
    private int[] legalActions;
    double v_win; // if terminal, 1.0 or -1.0, else from NN
    double v_health; // if terminal, player_health/player_max_health, else from NN
    double varianceM;
    double varianceS;
    double[] v_other; // if terminal, player_health/player_max_health, else from NN
    double[] q_comb; // total q value propagated from each child
    double[] q_win; // total v_win value propagated from each child
    double[] q_health; // total v_health value propagated from each child
    double[][] q_other; // total q value propagated from each child
    double total_q_comb; // sum of q_win array
    double total_q_win; // sum of q_win array
    double total_q_health; // sum of q_health _array
    double[] total_q_other; // sum of q_other array
    int[] n; // visit count for each child
    State[] ns; // the state object for each child (either GameState or ChanceState)
    int total_n; // sum of n array
    float[] policy; // policy from NN
    float[] policyMod; // used in training (with e.g. Dirichlet noise applied or futile pruning applied)
    Map<GameState, State> transpositions; // detect transposition within a "deterministic turn" (i.e. no stochastic transition occurred like drawing)
    Map<GameState, List<Tuple<GameState, Integer>>> transpositionsParent;
    int terminal_action; // detected a win from child, no need to waste more time search
    SearchFrontier searchFrontier;

    // Solver only
    BigRational e_health;
    BigRational e_win;

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GameState gameState = (GameState) o;
        return energy == gameState.energy && energyRefill == gameState.energyRefill && enemiesAlive == gameState.enemiesAlive && currentAction == gameState.currentAction && buffs == gameState.buffs && select1OutOf3CardsIdxes == gameState.select1OutOf3CardsIdxes && Arrays.equals(counter, gameState.counter) && actionCtx == gameState.actionCtx && Arrays.equals(deck, gameState.deck) && Arrays.equals(hand, gameState.hand) && Arrays.equals(discard, gameState.discard) && Arrays.equals(exhaust, gameState.exhaust) && Objects.equals(enemies, gameState.enemies) && Objects.equals(player, gameState.player) && Objects.equals(drawOrder, gameState.drawOrder) && Arrays.equals(potionsState, gameState.potionsState) && focus == gameState.focus && Arrays.equals(orbs, gameState.orbs) && ((gameActionDeque == null && gameState.gameActionDeque == null) || (gameActionDeque == null && gameState.gameActionDeque.size() == 0) || (gameActionDeque.size == 0 && gameState.gameActionDeque == null) || gameActionDeque.equals(gameState.gameActionDeque));
    }

    @Override public int hashCode() {
        // actionCtx, energy, energyRefill, hand, enemies health, previousCardIdx, drawOrder, buffs should cover most
        int result = Objects.hash(actionCtx, energy, energyRefill, currentAction, drawOrder, buffs);
        for (var enemy : enemies) {
            result = 31 * result + enemy.getHealth();
        }
        result = 31 * result + player.getHealth();
        result = 31 * result + Arrays.hashCode(hand);
        result = 31 * result + Arrays.hashCode(deck);
        result = 31 * result + Arrays.hashCode(discard);
        result = 31 * result + Arrays.hashCode(potionsState);
        return result;
    }

    public GameState(GameStateBuilder builder) {
        List<Enemy> enemiesArg = builder.getEnemies();
        Player player = builder.getPlayer();
        List<CardCount> cards = builder.getCards();
        List<Relic> relics = builder.getRelics();
        List<Potion> potions = builder.getPotions();
        GameStateRandomization randomization = builder.getRandomization();
        GameStateRandomization preBattleRandomization = builder.getPreBattleRandomization();
        GameStateRandomization preBattleScenarios = builder.getPreBattleScenarios();
        // game properties (shared)
        prop = new GameProperties();
        prop.randomization = randomization;
        prop.preBattleRandomization = preBattleRandomization;
        prop.potions = potions;
        prop.enemiesReordering = builder.getEnemyReordering().size() == 0 ? null : builder.getEnemyReordering();
        if (builder.getCharacter() == CharacterEnum.DEFECT) {
            prop.maxNumOfOrbs = 3;
            orbs = new short[3];
        }

        cards = collectAllPossibleCards(cards, enemiesArg, relics, potions);
        cards.sort((o1, o2) -> {
            if (!(o1.card() instanceof Card.CardTmpChangeCost) && o2.card() instanceof Card.CardTmpChangeCost) {
                return -1;
            } else if (o1.card() instanceof Card.CardTmpChangeCost && !(o2.card() instanceof Card.CardTmpChangeCost)) {
                return 1;
            } else {
                return o1.card().cardName.compareTo(o2.card().cardName);
            }
        });
        prop.cardDict = new Card[cards.size()];
        for (int i = 0; i < cards.size(); i++) {
            prop.cardDict[i] = cards.get(i).card();
        }
        prop.realCardsLen = (int) cards.stream().takeWhile((card) -> !(card.card() instanceof Card.CardTmpChangeCost)).count();
        if (prop.realCardsLen != cards.size()) {
            prop.tmpCostCardIdxes = new int[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                if (prop.cardDict[i] instanceof Card.CardTmpChangeCost) {
                    prop.tmpCostCardIdxes[i] = prop.findCardIndex(((Card.CardTmpChangeCost) prop.cardDict[i]).card);
                } else {
                    prop.tmpCostCardIdxes[i] = prop.findCardIndex(new Card.CardTmpChangeCost(prop.cardDict[i], 0));
                }
            }
        }

        prop.preBattleScenarios = preBattleScenarios;
        if (prop.preBattleScenarios != null) {
            prop.preBattleGameScenariosList = prop.preBattleScenarios.listRandomizations().entrySet()
                    .stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).toList();
        }

        // start of game actions
        prop.actionsByCtx = new GameAction[GameActionCtx.values().length][];
        prop.actionsByCtx[GameActionCtx.BEGIN_BATTLE.ordinal()] = new GameAction[] { new GameAction(GameActionType.BEGIN_BATTLE, 0) };
        prop.actionsByCtx[GameActionCtx.BEGIN_TURN.ordinal()] = new GameAction[] { new GameAction(GameActionType.BEGIN_TURN, 0) };

        // play card actions
        var l = cards.size() + 1 + potions.size();
        prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()] = new GameAction[l];
        for (int i = 0; i < cards.size(); i++) {
            prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][i] = new GameAction(GameActionType.PLAY_CARD, i);
        }
        for (int i = 0; i < potions.size(); i++) {
            prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cards.size() + i] = new GameAction(GameActionType.USE_POTION, i);
        }
        prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][l - 1] = new GameAction(GameActionType.END_TURN, 0);

        // select enemy actions
        if (enemiesArg.size() > 1) {
            prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] = new GameAction[enemiesArg.size()];
            for (int i = 0; i < enemiesArg.size(); i++) {
                prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()][i] = new GameAction(GameActionType.SELECT_ENEMY, i);
            }
        }

        // select hand actions
        if (cards.stream().anyMatch((x) -> x.card().selectFromHand)) {
            prop.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()] = new GameAction[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()][i] = new GameAction(GameActionType.SELECT_CARD_HAND, i);
            }
        }

        // select from discard actions
        if (cards.stream().anyMatch((x) -> x.card().selectFromDiscard) || potions.stream().anyMatch((x) -> x.selectFromDiscard)) {
            prop.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()] = new GameAction[prop.realCardsLen];
            for (int i = 0; i < prop.realCardsLen; i++) {
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()][i] = new GameAction(GameActionType.SELECT_CARD_DISCARD, i);
            }
        }

        // select from exhaust actions
        if (cards.stream().anyMatch((x) -> x.card().selectFromExhaust)) {
            prop.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()] = new GameAction[prop.realCardsLen];
            for (int i = 0; i < prop.realCardsLen; i++) {
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()][i] = new GameAction(GameActionType.SELECT_CARD_EXHAUST, i);
            }
        }

        // select from select 1 out of 3 cards action
        if (potions.stream().anyMatch((x) -> x.selectCard1OutOf3)) {
            prop.actionsByCtx[GameActionCtx.SELECT_CARD_1_OUT_OF_3.ordinal()] = new GameAction[cards.size()];
            for (int i = 0; i < cards.size(); i++) {
                prop.actionsByCtx[GameActionCtx.SELECT_CARD_1_OUT_OF_3.ordinal()][i] = new GameAction(GameActionType.SELECT_CARD_1_OUT_OF_3, i);
            }
        }

        // select pre battle scenario actions
        if (prop.preBattleScenarios != null) {
            prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()] = new GameAction[prop.preBattleGameScenariosList.size()];
            for (int i = 0; i < prop.preBattleGameScenariosList.size(); i++) {
                prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()][i] = new GameAction(GameActionType.SELECT_SCENARIO, i);
            }
        }

        for (int i = 0; i < prop.actionsByCtx.length; i++) {
            if (prop.actionsByCtx[i] != null) {
                prop.maxNumOfActions = Math.max(prop.maxNumOfActions, prop.actionsByCtx[i].length);
                if (i == GameActionCtx.PLAY_CARD.ordinal() || i == GameActionCtx.SELECT_ENEMY.ordinal() || i == GameActionCtx.SELECT_SCENARIO.ordinal()) {
                    prop.totalNumOfActions += prop.actionsByCtx[i].length;
                }
            }
        }

        // game state
        if (prop.preBattleScenarios == null) {
            actionCtx = GameActionCtx.BEGIN_BATTLE;
        } else {
            actionCtx = GameActionCtx.SELECT_SCENARIO;
        }
        deck = new byte[prop.realCardsLen];
        hand = new byte[cards.size()];
        discard = new byte[prop.realCardsLen];
        exhaust = new byte[prop.realCardsLen];
        for (int i = 0; i < deck.length; i++) {
            deck[i] = (byte) cards.get(i).count();
            deckArrLen += deck[i];
        }
        deckArr = new short[deckArrLen + 60];
        int idx = 0;
        for (short i = 0; i < cards.size(); i++) {
            for (int j = 0; j < cards.get(i).count(); j++) {
                deckArr[idx++] = i;
            }
        }
        energyRefill = 3;
        this.enemies = new EnemyList(enemiesArg);
        enemiesAlive = (int) enemiesArg.stream().filter(EnemyReadOnly::isAlive).count();
        this.player = player;
        drawOrder = new DrawOrder(10);
        for (int i = 0; i < deck.length; i++) { // todo: edge case more innate than first turn draw
            if (deck[i] > 0 && prop.cardDict[i].innate) {
                drawOrder.pushOnTop(i);
            }
        }
        if (potions.size() > 0) {
            potionsState = new short[potions.size() * 3];
            for (int i = 0; i < prop.potions.size(); i++) {
                potionsState[i * 3] = 0;
                potionsState[i * 3 + 1] = 100;
                potionsState[i * 3 + 2] = 0;
            }
        }

        List<Integer> strikeIdxes = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).card().cardName.equals("Burn")) {
                prop.burnCardIdx = i;
            } else if (cards.get(i).card().cardName.equals("Burn+")) {
                prop.burnPCardIdx = i;
            } else if (cards.get(i).card().cardName.equals("Dazed")) {
                prop.dazedCardIdx = i;
            } if (cards.get(i).card().cardName.equals("Slime")) {
                prop.slimeCardIdx = i;
            } else if (cards.get(i).card().cardName.equals("Wound")) {
                prop.woundCardIdx = i;
            } else if (cards.get(i).card().cardName.equals("Void")) {
                prop.voidCardIdx = i;
            } else if (cards.get(i).card().cardName.equals("Anger")) {
                prop.angerCardIdx = i;
            } else if (cards.get(i).card().cardName.equals("Anger+")) {
                prop.angerPCardIdx = i;
            } else if (cards.get(i).card().cardName.contains("Strike")) {
                strikeIdxes.add(i);
            } else if (cards.get(i).card().cardName.equals("Echo Form")) {
                prop.echoFormCardIdx = i;
            } else if (cards.get(i).card().cardName.equals("Echo Form+")) {
                prop.echoFormPCardIdx = i;
            }
        }
        prop.strikeCardIdxes = strikeIdxes.stream().mapToInt(Integer::intValue).toArray();
        prop.healCardsIdxes = findCardThatCanHealIdxes(cards, relics);
        prop.upgradeIdxes = findUpgradeIdxes(cards, relics);
        prop.discardIdxes = findDiscardToKeepTrackOf(cards, enemiesArg);
        prop.discardReverseIdxes = new int[prop.realCardsLen];
        for (int i = 0; i < prop.discardIdxes.length; i++) {
            prop.discardReverseIdxes[prop.discardIdxes[i]] = i;
        }
        prop.select1OutOf3CardsIdxes = findSelect1OutOf3CardsToKeepTrackOf(cards, potions);
        prop.select1OutOf3CardsReverseIdxes = new int[prop.cardDict.length];
        Arrays.fill(prop.select1OutOf3CardsReverseIdxes, -1);
        for (int i = 0; i < prop.select1OutOf3CardsIdxes.length; i++) {
            prop.select1OutOf3CardsReverseIdxes[prop.select1OutOf3CardsIdxes[i]] = i;
        }
        for (Relic relic : relics) {
            relic.startOfGameSetup(this);
        }
        for (Card card : prop.cardDict) {
            card.startOfGameSetup(this);
        }
        for (int i = 0; i < getEnemiesForRead().size(); i++) { // need to use i because setup can modify other enemies
            getEnemiesForRead().get(i).gamePropertiesSetup(this);
        }
        for (int i = 0; i < potions.size(); i++) { // need to use i because setup can modify other enemies
            potions.get(i).gamePropertiesSetup(this);
        }
        prop.compileCounterInfo();
        if (prop.counterNames.length > 0) {
            counter = new int[prop.counterNames.length];
        }
        Collections.sort(prop.startOfBattleHandlers);
        Collections.sort(prop.endOfBattleHandlers);
        Collections.sort(prop.startOfTurnHandlers);
        Collections.sort(prop.preEndTurnHandlers);
        Collections.sort(prop.onBlockHandlers);
        Collections.sort(prop.onExhaustHandlers);
        Collections.sort(prop.onBlockHandlers);
        Collections.sort(prop.onCardPlayedHandlers);
        Collections.sort(prop.onCardDrawnHandlers);

        prop.playerArtifactCanChange = getPlayeForRead().getArtifact() > 0;
        prop.playerArtifactCanChange |= potions.stream().anyMatch((x) -> x.changePlayerArtifact);
        prop.playerStrengthCanChange = cards.stream().anyMatch((x) -> x.card().changePlayerStrength);
        prop.playerStrengthCanChange |= enemiesArg.stream().anyMatch((x) -> x.property.changePlayerStrength);
        prop.playerStrengthCanChange |= relics.stream().anyMatch((x) -> x.changePlayerStrength);
        prop.playerStrengthCanChange |= potions.stream().anyMatch((x) -> x.changePlayerStrength);
        prop.playerDexterityCanChange = cards.stream().anyMatch((x) -> x.card().changePlayerDexterity);
        prop.playerDexterityCanChange |= enemiesArg.stream().anyMatch((x) -> x.property.changePlayerDexterity);
        prop.playerDexterityCanChange |= relics.stream().anyMatch((x) -> x.changePlayerDexterity);
        prop.playerDexterityCanChange |= potions.stream().anyMatch((x) -> x.changePlayerDexterity);
        prop.playerStrengthEotCanChange = cards.stream().anyMatch((x) -> x.card().changePlayerStrengthEot);
        prop.playerDexterityEotCanChange = potions.stream().anyMatch((x) -> x.changePlayerDexterityEot);
        prop.playerFocusCanChange = cards.stream().anyMatch((x) -> x.card().changePlayerFocus);
        prop.playerCanGetVuln = enemiesArg.stream().anyMatch((x) -> x.property.canVulnerable);
        prop.playerCanGetWeakened = enemiesArg.stream().anyMatch((x) -> x.property.canWeaken);
        prop.playerCanGetFrailed = enemiesArg.stream().anyMatch((x) -> x.property.canFrail);
        prop.playerCanGetEntangled = enemiesArg.stream().anyMatch((x) -> x.property.canEntangle);
        prop.enemyCanGetVuln = cards.stream().anyMatch((x) -> x.card().vulnEnemy);
        prop.enemyCanGetVuln |= relics.stream().anyMatch((x) -> x.vulnEnemy);
        prop.enemyCanGetVuln |= potions.stream().anyMatch((x) -> x.vulnEnemy);
        prop.enemyCanGetWeakened = cards.stream().anyMatch((x) -> x.card().weakEnemy);
        prop.enemyCanGetWeakened |= relics.stream().anyMatch((x) -> x.weakEnemy);
        prop.enemyCanGetWeakened |= potions.stream().anyMatch((x) -> x.weakEnemy);
        prop.enemyStrengthEotCanChange = cards.stream().anyMatch((x) -> x.card().affectEnemyStrengthEot);
        prop.enemyStrengthCanChange = cards.stream().anyMatch((x) -> x.card().affectEnemyStrength);
        prop.possibleBuffs |= cards.stream().anyMatch((x) -> x.card().cardName.contains("Corruption")) ? PlayerBuff.CORRUPTION.mask() : 0;
        prop.possibleBuffs |= cards.stream().anyMatch((x) -> x.card().cardName.contains("Barricade")) ? PlayerBuff.BARRICADE.mask() : 0;
        prop.possibleBuffs |= relics.stream().anyMatch((x) -> x instanceof Relic.Akabeko) ? PlayerBuff.AKABEKO.mask() : 0;
        prop.possibleBuffs |= relics.stream().anyMatch((x) -> x instanceof Relic.CentennialPuzzle) ? PlayerBuff.CENTENNIAL_PUZZLE.mask() : 0;
        prop.needDeckOrderMemory = cards.stream().anyMatch((x) -> x.card().putCardOnTopDeck);
        prop.selectFromExhaust = cards.stream().anyMatch((x) -> x.card().selectFromExhaust);
        prop.battleTranceExist = cards.stream().anyMatch((x) -> x.card().cardName.contains("Battle Trance"));
        prop.energyRefillCanChange = cards.stream().anyMatch((x) -> x.card().cardName.contains("Berserk"));
        prop.originalEnemies = new EnemyList(enemies);
        for (int i = 0; i < prop.originalEnemies.size(); i++) {
            prop.originalEnemies.getForWrite(i);
        }
        prop.compilerExtraTrainingTarget();
        prop.inputLen = getNNInputLen();

        // mcts related fields
        terminal_action = -100;
        transpositions = new HashMap<>();
        if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH) transpositionsParent = new HashMap<>();
    }

    private int[] findCardThatCanHealIdxes(List<CardCount> cards, List<Relic> relics) {
        // todo
        long c = cards.stream().filter((x) -> x.card().healPlayer).count();
        if (c == 0) {
            return null;
        }
        int[] r = new int[(int) c];
        int idx = 0;
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).card().healPlayer) {
                r[idx++] = i;
            }
        }
        return r;
    }

    private int[] findUpgradeIdxes(List<CardCount> cards, List<Relic> relics) {
        if (cards.stream().noneMatch((x) -> x.card().cardName.contains("Armanent") || x.card().cardName.contains("Apotheosis")) &&
            relics.stream().noneMatch((x) -> x instanceof Relic.WarpedTongs)) {
            return null;
        }
        int[] r = new int[cards.size()];
        Arrays.fill(r, -1);
        for (int i = 0; i < r.length; i++) {
            var upgrade = CardUpgrade.map.get(cards.get(i).card());
            if (upgrade != null) {
                r[i] = prop.findCardIndex(upgrade);
            }
        }
        return r;
    }

    private int[] findDiscardToKeepTrackOf(List<CardCount> cards, List<Enemy> enemies) {
        Set<Integer> l = new HashSet<>();
        for (Enemy enemy : enemies) {
            if (enemy.property.canDaze) {
                l.add(prop.findCardIndex(new Card.Dazed()));
            }
            l.addAll(enemy.getPossibleGeneratedCards().stream().map(prop::findCardIndex).toList());
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
        if (prop.preBattleScenarios != null) {
            var clone = this.clone(false);
            for (var r : prop.preBattleScenarios.listRandomizations().keySet()) {
                prop.preBattleScenarios.randomize(clone, r);
                for (int i = 0; i < deck.length; i++) {
                    if (clone.deck[i] != deck[i]) {
                        l.add(i);
                    }
                }
            }
        }
        if (prop.randomization != null) {
            var clone = this.clone(false);
            for (var r : prop.randomization.listRandomizations().keySet()) {
                prop.randomization.randomize(clone, r);
                for (int i = 0; i < deck.length; i++) {
                    if (clone.deck[i] != deck[i]) {
                        l.add(i);
                    }
                }
            }
        }
        return l.stream().filter((x) -> !(prop.cardDict[x] instanceof Card.CardTmpChangeCost)).sorted().mapToInt(Integer::intValue).toArray();
    }

    private int[] findSelect1OutOf3CardsToKeepTrackOf(List<CardCount> cards, List<Potion> potions) {
        var c = cards.stream().map(CardCount::card).toList();
        List<Integer> idxes = new ArrayList<>();
        for (int i = 0; i < potions.size(); i++) {
            potions.get(i).getPossibleSelect3OutOf1Cards(c).forEach((card) -> idxes.add(prop.findCardIndex(card)));
        }
        return idxes.stream().mapToInt(Integer::intValue).sorted().toArray();
    }

    private List<CardCount> collectAllPossibleCards(List<CardCount> cards, List<Enemy> enemies, List<Relic> relics, List<Potion> potions) {
        var set = new HashSet<>(cards);
        if (enemies.stream().anyMatch((x) -> x.property.canSlime)) {
            set.add(new CardCount(new Card.Slime(), 0));
        }
        if (enemies.stream().anyMatch((x) -> x.property.canDaze)) {
            set.add(new CardCount(new Card.Dazed(), 0));
        }
        for (Enemy enemy : enemies) {
            set.addAll(enemy.getPossibleGeneratedCards().stream().map((c) -> new CardCount(c, 0)).toList());
        }
        do {
            var newSet = new HashSet<>(set);
            for (CardCount c : set) {
                for (Card possibleCard : c.card().getPossibleGeneratedCards(set.stream().map(CardCount::card).toList())) {
                    newSet.add(new CardCount(possibleCard, 0));
                    if (possibleCard instanceof Card.CardTmpChangeCost tmp) {
                        newSet.add(new CardCount(tmp.card, 0));
                    }
                }
            }
            for (Relic relic : relics) {
                for (Card possibleCard : relic.getPossibleGeneratedCards(set.stream().map(CardCount::card).toList())) {
                    newSet.add(new CardCount(possibleCard, 0));
                    if (possibleCard instanceof Card.CardTmpChangeCost tmp) {
                        newSet.add(new CardCount(tmp.card, 0));
                    }
                }
            }
            for (Potion potion : potions) {
                for (Card possibleCard : potion.getPossibleGeneratedCards(set.stream().map(CardCount::card).toList())) {
                    newSet.add(new CardCount(possibleCard, 0));
                    if (possibleCard instanceof Card.CardTmpChangeCost tmp) {
                        newSet.add(new CardCount(tmp.card, 0));
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
        exhaust = other.exhaust;
        deckArr = Arrays.copyOf(other.deckArr, other.deckArr.length);
        deckArrLen = other.deckArrLen;
        energy = other.energy;
        turnNum = other.turnNum;
        playerTurnStartMaxPossibleHealth = other.playerTurnStartMaxPossibleHealth;
        playerTurnStartPotionCount = other.playerTurnStartPotionCount;
        energyRefill = other.energyRefill;
        player = other.player;
        enemies = other.enemies;
        enemiesAlive = other.enemiesAlive;
        currentAction = other.currentAction;
        drawOrder = other.drawOrder;
        searchRandomGen = other.searchRandomGen;
        if (other.gameActionDeque != null && other.gameActionDeque.size() > 0) {
            gameActionDeque = new CircularArray<>(other.gameActionDeque);
        }
        potionsState = other.potionsState != null ? Arrays.copyOf(other.potionsState, other.potionsState.length) : null;
        other.enemiesCloned = false;
        other.counterCloned = false;
        other.playerCloned = false;
        other.exhaustCloned = false;
        other.drawOrderCloned = false;

        buffs = other.buffs;
        counter = other.counter;
        select1OutOf3CardsIdxes = other.select1OutOf3CardsIdxes;
        if (other.orbs != null) {
            orbs = Arrays.copyOf(other.orbs, other.orbs.length); // todo: make it copy on write
        }
        focus = other.focus;

        legalActions = other.legalActions;
        terminal_action = -100;
    }

    public GameState clone(boolean keepTranspositions) {
        GameState clone = new GameState(this);
        if (keepTranspositions) {
            clone.transpositions = transpositions;
            clone.transpositionsParent = transpositionsParent;
        } else {
            clone.transpositions = new HashMap<>();
            if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH) clone.transpositionsParent = new HashMap<>();
        }
        return clone;
    }

    public void draw(int count) {
        if (count == 0 || getPlayeForRead().cannotDrawCard()) {
            return;
        }
//        if (!prop.testNewFeature || deckArrLen != count) { // todo: add discard count too, enemy nextMove should also set isStochastic
            setIsStochastic();
//        }
        int cardsInHand = 0;
        for (int i = 0; i < hand.length; i++) {
            cardsInHand += hand[i];
        }
        count = Math.min(GameState.HAND_LIMIT - cardsInHand, count);
        boolean firstRandomDraw = true;
        for (int c = 0; c < count; c++) {
            if (deckArrLen == 0) {
                reshuffle();
            }
            if (deckArrLen == 0) {
                return;
            }
            int i, cardIdx;
            if (drawOrder.size() > 0) {
                i = getDrawOrderForWrite().drawTop();
                assert deck[i] > 0;
                drawCardByIdx(i, true);
                cardIdx = i;
            } else {
                i = getSearchRandomGen().nextInt(this.deckArrLen, RandomGenCtx.CardDraw, this);
                if (prop.makingRealMove && prop.doingComparison) {
                    Arrays.sort(this.deckArr, 0, this.deckArrLen);
                }
                cardIdx = deckArr[i];
                deck[deckArr[i]] -= 1;
                hand[deckArr[i]] += 1;
                if (prop.makingRealMove || prop.stateDescOn) {
                    if (firstRandomDraw) {
                        getStateDesc().append(getStateDesc().length() > 0 ? "; " : "").append("Draw ");
                        firstRandomDraw = false;
                    }
                    if (getStateDesc().charAt(getStateDesc().length() - 1) != ' ') {
                        getStateDesc().append(", ");
                    }
                    getStateDesc().append(prop.cardDict[deckArr[i]].cardName);
                }
                deckArr[i] = deckArr[deckArrLen - 1];
                deckArrLen -= 1;
            }
            for (var handler : prop.onCardDrawnHandlers) {
                handler.handle(this, prop.cardDict[cardIdx], -1, false);
            }
        }
    }

    int drawOneCardSpecial() {
        setIsStochastic();
        if (deckArrLen == 0) {
            reshuffle();
        }
        if (deckArrLen == 0) {
            return -1;
        }
        int i;
        if (drawOrder.size() > 0) {
            i = getDrawOrderForWrite().drawTop();
            drawCardByIdx(i, true);
        } else {
            var idx = getSearchRandomGen().nextInt(this.deckArrLen, RandomGenCtx.CardDraw, this);
            i = deckArr[idx];
            deck[deckArr[idx]] -= 1;
            hand[deckArr[idx]] += 1;
            deckArr[idx] = deckArr[deckArrLen - 1];
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

    public void setActionCtx(GameActionCtx ctx, GameAction action) {
        switch (ctx) {
        case PLAY_CARD -> {
            currentAction = null;
            select1OutOf3CardsIdxes = 0;
            actionCtx = ctx;
        }
        case BEGIN_BATTLE, SELECT_ENEMY, SELECT_CARD_HAND, SELECT_CARD_DISCARD, SELECT_CARD_EXHAUST, SELECT_CARD_1_OUT_OF_3 -> {
            currentAction = action;
            actionCtx = ctx;
        }
        case SELECT_SCENARIO, BEGIN_TURN -> actionCtx = ctx;
        }
    }

    boolean playCard(GameAction action, int selectIdx, boolean runActionQueueOnEnd, boolean cloned, boolean useEnergy, boolean exhaustWhenPlayed) {
        int cardIdx = action.idx();
        int lastSelectedIdx = -1;
        boolean cardPlayedSuccessfully = true;
        int energyCost = getCardEnergyCost(cardIdx);
        if (actionCtx == GameActionCtx.PLAY_CARD) {
            hand[cardIdx] -= 1;
            if (energyCost < 0) {
                if (runActionQueueOnEnd) {
                    runActionsInQueueIfNonEmpty();
                }
                return false;
            }
            if (useEnergy) {
                energy -= energyCost;
            }
            if (cardIdx >= prop.realCardsLen) {
                cardIdx = prop.tmpCostCardIdxes[cardIdx];
                action = prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
            }
            if (prop.cardDict[cardIdx].selectEnemy) {
                setActionCtx(GameActionCtx.SELECT_ENEMY, action);
            } else if (prop.cardDict[cardIdx].selectFromHand && !prop.cardDict[cardIdx].selectFromHandLater) {
                setActionCtx(GameActionCtx.SELECT_CARD_HAND, action);
            } else if (prop.cardDict[cardIdx].selectFromDiscard && !prop.cardDict[cardIdx].selectFromDiscardLater) {
                setActionCtx(GameActionCtx.SELECT_CARD_DISCARD, action);
            } else if (prop.cardDict[cardIdx].selectFromExhaust) {
                setActionCtx(GameActionCtx.SELECT_CARD_EXHAUST, action);
            }
        }

        do {
            if (actionCtx == GameActionCtx.SELECT_ENEMY) {
                int targetableEnemies = 0;
                int idx = -1;
                for (int i = 0; i < enemies.size(); i++) {
                    if (enemies.get(i).isTargetable()) {
                        targetableEnemies++;
                        idx = i;
                    }
                }
                if (targetableEnemies == 1) {
                    lastSelectedIdx = idx;
                    setActionCtx(prop.cardDict[cardIdx].play(this, idx, energyCost), action);
                } else if (selectIdx >= 0) {
                    if (getEnemiesForRead().get(selectIdx).isAlive()) {
                        lastSelectedIdx = selectIdx;
                        setActionCtx(prop.cardDict[cardIdx].play(this, selectIdx, energyCost), action);
                        selectIdx = -1;
                    } else {
                        cardPlayedSuccessfully = false;
                        setActionCtx(GameActionCtx.PLAY_CARD, action);
                    }
                } else {
                    cardPlayedSuccessfully = false;
                    break;
                }
            } else if (actionCtx == GameActionCtx.SELECT_CARD_DISCARD) {
                int possibleChoicesCount = 0, lastIdx = 0;
                for (int j = 0; j < discard.length; j++) {
                    possibleChoicesCount += discard[j] > 0 ? 1 : 0;
                    lastIdx = discard[j] > 0 ? j : lastIdx;
                }
                if (possibleChoicesCount == 0) {
                    setActionCtx(GameActionCtx.PLAY_CARD, action);
                } else if (possibleChoicesCount == 1) {
                    setActionCtx(prop.cardDict[cardIdx].play(this, lastIdx, energyCost), action);
                } else if (selectIdx >= 0) {
                    setActionCtx(prop.cardDict[cardIdx].play(this, selectIdx, energyCost), action);
                    selectIdx = -1;
                } else {
                    cardPlayedSuccessfully = false;
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
                    setActionCtx(GameActionCtx.PLAY_CARD, action);
                } else if (possibleChoicesCount == 1) {
                    setActionCtx(prop.cardDict[cardIdx].play(this, lastIdx, energyCost), action);
                } else if (selectIdx >= 0) {
                    setActionCtx(prop.cardDict[cardIdx].play(this, selectIdx, energyCost), action);
                    selectIdx = -1;
                } else {
                    cardPlayedSuccessfully = false;
                    break;
                }
            } else if (actionCtx == GameActionCtx.SELECT_CARD_EXHAUST) {
                int possibleChoicesCount = 0, lastIdx = 0;
                for (int j = 0; j < exhaust.length; j++) {
                    possibleChoicesCount += exhaust[j] > 0 ? 1 : 0;
                    lastIdx = exhaust[j] > 0 ? j : lastIdx;
                }
                if (possibleChoicesCount == 0) {
                    setActionCtx(GameActionCtx.PLAY_CARD, action);
                } else if (possibleChoicesCount == 1) {
                    setActionCtx(prop.cardDict[cardIdx].play(this, lastIdx, energyCost), action);
                } else if (selectIdx >= 0) {
                    setActionCtx(prop.cardDict[cardIdx].play(this, selectIdx, energyCost), action);
                    selectIdx = -1;
                } else {
                    cardPlayedSuccessfully = false;
                    break;
                }
            } else if (actionCtx == GameActionCtx.PLAY_CARD) {
                setActionCtx(prop.cardDict[cardIdx].play(this, -1, energyCost), action);
            }
        } while (actionCtx != GameActionCtx.PLAY_CARD);

        if (actionCtx == GameActionCtx.PLAY_CARD) {
            if (cardPlayedSuccessfully) {
                for (var handler : prop.onCardPlayedHandlers) {
                    handler.handle(this, prop.cardDict[cardIdx], lastSelectedIdx, cloned);
                }
            }
            if (!cloned && !exhaustWhenPlayed) {
                if (prop.cardDict[cardIdx].exhaustWhenPlayed) {
                    exhaustedCardHandle(cardIdx, true);
                } else if ((buffs & PlayerBuff.CORRUPTION.mask()) != 0 && prop.cardDict[cardIdx].cardType == Card.SKILL) {
                    exhaustedCardHandle(cardIdx, true);
                } else if (prop.cardDict[cardIdx].cardType != Card.POWER) {
                    discard[cardIdx] += 1;
                }
            }
            if (prop.normalityCounterIdx < 0 || counter[prop.normalityCounterIdx] < 3) { // todo: hack
                if (runActionQueueOnEnd) {
                    runActionsInQueueIfNonEmpty();
                }
            }
        }
        return cardPlayedSuccessfully;
    }

    void usePotion(GameAction action, int selectIdx) {
        if (prop.hasToyOrniphopter) {
            getPlayerForWrite().heal(5);
        }
        int potionIdx = action.idx();
        if (actionCtx == GameActionCtx.PLAY_CARD) {
            if (prop.potions.get(potionIdx).selectEnemy) {
                setActionCtx(GameActionCtx.SELECT_ENEMY, action);
            } else if (prop.potions.get(potionIdx).selectFromHand) {
                setActionCtx(GameActionCtx.SELECT_CARD_HAND, action);
            } else if (prop.potions.get(potionIdx).selectFromDiscard) {
                setActionCtx(GameActionCtx.SELECT_CARD_DISCARD, action);
            }
        }

        do {
            if (actionCtx == GameActionCtx.SELECT_ENEMY) {
                int targetableEnemies = 0;
                int idx = -1;
                for (int i = 0; i < enemies.size(); i++) {
                    if (enemies.get(i).isTargetable()) {
                        targetableEnemies++;
                        idx = i;
                    }
                }
                if (targetableEnemies == 1) {
                    setActionCtx(prop.potions.get(potionIdx).use(this, idx), action);
                } else if (selectIdx >= 0) {
                    setActionCtx(prop.potions.get(potionIdx).use(this, selectIdx), action);
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
                    setActionCtx(GameActionCtx.PLAY_CARD, action);
                } else if (possibleChoicesCount == 1) {
                    setActionCtx(prop.potions.get(potionIdx).use(this, lastIdx), action);
                } else if (selectIdx >= 0) {
                    setActionCtx(prop.potions.get(potionIdx).use(this, selectIdx), action);
                    selectIdx = -1;
                } else {
                    break;
                }
            } else if (actionCtx == GameActionCtx.SELECT_CARD_HAND) {
                int possibleChoicesCount = 0, lastIdx = 0;
                for (int j = 0; j < hand.length; j++) {
                    if (hand[j] > 0) {
                        possibleChoicesCount += 1;
                        lastIdx = j;
                    }
                }
                if (possibleChoicesCount == 0) {
                    setActionCtx(GameActionCtx.PLAY_CARD, action);
                } else if (possibleChoicesCount == 1) {
                    setActionCtx(prop.potions.get(potionIdx).use(this, lastIdx), action);
                } else if (selectIdx >= 0) {
                    setActionCtx(prop.potions.get(potionIdx).use(this, selectIdx), action);
                    selectIdx = -1;
                } else {
                    break;
                }
            } else if (actionCtx == GameActionCtx.SELECT_CARD_1_OUT_OF_3) {
                break;
            } else if (actionCtx == GameActionCtx.PLAY_CARD) {
                setActionCtx(prop.potions.get(potionIdx).use(this, -1), action);
            }
        } while (actionCtx != GameActionCtx.PLAY_CARD);
        runActionsInQueueIfNonEmpty();
    }

    public void runActionsInQueueIfNonEmpty() {
        while (gameActionDeque != null && gameActionDeque.size() > 0 && isTerminal() == 0) {
            gameActionDeque.pollFirst().doAction(this);
        }
        if (gameActionDeque != null && isTerminal() != 0) {
            gameActionDeque.clear();
        }
    }

    void startTurn() {
        turnNum++;
        playerTurnStartMaxPossibleHealth = getMaxPossibleHealth();
        playerTurnStartPotionCount = getPotionCount();
        gainEnergy(energyRefill);
        var enemies = getEnemiesForWrite();
        for (int i = 0; i < enemies.size(); i++) {
            var enemy = enemies.get(i);
            if (enemy.isAlive()) {
                var enemy2 = enemies.getForWrite(i);
                enemy2.endTurn(turnNum);
                if (!prop.hasRunicDome) {
                    enemy2.nextMove(this, getSearchRandomGen());
                }
            }
        }
        draw(5);
        for (GameEventHandler handler : prop.startOfTurnHandlers) {
            handler.handle(this);
        }
    }

    public byte getPotionCount() {
        byte ret = 0;
        if (potionsState != null) {
            for (int i = 0; i < potionsState.length; i += 3) {
                ret += potionsState[i];
            }
        }
        return ret;
    }

    private void endTurn() {
        if (prop.cardDict.length != prop.realCardsLen) {
            for (int i = prop.realCardsLen; i < prop.cardDict.length; i++) {
                if (hand[i] > 0) {
                    discard[prop.tmpCostCardIdxes[i]] += hand[i];
                    hand[i] = 0;
                }
            }
        }
        for (GameEventHandler handler : prop.preEndTurnHandlers) {
            handler.handle(this);
        }
        triggerOrbsPassiveEndOfTurn();
        byte[] handDup = Arrays.copyOf(hand, hand.length);
        for (int i = 0; i < hand.length; i++) {
            if (handDup[i] > 0) {
                if (prop.cardDict[i].ethereal) {
                    for (int count = 0; count < handDup[i]; count++) {
                        exhaustedCardHandle(i, false);
                    }
                    hand[i] -= handDup[i];
                } else if (!prop.hasRunicPyramid) {
                    discard[i] += handDup[i];
                    hand[i] -= handDup[i];
                }
            }
        }
        var enemies = getEnemiesForWrite();
        for (int i = 0; i < enemies.size(); i++) {
            var enemy = enemies.get(i);
            if (enemy.isAlive()) {
                var enemy2 = enemies.getForWrite(i);
                if (prop.hasRunicDome) {
                    var oldIsStochastic = isStochastic;
                    isStochastic = false;
                    enemy2.nextMove(this, getSearchRandomGen());
                    if (isStochastic && prop.random instanceof InteractiveMode.RandomGenInteractive rgi && !rgi.rngOn) {
                        rgi.selectEnemyMove(this, enemy2, i);
                    }
                    isStochastic = oldIsStochastic;
                }
                enemy2.startTurn();
                enemy2.doMove(this);
            }
        }
        getPlayerForWrite().endTurn(this);
        if (!prop.hasIceCream) {
            energy = 0;
        }
    }

    int doAction(int actionIdx) {
        GameAction action = prop.actionsByCtx[actionCtx.ordinal()][getLegalActions()[actionIdx]];
        int ret = 0;
        if (action.type() == GameActionType.BEGIN_BATTLE) {
            if (prop.randomization != null) {
                ret = prop.randomization.randomize(this);
                setIsStochastic();
            }
            for (GameEventHandler handler : prop.startOfBattleHandlers) {
                handler.handle(this);
            }
            startTurn();
            setActionCtx(GameActionCtx.PLAY_CARD, null);
        } else if (action.type() == GameActionType.END_TURN) {
            endTurn();
//            startTurn();
            setActionCtx(GameActionCtx.BEGIN_TURN, null);
        } else if (action.type() == GameActionType.PLAY_CARD) {
            playCard(action, -1, true, false, true, false);
        } else if (action.type() == GameActionType.SELECT_ENEMY) {
            if (currentAction.type() == GameActionType.PLAY_CARD) {
                playCard(currentAction, action.idx(), true, false, true, false);
            } else if (currentAction.type() == GameActionType.USE_POTION) {
                usePotion(currentAction, action.idx());
            }
        } else if (action.type() == GameActionType.SELECT_CARD_HAND) {
            if (currentAction.type() == GameActionType.PLAY_CARD) {
                playCard(currentAction, action.idx(), true, false, true, false);
            } else if (currentAction.type() == GameActionType.USE_POTION) {
                usePotion(currentAction, action.idx());
            }
        } else if (action.type() == GameActionType.SELECT_CARD_DISCARD) {
            if (currentAction.type() == GameActionType.PLAY_CARD) {
                playCard(currentAction, action.idx(), true, false, true, false);
            } else if (currentAction.type() == GameActionType.USE_POTION) {
                usePotion(currentAction, action.idx());
            }
        } else if (action.type() == GameActionType.SELECT_CARD_EXHAUST) {
            playCard(currentAction, action.idx(), true, false, true, false);
        } else if (action.type() == GameActionType.SELECT_CARD_1_OUT_OF_3) {
            addCardToHand(action.idx());
            setActionCtx(GameActionCtx.PLAY_CARD, null);
        } else if (action.type() == GameActionType.USE_POTION) {
            potionsState[action.idx() * 3] = 0;
            usePotion(action, -1);
        } else if (action.type() == GameActionType.SELECT_SCENARIO) {
            prop.preBattleScenarios.randomize(this, prop.preBattleGameScenariosList.get(action.idx()).getKey());
            setActionCtx(GameActionCtx.BEGIN_BATTLE, null);
        } else if (action.type() == GameActionType.BEGIN_TURN) {
            startTurn();
            setActionCtx(GameActionCtx.PLAY_CARD, null);
            runActionsInQueueIfNonEmpty();
        }
        legalActions = null;
        v_other = null;
        q_other = null;
        total_q_other = null;
        policy = null;
        if (isStochastic) {
            if (!(Configuration.TRANSPOSITION_ACROSS_CHANCE_NODE && (!Configuration.TEST_TRANSPOSITION_ACROSS_CHANCE_NODE || prop.testNewFeature)) || (action.type() == GameActionType.BEGIN_TURN || action.type() == GameActionType.BEGIN_BATTLE)) {
                transpositions = new HashMap<>();
                if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH) transpositionsParent = new HashMap<>();
            }
            searchFrontier = null;
        }
        return ret;
    }

    boolean isActionLegal(int action) {
        if (actionCtx == GameActionCtx.BEGIN_BATTLE || actionCtx == GameActionCtx.BEGIN_TURN) {
            return action == 0;
        } else if (actionCtx == GameActionCtx.PLAY_CARD) {
            if (prop.normalityCounterIdx >= 0 && counter[prop.normalityCounterIdx] >= 3) {
                return false;
            }
            GameAction[] a = prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()];
            if (action < 0 || action >= a.length) {
                return false;
            }
            if (a[action].type() == GameActionType.END_TURN) {
                return true;
            } else if (a[action].type() == GameActionType.USE_POTION) {
                return potionsState[a[action].idx() * 3] == 1;
            } else if (hand[a[action].idx()] > 0) {
                int cost = getCardEnergyCost(a[action].idx());
                if (getPlayeForRead().isEntangled() && prop.cardDict[a[action].idx()].cardType == Card.ATTACK) {
                    return false;
                }
                if (cost >= 0 && cost <= energy) {
                    if (prop.cardDict[a[action].idx()].selectEnemy) {
                        for (int i = 0; i < enemies.size(); i++) {
                            if (enemies.get(i).isTargetable()) {
                                return true;
                            }
                        }
                        return false;
                    }
                    //                    if (prop.cardDict[a[action].idx()].cardName.equals("Defend")) {
//                        var dmg = 0;
//                        for (var enemy : enemies) {
//                            if (enemy.getHealth() > 0 && enemy.getMoveString(this).startsWith("Attack ")) {
//                                var str = enemy.getMoveString(this);
//                                var start = 7;
//                                while (start < str.length() && str.charAt(start) <= '9' && str.charAt(start) >= '0') {
//                                    start++;
//                                }
//                                dmg += Integer.parseInt(str.substring(7, start));
//                            }
//                        }
//                        if (player.getBlock() >= dmg) {
//                            return false;
//                        }
//                    }
                    return true;
                }
            }
            return false;
        } else if (actionCtx == GameActionCtx.SELECT_ENEMY) {
            GameAction[] a = prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()];
            if (action < 0 || action >= a.length) {
                return false;
            }
            return enemies.get(a[action].idx()).isAlive();
        } else if (actionCtx == GameActionCtx.SELECT_CARD_HAND) {
            GameAction[] a = prop.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()];
            if (action < 0 || action >= a.length) {
                return false;
            }
            if (currentAction.type() == GameActionType.PLAY_CARD) {
                return hand[a[action].idx()] > 0 && prop.cardDict[currentAction.idx()].canSelectFromHand(prop.cardDict[action]);
            } else {
                return true; // todo: potion like gamblers
            }
        } else if (actionCtx == GameActionCtx.SELECT_CARD_DISCARD) {
            GameAction[] a = prop.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()];
            if (action < 0 || action >= a.length) {
                return false;
            }
            return discard[a[action].idx()] > 0;
        } else if (actionCtx == GameActionCtx.SELECT_CARD_EXHAUST) {
            GameAction[] a = prop.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()];
            if (action < 0 || action >= a.length) {
                return false;
            }
            return exhaust[a[action].idx()] > 0;
        } else if (actionCtx == GameActionCtx.SELECT_CARD_1_OUT_OF_3) {
            GameAction[] a = prop.actionsByCtx[GameActionCtx.SELECT_CARD_1_OUT_OF_3.ordinal()];
            if (action < 0 || action >= a.length) {
                return false;
            }
            int idx = prop.select1OutOf3CardsReverseIdxes[a[action].idx()];
            return (select1OutOf3CardsIdxes & 255) == idx || ((select1OutOf3CardsIdxes >> 8) & 255) == idx || ((select1OutOf3CardsIdxes >> 16) & 255) == idx;
        } else if (actionCtx == GameActionCtx.SELECT_SCENARIO) {
            GameAction[] a = prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()];
            return action >= 0 && action < a.length;
        }
        return false;
    }

    int get_v_len() {
        return 3 + prop.extraOutputLen;
    }

    void get_v(double[] out) {
        var player = getPlayeForRead();
        if (player.getHealth() <= 0 || turnNum > 30) {
            Arrays.fill(out, 0);
            return;
        }
        boolean enemiesAllDead = true;
        for (var enemy : enemies) {
            if (enemy.isAlive()) {
                enemiesAllDead = false;
                break;
            }
        }
        if (enemiesAllDead) {
            out[V_WIN_IDX] = 1;
            out[V_HEALTH_IDX] = ((double) player.getHealth()) / player.getMaxHealth();
            if (prop.testPotionOutput) {
                for (int i = 0; i < prop.potions.size(); i++) {
                    if (potionsState[i * 3] == 1 || potionsState[i * 3 + 2] == 0) {
                        out[V_OTHER_IDX_START + i] = 1;
                    } else {
                        out[V_OTHER_IDX_START + i] = 0;
                    }
                }
            }
        } else {
            out[V_WIN_IDX] = v_win;
            out[V_HEALTH_IDX] = Math.min(v_health, getMaxPossibleHealth() / (float) getPlayeForRead().getMaxHealth());
            if (prop.testPotionOutput) {
                for (int i = 0; i < prop.potions.size(); i++) {
                    if (potionsState[i * 3 + 2] == 0) {
                        out[V_OTHER_IDX_START + i] = 1;
                    } else if (potionsState[i * 3] == 0) {
                        out[V_OTHER_IDX_START + i] = 0;
                    } else {
                        out[V_OTHER_IDX_START + i] = v_other[i];
                    }
                }
            }
        }
        for (int i = 0; i < prop.extraTrainingTargets.size(); i++) {
            prop.extraTrainingTargets.get(i).fillVArray(this, out, enemiesAllDead);
        }
        out[V_COMB_IDX] = calc_q(out);
    }

    private boolean checkIfCanHeal() {
        if (prop.hasBurningBlood) {
            return true;
        }
        if (prop.selfRepairCounterIdx >= 0) {
            return true;
        }
        if (prop.hasMeatOnBone) {
            return getPlayeForRead().getHealth() < getPlayeForRead().getMaxHealth() / 2 + 12;
        }
        if (prop.healCardsIdxes == null) {
            return false;
        }
        if (prop.findCardIndex("Exhume") >= 0 || prop.findCardIndex("Exhume+") >= 0) {
            return true;
        }
        for (int i = 0; i < prop.healCardsIdxes.length; i++) {
            if (hand[prop.healCardsIdxes[i]] > 0 || deck[prop.healCardsIdxes[i]] > 0 || discard[prop.healCardsIdxes[i]] > 0) {
                return true;
            }
        }
        return false;
    }

    // todo: yeah domain knowledge is really really hard
    public int getMaxPossibleHealth() {
        if (checkIfCanHeal()) {
            int v = 0;
            if (prop.healCardsIdxes != null) {
                for (int i = 0; i < prop.healCardsIdxes.length; i++) {
                    // todo: need to take take feed+ into account
                    if (prop.cardDict[prop.healCardsIdxes[i]].cardName.startsWith("Feed")) {
                        if (getExhaustForRead()[prop.findCardIndex("Armanent+")] > 0) {
                            v += 3;
                        } else {
                            v += 4;
                        }
                    }
                    // todo: need to count max strength
                    if (prop.cardDict[prop.healCardsIdxes[i]].cardName.startsWith("Reaper")) {
                        for (var enemy : getEnemiesForRead()) {
                            v += enemy.getHealth();
                        }
                    }
                    if (prop.cardDict[prop.healCardsIdxes[i]] instanceof CardDefect.SelfRepair ||
                            prop.cardDict[prop.healCardsIdxes[i]] instanceof CardDefect.SelfRepairP) {
                        int m = getNonExhaustCount(prop.healCardsIdxes[i]);
                        if (m > 0) {
                            if (prop.echoFormCardIdx > 0 || prop.echoFormPCardIdx > 0) {
                                if (getCounterForRead()[prop.echoFormCounterIdx] > 0) {
                                    m *= 2;
                                } else if (prop.echoFormCardIdx > 0 && getNonExhaustCount(prop.echoFormCardIdx) > 0) {
                                    m *= 2;
                                } else if (prop.echoFormPCardIdx > 0 && getNonExhaustCount(prop.echoFormPCardIdx) > 0) {
                                    m *= 2;
                                }
                            }
                        }
                        v += (prop.cardDict[prop.healCardsIdxes[i]] instanceof CardDefect.SelfRepair ? 7 : 10) * m;
                    }
                }
            }

            if (prop.selfRepairCounterIdx >= 0) {
                v += getCounterForRead()[prop.selfRepairCounterIdx];
            }
            if (prop.hasBurningBlood) {
                v += 6;
            }

            int hp;
            if (prop.hasMeatOnBone) {
                // todo: feed and meat on bone interaction when they are together
                // todo: self repair and meat on bone interaction when they are together
                if (getPlayeForRead().getHealth() >= getPlayeForRead().getMaxHealth() / 2 + 12) {
                    return Math.min(getPlayeForRead().getMaxHealth(), getPlayeForRead().getHealth() + v);
                }
                if (getPlayeForRead().getHealth() + v < getPlayeForRead().getMaxHealth() / 2) {
                    return getPlayeForRead().getHealth() + v + 12;
                }
                return Math.min(getPlayeForRead().getMaxHealth(), Math.max(getPlayeForRead().getHealth() + v, getPlayeForRead().getMaxHealth() / 2 + 12));
            } else {
                hp = Math.min(getPlayeForRead().getMaxHealth(), getPlayeForRead().getHealth() + v);
            }
            return hp;
        } else {
            return getPlayeForRead().getHealth();
        }
    }

    private int getNonExhaustCount(int cardIdx) {
        return getHand()[cardIdx] + getDiscard()[cardIdx] + getDeck()[cardIdx];
    }

    public int isTerminal() {
        if (getPlayeForRead().getHealth() <= 0 || turnNum > 30) {
            return -1;
        } else {
            for (var enemy : enemies) {
                if (enemy.isAlive()) {
                    return 0;
                }
            }
            return 1;
        }
    }

    public double calc_q(double[] v) {
        var health = v[V_HEALTH_IDX];
        var win = v[V_WIN_IDX];
        for (int i = 0; i < prop.potions.size(); i++) {
            if (potionsState[i * 3] == 0 && potionsState[i * 3 + 2] == 1 && prop.potions.get(i) instanceof Potion.BloodPotion pot) {
                v[V_HEALTH_IDX] = Math.min(v[V_HEALTH_IDX] - ((pot.getHealAmount(this) + 1) / (float) player.getMaxHealth()), 0);
                v[V_WIN_IDX] = v[V_WIN_IDX] * potionsState[i * 3 + 1] / 100.0;
            }
        }
        for (int i = 0; i < prop.extraTrainingTargets.size(); i++) {
            prop.extraTrainingTargets.get(i).updateQValues(this, v);
        }
        double base = v[V_WIN_IDX] * 0.5 + v[V_WIN_IDX] * v[V_WIN_IDX] * v[V_HEALTH_IDX] * 0.5;
        v[V_WIN_IDX] = win;
        v[V_HEALTH_IDX] = health;
        if (prop.testPotionOutput) {
            for (int i = 0; i < prop.potions.size(); i++) {
                //                health = Math.max(0, health - (1 - v[V_OTHER_IDX_START + i]) * (15 / (float) player.getMaxHealth()));
                if (potionsState[i * 3 + 2] == 1 && !(prop.potions.get(i) instanceof Potion.BloodPotion)) {
                    base *= potionsState[i * 3 + 1] / 100.0 + (100 - potionsState[i * 3 + 1]) / 100.0 * v[V_OTHER_IDX_START + i];
                }
            }
        }
        if (!prop.testPotionOutput) {
            for (int i = 0; i < prop.potions.size(); i++) {
                if (potionsState[i * 3] == 0 && potionsState[i * 3 + 2] == 1 && !(prop.potions.get(i) instanceof Potion.BloodPotion)) {
                    base *= potionsState[i * 3 + 1] / 100.0;
                }
            }
        }
        return base;
    }

    public double get_q() {
        var out = new double[get_v_len()];
        get_v(out);
        return calc_q(out);
    }

    @Override public String toString() {
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
        boolean hasExhaust = false;
        for (int i = 0; i < exhaust.length; i++) {
            if (exhaust[i] > 0) {
                hasExhaust = true;
            }
        }
        if (hasExhaust) {
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
        if (orbs != null) {
            str.append(", orbs=[");
            for (int i = orbs.length - 1; i >= 0 ; i--) {
                str.append(i == orbs.length - 1 ? "" : ", ").append(OrbType.values()[orbs[i]].abbrev);
            }
            str.append("]");
        }
        if (focus > 0) {
            str.append(", focus=").append(focus);
        }
        str.append(", energy=").append(energy);
        if (actionCtx != GameActionCtx.PLAY_CARD) {
            str.append(", ctx=").append(actionCtx);
            if (actionCtx == GameActionCtx.SELECT_ENEMY || actionCtx == GameActionCtx.SELECT_CARD_HAND ||
                    actionCtx == GameActionCtx.SELECT_CARD_EXHAUST || actionCtx == GameActionCtx.SELECT_CARD_DISCARD) {
                if (currentAction.type() == GameActionType.PLAY_CARD) {
                    str.append("[").append(prop.cardDict[currentAction.idx()].cardName).append("]");
                } else if (currentAction.type() == GameActionType.USE_POTION) {
                    str.append("[").append(prop.potions.get(currentAction.idx())).append("]");
                }
            }
        }
        str.append(", ").append(getPlayeForRead());
        str.append(", [");
        int eAlive = 0;
        for (var enemy : enemies) {
            if (enemy.isAlive()) {
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
                    String s = prop.counterHandlers[i].getDisplayString(this);
                    tmp.append(first ? "" : ", ").append(prop.counterNames[i]).append('=').append(s != null ? s : getCounterForRead()[i]);
                    first = false;
                }
            }
            if (tmp.length() > 0) {
                str.append(", other=[").append(tmp).append("]");
            }
        }
        boolean showQComb = prop.potions.size() > 0 || (prop.extraTrainingTargets.size() > 0 && isTerminal() > 0);
        str.append(", v=(");
        if (showQComb) {
            str.append(formatFloat(get_q())).append("/");
        }
        str.append(formatFloat(v_win)).append("/").append(formatFloat(v_health)).append(",").append(formatFloat(v_health * getPlayeForRead().getMaxHealth()));
        if (v_other != null) {
            for (int i = 0; i < v_other.length; i++) {
                str.append("/").append(formatFloat(v_other[i]));
            }
        }
        str.append(")");
//        str.append(", var=").append(formatFloat(varianceS / total_n));
        if (policy != null) {
            str.append(", q/p/n=[");
            first = true;
            for (int i = 0; i < getLegalActions().length; i++) {
                var p_str = policy != null ? formatFloat(policy[i]) : "0";
                var p_str2 = policyMod != null && policyMod != policy ? formatFloat(policyMod[i]) : null;
                var q_win_str = formatFloat(n[i] == 0 ? 0 : q_win[i] / n[i]);
                var q_health_str = formatFloat(n[i] == 0 ? 0 : q_health[i] / n[i]);
                var q_str = formatFloat(n[i] == 0 ? 0 : q_comb[i] / n[i]);
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
            str.append(']');
        }
        str.append('}');
        return str.toString();
    }

    public int[] getLegalActions() {
        if (legalActions == null) {
            int count = 0;
            for (int i = 0; i < prop.maxNumOfActions; i++) {
                if (isActionLegal(i)) {
                    count += 1;
                }
            }
            legalActions = new int[count];
            int idx = 0;
            for (int i = 0; i < prop.maxNumOfActions; i++) {
                if (isActionLegal(i)) {
                    legalActions[idx++] = i;
                }
            }
        }
        return legalActions;
    }

    public void doEval(Model model) {
        getLegalActions();
        NNOutput output = model.eval(this);
        policy = output.policy();
        v_health = output.v_health();
        v_win = output.v_win();
        v_other = output.v_other();
        q_comb = new double[policy.length];
        q_health = new double[policy.length];
        q_win = new double[policy.length];
        if (v_other != null) {
            q_other = new double[policy.length][v_other.length];
            total_q_other = new double[v_other.length];
        }
        n = new int[policy.length];
        ns = new State[policy.length];
    }

    private int getNNInputLen() {
        int inputLen = 0;
        inputLen += prop.realCardsLen;
        inputLen += hand.length;
        if (Configuration.CARD_IN_HAND_IN_NN_INPUT) {
            inputLen += 1;
        }
        inputLen += prop.discardIdxes.length;
        if (prop.selectFromExhaust) {
            inputLen += prop.realCardsLen;
        }
        if (MAX_AGENT_DECK_ORDER_MEMORY > 0 && prop.needDeckOrderMemory) {
            inputLen += prop.realCardsLen * MAX_AGENT_DECK_ORDER_MEMORY;
        }
        for (int i = 2; i < prop.actionsByCtx.length; i++) {
            if (prop.actionsByCtx[i] != null && i != GameActionCtx.BEGIN_TURN.ordinal()) {
                inputLen += 1;
            }
        }
        inputLen += 1; // energy
        inputLen += 1; // player health
        inputLen += 1; // player block
        inputLen += prop.maxNumOfOrbs * 5;
        if (prop.playerFocusCanChange) {
            inputLen += 1; // focus
        }
        if (prop.playerArtifactCanChange) {
            inputLen += 1; // player artifact
        }
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
        if (prop.playerCanGetEntangled) {
            inputLen += 1; // player entangled
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
        for (var handler : prop.nnInputHandlers) {
            inputLen += handler.getInputLenDelta();
        }
        inputLen += prop.potions.size() * 3;
        // cards currently selecting enemies
        if (prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null && enemies.size() > 1) {
            for (GameAction action : prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && prop.cardDict[action.idx()].selectEnemy && action.idx() < prop.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && prop.potions.get(action.idx()).selectEnemy) {
                    inputLen += 1;
                }
            }
        }
        if (prop.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()] != null) {
            for (GameAction action : prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && prop.cardDict[action.idx()].selectFromHand && action.idx() < prop.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && prop.potions.get(action.idx()).selectFromHand) {
                    inputLen += 1;
                }
            }
        }
        if (prop.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()] != null) {
            for (GameAction action : prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && prop.cardDict[action.idx()].selectFromDiscard && action.idx() < prop.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && prop.potions.get(action.idx()).selectFromDiscard) {
                    inputLen += 1;
                }
            }
        }
        if (prop.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()] != null) {
            for (GameAction action : prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && prop.cardDict[action.idx()].selectFromExhaust && action.idx() < prop.realCardsLen) {
                    inputLen += 1;
                }
            }
        }
        inputLen += prop.select1OutOf3CardsIdxes.length;
        for (var enemy : enemies) {
            inputLen += 1; // enemy health
            if (prop.enemyCanGetVuln) {
                inputLen += 1; // enemy vulnerable
            }
            if (prop.enemyCanGetWeakened) {
                inputLen += 1; // enemy weak
            }
            if (prop.enemyStrengthEotCanChange) {
                inputLen += 1; // enemy gain strength eot
            }
            if (enemy.property.canGainBlock) {
                inputLen += 1; // enemy block
            }
            if (enemy.property.canGainStrength || prop.enemyStrengthCanChange) {
                inputLen += 1; // enemy strength
            }
            if (enemy.property.hasArtifact) {
                inputLen += 1; // enemy artifact
            }
            if (enemy.property.canGainRegeneration) {
                inputLen += 1; // enemy regeneration
            }
            if (enemy.property.canGainMetallicize) {
                inputLen += 1; // enemy metallicize
            }
            inputLen += enemy.property.numOfMoves; // enemy moves
            if (enemy.property.useLast2MovesForMoveSelection) {
                inputLen += enemy.property.numOfMoves;
            }
            inputLen += enemy.getNNInputLen(prop);
            if (enemy instanceof Enemy.RedLouse || enemy instanceof Enemy.GreenLouse) {
                inputLen += 1;
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
        str += "Neural Network Input Breakdown (" + prop.inputLen + " inputs):\n";
        str += "    " + prop.realCardsLen + " inputs for cards in deck\n";
        str += "    " + hand.length + " inputs for cards in hand\n";
        if (Configuration.CARD_IN_HAND_IN_NN_INPUT) {
            str += "    1 input for number of cards in hand\n";
        }
        str += "    " + prop.discardIdxes.length + " inputs to keep track of cards in discard\n";
        if (prop.selectFromExhaust) {
            str += "    " + prop.realCardsLen + " inputs for cards in exhaust\n";
        }
        if (MAX_AGENT_DECK_ORDER_MEMORY > 0 && prop.needDeckOrderMemory) {
            str += "    " + prop.realCardsLen * MAX_AGENT_DECK_ORDER_MEMORY + " inputs to keep track of known card at top of deck\n";
        }
        for (int i = 2; i < prop.actionsByCtx.length; i++) {
            if (prop.actionsByCtx[i] != null && i != GameActionCtx.BEGIN_TURN.ordinal()) {
                str += "    1 input to keep track of ctx " + GameActionCtx.values()[i] + "\n";
            }
        }
        str += "    1 input to keep track of energy\n";
        str += "    1 input to keep track of player health\n";
        str += "    1 input to keep track of player block\n";
        if (prop.maxNumOfOrbs > 0) {
            str += "    " + prop.maxNumOfOrbs + "*5 inputs to keep track of player orb slots\n";
        }
        if (prop.playerFocusCanChange) {
            str += "    1 input to keep track of player focus\n";
        }
        if (prop.playerArtifactCanChange) {
            str += "    1 input to keep track of player artifact\n";
        }
        if (prop.playerStrengthCanChange) {
            str += "    1 input to keep track of player strength\n";
        }
        if (prop.playerDexterityCanChange) {
            str += "    1 input to keep track of player dexterity\n";
        }
        if (prop.playerStrengthEotCanChange) {
            str += "    1 input to keep track of player lose strength eot debuff\n";
        }
        if (prop.playerDexterityEotCanChange) {
            str += "    1 input to keep track of player lose dexterity eot debuff\n";
        }
        if (prop.playerCanGetVuln) {
            str += "    1 input to keep track of player vulnerable\n";
        }
        if (prop.playerCanGetWeakened) {
            str += "    1 input to keep track of player weak\n";
        }
        if (prop.playerCanGetFrailed) {
            str += "    1 input to keep track of player frail\n";
        }
        if (prop.playerCanGetEntangled) {
            str += "    1 input to keep track of whether player is entangled or not\n";
        }
        if (prop.battleTranceExist) {
            str += "    1 input to keep track of battle trance cannot draw card debuff\n";
        }
        if (prop.energyRefillCanChange) {
            str += "    1 input to keep track of berserk\n";
        }
        for (PlayerBuff buff : PlayerBuff.BUFFS) {
            if ((prop.possibleBuffs & buff.mask()) != 0) {
                str += "    1 input to keep track of buff " + buff.name() + "\n";
            }
        }
        for (int i = 0; i < prop.counterHandlers.length; i++) {
            if (prop.counterHandlers[i] != null) {
                str += "    " + prop.counterHandlers[i].getInputLenDelta() + " input to keep track of counter for " + prop.counterNames[i] + "\n";
            }
        }
        for (int i = 0; i < prop.nnInputHandlersName.length; i++) {
            str += "    " + prop.nnInputHandlers[i].getInputLenDelta() + " input to keep track of " + prop.nnInputHandlersName[i] + "\n";
        }
        for (int i = 0; i < prop.potions.size(); i++) {
            str += "    3 inputs to keep track of " + prop.potions.get(i) + " usage\n";
        }
        if (prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null && enemies.size() > 1) {
            for (GameAction action : prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && prop.cardDict[action.idx()].selectEnemy && action.idx() < prop.realCardsLen) {
                    str += "    1 input to keep track of currently played card " + prop.cardDict[action.idx()].cardName + " for selecting enemy\n";
                } else if (action.type() == GameActionType.USE_POTION && prop.potions.get(action.idx()).selectEnemy) {
                    str += "    1 input to keep track of currently used potion " + prop.potions.get(action.idx()) + " for selecting enemy\n";
                }
            }
        }
        if (prop.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()] != null) {
            for (GameAction action : prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && prop.cardDict[action.idx()].selectFromHand && action.idx() < prop.realCardsLen) {
                    str += "    1 input to keep track of currently played card " + prop.cardDict[action.idx()].cardName + " for selecting card from hand\n";
                } else if (action.type() == GameActionType.USE_POTION && prop.potions.get(action.idx()).selectFromHand) {
                    str += "    1 input to keep track of currently used potion " + prop.potions.get(action.idx()) + " for selecting card from hand\n";
                }
            }
        }
        if (prop.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()] != null) {
            for (GameAction action : prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && prop.cardDict[action.idx()].selectFromDiscard && action.idx() < prop.realCardsLen) {
                    str += "    1 input to keep track of currently played card " + prop.cardDict[action.idx()].cardName + " for selecting card from discard\n";
                } else if (action.type() == GameActionType.USE_POTION && prop.potions.get(action.idx()).selectFromDiscard) {
                    str += "    1 input to keep track of currently used potion " + prop.potions.get(action.idx()) + " for selecting card from discard\n";
                }
            }
        }
        if (prop.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()] != null) {
            for (GameAction action : prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && prop.cardDict[action.idx()].selectFromExhaust && action.idx() < prop.realCardsLen) {
                    str += "    1 input to keep track of currently played card " + prop.cardDict[action.idx()].cardName + " for selecting card from exhaust\n";
                }
            }
        }
        if (prop.select1OutOf3CardsIdxes.length > 0) {
            str += "    " + prop.select1OutOf3CardsIdxes.length + " inputs to keep track of selecting cards from 1 out of 3 cards";
        }
        for (var enemy : enemies) {
            str += "    *** " + enemy.getName() + " ***\n";
            str += "        1 input to keep track of health\n";
            if (prop.enemyCanGetVuln) {
                str += "        1 input to keep track of vulnerable\n";
            }
            if (prop.enemyCanGetWeakened) {
                str += "        1 input to keep track of weak\n";
            }
            if (prop.enemyStrengthEotCanChange) {
                str += "        1 input to keep track of enemy gain strength eot\n";
            }
            if (enemy.property.canGainBlock) {
                str += "        1 input to keep track of block\n";
            }
            if (enemy.property.canGainStrength || prop.enemyStrengthCanChange) {
                str += "        1 input to keep track of strength\n";
            }
            if (enemy.property.hasArtifact) {
                str += "        1 input to keep track of artifact\n";
            }
            if (enemy.property.canGainRegeneration) {
                str += "        1 input to keep track of regeneration\n";
            }
            if (enemy.property.canGainMetallicize) {
                str += "        1 input to keep track of metallicize\n";
            }
            str += "        " + enemy.property.numOfMoves + " inputs to keep track of current move from enemy\n";
            if (enemy.property.useLast2MovesForMoveSelection) {
                str += "        " + enemy.property.numOfMoves + " inputs to keep track of move history from enemy\n";
            }
            String desc = enemy.getNNInputDesc(prop);
            if (desc != null) {
                str += "        " + desc + "\n";
            }
            if (enemy instanceof Enemy.RedLouse || enemy instanceof Enemy.GreenLouse) {
                str += "        1 input to keep track of louse damage\n";
            }
        }
        return str;
    }

    public float[] getNNInput() {
        var player = getPlayeForRead();
        int idx = 0;
        var x = new float[prop.inputLen];
        for (int i = 0; i < prop.realCardsLen; i++) {
            x[idx++] = deck[i] / (float) 10.0;
        }
        int cardsInHand = 0;
        for (int j : hand) {
            cardsInHand += j;
            x[idx++] = j / (float) 10.0;
        }
        if (Configuration.CARD_IN_HAND_IN_NN_INPUT) {
            x[idx++] = (cardsInHand - 5) / 10.0f;
        }
        for (int i = 0; i < prop.discardIdxes.length; i++) {
            x[idx++] = discard[prop.discardIdxes[i]] / (float) 10.0;
        }
        if (prop.selectFromExhaust) {
            for (int i = 0; i < prop.realCardsLen; i++) {
                x[idx++] = exhaust[i] / (float) 10.0;
            }
        }
        if (MAX_AGENT_DECK_ORDER_MEMORY > 0 && prop.needDeckOrderMemory) {
            for (int i = 0; i < Math.min(MAX_AGENT_DECK_ORDER_MEMORY, drawOrder.size()); i++) {
                for (int j = 0; j < prop.realCardsLen; j++) {
                    if (j == drawOrder.ithCardFromTop(i)) {
                        x[idx++] = 1f;
                    } else {
                        x[idx++] = 0f;
                    }
                }
            }
            for (int i = 0; i < MAX_AGENT_DECK_ORDER_MEMORY - drawOrder.size(); i++) {
                for (int j = 0; j < prop.realCardsLen; j++) {
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
        if (orbs != null) {
            for (int i = 0; i < orbs.length; i++) {
                if (orbs[i] > 0) {
                    x[idx + orbs[i]] = 0.5f;
                }
                idx += 5;
            }
        }
        for (int i = orbs == null ? 0 : orbs.length; i < prop.maxNumOfOrbs; i++) {
            x[idx] = 0.5f;
            idx += 5;
        }
        if (prop.playerFocusCanChange) {
            x[idx] = focus / 15.0f;
        }
        if (prop.playerArtifactCanChange) {
            x[idx++] = player.getArtifact() / (float) 3.0;
        }
        if (prop.playerStrengthCanChange) {
            x[idx++] = player.getStrength() / (float) 30.0;
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
        if (prop.playerCanGetEntangled) {
            x[idx++] = player.isEntangled() ? 0.5f : -0.5f;
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
        for (var handler : prop.nnInputHandlers) {
            idx = handler.addToInput(this, x, idx);
        }
        for (int i = 0; i < prop.potions.size(); i++) {
            x[idx++] = potionsState[i * 3] == 1 ? 0.5f : -0.5f;
            x[idx++] = potionsState[i * 3 + 1] / 100f;
            x[idx++] = potionsState[i * 3 + 2] == 1 ? 0.5f : -0.5f;
        }
        if (prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null && enemies.size() > 1) {
            for (GameAction action : prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && prop.cardDict[action.idx()].selectEnemy && action.idx() < prop.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && prop.potions.get(action.idx()).selectEnemy) {
                    x[idx++] = currentAction == action ? 0.6f : -0.6f;
                }
            }
        }
        if (prop.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()] != null) {
            for (GameAction action : prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && prop.cardDict[action.idx()].selectFromHand && action.idx() < prop.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && prop.potions.get(action.idx()).selectFromHand) {
                    x[idx++] = currentAction == action ? 0.6f : -0.6f;
                }
            }
        }
        if (prop.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()] != null) {
            for (GameAction action : prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && prop.cardDict[action.idx()].selectFromDiscard && action.idx() < prop.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && prop.potions.get(action.idx()).selectFromDiscard) {
                    x[idx++] = currentAction == action ? 0.6f : -0.6f;
                }
            }
        }
        if (prop.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()] != null) {
            for (GameAction action : prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && prop.cardDict[action.idx()].selectFromExhaust && action.idx() < prop.realCardsLen) {
                    x[idx++] = currentAction == action ? 0.6f : -0.6f;
                }
            }
        }
        if (actionCtx == GameActionCtx.SELECT_CARD_1_OUT_OF_3) {
            x[idx + (select1OutOf3CardsIdxes & 255)] = 1;
            x[idx + ((select1OutOf3CardsIdxes >> 8) & 255)] = 1;
            x[idx + ((select1OutOf3CardsIdxes >> 16) & 255)] = 1;
        }
        idx += prop.select1OutOf3CardsIdxes.length;
        var enemyOrder = getEnemyOrder();
        for (int enemyIdx = 0; enemyIdx < enemies.size(); enemyIdx++) {
            var enemy = enemies.get(enemyOrder != null ? enemyOrder[enemyIdx] : enemyIdx);
            if (enemy.isAlive()) {
                x[idx++] = enemy.getHealth() / (float) enemy.property.maxHealth;
                if (prop.enemyCanGetVuln) {
                    x[idx++] = enemy.getVulnerable() / (float) 10.0;
                }
                if (prop.enemyCanGetWeakened) {
                    x[idx++] = enemy.getWeak() / (float) 10.0;
                }
                if (prop.enemyStrengthEotCanChange) {
                    x[idx++] = enemy.getLoseStrengthEot() / (float) 20.0;
                }
                if (enemy.property.canGainStrength || prop.enemyStrengthCanChange) {
                    x[idx++] = enemy.getStrength() / (float) 20.0;
                }
                if (enemy.property.canGainBlock) {
                    x[idx++] = enemy.getBlock() / (float) 20.0;
                }
                if (enemy.property.hasArtifact) {
                    x[idx++] = enemy.getArtifact() / 3.0f;
                }
                if (enemy.property.canGainRegeneration) {
                    x[idx++] = enemy.getRegeneration() / (float) 10.0;
                }
                if (enemy.property.canGainMetallicize) {
                    x[idx++] = enemy.getMetallicize() / (float) 14.0;
                }
                for (int i = 0; i < enemy.property.numOfMoves; i++) {
                    if (enemy.getMove() == i) {
                        x[idx++] = 0.5f;
                    } else {
                        x[idx++] = -0.5f;
                    }
                }
                if (enemy.property.useLast2MovesForMoveSelection) {
                    for (int i = 0; i < enemy.property.numOfMoves; i++) {
                        if (enemy.getLastMove() == i) {
                            x[idx++] = 0.5f;
                        } else {
                            x[idx++] = -0.5f;
                        }
                    }
                }
                idx += enemy.writeNNInput(prop, x, idx);
                if (enemy instanceof Enemy.RedLouse louse) {
                    x[idx++] = (louse.getCurlUpAmount() - 10) / 2.0f;
                } else if (enemy instanceof Enemy.GreenLouse louse) {
                    x[idx++] = (louse.getCurlUpAmount() - 10) / 2.0f;
                }
            } else {
                x[idx++] = enemy.getHealth() / (float) enemy.property.maxHealth;
                if (prop.enemyCanGetVuln) {
                    x[idx++] = -0.1f;
                }
                if (prop.enemyCanGetWeakened) {
                    x[idx++] = -0.1f;
                }
                if (prop.enemyStrengthEotCanChange) {
                    x[idx++] = -0.1f;
                }
                if (enemy.property.canGainStrength || prop.enemyStrengthCanChange) {
                    x[idx++] = -0.1f;
                }
                if (enemy.property.canGainBlock) {
                    x[idx++] = -0.1f;
                }
                if (enemy.property.hasArtifact) {
                    x[idx++] = -0.1f;
                }
                if (enemy.property.canGainRegeneration) {
                    x[idx++] = -0.1f;
                }
                if (enemy.property.canGainMetallicize) {
                    x[idx++] = -0.1f;
                }
                for (int i = 0; i < enemy.property.numOfMoves; i++) {
                    x[idx++] = -0.1f;
                }
                if (enemy.property.useLast2MovesForMoveSelection) {
                    for (int i = 0; i < enemy.property.numOfMoves; i++) {
                        x[idx++] = -0.1f;
                    }
                }
                for (int i = 0; i < enemy.getNNInputLen(prop); i++) {
                    x[idx++] = -0.1f;
                }
                if (enemy instanceof Enemy.RedLouse || enemy instanceof Enemy.GreenLouse) {
                    x[idx++] = -0.1f;
                }
            }
        }
        return x;
    }

    public int[] getEnemyOrder() {
        if (prop.enemiesReordering == null) {
            return null;
        }
        int[] order = new int[enemies.size()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        for (var reordering : prop.enemiesReordering) {
            reordering.accept(this, order);
        }
        return order;
    }

    void addGameActionToEndOfDeque(GameEnvironmentAction action) {
        if (gameActionDeque == null) {
            gameActionDeque = new CircularArray<>();
        }
        gameActionDeque.addLast(action);
    }

    void addGameActionToStartOfDeque(GameEnvironmentAction action) {
        if (gameActionDeque == null) {
            gameActionDeque = new CircularArray<>();
        }
        gameActionDeque.addFirst(action);
    }

    void exhaustCardFromHand(int cardIdx) {
        assert hand[cardIdx] > 0;
        hand[cardIdx] -= 1;
        exhaustedCardHandle(cardIdx, false);
    }

    void exhaustedCardHandle(int cardIdx, boolean fromCardPlay) {
        if (cardIdx >= prop.realCardsLen) {
            cardIdx = prop.tmpCostCardIdxes[cardIdx];
        }
        if (fromCardPlay && prop.hasStrangeSpoon && getSearchRandomGen().nextBoolean(RandomGenCtx.Misc)) {
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
            deckArr[deckArrLen] = (short) card_idx;
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
        for (short i = 0; i < discard.length; i++) {
            for (int j = 0; j < discard[i]; j++) {
                deckArr[deckArrLen++] = i;
            }
            deck[i] = discard[i];
            discard[i] = 0;
        }
    }

    public String getActionString(GameAction action) {
        if (action.type() == GameActionType.BEGIN_BATTLE) {
            return "Begin Battle";
        } else if (action.type() == GameActionType.PLAY_CARD) {
            return prop.cardDict[action.idx()].cardName;
        } else if (action.type() == GameActionType.END_TURN) {
            return "End Turn";
        } else if (action.type() == GameActionType.SELECT_ENEMY) {
            if (enemiesAlive > 1) {
                return "Select " + enemies.get(action.idx()).getName() + "(" + action.idx() + ")";
            } else {
                return "Select " + enemies.get(action.idx()).getName();
            }
        } else if (action.type() == GameActionType.SELECT_CARD_HAND) {
            return "Select " + prop.cardDict[action.idx()].cardName + " From Hand";
        } else if (action.type() == GameActionType.SELECT_CARD_DISCARD) {
            return "Select " + prop.cardDict[action.idx()].cardName + " From Discard";
        } else if (action.type() == GameActionType.SELECT_CARD_EXHAUST) {
            return "Select " + prop.cardDict[action.idx()].cardName + " From Exhaust";
        } else if (action.type() == GameActionType.SELECT_CARD_1_OUT_OF_3) {
            return "Select " + prop.cardDict[action.idx()].cardName + " Out Of 3";
        } else if (action.type() == GameActionType.USE_POTION) {
            return prop.potions.get(action.idx()).toString();
        } else if (action.type() == GameActionType.SELECT_SCENARIO) {
            return prop.preBattleGameScenariosList.get(action.idx()).getValue().desc();
        } else if (action.type() == GameActionType.BEGIN_TURN) {
            return "Begin Turn";
        }
        return "Unknown";
    }

    public String getActionString(int i) {
        return getActionString(getAction(i));
    }

    public GameAction getAction(int i) {
        return prop.actionsByCtx[actionCtx.ordinal()][getLegalActions()[i]];
    }

    public void addStartOfBattleHandler(GameEventHandler handler) {
        prop.startOfBattleHandlers.add(handler);
    }

    public void addStartOfBattleHandler(String handlerName, GameEventHandler handler) {
        if (prop.gameEventHandlers.get(handlerName + "StartOfBattler") == null) {
            prop.gameEventHandlers.put(handlerName + "StartOfBattler", handler);
            prop.startOfBattleHandlers.add(handler);
        }
    }
    public void addEndOfBattleHandler(GameEventHandler handler) {
        prop.endOfBattleHandlers.add(handler);
    }

    public void addEndOfBattleHandler(String handlerName, GameEventHandler handler) {
        if (prop.gameEventHandlers.get(handlerName + "EndOfGame") == null) {
            prop.gameEventHandlers.put(handlerName + "EndOfGame", handler);
            prop.endOfBattleHandlers.add(handler);
        }
    }

    public void addStartOfTurnHandler(GameEventHandler handler) {
        prop.startOfTurnHandlers.add(handler);
    }

    public void addStartOfTurnHandler(String handlerName, GameEventHandler handler) {
        if (prop.gameEventHandlers.get(handlerName + "StartOfTurn") == null) {
            prop.gameEventHandlers.put(handlerName + "StartOfTurn", handler);
            prop.startOfTurnHandlers.add(handler);
        }
    }

    public void addPreEndOfTurnHandler(GameEventHandler handler) {
        prop.preEndTurnHandlers.add(handler);
    }

    public void addPreEndOfTurnHandler(String handlerName, GameEventHandler handler) {
        if (prop.gameEventHandlers.get(handlerName + "PreEndOfTurn") == null) {
            prop.gameEventHandlers.put(handlerName + "PreEndOfTurn", handler);
            prop.preEndTurnHandlers.add(handler);
        }
    }

    public void addOnExhaustHandler(GameEventHandler handler) {
        prop.onExhaustHandlers.add(handler);
    }

    public void addOnExhaustHandler(String handlerName, GameEventHandler handler) {
        if (prop.gameEventHandlers.get(handlerName + "OnExhaust") == null) {
            prop.gameEventHandlers.put(handlerName + "OnExhaust", handler);
            prop.onExhaustHandlers.add(handler);
        }
    }

    public void addOnBlockHandler(GameEventHandler handler) {
        prop.onBlockHandlers.add(handler);
    }

    public void addOnBlockHandler(String handlerName, GameEventHandler handler) {
        if (prop.gameEventHandlers.get(handlerName + "OnBlock") == null) {
            prop.gameEventHandlers.put(handlerName + "OnBlock", handler);
            prop.onBlockHandlers.add(handler);
        }
    }

    public void addOnEnemyDeathHandler(GameEventHandler handler) {
        prop.onEnemyDeathHandlers.add(handler);
    }

    public void addOnEnemyDeathHandler(String handlerName, GameEventHandler handler) {
        if (prop.gameEventHandlers.get(handlerName + "OnEnemyDeath") == null) {
            prop.gameEventHandlers.put(handlerName + "OnEnemyDeath", handler);
            prop.onEnemyDeathHandlers.add(handler);
        }
    }


    public void addOnDamageHandler(OnDamageHandler handler) {
        prop.onDamageHandlers.add(handler);
    }

    public void addOnDamageHandler(String handlerName, OnDamageHandler handler) {
        if (prop.gameEventHandlers.get(handlerName + "OnDamage") == null) {
            prop.gameEventHandlers.put(handlerName + "OnDamage", handler);
            prop.onDamageHandlers.add(handler);
        }
    }

    public void addOnHealHandler(OnDamageHandler handler) {
        prop.onHealHandlers.add(handler);
    }

    public void addOnHealHandler(String handlerName, OnDamageHandler handler) {
        if (prop.gameEventHandlers.get(handlerName + "OnHeal") == null) {
            prop.gameEventHandlers.put(handlerName + "OnHeal", handler);
            prop.onHealHandlers.add(handler);
        }
    }

    public void addOnCardPlayedHandler(GameEventCardHandler handler) {
        prop.onCardPlayedHandlers.add(handler);
    }

    public void addOnCardPlayedHandler(String handlerName, GameEventCardHandler handler) {
        if (prop.gameEventHandlers.get(handlerName + "OnCardPlayed") == null) {
            prop.gameEventHandlers.put(handlerName + "OnCardPlayed", handler);
            prop.onCardPlayedHandlers.add(handler);
        }
    }

    public void addOnCardDrawnHandler(GameEventCardHandler handler) {
        prop.onCardDrawnHandlers.add(handler);
    }

    public void addOnCardDrawnHandler(String handlerName, GameEventCardHandler handler) {
        if (prop.gameEventHandlers.get(handlerName + "OnCardDrawn") == null) {
            prop.gameEventHandlers.put(handlerName + "OnCardDrawn", handler);
            prop.onCardDrawnHandlers.add(handler);
        }
    }

    public void clearNextStates() { // oom during training due to holding too many states
        if (ns != null) {
            Arrays.fill(ns, null);
        }
        transpositions = new HashMap<>();
        if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH) transpositionsParent = new HashMap<>();
        searchFrontier = null;
    }

    public void clearAllSearchInfo() {
        policy = null;
        v_health = 0;
        v_win = 0;
        v_other = null;
        q_comb = null;
        q_health = null;
        q_win = null;
        q_other = null;
        total_q_comb = 0;
        total_q_win = 0;
        total_q_health = 0;
        total_q_other = null;
        n = null;
        ns = null;
        total_n = 0;
        transpositions = new HashMap<>();
        if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH) transpositionsParent = new HashMap<>();
        legalActions = null;
        terminal_action = -100;
        searchFrontier = null;
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
            addCardToDiscard(idx);
        } else {
            hand[idx]++;
        }
    }

    public void removeCardFromHand(int cardIndex) {
        if (hand[cardIndex] > 0) {
            hand[cardIndex]--;
        }
    }

    public void addCardToDiscard(int cardIndex) {
        discard[cardIndex]++;
    }

    public void removeCardFromDiscard(int cardIndex) {
        discard[cardIndex]--;
    }

    public void addCardToDeck(int idx) {
        deck[idx]++;
        deckArr[deckArrLen++] = (short) idx;
    }

    public void removeCardFromDeck(int cardIndex) {
        if (deck[cardIndex] > 0) {
            deck[cardIndex]--;
            for (int i = 0; i < deckArrLen; i++) {
                if (deckArr[i] == cardIndex) {
                    var tmp = deckArr[i];
                    deckArr[i] = deckArr[deckArrLen - 1];
                    deckArr[deckArrLen - 1] = tmp;
                    deckArrLen -= 1;
                    break;
                }
            }
        }
    }

    public void setCardCountInDeck(int cardIndex, int count) {
        if (deck[cardIndex] > count) {
            for (int i = deck[cardIndex]; i > count ; i--) {
                removeCardFromDeck(cardIndex);
            }
        } else {
            for (int i = deck[cardIndex]; i < count ; i++) {
                addCardToDeck(cardIndex);
            }
        }
    }

    public void setCardCountInDiscard(int cardIndex, int count) {
        discard[cardIndex] = (byte) count;
    }

    public void setCardCountInHand(int cardIndex, int count) {
        hand[cardIndex] = (byte) count;
    }

    public void putCardOnTopOfDeck(int idx) {
        deck[idx]++;
        deckArr[deckArrLen++] = (short) idx;
        getDrawOrderForWrite().pushOnTop(idx);
    }

    public boolean playerDoDamageToEnemy(Enemy enemy, int dmg) {
        var player = getPlayeForRead();
        if ((buffs & PlayerBuff.AKABEKO.mask()) != 0) {
            dmg += 8;
        }
        dmg += player.getStrength();
        if (prop.penNibCounterIdx >= 0 && counter[prop.penNibCounterIdx] == 9) {
            dmg *= 2;
        }
        if (enemy.getVulnerable() > 0) {
            dmg = dmg + (prop.hasPaperPhrog ? dmg * 3 / 4 : dmg / 2);
        }
        if (player.getWeak() > 0) {
            dmg = dmg * 3 / 4;
        }
        if (prop.hasBoot && dmg < 5) {
            dmg = 5;
        }
        if (enemy.isAlive()) {
            enemy.damage(dmg, this);
            if (!enemy.isAlive()) {
                adjustEnemiesAlive(-1);
                for (GameEventHandler handler : prop.onEnemyDeathHandlers) {
                    handler.handle(this);
                }
                return true;
            }
        }
        return false;
    }

    public void adjustEnemiesAlive(int count) {
        enemiesAlive += count;
        if (enemiesAlive == 0) {
            for (var handler : prop.endOfBattleHandlers) {
                handler.handle(this);
            }
        }
    }

    public void playerDoNonAttackDamageToEnemy(Enemy enemy, int dmg, boolean blockable) {
        if (enemy.isAlive()) {
            enemy.nonAttackDamage(dmg, blockable, this);
            if (!enemy.isAlive()) {
                adjustEnemiesAlive(-1);
                for (GameEventHandler handler : prop.onEnemyDeathHandlers) {
                    handler.handle(this);
                }
            }
        }
    }

    public int enemyDoDamageToPlayer(EnemyReadOnly enemy, int dmg, int times) {
        int move = enemy.getMove();
        int totalDmgDealt = 0;
        var player = getPlayerForWrite();
        dmg += enemy.getStrength();
        if (dmg < 0) {
            dmg = 0;
        }
        if (player.getVulnerable() > 0) {
            dmg = dmg + dmg / (prop.hasOddMushroom ? 4 : 2);
        }
        if (enemy.getWeak() > 0) {
            dmg = dmg * 3 / 4;
        }
        for (int i = 0; i < times; i++) {
            if (!enemy.isAlive() || enemy.getMove() != move) { // dead or interrupted
                return totalDmgDealt;
            }
            int dmgDealt = player.damage(dmg);
            totalDmgDealt += dmgDealt;
            if (dmgDealt >= 0) {
                for (OnDamageHandler handler : prop.onDamageHandlers) {
                    handler.handle(this, enemy, true, dmgDealt);
                }
            }
        }
        return totalDmgDealt;
    }

    public int enemyCalcDamageToPlayer(Enemy enemy, int dmg) {
        dmg += enemy.getStrength();
        if (dmg < 0) {
            dmg = 0;
        }
        if (player.getVulnerable() > 0) {
            dmg = dmg + dmg / (prop.hasOddMushroom ? 4 : 2);
        }
        if (enemy.getWeak() > 0) {
            dmg = dmg * 3 / 4;
        }
        return dmg;
    }

    public void doNonAttackDamageToPlayer(int dmg, boolean blockable, Object source) {
        var damageDealt = getPlayerForWrite().nonAttackDamage(dmg, blockable);
        if (dmg > 0) {
            for (OnDamageHandler handler : prop.onDamageHandlers) {
                handler.handle(this, source, false, damageDealt);
            }
        }
    }

    public void healPlayer(int hp) {
        var healed = getPlayerForWrite().heal(hp);
        if (healed > 0) {
            for (var handler : prop.onHealHandlers) {
                handler.handle(this, null, false, healed);
            }
        }
    }

    public byte[] getDeck() {
        return deck;
    }

    public byte[] getHand() {
        return hand;
    }

    public byte[] getDiscard() {
        return discard;
    }

    public byte[] getExhaustForRead() {
        return exhaust;
    }

    public byte[] getExhaustForWrite() {
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

    public EnemyListReadOnly getEnemiesForRead() {
//        if (!enemiesCloned) {
//            enemies = new EnemyList(enemies);
//            enemiesCloned = true;
//        }
        return enemies;
    }

    public EnemyList getEnemiesForWrite() {
        if (!enemiesCloned) {
            enemies = new EnemyList(enemies);
            enemiesCloned = true;
        }
        return enemies;
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

    public RandomGen getSearchRandomGen() {
        if (prop.makingRealMove || searchRandomGen == null) {
            return prop.realMoveRandomGen != null ? prop.realMoveRandomGen : prop.random;
        }
        if (COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION && !searchRandomGenCloned) {
            searchRandomGen = searchRandomGen.getCopy();
            searchRandomGenCloned = true;
        }
        return searchRandomGen;
    }

    public void setSearchRandomGen(RandomGen randomGen) {
        searchRandomGen = randomGen;
        searchRandomGenCloned = true;
    }

    public StringBuilder getStateDesc() {
        if (stateDesc == null) {
            stateDesc = new StringBuilder();
        }
        return stateDesc;
    }

    public String getStateDescStr() {
        return stateDesc == null ? "" : stateDesc.toString();
    }

    public void killEnemy(int i) {
        if (getEnemiesForRead().get(i).isAlive()) {
            getEnemiesForWrite().getForWrite(i).setHealth(0);
            adjustEnemiesAlive(-1);
        }
    }

    public void reviveEnemy(int idx) {
        if (!getEnemiesForRead().get(idx).isAlive()) {
            getEnemiesForWrite().replace(idx, prop.originalEnemies.getForWrite(idx));
            getEnemiesForWrite().getForWrite(idx).randomize(getSearchRandomGen(), false);
            getEnemiesForWrite().getForWrite(idx).nextMove(this, getSearchRandomGen());
            adjustEnemiesAlive(1);
        } else {
            Integer.parseInt(null);
        }
    }

    public boolean potionUsed(int i) {
        return potionsState[i * 3 + 2] == 1 && potionsState[i * 3] == 0;
    }

    public boolean hadPotion(int i) {
        return potionsState[i * 3 + 2] == 1;
    }

    public void setSelect1OutOf3Idxes(int idx1, int idx2, int idx3) {
        if (idx2 > idx3) {
            int tmp = idx2;
            idx2 = idx3;
            idx3 = tmp;
        }
        if (idx1 > idx3) {
            select1OutOf3CardsIdxes = (idx2 << 16) + (idx3 << 8) + idx1;
        } else if (idx1 > idx2) {
            select1OutOf3CardsIdxes = (idx2 << 16) + (idx1 << 8) + idx3;
        } else {
            select1OutOf3CardsIdxes = (idx1 << 16) + (idx2 << 8) + idx3;
        }
    }

    public double getVOther(int vArrayIdx) {
        return v_other[vArrayIdx];
    }

    public void setIsStochastic() {
        // todo: I think the battle ends immediately on enemies death, maybe only distilled
        // chaos/havoc with bird faced urn, or meat on the bone + e.g. guardian may heal/dmg?
        if (isTerminal() == 0) {
            isStochastic = true;
        }
    }

    // Defect specific
    public short[] getOrbs() {
        return orbs;
    }

    public void channelOrb(OrbType orb) {
        if (orbs == null) return;
        if (orbs[orbs.length - 1] != 0) {
            evokeOrb(1);
            orbs[orbs.length - 1] = (short) orb.ordinal();
        } else {
            for (int i = 0; i < orbs.length; i++) {
                if (orbs[i] == 0) {
                    orbs[i] = (short) orb.ordinal();
                    break;
                }
            }
        }
    }

    public void evokeOrb(int times) {
        if (orbs == null) return;
        for (int i = 0; i < times; i++) {
            if (orbs[0] == OrbType.FROST.ordinal()) {
                getPlayerForWrite().gainBlockNotFromCardPlay(5 + focus);
            } else if (orbs[0] == OrbType.LIGHTNING.ordinal()) {
                int idx = GameStateUtils.getRandomEnemyIdx(this, RandomGenCtx.RandomEnemyLightningOrb);
                if (idx >= 0) {
                    var enemy = getEnemiesForWrite().getForWrite(idx);
                    if ((prop.makingRealMove || prop.stateDescOn) && enemiesAlive > 1) {
                        getStateDesc().append(getStateDesc().length() > 0 ? "; " : "").append("Lightning Orb evoke hit ").append(enemy.getName() + " (" + idx + ")");
                    }
                    playerDoNonAttackDamageToEnemy(enemy, 8 + focus, true);
                }
            } else if (orbs[0] == OrbType.DARK.ordinal()) {
                // todo: implement dark orb
            } else if (orbs[0] == OrbType.PLASMA.ordinal()) {
                gainEnergy(2);
            }
        }
        System.arraycopy(orbs, 1, orbs, 0, orbs.length - 1);
        orbs[orbs.length - 1] = 0;
    }

    private void triggerOrbsPassiveEndOfTurn() {
        if (orbs == null) return;
        for (int i = 0; i < orbs.length; i++) {
            if (orbs[i] == OrbType.FROST.ordinal()) {
                getPlayerForWrite().gainBlockNotFromCardPlay(2 + focus);
            } else if (orbs[i] == OrbType.LIGHTNING.ordinal()) {
                int idx = GameStateUtils.getRandomEnemyIdx(this, RandomGenCtx.RandomEnemyLightningOrb);
                if (idx >= 0) {
                    var enemy = getEnemiesForWrite().getForWrite(idx);
                    if ((prop.makingRealMove || prop.stateDescOn) && enemiesAlive > 1) {
                        getStateDesc().append(getStateDesc().length() > 0 ? "; " : "").append("Lightning Orb passive hit ").append(enemy.getName() + " (" + idx + ")");
                    }
                    playerDoNonAttackDamageToEnemy(enemy, 3 + focus, true);
                }
            } else if (orbs[i] == OrbType.DARK.ordinal()) {
                // todo: implement dark orb
            }
        }
    }

    public void gainOrbSlot(int n) {
        if (orbs == null) {
            return;
        }
        if (n < 0) {
            if (orbs.length + n == 0) {
                orbs = null;
            } else {
                orbs = Arrays.copyOf(orbs, orbs.length + n);
            }
        } else {
            var newOrbs = new short[orbs.length + 1];
            System.arraycopy(newOrbs, 0, orbs, 0, orbs.length);
            orbs = newOrbs;
        }
    }

    public void gainFocus(int n) {
        focus += n;
    }

    public short getFocus() {
        return focus;
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
    double varianceM;
    double varianceS;
    List<GameState> queue;
    GameState parentState;
    int parentAction;
    RandomGen searchRandomGen;

    // GameSolver only
    BigRational e_health = BigRational.ZERO;
    BigRational e_win = BigRational.ZERO;

    public ChanceState(GameState initState, GameState parentState, int action) {
        cache = new Hashtable<>();
        if (initState != null) {
            cache.put(initState, new Node(initState));
            total_node_n = 1;
        }
        if (GameState.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION && initState != null) {
            if (Configuration.NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION && (!Configuration.TEST_NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION || parentState.prop.testNewFeature)) {
                searchRandomGen = initState.getSearchRandomGen().getCopy();
            } else {
                searchRandomGen = initState.getSearchRandomGen().createWithSeed(initState.getSearchRandomGen().nextLong(RandomGenCtx.CommonNumberVR));
            }
        } else {
            searchRandomGen = parentState.prop.random;
        }
        this.parentState = parentState;
        this.parentAction = action;
        var tmpQueue = new ArrayList<GameState>();
//        if (parentState.prop.testNewFeature) {
//            for (int i = initState != null ? 1 : 0; i < 0; i++) {
//                tmpQueue.add(getNextState(false));
//            }
//        }
        total_n = initState != null ? 1 : 0;
        queue = tmpQueue;
    }

    public void addToQueue(GameState state) {
        addGeneratedState(state);
        total_n -= 1;
        queue.add(state);
    }

    // currently doesn't do anything
    public void correctV(GameState state2, double[] v) {
        var newVarianceM = varianceM + (v[GameState.V_COMB_IDX] - varianceM) / (total_n + 1);
        var newVarianceS = varianceS + (v[GameState.V_COMB_IDX] - varianceM) * (v[GameState.V_COMB_IDX] - newVarianceM);
        varianceM = newVarianceM;
        varianceS = newVarianceS;
        var node = cache.get(state2);
        if (node.revisit) {
            var new_total_q_comb = 0.0;
            var new_total_q_win = 0.0;
            var new_total_q_health = 0.0;
            var nnn = 0;
            for (Map.Entry<GameState, Node> entry : cache.entrySet()) {
                new_total_q_comb += entry.getValue().state.total_q_comb / (entry.getValue().state.total_n + 1) * entry.getValue().n;
                new_total_q_win += entry.getValue().state.total_q_win / (entry.getValue().state.total_n + 1) * entry.getValue().n;
                new_total_q_health += entry.getValue().state.total_q_health / (entry.getValue().state.total_n + 1) * entry.getValue().n;
                nnn += entry.getValue().n;
            }
            new_total_q_comb = new_total_q_comb / nnn * total_n;
            new_total_q_win = new_total_q_win / nnn * total_n;
            new_total_q_health = new_total_q_health / nnn * total_n;
            v[GameState.V_WIN_IDX] = new_total_q_win - total_q_win;
            v[GameState.V_HEALTH_IDX] = new_total_q_health - total_q_health;
            v[GameState.V_COMB_IDX] = new_total_q_comb - total_q_comb;

//            var node_cur_q_comb = node.state.total_q_comb / (node.state.total_n + 1);
//            var node_cur_q_win = node.state.total_q_win / (node.state.total_n + 1);
//            var node_cur_q_health = node.state.total_q_health / (node.state.total_n + 1);
//            var prev_q_comb = total_q_comb / (total_n - 1);
//            var prev_q_win = total_q_win / (total_n - 1);
//            var prev_q_health = total_q_health / (total_n - 1);
//            var new_q_comb = prev_q_comb - node.prev_q_comb * node.n / total_node_n + node_cur_q_comb * node.n / total_node_n ;
//            var new_q_win = prev_q_win - node.prev_q_win * node.n / total_node_n + node_cur_q_win * node.n / total_node_n ;
//            var new_q_health = prev_q_health - node.prev_q_health * node.n / total_node_n +  node_cur_q_health * node.n / total_node_n ;
//            node.prev_q_comb = node_cur_q_comb;
//            node.prev_q_win = node_cur_q_win;
//            node.prev_q_health = node_cur_q_health;
//            v[GameState.V_COMB_IDX] = new_q_comb * total_n - total_q_comb;
//            v[GameState.V_WIN_IDX] = new_q_win * total_n - total_q_win;
//            v[GameState.V_HEALTH_IDX] = new_q_health * total_n - total_q_health;
//            if (new_q_win * total_n - (total_q_win + v[GameState.V_WIN_IDX]) > 0.00000001) {
//                System.out.println(prev_q_win + "," + total_n + "," + total_q_win / total_n);
//            }
        } else {
            if ((Configuration.USE_PROGRESSIVE_WIDENING && (!Configuration.TEST_PROGRESSIVE_WIDENING || parentState.prop.testNewFeature)) ||
                    (Configuration.TRANSPOSITION_ACROSS_CHANCE_NODE && (!Configuration.TEST_TRANSPOSITION_ACROSS_CHANCE_NODE || parentState.prop.testNewFeature)) ||
                    (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || parentState.prop.testNewFeature))) {
                var new_total_q_comb = 0.0;
                var new_total_q_win = 0.0;
                var new_total_q_health = 0.0;
                var nnn = 0;
                for (Map.Entry<GameState, Node> entry : cache.entrySet()) {
                    new_total_q_comb += entry.getValue().state.total_q_comb / (entry.getValue().state.total_n + 1) * entry.getValue().n;
                    new_total_q_win += entry.getValue().state.total_q_win / (entry.getValue().state.total_n + 1) * entry.getValue().n;
                    new_total_q_health += entry.getValue().state.total_q_health / (entry.getValue().state.total_n + 1) * entry.getValue().n;
                    nnn += entry.getValue().n;
                }
                new_total_q_comb = new_total_q_comb / nnn * total_n;
                new_total_q_win = new_total_q_win / nnn * total_n;
                new_total_q_health = new_total_q_health / nnn * total_n;
                v[GameState.V_WIN_IDX] = new_total_q_win - total_q_win;
                v[GameState.V_HEALTH_IDX] = new_total_q_health - total_q_health;
                v[GameState.V_COMB_IDX] = new_total_q_comb - total_q_comb;

//                var node_cur_q_comb = node.state.total_q_comb / (node.state.total_n + 1);
//                var node_cur_q_win = node.state.total_q_win / (node.state.total_n + 1);
//                var node_cur_q_health = node.state.total_q_health / (node.state.total_n + 1);
//                var prev_q_comb = total_n == 1 ? 0 : total_q_comb / (total_n - 1);
//                var prev_q_win = total_n == 1 ? 0 : total_q_win / (total_n - 1);
//                var prev_q_health = total_n == 1 ? 0 :total_q_health / (total_n - 1);
//                var new_q_comb = (prev_q_comb * (total_node_n - 1) - node.prev_q_comb * (node.n - 1))  / total_node_n + node_cur_q_comb * node.n / total_node_n;
//                var new_q_win = (prev_q_win * (total_node_n - 1) - node.prev_q_win * (node.n - 1)) / total_node_n + node_cur_q_win * node.n / total_node_n;
//                var new_q_health = (prev_q_health * (total_node_n - 1) - node.prev_q_health * (node.n - 1)) / total_node_n + node_cur_q_health * node.n / total_node_n;
//                node.prev_q_comb = node_cur_q_comb;
//                node.prev_q_win = node_cur_q_win;
//                node.prev_q_health = node_cur_q_health;
//                v[GameState.V_COMB_IDX] = new_q_comb * total_n - total_q_comb;
//                v[GameState.V_WIN_IDX] = new_q_win * total_n - total_q_win;
//                v[GameState.V_HEALTH_IDX] = new_q_health * total_n - total_q_health;
//                            if (new_q_win * total_n - (total_q_win + v[GameState.V_WIN_IDX]) > 0.00000001) {
//                                System.out.println(prev_q_win + "," + total_n + "," + total_q_win / total_n);
//                            }
            }
        }
        total_q_comb += v[GameState.V_COMB_IDX];
        total_q_win += v[GameState.V_WIN_IDX];
        total_q_health += v[GameState.V_HEALTH_IDX];
    }

    GameState getNextState(boolean calledFromMCTS) {
        total_n += 1;
        if (queue != null && queue.size() > 0) {
            return queue.remove(0);
        }
        if (calledFromMCTS && cache.size() >= Math.ceil(Math.sqrt(total_n)) && Configuration.USE_PROGRESSIVE_WIDENING && (!Configuration.TEST_PROGRESSIVE_WIDENING || parentState.prop.testNewFeature)) {
            // instead of generating new nodes, revisit node, need testing
            var r = (long) searchRandomGen.nextInt((int) total_node_n, RandomGenCtx.Other, this);
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
        var state = parentState.clone(true);
        if (Configuration.NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION && (!Configuration.TEST_NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION || parentState.prop.testNewFeature)) {
            if (!parentState.prop.makingRealMove && GameState.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION) {
                state.setSearchRandomGen(searchRandomGen);
            }
        } else {
            if (!parentState.prop.makingRealMove && GameState.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION) {
                state.setSearchRandomGen(searchRandomGen);
            }
        }
        state.doAction(parentAction);
        if (state.actionCtx == GameActionCtx.BEGIN_TURN) {
            state.doAction(0);
        }
        if (Configuration.NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION && (!Configuration.TEST_NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION || parentState.prop.testNewFeature)) {
            if (!parentState.prop.makingRealMove && GameState.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION) {
                searchRandomGen = searchRandomGen.getCopy();
            }
        } else {
            if (!parentState.prop.makingRealMove && GameState.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION) {
                searchRandomGen = state.getSearchRandomGen().createWithSeed(state.getSearchRandomGen().nextLong(RandomGenCtx.CommonNumberVR));
            }
        }
        if (parentState.prop.makingRealMove && Configuration.NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION) {
            state.setSearchRandomGen(state.getSearchRandomGen().createWithSeed(state.getSearchRandomGen().nextLong(RandomGenCtx.CommonNumberVR)));
            total_n -= 1;
            return state;
        }
        total_node_n += 1;
        var node = cache.get(state);
        if (node != null) {
            node.n += 1;
            node.revisit = false;
            if (node.state.stateDesc == null && state.stateDesc != null) {
                node.state.stateDesc = state.stateDesc;
            }
            if (parentState.prop.makingRealMove && GameState.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION) {
               node.state.setSearchRandomGen(state.getSearchRandomGen().createWithSeed(state.getSearchRandomGen().nextLong(RandomGenCtx.CommonNumberVR)));
            }
            return node.state;
        }
        cache.put(state, new Node(state));
        if (parentState.prop.makingRealMove && GameState.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION) {
           state.setSearchRandomGen(state.getSearchRandomGen().createWithSeed(state.getSearchRandomGen().nextLong(RandomGenCtx.CommonNumberVR)));
        }
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
        return "ChanceState{state=" + parentState + ", action=" + parentAction + "}";
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChanceState that = (ChanceState) o;
        return parentAction == that.parentAction && Objects.equals(parentState, that.parentState);
    }

    @Override public int hashCode() {
        return Objects.hash(parentState, parentAction);
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
