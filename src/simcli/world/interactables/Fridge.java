package simcli.world.interactables;

import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.entities.items.Item;
import simcli.entities.items.Food;
import simcli.entities.items.Ingredient;
import simcli.engine.ActionCancelledException;
import simcli.engine.SimulationException;
import simcli.ui.ItemInfo;
import simcli.ui.Style;
import simcli.ui.UIManager;
import simcli.utils.GameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A kitchen interactable that restores hunger when used.
 * Sims can store food here (where it stays fresh longer), eat it later,
 * and cook raw {@link Ingredient}s into meals.
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
        boolean didSomething = false;
        while (open) {
            UIManager.clearScreen();
            UIManager.title("Fridge  " + storedFood.size() + (storedFood.size() == 1 ? " item" : " items"));
            if (storedFood.isEmpty()) {
                UIManager.info("The fridge is completely empty! {k}Buy food at the Supermarket.{/}");
            } else {
                for (int i = 0; i < storedFood.size(); i++) {
                    Item food = storedFood.get(i);
                    UIManager.option(String.valueOf(i + 1), Style.fit("Eat " + food.getObjectName(), 18)
                            + ItemInfo.describe(food));
                }
            }
            System.out.println();
            boolean canCook = !ingredients().isEmpty();
            if (canCook) {
                UIManager.option("C", "Cook a meal", "turns raw ingredients into a filling meal");
            }
            UIManager.option("S", "Store food from your bag");
            UIManager.option("0", "Close the fridge");
            UIManager.prompt("\nFridge> ");

            String choice = scanner.nextLine().trim().toUpperCase();

            if (choice.equals("0") || choice.isEmpty()) {
                open = false;
            } else if (choice.equals("S")) {
                didSomething |= storeFromBag(sim, scanner);
            } else if (choice.equals("C") && canCook) {
                didSomething |= cook(sim, scanner, timeManager);
            } else {
                try {
                    int c = Integer.parseInt(choice);
                    if (c > 0 && c <= storedFood.size()) {
                        Item food = retrieveItem(c - 1);
                        food.interact(sim, scanner, timeManager);
                        didSomething = true;
                    } else {
                        UIManager.error("Invalid choice.");
                    }
                } catch (NumberFormatException e) {
                    UIManager.error("Invalid input.");
                }
                UIManager.prompt("\nPress ENTER to continue...");
                scanner.nextLine();
            }
        }
        if (!didSomething) {
            // just looked inside — no time passes
            throw new ActionCancelledException();
        }
    }

    /** The raw ingredients currently in the fridge. */
    private List<Ingredient> ingredients() {
        List<Ingredient> found = new ArrayList<>();
        for (Item item : storedFood) {
            if (item instanceof Ingredient) {
                found.add((Ingredient) item);
            }
        }
        return found;
    }

    /**
     * Lets the player cook one of the fridge's ingredients into a meal and
     * eat it straight away.
     *
     * @return {@code true} if something was cooked.
     */
    private boolean cook(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException {
        List<Ingredient> raw = ingredients();
        UIManager.title("Cook what?");
        for (int i = 0; i < raw.size(); i++) {
            Ingredient ing = raw.get(i);
            UIManager.option(String.valueOf(i + 1), Style.fit(ing.getMealName(), 22)
                    + "{k}from " + ing.getObjectName() + "{/}  {G}+" + ing.getMealSatiation() + " hunger{/}");
        }
        UIManager.option("0", "Cancel");
        UIManager.prompt("\nCook> ");
        boolean cooked = false;
        try {
            int c = Integer.parseInt(scanner.nextLine().trim());
            if (c > 0 && c <= raw.size()) {
                Ingredient ing = raw.get(c - 1);
                storedFood.remove(ing);
                Food meal = ing.cook();
                UIManager.success(sim.getName() + " cooks up a " + meal.getObjectName() + ".  {G}+"
                        + GameConstants.COOKING_HAPPINESS + " fun{/}");
                sim.getHappiness().increase(GameConstants.COOKING_HAPPINESS);
                meal.interact(sim, scanner, timeManager);
                cooked = true;
            } else if (c != 0) {
                UIManager.error("Invalid selection.");
            }
        } catch (NumberFormatException e) {
            UIManager.error("Invalid input.");
        }
        UIManager.prompt("\nPress ENTER to continue...");
        scanner.nextLine();
        return cooked;
    }

    /**
     * Lets the player move a food item from their bag into the fridge.
     *
     * @return {@code true} if something was stored.
     */
    private boolean storeFromBag(Sim sim, Scanner scanner) {
        boolean stored = false;
        List<Item> inv = sim.getInventory();
        List<Item> foods = new ArrayList<>();
        for (Item item : inv) {
            if (item instanceof Food) {
                foods.add(item);
            }
        }
        if (foods.isEmpty()) {
            UIManager.info("No food in your bag!");
        } else {
            UIManager.title("Store which food?");
            for (int i = 0; i < foods.size(); i++) {
                UIManager.option(String.valueOf(i + 1), Style.fit(foods.get(i).getObjectName(), 18)
                        + ItemInfo.describe(foods.get(i)));
            }
            UIManager.option("0", "Cancel");
            UIManager.prompt("\nStore> ");
            try {
                int st = Integer.parseInt(scanner.nextLine().trim());
                if (st > 0 && st <= foods.size()) {
                    Item item = foods.get(st - 1);
                    inv.remove(item);
                    storeItem(item);
                    stored = true;
                    UIManager.success("Stored " + item.getObjectName() + " in the fridge.");
                } else if (st != 0) {
                    UIManager.error("Invalid selection.");
                }
            } catch (NumberFormatException e) {
                UIManager.error("Invalid input.");
            }
        }
        UIManager.prompt("\nPress ENTER to return...");
        scanner.nextLine();
        return stored;
    }

    @Override
    public String getObjectName() {
        return "Fridge";
    }
}
