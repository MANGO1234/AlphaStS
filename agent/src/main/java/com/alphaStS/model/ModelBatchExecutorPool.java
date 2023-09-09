package com.alphaStS.model;

import com.alphaStS.GameState;
import com.alphaStS.utils.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class ModelBatchExecutorPool {
    public final static Integer EXEC_WAITING = 0;
    public final static Integer EXEC_SUCCESS = 1;

    private final List<Thread> executorThreads = new ArrayList<>();
    private final List<ModelPlain> executorModels = new ArrayList<>();
    private final List<Thread> clientThreads = new ArrayList<>();
    private final List<ModelBatchClient> clientModels = new ArrayList<>();
    BlockingQueue<Tuple<Integer, GameState>> queue = new ArrayBlockingQueue<>(1000);
    List<Lock> locks = new ArrayList<>();
    List<Condition> conditions = new ArrayList<>();
    List<NNOutput> results = new ArrayList<>();
    List<Integer> resultsStatus = new ArrayList<>();
    private final int batchSize;
    private boolean running = true;

    public ModelBatchExecutorPool(String modelDir, int executorCount, int batchSize) {
        this.batchSize = batchSize;
        for (int i = 0; i < executorCount; i++) {
            executorModels.add(new ModelPlain(modelDir));
            executorThreads.add(null);
        }
    }

    public void start() {
        var _pool = this;
        for (int i = 0; i < executorModels.size(); i++) {
            int _i = i;
            executorThreads.set(i, new Thread(() -> {
                List<Tuple<Integer, GameState>> reqs = new ArrayList<>();
                List<NNInputHash> hashes = new ArrayList<>();
                int reqCount = 0;
                while (_pool.running) {
                    try {
                        var req = queue.poll(100, TimeUnit.MILLISECONDS);
                        if (req != null) {
                            if (req.v2() != null) {
                                var hash = new NNInputHash(req.v2().getNNInput());
                                var output = executorModels.get(_i).tryUseCache(hash);
                                if (output != null) {
                                    locks.get(req.v1()).lock();
                                    results.set(req.v1(), output);
                                    resultsStatus.set(req.v1(), EXEC_SUCCESS);
                                    conditions.get(req.v1()).signal();
                                    locks.get(req.v1()).unlock();
                                    continue;
                                }
                                hashes.add(hash);
                                reqCount++; // toward the end, fake requests will come in to keep the executor running without delay for the real requests
                            }
                            reqs.add(req);
                            if (reqs.size() >= batchSize) {
                                if (reqCount > 0) {
                                    var outputs = executorModels.get(_i).eval(reqs.stream().map(Tuple::v2).collect(Collectors.toList()), hashes, reqCount);
                                    for (int resultsIdx = 0; resultsIdx < reqs.size(); resultsIdx++) {
                                        locks.get(reqs.get(resultsIdx).v1()).lock();
                                        results.set(reqs.get(resultsIdx).v1(), outputs.get(resultsIdx));
                                        resultsStatus.set(reqs.get(resultsIdx).v1(), EXEC_SUCCESS);
                                        conditions.get(reqs.get(resultsIdx).v1()).signal();
                                        locks.get(reqs.get(resultsIdx).v1()).unlock();
                                    }
                                } else {
                                    for (int resultsIdx = 0; resultsIdx < reqs.size(); resultsIdx++) {
                                        locks.get(reqs.get(resultsIdx).v1()).lock();
                                        resultsStatus.set(reqs.get(resultsIdx).v1(), EXEC_SUCCESS);
                                        conditions.get(reqs.get(resultsIdx).v1()).signal();
                                        locks.get(reqs.get(resultsIdx).v1()).unlock();
                                    }
                                }
                                reqs.clear();
                                hashes.clear();
                                reqCount = 0;
                            }
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }));
            executorThreads.get(i).start();
        }
    }

    public void stop() {
        running = false;
        for (Thread thread : clientThreads) {
            thread.interrupt();
        }
        clientThreads.clear();
        for (Thread thread : executorThreads) {
            thread.interrupt();
        }
        queue.clear();
    }

    public Model getModelForClient(int clientIdx) {
        for (int i = clientModels.size(); i <= clientIdx; i++) {
            clientModels.add(null);
            locks.add(null);
            conditions.add(null);
            results.add(null);
            resultsStatus.add(EXEC_WAITING);
        }
        clientModels.set(clientIdx, new ModelBatchClient(this, clientIdx));
        locks.set(clientIdx, new ReentrantLock());
        conditions.set(clientIdx, locks.get(clientIdx).newCondition());
        results.set(clientIdx, null);
        resultsStatus.set(clientIdx, EXEC_WAITING);
        return clientModels.get(clientIdx);
    }

    public Thread addClientThread(Thread t) {
        clientThreads.add(t);
        return t;
    }

    public List<Model> getExecutorModels() {
        return new ArrayList<>(this.executorModels);
    }
}
