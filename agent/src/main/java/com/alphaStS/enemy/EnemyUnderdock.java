package com.alphaStS.enemy;

import com.alphaStS.*;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.random.RandomGen;
import com.alphaStS.random.RandomGenCtx;

import com.alphaStS.utils.Tuple;

public class EnemyUnderdock {
    public static class PhantasmalGardener extends Enemy {
        public static final int BITE = 0;
        public static final int LASH = 1;
        public static final int FLAIL = 2;
        public static final int ENLARGE = 3;

        private boolean tookDamageThisTurn;
        private int startingMove;

        public PhantasmalGardener() {
            this(32, BITE);
        }

        public PhantasmalGardener(int health, int startingMove) {
            super(health, 4, false);
            properties.canGainBlock = true;
            properties.canGainStrength = true;
            properties.isElite = true;
            this.startingMove = startingMove;
        }

        public PhantasmalGardener(PhantasmalGardener other) {
            super(other);
            this.tookDamageThisTurn = other.tookDamageThisTurn;
            this.startingMove = other.startingMove;
        }

        @Override public Enemy copy() {
            return new PhantasmalGardener(this);
        }

        @Override public int damage(double n, GameState state) {
            var dmg = super.damage(n, state);
            // todo: should only be after the card finished playing
            if (dmg > 0 && !tookDamageThisTurn) {
                tookDamageThisTurn = true;
                gainBlock(7);
            }
            return dmg;
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            tookDamageThisTurn = false;
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BITE) {
                state.enemyDoDamageToPlayer(this, 5, 1);
            } else if (move == LASH) {
                state.enemyDoDamageToPlayer(this, 7, 1);
            } else if (move == FLAIL) {
                state.enemyDoDamageToPlayer(this, 1, 3);
            } else if (move == ENLARGE) {
                gainStrength(3);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < 0) {
                move = startingMove;
            } else {
                int newMove = (move + 1) % 4;
                lastMove = move;
                move = newMove;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BITE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 5);
            } else if (move == LASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7);
            } else if (move == FLAIL) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 1) + "x3";
            } else if (move == ENLARGE) {
                return "Gain 3 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            int b = state.getSearchRandomGen().nextInt(2, RandomGenCtx.Other) + 1;
            if (training && b < 2) {
                health = (int) Math.round(((double) (health * b)) / 2);
            } else {
                health = 27 + state.getSearchRandomGen().nextInt(6, RandomGenCtx.RandomEnemyHealth, new Tuple<>(27, state));
            }
        }

        @Override public String getName() {
            return "Phantasmal Gardener";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return s.subSequence(0, s.length() - 1) + ", tookDamageThisTurn=" + tookDamageThisTurn + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && tookDamageThisTurn == ((PhantasmalGardener) o).tookDamageThisTurn;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 inputs to keep track whether block is gained this turn";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx++] = tookDamageThisTurn ? 0.5f : 0.0f;
            return 1;
        }
    }

    public static class TerrorEel extends Enemy {
        public static final int CRASH = 0;
        public static final int THRASH = 1;
        public static final int STUN = 2;
        public static final int TERROR = 3;

        private boolean stunned = false;
        private int vigor = 0;
        private int lastMoveBeforeStun = -1;

        public TerrorEel() {
            this(150);
        }

        public TerrorEel(int health) {
            super(health, 4, false);
            properties.entityProperty.changePlayerVulnerable = true;
            properties.isElite = true;
        }

        public TerrorEel(TerrorEel other) {
            super(other);
            this.stunned = other.stunned;
            this.vigor = other.vigor;
            this.lastMoveBeforeStun = other.lastMoveBeforeStun;
        }

        @Override public Enemy copy() {
            return new TerrorEel(this);
        }

        @Override public int damage(double n, GameState state) {
            var dmg = super.damage(n, state);
            // todo: should only be after the card finished playing
            if (health <= 75 && !stunned) {
                stunned = true;
                lastMoveBeforeStun = move;
                move = STUN;
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (health <= 75 && !stunned) {
                stunned = true;
                lastMoveBeforeStun = move;
                move = STUN;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CRASH) {
                state.enemyDoDamageToPlayer(this, 18 + vigor, 1);
                vigor = 0;
            } else if (move == THRASH) {
                state.enemyDoDamageToPlayer(this, 4 + vigor, 3);
                vigor += 6;
            } else if (move == STUN) {
            } else if (move == TERROR) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 99);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == STUN) {
                move = TERROR;
            } else if (move == TERROR) {
                move = lastMoveBeforeStun;
                lastMoveBeforeStun = -1;
            } else if (move == THRASH) {
                move = CRASH;
            } else {
                move = THRASH;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CRASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 18 + vigor);
            } else if (move == THRASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 4) + "x3";
            } else if (move == STUN) {
                return "Stunned";
            } else if (move == TERROR) {
                return "Vulnerable 99";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            int b = state.getSearchRandomGen().nextInt(6, RandomGenCtx.Other) + 1;
            if (training && b < 6) {
                health = (int) Math.round(((double) (health * b)) / 6);
            }
        }

        @Override public String getName() {
            return "Terror Eel";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return s.subSequence(0, s.length() - 1) + ", stunned=" + stunned + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && stunned == ((TerrorEel) o).stunned;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 4;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "4 inputs to keep track Terror Eel mechanics";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx++] = stunned ? 0.5f : 0.0f;
            input[idx++] = ((float) vigor) / 12.0f;
            input[idx++] = lastMoveBeforeStun == CRASH ? 0.5f : 0.0f;
            input[idx++] = lastMoveBeforeStun == THRASH ? 0.5f : 0.0f;
            return 4;
        }
    }

    public static class CorpseSlug extends Enemy {
        public static final int WHIP_SLAP = 0;
        public static final int GLOMP = 1;
        public static final int GOOP = 2;

        private int startingMove;

        public CorpseSlug() {
            this(32, WHIP_SLAP);
        }

        public CorpseSlug(int health, int startingMove) {
            super(health, 3, false);
            properties.canGainStrength = true;
            properties.canBeStunned = true;
            properties.entityProperty.changePlayerFrailed = true;
            this.startingMove = startingMove;
        }

        public CorpseSlug(CorpseSlug other) {
            super(other);
            this.startingMove = other.startingMove;
        }

        @Override public Enemy copy() {
            return new CorpseSlug(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == WHIP_SLAP) {
                state.enemyDoDamageToPlayer(this, 3, 2);
            } else if (move == GLOMP) {
                state.enemyDoDamageToPlayer(this, 9, 1);
            } else if (move == GOOP) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < 0) {
                move = startingMove;
            } else {
                int newMove = (move + 1) % 3;
                lastMove = move;
                move = newMove;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == WHIP_SLAP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 2) + "x2";
            } else if (move == GLOMP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9);
            } else if (move == GOOP) {
                return "Frail 2";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            health = 27 + state.getSearchRandomGen().nextInt(3, RandomGenCtx.RandomEnemyHealth, new Tuple<>(27, state));
        }

        @Override public String getName() {
            return "Corpse Slug";
        }
    }
}
