package simcli.world;

import simcli.entities.Sim;

public class Commercial extends Building {
    public Commercial(String name) {
        super(name);
    }
    
    @Override
    public void enter(Sim sim) {
        simcli.ui.UIManager.printMessage(sim.getName() + " walks into " + this.name + ".");
    }
}