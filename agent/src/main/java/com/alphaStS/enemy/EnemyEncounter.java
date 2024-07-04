package com.alphaStS.enemy;

import com.alphaStS.*;
import com.alphaStS.card.Card;
import com.alphaStS.card.CardSilent;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.utils.Tuple3;

import java.util.*;

public class EnemyEncounter {
    public static class GremlinGangRandomization implements GameStateRandomization {
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

    public static class GremlinLeaderRandomization2 implements GameStateRandomization {
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

    public static void addGremlinGangFight(GameStateBuilder builder) {
        // todo: in some situations, order matter, create a variant where order matters (would require 20 enemies instead of 8)
        builder.addEnemyEncounter(new EnemyExordium.MadGremlin(), new EnemyExordium.MadGremlin(),
                new EnemyExordium.SneakyGremlin(), new EnemyExordium.SneakyGremlin(),
                new EnemyExordium.FatGremlin(), new EnemyExordium.FatGremlin(),
                new EnemyExordium.ShieldGremlin(), new EnemyExordium.GremlinWizard());
    }

    public static void addGremlinLeaderFight(GameStateBuilder builder) {
        // todo: in some situations, order matter
        var start = builder.getEnemies().size();
        var gremlinList = List.of(new EnemyExordium.MadGremlin(), new EnemyExordium.SneakyGremlin(), new EnemyExordium.FatGremlin(), new EnemyExordium.GremlinWizard(),
                new EnemyExordium.ShieldGremlin());
        for (var gremlin : gremlinList) {
            gremlin.properties.isMinion = true;
        }
        var gremlin0 = new Enemy.MergedEnemy(gremlinList);
        gremlinList = List.of(new EnemyExordium.MadGremlin(), new EnemyExordium.SneakyGremlin(), new EnemyExordium.FatGremlin(),
                new EnemyExordium.ShieldGremlin(), new EnemyExordium.GremlinWizard());
        for (var gremlin : gremlinList) {
            gremlin.properties.isMinion = true;
        }
        var gremlin1 = new Enemy.MergedEnemy(gremlinList);
        gremlinList = List.of(new EnemyExordium.MadGremlin(), new EnemyExordium.SneakyGremlin(), new EnemyExordium.FatGremlin(),
                new EnemyExordium.ShieldGremlin(), new EnemyExordium.GremlinWizard());
        for (var gremlin : gremlinList) {
            gremlin.properties.isMinion = true;
        }
        builder.addEnemyEncounter(gremlin0, gremlin1, new Enemy.MergedEnemy(gremlinList), new EnemyCity.GremlinLeader());
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
        Enemy blueSlaver = new EnemyExordium.BlueSlaver();
        Enemy redSlaver = new EnemyExordium.RedSlaver();
        Enemy taskmaster = new EnemyCity.Taskmaster();
        blueSlaver.properties.isElite = true;
        redSlaver.properties.isElite = true;
        taskmaster.properties.isElite = true;
        builder.addEnemyEncounter(blueSlaver, taskmaster, redSlaver);
    }

    public static void addAwakenedOneFight(GameStateBuilder builder) {
        var start = builder.getEnemies().size();
        builder.addEnemyEncounter(new EnemyExordium.Cultist(), new EnemyExordium.Cultist(), new EnemyBeyond.AwakenedOne());
        builder.addEnemyReordering((state, order) -> {
            if (state.getEnemiesForRead().get(start).getHealth() > state.getEnemiesForRead().get(start + 1).getHealth()) {
                order[start] = start + 1;
                order[start + 1] = start;
            }
        });
    }

    public static void addDonuAndDecaFight(GameStateBuilder builder) {
        builder.addEnemyEncounter(new EnemyBeyond.Deca(), new EnemyBeyond.Donu());
    }

    public static void addSlimeBossFight(GameStateBuilder builder) {
        builder.addEnemyEncounter(new EnemyExordium.SlimeBoss(),
                new EnemyExordium.LargeSpikeSlime(75, true),
                new EnemyExordium.MediumSpikeSlime(37, true),
                new EnemyExordium.MediumSpikeSlime(37, true),
                new EnemyExordium.LargeAcidSlime(75, true),
                new EnemyExordium.MediumAcidSlime(37, true),
                new EnemyExordium.MediumAcidSlime(37, true));
    }

    public static void addByrdsFight(GameStateBuilder builder) {
        var start = builder.getEnemies().size();
        builder.addEnemyEncounter(new EnemyCity.Byrd(), new EnemyCity.Byrd(), new EnemyCity.Byrd());
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
        builder.addEnemyEncounter(new EnemyCity.Centurion(), new EnemyCity.Mystic());
    }

    public static void addSentriesFight(GameStateBuilder builder) {
        var start = builder.getEnemies().size();
        builder.addEnemyEncounter(new EnemyExordium.Sentry(45, EnemyExordium.Sentry.BOLT),
                new EnemyExordium.Sentry(45, EnemyExordium.Sentry.BEAM),
                new EnemyExordium.Sentry(45, EnemyExordium.Sentry.BOLT));
        builder.addEnemyReordering((state, order) -> {
            if (state.getEnemiesForRead().get(start).getHealth() > state.getEnemiesForRead().get(start + 2).getHealth()) {
                order[start] = start + 2;
                order[start + 2] = start;
            }
        });
    }

    public static void addRobbersEventFight(GameStateBuilder builder) {
        builder.addEnemyEncounter(new EnemyCity.Pointy(), new EnemyCity.Romeo(), new EnemyCity.Bear());
    }

    public static void addCollectorFight(GameStateBuilder builder) {
        builder.addEnemyEncounter(new EnemyCity.TorchHead(), new EnemyCity.TorchHead(), new EnemyCity.TheCollector());
    }

    public static void addCultistsFight(GameStateBuilder builder) {
        var start = builder.getEnemies().size();
        builder.addEnemyEncounter(new EnemyExordium.Cultist(), new EnemyExordium.Cultist(), new EnemyExordium.Cultist());
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
        builder.addEnemyEncounter(new EnemyCity.BronzeOrb(), new EnemyCity.BronzeAutomaton(), new EnemyCity.BronzeOrb());
    }

    public static void addDarklingsFight(GameStateBuilder builder) {
        var start = builder.getEnemies().size();
        builder.addEnemyEncounter(new EnemyBeyond.Darkling(false), new EnemyBeyond.Darkling(true), new EnemyBeyond.Darkling(false));
        builder.addEnemyReordering((state, order) -> {
            if (state.getEnemiesForRead().get(start).getHealth() > state.getEnemiesForRead().get(start + 2).getHealth()) {
                order[start] = start + 2;
                order[start + 2] = start;
            }
        });
    }

    public static void addTripleJawWormsFight(GameStateBuilder builder) {
        var start = builder.getEnemies().size();
        builder.addEnemyEncounter(new EnemyExordium.JawWorm(true), new EnemyExordium.JawWorm(true), new EnemyExordium.JawWorm(true));
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

    public static void addTripleCultistsFight(GameStateBuilder builder) {
        var start = builder.getEnemies().size();
        builder.addEnemyEncounter(new EnemyExordium.Cultist(), new EnemyExordium.Cultist(), new EnemyExordium.Cultist());
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

    public static void addReptomancerFight(GameStateBuilder builder) {
//        var start = builder.getEnemies().size();
        builder.addEnemyEncounter(new EnemyBeyond.Dagger(), // top left
                new EnemyBeyond.Dagger(), // bottom left
                new EnemyBeyond.Reptomancer(),
                new EnemyBeyond.Dagger(), // top right
                new EnemyBeyond.Dagger()); // bottom right
//        builder.addEnemyReordering((state, order) -> {
//            if (state.getEnemiesForRead().get(start).getHealth() > state.getEnemiesForRead().get(start + 2).getHealth()) {
//                order[start] = start + 2;
//                order[start + 2] = start;
//            }
//        });
    }

    public static void addShieldAndSpearFight(GameStateBuilder builder) {
        builder.addEnemyEncounter(new EnemyEnding.SpireShield(), new EnemyEnding.SpireSpear());
    }

    public static void addShieldAndSpearFollowByHeartFight(GameStateBuilder builder) {
        builder.addEnemyEncounter(new EnemyEnding.SpireShield(), new EnemyEnding.SpireSpear(), new EnemyEnding.CorruptHeart(800, true));
        builder.setSwitchBattleHandler((state) -> {
            for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                if (state.getEnemiesForRead().get(i) instanceof EnemyEnding.CorruptHeart heart) {
                    if (heart.getInvincible() >= 0) {
                        return state;
                    }
                }
            }
            var newState = state.properties.originalGameState.clone(false);
            newState.realTurnNum = state.realTurnNum;
            if (newState.actionCtx == GameActionCtx.BEGIN_PRE_BATTLE) {
                newState.doAction(0);
            }
            if (newState.actionCtx == GameActionCtx.SELECT_SCENARIO) {
                newState.doAction(state.preBattleScenariosChosenIdx);
            }
            newState.killEnemy(0, false);
            newState.killEnemy(1, false);
            newState.reviveEnemy(2, false, -1);
            ((EnemyEnding.CorruptHeart) newState.getEnemiesForWrite().getForWrite(2)).setInvincible(200);
            newState.getPlayerForWrite().setHealth(state.getPlayeForRead().getHealth());
            if (state.properties.potions.size() > 0) {
                for (int i = 0; i < newState.getPotionsStateForWrite().length; i++) {
                    newState.getPotionsStateForWrite()[i] = state.getPotionsStateForRead()[i];
                }
            }
            boolean usedSmokeBomb = usedSmokeBomb(newState);
            if (Configuration.HEART_GAUNTLET_POTION_REWARD) {
                if (newState.getPotionCount() < newState.properties.numOfPotionSlots && !usedSmokeBomb) {
                    if (newState.getSearchRandomGen().nextInt(100, RandomGenCtx.Other) < 40) {
                        newState.properties.potionsGenerator.generatePotion(newState);
                    }
                    newState.setIsStochastic();
                }
            }
            for (int i = 0; i < state.properties.counterInfos.length; i++) {
                if (state.properties.counterInfos[i].persistAcrossBattle) {
                    for (int j = 0; j < state.properties.counterInfos[i].length; j++) {
                        newState.getCounterForWrite()[state.properties.counterInfos[i].idx + j] = state.getCounterForRead()[state.properties.counterInfos[i].idx + j];
                    }
                }
            }
            newState.properties = state.properties;
            newState.preBattleRandomizationIdxChosen = state.preBattleRandomizationIdxChosen;
            newState.battleRandomizationIdxChosen = state.battleRandomizationIdxChosen;
            if (Configuration.HEART_GAUNTLET_CARD_REWARD) {
                if (!usedSmokeBomb) {
                    goToCardRewardCtx(newState);
                }
            }
            return newState;
        });
    }

    private static void goToCardRewardCtx(GameState state) {
        boolean interactive = state.getSearchRandomGen() instanceof InteractiveMode.RandomGenInteractive;
        int idx1 = state.getSearchRandomGen().nextInt(state.properties.cardRewardIdxes.length, RandomGenCtx.SelectCard1OutOf3,
                interactive ? new Tuple3<>(state, (255 << 8) + 255, state.properties.cardRewardIdxes) : null);
        int idx2 = state.getSearchRandomGen().nextInt(state.properties.cardRewardIdxes.length - 1, RandomGenCtx.SelectCard1OutOf3,
                interactive ? new Tuple3<>(state, (255 << 8) + idx1, state.properties.cardRewardIdxes) : null);
        int idx3 = state.getSearchRandomGen().nextInt(state.properties.cardRewardIdxes.length - 2, RandomGenCtx.SelectCard1OutOf3,
                interactive ? new Tuple3<>(state, (idx2 << 8) + idx1, state.properties.cardRewardIdxes) : null);
        if (idx2 >= idx1) {
            idx2++;
        }
        if (idx3 >= Math.min(idx1, idx2)) {
            idx3++;
        }
        if (idx3 >= Math.max(idx1, idx2)) {
            idx3++;
        }
        state.setSelect1OutOf3Idxes(state.properties.cardRewardIdxes[idx1], state.properties.cardRewardIdxes[idx2], state.properties.cardRewardIdxes[idx3]);
        state.setIsStochastic();
        state.setActionCtx(GameActionCtx.SELECT_CARD_1_OUT_OF_3, null, false);
    }

    private static boolean usedSmokeBomb(GameState state) {
        for (int i = 0; i < state.properties.potions.size(); i++) {
            if (state.properties.potions.get(i).getIsGenerated()) {
                return false;
            } else if (state.properties.potions.get(i) instanceof Potion.SmokeBomb) {
                if (state.potionUsed(i)) {
                    return true;
                }
                state.setPotionUsed(i);
            }
        }
        return false;
    }

    public static List<Card> getPossibleSelect1OutOf3CardsFromRewardScreen(GameProperties gameProperties) {
        if (gameProperties.character == CharacterEnum.IRONCLAD) {
        } else if (gameProperties.character == CharacterEnum.SILENT) {
            return List.of(
                    new CardSilent.Bane(),
                    new CardSilent.DaggerSpray(),
                    new CardSilent.DaggerThrow(),
                    new CardSilent.FlyingKnee(),
                    new CardSilent.PoisonedStab(),
                    new CardSilent.QuickSlash(),
                    new CardSilent.Slice(),
                    new CardSilent.SneakyStrike(),
                    new CardSilent.SuckerPunch(),
                    new CardSilent.AllOutAttack(),
                    new CardSilent.Backstab(),
                    new CardSilent.Choke(),
                    new CardSilent.Dash(),
                    new CardSilent.EndlessAgony(),
                    new CardSilent.Eviscerate(),
                    new CardSilent.Finisher(),
                    new CardSilent.Flechette(),
                    new CardSilent.HeelHook(),
                    new CardSilent.MasterfulStab(6),
                    new CardSilent.Predator(),
                    new CardSilent.RiddleWithHoles(),
                    new CardSilent.Skewer(),
                    new CardSilent.DieDieDie(),
                    new CardSilent.GlassKnife(),
                    new CardSilent.GrandFinale(),
                    new CardSilent.Unload(),

                    new CardSilent.Acrobatics(),
                    new CardSilent.Backflip(),
                    new CardSilent.BladeDance(),
                    new CardSilent.CloakAndDagger(),
                    new CardSilent.DeadlyPoison(),
                    new CardSilent.Deflect(),
                    new CardSilent.DodgeAndRoll(),
                    new CardSilent.Outmaneuver(),
                    new CardSilent.PiercingWail(),
                    new CardSilent.Prepared(),
                    new CardSilent.Blur(),
                    new CardSilent.BouncingFlask(),
                    new CardSilent.CalculatedGamble(),
                    new CardSilent.Catalyst(),
                    new CardSilent.Concentrate(),
                    new CardSilent.CripplingCloud(),
                    new CardSilent.Distraction(),
                    new CardSilent.EscapePlan(),
                    new CardSilent.Expertise(),
                    new CardSilent.LegSweep(),
                    new CardSilent.Reflex(),
                    new CardSilent.Setup(true),
                    new CardSilent.Tactician(),
                    new CardSilent.Terror(),
                    new CardSilent.Adrenaline(),
                    new CardSilent.Alchemize(0, 0),
                    new CardSilent.BulletTime(),
                    new CardSilent.Burst(),
                    new CardSilent.CorpseExplosion(),
                    new CardSilent.Doppelganger(),
                    new CardSilent.Malaise(),
                    new CardSilent.Nightmare(),
                    new CardSilent.PhantasmalKiller(),
                    new CardSilent.StormOfSteel(),

                    new CardSilent.Accuracy(),
                    new CardSilent.Caltrops(),
                    new CardSilent.Footwork(),
                    new CardSilent.InfiniteBlade(),
                    new CardSilent.NoxiousFume(),
                    new CardSilent.WellLaidPlans(),
                    new CardSilent.AThousandCuts(),
                    new CardSilent.AfterImage(),
                    new CardSilent.Envenom(),
                    new CardSilent.ToolsOfTheTrade(),
                    new CardSilent.WraithForm(),

                    new CardSilent.Bane().getUpgrade(),
                    new CardSilent.DaggerSpray().getUpgrade(),
                    new CardSilent.DaggerThrow().getUpgrade(),
                    new CardSilent.FlyingKnee().getUpgrade(),
                    new CardSilent.PoisonedStab().getUpgrade(),
                    new CardSilent.QuickSlash().getUpgrade(),
                    new CardSilent.Slice().getUpgrade(),
                    new CardSilent.SneakyStrike().getUpgrade(),
                    new CardSilent.SuckerPunch().getUpgrade(),
                    new CardSilent.AllOutAttack().getUpgrade(),
                    new CardSilent.Backstab().getUpgrade(),
                    new CardSilent.Choke().getUpgrade(),
                    new CardSilent.Dash().getUpgrade(),
                    new CardSilent.EndlessAgony().getUpgrade(),
                    new CardSilent.Eviscerate().getUpgrade(),
                    new CardSilent.Finisher().getUpgrade(),
                    new CardSilent.Flechette().getUpgrade(),
                    new CardSilent.HeelHook().getUpgrade(),
                    new CardSilent.MasterfulStab(6).getUpgrade(),
                    new CardSilent.Predator().getUpgrade(),
                    new CardSilent.RiddleWithHoles().getUpgrade(),
                    new CardSilent.Skewer().getUpgrade(),

                    new CardSilent.Acrobatics().getUpgrade(),
                    new CardSilent.Backflip().getUpgrade(),
                    new CardSilent.BladeDance().getUpgrade(),
                    new CardSilent.CloakAndDagger().getUpgrade(),
                    new CardSilent.DeadlyPoison().getUpgrade(),
                    new CardSilent.Deflect().getUpgrade(),
                    new CardSilent.DodgeAndRoll().getUpgrade(),
                    new CardSilent.Outmaneuver().getUpgrade(),
                    new CardSilent.PiercingWail().getUpgrade(),
                    new CardSilent.Prepared().getUpgrade(),
                    new CardSilent.Blur().getUpgrade(),
                    new CardSilent.BouncingFlask().getUpgrade(),
                    new CardSilent.CalculatedGamble().getUpgrade(),
                    new CardSilent.Catalyst().getUpgrade(),
                    new CardSilent.Concentrate().getUpgrade(),
                    new CardSilent.CripplingCloud().getUpgrade(),
                    new CardSilent.Distraction().getUpgrade(),
                    new CardSilent.EscapePlan().getUpgrade(),
                    new CardSilent.Expertise().getUpgrade(),
                    new CardSilent.LegSweep().getUpgrade(),
                    new CardSilent.Reflex().getUpgrade(),
                    new CardSilent.Setup(true).getUpgrade(),
                    new CardSilent.Tactician().getUpgrade(),
                    new CardSilent.Terror().getUpgrade(),

                    new CardSilent.Accuracy().getUpgrade(),
                    new CardSilent.Caltrops().getUpgrade(),
                    new CardSilent.Footwork().getUpgrade(),
                    new CardSilent.InfiniteBlade().getUpgrade(),
                    new CardSilent.NoxiousFume().getUpgrade(),
                    new CardSilent.WellLaidPlans().getUpgrade()
            );
        } else if (gameProperties.character == CharacterEnum.DEFECT) {
        }
        throw new IllegalArgumentException();
    }

    public static void gamePropertiesSetup(GameState state) {
        var cards = getPossibleSelect1OutOf3CardsFromRewardScreen(state.properties);
        state.properties.cardRewardIdxes = new int[cards.size()];
        for (int i = 0; i < cards.size(); i++) {
            state.properties.cardRewardIdxes[i] = state.properties.select1OutOf3CardsReverseIdxes[state.properties.findCardIndex(cards.get(i))];
        }
    }
}
