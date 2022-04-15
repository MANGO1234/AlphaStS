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
        var randomization = new GameStateRandomization() {
            @Override public int randomize(GameState state) {
                int r = state.prop.random.nextInt(5);
                randomize(state, r);
                return r;
            }

            @Override public void reset(GameState state) {
                state.setCardCountInDeck(state.prop.findCardIndex(new Card.Bash()), 1);
                state.setCardCountInDeck(state.prop.findCardIndex(new Card.BashP()), 0);
                state.setCardCountInDeck(state.prop.findCardIndex(new Card.Strike()), 5);
                state.setCardCountInDeck(state.prop.findCardIndex(new Card.StrikeP()), 0);
                state.setCardCountInDeck(state.prop.findCardIndex(new Card.Defend()), 4);
                state.setCardCountInDeck(state.prop.findCardIndex(new Card.DefendP()), 0);
            }

            @Override public void randomize(GameState state, int r) {
                reset(state);
                if (r == 1) {
                    state.setCardCountInDeck(state.prop.findCardIndex(new Card.Strike()), 4);
                    state.setCardCountInDeck(state.prop.findCardIndex(new Card.StrikeP()), 1);
                } else if (r == 2) {
                    state.setCardCountInDeck(state.prop.findCardIndex(new Card.Strike()), 3);
                    state.setCardCountInDeck(state.prop.findCardIndex(new Card.StrikeP()), 2);
                } else if (r == 3) {
                    state.setCardCountInDeck(state.prop.findCardIndex(new Card.Bash()), 0);
                    state.setCardCountInDeck(state.prop.findCardIndex(new Card.BashP()), 1);
                } else if (r == 4) {
                    state.setCardCountInDeck(state.prop.findCardIndex(new Card.Defend()), 0);
                    state.setCardCountInDeck(state.prop.findCardIndex(new Card.DefendP()), 4);
                }
            }

            @Override public Map<Integer, Info> listRandomizations(GameState state) {
                var map = new HashMap<Integer, Info>();
                map.put(0, new Info(1f / 5,"No Upgrade"));
                map.put(1, new Info(1f / 5,"One Strike Upgrade"));
                map.put(2, new Info(1f / 5,"Two Strikes Upgrade"));
                map.put(3, new Info(1f / 5,"Bash Upgrade"));
                map.put(4, new Info(1f / 5, "All Defends Upgraded"));
                return map;
            }
        };
        return new GameState(enemies, new Player(80, 80), cards, relics, null, randomization);
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
        relics.add(new Relic.BagOfPreparation());
        var randomization = new GameStateRandomization() {
            @Override public int randomize(GameState state) {
                int r = state.prop.random.nextInt(3);
                randomize(state, r);
                return r;
            }

            @Override public void reset(GameState state) {
                state.setCardCountInDeck(state.prop.findCardIndex(new Card.Bash()), 1);
                state.setCardCountInDeck(state.prop.findCardIndex(new Card.Defend()), 4);
                state.setCardCountInDeck(state.prop.findCardIndex(new CardSilent.Neutralize()), 0);
                state.setCardCountInDeck(state.prop.findCardIndex(new CardSilent.Survivor()), 0);
                state.setCardCountInDeck(state.prop.findCardIndex(new CardSilent.Acrobatics()), 0);
            }

            @Override public void randomize(GameState state, int r) {
                reset(state);
                if (r == 1 || r == 2) {
                    state.setCardCountInDeck(state.prop.findCardIndex(new Card.Bash()), 0);
                    state.setCardCountInDeck(state.prop.findCardIndex(new Card.Defend()), 5);
                    state.setCardCountInDeck(state.prop.findCardIndex(new CardSilent.Neutralize()), 1);
                    state.setCardCountInDeck(state.prop.findCardIndex(new CardSilent.Survivor()), 1);
                    if (r == 2) {
                        state.setCardCountInDeck(state.prop.findCardIndex(new CardSilent.Acrobatics()), 1);
                    }
                }
            }

            @Override public Map<Integer, Info> listRandomizations(GameState state) {
                var map = new HashMap<Integer, Info>();
                map.put(0, new Info(1f / 3,"Ironclad Starter Deck"));
                map.put(1, new Info(1f / 3,"Silent Starter Deck"));
                map.put(2, new Info(1f / 3,"Silent Starter Deck With Acrobatics"));
                return map;
            }
        };
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
        // https://www.youtube.com/watch?v=1CELexRf5ZE
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
        var randomization = new GameStateRandomization() {
            @Override public int randomize(GameState state) {
//                int r = state.prop.random.nextInt(4);
//                int r = state.prop.random.nextInt(2) + 1;
//                randomize(state, r);
//                return r;
                randomize(state, 0);
                return 0;
            }

            @Override public void reset(GameState state) {
                state.getPlayerForWrite().setOrigHealth(41);
                state.setCardCountInDeck(state.prop.findCardIndex(new Card.Carnage()), 1);
                state.setCardCountInDeck(state.prop.findCardIndex(new Card.CarnageP()), 0);
                state.setCardCountInDeck(state.prop.findCardIndex(new Card.ShrugItOff()), 1);
                state.setCardCountInDeck(state.prop.findCardIndex(new Card.ShrugItOffP()), 0);
                state.setCardCountInDeck(state.prop.findCardIndex(new Card.PowerThrough()), 1);
            }

            @Override public void randomize(GameState state, int r) {
                reset(state);
                if (r == 1) {
                    state.getPlayerForWrite().setOrigHealth(19);
                    state.setCardCountInDeck(state.prop.findCardIndex(new Card.ShrugItOff()), 0);
                    state.setCardCountInDeck(state.prop.findCardIndex(new Card.ShrugItOffP()), 1);
                } else if (r == 2) {
                    state.getPlayerForWrite().setOrigHealth(19);
                    state.setCardCountInDeck(state.prop.findCardIndex(new Card.Carnage()), 0);
                    state.setCardCountInDeck(state.prop.findCardIndex(new Card.CarnageP()), 1);
                } else if (r == 3) {
                    state.getPlayerForWrite().setOrigHealth(41);
                    state.setCardCountInDeck(state.prop.findCardIndex(new Card.PowerThrough()), 0);
                }
            }

            @Override public Map<Integer, Info> listRandomizations(GameState state) {
                var map = new HashMap<Integer, Info>();
                map.put(0, new Info(1f / 4, "Health 41"));
                map.put(1, new Info(1f / 4,"Health 19, Upgrade Shrug It Off"));
                map.put(2, new Info(1f / 4,"Health 19, Upgrade Carnage"));
                map.put(3, new Info(1f / 4, "Health 41, No Power Through"));
                return map;
            }
        };
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
        enemies.add(new Enemy.LargeAcidSlime(75, true));
        enemies.add(new Enemy.MediumSpikeSlime(37, true));
        enemies.add(new Enemy.MediumSpikeSlime(37, true));
        enemies.add(new Enemy.MediumAcidSlime(37, true));
        enemies.add(new Enemy.MediumAcidSlime(37, true));
        var relics = new ArrayList<Relic>();
        relics.add(new Relic.Anchor());
        var player = new Player(47, 75);
        player.gainArtifact(1);
        return new GameState(enemies, player, cards, relics);
    }

    public static void main(String[] args) throws IOException {
        var state = BasicLagavulinState();

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
        int NUMBER_OF_GAMES_TO_PLAY = 5;
        int NUMBER_OF_NODES_PER_TURN = 1000;
        int NUMBER_OF_THREADS = 2;
        int RANDOMIZATION_SCENARIO = -1;
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
            if (args[i].equals("-rand")) {
                RANDOMIZATION_SCENARIO = Integer.parseInt(args[i + 1]);
                i++;
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        String curIterationDir = SAVES_DIR + "/iteration0";
        try {
            JsonNode root = mapper.readTree(new File(SAVES_DIR + "/training.json"));
            int iteration = root.get("iteration").asInt();
            if (SAVES_DIR.startsWith("../")) {
                NUMBER_OF_GAMES_TO_PLAY = 1000;
                NUMBER_OF_NODES_PER_TURN = 5000;
//                RANDOMIZATION_SCENARIO = 1;
            }
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
                if (state.prop.randomization != null) {
                    state.prop.randomization.randomize(state, RANDOMIZATION_SCENARIO);
                }
//                GameSolver solver = new GameSolver(state);
//                solver.solve();
//                session.solver = solver;
            }
            session.training = TEST_TRAINING_AGENT;
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
            long start = System.currentTimeMillis();
            var games = session.playTrainingGames(state, 200, 100, CURRICULUM_TRAINING_ON);
            writeTrainingData(games, curIterationDir +  "/training_data.bin");
            long end = System.currentTimeMillis();
            System.out.println("Time Taken: " + (end - start));
            for (int i = 0; i < session.mcts.size(); i++) {
                var m = session.mcts.get(i);
                System.out.println("Time Taken (By Model " + i + "): " + m.model.time_taken);
                System.out.println("Model " + i + ": size=" + m.model.cache.size() + ", " + m.model.cache_hits + "/" + m.model.calls + " hits (" + (double) m.model.cache_hits / m.model.calls + ")");
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
                                    stream.writeFloat( 0);
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
            for (var info : state.prop.randomization.listRandomizations(state).values()) {
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
