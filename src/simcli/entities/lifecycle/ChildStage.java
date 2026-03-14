package simcli.entities.lifecycle;

/**
 * ChildStage — the concrete State for the childhood life stage.
 *
 * <p>Encapsulates all rules unique to a child Sim:</p>
 * <ul>
 *   <li>Children <strong>cannot</strong> work.</li>
 *   <li>Children burn energy <strong>1.5× faster</strong> than adults.</li>
 *   <li>At age {@value #ADULTHOOD_AGE}, {@link #getNextStage(int)} returns a new
 *       {@link AdultStage}, triggering the polymorphic "brain swap" inside
 *       {@link simcli.entities.Sim#ageUp()}.</li>
 * </ul>
 *
 * <p><strong>State Pattern benefit:</strong> The {@link simcli.entities.Sim} object
 * never needs to know what "being a child" means. It just delegates. When the
 * child grows up, only the internal reference is swapped — the Sim persists
 * unchanged in memory with all its data intact.</p>
 */
public class ChildStage implements LifeStage {

    /** The age at which a ChildStage transitions to AdultStage. */
    private static final int ADULTHOOD_AGE = 18;

    /**
     * Children are not permitted to work.
     *
     * @return {@code false} always.
     */
    @Override
    public boolean canWork() {
        return false;
    }

    /**
     * Children expend energy 50 % faster than a normal adult.
     *
     * @return {@code 1.5} — the child energy-decay multiplier.
     */
    @Override
    public double getEnergyDecayModifier() {
        return 1.5;
    }

    /**
     * Human-readable stage label for CLI output.
     *
     * @return {@code "Child"}.
     */
    @Override
    public String getStageName() {
        return "Child";
    }

    /**
     * Transition check called after every birthday.
     *
     * <p>Once {@code currentAge} reaches {@value #ADULTHOOD_AGE}, returns a fresh
     * {@link AdultStage} object. The Sim's {@code ageUp()} method will detect that
     * the returned object is different from {@code this} and execute the swap.
     * The old {@code ChildStage} then becomes unreachable and is garbage-collected
     * automatically — the "brain swap" in action.</p>
     *
     * @param currentAge the Sim's age after the birthday increment.
     * @return a new {@link AdultStage} if age &gt;= 18, or {@code this} otherwise.
     */
    @Override
    public LifeStage getNextStage(int currentAge) {
        if (currentAge >= ADULTHOOD_AGE) {
            // Transition triggered — return a new AdultStage instance.
            // Sim.ageUp() will store this reference, and the JVM garbage-collects
            // this ChildStage object automatically once it becomes unreachable.
            return new AdultStage();
        }
        // No transition yet — stay in ChildStage.
        return this;
    }
}
