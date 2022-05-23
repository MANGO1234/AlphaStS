package com.alphaStS.player;

import com.alphaStS.DebuffType;
import com.alphaStS.GameState;
import com.alphaStS.PlayerBuff;

public class Player extends PlayerReadOnly {
    public Player(int health, int maxHealth) {
        super(health, maxHealth);
    }

    public Player(Player other) {
        super(other);
    }

    public int damage(int n) {
        int startHealth = health;
        health -= Math.max(0, n - block);
        block = Math.max(0, block - n);
        if (health < 0) {
            health = 0;
        }
        return startHealth - health;
    }

    public int nonAttackDamage(int n, boolean blockable) {
        int startHealth = health;
        if (blockable) {
            health -= Math.max(0, n - block);
            block = Math.max(0, block - n);
        } else {
            health -= n;
        }
        if (health < 0) {
            health = 0;
        }
        return health - startHealth;
    }

    public int heal(int n) {
        int healed = Math.min(n, maxHealth - health) ;
        health += healed;
        return healed;
    }

    public void gainBlock(int n) {
        n += dexterity;
        n = frail > 0? (n - (n + 3) / 4) : n;
        block += n;
        if (block > 999) {
            block = 999;
        }
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
        if (state.prop.hasGinger && type == DebuffType.WEAK) {
            return;
        } else if (state.prop.hasTurnip && type == DebuffType.FRAIL) {
            return;
        }
        if (artifact > 0) {
            artifact--;
            return;
        }
        switch (type) {
        case VULNERABLE -> this.vulnerable += n;
        case WEAK -> this.weak += n;
        case FRAIL -> this.frail += n;
        case LOSE_STRENGTH -> this.gainStrength(-n);
        case LOSE_DEXTERITY -> this.gainDexterity(-n);
        case LOSE_STRENGTH_EOT -> this.loseStrengthEot += n;
        case LOSE_DEXTERITY_EOT -> this.loseDexterityEot += n;
        case NO_MORE_CARD_DRAW -> this.cannotDrawCard = true;
        case ENTANGLED -> this.entangled = n;
        case HEX -> this.hexed = true;
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
        cannotDrawCard = false;
        entangled--;
        if (loseStrengthEot > 0) {
            applyDebuff(state, DebuffType.LOSE_STRENGTH, loseStrengthEot);
        }
        if (loseDexterityEot > 0) {
            applyDebuff(state, DebuffType.LOSE_STRENGTH, loseDexterityEot);
        }
        if ((state.buffs & PlayerBuff.BARRICADE.mask()) != 0) {
        } else if (state.prop.hasCaliper) {
            block = Math.max(block - 15, 0);
        } else {
            block = 0;
        }
    }

    public void gainArtifact(int n) {
        artifact += n;
    }

    public void setHealth(int hp) {
        health = hp;
    }

    public void setOrigHealth(int hp) {
        origHealth = hp;
        health = hp;
    }
}
