package simcli.world.interactables;
import simcli.engine.SimulationException;
import simcli.entities.Sim;

/**
 * Interface defining objects that Sims can use.
 */
public interface Interactable {
    void interact(Sim sim, java.util.Scanner scanner) throws SimulationException;
    String getObjectName();
}