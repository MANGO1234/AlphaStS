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
    int loseStrengthEot;
    int loseDexterityEot;

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
        loseStrengthEot = other.loseStrengthEot;
        loseDexterityEot = other.loseDexterityEot;
    }

    public void damage(int n) {
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

    public void applyDebuff(GameState state, DebuffType type, int n) {
        if (state.prop.hasGinger && type == DebuffType.WEAK) {
            return;
        } else if (state.prop.hasTurnip && type == DebuffType.FRAIL) {
            return;
        }
        if (artifact > 0) {
            artifact--;
            return;
        }
        switch (type) {
        case VULNERABLE -> this.vulnerable += n;
        case WEAK -> this.weak += n;
        case FRAIL -> this.frail += n;
        case LOSE_STRENGTH -> this.gainStrength(-2);
        case LOSE_DEXTERITY -> this.gainDexterity(-2);
        case LOSE_STRENGTH_EOT -> this.loseStrengthEot = 2;
        case LOSE_DEXTERITY_EOT -> this.loseDexterityEot = 2;
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
        if (loseStrengthEot > 0) {
            applyDebuff(state, DebuffType.LOSE_STRENGTH, loseStrengthEot);
        }
        if (loseDexterityEot > 0) {
            applyDebuff(state, DebuffType.LOSE_STRENGTH, loseDexterityEot);
        }
        if ((state.buffs & PlayerBuff.BARRICADE.mask()) != 0) {
        } else if (state.prop.hasCaliper) {
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
        if (loseStrengthEot > 0) {
            str += ", loseStrEot=" + loseStrengthEot;
        }
        if (loseDexterityEot > 0) {
            str += ", loseDexEot=" + loseDexterityEot;
        }
        return str + '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Player player = (Player) o;
        return origHealth == player.origHealth && maxHealth == player.maxHealth && health == player.health && block == player.block && strength == player.strength && dexterity == player.dexterity && vulnerable == player.vulnerable && weak == player.weak && frail == player.frail && artifact == player.artifact && cannotDrawCard == player.cannotDrawCard && loseStrengthEot == player.loseStrengthEot && loseDexterityEot == player.loseDexterityEot;
    }

    @Override public int hashCode() {
        return Objects.hash(origHealth, maxHealth, health, block, strength, dexterity, vulnerable, weak, frail, artifact, cannotDrawCard, loseStrengthEot, loseDexterityEot);
    }
}
