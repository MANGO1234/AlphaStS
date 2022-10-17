package com.alphaStS;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.Result;
import ai.onnxruntime.OrtSession.SessionOptions;
import ai.onnxruntime.OrtSession.SessionOptions.OptLevel;
import com.alphaStS.utils.Utils;

import java.util.*;

record NNOutput(float v_health, float v_win, double[] v_other, float[] policy, int[] legalActions) {
}

public class Model {
    public static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private static final int MAX_ENTRIES;

        static {
            if (System.getProperty("os.name").startsWith("Windows")) {
                MAX_ENTRIES = 250000;
            } else {
                MAX_ENTRIES = 10000;
            }
        }

        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_ENTRIES;
        }
    }

    OrtEnvironment env;
    OrtSession session;
    String inputName;
    LRUCache<InputHash, NNOutput> cache;
    int calls;
    int cache_hits;
    long time_taken;

    /**
     * Naively takes the softmax of the input.
     *
     * @param input The input array.
     * @return The softmax of the input.
     */
    public static float[] softmax(float[] input) {
        double[] tmp = new double[input.length];
        float max = Float.MIN_VALUE;
        for (int i = 0; i < input.length; i++) {
            max = Math.max(max, input[i]);
        }
        double sum = 0.0;
        for (int i = 0; i < input.length; i++) {
            double val = Math.exp(input[i] - max);
            sum += val;
            tmp[i] = val;
        }
        float[] output = new float[input.length];
        for (int i = 0; i < output.length; i++) {
            output[i] = (float) (tmp[i] / sum);
        }
        return output;
    }

    public static float[] softmax(float[] input, float temp) {
        double[] tmp = new double[input.length];
        float max = Float.MIN_VALUE;
        for (int i = 0; i < input.length; i++) {
            max = Math.max(max, input[i] / temp);
        }
        double sum = 0.0;
        for (int i = 0; i < input.length; i++) {
            double val = Math.exp(input[i] / temp - max);
            sum += val;
            tmp[i] = val;
        }
        float[] output = new float[input.length];
        for (int i = 0; i < output.length; i++) {
            output[i] = (float) (tmp[i] / sum);
        }
        return output;
    }

    public Model(String modelDir) {
        try {
            env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions opts = new SessionOptions();
            opts.setOptimizationLevel(OptLevel.ALL_OPT);
            opts.setCPUArenaAllocator(true);
            opts.setMemoryPatternOptimization(true);
            opts.setExecutionMode(SessionOptions.ExecutionMode.PARALLEL);
            // opts.setInterOpNumThreads(1);
            // opts.setIntraOpNumThreads(1);
            session = env.createSession(modelDir + "/model.onnx", opts);
            inputName = session.getInputNames().iterator().next();
            cache = new LRUCache<>();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            session.close();
        } catch (OrtException e) {
            throw new RuntimeException(e);
        }
    }

    NNOutput eval(GameState state) {
        calls += 1;
        InputHash hash = new InputHash(state.getNNInput());
        NNOutput o = cache.get(hash);
        if (o != null) {
            if (!Arrays.equals(o.legalActions(), state.getLegalActions()) &&
                    (state.actionCtx != GameActionCtx.SELECT_ENEMY || state.prop.enemiesReordering == null)) {
                System.err.println(Arrays.toString(state.getNNInput()));
                System.err.println(state);
                System.err.println(Arrays.toString(o.legalActions()));
                System.err.println(Arrays.toString(state.getLegalActions()));
                for (int i = 0; i < state.getLegalActions().length; i++) {
                    System.err.println(state.getActionString(i));
                }
                Integer.parseInt(null);
            }
            cache_hits += 1;
            return o;
        }

        var x = new float[1][];
        x[0] = hash.input;
        long start = System.currentTimeMillis();
        try (OnnxTensor test = OnnxTensor.createTensor(env, x);
                Result output = session.run(Collections.singletonMap(inputName, test))) {
            float v_health = ((float[][]) output.get(0).getValue())[0][0];
            float v_win = ((float[][]) output.get(1).getValue())[0][0];

            float[] policy = ((float[][]) output.get(2).getValue())[0];
            if (state.prop.maxNumOfActions != state.prop.totalNumOfActions) {
                if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                    policy = Arrays.copyOf(policy, state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length);
                } else if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                    int startIdx = state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length;
                    int lenToCopy = state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()].length;
                    policy = Utils.arrayCopy(policy, startIdx, lenToCopy);
                } else if (state.actionCtx == GameActionCtx.SELECT_SCENARIO) {
                    int startIdx = state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length;
                    if (state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null) {
                        startIdx += state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()].length;
                    }
                    int lenToCopy = state.prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()].length;
                    policy = Utils.arrayCopy(policy, startIdx, lenToCopy);
                }
            }
            if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                var order = state.getEnemyOrder();
                if (order != null) {
                    var newPolicy = new float[policy.length];
                    for (int i = 0; i < newPolicy.length; i++) {
                        newPolicy[order[i]] = policy[i];
                    }
                    policy = newPolicy;
                }
            }
            var legalActions = state.getLegalActions();
            float[] policyCompressed = new float[legalActions.length];
            for (int i = 0; i < legalActions.length; i++) {
                policyCompressed[i] = policy[legalActions[i]];
            }

            double[] v_other = null;
            if (output.size() > 3 && state.prop.extraOutputLen > 0) {
                v_other = new double[state.prop.extraOutputLen];
                for (int i = 0; i < v_other.length; i++) {
                    float value = ((float[][]) output.get(3 + i).getValue())[0][0];
                    v_other[i] = (value + 1) / 2;
                }
            }

            state.v_health = (v_health + 1) / 2;
            state.v_win = (v_win + 1) / 2;
            state.policy = policy;
            state.v_other = v_other;
            o = new NNOutput((float) state.v_health, (float) state.v_win, v_other, softmax(policyCompressed), legalActions);
            cache.put(hash, o);
            time_taken += System.currentTimeMillis() - start;
            return o;
        } catch (OrtException exception) {
            throw new RuntimeException(exception);
        }
    }
}
