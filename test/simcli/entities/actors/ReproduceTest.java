package simcli.entities.actors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import simcli.engine.SimulationException;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;

import simcli.utils.GameConstants;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for social mechanics: relationship, marriage, and reproduction.
 *
 * Coverage:
 *  - reproduce() succeeds when Sims are married and of opposite genders (with 50% chance)
 *  - reproduce() throws when Sim is not married
 *  - reproduce() throws when both spouses are the same gender
 *  - marry() fails when relationship stat is too low
 *  - marry() succeeds when relationship stat >= 100
 *  - relationship score caps at 100
 *  - child returned by reproduce() starts in ChildStage (age 0 < 13)
 *  - child sim isPlayable() returns false when age < 13
 */
@DisplayName("Social Mechanics — Reproduce & Marriage Tests")
public class ReproduceTest {

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
    // Relationship score capping tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Relationship score caps at 100")
    void testRelationshipScoreCapsAt100() {
        // Each interactSocially adds 10 to the legacy map. Do it 15 times.
        for (int i = 0; i < 15; i++) {
            male.getRelationshipManager().interactSocially(female);
        }
        int score = male.getRelationshipManager().getRelationship(female);
        assertEquals(GameConstants.MAX_RELATIONSHIP_SCORE, score,
                "Relationship score should cap at " + GameConstants.MAX_RELATIONSHIP_SCORE);
    }

    // -----------------------------------------------------------------------
    // Marriage prerequisite tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("marry() fails when relationship score < 100")
    void testMarryFailsWhenRelationshipTooLow() {
        // Default relationship score is 0 — should not be able to marry
        boolean result = male.getRelationshipManager().marry(female);
        assertFalse(result, "Sims should not marry with insufficient relationship score");
        assertNull(male.getRelationshipManager().getSpouse(), "Adam's spouse should remain null");
        assertNull(female.getRelationshipManager().getSpouse(), "Eve's spouse should remain null");
    }

    @Test
    @DisplayName("marry() fails when relationship score is 50 (old threshold)")
    void testMarryFailsAtOldThreshold() {
        for (int i = 0; i < 5; i++) {
            male.getRelationshipManager().interactSocially(female);
        }
        // Score is 50 — no longer sufficient
        boolean result = male.getRelationshipManager().marry(female);
        assertFalse(result, "Sims should not marry at score 50 (new threshold is 100)");
    }

    @Test
    @DisplayName("marry() succeeds when relationship score >= 100")
    void testMarrySucceedsWithHighRelationship() {
        // Build relationship to 100 (each interactSocially adds 10)
        for (int i = 0; i < 10; i++) {
            male.getRelationshipManager().interactSocially(female);
        }
        boolean result = male.getRelationshipManager().marry(female);
        assertTrue(result, "Sims should marry when relationship >= 100");
        assertEquals(female, male.getRelationshipManager().getSpouse(),    "Adam's spouse should be Eve");
        assertEquals(male,   female.getRelationshipManager().getSpouse(),  "Eve's spouse should be Adam");
    }

    @Test
    @DisplayName("marry() does not allow a Sim to marry themselves")
    void testMarrySelfReturnsFalse() {
        boolean result = male.getRelationshipManager().marry(male);
        assertFalse(result, "A Sim cannot marry themselves");
    }

    // -----------------------------------------------------------------------
    // Reproduction tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("reproduce() returns non-null at least once in multiple attempts (50% success rate)")
    void testReproduceSuccessfulWithRetry() throws SimulationException {
        // Arrange: build relationship and marry
        for (int i = 0; i < 10; i++) male.getRelationshipManager().interactSocially(female);
        male.getRelationshipManager().marry(female);

        // Act: try multiple times to account for 50% success rate
        Sim child = null;
        for (int attempt = 0; attempt < 20; attempt++) {
            child = male.getRelationshipManager().reproduce("TestBaby");
            if (child != null) break;
        }

        // Assert
        assertNotNull(child, "reproduce() should succeed at least once in 20 attempts with 50% chance");
        assertEquals(0, child.getAge(), "Newborn should have age 0");
        assertFalse(child.canWork(), "A newborn child should not be able to work");
        assertEquals("Child", child.getCurrentStageName(), "Newborn should be in ChildStage");
        assertTrue(child.isChildSim(), "Child should have isChildSim flag set");
        assertFalse(child.isPlayable(), "A newborn child should not be playable");
    }

    @Test
    @DisplayName("reproduce() can return null (50% failure rate)")
    void testReproduceCanFail() throws SimulationException {
        for (int i = 0; i < 10; i++) male.getRelationshipManager().interactSocially(female);
        male.getRelationshipManager().marry(female);

        // Try many times — at least one should return null
        boolean hadNull = false;
        for (int attempt = 0; attempt < 50; attempt++) {
            Sim child = male.getRelationshipManager().reproduce("TestBaby");
            if (child == null) {
                hadNull = true;
                break;
            }
        }
        assertTrue(hadNull, "reproduce() should return null at least once in 50 attempts with 50% failure rate");
    }

    @Test
    @DisplayName("reproduce() throws SimulationException when Sim is not married")
    void testReproduceFailsWhenNotMarried() {
        assertThrows(SimulationException.class, () -> male.getRelationshipManager().reproduce("TestBaby"),
                "reproduce() must throw when the Sim has no spouse");
    }

    @Test
    @DisplayName("reproduce() throws SimulationException for same-gender couple")
    void testReproduceFailsForSameGenderCouple() {
        // Build relationship to 100 and marry
        male.getRelationshipManager().increaseRelationship(male2, 100);
        male2.getRelationshipManager().increaseRelationship(male, 100);
        boolean married = male.getRelationshipManager().marry(male2);
        assertTrue(married, "Two males can still get 'married' — the biological check is in reproduce()");

        assertThrows(SimulationException.class, () -> male.getRelationshipManager().reproduce("TestBaby"),
                "reproduce() must throw for same-gender couple");
    }

    @Test
    @DisplayName("Child returned by reproduce() is a distinct object from parents")
    void testChildIsIndependentObject() throws SimulationException {
        for (int i = 0; i < 10; i++) male.getRelationshipManager().interactSocially(female);
        male.getRelationshipManager().marry(female);

        Sim child = null;
        for (int attempt = 0; attempt < 20; attempt++) {
            child = male.getRelationshipManager().reproduce("TestBaby");
            if (child != null) break;
        }

        assertNotNull(child, "Should eventually produce a child");
        assertNotSame(male,   child, "Child must not be the same object as father");
        assertNotSame(female, child, "Child must not be the same object as mother");
    }

    @Test
    @DisplayName("Child is added to both parents' children list on reproduce()")
    void testChildAddedToParentsList() throws SimulationException {
        for (int i = 0; i < 10; i++) male.getRelationshipManager().interactSocially(female);
        male.getRelationshipManager().marry(female);

        Sim child = null;
        for (int attempt = 0; attempt < 20; attempt++) {
            child = male.getRelationshipManager().reproduce("TestBaby");
            if (child != null) break;
        }

        assertNotNull(child, "Should eventually produce a child");
        assertTrue(male.getRelationshipManager().getChildren().contains(child),
                "Father's children list should contain the child");
        assertTrue(female.getRelationshipManager().getChildren().contains(child),
                "Mother's children list should contain the child");
    }
}
