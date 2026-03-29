package simcli.entities.managers;

import simcli.entities.actors.NPCSim;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.utils.GameRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a persistent pool of NPC Sims that inhabit the world.
 *
 * <p>NPCs are randomly generated with gender-appropriate names, ages,
 * and careers. The pool is replenished automatically when the count
 * drops below a target threshold (e.g. when an NPC is married into
 * the player's household). Implements {@link NPCProvider} for
 * loose coupling with the {@link simcli.world.Park}.</p>
 */
public class NPCManager implements NPCProvider {

    /** The list of currently active NPCs in the world. */
    private final List<NPCSim> activeNPCs;

    /** Pool of male NPC names. */
    private static final String[] MALE_NAMES = {
        "Bob", "Charlie", "Edward", "George", "Ian", "Julia", "Kevin", "Mike", "Oscar", "Quincy", "Steve", "Victor", "Xander", "Zack"
    };

    /** Pool of female NPC names. */
    private static final String[] FEMALE_NAMES = {
        "Alice", "Diana", "Fiona", "Hannah", "Julia", "Laura", "Nina", "Paula", "Rose", "Tina", "Wendy", "Yara"
    };

    /**
     * Constructs a new NPCManager with an empty NPC pool.
     */
    public NPCManager() {
        this.activeNPCs = new ArrayList<>();
    }

    /**
     * Returns the list of currently active NPCs in the world.
     *
     * @return the active NPC list.
     */
    public List<NPCSim> getActiveNPCs() {
        return activeNPCs;
    }

    /**
     * Generates a completely random NPC with a gender-appropriate name,
     * a random age between 18 and 67, and a random career.
     *
     * @return a newly created {@link NPCSim}.
     */
    public NPCSim generateRandomNPC() {
        Gender gender = GameRandom.RANDOM.nextBoolean() ? Gender.MALE : Gender.FEMALE;
        String name;
        if (gender == Gender.MALE) {
            name = MALE_NAMES[GameRandom.RANDOM.nextInt(MALE_NAMES.length)];
        } else {
            name = FEMALE_NAMES[GameRandom.RANDOM.nextInt(FEMALE_NAMES.length)];
        }

        int age = 18 + GameRandom.RANDOM.nextInt(50); // Age 18 to 68

        Job[] jobs = Job.values();
        Job job = jobs[GameRandom.RANDOM.nextInt(jobs.length)];

        return new NPCSim(name, age, gender, job);
    }

    /**
     * Ensures the NPC pool has at least {@code targetCount} NPCs,
     * generating new ones as needed without exceeding the global cap
     * defined by {@link simcli.utils.GameConstants#MAX_NPCS}.
     *
     * @param targetCount the desired minimum number of NPCs.
     */
    public void replenishNPCs(int targetCount) {
        int actualTarget = Math.min(targetCount, simcli.utils.GameConstants.MAX_NPCS);
        while (activeNPCs.size() < actualTarget) {
            activeNPCs.add(generateRandomNPC());
        }
    }

    /**
     * Removes an NPC from the active pool (e.g. when they are married
     * into the player's household).
     *
     * @param npc the NPC to remove.
     */
    public void removeNPC(NPCSim npc) {
        activeNPCs.remove(npc);
    }

    /**
     * Adds an NPC to the active pool if not already present.
     * Used during save/load restoration.
     *
     * @param npc the NPC to add.
     */
    public void addNPC(NPCSim npc) {
        if (!activeNPCs.contains(npc)) {
            activeNPCs.add(npc);
        }
    }
}
