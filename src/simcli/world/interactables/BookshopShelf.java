package simcli.world.interactables;

import java.util.ArrayList;
import java.util.List;

import simcli.entities.Sim;
import simcli.entities.Item;
import simcli.entities.Consumable;
import simcli.entities.Furniture;
import simcli.utils.GameConstants;

/**
 * A bookshop selling knowledge-related items and entertainment.
 * Demonstrates polymorphism — same AbstractShop base, different catalog
 * from GroceryShelf, as described in the proposal (Slide 7).
 */
public class BookshopShelf extends AbstractShop {

    public BookshopShelf() {
        super("Bookshop Register");
    }

    @Override
    protected List<Item> getCatalog(Sim sim) {
        List<Item> catalog = new ArrayList<>();
        // Books that boost fun (reading for leisure)
        catalog.add(new Consumable("Novel", 15, 0, 0, 25));
        catalog.add(new Consumable("Comic Book", 8, 0, 0, 15));
        catalog.add(new Consumable("Cookbook", 20, 0, 0, 10));
        // Study materials — more expensive but boost skills when used at Computer
        catalog.add(new Consumable("Textbook", 50, 0, 0, 5));
        // Furniture available at the bookshop
        catalog.add(new Furniture("Computer", 500, 20));

        if (sim.getAge() >= GameConstants.ADULT_AGE) {
            catalog.add(new Consumable("Energy Drink", 25, 5, 30, 0));
        }

        return catalog;
    }
}
