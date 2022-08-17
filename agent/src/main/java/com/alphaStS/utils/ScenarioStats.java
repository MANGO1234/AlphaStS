package com.alphaStS.utils;

import com.alphaStS.GameState;
import com.alphaStS.GameStateRandomization;
import com.alphaStS.GameStep;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Map<Integer, Integer> damageCount;
    public double finalQComb;
    long modelCalls;
    long totalTurns;

    boolean hasState2;
    long win;
    long loss;
    long winByDmg;
    long lossByDmg;
    List<Integer> winDmgs;
    List<Integer> lossDmgs;
    long winDmgDiff;
    long loseDmgDiff;
    long[] winByPotion;
    long[] lossByPotion;
    long winByDagger;
    long lossByDagger;
    long winByFeed;
    long lossByFeed;
    long winByFeedAmt;
    long lossByFeedAmt;
    long winByQ;
    long lossByQ;
    long modelCalls2;
    long totalTurns2;

    public static ScenarioStats combine(ScenarioStats... stats) {
        ScenarioStats total = new ScenarioStats();
        if (stats.length > 0) {
            total.potionsUsed = new int[stats[0].potionsUsed.length];
            total.damageCount = new HashMap<>();
            total.winDmgs = new ArrayList<>();
            total.lossDmgs = new ArrayList<>();
        }
        for (ScenarioStats stat : stats) {
            total.numOfGames += stat.numOfGames;
            total.deathCount += stat.deathCount;
            total.totalDamageTaken += stat.totalDamageTaken;
            total.totalDamageTakenNoDeath += stat.totalDamageTakenNoDeath;
            for (int j = 0; j < total.potionsUsed.length; j++) {
                total.potionsUsed[j] += stat.potionsUsed[j];
            }
            total.daggerKilledEnemy += stat.daggerKilledEnemy;
            total.feedKilledEnemy += stat.feedKilledEnemy;
            total.feedHealTotal += stat.feedHealTotal;
            total.nunchakuCounter += stat.nunchakuCounter;
            for (var dmg : stat.damageCount.keySet()) {
                total.damageCount.putIfAbsent(dmg, 0);
                total.damageCount.computeIfPresent(dmg, (k, v) -> v + 1);
            }
            total.finalQComb += stat.finalQComb;
            total.modelCalls += stat.modelCalls;
            total.totalTurns += stat.totalTurns;

            total.hasState2 |= stat.hasState2;
            total.win += stat.win;
            total.loss += stat.loss;
            total.winByDmg += stat.winByDmg;
            total.lossByDmg += stat.lossByDmg;
            total.winDmgDiff += stat.winDmgDiff;
            total.loseDmgDiff += stat.loseDmgDiff;
            total.winDmgs.addAll(stat.winDmgs);
            total.lossDmgs.addAll(stat.lossDmgs);
            if (total.winByPotion == null && stat.winByPotion != null) {
                total.winByPotion = new long[stat.winByPotion.length];
                total.lossByPotion = new long[stat.winByPotion.length];
            }
            if (total.winByPotion != null) {
                for (int i = 0; i < stat.winByPotion.length; i++) {
                    total.winByPotion[i] += stat.winByPotion[i];
                    total.lossByPotion[i] += stat.lossByPotion[i];
                }
            }
            total.winByDagger += stat.winByDagger;
            total.lossByDagger += stat.lossByDagger;
            total.winByFeed += stat.winByFeed;
            total.lossByFeed += stat.lossByFeed;
            total.winByFeedAmt += stat.winByFeedAmt;
            total.lossByFeedAmt += stat.lossByFeedAmt;
            total.winByQ += stat.winByQ;
            total.lossByQ += stat.lossByQ;
            total.modelCalls2 += stat.modelCalls2;
            total.totalTurns2 += stat.totalTurns2;
        }
        return total;
    }

    public void add(List<GameStep> steps, int modelCalls, List<GameStep> steps2, int modelCalls2) {
        GameState state = steps.get(steps.size() - 1).state();
        this.modelCalls += modelCalls;
        totalTurns += state.turnNum;
        if (damageCount == null) {
            damageCount = new HashMap<>();
            potionsUsed = new int[state.prop.potions.size()];
            winDmgs = new ArrayList<>();
            lossDmgs = new ArrayList<>();
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
        }

        if (steps2 == null) {
            return;
        }
        GameState state2 = steps2.get(steps2.size() - 1).state();
        this.modelCalls2 += modelCalls2;
        totalTurns2 += state2.turnNum;
        hasState2 = true;

        var q1 = state.get_q();
        var q2 = state2.get_q();
        if (state.isTerminal() != state2.isTerminal()) {
            if (state.isTerminal() == 1) {
                win++;
            } else {
                loss++;
            }
        }

        if ((state.isTerminal() == 1 && state2.isTerminal() == 1) && state.getPlayeForRead().getHealth() != state2.getPlayeForRead().getHealth()) {
            if (state.getPlayeForRead().getHealth() > state2.getPlayeForRead().getHealth()) {
                winByDmg++;
                winDmgDiff += state.getPlayeForRead().getHealth() - state2.getPlayeForRead().getHealth();
                winDmgs.add(state.getPlayeForRead().getHealth() - state2.getPlayeForRead().getHealth());
            } else {
                lossByDmg++;
                loseDmgDiff += state2.getPlayeForRead().getHealth() - state.getPlayeForRead().getHealth();
                lossDmgs.add(state2.getPlayeForRead().getHealth() - state.getPlayeForRead().getHealth());
            }
        }

        if ((state.isTerminal() == 1 && state2.isTerminal() == 1) && q1 != q2) {
            if (q1 > q2) {
                winByQ++;
            } else {
                lossByQ++;
            }
        }

        if (winByPotion == null && state.prop.potions.size() > 0) {
            winByPotion = new long[state.prop.potions.size()];
            lossByPotion = new long[state.prop.potions.size()];
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
        System.out.println(indent + "Average Final Q: " + String.format("%.5f", finalQComb / (numOfGames - deathCount)));
        System.out.println(indent + "Nodes/Turns: " + modelCalls + "/" + totalTurns + "/" + (((double) modelCalls) / totalTurns));

        if (hasState2) {
            System.out.println(indent + "Compared Network Nodes/Turns: " + modelCalls2 + "/" + totalTurns2 + "/" + (((double) modelCalls2) / totalTurns2));
            System.out.println(indent + "Win/Loss: " + win + "/" + loss);
            System.out.println(indent + "Win/Loss Q: " + winByQ + "/" + lossByQ);
            System.out.println(indent + "Win/Loss Dmg: " + winByDmg + "/" + lossByDmg);
            if (winByPotion != null) {
                for (int i = 0; i < state.prop.potions.size(); i++) {
                    System.out.println(indent + "Win/Loss Potion " + state.prop.potions.get(i) + ": " + winByPotion[i] + "/" + lossByPotion[i]);
                }
            }
            if (state.prop.ritualDaggerCounterIdx >= 0) {
                System.out.println(indent + "Win/Loss Dagger: " + winByDagger + "/" + lossByDagger);
            }
            if (state.prop.feedCounterIdx >= 0) {
                System.out.println(indent + "Win/Loss Feed: " + winByFeed + "/" + lossByFeed + " (" + winByFeedAmt / (double) winByFeed + "/" + lossByFeedAmt / (double) lossByFeed + "/" + (winByFeedAmt - lossByFeedAmt) / (double) (winByFeed + lossByFeed) + ")");
            }
            DescriptiveStatistics ds = new DescriptiveStatistics();
            winDmgs.forEach(ds::addValue);
            var winVariance = ds.getVariance();
            var winDmgMean = ((double) winDmgDiff) / winByDmg;
            var winDmgUpperBound = winDmgMean + 1.98 * winVariance / Math.sqrt(winByDmg);
            var winDmgLowerBound = winDmgMean - 1.98 * winVariance / Math.sqrt(winByDmg);
            ds.clear();
            lossDmgs.forEach(ds::addValue);
            var lossVariance = ds.getVariance();
            var lossDmgMean = ((double) loseDmgDiff) / lossByDmg;
            var lossDmgUpperBound = lossDmgMean + 1.98 * lossVariance / Math.sqrt(lossByDmg);
            var lossDmgLowerBound = lossDmgMean - 1.98 * lossVariance / Math.sqrt(lossByDmg);
            System.out.printf("%sDmg Diff By Win/Loss: %6.5f (%6.5f) [%6.5f - %6.5f]/%6.5f (%6.5f) [%6.5f - %6.5f]\n", indent,
                    winDmgMean, winVariance / Math.sqrt(winByDmg), winDmgLowerBound, winDmgUpperBound,
                    lossDmgMean, lossVariance / Math.sqrt(lossByDmg), lossDmgLowerBound, lossDmgUpperBound);
            ds.clear();
            winDmgs.forEach(ds::addValue);
            lossDmgs.forEach((x)->ds.addValue(-x));
            var v = ds.getMean();
            var vU = v + 1.98 * ds.getVariance() / Math.sqrt(winDmgs.size() + lossDmgs.size());
            var vL = v - 1.98 * ds.getVariance() / Math.sqrt(winDmgs.size() + lossDmgs.size());
            System.out.printf("%sDmg Diff: %6.5f [%6.5f - %6.5f] %6.5f [%6.5f - %6.5f]\n", indent, ((double) winDmgDiff - loseDmgDiff) / (winByDmg + lossByDmg),
                    (winDmgLowerBound * winByDmg - lossDmgUpperBound * lossByDmg) / (winByDmg + lossByDmg),
                    (winDmgUpperBound * winByDmg - lossDmgLowerBound * lossByDmg) / (winByDmg + lossByDmg), v, vL, vU);
        }
    }

    public static String getCommonString(HashMap<Integer, GameStateRandomization.Info> scenarios, int[] scenarioGroup) {
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
