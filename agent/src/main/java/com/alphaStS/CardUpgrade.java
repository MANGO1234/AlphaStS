package com.alphaStS;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CardUpgrade {
    public static Map<Card, Card> map;
    static {
        map = new HashMap<>();
        map.put(new Card.Bash(), new Card.BashP());
        map.put(new Card.Strike(), new Card.StrikeP());
        map.put(new Card.Defend(), new Card.DefendP());
        map.put(new Card.Anger(), new Card.AngerP());
        map.put(new Card.Armanent(), new Card.ArmanentP());
        map.put(new Card.BodySlam(), new Card.BodySlamP());
        map.put(new Card.Clash(), new Card.ClashP());
        map.put(new Card.Cleave(), new Card.CleaveP());
        map.put(new Card.Clothesline(), new Card.ClotheslineP());
        map.put(new Card.Flex(), new Card.FlexP());
        map.put(new Card.Havoc(), new Card.HavocP());
        map.put(new Card.Headbutt(), new Card.HeadbuttP());
        map.put(new Card.HeavyBlade(), new Card.HeavyBladeP());
        map.put(new Card.IronWave(), new Card.IronWaveP());
        map.put(new Card.PerfectedStrike(), new Card.PerfectedStrikeP());
        map.put(new Card.PommelStrike(), new Card.PommelStrikeP());
        map.put(new Card.ShrugItOff(), new Card.ShrugItOffP());
        map.put(new Card.SwordBoomerang(), new Card.SwordBoomerangP());
        map.put(new Card.Thunderclap(), new Card.ThunderclapP());
        map.put(new Card.TrueGrit(), new Card.TrueGritP());
        map.put(new Card.TwinStrike(), new Card.TwinStrikeP());
        map.put(new Card.Warcry(), new Card.WarcryP());
        map.put(new Card.WildStrike(), new Card.WildStrikeP());
        map.put(new Card.BattleTrance(), new Card.BattleTranceP());
        map.put(new Card.BloodForBlood(), new Card.BloodForBloodP());
        map.put(new Card.Bloodletting(), new Card.BloodlettingP());
        map.put(new Card.BurningPact(), new Card.BurningPactP());
        map.put(new Card.Carnage(), new Card.CarnageP());
        map.put(new Card.Combust(), new Card.CombustP());
        map.put(new Card.DarkEmbrace(), new Card.DarkEmbraceP());
        map.put(new Card.Disarm(), new Card.DisarmP());
        map.put(new Card.Dropkick(), new Card.DropkickP());
        map.put(new Card.DualWield(), new Card.DualWieldP());
        map.put(new Card.Entrench(), new Card.EntrenchP());
        map.put(new Card.Evolve(), new Card.EvolveP());
        map.put(new Card.FeelNoPain(), new Card.FeelNoPainP());
        map.put(new Card.FireBreathing(), new Card.FireBreathingP());
        map.put(new Card.FlameBarrier(), new Card.FlameBarrierP());
        map.put(new Card.GhostlyArmor(), new Card.GhostlyArmorP());
        map.put(new Card.Hemokinesis(), new Card.HemokinesisP());
        map.put(new Card.InfernalBlade(), new Card.InfernalBladeP());
        map.put(new Card.Inflame(), new Card.InflameP());
        map.put(new Card.Intimidate(), new Card.IntimidateP());
        map.put(new Card.Metallicize(), new Card.MetallicizeP());
        map.put(new Card.PowerThrough(), new Card.PowerThroughP());
        map.put(new Card.Pummel(), new Card.PummelP());
        map.put(new Card.Rage(), new Card.RageP());
        map.put(new Card.Rampage(), new Card.RampageP());
        map.put(new Card.RecklessCharge(), new Card.RecklessChargeP());
        map.put(new Card.Rupture(), new Card.RuptureP());
        map.put(new Card.SecondWind(), new Card.SecondWindP());
        map.put(new Card.SeeingRed(), new Card.SeeingRedP());
        map.put(new Card.Sentinel(), new Card.SentinelP());
        map.put(new Card.SeverSoul(), new Card.SeverSoulP());
        map.put(new Card.Shockwave(), new Card.ShockwaveP());
        map.put(new Card.SpotWeakness(), new Card.SpotWeaknessP());
        map.put(new Card.Uppercut(), new Card.UppercutP());
        map.put(new Card.Whirlwind(), new Card.WhirlwindP());
        map.put(new Card.Barricade(), new Card.BarricadeP());
        map.put(new Card.Berserk(), new Card.BerserkP());
        map.put(new Card.Bludgeon(), new Card.BludgeonP());
        map.put(new Card.Brutality(), new Card.BrutalityP());
        map.put(new Card.Corruption(), new Card.CorruptionP());
        map.put(new Card.DemonForm(), new Card.DemonFormP());
        map.put(new Card.DoubleTap(), new Card.DoubleTapP());
        map.put(new Card.Exhume(), new Card.ExhumeP());
        map.put(new Card.FiendFire(), new Card.FiendFireP());
        map.put(new Card.Immolate(), new Card.ImmolateP());
        map.put(new Card.Impervious(), new Card.ImperviousP());
        map.put(new Card.Juggernaut(), new Card.JuggernautP());
        map.put(new Card.LimitBreak(), new Card.LimitBreakP());
        map.put(new Card.Offering(), new Card.OfferingP());
        map.put(new Card.Reaper(), new Card.ReaperP());

        map.put(new CardColorless.DarkShackles(), new CardColorless.DarkShacklesP());
        map.put(new CardColorless.DramaticEntrance(), new CardColorless.DramaticEntranceP());
        map.put(new CardColorless.Apotheosis(), new CardColorless.ApotheosisP());
        map.put(new CardColorless.SecretTechnique(), new CardColorless.SecretTechniqueP());
        map.put(new CardColorless.SecretWeapon(), new CardColorless.SecretWeaponP());

        map.put(new CardSilent.Survivor(), new CardSilent.SurvivorP());
        map.put(new CardSilent.Neutralize(), new CardSilent.NeutralizeP());
        map.put(new CardSilent.DaggerSpray(), new CardSilent.DaggerSprayP());

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
        map.put(new CardDefect.Equilibirum(), new CardDefect.EquilibirumP());
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
        map.put(new CardDefect.MachineLearningP(), new CardDefect.MachineLearningP());
        map.put(new CardDefect.MeteorStrike(), new CardDefect.MeteorStrikeP());
        map.put(new CardDefect.MultiCast(), new CardDefect.MultiCastP());
        map.put(new CardDefect.Rainbow(), new CardDefect.RainbowP());
        map.put(new CardDefect.Reboot(), new CardDefect.RebootP());
        map.put(new CardDefect.Seek(), new CardDefect.SeekP());
        map.put(new CardDefect.ThunderStrike(), new CardDefect.ThunderStrikeP());

        map = Collections.unmodifiableMap(map);
    }
}
