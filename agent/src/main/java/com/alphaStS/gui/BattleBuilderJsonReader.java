package com.alphaStS.gui;

import com.alphaStS.GameStateBuilder;
import com.alphaStS.card.Card;
import com.alphaStS.card.CardColorless;
import com.alphaStS.card.CardDefect;
import com.alphaStS.card.CardIronclad;
import com.alphaStS.card.CardOther;
import com.alphaStS.card.CardSilent;
import com.alphaStS.card.CardWatcher;
import com.alphaStS.entity.Potion;
import com.alphaStS.entity.Relic;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.player.Player;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parses a JSON battle definition string and produces a {@link GameStateBuilder}.
 * In phase 1, enemies and scenarios are not included; only player configuration,
 * deck, relics, and potions are wired.
 *
 * <p>Expected JSON format:
 * <pre>
 * {
 *   "battle_definition": {
 *     "character":        "Ironclad",
 *     "player_health":    50,
 *     "player_max_health": 62,
 *     "deck":    ["Bash", "Strike", "Defend", "Bash+"],
 *     "relics":  ["Akabeko"],
 *     "potions": ["Attack Potion"]
 *   }
 * }
 * </pre>
 */
public class BattleBuilderJsonReader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ----------------------------- Registries -----------------------------

    /** Maps card display name (exact case) to a representative Card instance. */
    private static final Map<String, Card> CARD_REGISTRY = new HashMap<>();

    /**
     * Maps relic display name (as in sts_relics.json) to its concrete Class.
     * A {@code null} value means the relic is marked {@code no_need_to_implement}.
     */
    private static final Map<String, Class<? extends Relic>> RELIC_REGISTRY = new HashMap<>();

    /** Reverse mapping from relic Class to its display name as defined in sts_relics.json. */
    public static final Map<Class<? extends Relic>, String> RELIC_CLASS_TO_NAME = new HashMap<>();

    /**
     * Maps potion display name (as in sts_potions.json) to its concrete Class.
     * A {@code null} value means the potion is marked {@code no_need_to_implement}.
     */
    private static final Map<String, Class<? extends Potion>> POTION_REGISTRY = new HashMap<>();

    /** Reverse mapping from potion Class to its display name as defined in sts_potions.json. */
    public static final Map<Class<? extends Potion>, String> POTION_CLASS_TO_NAME = new HashMap<>();

    // -------------------- no_need_to_implement sets -----------------------

    /** Lower-cased card names that have {@code no_need_to_implement} set in the data file. */
    private static final Set<String> NO_IMPL_CARDS = new HashSet<>();

    static {
        buildCardRegistry();
        buildRelicRegistry();
        buildPotionRegistry();
        buildNoImplSets();
    }

    // ----------------------------- Public API -----------------------------

    /**
     * Returns the Card registered under the given display name, or {@code null} if not found.
     *
     * @param displayName exact card name as stored in the registry (e.g. "Bash", "Bash+")
     */
    public static Card lookupCard(String displayName) {
        return CARD_REGISTRY.get(displayName);
    }

    /**
     * Instantiates a fresh Relic for the given display name, or {@code null} if
     * the name is unknown or the relic is marked no_need_to_implement.
     *
     * @param displayName exact display name as stored in sts_relics.json (e.g. "Akabeko")
     */
    public static Relic instantiateRelic(String displayName) {
        Class<? extends Relic> cls = RELIC_REGISTRY.get(displayName);
        if (cls == null) return null;
        try {
            Constructor<? extends Relic> ctor = cls.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) {
            System.err.println("[BattleDefinitionReader] Could not instantiate relic: \"" + displayName + "\"");
            return null;
        }
    }

    /**
     * Instantiates a fresh Potion for the given display name, or {@code null} if
     * the name is unknown or the potion is marked no_need_to_implement.
     *
     * @param displayName exact display name as stored in sts_potions.json (e.g. "Fear Potion")
     */
    public static Potion instantiatePotion(String displayName) {
        Class<? extends Potion> cls = POTION_REGISTRY.get(displayName);
        if (cls == null) return null;
        try {
            Constructor<? extends Potion> ctor = cls.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    /** Returns {@code true} if the card name is marked no_need_to_implement. */
    public static boolean isNoImplCard(String displayName) {
        return NO_IMPL_CARDS.contains(displayName.toLowerCase());
    }

    /** Returns {@code true} if the relic name is marked no_need_to_implement. */
    public static boolean isNoImplRelic(String displayName) {
        return RELIC_REGISTRY.containsKey(displayName) && RELIC_REGISTRY.get(displayName) == null;
    }

    /** Returns {@code true} if the potion name is marked no_need_to_implement. */
    public static boolean isNoImplPotion(String displayName) {
        return POTION_REGISTRY.containsKey(displayName) && POTION_REGISTRY.get(displayName) == null;
    }

    /**
     * Parses the given JSON string and returns a configured {@link GameStateBuilder}.
     * No enemies are added — the caller is responsible for supplying them.
     * Items marked {@code no_need_to_implement} in the data files are silently skipped.
     *
     * @param json the battle definition JSON
     * @return a GameStateBuilder with character, player, deck, relics, and potions set
     * @throws Exception on JSON parse errors or unknown card/relic/potion names
     */
    public static GameStateBuilder fromJson(String json) throws Exception {
        return fromJsonInternal(json, new ArrayList<>());
    }

    private static GameStateBuilder fromJsonInternal(String json, List<String> ignored) throws Exception {
        JsonNode root = MAPPER.readTree(json);
        if (!root.has("battle_definition")) {
            throw new IllegalArgumentException("Missing top-level 'battle_definition' key");
        }
        JsonNode def = root.get("battle_definition");

        assertField(def, "character");
        assertField(def, "player_health");
        assertField(def, "player_max_health");
        assertField(def, "deck");

        String characterStr = def.get("character").asText();
        int hp = def.get("player_health").asInt();
        int maxHp = def.get("player_max_health").asInt();

        if (hp > maxHp) {
            throw new IllegalArgumentException("player_health (" + hp + ") cannot exceed player_max_health (" + maxHp + ")");
        }
        if (hp <= 0) {
            throw new IllegalArgumentException("player_health must be positive");
        }

        CharacterEnum character = parseCharacter(characterStr);

        GameStateBuilder builder = new GameStateBuilder();
        builder.setCharacter(character);
        builder.setPlayer(new Player(hp, maxHp));

        for (JsonNode cardNameNode : def.get("deck")) {
            String cardName = cardNameNode.asText();
            Card card = CARD_REGISTRY.get(cardName);
            if (card == null) {
                if (NO_IMPL_CARDS.contains(cardName.toLowerCase())) {
                    ignored.add("card \"" + cardName + "\" (not implemented)");
                    continue;
                }
                throw new IllegalArgumentException("Unknown card: \"" + cardName + "\"");
            }
            builder.addCard(card);
        }

        JsonNode relicsNode = def.get("relics");
        if (relicsNode != null && !relicsNode.isNull()) {
            for (JsonNode relicNameNode : relicsNode) {
                String relicName = relicNameNode.asText();
                if (!RELIC_REGISTRY.containsKey(relicName)) {
                    throw new IllegalArgumentException("Unknown relic: \"" + relicName + "\"");
                }
                Class<? extends Relic> relicCls = RELIC_REGISTRY.get(relicName);
                if (relicCls == null) {
                    ignored.add("relic \"" + relicName + "\" (not implemented)");
                    continue;
                }
                Constructor<? extends Relic> ctor = relicCls.getDeclaredConstructor();
                ctor.setAccessible(true);
                builder.addRelic(ctor.newInstance());
            }
        }

        JsonNode potionsNode = def.get("potions");
        if (potionsNode != null && !potionsNode.isNull()) {
            for (JsonNode potionNameNode : potionsNode) {
                String potionName = potionNameNode.asText();
                if (!POTION_REGISTRY.containsKey(potionName)) {
                    throw new IllegalArgumentException("Unknown potion: \"" + potionName + "\"");
                }
                Class<? extends Potion> potionCls = POTION_REGISTRY.get(potionName);
                if (potionCls == null) {
                    ignored.add("potion \"" + potionName + "\" (not implemented)");
                    continue;
                }
                Constructor<? extends Potion> ctor = potionCls.getDeclaredConstructor();
                ctor.setAccessible(true);
                builder.addPotion(ctor.newInstance());
            }
        }

        return builder;
    }

    /**
     * Validates the given JSON string without constructing a full GameState.
     *
     * @param json the battle definition JSON
     * @return a {@link ValidationResult} with {@code valid=true} on success
     */
    public static ValidationResult validate(String json) {
        if (json == null || json.isBlank()) {
            return new ValidationResult(false, "Empty or null JSON");
        }
        try {
            JsonNode root = MAPPER.readTree(json);
            if (!root.has("battle_definition")) {
                return new ValidationResult(false, "Missing top-level 'battle_definition' key");
            }
            JsonNode def = root.get("battle_definition");
            for (String field : new String[]{"character", "player_health", "player_max_health", "deck"}) {
                if (!def.has(field) || def.get(field).isNull()) {
                    return new ValidationResult(false, "Missing required field: '" + field + "'");
                }
            }
            List<String> ignored = new ArrayList<>();
            fromJsonInternal(json, ignored);
            return new ValidationResult(true, null, List.copyOf(ignored));
        } catch (Exception e) {
            return new ValidationResult(false, e.getMessage());
        }
    }

    // ----------------------------- Inner Types ----------------------------

    /**
     * Holds the result of a {@link #validate} call.
     * {@code ignoredItems} lists any cards/relics/potions that were accepted but skipped
     * because they are marked {@code no_need_to_implement} in the data files.
     */
    public record ValidationResult(boolean valid, String error, List<String> ignoredItems) {
        /** Convenience constructor for error results with no ignored items. */
        public ValidationResult(boolean valid, String error) {
            this(valid, error, List.of());
        }
    }

    // ----------------------------- Private Helpers ------------------------

    private static CharacterEnum parseCharacter(String name) {
        return switch (name.toLowerCase()) {
            case "ironclad" -> CharacterEnum.IRONCLAD;
            case "silent"   -> CharacterEnum.SILENT;
            case "defect"   -> CharacterEnum.DEFECT;
            case "watcher"  -> CharacterEnum.WATCHER;
            default -> throw new IllegalArgumentException("Unknown character: \"" + name +
                    "\". Valid values: Ironclad, Silent, Defect, Watcher");
        };
    }

    private static void assertField(JsonNode node, String field) {
        if (!node.has(field) || node.get(field).isNull()) {
            throw new IllegalArgumentException("Missing required field: '" + field + "'");
        }
    }

    // ----------------------------- Registry Builders ----------------------

    @SuppressWarnings("unchecked")
    private static void buildCardRegistry() {
        List<Class<?>> containers = List.of(
            CardIronclad.class, CardSilent.class, CardDefect.class,
            CardWatcher.class, CardColorless.class, CardOther.class, Card.class
        );
        Map<String, Class<? extends Card>> classMap = new HashMap<>();
        for (Class<?> container : containers) {
            for (Class<?> cls : container.getDeclaredClasses()) {
                if (Card.class.isAssignableFrom(cls)) {
                    classMap.put(cls.getSimpleName().toLowerCase(), (Class<? extends Card>) cls);
                }
            }
        }

        try (InputStream is = BattleBuilderJsonReader.class.getResourceAsStream("/sts_cards.json")) {
            if (is == null) return;
            JsonNode arr = MAPPER.readTree(is);
            for (JsonNode entry : arr) {
                JsonNode nameNode = entry.get("name");
                if (nameNode == null) continue;
                String name = nameNode.asText();
                JsonNode noImpl = entry.get("no_need_to_implement");
                if (noImpl != null && !noImpl.isNull()) continue;
                String normalized = name.replace("'", "").replace(" ", "").replace("-", "").replace(".", "").toLowerCase();
                Class<? extends Card> cls = classMap.get(normalized);
                if (cls != null) {
                    try {
                        Constructor<? extends Card> ctor = cls.getDeclaredConstructor();
                        ctor.setAccessible(true);
                        CARD_REGISTRY.put(name, ctor.newInstance());
                    } catch (Exception e) {
                        System.err.println("[BattleDefinitionReader] Could not instantiate card: \"" + name + "\"");
                    }
                } else {
                    System.err.println("[BattleDefinitionReader] No card class found for: \"" + name + "\"");
                }
                Class<? extends Card> clsP = classMap.get(normalized + "p");
                if (clsP != null) {
                    try {
                        Constructor<? extends Card> ctorP = clsP.getDeclaredConstructor();
                        ctorP.setAccessible(true);
                        CARD_REGISTRY.put(name + "+", ctorP.newInstance());
                    } catch (Exception e) {
                        System.err.println("[BattleDefinitionReader] Could not instantiate card: \"" + name + "+\"");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[BattleDefinitionReader] Could not load card data: " + e.getMessage());
        }
    }

    private static void buildRelicRegistry() {
        buildItemRegistry(Relic.class, "/sts_relics.json", RELIC_REGISTRY, RELIC_CLASS_TO_NAME);
    }

    private static void buildPotionRegistry() {
        buildItemRegistry(Potion.class, "/sts_potions.json", POTION_REGISTRY, POTION_CLASS_TO_NAME);
    }

    @SuppressWarnings("unchecked")
    private static <T> void buildItemRegistry(
            Class<T> parentClass,
            String resourcePath,
            Map<String, Class<? extends T>> registry,
            Map<Class<? extends T>, String> classToName) {
        Map<String, Class<? extends T>> classMap = new HashMap<>();
        for (Class<?> cls : parentClass.getDeclaredClasses()) {
            if (parentClass.isAssignableFrom(cls)) {
                classMap.put(cls.getSimpleName().toLowerCase(), (Class<? extends T>) cls);
            }
        }

        try (InputStream is = BattleBuilderJsonReader.class.getResourceAsStream(resourcePath)) {
            if (is == null) return;
            JsonNode arr = MAPPER.readTree(is);
            for (JsonNode entry : arr) {
                JsonNode nameNode = entry.get("name");
                if (nameNode == null) continue;
                String name = nameNode.asText();
                JsonNode noImpl = entry.get("no_need_to_implement");
                if (noImpl != null && !noImpl.isNull()) {
                    registry.put(name, null);
                } else {
                    String normalized = name.replace("'", "").replace(" ", "").replace("-", "").replace(".", "").toLowerCase();
                    Class<? extends T> cls = classMap.get(normalized);
                    if (cls == null) {
                        System.err.println("[BattleDefinitionReader] No " + parentClass.getSimpleName().toLowerCase()
                                + " class found for: \"" + name + "\"");
                    } else {
                        registry.put(name, cls);
                        classToName.put(cls, name);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[BattleDefinitionReader] Could not load " + parentClass.getSimpleName().toLowerCase()
                    + " data: " + e.getMessage());
        }
    }

    private static void buildNoImplSets() {
        loadNoImplNames("/sts_cards.json", NO_IMPL_CARDS);
    }

    private static void loadNoImplNames(String resourcePath, Set<String> target) {
        try (InputStream is = BattleBuilderJsonReader.class.getResourceAsStream(resourcePath)) {
            if (is == null) return;
            JsonNode arr = MAPPER.readTree(is);
            for (JsonNode entry : arr) {
                JsonNode noImpl = entry.get("no_need_to_implement");
                if (noImpl != null && !noImpl.isNull()) {
                    JsonNode nameNode = entry.get("name");
                    if (nameNode != null) {
                        target.add(nameNode.asText().toLowerCase());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[BattleDefinitionReader] Could not load no_need_to_implement data from "
                    + resourcePath + ": " + e.getMessage());
        }
    }
}
