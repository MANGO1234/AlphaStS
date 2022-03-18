package com.alphaStS;

import java.util.Objects;

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
    boolean cannotDrawCard;

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
        artifact = other.artifact;
        cannotDrawCard = other.cannotDrawCard;
    }

    public void damage(int n) {
        if (vulnerable > 0) {
            n = n + n / 2;
        }
        health -= Math.max(0, n - block);
        block = Math.max(0, block - n);
        if (health < 0) {
            health = 0;
        }
    }

    public void nonAttackDamage(int n, boolean blockable) {
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

    public void doDamageToEnemy(GameState state, Enemy enemy, int n) {
        if (weak > 0) {
            n = n * 3 / 4;
        }
        enemy.damage(n + strength, state);
    }

    public void heal(int n) {
        health += Math.min(n, maxHealth - health);
    }

    public void gainBlock(int n) {
        n += dexterity;
        n = frail > 0? n * 3 / 4 : n;
        block += n;
        if (block > 999) {
            block = 999;
        }
    }

    public void gainBlockNotFromCardPlay(int n) {
        block += n;
        if (block > 999) {
            block = 999;
        }
    }

    public void gainStrength(int n) {
        strength += n;
        strength = Math.min(999, Math.max(-999, strength));
    }

    public void gainDexterity(int n) {
        dexterity += n;
        dexterity = Math.min(999, Math.max(-999, dexterity));
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
        case LOSE_DEXTERITY -> this.gainDexterity(-2);
        case LOSE_STRENGTH -> this.gainStrength(-2);
        case NO_MORE_CARD_DRAW -> this.cannotDrawCard = true;
        }
    }

    public void endTurn(GameState state) {
        if (vulnerable > 0) {
            vulnerable -= 1;
        }
        if (weak > 0) {
            weak -= 1;
        }
        if (frail > 0) {
            frail -= 1;
        }
        cannotDrawCard = false;
        if ((state.buffs & PlayerBuffs.BARRICADE) != 0) {
        } else if ((state.buffs & PlayerBuffs.CALIPERS) != 0) {
            block = Math.max(block - 15, 0);
        } else {
            block = 0;
        }
    }

    @Override public String toString() {
        String str = "Player{health=" + health;
        if (block > 0) {
            str += ", block=" + block;
        }
        if (strength != 0) {
            str += ", str=" + strength;
        }
        if (dexterity != 0) {
            str += ", dex=" + dexterity;
        }
        if (vulnerable > 0) {
            str += ", vuln=" + vulnerable;
        }
        if (weak > 0) {
            str += ", weak=" + weak;
        }
        if (frail > 0) {
            str += ", frail=" + frail;
        }
        if (artifact > 0) {
            str += ", art=" + artifact;
        }
        if (cannotDrawCard) {
            str += ", cannotDraw=true";
        }
        return str + '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Player player = (Player) o;
        return origHealth == player.origHealth && maxHealth == player.maxHealth && health == player.health && block == player.block && strength == player.strength && dexterity == player.dexterity && vulnerable == player.vulnerable && weak == player.weak && frail == player.frail && artifact == player.artifact && cannotDrawCard == player.cannotDrawCard;
    }

    @Override public int hashCode() {
        return Objects.hash(origHealth, maxHealth, health, block, strength, dexterity, vulnerable, weak, frail, artifact, cannotDrawCard);
    }
}
