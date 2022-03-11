package com.alphaStS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;

import static com.alphaStS.InteractiveMode.interactiveStart;

public class Main {
    public static GameState BasicGremlinNobState() {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.BashP(), 1));
        cards.add(new CardCount(new Card.Strike(), 5));
        cards.add(new CardCount(new Card.Defend(), 4));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.GremlinNob());
        enemies.get(0).health = 85;
        var relics = new ArrayList<Relic>();
        return new GameState(enemies, new Player(75, 75), cards, relics);
    }

    public static GameState BasicGremlinNobState2() {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.Bash(), 1));
        cards.add(new CardCount(new Card.Strike(), 4));
        cards.add(new CardCount(new Card.StrikeP(), 1));
        cards.add(new CardCount(new Card.Defend(), 4));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.GremlinNob());
        enemies.get(0).health = 85;
        var relics = new ArrayList<Relic>();
        return new GameState(enemies, new Player(75, 75), cards, relics);
    }

    public static GameState BasicSentriesState() {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.Bash(), 1));
        cards.add(new CardCount(new Card.Strike(), 4));
        cards.add(new CardCount(new Card.Defend(), 4));
        cards.add(new CardCount(new Card.AscendersBane(), 1));
        cards.add(new CardCount(new Card.SeverSoul(), 1));
        cards.add(new CardCount(new Card.Clash(), 1));
        cards.add(new CardCount(new Card.Headbutt(), 1));
        cards.add(new CardCount(new Card.Anger(), 1));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
        enemies.add(new Enemy.Sentry(45, Enemy.Sentry.BEAM));
        enemies.add(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
        var relics = new ArrayList<Relic>();
        relics.add(new Relic.Orichalcum());
        relics.add(new Relic.BronzeScales());
        relics.add(new Relic.Vajira());
        return new GameState(enemies, new Player(32, 75), cards, relics);
    }

    public static GameState SlimeBossState() {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.Bash(), 1));
        cards.add(new CardCount(new Card.Strike(), 4));
        cards.add(new CardCount(new Card.Defend(), 4));
        cards.add(new CardCount(new Card.AscendersBane(), 1));
        cards.add(new CardCount(new Card.SeverSoul(), 1));
        cards.add(new CardCount(new Card.Clash(), 1));
        cards.add(new CardCount(new Card.Headbutt(), 1));
        cards.add(new CardCount(new Card.Anger(), 1));
        cards.add(new CardCount(new Card.Disarm(), 1));
        cards.add(new CardCount(new Card.ArmanentP(), 1));
        cards.add(new CardCount(new Card.PommelStrike(), 1));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.SlimeBoss());
        enemies.add(new Enemy.LargeSpikeSlime(75, true));
        enemies.add(new Enemy.LargeAcidSlime(75, true));
        enemies.add(new Enemy.MediumSpikeSlime(37, true));
        enemies.add(new Enemy.MediumSpikeSlime(37, true));
        enemies.add(new Enemy.MediumAcidSlime(37, true));
        enemies.add(new Enemy.MediumAcidSlime(37, true));
        var relics = new ArrayList<Relic>();
        relics.add(new Relic.Orichalcum());
        relics.add(new Relic.BronzeScales());
        relics.add(new Relic.Vajira());
        relics.add(new Relic.Anchor());
        return new GameState(enemies, new Player(45, 85), cards, relics);
    }

    public static void main(String[] args) throws IOException {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.Bash(), 1));
        cards.add(new CardCount(new Card.Strike(), 5));
        cards.add(new CardCount(new Card.Defend(), 3));
        cards.add(new CardCount(new Card.DefendP(), 1));
        cards.add(new CardCount(new Card.AscendersBane(), 1));
        cards.add(new CardCount(new Card.Corruption(), 1));
        cards.add(new CardCount(new Card.TwinStrike(), 1));
        cards.add(new CardCount(new Card.UppercutP(), 1));
        cards.add(new CardCount(new Card.ShockwaveP(), 1));
        cards.add(new CardCount(new Card.ShrugItOff(), 1));
        cards.add(new CardCount(new Card.FlameBarrierP(), 1));
        cards.add(new CardCount(new Card.SpotWeakness(), 1));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.SlimeBoss());
        enemies.add(new Enemy.LargeSpikeSlime(75, true));
        enemies.add(new Enemy.LargeAcidSlime(75, true));
        enemies.add(new Enemy.MediumSpikeSlime(37, true));
        enemies.add(new Enemy.MediumSpikeSlime(37, true));
        enemies.add(new Enemy.MediumAcidSlime(37, true));
        enemies.add(new Enemy.MediumAcidSlime(37, true));
        var relics = new ArrayList<Relic>();
        relics.add(new Relic.Anchor());
        var state = new GameState(enemies, new Player(47, 75), cards, relics);
//        state = SlimeBossState();

        if (args.length > 0 && args[0].equals("--get-lengths")) {
            System.out.print(state.getInput().length + "," + state.prop.totalNumOfActions);
            return;
        }

        boolean GEN_TRAINING_MATCHES = false;
        boolean TEST_AGENT_FITNESS = false;
        boolean PLAY_MATCHES = false;
        boolean PLAY_A_GAME = false;
        boolean SLOW_TRAINING_WINDOW = false;
        boolean CURRICULUM_TRAINING_ON = false;
        int MATCHES_COUNT = 5;
        int NODE_COUNT = 1000;
        String SAVES_DIR = "../saves";
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
                MATCHES_COUNT = Integer.parseInt(args[i + 1]);
                i++;
            }
            if (args[i].equals("-n")) {
                NODE_COUNT = Integer.parseInt(args[i + 1]);
                i++;
            }
            if (args[i].equals("-dir")) {
                SAVES_DIR = args[i + 1];
                i++;
            }
            if (args[i].equals("-slow")) {
                SLOW_TRAINING_WINDOW = true;
            }
            if (args[i].equals("-curriculum_training")) {
                CURRICULUM_TRAINING_ON = true;
            }
        }

//        SAVES_DIR = "../tmp/saves4";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(SAVES_DIR + "/training.json"));
        int iteration = root.get("iteration").asInt();
        if (SAVES_DIR.startsWith("../")) {
            MATCHES_COUNT = 300;
            NODE_COUNT = 5000;
            MATCHES_COUNT = 500;
            NODE_COUNT = 100;
            //            SAVES_DIR = "../saves";
            iteration = 31;
        }
        String curIterationDir = SAVES_DIR + "/iteration" + (iteration - 1);

        if (args.length > 0 && (args[0].equals("--i") || args[0].equals("-i"))) {
            interactiveStart(state, curIterationDir);
            return;
        }

        if (PLAY_A_GAME) {
            MatchSession session = new MatchSession(state, curIterationDir);
            session.playGame(NODE_COUNT);
            for (GameStep step : session.states) {
                System.out.println(step.state().toStringReadable());
                if (step.action() >= 0) {
                    System.out.println("action=" + step.state().getActionString(step.action()) + " (" + step.action() + ")");
                }
            }
        }


        MatchSession session = new MatchSession(state, curIterationDir);
        if (TEST_AGENT_FITNESS || PLAY_MATCHES) {
            if (TEST_AGENT_FITNESS) {
                session.setMatchLogFile("training_matches.txt");
            } else if (MATCHES_COUNT <= 100) {
                session.setMatchLogFile("matches.txt");
            }
            long start = System.currentTimeMillis();
            for (int i = 0; i < MATCHES_COUNT; i++) {
                session.playGame(NODE_COUNT);
                if (!TEST_AGENT_FITNESS && session.game_i % 25 == 0) {
                    long end = System.currentTimeMillis();
                    System.out.println("Progress: " + session.game_i + "/" + MATCHES_COUNT);;
                    System.out.println("Deaths: " + session.deathCount);
                    System.out.println("Avg Damage: " + ((double) session.totalDamageTaken) / session.game_i);
                    System.out.println("Avg Damage (Not Including Deaths): " + ((double) (session.totalDamageTaken - session.origState.player.origHealth * session.deathCount)) / (session.game_i - session.deathCount));
                    System.out.println("Time Taken: " + (end - start));
                    System.out.println("Time Taken (By Model): " + session.mcts.model.time_taken);
                    System.out.println("Model: cache_size=" + session.mcts.model.cache.size() + ", " + session.mcts.model.cache_hits + "/" + session.mcts.model.calls + " hits (" + (double) session.mcts.model.cache_hits / session.mcts.model.calls + ")");
                    System.out.println("--------------------");
                }
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
            session.setTrainingDataLogFile("training_data.txt");
            session.randomize_enemy = CURRICULUM_TRAINING_ON;
            long start = System.currentTimeMillis();
            var games = new ArrayList<List<GameStep>>();
            for (int i = 0; i < 200; i++) {
                session.playTrainingGame(100);
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
            File file = new File(curIterationDir +  "/training_data.bin");
            file.delete();
            var writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(curIterationDir +  "/training_data.bin")));
            for (List<GameStep> game : games) {
                var lastState = game.get(game.size() - 1).state();
                float v_health = (float) (((double) lastState.player.health) / lastState.player.maxHealth);
                float v_win = lastState.isTerminal() == 1 ? 1.0f : 0.0f;
                for (int i = game.size() - 2; i >= 0; i--) {
                    state = game.get(i).state();
                    var x = state.getInput();
                    for (int j = 0; j < x.length; j++) {
                        writer.writeFloat(x[j]);
                    }
                    writer.writeFloat(v_health);
                    writer.writeFloat(v_win);
                    for (int j = 0; j < state.prop.totalNumOfActions; j++) {
                        if (j < state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length) {
                            if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                                writer.writeFloat(0);
                            } else {
                                assert !state.isActionLegal(j) || state.n[j] > 0;
                                writer.writeFloat((float) (((double) state.n[j]) / state.total_n));
                            }
                        } else {
                            if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                                assert !state.isActionLegal(j) || state.n[j] > 0;
                                writer.writeFloat((float) (((double) state.n[j - state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length]) / state.total_n));
                            } else {
                                writer.writeFloat(0);
                            }
                        }
                    }
                    if (!SLOW_TRAINING_WINDOW && state.isStochastic && i > 0) {
                        var prevState = game.get(i - 1).state();
                        var prevAction = game.get(i - 1).action();
                        ChanceState cState = new ChanceState(state);
                        for (int j = 1; j < 1000; j++) {
                            cState.getNextState(prevState, prevAction);
                        }
                        double est_v_win = 0;
                        double est_v_health = 0;
                        double[] out = new double[2];
                        for (ChanceState.Node node : cState.cache.values()) {
                            if (node.state != state) {
                                node.state.doEval(session.mcts.model);
                                node.state.get_v(out);
                                est_v_win += out[0] * node.n;
                                est_v_health += out[1] * node.n;
                            }
                        }
                        est_v_win /= cState.total_n;
                        est_v_health /= cState.total_n;
                        float p = ((float) cState.getCount(state)) / cState.total_n;
//                         System.out.println(p + "/" + prevState.v_win + "/" + (v_win * p + est_v_win) + "/" + prevState.v_win * (1 - p) + "/" + est_v_win + "/" + prevState.toStringReadable() + "/" + prevState.getActionString(prevAction));
//                         System.out.println(p + "/" + prevState.v_health + "/" + (v_health * p + est_v_health) + "/" + prevState.v_health * (1 - p) + "/" + est_v_health + "/" + prevState.toStringReadable() + "/" + prevState.getActionString(prevAction));
                        v_win = Math.min(v_win * p + (float) est_v_win, 1);
                        v_health = Math.min(v_health * p + (float) est_v_health, 1);
//                        v_win = v_win * p + (float) prevState.v_win * (1 - p);
//                        v_health = v_health * p + (float) prevState.v_health * (1 - p);
                    }
                }
            }
            writer.flush();
        }

        session.flushFileWriters();
    }
}
