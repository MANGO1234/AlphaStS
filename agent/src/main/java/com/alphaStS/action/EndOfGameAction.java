package com.alphaStS.action;

import com.alphaStS.GameState;

public class EndOfGameAction implements GameEnvironmentAction {
    public static final EndOfGameAction instance = new EndOfGameAction();

    @Override public void doAction(GameState state) {
        for (var handler : state.properties.endOfBattleHandlers) {
            handler.handle(state);
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        return o != null && getClass() == o.getClass();
    }

    @Override public String toString() {
        return "EndOfGameAction";
    }
}
