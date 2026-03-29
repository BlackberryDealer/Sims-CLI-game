package simcli.world.interactables;

import java.util.List;
import java.util.Scanner;
import simcli.engine.GameEngine;
import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.engine.TimeManager;
import simcli.entities.actors.NPCSim;
import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;
import simcli.entities.models.SkillType;
import simcli.entities.models.SocialAction;
import simcli.entities.models.Trait;
import simcli.utils.GameConstants;

/**
 * Represents a ParkBench location or interactable object.
 */
public class ParkBench implements Interactable {
    private final GameEngine engine;

    public ParkBench(GameEngine engine) {
        this.engine = engine;
    }

    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException {
        sim.setCurrentAction(ActionState.SOCIALIZING);
        SimulationLogger.logAnimation(sim);

        List<NPCSim> visitors = engine.getNpcManager().getActiveNPCs();

        SimulationLogger.prompt("\n=== Socialize at the Park ===\n");
        for (int i = 0; i < visitors.size(); i++) {
            NPCSim npc = visitors.get(i);
            int relScore = sim.getRelationshipManager().getRelationship(npc);
            String spouseTag = (sim.getRelationshipManager().getSpouse() == npc) ? " [SPOUSE]" : "";
            SimulationLogger.prompt("[" + (i + 1) + "] " + npc.getName() 
                    + ", " + npc.getAge() + ", " + npc.getCareer().getTitle()
                    + " (Relationship: " + relScore + "/100)" + spouseTag + "\n");
        }
        SimulationLogger.prompt("[0] Go back\nSelect person> ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice > 0 && choice <= visitors.size()) {
                NPCSim target = visitors.get(choice - 1);
                int relScore = sim.getRelationshipManager().getRelationship(target);

                // Build action menu dynamically
                SocialAction[] actions = {SocialAction.CHAT, SocialAction.JOKE, SocialAction.ARGUE};
                for (int i = 0; i < actions.length; i++) {
                    SimulationLogger.prompt("[" + (i + 1) + "] " + actions[i].getDisplayName() + "\n");
                }
                
                // Show Propose option if relationship is maxed and both are unmarried
                boolean canPropose = relScore >= GameConstants.MARRIAGE_THRESHOLD 
                        && sim.getRelationshipManager().getSpouse() == null 
                        && target.getRelationshipManager().getSpouse() == null;
                int proposeChoice = actions.length + 1;
                if (canPropose) {
                    SimulationLogger.prompt("[" + proposeChoice + "] Propose Marriage\n");
                }

                SimulationLogger.prompt("Select action> ");
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

                    SimulationLogger.log(sim.getName() + " performs " + selectedAction.getDisplayName().toLowerCase() + " with " + target.getName() + ".");
                    
                    sim.getRelationshipManager().increaseRelationship(target, relGain);
                    sim.getHappiness().increase(happyGain);
                    sim.getEnergy().decrease(-energyLoss); // energyChange is negative in enum
                    sim.getSocial().increase(socialGain);
                    if (xpGain > 0) {
                        sim.getSkillManager().addSkillExperience(SkillType.CHARISMA, xpGain, sim.getName(), false);
                    }
                } else if (actionChoice == proposeChoice && canPropose) {
                    handleProposal(sim, target);
                } else {
                    SimulationLogger.logWarning("Invalid action selection.");
                    return;
                }
                
                SimulationLogger.log("Relationship with " + target.getName() + " is now " + sim.getRelationshipManager().getRelationship(target) + "/100.");
            }
        } catch (NumberFormatException e) {
            SimulationLogger.logWarning("Invalid selection.");
        }
    }

    /**
     * Handles the marriage proposal flow. On success, adds the NPC to the neighborhood.
     */
    private void handleProposal(Sim sim, NPCSim target) {
        boolean married = sim.getRelationshipManager().marry(target);
        if (married) {
            // Remove from park pool and replenish
            engine.getNpcManager().removeNPC(target);
            engine.getNpcManager().replenishNPCs(3);

            // Add the NPC to the neighborhood as a playable character
            if (!engine.getNeighborhood().contains(target)) {
                engine.getNeighborhood().add(target);
                SimulationLogger.log(target.getName() + " has joined the household and is now playable!");
            }
        } else {
            SimulationLogger.log("The proposal was rejected. Build a stronger relationship first.");
        }
    }


    @Override
    public String getObjectName() {
        return "Socialize with NPCs";
    }
}
