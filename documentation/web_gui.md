# Web GUI

## Overview

`BattleBuilderGuiServer` starts an HTTP server on `localhost:4000` that serves an
interactive web interface for building battle definitions. Users can configure
the player character, HP, deck, relics, and potions through a point-and-click
interface, then submit the resulting JSON for validation.

---

## Starting the Server

```bash
# From the project root
java -cp agent/target/classes:<dependencies> com.alphaStS.Main --gui-server

# With options
java -cp ... com.alphaStS.Main --gui-server --port 8080
```

**Options:**

| Option | Default | Description |
|--------|---------|-------------|
| `--port <n>` | `4000` | Port to listen on |

Once started, open `http://localhost:4000/` in a browser.

---

## Interface Sections

### General Definition

Configure the player character and HP values.

- **Character** — dropdown for Ironclad, Silent, Defect, or Watcher. Changing
  the character updates the available cards and relics shown in the add modal.
- **HP** — current player health at the start of the battle.
- **Max HP** — player's maximum health.

### Deck

Shows all cards currently in the starting deck as a grid of card images.

- Click **+ Add** to open the card picker modal. Cards are filtered to the
  selected character plus Colorless cards.
- Click the **✕** overlay on any card image to remove it.
- Duplicate cards are allowed (e.g., five Strikes).

### Relics

Shows equipped relics. Click **+ Add** to open the relic picker, which shows
relics for the selected character plus "Any" relics.

### Potions

Shows equipped potions. All potions are available regardless of character.

---

## Adding Items

Clicking **+ Add** in any section header opens a modal dialog:

- A search box filters items by name in real time.
- Items are displayed as image tiles; click a tile to add it and close the modal.
- Press **Escape** or click outside the modal to dismiss it without adding anything.

---

## Submit & Validate

The **Submit & Validate** button:

1. Serialises the current configuration into the
   [Battle Definition JSON](./battle_definition_json.md) format.
2. Displays the generated JSON in the read-only text area at the bottom of the
   page (useful for copying into code or saving to a file).
3. POSTs the JSON to `/api/validate` on the server.
4. Shows a green **✓ Valid** badge on success, or a red **✗ error message**
   badge on failure.

---

## HTTP API

The server exposes the following endpoints:

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/` | Serves the GUI HTML page |
| `GET` | `/images/<path>` | Serves card/relic/potion images from the classpath (e.g., `/images/cards/Ironclad/Bash.png`) |
| `GET` | `/api/cards?character=<name>` | Returns the card list from `sts_cards.json`, filtered by character. Omit the parameter for all cards |
| `GET` | `/api/relics` | Returns the full relic list from `sts_relics.json` |
| `GET` | `/api/potions` | Returns the full potion list from `sts_potions.json` |
| `POST` | `/api/validate` | Validates a battle definition JSON body; returns `{"valid": true}` or `{"valid": false, "error": "..."}` |

All responses include `Access-Control-Allow-Origin: *` CORS headers.

---

## Image Sources

Card, relic, and potion images are served from the compiled classpath, which
corresponds to `agent/src/main/resources/` in the source tree:

```
agent/src/main/resources/
  cards/
    Ironclad/Bash.png
    Silent/Neutralize.png
    ...
  relics/
    Akabeko.png
    ...
  potions/
    AttackPotion.png
    ...
```

Image paths are taken directly from `sts_cards.json`, `sts_relics.json`, and
`sts_potions.json` (the `image_path` field). If an image is missing from the
classpath, a placeholder icon is displayed instead.

---

## Data Source

The card, relic, and potion lists served by the API are loaded at server startup
from the compiled classpath (`agent/src/main/resources/`). If the files are not
present on the classpath the API returns empty arrays; the server and validation
still function because `BattleDefinitionReader` uses the Java card registry
(populated from `CardManager` and reflection) independently of these JSON files.
