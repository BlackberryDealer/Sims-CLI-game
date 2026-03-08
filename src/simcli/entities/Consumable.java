package simcli.entities;

import simcli.engine.SimulationException;

public class Consumable extends Item {
    private int satiationValue;
    private int energyValue;
    private int happinessValue;

    public Consumable(String name, int price, int satiationValue, int energyValue, int happinessValue) {
        super(name, price);
        this.satiationValue = satiationValue;
        this.energyValue = energyValue;
        this.happinessValue = happinessValue;
    }

    @Override
    public void interact(Sim sim, java.util.Scanner scanner) throws SimulationException {
        System.out.println(sim.getName() + " uses the " + this.name + ".");
        sim.getHunger().increase(this.satiationValue);
        sim.getEnergy().increase(this.energyValue);
        sim.getHappiness().increase(this.happinessValue);
        sim.getInventory().remove(this);
    }
}
