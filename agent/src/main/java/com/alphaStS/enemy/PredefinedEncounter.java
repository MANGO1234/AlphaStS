package com.alphaStS.enemy;

import com.alphaStS.GameStateBuilder;

import java.util.List;
import java.util.function.Consumer;

public enum PredefinedEncounter {
    // Act 1
    CULTIST(1, List.of(new EnemyExordium.Cultist())),
    JAW_WORM(1, List.of(new EnemyExordium.JawWorm(false))),
    BLUE_SLAVER(1, List.of(new EnemyExordium.BlueSlaver())),
    RED_SLAVER(1, List.of(new EnemyExordium.RedSlaver())),
    DUAL_FUNGI_BEASTS(1, List.of(), EnemyEncounter::addDualFungiBeastFight),
    LOOTER(1, List.of(new EnemyExordium.Looter())),
    LARGE_SPIKE_SLIME(1, List.of(), EnemyEncounter::addLargeSpikeSlimeFight),
    ACID_SLIME(1, List.of(), EnemyEncounter::addAcidSlimeFight),
    SLAVERS_ELITE(1, List.of(), EnemyEncounter::addSlaversEliteFight),
    SENTRIES(1, List.of(), EnemyEncounter::addSentriesFight),
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
    TRIPLE_BYRDS(2, List.of(), EnemyEncounter::addByrdsFight),
    LOOTER_AND_MUGGER(2, List.of(new EnemyExordium.Looter(), new EnemyExordium.Mugger())),
    CHOSEN_AND_BYRD(2, List.of(new EnemyCity.Chosen(), new EnemyCity.Byrd())),
    CULTIST_AND_CHOSEN(2, List.of(new EnemyExordium.Cultist(), new EnemyCity.Chosen())),
    SENTRY_AND_SPHERIC_GUARDIAN(2, List.of(), EnemyEncounter::addSentryAndSphericGuardianFight),
    SNAKE_PLANT(2, List.of(new EnemyCity.SnakePlant())),
    SNECKO(2, List.of(new EnemyCity.Snecko())),
    CENTURION_AND_MYSTIC(2, List.of(), EnemyEncounter::addCenturionAndMysticFight),
    TRIPLE_CULTISTS(2, List.of(), EnemyEncounter::addTripleCultistsFight),
    SHELLED_PARASITE_AND_FUNGI_BEAST(2, List.of(new EnemyCity.ShelledParasite(), new EnemyExordium.FungiBeast())),
    ROBBERS_EVENT(2, List.of(), EnemyEncounter::addRobbersEventFight),
    GREMLIN_LEADER(2, List.of(), EnemyEncounter::addGremlinLeaderFight),
    BRONZE_AUTOMATON(2, List.of(new EnemyCity.BronzeOrb().startDead(), new EnemyCity.BronzeAutomaton(), new EnemyCity.BronzeOrb().startDead())),
    COLLECTOR(2, List.of(new EnemyCity.TorchHead().startDead(), new EnemyCity.TorchHead().startDead(), new EnemyCity.TheCollector())),
    THE_CHAMP(2, List.of(new EnemyCity.TheChamp())),

    // Act 3
    TRIPLE_DARKLINGS(3, List.of(), EnemyEncounter::addDarklingsFight),
    ORB_WALKER(3, List.of(new EnemyBeyond.OrbWalker())),
    MAW(3, List.of(new EnemyBeyond.TheMaw())),
    WRITHING_MASS(3, List.of(new EnemyBeyond.WrithingMass(0.8f))),
    TRIPLE_JAW_WORMS(3, List.of(), EnemyEncounter::addTripleJawWormsFight),
    SPIRE_GROWTH(3, List.of(new EnemyBeyond.SpireGrowth())),
    TRANSIENT(3, List.of(new EnemyBeyond.Transient())),
    REPTOMANCER(3, List.of(), EnemyEncounter::addReptomancerFight),
    DONU_AND_DECA(3, List.of(), EnemyEncounter::addDonuAndDecaFight),
    TIME_EATER(3, List.of(new EnemyBeyond.TimeEater())),
    AWAKENED_ONE(3, List.of(), EnemyEncounter::addAwakenedOneFight),

    // Act 4
    SPEAR_AND_SHIELD(4, List.of(new EnemyEnding.SpireShield(), new EnemyEnding.SpireSpear())),
    CORRUPT_HEART(4, List.of(new EnemyEnding.CorruptHeart()));

    public int act;
    public List<Enemy> enemies;
    public Consumer<GameStateBuilder> encounterExtraLogic;

    PredefinedEncounter(int act, List<Enemy> enemies, Consumer<GameStateBuilder> encounterExtraLogic) {
        this.act = act;
        this.enemies = enemies;
        this.encounterExtraLogic = encounterExtraLogic;
    }

    PredefinedEncounter(int act, List<Enemy> enemies) {
        this.act = act;
        this.enemies = enemies;
    }
}
