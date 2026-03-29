package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.utils.GameConstants;

import java.util.List;

/**
 * GameLoop — orchestrates per-tick and per-day processing for the simulation.
 *
 * <p>Each call to {@link #processTick(Sim)} advances one simulation tick:
 * decaying needs for all Sims, advancing the clock, triggering random events,
 * and — when a new day begins — delegating lifecycle processing to the
 * {@link LifecycleManager}.</p>
 *
 * <h2>Ownership</h2>
 * <p>This class owns the creation of {@link RandomEventManager} and
 * {@link LifecycleManager}, keeping subsystem factories close to their
 * consumers and reducing the construction burden on {@link GameEngine}.</p>
 */
public class GameLoop {
    private final TimeManager timeManager;
    private final List<Sim> neighborhood;
    private final RandomEventManager eventManager;
    private final LifecycleManager lifecycleManager;

    /**
     * Creates a new GameLoop, instantiating its own event and lifecycle managers.
     *
     * @param timeManager  tracks tick/day/time-of-day progression.
     * @param neighborhood every Sim in the current game world.
     * @param logger       the simulation logger for event and lifecycle messages.
     */
    public GameLoop(TimeManager timeManager, List<Sim> neighborhood,
                    SimulationLogger logger) {
        this.timeManager = timeManager;
        this.neighborhood = neighborhood;
        this.eventManager = new RandomEventManager(logger);
        this.lifecycleManager = new LifecycleManager(
                GameConstants.DAYS_PER_AGE_TICK, logger);
    }

    /**
     * Processes one simulation tick.
     *
     * <ol>
     *     <li>Decays needs for every Sim via {@link Sim#tick()}.</li>
     *     <li>Advances the simulation clock by one tick.</li>
     *     <li>Triggers any random events.</li>
     *     <li>If a new day has started, delegates to
     *         {@link #processDayBoundary()}.</li>
     * </ol>
     *
     * @param activePlayer the currently controlled Sim (passed to the event
     *                     manager for targeted events).
     */
    public void processTick(Sim activePlayer, int ticksToAdvance) {
        int previousDay = timeManager.getCurrentDay();

        for (Sim sim : neighborhood) {
            sim.tick();
        }

        timeManager.advanceTicks(ticksToAdvance);
        eventManager.trigger(activePlayer, timeManager);

        int currentDay = timeManager.getCurrentDay();
        int daysCrossed = currentDay - previousDay;
        
        for (int i = 0; i < daysCrossed; i++) {
            processDayBoundary(previousDay + 1 + i);
        }
    }

    /**
     * Processes a day boundary.
     *
     * <p>Prints a new-day message, then delegates all lifecycle processing
     * (aging, death, retirement, truancy checks) to the
     * {@link LifecycleManager}. This keeps the GameLoop thin — it only
     * coordinates; the lifecycle rules live in their own class.</p>
     */
    private void processDayBoundary(int dayNumber) {
        simcli.ui.UIManager.printMessage(
                "\n*** A new day has begun! (Day "
                + dayNumber + ") ***");

        // Delegate aging/death/retirement to LifecycleManager (SRP).
        lifecycleManager.processDayForAll(neighborhood);

        // Truancy checks remain here since CareerManager is already
        // reset inside LifecycleManager.processDay().
        for (Sim s : neighborhood) {
            s.getCareerManager().checkTruancy(s.getName());
        }
    }

    /** Returns the event manager owned by this loop. */
    public RandomEventManager getRandomEventManager() {
        return eventManager;
    }

    /** Returns the lifecycle manager owned by this loop. */
    public LifecycleManager getLifecycleManager() {
        return lifecycleManager;
    }
}
