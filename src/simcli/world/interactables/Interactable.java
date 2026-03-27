package simcli.world.interactables;
import simcli.engine.SimulationException;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;

import java.util.Scanner;

/**
 * Interface defining objects that Sims can use.
 */
public interface Interactable {
    void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException;
    String getObjectName();
}