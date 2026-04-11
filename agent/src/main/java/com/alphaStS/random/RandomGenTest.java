package com.alphaStS.random;

import com.alphaStS.test.TestReplayEvent;

import java.util.Queue;

/**
 * RNG implementation used during test replay. Reads enemy starting HP from a pre-loaded
 * queue of log events instead of generating random values, so the simulation matches
 * the exact HP the game chose. All other random calls delegate to normal RNG.
 */
public class RandomGenTest extends RandomGen.RandomGenPlain {
    private final Queue<TestReplayEvent> replayEventQueue;

    public RandomGenTest(Queue<TestReplayEvent> replayEventQueue) {
        this.replayEventQueue = replayEventQueue;
    }

    @Override public int nextInt(int bound, RandomGenCtx ctx, Object arg) {
        if (ctx == RandomGenCtx.RandomEnemyHealth) {
            TestReplayEvent event = replayEventQueue.poll();
            if (event == null) {
                throw new IllegalStateException("replayEventQueue is empty for RandomEnemyHealth");
            }
            if (!(event instanceof TestReplayEvent.EnemyHpEvent hpEvent)) {
                throw new IllegalStateException("Expected EnemyHpEvent but got " + event.getClass().getSimpleName());
            }
            int expectedBound = hpEvent.maxHp - hpEvent.minHp;
            if (bound != expectedBound + 1) {
                throw new IllegalStateException("RandomEnemyHealth bound mismatch: log expects " + expectedBound + 1 + " but got " + bound);
            }
            return hpEvent.chosenHp - hpEvent.minHp;
        }
        return super.nextInt(bound, ctx, arg);
    }
}
