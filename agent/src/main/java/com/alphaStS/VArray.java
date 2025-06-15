package com.alphaStS;

public class VArray {
    private final double[] data;
    private final int length;
    
    public VArray(int length) {
        this.length = length;
        this.data = new double[length];
    }
    
    public VArray(double[] data) {
        this.data = data;
        this.length = data.length;
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
        return length;
    }
    
    public double[] getData() {
        return data;
    }
    
    public void fill(double value) {
        for (int i = 0; i < length; i++) {
            data[i] = value;
        }
    }
    
    public void copyFrom(VArray other) {
        System.arraycopy(other.data, 0, this.data, 0, Math.min(this.length, other.length));
    }
    
    public void copyFrom(double[] other) {
        System.arraycopy(other, 0, this.data, 0, Math.min(this.length, other.length));
    }
    
    public void copyTo(double[] dest) {
        System.arraycopy(this.data, 0, dest, 0, Math.min(this.length, dest.length));
    }
    
    public VArray clone() {
        VArray result = new VArray(this.length);
        result.copyFrom(this);
        return result;
    }
}