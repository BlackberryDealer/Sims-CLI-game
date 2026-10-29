package simcli.entities.models;

/**
 * The household's first steps: a short checklist for the first week that
 * teaches the main systems and pays a little cash for each one done.
 *
 * <p>Checked every turn by {@link simcli.engine.GoalManager}.</p>
 */
public enum StarterGoal {

    /** Someone takes a job. */
    GET_JOB("Get a job", 100),

    /** Someone works a shift. */
    FIRST_SHIFT("Work your first shift", 50),

    /** Someone cooks a meal at the fridge. */
    COOK_MEAL("Cook a meal at home", 50),

    /** Someone gets to know a person (relationship 20+). */
    MEET_SOMEONE("Get to know someone (relationship 20+)", 50),

    /** The household pays its first weekly bills in full. */
    PAY_BILLS("Pay your first weekly bills", 100);

    /** What to do. */
    private final String description;

    /** The cash reward. */
    private final int reward;

    StarterGoal(String description, int reward) {
        this.description = description;
        this.reward = reward;
    }

    /** @return what the household has to do. */
    public String getDescription() {
        return description;
    }

    /** @return the cash paid when it's done. */
    public int getReward() {
        return reward;
    }
}
