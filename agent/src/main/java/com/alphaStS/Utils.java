package com.alphaStS;

public class Utils {
    public static int max(int[] arr) {
        int m = Integer.MIN_VALUE;
        for (int i = 0; i < arr.length; i++) {
            m = Math.max(m, arr[i]);
        }
        return m;
    }
}