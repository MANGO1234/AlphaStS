package com.alphaStS;

public class Configuration {
    public static final boolean COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION = true;

    public static final int TRAINING_GAME_COUNT = 200;
    public static final int TRAINING_GAME_NODES = 100;

    public static final boolean TRAINING_POLICY_CAP_ON = false;
    public static final boolean TRAINING_TEMPERATURE_MOVES_ON = true;
    public static final boolean TRAINING_RESCORE_TEMPERATURE_MOVE = true;
    public static final float TRAINING_PERCENTAGE_NO_TEMPERATURE = 0.2f;
    public static final boolean TRAINING_FORCED_PLAYOUT_ON = true;
}