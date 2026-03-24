package simcli.entities.actors;

/**
 * Encapsulates the overall physical constraints and emotional states of a Sim.
 */
public enum SimState {
    /** Sim is well, fed, rested, and capable of all actions. */
    HEALTHY(true, "Sim is feeling great."),
    /** Sim's hunger is low; they must seek food. */
    HUNGRY(true, "Sim needs food soon."),
    /** Sim lacks sufficient energy to perform strenuous tasks. */
    TIRED(false, "Sim is too exhausted to work."),
    /** Sim has expired. */
    DEAD(false, "Sim has passed away.");

    private final boolean canWork;
    private final String description;

    /**
     * Constructor for SimState enumeration.
     * @param canWork Decides if a Sim is capable of going to a job.
     * @param description Brief text defining the state.
     */
    private SimState(boolean canWork, String description) {
        this.canWork = canWork;
        this.description = description;
    }

    /**
     * Determines whether work operations should be blocked.
     * @return boolean reflecting ability to work.
     */
    public boolean canWork() {
        return canWork;
    }

    /**
     * Exposes the human-readable description.
     * @return Status description text.
     */
    public String getDescription() {
        return description;
    }
}