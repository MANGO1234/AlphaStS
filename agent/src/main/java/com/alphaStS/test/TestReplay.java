package com.alphaStS.test;

import com.alphaStS.GameState;
import com.alphaStS.card.Card;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class TestReplay {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Called from {@link GameState#reshuffle()} when {@code properties.testingReplayMode} is true.
     * Pops the next {@link TestReplayEvent.ShuffleEvent} from {@code properties.replayEventQueue}
     * and sets {@code deckArr} to that fixed order, ensuring cards are drawn in the exact sequence
     * recorded in the log.
     *
     * @return true if the queue had a ShuffleEvent and the deck was set, false if the queue was empty
     */
    public static boolean applyShuffleFromQueue(GameState state) {
        Queue<TestReplayEvent> queue = state.properties.replayEventQueue;
        if (queue == null || queue.isEmpty()) {
            return false;
        }
        TestReplayEvent event = queue.poll();
        if (!(event instanceof TestReplayEvent.ShuffleEvent shuffleEvent)) {
            throw new IllegalStateException("Expected ShuffleEvent but got " + event.getClass().getSimpleName());
        }
        String[] deckOrder = shuffleEvent.deckOrder;
        state.deckArrLen = 0;
        for (String cardName : deckOrder) {
            for (int i = 0; i < state.properties.cardDict.length; i++) {
                if (state.properties.cardDict[i].cardName.equals(cardName)) {
                    state.getDeckArrForWrite()[state.deckArrLen++] = (short) i;
                    break;
                }
            }
        }
        state.deckArrFixedDrawLen = state.deckArrLen;
        return true;
    }

    /**
     * Compares a {@code state:floor} log entry against the current {@link GameState} and syncs
     * the card piles from the log.
     *
     * <p>Strictly compared (throws {@link ReplayException} on mismatch): player energy, player
     * block, each monster's current HP, and the exhaust pile.
     *
     * <p>Synced from log without comparing: hand, draw pile, discard pile. STS draws randomly
     * while the simulation uses a different RNG, so draw order diverges between turns. Syncing
     * these piles ensures each action is applied to the exact hand the log recorded.
     *
     * @param stateFloorJson the raw JSON string of a {@code state:floor} log entry
     * @param state          the current game state to compare and sync against
     * @throws ReplayException if any strictly-compared field does not match the log
     */
    public static void compareStateFloor(String stateFloorJson, GameState state) throws ReplayException {
        JsonNode root;
        try {
            root = MAPPER.readTree(stateFloorJson);
        } catch (Exception e) {
            throw new ReplayException("Failed to parse state:floor JSON: " + e.getMessage(), state, stateFloorJson);
        }

        JsonNode combatState = root.path("combat_state");
        Card[] cardDict = state.properties.cardDict;

        int logEnergy = combatState.path("player").path("energy").asInt(-1);
        if (logEnergy >= 0 && logEnergy != state.energy) {
            throw new ReplayException(
                "Energy mismatch: log=" + logEnergy + " state=" + state.energy, state, stateFloorJson);
        }

        int logBlock = combatState.path("player").path("block").asInt(0);
        int stateBlock = state.getPlayerForRead().getBlock();
        if (logBlock != stateBlock) {
            throw new ReplayException(
                "Block mismatch: log=" + logBlock + " state=" + stateBlock, state, stateFloorJson);
        }

        JsonNode monsters = combatState.path("monsters");
        for (int i = 0; i < monsters.size(); i++) {
            int logHp = monsters.get(i).path("hp_current").asInt(-1);
            if (logHp >= 0) {
                int stateHp = state.getEnemiesForRead().get(i).getHealth();
                if (logHp != stateHp) {
                    throw new ReplayException(
                        "Monster[" + i + "] HP mismatch: log=" + logHp + " state=" + stateHp,
                        state, stateFloorJson);
                }
            }
        }

        compareCardList("exhaust_pile", combatState.path("exhaust_pile"),
            state.exhaustArr, state.exhaustArrLen, cardDict, state, stateFloorJson);

        state.handArr = buildCardArr(combatState.path("hand"), cardDict);
        state.handArrLen = combatState.path("hand").size();

        state.deckArr = buildCardArr(combatState.path("draw_pile"), cardDict);
        state.deckArrLen = combatState.path("draw_pile").size();
        state.deckArrFixedDrawLen = state.deckArrLen;

        state.discardArr = buildCardArr(combatState.path("discard_pile"), cardDict);
        state.discardArrLen = combatState.path("discard_pile").size();
    }

    private static void compareCardList(
            String listName, JsonNode logCards,
            short[] stateArr, int stateArrLen,
            Card[] cardDict,
            GameState state, String line) throws ReplayException {
        Map<String, Integer> logCounts = new HashMap<>();
        for (JsonNode card : logCards) {
            logCounts.merge(card.asText(), 1, Integer::sum);
        }

        Map<String, Integer> stateCounts = new HashMap<>();
        for (int i = 0; i < stateArrLen; i++) {
            stateCounts.merge(cardDict[stateArr[i]].cardName, 1, Integer::sum);
        }

        if (!logCounts.equals(stateCounts)) {
            throw new ReplayException(
                listName + " mismatch: log=" + logCounts + " state=" + stateCounts, state, line);
        }
    }

    private static short[] buildCardArr(JsonNode cards, Card[] cardDict) {
        short[] arr = new short[cards.size() + 2];
        int idx = 0;
        for (JsonNode card : cards) {
            String cardName = card.asText();
            for (int i = 0; i < cardDict.length; i++) {
                if (cardDict[i].cardName.equals(cardName)) {
                    arr[idx++] = (short) i;
                    break;
                }
            }
        }
        return arr;
    }
}
