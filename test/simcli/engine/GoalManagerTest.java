package simcli.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.entities.actors.NPCSim;
import simcli.entities.actors.Sim;
import simcli.entities.managers.NPCManager;
import simcli.entities.models.Achievement;
import simcli.entities.models.Aspiration;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.entities.models.SkillType;
import simcli.persistence.SaveManager;
import simcli.utils.GameConstants;
import simcli.world.Building;
import simcli.world.Residential;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for goals: life aspirations, achievements and the legacy ending.
 */
@DisplayName("GoalManager — Aspirations, Achievements and Legacy")
public class GoalManagerTest {

    private Sim ann;
    private List<Sim> household;
    private WorldManager world;
    private GoalManager goals;

    @BeforeEach
    void setUp() {
        SimulationLogger.setInstance(new SimulationLogger());
        ann = new Sim("Ann", 30, Gender.FEMALE, Job.UNEMPLOYED);
        household = new ArrayList<>(List.of(ann));
        world = new WorldManager(new NPCManager(), household);
        world.setupWorld();
        goals = new GoalManager(world, new SimulationLogger());
    }

    private Residential bungalow() {
        for (Building b : world.getCityMap()) {
            if (b instanceof Residential && ((Residential) b).getPurchasePrice() > 0) {
                return (Residential) b;
            }
        }
        throw new AssertionError("no bungalow");
    }

    @Test
    @DisplayName("Every Sim starts with an aspiration that isn't fulfilled")
    void testSimsHaveAspirations() {
        assertNotNull(ann.getAspiration());
        assertFalse(ann.isAspirationFulfilled());
    }

    @Test
    @DisplayName("Progress is measured for each aspiration")
    void testProgress() {
        assertEquals(0, goals.progress(ann, Aspiration.CAREER_STAR).getCurrent());
        ann.getCareerManager().changeJob(Job.SOFTWARE_ENGINEER, "Ann");
        ann.setJobTier(3);
        GoalManager.Progress career = goals.progress(ann, Aspiration.CAREER_STAR);
        assertEquals(3, career.getCurrent());
        assertEquals(5, career.getTarget());
        assertEquals(60, career.getPercent());

        ann.getSkillManager().setExperience(SkillType.LOGIC, 4 * GameConstants.SKILL_XP_PER_LEVEL);
        assertEquals(4, goals.progress(ann, Aspiration.SKILL_MASTER).getCurrent());

        assertFalse(goals.progress(ann, Aspiration.HOMEOWNER).isComplete());
        bungalow().setOwned(true);
        assertTrue(goals.progress(ann, Aspiration.HOMEOWNER).isComplete());
    }

    @Test
    @DisplayName("Fulfilling an aspiration pays out once and completes a one-Sim legacy")
    void testFulfilAspiration() {
        ann.setAspiration(Aspiration.HOMEOWNER);
        ann.setMoney(0);
        goals.check(household);
        assertFalse(ann.isAspirationFulfilled());
        assertFalse(goals.isLegacyComplete());

        bungalow().setOwned(true);
        goals.check(household);
        goals.check(household);

        assertTrue(ann.isAspirationFulfilled());
        assertEquals(GameConstants.ASPIRATION_REWARD_MONEY, ann.getMoney(), "The reward is paid exactly once");
        assertTrue(goals.isLegacyComplete());
    }

    @Test
    @DisplayName("The legacy needs every playable Sim's aspiration, not just one")
    void testLegacyNeedsEveryone() {
        Sim bob = new Sim("Bob", 30, Gender.MALE, Job.UNEMPLOYED);
        household.add(bob);
        ann.setAspiration(Aspiration.HOMEOWNER);
        bob.setAspiration(Aspiration.BIG_FAMILY);
        bungalow().setOwned(true);

        goals.check(household);

        assertTrue(ann.isAspirationFulfilled());
        assertFalse(bob.isAspirationFulfilled());
        assertFalse(goals.isLegacyComplete());
    }

    @Test
    @DisplayName("Babies don't hold up the legacy")
    void testBabiesDontCount() {
        Sim bob = new Sim("Bob", 30, Gender.MALE, Job.UNEMPLOYED);
        ann.getRelationshipManager().setSpouse(bob);
        bob.getRelationshipManager().setSpouse(ann);
        Sim baby = ann.getRelationshipManager().finalizeBaby("Bub", Gender.MALE);
        household.add(baby);
        ann.setAspiration(Aspiration.HOMEOWNER);
        bungalow().setOwned(true);

        goals.check(household);

        assertTrue(goals.isLegacyComplete());
    }

    @Test
    @DisplayName("Achievements unlock from the household's state")
    void testAchievements() {
        goals.check(household);
        assertFalse(goals.isUnlocked(Achievement.FIRST_JOB));

        ann.getCareerManager().changeJob(Job.SOFTWARE_ENGINEER, "Ann");
        ann.setJobTier(2);
        Sim bob = new NPCSim("Bob", 30, Gender.MALE, Job.UNEMPLOYED);
        ann.getRelationshipManager().increaseRelationship(bob, GameConstants.FRIEND_THRESHOLD);
        ann.getRelationshipManager().setSpouse(bob);
        ann.setMoney(GameConstants.FORTUNE_AMOUNT);
        goals.check(household);

        assertTrue(goals.isUnlocked(Achievement.FIRST_JOB));
        assertTrue(goals.isUnlocked(Achievement.PROMOTED));
        assertTrue(goals.isUnlocked(Achievement.GOOD_FRIENDS));
        assertTrue(goals.isUnlocked(Achievement.MARRIED));
        assertTrue(goals.isUnlocked(Achievement.FORTUNE));
        assertFalse(goals.isUnlocked(Achievement.TOP_TIER));
        assertFalse(goals.isUnlocked(Achievement.HOMEOWNER));

        // achievements stay unlocked even if the state changes back
        ann.getCareerManager().changeJob(Job.UNEMPLOYED, "Ann");
        goals.check(household);
        assertTrue(goals.isUnlocked(Achievement.FIRST_JOB));
    }

    @Test
    @DisplayName("Goals, townsfolk and their relationships survive a save and reload")
    void testGoalsSaved() {
        String worldName = "JUnitGoals";
        SaveManager.deleteSave(worldName);
        try {
            Sim sim = new Sim("Sam", 30, Gender.MALE, Job.UNEMPLOYED);
            sim.setAspiration(Aspiration.SOCIAL_BUTTERFLY);
            sim.setAspirationFulfilled(true);
            GameEngine engine = new GameEngine(worldName, 10, new ArrayList<>(List.of(sim)), false);
            engine.getGoalManager().restoreUnlocked(Achievement.MARRIED);
            engine.getGoalManager().setLegacyComplete(true);
            NPCSim absent = new NPCSim("Away", 40, Gender.FEMALE, Job.BARISTA);
            engine.getNpcManager().addTownsperson(absent);
            sim.getRelationshipManager().increaseRelationship(absent, 42);

            SaveManager.saveGame(engine, worldName);
            GameEngine loaded = SaveManager.loadGame(worldName);

            assertNotNull(loaded);
            Sim loadedSim = loaded.getNeighborhood().get(0);
            assertEquals(Aspiration.SOCIAL_BUTTERFLY, loadedSim.getAspiration());
            assertTrue(loadedSim.isAspirationFulfilled());
            assertTrue(loaded.getGoalManager().isUnlocked(Achievement.MARRIED));
            assertTrue(loaded.getGoalManager().isLegacyComplete());

            List<NPCSim> townsfolk = loaded.getNpcManager().getTownsfolk();
            assertEquals(1, townsfolk.size());
            NPCSim loadedAbsent = townsfolk.get(0);
            assertEquals("Away", loadedAbsent.getName());
            assertEquals(Job.BARISTA, loadedAbsent.getCareer());
            assertEquals(42, loadedSim.getRelationshipManager().getRelationship(loadedAbsent));
        } finally {
            SaveManager.deleteSave(worldName);
        }
    }
}
