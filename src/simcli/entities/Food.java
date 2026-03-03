package simcli.entities;

import simcli.engine.SimulationException;

public class Food extends Item {
    private int satiationValue;
    private int energyValue;

    public Food(String name, int price, int satiationValue, int energyValue) {
        super(name, price);
        this.satiationValue = satiationValue;
        this.energyValue = energyValue;
    }

    @Override
    public void interact(Sim sim, java.util.Scanner scanner) throws SimulationException {
        // Consuming food directly from inventory
        System.out.println(sim.getName() + " eats the " + this.name + ". Tasty!");
        sim.getHunger().increase(this.satiationValue);
        sim.getEnergy().increase(this.energyValue);
        sim.getInventory().remove(this);
    }
}
