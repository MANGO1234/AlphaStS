package com.alphaStS;

public record PlayerBuff(long mask, String name) {
    public final static PlayerBuff BARRICADE = new PlayerBuff(1, "Barricade");
    public final static PlayerBuff CORRUPTION = new PlayerBuff(1L << 1, "Corruption");

    public final static PlayerBuff AKABEKO = new PlayerBuff(1L << 32, "Akabeko");
    public final static PlayerBuff ART_OF_WAR = new PlayerBuff(1L << 33, "Art of War");
    public final static PlayerBuff CENTENNIAL_PUZZLE = new PlayerBuff(1L << 34, "Centennial Puzzle");
    public final static PlayerBuff NECRONOMICON = new PlayerBuff(1L << 35, "Necronomicon");
    public final static PlayerBuff BLASPHEMY = new PlayerBuff(1L << 36, "Blasphemy");
    public final static PlayerBuff END_TURN_IMMEDIATELY = new PlayerBuff(1L << 37, "End Turn Immediately");
    public final static PlayerBuff USED_VAULT = new PlayerBuff(1L << 38, "Used Vault");

    public final static PlayerBuff[] BUFFS = new PlayerBuff[] {
            BARRICADE,
            CORRUPTION,
            AKABEKO,
            ART_OF_WAR,
            CENTENNIAL_PUZZLE,
            NECRONOMICON,
            BLASPHEMY,
            END_TURN_IMMEDIATELY,
            USED_VAULT,
    };
}
