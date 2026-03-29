package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.entities.models.Job;
import simcli.entities.models.SimState;
import simcli.utils.GameConstants;

import java.util.List;

/**
 * LifecycleManager — centralizes all daily aging, life-stage transitions,
 * death-by-old-age, and retirement logic for every Sim in the neighborhood.
 *
 * <h2>Responsibility (Single Responsibility Principle)</h2>
 * <p>This class does exactly one thing: process day boundaries for each Sim's
 * lifecycle. It increments {@code daysAlive}, triggers {@link Sim#ageUp()} at
 * the correct interval, handles death-by-old-age, pension collection, and
 * forced retirement. It knows nothing about needs, inventory, rendering, or
 * input — those concerns belong elsewhere.</p>
 *
 * <h2>How it fits into the State Pattern</h2>
 * <p>{@link #processDay(Sim)} delegates to {@link Sim#ageUp()}, which in turn
 * calls {@code currentStage.getNextStage(age)} on the Sim's private stage. If
 * the stage returns a new object, the Sim swaps its "brain" polymorphically.
 * This manager never touches the stage directly — it only tells the Sim that
 * a birthday has occurred.</p>
 *
 * <h2>Encapsulation</h2>
 * <p>All lifecycle logic that was previously scattered inside {@code Sim} and
 * {@code GameLoop} is now consolidated here, making it easier to test, modify,
 * and reason about aging rules in isolation.</p>
 */
public class LifecycleManager {

    // -------------------------------------------------------------------------
    // Private Fields
    // -------------------------------------------------------------------------

    /**
     * How many in-game days must pass before a Sim ages by one year.
     * Sourced from {@link GameConstants#DAYS_PER_AGE_TICK}.
     */
    private final int daysPerAgeTick;

    /**
     * Logger for lifecycle event messages (death, pension, retirement).
     */
    private final SimulationLogger logger;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new {@code LifecycleManager}.
     *
     * @param daysPerAgeTick number of in-game days that constitute one
     *                       age-up cycle. Must be &gt;= 1.
     * @param logger        the simulation logger for lifecycle messages.
     * @throws IllegalArgumentException if {@code daysPerAgeTick} is less than 1.
     */
    public LifecycleManager(int daysPerAgeTick, SimulationLogger logger) {
        if (daysPerAgeTick < 1) {
            throw new IllegalArgumentException(
                    "daysPerAgeTick must be >= 1, but was: " + daysPerAgeTick);
        }
        this.daysPerAgeTick = daysPerAgeTick;
        this.logger = logger;
    }

    // -------------------------------------------------------------------------
    // Core Methods
    // -------------------------------------------------------------------------

    /**
     * Processes a day boundary for every Sim in the neighborhood.
     *
     * <p>This is the main entry point called by {@link GameLoop} whenever
     * a new day begins. It iterates over all Sims and delegates individual
     * processing to {@link #processDay(Sim)}. Dead Sims are skipped.</p>
     *
     * @param neighborhood the list of all Sims in the game world.
     */
    public void processDayForAll(List<Sim> neighborhood) {
        for (Sim sim : neighborhood) {
            if (sim.getState() != SimState.DEAD) {
                processDay(sim);
            }
        }
    }

    /**
     * Processes a single day boundary for one Sim.
     *
     * <p>Increments the Sim's day counter via {@link Sim#incrementDaysAlive()},
     * resets daily career counters, and checks whether the Sim has reached an
     * age-up boundary (every {@link #daysPerAgeTick} days). On an age-up:</p>
     * <ul>
     *     <li>Calls {@link Sim#ageUp()} to increment age and trigger the
     *         State Pattern transition if applicable.</li>
     *     <li>Checks if the Sim has reached {@link GameConstants#DEATH_AGE}
     *         and marks them as dead.</li>
     *     <li>Awards retirement pension to unemployed elders.</li>
     *     <li>Forces retirement if the Sim exceeds their job's max age.</li>
     * </ul>
     *
     * @param sim the {@link Sim} to process; must not be {@code null}.
     * @throws IllegalArgumentException if {@code sim} is {@code null}.
     */
    public void processDay(Sim sim) {
        if (sim == null) {
            throw new IllegalArgumentException(
                    "Cannot process a day for a null Sim.");
        }

        // Increment the Sim's day counter and reset daily career state.
        sim.incrementDaysAlive();
        sim.getCareerManager().resetDaily(true);

        // Check if a full age-up cycle has elapsed.
        if (sim.getDaysAlive() % daysPerAgeTick == 0) {
            sim.ageUp();

            // Death by old age.
            if (sim.getAge() >= GameConstants.DEATH_AGE) {
                sim.markAsDead();
                logger.log(
                        "\n*** " + sim.getName()
                        + " has passed away of old age. ***");
            }

            // Pension for retired elders.
            if (sim.getAge() >= GameConstants.ELDER_AGE
                    && sim.getCareer() == Job.UNEMPLOYED) {
                int pension = GameConstants.RETIREMENT_PENSION_INCOME_AMOUNT;
                sim.setMoney(sim.getMoney() + pension);
                logger.log(
                        sim.getName() + " collected a retirement pension of $"
                        + pension);
            }

            // Forced retirement if Sim exceeds job's max age.
            Job currentJob = sim.getCareer();
            if (currentJob != Job.UNEMPLOYED
                    && sim.getAge() > currentJob.getMaxAge()) {
                logger.log(
                        "\n[RETIREMENT] " + sim.getName()
                        + " is too old for " + currentJob.getTitle()
                        + " and must retire.");
                sim.getCareerManager().changeJob(Job.UNEMPLOYED, sim.getName());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the configured number of days per age-up cycle.
     *
     * @return days-per-age-tick value set at construction.
     */
    public int getDaysPerAgeTick() {
        return daysPerAgeTick;
    }
}
