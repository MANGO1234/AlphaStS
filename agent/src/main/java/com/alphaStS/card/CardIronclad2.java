package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.eventHandler.OnDamageHandler;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.random.RandomGenCtx;

public class CardIronclad2 {
    // **************************************************************************************************
    // ********************************************* Basic  *********************************************
    // **************************************************************************************************

    public static class Bash extends CardIronclad.Bash {
    }

    public static class BashP extends CardIronclad.BashP {
    }

    public static class Defend extends Card.Defend {
    }

    public static class DefendP extends Card.DefendP {
    }

    public static class Strike extends Card.Strike {
    }

    public static class StrikeP extends Card.StrikeP {
    }

    // **************************************************************************************************
    // ********************************************* Common *********************************************
    // **************************************************************************************************

    public static class Anger extends CardIronclad.Anger {
    }

    public static class AngerP extends CardIronclad.AngerP {
    }

    public static class Armaments extends CardIronclad.Armanent {
    }

    public static class ArmamentsP extends CardIronclad.ArmanentP {
    }

    private static abstract class _BloodWallT extends Card {
        private final int block;

        public _BloodWallT(String cardName, int block) {
            super(cardName, Card.SKILL, 2, Card.COMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.doNonAttackDamageToPlayer(2, false, this);
            state.getPlayerForWrite().gainBlock(block);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class BloodWall extends _BloodWallT {
        public BloodWall() {
            super("Blood Wall", 16);
        }
    }

    public static class BloodWallP extends _BloodWallT {
        public BloodWallP() {
            super("Blood Wall+", 20);
        }
    }

    public static class Bloodletting extends CardIronclad._BloodlettingT {
        public Bloodletting() {
            super("Bloodletting", 2, Card.COMMON);
        }
    }

    public static class BloodlettingP extends CardIronclad._BloodlettingT {
        public BloodlettingP() {
            super("Bloodletting+", 3, Card.COMMON);
        }
    }

    public static class BodySlam extends CardIronclad.BodySlam {
    }

    public static class BodySlamP extends CardIronclad.BodySlamP {
    }

    private static abstract class _BreakthroughT extends Card {
        private final int damage;

        public _BreakthroughT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.doNonAttackDamageToPlayer(1, false, this);
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, damage);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Breakthrough extends _BreakthroughT {
        public Breakthrough() {
            super("Breakthrough", 9);
        }
    }

    public static class BreakthroughP extends _BreakthroughT {
        public BreakthroughP() {
            super("Breakthrough+", 13);
        }
    }

    private static abstract class _CinderT extends Card {
        private final int damage;

        public _CinderT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            int cardIdx = state.drawOneCardSpecial();
            if (cardIdx >= 0) {
                state.exhaustedCardHandle(cardIdx, true);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Cinder extends _CinderT {
        public Cinder() {
            super("Cinder", 17);
        }
    }

    public static class CinderP extends _CinderT {
        public CinderP() {
            super("Cinder+", 22);
        }
    }

    public static class Havoc extends CardIronclad.Havoc {
    }

    public static class HavocP extends CardIronclad.HavocP {
    }

    public static class Headbutt extends CardIronclad.Headbutt {
    }

    public static class HeadbuttP extends CardIronclad.HeadbuttP {
    }

    public static class IronWave extends CardIronclad.IronWave {
    }

    public static class IronWaveP extends CardIronclad.IronWaveP {
    }

    private static abstract class _MoltenFistT extends Card {
        private final int damage;

        public _MoltenFistT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
            exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, enemy.getVulnerable());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class MoltenFist extends _MoltenFistT {
        public MoltenFist() {
            super("Molten Fist", 10);
        }
    }

    public static class MoltenFistP extends _MoltenFistT {
        public MoltenFistP() {
            super("Molten Fist+", 14);
        }
    }

    public static class PerfectedStrike extends CardIronclad.PerfectedStrike {
    }

    public static class PerfectedStrikeP extends CardIronclad.PerfectedStrikeP {
    }

    public static class PommelStrike extends CardIronclad.PommelStrike {
    }

    public static class PommelStrikeP extends CardIronclad.PommelStrikeP {
    }

    private static abstract class _SetupStrikeT extends Card {
        private final int damage;
        private final int strength;

        public _SetupStrikeT(String cardName, int damage, int strength) {
            super(cardName, Card.ATTACK, 1, Card.COMMON);
            this.damage = damage;
            this.strength = strength;
            entityProperty.selectEnemy = true;
            entityProperty.changePlayerStrength = true;
            entityProperty.changePlayerStrengthEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage);
            var player = state.getPlayerForWrite();
            player.gainStrength(strength);
            player.applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, strength);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class SetupStrike extends _SetupStrikeT {
        public SetupStrike() {
            super("Setup Strike", 7, 2);
        }
    }

    public static class SetupStrikeP extends _SetupStrikeT {
        public SetupStrikeP() {
            super("Setup Strike+", 9, 3);
        }
    }

    public static class ShrugItOff extends CardIronclad.ShrugItOff {
    }

    public static class ShrugItOffP extends CardIronclad.ShrugItOffP {
    }

    public static class SwordBoomerang extends CardIronclad.SwordBoomerang {
    }

    public static class SwordBoomerangP extends CardIronclad.SwordBoomerangP {
    }

    public static class Thunderclap extends CardIronclad.Thunderclap {
    }

    public static class ThunderclapP extends CardIronclad.ThunderclapP {
    }

    private static abstract class _TrembleT extends Card {
        private final int vulnerable;

        public _TrembleT(String cardName, int vulnerable) {
            super(cardName, Card.SKILL, 1, Card.COMMON);
            this.vulnerable = vulnerable;
            entityProperty.selectEnemy = true;
            entityProperty.vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.VULNERABLE, vulnerable);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Tremble extends _TrembleT {
        public Tremble() {
            super("Tremble", 2);
        }
    }

    public static class TrembleP extends _TrembleT {
        public TrembleP() {
            super("Tremble+", 3);
        }
    }

    public static class TrueGrit extends CardIronclad.TrueGrit {
    }

    public static class TrueGritP extends CardIronclad.TrueGritP {
    }

    public static class TwinStrike extends CardIronclad.TwinStrike {
    }

    public static class TwinStrikeP extends CardIronclad.TwinStrikeP {
    }

    // **************************************************************************************************
    // ********************************************* Uncommon *******************************************
    // **************************************************************************************************

    private static abstract class _AshenStrikeT extends Card {
        private final int damage;
        private final int dmgPerExhaust;

        public _AshenStrikeT(String cardName, int damage, int dmgPerExhaust) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
            this.dmgPerExhaust = dmgPerExhaust;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage + dmgPerExhaust * state.exhaustArrLen);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class AshenStrike extends _AshenStrikeT {
        public AshenStrike() {
            super("Ashen Strike", 6, 3);
        }
    }

    public static class AshenStrikeP extends _AshenStrikeT {
        public AshenStrikeP() {
            super("Ashen Strike+", 6, 4);
        }
    }

    public static class BattleTrance extends CardIronclad.BattleTrance {
    }

    public static class BattleTranceP extends CardIronclad.BattleTranceP {
    }

    public static class Bludgeon extends CardIronclad.Bludgeon {
    }

    public static class BludgeonP extends CardIronclad.BludgeonP {
    }

    private static abstract class _BullyT extends Card {
        private final int damage;
        private final int bonus;

        public _BullyT(String cardName, int damage, int bonus) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.damage = damage;
            this.bonus = bonus;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage + bonus * enemy.getVulnerable());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Bully extends _BullyT {
        public Bully() {
            super("Bully", 4, 2);
        }
    }

    public static class BullyP extends _BullyT {
        public BullyP() {
            super("Bully+", 4, 3);
        }
    }

    public static class BurningPact extends CardIronclad.BurningPact {
    }

    public static class BurningPactP extends CardIronclad.BurningPactP {
    }

    // No need to implement Demonic Shield: Multiplayer

    private static abstract class _DismantleT extends Card {
        private final int damage;

        public _DismantleT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage);
            if (enemy.getVulnerable() > 0) {
                state.playerDoDamageToEnemy(enemy, damage);
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Dismantle extends _DismantleT {
        public Dismantle() {
            super("Dismantle", 8);
        }
    }

    public static class DismantleP extends _DismantleT {
        public DismantleP() {
            super("Dismantle+", 10);
        }
    }

    private static abstract class _DominateT extends Card {
        public _DominateT(String cardName, boolean exhaust) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            entityProperty.selectEnemy = true;
            entityProperty.changePlayerStrength = true;
            exhaustWhenPlayed = exhaust;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.getPlayerForWrite().gainStrength(enemy.getVulnerable());
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Dominate extends _DominateT {
        public Dominate() {
            super("Dominate", true);
        }
    }

    public static class DominateP extends _DominateT {
        public DominateP() {
            super("Dominate+", false);
        }
    }

    private static abstract class _DrumOfBattleT extends Card {
        private final int drawCount;

        public _DrumOfBattleT(String cardName, int drawCount) {
            super(cardName, Card.POWER, 0, Card.UNCOMMON);
            this.drawCount = drawCount;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.draw(drawCount);
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("DrumOfBattle", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 10.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("DrumOfBattle", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int n = state.getCounterForRead()[counterIdx];
                    for (int i = 0; i < n; i++) {
                        int cardIdx = state.drawOneCardSpecial();
                        if (cardIdx >= 0) {
                            state.exhaustedCardHandle(cardIdx, false);
                        }
                    }
                }
            });
        }
    }

    public static class DrumOfBattle extends _DrumOfBattleT {
        public DrumOfBattle() {
            super("Drum of Battle", 2);
        }
    }

    public static class DrumOfBattleP extends _DrumOfBattleT {
        public DrumOfBattleP() {
            super("Drum of Battle+", 3);
        }
    }

    private static abstract class _EvilEyeT extends Card {
        private final int block;

        public _EvilEyeT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            if (state.getCounterForRead()[state.properties.exhaustedThisTurnCounterIdx] > 0) {
                state.getPlayerForWrite().gainBlock(block);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerExhaustedThisTurnCounter();
        }
    }

    public static class EvilEye extends _EvilEyeT {
        public EvilEye() {
            super("Evil Eye", 8);
        }
    }

    public static class EvilEyeP extends _EvilEyeT {
        public EvilEyeP() {
            super("Evil Eye+", 11);
        }
    }

    private static abstract class _ExpectAFightT extends Card {
        public _ExpectAFightT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int attackCount = 0;
            for (int i = 0; i < state.handArrLen; i++) {
                if (state.properties.cardDict[state.handArr[i]].cardType == Card.ATTACK) {
                    attackCount++;
                }
            }
            state.gainEnergy(attackCount);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class ExpectAFight extends _ExpectAFightT {
        public ExpectAFight() {
            super("Expect a Fight", 2);
        }
    }

    public static class ExpectAFightP extends _ExpectAFightT {
        public ExpectAFightP() {
            super("Expect a Fight+", 1);
        }
    }

    public static class FeelNoPain extends CardIronclad.FeelNoPain {
    }

    public static class FeelNoPainP extends CardIronclad.FeelNoPainP {
    }

    private static abstract class _FightMeT extends Card {
        private final int damage;
        private final int playerStr;

        public _FightMeT(String cardName, int damage, int playerStr) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            this.playerStr = playerStr;
            entityProperty.selectEnemy = true;
            entityProperty.changePlayerStrength = true;
            entityProperty.affectEnemyStrength = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage);
            state.playerDoDamageToEnemy(enemy, damage);
            state.getPlayerForWrite().gainStrength(playerStr);
            enemy.gainStrength(1);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class FightMe extends _FightMeT {
        public FightMe() {
            super("Fight Me!", 5, 2);
        }
    }

    public static class FightMeP extends _FightMeT {
        public FightMeP() {
            super("Fight Me!+", 6, 3);
        }
    }

    public static class FlameBarrier extends CardIronclad.FlameBarrier {
    }

    public static class FlameBarrierP extends CardIronclad.FlameBarrierP {
    }

    private static abstract class _ForgottenRitualT extends Card {
        private final int gainEnergy;

        public _ForgottenRitualT(String cardName, int gainEnergy) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.gainEnergy = gainEnergy;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.getCounterForRead()[state.properties.exhaustedThisTurnCounterIdx] > 0) {
                state.gainEnergy(gainEnergy);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerExhaustedThisTurnCounter();
        }
    }

    public static class ForgottenRitual extends _ForgottenRitualT {
        public ForgottenRitual() {
            super("Forgotten Ritual", 3);
        }
    }

    public static class ForgottenRitualP extends _ForgottenRitualT {
        public ForgottenRitualP() {
            super("Forgotten Ritual+", 4);
        }
    }

    private static abstract class _GrappleT extends Card {
        private final int damage;
        private final int dmgPerBlock;

        public _GrappleT(String cardName, int damage, int dmgPerBlock) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
            this.dmgPerBlock = dmgPerBlock;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage);
            state.getCounterForWrite()[counterIdx] += dmgPerBlock;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Grapple", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 14.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnBlockHandler("Grapple", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        int i = 0;
                        if (state.enemiesAlive > 1) {
                            i = state.getSearchRandomGen().nextInt(state.enemiesAlive, RandomGenCtx.RandomEnemyGeneral, state);
                            state.setIsStochastic();
                        }
                        int enemy_j = 0;
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            if (i == enemy_j) {
                                state.playerDoNonAttackDamageToEnemy(enemy, state.getCounterForRead()[counterIdx], true);
                            }
                            enemy_j++;
                        }
                    }
                }
            });
            state.properties.addPreEndOfTurnHandler("Grapple", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
        }
    }

    public static class Grapple extends _GrappleT {
        public Grapple() {
            super("Grapple", 7, 5);
        }
    }

    public static class GrappleP extends _GrappleT {
        public GrappleP() {
            super("Grapple+", 9, 7);
        }
    }

    public static class Hemokinesis extends CardIronclad._HemokinesisT {
        public Hemokinesis() {
            super("Hemokinesis", 14);
        }
    }

    public static class HemokinesisP extends CardIronclad._HemokinesisT {
        public HemokinesisP() {
            super("Hemokinesis+", 19);
        }
    }

    // TODO: Howl from Beyond (Uncommon) - 3 energy, Attack
    //   Effect: Deal 16 damage to ALL enemies. At the start of your turn, plays from the Exhaust Pile.
    //   Upgraded Effect: Deal 21 damage to ALL enemies. At the start of your turn, plays from the Exhaust Pile.

    public static class InfernalBlade extends CardIronclad.InfernalBlade {
    }

    public static class InfernalBladeP extends CardIronclad.InfernalBladeP {
    }

    private static abstract class _InfernoT extends Card {
        private final int dmgToEnemies;

        public _InfernoT(String cardName, int dmgToEnemies) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.dmgToEnemies = dmgToEnemies;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += dmgToEnemies;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            // counterIdx = damage accumulator, counterIdx + 1 = is-player-turn flag
            state.properties.registerCounter("Inferno", this, 2, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 18.0f;
                    input[idx + 1] = state.getCounterForRead()[counterIdx + 1] > 0 ? 1.0f : 0.0f;
                    return idx + 2;
                }

                @Override public int getInputLenDelta() {
                    return 2;
                }
            });
            state.properties.addStartOfTurnHandler("Inferno", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx + 1] = 1;
                        state.doNonAttackDamageToPlayer(1, false, this);
                    }
                }
            });
            state.properties.addPreEndOfTurnHandler("Inferno", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx + 1] = 0;
                }
            });
            state.properties.addOnDamageHandler("Inferno", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (!isAttack && damageDealt > 0 && state.getCounterForRead()[counterIdx] > 0 && state.getCounterForRead()[counterIdx + 1] > 0) {
                        for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                            state.playerDoNonAttackDamageToEnemy(enemy, state.getCounterForRead()[counterIdx], true);
                        }
                    }
                }
            });
        }
    }

    public static class Inferno extends _InfernoT {
        public Inferno() {
            super("Inferno", 6);
        }
    }

    public static class InfernoP extends _InfernoT {
        public InfernoP() {
            super("Inferno+", 9);
        }
    }

    public static class Inflame extends CardIronclad.Inflame {
    }

    public static class InflameP extends CardIronclad.InflameP {
    }

    private static abstract class _JugglingT extends Card {
        public _JugglingT(String cardName, boolean innate) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.innate = innate;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            // counterIdx = number of Juggling copies in play, counterIdx + 1 = attacks played this turn
            state.properties.registerCounter("Juggling", this, 2, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 2.0f;
                    input[idx + 1] = state.getCounterForRead()[counterIdx + 1] / 3.0f;
                    return idx + 2;
                }

                @Override public int getInputLenDelta() {
                    return 2;
                }
            });
            state.properties.addOnCardPlayedHandler("Juggling", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.getCounterForRead()[counterIdx] > 0 && state.properties.cardDict[cardIdx].cardType == Card.ATTACK) {
                        state.getCounterForWrite()[counterIdx + 1]++;
                        if (state.getCounterForRead()[counterIdx + 1] == 3) {
                            for (int i = 0; i < state.getCounterForRead()[counterIdx]; i++) {
                                state.addCardToHand(cardIdx);
                            }
                        }
                    }
                }
            });
            state.properties.addStartOfTurnHandler("Juggling", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx + 1] = 0;
                }
            });
        }
    }

    public static class Juggling extends _JugglingT {
        public Juggling() {
            super("Juggling", false);
        }
    }

    public static class JugglingP extends _JugglingT {
        public JugglingP() {
            super("Juggling+", true);
        }
    }

    private static abstract class _PillageT extends Card {
        private final int damage;

        public _PillageT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage);
            int drawnIdx;
            while ((drawnIdx = state.draw(1)) >= 0) {
                if (state.properties.cardDict[drawnIdx].cardType != Card.ATTACK) {
                    break;
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Pillage extends _PillageT {
        public Pillage() {
            super("Pillage", 6);
        }
    }

    public static class PillageP extends _PillageT {
        public PillageP() {
            super("Pillage+", 9);
        }
    }

    public static class Rage extends CardIronclad.Rage {
    }

    public static class RageP extends CardIronclad.RageP {
    }

    // TODO: Rampage (Uncommon) - 1 energy, Attack
    //   Effect: Deal 9 damage. Increase this card's damage by 5 this combat.
    //   Upgraded Effect: Deal 9 damage. Increase this card's damage by 9 this combat.

    // TODO: Rupture (Uncommon) - 1 energy, Power
    //   Effect: Whenever you lose HP on your turn, gain 1 Strength.
    //   Upgraded Effect: Whenever you lose HP on your turn, gain 2 Strength.

    public static class SecondWind extends CardIronclad.SecondWind {
    }

    public static class SecondWindP extends CardIronclad.SecondWindP {
    }

    // TODO: Spite (Uncommon) - 0 energy, Attack
    //   Effect: Deal 6 damage. If you lost HP this turn, draw 1 card.
    //   Upgraded Effect: Deal 9 damage. If you lost HP this turn, draw 1 card.

    // TODO: Stampede (Uncommon) - 2 energy, Power
    //   Effect: At the end of your turn, 1 random Attack in your Hand is played against a random enemy.
    //   Upgraded Effect (1 energy): At the end of your turn, 1 random Attack in your Hand is played against a random enemy.

    // TODO: Stomp (Uncommon) - 3 energy, Attack
    //   Effect: Deal 12 damage to ALL enemies. Costs 1 less energy for each Attack played this turn.
    //   Upgraded Effect: Deal 15 damage to ALL enemies. Costs 1 less energy for each Attack played this turn.

    private static abstract class _StoneArmorT extends Card {
        private final int amount;

        public _StoneArmorT(String cardName, int amount) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.amount = amount;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[state.properties.platingCounterIdx] += amount;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerPlatingCounter();
        }
    }

    public static class StoneArmor extends _StoneArmorT {
        public StoneArmor() {
            super("Stone Armor", 4);
        }
    }

    public static class StoneArmorP extends _StoneArmorT {
        public StoneArmorP() {
            super("Stone Armor+", 6);
        }
    }

    private static abstract class _TauntT extends Card {
        private final int block;
        private final int vulnerable;

        public _TauntT(String cardName, int block, int vulnerable) {
            super(cardName, Card.SKILL, 1, Card.UNCOMMON);
            this.block = block;
            this.vulnerable = vulnerable;
            entityProperty.selectEnemy = true;
            entityProperty.vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getPlayerForWrite().gainBlock(block);
            state.getEnemiesForWrite().getForWrite(idx).applyDebuff(state, DebuffType.VULNERABLE, vulnerable);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Taunt extends _TauntT {
        public Taunt() {
            super("Taunt", 7, 1);
        }
    }

    public static class TauntP extends _TauntT {
        public TauntP() {
            super("Taunt+", 8, 2);
        }
    }

    // TODO: Unrelenting (Uncommon) - 2 energy, Attack
    //   Effect: Deal 12 damage. The next Attack you play costs 0 energy.
    //   Upgraded Effect: Deal 18 damage. The next Attack you play costs 0 energy.

    public static class Uppercut extends CardIronclad.Uppercut {
    }

    public static class UppercutP extends CardIronclad.UppercutP {
    }

    // TODO: Vicious (Uncommon) - 1 energy, Power
    //   Effect: Whenever you apply Vulnerable, draw 1 card.
    //   Upgraded Effect: Whenever you apply Vulnerable, draw 2 cards.

    public static class Whirlwind extends CardIronclad.Whirlwind {
    }

    public static class WhirlwindP extends CardIronclad.WhirlwindP {
    }

    // **************************************************************************************************
    // *********************************************  Rare  *********************************************
    // **************************************************************************************************

    // TODO: Aggression (Rare) - 1 energy, Power
    //   Effect: At the start of your turn, put a random Attack from your Discard Pile into your Hand and Upgrade it.
    //   Upgraded Effect: Innate. At the start of your turn, put a random Attack from your Discard Pile into your Hand and Upgrade it.

    public static class Barricade extends CardIronclad.Barricade {
    }

    public static class BarricadeP extends CardIronclad.BarricadeP {
    }

    // TODO: Brand (Rare) - 0 energy, Skill
    //   Effect: Lose 1 HP. Exhaust 1 card. Gain 1 Strength.
    //   Upgraded Effect: Lose 1 HP. Exhaust 1 card. Gain 2 Strength.

    // TODO: Cascade (Rare) - X energy, Skill
    //   Effect: Play the top X cards of your Draw Pile.
    //   Upgraded Effect: Play the top X+1 cards of your Draw Pile.

    // TODO: Colossus (Rare) - 1 energy, Skill
    //   Effect: Gain 5 Block. You receive 50% less damage from Vulnerable enemies this turn.
    //   Upgraded Effect: Gain 8 Block. You receive 50% less damage from Vulnerable enemies this turn.

    // TODO: Conflagration (Rare) - 1 energy, Attack
    //   Effect: Deal 8 damage to ALL enemies. Deals 2 additional damage for each other Attack you've played this turn.
    //   Upgraded Effect: Deal 9 damage to ALL enemies. Deals 3 additional damage for each other Attack you've played this turn.

    // TODO: Crimson Mantle (Rare) - 1 energy, Power
    //   Effect: At the start of your turn, lose 1 HP and gain 8 Block.
    //   Upgraded Effect: At the start of your turn, lose 1 HP and gain 10 Block.

    // TODO: Cruelty (Rare) - 1 energy, Power
    //   Effect: Vulnerable enemies take an additional 25% damage.
    //   Upgraded Effect: Vulnerable enemies take an additional 50% damage.

    public static class DarkEmbrace extends CardIronclad.DarkEmbrace {
    }

    public static class DarkEmbraceP extends CardIronclad.DarkEmbraceP {
    }

    public static class DemonForm extends CardIronclad.DemonForm {
    }

    public static class DemonFormP extends CardIronclad.DemonFormP {
    }

    public static class Feed extends CardIronclad.Feed {
    }

    public static class FeedP extends CardIronclad.FeedP {
    }

    public static class FiendFire extends CardIronclad.FiendFire {
    }

    public static class FiendFireP extends CardIronclad.FiendFireP {
    }

    // TODO: Hellraiser (Rare) - 2 energy, Power
    //   Effect: Whenever you draw a card containing "Strike", it is played against a random enemy.
    //   Upgraded Effect (1 energy): Whenever you draw a card containing "Strike", it is played against a random enemy.

    public static class Impervious extends CardIronclad.Impervious {
    }

    public static class ImperviousP extends CardIronclad.ImperviousP {
    }

    public static class Juggernaut extends CardIronclad.Juggernaut {
    }

    public static class JuggernautP extends CardIronclad.JuggernautP {
    }

    // TODO: Mangle (Rare) - 3 energy, Attack
    //   Effect: Deal 15 damage. Enemy loses 10 Strength this turn.
    //   Upgraded Effect: Deal 20 damage. Enemy loses 15 Strength this turn.

    public static class Offering extends CardIronclad.Offering {
    }

    public static class OfferingP extends CardIronclad.OfferingP {
    }

    // TODO: One-Two Punch (Rare) - 1 energy, Skill
    //   Effect: This turn, your next Attack is played an extra time.
    //   Upgraded Effect: This turn, your next 2 Attacks are played an extra time.

    // TODO: Pact's End (Rare) - 0 energy, Attack
    //   Effect: Can only be played if you have 3 or more cards in your Exhaust Pile. Deal 17 damage to ALL enemies.
    //   Upgraded Effect: Can only be played if you have 3 or more cards in your Exhaust Pile. Deal 23 damage to ALL enemies.

    // TODO: Primal Force (Rare) - 0 energy, Skill
    //   Effect: Transform all Attacks in your Hand into Giant Rock.
    //   Upgraded Effect: Transform all Attacks in your Hand into Giant Rock+.

    // TODO: Pyre (Rare) - 2 energy, Power
    //   Effect: Gain energy at the start of each turn.
    //   Upgraded Effect: Gain 2 energy at the start of each turn.

    // TODO: Stoke (Rare) - 1 energy, Skill
    //   Effect: Exhaust your Hand. Draw a card for each card Exhausted. Exhaust.
    //   Upgraded Effect (0 energy): Exhaust your Hand. Draw a card for each card Exhausted. Exhaust.

    // TODO: Tank (Rare) - 1 energy, Power
    //   Effect: Take double damage from enemies. Allies take half damage from enemies.
    //   Upgraded Effect (0 energy): Take double damage from enemies. Allies take half damage from enemies.

    // TODO: Tear Asunder (Rare) - 2 energy, Attack
    //   Effect: Deal 5 damage. Hits an additional time for each time you lost HP this combat.
    //   Upgraded Effect: Deal 7 damage. Hits an additional time for each time you lost HP this combat.

    // TODO: Thrash (Rare) - 1 energy, Attack
    //   Effect: Deal 4 damage twice. Exhaust a random Attack in your Hand and add its damage to this card.
    //   Upgraded Effect: Deal 6 damage twice. Exhaust a random Attack in your Hand and add its damage to this card.

    // TODO: Unmovable (Rare) - 2 energy, Power
    //   Effect: The first time you gain Block from a card each turn, double the amount gained.
    //   Upgraded Effect (1 energy): The first time you gain Block from a card each turn, double the amount gained.

    // **************************************************************************************************
    // ********************************************* Event  *********************************************
    // **************************************************************************************************

    public static class Clash extends CardIronclad.Clash {
    }

    public static class ClashP extends CardIronclad.ClashP {
    }

    public static class DualWield extends CardIronclad.DualWield {
    }

    public static class DualWieldP extends CardIronclad.DualWieldP {
    }

    public static class Entrench extends CardIronclad.Entrench {
    }

    public static class EntrenchP extends CardIronclad.EntrenchP {
    }

    // **************************************************************************************************
    // ********************************************* Ancient *********************************************
    // **************************************************************************************************

    private static abstract class _BreakT extends Card {
        private final int damage;
        private final int vulnerable;

        public _BreakT(String cardName, int damage, int vulnerable) {
            super(cardName, Card.ATTACK, 2, Card.RARE);
            this.damage = damage;
            this.vulnerable = vulnerable;
            entityProperty.selectEnemy = true;
            entityProperty.vulnEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage);
            enemy.applyDebuff(state, DebuffType.VULNERABLE, vulnerable);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Break extends _BreakT {
        public Break() {
            super("Break", 20, 5);
        }
    }

    public static class BreakP extends _BreakT {
        public BreakP() {
            super("Break+", 25, 7);
        }
    }

    public static class Corruption extends CardIronclad.Corruption {
    }

    public static class CorruptionP extends CardIronclad.CorruptionP {
    }
}
