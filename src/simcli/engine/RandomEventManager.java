package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.entities.items.Food;
import simcli.entities.items.Item;
import simcli.entities.models.ActionState;
import simcli.entities.models.SimState;
import simcli.utils.GameConstants;
import simcli.utils.GameRandom;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.Fridge;
import simcli.world.interactables.Interactable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Fires random events each tick with a small probability.
 *
 * <p>Every tick, {@link #trigger(Sim, TimeManager)} is called by
 * {@link GameLoop}. There is a base 5&nbsp;% chance that an event fires;
 * the specific event is then chosen from a weighted distribution:</p>
 * <ul>
 *     <li><b>Found money</b> (20&nbsp;%) — +$50</li>
 *     <li><b>Stubbed toe</b> (15&nbsp;%) — happiness −15</li>
 *     <li><b>Stroke of inspiration</b> (15&nbsp;%) — energy +20</li>
 *     <li><b>Mild cold</b> (15&nbsp;%) — all needs −10</li>
 *     <li><b>Stove fire</b> (15&nbsp;%) — hunger −20, energy −25, health −10</li>
 *     <li><b>Lottery win</b> (20&nbsp;%) — +$100–500, happiness +30</li>
 * </ul>
 *
 * <p>Dead Sims are exempt from random events.</p>
 *
 * <p>{@link #triggerHouseholdEvent(List, TimeManager, IWorldManager)} rolls
 * separately ({@link GameConstants#HOUSEHOLD_EVENT_CHANCE}&nbsp;% per turn)
 * for an event that hits the whole household, chosen from those that fit
 * the time of day:</p>
 * <ul>
 *     <li><b>Sunny morning</b> (06:00–11:00) — happiness +10</li>
 *     <li><b>Neighbour's pie</b> (12:00–17:00) — social +10, a pie in the fridge</li>
 *     <li><b>Weekend party</b> (Sat/Sun 18:00–23:00) — social +25, happiness +15, energy −10 for everyone awake</li>
 *     <li><b>Power cut</b> (21:00–05:00) — hygiene −10, fridge food loses 2 days of freshness</li>
 *     <li><b>Flu</b> (any time) — energy −15, happiness −10</li>
 * </ul>
 */
public class RandomEventManager {
    private final SimulationLogger logger;

    /**
     * Creates a new {@code RandomEventManager}.
     *
     * @param logger the simulation logger for event messages.
     */
    public RandomEventManager(SimulationLogger logger) {
        this.logger = logger;
    }

    /**
     * Rolls for a random event and applies its effects to the active player.
     *
     * @param activePlayer the currently controlled Sim (may be null-safe for dead Sims).
     * @param timeManager  the simulation clock (available for future time-based events).
     */
    public void trigger(Sim activePlayer, TimeManager timeManager) {
        if (activePlayer.getState() == SimState.DEAD) return;

        double chance = GameRandom.RANDOM.nextDouble();
        if (chance < 0.05) { // 5% chance per tick to trigger an event
            double eventRoll = GameRandom.RANDOM.nextDouble();
            if (eventRoll < 0.20) {
                logger.log("\n*** RANDOM EVENT! You found $50 on the ground! ***");
                activePlayer.setMoney(activePlayer.getMoney() + 50);
                activePlayer.addTotalMoneyEarned(50);
            } else if (eventRoll < 0.35) {
                logger.log("\n*** RANDOM EVENT! Oh no, you stubbed your toe! Happiness decreased. ***");
                activePlayer.getHappiness().decrease(15);
            } else if (eventRoll < 0.50) {
                logger.log("\n*** RANDOM EVENT! A sudden stroke of inspiration! Energy restored. ***");
                activePlayer.getEnergy().increase(20);
            } else if (eventRoll < 0.65) {
                logger.log("\n*** RANDOM EVENT! You got a mild cold. All needs suffer slightly. ***");
                activePlayer.getHunger().decrease(10);
                activePlayer.getEnergy().decrease(10);
                activePlayer.getHygiene().decrease(10);
                activePlayer.getHappiness().decrease(10);
                activePlayer.getSocial().decrease(10);
            } else if (eventRoll < 0.80) {
                // Stove catches fire (from proposal slide 24)
                logger.log("\n*** RANDOM EVENT! The stove catches fire! ***");
                logger.log("You rush to put it out, burning yourself in the process.");
                activePlayer.getHunger().decrease(20);
                activePlayer.getEnergy().decrease(25);
                activePlayer.setHealth(Math.max(0, activePlayer.getHealth() - 10));
            } else {
                // Lottery win (from proposal slide 24)
                int winnings = 100 + simcli.utils.GameRandom.RANDOM.nextInt(401); // $100-$500
                logger.log("\n*** RANDOM EVENT! LOTTERY WIN! You won $" + winnings + "! ***");
                activePlayer.setMoney(activePlayer.getMoney() + winnings);
                activePlayer.addTotalMoneyEarned(winnings);
                activePlayer.getHappiness().increase(30);
            }
        }
    }

    /** The household-wide events, each tied to a time of day. */
    enum HouseholdEvent { SUNNY_MORNING, NEIGHBOUR_PIE, WEEKEND_PARTY, POWER_CUT, FLU }

    /**
     * Rolls for an event that affects the whole household, chosen from the
     * events that fit the current time of day.
     *
     * @param household    every Sim in the household (babies and the dead are left out).
     * @param timeManager  the simulation clock.
     * @param worldManager the city, for the fridges a power cut or a pie affects (may be {@code null}).
     */
    public void triggerHouseholdEvent(List<Sim> household, TimeManager timeManager, IWorldManager worldManager) {
        if (GameRandom.RANDOM.nextInt(100) >= GameConstants.HOUSEHOLD_EVENT_CHANCE) {
            return;
        }
        List<HouseholdEvent> possible = eventsAt(timeManager);
        fireHouseholdEvent(possible.get(GameRandom.RANDOM.nextInt(possible.size())),
                household, worldManager);
    }

    /** The household events that can happen at the current time. */
    static List<HouseholdEvent> eventsAt(TimeManager timeManager) {
        int hour = timeManager.getCurrentTick() % GameConstants.TICKS_PER_DAY;
        List<HouseholdEvent> possible = new ArrayList<>();
        possible.add(HouseholdEvent.FLU);
        if (hour >= 6 && hour < 12) {
            possible.add(HouseholdEvent.SUNNY_MORNING);
        }
        if (hour >= 12 && hour < 18) {
            possible.add(HouseholdEvent.NEIGHBOUR_PIE);
        }
        if (hour >= 18 && TimeManager.isWeekend(timeManager.getCurrentDay())) {
            possible.add(HouseholdEvent.WEEKEND_PARTY);
        }
        if (hour >= 21 || hour < 6) {
            possible.add(HouseholdEvent.POWER_CUT);
        }
        return possible;
    }

    void fireHouseholdEvent(HouseholdEvent event, List<Sim> household, IWorldManager worldManager) {
        List<Sim> members = new ArrayList<>();
        for (Sim sim : household) {
            if (sim.getState() != SimState.DEAD && sim.isPlayable()) {
                members.add(sim);
            }
        }
        if (members.isEmpty()) {
            return;
        }
        switch (event) {
            case SUNNY_MORNING:
                logger.log("\n*** HOUSEHOLD EVENT! It's a beautiful sunny morning. Everyone's in a good mood. ***");
                for (Sim sim : members) {
                    sim.getHappiness().increase(10);
                }
                break;
            case NEIGHBOUR_PIE:
                logger.log("\n*** HOUSEHOLD EVENT! A neighbour drops by with a homemade pie! ***");
                for (Sim sim : members) {
                    sim.getSocial().increase(10);
                }
                List<Fridge> fridges = homeFridges(worldManager);
                if (!fridges.isEmpty()) {
                    fridges.get(0).storeItem(new Food("Homemade Pie", 0, 45, 5, GameConstants.MEAL_SHELF_LIFE_DAYS));
                    logger.log("The pie goes in the fridge.");
                }
                break;
            case WEEKEND_PARTY:
                logger.log("\n*** HOUSEHOLD EVENT! The neighbours are throwing a weekend party, and everyone's invited! ***");
                for (Sim sim : members) {
                    if (sim.getCurrentAction() != ActionState.SLEEPING) {
                        sim.getSocial().increase(25);
                        sim.getHappiness().increase(15);
                        sim.getEnergy().decrease(10);
                    }
                }
                break;
            case POWER_CUT:
                logger.log("\n*** HOUSEHOLD EVENT! A storm knocks out the power overnight. No hot water, and the fridge warms up. ***");
                for (Sim sim : members) {
                    sim.getHygiene().decrease(10);
                }
                for (Fridge fridge : homeFridges(worldManager)) {
                    Iterator<Item> it = fridge.getStoredItems().iterator();
                    while (it.hasNext()) {
                        Item item = it.next();
                        if (item instanceof Food && ((Food) item).spoil(2)) {
                            it.remove();
                            logger.logWarning("The " + item.getObjectName() + " in the fridge spoiled in the power cut.");
                        }
                    }
                }
                break;
            case FLU:
                logger.log("\n*** HOUSEHOLD EVENT! A flu is going round the house. Everyone feels rotten. ***");
                for (Sim sim : members) {
                    sim.getEnergy().decrease(15);
                    sim.getHappiness().decrease(10);
                }
                break;
        }
    }

    private static List<Fridge> homeFridges(IWorldManager worldManager) {
        List<Fridge> fridges = new ArrayList<>();
        if (worldManager == null) {
            return fridges;
        }
        for (Building building : worldManager.getCityMap()) {
            if (building instanceof Residential && ((Residential) building).isOwned()) {
                for (Room room : ((Residential) building).getRooms()) {
                    for (Interactable object : room.getInteractables()) {
                        if (object instanceof Fridge) {
                            fridges.add((Fridge) object);
                        }
                    }
                }
            }
        }
        return fridges;
    }
}
