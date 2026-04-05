# Battle Definition JSON

## Overview

`BattleBuilderJsonReader` accepts a JSON string describing a battle setup and
returns a configured `GameStateBuilder`. `BattleBuilderJsonWriter` is the inverse:
it serializes a `GameStateBuilder` back to the same JSON format, enabling
run-data builders to be sent to the STS mod.

In phase 1, only player configuration, deck, relics, and potions are wired;
enemies and scenarios are not included.

---

## JSON Schema

```json
{
  "battle_definition": {
    "character":        "Ironclad",
    "player_health":    50,
    "player_max_health": 62,
    "deck":    ["Bash", "Strike", "Defend", "Bash+"],
    "relics":  ["Akabeko"],
    "potions": ["Attack Potion"]
  }
}
```

### Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `character` | string | Yes | One of `"Ironclad"`, `"Silent"`, `"Defect"`, `"Watcher"` (case-insensitive) |
| `player_health` | integer | Yes | Starting HP. Must be ≥ 1 and ≤ `player_max_health` |
| `player_max_health` | integer | Yes | Maximum HP. Must be ≥ 1 |
| `deck` | array of strings | Yes | Card names to include in the starting deck. Duplicates are allowed |
| `relics` | array of strings | No | Relic display names to equip |
| `potions` | array of strings | No | Potion display names to give to the player |

---

## Card Names

Card names must exactly match the `cardName` field in the corresponding Java
class. The easiest reference is to look at `sts_cards.json` in the `stsParse`
repository — the `name` field there corresponds directly to what the JSON reader
expects.

**Examples:**
- `"Bash"` — Ironclad basic attack
- `"Bash+"` — upgraded version (trailing `+` suffix denotes upgraded cards)
- `"Body Slam"` — multi-word card names include spaces
- `"Strike"`, `"Defend"` — shared starter cards available for all characters

**Upgraded cards:** append `+` to the base name. Not all cards have an upgraded
variant tracked in the simulation (e.g., `"Searing Blow"` does not support
`"Searing Blow+"` in the current implementation).

---

## Relic Names

Relic names use the same display name that the `Relic.toString()` method
produces: each uppercase letter in the class name is preceded by a space
(via `Utils.camelCaseToDisplayString`). Lookup is **case-insensitive**, so
both `"Akabeko"` and `"akabeko"` are accepted.

**Examples:**
- `"Akabeko"` → `Relic.Akabeko`
- `"Bag Of Preparation"` → `Relic.BagOfPreparation`
- `"Art Of War"` → `Relic.ArtOfWar`

Only relics with a no-argument constructor are supported. Scenario-gated relics
(those normally equipped only in specific randomization scenarios) can be added
but may have no effect if the corresponding scenario is not active.

---

## Potion Names

Potion names follow the same camelCase-to-display-string convention as relics.
Lookup is **case-insensitive**.

**Examples:**
- `"Attack Potion"` → `Potion.AttackPotion`
- `"Fear Potion"` → `Potion.FearPotion`
- `"Block Potion"` → `Potion.BlockPotion`

---

## Validation

`BattleBuilderJsonReader.validate(String json)` returns a `ValidationResult`
record with:
- `valid` — `true` if the definition is well-formed
- `error` — a human-readable error message when `valid` is `false`
- `ignoredItems` — a list of cards/relics/potions that were accepted but skipped
  because they are marked `no_need_to_implement` in the data files (only present
  when `valid` is `true` and at least one item was ignored)

Common validation errors:
- Missing top-level `battle_definition` key
- Missing required field (`character`, `player_health`, `player_max_health`, `deck`)
- `player_health` exceeds `player_max_health`
- Unknown card / relic / potion name

### no_need_to_implement

Items in `sts_cards.json`, `sts_relics.json`, or `sts_potions.json` may carry a
`no_need_to_implement` field (any non-null value). When such an item appears in a
submitted deck/relics/potions list, validation succeeds and the item is reported in
`ignoredItems` rather than causing an error. `BattleBuilderJsonReader.fromJson()`
silently skips those items when building the `GameStateBuilder`.

---

## Java API

```java
// Parse JSON into a GameStateBuilder (no enemies configured)
GameStateBuilder builder = BattleBuilderJsonReader.fromJson(jsonString);

// Validate without building
BattleBuilderJsonReader.ValidationResult result = BattleBuilderJsonReader.validate(jsonString);
if (!result.valid()) {
    System.err.println(result.error());
} else if (!result.ignoredItems().isEmpty()) {
    System.out.println("Ignored (not implemented): " + result.ignoredItems());
}

// Serialize a GameStateBuilder back to a battle definition JSON string
String json = BattleBuilderJsonWriter.toJson(builder);
```

---

## Limitations (Phase 1)

- Enemies are **not** part of the JSON format. After obtaining a `GameStateBuilder`
  from the reader, the caller must add enemies via `builder.addEnemyEncounter(...)`.
- Scenarios and pre-battle randomization are not configurable via JSON.
- Cards that require constructor parameters beyond their defaults (e.g.,
  `SearingBlow` with arbitrary upgrade counts) use the default parameterization
  from `CardManager`.
