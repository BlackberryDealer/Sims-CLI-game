package simcli.entities.items;

import simcli.world.interactables.Interactable;
import simcli.persistence.Savable;

/**
 * Abstract base class for all items that a Sim can own, buy, or interact with.
 * Implements both {@link Interactable} (for in-world usage) and {@link Savable}
 * (for persistence to plain-text save files).
 */
public abstract class Item implements Interactable, Savable {
    private String name;
    private int price;

    public Item(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public int getPrice() {
        return price;
    }

    @Override
    public String getObjectName() {
        return name;
    }

    public abstract Item copyItem();
}
