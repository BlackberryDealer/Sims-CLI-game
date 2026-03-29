package simcli.entities.models;

/**
 * Enumerates the possible activities a Sim can be performing at any moment.
 *
 * <p>Used by the needs system to vary decay rates (e.g. hunger decays faster
 * when working, energy restores when sleeping) and by the UI to select
 * the appropriate ASCII art animation.</p>
 */
public enum ActionState {

    /** The Sim is not performing any specific activity. */
    IDLE,

    /** The Sim is currently consuming food. */
    EATING,

    /** The Sim is asleep and restoring energy. */
    SLEEPING,

    /** The Sim is performing a work shift. */
    WORKING,

    /** The Sim is studying or reading. */
    STUDYING,

    /** The Sim is playing or having fun. */
    PLAYING,

    /** The Sim is socializing with another Sim. */
    SOCIALIZING
}
