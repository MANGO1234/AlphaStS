package com.alphaStS.enemy;

import com.alphaStS.Card;
import com.alphaStS.GameProperties;
import com.alphaStS.GameState;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class EnemyReadOnly {
    protected int health;
    protected int block;
    protected int strength;
    protected int vulnerable;
    protected int weak;
    protected int artifact;
    protected int regeneration;
    protected int metallicize;
    protected int loseStrengthEot;
    protected int move;
    protected int[] moveHistory;
    public int maxHealth;
    public int origHealth;
    public int numOfMoves;
    public boolean isElite = false;
    public boolean canVulnerable = false;
    public boolean canEntangle = false;
    public boolean canWeaken = false;
    public boolean canFrail = false;
    public boolean canSlime = false;
    public boolean canDaze = false;
    public boolean canGainStrength = false;
    public boolean canGainRegeneration = false;
    public boolean canGainMetallicize = false;
    public boolean canGainBlock = false;
    public boolean changePlayerStrength = false;
    public boolean changePlayerDexterity = false;
    protected boolean hasBurningHealthBuff = false;
    public boolean hasArtifact = false;

    public EnemyReadOnly(int health, int numOfMoves) {
        this.health = health;
        maxHealth = health;
        origHealth = health;
        this.numOfMoves = numOfMoves;
        move = -1;
    }

    public abstract void doMove(GameState state);
    public abstract Enemy copy();
    public abstract String getMoveString(GameState state, int move);
    public void gamePropertiesSetup(GameState state) {}
    public List<Card> getPossibleGeneratedCards() { return List.of(); }
    public int getNNInputLen(GameProperties prop) { return 0; }
    public String getNNInputDesc(GameProperties prop) { return null; }
    public int writeNNInput(GameProperties prop, float[] input, int idx) { return 0; }

    public String getMoveString(GameState state) {
        return getMoveString(state, this.move);
    }

    public abstract String getName();

    protected void setSharedFields(Enemy other) {
        health = other.health;
        block = other.block;
        strength = other.strength;
        vulnerable = other.vulnerable;
        weak = other.weak;
        loseStrengthEot = other.loseStrengthEot;
        artifact = other.artifact;
        move = other.move;
        numOfMoves = other.numOfMoves;
        maxHealth = other.maxHealth;
        origHealth = other.origHealth;
        regeneration = other.regeneration;
        metallicize = other.metallicize;
        canGainBlock = other.canGainBlock;
        canGainStrength = other.canGainStrength;
        canGainRegeneration = other.canGainRegeneration;
        canGainMetallicize = other.canGainMetallicize;
        hasBurningHealthBuff = other.hasBurningHealthBuff();
        if (other.moveHistory != null) {
            for (int i = 0; i < other.moveHistory.length; i++) {
                moveHistory[i] = other.moveHistory[i];
            }
        }
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

    public int getLoseStrengthEot() {
        return loseStrengthEot;
    }

    public int getVulnerable() {
        return vulnerable;
    }

    public int getWeak() {
        return weak;
    }

    public int getRegeneration() {
        return regeneration;
    }

    public int getMetallicize() {
        return metallicize;
    }

    public int getArtifact() {
        return artifact;
    }

    public boolean hasBurningHealthBuff() {
        return hasBurningHealthBuff;
    }

    public int getMove() {
        return move;
    }

    public int[] getMoveHistory() {
        return moveHistory;
    }

    public String toString(GameState state) {
        String str = this.getName() + "{hp=" + health;
        if (strength != 0) {
            str += ", str=" + strength;
        }
        if (loseStrengthEot != 0) {
            str += ", gainStrEot=" + -loseStrengthEot;
        }
        if (move >= 0) {
            str += ", move=" + getMoveString(state);
        }
        if (block > 0) {
            str += ", block=" + block;
        }
        if (vulnerable > 0) {
            str += ", vuln=" + vulnerable;
        }
        if (weak > 0) {
            str += ", weak=" + weak;
        }
        if (artifact > 0) {
            str += ", art=" + artifact;
        }
        if (regeneration > 0) {
            str += ", regen=" + regeneration;
        }
        if (metallicize > 0) {
            str += ", metallicize=" + metallicize;
        }
        return str + '}';
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Enemy enemy = (Enemy) o;
        if (health == 0 && health == enemy.health) {
            return true;
        }
        return health == enemy.health && maxHealth == enemy.maxHealth && block == enemy.block
                && strength == enemy.strength && vulnerable == enemy.vulnerable && weak == enemy.weak
                && artifact == enemy.artifact && move == enemy.move && Arrays.equals(moveHistory, enemy.moveHistory);
    }

    @Override public int hashCode() {
        int result = Objects.hash(health, maxHealth, block, strength, vulnerable, weak, artifact, move);
        result = 31 * result + Arrays.hashCode(moveHistory);
        return result;
    }
}
