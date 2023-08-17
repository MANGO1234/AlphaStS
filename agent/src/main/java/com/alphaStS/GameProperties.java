package com.alphaStS;

import com.alphaStS.enemy.EnemyList;
import com.alphaStS.enums.CharacterEnum;

import java.util.*;
import java.util.function.BiConsumer;

public class GameProperties implements Cloneable {
    public boolean testNewFeature = true;
    public boolean multithreadedMTCS;
    public boolean doingComparison;
    public boolean testPotionOutput = true;
    public boolean curriculumTraining;
    public int minDifficulty;
    public int maxDifficulty;
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
    public boolean enemyCanGetVuln;
    public boolean enemyCanGetWeakened;
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
    public int[] potionsVArrayIdx;
    public int[] potionsScenarios;
    public EnemyList originalEnemies;
    public int maxNumOfActions;
    public int totalNumOfActions;
    public GameAction[][] actionsByCtx;
    public CharacterEnum character;

    // cached card indexes
    public int realCardsLen; // not including CardTmpCostChange (due to liquid memory)
    public int[] discardIdxes; // cards that can change in number of copies during a fight
    public int[] discardReverseIdxes;
    public int[] upgradeIdxes;
    public int[] tmp0CostCardTransformIdxes;
    public int[] tmp0CostCardReverseTransformIdxes;
    public int[] select1OutOf3CardsIdxes;
    public int[] select1OutOf3CardsReverseIdxes;
    public int[] skillPotionIdxes;
    public int[] powerPotionIdxes;
    public int[][] sneckoIdxes;
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
    public int shivCardIdx = -1;
    public int shivPCardIdx = -1;
    public int echoFormCardIdx = -1;
    public int echoFormPCardIdx = -1;
    public int wellLaidPlansCardIdx = -1;
    public int toolsOfTheTradeCardIdx = -1;
    public int[] bloodForBloodIndexes;
    public int[] bloodForBloodTransformIndexes;
    public int[] bloodForBloodPIndexes;
    public int[] bloodForBloodPTransformIndexes;
    public int[] masterfulStabIndexes;
    public int[] masterfulStabTransformIndexes;
    public int[] masterfulStabPIndexes;
    public int[] masterfulStabPTransformIndexes;
    public int[] streamlineIndexes;
    public int[] streamlinePIndexes;
    public int[] clawIndexes;
    public int[] clawTransformIndexes;
    public int[] clawPIndexes;
    public int[] clawPTransformIndexes;
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
    public int sneakyStrikeCounterIdx = -1;
    public int eviscerateCounterIdx = -1;
    public int wellLaidPlansCounterIdx = -1;
    public int blurCounterIdx = -1;
    public int phantasmalKillerCounterIdx = -1;
    public int reboundCounterIdx = -1;
    public int sneckoDebuffCounterIdx = -1;
    public int envenomCounterIdx = -1;
    public int geneticAlgorithmCounterIdx = -1;
    public int bufferCounterIdx = -1;
    public int loopCounterIdx = -1;
    public int toolsOfTheTradeCounterIdx = -1;
    public int electrodynamicsCounterIdx = -1;

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
    public boolean hasTorri;
    public boolean hasTungstenRod;
    public boolean hasChampionBelt;
    public boolean hasPaperPhrog;
    public boolean hasMeatOnBone;
    public boolean hasBurningBlood;
    public boolean hasPaperCrane;
    public boolean hasSneckoSkull;
    public boolean hasSneckoEye;
    public boolean hasTingsha;
    public boolean hasToughBandages;
    public boolean hasGoldPlatedCable;

    public int loseDexterityPerTurnCounterIdx;
    public int loseFocusPerTurnCounterIdx = -1;
    public int constrictedCounterIdx = -1;
    public int drawReductionCounterIdx = -1;
    public int timeEaterCounterIdx = -1;
    public int normalityCounterIdx = -1;
    public int penNibCounterIdx = -1;

    // cached game properties for generating NN input
    public int shieldAndSpireFacingIdx = -1;
    public boolean battleTranceExist;
    public boolean energyRefillCanChange;
    public boolean isHeartFight;
    public boolean healEndOfAct;
    public int inputLen;
    public int extraOutputLen;
    public boolean cardInDiscardInNNInput;
    public boolean discard0CardOrderMatters;
    public int discardOrderMaxKeepTrackIn10s; // currently, those are sent manually in all or one
    public int discardOrder0CardMaxCopies;
    public int discardOrder0CostNumber;
    public int[] discardOrder0CardReverseIdx;
    public List<Integer> innateOrder;

    // relics/cards can add checks like e.g. Burn checking if it's in hand pre end of turn
    public Map<String, Object> gameEventHandlers = new HashMap<>();
    GameEventHandler endOfPreBattleHandler;
    public List<GameEventHandler> startOfBattleHandlers = new ArrayList<>();
    public List<GameEventHandler> endOfBattleHandlers = new ArrayList<>();
    public List<GameEventHandler> startOfTurnHandlers = new ArrayList<>();
    public List<GameEventHandler> preEndTurnHandlers = new ArrayList<>();
    public List<GameEventHandler> endOfTurnHandlers = new ArrayList<>();
    public List<GameEventHandler> onExhaustHandlers = new ArrayList<>();
    public List<GameEventHandler> onBlockHandlers = new ArrayList<>(); // todo: need to call handler
    public List<GameEventEnemyHandler> onEnemyDeathHandlers = new ArrayList<>();
    public List<OnDamageHandler> onDamageHandlers = new ArrayList<>();
    public List<OnDamageHandler> onHealHandlers = new ArrayList<>();
    public List<GameEventCardHandler> onPreCardPlayedHandlers = new ArrayList<>();
    public List<GameEventCardHandler> onCardPlayedHandlers = new ArrayList<>();
    public List<GameEventCardHandler> onCardDrawnHandlers = new ArrayList<>();
    public GameStateRandomization randomization;
    public GameStateRandomization preBattleRandomization;
    public GameStateRandomization preBattleScenarios;
    public GameStateRandomization preBattleScenariosBackup;
    public int preBattleScenariosChosen = -1;
    public List<Map.Entry<Integer, GameStateRandomization.Info>> preBattleGameScenariosList;
    public List<BiConsumer<GameState, int[]>> enemiesReordering;

    // defect specific
    public int maxNumOfOrbs;

    public double cpuct = 0.1;
    public int difficulty;
    public int fightProgressVIdx;
    public int qwinVIdx = -1;
    public int dmgDistVIdx = -1;
    public int v_total_len;

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

    public interface CounterRegistrant {
        void setCounterIdx(GameProperties gameProperties, int idx);
    }

    interface TrainingTargetRegistrant {
        void setVArrayIdx(int idx);
    }

    public interface NetworkInputHandler {
        int addToInput(GameState state, float[] input, int idx);
        int getInputLenDelta();
        default String getDisplayString(GameState state) { return null; }
        default void onRegister(int counterIdx) { }
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
            }
            counterIdx.put(counterNames[i], i);
            if (counterHandlers[i] != null) {
                counterHandlers[i].onRegister(i);
            }
        }
        counterHandlersNonNull = Arrays.stream(counterHandlers).filter(Objects::nonNull).toList()
                .toArray(new NetworkInputHandler[0]);

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

    public void addExtraTrainingTarget(String targetId, TrainingTargetRegistrant registrant, TrainingTarget target) {
        if (trainingTargetsMap.get(targetId) == null) {
            trainingTargetsMap.put(targetId, target);
            trainingTargetsRegistrantMap.put(targetId, registrant);
        }
    }

    public void compileExtraTrainingTarget() {
        var registrants = trainingTargetsRegistrantMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
        for (int i = 0; i < registrants.size(); i++) {
            registrants.get(i).getValue().setVArrayIdx(extraOutputLen);
            extraOutputLen += trainingTargetsMap.get(registrants.get(i).getKey()).getNumberOfTargets();
            extraTrainingTargets.add(trainingTargetsMap.get(registrants.get(i).getKey()));
            extraTrainingTargetsLabel.add(registrants.get(i).getKey());
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

    private static CounterRegistrant IntangibleCounterRegistrant = (gameProperties, idx) -> gameProperties.intangibleCounterIdx = idx;

    public void registerBufferCounter(GameState state, CounterRegistrant registrant) {
        state.prop.registerCounter("Buffer", registrant, new GameProperties.NetworkInputHandler() {
            @Override public int addToInput(GameState state, float[] input, int idx) {
                input[idx] = state.getCounterForRead()[state.prop.bufferCounterIdx] / 10.0f;
                return idx + 1;
            }
            @Override public int getInputLenDelta() {
                return 1;
            }
            @Override public void onRegister(int counterIdx) {
                state.prop.bufferCounterIdx = counterIdx;
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
                if (state.getCounterForRead()[state.prop.intangibleCounterIdx] > 0) {
                    state.getCounterForWrite()[state.prop.intangibleCounterIdx]--;
                }
            }
        });
    }

    public void registerMetallicizeHandler(GameState state, int counterIdx) {
        state.addPreEndOfTurnHandler("Metallicize", new GameEventHandler() {
            @Override public void handle(GameState state) {
                state.getPlayerForWrite().gainBlockNotFromCardPlay(state.getCounterForRead()[counterIdx]);
            }
        });
    }

    public void registerSneckoDebuffCounter() {
        registerCounter("Snecko", new GameProperties.CounterRegistrant() {
            @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
                gameProperties.sneckoDebuffCounterIdx = idx;
            }
        }, new GameProperties.NetworkInputHandler() {
            @Override public int addToInput(GameState state, float[] input, int idx) {
                input[idx] = state.getCounterForRead()[state.prop.sneckoDebuffCounterIdx] > 0 ? 0.5f : 0.0f;
                return idx + 1;
            }

            @Override public int getInputLenDelta() {
                return 1;
            }
        });
    }

    public static List<Card> generateSneckoCards(List<Card> cards) {
        var newCards = new ArrayList<Card>();
        cards.stream().filter((x) -> !x.isXCost && x.energyCost >= 0 && !(x instanceof Card.CardPermChangeCost) && !(x instanceof Card.CardTmpChangeCost)).forEach((x) -> {
            for (int i = 0; i < 4; i++) {
                if (x.energyCost == i) {
                    continue;
                }
                newCards.add(new Card.CardPermChangeCost(x, i));
            }
        });
        return newCards;
    }

    public void setupSneckoIndexes() {
        sneckoIdxes = new int[cardDict.length][];
        var m = new HashMap<String, int[]>();
        for (int i = 0; i < cardDict.length; i++) {
            var card = cardDict[i];
            if (!(card instanceof Card.CardPermChangeCost) && !(card instanceof Card.CardTmpChangeCost)) {
                var a = new int[] { 1, i, -1, -1, -1 };
                m.put(card.cardName, a);
                sneckoIdxes[i] = a;
            }
        }
        for (int i = 0; i < cardDict.length; i++) {
            var card = cardDict[i];
            if (card instanceof Card.CardPermChangeCost c) {
                var a = m.get(c.card.cardName);
                a[++a[0]] = i;
                sneckoIdxes[i] = a;
            }
        }
        for (int i = 0; i < cardDict.length; i++) {
            var card = cardDict[i];
            if (card instanceof Card.CardTmpChangeCost c) {
                if (c.card instanceof Card.CardPermChangeCost cc) {
                    sneckoIdxes[i] = m.get(cc.card.cardName);
                } else {
                    sneckoIdxes[i] = m.get(c.card.cardName);
                }
            }
        }
    }
}
