package com.alphaStS.enemy;

import com.alphaStS.*;
import com.alphaStS.card.Card;
import com.alphaStS.card.CardOther2;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.eventHandler.GameEventEnemyHandler;
import com.alphaStS.random.RandomGen;
import com.alphaStS.random.RandomGenCtx;

import java.util.List;
import com.alphaStS.utils.Tuple;

import static com.alphaStS.enemy.EnemyUtils.*;

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
            if (dmg > 0 && !tookDamageThisTurn) {
                tookDamageThisTurn = true;
            }
            return dmg;
        }

        @Override public void react(GameState state, Card card) {
            if (tookDamageThisTurn) {
                gainBlock(7);
            }
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
        }

        @Override public Enemy copy() {
            return new TerrorEel(this);
        }

        @Override public int damage(double n, GameState state) {
            var dmg = super.damage(n, state);
            // todo: should only be after the card finished playing
            if (health <= 75 && !stunned) {
                stunned = true;
                move = STUN;
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (health <= 75 && !stunned) {
                stunned = true;
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
            if (move < 0) {
                move = CRASH;
            } else if (move == STUN) {
                move = TERROR;
            } else if (move == TERROR) {
                move = CRASH;
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
                return "Attack " + state.enemyCalcDamageToPlayer(this, 4 + vigor) + "x3";
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
            return s.subSequence(0, s.length() - 1) + ", stunned=" + stunned + ", vigor=" + vigor + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && stunned == ((TerrorEel) o).stunned && vigor == ((TerrorEel) o).vigor;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 2;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "2 inputs to keep track whether Terror Eel was stunned and its vigor";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = stunned ? 0.5f : 0.0f;
            input[idx + 1] = vigor / 12.0f;
            return 2;
        }
    }

    public static class CorpseSlug extends Enemy {
        public static final int WHIP_SLAP = 0;
        public static final int GLOMP = 1;
        public static final int GOOP = 2;
        public static final int STUNNED = 3;

        private int startingMove;

        public CorpseSlug() {
            this(29, WHIP_SLAP);
        }

        public CorpseSlug(int health, int startingMove) {
            super(health, 4, true);
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

        public void ravenousStun() {
            lastMove = move;
            move = STUNNED;
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < 0) {
                move = startingMove;
            } else if (move == STUNNED) {
                move = lastMove;
                lastMove = STUNNED;
            } else {
                int newMove = (move + 1) % 3;
                lastMove = move;
                move = newMove;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == WHIP_SLAP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 3) + "x2";
            } else if (move == GLOMP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9);
            } else if (move == GOOP) {
                return "Frail 2";
            } else if (move == STUNNED) {
                return "Stunned";
            }
            return "Unknown";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnEnemyDeathHandler("CorpseSlugRavenous", new GameEventEnemyHandler() {
                @Override public void handle(GameState state, EnemyReadOnly deadEnemy) {
                    for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                        if (state.getEnemiesForRead().get(i).isAlive() && state.getEnemiesForRead().get(i) instanceof CorpseSlug) {
                            var slug = (CorpseSlug) state.getEnemiesForWrite().getForWrite(i);
                            slug.gainStrength(5);
                            slug.ravenousStun();
                        }
                    }
                }
            });
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 27, 29);
        }

        @Override public String getName() {
            return "Corpse Slug";
        }
    }

    public static class CalcifiedCultist extends Enemy {
        public static final int INCANTATION = 0;
        public static final int DARK_STRIKE = 1;

        public CalcifiedCultist() {
            this(42);
        }

        public CalcifiedCultist(int health) {
            super(health, 2, false);
            properties.canGainStrength = true;
        }

        public CalcifiedCultist(CalcifiedCultist other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new CalcifiedCultist(this);
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            if (move == DARK_STRIKE) {
                gainStrength(2);
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == DARK_STRIKE) {
                state.enemyDoDamageToPlayer(this, 11, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < DARK_STRIKE) {
                move++;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == INCANTATION) {
                return "Ritual 2";
            } else if (move == DARK_STRIKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 39, 42);
        }

        @Override public String getName() {
            return "Calcified Cultist";
        }
    }

    public static class DampCultist extends Enemy {
        public static final int INCANTATION = 0;
        public static final int DARK_STRIKE = 1;

        public DampCultist() {
            this(54);
        }

        public DampCultist(int health) {
            super(health, 2, false);
            properties.canGainStrength = true;
        }

        public DampCultist(DampCultist other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new DampCultist(this);
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            if (move == DARK_STRIKE) {
                gainStrength(6);
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == DARK_STRIKE) {
                state.enemyDoDamageToPlayer(this, 3, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < DARK_STRIKE) {
                move++;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == INCANTATION) {
                return "Ritual 6";
            } else if (move == DARK_STRIKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 3);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 52, 54);
        }

        @Override public String getName() {
            return "Damp Cultist";
        }
    }

    public static class GremlinMerc extends Enemy {
        public static final int GIMME = 0;
        public static final int DOUBLE_SMASH = 1;
        public static final int HEHE = 2;

        public GremlinMerc() {
            this(53);
        }

        public GremlinMerc(int health) {
            super(health, 3, false);
            properties.canGainStrength = true;
            properties.entityProperty.changePlayerWeakened = true;
            properties.entityProperty.canSummon = true;
        }

        public GremlinMerc(GremlinMerc other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new GremlinMerc(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == GIMME) {
                state.enemyDoDamageToPlayer(this, 8, 2);
                // TODO: Implement Thievery 20 stolen gold tracking.
            } else if (move == DOUBLE_SMASH) {
                state.enemyDoDamageToPlayer(this, 7, 2);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2);
                // TODO: Implement Thievery 20 stolen gold tracking.
            } else if (move == HEHE) {
                state.enemyDoDamageToPlayer(this, 9, 1);
                gainStrength(2);
                // TODO: Implement Thievery 20 stolen gold tracking.
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < 0) {
                move = GIMME;
            } else {
                lastMove = move;
                move = (move + 1) % 3;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == GIMME) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "x2";
            } else if (move == DOUBLE_SMASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "x2+Weak 2";
            } else if (move == HEHE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "+Strength 2";
            }
            return "Unknown";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnEnemyDeathHandler("GremlinMercSurprise", new GameEventEnemyHandler() {
                @Override public void handle(GameState state, EnemyReadOnly deadEnemy) {
                    if (deadEnemy instanceof GremlinMerc) {
                        reviveFirstDeadEnemy(state, GremlinMercSneakyGremlin.class);
                        reviveFirstDeadEnemy(state, GremlinMercFatGremlin.class);
                        // TODO: Transfer stolen gold to Fat Gremlin via Heist.
                    }
                }
            });
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 51, 53);
        }

        @Override public String getName() {
            return "Gremlin Merc";
        }
    }

    public static class GremlinMercFatGremlin extends Enemy {
        public static final int SPAWNED = 0;
        public static final int FLEE = 1;

        public GremlinMercFatGremlin() {
            this(18);
        }

        public GremlinMercFatGremlin(int health) {
            super(health, 2, false);
            properties.isMinion = true;
        }

        public GremlinMercFatGremlin(GremlinMercFatGremlin other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new GremlinMercFatGremlin(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == FLEE) {
                health = 0;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < FLEE) {
                move++;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SPAWNED) {
                return "Stunned";
            } else if (move == FLEE) {
                return "Escape";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 14, 18);
        }

        @Override public String getName() {
            return "Fat Gremlin";
        }
    }

    public static class GremlinMercSneakyGremlin extends Enemy {
        public static final int SPAWNED = 0;
        public static final int TACKLE = 1;

        public GremlinMercSneakyGremlin() {
            this(15);
        }

        public GremlinMercSneakyGremlin(int health) {
            super(health, 2, false);
            properties.isMinion = true;
        }

        public GremlinMercSneakyGremlin(GremlinMercSneakyGremlin other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new GremlinMercSneakyGremlin(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == TACKLE) {
                state.enemyDoDamageToPlayer(this, 10, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < TACKLE) {
                move++;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SPAWNED) {
                return "Stunned";
            } else if (move == TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 11, 15);
        }

        @Override public String getName() {
            return "Sneaky Gremlin";
        }
    }

    public static class HauntedShip extends Enemy {
        public static final int HAUNT = 0;
        public static final int RAMMING_SPEED = 1;
        public static final int SWIPE = 2;
        public static final int STOMP = 3;

        public HauntedShip() {
            this(67);
        }

        public HauntedShip(int health) {
            super(health, 4, false);
            properties.entityProperty.changePlayerWeakened = true;
        }

        public HauntedShip(HauntedShip other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new HauntedShip(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == HAUNT) {
                state.addCardToDiscard(properties.generatedCardIdx);
                state.addCardToDiscard(properties.generatedCardIdx);
                state.addCardToDiscard(properties.generatedCardIdx);
                state.addCardToDiscard(properties.generatedCardIdx);
                state.addCardToDiscard(properties.generatedCardIdx);
            } else if (move == RAMMING_SPEED) {
                state.enemyDoDamageToPlayer(this, 11, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
            } else if (move == SWIPE) {
                state.enemyDoDamageToPlayer(this, 14, 1);
            } else if (move == STOMP) {
                state.enemyDoDamageToPlayer(this, 5, 3);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move < 0) {
                newMove = HAUNT;
            } else if (move == HAUNT || move == SWIPE || move == STOMP) {
                newMove = RAMMING_SPEED;
            } else {
                state.setIsStochastic();
                newMove = random.nextInt(2, RandomGenCtx.EnemyChooseMove, this) == 0 ? SWIPE : STOMP;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == HAUNT) {
                return "Dazed 5";
            } else if (move == RAMMING_SPEED) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11) + "+Weak 1";
            } else if (move == SWIPE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14);
            } else if (move == STOMP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 5) + "x3";
            }
            return "Unknown";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.Dazed());
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 67, 67);
        }

        @Override public String getName() {
            return "Haunted Ship";
        }
    }

    public static class LagavulinMatriarch extends Enemy {
        public static final int SLEEP_1 = 0;
        public static final int SLEEP_2 = 1;
        public static final int SLEEP_3 = 2;
        public static final int SLASH = 3;
        public static final int DISEMBOWEL = 4;
        public static final int SLASH_2 = 5;
        public static final int SOUL_SIPHON = 6;

        public LagavulinMatriarch() {
            this(233);
        }

        public LagavulinMatriarch(int health) {
            super(health, 7, false);
            properties.isBoss = true;
            properties.canGainBlock = true;
            properties.canGainPlating = true;
            properties.canGainStrength = true;
            properties.entityProperty.changePlayerStrength = true;
            properties.entityProperty.changePlayerDexterity = true;
            setPlating(12);
        }

        public LagavulinMatriarch(LagavulinMatriarch other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new LagavulinMatriarch(this);
        }

        private boolean isAsleep() {
            return move == SLEEP_1 || move == SLEEP_2 || move == SLEEP_3;
        }

        private void wakeUp() {
            setPlating(0);
            move = SLASH;
        }

        @Override public int damage(double n, GameState state) {
            int dmg = super.damage(n, state);
            if (isAsleep() && dmg > 0) {
                wakeUp();
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int dmg = blockable ? Math.max(0, n - block) : n;
            super.nonAttackDamage(n, blockable, state);
            if (isAsleep() && dmg > 0) {
                wakeUp();
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SLASH) {
                state.enemyDoDamageToPlayer(this, 21, 1);
            } else if (move == DISEMBOWEL) {
                state.enemyDoDamageToPlayer(this, 10, 2);
            } else if (move == SLASH_2) {
                state.enemyDoDamageToPlayer(this, 14, 1);
                gainBlock(14);
            } else if (move == SOUL_SIPHON) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_STRENGTH, 2);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_DEXTERITY, 2);
                gainStrength(2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = SLEEP_1;
            } else if (move == SLEEP_1) {
                move = SLEEP_2;
            } else if (move == SLEEP_2) {
                move = SLEEP_3;
            } else if (move == SLEEP_3) {
                wakeUp();
            } else if (move == SOUL_SIPHON) {
                move = SLASH;
            } else {
                move++;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SLEEP_1 || move == SLEEP_2 || move == SLEEP_3) {
                return "Asleep";
            } else if (move == SLASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 21);
            } else if (move == DISEMBOWEL) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10) + "x2";
            } else if (move == SLASH_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14) + "+Block 14";
            } else if (move == SOUL_SIPHON) {
                return "-2 Strength+-2 Dexterity+Strength 2";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 233, 233);
        }

        @Override public String getName() {
            return "Lagavulin Matriarch";
        }
    }

    public static class LivingFog extends Enemy {
        public static final int ADVANCED_GAS = 0;
        public static final int BLOAT = 1;
        public static final int SUPER_GAS_BLAST = 2;

        public LivingFog() {
            this(82);
        }

        public LivingFog(int health) {
            super(health, 3, false);
            properties.entityProperty.canSummon = true;
        }

        public LivingFog(LivingFog other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new LivingFog(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == ADVANCED_GAS) {
                state.enemyDoDamageToPlayer(this, 9, 1);
                // TODO: Implement Smoggy: player can only play 1 Skill per turn.
            } else if (move == BLOAT) {
                state.enemyDoDamageToPlayer(this, 6, 1);
                reviveFirstDeadEnemy(state, GasBomb.class);
            } else if (move == SUPER_GAS_BLAST) {
                state.enemyDoDamageToPlayer(this, 9, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < 0) {
                move = ADVANCED_GAS;
            } else if (move == ADVANCED_GAS || move == SUPER_GAS_BLAST) {
                lastMove = move;
                move = BLOAT;
            } else {
                lastMove = move;
                move = SUPER_GAS_BLAST;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == ADVANCED_GAS) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "+Smoggy 1";
            } else if (move == BLOAT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6) + "+Summon Gas Bomb";
            } else if (move == SUPER_GAS_BLAST) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 82, 82);
        }

        @Override public String getName() {
            return "Living Fog";
        }
    }

    public static class PunchConstruct extends Enemy {
        public static final int READY = 0;
        public static final int STRONG_PUNCH = 1;
        public static final int FAST_PUNCH = 2;

        public PunchConstruct() {
            this(60);
        }

        public PunchConstruct(int health) {
            super(health, 3, false);
            artifact = 1;
            properties.canGainBlock = true;
            properties.entityProperty.changePlayerWeakened = true;
        }

        public PunchConstruct(PunchConstruct other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new PunchConstruct(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == READY) {
                gainBlock(10);
            } else if (move == STRONG_PUNCH) {
                state.enemyDoDamageToPlayer(this, 16, 1);
            } else if (move == FAST_PUNCH) {
                state.enemyDoDamageToPlayer(this, 6, 2);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 3);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == READY) {
                return "Block 10";
            } else if (move == STRONG_PUNCH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 16);
            } else if (move == FAST_PUNCH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6) + "x2+Weak 1";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 60, 60);
        }

        @Override public String getName() {
            return "Punch Construct";
        }
    }

    public static class Seapunk extends Enemy {
        public static final int SEA_KICK = 0;
        public static final int SPINNING_KICK = 1;
        public static final int BUBBLE_BURP = 2;

        public Seapunk() {
            this(49);
        }

        public Seapunk(int health) {
            super(health, 3, false);
            properties.canGainBlock = true;
            properties.canGainStrength = true;
        }

        public Seapunk(Seapunk other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Seapunk(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SEA_KICK) {
                state.enemyDoDamageToPlayer(this, 13, 1);
            } else if (move == SPINNING_KICK) {
                state.enemyDoDamageToPlayer(this, 2, 4);
            } else if (move == BUBBLE_BURP) {
                gainBlock(8);
                gainStrength(2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 3);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SEA_KICK) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 13);
            } else if (move == SPINNING_KICK) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 2) + "x4";
            } else if (move == BUBBLE_BURP) {
                return "Block 8+Strength 2";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 47, 49);
        }

        @Override public String getName() {
            return "Seapunk";
        }
    }

    public static class SewerClam extends Enemy {
        public static final int JET = 0;
        public static final int PRESSURIZE = 1;

        public SewerClam() {
            this(58);
        }

        public SewerClam(int health) {
            super(health, 2, false);
            setPlating(9);
            properties.canGainBlock = true;
            properties.canGainStrength = true;
        }

        public SewerClam(SewerClam other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new SewerClam(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == JET) {
                state.enemyDoDamageToPlayer(this, 11, 1);
            } else if (move == PRESSURIZE) {
                gainStrength(4);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = JET;
            } else {
                move = move == JET ? PRESSURIZE : JET;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == JET) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11);
            } else if (move == PRESSURIZE) {
                return "Strength 4";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 58, 58);
        }

        @Override public String getName() {
            return "Sewer Clam";
        }
    }

    public static class SkulkingColony extends Enemy {
        public static final int ZOOM_1 = 0;
        public static final int ZOOM_2 = 1;
        public static final int INERTIA = 2;
        public static final int PIERCING_STABS = 3;

        private int damageTakenThisTurn;

        public SkulkingColony() {
            this(80);
        }

        public SkulkingColony(int health) {
            super(health, 4, false);
            properties.isElite = true;
            properties.canGainBlock = true;
            properties.canGainStrength = true;
        }

        public SkulkingColony(SkulkingColony other) {
            super(other);
            damageTakenThisTurn = other.damageTakenThisTurn;
        }

        @Override public Enemy copy() {
            return new SkulkingColony(this);
        }

        @Override public int damage(double n, GameState state) {
            int prevHealth = health;
            int dmg = super.damage(n, state);
            capTurnDamage(prevHealth);
            return Math.min(dmg, Math.max(0, prevHealth - health));
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int prevHealth = health;
            super.nonAttackDamage(n, blockable, state);
            capTurnDamage(prevHealth);
        }

        private void capTurnDamage(int prevHealth) {
            int hpLost = Math.max(0, prevHealth - health);
            int allowed = Math.max(0, 20 - damageTakenThisTurn);
            if (hpLost > allowed) {
                setHealth(prevHealth - allowed);
                hpLost = allowed;
            }
            damageTakenThisTurn += hpLost;
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            damageTakenThisTurn = 0;
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == ZOOM_1 || move == ZOOM_2) {
                state.enemyDoDamageToPlayer(this, 16, 1);
            } else if (move == INERTIA) {
                state.enemyDoDamageToPlayer(this, 11, 1);
                gainStrength(4);
            } else if (move == PIERCING_STABS) {
                state.enemyDoDamageToPlayer(this, 8, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 4);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == ZOOM_1 || move == ZOOM_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 16);
            } else if (move == INERTIA) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11) + "+Strength 4";
            } else if (move == PIERCING_STABS) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "x2";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 80, 80);
        }

        @Override public String getName() {
            return "Skulking Colony";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return s.subSequence(0, s.length() - 1) + ", damageTakenThisTurn=" + damageTakenThisTurn + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && damageTakenThisTurn == ((SkulkingColony) o).damageTakenThisTurn;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Skulking Colony Hardened Shell";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = damageTakenThisTurn / 20.0f;
            return 1;
        }
    }

    public static class SludgeSpinner extends Enemy {
        public static final int OIL_SPRAY = 0;
        public static final int SLAM = 1;
        public static final int RAGE = 2;

        public SludgeSpinner() {
            this(42);
        }

        public SludgeSpinner(int health) {
            super(health, 3, false);
            properties.canGainStrength = true;
            properties.entityProperty.changePlayerWeakened = true;
        }

        public SludgeSpinner(SludgeSpinner other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new SludgeSpinner(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == OIL_SPRAY) {
                state.enemyDoDamageToPlayer(this, 9, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
            } else if (move == SLAM) {
                state.enemyDoDamageToPlayer(this, 12, 1);
            } else if (move == RAGE) {
                state.enemyDoDamageToPlayer(this, 7, 1);
                gainStrength(3);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = OIL_SPRAY;
                return;
            }
            state.setIsStochastic();
            int r = random.nextInt(2, RandomGenCtx.EnemyChooseMove, this);
            if (move == OIL_SPRAY) {
                move = r == 0 ? SLAM : RAGE;
            } else if (move == SLAM) {
                move = r == 0 ? OIL_SPRAY : RAGE;
            } else {
                move = r == 0 ? OIL_SPRAY : SLAM;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == OIL_SPRAY) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "+Weak 1";
            } else if (move == SLAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12);
            } else if (move == RAGE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "+Strength 3";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 41, 42);
        }

        @Override public String getName() {
            return "Sludge Spinner";
        }
    }

    public static class SoulFysh extends Enemy {
        public static final int BECKON = 0;
        public static final int DE_GAS = 1;
        public static final int GAZE = 2;
        public static final int FADE = 3;
        public static final int SCREAM = 4;

        private int intangible;

        public SoulFysh() {
            this(221);
        }

        public SoulFysh(int health) {
            super(health, 5, false);
            properties.isBoss = true;
            properties.entityProperty.changePlayerVulnerable = true;
        }

        public SoulFysh(SoulFysh other) {
            super(other);
            intangible = other.intangible;
        }

        @Override public Enemy copy() {
            return new SoulFysh(this);
        }

        @Override public int damage(double n, GameState state) {
            int prevHealth = health;
            int dmg = super.damage(n, state);
            if (intangible > 0 && prevHealth - health > 1) {
                if (state.properties.boot != null && state.properties.boot.isRelicEnabledInScenario(state)) {
                    if (prevHealth - health > 5) {
                        setHealth(prevHealth - 5);
                        return 5;
                    }
                } else {
                    setHealth(prevHealth - 1);
                    return 1;
                }
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int prevHealth = health;
            super.nonAttackDamage(n, blockable, state);
            if (intangible > 0 && prevHealth - health > 1) {
                setHealth(prevHealth - 1);
            }
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            if (turnNum > 0 && intangible > 0) {
                intangible--;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BECKON) {
                state.addCardToDeck(properties.generatedCardIdx);
                state.addCardToDiscard(properties.generatedCardIdx);
            } else if (move == DE_GAS) {
                state.enemyDoDamageToPlayer(this, 17, 1);
            } else if (move == GAZE) {
                state.enemyDoDamageToPlayer(this, 8, 1);
                state.addCardToDiscard(properties.generatedCardIdx);
            } else if (move == FADE) {
                intangible += 2;
            } else if (move == SCREAM) {
                state.enemyDoDamageToPlayer(this, 12, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 3);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 5);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BECKON) {
                return "Beckon 2";
            } else if (move == DE_GAS) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 17);
            } else if (move == GAZE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "+Beckon 1";
            } else if (move == FADE) {
                return "Intangible 2";
            } else if (move == SCREAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12) + "+Vulnerable 3";
            }
            return "Unknown";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.Beckon());
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 221, 221);
        }

        @Override public String getName() {
            return "Soul Fysh";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            if (intangible <= 0) {
                return s;
            }
            return s.subSequence(0, s.length() - 1) + ", intangible=" + intangible + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && intangible == ((SoulFysh) o).intangible;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Soul Fysh Intangible";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = intangible / 2.0f;
            return 1;
        }
    }

    public static class Toadpole extends Enemy {
        public static final int WHIRL = 0;
        public static final int SPIKEN = 1;
        public static final int SPIKE_SPIT = 2;

        private int thorn;
        private int startingMove;

        public Toadpole() {
            this(26, 0);
        }

        public Toadpole(int health) {
            this(health, 0);
        }

        public Toadpole(int health, int startingMove) {
            super(health, 3, false);
            this.startingMove = startingMove;
        }

        public Toadpole(Toadpole other) {
            super(other);
            thorn = other.thorn;
            startingMove = other.startingMove;
        }

        @Override public Enemy copy() {
            return new Toadpole(this);
        }

        @Override public int damage(double n, GameState state) {
            int dmg = super.damage(n, state);
            if (dmg > 0 && thorn > 0) {
                state.doNonAttackDamageToPlayer(thorn, true, this);
            }
            return dmg;
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SPIKE_SPIT) {
                state.enemyDoDamageToPlayer(this, 4, 3);
                thorn = Math.max(0, thorn - 2);
            } else if (move == WHIRL) {
                state.enemyDoDamageToPlayer(this, 8, 1);
            } else if (move == SPIKEN) {
                thorn += 2;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = move < 0 ? startingMove : nextFixedMove(move, 3);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SPIKE_SPIT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 4) + "x3-Thorns 2";
            } else if (move == WHIRL) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8);
            } else if (move == SPIKEN) {
                return "Thorns 2";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 22, 26);
        }

        @Override public String getName() {
            return "Toadpole";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return s.subSequence(0, s.length() - 1) + ", thorn=" + thorn + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && thorn == ((Toadpole) o).thorn && startingMove == ((Toadpole) o).startingMove;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Toadpole Thorns";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = thorn / 10.0f;
            return 1;
        }
    }

    public static class TwoTailedRat extends Enemy {
        public static final int SCRATCH = 0;
        public static final int DISEASE_BITE = 1;
        public static final int SCREECH = 2;
        public static final int CALL_FOR_BACKUP = 3;

        private int startingMove;
        private int turnsTaken;
        private boolean summonedBackup;

        public TwoTailedRat() {
            this(22, SCRATCH);
        }

        public TwoTailedRat(int health) {
            this(health, SCRATCH);
        }

        public TwoTailedRat(int health, int startingMove) {
            super(health, 4, false);
            this.startingMove = startingMove;
            properties.entityProperty.changePlayerFrailed = true;
            properties.entityProperty.canSummon = true;
        }

        public TwoTailedRat(TwoTailedRat other) {
            super(other);
            startingMove = other.startingMove;
            turnsTaken = other.turnsTaken;
            summonedBackup = other.summonedBackup;
        }

        @Override public Enemy copy() {
            return new TwoTailedRat(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SCRATCH) {
                state.enemyDoDamageToPlayer(this, 9, 1);
            } else if (move == DISEASE_BITE) {
                state.enemyDoDamageToPlayer(this, 7, 1);
            } else if (move == SCREECH) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 1);
            } else if (move == CALL_FOR_BACKUP) {
                summonedBackup = true;
                reviveFirstDeadEnemy(state, TwoTailedRat.class);
            }
            turnsTaken++;
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = startingMove;
                return;
            }
            state.setIsStochastic();
            boolean canCallBackup = !summonedBackup && turnsTaken >= 2 && countGroupSummons(state) < 3 && hasDeadRat(state);
            // TODO: Replace uniform selection if exact Call for Backup probability becomes known.
            int legalMoves = getLegalMoveCount(canCallBackup);
            int r = random.nextInt(legalMoves, RandomGenCtx.EnemyChooseMove, this);
            int idx = 0;
            for (int candidate = 0; candidate <= CALL_FOR_BACKUP; candidate++) {
                if (candidate == lastMove || candidate == CALL_FOR_BACKUP && !canCallBackup) {
                    continue;
                }
                if (idx == r) {
                    move = candidate;
                    return;
                }
                idx++;
            }
            move = SCRATCH;
        }

        private int getLegalMoveCount(boolean canCallBackup) {
            int legalMoves = canCallBackup ? 4 : 3;
            if (lastMove >= SCRATCH && lastMove <= SCREECH) {
                legalMoves--;
            }
            return legalMoves;
        }

        private int countGroupSummons(GameState state) {
            int count = 0;
            for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                if (state.getEnemiesForRead().get(i) instanceof TwoTailedRat rat && rat.summonedBackup) {
                    count++;
                }
            }
            return count;
        }

        private boolean hasDeadRat(GameState state) {
            for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                if (state.getEnemiesForRead().get(i) instanceof TwoTailedRat rat && !rat.isAlive()) {
                    return true;
                }
            }
            return false;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SCRATCH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9);
            } else if (move == DISEASE_BITE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7);
            } else if (move == SCREECH) {
                return "Frail 1";
            } else if (move == CALL_FOR_BACKUP) {
                return "Summon Two-Tailed Rat";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 18, 22);
        }

        @Override public String getName() {
            return "Two-Tailed Rat";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return s.subSequence(0, s.length() - 1) + ", turnsTaken=" + turnsTaken +
                    ", summonedBackup=" + summonedBackup + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && startingMove == ((TwoTailedRat) o).startingMove &&
                    turnsTaken == ((TwoTailedRat) o).turnsTaken && summonedBackup == ((TwoTailedRat) o).summonedBackup;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 2;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "2 inputs to keep track of Two-Tailed Rat turns and summon usage";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = turnsTaken / 4.0f;
            input[idx + 1] = summonedBackup ? 0.5f : 0.0f;
            return 2;
        }
    }

    public static class WaterfallGiant extends Enemy {
        public static final int PRESSURIZE = 0;
        public static final int STOMP = 1;
        public static final int RAM = 2;
        public static final int SIPHON = 3;
        public static final int PRESSURE_GUN = 4;
        public static final int PRESSURE_UP = 5;
        public static final int ABOUT_TO_BLOW = 6;
        public static final int EXPLODE = 7;

        private int steamEruption;
        private int pressureGunCount;

        public WaterfallGiant() {
            this(250);
        }

        public WaterfallGiant(int health) {
            super(health, 8, false);
            properties.isBoss = true;
            properties.canGainStrength = true;
            properties.entityProperty.changePlayerWeakened = true;
        }

        public WaterfallGiant(WaterfallGiant other) {
            super(other);
            steamEruption = other.steamEruption;
            pressureGunCount = other.pressureGunCount;
        }

        @Override public Enemy copy() {
            return new WaterfallGiant(this);
        }

        @Override public int damage(double n, GameState state) {
            if (move == ABOUT_TO_BLOW || move == EXPLODE) {
                return 0;
            }
            int prevHealth = health;
            int dmg = super.damage(n, state);
            interceptDeath(prevHealth);
            return Math.min(dmg, Math.max(0, prevHealth - health));
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            if (move == ABOUT_TO_BLOW || move == EXPLODE) {
                return;
            }
            int prevHealth = health;
            super.nonAttackDamage(n, blockable, state);
            interceptDeath(prevHealth);
        }

        private void interceptDeath(int prevHealth) {
            if (prevHealth > 0 && health <= 0 && steamEruption > 0) {
                health = 1;
                move = ABOUT_TO_BLOW;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == PRESSURIZE) {
                steamEruption += 20;
            } else if (move == STOMP) {
                state.enemyDoDamageToPlayer(this, 16, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
                steamEruption += 3;
            } else if (move == RAM) {
                state.enemyDoDamageToPlayer(this, 11, 1);
                steamEruption += 3;
            } else if (move == SIPHON) {
                heal(15);
                steamEruption += 3;
            } else if (move == PRESSURE_GUN) {
                state.enemyDoDamageToPlayer(this, getPressureGunDamage(), 1);
                pressureGunCount++;
                steamEruption += 3;
            } else if (move == PRESSURE_UP) {
                state.enemyDoDamageToPlayer(this, 14, 1);
                steamEruption += 3;
            } else if (move == ABOUT_TO_BLOW) {
                move = EXPLODE;
            } else if (move == EXPLODE) {
                state.enemyDoDamageToPlayer(this, steamEruption, 1);
                steamEruption = 0;
                health = 0;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move == ABOUT_TO_BLOW || move == EXPLODE) {
                return;
            } else if (move < 0) {
                move = PRESSURIZE;
            } else if (move == PRESSURIZE || move == PRESSURE_UP) {
                move = STOMP;
            } else {
                move = nextFixedMove(move, 6);
            }
        }

        private int getPressureGunDamage() {
            return 23 + pressureGunCount * 5;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == PRESSURIZE) {
                return "Steam Eruption 20";
            } else if (move == STOMP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 16) + "+Weak 1+Steam Eruption 3";
            } else if (move == RAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11) + "+Steam Eruption 3";
            } else if (move == SIPHON) {
                return "Heal 15+Steam Eruption 3";
            } else if (move == PRESSURE_GUN) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, getPressureGunDamage()) + "+Steam Eruption 3";
            } else if (move == PRESSURE_UP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14) + "+Steam Eruption 3";
            } else if (move == ABOUT_TO_BLOW) {
                return "Invulnerable+Prepare Explode";
            } else if (move == EXPLODE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, steamEruption) + "+Die";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 250, 250);
        }

        @Override public String getName() {
            return "Waterfall Giant";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return s.subSequence(0, s.length() - 1) + ", steamEruption=" + steamEruption +
                    ", pressureGunCount=" + pressureGunCount + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && steamEruption == ((WaterfallGiant) o).steamEruption &&
                    pressureGunCount == ((WaterfallGiant) o).pressureGunCount;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 2;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "2 inputs to keep track of Waterfall Giant Steam Eruption mechanics";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = steamEruption / 50.0f;
            input[idx + 1] = pressureGunCount / 5.0f;
            return 2;
        }
    }

    public static class GasBomb extends Enemy {
        public static final int EXPLODE = 0;

        public GasBomb() {
            this(8);
        }

        public GasBomb(int health) {
            super(health, 1, false);
            properties.isMinion = true;
        }

        public GasBomb(GasBomb other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new GasBomb(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == EXPLODE) {
                state.enemyDoDamageToPlayer(this, 9, 1);
                health = 0;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = EXPLODE;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == EXPLODE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "+Die";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 8, 8);
        }

        @Override public String getName() {
            return "Gas Bomb";
        }
    }
}
