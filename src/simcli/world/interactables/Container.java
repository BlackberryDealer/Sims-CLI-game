package simcli.world.interactables;

import simcli.entities.items.Item;
import java.util.List;

/**
 * Represents a Container location or interactable object.
 */
public interface Container {
    List<Item> getStoredItems();
    void storeItem(Item item);
    Item retrieveItem(int index);
}
