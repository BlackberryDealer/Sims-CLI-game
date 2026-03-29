package simcli.world.interactables;

import simcli.engine.SleepEventException;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.utils.GameConstants;

import java.util.Scanner;

/**
 * Represents a Bed location or interactable object.
 */
public class Bed implements Interactable {
    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SleepEventException {
        int currentInDay = timeManager.getCurrentTick() % GameConstants.TICKS_PER_DAY;
        // 8 AM is tick 8 relative to a 24-hour day (0-23)
        // ticks to reach 8 from currentInDay:
        int ticksToMorning = (GameConstants.TICKS_PER_DAY - currentInDay + 8) % GameConstants.TICKS_PER_DAY;
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