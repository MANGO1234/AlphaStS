package com.alphaStS.utils;

import com.alphaStS.GameState;
import com.alphaStS.RandomGen;
import com.alphaStS.RandomGenCtx;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

public class Utils {
    public static int max(int[] arr) {
        int m = Integer.MIN_VALUE;
        for (int i = 0; i < arr.length; i++) {
            m = Math.max(m, arr[i]);
        }
        return m;
    }

    public static boolean arrayEqualLen(int[] a1, int[] a2, int len) {
        if (len > a1.length || len > a2.length) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }
        return true;
    }

    public static int arrayHashCodeLen(int[] a, int len) {
        int result = 1;
        for (int i = 0; i < len; i++) {
            result = 31 * result + a[i];
        }
        return result;
    }

    public static float[] arrayCopy(float[] from, int start, int len) {
        var to = new float[len];
        for (int i = 0; i < len; i++) {
            to[i] = from[start + i];
        }
        return to;
    }

    public static String formatFloat(double f) {
        if (f == 0) {
            return "0";
        } else if (f < 0.001) {
            return String.format("%6.3e", f).trim();
        } else {
            return String.format("%6.3f", f).trim();
        }
    }

    public static void sleep(long ms) {
        try {
            if (ms > 0) Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int[] shortToIntArray(short[] arr) {
        int[] result = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = arr[i];
        }
        return result;
    }

    public static boolean equals(short[] arr1, short[] arr2, int len) {
        for (int i = 0; i < len; i++) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(boolean[] arr1, boolean[] arr2, int len) {
        for (int i = 0; i < len; i++) {
            if (arr1[i] != arr2[i]) {
                return false;
            }
        }
        return true;
    }

    public static void shuffle(GameState state, short[] arr, int len, RandomGen rand) {
        for (int i = len - 1; i >= 0; i--) {
            int j = rand.nextInt(i + 1, RandomGenCtx.CardDraw, new Tuple3<>(state, 0, 0));
            short temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    public static void shuffle(GameState state, short[] arr, int len, int start, RandomGen rand) {
        for (int i = len - 1; i >= start; i--) {
            int j = rand.nextInt(i + 1, RandomGenCtx.CardDraw, new Tuple3<>(state, 0, 0));
            short temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    public static float[] nArrayToPolicy(int[] n) {
        float[] policy = new float[n.length];
        var total_n = 0;
        for (int i = 0; i < n.length; i++) {
            total_n += n[i];
        }
        for (int i = 0; i < n.length; i++) {
            policy[i] = n[i] / (float) total_n;
        }
        return policy;
    }

    public static double calcKLDivergence(float[] p, float[] q) {
        if (p.length != q.length) {
            throw new IllegalArgumentException();
        }
        var kld = 0.0;
        for (int j = 0; j < q.length; j++) {
            var t = q[j];
            if (q[j] == 0) {
                t = 0.001f;
            }
            if (p[j] > 0) {
                kld += p[j] * Math.log(p[j] / t);
            }
        }
        return kld;
    }

    public static double[] chiSquareInverseProb = new double[5000];
    public static void initChiSquareInverseProb() {
        for (int i = 0; i < 5000; i++) {
            chiSquareInverseProb[i] = (i + 1) / new ChiSquaredDistribution(i + 1).inverseCumulativeProbability(0.01);
        }
    }

    public static double getChiSquareInverseProb(int degree) {
        if (degree <= chiSquareInverseProb.length) {
            return chiSquareInverseProb[degree - 1];
        }
        if (degree <= 5698) {
            return 1.045;
        }
        if (degree <= 7161) {
            return 1.04;
        }
        if (degree <= 9289) {
            return 1.035;
        }
        if (degree <= 12554) {
            return 1.03;
        }
        if (degree <= 17951) {
            return 1.025;
        }
        if (degree <= 27850) {
            return 1.02;
        }
        if (degree <= 49159) {
            return 1.015;
        }
        return 1;
    }

    /**
     * Converts a camelCase string to a display string with spaces.
     * Examples: "FearPotion" -> "Fear Potion", "PotionOfCapacity" -> "Potion Of Capacity"
     */
    public static String camelCaseToDisplayString(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }

        StringBuilder result = new StringBuilder();
        boolean isFirstChar = true;

        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);

            if (Character.isUpperCase(c)) {
                if (!isFirstChar) {
                    result.append(' ');
                }
                result.append(c);
            } else {
                result.append(c);
            }
            isFirstChar = false;
        }

        return result.toString();
    }
}