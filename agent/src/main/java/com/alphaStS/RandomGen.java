package com.alphaStS;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public abstract class RandomGen {
    public abstract boolean nextBoolean(RandomGenCtx ctx);
    public abstract float nextFloat(RandomGenCtx ctx);
    public abstract int nextInt(int bound, RandomGenCtx ctx);
    public abstract int nextInt(int bound, RandomGenCtx ctx, Object arg);
    public abstract long nextLong(RandomGenCtx ctx);

    public RandomGen createWithSeed(long seed) {
        throw new UnsupportedOperationException();
    }

    public RandomGen getCopy() {
        throw new UnsupportedOperationException();
    }

    public long getSeed(RandomGenCtx ctx) {
        throw new UnsupportedOperationException();
    }

    public long getStartingSeed() {
        throw new UnsupportedOperationException();
    }

    public void timeTravelToBeginning() {
        throw new UnsupportedOperationException();
    }

    public static class RandomGenPlain extends RandomGen {
        Random random;
        long startingSeed;

        public RandomGenPlain() {
            random = new Random();
            startingSeed = getSeed(null);
        }

        public RandomGenPlain(Random random) {
            this.random = random;
            startingSeed = getSeed(null);
        }

        public RandomGenPlain(long seed) {
            this.random = new Random(seed);
            startingSeed = seed;
        }

        protected Random getRandomClone() {
            long seed;
            try {
                Field field = Random.class.getDeclaredField("seed");
                field.setAccessible(true);
                AtomicLong scrambledSeed = (AtomicLong) field.get(random);
                seed = scrambledSeed.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return new Random(seed ^ 0x5DEECE66DL);
        }

        public boolean nextBoolean(RandomGenCtx ctx) {
            return random.nextBoolean();
        }

        public float nextFloat(RandomGenCtx ctx) {
            return random.nextFloat();
        }

        public int nextInt(int bound, RandomGenCtx ctx) {
            return random.nextInt(bound);
        }

        public int nextInt(int bound, RandomGenCtx ctx, Object arg) {
            return random.nextInt(bound);
        }

        public long nextLong(RandomGenCtx ctx) {
            return random.nextLong();
        }

        public RandomGen getCopy() {
            return new RandomGenPlain(getRandomClone());
        }

        public void timeTravelToBeginning() {
            throw new UnsupportedOperationException();
        }

        public RandomGen createWithSeed(long seed) {
            var random = new Random();
            random.setSeed(seed);
            return new RandomGenPlain(random);
        }

        public long getSeed(RandomGenCtx ctx) {
            return RandomGen.getRandomSeed(random);
        }

        public long getStartingSeed() {
            return startingSeed;
        }
    }

    private static long getRandomSeed(Random r) {
        long seed;
        try {
            Field field = Random.class.getDeclaredField("seed");
            field.setAccessible(true);
            AtomicLong scrambledSeed = (AtomicLong) field.get(r);
            seed = scrambledSeed.get();
            seed ^= 0x5DEECE66DL;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return seed;
    }

    private static Random getRandomClone(Random r) {
        return new Random(getRandomSeed(r));
    }

    public static class RandomGenByCtx extends RandomGen {
        public List<RandomGen> randoms = new ArrayList<>();
        long startingSeed;
        public boolean useNewCommonNumberVR;

        public RandomGenByCtx(long seed) {
            startingSeed = seed;
            Random random = new Random(seed);
            for (int i = 0; i < RandomGenCtx.values().length; i++) {
                randoms.add(new RandomGenMemory(random.nextLong()));
            }
        }

        public RandomGenByCtx(RandomGenByCtx other) {
            startingSeed = other.startingSeed;
            for (int i = 0; i < RandomGenCtx.values().length; i++) {
                randoms.add(other.randoms.get(i).getCopy());
            }
        }

        public boolean nextBoolean(RandomGenCtx ctx) {
            return randoms.get(ctx.ordinal()).nextBoolean(ctx);
        }

        public float nextFloat(RandomGenCtx ctx) {
            return randoms.get(ctx.ordinal()).nextFloat(ctx);
        }

        public int nextInt(int bound, RandomGenCtx ctx) {
            return randoms.get(ctx.ordinal()).nextInt(bound, ctx);
        }

        public int nextInt(int bound, RandomGenCtx ctx, Object arg) {
            return randoms.get(ctx.ordinal()).nextInt(bound, ctx, arg);
        }

        public long nextLong(RandomGenCtx ctx) {
            return randoms.get(ctx.ordinal()).nextLong(ctx);
        }

        public RandomGen getCopy() {
            return new RandomGenByCtx(this);
        }

        public RandomGen createWithSeed(long seed) {
            if (Configuration.NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION && (!Configuration.TEST_NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION || useNewCommonNumberVR)) {
                return new RandomGenByCtxCOW(seed);
            } else {
                var random = new Random();
                random.setSeed(seed);
                return new RandomGenPlain(random);
            }
        }

        public void timeTravelToBeginning() {
            for (int i = 0; i < RandomGenCtx.values().length; i++) {
                randoms.get(i).timeTravelToBeginning();
            }
        }

        public long getSeed(RandomGenCtx ctx) {
            return randoms.get(ctx.ordinal()).getSeed(ctx);
        }

        public long getStartingSeed() {
            return startingSeed;
        }
    }

    public static record Arg(int type, int bound, Object result) {
        public final static int NEXT_BOOLEAN = 0;
        public final static int NEXT_FLOAT = 1;
        public final static int NEXT_INT = 2;
        public final static int NEXT_LONG = 3;
    }

    // after calling timeTravelToStart, will return the same result for the same sequence of next* calls as the original
    public static class RandomGenMemory extends RandomGen {
        Random random;
        private final List<Arg> memory;
        private int currentIdx = -1;

        public RandomGenMemory(long seed) {
            random = new Random(seed);
            memory = new ArrayList<>();
        }

        public RandomGenMemory(RandomGenMemory other) {
            random = getRandomClone(other.random);
            currentIdx = other.currentIdx;
            memory = new ArrayList<>(other.memory);
        }

        public boolean nextBoolean(RandomGenCtx ctx) {
            currentIdx++;
            if (currentIdx < memory.size()) {
                if (memory.get(currentIdx).type == Arg.NEXT_BOOLEAN) {
                    return (Boolean) memory.get(currentIdx).result;
                } else {
                    memory.subList(currentIdx, memory.size()).clear();
                }
            }
            boolean result = random.nextBoolean();
            memory.add(new Arg(Arg.NEXT_BOOLEAN, 0, result));
            return result;
        }

        public float nextFloat(RandomGenCtx ctx) {
            currentIdx++;
            if (currentIdx < memory.size()) {
                if (memory.get(currentIdx).type == Arg.NEXT_FLOAT) {
                    return (Float) memory.get(currentIdx).result;
                } else {
                    memory.subList(currentIdx, memory.size()).clear();
                }
            }
            float result = random.nextFloat();
            memory.add(new Arg(Arg.NEXT_FLOAT, 0, result));
            return result;
        }

        public int nextInt(int bound, RandomGenCtx ctx) {
            currentIdx++;
            if (currentIdx < memory.size()) {
                if (memory.get(currentIdx).type == Arg.NEXT_INT && memory.get(currentIdx).bound == bound) {
                    return (Integer) memory.get(currentIdx).result;
                } else {
                    memory.subList(currentIdx, memory.size()).clear();
                }
            }
            int result = random.nextInt(bound);
            memory.add(new Arg(Arg.NEXT_INT, bound, result));
            return result;
        }

        public int nextInt(int bound, RandomGenCtx ctx, Object arg) {
            return nextInt(bound, ctx);
        }

        public long nextLong(RandomGenCtx ctx) {
            currentIdx++;
            if (currentIdx < memory.size()) {
                if (memory.get(currentIdx).type == Arg.NEXT_LONG) {
                    return (Long) memory.get(currentIdx).result;
                } else {
                    memory.subList(currentIdx, memory.size()).clear();
                }
            }
            long result = random.nextLong();
            memory.add(new Arg(Arg.NEXT_LONG, 0, result));
            return result;
        }

        public void timeTravelToBeginning() {
            currentIdx = -1;
        }

        public RandomGen getCopy() {
            return new RandomGenMemory(this);
        }

        public RandomGen createWithSeed(long seed) {
            throw new UnsupportedOperationException();
        }

        public long getSeed(RandomGenCtx ctx) {
            long seed;
            try {
                Field field = Random.class.getDeclaredField("seed");
                field.setAccessible(true);
                AtomicLong scrambledSeed = (AtomicLong) field.get(random);
                seed = scrambledSeed.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return seed ^ 0x5DEECE66DL;
        }
    }

    public static class RandomGenByCtxCOW extends RandomGen {
        public List<RandomGen> randoms = new ArrayList<>();
        long startingSeed;

        public RandomGenByCtxCOW(long seed) {
            startingSeed = seed;
            Random random = new Random(seed);
            for (int i = 0; i < RandomGenCtx.values().length; i++) {
                randoms.add(new RandomGenPlain(random.nextLong()));
            }
        }

        public RandomGenByCtxCOW(RandomGenByCtxCOW other) {
            startingSeed = other.startingSeed;
            for (int i = 0; i < RandomGenCtx.values().length; i++) {
                randoms.add(other.randoms.get(i).getCopy());
            }
        }

        public boolean nextBoolean(RandomGenCtx ctx) {
            return randoms.get(ctx.ordinal()).nextBoolean(ctx);
        }

        public float nextFloat(RandomGenCtx ctx) {
            return randoms.get(ctx.ordinal()).nextFloat(ctx);
        }

        public int nextInt(int bound, RandomGenCtx ctx) {
            return randoms.get(ctx.ordinal()).nextInt(bound, ctx);
        }

        public int nextInt(int bound, RandomGenCtx ctx, Object arg) {
            return randoms.get(ctx.ordinal()).nextInt(bound, ctx, arg);
        }

        public long nextLong(RandomGenCtx ctx) {
            return randoms.get(ctx.ordinal()).nextLong(ctx);
        }

        public long getSeed(RandomGenCtx ctx) {
            return randoms.get(ctx.ordinal()).getSeed(ctx);
        }

        public RandomGen getCopy() {
            return new RandomGenByCtxCOW(this);
        }

        public long getStartingSeed() {
            return startingSeed;
        }
    }
}
