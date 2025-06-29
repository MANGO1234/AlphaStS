package com.alphaStS.utils;

import com.alphaStS.GameState;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CounterStat {
    int counterIdx;
    String counterName;
    List<Integer> counterFrequency = new ArrayList<>();
    int cmpWin;
    int cmpWinAmt;
    int cmpLoss;
    int cmpLossAmt;
    int counterLen = 1;
    boolean showFrequency = false;

    public CounterStat() {} // for jackson

    public CounterStat(int idx, String name) {
        counterIdx = idx;
        counterName = name;
    }

    public CounterStat(int idx, int len, String name) {
        counterIdx = idx;
        counterName = name;
        counterLen = len;
    }

    public CounterStat copy() {
        return new CounterStat(counterIdx, counterName);
    }

    public void add(CounterStat counterStat) {
        for (int i = counterFrequency.size(); i <= counterStat.counterFrequency.size(); i++) {
            counterFrequency.add(0);
        }
        for (int i = 0; i < counterStat.counterFrequency.size(); i++) {
            counterFrequency.set(i, counterFrequency.get(i) + counterStat.counterFrequency.get(i));
        }
        cmpWin += counterStat.cmpWin;
        cmpWinAmt += counterStat.cmpWinAmt;
        cmpLoss += counterStat.cmpLoss;
        cmpLossAmt += counterStat.cmpLossAmt;
    }

    private int getCounter(GameState state) {
        if (counterLen == 1) {
            return state.getCounterForRead()[counterIdx];
        } else {
            for (int i = 0; i < counterLen; i++) {
                if (state.getCounterForRead()[i] > 0) {
                    return i;
                }
            }
            return 0;
        }
    }

    public void add(GameState state) {
        int counter = state.getCounterForRead()[counterIdx];
        for (int i = counterFrequency.size(); i <= counter; i++) {
            counterFrequency.add(0);
        }
        counterFrequency.set(counter, counterFrequency.get(counter) + 1);
    }

    public void addComparison(GameState state, GameState state2) {
        var counter1 = state.getCounterForRead()[counterIdx];
        var counter2 = state2.getCounterForRead()[counterIdx];
        if ((state.isTerminal() == 1 && state2.isTerminal() == 1) && counter1 != counter2) {
            if (counter1 > counter2) {
                cmpWin++;
                cmpWinAmt += counter1 - counter2;
            } else {
                cmpLoss++;
                cmpLossAmt += counter2 - counter1;
            }
        }
    }

    public void printStat(String indent, int n) {
        long totalCounterAmt = 0;
        for (int i = 0; i < counterFrequency.size(); i++) {
            totalCounterAmt += counterFrequency.get(i) * i;
        }
        if (counterLen > 1 || showFrequency) {
            StringJoiner sj = new StringJoiner(", ");
            for (int i = 0; i < counterFrequency.size(); i++) {
                if (counterFrequency.get(i) > 0) {
                    sj.add(i + ": " + counterFrequency.get(i) + " (%" + String.format("%.3f", 100.0 * counterFrequency.get(i) / n) + ")");
                }
            }
            System.out.println(indent + "Average " + counterName + " Counter: " + String.format("%.5f", ((double) totalCounterAmt) / n) + " (" + sj + ")");
        } else {
            System.out.println(indent + "Average " + counterName + " Counter: " + String.format("%.5f", ((double) totalCounterAmt) / n));
        }
    }

    public CounterStat setShowFrequency(boolean showFrequency) {
        this.showFrequency = showFrequency;
        return this;
    }

    public void printCmpStat(String indent) {
        System.out.println(indent + "Win/Loss " + counterName + ": " + cmpWin + "/" + cmpLoss + " (" + cmpWinAmt / (double) cmpWin + "/" + cmpLossAmt / (double) cmpLoss + "/" + (cmpWinAmt - cmpLossAmt) / (double) (cmpWin + cmpLoss) + ")");
    }
}
