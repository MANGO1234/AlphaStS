package com.alphaStS;

import com.alphaStS.enemy.*;
import com.alphaStS.enums.OrbType;
import com.alphaStS.utils.ScenarioStats;
import com.alphaStS.utils.Tuple;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.alphaStS.utils.Utils.formatFloat;

public class InteractiveMode {
    private PrintStream out = System.out;
    private List<MCTS> threadMCTS = new ArrayList<>();
    private String modelDir;

    private void allocateThreadMCTS(int numThreads) {
        for (int i = threadMCTS.size(); i < numThreads; i++) {
            Model model = null;
            try {
                model = new Model(modelDir);
            } catch (Exception e) {
            }
            threadMCTS.add(new MCTS());
            threadMCTS.get(i).setModel(model);
        }
    }

    public InteractiveMode() {
    }

    public InteractiveMode(PrintStream out) {
        this.out = out;
    }

    public void interactiveStart(GameState origState, String saveDir, String modelDir) throws IOException {
        List<String> history = new ArrayList<>();
        BufferedWriter writer;
        out.println("Model: " + modelDir);
        out.println("****************************************************");
        try {
            interactiveStartH(origState, saveDir, modelDir, history);
        } catch (Exception e) {
            writer = new BufferedWriter(new FileWriter(saveDir + "/session-crash.txt"));
            writer.write(String.join("\n", filterHistory(history)) + "\n");
            writer.close();
            throw e;
        }
    }

    public void interactiveApplyHistory(GameState origState, List<String> history) {
        try {
            interactiveStartH(origState, null, null, new ArrayList<>(history));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void interactiveStartH(GameState origState, String saveDir, String modelDir, List<String> history) throws IOException {
        InteractiveReader reader = new InteractiveReader(this, new InputStreamReader(System.in));
        var states = new ArrayList<GameState>();
        GameState state = origState;
        Model model = null;
        boolean isApplyingHistory = false;
        RandomGen prevSearchRandomGen = null;
        RandomGen prevRealMoveRandomGen = null;
        RandomGen prevRandom = null;
        List<String> nnPV = new ArrayList<>();
        this.modelDir = modelDir;
        allocateThreadMCTS(1);
        MCTS mcts = threadMCTS.get(0);
        if (history.size() > 0) {
            reader.addCmdsToQueue(history);
            isApplyingHistory = true;
            prevSearchRandomGen = state.getSearchRandomGen();
            prevRealMoveRandomGen = state.prop.realMoveRandomGen;
            prevRandom = state.prop.random;
        } else {
            interactiveRecordSeed(state, history);
        }
        state.setSearchRandomGen(state.prop.random);
        state.prop.random = new RandomGenInteractive(this, reader, history);
        state.prop.realMoveRandomGen = null;

        boolean printState = true;
        boolean printAction = true;
        while (true) {
            if (printState) {
                printState(state, states);
                printAction = true;
            }
            if (printAction) {
                printAction(state);
                printAction = true;
                printState = false;
            }
            out.print("> ");
            String line = reader.readLine();
            if (line.equals("exit")) {
                if (isApplyingHistory) {
                    state.setSearchRandomGen(prevSearchRandomGen);
                    state.prop.realMoveRandomGen = prevRealMoveRandomGen;
                    state.prop.random = prevRandom;
                }
                return;
            }
            history.add(line);

            if (line.equals("e") || line.equals("End Turn")) {
                states.add(state);
                state.clearAllSearchInfo();
                if (!isApplyingHistory) {
                    state = state.clone(false);
                }
                for (int i = 0; i < state.getLegalActions().length; i++) {
                    if (state.getAction(i).type() == GameActionType.END_TURN) {
                        history.add("# End of Turn");
                        history.add("# " + state);
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
                        history.add("# Start of Turn");
                        history.add("# " + state);
                        break;
                    }
                }
                printState = true;
            } else if (line.startsWith("#")) {
            } else if (line.equals("a")) {
                out.println("Deck");
                for (int i = 0; i < state.getDeckForRead().length; i++) {
                    if (state.getDeckForRead()[i] > 0) {
                        out.println("  " + state.getDeckForRead()[i] + " " + state.prop.cardDict[i].cardName);
                    }
                }
            } else if (line.equals("s")) {
                out.println("Discard");
                var discard = GameStateUtils.getCardArrCounts(state.getDiscardArrForRead(), state.getNumCardsInDiscard(), state.prop.realCardsLen);
                for (int i = 0; i < discard.length; i++) {
                    if (discard[i] > 0 && (!state.prop.discard0CardOrderMatters || state.prop.cardDict[i].realEnergyCost() != 0)) {
                        out.println("  " + discard[i] + " " + state.prop.cardDict[i].cardName);
                    }
                }
                if (state.prop.discard0CardOrderMatters) {
                    for (int i = 0; i < state.discardArrLen; i++) {
                        if (state.prop.cardDict[state.discardArr[i]].realEnergyCost() == 0) {
                            out.println("  " + state.prop.cardDict[state.discardArr[i]].cardName);
                        }
                    }
                }
            } else if (line.equals("x")) {
                out.println("Exhaust");
                for (int i = 0; i < state.getExhaustForRead().length; i++) {
                    if (state.getExhaustForRead()[i] > 0) {
                        out.println("  " + state.getExhaustForRead()[i] + " " + state.prop.cardDict[i].cardName);
                    }
                }
            } else if (line.equals("i")) {
                printState = true;
            } else if (line.equals("input")) {
                out.println(Arrays.toString(state.getNNInput()));
            } else if (line.startsWith("model ")) {
                try {
                    model = new Model(line.split(" ")[1]);
                    mcts.setModel(model);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (line.equals("eh")) {
                setEnemyHealth(reader, state, history);
                printState = true;
            } else if (line.equals("eho")) {
                setEnemyHealthOriginal(reader, state, history);
                printState = true;
            } else if (line.equals("em")) {
                setEnemyMove(reader, state, history);
                printState = true;
            } else if (line.equals("eo")) {
                setEnemyOther(reader, state, history);
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
                interactiveRecordSeed(state, history);
            } else if (line.equals("hist")) {
                for (String l : filterHistory(history)) {
                    out.println(l);
                }
            } else if (line.equals("save") || line.startsWith("save ")) {
                String suffix = "";
                if (line.startsWith("save ")) {
                    suffix = line.split(" ")[1];
                }
                if (suffix.equals("play")) {
                    out.print("List.of(\"\"");
                    List<String> hist = filterHistory(history);
                    hist.remove(0);
                    hist.remove(0);
                    hist = hist.stream().filter(x -> !x.startsWith("#")).collect(Collectors.toList());
                    for (String l : hist) {
                        out.print(", \"" + l + "\"");
                    }
                    out.println(", \"exit\")");
                } else {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(saveDir + "/session" + suffix + ".txt"));
                    writer.write(String.join("\n", filterHistory(history)) + "\n");
                    writer.close();
                }
            } else if (line.equals("load") || line.startsWith("load ")) {
                String suffix = "";
                if (line.startsWith("load ")) {
                    suffix = line.split(" ")[1];
                }
                BufferedReader fileReader = new BufferedReader(new FileReader(saveDir + "/session" + suffix + ".txt"));
                reader.addCmdsToQueue(fileReader.lines().toList());
                fileReader.close();
            } else if (line.equals("tree explore")) {
                exploreTree(state, reader, modelDir);
            } else if (line.equals("tree") || line.startsWith("tree ")) {
                printTree(state, line, modelDir);
            } else if (line.startsWith("n ")) {
                boolean prevRngOff = ((RandomGenInteractive) state.prop.random).rngOn;
                ((RandomGenInteractive) state.prop.random).rngOn = true;
                runMCTS(state, mcts, line);
                interactiveRecordSeed(state, history);
                ((RandomGenInteractive) state.prop.random).rngOn = prevRngOff;
            } else if (line.startsWith("nn ")) {
                boolean prevRngOff = ((RandomGenInteractive) state.prop.random).rngOn;
                if (line.substring(3).equals("exec")) {
                    reader.addCmdsToQueue(nnPV);
                } else if (line.substring(3).equals("execv")) {
                    reader.addCmdsToQueue(nnPV.subList(0, nnPV.size() - 1));
                } else {
                    ((RandomGenInteractive) state.prop.random).rngOn = true;
                    nnPV.clear();
                    runNNPV(state, mcts, nnPV, line, true);
                    interactiveRecordSeed(state, history);
                    ((RandomGenInteractive) state.prop.random).rngOn = prevRngOff;
                }
            } else if (line.startsWith("nnc ")) {
                boolean prevRngOff = ((RandomGenInteractive) state.prop.random).rngOn;
                ((RandomGenInteractive) state.prop.random).rngOn = true;
                runNNPVChance(reader, state, mcts, line);
                interactiveRecordSeed(state, history);
                ((RandomGenInteractive) state.prop.random).rngOn = prevRngOff;
            } else if (line.startsWith("nnv ")) {
                boolean prevRngOff = ((RandomGenInteractive) state.prop.random).rngOn;
                ((RandomGenInteractive) state.prop.random).rngOn = true;
                runNNPVVolatility(state, line);
                interactiveRecordSeed(state, history);
                ((RandomGenInteractive) state.prop.random).rngOn = prevRngOff;
            } else if (line.startsWith("nnn ")) {
                boolean prevRngOff = ((RandomGenInteractive) state.prop.random).rngOn;
                ((RandomGenInteractive) state.prop.random).rngOn = true;
                runNNPV2(state, mcts, line);
                interactiveRecordSeed(state, history);
                ((RandomGenInteractive) state.prop.random).rngOn = prevRngOff;
            } else if (line.equals("progressive")) {
                Configuration.USE_PROGRESSIVE_WIDENING = !Configuration.USE_PROGRESSIVE_WIDENING;
                out.println("Progressive Widening: " + (Configuration.USE_PROGRESSIVE_WIDENING ? "On" : "Off"));
            } else if (line.equals("progressive2")) {
                Configuration.PROGRESSIVE_WIDENING_IMPROVEMENTS = !Configuration.PROGRESSIVE_WIDENING_IMPROVEMENTS;
                out.println("Progressive Widening Improvement: " + (Configuration.PROGRESSIVE_WIDENING_IMPROVEMENTS ? "On" : "Off"));
            } else if (line.equals("progressive3")) {
                Configuration.PROGRESSIVE_WIDENING_IMPROVEMENTS2 = !Configuration.PROGRESSIVE_WIDENING_IMPROVEMENTS2;
                out.println("Progressive Widening Improvement 2: " + (Configuration.PROGRESSIVE_WIDENING_IMPROVEMENTS2 ? "On" : "Off"));
            } else if (line.equals("games") || line.startsWith("games ")) {
                boolean prevRngOff = ((RandomGenInteractive) state.prop.random).rngOn;
                ((RandomGenInteractive) state.prop.random).rngOn = true;
                runGames(modelDir, state, line);
                interactiveRecordSeed(state, history);
                ((RandomGenInteractive) state.prop.random).rngOn = prevRngOff;
            } else if (line.equals("cmpSet") || line.startsWith("cmpSet ")) {
                runGamesCmpSetup(state, line);
            } else if (line.equals("cmp") || line.startsWith("cmp ")) {
                boolean prevRngOff = ((RandomGenInteractive) state.prop.random).rngOn;
                ((RandomGenInteractive) state.prop.random).rngOn = true;
                runGamesCmp(reader, modelDir, line);
                interactiveRecordSeed(state, history);
                ((RandomGenInteractive) state.prop.random).rngOn = prevRngOff;
            } else if (line.startsWith("seed ")) {
                interactiveSetSeed(state, Long.parseLong(line.split(" ")[1]), Long.parseLong(line.split(" ")[2]));
            } else if (line.equals("rng off")) {
                ((RandomGenInteractive) state.prop.random).rngOn = false;
            } else if (line.equals("rng on")) {
                ((RandomGenInteractive) state.prop.random).rngOn = true;
            } else if (line.equals("test off")) {
                state.prop.testNewFeature = false;
            } else if (line.equals("test on")) {
                state.prop.testNewFeature = true;
            } else if (line.equals("stateDesc off")) {
                state.prop.stateDescOn = false;
            }  else if (line.equals("stateDesc on")) {
                state.prop.stateDescOn = true;
            } else if (line.equals("desc")) {
                GameStateUtils.writeStateDescription(state, new BufferedWriter(new OutputStreamWriter(out)));
            } else if (line.equals("")) {
            } else {
                int action = parseInt(line, -1);
                if (action < 0 || action >= state.getLegalActions().length) {
                    var _state = state;
                    var actionsOrig = IntStream.range(0, state.getLegalActions().length).mapToObj(_state::getActionString).toList();
                    var actions = actionsOrig.stream().map(String::toLowerCase).toList();
                    var actionStr = FuzzyMatch.getBestFuzzyMatch(line.toLowerCase(), actions);
                    if (actionStr != null) {
                        out.println("Fuzzy Match: " + actionsOrig.get(actions.indexOf(actionStr)));
                        action = actions.indexOf(actionStr);
                    }
                }
                if (action >= 0 && action <= state.getLegalActions().length) {
                    printState = reader.lines.size() == 0;
                    printAction = printState;
                    states.add(state);
                    state.clearAllSearchInfo();
                    if (!isApplyingHistory) {
                        state = state.clone(false);
                    }
                    state.prop.makingRealMove = true;
                    state.prop.isInteractive = true;
                    state.doAction(action);
                    state.prop.isInteractive = false;
                    state.prop.makingRealMove = false;
                } else {
                    out.println("Unknown Command.");
                }
            }
        }
    }

    private static void interactiveSetSeed(GameState state, long a, long b) {
        ((RandomGenInteractive) state.prop.random).random.setSeed(a);
        ((RandomGen.RandomGenPlain) state.getSearchRandomGen()).random.setSeed(b);
    }

    private static void interactiveRecordSeed(GameState state, List<String> history) {
        history.add("seed " + state.prop.random.getSeed(null)  + " " + state.getSearchRandomGen().getSeed(null));
    }

    private static List<String> filterHistory(List<String> history) {
        return history.stream().filter((l) ->
                !l.startsWith("tree") && !l.startsWith("games") && !l.equals("hist") && !l.equals("save") && !l.equals("load")
                        && !l.startsWith("nn ") && !l.startsWith("n ") && !l.startsWith("nnc ") && !l.startsWith("nnn ") && !l.startsWith("nnv ")
                        && !l.startsWith("cmpSet ") && !l.startsWith("cmp ") && !l.startsWith("save ") && !l.startsWith("load ")
        ).collect(Collectors.toList());
    }

    public void interactiveStart(List<GameStep> game, String modelDir) throws IOException {
        int idx = 0;
        GameState state = game.get(0).state();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            var states = game.stream().map(GameStep::state).limit(idx).toList();
            printState(state, states);
            if (idx > 0) {
                out.println("Previous Action: " + game.get(idx - 1).state().getActionString(game.get(idx - 1).action()));
            }
            out.println();
            out.println(game.get(idx).stateStr == null ? state : game.get(idx).stateStr);
            out.println();
            out.print("> ");
            String line;
            line = reader.readLine();
            if (line.equals("exit")) {
                return;
            } else if (line.equals("n")) {
                if (idx < game.size() - 1) {
                    idx++;
                    state = game.get(idx).state();
                }
            } else if (line.equals("b")) {
                if (idx > 0) {
                    idx--;
                    state = game.get(idx).state();
                }
            } else if (line.startsWith("g ")) {
                int i = parseInt(line.substring(2), -1);
                if (i >= 0 && i < game.size()) {
                    idx = i;
                    state = game.get(i).state();
                }
            } else if (line.startsWith("m")) {
                try {
                    var s = state.clone(false);
                    s.prop = state.prop.clone();
                    s.prop.random = new RandomGen.RandomGenPlain();
                    s.prop.makingRealMove = false;
                    interactiveStartH(s, null, modelDir, new ArrayList<>());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (line.startsWith("analyze ")) {
                String[] args = line.split(" ");
                int numberOfGames = 100;
                int numberOfThreads = 2;
                int nodeCount = 500;
                if (args.length > 1) {
                    for (int i = 1; i < args.length; i++) {
                        if (args[i].startsWith("c=")) {
                            numberOfGames = parseInt(args[i].substring(2), 0);
                        }
                        if (args[i].startsWith("t=")) {
                            numberOfThreads = parseInt(args[i].substring(2), 2);
                        }
                        if (args[i].startsWith("n=")) {
                            nodeCount = parseInt(args[i].substring(2), 500);
                        }
                    }
                }

                var statsArr = new ArrayList<ScenarioStats>();
                List<MatchSession.Game> curGames = new ArrayList<>();
                MatchSession session = new MatchSession(numberOfThreads, modelDir);
                for (int i = 0; i < game.size(); i++) {
                    var step = game.get(i);
                    curGames.stream().forEach(g -> g.steps().remove(0));
                    curGames = new ArrayList<>(curGames.stream().filter(g -> g.steps().get(0).state().equals(step.state())).toList());
                    out.println("****************** " + i + "/" + game.size() + " (" + (numberOfGames - curGames.size()) + ")");
                    step.state().prop.makingRealMove = false;
                    curGames.addAll(session.playGames(step.state(), numberOfGames - curGames.size(), nodeCount, true, false, true));
                    var stats = new ScenarioStats();
                    for (MatchSession.Game g : curGames) {
                        stats.add(g.steps(), 0);
                    }
                    statsArr.add(stats);
                }

                for (int i = 0; i < game.size(); i++) {
                    out.format("\n%-6s%-20s%-20s%-20s", "Index", "Q", "Death Percentage", "Average Damage");
                    for (int j = 0; j < statsArr.get(0).potionsUsed.length; j++) {
                        String potionName = state.prop.potions.get(j).toString();
                        out.format("%-20s", potionName);
                    }
                    out.println();
                    ScenarioStats stats = statsArr.get(i);
                    double deathPercentage = (double) stats.deathCount / stats.numOfGames * 100;
                    double avgDamage = (double) stats.totalDamageTaken / stats.numOfGames;
                    double avgQ = stats.finalQComb / stats.numOfGames;
                    out.format("%-6d%-20.3f%-20.3f%-20.3f", i, avgQ, deathPercentage, avgDamage);
                    for (int j = 0; j < stats.potionsUsed.length; j++) {
                        double potionPercentage = (double) stats.potionsUsed[j] / (stats.numOfGames - stats.deathCount) * 100;
                        out.format("%-20.3f", potionPercentage);
                    }
                    out.println();
                    var step = game.get(i);
                    out.println(i + ". " + step.state());
                    if (step.action() >= 0) {
                        out.print("action=" + step.state().getActionString(step.action()));
                        var nextStep = game.get(i + 1);
                        if (nextStep.state().stateDesc != null) {
                            out.print(" [" + nextStep.state().stateDesc + "]");
                        }
                        out.println();
                    }
                }

                out.format("\n%-6s%-20s%-20s%-20s", "Index", "Q", "Death Percentage", "Average Damage");
                for (int i = 0; i < statsArr.get(0).potionsUsed.length; i++) {
                    String potionName = state.prop.potions.get(i).toString();
                    out.format("%-20s", potionName);
                }
                out.println();

                for (int i = 0; i < statsArr.size(); i++) {
                    ScenarioStats stats = statsArr.get(i);
                    double deathPercentage = (double) stats.deathCount / stats.numOfGames * 100;
                    double avgDamage = (double) stats.totalDamageTaken / stats.numOfGames;
                    double avgQ = stats.finalQComb / stats.numOfGames;
                    out.format("%-6d%-20.3f%-20.3f%-20.3f", i, avgQ, deathPercentage, avgDamage);
                    for (int j = 0; j < stats.potionsUsed.length; j++) {
                        double potionPercentage = (double) stats.potionsUsed[j] / (stats.numOfGames - stats.deathCount) * 100;
                        out.format("%-20.3f", potionPercentage);
                    }
                    out.println();
                }
                out.println();

                out.format("python.exe ./plot.py -a \"Q\" \"[%s]\"", statsArr.stream().map(s -> String.format("%.3f", (s.finalQComb / s.numOfGames))).collect(Collectors.joining(", ")));
                out.format(" \"Death Percentage\" \"[%s]\"", statsArr.stream().map(s -> String.format("%.3f", (double) s.deathCount / s.numOfGames * 100)).collect(Collectors.joining(", ")));
                out.format(" \"Average Damage\" \"[%s]\"", statsArr.stream().map(s -> String.format("%.3f", (double) s.totalDamageTaken / s.numOfGames)).collect(Collectors.joining(", ")));
                for (int i = 0; i < statsArr.get(0).potionsUsed.length; i++) {
                    final int ii = i;
                    out.format(" \"%s\" \"[%s]\"", state.prop.potions.get(i), statsArr.stream().map(s -> String.format("%.3f", (double) s.potionsUsed[ii] / (s.numOfGames - s.deathCount))).collect(Collectors.joining(", ")));
                }
                out.println();
            } else if (line.startsWith("check ")) {
                String[] args = line.split(" ");
                int nodeCount = 500;
                if (args.length > 1) {
                    nodeCount = parseInt(args[1], 500);
                }

                Model model = new Model(modelDir);
                var m = new MCTS();
                m.setModel(model);
                var pvs = new ArrayList<Tuple<Tuple<Integer, Integer>, Tuple<List<GameStep>, List<GameStep>>>>();
                var start = 0;
                while (start < game.size() - 1) {
                    out.println(start + "/" + game.size());
                    var step = game.get(start);
                    step.state().prop.makingRealMove = false;
                    List<String> pv = new ArrayList<>();
                    runNNPV(step.state().clone(false), m, pv, "nn " + nodeCount, true);
                    List<GameStep> pv1 = new ArrayList<>();
                    var s = step.state().clone(false);
                    for (int i = 0; i < pv.size(); i++) {
                        for (int j = 0; j < s.getLegalActions().length; j++) {
                            if (pv.get(i).equals(s.getActionString(j))) {
                                pv1.add(new GameStep(s, j));
                                s = s.clone(false);
                                s.doAction(j);
                            }
                        }
                    }
                    if (!s.isStochastic) {
                        pv1.add(new GameStep(s, -1));
                    }

                    var end = start + 1;
                    var pv2 = new ArrayList<GameStep>();
                    pv2.add(new GameStep(game.get(start).state(), game.get(start).action()));
                    while (end < game.size() && !game.get(end).state().isStochastic) {
                        pv2.add(new GameStep(game.get(end).state(), game.get(end).action()));
                        end++;
                    }
                    if (pv2.get(pv2.size() - 1).action() >= 0) {
                        s = pv2.get(pv2.size() - 1).state().clone(false);
                        s.doAction(pv2.get(pv2.size() - 1).action());
                        if (!s.isStochastic) {
                            pv2.add(new GameStep(s, -1));
                        }
                    }

                    var idxes = pv2.stream().map(pv1::indexOf).toList();
                    var maxIdx = -1;
                    var max = -1;
                    for (int i = 0; i < idxes.size(); i++) {
                        if (idxes.get(i) > maxIdx) {
                            maxIdx = idxes.get(i);
                            max = i;
                        }
                    }
                    if (maxIdx != -1) {
                        end = Math.min(end, start + max + 1);
                    } else {
                        pvs.add(new Tuple<>(new Tuple(start, end), new Tuple(pv1, pv2)));
                    }
                    start = end;
                }

                for (int i = 0; i < pvs.size(); i++) {
                    out.println("Move " + pvs.get(i).v1().v1() + "-" + (pvs.get(i).v1().v2() - 1));
                    out.println(pvs.get(i).v2().v1().get(0).state());
                    var pv1 = pvs.get(i).v2().v1();
                    var pv2 = pvs.get(i).v2().v2();
                    out.println("Original:");
                    for (int j = 0; j < pv2.size(); j++) {
                        if (pv2.get(j).action() >= 0) {
                            out.println("  " + (j + 1) + ". " + pv2.get(j).state().getActionString(pv2.get(j).action()));
                        }
                    }
                    out.println("Better:");
                    for (int j = 0; j < pv1.size(); j++) {
                        if (pv1.get(j).action() >= 0) {
                            out.println("  " + (j + 1) + ". " + pv1.get(j).state().getActionString(pv1.get(j).action()));
                        }
                    }
                    out.println();
                }
            }
        }
    }

    private void printState(GameState state, List<GameState> states) {
        if (state.isTerminal() != 0) {
            out.println("Battle finished. Result is " + (state.isTerminal() == 1 ? "Win." : "Loss."));
        }
        int enemyIdx = 0, enemyArrayIdx = -1;
        out.println("Enemies Alive: " + state.enemiesAlive);
        for (var enemy : state.getEnemiesForRead()) {
            enemyArrayIdx++;
            if (!(enemy.isAlive() || enemy.property.canSelfRevive)) {
                continue;
            }
            out.println("Enemy " + (enemyIdx++) + ": " + enemy.getName());
            out.println("  HP: " + enemy.getHealth() + "/" + enemy.property.origHealth);
            if (enemy.getStrength() != 0) {
                out.println("  Strength: " + enemy.getStrength());
            }
            if (enemy.getBlock() > 0) {
                out.println("  Block: " + enemy.getBlock());
            }
            if (enemy.getArtifact() > 0) {
                out.println("  Artifact: " + enemy.getArtifact());
            }
            if (enemy.getVulnerable() > 0) {
                out.println("  Vulnerable: " + enemy.getVulnerable());
            }
            if (enemy.getWeak() > 0) {
                out.println("  Weak: " + enemy.getWeak());
            }
            if (enemy.getPoison() != 0) {
                out.println("  Poison: " + enemy.getPoison());
            }
            if (enemy.getCorpseExplosion() > 0) {
                out.println("  Corpse Explosion: " + enemy.getCorpseExplosion());
            }
            if (enemy.getRegeneration() > 0) {
                out.println("  Regeneration: " + enemy.getRegeneration());
            }
            if (enemy.getMetallicize() > 0) {
                out.println("  Metallicize: " + enemy.getMetallicize());
            }
            if (enemy.getLoseStrengthEot() != 0) {
                out.println("  Gain Strength EOT: " + -enemy.getLoseStrengthEot());
            }
            if (enemy instanceof Enemy.RedLouse louse) {
                if (!louse.hasCurledUp()) {
                    out.println("  Curl Up: " + louse.getCurlUpAmount());
                }
            } else if (enemy instanceof Enemy.GreenLouse louse) {
                if (!louse.hasCurledUp()) {
                    out.println("  Curl Up: " + louse.getCurlUpAmount());
                }
            } else if (enemy instanceof Enemy.TheGuardian guardian) {
                out.println("  Mode Shift Damage: " + guardian.getModeShiftDmg() + "/" + guardian.getMaxModeShiftDmg());
            } else if (enemy instanceof EnemyCity.BronzeOrb orb) {
                if (!orb.usedStasis()) {
                    out.println("  Stasis: Not Used");
                } else if (orb.getStasisCard() >= 0) {
                    out.println("  Stasis: Used (" + state.prop.cardDict[orb.getStasisCard()].cardName + ")");
                } else {
                    out.println("  Stasis: Used");
                }
            } else if (enemy instanceof EnemyBeyond.Darkling darkling) {
                if (darkling.getNipDamage() > 0) {
                    out.println("  Nip Damage: " + darkling.getNipDamage());
                }
            } else if (enemy instanceof EnemyBeyond.GiantHead giantHead) {
                if (giantHead.getMove() != EnemyBeyond.GiantHead.IT_IS_TIME) {
                    out.println("  Turn(s) Until Large Attack: " + giantHead.getTurnUntilLargeAttack());
                }
                if (giantHead.getSlow() > 0) {
                    out.println("  Slow: " + giantHead.getSlow());
                }
            } else if (enemy instanceof EnemyBeyond.Nemesis nemesis) {
                if (nemesis.isIntangible()) {
                    out.println("  Intangible");
                }
            } else if (enemy instanceof EnemyEnding.CorruptHeart heart) {
                out.println("  Invincible: " + heart.getInvincible());
                out.println("  Beat Of Death: " + heart.getBeatOfDeath());
                out.println("  Buff Count: " + heart.getBuffCount());
            }
            if (states.size() > 0) {
                GameState prevState = states.get(states.size() - 1);
                if (state.prop.hasRunicDome) {
                    String prevMove = prevState.getEnemiesForRead().get(enemyArrayIdx).getMoveString(prevState, enemy.getMove());
                    String prevPrevMove = prevState.getEnemiesForRead().get(enemyArrayIdx).getMoveString(prevState, enemy.getLastMove());
                    out.println("  Last Move: " + prevMove);
                    out.println("  Last Last Move: " + prevPrevMove);
                } else {
                    String prevMove = state.getEnemiesForRead().get(enemyArrayIdx).getMoveString(state, enemy.getMove());
                    String prevPrevMove = prevState.getEnemiesForRead().get(enemyArrayIdx).getMoveString(prevState, enemy.getLastMove());
                    out.println("  Move: " + prevMove);
                    out.println("  Last Move: " + prevPrevMove);
                }
            } else {
                String prevMove = state.getEnemiesForRead().get(enemyArrayIdx).getMoveString(state, enemy.getMove());
                String prevPrevMove = state.getEnemiesForRead().get(enemyArrayIdx).getMoveString(state, enemy.getLastMove());
                out.println("  Move: " + prevMove);
                out.println("  Last Move: " + prevPrevMove);
            }
            out.println();
        }
        out.println("Player");
        out.println("  Energy: " + state.energy);
        int maxPossibleHealth = state.getMaxPossibleHealth();
        int health = state.getPlayeForRead().getHealth();
        out.println("  HP: " + health + ((health != maxPossibleHealth) ? " (Max Possible HP=" + maxPossibleHealth + ")" : ""));
        if (state.getPlayeForRead().getBlock() > 0) {
            out.println("  Block: " + state.getPlayeForRead().getBlock());
        }
        if (state.getOrbs() != null) {
            var orbs = state.getOrbs();
            out.print("  Orbs (" + orbs.length / 2 + "): ");
            for (int i = orbs.length - 2; i >= 0; i -= 2) {
                out.print((i == orbs.length - 2 ? "" : ", ") + OrbType.values()[orbs[i]].displayName);
                if (orbs[i] == OrbType.DARK.ordinal()) {
                    out.print("(" + orbs[i + 1] + ")");
                }
            }
            out.println();
        }
        if (state.getPlayeForRead().getStrength() != 0) {
            out.println("  Strength: " + state.getPlayeForRead().getStrength());
        }
        if (state.getPlayeForRead().getDexterity() != 0) {
            out.println("  Dexterity: " + state.getPlayeForRead().getDexterity());
        }
        if (state.getPlayeForRead().getPlatedArmor() != 0) {
            out.println("  Plated Armor: " + state.getPlayeForRead().getPlatedArmor());
        }
        if (state.getFocus() != 0) {
            out.println("  Focus: " + state.getFocus());
        }
        if (state.getPlayeForRead().getVulnerable() > 0) {
            out.println("  Vulnerable: " + state.getPlayeForRead().getVulnerable());
        }
        if (state.getPlayeForRead().getWeak() > 0) {
            out.println("  Weak: " + state.getPlayeForRead().getWeak());
        }
        if (state.getPlayeForRead().getFrail() > 0) {
            out.println("  Frail: " + state.getPlayeForRead().getFrail());
        }
        if (state.buffs != 0) {
            out.println("  Buffs:");
            for (PlayerBuff buff : PlayerBuff.BUFFS) {
                if ((state.buffs & buff.mask()) != 0) {
                    out.println("    - " + buff.name());
                }
            }
        }
        if (state.prop.counterHandlersNonNull.length > 0) {
            out.println("  Other:");
            for (int i = 0; i < state.prop.counterHandlers.length; i++) {
                if (state.prop.counterHandlers[i] != null && state.getCounterForRead()[i] != 0) {
                    String counterStr = state.prop.counterHandlers[i].getDisplayString(state);
                    out.println("    - " + state.prop.counterNames[i] + "=" + (counterStr != null ? counterStr : state.getCounterForRead()[i]));
                }
            }
            if (state.getPlayeForRead().isEntangled()) {
                out.println("  - Entangled");
            }
            if (state.getPlayeForRead().cannotDrawCard()) {
                out.println("  - Cannot Draw Card");
            }
            for (int i = 0; i < state.prop.potions.size(); i++) {
                if (state.potionUsable(i)) {
                    out.println("  - " + state.prop.potions.get(i) + ": " + state.potionPenalty(i));
                }
            }
        }
        out.println();
        if (state.getDrawOrderForRead().size() > 0) {
            out.print("Draw Order: [");
            for (int i = state.getDrawOrderForRead().size() - 1; i >= 0; i--) {
                if (i != state.getDrawOrderForRead().size() - 1) {
                    out.print(", ");
                }
                out.print(state.prop.cardDict[state.getDrawOrderForRead().ithCardFromTop(i)].cardName);
            }
            out.println("]");
        }
        out.println("Hand (" + state.handArrLen + ")");
        var hand = GameStateUtils.getCardArrCounts(state.getHandArrForRead(), state.getNumCardsInHand(), state.prop.cardDict.length);
        for (int i = 0; i < hand.length; i++) {
            if (hand[i] > 0 && (!state.prop.discard0CardOrderMatters || state.prop.cardDict[i].realEnergyCost() != 0)) {
                out.println("  " + hand[i] + " " + state.prop.cardDict[i].cardName);
            }
        }
        if (state.prop.discard0CardOrderMatters) {
            for (int i = 0; i < state.handArrLen; i++) {
                if (state.prop.cardDict[state.handArr[i]].realEnergyCost() == 0) {
                    out.println("  " + state.prop.cardDict[state.handArr[i]].cardName);
                }
            }
        }
        if (state.chosenCardsArrLen > 0) {
            out.println("Chosen");
            for (int i = 0; i < state.chosenCardsArrLen; i++) {
                out.println("  " + state.prop.cardDict[state.chosenCardsArr[i]].cardName);
            }
        }
        if (state.nightmareCards != null && state.nightmareCardsLen > 0) {
            out.print("Nightmare: ");
            for (int i = 0; i < state.nightmareCardsLen; i++) {
                out.print((i == 0 ? "" : ", ") + state.prop.cardDict[state.nightmareCards[i]].cardName);
            }
            out.println();
        }
        if (state.stateDesc != null) {
            out.println("From Previous Action: " + state.stateDesc);
        }
    }

    private void printAction(GameState state) {
        for (int i = 0; i < state.getLegalActions().length; i++) {
            if (state.getAction(i).type() == GameActionType.PLAY_CARD ||
                    state.getAction(i).type() == GameActionType.SELECT_ENEMY ||
                    state.getAction(i).type() == GameActionType.SELECT_CARD_HAND ||
                    state.getAction(i).type() == GameActionType.SELECT_CARD_DISCARD ||
                    state.getAction(i).type() == GameActionType.SELECT_CARD_EXHAUST ||
                    state.getAction(i).type() == GameActionType.SELECT_CARD_DECK ||
                    state.getAction(i).type() == GameActionType.BEGIN_TURN ||
                    state.getAction(i).type() == GameActionType.USE_POTION ||
                    state.getAction(i).type() == GameActionType.SELECT_SCENARIO ||
                    state.getAction(i).type() == GameActionType.SELECT_CARD_1_OUT_OF_3 ||
                    state.getAction(i).type() == GameActionType.BEGIN_BATTLE ||
                    state.getAction(i).type() == GameActionType.BEGIN_PRE_BATTLE ||
                    state.getAction(i).type() == GameActionType.END_SELECT_CARD_HAND) {
                out.println(i + ". " + state.getActionString(i));
            } else if (state.getAction(i).type() == GameActionType.END_TURN) {
                out.println("e. End Turn");
            } else {
                out.println(state.getAction(i));
                throw new RuntimeException();
            }
        }
        out.println("a. Show Deck (" + state.getNumCardsInDeck() + ")");
        out.println("s. Show Discard (" + state.getNumCardsInDiscard() + ")");
        if (state.getNumCardsInExhaust() > 0) {
            out.println("x. Show Exhaust (" + state.getNumCardsInExhaust() + ")");
        }
    }

    private void setDrawOrder(BufferedReader reader, GameState state, List<String> history) throws IOException {
        for (int i = 0; i < state.prop.cardDict.length; i++) {
            out.println(i + ". " + state.prop.cardDict[i].cardName);
        }
        var drawOrder = new ArrayList<Integer>();
        for (int i = 0; i < state.getDrawOrderForRead().size(); i++) {
            drawOrder.add(state.getDrawOrderForRead().ithCardFromTop(i));
        }
        while (true) {
            out.print("[");
            for (int i = 0; i < drawOrder.size(); i++) {
                out.print(state.prop.cardDict[drawOrder.get(i)].cardName + (i == drawOrder.size() - 1 ? "" : ", "));
            }
            out.println("]");

            out.print("> ");
            String line = reader.readLine();
            history.add(line);
            if (line.equals("b")) { // back
                return;
            } else if (line.equals("e")) {
                state.getDrawOrderForWrite().clear();
                for (int i = drawOrder.size() - 1; i >= 0; i--) {
                    state.getDrawOrderForWrite().pushOnTop(drawOrder.get(i));
                }
                out.print("[");
                for (int i = 0; i < drawOrder.size(); i++) {
                    var idx = state.getDrawOrderForRead().ithCardFromTop(i);
                    out.print(state.prop.cardDict[idx].cardName + (i == drawOrder.size() - 1 ? "" : ", "));
                }
                out.println("]");
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
                    var card = FuzzyMatch.getBestFuzzyMatch(line.toLowerCase(), cards);
                    if (card != null) {
                        drawOrder.add(cards.indexOf(card));
                        continue;
                    }
                }
                out.println("Unknown Command.");
            }
        }
    }

    private int selectEnemy(BufferedReader reader, GameState state, List<String> history, boolean onlyAlive) throws IOException {
        int idx = 0;
        int[] idxes = new int[onlyAlive ? state.enemiesAlive : state.getEnemiesForRead().size()];
        for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
            if (!onlyAlive || state.getEnemiesForRead().get(i).isAlive()) {
                String a = !onlyAlive ? " (hp=" + state.getEnemiesForRead().get(i).getHealth() + ")" : "";
                out.println(idx + ". " + state.getEnemiesForRead().get(i).getName() + " (" + idx + ")" + a);
                idxes[idx++] = i;
            }
        }
        if (idxes.length == 1) {
            return idxes[0];
        }
        while (true) {
            out.print("> ");
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
                var enemy = FuzzyMatch.getBestFuzzyMatch(line.toLowerCase(), enemies);
                if (enemy != null) {
                    out.println("Fuzzy Match: " + enemiesOrig.get(enemies.indexOf(enemy)));
                    return idxes[enemies.indexOf(enemy)];
                }
            }
            out.println("Unknown Command.");
        }
    }

    private void setEnemyMove(BufferedReader reader, GameState state, List<String> history) throws IOException {
        int curEnemyIdx = selectEnemy(reader, state, history,true);
        if (curEnemyIdx < 0) {
            return;
        }
        EnemyReadOnly curEnemy = state.getEnemiesForRead().get(curEnemyIdx);

        for (int i = 0; i < curEnemy.property.numOfMoves; i++) {
            out.println(i + ". " + curEnemy.getMoveString(state, i));
        }
        while (true) {
            out.print("> ");
            String line = reader.readLine();
            history.add(line);
            if (line.equals("b")) {
                return;
            }
            int moveIdx = parseInt(line, -1);
            if (moveIdx >= 0 && moveIdx < curEnemy.property.numOfMoves) {
                state.getEnemiesForWrite().getForWrite(curEnemyIdx).setMove(moveIdx);
                return;
            } else {
                var movesOrig = IntStream.range(0, curEnemy.property.numOfMoves).mapToObj((i) -> curEnemy.getMoveString(state, i)).toList();
                var moves = movesOrig.stream().map((x) -> x.toLowerCase(Locale.ROOT)).toList();
                var move = FuzzyMatch.getBestFuzzyMatch(line.toLowerCase(), moves);
                if (move != null) {
                    out.println("Fuzzy Match: " + movesOrig.get(moves.indexOf(move)));
                    state.getEnemiesForWrite().getForWrite(curEnemyIdx).setMove(moves.indexOf(move));
                    return;
                }
            }
            out.println("Unknown Command.");
        }
    }

    private void setEnemyOther(BufferedReader reader, GameState state, List<String> history) throws IOException {
        int curEnemyIdx = selectEnemy(reader, state, history,false);
        if (curEnemyIdx < 0) {
            return;
        }

        if (state.getEnemiesForRead().get(curEnemyIdx) instanceof Enemy.RedLouse ||
                state.getEnemiesForRead().get(curEnemyIdx) instanceof Enemy.GreenLouse) {
            while (true) {
                out.print("0. Curl-Up");
                out.print("1. Damage");
                String line = reader.readLine();
                history.add(line);
                if (line.equals("b")) {
                    return;
                }
                int r = parseInt(line, -1);
                if (r == 0) {
                    out.println("Curl-Up Amount: ");
                    line = reader.readLine();
                    history.add(line);
                    if (line.equals("b")) {
                        return;
                    }
                    int n = parseInt(line, -1);
                    if (n > 0) {
                        if (state.getEnemiesForRead().get(curEnemyIdx) instanceof Enemy.RedLouse louse) {
                            louse.setCurlUpAmount(n);
                        } else if (state.getEnemiesForRead().get(curEnemyIdx) instanceof Enemy.GreenLouse louse) {
                            louse.setCurlUpAmount(n);
                        }
                        return;
                    }
                } else if (r == 1) {
                    out.println("Damage: ");
                    line = reader.readLine();
                    history.add(line);
                    if (line.equals("b")) {
                        return;
                    }
                    int n = parseInt(line, -1);
                    if (n > 0) {
                        if (state.getEnemiesForRead().get(curEnemyIdx) instanceof Enemy.RedLouse louse) {
                            louse.setD(n);
                        } else if (state.getEnemiesForRead().get(curEnemyIdx) instanceof Enemy.GreenLouse louse) {
                            louse.setD(n);
                        }
                        return;
                    }
                }
            }
        } else if (state.getEnemiesForRead().get(curEnemyIdx) instanceof EnemyBeyond.Darkling darkling) {
            while (true) {
                out.println("Nip Damage: ");
                String line = reader.readLine();
                history.add(line);
                if (line.equals("b")) {
                    return;
                }
                int n = parseInt(line, -1);
                if (n > 0 || n == -1) {
                    darkling.setNipDamage(n);
                    return;
                }
            }
        } else {
            out.println("Nothing to change.");
        }
    }

    private void setEnemyHealth(BufferedReader reader, GameState state, List<String> history) throws IOException {
        int curEnemyIdx = selectEnemy(reader, state, history,false);
        if (curEnemyIdx < 0) {
            return;
        }

        while (true) {
            out.print("Health: ");
            String line = reader.readLine();
            history.add(line);
            if (line.equals("b")) {
                return;
            }
            int hp = parseInt(line, -1);
            if (hp >= 0) {
                if (hp > 0 && !state.getEnemiesForRead().get(curEnemyIdx).isAlive()) {
                    state.reviveEnemy(curEnemyIdx, true, hp);
                } else {
                    if (hp == 0) {
                        state.killEnemy(curEnemyIdx, false);
                    } else {
                        state.getEnemiesForWrite().getForWrite(curEnemyIdx).setHealth(hp);
                    }
                }
                return;
            }
        }
    }

    private void setEnemyHealthOriginal(BufferedReader reader, GameState state, List<String> history) throws IOException {
        int curEnemyIdx = selectEnemy(reader, state, history,false);
        if (curEnemyIdx < 0) {
            return;
        }

        while (true) {
            out.print("Health: ");
            String line = reader.readLine();
            history.add(line);
            if (line.equals("b")) {
                return;
            }
            int hpOrig = parseInt(line, -1);
            if (hpOrig >= 0) {
                int oldHp = state.getEnemiesForWrite().getForWrite(curEnemyIdx).getHealth();
                int oldHpOrig = state.getEnemiesForWrite().getForWrite(curEnemyIdx).property.origHealth;
                state.getEnemiesForWrite().getForWrite(curEnemyIdx).property.origHealth = hpOrig;
                int hp = Math.max(0, Math.min(state.getEnemiesForWrite().getForWrite(curEnemyIdx).property.maxHealth, oldHp + (hpOrig - oldHpOrig)));
                if (hp > 0 && !state.getEnemiesForRead().get(curEnemyIdx).isAlive()) {
                    state.reviveEnemy(curEnemyIdx, true, hp);
                } else {
                    if (hp == 0) {
                        state.killEnemy(curEnemyIdx, false);
                    } else {
                        if (state.getEnemiesForWrite().getForWrite(curEnemyIdx) instanceof Enemy.LargeSpikeSlime s) {
                            s.setSplitMaxHealth(hpOrig);
                        }
                        state.getEnemiesForWrite().getForWrite(curEnemyIdx).setHealth(hp);
                    }
                }
                return;
            }
        }
    }

    private void setPotionUtility(GameState state, String line) {
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
            out.println("Invalid Command.");
            return;
        }
        if (util == 0) {
            out.println("Set " + state.prop.potions.get(potionIdx) + " to unusable.");
            state.getPotionsStateForWrite()[potionIdx * 3] = 0;
            state.getPotionsStateForWrite()[potionIdx * 3 + 1] = 100;
            state.getPotionsStateForWrite()[potionIdx * 3 + 2] = 0;
        } else {
            out.println("Set " + state.prop.potions.get(potionIdx) + " utility to " + util + ".");
            state.getPotionsStateForWrite()[potionIdx * 3] = 1;
            state.getPotionsStateForWrite()[potionIdx * 3 + 1] = (short) util;
            state.getPotionsStateForWrite()[potionIdx * 3 + 2] = 1;
        }
    }

    private void addCardToHandSelectScreen(BufferedReader reader, GameState state, List<String> history) throws IOException {
        for (int i = 0; i < state.prop.cardDict.length; i++) {
            out.println(i + ". " + state.prop.cardDict[i].cardName);
        }
        while (true) {
            out.print("> ");
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
            out.println("Unknown Command");
        }
    }

    private void removeCardFromHandSelectScreen(BufferedReader reader, GameState state, List<String> history) throws IOException {
        for (int i = 0; i < state.handArrLen; i++) {
            out.println(i + ". " + state.prop.cardDict[state.getHandArrForRead()[i]].cardName);
        }
        while (true) {
            out.print("> ");
            String line = reader.readLine();
            history.add(line);
            if (line.equals("b")) {
                return;
            }
            int idx = parseInt(line, -1);
            if (idx >= 0 && idx < state.handArrLen) {
                state.removeCardFromHandByPosition(idx);
                return;
            }
            out.println("Unknown Command");
        }
    }

    int selectCardFromHand(BufferedReader reader, GameState state, List<String> history) throws IOException {
        for (int i = 0; i < state.handArrLen; i++) {
            out.println(i + ". " + state.prop.cardDict[state.getHandArrForRead()[i]].cardName);
        }
        while (true) {
            out.print("> ");
            String line = reader.readLine();
            history.add(line);
            int r = parseInt(line, -1);
            if (r >= 0 && r < state.handArrLen) {
                return r;
            }
            out.println("Unknown Command");
        }
    }

    int selectCardForWarpedTongs(BufferedReader reader, GameState state, List<String> history) throws IOException {
        int nonUpgradedCardCount = 0;
        for (int i = 0; i < state.handArrLen; i++) {
            if (state.prop.upgradeIdxes[state.getHandArrForRead()[i]] >= 0) {
                out.println(nonUpgradedCardCount + ". " + state.prop.cardDict[state.getHandArrForRead()[i]].cardName);
                nonUpgradedCardCount++;
            }
        }
        while (true) {
            out.print("> ");
            String line = reader.readLine();
            history.add(line);
            int r = parseInt(line, -1);
            if (r >= 0 && r < nonUpgradedCardCount) {
                return r;
            }
            out.println("Unknown Command");
        }
    }

    int selectCardForMummifiedHand(BufferedReader reader, GameState state, List<String> history) throws IOException {
        int cardCount = 0;
        for (int i = 0; i < state.handArrLen; i++) {
            if (!state.prop.cardDict[state.getHandArrForRead()[i]].isXCost && state.prop.cardDict[state.getHandArrForRead()[i]].energyCost > 0) {
                out.println(cardCount + ". " + state.prop.cardDict[state.getHandArrForRead()[i]].cardName);
                cardCount++;
            }
        }
        while (true) {
            out.print("> ");
            String line = reader.readLine();
            history.add(line);
            int r = parseInt(line, -1);
            if (r >= 0 && r < cardCount) {
                return r;
            }
            out.println("Unknown Command");
        }
    }

    int selectScenarioForRandomization(BufferedReader reader, GameStateRandomization randomization, List<String> history) throws IOException {
        var info = randomization.listRandomizations();
        if (info.size() == 1) {
            return 0;
        }
        for (var entry : info.entrySet()) {
            out.println(entry.getKey() + ". " + entry.getValue().desc());
        }
        while (true) {
            out.print("> ");
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
            out.println("Unknown Command");
        }
    }

    int selectGremlinForGremlinLeaderEncounter(BufferedReader reader, GameStateRandomization randomization, List<String> history) throws IOException {
        out.println("0. Mad Gremlin");
        out.println("1. Sneaky Gremlin");
        out.println("2. Fat Gremlin");
        out.println("3. Shield Gremlin");
        out.println("4. Gremlin Wizard");
        while (true) {
            out.print("> ");
            String line = reader.readLine();
            history.add(line);
            int r = parseInt(line, -1);
            if (r >= 0 && r < 5) {
                if (r <= 3) {
                    return r * 2;
                }
                return 7;
            }
            out.println("Unknown Command");
        }
    }

    int selectCardsForSkillPotion(BufferedReader reader, Tuple<GameState, Integer> arg, List<String> history) throws IOException {
        var state = arg.v1();
        int currentIdx1 = arg.v2() & 255;
        int currentIdx2 = (arg.v2() >> 8) & 255;
        currentIdx2 = currentIdx2 >= currentIdx1 ? currentIdx2 + 1 : currentIdx2;
        int currentPick = currentIdx1 == 255 ? 0 : currentIdx2 == 255 ? 1 : 2;
        int p = 0;
        for (int i = 0; i < state.prop.skillPotionIdxes.length; i++) {
            if (i == currentIdx1 || i == currentIdx2) {
                continue;
            }
            var card = state.prop.cardDict[state.prop.select1OutOf3CardsIdxes[state.prop.skillPotionIdxes[i]]];
            out.println(p + ". " + card.cardName);
            p++;
        }
        while (true) {
            out.print("> ");
            String line = reader.readLine();
            history.add(line);
            int r = parseInt(line, -1);
            if (0 <= r && r < state.prop.skillPotionIdxes.length - currentPick) {
                return r;
            }
            out.println("Unknown Command");
        }
    }

    int selectCostForSnecko(BufferedReader reader, Tuple<GameState, Integer> arg, List<String> history) throws IOException {
        var snecko = arg.v1().prop.sneckoIdxes[arg.v2()];
        for (int i = 1; i < snecko[0] + 1; i++) {
            out.println((i - 1) + ". " + arg.v1().prop.cardDict[snecko[i]].cardName);
        }
        while (true) {
            out.print("> ");
            String line = reader.readLine();
            history.add(line);
            int r = parseInt(line, -1);
            if (r >= 0 && r < snecko[0]) {
                return r;
            }
            out.println("Unknown Command");
        }
    }

    int selectEnemeyRandomInteractive(BufferedReader reader, GameState state, List<String> history, RandomGenCtx ctx) throws IOException {
        out.println("Select enemy for " + ctx);
        int idx = 0;
        for (int i = 0; i < state.getEnemiesForRead().size(); i ++) {
            if (state.getEnemiesForRead().get(i).isAlive()) {
                out.println(idx + ". " + state.getEnemiesForRead().get(i).getName() + "(" + i + ")");
                idx++;
            }
        }
        while (true) {
            out.print("> ");
            String line = reader.readLine();
            history.add(line);
            int r = parseInt(line, -1);
            if (r >= 0 && r < state.enemiesAlive) {
                return r;
            }
            out.println("Unknown Command");
        }
    }

    private void runGames(String modelDir, GameState state, String line) {
        String[] s = line.split(" ");
        int numberOfGames = 100;
        int numberOfThreads = 2;
        int nodeCount = 500;
        int startingAction = -1;
        int randomizationScenario = -1;
        boolean printDamageDistribution = false;
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
                if (s[i].equals("dmg")) {
                    printDamageDistribution = true;
                }
                if (s[i].startsWith("a=")) {
                    if (s[i].substring(2).equals("e")) {
                        startingAction = state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length - 1;
                    } else {
                        startingAction = parseInt(s[i].substring(2), -1);
                    }
                    if (startingAction < 0 || startingAction >= state.getLegalActions().length) {
                        out.println("Unknown action.");
                        numberOfGames = 0;
                    }
                }
            }
        }
        MatchSession session = new MatchSession(numberOfThreads, modelDir);
        session.startingAction = startingAction;
        session.setMatchLogFile("matches_interactive.txt.gz");
        var prevRandomization = state.prop.randomization;
        if (randomizationScenario >= 0) {
            state.prop.randomization = state.prop.randomization.fixR(randomizationScenario);
        }
        try {
            session.playGames(state, numberOfGames, nodeCount, true, printDamageDistribution, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        state.prop.randomization = prevRandomization;
    }

    private static GameState state1;
    private static GameState state2;
    private static int startingAction1;
    private static int startingAction2;

    private void runGamesCmpSetup(GameState state, String line) {
        String[] s = line.split(" ");
        if (s.length <= 1) {
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
                out.println("Unknown action.");
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
            out.println("cmpSet <1|2>");
        }
    }

    private void runGamesCmp(BufferedReader reader, String modelDir, String line) throws IOException {
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
        if (state1 == null || state2 == null) {
            out.println("States not set");
            return;
        }
        var prevRandomization = state1.prop.randomization;
        if (randomizationScenario >= 0) {
            state1.prop.randomization = state1.prop.randomization.fixR(randomizationScenario);
        }
        out.println(state1);
        if (startingAction1 >= 0) {
            out.println("    " + state1.getActionString(startingAction1));
        }
        out.println(state2);
        if (startingAction2 >= 0) {
            out.println("    " + state2.getActionString(startingAction2));
        }
        out.println("Continue? (y/n)");
        out.print("> ");
        if (!reader.readLine().equals("y")) {
            return;
        }
        session.playGames(state1, numberOfGames, nodeCount, true, false, false);
        state1.prop.randomization = prevRandomization;
    }

    private void exploreTree(GameState root, BufferedReader reader, String modelDir) throws IOException {
        boolean printAction = true;
        boolean printState = true;
        var saves = new ArrayList<ArrayList<State>>();
        for (int i = 0; i < 10; i++) {
            saves.add(null);
        }
        var hist = new ArrayList<State>();
        hist.add(root);
        while (true) {
            var s = hist.get(hist.size() - 1);
            if (s instanceof GameState state) {
                if (printState) {
                    if (state.getStateDesc().length() > 0) {
                        out.println(state.getStateDesc());
                    }
                    out.println(state);
                }
                if (printAction) {
                    for (int i = 0; i < state.getLegalActions().length; i++) {
                        if (state.ns == null || state.ns[i] == null) {
                            continue;
                        }
                        if (state.getAction(i).type() == GameActionType.PLAY_CARD ||
                                state.getAction(i).type() == GameActionType.SELECT_ENEMY ||
                                state.getAction(i).type() == GameActionType.SELECT_CARD_HAND ||
                                state.getAction(i).type() == GameActionType.SELECT_CARD_DISCARD ||
                                state.getAction(i).type() == GameActionType.SELECT_CARD_EXHAUST ||
                                state.getAction(i).type() == GameActionType.SELECT_CARD_DECK ||
                                state.getAction(i).type() == GameActionType.BEGIN_TURN ||
                                state.getAction(i).type() == GameActionType.USE_POTION ||
                                state.getAction(i).type() == GameActionType.SELECT_SCENARIO ||
                                state.getAction(i).type() == GameActionType.SELECT_CARD_1_OUT_OF_3 ||
                                state.getAction(i).type() == GameActionType.BEGIN_BATTLE ||
                                state.getAction(i).type() == GameActionType.BEGIN_PRE_BATTLE) {
                            out.println(i + ". " + state.getActionString(i));
                        } else if (state.getAction(i).type() == GameActionType.END_TURN) {
                            out.println("e. End Turn");
                        } else {
                            out.println(state.getAction(i));
                            throw new RuntimeException();
                        }
                    }
                    printState = false;
                    printAction = false;
                }
                out.print("> ");
                String line = reader.readLine();
                if (line.equals("exit")) {
                    return;
                }
                if (line.equals("tree") || line.startsWith("tree ")) {
                    printTree(state, line, modelDir);
                } else if (line.equals("b")) {
                    if (hist.size() > 1) {
                        hist.remove(hist.size() - 1);
                        printState = true;
                        printAction = true;
                    }
                } else if (line.startsWith("save ")) {
                    int idx = parseInt(line.substring(5), -1);
                    if (idx >= 0 && idx <= 9) {
                        saves.set(idx, new ArrayList<>(hist));
                    }
                } else if (line.startsWith("restore ")) {
                    int idx = parseInt(line.substring(8), -1);
                    if (idx >= 0 && idx <= 9 && saves.get(idx) != null) {
                        hist = new ArrayList<>(saves.get(idx));
                        printState = true;
                        printAction = true;
                    }
                } else {
                    int action = parseInt(line, -1);
                    if (action < 0 || action >= state.getLegalActions().length) {
                        var _state = state;
                        var actionsOrig = IntStream.range(0, state.getLegalActions().length).mapToObj(_state::getActionString).toList();
                        var actions = actionsOrig.stream().map(String::toLowerCase).toList();
                        var actionStr = FuzzyMatch.getBestFuzzyMatch(line.toLowerCase(), actions);
                        if (actionStr != null) {
                            out.println("Fuzzy Match: " + actionsOrig.get(actions.indexOf(actionStr)));
                            action = actions.indexOf(actionStr);
                        }
                    }
                    if (action >= 0 && action <= state.getLegalActions().length && state.ns[action] != null) {
                        printState = true;
                        printAction = true;
                        hist.add(state.ns[action]);
                    } else {
                        out.println("Unknown Command.");
                    }
                }
            } else if (s instanceof ChanceState cs) {
                var chanceOutcomes = cs.cache.values().stream().sorted((a, b) -> {
                    var aStr = a.state.getStateDescStr();
                    aStr = aStr.length() == 0 ? a.state.toString() : aStr;
                    var bStr = b.state.getStateDescStr();
                    bStr = bStr.length() == 0 ? b.state.toString() : bStr;
                    return aStr.compareTo(bStr);
                }).toList();
                if (printState) {
                    for (int i = 0; i < chanceOutcomes.size(); i++) {
                        var str = chanceOutcomes.get(i).state.getStateDesc();
                        out.println(i + ". " + (str.length() == 0 ? chanceOutcomes.get(i).state : str) + " (" + chanceOutcomes.get(i).n + "/" + chanceOutcomes.get(i).state.total_n + ")");
                    }
                    printAction = false;
                }
                out.print("> ");
                String line = reader.readLine();
                if (line.equals("exit")) {
                    return;
                }
                if (line.equals("")) {

                } else {
                    int outcome = parseInt(line, -1);
                    if (outcome < 0 || outcome >= chanceOutcomes.size()) {
                        var outcomesOrig = chanceOutcomes.stream().map((x) -> x.state.getStateDesc().length() == 0 ? x.state.toString() : x.state.getStateDescStr()).toList();
                        var outcomes = outcomesOrig.stream().map(String::toLowerCase).toList();
                        var outcomeStr = FuzzyMatch.getBestFuzzyMatch(line.toLowerCase(), outcomes);
                        if (outcomeStr != null) {
                            out.println("Fuzzy Match: " + outcomesOrig.get(outcomes.indexOf(outcomeStr)));
                            outcome = outcomes.indexOf(outcomeStr);
                        }
                    }
                    if (outcome >= 0 && outcome <= chanceOutcomes.size()) {
                        printState = true;
                        printAction = true;
                        hist.add(chanceOutcomes.get(outcome).state);
                    } else {
                        out.println("Unknown Command.");
                    }
                }
            }
        }
    }

    private void printTree(GameState state, String line, String modelDir) {
        String[] s = line.split(" ");
        int depth = 3;
        int action = -1;
        boolean writeToFile = false;
        boolean dag = false;
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
                if (s[i].equals("dag")) {
                    dag = true;
                }
            }
        }
        Writer writer;
        try {
            if (dag) {
                writer = new BufferedWriter(new FileWriter(modelDir + "/dag.dot"));
                if (action >= 0 && action < state.ns.length && state.ns[action] != null) {
                    GameStateUtils.printDagGraphviz(state.ns[action], null, depth);
                    GameStateUtils.printDagGraphviz(state.ns[action], writer, depth);
                } else {
                    GameStateUtils.printDagGraphviz(state, null, depth);
                    GameStateUtils.printDagGraphviz(state, writer, depth);
                }
            } else {
                writer = writeToFile ? new BufferedWriter(new FileWriter(modelDir + "/tree.txt")) : new OutputStreamWriter(out);
                if (action >= 0 && action < state.ns.length && state.ns[action] != null) {
                    GameStateUtils.printTree2(state.ns[action], writer, depth);
                } else {
                    GameStateUtils.printTree2(state, writer, depth);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void runMCTS(GameState state, MCTS mcts, String line) {
        List<String> args = Arrays.asList(line.split(" "));
        if (args.size() < 2) {
            out.println("<node count>");
        }
        int count = Integer.parseInt(args.get(1));
        int numberOfThreads = parseArgsInt(args, "t", 1);
        mcts.forceRootAction = parseArgsInt(args, "a", -1);
        long start = System.currentTimeMillis();
        if (numberOfThreads <= 1) {
            for (int i = state.total_n; i < count; i++) {
                mcts.search(state, false, count - i);
            }
            mcts.forceRootAction = -1;
        } else {
            state.setMultithreaded(true);
            AtomicLong atomicLong = new AtomicLong(state.total_n);
            List<Thread> workerThreads = new ArrayList<>();
            allocateThreadMCTS(numberOfThreads);
            for (int i = 0; i < numberOfThreads; i++) {
                threadMCTS.get(i).forceRootAction = mcts.forceRootAction;
                final int tidx = i;
                workerThreads.add(new Thread(() -> {
                    long c = atomicLong.addAndGet(1);
                    while (c <= count) {
                        threadMCTS.get(tidx).search(state, false, (int) (count - c + 1));
                        c = atomicLong.addAndGet(1);
                    }
                }));
                workerThreads.get(i).start();
            }
            for (int i = 0; i < numberOfThreads; i++) {
                try {
                    workerThreads.get(i).join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                threadMCTS.get(i).forceRootAction = -1;
            }
            state.setMultithreaded(false);
        }
        System.out.println("Time: " + (System.currentTimeMillis() - start) + " ms");
        out.println(state);
        System.gc();
        System.gc();
        System.gc();
        out.println("Memory Usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + " bytes");
    }

    private Tuple<GameState, Integer> runNNPV(GameState state, MCTS mcts, List<String> pv, String line, boolean printPV) {
        List<String> args = Arrays.asList(line.split(" "));
        if (args.size() < 2) {
            out.println("<node count>");
        }
        int count = Integer.parseInt(args.get(1));
        boolean clear = args.size() > 2 && args.get(2).equals("clear");
        int numberOfThreads = parseArgsInt(args, "t", 1);
        GameState s = state;
        int move_i = 0;
        long start = System.currentTimeMillis();
        do {
            if (numberOfThreads <= 1) {
                for (int i = s.total_n; i < count; i++) {
                    mcts.search(s, false, count - i);
                }
            } else {
                s.setMultithreaded(true);
                AtomicLong atomicLong = new AtomicLong(s.total_n);
                List<Thread> workerThreads = new ArrayList<>();
                allocateThreadMCTS(numberOfThreads);
                for (int i = 0; i < numberOfThreads; i++) {
                    threadMCTS.get(i).forceRootAction = mcts.forceRootAction;
                    final int tidx = i;
                    var _s = s;
                    workerThreads.add(new Thread(() -> {
                        long c = atomicLong.addAndGet(1);
                        while (c <= count) {
                            threadMCTS.get(tidx).search(_s, false, (int) (count - c + 1));
                            c = atomicLong.addAndGet(1);
                        }
                    }));
                    workerThreads.get(i).start();
                }
                for (int i = 0; i < numberOfThreads; i++) {
                    try {
                        workerThreads.get(i).join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                state.setMultithreaded(false);
            }
            int action = MCTS.getActionWithMaxNodesOrTerminal(s, null);
            if (action < 0) {
                return null;
            }
            int max_n = s.n[action];
            if (printPV) {
                int baseIdx = (action + 1) * s.prop.v_total_len;
                out.println("  " + (++move_i) + ". " + s.getActionString(action) +
                        ": n=" + max_n + ", q=" + formatFloat(s.q[baseIdx + GameState.V_COMB_IDX] / max_n) + ", q_win=" + formatFloat(s.q[baseIdx + GameState.V_WIN_IDX] / max_n) + ", q_health=" + formatFloat(s.q[baseIdx + GameState.V_HEALTH_IDX] / max_n) + " (" + formatFloat(s.q[baseIdx + GameState.V_HEALTH_IDX] / max_n * s.getPlayeForRead().getMaxHealth()) + ")");
            }
            pv.add(s.getActionString(action));
            State ns = s.ns[action];
            if (ns instanceof ChanceState) {
                System.out.println("Time: " + (System.currentTimeMillis() - start) + " ms");
                return new Tuple<>(s, action);
            } else if (ns instanceof GameState ns2) {
                if (ns2.isTerminal() != 0) {
                    System.out.println("Time: " + (System.currentTimeMillis() - start) + " ms");
                    return new Tuple<>(s, action);
                } else {
                    if (clear) {
                        s.clearAllSearchInfo();
                    }
                    s = ns2;
                    if (clear) {
                        ns2.clearAllSearchInfo();
                    }
                }
            } else {
                out.println("Unknown ns: " + state);
                out.println("Unknown ns: " + Arrays.stream(state.ns).map(Objects::isNull).toList());
                return null;
            }
        } while (true);
    }

    private void runNNPVChance(BufferedReader reader, GameState state, MCTS mcts, String line) throws IOException {
        List<String> args = Arrays.asList(line.split(" "));
        int nodeCount = parseArgsInt(args, "n", 100);
        int numberOfThreads = parseArgsInt(args, "t", 1);
        int chanceAction = parseArgsInt(args, "a", -1);
        if (chanceAction < 0) {
            out.println("Unknown action.");
            return;
        }

        GameState s = state.clone(false);
        ChanceState cs = new ChanceState(null, s, chanceAction);
        s.prop.makingRealMove = true;
        for (int i = 0; i < 1000000; i++) {
            cs.getNextState(false, -1);
        }
        s.prop.makingRealMove = false;
        out.println("Number of Outcomes: " + cs.cache.size());
        out.println("Continue? (y/n)");
        out.print("> ");
        if (!reader.readLine().equals("y")) {
            return;
        }

        AtomicLong start = new AtomicLong(System.currentTimeMillis());
        AtomicLong doneCount = new AtomicLong(0);
        ConcurrentHashMap<List<String>, List<GameState>> pvs = new ConcurrentHashMap<>();
        ConcurrentLinkedQueue<GameState> stateQueue = new ConcurrentLinkedQueue<>();
        for (Map.Entry<GameState, ChanceState.Node> entry : cs.cache.entrySet()) {
            GameState cState = entry.getKey();
            cState.clearAllSearchInfo();
            stateQueue.offer(cState);
        }
        List<Thread> workerThreads = new ArrayList<>();
        allocateThreadMCTS(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            final int tidx = i;
            workerThreads.add(new Thread(() -> {
                while (true) {
                    GameState cState = stateQueue.poll();
                    if (cState == null) {
                        break;
                    }
                    List<String> pv = new ArrayList<>();
                    runNNPV(cState, threadMCTS.get(tidx), pv, "nn " + nodeCount, false);
                    synchronized (pvs) {
                        pvs.computeIfAbsent(pv, (_k) -> new ArrayList<>());
                        pvs.get(pv).add(cState);
                        cState.clearAllSearchInfo();
                    }
                    doneCount.incrementAndGet();
                    synchronized (start) {
                        if (System.currentTimeMillis() - start.get() > 3000) {
                            start.set(System.currentTimeMillis());
                            out.println((doneCount.get()) + "/" + cs.cache.size() + " Done");
                        }
                    }
                }
            }));
            workerThreads.get(i).start();
        }
        for (Thread workerThread : workerThreads) {
            try {
                workerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        out.println(doneCount.get() + "/" + cs.cache.size() + " Done");

        var sortedPvs = pvs.entrySet().stream().map(pv -> {
            var totalN = 0;
            for (GameState gameState : pv.getValue()) {
                totalN += cs.cache.get(gameState).n;
            }
            return new Tuple<>(pv, totalN);
        }).sorted((o1, o2) -> Integer.compare(o2.v2(), o1.v2())).collect(Collectors.toList());
        sortedPvs.forEach(pvTuple -> {
            out.println("******************** " + formatFloat(pvTuple.v2() / 10000.0) + "%");
            var pv = pvTuple.v1().getKey();
            var states = pvTuple.v1().getValue();
            var sortedStates = states.stream().sorted((o1, o2) -> Long.compare(cs.cache.get(o2).n, cs.cache.get(o1).n)).collect(Collectors.toList());
            for (GameState gameState : sortedStates) {
                out.println("    " + gameState.stateDesc + " (" + formatFloat(cs.cache.get(gameState).n / 10000.0) + "%)");
            }
            for (int i = 0; i < pv.size(); i++) {
                out.println("      " + (i + 1) + ". " + pv.get(i));
            }
        });
    }

    private void runNNPVVolatility(GameState state, String line) {
        List<String> args = Arrays.asList(line.split(" "));
        int trialCount = parseArgsInt(args, "c", 100);
        int nodeCount = parseArgsInt(args, "n", 100);
        int numberOfThreads = parseArgsInt(args, "t", 1);
        boolean clear = parseArgsBoolean(args, "clear");

        var pvs = new HashMap<Tuple<GameState, Integer>, Tuple<List<String>, Integer>>();
        AtomicLong trialsProcessed = new AtomicLong(0);
        AtomicLong start = new AtomicLong(System.currentTimeMillis());
        List<Thread> workerThreads = new ArrayList<>();
        allocateThreadMCTS(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            final int tidx = i;
            workerThreads.add(new Thread(() -> {
                while (true) {
                    if (trialsProcessed.getAndIncrement() >= trialCount) {
                        break;
                    }
                    GameState s = state.clone(false);
                    interactiveSetSeed(s, s.prop.random.nextLong(RandomGenCtx.Other), s.prop.random.nextLong(RandomGenCtx.Other));
                    List<String> pv = new ArrayList<>();
                    var k = runNNPV(s, threadMCTS.get(tidx), pv, "nn " + nodeCount + (clear ? " clear" : ""), false);
                    k.v1().clearAllSearchInfo();
                    if (k.v1().getAction(k.v2()).type() == GameActionType.END_TURN) {
                        var t = k.v1().clone(false);
                        t.doAction(k.v2());
                        if (!t.isStochastic) {
                            k = new Tuple<>(t, 0);
                        }
                    }
                    synchronized (pvs) {
                        if (pvs.containsKey(k)) {
                            pvs.put(k, new Tuple<>(pv, pvs.get(k).v2() + 1));
                        } else {
                            pvs.put(k, new Tuple<>(pv, 1));
                        }
                    }
                    synchronized (start) {
                        if (System.currentTimeMillis() - start.get() > 3000) {
                            start.set(System.currentTimeMillis());
                            out.println((trialsProcessed.get()) + "/" + trialCount + " Done");
                        }
                    }
                }
            }));
            workerThreads.get(i).start();
        }
        for (Thread thread : workerThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        pvs.forEach((_k, v) -> {
            out.println(v.v2() + "/" + trialCount + " (" + formatFloat (v.v2() / (double) trialCount) + ")");
            for (int i = 0; i < v.v1().size(); i++) {
                out.println("  " + (i + 1) + ". " + v.v1().get(i));
            }
        });
    }

    private int parseArgsInt(List<String> args, String field, int defaultValue) {
        try {
            for (int i = 0; i < args.size(); i++) {
                if (args.get(i).startsWith(field + "=")) {
                    return Integer.parseInt(args.get(i).substring(field.length() + 1));
                }
            }
        } catch (NumberFormatException e) {
        }
        return defaultValue;
    }

    private boolean parseArgsBoolean(List<String> args, String field) {
        try {
            for (int i = 0; i < args.size(); i++) {
                if (args.get(i).equals(field)) {
                    return true;
                }
            }
        } catch (NumberFormatException e) {
        }
        return false;
    }

    private void runNNPV2(GameState state, MCTS mcts, String line) {
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
        }).forEach(out::println);
    }

    private static int parseInt(String s, int default_v) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return default_v;
        }
    }

    private static class InteractiveReader extends BufferedReader {
        private ArrayDeque<String> lines = new ArrayDeque<>();
        private InteractiveMode interactiveMode;

        public InteractiveReader(InteractiveMode interactiveMode, Reader reader) {
            super(reader);
            this.interactiveMode = interactiveMode;
        }

        public String readLine() throws IOException {
            if (lines.size() > 0) {
                interactiveMode.out.println(lines.getFirst());
                return lines.pollFirst();
            }
            return super.readLine();
        }

        public void addCmdsToQueue(List<String> cmds) {
            lines.addAll(cmds.stream().filter(x -> !x.startsWith("#")).toList());
        }
    }

    public static class RandomGenInteractive extends RandomGen.RandomGenPlain {
        public boolean rngOn = true;
        private final BufferedReader reader;
        private final List<String> history;
        private final InteractiveMode interactiveMode;

        public RandomGenInteractive(InteractiveMode interactiveMode, Random random, BufferedReader reader, List<String> history) {
            this.random = random;
            this.reader = reader;
            this.history = history;
            this.interactiveMode = interactiveMode;
        }

        public RandomGenInteractive(InteractiveMode interactiveMode, BufferedReader reader, List<String> history) {
            this.reader = reader;
            this.history = history;
            this.interactiveMode = interactiveMode;
        }

        @SuppressWarnings("unchecked")
        @Override public int nextInt(int bound, RandomGenCtx ctx, Object arg) {
            if (rngOn) {
                return super.nextInt(bound, ctx, arg);
            }
            switch (ctx) {
            case RandomCardHand -> {
                try {
                    return interactiveMode.selectCardFromHand(reader, (GameState) arg, history);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case RandomCardHandWarpedTongs -> {
                try {
                    return interactiveMode.selectCardForWarpedTongs(reader, (GameState) arg, history);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case RandomCardHandMummifiedHand -> {
                try {
                    return interactiveMode.selectCardForMummifiedHand(reader, (GameState) arg, history);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case BeginningOfGameRandomization -> {
                try {
                    return interactiveMode.selectScenarioForRandomization(reader, (GameStateRandomization) arg, history);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case GremlinLeader -> {
                try {
                    return interactiveMode.selectGremlinForGremlinLeaderEncounter(reader, (GameStateRandomization) arg, history);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case SkillPotion -> {
                try {
                    return interactiveMode.selectCardsForSkillPotion(reader, (Tuple<GameState, Integer>) arg, history);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case Snecko -> {
                try {
                    return interactiveMode.selectCostForSnecko(reader, (Tuple<GameState, Integer>) arg, history);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case RandomEnemyGeneral, RandomEnemyJuggernaut, RandomEnemySwordBoomerang, RandomEnemyLightningOrb -> {
                try {
                    return interactiveMode.selectEnemeyRandomInteractive(reader, (GameState) arg, history, ctx);
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
            return new RandomGenInteractive(interactiveMode, getRandomClone(), reader, history);
        }

        public RandomGen createWithSeed(long seed) {
            random = new Random();
            random.setSeed(seed);
            return new RandomGenInteractive(interactiveMode, random, reader, history);
        }

        public void selectEnemyMove(GameState state, Enemy enemy, int enemyIdx) {
            interactiveMode.out.println("Select move for " + enemy.getName() + " (" + enemyIdx + ")");
            for (int i = 0; i < enemy.property.numOfMoves; i++) {
                interactiveMode.out.println(i + ". " + enemy.getMoveString(state, i));
            }
            while (true) {
                interactiveMode.out.print("> ");
                String line;
                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                history.add(line);
                int r = parseInt(line, -1);
                if (0 <= r && r < enemy.property.numOfMoves) {
                    enemy.setMove(r);
                    return;
                }
                interactiveMode.out.println("Unknown Move");
            }
        }

        public int selectBronzeOrbStasis(GameState state, short[] cards, int cardsLen, int rarity, Enemy enemy, int enemyIdx) {
            interactiveMode.out.println("Select card for " + enemy.getName() + " (" + enemyIdx + ")" + " to take");
            int cardCount = 0;
            for (int i = 0; i < cardsLen; i++) {
                if (rarity == state.prop.cardDict[cards[i]].rarity) {
                    interactiveMode.out.println(cardCount + ". " + state.prop.cardDict[cards[i]].cardName);
                    cardCount++;
                }
            }
            while (true) {
                interactiveMode.out.print("> ");
                String line;
                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                history.add(line);
                int r = parseInt(line, -1);
                if (0 <= r && r < cardCount) {
                    return r;
                }
                interactiveMode.out.println("Unknown Move");
            }
        }
    }
}
