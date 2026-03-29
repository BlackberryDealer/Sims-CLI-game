package simcli.engine;

/**
 * Tracks the simulation clock — ticks, days, and time-of-day.
 *
 * <p>Time progresses in discrete <b>ticks</b>. A configurable number of
 * ticks ({@code ticksPerDay}) constitutes one in-game day. The manager can
 * compute the current day number, time-of-day period (Morning / Afternoon /
 * Evening / Night), formatted hour string, and day-of-week name from the
 * raw tick counter.</p>
 *
 * <p>Ticks advance via {@link #advanceTick()} (single) or
 * {@link #advanceTicks(int)} (bulk). Bulk advancement is used when the Sim
 * sleeps — the clock jumps forward to the next morning.</p>
 */
public class TimeManager {
    private int currentTick;
    private int ticksPerDay;

    /**
     * Creates a {@code TimeManager} starting at tick&nbsp;1.
     *
     * @param ticksPerDay number of ticks that constitute one in-game day
     *                    (e.g. 24 for one tick per simulated hour).
     */
    public TimeManager(int ticksPerDay) {
        this.currentTick = 1;
        this.ticksPerDay = ticksPerDay;
    }

    /**
     * Creates a {@code TimeManager} restoring a previously saved tick.
     *
     * @param currentTick the tick counter to restore.
     * @param ticksPerDay number of ticks that constitute one in-game day.
     */
    public TimeManager(int currentTick, int ticksPerDay) {
        this.currentTick = currentTick;
        this.ticksPerDay = ticksPerDay;
    }

    /** Advances the simulation clock by a single tick. */
    public void advanceTick() {
        this.currentTick++;
    }

    /**
     * Advances the simulation clock by the given number of ticks.
     *
     * @param ticks the number of ticks to advance; must be &ge; 0.
     */
    public void advanceTicks(int ticks) {
        this.currentTick += ticks;
    }

    /** Returns the raw tick counter. */
    public int getCurrentTick() {
        return currentTick;
    }

    /** Returns the current in-game day number (1-indexed). */
    public int getCurrentDay() {
        return (currentTick / ticksPerDay) + 1;
    }

    /**
     * Returns the current time-of-day period based on the tick position
     * within the current day.
     *
     * @return one of {@code "Morning"}, {@code "Afternoon"},
     *         {@code "Evening"}, or {@code "Night"}.
     */
    public String getTimeOfDay() {
        int timeInDay = currentTick % ticksPerDay;
        double ratio = (double) timeInDay / ticksPerDay;

        if (ratio < 0.25)
            return "Morning";
        else if (ratio < 0.5)
            return "Afternoon";
        else if (ratio < 0.75)
            return "Evening";
        else
            return "Night";
    }

    /**
     * Returns a formatted hour string (e.g. {@code "08:00"}) derived from
     * the tick position within the current day.
     *
     * @return hour in {@code HH:00} format.
     */
    public String getFormattedTime() {
        int timeInDay = currentTick % ticksPerDay;
        return String.format("%02d:00", timeInDay);
    }

    /**
     * Returns the day-of-week name derived from the current day number.
     * Cycles through Monday–Sunday.
     *
     * @return the day-of-week name.
     */
    public String getDayOfWeek() {
        int day = getCurrentDay();
        int dayOfWeek = (day - 1) % 7; 
        switch (dayOfWeek) {
            case 0: return "Monday";
            case 1: return "Tuesday";
            case 2: return "Wednesday";
            case 3: return "Thursday";
            case 4: return "Friday";
            case 5: return "Saturday";
            case 6: return "Sunday";
            default: return "Monday";
        }
    }
}
