package com.alphaStS.test;

import com.alphaStS.GameState;

public class TestReplay {

    /**
     * Called from {@link GameState#reshuffle()} when {@code properties.testingReplayMode} is true.
     * Pops the next deck order from {@code properties.replayEventQueue} and sets {@code deckArr}
     * to that fixed order, ensuring cards are drawn in the exact sequence recorded in the log.
     *
     * @return true if the queue had an entry and the deck was set, false if the queue was empty
     */
    public static boolean applyShuffleFromQueue(GameState state) {
        if (state.properties.replayEventQueue == null || state.properties.replayEventQueue.isEmpty()) {
            return false;
        }
        String[] deckOrder = state.properties.replayEventQueue.poll();
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
     * Compares a {@code state:floor} JSON object from a replay log against the current
     * {@link GameState} to verify they match after each action.
     *
     * @param stateFloorJson the raw JSON string of a {@code state:floor} log entry
     * @param state          the current game state to compare against
     * @throws ReplayException if the states do not match
     */
    public static void compareStateFloor(String stateFloorJson, GameState state) throws ReplayException {
        // TODO: implement field-by-field comparison between the logged state:floor and GameState
    }
}
