package simcli.engine;

/**
 * Specialised signal indicating that the Sim is going to sleep.
 *
 * <p>Extends {@link SimulationException} so it travels through the same
 * catch chain, but {@link InputHandler} checks for it first and maps it
 * to {@link CommandResult#SLEEP_EVENT}.</p>
 */
public class SleepEventException extends SimulationException {

    /**
     * Constructs a new {@code SleepEventException} with a fixed message.
     */
    public SleepEventException() {
        super("SIM_SLEEP_EVENT");
    }
}
