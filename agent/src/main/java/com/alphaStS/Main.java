package com.alphaStS;

import com.alphaStS.enemy.Enemy;
import com.alphaStS.player.Player;
import com.alphaStS.utils.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static com.alphaStS.InteractiveMode.interactiveStart;

public class Main {
    public static GameState BasicGremlinNobState() {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.Bash(), 1));
        cards.add(new CardCount(new Card.BashP(), 0));
        cards.add(new CardCount(new Card.Strike(), 5));
        cards.add(new CardCount(new Card.StrikeP(), 0));
        cards.add(new CardCount(new Card.Defend(), 4));
        cards.add(new CardCount(new Card.DefendP(), 0));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.GremlinNob());
        var relics = new ArrayList<Relic>();
        var randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new Card.Bash(), 1),
                        new CardCount(new Card.Strike(), 5),
                        new CardCount(new Card.Defend(), 4)),
                List.of(new CardCount(new Card.Bash(), 1),
                        new CardCount(new Card.Strike(), 4),
                        new CardCount(new Card.StrikeP(), 1),
                        new CardCount(new Card.Defend(), 4)),
                List.of(new CardCount(new Card.Bash(), 1),
                        new CardCount(new Card.Strike(), 3),
                        new CardCount(new Card.StrikeP(), 2),
                        new CardCount(new Card.Defend(), 4)),
                List.of(new CardCount(new Card.BashP(), 1),
                        new CardCount(new Card.Strike(), 5),
                        new CardCount(new Card.Defend(), 4)),
                List.of(new CardCount(new Card.Bash(), 1),
                        new CardCount(new Card.Strike(), 5),
                        new CardCount(new Card.DefendP(), 4))
        )).setDescriptions(
                "No Upgrade",
                "One Strike Upgrade",
                "Two Strikes Upgrade",
                "Bash Upgrade",
                "All Defends Upgraded"
        );
        return new GameState(enemies, new Player(80, 80), cards, relics, null, randomization);
    }

    public static GameState BasicGremlinNobState2() {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.BashP(), 1));
        cards.add(new CardCount(new Card.Strike(), 5));
        cards.add(new CardCount(new Card.Defend(), 4));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.GremlinNob());
        var relics = new ArrayList<Relic>();
        return new GameState(enemies, new Player(80, 80), cards, relics, null, null);
    }

    public static GameState BasicJawWormState() {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.Bash(), 1));
        cards.add(new CardCount(new Card.Strike(), 5));
        cards.add(new CardCount(new Card.Defend(), 4));
        cards.add(new CardCount(new CardSilent.Neutralize(), 0));
        cards.add(new CardCount(new CardSilent.Survivor(), 0));
        cards.add(new CardCount(new CardSilent.Acrobatics(), 0));
        cards.add(new CardCount(new Card.AscendersBane(), 1));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.JawWorm());
        var relics = new ArrayList<Relic>();
        relics.add(new Relic.RingOfSerpant());
        var randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new Card.Bash(), 1),
                        new CardCount(new Card.Strike(), 5),
                        new CardCount(new Card.Defend(), 4)),
                List.of(new CardCount(new Card.Strike(), 5),
                        new CardCount(new Card.Defend(), 5),
                        new CardCount(new CardSilent.Neutralize(), 1),
                        new CardCount(new CardSilent.Survivor(), 1)),
                List.of(new CardCount(new Card.Strike(), 5),
                        new CardCount(new Card.Defend(), 5),
                        new CardCount(new CardSilent.Neutralize(), 1),
                        new CardCount(new CardSilent.Survivor(), 1),
                        new CardCount(new CardSilent.Acrobatics(), 1))
        )).setDescriptions(
                "Ironclad Starter Deck",
                "Silent Starter Deck",
                "Silent Starter Deck With Acrobatics"
        );
        return new GameState(enemies, new Player(80, 80), cards, relics, null, randomization);
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
        // https://youtu.be/1CELexRf5ZE?t=2205
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
        var potions = new ArrayList<Potion>();
        potions.add(new Potion.DexterityPotion());
        var player = new Player(73, 75);
        return new GameState(enemies, player, cards, relics, potions);
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
        cards.add(new CardCount(new Card.CarnageP(), 0));
        cards.add(new CardCount(new Card.Metallicize(), 1));
        cards.add(new CardCount(new Card.Shockwave(), 1));
        cards.add(new CardCount(new Card.ShrugItOff(), 1));
        cards.add(new CardCount(new Card.ShrugItOffP(), 0));
        cards.add(new CardCount(new Card.PowerThrough(), 1));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.TheGuardian());
        var relics = new ArrayList<Relic>();
        relics.add(new Relic.AncientTeaSet());
        relics.add(new Relic.DuVuDoll());
        relics.add(new Relic.WarpedTongs());
        var randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new Card.Carnage(), 1),
                        new CardCount(new Card.ShrugItOff(), 1),
                        new CardCount(new Card.PowerThrough(), 1)),
                List.of(new CardCount(new Card.Carnage(), 1),
                        new CardCount(new Card.ShrugItOffP(), 1),
                        new CardCount(new Card.PowerThrough(), 1)),
                List.of(new CardCount(new Card.CarnageP(), 1),
                        new CardCount(new Card.ShrugItOff(), 1),
                        new CardCount(new Card.PowerThrough(), 1)),
                List.of(new CardCount(new Card.Carnage(), 1),
                        new CardCount(new Card.ShrugItOff(), 1))
        )).join(new GameStateRandomization.SimpleCustomRandomization(List.of(
                (state) -> state.getPlayerForWrite().setHealth(41),
                (state) -> state.getPlayerForWrite().setHealth(19),
                (state) -> state.getPlayerForWrite().setHealth(19),
                (state) -> state.getPlayerForWrite().setHealth(41)
        ))).setDescriptions(
                "Health 41",
                "Health 19, Upgrade Shrug It Off",
                "Health 19, Upgrade Carnage",
                "Health 41, No Power Through"
        );
        return new GameState(enemies, new Player(41, 75), cards, relics, null, randomization);
    }

    public static GameState BasicInfiniteState() {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.BashP(), 1));
        cards.add(new CardCount(new Card.Dropkick(), 2));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.TheGuardian());
        var relics = new ArrayList<Relic>();
        return new GameState(enemies, new Player(41, 75), cards, relics);
    }

    public static GameState SlimeBossStateLC() {
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
        enemies.add(new Enemy.MediumSpikeSlime(37, true));
        enemies.add(new Enemy.MediumSpikeSlime(37, true));
        enemies.add(new Enemy.LargeAcidSlime(75, true));
        enemies.add(new Enemy.MediumAcidSlime(37, true));
        enemies.add(new Enemy.MediumAcidSlime(37, true));
        var relics = new ArrayList<Relic>();
        relics.add(new Relic.Anchor());
        var player = new Player(47, 75);
//        player.gainArtifact(1);
        List<Potion> potions = null;
        potions = List.of(new Potion.AncientPotion());
        return new GameState(enemies, player, cards, relics, potions);
    }

    public static GameState TestState() {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.Bash(), 1));
        cards.add(new CardCount(new Card.Strike(), 4));
        cards.add(new CardCount(new Card.Defend(), 4));
        cards.add(new CardCount(new Card.UppercutP(), 1));
        cards.add(new CardCount(new Card.Anger(), 1));
        cards.add(new CardCount(new Card.Inflame(), 1));
        cards.add(new CardCount(new Card.AscendersBane(), 1));
        cards.add(new CardCount(new CardColorless.DarkShackles(), 1));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
        enemies.add(new Enemy.Sentry(45, Enemy.Sentry.BEAM));
        enemies.add(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
        enemies.add(new Enemy.GremlinNob());
        enemies.add(new Enemy.Lagavulin());
        var relics = new ArrayList<Relic>();
        relics.add(new Relic.BagOfPreparation());
        var randomization = new GameStateRandomization.EnemyEncounterRandomization(enemies,
                new int[] { 0, 1, 2 },
                new int[] { 3 },
                new int[] { 4 }
        );
        //        randomization = null;
        return new GameState(enemies, new Player(43, 75), cards, relics, null, randomization);
    }

    public static GameState TestState2() {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.Bash(), 1));
        cards.add(new CardCount(new Card.Strike(), 4));
        cards.add(new CardCount(new Card.Defend(), 4));
        cards.add(new CardCount(new Card.UppercutP(), 1));
        cards.add(new CardCount(new Card.Anger(), 1));
        cards.add(new CardCount(new Card.Inflame(), 1));
        cards.add(new CardCount(new Card.AscendersBane(), 1));
        cards.add(new CardCount(new CardColorless.DarkShackles(), 1));
        cards.add(new CardCount(new Card.Combust(), 1));
        cards.add(new CardCount(new Card.SeeingRed(), 1));
        cards.add(new CardCount(new Card.IronWave(), 1));
        cards.add(new CardCount(new Card.DemonForm(), 1));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
        enemies.add(new Enemy.Sentry(45, Enemy.Sentry.BEAM));
        enemies.add(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
        enemies.add(new Enemy.GremlinNob());
        enemies.add(new Enemy.Lagavulin());
        var relics = new ArrayList<Relic>();
        relics.add(new Relic.BagOfPreparation());
        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new Card.Combust(), 1), new CardCount(new Card.IronWave(), 1)),
                List.of(new CardCount(new Card.Combust(), 1), new CardCount(new Card.DemonForm(), 1)),
                List.of(new CardCount(new Card.SeeingRed(), 1), new CardCount(new Card.IronWave(), 1)),
                List.of(new CardCount(new Card.SeeingRed(), 1), new CardCount(new Card.DemonForm(), 1)),
                List.of(new CardCount(new Card.Combust(), 1))
        ));
        randomization = new GameStateRandomization.EnemyEncounterRandomization(enemies,
                new int[] { 0, 1, 2 },
                new int[] { 3 },
                new int[] { 4 }
        ).doAfter(randomization).fixR(0, 1, 2, 12, 13, 14);
        return new GameState(enemies, new Player(41, 75), cards, relics, null, randomization);
    }

    public static GameState TestState3() {
        var cards = new ArrayList<CardCount>();
        cards.add(new CardCount(new Card.Bash(), 1));
        cards.add(new CardCount(new Card.Strike(), 4));
        cards.add(new CardCount(new Card.Defend(), 4));
        cards.add(new CardCount(new Card.UppercutP(), 1));
        cards.add(new CardCount(new Card.Anger(), 1));
        cards.add(new CardCount(new Card.Inflame(), 1));
        cards.add(new CardCount(new Card.AscendersBane(), 1));
        cards.add(new CardCount(new CardColorless.DarkShackles(), 1));
        cards.add(new CardCount(new Card.Combust(), 1));
        cards.add(new CardCount(new Card.IronWave(), 1));
        cards.add(new CardCount(new Card.SpotWeakness(), 1));
        var enemies = new ArrayList<Enemy>();
        enemies.add(new Enemy.SmallAcidSlime());
        enemies.add(new Enemy.SmallAcidSlime());
        enemies.add(new Enemy.SmallSpikeSlime());
        enemies.add(new Enemy.SmallSpikeSlime());
        enemies.add(new Enemy.SmallSpikeSlime());
        var relics = new ArrayList<Relic>();
        relics.add(new Relic.BagOfPreparation());
        var potions = new ArrayList<Potion>();
        potions.add(new Potion.WeakPotion());
        return new GameState(enemies, new Player(13, 75), cards, relics, potions, null);
    }

    public static void main(String[] args) throws IOException {
        var state = SlimeBossStateLC();

        if (args.length > 0 && args[0].equals("--get-lengths")) {
            System.out.print(state.getNNInput().length + "," + state.prop.totalNumOfActions);
            return;
        }

        if (args.length > 0 && args[0].equals("--server")) {
            ServerSocket serverSocket = new ServerSocket(4000);
            Socket socket = serverSocket.accept();
            System.out.println(socket.getInputStream().read());
            System.out.println(socket.getInputStream().read());
            socket.close();
            return;
        }

        if (args.length > 0 && args[0].equals("--client")) {
            Socket socket = new Socket("127.0.0.1", 4000);
            socket.getOutputStream().write(10);
            socket.getOutputStream().write(10);
            socket.close();
            return;
        }

        boolean GENERATE_TRAINING_GAMES = false;
        boolean TEST_TRAINING_AGENT = false;
        boolean PLAY_GAMES = false;
        boolean PLAY_A_GAME = false;
        boolean SLOW_TRAINING_WINDOW = false;
        boolean CURRICULUM_TRAINING_ON = false;
        boolean TRAINING_WITH_LINE = false;
        boolean GAMES_ADD_ENEMY_RANDOMIZATION = false;
        boolean GAMES_ADD_POTION_RANDOMIZATION = false;
        int POTION_STEPS = 4;
        int NUMBER_OF_GAMES_TO_PLAY = 5;
        int NUMBER_OF_NODES_PER_TURN = 1000;
        int NUMBER_OF_THREADS = 2;
        int RANDOMIZATION_SCENARIO = -1;
        String COMPARE_DIR = null;
        String SAVES_DIR = "../saves";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-training")) {
                GENERATE_TRAINING_GAMES = true;
            }
            if (args[i].equals("-tm")) {
                TEST_TRAINING_AGENT = true;
            }
            if (args[i].equals("-t")) {
                NUMBER_OF_THREADS = Integer.parseInt(args[i + 1]);
            }
            if (args[i].equals("-g")) {
                PLAY_GAMES = true;
            }
            if (args[i].equals("-p")) {
                PLAY_A_GAME = true;
            }
            if (args[i].equals("-c")) {
                NUMBER_OF_GAMES_TO_PLAY = Integer.parseInt(args[i + 1]);
                i++;
            }
            if (args[i].equals("-n")) {
                NUMBER_OF_NODES_PER_TURN = Integer.parseInt(args[i + 1]);
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
            if (args[i].equals("-training_with_line")) {
                TRAINING_WITH_LINE = true;
            }
            if (args[i].equals("-rand")) {
                RANDOMIZATION_SCENARIO = Integer.parseInt(args[i + 1]);
                i++;
            }
        }

        int iteration = -1;
        if (SAVES_DIR.startsWith("../")) {
//            SAVES_DIR = "../tmp/test/saves_sentries_norm";
//            SAVES_DIR = "../tmp/test/saves_nob";
//            SAVES_DIR = "../tmp/test/saves_laga";
//            SAVES_DIR = "../tmp/test2/saves";
//            SAVES_DIR = "../tmp/slimeboss/saves_artifact";
            NUMBER_OF_GAMES_TO_PLAY = 1000;
            GAMES_ADD_ENEMY_RANDOMIZATION = true;
            GAMES_ADD_POTION_RANDOMIZATION = true;
            NUMBER_OF_NODES_PER_TURN = 5000;
//            iteration = 31;
//            RANDOMIZATION_SCENARIO = 0;
        }

        if (!GENERATE_TRAINING_GAMES && GAMES_ADD_ENEMY_RANDOMIZATION) {
            state.prop.randomization = new GameStateRandomization.EnemyRandomization(false).doAfter(state.prop.randomization);
        }
        if (!GENERATE_TRAINING_GAMES && GAMES_ADD_POTION_RANDOMIZATION && state.prop.potions != null) {
            state.prop.randomization = new GameStateRandomization.PotionsUtilityRandomization(state.prop.potions, POTION_STEPS).fixR(0).doAfter(state.prop.randomization);
        }

        ObjectMapper mapper = new ObjectMapper();
        String curIterationDir = SAVES_DIR + "/iteration0";
        try {
            JsonNode root = mapper.readTree(new File(SAVES_DIR + "/training.json"));
            iteration = iteration < 0 ? root.get("iteration").asInt() : iteration;
            curIterationDir = SAVES_DIR + "/iteration" + (iteration - 1);
            File f = new File(SAVES_DIR + "/desc.txt");
            if (!f.exists()) {
                writeStateDescription(f, state);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Unable to find neural network.");
        }

        if (args.length > 0 && (args[0].equals("--i") || args[0].equals("-i"))) {
            interactiveStart(state, curIterationDir);
            return;
        }

        if (PLAY_A_GAME) {
            MatchSession session = new MatchSession(1, curIterationDir);
            for (GameStep step : session.playGame(state, session.mcts.get(0), NUMBER_OF_NODES_PER_TURN, RANDOMIZATION_SCENARIO).steps()) {
                System.out.println(step.state().toStringReadable());
                if (step.action() >= 0) {
                    System.out.println("action=" + step.state().getActionString(step.action()) + " (" + step.action() + ")");
                }
            }
        }

        MatchSession session = new MatchSession(NUMBER_OF_THREADS, curIterationDir);
        if (TEST_TRAINING_AGENT || PLAY_GAMES) {
            if (!TEST_TRAINING_AGENT) {
//                if (state.prop.randomization != null) {
//                    state.prop.randomization.randomize(state, RANDOMIZATION_SCENARIO);
//                }
//                GameSolver solver = new GameSolver(state);
//                solver.solve();
//                session.solver = solver;
            }
            session.training = TEST_TRAINING_AGENT;
            if (COMPARE_DIR != null) {
                session.compareModel = new Model(COMPARE_DIR);
            }
            if (TEST_TRAINING_AGENT && NUMBER_OF_GAMES_TO_PLAY <= 100) {
                session.setMatchLogFile("training_matches.txt");
            } else if (NUMBER_OF_GAMES_TO_PLAY <= 100) {
                session.setMatchLogFile("matches.txt");
            }
            session.playGames(state, NUMBER_OF_GAMES_TO_PLAY, NUMBER_OF_NODES_PER_TURN, RANDOMIZATION_SCENARIO, !TEST_TRAINING_AGENT);
        }

        if (GENERATE_TRAINING_GAMES) {
            session.setTrainingDataLogFile("training_data.txt");
            session.SLOW_TRAINING_WINDOW = SLOW_TRAINING_WINDOW;
            session.POLICY_CAP_ON = false;
            session.TRAINING_WITH_LINE = TRAINING_WITH_LINE;
            long start = System.currentTimeMillis();
            state.prop.randomization = new GameStateRandomization.EnemyRandomization(CURRICULUM_TRAINING_ON).doAfter(state.prop.randomization);
            if (state.prop.potions != null) {
                state.prop.randomization = new GameStateRandomization.PotionsUtilityRandomization(state.prop.potions, POTION_STEPS).fixR(0).doAfter(state.prop.randomization);
            }
            var games = session.playTrainingGames(state, 200, 100, CURRICULUM_TRAINING_ON);
            writeTrainingData(games, curIterationDir + "/training_data.bin");
            long end = System.currentTimeMillis();
            System.out.println("Time Taken: " + (end - start));
            for (int i = 0; i < session.mcts.size(); i++) {
                var m = session.mcts.get(i);
                System.out.println("Time Taken (By Model " + i + "): " + m.model.time_taken);
                System.out.println("Model " + i + ": size=" + m.model.cache.size() + ", " + m.model.cache_hits + "/"
                        + m.model.calls + " hits (" + (double) m.model.cache_hits / m.model.calls + ")");
            }
            System.out.println("--------------------");
        }

        session.flushFileWriters();
    }

    private static void writeTrainingData(List<List<GameStep>> games, String path) throws IOException {
        File file = new File(path);
        file.delete();
        var stream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
        for (var game : games) {
            for (int i = game.size() - 2; i >= 0; i--) {
                var step = game.get(i);
                if (!step.useForTraining) {
                    continue;
                }
                var state = game.get(i).state();
                var x = state.getNNInput();
                for (int j = 0; j < x.length; j++) {
                    stream.writeFloat(x[j]);
                }
                stream.writeFloat((step.v_health * 2) - 1);
                stream.writeFloat((step.v_win * 2) - 1);
                int idx = 0;
                for (int j = 0; j < state.prop.totalNumOfActions; j++) {
                    if (j < state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length) {
                        if (state.actionCtx == GameActionCtx.SELECT_ENEMY || !state.isActionLegal(j)) {
                            stream.writeFloat(-1);
                        } else {
                            if (state.terminal_action >= 0) {
                                if (state.getLegalActions()[state.terminal_action] == j) {
                                    stream.writeFloat(1);
                                } else {
                                    stream.writeFloat(0);
                                }
                            } else {
                                if (idx < state.getLegalActions().length && state.getLegalActions()[idx] == j) {
                                    stream.writeFloat((float) (((double) state.n[idx++]) / state.total_n));
                                } else {
                                    Integer.parseInt(null);
                                }
                            }
                        }
                    } else {
                        int action = j - state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length;
                        if (state.actionCtx == GameActionCtx.SELECT_ENEMY && state.isActionLegal(action)) {
                            if (state.terminal_action >= 0) {
                                if (state.getLegalActions()[state.terminal_action] == action) {
                                    stream.writeFloat(1);
                                } else {
                                    stream.writeFloat(0);
                                }
                            } else {
                                if (idx < state.getLegalActions().length && state.getLegalActions()[idx] == action) {
                                    stream.writeFloat((float) (((double) state.n[idx++]) / state.total_n));
                                } else {
                                    Integer.parseInt(null);
                                }
                            }
                        } else {
                            stream.writeFloat(-1);
                        }
                    }
                }
            }
        }
        stream.flush();
        stream.close();
    }

    private static void writeStateDescription(File f, GameState state) throws IOException {
        Writer writer = new BufferedWriter(new FileWriter(f));
        writer.write("************************** NN Description **************************\n");
        writer.write(state.getNNInputDesc());
        if (state.prop.randomization != null) {
            writer.write("\n************************** Randomizations **************************\n");
            int i = 1;
            for (var info : state.prop.randomization.listRandomizations().values()) {
                writer.write(i + ". (" + Utils.formatFloat(info.chance() * 100) + "%) " + info.desc() + "\n");
                i += 1;
            }
        }
        writer.write("\n************************** Other **************************\n");
        var i = 1;
        for (var enemy : state.getEnemiesForRead()) {
            writer.write("Enemy " + (i++) + ": " + enemy.toString(state) + "\n");
        }
        writer.flush();
        writer.close();
    }
}
