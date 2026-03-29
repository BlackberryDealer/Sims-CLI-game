package simcli.entities.models;

/**
 * Represents the ActionState entity or state in the simulation.
 */
public enum ActionState {
    IDLE,  // idle means not any other activity
    EATING,
    SLEEPING,
    WORKING,
    STUDYING,
    PLAYING,
    SOCIALIZING
}
