package simcli.world.interactables;

import java.util.ArrayList;
import java.util.List;

import simcli.entities.actors.Sim;
import simcli.entities.items.Item;
import simcli.entities.items.Food;
import simcli.entities.items.Consumable;
import simcli.entities.items.Furniture;
import simcli.entities.actors.Job;

public class GroceryShelf extends AbstractShop {

    public GroceryShelf() {
        super("Store Register");
    }

    @Override
    protected List<Item> getCatalog(Sim sim) {
        List<Item> catalog = new ArrayList<>();
        catalog.add(new Food("Apple", 10, 15, 5));
        catalog.add(new Food("Steak", 40, 50, 20));
        catalog.add(new Consumable("Perfume", 50, 0, 0, 30));
        catalog.add(new Furniture("Bed", 200, 30));
        catalog.add(new Furniture("Shower", 150, 10));
        catalog.add(new Furniture("Storage Chest", 100, 10));

        if (sim.getMoney() >= 500 || sim.getAge() >= 25) {
            catalog.add(new Furniture("Fridge", 300, 25));
        }

        if (sim.getAge() >= 22) {
            catalog.add(new Furniture("Weight Bench", 150, 40));
        }

        boolean isEngineerOrRich = false;
        if (sim.getCareer() == Job.SOFTWARE_ENGINEER) {
            isEngineerOrRich = true;
        }
        if (sim.getMoney() >= 1000) {
            isEngineerOrRich = true;
        }

        if (isEngineerOrRich) {
            catalog.add(new Furniture("Computer", 500, 20));
        }

        return catalog;
    }
}