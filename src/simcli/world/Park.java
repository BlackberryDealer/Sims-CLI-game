package simcli.world;

import simcli.entities.actors.Sim;
import simcli.entities.actors.NPCSim;
import simcli.entities.managers.NPCProvider;
import simcli.ui.UIManager;

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
        UIManager.info(sim.getName() + " has arrived at " + getName() + ".");
        StringBuilder names = new StringBuilder();
        List<NPCSim> visitors = getVisitors();
        for (NPCSim npc : visitors) {
            if (names.length() > 0) names.append(", ");
            names.append(npc.getName());
        }
        UIManager.info("People here today: {W}" + names + "{/}");
    }
}
