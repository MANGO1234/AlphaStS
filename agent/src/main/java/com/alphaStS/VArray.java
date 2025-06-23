package com.alphaStS;

import java.util.Arrays;

public class VArray {
    private final double[] data;
    private int[] accumulatedDmg; // array of accumulated damage
    private double[] accumulatedDmgV; // array of the probability of accumulated damage, lining up with accumulatedDmg array
    int accumulatedDmgLen = 0; // length of filled value in accumulatedDmg

    public VArray(int length) {
        this.data = new double[length];
        if (Configuration.USE_TURNS_LEFT_HEAD_ONLY_WHEN_NO_DMG) {
            this.accumulatedDmg = new int[2];
            this.accumulatedDmgV = new double[2];
        }
    }

    public double get(int index) {
        return data[index];
    }

    public void set(int index, double value) {
        data[index] = value;
    }

    public void add(int index, double value) {
        data[index] += value;
    }

    public int length() {
        return data.length;
    }

    public void reset() {
        Arrays.fill(data, 0);
        accumulatedDmgLen = 0;
    }

    public void copyFrom(VArray other) {
        System.arraycopy(other.data, 0, this.data, 0, Math.min(this.data.length, other.data.length));
        if (Configuration.USE_TURNS_LEFT_HEAD_ONLY_WHEN_NO_DMG) {
            accumulatedDmg = Arrays.copyOf(other.accumulatedDmg, other.accumulatedDmg.length);
            accumulatedDmgV = Arrays.copyOf(other.accumulatedDmgV, other.accumulatedDmgV.length);
            accumulatedDmgLen = other.accumulatedDmgLen;
        }
    }

    public VArray copy() {
        VArray result = new VArray(this.data.length);
        result.copyFrom(this);
        return result;
    }

    public double getVExtra(int vExtraIdx) {
        return data[GameState.V_EXTRA_IDX_START + vExtraIdx];
    }

    public void setVExtra(int vExtraIdx, double value) {
        data[GameState.V_EXTRA_IDX_START + vExtraIdx] = value;
    }

    public double getVZeroDmg(int accumulatedDamage) {
        int index = Arrays.binarySearch(accumulatedDmg, accumulatedDamage);
        return index < 0 ? 0 : accumulatedDmgV[index];
    }

    public void setVZeroDmg(int accumulatedDamage, double v) {
        accumulatedDmg[0] = accumulatedDamage;
        accumulatedDmgV[1] = v;
        accumulatedDmgLen = 1;
    }

    // The following are logic in various other places (like MCTS) that modifies VArray
    // accDmg is tricky to merge, so encapsulate all the logic here in VArray

    private void ensureAccumulatedArrCapacity(int newLen) {
        int curLen = accumulatedDmg.length;
        while (newLen > curLen) {
            curLen *= 2;
        }
        if (curLen != accumulatedDmg.length) {
            accumulatedDmg = Arrays.copyOf(accumulatedDmg, curLen);
            accumulatedDmgV = Arrays.copyOf(accumulatedDmgV, curLen);
        }
    }

    private int getAccumulatedArrLenAfterMerge(VArray a, VArray b) {
        int left = 0;
        int right = 0;
        int count = 0;
        while (left < a.accumulatedDmgLen && right < b.accumulatedDmgLen) {
            if (a.accumulatedDmg[left] == b.accumulatedDmg[right]) {
                left++;
                right++;
            } else if (a.accumulatedDmg[left] < b.accumulatedDmg[right]) {
                left++;
            } else {
                right++;
            }
            count++;
        }
        return count + (a.accumulatedDmgLen - left) + (b.accumulatedDmgLen - right);
    }

    private int getAccumulatedArrLenAfterMerge(VArray a, VArray b, VArray c) {
        int left = 0;
        int middle = 0;
        int right = 0;
        int mergeLen = 0;
        while (left < a.accumulatedDmgLen || middle < b.accumulatedDmgLen || right < c.accumulatedDmgLen) {
            int aDmg = left < a.accumulatedDmgLen ? a.accumulatedDmg[left] : Integer.MAX_VALUE;
            int bDmg = middle < b.accumulatedDmgLen ? b.accumulatedDmg[middle] : Integer.MAX_VALUE;
            int cDmg = right < c.accumulatedDmgLen ? c.accumulatedDmg[right] : Integer.MAX_VALUE;
            int min = Math.min(aDmg, Math.min(bDmg, cDmg));
            if (aDmg == min) {
                left++;
            }
            if (bDmg == min) {
                middle++;
            }
            if (cDmg == min) {
                right++;
            }
            mergeLen++;
        }
        return mergeLen;
    }

    public void trainingSetExpectedValueStage1(VArray out, long n) {
        for (int i = 0; i < data.length; i++) {
            data[i] += out.get(i) * n;
        }
        // todo
    }

    public void trainingSetExpectedValueStage2(long totalNodeN, VArray vCur, float p) {
        for (int i = 0; i < data.length; i++) {
            data[i] = data[i] / totalNodeN;
            if (i == 0) {
                data[i] = vCur.get(i) * p + data[i];
            } else {
                data[i] = (float) Math.min(vCur.get(i) * p + data[i], 1);
            }
        }
        // todo
    }

    public void trainingDiscountReward(VArray vCur) {
        for (int j = 0; j < vCur.length(); j++) {
            data[j] = Configuration.DISCOUNT_REWARD_ON_RANDOM_NODE * vCur.get(j) + (1 - Configuration.DISCOUNT_REWARD_ON_RANDOM_NODE) * data[j];
        }
        // todo
    }

    public void setToQNormalized(VArray totalQArray, long totalN) {
        for (int i = 0; i < data.length; i++) {
            data[i] = totalQArray.data[i] / totalN;
        }

        if (accumulatedDmg == null) return;
        accumulatedDmg = Arrays.copyOf(totalQArray.accumulatedDmg, totalQArray.accumulatedDmg.length);
        accumulatedDmgV = Arrays.copyOf(totalQArray.accumulatedDmgV, totalQArray.accumulatedDmgV.length);
        accumulatedDmgLen = totalQArray.accumulatedDmgLen;
        for (int i = 0; i < accumulatedDmgLen; i++) {
            accumulatedDmgV[i] /= totalN;
        }
    }

    public void setVarrayToDiffOfCurrentAccumulatedQAndTheoreticalChildQ(VArray totalQ, long totalN, int childN, VArray childQ) {
        for (int i = 0; i < data.length; i++) {
            data[i] = totalQ.data[i] / totalN * childN - childQ.get(i);
        }

        if (accumulatedDmg == null) return;
        int mergeLen = getAccumulatedArrLenAfterMerge(totalQ, childQ);
        ensureAccumulatedArrCapacity(mergeLen);
        int overwrite = mergeLen - 1;
        int leftIdx = totalQ.accumulatedDmgLen - 1;
        int rightIdx = childQ.accumulatedDmgLen - 1;
        while (leftIdx >= 0 || rightIdx >= 0) {
            double leftV = 0;
            double rightV = 0;
            int leftDmg = leftIdx >= 0 ? totalQ.accumulatedDmg[leftIdx] : Integer.MIN_VALUE;
            int rightDmg = rightIdx >= 0 ? childQ.accumulatedDmg[rightIdx] : Integer.MIN_VALUE;
            if (leftDmg == rightDmg) {
                accumulatedDmg[overwrite] = leftDmg;
                leftV = totalQ.accumulatedDmgV[leftIdx];
                rightV = childQ.accumulatedDmgV[rightIdx];
                leftIdx--;
                rightIdx--;
            } else if (leftDmg > rightDmg) {
                accumulatedDmg[overwrite] = leftDmg;
                leftV = totalQ.accumulatedDmgV[leftIdx];
                leftIdx--;
            } else {
                accumulatedDmg[overwrite] = rightDmg;
                rightV = childQ.accumulatedDmgV[rightIdx];
                rightIdx--;
            }
            accumulatedDmgV[overwrite] = leftV / totalN * childN - rightV;
            overwrite--;
        }
        accumulatedDmgLen = mergeLen;
    }

    public void addVarrayToMatchCurrentAccumulatedQAndTheoreticalChildQ(VArray totalQ, long totalN, int childN, VArray childQ) {
        for (int i = 0; i < data.length; i++) {
            data[i] += totalQ.data[i] / totalN * childN - childQ.get(i);
        }

        if (accumulatedDmg == null) return;
        int mergeLen = getAccumulatedArrLenAfterMerge(this, totalQ, childQ);
        ensureAccumulatedArrCapacity(mergeLen);
        int overwrite = mergeLen - 1;
        int leftIdx = accumulatedDmgLen - 1;
        int middleIdx = totalQ.accumulatedDmgLen - 1;
        int rightIdx = childQ.accumulatedDmgLen - 1;
        while (leftIdx >= 0 || middleIdx >= 0 || rightIdx >= 0) {
            double leftV = 0;
            double middleV = 0;
            double rightV = 0;
            int leftDmg = leftIdx >= 0 ? accumulatedDmg[leftIdx] : Integer.MIN_VALUE;
            int middleDmg = middleIdx >= 0 ? totalQ.accumulatedDmg[middleIdx] : Integer.MIN_VALUE;
            int rightDmg = rightIdx >= 0 ? childQ.accumulatedDmg[rightIdx] : Integer.MIN_VALUE;
            int max = Math.max(leftDmg, Math.max(middleDmg, rightDmg));
            if (leftDmg == max) {
                leftV = accumulatedDmgV[leftIdx];
                leftIdx--;
            }
            if (middleDmg == max) {
                middleV = totalQ.accumulatedDmgV[middleIdx];
                middleIdx--;
            }
            if (rightDmg == max) {
                rightV = childQ.accumulatedDmgV[rightIdx];
                rightIdx--;
            }
            accumulatedDmg[overwrite] = max;
            accumulatedDmgV[overwrite] = leftV + (middleV / totalN * childN) - rightV;
            overwrite--;
        }
        accumulatedDmgLen = mergeLen;
    }

    public void addVArray(VArray other) {
        for (int i = 0; i < data.length; i++) {
            data[i] += other.data[i];
        }

        if (accumulatedDmg == null) return;
        int mergeLen = getAccumulatedArrLenAfterMerge(this, other);
        ensureAccumulatedArrCapacity(mergeLen);
        int overwrite = mergeLen - 1;
        int leftIdx = accumulatedDmgLen - 1;
        int rightIdx = other.accumulatedDmgLen - 1;
        while (leftIdx >= 0 || rightIdx >= 0) {
            double leftV = 0;
            double rightV = 0;
            int leftDmg = leftIdx >= 0 ? accumulatedDmg[leftIdx] : Integer.MIN_VALUE;
            int rightDmg = rightIdx >= 0 ? other.accumulatedDmg[rightIdx] : Integer.MIN_VALUE;
            if (leftDmg == rightDmg) {
                accumulatedDmg[overwrite] = leftDmg;
                leftV = accumulatedDmgV[leftIdx];
                rightV = other.accumulatedDmgV[rightIdx];
                leftIdx--;
                rightIdx--;
            } else if (leftDmg > rightDmg) {
                accumulatedDmg[overwrite] = leftDmg;
                leftV = accumulatedDmgV[leftIdx];
                leftIdx--;
            } else {
                accumulatedDmg[overwrite] = rightDmg;
                rightV = other.accumulatedDmgV[rightIdx];
                rightIdx--;
            }
            accumulatedDmgV[overwrite] = leftV + rightV;
            overwrite--;
        }
        accumulatedDmgLen = mergeLen;
    }

    // see call site for the logic it's copying
    public void setChanceStateCorrectV(VArray totalQArray, int stateTotalN, VArray totalQ, long totalN, long totalNodeN, VArray prevQ, long nodeN) {
        if (accumulatedDmg == null) return;
        int mergeLen = getAccumulatedArrLenAfterMerge(totalQArray, totalQ, prevQ);
        ensureAccumulatedArrCapacity(mergeLen);
        int overwrite = mergeLen - 1;
        int leftIdx = totalQArray.accumulatedDmgLen - 1;
        int middleIdx = totalQ.accumulatedDmgLen - 1;
        int rightIdx = prevQ.accumulatedDmgLen - 1;
        while (leftIdx >= 0 || middleIdx >= 0 || rightIdx >= 0) {
            double leftV = 0;
            double middleV = 0;
            double rightV = 0;
            int leftDmg = leftIdx >= 0 ? totalQArray.accumulatedDmg[leftIdx] : Integer.MIN_VALUE;
            int middleDmg = middleIdx >= 0 ? totalQ.accumulatedDmg[middleIdx] : Integer.MIN_VALUE;
            int rightDmg = rightIdx >= 0 ? prevQ.accumulatedDmg[rightIdx] : Integer.MIN_VALUE;
            int max = Math.max(leftDmg, Math.max(middleDmg, rightDmg));
            if (leftDmg == max) {
                leftV = totalQArray.accumulatedDmgV[leftIdx];
                leftIdx--;
            }
            if (middleDmg == max) {
                middleV = totalQ.accumulatedDmgV[middleIdx];
                middleIdx--;
            }
            if (rightDmg == max) {
                rightV = prevQ.accumulatedDmgV[rightIdx];
                rightIdx--;
            }
            var node_cur_q = leftV / (stateTotalN + 1);
            var prev_q = totalN == 1 ? 0 : middleV / (totalN - 1);
            var new_q = (prev_q * (totalNodeN - 1) - rightV * (nodeN - 1)) / totalNodeN + node_cur_q * nodeN / totalNodeN;
            accumulatedDmg[overwrite] = max;
            accumulatedDmgV[overwrite] = new_q * totalN - middleV;
            overwrite--;
        }
        accumulatedDmgLen = mergeLen;
    }

    // see call site for the logic it's copying
    public void setChanceStateCorrectVParallel(VArray totalQArray, int stateTotalN, long nodeN, VArray prevQ, long prevNodeN) {
        if (accumulatedDmg == null) return;
        int mergeLen = getAccumulatedArrLenAfterMerge(totalQArray, prevQ);
        ensureAccumulatedArrCapacity(mergeLen);
        int overwrite = mergeLen - 1;
        int leftIdx = totalQArray.accumulatedDmgLen - 1;
        int rightIdx = prevQ.accumulatedDmgLen - 1;
        while (leftIdx >= 0 || rightIdx >= 0) {
            double leftV = 0;
            double rightV = 0;
            int leftDmg = leftIdx >= 0 ? totalQArray.accumulatedDmg[leftIdx] : Integer.MIN_VALUE;
            int rightDmg = rightIdx >= 0 ? prevQ.accumulatedDmg[rightIdx] : Integer.MIN_VALUE;
            if (leftDmg == rightDmg) {
                accumulatedDmg[overwrite] = leftDmg;
                leftV = totalQArray.accumulatedDmgV[leftIdx];
                rightV = prevQ.accumulatedDmgV[rightIdx];
                leftIdx--;
                rightIdx--;
            } else if (leftDmg > rightDmg) {
                accumulatedDmg[overwrite] = leftDmg;
                leftV = totalQArray.accumulatedDmgV[leftIdx];
                leftIdx--;
            } else {
                accumulatedDmg[overwrite] = rightDmg;
                rightV = prevQ.accumulatedDmgV[rightIdx];
                rightIdx--;
            }
            var node_cur_q = leftV / (stateTotalN + 1);
            accumulatedDmgV[overwrite] = node_cur_q * nodeN - rightV * prevNodeN;
            overwrite--;
        }
        accumulatedDmgLen = mergeLen;
    }
}