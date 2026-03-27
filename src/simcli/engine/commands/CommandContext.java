package simcli.engine.commands;

import simcli.engine.IGameEngine;
import simcli.engine.IWorldManager;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.world.Building;

import java.util.Scanner;

/**
 * Immutable data carrier that bundles common dependencies shared by all commands.
 * 
 * <p>Reduces constructor parameter bloat in command classes — instead of each
 * command accepting 3-5 separate constructor arguments, commands accept a single
 * {@code CommandContext} alongside any command-specific parameters.</p>
 * 
 * <p>All fields are {@code final} with getter-only access. No setters are
 * provided, enforcing immutability per the principle of least privilege.</p>
 */
public class CommandContext {
    private final Sim activePlayer;
    private final Scanner scanner;
    private final TimeManager timeManager;
    private final IWorldManager worldManager;
    private final Building currentLocation;
    private final IGameEngine engine;

    /**
     * Constructs a fully-populated command context.
     *
     * @param activePlayer    the Sim currently being controlled
     * @param scanner         the input scanner for user interaction
     * @param timeManager     the simulation time tracker
     * @param worldManager    the world/building manager
     * @param currentLocation the building the player is currently in
     * @param engine          the game engine interface for cross-cutting access
     */
    public CommandContext(Sim activePlayer, Scanner scanner, TimeManager timeManager,
                         IWorldManager worldManager, Building currentLocation, IGameEngine engine) {
        this.activePlayer = activePlayer;
        this.scanner = scanner;
        this.timeManager = timeManager;
        this.worldManager = worldManager;
        this.currentLocation = currentLocation;
        this.engine = engine;
    }

    public Sim getActivePlayer() { return activePlayer; }
    public Scanner getScanner() { return scanner; }
    public TimeManager getTimeManager() { return timeManager; }
    public IWorldManager getWorldManager() { return worldManager; }
    public Building getCurrentLocation() { return currentLocation; }
    public IGameEngine getEngine() { return engine; }
}
