package simcli.world.interactables;

import simcli.entities.Sim;
import simcli.engine.SimulationException;

public class Fridge implements Interactable {
    @Override
    public void interact(Sim sim, java.util.Scanner scanner) throws SimulationException {
        simcli.entities.Food foodToEat = null;
        for (simcli.entities.Item item : sim.getInventory()) {
            if (item instanceof simcli.entities.Food) {
                foodToEat = (simcli.entities.Food) item;
                break;
            }
        }
        
        if (foodToEat == null) {
            throw new SimulationException("The fridge is empty! You need to buy food.");
        }
        
        System.out.println(sim.getName() + " decides to eat " + foodToEat.getObjectName() + " from the fridge.");
        foodToEat.interact(sim, scanner);
    }
    
    @Override
    public String getObjectName() { return "Fridge"; }
}