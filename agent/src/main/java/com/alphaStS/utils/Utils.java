package com.alphaStS.utils;

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
}