# CLI Commands Documentation

This document describes all available commands in the CLI of the AlphaStS AI agent.

### Game Control Commands

e.g.



#### Basic Navigation

- **`exit`** - Exit interactive mode
- **`e`** or **`End Turn`** - End the current turn. Executes the END_TURN action, then automatically executes the BEGIN_TURN action for the next turn
- **`b`** - Undo the last action, restoring the previous game state. Can be used repeatedly to undo multiple actions
- **`i`** - Reprint the current game state display (enemies, player stats, hand, available actions)

#### Action Execution

- **`<number>`** - Execute a specific action by its index number (e.g., `0`, `1`, `2`). The available actions and their indices are displayed after each state change
- **`<action_name>`** - Execute an action by typing its name. Uses fuzzy matching, so partial or approximate names work (e.g., typing `strike` may match `Strike+`)

#### Stopping a Search

- **`stop`** - While any search is running (`n`, `nn`, `games`, etc.), type `stop` to interrupt the search early and return to the prompt with partial results

### Information Display Commands

#### Game State Information

- **`a`** - Show all cards in the draw pile with their counts
- **`s`** - Show all cards in the discard pile with their counts
- **`x`** - Show all cards in the exhaust pile with their counts
- **`states`** - Display the complete action history, showing each action taken at each step. Groups actions by turns, with separators at stochastic events and turn boundaries

#### History and Session Management

- **`hist`** - Display the command history for the current session. Filtered to exclude search/analysis commands (`n`, `nn`, `games`, `save`, `load`, `hist`)
- **`save [suffix]`** - Save the current session to a file
  - `save` - Save to default file (`<saveDir>/session.txt`)
  - `save <suffix>` - Save to a suffixed file (e.g., `save test1` saves to `<saveDir>/sessiontest1.txt`)
  - `save play` - Output a Java `List.of(...)` expression containing the filtered game actions from post-pre-battle onward, suitable for pasting into test code. Excludes comments and seed commands
- **`load [suffix]`** - Load a previously saved session file and replay all commands from it
  - `load` - Load from default file
  - `load <suffix>` - Load from suffixed file

### AI and Search Commands

#### MCTS (Monte Carlo Tree Search)

- **`n <count> [options]`** - Run MCTS search for the specified number of nodes

  | Option | Description | Default |
  |--------|-------------|---------|
  | `t=<n>` | Number of threads | 1 |
  | `b=<n>` | Batch size for NN inference | 1 |
  | `prune` | Enable smart pruning (stops searching subtrees early when remaining nodes are insufficient to change the best action) | off |
  | `a=<n>` | Force a specific root action index (only search the subtree rooted at this action) | none |

  **Example:** `n 5000 t=4 b=8 prune` runs a 5000-node search with 4 threads, batch size 8, and smart pruning enabled.

  After the search completes, displays the root state summary including Q-values and memory usage.

- **`reset`** - Clear the entire search tree and generate a new random seed for search. Use this before starting a fresh analysis from the same state

#### Neural Network Principal Variation (NN PV)

- **`nn <count> [options]`** - Compute the best sequence of moves (principal variation) by running MCTS at each step and following the best action until a stochastic event or terminal state is reached. If `<count>` is omitted, reuses the last count (default: 1000)

  | Option | Description | Default |
  |--------|-------------|---------|
  | `t=<n>` | Number of threads | 1 |
  | `b=<n>` | Batch size | 1 |
  | `noPrune` | Disable smart pruning (pruning is on by default for nn) | off |
  | `clear` | Clear search tree between moves (forces fresh search at each step) | off |

  Displays each move with Q-values (combined, win rate, health) and node counts. Automatically stops at chance nodes (stochastic events) or terminal states.

  During the search, periodically prints progress and the current best line of play.

- **`nn exec`** - Execute (play out) the previously computed principal variation, applying all moves to the game state
- **`nn execv`** - Execute the principal variation excluding the last move

### Random Number Generation

- **`rng off`** - Disable automatic random number generation. When off, the game prompts the user to manually choose outcomes for all random events (card draws, enemy intents, enemy health rolls, etc.), giving full control over game outcomes
- **`rng on`** - Re-enable automatic random number generation (default behavior)
- **`seed <a> <b>`** - Set specific random seeds: `a` for game RNG, `b` for search RNG. Automatically recorded in history after searches to allow reproduction of specific game sequences

### Game Simulation (`games`)

- **`games [options]`** - Run multiple simulated games from the current state for performance analysis

  | Option | Description | Default |
  |--------|-------------|---------|
  | `n=<n>` | MCTS node count per move | 500 |
  | `c=<n>` | Number of games to simulate | 100 |
  | `t=<n>` | Number of threads | 1 |
  | `b=<n>` | Batch size | 1 |
  | `r=<n>` | Fix a specific randomization scenario index | all |
  | `a=<n>` | Starting action index | none |
  | `dmg` | Print damage distribution across all scenarios | off |
  | `write` | Write match log to `matches_interactive.txt.gz` | off |

  **Example:** `games n=200 c=50 t=4 b=8 dmg` runs 50 games with 200 nodes per move using 4 threads, batch size 8, and prints damage distribution.

### Comments

- **`#<text>`** - Add a comment. The comment is stored in history and preserved in regular `save`, but filtered out of `save play` output
- **`##<text>`** - Add a persistent comment. Stored twice in history for emphasis. Preserved in regular `save`, filtered from `save play`

### Empty Command

- Pressing **Enter** on an empty line is a no-op and returns to the prompt

---

## Command Menu (`c`)

The `c` command opens an interactive menu for game state modification and diagnostic operations. Select an option by number.

### Menu Options

1. **Set Draw Order** - Manually set the order of cards to be drawn from the draw pile (see details below)
2. **Set Enemy Health** - Set an enemy's current health
3. **Set Enemy Health Original** - Set an enemy's original/max health in the current battle
4. **Set Enemy Move** - Set an enemy's next move
5. **Set Enemy Other** - Set enemy-specific properties (see details below)
6. **Set Player Health** - Set the player's current health
7. **Set Potion Utility** - Configure potion usability and utility values
8. **Remove Card From Hand** - Remove a specific card from the current hand (by position)
9. **Add Card To Hand** - Add any card from the card dictionary to the current hand
10. **Discard Card From Hand (If Exists)** - Move a specific card from hand to discard pile, if it exists in hand
11. **Add Card To Deck** - Add any real card to the draw pile
12. **Diagnostic** - Opens the diagnostic submenu (see below)
0. **Exit** - Return to main prompt

### Set Draw Order (`c` > `1`)

Opens an interactive editor for the draw order queue. The draw order determines which cards will be drawn next (overriding the normal random shuffle).

All cards in the card dictionary are listed with their indices. The current draw order is displayed as a bracket-delimited list.

**Sub-commands within the draw order editor:**
- **`<number>`** - Add the card at that index to the end of the draw order
- **`<card_name>`** - Add a card by name using fuzzy matching. Searches the draw pile first, then discard pile, then all cards
- **`p`** - Pop (remove) the last card from the draw order
- **`clear`** - Clear the entire draw order
- **`e`** - Apply the edited draw order and exit
- **`b`** - Cancel and return to the previous menu without applying changes

### Set Enemy Health (`c` > `2`)

Prompts to select an enemy (all enemies shown, including dead ones), then prompts for a health value.
- Setting health to **0** kills the enemy
- Setting health to a **positive value** on a dead enemy revives it with that health

### Set Enemy Health Original (`c` > `3`)

Sets the enemy's max health for the current battle. Also adjusts current health proportionally based on the change. Has special handling for Large Spike Slime (adjusts split max health).

### Set Enemy Move (`c` > `4`)

Prompts to select an alive enemy, lists all possible moves for that enemy, then prompts to select a move by index or by name (fuzzy matching supported).

### Set Enemy Other (`c` > `5`)

Prompts to select any enemy (including dead ones), then shows enemy-specific properties that can be modified:

- **Red/Green Louse:**
  - `0` - Set Curl-Up amount
  - `1` - Set base damage value (-1 to reset)
- **Darkling:**
  - Set Nip Damage as a single value (exact) or a range (`lower-upper`). Enter `0` to reset to default (7-11)
- **Other enemies:** "Nothing to change."

### Set Potion Utility (`c` > `7`)

If multiple potions are available, prompts to select one. Then prompts for a utility value:
- **`0`** - Disables the potion (makes it unusable)
- **Any positive value** - Sets the potion's utility threshold to that value (higher = more willing to use)

### Diagnostic Submenu (`c` > `12`)

1. **Print Search Tree** - Print the MCTS search tree structure. Prompts for tree depth (empty for default depth of 3). See the **Print Tree** section below for details on the format
2. **Explore Search Tree** - Enter interactive tree exploration mode (see **Tree Explore Mode** below)
3. **NN PV Chance** - Analyze all possible outcomes of a chance node. Prompts for args in format `n=<nodes> a=<action> t=<threads> b=<batch>`. The `a` parameter (action index leading to the chance node) is required. Enumerates all possible random outcomes, runs NN PV on each, then groups them by resulting principal variation
4. **NN PV Volatility** - Run multiple NN PV trials with different random seeds to measure how stable the principal variation is. Prompts for args: `c=<trials> n=<nodes> t=<threads> b=<batch> clear`. Groups results by unique PV and shows frequency counts
5. **NN PV 2** - Alternative MCTS search that uses line-based search (explores full action sequences rather than individual actions). Prompts for node count. Shows top 10 most-visited lines with Q-values
6. **Print NN Input** - Print the raw neural network input array for the current state as a numeric array
7. **Switch Model** - Switch to a different neural network model by providing a new model directory path
8. **Print Battle Description** - Print a detailed description of the current battle state
0. **Exit** - Return to command menu

### Print Tree Format

The print tree command (`c` > `12` > `1`, or `tree` in tree explore mode) supports additional arguments:

```
tree [d=<depth>] [a=<action>] [f] [dag]
```

- **`d=<depth>`** - Maximum tree depth to print (default: 3)
- **`a=<action>`** - Print the subtree rooted at a specific action index
- **`f`** - Write the tree output to a file (`<modelDir>/tree.txt`) instead of the console
- **`dag`** - Output a Graphviz DOT format DAG to `<modelDir>/dag.dot` (also prints to console)

---

## Tree Explore Mode

Entered from the Diagnostic submenu (`c` > `12` > `2`). Allows interactive navigation of the MCTS search tree, stepping through nodes to inspect search results.

At each **GameState node**, the available actions that have been explored (have child nodes) are displayed. At each **ChanceState node**, the possible outcomes are displayed with their visit counts.

### Commands in Tree Explore Mode

- **`<number>`** or **`<action_name>`** - Navigate to the child node for that action/outcome (fuzzy matching supported)
- **`b`** - Go back to the parent node
- **`tree [args]`** - Print the search tree from the current node (supports depth/action/file/dag arguments)
- **`save <0-9>`** - Save the current navigation position to a slot (0-9)
- **`restore <0-9>`** - Restore a previously saved position
- **`exit`** - Exit tree explore mode

---

## Configuration Menu (`config`)

The `config` command opens an interactive toggle menu for MCTS and display settings. Each option toggles between On/Off. The current state is shown in parentheses. You can toggle multiple options before exiting.

1. **Progressive Widening** - Enable/disable progressive widening in MCTS
2. **Progressive Widening Improvements** - Enable/disable progressive widening improvements
3. **Progressive Widening Improvements 2** - Enable/disable progressive widening improvements (variant 2)
4. **Ban Transposition In Tree** - Enable/disable banning transposition in the search tree
5. **Flatten Policy As Nodes Increase** - Enable/disable policy flattening as node count increases
6. **Prioritize Chance Nodes Before Deterministic In Tree** - Enable/disable chance node prioritization
7. **Test New Feature** - Enable/disable test new feature flag
8. **State Description** - Enable/disable state description output
0. **Exit** - Return to main prompt

---

## Model Comparison Menu (`cmp`)

The `cmp` command opens an interactive menu for comparing two game states by running simulated games from each. This is used to evaluate which state/action leads to better outcomes.

### Workflow

1. Navigate to the first game state, then enter `cmp` and select **Set State 1** to capture it
2. Navigate to the second game state (e.g., undo with `b`, take a different action), then enter `cmp` and select **Set State 2**
3. Adjust comparison parameters as needed (options 4-8), then select **Run Comparison**

### Menu Options

1. **Set State 1** - Capture the current game state as state 1 (prompts for optional starting action)
2. **Set State 2** - Capture the current game state as state 2 (prompts for optional starting action)
3. **Run Comparison** - Run the comparison games (both states must be set first). Displays both states and asks for confirmation (`y`/`n`) before running
- **Comparison Parameters** (persisted across menu invocations):
4. **Set Node Count** - MCTS nodes per move during comparison games (default: 500)
5. **Set Number of Games** - Total games to simulate per state (default: 100)
6. **Set Number of Threads** - Number of parallel threads (default: 1)
7. **Set Batch Size** - Neural network inference batch size (default: 1)
8. **Set Randomization Scenario** - Fix a specific randomization scenario index, or empty for all scenarios (default: none)
0. **Exit** - Return to main prompt

### Starting Action Prompt

When setting state 1 or 2, you are prompted for a starting action:
- Press **Enter** for no starting action (MCTS chooses from scratch)
- Enter **`e`** to use end turn as the starting action
- Enter a **number** to use a specific action index as the starting action

---

## Command Aliases

Custom command aliases can be defined in the `alphaStS_alias.conf` file. Each alias maps a shortcut to one or more commands.

### Alias File Format

```
alias_name=command1,command2,command3
```

Commands are separated by commas. The first command is executed immediately, and the rest are queued for sequential execution. Lines starting with `#` are treated as comments. The alias file is loaded when interactive mode starts.

### Built-in Backward Compatibility Aliases

| Alias | Expands To | Description |
|-------|------------|-------------|
| `do`  | `c,1`      | Set Draw Order |
| `eh`  | `c,2`      | Set Enemy Health |
| `eho` | `c,3`      | Set Enemy Health Original |
| `em`  | `c,4`      | Set Enemy Move |
| `eo`  | `c,5`      | Set Enemy Other |
| `ph`  | `c,6`      | Set Player Health |
| `pot` | `c,7`      | Set Potion Utility |

---

## Game Replay Mode

A separate read-only mode for reviewing previously recorded games step by step. Entered when the agent is started with a recorded game (e.g., from training data or match logs).

At each step, the game state is displayed along with the action that was taken. Provides statistical analysis tools for evaluating the quality of moves.

### Commands

- **`n`** - Go to the next step in the game
- **`b`** - Go to the previous step
- **`g <index>`** - Jump to a specific step by its index number
- **`m`** - Enter Battle Interactive Mode at the current game state. Creates a modifiable copy of the state so you can try different actions or run searches. Exit the sub-session with `exit` to return to replay mode
- **`exit`** - Exit replay mode

### Analysis Commands

- **`analyze [options]`** - For every state in the game, play simulated games from that state and collect statistics (Q-value, death percentage, average damage, potion usage). Used to find critical actions, bad RNG, or mistakes by analyzing changes in these stats across steps

  | Option | Description | Default |
  |--------|-------------|---------|
  | `n=<n>` | MCTS node count per move | 500 |
  | `c=<n>` | Number of games to simulate per state | 100 |
  | `t=<n>` | Number of threads | 1 |
  | `b=<n>` | Batch size | 1 |

  Outputs a table with Q-value, death percentage, and average damage for each step. Also generates a Python plot command for visualizing the data.

- **`check [options]`** - For each action in the game, compare it against what MCTS would choose with a higher node count. Identifies steps where the network might have made a different (better) decision

  | Option | Description | Default |
  |--------|-------------|---------|
  | `n=<n>` | MCTS node count for re-evaluation | 500 |
  | `t=<n>` | Number of threads | 1 |
  | `b=<n>` | Batch size | 1 |

  Shows the original move sequence vs. the suggested better sequence for each identified discrepancy.

---

## Game State Display

When the game state is printed (at the start, after actions, or with `i`), it shows:

### Enemy Information (for each alive enemy)

- Name, HP (current/max), and any active status effects:
  - Strength, Block, Artifact, Vulnerable, Weak, Poison, Corpse Explosion, Choke, Regeneration, Metallicize, Gain Strength EOT
- Enemy-specific information (e.g., Curl Up amount, Mode Shift damage, Stasis card, Nip Damage range, Malleable, Slow, Intangible, Invincible, Beat of Death)
- Current move and last move (hidden if Runic Dome is active, showing last two moves instead)

### Player Information

- Energy, HP (with max possible HP if different), Block
- Orbs (for Defect, with Dark orb damage shown)
- Strength, Dexterity, Plated Armor, Focus
- Status effects: Vulnerable, Weak, Frail, Artifact
- Active buffs
- Counters and special states (Entangled, Cannot Draw Card, potion availability)

### Card Information

- Draw Order (if manually set)
- Deck Order (if fixed draws are set)
- Hand contents (with counts)
- Chosen cards (if in a selection context)
- Nightmare cards (if active)
- State description from previous action (if applicable)

### Available Actions

Listed with indices for selection. Also shows shortcuts for viewing deck (`a`), discard (`s`), and exhaust (`x`) piles with their card counts.

---

## Command Usage Notes

1. **Case Sensitivity**: Commands are case-sensitive and should be entered in lowercase (except `End Turn`)
2. **Fuzzy Matching**: Action names, card names, enemy names, and move names all support fuzzy matching, so partial or approximate names often work
3. **Interactive Prompts**: Many commands open interactive sub-prompts. Enter `b` to go back/cancel in most prompts
4. **History Filtering**: Search and analysis commands (`n`, `nn`, `games`, `save`, `load`, `hist`) are filtered out of saved session history to keep session files focused on game actions
5. **Search Tree Persistence**: The MCTS search tree persists between commands unless explicitly cleared with `reset`
6. **RNG Auto-Enable**: When running search commands (`n`, `nn`, `games`, `cmp`), RNG is automatically re-enabled for the duration of the search (even if `rng off` was set), and restored afterwards. Seeds are recorded in history after each search
7. **Model Directory**: The model directory is used for loading ONNX model files for neural network inference

---

## Common Workflows

### Basic Gameplay

1. Use `i` to see current state
2. Use `<number>` or `<action_name>` to make moves
3. Use `e` to end turn when ready
4. Use `b` to undo and try alternatives
5. Use `save` to preserve important game states

### AI Analysis

1. Use `n 1000` to run MCTS search with 1000 nodes
2. Use `nn 100` to see the neural network's preferred line of play
3. Use `nn exec` to execute the suggested line
4. Use `c` > `12` > `1` to examine the search tree structure
5. Use `c` > `12` > `2` to interactively explore the search tree
6. Use `reset` to clear search before trying different analyses

### Comparing Actions

1. Navigate to the decision point in the game
2. Use `cmp` > `1` to capture state 1, then set a starting action if desired
3. Use `cmp` > `2` to capture state 2 with a different starting action
4. Adjust parameters if needed (e.g., `cmp` > `4` to change node count)
5. Use `cmp` > `3` to run the comparison

### Debugging and Testing

1. Use `rng off` for full manual control over random events
2. Use `c` menu for state modification (health, enemy moves, cards, draw order)
3. Use `states` to see the sequence of actions taken
4. Use `b` to undo moves and try alternatives
5. Use `save play` to export reproducible test sequences

### Analyzing a Recorded Game

1. Enter game replay mode with a recorded game
2. Use `n`/`b` to step through the game, or `g <index>` to jump to a specific point
3. Use `analyze c=200 n=500 t=4 b=8` to evaluate every decision point
4. Use `check n=1000 t=4 b=8` to find moves where higher search might disagree
5. Use `m` to enter interactive mode at any interesting state for deeper analysis
