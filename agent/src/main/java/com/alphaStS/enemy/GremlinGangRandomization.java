package com.alphaStS.enemy;

import com.alphaStS.GameState;
import com.alphaStS.GameStateRandomization;
import com.alphaStS.card.Card;
import com.alphaStS.random.RandomGenCtx;

import java.util.*;

/**
 * Created by mango123 on 4/1/2026.
 */
public class GremlinGangRandomization implements GameStateRandomization {
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
            enemy.setHealth(enemy.properties.maxHealth);
        }
        state.enemiesAlive = 4;
    }

    @Override public Map<Integer, Info> listRandomizations() {
        return infoMap;
    }

    @Override public List<Card> getPossibleGeneratedCards() {
        return List.of();
    }
}
