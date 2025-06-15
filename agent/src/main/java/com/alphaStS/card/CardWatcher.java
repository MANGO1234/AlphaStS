package com.alphaStS.card;

import com.alphaStS.GameActionCtx;
import com.alphaStS.GameState;
import com.alphaStS.enums.Stance;

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

    // Crush Joints
    // Crush Joints
    // Cut Through Fate
    // Evaluate
    // Flurry of Blows
    // Flying Sleeves
    // Follow-Up
    // Halt
    // Just Lucky
    // Pressure Points
    // Prostrate
    // Protect
    // Sash Whip
    // Third Eye
    // Tranquility
    // Battle Hymn
    // Carve Reality
    // Collect
    // Conclude
    // Deceive Reality
    // Empty Mind
    // Fasting
    // Fear No Evil
    // Foreign Influence
    // Foresight
    // Indignation
    // Inner Peace
    // Like Water
    // Meditate
    // Mental Fortress
    // Nirvana
    // Perseverance
    // Pray
    // Reach Heaven
    // Rushdown
    // Sanctity
    // Sands of Time
    // Signature Move
    // Simmering Fury
    // Study
    // Swivel
    // Talk to the Hand
    // Tantrum
    // Wallop
    // Wave of the Hand
    // Weave
    // Wheel Kick
    // Windmill Strike
    // Worship
    // Wreath of Flame
    // Alpha
    // Blasphemy
    // Brilliance
    // Conjure Blade
    // Deus Ex Machina
    // Deva Form
    // Devotion
    // Establishment
    // Judgment
    // Lesson Learned
    // Master Reality
    // Omniscience
    // Ragnarok
    // Scrawl
    // Spirit Shield
    // Vault
    // Wish
}
