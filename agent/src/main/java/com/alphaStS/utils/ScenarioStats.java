package com.alphaStS.utils;

import com.alphaStS.GameState;
import com.alphaStS.GameStateRandomization;
import com.alphaStS.GameStep;
import com.alphaStS.MatchSession.GameResult;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.interval.ClopperPearsonInterval;

import java.util.*;

public class ScenarioStats {
    public int numOfGames;
    public int deathCount;
    public int totalDamageTaken;
    public int totalDamageTakenNoDeath;
    public int[] potionsUsed;
    public int daggerKilledEnemy;
    public int feedKilledEnemy;
    public int feedHealTotal;
    public int nunchakuCounter;
    public int happyFlowerCounter;
    public Map<Integer, Integer> damageCount;
    public double finalQComb;
    public long modelCalls;
    public long totalTurns;
    public long totalTurnsInWins;

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
    public int winByNunchaku;
    public int lossByNunchaku;
    public long winByNunchakuAmt;
    public long lossByNunchakuAmt;
    public int winByHappyFlower;
    public int lossByHappyFlower;
    public long winByHappyFlowerAmt;
    public long lossByHappyFlowerAmt;
    public List<Double> winQs = new ArrayList<>();
    public List<Double> lossQs = new ArrayList<>();
    public long modelCalls2;
    public long totalTurns2;

    public static ScenarioStats combine(ScenarioStats... stats) {
        ScenarioStats total = new ScenarioStats();
        if (stats.length > 0) {
            total.potionsUsed = new int[stats[0].potionsUsed.length];
            total.damageCount = new HashMap<>();
        }
        for (ScenarioStats stat : stats) {
            total.add(stat, null);
        }
        return total;
    }

    public void add(ScenarioStats stat, GameState state) {
        if (damageCount == null) {
            damageCount = new HashMap<>();
            potionsUsed = new int[state.prop.potions.size()];
        }
        numOfGames += stat.numOfGames;
        numberOfDivergences += stat.numberOfDivergences;
        numberOfSamples += stat.numberOfSamples;
        deathCount += stat.deathCount;
        totalDamageTaken += stat.totalDamageTaken;
        totalDamageTakenNoDeath += stat.totalDamageTakenNoDeath;
        for (int j = 0; j < potionsUsed.length; j++) {
            potionsUsed[j] += stat.potionsUsed[j];
        }
        daggerKilledEnemy += stat.daggerKilledEnemy;
        feedKilledEnemy += stat.feedKilledEnemy;
        feedHealTotal += stat.feedHealTotal;
        nunchakuCounter += stat.nunchakuCounter;
        happyFlowerCounter += stat.happyFlowerCounter;
        for (var dmg : stat.damageCount.keySet()) {
            damageCount.putIfAbsent(dmg, 0);
            damageCount.computeIfPresent(dmg, (k, v) -> v + stat.damageCount.get(dmg));
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
        winByNunchaku += stat.winByNunchaku;
        lossByNunchaku += stat.lossByNunchaku;
        winByNunchakuAmt += stat.winByNunchakuAmt;
        lossByNunchakuAmt += stat.lossByNunchakuAmt;
        winByHappyFlower += stat.winByHappyFlower;
        lossByHappyFlower += stat.lossByHappyFlower;
        winByHappyFlowerAmt += stat.winByHappyFlowerAmt;
        lossByHappyFlowerAmt += stat.lossByHappyFlowerAmt;
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
        if (damageCount == null) {
            damageCount = new HashMap<>();
            potionsUsed = new int[state.prop.potions.size()];
        }
        int damageTaken = state.getPlayeForRead().getOrigHealth() - state.getPlayeForRead().getHealth();
        numOfGames++;
        deathCount += (state.isTerminal() == -1 ? 1 : 0);
        totalDamageTaken += damageTaken;
        totalDamageTakenNoDeath += state.isTerminal() == 1 ? damageTaken : 0;
        damageCount.putIfAbsent(damageTaken, 0);
        damageCount.computeIfPresent(damageTaken, (k, v) -> v + 1);
        if (state.isTerminal() > 0) {
            finalQComb += state.get_q();
            for (int i = 0; i < state.prop.potions.size(); i++) {
                if (state.potionUsed(i)) {
                    potionsUsed[i]++;
                }
            }
            if (state.prop.ritualDaggerCounterIdx >= 0) {
                if (state.getCounterForRead()[state.prop.ritualDaggerCounterIdx] > 0) {
                    daggerKilledEnemy++;
                }
            }
            if (state.prop.feedCounterIdx >= 0) {
                if (state.getCounterForRead()[state.prop.feedCounterIdx] > 0) {
                    feedKilledEnemy++;
                    feedHealTotal += state.getCounterForWrite()[state.prop.feedCounterIdx];
                }
            }
            if (state.prop.hasCounter("Nunchaku")) {
                nunchakuCounter += state.getCounterForRead()[state.prop.getCounterIdx("Nunchaku")];
            }
            if (state.prop.hasCounter("HappyFlower")) {
                happyFlowerCounter += state.getCounterForRead()[state.prop.getCounterIdx("HappyFlower")];
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

            if (winByPotion == null && state.prop.potions.size() > 0) {
                winByPotion = new int[state.prop.potions.size()];
                lossByPotion = new int[state.prop.potions.size()];
            }
            for (int i = 0; i < state.prop.potions.size(); i++) {
                if (!state.potionUsed(i) && state2.potionUsed(i)) {
                    winByPotion[i]++;
                } else if (state.potionUsed(i) && !state2.potionUsed(i)) {
                    lossByPotion[i]++;
                }
            }

            if (state.prop.ritualDaggerCounterIdx >= 0) {
                var daggerUsed1 = state.getCounterForRead()[state.prop.ritualDaggerCounterIdx] > 0;
                var daggerUsed2 = state2.getCounterForRead()[state.prop.ritualDaggerCounterIdx] > 0;
                if ((state.isTerminal() == 1 && state2.isTerminal() == 1) && daggerUsed1 != daggerUsed2) {
                    if (daggerUsed1) {
                        winByDagger++;
                    } else {
                        lossByDagger++;
                    }
                }
            }

            if (state.prop.feedCounterIdx >= 0) {
                var feedUsed1 = state.getCounterForRead()[state.prop.feedCounterIdx] > 0;
                var feedUsed2 = state2.getCounterForRead()[state.prop.feedCounterIdx] > 0;
                if ((state.isTerminal() == 1 && state2.isTerminal() == 1) && feedUsed1 != feedUsed2) {
                    if (feedUsed1) {
                        winByFeed++;
                        winByFeedAmt += state.getCounterForRead()[state.prop.feedCounterIdx] - state2.getCounterForRead()[state.prop.feedCounterIdx];
                    } else {
                        lossByFeed++;
                        lossByFeedAmt += state2.getCounterForRead()[state.prop.feedCounterIdx] - state.getCounterForRead()[state.prop.feedCounterIdx];
                    }
                }
            }

            if (state.prop.nunchakuCounterIdx >= 0) {
                var nunchakuCounter1 = state.getCounterForRead()[state.prop.nunchakuCounterIdx];
                var nunchakuCounter2 = state2.getCounterForRead()[state.prop.nunchakuCounterIdx];
                if ((state.isTerminal() == 1 && state2.isTerminal() == 1) && nunchakuCounter1 != nunchakuCounter2) {
                    if (nunchakuCounter1 > nunchakuCounter2) {
                        winByNunchaku++;
                        winByNunchakuAmt += nunchakuCounter1 - nunchakuCounter2;
                    } else {
                        lossByNunchaku++;
                        lossByNunchakuAmt += nunchakuCounter2 - nunchakuCounter1;
                    }
                }
            }

            if (state.prop.happyFlowerCounterIdx >= 0) {
                var happyFlowerCounter1 = state.getCounterForRead()[state.prop.happyFlowerCounterIdx];
                var happyFlowerCounter2 = state2.getCounterForRead()[state.prop.happyFlowerCounterIdx];
                if ((state.isTerminal() == 1 && state2.isTerminal() == 1) && happyFlowerCounter1 != happyFlowerCounter2) {
                    if (happyFlowerCounter1 > happyFlowerCounter2) {
                        winByHappyFlower++;
                        winByHappyFlowerAmt += happyFlowerCounter1 - happyFlowerCounter2;
                    } else {
                        lossByHappyFlower++;
                        lossByHappyFlowerAmt += happyFlowerCounter2 - happyFlowerCounter1;
                    }
                }
            }
        }

        int meanTotalDmg = 0;
        for (int stepsIdx = 0; stepsIdx < stepsArr.size(); stepsIdx++) {
            steps = stepsArr.get(stepsIdx);
            steps2 = stepsArr2.get(stepsIdx);
            GameState state = steps.get(steps.size() - 1).state();
            GameState state2 = steps2.get(steps2.size() - 1).state();
            if ((state.isTerminal() == 1 && state2.isTerminal() == 1)) {
                meanTotalDmg += state.getPlayeForRead().getHealth() - state2.getPlayeForRead().getHealth();
            }
        }
        if (meanTotalDmg > 0) {
            winDmgs.add(((double) meanTotalDmg) / stepsArr.size());
        } else if (meanTotalDmg < 0) {
            lossDmgs.add(((double) -meanTotalDmg) / stepsArr.size());
        }

        double meanTotalQ = 0;
        for (int stepsIdx = 0; stepsIdx < stepsArr.size(); stepsIdx++) {
            steps = stepsArr.get(stepsIdx);
            steps2 = stepsArr2.get(stepsIdx);
            GameState state = steps.get(steps.size() - 1).state();
            GameState state2 = steps2.get(steps2.size() - 1).state();
            if ((state.isTerminal() == 1 && state2.isTerminal() == 1)) {
                meanTotalQ += state.get_q() - state2.get_q();
            }
        }
        if (meanTotalQ > 0) {
            winQs.add(meanTotalQ / stepsArr.size());
        } else if (meanTotalQ < 0) {
            lossQs.add(-meanTotalQ / stepsArr.size());
        }
    }

    public void printStats(GameState state, int spaces) {
        String indent = " ".repeat(Math.max(0, spaces));
        System.out.println(indent + "Deaths: " + deathCount + "/" + numOfGames + " (" + String.format("%.2f", 100 * deathCount / (float) numOfGames).trim() + "%)");
        System.out.println(indent + "Avg Damage: " + ((double) totalDamageTaken) / numOfGames);
        System.out.println(indent + "Avg Damage (Not Including Deaths): " + ((double) totalDamageTakenNoDeath) / (numOfGames - deathCount));
        if (potionsUsed != null) {
            for (int i = 0; i < potionsUsed.length; i++) {
                System.out.println(indent + "" + state.prop.potions.get(i) + " Used Percentage: " + String.format("%.5f", ((double) potionsUsed[i]) / (numOfGames - deathCount)));
            }
        }
        if (state.prop.ritualDaggerCounterIdx >= 0) {
            System.out.println(indent + "Dagger Killed Percentage: " + String.format("%.5f", ((double) daggerKilledEnemy) / (numOfGames - deathCount)));
        }
        if (state.prop.feedCounterIdx >= 0) {
            System.out.println(indent + "Feed Killed Percentage: " + String.format("%.5f", ((double) feedKilledEnemy) / (numOfGames - deathCount)) + "(Average=" + ((double) feedHealTotal) / feedKilledEnemy + ")");
        }
        if (state.prop.hasCounter("Nunchaku")) {
            System.out.println(indent + "Average Nunchaku Counter: " + String.format("%.5f", ((double) nunchakuCounter) / (numOfGames - deathCount)));
        }
        if (state.prop.hasCounter("HappyFlower")) {
            System.out.println(indent + "Average Happy Flower Counter: " + String.format("%.5f", ((double) happyFlowerCounter) / (numOfGames - deathCount)));
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
                for (int i = 0; i < state.prop.potions.size(); i++) {
                    printBinomialStat(indent, "Win/Loss Potion " + state.prop.potions.get(i), winByPotion[i], lossByPotion[i]);
                }
            }
            if (state.prop.ritualDaggerCounterIdx >= 0) {
                System.out.println(indent + "Win/Loss Dagger: " + winByDagger + "/" + lossByDagger);
            }
            if (state.prop.feedCounterIdx >= 0) {
                System.out.println(indent + "Win/Loss Feed: " + winByFeed + "/" + lossByFeed + " (" + winByFeedAmt / (double) winByFeed + "/" + lossByFeedAmt / (double) lossByFeed + "/" + (winByFeedAmt - lossByFeedAmt) / (double) (winByFeed + lossByFeed) + ")");
            }
            if (state.prop.nunchakuCounterIdx >= 0) {
                System.out.println(indent + "Win/Loss Nunchaku: " + winByNunchaku + "/" + lossByNunchaku + " (" + winByNunchakuAmt / (double) winByNunchaku + "/" + lossByNunchakuAmt / (double) lossByNunchaku + "/" + (winByNunchakuAmt - lossByNunchakuAmt) / (double) (winByNunchaku + lossByNunchaku) + ")");
            }
            if (state.prop.happyFlowerCounterIdx >= 0) {
                System.out.println(indent + "Win/Loss Happy Flower: " + winByHappyFlower + "/" + lossByHappyFlower + " (" + winByHappyFlowerAmt / (double) winByHappyFlower + "/" + lossByHappyFlowerAmt / (double) lossByHappyFlower + "/" + (winByHappyFlowerAmt - lossByHappyFlowerAmt) / (double) (winByHappyFlower + lossByHappyFlower) + ")");
            }
            DescriptiveStatistics ds = new DescriptiveStatistics();
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
            System.out.printf("%sDmg Diff: %6.5f [%6.5f - %6.5f] [%6.5f - %6.5f]\n", indent, vDmg,
                    (winDmgLowerBound * winDmgs.size() - lossDmgUpperBound * lossDmgs.size()) / (winDmgs.size() + lossDmgs.size()),
                    (winDmgUpperBound * winDmgs.size() - lossDmgLowerBound * lossDmgs.size()) / (winDmgs.size() + lossDmgs.size()), vDmgL, vDmgU);

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
            System.out.printf("%sQ Diff: %6.5f [%6.5f - %6.5f] [%6.5f - %6.5f]\n", indent, vQ,
                    (winQLowerBound * winQs.size() - lossQUpperBound * lossQs.size()) / (winQs.size() + lossQs.size()),
                    (winQUpperBound * winQs.size() - lossQLowerBound * lossQs.size()) / (winQs.size() + lossQs.size()), vQL, vQU);
        }
//        for (Map.Entry<Integer, Integer> integerIntegerEntry : damageCount.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).toList()) {
//            System.out.println(indent + integerIntegerEntry.getKey() + ": " + integerIntegerEntry.getValue());
//        }
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
