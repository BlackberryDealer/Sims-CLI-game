package simcli.world.interactables;

import simcli.engine.SimulationException;
import simcli.entities.NPCSim;
import simcli.entities.Sim;

import java.util.List;
import java.util.Scanner;

/**
 * Interactable placed inside a Park that allows the player to socialize with NPCs.
 * Extracted from the anonymous inner class that was previously in Park.java.
 */
public class ParkBench implements Interactable {
    private final List<NPCSim> visitors;

    public ParkBench(List<NPCSim> visitors) {
        this.visitors = visitors;
    }

    @Override
    public void interact(Sim sim, Scanner scanner, simcli.engine.TimeManager timeManager) throws SimulationException {
        sim.setCurrentAction(simcli.entities.ActionState.SOCIALIZING);
        simcli.ui.UIManager.displayActionAnimation(sim);

        simcli.ui.UIManager.printMessage("\n=== Socialize at the Park ===");
        for (int i = 0; i < visitors.size(); i++) {
            NPCSim npc = visitors.get(i);
            simcli.ui.UIManager.printMessage("[" + (i + 1) + "] Talk to " + npc.getName()
                    + " (Relationship: " + npc.getRelationshipScore() + ")");
        }
        simcli.ui.UIManager.printMessage("[0] Go back");
        simcli.ui.UIManager.prompt("Select person> ");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice > 0 && choice <= visitors.size()) {
                NPCSim target = visitors.get(choice - 1);
                simcli.ui.UIManager.printMessage(
                        sim.getName() + " talks to " + target.getName() + ". They seem to enjoy the chat!");
                target.increaseRelationship(5);
                sim.getHappiness().increase(15);
                sim.getEnergy().decrease(10);
                simcli.ui.UIManager.printMessage(
                        "Relationship with " + target.getName() + " is now " + target.getRelationshipScore() + ".");
            }
        } catch (NumberFormatException e) {
            simcli.ui.UIManager.printMessage("Invalid selection.");
        }
    }

    @Override
    public String getObjectName() {
        return "Socialize with NPCs";
    }
}
