package com.alphaStS.card;

import com.alphaStS.DebuffType;
import com.alphaStS.GameActionCtx;
import com.alphaStS.GameEventHandler;
import com.alphaStS.GameProperties;
import com.alphaStS.GameState;
import com.alphaStS.GameStateUtils;
import com.alphaStS.RandomGenCtx;
import com.alphaStS.enums.Stance;

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

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
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
    // todo: Pressure Points

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

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
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

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
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

    // todo: Conclude

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

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
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

    // todo: Fasting
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
    // todo: Foreign Influence
    // todo: Foresight

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
    // todo: Meditate

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

    // todo: Nirvana
    // todo: Perseverance

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

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
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

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
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

    // todo: Sands of Time
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
    // todo: Simmering Fury

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

        public List<Card> getPossibleGeneratedCards(List<Card> cards) {
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

    // todo: Swivel
    // todo: Talk to the Hand
    // todo: Tantrum
    // todo: Wallop
    // todo: Wave of the Hand
    // todo: Weave
    // todo: Wheel Kick
    // todo: Windmill Strike

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

    // todo: Wreath of Flame

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

    // todo: Blasphemy

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

    // todo: Conjure Blade
    // todo: Deus Ex Machina

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

    // todo: Establishment

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

    // todo: Lesson Learned
    // todo: Master Reality
    // todo: Omniscience

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

    // todo: Vault
    // todo: Wish
}
