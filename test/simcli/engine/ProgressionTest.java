package simcli.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import simcli.engine.commands.CommandContext;
import simcli.engine.commands.HomeChatCommand;
import simcli.engine.commands.SpouseInteractionCommand;
import simcli.engine.commands.TravelCommand;
import simcli.entities.actors.Sim;
import simcli.entities.items.Food;
import simcli.entities.items.Ingredient;
import simcli.entities.managers.NPCManager;
import simcli.entities.managers.SkillManager;
import simcli.entities.models.Aspiration;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.entities.models.SimState;
import simcli.entities.models.SkillType;
import simcli.entities.models.StarterGoal;
import simcli.persistence.SaveManager;
import simcli.utils.GameConstants;
import simcli.utils.GameRandom;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.interactables.ComfyBed;
import simcli.world.interactables.Fridge;
import simcli.world.interactables.SmartFridge;
import simcli.world.interactables.Television;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the balance pass: early game (ageing, home chats, starter goals,
 * unlocks), midgame (pregnancy, baby food, furniture, the Townhouse) and
 * late game (inheritance, generations, mentoring, second aspirations,
 * legacy score, lifespan).
 */
@DisplayName("Progression — Early, Mid and Late Game")
public class ProgressionTest {

    private SimulationLogger logger;
    private Sim ann;
    private Sim bob;
    private List<Sim> household;
    private WorldManager world;

    @BeforeEach
    void setUp() {
        logger = new SimulationLogger();
        SimulationLogger.setInstance(logger);
        ann = new Sim("Ann", 30, Gender.FEMALE, Job.UNEMPLOYED);
        bob = new Sim("Bob", 30, Gender.MALE, Job.UNEMPLOYED);
        household = new ArrayList<>(List.of(ann, bob));
        world = new WorldManager(new NPCManager(), household);
        world.setupWorld();
    }

    @AfterEach
    void tearDown() {
        GameRandom.RANDOM.setSeed(System.nanoTime());
    }

    private CommandContext context(Sim active, String input, int startTick) {
        return new CommandContext.Builder()
                .activePlayer(active)
                .neighborhood(household)
                .scanner(new Scanner(new ByteArrayInputStream(input.getBytes())))
                .timeManager(new TimeManager(startTick, 24))
                .worldManager(world)
                .currentLocation(world.getCurrentLocation())
                .logger(logger)
                .build();
    }

    private Fridge dormFridge() {
        return HomeSupplies.find(world, Fridge.class).get(0);
    }

    private void marry() {
        ann.getRelationshipManager().setSpouse(bob);
        bob.getRelationshipManager().setSpouse(ann);
    }

    @Nested
    @DisplayName("Early game")
    class EarlyGame {

        @Test
        @DisplayName("Needs only drain faster with age for elders, and never more than 1.3x")
        void testAgeMultiplier() {
            assertEquals(1.0, new Sim("Teen", 15, Gender.MALE, Job.UNEMPLOYED).getAgeDecayMultiplier(), 1e-9);
            assertEquals(1.0, new Sim("Mid", 50, Gender.MALE, Job.UNEMPLOYED).getAgeDecayMultiplier(), 1e-9);
            assertEquals(1.1, new Sim("Old", 70, Gender.MALE, Job.UNEMPLOYED).getAgeDecayMultiplier(), 1e-9);
            assertEquals(GameConstants.MAX_AGE_DECAY_MULTIPLIER,
                    new Sim("Ancient", 89, Gender.MALE, Job.UNEMPLOYED).getAgeDecayMultiplier(), 1e-9);
        }

        @Test
        @DisplayName("A 50-year-old's needs drain exactly as fast as a 20-year-old's")
        void testMiddleAgeNotPunished() {
            Sim young = new Sim("Young", 20, Gender.MALE, Job.UNEMPLOYED);
            Sim older = new Sim("Older", 50, Gender.MALE, Job.UNEMPLOYED);
            for (Sim s : List.of(young, older)) {
                s.getTraits().clear();
                s.getTraits().add(simcli.entities.models.Trait.SOCIALITE);
            }
            for (int i = 0; i < 5; i++) {
                young.tick();
                older.tick();
            }
            assertEquals(young.getHunger().getValue(), older.getHunger().getValue());
            assertEquals(young.getSocial().getValue(), older.getSocial().getValue());
        }

        @Test
        @DisplayName("Chatting at home raises social for both Sims")
        void testHomeChat() throws Exception {
            ann.getSocial().setValue(20);
            bob.getSocial().setValue(20);

            CommandResult result = new HomeChatCommand(context(ann, "1\n1\n", 10)).execute();

            assertEquals(CommandResult.TICK_FORWARD, result);
            assertTrue(ann.getSocial().getValue() > 20);
            assertTrue(bob.getSocial().getValue() > 20, "The other Sim enjoys the chat too");
            assertTrue(ann.getRelationshipManager().getRelationship(bob) > 0);
        }

        @Test
        @DisplayName("Nobody to chat with: no time passes")
        void testHomeChatAlone() throws Exception {
            household.remove(bob);
            assertEquals(CommandResult.NO_TICK, new HomeChatCommand(context(ann, "\n", 10)).execute());
        }

        @Test
        @DisplayName("Lonely idle Sims chat with each other on their own")
        void testAutonomousChat() {
            Sim cal = new Sim("Cal", 30, Gender.MALE, Job.UNEMPLOYED);
            household.add(cal);
            bob.getSocial().setValue(10);
            cal.getSocial().setValue(60);

            new AutonomyManager(world, logger).act(ann, household, false);

            assertEquals(10 + GameConstants.HOME_CHAT_SOCIAL, bob.getSocial().getValue());
            assertEquals(GameConstants.HOME_CHAT_RELATIONSHIP, bob.getRelationshipManager().getRelationship(cal));
        }

        @Test
        @DisplayName("Starter goals pay out once each")
        void testStarterGoals() {
            GoalManager goals = new GoalManager(world, logger);
            ann.setMoney(0);
            goals.check(household);
            assertTrue(goals.getStartersDone().isEmpty());

            ann.getCareerManager().changeJob(Job.SOFTWARE_ENGINEER, "Ann");
            ann.recordMealCooked();
            goals.check(household);
            goals.check(household);

            assertTrue(goals.isStarterDone(StarterGoal.GET_JOB));
            assertTrue(goals.isStarterDone(StarterGoal.COOK_MEAL));
            assertFalse(goals.isStarterDone(StarterGoal.PAY_BILLS));
            assertEquals(StarterGoal.GET_JOB.getReward() + StarterGoal.COOK_MEAL.getReward(), ann.getMoney());

            goals.recordBillsPaid();
            goals.check(household);
            assertTrue(goals.isStarterDone(StarterGoal.PAY_BILLS));
        }

        @Test
        @DisplayName("The Bookshop opens after the household's first paycheck")
        void testBookshopUnlock() throws Exception {
            Building bookshop = world.getCityMap().get(3);
            assertFalse(bookshop.isOpenTo(household));
            assertEquals(CommandResult.NO_TICK, new TravelCommand(context(ann, "4\n\n", 10)).execute());

            bob.getCareerManager().changeJob(Job.BARISTA, "Bob");
            bob.performWork();
            assertTrue(bookshop.isOpenTo(household));
        }

        @Test
        @DisplayName("No household events in the first two days")
        void testCalmStart() {
            GameRandom.RANDOM.setSeed(3);
            Food pie = null;
            for (int i = 0; i < 2000 && pie == null; i++) {
                new RandomEventManager(logger).triggerHouseholdEvent(household, new TimeManager(12, 24), world);
                if (!dormFridge().getStoredItems().isEmpty()) {
                    pie = (Food) dormFridge().getStoredItems().get(0);
                }
            }
            assertNull(pie, "Day 1 should have no neighbour's pie (or any household event)");
        }
    }

    @Nested
    @DisplayName("Midgame")
    class Midgame {

        /** Seeds the shared random so the next 50% roll succeeds. */
        private void nextRollSucceeds() {
            long seed = 0;
            while (new Random(seed).nextInt(100) >= 50) seed++;
            GameRandom.RANDOM.setSeed(seed);
        }

        @Test
        @DisplayName("A successful try starts a pregnancy; the baby arrives days later")
        void testPregnancy() throws Exception {
            marry();
            nextRollSucceeds();

            new SpouseInteractionCommand(context(bob, "2\nPip\n", 10)).execute();

            assertTrue(ann.isPregnant(), "The mother carries the baby");
            assertEquals(2, household.size(), "No baby straight away");
            assertEquals("Pip", ann.getExpectedBabyName());

            FamilyManager family = new FamilyManager(logger);
            family.processDay(1 + GameConstants.PREGNANCY_DAYS - 1, household);
            assertEquals(2, household.size(), "Not due yet");
            family.processDay(1 + GameConstants.PREGNANCY_DAYS, household);
            assertEquals(3, household.size());
            assertFalse(ann.isPregnant());
            Sim pip = household.get(2);
            assertEquals("Pip", pip.getName());
            assertEquals(2, pip.getGeneration(), "Children are the next generation");
        }

        @Test
        @DisplayName("While expecting, and just after a birth, the couple can't try again")
        void testCooldown() throws Exception {
            marry();
            ann.startPregnancy(5, "Pip", Gender.FEMALE);
            assertEquals(CommandResult.NO_TICK, new SpouseInteractionCommand(context(bob, "2\n\n", 10)).execute());

            ann.endPregnancy(1);
            assertEquals(CommandResult.NO_TICK, new SpouseInteractionCommand(context(bob, "2\n\n", 10)).execute(),
                    "Day 1 of rest after a birth on day 1");
        }

        @Test
        @DisplayName("Babies eat real food: with an empty fridge they go hungry")
        void testBabiesNeedFood() {
            marry();
            Sim baby = ann.getRelationshipManager().finalizeBaby("Bub", Gender.MALE);
            household.add(baby);
            baby.getHunger().setValue(10);

            assertFalse(HomeSupplies.feedBaby(bob, baby, world, logger));
            assertEquals(10, baby.getHunger().getValue());

            dormFridge().storeItem(new Food("Apple", 10, 15, 5));
            dormFridge().storeItem(new Ingredient("Groceries", 15, 10, "Home-cooked Meal", 55, 6));
            assertTrue(HomeSupplies.feedBaby(bob, baby, world, logger));
            assertEquals(10 + GameConstants.BABY_FEED_AMOUNT, baby.getHunger().getValue());
            assertEquals(1, dormFridge().getStoredItems().size(), "One item per feed");
            assertTrue(dormFridge().getStoredItems().get(0) instanceof Ingredient,
                    "Babies get the least filling food, saving the big meals");
        }

        @Test
        @DisplayName("A Comfy Bed adds fun and halves the night's hunger")
        void testComfyBed() {
            ann.getHappiness().setValue(50);
            ann.getHunger().setValue(80);
            assertThrows(SleepEventException.class, () -> new ComfyBed().interact(ann, new Scanner(""), new TimeManager(22, 24)));
            assertEquals(50 + GameConstants.COMFY_BED_FUN, ann.getHappiness().getValue());
            assertEquals(80 - 3 * 10 / 2, ann.getHunger().getValue(), "10 hours till morning, half the usual cost");
        }

        @Test
        @DisplayName("A Smart Fridge keeps food twice as long")
        void testSmartFridge() {
            Residential dorm = (Residential) world.getCityMap().get(0);
            SmartFridge smart = new SmartFridge();
            dorm.getRooms().get(1).placeFurniture(smart, 25);
            Food inSmart = new Food("Apple", 10, 15, 5, 4);
            Food inPlain = new Food("Apple", 10, 15, 5, 4);
            smart.storeItem(inSmart);
            dormFridge().storeItem(inPlain);

            UpkeepManager upkeep = new UpkeepManager(world, logger);
            upkeep.processDay(9, household);
            upkeep.processDay(10, household);

            assertEquals(2, inPlain.getFreshness());
            assertEquals(3, inSmart.getFreshness());
        }

        @Test
        @DisplayName("TV is fun, and new furniture can be bought and rebuilt from saves")
        void testTvAndFactory() throws Exception {
            ann.getHappiness().setValue(40);
            new Television().interact(ann, new Scanner(""), new TimeManager(12, 24));
            assertEquals(40 + GameConstants.TV_FUN, ann.getHappiness().getValue());

            for (String name : List.of("Comfy Bed", "Smart Fridge", "TV")) {
                assertNotNull(simcli.world.interactables.InteractableFactory.create(name), name);
            }
        }

        @Test
        @DisplayName("The Townhouse is a third, pricier home with the best furniture")
        void testTownhouse() {
            Residential townhouse = (Residential) world.getCityMap().get(5);
            assertEquals("The Townhouse", townhouse.getName());
            assertEquals(15000, townhouse.getPurchasePrice());
            assertFalse(townhouse.isOwned());
            assertFalse(HomeSupplies.find(world, SmartFridge.class).iterator().hasNext(),
                    "Its furniture isn't usable until it's bought");
            townhouse.setOwned(true);
            assertEquals(1, HomeSupplies.find(world, SmartFridge.class).size());
            assertEquals(1, HomeSupplies.find(world, ComfyBed.class).size());
        }
    }

    @Nested
    @DisplayName("Late game")
    class LateGame {

        @Test
        @DisplayName("A Sim's money passes to their spouse, otherwise their children")
        void testInheritance() {
            marry();
            Sim kid = ann.getRelationshipManager().finalizeBaby("Kid", Gender.MALE);
            household.add(kid);
            ann.setMoney(1000);
            bob.setMoney(0);
            FamilyManager family = new FamilyManager(logger);
            family.checkDeaths(household);

            ann.markAsDead();
            family.checkDeaths(household);
            assertEquals(1000, bob.getMoney());
            assertEquals(0, ann.getMoney());

            bob.markAsDead();
            family.checkDeaths(household);
            assertEquals(1000, kid.getMoney(), "With no spouse left, the children inherit");
        }

        @Test
        @DisplayName("When the founders are gone, the next generation takes charge")
        void testGenerationHandover() {
            marry();
            Sim teen = ann.getRelationshipManager().finalizeBaby("Teen", Gender.MALE);
            for (int i = 0; i < GameConstants.TEEN_AGE; i++) {
                teen.ageUp(false);
            }
            household.add(teen);
            FamilyManager family = new FamilyManager(logger);
            family.checkDeaths(household);
            assertEquals(1, GoalManager.getHighestGeneration(List.of(ann, bob)));
            assertEquals(2, GoalManager.getHighestGeneration(household));

            ann.markAsDead();
            bob.markAsDead();
            logger.reset();
            family.checkDeaths(household);
            // the handover message is logged; nothing to crash, and the teen is the heir
            assertSame(teen, FamilyManager.heirs(bob, household).get(0));
        }

        @Test
        @DisplayName("Elders mentor teens in their best skill and enjoy it")
        void testMentoring() {
            Sim grandma = new Sim("Gran", 70, Gender.FEMALE, Job.UNEMPLOYED);
            grandma.getSkillManager().setExperience(SkillType.CHARISMA, SkillManager.experienceForLevel(6));
            grandma.getHappiness().setValue(40);
            Sim teen = new Sim("Tia", 15, Gender.FEMALE, Job.UNEMPLOYED);
            teen.getTraits().clear();
            teen.getTraits().add(simcli.entities.models.Trait.ACTIVE); // Socialites would learn Charisma faster
            teen.getHappiness().setValue(40);
            teen.getSocial().setValue(40);
            List<Sim> family = new ArrayList<>(List.of(grandma, teen));

            new FamilyManager(logger).processDay(5, family);

            assertEquals(GameConstants.MENTOR_XP, teen.getSkillManager().getExperience(SkillType.CHARISMA));
            assertEquals(40 + GameConstants.GRANDPARENT_CARE_FUN, grandma.getHappiness().getValue());
        }

        @Test
        @DisplayName("Grandparents enjoy feeding the babies")
        void testGrandparentCare() {
            Sim grandpa = new Sim("Gramps", 70, Gender.MALE, Job.UNEMPLOYED);
            grandpa.getSocial().setValue(30);
            Sim baby = new Sim("Bub", 0, Gender.MALE, Job.UNEMPLOYED);
            baby.setChildSim(true);
            baby.getHunger().setValue(10);
            dormFridge().storeItem(new Food("Apple", 10, 15, 5));

            assertTrue(HomeSupplies.feedBaby(grandpa, baby, world, logger));
            assertEquals(30 + GameConstants.GRANDPARENT_CARE_SOCIAL, grandpa.getSocial().getValue());
        }

        @Test
        @DisplayName("After fulfilling an aspiration a Sim can choose another, and the legacy score counts both")
        void testSecondAspiration() {
            GoalManager goals = new GoalManager(world, logger);
            ann.setAspiration(Aspiration.HOMEOWNER);
            ((Residential) world.getCityMap().get(1)).setOwned(true);
            goals.check(household);
            assertTrue(ann.isAspirationFulfilled());
            int scoreAfterOne = goals.getLegacyScore(household);

            assertFalse(goals.nextAspirations(ann).contains(Aspiration.HOMEOWNER));
            ann.chooseNextAspiration(Aspiration.SKILL_MASTER);
            assertFalse(ann.isAspirationFulfilled());
            assertTrue(ann.getPastAspirations().contains(Aspiration.HOMEOWNER));
            assertEquals(1, ann.getAspirationsFulfilled());

            ann.getSkillManager().setExperience(SkillType.LOGIC, SkillManager.experienceForLevel(10));
            goals.check(household);
            assertEquals(2, ann.getAspirationsFulfilled());
            assertTrue(goals.getLegacyScore(household) >= scoreAfterOne + GameConstants.LEGACY_POINTS_ASPIRATION);
        }

        @Test
        @DisplayName("Can't move on before the current aspiration is done")
        void testNoEarlySwitch() {
            assertThrows(IllegalStateException.class, () -> ann.chooseNextAspiration(Aspiration.BIG_FAMILY));
        }

        @Test
        @DisplayName("Lifespan changes how fast adults age; children still grow a year a day")
        void testLifespan() {
            GameEngine engine = new GameEngine("JUnitLifespan", 10, household, false);
            engine.setLifespan(Lifespan.LONG);
            assertEquals(5, engine.getLifecycleManager().getDaysPerAgeTick());
            Sim baby = new Sim("Bub", 0, Gender.MALE, Job.UNEMPLOYED);
            assertEquals(1, engine.getLifecycleManager().daysPerAgeTickFor(baby));
            assertEquals(5, engine.getLifecycleManager().daysPerAgeTickFor(ann));
        }
    }

    @Test
    @DisplayName("Everything new survives a save and reload")
    void testNewStateSaved() {
        String worldName = "JUnitProgression";
        SaveManager.deleteSave(worldName);
        try {
            marry();
            ann.setGeneration(2);
            ann.recordMealCooked();
            ann.setAspiration(Aspiration.HOMEOWNER);
            ann.setAspirationFulfilled(true);
            ann.chooseNextAspiration(Aspiration.SOCIAL_BUTTERFLY);
            ann.startPregnancy(12, "Bea, Jr.", Gender.FEMALE);
            ann.setLastBirthDay(4);
            bob.getCareerManager().changeJob(Job.SOFTWARE_ENGINEER, "Bob");
            bob.getCareerManager().setPerformance(42);
            bob.getCareerManager().setTotalShiftsWorked(7);

            GameEngine engine = new GameEngine(worldName, 10, household, false);
            engine.setLifespan(Lifespan.SHORT);
            engine.getGoalManager().restoreStarter(StarterGoal.GET_JOB);
            SaveManager.saveGame(engine, worldName);

            GameEngine loaded = SaveManager.loadGame(worldName);
            assertNotNull(loaded);
            Sim lAnn = loaded.getNeighborhood().get(0);
            Sim lBob = loaded.getNeighborhood().get(1);
            assertEquals(Lifespan.SHORT, loaded.getLifespan());
            assertEquals(2, loaded.getLifecycleManager().getDaysPerAgeTick());
            assertEquals(2, lAnn.getGeneration());
            assertEquals(1, lAnn.getMealsCooked());
            assertTrue(lAnn.getPastAspirations().contains(Aspiration.HOMEOWNER));
            assertEquals(Aspiration.SOCIAL_BUTTERFLY, lAnn.getAspiration());
            assertTrue(lAnn.isPregnant());
            assertEquals(12, lAnn.getPregnancyDueDay());
            assertEquals("Bea, Jr.", lAnn.getExpectedBabyName());
            assertEquals(Gender.FEMALE, lAnn.getExpectedBabyGender());
            assertEquals(4, lAnn.getLastBirthDay());
            assertEquals(42, lBob.getCareerManager().getPerformance());
            assertEquals(7, lBob.getCareerManager().getTotalShiftsWorked());
            assertTrue(loaded.getGoalManager().isStarterDone(StarterGoal.GET_JOB));
            assertNotEquals(SimState.DEAD, lAnn.getState());
        } finally {
            SaveManager.deleteSave(worldName);
        }
    }
}
