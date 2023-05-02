package com.alphaStS.enemy;

import com.alphaStS.*;

import java.util.List;

public class EnemyEnding {
    public static class SpireShield extends Enemy {
        private static final int BASH = 0;
        private static final int FORTIFY = 1;
        private static final int SMASH = 2;

        public SpireShield() {
            this(125);
        }

        public SpireShield(int health) {
            super(health, 3, true);
            artifact = 2;
            property.hasArtifact = true;
            property.canGainBlock = true;
            property.canGainStrength = true;
            property.changePlayerStrength = true;
            property.changePlayerFocus = true;
            property.isElite = true;
        }

        public SpireShield(SpireShield other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new SpireShield(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BASH) {
                state.enemyDoDamageToPlayer(this, 14, 1);
                if (state.getOrbs() != null) {
                    state.setIsStochastic();
                    if (state.getSearchRandomGen().nextInt(2, RandomGenCtx.ShieldAndSpire) == 0) {
                        state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_FOCUS, 1);
                    } else {
                        state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_STRENGTH, 1);
                    }
                } else {
                    state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_STRENGTH, 1);
                }
            } else if (move == FORTIFY) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemy.gainBlock(30);
                }
            } else if (move == SMASH) {
                state.enemyDoDamageToPlayer(this, 38, 1);
                gainBlock(99);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move == -1 || move == SMASH) {
                state.setIsStochastic();
                newMove = state.getSearchRandomGen().nextInt(2, RandomGenCtx.EnemyChooseMove);
            } else if (lastMove == -1 || lastMove == SMASH) {
                newMove = move == BASH ? FORTIFY : BASH;
            } else {
                newMove = SMASH;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14) + "+Debuff";
            } else if (move == FORTIFY) {
                return "Give All Enemies 30 Block";
            } else if (move == SMASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 38) + "+99 Block";
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(6, RandomGenCtx.Other) + 1;
            if (training && b < 6) {
                health = (int) Math.round(((double) (health * b)) / 6);
            } else {
                health = 125;
            }
        }

        @Override public String getName() {
            return "Spire Shield";
        }
    }

    public static class SpireSpear extends Enemy {
        private static final int BURN_STRIKE = 0;
        private static final int PIERCER = 1;
        private static final int SKEWER = 2;

        public SpireSpear() {
            this(180);
        }

        public SpireSpear(int health) {
            super(health, 3, true);
            artifact = 2;
            property.hasArtifact = true;
            property.canGainBlock = true;
            property.canGainStrength = true;
            property.isElite = true;
        }

        public SpireSpear(SpireSpear other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new SpireSpear(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BURN_STRIKE) {
                state.enemyDoDamageToPlayer(this, 6, 2);
                state.addCardToDeck(state.prop.burnCardIdx);
                state.addCardToDeck(state.prop.burnCardIdx);
                state.getDrawOrderForWrite().pushOnTop(state.prop.burnCardIdx);
                state.getDrawOrderForWrite().pushOnTop(state.prop.burnCardIdx);
            } else if (move == PIERCER) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemy.gainStrength(2);
                }
            } else if (move == SKEWER) {
                state.enemyDoDamageToPlayer(this, 10, 4);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move == -1) {
                newMove = BURN_STRIKE;
            } else if (lastMove == -1) {
                newMove = SKEWER;
            } else if (move == SKEWER) {
                state.setIsStochastic();
                newMove = state.getSearchRandomGen().nextInt(2, RandomGenCtx.EnemyChooseMove);
            } else if (lastMove == SKEWER) {
                newMove = move == BURN_STRIKE ? PIERCER : BURN_STRIKE;
            } else {
                newMove = SKEWER;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BURN_STRIKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6) + "x2+Put 2 Burns On Top Of Deck";
            } else if (move == PIERCER) {
                return "Give All Enemies 2 Strength";
            } else if (move == SKEWER) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10) + "x4";
            }
            return "Unknown";
        }

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new Card.Burn()); }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(9, RandomGenCtx.Other) + 1;
            if (training && b < 9) {
                health = (int) Math.round(((double) (health * b)) / 9);
            } else {
                health = 180;
            }
        }

        @Override public String getName() {
            return "Spire Spear";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.prop.registerCounter("Shield & Spear Facing", new GameProperties.CounterRegistrant() {
                @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
                    gameProperties.shieldAndSpireFacingIdx = idx;
                }
            }, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    int facing = state.getCounterForRead()[state.prop.shieldAndSpireFacingIdx];
                    if (facing == 1 || facing == 2) {
                        input[idx + facing - 1] = 0.5f;
                    }
                    return idx + 2;
                }

                @Override public int getInputLenDelta() {
                    return 2;
                }

                @Override public String getDisplayString(GameState state) {
                    int facing = state.getCounterForRead()[state.prop.shieldAndSpireFacingIdx];
                    if (facing == 1) {
                        return "Shield";
                    } else if (facing == 2) {
                        return "Spear";
                    } else {
                        return "N/A";
                    }
                }
            });
            state.addStartOfBattleHandler(new GameEventHandler() {
                @Override public void handle(GameState state) {
                    for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                        if (state.getEnemiesForRead().get(i).isAlive() && state.getEnemiesForRead().get(i) instanceof SpireSpear) {
                            state.getCounterForWrite()[state.prop.shieldAndSpireFacingIdx] = 2;
                        }
                    }
                }
            });
            state.addOnEnemyDeathHandler(new GameEventEnemyHandler() {
                @Override public void handle(GameState state2, EnemyReadOnly enemy) {
                    if (enemy instanceof EnemyEnding.SpireShield) {
                        state2.getCounterForWrite()[state2.prop.shieldAndSpireFacingIdx] = 2;
                    } else {
                        state2.getCounterForWrite()[state2.prop.shieldAndSpireFacingIdx] = 1;
                    }
                }
            });
        }
    }

    public static class CorruptHeart extends Enemy {
        private static final int DEBILITATE = 0;
        private static final int BLOOD_SHOT = 1;
        private static final int ECHO = 2;
        private static final int BUFF = 3;

        private int beatOfDeath = 2;
        private int invincible = 200;
        private int buffCount = 0;

        public int getInvincible() {
            return invincible;
        }

        public int getBeatOfDeath() {
            return beatOfDeath;
        }

        public int getBuffCount() {
            return buffCount;
        }

        public CorruptHeart() {
            this(800);
        }

        public CorruptHeart(int health) {
            super(health, 4, true);
            property.hasArtifact = true;
            property.canGainStrength = true;
            property.canVulnerable = true;
            property.canWeaken = true;
            property.canFrail = true;
            property.canDaze = true;
            property.isBoss = true;
        }

        public CorruptHeart(CorruptHeart other) {
            super(other);
            beatOfDeath = other.beatOfDeath;
            invincible = other.invincible;
            buffCount = other.buffCount;
        }

        @Override public Enemy copy() {
            return new CorruptHeart(this);
        }

        @Override public int damage(double n, GameState state) {
            int prevHp = health;
            int dmgDone = super.damage(n, state);
            if (dmgDone > 0) {
                if (invincible >= dmgDone) {
                    invincible -= dmgDone;
                } else {
                    health = prevHp - invincible;
                    invincible = 0;
                    dmgDone = invincible;
                }
            }
            return dmgDone;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int prevHp = health;
            super.nonAttackDamage(n, blockable, state);
            if (prevHp > health) {
                int dmg = prevHp - health;
                if (invincible >= dmg) {
                    invincible -= dmg;
                } else {
                    health = prevHp - invincible;
                    invincible = 0;
                }
            }
        }

        @Override public void startTurn(GameState state) {
            super.startTurn(state);
            invincible = 200;
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == DEBILITATE) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 2);
                state.addCardToDeck(state.prop.dazedCardIdx);
                state.addCardToDeck(state.prop.burnCardIdx);
                state.addCardToDeck(state.prop.voidCardIdx);
                state.addCardToDeck(state.prop.slimeCardIdx);
                state.addCardToDeck(state.prop.woundCardIdx);
            } else if (move == BLOOD_SHOT) {
                state.enemyDoDamageToPlayer(this, 2, 15);
            } else if (move == ECHO) {
                state.enemyDoDamageToPlayer(this, 45, 1);
            } else if (move == BUFF) {
                if (strength < 0) {
                    strength = 0;
                }
                gainStrength(2);
                if (buffCount == 0) {
                    artifact += 2;
                } else if (buffCount == 1) {
                    beatOfDeath++;
                } else if (buffCount == 2) {
                } else if (buffCount == 3) {
                    gainStrength(10);
                } else {
                    gainStrength(50);
                }
                buffCount++;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move == -1) {
                newMove = DEBILITATE;
            } else if (move == DEBILITATE || move == BUFF) {
                state.setIsStochastic();
                newMove = state.getSearchRandomGen().nextInt(2, RandomGenCtx.EnemyChooseMove) + 1;
            } else if (lastMove == DEBILITATE || lastMove == BUFF) {
                newMove = move == BLOOD_SHOT ? ECHO : BLOOD_SHOT;
            } else {
                newMove = BUFF;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == DEBILITATE) {
                return "Vulnerable 2+Weak 2+Frail 2+Add Statuses To Deck";
            } else if (move == BLOOD_SHOT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 2) + "x15";
            } else if (move == ECHO) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 45);
            } else if (move == BUFF) {
                if (buffCount == 0) {
                    return "Buff (2 Artifact)";
                } else if (buffCount == 1) {
                    return "Buff (+1 Beat Of Death)";
                } else if (buffCount == 2) {
                    return "Buff (Painful Stab)";
                } else if (buffCount == 3) {
                    return "Buff (+10 Extra Strength)";
                } else {
                    return "Buff (+50 Extra Strength)";
                }
            }
            return "Unknown";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned) {
                    for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                        if (state.getEnemiesForRead().get(i) instanceof CorruptHeart heart) {
                            state.doNonAttackDamageToPlayer(heart.beatOfDeath, true, heart);
                            break;
                        }
                    }
                }
            });
            state.addOnDamageHandler(new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (source instanceof CorruptHeart && ((CorruptHeart) source).buffCount >= 2 && isAttack && damageDealt > 0) {
                        state.addCardToDiscard(state.prop.woundCardIdx);
                    }
                }
            });
            state.prop.isHeartFight = true;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new Card.Burn(), new Card.Wound(), new Card.Dazed(), new Card.Slime(), new Card.Void()); }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return s.subSequence(0, s.length() - 1) + ", invincible=" + invincible + ", buffCount=" + buffCount + "}";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
                health = (int) Math.round(health * difficulty / 40);
            } else {
                health = 800;
            }
        }

        @Override public int getMaxRandomizeDifficulty() {
            return 40;
        }

        @Override public String getName() {
            return "Corrupt Heart";
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 7;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "7 inputs to keep track of Corrupt Heart Invincible Count, Beat Of Death, and Buff Count";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = invincible / 200.0f;
            input[idx + 1] = beatOfDeath / 3.0f;
            if (buffCount > 0) {
                if (buffCount <= 5) {
                    input[idx + buffCount - 1] = 0.5f;
                } else {
                    input[idx + 4] = 0.5f;
                }
            }
            return 7;
        }
    }
}
