package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.action.PlayCardAction;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.enums.OrbType;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.eventHandler.OnCardCreationHandler;
import com.alphaStS.eventHandler.OnEnergySpendHandler;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.random.RandomGenCtx;
import com.alphaStS.utils.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CardDefect2 {
    // **************************************************************************************************
    // ********************************************* Basic  *********************************************
    // **************************************************************************************************

    // Defend (Defect) (Basic) - 1 energy, Skill
    //   Effect: Gain 5 Block.
    //   Upgraded Effect: Gain 8 Block.
    public static class Defend extends Card.Defend {
    }

    public static class DefendP extends Card.DefendP {
    }

    // Dualcast (Basic) - 1 energy, Skill
    //   Effect: Evoke your rightmost Orb twice.
    //   Upgraded Effect (0 energy): Evoke your rightmost Orb twice.
    public static class Dualcast extends CardDefect.Dualcast {
    }

    public static class DualCastP extends CardDefect.DualcastP {
    }

    // Strike (Defect) (Basic) - 1 energy, Attack
    //   Effect: Deal 6 damage.
    //   Upgraded Effect: Deal 9 damage.
    public static class Strike extends Card.Strike {
    }

    public static class StrikeP extends Card.StrikeP {
    }

    // Zap (Basic) - 1 energy, Skill
    //   Effect: Channel 1 Lightning.
    //   Upgraded Effect (0 energy): Channel 1 Lightning.
    public static class Zap extends CardDefect.Zap {
    }

    public static class ZapP extends CardDefect.ZapP {
    }

    // **************************************************************************************************
    // ********************************************* Common *********************************************
    // **************************************************************************************************

    // Ball Lightning (Common) - 1 energy, Attack
    //   Effect: Deal 7 damage. Channel 1 Lightning.
    //   Upgraded Effect: Deal 10 damage. Channel 1 Lightning.
    public static class BallLightning extends CardDefect.BallLightning {
    }

    public static class BallLightningP extends CardDefect.BallLightningP {
    }

    // Barrage (Common) - 1 energy, Attack
    //   Effect: Deal 5 damage for each Channeled Orb.
    //   Upgraded Effect: Deal 7 damage for each Channeled Orb.
    public static class Barrage extends CardDefect._BarrageT {
        public Barrage() {
            super("Barrage", 5);
        }
    }

    public static class BarrageP extends CardDefect._BarrageT {
        public BarrageP() {
            super("Barrage+", 7);
        }
    }

    // Beam Cell (Common) - 0 energy, Attack
    //   Effect: Deal 3 damage. Apply 1 Vulnerable.
    //   Upgraded Effect: Deal 4 damage. Apply 2 Vulnerable.
    public static class BeamCell extends CardDefect.BeamCell {
    }

    public static class BeamCellP extends CardDefect.BeamCellP {
    }

    private static abstract class _BoostAwayT extends Card {
        private final int block;

        public _BoostAwayT(String cardName, int block) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.addCardToDiscard(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardOther.Dazed());
        }
    }

    // Boost Away (Common) - 0 energy, Skill
    //   Effect: Gain 6 Block. Add a Dazed into your Discard Pile.
    //   Upgraded Effect: Gain 9 Block. Add a Dazed into your Discard Pile.
    public static class BoostAway extends _BoostAwayT {
        public BoostAway() {
            super("Boost Away", 6);
        }
    }

    public static class BoostAwayP extends _BoostAwayT {
        public BoostAwayP() {
            super("Boost Away+", 9);
        }
    }

    // Charge Battery (Common) - 1 energy, Skill
    //   Effect: Gain 7 Block. Next turn, gain energy.
    //   Upgraded Effect: Gain 10 Block. Next turn, gain energy.
    public static class ChargeBattery extends CardDefect.ChargeBattery {
    }

    public static class ChargeBatteryP extends CardDefect.ChargeBatteryP {
    }

    // Claw (Common) - 0 energy, Attack
    //   Effect: Deal 3 damage. Increase the damage of ALL Claw cards by 2 this combat.
    //   Upgraded Effect: Deal 4 damage. Increase the damage of ALL Claw cards by 3 this combat.
    public static class Claw extends CardDefect._ClawT {
        public Claw() {
            this(3);
        }

        public Claw(int dmg) {
            super("Claw", dmg, 2);
        }

        @Override protected CardDefect._ClawT create(int dmg) {
            return new Claw(dmg);
        }

        @Override protected int[] getTransformIndexes(GameProperties properties) {
            return properties.clawTransformIndexes;
        }

        @Override protected void setTransformIndexes(GameProperties properties, int[] transformIndexes) {
            properties.clawTransformIndexes = transformIndexes;
        }

        @Override protected int[] getAfterPlayTransformIndexes(GameProperties properties) {
            return properties.clawAfterPlayTransformIndexes;
        }

        @Override protected void setAfterPlayTransformIndexes(GameProperties properties, int[] transformIndexes) {
            properties.clawAfterPlayTransformIndexes = transformIndexes;
        }

        public Card getUpgrade() {
            if (dmg + 1 > GameProperties.maxClawDamage) {
                return null;
            } else {
                return new CardDefect2.ClawP(dmg + 1);
            }
        }
    }

    public static class ClawP extends CardDefect._ClawT {
        public ClawP() {
            this(4);
        }

        public ClawP(int dmg) {
            super("Claw+", dmg, 3);
        }

        @Override protected CardDefect._ClawT create(int dmg) {
            return new ClawP(dmg);
        }

        @Override protected int[] getTransformIndexes(GameProperties properties) {
            return properties.clawPTransformIndexes;
        }

        @Override protected void setTransformIndexes(GameProperties properties, int[] transformIndexes) {
            properties.clawPTransformIndexes = transformIndexes;
        }

        @Override protected int[] getAfterPlayTransformIndexes(GameProperties properties) {
            return properties.clawPAfterPlayTransformIndexes;
        }

        @Override protected void setAfterPlayTransformIndexes(GameProperties properties, int[] transformIndexes) {
            properties.clawPAfterPlayTransformIndexes = transformIndexes;
        }
    }

    // Cold Snap (Common) - 1 energy, Attack
    //   Effect: Deal 6 damage. Channel 1 Frost.
    //   Upgraded Effect: Deal 9 damage. Channel 1 Frost.
    public static class ColdSnap extends CardDefect.ColdSnap {
    }

    public static class ColdSnapP extends CardDefect.ColdSnapP {
    }

    // Compile Driver (Common) - 1 energy, Attack
    //   Effect: Deal 7 damage. Draw 1 card for each unique Orb you have.
    //   Upgraded Effect: Deal 10 damage. Draw 1 card for each unique Orb you have.
    public static class CompileDriver extends CardDefect.CompileDriver {
    }

    public static class CompileDriverP extends CardDefect.CompileDriverP {
    }

    // Coolheaded (Common) - 1 energy, Skill
    //   Effect: Channel 1 Frost. Draw 1 card.
    //   Upgraded Effect: Channel 1 Frost. Draw 2 cards.
    public static class Coolheaded extends CardDefect.Coolheaded {
    }

    public static class CoolheadedP extends CardDefect.CoolheadedP {
    }

    private static abstract class _FocusedStrikeT extends Card {
        private final int damage;
        private final int focus;

        public _FocusedStrikeT(String cardName, int damage, int focus) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.focus = focus;
            entityProperty.selectEnemy = true;
            entityProperty.changePlayerFocus = true;
            entityProperty.changePlayerFocusEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            state.gainFocus(focus);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_FOCUS_EOT, focus);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Focused Strike (Common) - 1 energy, Attack
    //   Effect: Deal 9 damage. Gain 1 Focus this turn.
    //   Upgraded Effect: Deal 11 damage. Gain 2 Focus this turn.
    public static class FocusedStrike extends _FocusedStrikeT {
        public FocusedStrike() {
            super("Focused Strike", 9, 1);
        }
    }

    public static class FocusedStrikeP extends _FocusedStrikeT {
        public FocusedStrikeP() {
            super("Focused Strike+", 11, 2);
        }
    }

    // Go for the Eyes (Common) - 0 energy, Attack
    //   Effect: Deal 3 damage. If the enemy intends to attack, apply 1 Weak.
    //   Upgraded Effect: Deal 4 damage. If the enemy intends to attack, apply 2 Weak.
    public static class GoForTheEyes extends CardDefect.GoForTheEyes {
    }

    public static class GoForTheEyesP extends CardDefect.GoForTheEyesP {
    }

    private static abstract class _GunkUpT extends Card {
        private final int damage;

        public _GunkUpT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage, this);
            state.playerDoDamageToEnemy(enemy, damage, this);
            state.playerDoDamageToEnemy(enemy, damage, this);
            state.addCardToDiscard(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardOther.Slimed());
        }
    }

    // Gunk Up (Common) - 1 energy, Attack
    //   Effect: Deal 4 damage 3 times. Add a Slimed into your Discard Pile.
    //   Upgraded Effect: Deal 5 damage 3 times. Add a Slimed into your Discard Pile.
    public static class GunkUp extends _GunkUpT {
        public GunkUp() {
            super("Gunk Up", 4);
        }
    }

    public static class GunkUpP extends _GunkUpT {
        public GunkUpP() {
            super("Gunk Up+", 5);
        }
    }

    // Hologram (Common) - 1 energy, Skill
    //   Effect: Gain 3 Block. Put a card from your Discard Pile into your Hand. Exhaust.
    //   Upgraded Effect: Gain 5 Block. Put a card from your Discard Pile into your Hand.
    public static class Hologram extends CardDefect.Hologram {
    }

    public static class HologramP extends CardDefect.HologramP {
    }

    private static abstract class _HotfixT extends Card {
        private final int focus;

        public _HotfixT(String cardName, int focus, boolean exhaust) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.focus = focus;
            exhaustWhenPlayed = exhaust;
            entityProperty.changePlayerFocus = true;
            entityProperty.changePlayerFocusEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainFocus(focus);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_FOCUS_EOT, focus);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Hotfix (Common) - 0 energy, Skill
    //   Effect: Gain 2 Focus this turn. Exhaust.
    //   Upgraded Effect: Gain 2 Focus this turn.
    public static class Hotfix extends _HotfixT {
        public Hotfix() {
            super("Hotfix", 2, true);
        }
    }

    public static class HotfixP extends _HotfixT {
        public HotfixP() {
            super("Hotfix+", 2, false);
        }
    }

    // Leap (Common) - 1 energy, Skill
    //   Effect: Gain 9 Block.
    //   Upgraded Effect: Gain 12 Block.
    public static class Leap extends CardDefect.Leap {
    }

    public static class LeapP extends CardDefect.LeapP {
    }

    private static abstract class _LightningRodT extends Card {
        private final int block;

        public _LightningRodT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.block = block;
            entityProperty.orbGenerationPossible |= OrbType.LIGHTNING.mask;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.getCounterForWrite()[counterIdx] += 2;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("LightningRod", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 4.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("LightningRod", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx]--;
                        state.channelOrb(OrbType.LIGHTNING);
                    }
                }
            });
        }
    }

    // Lightning Rod (Common) - 1 energy, Skill
    //   Effect: Gain 4 Block. At the start of the next 2 turns, Channel 1 Lightning.
    //   Upgraded Effect: Gain 7 Block. At the start of the next 2 turns, Channel 1 Lightning.
    public static class LightningRod extends _LightningRodT {
        public LightningRod() {
            super("Lightning Rod", 4);
        }
    }

    public static class LightningRodP extends _LightningRodT {
        public LightningRodP() {
            super("Lightning Rod+", 7);
        }
    }

    private static abstract class _MomentumStrikeT extends Card {
        private final int damage;

        public _MomentumStrikeT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return cards.stream()
                    .filter(c -> c.getBaseCard() instanceof _MomentumStrikeT)
                    .map(c -> c.getPermCostIfPossible(0))
                    .distinct()
                    .toList();
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.momentumStrikeTransformIndexes = new int[state.properties.cardDict.length];
            Arrays.fill(state.properties.momentumStrikeTransformIndexes, -1);
            for (int i = 0; i < state.properties.momentumStrikeTransformIndexes.length; i++) {
                if (state.properties.cardDict[i].getBaseCard() instanceof _MomentumStrikeT
                        && state.properties.cardDict[i].energyCost != 0) {
                    state.properties.momentumStrikeTransformIndexes[i] =
                            state.properties.findCardIndex(state.properties.cardDict[i].getPermCostIfPossible(0));
                }
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop, int cardIdx) {
            return prop.momentumStrikeTransformIndexes[cardIdx];
        }
    }

    // Momentum Strike (Common) - 1 energy, Attack
    //   Effect: Deal 10 damage. Reduce this card's cost to 0 energy.
    //   Upgraded Effect: Deal 13 damage. Reduce this card's cost to 0 energy.
    public static class MomentumStrike extends _MomentumStrikeT {
        public MomentumStrike() {
            super("Momentum Strike", 10);
        }
    }

    public static class MomentumStrikeP extends _MomentumStrikeT {
        public MomentumStrikeP() {
            super("Momentum Strike+", 13);
        }
    }

    // Sweeping Beam (Common) - 1 energy, Attack
    //   Effect: Deal 6 damage to ALL enemies. Draw 1 card.
    //   Upgraded Effect: Deal 9 damage to ALL enemies. Draw 1 card.
    public static class SweepingBeam extends CardDefect.SweepingBeam {
    }

    public static class SweepingBeamP extends CardDefect.SweepingBeamP {
    }

    // TURBO (Common) - 0 energy, Skill
    //   Effect: Gain 2 energy. Add a Void into your Discard Pile.
    //   Upgraded Effect: Gain 3 energy. Add a Void into your Discard Pile.
    public static class Turbo extends CardDefect.Turbo {
    }

    public static class TurboP extends CardDefect.TurboP {
    }

    private static abstract class _UproarT extends Card {
        private final int damage;

        public _UproarT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage, this);
            state.playerDoDamageToEnemy(enemy, damage, this);
            int attackCount = 0;
            for (int i = 0; i < state.deckArrLen; i++) {
                if (state.properties.cardDict[state.deckArr[i]].cardType == Card.ATTACK) {
                    attackCount++;
                }
            }
            if (attackCount > 0) {
                int r = state.getSearchRandomGen().nextInt(attackCount, RandomGenCtx.RandomCardGen, new Tuple<>(state, null));
                int count = 0;
                for (int i = 0; i < state.deckArrLen; i++) {
                    int cardIdx = state.deckArr[i];
                    if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        if (count == r) {
                            state.removeCardFromDeck(cardIdx, false);
                            state.addGameActionToStartOfDeque(new PlayCardAction(cardIdx, state));
                            break;
                        }
                        count++;
                    }
                }
                if (attackCount > 1) {
                    state.setIsStochastic();
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Uproar (Common) - 2 energy, Attack
    //   Effect: Deal 6 damage twice. Play a random Attack from your Draw Pile.
    //   Upgraded Effect: Deal 8 damage twice. Play a random Attack from your Draw Pile.
    public static class Uproar extends _UproarT {
        public Uproar() {
            super("Uproar", 6);
        }
    }

    public static class UproarP extends _UproarT {
        public UproarP() {
            super("Uproar+", 8);
        }
    }

    // **************************************************************************************************
    // ********************************************* Uncommon *********************************************
    // **************************************************************************************************

    // Boot Sequence (Uncommon) - 0 energy, Skill
    //   Effect: Innate. Gain 10 Block. Exhaust.
    //   Upgraded Effect: Innate. Gain 13 Block. Exhaust.
    public static class BootSequence extends CardDefect.BootSequence {
    }

    public static class BootSequenceP extends CardDefect.BootSequenceP {
    }

    private static abstract class _BulkUpT extends Card {
        private final int n;

        public _BulkUpT(String cardName, int n) {
            super(cardName, Card.POWER, 2, Card.UNCOMMON);
            this.n = n;
            entityProperty.changePlayerStrength = true;
            entityProperty.changePlayerDexterity = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainOrbSlot(-1);
            state.getPlayerForWrite().gainStrength(n);
            state.getPlayerForWrite().gainDexterity(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Bulk Up (Uncommon) - 2 energy, Power
    //   Effect: Lose 1 Orb Slot. Gain 2 Strength. Gain 2 Dexterity.
    //   Upgraded Effect: Lose 1 Orb Slot. Gain 3 Strength. Gain 3 Dexterity.
    public static class BulkUp extends _BulkUpT {
        public BulkUp() {
            super("Bulk Up", 2);
        }
    }

    public static class BulkUpP extends _BulkUpT {
        public BulkUpP() {
            super("Bulk Up+", 3);
        }
    }

    // Capacitor (Uncommon) - 1 energy, Power
    //   Effect: Gain 2 Orb Slots.
    //   Upgraded Effect: Gain 3 Orb Slots.
    public static class Capacitor extends CardDefect.Capacitor {
    }

    public static class CapacitorP extends CardDefect.CapacitorP {
    }

    // Chaos (Uncommon) - 1 energy, Skill
    //   Effect: Channel 1 random Orb.
    //   Upgraded Effect: Channel 2 random Orbs.
    public static class Chaos extends CardDefect.Chaos {
    }

    public static class ChaosP extends CardDefect.ChaosP {
    }

    // Chill (Uncommon) - 0 energy, Skill
    //   Effect: Channel 1 Frost for each enemy. Exhaust.
    //   Upgraded Effect: Channel 1 Frost for each enemy.
    public static class Chill extends CardDefect.Chill {
    }

    public static class ChillP extends CardDefect._ChillT {
        public ChillP() {
            super("Chill+", false, false);
        }
    }

    private static abstract class _CompactT extends Card {
        private final int block;

        public _CompactT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            var handArr = state.getHandArrForWrite();
            for (int i = 0; i < state.handArrLen; i++) {
                if (state.properties.cardDict[handArr[i]].cardType == Card.STATUS) {
                    handArr[i] = (short) generatedCardIdx;
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Compact (Uncommon) - 1 energy, Skill
    //   Effect: Gain 6 Block. Transform all Status cards in your Hand into Fuel.
    //   Upgraded Effect: Gain 7 Block. Transform all Status cards in your Hand into Fuel+.
    public static class Compact extends _CompactT {
        public Compact() {
            super("Compact", 6);
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.Fuel());
        }
    }

    public static class CompactP extends _CompactT {
        public CompactP() {
            super("Compact+", 7);
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.FuelP());
        }
    }

    // Darkness (Uncommon) - 1 energy, Skill
    //   Effect: Channel 1 Dark. Trigger the passive ability of all Dark Orbs.
    //   Upgraded Effect: Channel 1 Dark. Trigger the passive ability of all Dark Orbs twice.
    public static class Darkness extends CardDefect._DarknessT {
        public Darkness() {
            super("Darkness", 1);
        }
    }

    public static class DarknessP extends CardDefect._DarknessT {
        public DarknessP() {
            super("Darkness+", 2);
        }
    }

    // Double Energy (Uncommon) - 1 energy, Skill
    //   Effect: Double your Energy. Exhaust.
    //   Upgraded Effect (0 energy): Double your Energy. Exhaust.
    public static class DoubleEnergy extends CardDefect.DoubleEnergy {
    }

    public static class DoubleEnergyP extends CardDefect.DoubleEnergyP {
    }

    // No need to implement Energy Surge: Multiplayer

    // FTL (Uncommon) - 0 energy, Attack
    //   Effect: Deal 5 damage. If you have played fewer than 3 cards this turn, draw 1 card.
    //   Upgraded Effect: Deal 6 damage. If you have played fewer than 4 cards this turn, draw 1 card.
    public static class FTL extends CardDefect.FTL {
    }

    public static class FTLP extends CardDefect.FTLP {
    }

    private static abstract class _FeralT extends Card {
        public _FeralT(String cardName, int energyCost) {
            super(cardName, Card.POWER, energyCost, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            // Upper nibble = stacks (permanent), lower nibble = remaining triggers this turn
            // Add 1 to each: +0x11 = +17
            state.getCounterForWrite()[counterIdx] += 0x11;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Feral", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = (state.getCounterForRead()[counterIdx] >> 4) / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Feral", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int stacks = state.getCounterForRead()[counterIdx] >> 4;
                    state.getCounterForWrite()[counterIdx] = (stacks << 4) | stacks;
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.feralCounterIdx = idx;
        }
    }

    // Feral (Uncommon) - 2 energy, Power
    //   Effect: The first time you play a 0 energy Attack each turn, return it to your Hand.
    //   Upgraded Effect (1 energy): The first time you play a 0 energy Attack each turn, return it to your Hand.
    public static class Feral extends _FeralT {
        public Feral() {
            super("Feral", 2);
        }
    }

    public static class FeralP extends _FeralT {
        public FeralP() {
            super("Feral+", 1);
        }
    }

    private static abstract class _FightThroughT extends Card {
        private final int block;

        public _FightThroughT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.addCardToDiscard(generatedCardIdx);
            state.addCardToDiscard(generatedCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardOther.Wound());
        }
    }

    // Fight Through (Uncommon) - 1 energy, Skill
    //   Effect: Gain 13 Block. Add 2 Wounds into your Discard Pile.
    //   Upgraded Effect: Gain 17 Block. Add 2 Wounds into your Discard Pile.
    public static class FightThrough extends _FightThroughT {
        public FightThrough() {
            super("Fight Through", 13);
        }
    }

    public static class FightThroughP extends _FightThroughT {
        public FightThroughP() {
            super("Fight Through+", 17);
        }
    }

    private static abstract class _FusionT extends Card {
        public _FusionT(String cardName, boolean exhaust) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            exhaustWhenPlayed = exhaust;
            entityProperty.orbGenerationPossible |= OrbType.PLASMA.mask;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.channelOrb(OrbType.PLASMA);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Fusion (Uncommon) - 1 energy, Skill
    //   Effect: Channel 1 Plasma. Exhaust.
    //   Upgraded Effect: Channel 1 Plasma.
    public static class Fusion extends _FusionT {
        public Fusion() {
            super("Fusion", true);
        }
    }

    public static class FusionP extends _FusionT {
        public FusionP() {
            super("Fusion+", false);
        }
    }

    // Glacier (Uncommon) - 2 energy, Skill
    //   Effect: Gain 6 Block. Channel 2 Frost.
    //   Upgraded Effect: Gain 9 Block. Channel 2 Frost.
    public static class Glacier extends CardDefect._GlacierT {
        public Glacier() {
            super("Glacier", 6);
        }
    }

    public static class GlacierP extends CardDefect._GlacierT {
        public GlacierP() {
            super("Glacier+", 9);
        }
    }

    private static abstract class _GlassworkT extends Card {
        private final int block;

        public _GlassworkT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
            entityProperty.orbGenerationPossible |= OrbType.GLASS.mask;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.channelOrb(OrbType.GLASS);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Glasswork (Uncommon) - 1 energy, Skill
    //   Effect: Gain 5 Block. Channel 1 Glass.
    //   Upgraded Effect: Gain 8 Block. Channel 1 Glass.
    public static class Glasswork extends _GlassworkT {
        public Glasswork() {
            super("Glasswork", 5);
        }
    }

    public static class GlassworkP extends _GlassworkT {
        public GlassworkP() {
            super("Glasswork+", 8);
        }
    }

    private static abstract class _HailstormT extends Card {
        private final int damage;

        public _HailstormT(String cardName, int damage) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addEndOfTurnHandler(cardName, new GameEventHandler() {
                @Override public void handle(GameState state) {
                    short[] orbs = state.getOrbs();
                    if (orbs == null) return;
                    for (int i = 0; i < orbs.length; i += 2) {
                        if (orbs[i] == OrbType.FROST.ordinal()) {
                            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                                state.playerDoNonAttackDamageToEnemy(enemy, damage, true);
                            }
                            return;
                        }
                    }
                }
            });
        }
    }

    // Hailstorm (Uncommon) - 1 energy, Power
    //   Effect: At the end of your turn, if you have Frost, deal 6 damage to ALL enemies.
    //   Upgraded Effect: At the end of your turn, if you have Frost, deal 8 damage to ALL enemies.
    public static class Hailstorm extends _HailstormT {
        public Hailstorm() {
            super("Hailstorm", 6);
        }
    }

    public static class HailstormP extends _HailstormT {
        public HailstormP() {
            super("Hailstorm+", 8);
        }
    }

    private static abstract class _IterationT extends Card {
        private final int draws;

        public _IterationT(String cardName, int draws) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.draws = draws;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            // counterIdx = total draws (stacks), counterIdx+1 = triggered-this-turn flag
            state.getCounterForWrite()[counterIdx] += draws;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Iteration", this, 2, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    input[idx + 1] = state.getCounterForRead()[counterIdx + 1];
                    return idx + 2;
                }

                @Override public int getInputLenDelta() {
                    return 2;
                }
            });
            state.properties.addOnCardDrawnHandler("Iteration", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.STATUS
                            && state.getCounterForRead()[counterIdx + 1] == 0
                            && state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx + 1] = 1;
                        state.draw(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
            state.properties.addStartOfTurnHandler("Iteration", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx + 1] = 0;
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.iterationCounterIdx = idx;
        }
    }

    // Iteration (Uncommon) - 1 energy, Power
    //   Effect: The first time you draw a Status card each turn, draw 2 cards.
    //   Upgraded Effect: The first time you draw a Status card each turn, draw 3 cards.
    public static class Iteration extends _IterationT {
        public Iteration() {
            super("Iteration", 2);
        }
    }

    public static class IterationP extends _IterationT {
        public IterationP() {
            super("Iteration+", 3);
        }
    }

    // Loop (Uncommon) - 1 energy, Power
    //   Effect: At the start of your turn, trigger the passive ability of your rightmost Orb.
    //   Upgraded Effect: At the start of your turn, trigger the passive ability of your rightmost Orb 2 times.
    public static class Loop extends CardDefect.Loop {
    }

    public static class LoopP extends CardDefect.LoopP {
    }

    private static abstract class _NullT extends Card {
        private final int damage;
        private final int weak;

        public _NullT(String cardName, int damage, int weak) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            this.weak = weak;
            entityProperty.selectEnemy = true;
            entityProperty.orbGenerationPossible |= OrbType.DARK.mask;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage, this);
            enemy.applyDebuff(state, DebuffType.WEAK, weak);
            state.channelOrb(OrbType.DARK);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Null (Uncommon) - 2 energy, Attack
    //   Effect: Deal 10 damage. Apply 2 Weak. Channel 1 Dark.
    //   Upgraded Effect: Deal 13 damage. Apply 3 Weak. Channel 1 Dark.
    public static class Null extends _NullT {
        public Null() {
            super("Null", 10, 2);
        }
    }

    public static class NullP extends _NullT {
        public NullP() {
            super("Null+", 13, 3);
        }
    }

    // Overclock (Uncommon) - 0 energy, Skill
    //   Effect: Draw 2 cards. Add a Burn into your Discard Pile.
    //   Upgraded Effect: Draw 3 cards. Add a Burn into your Discard Pile.
    public static class Overclock extends CardDefect.Overclock {
    }

    public static class OverclockP extends CardDefect.OverclockP {
    }

    private static abstract class _RefractT extends Card {
        private final int damage;

        public _RefractT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 3, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            entityProperty.orbGenerationPossible |= OrbType.GLASS.mask;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage, this);
            state.playerDoDamageToEnemy(enemy, damage, this);
            state.channelOrb(OrbType.GLASS);
            state.channelOrb(OrbType.GLASS);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Refract (Uncommon) - 3 energy, Attack
    //   Effect: Deal 9 damage twice. Channel 2 Glass.
    //   Upgraded Effect: Deal 12 damage twice. Channel 2 Glass.
    public static class Refract extends _RefractT {
        public Refract() {
            super("Refract", 9);
        }
    }

    public static class RefractP extends _RefractT {
        public RefractP() {
            super("Refract+", 12);
        }
    }

    private static abstract class _RocketPunchT extends Card {
        private final int damage;
        private final int draw;

        public _RocketPunchT(String cardName, int damage, int draw) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            this.draw = draw;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            state.draw(draw);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var list = new ArrayList<Card>();
            for (Card card : cards) {
                if (card.getBaseCard() instanceof _RocketPunchT) {
                    list.add(card.getTemporaryCostUntilPlayedIfPossible(0));
                }
            }
            return list;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var zeroCostIdx = new int[state.properties.cardDict.length];
            Arrays.fill(zeroCostIdx, -1);
            for (int i = 0; i < state.properties.cardDict.length; i++) {
                if (state.properties.cardDict[i].getBaseCard() instanceof _RocketPunchT) {
                    zeroCostIdx[i] = state.properties.findCardIndex(state.properties.cardDict[i].getTemporaryCostUntilPlayedIfPossible(0));
                }
            }
            state.properties.addOnCardCreationHandler("RocketPunch", new OnCardCreationHandler() {
                @Override public void handle(GameState state, int cardIdx) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.STATUS) {
                        for (int i = 0; i < state.handArrLen; i++) {
                            if (zeroCostIdx[state.handArr[i]] >= 0) {
                                state.getHandArrForWrite()[i] = (short) zeroCostIdx[state.handArr[i]];
                            }
                        }
                    }
                }
            });
        }
    }

    // Rocket Punch (Uncommon) - 2 energy, Attack
    //   Effect: Deal 13 damage. Draw 1 card. When a Status card is created, reduce this card's cost to 0 energy until played.
    //   Upgraded Effect: Deal 14 damage. Draw 2 cards. When a Status card is created, reduce this card's cost to 0 energy until played.
    public static class RocketPunch extends _RocketPunchT {
        public RocketPunch() {
            super("Rocket Punch", 13, 1);
        }
    }

    public static class RocketPunchP extends _RocketPunchT {
        public RocketPunchP() {
            super("Rocket Punch+", 14, 2);
        }
    }

    private static abstract class _ScavengeT extends Card {
        private final int energy;

        public _ScavengeT(String cardName, int energy) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.energy = energy;
            entityProperty.selectFromHand = true;
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.exhaustCardFromHand(idx);
            state.getCounterForWrite()[counterIdx] += energy;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerEnergyNextTurnCounter(state, this);
        }
    }

    // Scavenge (Uncommon) - 1 energy, Skill
    //   Effect: Exhaust a card. Next turn, gain 2 energy.
    //   Upgraded Effect: Exhaust a card. Next turn, gain 3 energy.
    public static class Scavenge extends _ScavengeT {
        public Scavenge() {
            super("Scavenge", 2);
        }
    }

    public static class ScavengeP extends _ScavengeT {
        public ScavengeP() {
            super("Scavenge+", 3);
        }
    }

    // Scrape (Uncommon) - 1 energy, Attack
    //   Effect: Deal 7 damage. Draw 4 cards. Discard all cards drawn this way that do not cost 0 energy.
    //   Upgraded Effect: Deal 10 damage. Draw 5 cards. Discard all cards drawn this way that do not cost 0 energy.
    public static class Scrape extends CardDefect.Scrape {
    }

    public static class ScrapeP extends CardDefect.ScrapeP {
    }

    private static abstract class _ShadowShieldT extends Card {
        private final int block;

        public _ShadowShieldT(String cardName, int block) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
            this.block = block;
            entityProperty.orbGenerationPossible |= OrbType.DARK.mask;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.channelOrb(OrbType.DARK);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Shadow Shield (Uncommon) - 2 energy, Skill
    //   Effect: Gain 11 Block. Channel 1 Dark.
    //   Upgraded Effect: Gain 15 Block. Channel 1 Dark.
    public static class ShadowShield extends _ShadowShieldT {
        public ShadowShield() {
            super("Shadow Shield", 11);
        }
    }

    public static class ShadowShieldP extends _ShadowShieldT {
        public ShadowShieldP() {
            super("Shadow Shield+", 15);
        }
    }

    // Skim (Uncommon) - 1 energy, Skill
    //   Effect: Draw 3 cards.
    //   Upgraded Effect: Draw 4 cards.
    public static class Skim extends CardDefect.Skim {
    }

    public static class SkimP extends CardDefect.SkimP {
    }

    private static abstract class _SmokestackT extends Card {
        private final int n;

        public _SmokestackT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Smokestack", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardCreationHandler("Smokestack", new OnCardCreationHandler() {
                @Override public void handle(GameState state, int cardIdx) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.STATUS) {
                        int dmg = state.getCounterForRead()[counterIdx];
                        if (dmg > 0) {
                            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                                state.playerDoNonAttackDamageToEnemy(enemy, dmg, true);
                            }
                        }
                    }
                }
            });
        }
    }

    // Smokestack (Uncommon) - 1 energy, Power
    //   Effect: Whenever you create a Status, deal 5 damage to ALL enemies.
    //   Upgraded Effect: Whenever you create a Status, deal 7 damage to ALL enemies.
    public static class Smokestack extends _SmokestackT {
        public Smokestack() {
            super("Smokestack", 5);
        }
    }

    public static class SmokestackP extends _SmokestackT {
        public SmokestackP() {
            super("Smokestack+", 7);
        }
    }

    // Storm (Uncommon) - 1 energy, Power
    //   Effect: Whenever you play a Power, Channel 1 Lightning.
    //   Upgraded Effect: Whenever you play a Power, Channel 2 Lightning.
    public static class Storm extends CardDefect.Storm {
    }

    public static class StormP extends CardDefect._StormT {
        public StormP() {
            super("Storm+", false, 2);
        }
    }

    private static abstract class _SubroutineT extends Card {
        public _SubroutineT(String cardName, int energyCost) {
            super(cardName, Card.POWER, energyCost, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnCardPlayedHandler("Subroutine", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.POWER) {
                        state.gainEnergy(1);
                    }
                }
            });
        }
    }

    // Subroutine (Uncommon) - 1 energy, Power
    //   Effect: Whenever you play a Power, gain energy.
    //   Upgraded Effect (0 energy): Whenever you play a Power, gain energy.
    public static class Subroutine extends _SubroutineT {
        public Subroutine() {
            super("Subroutine", 1);
        }
    }

    public static class SubroutineP extends _SubroutineT {
        public SubroutineP() {
            super("Subroutine+", 0);
        }
    }

    // Sunder (Uncommon) - 3 energy, Attack
    //   Effect: Deal 24 damage. If this kills an enemy, gain 3 energy.
    //   Upgraded Effect: Deal 32 damage. If this kills an enemy, gain 3 energy.
    public static class Sunder extends CardDefect.Sunder {
    }

    public static class SunderP extends CardDefect.SunderP {
    }

    private static abstract class _SynchronizeT extends Card {
        public _SynchronizeT(String cardName, boolean exhaust) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.exhaustWhenPlayed = exhaust;
            entityProperty.changePlayerFocus = true;
            entityProperty.changePlayerFocusEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            short[] orbs = state.getOrbs();
            int uniqueCount = 0;
            if (orbs != null) {
                int orbMask = 0;
                for (int i = 0; i < orbs.length; i += 2) {
                    if (orbs[i] != 0) {
                        orbMask |= (1 << orbs[i]);
                    }
                }
                uniqueCount = Integer.bitCount(orbMask);
            }
            int focusGain = 2 * uniqueCount;
            if (focusGain > 0) {
                state.gainFocus(focusGain);
                state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_FOCUS_EOT, focusGain);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Synchronize (Uncommon) - 1 energy, Skill
    //   Effect: Gain 2 Focus this turn for each unique Orb you have. Exhaust.
    //   Upgraded Effect: Gain 2 Focus this turn for each unique Orb you have.
    public static class Synchronize extends _SynchronizeT {
        public Synchronize() {
            super("Synchronize", true);
        }
    }

    public static class SynchronizeP extends _SynchronizeT {
        public SynchronizeP() {
            super("Synchronize+", false);
        }
    }

    private static abstract class _SynthesisT extends Card {
        private final int damage;

        public _SynthesisT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Synthesis", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("Synthesis", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.POWER
                            && state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx]--;
                        state.gainEnergy(energyUsed);
                    }
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.synthesisCounterIdx = idx;
        }
    }

    // Synthesis (Uncommon) - 2 energy, Attack
    //   Effect: Deal 14 damage. The next Power you play costs 0 energy.
    //   Upgraded Effect: Deal 20 damage. The next Power you play costs 0 energy.
    public static class Synthesis extends _SynthesisT {
        public Synthesis() {
            super("Synthesis", 14);
        }
    }

    public static class SynthesisP extends _SynthesisT {
        public SynthesisP() {
            super("Synthesis+", 20);
        }
    }

    // Tempest (Uncommon) - X energy, Skill
    //   Effect: Channel X Lightning.
    //   Upgraded Effect: Channel X+1 Lightning.
    public static class Tempest extends CardDefect.Tempest {
    }

    public static class TempestP extends CardDefect.TempestP {
    }

    private static abstract class _TeslaCoilT extends Card {
        private final int damage;
        private final int triggers;

        public _TeslaCoilT(String cardName, int damage, int triggers) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.damage = damage;
            this.triggers = triggers;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            for (int i = 0; i < triggers; i++) {
                state.triggerLightningOrbsAgainstEnemy(idx);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Tesla Coil (Uncommon) - 0 energy, Attack
    //   Effect: Deal 3 damage. Trigger all Lightning against the enemy.
    //   Upgraded Effect: Deal 4 damage. Trigger all Lightning against the enemy twice.
    public static class TeslaCoil extends _TeslaCoilT {
        public TeslaCoil() {
            super("Tesla Coil", 3, 1);
        }
    }

    public static class TeslaCoilP extends _TeslaCoilT {
        public TeslaCoilP() {
            super("Tesla Coil+", 4, 2);
        }
    }

    private static abstract class _ThunderT extends Card {
        private final int damage;

        public _ThunderT(String cardName, int damage) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += damage;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Thunder", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.thunderDamageCounterIdx = idx;
        }
    }

    // Thunder (Uncommon) - 1 energy, Power
    //   Effect: Whenever you Evoke Lightning, deal 6 damage to each enemy hit.
    //   Upgraded Effect: Whenever you Evoke Lightning, deal 8 damage to each enemy hit.
    public static class Thunder extends _ThunderT {
        public Thunder() {
            super("Thunder", 6);
        }
    }

    public static class ThunderP extends _ThunderT {
        public ThunderP() {
            super("Thunder+", 8);
        }
    }

    // White Noise (Uncommon) - 1 energy, Skill
    //   Effect: Add a random Power into your Hand. It's free to play this turn. Exhaust.
    //   Upgraded Effect (0 energy): Add a random Power into your Hand. It's free to play this turn. Exhaust.
    public static class WhiteNoise extends CardDefect.WhiteNoise {
    }

    public static class WhiteNoiseP extends CardDefect.WhiteNoiseP {
    }

    // **************************************************************************************************
    // *********************************************  Rare  *********************************************
    // **************************************************************************************************

    private static abstract class _AdaptiveStrikeT extends Card {
        private final int damage;

        public _AdaptiveStrikeT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.RARE);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
            state.addCardToDiscard(state.createCard(generatedCardIdx));
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(this.getPermCostIfPossible(0));
        }
    }

    // Adaptive Strike (Rare) - 2 energy, Attack
    //   Effect: Deal 18 damage. Add a 0 energy copy of this card into your Discard Pile.
    //   Upgraded Effect: Deal 23 damage. Add a 0 energy copy of this card into your Discard Pile.
    public static class AdaptiveStrike extends _AdaptiveStrikeT {
        public AdaptiveStrike() {
            super("Adaptive Strike", 18);
        }
    }

    public static class AdaptiveStrikeP extends _AdaptiveStrikeT {
        public AdaptiveStrikeP() {
            super("Adaptive Strike+", 23);
        }
    }

    // All for One (Rare) - 2 energy, Attack
    //   Effect: Deal 10 damage. Put ALL 0 energy cards from your Discard Pile into your Hand.
    //   Upgraded Effect: Deal 14 damage. Put ALL 0 energy cards from your Discard Pile into your Hand.
    public static class AllForOne extends CardDefect.AllForOne {
        public AllForOne(int discardOrderMaxKeepTrackIn10s, int discardOrder0CardMaxCopies) {
            super(discardOrderMaxKeepTrackIn10s, discardOrder0CardMaxCopies);
        }
    }

    public static class AllForOneP extends CardDefect.AllForOneP {
        public AllForOneP(int discardOrderMaxKeepTrackIn10s, int discardOrder0CardMaxCopies) {
            super(discardOrderMaxKeepTrackIn10s, discardOrder0CardMaxCopies);
        }
    }

    // Buffer (Rare) - 2 energy, Power
    //   Effect: Prevent the next time you would lose HP.
    //   Upgraded Effect: Prevent the next 2 times you would lose HP.
    public static class Buffer extends CardDefect.Buffer {
    }

    public static class BufferP extends CardDefect.BufferP {
    }

    private static abstract class _ConsumingShadowT extends Card {
        private final int darkOrbs;

        public _ConsumingShadowT(String cardName, int darkOrbs) {
            super(cardName, Card.POWER, 2, Card.RARE);
            this.darkOrbs = darkOrbs;
            entityProperty.orbGenerationPossible |= OrbType.DARK.mask;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < darkOrbs; i++) {
                state.channelOrb(OrbType.DARK);
            }
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("ConsumingShadow", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addEndOfTurnHandler("ConsumingShadow", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int count = state.getCounterForRead()[counterIdx];
                    for (int i = 0; i < count; i++) {
                        state.evokeOrbLeft();
                    }
                }
            });
        }
    }

    // Consuming Shadow (Rare) - 2 energy, Power
    //   Effect: Channel 2 Dark. At the end of your turn, Evoke your leftmost Orb.
    //   Upgraded Effect: Channel 3 Dark. At the end of your turn, Evoke your leftmost Orb.
    public static class ConsumingShadow extends _ConsumingShadowT {
        public ConsumingShadow() {
            super("Consuming Shadow", 2);
        }
    }

    public static class ConsumingShadowP extends _ConsumingShadowT {
        public ConsumingShadowP() {
            super("Consuming Shadow+", 3);
        }
    }

    private static abstract class _CoolantT extends Card {
        private final int blockPerOrb;

        public _CoolantT(String cardName, int blockPerOrb) {
            super(cardName, Card.POWER, 1, Card.RARE);
            this.blockPerOrb = blockPerOrb;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += blockPerOrb;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Coolant", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Coolant", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int n = state.getCounterForRead()[counterIdx];
                    if (n == 0) return;
                    short[] orbs = state.getOrbs();
                    if (orbs == null) return;
                    int orbMask = 0;
                    for (int i = 0; i < orbs.length; i += 2) {
                        if (orbs[i] != 0) {
                            orbMask |= (1 << orbs[i]);
                        }
                    }
                    int uniqueCount = Integer.bitCount(orbMask);
                    if (uniqueCount > 0) {
                        state.playerGainBlock(n * uniqueCount);
                    }
                }
            });
        }
    }

    // Coolant (Rare) - 1 energy, Power
    //   Effect: At the start of your turn, gain 2 Block for each unique Orb you have.
    //   Upgraded Effect: At the start of your turn, gain 3 Block for each unique Orb you have.
    public static class Coolant extends _CoolantT {
        public Coolant() {
            super("Coolant", 2);
        }
    }

    public static class CoolantP extends _CoolantT {
        public CoolantP() {
            super("Coolant+", 3);
        }
    }

    // Creative AI (Rare) - 3 energy, Power
    //   Effect: At the start of your turn, add a random Power into your Hand.
    //   Upgraded Effect (2 energy): At the start of your turn, add a random Power into your Hand.
    public static class CreativeAI extends CardDefect.CreativeAI {
    }

    public static class CreativeAIP extends CardDefect.CreativeAIP {
    }

    // Defragment (Rare) - 1 energy, Power
    //   Effect: Gain 1 Focus.
    //   Upgraded Effect: Gain 2 Focus.
    public static class Defragment extends CardDefect.Defragment {
    }

    public static class DefragmentP extends CardDefect.DefragmentP {
    }

    // Echo Form (Rare) - 3 energy, Power
    //   Effect: Ethereal. The first card you play each turn is played an extra time.
    //   Upgraded Effect: The first card you play each turn is played an extra time.
    public static class EchoForm extends CardDefect.EchoForm {
    }

    public static class EchoFormP extends CardDefect.EchoFormP {
    }

    private static abstract class _FlakCannonT extends Card {
        private final int damage;

        public _FlakCannonT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.RARE);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int count = 0;
            for (int i = state.handArrLen - 1; i >= 0; i--) {
                if (state.properties.cardDict[state.handArr[i]].cardType == Card.STATUS) {
                    state.exhaustCardFromHandByPosition(i, false);
                    count++;
                }
            }
            state.updateHandArr();
            for (int i = state.discardArrLen - 1; i >= 0; i--) {
                int cardIdx = state.getDiscardArrForRead()[i];
                if (state.properties.cardDict[cardIdx].cardType == Card.STATUS) {
                    state.removeCardFromDiscardByPosition(i);
                    state.exhaustedCardHandle(cardIdx, false);
                    count++;
                }
            }
            int j = 0;
            while (j < state.deckArrLen) {
                int cardIdx = state.getDeckArrForRead()[j];
                if (state.properties.cardDict[cardIdx].cardType == Card.STATUS) {
                    state.removeCardFromDeck(cardIdx, false);
                    state.exhaustedCardHandle(cardIdx, false);
                    count++;
                } else {
                    j++;
                }
            }
            for (int i = 0; i < count; i++) {
                int enemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
                if (enemyIdx >= 0) {
                    state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(enemyIdx), damage, true);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Flak Cannon (Rare) - 2 energy, Attack
    //   Effect: Exhaust ALL your Status cards. Deal 8 damage to a random enemy for each card Exhausted.
    //   Upgraded Effect: Exhaust ALL your Status cards. Deal 11 damage to a random enemy for each card Exhausted.
    public static class FlakCannon extends _FlakCannonT {
        public FlakCannon() {
            super("Flak Cannon", 8);
        }
    }

    public static class FlakCannonP extends _FlakCannonT {
        public FlakCannonP() {
            super("Flak Cannon+", 11);
        }
    }

    // Genetic Algorithm (Rare) - 1 energy, Skill
    //   Effect: Gain 1 Block. Permanently increase this card's Block by 3. Exhaust.
    //   Upgraded Effect: Gain 1 Block. Permanently increase this card's Block by 4. Exhaust.
    public static class GeneticAlgorithm extends CardDefect.GeneticAlgorithm {
        public GeneticAlgorithm() {
            super(1);
        }
    }

    public static class GeneticAlgorithmP extends CardDefect.GeneticAlgorithmP {
        public GeneticAlgorithmP() {
            super(1);
        }
    }

    private static abstract class _HelixDrillT extends Card {
        private final int damagePerEnergy;

        public _HelixDrillT(String cardName, int damagePerEnergy) {
            super(cardName, Card.ATTACK, 0, Card.RARE);
            this.damagePerEnergy = damagePerEnergy;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int energySpent = state.getCounterForRead()[counterIdx];
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damagePerEnergy * energySpent, this);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("HelixDrill", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnEnergySpendHandler("HelixDrill", new OnEnergySpendHandler() {
                @Override public void handle(GameState state, int energySpent) {
                    state.getCounterForWrite()[counterIdx] += energySpent;
                }
            });
            state.properties.addStartOfTurnHandler("HelixDrill", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    // Helix Drill (Rare) - 0 energy, Attack
    //   Effect: Deal 3 damage for each energy previously spent this turn.
    //   Upgraded Effect: Deal 5 damage for each energy previously spent this turn.
    public static class HelixDrill extends _HelixDrillT {
        public HelixDrill() {
            super("Helix Drill", 3);
        }
    }

    public static class HelixDrillP extends _HelixDrillT {
        public HelixDrillP() {
            super("Helix Drill+", 5);
        }
    }

    private static abstract class _HyperbeamT extends Card {
        private final int damage;

        public _HyperbeamT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.RARE);
            this.damage = damage;
            entityProperty.changePlayerFocus = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, damage, this);
            }
            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_FOCUS, 3);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Hyperbeam (Rare) - 2 energy, Attack
    //   Effect: Deal 28 damage to ALL enemies. Lose 3 Focus.
    //   Upgraded Effect: Deal 36 damage to ALL enemies. Lose 3 Focus.
    public static class Hyperbeam extends _HyperbeamT {
        public Hyperbeam() {
            super("Hyperbeam", 28);
        }
    }

    public static class HyperbeamP extends _HyperbeamT {
        public HyperbeamP() {
            super("Hyperbeam+", 36);
        }
    }

    private static abstract class _IceLanceT extends Card {
        private final int n;

        public _IceLanceT(String cardName, int n) {
            super(cardName, Card.ATTACK, 3, Card.RARE);
            this.n = n;
            entityProperty.selectEnemy = true;
            entityProperty.orbGenerationPossible |= OrbType.FROST.mask;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), n, this);
            state.channelOrb(OrbType.FROST);
            state.channelOrb(OrbType.FROST);
            state.channelOrb(OrbType.FROST);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Ice Lance (Rare) - 3 energy, Attack
    //   Effect: Deal 19 damage. Channel 3 Frost.
    //   Upgraded Effect: Deal 24 damage. Channel 3 Frost.
    public static class IceLance extends _IceLanceT {
        public IceLance() {
            super("Ice Lance", 19);
        }
    }

    public static class IceLanceP extends _IceLanceT {
        public IceLanceP() {
            super("Ice Lance+", 24);
        }
    }

    // No need to implement Ignition: Multiplayer

    // Machine Learning (Rare) - 1 energy, Power
    //   Effect: At the start of your turn, draw 1 additional card.
    //   Upgraded Effect: Innate. At the start of your turn, draw 1 additional card.
    public static class MachineLearning extends CardDefect.MachineLearning {
    }

    public static class MachineLearningP extends CardDefect.MachineLearningP {
    }

    // Meteor Strike (Rare) - 5 energy, Attack
    //   Effect: Deal 24 damage. Channel 3 Plasma.
    //   Upgraded Effect: Deal 30 damage. Channel 3 Plasma.
    public static class MeteorStrike extends CardDefect.MeteorStrike {
    }

    public static class MeteorStrikeP extends CardDefect.MeteorStrikeP {
    }

    private static abstract class _ModdedT extends Card {
        private final int energyCostVal;
        private final int drawCount;

        public _ModdedT(String cardName, int energyCostVal, int drawCount) {
            super(cardName, Card.SKILL, energyCostVal, Card.RARE);
            this.energyCostVal = energyCostVal;
            this.drawCount = drawCount;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainOrbSlot(1);
            state.draw(drawCount);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var list = new ArrayList<Card>();
            for (int c = energyCostVal + 1; c <= 5; c++) {
                list.add(drawCount == 1 ? new Modded(c) : new ModdedP(c));
            }
            return list;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            if (state.properties.moddedTransformIndexes == null) {
                state.properties.moddedTransformIndexes = new int[state.properties.cardDict.length];
                Arrays.fill(state.properties.moddedTransformIndexes, -1);
                for (int i = 0; i < state.properties.cardDict.length; i++) {
                    var card = state.properties.cardDict[i].getBaseCard();
                    if (card instanceof _ModdedT m && m.energyCostVal < 5) {
                        Card nextCard = m.drawCount == 1 ? new Modded(m.energyCostVal + 1) : new ModdedP(m.energyCostVal + 1);
                        state.properties.moddedTransformIndexes[i] = state.properties.findCardIndex(state.properties.cardDict[i].wrap(nextCard));
                    }
                }
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop, int cardIdx) {
            return prop.moddedTransformIndexes != null ? prop.moddedTransformIndexes[cardIdx] : -1;
        }
    }

    // Modded (Rare) - 0 energy, Skill
    //   Effect: Gain 1 Orb Slot. Draw 1 card. Increase this card's cost by 1.
    //   Upgraded Effect: Gain 1 Orb Slot. Draw 2 cards. Increase this card's cost by 1.
    public static class Modded extends _ModdedT {
        public Modded() {
            this(0);
        }

        public Modded(int cost) {
            super("Modded" + (cost == 0 ? "" : " (" + cost + ")"), cost, 1);
        }
    }

    public static class ModdedP extends _ModdedT {
        public ModdedP() {
            this(0);
        }

        public ModdedP(int cost) {
            super("Modded+" + (cost == 0 ? "" : " (" + cost + ")"), cost, 2);
        }
    }

    // Multi-Cast (Rare) - X energy, Skill
    //   Effect: Evoke your rightmost Orb X times.
    //   Upgraded Effect: Evoke your rightmost Orb X+1 times.
    public static class MultiCast extends CardDefect.MultiCast {
    }

    public static class MultiCastP extends CardDefect.MultiCastP {
    }

    // Rainbow (Rare) - 2 energy, Skill
    //   Effect: Channel 1 Lightning. Channel 1 Frost. Channel 1 Dark. Exhaust.
    //   Upgraded Effect: Channel 1 Lightning. Channel 1 Frost. Channel 1 Dark.
    public static class Rainbow extends CardDefect.Rainbow {
    }

    public static class RainbowP extends CardDefect.RainbowP {
    }

    // Reboot (Rare) - 0 energy, Skill
    //   Effect: Shuffle ALL your cards into your Draw Pile. Draw 4 cards. Exhaust.
    //   Upgraded Effect: Shuffle ALL your cards into your Draw Pile. Draw 6 cards. Exhaust.
    public static class Reboot extends CardDefect.Reboot {
    }

    public static class RebootP extends CardDefect.RebootP {
    }

    private static abstract class _ShatterT extends Card {
        private final int damage;

        public _ShatterT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.RARE);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, damage, this);
            }
            if (state.getOrbs() != null) {
                while (state.getOrbs()[0] != OrbType.EMPTY.ordinal()) {
                    state.evokeOrb(2);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Shatter (Rare) - 1 energy, Attack
    //   Effect: Deal 7 damage to ALL enemies. Evoke all of your Orbs twice.
    //   Upgraded Effect: Deal 11 damage to ALL enemies. Evoke all of your Orbs twice.
    public static class Shatter extends _ShatterT {
        public Shatter() {
            super("Shatter", 7);
        }
    }

    public static class ShatterP extends _ShatterT {
        public ShatterP() {
            super("Shatter+", 11);
        }
    }

    private static abstract class _SignalBoostT extends Card {
        public _SignalBoostT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.RARE);
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("SignalBoost", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = Math.abs(state.getCounterForRead()[counterIdx]) / 4.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("SignalBoost", new GameEventCardHandler(GameEventCardHandler.CLONE_CARD_PRIORITY) {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    var card = state.properties.cardDict[cardIdx];
                    if (card.cardType != Card.POWER || state.getCounterForRead()[counterIdx] == 0) {
                        return;
                    }
                    if (cloneSource != null && (state.getCounterForRead()[counterIdx] & (1 << 8)) > 0) {
                        state.getCounterForWrite()[counterIdx] ^= 1 << 8;
                    } else {
                        var counters = state.getCounterForWrite();
                        counters[counterIdx]--;
                        counters[counterIdx] |= 1 << 8;
                        state.addGameActionToEndOfDeque(curState -> {
                            var action = curState.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                            if (curState.playCard(action, lastIdx, true, SignalBoost.class, false, false, energyUsed, cloneParentLocation)) {
                            } else {
                                curState.getCounterForWrite()[counterIdx] ^= 1 << 8;
                            }
                        });
                    }
                }
            });
        }
    }

    // Signal Boost (Rare) - 1 energy, Skill
    //   Effect: The next Power you play is played an additional time. Exhaust.
    //   Upgraded Effect (0 energy): The next Power you play is played an additional time. Exhaust.
    public static class SignalBoost extends _SignalBoostT {
        public SignalBoost() {
            super("Signal Boost", 1);
        }
    }

    public static class SignalBoostP extends _SignalBoostT {
        public SignalBoostP() {
            super("Signal Boost+", 0);
        }
    }

    private static abstract class _SpinnerT extends Card {
        private final boolean channelOnPlay;

        public _SpinnerT(String cardName, boolean channelOnPlay) {
            super(cardName, Card.POWER, 1, Card.RARE);
            this.channelOnPlay = channelOnPlay;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (channelOnPlay) {
                state.channelOrb(OrbType.GLASS);
            }
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Spinner", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Spinner", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int stacks = state.getCounterForRead()[counterIdx];
                    for (int i = 0; i < stacks; i++) {
                        state.channelOrb(OrbType.GLASS);
                    }
                }
            });
        }
    }

    // Spinner (Rare) - 1 energy, Power
    //   Effect: At the start of your turn, Channel 1 Glass.
    //   Upgraded Effect: Channel 1 Glass. At the start of your turn, Channel 1 Glass.
    public static class Spinner extends _SpinnerT {
        public Spinner() {
            super("Spinner", false);
        }
    }

    public static class SpinnerP extends _SpinnerT {
        public SpinnerP() {
            super("Spinner+", true);
        }
    }

    private static abstract class _SupercriticalT extends Card {
        private final int n;

        public _SupercriticalT(String cardName, int n) {
            super(cardName, Card.SKILL, 0, Card.RARE);
            this.n = n;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainEnergy(n);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Supercritical (Rare) - 0 energy, Skill
    //   Effect: Gain 4 energy. Exhaust.
    //   Upgraded Effect: Gain 6 energy. Exhaust.
    public static class Supercritical extends _SupercriticalT {
        public Supercritical() {
            super("Supercritical", 4);
        }
    }

    public static class SupercriticalP extends _SupercriticalT {
        public SupercriticalP() {
            super("Supercritical+", 6);
        }
    }

    private static abstract class _TrashToTreasureT extends Card {
        public _TrashToTreasureT(String cardName) {
            super(cardName, Card.POWER, 1, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("TrashToTreasure", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardCreationHandler("TrashToTreasure", new OnCardCreationHandler() {
                @Override public void handle(GameState state, int cardIdx) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.STATUS) {
                        int stacks = state.getCounterForRead()[counterIdx];
                        for (int i = 0; i < stacks; i++) {
                            state.channelOrb(OrbType.getRandom(state));
                        }
                    }
                }
            });
        }
    }

    // Trash to Treasure (Rare) - 1 energy, Power
    //   Effect: Whenever you create a Status card, Channel 1 random Orb.
    //   Upgraded Effect: Innate. Whenever you create a Status card, Channel 1 random Orb.
    public static class TrashToTreasure extends _TrashToTreasureT {
        public TrashToTreasure() {
            super("Trash to Treasure");
        }
    }

    public static class TrashToTreasureP extends _TrashToTreasureT {
        public TrashToTreasureP() {
            super("Trash to Treasure+");
        }
    }

    private static abstract class _VoltaicT extends Card {
        public _VoltaicT(String cardName, boolean exhaust) {
            super(cardName, Card.SKILL, 3, Card.RARE);
            this.exhaustWhenPlayed = exhaust;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int count = state.getCounterForRead()[counterIdx];
            for (int i = 0; i < count; i++) {
                state.channelOrb(OrbType.LIGHTNING);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerLightningChanneledCounter(this);
        }
    }

    // Voltaic (Rare) - 3 energy, Skill
    //   Effect: Channel Lightning equal to the Lightning already Channeled this combat. Exhaust.
    //   Upgraded Effect: Channel Lightning equal to the Lightning already Channeled this combat.
    public static class Voltaic extends _VoltaicT {
        public Voltaic() {
            super("Voltaic", true);
        }
    }

    public static class VoltaicP extends _VoltaicT {
        public VoltaicP() {
            super("Voltaic+", false);
        }
    }

    // **************************************************************************************************
    // ********************************************* Event  *********************************************
    // **************************************************************************************************

    // Rip and Tear (Event) - 1 energy, Attack
    //   Effect: Deal 7 damage to a random enemy twice.
    //   Upgraded Effect: Deal 9 damage to a random enemy twice.
    public static class RipAndTear extends CardDefect.RipAndTear {
    }

    public static class RipAndTearP extends CardDefect.RipAndTearP {
    }

    // Hello World (Event) - 1 energy, Power
    //   Effect: At the start of your turn, add a random Common card into your Hand.
    //   Upgraded Effect: Innate. At the start of your turn, add a random Common card into your Hand.
    public static class HelloWorld extends CardDefect.HelloWorld {
    }

    public static class HelloWorldP extends CardDefect.HelloWorldP {
    }

    // Rebound (Event) - 1 energy, Attack
    //   Effect: Deal 9 damage. Put the next card you play this turn on top of your Draw Pile.
    //   Upgraded Effect: Deal 12 damage. Put the next card you play this turn on top of your Draw Pile.
    public static class Rebound extends CardDefect.Rebound {
    }

    public static class ReboundP extends CardDefect.ReboundP {
    }

    // Stack (Event) - 1 energy, Skill
    //   Effect: Gain Block equal to the number of cards in your Discard Pile.
    //   Upgraded Effect: Gain Block equal to the number of cards in your Discard Pile +3.
    public static class Stack extends CardDefect.Stack {
    }

    public static class StackP extends CardDefect.StackP {
    }

    // **************************************************************************************************
    // ********************************************* Ancient *********************************************
    // **************************************************************************************************

    // Biased Cognition (Ancient) - 1 energy, Power
    //   Effect: Gain 4 Focus. At the start of your turn, lose 1 Focus.
    //   Upgraded Effect: Gain 5 Focus. At the start of your turn, lose 1 Focus.
    public static class BiasedCognition extends CardDefect.BiasedCognition {
    }

    public static class BiasedCognitionP extends CardDefect.BiasedCognitionP {
    }

    private static abstract class _QuadcastT extends Card {
        public _QuadcastT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.evokeOrb(4);
            return GameActionCtx.PLAY_CARD;
        }
    }

    // Quadcast (Ancient) - 1 energy, Skill
    //   Effect: Evoke your rightmost Orb 4 times.
    //   Upgraded Effect (0 energy): Evoke your rightmost Orb 4 times.
    public static class Quadcast extends _QuadcastT {
        public Quadcast() {
            super("Quadcast", 1);
        }
    }

    public static class QuadcastP extends _QuadcastT {
        public QuadcastP() {
            super("Quadcast+", 0);
        }
    }
}
