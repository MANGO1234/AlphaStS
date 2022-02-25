package com.company;

public class Player {
    int origHealth;
    int maxHealth;
    int health;
    int block;
    int strength;
    int vulnerable;

    public Player(int health, int maxHealth) {
        this.maxHealth = maxHealth;
        this.health = health;
        origHealth = health;
    }

    public Player(Player other) {
        origHealth = other.origHealth;
        maxHealth = other.maxHealth;
        health = other.health;
        block = other.block;
        strength = other.strength;
        vulnerable = other.vulnerable;
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

    void doAttack(Enemy enemy, int n) {
        enemy.damage(n + enemy.strength);
    }

    void endTurn() {
        if (vulnerable > 0) {
            vulnerable -= 1;
        }
        block = 0;
    }

    @Override public String toString() {
        return "Player{" +
                "health=" + health +
                ", block=" + block +
                ", vulnerable=" + vulnerable +
                '}';
    }
}
