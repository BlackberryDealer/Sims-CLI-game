package simcli.world.interactables;

import simcli.entities.items.Item;
import java.util.List;

/**
 * Abstract component for any item that can hold other items.
 * Can be interacted with to deposit or retrieve inventory.
 */
public interface Container {
    List<Item> getStoredItems();
    void storeItem(Item item);
    Item retrieveItem(int index);
}
