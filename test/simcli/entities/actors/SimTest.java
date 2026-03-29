package simcli.entities.actors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import simcli.engine.SimulationLogger;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.entities.models.SimState;
import simcli.entities.models.ActionState;
import simcli.entities.models.WorkResult;
import simcli.needs.Need;
import simcli.utils.GameConstants;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Sim} — the core actor class.
 *
 * <p>Coverage:
 * <ul>
 *     <li>Constructor initializes all fields correctly</li>
 *     <li>tick() decays needs for playable Sims</li>
 *     <li>tick() only decays hunger for non-playable child Sims</li>
 *     <li>tick() skips dead Sims entirely</li>
 *     <li>performWork() fails for children and unemployed Sims</li>
 *     <li>performWork() earns salary and drains needs</li>
 *     <li>eat() restores needs and removes item from inventory</li>
 *     <li>sleep() restores energy and drains hunger</li>
 *     <li>markAsDead() sets state to DEAD</li>
 *     <li>isPlayable() distinguishes child Sims from regular Sims</li>
 * </ul>
 */
@DisplayName("Sim — Core Actor Entity Tests")
public class SimTest {

    private Sim adultSim;
    private Sim childSim;

    @BeforeEach
    void setUp() {
        SimulationLogger logger = new SimulationLogger();
        SimulationLogger.setInstance(logger);

        adultSim = new Sim("Alice", 25, Gender.FEMALE, Job.SOFTWARE_ENGINEER);
        childSim = new Sim("Tommy", 5, Gender.MALE, Job.UNEMPLOYED);
    }

    // -----------------------------------------------------------------------
    // Constructor tests
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Constructor & Initialization")
    class ConstructorTests {

        @Test
        @DisplayName("Adult Sim starts with correct name, age, gender")
        void testAdultSimBasicProperties() {
            assertEquals("Alice", adultSim.getName());
            assertEquals(25, adultSim.getAge());
            assertEquals(Gender.FEMALE, adultSim.getGender());
        }

        @Test
        @DisplayName("Sim starts with STARTING_MONEY")
        void testStartingMoney() {
            assertEquals(GameConstants.STARTING_MONEY, adultSim.getMoney(),
                    "A new Sim should start with $" + GameConstants.STARTING_MONEY);
        }

        @Test
        @DisplayName("Sim starts with all needs at MAX_VALUE (100)")
        void testNeedsStartFull() {
            assertEquals(Need.MAX_VALUE, adultSim.getHunger().getValue());
            assertEquals(Need.MAX_VALUE, adultSim.getEnergy().getValue());
            assertEquals(Need.MAX_VALUE, adultSim.getHygiene().getValue());
            assertEquals(Need.MAX_VALUE, adultSim.getHappiness().getValue());
            assertEquals(Need.MAX_VALUE, adultSim.getSocial().getValue());
        }

        @Test
        @DisplayName("Sim starts in IDLE action state")
        void testStartsIdle() {
            assertEquals(ActionState.IDLE, adultSim.getCurrentAction());
        }

        @Test
        @DisplayName("Constructor assigns at least one trait")
        void testHasAtLeastOneTrait() {
            assertFalse(adultSim.getTraits().isEmpty(),
                    "Sim should have at least one randomly assigned trait");
        }

        @Test
        @DisplayName("Constructor correctly assigns LifeStage based on age")
        void testLifeStageAssignment() {
            assertEquals("Adult", adultSim.getCurrentStageName());
            assertEquals("Child", childSim.getCurrentStageName());

            Sim teen = new Sim("Teen", 15, Gender.MALE, Job.UNEMPLOYED);
            assertEquals("Teen", teen.getCurrentStageName());

            Sim elder = new Sim("Elder", 70, Gender.FEMALE, Job.UNEMPLOYED);
            assertEquals("Elder", elder.getCurrentStageName());
        }

        @Test
        @DisplayName("Constructor with Job parameter sets career correctly")
        void testConstructorWithJob() {
            assertEquals(Job.SOFTWARE_ENGINEER, adultSim.getCareer());
        }
    }

    // -----------------------------------------------------------------------
    // tick() tests
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("tick() — Needs Decay")
    class TickTests {

        @Test
        @DisplayName("tick() decays needs for a normal adult Sim")
        void testTickDecaysNeeds() {
            int hungerBefore = adultSim.getHunger().getValue();
            int energyBefore = adultSim.getEnergy().getValue();

            adultSim.tick();

            assertTrue(adultSim.getHunger().getValue() < hungerBefore,
                    "Hunger should decrease after a tick");
            assertTrue(adultSim.getEnergy().getValue() < energyBefore,
                    "Energy should decrease after a tick");
        }

        @Test
        @DisplayName("tick() skips dead Sims entirely")
        void testTickSkipsDeadSim() {
            adultSim.markAsDead();
            int hungerBefore = adultSim.getHunger().getValue();

            adultSim.tick();

            assertEquals(hungerBefore, adultSim.getHunger().getValue(),
                    "Dead Sim's hunger should not change after tick");
        }

        @Test
        @DisplayName("tick() only decays hunger for non-playable child Sims (babies)")
        void testTickOnlyDecaysHungerForBaby() {
            childSim.setChildSim(true); // Mark as in-game born baby
            assertFalse(childSim.isPlayable(), "A 5-year-old child sim should not be playable");

            int energyBefore = childSim.getEnergy().getValue();

            childSim.tick();

            // Energy should NOT change for babies (only hunger decays)
            assertEquals(energyBefore, childSim.getEnergy().getValue(),
                    "Non-playable child Sim's energy should remain unchanged during tick");
        }
    }

    // -----------------------------------------------------------------------
    // performWork() tests
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("performWork() — Career Actions")
    class WorkTests {

        @Test
        @DisplayName("performWork() fails for child Sims (canWork = false)")
        void testChildCannotWork() {
            WorkResult result = childSim.performWork();
            assertFalse(result.isSuccess(),
                    "A child Sim should not be able to work");
        }

        @Test
        @DisplayName("performWork() fails for unemployed Sims")
        void testUnemployedCannotWork() {
            Sim unemployed = new Sim("Joe", 25, Gender.MALE, Job.UNEMPLOYED);
            WorkResult result = unemployed.performWork();
            assertFalse(result.isSuccess(),
                    "An unemployed Sim should not be able to perform work");
        }

        @Test
        @DisplayName("performWork() drains energy, hunger, and hygiene")
        void testWorkDrainsNeeds() {
            int energyBefore = adultSim.getEnergy().getValue();
            int hungerBefore = adultSim.getHunger().getValue();
            int hygieneBefore = adultSim.getHygiene().getValue();

            adultSim.performWork();

            assertTrue(adultSim.getEnergy().getValue() < energyBefore,
                    "Energy should decrease after working");
            assertTrue(adultSim.getHunger().getValue() < hungerBefore,
                    "Hunger should decrease after working");
            assertTrue(adultSim.getHygiene().getValue() < hygieneBefore,
                    "Hygiene should decrease after working");
        }

        @Test
        @DisplayName("performWork() earns salary and tracks total money earned")
        void testWorkEarnsSalary() {
            int moneyBefore = adultSim.getMoney();
            int expectedSalary = Job.SOFTWARE_ENGINEER.getSalaryAtTier(1);

            WorkResult result = adultSim.performWork();

            assertTrue(result.isSuccess());
            assertEquals(moneyBefore + expectedSalary, adultSim.getMoney(),
                    "Money should increase by the job's salary at current tier");
            assertEquals(expectedSalary, adultSim.getTotalMoneyEarned() - moneyBefore,
                    "Total money earned tracker should reflect earnings");
        }

        @Test
        @DisplayName("performWork() increments shifts worked today")
        void testWorkIncrementsShifts() {
            assertEquals(0, adultSim.getShiftsWorkedToday());
            adultSim.performWork();
            assertEquals(1, adultSim.getShiftsWorkedToday());
        }
    }

    // -----------------------------------------------------------------------
    // eat() and sleep() tests
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("eat() & sleep() — Need Restoration")
    class RestorationTests {

        @Test
        @DisplayName("eat() restores hunger, energy, happiness and removes item")
        void testEatRestoresNeeds() {
            adultSim.getHunger().setValue(30);
            adultSim.getEnergy().setValue(30);
            adultSim.getHappiness().setValue(30);

            // Use Consumable (not Food) — Food always has happinessValue=0
            simcli.entities.items.Consumable item =
                    new simcli.entities.items.Consumable("Energy Drink", 25, 10, 15, 20);
            adultSim.addItem(item);
            assertEquals(1, adultSim.getInventory().size());

            adultSim.eat(item);

            assertTrue(adultSim.getHunger().getValue() > 30,
                    "Hunger should increase after eating");
            assertTrue(adultSim.getEnergy().getValue() > 30,
                    "Energy should increase after eating");
            assertTrue(adultSim.getHappiness().getValue() > 30,
                    "Happiness should increase after consuming an item with happinessValue > 0");
            assertEquals(0, adultSim.getInventory().size(),
                    "Consumed item should be removed from inventory");
        }

        @Test
        @DisplayName("sleep() restores energy and drains hunger")
        void testSleepRestoresEnergy() {
            adultSim.getEnergy().setValue(20);
            int hungerBefore = adultSim.getHunger().getValue();

            adultSim.sleep(5); // 5 ticks until morning

            assertTrue(adultSim.getEnergy().getValue() > 20,
                    "Energy should increase after sleeping");
            assertTrue(adultSim.getHunger().getValue() < hungerBefore,
                    "Hunger should decrease while sleeping");
            assertEquals(ActionState.SLEEPING, adultSim.getCurrentAction());
        }
    }

    // -----------------------------------------------------------------------
    // State & lifecycle utility tests
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("State & Lifecycle Utilities")
    class StateTests {

        @Test
        @DisplayName("markAsDead() sets state to DEAD")
        void testMarkAsDead() {
            assertNotEquals(SimState.DEAD, adultSim.getState());
            adultSim.markAsDead();
            assertEquals(SimState.DEAD, adultSim.getState());
        }

        @Test
        @DisplayName("isPlayable() returns true for non-child Sims")
        void testIsPlayableForNonChild() {
            assertTrue(adultSim.isPlayable());
        }

        @Test
        @DisplayName("isPlayable() returns false for young child Sims")
        void testIsPlayableForYoungChild() {
            childSim.setChildSim(true);
            assertFalse(childSim.isPlayable(),
                    "A child sim under age 13 should not be playable");
        }

        @Test
        @DisplayName("isPlayable() returns true for teen-aged child Sims")
        void testIsPlayableForTeenChild() {
            Sim teenChild = new Sim("TeenKid", 14, Gender.FEMALE, Job.UNEMPLOYED);
            teenChild.setChildSim(true);
            assertTrue(teenChild.isPlayable(),
                    "A child sim aged 14 (>= TEEN_AGE) should be playable");
        }

        @Test
        @DisplayName("canWork() delegates to LifeStage correctly")
        void testCanWorkDelegatesToLifeStage() {
            assertFalse(childSim.canWork(), "Child stage returns canWork=false");
            assertTrue(adultSim.canWork(), "Adult stage returns canWork=true");
        }

        @Test
        @DisplayName("incrementDaysAlive() increases counter by 1")
        void testIncrementDaysAlive() {
            assertEquals(0, adultSim.getDaysAlive());
            adultSim.incrementDaysAlive();
            assertEquals(1, adultSim.getDaysAlive());
        }
    }
}
