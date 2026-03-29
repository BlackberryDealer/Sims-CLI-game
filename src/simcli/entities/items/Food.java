package simcli.entities.items;

/**
 * A consumable food item that restores hunger and energy but provides
 * no happiness bonus. Purchased from the grocery shelf.
 */
public class Food extends Consumable {
    public Food(String name, int price, int satiationValue, int energyValue) {
        super(name, price, satiationValue, energyValue, 0); // Food does not provide fun
    }

    @Override
    public Item copyItem() {
        return new Food(getObjectName(), getPrice(), getSatiationValue(), getEnergyValue());
    }

    @Override
    public String toSaveString() {
        return String.format("Food,%s,%d,%d,%d", getObjectName(), getPrice(), getSatiationValue(), getEnergyValue());
    }
}
