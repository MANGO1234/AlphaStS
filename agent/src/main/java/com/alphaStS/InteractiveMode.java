package com.alphaStS;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InteractiveMode {
    public static void interactiveStart(GameState origState, String modelDir) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int mode = 0;
        var states = new ArrayList<GameState>();
        GameState state = origState;
        boolean skipPrint = false;
        Model model = null;
        try {
            model = new Model(modelDir);
        } catch (Exception e) {}
        MCTS mcts = new MCTS();
        mcts.setModel(model);
        List<Integer> drawOrder = null;
        Enemy curEnemy = null;
        List<String> history = new ArrayList<>();
        state.prop.random = new RandomGenInteractive(reader, history);

        while (true) {
            if (mode == 0) {
                if (state.isTerminal() != 0) {
                    System.out.println("Game finished. Result " + (state.isTerminal() == 1 ? "Win" : "Loss"));
                }
                if (!skipPrint) {
                    for (int i = 0; i < state.enemies.size(); i++) {
                        var enemy = state.enemies.get(i);
                        if (enemy.health <= 0) {
                            continue;
                        }
                        System.out.println("Enemy " + i + ": " + enemy.getName());
                        System.out.println("  HP: " + enemy.health);
                        if (enemy.strength > 0) {
                            System.out.println("  Strength: " + enemy.strength);
                        }
                        if (enemy.block > 0) {
                            System.out.println("  Block: " + enemy.block);
                        }
                        if (enemy.artifact > 0) {
                            System.out.println("  Artifact: " + enemy.artifact);
                        }
                        if (enemy.vulnerable > 0) {
                            System.out.println("  Vulnerable: " + enemy.vulnerable);
                        }
                        if (enemy.weak > 0) {
                            System.out.println("  Weak: " + enemy.weak);
                        }
                        if (enemy instanceof Enemy.RedLouse louse) {
                            if (!louse.hasCurledUp) {
                                System.out.println("  Curl Up: " + louse.curlUpAmount);
                            }
                        } else if (enemy instanceof Enemy.GreenLouse louse) {
                            if (!louse.hasCurledUp) {
                                System.out.println("  Curl Up: " + louse.curlUpAmount);
                            }
                        } else if (enemy instanceof Enemy.TheGuardian guardian) {
                            System.out.println("  Mode Shift Damage: " + guardian.modeShiftDmg + "/" + guardian.maxModeShiftDmg);
                        }
                        System.out.println("  Move: " + enemy.getMoveString(state));
                        System.out.println();
                    }
                    System.out.println("Player");
                    System.out.println("  Energy: " + state.energy);
                    System.out.println("  HP: " + state.player.health);
                    if (state.player.block > 0) {
                        System.out.println("  Block: " + state.player.block);
                    }
                    if (state.player.vulnerable > 0) {
                        System.out.println("  Vulnerable: " + state.player.vulnerable);
                    }
                    if (state.player.weak > 0) {
                        System.out.println("  Weak: " + state.player.weak);
                    }
                    if (state.player.frail > 0) {
                        System.out.println("  Frail: " + state.player.frail);
                    }
                    if (state.thorn > 0) {
                        System.out.println("  Thorn: " + state.thorn);
                    }
                    if (state.buffs != 0) {
                        System.out.println("  Buffs:");
                        for (PlayerBuff buff : PlayerBuff.BUFFS) {
                            if ((state.buffs & buff.mask()) != 0) {
                                System.out.println("    - " + buff.name());
                            }
                        }
                    }
                    if (state.prop.counterHandlersNonNull.length > 0) {
                        System.out.println("  Other:");
                        for (int i = 0; i < state.prop.counterHandlers.length; i++) {
                            if (state.prop.counterHandlers[i] != null) {
                                System.out.println("    - " + state.prop.counterNames[i] + "=" + state.getCounterForRead()[i]);
                            }
                        }
                    }
                    System.out.println();
                    if (state.getDrawOrder().size() > 0) {
                        System.out.print("Draw Order: [");
                        for (int i = state.getDrawOrder().size() - 1; i >= 0; i--) {
                            if (i != state.getDrawOrder().size() - 1) {
                                System.out.print(", ");
                            }
                            System.out.print(state.prop.cardDict[state.getDrawOrder().ithCardFromTop(i)].cardName);
                        }
                        System.out.println("]");
                    }
                    System.out.println("Hand");
                    for (int i = 0; i < state.hand.length; i++) {
                        if (state.hand[i] > 0) {
                            System.out.println("  " + state.hand[i] + " " + state.prop.cardDict[i].cardName);
                        }
                    }
                }
                skipPrint = false;
                for (int i = 0; i < state.prop.maxNumOfActions; i++) {
                    if (state.isActionLegal(i)) {
                        if (state.getAction(i).type() == GameActionType.PLAY_CARD ||
                                state.getAction(i).type() == GameActionType.SELECT_ENEMY ||
                                state.getAction(i).type() == GameActionType.SELECT_CARD_HAND ||
                                state.getAction(i).type() == GameActionType.SELECT_CARD_DISCARD ||
                                state.getAction(i).type() == GameActionType.SELECT_CARD_EXHAUST) {
                            System.out.println(i + ". " + state.getActionString(i));
                        } else if (state.getAction(i).type() == GameActionType.END_TURN) {
                            System.out.println("e. End Turn");
                        } else if (state.getAction(i).type() == GameActionType.START_GAME) {
                            System.out.println(i + ". Start Game");
                        } else {
                            throw new RuntimeException();
                        }
                    }
                }
                System.out.println("a. Show Deck");
                System.out.println("m. Show Discard");
            } else if (mode == 1) {
                for (int i = 0; i < state.prop.cardDict.length; i++) {
                    System.out.println(i + ". " + state.prop.cardDict[i].cardName);
                }
                System.out.println();
                System.out.print("[");
                for (int i = 0; i < drawOrder.size(); i++) {
                    System.out.print(state.prop.cardDict[drawOrder.get(i)].cardName + (i == drawOrder.size() - 1 ? "" : ", "));
                }
                System.out.println("]");
            } else if (mode == 2) {
                int prevMove = curEnemy.move;
                for (int i = 0; i < curEnemy.numOfMoves; i++) {
                    curEnemy.move = i;
                    System.out.println(i + ". " + curEnemy.getMoveString(state));
                }
                curEnemy.move = prevMove;
            }

            System.out.print("> ");
            String line = reader.readLine();
            if (line.equals("exit") || line.equals("q")) {
                return;
            }
            history.add(line);

            if (mode == 0) {
                if (line.equals("e")) {
                    states.add(state);
                    state.clearAllSearchInfo();
                    state = state.clone(false);
                    for (int i = 0; i < state.prop.maxNumOfActions; i++) {
                        if (state.isActionLegal(i) && state.getAction(i).type() == GameActionType.END_TURN) {
                            state.doAction(i);
                        }
                    }
                    for (int i = 0; i < state.prop.maxNumOfActions; i++) {
                        if (state.isActionLegal(i) && state.getAction(i).type() == GameActionType.BEGIN_TURN) {
                            state.doAction(i);
                        }
                    }
                    continue;
                } else if (line.equals("a")) {
                    System.out.println("Deck");
                    for (int i = 0; i < state.deck.length; i++) {
                        if (state.deck[i] > 0) {
                            System.out.println("  " + state.deck[i] + " " + state.prop.cardDict[i].cardName);
                        }
                    }
                    skipPrint = true;
                    continue;
                } else if (line.equals("m")) {
                    System.out.println("Discard");
                    for (int i = 0; i < state.discard.length; i++) {
                        if (state.discard[i] > 0) {
                            System.out.println("  " + state.discard[i] + " " + state.prop.cardDict[i].cardName);
                        }
                    }
                    skipPrint = true;
                    continue;
                } else if (line.equals("s")) {
                    System.out.println(state.toStringReadable());
                    skipPrint = true;
                    continue;
                } else if (line.equals("i")) {
                    System.out.println(Arrays.toString(state.getNNInput()));
                    skipPrint = true;
                    continue;
                } else if (line.startsWith("eh ")) {
                    try {
                        String[] s = line.split(" ");
                        if (s.length == 2 || state.enemiesAlive == 1) {
                            int hp = Integer.parseInt(line.substring(3));
                            if (hp > 0) {
                                for (Enemy enemy : state.enemies) {
                                    if (enemy.health > 0) {
                                        enemy.health = hp;
                                    }
                                }
                            }
                        } else if (s.length == 3) {
                            int enemyIdx = Integer.parseInt(s[1]);
                            int hp = Integer.parseInt(s[2]);
                            if (enemyIdx >= 0 && enemyIdx < state.enemies.size() && hp >= 0) {
                                state.enemies.get(enemyIdx).health = hp;
                            }
                        }
                        continue;
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                } else if (line.startsWith("louse-curl ")) {
                    String[] s = line.split(" ");
                    if (s.length == 3) {
                        int enemyIdx = parseInt(s[1], -1);
                        int n = parseInt(s[2], -1);
                        if (enemyIdx >= 0 && enemyIdx < state.enemies.size() && n >= 0) {
                            if (state.enemies.get(enemyIdx) instanceof Enemy.RedLouse louse) {
                                louse.curlUpAmount = n;
                            }
                        }
                        if (enemyIdx >= 0 && enemyIdx < state.enemies.size() && n >= 0) {
                            if (state.enemies.get(enemyIdx) instanceof Enemy.GreenLouse louse) {
                                louse.curlUpAmount = n;
                            }
                        }
                    }
                    continue;
                } else if (line.startsWith("louse-dmg ")) {
                    String[] s = line.split(" ");
                    if (s.length == 3) {
                        int enemyIdx = parseInt(s[1], -1);
                        int n = Integer.parseInt(s[2], -1);
                        if (enemyIdx >= 0 && enemyIdx < state.enemies.size() && n >= 0) {
                            if (state.enemies.get(enemyIdx) instanceof Enemy.RedLouse louse) {
                                louse.d = n;
                            }
                        }
                        if (enemyIdx >= 0 && enemyIdx < state.enemies.size() && n >= 0) {
                            if (state.enemies.get(enemyIdx) instanceof Enemy.GreenLouse louse) {
                                louse.d = n;
                            }
                        }
                    }
                    continue;
                } else if (line.startsWith("em ")) {
                    int enemyIdx = parseInt(line.substring(3), -1);
                    if (0 <= enemyIdx && enemyIdx < state.enemies.size()) {
                        curEnemy = state.enemies.get(enemyIdx);
                        mode = 2;
                        continue;
                    }
                } else if (line.startsWith("ph ")) {
                    int hp = parseInt(line.substring(3), -1);
                    if (hp >= 0) {
                        state.player.health = hp;
                    }
                    continue;
                } else if (line.equals("b")) {
                    if (states.size() > 0) {
                        state = states.remove(states.size() - 1);
                    }
                    continue;
                } else if (line.equals("do")) {
                    drawOrder = new ArrayList<Integer>();
                    mode = 1;
                    continue;
                } else if (line.equals("rc")) { // remove card from hand
                    removeCardFromHandSelectScreen(reader, state, history);
                    state.clearAllSearchInfo();
                    continue;
                } else if (line.equals("ac")) { // add card to hand
                    addCardToHandSelectScreen(reader, state, history);
                    state.clearAllSearchInfo();
                    continue;
                } else if (line.equals("hist")) {
                    for (String l : history) {
                        if (!l.equals("tree") && !l.startsWith("matches") && !l.equals("hist") && !l.startsWith("nn ") && !l.startsWith("n ")) {
                            System.out.println(l);
                        }
                    }
                    skipPrint = true;
                    continue;
                } else if (line.equals("tree")) {
                    MCTS.printTree(state, new OutputStreamWriter(System.out), 3);
                    skipPrint = true;
                    continue;
                } else if (line.startsWith("n ")) {
                    boolean prevRngOff = ((RandomGenInteractive) state.prop.random).rngOn;
                    ((RandomGenInteractive) state.prop.random).rngOn = true;
                    runMCTS(state, mcts, line);
                    ((RandomGenInteractive) state.prop.random).rngOn = prevRngOff;
                    skipPrint = true;
                    continue;
                } else if (line.startsWith("nn ")) {
                    boolean prevRngOff = ((RandomGenInteractive) state.prop.random).rngOn;
                    ((RandomGenInteractive) state.prop.random).rngOn = true;
                    runNNPV(state, mcts, line);
                    ((RandomGenInteractive) state.prop.random).rngOn = prevRngOff;
                    skipPrint = true;
                    continue;
                } else if (line.startsWith("matches")) {
                    boolean prevRngOff = ((RandomGenInteractive) state.prop.random).rngOn;
                    ((RandomGenInteractive) state.prop.random).rngOn = true;
                    runMatches(modelDir, state, line);
                    ((RandomGenInteractive) state.prop.random).rngOn = prevRngOff;
                    skipPrint = true;
                    continue;
                } else if (line.equals("rng off")) {
                    ((RandomGenInteractive) state.prop.random).rngOn = false;
                    skipPrint = true;
                    continue;
                } else if (line.equals("rng on")) {
                    ((RandomGenInteractive) state.prop.random).rngOn = true;
                    skipPrint = true;
                    continue;
                } else if (line.equals("desc")) {
                    System.out.println(state.getNNInputDesc());
                    skipPrint = true;
                    continue;
                } else if (line.equals("")) {
                    continue;
                }

                int action = parseInt(line, -1);
                if (action >= 0 && state.isActionLegal(action)) {
                    states.add(state);
                    state.clearAllSearchInfo();
                    state = state.clone(false);
                    state.doAction(action);
                    continue;
                }

                System.out.println("Unknown Command.");
            } else if (mode == 1) {
                if (line.equals("b")) {
                    drawOrder = null;
                    mode = 0;
                    continue;
                } else if (line.equals("c")) {
                    if (drawOrder.size() > 0) {
                        drawOrder.remove(drawOrder.size() - 1);
                    }
                    continue;
                } else if (line.equals("clear")) {
                    state.getDrawOrder().clear();
                } else if (line.equals("e")) {
                    for (int i = drawOrder.size() - 1; i >= 0; i--) {
                        state.getDrawOrder().pushOnTop(drawOrder.get(i));
                    }
                    mode = 0;
                    continue;
                }

                try {
                    int cardIdx = Integer.parseInt(line);
                    if (cardIdx >= 0 && cardIdx < state.prop.cardDict.length) {
                        drawOrder.add(cardIdx);
                        continue;
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
                System.out.println("Unknown Command.");
            } else if (mode == 2) {
                if (line.equals("b")) {
                    drawOrder = null;
                    mode = 0;
                    continue;
                }

                try {
                    int moveIdx = Integer.parseInt(line);
                    if (moveIdx >= 0 && moveIdx < curEnemy.numOfMoves) {
                        curEnemy.move = moveIdx;
                        mode = 0;
                        continue;
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
                System.out.println("Unknown Command.");
            }
        }
    }

    private static void addCardToHandSelectScreen(BufferedReader reader, GameState state, List<String> history) throws IOException {
        for (int i = 0; i < state.prop.cardDict.length; i++) {
            System.out.println(i + ". " + state.prop.cardDict[i].cardName);
        }
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            history.add(line);
            if (line.equals("b")) {
                return;
            }
            int idx = parseInt(line, -1);
            if (idx >= 0 && idx < state.prop.cardDict.length) {
                state.addCardToHand(idx);
                return;
            }
            System.out.println("Unknown Command");
        }
    }

    private static void removeCardFromHandSelectScreen(BufferedReader reader, GameState state, List<String> history) throws IOException {
        for (int i = 0; i < state.prop.cardDict.length; i++) {
            if (state.hand[i] > 0) {
                System.out.println(i + ". " + state.prop.cardDict[i].cardName);
            }
        }
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            history.add(line);
            if (line.equals("b")) {
                return;
            }
            int idx = parseInt(line, -1);
            if (idx >= 0 && idx < state.prop.cardDict.length && state.hand[idx] > 0) {
                state.removeCardFromHand(idx);
                return;
            }
            System.out.println("Unknown Command");
        }
    }

    static int selectCardForWarpedTongs(BufferedReader reader, GameState state, List<String> history) throws IOException {
        int nonUpgradedCardCount = 0;
        for (int i = 0; i < state.prop.upgradeIdxes.length; i++) {
            if (state.hand[i] > 0 && state.prop.upgradeIdxes[i] >= 0) {
                System.out.println(nonUpgradedCardCount + ". " + state.prop.cardDict[i].cardName);
                nonUpgradedCardCount += state.hand[i];
            }
        }
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            history.add(line);
            int r = parseInt(line, -1);
            if (r >= 0 && r < nonUpgradedCardCount) {
                return r;
            }
            System.out.println("Unknown Command");
        }
    }

    private static void runMatches(String modelDir, GameState state, String line) {
        String[] s = line.split(" ");
        int matchCount = 100;
        int nodeCount = 1000;
        MatchSession session = new MatchSession(state, modelDir);

        if (s.length > 1) {
            for (int i = 1; i < s.length; i++) {
                if (s[i].startsWith("c=")) {
                    matchCount = parseInt(s[i].substring(2), 0);
                }
                if (s[i].startsWith("n=")) {
                    nodeCount = parseInt(s[i].substring(2), 1000);
                }
                if (s[i].startsWith("a=")) {
                    if (s[i].substring(2).equals("e")) {
                        session.startingAction = state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length - 1;
                    } else {
                        session.startingAction = parseInt(s[i].substring(2), -1);
                    }
                    if (!state.isActionLegal(session.startingAction)) {
                        System.out.println("Unknown action.");
                        matchCount = 0;
                    }
                }
            }
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < matchCount; i++) {
            session.playGame(nodeCount);
            if (session.game_i % 25 == 0) {
                session.printProgress(start, matchCount, true);
            }
        }
    }

    private static void runMCTS(GameState state, MCTS mcts, String line) {
        int count = Integer.parseInt(line.substring(2));
        for (int i = state.total_n; i < count; i++) {
            mcts.search(state, false, -1);
        }
        System.out.println(state.toStringReadable());
    }

    private static void runNNPV(GameState state, MCTS mcts, String line) {
        int count = parseInt(line.substring(3), 1);
        GameState s = state;
        int move_i = 0;
        do {
            for (int i = s.total_n; i < count; i++) {
                mcts.search(s, false, -1);
            }
            int action = MCTS.getActionWithMaxNodesOrTerminal(s);
            if (action < 0) {
                break;
            }
            int max_n = s.n[action];
            System.out.println("  " + (++move_i) + ". " + s.getActionString(action) +
                    " (" + max_n + ", " + GameState.calc_q(s.q_win[action] / max_n, s.q_health[action] / max_n) + ", "  +
                    (s.q_win[action] / max_n) + ", " + (s.q_health[action] / max_n) + ")");
            State ns = s.ns[action];
            if (ns instanceof ChanceState) {
                break;
            } else if (ns instanceof GameState ns2) {
                if (ns2.isTerminal() != 0) {
                    break;
                } else {
                    s = ns2;
                }
            } else {
                System.out.println("Unknown ns: " + state.toStringReadable());
                System.out.println("Unknown ns: " + Arrays.toString(state.transpositionsPolicyMask));
                System.out.println("Unknown ns: " + Arrays.asList(state.ns).stream().map((x) -> x == null).toList());
                break;
            }
        } while (true);
    }

    private static int parseInt(String s, int default_v) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return default_v;
        }
    }
}

class RandomGenInteractive extends RandomGen {
    boolean rngOn = true;
    private final BufferedReader reader;
    private final List<String> history;

    public RandomGenInteractive(BufferedReader reader, List<String> history) {
        this.reader = reader;
        this.history = history;
    }

    @Override public int nextInt(int bound, GameState state, RandomGenCtx ctx) {
        if (rngOn) {
            return super.nextInt(bound, state, ctx);
        }
        switch (ctx) {
        case WarpedTongs -> {
            try {
                return InteractiveMode.selectCardForWarpedTongs(reader, state, history);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        default -> {
            return super.nextInt(bound, state, ctx);
        }
        }
    }
}