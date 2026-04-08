package com.alphaStS.enemy;

import com.alphaStS.GameStateBuilder;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public enum PredefinedEncounter {
    // Act 1
    CULTIST("Cultist", 1, () -> List.of(new EnemyExordium.Cultist())),
    JAW_WORM("Jaw Worm", 1, () -> List.of(new EnemyExordium.JawWorm(false))),
    BLUE_SLAVER("Blue Slaver", 1, () -> List.of(new EnemyExordium.BlueSlaver())),
    RED_SLAVER("Red Slaver", 1, () -> List.of(new EnemyExordium.RedSlaver())),
    DUAL_FUNGI_BEASTS("2 Fungi Beasts", 1, () -> List.of(new EnemyExordium.FungiBeast(), new EnemyExordium.FungiBeast())),
    LOOTER("Looter", 1, () -> List.of(new EnemyExordium.Looter())),
    LARGE_SPIKE_SLIME("Large Slime", 1, () -> List.of(new EnemyExordium.LargeSpikeSlime(),
            new EnemyExordium.MediumSpikeSlime(36).startDead(),
            new EnemyExordium.MediumSpikeSlime(36).startDead())),
    ACID_SLIME("Large Slime", 1, () -> List.of(new EnemyExordium.LargeAcidSlime(),
            new EnemyExordium.MediumAcidSlime(36).startDead(),
            new EnemyExordium.MediumAcidSlime(36).startDead())),
    SLAVERS_ELITE("Slavers", 1, () -> List.of(new EnemyExordium.BlueSlaver().asElite(),
            new EnemyCity.Taskmaster(),
            new EnemyExordium.RedSlaver().asElite())),
    SENTRIES("3 Sentries", 1, () -> List.of(new EnemyExordium.Sentry(45, EnemyExordium.Sentry.BOLT),
            new EnemyExordium.Sentry(45, EnemyExordium.Sentry.BEAM),
            new EnemyExordium.Sentry(45, EnemyExordium.Sentry.BOLT))),
    GREMLIN_NOB("Gremlin Nob", 1, () -> List.of(new EnemyExordium.GremlinNob())),
    LAGAVULIN("Lagavulin", 1, () -> List.of(new EnemyExordium.Lagavulin())),
    GUARDIAN("The Guardian", 1, () -> List.of(new EnemyExordium.TheGuardian())),
    HEXAGHOST("Hexaghost", 1, () -> List.of(new EnemyExordium.Hexaghost())),
    // todo: in some situations, order matters, create a variant where order matters (would require 20 enemies instead of 8)
    GREMLIN_GANG("Gremlin Gang", 1, () -> List.of(new EnemyExordium.MadGremlin(), new EnemyExordium.MadGremlin(),
            new EnemyExordium.SneakyGremlin(), new EnemyExordium.SneakyGremlin(),
            new EnemyExordium.FatGremlin(), new EnemyExordium.FatGremlin(),
            new EnemyExordium.ShieldGremlin(), new EnemyExordium.GremlinWizard())),
    SLIME_BOSS("Slime Boss", 1, () -> List.of(new EnemyExordium.SlimeBoss(),
            new EnemyExordium.LargeSpikeSlime(75).startDead(),
            new EnemyExordium.MediumSpikeSlime(37).startDead(),
            new EnemyExordium.MediumSpikeSlime(37).startDead(),
            new EnemyExordium.LargeAcidSlime(75).startDead(),
            new EnemyExordium.MediumAcidSlime(37).startDead(),
            new EnemyExordium.MediumAcidSlime(37).startDead())),

    // Act 2
    SPHERIC_GUARDIAN("Spheric Guardian", 2, () -> List.of(new EnemyCity.SphericGuardian())),
    CHOSEN("Chosen", 2, () -> List.of(new EnemyCity.Chosen())),
    SHELLED_PARASITE("Shell Parasite", 2, () -> List.of(new EnemyCity.ShelledParasite())),
    TRIPLE_BYRDS("3 Byrds", 2, () -> List.of(new EnemyCity.Byrd(), new EnemyCity.Byrd(), new EnemyCity.Byrd())),
    LOOTER_AND_MUGGER("2 Thieves", 2, () -> List.of(new EnemyExordium.Looter(), new EnemyExordium.Mugger())),
    CHOSEN_AND_BYRD("Chosen and Byrds", 2, () -> List.of(new EnemyCity.Chosen(), new EnemyCity.Byrd())),
    CULTIST_AND_CHOSEN("Cultist and Chosen", 2, () -> List.of(new EnemyExordium.Cultist(), new EnemyCity.Chosen())),
    SENTRY_AND_SPHERIC_GUARDIAN("Sentry and Sphere", 2, () -> List.of(new EnemyExordium.Sentry(EnemyExordium.Sentry.BOLT).notElite(), new EnemyCity.SphericGuardian())),
    SNAKE_PLANT("Snake Plant", 2, () -> List.of(new EnemyCity.SnakePlant())),
    SNECKO("Snecko", 2, () -> List.of(new EnemyCity.Snecko())),
    CENTURION_AND_MYSTIC("Centurion and Healer", 2, () -> List.of(new EnemyCity.Centurion(), new EnemyCity.Mystic())),
    TRIPLE_CULTISTS("3 Cultists", 2, () -> List.of(new EnemyExordium.Cultist(), new EnemyExordium.Cultist(), new EnemyExordium.Cultist())),
    SHELLED_PARASITE_AND_FUNGI_BEAST("Shelled Parasite and Fungi", 2, () -> List.of(new EnemyCity.ShelledParasite(), new EnemyExordium.FungiBeast())),
    ROBBERS_EVENT("Masked Bandits", 2, () -> List.of(new EnemyCity.Pointy(), new EnemyCity.Romeo(), new EnemyCity.Bear())),
    GREMLIN_LEADER("Gremlin Leader", 2, EnemyEncounter::getGremlinLeaderFightEnemies),
    BRONZE_AUTOMATON("Automaton", 2, () -> List.of(new EnemyCity.BronzeOrb().startDead(), new EnemyCity.BronzeAutomaton(), new EnemyCity.BronzeOrb().startDead())),
    COLLECTOR("Collector", 2, () -> List.of(new EnemyCity.TorchHead().startDead(), new EnemyCity.TorchHead().startDead(), new EnemyCity.TheCollector())),
    BOOK_OF_STABBING("Book of Stabbing", 2, () -> List.of(new EnemyCity.BookOfStabbing())),
    THE_CHAMP("Champ", 2, () -> List.of(new EnemyCity.TheChamp())),

    // Act 3
    TRIPLE_DARKLINGS("3 Darklings", 3, () -> List.of(new EnemyBeyond.Darkling(false), new EnemyBeyond.Darkling(true), new EnemyBeyond.Darkling(false))),
    ORB_WALKER("Orb Walker", 3, () -> List.of(new EnemyBeyond.OrbWalker())),
    MAW("Maw", 3, () -> List.of(new EnemyBeyond.TheMaw())),
    WRITHING_MASS("Writhing Mass", 3, () -> List.of(new EnemyBeyond.WrithingMass(0.8f))),
    TRIPLE_JAW_WORMS("Jaw Worm Horde", 3, () -> List.of(new EnemyExordium.JawWorm(true), new EnemyExordium.JawWorm(true), new EnemyExordium.JawWorm(true))),
    SPIRE_GROWTH("Spire Growth", 3, () -> List.of(new EnemyBeyond.SpireGrowth())),
    TRANSIENT("Transient", 3, () -> List.of(new EnemyBeyond.Transient())),
    REPTOMANCER("Reptomancer", 3, () -> List.of(new EnemyBeyond.Dagger(), new EnemyBeyond.Dagger(), new EnemyBeyond.Reptomancer(), new EnemyBeyond.Dagger(), new EnemyBeyond.Dagger())),
    DONU_AND_DECA("Donu and Deca", 3, () -> List.of(new EnemyBeyond.Deca(), new EnemyBeyond.Donu())),
    TIME_EATER("Time Eater", 3, () -> List.of(new EnemyBeyond.TimeEater())),
    GIANT_HEAD("Giant Head", 3, () -> List.of(new EnemyBeyond.GiantHead())),
    NEMESIS("Nemesis", 3, () -> List.of(new EnemyBeyond.Nemesis())),
    AWAKENED_ONE("Awakened One", 3, () -> List.of(new EnemyExordium.Cultist(), new EnemyExordium.Cultist(), new EnemyBeyond.AwakenedOne())),

    // Act 4
    SPEAR_AND_SHIELD("Shield and Spear", 4, () -> List.of(new EnemyEnding.SpireShield(), new EnemyEnding.SpireSpear())),
    CORRUPT_HEART("The Heart", 4, () -> List.of(new EnemyEnding.CorruptHeart()));

    public final int act;
    public final String internalKey;
    public Supplier<List<Enemy>> enemiesSupplier;
    public Consumer<GameStateBuilder> encounterExtraLogic;
    public EnemyEncounter.EnemyReordering reordering;

    static {
        SENTRIES.reordering = EnemyEncounter.FIRST_AND_LAST_REORDERING;
        TRIPLE_BYRDS.reordering = EnemyEncounter.TRIPLE_ENEMIES_REORDERING;
        TRIPLE_CULTISTS.reordering = EnemyEncounter.TRIPLE_ENEMIES_REORDERING;
        TRIPLE_JAW_WORMS.reordering = EnemyEncounter.TRIPLE_ENEMIES_REORDERING;
        TRIPLE_DARKLINGS.reordering = EnemyEncounter.FIRST_AND_LAST_REORDERING;
        AWAKENED_ONE.reordering = EnemyEncounter.AWAKENED_ONE_REORDERING;
        GREMLIN_LEADER.reordering = EnemyEncounter.GREMLIN_LEADER_REORDERING;
    }

    PredefinedEncounter(String internalKey, int act, Supplier<List<Enemy>> enemiesSupplier) {
        this.internalKey = internalKey;
        this.act = act;
        this.enemiesSupplier = enemiesSupplier;
    }
}
