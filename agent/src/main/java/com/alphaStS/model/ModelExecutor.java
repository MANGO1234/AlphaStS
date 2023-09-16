package com.alphaStS.model;

import com.alphaStS.GameState;
import com.alphaStS.utils.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

// likely due to JNI, batching nn evaluations even on CPU is faster than executing 1 by 1 by 10x (small networks) to 3x (larger networks)
// currently, use producers threads to produce nn evaluations requests and executors to execute them
// todo: switch to continuations for better performance by removing context switch overheads from large amount of threads + locking/communication cost
// consideration: if I use C++ instead I don't have to do any of those, although the same batching logic can be used for GPU evaluation
public class ModelExecutor {
    public final static Integer EXEC_WAITING = 0;
    public final static Integer EXEC_SUCCESS = 1;
    public final static Integer EXEC_SUCCESS_CACHE_HIT = 2;

    private final List<Thread> executorThreads = new ArrayList<>();
    private final List<ModelPlain> executorModels = new ArrayList<>();
    private final List<Thread> producerThreads = new ArrayList<>();
    private final List<ModelBatchProducer> producerModels = new ArrayList<>();
    private final String modelDir;

    // ModelBatchProducer uses those for communication
    BlockingQueue<Tuple<Integer, GameState>> queue = new ArrayBlockingQueue<>(1000);
    List<Lock> locks = new ArrayList<>();
    List<Condition> conditions = new ArrayList<>();
    List<NNOutput> results = new ArrayList<>();
    List<Integer> resultsStatus = new ArrayList<>();
    private int currentBatchSize;
    private boolean running = true;

    public ModelExecutor(String modelDir) {
        this.modelDir = modelDir;
    }

    public void start(int executorCount, int batchSize) {
        if (executorCount >= executorModels.size()) {
            for (int i = executorModels.size(); i < executorCount; i++) {
                executorModels.add(new ModelPlain(modelDir));
            }
        }
        if (executorCount >= executorThreads.size()) {
            for (int i = executorThreads.size(); i < executorCount; i++) {
                executorThreads.add(null);
            }
        }

        currentBatchSize = batchSize;
        if (batchSize == 1) {
            return;
        }

        running = true;
        var _pool = this;
        for (int i = 0; i < executorCount; i++) {
            int _i = i;
            executorThreads.set(i, new Thread(() -> {
                List<Tuple<Integer, GameState>> reqs = new ArrayList<>();
                List<NNInputHash> hashes = new ArrayList<>();
                int reqCount = 0;
                while (_pool.running) {
                    try {
                        var req = queue.poll();
                        if (req != null) {
                            if (req.v2() != null) {
                                var hash = new NNInputHash(req.v2().getNNInput());
                                var output = executorModels.get(_i).tryUseCache(hash);
                                if (output != null) {
                                    locks.get(req.v1()).lock();
                                    results.set(req.v1(), output);
                                    resultsStatus.set(req.v1(), EXEC_SUCCESS_CACHE_HIT);
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
                        } else if (reqs.size() > 0) {
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
                    } catch (Exception e) {
                    }
                }
            }));
            executorThreads.get(i).start();
        }
    }

    public void stop() {
        running = false;
        for (Thread thread : producerThreads) {
            thread.interrupt();
        }
        for (Thread thread : producerThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        producerThreads.clear();
        if (currentBatchSize == 1) {
            return;
        }

        for (Thread thread : executorThreads) {
            thread.interrupt();
        }
        for (Thread thread : executorThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        executorThreads.clear();
        for (int i = 0; i < producerModels.size(); i++) {
            resultsStatus.set(i, EXEC_WAITING);
        }
        queue.clear();
    }

    public Model getModelForProducer(int producerIdx) {
        if (currentBatchSize == 1) {
            return executorModels.get(producerIdx);
        }
        for (int i = producerModels.size(); i <= producerIdx; i++) {
            producerModels.add(null);
            locks.add(null);
            conditions.add(null);
            results.add(null);
            resultsStatus.add(EXEC_WAITING);
        }
        producerModels.set(producerIdx, new ModelBatchProducer(this, producerIdx));
        locks.set(producerIdx, new ReentrantLock());
        conditions.set(producerIdx, locks.get(producerIdx).newCondition());
        results.set(producerIdx, null);
        resultsStatus.set(producerIdx, EXEC_WAITING);
        return producerModels.get(producerIdx);
    }

    public Thread addAndStartProducerThread(Runnable t) {
        producerThreads.add(new Thread(() -> {
            try {
                t.run();
            } catch (RuntimeException e) {
                if (!(e.getCause() instanceof InterruptedException)) {
                    throw e;
                }
            }
        }));
        producerThreads.get(producerThreads.size() - 1).start();
        return producerThreads.get(producerThreads.size() - 1);
    }

    public boolean producerWaitForClose(int producerIdx) {
        try {
            if (currentBatchSize == 1 || !running) {
                return true;
            }
            producerModels.get(producerIdx).eval(null);
        } catch (Exception e) {
            if (e.getCause() instanceof InterruptedException) {
                return !running;
            }
            throw e;
        }
        return !running;
    }

    public List<ModelPlain> getExecutorModels() {
        return new ArrayList<>(this.executorModels);
    }

    public void close() {
        for (int i = 0; i < executorModels.size(); i++) {
            executorModels.get(i).close();
        }
    }

    public static int getNumberOfProducers(int numberOfThreads, int batch) {
        return batch == 1 ? numberOfThreads : (numberOfThreads * batch + 2 * batch);
    }

    public void sleep(int ms) {
        try {
            if (ms > 0) Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setToNotRunning() {
        running = false;
    }
}
