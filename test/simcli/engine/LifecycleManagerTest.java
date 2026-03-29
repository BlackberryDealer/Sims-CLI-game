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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link LifecycleManager} — aging, death, retirement, and pension logic.
 *
 * <p>Coverage:
 * <ul>
 *     <li>Constructor validation (daysPerAgeTick >= 1)</li>
 *     <li>processDay() increments daysAlive</li>
 *     <li>Age-up triggers every daysPerAgeTick days</li>
 *     <li>Death at DEATH_AGE (90)</li>
 *     <li>Pension income for unemployed elders</li>
 *     <li>Forced retirement when exceeding job max age</li>
 *     <li>processDayForAll() skips dead Sims</li>
 *     <li>Null Sim rejection</li>
 * </ul>
 */
@DisplayName("LifecycleManager — Aging, Death & Retirement")
public class LifecycleManagerTest {

    private SimulationLogger logger;

    @BeforeEach
    void setUp() {
        logger = new SimulationLogger();
        SimulationLogger.setInstance(logger);
    }

    // -----------------------------------------------------------------------
    // Constructor validation
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Constructor throws IllegalArgumentException when daysPerAgeTick < 1")
    void testConstructorRejectsZeroDaysPerAgeTick() {
        assertThrows(IllegalArgumentException.class, () ->
                new LifecycleManager(0, logger),
                "daysPerAgeTick of 0 should be rejected");
    }

    @Test
    @DisplayName("Constructor throws IllegalArgumentException when daysPerAgeTick is negative")
    void testConstructorRejectsNegativeDaysPerAgeTick() {
        assertThrows(IllegalArgumentException.class, () ->
                new LifecycleManager(-5, logger),
                "Negative daysPerAgeTick should be rejected");
    }

    @Test
    @DisplayName("Constructor accepts daysPerAgeTick of 1")
    void testConstructorAcceptsMinimumDaysPerAgeTick() {
        LifecycleManager lm = new LifecycleManager(1, logger);
        assertEquals(1, lm.getDaysPerAgeTick());
    }

    // -----------------------------------------------------------------------
    // processDay() — day increment and daily reset
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("processDay() increments Sim's daysAlive by 1")
    void testProcessDayIncrementsDaysAlive() {
        LifecycleManager lm = new LifecycleManager(GameConstants.DAYS_PER_AGE_TICK, logger);
        Sim sim = new Sim("Alice", 25, Gender.FEMALE, Job.UNEMPLOYED);
        int daysBefore = sim.getDaysAlive();

        lm.processDay(sim);

        assertEquals(daysBefore + 1, sim.getDaysAlive(),
                "processDay should increment daysAlive by 1");
    }

    @Test
    @DisplayName("processDay() rejects null Sim")
    void testProcessDayRejectsNull() {
        LifecycleManager lm = new LifecycleManager(3, logger);
        assertThrows(IllegalArgumentException.class, () -> lm.processDay(null),
                "null Sim should throw IllegalArgumentException");
    }

    // -----------------------------------------------------------------------
    // Age-up logic
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Age-Up Transitions")
    class AgeUpTests {

        @Test
        @DisplayName("Sim ages up after exactly daysPerAgeTick days")
        void testSimAgesUpAtBoundary() {
            int daysPerAge = 3;
            LifecycleManager lm = new LifecycleManager(daysPerAge, logger);
            Sim sim = new Sim("Bob", 25, Gender.MALE, Job.UNEMPLOYED);
            int ageBefore = sim.getAge();

            // Process exactly daysPerAge days to trigger 1 birthday
            for (int i = 0; i < daysPerAge; i++) {
                lm.processDay(sim);
            }

            assertEquals(ageBefore + 1, sim.getAge(),
                    "Sim should age up by 1 after " + daysPerAge + " days");
        }

        @Test
        @DisplayName("Sim does NOT age up before reaching daysPerAgeTick")
        void testSimDoesNotAgeUpEarly() {
            int daysPerAge = 3;
            LifecycleManager lm = new LifecycleManager(daysPerAge, logger);
            Sim sim = new Sim("Charlie", 25, Gender.MALE, Job.UNEMPLOYED);
            int ageBefore = sim.getAge();

            // Process 2 of the 3 required days
            for (int i = 0; i < daysPerAge - 1; i++) {
                lm.processDay(sim);
            }

            assertEquals(ageBefore, sim.getAge(),
                    "Sim should NOT age up before reaching the daysPerAgeTick boundary");
        }

        @Test
        @DisplayName("Sim ages up multiple times over many days")
        void testMultipleAgeUps() {
            int daysPerAge = 2;
            LifecycleManager lm = new LifecycleManager(daysPerAge, logger);
            Sim sim = new Sim("Diana", 25, Gender.FEMALE, Job.UNEMPLOYED);
            int ageBefore = sim.getAge();

            // Process 6 days — should trigger 3 birthdays
            for (int i = 0; i < 6; i++) {
                lm.processDay(sim);
            }

            assertEquals(ageBefore + 3, sim.getAge(),
                    "Sim should age up 3 times after 6 days (daysPerAge=2)");
        }
    }

    // -----------------------------------------------------------------------
    // Death by old age
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Sim dies when reaching DEATH_AGE")
    void testDeathAtDeathAge() {
        LifecycleManager lm = new LifecycleManager(1, logger);
        // Create a Sim 1 year before death age
        Sim sim = new Sim("Elder", GameConstants.DEATH_AGE - 1, Gender.MALE, Job.UNEMPLOYED);

        assertNotEquals(SimState.DEAD, sim.getState(), "Sim should start alive");

        // One day with daysPerAge=1 ages the Sim to DEATH_AGE
        lm.processDay(sim);

        assertEquals(SimState.DEAD, sim.getState(),
                "Sim should be marked DEAD upon reaching DEATH_AGE (" + GameConstants.DEATH_AGE + ")");
    }

    @Test
    @DisplayName("Sim stays alive one tick before DEATH_AGE")
    void testSimAliveBeforeDeathAge() {
        LifecycleManager lm = new LifecycleManager(1, logger);
        // Create a Sim 2 years before death age — will be DEATH_AGE - 1 after one age-up
        Sim sim = new Sim("NearDeath", GameConstants.DEATH_AGE - 2, Gender.FEMALE, Job.UNEMPLOYED);

        lm.processDay(sim);

        assertNotEquals(SimState.DEAD, sim.getState(),
                "Sim at age " + sim.getAge() + " should still be alive (death at " + GameConstants.DEATH_AGE + ")");
    }

    // -----------------------------------------------------------------------
    // Retirement pension
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Unemployed elder receives pension on age-up")
    void testPensionForUnemployedElder() {
        LifecycleManager lm = new LifecycleManager(1, logger);
        Sim elder = new Sim("Retiree", GameConstants.ELDER_AGE, Gender.MALE, Job.UNEMPLOYED);
        int moneyBefore = elder.getMoney();

        lm.processDay(elder);

        assertEquals(moneyBefore + GameConstants.RETIREMENT_PENSION_INCOME_AMOUNT,
                elder.getMoney(),
                "Unemployed elder should receive pension of $"
                        + GameConstants.RETIREMENT_PENSION_INCOME_AMOUNT);
    }

    @Test
    @DisplayName("Employed elder does NOT receive pension")
    void testNoPensionForEmployedElder() {
        LifecycleManager lm = new LifecycleManager(1, logger);
        // Freelance Photographer allows up to age 80
        Sim elder = new Sim("Working Elder", GameConstants.ELDER_AGE, Gender.FEMALE,
                Job.FREELANCE_PHOTOGRAPHER);
        int moneyBefore = elder.getMoney();

        lm.processDay(elder);

        assertEquals(moneyBefore, elder.getMoney(),
                "Employed elder should NOT receive pension income");
    }

    // -----------------------------------------------------------------------
    // Forced retirement
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Sim is forced to retire when exceeding job's max age")
    void testForcedRetirement() {
        LifecycleManager lm = new LifecycleManager(1, logger);
        // Personal Trainer max age is 40. Create a Sim at exactly 40.
        Sim sim = new Sim("Trainer", 40, Gender.MALE, Job.PERSONAL_TRAINER);

        // After one age-up: age = 41 > maxAge(40) → forced retirement
        lm.processDay(sim);

        assertEquals(Job.UNEMPLOYED, sim.getCareer(),
                "Sim exceeding job max age should be forced into UNEMPLOYED");
    }

    @Test
    @DisplayName("Sim within job age range keeps their career")
    void testNoForcedRetirementWithinRange() {
        LifecycleManager lm = new LifecycleManager(1, logger);
        Sim sim = new Sim("Young Dev", 25, Gender.FEMALE, Job.SOFTWARE_ENGINEER);

        lm.processDay(sim);

        assertEquals(Job.SOFTWARE_ENGINEER, sim.getCareer(),
                "Sim within job age range should keep their career");
    }

    // -----------------------------------------------------------------------
    // processDayForAll()
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("processDayForAll() processes all living Sims")
    void testProcessDayForAllLivingSims() {
        LifecycleManager lm = new LifecycleManager(3, logger);
        Sim alice = new Sim("Alice", 25, Gender.FEMALE, Job.UNEMPLOYED);
        Sim bob = new Sim("Bob", 30, Gender.MALE, Job.UNEMPLOYED);
        List<Sim> neighborhood = Arrays.asList(alice, bob);

        lm.processDayForAll(neighborhood);

        assertEquals(1, alice.getDaysAlive(), "Alice should have 1 day alive");
        assertEquals(1, bob.getDaysAlive(), "Bob should have 1 day alive");
    }

    @Test
    @DisplayName("processDayForAll() skips dead Sims")
    void testProcessDayForAllSkipsDeadSims() {
        LifecycleManager lm = new LifecycleManager(3, logger);
        Sim alive = new Sim("Alive", 25, Gender.FEMALE, Job.UNEMPLOYED);
        Sim dead = new Sim("Dead", 25, Gender.MALE, Job.UNEMPLOYED);
        dead.markAsDead();

        List<Sim> neighborhood = Arrays.asList(alive, dead);
        lm.processDayForAll(neighborhood);

        assertEquals(1, alive.getDaysAlive(), "Living Sim should have 1 day alive");
        assertEquals(0, dead.getDaysAlive(), "Dead Sim should NOT have daysAlive incremented");
    }
}
