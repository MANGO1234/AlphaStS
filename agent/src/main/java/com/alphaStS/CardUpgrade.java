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
        map.put(new Card.Headbutt(), new Card.HeadbuttP());
        map.put(new Card.HeavyBlade(), new Card.HeavyBladeP());
        map.put(new Card.IronWave(), new Card.IronWaveP());
        map.put(new Card.PerfectedStrike(), new Card.PerfectedStrikeP());
        map.put(new Card.PommelStrike(), new Card.PommelStrikeP());
        map.put(new Card.ShrugItOff(), new Card.ShrugItOffP());
        map.put(new Card.SwordBoomerang(), new Card.SwordBoomerangP());
        map.put(new Card.Thunderclap(), new Card.ThunderclapP());
        map.put(new Card.TwinStrike(), new Card.TwinStrikeP());
        map.put(new Card.WildStrike(), new Card.WildStrikeP());
        map.put(new Card.Bloodletting(), new Card.BloodlettingP());
        map.put(new Card.Carnage(), new Card.CarnageP());
        map.put(new Card.Disarm(), new Card.DisarmP());
        map.put(new Card.Dropkick(), new Card.DropkickP());
        map.put(new Card.Entrench(), new Card.EntrenchP());
        map.put(new Card.GhostlyArmor(), new Card.GhostlyArmorP());
        map.put(new Card.Hemokinesis(), new Card.HemokinesisP());
        map.put(new Card.Inflame(), new Card.InflameP());
        map.put(new Card.Intimidate(), new Card.IntimidateP());
        map.put(new Card.PowerThrough(), new Card.PowerThroughP());
        map.put(new Card.Pummel(), new Card.PummelP());
        map.put(new Card.RecklessCharge(), new Card.RecklessChargeP());
        map.put(new Card.SecondWind(), new Card.SecondWindP());
        map.put(new Card.SeeingRed(), new Card.SeeingRedP());
        map.put(new Card.SeverSoul(), new Card.SeverSoulP());
        map.put(new Card.Shockwave(), new Card.ShockwaveP());
        map.put(new Card.SpotWeakness(), new Card.SpotWeaknessP());
        map.put(new Card.Uppercut(), new Card.UppercutP());
        map.put(new Card.Whirlwind(), new Card.WhirlwindP());
        map.put(new Card.Bludgeon(), new Card.BludgeonP());
        map.put(new Card.FiendFire(), new Card.FiendFireP());
        map.put(new Card.Immolate(), new Card.ImmolateP());
        map.put(new Card.Impervious(), new Card.ImperviousP());
        map.put(new Card.LimitBreak(), new Card.LimitBreakP());
        map.put(new Card.Offering(), new Card.OfferingP());
        map.put(new Card.Reaper(), new Card.ReaperP());
        map = Collections.unmodifiableMap(map);
    }
}
