package com.alphaStS;

import com.alphaStS.enemy.EnemyList;

import java.util.*;

public class GameProperties implements Cloneable {
    public boolean playerArtifactCanChange;
    public boolean playerStrengthCanChange;
    public boolean playerDexterityCanChange;
    public boolean playerStrengthEotCanChange;
    public boolean playerDexterityEotCanChange;
    public boolean playerCanGetVuln;
    public boolean playerCanGetWeakened;
    public boolean playerCanGetFrailed;
    public boolean playerCanHeal;
    public boolean enemyCanGetVuln;
    public boolean enemyCanGetWeakened;
    public boolean enemyStrengthEotCanChange;
    public long possibleBuffs;
    public boolean needDeckOrderMemory;
    public boolean selectFromExhaust;
    public RandomGen random;
    public RandomGen realMoveRandomGen;
    public boolean makingRealMove;
    public Card[] cardDict;
    public List<Potion> potions;
    public EnemyList originalEnemies;
    public int maxNumOfActions;
    public int totalNumOfActions;
    public GameAction[][] actionsByCtx;

    // cached card indexes
    public int realCardsLen; // not including CardTmpCostChange (due to liquid memory)
    public int[] discardIdxes; // cards that can change in number of copies during a fight
    public int[] upgradeIdxes;
    public int[] tmpCostCardIdxes;
    public int angerCardIdx = -1;
    public int angerPCardIdx = -1;
    public int[] strikeCardIdxes;
    // cached status indexes
    public int burnCardIdx = -1;
    public int dazedCardIdx = -1;
    public int slimeCardIdx = -1;
    public int woundCardIdx = -1;
    public int[] bloodForBloodIndexes;
    public int[] bloodForBloodPIndexes;
    public int[] infernalBladeIndexes;

    public boolean hasBlueCandle;
    public boolean hasBoot;
    public boolean hasCaliper;
    public boolean hasGinger;
    public boolean hasIceCream;
    public boolean hasMedicalKit;
    public boolean hasOddMushroom;
    public boolean hasRunicPyramid;
    public boolean hasStrangeSpoon;
    public boolean hasTurnip;

    public int normalityCounterIdx = -1;
    public int penNibCounterIdx = -1;

    // cached game properties for generating NN input
    public boolean battleTranceExist;
    public boolean energyRefillCanChange;
    public boolean isSlimeBossFight;
    public int inputLen;

    // relics/cards can add checks like e.g. Burn checking if it's in hand pre end of turn
    public Map<String, Object> gameEventHandlers = new HashMap<>();
    public List<GameEventHandler> startOfGameHandlers = new ArrayList<>();
    public List<GameEventHandler> startOfTurnHandlers = new ArrayList<>();
    public List<GameEventHandler> preEndTurnHandlers = new ArrayList<>();
    public List<GameEventHandler> onExhaustHandlers = new ArrayList<>();
    public List<GameEventHandler> onBlockHandlers = new ArrayList<>(); // todo: need to call handler
    public List<OnDamageHandler> onDamageHandlers = new ArrayList<>();
    public List<GameEventCardHandler> onCardPlayedHandlers = new ArrayList<>();
    public List<GameEventCardHandler> onCardDrawnHandlers = new ArrayList<>();
    public GameStateRandomization randomization;
    public GameStateRandomization preBattleRandomization;
    public GameStateRandomization preBattleScenarios;
    public List<Map.Entry<Integer, GameStateRandomization.Info>> preBattleGameScenariosList;

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

    interface CounterRegistrant {
        void setCounterIdx(GameProperties gameProperties, int idx);
    }

    interface NetworkInputHandler {
        int addToInput(GameState state, float[] input, int idx);
        int getInputLenDelta();
    }

    Map<String, List<CounterRegistrant>> counterRegistrants = new HashMap<>();
    Map<String, NetworkInputHandler> counterHandlerMap = new HashMap<>();

    public void registerCounter(String name, CounterRegistrant registrant, NetworkInputHandler handler) {
        var registrants = counterRegistrants.computeIfAbsent(name, k -> new ArrayList<>());
        registrants.add(registrant);
        if (handler != null) {
            counterHandlerMap.putIfAbsent(name, handler);
        }
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
        }
        counterHandlersNonNull = Arrays.stream(counterHandlers).filter(Objects::nonNull).toList()
                .toArray(new NetworkInputHandler[0]);
    }
}
