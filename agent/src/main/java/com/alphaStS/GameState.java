package com.alphaStS;

import com.alphaStS.action.GameEnvironmentAction;
import com.alphaStS.card.*;
import com.alphaStS.enemy.*;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.enums.OrbType;
import com.alphaStS.enums.Stance;
import com.alphaStS.model.Model;
import com.alphaStS.model.NNOutput;
import com.alphaStS.player.Player;
import com.alphaStS.player.PlayerReadOnly;
import com.alphaStS.utils.*;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static com.alphaStS.utils.Utils.formatFloat;

public final class GameState implements State {
    public static final int HAND_LIMIT = 10;
    private static final int MAX_AGENT_DECK_ORDER_MEMORY = 1;
    private static final int MAX_AGENT_DECK_ORDER_MEMORY_DUPLICATE_CARDS = 1;
    public static final int V_COMB_IDX = 0;
    public static final int V_WIN_IDX = 1;
    public static final int V_HEALTH_IDX = 2;
    public static final int V_EXTRA_IDX_START = 3;

    public static final int DISCARD = 1;
    public static final int DECK = 2;
    public static final int EXHAUST = 3;

    public boolean isStochastic;
    StringBuilder stateDesc;
    public GameProperties properties;
    public GameActionCtx actionCtx;
    Class actionCardCloneSource;

    public short[] handArr;
    public int handArrLen;
    public short[] discardArr;
    public int discardArrLen;
    public short[] deckArr;
    public int deckArrLen;
    public int deckArrFixedDrawLen;
    public short[] exhaustArr;
    public int exhaustArrLen;
    public short[] chosenCardsArr; // well laid plans, todo: gambler's potion? to know about any potential discard effect
    public short chosenCardsArrLen;
    public short[] nightmareCards;
    public short nightmareCardsLen;
    private boolean deckCloned;
    private boolean handCloned;
    private boolean discardCloned;
    private boolean exhaustCloned;
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
    boolean potionsStateCloned;
    short[] potionsState;
    public boolean searchRandomGenCloned;
    public RandomGen searchRandomGen;

    CircularArray<GameEnvironmentAction> gameActionDeque;
    public int energy;
    public int energyRefill;
    GameAction currentAction;
    public short turnNum;
    public short realTurnNum;
    public short actionsInCurTurn;
    int playerTurnStartMaxPossibleHealth;
    byte playerTurnStartPotionCount;
    byte playerTurnStartMaxHandOfGreed;
    byte playerTurnStartMaxRitualDagger;
    public int preBattleRandomizationIdxChosen = -1;
    public int preBattleScenariosChosenIdx = -1;
    public int battleRandomizationIdxChosen = -1;
    public int startOfBattleActionIdx = 0;
    public boolean skipInteractiveModeSetup;
    public EnemyEncounter.EncounterEnum currentEncounter = EnemyEncounter.EncounterEnum.UNKNOWN;

    // various other buffs/debuffs
    public long buffs;

    // Defect specific
    private short[] orbs;
    private short focus;

    // Watcher specific
    private Stance stance = Stance.NEUTRAL;
    public boolean[] scryCardIsKept;
    public int scryCurrentCount;
    private int lastCardPlayedType = -1; // -1 means no card played yet, otherwise Card.ATTACK, Card.SKILL, etc.

    // search related fields
    private int[] legalActions;
    float v_win; // if terminal, 1.0 or -1.0, else from NN
    float v_health; // if terminal, player_health/player_max_health, else from NN
    float[] v_extra; // set by the extra TrainingTarget
    double varianceM;
    double varianceS;
    private VArray q_total; // sum of q values
    private VArray[] q_child; // q values for each legal action
    int[] n; // visit count for each child
    State[] ns; // the state object for each child (either GameState or ChanceState)
    int total_n; // sum of n array
    float[] policy; // policy from NN
    float[] policyMod; // used in training (with e.g. Dirichlet noise applied or futile pruning applied)
    Map<GameState, State> transpositions; // detect transposition within a "deterministic turn" (i.e. no stochastic transition occurred like drawing)
    Map<GameState, List<Tuple<GameState, Integer>>> transpositionsParent;
    int terminalAction; // detected a win from child, no need to waste more time search
    SearchFrontier searchFrontier;
    ReentrantReadWriteLock lock;
    ReentrantLock transpositionsLock;
    AtomicInteger virtualLoss;

    boolean[] bannedActions;

    public void readLock() {
        lock.readLock().lock();
    }

    public void readUnlock() {
        lock.readLock().unlock();
    }

    public void writeLock() {
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    public void setMultithreaded(boolean multithreaded) {
        if (multithreaded) {
            properties.multithreadedMTCS = true;
            virtualLoss = new AtomicInteger();
            lock = new ReentrantReadWriteLock();
            transpositionsLock = new ReentrantLock();
        } else {
            properties.multithreadedMTCS = false;
        }
    }

    private boolean cardIdxArrEqual(short[] a, int aLen, short[] b, int bLen) {
        if (a == b) {
            return true;
        }
        if (aLen != bLen) {
            return false;
        }
        if (aLen == 0) {
            return true;
        }
        byte[] count = new byte[properties.cardDict.length];
        for (int i = 0; i < aLen; i++) {
            count[a[i]]++;
        }
        for (int i = 0; i < bLen; i++) {
            count[b[i]]--;
            if (count[b[i]] < 0) {
                return false;
            }
        }
        return true;
    }

    private short[] cardIdxArrAdd(short[] cardIdxArr, boolean clone, int len, int idx) {
        if (len == cardIdxArr.length) {
            cardIdxArr = Arrays.copyOf(cardIdxArr, len + (len + 2) / 2);
        } else if (clone) {
            cardIdxArr = Arrays.copyOf(cardIdxArr, cardIdxArr.length);
        }
        cardIdxArr[len] = (short) idx;
        return cardIdxArr;
    }

    public void handArrTransform(int[] transformIdxes) {
        for (int i = 0; i < handArrLen; i++) {
            if (transformIdxes[handArr[i]] >= 0) {
                getHandArrForWrite()[i] = (short) transformIdxes[handArr[i]];
            }
        }
    }

    public void discardArrTransform(int[] transformIdxes) {
        for (int i = 0; i < discardArrLen; i++) {
            if (transformIdxes[discardArr[i]] >= 0) {
                getDiscardArrForWrite()[i] = (short) transformIdxes[discardArr[i]];
            }
        }
    }

    public void deckArrTransform(int[] transformIdxes) {
        for (int i = 0; i < deckArrLen; i++) {
            if (transformIdxes[deckArr[i]] >= 0) {
                getDeckArrForWrite()[i] = (short) transformIdxes[deckArr[i]];
            }
        }
    }

    public void exhaustArrTransform(int[] transformIdxes) {
        for (int i = 0; i < exhaustArrLen; i++) {
            if (transformIdxes[exhaustArr[i]] >= 0) {
                getExhaustArrForWrite()[i] = (short) transformIdxes[exhaustArr[i]];
            }
        }
    }

    public void addNightmareCard(int idx) {
        if (idx >= properties.realCardsLen) {
            nightmareCards = cardIdxArrAdd(nightmareCards, true, nightmareCardsLen, properties.tmpModifiedCardReverseTransformIdxes[idx]);
        } else {
            nightmareCards = cardIdxArrAdd(nightmareCards, true, nightmareCardsLen, idx);
        }
        nightmareCards = cardIdxArrAdd(nightmareCards, true, nightmareCardsLen, idx);
        nightmareCardsLen++;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GameState gameState = (GameState) o;
        if (energy != gameState.energy) return false;
        if (energyRefill != gameState.energyRefill) return false;
        if (enemiesAlive != gameState.enemiesAlive) return false;
        if (currentAction != gameState.currentAction) return false;
        if (buffs != gameState.buffs) return false;
        if (select1OutOf3CardsIdxes != gameState.select1OutOf3CardsIdxes) return false;
        if (!Arrays.equals(counter, gameState.counter)) return false;
        if (actionCtx != gameState.actionCtx) return false;
        if (actionCardCloneSource != gameState.actionCardCloneSource) return false;
        if (!cardIdxArrEqual(handArr, handArrLen, gameState.handArr, gameState.handArrLen)) return false;
        if (!cardIdxArrEqual(discardArr, discardArrLen, gameState.discardArr, gameState.discardArrLen)) return false;
        if (!cardIdxArrEqual(deckArr, deckArrLen, gameState.deckArr, gameState.deckArrLen)) return false;
        if (deckArrFixedDrawLen != gameState.deckArrFixedDrawLen) return false;
        if (!Utils.equals(deckArr, gameState.deckArr, deckArrFixedDrawLen)) return false;
        if (!cardIdxArrEqual(exhaustArr, exhaustArrLen, gameState.exhaustArr, gameState.exhaustArrLen)) return false;
        if (false && properties.discard0CardOrderMatters) {
            if (!cardArray0CostCardOrderEquals(handArr, gameState.handArr, handArrLen)) return false;
            if (!cardArray0CostCardOrderEquals(discardArr, gameState.discardArr, discardArrLen)) return false;
        }
        if (!cardIdxArrEqual(chosenCardsArr, chosenCardsArrLen, gameState.chosenCardsArr, gameState.chosenCardsArrLen)) return false;
        if (!Objects.equals(enemies, gameState.enemies)) return false;
        if (!Objects.equals(player, gameState.player)) return false;
        if (!Objects.equals(drawOrder, gameState.drawOrder)) return false;
        if (!Arrays.equals(potionsState, gameState.potionsState)) return false;
        if (focus != gameState.focus) return false;
        if (stance != gameState.stance) return false;
        if (scryCurrentCount != gameState.scryCurrentCount) return false;
        if (!Utils.equals(scryCardIsKept, gameState.scryCardIsKept, scryCurrentCount)) return false;
        if (properties.previousCardPlayTracking) {
            for (int i = 0; i < handArrLen; i++) {
                if (properties.cardDict[handArr[i]].needsLastCardType) {
                    if (lastCardPlayedType != gameState.lastCardPlayedType) return false;
                    break;
                }
            }
        }
        if (!Arrays.equals(orbs, gameState.orbs)) return false;
        if (!cardIdxArrEqual(nightmareCards, nightmareCardsLen, gameState.nightmareCards, gameState.nightmareCardsLen)) return false;
        if (preBattleRandomizationIdxChosen != gameState.preBattleRandomizationIdxChosen) return false;
        if (preBattleScenariosChosenIdx != gameState.preBattleScenariosChosenIdx) return false;
        if (battleRandomizationIdxChosen != gameState.battleRandomizationIdxChosen) return false;
        if (startOfBattleActionIdx != gameState.startOfBattleActionIdx) return false;
        boolean dequeIsNull = gameActionDeque == null || gameActionDeque.size() == 0;
        boolean oDequeIsNull = gameState.gameActionDeque == null || gameState.gameActionDeque.size() == 0;
        if (dequeIsNull != oDequeIsNull) {
            return false;
        } else if (!dequeIsNull && !gameActionDeque.equals(gameState.gameActionDeque)) {
            return false;
        }
        return true;
    }

    private boolean cardArray0CostCardOrderEquals(short[] handArr1, short[] handArr2, int handArrLen) {
        int a = -1;
        int b = -1;
        while (true) {
            while (++a < handArrLen) {
                if (properties.cardDict[handArr1[a]].realEnergyCost() == 0) {
                    break;
                }
            }
            if (a == handArrLen) {
                break;
            }
            while (++b < handArrLen) {
                if (properties.cardDict[handArr2[b]].realEnergyCost() == 0) {
                    break;
                }
            }
            if (handArr1[a] != handArr2[b]) {
                return false;
            }
        }
        return true;
    }

    @Override public int hashCode() {
        // actionCtx, energy, energyRefill, hand, enemies health, previousCardIdx, drawOrder, buffs should cover most
        int result = Objects.hash(actionCtx, energy, energyRefill, currentAction, drawOrder, buffs);
        for (var enemy : enemies) {
            result = 31 * result + enemy.getHealth();
        }
        if (deckArrFixedDrawLen > 0) { // e.g. need for frozen eye or hash is not distributed evenly
            int[] tmp = new int[deckArrFixedDrawLen];
            for (int i = 0; i < deckArrFixedDrawLen; i++) {
                tmp[i] = deckArr[deckArrLen - 1 - i];
            }
            result = 31 * result + Arrays.hashCode(tmp);
        }
        if (actionCtx == GameActionCtx.SELECT_CARD_1_OUT_OF_3) {
            result = 31 * result + select1OutOf3CardsIdxes;
        }
        result = 31 * result + player.getHealth();
        result = 31 * result + Arrays.hashCode(GameStateUtils.getCardArrCounts(handArr, handArrLen, properties.cardDict.length));
        result = 31 * result + Arrays.hashCode(GameStateUtils.getCardArrCounts(deckArr, deckArrLen, properties.cardDict.length));
        result = 31 * result + Arrays.hashCode(GameStateUtils.getCardArrCounts(discardArr, discardArrLen, properties.realCardsLen));
        result = 31 * result + Arrays.hashCode(potionsState);
        if (properties.character == CharacterEnum.WATCHER) {
            result = 31 * result + stance.ordinal();
        }
        return result;
    }

    public GameState(GameStateBuilder builder) {
        builder.build(this);
        List<Enemy> enemiesArg = builder.getEnemies();
        Player player = builder.getPlayer();
        List<CardCount> cardCounts = builder.getCards();
        List<Relic> relics = builder.getRelics();
        List<Potion> potions = builder.getPotions();
        GameStateRandomization randomization = builder.getRandomization();
        GameStateRandomization preBattleRandomization = builder.getPreBattleRandomization();
        GameStateRandomization preBattleScenarios = builder.getPreBattleScenarios();
        // game properties (shared)
        properties = new GameProperties();
        properties.originalGameState = this;
        properties.switchBattleHandler = builder.getSwitchBattleHandler();
        properties.randomization = randomization;
        properties.preBattleRandomization = preBattleRandomization;
        properties.enemyHealthRandomization = new GameStateRandomization.EnemyHealthRandomization(false, null);
        properties.potions = potions;
        properties.potionsGenerator = builder.getPotionsGenerator();
        properties.nonGeneratedPotionsLength = potions.size();
        for (int i = 0; i < potions.size(); i++) {
            if (potions.get(i).isGenerated) {
                properties.nonGeneratedPotionsLength = i;
                break;
            }
        }
        properties.potionsScenarios = builder.getPotionsScenarios();
        properties.enemiesReordering = builder.getEnemyReordering().size() == 0 ? null : builder.getEnemyReordering();
        properties.character = builder.getCharacter();
        properties.relics = builder.getRelics();
        properties.enemiesEncounters = builder.getEnemiesEncounters();
        if (properties.potions.size() > 0) {
            GameStateRandomization p = new GameStateRandomization.PotionsUtilityRandomization(properties.potions);
            if (properties.potionsScenarios != null) {
                p = p.fixR(properties.potionsScenarios, 0);
            }
            properties.preBattleRandomization = properties.preBattleRandomization == null ? p : properties.preBattleRandomization.doAfter(p);
            properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.currentEncounter == EnemyEncounter.EncounterEnum.CORRUPT_HEART && properties.switchBattleHandler != null) {
                        for (int i = 0; i < properties.potions.size(); i++) {
                            if (properties.potions.get(i).isGenerated || (state.hadPotion(i) && !state.potionUsable(i))) {
                                continue;
                            }
                            state.setPotionUsable(i);
                            state.setPotionPenalty(i, (short) 100);
                        }
                    }
                }
            });
        }
        properties.potionsVExtraIdx = new int[properties.potions.size()];
        Arrays.fill(properties.potionsVExtraIdx, -1);
        registerPotionTrainingTargets();
        if (builder.getCharacter() == CharacterEnum.DEFECT) {
            properties.maxNumOfOrbs = 3;
            orbs = new short[3 * 2];
        }
        if (builder.getEndOfPreBattleSetupHandler() != null) {
            properties.endOfPreBattleHandler = builder.getEndOfPreBattleSetupHandler();
        }
        for (var encounter : properties.enemiesEncounters) {
            if (encounter.idxes.size() == 3 && encounter.encounterEnum == EnemyEncounter.EncounterEnum.SPEAR_AND_SHIELD) {
                properties.maxPossibleRealTurnsLeft = 100.0f;
                break;
            }
        }
        properties.startOfBattleActions = relics.stream().filter((relic) -> relic.startOfBattleAction != null).toList();
        properties.perScenarioCommands = builder.getPerScenarioCommands();

        properties.preBattleScenarios = properties.preBattleScenariosBackup = preBattleScenarios;
        if (properties.preBattleScenarios != null) {
            properties.preBattleGameScenariosList = properties.preBattleScenarios.listRandomizations().entrySet()
                    .stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).toList();
        }

        var cards = collectAllPossibleCards(cardCounts, enemiesArg, relics, potions);

        // start of game actions
        properties.actionsByCtx = new GameAction[GameActionCtx.values().length][];
        properties.actionsByCtx[GameActionCtx.BEGIN_PRE_BATTLE.ordinal()] = new GameAction[] { new GameAction(GameActionType.BEGIN_PRE_BATTLE, 0) };
        properties.actionsByCtx[GameActionCtx.BEGIN_BATTLE.ordinal()] = new GameAction[] { new GameAction(GameActionType.BEGIN_BATTLE, 0) };
        properties.actionsByCtx[GameActionCtx.BEGIN_TURN.ordinal()] = new GameAction[] { new GameAction(GameActionType.BEGIN_TURN, 0) };

        // play card actions
        var l = cards.size() + 1 + potions.size();
        properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()] = new GameAction[l];
        for (int i = 0; i < cards.size(); i++) {
            properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][i] = new GameAction(GameActionType.PLAY_CARD, i);
        }
        for (int i = 0; i < potions.size(); i++) {
            properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cards.size() + i] = new GameAction(GameActionType.USE_POTION, i);
        }
        properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][l - 1] = new GameAction(GameActionType.END_TURN, 0);

        // select enemy actions
        if (enemiesArg.size() > 1) {
            properties.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] = new GameAction[enemiesArg.size()];
            for (int i = 0; i < enemiesArg.size(); i++) {
                properties.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()][i] = new GameAction(GameActionType.SELECT_ENEMY, i);
            }
        }

        // select hand actions
        if (cards.stream().anyMatch((x) -> x.selectFromHand) || potions.stream().anyMatch((x) -> x.selectFromHand)) {
            properties.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()] = new GameAction[l];
            for (int i = 0; i < cards.size(); i++) {
                properties.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()][i] = new GameAction(GameActionType.SELECT_CARD_HAND, i);
            }
            for (int i = 0; i < potions.size(); i++) {
                properties.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()][cards.size() + i] = new GameAction(GameActionType.USE_POTION, i);
            }
            properties.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()][l - 1] = new GameAction(GameActionType.END_SELECT_CARD_HAND, 0);
        }

        // select from discard actions
        if (cards.stream().anyMatch((x) -> x.selectFromDiscard) || potions.stream().anyMatch((x) -> x.selectFromDiscard)) {
            properties.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()] = new GameAction[properties.realCardsLen];
            for (int i = 0; i < properties.realCardsLen; i++) {
                properties.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()][i] = new GameAction(GameActionType.SELECT_CARD_DISCARD, i);
            }
        }

        // select from exhaust actions
        if (cards.stream().anyMatch((x) -> x.selectFromExhaust)) {
            properties.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()] = new GameAction[properties.realCardsLen];
            for (int i = 0; i < properties.realCardsLen; i++) {
                properties.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()][i] = new GameAction(GameActionType.SELECT_CARD_EXHAUST, i);
            }
        }

        // select from deck actions
        if (cards.stream().anyMatch((x) -> x.selectFromDeck)) {
            properties.actionsByCtx[GameActionCtx.SELECT_CARD_DECK.ordinal()] = new GameAction[properties.realCardsLen];
            for (int i = 0; i < properties.realCardsLen; i++) {
                properties.actionsByCtx[GameActionCtx.SELECT_CARD_DECK.ordinal()][i] = new GameAction(GameActionType.SELECT_CARD_DECK, i);
            }
        }

        // scry actions
        if (cards.stream().anyMatch((x) -> x.scry) || relics.stream().anyMatch((x) -> x.scry)) {
            properties.actionsByCtx[GameActionCtx.SCRYING.ordinal()] = new GameAction[properties.realCardsLen + 1];
            for (int i = 0; i < properties.realCardsLen; i++) {
                properties.actionsByCtx[GameActionCtx.SCRYING.ordinal()][i] = new GameAction(GameActionType.SCRY_KEEP_CARD, i);
            }
            properties.actionsByCtx[GameActionCtx.SCRYING.ordinal()][l - 1] = new GameAction(GameActionType.SCRY_KEEP_CARD, properties.realCardsLen);
        }

        // select from select 1 out of 3 cards action
        if (cards.stream().anyMatch((x) -> !x.getPossibleSelect1OutOf3Cards(properties).isEmpty()) || relics.stream().anyMatch((x) -> !x.getPossibleSelect1OutOf3Cards(properties).isEmpty()) || potions.stream().anyMatch((x) -> !x.getPossibleSelect1OutOf3Cards(properties).isEmpty())) {
            properties.actionsByCtx[GameActionCtx.SELECT_CARD_1_OUT_OF_3.ordinal()] = new GameAction[l];
            for (int i = 0; i < cards.size(); i++) {
                properties.actionsByCtx[GameActionCtx.SELECT_CARD_1_OUT_OF_3.ordinal()][i] = new GameAction(GameActionType.SELECT_CARD_1_OUT_OF_3, i);
            }
            for (int i = 0; i < potions.size(); i++) {
                properties.actionsByCtx[GameActionCtx.SELECT_CARD_1_OUT_OF_3.ordinal()][cards.size() + i] = new GameAction(GameActionType.USE_POTION, i);
            }
            properties.actionsByCtx[GameActionCtx.SELECT_CARD_1_OUT_OF_3.ordinal()][l - 1] = new GameAction(GameActionType.END_SELECT_CARD_1_OUT_OF_3, 0);
        }

        // select pre battle scenario actions
        if (properties.preBattleScenarios != null) {
            properties.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()] = new GameAction[properties.preBattleGameScenariosList.size()];
            for (int i = 0; i < properties.preBattleGameScenariosList.size(); i++) {
                properties.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()][i] = new GameAction(GameActionType.SELECT_SCENARIO, i);
            }
        }

        for (int i = 0; i < properties.actionsByCtx.length; i++) {
            if (properties.actionsByCtx[i] != null) {
                properties.maxNumOfActions = Math.max(properties.maxNumOfActions, properties.actionsByCtx[i].length);
                if (i == GameActionCtx.PLAY_CARD.ordinal() || i == GameActionCtx.SELECT_ENEMY.ordinal() || i == GameActionCtx.SELECT_SCENARIO.ordinal()) {
                    properties.totalNumOfActions += properties.actionsByCtx[i].length;
                }
            }
        }

        // game state
        if (properties.preBattleRandomization != null || builder.getEndOfPreBattleSetupHandler() != null) {
            actionCtx = GameActionCtx.BEGIN_PRE_BATTLE;
        } else if (properties.preBattleScenarios != null) {
            actionCtx = GameActionCtx.SELECT_SCENARIO;
        } else {
            actionCtx = GameActionCtx.BEGIN_BATTLE;
        }
        handArr = new short[0];
        discardArr = new short[0];
        exhaustArr = new short[0];
        for (CardCount cardCount : cardCounts) {
            deckArrLen += cardCount.count();
        }
        deckArr = new short[deckArrLen];
        int idx = 0;
        for (short i = 0; i < cardCounts.size(); i++) {
            for (int j = 0; j < cardCounts.get(i).count(); j++) {
                deckArr[idx++] = (short) properties.findCardIndex(cardCounts.get(i).card());
            }
        }
        energyRefill = 3;
        this.enemies = new EnemyList(enemiesArg);
        enemiesAlive = (int) enemiesArg.stream().filter(EnemyReadOnly::isAlive).count();
        this.player = player;
        drawOrder = new DrawOrder(10);
        if (potions.size() > 0) {
            potionsState = new short[potions.size() * 3];
            for (int i = 0; i < properties.potions.size(); i++) {
                potionsState[i * 3] = 0;
                potionsState[i * 3 + 1] = 100;
                potionsState[i * 3 + 2] = 0;
            }
        }

        if (properties.cardIndexCache.size() == 0) {
            for (int i = 0; i < properties.cardDict.length; i++) {
                properties.cardIndexCache.put(properties.cardDict[i].cardName, i);
            }
        }
        List<Integer> strikeIdxes = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).cardName.equals("Normality")) {
                properties.normalityCardIdx = i;
            } else if (cards.get(i).cardName.contains("Strike")) {
                strikeIdxes.add(i);
            } else if (cards.get(i).cardName.equals("Echo Form")) {
                properties.echoFormCardIdx = i;
            } else if (cards.get(i).cardName.equals("Echo Form+")) {
                properties.echoFormPCardIdx = i;
            } else if (cards.get(i).cardName.equals("Apotheosis")) {
                properties.apotheosisCardIdx = i;
            } else if (cards.get(i).cardName.equals("Apotheosis+")) {
                properties.apotheosisPCardIdx = i;
            } else if (cards.get(i).cardName.equals("Well-Laid Plans") || cards.get(i).cardName.equals("Well-Laid Plans+")) {
                properties.wellLaidPlansCardIdx = i;
            } else if (cards.get(i).cardName.equals("Tools Of The Trade") || cards.get(i).cardName.equals("Tools Of The Trade+")) {
                properties.toolsOfTheTradeCardIdx = i;
            } else if (cards.get(i).cardName.equals("Foresight") || cards.get(i).cardName.equals("Foresight+")) {
                properties.foresightCardIdx = i;
            } else if (cards.get(i).cardName.equals("Gambling Chips")) {
                properties.gamblingChipsCardIdx = i;
            }
        }
        properties.strikeCardIdxes = strikeIdxes.stream().mapToInt(Integer::intValue).toArray();
        properties.healCardsIdxes = findCardThatCanHealIdxes(cards, relics);
        if (properties.healCardsIdxes != null) {
            properties.healCardsBooleanArr = new boolean[properties.cardDict.length];
            for (int i = 0; i < properties.healCardsIdxes.length; i++) {
                properties.healCardsBooleanArr[properties.healCardsIdxes[i]] = true;
            }
        }
        properties.select1OutOf3CardsIdxes = findSelect1OutOf3CardsToKeepTrackOf(Arrays.asList(properties.cardDict), relics, potions);
        properties.select1OutOf3CardsReverseIdxes = new int[properties.cardDict.length];
        Arrays.fill(properties.select1OutOf3CardsReverseIdxes, -1);
        for (int i = 0; i < properties.select1OutOf3CardsIdxes.length; i++) {
            properties.select1OutOf3CardsReverseIdxes[properties.select1OutOf3CardsIdxes[i]] = i;
        }
        for (Relic relic : relics) {
            relic.setupGeneratedCardIndexes(properties);
            relic.gamePropertiesSetup(this);
        }
        for (Card card : properties.cardDict) {
            card.setupGeneratedCardIndexes(properties);
            card.gamePropertiesSetup(this);
        }
        for (int i = 0; i < getEnemiesForRead().size(); i++) { // need to use i because setup can modify other enemies
            getEnemiesForRead().get(i).setupGeneratedCardIndexes(properties);
            getEnemiesForRead().get(i).gamePropertiesSetup(this);
        }
        for (int i = 0; i < potions.size(); i++) { // need to use i because setup can modify other enemies
            potions.get(i).setupGeneratedCardIndexes(properties);
            potions.get(i).gamePropertiesSetup(this);
        }
        if (Configuration.HEART_GAUNTLET_CARD_REWARD) {
            EnemyEncounter.gamePropertiesSetup(this);
        }
        properties.compileCounterInfo();
        counter = new int[properties.counterLength];
        Collections.sort(properties.startOfBattleHandlers);
        Collections.sort(properties.endOfBattleHandlers);
        Collections.sort(properties.startOfTurnHandlers);
        Collections.sort(properties.preEndTurnHandlers);
        Collections.sort(properties.endOfTurnHandlers);
        Collections.sort(properties.onBlockHandlers);
        Collections.sort(properties.onShuffleHandlers);
        Collections.sort(properties.onExhaustHandlers);
        Collections.sort(properties.onStanceChangeHandlers);
        Collections.sort(properties.onScryHandlers);
        Collections.sort(properties.onCardPlayedHandlers);
        Collections.sort(properties.onPreCardPlayedHandlers);
        Collections.sort(properties.onCardDrawnHandlers);
        Collections.sort(properties.onRetainHandlers);

        if (properties.biasedCognitionLimitCounterIdx >= 0) {
            properties.actionsByCtx[GameActionCtx.AFTER_RANDOMIZATION.ordinal()] = new GameAction[] { new GameAction(GameActionType.AFTER_RANDOMIZATION, 0) };
        }

        properties.playerArtifactCanChange = getPlayeForRead().getArtifact() > 0;
        properties.playerArtifactCanChange |= cards.stream().anyMatch((x) -> x.changePlayerArtifact);
        properties.playerArtifactCanChange |= potions.stream().anyMatch((x) -> x.changePlayerArtifact);
        properties.playerStrengthCanChange = cards.stream().anyMatch((x) -> x.changePlayerStrength);
        properties.playerStrengthCanChange |= enemiesArg.stream().anyMatch((x) -> x.properties.changePlayerStrength);
        properties.playerStrengthCanChange |= relics.stream().anyMatch((x) -> x.changePlayerStrength);
        properties.playerStrengthCanChange |= potions.stream().anyMatch((x) -> x.changePlayerStrength);
        properties.playerDexterityCanChange = cards.stream().anyMatch((x) -> x.changePlayerDexterity);
        properties.playerDexterityCanChange |= enemiesArg.stream().anyMatch((x) -> x.properties.changePlayerDexterity);
        properties.playerDexterityCanChange |= relics.stream().anyMatch((x) -> x.changePlayerDexterity);
        properties.playerDexterityCanChange |= potions.stream().anyMatch((x) -> x.changePlayerDexterity);
        properties.playerStrengthEotCanChange = cards.stream().anyMatch((x) -> x.changePlayerStrengthEot);
        properties.playerStrengthEotCanChange |= potions.stream().anyMatch((x) -> x.changePlayerStrengthEot);
        properties.playerDexterityEotCanChange = potions.stream().anyMatch((x) -> x.changePlayerDexterityEot);
        properties.playerFocusCanChange = cards.stream().anyMatch((x) -> x.changePlayerFocus);
        properties.playerFocusCanChange |= enemiesArg.stream().anyMatch((x) -> x.properties.changePlayerFocus);
        properties.playerFocusCanChange |= potions.stream().anyMatch((x) -> x.changePlayerFocus);
        properties.playerCanGetVuln = enemiesArg.stream().anyMatch((x) -> x.properties.canVulnerable);
        properties.playerCanGetVuln |= cards.stream().anyMatch((x) -> x.changePlayerVulnerable);
        properties.playerCanGetWeakened = enemiesArg.stream().anyMatch((x) -> x.properties.canWeaken);
        properties.playerCanGetFrailed = enemiesArg.stream().anyMatch((x) -> x.properties.canFrail);
        properties.playerCanGetEntangled = enemiesArg.stream().anyMatch((x) -> x.properties.canEntangle);
        properties.previousCardPlayTracking = cards.stream().anyMatch((x) -> x.needsLastCardType);
        properties.enemyCanGetVuln = cards.stream().anyMatch((x) -> x.vulnEnemy);
        properties.enemyCanGetVuln |= relics.stream().anyMatch((x) -> x.vulnEnemy);
        properties.enemyCanGetVuln |= potions.stream().anyMatch((x) -> x.vulnEnemy);
        properties.enemyCanGetWeakened = cards.stream().anyMatch((x) -> x.weakEnemy);
        properties.enemyCanGetWeakened |= relics.stream().anyMatch((x) -> x.weakEnemy);
        properties.enemyCanGetWeakened |= potions.stream().anyMatch((x) -> x.weakEnemy);
        properties.enemyCanGetChoked = cards.stream().anyMatch((x) -> x.chokeEnemy);
        properties.enemyCanGetLockOn = cards.stream().anyMatch((x) -> x.lockOnEnemy);
        properties.enemyCanGetTalkToTheHand = cards.stream().anyMatch((x) -> x.talkToTheHandEnemy);
        properties.enemyCanGetMark = cards.stream().anyMatch((x) -> x.markEnemy);
        properties.enemyCanGetPoisoned = cards.stream().anyMatch((x) -> x.poisonEnemy);
        properties.enemyCanGetPoisoned |= potions.stream().anyMatch((x) -> x.poisonEnemy);
        properties.enemyCanGetPoisoned |= relics.stream().anyMatch((x) -> x.poisonEnemy);
        properties.enemyCanGetCorpseExplosion = cards.stream().anyMatch((x) -> x.corpseExplosionEnemy);
        properties.enemyStrengthEotCanChange = cards.stream().anyMatch((x) -> x.affectEnemyStrengthEot);
        properties.enemyStrengthCanChange = cards.stream().anyMatch((x) -> x.affectEnemyStrength);
        properties.enemyStrengthCanChange |= relics.stream().anyMatch((x) -> x.changeEnemyStrength);
        properties.possibleBuffs |= cards.stream().anyMatch((x) -> x.cardName.contains("Corruption")) ? PlayerBuff.CORRUPTION.mask() : 0;
        properties.possibleBuffs |= cards.stream().anyMatch((x) -> x.cardName.contains("Barricade")) ? PlayerBuff.BARRICADE.mask() : 0;
        properties.possibleBuffs |= cards.stream().anyMatch((x) -> x.cardName.contains("Blasphemy")) ? PlayerBuff.BLASPHEMY.mask() : 0;
        properties.possibleBuffs |= relics.stream().anyMatch((x) -> x instanceof Relic.Akabeko) ? PlayerBuff.AKABEKO.mask() : 0;
        properties.possibleBuffs |= relics.stream().anyMatch((x) -> x instanceof Relic.ArtOfWar) ? PlayerBuff.ART_OF_WAR.mask() : 0;
        properties.possibleBuffs |= relics.stream().anyMatch((x) -> x instanceof Relic.CentennialPuzzle) ? PlayerBuff.CENTENNIAL_PUZZLE.mask() : 0;
        properties.possibleBuffs |= relics.stream().anyMatch((x) -> x instanceof Relic.Necronomicon) ? PlayerBuff.NECRONOMICON.mask() : 0;
        properties.possibleBuffs |= (enemiesArg.stream().anyMatch((x) -> x instanceof EnemyBeyond.TimeEater) || 
                                   cards.stream().anyMatch((x) -> x.cardName.contains("Meditate") || x.cardName.contains("Conclude") || x.cardName.contains("Vault"))) ? PlayerBuff.END_TURN_IMMEDIATELY.mask() : 0;
        properties.possibleBuffs |= cards.stream().anyMatch((x) -> x.cardName.contains("Vault")) ? PlayerBuff.USED_VAULT.mask() : 0;
        properties.needDeckOrderMemory |= cards.stream().anyMatch((x) -> x.putCardOnTopDeck);
        properties.selectFromExhaust = cards.stream().anyMatch((x) -> x.selectFromExhaust);
        properties.battleTranceExist = cards.stream().anyMatch((x) -> x.cardName.contains("Battle Trance"));
        properties.energyRefillCanChange = cards.stream().anyMatch((x) -> x.cardName.contains("Berserk"));
//        heal end of act makes learning harder due to loss of damage taken
//        prop.healEndOfAct = builder.getEnemies().stream().allMatch((x) -> x.property.isBoss);
        properties.originalEnemies = new EnemyList(enemies);
        for (int i = 0; i < properties.originalEnemies.size(); i++) {
            properties.originalEnemies.getForWrite(i);
        }
        if (Configuration.USE_FIGHT_PROGRESS_WHEN_LOSING) {
            properties.addExtraTrainingTarget("FightProgress", new GameProperties.TrainingTargetRegistrant() {
                @Override public void setVExtraIdx(GameProperties properties, int idx) {
                    properties.fightProgressVExtraIdx = idx;
                }
            }, new TrainingTarget() {
                @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                    if (isTerminal > 0) {
                        v.setVExtra(state.properties.fightProgressVExtraIdx, 1);
                    } else if (isTerminal == 0) {
                        v.setVExtra(state.properties.fightProgressVExtraIdx, state.getVExtra(properties.fightProgressVExtraIdx));
                    }
                }

                @Override public void updateQValues(GameState state, VArray v) {}
            });
        }
        if (Configuration.TRAINING_EXPERIMENT_USE_UNCERTAINTY_FOR_EXPLORATION) {
            properties.addExtraTrainingTarget("ZAWin", new GameProperties.TrainingTargetRegistrant() {
                @Override public void setVExtraIdx(GameProperties properties, int idx) {
                    properties.qwinVExtraIdx = idx;
                }
            }, new TrainingTarget() {
                @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                    if (isTerminal > 0) {
                        v.setVExtra(state.properties.qwinVExtraIdx, 0);
                    } else if (isTerminal < 0) {
                        v.setVExtra(state.properties.qwinVExtraIdx, 0);
                    } else if (isTerminal == 0) {
                        v.setVExtra(state.properties.qwinVExtraIdx, state.getVExtra(properties.qwinVExtraIdx));
                    }
                }

                @Override public void updateQValues(GameState state, VArray v) {}
            });
        }
        if (Configuration.USE_TURNS_LEFT_HEAD) {
            // note: we translate turns left to estimated ending turn so there's no need to change anything in the searching
            // code. Having the network predict ending turn directly seem to work for a training target too.
            properties.addExtraTrainingTarget("TurnsLeft", new GameProperties.TrainingTargetRegistrant() {
                @Override public void setVExtraIdx(GameProperties properties, int idx) {
                    properties.turnsLeftVExtraIdx = idx;
                }
            }, new TrainingTarget() {
                @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                    if (isTerminal != 0) {
                        v.setVExtra(state.properties.turnsLeftVExtraIdx, state.realTurnNum / state.properties.maxPossibleRealTurnsLeft);
                    } else {
                        v.setVExtra(state.properties.turnsLeftVExtraIdx, state.realTurnNum / state.properties.maxPossibleRealTurnsLeft + state.getVExtra(properties.turnsLeftVExtraIdx));
                        v.setVExtra(state.properties.turnsLeftVExtraIdx, Math.min(v.getVExtra(state.properties.turnsLeftVExtraIdx), 1.0));
                    }
                }
                @Override public void updateQValues(GameState state, VArray v) {}
            });
        }
        if (Configuration.USE_TURNS_LEFT_HEAD_ONLY_WHEN_NO_DMG) {
            properties.addExtraTrainingTarget("ZeroDmgProb", new GameProperties.TrainingTargetRegistrant() {
                @Override public void setVExtraIdx(GameProperties properties, int idx) {
                    properties.zeroDmgProbVExtraIdx = idx;
                }
            }, new TrainingTarget() {
                @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                    v.setVExtra(state.properties.zeroDmgProbVExtraIdx, 0);
                    if (isTerminal != 0) {
                        v.setVZeroDmg(state.getPlayeForRead().getAccumulatedDamage(), (isTerminal > 0 || state.isLossFrom50()) ? 1 : 0);
                    } else {
                        v.setVZeroDmg(state.getPlayeForRead().getAccumulatedDamage(), state.getVExtra(properties.zeroDmgProbVExtraIdx));
                    }
                }
                @Override public void updateQValues(GameState state, VArray v) {}
            });
        }
        properties.compileExtraTrainingTarget();
        properties.inputLen = getNNInputLen();
        properties.v_total_len = 3;
        for (var target : properties.extraTrainingTargets) {
            properties.v_total_len += target.getNumberOfTargets();
        }

        // mcts related fields
        terminalAction = -100;
        transpositions = new HashMap<>();
        if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH) {
            transpositionsParent = new HashMap<>();
        }
    }

    private void registerPotionTrainingTargets() {
        for (int i = 0; i < properties.potions.size(); i++) {
            if (properties.potions.get(i) instanceof Potion.FairyInABottle || properties.toyOrnithopter != null) {
                int _i = i;
                properties.addExtraTrainingTarget((properties.potions.get(i) + String.valueOf(i)).replace(" ", ""), new GameProperties.TrainingTargetRegistrant() {
                    @Override public void setVExtraIdx(GameProperties properties, int idx) {
                        properties.potionsVExtraIdx[_i] = idx;
                    }
                }, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, VArray v, int isTerminal) {
                        if (isTerminal != 0) {
                            if (state.potionsState[_i * 3] == 1 || state.potionsState[_i * 3 + 2] == 0) {
                                v.setVExtra(state.properties.potionsVExtraIdx[_i], 1);
                            } else {
                                v.setVExtra(state.properties.potionsVExtraIdx[_i], 0);
                            }
                        } else {
                            if (state.potionsState[_i * 3 + 2] == 0) {
                                v.setVExtra(state.properties.potionsVExtraIdx[_i], 1);
                            } else if (state.potionsState[_i * 3] == 0) {
                                v.setVExtra(state.properties.potionsVExtraIdx[_i], 0);
                            } else {
                                v.setVExtra(state.properties.potionsVExtraIdx[_i], state.getVExtra(state.properties.potionsVExtraIdx[_i]));
                            }
                        }
                    }

                    @Override public void updateQValues(GameState state, VArray v) {
                        if (properties.potions.get(_i) instanceof Potion.FairyInABottle pot) {
                            v.add(V_HEALTH_IDX, pot.getHealAmount(state) * v.get(GameState.V_EXTRA_IDX_START + state.properties.potionsVExtraIdx[_i]) / state.getPlayeForRead().getMaxHealth());
                        } else if (!properties.isHeartFight(state)) {
                            v.add(V_HEALTH_IDX, 5 * v.get(GameState.V_EXTRA_IDX_START + state.properties.potionsVExtraIdx[_i]) / state.getPlayeForRead().getMaxHealth());
                        }
                    }
                });
            }
        }
    }

    private int[] findCardThatCanHealIdxes(List<Card> cards, List<Relic> relics) {
        // todo
        long c = cards.stream().filter((x) -> x.healPlayer).count();
        if (c == 0) {
            return null;
        }
        int[] r = new int[(int) c];
        int idx = 0;
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).healPlayer) {
                r[idx++] = i;
            }
        }
        return r;
    }

    private int[] findUpgradeIdxes(List<Card> cards, List<Relic> relics, List<Potion> potions) {
        if (cards.stream().noneMatch((x) -> x.cardName.contains("Armanent") || x.cardName.contains("Apotheosis")) &&
            relics.stream().noneMatch((x) -> x instanceof Relic.WarpedTongs) && potions.stream().noneMatch((x) -> x instanceof Potion.BlessingOfTheForge)) {
            return null;
        }
        int[] r = new int[cards.size()];
        Arrays.fill(r, -1);
        for (int i = 0; i < r.length; i++) {
            var upgrade = cards.get(i).getUpgrade();
            if (upgrade != null) {
                r[i] = properties.findCardIndex(upgrade);
            }
        }
        return r;
    }

    private int[] findDiscardToKeepTrackOf(HashSet<Card> discardSet, List<Card> cards, List<Potion> potions) {
        Set<Integer> l = discardSet.stream().map((x) -> properties.findCardIndex(x)).collect(Collectors.toSet());
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).exhaustWhenPlayed && getCardEnergyCost(i) >= 0) {
                l.add(i);
            }
            if (cards.get(i).cardType == Card.POWER) {
                l.add(i);
            }
            if (cards.get(i).ethereal && getCardEnergyCost(i) >= 0) {
                l.add(i);
            }
            if (cards.get(i).exhaustNonAttacks) {
                for (int j = 0; j < cards.size(); j++) {
                    if (cards.get(j).cardType != Card.ATTACK) {
                        l.add(j);
                    }
                }
            }
            if (cards.get(i).exhaustSkill) {
                for (int j = 0; j < cards.size(); j++) {
                    if (cards.get(j).cardType == Card.SKILL) {
                        l.add(j);
                    }
                }
            }
            if (cards.get(i).discardNonAttack) {
                for (int j = 0; j < cards.size(); j++) {
                    if (cards.get(j).cardType != Card.ATTACK) {
                        l.add(j);
                    }
                }
            }
            if (cards.get(i).selectFromDiscard || cards.get(i).canExhaustAnyCard || cards.get(i).canDiscardAnyCard) {
                for (int j = 0; j < cards.size(); j++) {
                    l.add(j);
                }
            }
        }
        for (Potion potion : potions) {
            if (potion.selectFromDiscard) {
                for (int j = 0; j < cards.size(); j++) {
                    l.add(j);
                }
            }
        }
        if (properties.upgradeIdxes != null) {
            for (int i = 0; i < properties.upgradeIdxes.length; i++) {
                if (properties.upgradeIdxes[i] >= 0) {
                    l.add(i);
                    l.add(properties.upgradeIdxes[i]);
                }
            }
        }
        return l.stream().filter((x) -> !properties.cardDict[x].isTmpModifiedCard()).sorted().mapToInt(Integer::intValue).toArray();
    }

    private int[] findSelect1OutOf3CardsToKeepTrackOf(List<Card> cards, List<Relic> relics, List<Potion> potions) {
        List<Integer> idxes = new ArrayList<>();
        cards.forEach(c -> c.getPossibleSelect1OutOf3Cards(properties).forEach((card) -> idxes.add(properties.findCardIndex(card))));
        potions.forEach(potion -> potion.getPossibleSelect1OutOf3Cards(properties).forEach((card) -> idxes.add(properties.findCardIndex(card))));
        relics.forEach(relic -> relic.getPossibleSelect1OutOf3Cards(properties).forEach((card) -> idxes.add(properties.findCardIndex(card))));
        if (Configuration.HEART_GAUNTLET_CARD_REWARD) {
            EnemyEncounter.getPossibleSelect1OutOf3CardsFromRewardScreen(properties).forEach((card) -> idxes.add(properties.findCardIndex(card)));
        }
        return idxes.stream().mapToInt(Integer::intValue).sorted().toArray();
    }

    private List<Card> collectAllPossibleCards(List<CardCount> cardCounts, List<Enemy> enemies, List<Relic> relics, List<Potion> potions) {
        var set = new HashSet<>(cardCounts.stream().map(CardCount::card).toList());
        var discardSet = new HashSet<Card>();
        if (properties.preBattleScenarios != null) {
            addPossibleGeneratedCardsFromListOfCard(properties.preBattleScenarios.getPossibleGeneratedCards(), set, discardSet);
        }
        if (properties.randomization != null) {
            addPossibleGeneratedCardsFromListOfCard(properties.randomization.getPossibleGeneratedCards(), set, discardSet);
        }
        do {
            var newSet = new HashSet<>(set);
            for (Card c : set) {
                addPossibleGeneratedCardsFromListOfCard(c.getPossibleGeneratedCards(properties, set.stream().toList()), newSet, discardSet);
            }
            for (Relic relic : relics) {
                addPossibleGeneratedCardsFromListOfCard(relic.getPossibleGeneratedCards(properties, set.stream().toList()), newSet, discardSet);
            }
            for (Potion potion : potions) {
                addPossibleGeneratedCardsFromListOfCard(potion.getPossibleGeneratedCards(properties, set.stream().toList()), newSet, discardSet);
            }
            for (Enemy enemy : enemies) {
                addPossibleGeneratedCardsFromListOfCard(enemy.getPossibleGeneratedCards(properties, set.stream().toList()), newSet, discardSet);
            }
            if (Configuration.HEART_GAUNTLET_CARD_REWARD) {
                addPossibleGeneratedCardsFromListOfCard(EnemyEncounter.getPossibleSelect1OutOf3CardsFromRewardScreen(properties), newSet, discardSet);
            }
            if (set.size() == newSet.size()) {
                break;
            }
            set = newSet;
        } while (true);
        var newSet = new HashSet<>(set);
        for (Card c : set) {
            addPossibleGeneratedCardsFromListOfCard(c.getPossibleTransformTmpCostCards(set.stream().toList()), newSet, null);
        }
        for (Relic relic : relics) {
            addPossibleGeneratedCardsFromListOfCard(relic.getPossibleTransformTmpCostCards(properties, set.stream().toList()), newSet, null);
        }
        set = newSet;

        var cards = new ArrayList<>(set);
        cards.sort((o1, o2) -> {
            if (!o1.isTmpModifiedCard() && o2.isTmpModifiedCard()) {
                return -1;
            } else if (o1.isTmpModifiedCard() && !o2.isTmpModifiedCard()) {
                return 1;
            } else {
                return o1.cardName.compareTo(o2.cardName);
            }
        });
        properties.cardDict = new Card[cards.size()];
        for (int i = 0; i < cards.size(); i++) {
            properties.cardDict[i] = cards.get(i);
        }
        properties.realCardsLen = (int) cards.stream().takeWhile((card) -> !card.isTmpModifiedCard()).count();
        if (properties.realCardsLen != cards.size()) {
            properties.tmp0CostCardTransformIdxes = new int[cards.size()];
            properties.tmpModifiedCardReverseTransformIdxes = new int[cards.size()];
            Arrays.fill(properties.tmp0CostCardTransformIdxes, -1);
            Arrays.fill(properties.tmpModifiedCardReverseTransformIdxes, -1);
            for (int i = 0; i < cards.size(); i++) {
                if (properties.cardDict[i].isTmpModifiedCard()) {
                    properties.tmpModifiedCardReverseTransformIdxes[i] = properties.findCardIndex(properties.cardDict[i].getBaseCard());
                } else {
                    properties.tmp0CostCardTransformIdxes[i] = properties.findCardIndex(properties.cardDict[i].getTemporaryCostIfPossible(0));
                }
            }
        }

        properties.upgradeIdxes = findUpgradeIdxes(cards, relics, potions);
        properties.discardIdxes = findDiscardToKeepTrackOf(discardSet, cards, potions);
        properties.discardReverseIdxes = new int[properties.realCardsLen];
        for (int i = 0; i < properties.discardIdxes.length; i++) {
            properties.discardReverseIdxes[properties.discardIdxes[i]] = i;
        }
        return cards;
    }

    private static void addPossibleGeneratedCardsFromListOfCard(List<Card> c, HashSet<Card> newSet, HashSet<Card> discardSet) {
        for (Card possibleCard : c) {
            newSet.add(possibleCard);
            if (possibleCard instanceof Card.CardWrapper w && (w.isTmpRetain() || w.isTmpChangeCost() || w.isTmpUntilPlayedCost())) {
                newSet.add(possibleCard.getBaseCard());
                discardSet.add(possibleCard.getBaseCard());
            } else {
                discardSet.add(possibleCard);
            }
        }
    }

    private GameState(GameState other) {
        properties = other.properties;

        actionCtx = other.actionCtx;
        actionCardCloneSource = other.actionCardCloneSource;
        handArr = other.handArr;
        handArrLen = other.handArrLen;
        discardArr = other.discardArr;
        discardArrLen = other.discardArrLen;
        deckArr = other.deckArr;
        deckArrLen = other.deckArrLen;
        deckArrFixedDrawLen = other.deckArrFixedDrawLen;
        exhaustArr = other.exhaustArr;
        exhaustArrLen = other.exhaustArrLen;
        if (other.chosenCardsArr != null) {
            chosenCardsArr = other.chosenCardsArr;
            chosenCardsArrLen = other.chosenCardsArrLen;
        }
        nightmareCards = other.nightmareCards;
        nightmareCardsLen = other.nightmareCardsLen;
        energy = other.energy;
        turnNum = other.turnNum;
        realTurnNum = other.realTurnNum;
        actionsInCurTurn = other.actionsInCurTurn;
        playerTurnStartMaxPossibleHealth = other.playerTurnStartMaxPossibleHealth;
        playerTurnStartPotionCount = other.playerTurnStartPotionCount;
        playerTurnStartMaxHandOfGreed = other.playerTurnStartMaxHandOfGreed;
        playerTurnStartMaxRitualDagger = other.playerTurnStartMaxRitualDagger;
        preBattleRandomizationIdxChosen = other.preBattleRandomizationIdxChosen;
        preBattleScenariosChosenIdx = other.preBattleScenariosChosenIdx;
        battleRandomizationIdxChosen = other.battleRandomizationIdxChosen;
        startOfBattleActionIdx = other.startOfBattleActionIdx;
        skipInteractiveModeSetup = other.skipInteractiveModeSetup;
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
        potionsState = other.potionsState;
        currentEncounter = other.currentEncounter;
        other.enemiesCloned = false;
        other.counterCloned = false;
        other.playerCloned = false;
        other.handCloned = false;
        other.deckCloned = false;
        other.discardCloned = false;
        other.exhaustCloned = false;
        other.drawOrderCloned = false;
        other.potionsStateCloned = false;

        buffs = other.buffs;
        counter = other.counter;
        select1OutOf3CardsIdxes = other.select1OutOf3CardsIdxes;
        if (other.orbs != null) {
            orbs = Arrays.copyOf(other.orbs, other.orbs.length); // todo: make it copy on write
        }
        focus = other.focus;
        stance = other.stance;
        scryCardIsKept = other.scryCardIsKept;
        scryCurrentCount = other.scryCurrentCount;
        lastCardPlayedType = other.lastCardPlayedType;

        legalActions = other.legalActions;
        terminalAction = -100;
        if (properties.multithreadedMTCS) {
            lock = new ReentrantReadWriteLock();
            virtualLoss = new AtomicInteger();
        }
    }

    public GameState clone(boolean keepTranspositions) {
        GameState clone = new GameState(this);
        if (keepTranspositions) {
            clone.transpositions = transpositions;
            clone.transpositionsLock = transpositionsLock;
            clone.transpositionsParent = transpositionsParent;
        } else {
            clone.transpositions = new HashMap<>();
            clone.transpositionsLock = properties.multithreadedMTCS ? new ReentrantLock() : null;
            if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH) clone.transpositionsParent = new HashMap<>();
        }
        return clone;
    }

    public int draw(int count) {
        if (count == 0 || getPlayeForRead().cannotDrawCard()) {
            return -1;
        }
        count = Math.min(GameState.HAND_LIMIT - handArrLen, count);
        boolean firstRandomDraw = true;
        int drawnIdx = -1;
        for (int c = 0; c < count; c++) {
            if (deckArrLen == 0) {
                reshuffle();
            }
            if (deckArrLen == 0) {
                return -1;
            }
            int i, cardIdx;
            if (deckArrFixedDrawLen > 0) {
                cardIdx = deckArr[deckArrLen - 1];
                drawnIdx = cardIdx;
                addCardToHand(cardIdx);
                deckArrLen--;
                deckArrFixedDrawLen--;
            } else if (drawOrder.size() > 0) {
                i = getDrawOrderForWrite().peekTop();
                int prevLen = drawOrder.size();
                if (!drawCardByIdx(i, true)) {
                    continue;
                }
                if (prevLen == drawOrder.size()) {
                    getDrawOrderForWrite().drawTop();
                }
                cardIdx = i;
                drawnIdx = cardIdx;
            } else {
                i = getSearchRandomGen().nextInt(this.deckArrLen, RandomGenCtx.CardDraw);
                setIsStochastic();
                if (properties.makingRealMove || properties.doingComparison) {
                    Arrays.sort(getDeckArrForWrite(), 0, this.deckArrLen);
                }
                cardIdx = deckArr[i];
                addCardToHand(deckArr[i]);
                if (properties.makingRealMove || properties.stateDescOn) {
                    if (firstRandomDraw) {
                        getStateDesc().append(getStateDesc().length() > 0 ? "; " : "").append("Draw ");
                        firstRandomDraw = false;
                    }
                    if (getStateDesc().charAt(getStateDesc().length() - 1) != ' ') {
                        getStateDesc().append(", ");
                    }
                    getStateDesc().append(properties.cardDict[deckArr[i]].cardName);
                }
                getDeckArrForWrite()[i] = deckArr[deckArrLen - 1];
                deckArrLen -= 1;
                drawnIdx = cardIdx;
            }
            if (properties.sneckoDebuffCounterIdx >= 0 && getCounterForRead()[properties.sneckoDebuffCounterIdx] > 0) {
                removeCardFromHand(cardIdx);
                var snecko = properties.sneckoIdxes[cardIdx];
                if (snecko[0] == 1) {
                    cardIdx = snecko[1];
                    addCardToHand(snecko[1]);
                } else {
                    cardIdx = snecko[getSearchRandomGen().nextInt(snecko[0], RandomGenCtx.Snecko, new Tuple<>(this, cardIdx)) + 1];
                    addCardToHand(cardIdx);
                }
            }
            for (var handler : properties.onCardDrawnHandlers) {
                handler.handle(this, cardIdx, -1, -1, null, -1);
            }
        }
        return drawnIdx;
    }

    public int drawOneCardSpecial() {
        if (deckArrLen == 0) {
            reshuffle();
        }
        if (deckArrLen == 0) {
            return -1;
        }
        int cardIdx;
        if (deckArrFixedDrawLen > 0) {
            cardIdx = deckArr[deckArrLen - 1];
            deckArrFixedDrawLen--;
            deckArrLen -= 1;
        } else if (drawOrder.size() > 0) {
            cardIdx = getDrawOrderForWrite().peekTop();
            int prevLen = drawOrder.size();
            if (!drawCardByIdx(cardIdx, false)) {
                return -1;
            }
            if (prevLen == drawOrder.size()) {
                getDrawOrderForWrite().drawTop();
            }
        }  else {
            setIsStochastic();
            var idx = getSearchRandomGen().nextInt(this.deckArrLen, RandomGenCtx.CardDraw);
            cardIdx = deckArr[idx];
            addCardToHand(deckArr[idx]);
            getDeckArrForWrite()[idx] = deckArr[deckArrLen - 1];
            deckArrLen -= 1;
        }
        return cardIdx;
    }

    private int getCardEnergyCost(int cardIdx) {
        if ((buffs & PlayerBuff.CORRUPTION.mask()) != 0 && properties.cardDict[cardIdx].cardType == Card.SKILL) {
            return 0;
        } else if (properties.blueCandle != null && properties.blueCandle.isRelicEnabledInScenario(preBattleScenariosChosenIdx) && properties.cardDict[cardIdx].cardType == Card.CURSE) {
            return 0;
        } else if (properties.medicalKit != null && properties.medicalKit.isRelicEnabledInScenario(preBattleScenariosChosenIdx) && properties.cardDict[cardIdx].cardType == Card.STATUS) {
            return 0;
        } else if (properties.swivelCounterIdx >= 0 && counter[properties.swivelCounterIdx] > 0 && properties.cardDict[cardIdx].cardType == Card.ATTACK) {
            return 0;
        }
        return properties.cardDict[cardIdx].energyCost(this);
    }

    public void setActionCtx(GameActionCtx ctx, GameAction action, Class cloneSource) {
        switch (ctx) {
        case PLAY_CARD -> {
            currentAction = null;
            select1OutOf3CardsIdxes = 0;
            actionCtx = ctx;
            this.actionCardCloneSource = null;
        }
        case SELECT_ENEMY, SELECT_CARD_HAND, SELECT_CARD_DISCARD, SELECT_CARD_EXHAUST, SELECT_CARD_DECK, SCRYING, SELECT_CARD_1_OUT_OF_3 -> {
            currentAction = action;
            actionCtx = ctx;
            this.actionCardCloneSource = cloneSource;
        }
        default -> actionCtx = ctx;
        }
    }

    public GameActionCtx getActionCtx() {
        return actionCtx;
    }

    public boolean playCard(GameAction action, int selectIdx,
                            boolean runActionQueueOnEnd, // need to set to prevent running action queue while already in action queue execution
                            Class cloneSource, // cloned card source
                            boolean useEnergy, // when cloning/havoc/etc., do not use energy
                            boolean exhaustWhenPlayed, // havoc only flag
                            int overrideEnergyCost, // when cloning, need to know cost of X card to use
                            int cloneParentLocation // when cloning, need to transform the prev card (e.g. Streamline)
    ) {
        int cardIdx = action.idx();
        int lastSelectedIdx = -1;
        boolean cardPlayedSuccessfully = true;
        boolean targetHalfAlive = false;
        int energyCost = overrideEnergyCost >= 0 ? overrideEnergyCost : getCardEnergyCost(cardIdx);
        int realEnergyCost = energyCost;
        if (properties.cardDict[cardIdx].isXCost && properties.chemicalX != null && 
            properties.chemicalX.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
            energyCost += 2;
        }

        if (properties.velvetChokerCounterIndexIdx >= 0 && getCounterForRead()[properties.velvetChokerCounterIndexIdx] >= 6 && actionCtx == GameActionCtx.PLAY_CARD) {
            return false;
        } else if (properties.timeEaterCounterIdx >= 0 && getCounterForRead()[properties.timeEaterCounterIdx] == 12 && cardIdx != properties.wellLaidPlansCardIdx) {
            return false;
        }
        if (actionCtx == GameActionCtx.PLAY_CARD) {
            checkWristBladeBuffForZeroCostAttack(cardIdx);
            if (cloneSource == null && !exhaustWhenPlayed) {
                removeCardFromHand(cardIdx);
            }
            if (realEnergyCost < 0) {
                if (runActionQueueOnEnd) {
                    runActionsInQueueIfNonEmpty();
                }
                return false;
            }
            if (!properties.cardDict[cardIdx].delayUseEnergy && useEnergy) {
                energy -= realEnergyCost;
            }
            if (cardIdx >= properties.realCardsLen) {
                cardIdx = properties.tmpModifiedCardReverseTransformIdxes[cardIdx];
                action = properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
            }
            for (var handler : properties.onPreCardPlayedHandlers) {
                handler.handle(this, cardIdx, lastSelectedIdx, energyCost, cloneSource, -1);
            }
            if (properties.cardDict[cardIdx].selectEnemy) {
                setActionCtx(GameActionCtx.SELECT_ENEMY, action, cloneSource);
            } else if (properties.cardDict[cardIdx].selectFromHand && !properties.cardDict[cardIdx].selectFromHandLater) {
                setActionCtx(GameActionCtx.SELECT_CARD_HAND, action, cloneSource);
            } else if (properties.cardDict[cardIdx].selectFromDiscard && !properties.cardDict[cardIdx].selectFromDiscardLater) {
                setActionCtx(GameActionCtx.SELECT_CARD_DISCARD, action, cloneSource);
            } else if (properties.cardDict[cardIdx].selectFromExhaust) {
                setActionCtx(GameActionCtx.SELECT_CARD_EXHAUST, action, cloneSource);
            } else if (properties.cardDict[cardIdx].selectFromDeck) {
                setActionCtx(GameActionCtx.SELECT_CARD_DECK, action, cloneSource);
            }
        }

        boolean isCardPlayed = true;
        if (actionCtx == GameActionCtx.SELECT_CARD_HAND && properties.toolsOfTheTradeCardIdx == currentAction.idx()) {
            isCardPlayed = false;
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
                if (selectIdx >= 0) {
                    var e = getEnemiesForRead().get(selectIdx);
                    if (e.isAlive()) {
                        lastSelectedIdx = selectIdx;
                        onSelectEnemy(selectIdx);
                        setActionCtx(properties.cardDict[cardIdx].play(this, selectIdx, energyCost), action, cloneSource);
                        selectIdx = -1;
                    } else {
                        cardPlayedSuccessfully = false;
                        targetHalfAlive = e.properties.canSelfRevive;
                        setActionCtx(GameActionCtx.PLAY_CARD, action, cloneSource);
                    }
                } else if (targetableEnemies == 1) {
                    lastSelectedIdx = idx;
                    onSelectEnemy(idx);
                    setActionCtx(properties.cardDict[cardIdx].play(this, idx, energyCost), action, cloneSource);
                } else {
                    cardPlayedSuccessfully = false;
                    break;
                }
            } else if (actionCtx == GameActionCtx.SELECT_CARD_DISCARD) {
                int possibleChoicesCount = 0, lastIdx = 0;
                boolean[] seen = new boolean[properties.realCardsLen];
                for (int j = 0; j < discardArrLen; j++) {
                    if (!seen[discardArr[j]]) {
                        if (currentAction.type() == GameActionType.PLAY_CARD && !properties.cardDict[currentAction.idx()].canSelectCard(properties.cardDict[discardArr[j]])) {
                            continue;
                        }
                        possibleChoicesCount++;
                        lastIdx = discardArr[j];
                        seen[discardArr[j]] = true;
                    }
                }
                if (selectIdx >= 0) {
                    setActionCtx(properties.cardDict[cardIdx].play(this, selectIdx, energyCost), action, cloneSource);
                    selectIdx = -1;
                } else if (possibleChoicesCount == 0) {
                    setActionCtx(GameActionCtx.PLAY_CARD, action, cloneSource);
                } else if (possibleChoicesCount == 1) {
                    setActionCtx(properties.cardDict[cardIdx].play(this, lastIdx, energyCost), action, cloneSource);
                } else {
                    break;
                }
            } else if (actionCtx == GameActionCtx.SELECT_CARD_HAND) {
                int possibleChoicesCount = 0, lastIdx = 0;
                boolean[] seen = new boolean[properties.cardDict.length];
                for (int j = 0; j < handArrLen; j++) {
                    if (!seen[handArr[j]]) {
                        if (currentAction.type() == GameActionType.PLAY_CARD && !properties.cardDict[currentAction.idx()].canSelectCard(properties.cardDict[handArr[j]])) {
                            continue;
                        }
                        possibleChoicesCount++;
                        lastIdx = handArr[j];
                        seen[handArr[j]] = true;
                    }
                }
                if (cardIdx == properties.wellLaidPlansCardIdx) {
                    if (selectIdx >= 0) {
                        setActionCtx(properties.cardDict[cardIdx].play(this, selectIdx, energyCost), action, cloneSource);
                        if (getCounterForRead()[properties.wellLaidPlansCounterIdx] >> 5 == (getCounterForRead()[properties.wellLaidPlansCounterIdx] & 31)) {
                            endTurn();
                            runActionsInQueueIfNonEmpty();
                        }
                        if (getNumCardsInHand() == 0) {
                            endTurn();
                            runActionsInQueueIfNonEmpty();
                        }
                    } else if (possibleChoicesCount == 0) {
                        endTurn();
                        runActionsInQueueIfNonEmpty();
                    }
                    return true;
                } else if (cardIdx == properties.gamblingChipsCardIdx) {
                    if (selectIdx >= 0) {
                        setActionCtx(properties.cardDict[cardIdx].play(this, selectIdx, energyCost), action, cloneSource);
                    } else if (possibleChoicesCount == 0) {
                        endTurn();
                        runActionsInQueueIfNonEmpty();
                    }
                    return true;
                }
                if (selectIdx >= 0) {
                    setActionCtx(properties.cardDict[cardIdx].play(this, selectIdx, energyCost), action, cloneSource);
                    selectIdx = -1;
                } else if (possibleChoicesCount == 0) {
                    setActionCtx(GameActionCtx.PLAY_CARD, action, cloneSource);
                } else if (possibleChoicesCount == 1) {
                    setActionCtx(properties.cardDict[cardIdx].play(this, lastIdx, energyCost), action, cloneSource);
                } else {
                    break;
                }
            } else if (actionCtx == GameActionCtx.SELECT_CARD_EXHAUST) {
                int possibleChoicesCount = 0, lastIdx = 0;
                boolean[] seen = new boolean[properties.realCardsLen];
                for (int j = 0; j < exhaustArrLen; j++) {
                    if (currentAction.type() == GameActionType.PLAY_CARD && !properties.cardDict[currentAction.idx()].canSelectCard(properties.cardDict[exhaustArr[j]])) {
                        continue;
                    }
                    if (!seen[exhaustArr[j]]) {
                        possibleChoicesCount++;
                        lastIdx = exhaustArr[j];
                        seen[exhaustArr[j]] = true;
                    }
                }
                if (selectIdx >= 0) {
                    setActionCtx(properties.cardDict[cardIdx].play(this, selectIdx, energyCost), action, cloneSource);
                    selectIdx = -1;
                } else if (possibleChoicesCount == 0) {
                    setActionCtx(GameActionCtx.PLAY_CARD, action, cloneSource);
                } else if (possibleChoicesCount == 1) {
                    setActionCtx(properties.cardDict[cardIdx].play(this, lastIdx, energyCost), action, cloneSource);
                } else {
                    break;
                }
            } else if (actionCtx == GameActionCtx.SELECT_CARD_DECK) {
                int possibleChoicesCount = 0, lastIdx = 0;
                boolean[] seen = new boolean[properties.realCardsLen];
                for (int j = 0; j < deckArrLen; j++) {
                    if (!seen[deckArr[j]]) {
                        if (currentAction.type() == GameActionType.PLAY_CARD && !properties.cardDict[currentAction.idx()].canSelectCard(properties.cardDict[deckArr[j]])) {
                            continue;
                        }
                        possibleChoicesCount++;
                        lastIdx = deckArr[j];
                        seen[deckArr[j]] = true;
                    }
                }
                if (selectIdx >= 0) {
                    setActionCtx(properties.cardDict[cardIdx].play(this, selectIdx, energyCost), action, cloneSource);
                    selectIdx = -1;
                } else if (possibleChoicesCount == 0) {
                    setActionCtx(GameActionCtx.PLAY_CARD, action, cloneSource);
                } else if (possibleChoicesCount == 1) {
                    setActionCtx(properties.cardDict[cardIdx].play(this, lastIdx, energyCost), action, cloneSource);
                } else {
                    break;
                }
            } else if (actionCtx == GameActionCtx.SCRYING) {
                if (scryCardIsKept != null) {
                    setActionCtx(GameActionCtx.SCRYING, action, cloneSource);
                } else {
                    setActionCtx(properties.cardDict[cardIdx].play(this, -1, energyCost), action, cloneSource);
                }
                break;
            } else if (actionCtx == GameActionCtx.SELECT_CARD_1_OUT_OF_3) {
                break;
            } else if (actionCtx == GameActionCtx.PLAY_CARD) {
                setActionCtx(properties.cardDict[cardIdx].play(this, -1, energyCost), action, cloneSource);
            }
        } while (actionCtx != GameActionCtx.PLAY_CARD && actionCtx != GameActionCtx.START_OF_BATTLE);

        if (actionCtx == GameActionCtx.PLAY_CARD && isCardPlayed) {
//            runActionsInQueueIfNonEmpty(); // this is bugged?
            removeWristBladeBuff();
            if (cardPlayedSuccessfully && properties.previousCardPlayTracking) {
                lastCardPlayedType = properties.cardDict[cardIdx].cardType;
            }
            // Determine cloneParentLocation before calling handlers
            if (cloneSource == null && !exhaustWhenPlayed) {
                if (properties.cardDict[cardIdx].exhaustWhenPlayed) {
                    cloneParentLocation = GameState.EXHAUST;
                } else if ((buffs & PlayerBuff.CORRUPTION.mask()) != 0 && properties.cardDict[cardIdx].cardType == Card.SKILL) {
                    cloneParentLocation = GameState.EXHAUST;
                } else if (properties.cardDict[cardIdx].returnToDeckWhenPlay) {
                    cloneParentLocation = GameState.DECK;
                } else if (properties.cardDict[cardIdx].cardType != Card.POWER) {
                    if (properties.reboundCounterIdx >= 0 && getCounterForRead()[properties.reboundCounterIdx] > 0) {
                        if ((getCounterForRead()[properties.reboundCounterIdx] & (1 << 8)) != 0) {
                            cloneParentLocation = GameState.DISCARD;
                        } else {
                            cloneParentLocation = GameState.DECK;
                        }
                    } else {
                        cloneParentLocation = GameState.DISCARD;
                    }
                }
            }
            for (var handler : properties.onCardPlayedHandlers) {
                handler.handle(this, cardIdx, lastSelectedIdx, energyCost, cloneSource, cloneParentLocation);
            }
            int transformCardIdx = properties.cardDict[cardIdx].onPlayTransformCardIdx(properties, cardIdx);
            int prevCardIdx = cardIdx;
            if (transformCardIdx >= 0) {
                cardIdx = transformCardIdx;
            }
            if (cloneSource == null && !exhaustWhenPlayed) {
                if (properties.cardDict[cardIdx].exhaustWhenPlayed) {
                    exhaustedCardHandle(cardIdx, true);
                } else if ((buffs & PlayerBuff.CORRUPTION.mask()) != 0 && properties.cardDict[cardIdx].cardType == Card.SKILL) {
                    exhaustedCardHandle(cardIdx, true);
                } else if (properties.cardDict[cardIdx].returnToDeckWhenPlay) {
                    addCardToDeck(cardIdx);
                } else if (properties.cardDict[cardIdx].cardType != Card.POWER) {
                    if (properties.reboundCounterIdx >= 0 && getCounterForRead()[properties.reboundCounterIdx] > 0) {
                        if ((getCounterForRead()[properties.reboundCounterIdx] & (1 << 8)) != 0) {
                            addCardToDiscard(cardIdx);
                            getCounterForWrite()[properties.reboundCounterIdx]++;
                            getCounterForWrite()[properties.reboundCounterIdx] ^= 1 << 8;
                        } else {
                            addCardOnTopOfDeck(cardIdx);
                        }
                    } else {
                        addCardToDiscard(cardIdx);
                    }
                }
                if (properties.reboundCounterIdx >= 0 && getCounterForRead()[properties.reboundCounterIdx] > 0) {
                    getCounterForWrite()[properties.reboundCounterIdx]--;
                }
            } else if (cloneSource != null && prevCardIdx != cardIdx) {
                if (cloneParentLocation == GameState.DISCARD) {
                    transformTopMostCard(getDiscardArrForWrite(), discardArrLen, prevCardIdx, cardIdx);
                } else if (cloneParentLocation == GameState.EXHAUST) {
                    transformTopMostCard(getExhaustArrForWrite(), exhaustArrLen, prevCardIdx, cardIdx);
                } else if (cloneParentLocation == GameState.DECK) {
                    transformTopMostCard(getDeckArrForWrite(), deckArrLen, prevCardIdx, cardIdx);
                }
            }
            if (cardPlayedSuccessfully) {
                if (properties.cardDict[cardIdx].delayUseEnergy && useEnergy) {
                    energy -= realEnergyCost;
                }
                runActionsInQueueIfNonEmpty();
            }
            for (Enemy enemy : enemies.iterateOverAlive()) {
                enemy.react(this, properties.cardDict[cardIdx]);
            }
            if (properties.normalityCounterIdx < 0 || counter[properties.normalityCounterIdx] < 3) { // todo: hack
                if (runActionQueueOnEnd) {
                    runActionsInQueueIfNonEmpty();
                }
            }
        } else if (actionCtx == GameActionCtx.START_OF_BATTLE) {
            startOfBattleActionIdx++;
            if (startOfBattleActionIdx >= properties.startOfBattleActions.size()) {
                setActionCtx(GameActionCtx.BEGIN_BATTLE, null, null);
                legalActions = null;
                doAction(0);
            } else {
                runStartOfBattleActionLoop();
            }
        } else {
            runActionsInQueueDuringCardPlayIfNonEmpty();
        }
        return cardPlayedSuccessfully || targetHalfAlive;
    }

    private void runStartOfBattleActionLoop() {
        while (actionCtx == GameActionCtx.START_OF_BATTLE && startOfBattleActionIdx < properties.startOfBattleActions.size()) {
            if (properties.startOfBattleActions.get(startOfBattleActionIdx).isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
                var card = properties.startOfBattleActions.get(startOfBattleActionIdx).startOfBattleAction;
                setActionCtx(card.play(this, properties.findCardIndex(card), 0), findPlayCardAction(card), null);
            }
            if (actionCtx == GameActionCtx.START_OF_BATTLE) {
                startOfBattleActionIdx++;
                if (startOfBattleActionIdx >= properties.startOfBattleActions.size()) {
                    setActionCtx(GameActionCtx.BEGIN_BATTLE, null, null);
                    break;
                }
            }
        }
    }

    private void transformTopMostCard(short[] cardArrForWrite, int cardArrLen, int cardIdx, int newCardIdx) {
        for (int i = cardArrLen - 1; i >= 0; i--) {
            if (cardArrForWrite[i] == cardIdx) {
                cardArrForWrite[i] = (short) newCardIdx;
                break;
            }
        }
    }

    public void usePotion(GameAction action, int selectIdx) {
        int potionIdx = action.idx();
        if (actionCtx == GameActionCtx.PLAY_CARD) {
            if (properties.toyOrnithopter != null) {
                healPlayer(5);
            }
            if (properties.potions.get(potionIdx).selectEnemy) {
                setActionCtx(GameActionCtx.SELECT_ENEMY, action, null);
            } else if (properties.potions.get(potionIdx).selectFromHand) {
                setActionCtx(GameActionCtx.SELECT_CARD_HAND, action, null);
            } else if (properties.potions.get(potionIdx).selectFromDiscard) {
                setActionCtx(GameActionCtx.SELECT_CARD_DISCARD, action, null);
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
                if (selectIdx >= 0) {
                    onSelectEnemy(selectIdx);
                    setActionCtx(properties.potions.get(potionIdx).use(this, selectIdx), action, null);
                    selectIdx = -1;
                } else if (targetableEnemies == 1) {
                    onSelectEnemy(idx);
                    setActionCtx(properties.potions.get(potionIdx).use(this, idx), action, null);
                } else {
                    break;
                }
            } else if (actionCtx == GameActionCtx.SELECT_CARD_DISCARD) {
                int possibleChoicesCount = 0, lastIdx = 0;
                boolean[] seen = new boolean[properties.realCardsLen];
                for (int j = 0; j < discardArrLen; j++) {
                    if (!seen[discardArr[j]]) {
                        possibleChoicesCount++;
                        lastIdx = discardArr[j];
                        seen[discardArr[j]] = true;
                    }
                }
                if (selectIdx >= 0) {
                    setActionCtx(properties.potions.get(potionIdx).use(this, selectIdx), action, null);
                    selectIdx = -1;
                } else if (possibleChoicesCount == 0) {
                    setActionCtx(properties.potions.get(potionIdx).use(this, -1), action, null);
                } else if (possibleChoicesCount == 1) {
                    setActionCtx(properties.potions.get(potionIdx).use(this, lastIdx), action, null);
                } else {
                    break;
                }
            } else if (actionCtx == GameActionCtx.SELECT_CARD_HAND) {
                int possibleChoicesCount = 0;
                boolean[] seen = new boolean[properties.cardDict.length];
                for (int j = 0; j < handArrLen; j++) {
                    if (!seen[handArr[j]]) {
                        possibleChoicesCount++;
                        seen[handArr[j]] = true;
                    }
                }
                if (selectIdx >= 0) {
                    setActionCtx(properties.potions.get(potionIdx).use(this, selectIdx), action, null);
                    selectIdx = -1;
                } else if (possibleChoicesCount == 0) {
                    setActionCtx(properties.potions.get(potionIdx).use(this, -1), action, null);
                } else {
                    break;
                }
            } else if (actionCtx == GameActionCtx.SELECT_CARD_1_OUT_OF_3) {
                break;
            } else if (actionCtx == GameActionCtx.PLAY_CARD) {
                setActionCtx(properties.potions.get(potionIdx).use(this, -1), action, null);
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

    public void runActionsInQueueDuringCardPlayIfNonEmpty() {
        while (gameActionDeque != null && gameActionDeque.size() > 0 && gameActionDeque.peekFirst().canHappenInsideCardPlay() && isTerminal() == 0) {
            gameActionDeque.pollFirst().doAction(this);
        }
    }

    void beginTurn() {
        actionsInCurTurn = 0;
        if (turnNum == 0) { // start of turn 1
            List<Integer> order = new ArrayList<>();
            for (int i = 0; i < deckArrLen; i++) { // todo: edge case more innate cards than first turn draw
                if (properties.cardDict[deckArr[i]].innate) {
                    order.add((int) deckArr[i]);
                }
            }
            for (int i = 0; i < properties.relics.size(); i++) {
                if (properties.relics.get(i) instanceof Relic._BottledRelic relic && relic.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
                    order.add(properties.findCardIndex(relic.card));
                }
            }
            for (int i = 0; i < order.size(); i++) {
                putCardOnTopOfDeck(order.get(i));
            }
            if (properties.frozenEye != null && properties.frozenEye.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
                setIsStochastic();
                Utils.shuffle(this, getDeckArrForWrite(), deckArrLen - deckArrFixedDrawLen, getSearchRandomGen());
                deckArrFixedDrawLen = deckArrLen;
            }
        }
        runActionsInQueueIfNonEmpty();
        playerTurnStartMaxPossibleHealth = getMaxPossibleHealth();
        playerTurnStartPotionCount = getPotionCount();
        playerTurnStartMaxHandOfGreed = (byte) CardColorless.HandOfGreed.getMaxPossibleHandOfGreed(this);
        playerTurnStartMaxRitualDagger = (byte) CardColorless.RitualDagger.getMaxPossibleRitualDagger(this);
        gainEnergy(energyRefill);
        if (properties.character == CharacterEnum.WATCHER) {
            exitDivinityAtStartOfTurn();
        }
        triggerOrbsPassiveStartOfTurn();
        if ((buffs & PlayerBuff.USED_VAULT.mask()) == 0) {
            var enemies = getEnemiesForWrite();
            for (int i = 0; i < enemies.size(); i++) {
                var enemy = enemies.get(i);
                if (enemy.isAlive() || enemy.properties.canSelfRevive) {
                    var enemy2 = enemies.getForWrite(i);
                    enemy2.endTurn(turnNum);
                    if (!properties.isRunicDomeEnabled(this)) {
                        var oldIsStochastic = isStochastic;
                        isStochastic = false;
                        enemy2.nextMove(this, getSearchRandomGen());
                        if (properties.makingRealMove && isStochastic) {
                            getStateDesc().append(getStateDesc().length() > 0 ? "; " : "").append(enemy2.getName()).append(" (").append(i).append(") choose move ").append(enemy2.getMoveString(this));
                        }
                        isStochastic = oldIsStochastic | isStochastic;
                    } else {
                    // for enemy like GremlinLeader, we need to choose move based on the beginning of turn
                    // currently doing it like this so instead of saving last 2 moves so that
                    // enemyChooseMove leading to multiple tree where the turn is the same (since nn is passed in the same info)
                    // we have one tree that has a chance event on beginning of turn
                        enemy2.saveStateForNextMove(this);
                    }
                }
            }
        }
        buffs &= ~PlayerBuff.USED_VAULT.mask();
        if (properties.toolbox != null && turnNum == 0 && properties.toolbox.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
            Relic.Toolbox.changeToSelectionCtx(this);
        } else {
            beginTurnPart2();
        }
    }

    void beginTurnPart2() {
        turnNum++;
        realTurnNum++;
        var drawCount = 5;
        for (GameEventHandler handler : properties.preStartOfTurnHandlers) {
            handler.handle(this);
        }
        if (properties.drawReductionCounterIdx >= 0 && getCounterForRead()[properties.drawReductionCounterIdx] > 0) {
            drawCount--;
            getCounterForWrite()[properties.drawReductionCounterIdx]--;
        }
        if (properties.toolsOfTheTradeCounterIdx >= 0) {
            drawCount += getCounterForRead()[properties.toolsOfTheTradeCounterIdx];
        }
        if (properties.sneckoEye != null) {
            drawCount += 2;
        }
        draw(drawCount);
        for (GameEventHandler handler : properties.startOfTurnHandlers) {
            handler.handle(this);
        }
        if (properties.toolsOfTheTradeCounterIdx >= 0 && getCounterForRead()[properties.toolsOfTheTradeCounterIdx] > 0) {
            getCounterForWrite()[properties.toolsOfTheTradeCounterIdx] += getCounterForRead()[properties.toolsOfTheTradeCounterIdx] << 16;
            var action = properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][properties.toolsOfTheTradeCardIdx];
            setActionCtx(GameActionCtx.SELECT_CARD_HAND, action, null);
            return;
        } else if (properties.foresightCounterIdx >= 0 && getCounterForRead()[properties.foresightCounterIdx] > 0) {
            var action = properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][properties.foresightCardIdx];
            setActionCtx(startScry(getCounterForRead()[properties.foresightCounterIdx]), action, null);
            return;
        }
        if (properties.gamblingChipsCardIdx >= 0 && turnNum == 1) {
            var action = properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][properties.gamblingChipsCardIdx];
            setActionCtx(GameActionCtx.SELECT_CARD_HAND, action, null);
        } else {
            setActionCtx(GameActionCtx.PLAY_CARD, null, null);
            runActionsInQueueIfNonEmpty();
        }
    }

    public void selectBiasedCognitionLimit() {
        if (turnNum > 0 || properties.biasedCognitionLimitSet) {
            return;
        }
        if (false) {
            this.getCounterForWrite()[properties.biasedCognitionLimitCounterIdx] = 40;
            properties.biasedCognitionLimitUsed = this.getCounterForWrite()[properties.biasedCognitionLimitCounterIdx];
            return;
        }
        if (properties.currentMCTS == null) {
            return;
        }
        boolean prevRngOn = false;
        if (properties.random instanceof InteractiveMode.RandomGenInteractive) {
            prevRngOn = ((InteractiveMode.RandomGenInteractive) properties.random).rngOn;
            ((InteractiveMode.RandomGenInteractive) properties.random).rngOn = true;
        }
        var q = properties.biasedCognitionLimitCache.get(this);
        double[] q_win, q_comb;
        if (q == null) {
            q_win = new double[100];
            q_comb = new double[100];
            properties.biasedCognitionLimitSet = true;
            for (int i = 0; i < 100; i++) {
                var c = this.clone(false);
                c.getCounterForWrite()[properties.biasedCognitionLimitCounterIdx] = i;
                var upto = properties.isTraining ? 1 : 100;
                for (int j = 0; j < upto; j++) {
                    properties.currentMCTS.search(c, false, -1);
                }
                q_win[i] = c.getQValueTreeSearch(GameState.V_WIN_IDX);
                q_comb[i] = c.getQValueTreeSearch(GameState.V_COMB_IDX);
            }
            properties.biasedCognitionLimitSet = false;
            var qTmp = properties.biasedCognitionLimitCache.putIfAbsent(this.clone(false), new Tuple<>(q_win, q_comb));
            q_win = qTmp == null ? q_win : qTmp.v1();
            q_comb = qTmp == null ? q_comb : qTmp.v2();
        } else {
            q_win = q.v1();
            q_comb = q.v2();
        }
        properties.biasedCognitionLimitDistribution = q_win;
        q_win = Arrays.copyOf(q_win, q_win.length);
        if (properties.isTraining) {
            var minQ = 100000.0;
            for (int i = 0; i < q_win.length; i++) {
                if (q_win[i] < minQ) {
                    minQ = q_win[i];
                }
            }
            var sum = 0.0;
            for (int i = 0; i < q_win.length; i++) {
                q_win[i] = Math.pow(q_win[i] - minQ, 3);
                sum += q_win[i];
            }
            for (int i = 0; i < q_win.length; i++) {
                q_win[i] /= sum;
            }
            var r = getSearchRandomGen().nextDouble(RandomGenCtx.Misc);
            var t = 0.0;
            for (int i = 0; i < q_win.length; i++) {
                t += q_win[i];
                if (t >= r) {
                    getCounterForWrite()[properties.biasedCognitionLimitCounterIdx] = i;
                    break;
                }
            }
        } else {
            // select highest
            var maxIdx = -1;
            var maxQ = -100.0;
            for (int i = 0; i < q_win.length; i++) {
                if (q_win[i] > maxQ) {
                    maxQ = q_win[i];
                    maxIdx = i;
                }
            }
            var maxIdx2 = maxIdx;
//            maxQ = q_comb[maxIdx];
//            for (int i = 0; i < q_comb.length; i++) {
//                if (q_comb[i] > maxQ && q_win[i] > q_win[maxIdx] * 0.99) {
//                    maxQ = q_comb[i];
//                    maxIdx2 = i;
//                }
//            }
            getCounterForWrite()[properties.biasedCognitionLimitCounterIdx] = maxIdx2;
        }
        if (properties.random instanceof InteractiveMode.RandomGenInteractive) {
            ((InteractiveMode.RandomGenInteractive) properties.random).rngOn = prevRngOn;
        }
        properties.biasedCognitionLimitUsed = this.getCounterForWrite()[properties.biasedCognitionLimitCounterIdx];
    }

    private boolean isDiscardingCardEndOfTurn() {
        if (properties.runicPyramid != null && properties.runicPyramid.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
            return false;
        }
        if (properties.equilibriumCounterIdx >= 0 && getCounterForRead()[properties.equilibriumCounterIdx] > 0) {
            return false;
        }
        return true;
    }

    public void endTurn() {
        if (properties.wellLaidPlansCardIdx >= 0 && actionCtx != GameActionCtx.SELECT_CARD_HAND && getCounterForRead()[properties.wellLaidPlansCounterIdx] > 0 &&
                isDiscardingCardEndOfTurn() && getNumCardsInHand() > 0) {
            var action = properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][properties.wellLaidPlansCardIdx];
            setActionCtx(GameActionCtx.SELECT_CARD_HAND, action, null);
            return;
        }
        if (properties.gamblingChipsCardIdx >= 0 && actionCtx == GameActionCtx.SELECT_CARD_HAND) {
            setActionCtx(GameActionCtx.PLAY_CARD, null, null);
            properties.cardDict[properties.gamblingChipsCardIdx].play(this, -1, 0);
            return;
        }
        setActionCtx(GameActionCtx.BEGIN_TURN, null, null);
        for (GameEventHandler handler : properties.preEndTurnHandlers) {
            handler.handle(this);
        }
        runActionsInQueueIfNonEmpty();
        getPlayerForWrite().preEndTurn(this);
        triggerOrbsPassiveEndOfTurn();

        // set temp cost cards to original cost
        if (properties.cardDict.length != properties.realCardsLen) {
            handArrTransform(properties.tmpModifiedCardReverseTransformIdxes);
        }
        if (chosenCardsArr != null) {
            chosenCardsArr = Arrays.copyOf(chosenCardsArr, chosenCardsArrLen);
            if (properties.cardDict.length != properties.realCardsLen) {
                for (int i = 0; i < chosenCardsArrLen; i++) {
                    if (properties.tmpModifiedCardReverseTransformIdxes[chosenCardsArr[i]] >= 0) {
                        chosenCardsArr[i] = (short) properties.tmpModifiedCardReverseTransformIdxes[chosenCardsArr[i]];
                    }
                }
            }
        }

        // trigger burn, regret etc.
        int len = handArrLen + chosenCardsArrLen;
        for (int i = 0; i < handArrLen; i++) {
            if (properties.cardDict[handArr[i]].alwaysDiscard) {
                properties.cardDict[handArr[i]].onDiscardEndOfTurn(this, len);
            }
        }
        for (int i = 0; i < chosenCardsArrLen; i++) {
            if (properties.cardDict[chosenCardsArr[i]].alwaysDiscard) {
                properties.cardDict[chosenCardsArr[i]].onDiscardEndOfTurn(this, len);
            }
        }
        runActionsInQueueIfNonEmpty();

        // discard cards from hand
        for (int i = handArrLen - 1; i >= 0; i--) {
            if (properties.cardDict[handArr[i]].ethereal) {
                exhaustedCardHandle(handArr[i], false);
                getHandArrForWrite()[i] = -1;
            } else if (properties.cardDict[handArr[i]].alwaysDiscard) {
                addCardToDiscard(handArr[i]);
                getHandArrForWrite()[i] = -1;
            } else if (properties.cardDict[handArr[i]].retain()) {
                // Retain cards stay in hand, call onRetain handlers
                for (var handler : properties.onRetainHandlers) {
                    handler.handle(this, i, -1, -1, null, -1);
                }
            } else if (isDiscardingCardEndOfTurn()) {
                addCardToDiscard(handArr[i]);
                getHandArrForWrite()[i] = -1;
            }
        }
        updateHandArr();

        // remove temporary retain from cards (after retain processing is complete)
        handArrTransform(properties.tmpModifiedCardReverseTransformIdxes);

        // handle well laid plan cards
        for (int i = 0; i < chosenCardsArrLen; i++) {
            if (properties.cardDict[chosenCardsArr[i]].ethereal) {
                exhaustedCardHandle(chosenCardsArr[i], false);
            } else if (properties.cardDict[chosenCardsArr[i]].alwaysDiscard) {
                addCardToDiscard(chosenCardsArr[i]);
            } else {
                addCardToHand(chosenCardsArr[i]);
            }
        }
        chosenCardsArrLen = 0;
        if (properties.hoveringKiteCounterIdx >= 0 && getCounterForRead()[properties.hoveringKiteCounterIdx] > 0) {
            getCounterForWrite()[properties.hoveringKiteCounterIdx] = 0;
        }
        runActionsInQueueIfNonEmpty();

        if (properties.timeEaterCounterIdx >= 0 && getCounterForRead()[properties.timeEaterCounterIdx] == 12) {
            for (var enemy : getEnemiesForWrite().iterateOverAlive()) {
                if (enemy instanceof EnemyBeyond.TimeEater) {
                    enemy.gainStrength(2);
                }
            }
        }
        var enemies = getEnemiesForWrite();
        int[] livingEnemies = new int[enemies.size()];
        int livingEnemiesCount = 0;
        boolean atLeastOneAlive = false;
        for (int i = 0; i < enemies.size(); i++) {
            var enemy = enemies.get(i);
            if (enemy.isAlive() || enemy.properties.canSelfRevive) {
                livingEnemies[livingEnemiesCount++] = i;
                if (enemy.isAlive()) {
                    atLeastOneAlive = true;
                }
            }
        }
        if (atLeastOneAlive && (buffs & PlayerBuff.USED_VAULT.mask()) == 0) {
            for (int i = 0; i < livingEnemiesCount; i++) {
                if (enemies.getForWrite(livingEnemies[i]).getHealth() > 0) {
                    enemies.getForWrite(livingEnemies[i]).startTurn(this);
                }
            }
            for (int i = 0; i < livingEnemiesCount; i++) {
                var enemy2 = enemies.getForWrite(livingEnemies[i]);
                if (properties.isRunicDomeEnabled(this)) {
                    var oldIsStochastic = isStochastic;
                    isStochastic = false;
                    enemy2.nextMove(this, getSearchRandomGen());
                    if (isStochastic && properties.random instanceof InteractiveMode.RandomGenInteractive rgi && !rgi.rngOn) {
                        rgi.selectEnemyMove(this, enemy2, i);
                    }
                    if (properties.makingRealMove && isStochastic) {
                        getStateDesc().append(getStateDesc().length() > 0 ? "; " : "").append(enemy2.getName()).append(" (").append(i).append(") choose move ").append(enemy2.getMoveString(this));
                    }
                    isStochastic = oldIsStochastic | isStochastic;
                }
                if (enemy2.isAlive() || enemy2.properties.canSelfRevive) {
                    enemy2.doMove(this, enemy2);
                }
                if (i != livingEnemiesCount - 1) {
                    runActionsInQueueIfNonEmpty();
                }
            }
        }
        for (GameEventHandler handler : properties.endOfTurnHandlers) {
            handler.handle(this);
        }
        if (properties.timeEaterCounterIdx >= 0 && getCounterForRead()[properties.timeEaterCounterIdx] == 12) {
            getCounterForWrite()[properties.timeEaterCounterIdx] = 0;
        }
        buffs &= ~PlayerBuff.END_TURN_IMMEDIATELY.mask();
        getPlayerForWrite().endTurn(this);
        if (properties.iceCream == null || !properties.iceCream.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
            energy = 0;
        }
    }

    public GameState doAction(int actionIdx) {
        GameAction action = properties.actionsByCtx[actionCtx.ordinal()][getLegalActions()[actionIdx]];
        if (action.type() == GameActionType.BEGIN_BATTLE) {
            if (startOfBattleActionIdx < properties.startOfBattleActions.size()) {
                setActionCtx(GameActionCtx.START_OF_BATTLE, null, null);
                runStartOfBattleActionLoop();
            }
            if (actionCtx == GameActionCtx.BEGIN_BATTLE) {
                if (properties.randomization != null) {
                    if (properties.isHeartGauntlet && battleRandomizationIdxChosen >= 0) {
                        properties.randomization.randomize(this, battleRandomizationIdxChosen);
                    } else {
                        battleRandomizationIdxChosen = properties.randomization.randomize(this);
                        setIsStochastic();
                    }
                }
                properties.enemyHealthRandomization.randomize(this);
                for (GameEventHandler handler : properties.startOfBattleHandlers) {
                    handler.handle(this);
                }
                if (properties.biasedCognitionLimitCounterIdx >= 0) {
                    setActionCtx(GameActionCtx.AFTER_RANDOMIZATION, null, null);
                    selectBiasedCognitionLimit();
                } else {
                    beginTurn();
                }
            }
        } else if (action.type() == GameActionType.AFTER_RANDOMIZATION) {
            beginTurn();
        } else if (action.type() == GameActionType.END_TURN) {
            endTurn();
        } else if (action.type() == GameActionType.PLAY_CARD) {
            playCard(action, -1, true, null, true, false, -1, -1);
        } else if (action.type() == GameActionType.SELECT_ENEMY) {
            if (currentAction.type() == GameActionType.PLAY_CARD) {
                playCard(currentAction, action.idx(), true, actionCardCloneSource, true, false, -1, -1);
            } else if (currentAction.type() == GameActionType.USE_POTION) {
                usePotion(currentAction, action.idx());
            }
        } else if (action.type() == GameActionType.SELECT_CARD_HAND) {
            if (currentAction.type() == GameActionType.PLAY_CARD) {
                playCard(currentAction, action.idx(), true, actionCardCloneSource, true, false, -1, -1);
            } else if (currentAction.type() == GameActionType.USE_POTION) {
                usePotion(currentAction, action.idx());
            }
        } else if (action.type() == GameActionType.SELECT_CARD_DISCARD) {
            if (currentAction.type() == GameActionType.PLAY_CARD) {
                playCard(currentAction, action.idx(), true, actionCardCloneSource, true, false, -1, -1);
            } else if (currentAction.type() == GameActionType.USE_POTION) {
                usePotion(currentAction, action.idx());
            }
        } else if (action.type() == GameActionType.SELECT_CARD_EXHAUST) {
            playCard(currentAction, action.idx(), true, actionCardCloneSource, true, false, -1, -1);
        } else if (action.type() == GameActionType.SELECT_CARD_DECK) {
            playCard(currentAction, action.idx(), true, actionCardCloneSource, true, false, -1, -1);
        } else if (action.type() == GameActionType.SCRY_KEEP_CARD) {
            if (handleScryCardDecision(action.idx())) { // finished scrying
                for (var handler : properties.onScryHandlers) {
                    handler.handle(this);
                }
                playCard(currentAction, action.idx(), true, actionCardCloneSource, true, false, -1, -1);
            }
        } else if (action.type() == GameActionType.SELECT_CARD_1_OUT_OF_3) {
            if (turnNum == 0) {
                if (Configuration.HEART_GAUNTLET_CARD_REWARD) { // todo
                    addCardToDeck(action.idx(), false);
                    setActionCtx(GameActionCtx.BEGIN_BATTLE, null, null);
                } else {
                    addCardToHandGeneration(action.idx());
                    beginTurnPart2();
                }
            } else {
                if (properties.cardDict[action.idx()].select1OutOf3CardEffectCard) {
                    properties.cardDict[action.idx()].play(this, 0, 0);
                } else {
                    addCardToHandGeneration(action.idx());
                }
                setActionCtx(GameActionCtx.PLAY_CARD, null, null);
            }
        } else if (action.type() == GameActionType.USE_POTION) {
            if (properties.potions.get(action.idx()).isGenerated) {
                for (int i = properties.numOfPotionSlots - 1; i >= 0; i--) {
                    if (potionUsable(action.idx() + i)) {
                        getPotionsStateForWrite()[(action.idx() + i) * 3] = 0;
                    }
                }
            } else {
                getPotionsStateForWrite()[action.idx() * 3] = 0;
            }
            usePotion(action, -1);
        } else if (action.type() == GameActionType.BEGIN_PRE_BATTLE) {
            if (properties.preBattleRandomization != null) {
                if (properties.isHeartGauntlet && preBattleRandomizationIdxChosen >= 0) {
                    properties.preBattleRandomization.randomize(this, preBattleRandomizationIdxChosen);
                } else {
                    preBattleRandomizationIdxChosen = properties.preBattleRandomization.randomize(this);
                    setIsStochastic();
                }
            }
            setActionCtx(properties.preBattleScenarios == null ? GameActionCtx.BEGIN_BATTLE : GameActionCtx.SELECT_SCENARIO, null, null);
            if (properties.endOfPreBattleHandler != null && realTurnNum == 0) {
                properties.endOfPreBattleHandler.handle(this);
            }
        } else if (action.type() == GameActionType.SELECT_SCENARIO) {
            properties.preBattleScenarios.randomize(this, properties.preBattleGameScenariosList.get(action.idx()).getKey());
            setActionCtx(GameActionCtx.BEGIN_BATTLE, null, null);
            if (properties.perScenarioCommands != null) {
                legalActions = null;
                new InteractiveMode(new PrintStream(OutputStream.nullOutputStream())).interactiveApplyHistory(this, properties.perScenarioCommands.get(preBattleScenariosChosenIdx));
            }
        } else if (action.type() == GameActionType.BEGIN_TURN) {
            if (isTerminal() == 0) {
                beginTurn();
            }
        } else if (action.type() == GameActionType.END_SELECT_CARD_HAND) {
            if (currentAction.type() == GameActionType.USE_POTION) {
                setActionCtx(properties.potions.get(currentAction.idx()).use(this, properties.cardDict.length), action, null);
            } else if (currentAction.type() == GameActionType.PLAY_CARD) {
                endTurn();
                runActionsInQueueIfNonEmpty();
            }
        } else if (action.type() == GameActionType.END_SELECT_CARD_1_OUT_OF_3) {
            if (turnNum == 0) {
                if (Configuration.HEART_GAUNTLET_CARD_REWARD) { // todo
                    setActionCtx(GameActionCtx.BEGIN_BATTLE, null, null);
                } else {
                    beginTurnPart2();
                }
            } else {
                setActionCtx(GameActionCtx.PLAY_CARD, null, null);
            }
        }
        if (actionCtx == GameActionCtx.PLAY_CARD && properties.unceasingTop != null && properties.unceasingTop.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
            while (handArrLen == 0 && draw(1) >= 0) {
                runActionsInQueueIfNonEmpty();
            }
        }
        runActionsInQueueIfNonEmpty();
        if (actionCtx == GameActionCtx.BEGIN_TURN && properties.playCardOnTopOfDeckCounterIdx >= 0 && getCounterForRead()[properties.playCardOnTopOfDeckCounterIdx] > 0) {
            getCounterForWrite()[properties.playCardOnTopOfDeckCounterIdx] = 0;
        }
        while (actionCtx == GameActionCtx.PLAY_CARD && properties.playCardOnTopOfDeckCounterIdx >= 0 && getCounterForRead()[properties.playCardOnTopOfDeckCounterIdx] > 0 && isTerminal() == 0) {
            getCounterForWrite()[properties.playCardOnTopOfDeckCounterIdx]--;
            int cardIdx = drawOneCardSpecial();
            if (cardIdx < 0) {
                continue;
            }
            if (properties.makingRealMove || properties.stateDescOn) {
                if (!getStateDesc().isEmpty())
                    stateDesc.append(", (Play Card) ");
                getStateDesc().append(properties.cardDict[cardIdx].cardName);
            }
            var nextAction = properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
            playCard(nextAction, -1, true, null, false, false, -1, -1);
            while (actionCtx == GameActionCtx.SELECT_ENEMY && isTerminal() == 0) {
                int enemyIdx = GameStateUtils.getRandomEnemyIdx(this, RandomGenCtx.RandomEnemyGeneral);
                if (properties.makingRealMove || properties.stateDescOn) {
                    getStateDesc().append(" -> ").append(enemyIdx < 0 ? "None" : getEnemiesForRead().get(enemyIdx).getName())
                            .append(" (").append(enemyIdx).append(")");
                }
                playCard(nextAction, enemyIdx, true, null, false, false, -1, -1);
            }
        }
        actionsInCurTurn++;
        legalActions = null;
        v_extra = null;
        policy = null;
        if (isStochastic) {
            if (!Configuration.isTranspositionAcrossChanceNodeOn(this) || (action.type() == GameActionType.BEGIN_TURN || action.type() == GameActionType.BEGIN_BATTLE)) {
                transpositions = new HashMap<>();
                if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH) transpositionsParent = new HashMap<>();
            }
            searchFrontier = null;
        }
        if (isTerminal() > 0 && properties.switchBattleHandler != null) {
            return properties.switchBattleHandler.apply(this);
        }
        return this;
    }

    public GameAction findPlayCardAction(Card card) {
        return properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][properties.findCardIndex(card)];
    }

    boolean isActionLegal(int action) {
        return Arrays.binarySearch(getLegalActions(), action) >= 0;
    }

    private boolean isActionLegalInternal(int action) {
        if (actionCtx == GameActionCtx.BEGIN_BATTLE || actionCtx == GameActionCtx.BEGIN_PRE_BATTLE || actionCtx == GameActionCtx.AFTER_RANDOMIZATION || actionCtx == GameActionCtx.BEGIN_TURN) {
            return action == 0;
        } else if (actionCtx == GameActionCtx.SELECT_ENEMY) {
            GameAction[] a = properties.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()];
            if (a == null || action < 0 || action >= a.length) {
                return false;
            }
            return enemies.get(a[action].idx()).isAlive();
        } else if (actionCtx == GameActionCtx.SELECT_CARD_1_OUT_OF_3) {
            GameAction[] a = properties.actionsByCtx[GameActionCtx.SELECT_CARD_1_OUT_OF_3.ordinal()];
            if (action < 0 || action >= a.length) {
                return false;
            }
            if (a[action].type() != GameActionType.SELECT_CARD_1_OUT_OF_3) {
                return a[action].type() == GameActionType.END_SELECT_CARD_1_OUT_OF_3;
            }
            int idx = properties.select1OutOf3CardsReverseIdxes[a[action].idx()];
            return (select1OutOf3CardsIdxes & 255) == idx || ((select1OutOf3CardsIdxes >> 8) & 255) == idx || ((select1OutOf3CardsIdxes >> 16) & 255) == idx;
        } else if (actionCtx == GameActionCtx.SELECT_SCENARIO) {
            GameAction[] a = properties.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()];
            return action >= 0 && action < a.length;
        }
        return false;
    }

    public void onSelectEnemy(int idx) {
        if (properties.shieldAndSpireFacingIdx >= 0) {
            if (getEnemiesForRead().get(idx) instanceof EnemyEnding.SpireShield) {
                getCounterForWrite()[properties.shieldAndSpireFacingIdx] = 1;
            } else if (getEnemiesForRead().get(idx) instanceof EnemyEnding.SpireSpear) {
                getCounterForWrite()[properties.shieldAndSpireFacingIdx] = 2;
            }
        }
    }

    public int getScenarioIdxChosen() {
        int idx = 0;
        if (properties.preBattleRandomization != null) {
            idx = preBattleRandomizationIdxChosen;
            if (preBattleRandomizationIdxChosen < 0) {
                System.out.println(this);
                throw new RuntimeException();
            }
        }
        if (properties.preBattleScenarios != null) {
            idx *= properties.preBattleScenarios.listRandomizations().size();
            idx += preBattleScenariosChosenIdx;
            if (preBattleScenariosChosenIdx < 0) {
                throw new RuntimeException();
            }
        }
        if (properties.randomization != null) {
            idx *= properties.randomization.listRandomizations().size();
            idx += battleRandomizationIdxChosen;
            if (battleRandomizationIdxChosen < 0) {
                throw new RuntimeException();
            }
        }
        return idx;
    }

    public boolean isLossFrom50() {
        return getPlayeForRead().getHealth() > 0 && turnNum > 50;
    }

    public double calcQValue(VArray v) {
        var health = v.get(V_HEALTH_IDX);
        var win = v.get(V_WIN_IDX);
        for (int i = 0; i < properties.potions.size(); i++) {
            if (potionsState[i * 3] == 0 && potionsState[i * 3 + 2] == 1 && properties.potions.get(i) instanceof Potion.BloodPotion pot && !GameProperties.isHeartFight(this)) {
                v.set(V_HEALTH_IDX, Math.max(v.get(V_HEALTH_IDX) - ((pot.getHealAmount(this) + 1) / (float) player.getMaxHealth()), 0));
                v.set(V_WIN_IDX, v.get(V_WIN_IDX) * potionsState[i * 3 + 1] / 100.0);
            }
            if (potionsState[i * 3] == 0 && potionsState[i * 3 + 2] == 1 && potionsState[i * 3 + 1] < 100 && properties.potions.get(i) instanceof Potion.RegenerationPotion pot && !GameProperties.isHeartFight(this)) {
                v.set(V_HEALTH_IDX, Math.max(v.get(V_HEALTH_IDX) - ((pot.getHealAmount(this) + 1) / (float) player.getMaxHealth()), 0));
                v.set(V_WIN_IDX, v.get(V_WIN_IDX) * potionsState[i * 3 + 1] / 100.0);
            }
            if (potionsState[i * 3] == 0 && potionsState[i * 3 + 2] == 1 && properties.potions.get(i) instanceof Potion.BlockPotion pot && !GameProperties.isHeartFight(this)) {
                v.set(V_HEALTH_IDX, Math.max(v.get(V_HEALTH_IDX) - ((pot.getBlockAmount(this) + 1) / (float) player.getMaxHealth()), 0));
                v.set(V_WIN_IDX, v.get(V_WIN_IDX) * potionsState[i * 3 + 1] / 100.0);
            }
        }
        for (int i = 0; i < properties.extraTrainingTargets.size(); i++) {
            properties.extraTrainingTargets.get(i).updateQValues(this, v);
        }
        double base = v.get(V_WIN_IDX) * 0.5 + v.get(V_WIN_IDX) * v.get(V_WIN_IDX) * v.get(V_HEALTH_IDX) * 0.5;
        v.set(V_WIN_IDX, win);
        v.set(V_HEALTH_IDX, health);
        for (int i = 0; i < properties.potions.size(); i++) {
            if (potionsState[i * 3] == 0 && potionsState[i * 3 + 2] == 1 && properties.potions.get(i) instanceof Potion.FairyInABottle) {
                base *= potionsState[i * 3 + 1] / 100.0 + (100 - potionsState[i * 3 + 1]) / 100.0 * v.get(GameState.V_EXTRA_IDX_START + properties.potionsVExtraIdx[i]);
                continue;
            }
            if (potionsState[i * 3] == 0 && potionsState[i * 3 + 2] == 1 && !(properties.potions.get(i) instanceof Potion.BloodPotion) && !(properties.potions.get(i) instanceof Potion.BlockPotion) && !(properties.potions.get(i) instanceof Potion.RegenerationPotion) && !GameProperties.isHeartFight(this)) {
                base *= potionsState[i * 3 + 1] / 100.0;
            }
        }
        if (properties.alchemizeVExtraIdx >= 0 && properties.alchemizeMult > 0 && !GameProperties.isHeartFight(this)) {
            var alchemizeMult = 0.0;
            for (int i = 0; i < 5; i++) {
                alchemizeMult += Math.pow(properties.alchemizeMult, i) * v.getVExtra(properties.alchemizeVExtraIdx + i);
            }
            base *= alchemizeMult;
        }
        return base;
    }

    public double calcQValue() {
        var out = new VArray(properties.v_total_len);
        getVArray(out);
        return calcQValue(out);
    }

    public double getQValueTreeSearch(int idx) {
        return getTotalQ(idx) / (total_n + 1);
    }

    void getVArray(VArray out) {
        var player = getPlayeForRead();
        if (player.getHealth() <= 0 || turnNum > 50) {
            out.reset();
            if (properties.extraOutputLen > 0) {
                if (Configuration.USE_FIGHT_PROGRESS_WHEN_LOSING) {
                    out.setVExtra(properties.fightProgressVExtraIdx, calcFightProgress(false));
                }
            }
            for (int i = 0; i < properties.extraTrainingTargets.size(); i++) {
                properties.extraTrainingTargets.get(i).fillVArray(this, out, -1);
            }
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
            out.set(V_WIN_IDX, 1);
            out.set(V_HEALTH_IDX, ((double) player.getHealth()) / player.getMaxHealth());
        } else {
            out.set(V_WIN_IDX, v_win);
            out.set(V_HEALTH_IDX, Math.min(v_health, getMaxPossibleHealth() / (float) getPlayeForRead().getMaxHealth()));
        }
        for (int i = 0; i < properties.extraTrainingTargets.size(); i++) {
            properties.extraTrainingTargets.get(i).fillVArray(this, out, enemiesAllDead ? 1 : 0);
        }
        out.set(V_COMB_IDX, calcQValue(out));
    }

    VArray cached_v;
    VArray getVArrayCached() {
        if (cached_v == null) {
            cached_v = new VArray(properties.v_total_len);
            getVArray(cached_v);
        }
        return cached_v;
    }

    public boolean isQInitialized() {
        return q_total != null;
    }

    public double getTotalQ(int i) {
        return q_total != null ? q_total.get(i) : 0;
    }

    public double getChildQ(int childIdx, int i) {
        if (q_child == null || q_child[childIdx] == null) {
            return 0;
        }
        return q_child[childIdx].get(i);
    }

    public void addTotalQ(int i, double v) {
        if (q_total == null) {
            q_total = new VArray(properties.v_total_len);
        }
        q_total.add(i, v);
    }

    public void addChildQ(int childIdx, int i, double v) {
        if (q_child == null) {
            q_child = new VArray[policy.length];
        }
        if (q_child[childIdx] == null) {
            q_child[childIdx] = new VArray(properties.v_total_len);
        }
        q_child[childIdx].add(i, v);
    }

    public VArray getTotalQArray() {
        if (q_total == null) {
            q_total = new VArray(properties.v_total_len);
        }
        return q_total;
    }

    public VArray getChildQArray(int childIdx) {
        if (q_child == null) {
            q_child = new VArray[policy.length];
        }
        if (q_child[childIdx] == null) {
            q_child[childIdx] = new VArray(properties.v_total_len);
        }
        return q_child[childIdx];
    }

    public double calcFightProgress(boolean onlyHeart) {
        int totalMaxHp = 0;
        int totalCurHp = 0;
        boolean isSlimeBossAlive = false;
        boolean isSpikeSlimeLAlive = false;
        boolean isAcidSlimeLAlive = false;
        boolean isGremlinLeaderAlive = false;
        for (EnemyReadOnly enemy : enemies) {
            if (enemy instanceof EnemyCity.GremlinLeader) {
                isGremlinLeaderAlive = true;
                break;
            }
        }
        for (int i = 0; i < getEnemiesForRead().size(); i++) {
            EnemyReadOnly enemy = getEnemiesForRead().get(i);
            boolean addedMod = false;
            if (enemy instanceof EnemyExordium.SlimeBoss boss) {
                isSlimeBossAlive = boss.getHealth() > 0;
            } else if (enemy instanceof EnemyExordium.LargeSpikeSlime slime) {
                isSpikeSlimeLAlive = slime.getHealth() > 0;
                if (isSlimeBossAlive) {
                    totalCurHp += enemy.properties.maxHealth;
                    totalMaxHp += enemy.properties.maxHealth;
                    addedMod = true;
                }
            } else if (enemy instanceof EnemyExordium.LargeAcidSlime slime) {
                isAcidSlimeLAlive = slime.getHealth() > 0;
                if (isSlimeBossAlive) {
                    totalCurHp += enemy.properties.maxHealth;
                    totalMaxHp += enemy.properties.maxHealth;
                    addedMod = true;
                }
            } else if (enemy instanceof EnemyExordium.MediumSpikeSlime) {
                if (isSlimeBossAlive || isSpikeSlimeLAlive) {
                    totalCurHp += enemy.properties.maxHealth;
                    totalMaxHp += enemy.properties.maxHealth;
                    addedMod = true;
                }
            } else if (enemy instanceof EnemyExordium.MediumAcidSlime) {
                if (isSlimeBossAlive || isAcidSlimeLAlive) {
                    totalCurHp += enemy.properties.maxHealth;
                    totalMaxHp += enemy.properties.maxHealth;
                    addedMod = true;
                }
            } else if (enemy instanceof EnemyBeyond.AwakenedOne ao) {
                totalCurHp += ao.isAwakened() ? ao.getHealth() : (ao.getHealth() + enemy.properties.maxHealth);
                totalMaxHp += enemy.properties.maxHealth * 2;
                addedMod = true;
            } else if (enemy instanceof EnemyCity.BronzeAutomaton ba) {
                if (ba.getMove() <= EnemyCity.BronzeAutomaton.SPAWN_ORBS) {
                    totalCurHp += 60 * 2; // the orbs
                }
            } else if (enemy instanceof EnemyCity.TorchHead) {
                addedMod = true;
            } else if (isGremlinLeaderAlive && ((enemy instanceof EnemyExordium.FatGremlin) || (enemy instanceof EnemyExordium.GremlinWizard) || (enemy instanceof EnemyExordium.MadGremlin) || (enemy instanceof EnemyExordium.ShieldGremlin) || (enemy instanceof EnemyExordium.SneakyGremlin))) {
                addedMod = true;
            } else if (properties.isHeartGauntlet && enemy instanceof EnemyEnding.CorruptHeart) {
                if (getEnemiesForRead().get(0).getHealth() > 0 || getEnemiesForRead().get(1).getHealth() > 0) {
                    if (onlyHeart) {
                        totalCurHp = 0;
                        totalMaxHp = enemy.properties.maxHealth;
                    } else {
                        totalCurHp += enemy.properties.maxHealth;
                        totalMaxHp += enemy.properties.maxHealth;
                        addedMod = true;
                    }
                } else if (onlyHeart) {
                    totalCurHp = enemy.getHealth();
                    totalMaxHp = enemy.properties.maxHealth;
                }
            }
            if (!addedMod) {
                totalCurHp += enemy.getHealth();
                totalMaxHp += enemy.properties.maxHealth;
            }
        }
        var t = 1 - ((double) totalCurHp) / totalMaxHp;
        return t;
    }

    private boolean checkIfCanHeal() {
        if (properties.burningBlood != null || properties.bloodyIdol != null || properties.bloodVial != null) {
            return true;
        }
        if (properties.toyOrnithopter != null && !properties.potions.isEmpty()) {
            return true;
        }
        if (properties.regenerationCounterIdx >= 0) {
            return true;
        }
        if (properties.selfRepairCounterIdx >= 0) {
            return true;
        }
        if (properties.birdFacedUrn != null) {
            return true;
        }
        if (properties.meatOnTheBone != null) {
            return getPlayeForRead().getHealth() < getPlayeForRead().getInBattleMaxHealth() / 2 + 12;
        }
        if (properties.healCardsIdxes == null) {
            return false;
        }
        if (properties.findCardIndex("Exhume") >= 0 || properties.findCardIndex("Exhume+") >= 0) {
            return true;
        }
        for (int i = 0; i < handArrLen; i++) {
            if (properties.healCardsBooleanArr[handArr[i]]) {
                return true;
            }
        }
        for (int i = 0; i < discardArrLen; i++) {
            if (properties.healCardsBooleanArr[discardArr[i]]) {
                return true;
            }
        }
        for (int i = 0; i < deckArrLen; i++) {
            if (properties.healCardsBooleanArr[deckArr[i]]) {
                return true;
            }
        }
        return false;
    }

    // todo: yeah domain knowledge is really really hard
    public int getMaxPossibleHealth() {
//        if (checkIfCanHeal()) {
            int maxHealPreBattleEnd = 0;
            int maxPossibleIncreaseInMaxHP = 0;
            int maxPossibleRegen = 0;
            int maxPossiblePowers = 0;
            if (!properties.potions.isEmpty()) {
                for (int i = 0; i < properties.potions.size(); i++) {
                    if (potionUsable(i) && properties.toyOrnithopter != null && !(properties.potions.get(i) instanceof Potion.FairyInABottle)) {
                        maxHealPreBattleEnd += 5;
                    }
                    if (potionUsable(i) && properties.potions.get(i) instanceof Potion.BloodPotion pot) {
                        maxHealPreBattleEnd += pot.getHealAmount(this);
                    }
                    if (potionUsable(i) && properties.potions.get(i) instanceof Potion.RegenerationPotion) {
                        maxPossibleRegen += Potion.RegenerationPotion.getRegenerationAmount(this);
                    }
                    if (potionUsable(i) && properties.potions.get(i) instanceof Potion.PowerPotion) {
                        maxPossiblePowers += 1000;
                    }
                    if (potionUsable(i) && properties.potions.get(i) instanceof Potion.ColorlessPotion) {
                        maxPossiblePowers += 1000;
                    }
                }
            }
            if (properties.potionsGenerator != null) {
                boolean canGeneratePotion = false;
                for (int i = 0; i < properties.potions.size(); i++) {
                    if (potionUsable(i) && properties.potions.get(i) instanceof Potion.EntropicBrew) {
                        canGeneratePotion = true;
                        break;
                    }
                }
                if (CardSilent._AlchemizeT.getPossibleAlchemize(this) > 0) {
                    canGeneratePotion = true;
                }
                if (canGeneratePotion) {
                    maxPossibleRegen += Potion.RegenerationPotion.getRegenerationAmount(this) * properties.numOfPotionSlots;
                    if (properties.toyOrnithopter != null) {
                        maxHealPreBattleEnd += 5 * properties.numOfPotionSlots;
                    }
                    if (properties.birdFacedUrn != null) {
                        maxPossiblePowers += 1000;
                    }
                }
            }
            if (properties.regenerationCounterIdx >= 0) {
                maxPossibleRegen += getCounterForRead()[properties.regenerationCounterIdx];
            }
            maxHealPreBattleEnd += maxPossibleRegen * (maxPossibleRegen + 1) / 2;
            if (properties.feedCounterIdx >= 0) {
                maxPossibleIncreaseInMaxHP = CardIronclad.Feed.getMaxPossibleFeedRemaining(this);
                maxHealPreBattleEnd += maxPossibleIncreaseInMaxHP;
            }
            if (properties.healCardsIdxes != null) {
                for (int i = 0; i < properties.healCardsIdxes.length; i++) {
                    if (properties.healCardsIdxes[i] < 0) {
                        continue;
                    }
                    // todo: need to count max strength
                    if (properties.cardDict[properties.healCardsIdxes[i]].cardName.startsWith("Reaper")) {
                        for (var enemy : getEnemiesForRead()) {
                            maxHealPreBattleEnd += enemy.getHealth();
                        }
                    }
                    if (properties.cardDict[properties.healCardsIdxes[i]].cardName.startsWith("Bite")) {
                        maxHealPreBattleEnd += 10000;
                    }
                    if (properties.birdFacedUrn != null && properties.cardDict[properties.healCardsIdxes[i]].cardType == Card.POWER) {
                        if (properties.cardDict[properties.healCardsIdxes[i]].cardName.startsWith("Creative AI")) {
                            maxPossiblePowers += 1000;
                        } else {
                            maxPossiblePowers++;
                        }
                    }
                    if (!GameProperties.isHeartFight(this) && properties.cardDict[properties.healCardsIdxes[i]].cardName.startsWith("Self Repair")) {
                        int m = getNonExhaustCount(properties.healCardsIdxes[i]);
                        if (m > 0) {
                            if (properties.echoFormCardIdx >= 0 || properties.echoFormPCardIdx >= 0) {
                                if (getCounterForRead()[properties.echoFormCounterIdx] > 0) {
                                    m *= 2;
                                } else if (properties.echoFormCardIdx >= 0 && getNonExhaustCount(properties.echoFormCardIdx) > 0) {
                                    m *= 2;
                                } else if (properties.echoFormPCardIdx >= 0 && getNonExhaustCount(properties.echoFormPCardIdx) > 0) {
                                    m *= 2;
                                }
                            }
                        }
                        int h = properties.cardDict[properties.healCardsIdxes[i]] instanceof CardDefect.SelfRepair ? 7 : 10;
                        if (properties.apotheosisCardIdx >= 0 && getNonExhaustCount(properties.apotheosisCardIdx) > 0) h = 10;
                        if (properties.apotheosisPCardIdx >= 0 && getNonExhaustCount(properties.apotheosisPCardIdx) > 0) h = 10;
                        maxHealPreBattleEnd += h * m;
                    }
                }
            }
            if (properties.deadBranch != null && properties.deadBranch.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
                maxPossiblePowers += 10000;
            }
            if (properties.birdFacedUrn != null && maxPossiblePowers > 0) {
                boolean canDup = getNonExhaustCount("Amplify") > 0 || getNonExhaustCount("Echo Form") > 0;
                maxHealPreBattleEnd += (canDup ? 4 : 2) * maxPossiblePowers;
            }

            int hp;
            int maxPossibleHealth = getPlayeForRead().getHealth();
            for (int i = 0; i < properties.potions.size(); i++) {
                if (potionUsable(i) && properties.potions.get(i) instanceof Potion.FairyInABottle pot) {
                    maxPossibleHealth = Math.max(maxPossibleHealth, pot.getHealAmount(this));
                }
            }
            var maxHealth = player.getInBattleMaxHealth() + maxPossibleIncreaseInMaxHP;
            if (properties.meatOnTheBone != null && properties.meatOnTheBone.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
                if ((maxPossibleHealth + maxHealPreBattleEnd) >= maxHealth / 2 + 12) {
                    hp = Math.min(maxHealth, maxPossibleHealth + maxHealPreBattleEnd);
                } else if (maxPossibleHealth + maxHealPreBattleEnd < maxHealth / 2) {
                    hp = maxPossibleHealth + maxHealPreBattleEnd + 12;
                } else {
                    hp = Math.min(maxHealth, Math.max(maxPossibleHealth + maxHealPreBattleEnd, maxHealth / 2 + 12));
                }
            } else {
                hp = Math.min(maxHealth, maxPossibleHealth + maxHealPreBattleEnd);
            }
            if (!GameProperties.isHeartFight(this) && properties.selfRepairCounterIdx >= 0) {
                hp += getCounterForRead()[properties.selfRepairCounterIdx];
            }
            if (properties.burningBlood != null && properties.burningBlood.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
                hp += 6;
            }
            if (properties.bloodyIdol != null && properties.bloodyIdol.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
                hp += 5;
            }
            if (!GameProperties.isHeartFight(this) && properties.bloodVial != null && properties.bloodVial.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
                hp += 2;
            }
            hp = Math.min(maxHealth, hp);
            return hp;
//        } else {
//            return getPlayeForRead().getHealth();
//        }
    }

    public int getNonExhaustCount(int cardIdx) {
        int count = 0;
        count += GameStateUtils.getCardCount(handArr, handArrLen, cardIdx);
        count += GameStateUtils.getCardCount(discardArr, discardArrLen, cardIdx);
        return count + GameStateUtils.getCardCount(deckArr, deckArrLen, cardIdx);
    }

    public int getNonExhaustCount(String prefix) {
        int count = 0;
        count += GameStateUtils.getCardCount(properties, handArr, handArrLen, prefix);
        count += GameStateUtils.getCardCount(properties, discardArr, discardArrLen, prefix);
        return count + GameStateUtils.getCardCount(properties, deckArr, deckArrLen, prefix);
    }

    public int isTerminal() {
        if (getPlayeForRead().getHealth() <= 0 || turnNum > 50 || actionsInCurTurn > 100) {
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

    public byte getPotionCount() {
        byte ret = 0;
        if (potionsState != null) {
            for (int i = 0; i < potionsState.length; i += 3) {
                if (!(properties.potions.get(i / 3) instanceof Potion.LizardTail)) {
                    ret += potionsState[i];
                }
            }
        }
        return ret;
    }

    public int getNumCardsInDeck() {
        return deckArrLen;
    }

    public int getNumCardsInHand() {
        return handArrLen;
    }

    public int getNumCardsInDiscard() {
        return discardArrLen;
    }

    public int getNumCardsInExhaust() {
        return exhaustArrLen;
    }

    @Override public String toString() {
        boolean first;
        StringBuilder str = new StringBuilder("{");
        str.append("turn=").append(turnNum);
        if (realTurnNum != turnNum) {
            str.append(" (").append(realTurnNum).append(")");
        }
        str.append(", ");
        if (properties.biasedCognitionLimitCounterIdx >= 0) {
            str.append("biasedCogLimit=").append(getCounterForRead()[properties.biasedCognitionLimitCounterIdx]).append(", ");
        }
        str.append("hand=[");
        first = true;
        var hand = GameStateUtils.getCardArrCounts(handArr, handArrLen, properties.cardDict.length);
        for (int i = 0; i < hand.length; i++) {
            if (hand[i] > 0 && (!properties.discard0CardOrderMatters || properties.cardDict[i].realEnergyCost() != 0)) {
                if (!first) {
                    str.append(", ");
                }
                first = false;
                str.append(hand[i]).append(" ").append(properties.cardDict[i].cardName);
            }
        }
        if (properties.discard0CardOrderMatters) {
            for (int i = 0; i < handArrLen; i++) {
                if (properties.cardDict[handArr[i]].realEnergyCost() == 0) {
                    if (!first) {
                        str.append(", ");
                    }
                    first = false;
                    str.append(properties.cardDict[handArr[i]].cardName);
                }
            }
        }
        str.append("]");
        str.append(", deck=[");
        first = true;
        var deck = GameStateUtils.getCardArrCounts(deckArr, deckArrLen, properties.realCardsLen);
        if (deckArrFixedDrawLen > 0) {
            first = false;
            str.append("fixedDrawLen=").append(deckArrFixedDrawLen);
            for (int i = 0; i < deckArrFixedDrawLen; i++) {
                str.append(", ").append("(").append(i + 1).append(") ").append(properties.cardDict[deckArr[deckArrLen - 1 - i]].cardName);
                deck[deckArr[i]]--;
            }
        }
        for (int i = 0; i < deck.length; i++) {
            if (deck[i] > 0) {
                if (!first) {
                    str.append(", ");
                }
                first = false;
                str.append(deck[i]).append(" ").append(properties.cardDict[i].cardName);
            }
        }
        str.append("]");
        str.append(", discard=[");
        first = true;
        var discard = GameStateUtils.getCardArrCounts(discardArr, discardArrLen, properties.realCardsLen);
        for (int i = 0; i < discard.length; i++) {
            if (discard[i] > 0 && (!properties.discard0CardOrderMatters || properties.cardDict[i].realEnergyCost() != 0)) {
                if (!first) {
                    str.append(", ");
                }
                first = false;
                str.append(discard[i]).append(" ").append(properties.cardDict[i].cardName);
            }
        }
        if (properties.discard0CardOrderMatters) {
            for (int i = 0; i < discardArrLen; i++) {
                if (properties.cardDict[discardArr[i]].realEnergyCost() == 0) {
                    if (!first) {
                        str.append(", ");
                    }
                    first = false;
                    str.append(properties.cardDict[discardArr[i]].cardName);
                }
            }
        }
        str.append("]");
        if (exhaustArrLen > 0) {
            str.append(", exhaust=[");
            first = true;
            var exhaust = GameStateUtils.getCardArrCounts(exhaustArr, exhaustArrLen, properties.realCardsLen);
            for (int i = 0; i < exhaust.length; i++) {
                if (exhaust[i] > 0) {
                    if (!first) {
                        str.append(", ");
                    }
                    first = false;
                    str.append(exhaust[i]).append(" ").append(properties.cardDict[i].cardName);
                }
            }
            str.append("]");
        }
        if (chosenCardsArrLen > 0) {
            str.append(", chosen=[");
            first = true;
            for (int i = 0; i < chosenCardsArrLen; i++) {
                if (!first) {
                    str.append(", ");
                }
                first = false;
                str.append(properties.cardDict[chosenCardsArr[i]].cardName);
            }
            str.append("]");
        }
        if (nightmareCardsLen > 0) {
            var count = new byte[properties.cardDict.length];
            for (int i = 0; i < nightmareCardsLen; i++) {
                count[nightmareCards[i]]++;
            }
            first = true;
            str.append(", nightmare=[");
            for (int i = 0; i < nightmareCardsLen; i++) {
                str.append(!first ? ", " : "");
                str.append(count[nightmareCards[i]] == 1 ? "" : count[nightmareCards[i]] + " ").append(properties.cardDict[nightmareCards[i]].cardName);
                first = false;
            }
            str.append("]");
        }
        if (orbs != null) {
            str.append(", orbs=[");
            for (int i = orbs.length - 2; i >= 0 ; i -= 2) {
                str.append(i == orbs.length - 2 ? "" : ", ").append(OrbType.values()[orbs[i]].abbrev);
                if (orbs[i] == OrbType.DARK.ordinal()) {
                    str.append("(").append(orbs[i + 1]).append(")");
                }
            }
            str.append("]");
        }
        if (focus > 0) {
            str.append(", focus=").append(focus);
        }
        if (stance != Stance.NEUTRAL) {
            str.append(", stance=").append(stance.toString());
        }
        str.append(", energy=").append(energy);
        if (actionCtx != GameActionCtx.PLAY_CARD) {
            str.append(", ctx=").append(actionCtx);
            if (actionCtx == GameActionCtx.SELECT_ENEMY || actionCtx == GameActionCtx.SELECT_CARD_HAND ||
                    actionCtx == GameActionCtx.SELECT_CARD_EXHAUST || actionCtx == GameActionCtx.SELECT_CARD_DISCARD ||
                    actionCtx == GameActionCtx.SELECT_CARD_DECK || actionCtx == GameActionCtx.SCRYING) {
                if (currentAction.type() == GameActionType.PLAY_CARD) {
                    str.append("[").append(properties.cardDict[currentAction.idx()].cardName).append("]");
                } else if (currentAction.type() == GameActionType.USE_POTION) {
                    str.append("[").append(properties.potions.get(currentAction.idx())).append("]");
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
        for (int i = 0; i < properties.potions.size(); i++) {
            if (properties.potions.get(i) instanceof Potion.FairyInABottle && !potionUsed(i) && potionUsable(i)) {
                str.append(", ").append(properties.potions.get(i));
            }
        }
        if (properties.atLeastOneCounterHasNNHandler) {
            StringBuilder tmp = new StringBuilder();
            first = true;
            for (int i = 0; i < properties.counterInfos.length; i++) {
                if (properties.counterInfos[i].handler != null && getCounterForRead()[properties.counterInfos[i].idx] != 0) {
                    String s = properties.counterInfos[i].handler.getDisplayString(this);
                    tmp.append(first ? "" : ", ").append(properties.counterInfos[i].name).append('=').append(s != null ? s : getCounterForRead()[properties.counterInfos[i].idx]);
                    first = false;
                }
            }
            if (tmp.length() > 0) {
                str.append(", other=[").append(tmp).append("]");
            }
        }
        boolean showQComb = properties.potions.size() > 0 || (properties.extraTrainingTargets.size() > 0 && isTerminal() > 0);
        str.append(", v=(");
        if (showQComb) {
            str.append(formatFloat(calcQValue())).append("/");
        }
        str.append(formatFloat(v_win)).append("/").append(formatFloat(v_health)).append(",").append(formatFloat(v_health * getPlayeForRead().getMaxHealth()));
        if (v_extra != null) {
            var vArray = new VArray(properties.v_total_len);
            getVArray(vArray);
            int idx = 0;
            for (var target : properties.extraTrainingTargets) {
                int n = target.getNumberOfTargets();
                if (n == 1) {
                    if (v_extra[idx] == vArray.getVExtra(idx)) {
                        str.append("/").append(formatFloat(v_extra[idx]));
                    } else {
                        str.append("/").append(formatFloat(v_extra[idx])).append("->").append(formatFloat(vArray.getVExtra(idx)));
                    }
                } else {
                    str.append("/[");
                    for (int i = 0; i < n; i++) {
                        if (i > 0) {
                            str.append(',');
                        }
                        str.append(formatFloat(v_extra[idx + i]));
                    }
                    str.append(']');
                }
                idx += n;
            }
        }
        str.append(")");
//        str.append(", var=").append(formatFloat(varianceS / total_n));
        if (n != null) {
            str.append(", q/p/n=[");
            first = true;
            for (int i = 0; i < getLegalActions().length; i++) {
                var p_str = policy != null ? formatFloat(policy[i]) : "0";
                var p_str2 = policyMod != null && policyMod != policy ? formatFloat(policyMod[i]) : null;
                var q_win_str = formatFloat(n[i] == 0 ? 0 : getChildQ(i, GameState.V_WIN_IDX) / n[i]);
                var q_health_str = formatFloat(n[i] == 0 ? 0 : getChildQ(i, GameState.V_HEALTH_IDX) / n[i]);
                var q_str = formatFloat(n[i] == 0 ? 0 : getChildQ(i, GameState.V_COMB_IDX) / n[i]);
                if (!first) {
                    str.append(", ");
                }
                first = false;
                str.append(q_str).append('/').append(q_win_str).append('/').append(q_health_str).append('/');
                if (Configuration.USE_FIGHT_PROGRESS_WHEN_LOSING) {
                    var q_progress_str = properties.fightProgressVExtraIdx >= 0 ? formatFloat(n[i] == 0 ? 0 : getChildQ(i, GameState.V_EXTRA_IDX_START + properties.fightProgressVExtraIdx) / n[i]) : null;
                    str.append(q_progress_str).append('/');
                }
                if (Configuration.USE_TURNS_LEFT_HEAD) {
                    var q_progress_str = properties.turnsLeftVExtraIdx >= 0 ? formatFloat(n[i] == 0 ? 0 : getChildQ(i, GameState.V_EXTRA_IDX_START + properties.turnsLeftVExtraIdx) / n[i]) : null;
                    str.append(q_progress_str).append('/');
                }
                if (p_str2 != null) {
                    str.append(p_str2).append('/');
                }
                str.append(p_str).append('/').append(n[i]);
                if (bannedActions != null && bannedActions[i]) {
                    str.append("/banned");
                }
                str.append(" (").append(getActionString(i)).append(")");
            }
            str.append(']');
        } else if (policy != null) {
            str.append(", p=[");
            first = true;
            for (int i = 0; i < getLegalActions().length; i++) {
                var p_str = formatFloat(policy[i]);
                if (!first) {
                    str.append(", ");
                }
                first = false;
                str.append(p_str);
                str.append(" (").append(getActionString(i)).append(")");
            }
            str.append(']');
        }
        str.append('}');
        return str.toString();
    }

    public int[] getLegalActions() {
        if (legalActions == null) {
            if (actionCtx == GameActionCtx.PLAY_CARD) {
                var legal = new boolean[properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length];
                legal[legal.length - 1] = true; // End Turn
                int count = 1;

                if ((buffs & PlayerBuff.END_TURN_IMMEDIATELY.mask()) != 0) {
                    // Only End Turn is legal when this buff is active
                    legalActions = new int[1];
                    legalActions[0] = legal.length - 1; // End Turn action index
                    return legalActions;
                }

                if (!(properties.timeEaterCounterIdx >= 0 && counter[properties.timeEaterCounterIdx] >= 12)) {
                    for (int i = 0; i < properties.potions.size(); i++) {
                        if (properties.potions.get(i).isGenerated && properties.potions.get(i).generatedIdx > 0) {
                            continue;
                        }
                        if (potionUsable(i) && properties.potions.get(i) instanceof Potion.SmokeBomb) {
                            if (!Relic.isBossFight(this) && !GameStateUtils.isSurrounded(this)) {
                                legal[properties.cardDict.length + i] = true;
                                count++;
                            }
                        } else if (potionUsable(i) && !(properties.potions.get(i) instanceof Potion.FairyInABottle)) {
                            legal[properties.cardDict.length + i] = true;
                            count++;
                        }
                    }
                }
                if (!(properties.timeEaterCounterIdx >= 0 && counter[properties.timeEaterCounterIdx] >= 12) &&
                    !(properties.normalityCounterIdx >= 0 && counter[properties.normalityCounterIdx] >= 3 && GameStateUtils.getCardCount(getHandArrForRead(), handArrLen, properties.normalityCardIdx) > 0) &&
                    !(properties.velvetChokerCounterIndexIdx >= 0 && counter[properties.velvetChokerCounterIndexIdx] >= 6)) {
                    for (int i = 0; i < handArrLen; i++) {
                        if (!legal[handArr[i]]) {
                            if (getPlayeForRead().isEntangled() && properties.cardDict[handArr[i]].cardType == Card.ATTACK) {
                                continue;
                            }
                            int cost = getCardEnergyCost(handArr[i]);
                            if (cost < 0 || cost > energy) {
                                continue;
                            }
                            if (properties.cardDict[handArr[i]].selectEnemy) {
                                var atLeastOneTarget = false;
                                for (int j = 0; j < enemies.size(); j++) {
                                    if (enemies.get(j).isTargetable()) {
                                        atLeastOneTarget = true;
                                    }
                                }
                                if (!atLeastOneTarget) {
                                    continue;
                                }
                            }
                            if (properties.biasedCognitionLimitCounterIdx >= 0) {
                                if (properties.character == CharacterEnum.DEFECT) {
                                    if (getPlayeForRead().getArtifact() == 0 && properties.cardDict[handArr[i]].cardName.startsWith("Biased") && calcFightProgress(true) < getCounterForRead()[properties.biasedCognitionLimitCounterIdx] / 100.0) {
                                        continue;
                                    }
                                } else if (properties.character == CharacterEnum.SILENT) {
                                    if (getPlayeForRead().getArtifact() == 0 && properties.cardDict[handArr[i]].cardName.startsWith("Wraith Form") && calcFightProgress(true) < getCounterForRead()[properties.biasedCognitionLimitCounterIdx] / 100.0) {
                                        continue;
                                    }
                                }
                            }
//                             how to stall genetic algo for echo form more effectively
//                            if (properties.echoFormCounterIdx >= 0 && properties.cardDict[handArr[i]].cardName.startsWith("Genetic Algorithm+")) {
//                                if (getCounterForRead()[properties.echoFormCounterIdx] == 0) {
//                                    continue;
//                                }
//                                int limit = getCounterForRead()[properties.echoFormCounterIdx] >> 16;
//                                if (limit == 0) {
//                                    continue;
//                                }
//                                int cardsPlayThisTurn = (getCounterForRead()[properties.echoFormCounterIdx] & ((1 << 16) - 1));
//                                if (cardsPlayThisTurn >= limit) {
//                                    continue;
//                                }
//                            }
                            count++;
                            legal[handArr[i]] = true;
                        }
                    }
                }
                legalActions = new int[count];
                int j = 0;
                for (int i = 0; i < legal.length; i++) {
                    if (legal[i]) {
                        legalActions[j++] = i;
                    }
                }
            } else if (actionCtx == GameActionCtx.SELECT_CARD_HAND) {
                if (currentAction.type() == GameActionType.PLAY_CARD) {
                    var canEndSelect = currentAction.idx() == properties.wellLaidPlansCardIdx;
                    canEndSelect |= currentAction.idx() == properties.gamblingChipsCardIdx;
                    canEndSelect |= properties.cardDict[currentAction.idx()].getBaseCard() instanceof CardColorless.Purity;
                    if (canEndSelect) {
                        getLegalActionsSelectCardFromArr(handArr, handArrLen, properties.cardDict[currentAction.idx()], properties.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()].length - 1);
                    } else {
                        getLegalActionsSelectCardFromArr(handArr, handArrLen, properties.cardDict[currentAction.idx()], -1);
                    }
                } else if (currentAction.type() == GameActionType.USE_POTION) {
                    getLegalActionsSelectCardFromArr(handArr, handArrLen, null, properties.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()].length - 1);
                }
            } else if (actionCtx == GameActionCtx.SELECT_CARD_DISCARD) {
                if (currentAction.type() == GameActionType.PLAY_CARD) {
                    getLegalActionsSelectCardFromArr(discardArr, discardArrLen, properties.cardDict[currentAction.idx()], -1);
                } else {
                    getLegalActionsSelectCardFromArr(discardArr, discardArrLen, null, -1);
                }
            } else if (actionCtx == GameActionCtx.SELECT_CARD_DECK) {
                getLegalActionsSelectCardFromArr(deckArr, deckArrLen, properties.cardDict[currentAction.idx()], -1);
            } else if (actionCtx == GameActionCtx.SELECT_CARD_EXHAUST) {
                getLegalActionsSelectCardFromArr(exhaustArr, exhaustArrLen, properties.cardDict[currentAction.idx()], -1);
            } else if (actionCtx == GameActionCtx.SCRYING) {
                legalActions = new int[2];
                legalActions[0] = deckArr[deckArrLen - 1 - scryCurrentCount];
                legalActions[1] = properties.actionsByCtx[GameActionCtx.SCRYING.ordinal()].length - 1;
            } else {
                int count = 0;
                for (int i = 0; i < properties.maxNumOfActions; i++) {
                    if (isActionLegalInternal(i)) {
                        count += 1;
                    }
                }
                legalActions = new int[count];
                int idx = 0;
                for (int i = 0; i < properties.maxNumOfActions; i++) {
                    if (isActionLegalInternal(i)) {
                        legalActions[idx++] = i;
                    }
                }
            }
        }
        return legalActions;
    }

    private void getLegalActionsSelectCardFromArr(short[] cardArr, int cardArrLen, Card card, int extraAction) {
        int count = 0;
        var seen = new boolean[properties.cardDict.length];
        for (int i = 0; i < cardArrLen; i++) {
            if (!seen[cardArr[i]] && (card == null || card.canSelectCard(properties.cardDict[cardArr[i]]))) {
                count++;
                seen[cardArr[i]] = true;
            }
        }
        if (extraAction >= 0) {
            legalActions = new int[count + 1];
            legalActions[legalActions.length - 1] = extraAction;
        } else {
            legalActions = new int[count];
        }
        int j = 0;
        for (int i = 0; i < cardArrLen; i++) {
            if (seen[cardArr[i]]) {
                legalActions[j++] = cardArr[i];
                seen[cardArr[i]] = false;
            }
        }
        Arrays.sort(legalActions);
    }

    public void doEval(Model model) {
        getLegalActions();
        NNOutput output = model.eval(this);
        policy = output.policy();
        v_health = output.v_health();
        v_win = output.v_win();
        v_extra = output.v_other();
    }

    private static boolean needChosenCardsInInput() {
        return true;
    }

    private int getNNInputLen() {
        int inputLen = 0;
        if (Configuration.ADD_CURRENT_TURN_NUM_TO_NN_INPUT) {
            inputLen++;
            if (properties.isHeartGauntlet) {
                inputLen++;
            }
        }
        if (properties.preBattleScenariosBackup != null) {
            inputLen += properties.preBattleScenariosBackup.listRandomizations().size();
        }
        inputLen += properties.startOfBattleActions.size();
        inputLen += properties.realCardsLen;
        inputLen += properties.cardDict.length;
        if (Configuration.CARD_IN_HAND_IN_NN_INPUT) {
            inputLen += 1;
        }
        if (Configuration.CARD_IN_DECK_IN_NN_INPUT) {
            inputLen += 1;
        }
        if (properties.cardInDiscardInNNInput) {
            inputLen += 1;
        }
        inputLen += properties.discardIdxes.length;
        if (properties.selectFromExhaust) {
            inputLen += properties.realCardsLen;
        }
        if (needChosenCardsInInput() && chosenCardsArr != null) {
            inputLen += properties.cardDict.length;
        }
        if (nightmareCards != null) {
            inputLen += properties.realCardsLen;
        }
        if (MAX_AGENT_DECK_ORDER_MEMORY > 0 && properties.needDeckOrderMemory) {
            inputLen += properties.realCardsLen * MAX_AGENT_DECK_ORDER_MEMORY * MAX_AGENT_DECK_ORDER_MEMORY_DUPLICATE_CARDS;
        }
        if (false && properties.discard0CardOrderMatters) {
            inputLen += properties.discardOrder0CostNumber * properties.discardOrder0CardMaxCopies;
            inputLen += properties.discardOrder0CostNumber * properties.discardOrder0CardMaxCopies * properties.discardOrderMaxKeepTrackIn10s;
        }
        for (int i = 3; i < properties.actionsByCtx.length; i++) {
            if (properties.actionsByCtx[i] != null && (Configuration.ADD_BEGIN_TURN_CTX_TO_NN_INPUT || i != GameActionCtx.BEGIN_TURN.ordinal())) {
                inputLen += 1;
            }
        }
        inputLen += 1; // energy
        inputLen += 1; // player health
        inputLen += 1; // player block
        inputLen += properties.maxNumOfOrbs * 5;
        if (properties.playerFocusCanChange) {
            inputLen += 1; // focus
        }
        if (properties.character == CharacterEnum.WATCHER) {
            inputLen += 4; // Watcher stances (one-hot encoding: neutral, wrath, calm, divinity)
        }
        if (properties.previousCardPlayTracking) {
            inputLen += 3; // Last played card type (one-hot encoding: none, attack, skill)
        }
        if (properties.playerArtifactCanChange) {
            inputLen += 1; // player artifact
        }
        if (properties.playerStrengthCanChange) {
            inputLen += 1; // player strength
        }
        if (properties.playerDexterityCanChange) {
            inputLen += 1; // player dexterity
        }
        if (properties.playerStrengthEotCanChange) {
            inputLen += 1; // player lose strength eot
        }
        if (properties.playerDexterityEotCanChange) {
            inputLen += 1; // player lose dexterity eot
        }
        if (properties.playerPlatedArmorCanChange) {
            inputLen += 1; // player plated armor
        }
        if (properties.playerCanGetVuln) {
            inputLen += 1; // player vulnerable
        }
        if (properties.playerCanGetWeakened) {
            inputLen += 1; // player weak
        }
        if (properties.playerCanGetFrailed) {
            inputLen += 1; // player weak
        }
        if (properties.playerCanGetEntangled) {
            inputLen += 1; // player entangled
        }
        if (properties.battleTranceExist) {
            inputLen += 1; // battle trance
        }
        if (properties.energyRefillCanChange) {
            inputLen += 1; // berserk
        }
        for (PlayerBuff buff : PlayerBuff.BUFFS) {
            if ((properties.possibleBuffs & buff.mask()) != 0) {
                inputLen += 1; // barricade in deck
            }
        }
        for (var counterInfo : properties.counterInfos) {
            inputLen += counterInfo.handler == null ? 0 : counterInfo.handler.getInputLenDelta();
        }
        for (var handler : properties.nnInputHandlers) {
            inputLen += handler.getInputLenDelta();
        }
        inputLen += properties.potions.size() * 3;
        // cards currently selecting enemies
        if (properties.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null && enemies.size() > 1) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectEnemy && action.idx() < properties.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && properties.potions.get(action.idx()).selectEnemy) {
                    inputLen += 1;
                }
            }
        }
        if (properties.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectFromHand && action.idx() < properties.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && properties.potions.get(action.idx()).selectFromHand) {
                    inputLen += 1;
                }
            }
        }
        if (properties.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectFromDiscard && action.idx() < properties.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && properties.potions.get(action.idx()).selectFromDiscard) {
                    inputLen += 1;
                }
            }
        }
        if (properties.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectFromExhaust && action.idx() < properties.realCardsLen) {
                    inputLen += 1;
                }
            }
        }
        if (properties.actionsByCtx[GameActionCtx.SELECT_CARD_DECK.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectFromDeck && action.idx() < properties.realCardsLen) {
                    inputLen += 1;
                }
            }
        }
        if (properties.actionsByCtx[GameActionCtx.SCRYING.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].scry && action.idx() < properties.realCardsLen) {
                    inputLen += 1;
                }
            }
        }
        inputLen += properties.select1OutOf3CardsIdxes.length;
        for (var enemy : enemies) {
            inputLen += 1; // enemy health
            if (properties.enemyCanGetVuln) {
                inputLen += 1; // enemy vulnerable
            }
            if (properties.enemyCanGetWeakened) {
                inputLen += 1; // enemy weak
            }
            if (properties.enemyCanGetChoked) {
                inputLen += 1; // enemy choke
            }
            if (properties.enemyCanGetLockOn) {
                inputLen += 1; // enemy lockOn
            }
            if (properties.enemyCanGetTalkToTheHand) {
                inputLen += 1; // enemy talkToTheHand
            }
            if (properties.enemyCanGetMark) {
                inputLen += 1; // enemy mark
            }
            if (properties.enemyCanGetPoisoned) {
                inputLen += 1; // enemy poison
            }
            if (properties.enemyCanGetCorpseExplosion) {
                inputLen += 1; // enemy corpse explosion
            }
            if (properties.enemyStrengthEotCanChange) {
                inputLen += 1; // enemy gain strength eot
            }
            if (enemy.properties.canGainBlock) {
                inputLen += 1; // enemy block
            }
            if (enemy.properties.canGainStrength || properties.enemyStrengthCanChange) {
                inputLen += 1; // enemy strength
            }
            if (enemy.properties.hasArtifact) {
                inputLen += 1; // enemy artifact
            }
            if (enemy.properties.canGainRegeneration) {
                inputLen += 1; // enemy regeneration
            }
            if (enemy.properties.canGainMetallicize) {
                inputLen += 1; // enemy metallicize
            }
            if (enemy.properties.canGainPlatedArmor) {
                inputLen += 1; // enemy plated armor
            }
            if (enemy.properties.canGainRegeneration || enemy.properties.canHeal || properties.enemyCanGetCorpseExplosion) {
                inputLen += 1; // enemy max health since heal can't go over max health
            }
            if (enemy instanceof Enemy.MergedEnemy m) {
                inputLen += m.possibleEnemies.size();
                inputLen += enemy.properties.numOfMoves; // enemy moves
            } else {
                inputLen += enemy.properties.numOfMoves; // enemy moves
                if (enemy.properties.useLast2MovesForMoveSelection) {
                    inputLen += enemy.properties.numOfMoves;
                }
            }
            inputLen += enemy.getNNInputLen(properties);
            if (enemy instanceof EnemyExordium.RedLouse || enemy instanceof EnemyExordium.GreenLouse) {
                inputLen += 1;
            }
        }
        return inputLen;
    }

    public String getNNInputDesc() {
        var str = "Possible Cards:\n";
        for (Card card : properties.cardDict) {
            str += "    " + card.cardName + "\n";
        }
        str += "Cards That Can Change In Number:\n";
        for (int discardIdx : properties.discardIdxes) {
            str += "    " + properties.cardDict[discardIdx].cardName + "\n";
        }
        str += "Neural Network Input Breakdown (" + properties.inputLen + " inputs):\n";
        if (Configuration.ADD_CURRENT_TURN_NUM_TO_NN_INPUT) {
            str += "    1 input to keep track of current turn number\n";
            if (properties.isHeartGauntlet) {
                str += "    1 input to keep track of real current turn number\n";
            }
        }
        if (properties.preBattleScenariosBackup != null) {
            str += "    " + properties.preBattleScenariosBackup.listRandomizations().size() + " inputs to keep track of scenario chosen\n";
        }
        str += "    " + properties.realCardsLen + " inputs for cards in deck\n";
        str += "    " + properties.cardDict.length + " inputs for cards in hand\n";
        if (Configuration.CARD_IN_HAND_IN_NN_INPUT) {
            str += "    1 input for number of cards in hand\n";
        }
        if (Configuration.CARD_IN_DECK_IN_NN_INPUT) {
            str += "    1 input for number of cards in deck\n";
        }
        if (properties.cardInDiscardInNNInput) {
            str += "    1 input for number of cards in discard\n";
        }
        str += "    " + properties.discardIdxes.length + " inputs to keep track of cards in discard\n";
        if (properties.selectFromExhaust) {
            str += "    " + properties.realCardsLen + " inputs for cards in exhaust\n";
        }
        if (needChosenCardsInInput() && chosenCardsArr != null) {
            str += "    " + properties.realCardsLen + " inputs for cards that was chosen by Well Laid Plans\n";
        }
        if (nightmareCards != null) {
            str += "    " + properties.realCardsLen + " inputs for cards that was targeted by nightmare\n";
        }
        if (MAX_AGENT_DECK_ORDER_MEMORY > 0 && properties.needDeckOrderMemory) {
            str += "    " + properties.realCardsLen * MAX_AGENT_DECK_ORDER_MEMORY * MAX_AGENT_DECK_ORDER_MEMORY_DUPLICATE_CARDS + " inputs to keep track of known card draw order\n";
        }
        if (false && properties.discard0CardOrderMatters) {
            str += "    " + properties.discardOrder0CostNumber * properties.discardOrder0CardMaxCopies + " inputs to keep track of discard 0 cost cards order in hand\n";
            str += "    " + properties.discardOrder0CostNumber * properties.discardOrder0CardMaxCopies * properties.discardOrderMaxKeepTrackIn10s + " (" +
                    properties.discardOrder0CostNumber + " * " + properties.discardOrder0CardMaxCopies + " * " + properties.discardOrderMaxKeepTrackIn10s + ") inputs to keep track of discard 0 cost cards order in discard\n";
        }
        for (int i = 3; i < properties.actionsByCtx.length; i++) {
            if (properties.actionsByCtx[i] != null && (Configuration.ADD_BEGIN_TURN_CTX_TO_NN_INPUT || i != GameActionCtx.BEGIN_TURN.ordinal())) {
                str += "    1 input to keep track of ctx " + GameActionCtx.values()[i] + "\n";
            }
        }
        str += "    1 input to keep track of energy\n";
        str += "    1 input to keep track of player health\n";
        str += "    1 input to keep track of player block\n";
        if (properties.maxNumOfOrbs > 0) {
            str += "    " + properties.maxNumOfOrbs + "*5 inputs to keep track of player orb slots\n";
        }
        if (properties.playerFocusCanChange) {
            str += "    1 input to keep track of player focus\n";
        }
        if (properties.character == CharacterEnum.WATCHER) {
            str += "    4 inputs to keep track of Watcher stance (one-hot: neutral, wrath, calm, divinity)\n";
        }
        if (properties.previousCardPlayTracking) {
            str += "    3 inputs to keep track of last played card type (one-hot: none, attack, skill)\n";
        }
        if (properties.playerArtifactCanChange) {
            str += "    1 input to keep track of player artifact\n";
        }
        if (properties.playerStrengthCanChange) {
            str += "    1 input to keep track of player strength\n";
        }
        if (properties.playerDexterityCanChange) {
            str += "    1 input to keep track of player dexterity\n";
        }
        if (properties.playerStrengthEotCanChange) {
            str += "    1 input to keep track of player lose strength eot debuff\n";
        }
        if (properties.playerDexterityEotCanChange) {
            str += "    1 input to keep track of player lose dexterity eot debuff\n";
        }
        if (properties.playerPlatedArmorCanChange) {
            str += "    1 input to keep track of player plated armor\n";
        }
        if (properties.playerCanGetVuln) {
            str += "    1 input to keep track of player vulnerable\n";
        }
        if (properties.playerCanGetWeakened) {
            str += "    1 input to keep track of player weak\n";
        }
        if (properties.playerCanGetFrailed) {
            str += "    1 input to keep track of player frail\n";
        }
        if (properties.playerCanGetEntangled) {
            str += "    1 input to keep track of whether player is entangled or not\n";
        }
        if (properties.battleTranceExist) {
            str += "    1 input to keep track of battle trance cannot draw card debuff\n";
        }
        if (properties.energyRefillCanChange) {
            str += "    1 input to keep track of berserk\n";
        }
        for (PlayerBuff buff : PlayerBuff.BUFFS) {
            if ((properties.possibleBuffs & buff.mask()) != 0) {
                str += "    1 input to keep track of buff " + buff.name() + "\n";
            }
        }
        for (int i = 0; i < properties.counterInfos.length; i++) {
            if (properties.counterInfos[i].handler != null) {
                str += "    " + properties.counterInfos[i].handler.getInputLenDelta() + " input to keep track of counter for " + properties.counterInfos[i].name + "\n";
            }
        }
        for (int i = 0; i < properties.nnInputHandlersName.length; i++) {
            str += "    " + properties.nnInputHandlers[i].getInputLenDelta() + " input to keep track of " + properties.nnInputHandlersName[i] + "\n";
        }
        for (int i = 0; i < properties.potions.size(); i++) {
            str += "    3 inputs to keep track of " + properties.potions.get(i) + " usage\n";
        }
        if (properties.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null && enemies.size() > 1) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectEnemy && action.idx() < properties.realCardsLen) {
                    str += "    1 input to keep track of currently played card " + properties.cardDict[action.idx()].cardName + " for selecting enemy\n";
                } else if (action.type() == GameActionType.USE_POTION && properties.potions.get(action.idx()).selectEnemy) {
                    str += "    1 input to keep track of currently used potion " + properties.potions.get(action.idx()) + " for selecting enemy\n";
                }
            }
        }
        if (properties.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectFromHand && action.idx() < properties.realCardsLen) {
                    str += "    1 input to keep track of currently played card " + properties.cardDict[action.idx()].cardName + " for selecting card from hand\n";
                } else if (action.type() == GameActionType.USE_POTION && properties.potions.get(action.idx()).selectFromHand) {
                    str += "    1 input to keep track of currently used potion " + properties.potions.get(action.idx()) + " for selecting card from hand\n";
                }
            }
        }
        if (properties.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectFromDiscard && action.idx() < properties.realCardsLen) {
                    str += "    1 input to keep track of currently played card " + properties.cardDict[action.idx()].cardName + " for selecting card from discard\n";
                } else if (action.type() == GameActionType.USE_POTION && properties.potions.get(action.idx()).selectFromDiscard) {
                    str += "    1 input to keep track of currently used potion " + properties.potions.get(action.idx()) + " for selecting card from discard\n";
                }
            }
        }
        if (properties.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectFromExhaust && action.idx() < properties.realCardsLen) {
                    str += "    1 input to keep track of currently played card " + properties.cardDict[action.idx()].cardName + " for selecting card from exhaust\n";
                }
            }
        }
        if (properties.actionsByCtx[GameActionCtx.SELECT_CARD_DECK.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectFromDeck && action.idx() < properties.realCardsLen) {
                    str += "    1 input to keep track of currently played card " + properties.cardDict[action.idx()].cardName + " for selecting card from deck\n";
                }
            }
        }
        if (properties.actionsByCtx[GameActionCtx.SCRYING.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].scry && action.idx() < properties.realCardsLen) {
                    str += "    1 input to keep track of currently played card " + properties.cardDict[action.idx()].cardName + " for scrying\n";
                }
            }
        }
        if (properties.select1OutOf3CardsIdxes.length > 0) {
            str += "    " + properties.select1OutOf3CardsIdxes.length + " inputs to keep track of selecting cards from 1 out of 3 cards\n";
        }
        for (var enemy : enemies) {
            if (enemy instanceof Enemy.MergedEnemy m) {
                str += "    *** " + m.getDescName() + " ***\n";
            } else {
                str += "    *** " + enemy.getName() + " ***\n";
            }
            str += "        1 input to keep track of health\n";
            if (properties.enemyCanGetVuln) {
                str += "        1 input to keep track of vulnerable\n";
            }
            if (properties.enemyCanGetWeakened) {
                str += "        1 input to keep track of weak\n";
            }
            if (properties.enemyCanGetChoked) {
                str += "        1 input to keep track of choke\n";
            }
            if (properties.enemyCanGetLockOn) {
                str += "        1 input to keep track of lockOn\n";
            }
            if (properties.enemyCanGetTalkToTheHand) {
                str += "        1 input to keep track of talkToTheHand\n";
            }
            if (properties.enemyCanGetMark) {
                str += "        1 input to keep track of mark\n";
            }
            if (properties.enemyCanGetPoisoned) {
                str += "        1 input to keep track of poison\n";
            }
            if (properties.enemyCanGetCorpseExplosion) {
                str += "        1 input to keep track of corpse explosion\n";
            }
            if (properties.enemyStrengthEotCanChange) {
                str += "        1 input to keep track of enemy gain strength eot\n";
            }
            if (enemy.properties.canGainBlock) {
                str += "        1 input to keep track of block\n";
            }
            if (enemy.properties.canGainStrength || properties.enemyStrengthCanChange) {
                str += "        1 input to keep track of strength\n";
            }
            if (enemy.properties.hasArtifact) {
                str += "        1 input to keep track of artifact\n";
            }
            if (enemy.properties.canGainRegeneration) {
                str += "        1 input to keep track of regeneration\n";
            }
            if (enemy.properties.canGainMetallicize) {
                str += "        1 input to keep track of metallicize\n";
            }
            if (enemy.properties.canGainPlatedArmor) {
                str += "        1 input to keep track of plated armor\n";
            }
            if (enemy.properties.canGainRegeneration || enemy.properties.canHeal || properties.enemyCanGetCorpseExplosion) {
                str += "        1 input to keep track of enemy max health\n";
            }
            if (enemy instanceof Enemy.MergedEnemy m) {
                str += "        " + m.possibleEnemies.size() + " input to keep track of current enemy\n";
                for (int i = 0; i < m.possibleEnemies.size(); i++) {
                    str += "        %s inputs to keep track of current move from %s\n".formatted(m.possibleEnemies.get(i).properties.numOfMoves,
                            m.possibleEnemies.get(i).getName());
                    if (m.possibleEnemies.get(i).properties.useLast2MovesForMoveSelection) {
                        str += "        %d inputs to keep track of last move from %s\n".formatted(m.possibleEnemies.get(i).properties.numOfMoves,
                                m.possibleEnemies.get(i).getName());
                    }
                }
            } else {
                str += "        " + enemy.properties.numOfMoves + " inputs to keep track of current move from enemy\n";
                if (enemy.properties.useLast2MovesForMoveSelection) {
                    str += "        " + enemy.properties.numOfMoves + " inputs to keep track of last move from enemy\n";
                }
            }
            String desc = enemy.getNNInputDesc(properties);
            if (desc != null) {
                str += "        " + desc + "\n";
            }
            if (enemy instanceof EnemyExordium.RedLouse || enemy instanceof EnemyExordium.GreenLouse) {
                str += "        1 input to keep track of louse damage\n";
            }
        }
        return str;
    }

    public float[] getNNInput() {
        var player = getPlayeForRead();
        int idx = 0;
        var x = new float[properties.inputLen];
        if (Configuration.ADD_CURRENT_TURN_NUM_TO_NN_INPUT) {
            x[idx++] = turnNum / 50.0f;
            if (properties.isHeartGauntlet) {
                x[idx++] = realTurnNum / properties.maxPossibleRealTurnsLeft;
            }
        }
        if (properties.preBattleScenariosBackup != null) {
            if (preBattleScenariosChosenIdx >= 0) {
                x[idx + preBattleScenariosChosenIdx] = 0.5f;
            }
            idx += properties.preBattleScenariosBackup.listRandomizations().size();
        }
        if (startOfBattleActionIdx < properties.startOfBattleActions.size()) {
            x[idx + startOfBattleActionIdx] = 0.5f;
        }
        idx += properties.startOfBattleActions.size();
        for (int i = 0; i < deckArrLen; i++) {
            x[idx + deckArr[i]] += (float) 0.1;
        }
        idx += properties.realCardsLen;
        for (int i = 0; i < handArrLen; i++) {
            x[idx + handArr[i]] += (float) 0.1;
        }
        idx += properties.cardDict.length;
        if (Configuration.CARD_IN_HAND_IN_NN_INPUT) {
            x[idx++] = (handArrLen - 5) / 10.0f;
        }
        if (Configuration.CARD_IN_DECK_IN_NN_INPUT) {
            x[idx++] = getNumCardsInDeck() / 40.0f;
        }
        if (properties.cardInDiscardInNNInput) {
            x[idx++] = getNumCardsInDiscard() / 40.0f;
        }
        for (int i = 0; i < discardArrLen; i++) {
            x[idx + properties.discardReverseIdxes[discardArr[i]]] += (float) 0.1;
        }
        idx += properties.discardIdxes.length;
        if (properties.selectFromExhaust) {
            for (int i = 0; i < exhaustArrLen; i++) {
                x[idx + exhaustArr[i]] += (float) 0.1;
            }
            idx += properties.realCardsLen;
        }
        if (needChosenCardsInInput() && chosenCardsArr != null) {
            for (int i = 0; i < chosenCardsArrLen; i++) {
                x[idx + chosenCardsArr[i]] += (float) 0.1;
            }
            idx += properties.cardDict.length;
        }
        if (nightmareCards != null) {
            for (int i = 0; i < nightmareCardsLen; i++) {
                x[idx + nightmareCards[i]] += 0.1f;
            }
            idx += properties.realCardsLen;
        }
        if (MAX_AGENT_DECK_ORDER_MEMORY > 0 && properties.needDeckOrderMemory) {
            var tmpIdx = idx;
            for (int i = 0; i < Math.min(5 * MAX_AGENT_DECK_ORDER_MEMORY * MAX_AGENT_DECK_ORDER_MEMORY_DUPLICATE_CARDS, deckArrFixedDrawLen); i += 5) {
                for (int j = i; j < Math.min(i + 5, deckArrFixedDrawLen); j++) {
                    int cardIdx = deckArr[deckArrLen - 1 - j];
                    x[tmpIdx + cardIdx] += 0.1f;
//                    for (int k = 0; k < MAX_AGENT_DECK_ORDER_MEMORY_DUPLICATE_CARDS; k++) {
//                        if (x[tmpIdx + k * properties.realCardsLen + cardIdx] == 0) {
//                            x[tmpIdx + k * properties.realCardsLen + cardIdx] = (float) (1.0 - 0.2 * (j - i));
//                            break;
//                        }
//                    }
                }
//                tmpIdx += properties.realCardsLen * MAX_AGENT_DECK_ORDER_MEMORY_DUPLICATE_CARDS;
                tmpIdx += properties.realCardsLen;
            }
            idx += properties.realCardsLen * MAX_AGENT_DECK_ORDER_MEMORY * MAX_AGENT_DECK_ORDER_MEMORY_DUPLICATE_CARDS;
        }
        if (false && properties.discard0CardOrderMatters) {
            int k = 10;
            for (int i = 0; i < handArrLen; i++) {
                if (properties.cardDict[handArr[i]].realEnergyCost() == 0) {
                    int j = 0;
                    for (; j < properties.discardOrder0CardMaxCopies; j++) {
                        if (x[idx + properties.discardOrder0CostNumber * j + properties.discardOrder0CardReverseIdx[handArr[i]]] == 0) {
                            x[idx + properties.discardOrder0CostNumber * j + properties.discardOrder0CardReverseIdx[handArr[i]]] = k / 10.0f;
                            break;
                        }
                    }
                    if (j == properties.discardOrder0CardMaxCopies) {
                        throw new IllegalStateException("Too many 0 cost card copies of " + properties.cardDict[handArr[i]].cardName);
                    }
                    k--;
                }
            }
            idx += properties.discardOrder0CostNumber * properties.discardOrder0CardMaxCopies;
            k = 10;
            int nextIdx = idx + properties.discardOrder0CostNumber * properties.discardOrder0CardMaxCopies * properties.discardOrderMaxKeepTrackIn10s;
            for (int i = 0; i < discardArrLen; i++) {
                if (properties.cardDict[discardArr[i]].realEnergyCost() == 0) {
                    int j = 0;
                    for (; j < properties.discardOrder0CardMaxCopies; j++) {
                        if (x[idx + properties.discardOrder0CostNumber * j + properties.discardOrder0CardReverseIdx[discardArr[i]]] == 0) {
                            x[idx + properties.discardOrder0CostNumber * j + properties.discardOrder0CardReverseIdx[discardArr[i]]] = k / 10.0f;
                            break;
                        }
                    }
                    if (j == properties.discardOrder0CardMaxCopies) {
                        throw new IllegalStateException("Too many 0 cost card copies of " + properties.cardDict[discardArr[i]].cardName);
                    }
                    k--;
                }
                if (k == 0) {
                    idx += properties.discardOrder0CostNumber * properties.discardOrder0CardMaxCopies;
                    k = 10;
                }
            }
            idx = nextIdx;
        }
        for (int i = 3; i < properties.actionsByCtx.length; i++) {
            if (properties.actionsByCtx[i] != null && (Configuration.ADD_BEGIN_TURN_CTX_TO_NN_INPUT || i != GameActionCtx.BEGIN_TURN.ordinal())) {
                x[idx++] = actionCtx.ordinal() == i ? 0.5f : -0.5f;
            }
        }
        x[idx++] = energy / (float) 10;
        x[idx++] = player.getHealth() / (float) player.getMaxHealth();
        x[idx++] = player.getBlock() / (float) 40.0;
        if (orbs != null) {
            for (int i = 0; i < orbs.length; i += 2) {
                if (orbs[i] == OrbType.DARK.ordinal()) {
                    x[idx + orbs[i]] = orbs[i + 1] / 50.0f;
                } else if (orbs[i] > 0) {
                    x[idx + orbs[i]] = 0.5f;
                }
                idx += 5;
            }
        }
        for (int i = orbs == null ? 0 : orbs.length / 2; i < properties.maxNumOfOrbs; i++) {
            x[idx] = 0.5f;
            idx += 5;
        }
        if (properties.playerFocusCanChange) {
            x[idx++] = focus / 15.0f;
        }
        if (properties.character == CharacterEnum.WATCHER) {
            // One-hot encoding for Watcher stances
            x[idx++] = stance == Stance.NEUTRAL ? 1.0f : 0.0f;
            x[idx++] = stance == Stance.WRATH ? 1.0f : 0.0f;
            x[idx++] = stance == Stance.CALM ? 1.0f : 0.0f;
            x[idx++] = stance == Stance.DIVINITY ? 1.0f : 0.0f;
        }
        if (properties.previousCardPlayTracking) {
            // One-hot encoding for last played card type
            x[idx++] = (lastCardPlayedType != Card.ATTACK && lastCardPlayedType != Card.SKILL) ? 1.0f : 0.0f; // none
            x[idx++] = lastCardPlayedType == Card.ATTACK ? 1.0f : 0.0f; // attack
            x[idx++] = lastCardPlayedType == Card.SKILL ? 1.0f : 0.0f; // skill
        }
        if (properties.playerArtifactCanChange) {
            x[idx++] = player.getArtifact() / (float) 3.0;
        }
        if (properties.playerStrengthCanChange) {
            x[idx++] = player.getStrength() / (float) 30.0;
        }
        if (properties.playerDexterityCanChange) {
            x[idx++] = player.getDexterity() / (float) 10.0;
        }
        if (properties.playerStrengthEotCanChange) {
            x[idx++] = player.getLoseStrengthEot() / (float) 10.0;
        }
        if (properties.playerDexterityEotCanChange) {
            x[idx++] = player.getLoseDexterityEot() / (float) 10.0;
        }
        if (properties.playerPlatedArmorCanChange) {
            x[idx++] = player.getPlatedArmor() / (float) 10.0;
        }
        if (properties.playerCanGetVuln) {
            x[idx++] = player.getVulnerable() / (float) 10.0;
        }
        if (properties.playerCanGetWeakened) {
            x[idx++] = player.getWeak() / (float) 10.0;
        }
        if (properties.playerCanGetFrailed) {
            x[idx++] = player.getFrail() / (float) 10.0;
        }
        if (properties.playerCanGetEntangled) {
            x[idx++] = player.isEntangled() ? 0.5f : -0.5f;
        }
        if (properties.battleTranceExist) {
            x[idx++] = player.cannotDrawCard() ? 0.5f : -0.5f;
        }
        if (properties.energyRefillCanChange) {
            x[idx++] = (energyRefill - 5) / 2f;
        }
        for (PlayerBuff buff : PlayerBuff.BUFFS) {
            if ((properties.possibleBuffs & buff.mask()) != 0) {
                x[idx++] = (buffs & buff.mask()) != 0 ? 0.5f : -0.5f;
            }
        }
        for (var counterInfo : properties.counterInfos) {
            if (counterInfo.handler != null) {
                idx = counterInfo.handler.addToInput(this, x, idx);
            }
        }
        for (var handler : properties.nnInputHandlers) {
            idx = handler.addToInput(this, x, idx);
        }
        for (int i = 0; i < properties.potions.size(); i++) {
            x[idx++] = potionsState[i * 3] == 1 ? 0.5f : -0.5f;
            x[idx++] = potionsState[i * 3 + 1] / 100f;
            x[idx++] = potionsState[i * 3 + 2] == 1 ? 0.5f : -0.5f;
        }
        if (properties.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null && enemies.size() > 1) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectEnemy && action.idx() < properties.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && properties.potions.get(action.idx()).selectEnemy) {
                    x[idx++] = currentAction == action ? 0.6f : -0.6f;
                }
            }
        }
        if (properties.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectFromHand && action.idx() < properties.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && properties.potions.get(action.idx()).selectFromHand) {
                    x[idx++] = currentAction == action ? 0.6f : -0.6f;
                }
            }
        }
        if (properties.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectFromDiscard && action.idx() < properties.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && properties.potions.get(action.idx()).selectFromDiscard) {
                    x[idx++] = currentAction == action ? 0.6f : -0.6f;
                }
            }
        }
        if (properties.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectFromExhaust && action.idx() < properties.realCardsLen) {
                    x[idx++] = currentAction == action ? 0.6f : -0.6f;
                }
            }
        }
        if (properties.actionsByCtx[GameActionCtx.SELECT_CARD_DECK.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectFromDeck && action.idx() < properties.realCardsLen) {
                    x[idx++] = currentAction == action ? 0.6f : -0.6f;
                }
            }
        }
        if (properties.actionsByCtx[GameActionCtx.SCRYING.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].scry && action.idx() < properties.realCardsLen) {
                    x[idx++] = currentAction == action ? 0.6f : -0.6f;
                }
            }
        }
        if (actionCtx == GameActionCtx.SELECT_CARD_1_OUT_OF_3) {
            x[idx + (select1OutOf3CardsIdxes & 255)] = 1;
            x[idx + ((select1OutOf3CardsIdxes >> 8) & 255)] = 1;
            x[idx + ((select1OutOf3CardsIdxes >> 16) & 255)] = 1;
        }
        idx += properties.select1OutOf3CardsIdxes.length;
        var enemyOrder = getEnemyOrder();
        for (int enemyIdx = 0; enemyIdx < enemies.size(); enemyIdx++) {
            var enemy = enemies.get(enemyOrder != null ? enemyOrder[enemyIdx] : enemyIdx);
            if (enemy.isAlive()) {
                x[idx++] = enemy.getHealth() / (float) enemy.properties.maxHealth;
                if (properties.enemyCanGetVuln) {
                    x[idx++] = enemy.getVulnerable() / (float) 10.0;
                }
                if (properties.enemyCanGetWeakened) {
                    x[idx++] = enemy.getWeak() / (float) 10.0;
                }
                if (properties.enemyCanGetChoked) {
                    x[idx++] = enemy.getChoke() / (float) 10.0;
                }
                if (properties.enemyCanGetLockOn) {
                    x[idx++] = enemy.getLockOn() / (float) 10.0;
                }
                if (properties.enemyCanGetTalkToTheHand) {
                    x[idx++] = enemy.getTalkToTheHand() / (float) 10.0;
                }
                if (properties.enemyCanGetMark) {
                    x[idx++] = enemy.getMark() / (float) 10.0;
                }
                if (properties.enemyCanGetPoisoned) {
                    x[idx++] = enemy.getPoison() / (float) 30.0;
                }
                if (properties.enemyCanGetCorpseExplosion) {
                    x[idx++] = enemy.getCorpseExplosion() / 10.0f;
                }
                if (properties.enemyStrengthEotCanChange) {
                    x[idx++] = enemy.getLoseStrengthEot() / (float) 20.0;
                }
                if (enemy.properties.canGainStrength || properties.enemyStrengthCanChange) {
                    x[idx++] = enemy.getStrength() / (float) 20.0;
                }
                if (enemy.properties.canGainBlock) {
                    x[idx++] = enemy.getBlock() / (float) 20.0;
                }
                if (enemy.properties.hasArtifact) {
                    x[idx++] = enemy.getArtifact() / 3.0f;
                }
                if (enemy.properties.canGainRegeneration) {
                    x[idx++] = enemy.getRegeneration() / (float) 10.0;
                }
                if (enemy.properties.canGainMetallicize) {
                    x[idx++] = enemy.getMetallicize() / (float) 14.0;
                }
                if (enemy.properties.canGainPlatedArmor) {
                    x[idx++] = enemy.getPlatedArmor() / (float) 14.0;
                }
                if (enemy.properties.canGainRegeneration || enemy.properties.canHeal || properties.enemyCanGetCorpseExplosion) {
                    x[idx++] = enemy.properties.origHealth / (float) enemy.properties.maxHealth;
                }
                if (enemy instanceof Enemy.MergedEnemy m) {
                    x[idx + m.currentEnemyIdx] = 1.0f;
                    idx += m.possibleEnemies.size();
                    for (int pIdx = 0; pIdx < m.possibleEnemies.size(); pIdx++) {
                        if (pIdx == m.currentEnemyIdx) {
                            for (int i = 0; i < m.possibleEnemies.get(pIdx).properties.numOfMoves; i++) {
                                if (m.getMove() == i) {
                                    x[idx++] = 0.5f;
                                } else {
                                    x[idx++] = -0.5f;
                                }
                            }
                            if (m.possibleEnemies.get(pIdx).properties.useLast2MovesForMoveSelection) {
                                for (int i = 0; i < m.possibleEnemies.get(pIdx).properties.numOfMoves; i++) {
                                    if (m.getLastMove() == i) {
                                        x[idx++] = 0.5f;
                                    } else {
                                        x[idx++] = -0.5f;
                                    }
                                }
                            }
                        } else {
                            for (int i = 0; i < m.possibleEnemies.get(pIdx).properties.numOfMoves; i++) {
                                x[idx++] = -0.5f;
                            }
                            if (m.possibleEnemies.get(pIdx).properties.useLast2MovesForMoveSelection) {
                                for (int i = 0; i < m.possibleEnemies.get(pIdx).properties.numOfMoves; i++) {
                                    x[idx++] = -0.5f;
                                }
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < enemy.properties.numOfMoves; i++) {
                        if (enemy.getMove() == i) {
                            x[idx++] = 0.5f;
                        } else {
                            x[idx++] = -0.5f;
                        }
                    }
                    if (enemy.properties.useLast2MovesForMoveSelection) {
                        for (int i = 0; i < enemy.properties.numOfMoves; i++) {
                            if (enemy.getLastMove() == i) {
                                x[idx++] = 0.5f;
                            } else {
                                x[idx++] = -0.5f;
                            }
                        }
                    }
                }
                idx += enemy.writeNNInput(properties, x, idx);
                if (enemy instanceof EnemyExordium.RedLouse louse) {
                    x[idx++] = (louse.getCurlUpAmount() - 10) / 2.0f;
                } else if (enemy instanceof EnemyExordium.GreenLouse louse) {
                    x[idx++] = (louse.getCurlUpAmount() - 10) / 2.0f;
                }
            } else {
                x[idx++] = enemy.getHealth() / (float) enemy.properties.maxHealth;
                if (properties.enemyCanGetVuln) {
                    x[idx++] = -0.1f;
                }
                if (properties.enemyCanGetWeakened) {
                    x[idx++] = -0.1f;
                }
                if (properties.enemyCanGetChoked) {
                    x[idx++] = -0.1f;
                }
                if (properties.enemyCanGetLockOn) {
                    x[idx++] = -0.1f;
                }
                if (properties.enemyCanGetTalkToTheHand) {
                    x[idx++] = -0.1f;
                }
                if (properties.enemyCanGetMark) {
                    x[idx++] = -0.1f;
                }
                if (properties.enemyCanGetPoisoned) {
                    x[idx++] = -0.1f;
                }
                if (properties.enemyCanGetCorpseExplosion) {
                    x[idx++] = -0.1f;
                }
                if (properties.enemyStrengthEotCanChange) {
                    x[idx++] = -0.1f;
                }
                if (enemy.properties.canGainStrength || properties.enemyStrengthCanChange) {
                    x[idx++] = -0.1f;
                }
                if (enemy.properties.canGainBlock) {
                    x[idx++] = -0.1f;
                }
                if (enemy.properties.hasArtifact) {
                    x[idx++] = -0.1f;
                }
                if (enemy.properties.canGainRegeneration) {
                    x[idx++] = -0.1f;
                }
                if (enemy.properties.canGainMetallicize) {
                    x[idx++] = -0.1f;
                }
                if (enemy.properties.canGainPlatedArmor) {
                    x[idx++] = -0.1f;
                }
                if (enemy.properties.canGainRegeneration || enemy.properties.canHeal || properties.enemyCanGetCorpseExplosion) {
                    x[idx++] = -0.1f;
                }
                if (enemy instanceof Enemy.MergedEnemy m) {
                    idx += m.possibleEnemies.size();
                    for (int pIdx = 0; pIdx < m.possibleEnemies.size(); pIdx++) {
                        for (int i = 0; i < m.possibleEnemies.get(pIdx).properties.numOfMoves; i++) {
                            x[idx++] = -0.1f;
                        }
                        if (m.possibleEnemies.get(pIdx).properties.useLast2MovesForMoveSelection) {
                            for (int i = 0; i < m.possibleEnemies.get(pIdx).properties.numOfMoves; i++) {
                                x[idx++] = -0.1f;
                            }
                        }
                    }
                } else if (enemy.properties.canSelfRevive) {
                    for (int i = 0; i < enemy.properties.numOfMoves; i++) {
                        if (enemy.getMove() == i) {
                            x[idx++] = 0.5f;
                        } else {
                            x[idx++] = -0.5f;
                        }
                    }
                    if (enemy.properties.useLast2MovesForMoveSelection) {
                        for (int i = 0; i < enemy.properties.numOfMoves; i++) {
                            if (enemy.getLastMove() == i) {
                                x[idx++] = 0.5f;
                            } else {
                                x[idx++] = -0.5f;
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < enemy.properties.numOfMoves; i++) {
                        x[idx++] = -0.1f;
                    }
                    if (enemy.properties.useLast2MovesForMoveSelection) {
                        for (int i = 0; i < enemy.properties.numOfMoves; i++) {
                            x[idx++] = -0.1f;
                        }
                    }
                }
                for (int i = 0; i < enemy.getNNInputLen(properties); i++) {
                    x[idx++] = -0.1f;
                }
                if (enemy instanceof EnemyExordium.RedLouse || enemy instanceof EnemyExordium.GreenLouse) {
                    x[idx++] = -0.1f;
                }
            }
        }
        if (idx != properties.inputLen) {
            throw new IllegalStateException();
        }
        return x;
    }

    public void printNNInput(float[] input) {
        if (input.length != properties.inputLen) {
            throw new IllegalArgumentException("Input array length (" + input.length + ") does not match expected length (" + properties.inputLen + ")");
        }

        var player = getPlayeForRead();
        int idx = 0;
        System.out.println("Neural Network Input Analysis:");

        if (Configuration.ADD_CURRENT_TURN_NUM_TO_NN_INPUT) {
            System.out.println("Turn Number: " + input[idx++]);
            if (properties.isHeartGauntlet) {
                System.out.println("Real Turn Number: " + input[idx++]);
            }
        }

        if (properties.preBattleScenariosBackup != null) {
            System.out.print("Pre-battle Scenarios: [");
            for (int i = 0; i < properties.preBattleScenariosBackup.listRandomizations().size(); i++) {
                if (i > 0) System.out.print(", ");
                System.out.print(input[idx++]);
            }
            System.out.println("]");
        }

        if (properties.startOfBattleActions.size() > 0) {
            System.out.print("Start of Battle Actions: [");
            for (int i = 0; i < properties.startOfBattleActions.size(); i++) {
                if (i > 0) System.out.print(", ");
                System.out.print(input[idx++]);
            }
            System.out.println("]");
        }

        System.out.print("Cards in Deck: [");
        for (int i = 0; i < properties.realCardsLen; i++) {
            if (i > 0) System.out.print(", ");
            System.out.print(input[idx++]);
        }
        System.out.println("]");

        System.out.print("Cards in Hand: [");
        for (int i = 0; i < properties.cardDict.length; i++) {
            if (i > 0) System.out.print(", ");
            System.out.print(input[idx++]);
        }
        System.out.println("]");

        if (Configuration.CARD_IN_HAND_IN_NN_INPUT) {
            System.out.println("Hand Size: " + input[idx++]);
        }
        if (Configuration.CARD_IN_DECK_IN_NN_INPUT) {
            System.out.println("Deck Size: " + input[idx++]);
        }
        if (properties.cardInDiscardInNNInput) {
            System.out.println("Discard Size: " + input[idx++]);
        }

        System.out.print("Cards in Discard: [");
        for (int i = 0; i < properties.discardIdxes.length; i++) {
            if (i > 0) System.out.print(", ");
            System.out.print(input[idx++]);
        }
        System.out.println("]");

        if (properties.selectFromExhaust) {
            System.out.print("Cards in Exhaust: [");
            for (int i = 0; i < properties.realCardsLen; i++) {
                if (i > 0) System.out.print(", ");
                System.out.print(input[idx++]);
            }
            System.out.println("]");
        }

        if (needChosenCardsInInput() && chosenCardsArr != null) {
            System.out.print("Chosen Cards (Well Laid Plans): [");
            for (int i = 0; i < properties.cardDict.length; i++) {
                if (i > 0) System.out.print(", ");
                System.out.print(input[idx++]);
            }
            System.out.println("]");
        }

        if (nightmareCards != null) {
            System.out.print("Nightmare Cards: [");
            for (int i = 0; i < properties.realCardsLen; i++) {
                if (i > 0) System.out.print(", ");
                System.out.print(input[idx++]);
            }
            System.out.println("]");
        }

        if (MAX_AGENT_DECK_ORDER_MEMORY > 0 && properties.needDeckOrderMemory) {
            System.out.print("Deck Order Memory: [");
            int memorySize = properties.realCardsLen * MAX_AGENT_DECK_ORDER_MEMORY * MAX_AGENT_DECK_ORDER_MEMORY_DUPLICATE_CARDS;
            for (int i = 0; i < memorySize; i++) {
                if (i > 0) System.out.print(", ");
                System.out.print(input[idx++]);
            }
            System.out.println("]");
        }

        if (false && properties.discard0CardOrderMatters) {
            idx += properties.discardOrder0CostNumber * properties.discardOrder0CardMaxCopies;
            idx += properties.discardOrder0CostNumber * properties.discardOrder0CardMaxCopies * properties.discardOrderMaxKeepTrackIn10s;
        }

        for (int i = 3; i < properties.actionsByCtx.length; i++) {
            if (properties.actionsByCtx[i] != null && (Configuration.ADD_BEGIN_TURN_CTX_TO_NN_INPUT || i != GameActionCtx.BEGIN_TURN.ordinal())) {
                System.out.println("Action Context " + GameActionCtx.values()[i] + ": " + input[idx++]);
            }
        }

        System.out.println("Energy: " + input[idx++]);
        System.out.println("Player Health: " + input[idx++]);
        System.out.println("Player Block: " + input[idx++]);

        if (properties.maxNumOfOrbs > 0) {
            System.out.println("Orbs:");
            for (int i = 0; i < properties.maxNumOfOrbs; i++) {
                System.out.print("  Orb " + i + ": [");
                for (int j = 0; j < 5; j++) {
                    if (j > 0) System.out.print(", ");
                    System.out.print(input[idx++]);
                }
                System.out.println("]");
            }
        }

        if (properties.playerFocusCanChange) {
            System.out.println("Player Focus: " + input[idx++]);
        }

        if (properties.character == CharacterEnum.WATCHER) {
            System.out.println("Watcher Stance - Neutral: " + input[idx++]);
            System.out.println("Watcher Stance - Wrath: " + input[idx++]);
            System.out.println("Watcher Stance - Calm: " + input[idx++]);
            System.out.println("Watcher Stance - Divinity: " + input[idx++]);
        }

        if (properties.previousCardPlayTracking) {
            System.out.println("Previous Card Type - None: " + input[idx++]);
            System.out.println("Previous Card Type - Attack: " + input[idx++]);
            System.out.println("Previous Card Type - Skill: " + input[idx++]);
        }

        if (properties.playerArtifactCanChange) {
            System.out.println("Player Artifact: " + input[idx++]);
        }
        if (properties.playerStrengthCanChange) {
            System.out.println("Player Strength: " + input[idx++]);
        }
        if (properties.playerDexterityCanChange) {
            System.out.println("Player Dexterity: " + input[idx++]);
        }
        if (properties.playerStrengthEotCanChange) {
            System.out.println("Player Lose Strength EOT: " + input[idx++]);
        }
        if (properties.playerDexterityEotCanChange) {
            System.out.println("Player Lose Dexterity EOT: " + input[idx++]);
        }
        if (properties.playerPlatedArmorCanChange) {
            System.out.println("Player Plated Armor: " + input[idx++]);
        }
        if (properties.playerCanGetVuln) {
            System.out.println("Player Vulnerable: " + input[idx++]);
        }
        if (properties.playerCanGetWeakened) {
            System.out.println("Player Weak: " + input[idx++]);
        }
        if (properties.playerCanGetFrailed) {
            System.out.println("Player Frail: " + input[idx++]);
        }
        if (properties.playerCanGetEntangled) {
            System.out.println("Player Entangled: " + input[idx++]);
        }
        if (properties.battleTranceExist) {
            System.out.println("Battle Trance Cannot Draw: " + input[idx++]);
        }
        if (properties.energyRefillCanChange) {
            System.out.println("Energy Refill (Berserk): " + input[idx++]);
        }

        for (PlayerBuff buff : PlayerBuff.BUFFS) {
            if ((properties.possibleBuffs & buff.mask()) != 0) {
                System.out.println("Player Buff " + buff.name() + ": " + input[idx++]);
            }
        }

        for (var counterInfo : properties.counterInfos) {
            if (counterInfo.handler != null) {
                int deltaLen = counterInfo.handler.getInputLenDelta();
                System.out.print("Counter " + counterInfo.name + ": [");
                for (int i = 0; i < deltaLen; i++) {
                    if (i > 0) System.out.print(", ");
                    System.out.print(input[idx++]);
                }
                System.out.println("]");
            }
        }

        for (int i = 0; i < properties.nnInputHandlers.length; i++) {
            int deltaLen = properties.nnInputHandlers[i].getInputLenDelta();
            System.out.print("NN Input Handler " + properties.nnInputHandlersName[i] + ": [");
            for (int j = 0; j < deltaLen; j++) {
                if (j > 0) System.out.print(", ");
                System.out.print(input[idx++]);
            }
            System.out.println("]");
        }

        for (int i = 0; i < properties.potions.size(); i++) {
            System.out.println("Potion " + properties.potions.get(i) + " - Has: " + input[idx++]);
            System.out.println("Potion " + properties.potions.get(i) + " - Charges: " + input[idx++]);
            System.out.println("Potion " + properties.potions.get(i) + " - Can Use: " + input[idx++]);
        }

        if (properties.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null && enemies.size() > 1) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectEnemy && action.idx() < properties.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && properties.potions.get(action.idx()).selectEnemy) {
                    System.out.println("Select Enemy Action " + action + ": " + input[idx++]);
                }
            }
        }

        if (properties.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectFromHand && action.idx() < properties.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && properties.potions.get(action.idx()).selectFromHand) {
                    System.out.println("Select Card Hand Action " + action + ": " + input[idx++]);
                }
            }
        }

        if (properties.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectFromDiscard && action.idx() < properties.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && properties.potions.get(action.idx()).selectFromDiscard) {
                    System.out.println("Select Card Discard Action " + action + ": " + input[idx++]);
                }
            }
        }

        if (properties.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectFromExhaust && action.idx() < properties.realCardsLen) {
                    System.out.println("Select Card Exhaust Action " + action + ": " + input[idx++]);
                }
            }
        }

        if (properties.actionsByCtx[GameActionCtx.SELECT_CARD_DECK.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].selectFromDeck && action.idx() < properties.realCardsLen) {
                    System.out.println("Select Card Deck Action " + action + ": " + input[idx++]);
                }
            }
        }

        if (properties.actionsByCtx[GameActionCtx.SCRYING.ordinal()] != null) {
            for (GameAction action : properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && properties.cardDict[action.idx()].scry && action.idx() < properties.realCardsLen) {
                    System.out.println("Scrying Action " + action + ": " + input[idx++]);
                }
            }
        }

        if (properties.select1OutOf3CardsIdxes.length > 0) {
            System.out.print("Select 1 out of 3 Cards: [");
            for (int i = 0; i < properties.select1OutOf3CardsIdxes.length; i++) {
                if (i > 0) System.out.print(", ");
                System.out.print(input[idx++]);
            }
            System.out.println("]");
        }

        var enemyOrder = getEnemyOrder();
        for (int enemyIdx = 0; enemyIdx < enemies.size(); enemyIdx++) {
            var enemy = enemies.get(enemyOrder != null ? enemyOrder[enemyIdx] : enemyIdx);
            String enemyName = enemy instanceof Enemy.MergedEnemy m ? m.getDescName() : enemy.getName();
            System.out.println("*** " + enemyName + " ***");

            System.out.println("  Health: " + input[idx++]);
            if (properties.enemyCanGetVuln) {
                System.out.println("  Vulnerable: " + input[idx++]);
            }
            if (properties.enemyCanGetWeakened) {
                System.out.println("  Weak: " + input[idx++]);
            }
            if (properties.enemyCanGetChoked) {
                System.out.println("  Choke: " + input[idx++]);
            }
            if (properties.enemyCanGetLockOn) {
                System.out.println("  Lock-On: " + input[idx++]);
            }
            if (properties.enemyCanGetTalkToTheHand) {
                System.out.println("  Talk to the Hand: " + input[idx++]);
            }
            if (properties.enemyCanGetMark) {
                System.out.println("  Mark: " + input[idx++]);
            }
            if (properties.enemyCanGetPoisoned) {
                System.out.println("  Poison: " + input[idx++]);
            }
            if (properties.enemyCanGetCorpseExplosion) {
                System.out.println("  Corpse Explosion: " + input[idx++]);
            }
            if (properties.enemyStrengthEotCanChange) {
                System.out.println("  Lose Strength EOT: " + input[idx++]);
            }
            if (enemy.properties.canGainStrength || properties.enemyStrengthCanChange) {
                System.out.println("  Strength: " + input[idx++]);
            }
            if (enemy.properties.canGainBlock) {
                System.out.println("  Block: " + input[idx++]);
            }
            if (enemy.properties.hasArtifact) {
                System.out.println("  Artifact: " + input[idx++]);
            }
            if (enemy.properties.canGainRegeneration) {
                System.out.println("  Regeneration: " + input[idx++]);
            }
            if (enemy.properties.canGainMetallicize) {
                System.out.println("  Metallicize: " + input[idx++]);
            }
            if (enemy.properties.canGainPlatedArmor) {
                System.out.println("  Plated Armor: " + input[idx++]);
            }
            if (enemy.properties.canGainRegeneration || enemy.properties.canHeal || properties.enemyCanGetCorpseExplosion) {
                System.out.println("  Max Health: " + input[idx++]);
            }

            if (enemy instanceof Enemy.MergedEnemy m) {
                System.out.print("  Current Enemy: [");
                for (int i = 0; i < m.possibleEnemies.size(); i++) {
                    if (i > 0) System.out.print(", ");
                    System.out.print(input[idx++]);
                }
                System.out.println("]");

                for (int pIdx = 0; pIdx < m.possibleEnemies.size(); pIdx++) {
                    System.out.print("  Moves for " + m.possibleEnemies.get(pIdx).getName() + ": [");
                    for (int i = 0; i < m.possibleEnemies.get(pIdx).properties.numOfMoves; i++) {
                        if (i > 0) System.out.print(", ");
                        System.out.print(input[idx++]);
                    }
                    System.out.println("]");
                    if (m.possibleEnemies.get(pIdx).properties.useLast2MovesForMoveSelection) {
                        System.out.print("  Last Moves for " + m.possibleEnemies.get(pIdx).getName() + ": [");
                        for (int i = 0; i < m.possibleEnemies.get(pIdx).properties.numOfMoves; i++) {
                            if (i > 0) System.out.print(", ");
                            System.out.print(input[idx++]);
                        }
                        System.out.println("]");
                    }
                }
            } else {
                System.out.print("  Current Moves: [");
                for (int i = 0; i < enemy.properties.numOfMoves; i++) {
                    if (i > 0) System.out.print(", ");
                    System.out.print(input[idx++]);
                }
                System.out.println("]");
                if (enemy.properties.useLast2MovesForMoveSelection) {
                    System.out.print("  Last Moves: [");
                    for (int i = 0; i < enemy.properties.numOfMoves; i++) {
                        if (i > 0) System.out.print(", ");
                        System.out.print(input[idx++]);
                    }
                    System.out.println("]");
                }
            }

            int enemyNNInputLen = enemy.getNNInputLen(properties);
            if (enemyNNInputLen > 0) {
                System.out.print("  Enemy Specific Input: [");
                for (int i = 0; i < enemyNNInputLen; i++) {
                    if (i > 0) System.out.print(", ");
                    System.out.print(input[idx++]);
                }
                System.out.println("]");
            }

            if (enemy instanceof EnemyExordium.RedLouse || enemy instanceof EnemyExordium.GreenLouse) {
                System.out.println("  Louse Curl Up: " + input[idx++]);
            }
        }

        System.out.println("Total inputs processed: " + idx + " / " + properties.inputLen);
    }

    public int[] getEnemyOrder() {
        if (properties.enemiesReordering == null) {
            return null;
        }
        int[] order = new int[enemies.size()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        for (var reordering : properties.enemiesReordering) {
            reordering.accept(this, order);
        }
        return order;
    }

    public void addGameActionToEndOfDeque(GameEnvironmentAction action) {
        if (gameActionDeque == null) {
            gameActionDeque = new CircularArray<>();
        }
        gameActionDeque.addLast(action);
    }

    public void addGameActionToStartOfDeque(GameEnvironmentAction action) {
        if (gameActionDeque == null) {
            gameActionDeque = new CircularArray<>();
        }
        gameActionDeque.addFirst(action);
    }

    public void exhaustCardFromHand(int cardIdx) {
        removeCardFromHand(cardIdx);
        exhaustedCardHandle(cardIdx, false);
    }

    public void exhaustCardFromHandByPosition(int idx, boolean updateImmediately) {
        exhaustedCardHandle(getHandArrForWrite()[idx], false);
        getHandArrForWrite()[idx] = -1;
        if (updateImmediately) {
            updateHandArr();
        }
    }

    public void exhaustedCardHandle(int cardIdx, boolean fromCardPlay) {
        if (cardIdx >= properties.realCardsLen) {
            cardIdx = properties.tmpModifiedCardReverseTransformIdxes[cardIdx];
        }
        if (fromCardPlay && properties.strangeSpoon != null && properties.strangeSpoon.isRelicEnabledInScenario(preBattleScenariosChosenIdx) && getSearchRandomGen().nextBoolean(RandomGenCtx.Misc)) {
            addCardToDiscard(cardIdx);
            return;
        }
        if (fromCardPlay && properties.blueCandle != null && properties.blueCandle.isRelicEnabledInScenario(preBattleScenariosChosenIdx) && properties.cardDict[cardIdx].cardType == Card.CURSE) {
            doNonAttackDamageToPlayer(1, false, null);
        }
        properties.cardDict[cardIdx].onExhaust(this);
        addCardToExhaust(cardIdx);
        for (int i = 0; i < properties.onExhaustHandlers.size(); i++) {
            properties.onExhaustHandlers.get(i).handle(this);
        }
    }

    public boolean drawCardByIdx(int cardIdx, boolean addToHand) {
        if (removeCardFromDeck(cardIdx, false)) {
            if (addToHand) addCardToHand(cardIdx);
            return true;
        }
        return false;
    }

    public int discardHand(boolean triggerDiscard) {
        for (int i = handArrLen - 1; i >= 0; i--) {
            addCardToDiscard(handArr[i]);
            if (triggerDiscard) {
                triggerDiscardEffect(handArr[i]);
            }
        }
        int l = handArrLen;
        handArrLen = 0;
        return l;
    }

    public void reshuffle() {
        for (GameEventHandler handler : properties.onShuffleHandlers) {
            handler.handle(this);
        }
        // generate a new deck arr with fixed order instead of reusing discard, will help with consistency
        var discard = GameStateUtils.getCardArrCounts(discardArr, discardArrLen, properties.realCardsLen);
        if (deckArrLen > 0) {
            var deck = GameStateUtils.getCardArrCounts(deckArr, deckArrLen, properties.realCardsLen);
            for (int i = 0; i < discard.length; i++) {
                discard[i] += deck[i];
            }
            discardArrLen += deckArrLen;
            deckArrLen = 0;
        }
        getDeckArrForWrite(discardArrLen);
        for (short i = 0; i < discard.length; i++) {
            for (int j = 0; j < discard[i]; j++) {
                getDeckArrForWrite(discardArrLen)[deckArrLen++] = i;
            }
        }
        if (properties.frozenEye != null && properties.frozenEye.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
            setIsStochastic();
            Utils.shuffle(this, getDeckArrForWrite(), deckArrLen, getSearchRandomGen());
            deckArrFixedDrawLen = deckArrLen;
        }
        discardArrLen = 0;
    }

    public String getActionString(GameAction action) {
        if (action.type() == GameActionType.BEGIN_BATTLE) {
            return "Begin Battle";
        } else if (action.type() == GameActionType.AFTER_RANDOMIZATION) {
            return "After Randomization";
        } else if (action.type() == GameActionType.PLAY_CARD) {
            return properties.cardDict[action.idx()].cardName;
        } else if (action.type() == GameActionType.END_TURN) {
            return "End Turn";
        } else if (action.type() == GameActionType.SELECT_ENEMY) {
            if (enemiesAlive > 1) {
                return "Select " + enemies.get(action.idx()).getName() + "(" + action.idx() + ")";
            } else {
                return "Select " + enemies.get(action.idx()).getName();
            }
        } else if (action.type() == GameActionType.SELECT_CARD_HAND) {
            return "Select " + properties.cardDict[action.idx()].cardName + " From Hand";
        } else if (action.type() == GameActionType.SELECT_CARD_DISCARD) {
            return "Select " + properties.cardDict[action.idx()].cardName + " From Discard";
        } else if (action.type() == GameActionType.SELECT_CARD_EXHAUST) {
            return "Select " + properties.cardDict[action.idx()].cardName + " From Exhaust";
        } else if (action.type() == GameActionType.SELECT_CARD_DECK) {
            return "Select " + properties.cardDict[action.idx()].cardName + " From Deck";
        } else if (action.type() == GameActionType.SCRY_KEEP_CARD) {
            if (action.idx() >= properties.realCardsLen) {
                return "Scry Discard Card";
            } else {
                return "Scry Keep " + properties.cardDict[action.idx()].cardName;
            }
        } else if (action.type() == GameActionType.SELECT_CARD_1_OUT_OF_3) {
            return "Select " + properties.cardDict[action.idx()].cardName + " Out Of 3";
        } else if (action.type() == GameActionType.USE_POTION) {
            return properties.potions.get(action.idx()).toString();
        } else if (action.type() == GameActionType.SELECT_SCENARIO) {
            return properties.preBattleGameScenariosList.get(action.idx()).getValue().desc();
        } else if (action.type() == GameActionType.BEGIN_PRE_BATTLE) {
            return "Begin Pre-Battle";
        } else if (action.type() == GameActionType.BEGIN_TURN) {
            return "Begin Turn";
        } else if (action.type() == GameActionType.END_SELECT_CARD_HAND) {
            return "End Selecting Card From Hand";
        } else if (action.type() == GameActionType.END_SELECT_CARD_1_OUT_OF_3) {
            return "End Selecting 1 Out Of 3";
        }
        return "Unknown";
    }

    public String getActionString(int i) {
        return i < 0 ? "None" : getActionString(getAction(i));
    }

    public GameAction getAction(int i) {
        return properties.actionsByCtx[actionCtx.ordinal()][getLegalActions()[i]];
    }

    public void clearNextStates() { // oom during training due to holding too many states
        if (ns != null) {
            Arrays.fill(ns, null);
        }
        bannedActions = null;
        transpositions = new HashMap<>();
        transpositionsLock = properties.multithreadedMTCS ? new ReentrantLock() : null;
        if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH) transpositionsParent = new HashMap<>();
        searchFrontier = null;
    }

    public void initSearchInfoLeaf() {
        if (q_total == null) q_total = new VArray(properties.v_total_len);
    }

    public void initSearchInfo() {
        if (q_total == null) {
            q_total = new VArray(properties.v_total_len);
        }
        if (q_child == null) {
            q_child = new VArray[policy.length];
        }
        n = new int[policy.length];
        ns = new State[policy.length];
    }

    public void clearAllSearchInfo() {
        policy = null;
        v_health = 0;
        v_win = 0;
        v_extra = null;
        q_total = null;
        q_child = null;
        n = null;
        ns = null;
        total_n = 0;
        transpositions = new HashMap<>();
        if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH) transpositionsParent = new HashMap<>();
        legalActions = null;
        terminalAction = -100;
        searchFrontier = null;
        bannedActions = null;
    }

    public void gainEnergy(int n) {
        energy = Math.max(energy + n, 0);
    }

    public void addCardToHand(int cardIndex) {
        if (handArrLen >= GameState.HAND_LIMIT) {
            addCardToDiscard(cardIndex);
        } else {
            handArr = cardIdxArrAdd(handArr, !handCloned, handArrLen, cardIndex);
            handArrLen++;
            handCloned = true;
        }
    }

    public int addCardToHandGeneration(int cardIndex) {
        if (properties.forceFieldCounterIdx >= 0) {
            var baseCard = properties.cardDict[cardIndex].getBaseCard();
            if (baseCard instanceof CardDefect.ForceField forceField) {
                if (forceField.energyCost == 4) {
                    cardIndex = properties.findCardIndex(properties.cardDict[cardIndex].wrap(new CardDefect.ForceField(Math.max(4 - getCounterForRead()[properties.forceFieldCounterIdx], 0))));
                }
            }
            if (baseCard instanceof CardDefect.ForceFieldP forceField) {
                if (forceField.energyCost == 4) {
                    cardIndex = properties.findCardIndex(properties.cardDict[cardIndex].wrap(new CardDefect.ForceFieldP(Math.max(4 - getCounterForRead()[properties.forceFieldCounterIdx], 0))));
                }
            }
        }
        addCardToHand(cardIndex);
        return cardIndex;
    }

    public boolean removeCardFromHand(int cardIndex) {
        for (int i = handArrLen - 1; i >= 0; i--) {
            if (handArr[i] == cardIndex) {
                getHandArrForWrite();
                for (int j = i; j < handArrLen - 1; j++) {
                    handArr[j] = handArr[j + 1];
                }
                handArrLen--;
                return true;
            }
        }
        return false;
    }

    public void removeCardFromHandByPosition(int idx) {
        for (int i = idx; i < handArrLen - 1; i++) {
            getHandArrForWrite()[idx] = getHandArrForRead()[idx + 1];
        }
        handArrLen--;
    }

    public void modifyCardInHandByPosition(int idx, int newCardIdx) {
        getHandArrForWrite()[idx] = (short) newCardIdx;
    }

    public void addCardToDiscard(int cardIndex) {
        cardIndex = cardIndex >= properties.realCardsLen ? properties.tmpModifiedCardReverseTransformIdxes[cardIndex] : cardIndex;
        discardArr = cardIdxArrAdd(discardArr, !discardCloned, discardArrLen, cardIndex);
        discardArrLen++;
        discardCloned = true;
    }

    public void removeCardFromDiscard(int cardIndex) {
        for (int i = discardArrLen - 1; i >= 0; i--) {
            if (discardArr[i] == cardIndex) {
                getDiscardArrForWrite();
                for (int j = i; j < discardArrLen - 1; j++) {
                    discardArr[j] = discardArr[j + 1];
                }
                discardArrLen--;
                break;
            }
        }
    }

    public void removeCardFromDiscardByPosition(int idx) {
        for (int i = idx; i < discardArrLen - 1; i++) {
            getDiscardArrForWrite()[i] = getDiscardArrForRead()[i + 1];
        }
        discardArrLen--;
    }

    private void triggerDiscardEffect(int cardIndex) {
        properties.cardDict[cardIndex].onDiscard(this);
        if (properties.hoveringKite != null && properties.hoveringKite.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
            if (getCounterForRead()[properties.hoveringKiteCounterIdx] == 0) {
                gainEnergy(1);
                getCounterForWrite()[properties.hoveringKiteCounterIdx] = 1;
            }
        }
        if (properties.sneakyStrikeCounterIdx >= 0) {
            getCounterForWrite()[properties.sneakyStrikeCounterIdx] = 1;
        }
        if (properties.eviscerateCounterIdx >= 0) {
            getCounterForWrite()[properties.eviscerateCounterIdx] += 1;
        }
        if (properties.tingsha != null && properties.tingsha.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
            addGameActionToEndOfDeque(new GameEnvironmentAction() {
                @Override public void doAction(GameState state) {
                    var enemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemySwordBoomerang);
                    state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(enemyIdx), 3, true);
                }
                @Override public boolean equals(Object o) {
                    return o.getClass().equals(this.getClass());
                }
            });
        }
        if (properties.toughBandages != null && properties.toughBandages.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
            getPlayerForWrite().gainBlockNotFromCardPlay(3);
        }
    }

    public void discardCardFromHand(int cardIndex) {
        if (removeCardFromHand(cardIndex)) {
            addCardToDiscard(cardIndex);
            triggerDiscardEffect(cardIndex);
        }
    }

    public void discardCardFromHandByPosition(int idx, boolean updateImmediately) {
        var cardIdx = getHandArrForRead()[idx];
        addCardToDiscard(cardIdx);
        triggerDiscardEffect(cardIdx);
        getHandArrForWrite()[idx] = -1;
        if (updateImmediately) {
            updateHandArr();
        }
    }

    public void discardCardFromHandByPosition2(int idx) {
        var cardIdx = getHandArrForRead()[idx];
        addCardToDiscard(cardIdx);
        getHandArrForWrite()[idx] = -1;
        updateHandArr();
    }

    public void addCardToDeck(int idx) {
        addCardToDeck(idx, true);
    }

    public void addCardToDeck(int idx, boolean random) {
        deckArrLen += 1;
        getDeckArrForWrite(deckArrLen);
        if (properties.frozenEye != null && properties.frozenEye.isRelicEnabledInScenario(preBattleScenariosChosenIdx) && random) {
            var idxToInsert = 0;
            if (deckArrLen > 0) {
                setIsStochastic();
                idxToInsert = getSearchRandomGen().nextInt(deckArrLen, RandomGenCtx.CardDraw, new Tuple3<GameState, Integer, Integer>(this, 1, idx));
            }
            for (int i = deckArrLen - 1; i > idxToInsert; i--) {
                deckArr[i] = deckArr[i - 1];
            }
            deckArr[idxToInsert] = (short) idx;
            deckArrFixedDrawLen++;
        } else {
            for (int i = deckArrLen - 1; i >= deckArrLen - deckArrFixedDrawLen; i--) {
                deckArr[i] = deckArr[i - 1];
            }
            deckArr[deckArrLen - 1 - deckArrFixedDrawLen] = (short) idx;
        }
    }

    public boolean removeCardFromDeck(int cardIndex, boolean random) {
        var count = GameStateUtils.getCardCount(deckArr, deckArrLen, cardIndex);
        if (count > 0) {
            int r = 0;
            if (random && deckArrFixedDrawLen > 0) {
                for (int i = 0; i < deckArrFixedDrawLen; i++) {
                    if (deckArr[deckArrLen - 1 - i] == cardIndex) {
                        setIsStochastic();
                        r = getSearchRandomGen().nextInt(count, RandomGenCtx.CardDraw);
                    }
                }
            }
            // todo: always removed from end of deck if multiple copies (seek can choose which to remove), need to add ability for deck to choose
            int k = 0;
            for (int i = 0; i < deckArrLen; i++) {
                if (deckArr[i] == cardIndex && (k++) == r) {
                    getDeckArrForWrite();
                    if (i > deckArrLen - 1 - deckArrFixedDrawLen) {
                        deckArrFixedDrawLen--;
                    }
                    for (int j = i; j < deckArrLen - 1; j++) {
                        deckArr[j] = deckArr[j + 1];
                    }
                    deckArrLen -= 1;
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public void setCardCountInDeck(int cardIndex, int count) {
        var currentCount = GameStateUtils.getCardCount(deckArr, deckArrLen, cardIndex);
        if (currentCount > count) {
            for (int i = currentCount - 1; i >= count; i--) {
                removeCardFromDeck(cardIndex, false);
            }
        } else if (currentCount < count) {
            for (int i = currentCount; i < count; i++) {
                addCardToDeck(cardIndex);
            }
        }
    }

    public void addCardOnTopOfDeck(int idx) {
        if (idx >= properties.realCardsLen) {
            idx = properties.tmpModifiedCardReverseTransformIdxes[idx];
        }
        deckArrLen += 1;
        getDeckArrForWrite(deckArrLen);
        deckArr[deckArrLen - 1] = (short) idx;
        deckArrFixedDrawLen++;
    }

    private void putCardOnTopOfDeck(int idx) {
        getDeckArrForWrite();
        for (int i = deckArrLen - deckArrFixedDrawLen - 1; i >= 0; i--) {
            if (deckArr[i] == idx) {
                deckArr[i] = deckArr[deckArrLen - deckArrFixedDrawLen - 1];
                deckArr[deckArrLen - deckArrFixedDrawLen - 1] = (short) idx;
            }
        }
        deckArrFixedDrawLen++;
    }

    public void addCardToExhaust(int cardIndex) {
        cardIndex = cardIndex >= properties.realCardsLen ? properties.tmpModifiedCardReverseTransformIdxes[cardIndex] : cardIndex;
        exhaustArr = cardIdxArrAdd(exhaustArr, !exhaustCloned, exhaustArrLen, cardIndex);
        exhaustArrLen++;
        exhaustCloned = true;
    }

    public void removeCardFromExhaust(int cardIndex) {
        for (int i = exhaustArrLen - 1; i >= 0; i--) {
            if (exhaustArr[i] == cardIndex) {
                getExhaustArrForWrite()[i] = getExhaustArrForRead()[exhaustArrLen - 1];
                exhaustArrLen--;
                break;
            }
        }
    }

    public GameActionCtx startScry(int count) {
        if (properties.goldenEye != null && properties.goldenEye.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
            count += 2;
        }
        var scryTotalCount = Math.min(count, deckArrLen);
        if (scryTotalCount == 0) {
            return GameActionCtx.PLAY_CARD;
        }
        scryCardIsKept = new boolean[scryTotalCount];
        if (scryTotalCount > deckArrFixedDrawLen) {
            if (properties.makingRealMove || properties.doingComparison) {
                Arrays.sort(getDeckArrForWrite(), 0, this.deckArrLen - deckArrFixedDrawLen);
            }
            for (int i = deckArrFixedDrawLen; i < scryTotalCount; i++) {
                setIsStochastic();
                var idx = getSearchRandomGen().nextInt(this.deckArrLen - deckArrFixedDrawLen, RandomGenCtx.CardDraw);
                var cardIdx = deckArr[idx];
                deckArr[idx] = deckArr[deckArr.length - 1 - i];
                deckArr[deckArr.length - 1 - i] = cardIdx;
                deckArrFixedDrawLen++;
            }
        }
        return GameActionCtx.SCRYING;
    }

    private boolean handleScryCardDecision(int cardIndex) {
        if (cardIndex < properties.realCardsLen) {
            scryCardIsKept = Arrays.copyOf(scryCardIsKept, scryCardIsKept.length);
            scryCardIsKept[scryCurrentCount] = true;
        }
        scryCurrentCount++;
        if (scryCurrentCount == scryCardIsKept.length) {
            // move all the cards that's not kept out of fixed deck portion
            int start = 0;
            int end = scryCardIsKept.length;
            while (true) {
                while (start < scryCardIsKept.length && scryCardIsKept[start]) {
                    start++;
                }
                while (end > start && !scryCardIsKept[end - 1]) {
                    end--;
                }
                if (start >= end) {
                    break;
                }
                var tmp = deckArr[deckArrLen - 1 - start];
                deckArr[deckArrLen - 1 - start] = deckArr[deckArrLen - 1 - end];
                deckArr[deckArrLen - 1 - end] = tmp;
            }
            for (int i = 0; i < scryCardIsKept.length; i++) {
                if (!scryCardIsKept[i]) {
                    deckArrFixedDrawLen--;
                }
            }
            scryCardIsKept = null;
            scryCurrentCount = 0;
            return true;
        }
        return false;
    }

    public int playerDoDamageToEnemy(Enemy enemy, int dmgInt, boolean isShiv) {
        var player = getPlayeForRead();
        double dmg = dmgInt;
        if ((buffs & PlayerBuff.AKABEKO.mask()) != 0) {
            dmg += 8;
        }
        if ((buffs & PlayerBuff.WRIST_BLADE.mask()) != 0) {
            dmg += 4;
        }
        dmg += player.getStrength();
        if (properties.accuracyCounterIdx >= 0 && isShiv) {
            dmg += getCounterForRead()[properties.accuracyCounterIdx];
        }
        if (properties.penNibCounterIdx >= 0 && counter[properties.penNibCounterIdx] == 9) {
            dmg *= 2;
        }
        if (properties.phantasmalKillerCounterIdx >= 0 && (counter[properties.phantasmalKillerCounterIdx] & ((1 << 8) - 1)) > 0) {
            dmg *= 2;
        }
        if (properties.wreathOfFlameCounterIdx >= 0 && counter[properties.wreathOfFlameCounterIdx] > 0) {
            dmg += counter[properties.wreathOfFlameCounterIdx];
        }
        if (enemy.getVulnerable() > 0) {
            dmg = dmg * (properties.paperPhrog != null && properties.paperPhrog.isRelicEnabledInScenario(preBattleScenariosChosenIdx) ? 1.75 : 1.5);
        }
        if (player.getWeak() > 0) {
            dmg = dmg * 0.75;
        }
        // Watcher stance damage modifiers
        if (properties.character == CharacterEnum.WATCHER) {
            if (stance == Stance.WRATH) {
                dmg *= 2;
            } else if (stance == Stance.DIVINITY) {
                dmg *= 3;
            }
        }
        if (enemy.isAlive() && enemy.getHealth() > 0) {
            int enemyBlockBefore = enemy.getBlock();
            int dmgDone = enemy.damage(dmg, this);

            if (dmgDone > 0 && enemyBlockBefore > 0 && enemy.getBlock() == 0 && properties.handDrill != null && 
                properties.handDrill.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
                enemy.applyDebuff(this, DebuffType.VULNERABLE, 2);
            }

            if (enemy.getHealth() == 0) {
                for (var handler : properties.onEnemyDeathHandlers) {
                    handler.handle(this, enemy);
                }
            } else if (dmgDone > 0 && properties.envenomCounterIdx >= 0 && getCounterForRead()[properties.envenomCounterIdx] > 0) {
                enemy.applyDebuff(this, DebuffType.POISON, getCounterForRead()[properties.envenomCounterIdx]);
            }
            if (enemy.getTalkToTheHand() > 0) {
                getPlayerForWrite().gainBlockNotFromCardPlay(enemy.getTalkToTheHand());
            }
            if (enemy instanceof EnemyBeyond.Spiker spiker) {
                doNonAttackDamageToPlayer(spiker.getThorn(), true, spiker);
            }
            if (!enemy.isAlive()) {
                adjustEnemiesAlive(-1);
            }
            return dmgDone;
        }
        return 0;
    }

    public int playerDoDamageToEnemy(Enemy enemy, int dmgInt) {
        return playerDoDamageToEnemy(enemy, dmgInt, false);
    }

    public void checkWristBladeBuffForZeroCostAttack(int cardIdx) {
        if (properties.wristBlade != null && properties.wristBlade.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
            if (properties.cardDict[cardIdx].cardType == Card.ATTACK && getCardEnergyCost(cardIdx) == 0) {
                buffs |= PlayerBuff.WRIST_BLADE.mask();
            }
        }
    }

    public void removeWristBladeBuff() {
        buffs &= ~PlayerBuff.WRIST_BLADE.mask();
    }

    public void adjustEnemiesAlive(int count) {
        enemiesAlive += count;
        if (enemiesAlive == 0) {
            addGameActionToStartOfDeque(new GameEnvironmentAction() {
                @Override public void doAction(GameState state) {
                    for (var handler : properties.endOfBattleHandlers) {
                        handler.handle(state);
                    }
                }
            });
        }
    }

    public void playerDoNonAttackDamageToEnemy(Enemy enemy, int dmg, boolean blockable) {
        if (enemy.isAlive() && enemy.getHealth() > 0) {
            enemy.nonAttackDamage(dmg, blockable, this);
            if (enemy.getHealth() == 0) {
                for (var handler : properties.onEnemyDeathHandlers) {
                    handler.handle(this, enemy);
                }
            }
            if (!enemy.isAlive()) {
                adjustEnemiesAlive(-1);
            }
        }
    }

    public int enemyDoDamageToPlayer(EnemyReadOnly enemy, int dmgInt, int times) {
        int move = enemy.getMove();
        int totalDmgDealt = 0;
        var player = getPlayerForWrite();
        double dmg = dmgInt + enemy.getStrength();
        if (dmg < 0) {
            dmg = 0;
        }
        if (player.getVulnerable() > 0) {
            dmg *= 1.5;
        }
        if (getStance() == Stance.WRATH) {
            dmg *= 2;
        }
        if (enemy.getWeak() > 0) {
            dmg *= properties.paperCrane != null && properties.paperCrane.isRelicEnabledInScenario(preBattleScenariosChosenIdx) ? 0.6 : 0.75;
        }
        if (properties.shieldAndSpireFacingIdx >= 0) {
            if (getCounterForRead()[properties.shieldAndSpireFacingIdx] == 1 && enemy instanceof EnemyEnding.SpireSpear) {
                dmg *= 1.5;
            } else if (getCounterForRead()[properties.shieldAndSpireFacingIdx] == 2 && enemy instanceof EnemyEnding.SpireShield) {
                dmg *= 1.5;
            }
        }
        if (properties.intangibleCounterIdx >= 0 && getCounterForRead()[properties.intangibleCounterIdx] > 0 && dmg > 0) {
            dmg = 1;
        }
        for (int i = 0; i < times; i++) {
            if (!enemy.isAlive() || enemy.getMove() != move) { // dead or interrupted
                return totalDmgDealt;
            }
            int dmgDealt = player.damage(this, (int) dmg);
            totalDmgDealt += dmgDealt;
            if (dmgDealt >= 0) {
                for (OnDamageHandler handler : properties.onDamageHandlers) {
                    handler.handle(this, enemy, true, dmgDealt);
                }
            }
        }
        return totalDmgDealt;
    }

    public int enemyCalcDamageToPlayer(Enemy enemy, int dmgInt) {
        double dmg = dmgInt;
        dmg += enemy.getStrength();
        if (dmg < 0) {
            dmg = 0;
        }
        if (player.getVulnerable() > 0) {
            dmg *= 1.5;
        }
        if (getStance() == Stance.WRATH) {
            dmg *= 2;
        }
        if (enemy.getWeak() > 0) {
            dmg *= properties.paperCrane != null && properties.paperCrane.isRelicEnabledInScenario(preBattleScenariosChosenIdx) ? 0.6 : 0.75;
        }
        if (properties.shieldAndSpireFacingIdx >= 0) {
            if (getCounterForRead()[properties.shieldAndSpireFacingIdx] == 1 && enemy instanceof EnemyEnding.SpireSpear) {
                dmg = dmg + dmg / 2;
            } else if (getCounterForRead()[properties.shieldAndSpireFacingIdx] == 2 && enemy instanceof EnemyEnding.SpireShield) {
                dmg = dmg + dmg / 2;
            }
        }
        if (properties.intangibleCounterIdx >= 0 && getCounterForRead()[properties.intangibleCounterIdx] > 0 && dmg > 0) {
            dmg = 1;
        }
        return (int) dmg;
    }

    public void doNonAttackDamageToPlayer(int dmg, boolean blockable, Object source) {
        if (properties.intangibleCounterIdx >= 0 && getCounterForRead()[properties.intangibleCounterIdx] > 0 && dmg > 0) {
            dmg = 1;
        }
        var damageDealt = getPlayerForWrite().nonAttackDamage(this, dmg, blockable);
        if (dmg > 0 && properties.tungstenRod != null && properties.tungstenRod.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
            dmg -= 1;
        }
        if (dmg > 0) {
            for (OnDamageHandler handler : properties.onDamageHandlers) {
                handler.handle(this, source, false, damageDealt);
            }
        }
    }

    public void healPlayer(int hp) {
        if (properties.markOfTheBloom != null && properties.markOfTheBloom.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
            return;
        }
        if (properties.magicFlower != null && properties.magicFlower.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
            hp = (int) Math.ceil(hp * 1.5);
        }
        var healed = getPlayerForWrite().heal(hp);
        if (healed > 0) {
            for (var handler : properties.onHealHandlers) {
                handler.handle(this, null, false, healed);
            }
        }
    }

    public short[] getDeckArrForRead() {
        return deckArr;
    }

    private short[] getDeckArrForWrite() {
        if (!deckCloned) {
            deckArr = Arrays.copyOf(deckArr, Math.min(deckArr.length, deckArrLen + 2));
            deckCloned = true;
        }
        return deckArr;
    }

    private short[] getDeckArrForWrite(int newArrLen) {
        if (newArrLen >= deckArr.length) {
            deckArr = Arrays.copyOf(deckArr, newArrLen);
            if (!deckCloned) {
                deckCloned = true;
            }
        } else if (!deckCloned) {
            deckArr = Arrays.copyOf(deckArr, deckArr.length);
            deckCloned = true;
        }
        return deckArr;
    }

    public short[] getHandArrForRead() {
        return handArr;
    }

    public short[] getHandArrForWrite() {
        if (!handCloned) {
            handArr = Arrays.copyOf(handArr, Math.min(handArr.length, handArrLen + 2));
            handCloned = true;
        }
        return handArr;
    }

    public void updateHandArr() {
        getHandArrForWrite();
        int write = 0;
        for (int i = 0; i < handArrLen; i++) {
            if (handArr[i] >= 0) {
                handArr[write++] = handArr[i];
            }
        }
        handArrLen = write;
    }

    public short[] getDiscardArrForRead() {
        return discardArr;
    }

    public short[] getDiscardArrForWrite() {
        if (!discardCloned) {
            discardArr = Arrays.copyOf(discardArr, Math.min(discardArr.length, discardArrLen + 2));
            discardCloned = true;
        }
        return discardArr;
    }

    public short[] getExhaustArrForRead() {
        return exhaustArr;
    }

    public short[] getExhaustArrForWrite() {
        if (!exhaustCloned) {
            exhaustArr = Arrays.copyOf(exhaustArr, Math.min(exhaustArr.length, exhaustArrLen + 2));
            exhaustCloned = true;
        }
        return exhaustArr;
    }

    public void addCardToChosenCards(int cardIndex) {
        chosenCardsArr = cardIdxArrAdd(chosenCardsArr, true, chosenCardsArrLen, cardIndex);
        chosenCardsArrLen++;
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

    public int playerGainBlock(int n) {
        int blockGained = getPlayerForWrite().gainBlock(n);
        if (blockGained > 0) {
            for (var handler : properties.onBlockHandlers) {
                handler.handle(this);
            }
        }
        return blockGained;
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

    public short[] getPotionsStateForRead() {
        return potionsState;
    }

    public short[] getPotionsStateForWrite() {
        if (!potionsStateCloned) {
            potionsState = Arrays.copyOf(potionsState, potionsState.length);
            potionsStateCloned = true;
        }
        return potionsState;
    }

    public GameAction getCurrentAction() {
        return currentAction;
    }

    public RandomGen getSearchRandomGen() {
        return getSearchRandomGen(properties.makingRealMove);
    }

    public RandomGen getSearchRandomGen(boolean realMove) {
        if (realMove || searchRandomGen == null) {
            return properties.realMoveRandomGen != null ? properties.realMoveRandomGen : properties.random;
        }
        if (Configuration.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION && !searchRandomGenCloned) {
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

    public void killEnemy(int i, boolean triggerOnDeathHandler) {
        if (getEnemiesForRead().get(i).isAlive()) {
            getEnemiesForWrite().getForWrite(i).setHealth(0);
            adjustEnemiesAlive(-1);
            if (triggerOnDeathHandler) {
                for (var handler : properties.onEnemyDeathHandlers) {
                    handler.handle(this, getEnemiesForRead().get(i));
                }
            }
        }
    }

    public void reviveEnemy(int idx, boolean getNextMove, int startingHealth) {
        if (!getEnemiesForRead().get(idx).isAlive()) {
            getEnemiesForWrite().replace(idx, properties.originalEnemies.getForWrite(idx));
            setIsStochastic();
            var enemy = getEnemiesForWrite().getForWrite(idx);
            if (startingHealth < 0) {
                enemy.randomize(getSearchRandomGen(), properties.curriculumTraining, -1);
            } else {
                enemy.setHealth(startingHealth);
            }
            enemy.properties = enemy.properties.clone();
            enemy.properties.origHealth = enemy.getHealth();
            if (getNextMove && !properties.isRunicDomeEnabled(this)) {
                var oldIsStochastic = isStochastic;
                isStochastic = false;
                enemy.nextMove(this, getSearchRandomGen());
                if (properties.makingRealMove && isStochastic) {
                    getStateDesc().append(getStateDesc().length() > 0 ? "; " : "").append(enemy.getName()).append(" (").append(idx).append(") choose move ").append(enemy.getMoveString(this));
                }
                isStochastic = oldIsStochastic | isStochastic;
            }
            adjustEnemiesAlive(1);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public boolean potionUsed(int i) {
        return potionsState[i * 3 + 2] == 1 && potionsState[i * 3] == 0;
    }

    public boolean hadPotion(int i) {
        return potionsState[i * 3 + 2] == 1;
    }

    public boolean potionUsable(int i) {
        return potionsState[i * 3] == 1 && potionPenalty(i) > 0;
    }

    public int potionPenalty(int i) {
        return potionsState[i * 3 + 1];
    }

    public void setPotionUsable(int i) {
        getPotionsStateForWrite()[i * 3] = 1;
        getPotionsStateForWrite()[i * 3 + 2] = 1;
    }

    public void setPotionPenalty(int i, short penalty) {
        getPotionsStateForWrite()[i * 3 + 1] = penalty;
    }

    public void setPotionUnusable(int i, short penalty) {
        getPotionsStateForWrite()[i * 3] = 0;
        getPotionsStateForWrite()[i * 3 + 1] = penalty;
        getPotionsStateForWrite()[i * 3 + 2] = 0;
    }

    public void setPotionUsed(int i) {
        getPotionsStateForWrite()[i * 3] = 0;
    }

    public void setSelect1OutOf3Idxes(int idx1, int idx2, int idx3) {
        idx1 = idx1 >= 0 && idx1 < properties.cardDict.length ? properties.select1OutOf3CardsReverseIdxes[idx1] : 255;
        idx2 = idx2 >= 0 && idx2 < properties.cardDict.length ? properties.select1OutOf3CardsReverseIdxes[idx2] : 255;
        idx3 = idx3 >= 0 && idx3 < properties.cardDict.length ? properties.select1OutOf3CardsReverseIdxes[idx3] : 255;
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

    public void setSelect1OutOf3Idxes(int[] possibleCardIdxes) {
        boolean interactive = getSearchRandomGen() instanceof InteractiveMode.RandomGenInteractive;
        int idx1 = getSearchRandomGen().nextInt(possibleCardIdxes.length, RandomGenCtx.SelectCard1OutOf3,
                interactive ? new Tuple3<>(this, (255 << 8) + 255, possibleCardIdxes) : null);
        int idx2 = getSearchRandomGen().nextInt(possibleCardIdxes.length - 1, RandomGenCtx.SelectCard1OutOf3,
                interactive ? new Tuple3<>(this, (255 << 8) + idx1, possibleCardIdxes) : null);
        int idx3 = getSearchRandomGen().nextInt(possibleCardIdxes.length - 2, RandomGenCtx.SelectCard1OutOf3,
                interactive ? new Tuple3<>(this, (idx2 << 8) + idx1, possibleCardIdxes) : null);
        if (idx2 >= idx1) {
            idx2++;
        }
        if (idx3 >= Math.min(idx1, idx2)) {
            idx3++;
        }
        if (idx3 >= Math.max(idx1, idx2)) {
            idx3++;
        }
        setSelect1OutOf3Idxes(possibleCardIdxes[idx1], possibleCardIdxes[idx2], possibleCardIdxes[idx3]);
        setIsStochastic();
    }

    public double getVExtra(int vExtraIdx) {
        return v_extra == null ? 0 : v_extra[vExtraIdx];
    }

    public void setIsStochastic() {
        // todo: I think the battle ends immediately on enemies death, maybe only distilled
        // chaos/havoc with bird faced urn, or meat on the bone + e.g. guardian may heal/dmg?
        if (isTerminal() <= 0) {
            isStochastic = true;
        }
    }

    // Defect specific
    public short[] getOrbs() {
        return orbs;
    }

    public void channelOrb(OrbType orb) {
        if (orbs == null) return;
        if (orbs[orbs.length - 2] != 0) {
            evokeOrb(1);
            orbs[orbs.length - 2] = (short) orb.ordinal();
            if (orb == OrbType.DARK) {
                orbs[orbs.length - 1] = 6;
            }
        } else {
            for (int i = 0; i < orbs.length; i += 2) {
                if (orbs[i] == 0) {
                    orbs[i] = (short) orb.ordinal();
                    if (orb == OrbType.DARK) {
                        orbs[i + 1] = 6;
                    }
                    break;
                }
            }
        }
        if (properties.blizzardCounterIdx >= 0 && orb == OrbType.FROST) {
            getCounterForWrite()[properties.blizzardCounterIdx]++;
        } else if (properties.thunderStrikeCounterIdx >= 0 && orb == OrbType.LIGHTNING) {
            getCounterForWrite()[properties.thunderStrikeCounterIdx]++;
        }
    }

    public void triggerRightmostOrbActive() {
        if (orbs == null) return;
        if (orbs[0] == OrbType.FROST.ordinal()) {
            getPlayerForWrite().gainBlockNotFromCardPlay(5 + focus);
        } else if (orbs[0] == OrbType.LIGHTNING.ordinal()) {
            if (properties.electrodynamicsCounterIdx >= 0 && getCounterForRead()[properties.electrodynamicsCounterIdx] != 0) {
                for (Enemy enemy : getEnemiesForWrite().iterateOverAlive()) {
                    int dmg = 8 + focus + (enemy.getLockOn() > 0 ? (8 + focus) / 2 : 0);
                    playerDoNonAttackDamageToEnemy(enemy, dmg, true);
                }
            } else {
                int idx = GameStateUtils.getRandomEnemyIdx(this, RandomGenCtx.RandomEnemyLightningOrb);
                if (idx >= 0) {
                    var enemy = getEnemiesForWrite().getForWrite(idx);
                    if ((properties.makingRealMove || properties.stateDescOn) && enemiesAlive > 1) {
                        getStateDesc().append(getStateDesc().length() > 0 ? "; " : "").append("Lightning Orb evoke hit ").append(enemy.getName() + " (" + idx + ")");
                    }
                    int dmg = 8 + focus + (enemy.getLockOn() > 0 ? (8 + focus) / 2 : 0);
                    playerDoNonAttackDamageToEnemy(enemy, dmg, true);
                }
            }
        } else if (orbs[0] == OrbType.DARK.ordinal()) {
            Enemy minEnemy = null;
            for (var enemy : getEnemiesForWrite().iterateOverAlive()) {
                if (minEnemy == null || minEnemy.getHealth() > enemy.getHealth()) {
                    minEnemy = enemy;
                }
            }
            if (minEnemy != null) {
                playerDoNonAttackDamageToEnemy(minEnemy, orbs[1], true);
            }
        } else if (orbs[0] == OrbType.PLASMA.ordinal()) {
            gainEnergy(2);
        }
    }

    public void evokeOrb(int times) {
        if (orbs == null) return;
        for (int i = 0; i < times; i++) {
            triggerRightmostOrbActive();
        }
        System.arraycopy(orbs, 2, orbs, 0, orbs.length - 2);
        orbs[orbs.length - 2] = 0;
        orbs[orbs.length - 1] = 0;
    }

    public void removeRightmostOrb() {
        if (orbs == null) return;
        System.arraycopy(orbs, 2, orbs, 0, orbs.length - 2);
        orbs[orbs.length - 2] = 0;
        orbs[orbs.length - 1] = 0;
    }

    public void rotateOrbToBack() {
        if (orbs == null || orbs[0] == 0) return;
        short orb1 = orbs[0];
        short orb2 = orbs[1];
        System.arraycopy(orbs, 2, orbs, 0, orbs.length - 2);
        orbs[orbs.length - 2] = 0;
        orbs[orbs.length - 1] = 0;
        for (int i = 0; i < orbs.length; i += 2) {
            if (orbs[i] == 0) {
                orbs[i] = orb1;
                orbs[i + 1] = orb2;
                break;
            }
        }
        if (properties.blizzardCounterIdx >= 0 && orb1 == OrbType.FROST.ordinal()) {
            getCounterForWrite()[properties.blizzardCounterIdx]++;
        } else if (properties.thunderStrikeCounterIdx >= 0 && orb1 == OrbType.LIGHTNING.ordinal()) {
            getCounterForWrite()[properties.thunderStrikeCounterIdx]++;
        }
    }

    private void triggerNonPlasmaOrbPassive(int i) {
        if (orbs[i] == OrbType.FROST.ordinal()) {
            getPlayerForWrite().gainBlockNotFromCardPlay(2 + focus);
        } else if (orbs[i] == OrbType.LIGHTNING.ordinal()) {
            if (properties.electrodynamicsCounterIdx >= 0 && getCounterForRead()[properties.electrodynamicsCounterIdx] != 0) {
                for (Enemy enemy : getEnemiesForWrite().iterateOverAlive()) {
                    int dmg = 3 + focus + (enemy.getLockOn() > 0 ? (3 + focus) / 2 : 0);
                    playerDoNonAttackDamageToEnemy(enemy, dmg, true);
                }
            } else {
                int idx = GameStateUtils.getRandomEnemyIdx(this, RandomGenCtx.RandomEnemyLightningOrb);
                if (idx >= 0) {
                    var enemy = getEnemiesForWrite().getForWrite(idx);
                    if ((properties.makingRealMove || properties.stateDescOn) && enemiesAlive > 1) {
                        getStateDesc().append(getStateDesc().length() > 0 ? "; " : "").append("Lightning Orb passive hit ").append(enemy.getName() + " (" + idx + ")");
                    }
                    int dmg = 3 + focus + (enemy.getLockOn() > 0 ? (3 + focus) / 2 : 0);
                    playerDoNonAttackDamageToEnemy(enemy, dmg, true);
                }
            }
        } else if (orbs[i] == OrbType.DARK.ordinal()) {
            orbs[i + 1] += Math.max(0, 6 + focus);
        }
    }

    private void triggerOrbsPassiveEndOfTurn() {
        if (orbs == null) return;
        for (int i = 0; i < orbs.length; i += 2) {
            triggerNonPlasmaOrbPassive(i);
            if (properties.goldPlatedCable != null && properties.goldPlatedCable.isRelicEnabledInScenario(preBattleScenariosChosenIdx) && i == 0) {
                triggerNonPlasmaOrbPassive(i);
            }
        }
    }

    private void triggerOrbsPassiveStartOfTurn() {
        if (orbs == null) return;
        int triggerLoopCount = properties.loopCounterIdx >= 0 ? getCounterForRead()[properties.loopCounterIdx] : 0;
        for (int i = 0; i < orbs.length; i += 2) {
            if (orbs[i] == OrbType.PLASMA.ordinal()) {
                gainEnergy(1);
                if (properties.goldPlatedCable != null && properties.goldPlatedCable.isRelicEnabledInScenario(preBattleScenariosChosenIdx) && i == 0) {
                    gainEnergy(1);
                }
                if (triggerLoopCount > 0 && i == 0) {
                    gainEnergy(triggerLoopCount);
                }
            } else if (triggerLoopCount > 0 && i == 0) {
                for (int j = 0; j < triggerLoopCount; j++) {
                    triggerNonPlasmaOrbPassive(i);
                }
            }
        }
    }

    public void triggerAllOrbsPassive() {
        if (orbs == null) return;
        for (int i = 0; i < orbs.length; i += 2) {
            if (orbs[i] == OrbType.PLASMA.ordinal()) {
                gainEnergy(1);
            } else {
                triggerNonPlasmaOrbPassive(i);
            }
        }
    }

    public void triggerDarkPassive() {
        if (orbs == null) return;
        for (int i = 0; i < orbs.length; i += 2) {
            if (orbs[i] == OrbType.DARK.ordinal()) {
                orbs[i + 1] += Math.max(0, 6 + focus);
            }
        }
    }

    public void gainOrbSlot(int n) {
        if (orbs == null) {
            return;
        }
        if (n < 0) {
            if (orbs.length / 2 + n == 0) {
                orbs = null;
            } else {
                orbs = Arrays.copyOf(orbs, orbs.length + n * 2);
            }
        } else {
           var newLen = Math.min(20, orbs.length + n * 2);
           if (orbs.length == newLen) {
               return;
           }
            var newOrbs = new short[newLen];
            System.arraycopy(orbs, 0, newOrbs, 0, orbs.length);
            orbs = newOrbs;
        }
    }

    public void gainFocus(int n) {
        focus += n;
    }

    public short getFocus() {
        return focus;
    }

    public void gainMantra(int amount) {
        if (properties.mantraCounterIdx < 0) {
            return;
        }
        if (properties.brillianceCounterIdx >= 0) {
            getCounterForWrite()[properties.brillianceCounterIdx] += amount;
        }
        int currentMantra = getCounterForRead()[properties.mantraCounterIdx];
        int newMantra = (currentMantra + amount) % 10;
        if (currentMantra + amount >= 10) {
            changeStance(Stance.DIVINITY);
            getCounterForWrite()[properties.mantraCounterIdx] = newMantra;
        } else {
            getCounterForWrite()[properties.mantraCounterIdx] = newMantra;
        }
    }

    public Stance getStance() {
        return stance;
    }

    public int getLastCardPlayedType() {
        return lastCardPlayedType;
    }

    public void changeStance(Stance newStance) {
        if (properties.character != CharacterEnum.WATCHER) {
            return;
        }
        // Only trigger handlers and effects if stance actually changes
        if (stance == newStance) {
            return;
        }
        if (stance == Stance.CALM && newStance != Stance.CALM) {
            gainEnergy(2);
            if (properties.violetLotus != null && properties.violetLotus.isRelicEnabledInScenario(preBattleScenariosChosenIdx)) {
                gainEnergy(1);
            }
        }
        if (newStance == Stance.DIVINITY && stance != Stance.DIVINITY) {
            gainEnergy(3);
        }
        stance = newStance;
        for (var handler : properties.onStanceChangeHandlers) {
            handler.handle(this);
        }
    }

    public void exitDivinityAtStartOfTurn() {
        if (stance == Stance.DIVINITY) {
            changeStance(Stance.NEUTRAL);
        }
    }

    public boolean isFirstEncounter() {
        if (!properties.isHeartGauntlet) {
            return true;
        }
        if (!enemies.get(2).isAlive()) {
            return true;
        }
        return false;
    }
}
