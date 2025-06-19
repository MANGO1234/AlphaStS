package com.alphaStS.card;

import com.alphaStS.DebuffType;
import com.alphaStS.GameActionCtx;
import com.alphaStS.GameEventCardHandler;
import com.alphaStS.GameEventHandler;
import com.alphaStS.GameProperties;
import com.alphaStS.GameState;
import com.alphaStS.GameStateUtils;
import com.alphaStS.PlayerBuff;
import com.alphaStS.RandomGenCtx;
import com.alphaStS.TrainingTarget;
import com.alphaStS.enums.Stance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CardWatcher {
    private static abstract class _EruptionT extends Card {
        public _EruptionT(String cardName, int energyCost) {
            super(cardName, Card.ATTACK, energyCost, Card.COMMON);
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 9, true);
            state.changeStance(Stance.WRATH);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Eruption extends _EruptionT {
        public Eruption() {
            super("Eruption", 2);
        }
    }

    public static class EruptionP extends _EruptionT {
        public EruptionP() {
            super("Eruption+", 1);
        }
    }

    private static abstract class _VigilanceT extends Card {
        private final int block;

        public _VigilanceT(String cardName, int block) {
            super(cardName, Card.SKILL, 2, Card.COMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            state.changeStance(Stance.CALM);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Vigilance extends _VigilanceT {
        public Vigilance() {
            super("Vigilance", 8);
        }
    }

    public static class VigilanceP extends _VigilanceT {
        public VigilanceP() {
            super("Vigilance+", 12);
        }
    }

    private static abstract class _BowlingBashT extends Card {
        private final int damage;

        public _BowlingBashT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int enemyCount = 0;
            for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                if (state.getEnemiesForRead().get(i).isAlive()) {
                    enemyCount++;
                }
            }
            for (int i = 0; i < enemyCount; i++) {
                state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BowlingBash extends _BowlingBashT {
        public BowlingBash() {
            super("Bowling Bash", 7);
        }
    }

    public static class BowlingBashP extends _BowlingBashT {
        public BowlingBashP() {
            super("Bowling Bash+", 10);
        }
    }

    private static abstract class _ConsecrateT extends Card {
        private final int damage;

        public _ConsecrateT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.COMMON);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                if (state.getEnemiesForRead().get(i).isAlive()) {
                    state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(i), damage, true);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Consecrate extends _ConsecrateT {
        public Consecrate() {
            super("Consecrate", 5);
        }
    }

    public static class ConsecrateP extends _ConsecrateT {
        public ConsecrateP() {
            super("Consecrate+", 8);
        }
    }

    public static abstract class _CrescendoT extends Card {
        public _CrescendoT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.COMMON);
            this.retain = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.changeStance(Stance.WRATH);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Crescendo extends _CrescendoT {
        public Crescendo() {
            super("Crescendo", 1);
        }
    }

    public static class CrescendoP extends _CrescendoT {
        public CrescendoP() {
            super("Crescendo+", 0);
        }
    }

    private static abstract class _CrushJointsT extends Card {
        private final int damage;
        private final int vulnerable;

        public _CrushJointsT(String cardName, int damage, int vulnerable) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.vulnerable = vulnerable;
            this.selectEnemy = true;
            this.needsLastCardType = true;
            this.vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
            if (state.getLastCardPlayedType() == Card.SKILL) {
                state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.VULNERABLE, vulnerable);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class CrushJoints extends _CrushJointsT {
        public CrushJoints() {
            super("Crush Joints", 8, 1);
        }
    }

    public static class CrushJointsP extends _CrushJointsT {
        public CrushJointsP() {
            super("Crush Joints+", 10, 2);
        }
    }

    public static abstract class _CutThroughFateT extends Card {
        private final int damage;
        private final int scryAmount;

        public _CutThroughFateT(String cardName, int damage, int scryAmount) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.scryAmount = scryAmount;
            this.selectEnemy = true;
            this.scry = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
                return state.startScry(scryAmount);
            } else {
                state.draw(1);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class CutThroughFate extends _CutThroughFateT {
        public CutThroughFate() {
            super("Cut Through Fate", 7, 2);
        }
    }

    public static class CutThroughFateP extends _CutThroughFateT {
        public CutThroughFateP() {
            super("Cut Through Fate+", 9, 3);
        }
    }

    private static abstract class _EmptyBodyT extends Card {
        private final int block;

        public _EmptyBodyT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            state.changeStance(Stance.NEUTRAL);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class EmptyBody extends _EmptyBodyT {
        public EmptyBody() {
            super("Empty Body", 7);
        }
    }

    public static class EmptyBodyP extends _EmptyBodyT {
        public EmptyBodyP() {
            super("Empty Body+", 10);
        }
    }

    private static abstract class _EmptyFistT extends Card {
        private final int damage;

        public _EmptyFistT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
            state.changeStance(Stance.NEUTRAL);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class EmptyFist extends _EmptyFistT {
        public EmptyFist() {
            super("Empty Fist", 9);
        }
    }

    public static class EmptyFistP extends _EmptyFistT {
        public EmptyFistP() {
            super("Empty Fist+", 14);
        }
    }

    private static abstract class _EvaluateT extends Card {
        private final int block;

        public _EvaluateT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            state.addCardToDeck(state.properties.insightCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Insight());
        }
    }

    public static class Evaluate extends _EvaluateT {
        public Evaluate() {
            super("Evaluate", 6);
        }
    }

    public static class EvaluateP extends _EvaluateT {
        public EvaluateP() {
            super("Evaluate+", 10);
        }
    }

    public static abstract class _FlurryOfBlowsT extends Card {
        private final int damage;

        public _FlurryOfBlowsT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.COMMON);
            this.damage = damage;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnStanceChangeHandler("FlurryOfBlows", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    // Move all Flurry of Blows cards from discard to hand
                    for (int i = 0; i < state.discardArrLen && state.handArrLen < GameState.HAND_LIMIT; i++) {
                        int cardIdx = state.getDiscardArrForRead()[i];
                        if (state.properties.cardDict[cardIdx].cardName.startsWith("Flurry of Blows")) {
                            state.removeCardFromDiscardByPosition(i);
                            i--;
                            state.addCardToHand(cardIdx);
                        }
                    }
                }
            });
        }
    }

    public static class FlurryOfBlows extends _FlurryOfBlowsT {
        public FlurryOfBlows() {
            super("Flurry of Blows", 4);
        }
    }

    public static class FlurryOfBlowsP extends _FlurryOfBlowsT {
        public FlurryOfBlowsP() {
            super("Flurry of Blows+", 6);
        }
    }

    public static abstract class _FlyingSleevesT extends Card {
        private final int damage;

        public _FlyingSleevesT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.retain = true;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FlyingSleeves extends _FlyingSleevesT {
        public FlyingSleeves() {
            super("Flying Sleeves", 4);
        }
    }

    public static class FlyingSleevesP extends _FlyingSleevesT {
        public FlyingSleevesP() {
            super("Flying Sleeves+", 6);
        }
    }

    private static abstract class _FollowUpT extends Card {
        private final int damage;

        public _FollowUpT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.selectEnemy = true;
            this.needsLastCardType = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
            if (state.getLastCardPlayedType() == Card.ATTACK) {
                state.gainEnergy(1);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FollowUp extends _FollowUpT {
        public FollowUp() {
            super("Follow-Up", 7);
        }
    }

    public static class FollowUpP extends _FollowUpT {
        public FollowUpP() {
            super("Follow-Up+", 11);
        }
    }

    public static abstract class _HaltT extends Card {
        private final int block;
        private final int wrathBonus;

        public _HaltT(String cardName, int block, int wrathBonus) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.block = block;
            this.wrathBonus = wrathBonus;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int totalBlock = block;
            if (state.getStance() == Stance.WRATH) {
                totalBlock += wrathBonus;
            }
            state.getPlayerForWrite().gainBlock(totalBlock);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Halt extends _HaltT {
        public Halt() {
            super("Halt", 3, 9);
        }
    }

    public static class HaltP extends _HaltT {
        public HaltP() {
            super("Halt+", 4, 14);
        }
    }

    public static abstract class _JustLuckyT extends Card {
        private final int scryAmount;
        private final int block;
        private final int damage;

        public _JustLuckyT(String cardName, int scryAmount, int block, int damage) {
            super(cardName, Card.ATTACK, 0, Card.COMMON);
            this.scryAmount = scryAmount;
            this.block = block;
            this.damage = damage;
            this.selectEnemy = true;
            this.scry = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                state.getPlayerForWrite().gainBlock(block);
                state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
                return state.startScry(scryAmount);
            } else {
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class JustLucky extends _JustLuckyT {
        public JustLucky() {
            super("Just Lucky", 1, 2, 3);
        }
    }

    public static class JustLuckyP extends _JustLuckyT {
        public JustLuckyP() {
            super("Just Lucky+", 2, 3, 4);
        }
    }

    private static abstract class _PressurePointsT extends Card {
        private final int mark;

        public _PressurePointsT(String cardName, int mark) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.mark = mark;
            this.selectEnemy = true;
            this.markEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.MARK, mark);
            for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                var enemy = state.getEnemiesForRead().get(i);
                if (enemy.isAlive() && enemy.getMark() > 0) {
                    // Apply mark damage as non-attack damage (bypasses block)
                    state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(i), enemy.getMark(), false);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PressurePoints extends _PressurePointsT {
        public PressurePoints() {
            super("Pressure Points", 8);
        }
    }

    public static class PressurePointsP extends _PressurePointsT {
        public PressurePointsP() {
            super("Pressure Points+", 11);
        }
    }

    public static abstract class _ProstrateT extends Card {
        private final int mantra;
        private final int block;

        public _ProstrateT(String cardName, int mantra, int block) {
            super(cardName, Card.SKILL, 0, Card.COMMON);
            this.mantra = mantra;
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            state.gainMantra(mantra);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerMantraCounter();
        }
    }

    public static class Prostrate extends _ProstrateT {
        public Prostrate() {
            super("Prostrate", 2, 3);
        }
    }

    public static class ProstrateP extends _ProstrateT {
        public ProstrateP() {
            super("Prostrate+", 3, 4);
        }
    }

    public static abstract class _ProtectT extends Card {
        private final int block;

        public _ProtectT(String cardName, int block) {
            super(cardName, Card.SKILL, 2, Card.COMMON);
            this.block = block;
            this.retain = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Protect extends _ProtectT {
        public Protect() {
            super("Protect", 12);
        }
    }

    public static class ProtectP extends _ProtectT {
        public ProtectP() {
            super("Protect+", 16);
        }
    }

    public static abstract class _SashWhipT extends Card {
        private final int damage;
        private final int weak;

        public _SashWhipT(String cardName, int damage, int weak) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.weak = weak;
            this.weakEnemy = true;
            this.selectEnemy = true;
            this.needsLastCardType = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
            if (state.getLastCardPlayedType() == Card.ATTACK) {
                state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.WEAK, weak);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SashWhip extends _SashWhipT {
        public SashWhip() {
            super("Sash Whip", 8, 1);
        }
    }

    public static class SashWhipP extends _SashWhipT {
        public SashWhipP() {
            super("Sash Whip+", 10, 2);
        }
    }

    public static abstract class _ThirdEyeT extends Card {
        private final int scryAmount;
        private final int block;

        public _ThirdEyeT(String cardName, int scryAmount, int block) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.scryAmount = scryAmount;
            this.block = block;
            this.scry = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.getPlayerForWrite().gainBlock(block);
                return state.startScry(scryAmount);
            } else {
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class ThirdEye extends _ThirdEyeT {
        public ThirdEye() {
            super("Third Eye", 3, 7);
        }
    }

    public static class ThirdEyeP extends _ThirdEyeT {
        public ThirdEyeP() {
            super("Third Eye+", 5, 9);
        }
    }

    public static abstract class _TranquilityT extends Card {
        public _TranquilityT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.COMMON);
            this.retain = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.changeStance(Stance.CALM);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Tranquility extends _TranquilityT {
        public Tranquility() {
            super("Tranquility", 1);
        }
    }

    public static class TranquilityP extends _TranquilityT {
        public TranquilityP() {
            super("Tranquility+", 0);
        }
    }

    private static abstract class _BattleHymnT extends Card {
        public _BattleHymnT(String cardName, boolean innate) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.innate = innate;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("BattleHymn", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("BattleHymn", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (counterIdx >= 0 && state.getCounterForRead()[counterIdx] > 0) {
                        for (int i = 0; i < state.getCounterForRead()[counterIdx]; i++) {
                            state.addCardToHand(state.properties.smiteCardIdx);
                        }
                    }
                }
            });
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Smite());
        }
    }

    public static class BattleHymn extends _BattleHymnT {
        public BattleHymn() {
            super("Battle Hymn", false);
        }
    }

    public static class BattleHymnP extends _BattleHymnT {
        public BattleHymnP() {
            super("Battle Hymn+", true);
        }
    }

    private static abstract class _CarveRealityT extends Card {
        private final int damage;

        public _CarveRealityT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
            state.addCardToHand(state.properties.smiteCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Smite());
        }
    }

    public static class CarveReality extends _CarveRealityT {
        public CarveReality() {
            super("Carve Reality", 6);
        }
    }

    public static class CarveRealityP extends _CarveRealityT {
        public CarveRealityP() {
            super("Carve Reality+", 10);
        }
    }

    public static abstract class _CollectT extends Card {
        private final int bonus;

        public _CollectT(String cardName, int bonus) {
            super(cardName, Card.SKILL, -1, Card.UNCOMMON);
            this.bonus = bonus;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] = energyUsed + bonus;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Collect", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Collect", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.addCardToHand(state.properties.miraclePCardIdx);
                        state.getCounterForWrite()[counterIdx]--;
                    }
                }
            });
        }
    }

    public static class Collect extends _CollectT {
        public Collect() {
            super("Collect", 0);
        }
    }

    public static class CollectP extends _CollectT {
        public CollectP() {
            super("Collect+", 1);
        }
    }

    private static abstract class _ConcludeT extends Card {
        private final int damage;

        public _ConcludeT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < state.getEnemiesForRead().size(); i++) {
                if (state.getEnemiesForRead().get(i).isAlive()) {
                    state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(i), damage, true);
                }
            }
            state.buffs |= PlayerBuff.END_TURN_IMMEDIATELY.mask();
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Conclude extends _ConcludeT {
        public Conclude() {
            super("Conclude", 12);
        }
    }

    public static class ConcludeP extends _ConcludeT {
        public ConcludeP() {
            super("Conclude+", 16);
        }
    }

    public static abstract class _DeceiveRealityT extends Card {
        private final int block;

        public _DeceiveRealityT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            state.addCardToHand(state.properties.safetyCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Safety());
        }
    }

    public static class DeceiveReality extends _DeceiveRealityT {
        public DeceiveReality() {
            super("Deceive Reality", 4);
        }
    }

    public static class DeceiveRealityP extends _DeceiveRealityT {
        public DeceiveRealityP() {
            super("Deceive Reality+", 7);
        }
    }

    public static abstract class _EmptyMindT extends Card {
        private final int drawCount;

        public _EmptyMindT(String cardName, int drawCount) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.drawCount = drawCount;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.changeStance(Stance.NEUTRAL);
            state.draw(drawCount);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class EmptyMind extends _EmptyMindT {
        public EmptyMind() {
            super("Empty Mind", 2);
        }
    }

    public static class EmptyMindP extends _EmptyMindT {
        public EmptyMindP() {
            super("Empty Mind+", 3);
        }
    }

    public static abstract class _FastingT extends Card {
        private final int statBonus;

        public _FastingT(String cardName, int statBonus) {
            super(cardName, Card.POWER, 2, Card.UNCOMMON);
            this.statBonus = statBonus;
            this.changePlayerStrength = true;
            this.changePlayerDexterity = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainStrength(statBonus);
            state.getPlayerForWrite().gainDexterity(statBonus);
            state.getPlayerForWrite().applyDebuff(state, DebuffType.LOSE_ENERGY_PER_TURN, 1);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("FastingLoseEnergy", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Fasting", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.gainEnergy(-state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.loseEnergyPerTurnCounterIdx = idx;
        }
    }

    public static class Fasting extends _FastingT {
        public Fasting() {
            super("Fasting", 3);
        }
    }

    public static class FastingP extends _FastingT {
        public FastingP() {
            super("Fasting+", 4);
        }
    }

    public static abstract class _FearNoEvilT extends Card {
        private final int damage;

        public _FearNoEvilT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
            if (state.getEnemiesForWrite().get(idx).getMoveString(state).contains("Attack")) {
                state.changeStance(Stance.CALM);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FearNoEvil extends _FearNoEvilT {
        public FearNoEvil() {
            super("Fear No Evil", 8);
        }
    }

    public static class FearNoEvilP extends _FearNoEvilT {
        public FearNoEvilP() {
            super("Fear No Evil+", 11);
        }
    }

    public static abstract class _ForeignInfluenceT extends Card {
        public _ForeignInfluenceT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.UNCOMMON);
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            // TODO: Implement effect - Choose 1 of 3 Attacks of any color to add to hand, costs 0 this turn
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ForeignInfluence extends _ForeignInfluenceT {
        public ForeignInfluence() {
            super("Foreign Influence", 1);
        }
    }

    public static class ForeignInfluenceP extends _ForeignInfluenceT {
        public ForeignInfluenceP() {
            super("Foreign Influence+", 0);
        }
    }

    public static abstract class _ForesightT extends Card {
        private final int scryAmount;

        public _ForesightT(String cardName, int scryAmount) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.scryAmount = scryAmount;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.getCounterForWrite()[counterIdx] += scryAmount;
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Foresight", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public void onRegister(int counterIdx) {
                    state.properties.foresightCounterIdx = counterIdx;
                }
            });
        }
    }

    public static class Foresight extends _ForesightT {
        public Foresight() {
            super("Foresight", 3);
        }
    }

    public static class ForesightP extends _ForesightT {
        public ForesightP() {
            super("Foresight+", 4);
        }
    }

    public static abstract class _IndignationT extends Card {
        private final int vulnerable;

        public _IndignationT(String cardName, int vulnerable) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.vulnerable = vulnerable;
            this.vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.getStance() == Stance.WRATH) {
                for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                    enemy.applyDebuff(state, DebuffType.VULNERABLE, vulnerable);
                }
            } else {
                state.changeStance(Stance.WRATH);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Indignation extends _IndignationT {
        public Indignation() {
            super("Indignation", 3);
        }
    }

    public static class IndignationP extends _IndignationT {
        public IndignationP() {
            super("Indignation+", 5);
        }
    }

    public static abstract class _InnerPeaceT extends Card {
        private final int drawCount;

        public _InnerPeaceT(String cardName, int drawCount) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.drawCount = drawCount;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.getStance() == Stance.CALM) {
                state.draw(drawCount);
            } else {
                state.changeStance(Stance.CALM);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class InnerPeace extends _InnerPeaceT {
        public InnerPeace() {
            super("Inner Peace", 3);
        }
    }

    public static class InnerPeaceP extends _InnerPeaceT {
        public InnerPeaceP() {
            super("Inner Peace+", 4);
        }
    }

    public static abstract class _LikeWaterT extends Card {
        private final int block;

        public _LikeWaterT(String cardName, int block) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += block;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("LikeWater", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addEndOfTurnHandler("LikeWater", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (counterIdx >= 0 && state.getCounterForRead()[counterIdx] > 0 && state.getStance() == Stance.CALM) {
                        state.getPlayerForWrite().gainBlock(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }
    }

    public static class LikeWater extends _LikeWaterT {
        public LikeWater() {
            super("Like Water", 5);
        }
    }

    public static class LikeWaterP extends _LikeWaterT {
        public LikeWaterP() {
            super("Like Water+", 7);
        }
    }

    public static abstract class _MeditateT extends Card {
        private final int cardCount;

        public _MeditateT(String cardName, int energyCost, int cardCount) {
            super(cardName, Card.SKILL, energyCost, Card.UNCOMMON);
            this.cardCount = cardCount;
            this.selectFromDiscard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            // Remove card from discard and add to hand with temporary retain if there's room
            state.removeCardFromDiscard(idx);
            if (state.handArrLen < GameState.HAND_LIMIT) {
                Card retainedCard = new Card.CardTmpRetain(state.properties.cardDict[idx]);
                int retainedCardIdx = state.properties.findCardIndex(retainedCard);
                state.addCardToHand(retainedCardIdx);
            } else {
                // No room in hand, just add the original card
                state.addCardToHand(idx);
            }

            // Check if we need to select more cards
            state.getCounterForWrite()[counterIdx]++;
            if (state.getCounterForWrite()[counterIdx] < cardCount) {
                return GameActionCtx.SELECT_CARD_DISCARD;
            } else {
                // Reset counter, enter Calm, and end turn
                state.getCounterForWrite()[counterIdx] = 0;
                state.changeStance(Stance.CALM);
                state.buffs |= PlayerBuff.END_TURN_IMMEDIATELY.mask();
                return GameActionCtx.PLAY_CARD;
            }
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Meditate", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / (float) cardCount;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var generatedCards = new ArrayList<Card>();
            // Meditate can potentially return any card from discard with temporary retain
            for (Card card : cards) {
                generatedCards.add(card.getTmpRetainIfPossible());
            }
            return generatedCards;
        }
    }

    public static class Meditate extends _MeditateT {
        public Meditate() {
            super("Meditate", 1, 1);
        }
    }

    public static class MeditateP extends _MeditateT {
        public MeditateP() {
            super("Meditate+", 1, 2);
        }
    }

    private static abstract class _MentalFortressT extends Card {
        private final int block;

        public _MentalFortressT(String cardName, int block) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += block;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("MentalFortress", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnStanceChangeHandler("MentalFortress", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getPlayerForWrite().gainBlock(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }
    }

    public static class MentalFortress extends _MentalFortressT {
        public MentalFortress() {
            super("Mental Fortress", 4);
        }
    }

    public static class MentalFortressP extends _MentalFortressT {
        public MentalFortressP() {
            super("Mental Fortress+", 6);
        }
    }

    public static abstract class _NirvanaT extends Card {
        private final int block;

        public _NirvanaT(String cardName, int block) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += block;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Nirvana", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnScryHandler("Nirvana", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getPlayerForWrite().gainBlock(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }
    }

    public static class Nirvana extends _NirvanaT {
        public Nirvana() {
            super("Nirvana", 3);
        }
    }

    public static class NirvanaP extends _NirvanaT {
        public NirvanaP() {
            super("Nirvana+", 4);
        }
    }

    public static class Perseverance extends Card {
        public int limit;
        private int block;

        public Perseverance() {
            this(5);
        }

        public Perseverance(int block, int limit) {
            super("Perseverance (" + block + ")", Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
            this.limit = limit;
            this.retain = true;
        }

        public Perseverance(int block) {
            this(block, 25);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = block; i <= limit; i += 2) {
                c.add(new Perseverance(i, limit));
            }
            return c;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            if (state.properties.perseveranceTransformIndexes != null && state.properties.perseveranceTransformIndexes.length > (limit - block) / 2 + 1) {
                return;
            }
            state.properties.perseveranceTransformIndexes = new int[(limit - block) / 2 + 1];
            for (int i = block; i <= limit; i += 2) {
                state.properties.perseveranceTransformIndexes[(i - block) / 2] = state.properties.findCardIndex(new Perseverance(i, limit));
            }
            state.properties.addOnRetainHandler("Perseverance", new GameEventCardHandler() {
                @Override public void handle(GameState state, int handIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int cardIdx = state.getHandArrForRead()[handIdx];
                    if (state.properties.cardDict[cardIdx] instanceof Perseverance p) {
                        int newBlock = Math.min(p.block + 2, p.limit);
                        int newCardIdx = -1;
                        for (int i = 0; i < state.properties.perseveranceTransformIndexes.length; i++) {
                            if (state.properties.cardDict[state.properties.perseveranceTransformIndexes[i]] instanceof Perseverance candidate && candidate.block == newBlock) {
                                newCardIdx = state.properties.perseveranceTransformIndexes[i];
                                break;
                            }
                        }
                        if (newCardIdx != -1) {
                            state.getHandArrForWrite()[handIdx] = (short) newCardIdx;
                        }
                    }
                }
            });
        }

        public Card getUpgrade() {
            return new PerseveranceP(block + 2, limit + 12);
        }
    }

    public static class PerseveranceP extends Card {
        public int limit;
        private int block;

        public PerseveranceP() {
            this(7);
        }

        public PerseveranceP(int block, int limit) {
            super("Perseverance+ (" + block + ")", Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
            this.limit = limit;
            this.retain = true;
        }

        public PerseveranceP(int block) {
            this(block, 37);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = block; i <= limit; i += 3) {
                c.add(new PerseveranceP(i, limit));
            }
            return c;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            if (state.properties.perseverancePTransformIndexes != null && state.properties.perseverancePTransformIndexes.length > (limit - block) / 3 + 1) {
                return;
            }
            state.properties.perseverancePTransformIndexes = new int[(limit - block) / 3 + 1];
            for (int i = block; i <= limit; i += 3) {
                state.properties.perseverancePTransformIndexes[(i - block) / 3] = state.properties.findCardIndex(new PerseveranceP(i, limit));
            }
            state.properties.addOnRetainHandler("PerseveranceP", new GameEventCardHandler() {
                @Override public void handle(GameState state, int handIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int cardIdx = state.getHandArrForRead()[handIdx];
                    if (state.properties.cardDict[cardIdx] instanceof PerseveranceP p) {
                        int newBlock = Math.min(p.block + 3, p.limit);
                        int newCardIdx = -1;
                        for (int i = 0; i < state.properties.perseverancePTransformIndexes.length; i++) {
                            if (state.properties.cardDict[state.properties.perseverancePTransformIndexes[i]] instanceof PerseveranceP candidate && candidate.block == newBlock) {
                                newCardIdx = state.properties.perseverancePTransformIndexes[i];
                                break;
                            }
                        }
                        if (newCardIdx != -1) {
                            state.getHandArrForWrite()[handIdx] = (short) newCardIdx;
                        }
                    }
                }
            });
        }
    }

    public static abstract class _PrayT extends Card {
        private final int mantra;

        public _PrayT(String cardName, int mantra) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.mantra = mantra;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainMantra(mantra);
            state.addCardToDeck(state.properties.insightCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Insight());
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerMantraCounter();
        }
    }

    public static class Pray extends _PrayT {
        public Pray() {
            super("Pray", 3);
        }
    }

    public static class PrayP extends _PrayT {
        public PrayP() {
            super("Pray+", 4);
        }
    }

    public static abstract class _ReachHeavenT extends Card {
        private final int damage;

        public _ReachHeavenT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
            state.addCardToDeck(state.properties.throughViolenceCardIdx);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.ThroughViolence());
        }
    }

    public static class ReachHeaven extends _ReachHeavenT {
        public ReachHeaven() {
            super("Reach Heaven", 10);
        }
    }

    public static class ReachHeavenP extends _ReachHeavenT {
        public ReachHeavenP() {
            super("Reach Heaven+", 15);
        }
    }

    private static abstract class _RushdownT extends Card {
        private final int energyCost;

        public _RushdownT(String cardName, int energyCost) {
            super(cardName, Card.POWER, energyCost, Card.UNCOMMON);
            this.energyCost = energyCost;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += 2;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Rushdown", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 8.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnStanceChangeHandler("Rushdown", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0 && state.getStance() == Stance.WRATH) {
                        state.draw(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }
    }

    public static class Rushdown extends _RushdownT {
        public Rushdown() {
            super("Rushdown", 1);
        }
    }

    public static class RushdownP extends _RushdownT {
        public RushdownP() {
            super("Rushdown+", 0);
        }
    }

    private static abstract class _SanctityT extends Card {
        private final int block;

        public _SanctityT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
            this.needsLastCardType = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            if (state.getLastCardPlayedType() == Card.SKILL) {
                state.draw(2);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Sanctity extends _SanctityT {
        public Sanctity() {
            super("Sanctity", 6);
        }
    }

    public static class SanctityP extends _SanctityT {
        public SanctityP() {
            super("Sanctity+", 9);
        }
    }

    public static class SandsOfTime extends Card {
        public SandsOfTime() {
            this(4);
        }

        private SandsOfTime(int energyCost) {
            super("Sands of Time (" + energyCost + ")", Card.ATTACK, energyCost, Card.UNCOMMON);
            this.selectEnemy = true;
            this.retain = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 20);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new SandsOfTime(3), new SandsOfTime(2), new SandsOfTime(1), new SandsOfTime(0));
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.sandsOfTimeTransformIndexes = new int[state.properties.cardDict.length];
            Arrays.fill(state.properties.sandsOfTimeTransformIndexes, -1);
            for (int i = 1; i <= 4; i++) {
                int fromIdx = state.properties.findCardIndex(new SandsOfTime(i));
                int toIdx = state.properties.findCardIndex(new SandsOfTime(i - 1));
                if (fromIdx != -1 && toIdx != -1) {
                    state.properties.sandsOfTimeTransformIndexes[fromIdx] = toIdx;
                }
            }
            state.properties.addOnRetainHandler("SandsOfTime", new GameEventCardHandler() {
                @Override public void handle(GameState state, int handIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int cardIdx = state.getHandArrForRead()[handIdx];
                    if (state.properties.cardDict[cardIdx] instanceof SandsOfTime) {
                        int newCardIdx = state.properties.sandsOfTimeTransformIndexes[cardIdx];
                        if (newCardIdx != -1) {
                            state.getHandArrForWrite()[handIdx] = (short) newCardIdx;
                        }
                    }
                }
            });
        }
    }

    public static class SandsOfTimeP extends Card {
        public SandsOfTimeP() {
            this(4);
        }

        private SandsOfTimeP(int energyCost) {
            super("Sands of Time+ (" + energyCost + ")", Card.ATTACK, energyCost, Card.UNCOMMON);
            this.selectEnemy = true;
            this.retain = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 26);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new SandsOfTimeP(3), new SandsOfTimeP(2), new SandsOfTimeP(1), new SandsOfTimeP(0));
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.sandsOfTimePTransformIndexes = new int[state.properties.cardDict.length];
            Arrays.fill(state.properties.sandsOfTimePTransformIndexes, -1);
            for (int i = 1; i <= 4; i++) {
                int fromIdx = state.properties.findCardIndex(new SandsOfTimeP(i));
                int toIdx = state.properties.findCardIndex(new SandsOfTimeP(i - 1));
                if (fromIdx != -1 && toIdx != -1) {
                    state.properties.sandsOfTimePTransformIndexes[fromIdx] = toIdx;
                }
            }
            state.properties.addOnRetainHandler("SandsOfTimeP", new GameEventCardHandler() {
                @Override public void handle(GameState state, int handIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int cardIdx = state.getHandArrForRead()[handIdx];
                    if (state.properties.cardDict[cardIdx] instanceof SandsOfTimeP) {
                        int newCardIdx = state.properties.sandsOfTimePTransformIndexes[cardIdx];
                        if (newCardIdx != -1) {
                            state.getHandArrForWrite()[handIdx] = (short) newCardIdx;
                        }
                    }
                }
            });
        }
    }

    public static abstract class _SignatureMoveT extends Card {
        private final int damage;

        public _SignatureMoveT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, true);
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            int attackCount = 0;
            for (int i = 0; i < state.handArrLen; i++) {
                if (state.properties.cardDict[state.getHandArrForRead()[i]].cardType == Card.ATTACK) {
                    attackCount++;
                }
            }
            if (attackCount == 1) {
                return energyCost;
            } else {
                return -1;
            }
        }
    }

    public static class SignatureMove extends _SignatureMoveT {
        public SignatureMove() {
            super("Signature Move", 30);
        }
    }

    public static class SignatureMoveP extends _SignatureMoveT {
        public SignatureMoveP() {
            super("Signature Move+", 40);
        }
    }

    public static abstract class _SimmeringFuryT extends Card {
        private final int drawCount;

        public _SimmeringFuryT(String cardName, int drawCount) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.drawCount = drawCount;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += drawCount;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("SimmeringFury", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("SimmeringFury", new GameEventHandler(Integer.MAX_VALUE) {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.changeStance(Stance.WRATH);
                        state.draw(state.getCounterForRead()[counterIdx]);
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    public static class SimmeringFury extends _SimmeringFuryT {
        public SimmeringFury() {
            super("Simmering Fury", 2);
        }
    }

    public static class SimmeringFuryP extends _SimmeringFuryT {
        public SimmeringFuryP() {
            super("Simmering Fury+", 3);
        }
    }

    private static abstract class _StudyT extends Card {
        public _StudyT(String cardName, int energyCost) {
            super(cardName, Card.POWER, energyCost, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Study", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addEndOfTurnHandler("Study", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (counterIdx >= 0 && state.getCounterForRead()[counterIdx] > 0) {
                        for (int i = 0; i < state.getCounterForRead()[counterIdx]; i++) {
                            state.addCardToDeck(state.properties.insightCardIdx);
                        }
                    }
                }
            });
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Insight());
        }
    }

    public static class Study extends _StudyT {
        public Study() {
            super("Study", 2);
        }
    }

    public static class StudyP extends _StudyT {
        public StudyP() {
            super("Study+", 1);
        }
    }

    private static abstract class _SwivelT extends Card {
        private final int block;

        public _SwivelT(String cardName, int block) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Swivel", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("Swivel", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.getCounterForRead()[counterIdx] > 0 && state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        state.getCounterForWrite()[counterIdx]--;
                    }
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.swivelCounterIdx = idx;
        }
    }

    public static class Swivel extends _SwivelT {
        public Swivel() {
            super("Swivel", 8);
        }
    }

    public static class SwivelP extends _SwivelT {
        public SwivelP() {
            super("Swivel+", 11);
        }
    }

    public static abstract class _TalkToTheHandT extends Card {
        private final int damage;
        private final int blockReturn;

        public _TalkToTheHandT(String cardName, int damage, int blockReturn) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
            this.blockReturn = blockReturn;
            this.selectEnemy = true;
            this.exhaustWhenPlayed = true;
            this.talkToTheHandEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.TALK_TO_THE_HAND, blockReturn);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class TalkToTheHand extends _TalkToTheHandT {
        public TalkToTheHand() {
            super("Talk to the Hand", 5, 2);
        }
    }

    public static class TalkToTheHandP extends _TalkToTheHandT {
        public TalkToTheHandP() {
            super("Talk to the Hand+", 7, 3);
        }
    }

    public static abstract class _TantrumT extends Card {
        private final int hits;

        public _TantrumT(String cardName, int hits) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.hits = hits;
            this.selectEnemy = true;
            this.returnToDeckWhenPlay = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < hits; i++) {
                state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), 3);
            }
            state.changeStance(Stance.WRATH);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Tantrum extends _TantrumT {
        public Tantrum() {
            super("Tantrum", 3);
        }
    }

    public static class TantrumP extends _TantrumT {
        public TantrumP() {
            super("Tantrum+", 4);
        }
    }

    public static abstract class _WallopT extends Card {
        private final int damage;

        public _WallopT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            int dmgDone = state.playerDoDamageToEnemy(enemy, damage);
            if (dmgDone > 0) {
                state.getPlayerForWrite().gainBlock(dmgDone);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Wallop extends _WallopT {
        public Wallop() {
            super("Wallop", 9);
        }
    }

    public static class WallopP extends _WallopT {
        public WallopP() {
            super("Wallop+", 12);
        }
    }

    public static abstract class _WaveOfTheHandT extends Card {
        private final int weakAmount;

        public _WaveOfTheHandT(String cardName, int weakAmount) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.weakAmount = weakAmount;
            this.weakEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] = weakAmount;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("WaveOfTheHand", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnBlockHandler("WaveOfTheHand", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        for (var enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            enemy.applyDebuff(state, DebuffType.WEAK, state.getCounterForRead()[counterIdx]);
                        }
                    }
                }
            });
            state.properties.addEndOfTurnHandler("WaveOfTheHand", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    public static class WaveOfTheHand extends _WaveOfTheHandT {
        public WaveOfTheHand() {
            super("Wave of the Hand", 1);
        }
    }

    public static class WaveOfTheHandP extends _WaveOfTheHandT {
        public WaveOfTheHandP() {
            super("Wave of the Hand+", 2);
        }
    }

    public static abstract class _WeaveT extends Card {
        private final int damage;

        public _WeaveT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.damage = damage;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnScryHandler("Weave", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    for (int i = 0; i < state.discardArrLen && state.handArrLen < GameState.HAND_LIMIT; i++) {
                        int cardIdx = state.getDiscardArrForRead()[i];
                        if (state.properties.cardDict[cardIdx].cardName.startsWith("Weave")) {
                            state.removeCardFromDiscardByPosition(i);
                            i--;
                            state.addCardToHand(cardIdx);
                        }
                    }
                }
            });
        }
    }

    public static class Weave extends _WeaveT {
        public Weave() {
            super("Weave", 4);
        }
    }

    public static class WeaveP extends _WeaveT {
        public WeaveP() {
            super("Weave+", 6);
        }
    }

    public static abstract class _WheelKickT extends Card {
        private final int damage;

        public _WheelKickT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            state.draw(2);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class WheelKick extends _WheelKickT {
        public WheelKick() {
            super("Wheel Kick", 15);
        }
    }

    public static class WheelKickP extends _WheelKickT {
        public WheelKickP() {
            super("Wheel Kick+", 20);
        }
    }

    public static class WindmillStrike extends Card {
        public int limit;
        private int damage;

        public WindmillStrike() {
            this(7);
        }

        public WindmillStrike(int damage, int limit) {
            super("Windmill Strike (" + damage + ")", Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            this.limit = limit;
            this.selectEnemy = true;
            this.retain = true;
        }

        public WindmillStrike(int damage) {
            this(damage, 47);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = damage; i <= limit; i += 4) {
                c.add(new WindmillStrike(i, limit));
            }
            return c;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            if (state.properties.windmillStrikeTransformIndexes != null && state.properties.windmillStrikeTransformIndexes.length > (limit - damage) / 4 + 1) {
                return;
            }
            state.properties.windmillStrikeTransformIndexes = new int[(limit - damage) / 4 + 1];
            for (int i = damage; i <= limit; i += 4) {
                state.properties.windmillStrikeTransformIndexes[(i - damage) / 4] = state.properties.findCardIndex(new WindmillStrike(i, limit));
            }
            state.properties.addOnRetainHandler("WindmillStrike", new GameEventCardHandler() {
                @Override public void handle(GameState state, int handIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int cardIdx = state.getHandArrForRead()[handIdx];
                    if (state.properties.cardDict[cardIdx] instanceof WindmillStrike ws) {
                        int newDamage = Math.min(ws.damage + 4, ws.limit);
                        int newCardIdx = -1;
                        for (int i = 0; i < state.properties.windmillStrikeTransformIndexes.length; i++) {
                            if (state.properties.cardDict[state.properties.windmillStrikeTransformIndexes[i]] instanceof WindmillStrike candidate && candidate.damage == newDamage) {
                                newCardIdx = state.properties.windmillStrikeTransformIndexes[i];
                                break;
                            }
                        }
                        if (newCardIdx != -1) {
                            state.getHandArrForWrite()[handIdx] = (short) newCardIdx;
                        }
                    }
                }
            });
        }

        public Card getUpgrade() {
            return new WindmillStrikeP(damage + 3, limit + 13);
        }
    }

    public static class WindmillStrikeP extends Card {
        public int limit;
        private int damage;

        public WindmillStrikeP() {
            this(10);
        }

        public WindmillStrikeP(int damage, int limit) {
            super("Windmill Strike+ (" + damage + ")", Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            this.limit = limit;
            this.selectEnemy = true;
            this.retain = true;
        }

        public WindmillStrikeP(int damage) {
            this(damage, 60);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            return GameActionCtx.PLAY_CARD;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var c = new ArrayList<Card>();
            for (int i = damage; i <= limit; i += 5) {
                c.add(new WindmillStrikeP(i, limit));
            }
            return c;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            if (state.properties.windmillStrikePTransformIndexes != null && state.properties.windmillStrikePTransformIndexes.length > (limit - damage) / 5 + 1) {
                return;
            }
            state.properties.windmillStrikePTransformIndexes = new int[(limit - damage) / 5 + 1];
            for (int i = damage; i <= limit; i += 5) {
                state.properties.windmillStrikePTransformIndexes[(i - damage) / 5] = state.properties.findCardIndex(new WindmillStrikeP(i, limit));
            }
            state.properties.addOnRetainHandler("WindmillStrikeP", new GameEventCardHandler() {
                @Override public void handle(GameState state, int handIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    int cardIdx = state.getHandArrForRead()[handIdx];
                    if (state.properties.cardDict[cardIdx] instanceof WindmillStrikeP ws) {
                        int newDamage = Math.min(ws.damage + 5, ws.limit);
                        int newCardIdx = -1;
                        for (int i = 0; i < state.properties.windmillStrikePTransformIndexes.length; i++) {
                            if (state.properties.cardDict[state.properties.windmillStrikePTransformIndexes[i]] instanceof WindmillStrikeP candidate && candidate.damage == newDamage) {
                                newCardIdx = state.properties.windmillStrikePTransformIndexes[i];
                                break;
                            }
                        }
                        if (newCardIdx != -1) {
                            state.getHandArrForWrite()[handIdx] = (short) newCardIdx;
                        }
                    }
                }
            });
        }
    }

    public static abstract class _WorshipT extends Card {
        public _WorshipT(String cardName, boolean retain) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
            this.retain = retain;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainMantra(5);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerMantraCounter();
        }
    }

    public static class Worship extends _WorshipT {
        public Worship() {
            super("Worship", false);
        }
    }

    public static class WorshipP extends _WorshipT {
        public WorshipP() {
            super("Worship+", true);
        }
    }

    private static abstract class _WreathOfFlameT extends Card {
        private final int vigor;

        public _WreathOfFlameT(String cardName, int vigor) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.vigor = vigor;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += vigor;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("WreathOfFlame", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 20.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("WreathOfFlame", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.getCounterForRead()[counterIdx] > 0 && state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        // Attack played - consume vigor
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.wreathOfFlameCounterIdx = idx;
        }
    }

    public static class WreathOfFlame extends _WreathOfFlameT {
        public WreathOfFlame() {
            super("Wreath of Flame", 5);
        }
    }

    public static class WreathOfFlameP extends _WreathOfFlameT {
        public WreathOfFlameP() {
            super("Wreath of Flame+", 8);
        }
    }

    private static abstract class _AlphaT extends Card {
        public _AlphaT(String cardName, boolean innate) {
            super(cardName, Card.SKILL, 1, Card.RARE);
            this.exhaustWhenPlayed = true;
            this.innate = innate;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.addCardToDeck(state.properties.betaCardIdx);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Alpha extends _AlphaT {
        public Alpha() {
            super("Alpha", false);
        }
    }

    public static class AlphaP extends _AlphaT {
        public AlphaP() {
            super("Alpha+", true);
        }
    }

    public static abstract class _BlasphemyT extends Card {
        public _BlasphemyT(String cardName, boolean retain) {
            super(cardName, Card.SKILL, 1, Card.RARE);
            this.exhaustWhenPlayed = true;
            this.retain = retain;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.changeStance(Stance.DIVINITY);
            state.buffs |= PlayerBuff.BLASPHEMY.mask();
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("Blasphemy", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if ((state.buffs & PlayerBuff.BLASPHEMY.mask()) != 0) {
                        state.doNonAttackDamageToPlayer(99999, false, null);
                        state.buffs &= ~PlayerBuff.BLASPHEMY.mask();
                    }
                }
            });
        }
    }

    public static class Blasphemy extends _BlasphemyT {
        public Blasphemy() {
            super("Blasphemy", false);
        }
    }

    public static class BlasphemyP extends _BlasphemyT {
        public BlasphemyP() {
            super("Blasphemy+", true);
        }
    }

    public static abstract class _BrillianceT extends Card {
        private final int damage;

        public _BrillianceT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.RARE);
            this.damage = damage;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int totalDamage = damage;
            if (counterIdx >= 0) {
                totalDamage += state.getCounterForRead()[counterIdx];
            }
            state.playerDoNonAttackDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), totalDamage, true);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Brilliance", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 50.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.brillianceCounterIdx = idx;
        }
    }

    public static class Brilliance extends _BrillianceT {
        public Brilliance() {
            super("Brilliance", 12);
        }
    }

    public static class BrillianceP extends _BrillianceT {
        public BrillianceP() {
            super("Brilliance+", 16);
        }
    }

    private static abstract class _ConjureBladeT extends Card {
        private final boolean addExtraHit;
        private final int limit;

        public _ConjureBladeT(String cardName, boolean addExtraHit, int limit) {
            super(cardName, Card.SKILL, -1, Card.RARE);
            this.addExtraHit = addExtraHit;
            this.limit = limit;
            this.exhaustWhenPlayed = true;
            this.isXCost = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int xValue = addExtraHit ? energyUsed + 1 : energyUsed;
            int indexToUse = Math.min(xValue, limit);
            state.addCardToDeck(state.properties.conjureBladeIndexes[indexToUse]);
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            if (state.properties.conjureBladeIndexes == null || state.properties.conjureBladeIndexes.length - 1 < limit) {
                state.properties.conjureBladeIndexes = new int[limit + 1]; // 0-limit for different X values
                for (int i = 0; i <= limit; i++) {
                    state.properties.conjureBladeIndexes[i] = state.properties.findCardIndex(new CardColorless.Expunger(i));
                }
            }
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var result = new ArrayList<Card>();
            for (int i = 0; i <= limit; i++) {
                result.add(new CardColorless.Expunger(i));
            }
            return result;
        }
    }

    public static class ConjureBlade extends _ConjureBladeT {
        public ConjureBlade(int limit) {
            super("Conjure Blade", false, limit);
        }
    }

    public static class ConjureBladeP extends _ConjureBladeT {
        public ConjureBladeP(int limit) {
            super("Conjure Blade+", true, limit);
        }
    }

    public static abstract class _DeusExMachinaT extends Card {
        private final int miracleCount;

        public _DeusExMachinaT(String cardName, int miracleCount) {
            super(cardName, Card.SKILL, -1, Card.RARE);
            this.miracleCount = miracleCount;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            // This should never be called since it's unplayable (cost -1)
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addOnCardDrawnHandler("DeusExMachina", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx] instanceof _DeusExMachinaT deusEx) {
                        // Add Miracles to hand
                        for (int i = 0; i < deusEx.miracleCount; i++) {
                            state.addCardToHand(state.properties.miracleCardIdx);
                        }
                        // Exhaust this card - find and remove it from hand
                        for (int i = 0; i < state.handArrLen; i++) {
                            if (state.getHandArrForRead()[i] == cardIdx) {
                                state.removeCardFromHandByPosition(i);
                                break;
                            }
                        }
                        state.addCardToExhaust(cardIdx);
                    }
                }
            });
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless.Miracle());
        }
    }

    public static class DeusExMachina extends _DeusExMachinaT {
        public DeusExMachina() {
            super("Deus Ex Machina", 2);
        }
    }

    public static class DeusExMachinaP extends _DeusExMachinaT {
        public DeusExMachinaP() {
            super("Deus Ex Machina+", 3);
        }
    }

    public static abstract class _DevaFormT extends Card {
        public _DevaFormT(String cardName, boolean ethereal) {
            super(cardName, Card.POWER, 3, Card.RARE);
            this.ethereal = ethereal;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("DevaForm", this, 2, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 5.0f; // Deva Forms played
                    input[idx + 1] = state.getCounterForRead()[counterIdx + 1] / 20.0f; // Current energy gain
                    return idx + 2;
                }

                @Override public int getInputLenDelta() {
                    return 2;
                }
            });
            state.properties.addStartOfTurnHandler("DevaForm", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (counterIdx >= 0 && state.getCounterForRead()[counterIdx] > 0) {
                        int devaFormsPlayed = state.getCounterForRead()[counterIdx];
                        state.getCounterForWrite()[counterIdx + 1] += devaFormsPlayed;
                        state.gainEnergy(state.getCounterForRead()[counterIdx + 1]);
                    }
                }
            });
        }
    }

    public static class DevaForm extends _DevaFormT {
        public DevaForm() {
            super("Deva Form", true);
        }
    }

    public static class DevaFormP extends _DevaFormT {
        public DevaFormP() {
            super("Deva Form+", false);
        }
    }

    public static abstract class _DevotionT extends Card {
        private final int mantraPerTurn;

        public _DevotionT(String cardName, int mantraPerTurn) {
            super(cardName, Card.POWER, 1, Card.RARE);
            this.mantraPerTurn = mantraPerTurn;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += mantraPerTurn;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerMantraCounter();
            state.properties.registerCounter("Devotion", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Devotion", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (counterIdx >= 0 && state.getCounterForRead()[counterIdx] > 0) {
                        state.gainMantra(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }
    }

    public static class Devotion extends _DevotionT {
        public Devotion() {
            super("Devotion", 2);
        }
    }

    public static class DevotionP extends _DevotionT {
        public DevotionP() {
            super("Devotion+", 3);
        }
    }

    public static abstract class _EstablishmentT extends Card {
        public _EstablishmentT(String cardName, int energyCost) {
            super(cardName, Card.POWER, energyCost, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            // TODO: Implement effect - Whenever a card is Retained, lower its cost by 1
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Establishment extends _EstablishmentT {
        public Establishment() {
            super("Establishment", 1);
        }
    }

    public static class EstablishmentP extends _EstablishmentT {
        public EstablishmentP() {
            super("Establishment+", 1);
            this.innate = true;
        }
    }

    public static abstract class _JudgmentT extends Card {
        private final int hpThreshold;

        public _JudgmentT(String cardName, int hpThreshold) {
            super(cardName, Card.SKILL, 1, Card.RARE);
            this.hpThreshold = hpThreshold;
            this.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.getEnemiesForRead().get(idx).getHealth() <= hpThreshold) {
                state.killEnemy(idx, true);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Judgment extends _JudgmentT {
        public Judgment() {
            super("Judgment", 30);
        }
    }

    public static class JudgmentP extends _JudgmentT {
        public JudgmentP() {
            super("Judgment+", 40);
        }
    }

    public static abstract class _LessonLearnedT extends Card {
        private final int damage;
        protected double healthRewardRatio = 0;

        public _LessonLearnedT(String cardName, int damage, double healthRewardRatio) {
            super(cardName, Card.ATTACK, 2, Card.RARE);
            this.damage = damage;
            this.selectEnemy = true;
            this.exhaustWhenPlayed = true;
            this.healthRewardRatio = healthRewardRatio;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            if (!state.getEnemiesForRead().get(idx).properties.isMinion && state.getEnemiesForRead().get(idx).getHealth() <= 0) {
                if (state.getEnemiesForRead().get(idx) instanceof com.alphaStS.enemy.EnemyBeyond.Darkling ||
                        state.getEnemiesForRead().get(idx) instanceof com.alphaStS.enemy.EnemyBeyond.AwakenedOne) {
                    if (state.isTerminal() > 0) {
                        state.getCounterForWrite()[counterIdx]++;
                    }
                } else if (!(state.getEnemiesForRead().get(idx) instanceof com.alphaStS.enemy.EnemyEnding.CorruptHeart)) {
                    state.getCounterForWrite()[counterIdx]++;
                }
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("LessonLearned", this, healthRewardRatio == 0 ? null : new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            if (healthRewardRatio > 0) {
                state.properties.addExtraTrainingTarget("LessonLearned", this, new TrainingTarget() {
                    @Override public void fillVArray(GameState state, double[] v, int isTerminal) {
                        if (isTerminal > 0) {
                            v[GameState.V_OTHER_IDX_START + vArrayIdx] = state.getCounterForRead()[counterIdx] / 10.0;
                        }
                    }

                    @Override public void updateQValues(GameState state, double[] v) {
                        double vUpgrades = v[GameState.V_OTHER_IDX_START + vArrayIdx];
                        v[GameState.V_HEALTH_IDX] += 10 * vUpgrades * healthRewardRatio / state.getPlayeForRead().getMaxHealth();
                    }
                });
            }
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            counterIdx = idx;
        }
    }

    public static class LessonLearned extends _LessonLearnedT {
        public LessonLearned(double healthRewardRatio) {
            super("Lesson Learned", 10, healthRewardRatio);
        }

        public Card getUpgrade() {
            return new LessonLearnedP(healthRewardRatio);
        }
    }

    public static class LessonLearnedP extends _LessonLearnedT {
        public LessonLearnedP(double healthRewardRatio) {
            super("Lesson Learned+", 13, healthRewardRatio);
        }
    }

    public static abstract class _MasterRealityT extends Card {
        public _MasterRealityT(String cardName, int energyCost) {
            super(cardName, Card.POWER, energyCost, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            // TODO: Implement effect - upgrade any cards created during combat
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class MasterReality extends _MasterRealityT {
        public MasterReality() {
            super("Master Reality", 1);
        }
    }

    public static class MasterRealityP extends _MasterRealityT {
        public MasterRealityP() {
            super("Master Reality+", 0);
        }
    }

    public static abstract class _OmniscienceT extends Card {
        public _OmniscienceT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.RARE);
            this.selectFromDeck = true;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int cardIdx = state.getDeckArrForRead()[idx];
            state.removeCardFromDeck(idx, true);
            
            // Play the card twice
            var action = state.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
            if (action != null) {
                // First play
                state.addGameActionToStartOfDeque(curState -> {
                    curState.playCard(action, -1, true, null, false, true, -1, -1);
                    while (curState.actionCtx == GameActionCtx.SELECT_ENEMY) {
                        int enemyIdx = GameStateUtils.getRandomEnemyIdx(curState, RandomGenCtx.RandomEnemyGeneral);
                        curState.playCard(action, enemyIdx, true, null, false, true, -1, -1);
                    }
                });
                // Second play
                state.addGameActionToStartOfDeque(curState -> {
                    curState.playCard(action, -1, true, null, false, true, -1, -1);
                    while (curState.actionCtx == GameActionCtx.SELECT_ENEMY) {
                        int enemyIdx = GameStateUtils.getRandomEnemyIdx(curState, RandomGenCtx.RandomEnemyGeneral);
                        curState.playCard(action, enemyIdx, true, null, false, true, -1, -1);
                    }
                });
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Omniscience extends _OmniscienceT {
        public Omniscience() {
            super("Omniscience", 4);
        }
    }

    public static class OmniscienceP extends _OmniscienceT {
        public OmniscienceP() {
            super("Omniscience+", 3);
        }
    }

    public static abstract class _RagnarokT extends Card {
        private final int damage;
        private final int hits;

        public _RagnarokT(String cardName, int damage, int hits) {
            super(cardName, Card.ATTACK, 3, Card.RARE);
            this.damage = damage;
            this.hits = hits;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (int i = 0; i < hits; i++) {
                var enemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
                if (enemyIdx >= 0) {
                    state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(enemyIdx), damage);
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Ragnarok extends _RagnarokT {
        public Ragnarok() {
            super("Ragnarok", 5, 5);
        }
    }

    public static class RagnarokP extends _RagnarokT {
        public RagnarokP() {
            super("Ragnarok+", 6, 6);
        }
    }

    public static abstract class _ScrawlT extends Card {
        public _ScrawlT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.RARE);
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.draw(GameState.HAND_LIMIT - state.handArrLen);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Scrawl extends _ScrawlT {
        public Scrawl() {
            super("Scrawl", 1);
        }
    }

    public static class ScrawlP extends _ScrawlT {
        public ScrawlP() {
            super("Scrawl+", 0);
        }
    }

    public static abstract class _SpiritShieldT extends Card {
        private final int blockPerCard;

        public _SpiritShieldT(String cardName, int blockPerCard) {
            super(cardName, Card.SKILL, 2, Card.RARE);
            this.blockPerCard = blockPerCard;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(state.handArrLen * blockPerCard);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SpiritShield extends _SpiritShieldT {
        public SpiritShield() {
            super("Spirit Shield", 3);
        }
    }

    public static class SpiritShieldP extends _SpiritShieldT {
        public SpiritShieldP() {
            super("Spirit Shield+", 4);
        }
    }

    public static abstract class _VaultT extends Card {
        public _VaultT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.RARE);
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.buffs |= PlayerBuff.USED_VAULT.mask();
            state.buffs |= PlayerBuff.END_TURN_IMMEDIATELY.mask();
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Vault extends _VaultT {
        public Vault() {
            super("Vault", 3);
        }
    }

    public static class VaultP extends _VaultT {
        public VaultP() {
            super("Vault+", 2);
        }
    }

    public static abstract class _WishT extends Card {
        protected boolean upgraded = false;
        protected double healthRewardRatio = 0;

        public _WishT(String cardName, int energyCost, double healthRewardRatio) {
            super(cardName, Card.SKILL, energyCost, Card.RARE);
            this.exhaustWhenPlayed = true;
            this.healthRewardRatio = healthRewardRatio;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            // Set up the 3 wish choices
            Card armorCard = upgraded ? new com.alphaStS.card.CardOther.WishPlatedArmorP() : new com.alphaStS.card.CardOther.WishPlatedArmor();
            Card strengthCard = upgraded ? new com.alphaStS.card.CardOther.WishStrengthP() : new com.alphaStS.card.CardOther.WishStrength();
            Card goldCard = upgraded ? new com.alphaStS.card.CardOther.WishGoldP(healthRewardRatio) : new com.alphaStS.card.CardOther.WishGold(healthRewardRatio);
            
            int armorIdx = state.properties.findCardIndex(armorCard);
            int strengthIdx = state.properties.findCardIndex(strengthCard);
            int goldIdx = state.properties.findCardIndex(goldCard);
            
            state.setSelect1OutOf3Idxes(armorIdx, strengthIdx, goldIdx);
            return GameActionCtx.SELECT_CARD_1_OUT_OF_3;
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var generatedCards = new ArrayList<Card>();
            if (upgraded) {
                generatedCards.add(new com.alphaStS.card.CardOther.WishPlatedArmorP());
                generatedCards.add(new com.alphaStS.card.CardOther.WishStrengthP());
                generatedCards.add(new com.alphaStS.card.CardOther.WishGoldP(healthRewardRatio));
            } else {
                generatedCards.add(new com.alphaStS.card.CardOther.WishPlatedArmor());
                generatedCards.add(new com.alphaStS.card.CardOther.WishStrength());
                generatedCards.add(new com.alphaStS.card.CardOther.WishGold(healthRewardRatio));
            }
            return generatedCards;
        }

        public List<Card> getPossibleSelect1OutOf3Cards(GameProperties gameProperties) {
            if (upgraded) {
                return List.of(
                    new com.alphaStS.card.CardOther.WishPlatedArmorP(),
                    new com.alphaStS.card.CardOther.WishStrengthP(),
                    new com.alphaStS.card.CardOther.WishGoldP(healthRewardRatio)
                );
            } else {
                return List.of(
                    new com.alphaStS.card.CardOther.WishPlatedArmor(),
                    new com.alphaStS.card.CardOther.WishStrength(),
                    new com.alphaStS.card.CardOther.WishGold(healthRewardRatio)
                );
            }
        }
    }

    public static class Wish extends _WishT {
        public Wish(double healthRewardRatio) {
            super("Wish", 3, healthRewardRatio);
            this.upgraded = false;
        }
    }

    public static class WishP extends _WishT {
        public WishP(double healthRewardRatio) {
            super("Wish+", 3, healthRewardRatio);
            this.upgraded = true;
        }
    }
}
