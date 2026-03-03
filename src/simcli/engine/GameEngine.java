package simcli.engine;

import simcli.entities.AdultSim;
import simcli.entities.Job;
import simcli.entities.Sim;
import simcli.entities.SimState;
import simcli.ui.AsciiArt;
import simcli.world.Building;
import simcli.world.Commercial;
import simcli.world.Residential;
import simcli.world.interactables.*;
import simcli.utils.SaveManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameEngine {
    private List<Sim> neighborhood;
    private List<Building> cityMap;
    private Building currentLocation;
    private String worldName; 
    private int currentTick;

    // CONSTRUCTOR: For Creating a New World
    public GameEngine(String worldName) {
        this.worldName = worldName;
        this.currentTick = 1;
        this.neighborhood = new ArrayList<>();
        setupWorld();
    }

    // CONSTRUCTOR: For Loading an Existing World
    public GameEngine(String worldName, int currentTick, List<Sim> loadedNeighborhood) {
        this.worldName = worldName;
        this.currentTick = currentTick;
        this.neighborhood = loadedNeighborhood;
        setupWorld();
    }

    // Centralized method to build the map
    private void setupWorld() {
        this.cityMap = new ArrayList<>();
        
        // Build Home
        Residential home = new Residential("The Shared Dorm");
        home.addInteractable(new Bed());
        home.addInteractable(new Computer());
        home.addInteractable(new WeightBench());
        home.addInteractable(new Fridge()); // The new free/cheap food source
        this.cityMap.add(home);

        // Build Supermarket
        Commercial store = new Commercial("Town Supermarket");
        store.addInteractable(new GroceryShelf()); // The new paid food source
        this.cityMap.add(store);

        // Default spawn location
        this.currentLocation = home;
    }
    
    public void init(Scanner scanner) {
        System.out.println("\n=== Character Creation ===");
        System.out.print("Enter your Sim's Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = "Dylan";
        
        System.out.print("Enter your Sim's Age: ");
        int age = 21;
        try {
            String inputAge = scanner.nextLine().trim();
            if (!inputAge.isEmpty()) age = Integer.parseInt(inputAge);
        } catch (NumberFormatException e) {
            System.out.println("Invalid age. Defaulting to 21.");
        }
        
        System.out.println("Select a Starting Career:");
        Job[] jobs = Job.values();
        for (int i = 0; i < jobs.length; i++) {
            System.out.println("[" + (i+1) + "] " + jobs[i].getTitle() + " ($" + jobs[i].getSalary() + "/shift)");
        }
        System.out.print("Choice> ");
        Job selectedJob = Job.SOFTWARE_ENGINEER;
        try {
            String inputJob = scanner.nextLine().trim();
            if (!inputJob.isEmpty()) {
                int jobChoice = Integer.parseInt(inputJob) - 1;
                if (jobChoice >= 0 && jobChoice < jobs.length) {
                    selectedJob = jobs[jobChoice];
                } else {
                    System.out.println("Invalid choice. Defaulting to " + selectedJob.getTitle() + ".");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Defaulting to " + selectedJob.getTitle() + ".");
        }
        
        AdultSim player1 = new AdultSim(name, age, selectedJob);
        this.neighborhood.add(player1);
        System.out.println("\n=== Booting World: " + this.worldName + " ===");
    }
    
    public void run(Scanner scanner) {
        boolean running = true;
        boolean tickForward = true;
        Sim activePlayer = this.neighborhood.get(0);
        this.currentLocation.enter(activePlayer);
        
        while (running) {
            AsciiArt.clearScreen();
            
            if (currentTick == 3) {
                System.out.println("\n[Hint: To keep the UI clean, the screen clears between turns.]");
            }
            
            if (currentLocation.getName().contains("Dorm") || currentLocation.getName().contains("Home")) {
                AsciiArt.printHouse();
            } else if (currentLocation.getName().contains("Supermarket") || currentLocation.getName().contains("Market")) {
                AsciiArt.printStore();
            } else {
                System.out.println("   [" + currentLocation.getName().toUpperCase() + "]");
            }
            
            System.out.println("\n--- TICK " + currentTick + " | Location: " + currentLocation.getName() + " ---");
            
            if (tickForward) {
                activePlayer.tick();
                
                if (activePlayer.getState() == SimState.DEAD) {
                    System.out.println("Oh no! " + activePlayer.getName() + " has tragically died!");
                    activePlayer = getNextAliveSim();
                    if (activePlayer == null) {
                        System.out.println("\n*** GAME OVER ***\nAll your Sims have passed away.");
                        running = false;
                        System.out.print("\nPress ENTER to end simulation...");
                        scanner.nextLine();
                        break;
                    } else {
                        System.out.println("Switching control to " + activePlayer.getName() + ".");
                        this.currentLocation.enter(activePlayer);
                        System.out.print("\nPress ENTER to continue...");
                        scanner.nextLine();
                        continue; 
                    }
                } else if (activePlayer.getState() == SimState.STARVING) {
                    int ticksLeft = 4 - activePlayer.getStarvingTicks(); 
                    System.out.println("\n[WARNING] " + activePlayer.getName() + " is STARVING! Feed them within " + ticksLeft + " ticks or they will DIE!");
                }
            } else {
                System.out.println("[" + activePlayer.getName() + "] Hunger: " + activePlayer.getHunger().getValue() + 
                                   " | Energy: " + activePlayer.getEnergy().getValue() + 
                                   " | Cash: $" + activePlayer.getMoney() + " | Status: " + activePlayer.getState());
            }
            
            tickForward = true; // reset for next action
            
            System.out.println("Inventory -> Groceries: " + activePlayer.getGroceries());
            
            List<Interactable> items = currentLocation.getInteractables();
            System.out.println("\nAvailable Actions:");
            for (int i = 0; i < items.size(); i++) {
                System.out.print("[" + (i+1) + "] Use " + items.get(i).getObjectName() + "   ");
            }
            System.out.println("\n[W] Go to Work   [T] Travel   [I] Info / Status   [S] Save & Exit");
            System.out.print("COMMAND> ");
            
            String input = scanner.nextLine().toUpperCase();
            
            try {
                if (input.equals("W")) {
                    activePlayer.performActivity("Work");
                } else if (input.equals("T")) {
                    System.out.println("\nAvailable Locations:");
                    for (int i = 0; i < cityMap.size(); i++) {
                        System.out.println("[" + (i+1) + "] " + cityMap.get(i).getName());
                    }
                    System.out.println("[0] Cancel");
                    System.out.print("Select destination> ");
                    
                    int destStr = Integer.parseInt(scanner.nextLine().trim());
                    if (destStr == 0) {
                        tickForward = false;
                        continue;
                    } else if (destStr > 0 && destStr <= cityMap.size()) {
                        Building target = cityMap.get(destStr - 1);
                        if (currentLocation == target) {
                            System.out.println("You are already at " + target.getName() + "!");
                            tickForward = false;
                            System.out.print("\nPress ENTER to return...");
                            scanner.nextLine();
                            continue;
                        } else {
                            currentLocation = target;
                            AsciiArt.printTravelAnimation();
                            currentLocation.enter(activePlayer);
                        }
                    } else {
                        System.out.println("Invalid destination.");
                        tickForward = false;
                        System.out.print("\nPress ENTER to return...");
                        scanner.nextLine();
                        continue;
                    }
                } else if (input.equals("I")) {
                    System.out.println("\n=== SIM STATUS ===");
                    System.out.println("Name: " + activePlayer.getName());
                    System.out.println("Age: " + activePlayer.getAge());
                    System.out.println("Money: $" + activePlayer.getMoney());
                    if (activePlayer instanceof simcli.entities.AdultSim) {
                        System.out.println("Job: " + ((simcli.entities.AdultSim)activePlayer).getCareer().getTitle());
                    }
                    System.out.println("Hunger: " + activePlayer.getHunger().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
                    System.out.println("Energy: " + activePlayer.getEnergy().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
                    System.out.println("Groceries: " + activePlayer.getGroceries());
                    System.out.println("Location: " + currentLocation.getName());
                    System.out.println("==================");
                    System.out.print("\nPress ENTER to return...");
                    scanner.nextLine();
                    tickForward = false;
                    continue;
                } else if (input.equals("S")) {
                    running = false;
                    System.out.println("Saving game...");
                    SaveManager.saveGame(this, this.worldName);
                    System.out.println("Game Saved! Returning to Main Menu...\n");
                    System.out.print("Press ENTER to exit...");
                    scanner.nextLine();
                    break;
                } else {
                    int choice = Integer.parseInt(input) - 1;
                    if (choice >= 0 && choice < items.size()) {
                        items.get(choice).interact(activePlayer);
                    } else {
                        System.out.println("Invalid item choice.");
                        tickForward = false;
                        System.out.print("\nPress ENTER to return...");
                        scanner.nextLine();
                        continue;
                    }
                }
            } catch (simcli.engine.SimulationException e) {
                System.err.println("ACTION REJECTED: " + e.getMessage());
                tickForward = false;
                System.out.print("\nPress ENTER to return...");
                scanner.nextLine();
                continue;
            } catch (NumberFormatException e) {
                System.err.println("Invalid input.");
                tickForward = false;
                System.out.print("\nPress ENTER to return...");
                scanner.nextLine();
                continue;
            }
            
            if (running && tickForward) {
                currentTick++;
                if (currentTick % 10 == 0) {
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

    // Getters for SaveManager
    public String getWorldName() { return worldName; }
    public int getCurrentTick() { return currentTick; }
    public List<Sim> getNeighborhood() { return neighborhood; }
}