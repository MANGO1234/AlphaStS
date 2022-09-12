package com.alphaStS.enums;

public enum OrbType {
    EMPTY("E", "Empty"), LIGHTNING("L", "Lightning"), FROST("F", "Frost"), DARK("D", "Dark"), PLASMA("P", "Plasma");

    public final String abbrev;
    public final String displayName;
    OrbType(String abbrev, String displayName) {
        this.abbrev = abbrev;
        this.displayName = displayName;
    }
}
