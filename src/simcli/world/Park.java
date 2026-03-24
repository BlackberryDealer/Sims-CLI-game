package simcli.world;

import simcli.entities.actors.Sim;
import simcli.entities.actors.NPCSim;
import simcli.world.interactables.ParkBench;

import java.util.ArrayList;
import java.util.List;

/**
 * A public park building where Sims can socialize with NPCs.
 */
public class Park extends Building {
    private final List<NPCSim> visitors;
    
    public Park(String name) {
        super(name);
        this.visitors = new ArrayList<>();
        
        // Populate park with some default NPCs
        this.visitors.add(new NPCSim("Alice", 25));
        this.visitors.add(new NPCSim("Bob", 30));
        this.visitors.add(new NPCSim("Charlie", 22));

        // Add the socializing interactable (extracted to its own class)
        this.addInteractable(new ParkBench(this.visitors));
    }

    public List<NPCSim> getVisitors() {
        return visitors;
    }
    
    @Override
    public void enter(Sim sim) {
        simcli.ui.UIManager.printMessage(sim.getName() + " has arrived at " + getName() + ".");
        StringBuilder names = new StringBuilder("People currently here: ");
        for (NPCSim npc : visitors) {
            names.append(npc.getName()).append(" ");
        }
        simcli.ui.UIManager.printMessage(names.toString().trim());
    }
}
