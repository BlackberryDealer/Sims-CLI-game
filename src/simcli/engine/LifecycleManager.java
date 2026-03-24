package simcli.engine;

import simcli.entities.actors.Sim;

/**
 * LifecycleManager — drives simulation time and fires age transitions.
 *
 * <h2>Responsibility (Single Responsibility Principle)</h2>
 * <p>This class does exactly one thing: count ticks and call
 * {@link Sim#ageUp()} when a full year has elapsed. It knows nothing about
 * needs, inventory, jobs, or rendering. Those concerns belong elsewhere.</p>
 *
 * <h2>How it fits into the State Pattern</h2>
 * <p>{@code processTick()} calls {@link Sim#ageUp()}, which in turn calls
 * {@code currentStage.getNextStage(age)} on the Sim's private stage. If the
 * stage returns a new object, the Sim swaps its "brain" polymorphically.
 * This manager never touches the stage directly — it only tells the Sim that
 * a birthday has occurred.</p>
 *
 * <h2>Encapsulation</h2>
 * <p>Both fields are {@code private} with public read-only getters. External
 * code cannot manipulate the tick clock, preventing inconsistent state.</p>
 */
public class LifecycleManager {

    // -------------------------------------------------------------------------
    // Private Fields
    // -------------------------------------------------------------------------

    /**
     * How many simulation ticks equal one in-game year.
     * Set to a small number (e.g. {@code 1} or {@code 3}) for fast testing;
     * set to {@code 365} or {@code 24 * 365} for realistic gameplay.
     */
    private final int ticksPerYear;

    /**
     * Total ticks processed since this manager was created.
     * Using {@code long} prevents overflow in long-running simulations.
     */
    private long currentTick;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new {@code LifecycleManager}.
     *
     * @param ticksPerYear number of ticks that constitute one in-game year.
     *                     Must be &gt;= 1.
     * @throws IllegalArgumentException if {@code ticksPerYear} is less than 1.
     */
    public LifecycleManager(int ticksPerYear) {
        if (ticksPerYear < 1) {
            throw new IllegalArgumentException(
                    "ticksPerYear must be >= 1, but was: " + ticksPerYear);
        }
        this.ticksPerYear = ticksPerYear;
        this.currentTick  = 0L;
    }

    // -------------------------------------------------------------------------
    // Core Method
    // -------------------------------------------------------------------------

    /**
     * Advances the simulation clock by one tick.
     *
     * <p>When the number of elapsed ticks becomes evenly divisible by
     * {@link #ticksPerYear}, a full in-game year has passed and
     * {@link Sim#ageUp()} is called on the provided Sim. The Sim's
     * {@code ageUp()} method then performs the State Pattern transition check
     * internally — this manager never inspects or modifies the stage directly.</p>
     *
     * <p>Game-loop usage:</p>
     * <pre>
     *   LifecycleManager lm = new LifecycleManager(365);
     *   Sim alice = new Sim("Alice", 17, Gender.FEMALE);
     *   for (int i = 0; i &lt; 365; i++) {
     *       lm.processTick(alice); // after 365 ticks, Alice.ageUp() fires
     *   }
     * </pre>
     *
     * @param sim the {@link Sim} whose age to manage; must not be {@code null}.
     * @throws IllegalArgumentException if {@code sim} is {@code null}.
     */
    public void processTick(Sim sim) {
        if (sim == null) {
            throw new IllegalArgumentException(
                    "Cannot process a tick for a null Sim.");
        }

        // Advance the clock.
        this.currentTick++;

        // Has a full year elapsed?
        if (this.currentTick % this.ticksPerYear == 0) {
            simcli.ui.UIManager.printMessage(
                "[LifecycleManager] Year boundary reached at tick "
                + this.currentTick + ". Calling ageUp() on '"
                + sim.getName() + "'..."
            );
            // Delegate to the Sim — the State Pattern transition happens inside
            // Sim.ageUp() via the currentStage reference. This manager is
            // completely decoupled from the concrete stage classes.
            sim.ageUp();
        }
    }

    // -------------------------------------------------------------------------
    // Getters (read-only access to private fields)
    // -------------------------------------------------------------------------

    /**
     * Returns the configured number of ticks per in-game year.
     *
     * @return ticks-per-year value set at construction.
     */
    public int getTicksPerYear() {
        return ticksPerYear;
    }

    /**
     * Returns the total number of ticks processed so far.
     *
     * @return the current tick count as a {@code long}.
     */
    public long getCurrentTick() {
        return currentTick;
    }
}
