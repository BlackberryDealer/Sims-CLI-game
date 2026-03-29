package simcli.entities.items;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link Food} class — verifying that Food is a
 * specialized Consumable with happinessValue fixed at zero.
 */
@DisplayName("Food — Subclass Behavior Tests")
public class FoodTest {

    @Test
    @DisplayName("Food always has happinessValue of 0")
    void testFoodHasZeroHappiness() {
        Food food = new Food("Steak", 40, 50, 20);
        assertEquals(0, food.getHappinessValue(),
                "Food should always have happinessValue = 0 (basic nutrition only)");
    }

    @Test
    @DisplayName("Food constructor sets satiation and energy correctly")
    void testFoodNutritionalValues() {
        Food food = new Food("Rice", 10, 30, 10);
        assertEquals("Rice", food.getObjectName());
        assertEquals(10, food.getPrice());
        assertEquals(30, food.getSatiationValue());
        assertEquals(10, food.getEnergyValue());
    }

    @Test
    @DisplayName("copyItem() returns a distinct Food with same values")
    void testCopyItem() {
        Food original = new Food("Apple", 5, 15, 5);
        Item copy = original.copyItem();

        assertNotSame(original, copy, "Copy should be a different object");
        assertTrue(copy instanceof Food, "Copy should be a Food instance");
        assertEquals(original.getObjectName(), copy.getObjectName());
        assertEquals(original.getPrice(), copy.getPrice());
    }

    @Test
    @DisplayName("toSaveString() produces correct CSV format")
    void testToSaveString() {
        Food food = new Food("Burger", 15, 40, 10);
        String save = food.toSaveString();
        assertTrue(save.startsWith("Food,"), "Save string should start with 'Food,'");
        assertTrue(save.contains("Burger"), "Save string should contain food name");
    }
}
