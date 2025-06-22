package com.alphaStS;

import com.alphaStS.card.Card;
import com.alphaStS.card.CardDefect;
import com.alphaStS.enemy.EnemyEncounter;
import com.alphaStS.enemy.EnemyList;
import com.alphaStS.enemy.EnemyReadOnly;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.utils.CounterStat;
import com.alphaStS.utils.Tuple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class GameProperties implements Cloneable {
    public boolean testNewFeature = true;
    public boolean multithreadedMTCS;
    public boolean doingComparison;
    public boolean curriculumTraining;
    public boolean isTraining;
    public MCTS currentMCTS;
    public ConcurrentMap<GameState, Tuple<double[], double[]>> biasedCognitionLimitCache = new ConcurrentHashMap<>();
    public boolean biasedCognitionLimitSet;
    public int biasedCognitionLimitUsed;
    public double[] biasedCognitionLimitDistribution;
    public boolean playerArtifactCanChange;
    public boolean playerStrengthCanChange;
    public boolean playerDexterityCanChange;
    public boolean playerStrengthEotCanChange;
    public boolean playerDexterityEotCanChange;
    public boolean playerFocusCanChange;
    public boolean playerPlatedArmorCanChange;
    public boolean playerCanGetVuln;
    public boolean playerCanGetWeakened;
    public boolean playerCanGetFrailed;
    public boolean playerCanGetEntangled;
    public boolean previousCardPlayTracking;
    public boolean enemyCanGetVuln;
    public boolean enemyCanGetWeakened;
    public boolean enemyCanGetChoked;
    public boolean enemyCanGetLockOn;
    public boolean enemyCanGetTalkToTheHand;
    public boolean enemyCanGetMark;
    public boolean enemyStrengthCanChange;
    public boolean enemyStrengthEotCanChange;
    public boolean enemyCanGetPoisoned;
    public boolean enemyCanGetCorpseExplosion;
    public long possibleBuffs;
    public boolean needDeckOrderMemory;
    public boolean selectFromExhaust;
    public RandomGen random;
    public RandomGen realMoveRandomGen;
    public boolean makingRealMove;
    public boolean isInteractive;
    public boolean stateDescOn;
    public Card[] cardDict;
    public List<Potion> potions;
    public Potion.PotionGenerator potionsGenerator;
    public List<Integer> alchemizeCardIdxes;
    public int numOfPotionSlots = 2;
    public int nonGeneratedPotionsLength;
    public int[] potionsVArrayIdx;
    public int[] potionsScenarios;
    public EnemyList originalEnemies;
    public int maxNumOfActions;
    public int totalNumOfActions;
    public GameAction[][] actionsByCtx;
    public List<Relic> startOfBattleActions;
    public List<List<String>> perScenarioCommands;
    public CharacterEnum character;

    // cached card indexes
    public int realCardsLen; // not including CardTmpCostChange (due to liquid memory)
    public int[] discardIdxes; // cards that can change in number of copies during a fight
    public int[] discardReverseIdxes;
    public int[] upgradeIdxes;
    public int[] tmp0CostCardTransformIdxes;
    public int[] tmp0CostCardReverseTransformIdxes;
    public int[] tmpRetainCardTransformIdxes;
    public int[] tmpRetainCardReverseTransformIdxes;
    public int[] select1OutOf3CardsIdxes;
    public int[] select1OutOf3CardsReverseIdxes;
    public int[] nilrysCodexIdxes;
    public int[] colorlessTmp0Idxes;
    public int[] colorlessUpgradedTmp0Idxes;
    public int[] characterSkillPerm0Idxes;
    public int[] characterAttackPerm0Idxes;
    public int[] cardRewardIdxes;
    public int[] astrolabeCardsIdxes;
    public int[] pandorasBoxCardsIdxes;
    public int[][] sneckoIdxes;
    public int angerCardIdx = -1;
    public int angerPCardIdx = -1;
    public int[] strikeCardIdxes;
    public int[] colorlessCardIdxes;
    // cached status indexes
    public int burnCardIdx = -1;
    public int burnPCardIdx = -1;
    public int dazedCardIdx = -1;
    public int slimeCardIdx = -1;
    public int normalityCardIdx = -1;
    public int woundCardIdx = -1;
    public int voidCardIdx = -1;
    public int shivCardIdx = -1;
    public int shivPCardIdx = -1;
    public int smiteCardIdx = -1;
    public int insightCardIdx = -1;
    public int safetyCardIdx = -1;
    public int throughViolenceCardIdx = -1;
    public int echoFormCardIdx = -1;
    public int echoFormPCardIdx = -1;
    public int apotheosisCardIdx = -1;
    public int apotheosisPCardIdx = -1;
    public int miracleCardIdx = -1;
    public int miraclePCardIdx = -1;
    public int betaCardIdx = -1;
    public int omegaCardIdx = -1;
    public int wellLaidPlansCardIdx = -1;
    public int gamblingChipsCardIdx = -1;
    public int toolsOfTheTradeCardIdx = -1;
    public int foresightCardIdx = -1;
    public int[] sandsOfTimeTransformIndexes;
    public int[] sandsOfTimePTransformIndexes;
    public int[] windmillStrikeTransformIndexes;
    public int[] windmillStrikePTransformIndexes;
    public int[] perseveranceTransformIndexes;
    public int[] perseverancePTransformIndexes;
    public int[] conjureBladeIndexes;
    public int[] masterfulStabTransformIndexes;
    public int[] masterfulStabPTransformIndexes;
    public int[] setUpCardIdxes;
    public int[] streamlineIndexes;
    public int[] streamlinePIndexes;
    public int[] clawIndexes;
    public int[] clawTransformIndexes;
    public int[] clawPIndexes;
    public int[] clawPTransformIndexes;
    public int[] rampageIndexes;
    public int[] rampagePIndexes;
    public int[] glassKnifeIndexes;
    public int[] glassKnifePIndexes;
    public int[] steamBarrierIndexes;
    public int[] steamBarrierPIndexes;
    public int[] infernalBladeIndexes;
    public int[] distractionIndexes;
    public int[] healCardsIdxes;
    public boolean[] healCardsBooleanArr;
    public List<TrainingTarget> extraTrainingTargets = new ArrayList<>();
    public List<String> extraTrainingTargetsLabel = new ArrayList<>();
    public int ritualDaggerCounterIdx = -1;
    public int feedCounterIdx = -1;
    public int handOfGreedCounterIdx = -1;
    public int regenerationCounterIdx = -1;
    public int nunchakuCounterIdx = -1;
    public int penNibCounterIdx = -1;
    public int inkBottleCounterIdx = -1;
    public int happyFlowerCounterIdx = -1;
    public int velvetChokerCounterIndexIdx = -1;
    public int incenseBurnerCounterIdx = -1;
    public int incenseBurnerRewardType = -1;
    public int echoFormCounterIdx = -1;
    public int selfRepairCounterIdx = -1;
    public int equilibriumCounterIdx = -1;
    public int blizzardCounterIdx = -1;
    public int thunderStrikeCounterIdx = -1;
    public int intangibleCounterIdx = -1;
    public int metallicizeCounterIdx = -1;
    public int sneakyStrikeCounterIdx = -1;
    public int eviscerateCounterIdx = -1;
    public int wellLaidPlansCounterIdx = -1;
    public int hoveringKiteCounterIdx = -1;
    public int blurCounterIdx = -1;
    public int accuracyCounterIdx = -1;
    public int phantasmalKillerCounterIdx = -1;
    public int reboundCounterIdx = -1;
    public int sneckoDebuffCounterIdx = -1;
    public int envenomCounterIdx = -1;
    public int geneticAlgorithmCounterIdx = -1;
    public int bufferCounterIdx = -1;
    public int sundialCounterIdx = -1;
    public int sadisticNatureCounterIdx = -1;
    public int inserterCounterIdx = -1;
    public int loopCounterIdx = -1;
    public int forceFieldCounterIdx = -1;
    public int toolsOfTheTradeCounterIdx = -1;
    public int foresightCounterIdx = -1;
    public int electrodynamicsCounterIdx = -1;
    public int biasedCognitionLimitCounterIdx = -1;
    public int mantraCounterIdx = -1;
    public int brillianceCounterIdx = -1;
    public int wreathOfFlameCounterIdx = -1;
    public int swivelCounterIdx = -1;
    public int looterVArrayIdx = -1;
    public int writhingMassVIdx = -1;
    public int alchemizeVIdx = -1;

    public boolean hasBlueCandle;
    public boolean hasBoot;
    public boolean hasToyOrniphopter;
    public boolean hasCaliper;
    public boolean hasGinger;
    public boolean hasIceCream;
    public boolean hasFrozenEye;
    public boolean hasMedicalKit;
    public boolean hasVioletLotus;
    public boolean hasGoldenEye;
    public boolean hasOddMushroom;
    public boolean hasRunicDome;
    public boolean isRunicDomeEnabled(GameState state) {
        return hasRunicDome && getRelic(Relic.RunicDome.class).isRelicEnabledInScenario(state.preBattleScenariosChosenIdx);
    }
    public boolean hasRunicPyramid;
    public boolean hasSacredBark;
    public boolean hasStrangeSpoon;
    public boolean hasToolbox;
    public boolean hasTurnip;
    public boolean hasUnceasingTop;
    public boolean hasTorri;
    public boolean hasTungstenRod;
    public boolean hasChampionBelt;
    public boolean hasPaperPhrog;
    public boolean hasMeatOnBone;
    public boolean hasBirdFacedUrn;
    public boolean hasBurningBlood;
    public boolean hasBloodyIdol;
    public boolean hasBloodVial;
    public boolean hasPaperCrane;
    public boolean hasSneckoSkull;
    public boolean hasSneckoEye;
    public boolean hasStrikeDummy;
    public boolean hasTingsha;
    public boolean hasToughBandages;
    public boolean hasGoldPlatedCable;
    public boolean hasChemicalX;
    public boolean hasHandDrill;
    public boolean hasMarkOfTheBloom;
    public boolean hasDeadBranch;

    public int loseDexterityPerTurnCounterIdx;
    public int loseFocusPerTurnCounterIdx = -1;
    public int loseEnergyPerTurnCounterIdx = -1;
    public int constrictedCounterIdx = -1;
    public int drawReductionCounterIdx = -1;
    public int timeEaterCounterIdx = -1;
    public int normalityCounterIdx = -1;
    public int playCardOnTopOfDeckCounterIdx = -1;

    // cached game properties for generating NN input
    public int shieldAndSpireFacingIdx = -1;
    public boolean battleTranceExist;
    public boolean energyRefillCanChange;

    public static boolean isHeartFight(GameState state) {
        return state.currentEncounter == EnemyEncounter.EncounterEnum.CORRUPT_HEART;
    }

    public int inputLen;
    public int extraOutputLen;
    public int v_total_len;
    public int v_real_len;
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
    public double alchemizeMult = 1.25;

    // relics/cards can add checks like e.g. Burn checking if it's in hand pre end of turn
    public Map<String, Object> gameEventHandlers = new HashMap<>();
    public List<Relic> relics;
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
    public List<OnDamageHandler> onDamageHandlers = new ArrayList<>();
    public List<OnDamageHandler> onHealHandlers = new ArrayList<>();
    public List<GameEventCardHandler> onPreCardPlayedHandlers = new ArrayList<>();
    public List<GameEventCardHandler> onCardPlayedHandlers = new ArrayList<>();
    public List<GameEventCardHandler> onCardDrawnHandlers = new ArrayList<>();
    public List<GameEventCardHandler> onRetainHandlers = new ArrayList<>();
    public GameStateRandomization randomization;
    public GameStateRandomization preBattleRandomization;
    public GameStateRandomization preBattleScenarios;
    public GameStateRandomization preBattleScenariosBackup;
    public GameStateRandomization.EnemyHealthRandomization enemyHealthRandomization;
    public List<Map.Entry<Integer, GameStateRandomization.Info>> preBattleGameScenariosList;
    public List<BiConsumer<GameState, int[]>> enemiesReordering;

    // defect specific
    public int maxNumOfOrbs;

    public double cpuct = 0.1;
    public int fightProgressVIdx;
    public int turnsLeftVIdx = -1;
    public int zeroDmgProbVIdx = -1;
    public float maxPossibleRealTurnsLeft = 50.0f;
    public int qwinVIdx = -1;

    public int difficultyChosen;

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

    public Map<String, Integer> cardIndexCache = new HashMap<>();

    public int findCardIndex(String cardName) {
        Integer v = cardIndexCache.get(cardName);
        return v == null ? -1 : v;
    }

    public void findCardIndex(int[] idxes, String... cardNames) {
        for (int i = 0; i < cardNames.length; i++) {
            Integer v = cardIndexCache.get(cardNames[i]);
            idxes[i] = v == null ? -1 : v;
        }
    }

    public Relic getRelic(Class relicClass) {
        for (int i = 0; i < relics.size(); i++) {
            if (relics.get(i).getClass() == relicClass) {
                return relics.get(i);
            }
        }
        return null;
    }

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

    public void addStartOfBattleHandler(GameEventHandler handler) {
        startOfBattleHandlers.add(handler);
    }

    public void addStartOfBattleHandler(String handlerName, GameEventHandler handler) {
        if (gameEventHandlers.get(handlerName + "StartOfBattler") == null) {
            gameEventHandlers.put(handlerName + "StartOfBattler", handler);
            startOfBattleHandlers.add(handler);
        }
    }

    public void addEndOfBattleHandler(GameEventHandler handler) {
        endOfBattleHandlers.add(handler);
    }

    public void addEndOfBattleHandler(String handlerName, GameEventHandler handler) {
        if (gameEventHandlers.get(handlerName + "EndOfGame") == null) {
            gameEventHandlers.put(handlerName + "EndOfGame", handler);
            endOfBattleHandlers.add(handler);
        }
    }

    public void addStartOfTurnHandler(GameEventHandler handler) {
        startOfTurnHandlers.add(handler);
    }

    public void addStartOfTurnHandler(String handlerName, GameEventHandler handler) {
        if (gameEventHandlers.get(handlerName + "StartOfTurn") == null) {
            gameEventHandlers.put(handlerName + "StartOfTurn", handler);
            startOfTurnHandlers.add(handler);
        }
    }

    public void addPreStartOfTurnHandler(GameEventHandler handler) {
        preStartOfTurnHandlers.add(handler);
    }

    public void addPreStartOfTurnHandler(String handlerName, GameEventHandler handler) {
        if (gameEventHandlers.get(handlerName + "PreStartOfTurn") == null) {
            gameEventHandlers.put(handlerName + "PreStartOfTurn", handler);
            preStartOfTurnHandlers.add(handler);
        }
    }

    public void addPreEndOfTurnHandler(GameEventHandler handler) {
        preEndTurnHandlers.add(handler);
    }

    public void addPreEndOfTurnHandler(String handlerName, GameEventHandler handler) {
        if (gameEventHandlers.get(handlerName + "PreEndOfTurn") == null) {
            gameEventHandlers.put(handlerName + "PreEndOfTurn", handler);
            preEndTurnHandlers.add(handler);
        }
    }

    public void addEndOfTurnHandler(GameEventHandler handler) {
        endOfTurnHandlers.add(handler);
    }

    public void addEndOfTurnHandler(String handlerName, GameEventHandler handler) {
        if (gameEventHandlers.get(handlerName + "EndOfTurn") == null) {
            gameEventHandlers.put(handlerName + "EndOfTurn", handler);
            endOfTurnHandlers.add(handler);
        }
    }

    public void addOnExhaustHandler(GameEventHandler handler) {
        onExhaustHandlers.add(handler);
    }

    public void addOnExhaustHandler(String handlerName, GameEventHandler handler) {
        if (gameEventHandlers.get(handlerName + "OnExhaust") == null) {
            gameEventHandlers.put(handlerName + "OnExhaust", handler);
            onExhaustHandlers.add(handler);
        }
    }

    public void addOnBlockHandler(GameEventHandler handler) {
        onBlockHandlers.add(handler);
    }

    public void addOnBlockHandler(String handlerName, GameEventHandler handler) {
        if (gameEventHandlers.get(handlerName + "OnBlock") == null) {
            gameEventHandlers.put(handlerName + "OnBlock", handler);
            onBlockHandlers.add(handler);
        }
    }

    public void addOnStanceChangeHandler(GameEventHandler handler) {
        onStanceChangeHandlers.add(handler);
    }

    public void addOnStanceChangeHandler(String handlerName, GameEventHandler handler) {
        if (gameEventHandlers.get(handlerName + "OnStanceChange") == null) {
            gameEventHandlers.put(handlerName + "OnStanceChange", handler);
            onStanceChangeHandlers.add(handler);
        }
    }

    public void addOnScryHandler(GameEventHandler handler) {
        onScryHandlers.add(handler);
    }

    public void addOnScryHandler(String handlerName, GameEventHandler handler) {
        if (gameEventHandlers.get(handlerName + "OnScry") == null) {
            gameEventHandlers.put(handlerName + "OnScry", handler);
            onScryHandlers.add(handler);
        }
    }

    public void addOnEnemyDeathHandler(GameEventEnemyHandler handler) {
        onEnemyDeathHandlers.add(handler);
    }

    public void addOnEnemyDeathHandler(String handlerName, GameEventEnemyHandler handler) {
        if (gameEventHandlers.get(handlerName + "OnEnemyDeath") == null) {
            gameEventHandlers.put(handlerName + "OnEnemyDeath", handler);
            onEnemyDeathHandlers.add(handler);
        }
    }


    public void addOnDamageHandler(OnDamageHandler handler) {
        onDamageHandlers.add(handler);
    }

    public void addOnDamageHandler(String handlerName, OnDamageHandler handler) {
        if (gameEventHandlers.get(handlerName + "OnDamage") == null) {
            gameEventHandlers.put(handlerName + "OnDamage", handler);
            onDamageHandlers.add(handler);
        }
    }

    public void addOnHealHandler(OnDamageHandler handler) {
        onHealHandlers.add(handler);
    }

    public void addOnHealHandler(String handlerName, OnDamageHandler handler) {
        if (gameEventHandlers.get(handlerName + "OnHeal") == null) {
            gameEventHandlers.put(handlerName + "OnHeal", handler);
            onHealHandlers.add(handler);
        }
    }

    public void addOnPreCardPlayedHandler(GameEventCardHandler handler) {
        onPreCardPlayedHandlers.add(handler);
    }

    public void addOnPreCardPlayedHandler(String handlerName, GameEventCardHandler handler) {
        if (gameEventHandlers.get(handlerName + "OnPreCardPlayed") == null) {
            gameEventHandlers.put(handlerName + "OnPreCardPlayed", handler);
            onPreCardPlayedHandlers.add(handler);
        }
    }

    public void addOnCardPlayedHandler(GameEventCardHandler handler) {
        onCardPlayedHandlers.add(handler);
    }

    public void addOnCardPlayedHandler(String handlerName, GameEventCardHandler handler) {
        if (gameEventHandlers.get(handlerName + "OnCardPlayed") == null) {
            gameEventHandlers.put(handlerName + "OnCardPlayed", handler);
            onCardPlayedHandlers.add(handler);
        }
    }

    public void addOnCardDrawnHandler(GameEventCardHandler handler) {
        onCardDrawnHandlers.add(handler);
    }

    public void addOnCardDrawnHandler(String handlerName, GameEventCardHandler handler) {
        if (gameEventHandlers.get(handlerName + "OnCardDrawn") == null) {
            gameEventHandlers.put(handlerName + "OnCardDrawn", handler);
            onCardDrawnHandlers.add(handler);
        }
    }

    public void addOnRetainHandler(GameEventCardHandler handler) {
        onRetainHandlers.add(handler);
    }

    public void addOnRetainHandler(String handlerName, GameEventCardHandler handler) {
        if (gameEventHandlers.get(handlerName + "OnRetain") == null) {
            gameEventHandlers.put(handlerName + "OnRetain", handler);
            onRetainHandlers.add(handler);
        }
    }

    public void addOnShuffleHandler(GameEventHandler handler) {
        onShuffleHandlers.add(handler);
    }

    public void addOnShuffleHandler(String handlerName, GameEventHandler handler) {
        if (gameEventHandlers.get(handlerName + "OnShuffle") == null) {
            gameEventHandlers.put(handlerName + "OnShuffle", handler);
            onShuffleHandlers.add(handler);
        }
    }

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

    public void registerMetallicizeHandler(GameState state, int counterIdx) {
        state.properties.addPreEndOfTurnHandler("Metallicize", new GameEventHandler() {
            @Override public void handle(GameState state) {
                state.getPlayerForWrite().gainBlockNotFromCardPlay(state.getCounterForRead()[counterIdx]);
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
        cards.stream().filter((x) -> !x.isXCost && x.energyCost >= 0 && !(x instanceof Card.CardPermChangeCost) && !(x instanceof Card.CardTmpChangeCost) && !cardCanGenerateSneckoEnergyCost(x)).forEach((x) -> {
            for (int i = 0; i < 4; i++) {
                if (x.energyCost == i) {
                    continue;
                }
                newCards.add(new Card.CardPermChangeCost(x, i));
            }
        });
        if (cards.stream().anyMatch((x) -> x.cardName.equals("Streamline"))) {
            newCards.add(new CardDefect.Streamline(3));
        }
        if (cards.stream().anyMatch((x) -> x.cardName.equals("Streamline+"))) {
            newCards.add(new CardDefect.StreamlineP(3));
        }
        return newCards;
    }

    public void setupSneckoIndexes() {
        sneckoIdxes = new int[cardDict.length][];
        var m = new HashMap<String, int[]>();
        for (int i = 0; i < cardDict.length; i++) {
            var card = cardDict[i];
            if (!(card instanceof Card.CardPermChangeCost) && !(card instanceof Card.CardTmpChangeCost)) {
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
            if (card instanceof Card.CardPermChangeCost c && card.energyCost < 4) {
                var a = m.get(c.card.cardName);
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
            if (card instanceof Card.CardTmpChangeCost c) {
                if (c.card instanceof Card.CardPermChangeCost cc) {
                    sneckoIdxes[i] = m.get(cc.card.cardName);
                } else if (cardCanGenerateSneckoEnergyCost(card)) {
                    sneckoIdxes[i] = m.get(getCanonicalCardName(card));
                } else {
                    sneckoIdxes[i] = m.get(c.card.cardName);
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
