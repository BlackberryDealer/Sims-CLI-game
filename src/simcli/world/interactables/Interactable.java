package simcli.world.interactables;
import simcli.engine.SimulationException;
import simcli.engine.TimeManager;
import simcli.entities.Sim;

/**
 * Interface defining objects that Sims can use.
 */
public interface Interactable {
    void interact(Sim sim, java.util.Scanner scanner, TimeManager timeManager) throws SimulationException;
    String getObjectName();
}