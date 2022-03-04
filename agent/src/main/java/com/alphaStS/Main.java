package com.alphaStS;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.Bash(), 1));
        cards.add(new CardCount(new Card.Strike(), 4));
        cards.add(new CardCount(new Card.Defend(), 4));
        cards.add(new CardCount(new Card.AscendersBane(), 1));
        cards.add(new CardCount(new Card.SeverSoul(), 1));
        cards.add(new CardCount(new Card.Clash(), 1));
        cards.add(new CardCount(new Card.Headbutt(), 1));
        cards.add(new CardCount(new Card.Anger(), 1));
//       cards.add(new CardCount(new Card.Disarm(), 1));
//       cards.add(new CardCount(new Card.ArmanentP(), 1));
//       cards.add(new CardCount(new Card.PommelStrike(), 1));
        var enemies = new ArrayList<Enemy>();
//         enemies.add(new Enemy.GremlinNob());
         enemies.add(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
         enemies.add(new Enemy.Sentry(45, Enemy.Sentry.BEAM));
         enemies.add(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
//       enemies.add(new Enemy.SlimeBoss());
//       enemies.add(new Enemy.LargeSpikeSlime(75, true));
//       enemies.add(new Enemy.LargeAcidSlime(75, true));
//       enemies.add(new Enemy.MediumSpikeSlime(37, true));
//       enemies.add(new Enemy.MediumSpikeSlime(37, true));
//       enemies.add(new Enemy.MediumAcidSlime(37, true));
//       enemies.add(new Enemy.MediumAcidSlime(37, true));
        var relics = new ArrayList<Relic>();
        relics.add(new Relic.Orichalcum());
        relics.add(new Relic.BronzeScales());
        relics.add(new Relic.Vajira());
       relics.add(new Relic.Anchor());
        var state = new GameState(enemies, new Player(30, 85), cards, relics);
//        var cards = new ArrayList<CardCount>();
//        cards.add(new CardCount(new Card.Bash(), 1));
//        cards.add(new CardCount(new Card.Strike(), 5));
//        cards.add(new CardCount(new Card.Defend(), 4));
//        cards.add(new CardCount(new Card.AscendersBane(), 1));
//        cards.add(new CardCount(new Card.Corruption(), 1));
//        cards.add(new CardCount(new Card.TwinStrike(), 1));
//        var enemies = new ArrayList<Enemy>();
//        enemies.add(new Enemy.GreenLouse());
//        enemies.add(new Enemy.GreenLouse());
//        var relics = new ArrayList<Relic>();
//        var state = new GameState(enemies, new Player(46, 75), cards, relics);

        if (args.length > 0 && args[0].equals("--get-lengths")) {
            System.out.print(state.getInput().length + "," + state.prop.maxNumOfActions);
            return;
        }

        boolean GEN_TRAINING_MATCHES = false;
        boolean TEST_AGENT_FITNESS = false;
        boolean PLAY_MATCHES = false;
        boolean PLAY_A_GAME = false;
        int ITERATION_COUNT = 5;
        int NODE_COUNT = 1000;
        String TMP_DIR = "../tmp";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-t")) {
                GEN_TRAINING_MATCHES = true;
            }
            if (args[i].equals("-tm")) {
                TEST_AGENT_FITNESS = true;
            }
            if (args[i].equals("-m")) {
                PLAY_MATCHES = true;
            }
            if (args[i].equals("-p")) {
                PLAY_A_GAME = true;
            }
            if (args[i].equals("-c")) {
                ITERATION_COUNT = Integer.parseInt(args[i + 1]);
                i++;
            }
            if (args[i].equals("-n")) {
                NODE_COUNT = Integer.parseInt(args[i + 1]);
                i++;
            }
            if (args[i].equals("-dir")) {
                TMP_DIR = args[i + 1];
                i++;
            }
        }

        if (args.length > 0 && (args[0].equals("--i") || args[0].equals("-i"))) {
            //        if (args.length > 0 && (args[0].equals("--i") || args[0].equals("-i"))) {
            //        if (args.length > 0 && (args[0].equals("--i") || args[0].equals("-i"))) {
            interactiveMode(state, TMP_DIR);
            return;
        }
        if (TMP_DIR.equals("../tmp")) {
            ITERATION_COUNT = 100;
        }

        if (PLAY_A_GAME) {
            MatchSession session = new MatchSession(state, TMP_DIR);
            session.playGame(NODE_COUNT);
            for (GameStep step : session.states) {
                System.out.println(step.state().toStringReadable());
                if (step.action() >= 0) {
                    System.out.println("action=" + step.state().getActionString(step.action()) + " (" + step.action() + ")");
                }
            }
        }

        MatchSession session = new MatchSession(state, TMP_DIR);
        if (TEST_AGENT_FITNESS || PLAY_MATCHES) {
            session.logGame = !TEST_AGENT_FITNESS && ITERATION_COUNT <= 500;
            if (PLAY_MATCHES) {
                File file = new File(TMP_DIR + "/matches.txt");
                file.delete();
            }
            long start = System.currentTimeMillis();
            for (int i = 0; i < ITERATION_COUNT; i++) {
                session.playGame(NODE_COUNT);
            }
            long end = System.currentTimeMillis();
            System.out.println("Deaths: " + session.deathCount);
            System.out.println("Avg Damage: " + ((double) session.totalDamageTaken) / session.game_i);
            System.out.println("Avg Damage (Not Including Deaths): " + ((double) (session.totalDamageTaken - session.origState.player.origHealth * session.deathCount)) / (session.game_i - session.deathCount));
            System.out.println("Time Taken: " + (end - start));
            System.out.println("Time Taken (By Model): " + session.mcts.model.time_taken);
            System.out.println("Model: cache_size=" + session.mcts.model.cache.size() + ", " + session.mcts.model.cache_hits + "/" + session.mcts.model.calls + " hits (" + (double) session.mcts.model.cache_hits / session.mcts.model.calls + ")");
            System.out.print("--------------------");
        }

        if (GEN_TRAINING_MATCHES) {
            long start = System.currentTimeMillis();
            var games = new ArrayList<List<GameStep>>();
            for (int i = 0; i < 200; i++) {
                session.playTrainingGame(50);
                games.add(List.copyOf(session.states));
                for (GameStep step : session.states) {
                    step.state().clearNextStates();
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("Time Taken: " + (end - start));
            System.out.println("Time Taken (By Model): " + session.mcts.model.time_taken);
            System.out.println("Model: size=" + session.mcts.model.cache.size() + ", " + session.mcts.model.cache_hits + "/" + session.mcts.model.calls + " hits (" + (double) session.mcts.model.cache_hits / session.mcts.model.calls + ")");
            //            for (List<GameStep> game : games) {
            //                var lastState = game.get(game.size() - 1).state();
            //                float v = (float) (((double) lastState.player.health) / lastState.player.origHealth);
            //                for (int i = 0; i < game.size() - 1; i++) {
            //                    state = game.get(i).state();
            //                    var x = state.getInput();
            //                    System.out.println(state);
            //                    System.out.println(Arrays.toString(x));
            //                    System.out.println(v);
            //                    System.out.println(lastState.isTerminal() == 1 ? 1.0f : 0.0f);
            //                    float[] p = new float[state.numOfActions];
            //                    for (int j = 0; j < state.numOfActions; j++) {
            //                        p[j] = (float)(((double) state.n[j]) / state.total_n);
            //                    }
            //                    System.out.println(Arrays.toString(p));
            //                }
            //            }
            System.out.print("--------------------");
            var writer = new DataOutputStream(System.out);
            for (List<GameStep> game : games) {
                var lastState = game.get(game.size() - 1).state();
                float v = (float) (((double) lastState.player.health) / lastState.player.maxHealth);
                float v_win = lastState.isTerminal() == 1 ? 1.0f : 0.0f;
                for (int i = game.size() - 2; i >= 0; i--) {
                    state = game.get(i).state();
//                    if (state.isStochastic && i > 0) {
//                        var prevState = game.get(i - 1).state();
//                        var prevAction = game.get(i - 1).action();
//                        ChanceState cState = new ChanceState(state);
//                        for (int j = 0; j < 999; j++) {
//                            cState.getNextState(prevState, prevAction);
//                        }
//                        float p = ((float) cState.getCount(state)) / cState.total_n;
//                        v = v * p + (float) state.v_health * (1 - p);
//                        v_win = v_win * p + (float) state.v_win * (1 - p);
//                    }
                    var x = state.getInput();
                    for (int j = 0; j < x.length; j++) {
                        writer.writeFloat(x[j]);
                    }
                    writer.writeFloat(v);
                    writer.writeFloat(v_win);
                    for (int j = 0; j < state.prop.maxNumOfActions; j++) {
                        writer.writeFloat((float) (((double) state.n[j]) / state.total_n));
                    }
                }
            }
        }
    }

    private static void interactiveMode(GameState origState, String tmpDir) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int mode = 0;
        var states = new ArrayList<GameState>();
        GameState state = origState;
        boolean skipPrint = false;
        Model model = new Model(tmpDir);
        MCTS mcts = new MCTS();
        mcts.setModel(model);
        List<Integer> drawOrder = null;
        Enemy curEnemy = null;

        while (true) {
            if (mode == 0) {
                if (state.isTerminal() != 0) {
                    System.out.println("Game finished. Result " + (state.isTerminal() == 1 ? "Win" : "Loss"));
                    return;
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
                    }                    if (state.buffs != 0) {
                        System.out.print("  Buffs:");
                        if ((state.buffs & PlayerBuffs.CORRUPTION) != 0) {
                            System.out.println("    - Corruption");
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
                                state.getAction(i).type() == GameActionType.SELECT_CARD_DISCARD) {
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
            if (mode == 0) {
                if (line.equals("e")) {
                    states.add(state);
                    state = new GameState(state);
                    for (int i = 0; i < state.prop.maxNumOfActions; i++) {
                        if (state.isActionLegal(i) && state.getAction(i).type() == GameActionType.END_TURN) {
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
                    System.out.println(Arrays.toString(state.getInput()));
                    skipPrint = true;
                    continue;
                } else if (line.startsWith("n ")) {
                    try {
                        int count = Integer.parseInt(line.substring(2));
                        for (int i = state.total_n; i < count; i++) {
                            mcts.search(state, false, -1, true);
                        }
                        System.out.println(state.toStringReadable());
                        skipPrint = true;
                        continue;
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                } else if (line.startsWith("nn ")) {
                    try {
                        int count = Integer.parseInt(line.substring(3));
                        GameState s = state;
                        int move_i = 0;
                        do {
                            for (int i = s.total_n; i < count; i++) {
                                mcts.search(s, false, -1, true);
                            }
                            int max_n = -1;
                            int max_i = 0;
                            int number = 0;
                            for (int i = 0; i < s.prop.maxNumOfActions; i++) {
                                if (s.ns[i] != null) {
                                    number++;
                                    if (s.n[i] > max_n) {
                                        max_i = i;
                                        max_n = s.n[i];
                                    }
                                }
                            }
                            if (number == 0) {
                                break;
                            }
                            System.out.println("  " + (++move_i) + ". " + s.getActionString(max_i) +
                                    " (" + max_n + ", " + (s.q[max_i] / max_n + ")"));
                            State ns = s.ns[max_i];
                            if (ns instanceof ChanceState cState) {
                                break;
                            } else if (ns instanceof GameState ns2) {
                                if (ns2.isTerminal() != 0) {
                                    break;
                                } else {
                                    s = ns2;
                                }
                            }
                        } while (true);
                        skipPrint = true;
                        continue;
                    } catch (NumberFormatException e) {
                        // ignore
                    }
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
                    try {
                        String[] s = line.split(" ");
                        int enemyIdx = Integer.parseInt(s[1]);
                        int n = Integer.parseInt(s[2]);
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
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                } else if (line.startsWith("louse-dmg ")) {
                    try {
                        String[] s = line.split(" ");
                        int enemyIdx = Integer.parseInt(s[1]);
                        int n = Integer.parseInt(s[2]);
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
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                } else if (line.startsWith("em ")) {
                    try {
                        int enemyIdx = Integer.parseInt(line.substring(3));
                        if (0 <= enemyIdx && enemyIdx < state.enemies.size()) {
                            curEnemy = state.enemies.get(enemyIdx);
                            mode = 2;
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                } else if (line.equals("ph ")) {
                    try {
                        int hp = Integer.parseInt(line.substring(2));
                        if (hp >= 0) {
                            state.player.health = hp;
                        }
                        continue;
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                } else if (line.equals("b")) {
                    if (states.size() > 0) {
                        state = states.remove(states.size() - 1);
                    }
                    continue;
                } else if (line.equals("do")) {
                    drawOrder = new ArrayList<Integer>();
                    mode = 1;
                    continue;
                } else if (line.equals("tree")) {
                    MCTS.printTree(state, new OutputStreamWriter(System.out), 3);
                    continue;
                } else if (line.equals("matches")) {
                    MatchSession session = new MatchSession(state, "../tmp");
                    session.logGame = false;
                    long start = System.currentTimeMillis();
                    for (int i = 0; i < 100; i++) {
                        session.playGame(1000);
                    }
                    long end = System.currentTimeMillis();
                    System.out.println("Deaths: " + session.deathCount);
                    System.out.println("Avg Damage: " + ((double) session.totalDamageTaken) / session.game_i);
                    System.out.println("Avg Damage (Not Including Deaths): " + ((double) session.totalDamageTaken) / session.game_i);
                    System.out.println("Time Taken: " + (end - start));
                    System.out.println("Time Taken (By Model): " + session.mcts.model.time_taken);
                    System.out.println("Model: cache_size=" + session.mcts.model.cache.size() + ", " + session.mcts.model.cache_hits + "/" + session.mcts.model.calls + " hits (" + (double) session.mcts.model.cache_hits / session.mcts.model.calls + ")");
                    System.out.print("--------------------");
                    continue;
                } else if (line.equals("")) {
                    continue;
                }

                try {
                    int action = Integer.parseInt(line);
                    if (state.isActionLegal(action)) {
                        states.add(state);
                        state = new GameState(state);
                        state.doAction(action);
                        continue;
                    }
                } catch (NumberFormatException e) {
                    // ignore
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
}
