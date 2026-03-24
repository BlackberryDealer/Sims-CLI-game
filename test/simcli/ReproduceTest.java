package simcli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import simcli.engine.SimulationException;
import simcli.entities.actors.Gender;
import simcli.entities.actors.Job;
import simcli.entities.actors.Sim;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for social mechanics: relationship, marriage, and reproduction.
 *
 * Coverage:
 *  - reproduce() succeeds when Sims are married and of opposite genders
 *  - reproduce() throws when Sim is not married
 *  - reproduce() throws when both spouses are the same gender
 *  - marry() fails when relationship stat is too low
 *  - marry() succeeds when relationship stat >= 50
 *  - child returned by reproduce() starts in ChildStage (age 0 < 18)
 */
@DisplayName("Social Mechanics — Reproduce & Marriage Tests")
public class ReproduceTest {

    // UIManager uses static output; redirect System.out to avoid cluttering test output
    private Sim male;
    private Sim female;
    private Sim male2;

    @BeforeEach
    void setUp() {
        male   = new Sim("Adam",  25, Gender.MALE,   Job.UNEMPLOYED);
        female = new Sim("Eve",   23, Gender.FEMALE, Job.UNEMPLOYED);
        male2  = new Sim("Bob",   27, Gender.MALE,   Job.UNEMPLOYED);
    }

    // -----------------------------------------------------------------------
    // Marriage prerequisite tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("marry() fails when relationship score < 50")
    void testMarryFailsWhenRelationshipTooLow() {
        // Default relationship score is 0 — should not be able to marry
        boolean result = male.marry(female);
        assertFalse(result, "Sims should not marry with insufficient relationship score");
        assertNull(male.getSpouse(), "Adam's spouse should remain null");
        assertNull(female.getSpouse(), "Eve's spouse should remain null");
    }

    @Test
    @DisplayName("marry() succeeds when relationship score >= 50")
    void testMarrySucceedsWithHighRelationship() {
        // Build relationship via socialising (each call adds 10 points)
        for (int i = 0; i < 5; i++) {
            male.interactSocially(female);
        }
        // Relationship score is now 50
        boolean result = male.marry(female);
        assertTrue(result, "Sims should marry when relationship >= 50");
        assertEquals(female, male.getSpouse(),    "Adam's spouse should be Eve");
        assertEquals(male,   female.getSpouse(),  "Eve's spouse should be Adam");
    }

    @Test
    @DisplayName("marry() does not allow a Sim to marry themselves")
    void testMarrySelfReturnsFalse() {
        boolean result = male.marry(male);
        assertFalse(result, "A Sim cannot marry themselves");
    }

    // -----------------------------------------------------------------------
    // Reproduction tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("reproduce() succeeds for married opposite-gender Sims")
    void testReproduceSuccessful() throws SimulationException {
        // Arrange: build relationship and marry
        for (int i = 0; i < 5; i++) male.interactSocially(female);
        male.marry(female);

        // Act
        Sim child = male.reproduce();

        // Assert
        assertNotNull(child, "reproduce() should return a non-null child Sim");
        assertEquals(0, child.getAge(), "Newborn should have age 0");
        // Child's LifeStage should be ChildStage (canWork == false)
        assertFalse(child.canWork(), "A newborn child should not be able to work");
        assertEquals("Child", child.getCurrentStageName(), "Newborn should be in ChildStage");
    }

    @Test
    @DisplayName("reproduce() throws SimulationException when Sim is not married")
    void testReproduceFailsWhenNotMarried() {
        // No marriage has taken place
        assertThrows(SimulationException.class, () -> male.reproduce(),
                "reproduce() must throw when the Sim has no spouse");
    }

    @Test
    @DisplayName("reproduce() throws SimulationException for same-gender couple")
    void testReproduceFailsForSameGenderCouple() {
        // Manually force-marry two males by building relation then calling marry via reflection-free helper
        // We build 50+ relationship between male and male2 and get them married
        for (int i = 0; i < 5; i++) male.interactSocially(male2);
        // marry() will silently fail because gender check is in reproduce(), not marry()
        // Override: we access the relationship map and set score directly
        male.getRelationships().put(male2, 100);
        male2.getRelationships().put(male, 100);
        boolean married = male.marry(male2);
        assertTrue(married, "Two males can still get 'married' — the biological check is in reproduce()");

        // Now reproduce should throw
        assertThrows(SimulationException.class, () -> male.reproduce(),
                "reproduce() must throw for same-gender couple");
    }

    @Test
    @DisplayName("Child returned by reproduce() is a distinct object from parents")
    void testChildIsIndependentObject() throws SimulationException {
        for (int i = 0; i < 5; i++) male.interactSocially(female);
        male.marry(female);

        Sim child = male.reproduce();

        assertNotSame(male,   child, "Child must not be the same object as father");
        assertNotSame(female, child, "Child must not be the same object as mother");
    }
}
