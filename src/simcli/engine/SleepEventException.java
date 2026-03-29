package simcli.engine;

// Specialised signal: "the Sim is going to sleep."
// Extends SimulationException so it travels through the same catch chain,
// but InputHandler checks for it first and maps it to SLEEP_EVENT.
public class SleepEventException extends SimulationException {

    /**
     * Constructs a new {@code SleepEventException} with a fixed message.
     */
    public SleepEventException() {
        super("SIM_SLEEP_EVENT");
    }
}
