package simcli.engine;

import simcli.entities.actors.Sim;

import java.util.List;

/**
 * GameLoop — orchestrates per-tick and per-day processing for the simulation.
 *
 * <p>Each call to {@link #processTick(Sim)} advances one simulation tick:
 * decaying needs for all Sims, advancing the clock, triggering random events,
 * and — when a new day begins — delegating lifecycle processing to the
 * {@link LifecycleManager}.</p>
 */
public class GameLoop {
    private final TimeManager timeManager;
    private final List<Sim> neighborhood;
    private final RandomEventManager eventManager;
    private final LifecycleManager lifecycleManager;

    /**
     * Creates a new GameLoop with all required collaborators.
     *
     * @param timeManager      tracks tick/day/time-of-day progression.
     * @param neighborhood     every Sim in the current game world.
     * @param eventManager     fires random events each tick.
     * @param lifecycleManager handles aging, death, and retirement on day
     *                         boundaries.
     */
    public GameLoop(TimeManager timeManager, List<Sim> neighborhood,
                    RandomEventManager eventManager,
                    LifecycleManager lifecycleManager) {
        this.timeManager = timeManager;
        this.neighborhood = neighborhood;
        this.eventManager = eventManager;
        this.lifecycleManager = lifecycleManager;
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
    public void processTick(Sim activePlayer) {
        int previousDay = timeManager.getCurrentDay();

        for (Sim sim : neighborhood) {
            sim.tick();
        }

        timeManager.advanceTick();
        eventManager.trigger(activePlayer, timeManager);

        if (timeManager.getCurrentDay() > previousDay) {
            processDayBoundary();
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
    private void processDayBoundary() {
        simcli.ui.UIManager.printMessage(
                "\n*** A new day has begun! (Day "
                + timeManager.getCurrentDay() + ") ***");

        // Delegate aging/death/retirement to LifecycleManager (SRP).
        lifecycleManager.processDayForAll(neighborhood);

        // Truancy checks remain here since CareerManager is already
        // reset inside LifecycleManager.processDay().
        for (Sim s : neighborhood) {
            s.getCareerManager().checkTruancy(s.getName());
        }
    }
}
