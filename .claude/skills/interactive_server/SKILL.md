---
name: interactive-server
description: start a server that can receive commands to an interactive session running a game simulation and return the output, used for debugging and testing of newly developed features
---
# Interactive Server Skill

Use this skill when debugging game states, testing AI behavior, or exploring the Slay the Spire game simulation interactively.

## Overview

The interactive server provides an HTTP JSON API for executing interactive mode commands against the AlphaStS game simulation. This allows programmatic control of the game state for debugging and testing.

## Server Management

Use `interactive_server.py` to manage the server:

```bash
# Start the server in the background
python3 interactive_server.py start

# Start on a custom port
python3 interactive_server.py start --port 8000

# Check if server is running
python3 interactive_server.py status

# Stop the server
python3 interactive_server.py stop
```

## API Usage

The server exposes a single endpoint:

**POST http://localhost:7999/execute**

Request body (JSON):
```json
{
  "commands": ["cmd1", "cmd2", ...],
  "sessionId": "optional-session-id"
}
```

Response body (JSON):
```json
{
  "success": true,
  "output": "output of last command",
  "sessionId": "session-uuid"
}
```

### Session Management

- Omit `sessionId` to create a new session
- Include `sessionId` to continue an existing session with accumulated command history
- Sessions maintain state across requests

## Example Workflow

1. Start the server:
   ```bash
   python3 interactive_server.py start
   ```

2. Send commands via curl or HTTP client:
   ```bash
   # Create session and get game state
   curl -X POST http://localhost:7999/execute \
     -H "Content-Type: application/json" \
     -d '{"commands": ["i"]}'

   # Continue session with more commands
   curl -X POST http://localhost:7999/execute \
     -H "Content-Type: application/json" \
     -d '{"commands": ["a"], "sessionId": "your-session-id"}'
   ```

3. Stop when done:
   ```bash
   python3 interactive_server.py stop
   ```

## Available Commands

See `interactive_cli_commands.md` for the full list of available commands. Key commands include:

- `i` - Show current game state
- `a` - Show deck contents
- `s` - Show discard pile
- `n <count>` - Run MCTS search
- `nn <count>` - Run neural network analysis
- `<number>` - Execute action by index
- `e` - End turn
- `b` - Go back one state
- `reset` - Clear search tree

## Files

- `interactive_server.py` - Server management script
- `interactive_cli_commands.md` - Full command documentation
- `.interactive_server.pid` - PID file (auto-managed)
- `.interactive_server.log` - Server log output

## Debugging Tips

1. Check the log file if the server fails to start:
   ```bash
   cat .interactive_server.log
   ```

2. Use `rng off` command for deterministic behavior when debugging

3. Use `states` command to see the full action history

4. Use `save`/`load` commands to preserve interesting game states
