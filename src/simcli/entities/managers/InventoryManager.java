package simcli.entities.managers;

import simcli.entities.items.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a Sim's item inventory with a configurable capacity limit.
 *
 * <p>Items can be added, listed, and the capacity upgraded at runtime.
 * Extracted from {@link simcli.entities.actors.Sim} to follow the
 * Single Responsibility Principle.</p>
 */
public class InventoryManager {

    /** The list of items currently held by the Sim. */
    private List<Item> inventory;

    /** The maximum number of items the Sim can carry. */
    private int capacity;

    /**
     * Constructs a new InventoryManager with the given capacity.
     *
     * @param capacity the maximum number of items the Sim can hold.
     */
    public InventoryManager(int capacity) {
        this.inventory = new ArrayList<>();
        this.capacity = capacity;
    }

    /**
     * Returns the list of items in the inventory.
     *
     * @return the inventory contents as a mutable list.
     */
    public List<Item> getItems() {
        return inventory;
    }

    /**
     * Adds an item to the inventory.
     *
     * @param item the item to add.
     */
    public void addItem(Item item) {
        this.inventory.add(item);
    }

    /**
     * Returns the maximum inventory capacity.
     *
     * @return the capacity limit.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Sets the inventory capacity. Used when upgrading rooms or restoring saves.
     *
     * @param capacity the new capacity limit.
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
