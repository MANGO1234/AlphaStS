package com.alphaStS.utils;

import com.alphaStS.*;
import com.alphaStS.MatchSession.GameResult;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.interval.ClopperPearsonInterval;

import java.util.*;
import java.util.stream.IntStream;

public class ScenarioStats {
    protected final GameProperties properties;

    public int numOfGames;
    public int deathCount;
    public int totalDamageTaken;
    public int[] potionsUsed;
    public int[] potionsUsedAgg;
    public int daggerKilledEnemy;
    public int feedKilledEnemy;
    public int feedHealTotal;
    public Map<Integer, Integer> damageCount;
    public Map<Integer, Integer> damageCountNoDeath;
    public double finalQComb;
    public long modelCalls;
    public long totalTurns;
    public long totalTurnsInWins;
    public List<CounterStat> counterStats;
    public int[] cardsUsedCount;
    public int[] select1OutOf3Count;
    public int[][] astrolabeCount;
    public long biasedCognitionLimit;
    public int[] biasedCognitionLimitUsedCount;
    public double[] biasedCognitionLimitDist;
    public Map<Integer, Tuple<Double, Integer>> predictionError;

    public boolean hasState2;
    public long numberOfDivergences;
    public long numberOfSamples;
    public int win;
    public int loss;
    public List<Double> winDmgs = new ArrayList<>();
    public List<Double> lossDmgs = new ArrayList<>();
    public int[] winByPotion;
    public int[] lossByPotion;
    public int winByDagger;
    public int lossByDagger;
    public int winByFeed;
    public int lossByFeed;
    public long winByFeedAmt;
    public long lossByFeedAmt;
    public List<Double> winQs = new ArrayList<>();
    public List<Double> lossQs = new ArrayList<>();
    public long modelCalls2;
    public long totalTurns2;

    public ScenarioStats(GameProperties properties) {
        this.properties = properties;
        damageCount = new HashMap<>();
        damageCountNoDeath = new HashMap<>();
        potionsUsed = new int[1 << properties.potions.size()];
        potionsUsedAgg = new int[properties.potions.size()];
        counterStats = new ArrayList<>();
        for (var entry : properties.counterRegistrants.entrySet()) {
            CounterStat counterStat = entry.getValue().get(0).getCounterStat();
            if (counterStat != null) {
                counterStats.add(counterStat);
            }
        }
        cardsUsedCount = new int[properties.cardDict.length];
        select1OutOf3Count = new int[properties.cardDict.length];
        if (properties.astrolabeCardsIdxes != null) {
            astrolabeCount = new int[properties.astrolabeCardsIdxes.length][2];
        }
        biasedCognitionLimitUsedCount = new int[100];
        biasedCognitionLimitDist = new double[100];
        predictionError = new HashMap<>();
    }

    public static ScenarioStats combine(GameProperties properties, ScenarioStats... stats) {
        ScenarioStats total = new ScenarioStats(properties);
        for (ScenarioStats stat : stats) {
            total.add(stat);
        }
        return total;
    }

    public void add(ScenarioStats stat) {
        numOfGames += stat.numOfGames;
        numberOfDivergences += stat.numberOfDivergences;
        numberOfSamples += stat.numberOfSamples;
        deathCount += stat.deathCount;
        totalDamageTaken += stat.totalDamageTaken;
        for (int j = 0; j < potionsUsed.length; j++) {
            potionsUsed[j] += stat.potionsUsed[j];
        }
        for (int j = 0; j < potionsUsedAgg.length; j++) {
            potionsUsedAgg[j] += stat.potionsUsedAgg[j];
        }
        daggerKilledEnemy += stat.daggerKilledEnemy;
        feedKilledEnemy += stat.feedKilledEnemy;
        feedHealTotal += stat.feedHealTotal;
        for (var dmg : stat.damageCount.keySet()) {
            damageCount.putIfAbsent(dmg, 0);
            damageCount.computeIfPresent(dmg, (k, v) -> v + stat.damageCount.get(dmg));
        }
        for (var dmg : stat.damageCountNoDeath.keySet()) {
            damageCountNoDeath.putIfAbsent(dmg, 0);
            damageCountNoDeath.computeIfPresent(dmg, (k, v) -> v + stat.damageCountNoDeath.get(dmg));
        }
        for (int i = 0; i < counterStats.size(); i++) {
            counterStats.get(i).add(stat.counterStats.get(i));
        }
        for (int i = 0; i < cardsUsedCount.length; i++) {
            cardsUsedCount[i] += stat.cardsUsedCount[i];
        }
        for (int i = 0; i < select1OutOf3Count.length; i++) {
            select1OutOf3Count[i] += stat.select1OutOf3Count[i];
        }
        if (properties.astrolabeCardsIdxes != null) {
            for (int i = 0; i < properties.astrolabeCardsIdxes.length; i++) {
                astrolabeCount[i][0] += stat.astrolabeCount[i][0];
                astrolabeCount[i][1] += stat.astrolabeCount[i][1];
            }
        }
        biasedCognitionLimit += stat.biasedCognitionLimit;
        for (int i = 0; i < biasedCognitionLimitDist.length; i++) {
            biasedCognitionLimitUsedCount[i] += stat.biasedCognitionLimitUsedCount[i];
            biasedCognitionLimitDist[i] += stat.biasedCognitionLimitDist[i];
        }
        for (var turnEntry : stat.predictionError.entrySet()) {
            predictionError.computeIfAbsent(turnEntry.getKey(), (k) -> new Tuple<>(0.0, 0));
            predictionError.computeIfPresent(turnEntry.getKey(), (k, v) -> new Tuple<>(v.v1() + turnEntry.getValue().v1(), v.v2() + turnEntry.getValue().v2()));
        }
        finalQComb += stat.finalQComb;
        modelCalls += stat.modelCalls;
        totalTurns += stat.totalTurns;
        totalTurnsInWins += stat.totalTurnsInWins;

        hasState2 |= stat.hasState2;
        win += stat.win;
        loss += stat.loss;
        winDmgs.addAll(stat.winDmgs);
        lossDmgs.addAll(stat.lossDmgs);
        if (winByPotion == null && stat.winByPotion != null) {
            winByPotion = new int[stat.winByPotion.length];
            lossByPotion = new int[stat.winByPotion.length];
        }
        if (winByPotion != null) {
            for (int i = 0; i < stat.winByPotion.length; i++) {
                winByPotion[i] += stat.winByPotion[i];
                lossByPotion[i] += stat.lossByPotion[i];
            }
        }
        winByDagger += stat.winByDagger;
        lossByDagger += stat.lossByDagger;
        winByFeed += stat.winByFeed;
        lossByFeed += stat.lossByFeed;
        winByFeedAmt += stat.winByFeedAmt;
        lossByFeedAmt += stat.lossByFeedAmt;
        winQs.addAll(stat.winQs);
        lossQs.addAll(stat.lossQs);
        modelCalls2 += stat.modelCalls2;
        totalTurns2 += stat.totalTurns2;
    }

    public void add(List<GameStep> steps, int modelCalls) {
        GameState state = steps.get(steps.size() - 1).state();
        this.modelCalls += modelCalls;
        totalTurns += state.turnNum;
        totalTurnsInWins += (state.isTerminal() == 1 ? state.turnNum : 0);
        int damageTaken = state.getPlayeForRead().getOrigHealth() - state.getPlayeForRead().getHealth();
        numOfGames++;
        deathCount += (state.isTerminal() == -1 ? 1 : 0);
        totalDamageTaken += damageTaken;
        damageCount.putIfAbsent(damageTaken, 0);
        damageCount.computeIfPresent(damageTaken, (k, v) -> v + 1);
        if (state.isTerminal() == 1) {
            damageCountNoDeath.putIfAbsent(damageTaken, 0);
            damageCountNoDeath.computeIfPresent(damageTaken, (k, v) -> v + 1);
        }
        for (int i = 0; i < steps.size() - 1; i++) {
            if (steps.get(i).state().getAction(steps.get(i).action()).type() == GameActionType.PLAY_CARD) {
                cardsUsedCount[steps.get(i).state().getAction(steps.get(i).action()).idx()] += 1;
            } else if (steps.get(i).state().getAction(steps.get(i).action()).type() == GameActionType.SELECT_CARD_1_OUT_OF_3) {
                select1OutOf3Count[steps.get(i).state().getAction(steps.get(i).action()).idx()] += 1;
            }
        }
        if (properties.astrolabeCardsIdxes != null) {
            for (int i = 0; i < state.properties.astrolabeCardsTransformed.length; i++) {
                if (state.isTerminal() > 0) {
                    astrolabeCount[state.properties.astrolabeCardsTransformed[i]][0] += 1;
                }
                astrolabeCount[state.properties.astrolabeCardsTransformed[i]][1] += 1;
            }
        }
        biasedCognitionLimit += state.properties.biasedCognitionLimitUsed;
        biasedCognitionLimitUsedCount[state.properties.biasedCognitionLimitUsed] += 1;
        if (state.properties.biasedCognitionLimitDistribution != null) {
            for (int i = 0; i < state.properties.biasedCognitionLimitDistribution.length; i++) {
                biasedCognitionLimitDist[i] += state.properties.biasedCognitionLimitDistribution[i];
            }
        }
        var finalQ = state.get_q();
        for (int i = 0; i < steps.size() - 1; i++) {
            var curState = steps.get(i).state();
            if (curState.turnNum < 1) {
                continue;
            }
            predictionError.computeIfAbsent(state.turnNum - curState.turnNum, (k) -> new Tuple<>(0.0, 0));
            predictionError.computeIfPresent(state.turnNum - curState.turnNum, (k, v) -> new Tuple<>(v.v1() + curState.get_q_TreeSearch(GameState.V_COMB_IDX) - finalQ, v.v2() + 1));
        }
        if (state.isTerminal() > 0) {
            finalQComb += state.get_q();
            int idx = 0;
            for (int i = 0; i < state.properties.potions.size(); i++) {
                if (state.potionUsed(i)) {
                    idx |= 1 << i;
                    potionsUsedAgg[i]++;
                }
            }
            potionsUsed[idx]++;
            if (state.properties.ritualDaggerCounterIdx >= 0) {
                if (state.getCounterForRead()[state.properties.ritualDaggerCounterIdx] > 0) {
                    daggerKilledEnemy++;
                }
            }
            if (state.properties.feedCounterIdx >= 0) {
                if (state.getCounterForRead()[state.properties.feedCounterIdx] > 0) {
                    feedKilledEnemy++;
                    feedHealTotal += state.getCounterForWrite()[state.properties.feedCounterIdx];
                }
            }
            if (state.properties.geneticAlgorithmCounterIdx >= 0) {
                if (state.getCounterForRead()[state.properties.geneticAlgorithmCounterIdx] > 0) {
                    feedKilledEnemy++;
                    feedHealTotal += state.getCounterForWrite()[state.properties.geneticAlgorithmCounterIdx];
                }
            }
            for (CounterStat counterStat : counterStats) {
                counterStat.add(state);
            }
        }
    }

    public void add(List<GameStep> steps, List<GameStep> steps2, int modelCalls2, List<GameResult> reruns) {
        if (steps2 == null) {
            return;
        }
        numberOfDivergences += reruns != null ? 1 : 0;
        numberOfSamples += reruns != null ? 1 + reruns.size() : 0;
        hasState2 = true;
        this.modelCalls2 += modelCalls2;
        totalTurns2 += steps2.get(steps2.size() - 1).state().turnNum;

        List<List<GameStep>> stepsArr = new ArrayList<>();
        List<List<GameStep>> stepsArr2 = new ArrayList<>();
        stepsArr.add(steps);
        stepsArr2.add(steps2);
        if (reruns != null) {
            for (GameResult rerun : reruns) {
                stepsArr.add(rerun.game().steps());
                stepsArr2.add(rerun.game2().steps());
            }
        }
        for (int stepsIdx = 0; stepsIdx < stepsArr.size(); stepsIdx++) {
            steps = stepsArr.get(stepsIdx);
            steps2 = stepsArr2.get(stepsIdx);
            GameState state = steps.get(steps.size() - 1).state();
            GameState state2 = steps2.get(steps2.size() - 1).state();
            if (state.isTerminal() != state2.isTerminal()) {
                if (state.isTerminal() == 1) {
                    win++;
                } else {
                    loss++;
                }
            }

            if (winByPotion == null && state.properties.potions.size() > 0) {
                winByPotion = new int[state.properties.potions.size()];
                lossByPotion = new int[state.properties.potions.size()];
            }
            for (int i = 0; i < state.properties.potions.size(); i++) {
                if (!state.potionUsed(i) && state2.potionUsed(i)) {
                    winByPotion[i]++;
                } else if (state.potionUsed(i) && !state2.potionUsed(i)) {
                    lossByPotion[i]++;
                }
            }

            if (state.properties.ritualDaggerCounterIdx >= 0) {
                var daggerUsed1 = state.getCounterForRead()[state.properties.ritualDaggerCounterIdx] > 0;
                var daggerUsed2 = state2.getCounterForRead()[state.properties.ritualDaggerCounterIdx] > 0;
                if ((state.isTerminal() == 1 && state2.isTerminal() == 1) && daggerUsed1 != daggerUsed2) {
                    if (daggerUsed1) {
                        winByDagger++;
                    } else {
                        lossByDagger++;
                    }
                }
            }

            if (state.properties.feedCounterIdx >= 0) {
                var feedUsed1 = state.getCounterForRead()[state.properties.feedCounterIdx] > 0;
                var feedUsed2 = state2.getCounterForRead()[state.properties.feedCounterIdx] > 0;
                if ((state.isTerminal() == 1 && state2.isTerminal() == 1) && feedUsed1 != feedUsed2) {
                    if (feedUsed1) {
                        winByFeed++;
                        winByFeedAmt += state.getCounterForRead()[state.properties.feedCounterIdx] - state2.getCounterForRead()[state.properties.feedCounterIdx];
                    } else {
                        lossByFeed++;
                        lossByFeedAmt += state2.getCounterForRead()[state.properties.feedCounterIdx] - state.getCounterForRead()[state.properties.feedCounterIdx];
                    }
                }
            }

            for (CounterStat counterStat : counterStats) {
                counterStat.addComparison(state, state2);
            }
        }

        for (int stepsIdx = 0; stepsIdx < stepsArr.size(); stepsIdx++) {
            steps = stepsArr.get(stepsIdx);
            steps2 = stepsArr2.get(stepsIdx);
            GameState state = steps.get(steps.size() - 1).state();
            GameState state2 = steps2.get(steps2.size() - 1).state();
            if ((state.isTerminal() == 1 && state2.isTerminal() == 1)) {
                int diff = state.getPlayeForRead().getHealth() - state2.getPlayeForRead().getHealth();
                if (diff > 0) {
                    winDmgs.add((double) diff);
                } else if (diff < 0) {
                    lossDmgs.add((double) -diff);
                }
            }
        }

        for (int stepsIdx = 0; stepsIdx < stepsArr.size(); stepsIdx++) {
            steps = stepsArr.get(stepsIdx);
            steps2 = stepsArr2.get(stepsIdx);
            GameState state = steps.get(steps.size() - 1).state();
            GameState state2 = steps2.get(steps2.size() - 1).state();
            if ((state.isTerminal() == 1 && state2.isTerminal() == 1)) {
                double diff = state.get_q() - state2.get_q();
                if (diff > 0) {
                    winQs.add(diff);
                } else if (diff < 0) {
                    lossQs.add(-diff);
                }
            }
        }
    }

    public void printStats(GameState state, boolean printDmg, int spaces) {
        String indent = " ".repeat(Math.max(0, spaces));
        System.out.println(indent + "Deaths: " + deathCount + "/" + numOfGames + " (" + String.format("%.2f", 100 * deathCount / (float) numOfGames).trim() + "%)");
        if (numOfGames == 0) {
            return;
        }
        DescriptiveStatistics ds = new DescriptiveStatistics();
        for (Map.Entry<Integer, Integer> dmgEntry : damageCount.entrySet()) {
            for (int i = 0; i < dmgEntry.getValue(); i++) {
                ds.addValue(dmgEntry.getKey());
            }
        }
        var dmgMean = ds.getMean();
        var dmgVariance = ds.getVariance();
        var dmgUpperBound = dmgMean + 1.98 * Math.sqrt(dmgVariance / numOfGames);
        var dmgLowerBound = dmgMean - 1.98 * Math.sqrt(dmgVariance / numOfGames);
        System.out.printf(indent + "Avg Damage: %6.5f [%6.5f-%6.5f]\n", dmgMean, dmgLowerBound, dmgUpperBound);
        ds.clear();
        for (Map.Entry<Integer, Integer> dmgEntry : damageCountNoDeath.entrySet()) {
            for (int i = 0; i < dmgEntry.getValue(); i++) {
                ds.addValue(dmgEntry.getKey());
            }
        }
        dmgMean = ds.getMean();
        dmgVariance = ds.getVariance();
        dmgUpperBound = dmgMean + 1.98 * Math.sqrt(dmgVariance / numOfGames);
        dmgLowerBound = dmgMean - 1.98 * Math.sqrt(dmgVariance / numOfGames);
        System.out.printf(indent + "Avg Damage (Not Including Deaths): %6.5f [%6.5f-%6.5f] (Min: %d, Max: %d, 25th Percentile: %6.5f, 75th Percentile: %6.5f, Variance: %6.5f)\n", dmgMean, dmgLowerBound, dmgUpperBound, (int) ds.getMin(), (int) ds.getMax(), ds.getPercentile(25), ds.getPercentile(75), ds.getVariance());
        if (potionsUsed != null && potionsUsed.length > 1) {
            System.out.println(indent + "Potion Usage Percentage (By Combo):");
            for (int i = 1; i < potionsUsed.length; i++) {
                StringBuilder desc = new StringBuilder();
                for (int j = 0; j < state.properties.potions.size(); j++) {
                    if ((i & (1 << j)) > 0) {
                        desc.append(desc.length() > 0 ? "+" : "").append(state.properties.potions.get(j));
                    }
                }
                System.out.println(indent + "    " + desc + " Used Percentage: " + String.format("%.5f", ((double) potionsUsed[i]) / (numOfGames - deathCount)));
            }
            System.out.println(indent + "Potion Usage Percentage (By Potion):");
            for (int i = 0; i < state.properties.potions.size(); i++) {
                System.out.println(indent + "    " + state.properties.potions.get(i) + " Used Percentage: " + String.format("%.5f", ((double) potionsUsedAgg[i]) / (numOfGames - deathCount)));
            }
        }
        if (state.properties.ritualDaggerCounterIdx >= 0) {
            System.out.println(indent + "Dagger Killed Percentage: " + String.format("%.5f", ((double) daggerKilledEnemy) / (numOfGames - deathCount)));
        }
        if (state.properties.feedCounterIdx >= 0) {
            System.out.println(indent + "Feed Killed Percentage: " + String.format("%.5f", ((double) feedKilledEnemy) / (numOfGames - deathCount)) + "(Average=" + ((double) feedHealTotal) / feedKilledEnemy + ")");
        }
        for (CounterStat counterStat : counterStats) {
            counterStat.printStat(indent, numOfGames - deathCount);
        }
        System.out.println(indent + "Average Final Q: " + String.format("%.5f", finalQComb / (numOfGames - deathCount)));
        System.out.println(indent + "Nodes/Turns: " + modelCalls + "/" + totalTurns + "/" + (((double) modelCalls) / totalTurns));
        System.out.println(indent + "Average Turns: " + String.format("%.2f", ((double) totalTurns) / numOfGames) + "/" + String.format("%.2f", ((double) totalTurnsInWins) / (numOfGames - deathCount)));

        if (hasState2) {
            System.out.println(indent + "Compared Network Nodes/Turns: " + modelCalls2 + "/" + totalTurns2 + "/" + (((double) modelCalls2) / totalTurns2));
            System.out.println(indent + "Number of Divergences: " + numberOfDivergences + " (" + numberOfSamples + ")");
            printBinomialStat(indent, "Win/Loss", win, loss);
            System.out.println(indent + "Win/Loss Q: " + winQs.size() + "/" + lossQs.size());
            System.out.println(indent + "Win/Loss Dmg: " + winDmgs.size() + "/" + lossDmgs.size());
            if (winByPotion != null) {
                for (int i = 0; i < state.properties.potions.size(); i++) {
                    printBinomialStat(indent, "Win/Loss Potion " + state.properties.potions.get(i), winByPotion[i], lossByPotion[i]);
                }
            }
            if (state.properties.ritualDaggerCounterIdx >= 0) {
                System.out.println(indent + "Win/Loss Dagger: " + winByDagger + "/" + lossByDagger);
            }
            if (state.properties.feedCounterIdx >= 0) {
                System.out.println(indent + "Win/Loss Feed: " + winByFeed + "/" + lossByFeed + " (" + winByFeedAmt / (double) winByFeed + "/" + lossByFeedAmt / (double) lossByFeed + "/" + (winByFeedAmt - lossByFeedAmt) / (double) (winByFeed + lossByFeed) + ")");
            }
            for (CounterStat counterStat : counterStats) {
                counterStat.printCmpStat(indent);
            }
            ds.clear();
            winDmgs.forEach(ds::addValue);
            var winDmgMean = ds.getMean();
            var winDmgVariance = ds.getVariance();
            var winDmgUpperBound = winDmgMean + 1.98 * Math.sqrt(winDmgVariance / winDmgs.size());
            var winDmgLowerBound = winDmgMean - 1.98 * Math.sqrt(winDmgVariance / winDmgs.size());
            ds.clear();
            lossDmgs.forEach(ds::addValue);
            var lossDmgMean = ds.getMean();
            var lossDmgVariance = ds.getVariance();
            var lossDmgUpperBound = lossDmgMean + 1.98 * Math.sqrt(lossDmgVariance / lossDmgs.size());
            var lossDmgLowerBound = lossDmgMean - 1.98 * Math.sqrt(lossDmgVariance / lossDmgs.size());
            System.out.printf("%sDmg Diff By Win/Loss: %6.5f (%6.5f) [%6.5f - %6.5f]/%6.5f (%6.5f) [%6.5f - %6.5f]\n", indent,
                    winDmgMean, Math.sqrt(winDmgVariance / winDmgs.size()), winDmgLowerBound, winDmgUpperBound,
                    lossDmgMean, Math.sqrt(lossDmgVariance / lossDmgs.size()), lossDmgLowerBound, lossDmgUpperBound);
            ds.clear();
            winDmgs.forEach(ds::addValue);
            lossDmgs.forEach((x)->ds.addValue(-x));
            var vDmg = ds.getMean();
            var vDmgU = vDmg + 1.98 * ds.getStandardDeviation() / Math.sqrt(winDmgs.size() + lossDmgs.size());
            var vDmgL = vDmg - 1.98 * ds.getStandardDeviation() / Math.sqrt(winDmgs.size() + lossDmgs.size());
            System.out.printf("%sDmg Diff: %6.5f [%6.5f - %6.5f]\n", indent, vDmg, vDmgL, vDmgU);

            ds.clear();
            winQs.forEach(ds::addValue);
            var winQMean = ds.getMean();
            var winQVariance = ds.getVariance();
            var winQUpperBound = winQMean + 1.98 * Math.sqrt(winQVariance / winQs.size());
            var winQLowerBound = winQMean - 1.98 * Math.sqrt(winQVariance / winQs.size());
            ds.clear();
            lossQs.forEach(ds::addValue);
            var lossQMean = ds.getMean();
            var lossQVariance = ds.getVariance();
            var lossQUpperBound = lossQMean + 1.98 * Math.sqrt(lossQVariance / lossQs.size());
            var lossQLowerBound = lossQMean - 1.98 * Math.sqrt(lossQVariance / lossQs.size());
            System.out.printf("%sQ Diff By Win/Loss: %6.5f (%6.5f) [%6.5f - %6.5f]/%6.5f (%6.5f) [%6.5f - %6.5f]\n", indent,
                    winQMean, Math.sqrt(winQVariance / winQs.size()), winQLowerBound, winQUpperBound,
                    lossQMean, Math.sqrt(lossQVariance / lossQs.size()), lossQLowerBound, lossQUpperBound);
            ds.clear();
            winQs.forEach(ds::addValue);
            lossQs.forEach((x)->ds.addValue(-x));
            var vQ = ds.getMean();
            var vQU = vQ + 1.98 * ds.getStandardDeviation() / Math.sqrt(winQs.size() + lossQs.size());
            var vQL = vQ - 1.98 * ds.getStandardDeviation() / Math.sqrt(winQs.size() + lossQs.size());
            System.out.printf("%sQ Diff: %6.5f [%6.5f - %6.5f]\n", indent, vQ, vQL, vQU);
        }
        if (printDmg) {
            for (var dmgEntry : damageCount.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                System.out.println(indent + dmgEntry.getKey() + ": " + dmgEntry.getValue() + " (" + Utils.formatFloat(dmgEntry.getValue() / (float) numOfGames * 100) + "%)");
            }
        }
        if (Configuration.STATS_PRINT_CARD_USAGE_COUNT) {
            var usedCountList = IntStream.range(0, cardsUsedCount.length)
                    .filter(x -> cardsUsedCount[x] > 0)
                    .mapToObj(x -> new Tuple<>(properties.cardDict[x], cardsUsedCount[x]))
                    .sorted(Comparator.comparing(x -> -x.v2()))
                    .toList();
            for (int i = 0; i < usedCountList.size(); i++) {
                System.out.print(i == 0 ? indent + "Card Usage: [" : ", ");
                System.out.print(usedCountList.get(i).v1().cardName + ": " + usedCountList.get(i).v2() + " (" + Utils.formatFloat( usedCountList.get(i).v2() / (double) numOfGames * 100) + "%)");
            }
            System.out.println("]");
            var select1OutOf3CountList = IntStream.range(0, select1OutOf3Count.length)
                    .filter(x -> select1OutOf3Count[x] > 0)
                    .mapToObj(x -> new Tuple<>(properties.cardDict[x], select1OutOf3Count[x]))
                    .sorted(Comparator.comparing(x -> -x.v2()))
                    .toList();
            var total = IntStream.of(select1OutOf3Count).sum();
            for (int i = 0; i < select1OutOf3CountList.size(); i++) {
                System.out.print(i == 0 ? indent + "Select 1 Out Of 3 Card Chosen Count: [" : ", ");
                System.out.print(select1OutOf3CountList.get(i).v1().cardName + ": " + select1OutOf3CountList.get(i).v2() + " (" + Utils.formatFloat( select1OutOf3CountList.get(i).v2() / (double) total * 100) + "%)");
            }
            System.out.println("]");
            if (properties.astrolabeCardsIdxes != null) {
                var transformedCountList = IntStream.range(0, properties.astrolabeCardsIdxes.length)
                        .filter(x -> astrolabeCount[x][1] > 0)
                        .mapToObj(x -> new Tuple3<>(properties.cardDict[properties.astrolabeCardsIdxes[x]], astrolabeCount[x], 100 * astrolabeCount[x][0] / (double) astrolabeCount[x][1]))
                        .sorted(Comparator.comparing(x -> -x.v3()))
                        .toList();
                for (int i = 0; i < transformedCountList.size(); i++) {
                    System.out.print(i == 0 ? indent + "Astrolabe Card Count: [" : ", ");
                    System.out.print(transformedCountList.get(i).v1().cardName + ": " + Utils.formatFloat(transformedCountList.get(i).v3()) + "% (" + transformedCountList.get(i).v2()[0] + "/" + transformedCountList.get(i).v2()[1] + ")");
                }
                System.out.println("]");
            }
        }
        if (properties.biasedCognitionLimitCounterIdx >= 0) {
            System.out.printf("%sBiased Cognition Limit: %s\n", indent, Utils.formatFloat(((double) biasedCognitionLimit) / numOfGames));
            for (int i = 0; i < biasedCognitionLimitDist.length; i++) {
                System.out.print(i == 0 ? indent + "Biased Cognition Limit Distribution: [" : ", ");
                System.out.print(i + ": " + Utils.formatFloat(biasedCognitionLimitDist[i] / numOfGames) + (biasedCognitionLimitUsedCount[i] == 0 ? "" : " (" + biasedCognitionLimitUsedCount[i] + ")"));
            }
            System.out.println("]");
        }
        if (Configuration.STATS_PRINT_PREDICTION_ERRORS) {
            var totalError = 0.0;
            var totalTurns = 0;
            for (var turnEntry : predictionError.entrySet()) {
                totalError += turnEntry.getValue().v1();
                totalTurns += turnEntry.getValue().v2();
                System.out.println(indent + turnEntry.getKey() + ": " + turnEntry.getValue().v1() / turnEntry.getValue().v2() + " (" + turnEntry.getValue().v2() + " samples)");
            }
            System.out.println("Total: " + totalError / totalTurns);
        }
    }

    private void printBinomialStat(String indent, String prefix, int win, int loss) {
        if (win + loss > 0 && win > 0 && loss > 0) {
            var ci = new ClopperPearsonInterval().createInterval(win + loss, win, 0.95);
            System.out.printf("%s%s: %d/%d (%.5f [%.5f-%.5f])\n", indent, prefix, win, loss, ((double) win) / (win + loss), ci.getLowerBound(), ci.getUpperBound());
        } else {
            System.out.printf("%s%s: %d/%d\n", indent, prefix, win, loss);
        }
    }

    public static String getCommonString(Map<Integer, GameStateRandomization.Info> scenarios, int[] scenarioGroup) {
        String pre = scenarios.get(scenarioGroup[0]).desc();
        String suff = scenarios.get(scenarioGroup[0]).desc();
        for (int i = 1; i < scenarioGroup.length; i++) {
            var c = scenarios.get(scenarioGroup[i]).desc();
            for (int j = 0; j < pre.length(); j++) {
                if (c.charAt(j) != pre.charAt(j)) {
                    pre = pre.substring(0, j);
                    break;
                }
            }
            for (int j = 0; j < suff.length(); j++) {
                if (c.charAt(c.length() - 1 - j) != suff.charAt(suff.length() - 1 - j)) {
                    suff = suff.substring(suff.length() - j);
                    break;
                }
            }
        }
        if (pre.endsWith(" + ")) {
            pre = pre.substring(0, pre.length() - 3);
        }
        return pre + suff;
    }
}
