package com.alphaStS;

public class Player {
    int origHealth;
    int maxHealth;
    int health;
    int block;
    int strength;
    int dexterity;
    int vulnerable;
    int weak;
    int frail;
    int artifact;

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
        dexterity = other.dexterity;
        vulnerable = other.vulnerable;
        weak = other.weak;
        frail = other.frail;
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

    void nonAttackDamage(int n, boolean blockable) {
        if (blockable) {
            health -= Math.max(0, n - block);
            block = Math.max(0, block - n);
        } else {
            health -= n;
        }
        if (health < 0) {
            health = 0;
        }
    }

    public void applyDebuff(DebuffType type, int n) {
        if (artifact > 0) {
            artifact--;
            return;
        }
        switch (type) {
        case VULNERABLE -> this.vulnerable += n;
        case WEAK -> this.weak += n;
        case FRAIL -> this.frail += n;
        }
    }

    void doDamageToEnemy(GameState state, Enemy enemy, int n) {
        if (weak > 0) {
            n = n * 3 / 4;
        }
        enemy.damage(n + strength, state);
    }

    void gainBlock(int n) {
        n += dexterity;
        n = frail > 0? n * 3 / 4 : n;
        block += n;
        if (block > 999) {
            block = 999;
        }
    }

    void gainBlockNoDex(int n) {
        block += n;
        if (block > 999) {
            block = 999;
        }
    }

    void endTurn() {
        if (vulnerable > 0) {
            vulnerable -= 1;
        }
        if (weak > 0) {
            weak -= 1;
        }
        block = 0;
    }

    @Override public String toString() {
        String str = "Player{health=" + health;
        if (strength > 0) {
            str += ", str=" + strength;
        }
        if (block > 0) {
            str += ", block=" + block;
        }
        if (vulnerable > 0) {
            str += ", vuln=" + vulnerable;
        }
        if (weak > 0) {
            str += ", vuln=" + weak;
        }
        return str + '}';
    }

    public int calcDamage(int n) {
        if (vulnerable > 0) {
            return n + n / 2;
        }
        return n;
    }

    public void heal(int n) {
        health += Math.min(n, maxHealth - health);
    }

    public void gainStrength(int n) {
        strength += n;
        strength = Math.min(999, Math.max(-999, strength));
    }

    public void gainDexterity(int n) {
        dexterity += n;
        dexterity = Math.min(999, Math.max(-999, dexterity));
    }
}
