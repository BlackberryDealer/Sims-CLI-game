package simcli.entities.managers;

import simcli.entities.items.Item;

import java.util.ArrayList;
import java.util.List;

public class InventoryManager {
    private List<Item> inventory;
    private int capacity;

    public InventoryManager(int capacity) {
        this.inventory = new ArrayList<>();
        this.capacity = capacity;
    }

    public List<Item> getItems() {
        return inventory;
    }

    public void addItem(Item item) {
        this.inventory.add(item);
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
