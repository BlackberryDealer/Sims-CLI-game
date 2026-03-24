package simcli.ui;

import simcli.entities.actors.Sim;
import simcli.world.interactables.Interactable;

import java.util.List;

/**
 * Contract for rendering game state to the screen.
 * GameEngine depends on this interface rather than calling UIManager
 * static methods directly, so the view layer can change independently.
 */
public interface IRenderer {

    /** Clears the terminal screen. */
    void clear();

    /** Prints a randomly selected gameplay hint. */
    void printHint();

    /**
     * Renders the main HUD showing day, time, location, and the
     * active player's current need values.
     */
    void renderHUD(Sim player, simcli.world.Building location, int day, String formattedTime,
            String timeOfDay, boolean inRoom, String roomName);

    /**
     * Renders a compact dashboard of other alive Sims in the neighborhood
     */
    void renderHouseholdDashboard(List<Sim> neighborhood, Sim activePlayer);

    /**
     * Renders detailed stats for the currently controlled Sim right above the action menu
     */
    void renderActiveSimStats(Sim activePlayer, List<Sim> neighborhood);

    /**
     * Renders the memorial statistics block when a Sim dies.
     */
    void renderDeathStats(Sim deadSim);

    /**
     * Renders the grid of available actions (interactables + system commands).
     *
     * @param activePlayer  the currently controlled Sim
     * @param items         interactable objects at the current location
     * @param isResidential whether house-specific commands should be shown
     */
    void renderActions(Sim activePlayer, List<Interactable> items, boolean isResidential);
}
