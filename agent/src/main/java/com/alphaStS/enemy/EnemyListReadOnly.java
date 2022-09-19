package com.alphaStS.enemy;

import com.alphaStS.GameState;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class EnemyListReadOnly implements Iterable<EnemyReadOnly> {
    protected Enemy[] enemies;

    public EnemyListReadOnly(Enemy... enemies) {
        this.enemies = enemies;
    }

    public EnemyListReadOnly(Collection<Enemy> enemies) {
        this.enemies = enemies.toArray(new Enemy[0]);
    }

    public EnemyListReadOnly(EnemyListReadOnly enemyList) {
        this.enemies = Arrays.copyOf(enemyList.enemies, enemyList.enemies.length);
    }

    public EnemyReadOnly get(int i) {
        return enemies[i];
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EnemyListReadOnly that = (EnemyListReadOnly) o;
        return Arrays.equals(enemies, that.enemies);
    }

    @Override public int hashCode() {
        return Arrays.hashCode(enemies);
    }

    public int size() {
        return enemies.length;
    }

    public int find(EnemyReadOnly enemy) {
        for (int i = 0; i < enemies.length; i++) {
            if (enemies[i] instanceof Enemy.MergedEnemy m) {
                for (int j = 0; j < m.possibleEnemies.size(); j++) {
                    if (m.possibleEnemies.get(j).equals(enemy)) {
                        return i;
                    }
                }
            } else if (enemies[i].equals(enemy)) {
                return i;
            }
        }
        return -1;
    }

    @Override public String toString() {
        return Arrays.toString(enemies);
    }

    private static class EnemyListIterator implements Iterator<EnemyReadOnly> {
        Enemy[] enemies;
        int i = 0;

        protected EnemyListIterator(Enemy[] enemies) {
            this.enemies = enemies;
        }

        @Override public boolean hasNext() {
            return i < enemies.length;
        }

        @Override public EnemyReadOnly next() {
            return enemies[i++];
        }
    }

    @Override public Iterator<EnemyReadOnly> iterator() {
        return new EnemyListReadOnly.EnemyListIterator(enemies);
    }
}

