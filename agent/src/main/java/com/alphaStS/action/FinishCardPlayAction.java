package com.alphaStS.action;

import com.alphaStS.GameState;

public class FinishCardPlayAction implements GameEnvironmentAction {
    boolean runActionQueueOnEnd;
    boolean useEnergy;
    boolean cardPlayedSuccessfully;
    int realEnergyCost;
    private final Class cloneSource;
    private final int cloneParentLocation;
    private final int cardIdx;
    private final int energyCost;
    private final int prevCardIdx;

    public FinishCardPlayAction(boolean runActionQueueOnEnd, boolean useEnergy, boolean cardPlayedSuccessfully, int realEnergyCost,
            Class cloneSource, int cloneParentLocation, int cardIdx, int energyCost, int prevCardIdx) {
        this.runActionQueueOnEnd = runActionQueueOnEnd;
        this.useEnergy = useEnergy;
        this.cardPlayedSuccessfully = cardPlayedSuccessfully;
        this.realEnergyCost = realEnergyCost;
        this.cloneSource = cloneSource;
        this.cloneParentLocation = cloneParentLocation;
        this.cardIdx = cardIdx;
        this.energyCost = energyCost;
        this.prevCardIdx = prevCardIdx;
    }

    @Override public void doAction(GameState state) {
        state.handleCardFinishPlayingLocation(cloneSource, cloneParentLocation, cardIdx, energyCost, prevCardIdx);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FinishCardPlayAction that = (FinishCardPlayAction) o;
        return runActionQueueOnEnd == that.runActionQueueOnEnd && useEnergy == that.useEnergy && cardPlayedSuccessfully == that.cardPlayedSuccessfully && realEnergyCost == that.realEnergyCost && cloneSource == that.cloneSource && cloneParentLocation == that.cloneParentLocation && cardIdx == that.cardIdx && energyCost == that.energyCost && prevCardIdx == that.prevCardIdx;
    }

    @Override public int hashCode() {
        return cardIdx;
    }

    @Override public String toString() {
        return "FinishCardPlayAction(" + cardIdx + ")";
    }
}
