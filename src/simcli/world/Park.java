package simcli.world;

import simcli.entities.actors.Sim;
import simcli.entities.actors.NPCSim;
import simcli.entities.managers.NPCProvider;

import java.util.List;

/**
 * A public park building where Sims can socialize with NPCs.
 */
public class Park extends Building {
    private final NPCProvider npcProvider;

    public Park(String name, NPCProvider provider) {
        super(name);
        this.npcProvider = provider;
    }

    public List<NPCSim> getVisitors() {
        return npcProvider.getActiveNPCs();
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
