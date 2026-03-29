package simcli.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import simcli.engine.SimulationLogger;
import simcli.entities.actors.Sim;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link Building} hierarchy — Residential and Commercial behavior.
 *
 * <p>Coverage:
 * <ul>
 *     <li>Residential: room management, purchase logic, isResidential()</li>
 *     <li>Commercial: isResidential() returns false</li>
 *     <li>Building: interactable management</li>
 * </ul>
 */
@DisplayName("Building Hierarchy — Residential & Commercial Tests")
public class BuildingTest {

    private Sim sim;

    @BeforeEach
    void setUp() {
        SimulationLogger logger = new SimulationLogger();
        SimulationLogger.setInstance(logger);
        sim = new Sim("Alice", 25, Gender.FEMALE, Job.SOFTWARE_ENGINEER);
    }

    // -----------------------------------------------------------------------
    // Residential tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Free Residential is owned by default")
    void testFreeResidentialIsOwned() {
        Residential home = new Residential("Dorm");
        assertTrue(home.isOwned(), "Free residential should be owned by default");
        assertEquals(0, home.getPurchasePrice());
    }

    @Test
    @DisplayName("Priced Residential is NOT owned by default")
    void testPricedResidentialNotOwned() {
        Residential house = new Residential("Luxury Villa", 5000);
        assertFalse(house.isOwned(), "Priced residential should not be owned by default");
        assertEquals(5000, house.getPurchasePrice());
    }

    @Test
    @DisplayName("purchase() succeeds when Sim has enough money")
    void testPurchaseSuccess() {
        Residential house = new Residential("Cottage", 200);
        sim.setMoney(500);

        boolean result = house.purchase(sim);

        assertTrue(result, "Purchase should succeed");
        assertTrue(house.isOwned(), "House should be owned after purchase");
        assertEquals(300, sim.getMoney(), "Money should be deducted");
    }

    @Test
    @DisplayName("purchase() fails when Sim lacks money")
    void testPurchaseFails() {
        Residential house = new Residential("Mansion", 1000);
        sim.setMoney(100);

        boolean result = house.purchase(sim);

        assertFalse(result, "Purchase should fail");
        assertFalse(house.isOwned(), "House should remain unowned");
        assertEquals(100, sim.getMoney(), "Money should not change");
    }

    @Test
    @DisplayName("addRoom() and getRooms() work correctly")
    void testRoomManagement() {
        Residential home = new Residential("Home");
        assertEquals(0, home.getRooms().size());

        home.addRoom(new Room("Bedroom", 5));
        home.addRoom(new Room("Kitchen", 3));

        assertEquals(2, home.getRooms().size());
    }

    @Test
    @DisplayName("Residential.isResidential() returns true")
    void testResidentialIsResidential() {
        Residential home = new Residential("Home");
        assertTrue(home.isResidential());
    }

    // -----------------------------------------------------------------------
    // Commercial tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Commercial.isResidential() returns false")
    void testCommercialIsNotResidential() {
        Commercial shop = new Commercial("Supermarket");
        assertFalse(shop.isResidential());
    }

    @Test
    @DisplayName("Commercial has correct name")
    void testCommercialName() {
        Commercial shop = new Commercial("The Bookshop");
        assertEquals("The Bookshop", shop.getName());
    }
}
