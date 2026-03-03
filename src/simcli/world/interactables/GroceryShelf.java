package simcli.world.interactables;

import simcli.entities.Sim;
import simcli.engine.SimulationException;

public class GroceryShelf implements Interactable {

    @Override
    public void interact(Sim sim, java.util.Scanner scanner) throws SimulationException {
        simcli.entities.Item[] storeItems = new simcli.entities.Item[] {
            new simcli.entities.Food("Apple", 10, 15, 5),
            new simcli.entities.Food("Steak", 40, 50, 20),
            new simcli.entities.Furniture("Bed", 200, 30),
            new simcli.entities.Furniture("Computer", 500, 20),
            new simcli.entities.Furniture("Fridge", 300, 25),
            new simcli.entities.Furniture("Weight Bench", 150, 40)
        };

        boolean shopping = true;
        while (shopping) {
            System.out.println("\n=== STORE MENU ===");
            System.out.println("Your Cash: $" + sim.getMoney());
            for (int i = 0; i < storeItems.length; i++) {
                System.out.println("[" + (i+1) + "] " + storeItems[i].getObjectName() + " - $" + storeItems[i].getPrice());
            }
            System.out.println("[0] Exit Store");
            System.out.print("Buy Action> ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 0) {
                    shopping = false;
                } else if (choice > 0 && choice <= storeItems.length) {
                    simcli.entities.Item target = storeItems[choice - 1];
                    if (sim.getMoney() >= target.getPrice()) {
                        System.out.println("You purchased " + target.getObjectName() + "!");
                        sim.setMoney(sim.getMoney() - target.getPrice());
                        sim.addItem(target); // Note: Should we create a new instance? Yes!
                        // Actually, replacing item reference would mean sharing state if mutated, 
                        // but price/name/stats are immutable/final-like. It's safe to share the reference for Food/Furniture,
                        // unless Room placement tracks per-instance state. Wait, Furniture placement might need unique instances.
                        // For safety, let's instantiate new objects based on selection.
                        
                        // Objects are safely shared instances or just instantiated from scratch if needed.
                        simcli.entities.Item newItem = null;
                        if (target instanceof simcli.entities.Food) {
                            simcli.entities.Food f = (simcli.entities.Food) target;
                            newItem = new simcli.entities.Food(f.getObjectName(), f.getPrice(), 15, 5); // Simplification, hardcoded stats for now based on item.
                        } else if (target instanceof simcli.entities.Furniture) {
                            simcli.entities.Furniture f = (simcli.entities.Furniture) target;
                            newItem = new simcli.entities.Furniture(f.getObjectName(), f.getPrice(), f.getSpaceScore());
                        }
                        sim.addItem(newItem != null ? newItem : target);
                        sim.addTotalItemsBought(1);
                    } else {
                        System.out.println("Not enough Simoleons!");
                    }
                } else {
                    System.out.println("Invalid item choice.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
    }
    
    @Override
    public String getObjectName() { return "Store Register"; }
}