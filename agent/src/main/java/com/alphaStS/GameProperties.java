package com.alphaStS;

import com.alphaStS.card.Card;
import com.alphaStS.enemy.EnemyEncounter;
import com.alphaStS.enemy.EnemyList;
import com.alphaStS.enemy.EnemyReadOnly;
import com.alphaStS.entity.Potion;
import com.alphaStS.entity.Relic;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventEnemyDebuffHandler;
import com.alphaStS.eventHandler.GameEventEnemyHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.eventHandler.OnCardCreationHandler;
import com.alphaStS.eventHandler.OnDamageHandler;
import com.alphaStS.eventHandler.OnEnergySpendHandler;
import com.alphaStS.gameAction.GameAction;
import com.alphaStS.random.RandomGen;
import com.alphaStS.utils.CounterStat;
import com.alphaStS.utils.Tuple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class GameProperties implements Cloneable {
    // MCTS/Training/Playing etc. specific variables
    public boolean testNewFeature = true;
    public boolean multithreadedMTCS;
    public boolean doingComparison;
    public boolean curriculumTraining;
    public boolean isTraining;
    public int difficultyChosen;
    public MCTS currentMCTS;
    public RandomGen random;
    public RandomGen realMoveRandomGen;
    public boolean makingRealMove;
    public boolean isInteractive;
    public boolean stateDescOn;
    public double cpuct = 0.1;
    public float maxPossibleRealTurnsLeft = 50.0f;

    // General 
    public CharacterEnum character;
    public Card[] cardDict;
    public int realCardsLen; // not including CardTmpCostChange (e.g. from liquid memory)
    public List<Relic> relics;
    // defect specific
    public int maxNumOfOrbs;
    public EntityProperty anyEntityProperty = new EntityProperty();
    public EnemyList originalEnemies;
    public int maxNumOfActions;
    public int totalNumOfActions;
    public GameAction[][] actionsByCtx;
    public GameStateRandomization preBattleRandomization;
    public GameStateRandomization preBattleScenarios;
    public GameStateRandomization preBattleScenariosBackup;
    public GameStateRandomization randomization;
    public GameStateRandomization.EnemyHealthRandomization enemyHealthRandomization;
    public List<Map.Entry<Integer, GameStateRandomization.Info>> preBattleGameScenariosList;
    public List<BiConsumer<GameState, int[]>> enemiesReordering;

    // Potion related
    public List<Potion> potions;
    public int numOfPotionSlots = 2;
    public Potion.PotionGenerator potionsGenerator;
    public List<Integer> alchemizeCardIdxes;
    public int nonGeneratedPotionsLength;
    public int[] potionsScenarios;
    public double alchemizeMult = 1.25;

    // misc
    public boolean previousCardPlayTracking;
    public List<Relic> startOfBattleActions;
    public List<List<String>> perScenarioCommands;
    public ConcurrentMap<GameState, Tuple<double[], double[]>> biasedCognitionLimitCache = new ConcurrentHashMap<>();
    public boolean biasedCognitionLimitSet;
    public int biasedCognitionLimitUsed;
    public double[] biasedCognitionLimitDistribution;
    public int incenseBurnerRewardType = -1;
    public static boolean isHeartFight(GameState state) {
        return state.currentEncounter == EnemyEncounter.EncounterEnum.CORRUPT_HEART;
    }
    public int inputLen;
    public NNInputSchema nnInputProperties;
    public int extraOutputLen;
    public int v_total_len;
    public boolean cardInDiscardInNNInput;
    public boolean discard0CardOrderMatters;
    public int discardOrderMaxKeepTrackIn10s; // currently, those are sent manually in all or one
    public int discardOrder0CardMaxCopies;
    public int discardOrder0CostNumber;
    public int[] discardOrder0CardReverseIdx;
    public List<EnemyEncounter> enemiesEncounters;
    public Function<GameState, GameState> switchBattleHandler;
    public GameState originalGameState;
    public boolean isHeartGauntlet;
    public int enemiesEncounterChosen;
    public int[] astrolabeCardsTransformed;
    public int[] pandorasBoxCardsTransformed;

    // set in GameStateBuilder: this turns off certain card generation that would significantly bloat network size
    // sometimes the deck is so strong it doesn't matter, or the relative strength of card picks doesn't change
    public final static int GENERATE_CARD_DISCOVERY = 1;
    public final static int GENERATE_CARD_METAMORPHOSIS = 1 << 1;
    public final static int GENERATE_CARD_CHRYSALIS = 1 << 2;
    public final static int GENERATE_CARD_MADNESS = 1 << 3;
    public final static int GENERATE_CARD_ENLIGHTENMENT = 1 << 4;
    public final static int GENERATE_CARD_TRANSMUTATION = 1 << 5;
    public final static int GENERATE_CARD_INFERNAL_BLADE = 1 << 6;
    public final static int GENERATE_CARD_RAMPAGE_UPGRADE = 1 << 7;
    public final static int GENERATE_CARD_ARMANENT = 1 << 8;
    public final static int GENERATE_CARD_APOTHEOSIS = 1 << 9;
    public final static int GENERATE_CARD_SEARING_BLOW_UPGRADE = 1 << 10;
    public final static int GENERATE_CARD_ALL_COLORLESS = GENERATE_CARD_DISCOVERY | GENERATE_CARD_METAMORPHOSIS | GENERATE_CARD_CHRYSALIS | GENERATE_CARD_MADNESS | GENERATE_CARD_ENLIGHTENMENT | GENERATE_CARD_APOTHEOSIS;
    public int generateCardOptions;

    // system card indexes
    public boolean[] healCardsBooleanArr;
    public int[] astrolabeCardsIdxes;
    public int[] cardRewardIdxes;
    public int[] discardIdxes; // cards that can change in number of copies during a fight
    public int[] discardReverseIdxes;
    public int[] select1OutOf3CardsIdxes;
    public int[] select1OutOf3CardsReverseIdxes;
    public int[] tmp0CostCardTransformIdxes;
    public int[] tmpModifiedCardReverseTransformIdxes;
    public int[] upgradeIdxes;
    public int[][] sneckoIdxes;
    // individual cards indexes
    public int[] bloodForBloodPTransformIndexes;
    public int[] bloodForBloodTransformIndexes;
    public int[] clawAfterPlayTransformIndexes;
    public int[] clawPAfterPlayTransformIndexes;
    public int[] clawPTransformIndexes;
    public int[] clawTransformIndexes;
    public int[] forceFieldPTransformIndexes;
    public int[] forceFieldTransformIndexes;
    public int[] glassKnifePTransformIndexes;
    public int[] glassKnifeTransformIndexes;
    public int[] healCardsIdxes;
    public int[] masterfulStabPTransformIndexes;
    public int[] masterfulStabTransformIndexes;
    public int[] momentumStrikeTransformIndexes;
    public int[] nilroysCodexIdxes;
    public int[] pandorasBoxCardsIdxes;
    public int[] perseverancePTransformIndexes;
    public int[] perseveranceTransformIndexes;
    public int[] rampage2TransformIndexes;
    public int[] rampageP2TransformIndexes;
    public int[] rampagePTransformIndexes;
    public int[] rampageTransformIndexes;
    public int[] sandsOfTimePTransformIndexes;
    public int[] sandsOfTimeTransformIndexes;
    public int[] steamBarrierPTransformIndexes;
    public int[] steamBarrierTransformIndexes;
    public int[] moddedTransformIndexes;
    public int[] streamlineIndexes;
    public int[] streamlinePIndexes;
    public int[] strikeCardIdxes;
    public int[] windmillStrikePTransformIndexes;
    public int[] windmillStrikeTransformIndexes;
    public int apotheosisCardIdx = -1;
    public int apotheosisPCardIdx = -1;
    public int echoFormCardIdx = -1;
    public int echoFormPCardIdx = -1;
    public int foresightCardIdx = -1;
    public int gamblingChipsCardIdx = -1;
    public int nilroysCodexHelperCardIdx = -1;
    public int normalityCardIdx = -1;
    public int toolsOfTheTradeCardIdx = -1;
    public int wellLaidPlansCardIdx = -1;

    // cached counter indexes
    public int accuracyCounterIdx = -1;
    public int attacksPlayedThisTurnCounterIdx = -1;
    public int biasedCognitionLimitCounterIdx = -1;
    public int blizzardCounterIdx = -1;
    public int bloodForBloodCounterIdx = -1;
    public int blurCounterIdx = -1;
    public int brillianceCounterIdx = -1;
    public int bufferCounterIdx = -1;
    public int constrictedCounterIdx = -1;
    public int drawReductionCounterIdx = -1;
    public int echoFormCounterIdx = -1;
    public int electrodynamicsCounterIdx = -1;
    public int envenomCounterIdx = -1;
    public int equilibriumCounterIdx = -1;
    public int eviscerateCounterIdx = -1;
    public int exhaustedThisTurnCounterIdx = -1;
    public int feedCounterIdx = -1;
    public int feralCounterIdx = -1;
    public int forceFieldCounterIdx = -1;
    public int foresightCounterIdx = -1;
    public int geneticAlgorithmCounterIdx = -1;
    public int handOfGreedCounterIdx = -1;
    public int happyFlowerCounterIdx = -1;
    public int havocCounterIdx = -1;
    public int hoveringKiteCounterIdx = -1;
    public int incenseBurnerCounterIdx = -1;
    public int inkBottleCounterIdx = -1;
    public int inserterCounterIdx = -1;
    public int intangibleCounterIdx = -1;
    public int iterationCounterIdx = -1;
    public int loopCounterIdx = -1;
    public int loseDexterityPerTurnCounterIdx = -1;
    public int loseEnergyPerTurnCounterIdx = -1;
    public int loseFocusPerTurnCounterIdx = -1;
    public int mantraCounterIdx = -1;
    public int metallicizeCounterIdx = -1;
    public int normalityCounterIdx = -1;
    public int nunchakuCounterIdx = -1;
    public int penNibCounterIdx = -1;
    public int phantasmalKillerCounterIdx = -1;
    public int platingCounterIdx = -1;
    public int playCardOnTopOfDeckCounterIdx = -1;
    public int reboundCounterIdx = -1;
    public int regenerationCounterIdx = -1;
    public int ritualDaggerCounterIdx = -1;
    public int sadisticNatureCounterIdx = -1;
    public int selfRepairCounterIdx = -1;
    public int shieldAndSpireFacingCounterIdx = -1;
    public int sneakyStrikeCounterIdx = -1;
    public int sneckoDebuffCounterIdx = -1;
    public int sundialCounterIdx = -1;
    public int swivelCounterIdx = -1;
    public int synthesisCounterIdx = -1;
    public int thunderDamageCounterIdx = -1;
    public int thunderStrikeCounterIdx = -1;
    public int unrelentingCounterIdx = -1;
    public int timeEaterCounterIdx = -1;
    public int toolsOfTheTradeCounterIdx = -1;
    public int velvetChokerCounterIdx = -1;
    public int wellLaidPlansCounterIdx = -1;
    public int wreathOfFlameCounterIdx = -1;
    public int crueltyCounterIdx = -1;

    public Relic birdFacedUrn = null;
    public Relic blackBlood = null;
    public Relic bloodVial = null;
    public Relic bloodyIdol = null;
    public Relic blueCandle = null;
    public Relic boot = null;
    public Relic burningBlood = null;
    public Relic calipers = null;
    public Relic championBelt = null;
    public Relic chemicalX = null;
    public Relic deadBranch = null;
    public Relic frozenEye = null;
    public Relic ginger = null;
    public Relic goldenEye = null;
    public Relic goldPlatedCable = null;
    public Relic handDrill = null;
    public Relic hoveringKite = null;
    public Relic iceCream = null;
    public Relic magicFlower = null;
    public Relic markOfTheBloom = null;
    public Relic meatOnTheBone = null;
    public Relic medicalKit = null;
    public Relic nilroysCodex = null;
    public Relic oddMushroom = null;
    public Relic paperCrane = null;
    public Relic paperPhrog = null;
    public Relic runicDome = null;
    public Relic runicPyramid = null;
    public Relic sacredBark = null;
    public Relic sneckoEye = null;
    public Relic sneckoSkull = null;
    public Relic strangeSpoon = null;
    public Relic strikeDummy = null;
    public Relic tingsha = null;
    public Relic toolbox = null;
    public Relic torii = null;
    public Relic toughBandages = null;
    public Relic toyOrnithopter = null;
    public Relic tungstenRod = null;
    public Relic turnip = null;
    public Relic unceasingTop = null;
    public Relic violetLotus = null;
    public Relic wristBlade = null;
    public boolean isRunicDomeEnabled(GameState state) {
        return runicDome != null && runicDome.isRelicEnabledInScenario(state);
    }

    // cached V Extra indices for training targets
    public int fightProgressVExtraIdx = -1;
    public int turnsLeftVExtraIdx = -1;
    public int zeroDmgProbVExtraIdx = -1;
    public int alchemizeVExtraIdx = -1;
    public int[] potionsVExtraIdx;

    // ***************************************************************************************************************
    // ************************************************* Main ********************************************************
    // ***************************************************************************************************************

    public void setupEntityProperties(
            java.util.List<com.alphaStS.card.Card> cards,
            java.util.List<Relic> relics,
            java.util.List<Potion> potions,
            java.util.List<? extends com.alphaStS.enemy.EnemyReadOnly> enemies,
            boolean playerHasArtifact) {
        anyEntityProperty = new EntityProperty();
        if (playerHasArtifact) anyEntityProperty.changePlayerArtifact = true;
        for (var c : cards)   anyEntityProperty.mergeFrom(c.entityProperty);
        for (var r : relics)  anyEntityProperty.mergeFrom(r.entityProperty);
        for (var p : potions) anyEntityProperty.mergeFrom(p.entityProperty);
        for (var e : enemies) anyEntityProperty.mergeFrom(e.properties.entityProperty);
    }

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

    public Map<Class<?>, int[]> cardIndexCache = new HashMap<>();

    public int[] findCardIndex(Class<?> cardClass) {
        int[] cached = cardIndexCache.get(cardClass);
        return cached != null ? cached : new int[0];
    }

    // ***************************************************************************************************************
    // *************************** Counters/Training Target/NN Input Registration ************************************
    // ***************************************************************************************************************

    public interface CounterRegistrant {
        void setCounterIdx(GameProperties gameProperties, int idx);
        int getCounterIdx(GameProperties gameProperties);
        default CounterStat getCounterStat() {
            return null;
        }
    }

    public interface TrainingTargetRegistrant {
        void setVExtraIdx(GameProperties properties, int idx);
    }

    public interface NetworkInputHandler {
        int addToInput(GameState state, float[] input, int idx);
        int getInputLenDelta();
        default String getDisplayString(GameState state) { return null; }
        default void onRegister(int counterIdx) { }
    }

    public Map<String, List<CounterRegistrant>> counterRegistrants = new HashMap<>();
    public Map<String, Boolean> persistAcrossBattleCounterRegistrants = new HashMap<>();
    Map<String, Integer> counterIdx = new HashMap<>();
    Map<String, Integer> counterLens = new HashMap<>();
    Map<String, NetworkInputHandler> counterHandlerMap = new HashMap<>();
    Map<String, NetworkInputHandler> nnInputHandlerMap = new HashMap<>();
    NetworkInputHandler[] nnInputHandlers;
    String[] nnInputHandlersName;

    public void addNNInputHandler(String name, NetworkInputHandler handler) {
        nnInputHandlerMap.putIfAbsent(name, handler);
    }

    public void registerCounter(String name, CounterRegistrant registrant, int counterLen, NetworkInputHandler handler, boolean persistAcrossBattle) {
        var registrants = counterRegistrants.computeIfAbsent(name, k -> new ArrayList<>());
        registrants.add(registrant);
        counterLens.putIfAbsent(name, counterLen);
        if (handler != null) {
            counterHandlerMap.putIfAbsent(name, handler);
        }
        if (persistAcrossBattle) {
            persistAcrossBattleCounterRegistrants.putIfAbsent(name, true);
        }
    }

    public void registerCounter(String name, CounterRegistrant registrant, int counterLen, NetworkInputHandler handler) {
        registerCounter(name, registrant, counterLen, handler, false);
    }

    public void registerCounter(String name, CounterRegistrant registrant, NetworkInputHandler handler, boolean persistAcrossBattle) {
        registerCounter(name, registrant, 1, handler, persistAcrossBattle);
    }

    public void registerCounter(String name, CounterRegistrant registrant, NetworkInputHandler handler) {
        registerCounter(name, registrant, 1, handler, false);
    }

    public void registerEnergyNextTurnCounter(GameState state, CounterRegistrant registrant) {
        registerCounter("EnergyNextTurn", registrant, new NetworkInputHandler() {
            @Override public int addToInput(GameState state, float[] input, int idx) {
                input[idx] = state.getCounterForRead()[registrant.getCounterIdx(state.properties)] / 15.0f;
                return idx + 1;
            }

            @Override public int getInputLenDelta() {
                return 1;
            }
        });
        addStartOfTurnHandler("EnergyNextTurn", new GameEventHandler() {
            @Override public void handle(GameState state) {
                int cIdx = registrant.getCounterIdx(state.properties);
                if (state.getCounterForRead()[cIdx] > 0) {
                    state.gainEnergy(state.getCounterForRead()[cIdx]);
                    state.getCounterForWrite()[cIdx] = 0;
                }
            }
        });
    }

    public boolean hasCounter(String name) {
        return counterRegistrants.get(name) != null;
    }

    public int getCounterIdx(String name) {
        return counterIdx.get(name);
    }

    public static final class CounterInfo {
        public String name;
        public int idx;
        public int length;
        public NetworkInputHandler handler;
        public boolean persistAcrossBattle;
    }

    public CounterInfo[] counterInfos;
    public boolean atLeastOneCounterHasNNHandler;
    int counterLength;

    public void compileCounterInfo() {
        var names = counterRegistrants.keySet().stream().sorted().toList();
        counterInfos = new CounterInfo[names.size()];
        counterLength = 0;
        for (int i = 0; i < names.size(); i++) {
            var name = names.get(i);
            counterInfos[i] = new CounterInfo();
            counterInfos[i].name = name;
            counterInfos[i].idx = counterLength;
            counterInfos[i].length = counterLens.get(name);
            counterInfos[i].handler = counterHandlerMap.get(name);
            counterInfos[i].persistAcrossBattle = persistAcrossBattleCounterRegistrants.getOrDefault(name, false);
            for (CounterRegistrant registrant : counterRegistrants.get(name)) {
                registrant.setCounterIdx(this, counterInfos[i].idx);
            }
            counterIdx.put(name, counterInfos[i].idx);
            if (counterInfos[i].handler != null) {
                atLeastOneCounterHasNNHandler = true;
                counterInfos[i].handler.onRegister(counterLength);
            }
            counterLength += counterInfos[i].length;
        }

        nnInputHandlers = new NetworkInputHandler[nnInputHandlerMap.size()];
        names = nnInputHandlerMap.keySet().stream().sorted().toList();
        nnInputHandlersName = names.toArray(new String[] {});
        for (int i = 0; i < nnInputHandlersName.length; i++) {
            nnInputHandlers[i] = nnInputHandlerMap.get(nnInputHandlersName[i]);
            nnInputHandlers[i].onRegister(i);
        }
    }

    Map<String, TrainingTarget> trainingTargetsMap = new HashMap<>();
    Map<String, TrainingTargetRegistrant> trainingTargetsRegistrantMap = new HashMap<>();
    Map<Integer, Tuple<String, Integer>> trainingTargetsRegistrantVIdxMap = new HashMap<>();
    public List<TrainingTarget> extraTrainingTargets = new ArrayList<>();
    public List<String> extraTrainingTargetsLabel = new ArrayList<>();


    public void addExtraTrainingTarget(String targetId, TrainingTargetRegistrant registrant, TrainingTarget target) {
        if (trainingTargetsMap.get(targetId) == null) {
            trainingTargetsMap.put(targetId, target);
            trainingTargetsRegistrantMap.put(targetId, registrant);
        }
    }

    public void compileExtraTrainingTarget() {
        var registrants = trainingTargetsRegistrantMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
        for (int i = 0; i < registrants.size(); i++) {
            registrants.get(i).getValue().setVExtraIdx(this, extraOutputLen);
            trainingTargetsRegistrantVIdxMap.put(extraOutputLen, new Tuple<>(registrants.get(i).getKey(), trainingTargetsMap.get(registrants.get(i).getKey()).getNumberOfTargets()));
            extraOutputLen += trainingTargetsMap.get(registrants.get(i).getKey()).getNumberOfTargets();
            extraTrainingTargets.add(trainingTargetsMap.get(registrants.get(i).getKey()));
            extraTrainingTargetsLabel.add(registrants.get(i).getKey());
        }
    }


    // ***************************************************************************************************************
    // ************************************************** Handlers ***************************************************
    // ***************************************************************************************************************

    // event handlers that cards etc. add their effect in
    public Map<String, Object> gameEventHandlers = new HashMap<>();
    GameEventHandler endOfPreBattleHandler;
    public List<GameEventHandler> startOfBattleHandlers = new ArrayList<>();
    public List<GameEventHandler> endOfBattleHandlers = new ArrayList<>();
    public List<GameEventHandler> preStartOfTurnHandlers = new ArrayList<>();
    public List<GameEventHandler> startOfTurnHandlers = new ArrayList<>();
    public List<GameEventHandler> preEndTurnHandlers = new ArrayList<>();
    public List<GameEventHandler> endOfTurnHandlers = new ArrayList<>();
    public List<GameEventHandler> onExhaustHandlers = new ArrayList<>();
    public List<GameEventHandler> onBlockHandlers = new ArrayList<>();
    public List<GameEventHandler> onShuffleHandlers = new ArrayList<>();
    public List<GameEventHandler> onStanceChangeHandlers = new ArrayList<>();
    public List<GameEventHandler> onScryHandlers = new ArrayList<>();
    public List<GameEventEnemyHandler> onEnemyDeathHandlers = new ArrayList<>();
    public List<GameEventEnemyDebuffHandler> onEnemyDebuffHandlers = new ArrayList<>();
    public List<OnDamageHandler> onDamageHandlers = new ArrayList<>();
    public List<OnDamageHandler> onHealHandlers = new ArrayList<>();
    public List<GameEventCardHandler> onPreCardPlayedHandlers = new ArrayList<>();
    public List<GameEventCardHandler> onCardPlayedHandlers = new ArrayList<>();
    public List<GameEventCardHandler> onCardDrawnHandlers = new ArrayList<>();
    public List<GameEventCardHandler> onRetainHandlers = new ArrayList<>();
    public List<OnCardCreationHandler> onCardCreationHandlers = new ArrayList<>();
    public List<OnEnergySpendHandler> onEnergySpendHandlers = new ArrayList<>();

    private <T> void addHandler(String key, T handler, List<T> handlerList) {
        if (gameEventHandlers.get(key) == null) {
            gameEventHandlers.put(key, handler);
            handlerList.add(handler);
        }
    }

    public void addStartOfBattleHandler(GameEventHandler handler) {
        startOfBattleHandlers.add(handler);
    }

    public void addStartOfBattleHandler(String handlerName, GameEventHandler handler) {
        addHandler(handlerName + "StartOfBattler", handler, startOfBattleHandlers);
    }

    public void addEndOfBattleHandler(GameEventHandler handler) {
        endOfBattleHandlers.add(handler);
    }

    public void addEndOfBattleHandler(String handlerName, GameEventHandler handler) {
        addHandler(handlerName + "EndOfGame", handler, endOfBattleHandlers);
    }

    public void addStartOfTurnHandler(GameEventHandler handler) {
        startOfTurnHandlers.add(handler);
    }

    public void addStartOfTurnHandler(String handlerName, GameEventHandler handler) {
        addHandler(handlerName + "StartOfTurn", handler, startOfTurnHandlers);
    }

    public void addPreStartOfTurnHandler(GameEventHandler handler) {
        preStartOfTurnHandlers.add(handler);
    }

    public void addPreStartOfTurnHandler(String handlerName, GameEventHandler handler) {
        addHandler(handlerName + "PreStartOfTurn", handler, preStartOfTurnHandlers);
    }

    public void addPreEndOfTurnHandler(GameEventHandler handler) {
        preEndTurnHandlers.add(handler);
    }

    public void addPreEndOfTurnHandler(String handlerName, GameEventHandler handler) {
        addHandler(handlerName + "PreEndOfTurn", handler, preEndTurnHandlers);
    }

    public void addEndOfTurnHandler(GameEventHandler handler) {
        endOfTurnHandlers.add(handler);
    }

    public void addEndOfTurnHandler(String handlerName, GameEventHandler handler) {
        addHandler(handlerName + "EndOfTurn", handler, endOfTurnHandlers);
    }

    public void addOnExhaustHandler(GameEventHandler handler) {
        onExhaustHandlers.add(handler);
    }

    public void addOnExhaustHandler(String handlerName, GameEventHandler handler) {
        addHandler(handlerName + "OnExhaust", handler, onExhaustHandlers);
    }

    public void addOnBlockHandler(GameEventHandler handler) {
        onBlockHandlers.add(handler);
    }

    public void addOnBlockHandler(String handlerName, GameEventHandler handler) {
        addHandler(handlerName + "OnBlock", handler, onBlockHandlers);
    }

    public void addOnStanceChangeHandler(GameEventHandler handler) {
        onStanceChangeHandlers.add(handler);
    }

    public void addOnStanceChangeHandler(String handlerName, GameEventHandler handler) {
        addHandler(handlerName + "OnStanceChange", handler, onStanceChangeHandlers);
    }

    public void addOnScryHandler(GameEventHandler handler) {
        onScryHandlers.add(handler);
    }

    public void addOnScryHandler(String handlerName, GameEventHandler handler) {
        addHandler(handlerName + "OnScry", handler, onScryHandlers);
    }

    public void addOnEnemyDeathHandler(GameEventEnemyHandler handler) {
        onEnemyDeathHandlers.add(handler);
    }

    public void addOnEnemyDeathHandler(String handlerName, GameEventEnemyHandler handler) {
        addHandler(handlerName + "OnEnemyDeath", handler, onEnemyDeathHandlers);
    }

    public void addOnEnemyDebuffHandler(String handlerName, GameEventEnemyDebuffHandler handler) {
        addHandler(handlerName + "OnEnemyDebuff", handler, onEnemyDebuffHandlers);
    }

    public void addOnDamageHandler(OnDamageHandler handler) {
        onDamageHandlers.add(handler);
    }

    public void addOnDamageHandler(String handlerName, OnDamageHandler handler) {
        addHandler(handlerName + "OnDamage", handler, onDamageHandlers);
    }

    public void addOnHealHandler(OnDamageHandler handler) {
        onHealHandlers.add(handler);
    }

    public void addOnHealHandler(String handlerName, OnDamageHandler handler) {
        addHandler(handlerName + "OnHeal", handler, onHealHandlers);
    }

    public void addOnPreCardPlayedHandler(GameEventCardHandler handler) {
        onPreCardPlayedHandlers.add(handler);
    }

    public void addOnPreCardPlayedHandler(String handlerName, GameEventCardHandler handler) {
        addHandler(handlerName + "OnPreCardPlayed", handler, onPreCardPlayedHandlers);
    }

    public void addOnCardPlayedHandler(GameEventCardHandler handler) {
        onCardPlayedHandlers.add(handler);
    }

    public void addOnCardPlayedHandler(String handlerName, GameEventCardHandler handler) {
        addHandler(handlerName + "OnCardPlayed", handler, onCardPlayedHandlers);
    }

    public void addOnCardDrawnHandler(GameEventCardHandler handler) {
        onCardDrawnHandlers.add(handler);
    }

    public void addOnCardDrawnHandler(String handlerName, GameEventCardHandler handler) {
        addHandler(handlerName + "OnCardDrawn", handler, onCardDrawnHandlers);
    }

    public void addOnRetainHandler(GameEventCardHandler handler) {
        onRetainHandlers.add(handler);
    }

    public void addOnRetainHandler(String handlerName, GameEventCardHandler handler) {
        addHandler(handlerName + "OnRetain", handler, onRetainHandlers);
    }

    public void addOnCardCreationHandler(String handlerName, OnCardCreationHandler handler) {
        addHandler(handlerName + "OnCardCreation", handler, onCardCreationHandlers);
    }

    public void addOnEnergySpendHandler(String handlerName, OnEnergySpendHandler handler) {
        addHandler(handlerName + "OnEnergySpend", handler, onEnergySpendHandlers);
    }

    public void addOnShuffleHandler(GameEventHandler handler) {
        onShuffleHandlers.add(handler);
    }

    public void addOnShuffleHandler(String handlerName, GameEventHandler handler) {
        addHandler(handlerName + "OnShuffle", handler, onShuffleHandlers);
    }

    // ********************************************************************************************************************
    // ******************************** Shared Effects Registration *******************************************************
    // ********************************************************************************************************************

    private static CounterRegistrant ExhaustedThisTurnCounterRegistrant = new CounterRegistrant() {
        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            gameProperties.exhaustedThisTurnCounterIdx = idx;
        }
        @Override public int getCounterIdx(GameProperties gameProperties) {
            return gameProperties.exhaustedThisTurnCounterIdx;
        }
    };

    private static CounterRegistrant IntangibleCounterRegistrant = new CounterRegistrant() {
        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            gameProperties.intangibleCounterIdx = idx;
        }
        @Override public int getCounterIdx(GameProperties gameProperties) {
            return gameProperties.intangibleCounterIdx;
        }
    };

    private static CounterRegistrant PlayCardOnTopOfDeckCounterRegistrant = new CounterRegistrant() {
        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            gameProperties.playCardOnTopOfDeckCounterIdx = idx;
        }
        @Override public int getCounterIdx(GameProperties gameProperties) {
            return gameProperties.playCardOnTopOfDeckCounterIdx;
        }
    };

    private static CounterRegistrant MantraCounterRegistrant = new CounterRegistrant() {
        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            gameProperties.mantraCounterIdx = idx;
        }
        @Override public int getCounterIdx(GameProperties gameProperties) {
            return gameProperties.mantraCounterIdx;
        }
    };

    private static CounterRegistrant MetallicizeCounterRegistrant = new CounterRegistrant() {
        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            gameProperties.metallicizeCounterIdx = idx;
        }
        @Override public int getCounterIdx(GameProperties gameProperties) {
            return gameProperties.metallicizeCounterIdx;
        }
    };

    private static CounterRegistrant PlatingCounterRegistrant = new CounterRegistrant() {
        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            gameProperties.platingCounterIdx = idx;
        }
        @Override public int getCounterIdx(GameProperties gameProperties) {
            return gameProperties.platingCounterIdx;
        }
    };

    private static CounterRegistrant AttacksPlayedThisTurnCounterRegistrant = new CounterRegistrant() {
        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            gameProperties.attacksPlayedThisTurnCounterIdx = idx;
        }
        @Override public int getCounterIdx(GameProperties gameProperties) {
            return gameProperties.attacksPlayedThisTurnCounterIdx;
        }
    };

    public void registerBufferCounter(GameState state, CounterRegistrant registrant) {
        state.properties.registerCounter("Buffer", registrant, new GameProperties.NetworkInputHandler() {
            @Override public int addToInput(GameState state, float[] input, int idx) {
                input[idx] = state.getCounterForRead()[state.properties.bufferCounterIdx] / 10.0f;
                return idx + 1;
            }
            @Override public int getInputLenDelta() {
                return 1;
            }
            @Override public void onRegister(int counterIdx) {
                state.properties.bufferCounterIdx = counterIdx;
            }
        });
    }

    public void registerExhaustedThisTurnCounter() {
        registerCounter("ExhaustedThisTurn", ExhaustedThisTurnCounterRegistrant, new NetworkInputHandler() {
            @Override public int addToInput(GameState state, float[] input, int idx) {
                input[idx] = state.getCounterForRead()[state.properties.exhaustedThisTurnCounterIdx] > 0 ? 1.0f : 0.0f;
                return idx + 1;
            }

            @Override public int getInputLenDelta() {
                return 1;
            }
        });
        addOnExhaustHandler("ExhaustedThisTurn", new GameEventHandler() {
            @Override public void handle(GameState state) {
                state.getCounterForWrite()[state.properties.exhaustedThisTurnCounterIdx] = 1;
            }
        });
        addStartOfTurnHandler("ExhaustedThisTurn", new GameEventHandler() {
            @Override public void handle(GameState state) {
                state.getCounterForWrite()[state.properties.exhaustedThisTurnCounterIdx] = 0;
            }
        });
    }

    public void registerIntangibleCounter() {
        registerCounter("Intangible", IntangibleCounterRegistrant, new NetworkInputHandler() {
            @Override public int addToInput(GameState state, float[] input, int idx) {
                input[idx] = idx / 8.0f;
                return idx + 1;
            }
            @Override public int getInputLenDelta() {
                return 1;
            }
        });
        addEndOfTurnHandler("Intangible", new GameEventHandler() {
            @Override public void handle(GameState state) {
                if (state.getCounterForRead()[state.properties.intangibleCounterIdx] > 0) {
                    state.getCounterForWrite()[state.properties.intangibleCounterIdx]--;
                }
            }
        });
    }

    public void registerPlayCardOnTopOfDeckCounter() {
        registerCounter("PlayCardOnTopOfDeck", PlayCardOnTopOfDeckCounterRegistrant, new NetworkInputHandler() {
            @Override public int addToInput(GameState state, float[] input, int idx) {
                input[idx] = state.getCounterForRead()[state.properties.playCardOnTopOfDeckCounterIdx] / 6.0f;
                return idx + 1;
            }
            @Override public int getInputLenDelta() {
                return 1;
            }
        });
    }

    public void registerMantraCounter() {
        registerCounter("Mantra", MantraCounterRegistrant, new NetworkInputHandler() {
            @Override public int addToInput(GameState state, float[] input, int idx) {
                input[idx] = state.getCounterForRead()[state.properties.mantraCounterIdx] / 10.0f;
                return idx + 1;
            }
            @Override public int getInputLenDelta() {
                return 1;
            }
        });
    }

    public void registerThornCounter(GameState state2, CounterRegistrant registrant) {
        state2.properties.registerCounter("Thorn", registrant, null);
        state2.properties.addOnDamageHandler("Thorn", new OnDamageHandler() {
            @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                if (isAttack && source instanceof EnemyReadOnly enemy2) {
                    var idx = state.getEnemiesForRead().find(enemy2);
                    var enemy = state.getEnemiesForWrite().getForWrite(idx);
                    state.playerDoNonAttackDamageToEnemy(enemy, state.getCounterForRead()[registrant.getCounterIdx(state.properties)], true);
                }
            }
        });
    }

    public void registerMetallicizeCounter() {
        registerCounter("Metallicize", MetallicizeCounterRegistrant, new NetworkInputHandler() {
            @Override public int addToInput(GameState state, float[] input, int idx) {
                input[idx] = state.getCounterForRead()[state.properties.metallicizeCounterIdx] / 10.0f;
                return idx + 1;
            }
            @Override public int getInputLenDelta() {
                return 1;
            }
        });
        addPreEndOfTurnHandler("Metallicize", new GameEventHandler() {
            @Override public void handle(GameState state) {
                state.getPlayerForWrite().gainBlockNotFromCardPlay(state.getCounterForRead()[state.properties.metallicizeCounterIdx]);
            }
        });
    }

    public void registerPlatingCounter() {
        registerCounter("Plating", PlatingCounterRegistrant, new NetworkInputHandler() {
            @Override public int addToInput(GameState state, float[] input, int idx) {
                input[idx] = state.getCounterForRead()[state.properties.platingCounterIdx] / 10.0f;
                return idx + 1;
            }
            @Override public int getInputLenDelta() {
                return 1;
            }
        });
        addPreEndOfTurnHandler("Plating", new GameEventHandler() {
            @Override public void handle(GameState state) {
                state.getPlayerForWrite().gainBlockNotFromCardPlay(state.getCounterForRead()[state.properties.platingCounterIdx]);
            }
        });
        addEndOfTurnHandler("Plating", new GameEventHandler() {
            @Override public void handle(GameState state) {
                if (state.getCounterForRead()[state.properties.platingCounterIdx] > 0) {
                    state.getCounterForWrite()[state.properties.platingCounterIdx]--;
                }
            }
        });
    }

    public void registerAttacksPlayedThisTurnCounter() {
        registerCounter("AttacksPlayedThisTurn", AttacksPlayedThisTurnCounterRegistrant, new NetworkInputHandler() {
            @Override public int addToInput(GameState state, float[] input, int idx) {
                input[idx] = state.getCounterForRead()[state.properties.attacksPlayedThisTurnCounterIdx] / 3.0f;
                return idx + 1;
            }

            @Override public int getInputLenDelta() {
                return 1;
            }
        });
        addOnCardPlayedHandler("AttacksPlayedThisTurn", new GameEventCardHandler() {
            @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                    state.getCounterForWrite()[state.properties.attacksPlayedThisTurnCounterIdx]++;
                }
            }
        });
        addStartOfTurnHandler("AttacksPlayedThisTurn", new GameEventHandler() {
            @Override public void handle(GameState state) {
                state.getCounterForWrite()[state.properties.attacksPlayedThisTurnCounterIdx] = 0;
            }
        });
    }

    public void registerSneckoDebuffCounter() {
        registerCounter("Snecko", new GameProperties.CounterRegistrant() {
            @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
                gameProperties.sneckoDebuffCounterIdx = idx;
            }
            @Override public int getCounterIdx(GameProperties gameProperties) {
                return gameProperties.sneckoDebuffCounterIdx;
            }
        }, new GameProperties.NetworkInputHandler() {
            @Override public int addToInput(GameState state, float[] input, int idx) {
                input[idx] = state.getCounterForRead()[state.properties.sneckoDebuffCounterIdx] > 0 ? 0.5f : 0.0f;
                return idx + 1;
            }

            @Override public int getInputLenDelta() {
                return 1;
            }
        });
    }

    public static List<Card> generateSneckoCards(List<Card> cards) {
        var newCards = new ArrayList<Card>();
        cards.stream().filter((x) -> !x.isXCost && x.energyCost >= 0 && !(x instanceof Card.CardWrapper)).forEach((x) -> {
            for (int i = 0; i < 4; i++) {
                if (x.energyCost == i) {
                    continue;
                }
                newCards.add(x.getPermCostIfPossible(i));
            }
        });
        return newCards;
    }

    // todo
    public void setupSneckoIndexes() {
        sneckoIdxes = new int[cardDict.length][];
        var m = new HashMap<String, int[]>();
        for (int i = 0; i < cardDict.length; i++) {
            var card = cardDict[i];
            if (!(card instanceof Card.CardWrapper)) {
                if (cardCanGenerateSneckoEnergyCost(card) || card.energyCost > 3) {
                    var a = new int[] { 0, -1, -1, -1, -1 };
                    m.put(getCanonicalCardName(card), a);
                    sneckoIdxes[i] = a;
                } else {
                    var a = new int[] { 1, i, -1, -1, -1 };
                    m.put(card.cardName, a);
                    sneckoIdxes[i] = a;
                }
            }
        }
        for (int i = 0; i < cardDict.length; i++) {
            var card = cardDict[i];
            if (card instanceof Card.CardWrapper c && (c.isPermChangeCost() && !c.isTmpChangeCost()) && card.energyCost < 4) {
                var a = m.get(c.getBaseCard().cardName);
                a[++a[0]] = i;
                sneckoIdxes[i] = a;
            } else if (cardCanGenerateSneckoEnergyCost(card) && card.energyCost < 4) {
                var a = m.get(getCanonicalCardName(card));
                a[++a[0]] = i;
                sneckoIdxes[i] = a;
            }
        }
        for (int i = 0; i < cardDict.length; i++) {
            var card = cardDict[i];
            if (card instanceof Card.CardWrapper c && c.isTmpChangeCost()) {
                if (c.getBaseCard() instanceof Card.CardWrapper cc && cc.isPermChangeCost()) {
                    sneckoIdxes[i] = m.get(cc.getBaseCard().cardName);
                } else if (cardCanGenerateSneckoEnergyCost(card)) {
                    sneckoIdxes[i] = m.get(getCanonicalCardName(card));
                } else {
                    sneckoIdxes[i] = m.get(c.getBaseCard().cardName);
                }
            } else if (cardCanGenerateSneckoEnergyCost(card) || card.energyCost > 3) {
                sneckoIdxes[i] = m.get(getCanonicalCardName(card));
            }
        }
    }

    private static boolean cardCanGenerateSneckoEnergyCost(Card card) {
        return card.cardName.startsWith("Blood For Blood") || card.cardName.startsWith("Force Field") || card.cardName.startsWith("Streamline");
    }

    private static String getCanonicalCardName(Card card) {
        if (card.cardName.contains(" (")) {
            return card.cardName.substring(0, card.cardName.indexOf(" ("));
        }
        return card.cardName;
    }
}
