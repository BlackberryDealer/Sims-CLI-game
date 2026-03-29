package simcli.engine.commands;

import simcli.engine.IWorldManager;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.world.interactables.Interactable;

import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;

/**
 * Immutable context object passed to every command in the Command Pattern.
 *
 * <p>Instead of each command having a bespoke constructor that cherry-picks
 * individual dependencies from {@code GameEngine}, every command receives a
 * single {@code CommandContext} and reads only what it needs. This:</p>
 * <ul>
 *     <li>Eliminates tight coupling between commands and {@code GameEngine}.</li>
 *     <li>Standardizes command construction — every command has the same
 *         constructor signature: {@code new XxxCommand(ctx)}.</li>
 *     <li>Makes commands trivially testable — just build a context with
 *         the fields the test cares about.</li>
 * </ul>
 *
 * <p>For state mutations that need to propagate back to the engine (e.g.
 * switching the active player), the context carries a callback
 * ({@link #setActivePlayer}) rather than a reference to the engine itself.</p>
 */
public class CommandContext {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final Sim activePlayer;
    private final List<Sim> neighborhood;
    private final Scanner scanner;
    private final TimeManager timeManager;
    private final IWorldManager worldManager;
    private final Building currentLocation;
    private final List<Interactable> availableItems;
    private final Consumer<Sim> setActivePlayer;

    // -------------------------------------------------------------------------
    // Constructor (Builder-style via static factory)
    // -------------------------------------------------------------------------

    private CommandContext(Builder builder) {
        this.activePlayer    = builder.activePlayer;
        this.neighborhood    = builder.neighborhood;
        this.scanner         = builder.scanner;
        this.timeManager     = builder.timeManager;
        this.worldManager    = builder.worldManager;
        this.currentLocation = builder.currentLocation;
        this.availableItems  = builder.availableItems;
        this.setActivePlayer = builder.setActivePlayer;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** Returns the currently controlled Sim. */
    public Sim getActivePlayer() { return activePlayer; }

    /** Returns every Sim in the current household/neighborhood. */
    public List<Sim> getNeighborhood() { return neighborhood; }

    /** Returns the shared input scanner. */
    public Scanner getScanner() { return scanner; }

    /** Returns the simulation clock manager. */
    public TimeManager getTimeManager() { return timeManager; }

    /** Returns the world/location manager. */
    public IWorldManager getWorldManager() { return worldManager; }

    /** Returns the building the player is currently inside. */
    public Building getCurrentLocation() { return currentLocation; }

    /** Returns the interactable items available at the current location/room. */
    public List<Interactable> getAvailableItems() { return availableItems; }

    /**
     * Switches the engine's active player. This is a callback that propagates
     * the mutation back to {@code GameEngine} without the command needing a
     * direct reference to the engine.
     *
     * @param sim the Sim to make active.
     */
    public void switchActivePlayer(Sim sim) {
        if (setActivePlayer != null) {
            setActivePlayer.accept(sim);
        }
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    /**
     * Fluent builder for constructing a {@code CommandContext}.
     * Used by {@code InputHandler} to assemble the context once per command
     * invocation.
     */
    public static class Builder {
        private Sim activePlayer;
        private List<Sim> neighborhood;
        private Scanner scanner;
        private TimeManager timeManager;
        private IWorldManager worldManager;
        private Building currentLocation;
        private List<Interactable> availableItems;
        private Consumer<Sim> setActivePlayer;

        public Builder activePlayer(Sim activePlayer) {
            this.activePlayer = activePlayer;
            return this;
        }

        public Builder neighborhood(List<Sim> neighborhood) {
            this.neighborhood = neighborhood;
            return this;
        }

        public Builder scanner(Scanner scanner) {
            this.scanner = scanner;
            return this;
        }

        public Builder timeManager(TimeManager timeManager) {
            this.timeManager = timeManager;
            return this;
        }

        public Builder worldManager(IWorldManager worldManager) {
            this.worldManager = worldManager;
            return this;
        }

        public Builder currentLocation(Building currentLocation) {
            this.currentLocation = currentLocation;
            return this;
        }

        public Builder availableItems(List<Interactable> availableItems) {
            this.availableItems = availableItems;
            return this;
        }

        public Builder setActivePlayer(Consumer<Sim> setActivePlayer) {
            this.setActivePlayer = setActivePlayer;
            return this;
        }

        public CommandContext build() {
            return new CommandContext(this);
        }
    }
}
