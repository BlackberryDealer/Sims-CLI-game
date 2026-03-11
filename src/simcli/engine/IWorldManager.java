package simcli.engine;

import simcli.world.Building;

import java.util.List;

/**
 * Contract for the class responsible for managing world layout and
 * the player's current location. GameEngine depends on this interface,
 * not on a concrete WorldManager, keeping coupling low.
 */
public interface IWorldManager {

    /** Constructs all buildings, rooms, and furniture for the game world. */
    void setupWorld();

    /** Returns the building the player is currently inside. */
    Building getCurrentLocation();

    /** Moves the player to a new building. */
    void setCurrentLocation(Building b);

    /** Returns the full list of buildings in the city. */
    List<Building> getCityMap();
}
