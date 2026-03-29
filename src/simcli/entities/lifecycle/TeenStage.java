package simcli.entities.lifecycle;

import simcli.utils.GameConstants;

/**
 * Life stage for Sims aged 13–17. Teens cannot work and have
 * slightly elevated energy decay ({@code 1.2x}). Transitions to
 * {@link AdultStage} at age {@value simcli.utils.GameConstants#ADULT_AGE}.
 */
public class TeenStage implements LifeStage {

    /**
     * {@inheritDoc}
     *
     * @return {@code false} — teens cannot work.
     */
    @Override
    public boolean canWork() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code 1.2} — slightly elevated energy decay for teens.
     */
    @Override
    public double getEnergyDecayModifier() {
        return 1.2;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code "Teen"}.
     */
    @Override
    public String getStageName() {
        return "Teen";
    }

    /**
     * {@inheritDoc}
     * Transitions to {@link AdultStage} at age
     * {@value simcli.utils.GameConstants#ADULT_AGE}.
     */
    @Override
    public LifeStage getNextStage(int currentAge) {
        if (currentAge >= GameConstants.ADULT_AGE) {
            return new AdultStage();
        }
        return this;
    }
}
