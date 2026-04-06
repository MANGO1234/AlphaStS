package com.alphaStS.enemy;

import com.alphaStS.GameState;
import com.alphaStS.GameStateBuilder;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public enum PredefinedEncounter {
    // Act 1
    CULTIST("Cultist", 1, List.of(new EnemyExordium.Cultist())),
    JAW_WORM("Jaw Worm", 1, List.of(new EnemyExordium.JawWorm(false))),
    BLUE_SLAVER("Blue Slaver", 1, List.of(new EnemyExordium.BlueSlaver())),
    RED_SLAVER("Red Slaver", 1, List.of(new EnemyExordium.RedSlaver())),
    DUAL_FUNGI_BEASTS("2 Fungi Beasts", 1, List.of(new EnemyExordium.FungiBeast(), new EnemyExordium.FungiBeast())),
    LOOTER("Looter", 1, List.of(new EnemyExordium.Looter())),
    LARGE_SPIKE_SLIME("Large Slime", 1, List.of(new EnemyExordium.LargeSpikeSlime(),
            new EnemyExordium.MediumSpikeSlime(36).startDead(),
            new EnemyExordium.MediumSpikeSlime(36).startDead())),
    ACID_SLIME("Large Slime", 1, List.of(new EnemyExordium.LargeAcidSlime(),
            new EnemyExordium.MediumAcidSlime(36).startDead(),
            new EnemyExordium.MediumAcidSlime(36).startDead())),
    SLAVERS_ELITE("Slavers", 1, List.of(new EnemyExordium.BlueSlaver().asElite(),
            new EnemyCity.Taskmaster(),
            new EnemyExordium.RedSlaver().asElite())),
    SENTRIES("3 Sentries", 1, List.of(new EnemyExordium.Sentry(45, EnemyExordium.Sentry.BOLT),
            new EnemyExordium.Sentry(45, EnemyExordium.Sentry.BEAM),
            new EnemyExordium.Sentry(45, EnemyExordium.Sentry.BOLT)),
            EnemyEncounter.FIRST_AND_LAST_REORDERING),
    GREMLIN_NOB("Gremlin Nob", 1, List.of(new EnemyExordium.GremlinNob())),
    LAGAVULIN("Lagavulin", 1, List.of(new EnemyExordium.Lagavulin())),
    GUARDIAN("The Guardian", 1, List.of(new EnemyExordium.TheGuardian())),
    HEXAGHOST("Hexaghost", 1, List.of(new EnemyExordium.Hexaghost())),
    // todo: in some situations, order matters, create a variant where order matters (would require 20 enemies instead of 8)
    GREMLIN_GANG("Gremlin Gang", 1, List.of(new EnemyExordium.MadGremlin(), new EnemyExordium.MadGremlin(),
            new EnemyExordium.SneakyGremlin(), new EnemyExordium.SneakyGremlin(),
            new EnemyExordium.FatGremlin(), new EnemyExordium.FatGremlin(),
            new EnemyExordium.ShieldGremlin(), new EnemyExordium.GremlinWizard())),
    SLIME_BOSS("Slime Boss", 1, List.of(new EnemyExordium.SlimeBoss(),
            new EnemyExordium.LargeSpikeSlime(75).startDead(),
            new EnemyExordium.MediumSpikeSlime(37).startDead(),
            new EnemyExordium.MediumSpikeSlime(37).startDead(),
            new EnemyExordium.LargeAcidSlime(75).startDead(),
            new EnemyExordium.MediumAcidSlime(37).startDead(),
            new EnemyExordium.MediumAcidSlime(37).startDead())),

    // Act 2
    SPHERIC_GUARDIAN("Spheric Guardian", 2, List.of(new EnemyCity.SphericGuardian())),
    CHOSEN("Chosen", 2, List.of(new EnemyCity.Chosen())),
    SHELLED_PARASITE("Shell Parasite", 2, List.of(new EnemyCity.ShelledParasite())),
    TRIPLE_BYRDS("3 Byrds", 2, List.of(new EnemyCity.Byrd(), new EnemyCity.Byrd(), new EnemyCity.Byrd()),
            EnemyEncounter.TRIPLE_ENEMIES_REORDERING),
    LOOTER_AND_MUGGER("2 Thieves", 2, List.of(new EnemyExordium.Looter(), new EnemyExordium.Mugger())),
    CHOSEN_AND_BYRD("Chosen and Byrds", 2, List.of(new EnemyCity.Chosen(), new EnemyCity.Byrd())),
    CULTIST_AND_CHOSEN("Cultist and Chosen", 2, List.of(new EnemyExordium.Cultist(), new EnemyCity.Chosen())),
    SENTRY_AND_SPHERIC_GUARDIAN("Sentry and Sphere", 2, List.of(new EnemyExordium.Sentry(EnemyExordium.Sentry.BOLT).notElite(),
            new EnemyCity.SphericGuardian())),
    SNAKE_PLANT("Snake Plant", 2, List.of(new EnemyCity.SnakePlant())),
    SNECKO("Snecko", 2, List.of(new EnemyCity.Snecko())),
    CENTURION_AND_MYSTIC("Centurion and Healer", 2, List.of(new EnemyCity.Centurion(), new EnemyCity.Mystic())),
    TRIPLE_CULTISTS("3 Cultists", 2, List.of(new EnemyExordium.Cultist(), new EnemyExordium.Cultist(), new EnemyExordium.Cultist()),
            EnemyEncounter.TRIPLE_ENEMIES_REORDERING),
    SHELLED_PARASITE_AND_FUNGI_BEAST("Shelled Parasite and Fungi", 2, List.of(new EnemyCity.ShelledParasite(), new EnemyExordium.FungiBeast())),
    ROBBERS_EVENT("Masked Bandits", 2, List.of(new EnemyCity.Pointy(), new EnemyCity.Romeo(), new EnemyCity.Bear())),
    GREMLIN_LEADER("Gremlin Leader", 2, List.of(), EnemyEncounter::getGremlinLeaderFightEnemies, EnemyEncounter.GREMLIN_LEADER_REORDERING),
    BRONZE_AUTOMATON("Automaton", 2, List.of(new EnemyCity.BronzeOrb().startDead(), new EnemyCity.BronzeAutomaton(), new EnemyCity.BronzeOrb().startDead())),
    COLLECTOR("Collector", 2, List.of(new EnemyCity.TorchHead().startDead(), new EnemyCity.TorchHead().startDead(), new EnemyCity.TheCollector())),
    BOOK_OF_STABBING("Book of Stabbing", 2, List.of(new EnemyCity.BookOfStabbing())),
    THE_CHAMP("Champ", 2, List.of(new EnemyCity.TheChamp())),

    // Act 3
    TRIPLE_DARKLINGS("3 Darklings", 3, List.of(new EnemyBeyond.Darkling(false), new EnemyBeyond.Darkling(true), new EnemyBeyond.Darkling(false)),
            EnemyEncounter.FIRST_AND_LAST_REORDERING),
    ORB_WALKER("Orb Walker", 3, List.of(new EnemyBeyond.OrbWalker())),
    MAW("Maw", 3, List.of(new EnemyBeyond.TheMaw())),
    WRITHING_MASS("Writhing Mass", 3, List.of(new EnemyBeyond.WrithingMass(0.8f))),
    TRIPLE_JAW_WORMS("Jaw Worm Horde", 3, List.of(new EnemyExordium.JawWorm(true), new EnemyExordium.JawWorm(true), new EnemyExordium.JawWorm(true)),
            EnemyEncounter.TRIPLE_ENEMIES_REORDERING),
    SPIRE_GROWTH("Spire Growth", 3, List.of(new EnemyBeyond.SpireGrowth())),
    TRANSIENT("Transient", 3, List.of(new EnemyBeyond.Transient())),
    REPTOMANCER("Reptomancer", 3, List.of(new EnemyBeyond.Dagger(),
            new EnemyBeyond.Dagger(),
            new EnemyBeyond.Reptomancer(),
            new EnemyBeyond.Dagger(),
            new EnemyBeyond.Dagger())),
    DONU_AND_DECA("Donu and Deca", 3, List.of(new EnemyBeyond.Deca(), new EnemyBeyond.Donu())),
    TIME_EATER("Time Eater", 3, List.of(new EnemyBeyond.TimeEater())),
    GIANT_HEAD("Giant Head", 3, List.of(new EnemyBeyond.GiantHead())),
    NEMESIS("Nemesis", 3, List.of(new EnemyBeyond.Nemesis())),
    AWAKENED_ONE("Awakened One", 3, List.of(new EnemyExordium.Cultist(), new EnemyExordium.Cultist(), new EnemyBeyond.AwakenedOne()),
            EnemyEncounter.AWAKENED_ONE_REORDERING),

    // Act 4
    SPEAR_AND_SHIELD("Shield and Spear", 4, List.of(new EnemyEnding.SpireShield(), new EnemyEnding.SpireSpear())),
    CORRUPT_HEART("The Heart", 4, List.of(new EnemyEnding.CorruptHeart()));

    public final int act;
    public final String internalKey;
    public List<Enemy> enemies;
    public Supplier<List<Enemy>> enemiesSupplier;
    public Consumer<GameStateBuilder> encounterExtraLogic;
    public EnemyEncounter.EnemyReordering reordering;
    public BiFunction<GameState, Boolean, Double> calcFightProgress;

    static {
        SLIME_BOSS.calcFightProgress = PredefinedEncounter::calcSlimeBossFightProgress;
        AWAKENED_ONE.calcFightProgress = PredefinedEncounter::calcAwakenedOneFightProgress;
        BRONZE_AUTOMATON.calcFightProgress = PredefinedEncounter::calcBronzeAutomatonFightProgress;
        COLLECTOR.calcFightProgress = PredefinedEncounter::calcCollectorFightProgress;
        GREMLIN_LEADER.calcFightProgress = PredefinedEncounter::calcGremlinLeaderFightProgress;
        CORRUPT_HEART.calcFightProgress = PredefinedEncounter::calcCorruptHeartFightProgress;
    }

    PredefinedEncounter(String internalKey, int act, List<Enemy> enemies, Consumer<GameStateBuilder> encounterExtraLogic,
            EnemyEncounter.EnemyReordering reordering) {
        this.internalKey = internalKey;
        this.act = act;
        this.enemies = enemies;
        this.encounterExtraLogic = encounterExtraLogic;
        this.reordering = reordering;
    }

    PredefinedEncounter(String internalKey, int act, List<Enemy> enemies, EnemyEncounter.EnemyReordering reordering) {
        this.internalKey = internalKey;
        this.act = act;
        this.enemies = enemies;
        this.reordering = reordering;
    }

    PredefinedEncounter(String internalKey, int act, Supplier<List<Enemy>> enemiesSupplier, EnemyEncounter.EnemyReordering reordering) {
        this.internalKey = internalKey;
        this.act = act;
        this.enemiesSupplier = enemiesSupplier;
        this.reordering = reordering;
    }

    PredefinedEncounter(String internalKey, int act, List<Enemy> enemies) {
        this.internalKey = internalKey;
        this.act = act;
        this.enemies = enemies;
    }

    private static double calcSlimeBossFightProgress(GameState state, boolean onlyHeart) {
        int totalMaxHp = 0;
        int totalCurHp = 0;
        boolean isSlimeBossAlive = false;
        boolean isSpikeSlimeLAlive = false;
        boolean isAcidSlimeLAlive = false;
        for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
            EnemyReadOnly enemy = state.getEnemiesForRead().get(i);
            boolean addedMod = false;
            if (enemy instanceof EnemyExordium.SlimeBoss boss) {
                isSlimeBossAlive = boss.getHealth() > 0;
            } else if (enemy instanceof EnemyExordium.LargeSpikeSlime slime) {
                isSpikeSlimeLAlive = slime.getHealth() > 0;
                if (isSlimeBossAlive) {
                    totalCurHp += enemy.properties.maxHealth;
                    totalMaxHp += enemy.properties.maxHealth;
                    addedMod = true;
                }
            } else if (enemy instanceof EnemyExordium.LargeAcidSlime slime) {
                isAcidSlimeLAlive = slime.getHealth() > 0;
                if (isSlimeBossAlive) {
                    totalCurHp += enemy.properties.maxHealth;
                    totalMaxHp += enemy.properties.maxHealth;
                    addedMod = true;
                }
            } else if (enemy instanceof EnemyExordium.MediumSpikeSlime) {
                if (isSlimeBossAlive || isSpikeSlimeLAlive) {
                    totalCurHp += enemy.properties.maxHealth;
                    totalMaxHp += enemy.properties.maxHealth;
                    addedMod = true;
                }
            } else if (enemy instanceof EnemyExordium.MediumAcidSlime) {
                if (isSlimeBossAlive || isAcidSlimeLAlive) {
                    totalCurHp += enemy.properties.maxHealth;
                    totalMaxHp += enemy.properties.maxHealth;
                    addedMod = true;
                }
            }
            if (!addedMod) {
                totalCurHp += enemy.getHealth();
                totalMaxHp += enemy.properties.maxHealth;
            }
        }
        return 1 - ((double) totalCurHp) / totalMaxHp;
    }

    private static double calcAwakenedOneFightProgress(GameState state, boolean onlyHeart) {
        int totalMaxHp = 0;
        int totalCurHp = 0;
        for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
            EnemyReadOnly enemy = state.getEnemiesForRead().get(i);
            if (enemy instanceof EnemyBeyond.AwakenedOne ao) {
                totalCurHp += ao.isAwakened() ? ao.getHealth() : (ao.getHealth() + enemy.properties.maxHealth);
                totalMaxHp += enemy.properties.maxHealth * 2;
            } else {
                totalCurHp += enemy.getHealth();
                totalMaxHp += enemy.properties.maxHealth;
            }
        }
        return 1 - ((double) totalCurHp) / totalMaxHp;
    }

    private static double calcBronzeAutomatonFightProgress(GameState state, boolean onlyHeart) {
        int totalMaxHp = 0;
        int totalCurHp = 0;
        for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
            EnemyReadOnly enemy = state.getEnemiesForRead().get(i);
            if (enemy instanceof EnemyCity.BronzeAutomaton ba) {
                if (ba.getMove() <= EnemyCity.BronzeAutomaton.SPAWN_ORBS) {
                    totalCurHp += 60 * 2; // the orbs
                }
            }
            totalCurHp += enemy.getHealth();
            totalMaxHp += enemy.properties.maxHealth;
        }
        return 1 - ((double) totalCurHp) / totalMaxHp;
    }

    private static double calcCollectorFightProgress(GameState state, boolean onlyHeart) {
        int totalMaxHp = 0;
        int totalCurHp = 0;
        for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
            EnemyReadOnly enemy = state.getEnemiesForRead().get(i);
            if (enemy instanceof EnemyCity.TorchHead) {
                continue;
            }
            totalCurHp += enemy.getHealth();
            totalMaxHp += enemy.properties.maxHealth;
        }
        return 1 - ((double) totalCurHp) / totalMaxHp;
    }

    private static double calcGremlinLeaderFightProgress(GameState state, boolean onlyHeart) {
        int totalMaxHp = 0;
        int totalCurHp = 0;
        boolean isGremlinLeaderAlive = false;
        for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
            if (state.getEnemiesForRead().get(i) instanceof EnemyCity.GremlinLeader) {
                isGremlinLeaderAlive = true;
                break;
            }
        }
        for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
            EnemyReadOnly enemy = state.getEnemiesForRead().get(i);
            if (isGremlinLeaderAlive && (enemy instanceof EnemyExordium.FatGremlin || enemy instanceof EnemyExordium.GremlinWizard ||
                    enemy instanceof EnemyExordium.MadGremlin || enemy instanceof EnemyExordium.ShieldGremlin || enemy instanceof EnemyExordium.SneakyGremlin)) {
                continue;
            }
            totalCurHp += enemy.getHealth();
            totalMaxHp += enemy.properties.maxHealth;
        }
        return 1 - ((double) totalCurHp) / totalMaxHp;
    }

    private static double calcCorruptHeartFightProgress(GameState state, boolean onlyHeart) {
        int totalMaxHp = 0;
        int totalCurHp = 0;
        for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
            EnemyReadOnly enemy = state.getEnemiesForRead().get(i);
            boolean addedMod = false;
            if (state.properties.isHeartGauntlet && enemy instanceof EnemyEnding.CorruptHeart) {
                if (state.getEnemiesForRead().get(0).getHealth() > 0 || state.getEnemiesForRead().get(1).getHealth() > 0) {
                    if (onlyHeart) {
                        totalCurHp = 0;
                        totalMaxHp = enemy.properties.maxHealth;
                    } else {
                        totalCurHp += enemy.properties.maxHealth;
                        totalMaxHp += enemy.properties.maxHealth;
                        addedMod = true;
                    }
                } else if (onlyHeart) {
                    totalCurHp = enemy.getHealth();
                    totalMaxHp = enemy.properties.maxHealth;
                }
            }
            if (!addedMod) {
                totalCurHp += enemy.getHealth();
                totalMaxHp += enemy.properties.maxHealth;
            }
        }
        return 1 - ((double) totalCurHp) / totalMaxHp;
    }
}
