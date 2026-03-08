package simcli.world.interactables;
import simcli.engine.SimulationException;
import simcli.entities.Sim;

public class StorageChest implements Interactable {
    private boolean isOpened = false;
    
    @Override
    public void interact(Sim sim, java.util.Scanner scanner) throws SimulationException {
        if (!isOpened) {
            System.out.println(sim.getName() + " places the Storage Chest. Inventory capacity increased by +10!");
            sim.setInventoryCapacity(sim.getInventoryCapacity() + 10);
            isOpened = true;
        } else {
            System.out.println(sim.getName() + " rummages through the storage chest.");
        }
    }
    
    @Override
    public String getObjectName() { return "Storage Chest"; }
}
