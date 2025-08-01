package com.alphaStS.player;

import java.util.Objects;

public class PlayerReadOnly {
    protected int origHealth;
    protected int inBattleMaxHealth;
    protected int maxHealth;
    protected int health;
    protected int block;
    protected int strength;
    protected int dexterity;
    protected int vulnerable;
    protected int weak;
    protected int frail;
    protected int artifact;
    protected boolean cannotDrawCard;
    protected boolean hexed;
    protected int entangled;
    protected int loseStrengthEot;
    protected int loseDexterityEot;
    protected int platedArmor;
    protected short noMoreBlockFromCards;
    protected int accumulatedDamage;

    public PlayerReadOnly(int health, int maxHealth) {
        this(health, maxHealth, maxHealth);
    }

    public PlayerReadOnly(int health, int maxHealth, int inBattleMaxHealth) {
        this.maxHealth = maxHealth;
        this.inBattleMaxHealth = inBattleMaxHealth;
        this.health = health;
        origHealth = health;
    }

    public PlayerReadOnly(PlayerReadOnly other) {
        origHealth = other.origHealth;
        maxHealth = other.maxHealth;
        inBattleMaxHealth = other.inBattleMaxHealth;
        health = other.health;
        block = other.block;
        strength = other.strength;
        dexterity = other.dexterity;
        vulnerable = other.vulnerable;
        weak = other.weak;
        frail = other.frail;
        artifact = other.artifact;
        cannotDrawCard = other.cannotDrawCard;
        entangled = other.entangled;
        hexed = other.hexed;
        loseStrengthEot = other.loseStrengthEot;
        loseDexterityEot = other.loseDexterityEot;
        platedArmor = other.platedArmor;
        noMoreBlockFromCards = other.noMoreBlockFromCards;
        accumulatedDamage = other.accumulatedDamage;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getInBattleMaxHealth() {
        return inBattleMaxHealth;
    }

    public int getOrigHealth() {
        return origHealth;
    }

    public int getHealth() {
        return health;
    }

    public int getBlock() {
        return block;
    }

    public int getStrength() {
        return strength;
    }

    public int getDexterity() {
        return dexterity;
    }

    public int getVulnerable() {
        return vulnerable;
    }

    public int getWeak() {
        return weak;
    }

    public int getFrail() {
        return frail;
    }

    public int getArtifact() {
        return artifact;
    }

    public boolean cannotDrawCard() {
        return cannotDrawCard;
    }

    public int getLoseStrengthEot() {
        return loseStrengthEot;
    }

    public int getLoseDexterityEot() {
        return loseDexterityEot;
    }

    public int getPlatedArmor() {
        return platedArmor;
    }

    public boolean isEntangled() {
        return entangled > 0;
    }

    public boolean isHexed() {
        return hexed;
    }

    public int getAccumulatedDamage() {
        return accumulatedDamage;
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
        if (isEntangled()) {
            str += ", entangled";
        }
        if (hexed) {
            str += ", hexed";
        }
        if (loseStrengthEot > 0) {
            str += ", loseStrEot=" + loseStrengthEot;
        }
        if (loseDexterityEot > 0) {
            str += ", loseDexEot=" + loseDexterityEot;
        }
        if (noMoreBlockFromCards > 0) {
            str += ", noMoreBlockFromCards=" + noMoreBlockFromCards;
        }
        return str + '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PlayerReadOnly that = (PlayerReadOnly) o;
        return origHealth == that.origHealth && maxHealth == that.maxHealth && inBattleMaxHealth == that.inBattleMaxHealth && health == that.health && block == that.block && strength == that.strength && dexterity == that.dexterity && vulnerable == that.vulnerable && weak == that.weak && frail == that.frail && artifact == that.artifact && cannotDrawCard == that.cannotDrawCard && entangled == that.entangled && hexed == that.hexed && loseStrengthEot == that.loseStrengthEot && loseDexterityEot == that.loseDexterityEot && noMoreBlockFromCards == that.noMoreBlockFromCards && accumulatedDamage == that.accumulatedDamage;
    }

    @Override public int hashCode() {
        return Objects.hash(origHealth, maxHealth, health, block, strength, dexterity, vulnerable, weak, frail, artifact, cannotDrawCard, entangled, hexed, loseStrengthEot, loseDexterityEot);
    }
}
