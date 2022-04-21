package com.alphaStS;

import com.alphaStS.enemy.Enemy;

import java.util.HashMap;
import java.util.Map;

public interface GameStateRandomization {
    int randomize(GameState state);
    void reset(GameState state);
    void randomize(GameState state, int r);
    Map<Integer, Info> listRandomizations(GameState state);

    default GameStateRandomization doAfter(GameStateRandomization prev) {
        if (prev == null) {
            return this;
        }
        return new GameStateRandomization.Cartesian(prev, this);
    }

    record Info(double chance, String desc) {}

    class Cartesian implements GameStateRandomization {
        private final GameStateRandomization a;
        private final GameStateRandomization b;

        Cartesian(GameStateRandomization a, GameStateRandomization b) {
            this.a = a;
            this.b = b;
        }

        @Override public int randomize(GameState state) {
            var ar = a.randomize(state);
            var br = b.randomize(state);
            return ar * b.listRandomizations(state).size() + br;
        }

        @Override public void reset(GameState state) {
            b.reset(state);
            a.reset(state);
        }

        @Override public void randomize(GameState state, int r) {
            reset(state);
            var bLen = b.listRandomizations(state).size();
            a.randomize(state, r / bLen);
            b.randomize(state, r % bLen);
        }

        @Override public Map<Integer, Info> listRandomizations(GameState state) {
            Map<Integer, Info> aMap = a.listRandomizations(state);
            Map<Integer, Info> bMap = b.listRandomizations(state);
            Map<Integer, Info> map = new HashMap<>();
            for (var aEntry : aMap.entrySet()) {
                for (var bEntry : bMap.entrySet()) {
                    var r = aEntry.getKey() * bMap.size() + bEntry.getKey();
                    var chance = aEntry.getValue().chance * bEntry.getValue().chance;
                    var desc = aEntry.getValue().desc + " + " + bEntry.getValue().desc;
                    map.put(r, new Info(chance, desc));
                }
            }
            return map;
        }
    }

    class EnemyRandomization implements GameStateRandomization {
        private boolean curriculumTraining = false;

        public EnemyRandomization(boolean curriculumTraining) {
            this.curriculumTraining = curriculumTraining;
        }

        @Override public int randomize(GameState state) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                enemy.randomize(state.prop.random, curriculumTraining);
            }
            if (state.getEnemiesForRead().size() >= 3) {
                if (state.getEnemiesForWrite().get(2).getHealth() < state.getEnemiesForWrite().get(0).getHealth()) {
                    var h = state.getEnemiesForWrite().get(2).getHealth();
                    state.getEnemiesForWrite().getForWrite(2).setHealth(state.getEnemiesForWrite().get(0).getHealth());
                    state.getEnemiesForWrite().getForWrite(0).setHealth(h);
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

        @Override public Map<Integer, Info> listRandomizations(GameState state) {
            Map<Integer, Info> map = new HashMap<>();
            map.put(0, new Info(1, "Randomize enemy starting state (e.g. health)" + (curriculumTraining ? " with curriculum training" : "")));
            return map;
        }
    }
}
