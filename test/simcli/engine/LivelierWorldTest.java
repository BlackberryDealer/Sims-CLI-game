package simcli.engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.entities.actors.NPCSim;
import simcli.entities.actors.Sim;
import simcli.entities.managers.NPCManager;
import simcli.entities.models.ActionState;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.entities.models.SkillType;
import simcli.entities.models.SocialAction;
import simcli.entities.models.Trait;
import simcli.utils.GameConstants;
import simcli.utils.GameRandom;
import simcli.world.Building;
import simcli.world.Residential;
import simcli.world.Room;
import simcli.world.interactables.Fridge;
import simcli.world.interactables.Interactable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for a livelier world: rotating park visitors who age and have
 * personalities, faster-growing children with school and part-time jobs,
 * and household-wide events.
 */
@DisplayName("Livelier World — Park, Children and Household Events")
public class LivelierWorldTest {

    private SimulationLogger logger;

    @BeforeEach
    void setUp() {
        logger = new SimulationLogger();
        SimulationLogger.setInstance(logger);
    }

    @AfterEach
    void tearDown() {
        GameRandom.RANDOM.setSeed(System.nanoTime());
    }

    // -----------------------------------------------------------------------
    // Park
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Each morning some visitors leave and are replaced, keeping the park full")
    void testVisitorsRotate() {
        NPCManager npcs = new NPCManager();
        npcs.replenishNPCs(GameConstants.MAX_NPCS);
        Set<NPCSim> yesterday = new HashSet<>(npcs.getActiveNPCs());

        npcs.startNewDay(2, logger);

        assertEquals(GameConstants.MAX_NPCS, npcs.getActiveNPCs().size());
        long stayed = npcs.getActiveNPCs().stream().filter(yesterday::contains).count();
        assertEquals(GameConstants.MAX_NPCS - GameConstants.DAILY_VISITORS_LEAVING, stayed);
        assertEquals(GameConstants.DAILY_VISITORS_LEAVING, npcs.getTownsfolk().size(),
                "People who left are remembered as townsfolk");
    }

    @Test
    @DisplayName("Townsfolk come back to the park on later days")
    void testTownsfolkReturn() {
        GameRandom.RANDOM.setSeed(7);
        NPCManager npcs = new NPCManager();
        npcs.replenishNPCs(GameConstants.MAX_NPCS);
        NPCSim friend = npcs.getActiveNPCs().get(0);

        boolean leftAndCameBack = false;
        boolean away = false;
        for (int day = 2; day < 60 && !leftAndCameBack; day++) {
            npcs.startNewDay(day, logger);
            if (npcs.getTownsfolk().contains(friend)) {
                away = true;
            } else if (away && npcs.getActiveNPCs().contains(friend)) {
                leftAndCameBack = true;
            }
        }
        assertTrue(leftAndCameBack, "A visitor who left should eventually come back");
    }

    @Test
    @DisplayName("NPCs age every few days and move away when old")
    void testNpcsAgeAndMoveAway() {
        NPCManager npcs = new NPCManager();
        NPCSim young = new NPCSim("Young", 30, Gender.MALE, Job.UNEMPLOYED);
        NPCSim old = new NPCSim("Old", GameConstants.NPC_MOVE_AWAY_AGE - 1, Gender.FEMALE, Job.UNEMPLOYED);
        npcs.addNPC(young);
        npcs.addNPC(old);

        npcs.startNewDay(GameConstants.DAYS_PER_AGE_TICK, logger);

        assertEquals(31, young.getAge());
        assertFalse(npcs.getActiveNPCs().contains(old));
        assertFalse(npcs.getTownsfolk().contains(old), "They've left town entirely");
    }

    @Test
    @DisplayName("Marrying an NPC removes them from town")
    void testRemoveClearsTownsfolk() {
        NPCManager npcs = new NPCManager();
        NPCSim npc = new NPCSim("Nat", 30, Gender.MALE, Job.UNEMPLOYED);
        npcs.addTownsperson(npc);
        npcs.removeNPC(npc);
        assertTrue(npcs.getTownsfolk().isEmpty());
    }

    @Test
    @DisplayName("Socialites are easier to win over and Lazy Sims harder")
    void testTargetTraitsChangeOdds() {
        Sim talker = withTrait(new Sim("Tal", 30, Gender.FEMALE, Job.UNEMPLOYED), Trait.ACTIVE);
        Sim socialite = withTrait(new Sim("Soc", 30, Gender.MALE, Job.UNEMPLOYED), Trait.SOCIALITE);
        Sim lazy = withTrait(new Sim("Laz", 30, Gender.MALE, Job.UNEMPLOYED), Trait.LAZY);

        int base = GameConstants.JOKE_BASE_SUCCESS_CHANCE;
        assertEquals(base + GameConstants.SOCIALITE_TARGET_BONUS,
                talker.getRelationshipManager().getSuccessChance(socialite, SocialAction.JOKE));
        assertEquals(base - GameConstants.LAZY_TARGET_PENALTY,
                talker.getRelationshipManager().getSuccessChance(lazy, SocialAction.JOKE));
        assertEquals(100, talker.getRelationshipManager().getSuccessChance(lazy, SocialAction.CHAT),
                "Chats always land, whoever you talk to");
    }

    private static Sim withTrait(Sim sim, Trait trait) {
        sim.getTraits().clear();
        sim.getTraits().add(trait);
        return sim;
    }

    // -----------------------------------------------------------------------
    // Children
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Children age a year every day, so a baby is a teen in 13 days")
    void testChildrenGrowFast() {
        LifecycleManager lm = new LifecycleManager(GameConstants.DAYS_PER_AGE_TICK, logger);
        Sim baby = new Sim("Bub", 0, Gender.MALE, Job.UNEMPLOYED);
        baby.setChildSim(true);

        for (int day = 0; day < GameConstants.TEEN_AGE; day++) {
            lm.processDay(baby);
        }

        assertEquals(GameConstants.TEEN_AGE, baby.getAge());
        assertTrue(baby.isPlayable());

        for (int day = 0; day < GameConstants.DAYS_PER_AGE_TICK; day++) {
            lm.processDay(baby);
        }
        assertEquals(GameConstants.TEEN_AGE + 1, baby.getAge(),
                "Teens age at the normal rate: one year per " + GameConstants.DAYS_PER_AGE_TICK + " days");
    }

    @Test
    @DisplayName("Teens go to school on weekdays and gain Logic")
    void testTeensGoToSchool() {
        Sim parent = new Sim("Pat", 40, Gender.FEMALE, Job.UNEMPLOYED);
        Sim teen = new Sim("Tim", 15, Gender.MALE, Job.UNEMPLOYED);
        teen.getHappiness().setValue(40); // an ordinary mood: no great-mood bonus to the XP
        teen.getSocial().setValue(40);
        List<Sim> household = new ArrayList<>(List.of(parent, teen));
        // Monday 23:00 — the first hour crosses into Tuesday, so Monday was a school day
        new GameLoop(new TimeManager(23, 24), household, logger).processTick(parent, 1);

        assertEquals(GameConstants.SCHOOL_LOGIC_XP, teen.getSkillManager().getExperience(SkillType.LOGIC));
        assertEquals(0, parent.getSkillManager().getExperience(SkillType.LOGIC), "Adults don't go to school");
    }

    @Test
    @DisplayName("No school at the weekend")
    void testNoSchoolOnWeekends() {
        Sim parent = new Sim("Pat", 40, Gender.FEMALE, Job.UNEMPLOYED);
        Sim teen = new Sim("Tim", 15, Gender.MALE, Job.UNEMPLOYED);
        List<Sim> household = new ArrayList<>(List.of(parent, teen));
        // Saturday (day 6) 23:00 into Sunday
        new GameLoop(new TimeManager(5 * 24 + 23, 24), household, logger).processTick(parent, 1);

        assertEquals(0, teen.getSkillManager().getExperience(SkillType.LOGIC));
    }

    @Test
    @DisplayName("Teens can work part-time jobs but not adult careers")
    void testPartTimeJobs() {
        assertTrue(Job.BARISTA.isPartTime());
        assertTrue(Job.LIFEGUARD.isPartTime());
        assertTrue(Job.LIBRARY_ASSISTANT.isPartTime());
        assertFalse(Job.SOFTWARE_ENGINEER.isPartTime());
        assertFalse(Job.UNEMPLOYED.isPartTime());

        Sim teen = new Sim("Tim", 15, Gender.MALE, Job.BARISTA);
        teen.getHappiness().setValue(40); // an ordinary mood: no great-mood bonus to the pay
        teen.getSocial().setValue(40);
        assertTrue(teen.canWork());
        assertTrue(teen.performWork().isSuccess());
        assertEquals(Job.BARISTA.getSalaryAtTier(1), teen.getMoney() - GameConstants.STARTING_MONEY);
    }

    // -----------------------------------------------------------------------
    // Household events
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Which household events can happen depends on the time")
    void testEventsDependOnTime() {
        assertTrue(RandomEventManager.eventsAt(new TimeManager(8, 24))
                .contains(RandomEventManager.HouseholdEvent.SUNNY_MORNING));
        assertFalse(RandomEventManager.eventsAt(new TimeManager(8, 24))
                .contains(RandomEventManager.HouseholdEvent.POWER_CUT));
        assertTrue(RandomEventManager.eventsAt(new TimeManager(23, 24))
                .contains(RandomEventManager.HouseholdEvent.POWER_CUT));
        // day 1 is a Monday: no weekend party
        assertFalse(RandomEventManager.eventsAt(new TimeManager(19, 24))
                .contains(RandomEventManager.HouseholdEvent.WEEKEND_PARTY));
        // day 6 is a Saturday
        assertTrue(RandomEventManager.eventsAt(new TimeManager(5 * 24 + 19, 24))
                .contains(RandomEventManager.HouseholdEvent.WEEKEND_PARTY));
    }

    @Test
    @DisplayName("A weekend party lifts everyone who's awake")
    void testWeekendPartyHitsHousehold() {
        Sim a = new Sim("Ann", 30, Gender.FEMALE, Job.UNEMPLOYED);
        Sim b = new Sim("Bob", 30, Gender.MALE, Job.UNEMPLOYED);
        Sim sleeper = new Sim("Zed", 30, Gender.MALE, Job.UNEMPLOYED);
        for (Sim s : List.of(a, b, sleeper)) {
            s.getSocial().setValue(40);
        }
        sleeper.setCurrentAction(ActionState.SLEEPING);

        new RandomEventManager(logger).fireHouseholdEvent(RandomEventManager.HouseholdEvent.WEEKEND_PARTY,
                List.of(a, b, sleeper), null);

        assertEquals(65, a.getSocial().getValue());
        assertEquals(65, b.getSocial().getValue());
        assertEquals(40, sleeper.getSocial().getValue(), "Sleepers miss the party");
    }

    @Test
    @DisplayName("A power cut spoils fridge food; a neighbour's pie fills it")
    void testFridgeEvents() {
        Sim a = new Sim("Ann", 30, Gender.FEMALE, Job.UNEMPLOYED);
        List<Sim> household = new ArrayList<>(List.of(a));
        WorldManager world = new WorldManager(new NPCManager(), household);
        world.setupWorld();
        Fridge fridge = null;
        for (Building b : world.getCityMap()) {
            if (b instanceof Residential) {
                for (Room r : ((Residential) b).getRooms()) {
                    for (Interactable it : r.getInteractables()) {
                        if (it instanceof Fridge && fridge == null) fridge = (Fridge) it;
                    }
                }
            }
        }
        assertNotNull(fridge);
        RandomEventManager events = new RandomEventManager(logger);

        events.fireHouseholdEvent(RandomEventManager.HouseholdEvent.NEIGHBOUR_PIE, household, world);
        assertEquals(1, fridge.getStoredItems().size());

        fridge.getStoredItems().clear();
        fridge.storeItem(new simcli.entities.items.Food("Apple", 10, 15, 5, 2));
        events.fireHouseholdEvent(RandomEventManager.HouseholdEvent.POWER_CUT, household, world);
        assertTrue(fridge.getStoredItems().isEmpty(), "Food with 2 days left should spoil in a power cut");
    }

    @Test
    @DisplayName("Household events skip babies and the dead")
    void testEventsSkipBabiesAndDead() {
        Sim dead = new Sim("Dee", 30, Gender.FEMALE, Job.UNEMPLOYED);
        dead.markAsDead();
        Sim baby = new Sim("Bub", 0, Gender.MALE, Job.UNEMPLOYED);
        baby.setChildSim(true);
        dead.getEnergy().setValue(50);
        baby.getEnergy().setValue(50);

        new RandomEventManager(logger).fireHouseholdEvent(RandomEventManager.HouseholdEvent.FLU,
                List.of(dead, baby), null);

        assertEquals(50, dead.getEnergy().getValue());
        assertEquals(50, baby.getEnergy().getValue());
    }
}
