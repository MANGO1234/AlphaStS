package com.alphaStS;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.Result;
import ai.onnxruntime.OrtSession.SessionOptions;
import ai.onnxruntime.OrtSession.SessionOptions.OptLevel;
import com.alphaStS.utils.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

record NNOutput(float v_health, float v_win, float[] policy) {
}

public class Model {
    public static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private static final int MAX_ENTRIES = 1000000;

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

    public Model(String modelDir) {
        try {
            env = OrtEnvironment.getEnvironment();
            OrtSession.SessionOptions opts = new SessionOptions();
            opts.setOptimizationLevel(OptLevel.ALL_OPT);
            opts.setCPUArenaAllocator(true);
            opts.setMemoryPatternOptimization(true);
            opts.setExecutionMode(SessionOptions.ExecutionMode.PARALLEL);
            session = env.createSession(modelDir + "/model.onnx", opts);
            inputName = session.getInputNames().iterator().next();
            cache = new LRUCache<>();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    NNOutput eval(GameState state) {
        //                for (NodeInfo i : session.getInputInfo().values()) {
        //                    System.out.println(i.toString());
        //                }
        //                for (NodeInfo i : session.getOutputInfo().values()) {
        //                    System.out.println(i.toString());
        //                }
        calls += 1;
        InputHash hash = new InputHash(state.getNNInput());
        NNOutput o = cache.get(hash);
        if (o != null) {
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
                if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                    float[] newPolicy = new float[state.prop.maxNumOfActions];
                    Utils.arrayCopy(newPolicy, policy, state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length, state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()].length);
                    policy = newPolicy;
                } else {
                    policy = Arrays.copyOf(policy, state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length);
                }
            }
            for (int i = 0; i < policy.length; i++) {
                if (!state.isActionLegal(i)) {
                    policy[i] = -1000;
                }
            }
            state.v_health = v_health;
            state.v_win = v_win;
            state.policy = policy;
            o = new NNOutput(v_health, v_win, softmax(policy));
            cache.put(hash, o);
            time_taken += System.currentTimeMillis() - start;
            return o;
        } catch (OrtException exception) {
            throw new RuntimeException(exception);
        }
    }
}
