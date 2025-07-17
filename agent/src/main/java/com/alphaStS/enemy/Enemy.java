package com.alphaStS.enemy;

import com.alphaStS.*;
import com.alphaStS.card.Card;

import java.util.ArrayList;
import java.util.List;

public abstract class Enemy extends EnemyReadOnly {
    public Enemy(int health, int numOfMoves, boolean useLast2MovesForMoveSelection) {
        super(health, numOfMoves, useLast2MovesForMoveSelection);
    }

    public Enemy(Enemy other) {
        super(other);
        copyFieldsFrom(other);
    }

    public abstract void nextMove(GameState state, RandomGen random);
    public void saveStateForNextMove(GameState state) {}

    public int damage(double n, GameState state) {
        if (health <= 0) {
            return 0;
        }
        int dmg = ((int) n) - block;
        if (state.properties.boot != null && dmg > 0 && dmg < 5 && state.properties.boot.isRelicEnabledInScenario(state)) {
            dmg = 5;
        }
        int dmgDone = Math.max(0, dmg);
        health -= dmgDone;
        block = Math.max(0, block - ((int) n));
        if (dmg > 0 && platedArmor > 0) {
            platedArmor--;
        }
        if (health <= 0) {
            health = 0;
        }
        return dmgDone;
    }

    public void nonAttackDamage(int n, boolean blockable, GameState state) {
        if (health <= 0) {
            return;
        }
        if (blockable) {
            health -= Math.max(0, n - block);
            block = Math.max(0, block - n);
        } else {
            health -= n;
        }
        if (health <= 0) {
            health = 0;
        }
    }

    public void gainBlock(int n) {
        block += n;
        if (block > 999) {
            block = 999;
        }
    }

    public void gainStrength(int n) {
        strength += n;
    }

    public void setHealth(int hp) {
        health = hp;
    }

    public void setMaxHealthInBattle(int hp) {
        maxHealthInBattle = hp;
    }

    public void setRegeneration(int regen) {
        regeneration = regen;
    }

    public void setMetallicize(int n) {
        metallicize = n;
    }

    public void setBurningHealthBuff(boolean b) {
        if (properties.hasBurningHealthBuff != b) {
            properties = properties.clone();
            properties.hasBurningHealthBuff = b;
        }
    }

    public void gainMetallicize(int n) {
        metallicize += n;
    }

    public void gainPlatedArmor(int n) {
        platedArmor += n;
    }

    public void removeAllDebuffs() {
        vulnerable = 0;
        weak = 0;
        loseStrengthEot = 0;
        if (strength < 0) {
            strength = 0;
        }
        poison = 0;
        lockOn = 0;
        mark = 0;
    }

    public void setMove(int move) {
        this.move = move;
    }

    public void startTurn(GameState state) {
        block = 0;
        if (poison > 0) {
            state.playerDoNonAttackDamageToEnemy(this, poison, false);
            poison--;
        }
    }

    public void endTurn(int turnNum) {
        if (turnNum > 0 && vulnerable > 0) {
            vulnerable -= 1;
        }
        if (turnNum > 0 && weak > 0) {
            weak -= 1;
        }
        if (turnNum > 0 && lockOn > 0) {
            lockOn -= 1;
        }
        if (loseStrengthEot != 0) {
            strength += loseStrengthEot;
            loseStrengthEot = 0;
        }
        if (choke != 0) {
            choke = 0;
        }
        if (!properties.isElite || (turnNum > 0 && metallicize > 0)) { // todo: burning elite
            gainBlock(metallicize);
        }
        if (platedArmor > 0) {
            gainBlock(platedArmor);
        }
        if (regeneration > 0) {
            heal(regeneration);
        }
    }

    public void reviveReset() {
        block = 0;
        strength = 0;
        vulnerable = 0;
        lockOn = 0;
        weak = 0;
        artifact = 0;
        poison = 0;
        regeneration = 0;
        metallicize = 0;
        platedArmor = 0;
        loseStrengthEot = 0;
        move = -1;
        lastMove = -1;
    }

    protected void heal(int hp) {
        if (health > 0) {
            health += Math.min(hp, Math.max(0, getMaxHealthInBattle() - health));
        }
    }

    public void react(GameState state, Card card) {
    }

    public boolean applyDebuff(GameState state, DebuffType type, int n) {
        if (health <= 0) {
            return false;
        }
        if (artifact > 0) {
            artifact--;
            return false;
        }
        switch (type) {
        case VULNERABLE -> {
            this.vulnerable += n;
            if (state.properties.championBelt != null && state.properties.championBelt.isRelicEnabledInScenario(state)) {
                this.weak += 1;
            }
        }
        case WEAK -> this.weak += n;
        case LOSE_STRENGTH -> this.gainStrength(-n);
        case LOSE_STRENGTH_EOT -> {
            this.loseStrengthEot += n;
            this.gainStrength(-n);
        }
        case POISON -> this.poison += n + (state.properties.sneckoSkull != null && state.properties.sneckoSkull.isRelicEnabledInScenario(state) ? 1 : 0);
        case CORPSE_EXPLOSION -> this.corpseExplosion += n;
        case CHOKE -> this.choke += n;
        case LOCK_ON -> this.lockOn += n;
        case TALK_TO_THE_HAND -> this.talkToTheHand += n;
        case MARK -> this.mark += n;
        }

        if (state.properties.sadisticNatureCounterIdx >= 0 && state.getCounterForRead()[state.properties.sadisticNatureCounterIdx] > 0) {
            int damage = state.getCounterForRead()[state.properties.sadisticNatureCounterIdx];
            state.playerDoNonAttackDamageToEnemy(this, damage, false);
        }

        return true;
    }

    public void setBlock(int n) {
        block = n;
    }

    public Enemy markAsBurningElite() {
        properties.applyBurningEliteBuff();
        return this;
    }

    public abstract void randomize(RandomGen random, boolean training, int difficulty);
    public int getMaxRandomizeDifficulty() {
        return 0;
    }

    // used to implement an enemy that can "morph" to one of several enemies to reduce network size for fights
    // involving gremlin. Can and will be buggy for other combination of enemies.
    public static class MergedEnemy extends Enemy {
        public List<Enemy> possibleEnemies;
        public Enemy currentEnemy;
        public int currentEnemyIdx;

        public MergedEnemy(List<Enemy> possibleEnemies) {
            super(0, possibleEnemies.stream().mapToInt((enemy) -> enemy.properties.numOfMoves).reduce(0, Integer::sum), false);
            this.possibleEnemies = possibleEnemies;
            properties.maxHealth = possibleEnemies.stream().mapToInt((enemy) -> enemy.properties.maxHealth).reduce(0, Math::max);
            properties.origMaxHealth = properties.maxHealth;
            properties.isElite = possibleEnemies.stream().anyMatch((e) -> e.properties.isElite);
            properties.isMinion = possibleEnemies.stream().anyMatch((e) -> e.properties.isMinion);
            properties.canVulnerable = possibleEnemies.stream().anyMatch((e) -> e.properties.canVulnerable);
            properties.canEntangle = possibleEnemies.stream().anyMatch((e) -> e.properties.canEntangle);
            properties.canWeaken = possibleEnemies.stream().anyMatch((e) -> e.properties.canWeaken);
            properties.canFrail = possibleEnemies.stream().anyMatch((e) -> e.properties.canFrail);
            properties.canGainStrength = possibleEnemies.stream().anyMatch((e) -> e.properties.canGainStrength);
            properties.canGainRegeneration = possibleEnemies.stream().anyMatch((e) -> e.properties.canGainRegeneration);
            properties.canHeal = possibleEnemies.stream().anyMatch((e) -> e.properties.canHeal);
            properties.canGainMetallicize = possibleEnemies.stream().anyMatch((e) -> e.properties.canGainMetallicize);
            properties.canGainPlatedArmor = possibleEnemies.stream().anyMatch((e) -> e.properties.canGainPlatedArmor);
            properties.canGainBlock = possibleEnemies.stream().anyMatch((e) -> e.properties.canGainBlock);
            properties.changePlayerStrength = possibleEnemies.stream().anyMatch((e) -> e.properties.changePlayerStrength);
            properties.changePlayerDexterity = possibleEnemies.stream().anyMatch((e) -> e.properties.changePlayerDexterity);
            properties.changePlayerFocus = possibleEnemies.stream().anyMatch((e) -> e.properties.changePlayerFocus);
            properties.hasBurningEliteBuff = possibleEnemies.stream().anyMatch((e) -> e.properties.hasBurningEliteBuff);
            properties.hasArtifact = possibleEnemies.stream().anyMatch((e) -> e.properties.hasArtifact);
            properties.actNumber = possibleEnemies.stream().mapToInt((enemy) -> enemy.properties.actNumber).reduce(0, Math::max);
            setEnemy(0);
        }

        public MergedEnemy(MergedEnemy other) {
            super(other);
            possibleEnemies = other.possibleEnemies;
            currentEnemy = other.currentEnemy.copy();
            currentEnemyIdx = other.currentEnemyIdx;
        }

        public void setEnemy(int idx) {
            currentEnemy = possibleEnemies.get(idx).copy();
            currentEnemyIdx = idx;
        }

        public EnemyProperties getEnemyProperty(int idx) {
            return possibleEnemies.get(idx).properties;
        }

        public String getDescName() {
            return "Merged Enemy (" + String.join(", ", possibleEnemies.stream().map(Enemy::getName).toList()) + ")";
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            currentEnemy.nextMove(state, random);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            currentEnemy.doMove(state, self);
        }

        @Override public Enemy copy() {
            return new MergedEnemy(this);
        }

        @Override public String getMoveString(GameState state, int move) {
            return currentEnemy.getMoveString(state, move);
        }

        @Override public String getName() {
            return currentEnemy.getName();
        }

        @Override public void gamePropertiesSetup(GameState state) {
            for (var e : possibleEnemies) {
                e.gamePropertiesSetup(state);
            }
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            var newCards = new ArrayList<Card>();
            for (var e : possibleEnemies) {
                newCards.addAll(e.getPossibleGeneratedCards(prop, cards));
            }
            return newCards;
        }

        // ******************* generated delegate methods follow ****************

        @Override public int damage(double n, GameState state) {
            return currentEnemy.damage(n, state);
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            currentEnemy.nonAttackDamage(n, blockable, state);
        }

        @Override public void gainBlock(int n) {
            currentEnemy.gainBlock(n);
        }

        @Override public void gainStrength(int n) {
            currentEnemy.gainStrength(n);
        }

        @Override public void setHealth(int hp) {
            currentEnemy.setHealth(hp);
        }

        @Override public void setMaxHealthInBattle(int hp) {
            currentEnemy.setMaxHealthInBattle(hp);
        }

        @Override public void setRegeneration(int regen) {
            currentEnemy.setRegeneration(regen);
        }

        @Override public void setMetallicize(int n) {
            currentEnemy.setMetallicize(n);
        }

        @Override public void setBurningHealthBuff(boolean b) {
            currentEnemy.setBurningHealthBuff(b);
        }

        @Override public void gainMetallicize(int n) {
            currentEnemy.gainMetallicize(n);
        }

        @Override public void removeAllDebuffs() {
            currentEnemy.removeAllDebuffs();
        }

        @Override public void setMove(int move) {
            currentEnemy.setMove(move);
        }

        @Override public void startTurn(GameState state) {
            currentEnemy.startTurn(state);
        }

        @Override public void endTurn(int turnNum) {
            currentEnemy.endTurn(turnNum);
        }

        @Override public void heal(int hp) {
            currentEnemy.heal(hp);
        }

        @Override public void react(GameState state, Card card) {
            currentEnemy.react(state, card);
        }

        @Override public boolean applyDebuff(GameState state, DebuffType type, int n) {
            return currentEnemy.applyDebuff(state, type, n);
        }

        @Override public Enemy markAsBurningElite() {
            super.markAsBurningElite();
            for (int i = 0; i < possibleEnemies.size(); i++) {
                possibleEnemies.get(i).markAsBurningElite();
            }
            return this;
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            currentEnemy.randomize(random, training, difficulty);
        }

        @Override public int getMaxRandomizeDifficulty() {
            return currentEnemy.getMaxRandomizeDifficulty();
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return currentEnemy.getNNInputLen(prop);
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return currentEnemy.getNNInputDesc(prop);
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            return currentEnemy.writeNNInput(prop, input, idx);
        }

        @Override public String getMoveString(GameState state) {
            return currentEnemy.getMoveString(state);
        }

        @Override public int getHealth() {
            return currentEnemy.getHealth();
        }

        @Override public int getMaxHealthInBattle() {
            return currentEnemy.getMaxHealthInBattle();
        }

        @Override public int getBlock() {
            return currentEnemy.getBlock();
        }

        @Override public int getStrength() {
            return currentEnemy.getStrength();
        }

        @Override public int getLoseStrengthEot() {
            return currentEnemy.getLoseStrengthEot();
        }

        @Override public int getVulnerable() {
            return currentEnemy.getVulnerable();
        }

        @Override public int getWeak() {
            return currentEnemy.getWeak();
        }

        @Override public int getRegeneration() {
            return currentEnemy.getRegeneration();
        }

        @Override public int getMetallicize() {
            return currentEnemy.getMetallicize();
        }

        @Override public int getArtifact() {
            return currentEnemy.getArtifact();
        }

        @Override public boolean hasBurningHealthBuff() {
            return currentEnemy.hasBurningHealthBuff();
        }

        @Override public int getMove() {
            return currentEnemy.getMove();
        }

        @Override public int getLastMove() {
            return currentEnemy.getLastMove();
        }

        @Override public boolean isAlive() {
            return currentEnemy.isAlive();
        }

        @Override public boolean isTargetable() {
            return currentEnemy.isTargetable();
        }

        @Override public String toString(GameState state) {
            return currentEnemy.toString(state);
        }

        @Override public String toString() {
            return currentEnemy.toString();
        }

        @Override public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            MergedEnemy enemy = (MergedEnemy) o;
            return currentEnemyIdx == ((MergedEnemy) o).currentEnemyIdx && currentEnemy.equals(enemy.currentEnemy);
        }

        @Override public int hashCode() {
            return currentEnemy.hashCode();
        }
    }
}
