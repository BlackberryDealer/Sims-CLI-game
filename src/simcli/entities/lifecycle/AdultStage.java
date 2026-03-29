package simcli.entities.lifecycle;

import simcli.utils.GameConstants;

/**
 * Life stage for Sims aged 18–64. Adults can work, have standard
 * energy decay ({@code 1.0x}), and form the core employable population.
 * Transitions to {@link ElderStage} at age
 * {@value simcli.utils.GameConstants#ELDER_AGE}.
 */
public class AdultStage implements LifeStage {

    /**
     * {@inheritDoc}
     *
     * @return {@code true} — adults are allowed to work.
     */
    @Override
    public boolean canWork() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code 1.0} — standard energy decay rate.
     */
    @Override
    public double getEnergyDecayModifier() {
        return 1.0;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code "Adult"}.
     */
    @Override
    public String getStageName() {
        return "Adult";
    }

    /**
     * {@inheritDoc}
     * Transitions to {@link ElderStage} at age
     * {@value simcli.utils.GameConstants#ELDER_AGE}.
     */
    @Override
    public LifeStage getNextStage(int currentAge) {
        if (currentAge >= GameConstants.ELDER_AGE) {
            return new ElderStage();
        }
        return this;
    }
}
