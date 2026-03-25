package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.entities.managers.NPCManager;
import simcli.entities.models.SimState;
import simcli.ui.IRenderer;
import simcli.ui.TerminalRenderer;
import simcli.persistence.SaveManager;
import simcli.utils.GameConstants;

import java.util.List;
import java.util.Scanner;

public class GameEngine {
    private List<Sim> neighborhood;
    private IWorldManager worldManager;
    private IInputHandler inputHandler;
    private IRenderer renderer;
    private String worldName;
    private TimeManager timeManager;
    private boolean isGameOver;
    private RandomEventManager randomEventManager;

    // World Stats tracking (aggregated upon save/game over)
    private final NPCManager npcManager;
    private int sessionTotalMoney;
    private int sessionTotalItems;
    private Sim activePlayer;

    // CONSTRUCTOR: For Creating a New World
    public GameEngine(String worldName, List<Sim> startingNeighborhood) {
        this.worldName = worldName;
        this.timeManager = new TimeManager(1, GameConstants.TICKS_PER_DAY); // 24 ticks per day
        this.isGameOver = false;
        this.neighborhood = startingNeighborhood;
        this.worldManager = new WorldManager();
        ((WorldManager)this.worldManager).setEngine(this);
        this.worldManager.setupWorld();
        this.activePlayer = startingNeighborhood.get(0);
        this.npcManager = new NPCManager();
        this.npcManager.replenishNPCs(3);
        this.inputHandler = new InputHandler(this.worldManager, this.timeManager, this);
        this.renderer = new TerminalRenderer();
        this.randomEventManager = new RandomEventManager();
    }

    // CONSTRUCTOR: For Loading an Existing World
    public GameEngine(String worldName, int currentTick, List<Sim> loadedNeighborhood, boolean isGameOver) {
        this.worldName = worldName;
        this.timeManager = new TimeManager(currentTick, GameConstants.TICKS_PER_DAY);
        this.neighborhood = loadedNeighborhood;
        this.isGameOver = isGameOver;
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
        this.npcManager = new NPCManager();
        
        this.inputHandler = new InputHandler(this.worldManager, this.timeManager, this);
        this.renderer = new TerminalRenderer();
        this.randomEventManager = new RandomEventManager();
    }

    public Sim getActivePlayer() { return activePlayer; }
    public void setActivePlayer(Sim sim) { this.activePlayer = sim; }



    public IWorldManager getWorldManager() {
        return worldManager;
    }

    public NPCManager getNpcManager() {
        return npcManager;
    }

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

            boolean inRoom = this.worldManager.getCurrentLocation() instanceof simcli.world.Residential
                    && activePlayer.getCurrentRoom() != null;
            String roomName = inRoom ? activePlayer.getCurrentRoom().getName() : "";

            renderer.renderHUD(activePlayer, this.worldManager.getCurrentLocation(),
                    timeManager.getCurrentDay(), timeManager.getFormattedTime(),
                    timeManager.getTimeOfDay(), inRoom, roomName);

            renderer.renderHouseholdDashboard(this.neighborhood, activePlayer);

            if (tickForward) {
                for(Sim sim : this.neighborhood) {
                    sim.tick();
                }

                if (activePlayer.getState() == SimState.DEAD) {
                    Sim deadSim = activePlayer;
                    renderer.renderDeathStats(deadSim);

                    this.activePlayer = getNextAliveSim();
                    activePlayer = this.activePlayer;
                    if (this.activePlayer == null) {
                        this.isGameOver = true;
                        aggregateStats();
                        simcli.ui.UIManager.printGameOverStats(this.timeManager.getCurrentTick(),
                                this.sessionTotalMoney, this.sessionTotalItems);
                        simcli.ui.UIManager.printMessage("Saving final state...");
                        SaveManager.saveGame(this, this.worldName);
                        running = false;
                        simcli.ui.UIManager.prompt("\nPress ENTER to end simulation...");
                        scanner.nextLine();
                        break;
                    } else {
                        simcli.ui.UIManager.printMessage("Switching control to " + activePlayer.getName() + ".");
                        this.worldManager.getCurrentLocation().enter(activePlayer);
                        simcli.ui.UIManager.prompt("\nPress ENTER to continue...");
                        scanner.nextLine();
                        continue;
                    }
                } else if (activePlayer.getState() == SimState.HUNGRY) {
                    simcli.ui.UIManager
                            .printMessage("\n[WARNING] " + activePlayer.getName() + " is HUNGRY! Feed them!");
                }
            }

            tickForward = true;

            renderer.renderActiveSimStats(activePlayer, this.neighborhood);

            simcli.ui.UIManager.printMessage("Inventory Logs: " + activePlayer.getInventory().size() + " items");

            List<simcli.world.interactables.Interactable> items;
            if (inRoom) {
                items = activePlayer.getCurrentRoom().getInteractables();
            } else {
                items = this.worldManager.getCurrentLocation().getInteractables();
            }

            renderer.renderActions(activePlayer, items, this.worldManager.getCurrentLocation() instanceof simcli.world.Residential);
            simcli.engine.SimulationLogger.flushAndPrint();
            simcli.ui.UIManager.prompt("\nCOMMAND> ");

            int previousDay = timeManager.getCurrentDay();
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

                    simcli.ui.UIManager.printMessage("\n" + activePlayer.getName() + " sleeps deeply in the bed for "
                            + ticksToMorning + " hours.");
                    simcli.ui.UIManager.sleepAnimation();

                    timeManager.advanceTicks(ticksToMorning - 1);
                    tickForward = true;
                    break;
                case SAVE_AND_EXIT:
                    running = false;
                    simcli.ui.UIManager.printMessage("Saving game...");
                    SaveManager.saveGame(this, this.worldName);
                    simcli.ui.UIManager.printMessage("Game Saved! Returning to Main Menu...\n");
                    simcli.ui.UIManager.prompt("Press ENTER to exit...");
                    scanner.nextLine();
                    continue; // Will break out of loop since running is false
            }

            if (running && tickForward) {
                timeManager.advanceTick();
                this.randomEventManager.trigger(activePlayer, timeManager);

                if (timeManager.getCurrentDay() > previousDay) {
                    simcli.ui.UIManager
                            .printMessage("\n*** A new day has begun! (Day " + timeManager.getCurrentDay() + ") ***");
                    for (Sim s : neighborhood) {
                        s.growOlderDaily();
                        s.getCareerManager().checkTruancy(s.getName());
                    }
                }

                if (timeManager.getCurrentTick() % 10 == 0) {
                    simcli.ui.UIManager.printMessage("[System] Autosaving...");
                    SaveManager.saveGame(this, this.worldName);
                }

                simcli.ui.UIManager.prompt("\nPress ENTER to continue to the next turn...");
                scanner.nextLine();
            }
        }
    }

    private Sim getNextAliveSim() {
        for (Sim sim : neighborhood) {
            if (sim.getState() != SimState.DEAD) {
                return sim;
            }
        }
        return null;
    }

    private void aggregateStats() {
        this.sessionTotalMoney = 0;
        this.sessionTotalItems = 0;
        for (Sim sim : neighborhood) {
            this.sessionTotalMoney += sim.getTotalMoneyEarned();
            this.sessionTotalItems += sim.getTotalItemsBought();
        }
    }

    public String getWorldName() {
        return worldName;
    }

    public int getCurrentTick() {
        return timeManager.getCurrentTick();
    }

    public List<Sim> getNeighborhood() {
        return neighborhood;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public int getSessionTotalMoney() {
        return sessionTotalMoney;
    }

    public int getSessionTotalItems() {
        return sessionTotalItems;
    }
}