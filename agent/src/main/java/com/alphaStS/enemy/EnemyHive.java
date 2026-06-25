package com.alphaStS.enemy;

import com.alphaStS.GameProperties;
import com.alphaStS.GameState;
import com.alphaStS.card.Card;
import com.alphaStS.card.CardOther2;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventEnemyHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.random.RandomGen;
import com.alphaStS.random.RandomGenCtx;

import java.util.List;

import static com.alphaStS.enemy.EnemyUtils.nextFixedMove;
import static com.alphaStS.enemy.EnemyUtils.randomMove;
import static com.alphaStS.enemy.EnemyUtils.randomizeHealth;
import static com.alphaStS.enemy.EnemyUtils.reviveFirstDeadEnemy;

public class EnemyHive {
    public static class BowlbugRock extends Enemy {
        public static final int HEADBUTT = 0;
        public static final int DIZZY = 1;

        public BowlbugRock() {
            this(49);
        }

        public BowlbugRock(int health) {
            super(health, 2, false);
            properties.canBeStunned = true;
        }

        public BowlbugRock(BowlbugRock other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new BowlbugRock(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == HEADBUTT) {
                boolean playerHadBlock = state.getPlayerForRead().getBlock() > 0;
                int damageDealt = state.enemyDoDamageToPlayer(this, 16, 1);
                // TODO: confirm Imbalanced edge cases with replay logs.
                if (playerHadBlock && damageDealt == 0) {
                    lastMove = HEADBUTT;
                    move = DIZZY;
                }
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == DIZZY && lastMove == HEADBUTT) {
                lastMove = DIZZY;
            } else {
                lastMove = move;
                move = HEADBUTT;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == HEADBUTT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 16);
            } else if (move == DIZZY) {
                return "Stunned";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 46, 49);
        }

        @Override public String getName() {
            return "Bowlbug (Rock)";
        }
    }

    public static class BowlbugEgg extends Enemy {
        public static final int BITE = 0;

        public BowlbugEgg() {
            this(24);
        }

        public BowlbugEgg(int health) {
            super(health, 1, false);
            properties.canGainBlock = true;
        }

        public BowlbugEgg(BowlbugEgg other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new BowlbugEgg(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            state.enemyDoDamageToPlayer(this, 8, 1);
            gainBlock(8);
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = BITE;
        }

        @Override public String getMoveString(GameState state, int move) {
            return move == BITE ? "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "+8 Block" : "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 23, 24);
        }

        @Override public String getName() {
            return "Bowlbug (Egg)";
        }
    }

    public static class BowlbugSilk extends Enemy {
        public static final int THRASH = 0;
        public static final int SPIN_WEB = 1;

        public BowlbugSilk() {
            this(44);
        }

        public BowlbugSilk(int health) {
            super(health, 2, false);
            properties.entityProperty.changePlayerWeakened = true;
        }

        public BowlbugSilk(BowlbugSilk other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new BowlbugSilk(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == THRASH) {
                state.enemyDoDamageToPlayer(this, 5, 2);
            } else if (move == SPIN_WEB) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = move == SPIN_WEB ? THRASH : SPIN_WEB;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == THRASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 5) + "x2";
            } else if (move == SPIN_WEB) {
                return "Weak 1";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 41, 44);
        }

        @Override public String getName() {
            return "Bowlbug (Silk)";
        }
    }

    public static class BowlbugNectar extends Enemy {
        public static final int THRASH = 0;
        public static final int BUFF = 1;

        private boolean usedBuff;

        public BowlbugNectar() {
            this(39);
        }

        public BowlbugNectar(int health) {
            super(health, 2, false);
            properties.canGainStrength = true;
        }

        public BowlbugNectar(BowlbugNectar other) {
            super(other);
            usedBuff = other.usedBuff;
        }

        @Override public Enemy copy() {
            return new BowlbugNectar(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == THRASH) {
                state.enemyDoDamageToPlayer(this, 3, 1);
            } else if (move == BUFF) {
                gainStrength(16);
                usedBuff = true;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = move < 0 || usedBuff ? THRASH : BUFF;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == THRASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 3);
            } else if (move == BUFF) {
                return "Gain 16 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 36, 39);
        }

        @Override public String getName() {
            return "Bowlbug (Nectar)";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && usedBuff == ((BowlbugNectar) o).usedBuff;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Bowlbug (Nectar) buff";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = usedBuff ? 0.5f : 0.0f;
            return 1;
        }
    }

    public static class Chomper extends Enemy {
        public static final int CLAMP = 0;
        public static final int SCREECH = 1;

        private final int openingMove;

        public Chomper() {
            this(67, CLAMP);
        }

        public Chomper(int openingMove) {
            this(67, openingMove);
        }

        public Chomper(int health, int openingMove) {
            super(health, 2, false);
            this.openingMove = openingMove;
            artifact = 2;
            properties.hasArtifact = true;
        }

        public Chomper(Chomper other) {
            super(other);
            openingMove = other.openingMove;
        }

        @Override public Enemy copy() {
            return new Chomper(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CLAMP) {
                state.enemyDoDamageToPlayer(this, 9, 2);
            } else if (move == SCREECH) {
                state.addCardToDiscard(properties.generatedCardIdx);
                state.addCardToDiscard(properties.generatedCardIdx);
                state.addCardToDiscard(properties.generatedCardIdx);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = openingMove;
            } else {
                move = move == CLAMP ? SCREECH : CLAMP;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CLAMP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "x2";
            } else if (move == SCREECH) {
                return "Dazed 3";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 63, 67);
        }

        @Override public String getName() {
            return "Chomper";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.Dazed());
        }
    }

    public static class Decimillipede extends Enemy {
        public static final int BULK = 0;
        public static final int WRITHE = 1;
        public static final int OUTGAS = 2;
        public static final int REATTACH = 3;

        private final int openingMove;

        public Decimillipede() {
            this(52, BULK);
        }

        public Decimillipede(int openingMove) {
            this(52, openingMove);
        }

        public Decimillipede(int health, int openingMove) {
            super(health, 4, false);
            this.openingMove = openingMove;
            properties.isElite = true;
            properties.canGainStrength = true;
            properties.canSelfRevive = true;
            properties.entityProperty.changePlayerWeakened = true;
        }

        public Decimillipede(Decimillipede other) {
            super(other);
            openingMove = other.openingMove;
        }

        @Override public Enemy copy() {
            return new Decimillipede(this);
        }

        @Override public int damage(double n, GameState state) {
            int prevHealth = health;
            int dmg = super.damage(n, state);
            if (health <= 0 && prevHealth > 0) {
                move = REATTACH;
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int prevHealth = health;
            super.nonAttackDamage(n, blockable, state);
            if (health <= 0 && prevHealth > 0) {
                move = REATTACH;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BULK) {
                state.enemyDoDamageToPlayer(this, 7, 1);
                gainStrength(2);
            } else if (move == WRITHE) {
                state.enemyDoDamageToPlayer(this, 6, 2);
            } else if (move == OUTGAS) {
                state.enemyDoDamageToPlayer(this, 9, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move == REATTACH) {
                if (hasLivingSegment(state)) {
                    reviveReset();
                    health = 25;
                    state.adjustEnemiesAlive(1);
                    state.setIsStochastic();
                    move = random.nextInt(3, RandomGenCtx.EnemyChooseMove);
                }
            } else {
                move = move < 0 ? openingMove : (move + 1) % 3;
            }
        }

        private boolean hasLivingSegment(GameState state) {
            for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                EnemyReadOnly enemy = state.getEnemiesForRead().get(i);
                if (enemy instanceof Decimillipede && enemy.isAlive()) {
                    return true;
                }
            }
            return false;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BULK) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "+2 Strength";
            } else if (move == WRITHE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6) + "x2";
            } else if (move == OUTGAS) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "+Weak 1";
            } else if (move == REATTACH) {
                return "Reattach";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 46, 52);
        }

        @Override public String getName() {
            return "Decimillipede";
        }
    }

    public static class Entomancer extends Enemy {
        public static final int BEEEEES = 0;
        public static final int SPEAR = 1;
        public static final int PHEROMONE_SPIT = 2;

        private int personalHive = 1;

        public Entomancer() {
            this(155);
        }

        public Entomancer(int health) {
            super(health, 3, false);
            properties.isElite = true;
            properties.canGainStrength = true;
        }

        public Entomancer(Entomancer other) {
            super(other);
            personalHive = other.personalHive;
        }

        @Override public Enemy copy() {
            return new Entomancer(this);
        }

        @Override public int damage(double n, GameState state) {
            int dmg = super.damage(n, state);
            if (dmg > 0) {
                for (int i = 0; i < personalHive; i++) {
                    state.addCardToDeck(properties.generatedCardIdx);
                }
            }
            return dmg;
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BEEEEES) {
                state.enemyDoDamageToPlayer(this, 3, 8);
            } else if (move == SPEAR) {
                state.enemyDoDamageToPlayer(this, 20, 1);
            } else if (move == PHEROMONE_SPIT) {
                if (personalHive < 3) {
                    personalHive++;
                    gainStrength(1);
                } else {
                    gainStrength(2);
                }
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 3);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BEEEEES) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 3) + "x8";
            } else if (move == SPEAR) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 20);
            } else if (move == PHEROMONE_SPIT) {
                return personalHive < 3 ? "Gain 1 Personal Hive and 1 Strength" : "Gain 2 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 155, 155);
        }

        @Override public String getName() {
            return "Entomancer";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.Dazed());
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return s.subSequence(0, s.length() - 1) + ", personalHive=" + personalHive + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && personalHive == ((Entomancer) o).personalHive;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Entomancer Personal Hive";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = personalHive / 3.0f;
            return 1;
        }
    }

    public static class Exoskeleton extends Enemy {
        public static final int SKITTER = 0;
        public static final int MANDIBLES = 1;
        public static final int ENRAGE = 2;

        private final int openingMove;

        public Exoskeleton() {
            this(29, SKITTER);
        }

        public Exoskeleton(int openingMove) {
            this(29, openingMove);
        }

        public Exoskeleton(int health, int openingMove) {
            super(health, 3, false);
            this.openingMove = openingMove;
            properties.canGainStrength = true;
        }

        public Exoskeleton(Exoskeleton other) {
            super(other);
            openingMove = other.openingMove;
        }

        @Override public Enemy copy() {
            return new Exoskeleton(this);
        }

        @Override public int damage(double n, GameState state) {
            int prevHp = health;
            int dmg = super.damage(n, state);
            if (prevHp - health > 9) {
                setHealth(prevHp - 9);
                return 9;
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int prevHp = health;
            super.nonAttackDamage(n, blockable, state);
            if (prevHp - health > 9) {
                setHealth(prevHp - 9);
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SKITTER) {
                state.enemyDoDamageToPlayer(this, 1, 4);
            } else if (move == MANDIBLES) {
                state.enemyDoDamageToPlayer(this, 9, 1);
            } else if (move == ENRAGE) {
                gainStrength(2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                if (openingMove < 0) {
                    state.setIsStochastic();
                    move = random.nextBoolean(RandomGenCtx.EnemyChooseMove) ? SKITTER : MANDIBLES;
                } else {
                    move = openingMove;
                }
            } else if (move == MANDIBLES) {
                move = ENRAGE;
            } else {
                state.setIsStochastic();
                // TODO: check Exoskeleton move logic later against replay data.
                move = random.nextBoolean(RandomGenCtx.EnemyChooseMove) ? SKITTER : MANDIBLES;
                if (lastMove == SKITTER && move == SKITTER) {
                    move = MANDIBLES;
                }
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SKITTER) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 1) + "x4";
            } else if (move == MANDIBLES) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9);
            } else if (move == ENRAGE) {
                return "Gain 2 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 25, 29);
        }

        @Override public String getName() {
            return "Exoskeleton";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && openingMove == ((Exoskeleton) o).openingMove;
        }
    }

    public static class HunterKiller extends Enemy {
        public static final int TENDERIZING_GOOP = 0;
        public static final int BITE = 1;
        public static final int PUNCTURE = 2;

        public HunterKiller() {
            this(126);
        }

        public HunterKiller(int health) {
            super(health, 3, false);
            properties.entityProperty.changePlayerStrength = true;
            properties.entityProperty.changePlayerDexterity = true;
        }

        public HunterKiller(HunterKiller other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new HunterKiller(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == TENDERIZING_GOOP) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.TENDER, 1);
            } else if (move == BITE) {
                state.enemyDoDamageToPlayer(this, 19, 1);
            } else if (move == PUNCTURE) {
                state.enemyDoDamageToPlayer(this, 8, 3);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = TENDERIZING_GOOP;
            } else {
                state.setIsStochastic();
                move = randomMove(random, move == BITE ? 0 : 1, 2) + BITE;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == TENDERIZING_GOOP) {
                return "Tender 1";
            } else if (move == BITE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 19);
            } else if (move == PUNCTURE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "x3";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 126, 126);
        }

        @Override public String getName() {
            return "Hunter Killer";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            registerTenderCounter(state);
        }
    }

    public static class InfestedPrism extends Enemy {
        public static final int JAB = 0;
        public static final int RADIATE = 1;
        public static final int WHIRLWIND = 2;
        public static final int PULSATE = 3;

        private int tainted;

        public InfestedPrism() {
            this(171);
        }

        public InfestedPrism(int health) {
            super(health, 4, false);
            properties.isElite = true;
            properties.canGainBlock = true;
        }

        public InfestedPrism(InfestedPrism other) {
            super(other);
            tainted = other.tainted;
        }

        @Override public Enemy copy() {
            return new InfestedPrism(this);
        }

        @Override public void react(GameState state, Card card) {
            if (card.cardType == Card.SKILL) {
                tainted += 3;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == JAB) {
                state.enemyDoDamageToPlayer(this, 17 + tainted, 1);
            } else if (move == RADIATE) {
                state.enemyDoDamageToPlayer(this, 13 + tainted, 1);
                gainBlock(13);
            } else if (move == WHIRLWIND) {
                state.enemyDoDamageToPlayer(this, 6 + tainted, 3);
            } else if (move == PULSATE) {
                state.enemyDoDamageToPlayer(this, 10 + tainted, 1);
                gainBlock(22);
                tainted += 3;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 4);
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            tainted = 0;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == JAB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 17 + tainted);
            } else if (move == RADIATE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 13 + tainted) + "+13 Block";
            } else if (move == WHIRLWIND) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6 + tainted) + "x3";
            } else if (move == PULSATE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10 + tainted) + "+22 Block+Tainted 3";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 171, 171);
        }

        @Override public String getName() {
            return "Infested Prism";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return tainted == 0 ? s : s.subSequence(0, s.length() - 1) + ", tainted=" + tainted + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && tainted == ((InfestedPrism) o).tainted;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Infested Prism Tainted";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = tainted / 20.0f;
            return 1;
        }
    }

    public static class KnowledgeDemon extends Enemy {
        public static final int CURSE_OF_KNOWLEDGE = 0;
        public static final int SLAP = 1;
        public static final int KNOWLEDGE_OVERWHELMING = 2;
        public static final int PONDER = 3;

        private int curseCount;

        public KnowledgeDemon() {
            this(399);
        }

        public KnowledgeDemon(int health) {
            super(health, 4, false);
            properties.isBoss = true;
            properties.canGainStrength = true;
            properties.canHeal = true;
        }

        public KnowledgeDemon(KnowledgeDemon other) {
            super(other);
            curseCount = other.curseCount;
        }

        @Override public Enemy copy() {
            return new KnowledgeDemon(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CURSE_OF_KNOWLEDGE) {
                // TODO: Implement choosing one of two status cards for each Knowledge Demon set.
                state.addCardToDiscard(properties.generatedCardIdx);
                curseCount++;
            } else if (move == SLAP) {
                state.enemyDoDamageToPlayer(this, 18, 1);
            } else if (move == KNOWLEDGE_OVERWHELMING) {
                state.enemyDoDamageToPlayer(this, 9, 3);
            } else if (move == PONDER) {
                state.enemyDoDamageToPlayer(this, 13, 1);
                heal(30);
                gainStrength(3);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = curseCount < 3 ? CURSE_OF_KNOWLEDGE : SLAP;
            } else if (curseCount >= 3 && move == PONDER) {
                move = SLAP;
            } else {
                move = (move + 1) % 4;
                if (curseCount >= 3 && move == CURSE_OF_KNOWLEDGE) {
                    move = SLAP;
                }
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CURSE_OF_KNOWLEDGE) {
                return "Curse of Knowledge";
            } else if (move == SLAP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 18);
            } else if (move == KNOWLEDGE_OVERWHELMING) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "x3";
            } else if (move == PONDER) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 13) + "+Heal 30+Strength 3";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 399, 399);
        }

        @Override public String getName() {
            return "Knowledge Demon";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.Disintegration());
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return curseCount == 0 ? s : s.subSequence(0, s.length() - 1) + ", curseCount=" + curseCount + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && curseCount == ((KnowledgeDemon) o).curseCount;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Knowledge Demon curse sets";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = curseCount / 3.0f;
            return 1;
        }
    }

    public static class LouseProgenitor extends Enemy {
        public static final int WEB_CANNON = 0;
        public static final int CURL_AND_GROW = 1;
        public static final int POUNCE = 2;

        private boolean hasCurledUp;
        private boolean tookAttackDamage;

        public LouseProgenitor() {
            this(141);
        }

        public LouseProgenitor(int health) {
            super(health, 3, false);
            properties.canGainBlock = true;
            properties.canGainStrength = true;
            properties.entityProperty.changePlayerFrailed = true;
        }

        public LouseProgenitor(LouseProgenitor other) {
            super(other);
            hasCurledUp = other.hasCurledUp;
            tookAttackDamage = other.tookAttackDamage;
        }

        @Override public Enemy copy() {
            return new LouseProgenitor(this);
        }

        @Override public int damage(double n, GameState state) {
            tookAttackDamage = true;
            return super.damage(n, state);
        }

        @Override public void react(GameState state, Card card) {
            if (tookAttackDamage && !hasCurledUp) {
                hasCurledUp = true;
                gainBlock(18);
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == WEB_CANNON) {
                state.enemyDoDamageToPlayer(this, 10, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 2);
            } else if (move == CURL_AND_GROW) {
                gainBlock(18);
                gainStrength(5);
            } else if (move == POUNCE) {
                state.enemyDoDamageToPlayer(this, 16, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 3);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == WEB_CANNON) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10) + "+Frail 2";
            } else if (move == CURL_AND_GROW) {
                return "Gain 18 Block and 5 Strength";
            } else if (move == POUNCE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 16);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 138, 141);
        }

        @Override public String getName() {
            return "Louse Progenitor";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && hasCurledUp == ((LouseProgenitor) o).hasCurledUp;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Louse Progenitor Curl Up";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = hasCurledUp ? 0.5f : 0.0f;
            return 1;
        }
    }

    public static class Myte extends Enemy {
        public static final int TOXIC_CORNUCOPIA = 0;
        public static final int BITE = 1;
        public static final int SUCK = 2;

        private final int openingMove;

        public Myte() {
            this(69, TOXIC_CORNUCOPIA);
        }

        public Myte(int openingMove) {
            this(69, openingMove);
        }

        public Myte(int health, int openingMove) {
            super(health, 3, false);
            this.openingMove = openingMove;
            properties.canGainStrength = true;
        }

        public Myte(Myte other) {
            super(other);
            openingMove = other.openingMove;
        }

        @Override public Enemy copy() {
            return new Myte(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == TOXIC_CORNUCOPIA) {
                state.addCardToHand(properties.generatedCardIdx);
                state.addCardToHand(properties.generatedCardIdx);
            } else if (move == BITE) {
                state.enemyDoDamageToPlayer(this, 15, 1);
            } else if (move == SUCK) {
                state.enemyDoDamageToPlayer(this, 6, 1);
                gainStrength(3);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = move < 0 ? openingMove : nextFixedMove(move, 3);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == TOXIC_CORNUCOPIA) {
                return "Toxic 2";
            } else if (move == BITE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 15);
            } else if (move == SUCK) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6) + "+Strength 3";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 64, 69);
        }

        @Override public String getName() {
            return "Myte";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.Toxic());
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && openingMove == ((Myte) o).openingMove;
        }
    }

    public static class Ovicopter extends Enemy {
        public static final int LAY_EGGS = 0;
        public static final int SMASH = 1;
        public static final int TENDERIZER = 2;
        public static final int NUTRITIONAL_PASTE = 3;

        public Ovicopter() {
            this(132);
        }

        public Ovicopter(int health) {
            super(health, 4, false);
            properties.canGainStrength = true;
            properties.entityProperty.changePlayerVulnerable = true;
        }

        public Ovicopter(Ovicopter other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Ovicopter(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == LAY_EGGS) {
                for (int i = 0; i < 3; i++) {
                    reviveFirstDeadEnemy(state, ToughEggOrHatchling.class);
                }
            } else if (move == SMASH) {
                state.enemyDoDamageToPlayer(this, 17, 1);
            } else if (move == TENDERIZER) {
                state.enemyDoDamageToPlayer(this, 8, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
            } else if (move == NUTRITIONAL_PASTE) {
                gainStrength(4);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = LAY_EGGS;
            } else if (move == LAY_EGGS || move == NUTRITIONAL_PASTE) {
                move = SMASH;
            } else if (move == SMASH) {
                move = TENDERIZER;
            } else if (move == TENDERIZER) {
                move = state.enemiesAlive <= 4 ? LAY_EGGS : NUTRITIONAL_PASTE;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == LAY_EGGS) {
                return "Summon 3 Tough Eggs";
            } else if (move == SMASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 17);
            } else if (move == TENDERIZER) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "+Vulnerable 2";
            } else if (move == NUTRITIONAL_PASTE) {
                return "Gain 4 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 126, 132);
        }

        @Override public String getName() {
            return "Ovicopter";
        }
    }

    public static class ToughEggOrHatchling extends Enemy {
        public static final int WAIT = 0;
        public static final int HATCH = 1;
        public static final int NIBBLE = 2;

        private int hatchTimer = 2;
        private boolean hatchling;

        public ToughEggOrHatchling() {
            this(19);
        }

        public ToughEggOrHatchling(int health) {
            super(health, 3, false);
            properties.isMinion = true;
        }

        public ToughEggOrHatchling(ToughEggOrHatchling other) {
            super(other);
            hatchTimer = other.hatchTimer;
            hatchling = other.hatchling;
        }

        @Override public Enemy copy() {
            return new ToughEggOrHatchling(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == HATCH && !hatchling) {
                hatchling = true;
                reviveReset();
                properties.isMinion = true;
                randomizeHealth(this, state, false, 20, 23);
                setMaxHealthInBattle(getHealth());
            } else if (move == NIBBLE) {
                state.enemyDoDamageToPlayer(this, 5, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (hatchling) {
                move = NIBBLE;
            } else if (move < 0) {
                move = WAIT;
            } else if (move == WAIT) {
                hatchTimer--;
                move = hatchTimer <= 0 ? HATCH : WAIT;
            } else if (hatchTimer <= 0) {
                move = HATCH;
            } else {
                move = WAIT;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == WAIT && !hatchling) {
                return "Hatch " + hatchTimer;
            } else if (move == HATCH && !hatchling) {
                return "Hatch";
            } else if (move == NIBBLE || hatchling) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 5);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            hatchling = false;
            hatchTimer = 2;
            randomizeHealth(this, state, training, 15, 19);
        }

        @Override public String getName() {
            return hatchling ? "Hatchling" : "Tough Egg";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return hatchling ? s : s.subSequence(0, s.length() - 1) + ", hatch=" + hatchTimer + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o)
                    && hatchTimer == ((ToughEggOrHatchling) o).hatchTimer
                    && hatchling == ((ToughEggOrHatchling) o).hatchling;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 2;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "2 inputs to keep track of Tough Egg hatch timer and Hatchling state";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = hatchTimer / 2.0f;
            input[idx + 1] = hatchling ? 0.5f : 0.0f;
            return 2;
        }
    }

    public static class SlumberingBeetle extends Enemy {
        public static final int SNORE_1 = 0;
        public static final int SNORE_2 = 1;
        public static final int SNORE_3 = 2;
        public static final int ROLL_OUT = 3;

        public SlumberingBeetle() {
            this(89);
        }

        public SlumberingBeetle(int health) {
            super(health, 4, false);
            properties.canGainStrength = true;
            properties.canGainPlating = true;
            plating = 18;
        }

        public SlumberingBeetle(SlumberingBeetle other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new SlumberingBeetle(this);
        }

        private boolean isAsleep() {
            return move == SNORE_1 || move == SNORE_2 || move == SNORE_3;
        }

        private void wakeUp() {
            plating = 0;
            move = ROLL_OUT;
        }

        private void advanceSlumber() {
            if (move == SNORE_1) {
                move = SNORE_2;
            } else if (move == SNORE_2) {
                move = SNORE_3;
            } else if (move == SNORE_3) {
                wakeUp();
            }
        }

        @Override public int damage(double n, GameState state) {
            int dmg = super.damage(n, state);
            if (isAsleep() && dmg > 0) {
                advanceSlumber();
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int dmg = blockable ? Math.max(0, n - block) : n;
            super.nonAttackDamage(n, blockable, state);
            if (isAsleep() && dmg > 0) {
                advanceSlumber();
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == ROLL_OUT) {
                state.enemyDoDamageToPlayer(this, 18, 1);
                gainStrength(2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = SNORE_1;
            } else if (isAsleep()) {
                advanceSlumber();
            } else {
                move = ROLL_OUT;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SNORE_1 || move == SNORE_2 || move == SNORE_3) {
                return "Asleep";
            } else if (move == ROLL_OUT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 18) + "+Strength 2";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 89, 89);
        }

        @Override public String getName() {
            return "Slumbering Beetle";
        }
    }

    public static class SpinyToad extends Enemy {
        public static final int PROTRUDING_SPIKES = 0;
        public static final int SPIKE_EXPLOSION = 1;
        public static final int TONGUE_LASH = 2;

        private int thorn;

        public SpinyToad() {
            this(124);
        }

        public SpinyToad(int health) {
            super(health, 3, false);
        }

        public SpinyToad(SpinyToad other) {
            super(other);
            thorn = other.thorn;
        }

        @Override public Enemy copy() {
            return new SpinyToad(this);
        }

        @Override public int damage(double n, GameState state) {
            int dmg = super.damage(n, state);
            if (dmg > 0 && thorn > 0) {
                state.doNonAttackDamageToPlayer(thorn, true, this);
            }
            return dmg;
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == PROTRUDING_SPIKES) {
                thorn += 5;
            } else if (move == SPIKE_EXPLOSION) {
                state.enemyDoDamageToPlayer(this, 25, 1);
                thorn = Math.max(0, thorn - 5);
            } else if (move == TONGUE_LASH) {
                state.enemyDoDamageToPlayer(this, 19, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 3);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == PROTRUDING_SPIKES) {
                return "Gain 5 Thorns";
            } else if (move == SPIKE_EXPLOSION) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 25) + "-Thorns 5";
            } else if (move == TONGUE_LASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 19);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 121, 124);
        }

        @Override public String getName() {
            return "Spiny Toad";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && thorn == ((SpinyToad) o).thorn;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Spiny Toad Thorns";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = thorn / 15.0f;
            return 1;
        }

        public int getThorn() {
            return thorn;
        }
    }

    public static class TheInsatiable extends Enemy {
        public static final int LIQUIFY_GROUND = 0;
        public static final int THRASH_1 = 1;
        public static final int LUNGING_BITE = 2;
        public static final int SALIVATE = 3;
        public static final int THRASH_2 = 4;

        public TheInsatiable() {
            this(341);
        }

        public TheInsatiable(int health) {
            super(health, 5, false);
            properties.isBoss = true;
            properties.canGainStrength = true;
        }

        public TheInsatiable(TheInsatiable other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new TheInsatiable(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == LIQUIFY_GROUND) {
                // TODO: Implement Sandpit 4 once the player Sandpit counter exists.
                for (int i = 0; i < 3; i++) {
                    state.addCardToDeck(properties.generatedCardIdx);
                    state.addCardToDiscard(properties.generatedCardIdx);
                }
            } else if (move == THRASH_1 || move == THRASH_2) {
                state.enemyDoDamageToPlayer(this, 9, 2);
            } else if (move == LUNGING_BITE) {
                state.enemyDoDamageToPlayer(this, 31, 1);
            } else if (move == SALIVATE) {
                gainStrength(3);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 5);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == LIQUIFY_GROUND) {
                return "Sandpit 4+Frantic Escape 6";
            } else if (move == THRASH_1 || move == THRASH_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "x2";
            } else if (move == LUNGING_BITE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 31);
            } else if (move == SALIVATE) {
                return "Gain 3 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 341, 341);
        }

        @Override public String getName() {
            return "The Insatiable";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.FranticEscape());
        }
    }

    public static class TheObscura extends Enemy {
        public static final int ILLUSION = 0;
        public static final int PIERCING_GAZE = 1;
        public static final int WAIL = 2;
        public static final int HARDENING_STRIKE = 3;

        public TheObscura() {
            this(129);
        }

        public TheObscura(int health) {
            super(health, 4, false);
            properties.canGainBlock = true;
        }

        public TheObscura(TheObscura other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new TheObscura(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == ILLUSION) {
                reviveFirstDeadEnemy(state, Parafright.class);
            } else if (move == PIERCING_GAZE) {
                state.enemyDoDamageToPlayer(this, 11, 1);
            } else if (move == WAIL) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemy.gainStrength(3);
                }
            } else if (move == HARDENING_STRIKE) {
                state.enemyDoDamageToPlayer(this, 7, 1);
                gainBlock(7);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = ILLUSION;
            } else {
                state.setIsStochastic();
                move = randomMove(random, move == PIERCING_GAZE ? 0 : 1, move == WAIL ? 0 : 1,
                        move == HARDENING_STRIKE ? 0 : 1) + PIERCING_GAZE;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == ILLUSION) {
                return "Summon Parafright";
            } else if (move == PIERCING_GAZE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11);
            } else if (move == WAIL) {
                return "All Enemies Strength 3";
            } else if (move == HARDENING_STRIKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "+7 Block";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 129, 129);
        }

        @Override public String getName() {
            return "The Obscura";
        }
    }

    public static class Parafright extends Enemy {
        public static final int SLAM = 0;
        public static final int REVIVE = 1;

        public Parafright() {
            this(21);
        }

        public Parafright(int health) {
            super(health, 2, false);
            properties.canSelfRevive = true;
        }

        public Parafright(Parafright other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Parafright(this);
        }

        @Override public int damage(double n, GameState state) {
            int prevHealth = health;
            int dmg = super.damage(n, state);
            if (health <= 0 && prevHealth > 0) {
                move = REVIVE;
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int prevHealth = health;
            super.nonAttackDamage(n, blockable, state);
            if (health <= 0 && prevHealth > 0) {
                move = REVIVE;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SLAM) {
                state.enemyDoDamageToPlayer(this, 17, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move == REVIVE) {
                reviveReset();
                health = getMaxHealthInBattle();
                state.adjustEnemiesAlive(1);
                move = SLAM;
            } else {
                move = SLAM;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SLAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 17);
            } else if (move == REVIVE) {
                return "Revive";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 21, 21);
        }

        @Override public String getName() {
            return "Parafright";
        }
    }

    public static class ThievingHopper extends Enemy {
        public static final int THIEVERY = 0;
        public static final int FLUTTER = 1;
        public static final int HAT_TRICK = 2;
        public static final int NAB = 3;
        public static final int ESCAPE = 4;
        public static final int STUNNED = 5;

        private boolean flutter;
        private int flutterHits;
        private int stolenCards;

        public ThievingHopper() {
            this(84);
        }

        public ThievingHopper(int health) {
            super(health, 6, false);
            properties.canBeStunned = true;
        }

        public ThievingHopper(ThievingHopper other) {
            super(other);
            flutter = other.flutter;
            flutterHits = other.flutterHits;
            stolenCards = other.stolenCards;
        }

        @Override public Enemy copy() {
            return new ThievingHopper(this);
        }

        @Override public int damage(double n, GameState state) {
            if (flutter) {
                n *= 0.5;
                flutterHits++;
                if (flutterHits >= 5) {
                    flutter = false;
                    flutterHits = 0;
                    move = STUNNED;
                }
            }
            return super.damage(n, state);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == THIEVERY) {
                state.enemyDoDamageToPlayer(this, 19, 1);
                // TODO: Implement exact player deck card selection, theft tracking, and return-on-kill.
                if (state.deckArrLen > 0) {
                    state.removeCardFromDeck(state.getDeckArrForRead()[state.deckArrLen - 1], false);
                    stolenCards++;
                }
            } else if (move == FLUTTER) {
                flutter = true;
                flutterHits = 0;
            } else if (move == HAT_TRICK) {
                state.enemyDoDamageToPlayer(this, 23, 1);
            } else if (move == NAB) {
                state.enemyDoDamageToPlayer(this, 16, 1);
            } else if (move == ESCAPE) {
                state.killEnemy(state.getEnemiesForRead().find(self), false);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move == STUNNED) {
                move = HAT_TRICK;
            } else {
                move = nextFixedMove(move, 5);
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == THIEVERY) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 19) + "+Steal Card";
            } else if (move == FLUTTER) {
                return "Flutter";
            } else if (move == HAT_TRICK) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 23);
            } else if (move == NAB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 16);
            } else if (move == ESCAPE) {
                return "Escape";
            } else if (move == STUNNED) {
                return "Stunned";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 84, 84);
        }

        @Override public String getName() {
            return "Thieving Hopper";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            if (!flutter && stolenCards == 0) {
                return s;
            }
            return s.subSequence(0, s.length() - 1) + ", flutter=" + flutter + ", stolenCards=" + stolenCards + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o)
                    && flutter == ((ThievingHopper) o).flutter
                    && flutterHits == ((ThievingHopper) o).flutterHits
                    && stolenCards == ((ThievingHopper) o).stolenCards;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 3;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "3 inputs to keep track of Thieving Hopper Flutter and stolen card count";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = flutter ? 0.5f : 0.0f;
            input[idx + 1] = flutterHits / 5.0f;
            input[idx + 2] = stolenCards / 5.0f;
            return 3;
        }
    }

    public static class Tunneler extends Enemy {
        public static final int BITE = 0;
        public static final int BURROW = 1;
        public static final int ATTACK_FROM_BELOW = 2;
        public static final int EMERGING_STRIKE = 3;

        private boolean burrowed;

        public Tunneler() {
            this(92);
        }

        public Tunneler(int health) {
            super(health, 4, false);
            properties.canGainBlock = true;
            properties.canBeStunned = true;
        }

        public Tunneler(Tunneler other) {
            super(other);
            burrowed = other.burrowed;
        }

        @Override public Enemy copy() {
            return new Tunneler(this);
        }

        @Override public void startTurn(GameState state) {
            int oldBlock = block;
            super.startTurn(state);
            if (burrowed) {
                block = Math.max(block, oldBlock);
            }
        }

        @Override public int damage(double n, GameState state) {
            int prevBlock = block;
            int dmg = super.damage(n, state);
            if (burrowed && prevBlock > 0 && block == 0 && health > 0) {
                burrowed = false;
                move = EMERGING_STRIKE;
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int prevBlock = block;
            super.nonAttackDamage(n, blockable, state);
            if (burrowed && prevBlock > 0 && block == 0 && health > 0) {
                burrowed = false;
                move = EMERGING_STRIKE;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BITE) {
                state.enemyDoDamageToPlayer(this, 15, 1);
            } else if (move == BURROW) {
                burrowed = true;
                gainBlock(37);
            } else if (move == ATTACK_FROM_BELOW) {
                state.enemyDoDamageToPlayer(this, 26, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0 || move == EMERGING_STRIKE) {
                move = BITE;
            } else if (burrowed) {
                move = ATTACK_FROM_BELOW;
            } else if (move == BITE) {
                move = BURROW;
            } else {
                move = ATTACK_FROM_BELOW;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BITE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 15);
            } else if (move == BURROW) {
                return "Burrowed+37 Block";
            } else if (move == ATTACK_FROM_BELOW) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 26);
            } else if (move == EMERGING_STRIKE) {
                return "Stunned";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 92, 92);
        }

        @Override public String getName() {
            return "Tunneler";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return burrowed ? s.subSequence(0, s.length() - 1) + ", burrowed=true}" : s;
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && burrowed == ((Tunneler) o).burrowed;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Tunneler Burrowed";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = burrowed ? 0.5f : 0.0f;
            return 1;
        }
    }

    public static class Crusher extends Enemy {
        public static final int THRASH = 0;
        public static final int ENLARGING_STRIKE = 1;
        public static final int BUG_STING = 2;
        public static final int ADAPT = 3;
        public static final int GUARDED_STRIKE = 4;

        public Crusher() {
            this(219);
        }

        public Crusher(int health) {
            super(health, 5, false);
            properties.isBoss = true;
            properties.canGainBlock = true;
            properties.canGainStrength = true;
            properties.entityProperty.changePlayerWeakened = true;
            properties.entityProperty.changePlayerFrailed = true;
        }

        public Crusher(Crusher other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Crusher(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == THRASH) {
                state.enemyDoDamageToPlayer(this, 14, 1);
            } else if (move == ENLARGING_STRIKE) {
                state.enemyDoDamageToPlayer(this, 4, 1);
            } else if (move == BUG_STING) {
                state.enemyDoDamageToPlayer(this, 7, 2);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 2);
            } else if (move == ADAPT) {
                gainStrength(3);
            } else if (move == GUARDED_STRIKE) {
                state.enemyDoDamageToPlayer(this, 14, 1);
                gainBlock(18);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 5);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == THRASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14);
            } else if (move == ENLARGING_STRIKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 4);
            } else if (move == BUG_STING) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "x2+Weak 2+Frail 2";
            } else if (move == ADAPT) {
                return "Gain 3 Strength";
            } else if (move == GUARDED_STRIKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14) + "+18 Block";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 219, 219);
        }

        @Override public String getName() {
            return "Crusher";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            registerKaiserCrabHandlers(state);
        }
    }

    public static class Rocket extends Enemy {
        public static final int TARGETING_RETICLE = 0;
        public static final int PRECISION_BEAM = 1;
        public static final int CHARGE_UP = 2;
        public static final int LASER = 3;
        public static final int RECHARGE = 4;

        public Rocket() {
            this(209);
        }

        public Rocket(int health) {
            super(health, 5, false);
            properties.isBoss = true;
            properties.canGainBlock = true;
            properties.canGainStrength = true;
        }

        public Rocket(Rocket other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Rocket(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == TARGETING_RETICLE) {
                state.enemyDoDamageToPlayer(this, 4, 1);
            } else if (move == PRECISION_BEAM) {
                state.enemyDoDamageToPlayer(this, 20, 1);
            } else if (move == CHARGE_UP) {
                gainStrength(3);
            } else if (move == LASER) {
                state.enemyDoDamageToPlayer(this, 35, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 5);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == TARGETING_RETICLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 4);
            } else if (move == PRECISION_BEAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 20);
            } else if (move == CHARGE_UP) {
                return "Gain 3 Strength";
            } else if (move == LASER) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 35);
            } else if (move == RECHARGE) {
                return "Recharge";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 209, 209);
        }

        @Override public String getName() {
            return "Rocket";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            registerKaiserCrabHandlers(state);
        }
    }

    private static void registerTenderCounter(GameState state) {
        state.properties.registerCounter("Tender", new GameProperties.CounterRegistrant() {
            @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
                gameProperties.tenderCounterIdx = idx;
            }

            @Override public int getCounterIdx(GameProperties gameProperties) {
                return gameProperties.tenderCounterIdx;
            }
        }, new GameProperties.NetworkInputHandler() {
            @Override public int addToInput(GameState state, float[] input, int idx) {
                input[idx] = state.getCounterForRead()[state.properties.tenderCounterIdx] / 5.0f;
                return idx + 1;
            }

            @Override public int getInputLenDelta() {
                return 1;
            }
        });
        state.properties.addOnCardPlayedHandler("Tender", new GameEventCardHandler() {
            @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                int tender = state.getCounterForRead()[state.properties.tenderCounterIdx];
                if (tender > 0) {
                    state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, tender);
                    state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_DEXTERITY_EOT, tender);
                }
            }
        });
    }

    private static void registerKaiserCrabHandlers(GameState state) {
        state.properties.registerSurroundedEnemiesFacingCounter("Crusher", "Rocket");
        state.properties.addStartOfBattleHandler("KaiserCrabFacing", new GameEventHandler() {
            @Override public void handle(GameState state) {
                for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                    if (state.getEnemiesForRead().get(i).isAlive() && state.getEnemiesForRead().get(i) instanceof Rocket) {
                        state.getCounterForWrite()[state.properties.surroundedEnemiesFacingCounterIdx] = 2;
                    }
                }
            }
        });
        state.properties.addOnEnemyDeathHandler("KaiserCrabRage", new GameEventEnemyHandler() {
            @Override public void handle(GameState state, EnemyReadOnly enemy) {
                if (!(enemy instanceof Crusher || enemy instanceof Rocket)) {
                    return;
                }
                for (Enemy other : state.getEnemiesForWrite().iterateOverAlive()) {
                    if (other instanceof Crusher || other instanceof Rocket) {
                        other.gainStrength(6);
                        other.gainBlock(99);
                    }
                }
                if (enemy instanceof Crusher) {
                    state.getCounterForWrite()[state.properties.surroundedEnemiesFacingCounterIdx] = 2;
                } else {
                    state.getCounterForWrite()[state.properties.surroundedEnemiesFacingCounterIdx] = 1;
                }
            }
        });
    }
}
