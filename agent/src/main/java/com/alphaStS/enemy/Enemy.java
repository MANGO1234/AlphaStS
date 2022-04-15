package com.alphaStS.enemy;

import com.alphaStS.*;

public abstract class Enemy extends EnemyReadOnly {
    public Enemy(int health, int numOfMoves) {
        super(health, numOfMoves);
    }

    public abstract void nextMove(RandomGen random);

    public void damage(int n, GameState state) {
        if (health <= 0) {
            return;
        }
        health -= Math.max(0, n - block);
        block = Math.max(0, block - n);
        if (health <= 0) {
            health = 0;
            state.enemiesAlive -= 1;
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
            state.enemiesAlive -= 1;
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

    public void setMove(int move) {
        this.move = move;
    }

    public void startTurn() {
        block = 0;
    }

    public void endTurn() {
        if (vulnerable > 0) {
            vulnerable -= 1;
        }
        if (weak > 0) {
            weak -= 1;
        }
    }

    public void react(GameState state, Card card) {
    }

    public void applyDebuff(DebuffType type, int amount) {
        if (health <= 0) {
            return;
        }
        if (artifact > 0) {
            artifact--;
            return;
        }
        switch (type) {
        case VULNERABLE -> this.vulnerable += amount;
        case WEAK -> this.weak += amount;
        }
    }

    public void randomize(RandomGen random, boolean training) {
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
            super(health, 4);
            isElite = true;
            canVulnerable = true;
            canGainStrength = true;
        }

        public GremlinNob(GremlinNob other) {
            this(other.health);
            setSharedFields(other);
        }

        @Override public Enemy copy() {
            return new GremlinNob(this);
        }

        @Override public void doMove(GameState state) {
            if (move == SKULL_BASH) {
                state.enemyDoDamageToPlayer(this, 8, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 3);
            } else if (move == RUSH_1 || move == RUSH_2) {
                state.enemyDoDamageToPlayer(this, 16, 1);
            }
        }

        @Override public void nextMove(RandomGen random) {
            if (move == -1) {
                move = BELLOW;
            } else {
                move++;
                if (move == 4) {
                    move = SKULL_BASH;
                }
            }
        }

        @Override public void startOfGameSetup(GameState state) {
            var idx = state.getEnemiesForRead().find(this);
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card) {
                    if (card.cardType == Card.SKILL && state.getEnemiesForRead().get(idx).getMove() > 0) {
                        state.getEnemiesForWrite().getForWrite(idx).gainStrength(3);
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

        public void randomize(RandomGen random, boolean training) {
            int b = random.nextInt(10) + 1;
            if (b < 10 && training) {
                health = (int) Math.round(((double) (health * b)) / 10);
            } else {
                health = 85 + random.nextInt(6);
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

        boolean tookDamage = false;

        public Lagavulin() {
            this(116);
        }

        public Lagavulin(int health) {
            super(health, 6);
            isElite = true;
            canGainBlock = true;
            changePlayerStrength = true;
            changePlayerDexterity = true;
        }

        public Lagavulin(Lagavulin other) {
            this(other.health);
            setSharedFields(other);
            tookDamage = other.tookDamage;
        }

        @Override public Enemy copy() {
            return new Lagavulin(this);
        }

        @Override public void damage(int n, GameState state) {
            var dmg = Math.max(0, n - block);
            super.damage(n, state);
            if (!tookDamage && dmg > 0) {
                tookDamage = true;
            }
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            var dmg = blockable ? Math.max(0, n - block) : n;
            super.nonAttackDamage(n, blockable, state);
            if (!tookDamage && dmg > 0) {
                tookDamage = true;
            }
        }

        @Override public void endTurn() {
            super.endTurn();
            if (move <= WAIT_3) {
                gainBlock(8);
            }
        }

        @Override public void doMove(GameState state) {
            if (move == ATTACK_1 || move == ATTACK_2) {
                state.enemyDoDamageToPlayer(this, 20, 1);
            } else if (move == SIPHON_SOUL) {
                var player = state.getPlayerForWrite() ;
                player.applyDebuff(state, DebuffType.LOSE_DEXTERITY, -2);
                player.applyDebuff(state, DebuffType.LOSE_STRENGTH, -2);
            }
        }

        @Override public void nextMove(RandomGen random) {
            if (move <= 2 && tookDamage) {
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
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training) {
            int b = random.nextInt(10) + 1;
            if (training && b < 10) {
                health = (int) Math.round(((double) (health * b)) / 10);
            } else {
                health = 112 + random.nextInt(4);
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
            super(health, 2);
            this.startMove = startMove;
            artifact = 1;
            canDaze = true;
            hasArtifact = true;
        }

        public Sentry(Sentry other) {
            this(other.health, other.startMove);
            setSharedFields(other);
        }

        @Override public Enemy copy() {
            return new Sentry(this);
        }

        @Override public void doMove(GameState state) {
            if (move == BEAM) {
                state.enemyDoDamageToPlayer(this, 10, 1);
            } else if (move == BOLT) {
                state.addCardToDiscard(state.prop.dazedCardIdx);
                state.addCardToDiscard(state.prop.dazedCardIdx);
                state.addCardToDiscard(state.prop.dazedCardIdx);
            }
        }

        @Override public void nextMove(RandomGen random) {
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

        public void randomize(RandomGen random, boolean training) {
            int b = random.nextInt(4) + 1;
            if (training && b < 4) {
                health = (int) Math.round(((double) (health * b)) / 4);
            } else {
                health = 39 + random.nextInt(7);
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
            super(health, 9);
            canGainStrength = true;
            canGainBlock = true;
            afterFirstInfernal = false;
        }

        public Hexaghost(Hexaghost other) {
            this(other.health);
            setSharedFields(other);
            afterFirstInfernal = other.afterFirstInfernal;
        }

        @Override public Enemy copy() {
            return new Hexaghost(this);
        }

        @Override public void doMove(GameState state) {
            if (move == DIVIDER) {
                int n = state.getPlayeForRead().getHealth() / 12 + 1;
                state.enemyDoDamageToPlayer(this, n, 6);
            } else if (move == SEAR_1 || move == SEAR_2 || move == SEAR_3) {
                state.enemyDoDamageToPlayer(this, 6, 1);
            } else if (move == TACKLE_1 || move == TACKLE_2) {
                state.enemyDoDamageToPlayer(this, 6, 2);
            } else if (move == INFLAME_1) {
                strength += 3;
                gainBlock(12);;
            } else if (move == INFERNAL_1) {
                state.enemyDoDamageToPlayer(this, 3, 6);
            }
        }

        @Override public void nextMove(RandomGen random) {
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

        public void randomize(RandomGen random, boolean training) {
            int b = random.nextInt(25) + 1;
            health = 264 * b / 25;
        }

        public String getName() {
            return "Hexaghost";
        }
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
            super(health, 7);
            isElite = true;
            canGainBlock = true;
            canVulnerable = true;
            canWeaken = true;
            modeShiftDmg = 40;
            maxModeShiftDmg = 40;
        }

        public TheGuardian(TheGuardian other) {
            this(other.health);
            setSharedFields(other);
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

        @Override public void doMove(GameState state) {
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

        @Override public void nextMove(RandomGen random) {
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

        public void randomize(RandomGen random, boolean training) {
            int b = training ? random.nextInt(25) + 1 : 25;
            health = 250 * b / 25;
        }

        @Override public void startOfGameSetup(GameState state) {
            var idx = state.getEnemiesForRead().find(this);
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card) {
                    if (card.cardType == Card.ATTACK) {
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

        @Override public String toString() {
            String s = super.toString();
            return s.subSequence(0, s.length() - 1) + ", modeShift=" + modeShiftDmg + "/" + maxModeShiftDmg + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && maxModeShiftDmg == ((TheGuardian) o).maxModeShiftDmg && modeShiftDmg == ((TheGuardian) o).modeShiftDmg;
        }
    }

    public static class SlimeBoss extends Enemy {
        static int GOOP_SPRAY = 0;
        static int PREPARING = 1;
        static int SLAM = 2;
        static int SPLIT = 3;

        public SlimeBoss() {
            this(150);
        }

        public SlimeBoss(int health) {
            super(health, 4);
            canSlime = true;
        }

        public SlimeBoss(SlimeBoss other) {
            this(other.health);
            setSharedFields(other);
        }

        @Override public Enemy copy() {
            return new SlimeBoss(this);
        }

        @Override public void damage(int n, GameState state) {
            super.damage(n, state);
            if (health <= maxHealth / 2) {
                move = SPLIT;
            }
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (health <= maxHealth / 2) {
                move = SPLIT;
            }
        }

        @Override public void doMove(GameState state) {
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
                        var enemy = enemies.getForWrite(i);
                        enemy.health = health;
                        ((Enemy.LargeAcidSlime) enemy).splitMaxHealth = health;
                        state.enemiesAlive += 1;
                    } else if (enemies.get(i) instanceof Enemy.LargeSpikeSlime) {
                        var enemy = enemies.getForWrite(i);
                        enemy.health = health;
                        ((Enemy.LargeSpikeSlime) enemy).splitMaxHealth = health;
                        state.enemiesAlive += 1;
                    }
                }
                health = 0;
                state.enemiesAlive -= 1;
            }
        }

        @Override public void nextMove(RandomGen random) {
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

        public void randomize(RandomGen random, boolean training) {
            int b = random.nextInt(15) + 1;
            health = 10 * (training ? b : 15);
            if (health <= maxHealth / 2) {
                move = SPLIT;
            }
        }

        public String getName() {
            return "Slime Boss";
        }
    }

    public static class LargeSpikeSlime extends Enemy {
        private static final int FLAME_TACKLE = 0;
        private static final int LICK = 1;
        private static final int SPLIT = 2;

        public int splitMaxHealth;

        public LargeSpikeSlime() {
            this(73);
        }

        public LargeSpikeSlime(int health) {
            super(health, 3);
            moveHistory = new int[] {-1};
            canSlime = true;
            canFrail = true;
            splitMaxHealth = maxHealth;
        }

        public LargeSpikeSlime(LargeSpikeSlime other) {
            this(other.health);
            setSharedFields(other);
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

        @Override public void doMove(GameState state) {
            if (move == FLAME_TACKLE) {
                state.enemyDoDamageToPlayer(this, 18, 1);
                state.addCardToDiscard(state.prop.slimeCardIdx);
                state.addCardToDiscard(state.prop.slimeCardIdx);
            } else if (move == LICK) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 4);
            } else if (move == SPLIT) {
                var enemies = state.getEnemiesForWrite();
                for (int i = 0; i < enemies.size(); i++) {
                    if (enemies.get(i) instanceof Enemy.MediumSpikeSlime) {
                        enemies.getForWrite(i).health = health;
                        state.enemiesAlive += 1;
                    }
                }
                health = 0;
                state.enemiesAlive -= 1;
            }
        }

        @Override public void nextMove(RandomGen random) {
            if (move == SPLIT) {
                return;
            }
            int r = random.nextInt(100);
            int newMove;
            if (r < 30) {
                if (move == FLAME_TACKLE && moveHistory[0] == FLAME_TACKLE) {
                    newMove = LICK;
                } else {
                    newMove = FLAME_TACKLE;
                }
            } else if (move == LICK) {
                newMove = FLAME_TACKLE;
            } else {
                newMove = LICK;
            }
            moveHistory[0] = move;
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

        public void randomize(RandomGen random, boolean training) {
            health = 67 + random.nextInt(7);
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
            super(health, 2);
            moveHistory = new int[] {-1};
            canSlime = true;
            canFrail = true;
        }

        public MediumSpikeSlime(int health, boolean startDead) {
            this(health);
            if (startDead) this.health = 0;
        }

        public MediumSpikeSlime(MediumSpikeSlime other) {
            this(other.health);
            setSharedFields(other);
        }

        @Override public Enemy copy() {
            return new MediumSpikeSlime(this);
        }

        @Override public void doMove(GameState state) {
            if (move == FLAME_TACKLE) {
                state.enemyDoDamageToPlayer(this, 10, 1);
                state.addCardToDiscard(state.prop.slimeCardIdx);
            } else if (move == LICK) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 2);
            }
        }

        @Override public void nextMove(RandomGen random) {
            int r = random.nextInt(100);
            int newMove;
            if (r < 30) {
                if (move == FLAME_TACKLE && moveHistory[0] == FLAME_TACKLE) {
                    newMove = LICK;
                } else {
                    newMove = FLAME_TACKLE;
                }
            } else if (move == LICK) {
                newMove = FLAME_TACKLE;
            } else {
                newMove = LICK;
            }
            moveHistory[0] = move;
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

        public void randomize(RandomGen random, boolean training) {
            health = 29 + random.nextInt(6);
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
            super(health, 1);
        }

        public SmallSpikeSlime(SmallSpikeSlime other) {
            this(other.health);
            setSharedFields(other);
        }

        @Override public Enemy copy() {
            return new SmallSpikeSlime(this);
        }

        @Override public void doMove(GameState state) {
            state.enemyDoDamageToPlayer(this, 6, 1);
        }

        @Override public void nextMove(RandomGen random) {
            move = TACKLE;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training) {
            health = 11 + random.nextInt(5);
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

        public int splitMaxHealth;

        public LargeAcidSlime() {
            this(72);
        }

        public LargeAcidSlime(int health) {
            super(health, 3);
            moveHistory = new int[] {-1};
            canSlime = true;
            canWeaken = true;
        }

        public LargeAcidSlime(int health, boolean startDead) {
            this(health);
            if (startDead) this.health = 0;
            splitMaxHealth = maxHealth;
        }

        public LargeAcidSlime(LargeAcidSlime other) {
            this(other.health);
            setSharedFields(other);
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

        @Override public void doMove(GameState state) {
            if (move == CORROSIVE_SPIT) {
                state.enemyDoDamageToPlayer(this, 12, 1);
                state.addCardToDiscard(state.prop.slimeCardIdx);
                state.addCardToDiscard(state.prop.slimeCardIdx);
            } else if (move == TACKLE) {
                state.enemyDoDamageToPlayer(this, 18, 1);
            } else if (move == LICK) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 3);
            } else if (move == SPLIT) {
                var enemies = state.getEnemiesForWrite();
                for (int i = 0; i < enemies.size(); i++) {
                    if (enemies.get(i) instanceof Enemy.MediumAcidSlime) {
                        enemies.getForWrite(i).health = health;
                        state.enemiesAlive += 1;
                    }
                }
                health = 0;
                state.enemiesAlive -= 1;
            }
        }

        @Override public void nextMove(RandomGen random) {
            if (move == SPLIT) {
                return;
            }
            int r = random.nextInt(100);
            int newMove;
            if (r < 40) {
                if (move == CORROSIVE_SPIT && moveHistory[0] == CORROSIVE_SPIT) {
                    newMove = random.nextFloat() < 0.6 ? TACKLE : LICK;
                } else {
                    newMove = CORROSIVE_SPIT;
                }
            } else if (r < 70) {
                if (move == TACKLE && moveHistory[0] == TACKLE) {
                    newMove = random.nextFloat() < 0.6 ? CORROSIVE_SPIT : LICK;
                } else {
                    newMove = TACKLE;
                }
            } else if (move == LICK) {
                newMove = random.nextFloat() < 0.4 ? CORROSIVE_SPIT : TACKLE;
            } else {
                newMove = LICK;
            }
            moveHistory[0] = move;
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

        public void randomize(RandomGen random, boolean training) {
            health = 68 + random.nextInt(5);
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
            super(health, 3);
            moveHistory = new int[] {-1};
            canSlime = true;
            canWeaken = true;
        }

        public MediumAcidSlime(int health, boolean startDead) {
            this(health);
            if (startDead) this.health = 0;
        }


        public MediumAcidSlime(MediumAcidSlime other) {
            this(other.health);
            setSharedFields(other);
        }

        @Override public Enemy copy() {
            return new MediumAcidSlime(this);
        }

        @Override public void doMove(GameState state) {
            if (move == CORROSIVE_SPIT) {
                state.enemyDoDamageToPlayer(this, 8, 1);
                state.addCardToDiscard(state.prop.slimeCardIdx);
            } else if (move == TACKLE) {
                state.enemyDoDamageToPlayer(this, 12, 1);
            } else if (move == LICK) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2);
            }
        }

        @Override public void nextMove(RandomGen random) {
            int r = random.nextInt(100);
            int newMove;
            if (r < 40) {
                if (move == CORROSIVE_SPIT && moveHistory[0] == CORROSIVE_SPIT) {
                    newMove = random.nextBoolean() ? TACKLE : LICK;
                } else {
                    newMove = CORROSIVE_SPIT;
                }
            } else if (r < 80) {
                if (move == TACKLE && moveHistory[0] == TACKLE) {
                    newMove = random.nextBoolean() ? CORROSIVE_SPIT : LICK;
                } else {
                    newMove = TACKLE;
                }
            } else if (move == LICK) {
                newMove = random.nextFloat() < 0.4 ? CORROSIVE_SPIT : TACKLE;
            } else {
                newMove = LICK;
            }
            moveHistory[0] = move;
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

        public void randomize(RandomGen random, boolean training) {
            health = 29 + random.nextInt(6);
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
            super(health, 2);
        }

        public SmallAcidSlime(SmallAcidSlime other) {
            this(other.health);
            setSharedFields(other);
            canWeaken = true;
        }

        @Override public Enemy copy() {
            return new SmallAcidSlime(this);
        }

        @Override public void doMove(GameState state) {
            if (move == TACKLE) {
                state.enemyDoDamageToPlayer(this, 4, 1);
            } else if (move == LICK) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2);
            }
        }

        @Override public void nextMove(RandomGen random) {
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

        public void randomize(RandomGen random, boolean training) {
            health = 9 + random.nextInt(5);
        }

        public String getName() {
            return "Acid Slime (S)";
        }
    }

    public static class JawWorm extends Enemy {
        public JawWorm() {
            this(46);
        }

        public JawWorm(int health) {
            super(health, 3);
            canGainStrength = true;
            canGainBlock = true;
            moveHistory = new int[] {-1};
        }

        public JawWorm(JawWorm other) {
            this(other.health);
            setSharedFields(other);
        }

        @Override public Enemy copy() {
            return new JawWorm(this);
        }

        @Override public void doMove(GameState state) {
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

        @Override public void nextMove(RandomGen random) {
            if (move == -1) {
                move = 0;
                return;
            }
            int r = random.nextInt(100);
            int newMove;
            if (r < 25) {
                if (move == 0) {
                    newMove = random.nextFloat() < 0.5625 ? 1 : 2;
                } else {
                    newMove = 0;
                }
            } else if (r < 55) {
                if (move == 2 && moveHistory[0] == 2) {
                    newMove = random.nextFloat() < 0.357 ? 0 : 1;
                } else {
                    newMove = 2;
                }
            } else if (move == 1) {
                newMove = random.nextFloat() < 0.416 ? 0 : 2;
            } else {
                newMove = 1;
            }
            moveHistory[0] = move;
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

        public void randomize(RandomGen random, boolean training) {
            health = 42 + random.nextInt(5);
        }

        public String getName() {
            return "Jaw Worm";
        }
    }

    public static class RedLouse extends Enemy {
        static final int BITE = 0;
        static final int GROW = 1;

        int d = 8;
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
            super(health, 2);
            canGainStrength = true;
            canGainBlock = true;
            moveHistory = new int[] {-1};
        }

        public RedLouse(RedLouse other) {
            this(other.health);
            setSharedFields(other);
            d = other.d;
            curlUpAmount = other.curlUpAmount;
            hasCurledUp = other.hasCurledUp;
        }

        @Override public Enemy copy() {
            return new RedLouse(this);
        }

        @Override public void doMove(GameState state) {
            if (move == BITE) {
                state.enemyDoDamageToPlayer(this, d, 1);
            } else if (move == GROW) {
                strength += 4;
            }
        }

        @Override public void nextMove(RandomGen random) {
            int r = random.nextInt(100);
            int newMove;
            if (r < 25) {
                newMove = move == GROW ? BITE : GROW;
            } else if (move == BITE && moveHistory[0] == BITE) {
                newMove = GROW;
            } else {
                newMove = BITE;
            }
            moveHistory[0] = move;
            move = newMove;
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

        public void randomize(RandomGen random, boolean training) {
            health = 11 + random.nextInt(6);
            d = 6 + random.nextInt(3);
            curlUpAmount = 9 + random.nextInt(4);
        }

        public String getName() {
            return "Red Louse";
        }
    }

    public static class GreenLouse extends Enemy {
        static final int BITE = 0;
        static final int SPIT_WEB = 1;

        int d = 8;
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
            super(health, 2);
            canWeaken = true;
            moveHistory = new int[] {-1};
        }

        public GreenLouse(GreenLouse other) {
            this(other.health);
            setSharedFields(other);
            d = other.d;
            curlUpAmount = other.curlUpAmount;
            hasCurledUp = other.hasCurledUp;
        }

        @Override public Enemy copy() {
            return new GreenLouse(this);
        }

        @Override public void doMove(GameState state) {
            if (move == BITE) {
                state.enemyDoDamageToPlayer(this, d, 1);
            } else if (move == SPIT_WEB) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 3);
            }
        }

        @Override public void nextMove(RandomGen random) {
            int r = random.nextInt(100);
            int newMove;
            if (r < 25) {
                newMove = move == SPIT_WEB ? BITE : SPIT_WEB;
            } else if (move == BITE && moveHistory[0] == BITE) {
                newMove = SPIT_WEB;
            } else {
                newMove = BITE;
            }
            moveHistory[0] = move;
            move = newMove;
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

        public void randomize(RandomGen random, boolean training) {
            health = 12 + random.nextInt(7);
            d = 6 + random.nextInt(3);
            curlUpAmount = 9 + random.nextInt(4);
        }

        public String getName() {
            return "Green Louse";
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
            super(health, 2);
            canVulnerable = true;
            canGainStrength = true;
            moveHistory = new int[] {-1};
        }

        public FungiBeast(FungiBeast other) {
            this(other.health);
            setSharedFields(other);
            isDead = other.isDead;
        }

        @Override public Enemy copy() {
            return new FungiBeast(this);
        }

        @Override public void doMove(GameState state) {
            if (move == BITE) {
                state.enemyDoDamageToPlayer(this, 6, 1);
            } else if (move == GROW) {
                strength += 5;
            }
        }

        @Override public void nextMove(RandomGen random) {
            int r = random.nextInt(100);
            int newMove;
            if (r < 60) {
                if (move == 1 && moveHistory[0] == 1) {
                    newMove = GROW;
                } else {
                    newMove = BITE;
                }
            } else if (move == GROW) {
                newMove = BITE;
            } else {
                newMove = GROW;
            }
            moveHistory[0] = move;
            move = newMove;
        }

        @Override public void damage(int n, GameState state) {
            super.damage(n, state);
            if (!isDead && health <= 0) {
                isDead = true;
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 3);
            }
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (!isDead && health <= 0) {
                isDead = true;
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 3);
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

        public void randomize(RandomGen random, boolean training) {
            health = 24 + random.nextInt(5);
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
            super(health, 5);
            canGainBlock = true;
        }

        public Looter(Looter other) {
            this(other.health);
            setSharedFields(other);
        }

        @Override public Enemy copy() {
            return new Looter(this);
        }

        @Override public void doMove(GameState state) {
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

        @Override public void nextMove(RandomGen random) {
            int newMove;
            if (move < 0) {
                newMove = MUG_1;
            } else if (move == MUG_1) {
                newMove = MUG_2;
            } else if (move == MUG_2) {
                newMove = random.nextInt(100) < 50 ? LUNGE : SMOKE_BOMB;
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

        public void randomize(RandomGen random, boolean training) {
            health = 46 + random.nextInt(5);
        }

        public String getName() {
            return "Looter";
        }
    }
}
