package simcli.entities.models;

/**
 * A feeling a Sim carries for a while after something happens to them —
 * "Well rested" after a good night's sleep, "Grieving" after a death.
 *
 * <p>Each moodlet adds to (or takes from) the Sim's mood for a number of
 * hours, then wears off. Getting the same moodlet again restarts its
 * timer rather than stacking. Most are handed out by
 * {@link simcli.engine.InnerLifeManager}, which notices what changed in a
 * Sim's life after each turn.</p>
 */
public enum Moodlet {

    // --- good feelings ---
    /** Woke up with plenty of energy. */
    WELL_RESTED("Well rested", 10, 8),
    /** Cooked a proper meal. */
    HOME_COOKED("Home cooking", 10, 6),
    /** Had a good wash. */
    SQUEAKY_CLEAN("Squeaky clean", 5, 4),
    /** Did something fun. */
    HAD_A_BLAST("Had a blast", 10, 4),
    /** Spent time with people. */
    GOOD_COMPANY("Good company", 10, 4),
    /** Gained a skill level. */
    LEARNED_SOMETHING("Learned something", 5, 6),
    /** Made a new friend. */
    NEW_FRIEND("New friend", 10, 12),
    /** Was promoted. */
    PROMOTED("Promoted!", 20, 24),
    /** Had a birthday. */
    BIRTHDAY("Birthday", 10, 12),
    /** Got married. */
    NEWLYWED("Newlywed", 25, 48),
    /** Had a baby. */
    NEW_PARENT("New baby", 20, 48),
    /** Did something they wanted to do today. */
    WISH_CAME_TRUE("Wish come true", 10, 8),
    /** Fulfilled their aspiration. */
    LIVING_THE_DREAM("Living the dream", 25, 48),
    /** Bought from the reward store. */
    INSPIRED("Inspired", 20, 8),

    // --- bad feelings ---
    /** A social action fell flat. */
    REJECTED("Rejected", -10, 4),
    /** Missed a day of work. */
    GUILTY("Skipped work", -10, 12),
    /** Lost their job. */
    FIRED("Fired", -25, 24),
    /** Their fear for the day came true. */
    WORST_FEAR("Worst fear", -15, 12),
    /** Someone close to them died. */
    GRIEVING("Grieving", -25, 72);

    private final String label;
    private final int mood;
    private final int hours;

    Moodlet(String label, int mood, int hours) {
        this.label = label;
        this.mood = mood;
        this.hours = hours;
    }

    /** @return the name shown to the player, e.g. "Well rested". */
    public String getLabel() {
        return label;
    }

    /** @return how much this moodlet adds to mood (negative for bad feelings). */
    public int getMood() {
        return mood;
    }

    /** @return how many hours it lasts. */
    public int getHours() {
        return hours;
    }

    /** @return {@code true} for bad feelings. */
    public boolean isNegative() {
        return mood < 0;
    }
}
