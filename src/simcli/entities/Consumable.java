package simcli.entities;

import simcli.engine.SimulationException;

public class Consumable extends Item {
    protected int satiationValue;
    protected int energyValue;
    protected int happinessValue;

    public Consumable(String name, int price, int satiationValue, int energyValue, int happinessValue) {
        super(name, price);
        this.satiationValue = satiationValue;
        this.energyValue = energyValue;
        this.happinessValue = happinessValue;
    }

    @Override
    public void interact(Sim sim, java.util.Scanner scanner, simcli.engine.TimeManager timeManager) throws SimulationException {
        System.out.println(sim.getName() + " uses the " + this.name + ".");
        sim.getHunger().increase(this.satiationValue);
        sim.getEnergy().increase(this.energyValue);
        sim.getHappiness().increase(this.happinessValue);
        sim.getInventory().remove(this);
    }
    
    @Override
    public Item copyItem() {
        return new Consumable(this.name, this.price, this.satiationValue, this.energyValue, this.happinessValue);
    }
}
