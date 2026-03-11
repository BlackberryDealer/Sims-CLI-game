package simcli.world.interactables;

import simcli.entities.Sim;
import simcli.engine.SimulationException;

public class GroceryShelf implements Interactable {

    @Override
    public void interact(Sim sim, java.util.Scanner scanner, simcli.engine.TimeManager timeManager) throws SimulationException {
        java.util.List<simcli.entities.Item> catalog = new java.util.ArrayList<>();
        catalog.add(new simcli.entities.Food("Apple", 10, 15, 5));
        catalog.add(new simcli.entities.Food("Steak", 40, 50, 20));
        catalog.add(new simcli.entities.Consumable("Perfume", 50, 0, 0, 30));
        catalog.add(new simcli.entities.Furniture("Bed", 200, 30));
        catalog.add(new simcli.entities.Furniture("Shower", 150, 10));
        catalog.add(new simcli.entities.Furniture("Storage Chest", 100, 10));

        if (sim.getMoney() >= 500 || sim.getAge() >= 25) {
            catalog.add(new simcli.entities.Furniture("Fridge", 300, 25));
        }

        if (sim.getAge() >= 22) {
            catalog.add(new simcli.entities.Furniture("Weight Bench", 150, 40));
        }

        boolean isEngineerOrRich = false;
        if (sim instanceof simcli.entities.AdultSim) {
            simcli.entities.AdultSim adult = (simcli.entities.AdultSim) sim;
            if (adult.getCareer() == simcli.entities.Job.SOFTWARE_ENGINEER) {
                isEngineerOrRich = true;
            }
        }
        if (sim.getMoney() >= 1000) {
            isEngineerOrRich = true;
        }

        if (isEngineerOrRich) {
            catalog.add(new simcli.entities.Furniture("Computer", 500, 20));
        }

        int pageSize = 10;
        int currentPage = 0;
        int totalPages = (int) Math.ceil((double)catalog.size() / pageSize);

        boolean shopping = true;
        while (shopping) {
            simcli.ui.UIManager.clearScreen();
            System.out.println("\n=== STORE MENU (Page " + (currentPage + 1) + " of " + Math.max(1, totalPages) + ") ===");
            System.out.println("Your Cash: $" + sim.getMoney() + " | Inventory: " + sim.getInventory().size() + "/" + sim.getInventoryCapacity());

            int startIdx = currentPage * pageSize;
            int endIdx = Math.min(startIdx + pageSize, catalog.size());

            for (int i = startIdx; i < endIdx; i++) {
                System.out.println("[" + (i - startIdx + 1) + "] " + catalog.get(i).getObjectName() + " - $" + catalog.get(i).getPrice());
            }

            System.out.println("\n[N] Next Page   [P] Previous Page");
            System.out.println("[0] Exit Store");
            System.out.print("Buy Action> ");

            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("0")) {
                shopping = false;
            } else if (input.equals("N")) {
                if (currentPage < totalPages - 1) currentPage++;
            } else if (input.equals("P")) {
                if (currentPage > 0) currentPage--;
            } else {
                try {
                    int choice = Integer.parseInt(input);
                    if (choice > 0 && choice <= (endIdx - startIdx)) {
                        int realIndex = startIdx + choice - 1;
                        simcli.entities.Item target = catalog.get(realIndex);

                        if (sim.getInventory().size() >= sim.getInventoryCapacity()) {
                            System.out.println("Your inventory is full! Use or place items at home to clear space.");
                            System.out.print("Press ENTER to return...");
                            scanner.nextLine();
                            continue;
                        }

                        if (sim.getMoney() >= target.getPrice()) {
                            System.out.println("You purchased " + target.getObjectName() + "!");
                            sim.setMoney(sim.getMoney() - target.getPrice());

                            simcli.entities.Item newItem = target.copyItem();
                            sim.addItem(newItem);
                            sim.addTotalItemsBought(1);
                        } else {
                            System.out.println("Not enough Simoleons!");
                        }
                    } else {
                        System.out.println("Invalid item choice.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Use numbers or N/P.");
                }
                if (shopping) {
                    System.out.print("Press ENTER to continue...");
                    scanner.nextLine();
                }
            }
        }
    }

    @Override
    public String getObjectName() {
        return "Store Register";
    }
}