package com.alphaStS.enums;

import com.alphaStS.GameState;
import com.alphaStS.RandomGenCtx;

public enum OrbType {
    EMPTY("E", "Empty"), LIGHTNING("L", "Lightning"), FROST("F", "Frost"), DARK("D", "Dark"), PLASMA("P", "Plasma");

    public final String abbrev;
    public final String displayName;
    OrbType(String abbrev, String displayName) {
        this.abbrev = abbrev;
        this.displayName = displayName;
    }

    public static OrbType getRandom(GameState state) {
        state.setIsStochastic();
        var values = OrbType.values();
        int r = state.getSearchRandomGen().nextInt(values.length, RandomGenCtx.Chaos);
        return values()[r];
    }
}
