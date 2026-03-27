package simcli.world;

import simcli.entities.actors.Sim;
import simcli.ui.UIManager;

public class Commercial extends Building {
    public Commercial(String name) {
        super(name);
    }
    
    @Override
    public void enter(Sim sim) {
        UIManager.printMessage(sim.getName() + " walks into " + getName() + ".");
    }
}