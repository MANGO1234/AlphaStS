package com.alphaStS;

import com.alphaStS.card.*;
import com.alphaStS.enums.CharacterEnum;

import java.util.ArrayList;
import java.util.List;

public class CardManager {

    public static List<Card> getPossibleGeneratedCards(CharacterEnum character, int cardType, boolean generateHealingCard) {
        List<Card> cards = new ArrayList<>();
        switch (character) {
            case IRONCLAD:
                cards.addAll(getIroncladCards(cardType, generateHealingCard));
                break;
            case SILENT:
                cards.addAll(getSilentCards(cardType, generateHealingCard));
                break;
            case DEFECT:
                cards.addAll(getDefectCards(cardType, generateHealingCard));
                break;
            case WATCHER:
                cards.addAll(getWatcherCards(cardType, generateHealingCard));
                break;
        }
        return cards;
    }

    public static List<Card> getPossibleSelect1OutOf3Cards(CharacterEnum character, int cardType, boolean generateHealingCard) {
        List<Card> baseCards = getPossibleGeneratedCards(character, cardType, generateHealingCard);
        List<Card> cards = new ArrayList<>();
        for (Card card : baseCards) {
            cards.add(card.getTemporaryCostIfPossible(0));
        }
        return cards;
    }

    public static List<Card> getCharacterCards(CharacterEnum character, boolean generateHealingCard) {
        List<Card> cards = new ArrayList<>();
        cards.addAll(getPossibleGeneratedCards(character, Card.ATTACK, generateHealingCard));
        cards.addAll(getPossibleGeneratedCards(character, Card.SKILL, generateHealingCard));
        cards.addAll(getPossibleGeneratedCards(character, Card.POWER, generateHealingCard));
        return cards;
    }

    public static List<Card> getCharacterCardsSelect1OutOf3(CharacterEnum character, boolean generateHealingCard) {
        List<Card> baseCards = getCharacterCards(character, generateHealingCard);
        List<Card> cards = new ArrayList<>();
        for (Card card : baseCards) {
            cards.add(card.getTemporaryCostIfPossible(0));
        }
        return cards;
    }

    public static List<Card> getAllAttackCards(boolean generateHealingCard) {
        List<Card> cards = new ArrayList<>();
        cards.addAll(getPossibleGeneratedCards(CharacterEnum.IRONCLAD, Card.ATTACK, generateHealingCard));
        cards.addAll(getPossibleGeneratedCards(CharacterEnum.SILENT, Card.ATTACK, generateHealingCard));
        cards.addAll(getPossibleGeneratedCards(CharacterEnum.DEFECT, Card.ATTACK, generateHealingCard));
        cards.addAll(getPossibleGeneratedCards(CharacterEnum.WATCHER, Card.ATTACK, generateHealingCard));
        return cards;
    }

    public static List<Card> getAllAttackCardsSelect1OutOf3(boolean generateHealingCard) {
        List<Card> baseCards = getAllAttackCards(generateHealingCard);
        List<Card> cards = new ArrayList<>();
        for (Card card : baseCards) {
            cards.add(card.getTemporaryCostIfPossible(0));
        }
        return cards;
    }

    public static List<Card> getColorlessCards(boolean generateHealingCard) {
        List<Card> cards = new ArrayList<>();
        if (generateHealingCard) {
            cards.add(new CardColorless.BandageUp().getTemporaryCostIfPossible(0));
        }
        cards.add(new CardColorless.Blind().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.DarkShackles().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.DeepBreath().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.Discovery(false).getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.DramaticEntrance().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.Enlightenment().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.Finesse().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.FlashOfSteel().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.Forethought().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.GoodInstincts().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.Impatience().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.JackOfAllTrades().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.Madness().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.MindBlast().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.Panacea().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.PanicButton().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.Purity().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.SwiftStrike().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.Trip().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.Apotheosis().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.Chrysalis().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.HandOfGreed(0).getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.Magnetism().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.MasterOfStrategy().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.Mayhem().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.Metamorphosis().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.Panache().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.SadisticNature().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.SecretTechnique().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.SecretWeapon().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.TheBomb().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.ThinkingAhead().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.Transmutation().getTemporaryCostIfPossible(0));
        cards.add(new CardColorless.Violence().getTemporaryCostIfPossible(0));
        return cards;
    }

    private static List<Card> getIroncladCards(int cardType, boolean generateHealingCard) {
        List<Card> cards = new ArrayList<>();

        if (cardType == Card.ATTACK) {
            // Common attacks
            cards.add(new CardIronclad.Anger());
            cards.add(new CardIronclad.BodySlam());
            cards.add(new CardIronclad.Clash());
            cards.add(new CardIronclad.Cleave());
            cards.add(new CardIronclad.Clothesline());
            cards.add(new CardIronclad.Headbutt());
            cards.add(new CardIronclad.HeavyBlade());
            cards.add(new CardIronclad.IronWave());
            cards.add(new CardIronclad.PerfectedStrike());
            cards.add(new CardIronclad.PommelStrike());
            cards.add(new CardIronclad.SwordBoomerang());
            cards.add(new CardIronclad.Thunderclap());
            cards.add(new CardIronclad.TwinStrike());
            cards.add(new CardIronclad.WildStrike());

            // Uncommon attacks
            cards.add(new CardIronclad.BloodForBlood());
            cards.add(new CardIronclad.Carnage());
            cards.add(new CardIronclad.Dropkick());
            cards.add(new CardIronclad.Hemokinesis());
            cards.add(new CardIronclad.Pummel());
            cards.add(new CardIronclad.Rampage());
            cards.add(new CardIronclad.RecklessCharge());
            cards.add(new CardIronclad.SearingBlow(0));
            cards.add(new CardIronclad.SeverSoul());
            cards.add(new CardIronclad.Uppercut());
            cards.add(new CardIronclad.Whirlwind());

            // Rare attacks
            cards.add(new CardIronclad.Bludgeon());
            if (generateHealingCard) {
                cards.add(new CardIronclad.Feed());
            }
            cards.add(new CardIronclad.FiendFire());
            cards.add(new CardIronclad.Immolate());
            if (generateHealingCard) {
                cards.add(new CardIronclad.Reaper());
            }
        } else if (cardType == Card.SKILL) {
            // Common skills
            cards.add(new CardIronclad.Armanent());
            cards.add(new CardIronclad.Flex());
            cards.add(new CardIronclad.Havoc());
            cards.add(new CardIronclad.ShrugItOff());
            cards.add(new CardIronclad.TrueGrit());
            cards.add(new CardIronclad.Warcry());

            // Uncommon skills
            cards.add(new CardIronclad.BattleTrance());
            cards.add(new CardIronclad.Bloodletting());
            cards.add(new CardIronclad.BurningPact());
            cards.add(new CardIronclad.Disarm());
            cards.add(new CardIronclad.DualWield());
            cards.add(new CardIronclad.Entrench());
            cards.add(new CardIronclad.FlameBarrier());
            cards.add(new CardIronclad.GhostlyArmor());
            cards.add(new CardIronclad.InfernalBlade());
            cards.add(new CardIronclad.Intimidate());
            cards.add(new CardIronclad.PowerThrough());
            cards.add(new CardIronclad.Rage());
            cards.add(new CardIronclad.SecondWind());
            cards.add(new CardIronclad.SeeingRed());
            cards.add(new CardIronclad.Sentinel());
            cards.add(new CardIronclad.Shockwave());
            cards.add(new CardIronclad.SpotWeakness());

            // Rare skills
            cards.add(new CardIronclad.DoubleTap());
            cards.add(new CardIronclad.Exhume());
            cards.add(new CardIronclad.Impervious());
            cards.add(new CardIronclad.LimitBreak());
            cards.add(new CardIronclad.Offering());
        } else if (cardType == Card.POWER) {
            // Uncommon powers
            cards.add(new CardIronclad.Combust());
            cards.add(new CardIronclad.DarkEmbrace());
            cards.add(new CardIronclad.Evolve());
            cards.add(new CardIronclad.FeelNoPain());
            cards.add(new CardIronclad.FireBreathing());
            cards.add(new CardIronclad.Inflame());
            cards.add(new CardIronclad.Metallicize());
            cards.add(new CardIronclad.Rupture());

            // Rare powers
            cards.add(new CardIronclad.Barricade());
            cards.add(new CardIronclad.Berserk());
            cards.add(new CardIronclad.Brutality());
            cards.add(new CardIronclad.Corruption());
            cards.add(new CardIronclad.DemonForm());
            cards.add(new CardIronclad.Juggernaut());
        }

        return cards;
    }

    private static List<Card> getSilentCards(int cardType, boolean generateHealingCard) {
        List<Card> cards = new ArrayList<>();

        if (cardType == Card.ATTACK) {
            // Common attacks
            cards.add(new CardSilent.Bane());
            cards.add(new CardSilent.DaggerSpray());
            cards.add(new CardSilent.DaggerThrow());
            cards.add(new CardSilent.FlyingKnee());
            cards.add(new CardSilent.PoisonedStab());
            cards.add(new CardSilent.QuickSlash());
            cards.add(new CardSilent.Slice());
            cards.add(new CardSilent.SneakyStrike());
            cards.add(new CardSilent.SuckerPunch());

            // Uncommon attacks
            cards.add(new CardSilent.AllOutAttack());
            cards.add(new CardSilent.Backstab());
            cards.add(new CardSilent.Choke());
            cards.add(new CardSilent.Dash());
            cards.add(new CardSilent.EndlessAgony());
            cards.add(new CardSilent.Eviscerate());
            cards.add(new CardSilent.Finisher());
            cards.add(new CardSilent.Flechette());
            cards.add(new CardSilent.HeelHook());
            cards.add(new CardSilent.MasterfulStab(6));
            cards.add(new CardSilent.Predator());
            cards.add(new CardSilent.RiddleWithHoles());
            cards.add(new CardSilent.Skewer());

            // Rare attacks
            cards.add(new CardSilent.DieDieDie());
            cards.add(new CardSilent.GlassKnife());
            cards.add(new CardSilent.GrandFinale());
            cards.add(new CardSilent.Unload());
        } else if (cardType == Card.SKILL) {
            // Common skills
            cards.add(new CardSilent.Acrobatics());
            cards.add(new CardSilent.Backflip());
            cards.add(new CardSilent.BladeDance());
            cards.add(new CardSilent.CloakAndDagger());
            cards.add(new CardSilent.DeadlyPoison());
            cards.add(new CardSilent.Deflect());
            cards.add(new CardSilent.DodgeAndRoll());
            cards.add(new CardSilent.Outmaneuver());
            cards.add(new CardSilent.PiercingWail());
            cards.add(new CardSilent.Prepared());

            // Uncommon skills
            cards.add(new CardSilent.Blur());
            cards.add(new CardSilent.BouncingFlask());
            cards.add(new CardSilent.CalculatedGamble());
            cards.add(new CardSilent.Catalyst());
            cards.add(new CardSilent.Concentrate());
            cards.add(new CardSilent.CripplingCloud());
            cards.add(new CardSilent.Distraction());
            cards.add(new CardSilent.EscapePlan());
            cards.add(new CardSilent.Expertise());
            cards.add(new CardSilent.LegSweep());
            cards.add(new CardSilent.Reflex());
            cards.add(new CardSilent.Setup(true));
            cards.add(new CardSilent.Tactician());
            cards.add(new CardSilent.Terror());

            // Rare skills
            cards.add(new CardSilent.Adrenaline());
            cards.add(new CardSilent.Alchemize(0, 0, 0));
            cards.add(new CardSilent.BulletTime());
            cards.add(new CardSilent.Burst());
            cards.add(new CardSilent.CorpseExplosion());
            cards.add(new CardSilent.Doppelganger());
            cards.add(new CardSilent.Malaise());
            cards.add(new CardSilent.Nightmare());
            cards.add(new CardSilent.PhantasmalKiller());
            cards.add(new CardSilent.StormOfSteel());
        } else if (cardType == Card.POWER) {
            // Uncommon powers
            cards.add(new CardSilent.Accuracy());
            cards.add(new CardSilent.Caltrops());
            cards.add(new CardSilent.Footwork());
            cards.add(new CardSilent.InfiniteBlade());
            cards.add(new CardSilent.NoxiousFume());
            cards.add(new CardSilent.WellLaidPlans());

            // Rare powers
            cards.add(new CardSilent.AThousandCuts());
            cards.add(new CardSilent.AfterImage());
            cards.add(new CardSilent.Envenom());
            cards.add(new CardSilent.ToolsOfTheTrade());
            cards.add(new CardSilent.WraithForm());
        }

        return cards;
    }

    private static List<Card> getDefectCards(int cardType, boolean generateHealingCard) {
        List<Card> cards = new ArrayList<>();

        if (cardType == Card.ATTACK) {
            // Common attacks
            cards.add(new CardDefect.BallLightning());
            cards.add(new CardDefect.Barrage());
            cards.add(new CardDefect.BeamCell());
            cards.add(new CardDefect.Claw());
            cards.add(new CardDefect.ColdSnap());
            cards.add(new CardDefect.CompiledDriver());
            cards.add(new CardDefect.GoForTheEye());
            cards.add(new CardDefect.Rebound());
            cards.add(new CardDefect.Streamline());
            cards.add(new CardDefect.SweepingBeam());

            // Uncommon attacks
            cards.add(new CardDefect.Blizzard());
            cards.add(new CardDefect.BullsEye());
            cards.add(new CardDefect.DoomAndGloom());
            cards.add(new CardDefect.FTL());
            cards.add(new CardDefect.Melter());
            cards.add(new CardDefect.RipAndTear());
            cards.add(new CardDefect.Scrape());
            cards.add(new CardDefect.Sunder());

            // Rare attacks
            cards.add(new CardDefect.AllForOne(0, 0));
            cards.add(new CardDefect.CoreSurge());
            cards.add(new CardDefect.HyperBeam());
            cards.add(new CardDefect.MeteorStrike());
            cards.add(new CardDefect.ThunderStrike());
        } else if (cardType == Card.SKILL) {
            // Common skills
            cards.add(new CardDefect.ChargeBattery());
            cards.add(new CardDefect.Coolheaded());
            cards.add(new CardDefect.Hologram());
            cards.add(new CardDefect.Leap());
            cards.add(new CardDefect.Recursion());
            cards.add(new CardDefect.Stack());
            cards.add(new CardDefect.SteamBarrier());
            cards.add(new CardDefect.Turbo());

            // Uncommon skills
            cards.add(new CardDefect.Aggregate());
            cards.add(new CardDefect.AutoShields());
            cards.add(new CardDefect.BootSequence());
            cards.add(new CardDefect.Chaos());
            cards.add(new CardDefect.Chill());
            cards.add(new CardDefect.Consume());
            cards.add(new CardDefect.Darkness());
            cards.add(new CardDefect.DoubleEnergy());
            cards.add(new CardDefect.Equilibrium());
            cards.add(new CardDefect.ForceField());
            cards.add(new CardDefect.Fusion());
            cards.add(new CardDefect.GeneticAlgorithm(1, 0));
            cards.add(new CardDefect.Glacier());
            cards.add(new CardDefect.Overclock());
            cards.add(new CardDefect.Recycle());
            cards.add(new CardDefect.ReinforcedBody());
            cards.add(new CardDefect.Reprogram());
            cards.add(new CardDefect.Skim());
            cards.add(new CardDefect.Tempest());
            cards.add(new CardDefect.WhiteNoise());

            // Rare skills
            cards.add(new CardDefect.Amplify());
            cards.add(new CardDefect.Fission());
            cards.add(new CardDefect.MultiCast());
            cards.add(new CardDefect.Rainbow());
            cards.add(new CardDefect.Reboot());
            cards.add(new CardDefect.Seek());
        } else if (cardType == Card.POWER) {
            // Uncommon powers
            cards.add(new CardDefect.Capacitor());
            cards.add(new CardDefect.Defragment());
            cards.add(new CardDefect.Heatsinks());
            cards.add(new CardDefect.HelloWorld());
            cards.add(new CardDefect.Loop());
            if (generateHealingCard) {
                cards.add(new CardDefect.SelfRepair());
            }
            cards.add(new CardDefect.StaticDischarge());
            cards.add(new CardDefect.Storm());

            // Rare powers
            cards.add(new CardDefect.BiasedCognition());
            cards.add(new CardDefect.Buffer());
            cards.add(new CardDefect.CreativeAI());
            cards.add(new CardDefect.EchoForm());
            cards.add(new CardDefect.Electrodynamics());
            cards.add(new CardDefect.MachineLearning());
        }

        return cards;
    }

    private static List<Card> getWatcherCards(int cardType, boolean generateHealingCard) {
        List<Card> cards = new ArrayList<>();

        if (cardType == Card.ATTACK) {
            // Common attacks
            cards.add(new CardWatcher.BowlingBash());
            cards.add(new CardWatcher.Consecrate());
            cards.add(new CardWatcher.CrushJoints());
            cards.add(new CardWatcher.CutThroughFate());
            cards.add(new CardWatcher.EmptyFist());
            cards.add(new CardWatcher.FlurryOfBlows());
            cards.add(new CardWatcher.FlyingSleeves());
            cards.add(new CardWatcher.FollowUp());
            cards.add(new CardWatcher.JustLucky());
            cards.add(new CardWatcher.SashWhip());

            // Uncommon attacks
            cards.add(new CardWatcher.CarveReality());
            cards.add(new CardWatcher.Conclude());
            cards.add(new CardWatcher.FearNoEvil());
            cards.add(new CardWatcher.ReachHeaven());
            cards.add(new CardWatcher.SandsOfTime());
            cards.add(new CardWatcher.SignatureMove());
            cards.add(new CardWatcher.TalkToTheHand());
            cards.add(new CardWatcher.Tantrum());
            cards.add(new CardWatcher.Wallop());
            cards.add(new CardWatcher.Weave());
            cards.add(new CardWatcher.WheelKick());
            cards.add(new CardWatcher.WindmillStrike());

            // Rare attacks
            cards.add(new CardWatcher.Brilliance());
            cards.add(new CardWatcher.LessonLearned(0.0));
            cards.add(new CardWatcher.Ragnarok());
        } else if (cardType == Card.SKILL) {
            // Common skills
            cards.add(new CardWatcher.Crescendo());
            cards.add(new CardWatcher.EmptyBody());
            cards.add(new CardWatcher.Evaluate());
            cards.add(new CardWatcher.Halt());
            cards.add(new CardWatcher.PressurePoints());
            cards.add(new CardWatcher.Prostrate());
            cards.add(new CardWatcher.Protect());
            cards.add(new CardWatcher.ThirdEye());
            cards.add(new CardWatcher.Tranquility());

            // Uncommon skills
            cards.add(new CardWatcher.Collect());
            cards.add(new CardWatcher.DeceiveReality());
            cards.add(new CardWatcher.EmptyMind());
            cards.add(new CardWatcher.ForeignInfluence());
            cards.add(new CardWatcher.Indignation());
            cards.add(new CardWatcher.InnerPeace());
            cards.add(new CardWatcher.Meditate());
            cards.add(new CardWatcher.Perseverance());
            cards.add(new CardWatcher.Pray());
            cards.add(new CardWatcher.Sanctity());
            cards.add(new CardWatcher.SimmeringFury());
            cards.add(new CardWatcher.Swivel());
            cards.add(new CardWatcher.WaveOfTheHand());
            cards.add(new CardWatcher.Worship());
            cards.add(new CardWatcher.WreathOfFlame());

            // Rare skills
            cards.add(new CardWatcher.Alpha());
            cards.add(new CardWatcher.Blasphemy());
            cards.add(new CardWatcher.ConjureBlade(0));
            cards.add(new CardWatcher.DeusExMachina());
            cards.add(new CardWatcher.Judgment());
            cards.add(new CardWatcher.Omniscience());
            cards.add(new CardWatcher.Scrawl());
            cards.add(new CardWatcher.SpiritShield());
            cards.add(new CardWatcher.Vault());
            cards.add(new CardWatcher.Wish(0.0));
        } else if (cardType == Card.POWER) {
            // Uncommon powers
            cards.add(new CardWatcher.BattleHymn());
            cards.add(new CardWatcher.Fasting());
            cards.add(new CardWatcher.Foresight());
            cards.add(new CardWatcher.LikeWater());
            cards.add(new CardWatcher.MentalFortress());
            cards.add(new CardWatcher.Nirvana());
            cards.add(new CardWatcher.Rushdown());
            cards.add(new CardWatcher.Study());

            // Rare powers
            cards.add(new CardWatcher.DevaForm());
            cards.add(new CardWatcher.Devotion());
            cards.add(new CardWatcher.Establishment());
            cards.add(new CardWatcher.MasterReality());
        }

        return cards;
    }
}