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
| `RunDataParser` | `com.alphaStS.test` | Reads a JSON array of run-data objects; returns `Iterator<GameStateBuilder>` |
| `BattleLoaderMod` | `battleloadermod` (repo: `sts_battle_loader_mod`) | STS mod: TCP server on port 2345 that applies a battle definition JSON and restarts the current battle |

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
| `.log` format and logging mod (step 3) | TODO | Log schema (state/action/RNG format) not yet defined or implemented |
| Log replay + state assertions (step 4) | TODO | Replay logic and state comparison not yet implemented |
