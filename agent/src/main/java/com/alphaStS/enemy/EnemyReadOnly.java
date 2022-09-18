package com.alphaStS.enemy;

import com.alphaStS.Card;
import com.alphaStS.GameProperties;
import com.alphaStS.GameState;

import java.util.List;
import java.util.Objects;

public abstract class EnemyReadOnly {
    public static class EnemyProperty {
        public int numOfMoves = 0;
        public boolean isElite = false;
        public boolean isMinion = false;
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
        public boolean hasBurningEliteBuff = false;
        public boolean hasArtifact = false;
        public boolean useLast2MovesForMoveSelection = false;

        public EnemyProperty(int numOfMoves, boolean useLast2MovesForMoveSelection) {
            this.numOfMoves = numOfMoves;
            this.useLast2MovesForMoveSelection = useLast2MovesForMoveSelection;
        }

        public void applyBurningEliteBuff() {
            canGainMetallicize = true;
            canGainRegeneration = true;
            // todo
        }

        public boolean hasBurningEliteBuff() {
            return hasBurningEliteBuff;
        }
    }

    protected static EnemyProperty defaultProperty;
    public EnemyProperty property = defaultProperty;
    protected int health;
    protected int block;
    protected int strength;
    protected int vulnerable;
    protected int weak;
    protected int artifact;
    protected int regeneration;
    protected int metallicize;
    protected int loseStrengthEot;
    protected int move = -1;
    protected int lastMove = -1;
    public int maxHealth;
    public int origHealth;
    protected boolean hasBurningHealthBuff = false;

    public EnemyReadOnly(int health) {
        this.health = health;
        maxHealth = health;
        origHealth = health;
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
        property = other.property;
        health = other.health;
        block = other.block;
        strength = other.strength;
        vulnerable = other.vulnerable;
        weak = other.weak;
        loseStrengthEot = other.loseStrengthEot;
        artifact = other.artifact;
        move = other.move;
        lastMove = other.lastMove;
        maxHealth = other.maxHealth;
        origHealth = other.origHealth;
        regeneration = other.regeneration;
        metallicize = other.metallicize;
        hasBurningHealthBuff = other.hasBurningHealthBuff();
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

    public int getLastMove() {
        return lastMove;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public boolean isTargetable() {
        return health > 0;
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

    public String toString() {
        String str = this.getName() + "{hp=" + health;
        if (strength != 0) {
            str += ", str=" + strength;
        }
        if (loseStrengthEot != 0) {
            str += ", gainStrEot=" + -loseStrengthEot;
        }
        if (move >= 0) {
            str += ", move=" + move;
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
        return health == enemy.health && move == enemy.move && lastMove == enemy.lastMove &&
                maxHealth == enemy.maxHealth && block == enemy.block && strength == enemy.strength
                && vulnerable == enemy.vulnerable && weak == enemy.weak && artifact == enemy.artifact;
    }

    @Override public int hashCode() {
        return Objects.hash(health, maxHealth, block, strength, vulnerable, weak, artifact, move, lastMove);
    }
}
