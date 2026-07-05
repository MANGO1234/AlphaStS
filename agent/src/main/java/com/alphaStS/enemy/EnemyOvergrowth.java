package com.alphaStS.enemy;

import com.alphaStS.GameProperties;
import com.alphaStS.GameState;
import com.alphaStS.PlayerBuff;
import com.alphaStS.card.Card;
import com.alphaStS.card.CardOther2;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.random.RandomGen;
import com.alphaStS.random.RandomGenCtx;

import java.io.PrintStream;
import java.util.List;

import static com.alphaStS.enemy.EnemyUtils.*;

public class EnemyOvergrowth {
    public static class BygoneEffigy extends Enemy {
        public static final int SLEEP = 0;
        public static final int WAKE = 1;
        public static final int SLASHES = 2;

        private int slow;

        public BygoneEffigy() {
            this(132);
        }

        public BygoneEffigy(int health) {
            super(health, 3, false);
            properties.canGainStrength = true;
            properties.isElite = true;
        }

        public BygoneEffigy(BygoneEffigy other) {
            super(other);
            this.slow = other.slow;
        }

        @Override public Enemy copy() {
            return new BygoneEffigy(this);
        }

        @Override public int damage(double n, GameState state) {
            n = n * ((10 + slow) / 10.0);
            return super.damage(n, state);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == WAKE) {
                gainStrength(10);
            } else if (move == SLASHES) {
                state.enemyDoDamageToPlayer(this, 15, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = SLEEP;
            } else if (move == SLEEP) {
                move = WAKE;
            } else {
                move = SLASHES;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SLEEP) {
                return "Sleep";
            } else if (move == WAKE) {
                return "Gain 10 Strength";
            } else if (move == SLASHES) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 15);
            }
            return "Unknown";
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            slow = 0;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var idx = state.getEnemiesForRead().find(this);
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    ((BygoneEffigy) state.getEnemiesForWrite().getForWrite(idx)).slow++;
                }
            });
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 132, 132);
        }

        @Override public String getName() {
            return "Bygone Effigy";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            if (slow == 0) {
                return s;
            }
            return s.subSequence(0, s.length() - 1) + ", slow=" + slow + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && slow == ((BygoneEffigy) o).slow;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Bygone Effigy Slow";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = slow / 20.0f;
            return 1;
        }
    }

    public static class Byrdonis extends Enemy {
        public static final int SWOOP = 0;
        public static final int PECK = 1;

        public Byrdonis() {
            this(90);
        }

        public Byrdonis(int health) {
            super(health, 2, false);
            properties.canGainStrength = true;
            properties.isElite = true;
        }

        public Byrdonis(Byrdonis other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Byrdonis(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SWOOP) {
                state.enemyDoDamageToPlayer(this, 19, 1);
            } else if (move == PECK) {
                state.enemyDoDamageToPlayer(this, 4, 3);
            }
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            gainStrength(1);
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 2);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SWOOP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 19);
            } else if (move == PECK) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 4) + "x3";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 90, 90);
        }

        @Override public String getName() {
            return "Byrdonis";
        }
    }

    public static class CeremonialBeast extends Enemy {
        public static final int STAMP = 0;
        public static final int PLOW = 1;
        public static final int STUN = 2;
        public static final int BEAST_CRY = 3;
        public static final int STOMP = 4;
        public static final int CRUSH = 5;

        private boolean plowTriggered;

        public CeremonialBeast() {
            this(262);
        }

        public CeremonialBeast(int health) {
            super(health, 6, false);
            properties.canGainStrength = true;
            properties.isBoss = true;
        }

        public CeremonialBeast(CeremonialBeast other) {
            super(other);
            this.plowTriggered = other.plowTriggered;
        }

        @Override public Enemy copy() {
            return new CeremonialBeast(this);
        }

        private void checkPlow() {
            if (!plowTriggered && move != STAMP && health > 0 && health <= 160) {
                plowTriggered = true;
                strength = 0;
                move = STUN;
            }
        }

        @Override public int damage(double n, GameState state) {
            int damageDone = super.damage(n, state);
            if (damageDone > 0) {
                checkPlow();
            }
            return damageDone;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int previousHealth = health;
            super.nonAttackDamage(n, blockable, state);
            if (health < previousHealth) {
                checkPlow();
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == STAMP) {
                plowTriggered = false;
            } else if (move == PLOW) {
                state.enemyDoDamageToPlayer(this, 20, 1);
                gainStrength(2);
            } else if (move == BEAST_CRY) {
                // TODO: Ringing is not modeled by the shared debuff engine yet.
            } else if (move == STOMP) {
                state.enemyDoDamageToPlayer(this, 17, 1);
            } else if (move == CRUSH) {
                state.enemyDoDamageToPlayer(this, 19, 1);
                gainStrength(4);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = STAMP;
            } else if (!plowTriggered) {
                move = PLOW;
            } else if (move == STUN) {
                move = BEAST_CRY;
            } else {
                move = move == BEAST_CRY ? STOMP : move == STOMP ? CRUSH : BEAST_CRY;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == STAMP) {
                return "Gain Plow 160";
            } else if (move == PLOW) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 20) + "+Gain 2 Strength";
            } else if (move == STUN) {
                return "Stunned";
            } else if (move == BEAST_CRY) {
                return "Ringing 1";
            } else if (move == STOMP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 17);
            } else if (move == CRUSH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 19) + "+Gain 4 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 262, 262);
        }

        @Override public String getName() {
            return "Ceremonial Beast";
        }
    }

    public static class CubexConstruct extends Enemy {
        public static final int CHARGE_UP = 0;
        public static final int REPEATER_BLAST_1 = 1;
        public static final int REPEATER_BLAST_2 = 2;
        public static final int EXPEL_BLAST = 3;
        public static final int SUBMERGE = 4;

        public CubexConstruct() {
            this(70);
        }

        public CubexConstruct(int health) {
            super(health, 5, false);
            artifact = 1;
            block = 13;
            properties.hasArtifact = true;
            properties.canGainBlock = true;
            properties.canGainStrength = true;
        }

        public CubexConstruct(CubexConstruct other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new CubexConstruct(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CHARGE_UP) {
                gainStrength(2);
            } else if (move == REPEATER_BLAST_1 || move == REPEATER_BLAST_2) {
                state.enemyDoDamageToPlayer(this, 8, 1);
                gainStrength(2);
            } else if (move == EXPEL_BLAST) {
                state.enemyDoDamageToPlayer(this, 6, 2);
            } else if (move == SUBMERGE) {
                gainBlock(15);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = CHARGE_UP;
            } else if (move == CHARGE_UP || move == EXPEL_BLAST) {
                move = REPEATER_BLAST_1;
            } else if (move == REPEATER_BLAST_1) {
                move = REPEATER_BLAST_2;
            } else {
                move = EXPEL_BLAST;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CHARGE_UP) {
                return "Gain 2 Strength";
            } else if (move == REPEATER_BLAST_1 || move == REPEATER_BLAST_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "+Gain 2 Strength";
            } else if (move == EXPEL_BLAST) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6) + "x2";
            } else if (move == SUBMERGE) {
                return "Gain 15 Block";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 65, 70);
        }

        @Override public String getName() {
            return "Cubex Construct";
        }
    }

    public static class Flyconid extends Enemy {
        public static final int WEAKENING_SPORES = 0;
        public static final int FRAIL_SPORES = 1;
        public static final int SMASH = 2;

        public Flyconid() {
            this(53);
        }

        public Flyconid(int health) {
            super(health, 3, false);
            properties.entityProperty.changePlayerVulnerable = true;
            properties.entityProperty.changePlayerFrailed = true;
        }

        public Flyconid(Flyconid other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Flyconid(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == WEAKENING_SPORES) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
            } else if (move == FRAIL_SPORES) {
                state.enemyDoDamageToPlayer(this, 9, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 2);
            } else if (move == SMASH) {
                state.enemyDoDamageToPlayer(this, 12, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move < 0) {
                newMove = random.nextInt(3, RandomGenCtx.EnemyChooseMove) < 2 ? FRAIL_SPORES : SMASH;
            } else {
                do {
                    // TODO: check source code to see how it actually works
                    newMove = randomMove(random, 3, 2, 1);
                } while (newMove == move);
            }
            state.setIsStochastic();
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == WEAKENING_SPORES) {
                return "Vulnerable 2";
            } else if (move == FRAIL_SPORES) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "+Frail 2";
            } else if (move == SMASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 51, 53);
        }

        @Override public String getName() {
            return "Flyconid";
        }
    }

    public static class Fogmog extends Enemy {
        public static final int ILLUSORY_SPORES = 0;
        public static final int THWACK = 1;
        public static final int HEADBUTT = 2;

        private boolean randomPickNext;

        public Fogmog() {
            this(78);
        }

        public Fogmog(int health) {
            super(health, 3, false);
            properties.canGainStrength = true;
        }

        public Fogmog(Fogmog other) {
            super(other);
            this.randomPickNext = other.randomPickNext;
        }

        @Override public Enemy copy() {
            return new Fogmog(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == ILLUSORY_SPORES) {
                for (int i = 0; i < state.getEnemiesForWrite().size(); i++) {
                    if (state.getEnemiesForRead().get(i).getHealth() == 0 && state.getEnemiesForRead().get(i) instanceof EyeWithTeeth) {
                        state.reviveEnemy(i, false, -1);
                        state.getEnemiesForWrite().getForWrite(i).reviveReset();
                        break;
                    }
                }
            } else if (move == THWACK) {
                state.enemyDoDamageToPlayer(this, 9, 1);
                gainStrength(1);
            } else if (move == HEADBUTT) {
                state.enemyDoDamageToPlayer(this, 16, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move < 0) {
                newMove = ILLUSORY_SPORES;
            } else if (move == ILLUSORY_SPORES) {
                randomPickNext = true;
                newMove = THWACK;
            } else if (randomPickNext) {
                newMove = random.nextInt(100, RandomGenCtx.EnemyChooseMove) < 40 ? THWACK : HEADBUTT;
                randomPickNext = false;
                state.setIsStochastic();
            } else {
                newMove = move == THWACK ? HEADBUTT : THWACK;
                randomPickNext = move == HEADBUTT;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == ILLUSORY_SPORES) {
                return "Summon Eye With Teeth";
            } else if (move == THWACK) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "+Gain 1 Strength";
            } else if (move == HEADBUTT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 16);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 78, 78);
        }

        @Override public String getName() {
            return "Fogmog";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && randomPickNext == ((Fogmog) o).randomPickNext;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Fogmog move branching";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = randomPickNext ? 0.5f : 0.0f;
            return 1;
        }
    }

    // TODO: missing self revive
    public static class EyeWithTeeth extends Enemy {
        public static final int DISTRACT = 0;

        public EyeWithTeeth() {
            this(6);
        }

        public EyeWithTeeth(int health) {
            super(health, 1, false);
            this.health = 0;
        }

        public EyeWithTeeth(EyeWithTeeth other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new EyeWithTeeth(this);
        }

        @Override public boolean canSelfRevive(GameState state) {
            for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                EnemyReadOnly enemy = state.getEnemiesForRead().get(i);
                if (enemy instanceof Fogmog && enemy.isAlive()) {
                    return true;
                }
            }
            return false;
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == DISTRACT) {
                state.addCardToDiscard(properties.generatedCardIdx);
                state.addCardToDiscard(properties.generatedCardIdx);
                state.addCardToDiscard(properties.generatedCardIdx);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = DISTRACT;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == DISTRACT) {
                return "Dazed 3";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            health = 0;
        }

        @Override public String getName() {
            return "Eye With Teeth";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.Dazed());
        }
    }

    public static class FuzzyWurmCrawler extends Enemy {
        public static final int ACID_GOOP_1 = 0;
        public static final int INHALE = 1;
        public static final int ACID_GOOP_2 = 2;

        public FuzzyWurmCrawler() {
            this(59);
        }

        public FuzzyWurmCrawler(int health) {
            super(health, 3, false);
            properties.canGainStrength = true;
        }

        public FuzzyWurmCrawler(FuzzyWurmCrawler other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new FuzzyWurmCrawler(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == ACID_GOOP_1 || move == ACID_GOOP_2) {
                state.enemyDoDamageToPlayer(this, 6, 1);
            } else if (move == INHALE) {
                gainStrength(7);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 3);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == ACID_GOOP_1 || move == ACID_GOOP_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6);
            } else if (move == INHALE) {
                return "Gain 7 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 58, 59);
        }

        @Override public String getName() {
            return "Fuzzy Wurm Crawler";
        }
    }

    public static class Inklet extends Enemy {
        public static final int JAB = 0;
        public static final int WINDUP_PUNCH = 1;
        public static final int PIERCING_GAZE = 2;

        private final boolean middle;
        private int slippery = 1;

        public Inklet() {
            this(18, false);
        }

        public Inklet(int health, boolean middle) {
            super(health, 3, false);
            this.middle = middle;
        }

        public Inklet(Inklet other) {
            super(other);
            this.slippery = other.slippery;
            this.middle = other.middle;
        }

        @Override public Enemy copy() {
            return new Inklet(this);
        }

        @Override public int damage(double n, GameState state) {
            int previousHealth = health;
            int damageDone = super.damage(n, state);
            if (damageDone > 0 && slippery > 0) {
                health = previousHealth - 1;
                slippery--;
                return 1;
            }
            return damageDone;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int previousHealth = health;
            super.nonAttackDamage(n, blockable, state);
            if (health < previousHealth && slippery > 0) {
                health = previousHealth - 1;
                slippery--;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == JAB) {
                state.enemyDoDamageToPlayer(this, 4, 1);
            } else if (move == WINDUP_PUNCH) {
                state.enemyDoDamageToPlayer(this, 3, 3);
            } else if (move == PIERCING_GAZE) {
                state.enemyDoDamageToPlayer(this, 11, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move < 0) {
                if (middle) {
                    newMove = WINDUP_PUNCH;
                } else {
                    newMove = random.nextInt(100, RandomGenCtx.EnemyChooseMove) < 65 ? JAB : WINDUP_PUNCH;
                    state.setIsStochastic();
                }
            } else if (move == JAB) {
                newMove = random.nextBoolean(RandomGenCtx.EnemyChooseMove) ? PIERCING_GAZE : WINDUP_PUNCH;
                state.setIsStochastic();
            } else {
                newMove = JAB;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == JAB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 4);
            } else if (move == WINDUP_PUNCH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 3) + "x3";
            } else if (move == PIERCING_GAZE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 12, 18);
        }

        @Override public String getName() {
            return "Inklet";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return s.subSequence(0, s.length() - 1) + ", slippery=" + slippery + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && slippery == ((Inklet) o).slippery && middle == ((Inklet) o).middle;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Slippery";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = ((float) slippery) / 9.0f;
            return 1;
        }
    }

    public static class Mawler extends Enemy {
        public static final int RIP_AND_TEAR = 0;
        public static final int ROAR = 1;
        public static final int CLAW = 2;

        private boolean usedRoar;

        public Mawler() {
            this(76);
        }

        public Mawler(int health) {
            super(health, 3, false);
            properties.entityProperty.changePlayerVulnerable = true;
        }

        public Mawler(Mawler other) {
            super(other);
            this.usedRoar = other.usedRoar;
        }

        @Override public Enemy copy() {
            return new Mawler(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == RIP_AND_TEAR) {
                state.enemyDoDamageToPlayer(this, 16, 1);
            } else if (move == ROAR) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 3);
                usedRoar = true;
            } else if (move == CLAW) {
                state.enemyDoDamageToPlayer(this, 5, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move < 0) {
                newMove = CLAW;
            } else {
                do {
                    newMove = random.nextInt(3, RandomGenCtx.EnemyChooseMove);
                } while (newMove == move || (newMove == ROAR && usedRoar));
                state.setIsStochastic();
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == RIP_AND_TEAR) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 16);
            } else if (move == ROAR) {
                return "Vulnerable 3";
            } else if (move == CLAW) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 5) + "x2";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 76, 76);
        }

        @Override public String getName() {
            return "Mawler";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && usedRoar == ((Mawler) o).usedRoar;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of whether Mawler has Roared";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = usedRoar ? 0.5f : 0.0f;
            return 1;
        }
    }

    public static class Nibbit extends Enemy {
        public static final int BUTT = 0;
        public static final int HESITANT_SLICE = 1;
        public static final int HISS = 2;

        private final int openingMove;

        public Nibbit() {
            this(48, BUTT);
        }

        public Nibbit(int health, int openingMove) {
            super(health, 3, false);
            properties.canGainBlock = true;
            properties.canGainStrength = true;
            this.openingMove = openingMove;
        }

        public Nibbit(Nibbit other) {
            super(other);
            this.openingMove = other.openingMove;
        }

        @Override public Enemy copy() {
            return new Nibbit(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BUTT) {
                state.enemyDoDamageToPlayer(this, 13, 1);
            } else if (move == HESITANT_SLICE) {
                state.enemyDoDamageToPlayer(this, 7, 1);
                gainBlock(6);
            } else if (move == HISS) {
                gainStrength(3);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = openingMove;
            } else {
                move = (move + 1) % 3;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BUTT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 13);
            } else if (move == HESITANT_SLICE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "+Gain 6 Block";
            } else if (move == HISS) {
                return "Gain 3 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 44, 48);
        }

        @Override public String getName() {
            return "Nibbit";
        }
    }

    // TODO: Wrigglers
    public static class PhrogParasite extends Enemy {
        public static final int INFECT = 0;
        public static final int LASH = 1;

        public PhrogParasite() {
            this(68);
        }

        public PhrogParasite(int health) {
            super(health, 2, false);
            properties.isElite = true;
        }

        public PhrogParasite(PhrogParasite other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new PhrogParasite(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == INFECT) {
                state.addCardToDiscard(properties.generatedCardIdx);
                state.addCardToDiscard(properties.generatedCardIdx);
                state.addCardToDiscard(properties.generatedCardIdx);
            } else if (move == LASH) {
                state.enemyDoDamageToPlayer(this, 5, 4);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 2);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == INFECT) {
                return "Infection 3";
            } else if (move == LASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 5) + "x4";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 66, 68);
        }

        @Override public String getName() {
            return "Phrog Parasite";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.Infection());
        }
    }

    public static class AxeRaider extends Enemy {
        public static final int SWING_1 = 0;
        public static final int SWING_2 = 1;
        public static final int BIG_SWING = 2;

        public AxeRaider() {
            this(23);
        }

        public AxeRaider(int health) {
            super(health, 3, false);
            properties.canGainBlock = true;
        }

        public AxeRaider(AxeRaider other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new AxeRaider(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SWING_1 || move == SWING_2) {
                state.enemyDoDamageToPlayer(this, 6, 1);
                gainBlock(6);
            } else if (move == BIG_SWING) {
                state.enemyDoDamageToPlayer(this, 13, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 3);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SWING_1 || move == SWING_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6) + "+Gain 6 Block";
            } else if (move == BIG_SWING) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 13);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 21, 23);
        }

        @Override public String getName() {
            return "Axe Raider";
        }
    }

    public static class AssassinRaider extends Enemy {
        public static final int KILLSHOT = 0;

        public AssassinRaider() {
            this(24);
        }

        public AssassinRaider(int health) {
            super(health, 1, false);
        }

        public AssassinRaider(AssassinRaider other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new AssassinRaider(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == KILLSHOT) {
                state.enemyDoDamageToPlayer(this, 12, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = KILLSHOT;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == KILLSHOT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 19, 24);
        }

        @Override public String getName() {
            return "Assassin Raider";
        }
    }

    public static class BruteRaider extends Enemy {
        public static final int BEAT = 0;
        public static final int CLAP = 1;

        public BruteRaider() {
            this(34);
        }

        public BruteRaider(int health) {
            super(health, 2, false);
            properties.canGainStrength = true;
        }

        public BruteRaider(BruteRaider other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new BruteRaider(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BEAT) {
                state.enemyDoDamageToPlayer(this, 8, 1);
            } else if (move == CLAP) {
                gainStrength(3);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 2);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BEAT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8);
            } else if (move == CLAP) {
                return "Gain 3 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 31, 34);
        }

        @Override public String getName() {
            return "Brute Raider";
        }
    }

    public static class CrossbowRaider extends Enemy {
        public static final int RELOAD = 0;
        public static final int FIRE = 1;

        public CrossbowRaider() {
            this(22);
        }

        public CrossbowRaider(int health) {
            super(health, 2, false);
            properties.canGainBlock = true;
        }

        public CrossbowRaider(CrossbowRaider other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new CrossbowRaider(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == RELOAD) {
                gainBlock(3);
            } else if (move == FIRE) {
                state.enemyDoDamageToPlayer(this, 16, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 2);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == RELOAD) {
                return "Gain 3 Block";
            } else if (move == FIRE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 16);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 19, 22);
        }

        @Override public String getName() {
            return "Crossbow Raider";
        }
    }

    public static class TrackerRaider extends Enemy {
        public static final int TRACK = 0;
        public static final int UNLEASH_THE_HOUNDS = 1;

        public TrackerRaider() {
            this(26);
        }

        public TrackerRaider(int health) {
            super(health, 2, false);
            properties.entityProperty.changePlayerFrailed = true;
        }

        public TrackerRaider(TrackerRaider other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new TrackerRaider(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == TRACK) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 2);
            } else if (move == UNLEASH_THE_HOUNDS) {
                state.enemyDoDamageToPlayer(this, 1, 9);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = move < 0 ? TRACK : UNLEASH_THE_HOUNDS;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == TRACK) {
                return "Frail 2";
            } else if (move == UNLEASH_THE_HOUNDS) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 1) + "x9";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 22, 26);
        }

        @Override public String getName() {
            return "Tracker Raider";
        }
    }

    public static class ShrinkerBeetle extends Enemy {
        public static final int SHRINKER = 0;
        public static final int CHOMP = 1;
        public static final int STOMP = 2;

        public ShrinkerBeetle() {
            this(42);
        }

        public ShrinkerBeetle(int health) {
            super(health, 3, false);
        }

        public ShrinkerBeetle(ShrinkerBeetle other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new ShrinkerBeetle(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SHRINKER) {
                // TODO: Shrink is not modeled by the shared debuff engine yet.
            } else if (move == CHOMP) {
                state.enemyDoDamageToPlayer(this, 8, 1);
            } else if (move == STOMP) {
                state.enemyDoDamageToPlayer(this, 14, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = SHRINKER;
            } else if (move == SHRINKER || move == STOMP) {
                move = CHOMP;
            } else {
                move = STOMP;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SHRINKER) {
                return "Shrink";
            } else if (move == CHOMP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8);
            } else if (move == STOMP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 40, 42);
        }

        @Override public String getName() {
            return "Shrinker Beetle";
        }
    }

    public static class LeafSlimeS extends Enemy {
        public static final int TACKLE = 0;
        public static final int GOOP = 1;

        public LeafSlimeS() {
            this(16);
        }

        public LeafSlimeS(int health) {
            super(health, 2, false);
        }

        public LeafSlimeS(LeafSlimeS other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new LeafSlimeS(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == TACKLE) {
                state.enemyDoDamageToPlayer(this, 4, 1);
            } else if (move == GOOP) {
                state.addCardToDiscard(properties.generatedCardIdx);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move < 0) {
                newMove = random.nextBoolean(RandomGenCtx.EnemyChooseMove) ? TACKLE : GOOP;
                state.setIsStochastic();
            } else {
                newMove = move == TACKLE ? GOOP : TACKLE;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 4);
            } else if (move == GOOP) {
                return "Slimed 1";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 12, 16);
        }

        @Override public String getName() {
            return "Leaf Slime (S)";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.Slimed());
        }
    }

    public static class LeafSlimeM extends Enemy {
        public static final int CLUMP_SHOT = 0;
        public static final int STICKY_SHOT = 1;

        public LeafSlimeM() {
            this(36);
        }

        public LeafSlimeM(int health) {
            super(health, 2, false);
        }

        public LeafSlimeM(LeafSlimeM other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new LeafSlimeM(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CLUMP_SHOT) {
                state.enemyDoDamageToPlayer(this, 9, 1);
            } else if (move == STICKY_SHOT) {
                state.addCardToDiscard(properties.generatedCardIdx);
                state.addCardToDiscard(properties.generatedCardIdx);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = move < 0 || move == CLUMP_SHOT ? STICKY_SHOT : CLUMP_SHOT;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CLUMP_SHOT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9);
            } else if (move == STICKY_SHOT) {
                return "Slimed 2";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 33, 36);
        }

        @Override public String getName() {
            return "Leaf Slime (M)";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.Slimed());
        }
    }

    public static class TwigSlimeS extends Enemy {
        public static final int TACKLE = 0;

        public TwigSlimeS() {
            this(12);
        }

        public TwigSlimeS(int health) {
            super(health, 1, false);
        }

        public TwigSlimeS(TwigSlimeS other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new TwigSlimeS(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == TACKLE) {
                state.enemyDoDamageToPlayer(this, 5, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = TACKLE;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 5);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 8, 12);
        }

        @Override public String getName() {
            return "Twig Slime (S)";
        }
    }

    public static class TwigSlimeM extends Enemy {
        public static final int CHOMP = 0;
        public static final int STICKY_SHOT = 1;

        public TwigSlimeM() {
            this(29);
        }

        public TwigSlimeM(int health) {
            super(health, 2, false);
        }

        public TwigSlimeM(TwigSlimeM other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new TwigSlimeM(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CHOMP) {
                state.enemyDoDamageToPlayer(this, 12, 1);
            } else if (move == STICKY_SHOT) {
                state.addCardToDiscard(properties.generatedCardIdx);
            }
        }

        // TODO: check source code
        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move < 0) {
                newMove = STICKY_SHOT;
            } else if (move == STICKY_SHOT) {
                newMove = CHOMP;
            } else {
                newMove = random.nextInt(3, RandomGenCtx.EnemyChooseMove) < 2 ? CHOMP : STICKY_SHOT;
                state.setIsStochastic();
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CHOMP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12);
            } else if (move == STICKY_SHOT) {
                return "Slimed 1";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 27, 29);
        }

        @Override public String getName() {
            return "Twig Slime (M)";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.Slimed());
        }
    }

    public static class SlitheringStrangler extends Enemy {
        public static final int CONSTRICT = 0;
        public static final int THWACK = 1;
        public static final int LASH = 2;

        public SlitheringStrangler() {
            this(56);
        }

        public SlitheringStrangler(int health) {
            super(health, 3, false);
            properties.canGainBlock = true;
        }

        public SlitheringStrangler(SlitheringStrangler other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new SlitheringStrangler(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CONSTRICT) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.CONSTRICTED, 3);
            } else if (move == THWACK) {
                state.enemyDoDamageToPlayer(this, 8, 1);
                gainBlock(5);
            } else if (move == LASH) {
                state.enemyDoDamageToPlayer(this, 13, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move < 0 || move == THWACK || move == LASH) {
                newMove = CONSTRICT;
            } else {
                newMove = random.nextBoolean(RandomGenCtx.EnemyChooseMove) ? LASH : THWACK;
                state.setIsStochastic();
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CONSTRICT) {
                return "Apply 3 Constricted";
            } else if (move == THWACK) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "+Gain 5 Block";
            } else if (move == LASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 13);
            }
            return "Unknown";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Constricted", new GameProperties.CounterRegistrant() {
                @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
                    gameProperties.constrictedCounterIdx = idx;
                }

                @Override public int getCounterIdx(GameProperties gameProperties) {
                    return gameProperties.constrictedCounterIdx;
                }
            }, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    int counter = state.getCounterForRead()[state.properties.constrictedCounterIdx];
                    input[idx] = counter / 12.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addPreEndOfTurnHandler("Constricted", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[state.properties.constrictedCounterIdx] > 0) {
                        state.getPlayerForWrite().nonAttackDamage(state, state.getCounterForRead()[state.properties.constrictedCounterIdx], true);
                    }
                }
            });
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 54, 56);
        }

        @Override public String getName() {
            return "Slithering Strangler";
        }
    }

    public static class SnappingJaxfruit extends Enemy {
        public static final int ENERGY_ORB = 0;

        public SnappingJaxfruit() {
            this(36);
        }

        public SnappingJaxfruit(int health) {
            super(health, 1, false);
            properties.canGainStrength = true;
        }

        public SnappingJaxfruit(SnappingJaxfruit other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new SnappingJaxfruit(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == ENERGY_ORB) {
                state.enemyDoDamageToPlayer(this, 4, 1);
                gainStrength(2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = ENERGY_ORB;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == ENERGY_ORB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 4) + "+Gain 2 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 34, 36);
        }

        @Override public String getName() {
            return "Snapping Jaxfruit";
        }
    }

    public static class KinPriest extends Enemy {
        public static final int ORB_OF_FRAILTY = 0;
        public static final int ORB_OF_WEAKNESS = 1;
        public static final int SOUL_BEAM = 2;
        public static final int DARK_RITUAL = 3;

        public KinPriest() {
            this(199);
        }

        public KinPriest(int health) {
            super(health, 4, false);
            properties.entityProperty.changePlayerFrailed = true;
            properties.entityProperty.changePlayerWeakened = true;
            properties.canGainStrength = true;
            properties.isBoss = true;
        }

        public KinPriest(KinPriest other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new KinPriest(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == ORB_OF_FRAILTY) {
                state.enemyDoDamageToPlayer(this, 9, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 1);
            } else if (move == ORB_OF_WEAKNESS) {
                state.enemyDoDamageToPlayer(this, 9, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
            } else if (move == SOUL_BEAM) {
                state.enemyDoDamageToPlayer(this, 3, 3);
            } else if (move == DARK_RITUAL) {
                gainStrength(3);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 4);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == ORB_OF_FRAILTY) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "+Frail 1";
            } else if (move == ORB_OF_WEAKNESS) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "+Weak 1";
            } else if (move == SOUL_BEAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 3) + "x3";
            } else if (move == DARK_RITUAL) {
                return "Gain 3 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 199, 199);
        }

        @Override public String getName() {
            return "Kin Priest";
        }
    }

    public static class KinFollower extends Enemy {
        public static final int QUICK_SLASH = 0;
        public static final int BOOMERANG = 1;
        public static final int POWER_DANCE = 2;

        private final int openingMove;

        public KinFollower() {
            this(63, QUICK_SLASH);
        }

        public KinFollower(int health, int openingMove) {
            super(health, 3, false);
            properties.canGainStrength = true;
            properties.isBoss = true;
            properties.isMinion = true;
            this.openingMove = openingMove;
        }

        public KinFollower(KinFollower other) {
            super(other);
            this.openingMove = other.openingMove;
        }

        @Override public Enemy copy() {
            return new KinFollower(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == QUICK_SLASH) {
                state.enemyDoDamageToPlayer(this, 5, 1);
            } else if (move == BOOMERANG) {
                state.enemyDoDamageToPlayer(this, 2, 2);
            } else if (move == POWER_DANCE) {
                gainStrength(3);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = openingMove;
            } else {
                move = (move + 1) % 3;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == QUICK_SLASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 5);
            } else if (move == BOOMERANG) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 2) + "x2";
            } else if (move == POWER_DANCE) {
                return "Gain 3 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 62, 63);
        }

        @Override public String getName() {
            return "Kin Follower";
        }
    }

    public static class Vantom extends Enemy {
        public static final int INK_BLOT = 0;
        public static final int INKY_LANCE = 1;
        public static final int DISMEMBER = 2;
        public static final int PREPARE = 3;

        private int slippery = 9;

        public Vantom() {
            this(183);
        }

        public Vantom(int health) {
            super(health, 4, false);
            properties.canGainStrength = true;
            properties.isBoss = true;
        }

        public Vantom(Vantom other) {
            super(other);
            this.slippery = other.slippery;
        }

        @Override public Enemy copy() {
            return new Vantom(this);
        }

        @Override public void interactiveModePrint(GameState state, PrintStream out) {
            out.println("  Slippery " + slippery);
        }

        @Override public int damage(double n, GameState state) {
            int previousHealth = health;
            int damageDone = super.damage(n, state);
            if (damageDone > 0 && slippery > 0) {
                health = previousHealth - 1;
                slippery--;
                return 1;
            }
            return damageDone;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int previousHealth = health;
            super.nonAttackDamage(n, blockable, state);
            if (health < previousHealth && slippery > 0) {
                health = previousHealth - 1;
                slippery--;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == INK_BLOT) {
                state.enemyDoDamageToPlayer(this, 8, 1);
            } else if (move == INKY_LANCE) {
                state.enemyDoDamageToPlayer(this, 7, 2);
            } else if (move == DISMEMBER) {
                state.enemyDoDamageToPlayer(this, 30, 1);
                state.addCardToDiscard(properties.generatedCardIdx);
                state.addCardToDiscard(properties.generatedCardIdx);
                state.addCardToDiscard(properties.generatedCardIdx);
            } else if (move == PREPARE) {
                gainStrength(2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 4);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == INK_BLOT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8);
            } else if (move == INKY_LANCE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "x2";
            } else if (move == DISMEMBER) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 30) + "+Wound 3";
            } else if (move == PREPARE) {
                return "Gain 2 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 173, 183);
        }

        @Override public String getName() {
            return "Vantom";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return s.subSequence(0, s.length() - 1) + ", slippery=" + slippery + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && slippery == ((Vantom) o).slippery;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.Wound());
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Slippery";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = ((float) slippery) / 9.0f;
            return 1;
        }
    }

    public static class VineShambler extends Enemy {
        public static final int SWIPE = 0;
        public static final int GRASPING_VINES = 1;
        public static final int CHOMP = 2;

        public VineShambler() {
            this(64);
        }

        public VineShambler(int health) {
            super(health, 3, false);
            properties.entityProperty.addPossibleBuff(PlayerBuff.ENTANGLED);
        }

        public VineShambler(VineShambler other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new VineShambler(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SWIPE) {
                state.enemyDoDamageToPlayer(this, 7, 2);
            } else if (move == GRASPING_VINES) {
                state.enemyDoDamageToPlayer(this, 9, 1);
                // TODO: tangled should be like constricted, be a counter instead
                state.getPlayerForWrite().applyDebuff(state, DebuffType.ENTANGLED, 1);
            } else if (move == CHOMP) {
                state.enemyDoDamageToPlayer(this, 18, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 3);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SWIPE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "x2";
            } else if (move == GRASPING_VINES) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "+Entangled 1";
            } else if (move == CHOMP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 18);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 64, 64);
        }

        @Override public String getName() {
            return "Vine Shambler";
        }
    }
}
