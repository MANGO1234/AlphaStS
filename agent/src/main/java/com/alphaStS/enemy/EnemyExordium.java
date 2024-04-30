package com.alphaStS.enemy;

import com.alphaStS.*;
import com.alphaStS.action.GuardianGainBlockAction;
import com.alphaStS.card.Card;
import com.alphaStS.card.CardOther;

import java.util.Arrays;
import java.util.List;

public class EnemyExordium {
    public static class GremlinNob extends Enemy {
        public static int BELLOW = 0;
        public static int SKULL_BASH = 1;
        public static int RUSH_1 = 2;
        public static int RUSH_2 = 3;

        public GremlinNob() {
            this(90);
        }

        public GremlinNob(int health) {
            super(health, 4, false);
            properties.isElite = true;
            properties.actNumber = 1;
            properties.canVulnerable = true;
            properties.canGainStrength = true;
        }

        public GremlinNob(GremlinNob other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new GremlinNob(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SKULL_BASH) {
                state.enemyDoDamageToPlayer(this, 8, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
            } else if (move == RUSH_1 || move == RUSH_2) {
                state.enemyDoDamageToPlayer(this, 16, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == -1) {
                move = BELLOW;
            } else {
                move++;
                if (move == 4) {
                    move = SKULL_BASH;
                }
            }
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var idx = state.getEnemiesForRead().find(this);
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.SKILL) {
                        var e = state.getEnemiesForRead().get(idx);
                        if (e instanceof MergedEnemy m) {
                            if (m.currentEnemy instanceof GremlinNob && e.getMove() > 0) {
                                state.getEnemiesForWrite().getForWrite(idx).gainStrength(3);
                            }
                        } else if (e.getMove() > 0) {
                            state.getEnemiesForWrite().getForWrite(idx).gainStrength(3);
                        }
                    }
                }
            });
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BELLOW) {
                return "Buff";
            } else if (move == SKULL_BASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this,8);
            } else if (move == RUSH_1 || move == RUSH_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this,16);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(10, RandomGenCtx.Other) + 1;
            if (b < 10 && training) {
                health = (int) Math.round(((double) (health * b)) / 10);
            } else {
                health = 85 + random.nextInt(6, RandomGenCtx.Other);
            }
        }

        public String getName() {
            return "Gremlin Nob";
        }
    }

    public static class Lagavulin extends Enemy {
        static int WAIT_1 = 0;
        static int WAIT_2 = 1;
        static int WAIT_3 = 2;
        static int ATTACK_1 = 3;
        static int ATTACK_2 = 4;
        static int SIPHON_SOUL = 5;
        static int STUNNED = 6;

        public Lagavulin() {
            this(116);
        }

        public Lagavulin(int health) {
            super(health, 4, false);
            properties.isElite = true;
            properties.actNumber = 1;
            properties.canGainBlock = true;
            properties.changePlayerStrength = true;
            properties.changePlayerDexterity = true;
        }

        public Lagavulin(Lagavulin other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Lagavulin(this);
        }

        @Override public int damage(double n, GameState state) {
            var dmg = super.damage(n, state);
            if ((move == WAIT_1 || move == WAIT_2 || move == WAIT_3) && dmg > 0) {
                move = STUNNED;
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            var dmg = blockable ? Math.max(0, n - block) : n;
            super.nonAttackDamage(n, blockable, state);
            if ((move == WAIT_1 || move == WAIT_2 || move == WAIT_3) && dmg > 0) {
                move = STUNNED;
            }
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            if (move <= WAIT_3) {
                gainBlock(8);
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == ATTACK_1 || move == ATTACK_2) {
                state.enemyDoDamageToPlayer(this, 20, 1);
            } else if (move == SIPHON_SOUL) {
                var player = state.getPlayerForWrite() ;
                player.applyDebuff(state, DebuffType.LOSE_DEXTERITY, 2);
                player.applyDebuff(state, DebuffType.LOSE_STRENGTH, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == STUNNED) {
                move = ATTACK_1;
                return;
            }
            if (move < 5) {
                move++;
            } else if (move == 5) {
                move = 3;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == WAIT_1 || move == WAIT_2 || move == WAIT_3) {
                return "Asleep";
            } else if (move == ATTACK_1 || move == ATTACK_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 20);
            } else if (move == SIPHON_SOUL) {
                return "-2 Strength+-2 Dexterity";
            } else if (move == STUNNED) {
                return "Stunned";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(10, RandomGenCtx.Other) + 1;
            if (training && b < 10) {
                health = (int) Math.round(((double) (health * b)) / 10);
            } else {
                health = 112 + random.nextInt(4, RandomGenCtx.Other);
            }
        }

        public String getName() {
            return "Lagavulin";
        }
    }

    public static class Sentry extends Enemy {
        public final static int BEAM = 0;
        public final static int BOLT = 1;

        int startMove;

        public Sentry(int startMove) {
            this(45, startMove);
        }

        public Sentry(int health, int startMove) {
            super(health, 2, false);
            properties.isElite = true;
            properties.actNumber = 1;
            properties.hasArtifact = true;
            this.startMove = startMove;
            artifact = 1;
        }

        public Sentry(Sentry other) {
            super(other);
            startMove = other.startMove;
        }

        @Override public Enemy copy() {
            return new Sentry(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BEAM) {
                state.enemyDoDamageToPlayer(this, 10, 1);
            } else if (move == BOLT) {
                state.addCardToDiscard(state.properties.dazedCardIdx);
                state.addCardToDiscard(state.properties.dazedCardIdx);
                state.addCardToDiscard(state.properties.dazedCardIdx);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == -1) {
                move = startMove;
                return;
            }
            move = move == BEAM ? BOLT : BEAM;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BEAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10);
            } else if (move == BOLT) {
                return "Shuffle 3 Dazes Into Deck";
            }
            return "Unknown";
        }

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new CardOther.Dazed()); }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(4, RandomGenCtx.Other) + 1;
            if (training && b < 4) {
                health = (int) Math.round(((double) (health * b)) / 4);
            } else {
                health = 39 + random.nextInt(7, RandomGenCtx.Other);
            }
        }

        public String getName() {
            return "Sentry";
        }
    }

    public static class Hexaghost extends Enemy {
        static int ACTIVATE = 0;
        static int DIVIDER = 1;
        static int SEAR_1 = 2;
        static int TACKLE_1 = 3;
        static int SEAR_2 = 4;
        static int INFLAME_1 = 5;
        static int TACKLE_2 = 6;
        static int SEAR_3 = 7;
        static int INFERNAL_1 = 8;

        boolean afterFirstInfernal;

        public Hexaghost() {
            this(264);
        }

        public Hexaghost(int health) {
            super(health, 9, false);
            properties.isBoss = true;
            properties.canGainStrength = true;
            properties.canGainBlock = true;
            afterFirstInfernal = false;
        }

        public Hexaghost(Hexaghost other) {
            super(other);
            afterFirstInfernal = other.afterFirstInfernal;
        }

        @Override public Enemy copy() {
            return new Hexaghost(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == DIVIDER) {
                int n = state.getPlayeForRead().getHealth() / 12 + 1;
                state.enemyDoDamageToPlayer(this, n, 6);
            } else if (move == SEAR_1 || move == SEAR_2 || move == SEAR_3) {
                state.enemyDoDamageToPlayer(this, 6, 1);
                state.addCardToDiscard(state.properties.burnCardIdx);
                state.addCardToDiscard(state.properties.burnCardIdx);
            } else if (move == TACKLE_1 || move == TACKLE_2) {
                state.enemyDoDamageToPlayer(this, 6, 2);
            } else if (move == INFLAME_1) {
                strength += 3;
                gainBlock(12);
            } else if (move == INFERNAL_1) {
                state.enemyDoDamageToPlayer(this, 3, 6);
                var burnUpgrade = new int[state.properties.cardDict.length];
                Arrays.fill(burnUpgrade, -1);
                burnUpgrade[state.properties.burnCardIdx] = state.properties.burnPCardIdx;
                state.handArrTransform(burnUpgrade);
                state.discardArrTransform(burnUpgrade);
                state.deckArrTransform(burnUpgrade);
                state.addCardToDiscard(state.properties.burnPCardIdx);
                state.addCardToDiscard(state.properties.burnPCardIdx);
                state.addCardToDiscard(state.properties.burnPCardIdx);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < 9) {
                move = move + 1;
            }
            if (move == 9) {
                afterFirstInfernal = true;
                move = SEAR_1;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == ACTIVATE) {
                return "Activate";
            } else if (move == DIVIDER) {
                int n = state.getPlayeForRead().getHealth() / 12 + 1;
                return "Attack " + state.enemyCalcDamageToPlayer(this, 3) + "x6";
            } else if (move == SEAR_1 || move == SEAR_2 || move == SEAR_3) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6);
            } else if (move == TACKLE_1 || move == TACKLE_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6) + "x2";
            } else if (move == INFLAME_1) {
                return "Gain 3 Strength+12 Block";
            } else if (move == INFERNAL_1) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 3) + "x6";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
                int b = random.nextInt(25, RandomGenCtx.Other) + 1;
                health = 264 * b / 25;
            }
        }

        public String getName() {
            return "Hexaghost";
        }

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new CardOther.Burn(), new CardOther.BurnP()); }
    }

    public static class TheGuardian extends Enemy {
        static int CHARGING_UP = 0;
        static int FIERCE_BASH = 1;
        static int VENT_STEAM = 2;
        static int WHIRL_WIND = 3;
        static int DEFENSIVE_MODE = 4;
        static int ROLL_ATTACK = 5;
        static int TWIN_SLAM = 6;

        int modeShiftDmg;
        int maxModeShiftDmg;

        public int getModeShiftDmg() {
            return modeShiftDmg;
        }

        public int getMaxModeShiftDmg() {
            return maxModeShiftDmg;
        }

        public TheGuardian() {
            this(250);
        }

        public TheGuardian(int health) {
            super(health, 7, false);
            properties.isBoss = true;
            properties.canGainBlock = true;
            properties.canVulnerable = true;
            properties.canWeaken = true;
            modeShiftDmg = 40;
            maxModeShiftDmg = 40;
        }

        public TheGuardian(TheGuardian other) {
            super(other);
            modeShiftDmg = other.modeShiftDmg;
            maxModeShiftDmg = other.maxModeShiftDmg;
        }

        @Override public Enemy copy() {
            return new TheGuardian(this);
        }

        @Override public int damage(double n, GameState state) {
            var dmg = super.damage(n, state);
            if (move != DEFENSIVE_MODE && move != ROLL_ATTACK && move != TWIN_SLAM) {
                modeShiftDmg = Math.max(modeShiftDmg - dmg, 0);
                if (modeShiftDmg == 0) {
                    move = DEFENSIVE_MODE;
                    state.addGameActionToEndOfDeque(GuardianGainBlockAction.singleton);
                }
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int oldHealth = health;
            super.nonAttackDamage(n, blockable, state);
            if (move != DEFENSIVE_MODE && move != ROLL_ATTACK && move != TWIN_SLAM) {
                modeShiftDmg = Math.max(modeShiftDmg - (oldHealth - health), 0);
                if (modeShiftDmg == 0) {
                    move = DEFENSIVE_MODE;
                    state.addGameActionToEndOfDeque(GuardianGainBlockAction.singleton);
                }
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CHARGING_UP) {
                gainBlock(9);
            } else if (move == FIERCE_BASH) {
                state.enemyDoDamageToPlayer(this, 36, 1);
            } else if (move == VENT_STEAM) {
                var player = state.getPlayerForWrite();
                player.applyDebuff(state, DebuffType.VULNERABLE, 2);
                player.applyDebuff(state, DebuffType.WEAK, 2);
            } else if (move == WHIRL_WIND) {
                state.enemyDoDamageToPlayer(this, 5, 4);
            } else if (move == DEFENSIVE_MODE) {
            } else if (move == ROLL_ATTACK) {
                state.enemyDoDamageToPlayer(this, 10, 1);
            } else if (move == TWIN_SLAM) {
                state.enemyDoDamageToPlayer(this, 8, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < WHIRL_WIND) {
                move++;
            } else if (move == WHIRL_WIND) {
                move = CHARGING_UP;
            } else if (move == DEFENSIVE_MODE) {
                move = ROLL_ATTACK;
            } else if (move == ROLL_ATTACK) {
                move = TWIN_SLAM;
            } else if (move == TWIN_SLAM) {
                move = WHIRL_WIND;
                maxModeShiftDmg += 10;
                modeShiftDmg = maxModeShiftDmg;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CHARGING_UP) {
                return "Charging Up";
            } else if (move == FIERCE_BASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 36);
            } else if (move == VENT_STEAM) {
                return "2 Vulnerable+2 Weak";
            } else if (move == WHIRL_WIND) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 5) + "x4";
            } else if (move == DEFENSIVE_MODE) {
                return "Defensive Mode";
            } else if (move == ROLL_ATTACK) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10);
            } else if (move == TWIN_SLAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "x2";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = training ? random.nextInt(25, RandomGenCtx.Other) + 1 : 25;
            health = 250 * b / 25;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var idx = state.getEnemiesForRead().find(this);
            state.properties.addOnCardPlayedHandler(new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        var move = state.getEnemiesForRead().get(idx).move;
                        if (move == ROLL_ATTACK || move == TWIN_SLAM) {
                            state.doNonAttackDamageToPlayer(4, true, this);
                        }
                    }
                }
            });
        }

        public String getName() {
            return "The Guardian";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return s.subSequence(0, s.length() - 1) + ", modeShift=" + modeShiftDmg + "/" + maxModeShiftDmg + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && maxModeShiftDmg == ((TheGuardian) o).maxModeShiftDmg && modeShiftDmg == ((TheGuardian) o).modeShiftDmg;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 2;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "2 inputs to keep track of current and max guardian mode shift damage";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx++] = (modeShiftDmg - 50) / 20f;
            input[idx] = (maxModeShiftDmg - 50) / 20f;
            return 2;
        }
    }

    public static class SlimeBoss extends Enemy {
        static int GOOP_SPRAY = 0;
        static int PREPARING = 1;
        static int SLAM = 2;
        static int SPLIT = 3;

        private int splitHealth;

        public SlimeBoss() {
            this(150);
        }

        public SlimeBoss(int health) {
            super(health, 4, false);
            properties.isBoss = true;
        }

        public SlimeBoss(SlimeBoss other) {
            super(other);
            splitHealth = other.splitHealth;
        }

        @Override public Enemy copy() {
            return new SlimeBoss(this);
        }

        @Override public int damage(double n, GameState state) {
            var dmg = super.damage(n, state);
            if (health <= properties.maxHealth / 2) {
                move = SPLIT;
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (health <= properties.maxHealth / 2) {
                move = SPLIT;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == GOOP_SPRAY) {
                for (int i = 0; i < 5; i++) {
                    state.addCardToDiscard(state.properties.slimeCardIdx);
                }
            } else if (move == SLAM) {
                state.enemyDoDamageToPlayer(this, 38, 1);
            } else if (move == SPLIT) {
                var enemies = state.getEnemiesForWrite();
                for (int i = 0; i < enemies.size(); i++) {
                    if (enemies.get(i) instanceof LargeAcidSlime) {
                        state.reviveEnemy(i, false, health);
                        ((LargeAcidSlime) enemies.get(i)).splitMaxHealth = health;
                    } else if (enemies.get(i) instanceof LargeSpikeSlime) {
                        state.reviveEnemy(i, false, health);
                        ((LargeSpikeSlime) enemies.get(i)).splitMaxHealth = health;
                    }
                }
                splitHealth = health;
                health = 0;
                state.adjustEnemiesAlive(-1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == GOOP_SPRAY) {
                move = PREPARING;
            } else if (move == PREPARING) {
                move = SLAM;
            } else if (move < 0 || move == SLAM){
                move = GOOP_SPRAY;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == GOOP_SPRAY) {
                return "Slimed 5";
            } else if (move == PREPARING) {
                return "Preparing";
            } else if (move == SLAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 38);
            } else if (move == SPLIT) {
                return "Split";
            }
            return "Unknown";
        }

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new CardOther.Slime()); }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(15, RandomGenCtx.Other) + 1;
            health = 10 * (training ? b : 15);
            if (health <= properties.maxHealth / 2) {
                move = SPLIT;
            }
        }

        public String getName() {
            return "Slime Boss";
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of health slime boss split at";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = splitHealth / (float) 75.0;
            return 1;
        }

    }

    public static class LargeSpikeSlime extends Enemy {
        private static final int FLAME_TACKLE = 0;
        private static final int LICK = 1;
        private static final int SPLIT = 2;

        private int splitMaxHealth;

        public void setSplitMaxHealth(int splitMaxHealth) {
            this.splitMaxHealth = splitMaxHealth;
        }

        public LargeSpikeSlime() {
            this(73);
        }

        public LargeSpikeSlime(int health) {
            super(health, 3, true);
            properties.canFrail = true;
            splitMaxHealth = properties.maxHealth;
        }

        public LargeSpikeSlime(LargeSpikeSlime other) {
            super(other);
            splitMaxHealth = other.splitMaxHealth;
        }

        public LargeSpikeSlime(int health, boolean startDead) {
            this(health);
            if (startDead) this.health = 0;
        }

        @Override public Enemy copy() {
            return new LargeSpikeSlime(this);
        }

        @Override public int damage(double n, GameState state) {
            var dmg = super.damage(n, state);
            if (health <= splitMaxHealth / 2) {
                move = SPLIT;
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (health <= splitMaxHealth / 2) {
                move = SPLIT;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == FLAME_TACKLE) {
                state.enemyDoDamageToPlayer(this, 18, 1);
                state.addCardToDiscard(state.properties.slimeCardIdx);
                state.addCardToDiscard(state.properties.slimeCardIdx);
            } else if (move == LICK) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 3);
            } else if (move == SPLIT) {
                var enemies = state.getEnemiesForWrite();
                for (int i = 0; i < enemies.size(); i++) {
                    if (enemies.get(i) instanceof MediumSpikeSlime) {
                        state.reviveEnemy(i, false, health);
                    }
                }
                health = 0;
                state.adjustEnemiesAlive(-1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == SPLIT) {
                return;
            }
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
            int newMove;
            if (r < 30) {
                if (move == FLAME_TACKLE && lastMove == FLAME_TACKLE) {
                    newMove = LICK;
                } else {
                    newMove = FLAME_TACKLE;
                }
            } else if (move == LICK) {
                newMove = FLAME_TACKLE;
            } else {
                newMove = LICK;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == FLAME_TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 18) + "+Slimed 2";
            } else if (move == LICK) {
                return "Frail 3";
            } else if (move == SPLIT) {
                return "Split";
            }
            return "Unknown";
        }

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new CardOther.Slime()); }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 67 + random.nextInt(7, RandomGenCtx.Other);
            splitMaxHealth = health;
        }

        public String getName() {
            return "Spike Slime (L)";
        }
    }

    public static class MediumSpikeSlime extends Enemy {
        private static final int FLAME_TACKLE = 0;
        private static final int LICK = 1;

        public MediumSpikeSlime() {
            this(34);
        }

        public MediumSpikeSlime(int health) {
            super(health, 2, true);
            properties.canFrail = true;
        }

        public MediumSpikeSlime(int health, boolean startDead) {
            this(health);
            if (startDead) this.health = 0;
        }

        public MediumSpikeSlime(MediumSpikeSlime other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new MediumSpikeSlime(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == FLAME_TACKLE) {
                state.enemyDoDamageToPlayer(this, 10, 1);
                state.addCardToDiscard(state.properties.slimeCardIdx);
            } else if (move == LICK) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
            int newMove;
            if (r < 30) {
                if (move == FLAME_TACKLE && lastMove == FLAME_TACKLE) {
                    newMove = LICK;
                } else {
                    newMove = FLAME_TACKLE;
                }
            } else if (move == LICK) {
                newMove = FLAME_TACKLE;
            } else {
                newMove = LICK;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == FLAME_TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10) + "+Slimed 1";
            } else if (move == LICK) {
                return "Frail 1";
            }
            return "Unknown";
        }

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new CardOther.Slime()); }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 29 + random.nextInt(6, RandomGenCtx.Other);
        }

        public String getName() {
            return "Spike Slime (M)";
        }
    }

    public static class SmallSpikeSlime extends Enemy {
        private static final int TACKLE = 0;

        public SmallSpikeSlime() {
            this(15);
        }

        public SmallSpikeSlime(int health) {
            super(health, 1, false);
        }

        public SmallSpikeSlime(SmallSpikeSlime other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new SmallSpikeSlime(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            state.enemyDoDamageToPlayer(this, 6, 1);
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = TACKLE;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 11 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Spike Slime (S)";
        }
    }


    public static class LargeAcidSlime extends Enemy {
        private static final int CORROSIVE_SPIT = 0;
        private static final int TACKLE = 1;
        private static final int LICK = 2;
        private static final int SPLIT = 3;

        private int splitMaxHealth;

        public LargeAcidSlime() {
            this(72);
        }

        public LargeAcidSlime(int health) {
            super(health, 3, true);
            properties.canWeaken = true;
            splitMaxHealth = properties.maxHealth;
        }

        public LargeAcidSlime(int health, boolean startDead) {
            this(health);
            if (startDead) this.health = 0;
        }

        public LargeAcidSlime(LargeAcidSlime other) {
            super(other);
            splitMaxHealth = other.splitMaxHealth;
        }

        @Override public Enemy copy() {
            return new LargeAcidSlime(this);
        }

        @Override public int damage(double n, GameState state) {
            var dmg = super.damage(n, state);
            if (health <= splitMaxHealth / 2) {
                move = SPLIT;
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (health <= splitMaxHealth / 2) {
                move = SPLIT;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CORROSIVE_SPIT) {
                state.enemyDoDamageToPlayer(this, 12, 1);
                state.addCardToDiscard(state.properties.slimeCardIdx);
                state.addCardToDiscard(state.properties.slimeCardIdx);
            } else if (move == TACKLE) {
                state.enemyDoDamageToPlayer(this, 18, 1);
            } else if (move == LICK) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2);
            } else if (move == SPLIT) {
                var enemies = state.getEnemiesForWrite();
                for (int i = 0; i < enemies.size(); i++) {
                    if (enemies.get(i) instanceof MediumAcidSlime) {
                        state.reviveEnemy(i, false, health);
                    }
                }
                health = 0;
                state.adjustEnemiesAlive(-1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == SPLIT) {
                return;
            }
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null);
            int newMove;
            if (r < 40) {
                if (move == CORROSIVE_SPIT && lastMove == CORROSIVE_SPIT) {
                    newMove = random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.6 ? TACKLE : LICK;
                } else {
                    newMove = CORROSIVE_SPIT;
                }
            } else if (r < 70) {
                if (move == TACKLE && lastMove == TACKLE) {
                    newMove = random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.6 ? CORROSIVE_SPIT : LICK;
                } else {
                    newMove = TACKLE;
                }
            } else if (move == LICK) {
                newMove = random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.4 ? CORROSIVE_SPIT : TACKLE;
            } else {
                newMove = LICK;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CORROSIVE_SPIT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12) + "+Slimed 2";
            } else if (move == TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 18);
            } else if (move == LICK) {
                return "Weak 2";
            } else if (move == SPLIT) {
                return "Split";
            }
            return "Unknown";
        }

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new CardOther.Slime()); }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 68 + random.nextInt(5, RandomGenCtx.Other);
            splitMaxHealth = health;
        }

        public String getName() {
            return "Acid Slime (L)";
        }
    }

    public static class MediumAcidSlime extends Enemy {
        private static final int CORROSIVE_SPIT = 0;
        private static final int TACKLE = 1;
        private static final int LICK = 2;

        public MediumAcidSlime() {
            this(34);
        }

        public MediumAcidSlime(int health) {
            super(health, 3, true);
            properties.canWeaken = true;
        }

        public MediumAcidSlime(int health, boolean startDead) {
            this(health);
            if (startDead) this.health = 0;
        }


        public MediumAcidSlime(MediumAcidSlime other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new MediumAcidSlime(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CORROSIVE_SPIT) {
                state.enemyDoDamageToPlayer(this, 8, 1);
                state.addCardToDiscard(state.properties.slimeCardIdx);
            } else if (move == TACKLE) {
                state.enemyDoDamageToPlayer(this, 12, 1);
            } else if (move == LICK) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null);
            int newMove;
            if (r < 40) {
                if (move == CORROSIVE_SPIT && lastMove == CORROSIVE_SPIT) {
                    newMove = random.nextBoolean(RandomGenCtx.EnemyChooseMove) ? TACKLE : LICK;
                } else {
                    newMove = CORROSIVE_SPIT;
                }
            } else if (r < 80) {
                if (move == TACKLE && lastMove == TACKLE) {
                    newMove = random.nextBoolean(RandomGenCtx.EnemyChooseMove) ? CORROSIVE_SPIT : LICK;
                } else {
                    newMove = TACKLE;
                }
            } else if (move == LICK) {
                newMove = random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.4 ? CORROSIVE_SPIT : TACKLE;
            } else {
                newMove = LICK;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CORROSIVE_SPIT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "+Slimed 1";
            } else if (move == TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12);
            } else if (move == LICK) {
                return "Weak 1";
            }
            return "Unknown";
        }

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new CardOther.Slime()); }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 29 + random.nextInt(6, RandomGenCtx.Other);
        }

        public String getName() {
            return "Acid Slime (M)";
        }
    }

    public static class SmallAcidSlime extends Enemy {
        private static final int TACKLE = 0;
        private static final int LICK = 1;

        public SmallAcidSlime() {
            this(13);
        }

        public SmallAcidSlime(int health) {
            super(health, 2, false);
            properties.canWeaken = true;
        }

        public SmallAcidSlime(SmallAcidSlime other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new SmallAcidSlime(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == TACKLE) {
                state.enemyDoDamageToPlayer(this, 4, 1);
            } else if (move == LICK) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == -1 || move == TACKLE) {
                move = LICK;
            } else {
                move = TACKLE;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 4);
            } else if (move == LICK) {
                return "Weak 1";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 9 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Acid Slime (S)";
        }
    }

    public static class RedSlaver extends Enemy {
        public static final int STAB = 0;
        public static final int SCRAPE = 1;
        public static final int ENTANGLE = 2;

        private boolean usedEntangle;

        public RedSlaver() {
            this(52);
        }

        public RedSlaver(int health) {
            super(health, 3, true);
            properties.canVulnerable = true;
            properties.canEntangle = true;
        }

        public RedSlaver(RedSlaver other) {
            super(other);
            usedEntangle = other.usedEntangle;
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && usedEntangle == ((RedSlaver) o).usedEntangle;
        }

        @Override public Enemy copy() {
            return new RedSlaver(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == STAB) {
                state.enemyDoDamageToPlayer(this, 14, 1);
            } else if (move == SCRAPE) {
                state.enemyDoDamageToPlayer(this, 9, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
            } else if (move == ENTANGLE) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.ENTANGLED, 1);
                usedEntangle = true;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == -1) {
                move = STAB;
                return;
            }
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null);
            int newMove;
            if (r >= 75 && !usedEntangle) {
                newMove = ENTANGLE;
            } else if (r >= 55 && usedEntangle && (move != STAB || lastMove != STAB)) {
                newMove = STAB;
            } else if (move != SCRAPE) {
                newMove = SCRAPE;
            } else {
                newMove = STAB;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == STAB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14);
            } else if (move == SCRAPE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "+Vulnerable 2";
            } else if (move == ENTANGLE) {
                return "Entangle";
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(3, RandomGenCtx.Other) + 1;
            if (training && b < 3) {
                health = (int) Math.round(((double) (health * b)) / 3);
            } else {
                health = 48 + random.nextInt(5, RandomGenCtx.Other);
            }
        }

        @Override public String getName() {
            return "Red Slaver";
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 inputs to keep track of whether red slaver have used entangle";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = usedEntangle ? -0.5f : 0.5f;
            return 1;
        }
    }

    public static class BlueSlaver extends Enemy {
        public static final int STAB = 0;
        public static final int RAKE = 1;

        public BlueSlaver() {
            this(52);
        }

        public BlueSlaver(int health) {
            super(health, 2, true);
            properties.canWeaken = true;
        }

        public BlueSlaver(BlueSlaver other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new BlueSlaver(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == STAB) {
                state.enemyDoDamageToPlayer(this, 13, 1);
            } else if (move == RAKE) {
                state.enemyDoDamageToPlayer(this, 8, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null);
            int newMove;
            if (r >= 40 && (move != STAB || lastMove != STAB)) {
                newMove = STAB;
            } else if (move != RAKE) {
                newMove = RAKE;
            } else {
                newMove = STAB;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == STAB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 13);
            } else if (move == RAKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "+Weak 2";
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(3, RandomGenCtx.Other) + 1;
            if (training && b < 3) {
                health = (int) Math.round(((double) (health * b)) / 3);
            } else {
                health = 48 + random.nextInt(5, RandomGenCtx.Other);
            }
        }

        @Override public String getName() {
            return "Blue Slaver";
        }
    }

    public static class JawWorm extends Enemy {
        public JawWorm(boolean isAct3) {
            this(46, isAct3);
        }

        public JawWorm(int health, boolean isAct3) {
            super(health, 3, true);
            properties.canGainStrength = true;
            properties.canGainBlock = true;
            properties.isAct3 = isAct3;
            if (properties.isAct3) {
                gainStrength(5);
                gainBlock(9);
            }
        }

        public JawWorm(JawWorm other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new JawWorm(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == 0) {
                state.enemyDoDamageToPlayer(this, 12, 1);
            } else if (move == 1) {
                strength += 5;
                gainBlock(9);
            } else if (move == 2) {
                state.enemyDoDamageToPlayer(this, 7, 1);
                gainBlock(5);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (!properties.isAct3 && move == -1) {
                move = 0;
                return;
            }
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null);
            int newMove;
            if (r < 25) {
                if (move == 0) {
                    newMove = random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.5625 ? 1 : 2;
                } else {
                    newMove = 0;
                }
            } else if (r < 55) {
                if (move == 2 && lastMove == 2) {
                    newMove = random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.357 ? 0 : 1;
                } else {
                    newMove = 2;
                }
            } else if (move == 1) {
                newMove = random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.416 ? 0 : 2;
            } else {
                newMove = 1;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == 0) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12);
            } else if (move == 1) {
                return "Gain 5 Strength+Block 9";
            } else if (move == 2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "+Block 5";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 42 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Jaw Worm";
        }
    }

    public static class Cultist extends Enemy {
        public static final int INCANTATION = 0;
        public static final int DARK_STRIKE = 1;

        public Cultist() {
            this(56);
        }

        public Cultist(int health) {
            super(health, 2, false);
            properties.canGainStrength = true;
        }

        public Cultist(Cultist other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Cultist(this);
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            if (move == DARK_STRIKE) {
                gainStrength(5);
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == INCANTATION) {
            } else if (move == DARK_STRIKE) {
                state.enemyDoDamageToPlayer(this, 6, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < DARK_STRIKE) {
                move++;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == INCANTATION) {
                return "Ritual 5";
            } else if (move == DARK_STRIKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
                health = (int) Math.round(((double) (health * difficulty)) / 5);
            } else {
                health = 50 + random.nextInt(7, RandomGenCtx.RandomEnemyHealth, 50);
            }
        }

        @Override public int getMaxRandomizeDifficulty() {
            return 5;
        }

        public String getName() {
            return "Cultist";
        }
    }

    public static class RedLouse extends Enemy {
        static final int BITE = 0;
        static final int GROW = 1;

        int d = -1;
        int curlUpAmount = 12;
        boolean hasCurledUp = false;
        boolean tookAttackDamage = false;

        public boolean hasCurledUp() {
            return hasCurledUp;
        }

        public int getD() {
            return d;
        }

        public int getCurlUpAmount() {
            return curlUpAmount;
        }

        public void setD(int d) {
            this.d = d;
        }

        public void setCurlUpAmount(int curlUpAmount) {
            this.curlUpAmount = curlUpAmount;
        }

        public RedLouse() {
            this(16);
        }

        public RedLouse(int health) {
            super(health, 2, true);
            properties.canGainStrength = true;
            properties.canGainBlock = true;
        }

        public RedLouse(RedLouse other) {
            super(other);
            d = other.d;
            curlUpAmount = other.curlUpAmount;
            hasCurledUp = other.hasCurledUp;
            tookAttackDamage = other.tookAttackDamage;
        }

        @Override public Enemy copy() {
            return new RedLouse(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BITE) {
                state.enemyDoDamageToPlayer(this, d, 1);
            } else if (move == GROW) {
                strength += 4;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null);
            int newMove;
            if (r < 25) {
                newMove = move == GROW ? BITE : GROW;
            } else if (move == BITE && lastMove == BITE) {
                newMove = GROW;
            } else {
                newMove = BITE;
            }
            lastMove = move;
            move = newMove;
            if (d < 0 && move == BITE) {
                d = 6 + random.nextInt(3, RandomGenCtx.Other);
            }
        }

        @Override public int damage(double n, GameState state) {
            tookAttackDamage = true;
            return super.damage(n, state);
        }

        @Override public void react(GameState state, Card card) {
            if (tookAttackDamage && !hasCurledUp) {
                hasCurledUp = true;
                gainBlock(curlUpAmount);
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BITE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, d);
            } else if (move == GROW) {
                return "Gain 4 Strength";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 11 + random.nextInt(6, RandomGenCtx.Other);
            curlUpAmount = 9 + random.nextInt(4, RandomGenCtx.Other);
        }

        public String getName() {
            return "Red Louse";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            if (d < 0) {
                return s;
            }
            return s.subSequence(0, s.length() - 1) + ", biteDmg=" + d + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && d == ((RedLouse) o).d;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Red Louse Bite damage";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = d < 0 ? -0.5f : d / 8.0f;
            return 1;
        }
    }

    public static class GreenLouse extends Enemy {
        static final int BITE = 0;
        static final int SPIT_WEB = 1;

        int d = -1;
        int curlUpAmount = 12;
        boolean hasCurledUp = false;
        boolean tookAttackDamage = false;

        public boolean hasCurledUp() {
            return hasCurledUp;
        }

        public int getD() {
            return d;
        }

        public int getCurlUpAmount() {
            return curlUpAmount;
        }

        public void setD(int d) {
            this.d = d;
        }

        public void setCurlUpAmount(int curlUpAmount) {
            this.curlUpAmount = curlUpAmount;
        }

        public GreenLouse() {
            this(18);
        }

        public GreenLouse(int health) {
            super(health, 2, true);
            properties.canWeaken = true;
        }

        public GreenLouse(GreenLouse other) {
            super(other);
            d = other.d;
            curlUpAmount = other.curlUpAmount;
            hasCurledUp = other.hasCurledUp;
            tookAttackDamage = other.tookAttackDamage;
        }

        @Override public Enemy copy() {
            return new GreenLouse(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BITE) {
                state.enemyDoDamageToPlayer(this, d, 1);
            } else if (move == SPIT_WEB) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null);
            int newMove;
            if (r < 25) {
                newMove = move == SPIT_WEB ? BITE : SPIT_WEB;
            } else if (move == BITE && lastMove == BITE) {
                newMove = SPIT_WEB;
            } else {
                newMove = BITE;
            }
            lastMove = move;
            move = newMove;
            if (d < 0 && move == BITE) {
                d = 6 + random.nextInt(3, RandomGenCtx.Other);
            }
        }

        @Override public int damage(double n, GameState state) {
            tookAttackDamage = true;
            return super.damage(n, state);
        }

        @Override public void react(GameState state, Card card) {
            if (tookAttackDamage && !hasCurledUp) {
                hasCurledUp = true;
                gainBlock(curlUpAmount);
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BITE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, d);
            } else if (move == SPIT_WEB) {
                return "Applies 2 Week";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 12 + random.nextInt(7, RandomGenCtx.Other);
            curlUpAmount = 9 + random.nextInt(4, RandomGenCtx.Other);
        }

        public String getName() {
            return "Green Louse";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            if (d < 0) {
                return s;
            }
            return s.subSequence(0, s.length() - 1) + ", biteDmg=" + d + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && d == ((GreenLouse) o).d;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Green Louse Bite damage";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = d < 0 ? -0.5f : d / 8.0f;
            return 1;
        }
    }

    public static class FungiBeast extends Enemy {
        static final int BITE = 0;
        static final int GROW = 1;

        boolean isDead;

        public FungiBeast() {
            this(28);
        }

        public FungiBeast(int health) {
            super(health, 2, true);
            properties.canVulnerable = true;
            properties.canGainStrength = true;
        }

        public FungiBeast(FungiBeast other) {
            super(other);
            isDead = other.isDead;
        }

        @Override public Enemy copy() {
            return new FungiBeast(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BITE) {
                state.enemyDoDamageToPlayer(this, 6, 1);
            } else if (move == GROW) {
                strength += 5;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null);
            int newMove;
            if (r < 60) {
                if (move == 1 && lastMove == 1) {
                    newMove = GROW;
                } else {
                    newMove = BITE;
                }
            } else if (move == GROW) {
                newMove = BITE;
            } else {
                newMove = GROW;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public int damage(double n, GameState state) {
            var dmg = super.damage(n, state);
            if (!isDead && health <= 0) {
                isDead = true;
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            super.nonAttackDamage(n, blockable, state);
            if (!isDead && health <= 0) {
                isDead = true;
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BITE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6);
            } else if (move == GROW) {
                return "Gain 5 Strength";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 24 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Fungi Beast";
        }
    }

    public static class Looter extends Enemy {
        static final int MUG_1 = 0;
        static final int MUG_2 = 1;
        static final int LUNGE = 2;
        static final int SMOKE_BOMB = 3;
        static final int ESCAPE = 4;

        boolean escaped;

        public Looter() {
            this(50);
        }

        public Looter(int health) {
            super(health, 5, false);
            properties.canGainBlock = true;
        }

        public Looter(Looter other) {
            super(other);
            escaped = other.escaped;
        }

        @Override public Enemy copy() {
            return new Looter(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == MUG_1 || move == MUG_2) {
                state.enemyDoDamageToPlayer(this, 11, 1);
            } else if (move == LUNGE) {
                state.enemyDoDamageToPlayer(this, 14, 1);
            } else if (move == SMOKE_BOMB) {
                state.enemyDoDamageToPlayer(this, 6, 1);
            } else if (move == ESCAPE) {
                escaped = true;
                health = 0;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move < 0) {
                newMove = MUG_1;
            } else if (move == MUG_1) {
                newMove = MUG_2;
            } else if (move == MUG_2) {
                state.setIsStochastic();
                newMove = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null) < 50 ? LUNGE : SMOKE_BOMB;
            } else if (move == LUNGE) {
                newMove = SMOKE_BOMB;
            } else {
                newMove = ESCAPE;
            }
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == MUG_1 || move == MUG_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11);
            } else if (move == LUNGE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14);
            } else if (move == SMOKE_BOMB) {
                return "Block 6";
            } else if (move == ESCAPE) {
                return "Escape";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 46 + random.nextInt(5, RandomGenCtx.Other);
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of whether looter has escaped";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = escaped ? 0.5f : 0;
            return 1;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addExtraTrainingTarget("Looter", new GameProperties.TrainingTargetRegistrant() {
                @Override public void setVArrayIdx(int idx) {
                    state.properties.looterVArrayIdx = idx;
                }
            }, new TrainingTarget() {
                @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                    if (isTerminal > 0) {
                        boolean escaped = false;
                        for (var enemy : state.getEnemiesForRead()) {
                            if (enemy instanceof Looter looter) {
                                if (looter.escaped) {
                                    escaped = true;
                                    break;
                                }
                            }
                        }
                        v[GameState.V_OTHER_IDX_START + state.properties.looterVArrayIdx] = escaped ? 1.0f : 0.0f;
                    } else if (isTerminal == 0) {
                        v[GameState.V_OTHER_IDX_START + state.properties.looterVArrayIdx] = state.getVOther(state.properties.looterVArrayIdx);
                    }
                }

                @Override public void updateQValues(GameState state, double[] v) {
                    double value = v[GameState.V_OTHER_IDX_START + state.properties.looterVArrayIdx];
                    v[GameState.V_HEALTH_IDX] *= (0.9 + (1 - value) * 0.1);
                }
            });
        }

        public String getName() {
            return "Looter";
        }
    }

    public static class Mugger extends Enemy {
        static final int MUG_1 = 0;
        static final int MUG_2 = 1;
        static final int LUNGE = 2;
        static final int SMOKE_BOMB = 3;
        static final int ESCAPE = 4;

        public boolean escaped;

        public Mugger() {
            this(54);
        }

        public Mugger(int health) {
            super(health, 5, false);
            properties.canGainBlock = true;
        }

        public Mugger(Mugger other) {
            super(other);
            other.escaped = escaped;
        }

        @Override public Enemy copy() {
            return new Mugger(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == MUG_1 || move == MUG_2) {
                state.enemyDoDamageToPlayer(this, 11, 1);
            } else if (move == LUNGE) {
                state.enemyDoDamageToPlayer(this, 18, 1);
            } else if (move == SMOKE_BOMB) {
                state.enemyDoDamageToPlayer(this, 17, 1);
            } else if (move == ESCAPE) {
                escaped = true;
                health = 0;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move < 0) {
                newMove = MUG_1;
            } else if (move == MUG_1) {
                newMove = MUG_2;
            } else if (move == MUG_2) {
                state.setIsStochastic();
                newMove = random.nextInt(100, RandomGenCtx.EnemyChooseMove, null) < 50 ? LUNGE : SMOKE_BOMB;
            } else if (move == LUNGE) {
                newMove = SMOKE_BOMB;
            } else {
                newMove = ESCAPE;
            }
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == MUG_1 || move == MUG_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11);
            } else if (move == LUNGE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 18);
            } else if (move == SMOKE_BOMB) {
                return "Block 17";
            } else if (move == ESCAPE) {
                return "Escape";
            }
            return "Unknown";
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of whether mugger has escaped";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = escaped ? 0.5f : 0;
            return 1;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addExtraTrainingTarget("Mugger", new GameProperties.TrainingTargetRegistrant() {
                @Override public void setVArrayIdx(int idx) {
                    state.properties.looterVArrayIdx = idx;
                }
            }, new TrainingTarget() {
                @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                    boolean escaped = false;
                    for (var enemy : state.getEnemiesForRead()) {
                        if (enemy instanceof Mugger mugger) {
                            if (mugger.escaped) {
                                escaped = true;
                                break;
                            }
                        }
                    }
                    if (isTerminal > 0 || escaped) {
                        v[GameState.V_OTHER_IDX_START + state.properties.looterVArrayIdx] = escaped ? 1.0f : 0.0f;
                    } else if (isTerminal == 0) {
                        v[GameState.V_OTHER_IDX_START + state.properties.looterVArrayIdx] = state.getVOther(state.properties.looterVArrayIdx);
                    }
                }

                @Override public void updateQValues(GameState state, double[] v) {
                    double value = v[GameState.V_OTHER_IDX_START + state.properties.looterVArrayIdx];
                    v[GameState.V_HEALTH_IDX] *= (0.9 + (1 - value) * 0.1);
                }
            });
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 50 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Mugger";
        }
    }

    public static class FatGremlin extends Enemy {
        static final int SMASH = 0;

        public FatGremlin() {
            this(18);
        }

        public FatGremlin(int health) {
            super(health, 1, false);
            properties.canWeaken = true;
            properties.canFrail = true;
        }

        public FatGremlin(FatGremlin other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new FatGremlin(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SMASH) {
                state.enemyDoDamageToPlayer(this, 5, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = SMASH;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SMASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 5) + "+Frail 1+Weak 1";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 14 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Fat Gremlin";
        }
    }

    public static class MadGremlin extends Enemy {
        static final int SCRATCH = 0;

        public MadGremlin() {
            this(25);
        }

        public MadGremlin(int health) {
            super(health, 1, false);
            properties.canGainStrength = true;
        }

        public MadGremlin(MadGremlin other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new MadGremlin(this);
        }

        @Override public int damage(double n, GameState state) {
            var dmg = super.damage(n, state);
            if (dmg > 0) {
                gainStrength(2);
            }
            return dmg;
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SCRATCH) {
                state.enemyDoDamageToPlayer(this, 5, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = SCRATCH;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SCRATCH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 5);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 21 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Mad Gremlin";
        }
    }

    public static class SneakyGremlin extends Enemy {
        static final int PUNCTURE = 0;

        public SneakyGremlin() {
            this(15);
        }

        public SneakyGremlin(int health) {
            super(health, 1, false);
        }

        public SneakyGremlin(SneakyGremlin other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new SneakyGremlin(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == PUNCTURE) {
                state.enemyDoDamageToPlayer(this, 10, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = PUNCTURE;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == PUNCTURE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 11 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Sneaky Gremlin";
        }
    }

    public static class ShieldGremlin extends Enemy {
        static final int PROTECT = 0;
        static final int SHIELD_BASH = 1;

        public ShieldGremlin() {
            this(17);
        }

        public ShieldGremlin(int health) {
            super(health, 2, false);
        }

        public ShieldGremlin(ShieldGremlin other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new ShieldGremlin(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == PROTECT) {
                if (state.enemiesAlive == 1) { // can only be shield gremlin
                    gainBlock(11);
                } else {
                    int r = 0;
                    if (state.enemiesAlive > 2) {
                        state.setIsStochastic();
                        r = state.getSearchRandomGen().nextInt(state.enemiesAlive - 1, RandomGenCtx.ShieldGremlin);
                    }
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        if (enemy != self) {
                            if ((--r) < 0) {
                                enemy.gainBlock(11);
                                break;
                            }
                        }
                    }
                }
            } else if (move == SHIELD_BASH) {
                state.enemyDoDamageToPlayer(this, 8, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (state.enemiesAlive > 1) {
                move = PROTECT;
            } else {
                move = SHIELD_BASH;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == PROTECT) {
                return "Gives 11 block to a random ally";
            } else if (move == SHIELD_BASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 13 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Shield Gremlin";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var enemies = state.getEnemiesForWrite();
            for (int i = 0; i < enemies.size(); i++) {
                if (enemies.get(i).getName().contains("Gremlin")) {
                    enemies.get(i).properties.canGainBlock = true;
                }
            }
        }
    }

    public static class GremlinWizard extends Enemy {
        static final int CHARGING_1 = 0;
        static final int CHARGING_2 = 1;
        static final int ULTIMATE_BLAST = 2;

        public GremlinWizard() {
            this(26);
        }

        public GremlinWizard(int health) {
            super(health, 3, false);
        }

        public GremlinWizard(GremlinWizard other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new GremlinWizard(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == ULTIMATE_BLAST) {
                state.enemyDoDamageToPlayer(this, 30, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move < ULTIMATE_BLAST) {
                move++;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CHARGING_1 || move == CHARGING_2) {
                return "Charging";
            } else if (move == ULTIMATE_BLAST) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 30);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            health = 22 + random.nextInt(5, RandomGenCtx.Other);
        }

        public String getName() {
            return "Gremlin Wizard";
        }
    }
}
