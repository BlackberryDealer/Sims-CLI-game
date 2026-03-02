package simcli.world.interactables;

import simcli.entities.Sim;
import simcli.engine.SimulationException;

public class Fridge implements Interactable {
    @Override
    public void interact(Sim sim) throws SimulationException {
        if (sim.getGroceries() <= 0) {
            throw new SimulationException("The fridge is empty! You need to buy groceries.");
        }
        
        System.out.println(sim.getName() + " cooks a meal using groceries from the fridge.");
        sim.setGroceries(sim.getGroceries() - 1);
        sim.getHunger().increase(40); 
    }
    
    @Override
    public String getObjectName() { return "Fridge"; }
}