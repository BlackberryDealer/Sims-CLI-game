package simcli.world;

import simcli.engine.GameEngine;
import simcli.entities.actors.Sim;
import simcli.entities.actors.NPCSim;
import simcli.world.interactables.ParkBench;

import java.util.List;

/**
 * A public park building where Sims can socialize with NPCs.
 */
public class Park extends Building {
    private final GameEngine engine;

    public Park(String name, GameEngine engine) {
        super(name);
        this.engine = engine;

        // Add the socializing interactable (with engine reference for marriage/baby/NPCs)
        this.addInteractable(new ParkBench(engine));
    }

    public List<NPCSim> getVisitors() {
        return engine.getNpcManager().getActiveNPCs();
    }
    
    @Override
    public void enter(Sim sim) {
        simcli.ui.UIManager.printMessage(sim.getName() + " has arrived at " + getName() + ".");
        StringBuilder names = new StringBuilder("People currently here: ");
        List<NPCSim> visitors = getVisitors();
        for (NPCSim npc : visitors) {
            names.append(npc.getName()).append(" ");
        }
        simcli.ui.UIManager.printMessage(names.toString().trim());
    }
}
