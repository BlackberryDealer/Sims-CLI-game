package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.entities.managers.NPCManager;

import java.util.List;

/**
 * Contract for the game engine, decoupling consumers (commands, interactables,
 * persistence) from the concrete {@link GameEngine} implementation.
 * 
 * <p>Follows the <b>Dependency Inversion Principle</b>: high-level modules
 * (commands, ParkBench, SaveManager) depend on this abstraction rather than
 * the concrete GameEngine class.</p>
 */
public interface IGameEngine {

    /** Returns the Sim currently being controlled by the player. */
    Sim getActivePlayer();

    /** Switches player control to a different Sim. */
    void setActivePlayer(Sim sim);

    /** Returns the world manager responsible for buildings and locations. */
    IWorldManager getWorldManager();

    /** Returns the NPC manager for park visitors and social interactions. */
    NPCManager getNpcManager();

    /** Returns the list of all Sims in the player's household. */
    List<Sim> getNeighborhood();

    /** Returns the name of the current game world. */
    String getWorldName();

    /** Returns the current simulation tick count. */
    int getCurrentTick();

    /** Returns whether the game has ended (all Sims dead). */
    boolean isGameOver();

    /** Returns total money earned across all Sims this session. */
    int getSessionTotalMoney();

    /** Returns total items bought across all Sims this session. */
    int getSessionTotalItems();
}
