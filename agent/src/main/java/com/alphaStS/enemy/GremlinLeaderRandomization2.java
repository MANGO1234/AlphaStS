package com.alphaStS.enemy;

import com.alphaStS.GameState;
import com.alphaStS.GameStateRandomization;
import com.alphaStS.card.Card;
import com.alphaStS.random.RandomGenCtx;

import java.util.*;

/**
 * Created by mango123 on 4/1/2026.
 */
public class GremlinLeaderRandomization2 implements GameStateRandomization {
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
            enemy.setHealth(enemy.getEnemyProperty(enemies.get(i)).maxHealth);
        }
        state.getEnemiesForWrite().getForWrite(startIdx + 3).setHealth(state.getEnemiesForRead().get(startIdx + 3).properties.maxHealth);
        state.enemiesAlive = 3;
    }

    @Override public Map<Integer, Info> listRandomizations() {
        return infoMap;
    }

    @Override public List<Card> getPossibleGeneratedCards() {
        return List.of();
    }
}
