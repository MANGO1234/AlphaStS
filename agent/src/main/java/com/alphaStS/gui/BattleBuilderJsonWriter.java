package com.alphaStS.gui;

import com.alphaStS.GameStateBuilder;
import com.alphaStS.card.Card;
import com.alphaStS.entity.Potion;
import com.alphaStS.entity.Relic;
import com.alphaStS.enums.CharacterEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * Serializes a {@link GameStateBuilder} to a JSON string in the battle definition
 * format consumed by {@link BattleBuilderJsonReader}.
 *
 * <p>This is the inverse of {@link BattleBuilderJsonReader#fromJson}: given a builder
 * produced from run data, it emits a JSON object that can be sent to the STS mod to
 * reconstruct the same battle.
 *
 * <p>Note: enemies are not included in the output — they are not part of the
 * battle definition JSON format (phase 1 limitation, same as the reader).
 */
public class BattleBuilderJsonWriter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Converts the given {@link GameStateBuilder} to a battle definition JSON string.
     *
     * @param builder the builder to serialize; must have character, player, and deck set
     * @return a JSON string in the battle definition format
     * @throws IllegalArgumentException if the builder is missing required fields or
     *                                  contains relics/potions not in the registry
     * @throws RuntimeException         wrapping any JSON serialization error
     */
    public static String toJson(GameStateBuilder builder) {
        if (builder.getPlayer() == null) {
            throw new IllegalArgumentException("GameStateBuilder has no player set");
        }

        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode def = root.putObject("battle_definition");

        def.put("character", characterName(builder.getCharacter()));
        def.put("player_health", builder.getPlayer().getHealth());
        def.put("player_max_health", builder.getPlayer().getInBattleMaxHealth());

        ArrayNode deckNode = def.putArray("deck");
        for (Card card : builder.getCards()) {
            deckNode.add(card.cardName);
        }

        List<Relic> relics = builder.getRelics();
        if (!relics.isEmpty()) {
            ArrayNode relicsNode = def.putArray("relics");
            for (Relic relic : relics) {
                String name = BattleBuilderJsonReader.RELIC_CLASS_TO_NAME.get(relic.getClass());
                if (name == null) {
                    throw new IllegalArgumentException(
                            "Relic class not found in registry: " + relic.getClass().getSimpleName());
                }
                relicsNode.add(name);
            }
        }

        List<Potion> potions = builder.getExplicitPotions();
        if (potions != null && !potions.isEmpty()) {
            ArrayNode potionsNode = def.putArray("potions");
            for (Potion potion : potions) {
                String name = BattleBuilderJsonReader.POTION_CLASS_TO_NAME.get(potion.getClass());
                if (name == null) {
                    throw new IllegalArgumentException(
                            "Potion class not found in registry: " + potion.getClass().getSimpleName());
                }
                potionsNode.add(name);
            }
        }

        try {
            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize battle definition to JSON", e);
        }
    }

    private static String characterName(CharacterEnum character) {
        return switch (character) {
            case IRONCLAD -> "Ironclad";
            case SILENT   -> "Silent";
            case DEFECT   -> "Defect";
            case WATCHER  -> "Watcher";
        };
    }
}
