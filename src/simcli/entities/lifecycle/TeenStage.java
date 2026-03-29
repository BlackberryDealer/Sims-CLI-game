package simcli.entities.lifecycle;

import simcli.utils.GameConstants;

/**
 * Life stage for Sims aged 13–17. Teens cannot work and have
 * slightly elevated energy decay ({@code 1.2x}). Transitions to
 * {@link AdultStage} at age {@value simcli.utils.GameConstants#ADULT_AGE}.
 */
public class TeenStage implements LifeStage {

    @Override
    public boolean canWork() {
        return false;
    }

    @Override
    public double getEnergyDecayModifier() {
        return 1.2;
    }

    @Override
    public String getStageName() {
        return "Teen";
    }

    @Override
    public LifeStage getNextStage(int currentAge) {
        if (currentAge >= GameConstants.ADULT_AGE) {
            return new AdultStage();
        }
        return this;
    }
}
