package simcli.engine;
/**
 * Custom checked exception to handle invalid simulation actions gracefully.
 *
 * <p>Thrown by commands when game rules prevent execution (e.g. interacting
 * with an unavailable object, performing an action while in the wrong state).
 * {@link InputHandler} catches this and displays the message as a warning
 * to the player without crashing the game.</p>
 */
public class SimulationException extends Exception {

    /**
     * Constructs a new {@code SimulationException} with the given detail message.
     *
     * @param message human-readable explanation of why the action was rejected.
     */
    public SimulationException(String message) {
        super(message);
    }
}