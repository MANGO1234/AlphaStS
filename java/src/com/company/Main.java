package com.company;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.Bash(), 1));
        cards.add(new CardCount(new Card.Strike(), 5));
        cards.add(new CardCount(new Card.Defend(), 4));
        var state = new GameState(new Enemy.GremlinNob(85), new Player(68, 75), cards);
        state.startTurn();
        MCTS mcts = new MCTS();
        for (int i = 0; i < 100; i++) {
            mcts.search(state, false, -1);
        }
        mcts.setRoot(state);
        mcts.printTree(3);
    }
}
