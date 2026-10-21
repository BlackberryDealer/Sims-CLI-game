package simcli.entities.lifecycle;

import simcli.utils.GameConstants;

/**
 * Life stage for Sims aged 13–17. Teens go to school on weekdays, can
 * take part-time jobs (adult careers are age-gated), and have slightly
 * elevated energy decay ({@code 1.2x}). Transitions to
 * {@link AdultStage} at age {@value simcli.utils.GameConstants#ADULT_AGE}.
 */
public class TeenStage implements LifeStage {

    /**
     * {@inheritDoc}
     *
     * @return {@code true} — teens can take part-time jobs.
     */
    @Override
    public boolean canWork() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} — teens go to school on weekdays.
     */
    @Override
    public boolean attendsSchool() {
        return true;
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
