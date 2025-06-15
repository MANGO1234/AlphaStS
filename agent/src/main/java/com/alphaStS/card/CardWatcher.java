package com.alphaStS.card;

import com.alphaStS.DebuffType;
import com.alphaStS.GameActionCtx;
import com.alphaStS.GameEventHandler;
import com.alphaStS.GameProperties;
import com.alphaStS.GameState;
import com.alphaStS.enums.Stance;

import java.util.List;

public class CardWatcher {
    private static abstract class _EruptionT extends Card {
        public _EruptionT(String cardName, int energyCost) {
            super(cardName, Card.ATTACK, energyCost, Card.UNCOMMON);
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

    // Vigilance
    private static abstract class _VigilanceT extends Card {
        private final int block;

        public _VigilanceT(String cardName, int block) {
            super(cardName, Card.SKILL, 2, Card.UNCOMMON);
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

    // Prostrate
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

    // Pray
    public static abstract class _PrayT extends Card {
        private final int mantra;

        public _PrayT(String cardName, int mantra) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.mantra = mantra;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.gainMantra(mantra);
            return GameActionCtx.PLAY_CARD;
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

    // Worship
    public static abstract class _WorshipT extends Card {
        public _WorshipT(String cardName, boolean retain) {
            super(cardName, Card.SKILL, 2, Card.RARE);
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

    // Devotion
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

    private static abstract class _CrushJointsT extends Card {
        private final int damage;
        private final int vulnerable;

        public _CrushJointsT(String cardName, int damage, int vulnerable) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.vulnerable = vulnerable;
            this.selectEnemy = true;
            this.needsLastCardType = true;
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
    // todo: Cut Through Fate
    private static abstract class _EvaluateT extends Card {
        private final int block;

        public _EvaluateT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
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

    // todo: Flurry of Blows
    // todo: Flying Sleeves
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
    // todo: Halt
    // todo: Just Lucky
    // todo: Pressure Points
    // todo: Prostrate
    // todo: Protect
    // todo: Sash Whip
    // todo: Third Eye
    // todo: Tranquility
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

    // todo: Collect
    // todo: Conclude
    // todo: Deceive Reality
    // todo: Empty Mind
    // todo: Fasting
    // todo: Fear No Evil
    // todo: Foreign Influence
    // todo: Foresight
    // todo: Indignation
    // todo: Inner Peace
    // todo: Like Water
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
    // todo: Pray
    // todo: Reach Heaven
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
    // todo: Signature Move
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
    // todo: Worship
    // todo: Wreath of Flame
    // todo: Alpha
    // todo: Blasphemy
    // todo: Brilliance
    // todo: Conjure Blade
    // todo: Deus Ex Machina
    // todo: Deva Form
    // todo: Devotion
    // todo: Establishment
    // todo: Judgment
    // todo: Lesson Learned
    // todo: Master Reality
    // todo: Omniscience
    // todo: Ragnarok
    // todo: Scrawl
    // todo: Spirit Shield
    // todo: Vault
    // todo: Wish
}
