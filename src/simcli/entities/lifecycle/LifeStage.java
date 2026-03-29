package simcli.entities.lifecycle;

/**
 * State Pattern interface for Sim lifecycle stages (Child, Teen, Adult, Elder).
 *
 * <p>Each stage defines age-specific rules: whether the Sim can work,
 * how fast energy decays, and what the next stage is. The Sim holds a
 * reference to the current {@code LifeStage}, and transitions occur
 * when {@link #getNextStage(int)} returns a different instance.</p>
 */
public interface LifeStage {

    /**
     * Returns whether a Sim in this life stage is allowed to work.
     *
     * @return {@code true} if the Sim can take a job.
     */
    boolean canWork();

    /**
     * Returns the energy decay modifier for this life stage.
     * Values below 1.0 slow decay; values above 1.0 accelerate it.
     *
     * @return the energy decay multiplier.
     */
    double getEnergyDecayModifier();

    /**
     * Returns the human-readable name of this stage (e.g. "Child", "Adult").
     *
     * @return the stage name.
     */
    String getStageName();

    /**
     * Determines whether a transition to the next life stage should occur
     * based on the Sim's current age.
     *
     * @param currentAge the Sim's current age after the latest birthday.
     * @return a new {@code LifeStage} instance if a transition occurs,
     *         or {@code this} if the Sim stays in the current stage.
     */
    LifeStage getNextStage(int currentAge);
}
