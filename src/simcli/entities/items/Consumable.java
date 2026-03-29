package simcli.entities.items;

import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;

import simcli.engine.SimulationException;
import simcli.entities.models.ActionState;
import simcli.ui.UIManager;

import java.util.Scanner;

/**
 * An item that can be eaten by a Sim, restoring hunger (satiation),
 * energy, and happiness. Consumed items are removed from the inventory
 * after use.
 */
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

    public int getSatiationValue() {
        return satiationValue;
    }

    public int getEnergyValue() {
        return energyValue;
    }

    public int getHappinessValue() {
        return happinessValue;
    }

    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException {
        sim.setCurrentAction(ActionState.EATING);
        UIManager.displayActionAnimation(sim);
        UIManager.printMessage(sim.getName() + " consumes the " + getObjectName() + ".");
        sim.eat(this);
    }

    @Override
    public Item copyItem() {
        return new Consumable(getObjectName(), getPrice(), this.satiationValue, this.energyValue, this.happinessValue);
    }

    @Override
    public String toSaveString() {
        return String.format("Consumable,%s,%d,%d,%d,%d", getObjectName(), getPrice(), getSatiationValue(),
                getEnergyValue(), getHappinessValue());
    }
}
