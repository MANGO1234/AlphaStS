package com.alphaStS.enemy;

import com.alphaStS.*;

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

    public void damage(int n, GameState state) {
        if (health <= 0) {
            return;
        }
        int dmg = n - block;
        if (state.prop.hasBoot && dmg > 0 && dmg < 5) {
            dmg = 5;
        }
        health -= Math.max(0, dmg);
        block = Math.max(0, block - n);
        if (dmg > 0 && platedArmor > 0) {
            platedArmor--;
        }
        if (health <= 0) {
            health = 0;
        }
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

    public void setRegeneration(int regen) {
        regeneration = regen;
    }

    public void setMetallicize(int n) {
        metallicize = n;
    }

    public void setBurningHealthBuff(boolean b) {
        if (property.hasBurningHealthBuff != b) {
            property = property.clone();
            property.hasBurningHealthBuff = b;
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
    }

    public void setMove(int move) {
        this.move = move;
    }

    public void startTurn(GameState state) {
        if (poison > 0) {
            state.playerDoNonAttackDamageToEnemy(this, poison, false);
            poison--;
        }
        block = 0;
    }

    public void endTurn(int turnNum) {
        if (turnNum > 1 && vulnerable > 0) {
            vulnerable -= 1;
        }
        if (turnNum > 1 && weak > 0) {
            weak -= 1;
        }
        if (loseStrengthEot != 0) {
            strength += loseStrengthEot;
            loseStrengthEot = 0;
        }
        if (metallicize > 0) {
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
        health += Math.min(hp, Math.max(0, property.origHealth - health));
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
            if (state.prop.hasChampionBelt) {
                this.weak += 1;
            }
        }
        case WEAK -> this.weak += n;
        case LOSE_STRENGTH -> this.gainStrength(-n);
        case LOSE_STRENGTH_EOT -> {
            this.loseStrengthEot += n;
            this.gainStrength(-n);
        }
        case POISON -> this.poison += n;
        case CORPSE_EXPLOSION -> this.corpseExplosion += n;
        }
        return true;
    }

    public void setBlock(int n) {
        block = n;
    }

    public Enemy markAsBurningElite() {
        property.applyBurningEliteBuff();
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
            super(0, possibleEnemies.stream().mapToInt((enemy) -> enemy.property.numOfMoves).reduce(0, Integer::sum), false);
            this.possibleEnemies = possibleEnemies;
            property.maxHealth = possibleEnemies.stream().mapToInt((enemy) -> enemy.property.maxHealth).reduce(0, Math::max);
            property.origHealth = property.maxHealth;
            property.origMaxHealth = property.maxHealth;
            // todo: on isELite and isMinion
            property.isElite = possibleEnemies.stream().anyMatch((e) -> e.property.isElite);
            property.isMinion = possibleEnemies.stream().anyMatch((e) -> e.property.isMinion);
            property.canVulnerable = possibleEnemies.stream().anyMatch((e) -> e.property.canVulnerable);
            property.canEntangle = possibleEnemies.stream().anyMatch((e) -> e.property.canEntangle);
            property.canWeaken = possibleEnemies.stream().anyMatch((e) -> e.property.canWeaken);
            property.canFrail = possibleEnemies.stream().anyMatch((e) -> e.property.canFrail);
            property.canSlime = possibleEnemies.stream().anyMatch((e) -> e.property.canSlime);
            property.canDaze = possibleEnemies.stream().anyMatch((e) -> e.property.canDaze);
            property.canGainStrength = possibleEnemies.stream().anyMatch((e) -> e.property.canGainStrength);
            property.canGainRegeneration = possibleEnemies.stream().anyMatch((e) -> e.property.canGainRegeneration);
            property.canHeal = possibleEnemies.stream().anyMatch((e) -> e.property.canHeal);
            property.canGainMetallicize = possibleEnemies.stream().anyMatch((e) -> e.property.canGainMetallicize);
            property.canGainPlatedArmor = possibleEnemies.stream().anyMatch((e) -> e.property.canGainPlatedArmor);
            property.canGainBlock = possibleEnemies.stream().anyMatch((e) -> e.property.canGainBlock);
            property.changePlayerStrength = possibleEnemies.stream().anyMatch((e) -> e.property.changePlayerStrength);
            property.changePlayerDexterity = possibleEnemies.stream().anyMatch((e) -> e.property.changePlayerDexterity);
            property.changePlayerFocus = possibleEnemies.stream().anyMatch((e) -> e.property.changePlayerFocus);
            property.hasBurningEliteBuff = possibleEnemies.stream().anyMatch((e) -> e.property.hasBurningEliteBuff);
            property.hasArtifact = possibleEnemies.stream().anyMatch((e) -> e.property.hasArtifact);
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

        public EnemyProperty getEnemyProperty(int idx) {
            return possibleEnemies.get(idx).property;
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

        @Override public List<Card> getPossibleGeneratedCards() {
            var cards = new ArrayList<Card>();
            for (var e : possibleEnemies) {
                cards.addAll(e.getPossibleGeneratedCards());
            }
            return cards;
        }

        // ******************* generated delegate methods follow ****************

        @Override public void damage(int n, GameState state) {
            currentEnemy.damage(n, state);
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
            return currentEnemy.markAsBurningElite();
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
            return enemy.currentEnemyIdx == ((MergedEnemy) o).currentEnemyIdx && currentEnemy.equals(enemy.currentEnemy);
        }

        @Override public int hashCode() {
            return currentEnemy.hashCode();
        }
    }

    // ******************************************************************************************
    // ******************************************************************************************
    // ******************************************************************************************

    public static class GremlinNob extends Enemy {
        public static int BELLOW = 0;
        public static int SKULL_BASH = 1;
        public static int RUSH_1 = 2;
        public static int RUSH_2 = 3;

        public GremlinNob() {
            this(90);
        }

        public GremlinNob(int health) {
            super(health, 4, false);
            property.isElite = true;
            property.actNumber = 1;
            property.canVulnerable = true;
            property.canGainStrength = true;
        }

        public GremlinNob(GremlinNob other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new GremlinNob(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SKULL_BASH) {
                state.enemyDoDamageToPlayer(this, 8, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
            } else if (move == RUSH_1 || move == RUSH_2) {
                state.enemyDoDamageToPlayer(this, 16, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == -1) {
                move = BELLOW;
            } else {
                move++;
                if (move == 4) {
                    move = SKULL_BASH;
                }
            }
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var idx = state.getEnemiesForRead().find(this);
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned) {
                    if (state.prop.cardDict[cardIdx].cardType == Card.SKILL) {
                        var e = state.getEnemiesForRead().get(idx);
                        if (e instanceof MergedEnemy m) {
                            if (m.currentEnemy instanceof GremlinNob && e.getMove() > 0) {
                                state.getEnemiesForWrite().getForWrite(idx).gainStrength(3);
                            }
                        } else if (e.getMove() > 0) {
                            state.getEnemiesForWrite().getForWrite(idx).gainStrength(3);
                        }
                    }
                }
            });
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BELLOW) {
                return "Buff";
            } else if (move == SKULL_BASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this,8);
            } else if (move == RUSH_1 || move == RUSH_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this,16);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(10, RandomGenCtx.Other) + 1;
            if (b < 10 && training) {
                health = (int) Math.round(((double) (health * b)) / 10);
            } else {
                health = 85 + random.nextInt(6, RandomGenCtx.Other);
            }
        }

        public String getName() {
            return "Gremlin Nob";
        }
    }

    public static class Lagavulin extends Enemy {
        static int WAIT_1 = 0;
        static int WAIT_2 = 1;
        static int WAIT_3 = 2;
        static int ATTACK_1 = 3;
        static int ATTACK_2 = 4;
        static int SIPHON_SOUL = 5;
        static int STUNNED = 6;

        public Lagavulin() {
            this(116);
        }

        public Lagavulin(int health) {
            super(health, 4, false);
            property.isElite = true;
            property.actNumber = 1;
            property.canGainBlock = true;
            property.changePlayerStrength = true;
            property.changePlayerDexterity = true;
        }

        public Lagavulin(Lagavulin other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Lagavulin(this);
        }

        @Override public void damage(int n, GameState state) {
            var dmg = Math.max(0, n - block);
            super.damage(n, state);
            if ((move == WAIT_1 || move == WAIT_2 || move == WAIT_3) && dmg > 0) {
                move = STUNNED;
            }
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            var dmg = blockable ? Math.max(0, n - block) : n;
            super.nonAttackDamage(n, blockable, state);
            if ((move == WAIT_1 || move == WAIT_2 || move == WAIT_3) && dmg > 0) {
                move = STUNNED;
            }
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            if (move <= WAIT_3) {
                if (metallicize == 4 && turnNum == 1) {
                    gainBlock(4);
                } else {
                    gainBlock(8);
                }
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == ATTACK_1 || move == ATTACK_2) {
                state.enemyDoDamageToPlayer(this, 20, 1);
            } else if (move == SIPHON_SOUL) {
                var player = state.getPlayerForWrite() ;
                player.applyDebuff(state, DebuffType.LOSE_DEXTERITY, 2);
                player.applyDebuff(state, DebuffType.LOSE_STRENGTH, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == STUNNED) {
                move = ATTACK_1;
                return;
            }
            if (move < 5) {
                move++;
            } else if (move == 5) {
                move = 3;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == WAIT_1 || move == WAIT_2 || move == WAIT_3) {
                return "Asleep";
            } else if (move == ATTACK_1 || move == ATTACK_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 20);
            } else if (move == SIPHON_SOUL) {
                return "-2 Strength+-2 Dexterity";
            } else if (move == STUNNED) {
                return "Stunned";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(10, RandomGenCtx.Other) + 1;
            if (training && b < 10) {
                health = (int) Math.round(((double) (health * b)) / 10);
            } else {
                health = 112 + random.nextInt(4, RandomGenCtx.Other);
            }
        }

        public String getName() {
            return "Lagavulin";
        }
    }

    public static class Sentry extends Enemy {
        public final static int BEAM = 0;
        public final static int BOLT = 1;

        int startMove;

        public Sentry(int startMove) {
            this(45, startMove);
        }

        public Sentry(int health, int startMove) {
            super(health, 2, false);
            property.isElite = true;
            property.actNumber = 1;
            property.canDaze = true;
            property.hasArtifact = true;
            this.startMove = startMove;
            artifact = 1;
        }

        public Sentry(Sentry other) {
            super(other);
            startMove = other.startMove;
        }

        @Override public Enemy copy() {
            return new Sentry(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BEAM) {
                state.enemyDoDamageToPlayer(this, 10, 1);
            } else if (move == BOLT) {
                state.addCardToDiscard(state.prop.dazedCardIdx);
                state.addCardToDiscard(state.prop.dazedCardIdx);
                state.addCardToDiscard(state.prop.dazedCardIdx);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == -1) {
                move = startMove;
                return;
            }
            move = move == BEAM ? BOLT : BEAM;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BEAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10);
            } else if (move == BOLT) {
                return "Shuffle 3 Dazes Into Deck";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(4, RandomGenCtx.Other) + 1;
            if (training && b < 4) {
                health = (int) Math.round(((double) (health * b)) / 4);
            } else {
                health = 39 + random.nextInt(7, RandomGenCtx.Other);
            }
        }

        public String getName() {
            return "Sentry";
        }
    }

    public static class Hexaghost extends Enemy {
        static int ACTIVATE = 0;
        static int DIVIDER = 1;
        static int SEAR_1 = 2;
        static int TACKLE_1 = 3;
        static int SEAR_2 = 4;
        static int INFLAME_1 = 5;
        static int TACKLE_2 = 6;
        static int SEAR_3 = 7;
        static int INFERNAL_1 = 8;

        boolean afterFirstInfernal;

        public Hexaghost() {
            this(264);
        }

        public Hexaghost(int health) {
            super(health, 9, false);
//            property.isBoss = true;
            property.canGainStrength = true;
            property.canGainBlock = true;
            afterFirstInfernal = false;
        }

        public Hexaghost(Hexaghost other) {
            super(other);
            afterFirstInfernal = other.afterFirstInfernal;
        }

        @Override public Enemy copy() {
            return new Hexaghost(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == DIVIDER) {
                int n = state.getPlayeForRead().getHealth() / 12 + 1;
                state.enemyDoDamageToPlayer(this, n, 6);
            } else if (move == SEAR_1 || move == SEAR_2 || move == SEAR_3) {
                state.enemyDoDamageToPlayer(this, 6, 1);
                state.addCardToDiscard(state.prop.burnCardIdx);
                state.addCardToDiscard(state.prop.burnCardIdx);
            } else if (move == TACKLE_1 || move == TACKLE_2) {
                state.enemyDoDamageToPlayer(this, 6, 2);
            } else if (move == INFLAME_1) {
                strength += 3;
                gainBlock(12);
            } else if (move == INFERNAL_1) {
                state.enemyDoDamageToPlayer(this, 3, 6);
                var deck = state.getDeck();
                state.setCardCountInDeck(state.prop.burnPCardIdx, deck[state.prop.burnPCardIdx] + deck[state.prop.burnCardIdx]);
                state.setCardCountInDeck(state.prop.burnCardIdx, 0);
                var hand = state.getHand();
                state.setCardCountInHand(state.prop.burnPCardIdx, hand[state.prop.burnPCardIdx] + hand[state.prop.burnCardIdx]);
                state.setCardCountInHand(state.prop.burnCardIdx, 0);
                var discard = state.getDiscard();
                state.setCardCountInDiscard(state.prop.burnPCardIdx, discard[state.prop.burnPCardIdx] + discard[state.prop.burnCardIdx]);
                state.setCardCountInDiscard(state.prop.burnCardIdx, 0);
                state.addCardToDiscard(state.prop.burnPCardIdx);
                state.addCardToDiscard(state.prop.burnPCardIdx);
                state.addCardToDiscard(state.prop.burnPCardIdx);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < 9) {
                move = move + 1;
            }
            if (move == 9) {
                afterFirstInfernal = true;
                move = SEAR_1;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == ACTIVATE) {
                return "Activate";
            } else if (move == DIVIDER) {
                int n = state.getPlayeForRead().getHealth() / 12 + 1;
                return "Attack " + state.enemyCalcDamageToPlayer(this, 3) + "x6";
            } else if (move == SEAR_1 || move == SEAR_2 || move == SEAR_3) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6);
            } else if (move == TACKLE_1 || move == TACKLE_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6) + "x2";
            } else if (move == INFLAME_1) {
                return "Gain 3 Strength+12 Block";
            } else if (move == INFERNAL_1) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 3) + "x6";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
                int b = random.nextInt(25, RandomGenCtx.Other) + 1;
                health = 264 * b / 25;
            }
        }

        public String getName() {
            return "Hexaghost";
        }

        public List<Card> getPossibleGeneratedCards() { return List.of(new Card.Burn(), new Card.BurnP()); }
    }

    public static class TheGuardian extends Enemy {
        static int CHARGING_UP = 0;
        static int FIERCE_BASH = 1;
        static int VENT_STEAM = 2;
        static int WHIRL_WIND = 3;
        static int DEFENSIVE_MODE = 4;
        static int ROLL_ATTACK = 5;
        static int TWIN_SLAM = 6;

        int modeShiftDmg;
        int maxModeShiftDmg;

        public int getModeShiftDmg() {
            return modeShiftDmg;
        }

        public int getMaxModeShiftDmg() {
            return maxModeShiftDmg;
        }

        public TheGuardian() {
            this(250);
        }

        public TheGuardian(int health) {
            super(health, 7, false);
//            property.isBoss = true;
            property.canGainBlock = true;
            property.canVulnerable = true;
            property.canWeaken = true;
            modeShiftDmg = 40;
            maxModeShiftDmg = 40;
        }

        public TheGuardian(TheGuardian other) {
            super(other);
            modeShiftDmg = other.modeShiftDmg;
            maxModeShiftDmg = other.maxModeShiftDmg;
        }

        @Override public Enemy copy() {
            return new TheGuardian(this);
        }

        @Override public void damage(int n, GameState state) {
            int oldHealth = health;
            super.damage(n, state);
            if (move != DEFENSIVE_MODE && move != ROLL_ATTACK && move != TWIN_SLAM) {
                modeShiftDmg = Math.max(modeShiftDmg - (oldHealth - health), 0);
                if (modeShiftDmg == 0) {
                    move = DEFENSIVE_MODE;
                    gainBlock(20);
                }
            }
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int oldHealth = health;
            super.nonAttackDamage(n, blockable, state);
            if (move != DEFENSIVE_MODE && move != ROLL_ATTACK && move != TWIN_SLAM) {
                modeShiftDmg = Math.max(modeShiftDmg - (oldHealth - health), 0);
                if (modeShiftDmg == 0) {
                    move = DEFENSIVE_MODE;
                    gainBlock(20);
                }
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CHARGING_UP) {
                gainBlock(9);
            } else if (move == FIERCE_BASH) {
                state.enemyDoDamageToPlayer(this, 36, 1);
            } else if (move == VENT_STEAM) {
                var player = state.getPlayerForWrite();
                player.applyDebuff(state, DebuffType.VULNERABLE, 2);
                player.applyDebuff(state, DebuffType.WEAK, 2);
            } else if (move == WHIRL_WIND) {
                state.enemyDoDamageToPlayer(this, 5, 4);
            } else if (move == DEFENSIVE_MODE) {
            } else if (move == ROLL_ATTACK) {
                state.enemyDoDamageToPlayer(this, 10, 1);
            } else if (move == TWIN_SLAM) {
                state.enemyDoDamageToPlayer(this, 8, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < WHIRL_WIND) {
                move++;
            } else if (move == WHIRL_WIND) {
                move = CHARGING_UP;
            } else if (move == DEFENSIVE_MODE) {
                move = ROLL_ATTACK;
            } else if (move == ROLL_ATTACK) {
                move = TWIN_SLAM;
            } else if (move == TWIN_SLAM) {
                move = WHIRL_WIND;
                maxModeShiftDmg += 10;
                modeShiftDmg = maxModeShiftDmg;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CHARGING_UP) {
                return "Charging Up";
            } else if (move == FIERCE_BASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 36);
            } else if (move == VENT_STEAM) {
                return "2 Vulnerable+2 Weak";
            } else if (move == WHIRL_WIND) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 5) + "x4";
            } else if (move == DEFENSIVE_MODE) {
                return "Defensive Mode";
            } else if (move == ROLL_ATTACK) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10);
            } else if (move == TWIN_SLAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "x2";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = training ? random.nextInt(25, RandomGenCtx.Other) + 1 : 25;
            health = 250 * b / 25;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var idx = state.getEnemiesForRead().find(this);
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned) {
                    if (state.prop.cardDict[cardIdx].cardType == Card.ATTACK) {
                        var move = state.getEnemiesForRead().get(idx).move;
                        if (move == ROLL_ATTACK || move == TWIN_SLAM) {
                            state.doNonAttackDamageToPlayer(4, true, this);
                        }
                    }
                }
            });
        }

        public String getName() {
            return "The Guardian";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return s.subSequence(0, s.length() - 1) + ", modeShift=" + modeShiftDmg + "/" + maxModeShiftDmg + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && maxModeShiftDmg == ((TheGuardian) o).maxModeShiftDmg && modeShiftDmg == ((TheGuardian) o).modeShiftDmg;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 2;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "2 inputs to keep track of current and max guardian mode shift damage";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx++] = (modeShiftDmg - 50) / 20f;
            input[idx] = (maxModeShiftDmg - 50) / 20f;
            return 2;
        }
    }

    public static class SlimeBoss extends Enemy {
        static int GOOP_SPRAY = 0;
        static int PREPARING = 1;
        static int SLAM = 2;
        static int SPLIT = 3;

        private int splitHealth;

        public SlimeBoss() {
            this(150);
        }

        public SlimeBoss(int health) {
            super(health, 4, false);
//            property.isBoss = true;
            property.canSlime = true;
        }

        public SlimeBoss(SlimeBoss other) {
            super(other);
            splitHealth = other.splitHealth;
        }

        @Override public Enemy copy() {
            return new SlimeBoss(this);
        }

        @Override public void damage(int n, GameState state) {
            super.damage(n, state);
            if (health <= property.maxHealth / 2) {
                move = SPLIT;
            }
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (health <= property.maxHealth / 2) {
                move = SPLIT;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == GOOP_SPRAY) {
                for (int i = 0; i < 5; i++) {
                    state.addCardToDiscard(state.prop.slimeCardIdx);
                }
            } else if (move == SLAM) {
                state.enemyDoDamageToPlayer(this, 38, 1);
            } else if (move == SPLIT) {
                var enemies = state.getEnemiesForWrite();
                for (int i = 0; i < enemies.size(); i++) {
                    if (enemies.get(i) instanceof Enemy.LargeAcidSlime) {
                        state.reviveEnemy(i, false, health);
                        ((Enemy.LargeAcidSlime) enemies.get(i)).splitMaxHealth = health;
                    } else if (enemies.get(i) instanceof Enemy.LargeSpikeSlime) {
                        state.reviveEnemy(i, false, health);
                        ((Enemy.LargeSpikeSlime) enemies.get(i)).splitMaxHealth = health;
                    }
                }
                splitHealth = health;
                health = 0;
                state.adjustEnemiesAlive(-1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == GOOP_SPRAY) {
                move = PREPARING;
            } else if (move == PREPARING) {
                move = SLAM;
            } else if (move < 0 || move == SLAM){
                move = GOOP_SPRAY;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == GOOP_SPRAY) {
                return "Slimed 5";
            } else if (move == PREPARING) {
                return "Preparing";
            } else if (move == SLAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 38);
            } else if (move == SPLIT) {
                return "Split";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(15, RandomGenCtx.Other) + 1;
            health = 10 * (training ? b : 15);
            if (health <= property.maxHealth / 2) {
                move = SPLIT;
            }
        }

        public String getName() {
            return "Slime Boss";
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of health slime boss split at";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = splitHealth / (float) 75.0;
            return 1;
        }

    }

    public static class LargeSpikeSlime extends Enemy {
        private static final int FLAME_TACKLE = 0;
        private static final int LICK = 1;
        private static final int SPLIT = 2;

        private int splitMaxHealth;

        public void setSplitMaxHealth(int splitMaxHealth) {
            this.splitMaxHealth = splitMaxHealth;
        }

        public LargeSpikeSlime() {
            this(73);
        }

        public LargeSpikeSlime(int health) {
            super(health, 3, true);
            property.canSlime = true;
            property.canFrail = true;
            splitMaxHealth = property.maxHealth;
        }

        public LargeSpikeSlime(LargeSpikeSlime other) {
            super(other);
            splitMaxHealth = other.splitMaxHealth;
        }

        public LargeSpikeSlime(int health, boolean startDead) {
            this(health);
            if (startDead) this.health = 0;
        }

        @Override public Enemy copy() {
            return new LargeSpikeSlime(this);
        }

        @Override public void damage(int n, GameState state) {
            super.damage(n, state);
            if (health <= splitMaxHealth / 2) {
                move = SPLIT;
            }
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (health <= splitMaxHealth / 2) {
                move = SPLIT;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == FLAME_TACKLE) {
                state.enemyDoDamageToPlayer(this, 18, 1);
                state.addCardToDiscard(state.prop.slimeCardIdx);
                state.addCardToDiscard(state.prop.slimeCardIdx);
            } else if (move == LICK) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 3);
            } else if (move == SPLIT) {
                var enemies = state.getEnemiesForWrite();
                for (int i = 0; i < enemies.size(); i++) {
                    if (enemies.get(i) instanceof Enemy.MediumSpikeSlime) {
                        state.reviveEnemy(i, false, health);
                    }
                }
                health = 0;
                state.adjustEnemiesAlive(-1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == SPLIT) {
                return;
            }
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
            int newMove;
            if (r < 30) {
                if (move == FLAME_TACKLE && lastMove == FLAME_TACKLE) {
                    newMove = LICK;
                } else {
                    newMove = FLAME_TACKLE;
                }
            } else if (move == LICK) {
                newMove = FLAME_TACKLE;
            } else {
                newMove = LICK;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == FLAME_TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 18) + "+Slimed 2";
            } else if (move == LICK) {
                return "Frail 3";
            } else if (move == SPLIT) {
                return "Split";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 67 + random.nextInt(7, RandomGenCtx.Other);
            splitMaxHealth = health;
        }

        public String getName() {
            return "Spike Slime (L)";
        }
    }

    public static class MediumSpikeSlime extends Enemy {
        private static final int FLAME_TACKLE = 0;
        private static final int LICK = 1;

        public MediumSpikeSlime() {
            this(34);
        }

        public MediumSpikeSlime(int health) {
            super(health, 2, true);
            property.canSlime = true;
            property.canFrail = true;
        }

        public MediumSpikeSlime(int health, boolean startDead) {
            this(health);
            if (startDead) this.health = 0;
        }

        public MediumSpikeSlime(MediumSpikeSlime other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new MediumSpikeSlime(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == FLAME_TACKLE) {
                state.enemyDoDamageToPlayer(this, 10, 1);
                state.addCardToDiscard(state.prop.slimeCardIdx);
            } else if (move == LICK) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
            int newMove;
            if (r < 30) {
                if (move == FLAME_TACKLE && lastMove == FLAME_TACKLE) {
                    newMove = LICK;
                } else {
                    newMove = FLAME_TACKLE;
                }
            } else if (move == LICK) {
                newMove = FLAME_TACKLE;
            } else {
                newMove = LICK;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == FLAME_TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10) + "+Slimed 1";
            } else if (move == LICK) {
                return "Frail 1";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 29 + random.nextInt(6, RandomGenCtx.Other);
        }

        public String getName() {
            return "Spike Slime (M)";
        }
    }

    public static class SmallSpikeSlime extends Enemy {
        private static final int TACKLE = 0;

        public SmallSpikeSlime() {
            this(15);
        }

        public SmallSpikeSlime(int health) {
            super(health, 1, false);
        }

        public SmallSpikeSlime(SmallSpikeSlime other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new SmallSpikeSlime(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            state.enemyDoDamageToPlayer(this, 6, 1);
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = TACKLE;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 11 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Spike Slime (S)";
        }
    }


    public static class LargeAcidSlime extends Enemy {
        private static final int CORROSIVE_SPIT = 0;
        private static final int TACKLE = 1;
        private static final int LICK = 2;
        private static final int SPLIT = 3;

        private int splitMaxHealth;

        public LargeAcidSlime() {
            this(72);
        }

        public LargeAcidSlime(int health) {
            super(health, 3, true);
            property.canSlime = true;
            property.canWeaken = true;
            splitMaxHealth = property.maxHealth;
        }

        public LargeAcidSlime(int health, boolean startDead) {
            this(health);
            if (startDead) this.health = 0;
        }

        public LargeAcidSlime(LargeAcidSlime other) {
            super(other);
            splitMaxHealth = other.splitMaxHealth;
        }

        @Override public Enemy copy() {
            return new LargeAcidSlime(this);
        }

        @Override public void damage(int n, GameState state) {
            super.damage(n, state);
            if (health <= splitMaxHealth / 2) {
                move = SPLIT;
            }
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (health <= splitMaxHealth / 2) {
                move = SPLIT;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CORROSIVE_SPIT) {
                state.enemyDoDamageToPlayer(this, 12, 1);
                state.addCardToDiscard(state.prop.slimeCardIdx);
                state.addCardToDiscard(state.prop.slimeCardIdx);
            } else if (move == TACKLE) {
                state.enemyDoDamageToPlayer(this, 18, 1);
            } else if (move == LICK) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2);
            } else if (move == SPLIT) {
                var enemies = state.getEnemiesForWrite();
                for (int i = 0; i < enemies.size(); i++) {
                    if (enemies.get(i) instanceof Enemy.MediumAcidSlime) {
                        state.reviveEnemy(i, false, health);
                    }
                }
                health = 0;
                state.adjustEnemiesAlive(-1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == SPLIT) {
                return;
            }
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null);
            int newMove;
            if (r < 40) {
                if (move == CORROSIVE_SPIT && lastMove == CORROSIVE_SPIT) {
                    newMove = random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.6 ? TACKLE : LICK;
                } else {
                    newMove = CORROSIVE_SPIT;
                }
            } else if (r < 70) {
                if (move == TACKLE && lastMove == TACKLE) {
                    newMove = random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.6 ? CORROSIVE_SPIT : LICK;
                } else {
                    newMove = TACKLE;
                }
            } else if (move == LICK) {
                newMove = random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.4 ? CORROSIVE_SPIT : TACKLE;
            } else {
                newMove = LICK;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CORROSIVE_SPIT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12) + "+Slimed 2";
            } else if (move == TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 18);
            } else if (move == LICK) {
                return "Weak 2";
            } else if (move == SPLIT) {
                return "Split";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 68 + random.nextInt(5, RandomGenCtx.Other);
            splitMaxHealth = health;
        }

        public String getName() {
            return "Acid Slime (L)";
        }
    }

    public static class MediumAcidSlime extends Enemy {
        private static final int CORROSIVE_SPIT = 0;
        private static final int TACKLE = 1;
        private static final int LICK = 2;

        public MediumAcidSlime() {
            this(34);
        }

        public MediumAcidSlime(int health) {
            super(health, 3, true);
            property.canSlime = true;
            property.canWeaken = true;
        }

        public MediumAcidSlime(int health, boolean startDead) {
            this(health);
            if (startDead) this.health = 0;
        }


        public MediumAcidSlime(MediumAcidSlime other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new MediumAcidSlime(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CORROSIVE_SPIT) {
                state.enemyDoDamageToPlayer(this, 8, 1);
                state.addCardToDiscard(state.prop.slimeCardIdx);
            } else if (move == TACKLE) {
                state.enemyDoDamageToPlayer(this, 12, 1);
            } else if (move == LICK) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null);
            int newMove;
            if (r < 40) {
                if (move == CORROSIVE_SPIT && lastMove == CORROSIVE_SPIT) {
                    newMove = random.nextBoolean(RandomGenCtx.EnemyChooseMove) ? TACKLE : LICK;
                } else {
                    newMove = CORROSIVE_SPIT;
                }
            } else if (r < 80) {
                if (move == TACKLE && lastMove == TACKLE) {
                    newMove = random.nextBoolean(RandomGenCtx.EnemyChooseMove) ? CORROSIVE_SPIT : LICK;
                } else {
                    newMove = TACKLE;
                }
            } else if (move == LICK) {
                newMove = random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.4 ? CORROSIVE_SPIT : TACKLE;
            } else {
                newMove = LICK;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CORROSIVE_SPIT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "+Slimed 1";
            } else if (move == TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12);
            } else if (move == LICK) {
                return "Weak 1";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 29 + random.nextInt(6, RandomGenCtx.Other);
        }

        public String getName() {
            return "Acid Slime (M)";
        }
    }

    public static class SmallAcidSlime extends Enemy {
        private static final int TACKLE = 0;
        private static final int LICK = 1;

        public SmallAcidSlime() {
            this(13);
        }

        public SmallAcidSlime(int health) {
            super(health, 2, false);
            property.canWeaken = true;
        }

        public SmallAcidSlime(SmallAcidSlime other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new SmallAcidSlime(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == TACKLE) {
                state.enemyDoDamageToPlayer(this, 4, 1);
            } else if (move == LICK) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == -1 || move == TACKLE) {
                move = LICK;
            } else {
                move = TACKLE;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 4);
            } else if (move == LICK) {
                return "Weak 1";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 9 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Acid Slime (S)";
        }
    }

    public static class RedSlaver extends Enemy {
        public static final int STAB = 0;
        public static final int SCRAPE = 1;
        public static final int ENTANGLE = 2;

        private boolean usedEntangle;

        public RedSlaver() {
            this(52);
        }

        public RedSlaver(int health) {
            super(health, 3, true);
            property.canVulnerable = true;
            property.canEntangle = true;
        }

        public RedSlaver(RedSlaver other) {
            super(other);
            usedEntangle = other.usedEntangle;
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && usedEntangle == ((RedSlaver) o).usedEntangle;
        }

        @Override public Enemy copy() {
            return new RedSlaver(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == STAB) {
                state.enemyDoDamageToPlayer(this, 14, 1);
            } else if (move == SCRAPE) {
                state.enemyDoDamageToPlayer(this, 9, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
            } else if (move == ENTANGLE) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.ENTANGLED, 1);
                usedEntangle = true;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == -1) {
                move = STAB;
                return;
            }
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null);
            int newMove;
            if (r >= 75 && !usedEntangle) {
                newMove = ENTANGLE;
            } else if (r >= 55 && usedEntangle && (move != STAB || lastMove != STAB)) {
                newMove = STAB;
            } else if (move != SCRAPE) {
                newMove = SCRAPE;
            } else {
                newMove = STAB;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == STAB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14);
            } else if (move == SCRAPE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "+Vulnerable 2";
            } else if (move == ENTANGLE) {
                return "Entangle";
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(3, RandomGenCtx.Other) + 1;
            if (training && b < 3) {
                health = (int) Math.round(((double) (health * b)) / 3);
            } else {
                health = 48 + random.nextInt(5, RandomGenCtx.Other);
            }
        }

        @Override public String getName() {
            return "Red Slaver";
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 inputs to keep track of whether red slaver have used entangle";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = usedEntangle ? -0.5f : 0.5f;
            return 1;
        }
    }

    public static class BlueSlaver extends Enemy {
        public static final int STAB = 0;
        public static final int RAKE = 1;

        public BlueSlaver() {
            this(52);
        }

        public BlueSlaver(int health) {
            super(health, 2, true);
            property.canWeaken = true;
        }

        public BlueSlaver(BlueSlaver other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new BlueSlaver(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == STAB) {
                state.enemyDoDamageToPlayer(this, 13, 1);
            } else if (move == RAKE) {
                state.enemyDoDamageToPlayer(this, 8, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null);
            int newMove;
            if (r >= 40 && (move != STAB || lastMove != STAB)) {
                newMove = STAB;
            } else if (move != RAKE) {
                newMove = RAKE;
            } else {
                newMove = STAB;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == STAB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 13);
            } else if (move == RAKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "+Weak 2";
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(3, RandomGenCtx.Other) + 1;
            if (training && b < 3) {
                health = (int) Math.round(((double) (health * b)) / 3);
            } else {
                health = 48 + random.nextInt(5, RandomGenCtx.Other);
            }
        }

        @Override public String getName() {
            return "Blue Slaver";
        }
    }

    public static class JawWorm extends Enemy {
        public JawWorm() {
            this(46);
        }

        public JawWorm(int health) {
            super(health, 3, true);
            property.canGainStrength = true;
            property.canGainBlock = true;
        }

        public JawWorm(JawWorm other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new JawWorm(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == 0) {
                state.enemyDoDamageToPlayer(this, 12, 1);
            } else if (move == 1) {
                strength += 5;
                gainBlock(9);
            } else if (move == 2) {
                state.enemyDoDamageToPlayer(this, 7, 1);
                gainBlock(5);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == -1) {
                move = 0;
                return;
            }
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null);
            int newMove;
            if (r < 25) {
                if (move == 0) {
                    newMove = random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.5625 ? 1 : 2;
                } else {
                    newMove = 0;
                }
            } else if (r < 55) {
                if (move == 2 && lastMove == 2) {
                    newMove = random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.357 ? 0 : 1;
                } else {
                    newMove = 2;
                }
            } else if (move == 1) {
                newMove = random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.416 ? 0 : 2;
            } else {
                newMove = 1;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == 0) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12);
            } else if (move == 1) {
                return "Gain 5 Strength+Block 9";
            } else if (move == 2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "+Block 5";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 42 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Jaw Worm";
        }
    }

    public static class Cultist extends Enemy {
        public static final int INCANTATION = 0;
        public static final int DARK_STRIKE = 1;

        public Cultist() {
            this(56);
        }

        public Cultist(int health) {
            super(health, 2, false);
            property.canGainStrength = true;
        }

        public Cultist(Cultist other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Cultist(this);
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            if (move == DARK_STRIKE) {
                gainStrength(5);
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == INCANTATION) {
            } else if (move == DARK_STRIKE) {
                state.enemyDoDamageToPlayer(this, 6, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < DARK_STRIKE) {
                move++;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == INCANTATION) {
                return "Ritual 5";
            } else if (move == DARK_STRIKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
                health = (int) Math.round(((double) (health * difficulty)) / 5);
            } else {
                health = 50 + random.nextInt(7, RandomGenCtx.Other);
            }
        }

        @Override public int getMaxRandomizeDifficulty() {
            return 5;
        }

        public String getName() {
            return "Cultist";
        }
    }

    public static class RedLouse extends Enemy {
        static final int BITE = 0;
        static final int GROW = 1;

        int d = -1;
        int curlUpAmount = 12;
        boolean hasCurledUp = false;
        boolean tookAttackDamage = false;

        public boolean hasCurledUp() {
            return hasCurledUp;
        }

        public int getD() {
            return d;
        }

        public int getCurlUpAmount() {
            return curlUpAmount;
        }

        public void setD(int d) {
            this.d = d;
        }

        public void setCurlUpAmount(int curlUpAmount) {
            this.curlUpAmount = curlUpAmount;
        }

        public RedLouse() {
            this(16);
        }

        public RedLouse(int health) {
            super(health, 2, true);
            property.canGainStrength = true;
            property.canGainBlock = true;
        }

        public RedLouse(RedLouse other) {
            super(other);
            d = other.d;
            curlUpAmount = other.curlUpAmount;
            hasCurledUp = other.hasCurledUp;
            tookAttackDamage = other.tookAttackDamage;
        }

        @Override public Enemy copy() {
            return new RedLouse(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BITE) {
                state.enemyDoDamageToPlayer(this, d, 1);
            } else if (move == GROW) {
                strength += 4;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null);
            int newMove;
            if (r < 25) {
                newMove = move == GROW ? BITE : GROW;
            } else if (move == BITE && lastMove == BITE) {
                newMove = GROW;
            } else {
                newMove = BITE;
            }
            lastMove = move;
            move = newMove;
            if (d < 0 && move == BITE) {
                d = 6 + random.nextInt(3, RandomGenCtx.Other);
            }
        }

        @Override public void damage(int n, GameState state) {
            tookAttackDamage = true;
            super.damage(n, state);
        }

        @Override public void react(GameState state, Card card) {
            if (tookAttackDamage && !hasCurledUp) {
                hasCurledUp = true;
                gainBlock(curlUpAmount);
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BITE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, d);
            } else if (move == GROW) {
                return "Gain 4 Strength";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 11 + random.nextInt(6, RandomGenCtx.Other);
            curlUpAmount = 9 + random.nextInt(4, RandomGenCtx.Other);
        }

        public String getName() {
            return "Red Louse";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            if (d < 0) {
                return s;
            }
            return s.subSequence(0, s.length() - 1) + ", biteDmg=" + d + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && d == ((Enemy.RedLouse) o).d;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Red Louse Bite damage";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = d < 0 ? -0.5f : d / 8.0f;
            return 1;
        }
    }

    public static class GreenLouse extends Enemy {
        static final int BITE = 0;
        static final int SPIT_WEB = 1;

        int d = -1;
        int curlUpAmount = 12;
        boolean hasCurledUp = false;
        boolean tookAttackDamage = false;

        public boolean hasCurledUp() {
            return hasCurledUp;
        }

        public int getD() {
            return d;
        }

        public int getCurlUpAmount() {
            return curlUpAmount;
        }

        public void setD(int d) {
            this.d = d;
        }

        public void setCurlUpAmount(int curlUpAmount) {
            this.curlUpAmount = curlUpAmount;
        }

        public GreenLouse() {
            this(18);
        }

        public GreenLouse(int health) {
            super(health, 2, true);
            property.canWeaken = true;
        }

        public GreenLouse(GreenLouse other) {
            super(other);
            d = other.d;
            curlUpAmount = other.curlUpAmount;
            hasCurledUp = other.hasCurledUp;
            tookAttackDamage = other.tookAttackDamage;
        }

        @Override public Enemy copy() {
            return new GreenLouse(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BITE) {
                state.enemyDoDamageToPlayer(this, d, 1);
            } else if (move == SPIT_WEB) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null);
            int newMove;
            if (r < 25) {
                newMove = move == SPIT_WEB ? BITE : SPIT_WEB;
            } else if (move == BITE && lastMove == BITE) {
                newMove = SPIT_WEB;
            } else {
                newMove = BITE;
            }
            lastMove = move;
            move = newMove;
            if (d < 0 && move == BITE) {
                d = 6 + random.nextInt(3, RandomGenCtx.Other);
            }
        }

        @Override public void damage(int n, GameState state) {
            tookAttackDamage = true;
            super.damage(n, state);
        }

        @Override public void react(GameState state, Card card) {
            if (tookAttackDamage && !hasCurledUp) {
                hasCurledUp = true;
                gainBlock(curlUpAmount);
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BITE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, d);
            } else if (move == SPIT_WEB) {
                return "Applies 2 Week";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 12 + random.nextInt(7, RandomGenCtx.Other);
            curlUpAmount = 9 + random.nextInt(4, RandomGenCtx.Other);
        }

        public String getName() {
            return "Green Louse";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            if (d < 0) {
                return s;
            }
            return s.subSequence(0, s.length() - 1) + ", biteDmg=" + d + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && d == ((Enemy.GreenLouse) o).d;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Green Louse Bite damage";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = d < 0 ? -0.5f : d / 8.0f;
            return 1;
        }
    }

    public static class FungiBeast extends Enemy {
        static final int BITE = 0;
        static final int GROW = 1;

        boolean isDead;

        public FungiBeast() {
            this(28);
        }

        public FungiBeast(int health) {
            super(health, 2, true);
            property.canVulnerable = true;
            property.canGainStrength = true;
        }

        public FungiBeast(FungiBeast other) {
            super(other);
            isDead = other.isDead;
        }

        @Override public Enemy copy() {
            return new FungiBeast(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BITE) {
                state.enemyDoDamageToPlayer(this, 6, 1);
            } else if (move == GROW) {
                strength += 5;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null);
            int newMove;
            if (r < 60) {
                if (move == 1 && lastMove == 1) {
                    newMove = GROW;
                } else {
                    newMove = BITE;
                }
            } else if (move == GROW) {
                newMove = BITE;
            } else {
                newMove = GROW;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public void damage(int n, GameState state) {
            super.damage(n, state);
            if (!isDead && health <= 0) {
                isDead = true;
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
            }
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (!isDead && health <= 0) {
                isDead = true;
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BITE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6);
            } else if (move == GROW) {
                return "Gain 5 Strength";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 24 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Fungi Beast";
        }
    }

    public static class Looter extends Enemy {
        static final int MUG_1 = 0;
        static final int MUG_2 = 1;
        static final int LUNGE = 2;
        static final int SMOKE_BOMB = 3;
        static final int ESCAPE = 4;

        public Looter() {
            this(50);
        }

        public Looter(int health) {
            super(health, 5, false);
            property.canGainBlock = true;
        }

        public Looter(Looter other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Looter(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == MUG_1 || move == MUG_2) {
                state.enemyDoDamageToPlayer(this, 11, 1);
            } else if (move == LUNGE) {
                state.enemyDoDamageToPlayer(this, 14, 1);
            } else if (move == SMOKE_BOMB) {
                state.enemyDoDamageToPlayer(this, 6, 1);
            } else if (move == ESCAPE) { // simulate the pain of losing gold, todo: need to combine with mugger later, need to change due to onDamage effects
                state.doNonAttackDamageToPlayer(Math.min(30, state.getPlayeForRead().getHealth() - 1), false, this);
                health = 0;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move < 0) {
                newMove = MUG_1;
            } else if (move == MUG_1) {
                newMove = MUG_2;
            } else if (move == MUG_2) {
                state.setIsStochastic();
                newMove = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null) < 50 ? LUNGE : SMOKE_BOMB;
            } else if (move == LUNGE) {
                newMove = SMOKE_BOMB;
            } else {
                newMove = ESCAPE;
            }
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == MUG_1 || move == MUG_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11);
            } else if (move == LUNGE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14);
            } else if (move == SMOKE_BOMB) {
                return "Block 6";
            } else if (move == ESCAPE) {
                return "Escape";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 46 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Looter";
        }
    }

    public static class FatGremlin extends Enemy {
        static final int SMASH = 0;

        public FatGremlin() {
            this(18);
        }

        public FatGremlin(int health) {
            super(health, 1, false);
            property.canWeaken = true;
            property.canFrail = true;
        }

        public FatGremlin(FatGremlin other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new FatGremlin(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SMASH) {
                state.enemyDoDamageToPlayer(this, 5, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = SMASH;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SMASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 5) + "+Frail 1+Weak 1";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 14 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Fat Gremlin";
        }
    }

    public static class MadGremlin extends Enemy {
        static final int SCRATCH = 0;

        public MadGremlin() {
            this(25);
        }

        public MadGremlin(int health) {
            super(health, 1, false);
            property.canGainStrength = true;
        }

        public MadGremlin(MadGremlin other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new MadGremlin(this);
        }

        @Override public void damage(int n, GameState state) {
            var dmg = Math.max(0, n - block);
            super.damage(n, state);
            if (dmg > 0) {
                gainStrength(2);
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SCRATCH) {
                state.enemyDoDamageToPlayer(this, 5, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = SCRATCH;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SCRATCH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 5);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 21 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Mad Gremlin";
        }
    }

    public static class SneakyGremlin extends Enemy {
        static final int PUNCTURE = 0;

        public SneakyGremlin() {
            this(15);
        }

        public SneakyGremlin(int health) {
            super(health, 1, false);
        }

        public SneakyGremlin(SneakyGremlin other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new SneakyGremlin(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == PUNCTURE) {
                state.enemyDoDamageToPlayer(this, 10, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = PUNCTURE;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == PUNCTURE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 11 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Sneaky Gremlin";
        }
    }

    public static class ShieldGremlin extends Enemy {
        static final int PROTECT = 0;
        static final int SHIELD_BASH = 1;

        public ShieldGremlin() {
            this(17);
        }

        public ShieldGremlin(int health) {
            super(health, 2, false);
        }

        public ShieldGremlin(ShieldGremlin other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new ShieldGremlin(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == PROTECT) {
                if (state.enemiesAlive == 1) { // can only be shield gremlin
                    gainBlock(11);
                } else {
                    int r = 0;
                    if (state.enemiesAlive > 2) {
                        state.setIsStochastic();
                        r = state.getSearchRandomGen().nextInt(state.enemiesAlive - 1, RandomGenCtx.ShieldGremlin);
                    }
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        if (enemy != self) {
                            if ((--r) < 0) {
                                enemy.gainBlock(11);
                                break;
                            }
                        }
                    }
                }
            } else if (move == SHIELD_BASH) {
                state.enemyDoDamageToPlayer(this, 8, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (state.enemiesAlive > 1) {
                move = PROTECT;
            } else {
                move = SHIELD_BASH;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == PROTECT) {
                return "Gives 11 block to a random ally";
            } else if (move == SHIELD_BASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 13 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Shield Gremlin";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var enemies = state.getEnemiesForWrite();
            for (int i = 0; i < enemies.size(); i++) {
                if (enemies.get(i).getName().contains("Gremlin")) {
                    enemies.get(i).property.canGainBlock = true;
                }
            }
        }
    }

    public static class GremlinWizard extends Enemy {
        static final int CHARGING_1 = 0;
        static final int CHARGING_2 = 1;
        static final int ULTIMATE_BLAST = 2;

        public GremlinWizard() {
            this(26);
        }

        public GremlinWizard(int health) {
            super(health, 3, false);
        }

        public GremlinWizard(GremlinWizard other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new GremlinWizard(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == ULTIMATE_BLAST) {
                state.enemyDoDamageToPlayer(this, 30, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < ULTIMATE_BLAST) {
                move++;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CHARGING_1 || move == CHARGING_2) {
                return "Charging";
            } else if (move == ULTIMATE_BLAST) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 30);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 22 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Gremlin Wizard";
        }
    }

}
