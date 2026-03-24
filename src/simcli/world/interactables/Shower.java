package simcli.world.interactables;

import simcli.engine.SimulationException;
import simcli.entities.actors.Sim;

public class Shower implements Interactable {

    @Override
    public String getObjectName() {
        return "Shower";
    }

    @Override
    public void interact(Sim sim, java.util.Scanner scanner, simcli.engine.TimeManager timeManager) throws SimulationException {
        sim.setCurrentAction(simcli.entities.actors.ActionState.PLAYING); // closest match for a refreshing activity
        simcli.ui.UIManager.printMessage(sim.getName() + " takes a long, refreshing shower.");
        sim.getHygiene().increase(50);
        simcli.ui.UIManager.printMessage(
                "Hygiene is now " + sim.getHygiene().getValue() + " / " + simcli.needs.Need.MAX_VALUE);
    }
}
