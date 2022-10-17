package com.alphaStS.enemy;

import com.alphaStS.*;

import java.util.List;

public class EnemyBeyond {
    public static class AwakenedOne extends Enemy {
        private static final int SLASH = 0;
        private static final int SOUL_STRIKE = 1;
        private static final int REBIRTH = 2;
        private static final int DARK_ECHO = 3;
        private static final int SLUDGE = 4;
        private static final int TACKLE = 5;

        private boolean awakened;

        public AwakenedOne() {
            this(320);
        }

        public AwakenedOne(int health) {
            super(health, 6, true);
            property.canGainStrength = true;
            property.canSelfRevive = true;
            strength = 2;
        }

        public AwakenedOne(EnemyBeyond.AwakenedOne other) {
            super(other);
            awakened = other.awakened;
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.AwakenedOne(this);
        }

        @Override public void damage(int n, GameState state) {
            super.damage(n, state);
            if (health <= 0) {
                if (!awakened) {
                    if (!state.prop.hasRunicDome) {
                        move = REBIRTH;
                    }
                } else {
                    for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                        state.killEnemy(i);
                    }
                }
            }
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (health <= 0) {
                if (!awakened) {
                    if (!state.prop.hasRunicDome) {
                        move = REBIRTH;
                    }
                } else {
                    for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                        state.killEnemy(i);
                    }
                }
            }
        }

        @Override public void startTurn() {
            super.startTurn();
            heal(15);
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var idx = state.getEnemiesForRead().find(this);
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card, int lastIdx, boolean cloned) {
                    var enemy = state.getEnemiesForRead().get(idx);
                    if (card.cardType == Card.POWER && !((AwakenedOne) enemy).awakened && enemy.getMove() != REBIRTH) {
                        state.getEnemiesForWrite().getForWrite(idx).gainStrength(2);
                    }
                }
            });
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SLASH) {
                state.enemyDoDamageToPlayer(this, 20, 1);
            } else if (move == SOUL_STRIKE) {
                state.enemyDoDamageToPlayer(this, 6, 4);
            } else if (move == REBIRTH) {
                removeAllDebuffs();
                health = property.maxHealth;
                awakened = true;
            } else if (move == DARK_ECHO) {
                state.enemyDoDamageToPlayer(this, 40, 1);
            } else if (move == SLUDGE) {
                state.enemyDoDamageToPlayer(this, 18, 1);
                state.addCardToDeck(state.prop.voidCardIdx);
            } else if (move == TACKLE) {
                state.enemyDoDamageToPlayer(this, 10, 3);
            }
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            return List.of(new Card.Void());
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (health == 0) {
                newMove = REBIRTH;
            } else if (move == -1) {
                newMove = SLASH;
            } else if (!awakened) {
                state.setIsStochastic();
                int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                if (r < 25) {
                    if (move != SOUL_STRIKE) {
                        newMove = SOUL_STRIKE;
                    } else {
                        newMove = SLASH;
                    }
                } else if (move != SLASH || lastMove != SLASH) {
                    newMove = SLASH;
                } else {
                    newMove = SOUL_STRIKE;
                }
            } else {
                if (move == REBIRTH) {
                    newMove = DARK_ECHO;
                } else {
                    state.setIsStochastic();
                    int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                    if (r < 50) {
                        if (move != TACKLE || lastMove != TACKLE) {
                            newMove = TACKLE;
                        } else {
                            newMove = SLUDGE;
                        }
                    } else {
                        if (move != SLUDGE || lastMove != SLUDGE) {
                            newMove = SLUDGE;
                        } else {
                            newMove = TACKLE;
                        }
                    }
                }
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SLASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 20);
            } else if (move == SOUL_STRIKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6) + "x4";
            } else if (move == REBIRTH) {
                return "Remove All Debuffs";
            } else if (move == DARK_ECHO) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 40);
            } else if (move == SLUDGE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 18) + "+Void 1";
            } else if (move == TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10) + "x3";
            }
            return "Unknown";
        }

        @Override public boolean isAlive() {
            return !awakened || health > 0;
        }

        @Override public boolean isTargetable() {
            return health > 0 && move != REBIRTH;
        }

        @Override public void randomize(RandomGen random, boolean training) {
            int b = random.nextInt(32, RandomGenCtx.Other) + 1;
            if (training) {
                if (b < 16) {
                    awakened = true;
                    health = (int) Math.round(((double) (health * b)) / 16);
                } else {
                    health = (int) Math.round(((double) (health * (b - 16))) / 20);
                }
            } else {
                health = 320;
            }
        }

        @Override public String getName() {
            return "Awakened One";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && awakened == ((EnemyBeyond.AwakenedOne) o).awakened;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 2;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of whether Awakened One has awakened";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = awakened ? 0.5f : 0;
            input[idx + 1] = (awakened ? health : (320 + health)) / 640f;
            return 2;
        }
    }

    // ******************************************************************************************
    // ******************************************************************************************
    // ******************************************************************************************

    public static class GiantHead extends Enemy {
        private static final int COUNT = 0;
        private static final int GLARE = 1;
        public static final int IT_IS_TIME = 2;

        private int slow;
        private int turn;
        private int n;

        public GiantHead() {
            this(520);
        }

        public GiantHead(int health) {
            super(health, 3, true);
            property.isElite = true;
            property.actNumber = 3;
            property.canWeaken = true;
        }

        public GiantHead(EnemyBeyond.GiantHead other) {
            super(other);
            slow = other.slow;
            turn = other.turn;
            n = other.n;
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.GiantHead(this);
        }

        public int getSlow() {
            return slow;
        }

        public int getTurnUntilLargeAttack() {
            return 3 - turn;
        }

        @Override public void damage(int n, GameState state) {
            n = (int) (n * ((10 + slow) / 10.0)); // todo: when is slow applied with respect to vulnerable, and is it rounding down?
            super.damage(n, state);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == COUNT) {
                state.enemyDoDamageToPlayer(this, 13, 1);
            } else if (move == GLARE) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
            } else if (move == IT_IS_TIME) {
                state.enemyDoDamageToPlayer(this, 40 + n * 5, 1);
                if (n < 6) {
                    n++;
                }
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (turn < 3) {
                turn++;
                if (move == COUNT && lastMove == COUNT) {
                    newMove = GLARE;
                } else if (move == GLARE && lastMove == GLARE) {
                    newMove = COUNT;
                } else {
                    state.setIsStochastic();
                    newMove = random.nextBoolean(RandomGenCtx.EnemyChooseMove) ? GLARE : COUNT;
                }
            } else {
                newMove = IT_IS_TIME;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == COUNT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 13);
            } else if (move == GLARE) {
                return "Weak 1";
            } else if (move == IT_IS_TIME) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 40 + n * 5);
            }
            return "Unknown";
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            slow = 0;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var idx = state.getEnemiesForRead().find(this);
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, Card card, int lastIdx, boolean cloned) {
                    ((GiantHead) state.getEnemiesForWrite().getForWrite(idx)).slow++;
                }
            });
        }

        @Override public void randomize(RandomGen random, boolean training) {
            if (training) {
                int b = random.nextInt(25, RandomGenCtx.Other) + 1;
                health = (int) Math.round((health * b) / 25.0);
            } else {
                health = 520;
            }
        }

        @Override public String getName() {
            return "Giant Head";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            if (slow == 0) {
                return s;
            }
            return s.subSequence(0, s.length() - 1) + ", slow=" + slow + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && slow == ((GiantHead) o).slow && turn == ((GiantHead) o).turn && n == ((GiantHead) o).n;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 3;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "3 inputs to keep track of Giant Head slow, turn until large attack and large attack damage";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = slow / 20.0f;
            input[idx + 1] = (3 - turn) / 3.0f;
            input[idx + 2] = n / 6.0f;
            return 3;
        }
    }

    public static class Nemesis extends Enemy {
        private static final int DEBUFF = 0;
        private static final int ATTACK = 1;
        private static final int SCYTHE = 2;

        private boolean intangible;
        private int scytheCooldown = 0;

        public Nemesis() {
            this(200);
        }

        public Nemesis(int health) {
            super(health, 3, true);
            property.isElite = true;
            property.actNumber = 3;
        }

        public Nemesis(EnemyBeyond.Nemesis other) {
            super(other);
            intangible = other.intangible;
            scytheCooldown = other.scytheCooldown;
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.Nemesis(this);
        }

        public boolean isIntangible() {
            return intangible;
        }

        @Override public void damage(int n, GameState state) {
            int prevHp = health;
            super.damage(n, state);
            if (intangible) {
                if (state.prop.hasBoot) {
                    if (health - prevHp > 5) {
                        setHealth(prevHp + 5);
                    }
                } else if (health - prevHp > 1) {
                    setHealth(prevHp + 1);
                }
            }
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int prevHp = health;
            super.nonAttackDamage(n, blockable, state);
            if (intangible) {
                if (state.prop.hasBoot) {
                    if (health - prevHp > 5) {
                        setHealth(prevHp + 5);
                    }
                } else if (health - prevHp > 1) {
                    setHealth(prevHp + 1);
                }
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == DEBUFF) {
                state.addCardToDiscard(state.prop.burnCardIdx);
                state.addCardToDiscard(state.prop.burnCardIdx);
                state.addCardToDiscard(state.prop.burnCardIdx);
                state.addCardToDiscard(state.prop.burnCardIdx);
                state.addCardToDiscard(state.prop.burnCardIdx);
            } else if (move == ATTACK) {
                state.enemyDoDamageToPlayer(this, 7, 3);
            } else if (move == SCYTHE) {
                state.enemyDoDamageToPlayer(this, 45, 1);
            }
        }

        // simplified version, replace when tested successfully
        private void nextMove2(GameState state, RandomGen random) {
            int newMove;
            if (move < 0) {
                state.setIsStochastic();
                newMove = random.nextBoolean(RandomGenCtx.EnemyChooseMove) ? ATTACK : DEBUFF;
            } else {
                state.setIsStochastic();
                int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                if (r < 30) {
                    if (move != SCYTHE) {
                        newMove = SCYTHE;
                    } else if (random.nextBoolean(RandomGenCtx.EnemyChooseMove)) {
                        if (!(move == ATTACK && lastMove == ATTACK)) {
                            newMove = ATTACK;
                        } else {
                            newMove = DEBUFF;
                        }
                    } else if (!(move == DEBUFF && lastMove == DEBUFF)) {
                        newMove = DEBUFF;
                    } else {
                        newMove = ATTACK;
                    }
                } else if (r < 65) {
                    if (!(move == ATTACK && lastMove == ATTACK)) {
                        newMove = ATTACK;
                    } else if (random.nextBoolean(RandomGenCtx.EnemyChooseMove)) {
                        if (move == SCYTHE) {
                            newMove = DEBUFF;
                        } else {
                            newMove = SCYTHE;
                        }
                    } else {
                        newMove = DEBUFF;
                    }
                } else if (move != DEBUFF) {
                    newMove = DEBUFF;
                } else {
                    newMove = ATTACK;
                }
            }
            lastMove = move;
            move = newMove;
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            scytheCooldown--;
            if (move < 0) {
                state.setIsStochastic();
                newMove = random.nextBoolean(RandomGenCtx.EnemyChooseMove) ? ATTACK : DEBUFF;
            } else {
                state.setIsStochastic();
                int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                boolean r2 = random.nextBoolean(RandomGenCtx.EnemyChooseMove);
                if (r < 30) {
                    if (move != SCYTHE) {
                        newMove = SCYTHE;
                    } else if (r2) {
                        if (!(move == ATTACK && lastMove == ATTACK)) {
                            newMove = ATTACK;
                        } else {
                            newMove = DEBUFF;
                        }
                    } else if (!(move == DEBUFF && lastMove == DEBUFF)) {
                        newMove = DEBUFF;
                    } else {
                        newMove = ATTACK;
                    }
                } else if (r < 65) {
                    if (!(move == ATTACK && lastMove == ATTACK)) {
                        newMove = ATTACK;
                    } else if (r2) {
                        if (move == SCYTHE) {
                            newMove = DEBUFF;
                        } else {
                            newMove = SCYTHE;
                        }
                    } else {
                        newMove = DEBUFF;
                    }
                } else if (move != DEBUFF) {
                    newMove = DEBUFF;
                } else if (r2) {
                    newMove = SCYTHE;
                } else {
                    newMove = ATTACK;
                }
                int prevScythe = scytheCooldown;
                int mm = checkNextMove2(r, r2);
                if (newMove != mm) {
                    System.err.println(r);
                    System.err.println(r2);
                    System.err.println(move);
                    System.err.println(lastMove);
                    System.err.println(prevScythe);
                    System.err.println(newMove);
                    System.err.println(mm);
                    Integer.parseInt(null);
                }
            }
            lastMove = move;
            move = newMove;
        }

        private int checkNextMove2(int r, boolean r2) {
            if (r < 30) {
                if (move != SCYTHE && this.scytheCooldown <= 0) {
                    scytheCooldown = 2;
                    return SCYTHE;
                } else if (r2) {
                    if (!(move == ATTACK && lastMove == ATTACK)) {
                        return ATTACK;
                    } else {
                        return DEBUFF;
                    }
                } else if (!(move == DEBUFF && lastMove == DEBUFF)) {
                    return DEBUFF;
                } else {
                    return ATTACK;
                }
            } else if (r < 65) {
                if (!(move == ATTACK && lastMove == ATTACK)) {
                    return ATTACK;
                } else if (r2) {
                    if (scytheCooldown > 0) {
                        return DEBUFF;
                    } else {
                        scytheCooldown = 2;
                        return SCYTHE;
                    }
                } else {
                    return DEBUFF;
                }
            } else if (move != DEBUFF) {
                return DEBUFF;
            } else if (r2 && scytheCooldown <= 0) {
                scytheCooldown = 2;
                return SCYTHE;
            } else {
                return ATTACK;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == DEBUFF) {
                return "Add 5 Burns";
            } else if (move == ATTACK) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "x3";
            } else if (move == SCYTHE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 45);
            }
            return "Unknown";
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            intangible = !intangible;
        }

        public List<Card> getPossibleGeneratedCards() { return List.of(new Card.Burn()); }

        @Override public void randomize(RandomGen random, boolean training) {
            if (training) {
                int b = random.nextInt(10, RandomGenCtx.Other) + 1;
                health = (int) Math.round((health * b) / 10.0);
            } else {
                health = 200;
            }
        }

        @Override public String getName() {
            return "Nemesis";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            if (!intangible) {
                return s;
            }
            return s.subSequence(0, s.length() - 1) + ", intangible}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && intangible == ((Nemesis) o).intangible;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Nemesis intangible";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = intangible ? 0.5f : 0f;
            return 1;
        }
    }

    public static class Reptomancer extends Enemy {
        private static final int SUMMON = 0;
        private static final int SNAKE_STRIKE = 1;
        private static final int BIG_BITE = 2;
        private static final int SUMMON_ORDER[] = new int[] {4, 2, 1, 0};

        public Reptomancer() {
            this(200);
        }

        public Reptomancer(int health) {
            super(health, 3, true);
            property.isElite = true;
            property.actNumber = 3;
            property.canWeaken = true;
        }

        public Reptomancer(EnemyBeyond.Reptomancer other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.Reptomancer(this);
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

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SUMMON) {
                int summoned = 0, startIdx = 0, idx = 0;
                var enemies = state.getEnemiesForRead();
                for (int i = 0; i < enemies.size(); i++) {
                    if (state.getEnemiesForRead().get(i) instanceof Dagger) {
                        startIdx = i;
                        break;
                    }
                }
                while (state.enemiesAlive < 5 && summoned < 2 && idx < 4) {
                    // enemy order is top left, top right, bottom left, reptomancer, bottom right (0, 1, 2, 3, 4)
                    // summon order is bottom right, bottom left, top right, top left  (4, 2, 1, 0)
                    if (!state.getEnemiesForRead().get(startIdx + SUMMON_ORDER[idx++]).isAlive()) {
                        state.reviveEnemy(startIdx + SUMMON_ORDER[idx - 1], false);
                        state.getEnemiesForWrite().getForWrite(startIdx + SUMMON_ORDER[idx - 1]).reviveReset();
                        summoned++;
                    }
                }
            } else if (move == SNAKE_STRIKE) {
                state.enemyDoDamageToPlayer(this, 16, 2);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
            } else if (move == BIG_BITE) {
                state.enemyDoDamageToPlayer(this, 34, 1);
            }
        }

        private int nextMove(GameState state, RandomGen random, int r) {
            if (r < 33) {
                if (move != SNAKE_STRIKE) {
                    return SNAKE_STRIKE;
                } else {
                    return nextMove(state, random ,33 + random.nextInt(67, RandomGenCtx.EnemyChooseMove));
                }
            } else if (r < 66) {
                if (!(move == SUMMON && lastMove == SUMMON)) {
                    if (state.enemiesAlive < 5) {
                        return SUMMON;
                    } else {
                        return SNAKE_STRIKE;
                    }
                } else {
                    return SNAKE_STRIKE;
                }
            } else if (move != BIG_BITE) {
                return BIG_BITE;
            } else {
                return nextMove(state, random, random.nextInt(66, RandomGenCtx.EnemyChooseMove));
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move == -1) {
                newMove = SUMMON;
            } else {
                state.setIsStochastic();
                int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                newMove = nextMove(state, random, r);
            }
            lastMove = move;
            move = newMove;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    var enemies = state.getEnemiesForWrite();
                    for (int i = 0; i < enemies.size(); i++) {
                        if (enemies.get(i) instanceof Dagger) {
                            state.killEnemy(i);
                            state.killEnemy(i + 1);
                            break;
                        }
                    }
                }
            });
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SUMMON) {
                return "Summon Daggers";
            } else if (move == SNAKE_STRIKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 16) + "x2+Weak 1";
            } else if (move == BIG_BITE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 34);
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training) {
            if (training) {
                int b = random.nextInt(10, RandomGenCtx.Other) + 1;
                health = (int) Math.round((health * b) / 10.0);
            } else {
                health = 190 + random.nextInt(11, RandomGenCtx.Other);
            }
        }

        @Override public String getName() {
            return "Reptomancer";
        }
    }

    public static class Dagger extends Enemy {
        private static final int STAB = 0;
        private static final int EXPLODE = 1;

        public Dagger() {
            this(25);
        }

        public Dagger(int health) {
            super(health, 2, false);
            property.isElite = true;
            property.actNumber = 3;
        }

        public Dagger(EnemyBeyond.Dagger other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.Dagger(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == STAB) {
                state.enemyDoDamageToPlayer(this, 9, 1);
                state.addCardToDiscard(state.prop.woundCardIdx);
            } else if (move == EXPLODE) {
                state.enemyDoDamageToPlayer(this, 25, 1);
                state.killEnemy(state.getEnemiesForRead().find(self));
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move++;
        }

        public List<Card> getPossibleGeneratedCards() { return List.of(new Card.Wound()); }

        @Override public String getMoveString(GameState state, int move) {
            if (move == STAB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "+Wound 1";
            } else if (move == EXPLODE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 25);
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training) {
            if (training) {
                int b = random.nextInt(4, RandomGenCtx.Other) + 1;
                health = (int) Math.round((health * b) / 4.0);
            } else {
                health = 20 + random.nextInt(6, RandomGenCtx.Other);
            }
        }

        @Override public String getName() {
            return "Dagger";
        }
    }

    public static class TheMaw extends Enemy {
        private static final int ROAR = 0;
        private static final int DROOL = 1;
        private static final int SLAM = 2;
        private static final int NOM = 3;

        private int turnCount = 1;

        public TheMaw() {
            this(300);
        }

        public TheMaw(int health) {
            super(health, 4, false);
            property.canGainStrength = true;
            property.canWeaken = true;
            property.canFrail = true;
        }

        public TheMaw(EnemyBeyond.TheMaw other) {
            super(other);
            turnCount = other.turnCount;
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.TheMaw(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == ROAR) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 5);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 5);
            } else if (move == DROOL) {
                gainStrength(5);
            } else if (move == SLAM) {
                state.enemyDoDamageToPlayer(this, 30, 1);
            } else if (move == NOM) {
                state.enemyDoDamageToPlayer(this, 5, turnCount / 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            turnCount++;
            if (move < 0) {
                newMove = ROAR;
            } else if (move == ROAR || move == DROOL) {
                state.setIsStochastic();
                newMove = random.nextBoolean(RandomGenCtx.EnemyChooseMove) ? SLAM : NOM;
            } else if (move == SLAM) {
                state.setIsStochastic();
                newMove = random.nextBoolean(RandomGenCtx.EnemyChooseMove) ? NOM : DROOL;
            } else {
                newMove = DROOL;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == ROAR) {
                return "Weak 5+Frail 5";
            } else if (move == DROOL) {
                return "Gain 5 Strength";
            } else if (move == SLAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 30);
            } else if (move == NOM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 5) + "x" + turnCount / 2;
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training) {
            int b = random.nextInt(15, RandomGenCtx.Other) + 1;
            if (training) {
                health = (int) Math.round((health * b) / 15.0);
            } else {
                health = 300;
            }
        }

        @Override public String getName() {
            return "The Maw";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && turnCount == ((TheMaw) o).turnCount;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of current turn count for The Maw";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx + 1] = turnCount / 10.0f;
            return 1;
        }
    }

    public static class Darkling extends Enemy {
        private static final int NIP = 0;
        private static final int CHOMP = 1;
        private static final int HARDEN = 2;
        private static final int REGROW_1 = 3;
        private static final int REGROW_2 = 4;
        private static final int REINCARNATE = 5;

        private int d = -1;
        private boolean middle;

        public Darkling(boolean middle) {
            this(59, middle);
        }

        public Darkling(int health, boolean middle) {
            super(health, 6, true);
            this.middle = middle;
            property.canGainStrength = true;
            property.canGainBlock = true;
            property.canSelfRevive = true;
        }

        public Darkling(EnemyBeyond.Darkling other) {
            super(other);
            middle = other.middle;
            d = other.d;
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.Darkling(this);
        }

        public int getNipDamage() {
            return d;
        }

        public void setNipDamage(int n) {
            d = n;
        }

        @Override public void damage(int n, GameState state) {
            int prevHealth = health;
            super.damage(n, state);
            if (health <= 0 && prevHealth > 0) {
                move = REGROW_1;
            }
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int prevHealth = health;
            super.nonAttackDamage(n, blockable, state);
            if (health <= 0 && prevHealth > 0) {
                move = REGROW_1;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == NIP) {
                state.enemyDoDamageToPlayer(this, d + 2, 1);
            } else if (move == CHOMP) {
                state.enemyDoDamageToPlayer(this, 9, 2);
            } else if (move == HARDEN) {
                gainBlock(12);
                gainStrength(2);
            } else if (move == REGROW_1 || move == REGROW_2) {
            } else if (move == REINCARNATE) {
                health = property.origHealth / 2;
                state.adjustEnemiesAlive(1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove = -1;
            if (move == REGROW_1) {
                newMove = REGROW_2;
            } else if (move == REGROW_2) {
                newMove = REINCARNATE;
                reviveReset();
            } else if (move < 0) {
                state.setIsStochastic();
                newMove = random.nextBoolean(RandomGenCtx.EnemyChooseMove) ? NIP : HARDEN;
            } else {
                state.setIsStochastic();
                int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                if (r < 40) {
                    if (move != CHOMP && !middle) {
                        newMove = CHOMP;
                    } else {
                        r = 40 + random.nextInt(60, RandomGenCtx.EnemyChooseMove);
                    }
                }
                if (r >= 40 && r < 70) {
                    if (!(move == NIP && lastMove == NIP)) {
                        newMove = NIP;
                    } else {
                        newMove = HARDEN;
                    }
                } else if (r >= 70 && !(move == HARDEN && lastMove == HARDEN)) {
                    newMove = HARDEN;
                } else if (r >= 70) {
                    newMove = NIP;
                }
            }
            lastMove = move;
            move = newMove;
            if (d < 0 && move == NIP) {
                d = 7 + random.nextInt(5, RandomGenCtx.Other);
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == NIP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, d + 2);
            } else if (move == CHOMP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "x2";
            } else if (move == HARDEN) {
                return "Gain 12 Block and 2 Strength";
            } else if (move == REGROW_1 || move == REGROW_2) {
                return "Regrow";
            } else if (move == REINCARNATE) {
                return "Reincarnate";
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training) {
            int b = random.nextInt(6, RandomGenCtx.Other) + 1;
            if (training) {
                health = (int) Math.round((health * b) / 6.0);
            } else {
                health = 50 + random.nextInt(10, RandomGenCtx.Other);
            }
        }

        @Override public String getName() {
            return "Darkling";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            if (d < 0) {
                return s.replaceFirst("hp=(\\d+)", "hp=$1/" + property.origHealth);
            }
            return s.subSequence(0, s.length() - 1) + ", nipDmg=" + d + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && d == ((Darkling) o).d;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 2;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "2 input to keep track of Darkling Nip damage and max HP";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = d < 0 ? -0.5f : d / 11.0f;
            input[idx + 1] = property.origHealth / (float) property.maxHealth;
            return 2;
        }
    }
}
