package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.entities.managers.NPCManager;
import simcli.entities.models.SimState;
import simcli.ui.IRenderer;
import simcli.ui.TerminalRenderer;
import simcli.persistence.SaveManager;
import simcli.ui.UIManager;
import simcli.utils.GameConstants;
import simcli.world.Residential;
import simcli.world.interactables.Interactable;

import java.util.List;
import java.util.Scanner;

/**
 * Core coordinator of the simulation game.
 *
 * <p>{@code GameEngine} owns the main game loop, maintains references to all
 * subsystems (time, world, rendering, input), and orchestrates the flow
 * between player commands and simulation processing. It follows the
 * <b>Facade</b> pattern — higher-level code only talks to the engine,
 * while the engine delegates to specialised managers internally.</p>
 *
 * <h2>Key Collaborators</h2>
 * <ul>
 *     <li>{@link TimeManager} — tick/day/time-of-day tracking</li>
 *     <li>{@link WorldManager} — building layout and current location</li>
 *     <li>{@link InputHandler} — translates raw input into commands</li>
 *     <li>{@link GameLoop} — per-tick need decay, events, lifecycle</li>
 *     <li>{@link RandomEventManager} — random event generation</li>
 *     <li>{@link LifecycleManager} — aging, death, retirement</li>
 * </ul>
 *
 * <h2>Dependency Injection</h2>
 * <p>All core dependencies ({@link IRenderer}, {@link SimulationLogger},
 * {@link TimeManager}, etc.) are injected via constructors or the
 * {@link #initCore(TimeManager, NPCManager, IRenderer)} helper, ensuring
 * the engine is fully testable with mock implementations.</p>
 */
public class GameEngine {
    private List<Sim> neighborhood;
    private IWorldManager worldManager;
    private IInputHandler inputHandler;
    private IRenderer renderer;
    private String worldName;
    private TimeManager timeManager;
    private boolean isGameOver;
    private RandomEventManager randomEventManager;
    private LifecycleManager lifecycleManager;
    private GameLoop gameLoop;
    private SimulationLogger logger;

    // World Stats tracking (aggregated upon save/game over)
    private NPCManager npcManager;
    private int sessionTotalMoney;
    private int sessionTotalItems;
    private Sim activePlayer;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Constructs a new {@code GameEngine} for a brand-new world.
     *
     * <p>Initialises all subsystems, places every Sim inside the default
     * residential building, and selects the first Sim as the active player.</p>
     *
     * @param worldName            the display name for this save slot.
     * @param startingNeighborhood the initial list of Sims (at least one).
     */
    public GameEngine(String worldName, List<Sim> startingNeighborhood) {
        this(worldName, startingNeighborhood, new TerminalRenderer());
    }

    /**
     * Constructs a new {@code GameEngine} for a brand-new world with an
     * explicit renderer (useful for testing or alternate UIs).
     *
     * @param worldName            the display name for this save slot.
     * @param startingNeighborhood the initial list of Sims (at least one).
     * @param renderer             the renderer to use for all UI output.
     */
    public GameEngine(String worldName, List<Sim> startingNeighborhood,
                      IRenderer renderer) {
        this.worldName = worldName;
        this.neighborhood = startingNeighborhood;
        this.npcManager = new NPCManager();
        this.npcManager.replenishNPCs(simcli.utils.GameConstants.MAX_NPCS);
        this.activePlayer = startingNeighborhood.get(0);

        initCore(new TimeManager(1, GameConstants.TICKS_PER_DAY), renderer);
    }

    /**
     * Constructs a {@code GameEngine} by restoring a previously saved world.
     *
     * <p>Rebuilds all subsystems from persisted state. The first non-dead
     * Sim in {@code loadedNeighborhood} is selected as the active player.</p>
     *
     * @param worldName          the display name for this save slot.
     * @param currentTick        the persisted tick counter to restore.
     * @param loadedNeighborhood the previously saved list of Sims.
     * @param isGameOver         whether the game was already over when saved.
     */
    public GameEngine(String worldName, int currentTick,
                      List<Sim> loadedNeighborhood, boolean isGameOver) {
        this(worldName, currentTick, loadedNeighborhood, isGameOver,
                new TerminalRenderer());
    }

    /**
     * Constructs a {@code GameEngine} by restoring a previously saved world
     * with an explicit renderer.
     *
     * @param worldName          the display name for this save slot.
     * @param currentTick        the persisted tick counter to restore.
     * @param loadedNeighborhood the previously saved list of Sims.
     * @param isGameOver         whether the game was already over when saved.
     * @param renderer           the renderer to use for all UI output.
     */
    public GameEngine(String worldName, int currentTick,
                      List<Sim> loadedNeighborhood, boolean isGameOver,
                      IRenderer renderer) {
        this.worldName = worldName;
        this.neighborhood = loadedNeighborhood;
        this.isGameOver = isGameOver;
        this.npcManager = new NPCManager();

        // Find first alive sim, or default to index 0 if all dead
        Sim firstAlive = loadedNeighborhood.get(0);
        for (Sim s : loadedNeighborhood) {
            if (s.getState() != SimState.DEAD) {
                firstAlive = s;
                break;
            }
        }
        this.activePlayer = firstAlive;

        initCore(new TimeManager(currentTick, GameConstants.TICKS_PER_DAY), renderer);
    }

    // -------------------------------------------------------------------------
    // Core Initialization (DRY helper)
    // -------------------------------------------------------------------------

    /**
     * Shared initialization logic used by all constructors.
     *
     * <p>Creates the world, input handler, logger, event manager, lifecycle
     * manager, and game loop. This eliminates the duplicated subsystem setup
     * that previously existed in each constructor independently.</p>
     *
     * @param timeManager the time manager (already configured with the correct start tick).
     * @param renderer    the renderer to use for all UI output.
     */
    private void initCore(TimeManager timeManager, IRenderer renderer) {
        this.timeManager = timeManager;
        this.renderer = renderer;

        // Logger — instance-based, registered globally for legacy callers
        this.logger = new SimulationLogger();
        SimulationLogger.setInstance(this.logger);

        // World
        this.worldManager = new WorldManager(this.npcManager, this.neighborhood);
        this.worldManager.setupWorld();

        // Input
        this.inputHandler = new InputHandler(this.worldManager, this.timeManager,
                this.neighborhood, this::setActivePlayer, this.logger);

        // Simulation subsystems (created by GameLoop to respect ownership)
        this.gameLoop = new GameLoop(this.timeManager, this.neighborhood, this.logger);
        this.randomEventManager = this.gameLoop.getRandomEventManager();
        this.lifecycleManager = this.gameLoop.getLifecycleManager();
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Returns the currently active (controlled) Sim. */
    public Sim getActivePlayer() { return activePlayer; }

    /**
     * Switches the active player to the given Sim.
     *
     * @param sim the Sim to make active.
     */
    public void setActivePlayer(Sim sim) { this.activePlayer = sim; }

    /** Returns the world/location manager used by this engine. */
    public IWorldManager getWorldManager() {
        return worldManager;
    }

    /** Returns the NPC manager responsible for park NPCs. */
    public NPCManager getNpcManager() {
        return npcManager;
    }

    /** Returns the lifecycle manager handling aging and death. */
    public LifecycleManager getLifecycleManager() {
        return lifecycleManager;
    }

    // -------------------------------------------------------------------------
    // Main Game Loop (Refactored — reads like a table of contents)
    // -------------------------------------------------------------------------

    /**
     * Runs the main game loop until the player saves-and-exits or all Sims die.
     *
     * <p>Each iteration: renders the HUD, processes player input via
     * {@link InputHandler}, advances the simulation clock via
     * {@link GameLoop}, and periodically auto-saves.</p>
     *
     * <p>The method body is intentionally high-level; all detailed logic is
     * delegated to private helper methods for readability and testability.</p>
     *
     * @param scanner shared {@link Scanner} for reading player input.
     */
    public void run(Scanner scanner) {
        boolean running = true;
        boolean tickForward = true;

        placeSimsInWorld();

        while (running) {
            renderFrame();

            if (tickForward) {
                Sim activePlayer = this.activePlayer;
                if (activePlayer.getState() == SimState.DEAD) {
                    running = handleDeadActivePlayer(scanner);
                    tickForward = true;
                    continue;
                } else if (activePlayer.getState() == SimState.HUNGRY) {
                    UIManager.printMessage("\n[WARNING] " + activePlayer.getName() + " is HUNGRY! Feed them!");
                }
            }

            tickForward = true;

            renderActionMenu();

            this.logger.flushAndPrint();
            UIManager.prompt("\nCOMMAND> ");

            String input = scanner.nextLine().toUpperCase();
            CommandResult result = inputHandler.handle(input, this.activePlayer, scanner);

            int ticksToAdvance = 1;

            switch (result) {
                case TICK_FORWARD:
                    tickForward = true;
                    ticksToAdvance = 1;
                    break;
                case NO_TICK:
                    tickForward = false;
                    break;
                case SLEEP_EVENT:
                    ticksToAdvance = handleSleepEvent();
                    tickForward = true;
                    break;
                case SAVE_AND_EXIT:
                    running = false;
                    UIManager.printMessage("Saving game...");
                    SaveManager.saveGame(this, this.worldName);
                    UIManager.printMessage("Game Saved! Returning to Main Menu...\n");
                    UIManager.prompt("Press ENTER to exit...");
                    scanner.nextLine();
                    continue;
            }

            if (running && tickForward) {
                gameLoop.processTick(this.activePlayer, ticksToAdvance);
                maybeAutosave();
                UIManager.prompt("\nPress ENTER to continue to the next turn...");
                scanner.nextLine();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private Helpers (extracted from the God Method)
    // -------------------------------------------------------------------------

    /**
     * Places every non-dead Sim inside the current (default) location.
     * Called once at the start of {@link #run(Scanner)}.
     */
    private void placeSimsInWorld() {
        for (Sim sim : this.neighborhood) {
            if (sim.getCurrentRoom() == null && sim.getState() != SimState.DEAD) {
                this.worldManager.getCurrentLocation().enter(sim);
            }
        }
    }

    /**
     * Renders the full game frame: clear screen, hint, HUD, and household dashboard.
     */
    private void renderFrame() {
        Sim activePlayer = this.activePlayer;
        renderer.clear();
        renderer.printHint();

        boolean inRoom = this.worldManager.getCurrentLocation() instanceof Residential
                && activePlayer.getCurrentRoom() != null;
        String roomName = inRoom ? activePlayer.getCurrentRoom().getName() : "";

        renderer.renderHUD(activePlayer, this.worldManager.getCurrentLocation(),
                timeManager.getCurrentDay(), timeManager.getFormattedTime(),
                timeManager.getTimeOfDay(), inRoom, roomName);

        renderer.renderHouseholdDashboard(this.neighborhood, activePlayer);
    }

    /**
     * Renders the active Sim's detailed stats and the action menu.
     */
    private void renderActionMenu() {
        Sim activePlayer = this.activePlayer;
        boolean inRoom = this.worldManager.getCurrentLocation() instanceof Residential
                && activePlayer.getCurrentRoom() != null;

        renderer.renderActiveSimStats(activePlayer, this.neighborhood);

        UIManager.printMessage("Inventory Logs: " + activePlayer.getInventory().size() + " items");
        List<Interactable> items;
        if (inRoom) {
            items = activePlayer.getCurrentRoom().getInteractables();
        } else {
            items = this.worldManager.getCurrentLocation().getInteractables();
        }

        renderer.renderActions(activePlayer, items,
                this.worldManager.getCurrentLocation() instanceof Residential);
    }

    /**
     * Handles the case when the currently active player has died.
     *
     * <p>Displays death stats, switches to the next alive Sim, or triggers
     * game-over if all Sims are dead.</p>
     *
     * @param scanner shared input scanner.
     * @return {@code true} to continue the game loop, {@code false} to exit.
     */
    private boolean handleDeadActivePlayer(Scanner scanner) {
        Sim deadSim = this.activePlayer;
        renderer.renderDeathStats(deadSim);

        this.activePlayer = getNextAliveSim();
        if (this.activePlayer == null) {
            // All Sims are dead — game over
            this.isGameOver = true;
            aggregateStats();
            UIManager.printGameOverStats(this.timeManager.getCurrentTick(),
                    this.sessionTotalMoney, this.sessionTotalItems);
            UIManager.printMessage("Saving final state...");
            SaveManager.saveGame(this, this.worldName);
            UIManager.prompt("\nPress ENTER to end simulation...");
            scanner.nextLine();
            return false;
        }

        Sim activePlayer = this.activePlayer;
        UIManager.printMessage("Switching control to " + activePlayer.getName() + ".");
        this.worldManager.getCurrentLocation().enter(activePlayer);
        UIManager.prompt("\nPress ENTER to continue...");
        scanner.nextLine();
        return true;
    }

    /**
     * Handles the sleep event: fast-forwards the clock to the next morning.
     *
     * <p>Re-renders the HUD so the player sees the updated time, then
     * calculates ticks to 08:00 (morning).</p>
     * 
     * @return the number of ticks required to reach the target morning hour
     */
    private int handleSleepEvent() {
        Sim activePlayer = this.activePlayer;
        renderer.clear();

        boolean inRoom = this.worldManager.getCurrentLocation() instanceof Residential
                && activePlayer.getCurrentRoom() != null;
        String roomName = inRoom ? activePlayer.getCurrentRoom().getName() : "";

        renderer.renderHUD(activePlayer, this.worldManager.getCurrentLocation(),
                timeManager.getCurrentDay(), timeManager.getFormattedTime(),
                timeManager.getTimeOfDay(), inRoom, roomName);

        int currentInDay = timeManager.getCurrentTick() % GameConstants.TICKS_PER_DAY;
        int ticksToMorning = (GameConstants.TICKS_PER_DAY - currentInDay + GameConstants.MORNING_HOUR) % GameConstants.TICKS_PER_DAY;
        if (ticksToMorning == 0) {
            ticksToMorning = GameConstants.TICKS_PER_DAY;
        }

        UIManager.printMessage("\n" + activePlayer.getName() + " sleeps deeply in the bed for "
                + ticksToMorning + " hours.");
        UIManager.sleepAnimation();

        return ticksToMorning;
    }

    /**
     * Periodically auto-saves the game state at the configured interval.
     *
     * <p>The interval is defined by {@link GameConstants#AUTOSAVE_INTERVAL_TICKS}.</p>
     */
    private void maybeAutosave() {
        if (timeManager.getCurrentTick() % GameConstants.AUTOSAVE_INTERVAL_TICKS == 0) {
            UIManager.printMessage("[System] Autosaving...");
            SaveManager.saveGame(this, this.worldName);
        }
    }

    // -------------------------------------------------------------------------
    // Utility Methods
    // -------------------------------------------------------------------------

    /**
     * Finds the next alive Sim in the neighborhood.
     *
     * @return the first non-dead Sim, or {@code null} if all Sims are dead.
     */
    private Sim getNextAliveSim() {
        for (Sim sim : neighborhood) {
            if (sim.getState() != SimState.DEAD) {
                return sim;
            }
        }
        return null;
    }

    /**
     * Aggregates lifetime money and item statistics across all Sims.
     * Called when the game ends to produce the final summary.
     */
    private void aggregateStats() {
        this.sessionTotalMoney = 0;
        this.sessionTotalItems = 0;
        for (Sim sim : neighborhood) {
            this.sessionTotalMoney += sim.getTotalMoneyEarned();
            this.sessionTotalItems += sim.getTotalItemsBought();
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** Returns the display name of this save slot. */
    public String getWorldName() {
        return worldName;
    }

    /** Returns the current simulation tick counter. */
    public int getCurrentTick() {
        return timeManager.getCurrentTick();
    }

    /** Returns the full list of Sims in this neighborhood. */
    public List<Sim> getNeighborhood() {
        return neighborhood;
    }

    /** Returns {@code true} if the game-over condition has been reached. */
    public boolean isGameOver() {
        return isGameOver;
    }

    /** Returns the total money earned across all Sims this session. */
    public int getSessionTotalMoney() {
        return sessionTotalMoney;
    }

    /** Returns the total items bought across all Sims this session. */
    public int getSessionTotalItems() {
        return sessionTotalItems;
    }
}