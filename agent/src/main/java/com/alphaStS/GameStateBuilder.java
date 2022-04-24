package com.alphaStS;

import com.alphaStS.enemy.Enemy;
import com.alphaStS.player.Player;

import java.util.ArrayList;
import java.util.List;

public class GameStateBuilder {
    private Player player = null;
    private List<CardCount> cards = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Relic> relics = new ArrayList<>();
    private List<Potion> potions = new ArrayList<>();
    private GameStateRandomization randomization = null;
    private GameStateRandomization startOfGameScenarios = null;

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

    public void setStartOfGameScenarios(GameStateRandomization randomization) {
        startOfGameScenarios = randomization;
    }

    public GameStateRandomization getStartOfGameScenarios() {
        return startOfGameScenarios;
    }
}
