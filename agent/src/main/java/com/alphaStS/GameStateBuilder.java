package com.alphaStS;

import com.alphaStS.card.Card;
import com.alphaStS.card.CardCount;
import com.alphaStS.card.CardSilent;
import com.alphaStS.enemy.Enemy;
import com.alphaStS.enemy.EnemyCity;
import com.alphaStS.enemy.EnemyEncounter;
import com.alphaStS.enemy.EnemyExordium;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.player.Player;
import com.alphaStS.utils.Tuple;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class GameStateBuilder {
    private Player player = null;
    private CharacterEnum character = CharacterEnum.IRONCLAD;
    private List<CardCount> cards = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<EnemyEncounter> enemiesEncounters = new ArrayList<>();
    private List<Integer> gremlinEncounterFightIndexes = new ArrayList<>();
    private List<Integer> gremlinGangEncounterIndexes = new ArrayList<>();
    private List<Relic> relics = new ArrayList<>();
    private List<Potion> potions = new ArrayList<>();
    private Potion.PotionGenerator potionsGenerator;
    private List<BiConsumer<GameState, int[]>> enemyReorderings = new ArrayList<>();
    private GameStateRandomization randomization = null;
    private GameStateRandomization preBattleRandomization = null;
    private GameStateRandomization preBattleGameScenarios = null;
    private GameEventHandler endOfPreBattleSetupHandler = null;
    private int[] potionsScenarios;
    private boolean isBurningElite;
    private Function<GameState, GameState> switchBattleHandler;

    public void setSwitchBattleHandler(Function<GameState, GameState> switchBattleHandler) {
        this.switchBattleHandler = switchBattleHandler;
    }

    public Function<GameState, GameState> getSwitchBattleHandler() {
        return switchBattleHandler;
    }

    public GameStateBuilder() {
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void addCard(Card card, int count) {
        cards.add(new CardCount(card, count));
    }

    public List<Card> getStartingCards() {
        return this.cards.stream().map(CardCount::card).toList();
    }

    public List<Card> getStartingCards(Card... extraCards) {
        var cards = new ArrayList<>(List.of(extraCards));
        cards.addAll(this.cards.stream().map(CardCount::card).toList());
        return cards;
    }

    public List<CardCount> getCards() {
        return cards;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void addEnemyEncounter(EnemyEncounter.EncounterEnum encounterEnum, Enemy... enemies) {
        addEnemyEncounter(-1, encounterEnum, enemies);
    }

    public void addEnemyEncounter(int startingHealth, EnemyEncounter.EncounterEnum encounterEnum, Enemy... enemies) {
        var indexes = new ArrayList<Tuple<Integer, Integer>>();
        boolean isGremlinLeaderFight = false;
        boolean isGremlinGangFight = false;
        for (Enemy enemy : enemies) {
            if (enemy instanceof EnemyExordium.FatGremlin) {
                isGremlinGangFight = true;
                break;
            }
            if (enemy instanceof EnemyCity.GremlinLeader) {
                isGremlinLeaderFight = true;
                break;
            }
        }
        if (isGremlinLeaderFight) {
            indexes.add(new Tuple<>(this.enemies.size(), 0));
            indexes.add(new Tuple<>(this.enemies.size() + 1, 0));
            indexes.add(new Tuple<>(this.enemies.size() + 2, 0));
            indexes.add(new Tuple<>(this.enemies.size() + 3, -1));
            gremlinEncounterFightIndexes.add(enemiesEncounters.size());
        } else if (isGremlinGangFight) {
            for (int i = 0; i < enemies.length; i++) {
                indexes.add(new Tuple<>(this.enemies.size() + i, -1));
            }
            gremlinGangEncounterIndexes.add(enemiesEncounters.size());
        } else {
            for (int i = 0; i < enemies.length; i++) {
                indexes.add(new Tuple<>(this.enemies.size() + i, -1));
            }
        }
        enemiesEncounters.add(new EnemyEncounter(startingHealth, encounterEnum, indexes));
        this.enemies.addAll(List.of(enemies));
    }

    public void addEnemyEncounter(Enemy... enemies) {
        addEnemyEncounter(EnemyEncounter.EncounterEnum.UNKNOWN, enemies);
    }

    public void addEnemyEncounter(int startingHealth, Enemy... enemies) {
        addEnemyEncounter(startingHealth, EnemyEncounter.EncounterEnum.UNKNOWN, enemies);
    }

    public List<EnemyEncounter> getEnemiesEncounters() {
        return enemiesEncounters;
    }

    public void setBurningElite() {
        isBurningElite = true;
    }

    public void build(GameState state) {
        if (enemiesEncounters.size() > 1) {
            GameStateRandomization r = new GameStateRandomization.EnemyEncounterRandomization(enemies, enemiesEncounters);
            for (var gremlinLeaderFightIndex : gremlinEncounterFightIndexes) {
                var encounter = enemiesEncounters.get(gremlinLeaderFightIndex);
                r = r.followByIf(gremlinLeaderFightIndex, new EnemyEncounter.GremlinLeaderRandomization2(enemies, encounter.idxes.get(0).v1()).collapse("Random Gremlins"));
            }
            for (var gremlinGangEncounterIndexes : gremlinGangEncounterIndexes) {
                var encounter = enemiesEncounters.get(gremlinGangEncounterIndexes);
                r = r.followByIf(gremlinGangEncounterIndexes, new EnemyEncounter.GremlinGangRandomization(enemies, encounter.idxes.get(0).v1()));
            }
            randomization = randomization == null ? r : randomization.doAfter(r);
        } else if (enemiesEncounters.size() == 1) {
            if (gremlinEncounterFightIndexes.size() > 0) {
                var encounter = enemiesEncounters.get(gremlinEncounterFightIndexes.get(0));
                var r = new EnemyEncounter.GremlinLeaderRandomization2(enemies, encounter.idxes.get(0).v1()).collapse("Random Gremlins");
                randomization = randomization == null ? r : randomization.doAfter(r);
            } else if (gremlinGangEncounterIndexes.size() > 0) {
                var encounter = enemiesEncounters.get(gremlinGangEncounterIndexes.get(0));
                var r = new EnemyEncounter.GremlinGangRandomization(enemies, encounter.idxes.get(0).v1());
                randomization = randomization == null ? r : randomization.doAfter(r);
            }
            state.currentEncounter = enemiesEncounters.get(0).encounterEnum;
        }
        if (isBurningElite) {
            for (int i = 0; i < enemies.size(); i++) {
                enemies.get(i).markAsBurningElite();
            }
            randomization = new GameStateRandomization.BurningEliteRandomization().doAfter(randomization);
        }
    }

    public void addRelic(Relic relic) {
        relics.add(relic);
    }

    public List<Relic> getRelics() {
        return relics;
    }

    public void addPotion(Potion potion) {
        potions.add(potion);
    }

    public List<Potion> getPotions() {
        int maxPotionSlot = relics.stream().anyMatch((x) -> x instanceof Relic.PotionBelt) ? 4 : 2;
        for (Relic relic : relics) {
            if (relic instanceof Relic.LizardTail) {
                potions.add(new Potion.LizardTail());
            }
        }
        boolean canGeneratePotion = false;
        int basePenaltyRatio = 0;
        int possibleGeneratedPotions = 0;
        for (int i = 0; i < potions.size(); i++) {
            if (potions.get(i) instanceof Potion.EntropicBrew pot) {
                canGeneratePotion = true;
                basePenaltyRatio = Math.max(basePenaltyRatio, pot.basePenaltyRatio);
                possibleGeneratedPotions |= pot.possibleGeneratedPotions;
                break;
            }
        }
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).card().cardName.startsWith("Alchemize") && cards.get(i).card() instanceof CardSilent._AlchemizeT alchemize) {
                if (alchemize.basePenaltyRatio > 0) {
                    canGeneratePotion = true;
                    basePenaltyRatio = Math.max(basePenaltyRatio, alchemize.basePenaltyRatio);
                    possibleGeneratedPotions |= alchemize.possibleGeneratedPotions;
                    break;
                }
            }
        }
        if (canGeneratePotion) {
            potionsGenerator = new Potion.PotionGenerator(possibleGeneratedPotions);
            potionsGenerator.initPossibleGeneratedPotions(character, getPlayer().getMaxHealth(), basePenaltyRatio);
            for (int potionIdx = 0; potionIdx < potionsGenerator.commonPotions.size(); potionIdx++) {
                for (int j = 0; j < maxPotionSlot; j++) {
                    potions.add(potionsGenerator.commonPotions.get(potionIdx).get(j));
                }
            }
            for (int potionIdx = 0; potionIdx < potionsGenerator.uncommonPotions.size(); potionIdx++) {
                for (int j = 0; j < maxPotionSlot; j++) {
                    potions.add(potionsGenerator.uncommonPotions.get(potionIdx).get(j));
                }
            }
            for (int potionIdx = 0; potionIdx < potionsGenerator.rarePotions.size(); potionIdx++) {
                for (int j = 0; j < maxPotionSlot; j++) {
                    potions.add(potionsGenerator.rarePotions.get(potionIdx).get(j));
                }
            }
        }
        return potions;
    }

    public Potion.PotionGenerator getPotionsGenerator() {
        return potionsGenerator;
    }

    public void setPotionsScenarios(int... scenarios) {
        potionsScenarios = scenarios;
    }

    public int[] getPotionsScenarios() {
        return potionsScenarios;
    }

    public void setRandomization(GameStateRandomization randomization) {
        this.randomization = randomization;
    }

    public GameStateRandomization getRandomization() {
        return randomization;
    }

    public void setPreBattleRandomization(GameStateRandomization randomization) {
        this.preBattleRandomization = randomization;
    }

    public GameStateRandomization getPreBattleRandomization() {
        return preBattleRandomization;
    }

    public void setPreBattleScenarios(GameStateRandomization randomization) {
        List<Consumer<GameState>> setters = new ArrayList<>();
        var info = randomization.listRandomizations();
        var desc = new String[info.size()];
        for (int i = 0; i < info.size(); i++) {
            var _i = i;
            setters.add((state) -> state.preBattleScenariosChosenIdx = _i);
            desc[i] = info.get(i).desc();
        }
        preBattleGameScenarios = randomization.join(new GameStateRandomization.SimpleCustomRandomization(setters)).setDescriptions(desc);
    }

    public GameStateRandomization getPreBattleScenarios() {
        return preBattleGameScenarios;
    }

    public void addEnemyReordering(BiConsumer<GameState, int[]> reordering) {
        enemyReorderings.add(reordering);
    }

    public List<BiConsumer<GameState, int[]>> getEnemyReordering() {
        return enemyReorderings;
    }

    public void setCharacter(CharacterEnum character) {
        this.character = character;
    }

    public CharacterEnum getCharacter() {
        return character;
    }

    public GameEventHandler getEndOfPreBattleSetupHandler() {
        return endOfPreBattleSetupHandler;
    }

    public void setEndOfPreBattleSetupHandler(GameEventHandler endOfPreBattleSetupHandler) {
        this.endOfPreBattleSetupHandler = endOfPreBattleSetupHandler;
    }

    public void setGameStateViaInteractiveMode(List<String> commands, boolean trainWholeBattle) {
        setEndOfPreBattleSetupHandler(new GameEventHandler() {
            @Override public void handle(GameState state) {
//                if (state.properties.isTraining && state.skipInteractiveModeSetup) {
                if (state.skipInteractiveModeSetup) {
                    return;
                }
                state.clearAllSearchInfo();
                new InteractiveMode(new PrintStream(OutputStream.nullOutputStream())).interactiveApplyHistory(state, commands);
            }
        });
        if (trainWholeBattle) {
            preBattleRandomization = new GameStateRandomization.SimpleCustomRandomization(List.of(
                    (state) -> state.skipInteractiveModeSetup = true
            )).union(0.2, new GameStateRandomization.SimpleCustomRandomization(List.of(
                    (state) -> {}
            ))).setDescriptions("Skip interactive mode setup", "Interactive mode setup").doAfter(preBattleRandomization);
        }
    }

    public void setGameStateViaInteractiveMode(List<String> commands) {
        setGameStateViaInteractiveMode(commands, false);
    }


    public List<List<String>> perScenarioCommands;
    public void setPerScenarioCommands(List<List<String>> perScenarioCommands) {
        this.perScenarioCommands = perScenarioCommands;
    }

    public List<List<String>> getPerScenarioCommands() {
        return perScenarioCommands;
    }
}
