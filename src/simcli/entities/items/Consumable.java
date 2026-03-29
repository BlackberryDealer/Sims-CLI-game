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
 *
 * <p>Consumables are the parent class for {@link Food} and can also
 * represent specialty items purchased from shops that provide a
 * happiness bonus on top of the standard nutritional values.</p>
 */
public class Consumable extends Item {

    /** The amount of hunger restored when consumed. */
    private int satiationValue;

    /** The amount of energy restored when consumed. */
    private int energyValue;

    /** The amount of happiness restored when consumed. */
    private int happinessValue;

    /**
     * Constructs a new consumable item.
     *
     * @param name           the display name of the consumable.
     * @param price          the purchase price in Simoleons.
     * @param satiationValue the hunger points restored on consumption.
     * @param energyValue    the energy points restored on consumption.
     * @param happinessValue the happiness points restored on consumption.
     */
    public Consumable(String name, int price, int satiationValue, int energyValue, int happinessValue) {
        super(name, price);
        this.satiationValue = satiationValue;
        this.energyValue = energyValue;
        this.happinessValue = happinessValue;
    }

    /**
     * Returns the hunger restoration value.
     *
     * @return satiation points.
     */
    public int getSatiationValue() {
        return satiationValue;
    }

    /**
     * Returns the energy restoration value.
     *
     * @return energy points.
     */
    public int getEnergyValue() {
        return energyValue;
    }

    /**
     * Returns the happiness restoration value.
     *
     * @return happiness points.
     */
    public int getHappinessValue() {
        return happinessValue;
    }

    /**
     * Consumes this item: sets the Sim's action to EATING, plays the
     * eating animation, and calls {@link Sim#eat(Consumable)} to apply
     * the stat effects and remove the item from inventory.
     *
     * {@inheritDoc}
     */
    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException {
        sim.setCurrentAction(ActionState.EATING);
        UIManager.displayActionAnimation(sim);
        UIManager.printMessage(sim.getName() + " consumes the " + getObjectName() + ".");
        sim.eat(this);
    }

    /**
     * {@inheritDoc}
     *
     * @return a new {@code Consumable} with identical properties.
     */
    @Override
    public Item copyItem() {
        return new Consumable(getObjectName(), getPrice(), this.satiationValue, this.energyValue, this.happinessValue);
    }

    /**
     * {@inheritDoc}
     *
     * @return a CSV-formatted string: {@code Consumable,name,price,satiation,energy,happiness}.
     */
    @Override
    public String toSaveString() {
        return String.format("Consumable,%s,%d,%d,%d,%d", getObjectName(), getPrice(), getSatiationValue(),
                getEnergyValue(), getHappinessValue());
    }
}
