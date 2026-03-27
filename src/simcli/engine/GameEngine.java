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
 * Central coordinator (Facade) for the game simulation.
 * 
 * <p>Delegates state management to {@link GameStateManager},
 * statistics tracking to {@link StatisticsTracker}, world layout to
 * {@link WorldManager}, and aging to {@link LifecycleManager}.</p>
 * 
 * <p>Implements {@link IGameEngine} so that consumers (commands,
 * interactables, persistence) depend on the interface, not this class.</p>
 */
public class GameEngine implements IGameEngine {
    private final IWorldManager worldManager;
    private final IInputHandler inputHandler;
    private final IRenderer renderer;
    private final String worldName;
    private final TimeManager timeManager;
    private final RandomEventManager randomEventManager;
    private final GameLoop gameLoop;
    private final NPCManager npcManager;
    private final GameStateManager stateManager;
    private final StatisticsTracker statisticsTracker;

    // CONSTRUCTOR: For Creating a New World
    public GameEngine(String worldName, List<Sim> startingNeighborhood) {
        this.worldName = worldName;
        this.timeManager = new TimeManager(1, GameConstants.TICKS_PER_DAY);
        this.npcManager = new NPCManager();
        this.npcManager.replenishNPCs(3);
        this.stateManager = new GameStateManager(startingNeighborhood, startingNeighborhood.get(0));
        this.statisticsTracker = new StatisticsTracker();
        this.worldManager = new WorldManager(this.npcManager, this);
        this.worldManager.setupWorld();
        this.inputHandler = new InputHandler(this.worldManager, this.timeManager, this);
        this.renderer = new TerminalRenderer();
        this.randomEventManager = new RandomEventManager();
        LifecycleManager lifecycleManager = new LifecycleManager(GameConstants.TICKS_PER_DAY);
        this.gameLoop = new GameLoop(this.timeManager, startingNeighborhood, this.randomEventManager, lifecycleManager);
    }

    // CONSTRUCTOR: For Loading an Existing World
    public GameEngine(String worldName, int currentTick, List<Sim> loadedNeighborhood, boolean isGameOver) {
        this.worldName = worldName;
        this.timeManager = new TimeManager(currentTick, GameConstants.TICKS_PER_DAY);
        this.npcManager = new NPCManager();
        this.statisticsTracker = new StatisticsTracker();

        // Find first alive sim, or default to index 0 if all dead
        Sim firstAlive = loadedNeighborhood.get(0);
        for (Sim s : loadedNeighborhood) {
            if (s.getState() != SimState.DEAD) {
                firstAlive = s;
                break;
            }
        }
        this.stateManager = new GameStateManager(loadedNeighborhood, firstAlive);
        this.stateManager.setGameOver(isGameOver);

        this.worldManager = new WorldManager(this.npcManager, this);
        this.worldManager.setupWorld();
        this.inputHandler = new InputHandler(this.worldManager, this.timeManager, this);
        this.renderer = new TerminalRenderer();
        this.randomEventManager = new RandomEventManager();
        LifecycleManager lifecycleManager = new LifecycleManager(GameConstants.TICKS_PER_DAY);
        this.gameLoop = new GameLoop(this.timeManager, loadedNeighborhood, this.randomEventManager, lifecycleManager);
    }

    // -------------------------------------------------------------------------
    // IGameEngine Implementation — delegates to managers
    // -------------------------------------------------------------------------

    @Override
    public Sim getActivePlayer() { return stateManager.getActivePlayer(); }

    @Override
    public void setActivePlayer(Sim sim) { stateManager.setActivePlayer(sim); }

    @Override
    public IWorldManager getWorldManager() { return worldManager; }

    @Override
    public NPCManager getNpcManager() { return npcManager; }

    @Override
    public List<Sim> getNeighborhood() { return stateManager.getNeighborhood(); }

    @Override
    public String getWorldName() { return worldName; }

    @Override
    public int getCurrentTick() { return timeManager.getCurrentTick(); }

    @Override
    public boolean isGameOver() { return stateManager.isGameOver(); }

    @Override
    public int getSessionTotalMoney() { return statisticsTracker.getSessionTotalMoney(); }

    @Override
    public int getSessionTotalItems() { return statisticsTracker.getSessionTotalItems(); }

    // -------------------------------------------------------------------------
    // Game Loop
    // -------------------------------------------------------------------------

    public void run(Scanner scanner) {
        boolean running = true;
        boolean tickForward = true;
        List<Sim> neighborhood = stateManager.getNeighborhood();

        for (Sim sim : neighborhood) {
            if (sim.getCurrentRoom() == null && sim.getState() != SimState.DEAD) {
                this.worldManager.getCurrentLocation().enter(sim);
            }
        }

        while (running) {
            Sim activePlayer = stateManager.getActivePlayer();
            renderer.clear();
            renderer.printHint();

            boolean inRoom = this.worldManager.getCurrentLocation() instanceof Residential
                    && activePlayer.getCurrentRoom() != null;
            String roomName = inRoom ? activePlayer.getCurrentRoom().getName() : "";

            renderer.renderHUD(activePlayer, this.worldManager.getCurrentLocation(),
                    timeManager.getCurrentDay(), timeManager.getFormattedTime(),
                    timeManager.getTimeOfDay(), inRoom, roomName);

            renderer.renderHouseholdDashboard(neighborhood, activePlayer);

            if (tickForward) {
                if (activePlayer.getState() == SimState.DEAD) {
                    Sim deadSim = activePlayer;
                    renderer.renderDeathStats(deadSim);

                    stateManager.setActivePlayer(stateManager.getNextAliveSim());
                    activePlayer = stateManager.getActivePlayer();
                    if (activePlayer == null) {
                        stateManager.setGameOver(true);
                        statisticsTracker.aggregate(neighborhood);
                        UIManager.printGameOverStats(this.timeManager.getCurrentTick(),
                                statisticsTracker.getSessionTotalMoney(), statisticsTracker.getSessionTotalItems());
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

            renderer.renderActiveSimStats(activePlayer, neighborhood);

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
                    continue;
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
}