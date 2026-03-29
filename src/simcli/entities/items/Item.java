package simcli.entities.items;

import simcli.world.interactables.Interactable;
import simcli.persistence.Savable;

/**
 * Abstract base class for all items that a Sim can own, buy, or interact with.
 *
 * <p>Implements both {@link Interactable} (for in-world usage) and {@link Savable}
 * (for persistence to plain-text save files). Concrete subclasses include
 * {@link Consumable} (edible items) and {@link Furniture} (placeable items).</p>
 */
public abstract class Item implements Interactable, Savable {

    /** The display name of this item. */
    private String name;

    /** The purchase price of this item in Simoleons. */
    private int price;

    /**
     * Constructs a new item with the given name and price.
     *
     * @param name  the display name of the item.
     * @param price the purchase price in Simoleons.
     */
    public Item(String name, int price) {
        this.name = name;
        this.price = price;
    }

    /**
     * Returns the purchase price of this item.
     *
     * @return the price in Simoleons.
     */
    public int getPrice() {
        return price;
    }

    /**
     * {@inheritDoc}
     *
     * @return the display name of this item.
     */
    @Override
    public String getObjectName() {
        return name;
    }

    /**
     * Creates a deep copy of this item for inventory/shop duplication.
     *
     * @return a new {@code Item} instance with the same properties.
     */
    public abstract Item copyItem();
}
