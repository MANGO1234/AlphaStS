package com.company;

abstract class Enemy {
    int health;
    int maxHealth;
    int strength;
    int vulnerable;
    int block;
    int numOfMoves;
    int move;
    int lastMove;
    int lastLastMove;

    public abstract Enemy copy();
    public abstract void doMove(Player player);
    public abstract void nextMove();
    public abstract void react(Card card);

    public Enemy(int health) {
        this.health = health;
        maxHealth = health;
        move = -1;
        lastMove = -1;
        lastLastMove = -1;
    }

    void damage(int n) {
        if (vulnerable > 0) {
            n = n + n / 2;
        }
        health -= Math.max(0, n - block);
        block = Math.max(0, block - n);
        if (health < 0) {
            health = 0;
        }
    }

    void block(int n) {
        block += n;
        if (block > 999) {
            block = 999;
        }
    }

    void endTurn() {
        if (vulnerable > 0) {
            vulnerable -= 1;
        }
        block = 0;
    }

    public static class GremlinNob extends Enemy {
        int turn;

        public GremlinNob(int health) {
            super(health);
            numOfMoves = 3;
        }

        public GremlinNob(GremlinNob other) {
            super(other.health);
            maxHealth = other.maxHealth;
            strength = other.strength;
            vulnerable = other.vulnerable;
            block = other.block;
            numOfMoves = other.numOfMoves;
            move = other.move;
            lastMove = other.lastMove;
            lastLastMove = other.lastLastMove;
        }

        @Override public void doMove(Player player) {
            if (move == 1) {
                player.damage(8);
                player.vulnerable += 3;
            } else if (move == 2) {
                player.damage(16);
            }
        }

        @Override public void nextMove() {
            lastLastMove = lastMove;
            lastMove = move;
            if (turn == 0) {
                move = 0;
                turn = 1;
                return;
            }
            int m = turn % 3;
            if (m == 1) {
                move = 1;
            } else {
                move = 2;
            }
            turn += 1;
        }

        @Override public Enemy copy() {
            return new GremlinNob(this);
        }

        @Override public void react(Card card) {
            if (card.cardType == Card.SKILL && turn > 1) {
                strength += 3;
            }
        }

        @Override public String toString() {
            return "GremlinNob{" +
                    "health=" + health +
                    ", strength=" + strength +
                    ", vulnerable=" + vulnerable +
                    '}';
        }
    }
}
