package com.alphaStS;

import com.alphaStS.enemy.Enemy;
import com.alphaStS.player.Player;
import com.alphaStS.utils.Tuple;

import java.util.*;
import java.util.function.Consumer;

public interface GameStateRandomization {
    int randomize(GameState state);
    void randomize(GameState state, int r);
    Map<Integer, Info> listRandomizations();

    default GameStateRandomization doAfter(GameStateRandomization prev) {
        if (prev == null) {
            return this;
        }
        return new GameStateRandomization.Cartesian(prev, this);
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
    }

    class EnemyRandomization implements GameStateRandomization {
        private final boolean curriculumTraining;
        private final int minDifficulty;
        private final int maxDifficulty;

        public EnemyRandomization(boolean curriculumTraining, int minDifficulty, int maxDifficulty) {
            this.curriculumTraining = curriculumTraining;
            this.minDifficulty = minDifficulty;
            this.maxDifficulty = maxDifficulty;
        }

        @Override public int randomize(GameState state) {
            if (!curriculumTraining || minDifficulty <= 0) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemy.randomize(state.getSearchRandomGen(), curriculumTraining, -1);
                    if (enemy.hasBurningHealthBuff()) {
                        enemy.setHealth((int) (enemy.getHealth() * 1.25));
                    }
                    enemy.property = enemy.property.clone();
                    enemy.property.origHealth = enemy.getHealth();
                }
            } else if (minDifficulty == maxDifficulty) {
                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemy.randomize(state.getSearchRandomGen(), false, -1);
                    if (enemy.hasBurningHealthBuff()) {
                        enemy.setHealth((int) (enemy.getHealth() * 1.25));
                    }
                    enemy.property = enemy.property.clone();
                    enemy.property.origHealth = enemy.getHealth();
                }
            } else {
                int enemiesAlive = 0;
                for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemiesAlive++;
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
                state.prop.difficulty = minDifficulty + state.getSearchRandomGen().nextInt(maxDifficulty - minDifficulty + 1, RandomGenCtx.Other);
                for (int j = 0; j < state.prop.difficulty - enemiesAlive; j++) {
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
                    enemy.property = enemy.property.clone();
                    enemy.property.origHealth = enemy.getHealth();
                }
            }
            return 0;
        }

        @Override public void randomize(GameState state, int r) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.setHealth(enemy.property.maxHealth);
            }
            randomize(state);
        }

        @Override public Map<Integer, Info> listRandomizations() {
            Map<Integer, Info> map = new HashMap<>();
            map.put(0, new Info(1, "Randomize enemy starting state (e.g. health)" + (curriculumTraining ? " with curriculum training" : "")));
            return map;
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
                state.potionsState[potionIdx * 3] = 0;
                state.potionsState[potionIdx * 3 + 1] = startingRatio;
                state.potionsState[potionIdx * 3 + 2] = 0;
            } else {
                state.potionsState[potionIdx * 3] = 1;
                state.potionsState[potionIdx * 3 + 1] = (short) (startingRatio - 5 * (r - 1));
                state.potionsState[potionIdx * 3 + 2] = 1;
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
    }

    class PotionsUtilityRandomization implements GameStateRandomization {
        private final GameStateRandomization randomization;

        public PotionsUtilityRandomization(List<Potion> potions) {
            GameStateRandomization randomization = null;
            for (int i = 0; i < potions.size(); i++) {
                if (potions.get(i) instanceof Potion.BloodPotion || potions.get(i) instanceof Potion.BlockPotion) {
                    randomization = new PotionUtilityRandomization(potions.get(i), i, potions.get(i).getPenaltyRatioSteps(), potions.get(i).getBasePenaltyRatio()).fixR(1).doAfter(randomization);
                    continue;
                }
                randomization = new PotionUtilityRandomization(potions.get(i), i, potions.get(i).getPenaltyRatioSteps(), potions.get(i).getBasePenaltyRatio()).doAfter(randomization);
            }
            this.randomization = randomization;
        }

        // todo: think of a better distribution
        @Override public int randomize(GameState state) {
            return randomization.randomize(state);
        }

        @Override public void randomize(GameState state, int r) {
            randomization.randomize(state, r);
        }

        @Override public Map<Integer, Info> listRandomizations() {
            var map = new HashMap<>(randomization.listRandomizations());
            map.put(0, new Info(map.get(0).chance, "No potions can be used"));
            return map;
        }
    }

    class CardCountRandomization implements GameStateRandomization {
        private final List<List<CardCount>> scenarios;
        private final Map<Integer, Info> infoMap;

        public CardCountRandomization(List<List<CardCount>> scenarios) {
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
                if (desc.length() == 0) {
                    desc = "No Change";
                }
                infoMap.put(i, new Info(1.0 / scenarios.size(), desc));
            }
        }

        @Override public int randomize(GameState state) {
            int r = state.getSearchRandomGen().nextInt(scenarios.size(), RandomGenCtx.BeginningOfGameRandomization, this);
            randomize(state, r);
            return r;
        }

        @Override public void randomize(GameState state, int r) {
            var s = scenarios.get(r);
            for (CardCount cardCount : s) {
                state.setCardCountInDeck(state.prop.findCardIndex(cardCount.card()), cardCount.count());
            }
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }
    }

    class EnemyEncounterRandomization implements GameStateRandomization {
        private final List<List<Tuple<Integer, Integer>>> scenarios;
        private final Map<Integer, Info> infoMap;

        public EnemyEncounterRandomization(List<Enemy> enemies, int[]... enemiesIdx) {
            this.scenarios = new ArrayList<>();
            infoMap = new HashMap<>();
            for (int i = 0; i < enemiesIdx.length; i++) {
                scenarios.add(Arrays.stream(enemiesIdx[i]).mapToObj((x) -> new Tuple<>(x, -1)).toList());
                var names = Arrays.stream(enemiesIdx[i]).mapToObj((idx) -> enemies.get(idx).getName()).toList();
                infoMap.put(i, new Info(1.0 / enemiesIdx.length, String.join(", ", names)));
            }
        }

        public EnemyEncounterRandomization(List<Enemy> enemies, List<List<Tuple<Integer, Integer>>> enemiesIdx) {
            this.scenarios = new ArrayList<>();
            infoMap = new HashMap<>();
            for (int i = 0; i < enemiesIdx.size(); i++) {
                scenarios.add(enemiesIdx.get(i));
                var names = enemiesIdx.get(i).stream().map((t) -> {
                    if (t.v2() >= 0) {
                        return ((Enemy.MergedEnemy) enemies.get(t.v1())).getDescName();
                    }
                    return enemies.get(t.v1()).getName();
                }).toList();
                infoMap.put(i, new Info(1.0 / enemiesIdx.size(), String.join(", ", names)));
            }
        }

        @Override public int randomize(GameState state) {
            int r = state.getSearchRandomGen().nextInt(scenarios.size(), RandomGenCtx.BeginningOfGameRandomization, this);
            randomize(state, r);
            return r;
        }

        @Override public void randomize(GameState state, int r) {
            var enemies = state.getEnemiesForWrite();
            for (int i = 0; i < enemies.size(); i++) {
                enemies.getForWrite(i).setHealth(0);
            }
            var enemiesIdx = scenarios.get(r);
            for (var t : enemiesIdx) {
                var enemy = state.getEnemiesForWrite().getForWrite(t.v1());
                if (t.v2() >= 0) {
                    var e = (Enemy.MergedEnemy) enemy;
                    e.setEnemy(t.v2());
                    enemy.setHealth(e.getEnemyProperty(t.v2()).maxHealth);
                } else {
                    enemy.setHealth(enemy.property.maxHealth);
                }
            }
            state.enemiesAlive = enemiesIdx.size();
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
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
                        if (enemy.property.hasBurningEliteBuff()) {
                            enemy.setBurningHealthBuff(true);
                            enemy.setHealth(enemy.property.maxHealth);
                            enemy.gainStrength(-enemy.getStrength());
                            enemy.setMetallicize(0);
                            enemy.setRegeneration(0);
                        }
                    }
                }
                case 1 -> {
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        if (enemy.property.hasBurningEliteBuff()) {
                            enemy.setBurningHealthBuff(false);
                            enemy.setHealth(enemy.property.origMaxHealth);
                            enemy.gainStrength(enemy.property.actNumber + 1);
                            enemy.setMetallicize(0);
                            enemy.setRegeneration(0);
                        }
                    }
                }
                case 2 -> {
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        if (enemy.property.hasBurningEliteBuff()) {
                            enemy.setBurningHealthBuff(false);
                            enemy.setHealth(enemy.property.origMaxHealth);
                            enemy.gainStrength(-enemy.getStrength());
                            enemy.setMetallicize(2 * enemy.property.actNumber + 2);
                            enemy.setRegeneration(0);
                        }
                    }
                }
                case 3 -> {
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        if (enemy.property.hasBurningEliteBuff()) {
                            enemy.setBurningHealthBuff(false);
                            enemy.setHealth(enemy.property.origMaxHealth);
                            enemy.gainStrength(-enemy.getStrength());
                            enemy.setMetallicize(0);
                            enemy.setRegeneration(2 * enemy.property.actNumber + 1);
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
    }
}
