package simcli.engine;

import simcli.entities.Gender;
import simcli.entities.Job;
import simcli.entities.Sim;
import simcli.entities.SimState;
import simcli.ui.IRenderer;
import simcli.ui.TerminalRenderer;
import simcli.utils.SaveManager;

import java.util.ArrayList;
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

    // World Stats tracking (aggregated upon save/game over)
    private int sessionTotalMoney;
    private int sessionTotalItems;

    // CONSTRUCTOR: For Creating a New World
    public GameEngine(String worldName) {
        this.worldName = worldName;
        this.timeManager = new TimeManager(1, 24); // 24 ticks per day
        this.isGameOver = false;
        this.neighborhood = new ArrayList<>();
        this.worldManager = new WorldManager();
        this.worldManager.setupWorld();
        this.inputHandler = new InputHandler(this.worldManager, this.timeManager);
        this.renderer = new TerminalRenderer();
    }

    // CONSTRUCTOR: For Loading an Existing World
    public GameEngine(String worldName, int currentTick, List<Sim> loadedNeighborhood, boolean isGameOver) {
        this.worldName = worldName;
        this.timeManager = new TimeManager(currentTick, 24);
        this.neighborhood = loadedNeighborhood;
        this.isGameOver = isGameOver;
        this.worldManager = new WorldManager();
        this.worldManager.setupWorld();
        this.inputHandler = new InputHandler(this.worldManager, this.timeManager);
        this.renderer = new TerminalRenderer();
    }

    public void init(Scanner scanner) {
        simcli.ui.UIManager.printMessage("\n=== Character Creation ===");
        simcli.ui.UIManager.prompt("Enter your Sim's Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty())
            name = "Dylan";

        int age = 21;
        while (true) {
            simcli.ui.UIManager.prompt("Enter your Sim's Age (18-80): ");
            try {
                String inputAge = scanner.nextLine().trim();
                if (inputAge.isEmpty())
                    break;
                int parsedAge = Integer.parseInt(inputAge);
                if (parsedAge >= 18 && parsedAge <= 80) {
                    age = parsedAge;
                    break;
                } else {
                    simcli.ui.UIManager.printMessage("Age must be between 18 and 80. Please try again.");
                }
            } catch (NumberFormatException e) {
                simcli.ui.UIManager.printMessage("Invalid age format. Please enter a valid number.");
            }
        }

        Gender gender = Gender.MALE;
        while (true) {
            simcli.ui.UIManager.prompt("Enter your Sim's Gender (M/F): ");
            String gInput = scanner.nextLine().trim().toUpperCase();
            if (gInput.equals("M")) {
                gender = Gender.MALE;
                break;
            }
            if (gInput.equals("F")) {
                gender = Gender.FEMALE;
                break;
            }
            simcli.ui.UIManager.printMessage("Please enter M or F.");
        }

        Sim player1 = new Sim(name, age, gender, Job.UNEMPLOYED);
        this.neighborhood.add(player1);
        simcli.ui.UIManager.printMessage("\n=== Booting World: " + this.worldName + " ===");
    }

    public void run(Scanner scanner) {
        boolean running = true;
        boolean tickForward = true;
        Sim activePlayer = this.neighborhood.get(0);
        this.worldManager.getCurrentLocation().enter(activePlayer);

        while (running) {
            renderer.clear();
            renderer.printHint();

            boolean inRoom = this.worldManager.getCurrentLocation() instanceof simcli.world.Residential
                    && activePlayer.getCurrentRoom() != null;
            String roomName = inRoom ? activePlayer.getCurrentRoom().getName() : "";

            renderer.renderHUD(activePlayer, this.worldManager.getCurrentLocation(),
                    timeManager.getCurrentDay(), timeManager.getFormattedTime(),
                    timeManager.getTimeOfDay(), inRoom, roomName);

            if (tickForward) {
                activePlayer.tick();

                if (activePlayer.getState() == SimState.DEAD) {
                    simcli.ui.UIManager.printMessage("Oh no! " + activePlayer.getName() + " has tragically died!");
                    activePlayer = getNextAliveSim();
                    if (activePlayer == null) {
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
                } else if (activePlayer.getState() == SimState.STARVING) {
                    int ticksLeft = 4 - activePlayer.getStarvingTicks();
                    simcli.ui.UIManager
                            .printMessage("\n[WARNING] " + activePlayer.getName() + " is STARVING! Feed them within "
                                    + ticksLeft + " ticks or they will DIE!");
                }
            } else {
                simcli.ui.UIManager.printMessage(
                        "[" + activePlayer.getName() + "] Hunger: " + activePlayer.getHunger().getValue() +
                                " | Energy: " + activePlayer.getEnergy().getValue() +
                                " | Hygiene: " + activePlayer.getHygiene().getValue() +
                                " | Happiness: " + activePlayer.getHappiness().getValue() +
                                " | Cash: $" + activePlayer.getMoney() + " | Status: " + activePlayer.getState());
            }

            tickForward = true;

            simcli.ui.UIManager.printMessage("Inventory Logs: " + activePlayer.getInventory().size() + " items");

            List<simcli.world.interactables.Interactable> items;
            if (inRoom) {
                items = activePlayer.getCurrentRoom().getInteractables();
            } else {
                items = this.worldManager.getCurrentLocation().getInteractables();
            }

            renderer.renderActions(items, this.worldManager.getCurrentLocation() instanceof simcli.world.Residential);
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

                    int currentInDay = timeManager.getCurrentTick() % 24;
                    int ticksToMorning = (24 - currentInDay + 8) % 24;
                    if (ticksToMorning == 0)
                        ticksToMorning = 24;

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
                simcli.engine.RandomEventManager.trigger(activePlayer, timeManager);

                if (timeManager.getCurrentDay() > previousDay) {
                    simcli.ui.UIManager
                            .printMessage("\n*** A new day has begun! (Day " + timeManager.getCurrentDay() + ") ***");
                    for (Sim s : neighborhood) {
                        s.growOlderDaily();
                        s.checkTruancy();
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

    public boolean getIsGameOver() {
        return isGameOver;
    }

    public int getSessionTotalMoney() {
        return sessionTotalMoney;
    }

    public int getSessionTotalItems() {
        return sessionTotalItems;
    }
}