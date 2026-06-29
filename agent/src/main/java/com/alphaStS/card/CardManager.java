package com.alphaStS.card;

import com.alphaStS.enums.CharacterEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CardManager {

    public static boolean isUpgraded(Card card) {
        return card.cardName.contains("+");
    }

    public static boolean isStrike(Card card) {
        return card.cardName.contains("Strike");
    }

    public static List<Card> getCharacterCardsByType(CharacterEnum character, int cardType, boolean generateHealingCard) {
        return switch (character) {
            case IRONCLAD -> getIroncladCards(cardType, generateHealingCard);
            case SILENT -> getSilentCards(cardType, generateHealingCard);
            case DEFECT -> getDefectCards(cardType, generateHealingCard);
            case WATCHER -> getWatcherCards(cardType, generateHealingCard);
            case IRONCLAD2 -> getIronclad2Cards(cardType, generateHealingCard);
            case SILENT2 -> getSilent2Cards(cardType, generateHealingCard);
            case DEFECT2 -> getDefect2Cards(cardType, generateHealingCard);
            case REGENT -> getRegentCards(cardType, generateHealingCard);
            case NECROBINDER -> getNecrobinderCards(cardType, generateHealingCard);
        };
    }

    public static List<Card> getCharacterCardsByTypeTmp0Cost(CharacterEnum character, int cardType, boolean generateHealingCard) {
        List<Card> baseCards = getCharacterCardsByType(character, cardType, generateHealingCard);
        List<Card> cards = new ArrayList<>();
        for (Card card : baseCards) {
            cards.add(card.getTemporaryCostIfPossible(0));
        }
        return cards;
    }

    public static List<Card> getCharacterCards(CharacterEnum character, boolean generateHealingCard) {
        List<Card> cards = new ArrayList<>();
        cards.addAll(getCharacterCardsByType(character, Card.ATTACK, generateHealingCard));
        cards.addAll(getCharacterCardsByType(character, Card.SKILL, generateHealingCard));
        cards.addAll(getCharacterCardsByType(character, Card.POWER, generateHealingCard));
        return cards;
    }

    public static List<Card> getCharacterCardsTmp0Cost(CharacterEnum character, boolean generateHealingCard) {
        List<Card> baseCards = getCharacterCards(character, generateHealingCard);
        List<Card> cards = new ArrayList<>();
        for (Card card : baseCards) {
            cards.add(card.getTemporaryCostIfPossible(0));
        }
        return cards;
    }

    public static List<Card> getAllAttackCards(boolean generateHealingCard) {
        List<Card> cards = new ArrayList<>();
        cards.addAll(getCharacterCardsByType(CharacterEnum.IRONCLAD, Card.ATTACK, generateHealingCard));
        cards.addAll(getCharacterCardsByType(CharacterEnum.SILENT, Card.ATTACK, generateHealingCard));
        cards.addAll(getCharacterCardsByType(CharacterEnum.DEFECT, Card.ATTACK, generateHealingCard));
        cards.addAll(getCharacterCardsByType(CharacterEnum.WATCHER, Card.ATTACK, generateHealingCard));
        return cards;
    }

    public static List<Card> getAllAttackCardsTmp0Cost(boolean generateHealingCard) {
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
            cards.add(new CardColorless.BandageUp());
        }
        cards.add(new CardColorless.Blind());
        cards.add(new CardColorless.DarkShackles());
        cards.add(new CardColorless.DeepBreath());
        cards.add(new CardColorless.Discovery());
        cards.add(new CardColorless.DramaticEntrance());
        cards.add(new CardColorless.Enlightenment());
        cards.add(new CardColorless.Finesse());
        cards.add(new CardColorless.FlashOfSteel());
        cards.add(new CardColorless.Forethought());
        cards.add(new CardColorless.GoodInstincts());
        cards.add(new CardColorless.Impatience());
        cards.add(new CardColorless.JackOfAllTrades());
        cards.add(new CardColorless.Madness());
        cards.add(new CardColorless.MindBlast());
        cards.add(new CardColorless.Panacea());
        cards.add(new CardColorless.PanicButton());
        cards.add(new CardColorless.Purity());
        cards.add(new CardColorless.SwiftStrike());
        cards.add(new CardColorless.Trip());
        cards.add(new CardColorless.Apotheosis());
        cards.add(new CardColorless.Chrysalis());
        cards.add(new CardColorless.HandOfGreed(2));
        cards.add(new CardColorless.Magnetism());
        cards.add(new CardColorless.MasterOfStrategy());
        cards.add(new CardColorless.Mayhem());
        cards.add(new CardColorless.Metamorphosis());
        cards.add(new CardColorless.Panache());
        cards.add(new CardColorless.SadisticNature());
        cards.add(new CardColorless.SecretTechnique());
        cards.add(new CardColorless.SecretWeapon());
        cards.add(new CardColorless.TheBomb());
        cards.add(new CardColorless.ThinkingAhead());
        cards.add(new CardColorless.Transmutation());
        cards.add(new CardColorless.Violence());
        return cards;
    }

    public static List<Card> getCurseCards() {
        List<Card> cards = new ArrayList<>();
        cards.add(new CardOther2.BadLuck());
        cards.add(new CardOther2.Clumsy());
        cards.add(new CardOther2.Decay());
        cards.add(new CardOther2.Doubt());
        cards.add(new CardOther2.Folly());
        cards.add(new CardOther2.Greed());
        cards.add(new CardOther2.Injury());
        cards.add(new CardOther2.Normality());
        cards.add(new CardOther2.PoorSleep());
        cards.add(new CardOther2.Regret());
        cards.add(new CardOther2.Shame());
        cards.add(new CardOther2.SporeMind());
        cards.add(new CardOther2.Writhe());
        return cards;
    }

    public static List<Card> getColorlessCardsTmp0Cost(boolean generateHealingCard) {
        List<Card> baseCards = getColorlessCards(generateHealingCard);
        List<Card> cards = new ArrayList<>();
        for (Card card : baseCards) {
            cards.add(card.getTemporaryCostIfPossible(0));
        }
        return cards;
    }

    public enum ColorlessCard2Pool {
        CARD_GENERATION,
        ALL,
    }

    public static List<Card> getColorless2Cards(ColorlessCard2Pool pool) {
        List<Card> cards = new ArrayList<>();

        // Card generation pool
        // cards.add(new CardColorless2.Bolas()); // TODO
        cards.add(new CardColorless2.GoldAxe());
        cards.add(new CardColorless2.HandOfGreed(0.0));
        // cards.add(new CardColorless2.Jackpot()); // TODO
        // cards.add(new CardColorless2.Rend()); // TODO
        cards.add(new CardColorless2.Salvo());
        // cards.add(new CardColorless2.Calamity()); // TODO
        // cards.add(new CardColorless2.Entropy()); // TODO
        cards.add(new CardColorless2.EternalArmor());
        cards.add(new CardColorless2.Mayhem());
        cards.add(new CardColorless2.Nostalgia());
        cards.add(new CardColorless2.RollingBoulder());
        cards.add(new CardColorless2.Alchemize(0, 0, 0));
        cards.add(new CardColorless2.Anointed());
        cards.add(new CardColorless2.BeatDown());
        // cards.add(new CardColorless2.HiddenGem()); // TODO
        cards.add(new CardColorless2.MasterOfStrategy());
        cards.add(new CardColorless2.Scrawl());
        cards.add(new CardColorless2.SecretTechnique());
        cards.add(new CardColorless2.SecretWeapon());
        cards.add(new CardColorless2.TheGambit());
        cards.add(new CardColorless2.DramaticEntrance());
        cards.add(new CardColorless2.Fisticuffs());
        cards.add(new CardColorless2.FlashOfSteel());
        cards.add(new CardColorless2.MindBlast());
        cards.add(new CardColorless2.Omnislice());
        // cards.add(new CardColorless2.SeekerStrike()); // TODO
        // cards.add(new CardColorless2.ThrummingHatchet()); // TODO
        cards.add(new CardColorless2.UltimateStrike());
        cards.add(new CardColorless2.Volley());
        cards.add(new CardColorless2.Automation());
        cards.add(new CardColorless2.Fasten());
        cards.add(new CardColorless2.Panache());
        cards.add(new CardColorless2.PrepTime());
        cards.add(new CardColorless2.Prowess());
        // cards.add(new CardColorless2.Stratagem()); // TODO
        cards.add(new CardColorless2.Catastrophe());
        cards.add(new CardColorless2.DarkShackles());
        cards.add(new CardColorless2.Discovery());
        cards.add(new CardColorless2.Equilibrium());
        cards.add(new CardColorless2.Finesse());
        cards.add(new CardColorless2.Impatience());
        cards.add(new CardColorless2.JackOfAllTrades());
        cards.add(new CardColorless2.PanicButton());
        cards.add(new CardColorless2.Production());
        cards.add(new CardColorless2.Prolong());
        cards.add(new CardColorless2.Purity());
        cards.add(new CardColorless2.Restlessness());
        cards.add(new CardColorless2.Shockwave());
        // cards.add(new CardColorless2.Splash()); // TODO
        cards.add(new CardColorless2.TheBomb());
        cards.add(new CardColorless2.ThinkingAhead());
        cards.add(new CardColorless2.UltimateDefend());

        if (pool == ColorlessCard2Pool.ALL) {
            // cards.add(new CardColorless2.Maul()); // TODO
            cards.add(new CardColorless2.NeowsFury());
            // cards.add(new CardColorless2.Whistle()); // TODO
            cards.add(new CardColorless2.Apotheosis());
            cards.add(new CardColorless2.Apparition());
            cards.add(new CardColorless2.BrightestFlame());
            cards.add(new CardColorless2.Relax());
            cards.add(new CardColorless2.Wish());
            cards.add(new CardColorless2.ByrdSwoop());
            cards.add(new CardColorless2.Exterminate());
            cards.add(new CardColorless2.Peck());
            cards.add(new CardColorless2.Squash());
            cards.add(new CardColorless2.Enlightenment());
            cards.add(new CardColorless2.FeedingFrenzy());
            // cards.add(new CardColorless2.MadScience()); // TODO
            cards.add(new CardColorless2.Metamorphosis());
            cards.add(new CardColorless2.ToricToughness());
            cards.add(new CardColorless2.GiantRock());
            cards.add(new CardColorless2.MinionDiveBomb());
            cards.add(new CardColorless2.MinionStrike());
            cards.add(new CardColorless2.Shiv());
            cards.add(new CardColorless2.SovereignBlade());
            cards.add(new CardColorless2.SweepingGaze());
            cards.add(new CardColorless2.Fuel());
            cards.add(new CardColorless2.Luminesce());
            cards.add(new CardColorless2.MinionSacrifice());
            cards.add(new CardColorless2.Soul());
            cards.add(new CardOther2.AscendersBane());
            cards.add(new CardOther2.BadLuck());
            cards.add(new CardOther2.Clumsy());
            cards.add(new CardOther2.CurseOfTheBell());
            // cards.add(new CardOther2.Debt()); // TODO
            cards.add(new CardOther2.Decay());
            cards.add(new CardOther2.Doubt());
            cards.add(new CardOther2.Enthralled());
            cards.add(new CardOther2.Folly());
            cards.add(new CardOther2.Greed());
            cards.add(new CardOther2.Guilty());
            cards.add(new CardOther2.Injury());
            cards.add(new CardOther2.Normality());
            cards.add(new CardOther2.PoorSleep());
            cards.add(new CardOther2.Regret());
            cards.add(new CardOther2.Shame());
            cards.add(new CardOther2.SporeMind());
            cards.add(new CardOther2.Writhe());
            cards.add(new CardOther2.ByrdonisEgg());
            cards.add(new CardOther2.LanternKey());
            cards.add(new CardOther2.SpoilsMap());
            cards.add(new CardOther2.Beckon());
            cards.add(new CardOther2.Burn());
            cards.add(new CardOther2.Dazed());
            cards.add(new CardOther2.Debris());
            cards.add(new CardOther2.Disintegration());
            cards.add(new CardOther2.FranticEscape());
            cards.add(new CardOther2.Infection());
            // cards.add(new CardOther2.MindRot()); // TODO
            cards.add(new CardOther2.Slimed());
            // cards.add(new CardOther2.Sloth()); // TODO
            cards.add(new CardOther2.Soot());
            cards.add(new CardOther2.Toxic());
            cards.add(new CardOther2.Void());
            // cards.add(new CardOther2.WasteAway()); // TODO
            cards.add(new CardOther2.Wither());
            cards.add(new CardOther2.Wound());
        }
        return cards;
    }

    public static List<Card> getColorless2CardsTmp0Cost(boolean generateHealingCard) {
        List<Card> baseCards = getColorless2Cards(ColorlessCard2Pool.CARD_GENERATION);
        List<Card> cards = new ArrayList<>();
        for (Card card : baseCards) {
            cards.add(card.getTemporaryCostIfPossible(0));
        }
        return cards;
    }

    private static HashSet<Class<?>> colorless2CardClasses;

    public static boolean isColorless2Card(Card card) {
        if (colorless2CardClasses == null) {
            colorless2CardClasses = new HashSet<>();
            for (var c : getColorless2Cards(ColorlessCard2Pool.ALL)) {
                colorless2CardClasses.add(c.getClass());
            }
        }
        return colorless2CardClasses.contains(card.getBaseCard().getClass());
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
            cards.add(new CardIronclad.Rampage(8, 33, false));
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
            cards.add(new CardIronclad.Armaments());
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
            cards.add(new CardSilent.Flechettes());
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
            cards.add(new CardSilent.InfiniteBlades());
            cards.add(new CardSilent.NoxiousFumes());
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
            cards.add(new CardDefect.CompileDriver());
            cards.add(new CardDefect.GoForTheEyes());
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
            cards.add(new CardDefect.Hyperbeam());
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

    private static List<Card> getIronclad2Cards(int cardType, boolean generateHealingCard) {
        List<Card> cards = new ArrayList<>();

        if (cardType == Card.ATTACK) {
            // Common cards
            cards.add(new CardIronclad2.Anger());
            cards.add(new CardIronclad2.BodySlam());
            cards.add(new CardIronclad2.Breakthrough());
            cards.add(new CardIronclad2.Cinder());
            cards.add(new CardIronclad2.Headbutt());
            cards.add(new CardIronclad2.IronWave());
            cards.add(new CardIronclad2.MoltenFist());
            cards.add(new CardIronclad2.PerfectedStrike());
            cards.add(new CardIronclad2.PommelStrike());
            cards.add(new CardIronclad2.SetupStrike());
            cards.add(new CardIronclad2.SwordBoomerang());
            cards.add(new CardIronclad2.Thunderclap());
            cards.add(new CardIronclad2.TwinStrike());

            // Uncommon cards
            cards.add(new CardIronclad2.AshenStrike());
            cards.add(new CardIronclad2.Bludgeon());
            cards.add(new CardIronclad2.Bully());
            cards.add(new CardIronclad2.Dismantle());
            cards.add(new CardIronclad2.FightMe());
            cards.add(new CardIronclad2.Hemokinesis());
            cards.add(new CardIronclad2.HowlFromBeyond());
            cards.add(new CardIronclad2.Pillage());
            cards.add(new CardIronclad2.Rampage());
            cards.add(new CardIronclad2.Spite());
            cards.add(new CardIronclad2.Stomp());
            cards.add(new CardIronclad2.Unrelenting());
            cards.add(new CardIronclad2.Uppercut());
            cards.add(new CardIronclad2.Whirlwind());

            // Rare cards
            cards.add(new CardIronclad2.Conflagration());
            cards.add(new CardIronclad2.Feed());
            cards.add(new CardIronclad2.FiendFire());
            cards.add(new CardIronclad2.Mangle());
            cards.add(new CardIronclad2.PactsEnd());
            cards.add(new CardIronclad2.TearAsunder());
            // cards.add(new CardIronclad2.Thrash()); // TODO
        }
        else if (cardType == Card.SKILL) {
            // Common cards
            cards.add(new CardIronclad2.Armaments());
            cards.add(new CardIronclad2.BloodWall());
            cards.add(new CardIronclad2.Bloodletting());
            cards.add(new CardIronclad2.Havoc());
            cards.add(new CardIronclad2.ShrugItOff());
            cards.add(new CardIronclad2.Tremble());
            cards.add(new CardIronclad2.TrueGrit());

            // Uncommon cards
            cards.add(new CardIronclad2.BattleTrance());
            cards.add(new CardIronclad2.BurningPact());
            cards.add(new CardIronclad2.Colossus());
            cards.add(new CardIronclad2.Dominate());
            cards.add(new CardIronclad2.DrumOfBattle());
            cards.add(new CardIronclad2.EvilEye());
            cards.add(new CardIronclad2.ExpectAFight());
            cards.add(new CardIronclad2.FlameBarrier());
            cards.add(new CardIronclad2.ForgottenRitual());
            cards.add(new CardIronclad2.InfernalBlade());
            cards.add(new CardIronclad2.Rage());
            cards.add(new CardIronclad2.SecondWind());
            cards.add(new CardIronclad2.Taunt());

            // Rare cards
            cards.add(new CardIronclad2.Brand());
            cards.add(new CardIronclad2.Cascade());
            cards.add(new CardIronclad2.Impervious());
            // cards.add(new CardIronclad2.NotYet()); // TODO
            cards.add(new CardIronclad2.Offering());
            cards.add(new CardIronclad2.OneTwoPunch());
            cards.add(new CardIronclad2.PrimalForce());
            cards.add(new CardIronclad2.Stoke());
        }
        else if (cardType == Card.POWER) {
            // Uncommon cards
            cards.add(new CardIronclad2.FeelNoPain());
            cards.add(new CardIronclad2.Inferno());
            cards.add(new CardIronclad2.Inflame());
            cards.add(new CardIronclad2.Juggling());
            cards.add(new CardIronclad2.Rupture());
            cards.add(new CardIronclad2.Stampede());
            cards.add(new CardIronclad2.StoneArmor());
            cards.add(new CardIronclad2.Vicious());

            // Rare cards
            cards.add(new CardIronclad2.Aggression());
            cards.add(new CardIronclad2.Barricade());
            cards.add(new CardIronclad2.CrimsonMantle());
            cards.add(new CardIronclad2.Cruelty());
            cards.add(new CardIronclad2.DarkEmbrace());
            cards.add(new CardIronclad2.DemonForm());
            cards.add(new CardIronclad2.Hellraiser());
            cards.add(new CardIronclad2.Juggernaut());
            cards.add(new CardIronclad2.Pyre());
            cards.add(new CardIronclad2.Unmovable());
        }

        return cards;
    }

    private static List<Card> getSilent2Cards(int cardType, boolean generateHealingCard) {
        List<Card> cards = new ArrayList<>();

        if (cardType == Card.ATTACK) {
            // Common cards
            cards.add(new CardSilent2.DaggerSpray());
            cards.add(new CardSilent2.DaggerThrow());
            cards.add(new CardSilent2.FlickFlack());
            cards.add(new CardSilent2.LeadingStrike());
            cards.add(new CardSilent2.PoisonedStab());
            cards.add(new CardSilent2.Predator());
            cards.add(new CardSilent2.Ricochet());
            cards.add(new CardSilent2.Slice());
            cards.add(new CardSilent2.SuckerPunch());

            // Uncommon cards
            cards.add(new CardSilent2.Backstab());
            cards.add(new CardSilent2.Dash());
            cards.add(new CardSilent2.Finisher());
            cards.add(new CardSilent2.Flechettes());
            cards.add(new CardSilent2.MementoMori());
            cards.add(new CardSilent2.Pinpoint());
            cards.add(new CardSilent2.Pounce());
            cards.add(new CardSilent2.PreciseCut());
            cards.add(new CardSilent2.Skewer());
            cards.add(new CardSilent2.Strangle());

            // Rare cards
            cards.add(new CardSilent2.Assassinate());
            cards.add(new CardSilent2.EchoingSlash());
            cards.add(new CardSilent2.GrandFinale());
            cards.add(new CardSilent2.Murder());
            cards.add(new CardSilent2.TheHunt(0.0));
        }
        else if (cardType == Card.SKILL) {
            // Common cards
            cards.add(new CardSilent2.Anticipate());
            cards.add(new CardSilent2.Backflip());
            cards.add(new CardSilent2.BladeDance());
            cards.add(new CardSilent2.CloakAndDagger());
            cards.add(new CardSilent2.DeadlyPoison());
            cards.add(new CardSilent2.Deflect());
            cards.add(new CardSilent2.DodgeAndRoll());
            cards.add(new CardSilent2.PiercingWail());
            cards.add(new CardSilent2.Prepared());
            cards.add(new CardSilent2.Snakebite());
            cards.add(new CardSilent2.Untouchable());

            // Uncommon cards
            cards.add(new CardSilent2.Acrobatics());
            cards.add(new CardSilent2.Blur());
            cards.add(new CardSilent2.BouncingFlask());
            cards.add(new CardSilent2.BubbleBubble());
            cards.add(new CardSilent2.CalculatedGamble());
            cards.add(new CardSilent2.EscapePlan());
            cards.add(new CardSilent2.Expertise());
            cards.add(new CardSilent2.Expose());
            cards.add(new CardSilent2.HandTrick());
            cards.add(new CardSilent2.Haze());
            cards.add(new CardSilent2.HiddenDaggers());
            cards.add(new CardSilent2.LegSweep());
            cards.add(new CardSilent2.Mirage());
            cards.add(new CardSilent2.Reflex());
            cards.add(new CardSilent2.Scare());
            cards.add(new CardSilent2.Tactician());
            cards.add(new CardSilent2.UpMySleeve());

            // Rare cards
            cards.add(new CardSilent2.Adrenaline());
            cards.add(new CardSilent2.BladeOfInk());
            cards.add(new CardSilent2.BulletTime());
            cards.add(new CardSilent2.Burst());
            cards.add(new CardSilent2.CorrosiveWave());
            cards.add(new CardSilent2.KnifeTrap());
            cards.add(new CardSilent2.Malaise());
            cards.add(new CardSilent2.Nightmare());
            cards.add(new CardSilent2.ShadowStep());
            cards.add(new CardSilent2.Shadowmeld());
            cards.add(new CardSilent2.StormOfSteel());
        }
        else if (cardType == Card.POWER) {
            // Uncommon cards
            cards.add(new CardSilent2.Accuracy());
            cards.add(new CardSilent2.Footwork());
            cards.add(new CardSilent2.InfiniteBlades());
            cards.add(new CardSilent2.NoxiousFumes());
            cards.add(new CardSilent2.Outbreak());
            cards.add(new CardSilent2.PhantomBlades());
            cards.add(new CardSilent2.Speedster());
            cards.add(new CardSilent2.WellLaidPlans());

            // Rare cards
            cards.add(new CardSilent2.Abrasive());
            cards.add(new CardSilent2.Accelerant());
            cards.add(new CardSilent2.AfterImage());
            cards.add(new CardSilent2.Envenom());
            cards.add(new CardSilent2.FanOfKnives());
            cards.add(new CardSilent2.MasterPlanner());
            cards.add(new CardSilent2.SerpentForm());
            cards.add(new CardSilent2.ToolsOfTheTrade());
            cards.add(new CardSilent2.Tracking());
        }

        return cards;
    }

    private static List<Card> getDefect2Cards(int cardType, boolean generateHealingCard) {
        List<Card> cards = new ArrayList<>();

        if (cardType == Card.ATTACK) {
            // Common cards
            cards.add(new CardDefect2.BallLightning());
            cards.add(new CardDefect2.Barrage());
            cards.add(new CardDefect2.BeamCell());
            cards.add(new CardDefect2.Claw());
            cards.add(new CardDefect2.ColdSnap());
            cards.add(new CardDefect2.CompileDriver());
            cards.add(new CardDefect2.FocusedStrike());
            cards.add(new CardDefect2.GoForTheEyes());
            cards.add(new CardDefect2.GunkUp());
            cards.add(new CardDefect2.MomentumStrike());
            cards.add(new CardDefect2.SweepingBeam());
            cards.add(new CardDefect2.Uproar());

            // Uncommon cards
            cards.add(new CardDefect2.FTL());
            cards.add(new CardDefect2.Null());
            cards.add(new CardDefect2.Refract());
            cards.add(new CardDefect2.RocketPunch());
            cards.add(new CardDefect2.Scrape());
            cards.add(new CardDefect2.Sunder());
            cards.add(new CardDefect2.Synthesis());
            cards.add(new CardDefect2.TeslaCoil());

            // Rare cards
            cards.add(new CardDefect2.AdaptiveStrike());
            cards.add(new CardDefect2.AllForOne(0, 0));
            cards.add(new CardDefect2.FlakCannon());
            cards.add(new CardDefect2.HelixDrill());
            cards.add(new CardDefect2.Hyperbeam());
            cards.add(new CardDefect2.IceLance());
            cards.add(new CardDefect2.MeteorStrike());
            cards.add(new CardDefect2.Shatter());
        }
        else if (cardType == Card.SKILL) {
            // Common cards
            cards.add(new CardDefect2.BoostAway());
            cards.add(new CardDefect2.ChargeBattery());
            cards.add(new CardDefect2.Coolheaded());
            cards.add(new CardDefect2.Hologram());
            cards.add(new CardDefect2.Hotfix());
            cards.add(new CardDefect2.Leap());
            cards.add(new CardDefect2.LightningRod());
            cards.add(new CardDefect2.Turbo());

            // Uncommon cards
            cards.add(new CardDefect2.BootSequence());
            cards.add(new CardDefect2.Chaos());
            cards.add(new CardDefect2.Chill());
            cards.add(new CardDefect2.Compact());
            cards.add(new CardDefect2.Darkness());
            cards.add(new CardDefect2.DoubleEnergy());
            cards.add(new CardDefect2.FightThrough());
            cards.add(new CardDefect2.Fusion());
            cards.add(new CardDefect2.Glacier());
            cards.add(new CardDefect2.Glasswork());
            cards.add(new CardDefect2.Overclock());
            cards.add(new CardDefect2.Scavenge());
            cards.add(new CardDefect2.ShadowShield());
            cards.add(new CardDefect2.Skim());
            cards.add(new CardDefect2.Synchronize());
            cards.add(new CardDefect2.Tempest());
            cards.add(new CardDefect2.WhiteNoise());

            // Rare cards
            cards.add(new CardDefect2.GeneticAlgorithm());
            cards.add(new CardDefect2.Modded());
            cards.add(new CardDefect2.MultiCast());
            cards.add(new CardDefect2.Rainbow());
            cards.add(new CardDefect2.Reboot());
            cards.add(new CardDefect2.SignalBoost());
            cards.add(new CardDefect2.Supercritical());
            cards.add(new CardDefect2.Voltaic());
        }
        else if (cardType == Card.POWER) {
            // Uncommon cards
            cards.add(new CardDefect2.BulkUp());
            cards.add(new CardDefect2.Capacitor());
            cards.add(new CardDefect2.Feral());
            cards.add(new CardDefect2.Hailstorm());
            cards.add(new CardDefect2.Iteration());
            cards.add(new CardDefect2.Loop());
            cards.add(new CardDefect2.Smokestack());
            cards.add(new CardDefect2.Storm());
            cards.add(new CardDefect2.Subroutine());
            cards.add(new CardDefect2.Thunder());

            // Rare cards
            cards.add(new CardDefect2.Buffer());
            cards.add(new CardDefect2.ConsumingShadow());
            cards.add(new CardDefect2.Coolant());
            cards.add(new CardDefect2.CreativeAI());
            cards.add(new CardDefect2.Defragment());
            cards.add(new CardDefect2.EchoForm());
            cards.add(new CardDefect2.MachineLearning());
            cards.add(new CardDefect2.Spinner());
            cards.add(new CardDefect2.TrashToTreasure());
        }

        return cards;
    }

    private static List<Card> getRegentCards(int cardType, boolean generateHealingCard) {
        List<Card> cards = new ArrayList<>();

        if (cardType == Card.ATTACK) {
            // Common cards
            cards.add(new CardRegent2.AstralPulse());
            cards.add(new CardRegent2.CelestialMight());
            cards.add(new CardRegent2.CollisionCourse());
            cards.add(new CardRegent2.CrescentSpear());
            cards.add(new CardRegent2.CrushUnder());
            cards.add(new CardRegent2.GuidingStar());
            cards.add(new CardRegent2.PhotonCut());
            cards.add(new CardRegent2.SolarStrike());
            cards.add(new CardRegent2.WroughtInWar());

            // Uncommon cards
            cards.add(new CardRegent2.Devastate());
            cards.add(new CardRegent2.GammaBlast());
            cards.add(new CardRegent2.Hegemony());
            cards.add(new CardRegent2.KinglyKick());
            cards.add(new CardRegent2.KinglyPunch());
            cards.add(new CardRegent2.KnockoutBlow());
            cards.add(new CardRegent2.LunarBlast());
            cards.add(new CardRegent2.Radiate());
            cards.add(new CardRegent2.ShiningStrike());
            cards.add(new CardRegent2.Stardust());
            cards.add(new CardRegent2.Supermassive());

            // Rare cards
            cards.add(new CardRegent2.BeatIntoShape());
            cards.add(new CardRegent2.Bombardment());
            cards.add(new CardRegent2.Comet());
            cards.add(new CardRegent2.CrashLanding());
            cards.add(new CardRegent2.DyingStar());
            cards.add(new CardRegent2.HeavenlyDrill());
            cards.add(new CardRegent2.HeirloomHammer());
            cards.add(new CardRegent2.MakeItSo());
            cards.add(new CardRegent2.SevenStars());
        }
        else if (cardType == Card.SKILL) {
            // Common cards
            cards.add(new CardRegent2.Begone());
            cards.add(new CardRegent2.CloakOfStars());
            cards.add(new CardRegent2.CosmicIndifference());
            cards.add(new CardRegent2.GatherLight());
            cards.add(new CardRegent2.Glitterstream());
            cards.add(new CardRegent2.Glow());
            cards.add(new CardRegent2.HiddenCache());
            cards.add(new CardRegent2.KnowThyPlace());
            cards.add(new CardRegent2.Patter());
            cards.add(new CardRegent2.RefineBlade());
            cards.add(new CardRegent2.SpoilsOfBattle());

            // Uncommon cards
            cards.add(new CardRegent2.Alignment());
            cards.add(new CardRegent2.Bulwark());
            cards.add(new CardRegent2.Charge());
            cards.add(new CardRegent2.Conqueror());
            cards.add(new CardRegent2.Convergence());
            cards.add(new CardRegent2.Glimmer());
            cards.add(new CardRegent2.ManifestAuthority());
            cards.add(new CardRegent2.Monologue());
            cards.add(new CardRegent2.ParticleWall());
            cards.add(new CardRegent2.Prophesize());
            cards.add(new CardRegent2.Quasar());
            cards.add(new CardRegent2.Reflect());
            cards.add(new CardRegent2.Resonance());
            cards.add(new CardRegent2.RoyalGamble());
            cards.add(new CardRegent2.SummonForth());
            cards.add(new CardRegent2.Terraforming());

            // Rare cards
            cards.add(new CardRegent2.BigBang());
            cards.add(new CardRegent2.BundleOfJoy());
            cards.add(new CardRegent2.DecisionsDecisions());
            // cards.add(new CardRegent2.ForegoneConclusion()); // TODO
            cards.add(new CardRegent2.Guards());
            cards.add(new CardRegent2.IAmInvincible());
            cards.add(new CardRegent2.TheSmith());
        }
        else if (cardType == Card.POWER) {
            // Uncommon cards
            cards.add(new CardRegent2.BlackHole());
            cards.add(new CardRegent2.ChildOfTheStars());
            cards.add(new CardRegent2.Furnace());
            cards.add(new CardRegent2.Orbit());
            cards.add(new CardRegent2.PaleBlueDot());
            cards.add(new CardRegent2.Parry());
            cards.add(new CardRegent2.PillarOfCreation());
            cards.add(new CardRegent2.SpectrumShift());

            // Rare cards
            cards.add(new CardRegent2.Arsenal());
            cards.add(new CardRegent2.Genesis());
            cards.add(new CardRegent2.MonarchsGaze());
            cards.add(new CardRegent2.NeutronAegis());
            cards.add(new CardRegent2.Royalties(0.0));
            cards.add(new CardRegent2.SeekingEdge());
            cards.add(new CardRegent2.SwordSage());
            // cards.add(new CardRegent2.Tyranny()); // TODO
            cards.add(new CardRegent2.VoidForm());
        }

        return cards;
    }

    private static List<Card> getNecrobinderCards(int cardType, boolean generateHealingCard) {
        List<Card> cards = new ArrayList<>();

        if (cardType == Card.ATTACK) {
            // Common cards
            cards.add(new CardNecrobinder2.BlightStrike());
            cards.add(new CardNecrobinder2.Defile());
            cards.add(new CardNecrobinder2.DrainPower());
            cards.add(new CardNecrobinder2.Fear());
            cards.add(new CardNecrobinder2.Flatten());
            cards.add(new CardNecrobinder2.Graveblast());
            cards.add(new CardNecrobinder2.Poke());
            cards.add(new CardNecrobinder2.Reap());
            cards.add(new CardNecrobinder2.Reave());
            cards.add(new CardNecrobinder2.SculptingStrike());
            cards.add(new CardNecrobinder2.Snap());
            cards.add(new CardNecrobinder2.Sow());

            // Uncommon cards
            cards.add(new CardNecrobinder2.BoneShards());
            cards.add(new CardNecrobinder2.Bury());
            cards.add(new CardNecrobinder2.DeathMarch());
            cards.add(new CardNecrobinder2.Debilitate());
            cards.add(new CardNecrobinder2.Fetch());
            cards.add(new CardNecrobinder2.HighFive());
            cards.add(new CardNecrobinder2.PullFromBelow());
            cards.add(new CardNecrobinder2.Rattle());
            cards.add(new CardNecrobinder2.RightHandHand());
            cards.add(new CardNecrobinder2.Severance());
            cards.add(new CardNecrobinder2.SicEm());
            cards.add(new CardNecrobinder2.Veilpiercer());

            // Rare cards
            cards.add(new CardNecrobinder2.BansheesCry());
            cards.add(new CardNecrobinder2.Eradicate());
            cards.add(new CardNecrobinder2.Hang());
            cards.add(new CardNecrobinder2.Misery());
            cards.add(new CardNecrobinder2.SoulStorm());
            cards.add(new CardNecrobinder2.Squeeze());
            cards.add(new CardNecrobinder2.TheScythe());
            cards.add(new CardNecrobinder2.TimesUp());
        }
        else if (cardType == Card.SKILL) {
            // Common cards
            cards.add(new CardNecrobinder2.Afterlife());
            cards.add(new CardNecrobinder2.Defy());
            cards.add(new CardNecrobinder2.GraveWarden());
            cards.add(new CardNecrobinder2.Invoke());
            cards.add(new CardNecrobinder2.NegativePulse());
            cards.add(new CardNecrobinder2.PullAggro());
            cards.add(new CardNecrobinder2.Scourge());
            cards.add(new CardNecrobinder2.Wisp());

            // Uncommon cards
            cards.add(new CardNecrobinder2.BorrowedTime());
            cards.add(new CardNecrobinder2.CaptureSpirit());
            cards.add(new CardNecrobinder2.Cleanse());
            cards.add(new CardNecrobinder2.DeathsDoor());
            cards.add(new CardNecrobinder2.Deathbringer());
            cards.add(new CardNecrobinder2.Delay());
            cards.add(new CardNecrobinder2.Dirge());
            cards.add(new CardNecrobinder2.Dredge());
            cards.add(new CardNecrobinder2.EnfeeblingTouch());
            cards.add(new CardNecrobinder2.Melancholy());
            cards.add(new CardNecrobinder2.NoEscape());
            cards.add(new CardNecrobinder2.Parse());
            cards.add(new CardNecrobinder2.Putrefy());
            cards.add(new CardNecrobinder2.Spur());

            // Rare cards
            cards.add(new CardNecrobinder2.Eidolon());
            cards.add(new CardNecrobinder2.EndOfDays());
            cards.add(new CardNecrobinder2.Oblivion());
            cards.add(new CardNecrobinder2.Reanimate());
            cards.add(new CardNecrobinder2.Sacrifice());
            cards.add(new CardNecrobinder2.Seance());
            cards.add(new CardNecrobinder2.SharedFate());
            cards.add(new CardNecrobinder2.Transfigure());
            cards.add(new CardNecrobinder2.Undeath());
        }
        else if (cardType == Card.POWER) {
            // Uncommon cards
            cards.add(new CardNecrobinder2.Calcify());
            cards.add(new CardNecrobinder2.Countdown());
            cards.add(new CardNecrobinder2.DanseMacabre());
            cards.add(new CardNecrobinder2.Friendship());
            cards.add(new CardNecrobinder2.Haunt());
            cards.add(new CardNecrobinder2.Lethality());
            cards.add(new CardNecrobinder2.Pagestorm());
            cards.add(new CardNecrobinder2.Shroud());
            cards.add(new CardNecrobinder2.SleightOfFlesh());

            // Rare cards
            cards.add(new CardNecrobinder2.CallOfTheVoid());
            cards.add(new CardNecrobinder2.Demesne());
            cards.add(new CardNecrobinder2.DevourLife());
            cards.add(new CardNecrobinder2.NecroMastery());
            cards.add(new CardNecrobinder2.Neurosurge());
            cards.add(new CardNecrobinder2.ReaperForm());
            cards.add(new CardNecrobinder2.SentryMode());
            cards.add(new CardNecrobinder2.SpiritOfAsh());
        }

        return cards;
    }

    public static List<Card> getCharacterRareCards(CharacterEnum character, boolean generateHealingCard) {
        return getCharacterCards(character, generateHealingCard).stream().filter((card) -> card.rarity == Card.RARE).toList();
    }

    public static List<Card> getCharacter0CostCardsByType(CharacterEnum character) {
        return getCharacterCards(character, false).stream().filter((card) -> card.energyCost == 0).toList();
    }

    public static List<Card> getCharacterEtherealCardsByType(CharacterEnum character) {
        return getCharacterCards(character, false).stream().filter(Card::ethereal).toList();
    }

    public static List<Card> getPossibleSelect1OutOf3CardsFromRewardScreen(CharacterEnum character) {
        List<Card> baseCards = getCharacterCards(character, true);
        List<Card> cards = new ArrayList<>();
        for (Card card : baseCards) {
            cards.add(card);
            cards.add(card.getUpgrade());
        }
        return cards;
    }
}
