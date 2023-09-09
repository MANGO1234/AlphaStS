package com.alphaStS.model;

import java.util.Arrays;

class NNInputHash {
    float[] input;
    int hashCode;

    public NNInputHash(float[] input) {
        this.input = input;
        hashCode = Arrays.hashCode(input);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        NNInputHash inputHash = (NNInputHash) o;
        return hashCode == inputHash.hashCode && Arrays.equals(input, inputHash.input);
    }

    @Override public int hashCode() {
        return hashCode;
    }
}
