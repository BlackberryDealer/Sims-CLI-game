package simcli.entities.models;

/**
 * Something a Sim can buy with the satisfaction points they earn from
 * their {@link Want}s.
 *
 * <p>Perks last for life and can only be bought once; boosts are used up
 * straight away and can be bought again.</p>
 */
public enum Reward {

    /** Skills learn faster. */
    QUICK_LEARNER("Quick Learner", "Skills learn 25% faster", 2000, true),

    /** Work performance builds faster. */
    GO_GETTER("Go-Getter", "Work performance fills 25% faster", 2500, true),

    /** Jokes and flirts land more often. */
    SILVER_TONGUE("Silver Tongue", "Jokes and flirts land 10% more often", 1500, true),

    /** Bad moodlets hurt less. */
    SUNNY_DISPOSITION("Sunny Disposition", "Bad moodlets only count for half", 1500, true),

    /** An extra want each day. */
    BIG_DREAMER("Big Dreamer", "One more want every day", 3000, true),

    /** A short burst of inspiration. */
    INSPIRATION("Inspiration", "Feel Inspired (+20 mood) for 8 hours", 250, false),

    /** A burst of energy. */
    SECOND_WIND("Second Wind", "Restore 40 energy right now", 250, false);

    private final String displayName;
    private final String description;
    private final int cost;
    private final boolean perk;

    Reward(String displayName, String description, int cost, boolean perk) {
        this.displayName = displayName;
        this.description = description;
        this.cost = cost;
        this.perk = perk;
    }

    /** @return the reward's name. */
    public String getDisplayName() {
        return displayName;
    }

    /** @return what it does. */
    public String getDescription() {
        return description;
    }

    /** @return its price in satisfaction points. */
    public int getCost() {
        return cost;
    }

    /** @return {@code true} for a lifelong perk, {@code false} for a one-off boost. */
    public boolean isPerk() {
        return perk;
    }
}
