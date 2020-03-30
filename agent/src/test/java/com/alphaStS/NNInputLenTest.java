package com.alphaStS;

import com.alphaStS.card.*;
import com.alphaStS.enemy.EnemyCity;
import com.alphaStS.enemy.EnemyEncounter;
import com.alphaStS.enemy.EnemyEnding;
import com.alphaStS.enemy.EnemyExordium;
import com.alphaStS.entity.Potion;
import com.alphaStS.entity.Relic;
import com.alphaStS.player.Player;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NNInputLenTest {
    // region TestStates

    @Test
    public void testNNInputLen() {
        assertEquals(33, TestStates.BasicGremlinNobState().properties.inputLen);
        assertEquals(33, TestStates.BasicGremlinNobState2().properties.inputLen);
        assertEquals(25, TestStates.BasicInfiniteState().properties.inputLen);
        assertEquals(42, TestStates.BasicJawWormState().properties.inputLen);
        assertEquals(54, TestStates.BasicLagavulinState().properties.inputLen);
        assertEquals(42, TestStates.BasicLagavulinState2().properties.inputLen);
        assertEquals(73, TestStates.BasicSentriesState().properties.inputLen);
        assertEquals(51, TestStates.GuardianState().properties.inputLen);
        assertEquals(91, TestStates.GuardianState2().properties.inputLen);
        assertEquals(114, TestStates.SlimeBossStateLC().properties.inputLen);
        assertEquals(81, TestStates.TestState().properties.inputLen);
        assertEquals(71, TestStates.TestState10().properties.inputLen);
        assertEquals(92, TestStates.TestState12().properties.inputLen);
        assertEquals(116, TestStates.TestState13().properties.inputLen);
        assertEquals(100, TestStates.TestState14().properties.inputLen);
        assertEquals(133, TestStates.TestState15().properties.inputLen);
        assertEquals(112, TestStates.TestState16().properties.inputLen);
        assertEquals(212, TestStates.TestState16b().properties.inputLen);
        assertEquals(223, TestStates.TestState16c2().properties.inputLen);
        assertEquals(144, TestStates.TestState17().properties.inputLen);
        assertEquals(97, TestStates.TestState2().properties.inputLen);
        assertEquals(121, TestStates.TestState3().properties.inputLen);
        assertEquals(103, TestStates.TestState4().properties.inputLen);
        assertEquals(153, TestStates.TestState5().properties.inputLen);
        assertEquals(134, TestStates.TestState6().properties.inputLen);
        assertEquals(121, TestStates.TestState7().properties.inputLen);
        assertEquals(74, TestStates.TestState8().properties.inputLen);
        assertEquals(169, TestStates.TestState9().properties.inputLen);
        assertEquals(137, TestStates.TestStateDefect().properties.inputLen);
        assertEquals(106, TestStates.TestStateDefect1p1().properties.inputLen);
        assertEquals(175, TestStates.TestStateDefect1p2().properties.inputLen);
        assertEquals(193, TestStates.TestStateDefect1p3().properties.inputLen);
        assertEquals(195, TestStates.TestStateDefect1p4().properties.inputLen);
        assertEquals(85, TestStates.TestStateDefect2().properties.inputLen);
        assertEquals(50, TestStates.TestStateDefect3().properties.inputLen);
        assertEquals(183, TestStates.TestStateDefectB1().properties.inputLen);
        assertEquals(292, TestStates.TestStateDefectB2().properties.inputLen);
        assertEquals(723, TestStates.TestStateDefectB3().properties.inputLen);
        assertEquals(704, TestStates.TestStateDefectB4().properties.inputLen);
        assertEquals(611, TestStates.TestStateDefectRun10().properties.inputLen);
        assertEquals(608, TestStates.TestStateDefectRun10D().properties.inputLen);
        assertEquals(652, TestStates.TestStateDefectRun11().properties.inputLen);
        assertEquals(444, TestStates.TestStateDefectRun4().properties.inputLen);
        assertEquals(393, TestStates.TestStateDefectRun5().properties.inputLen);
        assertEquals(658, TestStates.TestStateDefectRun6().properties.inputLen);
        assertEquals(252, TestStates.TestStateDefectRun7().properties.inputLen);
        assertEquals(259, TestStates.TestStateDefectRun8().properties.inputLen);
        assertEquals(571, TestStates.TestStateDefectRun9().properties.inputLen);
        assertEquals(247, TestStates.TestStateIronCladHeart().properties.inputLen);
        assertEquals(230, TestStates.TestStateReddit().properties.inputLen);
        assertEquals(208, TestStates.TestStateReplayLostRunIronClad().properties.inputLen);
        assertEquals(196, TestStates.TestStateSilent2().properties.inputLen);
        assertEquals(270, TestStates.TestStateSilentHeartOrangePellet().properties.inputLen);
        assertEquals(156, TestStates.TestStateSilentRun10().properties.inputLen);
        assertEquals(525, TestStates.TestStateSilentRun2().properties.inputLen);
        assertEquals(641, TestStates.TestStateSilentRun3().properties.inputLen);
        assertEquals(280, TestStates.TestStateSilentRun4().properties.inputLen);
        assertEquals(233, TestStates.TestStateSilentRun5().properties.inputLen);
        assertEquals(173, TestStates.TestStateSilentRun6().properties.inputLen);
        assertEquals(554, TestStates.TestStateSilentRun7().properties.inputLen);
        assertEquals(533, TestStates.TestStateSilentRun8().properties.inputLen);
        assertEquals(522, TestStates.TestStateSilentRun9().properties.inputLen);
        assertEquals(260, TestStates.TestStateSilentTodo().properties.inputLen);
        assertEquals(230, TestStates.TestStateStreamerRun().properties.inputLen);
        assertEquals(247, TestStates.TestStateStreamerRun3().properties.inputLen);
        assertEquals(237, TestStates.TestStateStreamerRun4().properties.inputLen);
        assertEquals(219, TestStates.TestStateStreamerRun6().properties.inputLen);
        assertEquals(195, TestStates.TestStateXecnar2().properties.inputLen);
    }

    // endregion

    // region Card flags
    //
    // One representative card per Card boolean flag (Ironclad > Silent > Defect > Watcher):
    //   changePlayerStrength    -> CardIronclad.Flex
    //   changePlayerStrengthEot -> CardIronclad.Flex      (same card)
    //   changePlayerDexterity   -> CardDefect.Reprogram
    //   changePlayerFocus       -> CardDefect.Consume
    //   changePlayerArtifact    -> CardDefect.CoreSurge
    //   changePlayerVulnerable  -> CardIronclad.Berserk
    //   vulnEnemy               -> CardIronclad.Bash
    //   weakEnemy               -> CardIronclad.Clothesline
    //   chokeEnemy              -> CardSilent.Choke
    //   poisonEnemy             -> CardSilent.DeadlyPoison
    //   corpseExplosionEnemy    -> CardSilent.CorpseExplosion
    //   lockOnEnemy             -> CardDefect.BullsEye
    //   talkToTheHandEnemy      -> CardWatcher.TalkToTheHand
    //   markEnemy               -> CardWatcher.PressurePoints
    //   affectEnemyStrength     -> CardIronclad.Disarm
    //   affectEnemyStrengthEot  -> CardSilent.PiercingWail

    @Test
    public void testCardEffectsNNInputLen() {
        var builder = new GameStateBuilder();

        builder.addCard(new CardIronclad.Flex());          // changePlayerStrength + changePlayerStrengthEot
        builder.addCard(new CardDefect.Reprogram());       // changePlayerDexterity
        builder.addCard(new CardDefect.Consume());         // changePlayerFocus
        builder.addCard(new CardDefect.CoreSurge());       // changePlayerArtifact
        builder.addCard(new CardIronclad.Berserk());       // changePlayerVulnerable
        builder.addCard(new CardIronclad.Bash());          // vulnEnemy
        builder.addCard(new CardIronclad.Clothesline());   // weakEnemy
        builder.addCard(new CardSilent.Choke());           // chokeEnemy
        builder.addCard(new CardSilent.DeadlyPoison());    // poisonEnemy
        builder.addCard(new CardSilent.CorpseExplosion()); // corpseExplosionEnemy
        builder.addCard(new CardDefect.BullsEye());        // lockOnEnemy
        builder.addCard(new CardWatcher.TalkToTheHand());  // talkToTheHandEnemy
        builder.addCard(new CardWatcher.PressurePoints()); // markEnemy
        builder.addCard(new CardIronclad.Disarm());        // affectEnemyStrength
        builder.addCard(new CardSilent.PiercingWail());    // affectEnemyStrengthEot

        builder.setPlayer(new Player(80, 80));
        EnemyEncounter.addByrdsFight(builder);

        assertEquals(129, new GameState(builder).properties.inputLen);
    }

    // endregion

    // region Potion flags
    //
    // One representative potion per Potion boolean flag:
    //   vulnEnemy                -> Potion.FearPotion     (also covers selectEnemy)
    //   weakEnemy                -> Potion.WeakPotion
    //   changePlayerStrength     -> Potion.StrengthPotion
    //   changePlayerStrengthEot  -> Potion.FlexPotion
    //   changePlayerFocus        -> Potion.FocusPotion
    //   changePlayerDexterity    -> Potion.DexterityPotion
    //   changePlayerDexterityEot -> Potion.SpeedPotion
    //   changePlayerArtifact     -> Potion.AncientPotion
    //   selectEnemy              -> Potion.FearPotion      (also covers vulnEnemy)
    //   poisonEnemy              -> Potion.PoisonPotion
    //   healPlayer               -> Potion.BloodPotion
    //   selectFromHand           -> Potion.GamblersBrew
    //   selectFromDiscard        -> Potion.LiquidMemory

    @Test
    public void testPotionEffectsNNInputLen() {
        var builder = new GameStateBuilder();

        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.SeverSoul(), 1);
        builder.addCard(new CardIronclad.Clash(), 1);
        builder.addCard(new CardIronclad.Headbutt(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.BurningPactP(), 1);

        EnemyEncounter.addSentriesFight(builder);

        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.BronzeScales());
        builder.addRelic(new Relic.Vajra());

        builder.addPotion(new Potion.FearPotion());      // vulnEnemy + selectEnemy
        builder.addPotion(new Potion.WeakPotion());      // weakEnemy
        builder.addPotion(new Potion.StrengthPotion());  // changePlayerStrength
        builder.addPotion(new Potion.FlexPotion());      // changePlayerStrengthEot
        builder.addPotion(new Potion.FocusPotion());     // changePlayerFocus
        builder.addPotion(new Potion.DexterityPotion()); // changePlayerDexterity
        builder.addPotion(new Potion.SpeedPotion());     // changePlayerDexterityEot
        builder.addPotion(new Potion.AncientPotion());   // changePlayerArtifact
        builder.addPotion(new Potion.PoisonPotion());    // poisonEnemy
        builder.addPotion(new Potion.BloodPotion());     // healPlayer
        builder.addPotion(new Potion.GamblersBrew());    // selectFromHand
        builder.addPotion(new Potion.LiquidMemories());  // selectFromDiscard

        builder.setPlayer(new Player(32, 75));

        assertEquals(134, new GameState(builder).properties.inputLen);
    }

    // endregion

    // region Relic flags
    //
    // One representative relic per Relic boolean flag:
    //   changePlayerStrength     -> Relic.Shuriken
    //   changePlayerDexterity    -> Relic.Kunai
    //   changePlayerDexterityEot -> Relic.Duality
    //   vulnEnemy                -> Relic.BagOfMarbles
    //   weakEnemy                -> Relic.RedMask
    //   poisonEnemy              -> Relic.TwistedFunnel
    //   healPlayer               -> Relic.BirdFacedUrn
    //   scry                     -> Relic.Melange
    //   changeEnemyStrength      -> Relic.Brimstone

    @Test
    public void testRelicEffectsNNInputLen() {
        var builder = new GameStateBuilder();

        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.SeverSoul(), 1);
        builder.addCard(new CardIronclad.Clash(), 1);
        builder.addCard(new CardIronclad.Headbutt(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.BurningPactP(), 1);

        EnemyEncounter.addSentriesFight(builder);

        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.BronzeScales());
        builder.addRelic(new Relic.Vajra());

        builder.addRelic(new Relic.Shuriken());      // changePlayerStrength
        builder.addRelic(new Relic.Kunai());          // changePlayerDexterity
        builder.addRelic(new Relic.Duality());        // changePlayerDexterityEot
        builder.addRelic(new Relic.BagOfMarbles());   // vulnEnemy
        builder.addRelic(new Relic.RedMask());        // weakEnemy
        builder.addRelic(new Relic.TwistedFunnel());  // poisonEnemy
        builder.addRelic(new Relic.BirdFacedUrn());   // healPlayer
        builder.addRelic(new Relic.Melange());        // scry
        builder.addRelic(new Relic.Brimstone());      // changeEnemyStrength

        builder.setPlayer(new Player(32, 75));

        assertEquals(92, new GameState(builder).properties.inputLen); // Duality now correctly contributes changePlayerDexterityEot
    }

    // endregion

    // region Enemy flags
    //
    // One representative enemy per EnemyReadOnly.EnemyProperties boolean flag:
    //   canVulnerable         -> EnemyExordium.GremlinNob
    //   canEntangle           -> EnemyExordium.RedSlaver
    //   canWeaken             -> EnemyExordium.BlueSlaver
    //   canFrail              -> EnemyExordium.LargeSpikeSlime
    //   changePlayerStrength  -> EnemyExordium.Lagavulin   (also changePlayerDexterity)
    //   changePlayerFocus     -> EnemyEnding.SpireShield   (also changePlayerStrength)
    //   changePlayerDexterity -> EnemyCity.Bear

    @Test
    public void testEnemyEffectsNNInputLen() {
        var builder = new GameStateBuilder();

        builder.addCard(new CardIronclad.Bash(), 1);
        builder.addCard(new Card.Strike(), 4);
        builder.addCard(new Card.Defend(), 4);
        builder.addCard(new CardOther.AscendersBane(), 1);
        builder.addCard(new CardIronclad.SeverSoul(), 1);
        builder.addCard(new CardIronclad.Clash(), 1);
        builder.addCard(new CardIronclad.Headbutt(), 1);
        builder.addCard(new CardIronclad.Anger(), 1);
        builder.addCard(new CardIronclad.BurningPactP(), 1);

        builder.addEnemyEncounter(
                new EnemyExordium.GremlinNob(),      // canVulnerable
                new EnemyExordium.RedSlaver(),       // canEntangle
                new EnemyExordium.BlueSlaver(),      // canWeaken
                new EnemyExordium.LargeSpikeSlime(), // canFrail
                new EnemyExordium.Lagavulin(),       // changePlayerStrength
                new EnemyEnding.SpireShield(),       // changePlayerFocus
                new EnemyCity.Bear()                 // changePlayerDexterity
        );

        builder.addRelic(new Relic.Orichalcum());
        builder.addRelic(new Relic.BronzeScales());
        builder.addRelic(new Relic.Vajra());

        builder.setPlayer(new Player(32, 75));

        assertEquals(119, new GameState(builder).properties.inputLen);
    }

    // endregion
}
