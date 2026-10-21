package simcli.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.entities.actors.Sim;
import simcli.entities.items.Food;
import simcli.entities.items.Ingredient;
import simcli.entities.managers.NPCManager;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.persistence.Savable;
import simcli.utils.GameConstants;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.Fridge;
import simcli.world.interactables.Interactable;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for money pressure: weekly bills, food going off, and cooking.
 */
@DisplayName("Upkeep — Bills, Spoilage and Cooking")
public class UpkeepManagerTest {

    /** Day 8 is the second Monday; day 9 a Tuesday. */
    private static final int MONDAY = 8;
    private static final int TUESDAY = 9;

    private Sim rich;
    private Sim poor;
    private List<Sim> household;
    private WorldManager world;
    private UpkeepManager upkeep;

    @BeforeEach
    void setUp() {
        SimulationLogger.setInstance(new SimulationLogger());
        rich = new Sim("Rich", 30, Gender.FEMALE, Job.UNEMPLOYED);
        poor = new Sim("Poor", 30, Gender.MALE, Job.UNEMPLOYED);
        household = new ArrayList<>(List.of(rich, poor));
        world = new WorldManager(new NPCManager(), household);
        world.setupWorld();
        upkeep = new UpkeepManager(world, new SimulationLogger());
    }

    private Residential home(int index) {
        int seen = 0;
        for (Building b : world.getCityMap()) {
            if (b instanceof Residential && seen++ == index) {
                return (Residential) b;
            }
        }
        throw new AssertionError("no residential #" + index);
    }

    private Fridge dormFridge() {
        for (Room room : home(0).getRooms()) {
            for (Interactable it : room.getInteractables()) {
                if (it instanceof Fridge) {
                    return (Fridge) it;
                }
            }
        }
        throw new AssertionError("The dorm should have a fridge");
    }

    @Test
    @DisplayName("Bills are charged on Mondays, from the richest Sim first")
    void testBillsOnMonday() {
        rich.setMoney(500);
        poor.setMoney(50);

        upkeep.processDay(TUESDAY, household);
        assertEquals(500, rich.getMoney(), "No bills on a Tuesday");

        upkeep.processDay(MONDAY, household);
        assertEquals(500 - GameConstants.DORM_WEEKLY_BILL, rich.getMoney());
        assertEquals(50, poor.getMoney());
    }

    @Test
    @DisplayName("Owning the Bungalow adds its bill")
    void testBungalowBill() {
        home(1).setOwned(true);
        assertEquals(GameConstants.DORM_WEEKLY_BILL + GameConstants.BUNGALOW_WEEKLY_BILL, upkeep.getWeeklyBills());
    }

    @Test
    @DisplayName("Bills split across Sims, and unpaid bills make everyone unhappy")
    void testUnpaidBills() {
        rich.setMoney(60);
        poor.setMoney(30);
        rich.getHappiness().setValue(80);
        poor.getHappiness().setValue(80);

        upkeep.processDay(MONDAY, household);

        assertEquals(0, rich.getMoney());
        assertEquals(0, poor.getMoney());
        assertEquals(80 - GameConstants.UNPAID_BILLS_HAPPINESS_PENALTY, rich.getHappiness().getValue());
        assertEquals(80 - GameConstants.UNPAID_BILLS_HAPPINESS_PENALTY, poor.getHappiness().getValue());
    }

    @Test
    @DisplayName("Paid bills don't cost any happiness")
    void testPaidBillsNoPenalty() {
        rich.setMoney(1000);
        rich.getHappiness().setValue(80);
        upkeep.processDay(MONDAY, household);
        assertEquals(80, rich.getHappiness().getValue());
    }

    @Test
    @DisplayName("Food keeps twice as long in the fridge as in a bag")
    void testSpoilageRates() {
        Food inFridge = new Food("Apple", 10, 15, 5, 4);
        Food inBag = new Food("Apple", 10, 15, 5, 4);
        dormFridge().storeItem(inFridge);
        rich.addItem(inBag);

        upkeep.processDay(TUESDAY, household);

        assertEquals(3, inFridge.getFreshness());
        assertEquals(2, inBag.getFreshness());

        upkeep.processDay(TUESDAY + 1, household);
        assertFalse(rich.getInventory().contains(inBag), "Bag food should have gone off and been thrown out");
        assertTrue(dormFridge().getStoredItems().contains(inFridge), "Fridge food should still be good");
    }

    @Test
    @DisplayName("Books and other non-food items never go off")
    void testNonFoodDoesNotSpoil() {
        rich.addItem(new simcli.entities.items.Consumable("Novel", 15, 0, 0, 25));
        for (int day = TUESDAY; day < TUESDAY + 20; day++) {
            upkeep.processDay(day, household);
        }
        assertEquals(1, rich.getInventory().size());
    }

    @Test
    @DisplayName("Cooking an ingredient makes a far more filling meal")
    void testCookIngredient() {
        Ingredient groceries = new Ingredient("Groceries", 15, 10, "Home-cooked Meal", 55, 6);
        Food meal = groceries.cook();
        assertEquals("Home-cooked Meal", meal.getObjectName());
        assertEquals(55, meal.getSatiationValue());
        assertTrue(meal.getSatiationValue() > groceries.getSatiationValue());
        assertEquals(GameConstants.MEAL_SHELF_LIFE_DAYS, meal.getFreshness());
    }

    @Test
    @DisplayName("Cooking at the fridge uses the ingredient and feeds the Sim")
    void testCookAtFridge() throws Exception {
        Fridge fridge = dormFridge();
        fridge.storeItem(new Ingredient("Groceries", 15, 10, "Home-cooked Meal", 55, 6));
        rich.getHunger().setValue(20);
        rich.getHappiness().setValue(50);

        fridge.interact(rich, new Scanner(new ByteArrayInputStream("C\n1\n\n0\n".getBytes())), new TimeManager(12, 24));

        assertTrue(fridge.getStoredItems().isEmpty(), "The ingredient should be used up");
        assertEquals(75, rich.getHunger().getValue());
        assertEquals(50 + GameConstants.COOKING_HAPPINESS, rich.getHappiness().getValue());
    }

    @Test
    @DisplayName("Idle Sims cook ingredients instead of eating them raw")
    void testAutonomyCooks() {
        Sim active = rich;
        dormFridge().storeItem(new Ingredient("Groceries", 15, 10, "Home-cooked Meal", 55, 6));
        poor.getHunger().setValue(20);

        new GameLoop(new TimeManager(12, 24), household, new SimulationLogger(), world).processTick(active, 1);

        assertTrue(poor.getHunger().getValue() >= 70, "Should have eaten the cooked meal, not raw groceries");
    }

    @Test
    @DisplayName("Freshness and ingredients survive the save format; old food lines still load")
    void testSaveStrings() {
        Food apple = new Food("Apple", 10, 15, 5, 2);
        Food loadedApple = (Food) Savable.fromSaveString(apple.toSaveString());
        assertEquals(2, loadedApple.getFreshness());

        Ingredient steak = new Ingredient("Raw Steak", 35, 15, "Steak Dinner", 80, 4);
        Ingredient loadedSteak = (Ingredient) Savable.fromSaveString(steak.toSaveString());
        assertEquals("Steak Dinner", loadedSteak.getMealName());
        assertEquals(80, loadedSteak.getMealSatiation());
        assertEquals(4, loadedSteak.getFreshness());

        Food old = (Food) Savable.fromSaveString("Food,Apple,10,15,5");
        assertEquals(GameConstants.FOOD_SHELF_LIFE_DAYS, old.getFreshness());
    }
}
