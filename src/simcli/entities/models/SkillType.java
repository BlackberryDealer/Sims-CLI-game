package simcli.entities.models;

/**
 * The skills a Sim can train.
 *
 * <p>Each skill is raised by a matching activity and pays off elsewhere:
 * <ul>
 *     <li>{@link #FITNESS} — working out on the weight bench; lowers the
 *         energy cost of workouts and boosts a Personal Trainer's career.</li>
 *     <li>{@link #LOGIC} — studying at the computer; boosts the engineering
 *         and technician careers.</li>
 *     <li>{@link #CHARISMA} — socializing; makes social actions land more
 *         often and build relationships faster, and boosts the
 *         photographer career.</li>
 * </ul>
 */
public enum SkillType {

    /** Physical fitness, trained on the weight bench. */
    FITNESS("Fitness", "work out on the weight bench"),

    /** Problem solving, trained by studying at a computer. */
    LOGIC("Logic", "study at a computer"),

    /** Social ease, trained by talking to people. */
    CHARISMA("Charisma", "chat, joke and flirt with people");

    /** The human-readable name of this skill. */
    private final String displayName;

    /** A short tip on how to train this skill. */
    private final String trainingHint;

    SkillType(String displayName, String trainingHint) {
        this.displayName = displayName;
        this.trainingHint = trainingHint;
    }

    /**
     * Returns a short tip on how to train this skill, e.g.
     * "work out on the weight bench".
     *
     * @return the training hint.
     */
    public String getTrainingHint() {
        return trainingHint;
    }

    /**
     * Returns the human-readable name of this skill.
     *
     * @return the display name (e.g. "Fitness").
     */
    public String getDisplayName() {
        return displayName;
    }
}
