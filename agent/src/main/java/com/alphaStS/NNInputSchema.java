package com.alphaStS;

import com.alphaStS.card.Card;
import com.alphaStS.enemy.*;
import com.alphaStS.enums.*;
import com.alphaStS.gameAction.GameAction;
import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.gameAction.GameActionType;

import java.util.ArrayList;
import java.util.List;

public class NNInputSchema {
    static final int MAX_AGENT_DECK_ORDER_MEMORY = 1;
    static final int MAX_AGENT_DECK_ORDER_MEMORY_DUPLICATE_CARDS = 1;

    @FunctionalInterface
    interface NNModuleAction {
        int apply(GameState state, float[] x, int idx);
    }

    interface EnemyInputModule {
        int getLength(GameProperties props, EnemyReadOnly enemy);
        String getDescription(GameProperties props, EnemyReadOnly enemy);
        int fill(GameState state, EnemyReadOnly enemy, float[] x, int idx);
        int print(GameState state, EnemyReadOnly enemy, float[] input, int idx);

        default int fillDead(GameState state, EnemyReadOnly enemy, float[] x, int idx) {
            int len = getLength(state.properties, enemy);
            for (int i = 0; i < len; i++) x[idx + i] = -0.1f;
            return len;
        }
    }

    int inputLen;
    String description;
    List<NNModuleAction> inputModules = new ArrayList<>();
    List<NNModuleAction> inputPrinters = new ArrayList<>();
    List<List<EnemyInputModule>> perEnemyModules = new ArrayList<>();

    void setup(GameState state) {
        var props = state.properties;
        var enemies = state.getEnemiesForRead();
        inputLen = 0;
        var descBody = new StringBuilder();

        // --- Player/Game modules ---

        // Turn number
        if (Configuration.ADD_CURRENT_TURN_NUM_TO_NN_INPUT) {
            inputLen++;
            descBody.append("    1 input to keep track of current turn number\n");
            if (props.isHeartGauntlet) {
                inputLen++;
                descBody.append("    1 input to keep track of real current turn number\n");
                inputModules.add((s, x, idx) -> {
                    x[idx] = s.turnNum / 50.0f;
                    x[idx + 1] = s.realTurnNum / s.properties.maxPossibleRealTurnsLeft;
                    return 2;
                });
                inputPrinters.add((s, input, idx) -> {
                    System.out.println("Turn Number: " + input[idx]);
                    System.out.println("Real Turn Number: " + input[idx + 1]);
                    return 2;
                });
            } else {
                inputModules.add((s, x, idx) -> {
                    x[idx] = s.turnNum / 50.0f;
                    return 1;
                });
                inputPrinters.add((s, input, idx) -> {
                    System.out.println("Turn Number: " + input[idx]);
                    return 1;
                });
            }
        }

        // Pre-battle scenarios
        if (props.preBattleScenariosBackup != null) {
            int size = props.preBattleScenariosBackup.listRandomizations().size();
            inputLen += size;
            descBody.append("    ").append(size).append(" inputs to keep track of scenario chosen\n");
            inputModules.add((s, x, idx) -> {
                int sz = s.properties.preBattleScenariosBackup.listRandomizations().size();
                if (s.preBattleScenariosChosenIdx >= 0) {
                    x[idx + s.preBattleScenariosChosenIdx] = 0.5f;
                }
                return sz;
            });
            inputPrinters.add((s, input, idx) -> {
                int sz = s.properties.preBattleScenariosBackup.listRandomizations().size();
                System.out.print("Pre-battle Scenarios: [");
                for (int i = 0; i < sz; i++) {
                    if (i > 0) System.out.print(", ");
                    System.out.print(input[idx + i]);
                }
                System.out.println("]");
                return sz;
            });
        }

        // Start of battle actions
        {
            int size = props.startOfBattleActions.size();
            inputLen += size;
            inputModules.add((s, x, idx) -> {
                int sz = s.properties.startOfBattleActions.size();
                if (s.startOfBattleActionIdx < sz) {
                    x[idx + s.startOfBattleActionIdx] = 0.5f;
                }
                return sz;
            });
            if (size > 0) {
                inputPrinters.add((s, input, idx) -> {
                    int sz = s.properties.startOfBattleActions.size();
                    System.out.print("Start of Battle Actions: [");
                    for (int i = 0; i < sz; i++) {
                        if (i > 0) System.out.print(", ");
                        System.out.print(input[idx + i]);
                    }
                    System.out.println("]");
                    return sz;
                });
            } else {
                inputPrinters.add((s, input, idx) -> 0);
            }
        }

        // Cards in deck
        {
            int len = props.realCardsLen;
            inputLen += len;
            descBody.append("    ").append(len).append(" inputs for cards in deck\n");
            inputModules.add((s, x, idx) -> {
                for (int i = 0; i < s.deckArrLen; i++) {
                    x[idx + s.deckArr[i]] += 0.1f;
                }
                return s.properties.realCardsLen;
            });
            inputPrinters.add((s, input, idx) -> {
                int l = s.properties.realCardsLen;
                System.out.print("Cards in Deck: [");
                for (int i = 0; i < l; i++) {
                    if (i > 0) System.out.print(", ");
                    System.out.print(input[idx + i]);
                }
                System.out.println("]");
                return l;
            });
        }

        // Cards in hand
        {
            int len = props.cardDict.length;
            inputLen += len;
            descBody.append("    ").append(len).append(" inputs for cards in hand\n");
            inputModules.add((s, x, idx) -> {
                for (int i = 0; i < s.handArrLen; i++) {
                    x[idx + s.handArr[i]] += 0.1f;
                }
                return s.properties.cardDict.length;
            });
            inputPrinters.add((s, input, idx) -> {
                int l = s.properties.cardDict.length;
                System.out.print("Cards in Hand: [");
                for (int i = 0; i < l; i++) {
                    if (i > 0) System.out.print(", ");
                    System.out.print(input[idx + i]);
                }
                System.out.println("]");
                return l;
            });
        }

        // Hand size
        inputLen += 1;
        descBody.append("    1 input for number of cards in hand\n");
        inputModules.add((s, x, idx) -> {
            x[idx] = (s.handArrLen - 5) / 10.0f;
            return 1;
        });
        inputPrinters.add((s, input, idx) -> {
            System.out.println("Hand Size: " + input[idx]);
            return 1;
        });

        // Deck size
        inputLen += 1;
        descBody.append("    1 input for number of cards in deck\n");
        inputModules.add((s, x, idx) -> {
            x[idx] = s.getNumCardsInDeck() / 40.0f;
            return 1;
        });
        inputPrinters.add((s, input, idx) -> {
            System.out.println("Deck Size: " + input[idx]);
            return 1;
        });

        // Discard size
        if (props.cardInDiscardInNNInput) {
            inputLen += 1;
            descBody.append("    1 input for number of cards in discard\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.getNumCardsInDiscard() / 40.0f;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Discard Size: " + input[idx]);
                return 1;
            });
        }

        // Cards in discard
        {
            int len = props.discardIdxes.length;
            inputLen += len;
            descBody.append("    ").append(len).append(" inputs to keep track of cards in discard\n");
            inputModules.add((s, x, idx) -> {
                int l = s.properties.discardIdxes.length;
                for (int i = 0; i < s.discardArrLen; i++) {
                    x[idx + s.properties.discardReverseIdxes[s.discardArr[i]]] += 0.1f;
                }
                return l;
            });
            inputPrinters.add((s, input, idx) -> {
                int l = s.properties.discardIdxes.length;
                System.out.print("Cards in Discard: [");
                for (int i = 0; i < l; i++) {
                    if (i > 0) System.out.print(", ");
                    System.out.print(input[idx + i]);
                }
                System.out.println("]");
                return l;
            });
        }

        // Cards in exhaust
        if (props.anyEntityProperty.selectFromExhaust) {
            int len = props.realCardsLen;
            inputLen += len;
            descBody.append("    ").append(len).append(" inputs for cards in exhaust\n");
            inputModules.add((s, x, idx) -> {
                int l = s.properties.realCardsLen;
                for (int i = 0; i < s.exhaustArrLen; i++) {
                    x[idx + s.exhaustArr[i]] += 0.1f;
                }
                return l;
            });
            inputPrinters.add((s, input, idx) -> {
                int l = s.properties.realCardsLen;
                System.out.print("Cards in Exhaust: [");
                for (int i = 0; i < l; i++) {
                    if (i > 0) System.out.print(", ");
                    System.out.print(input[idx + i]);
                }
                System.out.println("]");
                return l;
            });
        }

        // Chosen cards (Well Laid Plans)
        if (state.chosenCardsArr != null) {
            int len = props.cardDict.length;
            inputLen += len;
            descBody.append("    ").append(len).append(" inputs for cards that was chosen by Well Laid Plans\n");
            inputModules.add((s, x, idx) -> {
                int l = s.properties.cardDict.length;
                for (int i = 0; i < s.chosenCardsArrLen; i++) {
                    x[idx + s.chosenCardsArr[i]] += 0.1f;
                }
                return l;
            });
            inputPrinters.add((s, input, idx) -> {
                int l = s.properties.cardDict.length;
                System.out.print("Chosen Cards (Well Laid Plans): [");
                for (int i = 0; i < l; i++) {
                    if (i > 0) System.out.print(", ");
                    System.out.print(input[idx + i]);
                }
                System.out.println("]");
                return l;
            });
        }

        // Nightmare cards
        if (state.nightmareCards != null) {
            int len = props.realCardsLen;
            inputLen += len;
            descBody.append("    ").append(len).append(" inputs for cards that was targeted by nightmare\n");
            inputModules.add((s, x, idx) -> {
                int l = s.properties.realCardsLen;
                for (int i = 0; i < s.nightmareCardsLen; i++) {
                    x[idx + s.nightmareCards[i]] += 0.1f;
                }
                return l;
            });
            inputPrinters.add((s, input, idx) -> {
                int l = s.properties.realCardsLen;
                System.out.print("Nightmare Cards: [");
                for (int i = 0; i < l; i++) {
                    if (i > 0) System.out.print(", ");
                    System.out.print(input[idx + i]);
                }
                System.out.println("]");
                return l;
            });
        }

        // Deck order memory
        if (MAX_AGENT_DECK_ORDER_MEMORY > 0 && props.anyEntityProperty.putCardOnTopDeck) {
            int len = props.realCardsLen * MAX_AGENT_DECK_ORDER_MEMORY * MAX_AGENT_DECK_ORDER_MEMORY_DUPLICATE_CARDS;
            inputLen += len;
            descBody.append("    ").append(len).append(" inputs to keep track of known card draw order\n");
            inputModules.add((s, x, idx) -> {
                int l = s.properties.realCardsLen * MAX_AGENT_DECK_ORDER_MEMORY * MAX_AGENT_DECK_ORDER_MEMORY_DUPLICATE_CARDS;
                var tmpIdx = idx;
                for (int i = 0; i < Math.min(5 * MAX_AGENT_DECK_ORDER_MEMORY * MAX_AGENT_DECK_ORDER_MEMORY_DUPLICATE_CARDS, s.deckArrFixedDrawLen); i += 5) {
                    for (int j = i; j < Math.min(i + 5, s.deckArrFixedDrawLen); j++) {
                        int cardIdx = s.deckArr[s.deckArrLen - 1 - j];
                        x[tmpIdx + cardIdx] += 0.1f;
                    }
                    tmpIdx += s.properties.realCardsLen;
                }
                return l;
            });
            inputPrinters.add((s, input, idx) -> {
                int l = s.properties.realCardsLen * MAX_AGENT_DECK_ORDER_MEMORY * MAX_AGENT_DECK_ORDER_MEMORY_DUPLICATE_CARDS;
                System.out.print("Deck Order Memory: [");
                for (int i = 0; i < l; i++) {
                    if (i > 0) System.out.print(", ");
                    System.out.print(input[idx + i]);
                }
                System.out.println("]");
                return l;
            });
        }

        // Discard 0 cost card order (disabled)
        if (false && props.discard0CardOrderMatters) {
            int len1 = props.discardOrder0CostNumber * props.discardOrder0CardMaxCopies;
            int len2 = props.discardOrder0CostNumber * props.discardOrder0CardMaxCopies * props.discardOrderMaxKeepTrackIn10s;
            inputLen += len1 + len2;
            descBody.append("    ").append(len1).append(" inputs to keep track of discard 0 cost cards order in hand\n");
            descBody.append("    ").append(len2).append(" (")
                    .append(props.discardOrder0CostNumber).append(" * ")
                    .append(props.discardOrder0CardMaxCopies).append(" * ")
                    .append(props.discardOrderMaxKeepTrackIn10s)
                    .append(") inputs to keep track of discard 0 cost cards order in discard\n");
            inputModules.add((s, x, idx) -> {
                var p = s.properties;
                int l1 = p.discardOrder0CostNumber * p.discardOrder0CardMaxCopies;
                int l2 = p.discardOrder0CostNumber * p.discardOrder0CardMaxCopies * p.discardOrderMaxKeepTrackIn10s;
                int k = 10;
                for (int i = 0; i < s.handArrLen; i++) {
                    if (p.cardDict[s.handArr[i]].realEnergyCost() == 0) {
                        int j = 0;
                        for (; j < p.discardOrder0CardMaxCopies; j++) {
                            if (x[idx + p.discardOrder0CostNumber * j + p.discardOrder0CardReverseIdx[s.handArr[i]]] == 0) {
                                x[idx + p.discardOrder0CostNumber * j + p.discardOrder0CardReverseIdx[s.handArr[i]]] = k / 10.0f;
                                break;
                            }
                        }
                        if (j == p.discardOrder0CardMaxCopies) {
                            throw new IllegalStateException("Too many 0 cost card copies of " + p.cardDict[s.handArr[i]].cardName);
                        }
                        k--;
                    }
                }
                int dIdx = idx + l1;
                k = 10;
                for (int i = 0; i < s.discardArrLen; i++) {
                    if (p.cardDict[s.discardArr[i]].realEnergyCost() == 0) {
                        int j = 0;
                        for (; j < p.discardOrder0CardMaxCopies; j++) {
                            if (x[dIdx + p.discardOrder0CostNumber * j + p.discardOrder0CardReverseIdx[s.discardArr[i]]] == 0) {
                                x[dIdx + p.discardOrder0CostNumber * j + p.discardOrder0CardReverseIdx[s.discardArr[i]]] = k / 10.0f;
                                break;
                            }
                        }
                        if (j == p.discardOrder0CardMaxCopies) {
                            throw new IllegalStateException("Too many 0 cost card copies of " + p.cardDict[s.discardArr[i]].cardName);
                        }
                        k--;
                    }
                    if (k == 0) {
                        dIdx += p.discardOrder0CostNumber * p.discardOrder0CardMaxCopies;
                        k = 10;
                    }
                }
                return l1 + l2;
            });
            inputPrinters.add((s, input, idx) -> {
                var p = s.properties;
                int l1 = p.discardOrder0CostNumber * p.discardOrder0CardMaxCopies;
                int l2 = p.discardOrder0CostNumber * p.discardOrder0CardMaxCopies * p.discardOrderMaxKeepTrackIn10s;
                return l1 + l2;
            });
        }

        // Action contexts
        for (int i = 3; i < props.actionsByCtx.length; i++) {
            if (props.actionsByCtx[i] != null && (Configuration.ADD_BEGIN_TURN_CTX_TO_NN_INPUT || i != GameActionCtx.BEGIN_TURN.ordinal())) {
                inputLen += 1;
                descBody.append("    1 input to keep track of ctx ").append(GameActionCtx.values()[i]).append("\n");
                final int ctxOrdinal = i;
                inputModules.add((s, x, idx) -> {
                    x[idx] = s.actionCtx.ordinal() == ctxOrdinal ? 0.5f : -0.5f;
                    return 1;
                });
                final String ctxName = GameActionCtx.values()[i].toString();
                inputPrinters.add((s, input, idx) -> {
                    System.out.println("Action Context " + ctxName + ": " + input[idx]);
                    return 1;
                });
            }
        }

        // Energy
        {
            inputLen += 1;
            descBody.append("    1 input to keep track of energy\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.energy / (float) 10;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Energy: " + input[idx]);
                return 1;
            });
        }

        // Star resource
        if (props.anyEntityProperty.hasStarCost) {
            inputLen += 1;
            descBody.append("    1 input to keep track of star resource\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.starResource / 10.0f;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Star Resource: " + input[idx]);
                return 1;
            });
        }

        // Player health
        {
            inputLen += 1;
            descBody.append("    1 input to keep track of player health\n");
            inputModules.add((s, x, idx) -> {
                var player = s.getPlayerForRead();
                x[idx] = player.getHealth() / (float) player.getMaxHealth();
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Player Health: " + input[idx]);
                return 1;
            });
        }

        // Player block
        {
            inputLen += 1;
            descBody.append("    1 input to keep track of player block\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.getPlayerForRead().getBlock() / (float) 40.0;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Player Block: " + input[idx]);
                return 1;
            });
        }

        // Orbs
        {
            if (Configuration.USE_ORB_GENERATION_POSSIBLE_FOR_NN_INPUT) {
                short ogp = props.anyEntityProperty.orbGenerationPossible;
                int featuresPerSlot = 1 + Integer.bitCount(ogp & 0xFFFF);
                int len = props.maxNumOfOrbs * featuresPerSlot;
                inputLen += len;
                if (props.maxNumOfOrbs > 0) {
                    descBody.append("    ").append(props.maxNumOfOrbs).append("*").append(featuresPerSlot).append(" inputs to keep track of player orb slots\n");
                }
                final int maxOrbs = props.maxNumOfOrbs;
                final int fps = featuresPerSlot;
                // Build compact index map: orbType ordinal -> local offset within slot block
                final int[] orbTypeToCompactIdx = new int[OrbType.values().length];
                int compactIdx = 1; // 0 is EMPTY
                for (OrbType t : OrbType.values()) {
                    if (t == OrbType.EMPTY) {
                        orbTypeToCompactIdx[t.ordinal()] = 0;
                    } else if ((ogp & t.mask) != 0) {
                        orbTypeToCompactIdx[t.ordinal()] = compactIdx++;
                    }
                }
                inputModules.add((s, x, idx) -> {
                    int l = s.properties.maxNumOfOrbs * fps;
                    var orbs = s.getOrbs();
                    int orbIdx = idx;
                    if (orbs != null) {
                        for (int i = 0; i < orbs.length; i += 2) {
                            if (orbs[i] == OrbType.DARK.ordinal()) {
                                x[orbIdx + orbTypeToCompactIdx[orbs[i]]] = orbs[i + 1] / 50.0f;
                            } else if (orbs[i] == OrbType.GLASS.ordinal()) {
                                x[orbIdx + orbTypeToCompactIdx[orbs[i]]] = orbs[i + 1] / 4.0f;
                            } else if (orbs[i] > 0) {
                                x[orbIdx + orbTypeToCompactIdx[orbs[i]]] = 0.5f;
                            }
                            orbIdx += fps;
                        }
                    }
                    for (int i = orbs == null ? 0 : orbs.length / 2; i < s.properties.maxNumOfOrbs; i++) {
                        x[orbIdx] = 0.5f;
                        orbIdx += fps;
                    }
                    return l;
                });
                if (maxOrbs > 0) {
                    inputPrinters.add((s, input, idx) -> {
                        int l = s.properties.maxNumOfOrbs * fps;
                        System.out.println("Orbs:");
                        int orbIdx = idx;
                        for (int i = 0; i < s.properties.maxNumOfOrbs; i++) {
                            System.out.print("  Orb " + i + ": [");
                            for (int j = 0; j < fps; j++) {
                                if (j > 0) System.out.print(", ");
                                System.out.print(input[orbIdx + j]);
                            }
                            System.out.println("]");
                            orbIdx += fps;
                        }
                        return l;
                    });
                } else {
                    inputPrinters.add((s, input, idx) -> 0);
                }
            } else {
                int len = props.maxNumOfOrbs * 5;
                inputLen += len;
                if (props.maxNumOfOrbs > 0) {
                    descBody.append("    ").append(props.maxNumOfOrbs).append("*5 inputs to keep track of player orb slots\n");
                }
                final int maxOrbs = props.maxNumOfOrbs;
                inputModules.add((s, x, idx) -> {
                    int l = s.properties.maxNumOfOrbs * 5;
                    var orbs = s.getOrbs();
                    int orbIdx = idx;
                    if (orbs != null) {
                        for (int i = 0; i < orbs.length; i += 2) {
                            if (orbs[i] == OrbType.DARK.ordinal()) {
                                x[orbIdx + orbs[i]] = orbs[i + 1] / 50.0f;
                            } else if (orbs[i] == OrbType.GLASS.ordinal()) {
                                x[orbIdx + orbs[i]] = orbs[i + 1] / 4.0f;
                            } else if (orbs[i] > 0) {
                                x[orbIdx + orbs[i]] = 0.5f;
                            }
                            orbIdx += 5;
                        }
                    }
                    for (int i = orbs == null ? 0 : orbs.length / 2; i < s.properties.maxNumOfOrbs; i++) {
                        x[orbIdx] = 0.5f;
                        orbIdx += 5;
                    }
                    return l;
                });
                if (maxOrbs > 0) {
                    inputPrinters.add((s, input, idx) -> {
                        int l = s.properties.maxNumOfOrbs * 5;
                        System.out.println("Orbs:");
                        int orbIdx = idx;
                        for (int i = 0; i < s.properties.maxNumOfOrbs; i++) {
                            System.out.print("  Orb " + i + ": [");
                            for (int j = 0; j < 5; j++) {
                                if (j > 0) System.out.print(", ");
                                System.out.print(input[orbIdx + j]);
                            }
                            System.out.println("]");
                            orbIdx += 5;
                        }
                        return l;
                    });
                } else {
                    inputPrinters.add((s, input, idx) -> 0);
                }
            }
        }

        // Focus
        if (props.anyEntityProperty.changePlayerFocus) {
            inputLen += 1;
            descBody.append("    1 input to keep track of player focus\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.getFocus() / 15.0f;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Player Focus: " + input[idx]);
                return 1;
            });
        }

        // Player lose focus eot
        if (props.anyEntityProperty.changePlayerFocusEot) {
            inputLen += 1;
            descBody.append("    1 input to keep track of player lose focus eot debuff\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.getPlayerForRead().getLoseFocusEot() / 10.0f;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Player Lose Focus Eot: " + input[idx]);
                return 1;
            });
        }

        // Watcher stance
        if (props.character == CharacterEnum.WATCHER) {
            inputLen += 4;
            descBody.append("    4 inputs to keep track of Watcher stance (one-hot: neutral, wrath, calm, divinity)\n");
            inputModules.add((s, x, idx) -> {
                var stance = s.getStance();
                x[idx] = stance == Stance.NEUTRAL ? 1.0f : 0.0f;
                x[idx + 1] = stance == Stance.WRATH ? 1.0f : 0.0f;
                x[idx + 2] = stance == Stance.CALM ? 1.0f : 0.0f;
                x[idx + 3] = stance == Stance.DIVINITY ? 1.0f : 0.0f;
                return 4;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Watcher Stance - Neutral: " + input[idx]);
                System.out.println("Watcher Stance - Wrath: " + input[idx + 1]);
                System.out.println("Watcher Stance - Calm: " + input[idx + 2]);
                System.out.println("Watcher Stance - Divinity: " + input[idx + 3]);
                return 4;
            });
        }

        // Previous card play tracking
        if (props.previousCardPlayTracking) {
            inputLen += 3;
            descBody.append("    3 inputs to keep track of last played card type (one-hot: none, attack, skill)\n");
            inputModules.add((s, x, idx) -> {
                int lcp = s.getLastCardPlayedType();
                x[idx] = (lcp != Card.ATTACK && lcp != Card.SKILL) ? 1.0f : 0.0f;
                x[idx + 1] = lcp == Card.ATTACK ? 1.0f : 0.0f;
                x[idx + 2] = lcp == Card.SKILL ? 1.0f : 0.0f;
                return 3;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Previous Card Type - None: " + input[idx]);
                System.out.println("Previous Card Type - Attack: " + input[idx + 1]);
                System.out.println("Previous Card Type - Skill: " + input[idx + 2]);
                return 3;
            });
        }

        // Player artifact
        if (props.anyEntityProperty.changePlayerArtifact) {
            inputLen += 1;
            descBody.append("    1 input to keep track of player artifact\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.getPlayerForRead().getArtifact() / (float) 3.0;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Player Artifact: " + input[idx]);
                return 1;
            });
        }

        // Player strength
        if (props.anyEntityProperty.changePlayerStrength) {
            inputLen += 1;
            descBody.append("    1 input to keep track of player strength\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.getPlayerForRead().getStrength() / (float) 30.0;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Player Strength: " + input[idx]);
                return 1;
            });
        }

        // Player dexterity
        if (props.anyEntityProperty.changePlayerDexterity) {
            inputLen += 1;
            descBody.append("    1 input to keep track of player dexterity\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.getPlayerForRead().getDexterity() / (float) 10.0;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Player Dexterity: " + input[idx]);
                return 1;
            });
        }

        // Player lose strength eot
        if (props.anyEntityProperty.changePlayerStrengthEot) {
            inputLen += 1;
            descBody.append("    1 input to keep track of player lose strength eot debuff\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.getPlayerForRead().getLoseStrengthEot() / (float) 10.0;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Player Lose Strength EOT: " + input[idx]);
                return 1;
            });
        }

        // Player lose dexterity eot
        if (props.anyEntityProperty.changePlayerDexterityEot) {
            inputLen += 1;
            descBody.append("    1 input to keep track of player lose dexterity eot debuff\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.getPlayerForRead().getLoseDexterityEot() / (float) 10.0;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Player Lose Dexterity EOT: " + input[idx]);
                return 1;
            });
        }

        // Player plated armor
        if (props.anyEntityProperty.changePlatedArmor) {
            inputLen += 1;
            descBody.append("    1 input to keep track of player plated armor\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.getPlayerForRead().getPlatedArmor() / (float) 10.0;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Player Plated Armor: " + input[idx]);
                return 1;
            });
        }

        // Player vulnerable
        if (props.anyEntityProperty.changePlayerVulnerable) {
            inputLen += 1;
            descBody.append("    1 input to keep track of player vulnerable\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.getPlayerForRead().getVulnerable() / (float) 10.0;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Player Vulnerable: " + input[idx]);
                return 1;
            });
        }

        // Player weak
        if (props.anyEntityProperty.changePlayerWeakened) {
            inputLen += 1;
            descBody.append("    1 input to keep track of player weak\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.getPlayerForRead().getWeak() / (float) 10.0;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Player Weak: " + input[idx]);
                return 1;
            });
        }

        // Player frail
        if (props.anyEntityProperty.changePlayerFrailed) {
            inputLen += 1;
            descBody.append("    1 input to keep track of player frail\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.getPlayerForRead().getFrail() / (float) 10.0;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Player Frail: " + input[idx]);
                return 1;
            });
        }

        // Player entangled
        if (props.anyEntityProperty.changePlayerEntangled) {
            inputLen += 1;
            descBody.append("    1 input to keep track of whether player is entangled or not\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.getPlayerForRead().isEntangled() ? 0.5f : -0.5f;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Player Entangled: " + input[idx]);
                return 1;
            });
        }

        // Battle trance
        if ((props.anyEntityProperty.possibleBuffs & PlayerBuff.BATTLE_TRANCE.mask()) != 0) {
            inputLen += 1;
            descBody.append("    1 input to keep track of battle trance cannot draw card debuff\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.getPlayerForRead().cannotDrawCard() ? 0.5f : -0.5f;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Battle Trance Cannot Draw: " + input[idx]);
                return 1;
            });
        }

        // energy refill
        if (props.anyEntityProperty.changeEnergyRefill) {
            inputLen += 1;
            descBody.append("    1 input to keep track of energy refill\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = (s.energyRefill - 5) / 2f;
                return 1;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Energy Refill: " + input[idx]);
                return 1;
            });
        }

        // Player buffs
        for (PlayerBuff buff : PlayerBuff.BUFFS) {
            if ((props.anyEntityProperty.possibleBuffs & buff.mask()) != 0) {
                inputLen += 1;
                descBody.append("    1 input to keep track of buff ").append(buff.name()).append("\n");
                final long mask = buff.mask();
                final String name = buff.name();
                inputModules.add((s, x, idx) -> {
                    x[idx] = (s.buffs & mask) != 0 ? 0.5f : -0.5f;
                    return 1;
                });
                inputPrinters.add((s, input, idx) -> {
                    System.out.println("Player Buff " + name + ": " + input[idx]);
                    return 1;
                });
            }
        }

        // Counter infos
        for (var counterInfo : props.counterInfos) {
            if (counterInfo.handler != null) {
                int delta = counterInfo.handler.getInputLenDelta();
                inputLen += delta;
                descBody.append("    ").append(delta).append(" input to keep track of counter for ").append(counterInfo.name).append("\n");
                final var handler = counterInfo.handler;
                final String name = counterInfo.name;
                inputModules.add((s, x, idx) -> handler.addToInput(s, x, idx) - idx);
                inputPrinters.add((s, input, idx) -> {
                    int deltaLen = handler.getInputLenDelta();
                    System.out.print("Counter " + name + ": [");
                    for (int i = 0; i < deltaLen; i++) {
                        if (i > 0) System.out.print(", ");
                        System.out.print(input[idx + i]);
                    }
                    System.out.println("]");
                    return deltaLen;
                });
            }
        }

        // NN input handlers
        for (int i = 0; i < props.nnInputHandlers.length; i++) {
            int delta = props.nnInputHandlers[i].getInputLenDelta();
            inputLen += delta;
            descBody.append("    ").append(delta).append(" input to keep track of ").append(props.nnInputHandlersName[i]).append("\n");
            final var handler = props.nnInputHandlers[i];
            final String name = props.nnInputHandlersName[i];
            inputModules.add((s, x, idx) -> handler.addToInput(s, x, idx) - idx);
            inputPrinters.add((s, input, idx) -> {
                int deltaLen = handler.getInputLenDelta();
                System.out.print("NN Input Handler " + name + ": [");
                for (int j = 0; j < deltaLen; j++) {
                    if (j > 0) System.out.print(", ");
                    System.out.print(input[idx + j]);
                }
                System.out.println("]");
                return deltaLen;
            });
        }

        // Potions
        for (int i = 0; i < props.potions.size(); i++) {
            inputLen += 3;
            final int potionIdx = i;
            final String potionName = props.potions.get(i).toString();
            descBody.append("    3 inputs to keep track of ").append(potionName).append(" usage\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.potionsState[potionIdx * 3] == 1 ? 0.5f : -0.5f;
                x[idx + 1] = s.potionsState[potionIdx * 3 + 1] / 100f;
                x[idx + 2] = s.potionsState[potionIdx * 3 + 2] == 1 ? 0.5f : -0.5f;
                return 3;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Potion " + potionName + " - Has: " + input[idx]);
                System.out.println("Potion " + potionName + " - Charges: " + input[idx + 1]);
                System.out.println("Potion " + potionName + " - Can Use: " + input[idx + 2]);
                return 3;
            });
        }

        // Select enemy context actions
        if (props.actionsByCtx[GameActionCtx.SELECT_ENEMY.ordinal()] != null && enemies.size() > 1) {
            for (GameAction action : props.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && props.cardDict[action.idx()].entityProperty.selectEnemy && action.idx() < props.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && props.potions.get(action.idx()).entityProperty.selectEnemy) {
                    inputLen += 1;
                    final GameAction act = action;
                    String actDesc;
                    if (action.type() == GameActionType.PLAY_CARD) {
                        actDesc = "currently played card " + props.cardDict[action.idx()].cardName + " for selecting enemy";
                    } else {
                        actDesc = "currently used potion " + props.potions.get(action.idx()) + " for selecting enemy";
                    }
                    descBody.append("    1 input to keep track of ").append(actDesc).append("\n");
                    inputModules.add((s, x, idx) -> {
                        x[idx] = s.currentAction == act ? 0.6f : -0.6f;
                        return 1;
                    });
                    final String printLabel = "Select Enemy Action " + action;
                    inputPrinters.add((s, input, idx) -> {
                        System.out.println(printLabel + ": " + input[idx]);
                        return 1;
                    });
                }
            }
        }

        // Select card from hand context actions
        if (props.actionsByCtx[GameActionCtx.SELECT_CARD_HAND.ordinal()] != null) {
            for (GameAction action : props.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && props.cardDict[action.idx()].entityProperty.selectFromHand && action.idx() < props.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && props.potions.get(action.idx()).entityProperty.selectFromHand) {
                    inputLen += 1;
                    final GameAction act = action;
                    String actDesc;
                    if (action.type() == GameActionType.PLAY_CARD) {
                        actDesc = "currently played card " + props.cardDict[action.idx()].cardName + " for selecting card from hand";
                    } else {
                        actDesc = "currently used potion " + props.potions.get(action.idx()) + " for selecting card from hand";
                    }
                    descBody.append("    1 input to keep track of ").append(actDesc).append("\n");
                    inputModules.add((s, x, idx) -> {
                        x[idx] = s.currentAction == act ? 0.6f : -0.6f;
                        return 1;
                    });
                    final String printLabel = "Select Card Hand Action " + action;
                    inputPrinters.add((s, input, idx) -> {
                        System.out.println(printLabel + ": " + input[idx]);
                        return 1;
                    });
                }
            }
        }

        // Select card from discard context actions
        if (props.actionsByCtx[GameActionCtx.SELECT_CARD_DISCARD.ordinal()] != null) {
            for (GameAction action : props.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if ((action.type() == GameActionType.PLAY_CARD && props.cardDict[action.idx()].entityProperty.selectFromDiscard && action.idx() < props.realCardsLen) ||
                     action.type() == GameActionType.USE_POTION && props.potions.get(action.idx()).entityProperty.selectFromDiscard) {
                    inputLen += 1;
                    final GameAction act = action;
                    String actDesc;
                    if (action.type() == GameActionType.PLAY_CARD) {
                        actDesc = "currently played card " + props.cardDict[action.idx()].cardName + " for selecting card from discard";
                    } else {
                        actDesc = "currently used potion " + props.potions.get(action.idx()) + " for selecting card from discard";
                    }
                    descBody.append("    1 input to keep track of ").append(actDesc).append("\n");
                    inputModules.add((s, x, idx) -> {
                        x[idx] = s.currentAction == act ? 0.6f : -0.6f;
                        return 1;
                    });
                    final String printLabel = "Select Card Discard Action " + action;
                    inputPrinters.add((s, input, idx) -> {
                        System.out.println(printLabel + ": " + input[idx]);
                        return 1;
                    });
                }
            }
        }

        // Select card from exhaust context actions
        if (props.actionsByCtx[GameActionCtx.SELECT_CARD_EXHAUST.ordinal()] != null) {
            for (GameAction action : props.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && props.cardDict[action.idx()].entityProperty.selectFromExhaust && action.idx() < props.realCardsLen) {
                    inputLen += 1;
                    final GameAction act = action;
                    descBody.append("    1 input to keep track of currently played card ").append(props.cardDict[action.idx()].cardName).append(" for selecting card from exhaust\n");
                    inputModules.add((s, x, idx) -> {
                        x[idx] = s.currentAction == act ? 0.6f : -0.6f;
                        return 1;
                    });
                    final String printLabel = "Select Card Exhaust Action " + action;
                    inputPrinters.add((s, input, idx) -> {
                        System.out.println(printLabel + ": " + input[idx]);
                        return 1;
                    });
                }
            }
        }

        // Select card from deck context actions
        if (props.actionsByCtx[GameActionCtx.SELECT_CARD_DECK.ordinal()] != null) {
            for (GameAction action : props.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && props.cardDict[action.idx()].entityProperty.selectFromDeck && action.idx() < props.realCardsLen) {
                    inputLen += 1;
                    final GameAction act = action;
                    descBody.append("    1 input to keep track of currently played card ").append(props.cardDict[action.idx()].cardName).append(" for selecting card from deck\n");
                    inputModules.add((s, x, idx) -> {
                        x[idx] = s.currentAction == act ? 0.6f : -0.6f;
                        return 1;
                    });
                    final String printLabel = "Select Card Deck Action " + action;
                    inputPrinters.add((s, input, idx) -> {
                        System.out.println(printLabel + ": " + input[idx]);
                        return 1;
                    });
                }
            }
        }

        // Scrying context actions
        if (props.actionsByCtx[GameActionCtx.SCRYING.ordinal()] != null) {
            for (GameAction action : props.actionsByCtx[GameActionCtx.PLAY_CARD.ordinal()]) {
                if (action.type() == GameActionType.PLAY_CARD && props.cardDict[action.idx()].scry && action.idx() < props.realCardsLen) {
                    inputLen += 1;
                    final GameAction act = action;
                    descBody.append("    1 input to keep track of currently played card ").append(props.cardDict[action.idx()].cardName).append(" for scrying\n");
                    inputModules.add((s, x, idx) -> {
                        x[idx] = s.currentAction == act ? 0.6f : -0.6f;
                        return 1;
                    });
                    final String printLabel = "Scrying Action " + action;
                    inputPrinters.add((s, input, idx) -> {
                        System.out.println(printLabel + ": " + input[idx]);
                        return 1;
                    });
                }
            }
        }

        // Select 1 out of 3 cards
        {
            int len = props.select1OutOf3CardsIdxes.length;
            inputLen += len;
            if (len > 0) {
                descBody.append("    ").append(len).append(" inputs to keep track of selecting cards from 1 out of 3 cards\n");
            }
            inputModules.add((s, x, idx) -> {
                int l = s.properties.select1OutOf3CardsIdxes.length;
                if (s.actionCtx == GameActionCtx.SELECT_CARD_1_OUT_OF_3) {
                    x[idx + (s.select1OutOf3CardsIdxes & 255)] = 1;
                    x[idx + ((s.select1OutOf3CardsIdxes >> 8) & 255)] = 1;
                    x[idx + ((s.select1OutOf3CardsIdxes >> 16) & 255)] = 1;
                }
                return l;
            });
            if (len > 0) {
                inputPrinters.add((s, input, idx) -> {
                    int l = s.properties.select1OutOf3CardsIdxes.length;
                    System.out.print("Select 1 out of 3 Cards: [");
                    for (int i = 0; i < l; i++) {
                        if (i > 0) System.out.print(", ");
                        System.out.print(input[idx + i]);
                    }
                    System.out.println("]");
                    return l;
                });
            } else {
                inputPrinters.add((s, input, idx) -> 0);
            }
        }

        // Otsy HP and MaxHP
        if (props.anyEntityProperty.canSummon) {
            inputLen += 2;
            descBody.append("    2 inputs to keep track of Otsy HP and MaxHP\n");
            inputModules.add((s, x, idx) -> {
                x[idx] = s.getCounterForRead()[s.properties.otsyHPCounterIdx] / 100.0f;
                x[idx + 1] = s.getCounterForRead()[s.properties.otsyMaxHPCounterIdx] / 100.0f;
                return 2;
            });
            inputPrinters.add((s, input, idx) -> {
                System.out.println("Otsy HP: " + input[idx]);
                System.out.println("Otsy MaxHP: " + input[idx + 1]);
                return 2;
            });
        }

        // --- Enemy input modules ---

        var allEnemyModules = new ArrayList<EnemyInputModule>();

        // Health (always present)
        allEnemyModules.add(new EnemyInputModule() {
            @Override public int getLength(GameProperties p, EnemyReadOnly enemy) { return 1; }
            @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) { return "        1 input to keep track of health\n"; }
            @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                x[idx] = enemy.getHealth() / (float) enemy.properties.maxHealth;
                return 1;
            }
            @Override public int fillDead(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                x[idx] = enemy.getHealth() / (float) enemy.properties.maxHealth;
                return 1;
            }
            @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                System.out.println("  Health: " + input[idx]);
                return 1;
            }
        });

        // Vulnerable
        if (props.anyEntityProperty.vulnEnemy) {
            allEnemyModules.add(new EnemyInputModule() {
                @Override public int getLength(GameProperties p, EnemyReadOnly enemy) { return 1; }
                @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) { return "        1 input to keep track of vulnerable\n"; }
                @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                    x[idx] = enemy.getVulnerable() / (float) 10.0;
                    return 1;
                }
                @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                    System.out.println("  Vulnerable: " + input[idx]);
                    return 1;
                }
            });
        }

        // Weak
        if (props.anyEntityProperty.weakEnemy) {
            allEnemyModules.add(new EnemyInputModule() {
                @Override public int getLength(GameProperties p, EnemyReadOnly enemy) { return 1; }
                @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) { return "        1 input to keep track of weak\n"; }
                @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                    x[idx] = enemy.getWeak() / (float) 10.0;
                    return 1;
                }
                @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                    System.out.println("  Weak: " + input[idx]);
                    return 1;
                }
            });
        }

        // Choke
        if (props.anyEntityProperty.chokeEnemy) {
            allEnemyModules.add(new EnemyInputModule() {
                @Override public int getLength(GameProperties p, EnemyReadOnly enemy) { return 1; }
                @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) { return "        1 input to keep track of choke\n"; }
                @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                    x[idx] = enemy.getChoke() / (float) 10.0;
                    return 1;
                }
                @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                    System.out.println("  Choke: " + input[idx]);
                    return 1;
                }
            });
        }

        // Lock-On
        if (props.anyEntityProperty.lockOnEnemy) {
            allEnemyModules.add(new EnemyInputModule() {
                @Override public int getLength(GameProperties p, EnemyReadOnly enemy) { return 1; }
                @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) { return "        1 input to keep track of lockOn\n"; }
                @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                    x[idx] = enemy.getLockOn() / (float) 10.0;
                    return 1;
                }
                @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                    System.out.println("  Lock-On: " + input[idx]);
                    return 1;
                }
            });
        }

        // Talk to the Hand
        if (props.anyEntityProperty.talkToTheHandEnemy) {
            allEnemyModules.add(new EnemyInputModule() {
                @Override public int getLength(GameProperties p, EnemyReadOnly enemy) { return 1; }
                @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) { return "        1 input to keep track of talkToTheHand\n"; }
                @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                    x[idx] = enemy.getTalkToTheHand() / (float) 10.0;
                    return 1;
                }
                @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                    System.out.println("  Talk to the Hand: " + input[idx]);
                    return 1;
                }
            });
        }

        if (props.anyEntityProperty.sicEmEnemy) {
            allEnemyModules.add(new EnemyInputModule() {
                @Override public int getLength(GameProperties p, EnemyReadOnly enemy) { return 1; }
                @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) { return "        1 input to keep track of sicEm\n"; }
                @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                    x[idx] = enemy.getSicEm() / (float) 10.0;
                    return 1;
                }
                @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                    System.out.println("  Sic Em: " + input[idx]);
                    return 1;
                }
            });
        }

        // Mark
        if (props.anyEntityProperty.markEnemy) {
            allEnemyModules.add(new EnemyInputModule() {
                @Override public int getLength(GameProperties p, EnemyReadOnly enemy) { return 1; }
                @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) { return "        1 input to keep track of mark\n"; }
                @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                    x[idx] = enemy.getMark() / (float) 10.0;
                    return 1;
                }
                @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                    System.out.println("  Mark: " + input[idx]);
                    return 1;
                }
            });
        }

        // Poison
        if (props.anyEntityProperty.poisonEnemy) {
            allEnemyModules.add(new EnemyInputModule() {
                @Override public int getLength(GameProperties p, EnemyReadOnly enemy) { return 1; }
                @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) { return "        1 input to keep track of poison\n"; }
                @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                    x[idx] = enemy.getPoison() / (float) 30.0;
                    return 1;
                }
                @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                    System.out.println("  Poison: " + input[idx]);
                    return 1;
                }
            });
        }

        // Debilitate
        if (props.anyEntityProperty.debilitateEnemy) {
            allEnemyModules.add(new EnemyInputModule() {
                @Override public int getLength(GameProperties p, EnemyReadOnly enemy) { return 1; }
                @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) { return "        1 input to keep track of debilitate\n"; }
                @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                    x[idx] = enemy.getDebilitate() / 10.0f;
                    return 1;
                }
                @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                    System.out.println("  Debilitate: " + input[idx]);
                    return 1;
                }
            });
        }

        // Doom
        if (props.anyEntityProperty.doomEnemy) {
            allEnemyModules.add(new EnemyInputModule() {
                @Override public int getLength(GameProperties p, EnemyReadOnly enemy) { return 1; }
                @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) { return "        1 input to keep track of doom\n"; }
                @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                    x[idx] = enemy.getDoom() / 100.0f;
                    return 1;
                }
                @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                    System.out.println("  Doom: " + input[idx]);
                    return 1;
                }
            });
        }

        // Corpse Explosion
        if (props.anyEntityProperty.corpseExplosionEnemy) {
            allEnemyModules.add(new EnemyInputModule() {
                @Override public int getLength(GameProperties p, EnemyReadOnly enemy) { return 1; }
                @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) { return "        1 input to keep track of corpse explosion\n"; }
                @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                    x[idx] = enemy.getCorpseExplosion() / 10.0f;
                    return 1;
                }
                @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                    System.out.println("  Corpse Explosion: " + input[idx]);
                    return 1;
                }
            });
        }

        // Lose Strength EOT (per-enemy or global)
        allEnemyModules.add(new EnemyInputModule() {
            @Override public int getLength(GameProperties p, EnemyReadOnly enemy) {
                return (enemy.properties.canGainLoseStrengthEot || p.anyEntityProperty.affectEnemyStrengthEot) ? 1 : 0;
            }
            @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) {
                return "        1 input to keep track of enemy gain strength eot\n";
            }
            @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                x[idx] = enemy.getLoseStrengthEot() / (float) 20.0;
                return 1;
            }
            @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                System.out.println("  Lose Strength EOT: " + input[idx]);
                return 1;
            }
        });

        // Enemy Block (per-enemy)
        allEnemyModules.add(new EnemyInputModule() {
            @Override public int getLength(GameProperties p, EnemyReadOnly enemy) {
                return enemy.properties.canGainBlock ? 1 : 0;
            }
            @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) {
                return "        1 input to keep track of block\n";
            }
            @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                x[idx] = enemy.getBlock() / (float) 20.0;
                return 1;
            }
            @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                System.out.println("  Block: " + input[idx]);
                return 1;
            }
        });

        // Enemy Strength (per-enemy or global)
        allEnemyModules.add(new EnemyInputModule() {
            @Override public int getLength(GameProperties p, EnemyReadOnly enemy) {
                return (enemy.properties.canGainStrength || p.anyEntityProperty.affectEnemyStrength) ? 1 : 0;
            }
            @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) {
                return "        1 input to keep track of strength\n";
            }
            @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                x[idx] = enemy.getStrength() / (float) 20.0;
                return 1;
            }
            @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                System.out.println("  Strength: " + input[idx]);
                return 1;
            }
        });

        // Enemy Artifact (per-enemy)
        allEnemyModules.add(new EnemyInputModule() {
            @Override public int getLength(GameProperties p, EnemyReadOnly enemy) {
                return enemy.properties.hasArtifact ? 1 : 0;
            }
            @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) {
                return "        1 input to keep track of artifact\n";
            }
            @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                x[idx] = enemy.getArtifact() / 3.0f;
                return 1;
            }
            @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                System.out.println("  Artifact: " + input[idx]);
                return 1;
            }
        });

        // Enemy Regeneration (per-enemy)
        allEnemyModules.add(new EnemyInputModule() {
            @Override public int getLength(GameProperties p, EnemyReadOnly enemy) {
                return enemy.properties.canGainRegeneration ? 1 : 0;
            }
            @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) {
                return "        1 input to keep track of regeneration\n";
            }
            @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                x[idx] = enemy.getRegeneration() / (float) 10.0;
                return 1;
            }
            @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                System.out.println("  Regeneration: " + input[idx]);
                return 1;
            }
        });

        // Enemy Metallicize (per-enemy)
        allEnemyModules.add(new EnemyInputModule() {
            @Override public int getLength(GameProperties p, EnemyReadOnly enemy) {
                return enemy.properties.canGainMetallicize ? 1 : 0;
            }
            @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) {
                return "        1 input to keep track of metallicize\n";
            }
            @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                x[idx] = enemy.getMetallicize() / (float) 14.0;
                return 1;
            }
            @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                System.out.println("  Metallicize: " + input[idx]);
                return 1;
            }
        });

        // Enemy Plated Armor (per-enemy)
        allEnemyModules.add(new EnemyInputModule() {
            @Override public int getLength(GameProperties p, EnemyReadOnly enemy) {
                return enemy.properties.canGainPlatedArmor ? 1 : 0;
            }
            @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) {
                return "        1 input to keep track of plated armor\n";
            }
            @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                x[idx] = enemy.getPlatedArmor() / (float) 14.0;
                return 1;
            }
            @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                System.out.println("  Plated Armor: " + input[idx]);
                return 1;
            }
        });

        // Enemy Max Health (per-enemy: canGainRegeneration || canHeal || global: enemyCanGetCorpseExplosion)
        allEnemyModules.add(new EnemyInputModule() {
            @Override public int getLength(GameProperties p, EnemyReadOnly enemy) {
                return (enemy.properties.canGainRegeneration || enemy.properties.canHeal || p.anyEntityProperty.corpseExplosionEnemy) ? 1 : 0;
            }
            @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) {
                return "        1 input to keep track of enemy max health\n";
            }
            @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                x[idx] = enemy.getMaxHealthInBattle() / (float) enemy.properties.maxHealth;
                return 1;
            }
            @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                System.out.println("  Max Health: " + input[idx]);
                return 1;
            }
        });

        // Enemy Moves (complex: MergedEnemy vs regular)
        allEnemyModules.add(new EnemyInputModule() {
            @Override public int getLength(GameProperties p, EnemyReadOnly enemy) {
                if (enemy instanceof Enemy.MergedEnemy m) {
                    int len = m.possibleEnemies.size(); // current enemy one-hot
                    for (var pe : m.possibleEnemies) {
                        len += pe.properties.numOfMoves;
                        if (pe.properties.useLast2MovesForMoveSelection) {
                            len += pe.properties.numOfMoves;
                        }
                    }
                    return len;
                } else {
                    int len = enemy.properties.numOfMoves;
                    if (enemy.properties.useLast2MovesForMoveSelection) {
                        len += enemy.properties.numOfMoves;
                    }
                    return len;
                }
            }
            @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) {
                var sb = new StringBuilder();
                if (enemy instanceof Enemy.MergedEnemy m) {
                    sb.append("        ").append(m.possibleEnemies.size()).append(" input to keep track of current enemy\n");
                    for (int i = 0; i < m.possibleEnemies.size(); i++) {
                        sb.append("        ").append(m.possibleEnemies.get(i).properties.numOfMoves)
                          .append(" inputs to keep track of current move from ").append(m.possibleEnemies.get(i).getName()).append("\n");
                        if (m.possibleEnemies.get(i).properties.useLast2MovesForMoveSelection) {
                            sb.append("        ").append(m.possibleEnemies.get(i).properties.numOfMoves)
                              .append(" inputs to keep track of last move from ").append(m.possibleEnemies.get(i).getName()).append("\n");
                        }
                    }
                } else {
                    sb.append("        ").append(enemy.properties.numOfMoves).append(" inputs to keep track of current move from enemy\n");
                    if (enemy.properties.useLast2MovesForMoveSelection) {
                        sb.append("        ").append(enemy.properties.numOfMoves).append(" inputs to keep track of last move from enemy\n");
                    }
                }
                return sb.toString();
            }
            @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                int startIdx = idx;
                if (enemy instanceof Enemy.MergedEnemy m) {
                    x[idx + m.currentEnemyIdx] = 1.0f;
                    idx += m.possibleEnemies.size();
                    for (int pIdx = 0; pIdx < m.possibleEnemies.size(); pIdx++) {
                        if (pIdx == m.currentEnemyIdx) {
                            for (int i = 0; i < m.possibleEnemies.get(pIdx).properties.numOfMoves; i++) {
                                x[idx++] = m.getMove() == i ? 0.5f : -0.5f;
                            }
                            if (m.possibleEnemies.get(pIdx).properties.useLast2MovesForMoveSelection) {
                                for (int i = 0; i < m.possibleEnemies.get(pIdx).properties.numOfMoves; i++) {
                                    x[idx++] = m.getLastMove() == i ? 0.5f : -0.5f;
                                }
                            }
                        } else {
                            for (int i = 0; i < m.possibleEnemies.get(pIdx).properties.numOfMoves; i++) {
                                x[idx++] = -0.5f;
                            }
                            if (m.possibleEnemies.get(pIdx).properties.useLast2MovesForMoveSelection) {
                                for (int i = 0; i < m.possibleEnemies.get(pIdx).properties.numOfMoves; i++) {
                                    x[idx++] = -0.5f;
                                }
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < enemy.properties.numOfMoves; i++) {
                        x[idx++] = enemy.getMove() == i ? 0.5f : -0.5f;
                    }
                    if (enemy.properties.useLast2MovesForMoveSelection) {
                        for (int i = 0; i < enemy.properties.numOfMoves; i++) {
                            x[idx++] = enemy.getLastMove() == i ? 0.5f : -0.5f;
                        }
                    }
                }
                return idx - startIdx;
            }
            @Override public int fillDead(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                int startIdx = idx;
                if (enemy instanceof Enemy.MergedEnemy m) {
                    idx += m.possibleEnemies.size();
                    for (int pIdx = 0; pIdx < m.possibleEnemies.size(); pIdx++) {
                        for (int i = 0; i < m.possibleEnemies.get(pIdx).properties.numOfMoves; i++) {
                            x[idx++] = -0.1f;
                        }
                        if (m.possibleEnemies.get(pIdx).properties.useLast2MovesForMoveSelection) {
                            for (int i = 0; i < m.possibleEnemies.get(pIdx).properties.numOfMoves; i++) {
                                x[idx++] = -0.1f;
                            }
                        }
                    }
                } else if (enemy.properties.canSelfRevive) {
                    for (int i = 0; i < enemy.properties.numOfMoves; i++) {
                        x[idx++] = enemy.getMove() == i ? 0.5f : -0.5f;
                    }
                    if (enemy.properties.useLast2MovesForMoveSelection) {
                        for (int i = 0; i < enemy.properties.numOfMoves; i++) {
                            x[idx++] = enemy.getLastMove() == i ? 0.5f : -0.5f;
                        }
                    }
                } else {
                    for (int i = 0; i < enemy.properties.numOfMoves; i++) {
                        x[idx++] = -0.1f;
                    }
                    if (enemy.properties.useLast2MovesForMoveSelection) {
                        for (int i = 0; i < enemy.properties.numOfMoves; i++) {
                            x[idx++] = -0.1f;
                        }
                    }
                }
                return idx - startIdx;
            }
            @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                int startIdx = idx;
                if (enemy instanceof Enemy.MergedEnemy m) {
                    System.out.print("  Current Enemy: [");
                    for (int i = 0; i < m.possibleEnemies.size(); i++) {
                        if (i > 0) System.out.print(", ");
                        System.out.print(input[idx++]);
                    }
                    System.out.println("]");
                    for (int pIdx = 0; pIdx < m.possibleEnemies.size(); pIdx++) {
                        System.out.print("  Moves for " + m.possibleEnemies.get(pIdx).getName() + ": [");
                        for (int i = 0; i < m.possibleEnemies.get(pIdx).properties.numOfMoves; i++) {
                            if (i > 0) System.out.print(", ");
                            System.out.print(input[idx++]);
                        }
                        System.out.println("]");
                        if (m.possibleEnemies.get(pIdx).properties.useLast2MovesForMoveSelection) {
                            System.out.print("  Last Moves for " + m.possibleEnemies.get(pIdx).getName() + ": [");
                            for (int i = 0; i < m.possibleEnemies.get(pIdx).properties.numOfMoves; i++) {
                                if (i > 0) System.out.print(", ");
                                System.out.print(input[idx++]);
                            }
                            System.out.println("]");
                        }
                    }
                } else {
                    System.out.print("  Current Moves: [");
                    for (int i = 0; i < enemy.properties.numOfMoves; i++) {
                        if (i > 0) System.out.print(", ");
                        System.out.print(input[idx++]);
                    }
                    System.out.println("]");
                    if (enemy.properties.useLast2MovesForMoveSelection) {
                        System.out.print("  Last Moves: [");
                        for (int i = 0; i < enemy.properties.numOfMoves; i++) {
                            if (i > 0) System.out.print(", ");
                            System.out.print(input[idx++]);
                        }
                        System.out.println("]");
                    }
                }
                return idx - startIdx;
            }
        });

        // Enemy-specific NN input
        allEnemyModules.add(new EnemyInputModule() {
            @Override public int getLength(GameProperties p, EnemyReadOnly enemy) {
                return enemy.getNNInputLen(p);
            }
            @Override public String getDescription(GameProperties p, EnemyReadOnly enemy) {
                String desc = enemy.getNNInputDesc(p);
                return desc != null ? "        " + desc + "\n" : "";
            }
            @Override public int fill(GameState s, EnemyReadOnly enemy, float[] x, int idx) {
                return enemy.writeNNInput(s.properties, x, idx);
            }
            @Override public int print(GameState s, EnemyReadOnly enemy, float[] input, int idx) {
                int len = enemy.getNNInputLen(s.properties);
                System.out.print("  Enemy Specific Input: [");
                for (int i = 0; i < len; i++) {
                    if (i > 0) System.out.print(", ");
                    System.out.print(input[idx + i]);
                }
                System.out.println("]");
                return len;
            }
        });

        // --- Filter modules per-enemy and build perEnemyModules ---
        for (var enemy : enemies) {
            var modules = new ArrayList<EnemyInputModule>();
            for (var module : allEnemyModules) {
                if (module.getLength(props, enemy) > 0) {
                    modules.add(module);
                }
            }
            perEnemyModules.add(modules);
        }

        // --- Calculate enemy input lengths and descriptions ---
        for (int i = 0; i < enemies.size(); i++) {
            var enemy = enemies.get(i);
            String enemyName;
            if (enemy instanceof Enemy.MergedEnemy m) {
                enemyName = m.getDescName();
            } else {
                enemyName = enemy.getName();
            }
            descBody.append("    *** ").append(enemyName).append(" ***\n");
            for (var module : perEnemyModules.get(i)) {
                inputLen += module.getLength(props, enemy);
                descBody.append(module.getDescription(props, enemy));
            }
        }

        // --- Assemble final description ---
        var preamble = new StringBuilder();
        preamble.append("Possible Cards:\n");
        for (Card card : props.cardDict) {
            preamble.append("    ").append(card.cardName).append("\n");
        }
        preamble.append("Cards That Can Change In Number:\n");
        for (int discardIdx : props.discardIdxes) {
            preamble.append("    ").append(props.cardDict[discardIdx].cardName).append("\n");
        }
        preamble.append("Neural Network Input Breakdown (").append(inputLen).append(" inputs):\n");
        preamble.append(descBody);
        description = preamble.toString();
    }

    float[] fillNNInput(GameState state) {
        float[] x = new float[inputLen];
        int idx = 0;
        for (var m : inputModules) {
            idx += m.apply(state, x, idx);
        }
        var enemies = state.getEnemiesForRead();
        var enemyOrder = state.getEnemyOrder();
        for (int enemyIdx = 0; enemyIdx < enemies.size(); enemyIdx++) {
            int actualIdx = enemyOrder != null ? enemyOrder[enemyIdx] : enemyIdx;
            var enemy = enemies.get(actualIdx);
            var modules = perEnemyModules.get(actualIdx);
            if (enemy.isAlive()) {
                for (var m : modules) {
                    idx += m.fill(state, enemy, x, idx);
                }
            } else {
                for (var m : modules) {
                    idx += m.fillDead(state, enemy, x, idx);
                }
            }
        }
        if (idx != inputLen) {
            throw new IllegalStateException();
        }
        return x;
    }

    void printNNInput(GameState state, float[] input) {
        if (input.length != inputLen) {
            throw new IllegalArgumentException("Input array length (" + input.length + ") does not match expected length (" + inputLen + ")");
        }
        int idx = 0;
        System.out.println("Neural Network Input Analysis:");
        for (var m : inputPrinters) {
            idx += m.apply(state, input, idx);
        }
        var enemies = state.getEnemiesForRead();
        var enemyOrder = state.getEnemyOrder();
        for (int enemyIdx = 0; enemyIdx < enemies.size(); enemyIdx++) {
            int actualIdx = enemyOrder != null ? enemyOrder[enemyIdx] : enemyIdx;
            var enemy = enemies.get(actualIdx);
            String enemyName = enemy instanceof Enemy.MergedEnemy m ? m.getDescName() : enemy.getName();
            System.out.println("*** " + enemyName + " ***");
            for (var module : perEnemyModules.get(actualIdx)) {
                idx += module.print(state, enemy, input, idx);
            }
        }
        System.out.println("Total inputs processed: " + idx + " / " + inputLen);
    }
}
