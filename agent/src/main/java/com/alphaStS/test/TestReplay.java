package com.alphaStS.test;

import com.alphaStS.GameState;
import com.alphaStS.card.Card;
import com.alphaStS.enemy.EnemyReadOnly;
import com.alphaStS.player.PlayerReadOnly;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class TestReplay {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Called from {@link GameState#reshuffle()} when {@code properties.testingReplayMode} is true.
     * Pops the next {@link TestReplayEvent.ShuffleEvent} from {@code properties.replayEventQueue}
     * and sets {@code deckArr} to that fixed order, ensuring cards are drawn in the exact sequence
     * recorded in the log.
     *
     * @return true if the queue had a ShuffleEvent and the deck was set, false if the queue was empty
     */
    public static boolean applyShuffleFromQueue(GameState state) {
        Queue<TestReplayEvent> queue = state.properties.replayEventQueue;
        if (queue == null || queue.isEmpty()) {
            return false;
        }
        TestReplayEvent event = queue.poll();
        if (!(event instanceof TestReplayEvent.ShuffleEvent shuffleEvent)) {
            throw new IllegalStateException("Expected ShuffleEvent but got " + event.getClass().getSimpleName());
        }
        String[] deckOrder = shuffleEvent.deckOrder;
        state.deckArrLen = 0;
        for (String cardName : deckOrder) {
            for (int i = 0; i < state.properties.cardDict.length; i++) {
                if (state.properties.cardDict[i].cardName.equals(cardName)) {
                    state.getDeckArrForWrite()[state.deckArrLen++] = (short) i;
                    break;
                }
            }
        }
        state.deckArrFixedDrawLen = state.deckArrLen;
        return true;
    }

    /**
     * Compares a {@code state:floor} log entry against the current {@link GameState} and syncs
     * the card piles from the log.
     *
     * <p>Strictly compared (throws {@link ReplayException} on mismatch): player energy, player
     * block, each monster's current HP, and the exhaust pile. Player and enemy detail fields
     * (strength, debuffs, move, etc.) are structurally compared but use hardcoded log values of 0
     * until the runlogger logs those fields.
     *
     * <p>Synced from log without comparing: hand, draw pile, discard pile. STS draws randomly
     * while the simulation uses a different RNG, so draw order diverges between turns. Syncing
     * these piles ensures each action is applied to the exact hand the log recorded.
     *
     * @param stateFloorJson the raw JSON string of a {@code state:floor} log entry
     * @param state          the current game state to compare and sync against
     * @throws ReplayException if any strictly-compared field does not match the log
     */
    public static void compareStateFloor(String stateFloorJson, GameState state) throws ReplayException {
        JsonNode root;
        try {
            root = MAPPER.readTree(stateFloorJson);
        } catch (Exception e) {
            throw new ReplayException("Failed to parse state:floor JSON: " + e.getMessage(), state, stateFloorJson);
        }

        JsonNode combatState = root.path("combat_state");
        Card[] cardDict = state.properties.cardDict;

        int logEnergy = combatState.path("player").path("energy").asInt(-1);
        if (logEnergy >= 0 && logEnergy != state.energy) {
            throw new ReplayException(
                "Energy mismatch: log=" + logEnergy + " state=" + state.energy, state, stateFloorJson);
        }

        int logBlock = combatState.path("player").path("block").asInt(0);
        int stateBlock = state.getPlayerForRead().getBlock();
        if (logBlock != stateBlock) {
            throw new ReplayException(
                "Block mismatch: log=" + logBlock + " state=" + stateBlock, state, stateFloorJson);
        }

        comparePlayerState(combatState.path("player"), state.getPlayerForRead(), state, stateFloorJson);

        JsonNode monsters = combatState.path("monsters");
        for (int i = 0; i < monsters.size(); i++) {
            int logHp = monsters.get(i).path("hp_current").asInt(-1);
            if (logHp >= 0) {
                int stateHp = state.getEnemiesForRead().get(i).getHealth();
                if (logHp != stateHp) {
                    throw new ReplayException(
                        "Monster[" + i + "] HP mismatch: log=" + logHp + " state=" + stateHp,
                        state, stateFloorJson);
                }
            }
            compareEnemyState(monsters.get(i), state.getEnemiesForRead().get(i), i, state, stateFloorJson);
        }

        compareCardList("exhaust_pile", combatState.path("exhaust_pile"),
            state.exhaustArr, state.exhaustArrLen, cardDict, state, stateFloorJson);

        state.handArr = buildCardArr(combatState.path("hand"), cardDict);
        state.handArrLen = combatState.path("hand").size();

        state.deckArr = buildCardArr(combatState.path("draw_pile"), cardDict);
        state.deckArrLen = combatState.path("draw_pile").size();
        state.deckArrFixedDrawLen = state.deckArrLen;

        state.discardArr = buildCardArr(combatState.path("discard_pile"), cardDict);
        state.discardArrLen = combatState.path("discard_pile").size();
    }

    // Compares player detail fields against the log. All log values are hardcoded to 0 until
    // the runlogger emits them; the guard (logX != 0) makes each check a no-op until then.
    private static void comparePlayerState(JsonNode playerNode, PlayerReadOnly player,
            GameState state, String line) throws ReplayException {
        int logOrigHealth = 0;
        if (logOrigHealth != 0 && logOrigHealth != player.getOrigHealth()) {
            throw new ReplayException(
                "Player origHealth mismatch: log=" + logOrigHealth + " state=" + player.getOrigHealth(), state, line);
        }

        int logInBattleMaxHealth = 0;
        if (logInBattleMaxHealth != 0 && logInBattleMaxHealth != player.getInBattleMaxHealth()) {
            throw new ReplayException(
                "Player inBattleMaxHealth mismatch: log=" + logInBattleMaxHealth + " state=" + player.getInBattleMaxHealth(), state, line);
        }

        int logMaxHealth = 0;
        if (logMaxHealth != 0 && logMaxHealth != player.getMaxHealth()) {
            throw new ReplayException(
                "Player maxHealth mismatch: log=" + logMaxHealth + " state=" + player.getMaxHealth(), state, line);
        }

        int logHealth = 0;
        if (logHealth != 0 && logHealth != player.getHealth()) {
            throw new ReplayException(
                "Player health mismatch: log=" + logHealth + " state=" + player.getHealth(), state, line);
        }

        int logStrength = 0;
        if (logStrength != 0 && logStrength != player.getStrength()) {
            throw new ReplayException(
                "Player strength mismatch: log=" + logStrength + " state=" + player.getStrength(), state, line);
        }

        int logDexterity = 0;
        if (logDexterity != 0 && logDexterity != player.getDexterity()) {
            throw new ReplayException(
                "Player dexterity mismatch: log=" + logDexterity + " state=" + player.getDexterity(), state, line);
        }

        int logVulnerable = 0;
        if (logVulnerable != 0 && logVulnerable != player.getVulnerable()) {
            throw new ReplayException(
                "Player vulnerable mismatch: log=" + logVulnerable + " state=" + player.getVulnerable(), state, line);
        }

        int logWeak = 0;
        if (logWeak != 0 && logWeak != player.getWeak()) {
            throw new ReplayException(
                "Player weak mismatch: log=" + logWeak + " state=" + player.getWeak(), state, line);
        }

        int logFrail = 0;
        if (logFrail != 0 && logFrail != player.getFrail()) {
            throw new ReplayException(
                "Player frail mismatch: log=" + logFrail + " state=" + player.getFrail(), state, line);
        }

        int logArtifact = 0;
        if (logArtifact != 0 && logArtifact != player.getArtifact()) {
            throw new ReplayException(
                "Player artifact mismatch: log=" + logArtifact + " state=" + player.getArtifact(), state, line);
        }

        // cannotDrawCard and hexed are booleans; comparison deferred until the runlogger logs them.

        int logEntangled = 0;
        if (logEntangled != 0 && logEntangled != player.getEntangled()) {
            throw new ReplayException(
                "Player entangled mismatch: log=" + logEntangled + " state=" + player.getEntangled(), state, line);
        }

        int logLoseStrengthEot = 0;
        if (logLoseStrengthEot != 0 && logLoseStrengthEot != player.getLoseStrengthEot()) {
            throw new ReplayException(
                "Player loseStrengthEot mismatch: log=" + logLoseStrengthEot + " state=" + player.getLoseStrengthEot(), state, line);
        }

        int logLoseDexterityEot = 0;
        if (logLoseDexterityEot != 0 && logLoseDexterityEot != player.getLoseDexterityEot()) {
            throw new ReplayException(
                "Player loseDexterityEot mismatch: log=" + logLoseDexterityEot + " state=" + player.getLoseDexterityEot(), state, line);
        }

        int logLoseFocusEot = 0;
        if (logLoseFocusEot != 0 && logLoseFocusEot != player.getLoseFocusEot()) {
            throw new ReplayException(
                "Player loseFocusEot mismatch: log=" + logLoseFocusEot + " state=" + player.getLoseFocusEot(), state, line);
        }

        int logPlatedArmor = 0;
        if (logPlatedArmor != 0 && logPlatedArmor != player.getPlatedArmor()) {
            throw new ReplayException(
                "Player platedArmor mismatch: log=" + logPlatedArmor + " state=" + player.getPlatedArmor(), state, line);
        }

        int logNoMoreBlockFromCards = 0;
        if (logNoMoreBlockFromCards != 0 && logNoMoreBlockFromCards != player.getNoMoreBlockFromCards()) {
            throw new ReplayException(
                "Player noMoreBlockFromCards mismatch: log=" + logNoMoreBlockFromCards + " state=" + player.getNoMoreBlockFromCards(), state, line);
        }

        int logAccumulatedDamage = 0;
        if (logAccumulatedDamage != 0 && logAccumulatedDamage != player.getAccumulatedDamage()) {
            throw new ReplayException(
                "Player accumulatedDamage mismatch: log=" + logAccumulatedDamage + " state=" + player.getAccumulatedDamage(), state, line);
        }
    }

    // Compares enemy detail fields against the log. All log values are hardcoded to 0 until
    // the runlogger emits them; the guard (logX != 0) makes each check a no-op until then.
    private static void compareEnemyState(JsonNode monsterNode, EnemyReadOnly enemy, int idx,
            GameState state, String line) throws ReplayException {
        int logMaxHealthInBattle = 0;
        if (logMaxHealthInBattle != 0 && logMaxHealthInBattle != enemy.getMaxHealthInBattle()) {
            throw new ReplayException(
                "Monster[" + idx + "] maxHealthInBattle mismatch: log=" + logMaxHealthInBattle + " state=" + enemy.getMaxHealthInBattle(), state, line);
        }

        int logBlock = 0;
        if (logBlock != 0 && logBlock != enemy.getBlock()) {
            throw new ReplayException(
                "Monster[" + idx + "] block mismatch: log=" + logBlock + " state=" + enemy.getBlock(), state, line);
        }

        int logStrength = 0;
        if (logStrength != 0 && logStrength != enemy.getStrength()) {
            throw new ReplayException(
                "Monster[" + idx + "] strength mismatch: log=" + logStrength + " state=" + enemy.getStrength(), state, line);
        }

        int logVulnerable = 0;
        if (logVulnerable != 0 && logVulnerable != enemy.getVulnerable()) {
            throw new ReplayException(
                "Monster[" + idx + "] vulnerable mismatch: log=" + logVulnerable + " state=" + enemy.getVulnerable(), state, line);
        }

        int logWeak = 0;
        if (logWeak != 0 && logWeak != enemy.getWeak()) {
            throw new ReplayException(
                "Monster[" + idx + "] weak mismatch: log=" + logWeak + " state=" + enemy.getWeak(), state, line);
        }

        int logArtifact = 0;
        if (logArtifact != 0 && logArtifact != enemy.getArtifact()) {
            throw new ReplayException(
                "Monster[" + idx + "] artifact mismatch: log=" + logArtifact + " state=" + enemy.getArtifact(), state, line);
        }

        int logPoison = 0;
        if (logPoison != 0 && logPoison != enemy.getPoison()) {
            throw new ReplayException(
                "Monster[" + idx + "] poison mismatch: log=" + logPoison + " state=" + enemy.getPoison(), state, line);
        }

        int logRegeneration = 0;
        if (logRegeneration != 0 && logRegeneration != enemy.getRegeneration()) {
            throw new ReplayException(
                "Monster[" + idx + "] regeneration mismatch: log=" + logRegeneration + " state=" + enemy.getRegeneration(), state, line);
        }

        int logMetallicize = 0;
        if (logMetallicize != 0 && logMetallicize != enemy.getMetallicize()) {
            throw new ReplayException(
                "Monster[" + idx + "] metallicize mismatch: log=" + logMetallicize + " state=" + enemy.getMetallicize(), state, line);
        }

        int logPlatedArmor = 0;
        if (logPlatedArmor != 0 && logPlatedArmor != enemy.getPlatedArmor()) {
            throw new ReplayException(
                "Monster[" + idx + "] platedArmor mismatch: log=" + logPlatedArmor + " state=" + enemy.getPlatedArmor(), state, line);
        }

        int logLoseStrengthEot = 0;
        if (logLoseStrengthEot != 0 && logLoseStrengthEot != enemy.getLoseStrengthEot()) {
            throw new ReplayException(
                "Monster[" + idx + "] loseStrengthEot mismatch: log=" + logLoseStrengthEot + " state=" + enemy.getLoseStrengthEot(), state, line);
        }

        int logCorpseExplosion = 0;
        if (logCorpseExplosion != 0 && logCorpseExplosion != enemy.getCorpseExplosion()) {
            throw new ReplayException(
                "Monster[" + idx + "] corpseExplosion mismatch: log=" + logCorpseExplosion + " state=" + enemy.getCorpseExplosion(), state, line);
        }

        int logChoke = 0;
        if (logChoke != 0 && logChoke != enemy.getChoke()) {
            throw new ReplayException(
                "Monster[" + idx + "] choke mismatch: log=" + logChoke + " state=" + enemy.getChoke(), state, line);
        }

        int logLockOn = 0;
        if (logLockOn != 0 && logLockOn != enemy.getLockOn()) {
            throw new ReplayException(
                "Monster[" + idx + "] lockOn mismatch: log=" + logLockOn + " state=" + enemy.getLockOn(), state, line);
        }

        int logTalkToTheHand = 0;
        if (logTalkToTheHand != 0 && logTalkToTheHand != enemy.getTalkToTheHand()) {
            throw new ReplayException(
                "Monster[" + idx + "] talkToTheHand mismatch: log=" + logTalkToTheHand + " state=" + enemy.getTalkToTheHand(), state, line);
        }

        int logMark = 0;
        if (logMark != 0 && logMark != enemy.getMark()) {
            throw new ReplayException(
                "Monster[" + idx + "] mark mismatch: log=" + logMark + " state=" + enemy.getMark(), state, line);
        }

        int logDoom = 0;
        if (logDoom != 0 && logDoom != enemy.getDoom()) {
            throw new ReplayException(
                "Monster[" + idx + "] doom mismatch: log=" + logDoom + " state=" + enemy.getDoom(), state, line);
        }

        int logDebilitate = 0;
        if (logDebilitate != 0 && logDebilitate != enemy.getDebilitate()) {
            throw new ReplayException(
                "Monster[" + idx + "] debilitate mismatch: log=" + logDebilitate + " state=" + enemy.getDebilitate(), state, line);
        }

        int logSicEm = 0;
        if (logSicEm != 0 && logSicEm != enemy.getSicEm()) {
            throw new ReplayException(
                "Monster[" + idx + "] sicEm mismatch: log=" + logSicEm + " state=" + enemy.getSicEm(), state, line);
        }

        int logHang = 0;
        if (logHang != 0 && logHang != enemy.getHang()) {
            throw new ReplayException(
                "Monster[" + idx + "] hang mismatch: log=" + logHang + " state=" + enemy.getHang(), state, line);
        }

        int logDoomPerCard = 0;
        if (logDoomPerCard != 0 && logDoomPerCard != enemy.getDoomPerCard()) {
            throw new ReplayException(
                "Monster[" + idx + "] doomPerCard mismatch: log=" + logDoomPerCard + " state=" + enemy.getDoomPerCard(), state, line);
        }

        int logPowderedDemise = 0;
        if (logPowderedDemise != 0 && logPowderedDemise != enemy.getPowderedDemise()) {
            throw new ReplayException(
                "Monster[" + idx + "] powderedDemise mismatch: log=" + logPowderedDemise + " state=" + enemy.getPowderedDemise(), state, line);
        }

        int logBeetleJuice = 0;
        if (logBeetleJuice != 0 && logBeetleJuice != enemy.getBeetleJuice()) {
            throw new ReplayException(
                "Monster[" + idx + "] beetleJuice mismatch: log=" + logBeetleJuice + " state=" + enemy.getBeetleJuice(), state, line);
        }

        int logHitByAttack = 0;
        if (logHitByAttack != 0 && logHitByAttack != enemy.getHitByAttack()) {
            throw new ReplayException(
                "Monster[" + idx + "] hitByAttack mismatch: log=" + logHitByAttack + " state=" + enemy.getHitByAttack(), state, line);
        }

        int logMove = 0;
        if (logMove != 0 && logMove != enemy.getMove()) {
            throw new ReplayException(
                "Monster[" + idx + "] move mismatch: log=" + logMove + " state=" + enemy.getMove(), state, line);
        }
    }

    private static void compareCardList(
            String listName, JsonNode logCards,
            short[] stateArr, int stateArrLen,
            Card[] cardDict,
            GameState state, String line) throws ReplayException {
        Map<String, Integer> logCounts = new HashMap<>();
        for (JsonNode card : logCards) {
            logCounts.merge(card.asText(), 1, Integer::sum);
        }

        Map<String, Integer> stateCounts = new HashMap<>();
        for (int i = 0; i < stateArrLen; i++) {
            stateCounts.merge(cardDict[stateArr[i]].cardName, 1, Integer::sum);
        }

        if (!logCounts.equals(stateCounts)) {
            throw new ReplayException(
                listName + " mismatch: log=" + logCounts + " state=" + stateCounts, state, line);
        }
    }

    private static short[] buildCardArr(JsonNode cards, Card[] cardDict) {
        short[] arr = new short[cards.size() + 2];
        int idx = 0;
        for (JsonNode card : cards) {
            String cardName = card.asText();
            for (int i = 0; i < cardDict.length; i++) {
                if (cardDict[i].cardName.equals(cardName)) {
                    arr[idx++] = (short) i;
                    break;
                }
            }
        }
        return arr;
    }
}
