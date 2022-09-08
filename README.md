Given a fight in Slay the Spire, uses AlphaZero algorithm to train a neural network to play it.

# Setup

## Windows:
1. Download and install JDK 17 (I have used IntelliJ's built in JDKs and Amazon Correto JDKs and both worked)
2. Download and install Maven (I use 3.8.4, but any version should work)
3. Download and install Miniconda (https://docs.conda.io/en/latest/miniconda.html)
4. Use conda to create a python environment from environment.yml and activate it
    1. conda env create -f environment.ml
    2. conda activate alphasts
5. Change working directory to agents folder and run mvn install to build the java agent
6. Change working directory back to project root and run python ml.py -t -c 20. It should start generating games in the saves/ directory. The default fight is a gremlin nob fight with the ironclad starter deck with bash upgraded and without ascender's bane (it will run the alphazero agorithm for 20 iterations)
7. Once finished, use python interactive.py to play the fight in the simulator (see Interactive Mode).

## Linux:
Note: I only set it up on AWS once. All I remember is it works but there were error messages at step 4a even though everything run fine.
1. Download and install JDK 17 from your distribution
2. Download and install Maven from your distribution
3. Download and install Miniconda (https://docs.conda.io/en/latest/miniconda.html)
4. Use conda to create a python environment from environment.yml and activate it
    1. conda env create -n alphasts python=3.8
    2. conda activate alphasts
    3. run python ml.py -t -c 20 and just download any missing dependencies from the error messages given via 'conda install' and hope it works in the end
        - note: use 'pip install' instead of 'conda install' instead for tf2onnx
5. Change working directory to agents folder and run mvn install to build the java agent
6. Change working directory back to project root and run python ml.py -t -c 20. It should start generating games in the saves/ directory. The default fight is a gremlin nob fight with the ironclad starter deck with bash upgraded and without ascender's bane (it will run the alphazero agorithm for 20 iterations)
7. Once finished, use python interactive.py to play the fight in the simulator (see Interactive Mode).

# Interactive Mode
Some common commands:
1. Enter the number/letter beside a card to play it. For example, 0. Bash -> type <0><Enter> to play bash. You can also use the card name (i.e. type Bash). Also there's fuzzy search so even 'ba' would work if there are no other possible matches.
2. n \<n=numberOfNodes\>: run monte carlo tree search on the current state n times and display the state 
2. nn \<n=numberOfNodes\>: give the best line of play with n nodes searched for each move
3. reset: clear all the nodes searched so far

# Architecture
Todo

## Input
Todo

## Output
Todo

There are currently 3 heads

1. q_win: Expected win percentage.
2. q_health: Expected health remaining at end of fight.
3. policy: Policy for each action

## MCTS
Todo

During Tree Search, the q value for MCTS is a function of q_win and q_health. Currently the formula is q_win * 0.5 + w_hea (q_win and q_health are pretty correlated)

### Chance Nodes
Todo

## Training Target
Todo

## Domain Knowledge
Todo