package com.alphaStS.enemy;

import com.alphaStS.*;
import com.alphaStS.card.Card;
import com.alphaStS.card.CardOther;

import java.util.List;

public class EnemyCity {
    public static class TheChamp extends Enemy {
        private static final int DEFENSIVE_STANCE = 0;
        private static final int FACE_SLAP = 1;
        private static final int TAUNT = 2;
        private static final int HEAVY_SLASH = 3;
        private static final int GLOAT = 4;
        private static final int EXECUTE = 5;
        private static final int ANGER = 6;

        private int numOfDefensiveStance;
        private int numTurns;
        private boolean angered;

        public TheChamp() {
            this(440);
        }

        public TheChamp(int health) {
            super(health, 7, true);
            properties.canGainStrength = true;
            properties.canGainMetallicize = true;
            properties.canWeaken = true;
            properties.canVulnerable = true;
            properties.canFrail = true;
            properties.canGainBlock = true;
            properties.isBoss = true;
        }

        public TheChamp(TheChamp other) {
            super(other);
            numOfDefensiveStance = other.numOfDefensiveStance;
            numTurns = other.numTurns;
            angered = other.angered;
        }

        @Override public Enemy copy() {
            return new TheChamp(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == DEFENSIVE_STANCE) {
                gainBlock(20);
                gainMetallicize(7);
                numOfDefensiveStance++;
            } else if (move == FACE_SLAP) {
                state.enemyDoDamageToPlayer(this, 14, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 2);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
            } else if (move == TAUNT) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
            } else if (move == HEAVY_SLASH) {
                state.enemyDoDamageToPlayer(this, 18, 1);
            } else if (move == GLOAT) {
                gainStrength(4);
            } else if (move == EXECUTE) {
                state.enemyDoDamageToPlayer(this, 10, 2);
            } else if (move == ANGER) {
                removeAllDebuffs();
                gainStrength(12);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            numTurns++;
            if (health < properties.maxHealth / 2 && !angered) {
                angered = true;
                newMove = ANGER;
            } else if (angered && move != EXECUTE && lastMove != EXECUTE) {
                newMove = EXECUTE;
            } else if (numTurns == 4 && !angered) {
                numTurns = 0;
                newMove = TAUNT;
            } else {
                int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                if (move != DEFENSIVE_STANCE && numOfDefensiveStance < 2 && r < 30) {
                    newMove = DEFENSIVE_STANCE;
                } else if ((move != GLOAT && move != DEFENSIVE_STANCE) && r < 30) {
                    newMove = GLOAT;
                } else if (move != FACE_SLAP && r < 55) {
                    newMove = FACE_SLAP;
                } else if (move != HEAVY_SLASH) {
                    newMove = HEAVY_SLASH;
                } else {
                    newMove = FACE_SLAP;
                }
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == DEFENSIVE_STANCE) {
                return "Block 20+Metallicize 7";
            } else if (move == FACE_SLAP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14) + "+Frail 2+Vulnerable 2";
            } else if (move == TAUNT) {
                return "Weak 2+Vulnerable 2";
            } else if (move == HEAVY_SLASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 18);
            } else if (move == GLOAT) {
                return "Strength 4";
            } else if (move == EXECUTE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10) + "x2";
            } else if (move == ANGER) {
                return "Remove All Debuffs+Strength 12";
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(20, RandomGenCtx.Other) + 1;
            if (training && b < 20) {
                health = (int) Math.round(((double) (health * b)) / 20);
            } else {
                health = 440;
            }
        }

        @Override public String getName() {
            return "The Champ";
        }

        @Override public String toString(GameState state) { // can skip numOfDefensiveStance because of metallicize
            String s = super.toString(state);
            if (angered) {
                return s.subSequence(0, s.length() - 1) + ", angered}";
            } else {
                return s.subSequence(0, s.length() - 1) + ", untilTaunt=" + (4 - numTurns) + "}";
            }
        }

        @Override public boolean equals(Object o) { // can skip numOfDefensiveStance because of metallicize
            return super.equals(o) && angered == ((TheChamp) o).angered && numTurns == ((TheChamp) o).numTurns;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 5;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "5 input to keep track of whether The Champ has been angered and whether it's close to taunting";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = angered ? 0.5f : -0.5f;
            for (int i = 0; i < 4; i++) {
                input[idx + 1 + i] = 0f;
            }
            if (!angered) {
                input[idx + 1 + numTurns] = 0.5f;
            }
            return 5;
        }
    }

    public static class BronzeAutomaton extends Enemy {
        public static final int SPAWN_ORBS = 0;
        private static final int FLAIL_1 = 1;
        private static final int BOOST_1 = 2;
        private static final int FLAIL_2 = 3;
        private static final int BOOST_2 = 4;
        private static final int HYPER_BEAM = 5;
        private static final int BOOST_3 = 6;

        public BronzeAutomaton() {
            this(320);
        }

        public BronzeAutomaton(int health) {
            super(health, 7, false);
            artifact = 3;
            properties.canGainStrength = true;
            properties.canGainBlock = true;
            properties.hasArtifact = true;
            properties.isBoss = true;
        }

        public BronzeAutomaton(BronzeAutomaton other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new BronzeAutomaton(this);
        }

        @Override public int damage(double n, GameState state) {
            var dmg = super.damage(n, state);
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
            if (move == SPAWN_ORBS) {
                var enemies = state.getEnemiesForWrite();
                for (int i = 0; i < enemies.size(); i++) {
                    if (enemies.get(i) instanceof BronzeOrb) {
                        state.reviveEnemy(i, false, -1);
                        state.getEnemiesForWrite().getForWrite(i).reviveReset();
                    }
                }
            } else if (move == BOOST_1 || move == BOOST_2 || move == BOOST_3) {
                gainStrength(4);
                gainBlock(12);
            } else if (move == FLAIL_1 || move == FLAIL_2) {
                state.enemyDoDamageToPlayer(this, 8, 2);
            } else if (move == HYPER_BEAM) {
                state.enemyDoDamageToPlayer(this, 50, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move++;
            if (move > BOOST_3) {
                move = FLAIL_1;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SPAWN_ORBS) {
                return "Spawn Orbs";
            } else if (move == BOOST_1 || move == BOOST_2 || move == BOOST_3) {
                return "Gain 4 Strength and 12 Block";
            } else if (move == FLAIL_1 || move == FLAIL_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "*2";
            } else if (move == HYPER_BEAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 50);
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(20, RandomGenCtx.Other) + 1;
            if (training && b < 20) {
                health = (int) Math.round(((double) (health * b)) / 20);
            } else {
                health = 320;
            }
        }

        @Override public String getName() {
            return "Bronze Automaton";
        }
    }

    public static class BronzeOrb extends Enemy {
        private static final int STASIS = 0;
        private static final int BEAM = 1;
        private static final int SUPPORT_BEAM = 2;

        private boolean usedStasis;
        private int stasisCardIdx = -1;

        public BronzeOrb() {
            this(60);
        }

        public BronzeOrb(int health) {
            super(health, 3, true);
            this.health = 0;
        }

        public BronzeOrb(BronzeOrb other) {
            super(other);
            usedStasis = other.usedStasis;
            stasisCardIdx = other.stasisCardIdx;
        }

        @Override public Enemy copy() {
            return new BronzeOrb(this);
        }

        public boolean usedStasis() {
            return usedStasis;
        }

        public int getStasisCard() {
            return stasisCardIdx;
        }

        @Override public int damage(double n, GameState state) {
            int prevHealth = health;
            var dmg = super.damage(n, state);
            if (prevHealth > 0 && health <= 0 && usedStasis && stasisCardIdx >= 0) {
                state.addCardToHand(stasisCardIdx);
            }
            return dmg;
        }

        @Override public void nonAttackDamage(int n, boolean blockable, GameState state) {
            int prevHealth = health;
            super.nonAttackDamage(n, blockable, state);
            if (prevHealth > 0 && health <= 0 && usedStasis && stasisCardIdx >= 0) {
                state.addCardToHand(stasisCardIdx);
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == STASIS) {
                var cards = state.getNumCardsInDeck() == 0 ? state.getDiscardArrForWrite() : state.getDeckArrForWrite();
                var cardsLen = state.getNumCardsInDeck() == 0 ? state.getNumCardsInDiscard() : state.getNumCardsInDeck();
                var cardRarityCounts = new int[Card.RARE + 1];
                var cardRarityDiffCount = new int[Card.RARE + 1];
                var seen = new boolean[state.properties.cardDict.length];
                for (int i = 0; i < cardsLen; i++) {
                    cardRarityCounts[state.properties.cardDict[cards[i]].rarity]++;
                    if (!seen[cards[i]]) {
                        cardRarityDiffCount[state.properties.cardDict[cards[i]].rarity]++;
                        seen[cards[i]] = true;
                    }
                }
                int rarity = -1;
                for (int i = Card.RARE; i >= 0; i--) {
                    if (cardRarityCounts[i] > 0) {
                        rarity = i;
                        break;
                    }
                }
                if (rarity >= 0) {
                    int r = 0;
                    if (cardRarityDiffCount[rarity] > 1) {
                        state.setIsStochastic();
                        if (state.properties.random instanceof InteractiveMode.RandomGenInteractive rgi && !rgi.rngOn) {
                            r = rgi.selectBronzeOrbStasis(state, cards, cardsLen, rarity,this, state.getEnemiesForRead().find(self));
                        } else {
                            r = state.getSearchRandomGen().nextInt(cardRarityCounts[rarity], RandomGenCtx.BronzeOrb, this);
                        }
                    }
                    int acc = 0;
                    for (int i = 0; i < cardsLen; i++) {
                        if (rarity == state.properties.cardDict[cards[i]].rarity) {
                            if (acc == r) {
                                stasisCardIdx = cards[i];
                                if (state.getNumCardsInDeck() > 0) {
                                    state.removeCardFromDeck(cards[i]);
                                } else {
                                    state.removeCardFromDiscardByPosition(i);
                                }
                                break;
                            }
                            acc++;
                        }
                    }
                    if (stasisCardIdx >= 0) {
                        state.getStateDesc().append(state.getStateDesc().length() > 0 ? "; " : "").append("Stasis Took ").append(state.properties.cardDict[stasisCardIdx].cardName);
                    }
                }
                usedStasis = true;
            } else if (move == BEAM) {
                state.enemyDoDamageToPlayer(this, 8, 1);
            } else if (move == SUPPORT_BEAM) {
                var enemies = state.getEnemiesForWrite();
                for (int i = 0; i < enemies.size(); i++) {
                    if (enemies.get(i) instanceof BronzeAutomaton) {
                        enemies.getForWrite(i).gainBlock(12);
                    }
                }
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
            if (!this.usedStasis && r >= 25) {
                newMove = STASIS;
            } else if (r >= 70 && !(move == SUPPORT_BEAM && lastMove == SUPPORT_BEAM)) {
                newMove = SUPPORT_BEAM;
            } else if (!(move == BEAM && lastMove == BEAM)) {
                newMove = BEAM;
            } else {
                newMove = SUPPORT_BEAM;
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == STASIS) {
                return "Stasis";
            } else if (move == BEAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8);
            } else if (move == SUPPORT_BEAM) {
                return "Support Beam";
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(6, RandomGenCtx.Other) + 1;
            if (training && b < 6) {
                health = (int) Math.round(((double) (properties.maxHealth * b)) / 6);
            } else {
                health = 54 + random.nextInt(7, RandomGenCtx.Other);
            }
        }

        @Override public String getName() {
            return "Bronze Orb";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            if (!usedStasis) {
                return s.subSequence(0, s.length() - 1) + ", stasisNotUsed}";
            } else if (stasisCardIdx >= 0) {
                return s.subSequence(0, s.length() - 1) + ", stasisCard=" + state.properties.cardDict[stasisCardIdx].cardName + "}";
            } else {
                return s;
            }
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && usedStasis == ((BronzeOrb) o).usedStasis && stasisCardIdx == ((BronzeOrb) o).stasisCardIdx;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1 + prop.realCardsLen;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1+" + prop.realCardsLen + " inputs to keep track of stasis used + stasis card";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = usedStasis ? 0.5f : 0;
            if (stasisCardIdx >= 0) {
                input[idx + 1 + stasisCardIdx] = 0.5f;
            }
            return 1 + prop.realCardsLen;
        }
    }

    public static class TheCollector extends Enemy {
        public static final int BUFF = 0;
        private static final int FIREBALL = 1;
        private static final int MEGA_DEBUFF = 2;
        private static final int SPAWN = 3;

        private int turn;

        public TheCollector() {
            this(300);
        }

        public TheCollector(int health) {
            super(health, 4, true);
            properties.canGainStrength = true;
            properties.canGainBlock = true;
            properties.canVulnerable = true;
            properties.canWeaken = true;
            properties.canFrail = true;
            properties.isBoss = true;
        }

        public TheCollector(TheCollector other) {
            super(other);
            turn = other.turn;
        }

        @Override public Enemy copy() {
            return new TheCollector(this);
        }

        @Override public int damage(double n, GameState state) {
            var dmg = super.damage(n, state);
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
            if (move == BUFF) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemy.gainStrength(5);
                }
                gainBlock(23);
            } else if (move == FIREBALL) {
                state.enemyDoDamageToPlayer(this, 21, 1);
            } else if (move == MEGA_DEBUFF) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 5);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 5);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 5);
            } else if (move == SPAWN) {
                EnemyList enemies = state.getEnemiesForWrite();
                for (int i = 0; i < enemies.size(); i++) {
                    if (enemies.get(i).getHealth() == 0 && enemies.get(i) instanceof TorchHead) {
                        state.reviveEnemy(i, false, -1);
                    }
                }
            }
        }

        private boolean isMinionDead(GameState state) {
            for (var enemy : state.getEnemiesForRead()) {
                if (enemy instanceof EnemyCity.TorchHead && enemy.getHealth() == 0) {
                    return true;
                }
            }
            return false;
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            turn++;
            if (move < 0) {
                newMove = SPAWN;
            } else if (turn == 4) {
                newMove = MEGA_DEBUFF;
            } else {
                int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                if (r <= 25 && isMinionDead(state) && move != SPAWN) {
                    newMove = SPAWN;
                } else if (r <= 70 && (move != FIREBALL || lastMove != FIREBALL)) {;
                    newMove = FIREBALL;
                } else if (move != BUFF) {
                    newMove = BUFF;
                } else {
                    newMove = FIREBALL;
                }
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BUFF) {
                return "All Enemies Gain 5 Strength+Gain 23 Block";
            } else if (move == FIREBALL) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 21);
            } else if (move == MEGA_DEBUFF) {
                return "Weak 5+Vulnerable 5+Frail 5";
            } else if (move == SPAWN) {
                return "Summon Torch Heads";
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(15, RandomGenCtx.Other) + 1;
            if (training && b < 15) {
                health = (int) Math.round(((double) (health * b)) / 15);
            } else {
                health = 300;
            }
        }

        @Override public String getName() {
            return "The Collector";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && turn == ((EnemyCity.TheCollector) o).turn;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 inputs to keep track of turn until Collector debuffs";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = (4 - turn) / 4.0f;
            return 1;
        }
    }

    public static class TorchHead extends Enemy {
        private static final int TACKLE = 0;

        public TorchHead() {
            this(45);
        }

        public TorchHead(int health) {
            super(health, 1, false);
            properties.canGainStrength = true;
            this.health = 0;
        }

        public TorchHead(TorchHead other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new TorchHead(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == TACKLE) {
                state.enemyDoDamageToPlayer(this, 7, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            lastMove = move;
            move = TACKLE;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == TACKLE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7);
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(2, RandomGenCtx.Other) + 1;
            if (training && b < 2) {
                health = (int) Math.round(((double) (properties.maxHealth * b)) / 2);
            } else {
                health = 40 + random.nextInt(6, RandomGenCtx.Other);
            }
        }

        @Override public String getName() {
            return "Torch Head";
        }
    }

    // ******************************************************************************************
    // ******************************************************************************************
    // ******************************************************************************************

    public static class GremlinLeader extends Enemy {
        static final int ENCOURAGE = 0;
        static final int RALLY = 1;
        static final int STAB = 2;

        int startOfTurnEnemiesAlive = -1;

        public GremlinLeader() {
            this(155);
        }

        public GremlinLeader(int health) {
            super(health, 3, false);
            properties.isElite = true;
            properties.actNumber = 2;
            properties.canGainStrength = true;
            properties.canGainBlock = true;
        }

        public GremlinLeader(GremlinLeader other) {
            super(other);
            startOfTurnEnemiesAlive = other.startOfTurnEnemiesAlive;
        }

        @Override public Enemy copy() {
            return new GremlinLeader(this);
        }

        @Override public int damage(double n, GameState state) {
            var dmg = super.damage(n, state);
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

        @Override public void saveStateForNextMove(GameState state) {
            startOfTurnEnemiesAlive = state.enemiesAlive;
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == ENCOURAGE) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemy.gainStrength(5);
                    if (enemy != this) {
                        enemy.gainBlock(10);
                    }
                }
            } else if (move == RALLY) {
                state.setIsStochastic();
                if (true) {
                    var enemies = state.getEnemiesForWrite();
                    var startIdx = 0;
                    for (int i = 0; i < enemies.size(); i++) {
                        if (enemies.get(i) instanceof Enemy.MergedEnemy m && m.possibleEnemies.get(0) instanceof EnemyExordium.MadGremlin) {
                            startIdx = i;
                            break;
                        }
                    }
                    for (int i = 0; i < 2; i++) {
                        int j;
                        for (j = 0; j < 3; j++) {
                            if (!enemies.get(startIdx + j).isAlive()) {
                                state.reviveEnemy(startIdx + j, false, 1);
                                state.getEnemiesForWrite().getForWrite(startIdx + j).reviveReset();
                                break;
                            }
                        }
                        int r = state.getSearchRandomGen().nextInt(8, RandomGenCtx.GremlinLeader, null);
                        if (r < 2) { // Mad Gremlin
                            ((Enemy.MergedEnemy) enemies.get(startIdx + j)).setEnemy(0);
                        } else if (r < 4) { // Sneaky Gremlin
                            ((Enemy.MergedEnemy) enemies.get(startIdx + j)).setEnemy(1);
                        } else if (r < 6) { // Fat Gremlin
                            ((Enemy.MergedEnemy) enemies.get(startIdx + j)).setEnemy(2);
                        } else if (r < 7) { // Shield Gremlin
                            ((Enemy.MergedEnemy) enemies.get(startIdx + j)).setEnemy(3);
                        } else { // Gremlin Wizard
                            ((Enemy.MergedEnemy) enemies.get(startIdx + j)).setEnemy(4);
                        }
                        var enemy = (MergedEnemy) enemies.get(startIdx + j);
                        enemy.randomize(state.getSearchRandomGen(), state.properties.curriculumTraining, -1);
                        enemy.properties.origHealth = enemy.getHealth();
                    }
                    if (state.enemiesAlive != 4) {
                        var j = 0;
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            if (enemy.getHealth() > 0) {
                                j += 1;
                            }
                        }
                        if (state.enemiesAlive != j) {
                            System.out.println("!!!!! ENEMIES_ALIVE IS WRONG " + state.enemiesAlive + ": " + j + ": " + state + ": " + startOfTurnEnemiesAlive);
                        }
                    }
                } else {
                    var enemies = state.getEnemiesForWrite();
                    var startIdx = 0;
                    for (int i = 0; i < enemies.size(); i++) {
                        if (enemies.get(i) instanceof EnemyExordium.MadGremlin) {
                            startIdx = i;
                            break;
                        }
                    }
                    for (int i = 0; i < 2; i++) {
                        int r = state.getSearchRandomGen().nextInt(8, RandomGenCtx.GremlinLeader, null);
                        var idx = -1;
                        if (r < 2) { // Mad Gremlin
                            idx = startIdx + 2;
                            if (!enemies.get(startIdx).isAlive()) {
                                idx = startIdx;
                            } else if (!enemies.get(startIdx + 1).isAlive()) {
                                idx = startIdx + 1;
                            }
                        } else if (r < 4) { // Sneaky Gremlin
                            idx = startIdx + 5;
                            if (!enemies.get(startIdx + 3).isAlive()) {
                                idx = startIdx + 3;
                            } else if (!enemies.get(startIdx + 4).isAlive()) {
                                idx = startIdx + 4;
                            }
                        } else if (r < 6) { // Fat Gremlin
                            idx = startIdx + 8;
                            if (!enemies.get(startIdx + 6).isAlive()) {
                                idx = startIdx + 6;
                            } else if (!enemies.get(startIdx + 7).isAlive()) {
                                idx = startIdx + 7;
                            }
                        } else if (r < 7) { // Shield Gremlin
                            idx = startIdx + 11;
                            if (!enemies.get(startIdx + 9).isAlive()) {
                                idx = startIdx + 9;
                            } else if (!enemies.get(startIdx + 10).isAlive()) {
                                idx = startIdx + 10;
                            }
                        } else { // Gremlin Wizard
                            idx = startIdx + 14;
                            if (!enemies.get(startIdx + 12).isAlive()) {
                                idx = startIdx + 12;
                            } else if (!enemies.get(startIdx + 13).isAlive()) {
                                idx = startIdx + 13;
                            }
                        }
                        state.reviveEnemy(idx, false, -1);
                        state.getEnemiesForWrite().getForWrite(idx).reviveReset();
                        if (state.enemiesAlive != 4) {
                            var j = 0;
                            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                                if (enemy.getHealth() > 0) {
                                    j += 1;
                                }
                            }
                            if (j < 4) {
                                System.out.println("!!!!! ENEMIES_ALIVE IS WRONG " + state.enemiesAlive + ": " + j + ": " + state);
                            }
                        }
                    }
                }
            } else if (move == STAB) {
                state.enemyDoDamageToPlayer(this, 6, 3);
            }
        }

        /*
            the following logic is the same as the slay the spire code, however it calls the rng whether it's needed or not
            and there will be ChanceNode in the tree with, so reworked to not have rng in ChanceNode
         */
        private void nextMove2(GameState state, RandomGen random, int r) {
            int enemiesAlive = startOfTurnEnemiesAlive >= 0 ? startOfTurnEnemiesAlive : state.enemiesAlive;
            if (enemiesAlive == 1) { // 0 gremlin
                if (r < 75) {
                    if (move != RALLY) {
                        move = RALLY;
                    } else {
                        move = STAB;
                    }
                } else if (move != STAB) {
                    move = STAB;
                } else {
                    move = RALLY;
                }
            } else if (enemiesAlive == 2) { // 1 gremlin
                if (r < 50) {
                    if (move != RALLY) {
                        move = RALLY;
                    } else {
                        nextMove2(state, random, 50 + random.nextInt(50, RandomGenCtx.EnemyChooseMove));
                    }
                } else if (r < 80) {
                    if (move != ENCOURAGE) {
                        move = ENCOURAGE;
                    } else {
                        move = STAB;
                    }
                } else if (move != STAB) {
                    move = STAB;
                } else {
                    nextMove2(state, random, random.nextInt(80, RandomGenCtx.EnemyChooseMove));
                }
            } else {
                if (r < 66) {
                    if (move != ENCOURAGE) {
                        move = ENCOURAGE;
                    } else {
                        move = STAB;
                    }
                } else if (move != STAB) {
                    move = STAB;
                } else {
                    move = ENCOURAGE;
                }
            }
        }

        public void nextMove2(GameState state, RandomGen random) {
            state.setIsStochastic();
            nextMove2(state, random, random.nextInt(100, RandomGenCtx.EnemyChooseMove));
        }

        private void nextMoveFor1Gremlin(GameState state, RandomGen random, int r) {
            if (r < 50) {
                if (move != RALLY) {
                    move = RALLY;
                } else {
                    nextMoveFor1Gremlin(state, random, 50 + random.nextInt(50, RandomGenCtx.EnemyChooseMove));
                }
            } else if (r < 80) {
                if (move != ENCOURAGE) {
                    move = ENCOURAGE;
                } else {
                    move = STAB;
                }
            } else if (move != STAB) {
                move = STAB;
            } else {
                nextMoveFor1Gremlin(state, random, random.nextInt(80, RandomGenCtx.EnemyChooseMove));
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int enemiesAlive = startOfTurnEnemiesAlive >= 0 ? startOfTurnEnemiesAlive : state.enemiesAlive;
            if (enemiesAlive == 1) { // 0 gremlin
                if (move == RALLY) {
                    move = STAB;
                } else if (move == STAB) {
                    move = RALLY;
                } else {
                    state.setIsStochastic();
                    int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                    if (r < 75) {
                        move = RALLY;
                    } else {
                        move = STAB;
                    }
                }
            } else if (enemiesAlive == 2) { // 1 gremlin
                state.setIsStochastic();
                int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                nextMoveFor1Gremlin(state, random, r);
            } else {
                if (move == ENCOURAGE) {
                    move = STAB;
                } else if (move == STAB) {
                    move = ENCOURAGE;
                } else {
                    state.setIsStochastic();
                    int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                    if (r < 66) {
                        move = ENCOURAGE;
                    } else {
                        move = STAB;
                    }
                }
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == ENCOURAGE) {
                return "Give all allies 5 strength and 10 block";
            } else if (move == RALLY) {
                return "Rally";
            } else if (move == STAB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6) + "x3";
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(10, RandomGenCtx.Other) + 1;
            if (training && b < 10) {
                health = (int) Math.round(((double) (health * b)) / 10);
            } else {
                health = 145 + random.nextInt(11, RandomGenCtx.Other);
            }
        }

        @Override public String getName() {
            return "Gremlin Leader";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var enemies = state.getEnemiesForWrite();
            for (int i = 0; i < enemies.size(); i++) {
                if (enemies.get(i).getName().contains("Gremlin")) {
                    enemies.get(i).properties.canGainBlock = true;
                    enemies.get(i).properties.canGainStrength = true;
                }
            }
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return s.subSequence(0, s.length() - 1) + ", startEnemies=" + startOfTurnEnemiesAlive + "}";
        }

        @Override public int getNNInputLen(GameProperties prop) {
            if (prop.hasRunicDome) {
                return 1;
            }
            return 0;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            if (prop.hasRunicDome) {
                return "1 input to keep track of gremlin count at the beginning of turn (because of Runic Dome)";
            }
            return null;
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            if (prop.hasRunicDome) {
                input[idx] = (startOfTurnEnemiesAlive - 1) / 3f;
                return 1;
            }
            return 0;
        }
    }

    public static class BookOfStabbing extends Enemy {
        static final int MULTI_STAB = 0;
        static final int SINGLE_STAB = 1;

        private int stabCount;

        public BookOfStabbing() {
            this(172);
        }

        public BookOfStabbing(int health) {
            super(health, 2, true);
            properties.isElite = true;
            properties.actNumber = 2;
            stabCount = 1;
        }

        public BookOfStabbing(BookOfStabbing other) {
            super(other);
            stabCount = other.stabCount;
        }

        @Override public Enemy copy() {
            return new BookOfStabbing(this);
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && stabCount == ((BookOfStabbing) o).stabCount;
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == MULTI_STAB) {
                state.enemyDoDamageToPlayer(this, 7, stabCount);
            } else if (move == SINGLE_STAB) {
                state.enemyDoDamageToPlayer(this, 24, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            state.setIsStochastic();
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
            int newMove;
            if (r < 15) {
                if (move == SINGLE_STAB) {
                    newMove = MULTI_STAB;
                } else {
                    newMove = SINGLE_STAB;
                }
            } else if (move == MULTI_STAB && lastMove == MULTI_STAB) {
                newMove = SINGLE_STAB;
            } else {
                newMove = MULTI_STAB;
            }
            stabCount++;
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == MULTI_STAB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "x" + stabCount;
            } else if (move == SINGLE_STAB) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 24);
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
                difficulty = random.nextInt(10, RandomGenCtx.Other) + 1;
                health = (int) Math.round(((double) (health * difficulty)) / 10);
            } else {
                health = 168 + random.nextInt(5, RandomGenCtx.Other);
            }
        }

        @Override public String getName() {
            return "Book of Stabbing";
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 inputs to keep track of current book of stabbing multi-hit counts";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = stabCount / 10f;
            return 1;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther.Wound());
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnDamageHandler("BookOfStabbing", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (source instanceof BookOfStabbing && damageDealt > 0) {
                        state.addCardToDiscard(state.properties.woundCardIdx);
                    }
                }
            });
        }
    }

    public static class Taskmaster extends Enemy {
        static final int SCOURING_WHIP = 0;

        public Taskmaster() {
            this(64);
        }

        public Taskmaster(int health) {
            super(health, 1, false);
            properties.isElite = true;
            properties.actNumber = 2;
            properties.canGainStrength = true;
        }

        public Taskmaster(Taskmaster other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Taskmaster(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SCOURING_WHIP) {
                state.enemyDoDamageToPlayer(this, 7, 1);
                gainStrength(1);
                state.addCardToDiscard(state.properties.woundCardIdx);
                state.addCardToDiscard(state.properties.woundCardIdx);
                state.addCardToDiscard(state.properties.woundCardIdx);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = SCOURING_WHIP;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SCOURING_WHIP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "+Gain Strength 1+Wound 3";
            }
            return "Unknown";
        }

        @Override public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(4, RandomGenCtx.Other) + 1;
            if (training && b < 4) {
                health = (int) Math.round(((double) (health * b)) / 4);
            } else {
                health = 57 + random.nextInt(8, RandomGenCtx.Other);
            }
        }

        @Override public String getName() {
            return "Taskmaster";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return List.of(new CardOther.Wound());
        }
    }

    public static class Byrd extends Enemy {
        static int CAW = 0;
        static int PECK = 1;
        static int SWOOP = 2;
        static int STUNNED = 3;
        static int HEADBUTT = 4;
        static int FLY = 5;

        int flying = 4;

        public int getFlying() {
            return flying;
        }

        public Byrd() {
            this(33);
        }

        public Byrd(int health) {
            super(health, 5, true);
            properties.canGainStrength = true;
        }

        public Byrd(Byrd other) {
            super(other);
            flying = other.flying;
        }

        @Override public Enemy copy() {
            return new Byrd(this);
        }

        @Override public int damage(double n, GameState state) {
            if (flying == 0) {
                return super.damage(n, state);
            }
            var dmg = super.damage(n / 2, state);
            if (dmg > 0 && flying > 0) {
                flying--;
                if (flying == 0) {
                    move = STUNNED;
                }
            }
            return dmg;
        }

        @Override public void startTurn(GameState state) {
            if (flying > 0) {
                flying = 4;
            }
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CAW) {
               gainStrength(1);
            } else if (move == PECK) {
                state.enemyDoDamageToPlayer(this, 1, 6);
            } else if (move == SWOOP) {
                state.enemyDoDamageToPlayer(this, 14, 1);
            } else if (move == FLY) {
                flying = 4;
            } else if (move == HEADBUTT) {
                state.enemyDoDamageToPlayer(this, 3, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move == STUNNED || move == HEADBUTT) {
                newMove = move + 1;
            } else if (move == -1) { // first turn
                state.setIsStochastic();
                if (random.nextInt(200, RandomGenCtx.EnemyChooseMove) < 125) {
                    newMove = PECK;
                } else {
                    newMove = CAW;
                }
            } else {
                state.setIsStochastic();
                int n = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                if (n < 50) {
                    if (move == PECK && lastMove == PECK) {
                        if (random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.4f) {
                            newMove = SWOOP;
                        } else {
                            newMove = CAW;
                        }
                    } else {
                        newMove = PECK;
                    }
                } else if (n < 70) {
                    if (move == SWOOP) {
                        if (random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.375f) {
                            newMove = CAW;
                        } else {
                            newMove = PECK;
                        }
                    } else {
                        newMove = SWOOP;
                    }
                } else if (move == CAW) {
                    if (random.nextFloat(RandomGenCtx.EnemyChooseMove) < 0.2857F) {
                        newMove = SWOOP;
                    } else {
                        newMove = PECK;
                    }
                } else {
                    newMove = CAW;
                }
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CAW) {
                return "Gain 1 Strength";
            } else if (move == PECK) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 1) + "x6";
            } else if (move == SWOOP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14);
            } else if (move == FLY) {
                return "Fly";
            } else if (move == HEADBUTT) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 3);
            } else if (move == STUNNED) {
                return "Stunned";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            int a = random.nextInt(8, RandomGenCtx.Other, null);
            health = 26 + a;
            if (training) {
                int b = random.nextInt(3, RandomGenCtx.Other, null) + 1;
                health = (int) (((double) b) / 3 * health);
            }
        }

        public String getName() {
            return "Byrd";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            return s.subSequence(0, s.length() - 1) + ", flying=" + flying + "}";
        }

        @Override public boolean equals(Object o) {
            return super.equals(o) && flying == ((Byrd) o).flying;
        }

        @Override public int getNNInputLen(GameProperties prop) {
            return 1;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of byrd flying";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = flying / 4f;
            return 1;
        }
    }

    public static class SphericGuardian extends Enemy {
        static int ACTIVATE = 0;
        static int ATTACK_DEBUFF = 1;
        static int SLAM = 2;
        static int HARDEN = 3;

        public SphericGuardian() {
            this(20);
        }

        public SphericGuardian(int health) {
            super(health, 4, false);
            properties.canFrail = true;
            properties.canGainBlock = true;
            properties.hasArtifact = true;
            artifact = 3;
            block = 40;
        }

        public SphericGuardian(SphericGuardian other) {
            super(other);
        }

        @Override public void startTurn(GameState state) {
            int prevBlock = block;
            super.startTurn(state);
            block = prevBlock;
        }

        @Override public Enemy copy() {
            return new SphericGuardian(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == ACTIVATE) {
                gainBlock(35);
            } else if (move == ATTACK_DEBUFF) {
                state.enemyDoDamageToPlayer(this, 11, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 5);
            } else if (move == SLAM) {
                state.enemyDoDamageToPlayer(this, 11, 2);
            } else if (move == HARDEN) {
                state.enemyDoDamageToPlayer(this, 11, 1);
                gainBlock(15);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move++;
            if (move == 4) {
                move = 2;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == ACTIVATE) {
                return "Gain 35 Block";
            } else if (move == ATTACK_DEBUFF) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11) + "+Frail 5";
            } else if (move == SLAM) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11) + "x2";
            } else if (move == HARDEN) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 11) + "+Block 15";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
                block = random.nextInt(6, RandomGenCtx.Other, null) * 8;
            }
        }

        public String getName() {
            return "Spheric Guardian";
        }
    }

    public static class ShelledParasite extends Enemy {
        static int DOUBLE_STRIKE = 0;
        static int SUCK = 1;
        static int FELL = 2;
        static int STUNNED = 3;

        public ShelledParasite() {
            this(75);
        }

        public ShelledParasite(int health) {
            super(health, 4, true);
            properties.canFrail = true;
            properties.canGainBlock = true;
            properties.canHeal = true;
            metallicize = 14;
        }

        public ShelledParasite(ShelledParasite other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new ShelledParasite(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == DOUBLE_STRIKE) {
                state.enemyDoDamageToPlayer(this, 7, 2);
            } else if (move == SUCK) {
                heal(state.enemyDoDamageToPlayer(this, 12, 1));
            } else if (move == FELL) {
                state.enemyDoDamageToPlayer(this, 21, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 2);
            } else if (move == STUNNED) {
                // do nothing
            }
        }

        @Override public int damage(double n, GameState state) {
            var dmg = super.damage(n, state);
            if (dmg > 0) {
                metallicize -= 1;
                if (metallicize == 0) {
                    move = STUNNED;
                }
            }
            return dmg;
        }

        private int nextMove(GameState state, RandomGen random, int num) {
            if (num < 20) {
                if (move != FELL) {
                    return move;
                } else {
                    return nextMove(state, random, random.nextInt(80, RandomGenCtx.EnemyChooseMove) + 20);
                }
            } else if (num < 60) {
                if (!(move == DOUBLE_STRIKE && lastMove == DOUBLE_STRIKE)) {
                    return DOUBLE_STRIKE;
                } else {
                    return SUCK;
                }
            } else if (!(move == SUCK && lastMove == SUCK)) {
                return SUCK;
            } else {
                return DOUBLE_STRIKE;
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move < 0) {
                newMove = FELL;
            } else {
                state.setIsStochastic();
                newMove = nextMove(state, random, random.nextInt(100, RandomGenCtx.EnemyChooseMove));
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == DOUBLE_STRIKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "x2";
            } else if (move == SUCK) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12);
            } else if (move == FELL) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 21) + "+Frail 2";
            } else if (move == STUNNED) {
                return "Stunned";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
                health = (int) ((random.nextInt(7, RandomGenCtx.Other, null) + 1) / 7.0 * 75);
            }
        }

        public String getName() {
            return "Shelled Parasite";
        }
    }

    public static class Snecko extends Enemy {
        static int PERPLEXING_GLARE = 0;
        static int TAIL_WHIP = 1;
        static int BITE = 2;

        public Snecko() {
            this(125);
        }

        public Snecko(int health) {
            super(health, 3, true);
            properties.canWeaken = true;
            properties.canVulnerable = true;
        }

        public Snecko(Snecko other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Snecko(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == PERPLEXING_GLARE) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.SNECKO, 1);
            } else if (move == TAIL_WHIP) {
                state.enemyDoDamageToPlayer(this, 10, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2);
            } else if (move == BITE) {
                state.enemyDoDamageToPlayer(this, 18, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move < 0) {
                newMove = PERPLEXING_GLARE;
            } else {
                if (move == BITE && lastMove == BITE) {
                    newMove = TAIL_WHIP;
                } else {
                    int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                    state.setIsStochastic();
                    if (r < 40) {
                        newMove = TAIL_WHIP;
                    } else {
                        newMove = BITE;
                    }
                }
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == PERPLEXING_GLARE) {
                return "Confuse";
            } else if (move == TAIL_WHIP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10) + "+Vulnerable 2+Weak 2";
            } else if (move == BITE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 18);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
                int b = random.nextInt(6, RandomGenCtx.Other, null) + 1;
                health = (int) Math.round(((double) (health * b)) / 6);
            } else {
                health = 120 + random.nextInt(6, RandomGenCtx.Other, null);
            }
        }

        public String getName() {
            return "Snecko";
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) {
            return GameProperties.generateSneckoCards(cards);
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.setupSneckoIndexes();
            state.properties.registerSneckoDebuffCounter();
        }
    }

    public static class Pointy extends Enemy {
        static int ATTACK = 0;

        public Pointy() {
            this(34);
        }

        public Pointy(int health) {
            super(health, 1, false);
        }

        public Pointy(Pointy other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Pointy(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == ATTACK) {
                state.enemyDoDamageToPlayer(this, 6, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move = ATTACK;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == ATTACK) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6) + "x2";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            if (training) {
                health = (int) ((random.nextInt(3, RandomGenCtx.Other, null) + 1) / 3.0 * 34);
            }
        }

        public String getName() {
            return "Pointy";
        }
    }

    public static class Romeo extends Enemy {
        static int MOCK = 0;
        static int AGONIZING_SLASH = 1;
        static int CROSS_SLASH_1 = 2;
        static int CROSS_SLASH_2 = 3;

        public Romeo() {
            this(41);
        }

        public Romeo(int health) {
            super(health, 4, false);
            properties.canWeaken = true;
        }

        public Romeo(Romeo other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Romeo(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == AGONIZING_SLASH) {
                state.enemyDoDamageToPlayer(this, 12, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 3);
            } else if (move == CROSS_SLASH_1 || move == CROSS_SLASH_2) {
                state.enemyDoDamageToPlayer(this, 17, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move++;
            if (move > CROSS_SLASH_2) {
                move = AGONIZING_SLASH;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == MOCK) {
                return "Mock";
            } if (move == AGONIZING_SLASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12) + "+Weak 3";
            } else if (move == CROSS_SLASH_1 || move == CROSS_SLASH_2) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 17);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(4, RandomGenCtx.Other, null) + 1;
            if (training && b < 4) {
                health = (int) Math.round(((double) (health * b)) / 4);
            } else {
                health = 37 + random.nextInt(5, RandomGenCtx.Other, null);
            }
        }

        public String getName() {
            return "Romeo";
        }
    }

    public static class Bear extends Enemy {
        static int BEAR_HUG = 0;
        static int LUNGE = 1;
        static int MAUL = 2;

        public Bear() {
            this(44);
        }

        public Bear(int health) {
            super(health, 3, false);
            properties.changePlayerDexterity = true;
            properties.canGainBlock = true;
        }

        public Bear(Bear other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Bear(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == BEAR_HUG) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_DEXTERITY, 4);
            } else if (move == LUNGE) {
                state.enemyDoDamageToPlayer(this, 10, 1);
                gainBlock(9);
            } else if (move == MAUL) {
                state.enemyDoDamageToPlayer(this, 20, 1);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            move++;
            if (move > MAUL) {
                move = LUNGE;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == BEAR_HUG) {
                return "-4 Dexterity";
            } else if (move == LUNGE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 10) + "+Block 9";
            } else if (move == MAUL) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 20);
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(4, RandomGenCtx.Other, null) + 1;
            if (training && b < 4) {
                health = (int) Math.round(((double) (health * b)) / 4);
            } else {
                health = 40 + random.nextInt(5, RandomGenCtx.Other, null);
            }
        }

        public String getName() {
            return "Bear";
        }
    }

    public static class Centurion extends Enemy {
        static int SLASH = 0;
        static int FURY = 1;
        static int DEFEND = 2;

        public Centurion() {
            this(83);
        }

        public Centurion(int health) {
            super(health, 3, false);
            properties.canGainBlock = true;
            properties.canGainStrength = true;
            properties.canHeal = true;
        }

        public Centurion(Centurion other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Centurion(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == SLASH) {
                state.enemyDoDamageToPlayer(this, 14, 1);
            } else if (move == FURY) {
                state.enemyDoDamageToPlayer(this, 7, 3);
            } else if (move == DEFEND) {
                for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    if (enemy != self) {
                        enemy.gainBlock(20);
                    }
                }
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
            state.setIsStochastic();
            if (r < 65) {
                boolean mysticAlive = false;
                for (var enemy : state.getEnemiesForRead()) {
                    if (enemy.isAlive() && enemy instanceof EnemyCity.Mystic) {
                        mysticAlive = true;
                    }
                }
                lastMove = move;
                move = mysticAlive ? DEFEND : FURY;
            } else {
                lastMove = move;
                move = SLASH;
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == SLASH) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 14);
            } else if (move == FURY) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 7) + "x3";
            } else if (move == DEFEND) {
                return "Ally Gain 20 Block";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(4, RandomGenCtx.Other, null) + 1;
            if (training && b < 4) {
                health = (int) Math.round(((double) (health * b)) / 4);
            } else {
                health = 78 + random.nextInt(6, RandomGenCtx.Other, null);
            }
        }

        public String getName() {
            return "Centurion";
        }
    }

    public static class Mystic extends Enemy {
        static int HEAL = 0;
        static int BUFF = 1;
        static int ATTACK_DEBUFF = 2;

        public Mystic() {
            this(58);
        }

        public Mystic(int health) {
            super(health, 3, true);
            properties.canGainStrength = true;
            properties.canFrail = true;
            properties.canHeal = true;
        }

        public Mystic(Mystic other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Mystic(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == HEAL) {
                for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemy.heal(20);
                }
            } else if (move == BUFF) {
                for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemy.gainStrength(4);
                }
            } else if (move == ATTACK_DEBUFF) {
                state.enemyDoDamageToPlayer(this, 9, 1);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int maxDiff = 0;
            for (var enemy : state.getEnemiesForRead()) {
                maxDiff = Math.max(enemy.properties.origHealth - enemy.getHealth(), maxDiff);
            }
            int newMove;
            if (maxDiff > 20 && move != HEAL && lastMove != HEAL) {
                newMove = HEAL;
            } else {
                int r = random.nextInt(100, RandomGenCtx.EnemyChooseMove);
                state.setIsStochastic();
                if (r >= 40 && move != ATTACK_DEBUFF) {
                    newMove = ATTACK_DEBUFF;
                } else if (move != BUFF || lastMove != BUFF) {
                    newMove = BUFF;
                } else {
                    newMove = ATTACK_DEBUFF;
                }
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == HEAL) {
                return "Ally Heal 20";
            } else if (move == BUFF) {
                return "Ally Gain 4 Str";
            } else if (move == ATTACK_DEBUFF) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 9) + "+Frail 2";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(2, RandomGenCtx.Other, null) + 1;
            if (training && b < 2) {
                health = (int) Math.round(((double) (health * b)) / 4);
            } else {
                health = 50 + random.nextInt(9, RandomGenCtx.Other, null);
            }
        }

        public String getName() {
            return "Mystic";
        }
    }

    public static class Chosen extends Enemy {
        static int HEX = 0;
        static int POKE = 1;
        static int ZAP = 2;
        static int DEBILITATE = 3;
        static int DRAIN = 4;

        public Chosen() {
            this(103);
        }

        public Chosen(int health) {
            super(health, 5, false);
            properties.canGainStrength = true;
            properties.canWeaken = true;
            properties.canVulnerable = true;
        }

        public Chosen(Chosen other) {
            super(other);
        }

        @Override public Enemy copy() {
            return new Chosen(this);
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == HEX) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.HEX, 1);
            } else if (move == POKE) {
                state.enemyDoDamageToPlayer(this, 6, 2);
            } else if (move == ZAP) {
                state.enemyDoDamageToPlayer(this, 21, 1);
            } else if (move == DEBILITATE) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.VULNERABLE, 2);
            } else if (move == DRAIN) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 3);
                gainStrength(3);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            if (move == -1) {
                move = HEX;
            } else if (move == HEX || move == POKE || move == ZAP) {
                state.setIsStochastic();
                int r = random.nextInt(2, RandomGenCtx.EnemyChooseMove);
                if (r == 0) {
                    move = DRAIN;
                } else {
                    move = DEBILITATE;
                }
            } else if (move == DEBILITATE || move == DRAIN) {
                state.setIsStochastic();
                int r = random.nextInt(10, RandomGenCtx.EnemyChooseMove);
                if (r < 4) {
                    move = ZAP;
                } else {
                    move = POKE;
                }
            }
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == HEX) {
                return "Hex";
            } else if (move == POKE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 6) + "x2";
            } else if (move == ZAP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 21);
            } else if (move == DEBILITATE) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 12) + "+Vulnerable 2";
            } else if (move == DRAIN) {
                return "Weak 3+Gain 3 Strength";
            }
            return "Unknown";
        }

        public List<Card> getPossibleGeneratedCards(GameProperties prop, List<Card> cards) { return List.of(new CardOther.Dazed()); }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(6, RandomGenCtx.Other, null) + 1;
            if (training && b < 6) {
                health = (int) Math.round(((double) (health * b)) / 6);
            } else {
                health = 98 + random.nextInt(6, RandomGenCtx.Other, null);
            }
        }

        public String getName() {
            return "Chosen";
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnCardPlayedHandler("Chosen", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, boolean cloned, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType != Card.ATTACK && state.getPlayeForRead().isHexed()) {
                        state.addCardToDeck(state.properties.dazedCardIdx);
                    }
                }
            });
            state.properties.addNNInputHandler("Hex", new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getPlayeForRead().isHexed() ? 0.5f : -0.5f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }
    }

    public static class SnakePlant extends Enemy {
        static int CHOMP = 0;
        static int ENFEEBLING_SPORES = 1;

        private int extraBlockPerAttack = 0;

        public SnakePlant() {
            this(82);
        }

        public SnakePlant(int health) {
            super(health, 2, true);
            properties.canWeaken = true;
            properties.canFrail = true;
            properties.canGainBlock = true;
        }

        public SnakePlant(SnakePlant other) {
            super(other);
            extraBlockPerAttack = other.extraBlockPerAttack;
        }

        @Override public Enemy copy() {
            return new SnakePlant(this);
        }

        @Override public int damage(double n, GameState state) {
            var dmg = super.damage(n, state);
            if (dmg > 0) {
                var idx = state.getEnemiesForRead().find(this);
                var extraBlockPerAttack = this.extraBlockPerAttack++;
                state.addGameActionToStartOfDeque(state1 -> state1.getEnemiesForWrite().getForWrite(idx).gainBlock(3 + extraBlockPerAttack));
            }
            return dmg;
        }

        @Override public void endTurn(int turnNum) {
            super.endTurn(turnNum);
            extraBlockPerAttack = 0;
        }

        @Override public void doMove(GameState state, EnemyReadOnly self) {
            if (move == CHOMP) {
                state.enemyDoDamageToPlayer(this, 8, 3);
            } else if (move == ENFEEBLING_SPORES) {
                state.getPlayerForWrite().applyDebuff(state, DebuffType.FRAIL, 2);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.WEAK, 2);
            }
        }

        @Override public void nextMove(GameState state, RandomGen random) {
            int newMove;
            if (move == ENFEEBLING_SPORES) {
                newMove = CHOMP;
            } else if (lastMove == ENFEEBLING_SPORES) {
                newMove = CHOMP;
            } else if (move == CHOMP && lastMove == CHOMP) {
                newMove = ENFEEBLING_SPORES;
            } else {
                newMove = random.nextInt(100, RandomGenCtx.EnemyChooseMove) < 65 ? CHOMP : ENFEEBLING_SPORES;
                state.setIsStochastic();
            }
            lastMove = move;
            move = newMove;
        }

        @Override public String getMoveString(GameState state, int move) {
            if (move == CHOMP) {
                return "Attack " + state.enemyCalcDamageToPlayer(this, 8) + "x3";
            } else if (move == ENFEEBLING_SPORES) {
                return "Frail 2+Weak 2";
            }
            return "Unknown";
        }

        public void randomize(RandomGen random, boolean training, int difficulty) {
            int b = random.nextInt(4, RandomGenCtx.Other, null) + 1;
            if (training && b < 4) {
                health = (int) Math.round(((double) (health * b)) / 4);
            } else {
                health = 78 + random.nextInt(5, RandomGenCtx.Other, null);
            }
        }

        public String getName() {
            return "Snake Plant";
        }

        @Override public String toString(GameState state) {
            String s = super.toString(state);
            if (extraBlockPerAttack > 0) {
                return s.subSequence(0, s.length() - 1) + ", malleable=" + (3 + extraBlockPerAttack) + "}";
            }
            return s;
        }

        @Override public String getNNInputDesc(GameProperties prop) {
            return "1 input to keep track of Snake Plant Malleable buff";
        }

        @Override public int writeNNInput(GameProperties prop, float[] input, int idx) {
            input[idx] = extraBlockPerAttack / 10.0f;
            return 1;
        }

        public int getExtraBlockPerAttack() {
            return extraBlockPerAttack;
        }
    }
}
