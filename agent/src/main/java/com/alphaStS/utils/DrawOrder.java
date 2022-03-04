package com.alphaStS.utils;

import java.util.Arrays;
import java.util.Objects;

public class DrawOrder {
    int[] order;
    int len;

    public DrawOrder(int startLen) {
        order = new int[startLen];
        len = 0;
    }

    public DrawOrder(DrawOrder other) {
        order = Arrays.copyOf(other.order, other.order.length);
        len = other.len;
    }

    // todo resizable
    public void pushOnTop(int cardIdx) {
        order[len++] = cardIdx;
    }

    public int drawTop() {
        if (len == 0) {
            return -1;
        }
        return order[--len];
    }

    public void clear() {
        len = 0;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DrawOrder drawOrder = (DrawOrder) o;
        return len == drawOrder.len && Utils.arrayEqualLen(order, drawOrder.order, len);
    }

    @Override public int hashCode() {
        int result = Objects.hash(len);
        result = 31 * result + Utils.arrayHashCodeLen(order, len);
        return result;
    }

    public int size() {
        return len;
    }

    public int ithCardFromTop(int i) {
        return order[len - 1 - i];
    }
}
