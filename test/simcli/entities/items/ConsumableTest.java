package simcli.entities.items;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.entities.actors.Sim;
import simcli.entities.actors.Gender;
import simcli.engine.TimeManager;
import simcli.engine.SimulationException;
import java.util.Scanner;
import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Consumable & Food Tests")
public class ConsumableTest {
    private Sim testSim;
    private TimeManager timeManager;
    private Scanner dummyScanner;

    @BeforeEach
    void setUp() {
        testSim = new Sim("TestSim", 25, Gender.MALE);
        timeManager = new TimeManager(1, 12);
        dummyScanner = new Scanner(new ByteArrayInputStream("".getBytes()));
    }

    @Test
    @DisplayName("Consumable restores correct needs and is removed from inventory")
    void testConsumableInteraction() throws SimulationException {
        // Decrease stats so there's room to restore
        testSim.getHunger().decrease(50);
        testSim.getEnergy().decrease(50);
        testSim.getFun().decrease(50);

        int initialHunger = testSim.getHunger().getValue();
        int initialEnergy = testSim.getEnergy().getValue();
        int initialFun = testSim.getFun().getValue();

        Consumable coffee = new Consumable("Coffee", 5, 10, 30, 5);
        testSim.addItem(coffee);
        assertEquals(1, testSim.getInventory().size(), "Coffee should be in inventory");

        coffee.interact(testSim, dummyScanner, timeManager);

        assertEquals(initialHunger + 10, testSim.getHunger().getValue());
        assertEquals(initialEnergy + 30, testSim.getEnergy().getValue());
        assertEquals(initialFun + 5, testSim.getFun().getValue());
        
        assertEquals(0, testSim.getInventory().size(), "Coffee should be removed after consumption");
    }

    @Test
    @DisplayName("Food restores only hunger and energy, not fun")
    void testFoodInteraction() throws SimulationException {
        testSim.getHunger().decrease(40);
        testSim.getEnergy().decrease(40);
        testSim.getFun().decrease(40);

        int initialHunger = testSim.getHunger().getValue();
        int initialEnergy = testSim.getEnergy().getValue();
        int initialFun = testSim.getFun().getValue();

        Food apple = new Food("Apple", 2, 20, 5);
        testSim.addItem(apple);

        apple.interact(testSim, dummyScanner, timeManager);

        assertEquals(initialHunger + 20, testSim.getHunger().getValue());
        assertEquals(initialEnergy + 5, testSim.getEnergy().getValue());
        assertEquals(initialFun, testSim.getFun().getValue(), "Food should not increase fun");
    }
}
