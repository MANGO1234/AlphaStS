# Repository Guidelines

## Project Structure & Module Organization
AlphaStS couples Python training utilities with a Java battle agent. Core training scripts (`ml.py`, `ml_conv.py`, `interactive.py`) live in the repository root alongside helper modules (`misc.py`, `plot.py`, `stats.py`). The `agent/` directory contains the Maven-built Java client that exposes `com.alphaStS.Main`; Java test fixtures live under `agent/src/test/java`. Generated fight data is written to `saves/` (symlinked here as `saves_zzzl`), so keep temporary runs inside that tree. Configuration files such as `environment.yml` and `environment_gpu.yml` describe the supported Conda environments.

## Build, Test, and Development Commands
Create the Python environment with `conda env create -f environment.yml` (or `environment_gpu.yml` when targeting CUDA), then `conda activate alphasts`. Build the Java agent from `agent/` via `mvn install`, which populates `agent/target/classes`. Kick off training or evaluation with `python ml.py -t -c 20`, adjusting flags like `-gpu` or `-training-n` as needed. Use `python interactive.py` for manual inspection, and `python test_dist.py -server` to run the agent as a socket server for distributed play.

## Coding Style & Naming Conventions
Python modules follow PEP 8: 4-space indentation, `snake_case` for functions and variables, and module-level constants in `UPPER_CASE`. Favor explicit imports and keep TensorFlow/Keras session code in top-level scripts as shown in `ml.py`. Java sources stick with standard conventions—classes in `UpperCamelCase`, methods and fields in `lowerCamelCase`, and braces on the same line. There is no enforced formatter, so mirror surrounding style and document unusual logic inline.

## Testing Guidelines
Run the Java test suite with `mvn test` before submitting changes to the agent. Python-side validation is script-driven: `python test.py` checks binary exchanges with the agent, while `python test_dist.py -m` executes match simulations. When introducing new mechanics, add focused assertions either in JUnit or lightweight Python checks and capture deterministic seeds so failures are reproducible.

## Commit & Pull Request Guidelines
Git history favors concise, imperative summaries (e.g., `export gpu environment`, `try out training cpuct`). Squash noisy checkpoints locally before opening a PR. Every PR should explain the motivation, list relevant command flags or seeds needed to reproduce results, and include screenshots or log snippets if behavior changes. Link related issues when available and call out any follow-up work that remains.

