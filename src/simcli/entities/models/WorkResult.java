package simcli.entities.models;

/**
 * Immutable value object returned by {@link simcli.entities.actors.Sim#performWork()}
 * containing the outcome of a work shift: success status, earnings, promotion
 * flag, and overwork indicator.
 *
 * <p>Uses static factory methods ({@link #success(int, boolean, boolean)} and
 * {@link #failure(String)}) for construction instead of public constructors.</p>
 */
public class WorkResult {

    /** Whether the work shift completed successfully. */
    private final boolean success;

    /** A message describing the outcome (mainly for failures). */
    private final String message;

    /** The amount of money earned during this shift. */
    private final int earnings;

    /** Whether the Sim was promoted after this shift. */
    private final boolean promoted;

    /** Whether the Sim worked extra shifts today (overwork). */
    private final boolean overworked;

    /**
     * Private constructor — use {@link #success(int, boolean, boolean)}
     * or {@link #failure(String)} instead.
     *
     * @param success    whether the shift was successful.
     * @param message    a descriptive outcome message.
     * @param earnings   money earned.
     * @param promoted   whether a promotion occurred.
     * @param overworked whether this was an overtime shift.
     */
    private WorkResult(boolean success, String message, int earnings, boolean promoted, boolean overworked) {
        this.success = success;
        this.message = message;
        this.earnings = earnings;
        this.promoted = promoted;
        this.overworked = overworked;
    }

    /**
     * Creates a successful work result.
     *
     * @param earnings   the money earned during the shift.
     * @param promoted   {@code true} if the Sim was promoted.
     * @param overworked {@code true} if this was an overtime shift.
     * @return a new successful {@code WorkResult}.
     */
    public static WorkResult success(int earnings, boolean promoted, boolean overworked) {
        return new WorkResult(true, "Success", earnings, promoted, overworked);
    }

    /**
     * Creates a failed work result with an explanatory message.
     *
     * @param message the reason the work attempt failed.
     * @return a new failed {@code WorkResult}.
     */
    public static WorkResult failure(String message) {
        return new WorkResult(false, message, 0, false, false);
    }

    /**
     * Returns whether the work shift was successful.
     *
     * @return {@code true} if successful.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the outcome message.
     *
     * @return the message string.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the money earned during the shift.
     *
     * @return the earnings amount (0 if unsuccessful).
     */
    public int getEarnings() {
        return earnings;
    }

    /**
     * Returns whether the Sim was promoted after this shift.
     *
     * @return {@code true} if promoted.
     */
    public boolean isPromoted() {
        return promoted;
    }

    /**
     * Returns whether this was an overtime (overwork) shift.
     *
     * @return {@code true} if the Sim worked extra shifts.
     */
    public boolean isOverworked() {
        return overworked;
    }
}
