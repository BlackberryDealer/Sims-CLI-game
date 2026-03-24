package simcli.entities.components;

import simcli.entities.managers.InventoryManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.entities.items.Food;
import simcli.entities.items.Item;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Inventory Manager Tests")
public class InventoryManagerTest {
    private InventoryManager inventoryManager;

    @BeforeEach
    void setUp() {
        inventoryManager = new InventoryManager(5);
    }

    @Test
    @DisplayName("Inventory initializes empty with correct capacity")
    void testInitialization() {
        assertEquals(0, inventoryManager.getItems().size());
        assertEquals(5, inventoryManager.getCapacity());
    }

    @Test
    @DisplayName("Adding items increases inventory size")
    void testAddItem() {
        Item food = new Food("Apple", 10, 20, 0);
        inventoryManager.addItem(food);
        assertEquals(1, inventoryManager.getItems().size());
        assertEquals(food, inventoryManager.getItems().get(0));
    }

    @Test
    @DisplayName("Can set and retrieve the capacity")
    void testSetCapacity() {
        inventoryManager.setCapacity(10);
        assertEquals(10, inventoryManager.getCapacity());
        inventoryManager.setCapacity(3);
        assertEquals(3, inventoryManager.getCapacity());
    }
}
