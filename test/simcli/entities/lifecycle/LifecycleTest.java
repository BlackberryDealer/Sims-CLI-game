package simcli.entities.lifecycle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.entities.actors.Sim;
import simcli.entities.lifecycle.LifeStage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the State Pattern lifecycle (LifeStage system).
 *
 * Coverage:
 *  - A newly created adult Sim (age >= 18) starts in AdultStage
 *  - A newly created child Sim (age < 18) starts in ChildStage
 *  - ageUp() on a ChildStage Sim at age 17→18 swaps the LifeStage to AdultStage
 *  - The Sim object reference is the SAME before and after the stage transition
 *  - Inventory items survive the stage transition intact (no data loss)
 *  - ChildStage reports canWork() == false; AdultStage reports canWork() == true
 *  - A Sim already in AdultStage stays in AdultStage after repeated ageUp() calls
 */
@DisplayName("State Pattern — LifeStage Lifecycle Tests")
public class LifecycleTest {

    private Sim childSim;
    private Sim adultSim;

    @BeforeEach
    void setUp() {
        childSim = new Sim("Tommy", 10, Gender.MALE,   Job.UNEMPLOYED);
        adultSim = new Sim("Alice", 25, Gender.FEMALE, Job.UNEMPLOYED);
    }

    // -----------------------------------------------------------------------
    // Initial stage assignment
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Sim aged < 18 starts in ChildStage")
    void testChildSimStartsInChildStage() {
        assertEquals("Child", childSim.getCurrentStageName(),
                "A Sim created with age 10 should start in ChildStage");
        assertFalse(childSim.canWork(), "A child should not be able to work");
    }

    @Test
    @DisplayName("Sim aged >= 18 starts in AdultStage")
    void testAdultSimStartsInAdultStage() {
        assertEquals("Adult", adultSim.getCurrentStageName(),
                "A Sim created with age 25 should start in AdultStage");
        assertTrue(adultSim.canWork(), "An adult should be able to work");
    }

    // -----------------------------------------------------------------------
    // Stage transition
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("ageUp() at age 17 transitions TeenStage → AdultStage")
    void testLifeStageBrainSwapAtAdulthood() {
        // Create a sim at age 17 (one year from transition)
        Sim sim = new Sim("Teen", 17, Gender.FEMALE, Job.UNEMPLOYED);
        assertEquals("Teen", sim.getCurrentStageName(), "Should be Teen at 17");

        // Record the LifeStage object reference BEFORE transition
        LifeStage stageBefore = sim.getLifeStage();

        // Age up to 18 — this should trigger the "brain swap"
        sim.ageUp();

        // Verify stage name changed
        assertEquals(18, sim.getAge(), "Age should be 18 after ageUp()");
        assertEquals("Adult", sim.getCurrentStageName(),
                "LifeStage should have transitioned to AdultStage at age 18");
        assertTrue(sim.canWork(), "Sim should be able to work after turning 18");

        // Verify the OLD stage object is a DIFFERENT object (the swap happened)
        assertNotSame(stageBefore, sim.getLifeStage(),
                "The LifeStage object reference should change after a brain swap");
    }

    @Test
    @DisplayName("Sim object identity is preserved across LifeStage transition")
    void testSimObjectIdentityPreservedAcrossTransition() {
        Sim sim = new Sim("Teen2", 17, Gender.MALE, Job.UNEMPLOYED);
        assertEquals("Teen", sim.getCurrentStageName(), "Should start as Teen");

        // Record the Sim object reference
        Sim simRef = sim;

        // Age up
        sim.ageUp();

        // The Java object should be the exact same object in memory
        assertSame(simRef, sim,
                "The Sim object reference must not change during a lifecycle transition");
    }

    @Test
    @DisplayName("Inventory survives a LifeStage transition without data loss")
    void testInventoryIntactAfterLifeStageTransition() {
        Sim sim = new Sim("Teen3", 17, Gender.FEMALE, Job.UNEMPLOYED);
        assertEquals("Teen", sim.getCurrentStageName(), "Should start as Teen");

        // Add items to inventory BEFORE transition
        sim.addItem(new simcli.entities.items.Food("Apple", 10, 15, 5));
        sim.addItem(new simcli.entities.items.Food("Steak", 40, 50, 20));
        assertEquals(2, sim.getInventory().size(), "Should have 2 items before transition");

        // Trigger the transition
        sim.ageUp();

        // Items must still be there
        assertEquals(2, sim.getInventory().size(),
                "Inventory must remain intact after LifeStage transition");
        assertEquals("Apple", sim.getInventory().get(0).getObjectName());
        assertEquals("Steak", sim.getInventory().get(1).getObjectName());
    }

    @Test
    @DisplayName("Name is preserved across LifeStage transition")
    void testNameIntactAfterTransition() {
        Sim sim = new Sim("Precious", 17, Gender.FEMALE, Job.UNEMPLOYED);
        assertEquals("Teen", sim.getCurrentStageName(), "Should start as Teen");
        sim.ageUp();
        assertEquals("Precious", sim.getName(),
                "Sim's name must not change after LifeStage transition");
    }

    @Test
    @DisplayName("AdultStage Sim stays in AdultStage after further ageUp() calls")
    void testAdultStageDoesNotTransitionFurther() {
        LifeStage stageBefore = adultSim.getLifeStage();
        adultSim.ageUp();
        adultSim.ageUp();
        assertSame(stageBefore, adultSim.getLifeStage(),
                "AdultStage.getNextStage() should return 'this' — no further transition");
        assertEquals("Adult", adultSim.getCurrentStageName());
    }

    // -----------------------------------------------------------------------
    // Teen stage tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Sim aged 13–17 starts in TeenStage")
    void testTeenSimStartsInTeenStage() {
        Sim teenSim = new Sim("Teenager", 15, Gender.MALE, Job.UNEMPLOYED);
        assertEquals("Teen", teenSim.getCurrentStageName(),
                "A Sim created at age 15 should start in TeenStage");
        assertFalse(teenSim.canWork(), "A teen should not be able to work");
    }

    @Test
    @DisplayName("ageUp() at age 12 transitions ChildStage → TeenStage")
    void testChildToTeenTransition() {
        Sim sim = new Sim("Kid", 12, Gender.FEMALE, Job.UNEMPLOYED);
        assertEquals("Child", sim.getCurrentStageName(), "Should be Child at 12");

        sim.ageUp(); // age becomes 13

        assertEquals(13, sim.getAge(), "Age should be 13 after ageUp()");
        assertEquals("Teen", sim.getCurrentStageName(),
                "LifeStage should have transitioned to TeenStage at age 13");
        assertFalse(sim.canWork(), "Teens cannot work");
    }

    @Test
    @DisplayName("ageUp() at age 17 transitions TeenStage → AdultStage")
    void testTeenToAdultTransition() {
        Sim sim = new Sim("TeenGrad", 17, Gender.MALE, Job.UNEMPLOYED);
        assertEquals("Teen", sim.getCurrentStageName(), "Should be Teen at 17");

        sim.ageUp(); // age becomes 18

        assertEquals(18, sim.getAge());
        assertEquals("Adult", sim.getCurrentStageName(),
                "LifeStage should have transitioned to AdultStage at age 18");
        assertTrue(sim.canWork(), "Adults can work");
    }

    @Test
    @DisplayName("TeenStage has higher energy decay modifier (1.2)")
    void testTeenEnergyDecayModifier() {
        Sim teenSim = new Sim("EnergyTeen", 15, Gender.MALE, Job.UNEMPLOYED);
        assertEquals(1.2, teenSim.getLifeStage().getEnergyDecayModifier(), 0.001,
                "TeenStage should have 1.2x energy decay modifier");
    }

    // -----------------------------------------------------------------------
    // Elder stage tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Sim aged >= 65 starts in ElderStage")
    void testElderSimStartsInElderStage() {
        Sim elderSim = new Sim("Grandpa", 70, Gender.MALE, Job.UNEMPLOYED);
        assertEquals("Elder", elderSim.getCurrentStageName(),
                "A Sim created at age 70 should start in ElderStage");
        assertTrue(elderSim.canWork(), "An elder can be able to work");
    }

    @Test
    @DisplayName("ageUp() at age 64 transitions AdultStage → ElderStage")
    void testAdultToElderTransition() {
        Sim sim = new Sim("Senior", 64, Gender.FEMALE, Job.UNEMPLOYED);
        assertEquals("Adult", sim.getCurrentStageName(), "Should be Adult at 64");

        sim.ageUp(); // age becomes 65

        assertEquals(65, sim.getAge());
        assertEquals("Elder", sim.getCurrentStageName(),
                "LifeStage should have transitioned to ElderStage at age 65");
        assertTrue(sim.canWork(), "Elders can still work");
    }

    @Test
    @DisplayName("ElderStage stays in ElderStage after further ageUp() calls")
    void testElderStageDoesNotTransition() {
        Sim sim = new Sim("OldTimer", 70, Gender.MALE, Job.UNEMPLOYED);
        LifeStage stageBefore = sim.getLifeStage();
        sim.ageUp();
        sim.ageUp();
        assertSame(stageBefore, sim.getLifeStage(),
                "ElderStage.getNextStage() should return 'this'");
        assertEquals("Elder", sim.getCurrentStageName());
    }

    @Test
    @DisplayName("ElderStage has lower energy decay modifier (0.8)")
    void testElderEnergyDecayModifier() {
        Sim elderSim = new Sim("SlowElder", 68, Gender.FEMALE, Job.UNEMPLOYED);
        assertEquals(0.8, elderSim.getLifeStage().getEnergyDecayModifier(), 0.001,
                "ElderStage should have 0.8x energy decay modifier");
    }
}
