package com.alphaStS;

public class Configuration {
    public static boolean DO_NOT_USE_CACHED_STATE_WHEN_MAKING_REAL_MOVE = false;

    // During training, percentage of games played with no temperature
    public static float TRAINING_PERCENTAGE_NO_TEMPERATURE = 0.2f;
    // During training, enable KataGo's forced playout
    public static boolean TRAINING_USE_FORCED_PLAYOUT = true;
    public static boolean TRAINING_EXPERIMENT_USE_UNCERTAINTY_FOR_EXPLORATION = false;
    public static boolean TRAIN_ONLY_ON_NON_TEMP_CONTAMINATED_VALUES = false;
    public static boolean CARD_IN_HAND_IN_NN_INPUT = true;
    public static boolean CARD_IN_DECK_IN_NN_INPUT = true;

    public static float PRE_BATTLE_SCENARIO_TEMP = 0f;

    public static boolean PRINT_MODEL_COMPARE_DIFF = false;
    public static int CMP_DEVIATION_NUM_RERUN = 0;
    public static int SLEEP_PER_GAME = 0;
    public static int SLEEP_PER_GAME_TRAINING = 0;
    public static boolean USE_REMOTE_SERVERS = true;

    public static boolean COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION = true;
    public static boolean CPUCT_SCALING = false;
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
        return TRANSPOSITION_ALWAYS_EXPAND_NEW_NODE && (!TEST_TRANSPOSITION_ALWAYS_EXPAND_NEW_NODE || state.properties.testNewFeature);
    }

    public static boolean isTranspositionAcrossChanceNodeOn(GameState state) {
        return TRANSPOSITION_ACROSS_CHANCE_NODE && (!TEST_TRANSPOSITION_ACROSS_CHANCE_NODE || state.properties.testNewFeature);
    }

    public static boolean USE_UTILITY_STD_ERR_FOR_PUCT = false;
    public static boolean TEST_USE_UTILITY_STD_ERR_FOR_PUCT = false;

    public static boolean isUseUtilityStdErrForPuctOn(GameState state) {
        return isTranspositionAlwaysExpandNewNodeOn(state) && USE_UTILITY_STD_ERR_FOR_PUCT && (!TEST_USE_UTILITY_STD_ERR_FOR_PUCT || state.properties.testNewFeature);
    }

    public static boolean BAN_TRANSPOSITION_IN_TREE = false;
    public static boolean TEST_BAN_TRANSPOSITION_IN_TREE = false;

    public static boolean isBanTranspositionInTreeOn(GameState state) {
        return BAN_TRANSPOSITION_IN_TREE && (!TEST_BAN_TRANSPOSITION_IN_TREE || state.properties.testNewFeature);
    }

    public static boolean PRIORITIZE_CHANCE_NODES_BEFORE_DETERMINISTIC_IN_TREE = true;
    public static boolean TEST_PRIORITIZE_CHANCE_NODES_BEFORE_DETERMINISTIC_IN_TREE = false;

    public static boolean isPrioritizeChanceNodesBeforeDeterministicInTreeOn(GameState state) {
        return PRIORITIZE_CHANCE_NODES_BEFORE_DETERMINISTIC_IN_TREE && (!TEST_PRIORITIZE_CHANCE_NODES_BEFORE_DETERMINISTIC_IN_TREE || state.properties.testNewFeature);
    }

    public static boolean FLATTEN_POLICY_AS_NODES_INCREASE = false;
    public static boolean TEST_FLATTEN_POLICY_AS_NODES_INCREASE = false;

    public static boolean isFlattenPolicyAsNodesIncreaseOn(GameState state) {
        return FLATTEN_POLICY_AS_NODES_INCREASE && (!TEST_FLATTEN_POLICY_AS_NODES_INCREASE || state.properties.testNewFeature);
    }

    public static boolean HEART_GAUNTLET_POTION_REWARD = false;
    public static boolean HEART_GAUNTLET_CARD_REWARD = false;

    // having this help network know when it's almost losing due to 50 turns losing rule
    // basic testing show it helps a bit in preventing losing to 50 turns
    // combine with USE_TURNS_LEFT_HEAD below for maximum effect
    public static boolean ADD_CURRENT_TURN_NUM_TO_NN_INPUT = true;

    // predict how many turns remains in the battle (regardless of win or loss), use it to select the move that
    // ends the battle faster among moves with similar evaluation
    // basic testing show it significantly helps in preventing losing to 50 turns and dramatically shorten number of turns
    // (usually in defect battles with lots of focus where the network stop progressing due to every move being the same eval)
    // disable unless needed for now, some testing shows it will cause the network to lose fights it wouldn't have lost by picking
    // more "aggressive" moves to do damage
    public static boolean USE_TURNS_LEFT_HEAD = false;

    // print model prediction error compare to actual result
    public static boolean STATS_PRINT_PREDICTION_ERRORS = false;
    public static boolean STATS_PRINT_CARD_USAGE_COUNT = false;

    public static boolean USE_NEW_ACTION_SELECTION = false;

    public static final String ONNX_LIB_PATH = "F:/git/lib";
    public static final boolean ONNX_USE_CUDA_FOR_INFERENCE = false;
}
