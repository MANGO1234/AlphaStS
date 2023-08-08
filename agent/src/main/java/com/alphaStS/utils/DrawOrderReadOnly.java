package com.alphaStS.utils;

import java.util.Arrays;
import java.util.Objects;

public class DrawOrderReadOnly {
    protected int[] order;
    protected int len;

    public DrawOrderReadOnly(int startLen) {
        order = new int[startLen];
        len = 0;
    }

    public DrawOrderReadOnly(DrawOrder other) {
        order = Arrays.copyOf(other.order, other.order.length);
        len = other.len;
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

    public boolean contains(int cardIndex) {
        for (int i = 0; i < len; i++) {
            if (order[i] == cardIndex) {
                return true;
            }
        }
        return false;
    }

    public void remove(int cardIndex) {
        for (int i = 0; i < len; i++) {
            if (order[i] == cardIndex) {
                for (int j = i; j < len - 1; j++) {
                    order[j] = order[j + 1];
                }
                len--;
                return;
            }
        }
    }
}
