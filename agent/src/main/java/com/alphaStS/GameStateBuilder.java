package com.alphaStS;

import com.alphaStS.card.Card;
import com.alphaStS.card.CardCount;
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

public class GameStateBuilder {
    private Player player = null;
    private CharacterEnum character = CharacterEnum.IRONCLAD;
    private List<CardCount> cards = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<List<Tuple<Integer, Integer>>> enemiesEncountersRandomizationIndexes = new ArrayList<>();
    private List<Integer> gremlinLeaderFightIndexes = new ArrayList<>();
    private List<Integer> gremlinGangFightIndexes = new ArrayList<>();
    private List<Relic> relics = new ArrayList<>();
    private List<Potion> potions = new ArrayList<>();
    private List<BiConsumer<GameState, int[]>> enemyReorderings = new ArrayList<>();
    private GameStateRandomization randomization = null;
    private GameStateRandomization preBattleRandomization = null;
    private GameStateRandomization preBattleGameScenarios = null;
    private GameEventHandler startOfGameSetupHandler = null;
    private GameEventHandler endOfPreBattleSetupHandler = null;
    private int[] potionsScenarios;
    private List<List<Tuple<Integer, Integer>>> enemiesEncountersIdx;
    private boolean isBurningElite;

    public void setEnemiesEncountersIdx(List<List<Tuple<Integer, Integer>>> enemiesEncountersIdx) {
        this.enemiesEncountersIdx = enemiesEncountersIdx;
    }

    public List<List<Tuple<Integer, Integer>>> getEnemiesEncountersIdx() {
        return enemiesEncountersIdx;
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

    public List<CardCount> getCards() {
        return cards;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void addEnemyEncounter(Enemy... enemies) {
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
            gremlinLeaderFightIndexes.add(enemiesEncountersRandomizationIndexes.size());
        } else if (isGremlinGangFight) {
            for (int i = 0; i < enemies.length; i++) {
                indexes.add(new Tuple<>(this.enemies.size() + i, -1));
            }
            gremlinGangFightIndexes.add(enemiesEncountersRandomizationIndexes.size());
        } else {
            for (int i = 0; i < enemies.length; i++) {
                indexes.add(new Tuple<>(this.enemies.size() + i, -1));
            }
        }
        enemiesEncountersRandomizationIndexes.add(indexes);
        this.enemies.addAll(List.of(enemies));
    }

    public void setBurningElite() {
        isBurningElite = true;
    }

    public void build() {
        if (enemiesEncountersRandomizationIndexes.size() > 1) {
            GameStateRandomization r = new GameStateRandomization.EnemyEncounterRandomization(enemies, enemiesEncountersRandomizationIndexes);
            for (var gremlinLeaderFightIndex : gremlinLeaderFightIndexes) {
                var idxes = enemiesEncountersRandomizationIndexes.get(gremlinLeaderFightIndex);
                r = r.followByIf(gremlinLeaderFightIndex, new EnemyEncounter.GremlinLeaderRandomization2(enemies, idxes.get(0).v1()).collapse("Random Gremlins"));
            }
            for (var gremlinGangFightIndexes : gremlinGangFightIndexes) {
                var idxes = enemiesEncountersRandomizationIndexes.get(gremlinGangFightIndexes);
                r = r.followByIf(gremlinGangFightIndexes, new EnemyEncounter.GremlinGangRandomization(enemies, idxes.get(0).v1()));
            }
            randomization = randomization == null ? r : randomization.doAfter(r);
        } else if (enemiesEncountersRandomizationIndexes.size() == 1) {
            if (gremlinLeaderFightIndexes.size() > 0) {
                var idxes = enemiesEncountersRandomizationIndexes.get(gremlinLeaderFightIndexes.get(0));
                var r = new EnemyEncounter.GremlinLeaderRandomization2(enemies, idxes.get(0).v1()).collapse("Random Gremlins");
                randomization = randomization == null ? r : randomization.doAfter(r);
            } else if (gremlinGangFightIndexes.size() > 0) {
                var idxes = enemiesEncountersRandomizationIndexes.get(gremlinGangFightIndexes.get(0));
                var r = new EnemyEncounter.GremlinGangRandomization(enemies, idxes.get(0).v1());
                randomization = randomization == null ? r : randomization.doAfter(r);
            }
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
        return potions;
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

    public void setStartOfGameSetup(GameEventHandler handler) {
        startOfGameSetupHandler = handler;
    }

    public GameEventHandler getStartOfGameSetup() {
        return startOfGameSetupHandler;
    }

    public GameEventHandler getEndOfPreBattleSetupHandler() {
        return endOfPreBattleSetupHandler;
    }

    public void setEndOfPreBattleSetupHandler(GameEventHandler endOfPreBattleSetupHandler) {
        this.endOfPreBattleSetupHandler = endOfPreBattleSetupHandler;
    }

    public void setGameStateViaInteractiveMode(List<String> commands) {
        setEndOfPreBattleSetupHandler(new GameEventHandler() {
            @Override public void handle(GameState state) {
                state.clearAllSearchInfo();
                new InteractiveMode(new PrintStream(OutputStream.nullOutputStream())).interactiveApplyHistory(state, commands);
            }
        });
    }
}
