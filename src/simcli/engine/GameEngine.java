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
    private boolean isGameOver;
    
    // World Stats tracking (aggregated upon save/game over)
    private int sessionTotalMoney;
    private int sessionTotalItems;

    // CONSTRUCTOR: For Creating a New World
    public GameEngine(String worldName) {
        this.worldName = worldName;
        this.currentTick = 1;
        this.isGameOver = false;
        this.neighborhood = new ArrayList<>();
        setupWorld();
    }

    // CONSTRUCTOR: For Loading an Existing World
    public GameEngine(String worldName, int currentTick, List<Sim> loadedNeighborhood, boolean isGameOver) {
        this.worldName = worldName;
        this.currentTick = currentTick;
        this.neighborhood = loadedNeighborhood;
        this.isGameOver = isGameOver;
        setupWorld();
    }

    // Centralized method to build the map
    private void setupWorld() {
        this.cityMap = new ArrayList<>();
        
        // Build Home
        Residential home = new Residential("The Shared Dorm");
        
        simcli.world.Room mainRoom = new simcli.world.Room("Bedroom", 100);
        mainRoom.placeFurniture(new Bed(), 30);
        mainRoom.placeFurniture(new Computer(), 20);
        home.addRoom(mainRoom);
        
        simcli.world.Room kitchen = new simcli.world.Room("Kitchen", 50);
        kitchen.placeFurniture(new Fridge(), 25);
        home.addRoom(kitchen);
        
        simcli.world.Room gym = new simcli.world.Room("Garage", 60);
        gym.placeFurniture(new WeightBench(), 40);
        home.addRoom(gym);
        
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
        
        int age = 21;
        while (true) {
            System.out.print("Enter your Sim's Age (18-80): ");
            try {
                String inputAge = scanner.nextLine().trim();
                if (inputAge.isEmpty()) {
                    break;
                }
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
        
        System.out.println("Select a Starting Career:");
        Job[] jobs = Job.values();
        for (int i = 0; i < jobs.length; i++) {
            Job j = jobs[i];
            System.out.println("[" + (i+1) + "] " + j.getTitle() + 
                "\n    Salary: $" + j.getSalary() + " | Hours: " + j.getWorkingHours() + " ticks/shift | Drain: " + j.getEnergyDrain() + " energy | Req Age: " + j.getMinAge() + "-" + j.getMaxAge());
        }
        
        Job selectedJob = Job.SOFTWARE_ENGINEER;
        while (true) {
            System.out.print("Choice> ");
            try {
                String inputJob = scanner.nextLine().trim();
                // Default handling
                if (inputJob.isEmpty()) {
                    if (age >= selectedJob.getMinAge() && age <= selectedJob.getMaxAge()) {
                        break;
                    } else {
                        System.out.println("Default (Software Engineer) requires age " + selectedJob.getMinAge() + "-" + selectedJob.getMaxAge() + ". Please choose another.");
                        continue;
                    }
                }
                
                int jobChoice = Integer.parseInt(inputJob) - 1;
                if (jobChoice >= 0 && jobChoice < jobs.length) {
                    Job target = jobs[jobChoice];
                    if (age >= target.getMinAge() && age <= target.getMaxAge()) {
                        selectedJob = target;
                        break;
                    } else {
                        System.out.println("Invalid Age! This job requires age " + target.getMinAge() + "-" + target.getMaxAge() + ".");
                    }
                } else {
                    System.out.println("Invalid choice. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Try again.");
            }
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
                        this.isGameOver = true;
                        aggregateStats();
                        System.out.println("\n*** GAME OVER ***");
                        System.out.println("All your Sims have passed away.");
                        System.out.println("--- WORLD STATS ---");
                        System.out.println("Total Ticks Survived: " + this.currentTick);
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
            
            System.out.println("Inventory Logs: " + activePlayer.getInventory().size() + " items");
            
            List<Interactable> items = currentLocation.getInteractables();
            System.out.println("\nAvailable Actions:");
            for (int i = 0; i < items.size(); i++) {
                System.out.print("[" + (i+1) + "] Use " + items.get(i).getObjectName() + "   ");
            }
            System.out.println("\n[W] Go to Work   [T] Travel   [I] Info / Status   [V] Inventory   [U] Upgrade Room   [S] Save & Exit");
            System.out.print("COMMAND> ");
            
            String input = scanner.nextLine().toUpperCase();
            
            try {
                if (input.equals("W")) {
                    activePlayer.performActivity("Work");
                    if (activePlayer instanceof simcli.entities.AdultSim) {
                        currentTick += ((simcli.entities.AdultSim) activePlayer).getCareer().getWorkingHours() - 1;
                        // Time passes instantly for the active player, background decay happens for everyone normally on tickForward.
                    }
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
                    System.out.println("Inventory Items: " + activePlayer.getInventory().size());
                    System.out.println("Location: " + currentLocation.getName());
                    System.out.println("==================");
                    System.out.print("\nPress ENTER to return...");
                    scanner.nextLine();
                    tickForward = false;
                    continue;
                } else if (input.equals("V")) {
                    System.out.println("\n=== INVENTORY ===");
                    List<simcli.entities.Item> inv = activePlayer.getInventory();
                    if (inv.isEmpty()) {
                        System.out.println("Your inventory is empty.");
                    } else {
                        for (int i = 0; i < inv.size(); i++) {
                            System.out.println("[" + (i+1) + "] " + inv.get(i).getObjectName());
                        }
                    }
                    System.out.println("[0] Back");
                    System.out.print("Select item to Use/Place> ");
                    try {
                        int invChoice = Integer.parseInt(scanner.nextLine().trim());
                        if (invChoice > 0 && invChoice <= inv.size()) {
                            simcli.entities.Item selectedItem = inv.get(invChoice - 1);
                            if (selectedItem instanceof simcli.entities.Furniture && currentLocation instanceof simcli.world.Residential) {
                                simcli.entities.Furniture furn = (simcli.entities.Furniture) selectedItem;
                                simcli.world.Residential res = (simcli.world.Residential) currentLocation;
                                System.out.println("Select a room to place " + furn.getObjectName() + " (Requires " + furn.getSpaceScore() + " space):");
                                List<simcli.world.Room> rooms = res.getRooms();
                                for (int i = 0; i < rooms.size(); i++) {
                                    simcli.world.Room r = rooms.get(i);
                                    System.out.println("[" + (i+1) + "] " + r.getName() + " (Capacity: " + r.getUsedCapacity() + "/" + r.getMaxCapacity() + ")");
                                }
                                System.out.println("[0] Cancel");
                                System.out.print("Room> ");
                                int rChoice = Integer.parseInt(scanner.nextLine().trim());
                                if (rChoice > 0 && rChoice <= rooms.size()) {
                                    simcli.world.Room targetRoom = rooms.get(rChoice - 1);
                                    if (targetRoom.canFit(furn)) {
                                        Interactable instance = null;
                                        switch(furn.getObjectName()) {
                                            case "Bed": instance = new simcli.world.interactables.Bed(); break;
                                            case "Computer": instance = new simcli.world.interactables.Computer(); break;
                                            case "Fridge": instance = new simcli.world.interactables.Fridge(); break;
                                            case "Weight Bench": instance = new simcli.world.interactables.WeightBench(); break;
                                        }
                                        if (instance != null) {
                                            targetRoom.placeFurniture(instance, furn.getSpaceScore());
                                            activePlayer.getInventory().remove(selectedItem);
                                            System.out.println("Placed " + furn.getObjectName() + " in " + targetRoom.getName() + "!");
                                        }
                                    } else {
                                        System.out.println("Not enough space in " + targetRoom.getName() + "!");
                                    }
                                }
                            } else {
                                selectedItem.interact(activePlayer, scanner);
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid selection.");
                    }
                    tickForward = false;
                    System.out.print("\nPress ENTER to return...");
                    scanner.nextLine();
                    continue;
                } else if (input.equals("U")) {
                    if (currentLocation instanceof simcli.world.Residential) {
                        simcli.world.Residential res = (simcli.world.Residential) currentLocation;
                        System.out.println("\n=== UPGRADE ROOM ===");
                        System.out.println("Cost: $500 for +20 Capacity");
                        System.out.println("Your Cash: $" + activePlayer.getMoney());
                        List<simcli.world.Room> rooms = res.getRooms();
                        for (int i = 0; i < rooms.size(); i++) {
                            simcli.world.Room r = rooms.get(i);
                            System.out.println("[" + (i+1) + "] " + r.getName() + " (Capacity: " + r.getUsedCapacity() + "/" + r.getMaxCapacity() + ")");
                        }
                        System.out.println("[0] Cancel");
                        System.out.print("Room> ");
                        try {
                            int rChoice = Integer.parseInt(scanner.nextLine().trim());
                            if (rChoice > 0 && rChoice <= rooms.size()) {
                                simcli.world.Room targetRoom = rooms.get(rChoice - 1);
                                targetRoom.upgradeCapacity(activePlayer, 20, 500);
                            }
                        } catch (Exception e) {
                            System.out.println("Invalid selection.");
                        }
                    } else {
                        System.out.println("You can only upgrade rooms at home!");
                    }
                    tickForward = false;
                    System.out.print("\nPress ENTER to return...");
                    scanner.nextLine();
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
                        items.get(choice).interact(activePlayer, scanner);
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
    
    private void aggregateStats() {
        this.sessionTotalMoney = 0;
        this.sessionTotalItems = 0;
        for (Sim sim : neighborhood) {
            this.sessionTotalMoney += sim.getTotalMoneyEarned();
            this.sessionTotalItems += sim.getTotalItemsBought();
        }
    }

    // Getters for SaveManager
    public String getWorldName() { return worldName; }
    public int getCurrentTick() { return currentTick; }
    public List<Sim> getNeighborhood() { return neighborhood; }
    public boolean getIsGameOver() { return isGameOver; }
    public int getSessionTotalMoney() { return sessionTotalMoney; }
    public int getSessionTotalItems() { return sessionTotalItems; }
}