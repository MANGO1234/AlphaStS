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
                    move = REBIRTH;
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
                    move = REBIRTH;
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
            if (move == -1) {
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
                strength = 0;
                // todo: reset everything
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
