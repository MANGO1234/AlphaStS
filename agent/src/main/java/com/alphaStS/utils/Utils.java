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

}