package simcli.engine;

/**
 * Control-flow signal between commands and the engine.
 *
 * <p>Commands return one of these values to say "what should happen next"
 * without ever calling {@code GameEngine} methods directly. This enum
 * decouples the command layer from the engine's tick-advancement logic.</p>
 */
public enum CommandResult {

    /** A normal action was performed — the game clock should advance. */
    TICK_FORWARD,

    /** An action was taken but no time should pass (e.g. menu cancel). */
    NO_TICK,

    /**
     * The Sim went to sleep — GameEngine should skip time forward
     * to the next morning rather than advancing by one tick.
     */
    SLEEP_EVENT,

    /** The player chose to save and exit to the main menu. */
    SAVE_AND_EXIT
}
