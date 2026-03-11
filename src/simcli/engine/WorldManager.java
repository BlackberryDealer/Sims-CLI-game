package simcli.engine;

import simcli.world.Building;
import simcli.world.Commercial;
import simcli.world.Park;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.*;

import java.util.ArrayList;
import java.util.List;

public class WorldManager implements IWorldManager {
    private List<Building> cityMap;
    private Building currentLocation;

    public WorldManager() {
        this.cityMap = new ArrayList<>();
    }

    @Override
    public void setupWorld() {
        this.cityMap.clear();

        // Build Home
        Residential home = new Residential("The Shared Dorm");

        Room mainRoom = new Room("Bedroom", 100);
        mainRoom.placeFurniture(new Bed(), 30);
        home.addRoom(mainRoom);

        Room kitchen = new Room("Kitchen", 50);
        home.addRoom(kitchen);

        Room gym = new Room("Garage", 60);
        home.addRoom(gym);

        Room bathroom = new Room("Bathroom", 30);
        home.addRoom(bathroom);

        this.cityMap.add(home);

        // Build Supermarket
        Commercial store = new Commercial("Town Supermarket");
        store.addInteractable(new GroceryShelf());
        this.cityMap.add(store);

        Park park = new Park("City Park");
        this.cityMap.add(park);

        // Default spawn location
        this.currentLocation = home;
    }

    @Override
    public Building getCurrentLocation() {
        return this.currentLocation;
    }

    @Override
    public void setCurrentLocation(Building b) {
        this.currentLocation = b;
    }

    @Override
    public List<Building> getCityMap() {
        return this.cityMap;
    }
}
