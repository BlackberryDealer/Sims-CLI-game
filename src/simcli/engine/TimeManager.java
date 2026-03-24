package simcli.engine;

public class TimeManager {
    private int currentTick;
    private int ticksPerDay;

    public TimeManager(int ticksPerDay) {
        this.currentTick = 1;
        this.ticksPerDay = ticksPerDay;
    }

    public TimeManager(int currentTick, int ticksPerDay) {
        this.currentTick = currentTick;
        this.ticksPerDay = ticksPerDay;
    }

    public void advanceTick() {
        this.currentTick++;
    }

    public void advanceTicks(int ticks) {
        this.currentTick += ticks;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public int getCurrentDay() {
        return (currentTick / ticksPerDay) + 1;
    }

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

    public String getFormattedTime() {
        int timeInDay = currentTick % ticksPerDay;
        return String.format("%02d:00", timeInDay);
    }

    public String getDayOfWeek() {
        int day = getCurrentDay();
        // Day 1 = Monday, Day 2 = Tuesday... Day 7 = Sunday
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

    /**
     * Determines if a year has passed, checking all sims.
     * @param sims Map of simulations.
     */
    public void processYearlyAging(java.util.List<simcli.entities.actors.Sim> sims) {
        final int TICKS_PER_YEAR = this.ticksPerDay * 3;
        
        if (this.currentTick % TICKS_PER_YEAR == 0) {
            for (simcli.entities.actors.Sim sim : sims) {
                sim.ageUp();
            }
        }
    }
}
