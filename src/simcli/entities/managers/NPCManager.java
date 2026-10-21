package simcli.entities.managers;

import simcli.engine.SimulationLogger;
import simcli.entities.actors.NPCSim;
import simcli.entities.models.Gender;
import simcli.entities.models.Job;
import simcli.utils.GameConstants;
import simcli.utils.GameRandom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages the NPC Sims that inhabit the town.
 *
 * <p>NPCs are randomly generated with gender-appropriate names, ages,
 * careers and personality traits. The <em>active</em> NPCs are today's
 * park visitors; the rest of the townsfolk are elsewhere but remembered,
 * so people the household has met turn up again on later days. Each
 * morning {@link #startNewDay(int, SimulationLogger)} sends a few visitors
 * home and brings in others, and every NPC ages — moving away once they
 * reach {@link GameConstants#NPC_MOVE_AWAY_AGE}.</p>
 *
 * <p>The visitor pool is replenished automatically when the count drops
 * below a target threshold (e.g. when an NPC is married into the
 * player's household). Implements {@link NPCProvider} for loose coupling
 * with the {@link simcli.world.Park}.</p>
 */
public class NPCManager implements NPCProvider {

    /** Most townsfolk remembered while away from the park; the longest-gone are forgotten first. */
    private static final int MAX_TOWNSFOLK = 24;

    /** Today's park visitors. */
    private final List<NPCSim> activeNPCs;

    /** NPCs who live in town but aren't at the park today. */
    private final List<NPCSim> townsfolk;

    /** Pool of male NPC names. */
    private static final String[] MALE_NAMES = {
        "Bob", "Charlie", "Edward", "George", "Ian", "Henry", "Kevin", "Mike", "Oscar", "Quincy", "Steve", "Victor", "Xander", "Zack"
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
        this.townsfolk = new ArrayList<>();
    }

    /**
     * Returns today's park visitors.
     *
     * @return the active NPC list.
     */
    public List<NPCSim> getActiveNPCs() {
        return activeNPCs;
    }

    /**
     * Returns the townsfolk who aren't at the park today.
     *
     * @return the absent NPC list.
     */
    public List<NPCSim> getTownsfolk() {
        return townsfolk;
    }

    /**
     * Adds an NPC who lives in town but isn't at the park today.
     * Used during save/load restoration.
     *
     * @param npc the NPC to add.
     */
    public void addTownsperson(NPCSim npc) {
        if (!townsfolk.contains(npc) && !activeNPCs.contains(npc)) {
            townsfolk.add(npc);
        }
    }

    /**
     * Starts a new day in town: every NPC ages on schedule (the oldest move
     * away), then {@link GameConstants#DAILY_VISITORS_LEAVING} visitors go
     * home and are replaced — about half the time by someone the town
     * already knows, otherwise by a new face.
     *
     * @param day    the day number that has just begun.
     * @param logger where to report NPCs moving away.
     */
    public void startNewDay(int day, SimulationLogger logger) {
        if (day % GameConstants.DAYS_PER_AGE_TICK == 0) {
            ageEveryone(activeNPCs, logger);
            ageEveryone(townsfolk, logger);
        }

        int leaving = Math.min(GameConstants.DAILY_VISITORS_LEAVING, activeNPCs.size());
        for (int i = 0; i < leaving; i++) {
            townsfolk.add(activeNPCs.remove(GameRandom.RANDOM.nextInt(activeNPCs.size())));
        }
        while (activeNPCs.size() < GameConstants.MAX_NPCS) {
            // someone who just left won't turn straight round and come back
            int returnable = townsfolk.size() - leaving;
            if (returnable > 0 && GameRandom.RANDOM.nextInt(100) < GameConstants.RETURNING_VISITOR_CHANCE) {
                activeNPCs.add(townsfolk.remove(GameRandom.RANDOM.nextInt(returnable)));
            } else {
                activeNPCs.add(generateRandomNPC());
            }
        }
        while (townsfolk.size() > MAX_TOWNSFOLK) {
            townsfolk.remove(0);
        }
    }

    private static void ageEveryone(List<NPCSim> npcs, SimulationLogger logger) {
        Iterator<NPCSim> it = npcs.iterator();
        while (it.hasNext()) {
            NPCSim npc = it.next();
            npc.ageUp(false);
            if (npc.getAge() >= GameConstants.NPC_MOVE_AWAY_AGE) {
                it.remove();
                logger.log(npc.getName() + " (" + npc.getAge() + ") has retired and moved away from town.");
            }
        }
    }

    /**
     * Generates a completely random NPC with a gender-appropriate name,
     * a random age between 18 and 67, and a random career.
     *
     * @return a newly created {@link NPCSim}.
     */
    public NPCSim generateRandomNPC() {
        Gender gender = GameRandom.RANDOM.nextBoolean() ? Gender.MALE : Gender.FEMALE;
        String name = pickUnusedName(gender == Gender.MALE ? MALE_NAMES : FEMALE_NAMES);

        int age = 18 + GameRandom.RANDOM.nextInt(50); // Age 18 to 68

        Job[] jobs = Job.values();
        Job job = jobs[GameRandom.RANDOM.nextInt(jobs.length)];

        return new NPCSim(name, age, gender, job);
    }

    /**
     * Picks a random name from the pool that no NPC in town is using, so the
     * park never shows two visitors with the same name. Falls back to any
     * name if every name in the pool is taken.
     */
    private String pickUnusedName(String[] pool) {
        List<NPCSim> everyone = new ArrayList<>(activeNPCs);
        everyone.addAll(townsfolk);
        List<String> free = new ArrayList<>();
        for (String candidate : pool) {
            boolean taken = false;
            for (NPCSim npc : everyone) {
                if (npc.getName().equals(candidate)) {
                    taken = true;
                    break;
                }
            }
            if (!taken) {
                free.add(candidate);
            }
        }
        if (free.isEmpty()) {
            return pool[GameRandom.RANDOM.nextInt(pool.length)];
        }
        return free.get(GameRandom.RANDOM.nextInt(free.size()));
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
        townsfolk.remove(npc);
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
