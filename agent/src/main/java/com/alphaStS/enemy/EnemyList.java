package com.alphaStS.enemy;

import java.util.Collection;
import java.util.Iterator;

public class EnemyList extends EnemyListReadOnly {
    protected long cloned;

    public EnemyList(EnemyList enemies) {
        super(enemies);
    }

    public EnemyList(Collection<Enemy> enemies) {
        super(enemies);
    }

    public void replace(int i, Enemy enemy) {
        cloned |= (1L << i);
        enemies[i] = enemy.copy();
    }

    public Enemy getForWrite(int i) {
        if ((cloned & (1L << i)) == 0) {
            enemies[i] = enemies[i].copy();
            cloned |= (1L << i);
        }
        return enemies[i];
    }

    public EnemyListAliveIterable iterateOverAlive() {
        return new EnemyListAliveIterable(this);
    }

    private static class EnemyListAliveIterator implements Iterator<Enemy> {
        EnemyList enemies;
        int i = 0;

        protected EnemyListAliveIterator(EnemyList enemies) {
            this.enemies = enemies;
        }

        @Override public boolean hasNext() {
            while (i < enemies.enemies.length) {
                if (enemies.enemies[i].isAlive()) {
                    return true;
                }
                i++;
            }
            return false;
        }

        @Override public Enemy next() {
            return enemies.getForWrite(i++);
        }
    }

    public static class EnemyListAliveIterable implements Iterable<Enemy> {
        EnemyList enemies;

        public EnemyListAliveIterable(EnemyList enemies) {
            this.enemies = enemies;
        }

        @Override public Iterator<Enemy> iterator() {
            return new EnemyListAliveIterator(enemies);
        }
    }

    public EnemyListAllIterable iterateOverAll() {
        return new EnemyListAllIterable(this);
    }

    private static class EnemyListAllIterator implements Iterator<Enemy> {
        EnemyList enemies;
        int i = 0;

        protected EnemyListAllIterator(EnemyList enemies) {
            this.enemies = enemies;
        }

        @Override public boolean hasNext() {
            return i < enemies.enemies.length;
        }

        @Override public Enemy next() {
            return enemies.getForWrite(i++);
        }
    }

    public static class EnemyListAllIterable implements Iterable<Enemy> {
        EnemyList enemies;

        public EnemyListAllIterable(EnemyList enemies) {
            this.enemies = enemies;
        }

        @Override public Iterator<Enemy> iterator() {
            return new EnemyListAllIterator(enemies);
        }
    }
}
