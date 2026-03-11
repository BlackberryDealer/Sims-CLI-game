package simcli.world.interactables;

import simcli.entities.Sim;
import simcli.entities.Item;
import simcli.entities.Food;
import simcli.engine.SimulationException;
import java.util.ArrayList;
import java.util.List;

public class Fridge implements Interactable, Container {
    private List<Item> storedFood = new ArrayList<>();

    @Override
    public List<Item> getStoredItems() { return storedFood; }

    @Override
    public void storeItem(Item item) { storedFood.add(item); }

    @Override
    public Item retrieveItem(int index) {
        if (index >= 0 && index < storedFood.size()) {
            return storedFood.remove(index);
        }
        return null;
    }

    @Override
    public void interact(Sim sim, java.util.Scanner scanner, simcli.engine.TimeManager timeManager) throws SimulationException {
        boolean open = true;
        while (open) {
            simcli.ui.UIManager.clearScreen();
            System.out.println("\n=== FRIDGE INVENTORY ===");
            if (storedFood.isEmpty()) {
                System.out.println("The fridge is completely empty!");
            } else {
                for (int i = 0; i < storedFood.size(); i++) {
                    System.out.println("[" + (i + 1) + "] " + storedFood.get(i).getObjectName());
                }
            }
            System.out.println("\n[S] Store Food from Your Personal Inventory");
            System.out.println("[0] Close Fridge");
            System.out.print("Action (number to EAT, S to store)> ");
            
            String choice = scanner.nextLine().trim().toUpperCase();
            
            if (choice.equals("0")) {
                open = false;
            } else if (choice.equals("S")) {
                System.out.println("Select a food item from your inventory to store:");
                List<Item> inv = sim.getInventory();
                List<Integer> foodIndices = new ArrayList<>();
                for (int i = 0; i < inv.size(); i++) {
                    if (inv.get(i) instanceof Food) {
                        System.out.println("[" + (i + 1) + "] " + inv.get(i).getObjectName());
                        foodIndices.add(i);
                    }
                }
                if (foodIndices.isEmpty()) {
                    System.out.println("No food in your personal inventory!");
                } else {
                    System.out.print("Item to store (0 to cancel)> ");
                    try {
                        int st = Integer.parseInt(scanner.nextLine().trim());
                        if (st > 0 && st <= inv.size() && foodIndices.contains(st - 1)) {
                            Item item = inv.get(st - 1);
                            inv.remove(item);
                            storeItem(item);
                            System.out.println("Stored " + item.getObjectName() + " in the fridge.");
                        } else if (st != 0) {
                            System.out.println("Invalid selection.");
                        }
                    } catch(Exception e) {
                        System.out.println("Invalid input.");
                    }
                }
                System.out.print("Press ENTER to return...");
                scanner.nextLine();
            } else {
                try {
                    int c = Integer.parseInt(choice);
                    if (c > 0 && c <= storedFood.size()) {
                        Item food = retrieveItem(c - 1);
                        System.out.println(sim.getName() + " takes " + food.getObjectName() + " from the fridge and eats it.");
                            food.interact(sim, scanner, timeManager);
                        System.out.print("Press ENTER to continue...");
                        scanner.nextLine();
                    } else {
                        System.out.println("Invalid choice.");
                        System.out.print("Press ENTER to continue...");
                        scanner.nextLine();
                    }
                } catch(Exception e) {
                    System.out.println("Invalid input.");
                    System.out.print("Press ENTER to continue...");
                    scanner.nextLine();
                }
            }
        }
    }
    
    @Override
    public String getObjectName() { return "Fridge"; }
}