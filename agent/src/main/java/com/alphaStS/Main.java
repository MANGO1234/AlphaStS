package com.alphaStS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    public static GameState BasicGremlinNobState3() {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.AscendersBane(), 1));
        cards.add(new CardCount(new Card.FeelNoPain(), 1));
        cards.add(new CardCount(new Card.Bash(), 1));
        cards.add(new CardCount(new Card.Strike(), 3));
        cards.add(new CardCount(new Card.StrikeP(), 1));
        cards.add(new CardCount(new Card.Dropkick(), 1));
        cards.add(new CardCount(new Card.Defend(), 3));
        cards.add(new CardCount(new Card.DefendP(), 1));
        cards.add(new CardCount(new Card.PommelStrike(), 1));
        cards.add(new CardCount(new Card.Whirlwind(), 1));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.GremlinNob());
        var relics = new ArrayList<Relic>();
        relics.add(new Relic.BagOfPreparation());
        return new GameState(enemies, new Player(37, 75), cards, relics);
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
        cards.add(new CardCount(new Card.BurningPactP(), 1));
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

    public static GameState BasicLagavulinState() {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.Bash(), 1));
        cards.add(new CardCount(new Card.Strike(), 2));
        cards.add(new CardCount(new Card.BodySlam(), 1));
        cards.add(new CardCount(new Card.Cleave(), 1));
        cards.add(new CardCount(new Card.IronWave(), 1));
        cards.add(new CardCount(new Card.AscendersBane(), 1));
        cards.add(new CardCount(new Card.Defend(), 4));
        cards.add(new CardCount(new Card.Impervious(), 1));
        cards.add(new CardCount(new Card.SeeingRed(), 1));
        cards.add(new CardCount(new Card.Exhume(), 1));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.Lagavulin());
        var relics = new ArrayList<Relic>();
        relics.add(new Relic.HappyFlower());
        var player = new Player(73, 75);
        player.dexterity = 2;
        return new GameState(enemies, player, cards, relics);
    }

    public static GameState BasicLagavulinState2() {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.Bash(), 1));
        cards.add(new CardCount(new Card.Strike(), 5));
        cards.add(new CardCount(new Card.AscendersBane(), 1));
        cards.add(new CardCount(new Card.Defend(), 4));
        cards.add(new CardCount(new Card.Impervious(), 1));
        cards.add(new CardCount(new Card.SeeingRed(), 1));
        cards.add(new CardCount(new Card.BattleTrance(), 1));
        cards.add(new CardCount(new Card.PommelStrike(), 1));
        cards.add(new CardCount(new Card.Shockwave(), 1));
        cards.add(new CardCount(new Card.Inflame(), 1));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.Lagavulin());
        var relics = new ArrayList<Relic>();
        var player = new Player(60, 75);
        return new GameState(enemies, player, cards, relics);
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

    public static GameState GuardianState() {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.Bash(), 1));
        cards.add(new CardCount(new Card.Strike(), 2));
        cards.add(new CardCount(new Card.Defend(), 4));
        cards.add(new CardCount(new Card.AscendersBane(), 1));
        cards.add(new CardCount(new Card.BodySlamP(), 1));
        cards.add(new CardCount(new Card.Anger(), 1));
        cards.add(new CardCount(new Card.HemokinesisP(), 1));
        cards.add(new CardCount(new Card.Metallicize(), 1));
        cards.add(new CardCount(new Card.Hemokinesis(), 1));
        cards.add(new CardCount(new Card.BattleTrance(), 1));
        cards.add(new CardCount(new Card.FlameBarrier(), 1));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.TheGuardian());
        var relics = new ArrayList<Relic>();
        relics.add(new Relic.CentennialPuzzle());
        return new GameState(enemies, new Player(36, 75), cards, relics);
    }

    public static GameState GuardianState2() {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.BashP(), 1));
        cards.add(new CardCount(new Card.Strike(), 4));
        cards.add(new CardCount(new Card.Defend(), 4));
        cards.add(new CardCount(new Card.AscendersBane(), 1));
        cards.add(new CardCount(new Card.Anger(), 1));
        cards.add(new CardCount(new Card.Clash(), 1));
        cards.add(new CardCount(new Card.Armanent(), 1));
        cards.add(new CardCount(new Card.Carnage(), 1));
        cards.add(new CardCount(new Card.Metallicize(), 1));
        cards.add(new CardCount(new Card.Shockwave(), 1));
        cards.add(new CardCount(new Card.ShrugItOff(), 1));
        cards.add(new CardCount(new Card.PowerThrough(), 1));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.TheGuardian());
        var relics = new ArrayList<Relic>();
        relics.add(new Relic.AncientTeaSet());
        relics.add(new Relic.DuVuDoll());
        relics.add(new Relic.WarpedTongs());
//        return new GameState(enemies, new Player(19, 75), cards, relics);
        return new GameState(enemies, new Player(41, 75), cards, relics);
    }

    private static GameState SlimeBossStateLC() {
        // https://youtu.be/wKbAoS80HA0?t=11397
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
        return new GameState(enemies, new Player(47, 75), cards, relics);
    }

    public static void main(String[] args) throws IOException {
        var state = GuardianState2();

        if (args.length > 0 && args[0].equals("--get-lengths")) {
            System.out.print(state.getNNInput().length + "," + state.prop.totalNumOfActions);
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

        ObjectMapper mapper = new ObjectMapper();
        String curIterationDir = SAVES_DIR + "/iteration0";
        try {
            JsonNode root = mapper.readTree(new File(SAVES_DIR + "/training.json"));
            int iteration = root.get("iteration").asInt();
            if (SAVES_DIR.startsWith("../")) {
                MATCHES_COUNT = 1000;
                NODE_COUNT = 10;
            }
            curIterationDir = SAVES_DIR + "/iteration" + (iteration - 1);
        } catch (FileNotFoundException e) {
            System.out.println("Unable to find neural network.");
        }

        if (args.length > 0 && (args[0].equals("--i") || args[0].equals("-i"))) {
            interactiveStart(state, curIterationDir);
            return;
        }

        if (PLAY_A_GAME) {
            MatchSession session = new MatchSession(1, curIterationDir);
            for (GameStep step : session.playGame(state, session.mcts.get(0), NODE_COUNT)) {
                System.out.println(step.state().toStringReadable());
                if (step.action() >= 0) {
                    System.out.println("action=" + step.state().getActionString(step.action()) + " (" + step.action() + ")");
                }
            }
        }

        MatchSession session = new MatchSession(2, curIterationDir);
        if (TEST_AGENT_FITNESS || PLAY_MATCHES) {
            if (TEST_AGENT_FITNESS && MATCHES_COUNT <= 100) {
                session.setMatchLogFile("training_matches.txt");
            } else if (MATCHES_COUNT <= 100) {
                session.setMatchLogFile("matches.txt");
            }
            session.playGames(state, MATCHES_COUNT, NODE_COUNT, !TEST_AGENT_FITNESS);
        }

        if (GEN_TRAINING_MATCHES) {
            session.setTrainingDataLogFile("training_data.txt");
            session.SLOW_TRAINING_WINDOW = SLOW_TRAINING_WINDOW;
            session.POLICY_CAP_ON = false;
            long start = System.currentTimeMillis();
            var games = session.playTrainingGames(state, 200, 100, CURRICULUM_TRAINING_ON);
            long end = System.currentTimeMillis();
            System.out.println("Time Taken: " + (end - start));
            for (int i = 0; i < session.mcts.size(); i++) {
                var m = session.mcts.get(i);
                System.out.println("Time Taken (By Model " + i + "): " + m.model.time_taken);
                System.out.println("Model " + i + ": size=" + m.model.cache.size() + ", " + m.model.cache_hits + "/" + m.model.calls + " hits (" + (double) m.model.cache_hits / m.model.calls + ")");
            }
            System.out.println("--------------------");
            File file = new File(curIterationDir +  "/training_data.bin");
            file.delete();
            start = System.currentTimeMillis();
            var writer = new BufferedOutputStream(new FileOutputStream(curIterationDir +  "/training_data.bin"));
            writeTrainingData(games, writer);
            end = System.currentTimeMillis();
            System.out.println("Time Taken: " + (end - start));
            for (int i = 0; i < session.mcts.size(); i++) {
                var m = session.mcts.get(i);
                System.out.println("Time Taken (By Model " + i + "): " + m.model.time_taken);
                System.out.println("Model " + i + ": size=" + m.model.cache.size() + ", " + m.model.cache_hits + "/" + m.model.calls + " hits (" + (double) m.model.cache_hits / m.model.calls + ")");
            }
            System.out.print("--------------------");
            writer.flush();
        }

        session.flushFileWriters();
    }

    private static void writeTrainingData(List<List<GameStep>> games, BufferedOutputStream fileWriter) throws IOException {
        DataOutputStream writer = new DataOutputStream(fileWriter);
        for (var game : games) {
            for (int i = game.size() - 2; i >= 0; i--) {
                var step = game.get(i);
                if (!step.useForTraining) {
                    continue;
                }
                var state = game.get(i).state();
                var x = state.getNNInput();
                for (int j = 0; j < x.length; j++) {
                    writer.writeFloat(x[j]);
                }
                writer.writeFloat(step.v_health);
                writer.writeFloat(step.v_win);
                for (int j = 0; j < state.prop.totalNumOfActions; j++) {
                    if (j < state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length) {
                        if (state.actionCtx == GameActionCtx.SELECT_ENEMY || !state.isActionLegal(j)) {
                            writer.writeFloat(-1);
                        } else {
                            if (state.terminal_action > 0) {
                                if (state.terminal_action == j) {
                                    writer.writeFloat(1);
                                } else {
                                    writer.writeFloat(0);
                                }
                            } else {
                                writer.writeFloat((float) (((double) state.n[j]) / state.total_n));
                            }
                        }
                    } else {
                        int action = j - state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length;
                        if (state.actionCtx == GameActionCtx.SELECT_ENEMY && state.isActionLegal(action)) {
                            if (state.terminal_action > 0) {
                                if (state.terminal_action == action) {
                                    writer.writeFloat(1);
                                } else {
                                    writer.writeFloat(0);
                                }
                            } else {
                                writer.writeFloat((float) (((double) state.n[action]) / state.total_n));
                            }
                        } else {
                            writer.writeFloat(-1);
                        }
                    }
                }
            }
        }
    }
}
