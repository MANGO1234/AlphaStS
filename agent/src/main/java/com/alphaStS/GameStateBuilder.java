package com.alphaStS;

import com.alphaStS.card.Card;
import com.alphaStS.card.CardSilent;
import com.alphaStS.enemy.*;
import com.alphaStS.entity.Potion;
import com.alphaStS.entity.Relic;
import com.alphaStS.enums.CharacterEnum;
import com.alphaStS.eventHandler.GameEventHandler;
import com.alphaStS.player.Player;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class GameStateBuilder {
    private Player player = null;
    private CharacterEnum character = CharacterEnum.IRONCLAD;
    private List<Card> cards = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<EnemyEncounter> enemiesEncounters = new ArrayList<>();
    private List<Relic> relics = new ArrayList<>();
    private List<Potion> potions = new ArrayList<>();
    private Potion.PotionGenerator potionsGenerator;
    private GameStateRandomization randomization = null;
    private GameStateRandomization preBattleRandomization = null;
    private GameStateRandomization preBattleGameScenarios = null;
    private GameEventHandler endOfPreBattleSetupHandler = null;
    private int[][] potionsScenarios;
    private boolean isBurningElite;
    private Function<GameState, GameState> switchBattleHandler;
    private int generateCardOptions = 0;

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
        for (int i = 0; i < count; i++) {
            cards.add(card);
        }
    }

    public void addCard(Card... cards) {
        for (Card card : cards) {
            this.cards.add(card);
        }
    }

    public List<Card> getStartingCards() {
        return new ArrayList<>(cards);
    }

    public List<Card> getStartingCards(Card... extraCards) {
        var cards = new ArrayList<>(List.of(extraCards));
        cards.addAll(this.cards);
        return cards;
    }

    public List<Card> getCards() {
        return cards;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void addEnemyEncounter(PredefinedEncounter encounterEnum, Enemy... enemies) {
        addEnemyEncounterInternal(encounterEnum, null, enemies);
    }

    public void addEnemyEncounter(PredefinedEncounter encounterEnum, GameStateRandomization randomization, Enemy... enemies) {
        addEnemyEncounterInternal(encounterEnum, randomization, enemies);
    }

    public void addEnemyEncounter(Enemy... enemies) {
        addEnemyEncounterInternal(null, null, enemies);
    }

    public void addEnemyEncounter(GameStateRandomization randomization, Enemy... enemies) {
        addEnemyEncounterInternal(null, randomization, enemies);
    }

    public void addEnemyEncounter(PredefinedEncounter encounter) {
        if (encounter.enemies != null && !encounter.enemies.isEmpty()) {
            addEnemyEncounter(encounter, encounter.enemies.stream().map(Enemy::copy).toArray(Enemy[]::new));
        } else if (encounter.enemiesSupplier != null) {
            addEnemyEncounter(encounter, encounter.enemiesSupplier.get().toArray(Enemy[]::new));
        } else {
            throw new IllegalArgumentException();
        }
        if (encounter.encounterExtraLogic != null) {
            encounter.encounterExtraLogic.accept(this);
        }
    }

    public void addEnemyEncounterInternal(PredefinedEncounter encounterEnum, GameStateRandomization randomization, Enemy... enemies) {
        var indexes = new ArrayList<EnemyEncounter.EnemyInfo>();
        if (encounterEnum == PredefinedEncounter.GREMLIN_LEADER) {
            indexes.add(new EnemyEncounter.EnemyInfo(this.enemies.size(), true));
            indexes.add(new EnemyEncounter.EnemyInfo(this.enemies.size() + 1, true));
            indexes.add(new EnemyEncounter.EnemyInfo(this.enemies.size() + 2, true));
            indexes.add(new EnemyEncounter.EnemyInfo(this.enemies.size() + 3, false));
        } else if (encounterEnum == PredefinedEncounter.GREMLIN_GANG) {
            for (int i = 0; i < enemies.length; i++) {
                indexes.add(new EnemyEncounter.EnemyInfo(this.enemies.size() + i, false));
            }
        } else {
            for (int i = 0; i < enemies.length; i++) {
                indexes.add(new EnemyEncounter.EnemyInfo(this.enemies.size() + i, false));
            }
        }
        var encounter = new EnemyEncounter(encounterEnum, indexes);
        encounter.randomization = randomization;
        if (encounterEnum != null) {
            encounter.reordering = encounterEnum.reordering;
        }
        enemiesEncounters.add(encounter);
        this.enemies.addAll(List.of(enemies));
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
            for (int i = 0; i < enemiesEncounters.size(); i++) {
                var encounter = enemiesEncounters.get(i);
                if (encounter.encounterEnum == PredefinedEncounter.GREMLIN_LEADER) {
                    r = r.followByIf(i, new GremlinLeaderRandomization2(enemies, encounter.idxes.get(0).index()).collapse("Random Gremlins"));
                } else if (encounter.encounterEnum == PredefinedEncounter.GREMLIN_GANG) {
                    r = r.followByIf(i, new GremlinGangRandomization(enemies, encounter.idxes.get(0).index()));
                }
            }
            if (isBurningElite) {
                for (Enemy enemy : enemies) {
                    if (enemy.properties.isElite) {
                        enemy.markAsBurningElite();
                    }
                }
                r = new GameStateRandomization.BurningEliteRandomization().doAfter(r);
            }
            for (int i = 0; i < enemiesEncounters.size(); i++) {
                var encounter = enemiesEncounters.get(i);
                if (encounter.randomization != null) {
                    r = r.followByIf(i, encounter.randomization);
                }
            }
            randomization = randomization == null ? r : randomization.doAfter(r);
        } else if (enemiesEncounters.size() == 1) {
            GameStateRandomization r = null;
            var encounter = enemiesEncounters.get(0);
            if (encounter.encounterEnum == PredefinedEncounter.GREMLIN_LEADER) {
                r = new GremlinLeaderRandomization2(enemies, encounter.idxes.get(0).index()).collapse("Random Gremlins");
            } else if (encounter.encounterEnum == PredefinedEncounter.GREMLIN_GANG) {
                r = new GremlinGangRandomization(enemies, encounter.idxes.get(0).index());
            }
            if (isBurningElite) {
                for (Enemy enemy : enemies) {
                    if (enemy.properties.isElite) {
                        enemy.markAsBurningElite();
                    }
                }
                r = new GameStateRandomization.BurningEliteRandomization().doAfter(r);
            }
            if (encounter.randomization != null) {
                r = encounter.randomization.doAfter(r);
            }
            randomization = randomization == null ? r : randomization.doAfter(r);
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
        for (Card card : cards) {
            if (card.cardName.startsWith("Alchemize") && card instanceof CardSilent._AlchemizeT alchemize) {
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

    public void setPotionsScenarios(int[]... scenarios) {
        potionsScenarios = scenarios;
    }

    public int[][] getPotionsScenarios() {
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
            StringBuilder descBuilder = new StringBuilder();
            for (Relic relic : relics) {
                if (relic.preBattleScenariosEnabled != null &&
                    i < relic.preBattleScenariosEnabled.length &&
                    relic.preBattleScenariosEnabled[i]) {
                    descBuilder.append("Enable ").append(relic).append(", ");
                }
            }
            descBuilder.append(info.get(i).desc());
            desc[i] = descBuilder.toString();
        }
        preBattleGameScenarios = randomization.join(new GameStateRandomization.SimpleCustomRandomization(setters)).setDescriptions(desc);
    }

    public GameStateRandomization getPreBattleScenarios() {
        return preBattleGameScenarios;
    }

    public void setCharacter(CharacterEnum character) {
        this.character = character;
    }

    public CharacterEnum getCharacter() {
        return character;
    }

    public void setGenerateCardOptions(int generateCardOptions) {
        this.generateCardOptions = generateCardOptions;
    }

    public int getGenerateCardOptions() {
        return generateCardOptions;
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
