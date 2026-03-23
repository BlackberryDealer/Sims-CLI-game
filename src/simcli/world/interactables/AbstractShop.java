package simcli.world.interactables;

import java.util.List;
import java.util.Scanner;

import simcli.engine.SimulationException;
import simcli.engine.TimeManager;
import simcli.entities.Item;
import simcli.entities.Sim;
import simcli.ui.MenuPagination;
import simcli.ui.UIManager;

/**
 * A reusable, extensible base architecture for all shop-like interactables.
 * Follows the Open/Closed Principle.
 */
public abstract class AbstractShop implements Interactable {
    protected String shopName;

    public AbstractShop(String name) {
        this.shopName = name;
    }

    /**
     * Subclasses define what items are sold.
     * They could check `sim` properties (age, job, money) to modify the catalog.
     */
    protected abstract List<Item> getCatalog(Sim sim);

    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException {
        List<Item> catalog = getCatalog(sim);

        MenuPagination.displayPaginatedMenu(shopName + " Catalog", catalog, "Buy Action", scanner, (item, realIndex) -> {
            if (sim.getInventory().size() >= sim.getInventoryCapacity()) {
                UIManager.printMessage("Inventory full! Cannot carry more items.");
            } else if (sim.getMoney() >= item.getPrice()) {
                sim.setMoney(sim.getMoney() - item.getPrice());
                sim.addTotalItemsBought(1);
                sim.getInventory().add(item.copyItem());
                UIManager.printMessage("Purchased " + item.getObjectName() + " for $" + item.getPrice() + "!");
                UIManager.printMessage("You are now left with $" + sim.getMoney() + "!");
            } else {
                UIManager.printMessage("Not enough money for " + item.getObjectName() + ".");
            }
        });
    }

    @Override
    public String getObjectName() {
        return shopName;
    }
}
