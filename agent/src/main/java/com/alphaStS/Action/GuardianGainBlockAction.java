package com.alphaStS.action;

import com.alphaStS.GameState;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyReadOnly;

public class GuardianGainBlockAction implements GameEnvironmentAction {
    public static GuardianGainBlockAction singleton = new GuardianGainBlockAction();

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        return true;
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public void doAction(GameState state) {
        for (EnemyReadOnly enemy : state.getEnemiesForWrite().iterateOverAlive()) {
            if (enemy instanceof Enemy.TheGuardian e) {
                e.gainBlock(20);
            }
        }
    }
}
