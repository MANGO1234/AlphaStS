package com.alphaStS.enemy;

import com.alphaStS.*;

import java.util.List;

public class EnemyCity {
    public static class GremlinLeader extends Enemy {
        static final int ENCOURAGE = 0;
        static final int RALLY = 1;
        static final int STAB = 2;

        public GremlinLeader() {
            this(155);
        }

        public GremlinLeader(int health) {
            super(health, 3);
            isElite = true;
            canGainStrength = true;
            canGainBlock = true;
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

        @Override public void randomize(RandomGen random, boolean training) {
            int b = random.nextInt(10, RandomGenCtx.Other) + 1;
            if (training && b < 10) {
                health = (int) Math.round(((double) (health * b)) / 10);
            } else {
                health = 145 + random.nextInt(11, RandomGenCtx.Other);
            }
        }

        @Override public String getName() {
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

    public static class BookOfStabbing extends Enemy {
        static final int MULTI_STAB = 0;
        static final int SINGLE_STAB = 1;

        private int stabCount;

        public BookOfStabbing() {
            this(172);
        }

        public BookOfStabbing(int health) {
            super(health, 2);
            moveHistory = new int[] {-1};
            isElite = true;
            stabCount = 1;
        }

        public BookOfStabbing(BookOfStabbing other) {
            this(other.health);
            setSharedFields(other);
            stabCount = other.stabCount;
        }

        @Override public Enemy copy() {
            return new BookOfStabbing(this);
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && stabCount == ((BookOfStabbing) o).stabCount;
        }

        @Override public void doMove(GameState state) {
            if (move == MULTI_STAB) {
                state.enemyDoDamageToPlayer(this, 7, stabCount);
            } else if (move == SINGLE_STAB) {
                state.enemyDoDamageToPlayer(this, 24, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
            int newMove;
            if (r < 15) {
                if (move == SINGLE_STAB) {
                    newMove = MULTI_STAB;
                } else {
                    newMove = SINGLE_STAB;
                }
            } else if (move == MULTI_STAB && moveHistory[0] == MULTI_STAB) {
                newMove = SINGLE_STAB;
            } else {
                newMove = MULTI_STAB;
            }
            stabCount++;
            moveHistory[0] = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == MULTI_STAB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "x" + stabCount;
            } else if (move == SINGLE_STAB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 24);
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training) {
            int b = random.nextInt(10, RandomGenCtx.Other) + 1;
            if (training && b < 10) {
                health = (int) Math.round(((double) (health * b)) / 10);
            } else {
                health = 168 + random.nextInt(5, RandomGenCtx.Other);
            }
        }

        @Override public String getName() {
            return "Book of Stabbing";
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 inputs to keep track of current book of stabbing multi-hit counts";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = stabCount / 10f;
            return 1;
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            return List.of(new Card.Wound());
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.addOnDamageHandler("BookOfStabbing", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (source instanceof BookOfStabbing && damageDealt > 0) {
                        state.addCardToDiscard(state.prop.woundCardIdx);
                    }
                }
            });
        }
    }

    public static class Taskmaster extends Enemy {
        static final int SCOURING_WHIP = 0;

        public Taskmaster() {
            this(64);
            isElite = true;
            canGainStrength = true;
        }

        public Taskmaster(int health) {
            super(health, 1);
        }

        public Taskmaster(Taskmaster other) {
            this(other.health);
            setSharedFields(other);
        }

        @Override public Enemy copy() {
            return new Taskmaster(this);
        }

        @Override public void doMove(GameState state) {
            if (move == SCOURING_WHIP) {
                state.enemyDoDamageToPlayer(this, 7, 1);
                gainStrength(1);
                state.addCardToDiscard(state.prop.woundCardIdx);
                state.addCardToDiscard(state.prop.woundCardIdx);
                state.addCardToDiscard(state.prop.woundCardIdx);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = SCOURING_WHIP;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SCOURING_WHIP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "+Gain Strength 1+Wound 3";
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training) {
            int b = random.nextInt(4, RandomGenCtx.Other) + 1;
            if (training && b < 4) {
                health = (int) Math.round(((double) (health * b)) / 4);
            } else {
                health = 57 + random.nextInt(8, RandomGenCtx.Other);
            }
        }

        @Override public String getName() {
            return "Taskmaster";
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            return List.of(new Card.Wound());
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
            int b = random.nextInt(4, RandomGenCtx.Other, null) + 1;
            if (training && b < 4) {
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
            int b = random.nextInt(4, RandomGenCtx.Other, null) + 1;
            if (training && b < 4) {
                health = (int) Math.round(((double) (health * b)) / 4);
            } else {
                health = 40 + random.nextInt(5, RandomGenCtx.Other, null);
            }
        }

        public String getName() {
            return "Bear";
        }
    }

    public static class Chosen extends Enemy {
        static int HEX = 0;
        static int POKE = 1;
        static int ZAP = 2;
        static int DEBILITATE = 3;
        static int DRAIN = 4;

        public Chosen() {
            this(103);
        }

        public Chosen(int health) {
            super(health, 5);
            canGainStrength = true;
            canWeaken = true;
            canVulnerable = true;
            canDaze = true;
        }

        public Chosen(Chosen other) {
            this(other.health);
            setSharedFields(other);
        }

        @Override public Enemy copy() {
            return new Chosen(this);
        }

        @Override public void doMove(GameState state) {
            if (move == HEX) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.HEX, 1);
            } else if (move == POKE) {
                state.enemyDoDamageToPlayer(this, 6, 2);
            } else if (move == ZAP) {
                state.enemyDoDamageToPlayer(this, 21, 1);
            } else if (move == DEBILITATE) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2 + 1);
            } else if (move == DRAIN) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 3 + 1);
                gainStrength(3);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == -1) {
                move = HEX;
            } else if (move == HEX || move == POKE || move == ZAP) {
                int r = random.nextInt(2, RandomGenCtx.EnemyChooseMove);
                if (r == 0) {
                    move = DRAIN;
                } else {
                    move = DEBILITATE;
                }
            } else if (move == DEBILITATE || move == DRAIN) {
                int r = random.nextInt(10, RandomGenCtx.EnemyChooseMove);
                if (r < 4) {
                    move = ZAP;
                } else {
                    move = POKE;
                }
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == HEX) {
                return "Hex";
            } else if (move == POKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6) + "x2";
            } else if (move == ZAP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 21);
            } else if (move == DEBILITATE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12) + "+Vulnerable 2";
            } else if (move == DRAIN) {
                return "Weak 3+Gain 3 Strength";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training) {
            int b = random.nextInt(6, RandomGenCtx.Other, null) + 1;
            if (training && b < 6) {
                health = (int) Math.round(((double) (health * b)) / 6);
            } else {
                health = 98 + random.nextInt(6, RandomGenCtx.Other, null);
            }
        }

        public String getName() {
            return "Chosen";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.addOnCardPlayedHandler("Chosen", new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card) {
                    if (card.cardType != Card.ATTACK && state.getPlayeForRead().isHexed()) {
                        state.addCardToDiscard(state.prop.dazedCardIdx);
                    }
                }
            });
            state.prop.addNNInputHandler("Hex", new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getPlayeForRead().isHexed() ? 0.5f : -0.5f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }
    }
}
