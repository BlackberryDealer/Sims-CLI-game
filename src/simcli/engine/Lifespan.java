package simcli.engine;

/**
 * How fast Sims grow old in a world, chosen when the world is created.
 *
 * <p>Teens, adults and elders age one year every {@link #getDaysPerYear()}
 * days; children always age a year a day. Townsfolk at the park age at the
 * normal rate whatever the setting.</p>
 */
public enum Lifespan {

    /** Lives fly by: a year every 2 days. */
    SHORT("Short", 2),

    /** The standard pace: a year every 3 days. */
    NORMAL("Normal", 3),

    /** Long, leisurely lives: a year every 5 days. */
    LONG("Long", 5);

    private final String displayName;
    private final int daysPerYear;

    Lifespan(String displayName, int daysPerYear) {
        this.displayName = displayName;
        this.daysPerYear = daysPerYear;
    }

    /** @return the setting's name, e.g. "Normal". */
    public String getDisplayName() {
        return displayName;
    }

    /** @return in-game days per year of age for teens, adults and elders. */
    public int getDaysPerYear() {
        return daysPerYear;
    }
}
