# Automated Random Testing

## Overview

The goal is to validate the game simulation against the real Slay the Spire as ground truth.
For each battle in a set of run data, a matching battle is constructed in a live STS instance,
random moves are played and logged, and the log is replayed through our `GameState` to verify
that every intermediate state matches.

---

## Pipeline

For each battle in the run data:

1. **Construct battle in STS** — using a mod, set up a battle with the exact deck, relics,
   potions, and enemies from the run data in a running Slay the Spire instance.

2. **Play random moves** — using the communication mod and a random-move bot with the player
   at 10 000 HP, make random moves until the battle ends (capped at ~30 turns).

3. **Log states and actions** — a mod logs every state, action, and RNG roll to a `.log` file.

4. **Replay and assert** — using the `.log`, construct a `GameState` for the same battle and
   replay every action from the start, asserting that the state matches after each step.

---

## Entry Points

| Class | Location | Purpose |
|---|---|---|
| `TestRunner` | `com.alphaStS.test` | Top-level driver: iterates run data and runs steps 1–4 per battle |
| `RunDataParser` | `com.alphaStS.test` | Reads a JSON array of run-data objects; returns `Iterator<BattleEntry>` |
| `BattleLoaderMod` | `battleloadermod` (repo: `sts_battle_loader_mod`) | STS mod: TCP server on port 2345 that applies a battle definition JSON and restarts the current battle |

---

## CLI Reference

All subcommands are invoked via `--replay-test <subcommand> ...`.

### `--parse-historical-data <path> [--filter <spec>]`

Parses a run data JSON file and prints a summary of each matching battle.

### `--generate-runs <path> [--filter <spec>] [--ip <host>] [--seed <long>] [--replay]`

Plays random moves in STS for each matching battle and saves `.run` log files to `tests/`.
`--replay` additionally validates each log against the Java simulation after it is saved.
`--seed` pins the bot's RNG to a fixed value so the same move choices are made given identical
game state; the seed is also stored in the run file header under `"seed"`.

### `--replay-run <path> [<path>...] [--verbose]`

Replays one or more saved `.run` log files. Each path may be a `.run` file or a directory;
directories are expanded to all `.run` files they contain (non-recursive, sorted). The battle
setup (`GameStateBuilder` and enemy encounter) is read directly from the header embedded when
each file was generated, so no historical data file is required.

### `--filter <spec>`

Selects which battles to process. `<spec>` is a comma-separated list of tokens of the form
`{run}:{battle}`, where the run part is:

| Syntax | Meaning |
|--------|---------|
| `*` | all runs |
| `N` | exactly run index N (0-based) |
| `N-M` | run indices N through M inclusive |
| `<play_id>` | run whose play_id matches this string |

And the battle part is:

| Syntax | Meaning |
|--------|---------|
| `*` | all battles |
| `N` | exactly battle index N (0-based) |
| `N-M` | battle indices N through M inclusive |

**Examples:**

```
--filter 0:*                              # all battles in run 0
--filter 1:2-5                            # battles 2, 3, 4, 5 in run 1
--filter *:0                              # first battle of every run
--filter 0-2:*,5:3                        # all battles in runs 0–2, plus battle 3 of run 5
--filter 251eb1e0-5bfe-4c74-...:0        # battle 0 of the run with that play_id
```

---

## Current Status

| Step | Status | Notes |
|---|---|---|
| `TestRunner` skeleton | Done | Steps 2–4 still have TODO stubs |
| `RunDataParser` | Done | Parses JSON array, stub `parseRunData()` returns empty builder |
| Parse run data into `GameStateBuilder` | TODO | `RunDataParser.parseRunData()` needs deck, relics, potions, enemies, player HP wired in |
| `GameStateBuilder` → JSON (`BattleBuilderJsonWriter`) | Done | Serializes player, deck, relics, potions to battle definition JSON |
| STS mod integration (step 1) | Done | `BattleLoaderMod` listens on port 2345; `TestRunner.sendBattleDefinition()` sends JSON and waits for "OK" |
| Communication mod + random bot (step 2) | TODO | Random-move bot and turn limit not yet implemented |
| `.log` format and logging mod (step 3) | Partial | `state:floor` and `action:*` logged; `event:shuffle` (deck order on reshuffle) via `EmptyDeckShuffleActionPatch`; `event:enemy_hp` (id/min/max/chosen HP per enemy) via `EnemyHpPatch` |
| Log replay + state assertions (step 4) | Done | `TestRunner.replayLog()` applies actions to `GameState`; `TestReplay.compareStateFloor` asserts energy, block, monster HP, exhaust pile; shuffle and enemy HP replay share a unified `Queue<TestReplayEvent>`; `RandomGenTest` validates bound and applies chosen HP on `RandomEnemyHealth` calls |
