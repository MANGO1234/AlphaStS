package com.alphaStS.test;

import com.alphaStS.card.CardIronclad;
import com.alphaStS.gui.BattleBuilderJsonReader;
import com.alphaStS.GameStateBuilder;
import com.alphaStS.card.Card;
import com.alphaStS.card.CardColorless;
import com.alphaStS.entity.Potion;
import com.alphaStS.entity.Relic;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.player.Player;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;

public class RunDataParser implements Iterable<BattleEntry> {

    /** Sorts entries by character ordinal (IRONCLAD → SILENT → DEFECT → WATCHER). */
    public static final Comparator<BattleEntry> SORT_BY_CHARACTER =
            Comparator.comparing(e -> e.getBuilder().getCharacter().ordinal());

    /** Filters for Ironclad battles only. */
    public static final Predicate<BattleEntry> FILTER_IRONCLAD =
            e -> e.getBuilder().getCharacter() == CharacterEnum.IRONCLAD;

    /** Filters for Silent battles only. */
    public static final Predicate<BattleEntry> FILTER_SILENT =
            e -> e.getBuilder().getCharacter() == CharacterEnum.SILENT;

    /** Filters for Defect battles only. */
    public static final Predicate<BattleEntry> FILTER_DEFECT =
            e -> e.getBuilder().getCharacter() == CharacterEnum.DEFECT;

    /** Filters for Watcher battles only. */
    public static final Predicate<BattleEntry> FILTER_WATCHER =
            e -> e.getBuilder().getCharacter() == CharacterEnum.WATCHER;

    private final String jsonFilePath;
    private Predicate<BattleEntry> filter;
    private Comparator<BattleEntry> sort;

    public RunDataParser(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
    }

    public RunDataParser withFilter(Predicate<BattleEntry> filter) {
        this.filter = filter;
        return this;
    }

    public RunDataParser withSort(Comparator<BattleEntry> sort) {
        this.sort = sort;
        return this;
    }

    @Override
    public Iterator<BattleEntry> iterator() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try {
            root = mapper.readTree(new File(jsonFilePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read run data file: " + jsonFilePath, e);
        }

        if (!root.isArray()) {
            throw new IllegalArgumentException("Expected a JSON array at the root of: " + jsonFilePath);
        }

        List<BattleEntry> builders = new ArrayList<>();
        for (int i = 0; i < root.size(); i++) {
            try {
                System.out.printf("Parsing Battle %d\n", i);
                builders.addAll(parseRunData(root.get(i), i, false));
            } catch (Exception e) {
                System.err.println("[RunDataParser] Failed to parse run " + i + ": " + Arrays.toString(e.getStackTrace()));
            }
        }

        if (filter != null) {
            builders.removeIf(filter.negate());
        }

        if (sort != null) {
            builders.sort(sort);
        }

        return builders.iterator();
    }

    /**
     * Parses the ith run from the JSON array, returning one tuple per battle.
     *
     * @param i zero-based run index
     * @return list of {@code (runIndex, battleIndex, GameStateBuilder)} for each battle
     */
    public List<BattleEntry> parseRun(int i) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(jsonFilePath));
        if (!root.isArray()) {
            throw new IllegalArgumentException("Expected a JSON array at the root of: " + jsonFilePath);
        }
        if (i < 0 || i >= root.size()) {
            throw new IndexOutOfBoundsException("Run index " + i + " out of range [0, " + root.size() + ")");
        }
        JsonNode runNode = root.get(i);
        System.out.println("Run " + i + " raw data:\n" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(runNode));
        return parseRunData(runNode, i, true);
    }

    /**
     * Parses the kth battle of the ith run from the JSON array.
     *
     * @param i zero-based run index
     * @param k zero-based battle index within the run
     * @return {@code (runIndex, battleIndex, GameStateBuilder)} for the requested battle
     */
    public BattleEntry parseBattle(int i, int k) throws Exception {
        List<BattleEntry> battles = parseRun(i);
        if (k < 0 || k >= battles.size()) {
            throw new IndexOutOfBoundsException(
                "Battle index " + k + " out of range [0, " + battles.size() + ")");
        }
        return battles.get(k);
    }

    /**
     * Reconstructs a {@link GameStateBuilder} for every battle in the run, yielding
     * one {@link BattleEntry} per entry in {@code damage_taken}. Each entry captures
     * the deck, relics, potions, player HP, and enemies name at the start of that battle.
     */
    private List<BattleEntry> parseRunData(JsonNode runNode, int runIdx, boolean verbose) {
        JsonNode run = runNode.has("event") ? runNode.get("event") : runNode;
        CharacterEnum character = parseCharacter(run.get("character_chosen").asText());

        int[] currentHpPerFloor = toIntArray(run.get("current_hp_per_floor"));
        int[] maxHpPerFloor = toIntArray(run.get("max_hp_per_floor"));

        ParseContext ctx = new ParseContext();

        if (run.has("circlet_count") && run.get("circlet_count").asInt() > 0) {
            System.out.println("[RunDataParser] Circlet count > 0");
            return new ArrayList<>();
        }

        // card_choices: floor -> card picked
        if (run.has("card_choices")) {
            for (JsonNode choice : run.get("card_choices")) {
                if (!choice.get("picked").asText().equals("SKIP") && !choice.get("picked").asText().equals("Singing Bowl")) {
                    addEntityToFloorMap(ctx.cardObtainedAtFloor, choice.get("floor").asInt(), normalizeCardName(choice.get("picked").asText()));
                }
            }
        }

        // relics_obtained: floor -> relic name
        if (run.has("relics_obtained")) {
            for (JsonNode entry : run.get("relics_obtained")) {
                addEntityToFloorMap(ctx.relicObtainedAtFloor, entry.get("floor").asInt(), normalizeRelicName(entry.get("key").asText()));
            }
        }

        // event_choices: floor -> (cards_obtained, cards_removed, relics_obtained)
        // Note: run data can be dirty with duplicate consecutive events at the same floor; skip the second.
        if (run.has("event_choices")) {
            int lastEventFloor = -1;
            for (JsonNode entry : run.get("event_choices")) {
                int floor = entry.get("floor").asInt();
                if (floor == lastEventFloor) {
                    continue;
                }
                lastEventFloor = floor;
                addEntityToFloorMap(ctx.cardObtainedAtFloor, floor, toStringList(entry.get("cards_obtained"), RunDataParser::normalizeCardName));
                addEntityToFloorMap(ctx.cardRemovedAtFloor, floor, toStringList(entry.get("cards_transformed"), RunDataParser::normalizeCardName));
                addEntityToFloorMap(ctx.cardRemovedAtFloor, floor, toStringList(entry.get("cards_removed"), RunDataParser::normalizeCardName));
                addEntityToFloorMap(ctx.cardUpgradedAtFloor, floor, toStringList(entry.get("cards_upgraded"), RunDataParser::normalizeCardName));
                addEntityToFloorMap(ctx.relicObtainedAtFloor, floor, toStringList(entry.get("relics_obtained"), RunDataParser::normalizeRelicName));
                addEntityToFloorMap(ctx.relicRemovedAtFloor, floor, toStringList(entry.get("relics_lost"), RunDataParser::normalizeRelicName));
                if (entry.has("event_name")) {
                    ctx.eventAtFloor.put(floor, entry.get("event_name").asText());
                }
            }
        }

        // campfire_choices: floor -> card name to upgrade (only SMITH entries)
        if (run.has("campfire_choices")) {
            for (JsonNode entry : run.get("campfire_choices")) {
                if ("SMITH".equals(entry.get("key").asText())) {
                    addEntityToFloorMap(ctx.cardUpgradedAtFloor, entry.get("floor").asInt(), normalizeCardName(entry.get("data").asText()));
                }
            }
        }

        // boss_relics: floor 17 / 34 / 51
        if (run.has("boss_relics")) {
            int[] bossFloors = {17, 34, 51};
            int idx = 0;
            for (JsonNode entry : run.get("boss_relics")) {
                if (idx >= bossFloors.length) {
                    System.err.println("[RunDataParser] Too many boss relics: " + entry);
                }
                JsonNode pickedNode = entry.get("picked");
                if (pickedNode != null && !pickedNode.isNull()) {
                    String picked = pickedNode.asText();
                    if (!"SKIP".equals(picked)) {
                        addEntityToFloorMap(ctx.relicObtainedAtFloor, bossFloors[idx], normalizeRelicName(picked));
                    }
                }
                idx++;
            }
        }

        // items_purged / items_purged_floors (parallel arrays)
        List<String> itemsPurged = toStringList(run.get("items_purged"), null);
        List<Integer> itemsPurgedFloors = toIntList(run.get("items_purged_floors"));
        if (itemsPurged.size() != itemsPurgedFloors.size()) {
            System.err.println("[RunDataParser] Inconsistent items_purged: " + itemsPurged + " vs " + itemsPurgedFloors);
        }
        for (int i = 0; i < Math.min(itemsPurged.size(), itemsPurgedFloors.size()); i++) {
            addEntityToFloorMap(ctx.cardRemovedAtFloor, itemsPurgedFloors.get(i), normalizeCardName(itemsPurged.get(i)));
        }

        // items_purchased / item_purchase_floors (parallel arrays)
        List<String> itemsPurchased = toStringList(run.get("items_purchased"), null);
        List<Integer> itemPurchaseFloors = toIntList(run.get("item_purchase_floors"));
        if (itemsPurchased.size() != itemPurchaseFloors.size()) {
            System.err.println("[RunDataParser] Inconsistent items_purchased: " + itemsPurchased + " vs " + itemPurchaseFloors);
        }
        for (int i = 0; i < Math.min(itemsPurchased.size(), itemPurchaseFloors.size()); i++) {
            classifyAndAddPurchasedItem(itemsPurchased.get(i), itemPurchaseFloors.get(i),
                    ctx.cardObtainedAtFloor, ctx.relicObtainedAtFloor, ctx.potionObtainedAtFloor);
        }

        // potions_obtained: floor -> potion key
        if (run.has("potions_obtained")) {
            for (JsonNode entry : run.get("potions_obtained")) {
                addEntityToFloorMap(ctx.potionObtainedAtFloor, entry.get("floor").asInt(), normalizePotionName(entry.get("key").asText()));
            }
        }

        // potions_floor_usage: set of floors where a potion was consumed
        if (run.has("potions_floor_usage")) {
            for (JsonNode floorNode : run.get("potions_floor_usage")) {
                ctx.potionUsedAtFloor.add(floorNode.asInt());
            }
        }

        // neow_bonus / neow_cost: update per-floor maps and budgets for floor 0
        String neowBonus = run.has("neow_bonus") ? run.get("neow_bonus").asText() : "";
        String neowCost = run.has("neow_cost") ? run.get("neow_cost").asText() : "";
        Set<String> relicsAlreadyTracked = new HashSet<>();
        for (List<String> names : ctx.relicObtainedAtFloor.values()) {
            relicsAlreadyTracked.addAll(names);
        }
        switch (neowBonus) {
            case "BOSS_RELIC" -> {
                if (run.has("relics") && !run.get("relics").isEmpty()) {
                    String firstRelic = normalizeRelicName(run.get("relics").get(0).asText());
                    if (firstRelic != null && BOSS_RELICS.contains(firstRelic)) {
                        addEntityToFloorMap(ctx.relicObtainedAtFloor, 0, firstRelic);
                    }
                }
            }
            case "TRANSFORM_TWO_CARDS" -> ctx.unknownNonCurseCardCountObtainedAtFloor.merge(0, 2, Integer::sum);
            case "TRANSFORM_CARD", "THREE_CARDS" -> ctx.unknownNonCurseCardCountObtainedAtFloor.merge(0, 1, Integer::sum);
            case "ONE_RANDOM_RARE_CARD", "THREE_RARE_CARDS" -> ctx.unknownRareCardCountObtainedAtFloor.merge(0, 1, Integer::sum);
            case "RANDOM_COLORLESS", "RANDOM_COLORLESS_2" -> ctx.unknownColorlessCardCountObtainedAtFloor.merge(0, 1, Integer::sum);
            case "ONE_RARE_RELIC", "RANDOM_COMMON_RELIC" -> {
                if (run.has("relics") && run.get("relics").size() > 1) {
                    String secondRelic = normalizeRelicName(run.get("relics").get(1).asText());
                    if (secondRelic != null && !relicsAlreadyTracked.contains(secondRelic)) {
                        addEntityToFloorMap(ctx.relicObtainedAtFloor, 0, secondRelic);
                    }
                }
            }
//            case "REMOVE_CARD" -> addEntityToFloorMap(ctx.cardRemovedAtFloor, 0, "Strike");
//            case "REMOVE_TWO" -> {
//                addEntityToFloorMap(ctx.cardRemovedAtFloor, 0, "Strike");
//                addEntityToFloorMap(ctx.cardRemovedAtFloor, 0, "Defend");
//            }
            case "UPGRADE_CARD" -> ctx.unknownUpgradeStarterCardCountObtainedAtFloor.merge(0, 1, Integer::sum);
        }
        if ("CURSE".equals(neowCost)) {
            ctx.unknownCurseCardCountObtainedAtFloor.merge(0, 1, Integer::sum);
        }

        // Collect battle floors from damage_taken (floor -> enemies string)
        if (run.has("damage_taken")) {
            for (JsonNode battle : run.get("damage_taken")) {
                int floor = battle.get("floor").asInt();
                String enemies = battle.has("enemies") ? battle.get("enemies").asText() : "";
                ctx.battleFloors.put(floor, enemies);
            }
        }

        // Writhing Mass battles can add a Parasite curse card to the deck
        for (Map.Entry<Integer, String> entry : ctx.battleFloors.entrySet()) {
            if ("Writhing Mass".equals(entry.getValue())) {
                ctx.unknownParasiteCountObtainedAtFloor.merge(entry.getKey(), 1, Integer::sum);
            }
        }

        // --- Pre-loop: add dependent relics/cards before simulating ---

        // Necronomicon obtained -> also obtain Calling Bell
        for (Map.Entry<Integer, List<String>> entry : new HashMap<>(ctx.relicObtainedAtFloor).entrySet()) {
            if (entry.getValue().contains("Necronomicon")) {
                addEntityToFloorMap(ctx.relicObtainedAtFloor, entry.getKey(), "Calling Bell");
            }
        }

        // Calling Bell obtained -> also obtain Curse of the Bell card
        for (Map.Entry<Integer, List<String>> entry : new HashMap<>(ctx.relicObtainedAtFloor).entrySet()) {
            if (entry.getValue().contains("Calling Bell")) {
                addEntityToFloorMap(ctx.cardObtainedAtFloor, entry.getKey(), "Curse of the Bell");
            }
        }

        // Whetstone obtained -> upgrade 2 attack cards at acquisition floor
        for (Map.Entry<Integer, List<String>> entry : ctx.relicObtainedAtFloor.entrySet()) {
            if (entry.getValue().contains("Whetstone")) {
                ctx.unknownUpgradeAttackCardCountObtainedAtFloor.merge(entry.getKey(), 2, Integer::sum);
            }
            if (entry.getValue().contains("War Paint")) {
                ctx.unknownUpgradeDefendCardCountObtainedAtFloor.merge(entry.getKey(), 2, Integer::sum);
            }
        }

        // --- Simulate the run floor by floor ---

        List<Card> deck = new ArrayList<>(getStartingDeck(character));
        List<Relic> relics = new ArrayList<>();
        relics.add(getStarterRelic(character));
        List<String> potions = new ArrayList<>();
        List<BattleEntry> battles = new ArrayList<>();
        int battleIdx = 0;

        if (verbose) {
            System.out.println("cardObtainedAtFloor: " + ctx.cardObtainedAtFloor);
            System.out.println("cardRemovedAtFloor: " + ctx.cardRemovedAtFloor);
            System.out.println("cardUpgradedAtFloor: " + ctx.cardUpgradedAtFloor);
            System.out.println("relicObtainedAtFloor: " + ctx.relicObtainedAtFloor);
        }

        for (int floor = 0; floor <= 55; floor++) {
            ctx.unknownNonCurseRemaining += ctx.unknownNonCurseCardCountObtainedAtFloor.getOrDefault(floor, 0);
            ctx.unknownCurseRemaining += ctx.unknownCurseCardCountObtainedAtFloor.getOrDefault(floor, 0);
            ctx.unknownRareRemaining += ctx.unknownRareCardCountObtainedAtFloor.getOrDefault(floor, 0);
            ctx.unknownColorlessRemaining += ctx.unknownColorlessCardCountObtainedAtFloor.getOrDefault(floor, 0);
            ctx.unknownUpgradeStarterRemaining += ctx.unknownUpgradeStarterCardCountObtainedAtFloor.getOrDefault(floor, 0);
            ctx.unknownUpgradeAttackRemaining += ctx.unknownUpgradeAttackCardCountObtainedAtFloor.getOrDefault(floor, 0);
            ctx.unknownUpgradeDefendRemaining += ctx.unknownUpgradeDefendCardCountObtainedAtFloor.getOrDefault(floor, 0);
            ctx.unknownParasiteRemaining += ctx.unknownParasiteCountObtainedAtFloor.getOrDefault(floor, 0);
            // Capture battle state BEFORE processing events at this floor
            if (ctx.battleFloors.containsKey(floor)) {
                String enemiesName = ctx.battleFloors.get(floor);
                battles.add(new BattleEntry(runIdx, battleIdx, buildBattleState(
                    character, deck, relics, potions,
                    floor, currentHpPerFloor, maxHpPerFloor
                ), enemiesName, run.get("play_id").asText()));
                battleIdx++;
            }
            applyFloorEvents(floor, deck, relics, potions, ctx);
            if (verbose) {
//                System.out.println(floor + ": " + deck + " " + relics + " " + potions);
            }
        }

        return battles;
    }

    // ----------------------------- Parse Context ---------------------------

    private static class ParseContext {
        final Map<Integer, List<String>> cardObtainedAtFloor = new HashMap<>();
        final Map<Integer, List<String>> cardRemovedAtFloor = new HashMap<>();
        final Map<Integer, List<String>> cardUpgradedAtFloor = new HashMap<>();
        final Map<Integer, List<String>> relicObtainedAtFloor = new HashMap<>();
        final Map<Integer, List<String>> relicRemovedAtFloor = new HashMap<>();
        final Map<Integer, List<String>> potionObtainedAtFloor = new HashMap<>();
        final Map<Integer, String> eventAtFloor = new HashMap<>();
        final Set<Integer> potionUsedAtFloor = new HashSet<>();
        final Map<Integer, Integer> unknownNonCurseCardCountObtainedAtFloor = new HashMap<>();
        final Map<Integer, Integer> unknownCurseCardCountObtainedAtFloor = new HashMap<>();
        final Map<Integer, Integer> unknownRareCardCountObtainedAtFloor = new HashMap<>();
        final Map<Integer, Integer> unknownColorlessCardCountObtainedAtFloor = new HashMap<>();
        final Map<Integer, Integer> unknownUpgradeStarterCardCountObtainedAtFloor = new HashMap<>();
        final Map<Integer, Integer> unknownUpgradeAttackCardCountObtainedAtFloor = new HashMap<>();
        final Map<Integer, Integer> unknownUpgradeDefendCardCountObtainedAtFloor = new HashMap<>();
        final Map<Integer, Integer> unknownParasiteCountObtainedAtFloor = new HashMap<>();
        final Map<Integer, String> battleFloors = new HashMap<>();
        public Set<String> relicStrings = new HashSet<>();

        int unknownUpgradedCardRemaining = 0;
        int unknownAnyCardRemaining = 0;
        int unknownNonCurseRemaining = 0;
        int unknownCurseRemaining = 0;
        int unknownRareRemaining = 0;
        int unknownColorlessRemaining = 0;
        int unknownUpgradeStarterRemaining = 0;
        int unknownUpgradeAttackRemaining = 0;
        int unknownUpgradeDefendRemaining = 0;
        int unknownParasiteRemaining = 0;
        int eventRemovalRemaining = 0;
    }

    // ----------------------------- Floor map helpers ----------------------

    public void addEntityToFloorMap(Map<Integer, List<String>> map, int floor, String name) {
        if (name == null) {
            return;
        }
        if (map.containsKey(floor)) {
            map.get(floor).add(name);
        } else {
            map.put(floor, new ArrayList<>(List.of(name)));
        }
    }

    public void addEntityToFloorMap(Map<Integer, List<String>> map, int floor, List<String> names) {
        if (map.containsKey(floor)) {
            map.get(floor).addAll(names);
        } else {
            map.put(floor, names);
        }
    }

    /**
     * Classifies a raw item from {@code items_purchased} as a card, relic, or potion
     * and adds it to the appropriate floor map.  Shop items can be any of the three types,
     * so we try card -> relic -> potion in order.
     */
    private void classifyAndAddPurchasedItem(
        String rawItem, int floor,
        Map<Integer, List<String>> cardObtainedAtFloor,
        Map<Integer, List<String>> relicObtainedAtFloor,
        Map<Integer, List<String>> potionObtainedAtFloor
    ) {
        String cardName = normalizeCardName(rawItem);
        if (cardName != null && (BattleBuilderJsonReader.lookupCard(cardName) != null || BattleBuilderJsonReader.isNoImplCard(cardName))) {
            addEntityToFloorMap(cardObtainedAtFloor, floor, cardName);
            return;
        }
        String relicName = normalizeRelicName(rawItem);
        if (relicName != null && (BattleBuilderJsonReader.instantiateRelic(relicName) != null || BattleBuilderJsonReader.isNoImplRelic(relicName))) {
            addEntityToFloorMap(relicObtainedAtFloor, floor, relicName);
            return;
        }
        String potionName = normalizePotionName(rawItem);
        if (potionName != null && (BattleBuilderJsonReader.instantiatePotion(potionName) != null || BattleBuilderJsonReader.isNoImplPotion(potionName))) {
            addEntityToFloorMap(potionObtainedAtFloor, floor, potionName);
            return;
        }
        if (rawItem.contains("+")) {
            rawItem = rawItem.substring(0, rawItem.indexOf('+'));
        }
        if (SKIP_CARDS.contains(rawItem) || SKIP_RELICS.contains(rawItem) || SKIP_POTIONS.contains(rawItem)) {
            return;
        }
        System.err.println("[RunDataParser] Unknown purchased item: \"" + rawItem + "\"");
    }

    private GameStateBuilder buildBattleState(
        CharacterEnum character,
        List<Card> deck,
        List<Relic> relics,
        List<String> potions,
        int floor,
        int[] currentHpPerFloor,
        int[] maxHpPerFloor
    ) {
        GameStateBuilder builder = new GameStateBuilder();
        builder.setCharacter(character);

        // current_hp_per_floor is 0-indexed: index 0 = after floor 1
        // For a battle at floor F, HP going in is current_hp_per_floor[F-1]
        int hpIdx = floor - 1;
        int hp = (hpIdx >= 0 && hpIdx < currentHpPerFloor.length)
            ? currentHpPerFloor[hpIdx]
            : (maxHpPerFloor.length > 0 ? maxHpPerFloor[0] : 80);
        int maxHp = (hpIdx >= 0 && hpIdx < maxHpPerFloor.length)
            ? maxHpPerFloor[hpIdx]
            : (maxHpPerFloor.length > 0 ? maxHpPerFloor[0] : 80);

        if (hp <= 0) hp = 1;
        if (hp > maxHp) hp = maxHp;
        builder.setPlayer(new Player(hp, maxHp));

        for (Card card : deck) {
            builder.addCard(card);
        }

        for (Relic relic : relics) {
            Relic fresh = BattleBuilderJsonReader.instantiateRelic(relic.toString());
            if (fresh != null) {
                builder.addRelic(fresh);
            }
        }

        for (String displayName : potions) {
            Potion potion = BattleBuilderJsonReader.instantiatePotion(displayName);
            if (potion != null) {
                builder.addPotion(potion);
            }
        }

        return builder;
    }

    private void applyFloorEvents(int floor, List<Card> deck, List<Relic> relics, List<String> potionDisplayNames, ParseContext ctx) {
        var cardsObtained = ctx.cardObtainedAtFloor.get(floor);
        if (cardsObtained != null) {
            for (String cardName : cardsObtained) {
                addCardToDeck(deck, ctx, floor, cardName);
                if (cardName.contains("Lesson Learned")) {
                    ctx.unknownUpgradedCardRemaining += 1000;
                }
            }
        }
        var cardsRemoved = ctx.cardRemovedAtFloor.get(floor);
        if (cardsRemoved != null) {
            for (String cardName : cardsRemoved) {
                removeCardFromDeck(deck, cardName, floor, ctx);
            }
        }
        var cardsUpgraded = ctx.cardUpgradedAtFloor.get(floor);
        if (cardsUpgraded != null) {
            for (String cardName : cardsUpgraded) {
                upgradeCardInDeck(deck, cardName, floor, ctx);
            }
        }
        var relicsObtained = ctx.relicObtainedAtFloor.get(floor);
        if (relicsObtained != null) {
            for (String relicName : relicsObtained) {
                if ("Pandora's Box".equals(relicName)) {
                    int strikeDefendCount = 0;
                    for (Card c : deck) {
                        if (isStrikeOrDefend(c.cardName)) {
                            strikeDefendCount++;
                        }
                    }
                    ctx.unknownNonCurseRemaining += strikeDefendCount;
                } else {
                    if ("Cursed Key".equals(relicName)) {
                        ctx.unknownCurseRemaining += 1000;
                    }
                    if ("Tiny House".equals(relicName)) {
                        ctx.unknownNonCurseRemaining += 2;
                    }
                    if ("Dolly's Mirror".equals(relicName)) {
                        ctx.unknownNonCurseRemaining += 1;
                    }
                    if ("Astrolabe".equals(relicName)) {
                        ctx.unknownAnyCardRemaining += 3;
                    }
                    addRelicToList(relics, ctx.relicStrings, relicName);
                }
            }
        }
        var relicsLost = ctx.relicRemovedAtFloor.get(floor);
        if (relicsLost != null) {
            for (String relicName : relicsLost) {
                relics.removeIf(r -> r.toString().equals(relicName));
            }
        }
        var potionsObtained = ctx.potionObtainedAtFloor.get(floor);
        if (potionsObtained != null) {
            for (String potionKey : potionsObtained) {
                int capacity = hasPotionBelt(relics) ? 4 : 2;
                String displayName = normalizePotionName(potionKey);
                if (potionDisplayNames.size() >= capacity) {
                    // Drop the oldest potion to make room
                    potionDisplayNames.remove(0);
                }
                potionDisplayNames.add(displayName);
            }
        }
        var potionUsed = ctx.potionUsedAtFloor.contains(floor);
        if (potionUsed && !potionDisplayNames.isEmpty()) {
            potionDisplayNames.remove(0);
        }
    }

    // ----------------------------- Deck helpers ---------------------------

    private void addCardToDeck(List<Card> deck, ParseContext ctx, int floor, String normalizedName) {
        Card card = BattleBuilderJsonReader.lookupCard(normalizedName);
        if (ctx.eventAtFloor.get(floor) != null) {
            if (card.cardType == Card.POWER && ctx.relicStrings.contains("Frozen Egg")) {
                var upgrade = card.getUpgrade();
                card = upgrade != null ? upgrade : card;
            } else if (card.cardType == Card.SKILL && ctx.relicStrings.contains("Toxic Egg")) {
                var upgrade = card.getUpgrade();
                card = upgrade != null ? upgrade : card;
            } else if (card.cardType == Card.ATTACK && ctx.relicStrings.contains("Molten Egg")) {
                var upgrade = card.getUpgrade();
                card = upgrade != null ? upgrade : card;
            }
        }
        if (card != null) {
            deck.add(card);
        } else if (!BattleBuilderJsonReader.isNoImplCard(normalizedName)) {
            System.err.println("[RunDataParser] Unknown card: \"" + normalizedName + "\"");
        }
    }

    private void removeCardFromDeck(List<Card> deck, String normalizedName, int floor, ParseContext ctx) {
        if (ctx.eventAtFloor.get(floor) != null) {
            ctx.eventRemovalRemaining++;
        }
        for (int i = 0; i < deck.size(); i++) {
            var name = deck.get(i).cardName;
            if (name.indexOf('(') >= 0) {
                name = name.substring(0, name.indexOf('(')).strip();
            }
            if (deck.get(i) != null && name.equals(normalizedName)) {
                deck.remove(i);
                return;
            }
        }
        // For any events, the run data may list the unupgraded card name even if the card was upgraded.
        // Try the upgraded version as a fallback.
        if (ctx.eventAtFloor.get(floor) != null) {
            String upgradedName = normalizedName + "+";
            for (int i = 0; i < deck.size(); i++) {
                if (deck.get(i) != null && deck.get(i).cardName.equals(upgradedName)) {
                    deck.remove(i);
                    //                    System.out.println("[RunDataParser] Event at floor " + floor
                    //                            + ": removed upgraded card \"" + upgradedName + "\" in place of \""
                    //                            + normalizedName + "\"");
                    return;
                }
            }
        }
        if (isCurseCard(normalizedName)) {
            if (normalizedName.equals("Parasite") && ctx.unknownParasiteRemaining > 0) {
                ctx.unknownParasiteRemaining--;
                return;
            }
            if (ctx.unknownCurseRemaining > 0) {
                ctx.unknownCurseRemaining--;
                return;
            }
            if (ctx.unknownAnyCardRemaining > 0) {
                ctx.unknownAnyCardRemaining--;
                return;
            }
        } else {
            if (isRareCard(normalizedName) && ctx.unknownRareRemaining > 0) {
                ctx.unknownRareRemaining--;
                return;
            }
            if (isColorlessCard(normalizedName) && ctx.unknownColorlessRemaining > 0) {
                ctx.unknownColorlessRemaining--;
                return;
            }
            if (STARTER_CARDS.contains(normalizedName) && ctx.unknownUpgradeStarterRemaining > 0) {
                ctx.unknownUpgradeStarterRemaining--;
                return;
            }
            Card baseCard = BattleBuilderJsonReader.lookupCard(normalizedName);
            if (baseCard.cardType == Card.ATTACK && baseCard.cardName.endsWith("+") && ctx.unknownUpgradeAttackRemaining > 0) {
                ctx.unknownUpgradeAttackRemaining--;
                return;
            }
            if (baseCard.cardType == Card.SKILL && baseCard.cardName.endsWith("+") && ctx.unknownUpgradeDefendRemaining > 0) {
                ctx.unknownUpgradeDefendRemaining--;
                return;
            }
            if (ctx.unknownUpgradedCardRemaining > 0) {
                ctx.unknownUpgradedCardRemaining--;
                return;
            }
            if (ctx.unknownNonCurseRemaining > 0) {
                ctx.unknownNonCurseRemaining--;
                return;
            }
            if (ctx.unknownAnyCardRemaining > 0) {
                ctx.unknownAnyCardRemaining--;
                return;
            }
        }
        System.err.println("[RunDataParser] Card to remove not found in deck: \"" + normalizedName + "\"");
    }

    private void upgradeCardInDeck(List<Card> deck, String normalizedBaseName, int floor, ParseContext ctx) {
        Card baseCard = BattleBuilderJsonReader.lookupCard(normalizedBaseName);
        if (normalizedBaseName.startsWith("Searing Blow")) {
            var name = normalizedBaseName;
            if (ctx.eventAtFloor.get(floor) != null) {
                for (Card card : deck) {
                    if (card != null && card.cardName.startsWith("Searing Blow")) {
                        name = card.cardName;
                        break;
                    }
                }
            }
            if (name.length() == 12) {
                baseCard = new CardIronclad.SearingBlow(0);
            } else if (name.length() == 13) {
                baseCard = new CardIronclad.SearingBlow(1);
            } else {
                baseCard = new CardIronclad.SearingBlow(Integer.parseInt(name.substring(13)));
            }
        }
        if (baseCard == null) {
            System.err.println("[RunDataParser] Unknown card to upgrade: \"" + normalizedBaseName + "\"");
            return;
        }
        Card upgraded = baseCard.getUpgrade();
        if (upgraded == null) {
            System.err.println("[RunDataParser] No upgrade found for: \"" + normalizedBaseName + "\"");
            return;
        }
        for (int i = 0; i < deck.size(); i++) {
            if (deck.get(i) != null && deck.get(i).cardName.equals(baseCard.cardName)) {
                deck.set(i, upgraded);
                return;
            }
        }
        if (ctx.eventRemovalRemaining > 0) {
            ctx.eventRemovalRemaining--;
            return;
        }
        if (ctx.unknownNonCurseRemaining > 0) {
            ctx.unknownNonCurseRemaining--;
            deck.add(upgraded);
            return;
        }
        if (isRareCard(normalizedBaseName) && ctx.unknownRareRemaining > 0) {
            ctx.unknownRareRemaining--;
            deck.add(upgraded);
            return;
        }
        if (normalizedBaseName.startsWith("Searing Blow") && (ctx.unknownUpgradeAttackRemaining > 0 || ctx.unknownAnyCardRemaining > 0)) {
            deck.add(upgraded);
            return;
        }
        System.err.println("[RunDataParser] Card to upgrade not found in deck: \"" + normalizedBaseName + "\"");
    }

    private void addRelicToList(List<Relic> relics, Set<String> relicStrings, String relicName) {
        if (SKIP_RELICS.contains(relicName)) {
            return;
        }
        relicStrings.add(relicName);
        Relic relic = BattleBuilderJsonReader.instantiateRelic(relicName);
        if (relic != null) {
            relics.add(relic);
        } else if (!BattleBuilderJsonReader.isNoImplRelic(relicName)) {
            System.err.println("[RunDataParser] Unknown relic: \"" + relicName + "\"");
        }
    }

    // ----------------------------- Predicates ----------------------------

    private boolean isStrikeOrDefend(String cardName) {
        return cardName.equals("Strike") || cardName.equals("Defend") ||
               cardName.equals("Strike+") || cardName.equals("Defend+");
    }

    private boolean isCurseCard(String normalizedName) {
        Card card = BattleBuilderJsonReader.lookupCard(normalizedName);
        return card != null && card.cardType == Card.CURSE;
    }

    private boolean isRareCard(String normalizedName) {
        Card card = BattleBuilderJsonReader.lookupCard(normalizedName);
        return card != null && card.rarity == Card.RARE;
    }

    private boolean isColorlessCard(String normalizedName) {
        Card card = BattleBuilderJsonReader.lookupCard(normalizedName);
        if (card == null) return false;
        return card.getClass().getEnclosingClass() == CardColorless.class;
    }

    // ----------------------------- Name normalization ----------------------

    // Cards/Relics/Potions may have wrong name due to historical or technical reasons, normalize them
    private static final Map<String, String> CARD_OLD_NAME = new HashMap<>();
    private static final Map<String, String> RELIC_OLD_NAME = new HashMap<>();
    private static final Map<String, String> POTION_OLD_NAME = new HashMap<>();
    private static final Set<String> SKIP_RELICS = Set.of(
            "Bottled Flame", "Bottled Lightning", "Bottled Tornado", "Dodecahedron"
    );
    private static final Set<String> BOSS_RELICS = Set.of(
            "Astrolabe", "Busted Crown", "Coffee Dripper", "Cursed Key", "Ectoplasm",
            "Fusion Hammer", "Pandora's Box", "Philosopher's Stone", "Runic Dome",
            "Runic Pyramid", "Sacred Bark", "Slaver's Collar", "Snecko Eye", "Sozu",
            "Velvet Choker", "Tiny House"
    );
    private static final Set<String> SKIP_CARDS = Set.of(
            "Undo", "Redo", "Venomology", "Vengeance", "Adaptation"
    );
    private static final Set<String> STARTER_CARDS = Set.of(
            "Strike", "Strike+", "Defend", "Defend+",
            "Bash", "Bash+",
            "Neutralize", "Neutralize+", "Survivor", "Survivor+",
            "Zap", "Zap+", "Dualcast", "Dualcast+",
            "Eruption", "Eruption+", "Vigilance", "Vigilance+"
    );
    private static final Set<String> SKIP_POTIONS = Set.of(
    );

    static {
        loadAltNames("/sts_cards.json", CARD_OLD_NAME);
        loadAltNames("/sts_relics.json", RELIC_OLD_NAME);
        loadAltNames("/sts_potions.json", POTION_OLD_NAME);

        // Relic run-data names that differ from registry names (not derivable by alt-name normalization)
        RELIC_OLD_NAME.put("Paper Frog", "Paper Phrog");
        RELIC_OLD_NAME.put("Snake Skull", "Snecko Skull");
        RELIC_OLD_NAME.put("Boot", "The Boot");
        RELIC_OLD_NAME.put("Paper Crane", "Paper Krane");
        RELIC_OLD_NAME.put("DollysMirror", "Dolly's Mirror");
        RELIC_OLD_NAME.put("Cables", "Gold-Plated Cables");
        RELIC_OLD_NAME.put("Sling", "Strike Dummy");
        RELIC_OLD_NAME.put("Yang", "Duality");
        RELIC_OLD_NAME.put("WingedGreaves", "Wing Boots");
        RELIC_OLD_NAME.put("GremlinMask", "Gremlin Visage");
        RELIC_OLD_NAME.put("CultistMask", "Cultist Headpiece");
        RELIC_OLD_NAME.put("Nloth's Gift", "N'loth's Gift");
        RELIC_OLD_NAME.put("NlothsMask", "N'loth's Hungry Face");

        // Potion run-data keys whose canonical names differ from the registry display names
        POTION_OLD_NAME.put("FairyPotion", "Fairy in a Bottle");
        POTION_OLD_NAME.put("Elixir Potion", "Elixir");
        POTION_OLD_NAME.put("SteroidPotion", "Flex Potion");
        POTION_OLD_NAME.put("ElixirPotion", "Elixir");

        CARD_OLD_NAME.put("Crippling Poison", "Crippling Cloud");
        CARD_OLD_NAME.put("Wraith Form v2", "Wraith Form");
        CARD_OLD_NAME.put("Gash", "Claw");
        CARD_OLD_NAME.put("Conserve Battery", "Charge Battery");
        CARD_OLD_NAME.put("Steam", "Steam Barrier");
        CARD_OLD_NAME.put("Steam Power", "Steam Barrier");
        CARD_OLD_NAME.put("Underhanded Strike", "Sneaky Strike");
        CARD_OLD_NAME.put("Ghostly", "Apparition");
        CARD_OLD_NAME.put("Judgement", "Judgment");
        CARD_OLD_NAME.put("Lockon", "Bullseye");
        CARD_OLD_NAME.put("ClearTheMind", "Tranquility");
        CARD_OLD_NAME.put("PathToVictory", "Pressure Points");
        CARD_OLD_NAME.put("Wireheading", "Foresight");
        CARD_OLD_NAME.put("Fasting2", "Fasting");
        CARD_OLD_NAME.put("Night Terror", "Nightmare");
    }

    private static void loadAltNames(String file, Map<String, String> nameMap) {
        try (InputStream is = BattleBuilderJsonReader.class.getResourceAsStream(file)) {
            if (is == null) return;
            JsonNode arr = new ObjectMapper().readTree(is);
            for (JsonNode entry : arr) {
                JsonNode nameNode = entry.get("name");
                if (nameNode == null) continue;
                String name = nameNode.asText();
                String potentialAltName = name.replace("'", "").replace("-", " ").toLowerCase();
                nameMap.put(potentialAltName, name);
                potentialAltName = name.replace("'", "").replace(" ", "").replace("-", "").toLowerCase();
                nameMap.put(potentialAltName, name);
            }
        } catch (Exception e) {
            System.err.println("[RunDataParser] Could not load data: " + e.getMessage());
        }
    }

    /**
     * Converts a run-data card name to the display name used in the internal registry.
     * <ul>
     *   <li>Strips character-color suffixes: {@code _R} (Ironclad), {@code _G} (Silent),
     *       {@code _B} (Defect), {@code _P} (Watcher).</li>
     *   <li>Replaces the {@code +1} upgrade indicator with {@code +}.</li>
     * </ul>
     * Examples: {@code "Strike_R"} -> {@code "Strike"},
     *           {@code "Bash+1"} -> {@code "Bash+"},
     *           {@code "Defend_R+1"} -> {@code "Defend+"}.
     */
    static String normalizeCardName(String runDataName) {
        String name = runDataName.trim();
        boolean upgraded = name.endsWith("+1");
        if (upgraded) {
            name = name.substring(0, name.length() - 2);
        }
        for (String suffix : new String[]{"_R", "_G", "_B", "_P"}) {
            if (name.endsWith(suffix)) {
                name = name.substring(0, name.length() - 2);
                break;
            }
        }
        if (CARD_OLD_NAME.containsKey(name)) {
            name = CARD_OLD_NAME.get(name);
        } else if (CARD_OLD_NAME.containsKey(name.toLowerCase())) {
            name = CARD_OLD_NAME.get(name.toLowerCase());
        }
        if (SKIP_CARDS.contains(name)) {
            return null;
        }
        if (upgraded) {
            name = name + "+";
        }
        return name;
    }

    /**
     * Converts a run-data relic name to the display name used in the internal registry.
     * <ul>
     *   <li>Strips a trailing {@code " 2"} suffix (e.g. {@code "Molten Egg 2"} -> {@code "Molten Egg"}).</li>
     *   <li>Applies alt-name aliases loaded from {@code sts_relics.json}, plus explicit renames
     *       (e.g. {@code "Paper Frog"} -> {@code "Paper Phrog"}, {@code "Snake Skull"} -> {@code "Snecko Skull"})
     *       stored in {@link #RELIC_OLD_NAME}.</li>
     *   <li>Handles camelCase run-data keys (e.g. {@code "PreservedInsect"}) by splitting them
     *       into words before the lookup.</li>
     * </ul>
     */
    static String normalizeRelicName(String runDataName) {
        String name = runDataName.trim();
        if (name.endsWith(" 2")) {
            name = name.substring(0, name.length() - 2);
        }
        if (RELIC_OLD_NAME.containsKey(name)) {
            name = RELIC_OLD_NAME.get(name);
        } else if (RELIC_OLD_NAME.containsKey(name.toLowerCase())) {
            name = RELIC_OLD_NAME.get(name.toLowerCase());
        }
        if (SKIP_RELICS.contains(name)) {
            return null;
        }
        return name;
    }

    /**
     * Converts a run-data potion key to the display name used in the internal registry.
     * Handles camelCase keys (e.g. {@code "PoisonPotion"}) and known aliases
     * (e.g. {@code "FairyPotion"} -> {@code "Fairy in a Bottle"}).
     */
    static String normalizePotionName(String runDataName) {
        String name = runDataName.trim();
        if (POTION_OLD_NAME.containsKey(name)) {
            name = POTION_OLD_NAME.get(name);
        } else if (POTION_OLD_NAME.containsKey(name.toLowerCase())) {
            name = POTION_OLD_NAME.get(name.toLowerCase());
        }
        if (SKIP_POTIONS.contains(name)) {
            return null;
        }
        return name;
    }

    // ----------------------------- Starting state -------------------------

    private static List<Card> getStartingDeck(CharacterEnum character) {
        List<Card> deck = new ArrayList<>();
        Card strike = BattleBuilderJsonReader.lookupCard("Strike");
        Card defend = BattleBuilderJsonReader.lookupCard("Defend");
        switch (character) {
            case IRONCLAD -> {
                for (int i = 0; i < 5; i++) deck.add(strike);
                for (int i = 0; i < 4; i++) deck.add(defend);
                deck.add(BattleBuilderJsonReader.lookupCard("Bash"));
            }
            case SILENT -> {
                for (int i = 0; i < 5; i++) deck.add(strike);
                for (int i = 0; i < 5; i++) deck.add(defend);
                deck.add(BattleBuilderJsonReader.lookupCard("Neutralize"));
                deck.add(BattleBuilderJsonReader.lookupCard("Survivor"));
            }
            case DEFECT -> {
                for (int i = 0; i < 4; i++) deck.add(strike);
                for (int i = 0; i < 4; i++) deck.add(defend);
                deck.add(BattleBuilderJsonReader.lookupCard("Zap"));
                deck.add(BattleBuilderJsonReader.lookupCard("Dualcast"));
            }
            case WATCHER -> {
                for (int i = 0; i < 4; i++) deck.add(strike);
                for (int i = 0; i < 4; i++) deck.add(defend);
                deck.add(BattleBuilderJsonReader.lookupCard("Eruption"));
                deck.add(BattleBuilderJsonReader.lookupCard("Vigilance"));
            }
        }
        deck.add(BattleBuilderJsonReader.lookupCard("Ascender's Bane"));
        return deck;
    }

    private static Relic getStarterRelic(CharacterEnum character) {
        String name = switch (character) {
            case IRONCLAD -> "Burning Blood";
            case SILENT   -> "Ring of the Snake";
            case DEFECT   -> "Cracked Core";
            case WATCHER  -> "Pure Water";
        };
        Relic relic = BattleBuilderJsonReader.instantiateRelic(name);
        if (relic == null) {
            throw new IllegalStateException("Starter relic not found for: " + character);
        }
        return relic;
    }

    // ----------------------------- Utility --------------------------------

    private static boolean hasPotionBelt(List<Relic> relics) {
        for (Relic r : relics) {
            if (r instanceof Relic.PotionBelt) return true;
        }
        return false;
    }

    private static CharacterEnum parseCharacter(String name) {
        return switch (name.toUpperCase()) {
            case "IRONCLAD" -> CharacterEnum.IRONCLAD;
            case "THE_SILENT", "SILENT" -> CharacterEnum.SILENT;
            case "DEFECT" -> CharacterEnum.DEFECT;
            case "WATCHER" -> CharacterEnum.WATCHER;
            default -> throw new IllegalArgumentException("Unknown character: \"" + name + "\"");
        };
    }

    private static int[] toIntArray(JsonNode node) {
        if (node == null || node.isNull()) return new int[0];
        int[] arr = new int[node.size()];
        for (int i = 0; i < node.size(); i++) {
            arr[i] = node.get(i).asInt();
        }
        return arr;
    }

    private static List<Integer> toIntList(JsonNode node) {
        List<Integer> list = new ArrayList<>();
        if (node == null || node.isNull()) return list;
        for (JsonNode n : node) {
            list.add(n.asInt());
        }
        return list;
    }

    private static List<String> toStringList(JsonNode node, Function<String, String> mapper) {
        List<String> list = new ArrayList<>();
        if (node == null || node.isNull()) return list;
        for (JsonNode n : node) {
            if (mapper != null) {
                var name = mapper.apply(n.asText());
                if (name != null) list.add(name);
            } else {
                list.add(n.asText());
            }
        }
        return list;
    }
}
