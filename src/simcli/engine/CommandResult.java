package simcli.engine;

/**
 * Represents the outcome of a player command, allowing InputHandler
 * to communicate control-flow intent back to GameEngine without
 * needing a direct reference to it.
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
