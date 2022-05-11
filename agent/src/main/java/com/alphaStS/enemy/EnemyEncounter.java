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
                enemy.setHealth(enemy.maxHealth);
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
                enemy.setHealth(enemy.maxHealth);
            }
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
        builder.addEnemy(new Enemy.MadGremlin());
        builder.addEnemy(new Enemy.MadGremlin());
        builder.addEnemy(new Enemy.MadGremlin());
        builder.addEnemy(new Enemy.SneakyGremlin());
        builder.addEnemy(new Enemy.SneakyGremlin());
        builder.addEnemy(new Enemy.SneakyGremlin());
        builder.addEnemy(new Enemy.FatGremlin());
        builder.addEnemy(new Enemy.FatGremlin());
        builder.addEnemy(new Enemy.FatGremlin());
        builder.addEnemy(new Enemy.ShieldGremlin());
        builder.addEnemy(new Enemy.ShieldGremlin());
        builder.addEnemy(new Enemy.ShieldGremlin());
        builder.addEnemy(new Enemy.GremlinWizard());
        builder.addEnemy(new Enemy.GremlinWizard());
        builder.addEnemy(new Enemy.GremlinWizard());
        builder.addEnemy(new EnemyCity.GremlinLeader());
        builder.setRandomization(new GremlinLeaderRandomization(builder.getEnemies(), start).doAfter(builder.getRandomization()));
    }

    public static void addSlaversEliteFight(GameStateBuilder builder) {
        Enemy enemy = new Enemy.BlueSlaver();
        enemy.isElite = true;
        builder.addEnemy(enemy);
        builder.addEnemy(new EnemyCity.Taskmaster());
        enemy = new Enemy.RedSlaver();
        enemy.isElite = true;
        builder.addEnemy(enemy);
    }
}
