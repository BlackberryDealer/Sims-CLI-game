package simcli.entities.items;

import simcli.utils.GameConstants;

/**
 * A consumable food item that restores hunger and energy but provides
 * no happiness bonus. Purchased from the grocery shelf.
 *
 * <p>Food is a specialized {@link Consumable} with a fixed happiness
 * value of zero, distinguishing basic nutrition from premium consumables
 * that may grant additional mood benefits.</p>
 *
 * <p>Food goes off: it stays fresh for a number of days (see
 * {@link #getFreshness()}), losing a day of freshness each day in a fridge
 * and two a day anywhere else, and is thrown away once it reaches zero.</p>
 */
public class Food extends Consumable {

    /** Days until this food goes off. */
    private int freshness;

    /**
     * Constructs a new food item with no happiness bonus that stays fresh
     * for {@link GameConstants#FOOD_SHELF_LIFE_DAYS} days.
     *
     * @param name           the display name of the food.
     * @param price          the purchase price in Simoleons.
     * @param satiationValue the hunger points restored on consumption.
     * @param energyValue    the energy points restored on consumption.
     */
    public Food(String name, int price, int satiationValue, int energyValue) {
        this(name, price, satiationValue, energyValue, GameConstants.FOOD_SHELF_LIFE_DAYS);
    }

    /**
     * Constructs a new food item with no happiness bonus.
     *
     * @param name           the display name of the food.
     * @param price          the purchase price in Simoleons.
     * @param satiationValue the hunger points restored on consumption.
     * @param energyValue    the energy points restored on consumption.
     * @param freshness      days until the food goes off.
     */
    public Food(String name, int price, int satiationValue, int energyValue, int freshness) {
        super(name, price, satiationValue, energyValue, 0); // Food does not provide fun
        this.freshness = freshness;
    }

    /**
     * Returns how many days this food has left before it goes off.
     *
     * @return days of freshness left.
     */
    public int getFreshness() {
        return freshness;
    }

    /**
     * Ages the food by the given number of days.
     *
     * @param days days of freshness lost.
     * @return {@code true} if the food has now gone off.
     */
    public boolean spoil(int days) {
        freshness = Math.max(0, freshness - days);
        return freshness == 0;
    }

    /**
     * {@inheritDoc}
     *
     * @return a new {@code Food} with identical properties.
     */
    @Override
    public Item copyItem() {
        return new Food(getObjectName(), getPrice(), getSatiationValue(), getEnergyValue(), freshness);
    }

    /**
     * {@inheritDoc}
     *
     * @return a CSV-formatted string: {@code Food,name,price,satiation,energy,freshness}.
     */
    @Override
    public String toSaveString() {
        return String.format("Food,%s,%d,%d,%d,%d", getObjectName(), getPrice(), getSatiationValue(),
                getEnergyValue(), freshness);
    }
}
