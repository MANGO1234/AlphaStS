package com.alphaStS;

import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyReadOnly;

import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

import static com.alphaStS.utils.Utils.formatFloat;

public class InteractiveMode {
    public static void interactiveStart(GameState origState, String modelDir) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        var states = new ArrayList<GameState>();
        GameState state = origState;
        Model model = null;
        try {
            model = new Model(modelDir);
        } catch (Exception e) {}
        MCTS mcts = new MCTS();
        mcts.setModel(model);
        List<String> history = new ArrayList<>();
        if (state.prop.realMoveRandomGen != null) {
            state.setSearchRandomGen(state.prop.realMoveRandomGen.createWithSeed(state.prop.realMoveRandomGen.nextLong(RandomGenCtx.Misc)));
        } else if (GameState.COMMON_RANDOM_NUMBER_VARIANCE_REDUCTION) {
            state.setSearchRandomGen(state.prop.random.createWithSeed(state.prop.random.nextLong(RandomGenCtx.CommonNumberVR)));
        } else {
            state.setSearchRandomGen(state.prop.random);
        }
        state.prop.random = new RandomGenInteractive(reader, history);

        boolean printState = true;
        boolean printAction = true;
        while (true) {
            if (state.isTerminal() != 0) {
                System.out.println("Game finished. Result is " + (state.isTerminal() == 1 ? "Win." : "Loss."));
            }
            if (printState) {
                int enemyIdx = 0;
                System.out.println("Enemies Alive: " + state.enemiesAlive);
                for (var enemy : state.getEnemiesForRead()) {
                    if (!enemy.isAlive()) {
                        continue;
                    }
                    System.out.println("Enemy " + (enemyIdx++) + ": " + enemy.getName());
                    System.out.println("  HP: " + enemy.getHealth());
                    if (enemy.getStrength() != 0) {
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
                    if (enemy.getRegeneration() > 0) {
                        System.out.println("  Regeneration: " + enemy.getRegeneration());
                    }
                    if (enemy.getMetallicize() > 0) {
                        System.out.println("  Metallicize: " + enemy.getMetallicize());
                    }
                    if (enemy.getLoseStrengthEot() != 0) {
                        System.out.println("  Gain Strength EOT: " + -enemy.getLoseStrengthEot());
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
                if (state.getPlayeForRead().getStrength() != 0) {
                    System.out.println("  Strength: " + state.getPlayeForRead().getStrength());
                }
                if (state.getPlayeForRead().getDexterity() != 0) {
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
                    if (state.getPlayeForRead().isEntangled()) {
                        System.out.println("  - Entangled");
                    }
                    if (state.getPlayeForRead().cannotDrawCard()) {
                        System.out.println("  - Cannot Draw Card");
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
                if (state.stateDesc != null) {
                    System.out.println("Previous Turn: " + state.stateDesc);
                }
            }
            if (printAction) {
                for (int i = 0; i < state.getLegalActions().length; i++) {
                    if (state.getAction(i).type() == GameActionType.PLAY_CARD ||
                            state.getAction(i).type() == GameActionType.SELECT_ENEMY ||
                            state.getAction(i).type() == GameActionType.SELECT_CARD_HAND ||
                            state.getAction(i).type() == GameActionType.SELECT_CARD_DISCARD ||
                            state.getAction(i).type() == GameActionType.SELECT_CARD_EXHAUST ||
                            state.getAction(i).type() == GameActionType.BEGIN_TURN ||
                            state.getAction(i).type() == GameActionType.USE_POTION ||
                            state.getAction(i).type() == GameActionType.SELECT_SCENARIO ||
                            state.getAction(i).type() == GameActionType.BEGIN_BATTLE) {
                        System.out.println(i + ". " + state.getActionString(i));
                    } else if (state.getAction(i).type() == GameActionType.END_TURN) {
                        System.out.println("e. End Turn");
                    } else {
                        System.out.println(state.getAction(i));
                        throw new RuntimeException();
                    }
                }
                System.out.println("a. Show Deck");
                System.out.println("s. Show Discard");
                for (int i = 0; i < state.getExhaustForRead().length; i++) {
                    if (state.getExhaustForRead()[i] > 0) {
                        System.out.println("x. Show Exhaust");
                        break;
                    }
                }
                printAction = true;
                printState = false;
            }

            System.out.print("> ");
            String line = reader.readLine();
            if (line.equals("exit")) {
                return;
            }
            history.add(line);

            if (line.equals("e")) {
                states.add(state);
                state.clearAllSearchInfo();
                state = state.clone(false);
                for (int i = 0; i < state.getLegalActions().length; i++) {
                    if (state.getAction(i).type() == GameActionType.END_TURN) {
                        state.prop.makingRealMove = true;
                        state.doAction(i);
                        state.prop.makingRealMove = false;
                        break;
                    }
                }
                for (int i = 0; i < state.getLegalActions().length; i++) {
                    if (state.getAction(i).type() == GameActionType.BEGIN_TURN) {
                        state.prop.makingRealMove = true;
                        state.doAction(i);
                        state.prop.makingRealMove = false;
                        break;
                    }
                }
                printState = true;
            } else if (line.equals("a")) {
                System.out.println("Deck");
                for (int i = 0; i < state.deck.length; i++) {
                    if (state.deck[i] > 0) {
                        System.out.println("  " + state.deck[i] + " " + state.prop.cardDict[i].cardName);
                    }
                }
            } else if (line.equals("s")) {
                System.out.println("Discard");
                for (int i = 0; i < state.discard.length; i++) {
                    if (state.discard[i] > 0) {
                        System.out.println("  " + state.discard[i] + " " + state.prop.cardDict[i].cardName);
                    }
                }
            } else if (line.equals("x")) {
                System.out.println("Exhaust");
                for (int i = 0; i < state.getExhaustForRead().length; i++) {
                    if (state.getExhaustForRead()[i] > 0) {
                        System.out.println("  " + state.getExhaustForRead()[i] + " " + state.prop.cardDict[i].cardName);
                    }
                }
            } else if (line.equals("i")) {
                printState = true;
            } else if (line.equals("input")) {
                System.out.println(Arrays.toString(state.getNNInput()));
            } else if (line.equals("eh")) {
                setEnemyHealth(reader, state, history);
                printState = true;
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
                printState = true;
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
                printState = true;
            } else if (line.equals("em")) {
                setEnemyMove(reader, state, history);
                printState = true;
            } else if (line.startsWith("ph ")) {
                int hp = parseInt(line.substring(3), -1);
                if (hp >= 0) {
                    state.getPlayerForWrite().setHealth(hp);
                }
                printState = true;
            } else if (line.equals("b")) {
                if (states.size() > 0) {
                    state = states.remove(states.size() - 1);
                }
                printState = true;
            } else if (line.startsWith("pot ")) {
                setPotionUtility(state, line);
            } else if (line.equals("do")) {
                setDrawOrder(reader, state, history);
            } else if (line.equals("rc")) { // remove card from hand
                removeCardFromHandSelectScreen(reader, state, history);
                state.clearAllSearchInfo();
            } else if (line.equals("ac")) { // add card to hand
                addCardToHandSelectScreen(reader, state, history);
                state.clearAllSearchInfo();
            } else if (line.equals("reset")) {
                state.clearAllSearchInfo();
                state.setSearchRandomGen(state.getSearchRandomGen().createWithSeed(state.searchRandomGen.nextLong(RandomGenCtx.Other)));
            } else if (line.equals("hist")) {
                for (String l : history) {
                    if (!l.startsWith("tree") && !l.startsWith("games") && !l.equals("hist") && !l.startsWith("nn ") && !l.startsWith("n ") && !l.startsWith("nnn ")) {
                        System.out.println(l);
                    }
                }
            } else if (line.equals("tree") || line.startsWith("tree ")) {
                printTree(state, line, modelDir);
            } else if (line.startsWith("n ")) {
                boolean prevRngOff = ((RandomGenInteractive) state.prop.random).rngOn;
                ((RandomGenInteractive) state.prop.random).rngOn = true;
                runMCTS(state, mcts, line);
                ((RandomGenInteractive) state.prop.random).rngOn = prevRngOff;
            } else if (line.startsWith("nn ")) {
                boolean prevRngOff = ((RandomGenInteractive) state.prop.random).rngOn;
                ((RandomGenInteractive) state.prop.random).rngOn = true;
                runNNPV(state, mcts, line);
                ((RandomGenInteractive) state.prop.random).rngOn = prevRngOff;
            } else if (line.startsWith("nnn ")) {
                boolean prevRngOff = ((RandomGenInteractive) state.prop.random).rngOn;
                ((RandomGenInteractive) state.prop.random).rngOn = true;
                runNNPV2(state, mcts, line);
                ((RandomGenInteractive) state.prop.random).rngOn = prevRngOff;
            } else if (line.equals("games") || line.startsWith("games ")) {
                boolean prevRngOff = ((RandomGenInteractive) state.prop.random).rngOn;
                ((RandomGenInteractive) state.prop.random).rngOn = true;
                runGames(modelDir, state, line);
                ((RandomGenInteractive) state.prop.random).rngOn = prevRngOff;
            } else if (line.equals("cmpSet") || line.startsWith("cmpSet ")) {
                runGamesCmpSetup(state, line);
            } else if (line.equals("cmp") || line.startsWith("cmp ")) {
                boolean prevRngOff = ((RandomGenInteractive) state.prop.random).rngOn;
                ((RandomGenInteractive) state.prop.random).rngOn = true;
                runGamesCmp(reader, modelDir, line);
                ((RandomGenInteractive) state.prop.random).rngOn = prevRngOff;
            } else if (line.equals("rng off")) {
                ((RandomGenInteractive) state.prop.random).rngOn = false;
            } else if (line.equals("rng on")) {
                ((RandomGenInteractive) state.prop.random).rngOn = true;
            } else if (line.equals("desc")) {
                System.out.println(state.getNNInputDesc());
            } else if (line.equals("")) {
            } else {
                int action = parseInt(line, -1);
                if (action < 0 || action >= state.getLegalActions().length) {
                    var _state = state;
                    var actionsOrig = IntStream.range(0, state.getLegalActions().length).mapToObj(_state::getActionString).toList();
                    var actions = actionsOrig.stream().map(String::toLowerCase).toList();
                    var actionStr = FuzzyMatch.getBestFuzzyMatch(line, actions);
                    if (actionStr != null) {
                        System.out.println("Fuzzy Match: " + actionsOrig.get(actions.indexOf(actionStr)));
                        action = actions.indexOf(actionStr);
                    }
                }
                if (action >= 0 && action <= state.getLegalActions().length) {
                    printState = true;
                    states.add(state);
                    state.clearAllSearchInfo();
                    state = state.clone(false);
                    state.prop.makingRealMove = true;
                    state.doAction(action);
                    state.prop.makingRealMove = false;
                } else {
                    System.out.println("Unknown Command.");
                }
            }
        }
    }

    private static void setDrawOrder(BufferedReader reader, GameState state, List<String> history) throws IOException {
        for (int i = 0; i < state.prop.cardDict.length; i++) {
            System.out.println(i + ". " + state.prop.cardDict[i].cardName);
        }
        var drawOrder = new ArrayList<Integer>();
        for (int i = 0; i < state.getDrawOrderForRead().size(); i++) {
            drawOrder.add(state.getDrawOrderForRead().ithCardFromTop(i));
        }
        while (true) {
            System.out.print("[");
            for (int i = 0; i < drawOrder.size(); i++) {
                System.out.print(state.prop.cardDict[drawOrder.get(i)].cardName + (i == drawOrder.size() - 1 ? "" : ", "));
            }
            System.out.println("]");

            System.out.print("> ");
            String line = reader.readLine();
            history.add(line);
            if (line.equals("b")) { // back
                return;
            } else if (line.equals("e")) {
                state.getDrawOrderForWrite().clear();
                for (int i = drawOrder.size() - 1; i >= 0; i--) {
                    state.getDrawOrderForWrite().pushOnTop(drawOrder.get(i));
                }
                System.out.print("[");
                for (int i = 0; i < drawOrder.size(); i++) {
                    var idx = state.getDrawOrderForRead().ithCardFromTop(i);
                    System.out.print(state.prop.cardDict[idx].cardName + (i == drawOrder.size() - 1 ? "" : ", "));
                }
                System.out.println("]");
                return;
            } else if (line.equals("p")) { // pop
                if (drawOrder.size() > 0) {
                    drawOrder.remove(drawOrder.size() - 1);
                }
            } else if (line.equals("clear")) {
                drawOrder.clear();
            } else {
                int cardIdx = parseInt(line, -1);
                if (cardIdx >= 0 && cardIdx < state.prop.cardDict.length) {
                    drawOrder.add(cardIdx);
                    continue;
                } else {
                    var cards = Arrays.stream(state.prop.cardDict).map((c) -> c.cardName.toLowerCase()).toList();
                    var card = FuzzyMatch.getBestFuzzyMatch(line, cards);
                    if (card != null) {
                        drawOrder.add(cards.indexOf(card));
                        continue;
                    }
                }
                System.out.println("Unknown Command.");
            }
        }
    }

    private static int selectEnemy(BufferedReader reader, GameState state, List<String> history, boolean onlyAlive) throws IOException {
        int idx = 0;
        int[] idxes = new int[onlyAlive ? state.enemiesAlive : state.getEnemiesForRead().size()];
        for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
            if (!onlyAlive || state.getEnemiesForRead().get(i).isAlive()) {
                System.out.println(idx + ". " + state.getEnemiesForRead().get(i).getName() + " (" + idx + ")");
                idxes[idx++] = i;
            }
        }
        if (idxes.length == 1) {
            return idxes[0];
        }
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            history.add(line);
            if (line.equals("b")) {
                return -1;
            }
            int enemyIdx = parseInt(line, -1);
            if (enemyIdx >= 0 && enemyIdx < idxes.length) {
                return idxes[enemyIdx];
            } else {
                var enemiesOrig = Arrays.stream(idxes).mapToObj((i) -> state.getEnemiesForRead().get(i).getName() + " (" + i + ")").toList();
                var enemies = enemiesOrig.stream().map(String::toLowerCase).toList();
                var enemy = FuzzyMatch.getBestFuzzyMatch(line, enemies);
                if (enemy != null) {
                    System.out.println("Fuzzy Match: " + enemiesOrig.get(enemies.indexOf(enemy)));
                    return idxes[enemies.indexOf(enemy)];
                }
            }
            System.out.println("Unknown Command.");
        }
    }

    private static void setEnemyMove(BufferedReader reader, GameState state, List<String> history) throws IOException {
        int curEnemyIdx = selectEnemy(reader, state, history,true);
        if (curEnemyIdx < 0) {
            return;
        }
        EnemyReadOnly curEnemy = state.getEnemiesForRead().get(curEnemyIdx);

        for (int i = 0; i < curEnemy.numOfMoves; i++) {
            System.out.println(i + ". " + curEnemy.getMoveString(state, i));
        }
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            history.add(line);
            if (line.equals("b")) {
                return;
            }
            int moveIdx = parseInt(line, -1);
            if (moveIdx >= 0 && moveIdx < curEnemy.numOfMoves) {
                state.getEnemiesForWrite().getForWrite(curEnemyIdx).setMove(moveIdx);
                return;
            } else {
                var movesOrig = IntStream.range(0, curEnemy.numOfMoves).mapToObj((i) -> curEnemy.getMoveString(state, i)).toList();
                var moves = movesOrig.stream().map((x) -> x.toLowerCase(Locale.ROOT)).toList();
                var move = FuzzyMatch.getBestFuzzyMatch(line, moves);
                if (move != null) {
                    System.out.println("Fuzzy Match: " + movesOrig.get(moves.indexOf(move)));
                    state.getEnemiesForWrite().getForWrite(curEnemyIdx).setMove(moves.indexOf(move));
                    return;
                }
            }
            System.out.println("Unknown Command.");
        }
    }

    private static void setEnemyHealth(BufferedReader reader, GameState state, List<String> history) throws IOException {
        int curEnemyIdx = selectEnemy(reader, state, history,false);
        if (curEnemyIdx < 0) {
            return;
        }

        while (true) {
            System.out.print("Health: ");
            String line = reader.readLine();
            history.add(line);
            if (line.equals("b")) {
                return;
            }
            int hp = parseInt(line, -1);
            if (hp >= 0) {
                if (hp > 0 && !state.getEnemiesForRead().get(curEnemyIdx).isAlive()) {
                    state.reviveEnemy(curEnemyIdx);
                    state.getEnemiesForWrite().getForWrite(curEnemyIdx).setHealth(hp);
                } else {
                    if (hp == 0) {
                        state.killEnemy(curEnemyIdx);
                    } else {
                        state.getEnemiesForWrite().getForWrite(curEnemyIdx).setHealth(hp);
                    }
                }
                return;
            }
        }
    }

    private static void setPotionUtility(GameState state, String line) {
        var s = line.substring(4).split(" ");
        var potionIdx = -1;
        var util = 0;
        if (s.length == 1 && state.prop.potions.size() == 1) {
            potionIdx = 0;
        } else if (s.length >= 2) {
            potionIdx = parseInt(s[0], -1);
            util = parseInt(s[1], 0);
        }
        if (potionIdx < 0) {
            System.out.println("Invalid Command.");
            return;
        }
        if (util == 0) {
            System.out.println("Set " + state.prop.potions.get(potionIdx) + " to unusable.");
            state.potionsState[potionIdx * 3] = 0;
            state.potionsState[potionIdx * 3 + 1] = 100;
            state.potionsState[potionIdx * 3 + 2] = 0;
        } else {
            System.out.println("Set " + state.prop.potions.get(potionIdx) + " utility to " + util + ".");
            state.potionsState[potionIdx * 3] = 1;
            state.potionsState[potionIdx * 3 + 1] = (short) util;
            state.potionsState[potionIdx * 3 + 2] = 1;
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

    static int selectScenarioForRandomization(BufferedReader reader, GameStateRandomization randomization, List<String> history) throws IOException {
        var info = randomization.listRandomizations();
        if (info.size() == 1) {
            return 0;
        }
        for (var entry : info.entrySet()) {
            System.out.println(entry.getKey() + ". " + entry.getValue().desc());
        }
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            history.add(line);
            int r = parseInt(line, -1);
            if (info.get(r) != null) {
                if (randomization instanceof GameStateRandomization.PotionUtilityRandomization pr) {
                    return r == 0 ? 0 : pr.getSteps() * (4 + r);
                } else {
                    return r;
                }
            }
            System.out.println("Unknown Command");
        }
    }

    static int selectEnemeyRandomInteractive(BufferedReader reader, GameState state, List<String> history) throws IOException {
        int i = 0;
        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
            System.out.println((i++) + ". " + enemy.getName());
        }
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            history.add(line);
            int r = parseInt(line, -1);
            if (r >= 0 && r < state.enemiesAlive) {
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
        var prevRandomization = state.prop.randomization;
        if (randomizationScenario >= 0) {
            state.prop.randomization = state.prop.randomization.fixR(randomizationScenario);
        }
        session.playGames(state, numberOfGames, nodeCount, true);
        state.prop.randomization = prevRandomization;
    }

    private static GameState state1;
    private static GameState state2;
    private static int startingAction1;
    private static int startingAction2;

    private static void runGamesCmpSetup(GameState state, String line) {
        String[] s = line.split(" ");
        if (s.length < 1) {
            return;
        }
        int startingAction = -1;
        if (s.length >= 3) {
            if (s[2].equals("e")) {
                startingAction = state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length - 1;
            } else {
                startingAction = parseInt(s[2], -1);
            }
            if (startingAction < 0 || startingAction >= state.getLegalActions().length) {
                System.out.println("Unknown action.");
                return;
            }
        }
        if (s[1].equals("1")) {
            state1 = state.clone(false);
            startingAction1 = startingAction;
        } else if (s[1].equals("2")) {
            state2 = state.clone(false);
            startingAction2 = startingAction;
        } else {
            System.out.println("cmpSet <1|2>");
        }
    }

    private static void runGamesCmp(BufferedReader reader, String modelDir, String line) throws IOException {
        String[] s = line.split(" ");
        int numberOfGames = 100;
        int numberOfThreads = 2;
        int nodeCount = 500;
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
            }
        }
        MatchSession session = new MatchSession(numberOfThreads, modelDir, modelDir);
        session.startingAction = startingAction1;
        session.origStateCmp = state2;
        session.startingActionCmp = startingAction2;
        var prevRandomization = state1.prop.randomization;
        if (randomizationScenario >= 0) {
            state1.prop.randomization = state1.prop.randomization.fixR(randomizationScenario);
        }
        if (state1 == null || state2 == null) {
            System.out.println("States not set");
            return;
        }
        System.out.println(state1);
        if (startingAction1 >= 0) {
            System.out.println("    " + state1.getActionString(startingAction1));
        }
        System.out.println(state2);
        if (startingAction2 >= 0) {
            System.out.println("    " + state2.getActionString(startingAction2));
        }
        System.out.println("Continue? (y/n)");
        System.out.print("> ");
        if (!reader.readLine().equals("y")) {
            return;
        }
        session.playGames(state1, numberOfGames, nodeCount, true);
        state1.prop.randomization = prevRandomization;
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
        Writer writer;
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
        GameState s = state;
        int move_i = 0;
        do {
            for (int i = s.total_n; i < count; i++) {
                mcts.search(s, false, -1);
            }
            int action = MCTS.getActionWithMaxNodesOrTerminal(s, null);
            if (action < 0) {
                break;
            }
            int max_n = s.n[action];
            System.out.println("  " + (++move_i) + ". " + s.getActionString(action) +
                    ": n=" + max_n + ", q=" + formatFloat(s.q_comb[action] / max_n) + ", q_win=" + formatFloat(s.q_win[action] / max_n) + ", q_health=" + formatFloat(s.q_health[action] / max_n) + " (" + s.q_health[action] / max_n * s.getPlayeForRead().getMaxHealth() + ")");
            State ns = s.ns[action];
            System.out.println(ns instanceof ChanceState);
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
                System.out.println("Unknown ns: " + Arrays.stream(state.ns).map(Objects::isNull).toList());
                break;
            }
        } while (true);
    }

    private static void runNNPV2(GameState state, MCTS mcts, String line) {
        int count = parseInt(line.substring(4), 1);
        if (state.searchFrontier != null && state.searchFrontier.total_n != state.total_n + 1) {
            state.clearAllSearchInfo();
        }
        for (int i = state.total_n; i < count; i++) {
            mcts.searchLine(state, false, true, -1);
        }
        state.searchFrontier.lines.values().stream().filter((x) -> {
            return x.state instanceof ChanceState || x.numberOfActions == ((GameState) x.state).getLegalActions().length;
        }).sorted((a, b) -> {
            if (a.internal == b.internal) {
                return -Integer.compare(a.n, b.n);
            } else if (a.internal) {
                return 1;
            } else {
                return -1;
            }
        }).limit(10).map((x) -> {
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
                    " (" + formatFloat(x.q_health / x.n * state.getPlayeForRead().getMaxHealth()) + ")";
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

class RandomGenInteractive extends RandomGen.RandomGenPlain {
    boolean rngOn = true;
    private final BufferedReader reader;
    private final List<String> history;

    public RandomGenInteractive(Random random, BufferedReader reader, List<String> history) {
        this.random = random;
        this.reader = reader;
        this.history = history;
    }

    public RandomGenInteractive(BufferedReader reader, List<String> history) {
        this.reader = reader;
        this.history = history;
    }

    @Override public int nextInt(int bound, RandomGenCtx ctx, Object arg) {
        if (rngOn) {
            return super.nextInt(bound, ctx, arg);
        }
        switch (ctx) {
        case WarpedTongs -> {
            try {
                return InteractiveMode.selectCardForWarpedTongs(reader, (GameState) arg, history);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        case BeginningOfGameRandomization -> {
            try {
                return InteractiveMode.selectScenarioForRandomization(reader, (GameStateRandomization) arg, history);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        case RandomEnemyGeneral, RandomEnemyJuggernaut, RandomEnemySwordBoomerang -> {
            try {
                return InteractiveMode.selectEnemeyRandomInteractive(reader, (GameState) arg, history);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        default -> {
            return super.nextInt(bound, ctx, arg);
        }
        }
    }

    public RandomGen getCopy() {
        return new RandomGenInteractive(getRandomClone(), reader, history);
    }

    public RandomGen createWithSeed(long seed) {
        random = new Random();
        random.setSeed(seed);
        return new RandomGenInteractive(random, reader, history);
    }
}