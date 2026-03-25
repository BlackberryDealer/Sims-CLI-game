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
    private GameEngine engine;

    public WorldManager() {
        this.cityMap = new ArrayList<>();
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
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

        Park park = new Park("City Park", this.engine.getNpcManager());
        park.addInteractable(new simcli.world.interactables.ParkBench(this.engine));
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
