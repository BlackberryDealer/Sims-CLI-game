package simcli.engine.commands;

import simcli.engine.IWorldManager;
import simcli.engine.SimulationLogger;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.world.interactables.Interactable;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Consumer;

// Bundles all the game state a command might need into one object.
// Built once per turn using the Builder pattern, then shared across all commands.
//
// Why not just pass GameEngine? Decoupling — commands never import GameEngine,
// so they're easy to test and impossible to accidentally call engine internals.
//
// For mutations that need to reach back to the engine (e.g. switching the active
// Sim), we pass a Consumer<Sim> callback instead of an engine reference.
public class CommandContext {

    private final Sim activePlayer;
    private final List<Sim> neighborhood;
    private final Scanner scanner;
    private final TimeManager timeManager;
    private final IWorldManager worldManager;
    private final Building currentLocation;
    private final List<Interactable> availableItems;
    private final Consumer<Sim> setActivePlayer;   // callback → GameEngine.setActivePlayer
    private final SimulationLogger logger;

    // private constructor forces everyone to use the Builder
    private CommandContext(Builder builder) {
        this.activePlayer    = builder.activePlayer;
        this.neighborhood    = builder.neighborhood;
        this.scanner         = builder.scanner;
        this.timeManager     = builder.timeManager;
        this.worldManager    = builder.worldManager;
        this.currentLocation = builder.currentLocation;
        this.availableItems  = builder.availableItems;
        this.setActivePlayer = builder.setActivePlayer;
        this.logger          = builder.logger;
    }

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

    /** Returns the simulation logger for buffering messages. */
    public SimulationLogger getLogger() { return logger; }

    // delegates back to engine without knowing about engine (Dependency Inversion)
    public void switchActivePlayer(Sim sim) {
        if (setActivePlayer != null) {
            setActivePlayer.accept(sim);
        }
    }

    // Builder pattern — fluent API so InputHandler can construct the context cleanly
    public static class Builder {
        private Sim activePlayer;
        private List<Sim> neighborhood;
        private Scanner scanner;
        private TimeManager timeManager;
        private IWorldManager worldManager;
        private Building currentLocation;
        private List<Interactable> availableItems;
        private Consumer<Sim> setActivePlayer;
        private SimulationLogger logger;

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

        public Builder logger(SimulationLogger logger) {
            this.logger = logger;
            return this;
        }

        // validates required fields so we fail fast if something's missing
        public CommandContext build() {
            Objects.requireNonNull(activePlayer, "activePlayer is required");
            Objects.requireNonNull(neighborhood, "neighborhood is required");
            Objects.requireNonNull(scanner, "scanner is required");
            Objects.requireNonNull(timeManager, "timeManager is required");
            Objects.requireNonNull(worldManager, "worldManager is required");
            Objects.requireNonNull(currentLocation, "currentLocation is required");
            Objects.requireNonNull(logger, "logger is required");
            return new CommandContext(this);
        }
    }
}
