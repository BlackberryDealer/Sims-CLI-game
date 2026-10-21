package simcli.world.interactables;

import java.util.List;
import java.util.Scanner;

import simcli.engine.ActionCancelledException;
import simcli.engine.SimulationException;
import simcli.engine.TimeManager;
import simcli.entities.items.Item;
import simcli.entities.actors.Sim;
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

        String title = shopName.replace(" Register", "") + " - what would you like?";
        boolean[] boughtSomething = {false};
        MenuPagination.displayPaginatedMenu(title, catalog, "Buy", scanner, (item, realIndex) -> {
            if (sim.getInventory().size() >= sim.getInventoryCapacity()) {
                UIManager.error("Inventory full! Cannot carry more items.");
            } else if (sim.getMoney() >= item.getPrice()) {
                sim.setMoney(sim.getMoney() - item.getPrice());
                sim.addTotalItemsBought(1);
                sim.getInventory().add(item.copyItem());
                boughtSomething[0] = true;
                UIManager.success("Purchased " + item.getObjectName() + " for $" + item.getPrice() + "!");
            } else {
                UIManager.error("Not enough money for " + item.getObjectName() + ".");
            }
        }, () -> "{k}cash{/} {G}$" + sim.getMoney() + "{/}   {k}bag{/} " + sim.getInventory().size()
                + "/" + sim.getInventoryCapacity());

        if (!boughtSomething[0]) {
            // just browsing — no time passes
            throw new ActionCancelledException();
        }
    }

    @Override
    public String getObjectName() {
        return shopName;
    }
}
