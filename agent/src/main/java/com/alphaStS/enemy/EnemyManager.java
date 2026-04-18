package com.alphaStS.enemy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EnemyManager {
    private static final Map<String, Supplier<Enemy>> ENEMY_FACTORIES = new HashMap<>();

    static {
        // EnemyExordium
        ENEMY_FACTORIES.put("Gremlin Nob",       EnemyExordium.GremlinNob::new);
        ENEMY_FACTORIES.put("Lagavulin",          EnemyExordium.Lagavulin::new);
        ENEMY_FACTORIES.put("Sentry",             () -> new EnemyExordium.Sentry(EnemyExordium.Sentry.BOLT));
        ENEMY_FACTORIES.put("Hexaghost",          EnemyExordium.Hexaghost::new);
        ENEMY_FACTORIES.put("The Guardian",       EnemyExordium.TheGuardian::new);
        ENEMY_FACTORIES.put("Slime Boss",         EnemyExordium.SlimeBoss::new);
        ENEMY_FACTORIES.put("Large Spike Slime",  EnemyExordium.LargeSpikeSlime::new);
        ENEMY_FACTORIES.put("Medium Spike Slime", EnemyExordium.MediumSpikeSlime::new);
        ENEMY_FACTORIES.put("Small Spike Slime",  EnemyExordium.SmallSpikeSlime::new);
        ENEMY_FACTORIES.put("Large Acid Slime",   EnemyExordium.LargeAcidSlime::new);
        ENEMY_FACTORIES.put("Medium Acid Slime",  EnemyExordium.MediumAcidSlime::new);
        ENEMY_FACTORIES.put("Small Acid Slime",   EnemyExordium.SmallAcidSlime::new);
        ENEMY_FACTORIES.put("Red Slaver",         EnemyExordium.RedSlaver::new);
        ENEMY_FACTORIES.put("Blue Slaver",        EnemyExordium.BlueSlaver::new);
        ENEMY_FACTORIES.put("Jaw Worm",           () -> new EnemyExordium.JawWorm(false));
        ENEMY_FACTORIES.put("Cultist",            EnemyExordium.Cultist::new);
        ENEMY_FACTORIES.put("Red Louse",          EnemyExordium.RedLouse::new);
        ENEMY_FACTORIES.put("Green Louse",        EnemyExordium.GreenLouse::new);
        ENEMY_FACTORIES.put("Fungi Beast",        EnemyExordium.FungiBeast::new);
        ENEMY_FACTORIES.put("Looter",             EnemyExordium.Looter::new);
        ENEMY_FACTORIES.put("Mugger",             EnemyExordium.Mugger::new);
        ENEMY_FACTORIES.put("Fat Gremlin",        EnemyExordium.FatGremlin::new);
        ENEMY_FACTORIES.put("Mad Gremlin",        EnemyExordium.MadGremlin::new);
        ENEMY_FACTORIES.put("Sneaky Gremlin",     EnemyExordium.SneakyGremlin::new);
        ENEMY_FACTORIES.put("Shield Gremlin",     EnemyExordium.ShieldGremlin::new);
        ENEMY_FACTORIES.put("Gremlin Wizard",     EnemyExordium.GremlinWizard::new);

        // EnemyCity
        ENEMY_FACTORIES.put("The Champ",          EnemyCity.TheChamp::new);
        ENEMY_FACTORIES.put("Bronze Automaton",   EnemyCity.BronzeAutomaton::new);
        ENEMY_FACTORIES.put("Bronze Orb",         EnemyCity.BronzeOrb::new);
        ENEMY_FACTORIES.put("The Collector",      EnemyCity.TheCollector::new);
        ENEMY_FACTORIES.put("Torch Head",         EnemyCity.TorchHead::new);
        ENEMY_FACTORIES.put("Gremlin Leader",     EnemyCity.GremlinLeader::new);
        ENEMY_FACTORIES.put("Book Of Stabbing",   EnemyCity.BookOfStabbing::new);
        ENEMY_FACTORIES.put("Taskmaster",         EnemyCity.Taskmaster::new);
        ENEMY_FACTORIES.put("Byrd",               EnemyCity.Byrd::new);
        ENEMY_FACTORIES.put("Spheric Guardian",   EnemyCity.SphericGuardian::new);
        ENEMY_FACTORIES.put("Shelled Parasite",   EnemyCity.ShelledParasite::new);
        ENEMY_FACTORIES.put("Snecko",             EnemyCity.Snecko::new);
        ENEMY_FACTORIES.put("Pointy",             EnemyCity.Pointy::new);
        ENEMY_FACTORIES.put("Romeo",              EnemyCity.Romeo::new);
        ENEMY_FACTORIES.put("Bear",               EnemyCity.Bear::new);
        ENEMY_FACTORIES.put("Centurion",          EnemyCity.Centurion::new);
        ENEMY_FACTORIES.put("Mystic",             EnemyCity.Mystic::new);
        ENEMY_FACTORIES.put("Chosen",             EnemyCity.Chosen::new);
        ENEMY_FACTORIES.put("Snake Plant",        EnemyCity.SnakePlant::new);

        // EnemyBeyond
        ENEMY_FACTORIES.put("Awakened One",       EnemyBeyond.AwakenedOne::new);
        ENEMY_FACTORIES.put("Donu",               EnemyBeyond.Donu::new);
        ENEMY_FACTORIES.put("Deca",               EnemyBeyond.Deca::new);
        ENEMY_FACTORIES.put("Time Eater",         EnemyBeyond.TimeEater::new);
        ENEMY_FACTORIES.put("Giant Head",         EnemyBeyond.GiantHead::new);
        ENEMY_FACTORIES.put("Nemesis",            EnemyBeyond.Nemesis::new);
        ENEMY_FACTORIES.put("Reptomancer",        EnemyBeyond.Reptomancer::new);
        ENEMY_FACTORIES.put("Dagger",             EnemyBeyond.Dagger::new);
        ENEMY_FACTORIES.put("The Maw",            EnemyBeyond.TheMaw::new);
        ENEMY_FACTORIES.put("Writhing Mass",      () -> new EnemyBeyond.WrithingMass(0.8f));
        ENEMY_FACTORIES.put("Orb Walker",         EnemyBeyond.OrbWalker::new);
        ENEMY_FACTORIES.put("Spire Growth",       EnemyBeyond.SpireGrowth::new);
        ENEMY_FACTORIES.put("Darkling",           () -> new EnemyBeyond.Darkling(false));
        ENEMY_FACTORIES.put("Repulsor",           EnemyBeyond.Repulsor::new);
        ENEMY_FACTORIES.put("Exploder",           EnemyBeyond.Exploder::new);
        ENEMY_FACTORIES.put("Spiker",             EnemyBeyond.Spiker::new);
        ENEMY_FACTORIES.put("Transient",          EnemyBeyond.Transient::new);

        // EnemyEnding
        ENEMY_FACTORIES.put("Spire Shield",       EnemyEnding.SpireShield::new);
        ENEMY_FACTORIES.put("Spire Spear",        EnemyEnding.SpireSpear::new);
        ENEMY_FACTORIES.put("Corrupt Heart",      EnemyEnding.CorruptHeart::new);
    }

    public static Supplier<Enemy> getFactory(String name) {
        return ENEMY_FACTORIES.get(name);
    }
}
