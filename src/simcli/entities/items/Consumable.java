package simcli.entities.items;

import simcli.entities.actors.Sim;

import simcli.engine.SimulationException;

public class Consumable extends Item {
    private int satiationValue;
    private int energyValue;
    private int funValue;

    public Consumable(String name, int price, int satiationValue, int energyValue, int funValue) {
        super(name, price);
        this.satiationValue = satiationValue;
        this.energyValue = energyValue;
        this.funValue = funValue;
    }

    public int getSatiationValue() { return satiationValue; }
    public int getEnergyValue() { return energyValue; }
    public int getFunValue() { return funValue; }

    @Override
    public void interact(Sim sim, java.util.Scanner scanner, simcli.engine.TimeManager timeManager) throws SimulationException {
        sim.setCurrentAction(simcli.entities.models.ActionState.EATING);
        simcli.ui.UIManager.displayActionAnimation(sim);
        simcli.ui.UIManager.printMessage(sim.getName() + " consumes the " + getObjectName() + ".");
        sim.getHunger().increase(this.satiationValue);
        sim.getEnergy().increase(this.energyValue);
        sim.getFun().increase(this.funValue);
        sim.getInventory().remove(this);
    }
    
    @Override
    public Item copyItem() {
        return new Consumable(getObjectName(), getPrice(), this.satiationValue, this.energyValue, this.funValue);
    }
}
