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
            property.isBoss = true;
            strength = 2;
        }

        public AwakenedOne(EnemyBeyond.AwakenedOne other) {
            super(other);
            awakened = other.awakened;
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.AwakenedOne(this);
        }

        @Override public int damage(double n, GameState state) {
            int dmg = super.damage(n, state);
            if (health <= 0) {
                if (!awakened) {
                    if (!state.prop.hasRunicDome) {
                        move = REBIRTH;
                    }
                } else {
                    for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                        state.killEnemy(i, true);
                    }
                }
            }
            return dmg;
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
                        state.killEnemy(i, true);
                    }
                }
            }
        }

        @Override public void startTurn(GameState state) {
            super.startTurn(state);
            heal(15);
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var idx = state.getEnemiesForRead().find(this);
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    var enemy = state.getEnemiesForRead().get(idx);
                    if (state.prop.cardDict[cardIdx].cardType == Card.POWER && !((AwakenedOne) enemy).awakened && enemy.getMove() != REBIRTH) {
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

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
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

        @Override public void setHealth(int hp) {
            health = hp;
            if (health <= 0) {
                awakened = true;
            }
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
                if (difficulty <= 16) {
                    awakened = true;
                    health = (int) Math.round(((double) (health * difficulty)) / 16);
                } else {
                    health = (int) Math.round(((double) (health * (difficulty - 16))) / 16);
                }
            } else {
                health = 320;
            }
        }

        @Override public int getMaxRandomizeDifficulty() {
            return 32;
        }

        @Override public String getName() {
            return "Awakened One";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            if (!awakened) {
                return s;
            }
            return s.subSequence(0, s.length() - 1) + ", awakened}";
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

        public boolean isAwakened() {
            return awakened;
        }
    }

    public static class Donu extends Enemy {
        private static final int CIRCLE_OF_POWER = 0;
        private static final int BEAM = 1;

        public Donu() {
            this(265);
        }

        public Donu(int health) {
            super(health, 2, false);
            artifact = 3;
            property.canGainStrength = true;
            property.canGainPlatedArmor = true;
            property.hasArtifact = true;
            property.isBoss = true;
        }

        public Donu(EnemyBeyond.Donu other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.Donu(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CIRCLE_OF_POWER) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemy.gainStrength(3);
                }
            } else if (move == BEAM) {
                state.enemyDoDamageToPlayer(this, 12, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move == -1 || move == BEAM) {
                newMove = CIRCLE_OF_POWER;
            } else {
                newMove = BEAM;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CIRCLE_OF_POWER) {
                return "All Enemies Gain 3 Strength";
            } else if (move == BEAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12) + "x2";
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
                difficulty = random.nextInt(12, RandomGenCtx.Other) + 1;
                health = (int) Math.round(((double) (health * difficulty)) / 12);
            } else {
                health = 265;
            }
        }

        @Override public String getName() {
            return "Donu";
        }
    }


    public static class Deca extends Enemy {
        private static final int SQUARE_OF_PROTECTION = 0;
        private static final int BEAM = 1;

        public Deca() {
            this(265);
        }

        public Deca(int health) {
            super(health, 2, false);
            artifact = 3;
            property.canGainStrength = true;
            property.canGainPlatedArmor = true;
            property.canDaze = true;
            property.hasArtifact = true;
            property.isBoss = true;
        }

        public Deca(EnemyBeyond.Deca other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.Deca(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SQUARE_OF_PROTECTION) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemy.gainBlock(16);
                    enemy.gainPlatedArmor(3);
                }
            } else if (move == BEAM) {
                state.enemyDoDamageToPlayer(this, 12, 2);
                state.addCardToDiscard(state.prop.dazedCardIdx);
                state.addCardToDiscard(state.prop.dazedCardIdx);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move == -1 || move == SQUARE_OF_PROTECTION) {
                newMove = BEAM;
            } else {
                newMove = SQUARE_OF_PROTECTION;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SQUARE_OF_PROTECTION) {
                return "All Enemies Gain 16 Block and 3 Metallicize";
            } else if (move == BEAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12) + "x2+Dazed 2";
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
                difficulty = random.nextInt(12, RandomGenCtx.Other) + 1;
                health = (int) Math.round(((double) (health * difficulty)) / 12);
            } else {
                health = 265;
            }
        }

        @Override public String getName() {
            return "Deca";
        }
    }

    public static class TimeEater extends Enemy {
        private static final int REVERBERATE = 0;
        private static final int HEAD_SLAM = 1;
        private static final int RIPPLE = 2;
        private static final int HASTE = 3;

        private boolean hasted;
        private boolean startTurnLessHalfHealth;

        public TimeEater() {
            this(480);
        }

        public TimeEater(int health) {
            super(health, 4, true);
            property.canSlime = true;
            property.canGainStrength = true;
            property.canGainBlock = true;
            property.canVulnerable = true;
            property.canWeaken = true;
            property.canFrail = true;
            property.isBoss = true;
        }

        public TimeEater(EnemyBeyond.TimeEater other) {
            super(other);
            hasted = other.hasted;
            startTurnLessHalfHealth = other.startTurnLessHalfHealth;
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.TimeEater(this);
        }

        @Override public void saveStateForNextMove(GameState state) {
            for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                if (state.getEnemiesForRead().get(i) instanceof TimeEater) {
                    startTurnLessHalfHealth = state.getEnemiesForRead().get(i).getHealth() < property.maxHealth / 2;
                }
            }
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.registerCounter("DrawReduction", new GameProperties.CounterRegistrant() {
                @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
                    gameProperties.drawReductionCounterIdx = idx;
                }
                @Override public int getCounterIdx(GameProperties gameProperties) {
                    return  gameProperties.drawReductionCounterIdx;
                }
            }, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[state.prop.drawReductionCounterIdx];
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.prop.registerCounter("TimeEater", new GameProperties.CounterRegistrant() {
                @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
                    gameProperties.timeEaterCounterIdx = idx;
                }
                @Override public int getCounterIdx(GameProperties gameProperties) {
                    return  gameProperties.timeEaterCounterIdx;
                }
            }, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    int counter = state.getCounterForRead()[state.prop.timeEaterCounterIdx];
                    input[idx + counter] = 0.5f;
                    return idx + 13;
                }

                @Override public int getInputLenDelta() {
                    return 13;
                }
            });
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    var c = state.getCounterForWrite();
                    if (c[state.prop.timeEaterCounterIdx] == 12) {
                        Integer.parseInt(null);
                    }
                    c[state.prop.timeEaterCounterIdx]++;
                }
            });
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == REVERBERATE) {
                state.enemyDoDamageToPlayer(this, 8, 3);
            } else if (move == HEAD_SLAM) {
                state.enemyDoDamageToPlayer(this, 32, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.DRAW_REDUCTION, 2);
                state.addCardToDiscard(state.prop.slimeCardIdx);
                state.addCardToDiscard(state.prop.slimeCardIdx);
            } else if (move == RIPPLE) {
                gainBlock(20);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 1);
            } else if (move == HASTE) {
                heal(480 / 2 - health);
                gainBlock(32);
                removeAllDebuffs();
                hasted = true;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove = -1;
            if ((state.prop.hasRunicDome ? startTurnLessHalfHealth : (health < property.maxHealth / 2)) && !hasted) {
                hasted = true;
                newMove = HASTE;
            } else {
                state.setIsStochastic();
                int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                for (int i = 0; i < 1; i++) {
                    if (r < 45) {
                        if (move != REVERBERATE || lastMove != REVERBERATE) {
                            newMove = REVERBERATE;
                        } else {
                            r = 50 + random.nextInt(50, RandomGenCtx.EnemyChooseMove);
                            i = -1;
                        }
                    } else if (r < 80) {
                        if (move != HEAD_SLAM) {
                            newMove = HEAD_SLAM;
                        } else if (random.nextInt(100, RandomGenCtx.EnemyChooseMove) <= 66) {
                            newMove = REVERBERATE;
                        } else {
                            newMove = RIPPLE;
                        }
                    } else if (move != RIPPLE) {
                        newMove = RIPPLE;
                    } else {
                        r = random.nextInt(75, RandomGenCtx.EnemyChooseMove);
                        i = -1;
                    }
                }
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == REVERBERATE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "x3";
            } else if (move == HEAD_SLAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 32) + "+Draw Reduce 1+Slime 2";
            } else if (move == RIPPLE) {
                return "Block 20+Vulnerable 1+Weaken 1+Frail 1";
            } else if (move == HASTE) {
                return "Heal+Block 32+Remove All Debuffs";
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
                int b = random.nextInt(24, RandomGenCtx.Other) + 1;
                health = (int) Math.round((health * b) / 24.0);
                if (health < 240) {
                    hasted = true;
                }
            } else {
                health = 480;
            }
        }

        @Override public String getName() {
            return "Time Eater";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            if (!hasted) {
                if (startTurnLessHalfHealth) {
                    return s.subSequence(0, s.length() - 1) + ", lessThanHalfHealth}";
                }
                return s;
            }
            return s.subSequence(0, s.length() - 1) + ", hasted}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && hasted == ((EnemyBeyond.TimeEater) o).hasted;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return prop.hasRunicDome ? 2 : 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return prop.hasRunicDome ? "2 inputs to keep track of whether Time Eater has hasted and started the turn with less than half health" :
                    "1 input to keep track of whether Time Eater has hasted";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = hasted ? 0.5f : 0;
            if (prop.hasRunicDome) {
                input[idx] = startTurnLessHalfHealth ? 0.5f : 0f;
                return 2;
            }
            return 1;
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

        @Override public int damage(double n, GameState state) {
            n = n * ((10 + slow) / 10.0);
            return super.damage(n, state);
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
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    ((GiantHead) state.getEnemiesForWrite().getForWrite(idx)).slow++;
                }
            });
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
                int b = random.nextInt(26, RandomGenCtx.Other) + 1;
                health = (int) Math.round((health * b) / 26.0);
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

        private boolean intangible = true;
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

        @Override public int damage(double n, GameState state) {
            int prevHp = health;
            int dmg = super.damage(n, state);
            if (intangible) {
                if (state.prop.hasBoot) {
                    if (prevHp - health > 5) {
                        setHealth(prevHp - 5);
                        return 5;
                    }
                } else if (prevHp - health > 1) {
                    setHealth(prevHp - 1);
                    return 1;
                }
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int prevHp = health;
            super.nonAttackDamage(n, blockable, state);
            if (intangible) {
                if (prevHp - health > 1) {
                    setHealth(prevHp - 1);
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

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new Card.Burn()); }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
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

        @Override public int damage(double n, GameState state) {
            int dmg = super.damage(n, state);
            if (health <= 0) {
                var enemies = state.getEnemiesForRead();
                for (int i = 0; i < enemies.size(); i++) {
                    state.killEnemy(i, true);
                }
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (health <= 0) {
                var enemies = state.getEnemiesForRead();
                for (int i = 0; i < enemies.size(); i++) {
                    state.killEnemy(i, true);
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
                        state.reviveEnemy(startIdx + SUMMON_ORDER[idx - 1], false, -1);
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
                            state.killEnemy(i, false);
                            state.killEnemy(i + 1, false);
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

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
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
            property.isMinion = true;
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
                state.killEnemy(state.getEnemiesForRead().find(self), true);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move++;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new Card.Wound()); }

        @Override public String getMoveString(GameState state, int move) {
            if (move == STAB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "+Wound 1";
            } else if (move == EXPLODE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 25);
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
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

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
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

    public static class OrbWalker extends Enemy {
        private static final int LASER = 0;
        private static final int CLAW = 1;

        public OrbWalker() {
            this(102);
            property.canGainStrength = true;
        }

        public OrbWalker(int health) {
            super(health, 2, true);
        }

        public OrbWalker(EnemyBeyond.OrbWalker other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.OrbWalker(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == LASER) {
                state.enemyDoDamageToPlayer(this, 11, 1);
                state.addCardToDiscard(state.prop.burnCardIdx);
                state.addCardToDeck(state.prop.burnCardIdx);
            } else if (move == CLAW) {
                state.enemyDoDamageToPlayer(this, 16, 1);
            }
            gainStrength(5);
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move == LASER && lastMove == LASER) {
                newMove = CLAW;
            } else if (move == CLAW && lastMove == CLAW) {
                newMove = LASER;
            } else {
                int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                newMove = r < 60 ? LASER : CLAW;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == LASER) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11) + "+Shuffle Burn";
            } else if (move == CLAW) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 16);
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
                int b = random.nextInt(5, RandomGenCtx.Other) + 1;
                health = (int) Math.round((health * b) / 5.0);
            } else {
                health = 92 + random.nextInt(11, RandomGenCtx.Other);
            }
        }

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new Card.Burn()); }

        @Override public String getName() {
            return "Orb Walker";
        }
    }

    public static class SpireGrowth extends Enemy {
        private static final int QUICK_TACKLE = 0;
        private static final int SMASH = 1;
        private static final int CONSTRICT = 2;

        public SpireGrowth() {
            this(190);
        }

        public SpireGrowth(int health) {
            super(health, 3, true);
        }

        public SpireGrowth(EnemyBeyond.SpireGrowth other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.SpireGrowth(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == QUICK_TACKLE) {
                state.enemyDoDamageToPlayer(this, 18, 1);
            } else if (move == SMASH) {
                state.enemyDoDamageToPlayer(this, 25, 1);
            } else if (move == CONSTRICT) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.CONSTRICTED, 12);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (state.getCounterForRead()[state.prop.constrictedCounterIdx] == 0 && lastMove != CONSTRICT) {
                newMove = CONSTRICT;
            } else {
                if (move == QUICK_TACKLE && lastMove == QUICK_TACKLE) {
                    newMove = SMASH;
                } else if (move == SMASH && lastMove == SMASH) {
                    newMove = QUICK_TACKLE;
                } else {
                    int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                    newMove = r < 50 ? QUICK_TACKLE : SMASH;
                }
//                int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
//                if (r < 50 && !(move == QUICK_TACKLE && lastMove == QUICK_TACKLE)) {
//                    newMove = QUICK_TACKLE;
//                } else if (!(move == SMASH && lastMove == SMASH)) {
//                    newMove = SMASH;
//                } else {
//                    newMove = QUICK_TACKLE;
//                }
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == QUICK_TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 18);
            } else if (move == SMASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 25);
            } else if (move == CONSTRICT) {
                return "Apply 12 Constricted";
            }
            return "Unknown";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.registerCounter("Constricted", new GameProperties.CounterRegistrant() {
                @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
                    gameProperties.constrictedCounterIdx = idx;
                }
                @Override public int getCounterIdx(GameProperties gameProperties) {
                    return  gameProperties.constrictedCounterIdx;
                }
            }, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    int counter = state.getCounterForRead()[state.prop.constrictedCounterIdx];
                    input[idx] = counter / 12.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.addPreEndOfTurnHandler("Constricted", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[state.prop.constrictedCounterIdx] > 0) {
                        state.getPlayerForWrite().nonAttackDamage(state, state.getCounterForRead()[state.prop.constrictedCounterIdx], true);
                    }
                }
            });
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(9, RandomGenCtx.Other) + 1;
            if (training) {
                health = (int) Math.round((health * b) / 9.0);
            } else {
                health = 190;
            }
        }

        @Override public String getName() {
            return "Spire Growth";
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

        @Override public int damage(double n, GameState state) {
            int prevHealth = health;
            int dmg = super.damage(n, state);
            if (health <= 0 && prevHealth > 0) {
                move = REGROW_1;
            }
            return dmg;
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

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
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
