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
}
