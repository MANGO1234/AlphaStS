package com.alphaStS.enemy;

import com.alphaStS.*;
import com.alphaStS.card.Card;
import com.alphaStS.card.CardOther;

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
            properties.canGainStrength = true;
            properties.canSelfRevive = true;
            properties.isBoss = true;
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
                    if (!state.properties.hasRunicDome) {
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
                    if (!state.properties.hasRunicDome) {
                        move = REBIRTH;
                    }
                } else {
                    for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                        state.killEnemy(i, true);
                    }
                }
            }
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            heal(15);
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var idx = state.getEnemiesForRead().find(this);
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    var enemy = state.getEnemiesForRead().get(idx);
                    if (state.properties.cardDict[cardIdx].cardType == Card.POWER && !((AwakenedOne) enemy).awakened && enemy.getMove() != REBIRTH) {
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
                health = properties.maxHealth;
                awakened = true;
            } else if (move == DARK_ECHO) {
                state.enemyDoDamageToPlayer(this, 40, 1);
            } else if (move == SLUDGE) {
                state.enemyDoDamageToPlayer(this, 18, 1);
                state.addCardToDeck(state.properties.voidCardIdx);
            } else if (move == TACKLE) {
                state.enemyDoDamageToPlayer(this, 10, 3);
            }
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther.Void());
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
            properties.canGainStrength = true;
            properties.canGainPlatedArmor = true;
            properties.hasArtifact = true;
            properties.isBoss = true;
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

        @Override public int getMaxRandomizeDifficulty() {
            return 12;
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
//                difficulty = random.nextInt(12, RandomGenCtx.Other) + 1;
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
            properties.canGainStrength = true;
            properties.canGainPlatedArmor = true;
            properties.hasArtifact = true;
            properties.isBoss = true;
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
                state.addCardToDiscard(state.properties.dazedCardIdx);
                state.addCardToDiscard(state.properties.dazedCardIdx);
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

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new CardOther.Dazed()); }

        @Override public int getMaxRandomizeDifficulty() {
            return 12;
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
//                difficulty = random.nextInt(12, RandomGenCtx.Other) + 1;
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
            properties.canGainStrength = true;
            properties.canGainBlock = true;
            properties.canVulnerable = true;
            properties.canWeaken = true;
            properties.canFrail = true;
            properties.isBoss = true;
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
                    startTurnLessHalfHealth = state.getEnemiesForRead().get(i).getHealth() < properties.maxHealth / 2;
                }
            }
        }

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new CardOther.Slime()); }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("DrawReduction", new GameProperties.CounterRegistrant() {
                @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
                    gameProperties.drawReductionCounterIdx = idx;
                }
                @Override public int getCounterIdx(GameProperties gameProperties) {
                    return  gameProperties.drawReductionCounterIdx;
                }
            }, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[state.properties.drawReductionCounterIdx];
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.registerCounter("TimeEater", new GameProperties.CounterRegistrant() {
                @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
                    gameProperties.timeEaterCounterIdx = idx;
                }
                @Override public int getCounterIdx(GameProperties gameProperties) {
                    return  gameProperties.timeEaterCounterIdx;
                }
            }, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    int counter = state.getCounterForRead()[state.properties.timeEaterCounterIdx];
                    input[idx + counter] = 0.5f;
                    return idx + 13;
                }

                @Override public int getInputLenDelta() {
                    return 13;
                }
            });
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    var c = state.getCounterForWrite();
                    if (c[state.properties.timeEaterCounterIdx] == 12) {
                        Integer.parseInt(null);
                    }
                    c[state.properties.timeEaterCounterIdx]++;
                }
            });
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == REVERBERATE) {
                state.enemyDoDamageToPlayer(this, 8, 3);
            } else if (move == HEAD_SLAM) {
                state.enemyDoDamageToPlayer(this, 32, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.DRAW_REDUCTION, 2);
                state.addCardToDiscard(state.properties.slimeCardIdx);
                state.addCardToDiscard(state.properties.slimeCardIdx);
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
            if ((state.properties.hasRunicDome ? startTurnLessHalfHealth : (health < properties.maxHealth / 2)) && !hasted) {
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

        @Override public int getMaxRandomizeDifficulty() {
            return 24;
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
//                int difficulty = random.nextInt(24, RandomGenCtx.Other) + 1;
                health = (int) Math.round((health * difficulty) / 24.0);
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
            properties.isElite = true;
            properties.actNumber = 3;
            properties.canWeaken = true;
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
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
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
            properties.isElite = true;
            properties.actNumber = 3;
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
                if (state.properties.hasBoot) {
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
                state.addCardToDiscard(state.properties.burnCardIdx);
                state.addCardToDiscard(state.properties.burnCardIdx);
                state.addCardToDiscard(state.properties.burnCardIdx);
                state.addCardToDiscard(state.properties.burnCardIdx);
                state.addCardToDiscard(state.properties.burnCardIdx);
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

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new CardOther.Burn()); }

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
        private static final int[] SUMMON_ORDER = new int[] {4, 1, 3, 0};

        public Reptomancer() {
            this(200);
        }

        public Reptomancer(int health) {
            super(health, 3, true);
            properties.isElite = true;
            properties.actNumber = 3;
            properties.canWeaken = true;
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
                    // enemy attack order is top left, bottom left, reptomancer, top right, bottom right (0, 1, 2, 3, 4)
                    // summon order is bottom right, bottom left, top right, top left  (4, 1, 3, 0)
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
            state.properties.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    var enemies = state.getEnemiesForWrite();
                    for (int i = 0; i < enemies.size(); i++) {
                        if (enemies.get(i) instanceof Dagger) {
                            state.killEnemy(i, false);
                            state.killEnemy(i + 3, false);
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
            properties.isElite = true;
            properties.isMinion = true;
            properties.actNumber = 3;
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
                state.addCardToDiscard(state.properties.woundCardIdx);
            } else if (move == EXPLODE) {
                state.enemyDoDamageToPlayer(this, 25, 1);
                state.killEnemy(state.getEnemiesForRead().find(self), true);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move++;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new CardOther.Wound()); }

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
            properties.canGainStrength = true;
            properties.canWeaken = true;
            properties.canFrail = true;
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
            input[idx] = turnCount / 10.0f;
            return 1;
        }
    }

    public static class WrithingMass extends Enemy {
        public static final int IMPLANT = 0;
        public static final int FLAIL = 1;
        public static final int WITHER = 2;
        public static final int MULTI_STRIKE = 3;
        public static final int STRONG_STRIKE = 4;

        private boolean implantUsed;
        private int extraBlockPerAttack = 0;
        private float implantPenalty = 0.8f;

        public WrithingMass(float implantPenalty) {
            this(175);
            this.implantPenalty = implantPenalty;
        }

        public WrithingMass(int health) {
            super(health, 5, true);
            properties.canWeaken = true;
            properties.canVulnerable = true;
            properties.canGainBlock = true;
        }

        public WrithingMass(EnemyBeyond.WrithingMass other) {
            super(other);
            implantUsed = other.implantUsed;
            extraBlockPerAttack = other.extraBlockPerAttack;
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.WrithingMass(this);
        }

        @Override public int damage(double n, GameState state) {
            var dmg = super.damage(n, state);
            if (dmg > 0) {
                var idx = state.getEnemiesForRead().find(this);
                var extraBlockPerAttack = this.extraBlockPerAttack++;
                state.addGameActionToStartOfDeque(state1 -> state1.getEnemiesForWrite().getForWrite(idx).gainBlock(3 + extraBlockPerAttack));
                var random = state.getSearchRandomGen();
                move = nextMove(state, random, random.nextInt(100, RandomGenCtx.EnemyChooseMove), move);
            }
            return dmg;
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            extraBlockPerAttack = 0;
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == IMPLANT) {
                implantUsed = true;
            } else if (move == FLAIL) {
                state.enemyDoDamageToPlayer(this, 16, 1);
                gainBlock(16);
            } else if (move == WITHER) {
                state.enemyDoDamageToPlayer(this, 12, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
            } else if (move == MULTI_STRIKE) {
                state.enemyDoDamageToPlayer(this, 9, 3);
            } else if (move == STRONG_STRIKE) {
                state.enemyDoDamageToPlayer(this, 38, 1);
            }
        }

        public int nextMove(GameState state, RandomGen random, int r, int lastMove) {
            state.setIsStochastic();
            if (r < 10) {
                if (lastMove != STRONG_STRIKE) {
                    return STRONG_STRIKE;
                } else {
                    return nextMove(state, random, random.nextInt(90, RandomGenCtx.EnemyChooseMove) + 10, lastMove);
                }
            } else if (r < 20) {
                if (!implantUsed && lastMove != IMPLANT) {
                    return IMPLANT;
                } else if (random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.1f) {
                    return STRONG_STRIKE;
                } else {
                    return nextMove(state, random, random.nextInt(80, RandomGenCtx.EnemyChooseMove) + 20, lastMove);
                }
            } else if (r < 40) {
                if (lastMove != WITHER) {
                    return WITHER;
                } else if (random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.4f) {
                    return nextMove(state, random, random.nextInt(20, RandomGenCtx.EnemyChooseMove), lastMove);
                } else {
                    return nextMove(state, random, random.nextInt(60, RandomGenCtx.EnemyChooseMove) + 40, lastMove);
                }
            } else if (r < 70) {
                if (lastMove != MULTI_STRIKE) {
                    return MULTI_STRIKE;
                } else if (random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.3f) {
                    return FLAIL;
                } else {
                    return nextMove(state, random, random.nextInt(40, RandomGenCtx.EnemyChooseMove), lastMove);
                }
            } else if (lastMove != FLAIL) {
                return FLAIL;
            } else {
                return nextMove(state, random, random.nextInt(70, RandomGenCtx.EnemyChooseMove), lastMove);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
            if (move < 0) {
                if (r < 33) {
                    newMove = MULTI_STRIKE;
                } else if (r < 66) {
                    newMove = FLAIL;
                } else {
                    newMove = WITHER;
                }
            } else {
                newMove = nextMove(state, random, r, move);
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == IMPLANT) {
                return "Implant";
            } else if (move == FLAIL) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 16) + "+Block 16";
            } else if (move == WITHER) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12) + "+Weak 2+Vulnerable 2";
            } else if (move == MULTI_STRIKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "x3";
            } else if (move == STRONG_STRIKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 38);
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(8, RandomGenCtx.Other) + 1;
            if (training) {
                health = (int) Math.round((health * b) / 8.0);
            } else {
                health = 175;
            }
        }

        @Override public String getName() {
            return "Writhing Mass";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            if (extraBlockPerAttack > 0) {
                s = s.subSequence(0, s.length() - 1) + ", malleable=" + (3 + extraBlockPerAttack) + "}";
            }
            if (implantUsed) {
                s = s.subSequence(0, s.length() - 1) + ", implant=used}";
            }
            return s;
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && implantUsed == ((WrithingMass) o).implantUsed && extraBlockPerAttack == ((WrithingMass) o).extraBlockPerAttack;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addExtraTrainingTarget("WrithingMassImplant", new GameProperties.TrainingTargetRegistrant() {
                @Override public void setVArrayIdx(int idx) {
                    state.properties.writhingMassVIdx = idx;
                }
            }, new TrainingTarget() {
                @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                    if (isTerminal > 0) {
                        boolean implantUsed = false;
                        for (var enemy : state.getEnemiesForRead()) {
                            if (enemy instanceof WrithingMass mass) {
                                if (mass.implantUsed) {
                                    implantUsed = true;
                                    break;
                                }
                            }
                        }
                        v[GameState.V_OTHER_IDX_START + state.properties.writhingMassVIdx] = implantUsed ? 1.0f : 0.0f;
                    } else if (isTerminal == 0) {
                        v[GameState.V_OTHER_IDX_START + state.properties.writhingMassVIdx] = state.getVOther(state.properties.writhingMassVIdx);
                    }
                }

                @Override public void updateQValues(GameState state, double[] v) {
                    double value = v[GameState.V_OTHER_IDX_START + state.properties.writhingMassVIdx];
                    v[GameState.V_HEALTH_IDX] *= 1 - value * implantPenalty;
                }
            });
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 2;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "2 inputs to keep track of whether implant has been used by Writhing Mass and Writhing Mass Malleable buff";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = implantUsed ? 0.5f : 0f;
            input[idx + 1] = extraBlockPerAttack / 10.0f;
            return 2;
        }
    }

    public static class OrbWalker extends Enemy {
        private static final int LASER = 0;
        private static final int CLAW = 1;

        public OrbWalker() {
            this(102);
            properties.canGainStrength = true;
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
                state.addCardToDiscard(state.properties.burnCardIdx);
                state.addCardToDeck(state.properties.burnCardIdx);
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

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new CardOther.Burn()); }

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
            if (state.getCounterForRead()[state.properties.constrictedCounterIdx] == 0 && move != CONSTRICT) {
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
            state.properties.registerCounter("Constricted", new GameProperties.CounterRegistrant() {
                @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
                    gameProperties.constrictedCounterIdx = idx;
                }
                @Override public int getCounterIdx(GameProperties gameProperties) {
                    return  gameProperties.constrictedCounterIdx;
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
        private static final int REGROW = 3;
        private static final int REINCARNATE = 4;

        private int lowerPossibleNipDmg = 7;
        private int upperPossibleNipDmg = 11;
        private final boolean middle;

        public Darkling(boolean middle) {
            this(59, middle);
        }

        public Darkling(int health, boolean middle) {
            super(health, 6, true);
            this.middle = middle;
            properties.canGainStrength = true;
            properties.canGainBlock = true;
            properties.canSelfRevive = true;
        }

        public Darkling(EnemyBeyond.Darkling other) {
            super(other);
            middle = other.middle;
            lowerPossibleNipDmg = other.lowerPossibleNipDmg;
            upperPossibleNipDmg = other.upperPossibleNipDmg;
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.Darkling(this);
        }

        public int getLowerPossibleNipDmg() {
            return lowerPossibleNipDmg;
        }

        public int getUpperPossibleNipDmg() {
            return upperPossibleNipDmg;
        }

        public void setLowerPossibleNipDmg(int lowerPossibleNipDmg) {
            this.lowerPossibleNipDmg = lowerPossibleNipDmg;
        }

        public void setUpperPossibleNipDmg(int upperPossibleNipDmg) {
            this.upperPossibleNipDmg = upperPossibleNipDmg;
        }

        @Override public int damage(double n, GameState state) {
            int prevHealth = health;
            int dmg = super.damage(n, state);
            if (health <= 0 && prevHealth > 0) {
                move = REGROW;
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int prevHealth = health;
            super.nonAttackDamage(n, blockable, state);
            if (health <= 0 && prevHealth > 0) {
                move = REGROW;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == NIP) {
                state.enemyDoDamageToPlayer(this, lowerPossibleNipDmg + 2, 1);
            } else if (move == CHOMP) {
                state.enemyDoDamageToPlayer(this, 9, 2);
            } else if (move == HARDEN) {
                gainBlock(12);
                gainStrength(2);
            } else if (move == REGROW) {
            } else if (move == REINCARNATE) {
                health = properties.origHealth / 2;
                state.adjustEnemiesAlive(1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove = -1;
            if (move == REGROW) {
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
            if (lowerPossibleNipDmg != upperPossibleNipDmg && move == NIP) {
                int nip = random.nextInt(upperPossibleNipDmg - lowerPossibleNipDmg + 1, RandomGenCtx.EnemyChooseMove);
                // todo: call this at end of card played -> if darkling is weak and its attack is reduced, need to update range
                this.setPossibleNipDmg(state, state.enemyCalcDamageToPlayer(this, lowerPossibleNipDmg + nip + 2));
            }
        }

        public void setPossibleNipDmg(GameState state, int targetDmg) {
            for (int i = lowerPossibleNipDmg; i <= upperPossibleNipDmg; i++) {
                if (state.enemyCalcDamageToPlayer(this, i + 2) == targetDmg) {
                    lowerPossibleNipDmg = i;
                    break;
                }
            }
            for (int i = lowerPossibleNipDmg + 1; i <= upperPossibleNipDmg; i++) {
                if (state.enemyCalcDamageToPlayer(this, i + 2) != targetDmg) {
                    upperPossibleNipDmg = i - 1;
                    break;
                }
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == NIP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, lowerPossibleNipDmg + 2);
            } else if (move == CHOMP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "x2";
            } else if (move == HARDEN) {
                return "Gain 12 Block and 2 Strength";
            } else if (move == REGROW) {
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
            s = s.replaceFirst("hp=(\\d+)", "hp=$1/" + properties.origHealth);
            if (lowerPossibleNipDmg != upperPossibleNipDmg) {
                return s.subSequence(0, s.length() - 1) + ", nipDmg=" + lowerPossibleNipDmg + "-" + upperPossibleNipDmg + "}";
            } else {
                return s.subSequence(0, s.length() - 1) + ", nipDmg=" + lowerPossibleNipDmg + "}";
            }
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && lowerPossibleNipDmg == ((Darkling) o).lowerPossibleNipDmg && upperPossibleNipDmg == ((Darkling) o).upperPossibleNipDmg;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 3;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "3 input to keep track of Darkling Nip damage and max HP";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = (lowerPossibleNipDmg - 7) / 4.0f;
            input[idx + 1] = (upperPossibleNipDmg - 7) / 4.0f;
            input[idx + 2] = properties.origHealth / (float) properties.maxHealth;
            return 3;
        }
    }

    public static class Repulsor extends Enemy {
        private static final int BASH = 0;
        private static final int REPULSE = 1;

        public Repulsor() {
            this(38);
        }

        public Repulsor(int health) {
            super(health, 2, false);
        }

        public Repulsor(EnemyBeyond.Repulsor other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.Repulsor(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BASH) {
                state.enemyDoDamageToPlayer(this, 13, 1);
            } else if (move == REPULSE) {
                state.addCardToDeck(state.properties.dazedCardIdx);
                state.addCardToDeck(state.properties.dazedCardIdx);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move == BASH) {
                newMove = REPULSE;
            } else {
                int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                newMove = r < 80 ? REPULSE : BASH;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 13);
            } else if (move == REPULSE) {
                return "Shuffle 2 Dazed";
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 31 + random.nextInt(8, RandomGenCtx.Other);
        }

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new CardOther.Dazed()); }

        @Override public String getName() {
            return "Repulsor";
        }
    }

    public static class Exploder extends Enemy {
        private static final int SLAM_1 = 0;
        private static final int SLAM_2 = 1;
        private static final int EXPLODE = 2;

        public Exploder() {
            this(35);
        }

        public Exploder(int health) {
            super(health, 2, false);
        }

        public Exploder(EnemyBeyond.Exploder other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.Exploder(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SLAM_1 || move == SLAM_2) {
                state.enemyDoDamageToPlayer(this, 11, 1);
            } else if (move == EXPLODE) {
                state.enemyDoDamageToPlayer(this, 30, 1);
                health = 0;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (lastMove < 0) {
                move = SLAM_1;
            } else if (lastMove == SLAM_1) {
                move = SLAM_2;
            } else if (lastMove == SLAM_2) {
                move = EXPLODE;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SLAM_1 || move == SLAM_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11);
            } else if (move == EXPLODE) {
                return "Dies and Explode " + state.enemyCalcDamageToPlayer(this, 30);
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 30 + random.nextInt(6, RandomGenCtx.Other);
        }

        @Override public String getName() {
            return "Exploder";
        }
    }

    public static class Spiker extends Enemy {
        private static final int CUT = 0;
        private static final int SPIKE = 1;
        private int thorn = 7;

        public Spiker() {
            this(60);
        }

        public Spiker(int health) {
            super(health, 2, false);
        }

        public Spiker(EnemyBeyond.Spiker other) {
            super(other);
            thorn = other.thorn;
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.Spiker(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CUT) {
                state.enemyDoDamageToPlayer(this, 9, 1);
            } else if (move == SPIKE) {
                thorn += 2;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (thorn == 19) {
                move = CUT;
            } else if (lastMove == CUT) {
                move = SPIKE;
            } else {
                int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                move = r < 50 ? CUT : SPIKE;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CUT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9);
            } else if (move == SPIKE) {
                return "Increase Thorn By 2 ";
            }
            return "Unknown";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && thorn == ((EnemyBeyond.Spiker) o).thorn;
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 44 + random.nextInt(17, RandomGenCtx.Other);
        }

        @Override public String getName() {
            return "Spiker";
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of thorn for Spiker";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = (thorn - 7) / 12.0f;
            return 1;
        }

        public int getThorn() {
            return thorn;
        }
    }
}
