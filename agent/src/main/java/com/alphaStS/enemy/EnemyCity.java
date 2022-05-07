package com.alphaStS.enemy;

import com.alphaStS.*;

public class EnemyCity {
    public static class GremlinLeader extends Enemy {
        static final int ENCOURAGE = 0;
        static final int RALLY = 1;
        static final int STAB = 2;

        public GremlinLeader() {
            this(155);
            isElite = true;
            canGainStrength = true;
            canGainBlock = true;
        }

        public GremlinLeader(int health) {
            super(health, 3);
        }

        public GremlinLeader(GremlinLeader other) {
            this(other.health);
            setSharedFields(other);
        }

        @Override public Enemy copy() {
            return new GremlinLeader(this);
        }

        @Override public void damage(int n, GameState state) {
            super.damage(n, state);
            if (health <= 0) {
                var enemies = state.getEnemiesForRead();
                for (int i = 0; i < enemies.size(); i++) {
                    state.killEnemy(i);
                }
            }
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (health <= 0) {
                var enemies = state.getEnemiesForRead();
                for (int i = 0; i < enemies.size(); i++) {
                    state.killEnemy(i);
                }
            }
        }

        @Override public void doMove(GameState state) {
            if (move == ENCOURAGE) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemy.gainStrength(5);
                    if (enemy != this) {
                        enemy.gainBlock(10);
                    }
                }
            } else if (move == RALLY) {
                state.isStochastic = true;
                var enemies = state.getEnemiesForWrite();
                var startIdx = 0;
                for (int i = 0; i < enemies.size(); i++) {
                    if (enemies.get(i) instanceof Enemy.MadGremlin) {
                        startIdx = i;
                        break;
                    }
                }
                for (int i = 0; i < 2; i++) {
                    int r = state.getSearchRandomGen().nextInt(8, RandomGenCtx.GremlinLeader);
                    var idx = -1;
                    if (r < 2) { // Mad Gremlin
                        idx = startIdx + 2;
                        if (enemies.get(startIdx).getHealth() <= 0) {
                            idx = startIdx;
                        } else if (enemies.get(startIdx + 1).getHealth() <= 0) {
                            idx = startIdx + 1;
                        }
                    } else if (r < 4) { // Sneaky Gremlin
                        idx = startIdx + 5;
                        if (enemies.get(startIdx + 3).getHealth() <= 0) {
                            idx = startIdx + 3;
                        } else if (enemies.get(startIdx + 4).getHealth() <= 0) {
                            idx = startIdx + 4;
                        }
                    } else if (r < 6) { // Fat Gremlin
                        idx = startIdx + 8;
                        if (enemies.get(startIdx + 6).getHealth() <= 0) {
                            idx = startIdx + 6;
                        } else if (enemies.get(startIdx + 7).getHealth() <= 0) {
                            idx = startIdx + 7;
                        }
                    } else if (r < 7) { // Shield Gremlin
                        idx = startIdx + 11;
                        if (enemies.get(startIdx + 9).getHealth() <= 0) {
                            idx = startIdx + 9;
                        } else if (enemies.get(startIdx + 10).getHealth() <= 0) {
                            idx = startIdx + 10;
                        }
                    } else { // Gremlin Wizard
                        idx = startIdx + 14;
                        if (enemies.get(startIdx + 12).getHealth() <= 0) {
                            idx = startIdx + 12;
                        } else if (enemies.get(startIdx + 13).getHealth() <= 0) {
                            idx = startIdx + 13;
                        }
                    }
                    state.reviveEnemy(idx);
                    if (state.enemiesAlive == 4) {
                        var j = 0;
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            if (enemy.health > 0) {
                                j += 1;
                            }
                        }
                        if (j < 4) {
                            System.out.println(state.enemiesAlive);
                        }
                    }
                }
            } else if (move == STAB) {
                state.enemyDoDamageToPlayer(this, 6, 3);
            }
        }

        private void nextMove(GameState state, RandomGen random, int r) {
            if (state.enemiesAlive == 1) { // 0 gremlin
                if (r < 75) {
                    if (move != RALLY) {
                        move = RALLY;
                    } else {
                        move = STAB;
                    }
                } else if (move != STAB) {
                    move = STAB;
                } else {
                    move = RALLY;
                }
            } else if (state.enemiesAlive == 2) { // 1 gremlin
                if (r < 50) {
                    if (move != RALLY) {
                        move = RALLY;
                    } else {
                        nextMove(state, random, 50 + random.nextInt(50, RandomGenCtx.EnemyChooseMove));
                    }
                } else if (r < 80) {
                    if (move != ENCOURAGE) {
                        move = ENCOURAGE;
                    } else {
                        move = STAB;
                    }
                } else if (move != STAB) {
                    move = STAB;
                } else {
                    nextMove(state, random, random.nextInt(80, RandomGenCtx.EnemyChooseMove));
                }
            } else {
                if (r < 66) {
                    if (move != ENCOURAGE) {
                        move = ENCOURAGE;
                    } else {
                        move = STAB;
                    }
                } else if (move != STAB) {
                    move = STAB;
                } else {
                    move = ENCOURAGE;
                }
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            nextMove(state, random, random.nextInt(100, RandomGenCtx.EnemyChooseMove));
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == ENCOURAGE) {
                return "Give all allies 5 strength and 10 block";
            } else if (move == RALLY) {
                return "Rally";
            } else if (move == STAB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6) + "x3";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training) {
            int b = random.nextInt(10, RandomGenCtx.Other) + 1;
            if (training && b < 10) {
                health = (int) Math.round(((double) (health * b)) / 10);
            } else {
                health = 145 + random.nextInt(11, RandomGenCtx.Other);
            }
        }

        public String getName() {
            return "Gremlin Leader";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var enemies = state.getEnemiesForWrite();
            for (int i = 0; i < enemies.size(); i++) {
                if (enemies.get(i).getName().contains("Gremlin")) {
                    enemies.get(i).canGainBlock = true;
                    enemies.get(i).canGainStrength = true;
                }
            }
        }
    }

    public static class Byrd extends Enemy {
        static int CAW = 0;
        static int PECK = 1;
        static int SWOOP = 2;
        static int STUNNED = 3;
        static int HEADBUTT = 4;
        static int FLY = 5;

        int flying = 4;

        public int getFlying() {
            return flying;
        }

        public Byrd() {
            this(33);
        }

        public Byrd(int health) {
            super(health, 5);
            moveHistory = new int[] {-1};
            canGainStrength = true;
        }

        public Byrd(Byrd other) {
            this(other.health);
            setSharedFields(other);
            flying = other.flying;
        }

        @Override public Enemy copy() {
            return new Byrd(this);
        }

        @Override public void damage(int n, GameState state) {
            if (flying == 0) {
                super.damage(n, state);
                return;
            }
            int oldHealth = health;
            super.damage(n / 2, state);
            if (oldHealth != health && flying > 0) {
                flying--;
                if (flying == 0) {
                    move = STUNNED;
                }
            }
        }

        @Override public void startTurn() {
            if (flying > 0) {
                flying = 4;
            }
        }

        @Override public void doMove(GameState state) {
            if (move == CAW) {
               gainStrength(1);
            } else if (move == PECK) {
                state.enemyDoDamageToPlayer(this, 1, 6);
            } else if (move == SWOOP) {
                state.enemyDoDamageToPlayer(this, 14, 1);
            } else if (move == FLY) {
                flying = 4;
            } else if (move == HEADBUTT) {
                state.enemyDoDamageToPlayer(this, 3, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move == STUNNED || move == HEADBUTT) {
                newMove = move + 1;
            } else if (move == -1) { // first turn
                if (random.nextInt(200, RandomGenCtx.EnemyChooseMove) < 125) {
                    newMove = PECK;
                } else {
                    newMove = CAW;
                }
            } else {
                int n = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                if (n < 50) {
                    if (move == PECK && moveHistory[0] == PECK) {
                        if (random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.4f) {
                            newMove = SWOOP;
                        } else {
                            newMove = CAW;
                        }
                    } else {
                        newMove = PECK;
                    }
                } else if (n < 70) {
                    if (move == SWOOP) {
                        if (random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.375f) {
                            newMove = CAW;
                        } else {
                            newMove = PECK;
                        }
                    } else {
                        newMove = SWOOP;
                    }
                } else if (move == CAW) {
                    if (random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.2857F) {
                        newMove = SWOOP;
                    } else {
                        newMove = PECK;
                    }
                } else {
                    newMove = CAW;
                }
            }
            moveHistory[0] = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CAW) {
                return "Gain 1 Strength";
            } else if (move == PECK) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 1) + "x6";
            } else if (move == SWOOP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14);
            } else if (move == FLY) {
                return "Fly";
            } else if (move == HEADBUTT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 3);
            } else if (move == STUNNED) {
                return "Stunned";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training) {
            int a = random.nextInt(8, RandomGenCtx.Other, null);
            health = 26 + a;
            if (training) {
                int b = random.nextInt(3, RandomGenCtx.Other, null) + 1;
                health = (int) (((double) b) / 3 * health);
            }
        }

        public String getName() {
            return "Byrd";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return s.subSequence(0, s.length() - 1) + ", flying=" + flying + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && flying == ((Byrd) o).flying;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of byrd flying";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = flying / 4f;
            return 1;
        }
    }

    public static class SphericGuardian extends Enemy {
        static int ACTIVATE = 0;
        static int ATTACK_DEBUFF = 1;
        static int SLAM = 2;
        static int HARDEN = 3;

        public SphericGuardian() {
            this(20);
        }

        public SphericGuardian(int health) {
            super(health, 4);
            canFrail = true;
            canGainBlock = true;
            hasArtifact = true;
            artifact = 3;
            block = 40;
        }

        public SphericGuardian(SphericGuardian other) {
            this(other.health);
            setSharedFields(other);
        }

        @Override public void startTurn() {}

        @Override public Enemy copy() {
            return new SphericGuardian(this);
        }

        @Override public void doMove(GameState state) {
            if (move == ACTIVATE) {
                gainBlock(35);
            } else if (move == ATTACK_DEBUFF) {
                state.enemyDoDamageToPlayer(this, 11, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 5 + 1);
            } else if (move == SLAM) {
                state.enemyDoDamageToPlayer(this, 11, 2);
            } else if (move == HARDEN) {
                state.enemyDoDamageToPlayer(this, 11, 1);
                gainBlock(15);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move++;
            if (move == 4) {
                move = 2;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == ACTIVATE) {
                return "Gain 35 Block";
            } else if (move == ATTACK_DEBUFF) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11) + "+Frail 5";
            } else if (move == SLAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11) + "x2";
            } else if (move == HARDEN) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11) + "+Block 15";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training) {
            if (training) {
                block = random.nextInt(6, RandomGenCtx.Other, null) * 8;
            }
        }

        public String getName() {
            return "Spheric Guardian";
        }
    }

    public static class Pointy extends Enemy {
        static int ATTACK = 0;

        public Pointy() {
            this(34);
        }

        public Pointy(int health) {
            super(health, 1);
        }

        public Pointy(Pointy other) {
            this(other.health);
            setSharedFields(other);
        }

        @Override public void startTurn() {}

        @Override public Enemy copy() {
            return new Pointy(this);
        }

        @Override public void doMove(GameState state) {
            if (move == ATTACK) {
                state.enemyDoDamageToPlayer(this, 6, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = ATTACK;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == ATTACK) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6) + "x2";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training) {
            if (training) {
                health = (int) ((random.nextInt(3, RandomGenCtx.Other, null) + 1) / 3.0 * 34);
            }
        }

        public String getName() {
            return "Pointy";
        }
    }

    public static class Romeo extends Enemy {
        static int MOCK = 0;
        static int AGONIZING_SLASH = 1;
        static int CROSS_SLASH_1 = 2;
        static int CROSS_SLASH_2 = 3;

        public Romeo() {
            this(41);
        }

        public Romeo(int health) {
            super(health, 4);
            canWeaken = true;
        }

        public Romeo(Romeo other) {
            this(other.health);
            setSharedFields(other);
        }

        @Override public void startTurn() {}

        @Override public Enemy copy() {
            return new Romeo(this);
        }

        @Override public void doMove(GameState state) {
            if (move == AGONIZING_SLASH) {
                state.enemyDoDamageToPlayer(this, 12, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 3 + 1);
            } else if (move == CROSS_SLASH_1 || move == CROSS_SLASH_2) {
                state.enemyDoDamageToPlayer(this, 17, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move++;
            if (move > CROSS_SLASH_2) {
                move = AGONIZING_SLASH;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == MOCK) {
                return "Mock";
            } if (move == AGONIZING_SLASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12) + "+Weak 3";
            } else if (move == CROSS_SLASH_1 || move == CROSS_SLASH_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 17);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training) {
            int b = random.nextInt(5, RandomGenCtx.Other, null) + 1;
            if (training && b < 5) {
                health = (int) Math.round(((double) (health * b)) / 4);
            } else {
                health = 37 + random.nextInt(5, RandomGenCtx.Other, null);
            }
        }

        public String getName() {
            return "Romeo";
        }
    }

    public static class Bear extends Enemy {
        static int BEAR_HUG = 0;
        static int LUNGE = 1;
        static int MAUL = 2;

        public Bear() {
            this(44);
        }

        public Bear(int health) {
            super(health, 3);
            changePlayerDexterity = true;
            canGainBlock = true;
        }

        public Bear(Bear other) {
            this(other.health);
            setSharedFields(other);
        }

        @Override public void startTurn() {}

        @Override public Enemy copy() {
            return new Bear(this);
        }

        @Override public void doMove(GameState state) {
            if (move == BEAR_HUG) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_DEXTERITY, 4);
            } else if (move == LUNGE) {
                state.enemyDoDamageToPlayer(this, 10, 1);
                gainBlock(9);
            } else if (move == MAUL) {
                state.enemyDoDamageToPlayer(this, 20, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move++;
            if (move > MAUL) {
                move = LUNGE;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BEAR_HUG) {
                return "-4 Dexterity";
            } else if (move == LUNGE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10) + "+Block 9";
            } else if (move == MAUL) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 20);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training) {
            int b = random.nextInt(5, RandomGenCtx.Other, null) + 1;
            if (training && b < 5) {
                health = (int) Math.round(((double) (health * b)) / 4);
            } else {
                health = 40 + random.nextInt(5, RandomGenCtx.Other, null);
            }
        }

        public String getName() {
            return "Bear";
        }
    }
}
