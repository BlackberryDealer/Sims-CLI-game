package simcli.entities.lifecycle;

import simcli.utils.GameConstants;

/**
 * Life stage for Sims aged 0–12. Children cannot work and have
 * faster energy decay ({@code 1.5x}). Transitions to {@link TeenStage}
 * when the Sim reaches age {@value simcli.utils.GameConstants#TEEN_AGE}.
 */
public class ChildStage implements LifeStage {

    /**
     * {@inheritDoc}
     *
     * @return {@code false} — children cannot work.
     */
    @Override
    public boolean canWork() {
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code 1.5x} — accelerated energy decay for children.
     */
    @Override
    public double getEnergyDecayModifier() {
        return simcli.utils.GameConstants.BONUS_TIMES;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code "Child"}.
     */
    @Override
    public String getStageName() {
        return "Child";
    }

    /**
     * {@inheritDoc}
     * Transitions to {@link TeenStage} at age
     * {@value simcli.utils.GameConstants#TEEN_AGE}.
     */
    @Override
    public LifeStage getNextStage(int currentAge) {
        if (currentAge >= GameConstants.TEEN_AGE) {
            // Transition triggered — return a new TeenStage instance.
            // Sim.ageUp() will store this reference, and the JVM garbage-collects
            // this ChildStage object automatically once it becomes unreachable.
            return new TeenStage();
        }
        // No transition yet — stay in ChildStage.
        return this;
    }
}
