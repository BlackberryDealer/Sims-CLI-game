package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.entities.items.Consumable;
import simcli.entities.items.Food;
import simcli.entities.items.Ingredient;
import simcli.entities.models.ActionState;
import simcli.entities.models.SimState;
import simcli.utils.GameConstants;
import simcli.world.interactables.Shower;

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
 *     <li>chats with another idle Sim at home when lonely,</li>
 *     <li>feeds any hungry baby in the household with food from the
 *         fridge (grandparents first).</li>
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
            } else if (sim.getSocial().getValue() <= GameConstants.AUTONOMY_SOCIAL_THRESHOLD) {
                chatAtHome(sim, activePlayer, household);
            }
        }
        feedBabies(activePlayer, household);
    }

    /** A lonely idle Sim has a chat with another idle, awake Sim at home. */
    private void chatAtHome(Sim sim, Sim activePlayer, List<Sim> household) {
        for (Sim other : household) {
            if (other != sim && other != activePlayer && canAct(other)
                    && other.getCurrentAction() != ActionState.SLEEPING) {
                sim.getSocial().increase(GameConstants.HOME_CHAT_SOCIAL);
                other.getSocial().increase(GameConstants.HOME_CHAT_SOCIAL);
                sim.getRelationshipManager().increaseRelationship(other, GameConstants.HOME_CHAT_RELATIONSHIP);
                other.getRelationshipManager().increaseRelationship(sim, GameConstants.HOME_CHAT_RELATIONSHIP);
                logger.log(sim.getName() + " and " + other.getName() + " catch up over a cup of tea.");
                return;
            }
        }
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
        Consumable food = HomeSupplies.takeFood(worldManager, true);
        if (food == null) {
            if (warnedNoFood.add(sim)) {
                logger.logWarning(sim.getName() + " is hungry, but there's nothing to eat in the fridge!");
            }
            return;
        }
        if (food instanceof Ingredient) {
            Food meal = ((Ingredient) food).cook();
            sim.getHappiness().increase(GameConstants.COOKING_HAPPINESS);
            sim.recordMealCooked();
            sim.eat(meal);
            logger.log(sim.getName() + " cooks a " + meal.getObjectName() + " and eats it.");
        } else {
            sim.eat(food);
            logger.log(sim.getName() + " grabs a " + food.getObjectName() + " from the fridge.");
        }
    }

    /** Takes a shower if there is one at home. */
    private void shower(Sim sim) {
        if (HomeSupplies.find(worldManager, Shower.class).isEmpty()) {
            return;
        }
        sim.getHygiene().increase(50);
        logger.log(sim.getName() + " takes a shower.");
    }

    /**
     * Has an idle household Sim feed every hungry baby, from the fridge.
     * Grandparents step in first, since they enjoy it most.
     */
    private void feedBabies(Sim activePlayer, List<Sim> household) {
        Sim carer = null;
        for (Sim sim : household) {
            if (sim != activePlayer && canAct(sim)
                    && (carer == null || (sim.getAge() >= GameConstants.ELDER_AGE
                            && carer.getAge() < GameConstants.ELDER_AGE))) {
                carer = sim;
            }
        }
        if (carer == null) {
            return;
        }
        for (Sim baby : household) {
            if (baby.isChildSim() && !baby.isPlayable() && baby.getState() != SimState.DEAD
                    && baby.getHunger().getValue() <= GameConstants.AUTONOMY_BABY_HUNGER_THRESHOLD
                    && !HomeSupplies.feedBaby(carer, baby, worldManager, logger)
                    && warnedNoFood.add(baby)) {
                logger.logWarning(baby.getName() + " is hungry, but there's no food in the fridge to feed them!");
            }
        }
    }
}
