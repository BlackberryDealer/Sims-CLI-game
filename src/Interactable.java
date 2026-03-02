/**
 * Interface defining objects that Sims can use.
 */
public interface Interactable {
    void interact(Sim sim) throws SimulationException;
    String getObjectName();
}