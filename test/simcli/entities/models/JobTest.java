package simcli.entities.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import simcli.utils.GameConstants;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link Job} enum — career definitions and salary calculations.
 *
 * <p>Coverage:
 * <ul>
 *     <li>Enum constant field values (salary, hours, energy, age range, tier)</li>
 *     <li>getSalaryAtTier() formula: baseSalary * multiplier^(tier-1)</li>
 *     <li>getSalaryAtTier() tier clamping (below 1 and above maxTier)</li>
 *     <li>UNEMPLOYED always returns $0 regardless of tier</li>
 * </ul>
 */
@DisplayName("Job Enum — Career Definitions & Salary Tests")
public class JobTest {

    // -----------------------------------------------------------------------
    // Field value verification
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Career Field Values")
    class FieldTests {

        @Test
        @DisplayName("SOFTWARE_ENGINEER has correct base values")
        void testSoftwareEngineerFields() {
            Job job = Job.SOFTWARE_ENGINEER;
            assertEquals("Software Engineer", job.getTitle());
            assertEquals(150, job.getBaseSalary());
            assertEquals(4, job.getWorkingHours());
            assertEquals(30, job.getEnergyDrain());
            assertEquals(GameConstants.ADULT_AGE, job.getMinAge());
            assertEquals(GameConstants.ELDER_AGE, job.getMaxAge());
            assertEquals(1.2, job.getPromotionMultiplier(), 0.001);
            assertEquals(5, job.getMaxTier());
        }

        @Test
        @DisplayName("HARDWARE_TECHNICIAN has correct base values")
        void testHardwareTechnicianFields() {
            Job job = Job.HARDWARE_TECHNICIAN;
            assertEquals("Hardware Technician", job.getTitle());
            assertEquals(120, job.getBaseSalary());
            assertEquals(5, job.getWorkingHours());
            assertEquals(45, job.getEnergyDrain());
            assertEquals(55, job.getMaxAge());
            assertEquals(4, job.getMaxTier());
        }

        @Test
        @DisplayName("PERSONAL_TRAINER has correct base values (high drain, low maxAge)")
        void testPersonalTrainerFields() {
            Job job = Job.PERSONAL_TRAINER;
            assertEquals(90, job.getBaseSalary());
            assertEquals(60, job.getEnergyDrain());
            assertEquals(40, job.getMaxAge());
            assertEquals(3, job.getMaxTier());
        }

        @Test
        @DisplayName("FREELANCE_PHOTOGRAPHER has correct base values (low drain)")
        void testFreelancePhotographerFields() {
            Job job = Job.FREELANCE_PHOTOGRAPHER;
            assertEquals(80, job.getBaseSalary());
            assertEquals(15, job.getEnergyDrain());
            assertEquals(80, job.getMaxAge());
            assertEquals(3, job.getMaxTier());
        }

        @Test
        @DisplayName("UNEMPLOYED has zero salary and drain")
        void testUnemployedFields() {
            Job job = Job.UNEMPLOYED;
            assertEquals(0, job.getBaseSalary());
            assertEquals(0, job.getEnergyDrain());
            assertEquals(0, job.getWorkingHours());
        }
    }

    // -----------------------------------------------------------------------
    // getSalaryAtTier() calculation
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Salary Calculation")
    class SalaryTests {

        @Test
        @DisplayName("getSalaryAtTier(1) returns base salary")
        void testTier1ReturnsBaseSalary() {
            // multiplier^(1-1) = multiplier^0 = 1
            assertEquals(150, Job.SOFTWARE_ENGINEER.getSalaryAtTier(1));
            assertEquals(120, Job.HARDWARE_TECHNICIAN.getSalaryAtTier(1));
            assertEquals(80, Job.FREELANCE_PHOTOGRAPHER.getSalaryAtTier(1));
            assertEquals(90, Job.PERSONAL_TRAINER.getSalaryAtTier(1));
        }

        @Test
        @DisplayName("getSalaryAtTier(2) applies multiplier once")
        void testTier2AppliesMultiplier() {
            // Software Engineer: 150 * 1.2^(2-1) = 150 * 1.2 = 180
            assertEquals((int) (150 * 1.2), Job.SOFTWARE_ENGINEER.getSalaryAtTier(2));
        }

        @Test
        @DisplayName("getSalaryAtTier(maxTier) applies full multiplier chain")
        void testMaxTierSalary() {
            // Software Engineer tier 5: 150 * 1.2^4
            int expected = (int) (150 * Math.pow(1.2, 4));
            assertEquals(expected, Job.SOFTWARE_ENGINEER.getSalaryAtTier(5));
        }

        @Test
        @DisplayName("getSalaryAtTier() clamps tier above maxTier")
        void testTierClampedAboveMax() {
            // Tier 10 should be clamped to maxTier (5) for SOFTWARE_ENGINEER
            int atMax = Job.SOFTWARE_ENGINEER.getSalaryAtTier(5);
            int aboveMax = Job.SOFTWARE_ENGINEER.getSalaryAtTier(10);
            assertEquals(atMax, aboveMax,
                    "Salary at tier above max should be clamped to maxTier salary");
        }

        @Test
        @DisplayName("getSalaryAtTier() clamps tier below 1 to tier 1")
        void testTierClampedBelowOne() {
            assertEquals(150, Job.SOFTWARE_ENGINEER.getSalaryAtTier(0),
                    "Tier 0 should be clamped to tier 1 (base salary)");
            assertEquals(150, Job.SOFTWARE_ENGINEER.getSalaryAtTier(-5),
                    "Negative tier should be clamped to tier 1");
        }

        @Test
        @DisplayName("UNEMPLOYED.getSalaryAtTier() always returns 0")
        void testUnemployedAlwaysReturnsZero() {
            assertEquals(0, Job.UNEMPLOYED.getSalaryAtTier(1));
            assertEquals(0, Job.UNEMPLOYED.getSalaryAtTier(5));
            assertEquals(0, Job.UNEMPLOYED.getSalaryAtTier(100));
        }
    }
}
