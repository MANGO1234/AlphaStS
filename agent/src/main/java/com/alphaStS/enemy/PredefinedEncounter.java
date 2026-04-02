package com.alphaStS.enemy;

import com.alphaStS.GameStateBuilder;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public enum PredefinedEncounter {
    // Act 1
    CULTIST(1, List.of(new EnemyExordium.Cultist())),
    JAW_WORM(1, List.of(new EnemyExordium.JawWorm(false))),
    BLUE_SLAVER(1, List.of(new EnemyExordium.BlueSlaver())),
    RED_SLAVER(1, List.of(new EnemyExordium.RedSlaver())),
    DUAL_FUNGI_BEASTS(1, List.of(new EnemyExordium.FungiBeast(), new EnemyExordium.FungiBeast())),
    LOOTER(1, List.of(new EnemyExordium.Looter())),
    LARGE_SPIKE_SLIME(1, List.of(new EnemyExordium.LargeSpikeSlime(),
            new EnemyExordium.MediumSpikeSlime(36).startDead(),
            new EnemyExordium.MediumSpikeSlime(36).startDead())),
    ACID_SLIME(1, List.of(new EnemyExordium.LargeAcidSlime(),
            new EnemyExordium.MediumAcidSlime(36).startDead(),
            new EnemyExordium.MediumAcidSlime(36).startDead())),
    SLAVERS_ELITE(1, List.of(new EnemyExordium.BlueSlaver().asElite(),
            new EnemyCity.Taskmaster(),
            new EnemyExordium.RedSlaver().asElite())),
    SENTRIES(1, List.of(new EnemyExordium.Sentry(45, EnemyExordium.Sentry.BOLT),
            new EnemyExordium.Sentry(45, EnemyExordium.Sentry.BEAM),
            new EnemyExordium.Sentry(45, EnemyExordium.Sentry.BOLT)),
            EnemyEncounter.FIRST_AND_LAST_REORDERING),
    GREMLIN_NOB(1, List.of(new EnemyExordium.GremlinNob())),
    LAGAVULIN(1, List.of(new EnemyExordium.Lagavulin())),
    GUARDIAN(1, List.of(new EnemyExordium.TheGuardian())),
    HEXAGHOST(1, List.of(new EnemyExordium.Hexaghost())),
    // todo: in some situations, order matters, create a variant where order matters (would require 20 enemies instead of 8)
    GREMLIN_GANG(1, List.of(new EnemyExordium.MadGremlin(), new EnemyExordium.MadGremlin(),
            new EnemyExordium.SneakyGremlin(), new EnemyExordium.SneakyGremlin(),
            new EnemyExordium.FatGremlin(), new EnemyExordium.FatGremlin(),
            new EnemyExordium.ShieldGremlin(), new EnemyExordium.GremlinWizard())),
    SLIME_BOSS(1, List.of(new EnemyExordium.SlimeBoss(),
            new EnemyExordium.LargeSpikeSlime(75).startDead(),
            new EnemyExordium.MediumSpikeSlime(37).startDead(),
            new EnemyExordium.MediumSpikeSlime(37).startDead(),
            new EnemyExordium.LargeAcidSlime(75).startDead(),
            new EnemyExordium.MediumAcidSlime(37).startDead(),
            new EnemyExordium.MediumAcidSlime(37).startDead())),

    // Act 2
    SPHERIC_GUARDIAN(2, List.of(new EnemyCity.SphericGuardian())),
    CHOSEN(2, List.of(new EnemyCity.Chosen())),
    SHELLED_PARASITE(2, List.of(new EnemyCity.ShelledParasite())),
    TRIPLE_BYRDS(2, List.of(new EnemyCity.Byrd(), new EnemyCity.Byrd(), new EnemyCity.Byrd()),
            EnemyEncounter.TRIPLE_ENEMIES_REORDERING),
    LOOTER_AND_MUGGER(2, List.of(new EnemyExordium.Looter(), new EnemyExordium.Mugger())),
    CHOSEN_AND_BYRD(2, List.of(new EnemyCity.Chosen(), new EnemyCity.Byrd())),
    CULTIST_AND_CHOSEN(2, List.of(new EnemyExordium.Cultist(), new EnemyCity.Chosen())),
    SENTRY_AND_SPHERIC_GUARDIAN(2, List.of(new EnemyExordium.Sentry(EnemyExordium.Sentry.BOLT).notElite(),
            new EnemyCity.SphericGuardian())),
    SNAKE_PLANT(2, List.of(new EnemyCity.SnakePlant())),
    SNECKO(2, List.of(new EnemyCity.Snecko())),
    CENTURION_AND_MYSTIC(2, List.of(new EnemyCity.Centurion(), new EnemyCity.Mystic())),
    TRIPLE_CULTISTS(2, List.of(new EnemyExordium.Cultist(), new EnemyExordium.Cultist(), new EnemyExordium.Cultist()),
            EnemyEncounter.TRIPLE_ENEMIES_REORDERING),
    SHELLED_PARASITE_AND_FUNGI_BEAST(2, List.of(new EnemyCity.ShelledParasite(), new EnemyExordium.FungiBeast())),
    ROBBERS_EVENT(2, List.of(new EnemyCity.Pointy(), new EnemyCity.Romeo(), new EnemyCity.Bear())),
    GREMLIN_LEADER(2, List.of(), EnemyEncounter::getGremlinLeaderFightEnemies, EnemyEncounter.GREMLIN_LEADER_REORDERING),
    BRONZE_AUTOMATON(2, List.of(new EnemyCity.BronzeOrb().startDead(), new EnemyCity.BronzeAutomaton(), new EnemyCity.BronzeOrb().startDead())),
    COLLECTOR(2, List.of(new EnemyCity.TorchHead().startDead(), new EnemyCity.TorchHead().startDead(), new EnemyCity.TheCollector())),
    BOOK_OF_STABBING(2, List.of(new EnemyCity.BookOfStabbing())),
    THE_CHAMP(2, List.of(new EnemyCity.TheChamp())),

    // Act 3
    TRIPLE_DARKLINGS(3, List.of(new EnemyBeyond.Darkling(false), new EnemyBeyond.Darkling(true), new EnemyBeyond.Darkling(false)),
            EnemyEncounter.FIRST_AND_LAST_REORDERING),
    ORB_WALKER(3, List.of(new EnemyBeyond.OrbWalker())),
    MAW(3, List.of(new EnemyBeyond.TheMaw())),
    WRITHING_MASS(3, List.of(new EnemyBeyond.WrithingMass(0.8f))),
    TRIPLE_JAW_WORMS(3, List.of(new EnemyExordium.JawWorm(true), new EnemyExordium.JawWorm(true), new EnemyExordium.JawWorm(true)),
            EnemyEncounter.TRIPLE_ENEMIES_REORDERING),
    SPIRE_GROWTH(3, List.of(new EnemyBeyond.SpireGrowth())),
    TRANSIENT(3, List.of(new EnemyBeyond.Transient())),
    REPTOMANCER(3, List.of(new EnemyBeyond.Dagger(),
            new EnemyBeyond.Dagger(),
            new EnemyBeyond.Reptomancer(),
            new EnemyBeyond.Dagger(),
            new EnemyBeyond.Dagger())),
    DONU_AND_DECA(3, List.of(new EnemyBeyond.Deca(), new EnemyBeyond.Donu())),
    TIME_EATER(3, List.of(new EnemyBeyond.TimeEater())),
    GIANT_HEAD(3, List.of(new EnemyBeyond.GiantHead())),
    NEMESIS(3, List.of(new EnemyBeyond.Nemesis())),
    AWAKENED_ONE(3, List.of(new EnemyExordium.Cultist(), new EnemyExordium.Cultist(), new EnemyBeyond.AwakenedOne()),
            EnemyEncounter.AWAKENED_ONE_REORDERING),

    // Act 4
    SPEAR_AND_SHIELD(4, List.of(new EnemyEnding.SpireShield(), new EnemyEnding.SpireSpear())),
    CORRUPT_HEART(4, List.of(new EnemyEnding.CorruptHeart()));

    public final int act;
    public List<Enemy> enemies;
    public Supplier<List<Enemy>> enemiesSupplier;
    public Consumer<GameStateBuilder> encounterExtraLogic;
    public EnemyEncounter.EnemyReordering reordering;

    PredefinedEncounter(int act, List<Enemy> enemies, Consumer<GameStateBuilder> encounterExtraLogic,
            EnemyEncounter.EnemyReordering reordering) {
        this.act = act;
        this.enemies = enemies;
        this.encounterExtraLogic = encounterExtraLogic;
        this.reordering = reordering;
    }

    PredefinedEncounter(int act, List<Enemy> enemies, EnemyEncounter.EnemyReordering reordering) {
        this.act = act;
        this.enemies = enemies;
        this.reordering = reordering;
    }

    PredefinedEncounter(int act, Supplier<List<Enemy>> enemiesSupplier, EnemyEncounter.EnemyReordering reordering) {
        this.act = act;
        this.enemiesSupplier = enemiesSupplier;
        this.reordering = reordering;
    }

    PredefinedEncounter(int act, List<Enemy> enemies) {
        this.act = act;
        this.enemies = enemies;
    }
}
