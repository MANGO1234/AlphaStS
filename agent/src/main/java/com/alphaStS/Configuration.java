package com.alphaStS;

public class Configuration {
    public static boolean DO_NOT_USE_CACHED_STATE_WHEN_MAKING_REAL_MOVE = false;

    public static float TRAINING_PERCENTAGE_NO_TEMPERATURE = 0.2f;
    public static boolean TRAINING_USE_FORCED_PLAYOUT = true;
    public static boolean TRAINING_EXPERIMENT_USE_UNCERTAINTY_FOR_EXPLORATION = false;
    public static boolean CARD_IN_HAND_IN_NN_INPUT = true;
    public static boolean CARD_IN_DECK_IN_NN_INPUT = true;

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
    public static boolean PROGRESSIVE_WIDENING_IMPROVEMENTS = true;
    public static boolean PROGRESSIVE_WIDENING_IMPROVEMENTS2 = false;
    public static boolean TRANSPOSITION_ACROSS_CHANCE_NODE = true;
    public static boolean TEST_TRANSPOSITION_ACROSS_CHANCE_NODE = false;
    public static boolean NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION = false;
    public static boolean TEST_NEW_COMMON_RANOM_NUMBER_VARIANCE_REDUCTION = true;
    public static boolean UPDATE_TRANSPOSITIONS_ON_ALL_PATH = false;
    public static boolean TEST_UPDATE_TRANSPOSITIONS_ON_ALL_PATH = false;
    public static boolean TRAINING_RESCORE_SEARCH_FOR_BEST_LINE = true;
    public static boolean TRAINING_SKIP_OPENING_TURNS = false;
    public static boolean COMBINE_END_AND_BEGIN_TURN_FOR_STOCHASTIC_BEGIN = true;
    public static int TRAINING_SKIP_OPENING_TURNS_UPTO = 3;
    public static double TRAINING_SKIP_OPENING_GAMES_INCREASE_RATIO = 1.25;

    public static boolean ADD_BEGIN_TURN_CTX_TO_NN_INPUT = true;
    public static boolean TRAINING_POLICY_SURPRISE_WEIGHTING = false;
    public static boolean USE_FIGHT_PROGRESS_WHEN_LOSING = true;
    public static boolean USE_Z_TRAINING = false;
    public static double DISCOUNT_REWARD_ON_RANDOM_NODE = 0.2f;

    public static boolean TRANSPOSITION_ALWAYS_EXPAND_NEW_NODE = false;
    public static boolean TEST_TRANSPOSITION_ALWAYS_EXPAND_NEW_NODE = false;

    public static boolean isTranspositionAlwaysExpandNewNodeOn(GameState state) {
        return TRANSPOSITION_ALWAYS_EXPAND_NEW_NODE && (!TEST_TRANSPOSITION_ALWAYS_EXPAND_NEW_NODE || state.prop.testNewFeature);
    }

    public static boolean USE_UTILITY_STD_ERR_FOR_PUCT = false;
    public static boolean TEST_USE_UTILITY_STD_ERR_FOR_PUCT = false;

    public static boolean isUseUtilityStdErrForPuctOn(GameState state) {
        return isTranspositionAlwaysExpandNewNodeOn(state) && USE_UTILITY_STD_ERR_FOR_PUCT && (!TEST_USE_UTILITY_STD_ERR_FOR_PUCT || state.prop.testNewFeature);
    }

    public static boolean USE_DMG_DISTRIBUTION = false;
    public static boolean TEST_USE_DMG_DISTRIBUTION = false;

    public static boolean isUseDmgDistributionOn(GameState state) {
        return USE_DMG_DISTRIBUTION && (!TEST_USE_DMG_DISTRIBUTION || state.prop.testNewFeature);
    }

    // having this help network know when it's almost losing due to 50 turns losing rule
    // basic testing show it helps a bit in preventing losing to 50 turns
    // combine with USE_TURNS_LEFT_HEAD below for maximum effect
    public static boolean ADD_CURRENT_TURN_NUM_TO_NN_INPUT = true;

    // predict how many turns remains in the battle (regardless of win or loss), use it to select the move that
    // ends the battle faster among moves with similar evaluation
    // basic testing show it significantly helps in preventing losing to 50 turns and dramatically shorten number of turns
    // (usually in defect battles with lots of focus where the network stop progressing due to every move being the same eval)
    public static boolean USE_TURNS_LEFT_HEAD = true;


    public static boolean USE_NEW_ACTION_SELECTION = false;

    public static final String ONNX_LIB_PATH = "F:/git/lib";
    public static final boolean ONNX_USE_CUDA_FOR_INFERENCE = false;
}
