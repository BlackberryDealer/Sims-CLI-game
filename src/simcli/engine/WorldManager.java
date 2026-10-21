package simcli.engine;

import simcli.entities.managers.NPCManager;
import simcli.persistence.GameContent;
import simcli.world.Building;
import simcli.world.Commercial;
import simcli.world.Park;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.CatalogShop;
import simcli.world.interactables.Interactable;
import simcli.world.interactables.InteractableFactory;
import simcli.world.interactables.ParkBench;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Concrete implementation of {@link IWorldManager}.
 *
 * <p>Builds the city map from {@code buildings.json} (homes with their rooms
 * and furniture, shops, the park — see {@link GameContent}) and tracks which
 * building the player is currently inside. The world is rebuilt from
 * scratch each time {@link #setupWorld()} is called (e.g. on new game or
 * load).</p>
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
        this.currentLocation = null;

        for (Map<String, Object> json : GameContent.buildings()) {
            Building building = build(json);
            this.cityMap.add(building);
            // a new household starts in the first home
            if (this.currentLocation == null && building instanceof Residential) {
                this.currentLocation = building;
            }
        }
        if (this.currentLocation == null) {
            throw new IllegalStateException("buildings.json needs at least one home");
        }
    }

    /** Builds one building from its entry in {@code buildings.json}. */
    private Building build(Map<String, Object> json) {
        String name = GameContent.string(json, "name");
        String type = GameContent.string(json, "type");
        switch (type) {
            case "home": {
                int price = GameContent.integer(json, "price", 0);
                // a home with no price is owned from the start
                Residential home = price > 0 ? new Residential(name, price) : new Residential(name);
                home.setWeeklyBill(GameContent.integer(json, "weeklyBill", 0));
                for (Object roomEntry : GameContent.list(json, "rooms")) {
                    Map<String, Object> roomJson = GameContent.map(roomEntry, "room");
                    Room room = new Room(GameContent.string(roomJson, "name"),
                            GameContent.integer(roomJson, "capacity", 0));
                    for (Object furnitureEntry : GameContent.list(roomJson, "furniture")) {
                        Map<String, Object> furniture = GameContent.map(furnitureEntry, "furniture");
                        String furnitureName = GameContent.string(furniture, "name");
                        Interactable object = InteractableFactory.create(furnitureName);
                        if (object == null) {
                            throw new IllegalStateException("buildings.json: the game can't build a '"
                                    + furnitureName + "' (in " + name + ")");
                        }
                        room.placeFurniture(object, GameContent.integer(furniture, "space", 0));
                    }
                    home.addRoom(room);
                }
                return home;
            }
            case "shop": {
                Commercial shop = new Commercial(name);
                shop.addInteractable(new CatalogShop(GameContent.string(json, "shop")));
                return shop;
            }
            case "park": {
                // ParkBench receives NPCManager + neighborhood directly
                Park park = new Park(name, this.npcManager);
                park.addInteractable(new ParkBench(this.npcManager, this.neighborhood));
                return park;
            }
            default:
                throw new IllegalStateException("buildings.json: unknown building type '" + type + "' for " + name);
        }
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
