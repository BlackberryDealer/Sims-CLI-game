package simcli.engine;
/**
 * Custom checked exception to handle invalid simulation actions gracefully.
 */
public class SimulationException extends Exception {
    public SimulationException(String message) {
        super(message);
    }
}