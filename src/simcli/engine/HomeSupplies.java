package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.entities.items.Consumable;
import simcli.entities.items.Ingredient;
import simcli.entities.items.Item;
import simcli.utils.GameConstants;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.Fridge;
import simcli.world.interactables.Interactable;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds things in the household's homes: furniture of a given kind, and
 * food in the fridges. Also feeds babies from those fridges.
 *
 * <p>Idle Sims are treated as being at home, so these look through every
 * residential building the household owns.</p>
 */
public final class HomeSupplies {

    private HomeSupplies() {
    }

    /**
     * Finds every object of the given kind in the household's owned homes.
     *
     * @param worldManager the city, or {@code null} (finds nothing).
     * @param kind         the class of object to look for (subclasses count).
     * @param <T>          the object type.
     * @return the objects found.
     */
    public static <T extends Interactable> List<T> find(IWorldManager worldManager, Class<T> kind) {
        List<T> found = new ArrayList<>();
        if (worldManager == null) {
            return found;
        }
        for (Building building : worldManager.getCityMap()) {
            if (building instanceof Residential && ((Residential) building).isOwned()) {
                for (Room room : ((Residential) building).getRooms()) {
                    for (Interactable object : room.getInteractables()) {
                        if (kind.isInstance(object)) {
                            found.add(kind.cast(object));
                        }
                    }
                }
            }
        }
        return found;
    }

    /**
     * Takes one piece of food out of a fridge at home. Raw ingredients are
     * judged by the meal they cook into; the caller cooks them
     * ({@link Ingredient#cook()}) before eating.
     *
     * @param worldManager the city, or {@code null} (finds nothing).
     * @param mostFilling  {@code true} for the most filling food (a hungry
     *                     adult), {@code false} for the least (a baby's
     *                     feed, saving the big meals).
     * @return the food taken, or {@code null} if the fridges are empty.
     */
    public static Consumable takeFood(IWorldManager worldManager, boolean mostFilling) {
        Fridge bestFridge = null;
        Consumable best = null;
        for (Fridge fridge : find(worldManager, Fridge.class)) {
            for (Item item : fridge.getStoredItems()) {
                if (!(item instanceof Consumable) || fillingness((Consumable) item) <= 0) {
                    continue;
                }
                Consumable food = (Consumable) item;
                boolean better = best == null || (mostFilling
                        ? fillingness(food) > fillingness(best)
                        : fillingness(food) < fillingness(best));
                if (better) {
                    best = food;
                    bestFridge = fridge;
                }
            }
        }
        if (best == null) {
            return null;
        }
        bestFridge.getStoredItems().remove(best);
        return best;
    }

    /**
     * Returns how many fridge items at home are food.
     *
     * @param worldManager the city, or {@code null}.
     * @return the number of food items in home fridges.
     */
    public static int foodCount(IWorldManager worldManager) {
        int count = 0;
        for (Fridge fridge : find(worldManager, Fridge.class)) {
            for (Item item : fridge.getStoredItems()) {
                if (item instanceof Consumable && fillingness((Consumable) item) > 0) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Returns how much hunger the food restores, counting ingredients as
     * the meal they cook into.
     *
     * @param food the food.
     * @return the hunger it restores once ready to eat.
     */
    public static int fillingness(Consumable food) {
        return food instanceof Ingredient ? ((Ingredient) food).getMealSatiation() : food.getSatiationValue();
    }

    /**
     * Has a household Sim feed a baby with the least filling food in the
     * fridge. Babies need real food: with an empty fridge, nothing happens.
     * Elders enjoy looking after the little ones, and gain social and fun.
     *
     * @param carer        the Sim doing the feeding.
     * @param baby         the baby.
     * @param worldManager the city, for the fridges.
     * @param logger       where the feeding is reported.
     * @return {@code true} if the baby was fed.
     */
    public static boolean feedBaby(Sim carer, Sim baby, IWorldManager worldManager, SimulationLogger logger) {
        Consumable food = takeFood(worldManager, false);
        if (food == null) {
            return false;
        }
        baby.getHunger().increase(GameConstants.BABY_FEED_AMOUNT);
        if (carer.getAge() >= GameConstants.ELDER_AGE) {
            carer.getSocial().increase(GameConstants.GRANDPARENT_CARE_SOCIAL);
            carer.getHappiness().increase(GameConstants.GRANDPARENT_CARE_FUN);
            logger.log(carer.getName() + " lovingly feeds little " + baby.getName() + " some "
                    + food.getObjectName() + ".");
        } else {
            logger.log(carer.getName() + " feeds " + baby.getName() + " some " + food.getObjectName() + ".");
        }
        return true;
    }
}
