package com.alphaStS.model;

import com.alphaStS.GameState;
import com.alphaStS.utils.Tuple;

import java.util.concurrent.TimeUnit;

public class ModelBatchProducer implements Model {
    private final ModelExecutor pool;
    private final int clientIdx;
    private int calls;
    private int cacheHits;

    public ModelBatchProducer(ModelExecutor modelExecutor, int clientIdx) {
        this.pool = modelExecutor;
        this.clientIdx = clientIdx;
    }

    @Override public NNOutput eval(GameState state) {
        if (state != null) {
            calls++;
        }
        pool.queue.offer(new Tuple<>(clientIdx, state));
        pool.locks.get(clientIdx).lock();
        while (pool.resultsStatus.get(clientIdx) == ModelExecutor.EXEC_WAITING) {
            try {
                pool.conditions.get(clientIdx).await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                pool.locks.get(clientIdx).unlock();
                throw new RuntimeException(e);
            }
        }
        pool.locks.get(clientIdx).unlock();
        if (pool.resultsStatus.get(clientIdx) == ModelExecutor.EXEC_SUCCESS_CACHE_HIT) {
            cacheHits++;
        }
        pool.resultsStatus.set(clientIdx, ModelExecutor.EXEC_WAITING);
        return pool.results.set(clientIdx, null);
    }

    @Override public void close() {
    }

    int prev;
    @Override public void startRecordCalls() {
        prev = calls - cacheHits;
    }

    @Override public int endRecordCalls() {
        return calls - cacheHits - prev;
    }
}
