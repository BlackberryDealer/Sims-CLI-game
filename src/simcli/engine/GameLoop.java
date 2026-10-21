package simcli.engine;

import simcli.entities.actors.Sim;
import simcli.entities.managers.NPCManager;
import simcli.entities.models.ActionState;
import simcli.entities.models.SimState;
import simcli.entities.models.SkillType;
import simcli.utils.GameConstants;

import java.util.List;

/**
 * GameLoop — orchestrates per-tick and per-day processing for the simulation.
 *
 * <p>Each call to {@link #processTick(Sim, int)} advances one simulation tick:
 * decaying needs for all Sims, advancing the clock, triggering random events,
 * and — when a new day begins — delegating lifecycle processing to the
 * {@link LifecycleManager}.</p>
 *
 * <h2>Ownership</h2>
 * <p>This class owns the creation of {@link RandomEventManager},
 * {@link LifecycleManager}, {@link AutonomyManager}, {@link UpkeepManager}
 * and {@link GoalManager}, keeping subsystem factories close to their
 * consumers and reducing the construction burden on {@link GameEngine}.</p>
 */
public class GameLoop {
    private final TimeManager timeManager;
    private final List<Sim> neighborhood;
    private final IWorldManager worldManager;
    private final NPCManager npcManager;
    private final SimulationLogger logger;
    private final RandomEventManager eventManager;
    private final LifecycleManager lifecycleManager;
    private final AutonomyManager autonomyManager;
    private final UpkeepManager upkeepManager;
    private final GoalManager goalManager;

    /**
     * Creates a new GameLoop with no world: idle Sims still sleep on their
     * own, but have no fridge or shower to use, and there are no bills.
     *
     * @param timeManager  tracks tick/day/time-of-day progression.
     * @param neighborhood every Sim in the current game world.
     * @param logger       the simulation logger for event and lifecycle messages.
     */
    public GameLoop(TimeManager timeManager, List<Sim> neighborhood,
                    SimulationLogger logger) {
        this(timeManager, neighborhood, logger, null, null);
    }

    /**
     * Creates a new GameLoop with a world but no park NPCs to manage.
     *
     * @param timeManager  tracks tick/day/time-of-day progression.
     * @param neighborhood every Sim in the current game world.
     * @param logger       the simulation logger for event and lifecycle messages.
     * @param worldManager the city, where idle Sims find food and showers
     *                     (may be {@code null}).
     */
    public GameLoop(TimeManager timeManager, List<Sim> neighborhood,
                    SimulationLogger logger, IWorldManager worldManager) {
        this(timeManager, neighborhood, logger, worldManager, null);
    }

    /**
     * Creates a new GameLoop, instantiating its own event, lifecycle,
     * autonomy, upkeep and goal managers.
     *
     * @param timeManager  tracks tick/day/time-of-day progression.
     * @param neighborhood every Sim in the current game world.
     * @param logger       the simulation logger for event and lifecycle messages.
     * @param worldManager the city, where idle Sims find food and showers and
     *                     whose homes cost bills (may be {@code null}).
     * @param npcManager   the townsfolk, who rotate through the park each day
     *                     (may be {@code null}).
     */
    public GameLoop(TimeManager timeManager, List<Sim> neighborhood,
                    SimulationLogger logger, IWorldManager worldManager, NPCManager npcManager) {
        this.timeManager = timeManager;
        this.neighborhood = neighborhood;
        this.worldManager = worldManager;
        this.npcManager = npcManager;
        this.logger = logger;
        this.eventManager = new RandomEventManager(logger);
        this.lifecycleManager = new LifecycleManager(
                GameConstants.DAYS_PER_AGE_TICK, logger);
        this.autonomyManager = new AutonomyManager(worldManager, logger);
        this.upkeepManager = new UpkeepManager(worldManager, logger);
        this.goalManager = new GoalManager(worldManager, logger);
    }

    /**
     * Processes one player turn, advancing the clock by {@code ticksToAdvance}
     * hours.
     *
     * <p>Each hour is simulated individually:</p>
     * <ol>
     *     <li>Lets idle household Sims look after themselves via the
     *         {@link AutonomyManager}.</li>
     *     <li>Decays needs for every Sim via {@link Sim#tick()}.</li>
     *     <li>Advances the simulation clock by one tick.</li>
     *     <li>If a new day has started, delegates to
     *         {@link #processDayBoundary(int)}.</li>
     * </ol>
     * <p>Once per turn, after the clock has moved, a random event is rolled
     * for the active Sim, another for the whole household, and the
     * household's goals are checked.</p>
     *
     * <p>Sims other than the active player are reset to
     * {@link ActionState#IDLE} first (unless asleep), so an action they
     * performed while they were last controlled does not keep affecting
     * their need decay forever. If the active player is asleep, the whole
     * household sleeps through the turn with them.</p>
     *
     * @param activePlayer   the currently controlled Sim (passed to the event
     *                       manager for targeted events).
     * @param ticksToAdvance the number of hours this turn takes.
     */
    public void processTick(Sim activePlayer, int ticksToAdvance) {
        autonomyManager.startTurn(activePlayer, neighborhood);
        boolean bedtime = activePlayer.getCurrentAction() == ActionState.SLEEPING;

        for (int i = 0; i < ticksToAdvance; i++) {
            int previousDay = timeManager.getCurrentDay();

            autonomyManager.act(activePlayer, neighborhood, bedtime);

            for (Sim sim : neighborhood) {
                sim.tick();
            }
            timeManager.advanceTick();

            if (timeManager.getCurrentDay() != previousDay) {
                processDayBoundary(timeManager.getCurrentDay());
            }
        }

        eventManager.trigger(activePlayer, timeManager);
        eventManager.triggerHouseholdEvent(neighborhood, timeManager, worldManager);
        goalManager.check(neighborhood);
    }

    /**
     * Processes a day boundary.
     *
     * <p>Prints a new-day message, runs truancy checks and school for the
     * day that just ended, then delegates lifecycle processing (aging,
     * death, retirement) to the {@link LifecycleManager}. Truancy must run
     * first because the lifecycle step resets each Sim's shifts-worked-today
     * counter. Finally, food ages and bills are charged
     * ({@link UpkeepManager}) and the park's visitors change.</p>
     *
     * @param dayNumber the day that has just begun.
     */
    private void processDayBoundary(int dayNumber) {
        simcli.ui.UIManager.printEvent(
                "*** A new day has begun! Day " + dayNumber + ", "
                + TimeManager.dayOfWeekName(dayNumber) + " ***");

        boolean endedDayWasWorkday = !TimeManager.isWeekend(dayNumber - 1);
        for (Sim s : neighborhood) {
            if (s.getState() != SimState.DEAD) {
                s.getCareerManager().checkTruancy(s.getName(), endedDayWasWorkday);
            }
        }
        if (endedDayWasWorkday) {
            attendSchool();
        }

        // Delegate aging/death/retirement to LifecycleManager (SRP).
        lifecycleManager.processDayForAll(neighborhood);

        upkeepManager.processDay(dayNumber, neighborhood);
        if (npcManager != null) {
            npcManager.startNewDay(dayNumber, logger);
        }
    }

    /** School-age Sims spent the weekday at school, which trains their Logic. */
    private void attendSchool() {
        for (Sim s : neighborhood) {
            if (s.getState() != SimState.DEAD && s.getLifeStage() != null && s.getLifeStage().attendsSchool()) {
                s.trainSkill(SkillType.LOGIC, GameConstants.SCHOOL_LOGIC_XP);
                logger.log(s.getName() + " went to school. (+Logic)");
            }
        }
    }

    /** Returns the event manager owned by this loop. */
    public RandomEventManager getRandomEventManager() {
        return eventManager;
    }

    /** Returns the lifecycle manager owned by this loop. */
    public LifecycleManager getLifecycleManager() {
        return lifecycleManager;
    }

    /** Returns the upkeep (bills and spoilage) manager owned by this loop. */
    public UpkeepManager getUpkeepManager() {
        return upkeepManager;
    }

    /** Returns the goal (aspirations and achievements) manager owned by this loop. */
    public GoalManager getGoalManager() {
        return goalManager;
    }
}
