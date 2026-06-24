package com.alphaStS.enemy;

import com.alphaStS.GameState;
import com.alphaStS.random.RandomGen;
import com.alphaStS.random.RandomGenCtx;
import com.alphaStS.utils.Tuple;

public class EnemyUtils {
    public static boolean reviveFirstDeadEnemy(GameState state, Class<?> enemyClass) {
        for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
            if (!state.getEnemiesForRead().get(i).isAlive() && enemyClass.isInstance(state.getEnemiesForRead().get(i))) {
                state.reviveEnemy(i, false, -1);
                state.getEnemiesForWrite().getForWrite(i).reviveReset();
                return true;
            }
        }
        return false;
    }

    public static void randomizeHealth(Enemy enemy, GameState state, boolean training, int minHealth, int maxHealth) {
        int range = maxHealth - minHealth + 1;
        if (range <= 1) {
            enemy.setHealth(maxHealth);
            return;
        }
        int difficulty = Math.max(1, maxHealth / 20);
        int b = state.getSearchRandomGen().nextInt(difficulty, RandomGenCtx.Other) + 1;
        if (training) {
            enemy.setHealth((int) Math.round(((double) (enemy.getHealth() * b)) / 6));
        } else if (range > 1) {
            enemy.setHealth(minHealth + state.getSearchRandomGen().nextInt(range, RandomGenCtx.RandomEnemyHealth, new Tuple<>(minHealth, state)));
        } else {
            enemy.setHealth(maxHealth);
        }
    }

    public static int nextFixedMove(int move, int moveCount) {
        return move < 0 ? 0 : (move + 1) % moveCount;
    }

    public static int randomMove(RandomGen random, int... weights) {
        int total = 0;
        for (int weight : weights) {
            total += weight;
        }
        int r = random.nextInt(total, RandomGenCtx.EnemyChooseMove);
        int running = 0;
        for (int i = 0; i < weights.length; i++) {
            running += weights[i];
            if (r < running) {
                return i;
            }
        }
        return weights.length - 1;
    }
}
