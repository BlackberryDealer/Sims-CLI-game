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
            simcli.ui.UIManager.printMessage("\n=== FRIDGE INVENTORY ===");
            if (storedFood.isEmpty()) {
                simcli.ui.UIManager.printMessage("The fridge is completely empty!");
            } else {
                for (int i = 0; i < storedFood.size(); i++) {
                    simcli.ui.UIManager.printMessage("[" + (i + 1) + "] " + storedFood.get(i).getObjectName());
                }
            }
            simcli.ui.UIManager.printMessage("\n[S] Store Food from Your Personal Inventory");
            simcli.ui.UIManager.printMessage("[0] Close Fridge");
            simcli.ui.UIManager.prompt("Action (number to EAT, S to store)> ");
            
            String choice = scanner.nextLine().trim().toUpperCase();
            
            if (choice.equals("0")) {
                open = false;
            } else if (choice.equals("S")) {
                simcli.ui.UIManager.printMessage("Select a food item from your inventory to store:");
                List<Item> inv = sim.getInventory();
                List<Integer> foodIndices = new ArrayList<>();
                for (int i = 0; i < inv.size(); i++) {
                    if (inv.get(i) instanceof Food) {
                        simcli.ui.UIManager.printMessage("[" + (i + 1) + "] " + inv.get(i).getObjectName());
                        foodIndices.add(i);
                    }
                }
                if (foodIndices.isEmpty()) {
                    simcli.ui.UIManager.printMessage("No food in your personal inventory!");
                } else {
                    simcli.ui.UIManager.prompt("Item to store (0 to cancel)> ");
                    try {
                        int st = Integer.parseInt(scanner.nextLine().trim());
                        if (st > 0 && st <= inv.size() && foodIndices.contains(st - 1)) {
                            Item item = inv.get(st - 1);
                            inv.remove(item);
                            storeItem(item);
                            simcli.ui.UIManager.printMessage("Stored " + item.getObjectName() + " in the fridge.");
                        } else if (st != 0) {
                            simcli.ui.UIManager.printMessage("Invalid selection.");
                        }
                    } catch(Exception e) {
                        simcli.ui.UIManager.printMessage("Invalid input.");
                    }
                }
                simcli.ui.UIManager.prompt("Press ENTER to return...");
                scanner.nextLine();
            } else {
                try {
                    int c = Integer.parseInt(choice);
                    if (c > 0 && c <= storedFood.size()) {
                        Item food = retrieveItem(c - 1);
                        simcli.ui.UIManager.printMessage(sim.getName() + " takes " + food.getObjectName() + " from the fridge and eats it.");
                            food.interact(sim, scanner, timeManager);
                        simcli.ui.UIManager.prompt("Press ENTER to continue...");
                        scanner.nextLine();
                    } else {
                        simcli.ui.UIManager.printMessage("Invalid choice.");
                        simcli.ui.UIManager.prompt("Press ENTER to continue...");
                        scanner.nextLine();
                    }
                } catch(Exception e) {
                    simcli.ui.UIManager.printMessage("Invalid input.");
                    simcli.ui.UIManager.prompt("Press ENTER to continue...");
                    scanner.nextLine();
                }
            }
        }
    }
    
    @Override
    public String getObjectName() { return "Fridge"; }
}