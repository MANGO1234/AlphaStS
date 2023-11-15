package com.alphaStS.enemy;

import com.alphaStS.Card;
import com.alphaStS.GameProperties;
import com.alphaStS.GameState;

import java.util.List;
import java.util.Objects;

public abstract class EnemyReadOnly {
    public static class EnemyProperties implements Cloneable {
        public int numOfMoves;
        public int maxHealth;
        public int origHealth; // during randomization, property cloning can be set to change origHealth, origMaxHealth and hasBurningHealthBuff for that battle
        public int origMaxHealth; // during randomization, property cloning can be set to change origHealth and hasBurningHealthBuff for that battle
        public int actNumber;
        protected boolean hasBurningHealthBuff = false;
        public boolean isElite = false;
        public boolean isBoss = false;
        public boolean isMinion = false;
        public boolean canVulnerable = false;
        public boolean canEntangle = false;
        public boolean canWeaken = false;
        public boolean canFrail = false;
        public boolean canSlime = false;
        public boolean canDaze = false;
        public boolean canGainStrength = false;
        public boolean canGainRegeneration = false;
        public boolean canHeal = false;
        public boolean canGainMetallicize = false;
        public boolean canGainPlatedArmor = false;
        public boolean canGainBlock = false;
        public boolean changePlayerStrength = false;
        public boolean changePlayerFocus = false;
        public boolean changePlayerDexterity = false;
        public boolean hasBurningEliteBuff = false;
        public boolean hasArtifact = false;
        public boolean useLast2MovesForMoveSelection;
        public boolean canSelfRevive;
        public boolean isAct3;

        public EnemyProperties(int numOfMoves, boolean useLast2MovesForMoveSelection) {
            this.numOfMoves = numOfMoves;
            this.useLast2MovesForMoveSelection = useLast2MovesForMoveSelection;
        }

        public void applyBurningEliteBuff() {
            hasBurningEliteBuff= true;
            canGainMetallicize = true;
            canGainRegeneration = true;
            maxHealth = (int) (maxHealth * 1.25);
        }

        public boolean hasBurningEliteBuff() {
            return hasBurningEliteBuff;
        }

        public EnemyProperties clone() {
            try {
                return (EnemyProperties) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public EnemyProperties properties;
    protected int health;
    protected int block;
    protected int strength;
    protected int vulnerable;
    protected int weak;
    protected int artifact;
    protected int poison;
    protected int regeneration;
    protected int metallicize;
    protected int platedArmor;
    protected int loseStrengthEot;
    protected int corpseExplosion;
    protected int lockOn;
    protected int move = -1;
    protected int lastMove = -1;

    public EnemyReadOnly(int health, int numOfMoves, boolean useLast2MovesForMoveSelection) {
        this.health = health;
        properties = new EnemyProperties(numOfMoves, useLast2MovesForMoveSelection);
        properties.maxHealth = health;
        properties.origMaxHealth = health;
        properties.origHealth = health;
    }

    public EnemyReadOnly(EnemyReadOnly other) {
        this.properties = other.properties;
    }

    public abstract void doMove(GameState state, EnemyReadOnly self);
    public abstract Enemy copy();
    public abstract String getMoveString(GameState state, int move);
    public void gamePropertiesSetup(GameState state) {}
    public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(); }
    public int getNNInputLen(GameProperties prop) { return 0; }
    public String getNNInputDesc(GameProperties prop) { return null; }
    public int writeNNInput(GameProperties prop, float[] input, int idx) { return 0; }

    public String getMoveString(GameState state) {
        return getMoveString(state, this.move);
    }

    public String getLastMoveString(GameState state) {
        return getMoveString(state, this.lastMove);
    }

    public abstract String getName();

    protected void copyFieldsFrom(Enemy other) {
        properties = other.properties;
        health = other.health;
        block = other.block;
        strength = other.strength;
        vulnerable = other.vulnerable;
        weak = other.weak;
        artifact = other.artifact;
        poison = other.poison;
        loseStrengthEot = other.loseStrengthEot;
        regeneration = other.regeneration;
        metallicize = other.metallicize;
        platedArmor = other.platedArmor;
        corpseExplosion = other.corpseExplosion;
        lockOn = other.lockOn;
        move = other.move;
        lastMove = other.lastMove;
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

    public int getPlatedArmor() {
        return platedArmor;
    }

    public int getArtifact() {
        return artifact;
    }

    public int getPoison() {
        return poison;
    }

    public int getCorpseExplosion() {
        return corpseExplosion;
    }

    public int getLockOn() {
        return lockOn;
    }

    public boolean hasBurningHealthBuff() {
        return properties.hasBurningHealthBuff;
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
            if (state.properties.hasRunicDome) {
                str += ", lastMove=" + getMoveString(state);
            } else {
                str += ", move=" + getMoveString(state);
            }
            if (properties.useLast2MovesForMoveSelection) {
                if (lastMove >= 0) {
                    str += " [prev=" + getLastMoveString(state) + "]";
                }
            }
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
        if (poison > 0) {
            str += ", poison=" + poison;
        }
        if (regeneration > 0) {
            str += ", regen=" + regeneration;
        }
        if (metallicize > 0) {
            str += ", metallicize=" + metallicize;
        }
        if (platedArmor > 0) {
            str += ", platedArmor=" + platedArmor;
        }
        if (corpseExplosion > 0) {
            str += ", corpseExplosion=" + corpseExplosion;
        }
        if (lockOn > 0) {
            str += ", lockOn=" + lockOn;
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
        if (poison > 0) {
            str += ", poison=" + poison;
        }
        if (regeneration > 0) {
            str += ", regen=" + regeneration;
        }
        if (metallicize > 0) {
            str += ", metallicize=" + metallicize;
        }
        if (platedArmor > 0) {
            str += ", platedArmor=" + platedArmor;
        }
        if (corpseExplosion > 0) {
            str += ", corpseExplosion=" + corpseExplosion;
        }
        if (lockOn > 0) {
            str += ", lockOn=" + lockOn;
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
        return health == enemy.health && move == enemy.move && lastMove == enemy.lastMove && block == enemy.block &&
                strength == enemy.strength && vulnerable == enemy.vulnerable && weak == enemy.weak && artifact == enemy.artifact &&
                poison == enemy.poison && loseStrengthEot == enemy.loseStrengthEot && corpseExplosion == enemy.corpseExplosion && lockOn == enemy.lockOn;
    }

    @Override public int hashCode() {
        return Objects.hash(health, block, strength, vulnerable, weak, artifact, poison, move, lastMove);
    }
}
