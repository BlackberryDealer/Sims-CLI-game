package simcli.world.interactables;
import simcli.engine.SimulationException;
import simcli.entities.Sim;
import simcli.entities.Item;
import simcli.ui.MenuPagination;
import java.util.ArrayList;
import java.util.List;

public class StorageChest implements Interactable, Container {
    private List<Item> storedItems = new ArrayList<>();
    
    @Override
    public List<Item> getStoredItems() { return storedItems; }

    @Override
    public void storeItem(Item item) { storedItems.add(item); }

    @Override
    public Item retrieveItem(int index) { return storedItems.remove(index); }

    @Override
    public void interact(Sim sim, java.util.Scanner scanner, simcli.engine.TimeManager timeManager) throws SimulationException {
        boolean inMenu = true;
        while (inMenu) {
            simcli.ui.UIManager.printMessage("\n=== STORAGE CHEST ===");
            simcli.ui.UIManager.printMessage("Chest Output: " + storedItems.size() + " items");
            simcli.ui.UIManager.printMessage("[1] Store Item from Inventory");
            simcli.ui.UIManager.printMessage("[2] Take Item from Chest");
            simcli.ui.UIManager.printMessage("[0] Close Chest");
            simcli.ui.UIManager.prompt("Action> ");
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                inMenu = false;
            } else if (input.equals("1")) {
                MenuPagination.displayPaginatedMenu("Your Inventory", sim.getInventory(), "Select item to store", scanner, 
                    (item, realIndex) -> {
                        storeItem(item);
                        sim.getInventory().remove(item);
                        simcli.ui.UIManager.printMessage("Stored " + item.getObjectName() + " into Chest.");
                    });
            } else if (input.equals("2")) {
                MenuPagination.displayPaginatedMenu("Chest Contents", storedItems, "Select item to take", scanner, 
                    (item, realIndex) -> {
                        if (sim.getInventory().size() < sim.getInventoryCapacity()) {
                            sim.addItem(item);
                            storedItems.remove(item);
                            simcli.ui.UIManager.printMessage("Took " + item.getObjectName() + " from Chest.");
                        } else {
                            simcli.ui.UIManager.printMessage("Your inventory is full!");
                        }
                    });
            } else {
                simcli.ui.UIManager.printMessage("Invalid selection.");
            }
        }
    }
    
    @Override
    public String getObjectName() { return "Storage Chest"; }
}
