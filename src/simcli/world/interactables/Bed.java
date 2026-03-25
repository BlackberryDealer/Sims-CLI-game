package simcli.world.interactables;

import simcli.entities.actors.Sim;

public class Bed implements Interactable {
    @Override
    public void interact(Sim sim, java.util.Scanner scanner, simcli.engine.TimeManager timeManager)
            throws simcli.engine.SleepEventException {
        int currentInDay = timeManager.getCurrentTick() % simcli.utils.GameConstants.TICKS_PER_DAY;
        // 8 AM is tick 8 relative to a 24-hour day (0-23)
        // ticks to reach 8 from currentInDay:
        int ticksToMorning = (simcli.utils.GameConstants.TICKS_PER_DAY - currentInDay + 8) % simcli.utils.GameConstants.TICKS_PER_DAY;
        if (ticksToMorning == 0)
            ticksToMorning = simcli.utils.GameConstants.TICKS_PER_DAY;

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