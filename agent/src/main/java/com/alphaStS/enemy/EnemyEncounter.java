package com.alphaStS.enemy;

import com.alphaStS.GameState;
import com.alphaStS.GameStateBuilder;
import com.alphaStS.GameStateRandomization;
import com.alphaStS.RandomGenCtx;

import java.util.*;

public class EnemyEncounter {
    private static class GremlinGangRandomization implements GameStateRandomization {
        Map<Integer, Integer> rMap = new HashMap<>();
        Map<Integer, List<Integer>> enemiesMap = new HashMap<>();
        Map<Integer, Info> infoMap = new HashMap<>();

        public GremlinGangRandomization(List<Enemy> enemies, int startIdx) {
            var map = new HashMap<List<Integer>, Integer>();
            var iMap = new HashMap<Integer, List<Integer>>();
            for (int a = 0; a < 8; a++) {
                for (int b = 0; b < 7; b++) {
                    for (int c = 0; c < 6; c++) {
                        for (int d = 0; d < 5; d++) {
                            var i = ((a * 8 + b) * 7 + c) * 6 + d;
                            var indexes = new ArrayList<Integer>();
                            indexes.add(startIdx + a);
                            var k = -1;
                            for (int j = 0; j < 8; j++) {
                                if (!indexes.contains(startIdx + j)) {
                                    if ((++k) == b) {
                                        indexes.add(startIdx + j);
                                        break;
                                    }
                                }
                            }
                            k = -1;
                            for (int j = 0; j < 8; j++) {
                                if (!indexes.contains(startIdx + j)) {
                                    if ((++k) == c) {
                                        indexes.add(startIdx + j);
                                        break;
                                    }
                                }
                            }
                            k = -1;
                            for (int j = 0; j < 8; j++) {
                                if (!indexes.contains(startIdx + j)) {
                                    if ((++k) == d) {
                                        indexes.add(startIdx + j);
                                        break;
                                    }
                                }
                            }
                            Collections.sort(indexes);
                            for (int j = 1; j < indexes.size(); j++) {
                                if (indexes.get(j) == 3 && indexes.get(j - 1) != 2) {
                                    indexes.set(j, 2);
                                } else if (indexes.get(j) == 5 && indexes.get(j - 1) != 4) {
                                    indexes.set(j, 4);
                                }
                            }
                            iMap.put(i, indexes);
                            map.putIfAbsent(indexes, 0);
                            map.computeIfPresent(indexes, (key, x) -> x + 1);
                        }
                    }
                }
            }
            var l = map.entrySet().stream().sorted((a, b) -> {
                for (int i = 0; i < a.getKey().size(); i++) {
                    var result = Integer.compare(a.getKey().get(i), b.getKey().get(i));
                    if (result != 0) {
                        return result;
                    }
                }
                return 0;
            }).toList();
            for (int i = 0; i < l.size(); i++) {
                enemiesMap.put(i, l.get(i).getKey());
                var names = l.get(i).getKey().stream().map((idx) -> enemies.get(idx).getName()).toList();
                infoMap.put(i, new Info(l.get(i).getValue() / 8.0 / 7 / 6 / 5, String.join(", ", names)));
                map.put(l.get(i).getKey(), i);
            }
            for (var entry : iMap.entrySet()) {
                rMap.put(entry.getKey(), map.get(entry.getValue()));
            }
        }

        @Override public int randomize(GameState state) {
            var a = state.getSearchRandomGen().nextInt(8, RandomGenCtx.BeginningOfGameRandomization, this);
            var b = state.getSearchRandomGen().nextInt(7, RandomGenCtx.BeginningOfGameRandomization, this);
            var c = state.getSearchRandomGen().nextInt(6, RandomGenCtx.BeginningOfGameRandomization, this);
            var d = state.getSearchRandomGen().nextInt(5, RandomGenCtx.BeginningOfGameRandomization, this);
            var i = ((a * 8 + b) * 7 + c) * 6 + d;
            var r = rMap.get(i);
            randomize(state, r);
            return r;
        }

        @Override public void randomize(GameState state, int r) {
            var enemies = enemiesMap.get(r);
            for (int i = 0; i < state.getEnemiesForWrite().size(); i++) {
                state.getEnemiesForWrite().getForWrite(i).setHealth(0);
            }
            for (int i = 0; i < 4; i++) {
                var enemy = state.getEnemiesForWrite().getForWrite(enemies.get(i));
                enemy.setHealth(enemy.property.maxHealth);
            }
            state.enemiesAlive = 4;
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }
    }

    private static class GremlinLeaderRandomization implements GameStateRandomization {
        Map<Integer, Integer> rMap = new HashMap<>();
        Map<Integer, List<Integer>> enemiesMap = new HashMap<>();
        Map<Integer, Info> infoMap = new HashMap<>();
        int gremlinLeaderIndex;

        public GremlinLeaderRandomization(List<Enemy> enemies, int startIdx) {
            var map = new HashMap<List<Integer>, Integer>();
            var iMap = new HashMap<Integer, List<Integer>>();
            for (int a = 0; a < 8; a++) {
                for (int b = 0; b < 8; b++) {
                    var i = a * 8 + b;
                    var indexes = new ArrayList<Integer>();
                    if (a < 2) { // Mad Gremlin
                        indexes.add(startIdx + 0);
                    } else if (a < 4) { // Sneaky Gremlin
                        indexes.add(startIdx + 3);
                    } else if (a < 6) { // Fat Gremlin
                        indexes.add(startIdx + 6);
                    } else if (a < 7) { // Shield Gremlin
                        indexes.add(startIdx + 9);
                    } else { // Gremlin Wizard
                        indexes.add(startIdx + 12);
                    }
                    if (b < 2) { // Mad Gremlin
                        indexes.add(startIdx + (a < 2 ? 1 : 0));
                    } else if (b < 4) { // Sneaky Gremlin
                        indexes.add(startIdx + (2 <= a && a < 4 ? 4 : 3));
                    } else if (b < 6) { // Fat Gremlin
                        indexes.add(startIdx + (4 <= a && a < 6 ? 7 : 6));
                    } else if (b < 7) { // Shield Gremlin
                        indexes.add(startIdx + (a == 6 ? 10 : 9));
                    } else { // Gremlin Wizard
                        indexes.add(startIdx + (a == 7 ? 13 : 12));
                    }
                    indexes.add(startIdx + 15);
                    Collections.sort(indexes);
                    iMap.put(i, indexes);
                    map.putIfAbsent(indexes, 0);
                    map.computeIfPresent(indexes, (key, x) -> x + 1);
                }
            }
            var l = map.entrySet().stream().sorted((a, b) -> {
                for (int i = 0; i < a.getKey().size(); i++) {
                    var result = Integer.compare(a.getKey().get(i), b.getKey().get(i));
                    if (result != 0) {
                        return result;
                    }
                }
                return 0;
            }).toList();
            for (int i = 0; i < l.size(); i++) {
                enemiesMap.put(i, l.get(i).getKey());
                var names = l.get(i).getKey().stream().limit(2).map((idx) -> enemies.get(idx).getName()).toList();
                infoMap.put(i, new Info(l.get(i).getValue() / 8.0 / 8, String.join(", ", names)));
                map.put(l.get(i).getKey(), i);
            }
            for (var entry : iMap.entrySet()) {
                rMap.put(entry.getKey(), map.get(entry.getValue()));
            }
            gremlinLeaderIndex = startIdx + 15;
        }

        @Override public int randomize(GameState state) {
            var a = state.getSearchRandomGen().nextInt(8, RandomGenCtx.BeginningOfGameRandomization, this);
            var b = state.getSearchRandomGen().nextInt(8, RandomGenCtx.BeginningOfGameRandomization, this);
            var i = a * 8 + b;
            var r = rMap.get(i);
            randomize(state, r);
            return r;
        }

        @Override public void randomize(GameState state, int r) {
            var enemies = enemiesMap.get(r);
            for (int i = 0; i < state.getEnemiesForWrite().size(); i++) {
                state.getEnemiesForWrite().getForWrite(i).setHealth(0);
            }
            for (int i = 0; i < 3; i++) {
                var enemy = state.getEnemiesForWrite().getForWrite(enemies.get(i));
                enemy.setHealth(enemy.property.maxHealth);
            }
            state.enemiesAlive = 3;
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }
    }

    private static class GremlinLeaderRandomization2 implements GameStateRandomization {
        Map<Integer, Integer> rMap = new HashMap<>();
        Map<Integer, List<Integer>> enemiesMap = new HashMap<>();
        Map<Integer, Info> infoMap = new HashMap<>();
        int startIdx;
        private static final int MAD_GREMLIN = 0;
        private static final int SNEAkY_GREMLIN = 1;
        private static final int FAT_GREMLIN = 2;
        private static final int SHIELD_GREMLIN = 3;
        private static final int GREMLIN_WIZARD = 4;

        public GremlinLeaderRandomization2(List<Enemy> enemies, int startIdx) {
            var map = new HashMap<List<Integer>, Integer>();
            var iMap = new HashMap<Integer, List<Integer>>();
            for (int a = 0; a < 8; a++) {
                for (int b = 0; b < 8; b++) {
                    var i = a * 8 + b;
                    var indexes = new ArrayList<Integer>();
                    if (a < 2) { // Mad Gremlin
                        indexes.add(MAD_GREMLIN);
                    } else if (a < 4) { // Sneaky Gremlin
                        indexes.add(SNEAkY_GREMLIN);
                    } else if (a < 6) { // Fat Gremlin
                        indexes.add(FAT_GREMLIN);
                    } else if (a < 7) { // Shield Gremlin
                        indexes.add(SHIELD_GREMLIN);
                    } else { // Gremlin Wizard
                        indexes.add(GREMLIN_WIZARD);
                    }
                    if (b < 2) { // Mad Gremlin
                        indexes.add(MAD_GREMLIN);
                    } else if (b < 4) { // Sneaky Gremlin
                        indexes.add(SNEAkY_GREMLIN);
                    } else if (b < 6) { // Fat Gremlin
                        indexes.add(FAT_GREMLIN);
                    } else if (b < 7) { // Shield Gremlin
                        indexes.add(SHIELD_GREMLIN);
                    } else { // Gremlin Wizard
                        indexes.add(GREMLIN_WIZARD);
                    }
                    Collections.sort(indexes);
                    iMap.put(i, indexes);
                    map.putIfAbsent(indexes, 0);
                    map.computeIfPresent(indexes, (key, x) -> x + 1);
                }
            }
            var l = map.entrySet().stream().sorted((a, b) -> {
                for (int i = 0; i < a.getKey().size(); i++) {
                    var result = Integer.compare(a.getKey().get(i), b.getKey().get(i));
                    if (result != 0) {
                        return result;
                    }
                }
                return 0;
            }).toList();
            for (int i = 0; i < l.size(); i++) {
                enemiesMap.put(i, l.get(i).getKey());
                var names = l.get(i).getKey().stream().map((idx) -> {
                    return ((Enemy.MergedEnemy) enemies.get(startIdx)).possibleEnemies.get(idx).getName();
                }).toList();
                infoMap.put(i, new Info(l.get(i).getValue() / 8.0 / 8, String.join(", ", names)));
                map.put(l.get(i).getKey(), i);
            }
            for (var entry : iMap.entrySet()) {
                rMap.put(entry.getKey(), map.get(entry.getValue()));
            }
            this.startIdx = startIdx;
        }

        @Override public int randomize(GameState state) {
            var a = state.getSearchRandomGen().nextInt(8, RandomGenCtx.GremlinLeader, this);
            var b = state.getSearchRandomGen().nextInt(8, RandomGenCtx.GremlinLeader, this);
            var i = a * 8 + b;
            var r = rMap.get(i);
            randomize(state, r);
            return r;
        }

        @Override public void randomize(GameState state, int r) {
            var enemies = enemiesMap.get(r);
            for (int i = 0; i < state.getEnemiesForWrite().size(); i++) {
                state.getEnemiesForWrite().getForWrite(i).setHealth(0);
            }
            for (int i = 0; i < 2; i++) {
                var enemy = (Enemy.MergedEnemy) state.getEnemiesForWrite().getForWrite(startIdx + 1 + i);
                enemy.setEnemy(enemies.get(i));
                enemy.setHealth(enemy.getEnemyProperty(i).maxHealth);
            }
            state.getEnemiesForWrite().getForWrite(startIdx + 3).setHealth(state.getEnemiesForRead().get(startIdx + 3).property.maxHealth);
            state.enemiesAlive = 3;
        }

        @Override public Map<Integer, Info> listRandomizations() {
            return infoMap;
        }
    }

    public static void addGremlinGangFight(GameStateBuilder builder) {
        // todo: in some situations, order matter, create a variant where order matters (would require 20 enemies instead of 8)
        var start = builder.getEnemies().size();
        builder.addEnemy(new Enemy.MadGremlin());
        builder.addEnemy(new Enemy.MadGremlin());
        builder.addEnemy(new Enemy.SneakyGremlin());
        builder.addEnemy(new Enemy.SneakyGremlin());
        builder.addEnemy(new Enemy.FatGremlin());
        builder.addEnemy(new Enemy.FatGremlin());
        builder.addEnemy(new Enemy.ShieldGremlin());
        builder.addEnemy(new Enemy.GremlinWizard());
        builder.setRandomization(new GremlinGangRandomization(builder.getEnemies(), start).doAfter(builder.getRandomization()));
    }

    public static void addGremlinLeaderFight(GameStateBuilder builder) {
        // todo: in some situations, order matter, create a variant where order matters
        var start = builder.getEnemies().size();
        Enemy enemy = new Enemy.MadGremlin();
        enemy.property.isMinion = true;
        builder.addEnemy(enemy);
        enemy = new Enemy.MadGremlin();
        enemy.property.isMinion = true;
        builder.addEnemy(enemy);
        enemy = new Enemy.MadGremlin();
        enemy.property.isMinion = true;
        builder.addEnemy(enemy);
        enemy = new Enemy.SneakyGremlin();
        enemy.property.isMinion = true;
        builder.addEnemy(enemy);
        enemy = new Enemy.SneakyGremlin();
        enemy.property.isMinion = true;
        builder.addEnemy(enemy);
        enemy = new Enemy.SneakyGremlin();
        enemy.property.isMinion = true;
        builder.addEnemy(enemy);
        enemy = new Enemy.FatGremlin();
        enemy.property.isMinion = true;
        builder.addEnemy(enemy);
        enemy = new Enemy.FatGremlin();
        enemy.property.isMinion = true;
        builder.addEnemy(enemy);
        enemy = new Enemy.FatGremlin();
        enemy.property.isMinion = true;
        builder.addEnemy(enemy);
        enemy = new Enemy.ShieldGremlin();
        enemy.property.isMinion = true;
        builder.addEnemy(enemy);
        enemy = new Enemy.ShieldGremlin();
        enemy.property.isMinion = true;
        builder.addEnemy(enemy);
        enemy = new Enemy.ShieldGremlin();
        enemy.property.isMinion = true;
        builder.addEnemy(enemy);
        enemy = new Enemy.GremlinWizard();
        enemy.property.isMinion = true;
        builder.addEnemy(enemy);
        enemy = new Enemy.GremlinWizard();
        enemy.property.isMinion = true;
        builder.addEnemy(enemy);
        enemy = new Enemy.GremlinWizard();
        enemy.property.isMinion = true;
        builder.addEnemy(enemy);
        builder.addEnemy(new EnemyCity.GremlinLeader());
        builder.setRandomization(new GremlinLeaderRandomization(builder.getEnemies(), start).doAfter(builder.getRandomization()));
    }


    public static void addGremlinLeaderFight2(GameStateBuilder builder) {
        // todo: in some situations, order matter
        var start = builder.getEnemies().size();
        var gremlinList = List.of(new Enemy.MadGremlin(), new Enemy.SneakyGremlin(), new Enemy.FatGremlin(), new Enemy.GremlinWizard(),
                new Enemy.ShieldGremlin());
        for (var gremlin : gremlinList) {
            gremlin.property.isMinion = true;
        }
        builder.addEnemy(new Enemy.MergedEnemy(gremlinList));
        gremlinList = List.of(new Enemy.MadGremlin(), new Enemy.SneakyGremlin(), new Enemy.FatGremlin(),
                new Enemy.ShieldGremlin(), new Enemy.GremlinWizard());
        for (var gremlin : gremlinList) {
            gremlin.property.isMinion = true;
        }
        builder.addEnemy(new Enemy.MergedEnemy(gremlinList));
        gremlinList = List.of(new Enemy.MadGremlin(), new Enemy.SneakyGremlin(), new Enemy.FatGremlin(),
                new Enemy.ShieldGremlin(), new Enemy.GremlinWizard());
        for (var gremlin : gremlinList) {
            gremlin.property.isMinion = true;
        }
        builder.addEnemy(new Enemy.MergedEnemy(gremlinList));
        builder.addEnemy(new EnemyCity.GremlinLeader());
        builder.setRandomization(new GremlinLeaderRandomization2(builder.getEnemies(), start).doAfter(builder.getRandomization()));
        builder.addEnemyReordering((state, order) -> {
            var e0 = (Enemy.MergedEnemy) state.getEnemiesForRead().get(start);
            var e1 = (Enemy.MergedEnemy) state.getEnemiesForRead().get(start + 1);
            var e2 = (Enemy.MergedEnemy) state.getEnemiesForRead().get(start + 2);
            if (mergedGremlinEnemiesCompare(e0, e1) <= 0) {
                if (mergedGremlinEnemiesCompare(e0, e2) <= 0) {
                    order[start] = start;
                    order[start + 1] = mergedGremlinEnemiesCompare(e1, e2) <= 0 ? start + 1 : start + 2;
                    order[start + 2] = start + 3 - (order[start + 1] - start);
                } else {
                    order[start] = start + 2;
                    order[start + 1] = start;
                    order[start + 2] = start + 1;
                }
            } else {
                if (mergedGremlinEnemiesCompare(e1, e2) <= 0) {
                    order[start] = start + 1;
                    order[start + 1] = mergedGremlinEnemiesCompare(e0, e2) <= 0 ? start : start + 2;
                    order[start + 2] = start + 2 - (order[start + 1] - start);
                } else {
                    order[start] = start + 2;
                    order[start + 1] = start + 1;
                    order[start + 2] = start;
                }
            }
        });
    }

    private static int mergedGremlinEnemiesCompare(Enemy.MergedEnemy g1, Enemy.MergedEnemy g2) {
        int r = Integer.compare(g1.currentEnemyIdx, g2.currentEnemyIdx);
        return r != 0 ? r : Integer.compare(g1.getHealth(), g2.getHealth());
    }

    public static void addSlaversEliteFight(GameStateBuilder builder) {
        Enemy enemy = new Enemy.BlueSlaver();
        enemy.property.isElite = true;
        builder.addEnemy(enemy);
        builder.addEnemy(new EnemyCity.Taskmaster());
        enemy = new Enemy.RedSlaver();
        enemy.property.isElite = true;
        builder.addEnemy(enemy);
    }

    public static void addAwakenedOneFight(GameStateBuilder builder) {
        builder.addEnemy(new Enemy.Cultist());
        builder.addEnemy(new Enemy.Cultist());
        builder.addEnemy(new EnemyBeyond.AwakenedOne());
    }

    public static void addDonuAndDecaFight(GameStateBuilder builder) {
        builder.addEnemy(new EnemyBeyond.Deca());
        builder.addEnemy(new EnemyBeyond.Donu());
    }

    public static void addSlimeBossFight(GameStateBuilder builder) {
        builder.addEnemy(new Enemy.SlimeBoss());
        builder.addEnemy(new Enemy.LargeSpikeSlime(75, true));
        builder.addEnemy(new Enemy.MediumSpikeSlime(37, true));
        builder.addEnemy(new Enemy.MediumSpikeSlime(37, true));
        builder.addEnemy(new Enemy.LargeAcidSlime(75, true));
        builder.addEnemy(new Enemy.MediumAcidSlime(37, true));
        builder.addEnemy(new Enemy.MediumAcidSlime(37, true));
    }

    public static void addByrdsFight(GameStateBuilder builder) {
        var start = builder.getEnemies().size();
        builder.addEnemy(new EnemyCity.Byrd());
        builder.addEnemy(new EnemyCity.Byrd());
        builder.addEnemy(new EnemyCity.Byrd());
        builder.addEnemyReordering((state, order) -> {
            if (state.getEnemiesForRead().get(start).getHealth() <= state.getEnemiesForRead().get(start + 1).getHealth()) {
                if (state.getEnemiesForRead().get(start).getHealth() <= state.getEnemiesForRead().get(start + 2).getHealth()) {
                    order[start] = start;
                    order[start + 1] = state.getEnemiesForRead().get(start + 1).getHealth() <= state.getEnemiesForRead().get(start + 2).getHealth() ? start + 1 : start + 2;
                    order[start + 2] = start + 3 - (order[start + 1] - start);
                } else {
                    order[start] = start + 2;
                    order[start + 1] = start;
                    order[start + 2] = start + 1;
                }
            } else {
                if (state.getEnemiesForRead().get(start + 1).getHealth() <= state.getEnemiesForRead().get(start + 2).getHealth()) {
                    order[start] = start + 1;
                    order[start + 1] = state.getEnemiesForRead().get(start).getHealth() <= state.getEnemiesForRead().get(start + 2).getHealth() ? start : start + 2;
                    order[start + 2] = start + 2 - (order[start + 1] - start);
                } else {
                    order[start] = start + 2;
                    order[start + 1] = start + 1;
                    order[start + 2] = start;
                }
            }
        });
    }

    public static void addCenturionAndMysticFight(GameStateBuilder builder) {
        builder.addEnemy(new EnemyCity.Centurion());
        builder.addEnemy(new EnemyCity.Mystic());
    }

    public static void addSentriesFight(GameStateBuilder builder) {
        addSentriesFight(builder, false);
    }

    public static void addSentriesFight(GameStateBuilder builder, boolean burning) {
        var start = builder.getEnemies().size();
        if (burning) {
            builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BOLT).markAsBurningElite());
            builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BEAM).markAsBurningElite());
            builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BOLT).markAsBurningElite());
        } else {
            builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
            builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BEAM));
            builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
        }
        builder.addEnemyReordering((state, order) -> {
            if (state.getEnemiesForRead().get(start).getHealth() > state.getEnemiesForRead().get(start + 2).getHealth()) {
                order[start] = start + 2;
                order[start + 2] = start;
            }
        });
    }

    public static void addCultistsFight(GameStateBuilder builder) {
        var start = builder.getEnemies().size();
        builder.addEnemy(new Enemy.Cultist());
        builder.addEnemy(new Enemy.Cultist());
        builder.addEnemy(new Enemy.Cultist());
        builder.addEnemyReordering((state, order) -> {
            if (state.getEnemiesForRead().get(start).getHealth() <= state.getEnemiesForRead().get(start + 1).getHealth()) {
                if (state.getEnemiesForRead().get(start).getHealth() <= state.getEnemiesForRead().get(start + 2).getHealth()) {
                    order[start] = start;
                    order[start + 1] = state.getEnemiesForRead().get(start + 1).getHealth() <= state.getEnemiesForRead().get(start + 2).getHealth() ? start + 1 : start + 2;
                    order[start + 2] = start + 3 - (order[start + 1] - start);
                } else {
                    order[start] = start + 2;
                    order[start + 1] = start;
                    order[start + 2] = start + 1;
                }
            } else {
                if (state.getEnemiesForRead().get(start + 1).getHealth() <= state.getEnemiesForRead().get(start + 2).getHealth()) {
                    order[start] = start + 1;
                    order[start + 1] = state.getEnemiesForRead().get(start).getHealth() <= state.getEnemiesForRead().get(start + 2).getHealth() ? start : start + 2;
                    order[start + 2] = start + 2 - (order[start + 1] - start);
                } else {
                    order[start] = start + 2;
                    order[start + 1] = start + 1;
                    order[start + 2] = start;
                }
            }        });
    }

    public static void addBronzeAutomatonFight(GameStateBuilder builder) {
        builder.addEnemy(new EnemyCity.BronzeOrb());
        builder.addEnemy(new EnemyCity.BronzeAutomaton());
        builder.addEnemy(new EnemyCity.BronzeOrb());
    }

    public static void addDarklingsFight(GameStateBuilder builder) {
        var start = builder.getEnemies().size();
        builder.addEnemy(new EnemyBeyond.Darkling(false));
        builder.addEnemy(new EnemyBeyond.Darkling(true));
        builder.addEnemy(new EnemyBeyond.Darkling(false));
        builder.addEnemyReordering((state, order) -> {
            if (state.getEnemiesForRead().get(start).getHealth() > state.getEnemiesForRead().get(start + 2).getHealth()) {
                order[start] = start + 2;
                order[start + 2] = start;
            }
        });
    }

    public static void addTripleJawWormsFight(GameStateBuilder builder) {
        var start = builder.getEnemies().size();
        builder.addEnemy(new Enemy.JawWorm(true));
        builder.addEnemy(new Enemy.JawWorm(true));
        builder.addEnemy(new Enemy.JawWorm(true));
        builder.addEnemyReordering((state, order) -> {
            if (state.getEnemiesForRead().get(start).getHealth() <= state.getEnemiesForRead().get(start + 1).getHealth()) {
                if (state.getEnemiesForRead().get(start).getHealth() <= state.getEnemiesForRead().get(start + 2).getHealth()) {
                    order[start] = start;
                    order[start + 1] = state.getEnemiesForRead().get(start + 1).getHealth() <= state.getEnemiesForRead().get(start + 2).getHealth() ? start + 1 : start + 2;
                    order[start + 2] = start + 3 - (order[start + 1] - start);
                } else {
                    order[start] = start + 2;
                    order[start + 1] = start;
                    order[start + 2] = start + 1;
                }
            } else {
                if (state.getEnemiesForRead().get(start + 1).getHealth() <= state.getEnemiesForRead().get(start + 2).getHealth()) {
                    order[start] = start + 1;
                    order[start + 1] = state.getEnemiesForRead().get(start).getHealth() <= state.getEnemiesForRead().get(start + 2).getHealth() ? start : start + 2;
                    order[start + 2] = start + 2 - (order[start + 1] - start);
                } else {
                    order[start] = start + 2;
                    order[start + 1] = start + 1;
                    order[start + 2] = start;
                }
            }
        });
    }

    public static void addReptomancerFight(GameStateBuilder builder, boolean burning) {
//        var start = builder.getEnemies().size();
        if (burning) {
            builder.addEnemy(new EnemyBeyond.Dagger().markAsBurningElite());
            builder.addEnemy(new EnemyBeyond.Dagger().markAsBurningElite());
            builder.addEnemy(new EnemyBeyond.Dagger().markAsBurningElite());
            builder.addEnemy(new EnemyBeyond.Reptomancer().markAsBurningElite());
            builder.addEnemy(new EnemyBeyond.Dagger().markAsBurningElite());
        } else {
            builder.addEnemy(new EnemyBeyond.Dagger());
            builder.addEnemy(new EnemyBeyond.Dagger());
            builder.addEnemy(new EnemyBeyond.Dagger());
            builder.addEnemy(new EnemyBeyond.Reptomancer());
            builder.addEnemy(new EnemyBeyond.Dagger());
        }
//        builder.addEnemyReordering((state, order) -> {
//            if (state.getEnemiesForRead().get(start).getHealth() > state.getEnemiesForRead().get(start + 2).getHealth()) {
//                order[start] = start + 2;
//                order[start + 2] = start;
//            }
//        });
    }

    public static void addShieldAndSpearFight(GameStateBuilder builder) {
        builder.addEnemy(new EnemyEnding.SpireShield());
        builder.addEnemy(new EnemyEnding.SpireSpear());
    }
}
