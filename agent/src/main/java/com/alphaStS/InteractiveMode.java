package com.alphaStS;

import com.alphaStS.enemy.Enemy;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.alphaStS.utils.Utils.formatFloat;

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
                    int enemyIdx = 0;
                    for (var enemy : state.getEnemiesForRead()) {
                        if (enemy.getHealth() <= 0) {
                            continue;
                        }
                        System.out.println("Enemy " + (enemyIdx++) + ": " + enemy.getName());
                        System.out.println("  HP: " + enemy.getHealth());
                        if (enemy.getStrength() > 0) {
                            System.out.println("  Strength: " + enemy.getStrength());
                        }
                        if (enemy.getBlock() > 0) {
                            System.out.println("  Block: " + enemy.getBlock());
                        }
                        if (enemy.getArtifact() > 0) {
                            System.out.println("  Artifact: " + enemy.getArtifact());
                        }
                        if (enemy.getVulnerable() > 0) {
                            System.out.println("  Vulnerable: " + enemy.getVulnerable());
                        }
                        if (enemy.getWeak() > 0) {
                            System.out.println("  Weak: " + enemy.getWeak());
                        }
                        if (enemy instanceof Enemy.RedLouse louse) {
                            if (!louse.hasCurledUp()) {
                                System.out.println("  Curl Up: " + louse.getCurlUpAmount());
                            }
                        } else if (enemy instanceof Enemy.GreenLouse louse) {
                            if (!louse.hasCurledUp()) {
                                System.out.println("  Curl Up: " + louse.getCurlUpAmount());
                            }
                        } else if (enemy instanceof Enemy.TheGuardian guardian) {
                            System.out.println("  Mode Shift Damage: " + guardian.getModeShiftDmg() + "/" + guardian.getMaxModeShiftDmg());
                        }
                        System.out.println("  Move: " + enemy.getMoveString(state));
                        System.out.println();
                    }
                    System.out.println("Player");
                    System.out.println("  Energy: " + state.energy);
                    System.out.println("  HP: " + state.getPlayeForRead().getHealth());
                    if (state.getPlayeForRead().getBlock() > 0) {
                        System.out.println("  Block: " + state.getPlayeForRead().getBlock());
                    }
                    if (state.getPlayeForRead().getStrength() > 0) {
                        System.out.println("  Strength: " + state.getPlayeForRead().getStrength());
                    }
                    if (state.getPlayeForRead().getDexterity() > 0) {
                        System.out.println("  Dexterity: " + state.getPlayeForRead().getDexterity());
                    }
                    if (state.getPlayeForRead().getVulnerable() > 0) {
                        System.out.println("  Vulnerable: " + state.getPlayeForRead().getVulnerable());
                    }
                    if (state.getPlayeForRead().getWeak() > 0) {
                        System.out.println("  Weak: " + state.getPlayeForRead().getWeak());
                    }
                    if (state.getPlayeForRead().getFrail() > 0) {
                        System.out.println("  Frail: " + state.getPlayeForRead().getFrail());
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
                            if (state.prop.counterHandlers[i] != null && state.getCounterForRead()[i] != 0) {
                                System.out.println("    - " + state.prop.counterNames[i] + "=" + state.getCounterForRead()[i]);
                            }
                        }
                    }
                    System.out.println();
                    if (state.getDrawOrderForRead().size() > 0) {
                        System.out.print("Draw Order: [");
                        for (int i = state.getDrawOrderForRead().size() - 1; i >= 0; i--) {
                            if (i != state.getDrawOrderForRead().size() - 1) {
                                System.out.print(", ");
                            }
                            System.out.print(state.prop.cardDict[state.getDrawOrderForRead().ithCardFromTop(i)].cardName);
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
                for (int i = 0; i < state.getLegalActions().length; i++) {
                    if (state.getAction(i).type() == GameActionType.PLAY_CARD ||
                            state.getAction(i).type() == GameActionType.SELECT_ENEMY ||
                            state.getAction(i).type() == GameActionType.SELECT_CARD_HAND ||
                            state.getAction(i).type() == GameActionType.SELECT_CARD_DISCARD ||
                            state.getAction(i).type() == GameActionType.SELECT_CARD_EXHAUST ||
                            state.getAction(i).type() == GameActionType.USE_POTION) {
                        System.out.println(i + ". " + state.getActionString(i));
                    } else if (state.getAction(i).type() == GameActionType.END_TURN) {
                        System.out.println("e. End Turn");
                    } else if (state.getAction(i).type() == GameActionType.START_GAME) {
                        System.out.println(i + ". Start Game");
                    } else {
                        System.out.println(state.getAction(i));
                        throw new RuntimeException();
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
                int prevMove = curEnemy.getMove();
                for (int i = 0; i < curEnemy.numOfMoves; i++) {
                    System.out.println(i + ". " + curEnemy.getMoveString(state, i));
                }
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
                    for (int i = 0; i < state.getLegalActions().length; i++) {
                        if (state.getAction(i).type() == GameActionType.END_TURN) {
                            state.doAction(i);
                            break;
                        }
                    }
                    for (int i = 0; i < state.getLegalActions().length; i++) {
                        if (state.getAction(i).type() == GameActionType.BEGIN_TURN) {
                            state.doAction(i);
                            break;
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
                                for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                                    enemy.setHealth(hp);
                                }
                            }
                        } else if (s.length == 3) {
                            int enemyIdx = Integer.parseInt(s[1]);
                            int hp = Integer.parseInt(s[2]);
                            if (enemyIdx >= 0 && enemyIdx < state.getEnemiesForRead().size() && hp >= 0) {
                                state.getEnemiesForWrite().getForWrite(enemyIdx).setHealth(hp);
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
                        if (enemyIdx >= 0 && enemyIdx < state.getEnemiesForWrite().size() && n >= 0) {
                            if (state.getEnemiesForWrite().get(enemyIdx) instanceof Enemy.RedLouse louse) {
                                louse.setCurlUpAmount(n);
                            }
                        }
                        if (enemyIdx >= 0 && enemyIdx < state.getEnemiesForWrite().size() && n >= 0) {
                            if (state.getEnemiesForWrite().get(enemyIdx) instanceof Enemy.GreenLouse louse) {
                                louse.setCurlUpAmount(n);
                            }
                        }
                    }
                    continue;
                } else if (line.startsWith("louse-dmg ")) {
                    String[] s = line.split(" ");
                    if (s.length == 3) {
                        int enemyIdx = parseInt(s[1], -1);
                        int n = Integer.parseInt(s[2], -1);
                        if (enemyIdx >= 0 && enemyIdx < state.getEnemiesForWrite().size() && n >= 0) {
                            if (state.getEnemiesForWrite().get(enemyIdx) instanceof Enemy.RedLouse louse) {
                                louse.setD(n);
                            }
                        }
                        if (enemyIdx >= 0 && enemyIdx < state.getEnemiesForWrite().size() && n >= 0) {
                            if (state.getEnemiesForWrite().get(enemyIdx) instanceof Enemy.GreenLouse louse) {
                                louse.setD(n);
                            }
                        }
                    }
                    continue;
                } else if (line.startsWith("em ")) {
                    int enemyIdx = parseInt(line.substring(3), -1);
                    if (0 <= enemyIdx && enemyIdx < state.getEnemiesForRead().size()) {
                        curEnemy = state.getEnemiesForWrite().getForWrite(enemyIdx);
                        mode = 2;
                        continue;
                    }
                } else if (line.startsWith("ph ")) {
                    int hp = parseInt(line.substring(3), -1);
                    if (hp >= 0) {
                        state.getPlayerForWrite().setHealth(hp);
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
                } else if (line.equals("rand")) {
                    selectRandomization(reader, state, history);
                    state.clearAllSearchInfo();
                    continue;
                } else if (line.equals("hist")) {
                    for (String l : history) {
                        if (!l.startsWith("tree") && !l.startsWith("games") && !l.equals("hist") && !l.startsWith("nn ") && !l.startsWith("n ")) {
                            System.out.println(l);
                        }
                    }
                    skipPrint = true;
                    continue;
                } else if (line.equals("tree") || line.startsWith("tree ")) {
                    printTree(state, line, modelDir);
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
                } else if (line.startsWith("nnn ")) {
                    boolean prevRngOff = ((RandomGenInteractive) state.prop.random).rngOn;
                    ((RandomGenInteractive) state.prop.random).rngOn = true;
                    runNNPV2(state, mcts, line);
                    ((RandomGenInteractive) state.prop.random).rngOn = prevRngOff;
                    skipPrint = true;
                    continue;
                } else if (line.equals("games") || line.startsWith("games ")) {
                    boolean prevRngOff = ((RandomGenInteractive) state.prop.random).rngOn;
                    ((RandomGenInteractive) state.prop.random).rngOn = true;
                    runGames(modelDir, state, line);
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
                if (action >= 0 && action <= state.getLegalActions().length) {
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
                    state.getDrawOrderForWrite().clear();
                } else if (line.equals("e")) {
                    for (int i = drawOrder.size() - 1; i >= 0; i--) {
                        state.getDrawOrderForWrite().pushOnTop(drawOrder.get(i));
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
                        curEnemy.setMove(moveIdx);
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

    static void selectRandomization(BufferedReader reader, GameState state, List<String> history) throws IOException {
        if (state.prop.randomization == null) {
            return ;
        }
        var randomizations = state.prop.randomization.listRandomizations(state);
        while (true) {
            for (var info : randomizations.entrySet()) {
                System.out.println(info.getKey() + ". " + info.getValue().desc());
            }
            System.out.print("> ");
            String line = reader.readLine();
            history.add(line);
            int r = parseInt(line, -1);
            if (r >= 0 && r < randomizations.size()) {
                state.prop.randomization.randomize(state, r);
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

    private static void runGames(String modelDir, GameState state, String line) {
        String[] s = line.split(" ");
        int numberOfGames = 100;
        int numberOfThreads = 2;
        int nodeCount = 500;
        int startingAction = -1;
        int randomizationScenario = -1;
        if (s.length > 1) {
            for (int i = 1; i < s.length; i++) {
                if (s[i].startsWith("c=")) {
                    numberOfGames = parseInt(s[i].substring(2), 0);
                }
                if (s[i].startsWith("t=")) {
                    numberOfThreads = parseInt(s[i].substring(2), 2);
                }
                if (s[i].startsWith("n=")) {
                    nodeCount = parseInt(s[i].substring(2), 500);
                }
                if (s[i].startsWith("r=")) {
                    randomizationScenario = parseInt(s[i].substring(2), -1);
                }
                if (s[i].startsWith("a=")) {
                    if (s[i].substring(2).equals("e")) {
                        startingAction = state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length - 1;
                    } else {
                        startingAction = parseInt(s[i].substring(2), -1);
                    }
                    if (startingAction < 0 || startingAction >= state.getLegalActions().length) {
                        System.out.println("Unknown action.");
                        numberOfGames = 0;
                    }
                }
            }
        }
        MatchSession session = new MatchSession(numberOfThreads, modelDir);
        session.startingAction = startingAction;
        session.playGames(state, numberOfGames, nodeCount, randomizationScenario, true);
    }

    private static void printTree(GameState state, String line, String modelDir) {
        String[] s = line.split(" ");
        int depth = 3;
        int action = -1;
        boolean writeToFile = false;
        if (s.length > 1) {
            for (int i = 1; i < s.length; i++) {
                if (s[i].startsWith("d=")) {
                    depth = parseInt(s[i].substring(2), 3);
                }
                if (s[i].startsWith("a=")) {
                    action = parseInt(s[i].substring(2), -1);
                }
                if (s[i].equals("f")) {
                    writeToFile = true;
                }
            }
        }
        Writer writer = null;
        try {
            writer = writeToFile ? new BufferedWriter(new FileWriter(modelDir + "/tree.txt")) : new OutputStreamWriter(System.out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (action >= 0 && action < state.ns.length && state.ns[action] != null) {
            GameStateUtils.printTree2(state.ns[action], writer, depth);
        } else {
            GameStateUtils.printTree2(state, writer, depth);
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
        state.clearAllSearchInfo();
        double[] policySnapshot = null;
        var next = 20;
        for (int i = 0; i < count; i++) {
            mcts.search(state, false, -1);
            if (i == next) {
                var nextSnapshot = new double[state.n.length];
                for (int j = 0; j < state.n.length; j++) {
                    nextSnapshot[j] = state.n[j] / (double) state.total_n;
                }
                if (policySnapshot != null) {
                    var kld = 0.0;
                    for (int j = 0; j < nextSnapshot.length; j++) {
                        if (policySnapshot[j] > 0) {
                            kld += policySnapshot[j] * Math.log(policySnapshot[j] / nextSnapshot[j]);
                        }
                    }
                    if (Math.abs(kld) > 0.01) {
                        System.out.println(state);
                    }
                    System.out.println("KLD at " + i + ": " + kld);
                }
                policySnapshot = nextSnapshot;
                next += 20;
            }
        }
        return;
//        GameState s = state;
//        int move_i = 0;
//        do {
//            for (int i = s.total_n; i < count; i++) {
//                mcts.search(s, false, -1);
//            }
//            int action = MCTS.getActionWithMaxNodesOrTerminal(s);
//            if (action < 0) {
//                break;
//            }
//            int max_n = s.n[action];
//            System.out.println("  " + (++move_i) + ". " + s.getActionString(action) +
//                    ": n=" + max_n + ", q=" + formatFloat(s.q_comb[action] / max_n) + ", q_win=" + formatFloat(s.q_win[action] / max_n) + ", q_health=" + formatFloat(s.q_health[action] / max_n) + " (" + s.q_health[action] / max_n * s.getPlayeForRead().getMaxHealth() + ")");
//            State ns = s.ns[action];
//            if (ns instanceof ChanceState) {
//                break;
//            } else if (ns instanceof GameState ns2) {
//                if (ns2.isTerminal() != 0) {
//                    break;
//                } else {
//                    s = ns2;
//                }
//            } else {
//                System.out.println("Unknown ns: " + state.toStringReadable());
//                System.out.println("Unknown ns: " + Arrays.stream(state.ns).map(Objects::isNull).toList());
//                break;
//            }
//        } while (true);
    }

    private static void runNNPV2(GameState state, MCTS mcts, String line) {
        int count = parseInt(line.substring(4), 1);
        GameState s = state;
        if (s.searchFrontier != null && s.searchFrontier.total_n != s.total_n + 1) {
            s.clearAllSearchInfo();
        }
        for (int i = s.total_n; i < count; i++) {
            mcts.searchLine(s, false, true, -1);
        }
        s.searchFrontier.lines.values().stream().filter((x) -> {
            return x.state instanceof ChanceState || x.numberOfActions == ((GameState) x.state).getLegalActions().length;
        }).sorted((a, b) -> {
            if (a.internal == b.internal) {
                return -Integer.compare(a.n, b.n);
            } else if (a.internal) {
                return 1;
            } else {
                return -1;
            }
        }).map((x) -> {
            var tmpS = state.clone(false);
            var actions = x.getActions(tmpS);
            var strings = new ArrayList<String>();
            for (var action : actions) {
                if (tmpS.getActionString(action).equals("Begin Turn")) {
                    continue;
                }
                strings.add(tmpS.getActionString(action));
                tmpS.doAction(action);
            }
            return String.join(", ", strings) + ": n=" + x.n + ", p=" + formatFloat(x.p_cur) + ", q=" + formatFloat(x.q_comb / x.n) +
                    ", q_win=" + formatFloat(x.q_win / x.n) + ", q_health=" + formatFloat(x.q_health / x.n)  +
                    " (" + formatFloat(x.q_health / x.n * s.getPlayeForRead().getMaxHealth()) + ")";
        }).forEach(System.out::println);
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