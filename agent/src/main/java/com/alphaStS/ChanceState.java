package com.alphaStS;

import com.alphaStS.utils.BigRational;
import com.alphaStS.utils.Tuple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ChanceState implements State {
    public long getCount(GameState state) {
        return cache.get(state).n;
    }

    static class Node {
        GameState state;
        long n;
        long other_n;
        VArray prev_q;

        public Node(GameState state, int startN) {
            this.state = state;
            prev_q = new VArray(state.properties.v_total_len);
            n = startN;
        }

        public Node(GameState state, boolean n) {
            this.state = state;
        }

        public void init() {
            prev_q = new VArray(state.properties.v_total_len);
        }
    }

    Map<GameState, Node> cache;
    HashMap<GameState, Node> otherCache;
    Node[] nodesArr;
    AtomicInteger currentNodeArrIdx;
    AtomicInteger currentNodeArrCommittedIdx;
    AtomicInteger uniqueNodeEntry;
    Node prevWidenedNode;
    long total_node_n; // actual n, sum of nodes' n in cache
    long total_n; // n called from parent
    VArray total_q;
    VArray total_node_q;
    double varianceM;
    double varianceS;
    List<GameState> queue;
    GameState parentState;
    int parentAction;
    RandomGen searchRandomGen;
    ReentrantReadWriteLock statsLock;
    ReentrantReadWriteLock nodesArrLock;
    ReentrantLock randomGenLock;
    public AtomicInteger virtualLoss;

    public void readLock() {
        statsLock.readLock().lock();
    }

    public void readUnlock() {
        statsLock.readLock().unlock();
    }

    public ChanceState(GameState initState, GameState parentState, int action) {
        if (parentState.properties.multithreadedMTCS) {
            statsLock = new ReentrantReadWriteLock();
            randomGenLock = new ReentrantLock();
            virtualLoss = new AtomicInteger(1);
            cache = new ConcurrentHashMap<>();
            if (Configuration.USE_PROGRESSIVE_WIDENING) {
                nodesArr = new Node[5];
                currentNodeArrIdx = new AtomicInteger(0);
                currentNodeArrCommittedIdx = new AtomicInteger(0);
                nodesArrLock = new ReentrantReadWriteLock();
                uniqueNodeEntry = new AtomicInteger(0);
                total_node_q = new VArray(parentState.properties.v_total_len);
            }
        } else {
            cache = new HashMap<>();
            if (initState != null) {
                cache.put(initState, new Node(initState, 1));
                total_node_n = 1;
                total_n = 1;
            }
        }
        otherCache = new HashMap<>();
        if (Configuration.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION && initState != null) {
            if (Configuration.NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION && (!Configuration.TEST_NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION || parentState.properties.testNewFeature)) {
                searchRandomGen = initState.getSearchRandomGen().getCopy();
            } else {
                searchRandomGen = initState.getSearchRandomGen().createWithSeed(initState.getSearchRandomGen().nextLong(RandomGenCtx.CommonNumberVR));
            }
        } else {
            searchRandomGen = parentState.properties.random;
        }
        this.parentState = parentState;
        this.parentAction = action;
        total_q = new VArray(parentState.properties.v_total_len);
        queue = new ArrayList<>();
    }

    public void addToQueue(GameState state) {
        addGeneratedState(state);
        total_n -= 1;
        queue.add(state);
    }

    public void correctV(GameState state2, VArray v, VArray realV) {
        if (parentState.properties.multithreadedMTCS) virtualLoss.decrementAndGet();
        var newVarianceM = varianceM + (realV.get(GameState.V_COMB_IDX) - varianceM) / total_n;
        var newVarianceS = varianceS + (realV.get(GameState.V_COMB_IDX) - varianceM) * (realV.get(GameState.V_COMB_IDX) - newVarianceM);
        varianceM = newVarianceM;
        varianceS = newVarianceS;
        var node = cache.get(state2);
        if ((Configuration.USE_PROGRESSIVE_WIDENING && (!Configuration.TEST_PROGRESSIVE_WIDENING || parentState.properties.testNewFeature)) ||
                Configuration.isTranspositionAcrossChanceNodeOn(parentState) ||
                (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || parentState.properties.testNewFeature))) {
//            var new_total_q = new double[node.state.prop.v_total_len];
//            var nnn = 0;
//            for (Map.Entry<GameState, Node> entry : cache.entrySet()) {
//                for (int i = 0; i < node.state.prop.v_total_len; i++) {
//                    new_total_q[i] += entry.getValue().state.getTotalQ(i) / (entry.getValue().state.total_n + 1) * entry.getValue().n;
//                }
//                nnn += entry.getValue().n;
//            }
//            for (int i = 0; i < node.state.prop.v_total_len; i++) {
//                new_total_q[i] = new_total_q[i] / nnn * total_n;
//                v.set(i, new_total_q[i] - total_q[i]);
//            }

            for (int i = 0; i < node.state.properties.v_total_len; i++) {
                var node_cur_q = node.state.getTotalQ(i) / (node.state.total_n + 1);
                var prev_q = total_n == 1 ? 0 : total_q.get(i) / (total_n - 1);
                var new_q = (prev_q * (total_node_n - 1) - node.prev_q.get(i) * (node.n - 1)) / total_node_n + node_cur_q * node.n / total_node_n;
                node.prev_q.set(i, node_cur_q);
                v.set(i, new_q * total_n - total_q.get(i));
            }
        }
        for (int i = 0; i < node.state.properties.v_total_len; i++) {
            total_q.add(i, v.get(i));
        }
    }

    GameState getNextState(boolean calledFromMCTS, int level) {
        boolean useProgressiveWidening = calledFromMCTS && Configuration.USE_PROGRESSIVE_WIDENING && (!Configuration.TEST_PROGRESSIVE_WIDENING || parentState.properties.testNewFeature);
        total_n += 1;
        if (queue != null && queue.size() > 0) {
            return queue.remove(0);
        }

        if (useProgressiveWidening && cache.size() >= Math.ceil(Math.pow(total_n, 0.35)) && false) {
            // instead of generating new nodes, revisit node, need testing
            var r = (long) searchRandomGen.nextInt((int) total_node_n, RandomGenCtx.Other, this);
            var acc = 0;
            for (Map.Entry<GameState, Node> entry : cache.entrySet()) {
                acc += entry.getValue().n;
                if (acc > r) {
                    return entry.getValue().state;
                }
            }
            Integer.parseInt(null);
        }
        if (useProgressiveWidening && Configuration.PROGRESSIVE_WIDENING_IMPROVEMENTS) {
            if (prevWidenedNode != null) {
                prevWidenedNode.n += 1;
                total_node_n += 1;
                var ret = prevWidenedNode.state;
                if (prevWidenedNode.n == prevWidenedNode.other_n) {
                    prevWidenedNode = null;
                }
                return ret;
            }
        }

        var state = parentState.clone(true);
        if (!parentState.properties.makingRealMove && Configuration.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION) {
            state.setSearchRandomGen(searchRandomGen);
        }
        state = state.doAction(parentAction);
        if ((Configuration.COMBINE_END_AND_BEGIN_TURN_FOR_STOCHASTIC_BEGIN || !state.isStochastic) && state.actionCtx == GameActionCtx.BEGIN_TURN) {
            state = state.doAction(0);
        }
        if (Configuration.NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION && (!Configuration.TEST_NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION || parentState.properties.testNewFeature)) {
            if (!parentState.properties.makingRealMove && Configuration.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION) {
                searchRandomGen = searchRandomGen.getCopy();
            }
        } else {
            if (!parentState.properties.makingRealMove && Configuration.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION) {
                searchRandomGen = state.getSearchRandomGen().createWithSeed(state.getSearchRandomGen().nextLong(RandomGenCtx.CommonNumberVR));
            }
        }
        if (parentState.properties.makingRealMove && Configuration.NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION) {
            state.setSearchRandomGen(state.getSearchRandomGen().createWithSeed(state.getSearchRandomGen().nextLong(RandomGenCtx.CommonNumberVR)));
            total_n -= 1;
            return state;
        }

        var node = cache.get(state);
        if (node != null) {
            node.n += 1;
            total_node_n += 1;
            if (node.state.stateDesc == null && state.stateDesc != null) {
                node.state.stateDesc = state.stateDesc;
            }
            if (parentState.properties.makingRealMove) {
                if (Configuration.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION) {
                    node.state.setSearchRandomGen(state.getSearchRandomGen().createWithSeed(state.getSearchRandomGen().nextLong(RandomGenCtx.CommonNumberVR)));
                }
                if (state.stateDesc != null) {
                    node.state.stateDesc = new StringBuilder(state.stateDesc);
                }
                if (Configuration.DO_NOT_USE_CACHED_STATE_WHEN_MAKING_REAL_MOVE) {
                    state.setSearchRandomGen(state.getSearchRandomGen().createWithSeed(state.getSearchRandomGen().nextLong(RandomGenCtx.CommonNumberVR)));
                    total_n -= 1;
                    return state;
                }
            }
            return node.state;
        }
        double x = 0.5;
        if (useProgressiveWidening && Configuration.PROGRESSIVE_WIDENING_IMPROVEMENTS2) {
            x = level > 0 ? Math.max(x - level * 0.05, 0.15) : x;
        }
        if (useProgressiveWidening && cache.size() >= Math.ceil(Math.pow(total_n, x))) {
            if (Configuration.PROGRESSIVE_WIDENING_IMPROVEMENTS) {
                node = otherCache.get(state);
                if (node == null) {
                    node = new Node(state, false);
                    otherCache.put(state, node);
                }
                node.other_n += 1;
            }
            // instead of generating new nodes, revisit node, need testing
            var r = (long) searchRandomGen.nextInt((int) total_node_n, RandomGenCtx.Other, this);
            var acc = 0;
            for (Map.Entry<GameState, Node> entry : cache.entrySet()) {
                acc += entry.getValue().n;
                if (acc > r) {
                    return entry.getValue().state;
                }
            }
            Integer.parseInt(null);
        }
        if (useProgressiveWidening && Configuration.PROGRESSIVE_WIDENING_IMPROVEMENTS) {
            node = otherCache.get(state);
            if (node == null) {
                total_node_n += 1;
                cache.put(state, new Node(state, 1));
            } else {
                cache.put(state, node);
                otherCache.remove(state);
                prevWidenedNode = node;
                state = node.state;
                node.init();
                node.n += 1;
                node.other_n += 1;
                total_node_n += 1;
                if (node.state.stateDesc == null && state.stateDesc != null) {
                    node.state.stateDesc = state.stateDesc;
                }
            }
        } else {
            total_node_n += 1;
            cache.put(state, new Node(state, 1));
            if (parentState.properties.makingRealMove && Configuration.DO_NOT_USE_CACHED_STATE_WHEN_MAKING_REAL_MOVE) {
                state.setSearchRandomGen(state.getSearchRandomGen().createWithSeed(state.getSearchRandomGen().nextLong(RandomGenCtx.CommonNumberVR)));
                total_n -= 1;
                return state;
            }
        }
        if (parentState.properties.makingRealMove && Configuration.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION) {
            state.setSearchRandomGen(state.getSearchRandomGen().createWithSeed(state.getSearchRandomGen().nextLong(RandomGenCtx.CommonNumberVR)));
        }
        return state;
    }

    Tuple<Node, Boolean> getNextStateParallel() {
        virtualLoss.incrementAndGet();

        var state = parentState.clone(true);
        if (Configuration.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION) {
            randomGenLock.lock();
            var gen = searchRandomGen;
            searchRandomGen = gen.createWithSeed(gen.nextLong(RandomGenCtx.CommonNumberVR));
            randomGenLock.unlock();
            state.setSearchRandomGen(gen);
        }
        state = state.doAction(parentAction);
        if ((Configuration.COMBINE_END_AND_BEGIN_TURN_FOR_STOCHASTIC_BEGIN || !state.isStochastic) && state.actionCtx == GameActionCtx.BEGIN_TURN) {
            state = state.doAction(0);
        }

        var _s = state;
        if (!Configuration.USE_PROGRESSIVE_WIDENING) {
            return new Tuple<>(cache.computeIfAbsent(state, k -> new Node(_s, 0)), Boolean.FALSE);
        } else {
            Node n = cache.computeIfAbsent(state, k -> new Node(_s, 0));
            if (n.n > 0) {
                return new Tuple<>(n, Boolean.FALSE);
            }
            if (total_n > 0 && uniqueNodeEntry.get() >= Math.ceil(Math.pow(total_n, 0.5))) {
                var r = currentNodeArrCommittedIdx.getPlain();
                while (r < nodesArr.length && nodesArr[r] != null && currentNodeArrCommittedIdx.compareAndExchange(r, r + 1) == r + 1) {
                    r++;
                }
                if (r > 0) {
                    return new Tuple<>(nodesArr[state.properties.random.nextInt(r, RandomGenCtx.Other, this)], Boolean.TRUE);
                }
            }
            return new Tuple<>(n, Boolean.FALSE);
        }
    }

    public void correctVParallel(Node node, boolean revisitFromProgressive, VArray v, VArray realV) {
        if (parentState.properties.multithreadedMTCS) virtualLoss.decrementAndGet();
        if ((Configuration.USE_PROGRESSIVE_WIDENING && (!Configuration.TEST_PROGRESSIVE_WIDENING || parentState.properties.testNewFeature)) ||
                Configuration.isTranspositionAcrossChanceNodeOn(parentState) ||
                (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || parentState.properties.testNewFeature))) {
            //            var new_total_q = new double[node.state.prop.v_total_len];
            //            var nnn = 0;
            //            for (Map.Entry<GameState, Node> entry : cache.entrySet()) {
            //                for (int i = 0; i < node.state.prop.v_total_len; i++) {
            //                    new_total_q[i] += entry.getValue().state.getTotalQ(i) / (entry.getValue().state.total_n + 1) * entry.getValue().n;
            //                }
            //                nnn += entry.getValue().n;
            //            }
            //            for (int i = 0; i < node.state.prop.v_total_len; i++) {
            //                new_total_q[i] = new_total_q[i] / nnn * total_n;
            //                v.set(i, new_total_q[i] - total_q[i]);
            //            }

            node.state.writeLock();
            var prevNodeN = node.n;
            if (!revisitFromProgressive) {
                node.n += 1;
            }
            for (int i = 0; i < node.state.properties.v_total_len; i++) {
                var node_cur_q = node.state.getTotalQ(i) / (node.state.total_n + 1);
                v.set(i, node_cur_q * node.n - node.prev_q.get(i) * prevNodeN);
                node.prev_q.set(i, node_cur_q);
            }
            node.state.writeUnlock();
            if (Configuration.USE_PROGRESSIVE_WIDENING && prevNodeN == 0) {
                uniqueNodeEntry.getAndIncrement();
            }
        }
        if (Configuration.USE_PROGRESSIVE_WIDENING && !revisitFromProgressive) {
            var nodeArrayIdx = currentNodeArrIdx.getAndIncrement();
            if (nodeArrayIdx >= nodesArr.length) {
                nodesArrLock.writeLock().lock();
                if (nodeArrayIdx >= nodesArr.length) {
                    nodesArr = Arrays.copyOf(nodesArr, Math.max(nodeArrayIdx + 1 + (nodeArrayIdx + 1) / 2, nodesArr.length + nodesArr.length / 2));
                }
                nodesArrLock.writeLock().unlock();
            }
            nodesArrLock.readLock().lock();
            nodesArr[nodeArrayIdx] = node;
            nodesArrLock.readLock().unlock();
            while (currentNodeArrCommittedIdx.compareAndExchange(nodeArrayIdx, nodeArrayIdx + 1) == nodeArrayIdx + 1) {
                nodeArrayIdx++;
                if (nodeArrayIdx >= nodesArr.length || nodesArr[nodeArrayIdx] == null) {
                    break;
                }
            }
        }
        statsLock.writeLock().lock();
        total_n++;
        if (!revisitFromProgressive) {
            total_node_n += 1;
        }
        var newVarianceM = varianceM + (realV.get(GameState.V_COMB_IDX) - varianceM) / total_n;
        var newVarianceS = varianceS + (realV.get(GameState.V_COMB_IDX) - varianceM) * (realV.get(GameState.V_COMB_IDX) - newVarianceM);
        varianceM = newVarianceM;
        varianceS = newVarianceS;
        for (int i = 0; i < node.state.properties.v_total_len; i++) {
            if (Configuration.USE_PROGRESSIVE_WIDENING) {
                var prevTotalQ = total_q.get(i);
                total_node_q.add(i, v.get(i));
                total_q.set(i, total_node_q.get(i) / total_node_n * total_n);
                v.set(i, total_q.get(i) - prevTotalQ);
            } else {
                total_q.add(i, v.get(i));
            }
        }
        statsLock.writeLock().unlock();
    }

    public Node addGeneratedStateParallel(GameState state) {
        return cache.computeIfAbsent(state, k -> new Node(state, 0));
    }

    public GameState addGeneratedState(GameState state) {
        var node = cache.get(state);
        if (node == null) {
            cache.put(state, new Node(state, 1));
            total_n += 1;
            total_node_n += 1;
            return state;
        }
        return node.state;
    }

    @Override
    public String toString() {
        return "ChanceState{state=" + parentState + ", action=" + parentAction + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChanceState that = (ChanceState) o;
        return parentAction == that.parentAction && Objects.equals(parentState, that.parentState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentState, parentAction);
    }
}
