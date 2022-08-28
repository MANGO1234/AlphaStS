package com.alphaStS;

import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyCity;
import com.alphaStS.enemy.EnemyEncounter;
import com.alphaStS.player.Player;

import java.util.List;

public class TestStates {
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
        EnemyEncounter.addSlimeBossFight(builder);
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

    public static GameState TestState4() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.UppercutP(), 1);
        builder.addCard(new Card.Anger(), 1);
        builder.addCard(new Card.Inflame(), 1);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        builder.addCard(new Card.Combust(), 1);
        builder.addCard(new Card.IronWave(), 1);
        builder.addCard(new Card.SpotWeakness(), 1);
        builder.addCard(new Card.BattleTrance(), 1);
        builder.addCard(new Card.PowerThrough(), 1);
        //        builder.addCard(new Card.Pummel(), 1);
        builder.addEnemy(new Enemy.Lagavulin().markAsBurningElite());
        builder.addEnemy(new Enemy.GremlinNob().markAsBurningElite());
        builder.addRelic(new Relic.BagOfPreparation());
        GameStateRandomization randomization = new GameStateRandomization.EnemyEncounterRandomization(builder.getEnemies(), new int[] {0}, new int[] {1});
        randomization = new GameStateRandomization.BurningEliteRandomization().doAfter(randomization);
        //        var startOfGameScenarios = new GameStateRandomization.CardCountRandomization(List.of(
        //                List.of(),
        //                List.of(new CardCount(new Card.Pummel(), 1)),
        //                List.of(new CardCount(new Card.PowerThrough(), 1))
        //        ));
        //        randomization = randomization.doAfter(startOfGameScenarios);
        builder.setRandomization(randomization);
        //        builder.setPreBattleScenarios(startOfGameScenarios);
        builder.addPotion(new Potion.LiquidMemory());
        builder.setPlayer(new Player(43, 75));
        return new GameState(builder);
    }

    public static GameState TestState5() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new CardColorless.Bite(), 5);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.UppercutP(), 1);
        builder.addCard(new Card.Anger(), 1);
        builder.addCard(new Card.InflameP(), 1);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        builder.addCard(new Card.Combust(), 1);
        builder.addCard(new Card.IronWave(), 1);
        builder.addCard(new Card.SpotWeakness(), 1);
        builder.addCard(new Card.BattleTrance(), 1);
        builder.addCard(new Card.PowerThrough(), 1);
        builder.addCard(new Card.PommelStrike(), 1);
        builder.addCard(new Card.Corruption(), 1);
        builder.addCard(new Card.Havoc(), 1);
        builder.addCard(new Card.FlameBarrier(), 1);
        //        builder.addCard(new Card.Impervious(), 1);
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.RedMask());
        EnemyEncounter.addGremlinLeaderFight(builder);
        builder.addPotion(new Potion.FirePotion());
        builder.addPotion(new Potion.DistilledChaos());
        builder.setPlayer(new Player(33, 60));
        return new GameState(builder);
    }

    public static GameState TestState6() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new CardColorless.Bite(), 5);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.UppercutP(), 1);
        builder.addCard(new Card.Anger(), 1);
        builder.addCard(new Card.InflameP(), 1);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        builder.addCard(new Card.Combust(), 1);
        builder.addCard(new Card.CombustP(), 0);
        builder.addCard(new Card.IronWave(), 1);
        builder.addCard(new Card.SpotWeakness(), 1);
        //        builder.addCard(new Card.SpotWeaknessP(), 1);
        builder.addCard(new Card.BattleTrance(), 1);
        builder.addCard(new Card.BattleTranceP(), 0);
        builder.addCard(new Card.PowerThrough(), 1);
        builder.addCard(new Card.PommelStrike(), 1);
        builder.addCard(new Card.PommelStrikeP(), 0);
        builder.addCard(new Card.Corruption(), 1);
        //        builder.addCard(new Card.CorruptionP(), 1);
        builder.addCard(new Card.Havoc(), 1);
        //        builder.addCard(new Card.HavocP(), 1);
        builder.addCard(new Card.FlameBarrier(), 1);
        builder.addCard(new Card.SeeingRedP(), 1);
        //        builder.addCard(new Card.Impervious(), 1);
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.RedMask());
        builder.addEnemy(new EnemyCity.BookOfStabbing());
        EnemyEncounter.addSlaversEliteFight(builder);
        GameStateRandomization randomization = new GameStateRandomization.EnemyEncounterRandomization(builder.getEnemies(), new int[] {0}, new int[] {1, 2, 3});
        var randomization2 = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new Card.PommelStrikeP(), 1),
                        new CardCount(new Card.BattleTrance(), 1),
                        new CardCount(new Card.Combust(), 1)),
                List.of(new CardCount(new Card.PommelStrike(), 1),
                        new CardCount(new Card.BattleTranceP(), 1),
                        new CardCount(new Card.Combust(), 1)),
                List.of(new CardCount(new Card.PommelStrike(), 1),
                        new CardCount(new Card.BattleTrance(), 1),
                        new CardCount(new Card.CombustP(), 1))
        ));
        randomization = randomization.doAfter(randomization2);
        builder.setRandomization(randomization);
        //        builder.addEnemy(new Enemy.TheGuardian());
        //        builder.addEnemy(new EnemyCity.Byrd());
        //        builder.addEnemy(new EnemyCity.Byrd());
        //        builder.addEnemy(new EnemyCity.Byrd());
        //        builder.addEnemy(new EnemyCity.SphericGuardian());
        //        builder.addEnemy(new EnemyCity.Pointy());
        //        builder.addEnemy(new EnemyCity.Romeo());
        //        builder.addEnemy(new EnemyCity.Bear());
        //        builder.setRandomization(startOfGameScenarios);
        //        builder.setPreBattleScenarios(startOf GameScenarios);
        //        builder.addPotion(new Potion.LiquidMemory());
        //        builder.setPlayer(new Player(75, 87));
        builder.addPotion(new Potion.DistilledChaos());
        builder.setPlayer(new Player(43, 60));
        return new GameState(builder);
    }

    public static GameState TestState7() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new CardColorless.Bite(), 5);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.UppercutP(), 1);
        builder.addCard(new Card.Anger(), 1);
        builder.addCard(new Card.InflameP(), 1);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        builder.addCard(new Card.Combust(), 1);
        builder.addCard(new Card.IronWave(), 1);
        builder.addCard(new Card.SpotWeakness(), 2);
        builder.addCard(new Card.BattleTranceP(), 1);
        builder.addCard(new Card.PowerThrough(), 1);
        builder.addCard(new Card.PommelStrikeP(), 1);
        builder.addCard(new Card.Corruption(), 1);
        builder.addCard(new Card.Havoc(), 1);
        builder.addCard(new Card.FlameBarrier(), 1);
        builder.addCard(new Card.SeeingRedP(), 1);
        builder.addCard(new Card.Disarm(), 1);
        builder.addCard(new Card.Impervious(), 1);
        builder.addCard(new Card.Shockwave(), 1);
        builder.addCard(new CardColorless.RitualDaggerP(15), 1);
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.RedMask());
        builder.addRelic(new Relic.AncientTeaSet());
        builder.addRelic(new Relic.HornCleat());
        builder.addRelic(new Relic.BurningBlood());
        builder.addRelic(new Relic.BottledLightning(new Card.BattleTranceP()));
        //        builder.addEnemy(new EnemyCity.BookOfStabbing());
        builder.addEnemy(new EnemyCity.TheChamp());
        builder.addPotion(new Potion.DistilledChaos());
        builder.addPotion(new Potion.EnergyPotion());
        builder.setPlayer(new Player(54, 60));
        return new GameState(builder);
    }

    public static GameState TestState8() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Strike(), 3);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.BattleTrance(), 1);
        builder.addCard(new Card.BodySlam(), 1);
        builder.addCard(new Card.Anger(), 1);
        builder.addCard(new Card.TrueGritP(), 1);
        builder.addCard(new Card.Rage(), 1);
        builder.addCard(new Card.ShrugItOff(), 1);
        builder.addCard(new Card.BurningPact(), 1);
        builder.addCard(new Card.Reaper(), 1);
        builder.addCard(new Card.DemonForm(), 1);
        builder.addEnemy(new Enemy.TheGuardian());
        builder.addRelic(new Relic.Anchor());
        builder.addRelic(new Relic.Vajira());
        builder.addRelic(new Relic.RedSkull());
        builder.addRelic(new Relic.BagOfMarbles());
        builder.addRelic(new Relic.BurningBlood());
        builder.addPotion(new Potion.DexterityPotion());
        builder.addPotion(new Potion.BloodPotion());
        builder.setPlayer(new Player(40, 68));
        return new GameState(builder);
    }

    public static GameState TestState9() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new CardColorless.Bite(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.UppercutP(), 1);
        builder.addCard(new Card.Anger(), 1);
        builder.addCard(new Card.InflameP(), 1);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        builder.addCard(new Card.Combust(), 1);
        builder.addCard(new Card.IronWave(), 1);
        builder.addCard(new Card.SpotWeakness(), 2);
        builder.addCard(new Card.BattleTranceP(), 1);
        builder.addCard(new Card.PowerThrough(), 1);
        builder.addCard(new Card.PommelStrikeP(), 1);
        builder.addCard(new Card.Corruption(), 1);
        builder.addCard(new Card.Havoc(), 1);
        builder.addCard(new Card.FlameBarrier(), 1);
        builder.addCard(new Card.SeeingRedP(), 1);
        builder.addCard(new Card.Disarm(), 1);
        builder.addCard(new Card.Impervious(), 1);
        builder.addCard(new Card.Shockwave(), 1);
        builder.addCard(new CardColorless.RitualDaggerP(30), 1);
        builder.addCard(new Card.BrutalityP(), 1);
        builder.addCard(new Card.ArmanentP(), 1);
        builder.addCard(new Card.Shockwave(), 1);
        builder.addCard(new Card.SwordBoomerang(), 1);
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.RedMask());
        builder.addRelic(new Relic.AncientTeaSet());
        builder.addRelic(new Relic.HornCleat());
        builder.addRelic(new Relic.BurningBlood());
        builder.addRelic(new Relic.BottledLightning(new Card.BattleTranceP()));
        builder.addRelic(new Relic.Shuriken());
        builder.addRelic(new Relic.Vajira());
        builder.addRelic(new Relic.OddlySmoothStone());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.PaperPhrog());
        builder.addRelic(new Relic.ChampionBelt());
        builder.addRelic(new Relic.SacredBark());
        EnemyEncounter.addAwakenedOneFight(builder);
        builder.addPotion(new Potion.LiquidMemory());
        //        builder.addPotion(new Potion.BlessingOfTheForge());
        builder.setPlayer(new Player(49, 60));
        return new GameState(builder);
    }

    public static GameState TestState10() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.ArmanentP(), 1);
        builder.addCard(new Card.Exhume(), 1);
        builder.addCard(new Card.PowerThrough(), 1);
        builder.addCard(new Card.Disarm(), 1);
        builder.addCard(new Card.WildStrike(), 2);
        builder.addCard(new Card.SecondWind(), 1);
        builder.addCard(new Card.Inflame(), 1);
        builder.addEnemy(new Enemy.TheGuardian());
        builder.addRelic(new Relic.ArtOfWar());
        //        builder.addPotion(new Potion.WeakPotion());
        //        builder.addPotion(new Potion.DuplicationPotion());
        builder.setPlayer(new Player(44, 85));
        return new GameState(builder);
    }

    public static GameState TestState12() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.Anger(), 1);
        builder.addCard(new Card.AngerP(), 0);
        builder.addCard(new Card.Thunderclap(), 1);
        builder.addCard(new Card.Uppercut(), 1);
        builder.addCard(new Card.UppercutP(), 0);
        builder.addCard(new CardColorless.Finess(), 1);
        builder.addCard(new Card.IronWave(), 0);
        builder.addRelic(new Relic.Vajira());
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BEAM));
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
        builder.addEnemy(new Enemy.GremlinNob());
        builder.addEnemy(new Enemy.Lagavulin());
        GameStateRandomization randomization = new GameStateRandomization.EnemyEncounterRandomization(builder.getEnemies(),
                new int[] { 0, 1, 2 },
                new int[] { 3 },
                new int[] { 4 }
        );
        GameStateRandomization scenarios = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardColorless.Finess(), 1)),
                List.of(new CardCount(new Card.IronWave(), 1))
        ));
        scenarios = scenarios.doAfter(new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new Card.Uppercut(), 1), new CardCount(new Card.Anger(), 1)),
                List.of(new CardCount(new Card.Uppercut(), 1), new CardCount(new Card.AngerP(), 1)),
                List.of(new CardCount(new Card.UppercutP(), 1), new CardCount(new Card.Anger(), 1))
        )).join(new GameStateRandomization.SimpleCustomRandomization(List.of(
                (state) -> state.getPlayerForWrite().setOrigHealth(57),
                (state) -> state.getPlayerForWrite().setOrigHealth(40),
                (state) -> state.getPlayerForWrite().setOrigHealth(40)
        ))).setDescriptions(
                "Rest",
                "Upgrade Anger",
                "Upgrade Uppercut"
        )).union( 6.0 / 7, new GameStateRandomization.SimpleCustomRandomization(List.of(
                (state) -> {
                    state.setCardCountInDeck(state.prop.findCardIndex(new Card.Strike()), 5);
                    state.getPlayerForWrite().setOrigHealth(57);
                }
        )));
        builder.setRandomization(randomization);
        builder.setPreBattleScenarios(scenarios);
        builder.setPlayer(new Player(40, 57));
        return new GameState(builder);
    }

    public static GameState TestState13() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.Anger(), 1);
        builder.addCard(new Card.Thunderclap(), 1);
        builder.addCard(new Card.UppercutP(), 1);
        builder.addCard(new Card.IronWave(), 1);
        builder.addCard(new Card.SpotWeakness(), 1);
        builder.addCard(new Card.PowerThrough(), 1);
        builder.addCard(new Card.ArmanentP(), 1);
        builder.addCard(new Card.BattleTrance(), 1);
        builder.addRelic(new Relic.Vajira());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.Nunchaku(2));
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BOLT).markAsBurningElite());
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BEAM).markAsBurningElite());
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BOLT).markAsBurningElite());
        //        builder.addEnemy(new Enemy.GremlinNob());
        builder.addEnemy(new Enemy.Lagavulin().markAsBurningElite());
        GameStateRandomization randomization = new GameStateRandomization.EnemyEncounterRandomization(builder.getEnemies(),
                new int[] { 0, 1, 2 },
                new int[] { 3 }
                //                new int[] { 4 }
        );
        randomization = new GameStateRandomization.BurningEliteRandomization().doAfter(randomization);
        builder.setRandomization(randomization);
        builder.addPotion(new Potion.DrawPotion());
        builder.addPotion(new Potion.LiquidMemory());
        builder.setPlayer(new Player(38, 38));
        return new GameState(builder);
    }

    public static GameState TestState14() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.Anger(), 1);
        builder.addCard(new Card.Thunderclap(), 1);
        builder.addCard(new Card.UppercutP(), 1);
        builder.addCard(new Card.IronWave(), 1);
        builder.addCard(new Card.SpotWeakness(), 1);
        builder.addCard(new Card.PowerThrough(), 1);
        builder.addCard(new Card.ArmanentP(), 1);
        builder.addCard(new Card.BattleTrance(), 1);
        builder.addCard(new Card.SwordBoomerang(), 1);
        builder.addCard(new Card.Disarm(), 1);
        builder.addCard(new Card.DisarmP(), 0);
        builder.addCard(new Card.PowerThroughP(), 0);
        builder.addRelic(new Relic.Vajira());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.Nunchaku(8));
        builder.addEnemy(new Enemy.Hexaghost());
        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new Card.Disarm(), 1), new CardCount(new Card.PowerThroughP(), 1)),
                List.of(new CardCount(new Card.DisarmP(), 1), new CardCount(new Card.PowerThrough(), 1))
        ));
        builder.setRandomization(randomization);
        builder.addPotion(new Potion.LiquidMemory());
        builder.setPlayer(new Player(35, 35));
        return new GameState(builder);
    }

    public static GameState TestState15() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.Anger(), 1);
        builder.addCard(new Card.Thunderclap(), 1);
        builder.addCard(new Card.UppercutP(), 1);
        builder.addCard(new Card.IronWave(), 1);
        builder.addCard(new Card.SpotWeakness(), 1);
        builder.addCard(new Card.ArmanentP(), 1);
        builder.addCard(new Card.BattleTrance(), 2);
        builder.addCard(new Card.SwordBoomerang(), 2);
        builder.addCard(new Card.Disarm(), 1);
        builder.addCard(new Card.PowerThroughP(), 1);
        builder.addCard(new Card.Feed(), 1);
        builder.addCard(new Card.FiendFire(), 1);
        builder.addCard(new Card.SwordBoomerang(), 1);
        builder.addCard(new Card.Sentinel(), 1);
        builder.addCard(new Card.PommelStrike(), 1);
        builder.addCard(new Card.SeverSoul(), 1);
        builder.addCard(new Card.DemonForm(), 1);
        builder.addCard(new Card.Evolve(), 1);
        builder.addCard(new Card.Havoc(), 0);
        builder.addRelic(new Relic.Vajira());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.Nunchaku(7));
        builder.addRelic(new Relic.GremlinHorn());
        builder.addEnemy(new EnemyCity.Byrd());
        builder.addEnemy(new EnemyCity.Byrd());
        builder.addEnemy(new EnemyCity.Byrd());
        //        builder.addEnemy(new EnemyCity.BookOfStabbing());
        //        EnemyEncounter.addSlaversEliteFight(builder);
        //        EnemyEncounter.addGremlinLeaderFight(builder);
        //        GameStateRandomization randomization = new GameStateRandomization.EnemyEncounterRandomization(builder.getEnemies(), new int[] {0}, new int[] {1, 2, 3}, new int[] {4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
        //        randomization = randomization.followByIf(2, builder.getRandomization().collapse("Random Gremlins"));
        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(),
                List.of(new CardCount(new Card.Havoc(), 1))
        ));
        builder.setRandomization(randomization);
        //        builder.addPotion(new Potion.LiquidMemory());
        builder.setPlayer(new Player(66, 70));
        return new GameState(builder);
    }

    public static GameState TestState16() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.IronWave(), 1);
        builder.addCard(new Card.ShrugItOff(), 1);
        builder.addCard(new Card.Cleave(), 1);
        builder.addCard(new CardColorless.ApotheosisP(), 1);
        builder.addCard(new Card.PommelStrikeP(), 1);
        builder.addCard(new Card.Havoc(), 1);
        builder.addCard(new Card.BludgeonP(), 0);
        builder.addRelic(new Relic.BronzeScales());
        builder.addRelic(new Relic.Calipers());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.GremlinHorn());
        builder.addRelic(new Relic.MeatOnTheBone());
        EnemyEncounter.addSlimeBossFight(builder);
        var randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new Card.BashP(), 1)),
                List.of(new CardCount(new Card.Havoc(), 1), new CardCount(new Card.BashP(), 1)),
                List.of(new CardCount(new Card.Havoc(), 1), new CardCount(new Card.BludgeonP(), 1), new CardCount(new Card.Bash(), 1))
        ));
        builder.setRandomization(randomization);
        builder.addPotion(new Potion.DistilledChaos());
        builder.addPotion(new Potion.StrengthPotion());
        builder.setPlayer(new Player(67, 73));
        return new GameState(builder);
    }

    public static GameState TestState17() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.Strike(), 5);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.Carnage(), 1);
        builder.addCard(new Card.Armanent(), 1);
        builder.addCard(new Card.Metallicize(), 1);
        builder.addCard(new Card.Decay(), 1);
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BEAM));
        builder.addEnemy(new Enemy.Sentry(45, Enemy.Sentry.BOLT));
        builder.addEnemy(new Enemy.GremlinNob());
        builder.addEnemy(new Enemy.Lagavulin());
        GameStateRandomization randomization = new GameStateRandomization.EnemyEncounterRandomization(builder.getEnemies(),
                new int[] { 0, 1, 2 },
                new int[] { 3 },
                new int[] { 4 }
        );
        builder.setRandomization(randomization);
//        builder.addPotion(new Potion.SkillPotion());
        builder.setPlayer(new Player(62, 62));
        return new GameState(builder);
    }
}
