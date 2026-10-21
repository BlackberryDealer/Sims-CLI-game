package simcli.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.entities.actors.Sim;
import simcli.entities.items.Food;
import simcli.entities.items.Furniture;
import simcli.entities.items.Ingredient;
import simcli.entities.items.Item;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.utils.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the JSON reader and the content loaded from
 * {@code resources/simcli/data/}.
 */
@DisplayName("Game Content — JSON Data Files")
public class GameContentTest {

    private static List<String> names(List<Item> items) {
        List<String> names = new ArrayList<>();
        for (Item item : items) {
            names.add(item.getObjectName());
        }
        return names;
    }

    @Test
    @DisplayName("The JSON reader handles objects, arrays, strings, numbers and literals")
    void testJsonParse() {
        Object parsed = Json.parse("{ \"a\": [1, 2.5, -3], \"b\": \"x\\\"y\\u0041\", \"c\": true, \"d\": null }");
        Map<String, Object> map = GameContent.map(parsed, "test");
        assertEquals(List.of(1.0, 2.5, -3.0), map.get("a"));
        assertEquals("x\"yA", map.get("b"));
        assertEquals(Boolean.TRUE, map.get("c"));
        assertTrue(map.containsKey("d"));
        assertNull(map.get("d"));
    }

    @Test
    @DisplayName("Bad JSON reports where the mistake is")
    void testJsonErrors() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Json.parse("{\n  \"a\": 1,\n  \"b\" 2\n}"));
        assertTrue(e.getMessage().contains("line 3"), e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> Json.parse("[1, 2"));
        assertThrows(IllegalArgumentException.class, () -> Json.parse("{} extra"));
    }

    @Test
    @DisplayName("Items are built from their JSON descriptions")
    void testItemTypes() {
        Item steak = GameContent.item(GameContent.map(Json.parse(
                "{\"type\":\"ingredient\",\"name\":\"Raw Steak\",\"price\":35,\"hunger\":15,"
                        + "\"meal\":\"Steak Dinner\",\"mealHunger\":80}"), "item"));
        assertTrue(steak instanceof Ingredient);
        assertEquals(80, ((Ingredient) steak).getMealSatiation());

        Item bed = GameContent.item(GameContent.map(Json.parse(
                "{\"type\":\"furniture\",\"name\":\"Bed\",\"price\":200,\"space\":30}"), "item"));
        assertTrue(bed instanceof Furniture);
        assertEquals(30, ((Furniture) bed).getSpaceScore());

        Item apple = GameContent.item(GameContent.map(Json.parse(
                "{\"type\":\"food\",\"name\":\"Apple\",\"price\":10,\"hunger\":15,\"energy\":5,\"freshness\":2}"),
                "item"));
        assertEquals(2, ((Food) apple).getFreshness());
    }

    @Test
    @DisplayName("Shop items with conditions only appear for shoppers who meet one")
    void testCatalogConditions() {
        Sim young = new Sim("Yan", 18, Gender.MALE, Job.UNEMPLOYED);
        young.setMoney(0);
        List<String> forYoung = names(GameContent.shopCatalog("grocery", young));
        assertTrue(forYoung.contains("Apple"));
        assertTrue(forYoung.contains("Groceries"));
        assertFalse(forYoung.contains("Fridge"), "Fridge needs $500 or age 25");
        assertFalse(forYoung.contains("Weight Bench"), "Weight Bench needs age 22");
        assertFalse(forYoung.contains("Computer"));

        Sim engineer = new Sim("Eng", 30, Gender.FEMALE, Job.SOFTWARE_ENGINEER);
        engineer.setMoney(0);
        List<String> forEngineer = names(GameContent.shopCatalog("grocery", engineer));
        assertTrue(forEngineer.contains("Fridge"));
        assertTrue(forEngineer.contains("Weight Bench"));
        assertTrue(forEngineer.contains("Computer"), "Software engineers can buy a computer here");
    }

    @Test
    @DisplayName("Each call returns new item instances")
    void testCatalogItemsAreFresh() {
        Sim sim = new Sim("Sam", 30, Gender.MALE, Job.UNEMPLOYED);
        assertNotSame(GameContent.shopCatalog("bookshop", sim).get(0), GameContent.shopCatalog("bookshop", sim).get(0));
        assertEquals("Bookshop Register", GameContent.registerName("bookshop"));
    }

    @Test
    @DisplayName("Unknown shops are a clear error")
    void testUnknownShop() {
        Sim sim = new Sim("Sam", 30, Gender.MALE, Job.UNEMPLOYED);
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> GameContent.shopCatalog("nope", sim));
        assertTrue(e.getMessage().contains("nope"));
    }

    @Test
    @DisplayName("The city layout loads in travel-menu order")
    void testBuildings() {
        List<Map<String, Object>> buildings = GameContent.buildings();
        assertEquals(5, buildings.size());
        assertEquals("The Shared Dorm", GameContent.string(buildings.get(0), "name"));
        assertEquals("home", GameContent.string(buildings.get(0), "type"));
        assertEquals(100, GameContent.integer(buildings.get(0), "weeklyBill", 0));
        assertEquals(5000, GameContent.integer(buildings.get(1), "price", 0));
        assertEquals("park", GameContent.string(buildings.get(4), "type"));
    }
}
