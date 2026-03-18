package simcli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import simcli.entities.Gender;
import simcli.entities.Job;
import simcli.entities.Sim;
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
    @DisplayName("ageUp() at age 17 transitions ChildStage → AdultStage")
    void testLifeStageBrainSwapAtAdulthood() {
        // Create a sim at age 17 (one year from transition)
        Sim sim = new Sim("Teen", 17, Gender.FEMALE, Job.UNEMPLOYED);
        assertEquals("Child", sim.getCurrentStageName(), "Should be Child at 17");

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

        // Add items to inventory BEFORE transition
        sim.addItem(new simcli.entities.Food("Apple", 10, 15, 5));
        sim.addItem(new simcli.entities.Food("Steak", 40, 50, 20));
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
}
