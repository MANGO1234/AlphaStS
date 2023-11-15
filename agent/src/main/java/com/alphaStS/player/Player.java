package com.alphaStS.player;

import com.alphaStS.*;

public class Player extends PlayerReadOnly {
    public Player(int health, int maxHealth) {
        super(health, maxHealth);
    }

    public Player(Player other) {
        super(other);
    }

    public int damage(GameState state, int n) {
        int startHealth = health;
        int dmg = Math.max(0, n - block);
        if (dmg <= 5 && dmg >= 2 && state.properties.hasTorri) {
            dmg = 1;
        }
        if (dmg > 0 && state.properties.hasTungstenRod) {
            dmg -= 1;
        }
        if (n > block && state.properties.bufferCounterIdx >= 0 && state.getCounterForRead()[state.properties.bufferCounterIdx] > 0) {
            dmg = 0;
            state.getCounterForWrite()[state.properties.bufferCounterIdx]--;
        }
        health -= dmg;
        block = Math.max(0, block - n);
        if (platedArmor > 0 && dmg > 0) {
            platedArmor -= 1;
        }
        if (health < 0) {
            health = 0;
        }
        int dmgDealt = startHealth - health;
        if (health == 0) {
            tryReviveWithFairyInABottle(state);
        }
        return dmgDealt;
    }

    public int nonAttackDamage(GameState state, int n, boolean blockable) {
        int startHealth = health;
        if (blockable) {
            int dmg = Math.max(0, n - block);
            if (dmg > 0 && state.properties.hasTungstenRod) {
                dmg -= 1;
            }
            if (n > block && state.properties.bufferCounterIdx >= 0 && state.getCounterForRead()[state.properties.bufferCounterIdx] > 0) {
                dmg = 0;
                state.getCounterForWrite()[state.properties.bufferCounterIdx]--;
            }
            health -= dmg;
            block = Math.max(0, block - n);
        } else {
            int dmg = n;
            if (dmg > 0 && state.properties.hasTungstenRod) {
                dmg -= 1;
            }
            if (dmg >= 0 && state.properties.bufferCounterIdx >= 0 && state.getCounterForRead()[state.properties.bufferCounterIdx] > 0) {
                dmg = 0;
                state.getCounterForWrite()[state.properties.bufferCounterIdx]--;
            }
            health -= dmg;
        }
        if (health < 0) {
            health = 0;
        }
        int dmgDealt = startHealth - health;
        if (health == 0) {
            tryReviveWithFairyInABottle(state);
        }
        return dmgDealt;
    }

    private void tryReviveWithFairyInABottle(GameState state) {
        for (int i = 0; i < state.properties.potions.size(); i++) {
            if (state.potionUsable(i) && state.properties.potions.get(i) instanceof Potion.FairyInABottle) {
                state.getPotionsStateForWrite()[i * 3] = 0;
                state.properties.potions.get(i).use(state, -1);
                break;
            }
        }
    }

    public int heal(int n) {
        int healed = Math.min(n, maxHealth - health) ;
        health += healed;
        return healed;
    }

    public int gainBlock(int n) {
        n += dexterity;
        n = frail > 0? (n - (n + 3) / 4) : n;
        if (n < 0) {
            n = 0;
        }
        block += n;
        if (block > 999) {
            block = 999;
        }
        return n;
    }

    public void gainBlockNotFromCardPlay(int n) {
        block += n;
        if (block > 999) {
            block = 999;
        }
    }

    public void gainStrength(int n) {
        strength += n;
        strength = Math.min(999, Math.max(-999, strength));
    }

    public void gainDexterity(int n) {
        dexterity += n;
        dexterity = Math.min(999, Math.max(-999, dexterity));
    }

    public void applyDebuff(GameState state, DebuffType type, int n) {
        if (n == 0) {
            return;
        }
        if (state.properties.hasGinger && type == DebuffType.WEAK) {
            return;
        } else if (state.properties.hasTurnip && type == DebuffType.FRAIL) {
            return;
        }
        if (artifact > 0) {
            artifact--;
            return;
        }
        // add 1 to some debuff so they don't get cleared after enemies do their move
        switch (type) {
        case VULNERABLE -> this.vulnerable += state.getActionCtx() == GameActionCtx.BEGIN_TURN && this.vulnerable == 0 ? n + 1 : n;
        case WEAK -> this.weak += state.getActionCtx() == GameActionCtx.BEGIN_TURN && this.weak == 0 ? n + 1 : n;
        case FRAIL -> this.frail += state.getActionCtx() == GameActionCtx.BEGIN_TURN && this.frail == 0 ? n + 1 : n;
        case LOSE_STRENGTH -> this.gainStrength(-n);
        case LOSE_DEXTERITY -> this.gainDexterity(-n);
        case LOSE_STRENGTH_EOT -> this.loseStrengthEot += n;
        case LOSE_DEXTERITY_EOT -> this.loseDexterityEot += n;
        case LOSE_DEXTERITY_PER_TURN -> state.getCounterForWrite()[state.properties.loseDexterityPerTurnCounterIdx] += n;
        case NO_MORE_CARD_DRAW -> this.cannotDrawCard = true;
        case ENTANGLED -> this.entangled = state.getActionCtx() == GameActionCtx.BEGIN_TURN && this.entangled == 0 ? n + 1 : n;
        case HEX -> this.hexed = true;
        case LOSE_FOCUS -> state.gainFocus(-n);
        case LOSE_FOCUS_PER_TURN -> state.getCounterForWrite()[state.properties.loseFocusPerTurnCounterIdx] += n;
        case CONSTRICTED -> state.getCounterForWrite()[state.properties.constrictedCounterIdx] += n;
        case DRAW_REDUCTION -> state.getCounterForWrite()[state.properties.drawReductionCounterIdx] += n;
        case SNECKO -> state.getCounterForWrite()[state.properties.sneckoDebuffCounterIdx] = 1;
        }
    }

    public void preEndTurn(GameState state) {
        cannotDrawCard = false;
        if (platedArmor > 0) {
            gainBlockNotFromCardPlay(platedArmor);
        }
    }

    public void endTurn(GameState state) {
        if (vulnerable > 0) {
            vulnerable -= 1;
        }
        if (weak > 0) {
            weak -= 1;
        }
        if (frail > 0) {
            frail -= 1;
        }
        if (entangled > 0) {
            entangled -= 1;
        }
        if (loseStrengthEot > 0) {
            applyDebuff(state, DebuffType.LOSE_STRENGTH, loseStrengthEot);
        }
        if (loseDexterityEot > 0) {
            applyDebuff(state, DebuffType.LOSE_DEXTERITY, loseDexterityEot);
        }
        loseStrengthEot = 0;
        loseDexterityEot = 0;
        if ((state.buffs & PlayerBuff.BARRICADE.mask()) != 0) {
        } else if (state.properties.blurCounterIdx >= 0 && state.getCounterForRead()[state.properties.blurCounterIdx] > 0) {
            state.getCounterForWrite()[state.properties.blurCounterIdx]--;
        } else if (state.properties.hasCaliper) {
            block = Math.max(block - 15, 0);
        } else {
            block = 0;
        }
    }

    public void gainArtifact(int n) {
        artifact += n;
    }

    public void gainPlatedArmor(int n) {
        platedArmor += n;
    }

    public void setHealth(int hp) {
        health = hp;
    }

    public void setOrigHealth(int hp) {
        origHealth = hp;
        health = hp;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void removeAllDebuffs(GameState state) {
        vulnerable = 0;
        weak = 0;
        frail = 0;
        cannotDrawCard = false;
        hexed = false;
        entangled = 0;
        loseStrengthEot = 0;
        loseDexterityEot = 0;
        if (strength < 0) {
            strength = 0;
        }
        if (dexterity < 0) {
            dexterity = 0;
        }
        if (state.properties.loseDexterityPerTurnCounterIdx >= 0) {
            state.getCounterForWrite()[state.properties.loseDexterityPerTurnCounterIdx] = 0;
        }
        if (state.properties.loseFocusPerTurnCounterIdx >= 0) {
            state.getCounterForWrite()[state.properties.loseFocusPerTurnCounterIdx] = 0;
        }
        if (state.properties.constrictedCounterIdx >= 0) {
            state.getCounterForWrite()[state.properties.constrictedCounterIdx] = 0;
        }
        if (state.properties.drawReductionCounterIdx >= 0) {
            state.getCounterForWrite()[state.properties.drawReductionCounterIdx] = 0;
        }
        if (state.properties.sneckoDebuffCounterIdx >= 0) {
            state.getCounterForWrite()[state.properties.sneckoDebuffCounterIdx] = 0;
        }

    }
}
