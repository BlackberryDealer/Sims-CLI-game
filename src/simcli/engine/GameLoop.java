package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;
import simcli.entities.models.SimState;
import simcli.utils.GameConstants;

import java.util.List;

/**
 * GameLoop — orchestrates per-tick and per-day processing for the simulation.
 *
 * <p>Each call to {@link #processTick(Sim, int)} advances one simulation tick:
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
     * Processes one player turn, advancing the clock by {@code ticksToAdvance}
     * hours.
     *
     * <p>Each hour is simulated individually:</p>
     * <ol>
     *     <li>Decays needs for every Sim via {@link Sim#tick()}.</li>
     *     <li>Advances the simulation clock by one tick.</li>
     *     <li>If a new day has started, delegates to
     *         {@link #processDayBoundary(int)}.</li>
     * </ol>
     * <p>A random event is rolled once per turn, after the clock has moved.</p>
     *
     * <p>Sims other than the active player are reset to
     * {@link ActionState#IDLE} first, so an action they performed while they
     * were last controlled (e.g. sleeping) does not keep affecting their
     * need decay forever.</p>
     *
     * @param activePlayer   the currently controlled Sim (passed to the event
     *                       manager for targeted events).
     * @param ticksToAdvance the number of hours this turn takes.
     */
    public void processTick(Sim activePlayer, int ticksToAdvance) {
        for (Sim sim : neighborhood) {
            if (sim != activePlayer) {
                sim.setCurrentAction(ActionState.IDLE);
            }
        }

        for (int i = 0; i < ticksToAdvance; i++) {
            int previousDay = timeManager.getCurrentDay();

            for (Sim sim : neighborhood) {
                sim.tick();
            }
            timeManager.advanceTick();

            if (timeManager.getCurrentDay() != previousDay) {
                processDayBoundary(timeManager.getCurrentDay());
            }
        }

        eventManager.trigger(activePlayer, timeManager);
    }

    /**
     * Processes a day boundary.
     *
     * <p>Prints a new-day message, runs truancy checks for the day that just
     * ended, then delegates lifecycle processing (aging, death, retirement)
     * to the {@link LifecycleManager}. Truancy must run first because the
     * lifecycle step resets each Sim's shifts-worked-today counter.</p>
     *
     * @param dayNumber the day that has just begun.
     */
    private void processDayBoundary(int dayNumber) {
        simcli.ui.UIManager.printEvent(
                "*** A new day has begun! Day " + dayNumber + ", "
                + TimeManager.dayOfWeekName(dayNumber) + " ***");

        boolean endedDayWasWorkday = !TimeManager.isWeekend(dayNumber - 1);
        for (Sim s : neighborhood) {
            if (s.getState() != SimState.DEAD) {
                s.getCareerManager().checkTruancy(s.getName(), endedDayWasWorkday);
            }
        }

        // Delegate aging/death/retirement to LifecycleManager (SRP).
        lifecycleManager.processDayForAll(neighborhood);
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
