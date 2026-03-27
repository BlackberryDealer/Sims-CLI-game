package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.entities.models.SimState;

import java.util.List;

/**
 * Manages game state: the household (neighborhood), active player, and
 * game-over status. Extracted from GameEngine to enforce the Single
 * Responsibility Principle.
 */
public class GameStateManager {
    private final List<Sim> neighborhood;
    private Sim activePlayer;
    private boolean isGameOver;

    /**
     * Creates a GameStateManager for a new game.
     *
     * @param neighborhood      the household Sim list
     * @param initialActivePlayer the first active player
     */
    public GameStateManager(List<Sim> neighborhood, Sim initialActivePlayer) {
        this.neighborhood = neighborhood;
        this.activePlayer = initialActivePlayer;
        this.isGameOver = false;
    }

    public List<Sim> getNeighborhood() {
        return neighborhood;
    }

    public Sim getActivePlayer() {
        return activePlayer;
    }

    public void setActivePlayer(Sim sim) {
        this.activePlayer = sim;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.isGameOver = gameOver;
    }

    /**
     * Finds the next alive Sim in the household.
     *
     * @return the next alive Sim, or null if all are dead
     */
    public Sim getNextAliveSim() {
        for (Sim sim : neighborhood) {
            if (sim.getState() != SimState.DEAD) {
                return sim;
            }
        }
        return null;
    }
}
