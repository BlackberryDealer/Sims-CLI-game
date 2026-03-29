package simcli.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import simcli.entities.actors.Sim;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.entities.models.SimState;
import simcli.utils.GameConstants;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RandomEventManager} — event triggering behaviour,
 * dead Sim exclusion, and manager instantiation.
 *
 * <p>Because random events involve randomness (5% trigger chance), we
 * take a statistical approach where needed: run many iterations and
 * verify events occur at least once. For deterministic tests,
 * we verify dead Sim exclusion and that the method never throws.</p>
 *
 * <p>Coverage:
 * <ul>
 *     <li>trigger() does not throw for healthy Sims</li>
 *     <li>trigger() skips dead Sims entirely</li>
 *     <li>Over many runs, at least one event fires (statistical)</li>
 *     <li>Event effects never push values below 0 or above MAX</li>
 * </ul>
 */
@DisplayName("RandomEventManager — Random Event Logic")
public class RandomEventManagerTest {

    private SimulationLogger logger;
    private RandomEventManager eventManager;

    @BeforeEach
    void setUp() {
        logger = new SimulationLogger();
        SimulationLogger.setInstance(logger);
        eventManager = new RandomEventManager(logger);
    }

    // -----------------------------------------------------------------------
    // Basic trigger safety
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("trigger() does not throw for a healthy Sim")
    void testTriggerDoesNotThrow() {
        Sim sim = new Sim("Alice", 25, Gender.FEMALE, Job.UNEMPLOYED);
        TimeManager tm = new TimeManager(1, 24);

        assertDoesNotThrow(() -> eventManager.trigger(sim, tm),
                "trigger() should never throw for a healthy Sim");
    }

    @Test
    @DisplayName("trigger() can be called repeatedly without error")
    void testTriggerMultipleTimes() {
        Sim sim = new Sim("Bob", 30, Gender.MALE, Job.SOFTWARE_ENGINEER);
        TimeManager tm = new TimeManager(1, 24);

        assertDoesNotThrow(() -> {
            for (int i = 0; i < 100; i++) {
                eventManager.trigger(sim, tm);
            }
        }, "trigger() should be safe to call many times");
    }

    // -----------------------------------------------------------------------
    // Dead Sim exclusion
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Dead Sim Handling")
    class DeadSimTests {

        @Test
        @DisplayName("trigger() does not modify dead Sim's money")
        void testDeadSimMoneyUnchanged() {
            Sim dead = new Sim("Dead", 25, Gender.MALE, Job.UNEMPLOYED);
            dead.markAsDead();
            int moneyBefore = dead.getMoney();
            TimeManager tm = new TimeManager(1, 24);

            for (int i = 0; i < 1000; i++) {
                eventManager.trigger(dead, tm);
            }

            assertEquals(moneyBefore, dead.getMoney(),
                    "Dead Sim's money should be untouched after many event attempts");
        }

        @Test
        @DisplayName("trigger() does not modify dead Sim's needs")
        void testDeadSimNeedsUnchanged() {
            Sim dead = new Sim("Dead", 25, Gender.FEMALE, Job.UNEMPLOYED);
            dead.markAsDead();
            int hungerBefore = dead.getHunger().getValue();
            int energyBefore = dead.getEnergy().getValue();
            int happinessBefore = dead.getHappiness().getValue();
            TimeManager tm = new TimeManager(1, 24);

            for (int i = 0; i < 1000; i++) {
                eventManager.trigger(dead, tm);
            }

            assertEquals(hungerBefore, dead.getHunger().getValue());
            assertEquals(energyBefore, dead.getEnergy().getValue());
            assertEquals(happinessBefore, dead.getHappiness().getValue());
        }
    }

    // -----------------------------------------------------------------------
    // Statistical event occurrence
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Over 1000 triggers, at least one event fires (modifies stats)")
    void testAtLeastOneEventFires() {
        Sim sim = new Sim("Lucky", 25, Gender.MALE, Job.UNEMPLOYED);
        TimeManager tm = new TimeManager(1, 24);
        int initialMoney = sim.getMoney();
        int initialHunger = sim.getHunger().getValue();
        int initialEnergy = sim.getEnergy().getValue();
        int initialHappiness = sim.getHappiness().getValue();
        int initialHygiene = sim.getHygiene().getValue();

        for (int i = 0; i < 1000; i++) {
            eventManager.trigger(sim, tm);
        }

        // With a 5% trigger chance over 1000 calls, the probability of
        // zero events is ~(0.95)^1000 ≈ 5.3e-23 — effectively impossible.
        boolean somethingChanged =
                sim.getMoney() != initialMoney ||
                sim.getHunger().getValue() != initialHunger ||
                sim.getEnergy().getValue() != initialEnergy ||
                sim.getHappiness().getValue() != initialHappiness ||
                sim.getHygiene().getValue() != initialHygiene;

        assertTrue(somethingChanged,
                "At least one random event should have fired over 1000 trigger attempts");
    }

    // -----------------------------------------------------------------------
    // Event value boundaries
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Events never push need values below 0")
    void testEventsRespectMinimumBounds() {
        // Start with very low needs to stress-test floor clamping
        Sim sim = new Sim("Low", 25, Gender.MALE, Job.UNEMPLOYED);
        sim.getHunger().setValue(5);
        sim.getEnergy().setValue(5);
        sim.getHappiness().setValue(5);
        sim.getHygiene().setValue(5);
        sim.getSocial().setValue(5);
        TimeManager tm = new TimeManager(1, 24);

        for (int i = 0; i < 1000; i++) {
            eventManager.trigger(sim, tm);
        }

        assertTrue(sim.getHunger().getValue() >= 0, "Hunger should never go below 0");
        assertTrue(sim.getEnergy().getValue() >= 0, "Energy should never go below 0");
        assertTrue(sim.getHappiness().getValue() >= 0, "Happiness should never go below 0");
        assertTrue(sim.getHygiene().getValue() >= 0, "Hygiene should never go below 0");
        assertTrue(sim.getSocial().getValue() >= 0, "Social should never go below 0");
    }

    @Test
    @DisplayName("Events never push need values above MAX_VALUE (100)")
    void testEventsRespectMaximumBounds() {
        Sim sim = new Sim("Max", 25, Gender.FEMALE, Job.UNEMPLOYED);
        // Needs start at 100 (MAX_VALUE)
        TimeManager tm = new TimeManager(1, 24);

        for (int i = 0; i < 1000; i++) {
            eventManager.trigger(sim, tm);
        }

        assertTrue(sim.getHunger().getValue() <= 100, "Hunger should never exceed 100");
        assertTrue(sim.getEnergy().getValue() <= 100, "Energy should never exceed 100");
        assertTrue(sim.getHappiness().getValue() <= 100, "Happiness should never exceed 100");
        assertTrue(sim.getHygiene().getValue() <= 100, "Hygiene should never exceed 100");
        assertTrue(sim.getSocial().getValue() <= 100, "Social should never exceed 100");
    }
}
