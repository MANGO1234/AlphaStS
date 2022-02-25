package com.company;

abstract class Card {
    public static int ATTACK = 0;
    public static int SKILL = 1;
    public static int POWER = 2;
    public static int CURSE = 3;
    public static int STATUS = 4;

    int cardType;
    String cardName;
    public boolean exhaustEndOfTurn;

    public Card(String cardName, int cardType) {
        this.cardType = cardType;
        this.cardName = cardName;
    }

    abstract void play(GameState state);
    abstract int energyCost(GameState state);

    @Override public String toString() {
        return "Card{" +
                "cardName='" + cardName + '\'' +
                '}';
    }

    public static class Strike extends Card {
        public Strike() {
            super("Strike", Card.ATTACK);
        }

        public void play(GameState state) {
            state.enemy.damage(6);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class Defend extends Card {
        public Defend() {
            super("Defend", Card.SKILL);
        }

        public void play(GameState state) {
            state.player.block(5);
        }

        public int energyCost(GameState state) {
            return 1;
        }
    }

    public static class Bash extends Card {
        public Bash() {
            super("Bash", Card.ATTACK);
        }

        public void play(GameState state) {
            state.enemy.damage(8);
            state.enemy.vulnerable += 2;
        }

        public int energyCost(GameState state) {
            return 2;
        }
    }
}
