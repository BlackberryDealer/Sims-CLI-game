package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.entities.items.Food;
import simcli.entities.items.Item;
import simcli.entities.models.SimState;
import simcli.utils.GameConstants;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.Container;
import simcli.world.interactables.Fridge;
import simcli.world.interactables.Interactable;
import simcli.world.interactables.SmartFridge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The daily cost of running a household: food going off and the weekly bills.
 *
 * <p>Called by {@link GameLoop} at every day boundary.</p>
 * <ul>
 *     <li>Food in a fridge loses {@link GameConstants#FRIDGE_SPOIL_RATE}
 *         day of freshness per day (a Smart Fridge only every other day);
 *         food in a bag or storage chest loses
 *         {@link GameConstants#UNREFRIGERATED_SPOIL_RATE}. Food that runs
 *         out of freshness is thrown away.</li>
 *     <li>Every Monday the household pays the weekly bill of every home it
 *         owns, starting with whoever has the most money. If it can't
 *         cover the bills, everyone is unhappy.</li>
 * </ul>
 */
public class UpkeepManager {

    /** The city, for finding homes and their containers. May be {@code null}. */
    private final IWorldManager worldManager;

    /** Where spoiled food and bills are reported. */
    private final SimulationLogger logger;

    /**
     * Creates an UpkeepManager.
     *
     * @param worldManager the city map, or {@code null} for no homes (no bills,
     *                     and only food in bags spoils).
     * @param logger       the logger for reporting spoiled food and bills.
     */
    public UpkeepManager(IWorldManager worldManager, SimulationLogger logger) {
        this.worldManager = worldManager;
        this.logger = logger;
    }

    /**
     * Runs the upkeep for the day that has just begun.
     *
     * @param newDay    the day number that has just started.
     * @param household every Sim in the household.
     * @return {@code true} if weekly bills were due today and paid in full.
     */
    public boolean processDay(int newDay, List<Sim> household) {
        spoilFood(newDay, household);
        if (TimeManager.dayOfWeekName(newDay).equals("Monday")) {
            return payBills(household);
        }
        return false;
    }

    /**
     * Returns the total weekly bill for every home the household owns.
     *
     * @return the amount due each Monday.
     */
    public int getWeeklyBills() {
        int total = 0;
        for (Residential home : ownedHomes()) {
            total += home.getWeeklyBill();
        }
        return total;
    }

    /**
     * Ages all of the household's food by a day and throws out anything that
     * has gone off. A Smart Fridge only ages its food every other day.
     */
    private void spoilFood(int day, List<Sim> household) {
        for (Residential home : ownedHomes()) {
            for (Room room : home.getRooms()) {
                for (Interactable object : room.getInteractables()) {
                    if (object instanceof Container) {
                        int rate = object instanceof Fridge
                                ? GameConstants.FRIDGE_SPOIL_RATE : GameConstants.UNREFRIGERATED_SPOIL_RATE;
                        if (object instanceof SmartFridge && day % 2 == 1) {
                            rate = 0;
                        }
                        spoil(((Container) object).getStoredItems(), rate,
                                "in the " + object.getObjectName().toLowerCase());
                    }
                }
            }
        }
        for (Sim sim : household) {
            if (sim.getState() != SimState.DEAD) {
                spoil(sim.getInventory(), GameConstants.UNREFRIGERATED_SPOIL_RATE,
                        "in " + sim.getName() + "'s bag");
            }
        }
    }

    private void spoil(List<Item> items, int days, String where) {
        Iterator<Item> it = items.iterator();
        while (it.hasNext()) {
            Item item = it.next();
            if (item instanceof Food && ((Food) item).spoil(days)) {
                it.remove();
                logger.logWarning("The " + item.getObjectName() + " " + where + " went off and was thrown out.");
            }
        }
    }

    /**
     * Charges the weekly bills, taking money from the richest living Sims
     * first. If the household can't afford them, whatever money it has is
     * taken and everyone loses happiness.
     *
     * @return {@code true} if the bills were paid in full.
     */
    private boolean payBills(List<Sim> household) {
        int due = getWeeklyBills();
        if (due <= 0) {
            return false;
        }
        List<Sim> payers = new ArrayList<>();
        for (Sim sim : household) {
            if (sim.getState() != SimState.DEAD) {
                payers.add(sim);
            }
        }
        payers.sort((a, b) -> b.getMoney() - a.getMoney());

        int remaining = due;
        for (Sim payer : payers) {
            int share = Math.min(remaining, payer.getMoney());
            payer.setMoney(payer.getMoney() - share);
            remaining -= share;
            if (remaining == 0) {
                break;
            }
        }

        if (remaining == 0) {
            logger.log("The household paid $" + due + " in weekly bills (rent and utilities).");
            return true;
        }
        logger.logWarning("The household couldn't pay the weekly bills! $" + remaining
                + " of $" + due + " is unpaid, and everyone is stressed about money.");
        for (Sim sim : payers) {
            sim.getHappiness().decrease(GameConstants.UNPAID_BILLS_HAPPINESS_PENALTY);
        }
        return false;
    }

    private List<Residential> ownedHomes() {
        List<Residential> homes = new ArrayList<>();
        if (worldManager == null) {
            return homes;
        }
        for (Building building : worldManager.getCityMap()) {
            if (building instanceof Residential && ((Residential) building).isOwned()) {
                homes.add((Residential) building);
            }
        }
        return homes;
    }
}
