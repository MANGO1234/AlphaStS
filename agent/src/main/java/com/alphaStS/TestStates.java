package com.alphaStS;

import com.alphaStS.card.*;
import com.alphaStS.enemy.*;
import com.alphaStS.entity.Potion;
import com.alphaStS.entity.Relic;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.player.Player;
import com.alphaStS.GameStateRandomization.StateModificationRandomization;
import com.alphaStS.GameStateRandomization.StateModificationRandomization.Add;
import com.alphaStS.GameStateRandomization.StateModificationRandomization.Remove;
import com.alphaStS.GameStateRandomization.StateModificationRandomization.Upgrade;
import com.alphaStS.GameStateRandomization.StateModificationRandomization.PlayerHealth;

import java.util.List;

public class TestStates {
    public static GameState BasicGremlinNobState() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 5);
        builder.addCard(new Card.Defend(), 4);
        builder.addEnemyEncounter(PredefinedEncounter.GREMLIN_NOB);
        var randomization = new StateModificationRandomization(List.of(
                List.of(),
                List.of(new Upgrade(new Card.Strike())),
                List.of(new Upgrade(new Card.Strike(), 2)),
                List.of(new Upgrade(new CardIronclad.Bash())),
                List.of(new Upgrade(new Card.Defend(), 4))
        ));
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
        builder.addEnemyEncounter(PredefinedEncounter.JAW_WORM);
        builder.addRelic(new Relic.RingOfTheSnake());
        var randomization = new StateModificationRandomization(List.of(
                List.of(new Add(new CardIronclad.Bash())),
                List.of(new Add(new Card.Defend(), new CardSilent.Neutralize(), new CardSilent.Survivor())),
                List.of(new Add(new Card.Defend(), new CardSilent.Neutralize(), new CardSilent.Survivor(), new CardSilent.Acrobatics()))
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
        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.SeverSoul(), 1);
        builder.addCard(new CardIronclad.Clash(), 1);
        builder.addCard(new CardIronclad.Headbutt(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.BurningPactP(), 1);
        builder.addEnemyEncounter(PredefinedEncounter.SENTRIES);
        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.BronzeScales());
        builder.addRelic(new Relic.Vajra());
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
        builder.addEnemyEncounter(PredefinedEncounter.LAGAVULIN);
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
        builder.addEnemyEncounter(PredefinedEncounter.LAGAVULIN);
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
        builder.addEnemyEncounter(PredefinedEncounter.GUARDIAN);
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
        builder.addCard(new CardIronclad.Armaments(), 1);
        builder.addCard(new CardIronclad.Carnage(), 1);
        builder.addCard(new CardIronclad.Metallicize(), 1);
        builder.addCard(new CardIronclad.Shockwave(), 1);
        builder.addCard(new CardIronclad.ShrugItOff(), 1);
        builder.addCard(new CardIronclad.PowerThrough(), 1);
        builder.addEnemyEncounter(PredefinedEncounter.GUARDIAN);
        builder.addRelic(new Relic.AncientTeaSet());
        builder.addRelic(new Relic.DuVuDoll());
        builder.addRelic(new Relic.WarpedTongs());
        var randomization = new StateModificationRandomization(List.of(
                List.of(),
                List.of(new Upgrade(new CardIronclad.ShrugItOff()), new PlayerHealth(19)),
                List.of(new Upgrade(new CardIronclad.Carnage()), new PlayerHealth(19)),
                List.of(new Remove(new CardIronclad.PowerThrough()))
        ));
        builder.setRandomization(randomization);
        builder.setPlayer(new Player(41, 75));
        return new GameState(builder);
    }

    public static GameState BasicInfiniteState() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.BashP(), 1);
        builder.addCard(new CardIronclad.Dropkick(), 2);
        builder.addEnemyEncounter(PredefinedEncounter.GUARDIAN);
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
        builder.addEnemyEncounter(PredefinedEncounter.SLIME_BOSS);
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
        builder.addEnemyEncounter(PredefinedEncounter.SENTRIES);
        builder.addEnemyEncounter(PredefinedEncounter.GREMLIN_NOB);
        builder.addEnemyEncounter(PredefinedEncounter.LAGAVULIN);
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
        builder.addEnemyEncounter(PredefinedEncounter.SENTRIES);
        builder.addEnemyEncounter(PredefinedEncounter.GREMLIN_NOB);
        builder.addEnemyEncounter(PredefinedEncounter.LAGAVULIN);
        builder.addRelic(new Relic.BagOfPreparation());
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(new Add(new CardIronclad.Combust(), new CardIronclad.IronWave())),
                List.of(new Add(new CardIronclad.Combust(), new CardIronclad.DemonForm())),
                List.of(new Add(new CardIronclad.SeeingRed(), new CardIronclad.IronWave())),
                List.of(new Add(new CardIronclad.SeeingRed(), new CardIronclad.DemonForm())),
                List.of(new Add(new CardIronclad.Combust()))
        )).fixR(0, 4);
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
        builder.addEnemyEncounter(PredefinedEncounter.LAGAVULIN);
        builder.addEnemyEncounter(PredefinedEncounter.GREMLIN_NOB);
        builder.setBurningElite();
        var randomization = new StateModificationRandomization(List.of(
                List.of(new Upgrade(new CardIronclad.Inflame())),
                List.of(new Upgrade(new CardIronclad.IronWave())),
                List.of(new Upgrade(new CardIronclad.Anger())),
                List.of(new Upgrade(new CardColorless.DarkShackles())),
                List.of(new Upgrade(new CardIronclad.Combust()))
        ));
        builder.setRandomization(randomization);
        builder.addPotion(new Potion.WeakPotion());
        builder.addPotion(new Potion.LiquidMemories());
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
        builder.addEnemyEncounter(PredefinedEncounter.LAGAVULIN);
        builder.addEnemyEncounter(PredefinedEncounter.GREMLIN_NOB);
        builder.setBurningElite();
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addPotion(new Potion.LiquidMemories());
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
        builder.addEnemyEncounter(PredefinedEncounter.GREMLIN_LEADER);
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
        builder.addEnemyEncounter(PredefinedEncounter.BOOK_OF_STABBING);
        builder.addEnemyEncounter(PredefinedEncounter.SLAVERS_ELITE);
        var randomization = new StateModificationRandomization(List.of(
                List.of(new Upgrade(new CardIronclad.PommelStrike())),
                List.of(new Upgrade(new CardIronclad.BattleTrance())),
                List.of(new Upgrade(new CardIronclad.Combust()))
        ));
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
        builder.addCard(new CardColorless.RitualDaggerP(15, 2), 1);
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.RedMask());
        builder.addRelic(new Relic.AncientTeaSet());
        builder.addRelic(new Relic.HornCleat());
        builder.addRelic(new Relic.BurningBlood());
        builder.addRelic(new Relic.BottledLightning(new CardIronclad.BattleTranceP()));
        builder.addEnemyEncounter(PredefinedEncounter.THE_CHAMP);
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
        builder.addEnemyEncounter(PredefinedEncounter.GUARDIAN);
        builder.addRelic(new Relic.Anchor());
        builder.addRelic(new Relic.Vajra());
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
        builder.addCard(new CardColorless.RitualDaggerP(30, 2), 1);
        builder.addCard(new CardIronclad.BrutalityP(), 1);
        builder.addCard(new CardIronclad.ArmamentP(), 1);
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
        builder.addRelic(new Relic.Vajra());
        builder.addRelic(new Relic.OddlySmoothStone());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.PaperPhrog());
        builder.addRelic(new Relic.ChampionBelt());
        builder.addRelic(new Relic.SacredBark());
        builder.addEnemyEncounter(PredefinedEncounter.AWAKENED_ONE);
        builder.addPotion(new Potion.LiquidMemories());
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
        builder.addCard(new CardIronclad.ArmamentP(), 1);
        builder.addCard(new CardIronclad.Exhume(), 1);
        builder.addCard(new CardIronclad.PowerThrough(), 1);
        builder.addCard(new CardIronclad.Disarm(), 1);
        builder.addCard(new CardIronclad.WildStrike(), 2);
        builder.addCard(new CardIronclad.SecondWind(), 1);
        builder.addCard(new CardIronclad.Inflame(), 1);
        builder.addEnemyEncounter(PredefinedEncounter.GUARDIAN);
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
        builder.addRelic(new Relic.Vajra());
        builder.addEnemyEncounter(PredefinedEncounter.SENTRIES);
        builder.addEnemyEncounter(PredefinedEncounter.GREMLIN_NOB);
        builder.addEnemyEncounter(PredefinedEncounter.LAGAVULIN);
        GameStateRandomization scenarios = new StateModificationRandomization(List.of(
                List.of(new Add(new CardColorless.Finesse())),
                List.of(new Add(new CardIronclad.IronWave()))
        )).doAfter(new StateModificationRandomization(List.of(
                List.of(new PlayerHealth(57)),
                List.of(new Upgrade(new CardIronclad.Anger()), new PlayerHealth(40)),
                List.of(new Upgrade(new CardIronclad.Uppercut()), new PlayerHealth(40))
        )));
        scenarios = scenarios.union(new StateModificationRandomization(List.of(
                List.of(new Add(new Card.Strike()), new PlayerHealth(57))
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
        builder.addCard(new CardIronclad.ArmamentP(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 1);
        builder.addRelic(new Relic.Vajra());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.Nunchaku(2, 2));
        builder.addEnemyEncounter(PredefinedEncounter.SENTRIES);
        //        builder.addEnemyEncounter(PredefinedEncounter.GREMLIN_NOB);
        builder.addEnemyEncounter(PredefinedEncounter.LAGAVULIN);
        builder.setBurningElite();
        builder.addPotion(new Potion.SwiftPotion());
        builder.addPotion(new Potion.LiquidMemories());
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
        builder.addCard(new CardIronclad.ArmamentP(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 1);
        builder.addCard(new CardIronclad.SwordBoomerang(), 1);
        builder.addCard(new CardIronclad.Disarm(), 1);
        builder.addRelic(new Relic.Vajra());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.Nunchaku(8, 2));
        builder.addEnemyEncounter(PredefinedEncounter.HEXAGHOST);
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(new Upgrade(new CardIronclad.PowerThrough())),
                List.of(new Upgrade(new CardIronclad.Disarm()))
        ));
        builder.setRandomization(randomization);
        builder.addPotion(new Potion.LiquidMemories());
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
        builder.addCard(new CardIronclad.ArmamentP(), 1);
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
        builder.addRelic(new Relic.Vajra());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.Nunchaku(7, 2));
        builder.addRelic(new Relic.GremlinHorn());
        builder.addEnemyEncounter(PredefinedEncounter.TRIPLE_BYRDS);
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(),
                List.of(new Add(new CardIronclad.Havoc()))
        ));
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
        builder.addCard(new CardIronclad.ArmamentP(), 1);
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
        builder.addRelic(new Relic.Vajra());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.Nunchaku(9, 8));
        builder.addRelic(new Relic.GremlinHorn());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.HappyFlower(0, 8));
        builder.addRelic(new Relic.HornCleat());
        builder.addEnemyEncounter(PredefinedEncounter.THE_CHAMP);
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
        builder.addCard(new CardIronclad.ArmamentP(), 1);
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
        builder.addRelic(new Relic.Vajra());
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
        builder.addEnemyEncounter(PredefinedEncounter.TIME_EATER);
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
        builder.addCard(new CardIronclad.ArmamentP(), 1);
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
        builder.addRelic(new Relic.Vajra());
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
        builder.addEnemyEncounter(PredefinedEncounter.AWAKENED_ONE);
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
        builder.addCard(new CardIronclad.ArmamentP(), 1);
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
        builder.addRelic(new Relic.Vajra());
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
        builder.addRelic(new Relic.InkBottle(9, 0));
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
//        builder.addPotion(new Potion.SneckoPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.LiquidMemories().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.WeakPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.RegenPotion().setBasePenaltyRatio(100));
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
        builder.addCard(new CardIronclad.ArmamentP(), 1);
        builder.addCard(new CardIronclad.Metallicize(), 1);
        builder.addCard(new CardIronclad.IronWave(), 1);
        builder.addCard(new CardIronclad.ShrugItOff(), 1);
        builder.addCard(new CardIronclad.BattleTrance(), 1);
        builder.addCard(new CardIronclad.ImmolateP(), 1);
        builder.addEnemyEncounter(PredefinedEncounter.SLIME_BOSS);
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(new Add(new CardIronclad.Anger(), new CardIronclad.Headbutt(), new CardIronclad.Evolve())),
                List.of(new Add(new CardIronclad.Anger(), new CardIronclad.Headbutt())),
                List.of(new Add(new CardIronclad.Anger(), new CardIronclad.Evolve())),
                List.of(new Add(new CardIronclad.Headbutt(), new CardIronclad.Evolve())),
                List.of(),
                List.of(new Add(new CardIronclad.Evolve()))
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

    public static GameState TestStateReplayLostRunIronClad() {
        var builder = new GameStateBuilder();
        builder.addCard(new CardIronclad.BashP(), 1);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.StrikeP(), 1);
        builder.addCard(new Card.Defend(), 1);
        builder.addCard(new CardIronclad.PerfectedStrikeP(), 1);
        builder.addCard(new CardIronclad.PommelStrikeP(), 1);
        builder.addCard(new CardIronclad.SeeingRed(), 1);
        builder.addCard(new CardIronclad.AngerP(), 1);
        builder.addCard(new CardIronclad.Headbutt(), 1);
        builder.addCard(new CardIronclad.Rage(), 1);
        builder.addCard(new CardIronclad.RageP(), 1);
        builder.addCard(new CardIronclad.Feed(0), 2);
        builder.addCard(new CardIronclad.CarnageP(), 1);
        builder.addCard(new CardIronclad.WhirlwindP(), 1);
        builder.addCard(new CardIronclad.BloodlettingP(), 1);
        builder.addCard(new CardIronclad.DisarmP(), 2);
        builder.addCard(new CardIronclad.IntimidateP(), 1);
        builder.addCard(new CardIronclad.Barricade(), 1);
        builder.addCard(new CardIronclad.ShrugItOff(), 2);
        builder.addCard(new CardColorless.Apotheosis(), 1);
        builder.addCard(new CardIronclad.Evolve(), 2);
        builder.addCard(new CardIronclad.FeelNoPain(), 1);
        builder.addCard(new CardIronclad.ShockwaveP(), 1);
        builder.addCard(new CardIronclad.Havoc(), 3);
        builder.addCard(new CardIronclad.RuptureP(), 1);
        builder.addCard(new CardIronclad.InflameP(), 1);
        builder.addCard(new CardIronclad.MetallicizeP(), 2);
        builder.addRelic(new Relic.BurningBlood());
        builder.addRelic(new Relic.MeatOnTheBone());
        builder.addRelic(new Relic.Akabeko());
        builder.addRelic(new Relic.HappyFlower(2, 0));
        builder.addRelic(new Relic.SlaversCollar());
        builder.addRelic(new Relic.SelfFormingClay());
        builder.addRelic(new Relic.CentennialPuzzle());
//        builder.addRelic(new Relic.Toolbox());
        builder.addRelic(new Relic.Toolbox(new CardColorless.Discovery(), new CardColorless.TheBomb(), new CardColorless.SecretWeapon()));
        builder.addRelic(new Relic.PenNib(9, 0));
        builder.addRelic(new Relic.OrnamentalFan());
        builder.addRelic(new Relic.FusionHammer());
        builder.addRelic(new Relic.Turnip());
        builder.addRelic(new Relic.OrangePellets());
        builder.addRelic(new Relic.Anchor());
//        builder.addRelic(new Relic.Pantograph());
        builder.addRelic(new Relic.TheBoot());
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(new Add(new CardIronclad.MetallicizeP())),
                List.of(new Add(new CardIronclad.RecklessChargeP())),
                List.of(new Add(new CardIronclad.Entrench())),
                List.of()
        ));
        builder.setGameStateViaInteractiveMode(List.of("", "0", "c", "1", "rage", "rage+", "ange+", "apo", "see", "e", "1", "exit"));
//        builder.setPreBattleScenarios(randomization);
        builder.addPotion(new Potion.HeartOfIron().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.FairyInABottle().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(1);
        builder.setPlayer(new Player(112, 118, 139));
        return new GameState(builder);
    }

    public static GameState TestStateDefect() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.Strike(), 2);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.Dualcast(), 1);
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
//        builder.addEnemyEncounter(PredefinedEncounter.BOOK_OF_STABBING);
//        builder.addEnemyEncounter(PredefinedEncounter.SLAVERS_ELITE);
        builder.addEnemyEncounter(PredefinedEncounter.GREMLIN_LEADER);
        builder.addRelic(new Relic.CrackedCore());
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
        builder.addCard(new CardDefect.Dualcast(), 1);
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
        builder.addEnemyEncounter(PredefinedEncounter.BOOK_OF_STABBING);
//        builder.addEnemyEncounter(PredefinedEncounter.SLAVERS_ELITE);
//        EnemyEncounter.addGremlinLeaderFight2(builder);
//        builder.addEnemyEncounter(PredefinedEncounter.BRONZE_AUTOMATON);
        builder.addRelic(new Relic.CrackedCore());
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
        builder.addCard(new CardDefect.Dualcast(), 1);
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
        builder.addEnemyEncounter(PredefinedEncounter.DONU_AND_DECA);
//        builder.addEnemyEncounter(PredefinedEncounter.GIANT_HEAD);
//        builder.addEnemyEncounter(PredefinedEncounter.NEMESIS);
//        EnemyEncounter.addReptomancerFight(builder);
//        builder.setBurningElite();
        builder.addRelic(new Relic.CrackedCore());
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.BagOfMarbles());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.RunicDome());
        builder.addRelic(new Relic.Torii());
        builder.addRelic(new Relic.RunicCapacitor());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.IncenseBurner(5, Relic.IncenseBurner.NEXT_FIGHT_IS_SPEAR_AND_SHIELD_REWARD));
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
        builder.addCard(new CardDefect.Dualcast(), 1);
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
        builder.addEnemyEncounter(PredefinedEncounter.SPEAR_AND_SHIELD);
        builder.addRelic(new Relic.CrackedCore());
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.BagOfMarbles());
        builder.addRelic(new Relic.Lantern());
        builder.addRelic(new Relic.RunicDome());
        builder.addRelic(new Relic.Torii());
        builder.addRelic(new Relic.RunicCapacitor());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.IncenseBurner(4));
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
        builder.addCard(new CardDefect.Dualcast(), 1);
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
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        builder.addRelic(new Relic.CrackedCore());
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
        builder.addCard(new CardDefect.DualcastP(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
        builder.addCard(new CardDefect.BallLightning(), 2);
        builder.addCard(new CardDefect.Skim(), 2);
        builder.addCard(new CardDefect.MelterP(), 1);
        builder.addCard(new CardDefect.BeamCellP(), 1);
        builder.addCard(new CardDefect.BiasedCognitionP(),1);
        builder.addCard(new CardDefect.Coolheaded(), 1);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addEnemyEncounter(PredefinedEncounter.GUARDIAN);
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(new Upgrade(new CardDefect.Coolheaded())),
                List.of(new Upgrade(new CardDefect.Glacier())),
                List.of(new Upgrade(new CardDefect.Zap())),
                List.of(new PlayerHealth(29))
        )).setDescriptions(
                "Health 8, Upgrade Coolheaded",
                "Health 8, Upgrade Glacier",
                "Health 8, Upgrade Zap",
                "Health 29"
        );
        builder.setRandomization(randomization);
        builder.addPotion(new Potion.SwiftPotion());
        builder.addRelic(new Relic.CrackedCore());
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
        builder.addCard(new CardDefect.Dualcast(), 1);
        builder.addCard(new CardDefect.BeamCell(), 1);
        builder.addCard(new CardDefect.BootSequence(), 1);
        builder.addCard(new CardDefect.DoomAndGloomP(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
        builder.addEnemyEncounter(PredefinedEncounter.LAGAVULIN);
        builder.addPotion(new Potion.FocusPotion());
        builder.addRelic(new Relic.CrackedCore());
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
        builder.addEnemyEncounter(PredefinedEncounter.DONU_AND_DECA);
        var startOfGameScenarios = new StateModificationRandomization(List.of(
                List.of(),
                List.of(new Add(new CardSilent.Deflect())),
                List.of(new Add(new CardSilent.Deflect(), new CardSilent.Blur()))
        ));
        builder.setPreBattleScenarios(startOfGameScenarios);
        builder.addPotion(new Potion.SwiftPotion().setBasePenaltyRatio(90));
        builder.addPotion(new Potion.BlockPotion().setBasePenaltyRatio(90));
        builder.addRelic(new Relic.MummifiedHand());
        builder.addRelic(new Relic.RingOfTheSnake());
        builder.addRelic(new Relic.Shuriken());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.SlaversCollar());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.SneckoSkull());
        builder.addRelic(new Relic.PaperKrane());
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
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        builder.setPotionsScenarios(1);
        builder.addPotion(new Potion.SwiftPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.BlockPotion().setBasePenaltyRatio(100));
//        builder.setGameStateViaInteractiveMode(List.of("rng off", "c", "1", "def", "mal+", "def", "cal", "pha+", "legswe+", "e", "0", "1", "exit"));
        builder.addRelic(new Relic.MummifiedHand());
        builder.addRelic(new Relic.RingOfTheSnake());
        builder.addRelic(new Relic.Shuriken());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.SlaversCollar());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.SneckoSkull());
        builder.addRelic(new Relic.PaperKrane());
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
        builder.addCard(new CardDefect.Dualcast(), 1);
        builder.addCard(new CardDefect.BiasedCognition(), 2);
        builder.addCard(new CardDefect.BiasedCognitionP(), 1);
        builder.addCard(new CardDefect.SteamBarrier(), 1);
        builder.addCard(new CardDefect.DoomAndGloom(), 1);
        builder.addCard(new CardDefect.CompileDriver(), 1);
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
        builder.addEnemyEncounter(PredefinedEncounter.COLLECTOR);
        builder.setGameStateViaInteractiveMode(List.of("", "c", "1", "dua", "bi", "zap", "strea2", "e", "0", "exit"));
        builder.addPotion(new Potion.DexterityPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.WeakPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(3);
        builder.setPlayer(new Player(53, 53));
        builder.addRelic(new Relic.CrackedCore());
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
        builder.addCard(new CardDefect.Dualcast(), 1);
        builder.addCard(new CardDefect.BiasedCognition(), 1);
        builder.addCard(new CardDefect.BiasedCognitionP(), 2);
        builder.addCard(new CardDefect.SteamBarrier(), 1);
        builder.addCard(new CardDefect.DoomAndGloom(), 1);
        builder.addCard(new CardDefect.CompileDriver(), 1);
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
//        builder.addEnemyEncounter(PredefinedEncounter.SPEAR_AND_SHIELD);
//        builder.addEnemyEncounter(PredefinedEncounter.DONU_AND_DECA);
//        builder.addEnemyEncounter(PredefinedEncounter.AWAKENED_ONE);
        builder.addEnemyEncounter(PredefinedEncounter.TIME_EATER);
        builder.setGameStateViaInteractiveMode(List.of("", "c", "1", "def", "coo", "com", "p", "compi", "loop+", "e", "rng off", "0", "c", "4", "2", "exit"));
        builder.addPotion(new Potion.BlessingOfTheForge().setBasePenaltyRatio(90));
        builder.addPotion(new Potion.FairyInABottle().setBasePenaltyRatio(90));
        builder.setPotionsScenarios(1);
        builder.setPlayer(new Player(80, 80));
        builder.addRelic(new Relic.CrackedCore());
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
        builder.addCard(new CardDefect.Dualcast(), 1);
        builder.addCard(new CardDefect.BiasedCognition(), 1);
        builder.addCard(new CardDefect.BiasedCognitionP(), 2);
        builder.addCard(new CardDefect.SteamBarrier(), 1);
        builder.addCard(new CardDefect.DoomAndGloom(), 1);
        builder.addCard(new CardDefect.CompileDriver(), 1);
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
        builder.addEnemyEncounter(PredefinedEncounter.SPEAR_AND_SHIELD);
//        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        builder.setGameStateViaInteractiveMode(List.of("", "rng off", "0", "19", "18", "13", "c", "4", "1", "0", "c", "4", "0", "1", "exit"));
        builder.addPotion(new Potion.BlessingOfTheForge().setBasePenaltyRatio(90));
        builder.addPotion(new Potion.FairyInABottle().setBasePenaltyRatio(90));
        builder.setPotionsScenarios(1);
        builder.setPlayer(new Player(88, 98));
        builder.addRelic(new Relic.CrackedCore());
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
        builder.addCard(new CardDefect.Dualcast(), 1);
        builder.addCard(new CardDefect.BiasedCognition(), 1);
        builder.addCard(new CardDefect.BiasedCognitionP(), 2);
        builder.addCard(new CardDefect.SteamBarrier(), 1);
        builder.addCard(new CardDefect.DoomAndGloom(), 1);
        builder.addCard(new CardDefect.CompileDriver(), 1);
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
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
//        builder.setGameStateViaInteractiveMode(List.of("", "rng off", "0", "19", "18", "13", "c", "4", "1", "0", "c", "4", "0", "1", "exit"));
        builder.addPotion(new Potion.BlessingOfTheForge().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.FairyInABottle().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(1);
        builder.setPlayer(new Player(63, 63));
        builder.addRelic(new Relic.CrackedCore());
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
        builder.addCard(new CardDefect.DualcastP(), 1);
        builder.addCard(new CardDefect.CoolheadedP(), 2);
        builder.addCard(new CardDefect.SweepingBeamP(), 1);
        builder.addCard(new CardDefect.BallLightning(), 1);
        builder.addCard(new CardDefect.CompileDriver(), 2);
        builder.addCard(new CardDefect.CompileDriverP(), 1);
        builder.addCard(new CardDefect.Equilibrium(), 1);
        builder.addCard(new CardDefect.BeamCellP(), 1);
        builder.addCard(new CardDefect.BufferP(), 1);
        builder.addCard(new CardDefect.Sunder(), 1);
        builder.addCard(new CardDefect.MachineLearning(), 1);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addCard(new CardDefect.Capacitor(), 1);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.HologramP(), 1);
        builder.addCard(new CardDefect.GoForTheEyes(), 1);
        builder.addCard(new CardDefect.CoreSurge(), 1);
        builder.addCard(new CardDefect.SelfRepair(), 1);
        builder.addCard(new CardDefect.ForceField(), 1);
        builder.addCard(new CardDefect.AggregateP(), 1);
        builder.addCard(new CardDefect.LoopP(), 1);
        builder.addCard(new CardDefect.StackP(), 1);
        builder.addCard(new CardDefect.Stack(), 2);
        builder.setPlayer(new Player(63, 77));
//        builder.addEnemyEncounter(PredefinedEncounter.AWAKENED_ONE);
//        builder.addEnemyEncounter(PredefinedEncounter.DONU_AND_DECA);
        builder.setGameStateViaInteractiveMode(List.of("", "rng off", "c", "1", "def", "sta", "def", "coo+", "tur", "agg+", "com+", "holo+", "mac", "e", "0", "1", "2", "2", "2", "0", "0", "1", "3", "1", "exit"));
//        builder.addPotion(new Potion.FocusPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.DuplicationPotion().setBasePenaltyRatio(95));
        builder.setPotionsScenarios(1);
        builder.addRelic(new Relic.CrackedCore());
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
        builder.addCard(new CardDefect.Dualcast(), 1);
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
        builder.addEnemyEncounter(PredefinedEncounter.TRIPLE_BYRDS);
//        builder.addEnemyEncounter(PredefinedEncounter.BOOK_OF_STABBING);
//        builder.addEnemyEncounter(PredefinedEncounter.SLAVERS_ELITE);
//        EnemyEncounter.addGremlinLeaderFight2(builder);
        builder.setGameStateViaInteractiveMode(List.of("", "rng off", "c", "1", "con", "zap", "def", "rain", "str", "co", "p", "coo", "dua", "e", "0", "1", "3", "2", "3", "2", "1", "2", "c", "4", "0", "1", "c", "4", "1", "1", "c", "4", "2", "1", "c", "3", "0", "27", "c", "3", "1", "33", "c", "3", "2", "28", "exit"));
        builder.addPotion(new Potion.FirePotion().setBasePenaltyRatio(90));
        builder.addPotion(new Potion.EssenceOfSteel().setBasePenaltyRatio(90));
        builder.setPotionsScenarios(3);
        builder.addRelic(new Relic.CrackedCore());
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
        builder.addEnemyEncounter(PredefinedEncounter.BOOK_OF_STABBING);
        builder.addEnemyEncounter(PredefinedEncounter.SLAVERS_ELITE);
        //        EnemyEncounter.addGremlinLeaderFight2(builder);
        var startOfGameScenarios = new StateModificationRandomization(List.of(
                List.of(new Upgrade(new CardSilent.Terror())),
                List.of(new Add(new CardSilent.CloakAndDagger()), new Upgrade(new CardSilent.CloakAndDagger())),
                List.of(new Add(new CardSilent.CloakAndDagger()), new Upgrade(new CardSilent.Terror()))
        ));
        builder.setPreBattleScenarios(startOfGameScenarios);
        builder.addRelic(new Relic.MummifiedHand());
        builder.addRelic(new Relic.RingOfTheSnake());
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
        builder.addCard(new CardDefect.CompileDriver(), 1);
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
        builder.addCard(new CardDefect.GoForTheEyesP(), 1);
        builder.addCard(new CardDefect.DefragmentP(), 1);
        builder.addCard(new CardDefect.Heatsinks(), 1);
        builder.addCard(new CardDefect.SeekP(), 1);
        builder.addCard(new CardDefect.SelfRepair(), 1);
        builder.addCard(new CardDefect.HologramP(), 1);
        builder.addCard(new CardDefect.TurboP(), 1);
        builder.addCard(new CardDefect.GlacierP(), 1);
        builder.addCard(new CardOther.Writhe(), 1);
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        builder.addPotion(new Potion.PowerPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.WeakPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(3);
        builder.addRelic(new Relic.CrackedCore());
        builder.addRelic(new Relic.BronzeScales());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.Nunchaku(9, 0));
        builder.addRelic(new Relic.BlueCandle());
        builder.addRelic(new Relic.MummifiedHand());
        builder.addRelic(new Relic.Vajra());
        builder.addRelic(new Relic.Shuriken());
        builder.addRelic(new Relic.Anchor());
        builder.addRelic(new Relic.StrikeDummy());
        builder.addRelic(new Relic.RunicPyramid());
        builder.addRelic(new Relic.RunicCapacitor());
        builder.addRelic(new Relic.Sundial(2, 0));
        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.StoneCalendar());
        builder.setGameStateViaInteractiveMode(List.of("", "0", "rng off", "c", "1", "seek+", "mete", "core", "skim", "comp", "glac", "e", "0", "exit"));
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(new Add(new CardDefect.Skim())),
                List.of()
        ));
        builder.setPreBattleScenarios(randomization);
        builder.setPlayer(new Player(71, 71));
        return new GameState(builder);
    }

    public static GameState TestStateDefectRun5() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.Dualcast(), 1);
        builder.addCard(new Card.Defend(), 3);
        builder.addCard(new Card.DefendP(), 1);
        builder.addCard(new CardDefect.SweepingBeam(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.ColdSnapP(), 1);
        builder.addCard(new CardDefect.HyperbeamP(), 1);
        builder.addCard(new CardDefect.DefragmentP().setInnate(true), 1);
        builder.addCard(new CardDefect.SelfRepair(), 2);
        builder.addCard(new CardDefect.ChargeBattery(), 2);
        builder.addCard(new CardDefect.Rebound(), 1);
        builder.addCard(new CardDefect.BiasedCognitionP(), 3);
        builder.addCard(new CardDefect.Sunder(), 1);
        builder.addCard(new CardDefect.Seek(), 1);
        builder.addCard(new CardDefect.FTLP(), 1);
        builder.addCard(new CardDefect.FTL(), 1);
        builder.addCard(new CardDefect.Equilibrium(), 1);
        builder.addCard(new CardDefect.MachineLearningP(), 1);
        builder.addCard(new CardDefect.CompileDriverP(), 1);
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
        builder.addRelic(new Relic.CrackedCore());
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
        builder.setGameStateViaInteractiveMode(List.of("", "c", "1", "bias+", "think", "dua", "sel", "def", "e", "exit"));
//        builder.addRelic(new Relic.BiasedCognitionLimit());
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        builder.addPotion(new Potion.AttackPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.FirePotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.DexterityPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.LizardTail().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(7);
        builder.setPlayer(new Player(39, 54));
        return new GameState(builder);
    }

    public static GameState TestStateDefectRun6() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.Dualcast(), 1);
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
        builder.addCard(new CardDefect.CompileDriverP(), 1);
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
        builder.addRelic(new Relic.CrackedCore());
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
        builder.addPotion(new Potion.FairyInABottle().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.EntropicBrew().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(7);
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(new Add(new CardDefect.StormP())),
                List.of(new Add(new CardDefect.BeamCellP())),
                List.of()
        ));
        builder.setPreBattleScenarios(randomization);
        builder.setGameStateViaInteractiveMode(List.of("2", "c", "1", "rec", "sun+", "cons", "def+", "zap+", "cold+", "defra", "e", "0", "exit"));
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
        builder.addCard(new CardDefect.GoForTheEyesP(), 1);
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
        builder.addRelic(new Relic.CrackedCore());
        builder.addRelic(new Relic.SlingOfCourage());
        builder.addRelic(new Relic.IceCream());
        builder.addRelic(new Relic.DataDisk());
        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.BloodVial());
        builder.addRelic(new Relic.PenNib(7, 0));
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.RunicPyramid());
        builder.addRelic(new Relic.Akabeko());
        builder.addRelic(new Relic.Vajra());
        builder.addRelic(new Relic.MedicalKit());
        builder.addRelic(new Relic.TungstenRod());
        builder.addPotion(new Potion.LizardTail().setBasePenaltyRatio(100));
//        builder.addPotion(new Potion.DuplicationPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.PotionOfCapacity().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.LiquidMemories().setBasePenaltyRatio(100));
//        builder.addPotion(new Potion.DrawPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(3);
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
//        EnemyEncounter.addShieldAndSpearFollowByHeartFight(builder);
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(new Add(new CardDefect.Capacitor())),
                List.of(new Add(new CardDefect.BarrageP())),
                List.of(new Add(new CardDefect.MultiCastP())),
                List.of()
        ));
        builder.setPreBattleScenarios(randomization);
        builder.setPlayer(new Player(83, 83));
        builder.setGameStateViaInteractiveMode(List.of("", "1", "c", "1", "bea", "co", "co", "p", "p", "cold", "col+", "char", "e", "0", "exit"));
        return new GameState(builder);
    }

    public static GameState TestStateDefectRun8() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.Zap(), 1);
        builder.addCard(new CardDefect.Dualcast(), 1);
        builder.addCard(new Card.Strike(), 1);
        builder.addCard(new Card.StrikeP(), 1);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.Rebound(), 1);
        builder.addCard(new CardDefect.Sunder(), 1);
        builder.addCard(new CardDefect.BeamCellP(), 1);
        builder.addCard(new CardDefect.SelfRepair(), 2);
        builder.addCard(new CardDefect.FTL(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
        builder.addCard(new CardDefect.SeekP(), 2);
        builder.addCard(new CardDefect.CoreSurge(), 1);
        builder.addCard(new CardDefect.CoolheadedP(), 1);
        builder.addCard(new CardDefect.GoForTheEyes(), 1);
        builder.addCard(new CardDefect.ConsumeP(), 1);
        builder.addCard(new CardDefect.DoomAndGloom(), 1);
        builder.addCard(new CardDefect.BiasedCognitionP(), 1);
        builder.addCard(new CardDefect.MeteorStrike(), 1);
        builder.addCard(new CardDefect.Fission(), 1);
        builder.addCard(new CardDefect.Overclock(), 1);
        builder.addCard(new CardDefect.StaticDischarge(), 1);
        builder.addCard(new CardDefect.ReinforcedBody(), 1);
        builder.addCard(new CardDefect.Coolheaded(), 1);
        builder.addCard(new CardDefect.EchoForm(), 1);
        builder.addCard(new CardDefect.Capacitor(), 1);
        builder.addCard(new CardColorless.ApotheosisP(), 1);
        builder.addRelic(new Relic.CrackedCore());
        builder.addRelic(new Relic.IceCream());
        builder.addRelic(new Relic.Akabeko());
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.UnceasingTop());
        builder.addRelic(new Relic.MummifiedHand());
        builder.addRelic(new Relic.Ectoplasm());
        builder.addRelic(new Relic.RunicPyramid());
        builder.addRelic(new Relic.EmotionChip());
        builder.addRelic(new Relic.TheBoot());
        builder.addRelic(new Relic.BloodVial());
        builder.addRelic(new Relic.IncenseBurner(4));
        builder.addRelic(new Relic.Shuriken());
        builder.addRelic(new Relic.ArtOfWar());
//        builder.addRelic(new Relic.BiasedCognitionLimit());
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        builder.addPotion(new Potion.FocusPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.FocusPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.RegenPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.DrawPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(15);
        builder.setGameStateViaInteractiveMode(List.of("", "c", "1", "core", "apo", "see", "ec", "bea", "e", "0", "exit"));
        builder.setPlayer(new Player(67, 67));
        return new GameState(builder);
    }

    public static GameState TestStateDefectRun9() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.BeamCellP(), 2);
        builder.addCard(new CardDefect.RipAndTearP(), 1);
        builder.addCard(new CardDefect.ReprogramP(), 1);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.DoomAndGloom(), 1);
        builder.addCard(new CardDefect.BufferP(), 1);
        builder.addCard(new CardDefect.Equilibrium(), 1);
        builder.addCard(new CardDefect.Fission(), 1);
        builder.addCard(new CardDefect.FTL(), 1);
        builder.addCard(new CardDefect.Leap(), 1);
        builder.addCard(new CardDefect.Overclock(), 2);
        builder.addCard(new CardDefect.Barrage(), 1);
        builder.addCard(new CardDefect.Heatsinks(), 1);
        builder.addCard(new CardDefect.Hyperbeam(), 1);
        builder.addCard(new CardDefect.HologramP(), 1);
        builder.addCard(new CardDefect.Skim(), 1);
        builder.addCard(new CardDefect.SeekP(), 1);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addCard(new CardDefect.TurboP(), 1);
        builder.addCard(new CardDefect.ScrapeP(), 1);
        builder.addCard(new CardDefect.EquilibriumP(), 1);
        builder.addCard(new CardDefect.ForceField(), 2);
        builder.addCard(new CardDefect.EchoForm(), 2);
        builder.addCard(new CardDefect.Reprogram(), 1);
        builder.addCard(new CardDefect.StackP(), 2);
        builder.addCard(new CardColorless.MasterOfStrategy(), 1);
        builder.addCard(new CardDefect.AllForOne(0, 0), 1);
        builder.addRelic(new Relic.CrackedCore());
        builder.addRelic(new Relic.BagOfMarbles());
        builder.addRelic(new Relic.InkBottle(9, 2));
        builder.addRelic(new Relic.Enchiridion(new CardDefect.Electrodynamics()));
        builder.addRelic(new Relic.HappyFlower(0, 2));
        builder.addRelic(new Relic.DeadBranch());
        builder.addRelic(new Relic.Kunai());
        builder.addRelic(new Relic.OrnamentalFan());
        builder.addRelic(new Relic.SlaversCollar());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.IncenseBurner(4));
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        builder.addPotion(new Potion.DuplicationPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.CultistPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.FairyInABottle().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(3);
        builder.setPlayer(new Player(67, 67));
        builder.setGameStateViaInteractiveMode(List.of("", "c", "1", "rip", "eq", "p", "eq+", "repr", "sta+", "tur", "asc", "ec", "e", "0", "exit"), true);
        return new GameState(builder);
    }

    public static GameState TestStateDefectRun10D() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.Dualcast(), 1);
        builder.addCard(new CardColorless.HandOfGreedP(2), 1);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.Skim(), 1);
        builder.addCard(new CardDefect.SelfRepair(), 1);
        builder.addCard(new CardDefect.BullsEye(), 1);
        builder.addCard(new CardDefect.Coolheaded(), 1);
        builder.addCard(new CardDefect.BeamCellP(), 1);
        builder.addCard(new CardDefect.BiasedCognitionP(), 1);
        builder.addCard(new CardDefect.CreativeAI(), 1);
        builder.addCard(new CardDefect.Scrape(), 1);
        builder.addCard(new CardDefect.RipAndTear(), 1);
        builder.addCard(new CardDefect.BeamCell(), 1);
        builder.addCard(new CardDefect.Coolheaded(), 1);
        builder.addCard(new CardDefect.GoForTheEyes(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.EchoForm(), 1);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addCard(new CardDefect.SkimP(), 1);
        builder.addCard(new CardDefect.CoreSurge(), 1);
        builder.addCard(new CardDefect.DoomAndGloomP(), 1);
        builder.addCard(new CardDefect.Chill(), 1);
        builder.addCard(new CardDefect.Defragment(), 1);
        builder.addCard(new CardDefect.Coolheaded(), 1);
        builder.addRelic(new Relic.CrackedCore());
        builder.addRelic(new Relic.BirdFacedUrn());
        builder.addRelic(new Relic.EmotionChip());
        builder.addRelic(new Relic.DataDisk());
        builder.addRelic(new Relic.MeatOnTheBone());
        builder.addRelic(new Relic.PenNib(9, 2));
        builder.addRelic(new Relic.WarpedTongs());
        builder.addRelic(new Relic.CoffeeDripper());
        builder.addRelic(new Relic.ArtOfWar());
        builder.addRelic(new Relic.Anchor());
        builder.addRelic(new Relic.BiasedCognitionLimit());
        builder.addEnemyEncounter(PredefinedEncounter.TIME_EATER);
//        builder.addEnemyEncounter(PredefinedEncounter.SPEAR_AND_SHIELD);
//        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        builder.setGameStateViaInteractiveMode(List.of("", "rng off", "c", "1", "go", "tur", "bull", "self+", "p", "self", "bea", "e", "0", "0", "3", "c", "4", "0", "exit"));
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(new Add(new CardDefect.Coolheaded())),
                List.of(new Add(new CardDefect.AllForOne(0, 0))),
                List.of()
        ));
//        builder.setPreBattleScenarios(randomization);
        builder.addPotion(new Potion.SwiftPotion().setBasePenaltyRatio(90));
        builder.addPotion(new Potion.PowerPotion().setBasePenaltyRatio(90));
        builder.setPotionsScenarios(3);
        builder.setPlayer(new Player(73, 73));
        return new GameState(builder);
    }

    public static GameState TestStateDefectRun10() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.ZapP(), 1);
        builder.addCard(new CardDefect.Dualcast(), 1);
        builder.addCard(new CardColorless.HandOfGreedP(0), 1);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.Skim(), 1);
        builder.addCard(new CardDefect.SelfRepair(), 1);
        builder.addCard(new CardDefect.BullsEye(), 1);
        builder.addCard(new CardDefect.Coolheaded(), 1);
        builder.addCard(new CardDefect.BeamCellP(), 1);
        builder.addCard(new CardDefect.BiasedCognitionP(), 1);
        builder.addCard(new CardDefect.CreativeAI(), 1);
        builder.addCard(new CardDefect.Scrape(), 1);
        builder.addCard(new CardDefect.RipAndTear(), 1);
        builder.addCard(new CardDefect.BeamCell(), 1);
        builder.addCard(new CardDefect.Coolheaded(), 1);
        builder.addCard(new CardDefect.GoForTheEyes(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 1);
        builder.addCard(new CardDefect.EchoForm(), 1);
        builder.addCard(new CardDefect.Turbo(), 1);
        builder.addCard(new CardDefect.SkimP(), 1);
        builder.addCard(new CardDefect.CoreSurge(), 1);
        builder.addCard(new CardDefect.Chill(), 1);
        builder.addCard(new CardDefect.DefragmentP(), 1);
        builder.addCard(new CardDefect.Coolheaded(), 1);
        builder.addCard(new CardColorless.Apotheosis(), 1);
        builder.addCard(new CardDefect.Equilibrium(), 1);
        builder.addCard(new CardDefect.TurboP(), 1);
        builder.addCard(new CardDefect.Storm(), 1);
        builder.addRelic(new Relic.CrackedCore());
        builder.addRelic(new Relic.BirdFacedUrn());
        builder.addRelic(new Relic.EmotionChip());
        builder.addRelic(new Relic.DataDisk());
        builder.addRelic(new Relic.MeatOnTheBone());
        builder.addRelic(new Relic.PenNib(9, 2));
        builder.addRelic(new Relic.WarpedTongs());
        builder.addRelic(new Relic.CoffeeDripper());
        builder.addRelic(new Relic.ArtOfWar());
        builder.addRelic(new Relic.Anchor());
        builder.addRelic(new Relic.UnceasingTop());
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        builder.setGameStateViaInteractiveMode(List.of("", "c", "1", "echo", "cool", "bull", "apo", "sto+", "p", "sto", "e", "rng off", "0", "4", "exit"), true);
        builder.addPotion(new Potion.SwiftPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.PowerPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(3);
        builder.setPlayer(new Player(81, 81));
        return new GameState(builder);
    }

    public static GameState TestStateDefectRun11() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new CardOther.AscendersBane());
        builder.addCard(new CardDefect.Zap());
        builder.addCard(new CardDefect.Dualcast());
        builder.addCard(new CardDefect.MultiCast());
        builder.addCard(new CardDefect.BallLightning(), 2);
        builder.addCard(new CardDefect.SelfRepair());
        builder.addCard(new CardDefect.StreamlineP());
        builder.addCard(new CardDefect.Coolheaded());
        builder.addCard(new CardDefect.ConsumeP());
        builder.addCard(new CardDefect.CoolheadedP(), 3);
        builder.addCard(new CardDefect.Rebound());
        builder.addCard(new CardDefect.CoreSurge());
        builder.addCard(new CardDefect.DarknessP());
        builder.addCard(new CardDefect.RebootP());
        builder.addCard(new CardDefect.Capacitor());
        builder.addCard(new CardDefect.DefragmentP());
        builder.addCard(new CardDefect.ChargeBatteryP());
        builder.addCard(new CardDefect.Sunder());
        builder.addCard(new CardColorless.TripP());
        builder.addCard(new CardDefect.RecursionP(), 2);
        builder.addCard(new CardDefect.ColdSnapP());
        builder.addCard(new CardDefect.CompileDriverP());
        builder.addCard(new CardDefect.GoForTheEyesP());
        builder.addCard(new CardDefect.HologramP(), 3);
        builder.addCard(new CardDefect.Electrodynamics());
        builder.addCard(new CardDefect.WhiteNoiseP());
        builder.addCard(new CardDefect.BootSequenceP());
        builder.addCard(new CardDefect.RecycleP());
        builder.addCard(new CardDefect.ReinforcedBodyP());
        builder.addCard(new CardDefect.Loop());
        builder.addCard(new CardDefect.AllForOne(0 ,0));
        builder.addCard(new Card.Strike(), 2);
        builder.addCard(new Card.Defend(), 4);
        builder.addRelic(new Relic.CrackedCore());
        builder.addRelic(new Relic.CursedKey());
        builder.addRelic(new Relic.Pocketwatch());
        builder.addRelic(new Relic.FusionHammer());
        builder.addRelic(new Relic.Toolbox(new CardColorless.Purity(), new CardColorless.Apotheosis(), new CardColorless.HandOfGreed(2)));
        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.FrozenEye());
        builder.setGenerateCardOptions(GameProperties.GENERATE_CARD_APOTHEOSIS);
        builder.setGameStateViaInteractiveMode(List.of("", "rng off", "0", "defend", "consume+", "defend", "cold snap+", "sunder", "multi-cast", "defend", "dual cast", "streamline+", "ball lightning", "holog+", "chargebatt+", "recursion+", "ball light", "white noise+", "coolhead+", "recycle+", "strike", "holo+", "defragmen+", "defend", "holo+", "gofortheeye+", "capacitor", "coresurge", "ascender", "selfrepair", "reinforce", "loop", "reboot+", "rebound", "recurs+", "coolheade+", "darkness+", "coolheade+", "strike", "coolhea", "electr", "allfor", "zap", "compile", "trip", "0", "0", "0", "0", "0", "1", "1", "e", "7", "32", "26", "24", "19", "c", "4", "2", "2", "2", "5", "2", "e", "6", "2", "6", "2", "3", "9", "0", "5", "7", "0", "3", "5", "1", "5", "0", "0", "e", "3", "0", "exit"));
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        builder.addPotion(new Potion.DuplicationPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(1);
        builder.setPlayer(new Player(67, 76));
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
        builder.addRelic(new Relic.RingOfTheSnake());
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
        builder.addRelic(new Relic.PaperKrane());
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
//        EnemyEncounter.addShieldAndSpearFollowByHeartFight(builder);
        builder.addPotion(new Potion.DuplicationPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.ColorlessPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(3);
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(new Add(new CardSilent.Adrenaline())),
                List.of(new Add(new CardSilent.RiddleWithHoles()))
        ));
        builder.setPreBattleScenarios(randomization);
        builder.setPlayer(new Player(74, 74));
        builder.setGameStateViaInteractiveMode(List.of("", "0", "rng off", "0", "expertise+", "strike+", "predator+", "footwork", "predator+", "finishe+", "panic", "adrenaline", "dash+", "neutral", "piercing+", "survivor", "blade+", "blade", "defend+", "defend+", "calculated", "accuracy+", "legsweeo", "legsweep", "blade+", "blade+", "quickslash+", "12", "defend+", "9", "dopple", "parasite", "foot", "welllaid", "sucker", "malaise+", "defend+", "strike+", "bullet", "defend+", "ascender", "0", "exit"));
        return new GameState(builder);
    }

    public static GameState TestStateSilentRun3() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new Card.Defend(), 3);
        builder.addCard(new CardSilent.Neutralize(), 1);
        builder.addCard(new CardSilent.Survivor(), 1);
        builder.addCard(new CardSilent.QuickSlash(), 1);
        builder.addCard(new CardSilent.DeadlyPoisonP(), 1);
        builder.addCard(new CardSilent.Backflip(), 3);
        builder.addCard(new CardSilent.EscapePlan(), 1);
        builder.addCard(new CardSilent.FlyingKnee(), 1);
        builder.addCard(new CardSilent.Backstab(), 1);
        builder.addCard(new CardSilent.Alchemize(100, 0, 0), 1);
        builder.addCard(new CardSilent.FlyingKneeP(), 1);
        builder.addCard(new CardSilent.BladeDance(), 3);
        builder.addCard(new CardSilent.FootworkP(), 1);
        builder.addCard(new CardSilent.Accuracy(), 2);
        builder.addCard(new CardSilent.AccuracyP(), 1);
        builder.addCard(new CardSilent.Deflect(), 1);
        builder.addCard(new CardSilent.Acrobatics(), 2);
        builder.addCard(new CardSilent.AThousandCuts(), 1);
        builder.addCard(new CardSilent.NoxiousFumes(), 1);
        builder.addCard(new CardSilent.Terror(), 1);
        builder.addCard(new CardSilent.CloakAndDagger(), 1);
        builder.addCard(new CardColorless.DarkShackles(), 1);
        builder.addCard(new CardColorless.ApotheosisP(), 1);
        builder.addCard(new CardOther.CurseOfTheBell(), 1);
        builder.addRelic(new Relic.RingOfTheSnake());
        builder.addRelic(new Relic.ToyOrnithopter());
        builder.addRelic(new Relic.ArtOfWar());
        builder.addRelic(new Relic.Nunchaku(9, 2));
        builder.addRelic(new Relic.ThreadAndNeedle());
        builder.addRelic(new Relic.GremlinHorn());
        builder.addRelic(new Relic.BronzeScales());
        builder.addRelic(new Relic.DuVuDoll());
        builder.addRelic(new Relic.OrnamentalFan());
        builder.addRelic(new Relic.FusionHammer());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.Orichalcum());
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        builder.addPotion(new Potion.PoisonPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.EntropicBrew(Potion.PotionGenerator.UPRGADE_POTIONS | Potion.PotionGenerator.POWER_POTION).setBasePenaltyRatio(100));
        builder.addPotion(new Potion.LizardTail().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(3);
        builder.setGameStateViaInteractiveMode(List.of("", "c", "1", "backfl", "cloak", "curse of", "deflec", "blade", "surviv", "flying", "foo+", "e", "0", "exit"));
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(new Add(new CardSilent.CalculatedGambleP()), new PlayerHealth(31)),
                List.of(new Add(new CardSilent.Burst()), new PlayerHealth(31)),
                List.of()
        ));
//        builder.setPreBattleScenarios(randomization);
        builder.setPlayer(new Player(33, 78));
        return new GameState(builder);
    }

    public static GameState TestStateSilentRun4() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardSilent.Flechettes(), 1);
        builder.addCard(new CardSilent.CloakAndDagger(), 2);
        builder.addCard(new CardSilent.DeadlyPoison(), 1);
        builder.addCard(new CardSilent.Backstab(), 1);
        builder.addCard(new CardSilent.Neutralize(), 1);
        builder.addCard(new CardSilent.Survivor(), 1);
        builder.addCard(new CardSilent.BladeDanceP(), 2);
        builder.addCard(new CardSilent.PoisonedStabP(), 1);
        builder.addCard(new CardSilent.AccuracyP(), 1);
        builder.addCard(new CardSilent.ToolsOfTheTrade(), 2);
        builder.addCard(new CardSilent.EnvenomP(), 1);
        builder.addCard(new CardSilent.DieDieDie(), 1);
        builder.addCard(new CardSilent.Doppelganger(), 1);
        builder.addCard(new CardSilent.Tactician(), 1);
        builder.addCard(new CardSilent.SneakyStrike(), 1);
        builder.addCard(new CardSilent.Finisher(), 1);
        builder.addCard(new CardSilent.PiercingWail(), 1);
        builder.addCard(new CardSilent.Catalyst(), 1);
        builder.addCard(new CardSilent.Malaise(), 1);
        builder.addCard(new CardSilent.MalaiseP(), 1);
        builder.addCard(new CardSilent.CalculatedGamble(), 1);
        builder.addCard(new CardSilent.EscapePlanP(), 1);
        builder.addCard(new CardSilent.LegSweep(), 1);
        builder.addCard(new CardSilent.BackflipP(), 2);
        builder.addCard(new CardSilent.FootworkP(), 1);
        builder.addCard(new CardColorless.ApotheosisP(), 1);
        builder.addCard(new CardColorless.Apparition(), 3);
        builder.addRelic(new Relic.RingOfTheSnake());
        builder.addRelic(new Relic.Anchor());
        builder.addRelic(new Relic.NinjaScroll());
        builder.addRelic(new Relic.BronzeScales());
        builder.addRelic(new Relic.Sundial(1, 0));
        builder.addRelic(new Relic.RunicPyramid());
//        builder.addRelic(new Relic.BloodVial());
        builder.addRelic(new Relic.SneckoSkull());
        builder.addRelic(new Relic.HornCleat());
        builder.addRelic(new Relic.CaptainsWheel());
        builder.addRelic(new Relic.PenNib(8, 0));
        builder.addRelic(new Relic.FrozenEye());
        builder.addRelic(new Relic.MercuryHourglass());
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        builder.addPotion(new Potion.DrawPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.LiquidBronze().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.PowerPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(5);
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(new Add(new CardSilent.CloakAndDaggerP())),
                List.of(new PlayerHealth(61))
        ));
        builder.setPreBattleScenarios(randomization);
        builder.setGameStateViaInteractiveMode(List.of("1", "", "rng off", "0", "blade+", "31", "5", "ascender", "malaise", "neutra", "blade+", "cataly", "piercing", "tactician", "calculated", "apparition", "finisher", "survivor", "dopple", "diedie", "backflip+", "envenom+", "cloa", "cloa", "esca", "deadly", "malaise+", "backflip+", "footwork+", "flet", "accura+", "apotheos+", "tool", "legswee", "apotheo+", "tool", "poi", "snea", "exit"));
        builder.setPlayer(new Player(59, 61));
        return new GameState(builder);
    }

    public static GameState TestStateSilentRun5() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardSilent.Neutralize(), 1);
        builder.addCard(new CardSilent.Survivor(), 1);
        builder.addCard(new CardSilent.SkewerP(), 1);
        builder.addCard(new CardSilent.QuickSlash(), 1);
        builder.addCard(new CardSilent.DaggerSpray(), 1);
        builder.addCard(new CardSilent.EndlessAgony(), 1);
        builder.addCard(new CardSilent.Dash(), 1);
        builder.addCard(new CardSilent.DaggerThrowP(), 1);
        builder.addCard(new CardSilent.PoisonedStabP(), 1);
        builder.addCard(new CardSilent.DodgeAndRoll(), 1);
        builder.addCard(new Card.Strike(), 5);
        builder.addCard(new Card.Defend(), 5);
        builder.addRelic(new Relic.RingOfTheSnake());
        builder.addRelic(new Relic.StoneCalendar());
        builder.addRelic(new Relic.Sundial(0, 0));
        builder.addEnemyEncounter(PredefinedEncounter.GUARDIAN);
        builder.addPotion(new Potion.SpeedPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.AttackPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(3);
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(new Add(new CardSilent.BouncingFlask()), new Remove(new Card.Strike())),
                List.of(new Add(new CardSilent.BouncingFlask()), new Remove(new Card.Defend())),
                List.of(new Remove(new Card.Strike())),
                List.of(new Remove(new Card.Defend()))
        ));
        builder.setPreBattleScenarios(randomization);
        builder.setPlayer(new Player(36, 36));
        return new GameState(builder);
    }

    public static GameState TestStateSilentRun6() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 5);
        builder.addCard(new CardSilent.Survivor(), 1);
        builder.addCard(new CardSilent.TerrorP(), 1);
        builder.addCard(new CardSilent.Choke(), 1);
        builder.addCard(new CardSilent.DaggerSpray(), 2);
        builder.addCard(new CardSilent.PhantasmalKillerP(), 1);
        builder.addCard(new CardSilent.Expertise(), 1);
        builder.addCard(new CardSilent.QuickSlash(), 1);
        builder.addCard(new CardSilent.Slice(), 1);
        builder.addCard(new CardSilent.CorpseExplosionP(), 1);
        builder.addCard(new CardSilent.DoppelgangerP(), 1);
        builder.addCard(new CardSilent.PreparedP(), 1);
        builder.addCard(new CardSilent.FootworkP(), 3);
        builder.addCard(new CardSilent.AcrobaticsP(), 1);
        builder.addCard(new CardSilent.LegSweep(), 2);
        builder.addCard(new CardSilent.PiercingWail(), 1);
        builder.addCard(new CardSilent.Deflect(), 3);
        builder.addCard(new CardSilent.AfterImageP(), 1);
        builder.addCard(new CardSilent.DeflectP(), 1);
        builder.addCard(new CardSilent.DodgeAndRollP(), 1);
        builder.addCard(new CardSilent.CaltropsP(), 1);
        builder.addCard(new CardSilent.Malaise(), 1);
        builder.addCard(new CardSilent.Backflip(), 1);
        builder.addCard(new CardSilent.NoxiousFumesP(), 1);
        builder.addCard(new CardSilent.OutmaneuverP(), 1);
        builder.addCard(new CardSilent.QuickSlashP(), 1);
        builder.addCard(new CardColorless.RitualDagger(54, 0), 1);
        builder.addCard(new CardColorless.GoodInstincts(), 1);
        builder.addRelic(new Relic.RingOfTheSnake());
        builder.addRelic(new Relic.IceCream());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.ArtOfWar());
        builder.addRelic(new Relic.RunicPyramid());
        builder.addRelic(new Relic.FossilizedHelix());
        builder.addRelic(new Relic.Vajra());
        builder.addRelic(new Relic.CursedKey());
        builder.addRelic(new Relic.HornCleat());
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(new Add(new CardSilent.BladeDance())),
                List.of(new Add(new CardSilent.DodgeAndRoll())),
                List.of(new Add(new CardSilent.PiercingWail())),
                List.of(new PlayerHealth(33))
        ));
        builder.setPreBattleScenarios(randomization);
        builder.addPotion(new Potion.FairyInABottle().setBasePenaltyRatio(100));
        builder.setPlayer(new Player(31, 33));
        builder.setGameStateViaInteractiveMode(List.of("", "3", "c", "1", "def", "def", "def", "quick", "str", "exp", "e", "0", "exit"));
        return new GameState(builder);
    }

    public static GameState TestStateSilentRun7() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new Card.StrikeP(), 3);
        builder.addCard(new Card.DefendP(), 3);
        builder.addCard(new CardSilent.Survivor(), 1);
        builder.addCard(new CardSilent.NeutralizeP(), 1);
        builder.addCard(new CardSilent.SneakyStrike(), 1);
        builder.addCard(new CardSilent.QuickSlash(), 1);
        builder.addCard(new CardSilent.NoxiousFumesP(), 1);
        builder.addCard(new CardSilent.BouncingFlaskP(), 1);
        builder.addCard(new CardSilent.Backstab(), 1);
        builder.addCard(new CardSilent.PiercingWailP(), 2);
        builder.addCard(new CardSilent.AcrobaticsP(), 2);
        builder.addCard(new CardSilent.DeflectP(), 3);
        builder.addCard(new CardSilent.NightmareP(), 1);
        builder.addCard(new CardSilent.EndlessAgony(), 1);
        builder.addCard(new CardSilent.ReflexP(), 1);
        builder.addCard(new CardSilent.BackflipP(), 1);
        builder.addCard(new CardSilent.BladeDanceP(), 2);
        builder.addCard(new CardSilent.AlchemizeP(100, 0, 0), 1);
        builder.addCard(new CardSilent.EscapePlanP(), 2);
        builder.addCard(new CardSilent.GrandFinale(), 1);
        builder.addCard(new CardSilent.PreparedP(), 2);
        builder.addCard(new CardColorless.TripP(), 1);
        builder.addRelic(new Relic.RingOfTheSnake());
        builder.addRelic(new Relic.Kunai());
        builder.addRelic(new Relic.Ectoplasm());
        builder.addRelic(new Relic.Enchiridion());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.InkBottle(9, 2));
        builder.addRelic(new Relic.BronzeScales());
        builder.addRelic(new Relic.RunicPyramid());
        builder.addRelic(new Relic.BottledTornado(new CardSilent.NoxiousFumesP()));
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(new Add(new CardSilent.CloakAndDaggerP())),
                List.of(new Add(new CardSilent.DeadlyPoisonP())),
                List.of(new Add(new CardSilent.BulletTimeP())),
                List.of(new PlayerHealth(69))
        ));
        builder.setGameStateViaInteractiveMode(List.of("", "3", "c", "1", "acro", "gran", "pre", "pre", "ne", "e", "rng off", "0", "6", "exit"));
        builder.setPreBattleScenarios(randomization);
        builder.addPotion(new Potion.DuplicationPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.GhostInAJar().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(3);
        builder.setPlayer(new Player(67, 69));
        return new GameState(builder);
    }

    public static GameState TestStateSilentRun8() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardSilent.NoxiousFumesP(), 1);
        builder.addCard(new CardSilent.Predator(), 1);
        builder.addCard(new CardSilent.Survivor(), 1);
        builder.addCard(new CardSilent.Neutralize(), 1);
        builder.addCard(new CardSilent.EndlessAgony(), 1);
        builder.addCard(new CardSilent.Finisher(), 1);
        builder.addCard(new CardSilent.SuckerPunchP(), 1);
        builder.addCard(new CardSilent.Backstab(), 1);
        builder.addCard(new CardSilent.BladeDance(), 2);
        builder.addCard(new CardSilent.Acrobatics(), 1);
        builder.addCard(new CardSilent.GlassKnife(), 1);
        builder.addCard(new CardSilent.DaggerThrow(), 1);
        builder.addCard(new CardSilent.AfterImageP(), 1);
        builder.addCard(new CardSilent.DodgeAndRoll(), 1);
        builder.addCard(new CardSilent.MasterfulStab(5), 1);
        builder.addCard(new CardSilent.Alchemize(100, 0, 0), 1);
        builder.addCard(new CardSilent.DaggerSpray(), 1);
        builder.addCard(new CardSilent.Reflex(), 1);
        builder.addCard(new CardSilent.CloakAndDagger(), 1);
        builder.addCard(new CardSilent.WellLaidPlansP(), 1);
        builder.addCard(new CardSilent.LegSweepP(), 1);
        builder.addCard(new CardSilent.ToolsOfTheTrade(), 1);
        builder.addCard(new CardSilent.TerrorP(), 1);
        builder.addCard(new CardSilent.PiercingWail(), 1);
        builder.addCard(new CardSilent.Burst(), 1);
        builder.addCard(new CardSilent.Accuracy(), 1);
        builder.addCard(new CardSilent.WraithFormP(), 2);
        builder.addCard(new CardSilent.Caltrops(), 1);
        builder.addCard(new CardSilent.BladeDanceP(), 1);
        builder.addCard(new CardColorless.SecretTechnique(), 0); // via Toolbox
        builder.addRelic(new Relic.RingOfTheSnake());
        builder.addRelic(new Relic.Shuriken());
        builder.addRelic(new Relic.ToyOrnithopter());
        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.Torii());
        builder.addRelic(new Relic.MummifiedHand());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.Calipers());
        builder.addRelic(new Relic.HoveringKite());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.InkBottle(9, 2));
        builder.addRelic(new Relic.MeatOnTheBone());
        builder.addRelic(new Relic.StrikeDummy());
        builder.addRelic(new Relic.HornCleat());
        builder.addRelic(new Relic.FossilizedHelix());
        builder.addRelic(new Relic.Vajra());
//        builder.addRelic(new Relic.Toolbox());
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        builder.setGameStateViaInteractiveMode(List.of("", "c", "1", "leg", "refl", "bla", "bla+", "asc", "mas", "gla", "e", "0", "c", "8", "35", "exit"));
        builder.addPotion(new Potion.DuplicationPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.DuplicationPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.WeakPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.FairyInABottle().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(7);
        builder.setPlayer(new Player(71, 72));
        return new GameState(builder);
    }

    public static GameState TestStateSilentRun9() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 5);
        builder.addCard(new CardSilent.Survivor(), 1);
        builder.addCard(new CardSilent.Neutralize(), 1);
        builder.addCard(new CardSilent.RiddleWithHolesP(), 1);
        builder.addCard(new CardSilent.BouncingFlask(), 1);
        builder.addCard(new CardSilent.PoisonedStab(), 1);
        builder.addCard(new CardSilent.SuckerPunch(), 1);
        builder.addCard(new CardSilent.StormOfSteelP(), 1);
        builder.addCard(new CardSilent.BouncingFlaskP(), 1);
        builder.addCard(new CardSilent.DeadlyPoisonP(), 2);
        builder.addCard(new CardSilent.DieDieDie(), 1);
        builder.addCard(new CardSilent.PredatorP(), 1);
        builder.addCard(new CardSilent.LegSweepP(), 2);
        builder.addCard(new CardSilent.CatalystP(), 2);
        builder.addCard(new CardSilent.FootworkP(), 1);
        builder.addCard(new CardSilent.PiercingWailP(), 3);
        builder.addCard(new CardSilent.BlurP(), 3);
        builder.addCard(new CardSilent.WellLaidPlansP(), 1);
        builder.addCard(new CardSilent.DodgeAndRollP(), 3);
        builder.addCard(new CardSilent.Dash(), 1);
        builder.addCard(new CardSilent.BackflipP(), 1);
        builder.addCard(new CardSilent.ExpertiseP(), 1);
        builder.addCard(new CardSilent.BurstP(), 1);
        builder.addCard(new CardSilent.AcrobaticsP(), 1);
        builder.addCard(new CardSilent.FootworkP(), 1);
        builder.addCard(new CardColorless.SecretTechniqueP(), 1);
        builder.addRelic(new Relic.RingOfTheSnake());
        builder.addRelic(new Relic.Torii());
        builder.addRelic(new Relic.Ectoplasm());
        builder.addRelic(new Relic.MummifiedHand());
        builder.addRelic(new Relic.Necronomicon());
        builder.addRelic(new Relic.Anchor());
        builder.addRelic(new Relic.GremlinHorn());
        builder.addRelic(new Relic.SlaversCollar());
        builder.addRelic(new Relic.StoneCalendar());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.PenNib(9, 2));
        builder.addRelic(new Relic.BloodVial());
        builder.addRelic(new Relic.Tingsha());
        builder.addRelic(new Relic.ClockworkSouvenir());
        builder.addRelic(new Relic.Toolbox());
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        builder.setGameStateViaInteractiveMode(List.of("", "0", "rng off", "c", "1", "die", "leg", "blu", "def", "pie", "back", "neco", "sur", "well", "e", "0", "4", "26", "29", "1", "exit"));
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(),
                List.of(new Add(new CardSilent.CripplingCloudP()))
        ));
        builder.setPreBattleScenarios(randomization);
        builder.setPlayer(new Player(17, 17));
        return new GameState(builder);
    }

    public static GameState TestStateSilentRun10() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.SILENT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new Card.Strike(), 3);
        builder.addCard(new Card.Defend(), 3);
        builder.addCard(new Card.DefendP(), 1);
        builder.addCard(new CardSilent.Survivor(), 1);
        builder.addCard(new CardSilent.Neutralize(), 1);
        builder.addCard(new CardSilent.Choke(), 3);
        builder.addCard(new CardSilent.DaggerSprayP(), 1);
        builder.addCard(new CardSilent.DaggerSpray(), 1);
        builder.addCard(new CardSilent.Backstab(), 1);
        builder.addCard(new CardSilent.InfiniteBlades(), 1);
        builder.addCard(new CardSilent.PiercingWail(), 1);
        builder.addCard(new CardSilent.LegSweep(), 1);
        builder.addCard(new CardSilent.AThousandCutsP(), 1);
        builder.addCard(new CardSilent.QuickSlash(), 1);
        builder.addCard(new CardSilent.WraithFormP(), 2);
        builder.addCard(new CardSilent.DaggerThrow(), 1);
        builder.addCard(new CardSilent.AcrobaticsP(), 1);
        builder.addCard(new CardSilent.Footwork(), 1);
        builder.addCard(new CardSilent.Outmaneuver(), 1);
        builder.addCard(new CardSilent.BladeDanceP(), 2);
        builder.addCard(new CardSilent.CalculatedGamble(), 1);
        builder.addCard(new CardSilent.AfterImage(), 1);
        builder.addCard(new CardOther.Decay(), 1);
        builder.addRelic(new Relic.RingOfTheSnake());
        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.OddlySmoothStone());
        builder.addRelic(new Relic.Nunchaku(8, 0));
        builder.addRelic(new Relic.PhilosophersStone());
        builder.addRelic(new Relic.SlingOfCourage());
        builder.addRelic(new Relic.Necronomicon());
//        builder.addRelic(new Relic.AncientTeaSet());
//        builder.addRelic(new Relic.BiasedCognitionLimit());
        builder.addRelic(new Relic.RunicPyramid());
        builder.addRelic(new Relic.IceCream());
        builder.addRelic(new Relic.BirdFacedUrn());
        builder.addRelic(new Relic.BagOfMarbles());
        builder.addRelic(new Relic.Sundial(2, 0));
        builder.addRelic(new Relic.HappyFlower(2, 0));
        builder.addRelic(new Relic.Akabeko());
        builder.setGameStateViaInteractiveMode(List.of("", "0", "c", "1", "back", "str", "p", "p", "str", "cal", "blad+", "wra", "ne", "asc", "e", "0", "exit"));
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(new Add(new CardSilent.Acrobatics())),
                List.of(new Add(new CardSilent.FlyingKnee())),
                List.of()
        ));
        builder.setPreBattleScenarios(randomization);
        builder.addPotion(new Potion.DuplicationPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(1);
        builder.setPlayer(new Player(71, 71));
        return new GameState(builder);
    }

    public static GameState TestStateStreamerRun() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new Card.Strike(), 2);
        builder.addCard(new Card.Defend(), 2);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.Zap(), 1);
        builder.addCard(new CardDefect.Dualcast(), 1);
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
        builder.addEnemyEncounter(PredefinedEncounter.COLLECTOR);
        var startOfGameScenarios = new StateModificationRandomization(List.of(
                List.of(new Add(new CardDefect.Seek())),
                List.of(new Add(new CardDefect.SeekP(), new CardDefect.Streamline(), new CardDefect.Skim(), new CardDefect.StackP())),
                List.of(new Add(new CardDefect.SeekP(), new CardDefect.Skim(), new CardDefect.StackP()))
        ));
        builder.setPreBattleScenarios(startOfGameScenarios);
        builder.addPotion(new Potion.StrengthPotion().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.FirePotion().setBasePenaltyRatio(85));
        builder.setPotionsScenarios(3);
        builder.setPlayer(new Player(50, 50));
        builder.addRelic(new Relic.CrackedCore());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.BagOfMarbles());
        builder.addRelic(new Relic.GoldPlatedCables());
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
        builder.addCard(new CardDefect.Dualcast(), 1);
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
        builder.addPotion(new Potion.FairyInABottle().setBasePenaltyRatio(80));
        builder.addPotion(new Potion.SpeedPotion().setBasePenaltyRatio(95));
        builder.setPotionsScenarios(1);
        builder.addEnemyEncounter(PredefinedEncounter.TIME_EATER);
//        builder.setInnateOrder("Boot Sequence", "Dramatic Entrance+");
        builder.setGameStateViaInteractiveMode(List.of("c", "1", "Defend", "Defend", "Stack+", "e", "0", "exit"));
        builder.setPlayer(new Player(63, 63));
        builder.addRelic(new Relic.CrackedCore());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.BagOfMarbles());
        builder.addRelic(new Relic.GoldPlatedCables());
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
        builder.addCard(new CardDefect.Dualcast(), 1);
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
        builder.addEnemyEncounter(PredefinedEncounter.AWAKENED_ONE);
        //        builder.setInnateOrder("Boot Sequence", "Dramatic Entrance+");
        builder.setGameStateViaInteractiveMode(List.of("c", "1", "Ascender's Bane", "Rebound", "Reboot+", "e", "0", "c", "3", "0", "56", "c", "3", "1", "56", "exit"));
        builder.setPlayer(new Player(1, 1));
        builder.addRelic(new Relic.CrackedCore());
        builder.addRelic(new Relic.PreservedInsect());
        builder.addRelic(new Relic.BagOfMarbles());
        builder.addRelic(new Relic.GoldPlatedCables());
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
        builder.addRelic(new Relic.RingOfTheSnake());
        builder.addRelic(new Relic.HappyFlower(2, 1));
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.ArtOfWar());
        builder.addRelic(new Relic.CoffeeDripper());
        builder.addPotion(new Potion.SpeedPotion().setBasePenaltyRatio(95));
        builder.addPotion(new Potion.CunningPotion().setBasePenaltyRatio(95));
        builder.setPotionsScenarios(3);
        builder.addEnemyEncounter(PredefinedEncounter.BRONZE_AUTOMATON);
        return new GameState(builder);
    }

    public static GameState TestStateReddit() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.Zap(), 1);
        builder.addCard(new CardDefect.Dualcast(), 1);
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
        builder.addCard(new CardDefect.CompileDriver(), 1);
        builder.addCard(new CardDefect.ReinforcedBody(), 1);
        builder.addCard(new CardDefect.Aggregate(), 1);
        builder.addCard(new CardDefect.SelfRepair(), 1);
        builder.addCard(new CardColorless.Apotheosis(), 1);
        builder.addEnemyEncounter(PredefinedEncounter.BOOK_OF_STABBING);
        builder.addEnemyEncounter(PredefinedEncounter.SLAVERS_ELITE);
        builder.addEnemyEncounter(PredefinedEncounter.GREMLIN_LEADER);
        GameStateRandomization randomization = new StateModificationRandomization(List.of(
                List.of(new Add(new CardDefect.CompileDriver())),
                List.of(new Add(new CardDefect.Rebound())),
                List.of(new Add(new CardDefect.BeamCellP())),
                List.of()
        ));
        builder.setRandomization(randomization);
//        builder.setGameStateViaInteractiveMode(List.of("", "c", "1", "stat", "gl", "self", "agg", "reinf", "e", "0", "exit"));
        builder.setPlayer(new Player(55, 55));
        builder.addPotion(new Potion.FocusPotion().setBasePenaltyRatio(90));
        builder.addPotion(new Potion.EssenceOfSteel().setBasePenaltyRatio(90));
        builder.setPotionsScenarios(3);
        builder.addRelic(new Relic.CrackedCore());
        builder.addRelic(new Relic.PenNib(9, 2));
        builder.addRelic(new Relic.LetterOpener());
        builder.addRelic(new Relic.OrangePellets());
        return new GameState(builder);
    }

    public static GameState TestStateXecnar2() {
        var builder = new GameStateBuilder();
        builder.setCharacter(CharacterEnum.DEFECT);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardDefect.Dualcast(), 1);
        builder.addCard(new CardDefect.ColdSnap(), 3);
        builder.addCard(new CardDefect.GeneticAlgorithm(57, 0), 1);
        builder.addCard(new CardDefect.Tempest(), 1);
        builder.addCard(new CardDefect.LoopP(), 1);
        builder.addCard(new CardDefect.BallLightning(), 1);
        builder.addCard(new CardDefect.ChargeBattery(), 1);
        builder.addCard(new CardDefect.CompileDriver(), 1);
        builder.addCard(new CardDefect.ConsumeP(), 1);
        builder.addCard(new CardDefect.MeteorStrike(), 1);
        builder.addCard(new CardDefect.Blizzard(), 1);
        builder.addCard(new CardDefect.MultiCast(), 1);
        builder.addCard(new CardDefect.Aggregate(), 1);
        builder.addCard(new CardDefect.SkimP(), 1);
        builder.addCard(new CardDefect.Sunder(), 1);
        builder.addCard(new CardDefect.Glacier(), 1);
        builder.addCard(new CardDefect.DefragmentP(), 1);
        builder.addCard(new CardDefect.Buffer(), 1);
        builder.addCard(new CardDefect.FissionP(), 1);
        builder.addCard(new CardDefect.Equilibrium(), 2);
        builder.addCard(new CardDefect.Heatsinks(), 1);
        builder.addCard(new CardDefect.CoolheadedP(), 1);
        builder.addCard(new CardDefect.Capacitor(), 1);
        builder.addCard(new CardDefect.Skim(), 1);
        builder.addCard(new CardDefect.Loop(), 1);
        builder.addCard(new CardColorless.EnlightenmentP(), 1);
        builder.addRelic(new Relic.CrackedCore());
        builder.addRelic(new Relic.EmotionChip());
        builder.addRelic(new Relic.MeatOnTheBone());
        builder.addRelic(new Relic.Akabeko());
        builder.addRelic(new Relic.BagOfPreparation());
        builder.addRelic(new Relic.RedMask());
        builder.addRelic(new Relic.CentennialPuzzle());
        builder.addRelic(new Relic.Shuriken());
        builder.addRelic(new Relic.MummifiedHand());
        builder.addRelic(new Relic.InkBottle(9, 0));
        builder.addRelic(new Relic.CoffeeDripper());
        builder.addRelic(new Relic.GoldPlatedCables());
        builder.addRelic(new Relic.DataDisk());
        builder.addRelic(new Relic.IceCream());
        builder.addRelic(new Relic.BloodVial());
        builder.addRelic(new Relic.TungstenRod());
        builder.addEnemyEncounter(PredefinedEncounter.CORRUPT_HEART);
        builder.setPlayer(new Player(71, 71));
        builder.addPotion(new Potion.GamblersBrew().setBasePenaltyRatio(100));
        builder.addPotion(new Potion.DuplicationPotion().setBasePenaltyRatio(100));
        builder.setPotionsScenarios(3);
        builder.setGameStateViaInteractiveMode(List.of("", "c", "1", "cold", "asc", "defra+", "blizz", "compi", "ba", "p", "ball", "cold", "e", "0", "exit"), true);
        return new GameState(builder);
    }
}
