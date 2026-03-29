package simcli.world.interactables;

import simcli.engine.SimulationException;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;
import simcli.needs.Need;
import simcli.ui.UIManager;

import java.util.Scanner;

/**
 * Represents a Shower location or interactable object.
 */
public class Shower implements Interactable {

    @Override
    public String getObjectName() {
        return "Shower";
    }

    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException {
        sim.setCurrentAction(ActionState.PLAYING); // closest match for a refreshing activity
        UIManager.printMessage(sim.getName() + " takes a long, refreshing shower.");
        sim.getHygiene().increase(50);
        UIManager.printMessage(
                "Hygiene is now " + sim.getHygiene().getValue() + " / " + Need.MAX_VALUE);
    }
}
