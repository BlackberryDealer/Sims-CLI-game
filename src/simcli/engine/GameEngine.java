package simcli.engine;

import simcli.entities.AdultSim;
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
        System.out.println("\n=== Character Creation ===");
        System.out.print("Enter your Sim's Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty())
            name = "Dylan";

        int age = 21;
        while (true) {
            System.out.print("Enter your Sim's Age (18-80): ");
            try {
                String inputAge = scanner.nextLine().trim();
                if (inputAge.isEmpty())
                    break;
                int parsedAge = Integer.parseInt(inputAge);
                if (parsedAge >= 18 && parsedAge <= 80) {
                    age = parsedAge;
                    break;
                } else {
                    System.out.println("Age must be between 18 and 80. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid age format. Please enter a valid number.");
            }
        }

        AdultSim player1 = new AdultSim(name, age, Job.UNEMPLOYED);
        this.neighborhood.add(player1);
        System.out.println("\n=== Booting World: " + this.worldName + " ===");
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

            renderer.renderHUD(activePlayer, this.worldManager.getCurrentLocation().getName(),
                    timeManager.getCurrentDay(), timeManager.getCurrentTick(),
                    timeManager.getTimeOfDay(), inRoom, roomName);

            if (tickForward) {
                activePlayer.tick();

                if (activePlayer.getState() == SimState.DEAD) {
                    System.out.println("Oh no! " + activePlayer.getName() + " has tragically died!");
                    activePlayer = getNextAliveSim();
                    if (activePlayer == null) {
                        this.isGameOver = true;
                        aggregateStats();
                        System.out.println("\n*** GAME OVER ***");
                        System.out.println("All your Sims have passed away.");
                        System.out.println("--- WORLD STATS ---");
                        System.out.println("Total Ticks Survived: " + this.timeManager.getCurrentTick());
                        System.out.println("Total Money Earned: $" + this.sessionTotalMoney);
                        System.out.println("Total Items Bought: " + this.sessionTotalItems);
                        System.out.println("-------------------");
                        System.out.println("Saving final state...");
                        SaveManager.saveGame(this, this.worldName);
                        running = false;
                        System.out.print("\nPress ENTER to end simulation...");
                        scanner.nextLine();
                        break;
                    } else {
                        System.out.println("Switching control to " + activePlayer.getName() + ".");
                        this.worldManager.getCurrentLocation().enter(activePlayer);
                        System.out.print("\nPress ENTER to continue...");
                        scanner.nextLine();
                        continue;
                    }
                } else if (activePlayer.getState() == SimState.STARVING) {
                    int ticksLeft = 4 - activePlayer.getStarvingTicks();
                    System.out.println("\n[WARNING] " + activePlayer.getName() + " is STARVING! Feed them within "
                            + ticksLeft + " ticks or they will DIE!");
                }
            } else {
                System.out.println("[" + activePlayer.getName() + "] Hunger: " + activePlayer.getHunger().getValue() +
                        " | Energy: " + activePlayer.getEnergy().getValue() +
                        " | Hygiene: " + activePlayer.getHygiene().getValue() +
                        " | Happiness: " + activePlayer.getHappiness().getValue() +
                        " | Cash: $" + activePlayer.getMoney() + " | Status: " + activePlayer.getState());
            }

            tickForward = true;

            System.out.println("Inventory Logs: " + activePlayer.getInventory().size() + " items");

            List<simcli.world.interactables.Interactable> items;
            if (inRoom) {
                items = activePlayer.getCurrentRoom().getInteractables();
            } else {
                items = this.worldManager.getCurrentLocation().getInteractables();
            }

            renderer.renderActions(items, this.worldManager.getCurrentLocation() instanceof simcli.world.Residential);
            System.out.print("\nCOMMAND> ");

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
                    int currentInDay = timeManager.getCurrentTick() % 24;
                    int ticksToMorning = (24 - currentInDay + 8) % 24;
                    if (ticksToMorning == 0)
                        ticksToMorning = 24;
                    timeManager.advanceTicks(ticksToMorning - 1);
                    tickForward = true;
                    break;
                case SAVE_AND_EXIT:
                    running = false;
                    System.out.println("Saving game...");
                    SaveManager.saveGame(this, this.worldName);
                    System.out.println("Game Saved! Returning to Main Menu...\n");
                    System.out.print("Press ENTER to exit...");
                    scanner.nextLine();
                    continue; // Will break out of loop since running is false
            }

            if (running && tickForward) {
                timeManager.advanceTick();

                if (timeManager.getCurrentDay() > previousDay) {
                    System.out.println("\n*** A new day has begun! (Day " + timeManager.getCurrentDay() + ") ***");
                    for (Sim s : neighborhood) {
                        s.growOlderDaily();
                        if (s instanceof AdultSim) {
                            ((AdultSim) s).checkTruancy();
                        }
                    }
                }

                if (timeManager.getCurrentTick() % 10 == 0) {
                    System.out.println("[System] Autosaving...");
                    SaveManager.saveGame(this, this.worldName);
                }

                System.out.print("\nPress ENTER to continue to the next turn...");
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