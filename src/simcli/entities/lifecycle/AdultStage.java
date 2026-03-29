package simcli.entities.lifecycle;

import simcli.utils.GameConstants;

/**
 * Life stage for Sims aged 18–64. Adults can work, have standard
 * energy decay ({@code 1.0x}), and form the core employable population.
 * Transitions to {@link ElderStage} at age
 * {@value simcli.utils.GameConstants#ELDER_AGE}.
 */
public class AdultStage implements LifeStage {

    @Override
    public boolean canWork() {
        return true;
    }

    @Override
    public double getEnergyDecayModifier() {
        return 1.0;
    }

    @Override
    public String getStageName() {
        return "Adult";
    }

    @Override
    public LifeStage getNextStage(int currentAge) {
        if (currentAge >= GameConstants.ELDER_AGE) {
            return new ElderStage();
        }
        return this;
    }
}
