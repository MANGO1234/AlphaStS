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
     * block, player HP, each monster's current HP and powers, and the exhaust pile.
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
            throw new ReplayException("Energy mismatch: log=" + logEnergy + " state=" + state.energy, state, stateFloorJson);
        }

        int logBlock = combatState.path("player").path("block").asInt(0);
        int stateBlock = state.getPlayerForRead().getBlock();
        if (logBlock != stateBlock) {
            throw new ReplayException("Block mismatch: log=" + logBlock + " state=" + stateBlock, state, stateFloorJson);
        }

        comparePlayerState(combatState.path("player"), root, state.getPlayerForRead(), state, stateFloorJson);

        JsonNode monsters = combatState.path("monsters");
        for (int i = 0; i < monsters.size(); i++) {
            int logHp = monsters.get(i).path("hp_current").asInt(-1);
            if (logHp >= 0) {
                int stateHp = state.getEnemiesForRead().get(i).getHealth();
                if (logHp != stateHp) {
                    throw new ReplayException("Monster[" + i + "] HP mismatch: log=" + logHp + " state=" + stateHp,
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

    // Compares player fields; reads hp_current/hp_max from the state:floor root, other fields
    // from combat_state.player. Fields absent from the log default to 0/false so alphaStS is
    // always checked against the expected baseline.
    private static void comparePlayerState(JsonNode playerNode, JsonNode root, PlayerReadOnly player,
            GameState state, String line) throws ReplayException {
        int logInBattleMaxHealth = root.path("hp_max").asInt(-1);
        if (logInBattleMaxHealth >= 0 && logInBattleMaxHealth != player.getInBattleMaxHealth()) {
            throw new ReplayException("Player inBattleMaxHealth mismatch: log=" + logInBattleMaxHealth + " state=" + player.getInBattleMaxHealth(), state, line);
        }

        int logHealth = root.path("hp_current").asInt(-1);
        if (logHealth >= 0 && logHealth != player.getHealth()) {
            throw new ReplayException("Player health mismatch: log=" + logHealth + " state=" + player.getHealth(), state, line);
        }

        int logStrength = playerNode.path("strength").asInt(0);
        if (logStrength != player.getStrength()) {
            throw new ReplayException("Player strength mismatch: log=" + logStrength + " state=" + player.getStrength(), state, line);
        }

        int logDexterity = playerNode.path("dexterity").asInt(0);
        if (logDexterity != player.getDexterity()) {
            throw new ReplayException("Player dexterity mismatch: log=" + logDexterity + " state=" + player.getDexterity(), state, line);
        }

        int logVulnerable = playerNode.path("vulnerable").asInt(0);
        if (logVulnerable != player.getVulnerable()) {
            throw new ReplayException("Player vulnerable mismatch: log=" + logVulnerable + " state=" + player.getVulnerable(), state, line);
        }

        int logWeak = playerNode.path("weak").asInt(0);
        if (logWeak != player.getWeak()) {
            throw new ReplayException("Player weak mismatch: log=" + logWeak + " state=" + player.getWeak(), state, line);
        }

        int logFrail = playerNode.path("frail").asInt(0);
        if (logFrail != player.getFrail()) {
            throw new ReplayException("Player frail mismatch: log=" + logFrail + " state=" + player.getFrail(), state, line);
        }

        int logArtifact = playerNode.path("artifact").asInt(0);
        if (logArtifact != player.getArtifact()) {
            throw new ReplayException("Player artifact mismatch: log=" + logArtifact + " state=" + player.getArtifact(), state, line);
        }

        boolean logCannotDrawCard = playerNode.path("cannot_draw_card").asBoolean(false);
        if (logCannotDrawCard != player.cannotDrawCard()) {
            throw new ReplayException("Player cannotDrawCard mismatch: log=" + logCannotDrawCard + " state=" + player.cannotDrawCard(), state, line);
        }

        boolean logHexed = playerNode.path("hexed").asBoolean(false);
        if (logHexed != player.isHexed()) {
            throw new ReplayException("Player hexed mismatch: log=" + logHexed + " state=" + player.isHexed(), state, line);
        }

        int logEntangled = playerNode.path("entangled").asInt(0);
        if (logEntangled != player.getEntangled()) {
            throw new ReplayException("Player entangled mismatch: log=" + logEntangled + " state=" + player.getEntangled(), state, line);
        }

        int logLoseStrengthEot = playerNode.path("lose_strength_eot").asInt(0);
        if (logLoseStrengthEot != player.getLoseStrengthEot()) {
            throw new ReplayException("Player loseStrengthEot mismatch: log=" + logLoseStrengthEot + " state=" + player.getLoseStrengthEot(), state, line);
        }

        int logLoseDexterityEot = playerNode.path("lose_dexterity_eot").asInt(0);
        if (logLoseDexterityEot != player.getLoseDexterityEot()) {
            throw new ReplayException("Player loseDexterityEot mismatch: log=" + logLoseDexterityEot + " state=" + player.getLoseDexterityEot(), state, line);
        }

        int logLoseFocusEot = playerNode.path("lose_focus_eot").asInt(0);
        if (logLoseFocusEot != player.getLoseFocusEot()) {
            throw new ReplayException("Player loseFocusEot mismatch: log=" + logLoseFocusEot + " state=" + player.getLoseFocusEot(), state, line);
        }

        int logPlatedArmor = playerNode.path("plated_armor").asInt(0);
        if (logPlatedArmor != player.getPlatedArmor()) {
            throw new ReplayException("Player platedArmor mismatch: log=" + logPlatedArmor + " state=" + player.getPlatedArmor(), state, line);
        }

        int logNoMoreBlockFromCards = playerNode.path("no_more_block_from_cards").asInt(0);
        if (logNoMoreBlockFromCards != player.getNoMoreBlockFromCards()) {
            throw new ReplayException("Player noMoreBlockFromCards mismatch: log=" + logNoMoreBlockFromCards + " state=" + player.getNoMoreBlockFromCards(), state, line);
        }

        int logAccumulatedDamage = playerNode.path("accumulated_damage").asInt(0);
        if (logAccumulatedDamage != player.getAccumulatedDamage()) {
            throw new ReplayException("Player accumulatedDamage mismatch: log=" + logAccumulatedDamage + " state=" + player.getAccumulatedDamage(), state, line);
        }
    }

    // Extracts the amount for a named power from a monster's powers array; returns 0 if absent.
    private static int getPowerAmount(JsonNode monsterNode, String powerId) {
        JsonNode powers = monsterNode.path("powers");
        if (powers.isMissingNode() || !powers.isArray()) {
            return 0;
        }
        for (JsonNode power : powers) {
            if (powerId.equals(power.path("id").asText())) {
                return power.path("amount").asInt(0);
            }
        }
        return 0;
    }

    // Compares enemy fields; reads hp values directly from the monster node and power amounts
    // from the powers array. Fields not tracked in the log are compared against 0.
    private static void compareEnemyState(JsonNode monsterNode, EnemyReadOnly enemy, int idx,
            GameState state, String line) throws ReplayException {
        int logMaxHealthInBattle = monsterNode.path("hp_max").asInt(-1);
        if (logMaxHealthInBattle >= 0 && logMaxHealthInBattle != enemy.getMaxHealthInBattle()) {
            throw new ReplayException("Monster[" + idx + "] maxHealthInBattle mismatch: log=" + logMaxHealthInBattle + " state=" + enemy.getMaxHealthInBattle(), state, line);
        }

        int logHealth = monsterNode.path("hp_current").asInt(-1);
        if (logHealth >= 0 && logHealth != enemy.getHealth()) {
            throw new ReplayException("Monster[" + idx + "] health mismatch: log=" + logHealth + " state=" + enemy.getHealth(), state, line);
        }

        int logBlock = monsterNode.path("block").asInt(0);
        if (logBlock != enemy.getBlock()) {
            throw new ReplayException("Monster[" + idx + "] block mismatch: log=" + logBlock + " state=" + enemy.getBlock(), state, line);
        }

        int logStrength = getPowerAmount(monsterNode, "Strength");
        if (logStrength != enemy.getStrength()) {
            throw new ReplayException("Monster[" + idx + "] strength mismatch: log=" + logStrength + " state=" + enemy.getStrength(), state, line);
        }

        int logVulnerable = getPowerAmount(monsterNode, "Vulnerable");
        if (logVulnerable != enemy.getVulnerable()) {
            throw new ReplayException("Monster[" + idx + "] vulnerable mismatch: log=" + logVulnerable + " state=" + enemy.getVulnerable(), state, line);
        }

        int logWeak = getPowerAmount(monsterNode, "Weak");
        if (logWeak != enemy.getWeak()) {
            throw new ReplayException("Monster[" + idx + "] weak mismatch: log=" + logWeak + " state=" + enemy.getWeak(), state, line);
        }

        int logArtifact = getPowerAmount(monsterNode, "Artifact");
        if (logArtifact != enemy.getArtifact()) {
            throw new ReplayException("Monster[" + idx + "] artifact mismatch: log=" + logArtifact + " state=" + enemy.getArtifact(), state, line);
        }

        int logPoison = getPowerAmount(monsterNode, "Poison");
        if (logPoison != enemy.getPoison()) {
            throw new ReplayException("Monster[" + idx + "] poison mismatch: log=" + logPoison + " state=" + enemy.getPoison(), state, line);
        }

        int logRegeneration = getPowerAmount(monsterNode, "Regen");
        if (logRegeneration != enemy.getRegeneration()) {
            throw new ReplayException("Monster[" + idx + "] regeneration mismatch: log=" + logRegeneration + " state=" + enemy.getRegeneration(), state, line);
        }

        int logMetallicize = getPowerAmount(monsterNode, "Metallicize");
        if (logMetallicize != enemy.getMetallicize()) {
            throw new ReplayException("Monster[" + idx + "] metallicize mismatch: log=" + logMetallicize + " state=" + enemy.getMetallicize(), state, line);
        }

        int logPlatedArmor = getPowerAmount(monsterNode, "Plated Armor");
        if (logPlatedArmor != enemy.getPlatedArmor()) {
            throw new ReplayException("Monster[" + idx + "] platedArmor mismatch: log=" + logPlatedArmor + " state=" + enemy.getPlatedArmor(), state, line);
        }

        int logLoseStrengthEot = getPowerAmount(monsterNode, "Strength Down");
        if (logLoseStrengthEot != enemy.getLoseStrengthEot()) {
            throw new ReplayException("Monster[" + idx + "] loseStrengthEot mismatch: log=" + logLoseStrengthEot + " state=" + enemy.getLoseStrengthEot(), state, line);
        }

        int logCorpseExplosion = getPowerAmount(monsterNode, "Corpse Explosion");
        if (logCorpseExplosion != enemy.getCorpseExplosion()) {
            throw new ReplayException("Monster[" + idx + "] corpseExplosion mismatch: log=" + logCorpseExplosion + " state=" + enemy.getCorpseExplosion(), state, line);
        }

        int logChoke = getPowerAmount(monsterNode, "Choke");
        if (logChoke != enemy.getChoke()) {
            throw new ReplayException("Monster[" + idx + "] choke mismatch: log=" + logChoke + " state=" + enemy.getChoke(), state, line);
        }

        int logLockOn = getPowerAmount(monsterNode, "Lock On");
        if (logLockOn != enemy.getLockOn()) {
            throw new ReplayException("Monster[" + idx + "] lockOn mismatch: log=" + logLockOn + " state=" + enemy.getLockOn(), state, line);
        }

        int logTalkToTheHand = getPowerAmount(monsterNode, "Talk to the Hand");
        if (logTalkToTheHand != enemy.getTalkToTheHand()) {
            throw new ReplayException("Monster[" + idx + "] talkToTheHand mismatch: log=" + logTalkToTheHand + " state=" + enemy.getTalkToTheHand(), state, line);
        }

        int logMark = getPowerAmount(monsterNode, "Mark");
        if (logMark != enemy.getMark()) {
            throw new ReplayException("Monster[" + idx + "] mark mismatch: log=" + logMark + " state=" + enemy.getMark(), state, line);
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
            throw new ReplayException(listName + " mismatch: log=" + logCounts + " state=" + stateCounts, state, line);
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
