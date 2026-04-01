package com.alphaStS;

import com.alphaStS.gui.BattleBuilderJsonReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Temporary validation script: iterates every known card, relic, and potion
 * from the STS data files and checks whether the backend (BattleDefinitionReader)
 * can resolve each one.  All items are validated, including those marked
 * no_need_to_implement.  Failures are printed at the end; the test itself never
 * fails so the full report is always produced.
 */
public class ValidateAllItemsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private List<JsonNode> loadEntries(String resource) throws Exception {
        try (InputStream is = getClass().getResourceAsStream(resource)) {
            if (is == null) throw new IllegalStateException("Resource not found: " + resource);
            JsonNode arr = MAPPER.readTree(is);
            List<JsonNode> list = new ArrayList<>();
            arr.forEach(list::add);
            return list;
        }
    }

    /** Returns the character string to use in the battle_definition JSON. */
    private static String resolveCharacter(JsonNode entry) {
        String ch = entry.has("character") ? entry.get("character").asText() : "Ironclad";
        return switch (ch.toLowerCase()) {
            case "colorless", "any" -> "Ironclad";
            default -> ch;
        };
    }

    private static String battleJson(String character, String deckJson, String relicsJson, String potionsJson) {
        return String.format("""
                {
                  "battle_definition": {
                    "character": "%s",
                    "player_health": 80,
                    "player_max_health": 80,
                    "deck": %s,
                    "relics": %s,
                    "potions": %s
                  }
                }""", character, deckJson, relicsJson, potionsJson);
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // -------------------------------------------------------------------------
    // Main test
    // -------------------------------------------------------------------------

    @Test
    public void validateAllItems() throws Exception {
        List<String> failedCards = new ArrayList<>();
        List<String> failedRelics = new ArrayList<>();
        List<String> failedPotions = new ArrayList<>();

        // --- Cards (base + upgraded) -----------------------------------------
        List<JsonNode> cards = loadEntries("/sts_cards.json");
        for (JsonNode entry : cards) {
            String name = entry.get("name").asText();
            String character = resolveCharacter(entry);

            // Validate base card
            String json = battleJson(character,
                    "[\"" + escape(name) + "\"]", "[]", "[]");
            BattleBuilderJsonReader.ValidationResult result = BattleBuilderJsonReader.validate(json);
            if (!result.valid()) {
                failedCards.add(name + " (" + character + "): " + result.error());
            }

            // Validate upgraded card (name+)
            String upgradedName = name + "+";
            String jsonUpgraded = battleJson(character,
                    "[\"" + escape(upgradedName) + "\"]", "[]", "[]");
            BattleBuilderJsonReader.ValidationResult upgradedResult = BattleBuilderJsonReader.validate(jsonUpgraded);
            if (!upgradedResult.valid()) {
                failedCards.add(upgradedName + " (" + character + "): " + upgradedResult.error());
            }
        }

        // --- Relics ----------------------------------------------------------
        List<JsonNode> relics = loadEntries("/sts_relics.json");
        for (JsonNode entry : relics) {
            String name = entry.get("name").asText();
            String character = resolveCharacter(entry);

            String json = battleJson(character, "[]",
                    "[\"" + escape(name) + "\"]", "[]");
            BattleBuilderJsonReader.ValidationResult result = BattleBuilderJsonReader.validate(json);
            if (!result.valid()) {
                failedRelics.add(name + " (" + character + "): " + result.error());
            }
        }

        // --- Potions ---------------------------------------------------------
        List<JsonNode> potions = loadEntries("/sts_potions.json");
        for (JsonNode entry : potions) {
            String name = entry.get("name").asText();
            String character = resolveCharacter(entry);

            String json = battleJson(character, "[]", "[]",
                    "[\"" + escape(name) + "\"]");
            BattleBuilderJsonReader.ValidationResult result = BattleBuilderJsonReader.validate(json);
            if (!result.valid()) {
                failedPotions.add(name + " (" + character + "): " + result.error());
            }
        }

        // --- Report ----------------------------------------------------------
        System.out.println("\n========== VALIDATION REPORT ==========");
        System.out.printf("Cards checked : %d base + %d upgraded%n", cards.size(), cards.size());
        System.out.printf("Relics checked: %d%n", relics.size());
        System.out.printf("Potions checked: %d%n", potions.size());
        System.out.println();

        if (failedCards.isEmpty() && failedRelics.isEmpty() && failedPotions.isEmpty()) {
            System.out.println("All items passed validation.");
        } else {
            if (!failedCards.isEmpty()) {
                System.out.println("--- FAILED CARDS (" + failedCards.size() + ") ---");
                failedCards.forEach(s -> System.out.println("  " + s));
            }
            if (!failedRelics.isEmpty()) {
                System.out.println("--- FAILED RELICS (" + failedRelics.size() + ") ---");
                failedRelics.forEach(s -> System.out.println("  " + s));
            }
            if (!failedPotions.isEmpty()) {
                System.out.println("--- FAILED POTIONS (" + failedPotions.size() + ") ---");
                failedPotions.forEach(s -> System.out.println("  " + s));
            }
        }
        System.out.println("========================================\n");
    }
}
