package simcli.engine;

import simcli.entities.managers.NPCManager;
import simcli.world.Building;
import simcli.world.Commercial;
import simcli.world.Park;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of {@link IWorldManager}.
 *
 * <p>Builds the city map from hardcoded building definitions (dorm, bungalow,
 * supermarket, bookshop, park) and tracks which building the player is
 * currently inside. The world is rebuilt from scratch each time
 * {@link #setupWorld()} is called (e.g. on new game or load).</p>
 *
 * <p>Receives its {@link NPCManager} dependency via the constructor rather
 * than reaching back into {@code GameEngine}, eliminating the previous
 * circular dependency. The neighborhood list is also injected so that
 * {@code ParkBench} can add married NPCs to the household without
 * referencing the engine.</p>
 */
public class WorldManager implements IWorldManager {
    private List<Building> cityMap;
    private Building currentLocation;
    private final NPCManager npcManager;
    private final List<simcli.entities.actors.Sim> neighborhood;

    /**
     * Creates a {@code WorldManager} with the given NPC manager.
     * Call {@link #setupWorld()} to build the map.
     *
     * @param npcManager   provides NPCs for park buildings.
     * @param neighborhood the mutable household list (for ParkBench marriage).
     */
    public WorldManager(NPCManager npcManager, List<simcli.entities.actors.Sim> neighborhood) {
        this.cityMap = new ArrayList<>();
        this.npcManager = npcManager;
        this.neighborhood = neighborhood;
    }

    /**
     * Constructs all buildings, rooms, furniture, and NPCs for the game world.
     * Clears any previous map before rebuilding.
     */
    @Override
    public void setupWorld() {
        this.cityMap.clear();

        // Build Home
        Residential home = new Residential("The Shared Dorm");

        Room mainRoom = new Room("Bedroom", 100);
        mainRoom.placeFurniture(new Bed(), 30);
        home.addRoom(mainRoom);

        Room kitchen = new Room("Kitchen", 50);
        kitchen.placeFurniture(new Fridge(), 25);
        home.addRoom(kitchen);

        Room gym = new Room("Garage", 60);
        gym.placeFurniture(new WeightBench(), 40);
        home.addRoom(gym);

        Room bathroom = new Room("Bathroom", 30);
        bathroom.placeFurniture(new Shower(), 10);
        home.addRoom(bathroom);

        this.cityMap.add(home);

        // Build the Bungalow — purchasable for $5000 (proposal Slide 7)
        Residential bungalow = new Residential("The Bungalow", 5000);

        Room livingRoom = new Room("Living Room", 120);
        livingRoom.placeFurniture(new Bed(), 30);
        livingRoom.placeFurniture(new Fridge(), 25);
        bungalow.addRoom(livingRoom);

        Room study = new Room("Study", 80);
        study.placeFurniture(new Computer(), 20);
        bungalow.addRoom(study);

        Room bungalowBath = new Room("Ensuite Bathroom", 40);
        bungalowBath.placeFurniture(new Shower(), 10);
        bungalow.addRoom(bungalowBath);

        this.cityMap.add(bungalow);

        // Build Supermarket
        Commercial store = new Commercial("Town Supermarket");
        store.addInteractable(new GroceryShelf());
        this.cityMap.add(store);

        // Build Bookshop (proposal Slide 7 — different interactables from Supermarket)
        Commercial bookshop = new Commercial("The Bookshop");
        bookshop.addInteractable(new BookshopShelf());
        this.cityMap.add(bookshop);

        // Build Park — ParkBench now receives NPCManager + neighborhood directly
        Park park = new Park("City Park", this.npcManager);
        park.addInteractable(new ParkBench(this.npcManager, this.neighborhood));
        this.cityMap.add(park);

        // Default spawn location
        this.currentLocation = home;
    }

    /** {@inheritDoc} */
    @Override
    public Building getCurrentLocation() {
        return this.currentLocation;
    }

    /** {@inheritDoc} */
    @Override
    public void setCurrentLocation(Building b) {
        this.currentLocation = b;
    }

    /** {@inheritDoc} */
    @Override
    public List<Building> getCityMap() {
        return this.cityMap;
    }
}
