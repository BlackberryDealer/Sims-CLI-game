package simcli.world.interactables;
import simcli.entities.Sim;
import simcli.world.Interactable;

public class Bed implements Interactable {
    @Override
    public void interact(Sim sim) {
        System.out.println(sim.getName() + " sleeps deeply in the bed.");
        sim.getEnergy().increase(50);
        sim.getHunger().decrease(10); // Wakes up hungry
    }
    
    @Override
    public String getObjectName() { return "Bed"; }
}