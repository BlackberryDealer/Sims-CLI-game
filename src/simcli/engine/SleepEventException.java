package simcli.engine;

/**
 * Signals that the active Sim has gone to sleep.
 *
 * <p>Thrown by the {@link simcli.engine.commands.ICommand#execute()} method
 * (typically via {@link simcli.world.interactables.Interactable#interact})
 * when the player interacts with a bed. {@link InputHandler} catches this
 * and returns {@link CommandResult#SLEEP_EVENT}, which causes
 * {@link GameEngine} to fast-forward the clock to the next morning.</p>
 */
public class SleepEventException extends SimulationException {

    /**
     * Constructs a new {@code SleepEventException} with a fixed message.
     */
    public SleepEventException() {
        super("SIM_SLEEP_EVENT");
    }
}
