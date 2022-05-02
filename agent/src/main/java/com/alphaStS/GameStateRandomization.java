package com.alphaStS;

import com.alphaStS.enemy.Enemy;
import com.alphaStS.player.Player;

import java.util.*;
import java.util.function.Consumer;

public interface GameStateRandomization {
    int randomize(GameState state);
    void reset(GameState state);
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

    default GameStateRandomization fixR(int... r) {
        return new GameStateRandomization.FixedRandomization(this, r);
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

        @Override public void reset(GameState state) {
            b.reset(state);
            a.reset(state);
        }

        @Override public void randomize(GameState state, int r) {
            reset(state);
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

        @Override public void reset(GameState state) {
            b.reset(state);
            a.reset(state);
        }

        @Override public void randomize(GameState state, int r) {
            reset(state);
            a.randomize(state, r);
            b.randomize(state, r);
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
            var i = state.getSearchRandomGen().nextInt(rs.length, RandomGenCtx.BeginningOfGameRandomization, listRandomizations());
            a.randomize(state, rs[i]);
            return i;
        }

        @Override public void reset(GameState state) {
            a.reset(state);
        }

        @Override public void randomize(GameState state, int r) {
            reset(state);
            a.randomize(state, rs[r]);
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

        @Override public void reset(GameState state) {
            a.reset(state);
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

        public EnemyRandomization(boolean curriculumTraining) {
            this.curriculumTraining = curriculumTraining;
        }

        @Override public int randomize(GameState state) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.randomize(state.getSearchRandomGen(), curriculumTraining);
                if (enemy.hasBurningHealthBuff()) {
                    enemy.setHealth((int) (enemy.getHealth() * 1.25));
                }
            }
            if (state.getEnemiesForRead().size() >= 3) {
                if (state.getEnemiesForRead().get(0).getClass().equals(state.getEnemiesForRead().get(2).getClass())) {
                    if (state.getEnemiesForWrite().get(2).getHealth() < state.getEnemiesForWrite().get(0).getHealth()) {
                        var h = state.getEnemiesForWrite().get(2).getHealth();
                        state.getEnemiesForWrite().getForWrite(2).setHealth(state.getEnemiesForWrite().get(0).getHealth());
                        state.getEnemiesForWrite().getForWrite(0).setHealth(h);
                    }
                }
            }
            return 0;
        }

        @Override public void reset(GameState state) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.setHealth(enemy.maxHealth);
            }
        }

        @Override public void randomize(GameState state, int r) {
            reset(state);
            randomize(state);
        }

        @Override public Map<Integer, Info> listRandomizations() {
            Map<Integer, Info> map = new HashMap<>();
            map.put(0, new Info(1, "Randomize enemy starting state (e.g. health)" + (curriculumTraining ? " with curriculum training" : "")));
            return map;
        }
    }

    class PotionsUtilityRandomization implements GameStateRandomization {
        private final List<Potion> potions;
        private final int steps;
        private final int startingRatio;

        public PotionsUtilityRandomization(List<Potion> potions, int steps) {
            this(potions, steps, 100);
        }

        public PotionsUtilityRandomization(List<Potion> potions, int steps, int startingRatio) {
            this.potions = potions;
            this.steps = steps;
            this.startingRatio = startingRatio;
        }

        // todo: think of a better distribution
        @Override public int randomize(GameState state) {
            var r = state.getSearchRandomGen().nextInt(10, RandomGenCtx.BeginningOfGameRandomization, listRandomizations());
            if (r < 4) {
                r = 0;
            } else {
                var upto = (int) Math.pow(steps, potions.size());
                r = 1 + state.getSearchRandomGen().nextInt(upto, RandomGenCtx.BeginningOfGameRandomization, listRandomizations());
            }
            randomize(state, r);
            return r;
        }

        @Override public void reset(GameState state) {
            for (int i = 0; i < potions.size(); i++) {
                state.potionsState[i * 3] = 0;
                state.potionsState[i * 3 + 1] = startingRatio;
                state.potionsState[i * 3 + 2] = 0;
            }
        }

        @Override public void randomize(GameState state, int r) {
            reset(state);
            if (r > 0) {
                r--;
                int[] s = new int[potions.size()];
                for (int i = 0; i < s.length; i++) {
                    s[i] = r % steps;
                    r /= steps;
                }
                for (int i = 0; i < potions.size(); i++) {
                    state.potionsState[i * 3] = 1;
                    state.potionsState[i * 3 + 1] = startingRatio - 5 * s[i];
                    state.potionsState[i * 3 + 2] = 1;
                }
            }
        }

        @Override public Map<Integer, Info> listRandomizations() {
            Map<Integer, Info> map = new HashMap<>();
            map.put(0, new Info(0.2, "No potions can be used"));
            var upto = (int) Math.pow(steps, potions.size());
            for (int cur_r = 0; cur_r < upto; cur_r++) {
                String[] desc = new String[potions.size()];
                var r = cur_r;
                for (int i = 0; i < desc.length; i++) {
                    var u = startingRatio - 5 * (r % steps);
                    desc[i] = potions.get(i) + " " + u;
                    r /= steps;
                }
                map.put(cur_r + 1, new Info(0.8 / upto, String.join(", ", desc)));
            }
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
            int r = state.getSearchRandomGen().nextInt(scenarios.size(), RandomGenCtx.BeginningOfGameRandomization, listRandomizations());
            randomize(state, r);
            return r;
        }

        @Override public void reset(GameState state) {
            randomize(state, 0);
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
        private final List<int[]> scenarios;
        private final Map<Integer, Info> infoMap;

        public EnemyEncounterRandomization(List<Enemy> enemies, int[]... enemiesIdx) {
            this.scenarios = new ArrayList<>();
            infoMap = new HashMap<>();
            for (int i = 0; i < enemiesIdx.length; i++) {
                scenarios.add(enemiesIdx[i]);
                var names = Arrays.stream(enemiesIdx[i]).mapToObj((idx) -> enemies.get(idx).getName()).toList();
                infoMap.put(i, new Info(1.0 / enemiesIdx.length, String.join(", ", names)));
            }
        }

        @Override public int randomize(GameState state) {
            int r = state.getSearchRandomGen().nextInt(scenarios.size(), RandomGenCtx.BeginningOfGameRandomization, listRandomizations());
            randomize(state, r);
            return r;
        }

        @Override public void reset(GameState state) {
            randomize(state, 0);
        }

        @Override public void randomize(GameState state, int r) {
            var enemies = state.getEnemiesForWrite();
            for (int i = 0; i < enemies.size(); i++) {
                enemies.getForWrite(i).setHealth(0);
            }
            var enemiesIdx = scenarios.get(r);
            for (int idx : enemiesIdx) {
                var enemy = state.getEnemiesForWrite().getForWrite(idx);
                enemy.setHealth(enemy.maxHealth);
            }
            state.enemiesAlive = enemiesIdx.length;
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
            int r = state.getSearchRandomGen().nextInt(randomizations.size(), RandomGenCtx.BeginningOfGameRandomization, listRandomizations());
            randomize(state, r);
            return r;
        }

        @Override public void reset(GameState state) {
            randomize(state, 0);
        }

        @Override public void randomize(GameState state, int r) {
            randomizations.get(r).accept(state);
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }
    }

    // todo: floor
    class BurningEliteRandomization implements GameStateRandomization {
        @Override public int randomize(GameState state) {
            int r = state.getSearchRandomGen().nextInt(4, RandomGenCtx.BeginningOfGameRandomization, listRandomizations());
            randomize(state, r);
            return r;
        }

        // todo: need to think about reset, e.g. philosopher stone with resetting strength below
        @Override public void reset(GameState state) {
            randomize(state, 0);
        }

        @Override public void randomize(GameState state, int r) {
            switch (r) {
                case 0 -> {
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        if (enemy.canGainRegeneration) {
                            enemy.setBurningHealthBuff(true);
                            enemy.setHealth(enemy.maxHealth);
                            enemy.gainStrength(-enemy.getStrength());
                            enemy.setMetallicize(0);
                            enemy.setRegeneration(0);
                        }
                    }
                }
                case 1 -> {
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        if (enemy.canGainRegeneration) {
                            enemy.setBurningHealthBuff(false);
                            enemy.setHealth(enemy.origHealth);
                            enemy.gainStrength(2);
                            enemy.setMetallicize(0);
                            enemy.setRegeneration(0);
                        }
                    }
                }
                case 2 -> {
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        if (enemy.canGainRegeneration) {
                            enemy.setBurningHealthBuff(false);
                            enemy.setHealth(enemy.origHealth);
                            enemy.gainStrength(-enemy.getStrength());
                            enemy.setMetallicize(4);
                            enemy.setRegeneration(0);
                        }
                    }
                }
                case 3 -> {
                    for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                        if (enemy.canGainRegeneration) {
                            enemy.setBurningHealthBuff(false);
                            enemy.setHealth(enemy.origHealth);
                            enemy.gainStrength(-enemy.getStrength());
                            enemy.setMetallicize(0);
                            enemy.setRegeneration(3);
                        }
                    }
                }
            }
        }

        @Override public Map<Integer, Info> listRandomizations() {
            var map = new HashMap<Integer, Info>();
            map.put(0, new Info(1.0 / 4, "+25% Health"));
            map.put(1, new Info(1.0 / 4, "+2 Strength"));
            map.put(2, new Info(1.0 / 4, "+4 Metallicize"));
            map.put(3, new Info(1.0 / 4, "+3 Regeneration"));
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
                this.cards = possibleCards.stream().map(CardCount::card).filter((card) -> CardUpgrade.map.containsKey(card)).toList();
            } else {
                this.cards = cards.stream().filter((card) -> CardUpgrade.map.containsKey(card)).toList();
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
            int r = state.getSearchRandomGen().nextInt(cards.size(), RandomGenCtx.BeginningOfGameRandomization, listRandomizations());
            randomize(state, r);
            return r;
        }

        @Override public void reset(GameState state) {
            randomize(state, 0);
        }

        @Override public void randomize(GameState state, int r) {
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }
    }
}
