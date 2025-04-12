package com.alphaStS;

import com.alphaStS.card.Card;
import com.alphaStS.card.CardCount;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyEncounter;
import com.alphaStS.enemy.EnemyEnding;
import com.alphaStS.enemy.EnemyReadOnly;
import com.alphaStS.player.Player;
import com.alphaStS.utils.Tuple3;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface GameStateRandomization {
    int randomize(GameState state);
    void randomize(GameState state, int r);
    Map<Integer, Info> listRandomizations();
    List<Card> getPossibleGeneratedCards();

    default GameStateRandomization doAfter(GameStateRandomization prev) {
        if (prev == null) {
            return this;
        }
        return new GameStateRandomization.Cartesian(prev, this);
    }

    default GameStateRandomization ignore(GameStateRandomization prev) {
        return this;
    }

    default GameStateRandomization join(GameStateRandomization b) {
        return new GameStateRandomization.Join(this, b);
    }

    default GameStateRandomization union(double aChance, GameStateRandomization b) {
        return new GameStateRandomization.Union(this, aChance, b);
    }

    default GameStateRandomization fixR(int... r) {
        return new GameStateRandomization.FixedRandomization(this, r);
    }

    default GameStateRandomization fixR(int[] r, int k) {
        return new GameStateRandomization.FixedRandomization(this, r);
    }

    default GameStateRandomization followByIf(int r, GameStateRandomization b) {
        return new GameStateRandomization.FollowByIf(this, r, b);
    }

    default GameStateRandomization collapse(String desc) {
        return new GameStateRandomization.CollapsedRandomization(this, desc);
    }

    default GameStateRandomization setDescriptions(String... desc) {
        return new GameStateRandomization.OverrideDescRandomization(this, desc);
    }

    record Info(double chance, String desc) {}

    class Cartesian implements GameStateRandomization {
        private final GameStateRandomization a;
        private final GameStateRandomization b;
        private final Map<Integer, Info> infoMap;

        Cartesian(GameStateRandomization a, GameStateRandomization b) {
            this.a = a;
            this.b = b;
            Map<Integer, Info> aMap = a.listRandomizations();
            Map<Integer, Info> bMap = b.listRandomizations();
            infoMap = new HashMap<>();
            for (var aEntry : aMap.entrySet()) {
                for (var bEntry : bMap.entrySet()) {
                    var r = aEntry.getKey() * bMap.size() + bEntry.getKey();
                    var chance = aEntry.getValue().chance * bEntry.getValue().chance;
                    var desc = aEntry.getValue().desc + " + " + bEntry.getValue().desc;
                    infoMap.put(r, new Info(chance, desc));
                }
            }
        }

        @Override public int randomize(GameState state) {
            var ar = a.randomize(state);
            var br = b.randomize(state);
            return ar * b.listRandomizations().size() + br;
        }

        @Override public void randomize(GameState state, int r) {
            var bLen = b.listRandomizations().size();
            a.randomize(state, r / bLen);
            b.randomize(state, r % bLen);
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            List<Card> cards = new ArrayList<>();
            cards.addAll(a.getPossibleGeneratedCards());
            cards.addAll(b.getPossibleGeneratedCards());
            return cards;
        }
    }

    class Join implements GameStateRandomization {
        private final GameStateRandomization a;
        private final GameStateRandomization b;
        private final Map<Integer, Info> infoMap;

        Join(GameStateRandomization a, GameStateRandomization b) {
            this.a = a;
            this.b = b;
            Map<Integer, Info> aMap = a.listRandomizations();
            Map<Integer, Info> bMap = b.listRandomizations();
            infoMap = new HashMap<>();
            for (var aEntry : aMap.entrySet()) {
                var desc = bMap.get(aEntry.getKey()).desc;
                infoMap.put(aEntry.getKey(), new Info(aEntry.getValue().chance, aEntry.getValue().desc + ", " + desc));
            }
        }

        @Override public int randomize(GameState state) {
            var r = a.randomize(state);
            b.randomize(state, r);
            return r;
        }


        @Override public void randomize(GameState state, int r) {
            a.randomize(state, r);
            b.randomize(state, r);
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            List<Card> cards = new ArrayList<>();
            cards.addAll(a.getPossibleGeneratedCards());
            cards.addAll(b.getPossibleGeneratedCards());
            return cards;
        }
    }

    class Union implements GameStateRandomization {
        private final GameStateRandomization a;
        private double aChance;
        private final GameStateRandomization b;
        private final Map<Integer, Info> infoMap;

        Union(GameStateRandomization a, double aChance, GameStateRandomization b) {
            this.a = a;
            this.b = b;
            Map<Integer, Info> aMap = a.listRandomizations();
            Map<Integer, Info> bMap = b.listRandomizations();
            this.aChance = aChance;
            infoMap = new HashMap<>();
            for (var aEntry : aMap.entrySet()) {
                infoMap.put(aEntry.getKey(), new Info(aEntry.getValue().chance * aChance, aEntry.getValue().desc));
            }
            for (var bEntry : bMap.entrySet()) {
                infoMap.put(bEntry.getKey() + aMap.size(), new Info(bEntry.getValue().chance * (1 - aChance), bEntry.getValue().desc));
            }
        }

        @Override public int randomize(GameState state) {
            var r = state.getSearchRandomGen().nextFloat(RandomGenCtx.BeginningOfGameRandomization);
            if (r < aChance) {
                return a.randomize(state);
            } else {
                return b.randomize(state) + a.listRandomizations().size();
            }
        }


        @Override public void randomize(GameState state, int r) {
            if (r < a.listRandomizations().size()) {
                a.randomize(state, r);
            } else {
                b.randomize(state, r - a.listRandomizations().size());
            }
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            List<Card> cards = new ArrayList<>();
            cards.addAll(a.getPossibleGeneratedCards());
            cards.addAll(b.getPossibleGeneratedCards());
            return cards;
        }
    }

    class FollowByIf implements GameStateRandomization {
        private final GameStateRandomization a;
        private final GameStateRandomization b;
        int a_r;
        private final Map<Integer, Info> infoMap;
        private final Map<List<Integer>, Integer> abMap;
        private final Map<Integer, List<Integer>> rMapToAB;

        FollowByIf(GameStateRandomization a, int a_r, GameStateRandomization b) {
            this.a = a;
            this.b = b;
            this.a_r = a_r;
            Map<Integer, Info> aMap = a.listRandomizations();
            Map<Integer, Info> bMap = b.listRandomizations();

            infoMap = new HashMap<>();
            abMap = new HashMap<>();
            rMapToAB = new HashMap<>();
            var aKeys = aMap.keySet().stream().sorted().toList();
            int r = 0;
            for (int i = 0; i < aKeys.size(); i++) {
                if (aKeys.get(i) == a_r) {
                    var bKeys = bMap.keySet().stream().sorted().toList();
                    var aEntry = aMap.get(aKeys.get(i));
                    for (int j = 0; j < bKeys.size(); j++) {
                        var l = List.of(aKeys.get(i), bKeys.get(j));
                        abMap.put(l, r);
                        rMapToAB.put(r, l);
                        var bEntry = bMap.get(bKeys.get(j));
                        var chance = aEntry.chance() * bEntry.chance();
                        var desc = aEntry.desc() + " + " + bEntry.desc();
                        infoMap.put(r, new Info(chance, desc));
                        r++;
                    }
                } else {
                    var l = List.of(aKeys.get(i));
                    abMap.put(l, r);
                    rMapToAB.put(r, l);
                    infoMap.put(r, aMap.get(aKeys.get(i)));
                    r++;
                }
            }
        }

        @Override public int randomize(GameState state) {
            var r = a.randomize(state);
            if (r == a_r) {
                return abMap.get(List.of(r, b.randomize(state)));
            }
            return abMap.get(List.of(r));
        }


        @Override public void randomize(GameState state, int r) {
            var l = rMapToAB.get(r);
            if (l.get(0) == a_r) {
                a.randomize(state, l.get(0));
                b.randomize(state, l.get(1));
            } else {
                a.randomize(state, l.get(0));
            }
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            List<Card> cards = new ArrayList<>();
            cards.addAll(a.getPossibleGeneratedCards());
            cards.addAll(b.getPossibleGeneratedCards());
            return cards;
        }
    }

    class FixedRandomization implements GameStateRandomization {
        private final GameStateRandomization a;
        private final int[] rs;
        private final Map<Integer, Info> infoMap;

        FixedRandomization(GameStateRandomization a, int... rs) {
            this.a = a;
            this.rs = rs;
            Map<Integer, Info> aMap = a.listRandomizations();
            infoMap = new HashMap<>();
            for (int i = 0; i < rs.length; i++) {
                var info = aMap.get(rs[i]);
                infoMap.put(i, new Info(1.0 / rs.length, info.desc));
            }
        }

        @Override public int randomize(GameState state) {
            var i = state.getSearchRandomGen().nextInt(rs.length, RandomGenCtx.BeginningOfGameRandomization, this);
            a.randomize(state, rs[i]);
            return i;
        }

        @Override public void randomize(GameState state, int r) {
            a.randomize(state, rs[r]);
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            return a.getPossibleGeneratedCards(); // better to return all the underlying cards to reuse neural network
        }
    }

    class CollapsedRandomization implements GameStateRandomization {
        private final GameStateRandomization a;
        private final Map<Integer, Info> infoMap;

        CollapsedRandomization(GameStateRandomization a, String desc) {
            this.a = a;
            infoMap = new HashMap<>();
            infoMap.put(0, new Info(1.0, desc));
        }

        @Override public int randomize(GameState state) {
            a.randomize(state);
            return 0;
        }

        @Override public void randomize(GameState state, int r) {
            a.randomize(state);
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }


        @Override public List<Card> getPossibleGeneratedCards() {
            return a.getPossibleGeneratedCards();
        }
    }

    class OverrideDescRandomization implements GameStateRandomization {
        private final GameStateRandomization a;
        private final Map<Integer, Info> infoMap;

        OverrideDescRandomization(GameStateRandomization a, String... desc) {
            this.a = a;
            infoMap = new HashMap<>(a.listRandomizations());
            for (int i = 0; i < desc.length; i++) {
                infoMap.put(i, new Info(infoMap.get(i).chance, desc[i]));
            }
        }

        @Override public int randomize(GameState state) {
            return a.randomize(state);
        }

        @Override public void randomize(GameState state, int r) {
            a.randomize(state, r);
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            return a.getPossibleGeneratedCards(); // better to return all the underlying cards to reuse neural network
        }
    }

    class EnemyHealthRandomization implements GameStateRandomization {
        private Map<Integer, Tuple3<Integer, Integer, Integer>> randomizationScenarioToDifficulty;
        private final boolean curriculumTrainingIfNoDifficulty;

        public EnemyHealthRandomization(boolean curriculumTrainingIfNoDifficulty, Map<Integer, Tuple3<Integer, Integer, Integer>> randomizationScenarioToDifficulty) {
            this.curriculumTrainingIfNoDifficulty = curriculumTrainingIfNoDifficulty;
            this.randomizationScenarioToDifficulty = randomizationScenarioToDifficulty;
        }

        @Override public int randomize(GameState state) {
            if (state.properties.isHeartGauntlet && state.enemiesAlive == 1) {
                return 0;
            }
            int minDifficulty = -1;
            int maxDifficulty = -1;
            int totalDifficulty = 0;
            int enemiesAlive = 0;
            boolean canAutomateTraining = true;
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                if (enemy.getMaxRandomizeDifficulty() <= 0) {
                    canAutomateTraining = false;
                    break;
                }
            }
            if (state.properties.isTraining && canAutomateTraining) {
                if (state.properties.isHeartGauntlet) {
                    for (EnemyReadOnly enemy : state.getEnemiesForWrite()) {
                        totalDifficulty += ((Enemy) enemy).getMaxRandomizeDifficulty();
                    }
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        enemiesAlive++;
                    }
                } else {
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        enemiesAlive++;
                        totalDifficulty += ((Enemy) enemy).getMaxRandomizeDifficulty();
                    }
                }
                if (enemiesAlive != state.enemiesAlive) {
                    throw new RuntimeException();
                }
                if (randomizationScenarioToDifficulty != null) {
                    var difficulty = randomizationScenarioToDifficulty.get(state.getScenarioIdxChosen());
                    if (difficulty == null) {
                        minDifficulty = enemiesAlive;
                        maxDifficulty = (enemiesAlive + totalDifficulty + 1) / 2;
                        randomizationScenarioToDifficulty.put(state.getScenarioIdxChosen(), new Tuple3<>(minDifficulty, maxDifficulty, totalDifficulty));
                    } else {
                        minDifficulty = difficulty.v1();
                        maxDifficulty = difficulty.v2();
                    }
                }
            }
            if (!state.properties.isTraining || (!curriculumTrainingIfNoDifficulty && (minDifficulty <= 0 || minDifficulty == totalDifficulty))) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemy.randomize(state.getSearchRandomGen(), false, -1);
                    if (enemy.hasBurningHealthBuff()) {
                        enemy.setHealth((int) (enemy.getHealth() * 1.25));
                    }
                    enemy.properties = enemy.properties.clone();
                    enemy.properties.origHealth = enemy.getHealth();
                }
            } else if (minDifficulty == maxDifficulty) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    if (enemy.getMaxRandomizeDifficulty() > 0) {
                        state.setIsStochastic();
                        var r = state.getSearchRandomGen().nextInt(enemy.getMaxRandomizeDifficulty(), RandomGenCtx.Other) + 1;
                        enemy.randomize(state.getSearchRandomGen(), true, r);
                    } else {
                        enemy.randomize(state.getSearchRandomGen(), true, -1);
                    }
                    if (enemy.hasBurningHealthBuff()) {
                        enemy.setHealth((int) (enemy.getHealth() * 1.25));
                    }
                    enemy.properties = enemy.properties.clone();
                    enemy.properties.origHealth = enemy.getHealth();
                }
            } else {
                if (minDifficulty == 0) {
                    minDifficulty = 1;
                }
                if (maxDifficulty == 0) {
                    maxDifficulty = 1;
                }
                var difficultyChosen = minDifficulty + state.getSearchRandomGen().nextInt(maxDifficulty - minDifficulty + 1, RandomGenCtx.Other);
                if (state.properties.makingRealMove) {
                    state.properties.difficultyChosen = difficultyChosen;
                }
                if (state.properties.isHeartGauntlet) {
                    if (difficultyChosen <= 40) { // after beating shield and spear fight, heart fight
                        state.killEnemy(0, false);
                        state.killEnemy(1, false);
                        state.reviveEnemy(2, false, -1);
                        ((EnemyEnding.CorruptHeart) state.getEnemiesForWrite().getForWrite(2)).setInvincible(200);
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            enemy.randomize(state.getSearchRandomGen(), true, difficultyChosen);
                            if (enemy.hasBurningHealthBuff()) {
                                enemy.setHealth((int) (enemy.getHealth() * 1.25));
                            }
                            enemy.properties = enemy.properties.clone();
                            enemy.properties.origHealth = enemy.getHealth();
                        }
                        return 0;
                    }
                    difficultyChosen -= 40;
                    if (difficultyChosen <= 2) {
                        difficultyChosen = 2;
                    }
                }
                int[] maxDifficulties = new int[enemiesAlive];
                int[] difficulties = new int[enemiesAlive];
                int i = 0;
                for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    maxDifficulties[i++] = enemy.getMaxRandomizeDifficulty();
                }
                var allowed = new ArrayList<Integer>();
                for (int j = 0; j < enemiesAlive; j++) {
                    allowed.add(j);
                }
                for (int j = 0; j < difficultyChosen - enemiesAlive; j++) {
                    i = state.getSearchRandomGen().nextInt(allowed.size(), RandomGenCtx.Other); // currently equal probability
                    difficulties[allowed.get(i)]++;
                    if (difficulties[allowed.get(i)] == maxDifficulties[allowed.get(i)] - 1) {
                        allowed.remove(allowed.get(i));
                    }
                }
                i = 0;
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemy.randomize(state.getSearchRandomGen(), true, difficulties[i++] + 1);
                    if (enemy.hasBurningHealthBuff()) {
                        enemy.setHealth((int) (enemy.getHealth() * 1.25));
                    }
                    enemy.properties = enemy.properties.clone();
                    enemy.properties.origHealth = enemy.getHealth();
                }
            }
            return 0;
        }

        @Override public void randomize(GameState state, int r) {
            if (state.properties.isHeartGauntlet && state.enemiesAlive == 1) {
                return;
            }
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.setHealth(enemy.properties.maxHealth);
            }
            randomize(state);
        }

        @Override public Map<Integer, Info> listRandomizations() {
            Map<Integer, Info> map = new HashMap<>();
            map.put(0, new Info(1, "Randomize enemy starting state (e.g. health)" + (curriculumTrainingIfNoDifficulty ? " with curriculum training" : "")));
            return map;
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            return List.of();
        }
    }

    class PotionUtilityRandomization implements GameStateRandomization {
        private final int potionIdx;
        private final String potionName;
        private final int steps;
        private final short startingRatio;

        public PotionUtilityRandomization(Potion potion, int potionIdx, int steps) {
            this(potion, potionIdx, steps, (short) 100);
        }

        public PotionUtilityRandomization(Potion potion, int potionIdx, int steps, short startingRatio) {
            this.potionIdx = potionIdx;
            this.potionName = potion.toString();
            this.steps = steps;
            this.startingRatio = startingRatio;
        }

        // todo: think of a better distribution
        @Override public int randomize(GameState state) {
            var upto = steps * 10;
            var div = (upto - 5 * steps) / steps;
            var r = state.getSearchRandomGen().nextInt(upto, RandomGenCtx.BeginningOfGameRandomization, this);
            if (r < 5 * steps) {
                r = 0;
            } else {
                r = 1 + (r - 5 * steps) / div;
            }
            randomize(state, r);
            return r;
        }

        @Override public void randomize(GameState state, int r) {
            if (r == 0) {
                state.getPotionsStateForWrite()[potionIdx * 3] = 0;
                state.getPotionsStateForWrite()[potionIdx * 3 + 1] = startingRatio;
                state.getPotionsStateForWrite()[potionIdx * 3 + 2] = 0;
            } else {
                state.getPotionsStateForWrite()[potionIdx * 3] = 1;
                state.getPotionsStateForWrite()[potionIdx * 3 + 1] = (short) (startingRatio - 5 * (r - 1));
                state.getPotionsStateForWrite()[potionIdx * 3 + 2] = 1;
            }
        }

        @Override public Map<Integer, Info> listRandomizations() {
            Map<Integer, Info> map = new HashMap<>();
            map.put(0, new Info(0.4,  potionName + " cannot be used"));
            for (int cur_r = 0; cur_r < steps; cur_r++) {
                var u = startingRatio - 5 * cur_r;
                map.put(cur_r + 1, new Info(0.6 / steps, potionName + " " + u));
            }
            return map;
        }

        public int getSteps() {
            return steps;
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            return List.of();
        }
    }

    class PotionsUtilityRandomization implements GameStateRandomization {
        private final GameStateRandomization randomization;

        public PotionsUtilityRandomization(List<Potion> potions) {
            GameStateRandomization randomization = null;
            for (int i = 0; i < potions.size(); i++) {
                if (potions.get(i).isGenerated) {
                    continue;
                }
                if (potions.get(i) instanceof Potion.BloodPotion || potions.get(i) instanceof Potion.BlockPotion || potions.get(i) instanceof Potion.FairyInABottle) {
                    randomization = new PotionUtilityRandomization(potions.get(i), i, potions.get(i).getPenaltyRatioSteps(), potions.get(i).getBasePenaltyRatio()).fixR(1).doAfter(randomization);
                    continue;
                }
                randomization = new PotionUtilityRandomization(potions.get(i), i, potions.get(i).getPenaltyRatioSteps(), potions.get(i).getBasePenaltyRatio()).doAfter(randomization);
            }
            this.randomization = randomization;
        }

        // todo: think of a better distribution
        @Override public int randomize(GameState state) {
            for (int i = 0; i < state.properties.potions.size(); i++) {
                if (state.properties.potions.get(i).isGenerated) {
                    state.setPotionUnusable(i, state.properties.potions.get(i).basePenaltyRatio);
                }
            }
            return randomization.randomize(state);
        }

        @Override public void randomize(GameState state, int r) {
            for (int i = 0; i < state.properties.potions.size(); i++) {
                if (state.properties.potions.get(i).isGenerated) {
                    state.setPotionUnusable(i, state.properties.potions.get(i).basePenaltyRatio);
                }
            }
            randomization.randomize(state, r);
        }

        @Override public Map<Integer, Info> listRandomizations() {
            var map = new HashMap<>(randomization.listRandomizations());
            map.put(0, new Info(map.get(0).chance, "No potions can be used"));
            return map;
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            return List.of();
        }
    }

    class CardCountRandomization implements GameStateRandomization {
        private final List<List<CardCount>> scenarios;
        private final Map<Integer, Info> infoMap;
        private final int type;

        public final static int ADD_TO_DECK = 1;
        public final static int UPGRADE = 2;
        public final static int REMOVE_FROM_DECK = 3;

        public CardCountRandomization(List<List<CardCount>> scenarios, int type) {
            var map = new HashMap<Card, Integer>();
            for (List<CardCount> scenario : scenarios) {
                for (CardCount cardCount : scenario) {
                    map.put(cardCount.card(), 0);
                }
            }
            this.scenarios = new ArrayList<>();
            infoMap = new HashMap<>();
            for (int i = 0; i < scenarios.size(); i++) {
                var m = new HashMap<>(map);
                for (CardCount cardCount : scenarios.get(i)) {
                    m.put(cardCount.card(), cardCount.count());
                }
                this.scenarios.add(m.entrySet().stream().map((e) -> new CardCount(e.getKey(), e.getValue())).toList());
                var desc = String.join(", ", scenarios.get(i).stream().map((e) -> e.count() + " " + e.card().cardName).toList());
                if (type == UPGRADE) {
                    desc = String.join(", ", scenarios.get(i).stream().map((e) -> "Upgrade " + e.count() + " " + e.card().cardName).toList());
                } else if (type == REMOVE_FROM_DECK) {
                    desc = String.join(", ", scenarios.get(i).stream().map((e) -> "-" + e.count() + " " + e.card().cardName).toList());
                }
                if (desc.length() == 0) {
                    desc = "No Change";
                }
                infoMap.put(i, new Info(1.0 / scenarios.size(), desc));
            }
            this.type = type;
        }

        @Override public int randomize(GameState state) {
            int r = state.getSearchRandomGen().nextInt(scenarios.size(), RandomGenCtx.BeginningOfGameRandomization, this);
            randomize(state, r);
            return r;
        }

        @Override public void randomize(GameState state, int r) {
            var s = scenarios.get(r);
            for (CardCount cardCount : s) {
                if (type == ADD_TO_DECK) {
                    for (int i = 0; i < cardCount.count(); i++) {
                        state.addCardToDeck(state.properties.findCardIndex(cardCount.card()), false);
                    }
                } else if (type == UPGRADE) {
                    if (GameStateUtils.getCardCount(state.getDeckArrForRead(), state.getNumCardsInDeck(), state.properties.findCardIndex(cardCount.card())) < cardCount.count()) {
                        throw new RuntimeException("Not enough cards in deck");
                    }
                    for (int i = 0; i < cardCount.count(); i++) {
                        state.removeCardFromDeck(state.properties.findCardIndex(cardCount.card()), false);
                        state.addCardToDeck(state.properties.findCardIndex(cardCount.card().getUpgrade()), false);
                    }
                } else if (type == REMOVE_FROM_DECK) {
                    if (GameStateUtils.getCardCount(state.getDeckArrForRead(), state.getNumCardsInDeck(), state.properties.findCardIndex(cardCount.card())) < cardCount.count()) {
                        throw new RuntimeException("Not enough cards in deck");
                    }
                    for (int i = 0; i < cardCount.count(); i++) {
                        state.removeCardFromDeck(state.properties.findCardIndex(cardCount.card()), false);
                    }
                } else {
                    throw new IllegalStateException(); // todo: switch to enum
                }
            }
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            if (type == UPGRADE) {
                return scenarios.stream().flatMap((s) -> s.stream().map(CardCount::card)).map(Card::getUpgrade).distinct().toList();
            } else {
                return scenarios.stream().flatMap((s) -> s.stream().map(CardCount::card)).distinct().toList();
            }
        }
    }

    class PlayerHealthRandomization implements GameStateRandomization {
        private final Map<Integer, Info> infoMap;
        private final int[] possibleHealths;

        public PlayerHealthRandomization(int[] possibleHealths) {
            this.possibleHealths = possibleHealths;
            infoMap = new HashMap<>();
            for (int i = 0; i < possibleHealths.length; i++) {
                infoMap.put(i, new Info(1.0 / possibleHealths.length, "Health " + possibleHealths[i]));
            }
        }

        public PlayerHealthRandomization(int start, int end) {
            this.possibleHealths = new int[end - start + 1];
            for (int i = 0; i < possibleHealths.length; i++) {
                this.possibleHealths[i] = start + i;
            }
            infoMap = new HashMap<>();
            for (int i = 0; i < possibleHealths.length; i++) {
                infoMap.put(i, new Info(1.0 / possibleHealths.length, "Health " + possibleHealths[i]));
            }
        }

        @Override public int randomize(GameState state) {
            int r = state.getSearchRandomGen().nextInt(possibleHealths.length, RandomGenCtx.BeginningOfGameRandomization, this);
            randomize(state, r);
            return r;
        }

        @Override public void randomize(GameState state, int r) {
            state.getPlayerForWrite().setHealth(possibleHealths[r]);
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            return List.of();
        }
    }

    class EnemyEncounterRandomization implements GameStateRandomization {
        private final List<EnemyEncounter> scenarios;
        private final Map<Integer, Info> infoMap;

        public EnemyEncounterRandomization(List<Enemy> enemies, List<EnemyEncounter> enemyEncounters) {
            this.scenarios = new ArrayList<>();
            infoMap = new HashMap<>();
            for (int i = 0; i < enemyEncounters.size(); i++) {
                scenarios.add(enemyEncounters.get(i));
                var names = enemyEncounters.get(i).idxes.stream().map((t) -> {
                    if (t.v2() >= 0) {
                        return ((Enemy.MergedEnemy) enemies.get(t.v1())).getDescName();
                    }
                    return enemies.get(t.v1()).getName();
                }).toList();
                infoMap.put(i, new Info(1.0 / enemyEncounters.size(), String.join(", ", names)));
            }
        }

        @Override public int randomize(GameState state) {
            int r = state.getSearchRandomGen().nextInt(scenarios.size(), RandomGenCtx.BeginningOfGameRandomization, this);
            randomize(state, r);
            state.properties.enemiesEncounterChosen = r;
            return r;
        }

        @Override public void randomize(GameState state, int r) {
            var enemies = state.getEnemiesForWrite();
            for (int i = 0; i < enemies.size(); i++) {
                enemies.getForWrite(i).setHealth(0);
            }
            var encounter = scenarios.get(r);
            for (var t : encounter.idxes) {
                var enemy = state.getEnemiesForWrite().getForWrite(t.v1());
                if (t.v2() >= 0) {
                    var e = (Enemy.MergedEnemy) enemy;
                    e.setEnemy(t.v2());
                    enemy.setHealth(e.getEnemyProperty(t.v2()).maxHealth);
                } else {
                    enemy.setHealth(enemy.properties.maxHealth);
                }
            }
            state.currentEncounter = encounter.encounterEnum;
            state.enemiesAlive = encounter.idxes.size();
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            return List.of();
        }
    }

    class SimpleCustomRandomization implements GameStateRandomization {
        private final List<Consumer<GameState>> randomizations;
        private final Map<Integer, Info> infoMap;

        public SimpleCustomRandomization(List<Consumer<GameState>> randomizations) {
            this.randomizations = randomizations;
            infoMap = new HashMap<>();
            for (int i = 0; i < randomizations.size(); i++) {
                infoMap.put(i, new Info(1.0 / randomizations.size(), "Randomization " + i));
            }
        }

        @Override public int randomize(GameState state) {
            int r = state.getSearchRandomGen().nextInt(randomizations.size(), RandomGenCtx.BeginningOfGameRandomization, this);
            randomize(state, r);
            return r;
        }

        @Override public void randomize(GameState state, int r) {
            randomizations.get(r).accept(state);
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            return List.of();
        }
    }

    class InteractiveModeRandomization extends SimpleCustomRandomization {
        public InteractiveModeRandomization(List<String> ...commandList) {
            super(List.of((state) -> {
                new InteractiveMode(new PrintStream(OutputStream.nullOutputStream())).interactiveApplyHistory(state, commandList[state.preBattleScenariosChosenIdx]);
            }));
        }
    }

    class BurningEliteRandomization implements GameStateRandomization {
        @Override public int randomize(GameState state) {
            int r = state.getSearchRandomGen().nextInt(4, RandomGenCtx.BeginningOfGameRandomization, this);
            randomize(state, r);
            return r;
        }

        @Override public void randomize(GameState state, int r) {
            switch (r) {
                case 0 -> {
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        if (enemy.properties.hasBurningEliteBuff()) {
                            enemy.setBurningHealthBuff(true);
                            enemy.setHealth(enemy.properties.maxHealth);
                            enemy.gainStrength(-enemy.getStrength());
                            enemy.setMetallicize(0);
                            enemy.setRegeneration(0);
                        }
                    }
                }
                case 1 -> {
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        if (enemy.properties.hasBurningEliteBuff()) {
                            enemy.setBurningHealthBuff(false);
                            enemy.setHealth(enemy.properties.origMaxHealth);
                            enemy.gainStrength(enemy.properties.actNumber + 1);
                            enemy.setMetallicize(0);
                            enemy.setRegeneration(0);
                        }
                    }
                }
                case 2 -> {
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        if (enemy.properties.hasBurningEliteBuff()) {
                            enemy.setBurningHealthBuff(false);
                            enemy.setHealth(enemy.properties.origMaxHealth);
                            enemy.gainStrength(-enemy.getStrength());
                            enemy.setMetallicize(2 * enemy.properties.actNumber + 2);
                            enemy.setRegeneration(0);
                        }
                    }
                }
                case 3 -> {
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        if (enemy.properties.hasBurningEliteBuff()) {
                            enemy.setBurningHealthBuff(false);
                            enemy.setHealth(enemy.properties.origMaxHealth);
                            enemy.gainStrength(-enemy.getStrength());
                            enemy.setMetallicize(0);
                            enemy.setRegeneration(2 * enemy.properties.actNumber + 1);
                        }
                    }
                }
            }
        }

        @Override public Map<Integer, Info> listRandomizations() {
            var map = new HashMap<Integer, Info>();
            map.put(0, new Info(1.0 / 4, "+25% Health"));
            map.put(1, new Info(1.0 / 4, "+Strength"));
            map.put(2, new Info(1.0 / 4, "+Metallicize"));
            map.put(3, new Info(1.0 / 4, "+Regeneration"));
            return map;
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            return List.of();
        }
    }

    // todo
    class CampfireRandomization implements GameStateRandomization {
        private final List<Card> cards;
        private final Map<Card, Integer> startingCardCount;
        private final Map<Integer, Info> infoMap;
        private final int restHp;
        private final int noRestHp;

        public CampfireRandomization(Player player, List<CardCount> possibleCards, List<Card> cards) {
            if (cards == null) {
                this.cards = possibleCards.stream().map(CardCount::card).filter((card) -> card.getUpgrade() != null).toList();
            } else {
                this.cards = cards.stream().filter((card) -> card.getUpgrade() != null).toList();
            }
            startingCardCount = new HashMap<>();
            for (Card card : cards) {
                for (CardCount possibleCard : possibleCards) {
                    if (possibleCard.card().equals(card)) {
                        startingCardCount.put(card, possibleCard.count());
                    }
                }
            }
            noRestHp = player.getHealth();
            restHp = Math.min(player.getHealth() + (int) (0.3 * player.getMaxHealth()), player.getMaxHealth());
            infoMap = new HashMap<>();
            infoMap.put(0, new Info(1.0 / this.cards.size(), "Heal to " + restHp));
            for (int i = 0; i < this.cards.size(); i++) {
                infoMap.put(i + 1, new Info(1.0 / this.cards.size(), "Upgrade " + this.cards.get(i)));
            }
        }

        @Override public int randomize(GameState state) {
            int r = state.getSearchRandomGen().nextInt(cards.size(), RandomGenCtx.BeginningOfGameRandomization, this);
            randomize(state, r);
            return r;
        }

        @Override public void randomize(GameState state, int r) {
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }

        @Override public List<Card> getPossibleGeneratedCards() {
            return List.of();
        }
    }
}
