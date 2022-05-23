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
            super(health, 6);
            canGainStrength = true;
            moveHistory = new int[1];
        }

        public AwakenedOne(EnemyBeyond.AwakenedOne other) {
            this(other.health);
            setSharedFields(other);
        }

        @Override public Enemy copy() {
            return new EnemyBeyond.AwakenedOne(this);
        }

        @Override public void damage(int n, GameState state) {
            super.damage(n, state);
            if (!awakened && health <= 0) {
                awakened = true;
                move = REBIRTH;
            }
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (!awakened && health <= 0) {
                awakened = true;
                move = REBIRTH;
            }
        }

        @Override public void doMove(GameState state) {
            if (move == SLASH) {
                state.enemyDoDamageToPlayer(this, 20, 1);
            } else if (move == SOUL_STRIKE) {
                state.enemyDoDamageToPlayer(this, 6, 4);
            } else if (move == REBIRTH) {
                removeAllDebuffs();
                health = maxHealth;
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
            if (move == -1) {
                newMove = SLASH;
            } else if (!awakened) {
                int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                if (r < 25) {
                    newMove = SOUL_STRIKE;
                } else {
                    newMove = SLASH;
                }
            } else {
                if (move == REBIRTH) {
                    newMove = DARK_ECHO;
                } else {
                    int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                    if (r < 50) {
                        newMove = TACKLE;
                    } else {
                        newMove = SLUDGE;
                    }
                }
            }
            moveHistory[0] = move;
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
    }

}
