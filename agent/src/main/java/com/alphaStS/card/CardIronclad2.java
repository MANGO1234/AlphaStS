package com.alphaStS.card;

import com.alphaStS.*;
import com.alphaStS.action.GameEnvironmentAction;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyReadOnly;
import com.alphaStS.enums.DebuffType;
import com.alphaStS.eventHandler.GameEventCardHandler;
import com.alphaStS.eventHandler.GameEventEnemyDebuffHandler;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.eventHandler.OnDamageHandler;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.random.RandomGenCtx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static class Armaments extends CardIronclad.Armaments {
    }

    public static class ArmamentsP extends CardIronclad.ArmamentP {
    }

    private static abstract class _BloodWallT extends Card {
        private final int block;

        public _BloodWallT(String cardName, int block) {
            super(cardName, Card.SKILL, 2, Card.COMMON);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.doNonAttackDamageToPlayer(2, false, this);
            state.playerGainBlock(block);
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
                state.playerDoDamageToEnemy(enemy, damage, this);
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
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
            state.playerDoDamageToEnemy(enemy, damage, this);
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
            state.playerDoDamageToEnemy(state.getEnemiesForWrite().getForWrite(idx), damage, this);
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
            state.playerDoDamageToEnemy(enemy, damage + dmgPerExhaust * state.exhaustArrLen, this);
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
            state.playerDoDamageToEnemy(enemy, damage + bonus * enemy.getVulnerable(), this);
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
            state.playerDoDamageToEnemy(enemy, damage, this);
            if (enemy.getVulnerable() > 0) {
                state.playerDoDamageToEnemy(enemy, damage, this);
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
            state.playerGainBlock(block);
            if (state.getCounterForRead()[state.properties.exhaustedThisTurnCounterIdx] > 0) {
                state.playerGainBlock(block);
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
            state.playerDoDamageToEnemy(enemy, damage, this);
            state.playerDoDamageToEnemy(enemy, damage, this);
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
            state.playerDoDamageToEnemy(enemy, damage, this);
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

    private static abstract class _HowlFromBeyondT extends Card {
        private final int damage;

        public _HowlFromBeyondT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 3, Card.UNCOMMON);
            this.damage = damage;
            this.exhaustWhenPlayed = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, damage, this);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("HowlFromBeyond", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    for (int i = 0; i < state.exhaustArrLen; i++) {
                        int cardIdx = state.exhaustArr[i];
                        if (state.properties.cardDict[cardIdx].getBaseCard() instanceof _HowlFromBeyondT) {
                            final int ci = cardIdx;
                            state.addGameActionToEndOfDeque(new GameEnvironmentAction() {
                                @Override public void doAction(GameState s) {
                                    var a = s.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][ci];
                                    s.playCard(a, 0, true, _HowlFromBeyondT.class, false, false, -1, GameState.EXHAUST);
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    public static class HowlFromBeyond extends _HowlFromBeyondT {
        public HowlFromBeyond() {
            super("Howl from Beyond", 16);
        }
    }

    public static class HowlFromBeyondP extends _HowlFromBeyondT {
        public HowlFromBeyondP() {
            super("Howl from Beyond+", 21);
        }
    }

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
            state.playerDoDamageToEnemy(enemy, damage, this);
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

    private static abstract class _Rampage2T extends Card {
        final int dmg;
        final int limit;

        public _Rampage2T(String cardName, int dmg, int limit) {
            super(cardName, Card.ATTACK, 1, Card.UNCOMMON);
            this.dmg = dmg;
            this.limit = limit;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, dmg, this);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Rampage extends _Rampage2T {
        public Rampage() {
            this(9, 34);
        }

        public Rampage(int dmg, int limit) {
            super("Rampage (" + dmg + ")", dmg, limit);
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var c = new ArrayList<Card>();
            for (Card card : cards) {
                if (card.getBaseCard() instanceof Rampage r) {
                    for (int j = r.dmg + 5; j <= r.limit; j += 5) {
                        c.add(card.wrapAfterPlay(new Rampage(j, r.limit)));
                    }
                }
            }
            return c;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.rampage2TransformIndexes = new int[state.properties.cardDict.length];
            Arrays.fill(state.properties.rampage2TransformIndexes, -1);
            for (int i = 0; i < state.properties.rampage2TransformIndexes.length; i++) {
                var card = state.properties.cardDict[i].getBaseCard();
                if (card instanceof Rampage r) {
                    state.properties.rampage2TransformIndexes[i] = state.properties.findCardIndex(state.properties.cardDict[i].wrap(new Rampage(r.dmg + 5, r.limit)));
                }
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop, int cardIdx) {
            return prop.rampage2TransformIndexes[cardIdx];
        }
    }

    public static class RampageP extends _Rampage2T {
        public RampageP() {
            this(9, 54);
        }

        public RampageP(int dmg, int limit) {
            super("Rampage+ (" + dmg + ")", dmg, limit);
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            var c = new ArrayList<Card>();
            for (Card card : cards) {
                if (card.getBaseCard() instanceof RampageP r) {
                    for (int j = r.dmg + 9; j <= r.limit; j += 9) {
                        c.add(card.wrapAfterPlay(new RampageP(j, r.limit)));
                    }
                }
            }
            return c;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.rampageP2TransformIndexes = new int[state.properties.cardDict.length];
            Arrays.fill(state.properties.rampageP2TransformIndexes, -1);
            for (int i = 0; i < state.properties.rampageP2TransformIndexes.length; i++) {
                var card = state.properties.cardDict[i].getBaseCard();
                if (card instanceof RampageP r) {
                    state.properties.rampageP2TransformIndexes[i] = state.properties.findCardIndex(state.properties.cardDict[i].wrap(new RampageP(r.dmg + 9, r.limit)));
                }
            }
        }

        @Override public int onPlayTransformCardIdx(GameProperties prop, int cardIdx) {
            return prop.rampageP2TransformIndexes[cardIdx];
        }
    }

    private static abstract class _Rupture2T extends Card {
        private final int n;

        public _Rupture2T(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            entityProperty.changePlayerStrength = true;
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Rupture2", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 4.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnDamageHandler("Rupture2", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (damageDealt > 0) {
                        state.getPlayerForWrite().gainStrength(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }
    }

    public static class Rupture extends _Rupture2T {
        public Rupture() {
            super("Rupture", 1);
        }
    }

    public static class RuptureP extends _Rupture2T {
        public RuptureP() {
            super("Rupture+", 2);
        }
    }

    public static class SecondWind extends CardIronclad.SecondWind {
    }

    public static class SecondWindP extends CardIronclad.SecondWindP {
    }

    private static abstract class _SpiteT extends Card {
        private final int damage;

        public _SpiteT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage, this);
            if (state.getCounterForRead()[counterIdx] > 0) {
                state.draw(1);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Spite", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] > 0 ? 1.0f : 0.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addPreStartOfTurnHandler("Spite", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.getCounterForWrite()[counterIdx] = 0;
                }
            });
            state.properties.addOnDamageHandler("Spite", new OnDamageHandler() {
                @Override public void handle(GameState state, Object source, boolean isAttack, int damageDealt) {
                    if (damageDealt > 0) {
                        state.getCounterForWrite()[counterIdx] = 1;
                    }
                }
            });
        }
    }

    public static class Spite extends _SpiteT {
        public Spite() {
            super("Spite", 6);
        }
    }

    public static class SpiteP extends _SpiteT {
        public SpiteP() {
            super("Spite+", 9);
        }
    }

    private static abstract class _StampedeT extends Card {
        public _StampedeT(String cardName, int energy) {
            super(cardName, Card.POWER, energy, Card.UNCOMMON);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Stampede", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addPreEndOfTurnHandler("Stampede", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int copies = state.getCounterForRead()[counterIdx];
                    if (copies == 0) return;
                    for (int k = 0; k < copies; k++) {
                        state.addGameActionToEndOfDeque(new GameEnvironmentAction() {
                            @Override public void doAction(GameState s) {
                                int attackCount = 0;
                                for (int i = 0; i < s.handArrLen; i++) {
                                    if (s.properties.cardDict[s.getHandArrForRead()[i]].cardType == Card.ATTACK) {
                                        attackCount++;
                                    }
                                }
                                if (attackCount == 0) return;
                                int r = 0;
                                if (attackCount > 1) {
                                    s.setIsStochastic();
                                    r = s.getSearchRandomGen().nextInt(attackCount, RandomGenCtx.RandomCardHand, s);
                                }
                                int selectedCardIdx = -1;
                                int acc = 0;
                                for (int i = 0; i < s.handArrLen; i++) {
                                    if (s.properties.cardDict[s.getHandArrForRead()[i]].cardType == Card.ATTACK) {
                                        if (acc == r) {
                                            selectedCardIdx = s.getHandArrForRead()[i];
                                            break;
                                        }
                                        acc++;
                                    }
                                }
                                int enemyIdx = GameStateUtils.getRandomEnemyIdx(s, RandomGenCtx.RandomEnemyGeneral);
                                if (enemyIdx < 0) return;
                                var a = s.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][selectedCardIdx];
                                s.playCard(a, enemyIdx, true, null, false, false, -1, -1);
                            }
                        });
                    }
                }
            });
        }
    }

    public static class Stampede extends _StampedeT {
        public Stampede() {
            super("Stampede", 2);
        }
    }

    public static class StampedeP extends _StampedeT {
        public StampedeP() {
            super("Stampede+", 1);
        }
    }

    private static abstract class _StompT extends Card {
        private final int damage;

        public _StompT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 3, Card.UNCOMMON);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, damage, this);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public int energyCost(GameState state) {
            if (state == null) return 3;
            return Math.max(0, 3 - state.getCounterForRead()[state.properties.attacksPlayedThisTurnCounterIdx]);
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerAttacksPlayedThisTurnCounter();
        }
    }

    public static class Stomp extends _StompT {
        public Stomp() {
            super("Stomp", 12);
        }
    }

    public static class StompP extends _StompT {
        public StompP() {
            super("Stomp+", 15);
        }
    }

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
            state.playerGainBlock(block);
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

    private static abstract class _UnrelentingT extends Card {
        private final int damage;

        public _UnrelentingT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 2, Card.UNCOMMON);
            this.damage = damage;
            entityProperty.selectEnemy = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage, this);
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Unrelenting", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 3.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("Unrelenting", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.properties.cardDict[cardIdx].cardType == Card.ATTACK && state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx]--;
                    }
                }
            });
        }

        @Override public void setCounterIdx(GameProperties gameProperties, int idx) {
            super.setCounterIdx(gameProperties, idx);
            gameProperties.unrelentingCounterIdx = idx;
        }
    }

    public static class Unrelenting extends _UnrelentingT {
        public Unrelenting() {
            super("Unrelenting", 12);
        }
    }

    public static class UnrelentingP extends _UnrelentingT {
        public UnrelentingP() {
            super("Unrelenting+", 18);
        }
    }

    public static class Uppercut extends CardIronclad.Uppercut {
    }

    public static class UppercutP extends CardIronclad.UppercutP {
    }

    private static abstract class _ViciousT extends Card {
        private final int n;

        public _ViciousT(String cardName, int n) {
            super(cardName, Card.POWER, 1, Card.UNCOMMON);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Vicious", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 4.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnEnemyDebuffHandler("Vicious", new GameEventEnemyDebuffHandler() {
                @Override public void handle(GameState state, EnemyReadOnly enemy, DebuffType type, int amount) {
                    if (type == DebuffType.VULNERABLE && state.getCounterForRead()[counterIdx] > 0) {
                        state.draw(state.getCounterForRead()[counterIdx]);
                    }
                }
            });
        }
    }

    public static class Vicious extends _ViciousT {
        public Vicious() {
            super("Vicious", 1);
        }
    }

    public static class ViciousP extends _ViciousT {
        public ViciousP() {
            super("Vicious+", 2);
        }
    }

    public static class Whirlwind extends CardIronclad.Whirlwind {
    }

    public static class WhirlwindP extends CardIronclad.WhirlwindP {
    }

    // **************************************************************************************************
    // *********************************************  Rare  *********************************************
    // **************************************************************************************************

    private static abstract class _AggressionT extends Card {
        public _AggressionT(String cardName, boolean innate) {
            super(cardName, Card.POWER, 1, Card.RARE);
            this.innate = innate;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Aggression", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 2.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addStartOfTurnHandler("Aggression", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    int n = state.getCounterForRead()[counterIdx];
                    for (int j = 0; j < n; j++) {
                        if (state.handArrLen >= GameState.HAND_LIMIT) {
                            return;
                        }
                        var discardArr = state.getDiscardArrForRead();
                        int discardLen = state.getNumCardsInDiscard();
                        var attackIdxes = new ArrayList<Integer>();
                        for (int i = 0; i < discardLen; i++) {
                            if (state.properties.cardDict[discardArr[i]].cardType == Card.ATTACK) {
                                attackIdxes.add(i);
                            }
                        }
                        if (attackIdxes.isEmpty()) {
                            return;
                        }
                        int posInDiscard;
                        if (attackIdxes.size() > 1) {
                            state.setIsStochastic();
                            int pick = state.getSearchRandomGen().nextInt(attackIdxes.size(), RandomGenCtx.RandomCardDiscardAggression);
                            posInDiscard = attackIdxes.get(pick);
                        } else {
                            posInDiscard = attackIdxes.get(0);
                        }
                        int cardIdx = discardArr[posInDiscard];
                        state.removeCardFromDiscardByPosition(posInDiscard);
                        int upgradedIdx = state.properties.upgradeIdxes[cardIdx];
                        state.addCardToHand(upgradedIdx >= 0 ? upgradedIdx : cardIdx);
                    }
                }
            });
        }

        public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return cards.stream()
                    .filter(c -> c.cardType == Card.ATTACK)
                    .map(Card::getUpgrade)
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }
    }

    public static class Aggression extends _AggressionT {
        public Aggression() {
            super("Aggression", false);
        }
    }

    public static class AggressionP extends _AggressionT {
        public AggressionP() {
            super("Aggression+", true);
        }
    }

    public static class Barricade extends CardIronclad.Barricade {
    }

    public static class BarricadeP extends CardIronclad.BarricadeP {
    }

    private static abstract class _BrandT extends Card {
        private final int strength;

        public _BrandT(String cardName, int strength) {
            super(cardName, Card.SKILL, 0, Card.RARE);
            this.strength = strength;
            entityProperty.selectFromHand = true;
            selectFromHandLater = true;
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            if (state.actionCtx == GameActionCtx.PLAY_CARD) {
                state.doNonAttackDamageToPlayer(1, false, this);
                return GameActionCtx.SELECT_CARD_HAND;
            } else {
                state.exhaustCardFromHand(idx);
                state.getPlayerForWrite().gainStrength(strength);
                return GameActionCtx.PLAY_CARD;
            }
        }
    }

    public static class Brand extends _BrandT {
        public Brand() {
            super("Brand", 1);
        }
    }

    public static class BrandP extends _BrandT {
        public BrandP() {
            super("Brand+", 2);
        }
    }

    private static abstract class _CascadeT extends Card {
        private final int extraCards;

        public _CascadeT(String cardName, int extraCards) {
            super(cardName, Card.SKILL, -1, Card.RARE);
            isXCost = true;
            this.extraCards = extraCards;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[state.properties.playCardOnTopOfDeckCounterIdx] += energyUsed + extraCards;
            return GameActionCtx.PLAY_CARD;
        }

        public int energyCost(GameState state) {
            return state.energy;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerPlayCardOnTopOfDeckCounter();
        }
    }

    public static class Cascade extends _CascadeT {
        public Cascade() {
            super("Cascade", 0);
        }
    }

    public static class CascadeP extends _CascadeT {
        public CascadeP() {
            super("Cascade+", 1);
        }
    }

    private static abstract class _ColossusT extends Card {
        private final int block;

        public _ColossusT(String cardName, int block) {
            super(cardName, Card.SKILL, 1, Card.RARE);
            this.block = block;
            entityProperty.possibleBuffs |= PlayerBuff.COLOSSUS.mask();
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.playerGainBlock(block);
            state.buffs |= PlayerBuff.COLOSSUS.mask();
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.addStartOfTurnHandler("Colossus", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    state.buffs &= ~PlayerBuff.COLOSSUS.mask();
                }
            });
        }
    }

    public static class Colossus extends _ColossusT {
        public Colossus() {
            super("Colossus", 5);
        }
    }

    public static class ColossusP extends _ColossusT {
        public ColossusP() {
            super("Colossus+", 8);
        }
    }

    private static abstract class _ConflagrationT extends Card {
        private final int baseDamage;
        private final int bonusPerAttack;

        public _ConflagrationT(String cardName, int baseDamage, int bonusPerAttack) {
            super(cardName, Card.ATTACK, 1, Card.RARE);
            this.baseDamage = baseDamage;
            this.bonusPerAttack = bonusPerAttack;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int attacksPlayed = state.getCounterForRead()[state.properties.attacksPlayedThisTurnCounterIdx];
            int damage = baseDamage + bonusPerAttack * attacksPlayed;
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, damage, this);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerAttacksPlayedThisTurnCounter();
        }
    }

    public static class Conflagration extends _ConflagrationT {
        public Conflagration() {
            super("Conflagration", 8, 2);
        }
    }

    public static class ConflagrationP extends _ConflagrationT {
        public ConflagrationP() {
            super("Conflagration+", 9, 3);
        }
    }

    private static abstract class _CrimsonMantleT extends Card {
        private final int block;

        public _CrimsonMantleT(String cardName, int block) {
            super(cardName, Card.POWER, 1, Card.RARE);
            this.block = block;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += 1 << 16;
            state.getCounterForWrite()[counterIdx] += block;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            var _this = this;
            var name = cardName.endsWith("+") ? cardName.substring(0, cardName.length() - 1) : cardName;
            state.properties.registerCounter(name, this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    int counter = state.getCounterForRead()[counterIdx];
                    input[idx] = (counter >> 16) / 2.0f;
                    input[idx + 1] = (counter & ((1 << 16) - 1)) / 20.0f;
                    return idx + 2;
                }

                @Override public int getInputLenDelta() {
                    return 2;
                }
            });
            state.properties.addStartOfTurnHandler(name, new GameEventHandler() {
                @Override public void handle(GameState state) {
                    var counter = state.getCounterForRead()[counterIdx];
                    var selfDmg = counter >> 16;
                    var blockAmount = counter & ((1 << 16) - 1);
                    if (selfDmg > 0) {
                        state.doNonAttackDamageToPlayer(selfDmg, false, _this);
                        state.playerGainBlock(blockAmount);
                    }
                }
            });
        }
    }

    public static class CrimsonMantle extends _CrimsonMantleT {
        public CrimsonMantle() {
            super("Crimson Mantle", 8);
        }
    }

    public static class CrimsonMantleP extends _CrimsonMantleT {
        public CrimsonMantleP() {
            super("Crimson Mantle+", 10);
        }
    }

    private static abstract class _CrueltyT extends Card {
        private final int bonusPct;

        public _CrueltyT(String cardName, int bonusPct) {
            super(cardName, Card.POWER, 1, Card.RARE);
            this.bonusPct = bonusPct;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += bonusPct;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Cruelty", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 100.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
                @Override public void onRegister(int counterIdx) {
                    state.properties.crueltyCounterIdx = counterIdx;
                }
            });
        }
    }

    public static class Cruelty extends _CrueltyT {
        public Cruelty() {
            super("Cruelty", 25);
        }
    }

    public static class CrueltyP extends _CrueltyT {
        public CrueltyP() {
            super("Cruelty+", 50);
        }
    }

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

    private static abstract class _HellraiserT extends Card {
        public _HellraiserT(String cardName, int energyCost) {
            super(cardName, Card.POWER, energyCost, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx]++;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("Hellraiser", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = state.getCounterForRead()[counterIdx] / 2.0f;
                    return idx + 1;
                }

                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            var isStrike = new boolean[state.properties.cardDict.length];
            for (int i = 0; i < state.properties.strikeCardIdxes.length; i++) {
                isStrike[state.properties.strikeCardIdxes[i]] = true;
            }
            state.properties.addOnCardDrawnHandler("Hellraiser", new GameEventCardHandler() {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    if (state.getCounterForRead()[counterIdx] > 0 && isStrike[cardIdx]) {
                        state.addGameActionToStartOfDeque(new GameEnvironmentAction() {
                            @Override public void doAction(GameState state) {
                                if (!state.removeCardFromHand(cardIdx)) {
                                    return;
                                }
                                var action = state.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                                state.playCard(action, -1, true, null, false, true, -1, -1);
                                while (state.actionCtx == GameActionCtx.SELECT_ENEMY) {
                                    int enemyIdx = GameStateUtils.getRandomEnemyIdx(state, RandomGenCtx.RandomEnemyGeneral);
                                    if (state.properties.makingRealMove || state.properties.stateDescOn) {
                                        state.getStateDesc().append(" -> ").append(state.getEnemiesForRead().get(enemyIdx).getName()).append(" (").append(enemyIdx).append(")");
                                    }
                                    state.playCard(action, enemyIdx, true, null, false, true, -1, -1);
                                }
                            }

                            @Override public boolean canHappenInsideCardPlay() {
                                return true;
                            }
                        });
                    }
                }
            });
        }
    }

    public static class Hellraiser extends _HellraiserT {
        public Hellraiser() {
            super("Hellraiser", 2);
        }
    }

    public static class HellraiserP extends _HellraiserT {
        public HellraiserP() {
            super("Hellraiser+", 1);
        }
    }

    public static class Impervious extends CardIronclad.Impervious {
    }

    public static class ImperviousP extends CardIronclad.ImperviousP {
    }

    public static class Juggernaut extends CardIronclad.Juggernaut {
    }

    public static class JuggernautP extends CardIronclad.JuggernautP {
    }

    private static abstract class _MangleT extends Card {
        private final int damage;
        private final int strengthLoss;

        public _MangleT(String cardName, int damage, int strengthLoss) {
            super(cardName, Card.ATTACK, 3, Card.RARE);
            this.damage = damage;
            this.strengthLoss = strengthLoss;
            entityProperty.selectEnemy = true;
            entityProperty.affectEnemyStrength = true;
            entityProperty.affectEnemyStrengthEot = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var enemy = state.getEnemiesForWrite().getForWrite(idx);
            state.playerDoDamageToEnemy(enemy, damage, this);
            enemy.applyDebuff(state, DebuffType.LOSE_STRENGTH_EOT, strengthLoss);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Mangle extends _MangleT {
        public Mangle() {
            super("Mangle", 15, 10);
        }
    }

    public static class MangleP extends _MangleT {
        public MangleP() {
            super("Mangle+", 20, 15);
        }
    }

    public static class Offering extends CardIronclad.Offering {
    }

    public static class OfferingP extends CardIronclad.OfferingP {
    }

    private static abstract class _OneTwoPunchT extends Card {
        private final int n;

        public _OneTwoPunchT(String cardName, int n) {
            super(cardName, Card.SKILL, 1, Card.RARE);
            this.n = n;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.getCounterForWrite()[counterIdx] += n;
            return GameActionCtx.PLAY_CARD;
        }

        @Override public void gamePropertiesSetup(GameState state) {
            state.properties.registerCounter("OneTwoPunch", this, new GameProperties.NetworkInputHandler() {
                @Override public int addToInput(GameState state, float[] input, int idx) {
                    input[idx] = Math.abs(state.getCounterForRead()[counterIdx]) / 4.0f;
                    return idx + 1;
                }
                @Override public int getInputLenDelta() {
                    return 1;
                }
            });
            state.properties.addOnCardPlayedHandler("OneTwoPunch", new GameEventCardHandler(GameEventCardHandler.CLONE_CARD_PRIORITY) {
                @Override public void handle(GameState state, int cardIdx, int lastIdx, int energyUsed, Class cloneSource, int cloneParentLocation) {
                    var card = state.properties.cardDict[cardIdx];
                    if (cloneSource != null || card.cardType != Card.ATTACK) {
                        return;
                    } else if (state.getCounterForRead()[counterIdx] == 0) {
                        return;
                    }
                    var counters = state.getCounterForWrite();
                    counters[counterIdx] -= 1;
                    state.addGameActionToEndOfDeque(curState -> {
                        var action = curState.properties.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()][cardIdx];
                        curState.playCard(action, lastIdx, true, OneTwoPunch.class, false, false, energyUsed, cloneParentLocation);
                    });
                }
            });
            state.properties.addEndOfTurnHandler("OneTwoPunch", new GameEventHandler() {
                @Override public void handle(GameState state) {
                    if (state.getCounterForRead()[counterIdx] > 0) {
                        state.getCounterForWrite()[counterIdx] = 0;
                    }
                }
            });
        }
    }

    public static class OneTwoPunch extends _OneTwoPunchT {
        public OneTwoPunch() {
            super("One-Two Punch", 1);
        }
    }

    public static class OneTwoPunchP extends _OneTwoPunchT {
        public OneTwoPunchP() {
            super("One-Two Punch+", 2);
        }
    }

    private static abstract class _PactsEndT extends Card {
        private final int damage;

        public _PactsEndT(String cardName, int damage) {
            super(cardName, Card.ATTACK, 0, Card.RARE);
            this.damage = damage;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            for (Enemy enemy : state.getEnemiesForWrite().iterateOverAlive()) {
                state.playerDoDamageToEnemy(enemy, damage, this);
            }
            return GameActionCtx.PLAY_CARD;
        }

        @Override public int energyCost(GameState state) {
            if (state == null) return 0;
            return state.getNumCardsInExhaust() >= 3 ? 0 : -1;
        }
    }

    public static class PactsEnd extends _PactsEndT {
        public PactsEnd() {
            super("Pact's End", 17);
        }
    }

    public static class PactsEndP extends _PactsEndT {
        public PactsEndP() {
            super("Pact's End+", 23);
        }
    }

    private static abstract class _PrimalForceT extends Card {
        public _PrimalForceT(String cardName) {
            super(cardName, Card.SKILL, 0, Card.RARE);
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            var handArr = state.getHandArrForWrite();
            for (int i = 0; i < state.handArrLen; i++) {
                if (state.properties.cardDict[handArr[i]].cardType == Card.ATTACK) {
                    handArr[i] = (short) generatedCardIdx;
                }
            }
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class PrimalForce extends _PrimalForceT {
        public PrimalForce() {
            super("Primal Force");
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.GiantRock());
        }
    }

    public static class PrimalForceP extends _PrimalForceT {
        public PrimalForceP() {
            super("Primal Force+");
        }

        @Override public List<Card> getPossibleGeneratedCards(GameProperties properties, List<Card> cards) {
            return List.of(new CardColorless2.GiantRockP());
        }
    }

    private static abstract class _PyreT extends Card {
        private final int energy;

        public _PyreT(String cardName, int energy) {
            super(cardName, Card.POWER, 2, Card.RARE);
            this.energy = energy;
            this.entityProperty.changeEnergyRefill = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            state.energyRefill += energy;
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Pyre extends _PyreT {
        public Pyre() {
            super("Pyre", 1);
        }
    }

    public static class PyreP extends _PyreT {
        public PyreP() {
            super("Pyre+", 2);
        }
    }

    private static abstract class _StokeT extends Card {
        public _StokeT(String cardName, int energyCost) {
            super(cardName, Card.SKILL, energyCost, Card.RARE);
            exhaustWhenPlayed = true;
            canExhaustAnyCard = true;
        }

        public GameActionCtx play(GameState state, int idx, int energyUsed) {
            int c = state.handArrLen;
            for (int i = 0; i < c; i++) {
                state.exhaustCardFromHandByPosition(i, false);
            }
            state.updateHandArr();
            state.draw(c);
            return GameActionCtx.PLAY_CARD;
        }
    }

    public static class Stoke extends _StokeT {
        public Stoke() {
            super("Stoke", 1);
        }
    }

    public static class StokeP extends _StokeT {
        public StokeP() {
            super("Stoke+", 0);
        }
    }

    // No need to implement Tank: Multiplayer

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
            state.playerDoDamageToEnemy(enemy, damage, this);
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
