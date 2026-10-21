package simcli.entities.items;

import simcli.utils.GameConstants;

/**
 * Raw food that is cheap but barely filling on its own, and becomes a
 * proper meal when cooked at a fridge.
 *
 * <p>Eating an ingredient raw restores only its (low) satiation value;
 * {@link #cook()} turns it into a {@link Food} meal that restores much
 * more.</p>
 */
public class Ingredient extends Food {

    /** The name of the meal this ingredient cooks into. */
    private final String mealName;

    /** The hunger the cooked meal restores. */
    private final int mealSatiation;

    /**
     * Constructs a new ingredient.
     *
     * @param name          the display name of the ingredient.
     * @param price         the purchase price in Simoleons.
     * @param rawSatiation  the hunger restored if eaten raw.
     * @param mealName      the name of the cooked meal.
     * @param mealSatiation the hunger restored by the cooked meal.
     * @param freshness     days until the ingredient goes off.
     */
    public Ingredient(String name, int price, int rawSatiation, String mealName, int mealSatiation, int freshness) {
        super(name, price, rawSatiation, 0, freshness);
        this.mealName = mealName;
        this.mealSatiation = mealSatiation;
    }

    /**
     * Returns the name of the meal this cooks into.
     *
     * @return the meal name.
     */
    public String getMealName() {
        return mealName;
    }

    /**
     * Returns the hunger the cooked meal restores.
     *
     * @return the meal's satiation value.
     */
    public int getMealSatiation() {
        return mealSatiation;
    }

    /**
     * Cooks this ingredient into a meal that stays fresh for
     * {@link GameConstants#MEAL_SHELF_LIFE_DAYS} days.
     *
     * @return the cooked meal.
     */
    public Food cook() {
        return new Food(mealName, getPrice(), mealSatiation, 0, GameConstants.MEAL_SHELF_LIFE_DAYS);
    }

    @Override
    public Item copyItem() {
        return new Ingredient(getObjectName(), getPrice(), getSatiationValue(), mealName, mealSatiation,
                getFreshness());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code Ingredient,name,price,rawSatiation,mealName,mealSatiation,freshness}.
     */
    @Override
    public String toSaveString() {
        return String.format("Ingredient,%s,%d,%d,%s,%d,%d", getObjectName(), getPrice(), getSatiationValue(),
                mealName, mealSatiation, getFreshness());
    }
}
