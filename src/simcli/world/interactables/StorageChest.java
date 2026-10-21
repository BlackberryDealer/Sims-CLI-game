package simcli.world.interactables;
import simcli.engine.ActionCancelledException;
import simcli.engine.SimulationException;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.entities.items.Item;
import simcli.ui.MenuPagination;
import simcli.ui.UIManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A storage container placed in the world where a Sim can deposit Items
 * from their personal inventory for later retrieval.
 */
public class StorageChest implements Interactable, Container {
    private List<Item> storedItems = new ArrayList<>();
    
    @Override
    public List<Item> getStoredItems() { return storedItems; }

    @Override
    public void storeItem(Item item) { storedItems.add(item); }

    @Override
    public Item retrieveItem(int index) {
        return index >= 0 && index < storedItems.size() ? storedItems.remove(index) : null;
    }

    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException {
        boolean inMenu = true;
        List<Item> snapshot = new ArrayList<>(storedItems);
        while (inMenu) {
            UIManager.title("Storage Chest  " + storedItems.size() + (storedItems.size() == 1 ? " item" : " items"));
            UIManager.option("1", "Put something in", "from your bag (" + sim.getInventory().size() + "/"
                    + sim.getInventoryCapacity() + ")");
            UIManager.option("2", "Take something out");
            UIManager.option("0", "Close the chest");
            UIManager.prompt("\nChest> ");
            String input = scanner.nextLine().trim();

            if (input.equals("0") || input.isEmpty()) {
                inMenu = false;
            } else if (input.equals("1")) {
                MenuPagination.displayPaginatedMenu("Your bag", sim.getInventory(), "Store", scanner,
                    (item, realIndex) -> {
                        storeItem(item);
                        sim.getInventory().remove(item);
                        UIManager.success("Stored " + item.getObjectName() + " in the chest.");
                    });
            } else if (input.equals("2")) {
                MenuPagination.displayPaginatedMenu("Chest contents", storedItems, "Take", scanner,
                    (item, realIndex) -> {
                        if (sim.getInventory().size() < sim.getInventoryCapacity()) {
                            sim.addItem(item);
                            storedItems.remove(item);
                            UIManager.success("Took " + item.getObjectName() + " from the chest.");
                        } else {
                            UIManager.error("Your inventory is full!");
                        }
                    });
            } else {
                UIManager.error("Invalid selection.");
            }
        }
        if (snapshot.equals(storedItems)) {
            // nothing moved in or out — no time passes
            throw new ActionCancelledException();
        }
    }
    
    @Override
    public String getObjectName() { return "Storage Chest"; }
}
