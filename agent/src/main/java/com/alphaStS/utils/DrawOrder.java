package com.alphaStS.utils;

public class DrawOrder extends DrawOrderReadOnly {
    public DrawOrder(int startLen) {
        super(startLen);
    }

    public DrawOrder(DrawOrder other) {
        super(other);
    }

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

    public void transformTopMost(int cardIdx, int newCardIdx) {
        for (int i = len - 1; i >= 0 ; i--) {
            if (order[i] == cardIdx) {
                order[i] = newCardIdx;
                return;
            }
        }
    }
}
