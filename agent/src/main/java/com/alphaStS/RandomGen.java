package com.alphaStS;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class RandomGen {
    Random random;

    public RandomGen() {
        random = new Random();
    }

    public RandomGen(long seed) {
        random = new Random();
        random.setSeed(seed);
    }

    public RandomGen(RandomGen other) {
        long seed;
        try {
            Field field = Random.class.getDeclaredField("seed");
            field.setAccessible(true);
            AtomicLong scrambledSeed = (AtomicLong) field.get(other.random);
            seed = scrambledSeed.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        random = new Random(seed ^ 0x5DEECE66DL);
    }

    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    public float nextFloat() {
        return random.nextFloat();
    }

    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    public int nextInt(int bound, GameState state, RandomGenCtx ctx) {
        return random.nextInt(bound);
    }

    public long nextLong() {
        return random.nextLong();
    }
}
