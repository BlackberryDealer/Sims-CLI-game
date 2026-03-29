package simcli.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import simcli.entities.actors.Sim;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link GameLoop} — per-tick processing, day boundary detection,
 * and integration with LifecycleManager and TimeManager.
 *
 * <p>Coverage:
 * <ul>
 *     <li>processTick() advances the clock by the given number of ticks</li>
 *     <li>processTick() calls tick() on every Sim in the neighborhood</li>
 *     <li>Day boundary triggers lifecycle processing (daysAlive increments)</li>
 *     <li>Multi-tick sleep crossing multiple day boundaries</li>
 *     <li>Owned subsystem getters return non-null</li>
 * </ul>
 */
@DisplayName("GameLoop — Per-Tick Processing & Day Boundaries")
public class GameLoopTest {

    private SimulationLogger logger;

    @BeforeEach
    void setUp() {
        logger = new SimulationLogger();
        SimulationLogger.setInstance(logger);
    }

    // -----------------------------------------------------------------------
    // Subsystem ownership
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Subsystem Ownership")
    class SubsystemTests {

        @Test
        @DisplayName("getRandomEventManager() returns a non-null instance")
        void testRandomEventManagerNotNull() {
            TimeManager tm = new TimeManager(1, 24);
            List<Sim> neighborhood = new ArrayList<>();
            neighborhood.add(new Sim("Test", 25, Gender.MALE, Job.UNEMPLOYED));
            GameLoop loop = new GameLoop(tm, neighborhood, logger);

            assertNotNull(loop.getRandomEventManager(),
                    "GameLoop should own a RandomEventManager");
        }

        @Test
        @DisplayName("getLifecycleManager() returns a non-null instance")
        void testLifecycleManagerNotNull() {
            TimeManager tm = new TimeManager(1, 24);
            List<Sim> neighborhood = new ArrayList<>();
            neighborhood.add(new Sim("Test", 25, Gender.FEMALE, Job.UNEMPLOYED));
            GameLoop loop = new GameLoop(tm, neighborhood, logger);

            assertNotNull(loop.getLifecycleManager(),
                    "GameLoop should own a LifecycleManager");
        }
    }

    // -----------------------------------------------------------------------
    // processTick() — clock advancement
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Tick Processing")
    class TickProcessingTests {

        @Test
        @DisplayName("processTick(1) advances the clock by exactly 1 tick")
        void testProcessTickByOne() {
            TimeManager tm = new TimeManager(1, 24);
            Sim sim = new Sim("Alice", 25, Gender.FEMALE, Job.UNEMPLOYED);
            List<Sim> neighborhood = new ArrayList<>();
            neighborhood.add(sim);
            GameLoop loop = new GameLoop(tm, neighborhood, logger);

            int tickBefore = tm.getCurrentTick();
            loop.processTick(sim, 1);

            assertEquals(tickBefore + 1, tm.getCurrentTick(),
                    "processTick(1) should advance clock by exactly 1");
        }

        @Test
        @DisplayName("processTick(5) advances the clock by 5 ticks")
        void testProcessTickByFive() {
            TimeManager tm = new TimeManager(1, 24);
            Sim sim = new Sim("Bob", 25, Gender.MALE, Job.UNEMPLOYED);
            List<Sim> neighborhood = new ArrayList<>();
            neighborhood.add(sim);
            GameLoop loop = new GameLoop(tm, neighborhood, logger);

            int tickBefore = tm.getCurrentTick();
            loop.processTick(sim, 5);

            assertEquals(tickBefore + 5, tm.getCurrentTick(),
                    "processTick(5) should advance clock by exactly 5");
        }
    }

    // -----------------------------------------------------------------------
    // Need decay — tick() is called on all Sims
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("processTick() decays needs for all Sims in the neighborhood")
    void testProcessTickDecaysAllSimNeeds() {
        TimeManager tm = new TimeManager(1, 24);
        Sim alice = new Sim("Alice", 25, Gender.FEMALE, Job.UNEMPLOYED);
        Sim bob = new Sim("Bob", 30, Gender.MALE, Job.UNEMPLOYED);
        List<Sim> neighborhood = new ArrayList<>(Arrays.asList(alice, bob));
        GameLoop loop = new GameLoop(tm, neighborhood, logger);

        int aliceHungerBefore = alice.getHunger().getValue();
        int bobHungerBefore = bob.getHunger().getValue();

        loop.processTick(alice, 1);

        // Hunger should have decayed for both Sims (both start at 100)
        assertTrue(alice.getHunger().getValue() < aliceHungerBefore,
                "Alice's hunger should have decayed after processTick");
        assertTrue(bob.getHunger().getValue() < bobHungerBefore,
                "Bob's hunger should have decayed after processTick");
    }

    @Test
    @DisplayName("processTick() does NOT decay needs for dead Sims")
    void testProcessTickSkipsDeadSims() {
        TimeManager tm = new TimeManager(1, 24);
        Sim alive = new Sim("Alive", 25, Gender.FEMALE, Job.UNEMPLOYED);
        Sim dead = new Sim("Dead", 25, Gender.MALE, Job.UNEMPLOYED);
        dead.markAsDead();
        int deadHungerBefore = dead.getHunger().getValue();

        List<Sim> neighborhood = new ArrayList<>(Arrays.asList(alive, dead));
        GameLoop loop = new GameLoop(tm, neighborhood, logger);

        loop.processTick(alive, 1);

        assertEquals(deadHungerBefore, dead.getHunger().getValue(),
                "Dead Sim's hunger should remain unchanged");
    }

    // -----------------------------------------------------------------------
    // Day boundary detection
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Day Boundary Detection")
    class DayBoundaryTests {

        @Test
        @DisplayName("Crossing a day boundary triggers lifecycle processing (daysAlive increments)")
        void testDayBoundaryCrossed() {
            // Start at tick 23 (end of day 1 with 24 ticks/day)
            TimeManager tm = new TimeManager(23, 24);
            Sim sim = new Sim("Charlie", 25, Gender.MALE, Job.UNEMPLOYED);
            List<Sim> neighborhood = new ArrayList<>();
            neighborhood.add(sim);
            GameLoop loop = new GameLoop(tm, neighborhood, logger);

            int daysBefore = sim.getDaysAlive();

            // Advance 1 tick: 23 → 24, crossing from day 1 to day 2
            loop.processTick(sim, 1);

            assertEquals(daysBefore + 1, sim.getDaysAlive(),
                    "Crossing a day boundary should increment daysAlive via LifecycleManager");
        }

        @Test
        @DisplayName("Staying within the same day does NOT trigger lifecycle processing")
        void testNoDayBoundaryCrossed() {
            // Start at tick 5 (well within day 1)
            TimeManager tm = new TimeManager(5, 24);
            Sim sim = new Sim("Diana", 25, Gender.FEMALE, Job.UNEMPLOYED);
            List<Sim> neighborhood = new ArrayList<>();
            neighborhood.add(sim);
            GameLoop loop = new GameLoop(tm, neighborhood, logger);

            int daysBefore = sim.getDaysAlive();

            // Advance 1 tick: 5 → 6 (still day 1)
            loop.processTick(sim, 1);

            assertEquals(daysBefore, sim.getDaysAlive(),
                    "Staying within the same day should NOT increment daysAlive");
        }

        @Test
        @DisplayName("Sleep crossing multiple day boundaries processes each day")
        void testMultipleDayBoundariesCrossed() {
            // Start at tick 23 (end of day 1)
            TimeManager tm = new TimeManager(23, 24);
            Sim sim = new Sim("Eve", 25, Gender.FEMALE, Job.UNEMPLOYED);
            List<Sim> neighborhood = new ArrayList<>();
            neighborhood.add(sim);
            GameLoop loop = new GameLoop(tm, neighborhood, logger);

            int daysBefore = sim.getDaysAlive();

            // Advance 25 ticks: 23 → 48 — crosses day 1→2 and day 2→3
            loop.processTick(sim, 25);

            assertEquals(daysBefore + 2, sim.getDaysAlive(),
                    "Crossing 2 day boundaries should increment daysAlive by 2");
        }
    }
}
