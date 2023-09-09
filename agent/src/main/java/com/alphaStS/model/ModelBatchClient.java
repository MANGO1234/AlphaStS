package com.alphaStS.model;

import com.alphaStS.GameState;
import com.alphaStS.utils.Tuple;

public class ModelBatchClient implements Model {
    private final ModelBatchExecutorPool pool;
    private final int clientIdx;
    private int calls;
    private int cache_hits;

    public ModelBatchClient(ModelBatchExecutorPool modelBatchExecutorPool, int clientIdx) {
        this.pool = modelBatchExecutorPool;
        this.clientIdx = clientIdx;
    }

    @Override public NNOutput eval(GameState state) {
        if (state != null) {
            calls++;
        }
        pool.queue.offer(new Tuple<>(clientIdx, state));
        pool.locks.get(clientIdx).lock();
        while (pool.resultsStatus.get(clientIdx) == ModelBatchExecutorPool.EXEC_WAITING) {
            try {
                pool.conditions.get(clientIdx).await();
            } catch (InterruptedException e) {
                pool.locks.get(clientIdx).unlock();
                throw new RuntimeException(e);
            }
        }
        pool.locks.get(clientIdx).unlock();
        pool.resultsStatus.set(clientIdx, ModelBatchExecutorPool.EXEC_WAITING);
        return pool.results.set(clientIdx, null);
    }

    @Override public void close() {
    }

    int prev;
    @Override public void startRecordCalls() {
        prev = calls - cache_hits;
    }

    @Override public int endRecordCalls() {
        return calls - cache_hits - prev;
    }
}
