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
    public static final boolean CARD_IN_HAND_IN_NN_INPUT = true;

    public static final float UTIL_FOR_RITUAL_DAGGER = 0.4f;
    public static final float PRE_BATTLE_SCENARIO_TEMP = 0f;

    public static final boolean PRINT_MODEL_COMPARE_DIFF = false;
    public static final int CMP_DEVIATION_NUM_RERUN = 3;
    public static final int SLEEP_PER_GAME = 0;
    public static final int SLEEP_PER_GAME_TRAINING = 0;
    public static final boolean USE_REMOTE_SERVERS = true;

    public static final boolean USE_PROGRESSIVE_WIDENING = false;
    public static final boolean TEST_PROGRESSIVE_WIDENING = true;
    public static final boolean TRANSPOSITION_ACROSS_CHANCE_NODE = false;
    public static final boolean TEST_TRANSPOSITION_ACROSS_CHANCE_NODE = true;
    public static final boolean NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION = false;
    public static final boolean TEST_NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION = true;
    public static final boolean UPDATE_TRANSPOSITIONS_ON_ALL_PATH = false;
    public static final boolean TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH = false;
    public static final boolean TRAINING_RESCORE_SEARCH_FOR_BEST_LINE = false;
}
