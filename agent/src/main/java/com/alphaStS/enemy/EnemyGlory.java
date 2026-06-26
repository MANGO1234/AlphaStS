package com.alphaStS.enemy;

import com.alphaStS.GameProperties;
import com.alphaStS.GameState;
import com.alphaStS.card.Card;
import com.alphaStS.card.CardOther2;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventEnemyHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.eventHandler.OnDamageHandler;
import com.alphaStS.random.RandomGen;
import com.alphaStS.random.RandomGenCtx;

import java.util.List;

import static com.alphaStS.enemy.EnemyUtils.nextFixedMove;
import static com.alphaStS.enemy.EnemyUtils.randomMove;
import static com.alphaStS.enemy.EnemyUtils.randomizeHealth;
import static com.alphaStS.enemy.EnemyUtils.reviveFirstDeadEnemy;

public class EnemyGlory {
    public static class Aeonglass extends Enemy {
        public static final int EBB = 0;
        public static final int EYE_LASERS = 1;
        public static final int INCREASING_INTENSITY = 2;

        public Aeonglass() {
            this(535);
        }

        public Aeonglass(int health) {
            super(health, 3, false);
            properties.isBoss = true;
            properties.hasArtifact = true;
            properties.canGainBlock = true;
            properties.canGainStrength = true;
            artifact = 3;
        }

        public Aeonglass(Aeonglass other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Aeonglass(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == EBB) {
                state.enemyDoDamageToPlayer(this, 32, 1);
                gainBlock(33);
            } else if (move == EYE_LASERS) {
                state.enemyDoDamageToPlayer(this, 12, 2);
            } else if (move == INCREASING_INTENSITY) {
                state.addCardToDiscard(properties.generatedCardIdx);
                state.addCardToDiscard(properties.generatedCardIdx);
                gainStrength(3);
                state.getCounterForWrite()[state.properties.aeonglassWitherCounterIdx]++;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 3);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == EBB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 32) + "+33 Block";
            } else if (move == EYE_LASERS) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12) + "x2";
            } else if (move == INCREASING_INTENSITY) {
                return "Wither Upgrade+Gain 3 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 535, 535);
        }

        @Override public String getName() {
            return "Aeonglass";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.Wither());
        }

        // TODO: better to have aeonglassWitherCounterIdx + 1 be an internal field with nninput within enemy
        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Aeonglass Wither", new GameProperties.CounterRegistrant() {
                @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
                    gameProperties.aeonglassWitherCounterIdx = idx;
                }

                @Override public int getCounterIdx(GameProperties gameProperties) {
                    return gameProperties.aeonglassWitherCounterIdx;
                }
            }, 2, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[state.properties.aeonglassWitherCounterIdx] / 10.0f;
                    input[idx + 1] = state.getCounterForRead()[state.properties.aeonglassWitherCounterIdx + 1] / 6.0f;
                    return idx + 2;
                }

                @Override public int getInputLenDelta() {
                    return 2;
                }
            });
            state.properties.addOnCardPlayedHandler("Withering Presence", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int[] counter = state.getCounterForWrite();
                    counter[state.properties.aeonglassWitherCounterIdx + 1]++;
                    if (counter[state.properties.aeonglassWitherCounterIdx + 1] >= 6) {
                        counter[state.properties.aeonglassWitherCounterIdx + 1] = 0;
                        state.addCardToHand(properties.generatedCardIdx);
                    }
                }
            });
        }
    }

    public static class Axebot extends Enemy {
        public static final int BOOT_UP = 0;
        public static final int THE_ONE_TWO = 1;
        public static final int HAMMER_UPPERCUT = 2;

        private int stock = 2;

        public Axebot() {
            this(86);
        }

        public Axebot(int health) {
            super(health, 3, false);
            properties.canGainBlock = true;
            properties.canGainStrength = true;
            properties.entityProperty.changePlayerWeakened = true;
            properties.entityProperty.changePlayerFrailed = true;
        }

        public Axebot(Axebot other) {
            super(other);
            stock = other.stock;
        }

        @Override public Enemy copy() {
            return new Axebot(this);
        }

        @Override public boolean canSelfRevive(GameState state) {
            return stock > 0;
        }

        @Override public int damage(double n, GameState state) {
            int prevHealth = health;
            int dmg = super.damage(n, state);
            reviveIfStocked(prevHealth, state);
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int prevHealth = health;
            super.nonAttackDamage(n, blockable, state);
            reviveIfStocked(prevHealth, state);
        }

        private void reviveIfStocked(int prevHealth, GameState state) {
            if (prevHealth > 0 && health <= 0 && stock > 0) {
                stock--;
                reviveReset();
                health = getMaxHealthInBattle();
                move = BOOT_UP;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BOOT_UP) {
                gainBlock(10);
                gainStrength(3);
            } else if (move == THE_ONE_TWO) {
                state.enemyDoDamageToPlayer(this, 10, 2);
            } else if (move == HAMMER_UPPERCUT) {
                state.enemyDoDamageToPlayer(this, 14, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0 || move == BOOT_UP) {
                state.setIsStochastic();
                move = random.nextInt(2, RandomGenCtx.EnemyChooseMove) == 0 ? THE_ONE_TWO : HAMMER_UPPERCUT;
            } else {
                move = move == THE_ONE_TWO ? HAMMER_UPPERCUT : THE_ONE_TWO;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BOOT_UP) {
                return "10 Block+Gain 3 Strength";
            } else if (move == THE_ONE_TWO) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10) + "x2";
            } else if (move == HAMMER_UPPERCUT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14) + "+Weak 2+Frail 2";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 76, 86);
        }

        @Override public String getName() {
            return "Axebot";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return stock == 2 ? s : s.subSequence(0, s.length() - 1) + ", stock=" + stock + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && stock == ((Axebot) o).stock;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Axebot Stock";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = stock / 2.0f;
            return 1;
        }
    }

    public static class DevotedSculptor extends Enemy {
        public static final int FORBIDDEN_INCANTATION = 0;
        public static final int SAVAGE = 1;

        public DevotedSculptor() {
            this(172);
        }

        public DevotedSculptor(int health) {
            super(health, 2, false);
            properties.canGainStrength = true;
        }

        public DevotedSculptor(DevotedSculptor other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new DevotedSculptor(this);
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            if (move == SAVAGE) {
                gainStrength(9);
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SAVAGE) {
                state.enemyDoDamageToPlayer(this, 15, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = move < 0 ? FORBIDDEN_INCANTATION : SAVAGE;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == FORBIDDEN_INCANTATION) {
                return "Ritual 9";
            } else if (move == SAVAGE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 15);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 172, 172);
        }

        @Override public String getName() {
            return "Devoted Sculptor";
        }
    }

    public static class Fabricator extends Enemy {
        public static final int FABRICATE = 0;
        public static final int FABRICATING_STRIKE = 1;
        public static final int DISINTEGRATE = 2;

        private Class<?> lastSummonedBot;

        public Fabricator() {
            this(155);
        }

        public Fabricator(int health) {
            super(health, 3, false);
        }

        public Fabricator(Fabricator other) {
            super(other);
            lastSummonedBot = other.lastSummonedBot;
        }

        @Override public Enemy copy() {
            return new Fabricator(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == FABRICATE) {
                summonBot(state, new Class<?>[] {Guardbot.class, Noisebot.class});
                summonBot(state, new Class<?>[] {Zapbot.class, Stabbot.class});
            } else if (move == FABRICATING_STRIKE) {
                state.enemyDoDamageToPlayer(this, 21, 1);
                summonBot(state, new Class<?>[] {Zapbot.class, Stabbot.class});
            } else if (move == DISINTEGRATE) {
                state.enemyDoDamageToPlayer(this, 13, 1);
            }
        }

        private void summonBot(GameState state, Class<?>[] botClasses) {
            Class<?> preferred = botClasses[0] == lastSummonedBot && botClasses.length > 1 ? botClasses[1] : botClasses[0];
            if (reviveFirstDeadEnemy(state, preferred)) {
                lastSummonedBot = preferred;
                return;
            }
            for (Class<?> botClass : botClasses) {
                if (botClass != preferred && reviveFirstDeadEnemy(state, botClass)) {
                    lastSummonedBot = botClass;
                    return;
                }
            }
            // TODO: Match the exact random summon selection and no-consecutive-bot rule.
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (livingAllies(state) >= 4) {
                move = DISINTEGRATE;
            } else {
                state.setIsStochastic();
                move = random.nextInt(2, RandomGenCtx.EnemyChooseMove) == 0 ? FABRICATE : FABRICATING_STRIKE;
            }
        }

        private int livingAllies(GameState state) {
            int allies = 0;
            for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                EnemyReadOnly enemy = state.getEnemiesForRead().get(i);
                if (enemy.isAlive() && !(enemy instanceof Fabricator)) {
                    allies++;
                }
            }
            return allies;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == FABRICATE) {
                return "Summon Defensive Bot+Summon Aggressive Bot";
            } else if (move == FABRICATING_STRIKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 21) + "+Summon Aggressive Bot";
            } else if (move == DISINTEGRATE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 13);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 155, 155);
        }

        @Override public String getName() {
            return "Fabricator";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return lastSummonedBot == null ? s : s.subSequence(0, s.length() - 1) + ", lastSummonedBot=" + lastSummonedBot.getSimpleName() + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && lastSummonedBot == ((Fabricator) o).lastSummonedBot;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 4;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "4 inputs to one-hot encode Fabricator last summoned bot";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            if (lastSummonedBot == Zapbot.class) {
                input[idx] = 0.5f;
            } else if (lastSummonedBot == Stabbot.class) {
                input[idx + 1] = 0.5f;
            } else if (lastSummonedBot == Guardbot.class) {
                input[idx + 2] = 0.5f;
            } else if (lastSummonedBot == Noisebot.class) {
                input[idx + 3] = 0.5f;
            }
            return 4;
        }
    }

    public static class Zapbot extends Enemy {
        public static final int ZAP = 0;

        public Zapbot() {
            this(24);
        }

        public Zapbot(int health) {
            super(health, 1, false);
            properties.canGainStrength = true;
        }

        public Zapbot(Zapbot other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Zapbot(this);
        }

        @Override public void startTurn(GameState state) {
            super.startTurn(state);
            gainStrength(2);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            state.enemyDoDamageToPlayer(this, 15, 1);
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = ZAP;
        }

        @Override public String getMoveString(GameState state, int move) {
            return move == ZAP ? "Attack " + state.enemyCalcDamageToPlayer(this, 15) : "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 19, 24);
        }

        @Override public String getName() {
            return "Zapbot";
        }
    }

    public static class Stabbot extends Enemy {
        public static final int STAB = 0;

        public Stabbot() {
            this(24);
        }

        public Stabbot(int health) {
            super(health, 1, false);
            properties.entityProperty.changePlayerFrailed = true;
        }

        public Stabbot(Stabbot other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Stabbot(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            state.enemyDoDamageToPlayer(this, 12, 1);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 1);
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = STAB;
        }

        @Override public String getMoveString(GameState state, int move) {
            return move == STAB ? "Attack " + state.enemyCalcDamageToPlayer(this, 12) + "+Frail 1" : "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 19, 24);
        }

        @Override public String getName() {
            return "Stabbot";
        }
    }

    public static class Guardbot extends Enemy {
        public static final int GUARD = 0;

        public Guardbot() {
            this(21);
        }

        public Guardbot(int health) {
            super(health, 1, false);
        }

        public Guardbot(Guardbot other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Guardbot(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                if (enemy instanceof Fabricator) {
                    enemy.gainBlock(15);
                    break;
                }
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = GUARD;
        }

        @Override public String getMoveString(GameState state, int move) {
            return move == GUARD ? "Fabricator 15 Block" : "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 17, 21);
        }

        @Override public String getName() {
            return "Guardbot";
        }
    }

    public static class Noisebot extends Enemy {
        public static final int NOISE = 0;

        public Noisebot() {
            this(24);
        }

        public Noisebot(int health) {
            super(health, 1, false);
        }

        public Noisebot(Noisebot other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Noisebot(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            state.addCardToDeck(properties.generatedCardIdx);
            state.addCardToDiscard(properties.generatedCardIdx);
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = NOISE;
        }

        @Override public String getMoveString(GameState state, int move) {
            return move == NOISE ? "Dazed to Draw and Discard" : "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 19, 24);
        }

        @Override public String getName() {
            return "Noisebot";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.Dazed());
        }
    }

    public static class FrogKnight extends Enemy {
        public static final int TONGUE_LASH = 0;
        public static final int STRIKE_DOWN_EVIL = 1;
        public static final int FOR_THE_QUEEN = 2;
        public static final int BEETLE_CHARGE = 3;

        private boolean usedBeetleCharge;

        public FrogKnight() {
            this(199);
        }

        public FrogKnight(int health) {
            super(health, 4, false);
            properties.canGainStrength = true;
            properties.canGainPlating = true;
            properties.entityProperty.changePlayerFrailed = true;
            plating = 19;
        }

        public FrogKnight(FrogKnight other) {
            super(other);
            usedBeetleCharge = other.usedBeetleCharge;
        }

        @Override public Enemy copy() {
            return new FrogKnight(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == TONGUE_LASH) {
                state.enemyDoDamageToPlayer(this, 14, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 2);
            } else if (move == STRIKE_DOWN_EVIL) {
                state.enemyDoDamageToPlayer(this, 23, 1);
            } else if (move == FOR_THE_QUEEN) {
                gainStrength(5);
            } else if (move == BEETLE_CHARGE) {
                state.enemyDoDamageToPlayer(this, 40, 1);
                usedBeetleCharge = true;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0 || move == BEETLE_CHARGE) {
                move = TONGUE_LASH;
            } else if (move == TONGUE_LASH) {
                move = STRIKE_DOWN_EVIL;
            } else if (move == STRIKE_DOWN_EVIL) {
                move = FOR_THE_QUEEN;
            } else if (!usedBeetleCharge && health * 2 < getMaxHealthInBattle()) {
                move = BEETLE_CHARGE;
            } else {
                move = TONGUE_LASH;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == TONGUE_LASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14) + "+Frail 2";
            } else if (move == STRIKE_DOWN_EVIL) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 23);
            } else if (move == FOR_THE_QUEEN) {
                return "Gain 5 Strength";
            } else if (move == BEETLE_CHARGE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 40);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 199, 199);
        }

        @Override public String getName() {
            return "Frog Knight";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && usedBeetleCharge == ((FrogKnight) o).usedBeetleCharge;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Frog Knight Beetle Charge";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = usedBeetleCharge ? 0.5f : 0.0f;
            return 1;
        }
    }

    public static class GlobeHead extends Enemy {
        public static final int SHOCKING_SLAP = 0;
        public static final int THUNDER_STRIKE = 1;
        public static final int GALVANIC_BURST = 2;

        public GlobeHead() {
            this(158);
        }

        public GlobeHead(int health) {
            super(health, 3, false);
            properties.canGainStrength = true;
            properties.entityProperty.changePlayerFrailed = true;
        }

        public GlobeHead(GlobeHead other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new GlobeHead(this);
        }

        @Override public void react(GameState state, Card card) {
            if (card.cardType == Card.POWER) {
                state.doNonAttackDamageToPlayer(6, false, card);
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SHOCKING_SLAP) {
                state.enemyDoDamageToPlayer(this, 14, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 2);
            } else if (move == THUNDER_STRIKE) {
                state.enemyDoDamageToPlayer(this, 7, 3);
            } else if (move == GALVANIC_BURST) {
                state.enemyDoDamageToPlayer(this, 17, 1);
                gainStrength(2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = nextFixedMove(move, 3);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SHOCKING_SLAP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14) + "+Frail 2";
            } else if (move == THUNDER_STRIKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "x3";
            } else if (move == GALVANIC_BURST) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 17) + "+Gain 2 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 158, 158);
        }

        @Override public String getName() {
            return "Globe Head";
        }
    }

    public static class FlailKnight extends Enemy {
        public static final int BREAKER = 0;
        public static final int FLAIL = 1;
        public static final int RAM = 2;

        public FlailKnight() {
            this(108);
        }

        public FlailKnight(int health) {
            super(health, 3, false);
            properties.isElite = true;
            properties.canGainStrength = true;
        }

        public FlailKnight(FlailKnight other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new FlailKnight(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BREAKER) {
                gainStrength(3);
            } else if (move == FLAIL) {
                state.enemyDoDamageToPlayer(this, 10, 2);
            } else if (move == RAM) {
                state.enemyDoDamageToPlayer(this, 17, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = RAM;
            } else {
                state.setIsStochastic();
                move = randomMove(random, move == BREAKER ? 0 : 1, 1, 1);
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BREAKER) {
                return "Gain 3 Strength";
            } else if (move == FLAIL) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10) + "x2";
            } else if (move == RAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 17);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 108, 108);
        }

        @Override public String getName() {
            return "Flail Knight";
        }
    }

    public static class SpectralKnight extends Enemy {
        public static final int HEX = 0;
        public static final int SOUL_SLASH = 1;
        public static final int SOUL_FLAME = 2;

        public SpectralKnight() {
            this(97);
        }

        public SpectralKnight(int health) {
            super(health, 3, false);
            properties.isElite = true;
        }

        public SpectralKnight(SpectralKnight other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new SpectralKnight(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == HEX) {
                // TODO: Implement Spectral Knight Hex once global alive-enemy ethereal card handling exists.
            } else if (move == SOUL_SLASH) {
                state.enemyDoDamageToPlayer(this, 17, 1);
            } else if (move == SOUL_FLAME) {
                state.enemyDoDamageToPlayer(this, 4, 3);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = HEX;
            } else if (move == HEX || move == SOUL_FLAME) {
                move = SOUL_SLASH;
            } else {
                move = SOUL_FLAME;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == HEX) {
                return "Hex 2";
            } else if (move == SOUL_SLASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 17);
            } else if (move == SOUL_FLAME) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 4) + "x3";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 97, 97);
        }

        @Override public String getName() {
            return "Spectral Knight";
        }
    }

    public static class MagiKnight extends Enemy {
        public static final int POWER_SHIELD = 0;
        public static final int DAMPEN = 1;
        public static final int RAM = 2;
        public static final int PREP = 3;
        public static final int MAGIC_BOMB = 4;

        public MagiKnight() {
            this(89);
        }

        public MagiKnight(int health) {
            super(health, 5, false);
            properties.isElite = true;
            properties.canGainBlock = true;
        }

        public MagiKnight(MagiKnight other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new MagiKnight(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == POWER_SHIELD) {
                state.enemyDoDamageToPlayer(this, 7, 1);
                gainBlock(9);
            } else if (move == DAMPEN) {
                // TODO: Implement Dampen once global alive-enemy card downgrade handling exists.
            } else if (move == RAM) {
                state.enemyDoDamageToPlayer(this, 11, 1);
            } else if (move == PREP) {
                gainBlock(9);
            } else if (move == MAGIC_BOMB) {
                state.enemyDoDamageToPlayer(this, 40, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = POWER_SHIELD;
            } else if (move == POWER_SHIELD) {
                move = DAMPEN;
            } else if (move == DAMPEN || move == MAGIC_BOMB) {
                move = RAM;
            } else if (move == RAM) {
                move = PREP;
            } else {
                move = MAGIC_BOMB;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == POWER_SHIELD) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "+9 Block";
            } else if (move == DAMPEN) {
                return "Dampen";
            } else if (move == RAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11);
            } else if (move == PREP) {
                return "9 Block";
            } else if (move == MAGIC_BOMB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 40);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 89, 89);
        }

        @Override public String getName() {
            return "Magi Knight";
        }
    }

    public static class MechaKnight extends Enemy {
        public static final int CHARGE = 0;
        public static final int FLAMETHROWER = 1;
        public static final int WINDUP = 2;
        public static final int HEAVY_CLEAVE = 3;

        public MechaKnight() {
            this(320);
        }

        public MechaKnight(int health) {
            super(health, 4, false);
            properties.isElite = true;
            properties.hasArtifact = true;
            properties.canGainBlock = true;
            properties.canGainStrength = true;
            artifact = 3;
        }

        public MechaKnight(MechaKnight other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new MechaKnight(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CHARGE) {
                state.enemyDoDamageToPlayer(this, 30, 1);
            } else if (move == FLAMETHROWER) {
                state.addCardToHand(properties.generatedCardIdx);
                state.addCardToHand(properties.generatedCardIdx);
                state.addCardToHand(properties.generatedCardIdx);
                state.addCardToHand(properties.generatedCardIdx);
            } else if (move == WINDUP) {
                gainBlock(15);
                gainStrength(5);
            } else if (move == HEAVY_CLEAVE) {
                state.enemyDoDamageToPlayer(this, 40, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = CHARGE;
            } else if (move == CHARGE || move == HEAVY_CLEAVE) {
                move = FLAMETHROWER;
            } else if (move == FLAMETHROWER) {
                move = WINDUP;
            } else {
                move = HEAVY_CLEAVE;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CHARGE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 30);
            } else if (move == FLAMETHROWER) {
                return "Burn 4";
            } else if (move == WINDUP) {
                return "15 Block+Gain 5 Strength";
            } else if (move == HEAVY_CLEAVE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 40);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 320, 320);
        }

        @Override public String getName() {
            return "Mecha Knight";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.Burn());
        }
    }

    public static class OwlMagistrate extends Enemy {
        public static final int MAGISTRATE_SCRUTINY = 0;
        public static final int PECK_ASSAULT = 1;
        public static final int JUDICIAL_FLIGHT = 2;
        public static final int VERDICT = 3;

        private int soar;

        public OwlMagistrate() {
            this(243);
        }

        public OwlMagistrate(int health) {
            super(health, 4, false);
            properties.entityProperty.changePlayerVulnerable = true;
        }

        public OwlMagistrate(OwlMagistrate other) {
            super(other);
            soar = other.soar;
        }

        @Override public Enemy copy() {
            return new OwlMagistrate(this);
        }

        @Override public int damage(double n, GameState state) {
            return super.damage(soar > 0 ? n / 2 : n, state);
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            if (soar > 0) {
                soar--;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == MAGISTRATE_SCRUTINY) {
                state.enemyDoDamageToPlayer(this, 17, 1);
            } else if (move == PECK_ASSAULT) {
                state.enemyDoDamageToPlayer(this, 4, 6);
            } else if (move == JUDICIAL_FLIGHT) {
                soar += 2;
            } else if (move == VERDICT) {
                state.enemyDoDamageToPlayer(this, 36, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 4);
                soar = 0;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = nextFixedMove(move, 4);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == MAGISTRATE_SCRUTINY) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 17);
            } else if (move == PECK_ASSAULT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 4) + "x6";
            } else if (move == JUDICIAL_FLIGHT) {
                return "Gain Soar";
            } else if (move == VERDICT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 36) + "+Vulnerable 4+Remove Soar";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 243, 243);
        }

        @Override public String getName() {
            return "Owl Magistrate";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return soar > 0 ? s.subSequence(0, s.length() - 1) + ", soar=" + soar + "}" : s;
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && soar == ((OwlMagistrate) o).soar;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Owl Magistrate Soar";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = soar / 2f;
            return 1;
        }
    }

    public static class Queen extends Enemy {
        public static final int PUPPET_STRINGS = 0;
        public static final int YOURE_MINE = 1;
        public static final int BURN_BRIGHT_FOR_ME = 2;
        public static final int OFF_WITH_YOUR_HEAD = 3;
        public static final int EXECUTION = 4;
        public static final int ENRAGE = 5;

        public Queen() {
            this(419);
        }

        public Queen(int health) {
            super(health, 6, false);
            properties.isBoss = true;
            properties.canGainBlock = true;
            properties.canGainStrength = true;
            properties.entityProperty.changePlayerFrailed = true;
            properties.entityProperty.changePlayerVulnerable = true;
            properties.entityProperty.changePlayerWeakened = true;
        }

        public Queen(Queen other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Queen(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == PUPPET_STRINGS) {
                // TODO: Implement Chains of Binding and Bound card play restrictions.
            } else if (move == YOURE_MINE) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 99);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 99);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 99);
            } else if (move == BURN_BRIGHT_FOR_ME) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    if (enemy instanceof TorchHeadAmalgam) {
                        enemy.gainStrength(1);
                    }
                }
                gainBlock(20);
            } else if (move == OFF_WITH_YOUR_HEAD) {
                state.enemyDoDamageToPlayer(this, 4, 5);
            } else if (move == EXECUTION) {
                state.enemyDoDamageToPlayer(this, 18, 1);
            } else if (move == ENRAGE) {
                gainStrength(2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = PUPPET_STRINGS;
            } else if (move == PUPPET_STRINGS) {
                move = YOURE_MINE;
            } else if (torchHeadAmalgamAlive(state)) {
                move = BURN_BRIGHT_FOR_ME;
            } else if (move == OFF_WITH_YOUR_HEAD || move == EXECUTION || move == ENRAGE) {
                move = nextFixedMove(move - OFF_WITH_YOUR_HEAD, 3) + OFF_WITH_YOUR_HEAD;
            } else {
                move = OFF_WITH_YOUR_HEAD;
            }
        }

        private boolean torchHeadAmalgamAlive(GameState state) {
            for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                EnemyReadOnly enemy = state.getEnemiesForRead().get(i);
                if (enemy instanceof TorchHeadAmalgam && enemy.isAlive()) {
                    return true;
                }
            }
            return false;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == PUPPET_STRINGS) {
                return "Chains of Binding 3";
            } else if (move == YOURE_MINE) {
                return "Frail 99+Weak 99+Vulnerable 99";
            } else if (move == BURN_BRIGHT_FOR_ME) {
                return "Torch Head Amalgam gains 1 Strength+20 Block";
            } else if (move == OFF_WITH_YOUR_HEAD) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 4) + "x5";
            } else if (move == EXECUTION) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 18);
            } else if (move == ENRAGE) {
                return "Gain 2 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 419, 419);
        }

        @Override public String getName() {
            return "Queen";
        }
    }

    public static class TorchHeadAmalgam extends Enemy {
        public static final int TACKLE_1 = 0;
        public static final int TACKLE_2 = 1;
        public static final int BEAM = 2;
        public static final int WEAK_TACKLE_1 = 3;
        public static final int WEAK_TACKLE_2 = 4;

        public TorchHeadAmalgam() {
            this(211);
        }

        public TorchHeadAmalgam(int health) {
            super(health, 5, false);
            properties.isBoss = true;
            properties.isMinion = true;
            properties.canGainStrength = true;
        }

        public TorchHeadAmalgam(TorchHeadAmalgam other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new TorchHeadAmalgam(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == TACKLE_1 || move == TACKLE_2) {
                state.enemyDoDamageToPlayer(this, 19, 1);
            } else if (move == BEAM) {
                state.enemyDoDamageToPlayer(this, 8, 3);
            } else if (move == WEAK_TACKLE_1 || move == WEAK_TACKLE_2) {
                state.enemyDoDamageToPlayer(this, 15, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0) {
                move = TACKLE_1;
            } else if (move == TACKLE_1) {
                move = TACKLE_2;
            } else if (move == TACKLE_2 || move == WEAK_TACKLE_2) {
                move = BEAM;
            } else if (move == BEAM) {
                move = WEAK_TACKLE_1;
            } else {
                move = WEAK_TACKLE_2;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == TACKLE_1 || move == TACKLE_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 19);
            } else if (move == BEAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "x3";
            } else if (move == WEAK_TACKLE_1 || move == WEAK_TACKLE_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 15);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 211, 211);
        }

        @Override public String getName() {
            return "Torch Head Amalgam";
        }
    }

    public static class ScrollOfBiting extends Enemy {
        public static final int CHOMP = 0;
        public static final int MORE_TEETH = 1;
        public static final int CHEW = 2;

        public ScrollOfBiting() {
            this(39);
        }

        public ScrollOfBiting(int health) {
            super(health, 3, false);
            properties.canGainStrength = true;
        }

        public ScrollOfBiting(ScrollOfBiting other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new ScrollOfBiting(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CHOMP) {
                state.enemyDoDamageToPlayer(this, 16, 1);
            } else if (move == MORE_TEETH) {
                gainStrength(2);
            } else if (move == CHEW) {
                state.enemyDoDamageToPlayer(this, 6, 2);
            }
            // TODO: Implement Paper Cuts max HP penalty after unblocked attack damage.
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0 || move == CHOMP) {
                move = MORE_TEETH;
            } else if (move == MORE_TEETH) {
                move = CHEW;
            } else {
                state.setIsStochastic();
                move = random.nextInt(3, RandomGenCtx.EnemyChooseMove) == 0 ? CHOMP : CHEW;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CHOMP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 16);
            } else if (move == MORE_TEETH) {
                return "Gain 2 Strength";
            } else if (move == CHEW) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6) + "x2";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 32, 39);
        }

        @Override public String getName() {
            return "Scroll of Biting";
        }
    }

    public static class SlimedBerserker extends Enemy {
        public static final int VOMIT_ICHOR = 0;
        public static final int FURIOUS_PUMMELING = 1;
        public static final int LEECHING_HUG = 2;
        public static final int SMOTHER = 3;

        public SlimedBerserker() {
            this(276);
        }

        public SlimedBerserker(int health) {
            super(health, 4, false);
            properties.canGainStrength = true;
            properties.entityProperty.changePlayerWeakened = true;
        }

        public SlimedBerserker(SlimedBerserker other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new SlimedBerserker(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == VOMIT_ICHOR) {
                for (int i = 0; i < 10; i++) {
                    state.addCardToDiscard(properties.generatedCardIdx);
                }
            } else if (move == FURIOUS_PUMMELING) {
                state.enemyDoDamageToPlayer(this, 5, 4);
            } else if (move == LEECHING_HUG) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 3);
                gainStrength(3);
            } else if (move == SMOTHER) {
                state.enemyDoDamageToPlayer(this, 33, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = nextFixedMove(move, 4);
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == VOMIT_ICHOR) {
                return "Slimed 10";
            } else if (move == FURIOUS_PUMMELING) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 5) + "x4";
            } else if (move == LEECHING_HUG) {
                return "Weak 3+Gain 3 Strength";
            } else if (move == SMOTHER) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 33);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 276, 276);
        }

        @Override public String getName() {
            return "Slimed Berserker";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.Slimed());
        }
    }

    public static class SoulNexus extends Enemy {
        public static final int SOUL_BURN = 0;
        public static final int MAELSTROM = 1;
        public static final int DRAIN_LIFE = 2;

        public SoulNexus() {
            this(254);
        }

        public SoulNexus(int health) {
            super(health, 3, false);
            properties.isElite = true;
            properties.entityProperty.changePlayerVulnerable = true;
            properties.entityProperty.changePlayerWeakened = true;
        }

        public SoulNexus(SoulNexus other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new SoulNexus(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SOUL_BURN) {
                state.enemyDoDamageToPlayer(this, 31, 1);
            } else if (move == MAELSTROM) {
                state.enemyDoDamageToPlayer(this, 7, 4);
            } else if (move == DRAIN_LIFE) {
                state.enemyDoDamageToPlayer(this, 19, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < 0) {
                move = SOUL_BURN;
            } else {
                state.setIsStochastic();
                move = randomMove(random, move == SOUL_BURN ? 0 : 1, move == MAELSTROM ? 0 : 1, move == DRAIN_LIFE ? 0 : 1);
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SOUL_BURN) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 31);
            } else if (move == MAELSTROM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "x4";
            } else if (move == DRAIN_LIFE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 19) + "+Vulnerable 2+Weak 2";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 254, 254);
        }

        @Override public String getName() {
            return "Soul Nexus";
        }
    }

    public static class TestSubject extends Enemy {
        public static final int BITE = 0;
        public static final int SKULL_BASH = 1;
        public static final int ADAPTABLE = 2;
        public static final int MULTI_CLAW = 3;
        public static final int LACERATE = 4;
        public static final int BIG_POUNCE = 5;
        public static final int BURNING_GROWL = 6;

        private int phase = 1;
        private int multiClawHits = 3;
        private boolean intangible = false;

        public TestSubject() {
            this(111);
        }

        public TestSubject(int health) {
            super(health, 7, false);
            properties.maxHealth = 313;
            properties.origMaxHealth = 313;
            properties.isBoss = true;
            properties.canGainStrength = true;
            properties.entityProperty.changePlayerVulnerable = true;
            strength = 3;
        }

        public TestSubject(TestSubject other) {
            super(other);
            phase = other.phase;
            multiClawHits = other.multiClawHits;
            intangible = other.intangible;
        }

        @Override public Enemy copy() {
            return new TestSubject(this);
        }

        @Override public boolean canSelfRevive(GameState state) {
            return phase < 3;
        }

        @Override public int damage(double n, GameState state) {
            int prevHp = health;
            int dmg = super.damage(n, state);
            applyIntangibleDamageLimit(prevHp, state);
            if (health <= 0 && phase < 3 && !state.properties.isRunicDomeEnabled(state)) {
                move = ADAPTABLE;
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int prevHp = health;
            super.nonAttackDamage(n, blockable, state);
            applyIntangibleDamageLimit(prevHp, state);
            if (health <= 0 && phase < 3 && !state.properties.isRunicDomeEnabled(state)) {
                move = ADAPTABLE;
            }
        }

        private void applyIntangibleDamageLimit(int prevHp, GameState state) {
            if (phase != 3 || !intangible) {
                return;
            }
            if (state.properties.boot != null && state.properties.boot.isRelicEnabledInScenario(state)) {
                if (prevHp - health > 5) {
                    setHealth(prevHp - 5);
                }
            } else if (prevHp - health > 1) {
                setHealth(prevHp - 1);
            }
        }

        private void setupPhase(int phase, int health) {
            this.phase = phase;
            this.health = health;
            setMaxHealthInBattle(phaseHealth(phase));
            if (phase == 1) {
                strength = 3;
            } else {
                strength = 0;
            }
            if (phase <= 2) {
                intangible = false;
            } else {
                intangible = true;
            }
        }

        private int phaseHealth(int phase) {
            if (phase == 1) {
                return 111;
            } else if (phase == 2) {
                return 212;
            }
            return 313;
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BITE) {
                state.enemyDoDamageToPlayer(this, 22, 1);
            } else if (move == SKULL_BASH) {
                state.enemyDoDamageToPlayer(this, 16, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 1);
            } else if (move == ADAPTABLE) {
                reviveReset();
                phase++;
                if (phase == 2) {
                    setupPhase(2, 212);
                    multiClawHits = 3;
                } else {
                    setupPhase(3, 313);
                }
            } else if (move == MULTI_CLAW) {
                state.enemyDoDamageToPlayer(this, 11, multiClawHits);
                multiClawHits++;
            } else if (move == LACERATE) {
                state.enemyDoDamageToPlayer(this, 11, 3);
            } else if (move == BIG_POUNCE) {
                state.enemyDoDamageToPlayer(this, 45, 1);
            } else if (move == BURNING_GROWL) {
                for (int i = 0; i < 5; i++) {
                    state.addCardToDiscard(properties.generatedCardIdxes[1]);
                }
                gainStrength(3);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (health == 0 && phase < 3) {
                move = ADAPTABLE;
            } else if (phase == 1) {
                move = move == BITE ? SKULL_BASH : BITE;
            } else if (phase == 2) {
                move = MULTI_CLAW;
            } else if (move == ADAPTABLE || move < 0) {
                move = LACERATE;
            } else if (move == LACERATE) {
                move = BIG_POUNCE;
            } else if (move == BIG_POUNCE) {
                move = BURNING_GROWL;
            } else {
                move = LACERATE;
            }
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            if (phase == 3) {
                intangible = !intangible;
            }
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var idx = state.getEnemiesForRead().find(this);
            state.properties.addOnCardPlayedHandler("TestSubjectEnrage", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    EnemyReadOnly enemy = state.getEnemiesForRead().get(idx);
                    if (enemy instanceof TestSubject testSubject && testSubject.phase == 1 && enemy.isAlive()
                            && enemy.getMove() != ADAPTABLE && state.properties.cardDict[cardIdx].cardType == Card.SKILL) {
                        state.getEnemiesForWrite().getForWrite(idx).gainStrength(3);
                    }
                }
            });
            state.properties.addOnDamageHandler("TestSubjectPainfulStabs", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (source instanceof TestSubject testSubject && testSubject.phase == 2 && isAttack && damageDealt > 0) {
                        state.addCardToDiscard(properties.generatedCardIdxes[0]);
                    }
                }
            });
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BITE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 22);
            } else if (move == SKULL_BASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 16) + "+Vulnerable 1";
            } else if (move == ADAPTABLE) {
                return "Revive";
            } else if (move == MULTI_CLAW) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11) + "x" + multiClawHits;
            } else if (move == LACERATE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11) + "x3";
            } else if (move == BIG_POUNCE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 45);
            } else if (move == BURNING_GROWL) {
                return "Burn 5+Gain 3 Strength";
            }
            return "Unknown";
        }

        @Override public boolean isAlive() {
            return phase < 3 || health > 0;
        }

        @Override public boolean isTargetable() {
            return health > 0 && move != ADAPTABLE;
        }

        @Override public void setHealth(int hp) {
            health = hp;
            if (phase == 3 && health <= 0) {
                health = 0;
            }
        }

        @Override public int getMaxRandomizeDifficulty() {
            return 636;
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            if (training) {
                difficulty = difficulty <= 0 ? state.getSearchRandomGen().nextInt(636, RandomGenCtx.Other) + 1 : difficulty;
            } else {
                difficulty = 636;
            }
            if (difficulty <= 313) {
                setupPhase(3, difficulty);
            } else if (difficulty <= 525) {
                setupPhase(2, difficulty - 313);
            } else {
                setupPhase(1, difficulty - 525);
            }
        }

        @Override public String getName() {
            return "Test Subject";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return s.subSequence(0, s.length() - 1) + ", phase=" + phase + ", multiClawHits=" + multiClawHits
                    + (intangible ? ", intangible}" : "}");
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && phase == ((TestSubject) o).phase
                    && multiClawHits == ((TestSubject) o).multiClawHits && intangible == ((TestSubject) o).intangible;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 3;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "3 inputs to keep track of Test Subject phase, multi-claw hits, and intangible";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = phase / 3f;
            input[idx + 1] = multiClawHits / 10f;
            input[idx + 2] = intangible ? 0.5f : 0f;
            return 3;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther2.Wound(), new CardOther2.Burn());
        }
    }

    public static class TheLost extends Enemy {
        public static final int DEBILITATING_SMOG = 0;
        public static final int EYE_LASERS = 1;

        private int stolenStrength;

        public TheLost() {
            this(99);
        }

        public TheLost(int health) {
            super(health, 2, false);
            properties.canGainStrength = true;
        }

        public TheLost(TheLost other) {
            super(other);
            stolenStrength = other.stolenStrength;
        }

        @Override public Enemy copy() {
            return new TheLost(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == DEBILITATING_SMOG) {
                int prevStrength = state.getPlayerForRead().getStrength();
                state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_STRENGTH, 2);
                int stolen = prevStrength - state.getPlayerForRead().getStrength();
                stolenStrength += stolen;
                gainStrength(2);
            } else if (move == EYE_LASERS) {
                state.enemyDoDamageToPlayer(this, 5, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = nextFixedMove(move, 2);
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var idx = state.getEnemiesForRead().find(this);
            state.properties.addOnEnemyDeathHandler("TheLostReturnStrength", new GameEventEnemyHandler() {
                @Override public void handle(GameState state, EnemyReadOnly enemy) {
                    if (state.getEnemiesForRead().get(idx) == enemy && enemy instanceof TheLost lost && lost.stolenStrength > 0) {
                        state.getPlayerForWrite().gainStrength(lost.stolenStrength);
                    }
                }
            });
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == DEBILITATING_SMOG) {
                return "Steal 2 Strength";
            } else if (move == EYE_LASERS) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 5) + "x2";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 99, 99);
        }

        @Override public String getName() {
            return "The Lost";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return stolenStrength == 0 ? s : s.subSequence(0, s.length() - 1) + ", stolenStrength=" + stolenStrength + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && stolenStrength == ((TheLost) o).stolenStrength;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of The Lost stolen Strength";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = stolenStrength / 10f;
            return 1;
        }
    }

    public static class TheForgotten extends Enemy {
        public static final int MIASMA = 0;
        public static final int DREAD = 1;

        private int stolenDexterity;
        private int dexterity;

        public TheForgotten() {
            this(111);
        }

        public TheForgotten(int health) {
            super(health, 2, false);
            properties.canGainBlock = true;
        }

        public TheForgotten(TheForgotten other) {
            super(other);
            stolenDexterity = other.stolenDexterity;
            dexterity = other.dexterity;
        }

        @Override public Enemy copy() {
            return new TheForgotten(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == MIASMA) {
                int prevDexterity = state.getPlayerForRead().getDexterity();
                state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_DEXTERITY, 2);
                int stolen = prevDexterity - state.getPlayerForRead().getDexterity();
                stolenDexterity += stolen;
                dexterity += 2;
                gainBlock(8);
            } else if (move == DREAD) {
                state.enemyDoDamageToPlayer(this, 15 + dexterity, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = nextFixedMove(move, 2);
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var idx = state.getEnemiesForRead().find(this);
            state.properties.addOnEnemyDeathHandler("TheForgottenReturnDexterity", new GameEventEnemyHandler() {
                @Override public void handle(GameState state, EnemyReadOnly enemy) {
                    if (state.getEnemiesForRead().get(idx) == enemy && enemy instanceof TheForgotten forgotten
                            && forgotten.stolenDexterity > 0) {
                        state.getPlayerForWrite().gainDexterity(forgotten.stolenDexterity);
                    }
                }
            });
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == MIASMA) {
                return "Steal 2 Dexterity+8 Block";
            } else if (move == DREAD) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 15 + dexterity);
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 111, 111);
        }

        @Override public String getName() {
            return "The Forgotten";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            if (stolenDexterity == 0 && dexterity == 0) {
                return s;
            }
            return s.subSequence(0, s.length() - 1) + ", stolenDexterity=" + stolenDexterity + ", dexterity=" + dexterity + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && stolenDexterity == ((TheForgotten) o).stolenDexterity
                    && dexterity == ((TheForgotten) o).dexterity;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 2;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "2 inputs to keep track of The Forgotten stolen Dexterity and Dexterity";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = stolenDexterity / 10f;
            input[idx + 1] = dexterity / 10f;
            return 2;
        }
    }

    public static class TurretOperator extends Enemy {
        public static final int UNLOAD_1 = 0;
        public static final int UNLOAD_2 = 1;
        public static final int LOADING = 2;

        public TurretOperator() {
            this(51);
        }

        public TurretOperator(int health) {
            super(health, 3, false);
            properties.canGainBlock = true;
            properties.canGainStrength = true;
        }

        public TurretOperator(TurretOperator other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new TurretOperator(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == UNLOAD_1 || move == UNLOAD_2) {
                state.enemyDoDamageToPlayer(this, 4, 5);
            } else if (move == LOADING) {
                gainStrength(1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            if (move < 0 || move == LOADING) {
                move = UNLOAD_1;
            } else if (move == UNLOAD_1) {
                move = UNLOAD_2;
            } else {
                move = LOADING;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == UNLOAD_1 || move == UNLOAD_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 4) + "x5";
            } else if (move == LOADING) {
                return "Gain 1 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 51, 51);
        }

        @Override public String getName() {
            return "Turret Operator";
        }
    }

    public static class LivingShield extends Enemy {
        public static final int SHIELD_SLAM = 0;
        public static final int SMASH = 1;

        public LivingShield() {
            this(65);
        }

        public LivingShield(int health) {
            super(health, 2, false);
            properties.canGainStrength = true;
        }

        public LivingShield(LivingShield other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new LivingShield(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SHIELD_SLAM) {
                state.enemyDoDamageToPlayer(this, 6, 1);
            } else if (move == SMASH) {
                state.enemyDoDamageToPlayer(this, 18, 1);
                gainStrength(3);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = turretOperatorAlive(state) ? SHIELD_SLAM : SMASH;
        }

        private boolean turretOperatorAlive(GameState state) {
            for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                EnemyReadOnly enemy = state.getEnemiesForRead().get(i);
                if (enemy instanceof TurretOperator && enemy.isAlive()) {
                    return true;
                }
            }
            return false;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("LivingShieldRampart", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    boolean rampartActive = false;
                    for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                        if (state.getEnemiesForRead().get(i) instanceof LivingShield && state.getEnemiesForRead().get(i).isAlive()) {
                            rampartActive = true;
                            break;
                        }
                    }
                    if (!rampartActive) {
                        return;
                    }
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        if (enemy instanceof TurretOperator) {
                            enemy.gainBlock(25);
                        }
                    }
                }
            });
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SHIELD_SLAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6);
            } else if (move == SMASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 18) + "+Gain 3 Strength";
            }
            return "Unknown";
        }

        @Override public void randomize(GameState state, boolean training, int difficulty) {
            randomizeHealth(this, state, training, 65, 65);
        }

        @Override public String getName() {
            return "Living Shield";
        }
    }
}
