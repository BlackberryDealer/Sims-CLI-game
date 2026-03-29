package simcli.entities.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import simcli.entities.actors.NPCSim;
import simcli.utils.GameConstants;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link NPCManager} — the NPC pool manager.
 *
 * <p>Coverage:
 * <ul>
 *     <li>Pool starts empty</li>
 *     <li>replenishNPCs() fills pool up to the requested count</li>
 *     <li>replenishNPCs() never exceeds MAX_NPCS cap</li>
 *     <li>removeNPC() correctly removes an NPC from the pool</li>
 *     <li>addNPC() adds an NPC and prevents duplicates</li>
 *     <li>generateRandomNPC() returns valid NPCs</li>
 * </ul>
 */
@DisplayName("NPCManager — NPC Pool Management Tests")
public class NPCManagerTest {

    private NPCManager npcManager;

    @BeforeEach
    void setUp() {
        npcManager = new NPCManager();
    }

    // -----------------------------------------------------------------------
    // Initial state
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("NPC pool starts empty")
    void testPoolStartsEmpty() {
        assertTrue(npcManager.getActiveNPCs().isEmpty(),
                "NPC pool should be empty after construction");
    }

    // -----------------------------------------------------------------------
    // replenishNPCs()
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("replenishNPCs() fills pool to requested count")
    void testReplenishFillsPool() {
        npcManager.replenishNPCs(GameConstants.MAX_NPCS);
        assertEquals(GameConstants.MAX_NPCS, npcManager.getActiveNPCs().size(),
                "Pool should have exactly MAX_NPCS NPCs after replenishing");
    }

    @Test
    @DisplayName("replenishNPCs() never exceeds MAX_NPCS cap")
    void testReplenishCapsAtMax() {
        npcManager.replenishNPCs(100); // Request way more than limit
        assertEquals(GameConstants.MAX_NPCS, npcManager.getActiveNPCs().size(),
                "Pool should not exceed MAX_NPCS regardless of target count");
    }

    @Test
    @DisplayName("replenishNPCs() with 0 does nothing")
    void testReplenishWithZero() {
        npcManager.replenishNPCs(0);
        assertTrue(npcManager.getActiveNPCs().isEmpty(),
                "Replenishing with 0 should leave the pool empty");
    }

    @Test
    @DisplayName("replenishNPCs() does not add more when pool is already full")
    void testReplenishWhenAlreadyFull() {
        npcManager.replenishNPCs(GameConstants.MAX_NPCS);
        int sizeBefore = npcManager.getActiveNPCs().size();

        npcManager.replenishNPCs(GameConstants.MAX_NPCS);

        assertEquals(sizeBefore, npcManager.getActiveNPCs().size(),
                "Replenishing a full pool should not add more NPCs");
    }

    // -----------------------------------------------------------------------
    // removeNPC()
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("removeNPC() removes the specified NPC from the pool")
    void testRemoveNPC() {
        npcManager.replenishNPCs(GameConstants.MAX_NPCS);
        NPCSim npcToRemove = npcManager.getActiveNPCs().get(0);
        int sizeBefore = npcManager.getActiveNPCs().size();

        npcManager.removeNPC(npcToRemove);

        assertEquals(sizeBefore - 1, npcManager.getActiveNPCs().size(),
                "Pool size should decrease by 1 after removing an NPC");
        assertFalse(npcManager.getActiveNPCs().contains(npcToRemove),
                "Removed NPC should no longer be in the pool");
    }

    // -----------------------------------------------------------------------
    // addNPC()
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("addNPC() adds an NPC to the pool")
    void testAddNPC() {
        NPCSim npc = npcManager.generateRandomNPC();
        npcManager.addNPC(npc);

        assertEquals(1, npcManager.getActiveNPCs().size());
        assertTrue(npcManager.getActiveNPCs().contains(npc));
    }

    @Test
    @DisplayName("addNPC() prevents duplicate entries")
    void testAddNPCPreventsDuplicates() {
        NPCSim npc = npcManager.generateRandomNPC();
        npcManager.addNPC(npc);
        npcManager.addNPC(npc); // Add same NPC again

        assertEquals(1, npcManager.getActiveNPCs().size(),
                "addNPC should not add the same NPC twice");
    }

    // -----------------------------------------------------------------------
    // generateRandomNPC()
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("generateRandomNPC() returns a non-null NPC with valid properties")
    void testGenerateRandomNPCProperties() {
        NPCSim npc = npcManager.generateRandomNPC();

        assertNotNull(npc, "Generated NPC should not be null");
        assertNotNull(npc.getName(), "NPC should have a name");
        assertFalse(npc.getName().isEmpty(), "NPC name should not be empty");
        assertTrue(npc.getAge() >= 18 && npc.getAge() <= 68,
                "NPC age should be between 18 and 68, but was: " + npc.getAge());
        assertNotNull(npc.getGender(), "NPC should have a gender");
        assertNotNull(npc.getCareer(), "NPC should have a career assigned");
    }

    @Test
    @DisplayName("generateRandomNPC() creates distinct NPC instances")
    void testGenerateRandomNPCDistinctInstances() {
        NPCSim npc1 = npcManager.generateRandomNPC();
        NPCSim npc2 = npcManager.generateRandomNPC();

        assertNotSame(npc1, npc2, "Each call should create a new NPC object");
    }
}
