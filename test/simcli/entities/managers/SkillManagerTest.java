package simcli.entities.managers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import simcli.engine.GameEngine;
import simcli.engine.SimulationLogger;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.entities.models.SkillType;
import simcli.entities.models.Trait;
import simcli.entities.models.WorkResult;
import simcli.persistence.SaveManager;
import simcli.utils.GameConstants;
import simcli.utils.GameRandom;
import simcli.world.interactables.Computer;
import simcli.world.interactables.WeightBench;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the skills system: {@link SkillManager} levelling, how Sims
 * train skills, and how skills change pay, promotions and workouts.
 */
@DisplayName("Skills — Levelling, Training and Career Effects")
public class SkillManagerTest {

    @BeforeEach
    void setUp() {
        SimulationLogger.setInstance(new SimulationLogger());
    }

    @AfterEach
    void tearDown() {
        GameRandom.RANDOM.setSeed(System.nanoTime());
    }

    /** A Sim with one fixed trait, so trait bonuses don't make tests flaky. */
    private static Sim sim(Job job, Trait trait) {
        Sim sim = new Sim("Sam", 30, Gender.MALE, job);
        sim.getTraits().clear();
        sim.getTraits().add(trait);
        return sim;
    }

    private static Scanner noInput() {
        return new Scanner(new ByteArrayInputStream(new byte[0]));
    }

    @Nested
    @DisplayName("SkillManager")
    class Levelling {

        @Test
        @DisplayName("Every skill starts at level 0")
        void testStartsAtZero() {
            SkillManager skills = new SkillManager();
            for (SkillType type : SkillType.values()) {
                assertEquals(0, skills.getLevel(type));
                assertEquals(0, skills.getExperience(type));
            }
        }

        @Test
        @DisplayName("Each SKILL_XP_PER_LEVEL points is one level")
        void testLevelsFromExperience() {
            SkillManager skills = new SkillManager();
            assertFalse(skills.addExperience(SkillType.LOGIC, GameConstants.SKILL_XP_PER_LEVEL - 1, "Sam"));
            assertEquals(0, skills.getLevel(SkillType.LOGIC));
            assertTrue(skills.addExperience(SkillType.LOGIC, 1, "Sam"), "Crossing a level boundary is a level-up");
            assertEquals(1, skills.getLevel(SkillType.LOGIC));
            assertEquals(0, skills.getLevel(SkillType.FITNESS), "Skills are independent");
        }

        @Test
        @DisplayName("Skills stop at MAX_SKILL_LEVEL")
        void testCappedAtMax() {
            SkillManager skills = new SkillManager();
            skills.addExperience(SkillType.CHARISMA, 10_000, "Sam");
            assertEquals(GameConstants.MAX_SKILL_LEVEL, skills.getLevel(SkillType.CHARISMA));
            assertEquals(100, skills.getProgressPercent(SkillType.CHARISMA));
        }

        @Test
        @DisplayName("Progress shows how far through the current level the skill is")
        void testProgressPercent() {
            SkillManager skills = new SkillManager();
            skills.setExperience(SkillType.FITNESS, GameConstants.SKILL_XP_PER_LEVEL + GameConstants.SKILL_XP_PER_LEVEL / 2);
            assertEquals(1, skills.getLevel(SkillType.FITNESS));
            assertEquals(50, skills.getProgressPercent(SkillType.FITNESS));
        }

        @Test
        @DisplayName("Negative or zero experience changes nothing")
        void testIgnoresNonPositive() {
            SkillManager skills = new SkillManager();
            assertFalse(skills.addExperience(SkillType.LOGIC, -50, "Sam"));
            assertEquals(0, skills.getExperience(SkillType.LOGIC));
        }
    }

    @Nested
    @DisplayName("Training")
    class Training {

        @Test
        @DisplayName("Working out trains Fitness and fitter Sims spend less energy")
        void testWorkoutTrainsFitness() throws Exception {
            Sim sim = sim(Job.UNEMPLOYED, Trait.LAZY);
            new WeightBench().interact(sim, noInput(), new TimeManager(8, 24));
            assertEquals(GameConstants.WORKOUT_FITNESS_XP, sim.getSkillManager().getExperience(SkillType.FITNESS));

            Sim fit = sim(Job.UNEMPLOYED, Trait.LAZY);
            fit.getSkillManager().setExperience(SkillType.FITNESS, 5 * GameConstants.SKILL_XP_PER_LEVEL);
            new WeightBench().interact(fit, noInput(), new TimeManager(8, 24));
            assertTrue(fit.getEnergy().getValue() > sim.getEnergy().getValue(),
                    "A level-5 Sim should tire less from the same workout");
        }

        @Test
        @DisplayName("Studying at the computer trains Logic")
        void testStudyTrainsLogic() throws Exception {
            Sim sim = sim(Job.UNEMPLOYED, Trait.LAZY);
            new Computer().interact(sim, noInput(), new TimeManager(8, 24));
            assertEquals(GameConstants.STUDY_LOGIC_XP, sim.getSkillManager().getExperience(SkillType.LOGIC));
        }

        @Test
        @DisplayName("Active Sims learn Fitness and Socialites learn Charisma 50% faster")
        void testTraitBonuses() {
            Sim active = sim(Job.UNEMPLOYED, Trait.ACTIVE);
            active.trainSkill(SkillType.FITNESS, 10);
            active.trainSkill(SkillType.LOGIC, 10);
            assertEquals(15, active.getSkillManager().getExperience(SkillType.FITNESS));
            assertEquals(10, active.getSkillManager().getExperience(SkillType.LOGIC));

            Sim socialite = sim(Job.UNEMPLOYED, Trait.SOCIALITE);
            socialite.trainSkill(SkillType.CHARISMA, 10);
            assertEquals(15, socialite.getSkillManager().getExperience(SkillType.CHARISMA));
        }

        @Test
        @DisplayName("A work shift trains the career's skill")
        void testWorkTrainsJobSkill() {
            Sim sim = sim(Job.PERSONAL_TRAINER, Trait.LAZY);
            sim.performWork();
            assertEquals(GameConstants.WORK_SKILL_XP, sim.getSkillManager().getExperience(SkillType.FITNESS));
        }
    }

    @Nested
    @DisplayName("Career effects")
    class CareerEffects {

        @Test
        @DisplayName("Every career has a skill; unemployment has none")
        void testPrimarySkills() {
            assertNull(Job.UNEMPLOYED.getPrimarySkill());
            assertEquals(SkillType.LOGIC, Job.SOFTWARE_ENGINEER.getPrimarySkill());
            assertEquals(SkillType.LOGIC, Job.HARDWARE_TECHNICIAN.getPrimarySkill());
            assertEquals(SkillType.CHARISMA, Job.FREELANCE_PHOTOGRAPHER.getPrimarySkill());
            assertEquals(SkillType.FITNESS, Job.PERSONAL_TRAINER.getPrimarySkill());
        }

        @Test
        @DisplayName("Pay rises 5% per level of the career's skill")
        void testSkilledPayIsHigher() {
            Sim novice = sim(Job.SOFTWARE_ENGINEER, Trait.LAZY);
            Sim expert = sim(Job.SOFTWARE_ENGINEER, Trait.LAZY);
            expert.getSkillManager().setExperience(SkillType.LOGIC, 10 * GameConstants.SKILL_XP_PER_LEVEL);

            WorkResult a = novice.performWork();
            WorkResult b = expert.performWork();

            assertEquals(Job.SOFTWARE_ENGINEER.getSalaryAtTier(1), a.getEarnings(), "No skill means base pay");
            assertEquals((int) Math.round(Job.SOFTWARE_ENGINEER.getSalaryAtTier(1) * 1.5), b.getEarnings(),
                    "Level 10 should pay 50% more");
        }

        @Test
        @DisplayName("Promotion chance starts at 25% and grows with skill, up to 75%")
        void testPromotionChance() {
            assertEquals(0.25, Job.SOFTWARE_ENGINEER.getPromotionChance(0), 1e-9);
            assertEquals(0.50, Job.SOFTWARE_ENGINEER.getPromotionChance(5), 1e-9);
            assertEquals(0.75, Job.SOFTWARE_ENGINEER.getPromotionChance(10), 1e-9);
        }

        @Test
        @DisplayName("Promotion past tier 2 needs the career's skill")
        void testPromotionNeedsSkill() {
            assertEquals(0, Job.SOFTWARE_ENGINEER.getSkillRequiredForTier(2));
            assertEquals(2, Job.SOFTWARE_ENGINEER.getSkillRequiredForTier(3));
            assertEquals(6, Job.SOFTWARE_ENGINEER.getSkillRequiredForTier(5));

            GameRandom.RANDOM.setSeed(1);
            Sim sim = sim(Job.SOFTWARE_ENGINEER, Trait.LAZY);
            sim.setJobTier(2);
            assertEquals(2, sim.getSkillNeededForPromotion());
            for (int i = 0; i < 50; i++) {
                sim.getCareerManager().setShiftsWorkedToday(0);
                sim.getSkillManager().setExperience(SkillType.LOGIC, 0); // don't let work train it past the gate
                sim.performWork();
            }
            assertEquals(2, sim.getJobTier(), "Without Logic 2 the Sim must never reach tier 3");

            sim.getSkillManager().setExperience(SkillType.LOGIC, 2 * GameConstants.SKILL_XP_PER_LEVEL);
            assertEquals(0, sim.getSkillNeededForPromotion());
            for (int i = 0; i < 50 && sim.getJobTier() == 2; i++) {
                sim.getCareerManager().setShiftsWorkedToday(0);
                sim.performWork();
            }
            assertEquals(3, sim.getJobTier(), "With Logic 2 a promotion to tier 3 should come quickly");
        }

        @Test
        @DisplayName("Nothing is needed at the top tier or without a job")
        void testNoRequirementWhenNoPromotionPossible() {
            Sim jobless = sim(Job.UNEMPLOYED, Trait.LAZY);
            assertEquals(0, jobless.getSkillNeededForPromotion());
            Sim top = sim(Job.PERSONAL_TRAINER, Trait.LAZY);
            top.setJobTier(Job.PERSONAL_TRAINER.getMaxTier());
            assertEquals(0, top.getSkillNeededForPromotion());
        }
    }

    @Test
    @DisplayName("Skills survive a save and reload")
    void testSkillsSaved() {
        String world = "JUnitSkills";
        SaveManager.deleteSave(world);
        try {
            Sim sim = new Sim("Sam", 30, Gender.MALE, Job.SOFTWARE_ENGINEER);
            sim.getSkillManager().setExperience(SkillType.LOGIC, 47);
            sim.getSkillManager().setExperience(SkillType.CHARISMA, 5);
            List<Sim> household = new ArrayList<>(List.of(sim));
            SaveManager.saveGame(new GameEngine(world, 10, household, false), world);

            GameEngine loaded = SaveManager.loadGame(world);
            assertNotNull(loaded);
            SkillManager skills = loaded.getNeighborhood().get(0).getSkillManager();
            assertEquals(47, skills.getExperience(SkillType.LOGIC));
            assertEquals(5, skills.getExperience(SkillType.CHARISMA));
            assertEquals(0, skills.getExperience(SkillType.FITNESS));
        } finally {
            SaveManager.deleteSave(world);
        }
    }
}
