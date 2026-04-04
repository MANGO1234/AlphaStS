package com.alphaStS.test;

import com.alphaStS.GameStateBuilder;
import com.alphaStS.utils.Tuple3;

import java.util.Iterator;

public class TestRunner {
    public void test(String runDataPath) throws Exception {
        Iterator<Tuple3<Integer, Integer, GameStateBuilder>> runData = new RunDataParser(runDataPath).iterator();

        while (runData.hasNext()) {
            Tuple3<Integer, Integer, GameStateBuilder> entry = runData.next();
            int runIdx = entry.v1();
            int battleIdx = entry.v2();
            GameStateBuilder builder = entry.v3();
            System.out.println("[TestRunner] Run " + runIdx + ", Battle " + battleIdx);

            // TODO 1: Using ??? mod, construct a battle with the same deck, relics, potion etc.
            // of the battle data in a running Slay the Spire instance.

            // TODO 2: Using communication mod and a random move bot + player having 10000 hp,
            // make random moves until battle ends (e.g. 30 turns limit).

            // TODO 3: The states, actions, and rng rolls will be logged to .log (via a mod).

            // TODO 4: Use .log, construct a GameState of the same battle, replay from the start
            // of log until the end and check if the states match after each action.
        }
    }
}
