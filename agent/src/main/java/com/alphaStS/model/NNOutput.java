package com.alphaStS.model;

public record NNOutput(float v_health, float v_win, float[] v_other, float[] policy, int[] legalActions) {
}
