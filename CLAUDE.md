# README.md

This file provides guidance to AI Assistents when working with code in this repository.

## Project Overview

This is an AlphaZero-based AI agent for Slay the Spire, a card game. The project uses a hybrid architecture with Java for game simulation and Python for neural network training.

## Architecture

### Core Components

- **Java Agent** (`agent/`): Game simulation, MCTS search, and state management
  - `GameState.java`: Core game state representation and game logic
  - `MCTS.java`: Monte Carlo Tree Search implementation
  - `Main.java`: Entry point supporting multiple modes (interactive, training, server)
  - `card/`: Card implementations for all characters (Ironclad, Silent, Defect, Watcher)
  - `enemy/`: Enemy encounter definitions and behaviors
  - `model/`: Neural network interface and ONNX model execution

- **Python Training** (root): Neural network training and data processing
  - `ml.py`: Main training pipeline with TensorFlow/Keras
  - `interactive.py`: Python wrapper for interactive mode
  - `upload_server.py`: Model upload and management server

### Key Design Patterns

- **State-Action Architecture**: Game states contain all information needed for decision making
- **Multi-step Actions**: Complex actions (e.g., True Grit+) are decomposed into sequential choices
- **Transposition Handling**: MCTS uses game state hashing for efficient search
- **Stochastic Handling**: Random events are modeled as chance nodes in the search tree

## Development Commands

### Setup
```bash
# Java build
cd agent && mvn install

# Python environment
conda env create -f environment.yml
conda activate alphasts
```

### Running the Agent
```bash
# Interactive mode
python interactive.py
# or directly: java -cp <classpath> com.alphaStS.Main -i

# Training
python ml.py -t -c 20  # 20 iterations

# Single game test
java -cp <classpath> com.alphaStS.Main -pg

# Server mode
java -cp <classpath> com.alphaStS.Main --server
```

### Testing
```bash
cd agent && mvn test
```

## File Structure Notes

- `saves/` and `saves_*`: Training data and model checkpoints
- `environment.yml`: Conda environment with TensorFlow 2.10 and ONNX runtime
- `commands.txt`: Interactive mode command reference
- `agent/target/`: Maven build artifacts
- `*.onnx`: Neural network models in ONNX format

## Neural Network Architecture

- **Input**: Game state features (hand, deck, player/enemy HP, buffs, etc.)
- **Output**: Policy (action probabilities), value estimates (win rate, expected health)
- **Loss**: Weighted combination of policy cross-entropy and value MSE losses
- **Architecture**: Currently 2-layer fully connected feedforward network

## Interactive Mode Key Commands

- `n <count>`: Run MCTS search for `count` nodes
- `nn <count>`: Search until stochastic action, show best line
- `reset`: Clear search tree
- `save/load`: Session persistence
- `do`: Modify draw order for deterministic testing

## Training Pipeline

1. **Game Generation**: Java agent plays games using current model
2. **Data Collection**: States, actions, and outcomes saved to binary files
3. **Neural Network Training**: Python processes data and updates model
4. **Model Export**: TensorFlow model converted to ONNX for Java consumption
5. **Iteration**: Process repeats with improved model

## Randomization Control

- `RandomGen`: Centralized RNG for reproducible testing
- `rng off/on`: Toggle manual control of random events in interactive mode
- Seed management for consistent training runs
