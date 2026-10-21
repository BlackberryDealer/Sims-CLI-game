package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.entities.items.Consumable;
import simcli.entities.items.Food;
import simcli.entities.items.Ingredient;
import simcli.entities.items.Item;
import simcli.entities.models.ActionState;
import simcli.entities.models.SimState;
import simcli.utils.GameConstants;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.Fridge;
import simcli.world.interactables.Interactable;
import simcli.world.interactables.Shower;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Lets household Sims the player isn't controlling look after themselves.
 *
 * <p>Every simulated hour, each idle household member:</p>
 * <ul>
 *     <li>eats the most filling food in a fridge at home when hungry,</li>
 *     <li>showers when their hygiene is very low,</li>
 *     <li>goes to bed when exhausted — or when the active Sim does — and
 *         gets up again once rested,</li>
 *     <li>feeds any hungry baby in the household.</li>
 * </ul>
 *
 * <p>Idle Sims are treated as being at home, so they can use the fridges
 * and showers of every residential building the household owns.</p>
 *
 * <p>Owned by {@link GameLoop}, which calls {@link #startTurn(Sim, List)}
 * once per player turn and {@link #act(Sim, List, boolean)} before each
 * hour of that turn.</p>
 */
public class AutonomyManager {

    /** The city, for finding fridges and showers. May be {@code null} (e.g. in tests). */
    private final IWorldManager worldManager;

    /** Where autonomous actions are reported. */
    private final SimulationLogger logger;

    /** Sims already told about an empty fridge this turn, so the warning isn't repeated hourly. */
    private final Set<Sim> warnedNoFood = Collections.newSetFromMap(new IdentityHashMap<>());

    /**
     * Creates an AutonomyManager.
     *
     * @param worldManager the city map, or {@code null} if there is no world
     *                     (Sims will still sleep but can't eat or shower).
     * @param logger       the logger for reporting what idle Sims do.
     */
    public AutonomyManager(IWorldManager worldManager, SimulationLogger logger) {
        this.worldManager = worldManager;
        this.logger = logger;
    }

    /**
     * Prepares the household for a new player turn.
     *
     * <p>Any action an idle Sim was left doing while the player last
     * controlled them is cleared, except sleep, which they keep until
     * rested. If the active Sim is going to bed, everyone else does too.</p>
     *
     * @param activePlayer the Sim the player controls.
     * @param household    every Sim in the household.
     */
    public void startTurn(Sim activePlayer, List<Sim> household) {
        warnedNoFood.clear();
        boolean bedtime = activePlayer.getCurrentAction() == ActionState.SLEEPING;
        for (Sim sim : household) {
            if (sim == activePlayer) {
                continue;
            }
            if (bedtime && canAct(sim)) {
                if (sim.getCurrentAction() != ActionState.SLEEPING) {
                    sim.setCurrentAction(ActionState.SLEEPING);
                    logger.log(sim.getName() + " heads to bed too.");
                }
            } else if (sim.getCurrentAction() != ActionState.SLEEPING) {
                sim.setCurrentAction(ActionState.IDLE);
            }
        }
    }

    /**
     * Lets every idle household Sim act for one hour.
     *
     * @param activePlayer the Sim the player controls (never acts on its own).
     * @param household    every Sim in the household.
     * @param bedtime      {@code true} while the active Sim is asleep, so
     *                     the rest of the household stays in bed too.
     */
    public void act(Sim activePlayer, List<Sim> household, boolean bedtime) {
        for (Sim sim : household) {
            if (sim == activePlayer || !canAct(sim)) {
                continue;
            }
            if (sim.getHunger().getValue() <= GameConstants.AUTONOMY_HUNGER_THRESHOLD) {
                eat(sim);
            }
            boolean asleep = sim.getCurrentAction() == ActionState.SLEEPING;
            if (asleep) {
                if (!bedtime && sim.getEnergy().getValue() >= GameConstants.AUTONOMY_WAKE_THRESHOLD) {
                    sim.setCurrentAction(ActionState.IDLE);
                    logger.log(sim.getName() + " wakes up feeling rested.");
                }
            } else if (bedtime || sim.getEnergy().getValue() <= GameConstants.AUTONOMY_SLEEP_THRESHOLD) {
                sim.setCurrentAction(ActionState.SLEEPING);
                logger.log(sim.getName() + " is exhausted and goes to bed.");
            } else if (sim.getHygiene().getValue() <= GameConstants.AUTONOMY_HYGIENE_THRESHOLD) {
                shower(sim);
            }
        }
        feedBabies(activePlayer, household);
    }

    /** Only living, playable Sims look after themselves; babies can't. */
    private static boolean canAct(Sim sim) {
        return sim.getState() != SimState.DEAD && sim.isPlayable();
    }

    /**
     * Eats the most filling food found in any fridge at home, cooking raw
     * ingredients first.
     */
    private void eat(Sim sim) {
        Fridge bestFridge = null;
        Consumable bestFood = null;
        for (Fridge fridge : homeObjects(Fridge.class)) {
            for (Item item : fridge.getStoredItems()) {
                if (item instanceof Consumable) {
                    Consumable food = (Consumable) item;
                    if (fillingness(food) > 0 && (bestFood == null || fillingness(food) > fillingness(bestFood))) {
                        bestFood = food;
                        bestFridge = fridge;
                    }
                }
            }
        }
        if (bestFood == null) {
            if (warnedNoFood.add(sim)) {
                logger.logWarning(sim.getName() + " is hungry, but there's nothing to eat in the fridge!");
            }
            return;
        }
        bestFridge.getStoredItems().remove(bestFood);
        if (bestFood instanceof Ingredient) {
            Food meal = ((Ingredient) bestFood).cook();
            sim.getHappiness().increase(GameConstants.COOKING_HAPPINESS);
            sim.eat(meal);
            logger.log(sim.getName() + " cooks a " + meal.getObjectName() + " and eats it.");
        } else {
            sim.eat(bestFood);
            logger.log(sim.getName() + " grabs a " + bestFood.getObjectName() + " from the fridge.");
        }
    }

    /** How much hunger the food restores, counting ingredients as the meal they cook into. */
    private static int fillingness(Consumable food) {
        return food instanceof Ingredient ? ((Ingredient) food).getMealSatiation() : food.getSatiationValue();
    }

    /** Takes a shower if there is one at home. */
    private void shower(Sim sim) {
        if (homeObjects(Shower.class).isEmpty()) {
            return;
        }
        sim.getHygiene().increase(50);
        logger.log(sim.getName() + " takes a shower.");
    }

    /** Has the first available household Sim feed every hungry baby. */
    private void feedBabies(Sim activePlayer, List<Sim> household) {
        Sim carer = null;
        for (Sim sim : household) {
            if (sim != activePlayer && canAct(sim)) {
                carer = sim;
                break;
            }
        }
        if (carer == null) {
            return;
        }
        for (Sim baby : household) {
            if (baby.isChildSim() && !baby.isPlayable() && baby.getState() != SimState.DEAD
                    && baby.getHunger().getValue() <= GameConstants.AUTONOMY_BABY_HUNGER_THRESHOLD) {
                baby.getHunger().increase(GameConstants.BABY_FEED_AMOUNT);
                logger.log(carer.getName() + " feeds " + baby.getName() + ".");
            }
        }
    }

    /** Finds every object of the given kind in the household's owned homes. */
    private <T extends Interactable> List<T> homeObjects(Class<T> kind) {
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
}
