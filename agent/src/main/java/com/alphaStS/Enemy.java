package com.alphaStS;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

abstract class Enemy {
    int health;
    int maxHealth;
    int block;
    int strength;
    int vulnerable;
    int weak;
    int artifact;
    int numOfMoves;
    int move;
    int moveHistory[];
    boolean canVulnerable;
    boolean canWeaken;
    boolean canFrail;
    boolean canSlime;
    boolean canDaze;
    boolean canGainStrength;
    boolean canGainBlock;
    boolean canAffectPlayerStrength;
    boolean canAffectPlayerDexterity;
    boolean hasArtifact;

    public abstract void doMove(GameState state);
    public abstract void nextMove(Random random);
    public abstract Enemy copy();
    public abstract String getMoveString(GameState state);
    public abstract String getName();

    public Enemy(int health, int numOfMoves) {
        this.health = health;
        maxHealth = health;
        this.numOfMoves = numOfMoves;
        move = -1;
    }

    public void setSharedFields(Enemy other) {
        health = other.health;
        maxHealth = other.maxHealth;
        block = other.block;
        strength = other.strength;
        vulnerable = other.vulnerable;
        weak = other.weak;
        artifact = other.artifact;
        numOfMoves = other.numOfMoves;
        move = other.move;
        if (other.moveHistory != null) {
            for (int i = 0; i < other.moveHistory.length; i++) {
                moveHistory[i] = other.moveHistory[i];
            }
        }
    }

    void damage(int n, GameState state) {
        if (health <= 0) {
            return;
        }
        if (vulnerable > 0) {
            n = n + n / 2;
        }
        health -= Math.max(0, n - block);
        block = Math.max(0, block - n);
        if (health <= 0) {
            health = 0;
            state.enemiesAlive -= 1;
        }
    }

    void nonAttackDamage(int n, boolean blockable, GameState state) {
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

    void gainBlock(int n) {
        block += n;
        if (block > 999) {
            block = 999;
        }
    }

    public void startTurn() {}

    void endTurn() {
        if (vulnerable > 0) {
            vulnerable -= 1;
        }
        if (weak > 0) {
            weak -= 1;
        }
        block = 0;
    }

    public String toString(GameState state) {
        String str = this.getName() +  "{hp=" + health;
        if (strength > 0) {
            str += ", str=" + strength;
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
        return str + '}';
    }

    public void startOfGameSetup(Random random) {
    }

    public void react(Card card) {
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

    public void randomize(Random random, boolean training) {
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Enemy enemy = (Enemy) o;
        return health == enemy.health && maxHealth == enemy.maxHealth && block == enemy.block && strength == enemy.strength && vulnerable == enemy.vulnerable && weak == enemy.weak && artifact == enemy.artifact && move == enemy.move && Arrays.equals(moveHistory, enemy.moveHistory);
    }

    @Override public int hashCode() {
        int result = Objects.hash(health, maxHealth, block, strength, vulnerable, weak, artifact, move);
        result = 31 * result + Arrays.hashCode(moveHistory);
        return result;
    }

    // ******************************************************************************************
    // ******************************************************************************************
    // ******************************************************************************************

    public static class GremlinNob extends Enemy {
        int turn;

        public GremlinNob() {
            this(90);
        }

        public GremlinNob(int health) {
            super(health, 4);
            canVulnerable = true;
            canGainStrength = true;
        }

        public GremlinNob(GremlinNob other) {
            this(other.health);
            setSharedFields(other);
            turn = other.turn;
        }

        @Override public Enemy copy() {
            return new GremlinNob(this);
        }

        @Override public void doMove(GameState state) {
            if (move == 1) {
                state.enemyDoDamageToPlayer(this, 8);
                state.player.vulnerable += 3;
            } else if (move == 2 || move == 3) {
                state.enemyDoDamageToPlayer(this, 16);
            }
        }

        @Override public void nextMove(Random random) {
            if (turn == 0) {
                move = 0;
                turn = 1;
                return;
            }
            move = (turn - 1) % 3 + 1;
            turn += 1;
        }

        @Override public void react(Card card) {
            if (card.cardType == Card.SKILL && turn > 1) {
                strength += 3;
            }
        }

        public String getMoveString(GameState state) {
            if (move == 0) {
                return "Buff";
            } else if (move == 1) {
                return "Attack " + state.enemyCalcDamageToPlayer(this,8);
            } else if (move == 2 || move == 3) {
                return "Attack " + state.enemyCalcDamageToPlayer(this,16);
            }
            return "Unknown";
        }

        public void randomize(Random random, boolean training) {
            int b = random.nextInt(10) + 1;
            if (b < 10) {
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
            canGainBlock = true;
            canAffectPlayerStrength = true;
            canAffectPlayerDexterity = true;
        }

        public Lagavulin(Lagavulin other) {
            this(other.health);
            setSharedFields(other);
            tookDamage = other.tookDamage;
        }

        @Override public Enemy copy() {
            return new Lagavulin(this);
        }

        @Override void damage(int n, GameState state) {
            super.damage(n, state);
            if (!tookDamage && health < maxHealth) {
                tookDamage = true;
            }
        }

        @Override void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (!tookDamage && health < maxHealth) {
                tookDamage = true;
            }
        }

        @Override public void startTurn() {
            if (move <= WAIT_3) {
                gainBlock(8);
            }
        }

        @Override public void doMove(GameState state) {
            if (move == ATTACK_1 || move == ATTACK_2) {
                state.enemyDoDamageToPlayer(this, 20);
            } else if (move == SIPHON_SOUL) {
                state.player.gainStrength(-2);
                state.player.gainDexterity(-2);
            }
        }

        @Override public void nextMove(Random random) {
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

        public String getMoveString(GameState state) {
            if (move == WAIT_1 || move == WAIT_2 || move == WAIT_3) {
                return "Asleep";
            } else if (move == ATTACK_1 || move == ATTACK_2) {
                return "Attack " + state.player.calcDamage(20 + strength);
            } else if (move == SIPHON_SOUL) {
                return "-2 Strength+-2 Dexterity";
            }
            return "Unknown";
        }

        public void randomize(Random random, boolean training) {
            int b = random.nextInt(10) + 1;
            if (b < 10) {
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
        static int BEAM = 0;
        static int BOLT = 1;

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
                state.enemyDoDamageToPlayer(this, 10);
            } else if (move == BOLT) {
                state.addCardToDiscard(state.prop.dazedCardIdx);
                state.addCardToDiscard(state.prop.dazedCardIdx);
                state.addCardToDiscard(state.prop.dazedCardIdx);
            }
        }

        @Override public void nextMove(Random random) {
            if (move == -1) {
                move = startMove;
                return;
            }
            move = move == BEAM ? BOLT : BEAM;
        }

        public String getMoveString(GameState state) {
            if (move == BEAM) {
                return "Attack " + state.player.calcDamage(10 + strength);
            } else if (move == BOLT) {
                return "Shuffle 3 Dazes Into Deck";
            }
            return "Unknown";
        }

        public void randomize(Random random, boolean training) {
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
                int n = state.player.health / 12 + 1;
                for (int i = 0; i < 6; i++) {
                    state.enemyDoDamageToPlayer(this, n + strength);
                }
            } else if (move == SEAR_1 || move == SEAR_2 || move == SEAR_3) {
                state.enemyDoDamageToPlayer(this, 6 + strength);
            } else if (move == TACKLE_1 || move == TACKLE_2) {
                state.enemyDoDamageToPlayer(this, 6 + strength);
                state.enemyDoDamageToPlayer(this, 6 + strength);
            } else if (move == INFLAME_1) {
                strength += 3;
                gainBlock(12);;
            } else if (move == INFERNAL_1) {
                for (int i = 0; i < 6; i++) {
                    state.enemyDoDamageToPlayer(this, 3 + strength);
                }
            }
        }

        @Override public void nextMove(Random random) {
            if (move < 9) {
                move = move + 1;
            }
            if (move == 9) {
                afterFirstInfernal = true;
                move = SEAR_1;
            }
        }

        public String getMoveString(GameState state) {
            if (move == ACTIVATE) {
                return "Activate";
            } else if (move == DIVIDER) {
                int n = state.player.health / 12 + 1;
                return "Attack " + state.player.calcDamage(3 + strength) + "x6";
            } else if (move == SEAR_1 || move == SEAR_2 || move == SEAR_3) {
                return "Attack " + state.player.calcDamage(6 + strength);
            } else if (move == TACKLE_1 || move == TACKLE_2) {
                return "Attack " + state.player.calcDamage(6 + strength) + "x2";
            } else if (move == INFLAME_1) {
                return "Gain 3 Strength+12 Block";
            } else if (move == INFERNAL_1) {
                return "Attack " + state.player.calcDamage(3 + strength) + "x6";
            }
            return "Unknown";
        }

        public void randomize(Random random, boolean training) {
            int b = random.nextInt(25) + 1;
            health = 264 * b / 25;
        }

        public String getName() {
            return "Hexaghost";
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

        @Override void damage(int n, GameState state) {
            super.damage(n, state);
            if (health <= maxHealth / 2) {
                move = SPLIT;
            }
        }

        @Override void nonAttackDamage(int n, boolean blockable, GameState state) {
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
                state.enemyDoDamageToPlayer(this, 38);
            } else if (move == SPLIT) {
                for (Enemy enemy : state.enemies) {
                    if (enemy instanceof Enemy.LargeAcidSlime slime) {
                        enemy.health = health;
                        slime.splitMaxHealth = health;
                        state.enemiesAlive += 1;
                    } else if (enemy instanceof Enemy.LargeSpikeSlime slime) {
                        enemy.health = health;
                        slime.splitMaxHealth = health;
                        state.enemiesAlive += 1;
                    }
                }
                health = 0;
                state.enemiesAlive -= 1;
            }
        }

        @Override public void nextMove(Random random) {
            if (move == GOOP_SPRAY) {
                move = PREPARING;
            } else if (move == PREPARING) {
                move = SLAM;
            } else if (move < 0 || move == SLAM){
                move = GOOP_SPRAY;
            }
        }

        public String getMoveString(GameState state) {
            if (move == GOOP_SPRAY) {
                return "Slimed 5";
            } else if (move == PREPARING) {
                return "Preparing";
            } else if (move == SLAM) {
                return "Attack " + state.player.calcDamage(38 + strength);
            } else if (move == SPLIT) {
                return "Split";
            }
            return "Unknown";
        }

        public void randomize(Random random, boolean training) {
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

        private int splitMaxHealth;

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

        @Override void damage(int n, GameState state) {
            super.damage(n, state);
            if (health <= splitMaxHealth / 2) {
                move = SPLIT;
            }
        }

        @Override void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (health <= splitMaxHealth / 2) {
                move = SPLIT;
            }
        }

        @Override public void doMove(GameState state) {
            if (move == FLAME_TACKLE) {
                state.enemyDoDamageToPlayer(this, 18);
                state.addCardToDiscard(state.prop.slimeCardIdx);
                state.addCardToDiscard(state.prop.slimeCardIdx);
            } else if (move == LICK) {
                state.player.applyDebuff(DebuffType.FRAIL, 4);
            } else if (move == SPLIT) {
                for (Enemy enemy : state.enemies) {
                    if (enemy instanceof Enemy.MediumSpikeSlime) {
                        enemy.health = health;
                        state.enemiesAlive += 1;
                    }
                }
                health = 0;
                state.enemiesAlive -= 1;
            }
        }

        @Override public void nextMove(Random random) {
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

        public String getMoveString(GameState state) {
            if (move == FLAME_TACKLE) {
                return "Attack " + state.player.calcDamage(18 + strength) + "+Slimed 2";
            } else if (move == LICK) {
                return "Frail 3";
            } else if (move == SPLIT) {
                return "Split";
            }
            return "Unknown";
        }

        public void randomize(Random random, boolean training) {
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
                state.enemyDoDamageToPlayer(this, 10);
                state.addCardToDiscard(state.prop.slimeCardIdx);
            } else if (move == LICK) {
                state.player.applyDebuff(DebuffType.FRAIL, 2);
            }
        }

        @Override public void nextMove(Random random) {
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

        public String getMoveString(GameState state) {
            if (move == FLAME_TACKLE) {
                return "Attack " + state.player.calcDamage(10 + strength) + "+Slimed 1";
            } else if (move == LICK) {
                return "Frail 1";
            }
            return "Unknown";
        }

        public void randomize(Random random, boolean training) {
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
            state.enemyDoDamageToPlayer(this, 6);
        }

        @Override public void nextMove(Random random) {
            move = TACKLE;
        }

        public String getMoveString(GameState state) {
            if (move == TACKLE) {
                return "Attack " + state.player.calcDamage(6 + strength);
            }
            return "Unknown";
        }

        public void randomize(Random random, boolean training) {
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

        private int splitMaxHealth;

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

        @Override void damage(int n, GameState state) {
            super.damage(n, state);
            if (health <= splitMaxHealth / 2) {
                move = SPLIT;
            }
        }

        @Override void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (health <= splitMaxHealth / 2) {
                move = SPLIT;
            }
        }

        @Override public void doMove(GameState state) {
            if (move == CORROSIVE_SPIT) {
                state.enemyDoDamageToPlayer(this, 12);
                state.addCardToDiscard(state.prop.slimeCardIdx);
                state.addCardToDiscard(state.prop.slimeCardIdx);
            } else if (move == TACKLE) {
                state.enemyDoDamageToPlayer(this, 18);
            } else if (move == LICK) {
                state.player.applyDebuff(DebuffType.WEAK, 3);
            } else if (move == SPLIT) {
                for (Enemy enemy : state.enemies) {
                    if (enemy instanceof Enemy.MediumAcidSlime) {
                        enemy.health = health;
                        state.enemiesAlive += 1;
                    }
                }
                health = 0;
                state.enemiesAlive -= 1;
            }
        }

        @Override public void nextMove(Random random) {
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

        public String getMoveString(GameState state) {
            if (move == CORROSIVE_SPIT) {
                return "Attack " + state.player.calcDamage(12 + strength) + "+Slimed 2";
            } else if (move == TACKLE) {
                return "Attack " + state.player.calcDamage(18 + strength);
            } else if (move == LICK) {
                return "Weak 2";
            } else if (move == SPLIT) {
                return "Split";
            }
            return "Unknown";
        }

        public void randomize(Random random, boolean training) {
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
                state.enemyDoDamageToPlayer(this, 8);
                state.addCardToDiscard(state.prop.slimeCardIdx);
            } else if (move == TACKLE) {
                state.enemyDoDamageToPlayer(this, 12);
            } else if (move == LICK) {
                state.player.applyDebuff(DebuffType.WEAK, 2);
            }
        }

        @Override public void nextMove(Random random) {
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

        public String getMoveString(GameState state) {
            if (move == CORROSIVE_SPIT) {
                return "Attack " + state.player.calcDamage(8 + strength) + "+Slimed 1";
            } else if (move == TACKLE) {
                return "Attack " + state.player.calcDamage(12 + strength);
            } else if (move == LICK) {
                return "Weak 1";
            }
            return "Unknown";
        }

        public void randomize(Random random, boolean training) {
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
                state.enemyDoDamageToPlayer(this, 4);
            } else if (move == LICK) {
                state.player.applyDebuff(DebuffType.WEAK, 2);
            }
        }

        @Override public void nextMove(Random random) {
            if (move == -1 || move == TACKLE) {
                move = LICK;
            } else {
                move = TACKLE;
            }
        }

        public String getMoveString(GameState state) {
            if (move == TACKLE) {
                return "Attack " + state.player.calcDamage(4 + strength);
            } else if (move == LICK) {
                return "Weak 1";
            }
            return "Unknown";
        }

        public void randomize(Random random, boolean training) {
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
                state.enemyDoDamageToPlayer(this, 12);
            } else if (move == 1) {
                strength += 5;
                gainBlock(9);
            } else if (move == 2) {
                state.enemyDoDamageToPlayer(this, 7);
                gainBlock(5);
            }
        }

        @Override public void nextMove(Random random) {
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

        public String getMoveString(GameState state) {
            if (move == 0) {
                return "Attack " + state.player.calcDamage(12 + strength);
            } else if (move == 1) {
                return "Gain 5 Strength+Block 9";
            } else if (move == 2) {
                return "Attack " + state.player.calcDamage(7 + strength) + "+Block 5";
            }
            return "Unknown";
        }

        public void randomize(Random random, boolean training) {
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

        @Override public void startOfGameSetup(Random random) {
            d = 6 + random.nextInt(3);
            curlUpAmount = 9 + random.nextInt(4);
        }

        @Override public void doMove(GameState state) {
            if (move == BITE) {
                state.enemyDoDamageToPlayer(this, d);
            } else if (move == GROW) {
                strength += 4;
            }
        }

        @Override public void nextMove(Random random) {
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

        void damage(int n, GameState state) {
            tookAttackDamage = true;
            super.damage(n, state);
        }

        @Override public void react(Card card) {
            if (tookAttackDamage && !hasCurledUp) {
                hasCurledUp = true;
                gainBlock(curlUpAmount);
            }
        }

        public String getMoveString(GameState state) {
            if (move == BITE) {
                return "Attack " + state.player.calcDamage(d + strength);
            } else if (move == GROW) {
                return "Gain 4 Strength";
            }
            return "Unknown";
        }

        public void randomize(Random random, boolean training) {
            health = 11 + random.nextInt(6);
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

        @Override public void startOfGameSetup(Random random) {
            d = 6 + random.nextInt(3);
            curlUpAmount = 9 + random.nextInt(4);
        }

        @Override public void doMove(GameState state) {
            if (move == BITE) {
                state.enemyDoDamageToPlayer(this, d);
            } else if (move == SPIT_WEB) {
                state.player.applyDebuff(DebuffType.WEAK, 3);
            }
        }

        @Override public void nextMove(Random random) {
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

        void damage(int n, GameState state) {
            tookAttackDamage = true;
            super.damage(n, state);
        }

        @Override public void react(Card card) {
            if (tookAttackDamage && !hasCurledUp) {
                hasCurledUp = true;
                gainBlock(curlUpAmount);
            }
        }

        public String getMoveString(GameState state) {
            if (move == BITE) {
                return "Attack " + state.player.calcDamage(d + strength);
            } else if (move == SPIT_WEB) {
                return "Applies 2 Week";
            }
            return "Unknown";
        }

        public void randomize(Random random, boolean training) {
            health = 12 + random.nextInt(7);
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
                state.enemyDoDamageToPlayer(this, 6);
            } else if (move == GROW) {
                strength += 5;
            }
        }

        @Override public void nextMove(Random random) {
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

        void damage(int n, GameState state) {
            super.damage(n, state);
            if (!isDead && health <= 0) {
                isDead = true;
                state.player.applyDebuff(DebuffType.VULNERABLE, 3);
            }
        }

        void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (!isDead && health <= 0) {
                isDead = true;
                state.player.applyDebuff(DebuffType.VULNERABLE, 3);
            }
        }

        public String getMoveString(GameState state) {
            if (move == BITE) {
                return "Attack " + state.player.calcDamage(6 + strength);
            } else if (move == GROW) {
                return "Gain 5 Strength";
            }
            return "Unknown";
        }

        public void randomize(Random random, boolean training) {
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
                state.enemyDoDamageToPlayer(this, 11);
            } else if (move == LUNGE) {
                state.enemyDoDamageToPlayer(this, 14);
            } else if (move == SMOKE_BOMB) {
                state.enemyDoDamageToPlayer(this, 6);
            } else if (move == ESCAPE) { // simulate the pain of losing gold, todo: need to combine with mugger later
                state.enemyDoNonAttackDamageToPlayer(this, Math.min(30, state.player.health - 1), false, false);
                health = 0;
            }
        }

        @Override public void nextMove(Random random) {
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
        public String getMoveString(GameState state) {
            if (move == MUG_1 || move == MUG_2) {
                return "Attack " + state.player.calcDamage(11 + strength);
            } else if (move == LUNGE) {
                return "Attack " + state.player.calcDamage(14 + strength);
            } else if (move == SMOKE_BOMB) {
                return "Block 6";
            } else if (move == ESCAPE) {
                return "Escape";
            }
            return "Unknown";
        }

        public void randomize(Random random, boolean training) {
            health = 46 + random.nextInt(5);
        }

        public String getName() {
            return "Looter";
        }
    }
}
