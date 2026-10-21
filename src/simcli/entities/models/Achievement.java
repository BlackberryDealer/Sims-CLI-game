package simcli.entities.models;

/**
 * Household milestones, unlocked once and kept for the rest of the game.
 *
 * <p>Checked every turn by {@link simcli.engine.GoalManager}.</p>
 */
public enum Achievement {

    /** Someone in the household has a job. */
    FIRST_JOB("Gainfully Employed", "Get a job"),

    /** Someone has been promoted. */
    PROMOTED("Moving Up", "Earn a promotion"),

    /** Someone has reached the top of their career. */
    TOP_TIER("Corner Office", "Reach the top tier of a career"),

    /** Someone has a good friend. */
    GOOD_FRIENDS("Good Friends", "Reach 50 relationship with someone"),

    /** Someone is married. */
    MARRIED("Wedding Bells", "Get married"),

    /** A baby has been born. */
    NEW_BABY("Bundle of Joy", "Have a baby"),

    /** A child born in the game has grown into a teen. */
    GROWN_UP("All Grown Up", "Raise a child to their teens"),

    /** The household owns a home it bought. */
    HOMEOWNER("Moving On Up", "Buy the Bungalow"),

    /** Someone has a skill at level 5. */
    SKILLED("Skilled", "Reach level 5 in a skill"),

    /** Someone has a skill at level 10. */
    MASTER("Master", "Reach level 10 in a skill"),

    /** The household has saved a fortune. */
    FORTUNE("Moneybags", "Have $10,000 across the household"),

    /** Someone has reached old age. */
    GOLDEN_YEARS("Golden Years", "Have a Sim reach elder age");

    /** The achievement's name. */
    private final String displayName;

    /** What unlocks it. */
    private final String description;

    Achievement(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * Returns the achievement's name, e.g. "Wedding Bells".
     *
     * @return the display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns what unlocks this achievement.
     *
     * @return the description.
     */
    public String getDescription() {
        return description;
    }
}
