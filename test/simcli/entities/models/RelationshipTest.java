package simcli.entities.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import simcli.entities.actors.Sim;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link Relationship} model — score tracking and status transitions.
 *
 * <p>Coverage:
 * <ul>
 *     <li>Initial state (score 0, STRANGER)</li>
 *     <li>setFriendshipScore() clamping [0, 100]</li>
 *     <li>updateStatus() threshold transitions</li>
 * </ul>
 */
@DisplayName("Relationship Model — Score & Status Tests")
public class RelationshipTest {

    private Sim target;
    private Relationship rel;

    @BeforeEach
    void setUp() {
        target = new Sim("Bob", 25, Gender.MALE, Job.UNEMPLOYED);
        rel = new Relationship(target);
    }

    // -----------------------------------------------------------------------
    // Initial state
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("New relationship starts at score 0 with STRANGER status")
    void testInitialState() {
        assertEquals(0, rel.getFriendshipScore());
        assertEquals(RelationshipStatus.STRANGER, rel.getStatus());
        assertSame(target, rel.getTargetSim());
    }

    // -----------------------------------------------------------------------
    // Score clamping
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("setFriendshipScore() clamps to 100 max")
    void testScoreClampsAtMax() {
        rel.setFriendshipScore(150);
        assertEquals(100, rel.getFriendshipScore());
    }

    @Test
    @DisplayName("setFriendshipScore() clamps to 0 min")
    void testScoreClampsAtMin() {
        rel.setFriendshipScore(-50);
        assertEquals(0, rel.getFriendshipScore());
    }

    @Test
    @DisplayName("setFriendshipScore() accepts valid value")
    void testScoreAcceptsValid() {
        rel.setFriendshipScore(42);
        assertEquals(42, rel.getFriendshipScore());
    }

    // -----------------------------------------------------------------------
    // Status transitions (updateStatus)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Score 0-19 → STRANGER")
    void testStatusStranger() {
        rel.setFriendshipScore(10);
        rel.updateStatus();
        assertEquals(RelationshipStatus.STRANGER, rel.getStatus());
    }

    @Test
    @DisplayName("Score 20-49 → ACQUAINTANCE")
    void testStatusAcquaintance() {
        rel.setFriendshipScore(35);
        rel.updateStatus();
        assertEquals(RelationshipStatus.ACQUAINTANCE, rel.getStatus());
    }

    @Test
    @DisplayName("Score 50-79 → FRIEND")
    void testStatusFriend() {
        rel.setFriendshipScore(65);
        rel.updateStatus();
        assertEquals(RelationshipStatus.FRIEND, rel.getStatus());
    }

    @Test
    @DisplayName("Score 80+ → ROMANTIC")
    void testStatusRomantic() {
        rel.setFriendshipScore(85);
        rel.updateStatus();
        assertEquals(RelationshipStatus.ROMANTIC, rel.getStatus());
    }

    @Test
    @DisplayName("Score at exact boundary 20 → ACQUAINTANCE")
    void testBoundary20() {
        rel.setFriendshipScore(20);
        rel.updateStatus();
        assertEquals(RelationshipStatus.ACQUAINTANCE, rel.getStatus());
    }

    @Test
    @DisplayName("Score at exact boundary 50 → FRIEND")
    void testBoundary50() {
        rel.setFriendshipScore(50);
        rel.updateStatus();
        assertEquals(RelationshipStatus.FRIEND, rel.getStatus());
    }

    @Test
    @DisplayName("Score at exact boundary 80 → ROMANTIC")
    void testBoundary80() {
        rel.setFriendshipScore(80);
        rel.updateStatus();
        assertEquals(RelationshipStatus.ROMANTIC, rel.getStatus());
    }
}
