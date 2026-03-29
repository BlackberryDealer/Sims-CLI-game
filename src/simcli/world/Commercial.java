package simcli.world;

import simcli.entities.actors.Sim;
import simcli.ui.UIManager;

/**
 * A shop building where Sims can browse and purchase items
 * (e.g. the Town Supermarket or The Bookshop).
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