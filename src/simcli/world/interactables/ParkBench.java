package simcli.world.interactables;

import java.util.List;
import java.util.Scanner;

import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.engine.TimeManager;
import simcli.entities.actors.NPCSim;
import simcli.entities.actors.Sim;
import simcli.entities.managers.NPCManager;
import simcli.entities.models.ActionState;
import simcli.entities.models.SkillType;
import simcli.entities.models.SocialAction;
import simcli.entities.models.Trait;
import simcli.utils.GameConstants;

/**
 * Represents a park-bench interactable where the active Sim can socialize
 * with NPC visitors.
 *
 * <p>Previously held a direct reference to {@code GameEngine}, creating a
 * circular dependency through {@code WorldManager}. Now receives only the
 * fine-grained collaborators it actually needs: an {@link NPCManager} for
 * the NPC pool and a mutable {@code neighborhood} list for adding married
 * NPCs to the household.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *     <li>Display the NPC socialization menu.</li>
 *     <li>Apply relationship / need / skill changes for social actions.</li>
 *     <li>Handle marriage proposals — removes NPC from pool, adds to household.</li>
 * </ul>
 */
public class ParkBench implements Interactable {

    /** Provides and manages the pool of park NPCs. */
    private final NPCManager npcManager;

    /** The mutable household list — married NPCs are added here. */
    private final List<Sim> neighborhood;

    /**
     * Creates a new {@code ParkBench}.
     *
     * @param npcManager   manages the NPC visitor pool.
     * @param neighborhood the mutable household list for adding married NPCs.
     */
    public ParkBench(NPCManager npcManager, List<Sim> neighborhood) {
        this.npcManager  = npcManager;
        this.neighborhood = neighborhood;
    }

    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException {
        sim.setCurrentAction(ActionState.SOCIALIZING);
        SimulationLogger.getInstance().logAnimation(sim);

        List<NPCSim> visitors = npcManager.getActiveNPCs();

        SimulationLogger.getInstance().prompt("\n=== Socialize at the Park ===\n");
        for (int i = 0; i < visitors.size(); i++) {
            NPCSim npc = visitors.get(i);
            int relScore = sim.getRelationshipManager().getRelationship(npc);
            String spouseTag = (sim.getRelationshipManager().getSpouse() == npc) ? " [SPOUSE]" : "";
            SimulationLogger.getInstance().prompt("[" + (i + 1) + "] " + npc.getName()
                    + ", " + npc.getAge() + ", " + npc.getCareer().getTitle()
                    + " (Relationship: " + relScore + "/100)" + spouseTag + "\n");
        }
        SimulationLogger.getInstance().prompt("[0] Go back\nSelect person> ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice > 0 && choice <= visitors.size()) {
                NPCSim target = visitors.get(choice - 1);
                int relScore = sim.getRelationshipManager().getRelationship(target);

                SocialAction[] actions = {SocialAction.CHAT, SocialAction.JOKE, SocialAction.ARGUE};
                for (int i = 0; i < actions.length; i++) {
                    SimulationLogger.getInstance().prompt("[" + (i + 1) + "] " + actions[i].getDisplayName() + "\n");
                }

                boolean canPropose = relScore >= GameConstants.MARRIAGE_THRESHOLD
                        && sim.getRelationshipManager().getSpouse() == null
                        && target.getRelationshipManager().getSpouse() == null;
                int proposeChoice = actions.length + 1;
                if (canPropose) {
                    SimulationLogger.getInstance().prompt("[" + proposeChoice + "] Propose Marriage\n");
                }

                SimulationLogger.getInstance().prompt("Select action> ");
                int actionChoice = Integer.parseInt(scanner.nextLine().trim());

                if (actionChoice > 0 && actionChoice <= actions.length) {
                    SocialAction selectedAction = actions[actionChoice - 1];
                    boolean isSocialite = sim.hasTrait(Trait.SOCIALITE);
                    double multiplier = isSocialite ? GameConstants.BONUS_TIMES : 1.0;

                    int relGain = (int) (selectedAction.getRelationshipChange() * multiplier);
                    int happyGain = (int) (selectedAction.getHappinessChange() * multiplier);
                    int energyLoss = (int) (selectedAction.getEnergyChange() * multiplier);
                    int socialGain = (int) (selectedAction.getSocialChange() * multiplier);
                    int xpGain = (int) (selectedAction.getSkillXP() * multiplier);

                    SimulationLogger.getInstance().log(sim.getName() + " performs " + selectedAction.getDisplayName().toLowerCase() + " with " + target.getName() + ".");
                    
                    sim.getRelationshipManager().increaseRelationship(target, relGain);
                    sim.getHappiness().increase(happyGain);
                    sim.getEnergy().decrease(-energyLoss);
                    sim.getSocial().increase(socialGain);
                    if (xpGain > 0) {
                        sim.getSkillManager().addSkillExperience(SkillType.CHARISMA, xpGain, sim.getName(), false);
                    }
                } else if (actionChoice == proposeChoice && canPropose) {
                    handleProposal(sim, target);
                } else {
                    SimulationLogger.getInstance().logWarning("Invalid action selection.");
                    return;
                }
                
                SimulationLogger.getInstance().log("Relationship with " + target.getName() + " is now " + sim.getRelationshipManager().getRelationship(target) + "/100.");
            }
        } catch (NumberFormatException e) {
            SimulationLogger.getInstance().logWarning("Invalid selection.");
        }
    }

    /**
     * Handles the marriage proposal flow. On success, removes the NPC from
     * the park pool, replenishes, and adds the new spouse to the household.
     *
     * @param sim    the proposing Sim.
     * @param target the NPC being proposed to.
     */
    private void handleProposal(Sim sim, NPCSim target) {
        boolean married = sim.getRelationshipManager().marry(target);
        if (married) {
            npcManager.removeNPC(target);
            npcManager.replenishNPCs(3);

            if (!neighborhood.contains(target)) {
                neighborhood.add(target);
                SimulationLogger.getInstance().log(target.getName() + " has joined the household and is now playable!");
            }
        } else {
            SimulationLogger.getInstance().log("The proposal was rejected. Build a stronger relationship first.");
        }
    }

    @Override
    public String getObjectName() {
        return "Socialize with NPCs";
    }
}
