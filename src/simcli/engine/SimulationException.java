package simcli.engine;
// Thrown when game rules block an action (e.g. too young to work, no money).
// InputHandler catches this and shows a warning — the game never crashes.
// Using a checked exception forces commands to declare what can go wrong.
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