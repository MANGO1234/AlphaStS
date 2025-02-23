package com.alphaStS.card;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CardUpgrade {
    public static Map<Card, Card> map;
    static {
        map = new HashMap<>();
        map.put(new Card.Strike(), new Card.StrikeP());
        map.put(new Card.Defend(), new Card.DefendP());

        map.put(new CardIronclad.Bash(), new CardIronclad.BashP());
        map.put(new CardIronclad.Anger(), new CardIronclad.AngerP());
        map.put(new CardIronclad.Armanent(), new CardIronclad.ArmanentP());
        map.put(new CardIronclad.BodySlam(), new CardIronclad.BodySlamP());
        map.put(new CardIronclad.Clash(), new CardIronclad.ClashP());
        map.put(new CardIronclad.Cleave(), new CardIronclad.CleaveP());
        map.put(new CardIronclad.Clothesline(), new CardIronclad.ClotheslineP());
        map.put(new CardIronclad.Flex(), new CardIronclad.FlexP());
        map.put(new CardIronclad.Havoc(), new CardIronclad.HavocP());
        map.put(new CardIronclad.Headbutt(), new CardIronclad.HeadbuttP());
        map.put(new CardIronclad.HeavyBlade(), new CardIronclad.HeavyBladeP());
        map.put(new CardIronclad.IronWave(), new CardIronclad.IronWaveP());
        map.put(new CardIronclad.PerfectedStrike(), new CardIronclad.PerfectedStrikeP());
        map.put(new CardIronclad.PommelStrike(), new CardIronclad.PommelStrikeP());
        map.put(new CardIronclad.ShrugItOff(), new CardIronclad.ShrugItOffP());
        map.put(new CardIronclad.SwordBoomerang(), new CardIronclad.SwordBoomerangP());
        map.put(new CardIronclad.Thunderclap(), new CardIronclad.ThunderclapP());
        map.put(new CardIronclad.TrueGrit(), new CardIronclad.TrueGritP());
        map.put(new CardIronclad.TwinStrike(), new CardIronclad.TwinStrikeP());
        map.put(new CardIronclad.Warcry(), new CardIronclad.WarcryP());
        map.put(new CardIronclad.WildStrike(), new CardIronclad.WildStrikeP());
        map.put(new CardIronclad.BattleTrance(), new CardIronclad.BattleTranceP());
        map.put(new CardIronclad.BloodForBlood(), new CardIronclad.BloodForBloodP());
        map.put(new CardIronclad.Bloodletting(), new CardIronclad.BloodlettingP());
        map.put(new CardIronclad.BurningPact(), new CardIronclad.BurningPactP());
        map.put(new CardIronclad.Carnage(), new CardIronclad.CarnageP());
        map.put(new CardIronclad.Combust(), new CardIronclad.CombustP());
        map.put(new CardIronclad.DarkEmbrace(), new CardIronclad.DarkEmbraceP());
        map.put(new CardIronclad.Disarm(), new CardIronclad.DisarmP());
        map.put(new CardIronclad.Dropkick(), new CardIronclad.DropkickP());
        map.put(new CardIronclad.DualWield(), new CardIronclad.DualWieldP());
        map.put(new CardIronclad.Entrench(), new CardIronclad.EntrenchP());
        map.put(new CardIronclad.Evolve(), new CardIronclad.EvolveP());
        map.put(new CardIronclad.FeelNoPain(), new CardIronclad.FeelNoPainP());
        map.put(new CardIronclad.FireBreathing(), new CardIronclad.FireBreathingP());
        map.put(new CardIronclad.FlameBarrier(), new CardIronclad.FlameBarrierP());
        map.put(new CardIronclad.GhostlyArmor(), new CardIronclad.GhostlyArmorP());
        map.put(new CardIronclad.Hemokinesis(), new CardIronclad.HemokinesisP());
        map.put(new CardIronclad.InfernalBlade(), new CardIronclad.InfernalBladeP());
        map.put(new CardIronclad.Inflame(), new CardIronclad.InflameP());
        map.put(new CardIronclad.Intimidate(), new CardIronclad.IntimidateP());
        map.put(new CardIronclad.Metallicize(), new CardIronclad.MetallicizeP());
        map.put(new CardIronclad.PowerThrough(), new CardIronclad.PowerThroughP());
        map.put(new CardIronclad.Pummel(), new CardIronclad.PummelP());
        map.put(new CardIronclad.Rage(), new CardIronclad.RageP());
        map.put(new CardIronclad.Rampage(), new CardIronclad.RampageP());
        map.put(new CardIronclad.RecklessCharge(), new CardIronclad.RecklessChargeP());
        map.put(new CardIronclad.Rupture(), new CardIronclad.RuptureP());
        map.put(new CardIronclad.SecondWind(), new CardIronclad.SecondWindP());
        map.put(new CardIronclad.SeeingRed(), new CardIronclad.SeeingRedP());
        map.put(new CardIronclad.Sentinel(), new CardIronclad.SentinelP());
        map.put(new CardIronclad.SeverSoul(), new CardIronclad.SeverSoulP());
        map.put(new CardIronclad.Shockwave(), new CardIronclad.ShockwaveP());
        map.put(new CardIronclad.SpotWeakness(), new CardIronclad.SpotWeaknessP());
        map.put(new CardIronclad.Uppercut(), new CardIronclad.UppercutP());
        map.put(new CardIronclad.Whirlwind(), new CardIronclad.WhirlwindP());
        map.put(new CardIronclad.Barricade(), new CardIronclad.BarricadeP());
        map.put(new CardIronclad.Berserk(), new CardIronclad.BerserkP());
        map.put(new CardIronclad.Bludgeon(), new CardIronclad.BludgeonP());
        map.put(new CardIronclad.Brutality(), new CardIronclad.BrutalityP());
        map.put(new CardIronclad.Corruption(), new CardIronclad.CorruptionP());
        map.put(new CardIronclad.DemonForm(), new CardIronclad.DemonFormP());
        map.put(new CardIronclad.DoubleTap(), new CardIronclad.DoubleTapP());
        map.put(new CardIronclad.Exhume(), new CardIronclad.ExhumeP());
        map.put(new CardIronclad.FiendFire(), new CardIronclad.FiendFireP());
        map.put(new CardIronclad.Immolate(), new CardIronclad.ImmolateP());
        map.put(new CardIronclad.Impervious(), new CardIronclad.ImperviousP());
        map.put(new CardIronclad.Juggernaut(), new CardIronclad.JuggernautP());
        map.put(new CardIronclad.LimitBreak(), new CardIronclad.LimitBreakP());
        map.put(new CardIronclad.Offering(), new CardIronclad.OfferingP());
        map.put(new CardIronclad.Reaper(), new CardIronclad.ReaperP());

        map.put(new CardSilent.Neutralize(), new CardSilent.NeutralizeP());
        map.put(new CardSilent.Survivor(), new CardSilent.SurvivorP());
        map.put(new CardSilent.Acrobatics(), new CardSilent.AcrobaticsP());
        map.put(new CardSilent.Backflip(), new CardSilent.BackflipP());
        map.put(new CardSilent.Bane(), new CardSilent.BaneP());
        map.put(new CardSilent.BladeDance(), new CardSilent.BladeDanceP());
        map.put(new CardSilent.CloakAndDagger(), new CardSilent.CloakAndDaggerP());
        map.put(new CardSilent.DaggerSpray(), new CardSilent.DaggerSprayP());
        map.put(new CardSilent.DaggerThrow(), new CardSilent.DaggerThrowP());
        map.put(new CardSilent.DeadlyPoison(), new CardSilent.DeadlyPoisonP());
        map.put(new CardSilent.Deflect(), new CardSilent.DeflectP());
        map.put(new CardSilent.DodgeAndRoll(), new CardSilent.DodgeAndRollP());
        map.put(new CardSilent.FlyingKnee(), new CardSilent.FlyingKneeP());
        map.put(new CardSilent.Outmaneuver(), new CardSilent.OutmaneuverP());
        map.put(new CardSilent.PiercingWail(), new CardSilent.PiercingWailP());
        map.put(new CardSilent.PoisonedStab(), new CardSilent.PoisonedStabP());
        map.put(new CardSilent.Prepared(), new CardSilent.PreparedP());
        map.put(new CardSilent.QuickSlash(), new CardSilent.QuickSlashP());
        map.put(new CardSilent.Slice(), new CardSilent.SliceP());
        map.put(new CardSilent.SneakyStrike(), new CardSilent.SneakyStrikeP());
        map.put(new CardSilent.SuckerPunch(), new CardSilent.SuckerPunchP());
        map.put(new CardSilent.Accuracy(), new CardSilent.AccuracyP());
        map.put(new CardSilent.AllOutAttack(), new CardSilent.AllOutAttackP());
        map.put(new CardSilent.Backstab(), new CardSilent.BackstabP());
        map.put(new CardSilent.Blur(), new CardSilent.BlurP());
        map.put(new CardSilent.BouncingFlask(), new CardSilent.BouncingFlaskP());
        map.put(new CardSilent.CalculatedGamble(), new CardSilent.CalculatedGambleP());
        map.put(new CardSilent.Caltrops(), new CardSilent.CaltropsP());
        map.put(new CardSilent.Catalyst(), new CardSilent.CatalystP());
        map.put(new CardSilent.Choke(), new CardSilent.ChokeP());
        map.put(new CardSilent.Concentrate(), new CardSilent.ConcentrateP());
        map.put(new CardSilent.CripplingCloud(), new CardSilent.CripplingCloudP());
        map.put(new CardSilent.Dash(), new CardSilent.DashP());
        map.put(new CardSilent.Distraction(), new CardSilent.DistractionP());
        map.put(new CardSilent.EndlessAgony(), new CardSilent.EndlessAgonyP());
        map.put(new CardSilent.EscapePlan(), new CardSilent.EscapePlanP());
        map.put(new CardSilent.Eviscerate(), new CardSilent.EviscerateP());
        map.put(new CardSilent.Expertise(), new CardSilent.ExpertiseP());
        map.put(new CardSilent.Finisher(), new CardSilent.FinisherP());
        map.put(new CardSilent.Flechette(), new CardSilent.FlechetteP());
        map.put(new CardSilent.Footwork(), new CardSilent.FootworkP());
        map.put(new CardSilent.HeelHook(), new CardSilent.HeelHookP());
        map.put(new CardSilent.InfiniteBlade(), new CardSilent.InfiniteBladeP());
        map.put(new CardSilent.LegSweep(), new CardSilent.LegSweepP());
//        map.put(new CardSilent.MasterfulStab(), new CardSilent.MasterfulStabP());
        map.put(new CardSilent.NoxiousFume(), new CardSilent.NoxiousFumeP());
        map.put(new CardSilent.Predator(), new CardSilent.PredatorP());
        map.put(new CardSilent.Reflex(), new CardSilent.ReflexP());
        map.put(new CardSilent.RiddleWithHoles(), new CardSilent.RiddleWithHolesP());
//        map.put(new CardSilent.Setup(), new CardSilent.SetupP());
        map.put(new CardSilent.Skewer(), new CardSilent.SkewerP());
        map.put(new CardSilent.Tactician(), new CardSilent.TacticianP());
        map.put(new CardSilent.Terror(), new CardSilent.TerrorP());
        map.put(new CardSilent.WellLaidPlans(), new CardSilent.WellLaidPlansP());
        map.put(new CardSilent.AThousandCuts(), new CardSilent.AThousandCutsP());
        map.put(new CardSilent.Adrenaline(), new CardSilent.AdrenalineP());
        map.put(new CardSilent.AfterImage(), new CardSilent.AfterImageP());
//        map.put(new CardSilent.Alchemize(), new CardSilent.AlchemizeP());
        map.put(new CardSilent.BulletTime(), new CardSilent.BulletTimeP());
        map.put(new CardSilent.Burst(), new CardSilent.BurstP());
        map.put(new CardSilent.CorpseExplosion(), new CardSilent.CorpseExplosionP());
        map.put(new CardSilent.DieDieDie(), new CardSilent.DieDieDieP());
        map.put(new CardSilent.Doppelganger(), new CardSilent.DoppelgangerP());
        map.put(new CardSilent.Envenom(), new CardSilent.EnvenomP());
        map.put(new CardSilent.GlassKnife(), new CardSilent.GlassKnifeP());
        map.put(new CardSilent.GrandFinale(), new CardSilent.GrandFinaleP());
        map.put(new CardSilent.Malaise(), new CardSilent.MalaiseP());
        map.put(new CardSilent.Nightmare(), new CardSilent.NightmareP());
        map.put(new CardSilent.PhantasmalKiller(), new CardSilent.PhantasmalKillerP());
        map.put(new CardSilent.StormOfSteel(), new CardSilent.StormOfSteelP());
        map.put(new CardSilent.ToolsOfTheTrade(), new CardSilent.ToolsOfTheTradeP());
        map.put(new CardSilent.Unload(), new CardSilent.UnloadP());
        map.put(new CardSilent.WraithForm(), new CardSilent.WraithFormP());

        map.put(new CardDefect.DualCast(), new CardDefect.DualCastP());
        map.put(new CardDefect.Zap(), new CardDefect.ZapP());
        map.put(new CardDefect.BallLightning(), new CardDefect.BallLightningP());
        map.put(new CardDefect.Barrage(), new CardDefect.BarrageP());
        map.put(new CardDefect.BeamCell(), new CardDefect.BeamCellP());
        map.put(new CardDefect.ChargeBattery(), new CardDefect.ChargeBatteryP());
//        map.put(new CardDefect.Claw(3), new CardDefect.ClawP(5));
        map.put(new CardDefect.ColdSnap(), new CardDefect.ColdSnapP());
        map.put(new CardDefect.CompiledDriver(), new CardDefect.CompiledDriverP());
        map.put(new CardDefect.Coolheaded(), new CardDefect.CoolheadedP());
        map.put(new CardDefect.GoForTheEye(), new CardDefect.GoForTheEyeP());
        map.put(new CardDefect.Hologram(), new CardDefect.HologramP());
        map.put(new CardDefect.Leap(), new CardDefect.LeapP());
        map.put(new CardDefect.Rebound(), new CardDefect.ReboundP());
        map.put(new CardDefect.Recursion(), new CardDefect.RecursionP());
        map.put(new CardDefect.Stack(), new CardDefect.StackP());
//        map.put(new CardDefect.SteamBarrier(6), new CardDefect.SteamBarrierP(8));
        map.put(new CardDefect.Streamline(), new CardDefect.StreamlineP());
        map.put(new CardDefect.SweepingBeam(), new CardDefect.SweepingBeamP());
        map.put(new CardDefect.Turbo(), new CardDefect.TurboP());
        map.put(new CardDefect.Aggregate(), new CardDefect.AggregateP());
        map.put(new CardDefect.AutoShields(), new CardDefect.AutoShieldsP());
        map.put(new CardDefect.Blizzard(), new CardDefect.BlizzardP());
        map.put(new CardDefect.BootSequence(), new CardDefect.BootSequenceP());
        map.put(new CardDefect.BullsEye(), new CardDefect.BullsEyeP());
        map.put(new CardDefect.Capacitor(), new CardDefect.CapacitorP());
        map.put(new CardDefect.Chaos(), new CardDefect.ChaosP());
        map.put(new CardDefect.Chill(), new CardDefect.ChillP());
        map.put(new CardDefect.Consume(), new CardDefect.ConsumeP());
        map.put(new CardDefect.Darkness(), new CardDefect.DarknessP());
        map.put(new CardDefect.Defragment(), new CardDefect.DefragmentP());
        map.put(new CardDefect.DoomAndGloom(), new CardDefect.DoomAndGloomP());
        map.put(new CardDefect.DoubleEnergy(), new CardDefect.DoubleEnergyP());
        map.put(new CardDefect.Equilibrium(), new CardDefect.EquilibriumP());
        map.put(new CardDefect.FTL(), new CardDefect.FTLP());
        map.put(new CardDefect.ForceField(), new CardDefect.ForceFieldP());
        map.put(new CardDefect.Fusion(), new CardDefect.FusionP());
//        map.put(new CardDefect.GeneticAlgorithm(1), new CardDefect.GeneticAlgorithmP(1));
        map.put(new CardDefect.Glacier(), new CardDefect.GlacierP());
        map.put(new CardDefect.Heatsinks(), new CardDefect.HeatsinksP());
        map.put(new CardDefect.HelloWorld(), new CardDefect.HelloWorldP());
        map.put(new CardDefect.Loop(), new CardDefect.LoopP());
        map.put(new CardDefect.Melter(), new CardDefect.MelterP());
        map.put(new CardDefect.Overclock(), new CardDefect.OverclockP());
        map.put(new CardDefect.Recycle(), new CardDefect.RecycleP());
        map.put(new CardDefect.ReinforcedBody(), new CardDefect.ReinforcedBodyP());
        map.put(new CardDefect.Reprogram(), new CardDefect.ReprogramP());
        map.put(new CardDefect.RipAndTear(), new CardDefect.RipAndTearP());
        map.put(new CardDefect.Scrape(), new CardDefect.ScrapeP());
        map.put(new CardDefect.SelfRepair(), new CardDefect.SelfRepairP());
        map.put(new CardDefect.Skim(), new CardDefect.SkimP());
        map.put(new CardDefect.StaticDischarge(), new CardDefect.StaticDischargeP());
        map.put(new CardDefect.Storm(), new CardDefect.StormP());
        map.put(new CardDefect.Sunder(), new CardDefect.SunderP());
        map.put(new CardDefect.Tempest(), new CardDefect.TempestP());
        map.put(new CardDefect.WhiteNoise(), new CardDefect.WhiteNoiseP());
//        map.put(new CardDefect.AllForOne(0, 0), new CardDefect.AllForOneP(0, 0));
        map.put(new CardDefect.Amplify(), new CardDefect.AmplifyP());
        map.put(new CardDefect.BiasedCognition(), new CardDefect.BiasedCognitionP());
        map.put(new CardDefect.Buffer(), new CardDefect.BufferP());
        map.put(new CardDefect.CoreSurge(), new CardDefect.CoreSurgeP());
        map.put(new CardDefect.CreativeAI(), new CardDefect.CreativeAIP());
        map.put(new CardDefect.EchoForm(), new CardDefect.EchoFormP());
        map.put(new CardDefect.Electrodynamics(), new CardDefect.ElectrodynamicsP());
        map.put(new CardDefect.Fission(), new CardDefect.FissionP());
        map.put(new CardDefect.HyperBeam(), new CardDefect.HyperBeamP());
        map.put(new CardDefect.MachineLearning(), new CardDefect.MachineLearningP());
        map.put(new CardDefect.MeteorStrike(), new CardDefect.MeteorStrikeP());
        map.put(new CardDefect.MultiCast(), new CardDefect.MultiCastP());
        map.put(new CardDefect.Rainbow(), new CardDefect.RainbowP());
        map.put(new CardDefect.Reboot(), new CardDefect.RebootP());
        map.put(new CardDefect.Seek(), new CardDefect.SeekP());
        map.put(new CardDefect.ThunderStrike(), new CardDefect.ThunderStrikeP());

        map.put(new CardColorless.Apparition(), new CardColorless.ApparitionP());
        map.put(new CardColorless.Blind(), new CardColorless.BlindP());
        map.put(new CardColorless.DarkShackles(), new CardColorless.DarkShacklesP());
        map.put(new CardColorless.DeepBreath(), new CardColorless.DeepBreathP());
        map.put(new CardColorless.DramaticEntrance(), new CardColorless.DramaticEntranceP());
        map.put(new CardColorless.Enlightenment(), new CardColorless.EnlightenmentP());
        map.put(new CardColorless.Finesse(), new CardColorless.FinesseP());
        map.put(new CardColorless.FlashOfSteel(), new CardColorless.FlashOfSteelP());
        map.put(new CardColorless.GoodInstincts(), new CardColorless.GoodInstinctsP());
        map.put(new CardColorless.Impatience(), new CardColorless.ImpatienceP());
        map.put(new CardColorless.MindBlast(), new CardColorless.MindBlastP());
        map.put(new CardColorless.Panacea(), new CardColorless.PanaceaP());
        map.put(new CardColorless.PanicButton(), new CardColorless.PanicButtonP());
        map.put(new CardColorless.SwiftStrike(), new CardColorless.SwiftStrikeP());
        map.put(new CardColorless.Trip(), new CardColorless.TripP());
        map.put(new CardColorless.Apotheosis(), new CardColorless.ApotheosisP());
//        map.put(new CardColorless.HandOfGreed(), new CardColorless.HandOfGreedP());
        map.put(new CardColorless.MasterOfStrategy(), new CardColorless.MasterOfStrategyP());
        map.put(new CardColorless.Panache(), new CardColorless.PanacheP());
        map.put(new CardColorless.SecretTechnique(), new CardColorless.SecretTechniqueP());
        map.put(new CardColorless.SecretWeapon(), new CardColorless.SecretWeaponP());
        map.put(new CardColorless.TheBomb(), new CardColorless.TheBombP());
        map.put(new CardColorless.ThinkingAhead(), new CardColorless.ThinkingAheadP());
        map.put(new CardColorless.Violence(), new CardColorless.ViolenceP());
        map.put(new CardColorless.Bite(), new CardColorless.BiteP());
//        map.put(new CardColorless.RitualDagger(), new CardColorless.RitualDaggerP());
        map.put(new CardColorless.Shiv(), new CardColorless.ShivP());
        map.put(new CardColorless.Apparition(), new CardColorless.ApparitionP());

        map = Collections.unmodifiableMap(map);
    }
}
