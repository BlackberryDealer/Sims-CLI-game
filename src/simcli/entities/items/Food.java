package simcli.entities.items;

/**
 * A consumable food item that restores hunger and energy but provides
 * no happiness bonus. Purchased from the grocery shelf.
 *
 * <p>Food is a specialized {@link Consumable} with a fixed happiness
 * value of zero, distinguishing basic nutrition from premium consumables
 * that may grant additional mood benefits.</p>
 */
public class Food extends Consumable {

    /**
     * Constructs a new food item with no happiness bonus.
     *
     * @param name           the display name of the food.
     * @param price          the purchase price in Simoleons.
     * @param satiationValue the hunger points restored on consumption.
     * @param energyValue    the energy points restored on consumption.
     */
    public Food(String name, int price, int satiationValue, int energyValue) {
        super(name, price, satiationValue, energyValue, 0); // Food does not provide fun
    }

    /**
     * {@inheritDoc}
     *
     * @return a new {@code Food} with identical properties.
     */
    @Override
    public Item copyItem() {
        return new Food(getObjectName(), getPrice(), getSatiationValue(), getEnergyValue());
    }

    /**
     * {@inheritDoc}
     *
     * @return a CSV-formatted string: {@code Food,name,price,satiation,energy}.
     */
    @Override
    public String toSaveString() {
        return String.format("Food,%s,%d,%d,%d", getObjectName(), getPrice(), getSatiationValue(), getEnergyValue());
    }
}
