package simcli.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import simcli.entities.actors.Sim;
import simcli.entities.managers.NPCManager;
import simcli.entities.models.Fear;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.entities.models.Memory;
import simcli.entities.models.Moodlet;
import simcli.entities.models.Reward;
import simcli.entities.models.SkillType;
import simcli.entities.models.Want;
import simcli.persistence.SaveManager;
import simcli.utils.GameConstants;
import simcli.utils.GameRandom;
import simcli.world.Residential;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the household's inner life: moodlets, daily wants and fears,
 * the reward store, and memories.
 */
@DisplayName("Inner Life — Moodlets, Wants, Fears, Rewards and Memories")
public class InnerLifeTest {

    private SimulationLogger logger;
    private Sim ann;
    private Sim bob;
    private List<Sim> household;
    private WorldManager world;
    private InnerLifeManager inner;

    @BeforeEach
    void setUp() {
        GameRandom.RANDOM.setSeed(7);
        logger = new SimulationLogger();
        SimulationLogger.setInstance(logger);
        ann = new Sim("Ann", 30, Gender.FEMALE, Job.UNEMPLOYED);
        bob = new Sim("Bob", 30, Gender.MALE, Job.UNEMPLOYED);
        household = new ArrayList<>(List.of(ann, bob));
        world = new WorldManager(new NPCManager(), household);
        world.setupWorld();
        inner = new InnerLifeManager(world, logger);
        setNeeds(ann, 60);
        setNeeds(bob, 60);
    }

    @AfterEach
    void tearDown() {
        GameRandom.RANDOM.setSeed(System.nanoTime());
    }

    private static void setNeeds(Sim sim, int value) {
        sim.getHunger().setValue(value);
        sim.getEnergy().setValue(value);
        sim.getHygiene().setValue(value);
        sim.getHappiness().setValue(value);
        sim.getSocial().setValue(value);
    }

    /** Ann and Bob fall for each other and marry. */
    private void wed() {
        ann.getRelationshipManager().increaseRelationship(bob, GameConstants.MARRIAGE_THRESHOLD);
        assertTrue(ann.getRelationshipManager().marry(bob));
    }

    /** Takes the first look at the household on day 2, then gives Ann exactly these wants. */
    private void startWith(List<Want> wants, Fear fear) {
        inner.afterTurn(household, 2);
        ann.getWants().startDay(2, wants, fear);
    }

    @Nested
    @DisplayName("Moodlets")
    class Moodlets {

        @Test
        @DisplayName("Moodlets add to the needs average, and bad ones take away")
        void testMoodAddsMoodlets() {
            assertEquals(60, ann.getMood());
            ann.getMoodlets().add(Moodlet.PROMOTED);
            assertEquals(80, ann.getMood());
            assertTrue(ann.isInGreatMood(), "A big enough moodlet can tip a Sim into a great mood");
            ann.getMoodlets().add(Moodlet.GRIEVING);
            assertEquals(55, ann.getMood());
        }

        @Test
        @DisplayName("Mood stays between 0 and 100")
        void testMoodClamped() {
            setNeeds(ann, 95);
            ann.getMoodlets().add(Moodlet.NEWLYWED);
            assertEquals(100, ann.getMood());
            setNeeds(ann, 5);
            ann.getMoodlets().clear();
            ann.getMoodlets().add(Moodlet.GRIEVING);
            assertEquals(0, ann.getMood());
        }

        @Test
        @DisplayName("Moodlets wear off after their hours, one hour per tick")
        void testMoodletsWearOff() {
            ann.getMoodlets().add(Moodlet.SQUEAKY_CLEAN);
            for (int i = 0; i < Moodlet.SQUEAKY_CLEAN.getHours() - 1; i++) {
                ann.tick();
            }
            assertTrue(ann.getMoodlets().has(Moodlet.SQUEAKY_CLEAN));
            assertEquals(1, ann.getMoodlets().getHoursLeft(Moodlet.SQUEAKY_CLEAN));
            ann.tick();
            assertFalse(ann.getMoodlets().has(Moodlet.SQUEAKY_CLEAN));
        }

        @Test
        @DisplayName("Feeling the same moodlet again restarts it instead of stacking")
        void testNoStacking() {
            ann.getMoodlets().add(Moodlet.HAD_A_BLAST);
            ann.tick();
            ann.getMoodlets().add(Moodlet.HAD_A_BLAST);
            assertEquals(Moodlet.HAD_A_BLAST.getHours(), ann.getMoodlets().getHoursLeft(Moodlet.HAD_A_BLAST));
            assertEquals(Moodlet.HAD_A_BLAST.getMood(), ann.getMoodlets().getTotal());
        }

        @Test
        @DisplayName("Sunny Disposition halves bad moodlets")
        void testSunny() {
            ann.grantPerk(Reward.SUNNY_DISPOSITION);
            ann.getMoodlets().add(Moodlet.GRIEVING);
            assertEquals(60 - 12, ann.getMood());
        }

        @Test
        @DisplayName("Cooking, showering, fun, chatting and sleeping well bring moodlets")
        void testEverydayMoodlets() {
            inner.afterTurn(household, 2);
            ann.recordMealCooked();
            ann.getHygiene().increase(50);
            ann.getHappiness().increase(25);
            ann.getSocial().increase(20);
            ann.getEnergy().setValue(100);
            inner.afterTurn(household, 2);
            for (Moodlet m : List.of(Moodlet.HOME_COOKED, Moodlet.SQUEAKY_CLEAN, Moodlet.HAD_A_BLAST,
                    Moodlet.GOOD_COMPANY, Moodlet.WELL_RESTED)) {
                assertTrue(ann.getMoodlets().has(m), "Missing " + m);
            }
            assertFalse(bob.getMoodlets().has(Moodlet.HOME_COOKED), "Bob didn't cook");
        }

        @Test
        @DisplayName("A failed social action leaves the Sim feeling rejected")
        void testRejected() {
            ann.getRelationshipManager().increaseRelationship(bob, 30);
            inner.afterTurn(household, 2);
            ann.getRelationshipManager().increaseRelationship(bob, -GameConstants.SOCIAL_FAIL_RELATIONSHIP_PENALTY);
            inner.afterTurn(household, 2);
            assertTrue(ann.getMoodlets().has(Moodlet.REJECTED));
        }
    }

    @Nested
    @DisplayName("Wants and fears")
    class WantsAndFears {

        @Test
        @DisplayName("Every playable Sim gets three wants and a fear each new day")
        void testDailyRoll() {
            inner.afterTurn(household, 1);
            for (Sim sim : household) {
                assertEquals(GameConstants.WANTS_PER_DAY, sim.getWants().getWants().size());
                assertNotNull(sim.getWants().getFear());
                assertEquals(1, sim.getWants().getDay());
            }
            List<Want> dayOne = new ArrayList<>(ann.getWants().getWants());
            inner.afterTurn(household, 1);
            assertEquals(dayOne, ann.getWants().getWants(), "Wants don't change during the day");
            inner.afterTurn(household, 2);
            assertEquals(2, ann.getWants().getDay(), "A new day brings new wants");
        }

        @Test
        @DisplayName("Big Dreamer gives an extra want")
        void testBigDreamer() {
            ann.grantPerk(Reward.BIG_DREAMER);
            inner.afterTurn(household, 1);
            assertEquals(GameConstants.WANTS_PER_DAY + 1, ann.getWants().getWants().size());
        }

        @Test
        @DisplayName("Wants only come up when they're possible")
        void testWantsSuit() {
            for (int day = 1; day <= 20; day++) {
                inner.afterTurn(household, day);
                List<Want> wants = ann.getWants().getWants();
                assertFalse(wants.contains(Want.WORK_A_SHIFT), "Ann has no job");
                assertFalse(wants.contains(Want.EARN_MONEY), "Ann has no job");
                assertFalse(wants.contains(Want.STUDY), "The dorm has no computer");
                assertNotEquals(Fear.GETTING_FIRED, ann.getWants().getFear(), "Ann has no job to lose");
            }
        }

        @Test
        @DisplayName("Doing a want pays satisfaction points and feels good")
        void testWantDone() {
            startWith(List.of(Want.COOK_A_MEAL, Want.BUY_SOMETHING), null);
            ann.recordMealCooked();
            inner.afterTurn(household, 2);
            assertTrue(ann.getWants().isDone(Want.COOK_A_MEAL));
            assertFalse(ann.getWants().isDone(Want.BUY_SOMETHING));
            assertEquals(Want.COOK_A_MEAL.getPoints(), ann.getWants().getPoints());
            assertTrue(ann.getMoodlets().has(Moodlet.WISH_CAME_TRUE));

            ann.recordMealCooked();
            inner.afterTurn(household, 2);
            assertEquals(Want.COOK_A_MEAL.getPoints(), ann.getWants().getPoints(), "A want only pays once");
        }

        @Test
        @DisplayName("Doing every want of the day pays a bonus")
        void testAllWantsBonus() {
            startWith(List.of(Want.COOK_A_MEAL, Want.BUY_SOMETHING), null);
            ann.recordMealCooked();
            ann.addTotalItemsBought(1);
            inner.afterTurn(household, 2);
            assertEquals(Want.COOK_A_MEAL.getPoints() + Want.BUY_SOMETHING.getPoints()
                    + GameConstants.ALL_WANTS_BONUS, ann.getWants().getPoints());
        }

        @Test
        @DisplayName("Wants done just before midnight still count")
        void testWantBeforeMidnight() {
            startWith(List.of(Want.COOK_A_MEAL, Want.MAKE_A_FRIEND), null);
            ann.recordMealCooked();
            inner.afterTurn(household, 3);
            assertEquals(Want.COOK_A_MEAL.getPoints(), ann.getWants().getPoints());
            assertEquals(3, ann.getWants().getDay());
        }

        @Test
        @DisplayName("Skill wants count practice and levels")
        void testSkillWants() {
            startWith(List.of(Want.WORK_OUT, Want.LEVEL_UP_A_SKILL), null);
            ann.getSkillManager().addExperience(SkillType.FITNESS, 5, "Ann");
            inner.afterTurn(household, 2);
            assertTrue(ann.getWants().isDone(Want.WORK_OUT));
            assertFalse(ann.getWants().isDone(Want.LEVEL_UP_A_SKILL));
            ann.getSkillManager().addExperience(SkillType.FITNESS, 100, "Ann");
            inner.afterTurn(household, 2);
            assertTrue(ann.getWants().isDone(Want.LEVEL_UP_A_SKILL));
        }

        @Test
        @DisplayName("A fear that comes true costs points and a bad moodlet, once")
        void testFearCameTrue() {
            startWith(List.of(Want.COOK_A_MEAL), Fear.GOING_HUNGRY);
            ann.getWants().setPoints(250);
            ann.getHunger().setValue(10);
            inner.afterTurn(household, 2);
            assertTrue(ann.getWants().hasFearCameTrue());
            assertEquals(250 - GameConstants.FEAR_POINT_PENALTY, ann.getWants().getPoints());
            assertTrue(ann.getMoodlets().has(Moodlet.WORST_FEAR));
            inner.afterTurn(household, 2);
            assertEquals(250 - GameConstants.FEAR_POINT_PENALTY, ann.getWants().getPoints(), "Only punished once");
        }

        @Test
        @DisplayName("Points never go below zero")
        void testPointsFloor() {
            startWith(List.of(), Fear.GOING_HUNGRY);
            ann.getWants().setPoints(30);
            ann.getHunger().setValue(0);
            inner.afterTurn(household, 2);
            assertEquals(0, ann.getWants().getPoints());
        }

        @Test
        @DisplayName("Getting fired makes the fear come true and leaves a Fired moodlet")
        void testFiredFear() {
            ann.getCareerManager().changeJob(Job.BARISTA, "Ann");
            startWith(List.of(), Fear.GETTING_FIRED);
            for (int i = 0; i < 4; i++) {
                ann.getCareerManager().checkTruancy("Ann", true);
            }
            assertEquals(Job.UNEMPLOYED, ann.getCareer());
            inner.afterTurn(household, 2);
            assertTrue(ann.getWants().hasFearCameTrue());
            assertTrue(ann.getMoodlets().has(Moodlet.FIRED));
            assertTrue(memories(ann).contains("Was fired from the Barista job"));
        }

        @Test
        @DisplayName("Babies don't have wants")
        void testBabiesHaveNoWants() {
            Sim baby = new Sim("Cy", 0, Gender.MALE, Job.UNEMPLOYED);
            baby.setChildSim(true);
            household.add(baby);
            inner.afterTurn(household, 1);
            assertTrue(baby.getWants().getWants().isEmpty());
        }
    }

    @Nested
    @DisplayName("Reward store")
    class Rewards {

        @Test
        @DisplayName("Rewards cost points, and perks can only be bought once")
        void testRedeem() {
            assertFalse(ann.redeem(Reward.QUICK_LEARNER), "No points yet");
            ann.getWants().setPoints(Reward.QUICK_LEARNER.getCost() * 2);
            assertTrue(ann.redeem(Reward.QUICK_LEARNER));
            assertTrue(ann.hasPerk(Reward.QUICK_LEARNER));
            assertEquals(Reward.QUICK_LEARNER.getCost(), ann.getWants().getPoints());
            assertFalse(ann.redeem(Reward.QUICK_LEARNER), "Already owned");
            assertEquals(Reward.QUICK_LEARNER.getCost(), ann.getWants().getPoints());
        }

        @Test
        @DisplayName("Boosts take effect at once and can be bought again")
        void testBoosts() {
            ann.getWants().setPoints(1000);
            assertTrue(ann.redeem(Reward.INSPIRATION));
            assertTrue(ann.getMoodlets().has(Moodlet.INSPIRED));
            assertTrue(ann.redeem(Reward.INSPIRATION));
            int energy = ann.getEnergy().getValue();
            assertTrue(ann.redeem(Reward.SECOND_WIND));
            assertEquals(energy + GameConstants.SECOND_WIND_ENERGY, ann.getEnergy().getValue());
            assertFalse(ann.hasPerk(Reward.SECOND_WIND), "Boosts aren't kept");
        }

        @Test
        @DisplayName("Quick Learner speeds up skills")
        void testQuickLearner() {
            ann.trainSkill(SkillType.LOGIC, 20);
            int without = ann.getSkillManager().getExperience(SkillType.LOGIC);
            bob.grantPerk(Reward.QUICK_LEARNER);
            bob.trainSkill(SkillType.LOGIC, 20);
            assertEquals((int) (without * GameConstants.PERK_BONUS), bob.getSkillManager().getExperience(SkillType.LOGIC));
        }

        @Test
        @DisplayName("Go-Getter fills the performance meter faster")
        void testGoGetter() {
            ann.getCareerManager().changeJob(Job.BARISTA, "Ann");
            bob.getCareerManager().changeJob(Job.BARISTA, "Bob");
            bob.grantPerk(Reward.GO_GETTER);
            ann.performWork();
            bob.performWork();
            assertTrue(bob.getCareerManager().getPerformance() > ann.getCareerManager().getPerformance());
        }

        @Test
        @DisplayName("Silver Tongue makes jokes land more often")
        void testSilverTongue() {
            int before = ann.getRelationshipManager().getSuccessChance(bob, simcli.entities.models.SocialAction.JOKE);
            ann.grantPerk(Reward.SILVER_TONGUE);
            int after = ann.getRelationshipManager().getSuccessChance(bob, simcli.entities.models.SocialAction.JOKE);
            assertEquals(Math.min(95, before + GameConstants.SILVER_TONGUE_BONUS), after);
        }
    }

    @Nested
    @DisplayName("Memories")
    class Memories {

        @Test
        @DisplayName("A new household starts its story on day 1")
        void testNewLife() {
            inner.afterTurn(household, 1);
            assertEquals(List.of("Started a new life at The Shared Dorm"), memories(ann));
        }

        @Test
        @DisplayName("Loading a game mid-way doesn't invent memories")
        void testNoMemoriesOnLoad() {
            wed();
            inner.afterTurn(household, 9);
            assertTrue(ann.getMemories().isEmpty());
        }

        @Test
        @DisplayName("Jobs, promotions, friends and weddings are remembered")
        void testLifeEvents() {
            inner.afterTurn(household, 2);
            ann.getCareerManager().changeJob(Job.BARISTA, "Ann");
            inner.afterTurn(household, 2);
            ann.getCareerManager().promote("Ann");
            inner.afterTurn(household, 3);
            ann.getRelationshipManager().increaseRelationship(bob, GameConstants.FRIEND_THRESHOLD);
            inner.afterTurn(household, 4);
            wed();
            inner.afterTurn(household, 5);

            List<String> m = memories(ann);
            assertTrue(m.contains("Got a job as a Barista"), m.toString());
            assertTrue(m.contains("Was promoted to tier 2 as a Barista"), m.toString());
            assertTrue(m.contains("Became friends with Bob"), m.toString());
            assertTrue(m.contains("Married Bob"), m.toString());
            assertTrue(ann.getMoodlets().has(Moodlet.NEWLYWED));
            assertEquals(5, ann.getMemories().get(ann.getMemories().size() - 1).getDay());
        }

        @Test
        @DisplayName("A birth is remembered by the parents and the baby")
        void testBirth() {
            wed();
            inner.afterTurn(household, 2);
            Sim baby = ann.getRelationshipManager().finalizeBaby("Cy", Gender.MALE);
            household.add(baby);
            inner.afterTurn(household, 3);
            assertTrue(memories(ann).contains("Welcomed a baby, Cy"));
            assertTrue(ann.getMoodlets().has(Moodlet.NEW_PARENT));
            assertTrue(memories(baby).get(0).startsWith("Was born to "), memories(baby).toString());
        }

        @Test
        @DisplayName("A death is remembered, and loved ones grieve")
        void testDeath() {
            wed();
            inner.afterTurn(household, 2);
            bob.markAsDead();
            inner.afterTurn(household, 3);
            assertTrue(memories(bob).contains("Passed away at the age of 30"));
            assertTrue(memories(ann).contains("Lost Bob"));
            assertTrue(ann.getMoodlets().has(Moodlet.GRIEVING));
        }

        @Test
        @DisplayName("Buying a home is remembered by everyone")
        void testNewHome() {
            inner.afterTurn(household, 2);
            ann.setMoney(10000);
            ((Residential) world.getCityMap().get(1)).purchase(ann);
            inner.afterTurn(household, 3);
            assertTrue(memories(ann).contains("Moved into The Bungalow"));
            assertTrue(memories(bob).contains("Moved into The Bungalow"));
        }
    }

    @Test
    @DisplayName("The game loop runs the inner life every turn")
    void testGameLoopWiring() {
        TimeManager time = new TimeManager(GameConstants.MORNING_HOUR, GameConstants.TICKS_PER_DAY);
        GameLoop loop = new GameLoop(time, household, logger, world, null);
        loop.beginSession();
        assertEquals(GameConstants.WANTS_PER_DAY, ann.getWants().getWants().size());
        ann.recordMealCooked();
        loop.processTick(ann, 1);
        assertTrue(ann.getMoodlets().has(Moodlet.HOME_COOKED));
    }

    @Test
    @DisplayName("Moodlets, wants, fears, points, perks and memories survive a save")
    void testSaveRoundTrip() {
        String worldName = "JUnitInnerLife";
        SaveManager.deleteSave(worldName);
        try {
            ann.getMoodlets().add(Moodlet.PROMOTED);
            ann.getMoodlets().tick();
            ann.getWants().startDay(1, List.of(Want.COOK_A_MEAL, Want.MAKE_A_FRIEND), Fear.REJECTION);
            ann.getWants().markDone(Want.COOK_A_MEAL);
            ann.getWants().markFearCameTrue();
            ann.getWants().setPoints(420);
            ann.grantPerk(Reward.SUNNY_DISPOSITION);
            ann.remember(1, "Started a new life, with commas, at home");
            ann.remember(3, "Married Bob");

            GameEngine engine = new GameEngine(worldName, 10, household, false);
            SaveManager.saveGame(engine, worldName);
            GameEngine loaded = SaveManager.loadGame(worldName);
            assertNotNull(loaded);
            Sim lAnn = loaded.getNeighborhood().get(0);

            assertEquals(Moodlet.PROMOTED.getHours() - 1, lAnn.getMoodlets().getHoursLeft(Moodlet.PROMOTED));
            assertEquals(List.of(Want.COOK_A_MEAL, Want.MAKE_A_FRIEND), lAnn.getWants().getWants());
            assertTrue(lAnn.getWants().isDone(Want.COOK_A_MEAL));
            assertFalse(lAnn.getWants().isDone(Want.MAKE_A_FRIEND));
            assertEquals(Fear.REJECTION, lAnn.getWants().getFear());
            assertTrue(lAnn.getWants().hasFearCameTrue());
            assertEquals(1, lAnn.getWants().getDay());
            assertEquals(420, lAnn.getWants().getPoints());
            assertTrue(lAnn.hasPerk(Reward.SUNNY_DISPOSITION));
            lAnn.getMoodlets().add(Moodlet.GRIEVING);
            assertEquals(Moodlet.GRIEVING.getMood() / 2, lAnn.getMoodlets().effectOf(Moodlet.GRIEVING),
                    "The perk's effect comes back too");
            assertEquals(List.of("Started a new life, with commas, at home", "Married Bob"), memories(lAnn));
            assertEquals(3, lAnn.getMemories().get(1).getDay());
            assertTrue(loaded.getNeighborhood().get(1).getWants().getWants().isEmpty());
        } finally {
            SaveManager.deleteSave(worldName);
        }
    }

    private static List<String> memories(Sim sim) {
        List<String> texts = new ArrayList<>();
        for (Memory m : sim.getMemories()) {
            texts.add(m.getText());
        }
        return texts;
    }
}
