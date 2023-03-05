package com.alphaStS;

public class Configuration {
    public static boolean COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION = true;

    public static int TRAINING_GAME_COUNT = 200;
    public static int TRAINING_GAME_NODES = 100;

    public static boolean TRAINING_POLICY_CAP_ON = false;
    public static boolean TRAINING_TEMPERATURE_MOVES_ON = true;
    public static boolean TRAINING_RESCORE_TEMPERATURE_MOVE = true;
    public static float TRAINING_PERCENTAGE_NO_TEMPERATURE = 0.2f;
    public static boolean TRAINING_FORCED_PLAYOUT_ON = true;
    public static boolean CARD_IN_HAND_IN_NN_INPUT = true;
    public static boolean CARD_IN_DECK_IN_NN_INPUT = true;

    public static float UTIL_FOR_RITUAL_DAGGER = 0.4f;
    public static float PRE_BATTLE_SCENARIO_TEMP = 0f;

    public static boolean PRINT_MODEL_COMPARE_DIFF = false;
    public static int CMP_DEVIATION_NUM_RERUN = 3;
    public static int SLEEP_PER_GAME = 0;
    public static int SLEEP_PER_GAME_TRAINING = 0;
    public static boolean USE_REMOTE_SERVERS = true;

    public static boolean CPUCT_SCALING = true;
    public static boolean TEST_CPUCT_SCALING = false;
    public static boolean USE_PROGRESSIVE_WIDENING = false;
    public static boolean TEST_PROGRESSIVE_WIDENING = false;
    public static boolean TRANSPOSITION_ACROSS_CHANCE_NODE = true;
    public static boolean TEST_TRANSPOSITION_ACROSS_CHANCE_NODE = true;
    public static boolean NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION = false;
    public static boolean TEST_NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION = true;
    public static boolean UPDATE_TRANSPOSITIONS_ON_ALL_PATH = false;
    public static boolean TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH = false;
    public static boolean TRAINING_RESCORE_SEARCH_FOR_BEST_LINE = true;
    public static boolean TRAINING_SKIP_OPENING_TURNS = false;
    public static int TRAINING_SKIP_OPENING_TURNS_UPTO = 3;
    public static double TRAINING_SKIP_OPENING_GAMES_INCREASE_RATIO = 1.25;

    public static boolean ADD_BEGIN_TURN_CTX_TO_NN_INPUT = true;
    public static boolean TRAINING_POLICY_SURPRISE_WEIGHTING = false;
    public static boolean USE_FIGHT_PROGRESS_WHEN_LOSING = false;
}
