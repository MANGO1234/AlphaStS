Given the description of a fight in Slay the Spire (i.e. player health, deck, enemies, relics, and potions), uses the AlphaZero algorithm to train a neural network to play it.

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
Note: I only set it up on AWS once. All I remember is it works but there were error messages at step 4.iii even though everything run fine.
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
2. n \<n=numberOfNodes\>: run monte carlo tree search on the current state for n simulations and display the state
3. nn \<n=numberOfNodes\>: give the best line of play with n simulations searched for each move
4. reset: clear all the nodes searched so far

# Architecture
It's suggested for the reader to have knowledge of the AlphaZero algorithm first before reading the Architecture section.

## Input
All information that can change in a fight are passed to the neural network. Using the fight of A20 base Ironclad deck versus Gremlin Nob as an example: the count of Bash, Strike, Defend, Ascender's Bane in deck+hand+discard, player hp and vulnerable, Gremlin Nob hp and vulnerable, and the current move of Gremlin Nob are all passed to the network. Information such as energy per turn, strength, or weakness are not passed in since they are static values. Those are consider as part of the environment. All inputs are standardized to around [-1, 1].

Multi-steps actions are split up as individual action while playing the fight. For example, to play True Grit+, we first select True Grit+ as the card to play, then we select what card to exhaust from our hand. As such, additional inputs such as the current state context (e.g. selecting a card from hand) and the card that triggered the current context (e.g. True Grit+) are also passed in as inputs as needed.

## Neural Network Architecture
Currently just a simple 2 layer fully connected feedforward network.

## Output
**policy**: probability distribution over each possible action. This includes for each card an action to select the card to play, for each enemy an action to select the enemy, for each potion an action to use the potion, and ending the turn. Currently the action for selecting a card to play are also reused for selecting a card from hand, selecting a card from discard, and selecting a card from exhaust.

**v_win**: a value between -1 and 1 represting win rate, with -1 mapping to 0% chance of winning and 1 mapping 100% chance of meaning.

**v_health**: a value between -1 and 1 represting expected health at the end of fight, with -1 mapping to 0 health and 1 mapping to max player health.

Note the agent will shift v_win and v_health to [0, 1] when using it. We will refer to the shifted value as v_win_norm and v_health_norm.

There may also be extra outputs for meta rewards such as using Feed to kill an enemy depending on the fight.

## Loss
The loss function is 0.45 * MSE(v_health) + 0.45 * MSE(v_win) + 0.1 * crossEntropyLoss(policy) + 0.25 * MSE(meta rewards).

Note I haven't added regularization to the loss yet (planning to test it).

Note the high value to policy loss ratio empirically speeds up training for the few cases I have tested it on. Based on [Accelerating and Improving AlphaZero Using Population Based Training
](https://arxiv.org/abs/2003.06212) and [Alternative Loss Functions in AlphaZero-like Self-play
](https://ieeexplore.ieee.org/document/9002814), high value to loss ratio are also preferred in other games. Intuitively speaking, it should be more important that the network can distinguish between a 60% win rate position and a 63% win rate position than it is to have a slightly bit more accurate policy because MCTS would override the policy as more simulations are run.

Another thing to note is currently only legal actions contribute to the cross entropy loss of the policy. i.e. non-legal actions are masked out when calculating the loss.

## MCTS

## Reward (or Q value to maximize for MCTS)
Currently the reward is v_base=v_win_norm / 2 + (v_win_norm<sup>2</sup> * v_health_norm) / 2. The intention is that win rate is maximized first, and if win rate are about equal then expected health at the end of the fight is maximized.

Playing a potion will penalize v_base by a percentage (e.g. 20% generally means a potion won't be played unless the situation is dire, while 0% means use it at the possible moment to maximize win rate and expected health). The percentage is passed in as part of the fight description as a measure of how valuable the potion is. This penalty is only applied during MCTS search, but the neural network will learn about this penalty in its policy and value outputs. Note this was a very adhoc choice. It seem to work alright so far but there should be better choices out there.

Meta rewards currently are added to v_health for purpose of reward calculation. So e.g. using Feed to kill an enemy adds 6 hp to v_health_norm for the purpose of calculating v_base. The amount of health added is also passed in as part of the fight description as a measure of how valuable the meta reward is.

### Chance Nodes
Todo

### Transpositions
Todo

### Terminal Propagation
Todo

## Training
Todo

## Domain Knowledge
Todo