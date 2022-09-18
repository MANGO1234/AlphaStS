package com.alphaStS.enemy;

import com.alphaStS.*;

import java.util.List;

public class EnemyCity {
    public static class TheChamp extends Enemy {
        private static final int DEFENSIVE_STANCE = 0;
        private static final int FACE_SLAP = 1;
        private static final int TAUNT = 2;
        private static final int HEAVY_SLASH = 3;
        private static final int GLOAT = 4;
        private static final int EXECUTE = 5;
        private static final int ANGER = 6;

        static {
            defaultProperty = new EnemyProperty(7, true);
            defaultProperty.canGainStrength = true;
            defaultProperty.canGainMetallicize = true;
            defaultProperty.canWeaken = true;
            defaultProperty.canVulnerable = true;
            defaultProperty.canFrail = true;
            defaultProperty.canGainBlock = true;
        }

        private int numOfDefensiveStance;
        private int numTurns;
        private boolean angered;

        public TheChamp() {
            this(440);
        }

        public TheChamp(int health) {
            super(health);
        }

        public TheChamp(TheChamp other) {
            this(other.health);
            setSharedFields(other);
            numOfDefensiveStance = other.numOfDefensiveStance;
            numTurns = other.numTurns;
            angered = other.angered;
        }

        @Override public Enemy copy() {
            return new TheChamp(this);
        }

        @Override public void doMove(GameState state) {
            if (move == DEFENSIVE_STANCE) {
                gainBlock(20);
                gainMetallicize(7);
            } else if (move == FACE_SLAP) {
                state.enemyDoDamageToPlayer(this, 14, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 2 + 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2 + 1);
            } else if (move == TAUNT) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2 + 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2 + 1);
            } else if (move == HEAVY_SLASH) {
                state.enemyDoDamageToPlayer(this, 18, 1);
            } else if (move == GLOAT) {
                gainStrength(4);
            } else if (move == EXECUTE) {
                state.enemyDoDamageToPlayer(this, 10, 2);
            } else if (move == ANGER) {
                removeAllDebuffs();
                gainStrength(12);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            numTurns++;
            if (health < maxHealth / 2 && !angered) {
                angered = true;
                newMove = ANGER;
            } else if (angered && move != EXECUTE && lastMove != EXECUTE) {
                newMove = EXECUTE;
            } else if (numTurns == 4 && !angered) {
                numTurns = 0;
                newMove = TAUNT;
            } else {
                int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                if (move != DEFENSIVE_STANCE && numOfDefensiveStance < 2 && r < 30) {
                    numOfDefensiveStance++;
                    newMove = DEFENSIVE_STANCE;
                } else if ((move != GLOAT && move != DEFENSIVE_STANCE) && r < 30) {
                    newMove = GLOAT;
                } else if (move != FACE_SLAP && r < 55) {
                    newMove = FACE_SLAP;
                } else if (move != HEAVY_SLASH) {
                    newMove = HEAVY_SLASH;
                } else {
                    newMove = FACE_SLAP;
                }
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == DEFENSIVE_STANCE) {
                return "Block 20+Metallicize 7";
            } else if (move == FACE_SLAP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14) + "+Frail 2+Vulnerable 2";
            } else if (move == TAUNT) {
                return "Weak 2+Vulnerable 2";
            } else if (move == HEAVY_SLASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 18);
            } else if (move == GLOAT) {
                return "Strength 4";
            } else if (move == EXECUTE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10) + "x2";
            } else if (move == ANGER) {
                return "Remove All Debuffs+Strength 12";
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training) {
            int b = random.nextInt(20, RandomGenCtx.Other) + 1;
            if (training && b < 20) {
                health = (int) Math.round(((double) (health * b)) / 20);
            } else {
                health = 440;
            }
        }

        @Override public String getName() {
            return "The Champ";
        }

        @Override public String toString(GameState state) { // can skip numOfDefensiveStance because of metallicize
            String s = super.toString(state);
            if (angered) {
                return s.subSequence(0, s.length() - 1) + ", angered}";
            } else {
                return s.subSequence(0, s.length() - 1) + ", untilTaunt=" + (4 - numTurns) + "}";
            }
        }

        @Override public boolean equals(Object o) { // can skip numOfDefensiveStance because of metallicize
            return super.equals(o) && angered == ((TheChamp) o).angered && numTurns == ((TheChamp) o).numTurns;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 5;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "5 input to keep track of whether The Champ has been angered and whether it's close to taunting";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = angered ? 0.5f : -0.5f;
            for (int i = 0; i < 4; i++) {
                input[idx + 1 + i] = 0f;
            }
            if (!angered) {
                input[idx + 1 + numTurns] = 0.5f;
            }
            return 5;
        }
    }

    // ******************************************************************************************
    // ******************************************************************************************
    // ******************************************************************************************

    public static class GremlinLeader extends Enemy {
        static final int ENCOURAGE = 0;
        static final int RALLY = 1;
        static final int STAB = 2;

        static {
            defaultProperty = new EnemyProperty(3, false);
            defaultProperty.isElite = true;
            defaultProperty.canGainStrength = true;
            defaultProperty.canGainBlock = true;
        }

        public GremlinLeader() {
            this(155);
        }

        public GremlinLeader(int health) {
            super(health);
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
                state.setIsStochastic();
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
                        if (!enemies.get(startIdx).isAlive()) {
                            idx = startIdx;
                        } else if (!enemies.get(startIdx + 1).isAlive()) {
                            idx = startIdx + 1;
                        }
                    } else if (r < 4) { // Sneaky Gremlin
                        idx = startIdx + 5;
                        if (!enemies.get(startIdx + 3).isAlive()) {
                            idx = startIdx + 3;
                        } else if (!enemies.get(startIdx + 4).isAlive()) {
                            idx = startIdx + 4;
                        }
                    } else if (r < 6) { // Fat Gremlin
                        idx = startIdx + 8;
                        if (!enemies.get(startIdx + 6).isAlive()) {
                            idx = startIdx + 6;
                        } else if (!enemies.get(startIdx + 7).isAlive()) {
                            idx = startIdx + 7;
                        }
                    } else if (r < 7) { // Shield Gremlin
                        idx = startIdx + 11;
                        if (!enemies.get(startIdx + 9).isAlive()) {
                            idx = startIdx + 9;
                        } else if (!enemies.get(startIdx + 10).isAlive()) {
                            idx = startIdx + 10;
                        }
                    } else { // Gremlin Wizard
                        idx = startIdx + 14;
                        if (!enemies.get(startIdx + 12).isAlive()) {
                            idx = startIdx + 12;
                        } else if (!enemies.get(startIdx + 13).isAlive()) {
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
                            System.out.println("!!!!! ENEMIES_ALIVE IS WRONG " + state.enemiesAlive + ": " + j);
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
            state.setIsStochastic();
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
                    enemies.get(i).property.canGainBlock = true;
                    enemies.get(i).property.canGainStrength = true;
                }
            }
        }
    }

    public static class BookOfStabbing extends Enemy {
        static final int MULTI_STAB = 0;
        static final int SINGLE_STAB = 1;

        static {
            defaultProperty = new EnemyProperty(2, true);
            defaultProperty.isElite = true;
        }

        private int stabCount;

        public BookOfStabbing() {
            this(172);
        }

        public BookOfStabbing(int health) {
            super(health);
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
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
            int newMove;
            if (r < 15) {
                if (move == SINGLE_STAB) {
                    newMove = MULTI_STAB;
                } else {
                    newMove = SINGLE_STAB;
                }
            } else if (move == MULTI_STAB && lastMove == MULTI_STAB) {
                newMove = SINGLE_STAB;
            } else {
                newMove = MULTI_STAB;
            }
            stabCount++;
            lastMove = move;
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

        static {
            defaultProperty = new EnemyProperty(1, false);
            defaultProperty.isElite = true;
            defaultProperty.canGainStrength = true;
        }

        public Taskmaster() {
            this(64);
        }

        public Taskmaster(int health) {
            super(health);
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

        static {
            defaultProperty = new EnemyProperty(5, true);
            defaultProperty.canGainStrength = true;
        }

        int flying = 4;

        public int getFlying() {
            return flying;
        }

        public Byrd() {
            this(33);
        }

        public Byrd(int health) {
            super(health);
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
                state.setIsStochastic();
                if (random.nextInt(200, RandomGenCtx.EnemyChooseMove) < 125) {
                    newMove = PECK;
                } else {
                    newMove = CAW;
                }
            } else {
                state.setIsStochastic();
                int n = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                if (n < 50) {
                    if (move == PECK && lastMove == PECK) {
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
            lastMove = move;
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

        static {
            defaultProperty = new EnemyProperty(4, false);
            defaultProperty.canFrail = true;
            defaultProperty.canGainBlock = true;
            defaultProperty.hasArtifact = true;
        }

        public SphericGuardian() {
            this(20);
        }

        public SphericGuardian(int health) {
            super(health);
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

    public static class ShelledParasite extends Enemy {
        static int DOUBLE_STRIKE = 0;
        static int SUCK = 1;
        static int FELL = 2;
        static int STUNNED = 3;

        static {
            defaultProperty = new EnemyProperty(4, true);
            defaultProperty.canFrail = true;
            defaultProperty.canGainBlock = true;
        }

        public ShelledParasite() {
            this(75);
        }

        public ShelledParasite(int health) {
            super(health);
            metallicize = 14;
        }

        public ShelledParasite(ShelledParasite other) {
            this(other.health);
            setSharedFields(other);
        }

        @Override public Enemy copy() {
            return new ShelledParasite(this);
        }

        @Override public void doMove(GameState state) {
            if (move == DOUBLE_STRIKE) {
                state.enemyDoDamageToPlayer(this, 7, 2);
            } else if (move == SUCK) {
                heal(state.enemyDoDamageToPlayer(this, 12, 1));
            } else if (move == FELL) {
                state.enemyDoDamageToPlayer(this, 21, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 2 + 1);
            } else if (move == STUNNED) {
                // do nothing
            }
        }

        @Override public void damage(int n, GameState state) {
            var dmg = Math.max(0, n - block);
            super.damage(n, state);
            if (dmg > 0) {
                metallicize -= 1;
                if (metallicize == 0) {
                    move = STUNNED;
                }
            }
        }

        private int nextMove(GameState state, RandomGen random, int num) {
            if (num < 20) {
                if (move != FELL) {
                    return move;
                } else {
                    return nextMove(state, random, random.nextInt(80, RandomGenCtx.EnemyChooseMove) + 20);
                }
            } else if (num < 60) {
                if (!(move == DOUBLE_STRIKE && lastMove == DOUBLE_STRIKE)) {
                    return DOUBLE_STRIKE;
                } else {
                    return SUCK;
                }
            } else if (!(move == SUCK && lastMove == SUCK)) {
                return SUCK;
            } else {
                return DOUBLE_STRIKE;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move < 0) {
                newMove = FELL;
            } else {
                state.setIsStochastic();
                newMove = nextMove(state, random, random.nextInt(100, RandomGenCtx.EnemyChooseMove));
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == DOUBLE_STRIKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "x2";
            } else if (move == SUCK) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12);
            } else if (move == FELL) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 21) + "+Frail 2";
            } else if (move == STUNNED) {
                return "Stunned";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training) {
            if (training) {
                health = (int) ((random.nextInt(7, RandomGenCtx.Other, null) + 1) / 7.0 * 75);
            }
        }

        public String getName() {
            return "Shelled Parasite";
        }
    }

    public static class Pointy extends Enemy {
        static int ATTACK = 0;

        static {
            defaultProperty = new EnemyProperty(1, false);
        }

        public Pointy() {
            this(34);
        }

        public Pointy(int health) {
            super(health);
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

        static {
            defaultProperty = new EnemyProperty(4, false);
            defaultProperty.canWeaken = true;
        }

        public Romeo() {
            this(41);
        }

        public Romeo(int health) {
            super(health);
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

        static {
            defaultProperty = new EnemyProperty(3, false);
            defaultProperty.changePlayerDexterity = true;
            defaultProperty.canGainBlock = true;
        }

        public Bear() {
            this(44);
        }

        public Bear(int health) {
            super(health);
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

        static {
            defaultProperty = new EnemyProperty(5, false);
            defaultProperty.canGainStrength = true;
            defaultProperty.canWeaken = true;
            defaultProperty.canVulnerable = true;
            defaultProperty.canDaze = true;
        }

        public Chosen() {
            this(103);
        }

        public Chosen(int health) {
            super(health);
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
                state.setIsStochastic();
                int r = random.nextInt(2, RandomGenCtx.EnemyChooseMove);
                if (r == 0) {
                    move = DRAIN;
                } else {
                    move = DEBILITATE;
                }
            } else if (move == DEBILITATE || move == DRAIN) {
                state.setIsStochastic();
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
                @Override public void handle(GameState state, Card card, int lastIdx, boolean cloned) {
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
