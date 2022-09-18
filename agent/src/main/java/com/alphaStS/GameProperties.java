package com.alphaStS;

import com.alphaStS.enemy.EnemyList;

import java.util.*;
import java.util.function.BiConsumer;

public class GameProperties implements Cloneable {
    public boolean testNewFeature = true;
    public boolean doingComparison;
    public boolean testPotionOutput;
    public boolean playerArtifactCanChange;
    public boolean playerStrengthCanChange;
    public boolean playerDexterityCanChange;
    public boolean playerStrengthEotCanChange;
    public boolean playerDexterityEotCanChange;
    public boolean playerFocusCanChange;
    public boolean playerCanGetVuln;
    public boolean playerCanGetWeakened;
    public boolean playerCanGetFrailed;
    public boolean playerCanGetEntangled;
    public boolean enemyCanGetVuln;
    public boolean enemyCanGetWeakened;
    public boolean enemyStrengthCanChange;
    public boolean enemyStrengthEotCanChange;
    public long possibleBuffs;
    public boolean needDeckOrderMemory;
    public boolean selectFromExhaust;
    public RandomGen random;
    public RandomGen realMoveRandomGen;
    public boolean makingRealMove;
    public boolean stateDescOn;
    public Card[] cardDict;
    public List<Potion> potions;
    public EnemyList originalEnemies;
    public int maxNumOfActions;
    public int totalNumOfActions;
    public GameAction[][] actionsByCtx;

    // cached card indexes
    public int realCardsLen; // not including CardTmpCostChange (due to liquid memory)
    public int[] discardIdxes; // cards that can change in number of copies during a fight
    public int[] discardReverseIdxes;
    public int[] upgradeIdxes;
    public int[] tmpCostCardIdxes;
    public int[] select1OutOf3CardsIdxes;
    public int[] select1OutOf3CardsReverseIdxes;
    public int[] skillPotionIdxes;
    public int angerCardIdx = -1;
    public int angerPCardIdx = -1;
    public int[] strikeCardIdxes;
    // cached status indexes
    public int burnCardIdx = -1;
    public int burnPCardIdx = -1;
    public int dazedCardIdx = -1;
    public int slimeCardIdx = -1;
    public int woundCardIdx = -1;
    public int voidCardIdx = -1;
    public int echoFormCardIdx = -1;
    public int echoFormPCardIdx = -1;
    public int[] bloodForBloodIndexes;
    public int[] bloodForBloodPIndexes;
    public int[] infernalBladeIndexes;
    public int[] healCardsIdxes;
    public List<TrainingTarget> extraTrainingTargets = new ArrayList<>();
    public int ritualDaggerCounterIdx = -1;
    public int feedCounterIdx = -1;
    public int echoFormCounterIdx = -1;
    public int selfRepairCounterIdx = -1;

    public boolean hasBlueCandle;
    public boolean hasBoot;
    public boolean hasToyOrniphopter;
    public boolean hasCaliper;
    public boolean hasGinger;
    public boolean hasIceCream;
    public boolean hasMedicalKit;
    public boolean hasOddMushroom;
    public boolean hasRunicDome;
    public boolean hasRunicPyramid;
    public boolean hasSacredBark;
    public boolean hasStrangeSpoon;
    public boolean hasTurnip;
    public boolean hasChampionBelt;
    public boolean hasPaperPhrog;
    public boolean hasMeatOnBone;
    public boolean hasBurningBlood;

    public int normalityCounterIdx = -1;
    public int penNibCounterIdx = -1;

    // cached game properties for generating NN input
    public boolean battleTranceExist;
    public boolean energyRefillCanChange;
    public boolean isSlimeBossFight;
    public int inputLen;
    public int extraOutputLen;

    // relics/cards can add checks like e.g. Burn checking if it's in hand pre end of turn
    public Map<String, Object> gameEventHandlers = new HashMap<>();
    public List<GameEventHandler> startOfBattleHandlers = new ArrayList<>();
    public List<GameEventHandler> endOfBattleHandlers = new ArrayList<>();
    public List<GameEventHandler> startOfTurnHandlers = new ArrayList<>();
    public List<GameEventHandler> preEndTurnHandlers = new ArrayList<>();
    public List<GameEventHandler> onExhaustHandlers = new ArrayList<>();
    public List<GameEventHandler> onBlockHandlers = new ArrayList<>(); // todo: need to call handler
    public List<GameEventHandler> onEnemyDeathHandlers = new ArrayList<>();
    public List<OnDamageHandler> onDamageHandlers = new ArrayList<>();
    public List<OnDamageHandler> onHealHandlers = new ArrayList<>();
    public List<GameEventCardHandler> onCardPlayedHandlers = new ArrayList<>();
    public List<GameEventCardHandler> onCardDrawnHandlers = new ArrayList<>();
    public GameStateRandomization randomization;
    public GameStateRandomization preBattleRandomization;
    public GameStateRandomization preBattleScenarios;
    public List<Map.Entry<Integer, GameStateRandomization.Info>> preBattleGameScenariosList;
    public List<BiConsumer<GameState, int[]>> enemiesReordering;

    // defect specific
    public int maxNumOfOrbs;

    public double cpuct = 0.1;

    public GameProperties clone() {
        try {
            return (GameProperties) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public GameProperties() {
        random = new RandomGen.RandomGenPlain();
    }

    public int findCardIndex(Card card) {
        for (int i = 0; i < cardDict.length; i++) {
            if (cardDict[i].equals(card)) {
                return i;
            }
        }
        return -1;
    }

    private Map<String, Integer> cardIndexCache = new HashMap<>();

    public int findCardIndex(String cardName) {
        if (cardIndexCache.size() == 0) { // todo: move to construction
            for (int i = 0; i < cardDict.length; i++) {
                cardIndexCache.put(cardDict[i].cardName, i);
            }
        }
        Integer v = cardIndexCache.get(cardName);
        return v == null ? -1 : v;
    }

    interface CounterRegistrant {
        void setCounterIdx(GameProperties gameProperties, int idx);
    }

    interface TrainingTargetRegistrant {
        void setVArrayIdx(int idx);
    }

    public interface NetworkInputHandler {
        int addToInput(GameState state, float[] input, int idx);
        int getInputLenDelta();
        default String getDisplayString(GameState state) { return null; }
    }

    Map<String, List<CounterRegistrant>> counterRegistrants = new HashMap<>();
    Map<String, Integer> counterIdx = new HashMap<>();
    Map<String, NetworkInputHandler> counterHandlerMap = new HashMap<>();
    Map<String, NetworkInputHandler> nnInputHandlerMap = new HashMap<>();
    NetworkInputHandler[] nnInputHandlers;
    String[] nnInputHandlersName;

    public void addNNInputHandler(String name, NetworkInputHandler handler) {
        nnInputHandlerMap.putIfAbsent(name, handler);
    }

    public void registerCounter(String name, CounterRegistrant registrant, NetworkInputHandler handler) {
        var registrants = counterRegistrants.computeIfAbsent(name, k -> new ArrayList<>());
        registrants.add(registrant);
        if (handler != null) {
            counterHandlerMap.putIfAbsent(name, handler);
        }
    }

    public boolean hasCounter(String name) {
        return counterRegistrants.get(name) != null;
    }

    public int getCounterIdx(String name) {
        return counterIdx.get(name);
    }

    String[] counterNames;
    NetworkInputHandler[] counterHandlers;
    NetworkInputHandler[] counterHandlersNonNull;

    public void compileCounterInfo() {
        var names = counterRegistrants.keySet().stream().sorted().toList();
        counterNames = names.toArray(new String[] {});
        counterHandlers = new NetworkInputHandler[counterNames.length];
        for (int i = 0; i < counterNames.length; i++) {
            counterHandlers[i] = counterHandlerMap.get(counterNames[i]);
            for (CounterRegistrant registrant : counterRegistrants.get(counterNames[i])) {
                registrant.setCounterIdx(this, i);
                counterIdx.put(counterNames[i], i);
            }
        }
        counterHandlersNonNull = Arrays.stream(counterHandlers).filter(Objects::nonNull).toList()
                .toArray(new NetworkInputHandler[0]);

        nnInputHandlers = new NetworkInputHandler[nnInputHandlerMap.size()];
        names = nnInputHandlerMap.keySet().stream().sorted().toList();
        nnInputHandlersName = names.toArray(new String[] {});
        for (int i = 0; i < nnInputHandlersName.length; i++) {
            nnInputHandlers[i] = nnInputHandlerMap.get(nnInputHandlersName[i]);
        }
    }

    Map<String, TrainingTarget> trainingTargetsMap = new HashMap<>();
    Map<String, TrainingTargetRegistrant> trainingTargetsRegistrantMap = new HashMap<>();

    public void addExtraTrainingTarget(String targetId, TrainingTargetRegistrant registrant, TrainingTarget target) {
        if (trainingTargetsMap.get(targetId) == null) {
            trainingTargetsMap.put(targetId, target);
            trainingTargetsRegistrantMap.put(targetId, registrant);
        }
    }

    public void compilerExtraTrainingTarget() {
        var registrants = trainingTargetsRegistrantMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
        for (int i = 0; i < registrants.size(); i++) {
            registrants.get(i).getValue().setVArrayIdx(extraOutputLen++);
            extraTrainingTargets.add(trainingTargetsMap.get(registrants.get(i).getKey()));
        }
    }
}
