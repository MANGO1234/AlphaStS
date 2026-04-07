package com.alphaStS.enemy;

import com.alphaStS.*;
import com.alphaStS.card.CardManager;
import com.alphaStS.entity.Potion;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.random.RandomGenCtx;
import java.util.*;

public class EnemyEncounter {
    public record EnemyInfo(int index, boolean isMergedEnemy) {}

    @FunctionalInterface
    public interface EnemyReordering {
        void reorder(GameState state, EnemyEncounter encounter, int[] order);
    }

    public PredefinedEncounter encounterEnum;
    public List<EnemyInfo> idxes;
    public GameStateRandomization randomization;
    public EnemyReordering reordering;

    public EnemyEncounter(PredefinedEncounter encounterEnum, ArrayList<EnemyInfo> indexes) {
        this.encounterEnum = encounterEnum;
        this.idxes = indexes;
    }

    private EnemyEncounter clone(PredefinedEncounter predefinedEncounter) {
        var other = new EnemyEncounter(predefinedEncounter, new ArrayList<>(idxes));
        other.randomization = this.randomization;
        other.reordering = this.reordering;
        return other;
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
        state.currentEncounter = this;
        int k = 0;
        for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
            if (state.getEnemiesForRead().get(i).getHealth() > 0) {
                k++;
            }
        }
        state.enemiesAlive = k;
    }

    public static List<Enemy> getGremlinLeaderFightEnemies() {
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
        return List.of(gremlin0, gremlin1, new Enemy.MergedEnemy(gremlinList), new EnemyCity.GremlinLeader());
    }

    private static int mergedGremlinEnemiesCompare(Enemy.MergedEnemy g1, Enemy.MergedEnemy g2) {
        int r = Integer.compare(g1.currentEnemyIdx, g2.currentEnemyIdx);
        return r != 0 ? r : Integer.compare(g1.getHealth(), g2.getHealth());
    }

    public static final EnemyReordering GREMLIN_LEADER_REORDERING = (state, encounter, order) -> {
        int start = encounter.idxes.get(0).index();
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
    };

    public static final EnemyReordering AWAKENED_ONE_REORDERING = (state, encounter, order) -> {
        int start = encounter.idxes.get(0).index();
        if (state.getEnemiesForRead().get(start).getHealth() > state.getEnemiesForRead().get(start + 1).getHealth()) {
            order[start] = start + 1;
            order[start + 1] = start;
        }
    };

    public static final EnemyReordering TRIPLE_ENEMIES_REORDERING = (state, encounter, order) -> {
        int start = encounter.idxes.get(0).index();
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
    };

    public static final EnemyReordering FIRST_AND_LAST_REORDERING = (state, encounter, order) -> {
        int start = encounter.idxes.get(0).index();
        if (state.getEnemiesForRead().get(start).getHealth() > state.getEnemiesForRead().get(start + 2).getHealth()) {
            order[start] = start + 2;
            order[start + 2] = start;
        }
    };

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
            newState.currentEncounter = state.currentEncounter.clone(PredefinedEncounter.CORRUPT_HEART);
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
