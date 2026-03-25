package simcli.world.interactables;

import java.util.List;
import java.util.Scanner;
import simcli.engine.GameEngine;
import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.entities.actors.NPCSim;
import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;
import simcli.entities.models.SkillType;
import simcli.entities.models.Trait;
import simcli.utils.GameConstants;

public class ParkBench implements Interactable {
    private final GameEngine engine;

    public ParkBench(GameEngine engine) {
        this.engine = engine;
    }

    @Override
    public void interact(Sim sim, Scanner scanner, simcli.engine.TimeManager timeManager) throws SimulationException {
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
                boolean isSpouse = sim.getRelationshipManager().getSpouse() == target;

                // Build action menu dynamically
                SimulationLogger.prompt("\n[1] Chat\n[2] Joke\n[3] Argue\n");
                
                // Show Propose option if relationship is maxed and both are unmarried
                boolean canPropose = relScore >= GameConstants.MARRIAGE_THRESHOLD 
                        && sim.getRelationshipManager().getSpouse() == null 
                        && target.getRelationshipManager().getSpouse() == null;
                if (canPropose) {
                    SimulationLogger.prompt("[4] Propose Marriage\n");
                }

                SimulationLogger.prompt("Select action> ");
                int actionChoice = Integer.parseInt(scanner.nextLine().trim());
                
                boolean isSocialite = sim.hasTrait(Trait.SOCIALITE);
                if (actionChoice == 1) {
                    int relGain = isSocialite ? 8 : 5;
                    SimulationLogger.log(sim.getName() + " chats politely with " + target.getName() + ".");
                    sim.getRelationshipManager().increaseRelationship(target, relGain);
                    sim.getHappiness().increase(10);
                    sim.getEnergy().decrease(5);
                    sim.getSocial().increase(30);
                    sim.getSkillManager().addSkillExperience(SkillType.CHARISMA, isSocialite ? 8 : 5, sim.getName(), false);
                } else if (actionChoice == 2) {
                    int relGain = isSocialite ? 15 : 10;
                    SimulationLogger.log(sim.getName() + " tells a funny joke to " + target.getName() + "!");
                    sim.getRelationshipManager().increaseRelationship(target, relGain);
                    sim.getHappiness().increase(15);
                    sim.getEnergy().decrease(10);
                    sim.getSocial().increase(70);
                    sim.getSkillManager().addSkillExperience(SkillType.CHARISMA, isSocialite ? 15 : 10, sim.getName(), false);
                } else if (actionChoice == 3) {
                    SimulationLogger.log(sim.getName() + " argues bitterly with " + target.getName() + ".");
                    sim.getRelationshipManager().increaseRelationship(target, -15);
                    sim.getHappiness().decrease(10);
                    sim.getEnergy().decrease(15);
                    sim.getSocial().increase(20);
                } else if (actionChoice == 4 && canPropose) {
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
