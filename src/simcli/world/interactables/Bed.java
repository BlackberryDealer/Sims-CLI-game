package simcli.world.interactables;
import simcli.entities.Sim;

public class Bed implements Interactable {
    @Override
    public void interact(Sim sim, java.util.Scanner scanner) {
        System.out.println(sim.getName() + " sleeps deeply in the bed.");
        simcli.ui.UIManager.sleepAnimation();
        sim.getEnergy().increase(50);
        sim.getHunger().decrease(10); // Wakes up hungry
    }
    
    @Override
    public String getObjectName() { return "Bed"; }
}