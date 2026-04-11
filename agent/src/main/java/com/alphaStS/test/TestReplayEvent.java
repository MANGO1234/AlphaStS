package com.alphaStS.test;

/**
 * Sealed event hierarchy for the unified replay event queue. Each event type corresponds to
 * a log entry that the replay infrastructure needs to consume in order.
 */
public abstract class TestReplayEvent {

    public static final class ShuffleEvent extends TestReplayEvent {
        public final String[] deckOrder;

        public ShuffleEvent(String[] deckOrder) {
            this.deckOrder = deckOrder;
        }
    }

    public static final class EnemyHpEvent extends TestReplayEvent {
        public final int minHp;
        public final int maxHp;
        public final int chosenHp;

        public EnemyHpEvent(int minHp, int maxHp, int chosenHp) {
            this.minHp = minHp;
            this.maxHp = maxHp;
            this.chosenHp = chosenHp;
        }
    }
}
