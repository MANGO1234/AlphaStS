package com.alphaStS;

import com.alphaStS.card.*;
import com.alphaStS.enemy.*;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.player.Player;

import java.util.List;

public class TestStates {
    public static GameState BasicGremlinNobState() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 5);
        builder.addCard(new Card.Defend(), 4);
        builder.addEnemyEncounter(new EnemyExordium.GremlinNob());
        var randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(),
                List.of(new CardCount(new Card.Strike(), 1)),
                List.of(new CardCount(new Card.Strike(), 2)),
                List.of(new CardCount(new CardIronclad.Bash(), 1)),
                List.of(new CardCount(new Card.Defend(), 4))
        ), GameStateRandomization.CardCountRandomization.UPGRADE);
        builder.setRandomization(randomization);
        builder.setPlayer(new Player(80, 80));
        return new GameState(builder);
    }

    public static GameState BasicGremlinNobState2() {
        var state = BasicGremlinNobState();
        state.properties.randomization = state.properties.randomization.fixR(3);
        return state;
    }

    public static GameState BasicJawWormState() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Strike(), 5);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addEnemyEncounter(new EnemyExordium.JawWorm(false));
        builder.addRelic(new Relic.RingOfSerpent());
        var randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardIronclad.Bash(), 1)),
                List.of(new CardCount(new Card.Defend(), 1),
                        new CardCount(new CardSilent.Neutralize(), 1),
                        new CardCount(new CardSilent.Survivor(), 1)),
                List.of(new CardCount(new Card.Defend(), 1),
                        new CardCount(new CardSilent.Neutralize(), 1),
                        new CardCount(new CardSilent.Survivor(), 1),
                        new CardCount(new CardSilent.Acrobatics(), 1))
        ), GameStateRandomization.CardCountRandomization.ADD_TO_DECK).setDescriptions(
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
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.SeverSoul(), 1);
        builder.addCard(new CardIronclad.Clash(), 1);
        builder.addCard(new CardIronclad.Headbutt(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.BurningPactP(), 1);
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
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 2);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.BodySlam(), 1);
        builder.addCard(new CardIronclad.Cleave(), 1);
        builder.addCard(new CardIronclad.IronWave(), 1);
        builder.addCard(new CardIronclad.Impervious(), 1);
        builder.addCard(new CardIronclad.SeeingRed(), 1);
        builder.addCard(new CardIronclad.Exhume(), 1);
        builder.addEnemyEncounter(new EnemyExordium.Lagavulin());
        builder.addPotion(new Potion.DexterityPotion());
        builder.setPlayer(new Player(73, 75));
        return new GameState(builder);
    }

    public static GameState BasicLagavulinState2() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 5);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardIronclad.Impervious(), 1);
        builder.addCard(new CardIronclad.SeeingRed(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 1);
        builder.addCard(new CardIronclad.PommelStrike(), 1);
        builder.addCard(new CardIronclad.Shockwave(), 1);
        builder.addCard(new CardIronclad.Inflame(), 1);
        builder.addEnemyEncounter(new EnemyExordium.Lagavulin());
        builder.setPlayer(new Player(60, 75));
        return new GameState(builder);
    }

    public static GameState GuardianState() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 2);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.BodySlamP(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.HemokinesisP(), 1);
        builder.addCard(new CardIronclad.Metallicize(), 1);
        builder.addCard(new CardIronclad.Hemokinesis(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 1);
        builder.addCard(new CardIronclad.FlameBarrier(), 1);
        builder.addEnemyEncounter(new EnemyExordium.TheGuardian());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.setPlayer(new Player(36, 75));
        return new GameState(builder);
    }

    public static GameState GuardianState2() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.BashP(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.Clash(), 1);
        builder.addCard(new CardIronclad.Armanent(), 1);
        builder.addCard(new CardIronclad.Carnage(), 1);
        builder.addCard(new CardIronclad.Metallicize(), 1);
        builder.addCard(new CardIronclad.Shockwave(), 1);
        builder.addCard(new CardIronclad.ShrugItOff(), 1);
        builder.addCard(new CardIronclad.PowerThrough(), 1);
        builder.addEnemyEncounter(new EnemyExordium.TheGuardian());
        builder.addRelic(new Relic.AncientTeaSet());
        builder.addRelic(new Relic.DuVuDoll());
        builder.addRelic(new Relic.WarpedTongs());
        var randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(),
                List.of(new CardCount(new CardIronclad.ShrugItOff(), 1)),
                List.of(new CardCount(new CardIronclad.Carnage(), 1))
        ), GameStateRandomization.CardCountRandomization.ADD_TO_DECK).union(3.0 / 4.0, new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardIronclad.PowerThrough(), 1))
        ), GameStateRandomization.CardCountRandomization.REMOVE_FROM_DECK)).join(new GameStateRandomization.SimpleCustomRandomization(List.of(
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
        builder.addCard(new CardIronclad.BashP(), 1);
        builder.addCard(new CardIronclad.Dropkick(), 2);
        builder.addEnemyEncounter(new EnemyExordium.TheGuardian());
        builder.setPlayer(new Player(41, 75));
        return new GameState(builder);
    }

    public static GameState SlimeBossStateLC() {
        // https://youtu.be/wKbAoS80HA0?t=11397
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 5);
        builder.addCard(new Card.Defend(), 3);
        builder.addCard(new Card.DefendP(), 1);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.Corruption(), 1);
        builder.addCard(new CardIronclad.TwinStrike(), 1);
        builder.addCard(new CardIronclad.UppercutP(), 1);
        builder.addCard(new CardIronclad.ShockwaveP(), 1);
        builder.addCard(new CardIronclad.ShrugItOff(), 1);
        builder.addCard(new CardIronclad.FlameBarrierP(), 1);
        builder.addCard(new CardIronclad.SpotWeakness(), 1);
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
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardIronclad.UppercutP(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.Inflame(), 1);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        EnemyEncounter.addSentriesFight(builder);
        builder.addEnemyEncounter(new EnemyExordium.GremlinNob());
        builder.addEnemyEncounter(new EnemyExordium.Lagavulin());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.setPlayer(new Player(43, 75));
        return new GameState(builder);
    }

    public static GameState TestState2() {
        GameStateBuilder builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.UppercutP(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.Inflame(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        EnemyEncounter.addSentriesFight(builder);
        builder.addEnemyEncounter(new EnemyExordium.GremlinNob());
        builder.addEnemyEncounter(new EnemyExordium.Lagavulin());
        builder.addRelic(new Relic.BagOfPreparation());
        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardIronclad.Combust(), 1), new CardCount(new CardIronclad.IronWave(), 1)),
                List.of(new CardCount(new CardIronclad.Combust(), 1), new CardCount(new CardIronclad.DemonForm(), 1)),
                List.of(new CardCount(new CardIronclad.SeeingRed(), 1), new CardCount(new CardIronclad.IronWave(), 1)),
                List.of(new CardCount(new CardIronclad.SeeingRed(), 1), new CardCount(new CardIronclad.DemonForm(), 1)),
                List.of(new CardCount(new CardIronclad.Combust(), 1))
        ), GameStateRandomization.CardCountRandomization.ADD_TO_DECK).fixR(0, 4);
        builder.setRandomization(randomization);
        builder.setPlayer(new Player(41, 75));
        return new GameState(builder);
    }

    public static GameState TestState3() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.UppercutP(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.Inflame(), 1);
        builder.addCard(new CardIronclad.Combust(), 1);
        builder.addCard(new CardIronclad.IronWave(), 1);
        builder.addCard(new CardIronclad.SpotWeakness(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        builder.addEnemyEncounter(new EnemyExordium.Lagavulin());
        builder.addEnemyEncounter(new EnemyExordium.GremlinNob());
        builder.setBurningElite();
        var randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardIronclad.Inflame(), 1)),
                List.of(new CardCount(new CardIronclad.IronWave(), 1)),
                List.of(new CardCount(new CardIronclad.Anger(), 1)),
                List.of(new CardCount(new CardColorless.DarkShackles(), 1)),
                List.of(new CardCount(new CardIronclad.Combust(), 1))
        ), GameStateRandomization.CardCountRandomization.UPGRADE);
        builder.setRandomization(randomization);
        builder.addPotion(new Potion.WeakPotion());
        builder.addPotion(new Potion.LiquidMemory());
        builder.setPlayer(new Player(16, 75));
        return new GameState(builder);
    }

    public static GameState TestState4() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.UppercutP(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.Inflame(), 1);
        builder.addCard(new CardIronclad.Combust(), 1);
        builder.addCard(new CardIronclad.IronWave(), 1);
        builder.addCard(new CardIronclad.SpotWeakness(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 1);
        builder.addCard(new CardIronclad.PowerThrough(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        builder.addEnemyEncounter(new EnemyExordium.Lagavulin());
        builder.addEnemyEncounter(new EnemyExordium.GremlinNob());
        builder.setBurningElite();
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addPotion(new Potion.LiquidMemory());
        builder.setPlayer(new Player(43, 75));
        return new GameState(builder);
    }

    public static GameState TestState5() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new CardColorless.Bite(), 5);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.UppercutP(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.InflameP(), 1);
        builder.addCard(new CardIronclad.Combust(), 1);
        builder.addCard(new CardIronclad.IronWave(), 1);
        builder.addCard(new CardIronclad.SpotWeakness(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 1);
        builder.addCard(new CardIronclad.PowerThrough(), 1);
        builder.addCard(new CardIronclad.PommelStrike(), 1);
        builder.addCard(new CardIronclad.Corruption(), 1);
        builder.addCard(new CardIronclad.Havoc(), 1);
        builder.addCard(new CardIronclad.FlameBarrier(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        //        builder.addCard(new CardIronclad.Impervious(), 1);
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
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new CardColorless.Bite(), 5);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.UppercutP(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.InflameP(), 1);
        builder.addCard(new CardIronclad.Combust(), 1);
        builder.addCard(new CardIronclad.IronWave(), 1);
        builder.addCard(new CardIronclad.SpotWeakness(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 1);
        builder.addCard(new CardIronclad.PowerThrough(), 1);
        builder.addCard(new CardIronclad.PommelStrike(), 1);
        builder.addCard(new CardIronclad.Corruption(), 1);
        builder.addCard(new CardIronclad.Havoc(), 1);
        builder.addCard(new CardIronclad.FlameBarrier(), 1);
        builder.addCard(new CardIronclad.SeeingRedP(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.RedMask());
        builder.addEnemyEncounter(new EnemyCity.BookOfStabbing());
        EnemyEncounter.addSlaversEliteFight(builder);
        var randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardIronclad.PommelStrike(), 1)),
                List.of(new CardCount(new CardIronclad.BattleTrance(), 1)),
                List.of(new CardCount(new CardIronclad.Combust(), 1))
        ), GameStateRandomization.CardCountRandomization.UPGRADE);
        builder.setRandomization(randomization);
        builder.addPotion(new Potion.DistilledChaos());
        builder.setPlayer(new Player(43, 60));
        return new GameState(builder);
    }

    public static GameState TestState7() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new CardColorless.Bite(), 5);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.UppercutP(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.InflameP(), 1);
        builder.addCard(new CardIronclad.Combust(), 1);
        builder.addCard(new CardIronclad.IronWave(), 1);
        builder.addCard(new CardIronclad.SpotWeakness(), 2);
        builder.addCard(new CardIronclad.BattleTranceP(), 1);
        builder.addCard(new CardIronclad.PowerThrough(), 1);
        builder.addCard(new CardIronclad.PommelStrikeP(), 1);
        builder.addCard(new CardIronclad.Corruption(), 1);
        builder.addCard(new CardIronclad.Havoc(), 1);
        builder.addCard(new CardIronclad.FlameBarrier(), 1);
        builder.addCard(new CardIronclad.SeeingRedP(), 1);
        builder.addCard(new CardIronclad.Disarm(), 1);
        builder.addCard(new CardIronclad.Impervious(), 1);
        builder.addCard(new CardIronclad.Shockwave(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        builder.addCard(new CardColorless.RitualDaggerP(15), 1);
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.RedMask());
        builder.addRelic(new Relic.AncientTeaSet());
        builder.addRelic(new Relic.HornCleat());
        builder.addRelic(new Relic.BurningBlood());
        builder.addRelic(new Relic.BottledLightning(new CardIronclad.BattleTranceP()));
        builder.addEnemyEncounter(new EnemyCity.TheChamp());
        builder.addPotion(new Potion.DistilledChaos());
        builder.addPotion(new Potion.EnergyPotion());
        builder.setPlayer(new Player(54, 60));
        return new GameState(builder);
    }

    public static GameState TestState8() {
        var builder = new GameStateBuilder();
        builder.addCard(new Card.Strike(), 3);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 1);
        builder.addCard(new CardIronclad.BodySlam(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.TrueGritP(), 1);
        builder.addCard(new CardIronclad.Rage(), 1);
        builder.addCard(new CardIronclad.ShrugItOff(), 1);
        builder.addCard(new CardIronclad.BurningPact(), 1);
        builder.addCard(new CardIronclad.Reaper(), 1);
        builder.addCard(new CardIronclad.DemonForm(), 1);
        builder.addEnemyEncounter(new EnemyExordium.TheGuardian());
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
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new CardColorless.Bite(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.UppercutP(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.InflameP(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        builder.addCard(new CardIronclad.Combust(), 1);
        builder.addCard(new CardIronclad.IronWave(), 1);
        builder.addCard(new CardIronclad.SpotWeakness(), 2);
        builder.addCard(new CardIronclad.BattleTranceP(), 1);
        builder.addCard(new CardIronclad.PowerThrough(), 1);
        builder.addCard(new CardIronclad.PommelStrikeP(), 1);
        builder.addCard(new CardIronclad.Corruption(), 1);
        builder.addCard(new CardIronclad.Havoc(), 1);
        builder.addCard(new CardIronclad.FlameBarrier(), 1);
        builder.addCard(new CardIronclad.SeeingRedP(), 1);
        builder.addCard(new CardIronclad.Disarm(), 1);
        builder.addCard(new CardIronclad.Impervious(), 1);
        builder.addCard(new CardIronclad.Shockwave(), 1);
        builder.addCard(new CardColorless.RitualDaggerP(30), 1);
        builder.addCard(new CardIronclad.BrutalityP(), 1);
        builder.addCard(new CardIronclad.ArmanentP(), 1);
        builder.addCard(new CardIronclad.Shockwave(), 1);
        builder.addCard(new CardIronclad.SwordBoomerang(), 1);
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.RedMask());
        builder.addRelic(new Relic.AncientTeaSet());
        builder.addRelic(new Relic.HornCleat());
        builder.addRelic(new Relic.BurningBlood());
        builder.addRelic(new Relic.BottledLightning(new CardIronclad.BattleTranceP()));
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
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.ArmanentP(), 1);
        builder.addCard(new CardIronclad.Exhume(), 1);
        builder.addCard(new CardIronclad.PowerThrough(), 1);
        builder.addCard(new CardIronclad.Disarm(), 1);
        builder.addCard(new CardIronclad.WildStrike(), 2);
        builder.addCard(new CardIronclad.SecondWind(), 1);
        builder.addCard(new CardIronclad.Inflame(), 1);
        builder.addEnemyEncounter(new EnemyExordium.TheGuardian());
        builder.addRelic(new Relic.ArtOfWar());
        //        builder.addPotion(new Potion.WeakPotion());
        //        builder.addPotion(new Potion.DuplicationPotion());
        builder.setPlayer(new Player(44, 85));
        return new GameState(builder);
    }

    public static GameState TestState12() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.Thunderclap(), 1);
        builder.addCard(new CardIronclad.Uppercut(), 1);
        builder.addRelic(new Relic.Vajira());
        EnemyEncounter.addSentriesFight(builder);
        builder.addEnemyEncounter(new EnemyExordium.GremlinNob());
        builder.addEnemyEncounter(new EnemyExordium.Lagavulin());
        GameStateRandomization scenarios = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardColorless.Finesse(), 1)),
                List.of(new CardCount(new CardIronclad.IronWave(), 1))
        ), GameStateRandomization.CardCountRandomization.ADD_TO_DECK).doAfter(new GameStateRandomization.CardCountRandomization(List.of(
                List.of(),
                List.of(new CardCount(new CardIronclad.Anger(), 1)),
                List.of(new CardCount(new CardIronclad.Uppercut(), 1))
        ), GameStateRandomization.CardCountRandomization.UPGRADE).join(new GameStateRandomization.SimpleCustomRandomization(List.of(
                (state) -> state.getPlayerForWrite().setOrigHealth(57),
                (state) -> state.getPlayerForWrite().setOrigHealth(40),
                (state) -> state.getPlayerForWrite().setOrigHealth(40)
        ))).setDescriptions(
                "Rest",
                "Upgrade Anger",
                "Upgrade Uppercut"
        ));
        scenarios = scenarios.union( 6.0 / 7, new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new Card.Strike(), 1))
        ), GameStateRandomization.CardCountRandomization.ADD_TO_DECK)).join(new GameStateRandomization.SimpleCustomRandomization(List.of(
                (state) -> state.getPlayerForWrite().setOrigHealth(57)
        )));
        builder.setPreBattleScenarios(scenarios);
        builder.setPlayer(new Player(40, 57));
        return new GameState(builder);
    }

    public static GameState TestState13() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.Thunderclap(), 1);
        builder.addCard(new CardIronclad.UppercutP(), 1);
        builder.addCard(new CardIronclad.IronWave(), 1);
        builder.addCard(new CardIronclad.SpotWeakness(), 1);
        builder.addCard(new CardIronclad.PowerThrough(), 1);
        builder.addCard(new CardIronclad.ArmanentP(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 1);
        builder.addRelic(new Relic.Vajira());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.Nunchaku(2, 2));
        EnemyEncounter.addSentriesFight(builder);
        //        builder.addEnemyEncounter(new EnemyExordium.GremlinNob());
        builder.addEnemyEncounter(new EnemyExordium.Lagavulin());
        builder.setBurningElite();
        builder.addPotion(new Potion.SwiftPotion());
        builder.addPotion(new Potion.LiquidMemory());
        builder.setPlayer(new Player(38, 38));
        return new GameState(builder);
    }

    public static GameState TestState14() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.Thunderclap(), 1);
        builder.addCard(new CardIronclad.UppercutP(), 1);
        builder.addCard(new CardIronclad.IronWave(), 1);
        builder.addCard(new CardIronclad.SpotWeakness(), 1);
        builder.addCard(new CardIronclad.PowerThrough(), 1);
        builder.addCard(new CardIronclad.ArmanentP(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 1);
        builder.addCard(new CardIronclad.SwordBoomerang(), 1);
        builder.addCard(new CardIronclad.Disarm(), 1);
        builder.addRelic(new Relic.Vajira());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.Nunchaku(8, 2));
        builder.addEnemyEncounter(new EnemyExordium.Hexaghost());
        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardIronclad.PowerThrough(), 1)),
                List.of(new CardCount(new CardIronclad.Disarm(), 1))
        ), GameStateRandomization.CardCountRandomization.UPGRADE);
        builder.setRandomization(randomization);
        builder.addPotion(new Potion.LiquidMemory());
        builder.setPlayer(new Player(35, 35));
        return new GameState(builder);
    }

    public static GameState TestState15() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.Thunderclap(), 1);
        builder.addCard(new CardIronclad.UppercutP(), 1);
        builder.addCard(new CardIronclad.IronWave(), 1);
        builder.addCard(new CardIronclad.SpotWeakness(), 1);
        builder.addCard(new CardIronclad.ArmanentP(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 2);
        builder.addCard(new CardIronclad.SwordBoomerang(), 2);
        builder.addCard(new CardIronclad.Disarm(), 1);
        builder.addCard(new CardIronclad.PowerThroughP(), 1);
        builder.addCard(new CardIronclad.Feed(), 1);
        builder.addCard(new CardIronclad.FiendFire(), 1);
        builder.addCard(new CardIronclad.Sentinel(), 1);
        builder.addCard(new CardIronclad.PommelStrike(), 1);
        builder.addCard(new CardIronclad.SeverSoul(), 1);
        builder.addCard(new CardIronclad.DemonForm(), 1);
        builder.addCard(new CardIronclad.Evolve(), 1);
        builder.addRelic(new Relic.Vajira());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.Nunchaku(7, 2));
        builder.addRelic(new Relic.GremlinHorn());
        EnemyEncounter.addByrdsFight(builder);
        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(),
                List.of(new CardCount(new CardIronclad.Havoc(), 1))
        ), GameStateRandomization.CardCountRandomization.ADD_TO_DECK);
        builder.setRandomization(randomization);
        builder.setPlayer(new Player(66, 70));
        return new GameState(builder);
    }

    public static GameState TestState16() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.AngerP(), 1);
        builder.addCard(new CardIronclad.Thunderclap(), 1);
        builder.addCard(new CardIronclad.UppercutP(), 1);
        builder.addCard(new CardIronclad.IronWave(), 1);
        builder.addCard(new CardIronclad.SpotWeakness(), 1);
        builder.addCard(new CardIronclad.ArmanentP(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 1);
        builder.addCard(new CardIronclad.BattleTranceP(), 1);
        builder.addCard(new CardIronclad.SwordBoomerang(), 2);
        builder.addCard(new CardIronclad.Disarm(), 1);
        builder.addCard(new CardIronclad.PowerThrough(), 1);
        builder.addCard(new CardIronclad.PowerThroughP(), 1);
        builder.addCard(new CardIronclad.Feed(8), 1);
        builder.addCard(new CardIronclad.FiendFireP(), 1);
        builder.addCard(new CardIronclad.Sentinel(), 1);
        builder.addCard(new CardIronclad.PommelStrikeP(), 1);
        builder.addCard(new CardIronclad.DemonFormP(), 1);
        builder.addCard(new CardIronclad.Evolve(), 1);
        builder.addCard(new CardIronclad.ShrugItOff(), 1);
        builder.addRelic(new Relic.Vajira());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.Nunchaku(9, 8));
        builder.addRelic(new Relic.GremlinHorn());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.HappyFlower(0, 8));
        builder.addRelic(new Relic.HornCleat());
        builder.addEnemyEncounter(new EnemyCity.TheChamp());
        builder.setPlayer(new Player(97, 101));
        return new GameState(builder);
    }

    public static GameState TestState16b() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.AngerP(), 1);
        builder.addCard(new CardIronclad.Thunderclap(), 1);
        builder.addCard(new CardIronclad.UppercutP(), 1);
        builder.addCard(new CardIronclad.IronWave(), 1);
        builder.addCard(new CardIronclad.SpotWeakness(), 1);
        builder.addCard(new CardIronclad.ArmanentP(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 1);
        builder.addCard(new CardIronclad.BattleTranceP(), 1);
        builder.addCard(new CardIronclad.SwordBoomerang(), 2);
        builder.addCard(new CardIronclad.Disarm(), 1);
        builder.addCard(new CardIronclad.PowerThrough(), 1);
        builder.addCard(new CardIronclad.PowerThroughP(), 1);
        builder.addCard(new CardIronclad.Feed(), 1);
        builder.addCard(new CardIronclad.FiendFireP(), 1);
        builder.addCard(new CardIronclad.Sentinel(), 1);
        builder.addCard(new CardIronclad.PommelStrikeP(), 2);
        builder.addCard(new CardIronclad.DemonFormP(), 1);
        builder.addCard(new CardIronclad.Evolve(), 1);
        builder.addCard(new CardIronclad.ShrugItOff(), 1);
        builder.addCard(new CardIronclad.Offering(), 1);
        builder.addCard(new CardIronclad.DarkEmbraceP(), 1);
        builder.addCard(new CardIronclad.FeelNoPain(), 2);
        builder.addCard(new CardIronclad.ShockwaveP(), 1);
        builder.addCard(new CardIronclad.WarcryP(), 2);
        builder.addCard(new CardIronclad.Exhume(), 1);
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
        builder.addEnemyEncounter(new EnemyBeyond.TimeEater());
//        builder.addPotion(new Potion.SneckoPotion());
//        builder.addPotion(new Potion.LiquidMemory());
//        builder.addPotion(new Potion.WeakPotion());
        builder.setPlayer(new Player(141, 145));
        return new GameState(builder);
    }

    public static GameState TestState16c2() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.AngerP(), 1);
        builder.addCard(new CardIronclad.Thunderclap(), 1);
        builder.addCard(new CardIronclad.UppercutP(), 1);
        builder.addCard(new CardIronclad.IronWave(), 1);
        builder.addCard(new CardIronclad.SpotWeakness(), 1);
        builder.addCard(new CardIronclad.ArmanentP(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 1);
        builder.addCard(new CardIronclad.BattleTranceP(), 1);
        builder.addCard(new CardIronclad.SwordBoomerang(), 2);
        builder.addCard(new CardIronclad.Disarm(), 1);
        builder.addCard(new CardIronclad.PowerThrough(), 1);
        builder.addCard(new CardIronclad.PowerThroughP(), 1);
        builder.addCard(new CardIronclad.Feed(), 1);
        builder.addCard(new CardIronclad.FiendFireP(), 1);
        builder.addCard(new CardIronclad.Sentinel(), 1);
        builder.addCard(new CardIronclad.PommelStrikeP(), 2);
        builder.addCard(new CardIronclad.DemonFormP(), 1);
        builder.addCard(new CardIronclad.Evolve(), 1);
        builder.addCard(new CardIronclad.ShrugItOff(), 1);
        builder.addCard(new CardIronclad.Offering(), 1);
        builder.addCard(new CardIronclad.DarkEmbraceP(), 1);
        builder.addCard(new CardIronclad.FeelNoPain(), 2);
        builder.addCard(new CardIronclad.ShockwaveP(), 1);
        builder.addCard(new CardIronclad.WarcryP(), 2);
        builder.addCard(new CardIronclad.Exhume(), 1);
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
        //        builder.addPotion(new Potion.SneckoPoRtion());
        //        builder.addPotion(new Potion.LiquidMemory());
        //        builder.addPotion(new Potion.WeakPotion());
        builder.setPlayer(new Player(126, 134));
        return new GameState(builder);
    }

    public static GameState TestStateIronCladHeart() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.AngerP(), 1);
        builder.addCard(new CardIronclad.Thunderclap(), 1);
        builder.addCard(new CardIronclad.UppercutP(), 1);
        builder.addCard(new CardIronclad.IronWave(), 1);
        builder.addCard(new CardIronclad.SpotWeakness(), 1);
        builder.addCard(new CardIronclad.ArmanentP(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 2);
        builder.addCard(new CardIronclad.BattleTranceP(), 1);
        builder.addCard(new CardIronclad.SwordBoomerang(), 2);
        builder.addCard(new CardIronclad.Disarm(), 1);
        builder.addCard(new CardIronclad.PowerThrough(), 1);
        builder.addCard(new CardIronclad.PowerThroughP(), 1);
        builder.addCard(new CardIronclad.Feed(0), 1);
        builder.addCard(new CardIronclad.FiendFireP(), 1);
        builder.addCard(new CardIronclad.PommelStrikeP(), 2);
        builder.addCard(new CardIronclad.DemonFormP(), 1);
        builder.addCard(new CardIronclad.EvolveP(), 1);
        builder.addCard(new CardIronclad.ShrugItOff(), 2);
        builder.addCard(new CardIronclad.Offering(), 1);
        builder.addCard(new CardIronclad.DarkEmbraceP(), 1);
        builder.addCard(new CardIronclad.FeelNoPain(), 2);
        builder.addCard(new CardIronclad.ShockwaveP(), 1);
        builder.addCard(new CardIronclad.WarcryP(), 2);
        builder.addCard(new CardIronclad.Exhume(), 1);
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
        builder.addEnemyEncounter(new EnemyEnding.CorruptHeart());
//        builder.addPotion(new Potion.SneckoPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.LiquidMemory().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.WeakPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.RegenerationPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(3);
        builder.setPlayer(new Player(123, 144));
        return new GameState(builder);
    }

    public static GameState TestState17() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 5);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.Carnage(), 1);
        builder.addCard(new CardIronclad.ArmanentP(), 1);
        builder.addCard(new CardIronclad.Metallicize(), 1);
        builder.addCard(new CardIronclad.IronWave(), 1);
        builder.addCard(new CardIronclad.ShrugItOff(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 1);
        builder.addCard(new CardIronclad.ImmolateP(), 1);
        EnemyEncounter.addSlimeBossFight(builder);
        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardIronclad.Anger(), 1), new CardCount(new CardIronclad.Headbutt(), 1), new CardCount(new CardIronclad.Evolve(), 1)),
                List.of(new CardCount(new CardIronclad.Anger(), 1), new CardCount(new CardIronclad.Headbutt(), 1)),
                List.of(new CardCount(new CardIronclad.Anger(), 1), new CardCount(new CardIronclad.Evolve(), 1)),
                List.of(new CardCount(new CardIronclad.Headbutt(), 1), new CardCount(new CardIronclad.Evolve(), 1)),
                List.of(),
                List.of(new CardCount(new CardIronclad.Evolve(), 1))
        ), GameStateRandomization.CardCountRandomization.ADD_TO_DECK);
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
        builder.addCard(new CardOther.AscendersBane(), 1);
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
//        builder.addEnemyEncounter(new EnemyCity.BookOfStabbing());
//        EnemyEncounter.addSlaversEliteFight(builder);
        EnemyEncounter.addGremlinLeaderFight(builder);
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
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.SunderP(), 2);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.BootSequenceP(), 1);
        builder.addCard(new CardDefect.EchoForm(), 2);
        builder.addCard(new CardDefect.Consume(), 1);
        builder.addCard(new CardDefect.EquilibriumP(), 1);
        builder.addCard(new CardDefect.SelfRepair(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
        builder.addCard(new CardDefect.RebootP(), 1);
        builder.addCard(new CardDefect.Darkness(), 1);
        builder.addEnemyEncounter(new EnemyCity.BookOfStabbing());
//        EnemyEncounter.addSlaversEliteFight(builder);
//        EnemyEncounter.addGremlinLeaderFight2(builder);
//        EnemyEncounter.addBronzeAutomatonFight(builder);
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
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.SunderP(), 2);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.BootSequenceP(), 1);
        builder.addCard(new CardDefect.EchoForm(), 2);
        builder.addCard(new CardDefect.ConsumeP(), 1);
        builder.addCard(new CardDefect.EquilibriumP(), 1);
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
        builder.addCard(new CardOther.Parasite(), 1);
        EnemyEncounter.addDonuAndDecaFight(builder);
//        builder.addEnemyEncounter(new EnemyBeyond.GiantHead());
//        builder.addEnemyEncounter(new EnemyBeyond.Nemesis());
//        EnemyEncounter.addReptomancerFight(builder);
//        builder.setBurningElite();
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
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.SunderP(), 2);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.BootSequenceP(), 1);
        builder.addCard(new CardDefect.EchoForm(), 2);
        builder.addCard(new CardDefect.ConsumeP(), 1);
        builder.addCard(new CardDefect.EquilibriumP(), 1);
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
        builder.addCard(new CardOther.Parasite(), 1);
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
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.SunderP(), 2);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.BootSequenceP(), 1);
        builder.addCard(new CardDefect.EchoForm(), 2);
        builder.addCard(new CardDefect.ConsumeP(), 1);
        builder.addCard(new CardDefect.EquilibriumP(), 1);
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
        builder.addCard(new CardOther.Parasite(), 1);
        builder.addEnemyEncounter(new EnemyEnding.CorruptHeart());
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
        builder.setPotionsScenarios(7);
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
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.Zap(), 1);
        builder.addCard(new CardDefect.DualCastP(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
        builder.addCard(new CardDefect.BallLightning(), 2);
        builder.addCard(new CardDefect.Skim(), 2);
        builder.addCard(new CardDefect.MelterP(), 1);
        builder.addCard(new CardDefect.BeamCellP(), 1);
        builder.addCard(new CardDefect.BiasedCognitionP(),1);
        builder.addCard(new CardDefect.Coolheaded(), 1);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addEnemyEncounter(new EnemyExordium.TheGuardian());
        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardDefect.Coolheaded(), 1)),
                List.of(new CardCount(new CardDefect.Glacier(), 1)),
                List.of(new CardCount(new CardDefect.Zap(), 1)),
                List.of()
        ), GameStateRandomization.CardCountRandomization.UPGRADE).join(new GameStateRandomization.SimpleCustomRandomization(List.of(
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
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.Zap(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.BeamCell(), 1);
        builder.addCard(new CardDefect.BootSequence(), 1);
        builder.addCard(new CardDefect.DoomAndGloomP(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
        builder.addEnemyEncounter(new EnemyExordium.Lagavulin());
        builder.addPotion(new Potion.FocusPotion());
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.TungstenRod());
        builder.setPlayer(new Player(68, 68));
        return new GameState(builder);
    }

    public static GameState TestStateSilentTodo() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 5);
        builder.addCard(new CardOther.AscendersBane(), 1);
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
        builder.addCard(new CardSilent.CalculatedGamble(), 1);
        builder.addCard(new CardSilent.PiercingWailP(), 1);
        builder.addCard(new CardColorless.MasterOfStrategy(), 1);
        builder.addCard(new CardColorless.ThinkingAhead(), 1);
        builder.addCard(new CardColorless.SecretTechnique(), 1);
        EnemyEncounter.addDonuAndDecaFight(builder);
        var startOfGameScenarios = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(),
                List.of(new CardCount(new CardSilent.Deflect(), 1)),
                List.of(new CardCount(new CardSilent.Deflect(), 1), new CardCount(new CardSilent.Blur(), 1))
        ), GameStateRandomization.CardCountRandomization.ADD_TO_DECK);
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

    public static GameState TestStateSilentHeartOrangePellet() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 5);
        builder.addCard(new CardOther.AscendersBane(), 1);
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
        builder.addCard(new CardSilent.LegSweepP(), 1);
        builder.addCard(new CardSilent.PiercingWailP(), 1);
        builder.addCard(new CardSilent.PiercingWailP(), 1);
        builder.addCard(new CardColorless.MasterOfStrategy(), 1);
        builder.addCard(new CardColorless.ThinkingAhead(), 1);
        builder.addCard(new CardColorless.SecretTechnique(), 1);
        builder.addEnemyEncounter(new EnemyEnding.CorruptHeart());
        builder.setPotionsScenarios(1);
        builder.addPotion(new Potion.SwiftPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.BlockPotion().setBasePenaltyRatio(100));
//        builder.setGameStateViaInteractiveMode(List.of("rng off", "do", "def", "mal+", "def", "cal", "pha+", "legswe+", "e", "0", "1", "exit"));
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

    public static GameState TestStateDefectB1() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.Strike(), 3);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.Zap(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.BiasedCognition(), 2);
        builder.addCard(new CardDefect.BiasedCognitionP(), 1);
        builder.addCard(new CardDefect.SteamBarrier(), 1);
        builder.addCard(new CardDefect.DoomAndGloom(), 1);
        builder.addCard(new CardDefect.CompiledDriver(), 1);
        builder.addCard(new CardDefect.Streamline(), 1);
        builder.addCard(new CardDefect.ChargeBatteryP(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.FissionP(), 1);
        builder.addCard(new CardDefect.BarrageP(), 1);
        builder.addCard(new CardDefect.TurboP(), 1);
        builder.addCard(new CardDefect.MachineLearning(), 1);
        builder.addCard(new CardDefect.ChillP(), 1);
        builder.addCard(new CardDefect.ChaosP(), 1);
        builder.addCard(new CardDefect.RecursionP(), 1);
        builder.addCard(new CardDefect.SeekP(), 1);
        builder.addEnemyEncounter(new EnemyCity.TorchHead(), new EnemyCity.TorchHead(), new EnemyCity.TheCollector());
        builder.setGameStateViaInteractiveMode(List.of("", "do", "dua", "bi", "zap", "strea2", "e", "0", "exit"));
        builder.addPotion(new Potion.DexterityPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.WeakPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(3);
        builder.setPlayer(new Player(53, 53));
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.Sundial(0, 0));
        builder.addRelic(new Relic.StrikeDummy());
        builder.addRelic(new Relic.Inserter(1, 0));
        builder.addRelic(new Relic.EmotionChip());
        builder.addRelic(new Relic.AncientTeaSet());
        return new GameState(builder);
    }

    public static GameState TestStateDefectB2() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.Strike(), 1);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.Zap(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.BiasedCognition(), 1);
        builder.addCard(new CardDefect.BiasedCognitionP(), 2);
        builder.addCard(new CardDefect.SteamBarrier(), 1);
        builder.addCard(new CardDefect.DoomAndGloom(), 1);
        builder.addCard(new CardDefect.CompiledDriver(), 1);
        builder.addCard(new CardDefect.Streamline(), 1);
        builder.addCard(new CardDefect.ChargeBatteryP(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.FissionP(), 2);
        builder.addCard(new CardDefect.BarrageP(), 1);
        builder.addCard(new CardDefect.TurboP(), 1);
        builder.addCard(new CardDefect.MachineLearning(), 1);
        builder.addCard(new CardDefect.ChillP(), 1);
        builder.addCard(new CardDefect.ChaosP(), 1);
        builder.addCard(new CardDefect.RecursionP(), 1);
        builder.addCard(new CardDefect.SeekP(), 2);
        builder.addCard(new CardDefect.RainbowP(), 1);
        builder.addCard(new CardDefect.OverclockP(), 1);
        builder.addCard(new CardDefect.ConsumeP(), 1);
        builder.addCard(new CardDefect.LoopP(), 1);
        builder.addCard(new CardDefect.EchoForm(), 1);
        builder.addCard(new CardDefect.HologramP(), 1);
        builder.addCard(new CardDefect.CoolheadedP(), 1);
        builder.addCard(new CardOther.Decay(), 1);
//        EnemyEncounter.addShieldAndSpearFight(builder);
//        EnemyEncounter.addDonuAndDecaFight(builder);
//        EnemyEncounter.addAwakenedOneFight(builder);
        builder.addEnemyEncounter(new EnemyBeyond.TimeEater());
        builder.setGameStateViaInteractiveMode(List.of("", "do", "def", "coo", "com", "p", "compi", "loop+", "e", "rng off", "0", "em", "2", "exit"));
        builder.addPotion(new Potion.BlessingOfTheForge().setBasePenaltyRatio(90));
        builder.addPotion(new Potion.FairyInABottle(98).setBasePenaltyRatio(90));
        builder.setPotionsScenarios(1);
        builder.setPlayer(new Player(80, 80));
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.Sundial(0, 2));
        builder.addRelic(new Relic.StrikeDummy());
        builder.addRelic(new Relic.Inserter(0, 2));
        builder.addRelic(new Relic.EmotionChip());
        builder.addRelic(new Relic.AncientTeaSet());
        builder.addRelic(new Relic.CursedKey());
        return new GameState(builder);
    }

    public static GameState TestStateDefectB3() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.Strike(), 1);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.Zap(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.BiasedCognition(), 1);
        builder.addCard(new CardDefect.BiasedCognitionP(), 2);
        builder.addCard(new CardDefect.SteamBarrier(), 1);
        builder.addCard(new CardDefect.DoomAndGloom(), 1);
        builder.addCard(new CardDefect.CompiledDriver(), 1);
        builder.addCard(new CardDefect.Streamline(), 1);
        builder.addCard(new CardDefect.ChargeBatteryP(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.FissionP(), 2);
        builder.addCard(new CardDefect.BarrageP(), 1);
        builder.addCard(new CardDefect.TurboP(), 1);
        builder.addCard(new CardDefect.MachineLearningP(), 1);
        builder.addCard(new CardDefect.ChillP(), 1);
        builder.addCard(new CardDefect.ChaosP(), 1);
        builder.addCard(new CardDefect.RecursionP(), 1);
        builder.addCard(new CardDefect.SeekP(), 2);
        builder.addCard(new CardDefect.RainbowP(), 1);
        builder.addCard(new CardDefect.OverclockP(), 1);
        builder.addCard(new CardDefect.ConsumeP(), 1);
        builder.addCard(new CardDefect.LoopP(), 1);
        builder.addCard(new CardDefect.EchoForm(), 1);
        builder.addCard(new CardDefect.HologramP(), 1);
        builder.addCard(new CardDefect.CoolheadedP(), 1);
        builder.addCard(new CardDefect.Loop(), 1);
        builder.addCard(new CardDefect.StackP(), 1);
        builder.addCard(new CardOther.Decay(), 1);
        EnemyEncounter.addShieldAndSpearFight(builder);
//        builder.addEnemyEncounter(new EnemyEnding.CorruptHeart());
        builder.setGameStateViaInteractiveMode(List.of("", "rng off", "0", "19", "18", "13", "em", "1", "0", "em", "0", "1", "exit"));
        builder.addPotion(new Potion.BlessingOfTheForge().setBasePenaltyRatio(90));
        builder.addPotion(new Potion.FairyInABottle(98).setBasePenaltyRatio(90));
        builder.setPotionsScenarios(1);
        builder.setPlayer(new Player(88, 98));
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.Sundial(2, 2));
        builder.addRelic(new Relic.StrikeDummy());
        builder.addRelic(new Relic.Inserter(1, 2));
        builder.addRelic(new Relic.EmotionChip());
        builder.addRelic(new Relic.AncientTeaSet());
        builder.addRelic(new Relic.CursedKey());
        builder.addRelic(new Relic.Toolbox());
        return new GameState(builder);
    }

    public static GameState TestStateDefectB4() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.Strike(), 1);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.Zap(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.BiasedCognition(), 1);
        builder.addCard(new CardDefect.BiasedCognitionP(), 2);
        builder.addCard(new CardDefect.SteamBarrier(), 1);
        builder.addCard(new CardDefect.DoomAndGloom(), 1);
        builder.addCard(new CardDefect.CompiledDriver(), 1);
        builder.addCard(new CardDefect.Streamline(), 1);
        builder.addCard(new CardDefect.ChargeBatteryP(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.FissionP(), 2);
        builder.addCard(new CardDefect.BarrageP(), 1);
        builder.addCard(new CardDefect.TurboP(), 1);
        builder.addCard(new CardDefect.MachineLearningP(), 1);
        builder.addCard(new CardDefect.ChillP(), 1);
        builder.addCard(new CardDefect.ChaosP(), 1);
        builder.addCard(new CardDefect.RecursionP(), 1);
        builder.addCard(new CardDefect.SeekP(), 2);
        builder.addCard(new CardDefect.RainbowP(), 1);
        builder.addCard(new CardDefect.OverclockP(), 1);
        builder.addCard(new CardDefect.ConsumeP(), 1);
        builder.addCard(new CardDefect.LoopP(), 1);
        builder.addCard(new CardDefect.EchoForm(), 1);
        builder.addCard(new CardDefect.HologramP(), 1);
        builder.addCard(new CardDefect.CoolheadedP(), 1);
        builder.addCard(new CardDefect.Loop(), 1);
        builder.addCard(new CardDefect.StackP(), 1);
        builder.addCard(new CardOther.Decay(), 1);
        builder.addEnemyEncounter(new EnemyEnding.CorruptHeart());
//        builder.setGameStateViaInteractiveMode(List.of("", "rng off", "0", "19", "18", "13", "em", "1", "0", "em", "0", "1", "exit"));
        builder.addPotion(new Potion.BlessingOfTheForge().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.FairyInABottle(98).setBasePenaltyRatio(100));
        builder.setPotionsScenarios(1);
        builder.setPlayer(new Player(63, 63));
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.Sundial(0, 0));
        builder.addRelic(new Relic.StrikeDummy());
        builder.addRelic(new Relic.Inserter(1, 0));
        builder.addRelic(new Relic.EmotionChip());
        builder.addRelic(new Relic.CursedKey());
        builder.addRelic(new Relic.Toolbox());
        builder.addRelic(new Relic.DataDisk());
        return new GameState(builder);
    }

    public static GameState TestStateDefectC2() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.Defend(), 3);
        builder.addCard(new Card.Strike(), 1);
        builder.addCard(new Card.StrikeP(), 1);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.DualCastP(), 1);
        builder.addCard(new CardDefect.CoolheadedP(), 2);
        builder.addCard(new CardDefect.SweepingBeamP(), 1);
        builder.addCard(new CardDefect.BallLightning(), 1);
        builder.addCard(new CardDefect.CompiledDriver(), 2);
        builder.addCard(new CardDefect.CompiledDriverP(), 1);
        builder.addCard(new CardDefect.Equilibrium(), 1);
        builder.addCard(new CardDefect.BeamCellP(), 1);
        builder.addCard(new CardDefect.BufferP(), 1);
        builder.addCard(new CardDefect.Sunder(), 1);
        builder.addCard(new CardDefect.MachineLearning(), 1);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addCard(new CardDefect.Capacitor(), 1);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.HologramP(), 1);
        builder.addCard(new CardDefect.GoForTheEye(), 1);
        builder.addCard(new CardDefect.CoreSurge(), 1);
        builder.addCard(new CardDefect.SelfRepair(), 1);
        builder.addCard(new CardDefect.ForceField(), 1);
        builder.addCard(new CardDefect.AggregateP(), 1);
        builder.addCard(new CardDefect.LoopP(), 1);
        builder.addCard(new CardDefect.StackP(), 1);
        builder.addCard(new CardDefect.Stack(), 2);
        builder.setPlayer(new Player(63, 77));
//        EnemyEncounter.addAwakenedOneFight(builder);
//        EnemyEncounter.addDonuAndDecaFight(builder);
        builder.setGameStateViaInteractiveMode(List.of("", "rng off", "do", "def", "sta", "def", "coo+", "tur", "agg+", "com+", "holo+", "mac", "e", "0", "1", "2", "2", "2", "0", "0", "1", "3", "1", "exit"));
//        builder.addPotion(new Potion.FocusPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.DuplicationPotion().setBasePenaltyRatio(95));
        builder.setPotionsScenarios(1);
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.NuclearBattery());
        builder.addRelic(new Relic.RedMask());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.GamblingChip());
        builder.addRelic(new Relic.DeadBranch());
        builder.addRelic(new Relic.SneckoEye());
        builder.addRelic(new Relic.UnceasingTop());
        builder.addRelic(new Relic.ArtOfWar());
        return new GameState(builder);
    }

    public static GameState TestStateDefectD() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new Card.Strike(), 3);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.Hologram(), 1);
        builder.addCard(new CardDefect.Coolheaded(), 1);
        builder.addCard(new CardDefect.FTL(), 1);
        builder.addCard(new CardDefect.Zap(), 1);
        builder.addCard(new CardDefect.SunderP(), 1);
        builder.addCard(new CardDefect.ChargeBattery(), 2);
        builder.addCard(new CardDefect.ChargeBatteryP(), 1);
        builder.addCard(new CardDefect.Rainbow(), 1);
        builder.addCard(new CardDefect.ConsumeP(), 1);
        builder.addCard(new CardDefect.DoomAndGloom(), 1);
        builder.addCard(new CardDefect.StaticDischarge(), 1);
        builder.addCard(new CardDefect.EchoForm(), 1);
        builder.setPlayer(new Player(65, 65));
        EnemyEncounter.addByrdsFight(builder);
//        builder.addEnemyEncounter(new EnemyCity.BookOfStabbing());
//        EnemyEncounter.addSlaversEliteFight(builder);
//        EnemyEncounter.addGremlinLeaderFight2(builder);
        builder.setGameStateViaInteractiveMode(List.of("", "rng off", "do", "con", "zap", "def", "rain", "str", "co", "p", "coo", "dua", "e", "0", "1", "3", "2", "3", "2", "1", "2", "em", "0", "1", "em", "1", "1", "em", "2", "1", "eho", "0", "27", "eho", "1", "33", "eho", "2", "28", "exit"));
        builder.addPotion(new Potion.FirePotion().setBasePenaltyRatio(90));
        builder.addPotion(new Potion.EssenceOfSteel().setBasePenaltyRatio(90));
        builder.setPotionsScenarios(3);
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.MummifiedHand());
        builder.addRelic(new Relic.SneckoEye());
        return new GameState(builder);
    }

    public static GameState TestStateSilent2() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 5);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardSilent.Survivor(), 1);
        builder.addCard(new CardSilent.NeutralizeP(), 1);
        builder.addCard(new CardSilent.EndlessAgony(), 1);
        builder.addCard(new CardSilent.Backstab(), 1);
        builder.addCard(new CardSilent.Eviscerate(), 1);
        builder.addCard(new CardSilent.WellLaidPlans(), 1);
        builder.addCard(new CardSilent.AllOutAttackP(), 1);
        builder.addCard(new CardSilent.BurstP(), 1);
        builder.addCard(new CardSilent.EscapePlan(), 1);
        builder.addCard(new CardSilent.Acrobatics(), 2);
        builder.addCard(new CardSilent.Terror(), 1);
        builder.addCard(new CardSilent.DaggerSpray(), 1);
        builder.addCard(new CardSilent.PhantasmalKillerP(), 1);
        builder.addCard(new CardSilent.BladeDance(), 1);
        builder.addCard(new CardSilent.DodgeAndRoll(), 1);
        builder.addCard(new CardSilent.Terror(), 1);
        builder.addCard(new CardOther.Writhe(), 1);
        builder.addPotion(new Potion.SwiftPotion().setBasePenaltyRatio(90));
        builder.setPotionsScenarios(1);
        builder.addEnemyEncounter(new EnemyCity.BookOfStabbing());
        EnemyEncounter.addSlaversEliteFight(builder);
        //        EnemyEncounter.addGremlinLeaderFight2(builder);
        var startOfGameScenarios = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(),
                List.of(new CardCount(new CardSilent.CloakAndDagger(), 1)),
                List.of(new CardCount(new CardSilent.CloakAndDagger(), 1))
        ), GameStateRandomization.CardCountRandomization.ADD_TO_DECK).join(new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardSilent.Terror(), 1)),
                List.of(new CardCount(new CardSilent.CloakAndDagger(), 1)),
                List.of(new CardCount(new CardSilent.Terror(), 1))
        ), GameStateRandomization.CardCountRandomization.UPGRADE));
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

    public static GameState TestStateDefectRun4() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.BiasedCognitionP(), 1);
        builder.addCard(new CardDefect.SweepingBeam(), 2);
        builder.addCard(new CardDefect.CompiledDriver(), 1);
        builder.addCard(new CardDefect.FTL(), 1);
        builder.addCard(new CardDefect.ChargeBattery(), 4);
        builder.addCard(new CardDefect.Coolheaded(), 2);
        builder.addCard(new CardDefect.Hologram(), 1);
        builder.addCard(new CardDefect.SteamBarrier(), 1);
        builder.addCard(new CardDefect.BallLightning(), 1);
        builder.addCard(new CardDefect.CoreSurge(), 2);
        builder.addCard(new CardDefect.DoomAndGloom(), 1);
        builder.addCard(new CardDefect.EchoForm(), 1);
        builder.addCard(new CardDefect.Storm(), 1);
        builder.addCard(new CardDefect.Barrage(), 1);
        builder.addCard(new CardDefect.MeteorStrike(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
        builder.addCard(new CardDefect.Scrape(), 1);
        builder.addCard(new CardDefect.GoForTheEyeP(), 1);
        builder.addCard(new CardDefect.DefragmentP(), 1);
        builder.addCard(new CardDefect.Heatsinks(), 1);
        builder.addCard(new CardDefect.SeekP(), 1);
        builder.addCard(new CardDefect.SelfRepair(), 1);
        builder.addCard(new CardDefect.HologramP(), 1);
        builder.addCard(new CardDefect.TurboP(), 1);
        builder.addCard(new CardDefect.GlacierP(), 1);
        builder.addCard(new CardOther.Writhe(), 1);
        builder.addEnemyEncounter(new EnemyEnding.CorruptHeart());
        builder.addPotion(new Potion.PowerPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.WeakPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(3);
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.BronzeScales());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.Nunchaku(9, 0));
        builder.addRelic(new Relic.BlueCandle());
        builder.addRelic(new Relic.MummifiedHand());
        builder.addRelic(new Relic.Vajira());
        builder.addRelic(new Relic.Shuriken());
        builder.addRelic(new Relic.Anchor());
        builder.addRelic(new Relic.StrikeDummy());
        builder.addRelic(new Relic.RunicPyramid());
        builder.addRelic(new Relic.RunicCapacitor());
        builder.addRelic(new Relic.Sundial(2, 0));
        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.StoneCalendar());
        builder.setGameStateViaInteractiveMode(List.of("", "0", "rng off", "do", "seek+", "mete", "core", "skim", "comp", "glac", "e", "0", "exit"));
        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardDefect.Skim(), 1)),
                List.of()
        ), GameStateRandomization.CardCountRandomization.ADD_TO_DECK);
        builder.setPreBattleScenarios(randomization);
        builder.setPlayer(new Player(71, 71));
        return new GameState(builder);
    }

    public static GameState TestStateDefectRun5() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new Card.Defend(), 3);
        builder.addCard(new Card.DefendP(), 1);
        builder.addCard(new CardDefect.SweepingBeam(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.ColdSnapP(), 1);
        builder.addCard(new CardDefect.HyperBeamP(), 1);
        builder.addCard(new CardDefect.DefragmentP().setInnate(true), 1);
        builder.addCard(new CardDefect.SelfRepairHeart(), 2);
        builder.addCard(new CardDefect.ChargeBattery(), 2);
        builder.addCard(new CardDefect.Rebound(), 1);
        builder.addCard(new CardDefect.BiasedCognitionP(), 3);
        builder.addCard(new CardDefect.Sunder(), 1);
        builder.addCard(new CardDefect.Seek(), 1);
        builder.addCard(new CardDefect.FTLP(), 1);
        builder.addCard(new CardDefect.FTL(), 1);
        builder.addCard(new CardDefect.Equilibrium(), 1);
        builder.addCard(new CardDefect.MachineLearningP(), 1);
        builder.addCard(new CardDefect.CompiledDriverP(), 1);
        builder.addCard(new CardDefect.CapacitorP(), 1);
        builder.addCard(new CardDefect.Rainbow(), 1);
        builder.addCard(new CardDefect.HeatsinksP(), 2);
        builder.addCard(new CardDefect.HologramP(), 1);
        builder.addCard(new CardDefect.DarknessP(), 1);
        builder.addCard(new CardDefect.TurboP(), 1);
        builder.addCard(new CardDefect.RecursionP(), 1);
        builder.addCard(new CardDefect.Overclock(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
        builder.addCard(new CardDefect.Coolheaded(), 1);
        builder.addCard(new CardColorless.ThinkingAhead(), 1);
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.ClockworkSouvenir());
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.Anchor());
        builder.addRelic(new Relic.FusionHammer());
        builder.addRelic(new Relic.CaptainsWheel());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.ToyOrnithopter());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.BronzeScales());
        builder.addRelic(new Relic.TheBoot());
        builder.addRelic(new Relic.PenNib(9, 0));
        builder.setGameStateViaInteractiveMode(List.of("", "do", "bias+", "think", "dua", "sel", "def", "e", "exit"));
//        builder.addRelic(new Relic.BiasedCognitionLimit());
        builder.addEnemyEncounter(new EnemyEnding.CorruptHeart());
        builder.addPotion(new Potion.AttackPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.FirePotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.DexterityPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.LizardTail(67).setBasePenaltyRatio(100));
        builder.setPotionsScenarios(7);
        builder.setPlayer(new Player(39, 54));
        return new GameState(builder);
    }

    public static GameState TestStateDefectRun6() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.BallLightning(), 1);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.SunderP(), 1);
        builder.addCard(new CardDefect.BeamCell(), 2);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.SweepingBeam(), 1);
        builder.addCard(new CardDefect.DoomAndGloom(), 1);
        builder.addCard(new CardDefect.FissionP(), 1);
        builder.addCard(new CardDefect.FTL(), 1);
        builder.addCard(new CardDefect.Sunder(), 1);
        builder.addCard(new CardDefect.RecycleP(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
        builder.addCard(new CardDefect.DefragmentP(), 1);
        builder.addCard(new CardDefect.Defragment(), 1);
        builder.addCard(new CardDefect.SkimP(), 2);
        builder.addCard(new CardDefect.Chill(), 1);
        builder.addCard(new CardDefect.Hologram(), 1);
        builder.addCard(new CardDefect.EchoForm(), 1);
        builder.addCard(new CardDefect.CompiledDriverP(), 1);
        builder.addCard(new CardDefect.ConsumeP(), 1);
        builder.addCard(new CardDefect.Equilibrium(), 1);
        builder.addCard(new CardDefect.ColdSnapP(), 1);
        builder.addCard(new CardDefect.BlizzardP(), 1);
        builder.addCard(new CardDefect.HeatsinksP(), 1);
        builder.addCard(new CardDefect.Heatsinks(), 1);
        builder.addCard(new CardDefect.Loop(), 1);
        builder.addCard(new CardDefect.LoopP(), 1);
        builder.addCard(new CardDefect.GeneticAlgorithm(71, 0), 1);
        builder.addCard(new CardDefect.GeneticAlgorithmP(76, 0), 1);
        builder.addCard(new CardDefect.GeneticAlgorithmP(67, 0), 1);
        builder.addCard(new Card.Defend(), 1);
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.ToyOrnithopter());
        builder.addRelic(new Relic.Anchor());
        builder.addRelic(new Relic.RedMask());
        builder.addRelic(new Relic.CoffeeDripper());
        builder.addRelic(new Relic.BronzeScales());
        builder.addRelic(new Relic.Ginger());
        builder.addRelic(new Relic.BloodyIdol());
        builder.addRelic(new Relic.BloodVial());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.PotionBelt());
        builder.addRelic(new Relic.PenNib(0, 0));
        builder.addPotion(new Potion.DuplicationPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.ExplosivePotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.FairyInABottle(71).setBasePenaltyRatio(100));
        builder.addPotion(new Potion.EntropicBrew().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(7);
        builder.addEnemyEncounter(new EnemyEnding.CorruptHeart());
        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardDefect.StormP(), 1)),
                List.of(new CardCount(new CardDefect.BeamCellP(), 1)),
                List.of()
        ), GameStateRandomization.CardCountRandomization.ADD_TO_DECK);
        builder.setPreBattleScenarios(randomization);
        builder.setGameStateViaInteractiveMode(List.of("2", "do", "rec", "sun+", "cons", "def+", "zap+", "cold+", "defra", "e", "0", "exit"));
        builder.setPlayer(new Player(34, 71));
        return new GameState(builder);
    }

    public static GameState TestStateDefectRun7() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.SunderP(), 1);
        builder.addCard(new CardDefect.MachineLearning(), 1);
        builder.addCard(new CardDefect.BallLightning(), 1);
        builder.addCard(new CardDefect.Coolheaded(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 2);
        builder.addCard(new CardDefect.ColdSnapP(), 1);
        builder.addCard(new CardDefect.BeamCellP(), 1);
        builder.addCard(new CardDefect.SkimP(), 1);
        builder.addCard(new CardDefect.AggregateP(), 2);
        builder.addCard(new CardDefect.ChargeBatteryP(), 1);
        builder.addCard(new CardDefect.BootSequenceP(), 1);
        builder.addCard(new CardDefect.EchoForm(), 1);
        builder.addCard(new CardDefect.ChaosP(), 1);
        builder.addCard(new CardDefect.AllForOne(0, 0), 1);
        builder.addCard(new CardDefect.ConsumeP(), 1);
        builder.addCard(new CardDefect.RecursionP(), 1);
        builder.addCard(new CardDefect.GoForTheEyeP(), 1);
        builder.addCard(new CardDefect.SelfRepair(), 1);
        builder.addCard(new CardDefect.CoolheadedP(), 2);
        builder.addCard(new CardDefect.DefragmentP(), 3);
        builder.addCard(new CardDefect.HologramP(), 2);
        builder.addCard(new CardDefect.TurboP(), 2);
        builder.addCard(new CardDefect.OverclockP(), 2);
        builder.addCard(new CardDefect.DoubleEnergyP(), 1);
        builder.addCard(new CardDefect.HeatsinksP(), 1);
        builder.addCard(new CardDefect.SeekP(), 1);
        builder.addCard(new CardDefect.RecycleP(), 1);
        builder.addCard(new CardDefect.Capacitor(), 1);
        builder.addCard(new CardDefect.GeneticAlgorithmP(37, 0), 1);
        builder.addCard(new CardDefect.GeneticAlgorithmP(43, 0), 1);
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.SlingOfCourage());
        builder.addRelic(new Relic.IceCream());
        builder.addRelic(new Relic.DataDisk());
        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.BloodVial());
        builder.addRelic(new Relic.PenNib(7, 0));
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.RunicPyramid());
        builder.addRelic(new Relic.Akabeko());
        builder.addRelic(new Relic.Vajira());
        builder.addRelic(new Relic.MedicalKit());
        builder.addRelic(new Relic.TungstenRod());
        builder.addPotion(new Potion.LizardTail(83).setBasePenaltyRatio(100));
//        builder.addPotion(new Potion.DuplicationPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.PotionOfCapacity().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.LiquidMemory().setBasePenaltyRatio(100));
//        builder.addPotion(new Potion.DrawPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(3);
        builder.addEnemyEncounter(new EnemyEnding.CorruptHeart());
//        EnemyEncounter.addShieldAndSpearFollowByHeartFight(builder);
        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardDefect.Capacitor(), 1)),
                List.of(new CardCount(new CardDefect.BarrageP(), 1)),
                List.of(new CardCount(new CardDefect.MultiCastP(), 1)),
                List.of()
        ), GameStateRandomization.CardCountRandomization.ADD_TO_DECK);
        builder.setPreBattleScenarios(randomization);
        builder.setPlayer(new Player(83, 83));
        builder.setGameStateViaInteractiveMode(List.of("", "1", "do", "bea", "co", "co", "p", "p", "cold", "col+", "char", "e", "0", "exit"));
        return new GameState(builder);
    }

    public static GameState TestStateSilentRun2() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new Card.StrikeP(), 2);
        builder.addCard(new Card.DefendP(), 5);
        builder.addCard(new CardSilent.Neutralize(), 1);
        builder.addCard(new CardSilent.Survivor(), 1);
        builder.addCard(new CardSilent.PredatorP(), 2);
        builder.addCard(new CardSilent.BladeDance(), 1);
        builder.addCard(new CardSilent.BladeDanceP(), 3);
        builder.addCard(new CardSilent.BulletTime(), 1);
        builder.addCard(new CardSilent.Footwork(), 2);
        builder.addCard(new CardSilent.SuckerPunch(), 1);
        builder.addCard(new CardSilent.LegSweep(), 1);
        builder.addCard(new CardSilent.AfterImageP(), 1);
        builder.addCard(new CardSilent.AccuracyP(), 1);
        builder.addCard(new CardSilent.Predator(), 1);
        builder.addCard(new CardSilent.Backstab(), 1);
        builder.addCard(new CardSilent.PiercingWail(), 1);
        builder.addCard(new CardSilent.CalculatedGamble(), 1);
        builder.addCard(new CardSilent.MalaiseP(), 1);
        builder.addCard(new CardSilent.PiercingWailP(), 1);
        builder.addCard(new CardSilent.FinisherP(), 1);
        builder.addCard(new CardSilent.Acrobatics(), 1);
        builder.addCard(new CardSilent.DashP(), 1);
        builder.addCard(new CardSilent.QuickSlashP(), 1);
        builder.addCard(new CardSilent.Doppelganger(), 1);
        builder.addCard(new CardSilent.ExpertiseP(), 1);
        builder.addCard(new CardSilent.WellLaidPlans(), 1);
        builder.addCard(new CardOther.Parasite(), 1);
        builder.addCard(new CardColorless.PanicButton(), 1);
        builder.addRelic(new Relic.RingOfSerpent());
        builder.addRelic(new Relic.Anchor());
        builder.addRelic(new Relic.CoffeeDripper());
        builder.addRelic(new Relic.CursedKey());
        builder.addRelic(new Relic.OrnamentalFan());
        builder.addRelic(new Relic.FrozenEye());
        builder.addRelic(new Relic.HornCleat());
        builder.addRelic(new Relic.Girya(3));
        builder.addRelic(new Relic.Shuriken());
        builder.addRelic(new Relic.Akabeko());
        builder.addRelic(new Relic.UnceasingTop());
        builder.addRelic(new Relic.PenNib(6, 2));
        builder.addRelic(new Relic.PaperCrane());
        builder.addEnemyEncounter(new EnemyEnding.CorruptHeart());
//        EnemyEncounter.addShieldAndSpearFollowByHeartFight(builder);
        builder.addPotion(new Potion.DuplicationPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.ColorlessPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(3);
        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardSilent.Adrenaline(), 1)),
                List.of(new CardCount(new CardSilent.RiddleWithHoles(), 1))
        ), GameStateRandomization.CardCountRandomization.ADD_TO_DECK);
        builder.setPreBattleScenarios(randomization);
        builder.setPlayer(new Player(74, 74));
        builder.setGameStateViaInteractiveMode(List.of("", "0", "rng off", "0", "expertise+", "strike+", "predator+", "footwork", "predator+", "finishe+", "panic", "adrenaline", "dash+", "neutral", "piercing+", "survivor", "blade+", "blade", "defend+", "defend+", "calculated", "accuracy+", "legsweeo", "legsweep", "blade+", "blade+", "quickslash+", "12", "defend+", "9", "dopple", "parasite", "foot", "welllaid", "sucker", "malaise+", "defend+", "strike+", "bullet", "defend+", "ascender", "0", "exit"));
        return new GameState(builder);
    }

    public static GameState TestStateStreamerRun() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.Strike(), 2);
        builder.addCard(new Card.Defend(), 2);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.Zap(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.Rebound(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.SteamBarrier(6, -2), 1);
        builder.addCard(new CardDefect.BootSequence(), 1);
        builder.addCard(new CardDefect.BeamCellP(), 1);
        builder.addCard(new CardDefect.Coolheaded(), 1);
        builder.addCard(new CardDefect.GeneticAlgorithm(21, 4), 1);
        builder.addCard(new CardDefect.RebootP(), 1);
        builder.addCard(new CardDefect.CoreSurge(), 1);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.SweepingBeamP(), 1);
        builder.addCard(new CardDefect.HologramP(), 1);
        builder.addCard(new CardDefect.SweepingBeam(), 1);
        builder.addCard(new CardDefect.SkimP(), 1);
        builder.addCard(new CardColorless.DramaticEntranceP(), 1);
        builder.addEnemyEncounter(new EnemyCity.TorchHead(), new EnemyCity.TorchHead(), new EnemyCity.TheCollector());
        var startOfGameScenarios = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardDefect.Seek(), 1)),
                List.of(new CardCount(new CardDefect.SeekP(), 1), new CardCount(new CardDefect.Streamline(), 1),
                        new CardCount(new CardDefect.Skim(), 1), new CardCount(new CardDefect.StackP(), 1)),
                List.of(new CardCount(new CardDefect.SeekP(), 1), new CardCount(new CardDefect.Skim(), 1),
                        new CardCount(new CardDefect.StackP(), 1))
        ), GameStateRandomization.CardCountRandomization.ADD_TO_DECK);
        builder.setPreBattleScenarios(startOfGameScenarios);
        builder.addPotion(new Potion.StrengthPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.FirePotion().setBasePenaltyRatio(85));
        builder.setPotionsScenarios(3);
        builder.setPlayer(new Player(50, 50));
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.BagOfMarbles());
        builder.addRelic(new Relic.GoldPlatedCable());
        builder.addRelic(new Relic.NuclearBattery());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.RunicCapacitor());
        builder.addRelic(new Relic.PenNib(3, 2));
        builder.addRelic(new Relic.FossilizedHelix());
        return new GameState(builder);
    }

    public static GameState TestStateStreamerRun3() {
        var builder = new GameStateBuilder();
        boolean test = false;
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.Defend(), 2);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.Zap(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.Rebound(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.SteamBarrier(6, -2), 1);
        builder.addCard(new CardDefect.BootSequence(), 1);
        builder.addCard(new CardDefect.BeamCellP(), 2);
        builder.addCard(new CardDefect.Coolheaded(), 1);
        builder.addCard(new CardDefect.GeneticAlgorithm(39, test ? 0 : 4), 1);
        builder.addCard(new CardDefect.RebootP(), 1);
        builder.addCard(new CardDefect.CoreSurge(), 1);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.SweepingBeamP(), 1);
        builder.addCard(new CardDefect.HologramP(), 1);
        builder.addCard(new CardDefect.SweepingBeam(), 1);
        builder.addCard(new CardDefect.SkimP(), 2);
        builder.addCard(new CardColorless.DramaticEntranceP(), 1);
        builder.addCard(new CardDefect.StackP(), 1);
        builder.addCard(new CardDefect.Streamline(), 1);
        builder.addCard(new CardDefect.SeekP(), 1);
        builder.addCard(new CardDefect.AllForOne(1, 1), 1);
        builder.addCard(new CardDefect.BarrageP(), 1);
        builder.addCard(new CardDefect.LoopP(), 1);
        builder.addCard(new CardDefect.DoubleEnergyP(), 1);
        builder.addCard(new CardDefect.MeteorStrike(), 1);
        builder.addCard(new CardDefect.RecycleP(), 1);
        builder.addCard(new CardDefect.ChargeBatteryP(), 1);
        builder.addPotion(new Potion.FairyInABottle(71).setBasePenaltyRatio(80));
        builder.addPotion(new Potion.SpeedPotion().setBasePenaltyRatio(95));
        builder.setPotionsScenarios(1);
        builder.addEnemyEncounter(new EnemyBeyond.TimeEater());
//        builder.setInnateOrder("Boot Sequence", "Dramatic Entrance+");
        builder.setGameStateViaInteractiveMode(List.of("do", "Defend", "Defend", "Stack+", "e", "0", "exit"));
        builder.setPlayer(new Player(63, 63));
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.BagOfMarbles());
        builder.addRelic(new Relic.GoldPlatedCable());
        builder.addRelic(new Relic.NuclearBattery());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.RunicCapacitor());
        builder.addRelic(new Relic.PenNib(7, test ? 1 : 2));
        builder.addRelic(new Relic.FossilizedHelix());
        builder.addRelic(new Relic.RunicDome());
        builder.addRelic(new Relic.TungstenRod());
        builder.addRelic(new Relic.GremlinHorn());
        return new GameState(builder);
    }

    public static GameState TestStateStreamerRun4() {
        var builder = new GameStateBuilder();
        boolean test = true;
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.Defend(), 2);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.Zap(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardDefect.Rebound(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.SteamBarrier(6, test ? 0 : -2), 1);
        builder.addCard(new CardDefect.BootSequence(), 1);
        builder.addCard(new CardDefect.BeamCellP(), 2);
        builder.addCard(new CardDefect.Coolheaded(), 1);
        builder.addCard(new CardDefect.GeneticAlgorithm(41, test ? -2 : 0), 1);
        builder.addCard(new CardDefect.RebootP(), 1);
        builder.addCard(new CardDefect.CoreSurge(), 1);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.SweepingBeamP(), 1);
        builder.addCard(new CardDefect.HologramP(), 1);
        builder.addCard(new CardDefect.SweepingBeam(), 1);
        builder.addCard(new CardDefect.SkimP(), 2);
        builder.addCard(new CardColorless.DramaticEntranceP(), 1);
        builder.addCard(new CardDefect.StackP(), 1);
        builder.addCard(new CardDefect.Streamline(), 1);
        builder.addCard(new CardDefect.SeekP(), 1);
        builder.addCard(new CardDefect.AllForOne(1, 1), 1);
        builder.addCard(new CardDefect.BarrageP(), 1);
        builder.addCard(new CardDefect.LoopP(), 1);
        builder.addCard(new CardDefect.DoubleEnergyP(), 1);
        builder.addCard(new CardDefect.MeteorStrike(), 1);
        builder.addCard(new CardDefect.RecycleP(), 1);
        builder.addCard(new CardDefect.ChargeBatteryP(), 1);
        EnemyEncounter.addAwakenedOneFight(builder);
        //        builder.setInnateOrder("Boot Sequence", "Dramatic Entrance+");
        builder.setGameStateViaInteractiveMode(List.of("do", "Ascender's Bane", "Rebound", "Reboot+", "e", "0", "eho", "0", "56", "eho", "1", "56", "exit"));
        builder.setPlayer(new Player(1, 1));
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.BagOfMarbles());
        builder.addRelic(new Relic.GoldPlatedCable());
        builder.addRelic(new Relic.NuclearBattery());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.RunicCapacitor());
        builder.addRelic(new Relic.PenNib(5, 0));
        builder.addRelic(new Relic.FossilizedHelix());
        builder.addRelic(new Relic.RunicDome());
        builder.addRelic(new Relic.TungstenRod());
        builder.addRelic(new Relic.GremlinHorn());
        return new GameState(builder);
    }

    public static GameState TestStateStreamerRun6() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new Card.Defend(), 5);
        builder.addCard(new Card.Strike(), 5);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardSilent.Survivor(), 1);
        builder.addCard(new CardSilent.Neutralize(), 1);
        builder.addCard(new CardColorless.Panacea(), 1);
        builder.addCard(new CardSilent.MasterfulStab(4), 1);
        builder.addCard(new CardSilent.Backstab(), 1);
        builder.addCard(new CardSilent.DeadlyPoisonP(), 2);
        builder.addCard(new CardSilent.PiercingWail(), 2);
        builder.addCard(new CardSilent.SneakyStrike(), 1);
        builder.addCard(new CardSilent.SneakyStrikeP(), 1);
        builder.addCard(new CardSilent.AfterImage(), 1);
        builder.addCard(new CardSilent.SkewerP(), 1);
        builder.addCard(new CardSilent.Malaise(), 1);
        builder.addCard(new CardSilent.DashP(), 1);
        builder.addCard(new CardSilent.EviscerateP(), 1);
        builder.addCard(new CardSilent.Doppelganger(), 1);
        builder.addCard(new CardSilent.WraithFormP(), 2);
        builder.addCard(new CardColorless.SecretTechniqueP(), 1);
        builder.addCard(new CardColorless.Apparition(), 2);
        builder.addCard(new CardColorless.ApparitionP(), 1);
        builder.setPlayer(new Player(35, 35));
        builder.addRelic(new Relic.RingOfSerpent());
        builder.addRelic(new Relic.HappyFlower(2, 1));
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.ArtOfWar());
        builder.addRelic(new Relic.CoffeeDripper());
        builder.addPotion(new Potion.SpeedPotion().setBasePenaltyRatio(95));
        builder.addPotion(new Potion.CunningPotion().setBasePenaltyRatio(95));
        builder.setPotionsScenarios(3);
        EnemyEncounter.addBronzeAutomatonFight(builder);
        return new GameState(builder);
    }

    public static GameState TestStateReddit() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.Zap(), 1);
        builder.addCard(new CardDefect.DualCast(), 1);
        builder.addCard(new CardColorless.SecretTechnique(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.BeamCellP(), 1);
        builder.addCard(new CardDefect.BallLightning(), 1);
        builder.addCard(new CardDefect.DoomAndGloomP(), 1);
        builder.addCard(new CardDefect.Chill(), 1);
        builder.addCard(new CardDefect.DefragmentP(), 1);
        builder.addCard(new CardDefect.HologramP(), 1);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.EchoForm(), 2);
        builder.addCard(new CardDefect.StaticDischarge(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
        builder.addCard(new CardDefect.Blizzard(), 1);
        builder.addCard(new CardDefect.Skim(), 1);
        builder.addCard(new CardDefect.Buffer(), 1);
        builder.addCard(new CardDefect.CompiledDriver(), 1);
        builder.addCard(new CardDefect.ReinforcedBody(), 1);
        builder.addCard(new CardDefect.Aggregate(), 1);
        builder.addCard(new CardDefect.SelfRepair(), 1);
        builder.addCard(new CardColorless.Apotheosis(), 1);
        builder.addEnemyEncounter(new EnemyCity.BookOfStabbing());
        EnemyEncounter.addSlaversEliteFight(builder);
        EnemyEncounter.addGremlinLeaderFight(builder);
        GameStateRandomization randomization = new GameStateRandomization.CardCountRandomization(List.of(
                List.of(new CardCount(new CardDefect.CompiledDriver(), 1)),
                List.of(new CardCount(new CardDefect.Rebound(), 1)),
                List.of(new CardCount(new CardDefect.BeamCellP(), 1)),
                List.of()
        ), GameStateRandomization.CardCountRandomization.ADD_TO_DECK);
        builder.setRandomization(randomization);
//        builder.setGameStateViaInteractiveMode(List.of("", "do", "stat", "gl", "self", "agg", "reinf", "e", "0", "exit"));
        builder.setPlayer(new Player(55, 55));
        builder.addPotion(new Potion.FocusPotion().setBasePenaltyRatio(90));
        builder.addPotion(new Potion.EssenceOfSteel().setBasePenaltyRatio(90));
        builder.setPotionsScenarios(3);
        builder.addRelic(new Relic.CrackedOrb());
        builder.addRelic(new Relic.PenNib(9, 2));
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.OrangePellets());
        return new GameState(builder);
    }
}
