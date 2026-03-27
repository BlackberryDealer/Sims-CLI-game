package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.ui.UIManager;

import java.util.List;

/**
 * Drives the per-tick simulation loop: advances time, triggers need decay,
 * fires random events, and delegates aging to {@link LifecycleManager}.
 */
public class GameLoop {
    private final TimeManager timeManager;
    private final List<Sim> neighborhood;
    private final RandomEventManager eventManager;
    private final LifecycleManager lifecycleManager;

    public GameLoop(TimeManager timeManager, List<Sim> neighborhood,
                    RandomEventManager eventManager, LifecycleManager lifecycleManager) {
        this.timeManager = timeManager;
        this.neighborhood = neighborhood;
        this.eventManager = eventManager;
        this.lifecycleManager = lifecycleManager;
    }

    /**
     * Processes a single game tick: decays needs, advances time,
     * triggers random events, and handles day boundary logic.
     *
     * @param activePlayer the currently controlled Sim
     */
    public void processTick(Sim activePlayer) {
        int previousDay = timeManager.getCurrentDay();

        for (Sim sim : neighborhood) {
            sim.tick();
        }

        timeManager.advanceTick();
        lifecycleManager.processTick(neighborhood);
        eventManager.trigger(activePlayer, timeManager);

        if (timeManager.getCurrentDay() > previousDay) {
            processDayBoundary();
        }
    }

    /**
     * Handles new-day logic: prints the day message and checks truancy.
     * Aging is now handled by {@link LifecycleManager}, not here.
     */
    private void processDayBoundary() {
        UIManager.printMessage("\n*** A new day has begun! (Day " + timeManager.getCurrentDay() + ") ***");
        for (Sim s : neighborhood) {
            s.getCareerManager().checkTruancy(s.getName());
        }
    }
}
