package simcli.world;

import simcli.world.interactables.Interactable;
import simcli.entities.Sim;
import simcli.entities.NPCSim;
import simcli.engine.SimulationException;
import java.util.ArrayList;
import java.util.List;

public class Park extends Building {
    private List<NPCSim> visitors;
    
    public Park(String name) {
        super(name);
        this.visitors = new ArrayList<>();
        
        // Populate park with some default NPCs
        this.visitors.add(new NPCSim("Alice", 25));
        this.visitors.add(new NPCSim("Bob", 30));
        this.visitors.add(new NPCSim("Charlie", 22));

        // Add a general "Talk to people" interactable to the building
        this.addInteractable(new Interactable() {
            @Override
            public void interact(Sim sim, java.util.Scanner scanner, simcli.engine.TimeManager timeManager) throws SimulationException {
                System.out.println("\n=== Socialize at the Park ===");
                for (int i = 0; i < visitors.size(); i++) {
                    NPCSim npc = visitors.get(i);
                    System.out.println("[" + (i+1) + "] Talk to " + npc.getName() + " (Relationship: " + npc.getRelationshipScore() + ")");
                }
                System.out.println("[0] Go back");
                System.out.print("Select person> ");
                try {
                    int choice = Integer.parseInt(scanner.nextLine().trim());
                    if (choice > 0 && choice <= visitors.size()) {
                        NPCSim target = visitors.get(choice - 1);
                        System.out.println(sim.getName() + " talks to " + target.getName() + ". They seem to enjoy the chat!");
                        target.increaseRelationship(5);
                        sim.getHappiness().increase(15);
                        sim.getEnergy().decrease(10);
                        System.out.println("Relationship with " + target.getName() + " is now " + target.getRelationshipScore() + ".");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid selection.");
                }
            }

            @Override
            public String getObjectName() {
                return "Socialize with NPCs";
            }
        });
    }
    
    @Override
    public void enter(Sim sim) {
        System.out.println(sim.getName() + " has arrived at " + this.name + ".");
        System.out.print("People currently here: ");
        for (NPCSim npc : visitors) {
            System.out.print(npc.getName() + " ");
        }
        System.out.println();
    }
}
