package com.alphaStS;

import com.alphaStS.enemy.Enemy;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class GameStateBuilder {
    private Player player = null;
    private CharacterEnum character = CharacterEnum.IRONCLAD;
    private List<CardCount> cards = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Relic> relics = new ArrayList<>();
    private List<Potion> potions = new ArrayList<>();
    private List<BiConsumer<GameState, int[]>> enemyReorderings = new ArrayList<>();
    private GameStateRandomization randomization = null;
    private GameStateRandomization preBattleRandomization = null;
    private GameStateRandomization preBattleGameScenarios = null;

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

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    public List<Enemy> getEnemies() {
        return enemies;
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
        preBattleGameScenarios = randomization;
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
}
