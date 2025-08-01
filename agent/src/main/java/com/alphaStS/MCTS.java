package com.alphaStS;

import java.util.*;

import cc.mallet.types.Dirichlet;
import com.alphaStS.card.CardDefect;
import com.alphaStS.enemy.EnemyEncounter;
import com.alphaStS.model.Model;
import com.alphaStS.model.ModelPlain;
import com.alphaStS.utils.Tuple;
import com.alphaStS.utils.Utils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import static java.lang.Math.sqrt;

public class MCTS {
    public Map<Integer, VArray> exploredActions = null;
    public VArray exploredV;
    Model model;
    private VArray v; // v that will be modified during tree search to propagate upward
    private VArray realV; // v of the leaf node (used for variance calculation)
    private final int[] selectActionRet = new int[2]; // [0] is action selected, [1] is number of actions possible
    private boolean reachedGuaranteedWin; // win has been reached, propagate until first ChanceNode
    public int forceRootAction = -1; // use in some circumstance to force root action to be an action (e.g. to search a ChanceNode)

    int numberOfPossibleActions; // the number of possible actions at root, used by the caller

    public MCTS(Model model) {
        this.model = model;
    }

    void setModel(Model model) {
        this.model = model;
    }

    private static final int SEARCH_SUCCESS = 0;
    private static final int SEARCH_RETRY = 1;
    private static final int SEARCH_NO_ACTIONS_LEFT = 2;

    void search(GameState state, boolean training, int remainingCalls) {
        exploredV = null;
        if (v == null) {
            v = new VArray(state.properties.v_total_len);
            realV = new VArray(state.properties.v_total_len);
        }
        search2(state, training, remainingCalls);
    }

    void search2(GameState state, boolean training, int remainingCalls) {
        reachedGuaranteedWin = false;
        if (state.properties.multithreadedMTCS) {
            search2parallel_r(state, training, remainingCalls, true, 0, false, null);
        } else {
            search2_r(state, training, remainingCalls, true, 0, null);
        }
    }

    int search2_r(GameState state, boolean training, int remainingCalls, boolean isRoot, int level, List<Tuple<GameState, Integer>> deterministicPath) {
        if (exploredV != null) {
            v.copyFrom(exploredV);
            for (int i = 0; i < state.properties.v_total_len; i++) {
                state.initSearchInfoLeaf();
                state.addTotalQ(i, v.get(i));
            }
            return SEARCH_SUCCESS;
        }
        if (state.terminalAction >= 0) {
            v.setToQNormalized(state.getChildQArray(state.terminalAction), state.n[state.terminalAction]);
            realV.copyFrom(v);
            numberOfPossibleActions = 1;
            return SEARCH_SUCCESS;
        }
        if (state.isTerminal() != 0) {
            state.getVArray(v);
            realV.copyFrom(v);
            state.initSearchInfoLeaf();
            state.getTotalQArray().copyFrom(v);
            if (v.get(GameState.V_WIN_IDX) > 0.5 && cannotImproveState(state)) {
                reachedGuaranteedWin = true;
            }
            return SEARCH_SUCCESS;
        }
        if (state.policy == null) {
            state.doEval(model);
            state.getVArray(v);
            realV.copyFrom(v);
            state.initSearchInfoLeaf();
            state.getTotalQArray().copyFrom(v);
            numberOfPossibleActions = state.getLegalActions().length;
            state.varianceM = v.get(GameState.V_COMB_IDX);
            state.varianceS = 0;
            if (GameProperties.isHeartFight(state) && state.properties.switchBattleHandler != null && state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
                v.correctForSwitchBattle(state.total_n + 1);
            }
            return SEARCH_SUCCESS;
        }
        if (state.n == null) {
            state.initSearchInfo();
        }

        float[] policy = getPolicy(state, training, remainingCalls, isRoot);
        selectAction(state, policy, training, isRoot, true);
        int action = selectActionRet[0];
        int numberOfActions = selectActionRet[1];
        while (true) {
            if (numberOfActions == 0) {
                return SEARCH_NO_ACTIONS_LEFT;
            }
            if (!Configuration.isPrioritizeChanceNodesBeforeDeterministicInTreeOn(state) || state.ns[action] != null) {
                break;
            } else if (state.getAction(action).type() == GameActionType.END_TURN || state.getAction(action).type() == GameActionType.BEGIN_TURN) {
                break;
            }
            GameState newState = state.clone(false);
            newState = newState.doAction(action);
            if (newState.isStochastic && checkDeterministicPath(deterministicPath, state, action)) {
                addBannedAction(state, action);
                selectAction(state, policy, training, isRoot, false);
                action = selectActionRet[0];
                numberOfActions = selectActionRet[1];
            } else {
                break;
            }
        }

        State nextState = state.ns[action];
        GameState state2;
        if (isRoot && exploredActions != null && exploredActions.containsKey(action)) {
            exploredV = exploredActions.get(action);
        }
        if (nextState == null) {
            state2 = state.clone(true);
            state2 = state2.doAction(action);
            if (state2.isStochastic) {
                if (Configuration.isTranspositionAcrossChanceNodeOn(state)) {
                    var s = state.transpositions.get(state2);
                    if (s == null || s instanceof ChanceState) {
                        if (Configuration.COMBINE_END_AND_BEGIN_TURN_FOR_STOCHASTIC_BEGIN && state2.actionCtx == GameActionCtx.BEGIN_TURN) {
                            state2 = state2.doAction(0);
                        } else {
                            state.transpositions.put(state2, state2);
                            if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.properties.testNewFeature)) {
                                if (s == null) {
                                    var parents = new ArrayList<Tuple<GameState, Integer>>();
                                    state.transpositionsParent.put(state2, parents);
                                    parents.add(new Tuple<>(state, action));
                                } else {
                                    state.transpositionsParent.get(state2).add(new Tuple<>(state, action));
                                }
                            }
                        }
                    } else {
                        state2 = (GameState) s;
                        if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.properties.testNewFeature)) {
                            state.transpositionsParent.get(state2).add(new Tuple<>(state, action));
                        }
                    }
                } else {
                    if (Configuration.COMBINE_END_AND_BEGIN_TURN_FOR_STOCHASTIC_BEGIN && state2.actionCtx == GameActionCtx.BEGIN_TURN) {
                        state2 = state2.doAction(0);
                    }
                }
                state.ns[action] = new ChanceState(state2, state, action);
                this.search2_r(state2, training, remainingCalls, false, level + 1, null);
                ((ChanceState) (state.ns[action])).correctV(state2, v, realV);
            } else {
                var s = state.transpositions.get(state2);
                if (s == null) {
                    if (state2.actionCtx == GameActionCtx.BEGIN_TURN && state2.isTerminal() == 0) {
                        var parentState = state2;
                        state2 = parentState.clone(false);
                        state2 = state2.doAction(0);
                        if (state2.properties.frozenEye == null || !state2.properties.frozenEye.isRelicEnabledInScenario(state2) || state2.isStochastic) {
                            var cState = new ChanceState(state2, parentState, 0);
                            state.ns[action] = cState;
                            state.transpositions.put(parentState, cState);
                            if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.properties.testNewFeature)) {
                                var parents = new ArrayList<Tuple<GameState, Integer>>();
                                state.transpositionsParent.put(parentState, parents);
                                parents.add(new Tuple<>(state, action));
                            }
                            this.search2_r(state2, training, remainingCalls, false, level + 1, null);
                            cState.correctV(state2, v, realV);
                        } else {
                            state.ns[action] = state2;
                            state.transpositions.put(state2, state2);
                            if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.properties.testNewFeature)) {
                                var parents = new ArrayList<Tuple<GameState, Integer>>();
                                state.transpositionsParent.put(state2, parents);
                                parents.add(new Tuple<>(state, action));
                            }
                            this.search2_r(state2, training, remainingCalls, false, level, deterministicPathPush(deterministicPath, state, action));
                            deterministicPathPop(deterministicPath);
                        }
                    } else {
                        state.ns[action] = state2;
                        state.transpositions.put(state2, state2);
                        if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.properties.testNewFeature)) {
                            var parents = new ArrayList<Tuple<GameState, Integer>>();
                            state.transpositionsParent.put(state2, parents);
                            parents.add(new Tuple<>(state, action));
                        }
                        this.search2_r(state2, training, remainingCalls, false, level, deterministicPathPush(deterministicPath, state, action));
                        deterministicPathPop(deterministicPath);
                    }
                } else if (s instanceof GameState ns) {
                    if (Configuration.isBanTranspositionInTreeOn(state) && numberOfActions > 1 && !isRoot) {
                        addBannedAction(state, action);
                        return SEARCH_RETRY;
                    }
                    if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.properties.testNewFeature)) {
                        state.transpositionsParent.get(state2).add(new Tuple<>(state, action));
                    }
                    if (Configuration.isTranspositionAlwaysExpandNewNodeOn(state)) {
                        this.search2_r(ns, training, remainingCalls, false, level, deterministicPathPush(deterministicPath, state, action));
                        deterministicPathPop(deterministicPath);
                    }
                    state.ns[action] = ns;
                    v.setToQNormalized(ns.getTotalQArray(), (ns.total_n + 1));
                } else if (s instanceof ChanceState ns) {
                    if (Configuration.isBanTranspositionInTreeOn(state) && numberOfActions > 1 && !isRoot) {
                        addBannedAction(state, action);
                        return SEARCH_RETRY;
                    }
                    if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.properties.testNewFeature)) {
                        state.transpositionsParent.get(state2).add(new Tuple<>(state, action));
                    }
                    if (Configuration.isTranspositionAlwaysExpandNewNodeOn(state)) {
                        state2 = ns.getNextState(true, level);
                        this.search2_r(state2, training, remainingCalls, false, level + 1, null);
                        ns.correctV(state2, v, realV);
                    }
                    state.ns[action] = ns;
                    v.setToQNormalized(ns.total_q, ns.total_n);
                }
            }
        } else {
            if (nextState instanceof ChanceState cState) {
                if (state.n[action] < cState.total_n) {
                    if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.properties.testNewFeature)) {
                        state2 = cState.getNextState(true, level);
                        if (handleFailedSearch2(state, action, state2, training, remainingCalls, false, level + 1, null)) {
                            Integer.parseInt(null); // fail for now
                        }
                        cState.correctV(state2, v, realV);
                        if (true) {
                            var newS = state.clone(true);
                            newS = newS.doAction(action);
                            updateTranspositions(newS, state2, state, action);
                        } else {
                            Integer.parseInt(null);
                        }
                    }
                    if (Configuration.isTranspositionAlwaysExpandNewNodeOn(state)) {
                        state2 = cState.getNextState(true, level);
                        if (handleFailedSearch2(state, action, state2, training, remainingCalls, false, level + 1, null)) {
                            Integer.parseInt(null); // fail for now
                        }
                        cState.correctV(state2, v, realV);
                    }
                    v.setVarrayToDiffOfCurrentAccumulatedQAndTheoreticalChildQ(cState.total_q, cState.total_n, state.n[action] + 1, state.getChildQArray(action));
                } else {
                    state2 = cState.getNextState(true, level);
                    if (handleFailedSearch2(state, action, state2, training, remainingCalls, false, level + 1, null)) {
                        Integer.parseInt(null); // fail for now
                    }
                    cState.correctV(state2, v, realV);
                    if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.properties.testNewFeature)) {
                        var parents = state2.transpositionsParent.get(state2);
                        if (parents != null && parents.size() > 1) {
                            updateTranspositions(state2, state2, state, action);
                        }
                    }
                }
            } else if (nextState instanceof GameState nState) {
                if (state.n[action] < nState.total_n + 1) {
                    if (Configuration.UPDATE_TRANSPOSITIONS_ON_ALL_PATH && (!Configuration.TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH || state.properties.testNewFeature)) {
                        if (handleFailedSearch2(state, action, nState, training, remainingCalls, false, level, deterministicPathPush(deterministicPath, state, action))) {
                            return SEARCH_RETRY;
                        }
                        updateTranspositions(nState, nState, state, action);
                    } else if (Configuration.isTranspositionAlwaysExpandNewNodeOn(state)) {
                        if (handleFailedSearch2(state, action, nState, training, remainingCalls, false, level, deterministicPathPush(deterministicPath, state, action))) {
                            return SEARCH_RETRY;
                        }
                    }
                    v.setVarrayToDiffOfCurrentAccumulatedQAndTheoreticalChildQ(nState.getTotalQArray(), nState.total_n + 1, state.n[action] + 1, state.getChildQArray(action));
                } else {
                    if (handleFailedSearch2(state, action, nState, training, remainingCalls, false, level, deterministicPathPush(deterministicPath, state, action))) {
                       return SEARCH_RETRY;
                    }
                }
            }
        }

        if (GameProperties.isHeartFight(state) && state.properties.switchBattleHandler != null && state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
            v.correctForSwitchBattle(state.total_n + 1);
        }
        state.getChildQArray(action).addVArray(v);
        state.getTotalQArray().addVArray(v);
        state.n[action] += 1;
        state.total_n += 1;
        var newVarianceM = state.varianceM + (realV.get(GameState.V_COMB_IDX) - state.varianceM) / (state.total_n + 1);
        var newVarianceS = state.varianceS + (realV.get(GameState.V_COMB_IDX) - state.varianceM) * (realV.get(GameState.V_COMB_IDX) - newVarianceM);
        state.varianceM = newVarianceM;
        state.varianceS = newVarianceS;
        if (reachedGuaranteedWin) {
            if (state.ns[action] instanceof ChanceState) {
                reachedGuaranteedWin = false;
            } else {
                state.terminalAction = action;
                v.addVarrayToMatchCurrentAccumulatedQAndTheoreticalChildQ(state.getChildQArray(action), state.n[action], state.total_n + 1, state.getTotalQArray());
            }
        }
        numberOfPossibleActions = numberOfActions;
        return SEARCH_SUCCESS;
    }

    private boolean checkDeterministicPath(List<Tuple<GameState, Integer>> deterministicPath, GameState state, int action) {
        if (deterministicPath == null) {
            return false;
        }
        boolean debug = false;
        var rand = state.getSearchRandomGen().getCopy();
        var seen = new HashSet<GameState>();
        for (int i = 0; i < 100; i++) {
            var startingRand = new RandomGen.RandomGenByCtx(rand.nextLong(RandomGenCtx.Other));
            var finalState = state.clone(false);
            finalState.setSearchRandomGen(startingRand.getCopy());
            finalState = finalState.doAction(action);
            if (seen.contains(finalState)) {
                continue;
            }
            seen.add(finalState);

            GameAction stochasticFollowUpAction = null; // e.g. acrobatics, choose 1 out of 3 potions
            if (finalState.getActionCtx() == GameActionCtx.SELECT_ENEMY ||
                    finalState.getActionCtx() == GameActionCtx.SELECT_CARD_HAND ||
                    finalState.getActionCtx() == GameActionCtx.SELECT_CARD_EXHAUST ||
                    finalState.getActionCtx() == GameActionCtx.SELECT_CARD_DECK ||
                    finalState.getActionCtx() == GameActionCtx.SELECT_CARD_DISCARD ||
                    finalState.getActionCtx() == GameActionCtx.SELECT_CARD_1_OUT_OF_3) {
                int tmp = rand.nextInt(finalState.getLegalActions().length, RandomGenCtx.Other);
                stochasticFollowUpAction = finalState.getAction(tmp);
                finalState = finalState.doAction(tmp);
            }

            int causalAction = deterministicPath.size();
            if (state.getAction(action).type() == GameActionType.SELECT_ENEMY ||
                    state.getAction(action).type() == GameActionType.SELECT_CARD_HAND ||
                    state.getAction(action).type() == GameActionType.SELECT_CARD_EXHAUST ||
                    state.getAction(action).type() == GameActionType.SELECT_CARD_DECK ||
                    state.getAction(action).type() == GameActionType.SELECT_CARD_DISCARD ||
                    state.getAction(action).type() == GameActionType.SELECT_CARD_1_OUT_OF_3) {
                for (int dIdx = deterministicPath.size() - 1; dIdx >= 0; dIdx--) {
                    var actionOb = deterministicPath.get(dIdx).v1().getAction(deterministicPath.get(dIdx).v2());
                    if (actionOb.type() == GameActionType.PLAY_CARD || actionOb.type() == GameActionType.USE_POTION){
                        causalAction = dIdx;
                        break;
                    }
                }
            }
            boolean success = false;
            for (int dIdx = causalAction - 1; dIdx >= 0; dIdx--) {
                if (deterministicPath.get(dIdx).v1().actionCtx == GameActionCtx.SELECT_ENEMY ||
                        deterministicPath.get(dIdx).v1().actionCtx == GameActionCtx.SELECT_CARD_DECK ||
                        deterministicPath.get(dIdx).v1().actionCtx == GameActionCtx.SELECT_CARD_HAND ||
                        deterministicPath.get(dIdx).v1().actionCtx == GameActionCtx.SELECT_CARD_DISCARD) {
                    continue;
                }
                if (!cannotPlayStochasticActionEarlier(deterministicPath, dIdx, causalAction, state, action, stochasticFollowUpAction, finalState, startingRand, debug)) {
                    success = true;
                    break;
                }
            }
            if (!success) {
                return false;
            }
        }
        if (debug) {
            System.out.println("banned action: " + state.getActionString(action) + " from " + state);
        }
        return true;
    }

    private static boolean cannotPlayStochasticActionEarlier(List<Tuple<GameState, Integer>> deterministicPath, int dIdx, int causalActionIdx, GameState state, int action, GameAction stochasticFollowUpAction, GameState finalState, RandomGen.RandomGenByCtx startingRand, boolean debug) {
        var replayState = deterministicPath.get(dIdx).v1().clone(false);
        replayState.setSearchRandomGen(startingRand.getCopy());
        var len = replayState.getLegalActions().length;
        var found = false;
        for (int j = causalActionIdx; j < deterministicPath.size(); j++) {
            var originalAction = deterministicPath.get(j).v1().getAction(deterministicPath.get(j).v2());
            len = replayState.getLegalActions().length;
            found = false;
            for (int k = 0; k < len; k++) {
                if (replayState.getAction(k).equals(originalAction)) {
                    replayState.isStochastic = false;
                    replayState = replayState.doAction(k);
                    replayState.clearAllSearchInfo();
                    found = true;
                    break;
                }
            }
            if (!found || replayState.isStochastic) {
                if (debug) {
                    System.out.println("failed 4 " + !found + ", " + replayState.isStochastic + ", " + j);
                }
                return true;
            }
        }
        len = replayState.getLegalActions().length;
        found = false;
        for (int k = 0; k < len; k++) {
            try {
                if (replayState.getAction(k).equals(state.getAction(action))) {
                    replayState = replayState.doAction(k);
                    if (stochasticFollowUpAction != null) {
                        len = replayState.getLegalActions().length;
                        found = false;
                        for (int l = 0; l < len; l++) {
                            if (replayState.getAction(l).equals(stochasticFollowUpAction)) {
                                replayState = replayState.doAction(l);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            if (debug) {
                                System.out.println("failed 5");
                            }
                            return true;
                        }
                    }
                    replayState.clearAllSearchInfo();
                    found = true;
                    break;
                }
            } catch (Exception e) {
                System.out.println(replayState + ", " + k + ", " + state + ", " + action);
                if (k < replayState.getLegalActions().length) {
                    System.out.println("replay: " + replayState.getActionString(k));
                }
                if (action < state.getLegalActions().length) {
                    System.out.println("state: " + state.getActionString(action));
                }
                System.out.println(finalState);
                for (int i = 0; i < deterministicPath.size(); i++) {
                    System.out.println(i + ", " + deterministicPath.get(i) + ", " + deterministicPath.get(i).v1().getActionString(deterministicPath.get(i).v2()));
                }
                System.out.println(dIdx);
                System.out.println(causalActionIdx);
                throw e;
            }
        }
        if (!found || !replayState.isStochastic) {
            if (debug) {
                System.out.println("failed 1");
            }
            return true;
        }
        for (int j = dIdx; j < causalActionIdx; j++) {
            var isTerminal = replayState.isTerminal();
            if (isTerminal != 0) {
                return isTerminal == finalState.isTerminal() && replayState.equals(finalState);
            }
            var originalAction = deterministicPath.get(j).v1().getAction(deterministicPath.get(j).v2());
            len = replayState.getLegalActions().length;
            found = false;
            for (int k = 0; k < len; k++) {
                if (replayState.getAction(k).equals(originalAction)) {
                    replayState.isStochastic = false;
                    replayState = replayState.doAction(k);
                    replayState.clearAllSearchInfo();
                    found = true;
                    break;
                }
            }
            if (!found || replayState.isStochastic) {
                if (debug) {
                    System.out.println("failed 2 " + found + " " + replayState.isStochastic + " " + deterministicPath.get(j).v1().getActionString(deterministicPath.get(j).v2()));
                }
                return true;
            }
        }
        if (!replayState.equals(finalState)) {
            if (debug) {
                System.out.println("failed 3 " + replayState + " " + finalState + " xx " + state);
            }
            return true;
        }
        return false;
    }

    private List<Tuple<GameState, Integer>> deterministicPathPush(List<Tuple<GameState, Integer>> deterministicPath, GameState state, int action) {
        if (!Configuration.isPrioritizeChanceNodesBeforeDeterministicInTreeOn(state)) {
            return null;
        }
        if (deterministicPath == null) {
            deterministicPath = new ArrayList<>();
        }
        deterministicPath.add(new Tuple<>(state, action));
        return deterministicPath;
    }

    private void deterministicPathPop(List<Tuple<GameState, Integer>> deterministicPath) {
        if (deterministicPath != null) {
            deterministicPath.remove(deterministicPath.size() - 1);
        }
    }

    private boolean handleFailedSearch2(GameState state, int action, GameState childState, boolean training, int remainingCalls, boolean isRoot, int level, List<Tuple<GameState, Integer>> deterministicPath) {
        while (true) {
            int ret = search2_r(childState, training, remainingCalls, isRoot, level, deterministicPath);
            if (ret == SEARCH_SUCCESS) {
                return false;
            } else if (ret == SEARCH_NO_ACTIONS_LEFT) {
                addBannedAction(state, action);
                return true;
            }
        }
    }

    int search2parallel_r(GameState state, boolean training, int remainingCalls, boolean isRoot, int level, boolean addVirtualLoss, List<Tuple<GameState, Integer>> deterministicPath) {
        if (state.terminalAction >= 0) {
            state.readLock();
            v.setToQNormalized(state.getChildQArray(state.terminalAction), state.n[state.terminalAction]);
            state.readUnlock();
            realV.copyFrom(v);
            numberOfPossibleActions = 1;
            return SEARCH_SUCCESS;
        }

        if (state.isTerminal() != 0) {
            state.getVArray(v);
            realV.copyFrom(v);
            state.initSearchInfoLeaf();
            state.getTotalQArray().copyFrom(v);;
            if (v.get(GameState.V_WIN_IDX) > 0.5 && cannotImproveState(state)) {
                reachedGuaranteedWin = true;
            }
            return SEARCH_SUCCESS;
        }

        if (state.policy == null) {
            state.writeLock();
            if (state.policy == null) {
                state.doEval(model);
                state.getVArray(v);
                realV.copyFrom(v);
                state.initSearchInfoLeaf();
                state.getTotalQArray().copyFrom(v);;
                numberOfPossibleActions = state.getLegalActions().length;
                state.varianceM = v.get(GameState.V_COMB_IDX);
                state.varianceS = 0;
                state.writeUnlock();
                if (GameProperties.isHeartFight(state) && state.properties.switchBattleHandler != null && state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
                    v.correctForSwitchBattle(state.total_n + 1);
                }
                return SEARCH_SUCCESS;
            }
            state.writeUnlock();
        }
        if (state.ns == null) {
            state.writeLock();
            if (state.ns == null) {
                state.initSearchInfo();
            }
            state.writeUnlock();
        }
        if (addVirtualLoss) {
            state.virtualLoss.incrementAndGet();
        }

        float[] policy = getPolicy(state, training, remainingCalls, isRoot);
        selectAction(state, policy, training, isRoot, true);
        int action = selectActionRet[0];
        int numberOfActions = selectActionRet[1];
        while (true) {
            if (numberOfActions == 0) {
                if (addVirtualLoss) {
                    state.virtualLoss.decrementAndGet();
                }
                return SEARCH_NO_ACTIONS_LEFT;
            }
            if (!Configuration.isPrioritizeChanceNodesBeforeDeterministicInTreeOn(state) || state.ns[action] != null) {
                break;
            } else if (state.getAction(action).type() == GameActionType.END_TURN || state.getAction(action).type() == GameActionType.BEGIN_TURN) {
                break;
            }
            state.readLock();
            GameState newState = state.clone(false);
            state.readUnlock();
            newState = newState.doAction(action);
            if (newState.isStochastic && checkDeterministicPath(deterministicPath, state, action)) {
                state.writeLock();
                addBannedAction(state, action);
                state.writeUnlock();
                selectAction(state, policy, training, isRoot, false);
                action = selectActionRet[0];
                numberOfActions = selectActionRet[1];
            } else {
                break;
            }
        }

        State nextState = state.ns[action];
        if (nextState == null) {
            state.writeLock();
            nextState = state.ns[action];
            if (nextState != null) {
                state.writeUnlock();
            }
        }
        GameState state2;
        if (nextState == null) {
            state2 = state.clone(true);
            state2 = state2.doAction(action);
            if (state2.isStochastic) {
                if (Configuration.isTranspositionAcrossChanceNodeOn(state)) {
                    state.transpositionsLock.lock();
                    var s = state.transpositions.get(state2);
                    if (s == null || s instanceof ChanceState) {
                        if (Configuration.COMBINE_END_AND_BEGIN_TURN_FOR_STOCHASTIC_BEGIN && state2.actionCtx == GameActionCtx.BEGIN_TURN) {
                            state2 = state2.doAction(0);
                        } else {
                            state.transpositions.put(state2, state2);
                        }
                    } else {
                        state2 = (GameState) s;
                    }
                    state.transpositionsLock.unlock();
                    var cState = new ChanceState(state2, state, action);
                    var node = cState.addGeneratedStateParallel(state2);
                    state.ns[action] = cState;
                    state.writeUnlock();
                    if (handleFailedSearch2Parallel(state, action, node.state, training, remainingCalls, false, level + 1, false, null)) {
                        Integer.parseInt(null); // fail for now
                    }
                    cState.correctVParallel(node, false, v, realV);
                } else {
                    if (Configuration.COMBINE_END_AND_BEGIN_TURN_FOR_STOCHASTIC_BEGIN && state2.actionCtx == GameActionCtx.BEGIN_TURN) {
                        state2 = state2.doAction(0);
                    }
                    var cState = new ChanceState(state2, state, action);
                    var node = cState.addGeneratedStateParallel(state2);
                    state.ns[action] = cState;
                    state.writeUnlock();
                    if (handleFailedSearch2Parallel(state, action, node.state, training, remainingCalls, false, level + 1, false, null)) {
                        Integer.parseInt(null); // fail for now
                    }
                    cState.correctVParallel(node, false, v, realV);
                }
            } else {
                state.transpositionsLock.lock();
                if (state.bannedActions != null && state.bannedActions[action]) {
                    state.transpositionsLock.unlock();
                    state.writeUnlock();
                    if (addVirtualLoss) {
                        state.virtualLoss.decrementAndGet();
                    }
                    return SEARCH_RETRY;
                }
                var s = state.transpositions.get(state2);
                if (s == null) {
                    if (state2.actionCtx == GameActionCtx.BEGIN_TURN && state2.isTerminal() == 0) {
                        var parentState = state2;
                        state2 = parentState.clone(false);
                        state2 = state2.doAction(0);
                        if (state2.properties.frozenEye == null || !state2.properties.frozenEye.isRelicEnabledInScenario(state2) || state2.isStochastic) {
                            var cState = new ChanceState(state2, parentState, 0);
                            var node = cState.addGeneratedStateParallel(state2);
                            state.transpositions.put(parentState, cState);
                            state.transpositionsLock.unlock();
                            state.ns[action] = cState;
                            state.writeUnlock();
                            this.search2parallel_r(node.state, training, remainingCalls, false, level + 1, false, null);
                            cState.correctVParallel(node, false, v, realV);
                        } else {
                            state.transpositions.put(parentState, state2);
                            state.transpositionsLock.unlock();
                            state.ns[action] = state2;
                            state.writeUnlock();
                            if (handleFailedSearch2Parallel(state, action, state2, training, remainingCalls, false, level, true, deterministicPathPush(deterministicPath, state, action))) {
                                if (addVirtualLoss) {
                                    state.virtualLoss.decrementAndGet();
                                }
                                return SEARCH_RETRY;
                            }
                        }
                    } else {
                        state.transpositions.put(state2, state2);
                        state.transpositionsLock.unlock();
                        state.ns[action] = state2;
                        state.writeUnlock();
                        if (handleFailedSearch2Parallel(state, action, state2, training, remainingCalls, false, level, true, deterministicPathPush(deterministicPath, state, action))) {
                            if (addVirtualLoss) {
                                state.virtualLoss.decrementAndGet();
                            }
                            return SEARCH_RETRY;
                        }
                    }
                } else if (s instanceof GameState ns) {
                    if (Configuration.isBanTranspositionInTreeOn(state) && numberOfActions > 1 && !isRoot && !(state.actionCtx == GameActionCtx.SELECT_CARD_1_OUT_OF_3)) {
                        addBannedAction(state, action);
                        state.transpositionsLock.unlock();
                        state.writeUnlock();
                        if (addVirtualLoss) {
                            state.virtualLoss.decrementAndGet();
                        }
                        return SEARCH_RETRY;
                    }
                    state.transpositionsLock.unlock();
                    state.ns[action] = ns;
                    state.writeUnlock();
                    if (!ns.isQInitialized() || Configuration.isTranspositionAlwaysExpandNewNodeOn(state)) {
                        if (handleFailedSearch2Parallel(state, action, ns, training, remainingCalls, false, level, true, deterministicPathPush(deterministicPath, state, action))) {
                            if (addVirtualLoss) {
                                state.virtualLoss.decrementAndGet();
                            }
                            return SEARCH_RETRY;
                        }
                    }
                    ns.readLock();
                    v.setToQNormalized(ns.getTotalQArray(), (ns.total_n + 1));
                    ns.readUnlock();
                } else if (s instanceof ChanceState ns) {
                    if (Configuration.isBanTranspositionInTreeOn(state) && numberOfActions > 1 && !isRoot) {
                        addBannedAction(state, action);
                        state.transpositionsLock.unlock();
                        state.writeUnlock();
                        if (addVirtualLoss) {
                            state.virtualLoss.decrementAndGet();
                        }
                        return SEARCH_RETRY;
                    }
                    state.transpositionsLock.unlock();
                    state.ns[action] = ns;
                    state.writeUnlock();
                    if (ns.total_n == 0 || Configuration.isTranspositionAlwaysExpandNewNodeOn(state)) {
                        var n = ns.getNextStateParallel();
                        if (handleFailedSearch2Parallel(state, action, n.v1().state, training, remainingCalls, false, level + 1, false, null)) {
                            Integer.parseInt(null); // fail for now
                        }
                        ns.correctVParallel(n.v1(), n.v2(), v, realV);
                    }
                    ns.readLock();
                    v.setToQNormalized(ns.total_q, ns.total_n);
                    ns.readUnlock();
                } else {
                    throw new RuntimeException();
                }
            }
        } else {
            if (nextState instanceof ChanceState cState) {
                if (state.n[action] < cState.total_n && !Configuration.isBanTranspositionInTreeOn(state)) {
                    // hmm this is buggy because this can be true even if it's not transposed due to multithreading...
                    if (Configuration.isTranspositionAlwaysExpandNewNodeOn(state)) {
                        var n = cState.getNextStateParallel();
                        if (handleFailedSearch2Parallel(state, action, n.v1().state, training, remainingCalls, false, level + 1, false, null)) {
                            Integer.parseInt(null); // fail for now
                        }
                        cState.correctVParallel(n.v1(), n.v2(), v, realV);
                    }
                    state.readLock();
                    cState.readLock();
                    v.setVarrayToDiffOfCurrentAccumulatedQAndTheoreticalChildQ(cState.total_q, cState.total_n, state.n[action] + 1, state.getChildQArray(action));
                    cState.readUnlock();
                    state.readUnlock();
                } else {
                    var n = cState.getNextStateParallel();
                    if (handleFailedSearch2Parallel(state, action, n.v1().state, training, remainingCalls, false, level + 1, false, null)) {
                        Integer.parseInt(null); // fail for now
                    }
                    cState.correctVParallel(n.v1(), n.v2(), v, realV);
                }
            } else if (nextState instanceof GameState nState) {
                if (state.n[action] < nState.total_n + 1 && !Configuration.isBanTranspositionInTreeOn(state)) {
                    // hmm this is buggy because this can be true even if it's not transposed due to multithreading...
                    if (!nState.isQInitialized() || Configuration.isTranspositionAlwaysExpandNewNodeOn(state)) {
                        if (handleFailedSearch2Parallel(state, action, nState, training, remainingCalls, false, level, true, deterministicPathPush(deterministicPath, state, action))) {
//                            Integer.parseInt(null); // fail for now
                            return SEARCH_RETRY;
                        }
                    }
                    state.readLock();
                    nState.readLock();
                    v.setVarrayToDiffOfCurrentAccumulatedQAndTheoreticalChildQ(nState.getTotalQArray(), nState.total_n + 1, state.n[action] + 1, state.getChildQArray(action));
                    nState.readUnlock();
                    state.readUnlock();
                } else {
                    if (handleFailedSearch2Parallel(state, action, nState, training, remainingCalls, false, level, true, deterministicPathPush(deterministicPath, state, action))) {
                        if (addVirtualLoss) {
                            state.virtualLoss.decrementAndGet();
                        }
                        return SEARCH_RETRY;
                    }
                }
            }
        }

        if (GameProperties.isHeartFight(state) && state.properties.switchBattleHandler != null && state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
            v.correctForSwitchBattle(state.total_n + 1);
        }
        state.writeLock();
        if (state.terminalAction >= 0) {
            state.writeUnlock(); // another thread may have detected terminal action
            search2parallel_r(state, training, remainingCalls, isRoot, level, addVirtualLoss, null);
            if (addVirtualLoss) {
                state.virtualLoss.decrementAndGet();
            }
            return SEARCH_SUCCESS;
        }
        state.getChildQArray(action).addVArray(v);
        state.getTotalQArray().addVArray(v);
        state.n[action] += 1;
        state.total_n += 1;
        var newVarianceM = state.varianceM + (realV.get(GameState.V_COMB_IDX) - state.varianceM) / (state.total_n + 1);
        var newVarianceS = state.varianceS + (realV.get(GameState.V_COMB_IDX) - state.varianceM) * (realV.get(GameState.V_COMB_IDX) - newVarianceM);
        state.varianceM = newVarianceM;
        state.varianceS = newVarianceS;
        if (reachedGuaranteedWin) {
            if (state.ns[action] instanceof ChanceState) {
                reachedGuaranteedWin = false;
            } else {
                state.terminalAction = action;
                v.addVarrayToMatchCurrentAccumulatedQAndTheoreticalChildQ(state.getChildQArray(action), state.n[action], state.total_n + 1, state.getTotalQArray());
            }
        }
        state.writeUnlock();
        if (addVirtualLoss) {
            state.virtualLoss.decrementAndGet();
        }
        numberOfPossibleActions = numberOfActions;
        return SEARCH_SUCCESS;
    }

    private static void addBannedAction(GameState state, int action) {
        if (state.bannedActions == null) {
            boolean[] bannedActions = new boolean[state.getLegalActions().length];
            bannedActions[action] = true;
            state.bannedActions = bannedActions;
        } else {
            state.bannedActions[action] = true;
        }
    }

    private boolean handleFailedSearch2Parallel(GameState state, int action, GameState childState, boolean training, int remainingCalls, boolean isRoot, int level, boolean addVirtualLoss, List<Tuple<GameState, Integer>> deterministicPath) {
        while (true) {
            int ret = search2parallel_r(childState, training, remainingCalls, isRoot, level, addVirtualLoss, deterministicPath);
            if (ret == SEARCH_SUCCESS) {
                return false;
            } else if (ret == SEARCH_NO_ACTIONS_LEFT) {
                state.writeLock();
                addBannedAction(state, action);
                state.writeUnlock();
                return true;
            }
        }
    }

    public boolean cannotImproveState(GameState state) {
        if (state.playerTurnStartMaxPossibleHealth != state.getPlayeForRead().getHealth()) {
            return false;
        }
        if (GameProperties.isHeartFight(state)) {
            return true;
        }
        if (state.playerTurnStartPotionCount != state.getPotionCount()) {
            return false;
        }
        if (state.properties.handOfGreedCounterIdx >= 0 && state.playerTurnStartMaxHandOfGreed != state.getCounterForRead()[state.properties.handOfGreedCounterIdx]) {
            return false;
        }
        if (state.properties.ritualDaggerCounterIdx >= 0 && state.playerTurnStartMaxRitualDagger != state.getCounterForRead()[state.properties.ritualDaggerCounterIdx]) {
            return false;
        }
        if (state.properties.nunchakuCounterIdx >= 0 && state.getCounterForRead()[state.properties.nunchakuCounterIdx] < 9) {
            return false;
        }
        if (state.properties.penNibCounterIdx >= 0 && state.getCounterForRead()[state.properties.penNibCounterIdx] < 9) {
            return false;
        }
        if (state.properties.inkBottleCounterIdx >= 0 && state.getCounterForRead()[state.properties.inkBottleCounterIdx] < 9) {
            return false;
        }
        if (state.properties.happyFlowerCounterIdx >= 0 && state.getCounterForRead()[state.properties.happyFlowerCounterIdx] < 2) {
            return false;
        }
        if (state.properties.geneticAlgorithmCounterIdx >= 0 && CardDefect.GeneticAlgorithm.getMaxPossibleGARemaining(state) != 0) {
            return false;
        }
        if (state.properties.sundialCounterIdx >= 0 && state.getCounterForRead()[state.properties.sundialCounterIdx] < 2) {
            return false;
        }
        if (state.properties.inserterCounterIdx >= 0 && state.getCounterForRead()[state.properties.inserterCounterIdx] < 1) {
            return false;
        }
        if (state.properties.incenseBurnerCounterIdx >= 0) {
            if (state.properties.incenseBurnerRewardType == Relic.IncenseBurner.DEFAULT_REWARD) {
                if (state.getCounterForRead()[state.properties.incenseBurnerCounterIdx] < 5) {
                    return false;
                }
            } else if (state.properties.incenseBurnerRewardType == Relic.IncenseBurner.NEXT_FIGHT_IS_SPEAR_AND_SHIELD_REWARD ||
                    state.currentEncounter == EnemyEncounter.EncounterEnum.SPEAR_AND_SHIELD) {
                if (state.getCounterForRead()[state.properties.incenseBurnerCounterIdx] != 4) {
                    return false;
                }
            }
        }
        return true;
    }

    private void updateTranspositions(GameState transpositionKey, GameState state, GameState curParentState, int action) {
        var parents = transpositionKey.transpositionsParent.get(transpositionKey);
        if (parents == null) {
            return;
        }
        for (var parent : parents) {
            if (parent.v1().equals(curParentState) && parent.v2() == action) {
                continue;
            }
            if (parent.v1().ns[parent.v2()] instanceof GameState) {
                for (int i = 0; i < state.properties.v_total_len; i++) {
                    var vChange = state.getTotalQ(i) / (state.total_n + 1) * parent.v1().n[parent.v2()] - parent.v1().getChildQ(parent.v2(), i);
                    parent.v1().addChildQ(parent.v2(), i, vChange);
                    parent.v1().addTotalQ(i, vChange);
                }
                updateTranspositions(parent.v1(), parent.v1(), null, -1);
            } else if (parent.v1().ns[parent.v2()] instanceof ChanceState cs) {
                if (cs.cache.get(state) == null) {
                    // in some states -> end turn lead to same states even though they have differences
                    // so the begin turn chance state may be different and have different states
                    continue;
                }
                var v = new VArray(state.properties.v_total_len);
                cs.correctV(state, v, realV);
                for (int i = 0; i < state.properties.v_total_len; i++) {
                    var vChange = cs.total_q.get(i) / cs.total_n * parent.v1().n[parent.v2()] - parent.v1().getChildQ(parent.v2(), i);
                    parent.v1().addChildQ(parent.v2(), i, vChange);
                    parent.v1().addTotalQ(i, vChange);
                }
                updateTranspositions(parent.v1(), parent.v1(), null, -1);
            }
        }
    }

    void search3(GameState state, boolean training, int remainingCalls) {
        reachedGuaranteedWin = false;
        search3_r(state, training, remainingCalls, true);
    }

    void search3_r(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        if (state.terminalAction >= 0) {
            v.setToQNormalized(state.getChildQArray(state.terminalAction), state.n[state.terminalAction]);
            realV.copyFrom(v);
            numberOfPossibleActions = 1;
            return;
        }
        if (state.isTerminal() != 0) {
            state.getVArray(v);
            realV.copyFrom(v);
            state.initSearchInfoLeaf();
            state.getTotalQArray().copyFrom(v);
            if (v.get(GameState.V_WIN_IDX) > 0.5 && cannotImproveState(state)) {
                reachedGuaranteedWin = true;
            }
            return;
        }
        if (state.policy == null) {
            state.doEval(model);
            state.getVArray(v);
            realV.copyFrom(v);
            state.initSearchInfoLeaf();
            state.getTotalQArray().copyFrom(v);
            numberOfPossibleActions = state.getLegalActions().length;
            state.varianceM = v.get(GameState.V_COMB_IDX);
            state.varianceS = 0;
            if (GameProperties.isHeartFight(state) && state.properties.switchBattleHandler != null && state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
                v.correctForSwitchBattle(state.total_n + 1);
            }
            return;
        }
        if (state.n == null) {
            state.initSearchInfo();
        }

        float[] policy = getPolicy(state, training, remainingCalls, isRoot);
        selectAction(state, policy, training, isRoot, true);
        int action = selectActionRet[0];
        int numberOfActions = selectActionRet[1];

        State nextState = state.ns[action];
        GameState state2;
        if (isRoot && exploredActions != null && exploredActions.containsKey(action)) {
            exploredV = exploredActions.get(action);
        }
        if (nextState == null) {
            state2 = state.clone(true);
            state2 = state2.doAction(action);
            if (state2.isStochastic) {
                if (Configuration.isTranspositionAcrossChanceNodeOn(state)) {
                    var s = state.transpositions.get(state2);
                    if (s == null) {
                        state.ns[action] = new ChanceState(state2, state, action);
                        state.transpositions.put(state2, state2);
                        this.search3_r(state2, training, remainingCalls, false);
                        ((ChanceState) (state.ns[action])).correctV(state2, v, realV);
                    } else {
                        state2 = (GameState) s;
                        state.ns[action] = new ChanceState(state2, state, action);
                        this.search3_r(state2, training, remainingCalls, false);
                        ((ChanceState) (state.ns[action])).correctV(state2, v, realV);
                    }
                } else {
                    state.ns[action] = new ChanceState(state2, state, action);
                    this.search3_r(state2, training, remainingCalls, false);
                    ((ChanceState) (state.ns[action])).correctV(state2, v, realV);
                }
            } else {
                var s = state.transpositions.get(state2);
                if (s == null) {
                    if (state2.actionCtx == GameActionCtx.BEGIN_TURN && state2.isTerminal() == 0) {
                        if (state2.properties.frozenEye == null || !state2.properties.frozenEye.isRelicEnabledInScenario(state2) || state2.isStochastic) {
                            var parentState = state2;
                            state2 = parentState.clone(false);
                            state2 = state2.doAction(0);
                            var cState = new ChanceState(state2, parentState, 0);
                            state.ns[action] = cState;
                            state.transpositions.put(parentState, cState);
                            this.search3_r(state2, training, remainingCalls, false);
                            cState.correctV(state2, v, realV);
                        } else {
                            state.ns[action] = state2;
                            state.transpositions.put(state2, state2);
                            this.search3_r(state2, training, remainingCalls, false);
                        }
                    } else {
                        state.ns[action] = state2;
                        state.transpositions.put(state2, state2);
                        this.search3_r(state2, training, remainingCalls, false);
                    }
                } else if (s instanceof GameState ns) {
                    state.ns[action] = ns;
                    v.setToQNormalized(ns.getTotalQArray(), (ns.total_n + 1));
                } else if (s instanceof ChanceState ns) {
                    state.ns[action] = ns;
                    v.setToQNormalized(ns.total_q, ns.total_n);
                }
            }
        } else {
            if (nextState instanceof ChanceState cState) {
                if (state.n[action] < cState.total_n) {
//                    state2 = cState.getNextState(true);
//                    this.search3_r(state2, training, remainingCalls, false);
//                    cState.correctV(state2, v, realV);
                    v.setVarrayToDiffOfCurrentAccumulatedQAndTheoreticalChildQ(cState.total_q, cState.total_n, state.n[action] + 1, state.getChildQArray(action));
                } else {
                    state2 = cState.getNextState(true, -1);
                    this.search3_r(state2, training, remainingCalls, false);
                    cState.correctV(state2, v, realV);
                }
            } else if (nextState instanceof GameState nState) {
                if (state.n[action] < nState.total_n + 1) {
//                    this.search3_r(nState, training, remainingCalls, false);
                    v.setVarrayToDiffOfCurrentAccumulatedQAndTheoreticalChildQ(nState.getTotalQArray(), nState.total_n + 1, state.n[action] + 1, state.getChildQArray(action));
                } else {
                    this.search3_r(nState, training, remainingCalls, false);
                }
            }
        }

        if (GameProperties.isHeartFight(state) && state.properties.switchBattleHandler != null && state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
            v.correctForSwitchBattle(state.total_n + 1);
        }
        for (int i = 0; i < state.properties.v_total_len; i++) {
            state.addChildQ(action, i, v.get(i));
        }
        state.n[action] += 1;
        state.total_n += 1;
        int actionToPropagate;
        if (reachedGuaranteedWin) {
            if (state.ns[action] instanceof ChanceState) {
                reachedGuaranteedWin = false;
                actionToPropagate = getActionWithMaxNodesOrTerminal(state);
            } else {
                state.terminalAction = action;
                actionToPropagate = action;
            }
        } else {
            actionToPropagate = getActionWithMaxNodesOrTerminal(state);
        }
        for (int i = 0; i < state.properties.v_total_len; i++) {
            double qTotal = state.getChildQ(actionToPropagate, i) / state.n[actionToPropagate] * (state.total_n + 1);
            if (state.ns[actionToPropagate] instanceof ChanceState) {
                qTotal = state.getChildQ(actionToPropagate, i) / state.n[actionToPropagate] * state.total_n + state.calcQValue();
            }
            v.set(i, qTotal - state.getTotalQ(i));
            state.addTotalQ(i, v.get(i));
        }
        numberOfPossibleActions = numberOfActions;
    }

    void searchPlain(GameState state, boolean training, int remainingCalls) {
        searchPlain_r(state, training, remainingCalls, true);
    }

    void searchPlain_r(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        if (state.isTerminal() != 0) {
            state.getVArray(v);
            state.getTotalQArray().copyFrom(v);
            numberOfPossibleActions = 1;
            return;
        }
        if (state.policy == null) {
            state.doEval(model);
            state.getVArray(v);
            state.getTotalQArray().copyFrom(v);
            numberOfPossibleActions = state.getLegalActions().length;
            if (GameProperties.isHeartFight(state) && state.properties.switchBattleHandler != null && state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
                v.correctForSwitchBattle(state.total_n + 1);
            }
            return;
        }
        if (state.n == null) {
            state.initSearchInfo();
        }

        float[] policy = getPolicy(state, training, remainingCalls, isRoot);
        selectAction(state, policy, training, isRoot, true);
        int action = selectActionRet[0];
        int numberOfActions = selectActionRet[1];

        State nextState = state.ns[action];
        GameState state2;
        if (nextState == null) {
            state2 = state.clone(true);
            state2 = state2.doAction(action);
            if (state2.actionCtx == GameActionCtx.BEGIN_TURN) {
                state2 = state2.doAction(0);
            }
            if (state2.isStochastic) {
                state.ns[action] = new ChanceState(state2, state, action);
                this.searchPlain_r(state2, training, remainingCalls, false);
                ((ChanceState) (state.ns[action])).correctV(state2, v, realV);
            } else {
                state.ns[action] = state2;
                this.searchPlain_r(state2, training, remainingCalls, false);
            }
        } else {
            if (nextState instanceof ChanceState cState) {
                state2 = cState.getNextState(true,-1);
                this.searchPlain_r(state2, training, remainingCalls, false);
                cState.correctV(state2, v, realV);
            } else if (nextState instanceof GameState nState) {
                this.searchPlain_r(nState, training, remainingCalls, false);
            }
        }

        if (GameProperties.isHeartFight(state) && state.properties.switchBattleHandler != null && state.actionCtx == GameActionCtx.BEGIN_BATTLE) {
            v.correctForSwitchBattle(state.total_n + 1);
        }
        for (int i = 0; i < state.properties.v_total_len; i++) {
            state.addChildQ(action, i, v.get(i));
        }
        state.n[action] += 1;
        state.total_n += 1;
        for (int i = 0; i < state.properties.v_total_len; i++) {
            state.addTotalQ(i, v.get(i));
        }
        numberOfPossibleActions = numberOfActions;
    }

    void searchLine(GameState state, boolean training, boolean isRoot, int remainingCalls) {
        if (isRoot) {
            if (v == null) {
                v = new VArray(state.properties.v_total_len);
                realV = new VArray(state.properties.v_total_len);
            }
        }
        if (state.terminalAction >= 0) {
            for (int i = 0; i < state.properties.v_total_len; i++) {
                v.set(i, state.getChildQ(state.terminalAction, i) / state.n[state.terminalAction]);
            }
            return;
        }
        if (state.searchFrontier == null) {
            state.searchFrontier = new SearchFrontier();
            state.searchFrontier.addLine(new LineOfPlay(state, 1, 1, null, 0));
        }
        reachedGuaranteedWin = false;

        int max_n = 0;
        var lines = state.searchFrontier.lines.values();
        if (!training) {
            for (var line : lines) {
                if (line.numberOfActions == 0) {
                    continue;
                }
                if (line.n > max_n) {
                    max_n = line.n;
                }
            }
        }

        LineOfPlay maxLine = null;
        double maxU = 0.0;
        if (isRoot) numberOfPossibleActions = 0;
        double p_uniform = 1.0 / state.searchFrontier.lines.size();
        double ratio = Math.min(((double) state.total_n) * state.total_n / 1000000.0, 1);
        int frontierNodes = 0;
        for (var line : lines) {
            if (line.numberOfActions == 0) {
                continue;
            }
            if (line.internal) {
                continue;
            }
            frontierNodes++;
        }
        boolean skipInternalNodes = false;
        if (frontierNodes >= Math.ceil(Math.pow(state.total_n + 1, 0.5))) {
            skipInternalNodes = true;
        }
        for (var line : lines) {
            if (line.numberOfActions == 0) {
                continue;
            }
            if (remainingCalls > 0 && max_n - line.n > remainingCalls) {
                continue;
            }
            if (skipInternalNodes && line.internal) {
                continue;
            }
            if (isRoot) numberOfPossibleActions += 1; // todo: why this?
            if (isRoot && remainingCalls > 0 && max_n < remainingCalls) {
                numberOfPossibleActions += line.numberOfActions;
            }
            var parentLine = line.parentLines == null ? null : line.parentLines.get(0).line();
            double q = line.n > 0 ? line.q_comb / line.n : parentLine != null && parentLine.n > 0 ? parentLine.q_comb / parentLine.n : 0;
//            double u = state.searchFrontier.total_n > 0 ? q + (0.125 + Math.log((state.searchFrontier.total_n + 10000f + 1) / 10000) / 10) * line.p_cur * sqrt(state.searchFrontier.total_n) / (1 + line.n) : line.p_cur;
            double p_prime = line.p_cur * (1 - ratio) + p_uniform * ratio;
//            if (state.properties.testNewFeature) {
//                p_prime = Math.pow(line.p_cur, 1.0 / line.depth) * (1 - ratio) + p_uniform * ratio;
//            }
            double cpuct = state.properties.cpuct;
            // if (state.getCounterForRead()[state.properties.biasedCognitionLimitCounterIdx] > 0) {
            //     cpuct = state.getCounterForRead()[state.properties.biasedCognitionLimitCounterIdx] / 100.0;
            // }
            if (Configuration.CPUCT_SCALING && (!Configuration.TEST_CPUCT_SCALING || state.properties.testNewFeature)) {
                cpuct = cpuct + 0.1 * Math.log((state.total_n + 1 + 5000) / 5000.0);
            }
            double u = state.searchFrontier.total_n > 0 ? q + cpuct * p_prime * sqrt(state.searchFrontier.total_n) / (1 + line.n) : line.p_cur;
//            double u = state.searchFrontier.total_n > 0 ? q + 0.125 * p_prime * sqrt(state.searchFrontier.total_n) / (1 + line.n) : line.p_cur;
            if (u > maxU) {
                maxU = u;
                maxLine = line;
            }
        }
        searchLine_r(state, maxLine, training, 1);
        searchLinePropagate(state, maxLine);
        reachedGuaranteedWin = false;
    }

    private void searchLinePropagate(GameState parentState, LineOfPlay line) {
        while (line.parentLines != null) {
            LineOfPlay.Edge edge;
            if (line.parentLines.size() == 1) {
                edge = line.parentLines.get(0);
            } else {
                edge = line.parentLines.get(parentState.properties.random.nextInt(line.parentLines.size(), RandomGenCtx.Other));
            }
            GameState state = (GameState) edge.line().state;
            state.n[edge.action()] += 1;
            for (int i = 0; i < state.properties.v_total_len; i++) {
                state.addChildQ(edge.action(), i, v.get(i));
            }
            state.total_n += 1;
            var newVarianceM = state.varianceM + (v.get(GameState.V_COMB_IDX) - state.varianceM) / (state.total_n + 1);
            var newVarianceS = state.varianceS + (v.get(GameState.V_COMB_IDX) - state.varianceM) * (v.get(GameState.V_COMB_IDX) - newVarianceM);
            state.varianceM = newVarianceM;
            state.varianceS = newVarianceS;
            if (line.state instanceof GameState childState && childState.terminalAction >= 0) {
                state.terminalAction = edge.action();
                for (int i = 0; i < state.properties.v_total_len; i++) {
                    double qTotal = childState.getTotalQ(i) / (childState.total_n + 1) * (state.total_n + 1);
                    v.set(i, qTotal - state.getTotalQ(i));
                }
            }
            for (int i = 0; i < state.properties.v_total_len; i++) {
                state.addTotalQ(i, v.get(i));
            }
            line = edge.line();
        }
    }

    private void searchLine_r(GameState parentState, LineOfPlay curLine, boolean training, int depth) {
        if (curLine.state instanceof ChanceState cState) {
            var nextState = cState.getNextState(true, -1);
            searchLine(nextState, training, false, -1);
            cState.correctV(nextState, v, realV);
            curLine.n += 1;
            curLine.q_comb += v.get(GameState.V_COMB_IDX);
            curLine.q_win += v.get(GameState.V_WIN_IDX);
            curLine.q_health += v.get(GameState.V_HEALTH_IDX);
            parentState.searchFrontier.total_n += 1;
            return;
        }
        GameState state = (GameState) curLine.state;
        if (state.isTerminal() != 0) {
            state.getVArray(v);
            state.initSearchInfoLeaf();
            for (int i = 0; i < state.properties.v_total_len; i++) {
                state.addTotalQ(i, v.get(i));
            }
            curLine.n += 1;
            curLine.q_comb += v.get(GameState.V_COMB_IDX);
            curLine.q_win += v.get(GameState.V_WIN_IDX);
            curLine.q_health += v.get(GameState.V_HEALTH_IDX);
            parentState.searchFrontier.total_n += 1;
            if (v.get(GameState.V_WIN_IDX) > 0.5 && cannotImproveState(state)) {
                state.terminalAction = -1234;
                reachedGuaranteedWin = true;
            }
            return;
        }
        if (state.policy == null) {
            state.doEval(model);
            state.getVArray(v);
            state.initSearchInfoLeaf();
            for (int i = 0; i < state.properties.v_total_len; i++) {
                state.addTotalQ(i, v.get(i));
            }
            if (training) {
                state.policyMod = applyDirichletNoiseToPolicy(state.policy, 0.5f);
            } else {
                state.policyMod = state.policy;
            }
            curLine.n = 1;
            curLine.q_comb = v.get(GameState.V_COMB_IDX);
            curLine.q_win = v.get(GameState.V_WIN_IDX);
            curLine.q_health = v.get(GameState.V_HEALTH_IDX);
            parentState.searchFrontier.total_n += 1;
            state.varianceM = v.get(GameState.V_COMB_IDX);
            state.varianceS = 0;
            return;
        }
        if (state.n == null) {
            state.initSearchInfo();
        }

        float[] policy = state.policyMod;
        int action = 0;
        int numberOfActions = 0;
        double maxU = -1000000;
        for (int i = 0; i < state.getLegalActions().length; i++) {
            if (state.n[i] > 0) {
                continue;
            }
            numberOfActions += 1;
            double u = policy[i];
            if (u > maxU) {
                action = i;
                maxU = u;
            }
        }
        curLine.internal = true;
        if (numberOfActions > 0) {
            curLine.numberOfActions -= 1;
        }
        if (numberOfActions == 1) {
            curLine.q_comb = 0;
        } else if (numberOfActions == 0) {
            for (int i = 0; i < state.getLegalActions().length; i++) {
                var line = parentState.searchFrontier.getLine(state.ns[i]);
                if (line == null) {
                    continue;
                }
                double u = line.q_comb / line.n;
                if (u > maxU) {
                    action = i;
                    maxU = u;
                }
            }
        }

        var nextState = state.clone(true);
        nextState = nextState.doAction(action);
        if (nextState.isStochastic || nextState.actionCtx == GameActionCtx.BEGIN_TURN) {
            var cState = nextState.isStochastic ? new ChanceState(null, state, action) : null;
            if (!nextState.isStochastic) {
                cState = new ChanceState(null, nextState, 0);
                nextState = nextState.clone(false);
                nextState = nextState.doAction(0);
            }
            var transposedLine = parentState.searchFrontier.getLine(cState);
            if (transposedLine == null) {
                cState.addToQueue(nextState);
                var newLine = new LineOfPlay(cState, curLine.p_total * policy[action], depth, curLine, action);
                curLine.p_cur -= curLine.p_total * policy[action];
                parentState.searchFrontier.addLine(newLine);
                searchLine_r(parentState, newLine, training, 1);
                if (state.n[action] == 0) {
                    state.ns[action] = newLine.state;
                }
            } else {
                if (state.n[action] == 0) {
                    searchLineTransposePropagatePolicy(parentState, transposedLine, curLine.p_total * policy[action]);
                    curLine.p_cur -= curLine.p_total * policy[action];
                    state.ns[action] = transposedLine.state;
                    transposedLine.parentLines.add(new LineOfPlay.Edge(curLine, action));
                }
                searchLine_r(parentState, transposedLine, training, 1);
            }
        } else {
            var transposedLine = parentState.searchFrontier.getLine(nextState);
            if (transposedLine == null) {
                var newLine = new LineOfPlay(nextState, curLine.p_total * policy[action], depth, curLine, action);
                curLine.p_cur -= curLine.p_total * policy[action];
                parentState.searchFrontier.addLine(newLine);
                searchLine_r(parentState, newLine, training, depth + 1);
                if (state.n[action] == 0) {
                    state.ns[action] = newLine.state;
                }
            } else {
                if (state.n[action] == 0) {
                    searchLineTransposePropagatePolicy(parentState, transposedLine, curLine.p_total * policy[action]);
                    curLine.p_cur -= curLine.p_total * policy[action];
                    state.ns[action] = transposedLine.state;
                    transposedLine.parentLines.add(new LineOfPlay.Edge(curLine, action));
                }
                searchLine_r(parentState, transposedLine, training, depth + 1);
            }
        }
        state.n[action] += 1;
        for (int i = 0; i < state.properties.v_total_len; i++) {
            state.addChildQ(action, i, v.get(i));
        }
        state.total_n += 1;
        if (reachedGuaranteedWin) {
            state.terminalAction = action;
            for (int i = 0; i < state.properties.v_total_len; i++) {
                double qTotal = state.getChildQ(action, i) / state.n[action] * (state.total_n + 1);
                v.set(i, qTotal - state.getTotalQ(i));
            }
        }
        for (int i = 0; i < state.properties.v_total_len; i++) {
            state.addTotalQ(i, v.get(i));
        }
    }

    private void searchLineTransposePropagatePolicy(GameState parentState, LineOfPlay line, double p) {
        if (line.state instanceof GameState state) {
            if (state.isTerminal() == 0) {
                if (state.n == null) {
                    state.initSearchInfo();
                }
                for (int i = 0; i < state.n.length; i++) {
                    if (state.n[i] > 0) {
                        var nextLine = parentState.searchFrontier.getLine(state.ns[i]);
                        searchLineTransposePropagatePolicy(parentState, nextLine, p * state.policyMod[i]);
                    }
                }
            }
        }
        line.p_cur += p * (line.p_cur / line.p_total);
        line.p_total += p;
    }

    private float[] getPolicy(GameState state, boolean training, int remainingCalls, boolean isRoot) {
        if (!training) {
            return applyFutileSearchPruning(state, state.policy, remainingCalls);
        }
        if (!isRoot) {
            return state.policy;
        }
        if (state.policyMod == null) {
            state.policyMod = ModelPlain.softmax(state.policy, 1.25f);
            state.policyMod = applyDirichletNoiseToPolicy(state.policy, 0.25f);
        }
        return state.policyMod;
    }

    private void selectAction(GameState state, float[] policy, boolean training, boolean isRoot, boolean firstCall) {
        // MCTS caller force root action to be taken
        if (isRoot && forceRootAction >= 0) {
            selectActionRet[0] = forceRootAction;
            selectActionRet[1] = 1;
            return;
        }

        // only use fight progress when every child node has been explored and <= 0.1% chance of winning for every one
        boolean useFightProgress = Configuration.USE_FIGHT_PROGRESS_WHEN_LOSING;
        if (Configuration.USE_FIGHT_PROGRESS_WHEN_LOSING) {
            for (int i = 0; i < state.getLegalActions().length; i++) {
                if (policy[i] <= 0 || (state.bannedActions != null && state.bannedActions[i])) {
                    continue;
                }
                double q_win = state.n[i] > 0 ? state.getChildQ(i, GameState.V_WIN_IDX) / state.n[i] : 1;
                if (q_win >= 0.001) {
                    useFightProgress = false;
                    break;
                }
            }
        }

        int numberOfActions = 0;
        double[] uValues = new double[state.getLegalActions().length];
        double[] qValues = new double[state.getLegalActions().length];
        double[] puct = new double[state.getLegalActions().length];
        for (int i = 0; i < state.getLegalActions().length; i++) {
            if (policy[i] <= 0 || (state.bannedActions != null && state.bannedActions[i])) {
                continue;
            }
            numberOfActions += 1;
            int childN = state.n[i];

            // multithreaded mcts -> add virtual loss
            if (state.properties.multithreadedMTCS && firstCall) {
                if (state.ns[i] != null && state.ns[i] instanceof GameState s) {
                    childN += s.virtualLoss.getPlain();
                } else if (state.ns[i] != null && state.ns[i] instanceof ChanceState s) {
                    childN += s.virtualLoss.getPlain();
                }
            }

            // first play urgency -> use parent q value
            double cpuct = state.properties.cpuct;
            double q = state.n[i] > 0 ? state.getChildQ(i, GameState.V_COMB_IDX) / childN : state.getTotalQ(GameState.V_COMB_IDX) / (state.total_n + 1);
            if (Configuration.USE_NEW_ACTION_SELECTION && state.properties.testNewFeature) {
                q = state.n[i] > 0 ? state.getChildQ(i, GameState.V_WIN_IDX) / childN : state.getTotalQ(GameState.V_WIN_IDX) / (state.total_n + 1);
            }
            if (useFightProgress) { // only when every child has at least one node
                q = state.getChildQ(i, GameState.V_EXTRA_IDX_START + state.properties.fightProgressVExtraIdx) / childN;
            }

            if (Configuration.CPUCT_SCALING && (!Configuration.TEST_CPUCT_SCALING || state.properties.testNewFeature)) {
                // cpuct scaling is equivalent to scaling the numerator in sqrt(state.total_n) / (1 + childN) further to encourage exploration
                // noticing at high nodes very little exploration is done if the initial policy is low with logaritmic scaling
                // so switch to total_n^0.25 which seem to work better?
//                cpuct = cpuct + 0.1 * Math.log((state.total_n + 1 + 5000) / 5000.0);
                cpuct = cpuct * sqrt(sqrt(Math.max(state.total_n, 1)));
            }

            // std err is an experimentation, not working out, remove in future
            // when there are no child n, use policy instead to select highest policy move
            var std_err = 0.0;
            if (Configuration.isUseUtilityStdErrForPuctOn(state)) {
                std_err = Math.sqrt(state.varianceS / state.total_n) / Math.sqrt(state.total_n + 1);
            }
            puct[i] = cpuct * policy[i] * (sqrt(state.total_n) / (1 + childN) + 5 * std_err);
            qValues[i] = q;
            uValues[i] = state.total_n > 0 ? q + puct[i] : policy[i];
        }

        int action = 0;
        double maxU = -1000000;
        boolean doForcePlayout = false;
        // when training: use KataGo's forced playout to help exploration
        if (Configuration.TRAINING_USE_FORCED_PLAYOUT && training && isRoot) {
            for (int i = 0; i < state.getLegalActions().length; i++) {
                if (policy[i] <= 0 || (state.bannedActions != null && state.bannedActions[i])) {
                    continue;
                }
                var force_n = (int) Math.sqrt(0.5 * policy[i] * state.total_n);
                if (state.n[i] < force_n) {
                    if (!doForcePlayout) {
                        action = i;
                        maxU = uValues[i];
                        doForcePlayout = true;
                    } else if (uValues[i] > maxU) { // if multiple actions is forced, we select action with the highest utility
                        action = i;
                        maxU = uValues[i];
                    }
                }
            }
        }
        if (!doForcePlayout) {
            for (int i = 0; i < state.getLegalActions().length; i++) {
                if (policy[i] <= 0 || (state.bannedActions != null && state.bannedActions[i])) {
                    continue;
                }
                if (uValues[i] > maxU) {
                    action = i;
                    maxU = uValues[i];
                }
            }
            if (Configuration.USE_NEW_ACTION_SELECTION && state.properties.testNewFeature && state.n[action] > 0) {
                double maxQ = -100000;
                for (int i = 0; i < state.getLegalActions().length; i++) {
                    if (policy[i] <= 0 || (state.bannedActions != null && state.bannedActions[i]) || state.n[i] <= 0) {
                        continue;
                    }
                    if (qValues[i] > maxQ) {
                        maxQ = qValues[i];
                    }
                }

                double currentQ = qValues[action];
                if (currentQ >= 0.999 * maxQ) {
                    for (int i = 0; i < state.getLegalActions().length; i++) {
                        if (policy[i] <= 0 || (state.bannedActions != null && state.bannedActions[i])) {
                            continue;
                        }
                        if (state.n[i] > 0 && qValues[i] >= 0.999 * maxQ) {
                            qValues[i] = state.getChildQ(i, GameState.V_COMB_IDX) / state.n[i];
                            uValues[i] = qValues[i] + puct[i];
                        } else {
                            qValues[i] = -100;
                            uValues[i] = -100;
                        }
                    }
                    maxU = -1000000;
                    for (int i = 0; i < state.getLegalActions().length; i++) {
                        if (policy[i] <= 0 || (state.bannedActions != null && state.bannedActions[i])) {
                            continue;
                        }
                        if (uValues[i] > maxU) {
                            action = i;
                            maxU = uValues[i];
                        }
                    }
                }
            }
            boolean useTurnLeftHead = !useFightProgress && Configuration.USE_TURNS_LEFT_HEAD && state.n[action] > 0;
            if (Configuration.USE_TURNS_LEFT_HEAD_ONLY_WHEN_NO_DMG) {
                if (state.getTotalQArray().getVZeroDmg(state.getPlayeForRead().getAccumulatedDamage()) / (state.total_n + 1) < 0.99) {
                    useTurnLeftHead = false;
                }
            }
            if (useTurnLeftHead) {
                double maxQ = -100000;
                int maxAction = -1;
                for (int i = 0; i < state.getLegalActions().length; i++) {
                    if (policy[i] <= 0 || (state.bannedActions != null && state.bannedActions[i]) || state.n[i] <= 0) {
                        continue;
                    }
                    if (qValues[i] > maxQ) {
                        maxQ = qValues[i];
                        maxAction = i;
                    }
                }

                // increase u more the lower the number of turns left and reselect action
                // the constants 0.999 and 1 is arbitrary right now
                double currentQ = qValues[action];
                if (currentQ >= 0.999 * maxQ) {
                    for (int i = 0; i < state.getLegalActions().length; i++) {
                        if (policy[i] <= 0 || (state.bannedActions != null && state.bannedActions[i]) || state.n[i] <= 0) {
                            continue;
                        }
                        double turns2 = state.getChildQ(i, GameState.V_EXTRA_IDX_START + state.properties.turnsLeftVExtraIdx) / state.n[i];
                        if (qValues[i] >= 0.999 * maxQ) {
                            uValues[i] += (1 - turns2) * state.properties.maxPossibleRealTurnsLeft / 50.0 ;
                        }
                    }
                    for (int i = 0; i < state.getLegalActions().length; i++) {
                        if (policy[i] <= 0 || (state.bannedActions != null && state.bannedActions[i]) || state.n[i] <= 0) {
                            continue;
                        }
                        if (uValues[i] > maxU) {
                            action = i;
                            maxU = uValues[i];
                        }
                    }
                }
            }
        }
        selectActionRet[0] = action;
        selectActionRet[1] = numberOfActions;
    }

    private float[] applyFutileSearchPruning(GameState state, float[] policy, int remainingCalls) {
        if (remainingCalls <= 0) {
            return policy;
        }
        int max_n = Utils.max(state.n);
        float[] newPolicy = policy;
        if (Configuration.isFlattenPolicyAsNodesIncreaseOn(state) && state.total_n > 100) {
            newPolicy = new float[policy.length];
            var sum = 0.0;
            for (int i = 0; i < policy.length; i++) {
                newPolicy[i] = (float) Math.pow(policy[i], (double) 100 / state.total_n);
//                if (state.ns[i] != null && state.n[i] >= 2) {
//                    if (state.ns[i] instanceof GameState gs) {
//                        newPolicy[i] = (float) Math.pow(policy[i], (gs.varianceS / gs.total_n) / (state.varianceS / state.total_n));
//                    } else if (state.ns[i] instanceof ChanceState cs) {
//                        newPolicy[i] = (float) Math.pow(policy[i], (cs.varianceS / (cs.total_n - 1)) / (state.varianceS / state.total_n));
//                    }
//                }
                sum += newPolicy[i];
            }
            for (int i = 0; i < policy.length; i++) {
                newPolicy[i] /= sum;
            }
        }
        for (int i = 0; i < policy.length; i++) {
            if (policy[i] > 0 && max_n - state.n[i] > remainingCalls) {
                if (newPolicy == policy) {
                    newPolicy = Arrays.copyOf(policy, policy.length);
                }
                newPolicy[i] = 0;
            }
        }
        return newPolicy;
    }

    private float[] applyDirichletNoiseToPolicy(float[] policy, float proportion) {
        policy = Arrays.copyOf(policy, policy.length);
        var param = new double[policy.length];
        Arrays.fill(param, 2);
        var noiseGen = new Dirichlet(param);
        var noise = noiseGen.nextDistribution(); // todo move out
        for (int i = 0; i < policy.length; i++) {
            policy[i] = (float) noise[i] * proportion + policy[i] * (1 - proportion);
        }
        return policy;
    }

    public static int getActionRandomOrTerminalWithUncertainty(GameState state) {
        if (state.terminalAction >= 0) {
            return state.terminalAction;
        }
        var total_n = 0;
        DescriptiveStatistics ds = new DescriptiveStatistics();
        var uncertainty = new double[state.policy.length];
        for (int i = 0; i < state.policy.length; i++) {
            if (state.n[i] > 0 && (state.ns[i] instanceof ChanceState || ((GameState) state.ns[i]).isTerminal() >= 0)) {
                total_n += state.n[i] * state.n[i];
                if (state.ns[i] instanceof ChanceState cs) {
                    var t = 0.0;
                    for (ChanceState.Node node : cs.cache.values()) {
                        t += node.state.getVExtra(state.properties.qwinVExtraIdx) * node.n / cs.total_node_n;
                    }
                    ds.addValue(t);
                    uncertainty[i] = t;
                } else {
                    ds.addValue(((GameState) state.ns[i]).getVExtra(state.properties.qwinVExtraIdx));
                    uncertainty[i] = ((GameState) state.ns[i]).getVExtra(state.properties.qwinVExtraIdx);
                }
            }
        }
        boolean useAll = false;
        if (total_n == 0) {
            useAll = true;
        }
        var max = ds.getMax();
        var min = ds.getMin();
        var p = new double[state.n.length];
        for (int i = 0; i < state.policy.length; i++) {
            if (useAll) {
                p[i] = state.n[i];
            } else if ((state.n[i] > 0 && (state.ns[i] instanceof ChanceState || ((GameState) state.ns[i]).isTerminal() >= 0))) {
                var pow = 2.0;
                if (max == min) {
                    pow = 1;
                } else {
                    pow = pow + 2 * ((uncertainty[i] - min) / (max - min) - 0.5);
                }
                p[i] = Math.pow(state.n[i] + 1, pow);
            }
        }

        var total_p = 0.0;
        var prevP = Arrays.copyOf(p, p.length);
        for (int i = 0; i < p.length; i++) {
            total_p += p[i];
        }
        for (int i = 0; i < p.length; i++) {
            p[i] = (float) (p[i] / total_p);
        }

        double r = state.getSearchRandomGen(true).nextDouble(RandomGenCtx.Other);
        int action = -1;
        double acc = 0.0;
        for (int i = 0; i < p.length; i++) {
            acc += p[i];
            if (acc > r) {
                action = i;
                break;
            }
        }
        return action;
    }

    public int getActionRandomOrTerminal(GameState state, boolean useUncertainty) {
        if (Configuration.TRAINING_EXPERIMENT_USE_UNCERTAINTY_FOR_EXPLORATION && useUncertainty && !Configuration.USE_Z_TRAINING) {
            return getActionRandomOrTerminalWithUncertainty(state);
        }
        if (state.terminalAction >= 0) {
            return state.terminalAction;
        }
        var total_n = 0;
        for (int i = 0; i < state.policy.length; i++) {
            if (state.n[i] > 0 && (state.ns[i] instanceof ChanceState || ((GameState) state.ns[i]).isTerminal() >= 0)) {
                total_n += state.n[i];
            }
        }
        if (total_n == 0) {
            total_n = state.total_n;
        }
        int r = state.getSearchRandomGen(true).nextInt(total_n, RandomGenCtx.Other);
        int acc = 0;
        int action = -1;
        for (int i = 0; i < state.policy.length; i++) {
            if (total_n == state.total_n || (state.n[i] > 0 && (state.ns[i] instanceof ChanceState || ((GameState) state.ns[i]).isTerminal() >= 0))) {
                acc += state.n[i];
                if (acc > r) {
                    action = i;
                    break;
                }
            }
        }
        return action;
    }

    public static int getActionRandomOrTerminalSelectScenario(GameState state) {
        if (state.terminalAction >= 0) {
            return state.terminalAction;
        }
        var total_n = 0;
        for (int i = 0; i < state.policy.length; i++) {
            if (state.n[i] > 0) {
                total_n += Math.pow(state.n[i], Configuration.PRE_BATTLE_SCENARIO_TEMP);
            }
        }
        int r = state.getSearchRandomGen(true).nextInt(total_n, RandomGenCtx.Other);
        int acc = 0;
        int action = -1;
        for (int i = 0; i < state.policy.length; i++) {
            if (state.n[i] > 0) {
                acc += Math.pow(state.n[i], Configuration.PRE_BATTLE_SCENARIO_TEMP);
                if (acc > r) {
                    action = i;
                    break;
                }
            }
        }
        return action;
    }

    public static int getActionWithMaxNodesOrTerminal(GameState state) {
        if (state.terminalAction >= 0) {
            return state.terminalAction;
        }
        if (state.total_n == 0) {
            int action = -1;
            float max_p = -1000.0f;
            for (int i = 0; i < state.getLegalActions().length; i++) {
                if (state.policy[i] > max_p) {
                    max_p = state.policy[i];
                    action = i;
                }
            }
            return action;
        } else {
            int action = -1;
            int max_n = -1000;
            for (int i = 0; i < state.getLegalActions().length; i++) {
                if (state.n[i] > max_n) {
                    max_n = state.n[i];
                    action = i;
                }
            }
            return action;
        }
    }

    public static int[] getActionWithMaxNodesOrTerminal2(GameState state) {
        if (state.terminalAction >= 0) {
            return new int[] {state.terminalAction};
        }
        if (state.total_n == 0) {
            int action = -1;
            float max_p = -1000.0f;
            for (int i = 0; i < state.getLegalActions().length; i++) {
                if (state.policy[i] > max_p) {
                    max_p = state.policy[i];
                    action = i;
                }
            }
            return new int[] { action };
        } else {
            int action = -1;
            int action2 = -1;
            int max_n = -1000;
            int max_n2 = -1000;
            for (int i = 0; i < state.getLegalActions().length; i++) {
                if (state.n[i] > max_n) {
                    max_n2 = max_n;
                    action2 = action;
                    max_n = state.n[i];
                    action = i;
                } else if (state.n[i] > max_n2) {
                    max_n2 = state.n[i];
                    action2 = i;
                }
            }
            if (state.n[action] >= state.total_n * 0.8) {
                return new int[] { action };
            }
            return new int[] { action, action2 };
        }
    }
}
