package com.alphaStS.enums;

import com.alphaStS.GameState;
import com.alphaStS.random.RandomGenCtx;

public enum OrbType {
    EMPTY("E", "Empty", (short) 1),
    LIGHTNING("L", "Lightning", (short) 2),
    FROST("F", "Frost", (short) 4),
    DARK("D", "Dark", (short) 8),
    PLASMA("P", "Plasma", (short) 16);

    public final String abbrev;
    public final String displayName;
    public final short mask;
    OrbType(String abbrev, String displayName, short mask) {
        this.abbrev = abbrev;
        this.displayName = displayName;
        this.mask = mask;
    }

    public static OrbType getRandom(GameState state) {
        state.setIsStochastic();
        var values = OrbType.values();
        int r = state.getSearchRandomGen().nextInt(values.length - 1, RandomGenCtx.Chaos, state) + 1;
        return values()[r];
    }
}
