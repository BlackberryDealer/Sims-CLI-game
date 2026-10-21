package simcli.engine;

/**
 * Signals that the player backed out of an interaction without doing
 * anything (e.g. left a shop without buying), so no time should pass.
 *
 * <p>{@link InputHandler} maps it to {@link CommandResult#NO_TICK} without
 * showing an error.</p>
 */
public class ActionCancelledException extends SimulationException {

    /** Constructs a new {@code ActionCancelledException}. */
    public ActionCancelledException() {
        super("Action cancelled");
    }
}
