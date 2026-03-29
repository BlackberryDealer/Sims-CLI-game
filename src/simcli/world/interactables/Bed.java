package simcli.world.interactables;

import simcli.engine.SleepEventException;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.utils.GameConstants;

import java.util.Scanner;

/**
 * A bedroom interactable that puts the Sim to sleep, restoring energy
 * and fast-forwarding the game clock to the next morning (08:00).
 * Triggers a {@link SleepEventException} to signal the engine.
 */
public class Bed implements Interactable {
    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SleepEventException {
        int currentInDay = timeManager.getCurrentTick() % GameConstants.TICKS_PER_DAY;
        // Tick 8 AM relative to a 24-hour day (0-23)
        int ticksToMorning = (GameConstants.TICKS_PER_DAY - currentInDay + GameConstants.MORNING_HOUR)
                % GameConstants.TICKS_PER_DAY;
        if (ticksToMorning == 0)
            ticksToMorning = GameConstants.TICKS_PER_DAY;

        sim.sleep(ticksToMorning);

        // Throw exception to let the game engine render the sleeping ASCII art
        // and pause for the sleep animation BEFORE advancing time.
        throw new simcli.engine.SleepEventException();
    }

    @Override
    public String getObjectName() {
        return "Bed";
    }
}