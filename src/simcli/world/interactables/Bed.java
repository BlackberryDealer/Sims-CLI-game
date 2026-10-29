package simcli.world.interactables;

import simcli.engine.ActionCancelledException;
import simcli.engine.SimulationException;
import simcli.engine.SleepEventException;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.ui.UIManager;
import simcli.utils.GameConstants;

import java.util.Scanner;

/**
 * A bedroom interactable that puts the Sim to sleep, restoring energy
 * and fast-forwarding the game clock to the next morning (08:00).
 * Triggers a {@link SleepEventException} to signal the engine.
 *
 * <p>A very hungry Sim is warned that they may starve overnight and asked
 * to confirm first.</p>
 */
public class Bed implements Interactable {
    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException {
        int currentInDay = timeManager.getCurrentTick() % GameConstants.TICKS_PER_DAY;
        // Tick 8 AM relative to a 24-hour day (0-23)
        int ticksToMorning = (GameConstants.TICKS_PER_DAY - currentInDay + GameConstants.MORNING_HOUR)
                % GameConstants.TICKS_PER_DAY;
        if (ticksToMorning == 0)
            ticksToMorning = GameConstants.TICKS_PER_DAY;

        // going to bed hungry can mean starving overnight, so check first
        if (sim.getHunger().getValue() <= GameConstants.HUNGER_WARNING_LEVEL) {
            int hungerByMorning = Math.max(0, sim.getHunger().getValue() - overnightHunger(ticksToMorning));
            UIManager.printWarning(sim.getName() + " is very hungry (" + sim.getHunger().getValue()
                    + ") and will be down to about " + hungerByMorning + " by morning"
                    + (hungerByMorning == 0 ? " - they could starve in their sleep!" : "."));
            UIManager.prompt("Sleep anyway? (Y/N)> ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) {
                UIManager.info("Good idea - eat something first.");
                throw new ActionCancelledException();
            }
        }

        sim.sleep(ticksToMorning);
        afterSleep(sim, ticksToMorning);

        // Throw exception to let the game engine render the sleeping ASCII art
        // and pause for the sleep animation BEFORE advancing time.
        throw new simcli.engine.SleepEventException();
    }

    /**
     * Returns how much hunger a night in this bed costs.
     *
     * @param hours hours until morning.
     * @return the hunger lost overnight.
     */
    protected int overnightHunger(int hours) {
        return 3 * hours;
    }

    /**
     * Called once the Sim has gone to sleep, for better beds to add their
     * comforts. A plain bed adds nothing.
     *
     * @param sim   the sleeping Sim.
     * @param hours hours until morning.
     */
    protected void afterSleep(Sim sim, int hours) {
    }

    @Override
    public String getObjectName() {
        return "Bed";
    }
}