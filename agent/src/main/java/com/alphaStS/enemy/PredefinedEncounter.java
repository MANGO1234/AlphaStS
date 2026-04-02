package com.alphaStS.enemy;

import com.alphaStS.GameStateBuilder;

import java.util.List;
import java.util.function.Consumer;

public enum PredefinedEncounter {
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
    GREMLIN_LEADER(2, List.of(), EnemyEncounter::addGremlinLeaderFight),
    BRONZE_AUTOMATON(2, List.of(new EnemyCity.BronzeOrb().startDead(), new EnemyCity.BronzeAutomaton(), new EnemyCity.BronzeOrb().startDead())),
    COLLECTOR(2, List.of(new EnemyCity.TorchHead().startDead(), new EnemyCity.TorchHead().startDead(), new EnemyCity.TheCollector())),
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
