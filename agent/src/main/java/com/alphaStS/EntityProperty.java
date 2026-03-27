package com.alphaStS;

import java.util.List;

public class EntityProperty {
    public boolean changePlayerArtifact;
    public boolean changePlayerStrength;
    public boolean changePlayerStrengthEot;
    public boolean changePlayerDexterity;
    public boolean changePlayerDexterityEot;
    public boolean changePlayerFocus;
    public boolean changePlayerFocusEot;
    public boolean changePlayerVulnerable;
    public boolean changePlayerWeakened;
    public boolean changePlayerFrailed;
    public boolean changePlayerEntangled;
    public boolean vulnEnemy;
    public boolean weakEnemy;
    public boolean chokeEnemy;
    public boolean poisonEnemy;
    public boolean corpseExplosionEnemy;
    public boolean lockOnEnemy;
    public boolean talkToTheHandEnemy;
    public boolean sicEmEnemy;
    public boolean markEnemy;
    public boolean doomEnemy;
    public boolean debilitateEnemy;
    public boolean hangEnemy;
    public boolean doomPerCardEnemy;
    public boolean affectEnemyStrength;
    public boolean affectEnemyStrengthEot;
    public boolean changeEnergyRefill;
    public boolean changePlatedArmor;
    public boolean selectEnemy;
    public boolean selectFromDiscard;
    public boolean selectFromExhaust;
    public boolean selectFromDeck;
    public boolean selectFromHand;
    public boolean putCardOnTopDeck;
    public long possibleBuffs;
    public short orbGenerationPossible;
    public boolean sly;
    public boolean canSummon;
    public boolean hasStarCost;
    public boolean canForge;

    public void mergeFrom(EntityProperty other) {
        changePlayerArtifact |= other.changePlayerArtifact;
        changePlayerStrength |= other.changePlayerStrength;
        changePlayerStrengthEot |= other.changePlayerStrengthEot;
        changePlayerDexterity |= other.changePlayerDexterity;
        changePlayerDexterityEot |= other.changePlayerDexterityEot;
        changePlayerFocus |= other.changePlayerFocus;
        changePlayerFocusEot |= other.changePlayerFocusEot;
        changePlayerVulnerable |= other.changePlayerVulnerable;
        changePlayerWeakened |= other.changePlayerWeakened;
        changePlayerFrailed |= other.changePlayerFrailed;
        changePlayerEntangled |= other.changePlayerEntangled;
        vulnEnemy |= other.vulnEnemy;
        weakEnemy |= other.weakEnemy;
        chokeEnemy |= other.chokeEnemy;
        poisonEnemy |= other.poisonEnemy;
        corpseExplosionEnemy |= other.corpseExplosionEnemy;
        lockOnEnemy |= other.lockOnEnemy;
        talkToTheHandEnemy |= other.talkToTheHandEnemy;
        sicEmEnemy |= other.sicEmEnemy;
        markEnemy |= other.markEnemy;
        doomEnemy |= other.doomEnemy;
        doomPerCardEnemy |= other.doomPerCardEnemy;
        debilitateEnemy |= other.debilitateEnemy;
        affectEnemyStrength |= other.affectEnemyStrength;
        affectEnemyStrengthEot |= other.affectEnemyStrengthEot;
        changeEnergyRefill |= other.changeEnergyRefill;
        changePlatedArmor |= other.changePlatedArmor;
        selectEnemy |= other.selectEnemy;
        selectFromDiscard |= other.selectFromDiscard;
        selectFromExhaust |= other.selectFromExhaust;
        selectFromDeck |= other.selectFromDeck;
        selectFromHand |= other.selectFromHand;
        putCardOnTopDeck |= other.putCardOnTopDeck;
        possibleBuffs |= other.possibleBuffs;
        orbGenerationPossible |= other.orbGenerationPossible;
        sly |= other.sly;
        canSummon |= other.canSummon;
        hasStarCost |= other.hasStarCost;
        canForge |= other.canForge;
    }

    public static EntityProperty aggregate(List<EntityProperty> list) {
        EntityProperty result = new EntityProperty();
        for (EntityProperty ep : list) {
            result.mergeFrom(ep);
        }
        return result;
    }
}
