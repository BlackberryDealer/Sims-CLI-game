package simcli.world.interactables;

import simcli.entities.Sim;
import simcli.engine.SimulationException;

public class GroceryShelf implements Interactable {
    private final int GROCERY_COST = 50;

    @Override
    public void interact(Sim sim) throws SimulationException {
        if (sim.getMoney() < GROCERY_COST) {
            throw new SimulationException("Not enough Simoleons! Groceries cost $" + GROCERY_COST);
        }
        
        System.out.println(sim.getName() + " buys a bag of groceries for $" + GROCERY_COST + ".");
        sim.setMoney(sim.getMoney() - GROCERY_COST);
        sim.setGroceries(sim.getGroceries() + 1);
    }
    
    @Override
    public String getObjectName() { return "Grocery Shelf"; }
}