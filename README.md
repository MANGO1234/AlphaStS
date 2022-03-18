Uses AlphaZero algorithm to train a NN to play the given fight.

Architecture

Input

The current state of the game.

Output

There are currently 3 head

1. q_win: Expected win percentage.
2. q_health: Expected health remaining at end of fight.
3. policy: Policy for each action

Chance Nodes

MCTS

During Tree Search, the q value for MCTS is a function of q_win and q_health. Currently the formula is q_win * 0.5 + w_hea (q_win and q_health are pretty correlated)

Backup Operator
This is highly experimental with no theory backing it up. 

Training Target

Domain Knowledge
