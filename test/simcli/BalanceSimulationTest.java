package simcli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.engine.GameLoop;
import simcli.engine.GoalManager;
import simcli.engine.HomeSupplies;
import simcli.engine.SimulationLogger;
import simcli.engine.TimeManager;
import simcli.engine.WorldManager;
import simcli.entities.actors.Sim;
import simcli.entities.items.Consumable;
import simcli.entities.items.Food;
import simcli.entities.items.Ingredient;
import simcli.entities.items.Item;
import simcli.entities.managers.NPCManager;
import simcli.entities.models.ActionState;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.entities.models.Memory;
import simcli.entities.models.Reward;
import simcli.entities.models.SimState;
import simcli.entities.models.SkillType;
import simcli.entities.models.SocialAction;
import simcli.entities.models.StarterGoal;
import simcli.entities.models.Trait;
import simcli.persistence.GameContent;
import simcli.utils.GameConstants;
import simcli.utils.GameRandom;
import simcli.world.Residential;
import simcli.world.interactables.Computer;
import simcli.world.interactables.Fridge;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A headless balance check: a simple bot plays a two-Sim household for
 * {@value #DAYS} in-game days and reports how the run went — money over
 * time, the worst need levels, and the day each milestone was reached.
 *
 * <p>The bot plays sensibly but not cleverly: it works a shift every
 * weekday morning, buys and cooks groceries, keeps clean and sociable, goes
 * to bed at night, studies once it can afford a computer, and buys the
 * Bungalow when it has the money. Its partner is left to autonomy.</p>
 *
 * <p>The assertions are guard rails, not exact targets: they fail if a
 * change makes the game much easier or much harder than intended (for
 * example, a promotion on day 1, a maxed skill in a week, or a sensible
 * player starving).</p>
 */
@DisplayName("Balance — a bot plays " + BalanceSimulationTest.DAYS + " days")
public class BalanceSimulationTest {

    static final int DAYS = 90;

    private SimulationLogger logger;
    private TimeManager time;
    private WorldManager world;
    private GameLoop loop;
    private GoalManager goals;
    private Sim alex;
    private Sim sam;
    private List<Sim> household;

    /** Day each milestone was first reached. */
    private final Map<String, Integer> milestones = new LinkedHashMap<>();
    /** Alex's money at the end of selected days. */
    private final Map<Integer, Integer> moneyByDay = new LinkedHashMap<>();
    private int lowestHunger = 100;
    private int lowestEnergy = 100;
    private int lowestSocial = 100;
    private int shiftsWorked;
    private int lastShiftDay;
    private int pointsSpent;
    private int greatMoodTurns;
    private int turns;

    @BeforeEach
    void setUp() {
        GameRandom.RANDOM.setSeed(2024);
        logger = new SimulationLogger();
        SimulationLogger.setInstance(logger);
        alex = new Sim("Alex", 21, Gender.FEMALE, Job.SOFTWARE_ENGINEER);
        sam = new Sim("Sam", 21, Gender.MALE, Job.UNEMPLOYED);
        for (Sim s : List.of(alex, sam)) {
            s.getTraits().clear();
            s.getTraits().add(Trait.ACTIVE);
        }
        household = new ArrayList<>(List.of(alex, sam));
        world = new WorldManager(new NPCManager(), household);
        world.setupWorld();
        time = new TimeManager(GameConstants.MORNING_HOUR, GameConstants.TICKS_PER_DAY);
        loop = new GameLoop(time, household, logger, world, null);
        goals = loop.getGoalManager();
        alex.setCurrentRoom(((Residential) world.getCityMap().get(0)).getRooms().get(0));
    }

    @AfterEach
    void tearDown() {
        GameRandom.RANDOM.setSeed(System.nanoTime());
    }

    @Test
    @DisplayName("A sensible player thrives, and progress is paced over weeks, not days")
    void testPacing() {
        while (time.getCurrentDay() <= DAYS && alex.getState() != SimState.DEAD) {
            playOneTurn();
            observe();
            logger.reset();
        }

        printReport();

        assertNotEquals(SimState.DEAD, alex.getState(), "A sensible player shouldn't die");
        assertNotEquals(SimState.DEAD, sam.getState(), "An idle partner with a stocked fridge shouldn't die");
        assertTrue(lowestHunger > 0, "The bot never lets hunger hit zero");

        Integer firstPromotion = milestones.get("first promotion");
        assertNotNull(firstPromotion, "A full-time worker should be promoted within " + DAYS + " days");
        assertTrue(firstPromotion >= 4, "Promotions should take more than a few shifts (day " + firstPromotion + ")");
        assertTrue(firstPromotion <= 21, "The first promotion shouldn't take more than 3 weeks (day " + firstPromotion + ")");

        Integer logicMax = milestones.get("Logic level 10");
        assertTrue(logicMax == null || logicMax >= 20, "Maxing a skill should take weeks (day " + logicMax + ")");

        Integer bungalow = milestones.get("bought the Bungalow");
        assertNotNull(bungalow, "A steady earner should afford the Bungalow within " + DAYS + " days");
        assertTrue(bungalow >= 20, "The Bungalow should be a midgame goal, not a first-week one (day " + bungalow + ")");

        Integer firstPerk = milestones.get("first reward perk");
        assertNotNull(firstPerk, "Doing daily wants should earn a perk within " + DAYS + " days");
        assertTrue(firstPerk >= 3, "The first perk should take a few days of wants (day " + firstPerk + ")");
        assertFalse(alex.getMemories().isEmpty(), "A life this full should leave memories");

        for (StarterGoal goal : List.of(StarterGoal.GET_JOB, StarterGoal.FIRST_SHIFT, StarterGoal.COOK_MEAL,
                StarterGoal.PAY_BILLS)) {
            assertTrue(goals.isStarterDone(goal), "Starter goal not reached: " + goal);
        }
    }

    // -------------------------------------------------------------------------
    // The bot
    // -------------------------------------------------------------------------

    private int hour() {
        return time.getCurrentTick() % GameConstants.TICKS_PER_DAY;
    }

    /** Decides and plays one action for Alex, then advances time like the engine does. */
    private void playOneTurn() {
        int hour = hour();
        int day = time.getCurrentDay();
        buyPerks();

        if (hour >= 22 || hour < 7) {
            sleep();
            return;
        }
        if (alex.getHunger().getValue() <= 45 && eat()) {
            advance(1);
            return;
        }
        if (HomeSupplies.foodCount(world) < 4 && alex.getMoney() >= 60) {
            shop();
            advance(2);
            return;
        }
        if (!TimeManager.isWeekend(day) && lastShiftDay != day && hour >= 9 && hour <= 12
                && alex.getState().canWork() && alex.getState() != SimState.HUNGRY) {
            alex.setCurrentAction(ActionState.WORKING);
            alex.performWork();
            alex.setCurrentAction(ActionState.IDLE);
            shiftsWorked++;
            lastShiftDay = day;
            advance(alex.getCareer().getWorkingHours());
            return;
        }
        if (alex.getHygiene().getValue() <= 35) {
            alex.getHygiene().increase(50);
            advance(1);
            return;
        }
        if (alex.getSocial().getValue() <= 45 && sam.getState() != SimState.DEAD) {
            alex.getRelationshipManager().performSocialAction(sam, SocialAction.CHAT);
            sam.getSocial().increase(SocialAction.CHAT.getSocialChange() / 2);
            advance(1);
            return;
        }
        Residential bungalow = (Residential) world.getCityMap().get(1);
        if (!bungalow.isOwned() && alex.getMoney() >= bungalow.getPurchasePrice() + 400) {
            bungalow.purchase(alex);
            milestone("bought the Bungalow");
            advance(1);
            return;
        }
        if (HomeSupplies.find(world, Computer.class).isEmpty() && alex.getMoney() >= 900) {
            alex.setMoney(alex.getMoney() - 500);
            ((Residential) world.getCityMap().get(0)).getRooms().get(0).placeFurniture(new Computer(), 20);
            milestone("bought a Computer");
            advance(1);
            return;
        }
        if (!HomeSupplies.find(world, Computer.class).isEmpty() && alex.getState().canWork()
                && alex.getHunger().getValue() > 30) {
            try {
                new Computer().interact(alex, new Scanner(""), time);
            } catch (Exception e) {
                // too tired to study; just pass the hour
            }
            alex.setCurrentAction(ActionState.IDLE);
            advance(1);
            return;
        }
        advance(1);
    }

    /** Spends satisfaction points on perks as soon as Alex can afford them (takes no time). */
    private void buyPerks() {
        for (Reward perk : List.of(Reward.QUICK_LEARNER, Reward.GO_GETTER, Reward.SUNNY_DISPOSITION,
                Reward.BIG_DREAMER, Reward.SILVER_TONGUE)) {
            if (!alex.hasPerk(perk)) {
                if (alex.redeem(perk)) {
                    pointsSpent += perk.getCost();
                    if (alex.getWants().getPerks().size() == 1) {
                        milestone("first reward perk");
                    }
                    if (alex.getWants().getPerks().size() == 5) {
                        milestone("all five perks");
                    }
                }
                return;
            }
        }
    }

    /** Eats the most filling food at home, cooking raw ingredients. */
    private boolean eat() {
        Consumable food = HomeSupplies.takeFood(world, true);
        if (food == null) {
            return false;
        }
        if (food instanceof Ingredient) {
            Food meal = ((Ingredient) food).cook();
            alex.getHappiness().increase(GameConstants.COOKING_HAPPINESS);
            alex.recordMealCooked();
            alex.eat(meal);
        } else {
            alex.eat(food);
        }
        return true;
    }

    /** Buys groceries for the week and puts them in the fridge. */
    private void shop() {
        Item groceries = null;
        for (Item item : GameContent.shopCatalog("grocery", alex)) {
            if (item.getObjectName().equals("Groceries")) {
                groceries = item;
            }
        }
        Fridge fridge = HomeSupplies.find(world, Fridge.class).get(0);
        for (int i = 0; i < 8 && alex.getMoney() >= groceries.getPrice(); i++) {
            alex.setMoney(alex.getMoney() - groceries.getPrice());
            fridge.storeItem(groceries.copyItem());
        }
    }

    private void sleep() {
        int hour = hour();
        int hours = (GameConstants.TICKS_PER_DAY - hour + GameConstants.MORNING_HOUR) % GameConstants.TICKS_PER_DAY;
        if (hours == 0) hours = GameConstants.TICKS_PER_DAY;
        alex.sleep(hours);
        loop.processTick(alex, hours);
        alex.setCurrentAction(ActionState.IDLE);
    }

    private void advance(int hours) {
        loop.processTick(alex, hours);
    }

    // -------------------------------------------------------------------------
    // Measuring
    // -------------------------------------------------------------------------

    private void milestone(String name) {
        milestones.putIfAbsent(name, time.getCurrentDay());
    }

    private void observe() {
        int day = time.getCurrentDay();
        lowestHunger = Math.min(lowestHunger, alex.getHunger().getValue());
        lowestEnergy = Math.min(lowestEnergy, alex.getEnergy().getValue());
        lowestSocial = Math.min(lowestSocial, alex.getSocial().getValue());
        turns++;
        if (alex.isInGreatMood()) greatMoodTurns++;
        if (alex.getJobTier() >= 2) milestone("first promotion");
        if (alex.getJobTier() >= alex.getCareer().getMaxTier()) milestone("top of the career");
        if (alex.getSkillLevel(SkillType.LOGIC) >= 5) milestone("Logic level 5");
        if (alex.getSkillLevel(SkillType.LOGIC) >= GameConstants.MAX_SKILL_LEVEL) milestone("Logic level 10");
        if (goals.getStartersDone().size() == StarterGoal.values().length - 1) milestone("starter goals (bar meeting someone)");
        if (goals.isLegacyComplete()) milestone("legacy complete");
        for (int checkpoint : new int[] {7, 14, 30, 60, DAYS}) {
            if (day == checkpoint + 1) moneyByDay.putIfAbsent(checkpoint, alex.getMoney() + sam.getMoney());
        }
    }

    private void printReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Balance report: ").append(DAYS).append(" days, Software Engineer + idle partner ===\n");
        sb.append(String.format("  %-38s %s%n", "Milestone", "Day"));
        for (Map.Entry<String, Integer> m : milestones.entrySet()) {
            sb.append(String.format("  %-38s %d%n", m.getKey(), m.getValue()));
        }
        sb.append(String.format("%n  %-38s %s%n", "Household money at the end of day", "$"));
        for (Map.Entry<Integer, Integer> m : moneyByDay.entrySet()) {
            sb.append(String.format("  %-38s %d%n", "day " + m.getKey(), m.getValue()));
        }
        sb.append(String.format("%n  Final: tier %d/%d, Logic %d, performance %d/100, %d shifts worked%n",
                alex.getJobTier(), alex.getCareer().getMaxTier(), alex.getSkillLevel(SkillType.LOGIC),
                alex.getCareerManager().getPerformance(), shiftsWorked));
        sb.append(String.format("  Lowest needs: hunger %d, energy %d, social %d%n",
                lowestHunger, lowestEnergy, lowestSocial));
        sb.append(String.format("  Legacy score %d, Alex's aspiration %s (%s)%n", goals.getLegacyScore(household),
                alex.getAspiration().getDisplayName(), alex.isAspirationFulfilled() ? "fulfilled" : "in progress"));
        sb.append(String.format("  Inner life: %d satisfaction earned, %d perks, great mood %d%% of turns, %d memories%n",
                pointsSpent + alex.getWants().getPoints(), alex.getWants().getPerks().size(),
                turns == 0 ? 0 : greatMoodTurns * 100 / turns, alex.getMemories().size()));
        for (Memory m : alex.getMemories()) {
            sb.append(String.format("    day %-3d %s%n", m.getDay(), m.getText()));
        }
        System.out.println(sb);
    }
}
