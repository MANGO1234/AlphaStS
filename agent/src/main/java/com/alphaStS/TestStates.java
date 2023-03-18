package com.alphaStS;

import com.alphaStS.enemy.*;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.player.Player;
import com.alphaStS.utils.Tuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public static GameState BasicGremlinNobState2() {
        var state = BasicGremlinNobState();
        state.prop.randomization = state.prop.randomization.fixR(3);
        return state;
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
        builder.addRelic(new Relic.RingOfSerpent());
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
        EnemyEncounter.addSentriesFight(builder);
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
                (state) -> state.getPlayerForWrite().setOrigHealth(41),
                (state) -> state.getPlayerForWrite().setOrigHealth(19),
                (state) -> state.getPlayerForWrite().setOrigHealth(19),
                (state) -> state.getPlayerForWrite().setOrigHealth(41)
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
        EnemyEncounter.addSentriesFight(builder);
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
        EnemyEncounter.addSentriesFight(builder);
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
        //        EnemyEncounter.addByrdsFight(builder);
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
        builder.addCard(new CardColorless.Finesse(), 1);
        builder.addCard(new Card.IronWave(), 0);
        builder.addRelic(new Relic.Vajira());
        EnemyEncounter.addSentriesFight(builder);
        builder.addEnemy(new Enemy.GremlinNob());
        builder.addEnemy(new Enemy.Lagavulin());
        GameStateRandomization randomization = new GameStateRandomization.EnemyEncounterRandomization(builder.getEnemies(),
                new int[] { 0, 1, 2 },
                new int[] { 3 },
                new int[] { 4 }
        );
        GameStateRandomization scenarios = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardColorless.Finesse(), 1)),
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
        builder.addRelic(new Relic.Nunchaku(2, 2));
        EnemyEncounter.addSentriesFight(builder, true);
        //        builder.addEnemy(new Enemy.GremlinNob());
        builder.addEnemy(new Enemy.Lagavulin().markAsBurningElite());
        GameStateRandomization randomization = new GameStateRandomization.EnemyEncounterRandomization(builder.getEnemies(),
                new int[] { 0, 1, 2 },
                new int[] { 3 }
                //                new int[] { 4 }
        );
        randomization = new GameStateRandomization.BurningEliteRandomization().doAfter(randomization);
        builder.setRandomization(randomization);
        builder.addPotion(new Potion.SwiftPotion());
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
        builder.addRelic(new Relic.Nunchaku(8, 2));
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
        builder.addCard(new Card.Sentinel(), 1);
        builder.addCard(new Card.PommelStrike(), 1);
        builder.addCard(new Card.SeverSoul(), 1);
        builder.addCard(new Card.DemonForm(), 1);
        builder.addCard(new Card.Evolve(), 1);
        builder.addCard(new Card.Havoc(), 0);
        builder.addRelic(new Relic.Vajira());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.Nunchaku(7, 2));
        builder.addRelic(new Relic.GremlinHorn());
        EnemyEncounter.addByrdsFight(builder);
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
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.Anger(), 1);
        builder.addCard(new Card.AngerP(), 1);
        builder.addCard(new Card.Thunderclap(), 1);
        builder.addCard(new Card.UppercutP(), 1);
        builder.addCard(new Card.IronWave(), 1);
        builder.addCard(new Card.SpotWeakness(), 1);
        builder.addCard(new Card.ArmanentP(), 1);
        builder.addCard(new Card.BattleTrance(), 1);
        builder.addCard(new Card.BattleTranceP(), 1);
        builder.addCard(new Card.SwordBoomerang(), 2);
        builder.addCard(new Card.Disarm(), 1);
        builder.addCard(new Card.PowerThrough(), 1);
        builder.addCard(new Card.PowerThroughP(), 1);
        builder.addCard(new Card.Feed(8), 1);
        builder.addCard(new Card.FiendFireP(), 1);
        builder.addCard(new Card.Sentinel(), 1);
        builder.addCard(new Card.PommelStrikeP(), 1);
        builder.addCard(new Card.DemonFormP(), 1);
        builder.addCard(new Card.Evolve(), 1);
        builder.addCard(new Card.ShrugItOff(), 1);
        builder.addRelic(new Relic.Vajira());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.Nunchaku(9, 8));
        builder.addRelic(new Relic.GremlinHorn());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.HappyFlower(0, 8));
        builder.addRelic(new Relic.HornCleat());
        builder.addEnemy(new EnemyCity.TheChamp());
//        builder.addEnemy(new EnemyCity.BookOfStabbing());
//        EnemyEncounter.addSlaversEliteFight(builder);
//        EnemyEncounter.addGremlinLeaderFight2(builder);
//        GameStateRandomization randomization = new GameStateRandomization.EnemyEncounterRandomization(builder.getEnemies(), List.of(
//                List.of(new Tuple<>(0, -1)),
//                List.of(new Tuple<>(0, -1), new Tuple<>(1, -1), new Tuple<>(2, -1)),
//                List.of(new Tuple<>(3, 0), new Tuple<>(4, 0), new Tuple<>(5, 0), new Tuple<>(6, -1))
//        ));
//        randomization = randomization.followByIf(0, builder.getRandomization().collapse("Random Gremlins"));
//        GameStateRandomization randomization2 = new GameStateRandomization.CardCountRandomization(List.of(
//                List.of(),
//                List.of(new CardCount(new Card.HeadbuttP(), 1)),
//                List.of(new CardCount(new Card.TrueGritP(), 1))
//        ));
//        builder.setRandomization(randomization.doAfter(randomization2));
//        builder.addPotion(new Potion.LiquidMemory());
//        builder.addPotion(new Potion.SneckoPotion());
        builder.setPlayer(new Player(97, 101));
        return new GameState(builder);
    }

    public static GameState TestState16b() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.AngerP(), 1);
        builder.addCard(new Card.Thunderclap(), 1);
        builder.addCard(new Card.UppercutP(), 1);
        builder.addCard(new Card.IronWave(), 1);
        builder.addCard(new Card.SpotWeakness(), 1);
        builder.addCard(new Card.ArmanentP(), 1);
        builder.addCard(new Card.BattleTrance(), 1);
        builder.addCard(new Card.BattleTranceP(), 1);
        builder.addCard(new Card.SwordBoomerang(), 2);
        builder.addCard(new Card.Disarm(), 1);
        builder.addCard(new Card.PowerThrough(), 1);
        builder.addCard(new Card.PowerThroughP(), 1);
        builder.addCard(new Card.Feed(), 1);
        builder.addCard(new Card.FiendFireP(), 1);
        builder.addCard(new Card.Sentinel(), 1);
        builder.addCard(new Card.PommelStrikeP(), 2);
        builder.addCard(new Card.DemonFormP(), 1);
        builder.addCard(new Card.Evolve(), 1);
        builder.addCard(new Card.ShrugItOff(), 1);
        builder.addCard(new Card.Offering(), 1);
        builder.addCard(new Card.DarkEmbraceP(), 1);
        builder.addCard(new Card.FeelNoPain(), 2);
        builder.addCard(new Card.ShockwaveP(), 1);
        builder.addCard(new Card.WarcryP(), 2);
        builder.addCard(new Card.Exhume(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        builder.addRelic(new Relic.Vajira());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.Nunchaku(8, 2));
        builder.addRelic(new Relic.GremlinHorn());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.HappyFlower(2, 2));
        builder.addRelic(new Relic.HornCleat());
        builder.addRelic(new Relic.VelvetChoker());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.Anchor());
        builder.addRelic(new Relic.ArtOfWar());
        builder.addRelic(new Relic.ToyOrnithopter());
        builder.addEnemy(new EnemyBeyond.TimeEater());
//        builder.addPotion(new Potion.SneckoPotion());
//        builder.addPotion(new Potion.LiquidMemory());
//        builder.addPotion(new Potion.WeakPotion());
        builder.setPlayer(new Player(141, 145));
        return new GameState(builder);
    }



    public static GameState TestState16c2() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.AngerP(), 1);
        builder.addCard(new Card.Thunderclap(), 1);
        builder.addCard(new Card.UppercutP(), 1);
        builder.addCard(new Card.IronWave(), 1);
        builder.addCard(new Card.SpotWeakness(), 1);
        builder.addCard(new Card.ArmanentP(), 1);
        builder.addCard(new Card.BattleTrance(), 1);
        builder.addCard(new Card.BattleTranceP(), 1);
        builder.addCard(new Card.SwordBoomerang(), 2);
        builder.addCard(new Card.Disarm(), 1);
        builder.addCard(new Card.PowerThrough(), 1);
        builder.addCard(new Card.PowerThroughP(), 1);
        builder.addCard(new Card.Feed(), 1);
        builder.addCard(new Card.FiendFireP(), 1);
        builder.addCard(new Card.Sentinel(), 1);
        builder.addCard(new Card.PommelStrikeP(), 2);
        builder.addCard(new Card.DemonFormP(), 1);
        builder.addCard(new Card.Evolve(), 1);
        builder.addCard(new Card.ShrugItOff(), 1);
        builder.addCard(new Card.Offering(), 1);
        builder.addCard(new Card.DarkEmbraceP(), 1);
        builder.addCard(new Card.FeelNoPain(), 2);
        builder.addCard(new Card.ShockwaveP(), 1);
        builder.addCard(new Card.WarcryP(), 2);
        builder.addCard(new Card.Exhume(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        builder.addRelic(new Relic.Vajira());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.Nunchaku(9, 2));
        builder.addRelic(new Relic.GremlinHorn());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.HappyFlower(2, 2));
        builder.addRelic(new Relic.HornCleat());
        builder.addRelic(new Relic.VelvetChoker());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.Anchor());
        builder.addRelic(new Relic.ArtOfWar());
        builder.addRelic(new Relic.ToyOrnithopter());
        EnemyEncounter.addAwakenedOneFight(builder);
        //        builder.addPotion(new Potion.SneckoPotion());
        //        builder.addPotion(new Potion.LiquidMemory());
        //        builder.addPotion(new Potion.WeakPotion());
        builder.setPlayer(new Player(126, 134));
        return new GameState(builder);
    }

    public static GameState TestState16c() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.AngerP(), 1);
        builder.addCard(new Card.Thunderclap(), 1);
        builder.addCard(new Card.UppercutP(), 1);
        builder.addCard(new Card.IronWave(), 1);
        builder.addCard(new Card.SpotWeakness(), 1);
        builder.addCard(new Card.ArmanentP(), 1);
        builder.addCard(new Card.BattleTrance(), 2);
        builder.addCard(new Card.BattleTranceP(), 1);
        builder.addCard(new Card.SwordBoomerang(), 2);
        builder.addCard(new Card.Disarm(), 1);
        builder.addCard(new Card.PowerThrough(), 1);
        builder.addCard(new Card.PowerThroughP(), 1);
        builder.addCard(new Card.Feed(0), 1);
        builder.addCard(new Card.FiendFireP(), 1);
        builder.addCard(new Card.PommelStrikeP(), 2);
        builder.addCard(new Card.DemonFormP(), 1);
        builder.addCard(new Card.EvolveP(), 1);
        builder.addCard(new Card.ShrugItOff(), 2);
        builder.addCard(new Card.Offering(), 1);
        builder.addCard(new Card.DarkEmbraceP(), 1);
        builder.addCard(new Card.FeelNoPain(), 2);
        builder.addCard(new Card.ShockwaveP(), 1);
        builder.addCard(new Card.WarcryP(), 2);
        builder.addCard(new Card.Exhume(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        builder.addRelic(new Relic.Vajira());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.Nunchaku(9, 0));
        builder.addRelic(new Relic.GremlinHorn());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.HappyFlower(1, 0));
        builder.addRelic(new Relic.HornCleat());
        builder.addRelic(new Relic.VelvetChoker());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.Anchor());
        builder.addRelic(new Relic.ArtOfWar());
        builder.addRelic(new Relic.ToyOrnithopter());
        builder.addRelic(new Relic.InkBottle());
        builder.addEnemy(new EnemyEnding.CorruptHeart());
        builder.addPotion(new Potion.SneckoPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.LiquidMemory().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.WeakPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.RegenerationPotion().setBasePenaltyRatio(100));
        builder.setPlayer(new Player(123, 144));
        return new GameState(builder);
    }

    public static GameState TestState17() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Bash(), 1);
        builder.addCard(new Card.Strike(), 5);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new Card.Carnage(), 1);
        builder.addCard(new Card.ArmanentP(), 1);
        builder.addCard(new Card.Metallicize(), 1);
        builder.addCard(new Card.IronWave(), 1);
        builder.addCard(new Card.ShrugItOff(), 1);
        builder.addCard(new Card.BattleTrance(), 1);
        builder.addCard(new Card.ImmolateP(), 1);
        builder.addCard(new Card.Anger(), 0);
        builder.addCard(new Card.Headbutt(), 0);
        builder.addCard(new Card.Evolve(), 0);
        EnemyEncounter.addSlimeBossFight(builder);
        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new Card.Anger(), 1), new CardCount(new Card.Headbutt(), 1), new CardCount(new Card.Evolve(), 1)),
                List.of(new CardCount(new Card.Anger(), 1), new CardCount(new Card.Headbutt(), 1), new CardCount(new Card.Evolve(), 0)),
                List.of(new CardCount(new Card.Anger(), 1), new CardCount(new Card.Headbutt(), 0), new CardCount(new Card.Evolve(), 1)),
                List.of(new CardCount(new Card.Anger(), 0), new CardCount(new Card.Headbutt(), 1), new CardCount(new Card.Evolve(), 1)),
                List.of(new CardCount(new Card.Anger(), 0), new CardCount(new Card.Headbutt(), 0), new CardCount(new Card.Evolve(), 0)),
                List.of(new CardCount(new Card.Anger(), 0), new CardCount(new Card.Headbutt(), 0), new CardCount(new Card.Evolve(), 1))
                ));
        builder.setRandomization(randomization);
        builder.addPotion(new Potion.BloodPotion(16));
        builder.addPotion(new Potion.SpeedPotion());
        builder.addRelic(new Relic.PaperPhrog());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.OddlySmoothStone());
        builder.setPlayer(new Player(26, 42));
        return new GameState(builder);
    }

    public static GameState TestStateDefect() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.Strike(), 2);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.SunderP(), 1);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.BootSequenceP(), 1);
        builder.addCard(new CardDefect.EchoForm(), 2);
        builder.addCard(new CardDefect.Sunder(), 1);
        builder.addCard(new CardDefect.Consume(), 1);
        builder.addCard(new CardDefect.SelfRepair(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
//        builder.addEnemy(new EnemyCity.BookOfStabbing());
//        EnemyEncounter.addSlaversEliteFight(builder);
        EnemyEncounter.addGremlinLeaderFight2(builder);
//        GameStateRandomization randomization = new GameStateRandomization.EnemyEncounterRandomization(builder.getEnemies(), List.of(
//                List.of(new Tuple<>(0, -1)),
//                List.of(new Tuple<>(1, -1), new Tuple<>(2, -1), new Tuple<>(3, -1)),
//                List.of(new Tuple<>(4, 0), new Tuple<>(5, 0), new Tuple<>(6, 0), new Tuple<>(7, -1))
//        ));
//        randomization = randomization.followByIf(2, builder.getRandomization().collapse("Random Gremlins"));
        builder.setRandomization(builder.getRandomization().collapse("Random Gremlins"));
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.BagOfMarbles());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.RunicDome());
        builder.addPotion(new Potion.DistilledChaos());
        builder.addPotion(new Potion.GamblersBrew());
        builder.setPlayer(new Player(60, 74));
        return new GameState(builder);
    }

    public static GameState TestStateDefect1p1() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.Strike(), 2);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.SunderP(), 2);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addCard(new CardDefect.TurboP(), 0);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.BootSequenceP(), 1);
        builder.addCard(new CardDefect.EchoForm(), 2);
        builder.addCard(new CardDefect.Consume(), 1);
        builder.addCard(new CardDefect.ConsumeP(), 0);
        builder.addCard(new CardDefect.EquilibirumP(), 1);
        builder.addCard(new CardDefect.SelfRepair(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
        builder.addCard(new CardDefect.RebootP(), 1);
        builder.addCard(new CardDefect.StreamlineP(), 0);
        builder.addCard(new CardDefect.Darkness(), 1);
        builder.addCard(new CardDefect.DarknessP(), 0);
        builder.addEnemy(new EnemyCity.BookOfStabbing());
//        EnemyEncounter.addSlaversEliteFight(builder);
//        EnemyEncounter.addGremlinLeaderFight2(builder);
//        EnemyEncounter.addBronzeAutomatonFight(builder);
//        GameStateRandomization randomization = new GameStateRandomization.EnemyEncounterRandomization(builder.getEnemies(), List.of(
//            List.of(new Tuple<>(0, -1)),
////            List.of(new Tuple<>(1, -1), new Tuple<>(2, -1), new Tuple<>(3, -1))
////            List.of(new Tuple<>(1, -1), new Tuple<>(2, -1), new Tuple<>(3, -1), new Tuple<>(4, -1))
////            List.of(new Tuple<>(4, 0), new Tuple<>(5, 0), new Tuple<>(6, 0), new Tuple<>(7, -1))
//        ));
//        GameState randomization = randomization.followByIf(1, builder.getRandomization().collapse("Random Gremlins"));
//        randomization = new GameStateRandomization.CardCountRandomization(List.of(
//                List.of(new CardCount(new CardDefect.Reboot(), 0), new CardCount(new CardDefect.RebootP(), 1),
//                        new CardCount(new CardDefect.Darkness(), 0), new CardCount(new CardDefect.DarknessP(), 0)),
//                List.of(new CardCount(new CardDefect.Reboot(), 0), new CardCount(new CardDefect.RebootP(), 1),
//                        new CardCount(new CardDefect.Darkness(), 1), new CardCount(new CardDefect.DarknessP(), 0)),
//                List.of(new CardCount(new CardDefect.Reboot(), 1), new CardCount(new CardDefect.RebootP(), 0),
//                        new CardCount(new CardDefect.Darkness(), 0), new CardCount(new CardDefect.DarknessP(), 1))
//        )).doAfter(randomization);
//        builder.setRandomization(randomization);
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.BagOfMarbles());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.RunicDome());
        builder.addRelic(new Relic.Torii());
        builder.addRelic(new Relic.RunicCapacitor());
        builder.addPotion(new Potion.BlockPotion());
        builder.addPotion(new Potion.BlockPotion());
        builder.addPotion(new Potion.GamblersBrew());
        builder.addPotion(new Potion.WeakPotion());
        builder.setPlayer(new Player(62, 76));
        return new GameState(builder);
    }

    public static GameState TestStateDefect1p2() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.StrikeP(), 2);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.SunderP(), 2);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.BootSequenceP(), 1);
        builder.addCard(new CardDefect.EchoForm(), 2);
        builder.addCard(new CardDefect.ConsumeP(), 1);
        builder.addCard(new CardDefect.EquilibirumP(), 1);
        builder.addCard(new CardDefect.SelfRepair(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
        builder.addCard(new CardDefect.RebootP(), 1);
        builder.addCard(new CardDefect.Darkness(), 1);
        builder.addCard(new CardDefect.Aggregate(), 1);
        builder.addCard(new CardDefect.SeekP(), 1);
        builder.addCard(new CardDefect.Hologram(), 1);
        builder.addCard(new CardDefect.RecursionP(), 1);
        builder.addCard(new CardDefect.CoolheadedP(), 1);
        builder.addCard(new CardDefect.SkimP(), 1);
        builder.addCard(new CardDefect.BeamCellP(), 1);
        builder.addCard(new CardDefect.Blizzard(), 1);
        builder.addCard(new CardColorless.Impatience(), 1);
        builder.addCard(new Card.Parasite(), 1);
        EnemyEncounter.addDonuAndDecaFight(builder);
//        builder.addEnemy(new EnemyBeyond.GiantHead().markAsBurningElite());
//        builder.addEnemy(new EnemyBeyond.Nemesis().markAsBurningElite());
//        EnemyEncounter.addReptomancerFight(builder, true);
//        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
//                List.of(new CardCount(new CardDefect.CoolheadedP(), 1)),
//                List.of(new CardCount(new CardDefect.BarrageP(), 1))
//        )).fixR(0);
//        randomization = new GameStateRandomization.EnemyEncounterRandomization(builder.getEnemies(), List.of(
//                List.of(new Tuple<>(0, -1)),
//                List.of(new Tuple<>(1, -1)),
//                List.of(new Tuple<>(2, -1), new Tuple<>(3, -1), new Tuple<>(4, -1), new Tuple<>(5, -1), new Tuple<>(6, -1))
//        )).doAfter(randomization);
//        builder.setRandomization(new GameStateRandomization.BurningEliteRandomization().doAfter(randomization));
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.BagOfMarbles());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.RunicDome());
        builder.addRelic(new Relic.Torii());
        builder.addRelic(new Relic.RunicCapacitor());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.IncenseBurner(5, Relic.IncenseBurner.SHIELD_AND_SPEAR_REWARD));
        builder.addRelic(new Relic.TheBoot());
        builder.addRelic(new Relic.Shuriken());
        builder.addPotion(new Potion.BlockPotion().setBasePenaltyRatio(90));
        builder.addPotion(new Potion.BlockPotion().setBasePenaltyRatio(90));
        builder.addPotion(new Potion.FocusPotion());
        builder.addPotion(new Potion.GamblersBrew());
        builder.setPlayer(new Player(72, 72));
        return new GameState(builder);
    }

    public static GameState TestStateDefect1p3() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.StrikeP(), 2);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.SunderP(), 2);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.BootSequenceP(), 1);
        builder.addCard(new CardDefect.EchoForm(), 2);
        builder.addCard(new CardDefect.ConsumeP(), 1);
        builder.addCard(new CardDefect.EquilibirumP(), 1);
        builder.addCard(new CardDefect.SelfRepair(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
        builder.addCard(new CardDefect.RebootP(), 1);
        builder.addCard(new CardDefect.Darkness(), 1);
        builder.addCard(new CardDefect.Aggregate(), 1);
        builder.addCard(new CardDefect.Aggregate(), 1);
        builder.addCard(new CardDefect.SeekP(), 1);
        builder.addCard(new CardDefect.HologramP(), 1);
        builder.addCard(new CardDefect.RecursionP(), 1);
        builder.addCard(new CardDefect.CoolheadedP(), 1);
        builder.addCard(new CardDefect.SkimP(), 1);
        builder.addCard(new CardDefect.BeamCellP(), 1);
        builder.addCard(new CardDefect.Blizzard(), 1);
        builder.addCard(new CardColorless.Impatience(), 1);
        builder.addCard(new Card.Parasite(), 1);
        EnemyEncounter.addShieldAndSpearFight(builder);
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.BagOfMarbles());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.RunicDome());
        builder.addRelic(new Relic.Torii());
        builder.addRelic(new Relic.RunicCapacitor());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.IncenseBurner(4, Relic.IncenseBurner.HEART_REWARD));
        builder.addRelic(new Relic.TheBoot());
        builder.addRelic(new Relic.Shuriken());
        builder.addRelic(new Relic.DataDisk());
        builder.addPotion(new Potion.PotionOfCapacity());
        builder.addPotion(new Potion.BlockPotion().setBasePenaltyRatio(90));
        builder.addPotion(new Potion.FocusPotion().setBasePenaltyRatio(70));
        builder.addPotion(new Potion.GamblersBrew().setBasePenaltyRatio(80));
        builder.setPlayer(new Player(72, 72));
        return new GameState(builder);
    }


    public static GameState TestStateDefect1p4() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.StrikeP(), 2);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.SunderP(), 2);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.BootSequenceP(), 1);
        builder.addCard(new CardDefect.EchoForm(), 2);
        builder.addCard(new CardDefect.ConsumeP(), 1);
        builder.addCard(new CardDefect.EquilibirumP(), 1);
        builder.addCard(new CardDefect.SelfRepair(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
        builder.addCard(new CardDefect.RebootP(), 1);
        builder.addCard(new CardDefect.Darkness(), 1);
        builder.addCard(new CardDefect.Aggregate(), 1);
        builder.addCard(new CardDefect.Aggregate(), 1);
        builder.addCard(new CardDefect.SeekP(), 1);
        builder.addCard(new CardDefect.HologramP(), 1);
        builder.addCard(new CardDefect.RecursionP(), 1);
        builder.addCard(new CardDefect.CoolheadedP(), 1);
        builder.addCard(new CardDefect.SkimP(), 1);
        builder.addCard(new CardDefect.BeamCellP(), 1);
        builder.addCard(new CardDefect.Blizzard(), 1);
        builder.addCard(new CardColorless.Impatience(), 1);
        builder.addCard(new Card.Parasite(), 1);
        builder.addEnemy(new EnemyEnding.CorruptHeart());
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.BagOfMarbles());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.RunicDome());
        builder.addRelic(new Relic.Torii());
        builder.addRelic(new Relic.RunicCapacitor());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.IncenseBurner(4, Relic.IncenseBurner.NO_REWARD));
        builder.addRelic(new Relic.TheBoot());
        builder.addRelic(new Relic.Shuriken());
        builder.addRelic(new Relic.DataDisk());
        builder.addRelic(new Relic.BirdFacedUrn());
        builder.addPotion(new Potion.PotionOfCapacity().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.BlockPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.FocusPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.GamblersBrew().setBasePenaltyRatio(100));
        builder.setPlayer(new Player(72, 72));
        return new GameState(builder);
    }

    public static GameState TestStateDefect2() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.Strike(), 3);
        builder.addCard(new Card.StrikeP(), 1);
        builder.addCard(new Card.Defend(), 3);
        builder.addCard(new Card.DefendP(), 1);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardDefect.Zap(), 1);
        builder.addCard(new CardDefect.ZapP(), 0);
        builder.addCard(new CardDefect.DualCastP(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
        builder.addCard(new CardDefect.GlacierP(), 0);
        builder.addCard(new CardDefect.BallLightning(), 2);
        builder.addCard(new CardDefect.Skim(), 2);
        builder.addCard(new CardDefect.MelterP(), 1);
        builder.addCard(new CardDefect.BeamCellP(), 1);
        builder.addCard(new CardDefect.BiasedCognition(), 0);
        builder.addCard(new CardDefect.BiasedCognitionP(),1);
        builder.addCard(new CardDefect.Coolheaded(), 1);
        builder.addCard(new CardDefect.CoolheadedP(), 0);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addEnemy(new Enemy.TheGuardian());
//        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
//                List.of(new CardCount(new CardDefect.BiasedCognition(), 1)),
//                List.of(new CardCount(new CardDefect.BiasedCognition(), 0))
//        ));
        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardDefect.CoolheadedP(), 1), new CardCount(new CardDefect.Coolheaded(), 0),
                        new CardCount(new CardDefect.GlacierP(), 0), new CardCount(new CardDefect.Glacier(), 1),
                        new CardCount(new CardDefect.ZapP(), 0), new CardCount(new CardDefect.Zap(), 1)),
                List.of(new CardCount(new CardDefect.CoolheadedP(), 0), new CardCount(new CardDefect.Coolheaded(), 1),
                        new CardCount(new CardDefect.GlacierP(), 1), new CardCount(new CardDefect.Glacier(), 0),
                        new CardCount(new CardDefect.ZapP(), 0), new CardCount(new CardDefect.Zap(), 1)),
                List.of(new CardCount(new CardDefect.CoolheadedP(), 0), new CardCount(new CardDefect.Coolheaded(), 1),
                        new CardCount(new CardDefect.GlacierP(), 0), new CardCount(new CardDefect.Glacier(), 1),
                        new CardCount(new CardDefect.ZapP(), 1), new CardCount(new CardDefect.Zap(), 0)),
                List.of(new CardCount(new CardDefect.CoolheadedP(), 0), new CardCount(new CardDefect.Coolheaded(), 1),
                        new CardCount(new CardDefect.GlacierP(), 0), new CardCount(new CardDefect.Glacier(), 1),
                        new CardCount(new CardDefect.ZapP(), 0), new CardCount(new CardDefect.Zap(), 1))
        )).join(new GameStateRandomization.SimpleCustomRandomization(List.of(
                (state) -> state.getPlayerForWrite().setOrigHealth(8),
                (state) -> state.getPlayerForWrite().setOrigHealth(8),
                (state) -> state.getPlayerForWrite().setOrigHealth(8),
                (state) -> state.getPlayerForWrite().setOrigHealth(29)
        ))).setDescriptions(
                "Health 8, Upgrade Coolheaded",
                "Health 8, Upgrade Glacier",
                "Health 8, Upgrade Zap",
                "Health 29"
        );
        builder.setRandomization(randomization);
        builder.addPotion(new Potion.SwiftPotion());
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.OddlySmoothStone());
        builder.addRelic(new Relic.DataDisk());
        builder.addRelic(new Relic.Nunchaku(0, 1));
        builder.setPlayer(new Player(8, 71));
        return new GameState(builder);
    }

    public static GameState TestStateDefect3() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 3);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardDefect.Zap(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.BeamCell(), 1);
        builder.addCard(new CardDefect.BootSequence(), 1);
        builder.addCard(new CardDefect.DoomAndGloomP(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
        builder.addEnemy(new Enemy.Lagavulin());
        builder.addPotion(new Potion.FocusPotion());
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.TungstenRod());
        builder.setPlayer(new Player(68, 68));
        return new GameState(builder);
    }

    public static GameState TestStateDefectReddit() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardDefect.Zap(), 1);
        builder.addCard(new CardDefect.DualCastP(), 1);
        builder.addCard(new CardDefect.SunderP(), 1);
        builder.addCard(new CardDefect.Claw(3, 14), 2);
        builder.addCard(new CardDefect.MeteorStrike(), 1);
        builder.addCard(new CardDefect.BallLightning(), 1);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addCard(new CardDefect.Equilibirum(), 1);
        builder.addCard(new CardDefect.SelfRepair(), 1);
        builder.addEnemy(new Enemy.TheGuardian());
        builder.addPotion(new Potion.LiquidMemory());
        builder.addPotion(new Potion.StrengthPotion());
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.GremlinVisage());
        builder.addRelic(new Relic.BagOfMarbles());
        builder.addRelic(new Relic.AncientTeaSet());
        builder.addRelic(new Relic.HappyFlower(2, 2));
        builder.setPlayer(new Player(39, 39));
        builder.setStartOfGameSetup(new GameEventHandler() {
            @Override public void handle(GameState state) {
                state.getDrawOrderForWrite().pushOnTop(state.prop.findCardIndex("TURBO"));
                state.getDrawOrderForWrite().pushOnTop(state.prop.findCardIndex("Claw (3)"));
                state.getDrawOrderForWrite().pushOnTop(state.prop.findCardIndex("Strike"));
                state.getDrawOrderForWrite().pushOnTop(state.prop.findCardIndex("Zap"));
                state.getDrawOrderForWrite().pushOnTop(state.prop.findCardIndex("Ball Lightning"));
            }
        });
        return new GameState(builder);
    }

    public static GameState TestStateReddit() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new Card.Strike(), 5);
        builder.addCard(new Card.Defend(), 5);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardSilent.Survivor(), 1);
        builder.addCard(new CardSilent.Neutralize(), 1);
        builder.addCard(new CardSilent.DaggerSpray(), 1);
        builder.addCard(new CardColorless.Apotheosis(), 0);
        builder.addCard(new CardColorless.HandOfGreed(), 0);
        EnemyEncounter.addSentriesFight(builder);
        builder.addEnemy(new Enemy.GremlinNob());
        builder.addEnemy(new Enemy.Lagavulin());
        GameStateRandomization randomization = new GameStateRandomization.EnemyEncounterRandomization(builder.getEnemies(),
                new int[] { 0, 1, 2 },
                new int[] { 3 },
                new int[] { 4 }
        );
        randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardColorless.HandOfGreed(), 1),
                        new CardCount(new CardColorless.Apotheosis(), 0)),
                List.of(new CardCount(new CardColorless.HandOfGreed(), 0),
                        new CardCount(new CardColorless.Apotheosis(), 1))
        )).doAfter(randomization);
        builder.setRandomization(randomization);
        builder.addRelic(new Relic.RingOfSerpent());
        builder.setPlayer(new Player(63, 63));
        return new GameState(builder);
    }

    public static GameState TestStateSilentTodo() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 5);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardSilent.Survivor(), 1);
        builder.addCard(new CardSilent.NeutralizeP(), 1);
        builder.addCard(new CardSilent.EndlessAgony(), 1);
        builder.addCard(new CardSilent.Backstab(), 1);
        builder.addCard(new CardSilent.Eviscerate(), 1);
        builder.addCard(new CardSilent.WellLaidPlansP(), 1);
        builder.addCard(new CardSilent.AllOutAttackP(), 1);
        builder.addCard(new CardSilent.BurstP(), 1);
        builder.addCard(new CardSilent.EscapePlan(), 1);
        builder.addCard(new CardSilent.Acrobatics(), 2);
        builder.addCard(new CardSilent.TerrorP(), 1);
        builder.addCard(new CardSilent.DaggerSpray(), 1);
        builder.addCard(new CardSilent.PhantasmalKillerP(), 1);
        builder.addCard(new CardSilent.BladeDance(), 1);
        builder.addCard(new CardSilent.DodgeAndRoll(), 1);
        builder.addCard(new CardSilent.CloakAndDagger(), 1);
        builder.addCard(new CardSilent.MalaiseP(), 1);
        builder.addCard(new CardSilent.EnvenomP(), 1);
        builder.addCard(new CardSilent.Footwork(), 1);
        builder.addCard(new CardSilent.FootworkP(), 1);
        builder.addCard(new CardSilent.Deflect(), 1);
        builder.addCard(new CardSilent.Blur(), 0);
        builder.addCard(new CardSilent.CalculatedGamble(), 1);
        builder.addCard(new CardSilent.PiercingWailP(), 1);
        builder.addCard(new CardColorless.MasterOfStrategy(), 1);
        builder.addCard(new CardColorless.ThinkingAhead(), 1);
        builder.addCard(new CardColorless.SecretTechnique(), 1);
        EnemyEncounter.addDonuAndDecaFight(builder);
        var startOfGameScenarios = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardSilent.Deflect(), 0)),
                List.of(new CardCount(new CardSilent.Deflect(), 1)),
                List.of(new CardCount(new CardSilent.Deflect(), 1), new CardCount(new CardSilent.Blur(), 1))
        ));
        builder.setPreBattleScenarios(startOfGameScenarios);
        builder.addPotion(new Potion.SwiftPotion().setBasePenaltyRatio(90));
        builder.addPotion(new Potion.BlockPotion().setBasePenaltyRatio(90));
        builder.addRelic(new Relic.MummifiedHand());
        builder.addRelic(new Relic.RingOfSerpent());
        builder.addRelic(new Relic.Shuriken());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.SlaversCollar());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.SneckoSkull());
        builder.addRelic(new Relic.PaperCrane());
        builder.addRelic(new Relic.Tingsha());
        builder.addRelic(new Relic.ThreadAndNeedle());
        builder.setPlayer(new Player(66, 66));
        return new GameState(builder);
    }

    public static GameState TestStateSilent() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 5);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardSilent.Survivor(), 1);
        builder.addCard(new CardSilent.NeutralizeP(), 1);
        builder.addCard(new CardSilent.EndlessAgony(), 1);
        builder.addCard(new CardSilent.Backstab(), 1);
        builder.addCard(new CardSilent.Eviscerate(), 1);
        builder.addCard(new CardSilent.WellLaidPlansP(), 1);
        builder.addCard(new CardSilent.AllOutAttackP(), 1);
        builder.addCard(new CardSilent.BurstP(), 1);
        builder.addCard(new CardSilent.EscapePlan(), 1);
        builder.addCard(new CardSilent.Acrobatics(), 2);
        builder.addCard(new CardSilent.TerrorP(), 1);
        builder.addCard(new CardSilent.DaggerSpray(), 1);
        builder.addCard(new CardSilent.PhantasmalKillerP(), 1);
        builder.addCard(new CardSilent.BladeDance(), 1);
        builder.addCard(new CardSilent.DodgeAndRoll(), 1);
        builder.addCard(new CardSilent.CloakAndDagger(), 1);
        builder.addCard(new CardSilent.MalaiseP(), 1);
        builder.addCard(new CardSilent.EnvenomP(), 1);
        builder.addCard(new CardSilent.FootworkP(), 2);
        builder.addCard(new CardSilent.Deflect(), 1);
        builder.addCard(new CardSilent.CalculatedGamble(), 1);
        builder.addCard(new CardSilent.PiercingWailP(), 1);
        builder.addCard(new CardSilent.Predator(), 0);
        builder.addCard(new CardColorless.MasterOfStrategy(), 1);
        builder.addCard(new CardColorless.ThinkingAhead(), 1);
        builder.addCard(new CardColorless.SecretTechnique(), 1);
        EnemyEncounter.addShieldAndSpearFight(builder);
        builder.setPotionsScenarios(1);
        var startOfGameScenarios = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardSilent.Predator(), 1), new CardCount(new CardSilent.CloakAndDagger(), 1)),
                List.of(new CardCount(new CardSilent.CloakAndDagger(), 2)),
                List.of(new CardCount(new CardSilent.CloakAndDagger(), 1))
        ));
        builder.setPreBattleScenarios(startOfGameScenarios);
        builder.addPotion(new Potion.SwiftPotion().setBasePenaltyRatio(90));
        builder.addPotion(new Potion.BlockPotion().setBasePenaltyRatio(90));
        builder.addRelic(new Relic.MummifiedHand());
        builder.addRelic(new Relic.RingOfSerpent());
        builder.addRelic(new Relic.Shuriken());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.SlaversCollar());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.SneckoSkull());
        builder.addRelic(new Relic.PaperCrane());
        builder.addRelic(new Relic.Tingsha());
        builder.addRelic(new Relic.ThreadAndNeedle());
        builder.addRelic(new Relic.OrangePellets());
        builder.setPlayer(new Player(70, 70));
        return new GameState(builder);
    }

    public static GameState TestStateSilentHeart() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 5);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardSilent.Survivor(), 1);
        builder.addCard(new CardSilent.NeutralizeP(), 1);
        builder.addCard(new CardSilent.EndlessAgony(), 1);
        builder.addCard(new CardSilent.Backstab(), 1);
        builder.addCard(new CardSilent.Eviscerate(), 1);
        builder.addCard(new CardSilent.WellLaidPlansP(), 1);
        builder.addCard(new CardSilent.AllOutAttackP(), 1);
        builder.addCard(new CardSilent.BurstP(), 1);
        builder.addCard(new CardSilent.EscapePlan(), 1);
        builder.addCard(new CardSilent.Acrobatics(), 2);
        builder.addCard(new CardSilent.TerrorP(), 1);
        builder.addCard(new CardSilent.DaggerSpray(), 1);
        builder.addCard(new CardSilent.PhantasmalKillerP(), 1);
        builder.addCard(new CardSilent.BladeDance(), 1);
        builder.addCard(new CardSilent.DodgeAndRoll(), 1);
        builder.addCard(new CardSilent.CloakAndDagger(), 2);
        builder.addCard(new CardSilent.MalaiseP(), 1);
        builder.addCard(new CardSilent.EnvenomP(), 1);
        builder.addCard(new CardSilent.FootworkP(), 2);
        builder.addCard(new CardSilent.Deflect(), 1);
        builder.addCard(new CardSilent.CalculatedGamble(), 1);
        builder.addCard(new CardSilent.PiercingWailP(), 1);
        builder.addCard(new CardSilent.Predator(), 0);
        builder.addCard(new CardColorless.MasterOfStrategy(), 1);
        builder.addCard(new CardColorless.ThinkingAhead(), 1);
        builder.addCard(new CardColorless.SecretTechnique(), 1);
        builder.addEnemy(new EnemyEnding.CorruptHeart());
        builder.addPotion(new Potion.SwiftPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.BlockPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(1);
        GameStateRandomization randomization = new GameStateRandomization.SimpleCustomRandomization(List.of(
                (state) -> {},
                (state) -> {
                    state.getPotionsStateForWrite()[0] = 0;
                },
                (state) -> {
                    state.getPotionsStateForWrite()[3] = 0;
                },
                (state) -> {
                    state.getPotionsStateForWrite()[0] = 0;
                    state.getPotionsStateForWrite()[3] = 0;
                }
        )).setDescriptions(
                "No Potion Used",
                "Swift Potion Used",
                "Block Potion Used",
                "All Potions Used"
        );
        randomization = new GameStateRandomization.SimpleCustomRandomization(List.of(
                (state) -> {
                    state.setIsStochastic();
                    if (state.getSearchRandomGen().nextInt(4, RandomGenCtx.Other) == 3) {
                        state.getPlayerForWrite().setHealth(26 + state.getSearchRandomGen().nextInt(46, RandomGenCtx.Other));
                    }
                }
        )).setDescriptions(
                "Random Health"
        ).doAfter(randomization);
        randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardSilent.Predator(), 1), new CardCount(new CardSilent.CloakAndDagger(), 1)),
                List.of(new CardCount(new CardSilent.CloakAndDagger(), 1))
        )).doAfter(randomization);
        builder.setRandomization(randomization);
        builder.addRelic(new Relic.MummifiedHand());
        builder.addRelic(new Relic.RingOfSerpent());
        builder.addRelic(new Relic.Shuriken());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.SlaversCollar());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.SneckoSkull());
        builder.addRelic(new Relic.PaperCrane());
        builder.addRelic(new Relic.Tingsha());
        builder.addRelic(new Relic.ThreadAndNeedle());
        builder.addRelic(new Relic.OrangePellets());
        builder.setPlayer(new Player(72, 72));
        return new GameState(builder);
    }

    public static GameState TestStateSilentHeart2() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new Card.Strike(), 3);
        builder.addCard(new Card.Defend(), 5);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardSilent.Survivor(), 1);
        builder.addCard(new CardSilent.NeutralizeP(), 1);
        builder.addCard(new CardSilent.EndlessAgony(), 1);
        builder.addCard(new CardSilent.Backstab(), 1);
        builder.addCard(new CardSilent.Eviscerate(), 1);
        builder.addCard(new CardSilent.WellLaidPlansP(), 1);
        builder.addCard(new CardSilent.AllOutAttackP(), 1);
        builder.addCard(new CardSilent.BurstP(), 1);
        builder.addCard(new CardSilent.EscapePlan(), 1);
        builder.addCard(new CardSilent.Acrobatics(), 2);
        builder.addCard(new CardSilent.TerrorP(), 1);
        builder.addCard(new CardSilent.DaggerSpray(), 1);
        builder.addCard(new CardSilent.PhantasmalKillerP(), 1);
        builder.addCard(new CardSilent.BladeDance(), 1);
        builder.addCard(new CardSilent.DodgeAndRoll(), 1);
        builder.addCard(new CardSilent.CloakAndDagger(), 2);
        builder.addCard(new CardSilent.MalaiseP(), 1);
        builder.addCard(new CardSilent.EnvenomP(), 1);
        builder.addCard(new CardSilent.FootworkP(), 2);
        builder.addCard(new CardSilent.Deflect(), 1);
        builder.addCard(new CardSilent.CalculatedGamble(), 1);
        builder.addCard(new CardSilent.PiercingWailP(), 1);
        builder.addCard(new CardSilent.Predator(), 0);
        builder.addCard(new CardColorless.MasterOfStrategy(), 1);
        builder.addCard(new CardColorless.ThinkingAhead(), 1);
        builder.addCard(new CardColorless.SecretTechnique(), 1);
        builder.addEnemy(new EnemyEnding.CorruptHeart());
        builder.addPotion(new Potion.DuplicationPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.BlockPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(1);
        GameStateRandomization randomization = new GameStateRandomization.SimpleCustomRandomization(List.of(
                (state) -> {},
                (state) -> {
                    state.getPotionsStateForWrite()[0] = 0;
                },
                (state) -> {
                    state.getPotionsStateForWrite()[3] = 0;
                },
                (state) -> {
                    state.getPotionsStateForWrite()[0] = 0;
                    state.getPotionsStateForWrite()[3] = 0;
                }
        )).setDescriptions(
                "No Potion Used",
                "Swift Potion Used",
                "Block Potion Used",
                "All Potions Used"
        );
        randomization = new GameStateRandomization.SimpleCustomRandomization(List.of(
                (state) -> {
                    state.setIsStochastic();
                    if (state.getSearchRandomGen().nextInt(4, RandomGenCtx.Other) == 3) {
                        state.getPlayerForWrite().setHealth(26 + state.getSearchRandomGen().nextInt(46, RandomGenCtx.Other));
                    }
                }
        )).setDescriptions(
                "Random Health"
        ).doAfter(randomization);
        randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardSilent.Predator(), 1), new CardCount(new CardSilent.CloakAndDagger(), 1)),
                List.of(new CardCount(new CardSilent.CloakAndDagger(), 1))
        )).doAfter(randomization);
        builder.setPreBattleRandomization(randomization);
        builder.addRelic(new Relic.MummifiedHand());
        builder.addRelic(new Relic.RingOfSerpent());
        builder.addRelic(new Relic.Shuriken());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.SlaversCollar());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.SneckoSkull());
        builder.addRelic(new Relic.PaperCrane());
        builder.addRelic(new Relic.Tingsha());
        builder.addRelic(new Relic.ThreadAndNeedle());
        builder.setPlayer(new Player(72, 72));
        return new GameState(builder);
    }

    public static GameState TestStateSilent2() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 5);
        builder.addCard(new Card.AscendersBane(), 1);
        builder.addCard(new CardSilent.Survivor(), 1);
        builder.addCard(new CardSilent.NeutralizeP(), 1);
        builder.addCard(new CardSilent.EndlessAgony(), 1);
        builder.addCard(new CardSilent.Backstab(), 1);
        builder.addCard(new CardSilent.Eviscerate(), 1);
        builder.addCard(new CardSilent.WellLaidPlans(), 1);
        builder.addCard(new CardSilent.WellLaidPlansP(), 0);
        builder.addCard(new CardSilent.AllOutAttackP(), 1);
        builder.addCard(new CardSilent.BurstP(), 1);
        builder.addCard(new CardSilent.EscapePlan(), 1);
        builder.addCard(new CardSilent.Acrobatics(), 2);
        builder.addCard(new CardSilent.AcrobaticsP(), 0);
        builder.addCard(new CardSilent.Terror(), 1);
        builder.addCard(new CardSilent.TerrorP(), 0);
        builder.addCard(new CardSilent.DaggerSpray(), 1);
        builder.addCard(new CardSilent.PhantasmalKillerP(), 1);
        builder.addCard(new CardSilent.BladeDance(), 1);
        builder.addCard(new CardSilent.BladeDanceP(), 0);
        builder.addCard(new CardSilent.DodgeAndRoll(), 1);
        builder.addCard(new CardSilent.CloakAndDagger(), 1);
        builder.addCard(new CardSilent.CloakAndDaggerP(), 0);
        builder.addCard(new Card.Writhe(), 1);
        builder.addPotion(new Potion.SwiftPotion().setBasePenaltyRatio(90));
        builder.setPotionsScenarios(1);
        builder.addEnemy(new EnemyCity.BookOfStabbing());
        EnemyEncounter.addSlaversEliteFight(builder);
        //        EnemyEncounter.addGremlinLeaderFight2(builder);
        GameStateRandomization randomization = new GameStateRandomization.EnemyEncounterRandomization(builder.getEnemies(), List.of(
                List.of(new Tuple<>(0, -1)),
                List.of(new Tuple<>(1, -1), new Tuple<>(2, -1), new Tuple<>(3, -1))
                //                List.of(new Tuple<>(4, 0), new Tuple<>(5, 0), new Tuple<>(6, 0), new Tuple<>(7, -1))
        ));
        //        randomization = randomization.followByIf(2, builder.getRandomization().collapse("Random Gremlins"));
        builder.setRandomization(randomization);
        var startOfGameScenarios = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardSilent.CloakAndDaggerP(), 0), new CardCount(new CardSilent.CloakAndDagger(), 0),
                        new CardCount(new CardSilent.TerrorP(), 1), new CardCount(new CardSilent.Terror(), 0)),
                List.of(new CardCount(new CardSilent.CloakAndDaggerP(), 1), new CardCount(new CardSilent.CloakAndDagger(), 0),
                        new CardCount(new CardSilent.TerrorP(), 0), new CardCount(new CardSilent.Terror(), 1)),
                List.of(new CardCount(new CardSilent.CloakAndDaggerP(), 0), new CardCount(new CardSilent.CloakAndDagger(), 1),
                        new CardCount(new CardSilent.TerrorP(), 1), new CardCount(new CardSilent.Terror(), 0))
        )).setDescriptions(
                "No Clock And Dagger",
                "Upgrade Clock And Dagger",
                "Upgrade Terror"
        );
        builder.setPreBattleScenarios(startOfGameScenarios);
        builder.addRelic(new Relic.MummifiedHand());
        builder.addRelic(new Relic.RingOfSerpent());
        builder.addRelic(new Relic.Shuriken());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.SlaversCollar());
        builder.addRelic(new Relic.PreservedInsect());
        builder.setPlayer(new Player(38, 38));
        return new GameState(builder);
    }
}
