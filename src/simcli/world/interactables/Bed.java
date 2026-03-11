package simcli.world.interactables;
import simcli.entities.Sim;

public class Bed implements Interactable {
    @Override
    public void interact(Sim sim, java.util.Scanner scanner, simcli.engine.TimeManager timeManager) {
        int currentInDay = timeManager.getCurrentTick() % 24;
        // 8 AM is tick 8 relative to a 24-hour day (0-23)
        // ticks to reach 8 from currentInDay:
        int ticksToMorning = (24 - currentInDay + 8) % 24;
        if (ticksToMorning == 0) ticksToMorning = 24;

        simcli.ui.UIManager.printMessage(sim.getName() + " sleeps deeply in the bed for " + ticksToMorning + " hours.");
        simcli.ui.UIManager.sleepAnimation();
        
        int energyGain = Math.min(100, 15 * ticksToMorning);
        int hungerLoss = 3 * ticksToMorning;

        sim.getEnergy().increase(energyGain);
        sim.getHunger().decrease(hungerLoss); 
        
        timeManager.advanceTicks(ticksToMorning - 1);
    }
    
    @Override
    public String getObjectName() { return "Bed"; }
}