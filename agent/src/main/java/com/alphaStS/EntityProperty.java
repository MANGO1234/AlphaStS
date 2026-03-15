package com.alphaStS;

import java.util.List;

public class EntityProperty {
    public boolean changePlayerArtifact;
    public boolean changePlayerStrength;
    public boolean changePlayerStrengthEot;
    public boolean changePlayerDexterity;
    public boolean changePlayerDexterityEot;
    public boolean changePlayerFocus;
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
    public boolean markEnemy;
    public boolean affectEnemyStrength;
    public boolean affectEnemyStrengthEot;
    public boolean changeEnergyRefill;
    public long possibleBuffs;
    public short orbGenerationPossible;

    public void mergeFrom(EntityProperty other) {
        changePlayerArtifact |= other.changePlayerArtifact;
        changePlayerStrength |= other.changePlayerStrength;
        changePlayerStrengthEot |= other.changePlayerStrengthEot;
        changePlayerDexterity |= other.changePlayerDexterity;
        changePlayerDexterityEot |= other.changePlayerDexterityEot;
        changePlayerFocus |= other.changePlayerFocus;
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
        markEnemy |= other.markEnemy;
        affectEnemyStrength |= other.affectEnemyStrength;
        affectEnemyStrengthEot |= other.affectEnemyStrengthEot;
        changeEnergyRefill |= other.changeEnergyRefill;
        possibleBuffs |= other.possibleBuffs;
        orbGenerationPossible |= other.orbGenerationPossible;
    }

    public static EntityProperty aggregate(List<EntityProperty> list) {
        EntityProperty result = new EntityProperty();
        for (EntityProperty ep : list) {
            result.mergeFrom(ep);
        }
        return result;
    }
}
