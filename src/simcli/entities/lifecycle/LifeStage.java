package simcli.entities.lifecycle;

/**
 * LifeStage — the core interface for the State Design Pattern lifecycle system.
 *
 * <p>Each concrete implementation (e.g. {@link ChildStage}, {@link AdultStage})
 * encapsulates the <em>rules</em> that govern a Sim's behaviour at one point in
 * their life. The {@link simcli.entities.Sim} object itself never changes or gets
 * destroyed; only its internal {@code currentStage} reference is swapped when a
 * birthday triggers a transition. This is the beating heart of the State Pattern.</p>
 *
 * <p><strong>Open/Closed benefit:</strong> To add a new life stage (e.g.
 * {@code ElderStage}), implement this interface and update {@code getNextStage()}
 * in the preceding stage only. No existing class requires modification.</p>
 */
public interface LifeStage {

    /**
     * Returns whether a Sim at this stage may hold a job and earn income.
     * Children return {@code false}; working-age adults return {@code true}.
     *
     * @return {@code true} if the Sim can legally work.
     */
    boolean canWork();

    /**
     * Returns the multiplier applied to energy decay each tick for a Sim at this
     * stage. {@code 1.0} is the normal adult rate; {@code 1.5} means a child
     * loses energy 50 % faster because they are always running and growing.
     *
     * @return a positive {@code double} energy-decay modifier.
     */
    double getEnergyDecayModifier();

    /**
     * Returns a human-readable label for this stage, suitable for CLI display
     * (e.g. {@code "Child"}, {@code "Adult"}).
     *
     * @return the stage display name.
     */
    String getStageName();

    /**
     * The <strong>transition logic</strong> of the State Pattern.
     *
     * <p>Given the Sim's {@code currentAge} after a birthday, this method decides
     * whether a stage change is required. If so, it returns the <em>next</em>
     * {@link LifeStage} instance; otherwise it returns {@code this} (no change).</p>
     *
     * <p>The caller ({@link simcli.entities.Sim#ageUp()}) simply does:</p>
     * <pre>
     *     this.currentStage = this.currentStage.getNextStage(this.age);
     * </pre>
     * <p>Because the caller only knows the {@code LifeStage} interface, this call
     * is fully polymorphic — no casting or {@code instanceof} needed.</p>
     *
     * @param currentAge the Sim's age <em>after</em> the birthday increment.
     * @return the next {@link LifeStage} (or {@code this} if no transition occurs).
     */
    LifeStage getNextStage(int currentAge);
}
