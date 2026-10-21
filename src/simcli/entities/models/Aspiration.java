package simcli.entities.models;

/**
 * A Sim's lifelong goal. Each Sim has one; fulfilling it earns a reward,
 * and a household whose Sims have all fulfilled theirs completes its legacy.
 *
 * <p>Progress is measured by {@link simcli.engine.GoalManager}.</p>
 */
public enum Aspiration {

    /** Reach the top tier of a career. */
    CAREER_STAR("Career Star", "Reach the top tier of a career"),

    /** Have three children. */
    BIG_FAMILY("Big Family", "Raise a family of 3 children"),

    /** Buy a home of their own. */
    HOMEOWNER("Homeowner", "Buy the Bungalow"),

    /** Max out a skill. */
    SKILL_MASTER("Master of One", "Reach level 10 in any skill"),

    /** Make lots of friends. */
    SOCIAL_BUTTERFLY("Social Butterfly", "Make 5 good friends (relationship 50+)");

    /** The aspiration's name. */
    private final String displayName;

    /** What the Sim has to do. */
    private final String goal;

    Aspiration(String displayName, String goal) {
        this.displayName = displayName;
        this.goal = goal;
    }

    /**
     * Returns the aspiration's name, e.g. "Career Star".
     *
     * @return the display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns a description of what fulfils the aspiration.
     *
     * @return the goal text.
     */
    public String getGoal() {
        return goal;
    }
}
