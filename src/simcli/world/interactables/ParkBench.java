package simcli.world.interactables;

import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.entities.ActionState;
import simcli.entities.NPCSim;
import simcli.entities.Sim;
import simcli.entities.SkillType;

import java.util.List;
import java.util.Scanner;

public class ParkBench implements Interactable {
    private final List<NPCSim> visitors;

    public ParkBench(List<NPCSim> visitors) {
        this.visitors = visitors;
    }

    @Override
    public void interact(Sim sim, Scanner scanner, simcli.engine.TimeManager timeManager) throws SimulationException {
        sim.setCurrentAction(ActionState.SOCIALIZING);
        SimulationLogger.logAnimation(sim);

        SimulationLogger.prompt("\n=== Socialize at the Park ===\n");
        for (int i = 0; i < visitors.size(); i++) {
            NPCSim npc = visitors.get(i);
            SimulationLogger.prompt("[" + (i + 1) + "] Talk to " + npc.getName()
                    + " (Relationship: " + sim.getRelationship(npc) + ")\n");
        }
        SimulationLogger.prompt("[0] Go back\nSelect person> ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice > 0 && choice <= visitors.size()) {
                NPCSim target = visitors.get(choice - 1);
                
                SimulationLogger.prompt("\n[1] Chat\n[2] Joke\n[3] Argue\nSelect action> ");
                int actionChoice = Integer.parseInt(scanner.nextLine().trim());
                
                if (actionChoice == 1) {
                    SimulationLogger.log(sim.getName() + " chats politely with " + target.getName() + ".");
                    sim.increaseRelationship(target, 5);
                    sim.getFun().increase(10);
                    sim.getEnergy().decrease(5);
                    sim.getSkillManager().addSkillExperience(SkillType.CHARISMA, 5, sim.getName(), false);
                } else if (actionChoice == 2) {
                    SimulationLogger.log(sim.getName() + " tells a funny joke to " + target.getName() + "!");
                    sim.increaseRelationship(target, 10);
                    sim.getFun().increase(15);
                    sim.getEnergy().decrease(10);
                    sim.getSkillManager().addSkillExperience(SkillType.CHARISMA, 10, sim.getName(), false);
                } else if (actionChoice == 3) {
                    SimulationLogger.log(sim.getName() + " argues bitterly with " + target.getName() + ".");
                    sim.increaseRelationship(target, -15);
                    sim.getFun().decrease(10);
                    sim.getEnergy().decrease(15);
                } else {
                    SimulationLogger.logWarning("Invalid action selection.");
                    return;
                }
                
                SimulationLogger.log("Relationship with " + target.getName() + " is now " + sim.getRelationship(target) + ".");
            }
        } catch (NumberFormatException e) {
            SimulationLogger.logWarning("Invalid selection.");
        }
    }

    @Override
    public String getObjectName() {
        return "Socialize with NPCs";
    }
}
