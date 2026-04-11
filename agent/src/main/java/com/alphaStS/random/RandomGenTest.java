package com.alphaStS.random;

import java.util.Queue;

/**
 * RNG implementation used during test replay. Reads enemy starting HP from a pre-loaded
 * queue of log events instead of generating random values, so the simulation matches
 * the exact HP the game chose. All other random calls delegate to normal RNG.
 */
public class RandomGenTest extends RandomGen.RandomGenPlain {
    private final Queue<int[]> replayEventQueue;

    public RandomGenTest(Queue<int[]> replayEventQueue) {
        this.replayEventQueue = replayEventQueue;
    }

    @Override public int nextInt(int bound, RandomGenCtx ctx, Object arg) {
        if (ctx == RandomGenCtx.RandomEnemyHealth) {
            int[] entry = replayEventQueue.poll();
            if (entry == null) {
                throw new IllegalStateException("replayEventQueue is empty for RandomEnemyHealth");
            }
            return entry[1] - entry[0];
        }
        return super.nextInt(bound, ctx, arg);
    }
}
