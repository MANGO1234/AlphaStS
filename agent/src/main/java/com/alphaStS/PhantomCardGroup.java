package com.alphaStS;

import com.alphaStS.card.Card;

import java.util.ArrayList;
import java.util.List;

public class PhantomCardGroup {
    Card card;
    int phantomCardIdxStart = -100000;
    List<Integer> cardIdxes = new ArrayList<>();

    public PhantomCardGroup(Card card) {
        this.card = card;
    }
}
