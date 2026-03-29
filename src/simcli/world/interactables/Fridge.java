package simcli.world.interactables;

import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.entities.items.Item;
import simcli.entities.items.Food;
import simcli.engine.SimulationException;
import simcli.entities.models.ActionState;
import simcli.ui.UIManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Represents a Fridge location or interactable object.
 */
public class Fridge implements Interactable, Container {
    private List<Item> storedFood = new ArrayList<>();

    @Override
    public List<Item> getStoredItems() {
        return storedFood;
    }

    @Override
    public void storeItem(Item item) {
        storedFood.add(item);
    }

    @Override
    public Item retrieveItem(int index) {
        if (index >= 0 && index < storedFood.size()) {
            return storedFood.remove(index);
        }
        return null;
    }

    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException {
        boolean open = true;
        while (open) {
            UIManager.clearScreen();
            UIManager.printMessage("\n=== FRIDGE INVENTORY ===");
            if (storedFood.isEmpty()) {
                UIManager.printMessage("The fridge is completely empty!");
            } else {
                for (int i = 0; i < storedFood.size(); i++) {
                    UIManager.printMessage("[" + (i + 1) + "] " + storedFood.get(i).getObjectName());
                }
            }
            UIManager.printMessage("\n[S] Store Food from Your Personal Inventory");
            UIManager.printMessage("[0] Close Fridge");
            UIManager.prompt("Action (number to EAT, S to store)> ");

            String choice = scanner.nextLine().trim().toUpperCase();

            if (choice.equals("0")) {
                open = false;
            } else if (choice.equals("S")) {
                UIManager.printMessage("Select a food item from your inventory to store:");
                List<Item> inv = sim.getInventory();
                List<Integer> foodIndices = new ArrayList<>();
                for (int i = 0; i < inv.size(); i++) {
                    if (inv.get(i) instanceof Food) {
                        UIManager.printMessage("[" + (i + 1) + "] " + inv.get(i).getObjectName());
                        foodIndices.add(i);
                    }
                }
                if (foodIndices.isEmpty()) {
                    UIManager.printMessage("No food in your personal inventory!");
                } else {
                    UIManager.prompt("Item to store (0 to cancel)> ");
                    try {
                        int st = Integer.parseInt(scanner.nextLine().trim());
                        if (st > 0 && st <= inv.size() && foodIndices.contains(st - 1)) {
                            Item item = inv.get(st - 1);
                            inv.remove(item);
                            storeItem(item);
                            UIManager.printMessage("Stored " + item.getObjectName() + " in the fridge.");
                        } else if (st != 0) {
                            UIManager.printMessage("Invalid selection.");
                        }
                    } catch (Exception e) {
                        UIManager.printMessage("Invalid input.");
                    }
                }
                UIManager.prompt("Press ENTER to return...");
                scanner.nextLine();
            } else {
                try {
                    int c = Integer.parseInt(choice);
                    if (c > 0 && c <= storedFood.size()) {
                        Item food = retrieveItem(c - 1);
                        sim.setCurrentAction(ActionState.EATING);
                        UIManager.displayActionAnimation(sim);
                        UIManager.printMessage(
                                sim.getName() + " takes " + food.getObjectName() + " from the fridge and eats it.");
                        food.interact(sim, scanner, timeManager);
                        UIManager.prompt("Press ENTER to continue...");
                        scanner.nextLine();
                    } else {
                        UIManager.printMessage("Invalid choice.");
                        UIManager.prompt("Press ENTER to continue...");
                        scanner.nextLine();
                    }
                } catch (Exception e) {
                    UIManager.printMessage("Invalid input.");
                    UIManager.prompt("Press ENTER to continue...");
                    scanner.nextLine();
                }
            }
        }
    }

    @Override
    public String getObjectName() {
        return "Fridge";
    }
}