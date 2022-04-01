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
                if (enemies.enemies[i].getHealth() > 0) {
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
}
