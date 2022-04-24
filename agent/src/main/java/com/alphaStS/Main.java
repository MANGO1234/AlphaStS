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
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.BashP(), 0);
        builder.addCard(new Card.Strike(), 5);
        builder.addCard(new Card.StrikeP(), 0);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.DefendP(), 0);
        builder.addEnemy(new Enemy.GremlinNob());
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
        builder.setRandomization(randomization);
        builder.setPlayer(new Player(80, 80));
        return new GameState(builder);
    }

    public static GameState BasicJawWormState() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.Strike(), 5);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardSilent.Neutralize(), 0);
        builder.addCard(new CardSilent.Survivor(), 0);
        builder.addCard(new CardSilent.Acrobatics(), 0);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addEnemy(new Enemy.JawWorm());
        builder.addRelic(new Relic.RingOfSerpant());
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
        builder.setRandomization(randomization);
        builder.setPlayer(new Player(80, 80));
        return new GameState(builder);
    }

    public static GameState BasicSentriesState() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.SeverSoul(), 1);
        builder.addCard(new Card.Clash(), 1);
        builder.addCard(new Card.Headbutt(), 1);
        builder.addCard(new Card.Anger(), 1);
        builder.addCard(new Card.BurningPactP(), 1);
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BEAM));
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.BronzeScales());
        builder.addRelic(new Relic.Vajira());
        builder.setPlayer(new Player(32, 75));
        return new GameState(builder);
    }

    public static GameState BasicLagavulinState() {
        // https://youtu.be/1CELexRf5ZE?t=2205
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.Strike(), 2);
        builder.addCard(new Card.BodySlam(), 1);
        builder.addCard(new Card.Cleave(), 1);
        builder.addCard(new Card.IronWave(), 1);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.Impervious(), 1);
        builder.addCard(new Card.SeeingRed(), 1);
        builder.addCard(new Card.Exhume(), 1);
        builder.addEnemy(new Enemy.Lagavulin());
        builder.addPotion(new Potion.DexterityPotion());
        builder.setPlayer(new Player(73, 75));
        return new GameState(builder);
    }

    public static GameState BasicLagavulinState2() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.Strike(), 5);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.Impervious(), 1);
        builder.addCard(new Card.SeeingRed(), 1);
        builder.addCard(new Card.BattleTrance(), 1);
        builder.addCard(new Card.PommelStrike(), 1);
        builder.addCard(new Card.Shockwave(), 1);
        builder.addCard(new Card.Inflame(), 1);
        builder.addEnemy(new Enemy.Lagavulin());
        builder.setPlayer(new Player(60, 75));
        return new GameState(builder);
    }

    public static GameState GuardianState() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.Strike(), 2);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.BodySlamP(), 1);
        builder.addCard(new Card.Anger(), 1);
        builder.addCard(new Card.HemokinesisP(), 1);
        builder.addCard(new Card.Metallicize(), 1);
        builder.addCard(new Card.Hemokinesis(), 1);
        builder.addCard(new Card.BattleTrance(), 1);
        builder.addCard(new Card.FlameBarrier(), 1);
        builder.addEnemy(new Enemy.TheGuardian());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.setPlayer(new Player(36, 75));
        return new GameState(builder);
    }

    public static GameState GuardianState2() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.BashP(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.Anger(), 1);
        builder.addCard(new Card.Clash(), 1);
        builder.addCard(new Card.Armanent(), 1);
        builder.addCard(new Card.Carnage(), 1);
        builder.addCard(new Card.CarnageP(), 0);
        builder.addCard(new Card.Metallicize(), 1);
        builder.addCard(new Card.Shockwave(), 1);
        builder.addCard(new Card.ShrugItOff(), 1);
        builder.addCard(new Card.ShrugItOffP(), 0);
        builder.addCard(new Card.PowerThrough(), 1);
        builder.addEnemy(new Enemy.TheGuardian());
        builder.addRelic(new Relic.AncientTeaSet());
        builder.addRelic(new Relic.DuVuDoll());
        builder.addRelic(new Relic.WarpedTongs());
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
        builder.setRandomization(randomization);
        builder.setPlayer(new Player(41, 75));
        return new GameState(builder);
    }

    public static GameState BasicInfiniteState() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.BashP(), 1);
        builder.addCard(new Card.Dropkick(), 2);
        builder.addEnemy(new Enemy.TheGuardian());
        builder.setPlayer(new Player(41, 75));
        return new GameState(builder);
    }

    public static GameState SlimeBossStateLC() {
        // https://youtu.be/wKbAoS80HA0?t=11397
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.Strike(), 5);
        builder.addCard(new Card.Defend(), 3);
        builder.addCard(new Card.DefendP(), 1);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.Corruption(), 1);
        builder.addCard(new Card.TwinStrike(), 1);
        builder.addCard(new Card.UppercutP(), 1);
        builder.addCard(new Card.ShockwaveP(), 1);
        builder.addCard(new Card.ShrugItOff(), 1);
        builder.addCard(new Card.FlameBarrierP(), 1);
        builder.addCard(new Card.SpotWeakness(), 1);
        builder.addEnemy(new Enemy.SlimeBoss());
        builder.addEnemy(new Enemy.LargeSpikeSlime(75, true));
        builder.addEnemy(new Enemy.MediumSpikeSlime(37, true));
        builder.addEnemy(new Enemy.MediumSpikeSlime(37, true));
        builder.addEnemy(new Enemy.LargeAcidSlime(75, true));
        builder.addEnemy(new Enemy.MediumAcidSlime(37, true));
        builder.addEnemy(new Enemy.MediumAcidSlime(37, true));
        builder.addRelic(new Relic.Anchor());
        var player = new Player(47, 75);
//        player.gainArtifact(1);
        builder.setPlayer(player);
        builder.addPotion(new Potion.AncientPotion());
        return new GameState(builder);
    }

    public static GameState TestState() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.UppercutP(), 1);
        builder.addCard(new Card.Anger(), 1);
        builder.addCard(new Card.Inflame(), 1);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BEAM));
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
        builder.addEnemy(new Enemy.GremlinNob());
        builder.addEnemy(new Enemy.Lagavulin());
        builder.addRelic(new Relic.BagOfPreparation());
        var randomization = new GameStateRandomization.EnemyEncounterRandomization(builder.getEnemies(),
                new int[] { 0, 1, 2 },
                new int[] { 3 },
                new int[] { 4 }
        );
        builder.setRandomization(randomization);
        builder.setPlayer(new Player(43, 75));
        return new GameState(builder);
    }

    public static GameState TestState2() {
        GameStateBuilder builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.UppercutP(), 1);
        builder.addCard(new Card.Anger(), 1);
        builder.addCard(new Card.Inflame(), 1);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        builder.addCard(new Card.Combust(), 1);
        builder.addCard(new Card.SeeingRed(), 1);
        builder.addCard(new Card.IronWave(), 1);
        builder.addCard(new Card.DemonForm(), 1);
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BEAM));
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
        builder.addEnemy(new Enemy.GremlinNob());
        builder.addEnemy(new Enemy.Lagavulin());
        builder.addRelic(new Relic.BagOfPreparation());
        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new Card.Combust(), 1), new CardCount(new Card.IronWave(), 1)),
                List.of(new CardCount(new Card.Combust(), 1), new CardCount(new Card.DemonForm(), 1)),
                List.of(new CardCount(new Card.SeeingRed(), 1), new CardCount(new Card.IronWave(), 1)),
                List.of(new CardCount(new Card.SeeingRed(), 1), new CardCount(new Card.DemonForm(), 1)),
                List.of(new CardCount(new Card.Combust(), 1))
        ));
        randomization = new GameStateRandomization.EnemyEncounterRandomization(builder.getEnemies(),
                new int[] { 0, 1, 2 },
                new int[] { 3 },
                new int[] { 4 }
        ).doAfter(randomization).fixR(0, 1, 2, 12, 13, 14);
        builder.setRandomization(randomization);
        builder.setPlayer(new Player(41, 75));
        return new GameState(builder);
    }

    public static GameState TestState3() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.UppercutP(), 1);
        builder.addCard(new Card.Anger(), 1);
        builder.addCard(new Card.AngerP(), 0);
        builder.addCard(new Card.Inflame(), 1);
        builder.addCard(new Card.InflameP(), 0);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        builder.addCard(new CardColorless.DarkShacklesP(), 0);
        builder.addCard(new Card.Combust(), 1);
        builder.addCard(new Card.CombustP(), 0);
        builder.addCard(new Card.IronWave(), 1);
        builder.addCard(new Card.IronWaveP(), 0);
        builder.addCard(new Card.SpotWeakness(), 1);
        builder.addCard(new Card.BattleTrance(), 1);
        builder.addEnemy(new Enemy.Lagavulin().markAsBurningElite());
        builder.addEnemy(new Enemy.GremlinNob().markAsBurningElite());
        GameStateRandomization randomization = new GameStateRandomization.EnemyEncounterRandomization(builder.getEnemies(), new int[] {0}, new int[] {1});
        randomization = new GameStateRandomization.BurningEliteRandomization().doAfter(randomization);
        var startOfGameScenarios = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new Card.InflameP(), 1), new CardCount(new Card.IronWave(), 1), new CardCount(new Card.Anger(), 1), new CardCount(new CardColorless.DarkShackles(), 1), new CardCount(new Card.Combust(), 1)),
                List.of(new CardCount(new Card.Inflame(), 1), new CardCount(new Card.IronWaveP(), 1), new CardCount(new Card.Anger(), 1), new CardCount(new CardColorless.DarkShackles(), 1), new CardCount(new Card.Combust(), 1)),
                List.of(new CardCount(new Card.Inflame(), 1), new CardCount(new Card.IronWave(), 1), new CardCount(new Card.AngerP(), 1), new CardCount(new CardColorless.DarkShackles(), 1), new CardCount(new Card.Combust(), 1)),
                List.of(new CardCount(new Card.Inflame(), 1), new CardCount(new Card.IronWave(), 1), new CardCount(new Card.Anger(), 1), new CardCount(new CardColorless.DarkShacklesP(), 1), new CardCount(new Card.Combust(), 1)),
                List.of(new CardCount(new Card.Inflame(), 1), new CardCount(new Card.IronWave(), 1), new CardCount(new Card.Anger(), 1), new CardCount(new CardColorless.DarkShackles(), 1), new CardCount(new Card.CombustP(), 1))
        ));
        randomization = randomization.doAfter(startOfGameScenarios);
        builder.setRandomization(randomization);
//        builder.setPreBattleScenarios(startOfGameScenarios);
        builder.addPotion(new Potion.WeakPotion());
        builder.addPotion(new Potion.LiquidMemory());
        builder.setPlayer(new Player(16, 75));
        return new GameState(builder);
    }

    public static void main(String[] args) throws IOException {
        var state = TestState3();

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
        boolean GAMES_TEST_CHOOSE_SCENARIO_RANDOMIZATION = false;
        int POTION_STEPS = 1;
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
//            SAVES_DIR = "../tmp/burning/saves_all_pot";
            NUMBER_OF_GAMES_TO_PLAY = 100;
            GAMES_ADD_ENEMY_RANDOMIZATION = true;
            GAMES_ADD_POTION_RANDOMIZATION = true;
//            GAMES_TEST_CHOOSE_SCENARIO_RANDOMIZATION = true;
            NUMBER_OF_NODES_PER_TURN = 100;
//            iteration = 21;
//            COMPARE_DIR = "../saves/iteration20";
//            COMPARE_DIR = SAVES_DIR + "/iteration" + (iteration - 1);
//            RANDOMIZATION_SCENARIO = 0;
        }

        if (!GENERATE_TRAINING_GAMES && GAMES_ADD_ENEMY_RANDOMIZATION) {
            state.prop.randomization = new GameStateRandomization.EnemyRandomization(false).doAfter(state.prop.randomization);
        }
        if (!GENERATE_TRAINING_GAMES && GAMES_ADD_POTION_RANDOMIZATION && state.prop.potions != null) {
            state.prop.randomization = new GameStateRandomization.PotionsUtilityRandomization(state.prop.potions, POTION_STEPS).fixR(1).doAfter(state.prop.randomization);
        }
        if (!GENERATE_TRAINING_GAMES && GAMES_TEST_CHOOSE_SCENARIO_RANDOMIZATION && state.prop.preBattleScenarios != null) {
            if (state.prop.randomization == null) {
                state.prop.randomization = state.prop.preBattleScenarios;
            } else {
                state.prop.randomization = state.prop.randomization.doAfter(state.prop.preBattleScenarios);
            }
            state.prop.preBattleScenarios = null;
            state.setActionCtx(GameActionCtx.BEGIN_BATTLE, null);
        }
        if (RANDOMIZATION_SCENARIO >= 0 && state.prop.randomization != null) {
            state.prop.randomization = state.prop.randomization.fixR(RANDOMIZATION_SCENARIO);
        }
//        state.prop.randomization = state.prop.randomization.fixR(0,1,2,3,4,5,6,7);
//        state.prop.randomization = state.prop.randomization.fixR(8,9,10,11,12,13,14,15);
//        state.prop.randomization = state.prop.randomization.fixR(16,17,18,19,20,21,22,23);
//        state.prop.randomization = state.prop.randomization.fixR(24,25,26,27,28,29,30,31);
//        state.prop.randomization = state.prop.randomization.fixR(32,33,34,35,36,37,38,39);

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
            for (GameStep step : session.playGame(state, session.mcts.get(0), NUMBER_OF_NODES_PER_TURN).steps()) {
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
            session.playGames(state, NUMBER_OF_GAMES_TO_PLAY, NUMBER_OF_NODES_PER_TURN, !TEST_TRAINING_AGENT);
        }

        if (GENERATE_TRAINING_GAMES) {
            session.setTrainingDataLogFile("training_data.txt");
            session.SLOW_TRAINING_WINDOW = SLOW_TRAINING_WINDOW;
            session.POLICY_CAP_ON = false;
            session.TRAINING_WITH_LINE = TRAINING_WITH_LINE;
            long start = System.currentTimeMillis();
            state.prop.randomization = new GameStateRandomization.EnemyRandomization(CURRICULUM_TRAINING_ON).doAfter(state.prop.randomization);
            if (state.prop.potions != null) {
                state.prop.preBattleRandomization = new GameStateRandomization.PotionsUtilityRandomization(state.prop.potions, POTION_STEPS).fixR(1).doAfter(state.prop.preBattleRandomization);
            }
            var games = session.playTrainingGames(state, 600, 100);
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
                if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                    for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length; action++) {
                        stream.writeFloat(-1);
                    }
                    if (state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null) {
                        for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()].length; action++) {
                            if (state.terminal_action >= 0) {
                                if (state.terminal_action == action) {
                                    stream.writeFloat(1);
                                } else {
                                    stream.writeInt(0);
                                }
                            } else if (state.isActionLegal(action)) {
                                if (idx < state.getLegalActions().length && state.getLegalActions()[idx] == action) {
                                    stream.writeFloat((float) (((double) state.n[idx++]) / state.total_n));
                                } else {
                                    Integer.parseInt(null);
                                }
                            } else {
                                stream.writeFloat(-1);
                            }
                        }
                    }
                    if (state.prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()] != null) {
                        for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()].length; action++) {
                            stream.writeFloat(-1);
                        }
                    }
                } else if (state.actionCtx == GameActionCtx.SELECT_SCENARIO) {
                    for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length; action++) {
                        stream.writeFloat(-1);
                    }
                    if (state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null) {
                        for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()].length; action++) {
                            stream.writeFloat(-1);
                        }
                    }
                    if (state.prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()] != null) {
                        for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()].length; action++) {
                            if (state.isActionLegal(action)) {
                                if (idx < state.getLegalActions().length && state.getLegalActions()[idx] == action) {
                                    stream.writeFloat((float) (((double) state.n[idx++]) / state.total_n));
                                } else {
                                    Integer.parseInt(null);
                                }
                            } else {
                                stream.writeFloat(-1);
                            }
                        }
                    }
                } else {
                    for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()].length; action++) {
                        if (state.terminal_action >= 0) {
                            if (state.terminal_action == action) {
                                stream.writeFloat(1);
                            } else {
                                stream.writeInt(0);
                            }
                        } else if (state.isActionLegal(action)) {
                            if (idx < state.getLegalActions().length && state.getLegalActions()[idx] == action) {
                                stream.writeFloat((float) (((double) state.n[idx++]) / state.total_n));
                            } else {
                                Integer.parseInt(null);
                            }
                        } else {
                            stream.writeFloat(-1);
                        }
                    }
                    if (state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null) {
                        for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()].length; action++) {
                            stream.writeFloat(-1);
                        }
                    }
                    if (state.prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()] != null) {
                        for (int action = 0; action < state.prop.actionsByCtx[GameActionCtx.SELECT_SCENARIO.ordinal()].length; action++) {
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
