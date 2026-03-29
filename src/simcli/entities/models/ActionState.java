package simcli.entities.models;

/**
 * Enumerates the possible activities a Sim can be performing at any moment.
 * Used by the needs system to vary decay rates (e.g. hunger decays faster
 * when working, energy restores when sleeping).
 */
public enum ActionState {
    IDLE, // idle means not any other activity
    EATING,
    SLEEPING,
    WORKING,
    STUDYING,
    PLAYING,
    SOCIALIZING
}
