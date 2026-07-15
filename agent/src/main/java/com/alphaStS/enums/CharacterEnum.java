package com.alphaStS.enums;

public enum CharacterEnum {
    IRONCLAD(false), SILENT(false), DEFECT(false), WATCHER(false),
    IRONCLAD2(true), SILENT2(true), DEFECT2(true), REGENT(true), NECROBINDER(true);

    public final boolean isSts2;

    CharacterEnum(boolean isSts2) {
        this.isSts2 = isSts2;
    }
}
