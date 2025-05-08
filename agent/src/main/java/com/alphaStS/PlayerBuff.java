package com.alphaStS;

public record PlayerBuff(long mask, String name) {
    public final static PlayerBuff BARRICADE = new PlayerBuff(1, "Barricade");
    public final static PlayerBuff CORRUPTION = new PlayerBuff(1L << 1, "Corruption");

    public final static PlayerBuff AKABEKO = new PlayerBuff(1L << 32, "Akabeko");
    public final static PlayerBuff ART_OF_WAR = new PlayerBuff(1L << 33, "Art of War");
    public final static PlayerBuff CENTENNIAL_PUZZLE = new PlayerBuff(1L << 34, "Centennial Puzzle");
    public final static PlayerBuff NECRONOMICON = new PlayerBuff(1L << 35, "Necronomicon");

    public final static PlayerBuff[] BUFFS = new PlayerBuff[] {
            BARRICADE,
            CORRUPTION,
            AKABEKO,
            ART_OF_WAR,
            CENTENNIAL_PUZZLE,
            NECRONOMICON,
    };
}
