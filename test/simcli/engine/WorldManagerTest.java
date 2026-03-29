package simcli.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import simcli.entities.actors.Sim;
import simcli.entities.managers.NPCManager;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.world.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link WorldManager} — city map construction, location tracking,
 * and building configuration.
 *
 * <p>Coverage:
 * <ul>
 *     <li>setupWorld() creates exactly 5 buildings</li>
 *     <li>Building types match expected order (Residential, Residential, Commercial, Commercial, Park)</li>
 *     <li>Default location is the first residential building</li>
 *     <li>setCurrentLocation() changes the active building</li>
 *     <li>Residential buildings have expected room counts</li>
 *     <li>Buildings contain expected interactables</li>
 *     <li>setupWorld() is idempotent (calling it twice resets cleanly)</li>
 * </ul>
 */
@DisplayName("WorldManager — City Map Construction & Location Tracking")
public class WorldManagerTest {

    private WorldManager worldManager;
    private NPCManager npcManager;
    private List<Sim> neighborhood;

    @BeforeEach
    void setUp() {
        SimulationLogger.setInstance(new SimulationLogger());
        npcManager = new NPCManager();
        Sim sim = new Sim("TestSim", 25, Gender.MALE, Job.UNEMPLOYED);
        neighborhood = new ArrayList<>();
        neighborhood.add(sim);
        worldManager = new WorldManager(npcManager, neighborhood);
        worldManager.setupWorld();
    }

    // -----------------------------------------------------------------------
    // City map structure
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("City Map Structure")
    class CityMapTests {

        @Test
        @DisplayName("setupWorld() creates exactly 5 buildings")
        void testCityMapSize() {
            assertEquals(5, worldManager.getCityMap().size(),
                    "City map should contain exactly 5 buildings");
        }

        @Test
        @DisplayName("Building [0] is a Residential (The Shared Dorm)")
        void testFirstBuildingIsResidential() {
            Building first = worldManager.getCityMap().get(0);
            assertTrue(first instanceof Residential,
                    "First building should be Residential");
            assertEquals("The Shared Dorm", first.getName());
        }

        @Test
        @DisplayName("Building [1] is a Residential (The Bungalow)")
        void testSecondBuildingIsResidential() {
            Building second = worldManager.getCityMap().get(1);
            assertTrue(second instanceof Residential,
                    "Second building should be Residential");
            assertEquals("The Bungalow", second.getName());
        }

        @Test
        @DisplayName("Building [2] is a Commercial (Town Supermarket)")
        void testThirdBuildingIsCommercial() {
            Building third = worldManager.getCityMap().get(2);
            assertTrue(third instanceof Commercial,
                    "Third building should be Commercial");
            assertEquals("Town Supermarket", third.getName());
        }

        @Test
        @DisplayName("Building [3] is a Commercial (The Bookshop)")
        void testFourthBuildingIsCommercial() {
            Building fourth = worldManager.getCityMap().get(3);
            assertTrue(fourth instanceof Commercial,
                    "Fourth building should be Commercial");
            assertEquals("The Bookshop", fourth.getName());
        }

        @Test
        @DisplayName("Building [4] is a Park (City Park)")
        void testFifthBuildingIsPark() {
            Building fifth = worldManager.getCityMap().get(4);
            assertTrue(fifth instanceof Park,
                    "Fifth building should be Park");
            assertEquals("City Park", fifth.getName());
        }
    }

    // -----------------------------------------------------------------------
    // Default location
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Default Location")
    class DefaultLocationTests {

        @Test
        @DisplayName("Default location is the first building (The Shared Dorm)")
        void testDefaultLocationIsDorm() {
            Building defaultLocation = worldManager.getCurrentLocation();
            assertEquals("The Shared Dorm", defaultLocation.getName(),
                    "Default spawn location should be 'The Shared Dorm'");
        }

        @Test
        @DisplayName("Default location is a Residential building")
        void testDefaultLocationIsResidential() {
            assertTrue(worldManager.getCurrentLocation() instanceof Residential,
                    "Default location should be Residential");
        }
    }

    // -----------------------------------------------------------------------
    // Location changing
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Location Navigation")
    class LocationNavigationTests {

        @Test
        @DisplayName("setCurrentLocation() changes the active building")
        void testSetCurrentLocation() {
            Building park = worldManager.getCityMap().get(4);
            worldManager.setCurrentLocation(park);

            assertSame(park, worldManager.getCurrentLocation(),
                    "getCurrentLocation() should return the park after setCurrentLocation()");
        }

        @Test
        @DisplayName("setCurrentLocation() accepts Commercial buildings")
        void testSetLocationToCommercial() {
            Building store = worldManager.getCityMap().get(2);
            worldManager.setCurrentLocation(store);

            assertEquals("Town Supermarket", worldManager.getCurrentLocation().getName());
        }
    }

    // -----------------------------------------------------------------------
    // Room structure
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Residential Room Structure")
    class RoomStructureTests {

        @Test
        @DisplayName("The Shared Dorm has 4 rooms")
        void testDormHasFourRooms() {
            Residential dorm = (Residential) worldManager.getCityMap().get(0);
            assertEquals(4, dorm.getRooms().size(),
                    "The Shared Dorm should have 4 rooms");
        }

        @Test
        @DisplayName("The Shared Dorm rooms are Bedroom, Kitchen, Garage, Bathroom")
        void testDormRoomNames() {
            Residential dorm = (Residential) worldManager.getCityMap().get(0);
            List<Room> rooms = dorm.getRooms();

            assertEquals("Bedroom", rooms.get(0).getName());
            assertEquals("Kitchen", rooms.get(1).getName());
            assertEquals("Garage", rooms.get(2).getName());
            assertEquals("Bathroom", rooms.get(3).getName());
        }

        @Test
        @DisplayName("The Bungalow has 3 rooms")
        void testBungalowHasThreeRooms() {
            Residential bungalow = (Residential) worldManager.getCityMap().get(1);
            assertEquals(3, bungalow.getRooms().size(),
                    "The Bungalow should have 3 rooms");
        }

        @Test
        @DisplayName("The Bungalow rooms are Living Room, Study, Ensuite Bathroom")
        void testBungalowRoomNames() {
            Residential bungalow = (Residential) worldManager.getCityMap().get(1);
            List<Room> rooms = bungalow.getRooms();

            assertEquals("Living Room", rooms.get(0).getName());
            assertEquals("Study", rooms.get(1).getName());
            assertEquals("Ensuite Bathroom", rooms.get(2).getName());
        }
    }

    // -----------------------------------------------------------------------
    // Property ownership
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Property Ownership")
    class PropertyTests {

        @Test
        @DisplayName("The Shared Dorm is owned by default")
        void testDormIsOwned() {
            Residential dorm = (Residential) worldManager.getCityMap().get(0);
            assertTrue(dorm.isOwned(), "The Shared Dorm should be owned by default");
        }

        @Test
        @DisplayName("The Bungalow is NOT owned by default")
        void testBungalowIsNotOwned() {
            Residential bungalow = (Residential) worldManager.getCityMap().get(1);
            assertFalse(bungalow.isOwned(), "The Bungalow should NOT be owned by default");
        }

        @Test
        @DisplayName("The Bungalow costs $5000")
        void testBungalowPrice() {
            Residential bungalow = (Residential) worldManager.getCityMap().get(1);
            assertEquals(5000, bungalow.getPurchasePrice(),
                    "The Bungalow should cost $5000");
        }
    }

    // -----------------------------------------------------------------------
    // Interactables
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Each room in dorm has at least 1 interactable")
    void testDormRoomsHaveInteractables() {
        Residential dorm = (Residential) worldManager.getCityMap().get(0);
        for (Room room : dorm.getRooms()) {
            assertFalse(room.getInteractables().isEmpty(),
                    "Room '" + room.getName() + "' should have at least 1 interactable");
        }
    }

    @Test
    @DisplayName("Commercial buildings have at least 1 interactable")
    void testCommercialBuildingsHaveInteractables() {
        for (int i = 2; i <= 3; i++) {
            Building b = worldManager.getCityMap().get(i);
            assertFalse(b.getInteractables().isEmpty(),
                    b.getName() + " should have at least 1 interactable");
        }
    }

    // -----------------------------------------------------------------------
    // Idempotency
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Calling setupWorld() twice resets cleanly (no duplicates)")
    void testSetupWorldIdempotency() {
        worldManager.setupWorld();

        assertEquals(5, worldManager.getCityMap().size(),
                "Calling setupWorld() twice should still yield exactly 5 buildings");
    }
}
