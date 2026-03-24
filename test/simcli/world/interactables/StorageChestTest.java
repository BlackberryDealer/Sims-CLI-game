package simcli.world.interactables;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.entities.items.Food;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Storage Chest Container Logic")
public class StorageChestTest {

    @Test
    @DisplayName("Can safely insert and retrieve memory references for Items")
    void testStoreAndRetrieveItem() {
        StorageChest chest = new StorageChest();
        Food apple = new Food("Apple", 5, 5, 5);
        
        chest.storeItem(apple);
        assertEquals(1, chest.getStoredItems().size(), "Item should be pushed to Chest array");
        
        Food retrieved = (Food) chest.retrieveItem(0);
        assertEquals(apple, retrieved, "The retrieved object reference must exactly match the deposited object");
        assertEquals(0, chest.getStoredItems().size(), "Retrieving item cleanly pops it out of the Chest array");
    }
}
