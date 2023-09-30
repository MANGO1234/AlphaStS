package com.alphaStS.model;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OrtSession.Result;
import ai.onnxruntime.OrtSession.SessionOptions;
import ai.onnxruntime.OrtSession.SessionOptions.OptLevel;
import com.alphaStS.Configuration;
import com.alphaStS.GameActionCtx;
import com.alphaStS.GameState;
import com.alphaStS.utils.Utils;

import java.util.*;

public class ModelPlain implements Model {
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
    public LRUCache<NNInputHash, NNOutput> cache;
    public int calls;
    public int cache_hits;
    public long time_taken;

    /**
     * Naively takes the softmax of the input.
     *
     * @param input The input array.
     * @return The softmax of the input.
     */
    public static float[] softmax(float[] input) {
        double[] tmp = new double[input.length];
        float max = input[0];
        for (int i = 1; i < input.length; i++) {
            max = Math.max(max, input[i]);
        }
        double sum = 0.0;
        for (int i = 0; i < input.length; i++) {
            double val = Math.exp(input[i] - max);
            sum += val;
            tmp[i] = val;
        }
        for (int i = 0; i < input.length; i++) {
            input[i] = (float) (tmp[i] / sum);
        }
        return input;
    }

    public static void softmax(float[] input, int start, int len) {
        double[] tmp = new double[len];
        double max = input[0];
        for (int i = 1; i < len; i++) {
            max = Math.max(max, input[start + i]);
        }
        double sum = 0.0;
        for (int i = 0; i < len; i++) {
            double val = Math.exp(input[start + i] - max);
            sum += val;
            tmp[i] = val;
        }
        for (int i = 0; i < len; i++) {
            input[start + i] = (float) (tmp[i] / sum);
        }
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

    public ModelPlain(String modelDir) {
        try {
            env = OrtEnvironment.getEnvironment();
            if (Configuration.ONNX_USE_CUDA_FOR_INFERENCE) {
                System.setProperty("onnxruntime.native.path", Configuration.ONNX_LIB_PATH);
            }
            OrtSession.SessionOptions opts = new SessionOptions();
            opts.setOptimizationLevel(OptLevel.ALL_OPT);
            opts.setCPUArenaAllocator(true);
            opts.setMemoryPatternOptimization(true);
            opts.setExecutionMode(SessionOptions.ExecutionMode.SEQUENTIAL);
            opts.setInterOpNumThreads(1);
            opts.setIntraOpNumThreads(1);
            if (Configuration.ONNX_USE_CUDA_FOR_INFERENCE) {
                opts.addCUDA(0);
            }
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

    int prev;
    @Override public void startRecordCalls() {
        prev = calls - cache_hits;
    }

    @Override public int endRecordCalls() {
        return calls - cache_hits - prev;
    }

    public void resetStats() {
        calls = 0;
        cache_hits = 0;
        time_taken = 0;
    }

    public NNOutput eval(GameState state) {
        calls += 1;
        NNInputHash hash;
        try {
            hash = new NNInputHash(state.getNNInput());
        } catch (Exception e) {
            System.out.println(state);
            throw e;
        }
        NNOutput o = cache.get(hash);
        if (o != null) {
            if (!Arrays.equals(o.legalActions(), state.getLegalActions()) &&
                    (state.getActionCtx() != GameActionCtx.SELECT_ENEMY || state.prop.enemiesReordering == null)) {
                System.err.println(Arrays.toString(state.getNNInput()));
                System.err.println(state);
                System.err.println(Arrays.toString(o.legalActions()));
                System.err.println(Arrays.toString(state.getLegalActions()));
                for (int i = 0; i < state.getLegalActions().length; i++) {
                    System.err.println(state.getActionString(i));
                }
                Integer.parseInt(null);
            }
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
                if (state.getActionCtx() == GameActionCtx.PLAY_CARD) {
                    policy = Arrays.copyOf(policy, state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length);
                } else if (state.getActionCtx() == GameActionCtx.SELECT_ENEMY) {
                    int startIdx = state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length;
                    int lenToCopy = state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()].length;
                    policy = Utils.arrayCopy(policy, startIdx, lenToCopy);
                } else if (state.getActionCtx() == GameActionCtx.SELECT_SCENARIO) {
                    int startIdx = state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length;
                    if (state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null) {
                        startIdx += state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()].length;
                    }
                    int lenToCopy = state.prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()].length;
                    policy = Utils.arrayCopy(policy, startIdx, lenToCopy);
                }
            }
            if (state.getActionCtx() == GameActionCtx.SELECT_ENEMY) {
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

            float[] v_other = null;
            if (output.size() > 3 && state.prop.extraOutputLen > 0) {
                v_other = new float[state.prop.extraOutputLen];
                int idx = 0;
                for (int i = 0; i < state.prop.extraTrainingTargets.size(); i++) {
                    int n = state.prop.extraTrainingTargets.get(i).getNumberOfTargets();
                    if (n == 1) {
                        float value = ((float[][]) output.get(3 + i).getValue())[0][0];
                        if (state.prop.extraTrainingTargetsLabel.get(i).startsWith("Z")) {
                            v_other[idx] = value;
                        } else {
                            v_other[idx] = (value + 1) / 2;
                        }
                    } else {
                        float[] v = ((float[][]) output.get(3 + i).getValue())[0];
                        for (int j = 0; j < n; j++) {
                            v_other[idx + j] = v[j];
                        }
                        softmax(v_other, idx, n);
                    }
                    idx += n;
                }
            }

            v_health = (v_health + 1) / 2;
            v_win = (v_win + 1) / 2;
            o = new NNOutput(v_health, v_win, v_other, softmax(policyCompressed), legalActions);
            cache.put(hash, o);
            time_taken += System.currentTimeMillis() - start;
            return o;
        } catch (OrtException exception) {
            throw new RuntimeException(exception);
        }
    }

    public NNOutput tryUseCache(NNInputHash input) {
        var output = cache.get(input);
        if (output != null) {
            cache_hits++;
            calls++;
        }
        return output;
    }

    public List<NNOutput> eval(List<GameState> states, List<NNInputHash> hashes, int reqCount) {
        calls += reqCount;
        var x = new float[reqCount][];
        var outputs = new ArrayList<NNOutput>();
        var outputsIdx = new int[reqCount];
        var k = 0;
        for (int i = 0; i < states.size(); i++) {
            outputs.add(null);
            if (states.get(i) != null) {
                outputsIdx[k] = i;
                x[k++] = states.get(i).getNNInput();
            }
        }

        long start = System.currentTimeMillis();
        try (OnnxTensor test = OnnxTensor.createTensor(env, x);
                Result output = session.run(Collections.singletonMap(inputName, test))) {
            for (int row = 0; row < x.length; row++) {
                var state = states.get(outputsIdx[row]);
                float v_health = ((float[][]) output.get(0).getValue())[row][0];
                float v_win = ((float[][]) output.get(1).getValue())[row][0];

                float[] policy = ((float[][]) output.get(2).getValue())[row];
                if (state.prop.maxNumOfActions != state.prop.totalNumOfActions) {
                    if (state.getActionCtx() == GameActionCtx.PLAY_CARD) {
                        policy = Arrays.copyOf(policy, state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length);
                    } else if (state.getActionCtx() == GameActionCtx.SELECT_ENEMY) {
                        int startIdx = state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length;
                        int lenToCopy = state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()].length;
                        policy = Utils.arrayCopy(policy, startIdx, lenToCopy);
                    } else if (state.getActionCtx() == GameActionCtx.SELECT_SCENARIO) {
                        int startIdx = state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length;
                        if (state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null) {
                            startIdx += state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()].length;
                        }
                        int lenToCopy = state.prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()].length;
                        policy = Utils.arrayCopy(policy, startIdx, lenToCopy);
                    }
                }
                if (state.getActionCtx() == GameActionCtx.SELECT_ENEMY) {
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

                float[] v_other = null;
                if (output.size() > 3 && state.prop.extraOutputLen > 0) {
                    v_other = new float[state.prop.extraOutputLen];
                    int idx = 0;
                    for (int i = 0; i < state.prop.extraTrainingTargets.size(); i++) {
                        int n = state.prop.extraTrainingTargets.get(i).getNumberOfTargets();
                        if (n == 1) {
                            float value = ((float[][]) output.get(3 + i).getValue())[row][0];
                            if (state.prop.extraTrainingTargetsLabel.get(i).startsWith("Z")) {
                                v_other[idx] = value;
                            } else {
                                v_other[idx] = (value + 1) / 2;
                            }
                        } else {
                            float[] v = ((float[][]) output.get(3 + i).getValue())[row];
                            for (int j = 0; j < n; j++) {
                                v_other[idx + j] = v[j];
                            }
                            softmax(v_other, idx, n);
                        }
                        idx += n;
                    }
                }

                v_health = (v_health + 1) / 2;
                v_win = (v_win + 1) / 2;
                var o = new NNOutput(v_health, v_win, v_other, softmax(policyCompressed), legalActions);
                cache.put(hashes.get(row), o);
                outputs.set(outputsIdx[row], o);
            }
            time_taken += System.currentTimeMillis() - start;
            return outputs;
        } catch (OrtException exception) {
            throw new RuntimeException(exception);
        }
    }
}
