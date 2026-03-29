package simcli.world;

import simcli.entities.actors.Sim;
import simcli.ui.UIManager;

/**
 * Represents a Commercial location or interactable object.
 */
public class Commercial extends Building {
    public Commercial(String name) {
        super(name);
    }
    
    @Override
    public void enter(Sim sim) {
        UIManager.printMessage(sim.getName() + " walks into " + getName() + ".");
    }
}