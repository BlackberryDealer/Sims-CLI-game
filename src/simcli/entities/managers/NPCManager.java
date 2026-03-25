package simcli.entities.managers;

import simcli.entities.actors.NPCSim;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.utils.GameRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a persistent pool of NPCs in the world.
 */
public class NPCManager {
    private final List<NPCSim> activeNPCs;
    
    private static final String[] MALE_NAMES = {
        "Bob", "Charlie", "Edward", "George", "Ian", "Julia", "Kevin", "Mike", "Oscar", "Quincy", "Steve", "Victor", "Xander", "Zack"
    };
    
    private static final String[] FEMALE_NAMES = {
        "Alice", "Diana", "Fiona", "Hannah", "Julia", "Laura", "Nina", "Paula", "Rose", "Tina", "Wendy", "Yara"
    };

    private static final int MAX_NPCS = 8;
    
    public NPCManager() {
        this.activeNPCs = new ArrayList<>();
    }

    public List<NPCSim> getActiveNPCs() {
        return activeNPCs;
    }

    /**
     * Generates a completely random NPC with a gender-appropriate name.
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
     * Ensures the park has a minimum number of NPCs, without exceeding the global cap.
     */
    public void replenishNPCs(int targetCount) {
        int actualTarget = Math.min(targetCount, MAX_NPCS);
        while (activeNPCs.size() < actualTarget) {
            activeNPCs.add(generateRandomNPC());
        }
    }

    /**
     * Removes an NPC from the pool (e.g., when they join a household).
     */
    public void removeNPC(NPCSim npc) {
        activeNPCs.remove(npc);
    }

    /**
     * Adds an NPC to the pool (used during loading).
     */
    public void addNPC(NPCSim npc) {
        if (!activeNPCs.contains(npc)) {
            activeNPCs.add(npc);
        }
    }
}
