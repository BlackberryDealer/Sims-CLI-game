package simcli.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.world.Residential;
import simcli.world.Room;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for time-keeping bugs: shifts crossing midnight, hourly
 * need decay, truancy on weekends / worked days, and the active Sim's room
 * drifting out of the household's current building.
 */
@DisplayName("Regression — Time-keeping, Truancy & Location")
public class TimekeepingRegressionTest {

    private SimulationLogger logger;
    /** Receives random events, so they can't disturb the Sims under test. */
    private Sim bystander;

    @BeforeEach
    void setUp() {
        logger = new SimulationLogger();
        SimulationLogger.setInstance(logger);
        bystander = new Sim("Bystander", 30, Gender.MALE, Job.UNEMPLOYED);
    }

    private GameLoop loopFor(TimeManager tm, Sim... sims) {
        return new GameLoop(tm, new ArrayList<>(Arrays.asList(sims)), logger);
    }

    @Test
    @DisplayName("A shift that crosses midnight still processes the new day")
    void testShiftAcrossMidnightProcessesDay() {
        TimeManager tm = new TimeManager(22, 24); // Monday 22:00
        Sim worker = new Sim("Worker", 25, Gender.FEMALE, Job.HARDWARE_TECHNICIAN);
        GameLoop loop = loopFor(tm, worker);

        worker.performWork();
        assertEquals(1, worker.getShiftsWorkedToday());

        loop.processTick(bystander, Job.HARDWARE_TECHNICIAN.getWorkingHours()); // 5h → Tuesday 03:00

        assertEquals(2, tm.getCurrentDay());
        assertEquals(1, worker.getDaysAlive(), "The day boundary crossed during the shift must be processed");
        assertEquals(0, worker.getShiftsWorkedToday(), "The new day should reset the shift counter");
        assertEquals(0, worker.getCareerManager().getConsecutiveDaysMissed(),
                "Monday was worked, so it must not count as missed");
    }

    @Test
    @DisplayName("Needs decay once per hour advanced, not once per turn")
    void testNeedsDecayPerHour() {
        Sim oneHour = new Sim("One", 25, Gender.MALE, Job.UNEMPLOYED);
        Sim fiveHours = new Sim("Five", 25, Gender.MALE, Job.UNEMPLOYED);

        loopFor(new TimeManager(1, 24), oneHour).processTick(bystander, 1);
        loopFor(new TimeManager(1, 24), fiveHours).processTick(bystander, 5);

        int oneHourLoss = 100 - oneHour.getHunger().getValue();
        int fiveHourLoss = 100 - fiveHours.getHunger().getValue();
        assertEquals(5 * oneHourLoss, fiveHourLoss, "Five hours should cost five times one hour of hunger");
    }

    @Test
    @DisplayName("Sleeping through the night never raises hunger or drains social")
    void testSleepingHours() {
        Sim sleeper = new Sim("Sleeper", 25, Gender.MALE, Job.UNEMPLOYED);
        sleeper.getHunger().setValue(60);
        sleeper.getSocial().setValue(60);
        TimeManager tm = new TimeManager(22, 24);
        GameLoop loop = loopFor(tm, sleeper);

        sleeper.setCurrentAction(ActionState.SLEEPING);
        loop.processTick(sleeper, 10);

        assertTrue(sleeper.getHunger().getValue() <= 60, "Hunger must not go up while sleeping");
        // 10 idle hours would cost 50 social; allow for a random "mild cold" event (-10).
        assertTrue(sleeper.getSocial().getValue() >= 50, "Social should not decay while asleep");
    }

    @Test
    @DisplayName("Inactive Sims are reset to IDLE so a past action stops affecting them")
    void testInactiveSimsResetToIdle() {
        Sim active = new Sim("Active", 25, Gender.MALE, Job.UNEMPLOYED);
        Sim other = new Sim("Other", 25, Gender.FEMALE, Job.UNEMPLOYED);
        other.setCurrentAction(ActionState.SLEEPING);

        loopFor(new TimeManager(1, 24), active, other).processTick(active, 1);

        assertEquals(ActionState.IDLE, other.getCurrentAction());
    }

    @Test
    @DisplayName("Missing a weekday counts as truancy")
    void testMissedWeekdayCounts() {
        TimeManager tm = new TimeManager(23, 24); // Monday 23:00
        Sim worker = new Sim("Worker", 25, Gender.MALE, Job.SOFTWARE_ENGINEER);

        loopFor(tm, worker).processTick(bystander, 1);

        assertEquals(1, worker.getCareerManager().getConsecutiveDaysMissed());
    }

    @Test
    @DisplayName("Weekend days never count as missed work")
    void testWeekendNotTruancy() {
        TimeManager tm = new TimeManager(5 * 24 + 23, 24); // Saturday 23:00
        assertEquals("Saturday", tm.getDayOfWeek());
        Sim worker = new Sim("Worker", 25, Gender.MALE, Job.SOFTWARE_ENGINEER);

        loopFor(tm, worker).processTick(bystander, 25); // through Sunday into Monday

        assertEquals("Monday", tm.getDayOfWeek());
        assertEquals(0, worker.getCareerManager().getConsecutiveDaysMissed());
        assertEquals(Job.SOFTWARE_ENGINEER, worker.getCareer());
    }

    @Test
    @DisplayName("Dead Sims are not checked for truancy")
    void testDeadSimsSkipTruancy() {
        TimeManager tm = new TimeManager(23, 24);
        Sim ghost = new Sim("Ghost", 25, Gender.MALE, Job.SOFTWARE_ENGINEER);
        ghost.markAsDead();

        loopFor(tm, ghost).processTick(bystander, 1);

        assertEquals(0, ghost.getCareerManager().getConsecutiveDaysMissed());
    }

    @Test
    @DisplayName("TimeManager.isWeekend() matches the day-of-week names")
    void testIsWeekend() {
        for (int day = 1; day <= 14; day++) {
            String name = TimeManager.dayOfWeekName(day);
            boolean weekendByName = name.equals("Saturday") || name.equals("Sunday");
            assertEquals(weekendByName, TimeManager.isWeekend(day), "day " + day);
        }
    }

    @Test
    @DisplayName("Switching to a Sim whose room is in another building moves them into the current one")
    void testActivePlayerSyncedToCurrentBuilding() {
        Sim a = new Sim("A", 25, Gender.MALE, Job.UNEMPLOYED);
        Sim b = new Sim("B", 25, Gender.FEMALE, Job.UNEMPLOYED);
        GameEngine engine = new GameEngine("SyncTest", 1, new ArrayList<>(List.of(a, b)), false);

        Residential dorm = (Residential) engine.getWorldManager().getCityMap().get(0);
        Residential bungalow = (Residential) engine.getWorldManager().getCityMap().get(1);
        b.setCurrentRoom(dorm.getRooms().get(0));

        bungalow.setOwned(true);
        engine.getWorldManager().setCurrentLocation(bungalow);
        engine.setActivePlayer(b);
        engine.syncActivePlayerToLocation();

        assertTrue(bungalow.getRooms().contains(b.getCurrentRoom()),
                "B must be placed in a Bungalow room, not left in the Dorm");

        Room study = bungalow.getRooms().get(1);
        b.setCurrentRoom(study);
        engine.syncActivePlayerToLocation();
        assertSame(study, b.getCurrentRoom(), "A room already in the current building is kept");
    }
}
