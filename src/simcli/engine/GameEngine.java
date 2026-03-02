package simcli.engine;

import simcli.entities.AdultSim;
import simcli.entities.Job;
import simcli.entities.Sim;
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
    
    public void init() {
        // Start the player with the Software Engineer job from our new Enum
        AdultSim player1 = new AdultSim("Dylan", 21, Job.SOFTWARE_ENGINEER);
        this.neighborhood.add(player1);
        System.out.println("\n=== Booting World: " + this.worldName + " ===");
    }
    
    public void run(Scanner scanner) {
        boolean running = true;
        Sim activePlayer = this.neighborhood.get(0);
        this.currentLocation.enter(activePlayer);
        
        while (running) {
            System.out.println("\n--- TICK " + currentTick + " | Location: " + currentLocation.getName() + " ---");
            activePlayer.tick();
            System.out.println("Inventory -> Groceries: " + activePlayer.getGroceries());
            
            List<Interactable> items = currentLocation.getInteractables();
            System.out.println("\nAvailable Actions:");
            for (int i = 0; i < items.size(); i++) {
                System.out.print("[" + (i+1) + "] Use " + items.get(i).getObjectName() + "   ");
            }
            System.out.println("\n[W] Go to Work   [T] Travel   [S] Save & Exit");
            System.out.print("COMMAND> ");
            
            String input = scanner.nextLine().toUpperCase();
            
            try {
                if (input.equals("W")) {
                    activePlayer.performActivity("Work");
                } else if (input.equals("T")) {
                    // Swap location logic
                    currentLocation = (currentLocation == cityMap.get(0)) ? cityMap.get(1) : cityMap.get(0);
                    currentLocation.enter(activePlayer);
                } else if (input.equals("S")) {
                    running = false;
                    System.out.println("Saving game...");
                    SaveManager.saveGame(this, this.worldName);
                    System.out.println("Game Saved! Returning to Main Menu...\n");
                } else {
                    // Handle numbers for objects
                    int choice = Integer.parseInt(input) - 1;
                    if (choice >= 0 && choice < items.size()) {
                        items.get(choice).interact(activePlayer);
                    } else {
                        System.out.println("Invalid item choice.");
                    }
                }
            } catch (SimulationException e) {
                System.err.println("ACTION REJECTED: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Time passes idly.");
            }
            
            if (running) {
                currentTick++;
                if (currentTick % 10 == 0) {
                    System.out.println("[System] Autosaving...");
                    SaveManager.saveGame(this, this.worldName); 
                }
            }
        }
    }

    // Getters for SaveManager
    public String getWorldName() { return worldName; }
    public int getCurrentTick() { return currentTick; }
    public List<Sim> getNeighborhood() { return neighborhood; }
}