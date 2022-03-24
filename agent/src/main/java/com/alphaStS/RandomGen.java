package com.alphaStS;

import java.util.Random;

public class RandomGen {
    Random random;

    public RandomGen() {
        random = new Random();
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
}
