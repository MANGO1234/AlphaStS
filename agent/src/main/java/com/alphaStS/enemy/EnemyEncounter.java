package com.alphaStS.enemy;

import com.alphaStS.*;
import com.alphaStS.card.CardManager;
import com.alphaStS.entity.Potion;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.random.RandomGenCtx;
import java.util.*;

public class EnemyEncounter {
    public record EnemyInfo(int index, boolean isMergedEnemy) {}

    public PredefinedEncounter encounterEnum;
    public List<EnemyInfo> idxes;
    public GameStateRandomization randomization;

    public EnemyEncounter(PredefinedEncounter encounterEnum, ArrayList<EnemyInfo> indexes) {
        this.encounterEnum = encounterEnum;
        this.idxes = indexes;
    }

    public void startFight(GameState state) {
        var enemies = state.getEnemiesForWrite();
        for (int i = 0; i < enemies.size(); i++) {
            enemies.getForWrite(i).setHealth(0);
        }
        for (var t : idxes) {
            var enemy = enemies.getForWrite(t.index());
            if (!enemy.startDead) {
                if (t.isMergedEnemy()) {
                    var e = (Enemy.MergedEnemy) enemy;
                    e.setEnemy(0);
                    enemy.setHealth(e.getEnemyProperty(0).maxHealth);
                } else {
                    enemy.setHealth(enemy.properties.maxHealth);
                }
            }
        }
        state.currentEncounter = encounterEnum;
        int k = 0;
        for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
            if (state.getEnemiesForRead().get(i).getHealth() > 0) {
                k++;
            }
        }
        state.enemiesAlive = k;
    }

    public static void addDualFungiBeastFight(GameStateBuilder builder) {
        builder.addEnemyEncounter(PredefinedEncounter.DUAL_FUNGI_BEASTS, new EnemyExordium.FungiBeast(), new EnemyExordium.FungiBeast());
    }

    public static void addLargeSpikeSlimeFight(GameStateBuilder builder) {
        builder.addEnemyEncounter(PredefinedEncounter.LARGE_SPIKE_SLIME, new EnemyExordium.LargeSpikeSlime(), new EnemyExordium.MediumSpikeSlime(36).startDead(), new EnemyExordium.MediumSpikeSlime(36).startDead());
    }

    public static void addGremlinLeaderFight(GameStateBuilder builder) {
        // todo: in some situations, order matter
        var start = builder.getEnemies().size();
        var gremlinList = List.of(new EnemyExordium.MadGremlin(), new EnemyExordium.SneakyGremlin(), new EnemyExordium.FatGremlin(), new EnemyExordium.GremlinWizard(),
                new EnemyExordium.ShieldGremlin());
        for (var gremlin : gremlinList) {
            gremlin.properties.isElite = true;
            gremlin.properties.isMinion = true;
            gremlin.properties.actNumber = 2;
        }
        var gremlin0 = new Enemy.MergedEnemy(gremlinList);
        gremlinList = List.of(new EnemyExordium.MadGremlin(), new EnemyExordium.SneakyGremlin(), new EnemyExordium.FatGremlin(),
                new EnemyExordium.ShieldGremlin(), new EnemyExordium.GremlinWizard());
        for (var gremlin : gremlinList) {
            gremlin.properties.isElite = true;
            gremlin.properties.isMinion = true;
            gremlin.properties.actNumber = 2;
        }
        var gremlin1 = new Enemy.MergedEnemy(gremlinList);
        gremlinList = List.of(new EnemyExordium.MadGremlin(), new EnemyExordium.SneakyGremlin(), new EnemyExordium.FatGremlin(),
                new EnemyExordium.ShieldGremlin(), new EnemyExordium.GremlinWizard());
        for (var gremlin : gremlinList) {
            gremlin.properties.isElite = true;
            gremlin.properties.isMinion = true;
            gremlin.properties.actNumber = 2;
        }
        builder.addEnemyEncounter(PredefinedEncounter.GREMLIN_LEADER, gremlin0, gremlin1, new Enemy.MergedEnemy(gremlinList), new EnemyCity.GremlinLeader());
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
        builder.addEnemyEncounter(PredefinedEncounter.SLAVERS_ELITE, blueSlaver, taskmaster, redSlaver);
    }

    public static void addAwakenedOneFight(GameStateBuilder builder) {
        var start = builder.getEnemies().size();
        builder.addEnemyEncounter(PredefinedEncounter.AWAKENED_ONE, new EnemyExordium.Cultist(), new EnemyExordium.Cultist(), new EnemyBeyond.AwakenedOne());
        builder.addEnemyReordering((state, order) -> {
            if (state.getEnemiesForRead().get(start).getHealth() > state.getEnemiesForRead().get(start + 1).getHealth()) {
                order[start] = start + 1;
                order[start + 1] = start;
            }
        });
    }

    public static void addDonuAndDecaFight(GameStateBuilder builder) {
        builder.addEnemyEncounter(PredefinedEncounter.DONU_AND_DECA, new EnemyBeyond.Deca(), new EnemyBeyond.Donu());
    }

    public static void addAcidSlimeFight(GameStateBuilder builder) {
        builder.addEnemyEncounter(PredefinedEncounter.ACID_SLIME, new EnemyExordium.LargeAcidSlime(), new EnemyExordium.MediumAcidSlime(36).startDead(), new EnemyExordium.MediumAcidSlime(36).startDead());
    }

    public static void addByrdsFight(GameStateBuilder builder) {
        var start = builder.getEnemies().size();
        builder.addEnemyEncounter(PredefinedEncounter.TRIPLE_BYRDS, new EnemyCity.Byrd(), new EnemyCity.Byrd(), new EnemyCity.Byrd());
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
        builder.addEnemyEncounter(PredefinedEncounter.CENTURION_AND_MYSTIC, new EnemyCity.Centurion(), new EnemyCity.Mystic());
    }

    public static void addSentryAndSphericGuardianFight(GameStateBuilder builder) {
        var sentry = new EnemyExordium.Sentry(EnemyExordium.Sentry.BOLT);
        sentry.properties.isElite = false;
        builder.addEnemyEncounter(PredefinedEncounter.SENTRY_AND_SPHERIC_GUARDIAN, sentry, new EnemyCity.SphericGuardian());
    }

    public static void addSentriesFight(GameStateBuilder builder) {
        var start = builder.getEnemies().size();
        builder.addEnemyEncounter(PredefinedEncounter.SENTRIES, new EnemyExordium.Sentry(45, EnemyExordium.Sentry.BOLT),
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
        builder.addEnemyEncounter(PredefinedEncounter.ROBBERS_EVENT, new EnemyCity.Pointy(), new EnemyCity.Romeo(), new EnemyCity.Bear());
    }

    public static void addCultistsFight(GameStateBuilder builder) {
        var start = builder.getEnemies().size();
        builder.addEnemyEncounter(PredefinedEncounter.TRIPLE_CULTISTS, new EnemyExordium.Cultist(), new EnemyExordium.Cultist(), new EnemyExordium.Cultist());
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

    public static void addDarklingsFight(GameStateBuilder builder) {
        var start = builder.getEnemies().size();
        builder.addEnemyEncounter(PredefinedEncounter.TRIPLE_DARKLINGS, new EnemyBeyond.Darkling(false), new EnemyBeyond.Darkling(true), new EnemyBeyond.Darkling(false));
        builder.addEnemyReordering((state, order) -> {
            if (state.getEnemiesForRead().get(start).getHealth() > state.getEnemiesForRead().get(start + 2).getHealth()) {
                order[start] = start + 2;
                order[start + 2] = start;
            }
        });
    }

    public static void addTripleJawWormsFight(GameStateBuilder builder) {
        var start = builder.getEnemies().size();
        builder.addEnemyEncounter(PredefinedEncounter.TRIPLE_JAW_WORMS, new EnemyExordium.JawWorm(true), new EnemyExordium.JawWorm(true), new EnemyExordium.JawWorm(true));
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
        builder.addEnemyEncounter(PredefinedEncounter.TRIPLE_CULTISTS, new EnemyExordium.Cultist(), new EnemyExordium.Cultist(), new EnemyExordium.Cultist());
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
        builder.addEnemyEncounter(PredefinedEncounter.REPTOMANCER, new EnemyBeyond.Dagger(), // top left
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

    public enum ACT3_BOSS {
        TIME_EATER_BOSS, AWAKENED_ONE_BOSS, DONU_AND_DECA_BOSS
    } ;

    public static void addAct3BossConsecutiveFight(GameStateBuilder builder, ACT3_BOSS startingBoss) {
        if (startingBoss == ACT3_BOSS.TIME_EATER_BOSS) {
            builder.addEnemyEncounter(new EnemyBeyond.TimeEater(), new EnemyBeyond.Deca(), new EnemyBeyond.Donu(), new EnemyExordium.Cultist(), new EnemyExordium.Cultist(), new EnemyBeyond.AwakenedOne());
        } else if (startingBoss == ACT3_BOSS.AWAKENED_ONE_BOSS) {
            builder.addEnemyEncounter(new EnemyBeyond.TimeEater(), new EnemyBeyond.Deca(), new EnemyBeyond.Donu(), new EnemyExordium.Cultist(), new EnemyExordium.Cultist(), new EnemyBeyond.AwakenedOne());
        } else {
            builder.addEnemyEncounter(new EnemyBeyond.TimeEater(), new EnemyBeyond.Deca(), new EnemyBeyond.Donu(), new EnemyExordium.Cultist(), new EnemyExordium.Cultist(), new EnemyBeyond.AwakenedOne());
        }
        builder.setSwitchBattleHandler((state) -> {
//            for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
//                if (state.getEnemiesForRead().get(i) instanceof EnemyEnding.CorruptHeart heart) {
//                    if (heart.getInvincible() >= 0) {
//                        return state;
//                    }
//                }
//            }
            var newState = state.properties.originalGameState.clone(false);
            newState.realTurnNum = state.realTurnNum;
            if (newState.actionCtx == GameActionCtx.BEGIN_PRE_BATTLE) {
                newState.doAction(0);
            }
            if (newState.actionCtx == GameActionCtx.SELECT_SCENARIO) {
                newState.doAction(state.preBattleScenariosChosenIdx);
            }
            var nextBoss = state.getSearchRandomGen().nextBoolean(RandomGenCtx.Other);
            if (startingBoss == ACT3_BOSS.TIME_EATER_BOSS) {
                newState.killEnemy(0, false);
                if (nextBoss) {
                    newState.reviveEnemy(1, false, -1);
                    newState.reviveEnemy(2, false, -1);
                } else {
                    newState.reviveEnemy(3, false, -1);
                    newState.reviveEnemy(4, false, -1);
                    newState.reviveEnemy(5, false, -1);
                }
            } else if (startingBoss == ACT3_BOSS.AWAKENED_ONE_BOSS) {
                newState.killEnemy(3, false);
                newState.killEnemy(4, false);
                newState.killEnemy(5, false);
                if (nextBoss) {
                    newState.reviveEnemy(0, false, -1);
                } else {
                    newState.reviveEnemy(1, false, -1);
                    newState.reviveEnemy(2, false, -1);
                }
            } else {
                newState.killEnemy(1, false);
                newState.killEnemy(2, false);
                if (nextBoss) {
                    newState.reviveEnemy(0, false, -1);
                } else {
                    newState.reviveEnemy(3, false, -1);
                    newState.reviveEnemy(4, false, -1);
                    newState.reviveEnemy(5, false, -1);
                }
            }
            newState.getPlayerForWrite().setHealth(state.getPlayerForRead().getHealth());
            if (state.properties.potions.size() > 0) {
                for (int i = 0; i < newState.getPotionsStateForWrite().length; i++) {
                    newState.getPotionsStateForWrite()[i] = state.getPotionsStateForRead()[i];
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
            return newState;
        });
    }

    public static void addShieldAndSpearFollowByHeartFight(GameStateBuilder builder) {
        builder.addEnemyEncounter(PredefinedEncounter.SPEAR_AND_SHIELD, new EnemyEnding.SpireShield(), new EnemyEnding.SpireSpear(), new EnemyEnding.CorruptHeart(800, true));
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
            newState.currentEncounter = PredefinedEncounter.CORRUPT_HEART;
            if (newState.actionCtx == GameActionCtx.BEGIN_PRE_BATTLE) {
                newState.doAction(0);
            }
            newState.isStochastic = false;
            if (newState.actionCtx == GameActionCtx.SELECT_SCENARIO) {
                newState.doAction(state.preBattleScenariosChosenIdx);
            }
            newState.killEnemy(0, false);
            newState.killEnemy(1, false);
            newState.reviveEnemy(2, false, -1);
            ((EnemyEnding.CorruptHeart) newState.getEnemiesForWrite().getForWrite(2)).setInvincible(200);
            newState.getPlayerForWrite().setHealth(state.getPlayerForRead().getHealth());
            newState.getPlayerForWrite().setInBattleMaxHealth(state.getPlayerForRead().getInBattleMaxHealth());
            newState.getPlayerForWrite().setAccumulatedDamage(state.getPlayerForRead().getAccumulatedDamage());
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
        state.setSelect1OutOf3Idxes(state.properties.cardRewardIdxes);
        state.setActionCtx(GameActionCtx.SELECT_CARD_1_OUT_OF_3, null, null);
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

    public static void heartGauntletSetup(GameState state) {
        var cards = CardManager.getPossibleSelect1OutOf3CardsFromRewardScreen(state.properties.character);
        state.properties.cardRewardIdxes = new int[cards.size()];
        for (int i = 0; i < cards.size(); i++) {
            state.properties.cardRewardIdxes[i] = state.properties.select1OutOf3CardsReverseIdxes[state.properties.findCardIndex(cards.get(i))];
        }
    }
}
