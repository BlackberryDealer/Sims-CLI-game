package simcli.engine;

import simcli.entities.actors.Sim;

import java.util.List;

/**
 * LifecycleManager — the single authority for Sim aging in the simulation.
 *
 * <h2>Responsibility (Single Responsibility Principle)</h2>
 * <p>This class does exactly one thing: track elapsed ticks and call
 * {@link Sim#growOlderDaily()} on every Sim in the neighborhood when a
 * full in-game day has elapsed. It knows nothing about needs, inventory,
 * jobs, or rendering. Those concerns belong elsewhere.</p>
 *
 * <h2>How it fits into the State Pattern</h2>
 * <p>Daily aging ({@code growOlderDaily()}) internally triggers
 * {@link Sim#ageUp()} when enough days have passed for a birthday.
 * {@code ageUp()} in turn calls {@code currentStage.getNextStage(age)}
 * on the Sim's private stage, performing the State Pattern transition
 * polymorphically. This manager never touches the stage directly.</p>
 *
 * <h2>Encapsulation</h2>
 * <p>Both fields are {@code private final} with public read-only getters.
 * External code cannot manipulate the tick clock, preventing inconsistent state.</p>
 */
public class LifecycleManager {

    /**
     * How many simulation ticks equal one in-game day.
     */
    private final int ticksPerDay;

    /**
     * Total ticks processed since this manager was created.
     * Using {@code long} prevents overflow in long-running simulations.
     */
    private long currentTick;

    /**
     * Creates a new {@code LifecycleManager}.
     *
     * @param ticksPerDay number of ticks that constitute one in-game day.
     *                    Must be >= 1.
     * @throws IllegalArgumentException if {@code ticksPerDay} is less than 1.
     */
    public LifecycleManager(int ticksPerDay) {
        if (ticksPerDay < 1) {
            throw new IllegalArgumentException(
                    "ticksPerDay must be >= 1, but was: " + ticksPerDay);
        }
        this.ticksPerDay = ticksPerDay;
        this.currentTick = 0L;
    }

    /**
     * Advances the simulation clock by one tick.
     *
     * <p>When the tick count crosses a day boundary (becomes divisible by
     * {@link #ticksPerDay}), {@link Sim#growOlderDaily()} is called on
     * every Sim in the neighborhood, triggering the daily aging cycle.</p>
     *
     * @param neighborhood the list of all Sims in the household; must not be {@code null}.
     * @throws IllegalArgumentException if {@code neighborhood} is {@code null}.
     */
    public void processTick(List<Sim> neighborhood) {
        if (neighborhood == null) {
            throw new IllegalArgumentException(
                    "Cannot process a tick for a null neighborhood.");
        }

        long previousDay = this.currentTick / this.ticksPerDay;
        this.currentTick++;
        long currentDay = this.currentTick / this.ticksPerDay;

        // Has a full day elapsed?
        if (currentDay > previousDay) {
            for (Sim sim : neighborhood) {
                sim.growOlderDaily();
            }
        }
    }

    /**
     * Returns the configured number of ticks per in-game day.
     *
     * @return ticks-per-day value set at construction.
     */
    public int getTicksPerDay() {
        return ticksPerDay;
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
