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

    // World Stats tracking (aggregated upon save/game over)
    private final NPCManager npcManager;
    private int sessionTotalMoney;
    private int sessionTotalItems;
    private Sim activePlayer;

    /**
     * Constructs a new {@code GameEngine} for a brand-new world.
     *
     * <p>Initialises all subsystems, places every Sim inside the default
     * residential building, and selects the first Sim as the active player.</p>
     *
     * @param worldName            the display name for this save slot.
     * @param startingNeighborhood the initial list of Sims (at least one).
     */
    // CONSTRUCTOR: For Creating a New World
    public GameEngine(String worldName, List<Sim> startingNeighborhood) {
        this.worldName = worldName;
        this.timeManager = new TimeManager(1, GameConstants.TICKS_PER_DAY); // 24 ticks per day
        this.isGameOver = false;
        this.neighborhood = startingNeighborhood;
        this.npcManager = new NPCManager();
        this.npcManager.replenishNPCs(3);
        this.worldManager = new WorldManager();
        ((WorldManager)this.worldManager).setEngine(this);
        this.worldManager.setupWorld();
        this.activePlayer = startingNeighborhood.get(0);
        this.inputHandler = new InputHandler(this.worldManager, this.timeManager,
                this.neighborhood, this::setActivePlayer);
        this.renderer = new TerminalRenderer();
        this.randomEventManager = new RandomEventManager();
        this.lifecycleManager = new LifecycleManager(GameConstants.DAYS_PER_AGE_TICK);
        this.gameLoop = new GameLoop(this.timeManager, this.neighborhood, this.randomEventManager, this.lifecycleManager);
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
    // CONSTRUCTOR: For Loading an Existing World
    public GameEngine(String worldName, int currentTick, List<Sim> loadedNeighborhood, boolean isGameOver) {
        this.worldName = worldName;
        this.timeManager = new TimeManager(currentTick, GameConstants.TICKS_PER_DAY);
        this.neighborhood = loadedNeighborhood;
        this.isGameOver = isGameOver;
        this.npcManager = new NPCManager();
        this.worldManager = new WorldManager();
        ((WorldManager)this.worldManager).setEngine(this);
        this.worldManager.setupWorld();
        
        // Find first alive sim, or default to 0 if all dead
        Sim firstAlive = loadedNeighborhood.get(0);
        for(Sim s : loadedNeighborhood) {
            if(s.getState() != SimState.DEAD) {
                firstAlive = s;
                break;
            }
        }
        this.activePlayer = firstAlive;
        
        this.inputHandler = new InputHandler(this.worldManager, this.timeManager,
                this.neighborhood, this::setActivePlayer);
        this.renderer = new TerminalRenderer();
        this.randomEventManager = new RandomEventManager();
        this.lifecycleManager = new LifecycleManager(GameConstants.DAYS_PER_AGE_TICK);
        this.gameLoop = new GameLoop(this.timeManager, this.neighborhood, this.randomEventManager, this.lifecycleManager);
    }

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

    /**
     * Runs the main game loop until the player saves-and-exits or all Sims die.
     *
     * <p>Each iteration: renders the HUD, processes player input via
     * {@link InputHandler}, advances the simulation clock via
     * {@link GameLoop}, and periodically auto-saves.</p>
     *
     * @param scanner shared {@link Scanner} for reading player input.
     */
    public void run(Scanner scanner) {
        boolean running = true;
        boolean tickForward = true;
        
        for (Sim sim : this.neighborhood) {
            if (sim.getCurrentRoom() == null && sim.getState() != SimState.DEAD) {
                this.worldManager.getCurrentLocation().enter(sim);
            }
        }

        while (running) {
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

            if (tickForward) {
                if (activePlayer.getState() == SimState.DEAD) {
                    Sim deadSim = activePlayer;
                    renderer.renderDeathStats(deadSim);

                    this.activePlayer = getNextAliveSim();
                    activePlayer = this.activePlayer;
                    if (this.activePlayer == null) {
                        this.isGameOver = true;
                        aggregateStats();
                        UIManager.printGameOverStats(this.timeManager.getCurrentTick(),
                                this.sessionTotalMoney, this.sessionTotalItems);
                        UIManager.printMessage("Saving final state...");
                        SaveManager.saveGame(this, this.worldName);
                        running = false;
                        UIManager.prompt("\nPress ENTER to end simulation...");
                        scanner.nextLine();
                        break;
                    } else {
                        UIManager.printMessage("Switching control to " + activePlayer.getName() + ".");
                        this.worldManager.getCurrentLocation().enter(activePlayer);
                        UIManager.prompt("\nPress ENTER to continue...");
                        scanner.nextLine();
                        continue;
                    }
                } else if (activePlayer.getState() == SimState.HUNGRY) {
                    UIManager.printMessage("\n[WARNING] " + activePlayer.getName() + " is HUNGRY! Feed them!");
                }
            }

            tickForward = true;

            renderer.renderActiveSimStats(activePlayer, this.neighborhood);

            UIManager.printMessage("Inventory Logs: " + activePlayer.getInventory().size() + " items");
            List<Interactable> items;
            if (inRoom) {
                items = activePlayer.getCurrentRoom().getInteractables();
            } else {
                items = this.worldManager.getCurrentLocation().getInteractables();
            }

            renderer.renderActions(activePlayer, items, this.worldManager.getCurrentLocation() instanceof Residential);
            SimulationLogger.flushAndPrint();
            UIManager.prompt("\nCOMMAND> ");

            String input = scanner.nextLine().toUpperCase();

            CommandResult result = inputHandler.handle(input, activePlayer, scanner);

            switch (result) {
                case TICK_FORWARD:
                    tickForward = true;
                    break;
                case NO_TICK:
                    tickForward = false;
                    break;
                case SLEEP_EVENT:
                    renderer.clear();
                    renderer.renderHUD(activePlayer, this.worldManager.getCurrentLocation(),
                            timeManager.getCurrentDay(), timeManager.getFormattedTime(),
                            timeManager.getTimeOfDay(), inRoom, roomName);

                    int currentInDay = timeManager.getCurrentTick() % GameConstants.TICKS_PER_DAY;
                    int ticksToMorning = (GameConstants.TICKS_PER_DAY - currentInDay + 8) % GameConstants.TICKS_PER_DAY;
                    if (ticksToMorning == 0)
                        ticksToMorning = GameConstants.TICKS_PER_DAY;

                    UIManager.printMessage("\n" + activePlayer.getName() + " sleeps deeply in the bed for "
                            + ticksToMorning + " hours.");
                    UIManager.sleepAnimation();

                    timeManager.advanceTicks(ticksToMorning - 1);
                    tickForward = true;
                    break;
                case SAVE_AND_EXIT:
                    running = false;
                    UIManager.printMessage("Saving game...");
                    SaveManager.saveGame(this, this.worldName);
                    UIManager.printMessage("Game Saved! Returning to Main Menu...\n");
                    UIManager.prompt("Press ENTER to exit...");
                    scanner.nextLine();
                    continue; // Will break out of loop since running is false
            }

            if (running && tickForward) {
                gameLoop.processTick(activePlayer);

                if (timeManager.getCurrentTick() % 10 == 0) {
                    UIManager.printMessage("[System] Autosaving...");
                    SaveManager.saveGame(this, this.worldName);
                }

                UIManager.prompt("\nPress ENTER to continue to the next turn...");
                scanner.nextLine();
            }
        }
    }

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