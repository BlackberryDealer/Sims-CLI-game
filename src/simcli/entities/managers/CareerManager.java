package simcli.entities.managers;

import simcli.entities.models.Job;

import simcli.engine.SimulationLogger;

/**
 * Manages a Sim's employment state: current job, tier/promotion level,
 * shift tracking, truancy detection, and daily resets. Extracted from
 * {@link simcli.entities.actors.Sim} to follow the Single Responsibility
 * Principle.
 */
public class CareerManager {
    private Job career;
    private int jobTier;
    private int consecutiveDaysMissed;
    private int shiftsWorkedToday;
    private boolean hasWarnedAboutOverwork;

    public CareerManager() {
        this.career = Job.UNEMPLOYED;
        this.jobTier = 1;
        this.consecutiveDaysMissed = 0;
        this.shiftsWorkedToday = 0;
        this.hasWarnedAboutOverwork = false;
    }

    public CareerManager(Job job) {
        this();
        this.career = job;
    }

    public void checkTruancy(String simName) {
        if (this.career == Job.UNEMPLOYED)
            return;
        this.consecutiveDaysMissed++;
        if (this.consecutiveDaysMissed > 3) {
            SimulationLogger.getInstance()
                    .logWarning("Oh no! " + simName + " missed too many days of work and was fired from "
                            + this.career.getTitle() + ".");
            this.career = Job.UNEMPLOYED;
            this.jobTier = 1;
            this.consecutiveDaysMissed = 0;
            this.shiftsWorkedToday = 0;
        } else if (this.consecutiveDaysMissed > 0) {
            SimulationLogger.getInstance()
                    .logWarning(simName + " missed work! Consecutive days missed: " + this.consecutiveDaysMissed);
        }
    }

    public void promote(String simName) {
        if (this.career == Job.UNEMPLOYED)
            return;
        if (this.jobTier < this.career.getMaxTier()) {
            this.jobTier++;
            SimulationLogger.getInstance().log("\n*** PROMOTION! " + simName + " has been promoted to tier "
                    + this.jobTier + " in " + this.career.getTitle() + "! ***");
        }
    }

    public void changeJob(Job newJob, String simName) {
        this.career = newJob;
        this.jobTier = 1;
        this.consecutiveDaysMissed = 0;
        this.shiftsWorkedToday = 0;
        SimulationLogger.getInstance().log(simName + " has started a new job as a " + newJob.getTitle() + ".");
    }

    public void resetDaily(boolean zeroShifts) {
        if (zeroShifts) {
            this.shiftsWorkedToday = 0;
        }
        this.hasWarnedAboutOverwork = false;
    }

    public Job getCareer() {
        return career;
    }

    public void setCareer(Job career) {
        this.career = career;
    }

    public int getJobTier() {
        return jobTier;
    }

    public void setJobTier(int tier) {
        this.jobTier = tier;
    }

    public int getConsecutiveDaysMissed() {
        return consecutiveDaysMissed;
    }

    public void setConsecutiveDaysMissed(int days) {
        this.consecutiveDaysMissed = days;
    }

    public int getShiftsWorkedToday() {
        return shiftsWorkedToday;
    }

    public void incrementShiftsWorkedToday() {
        this.shiftsWorkedToday++;
    }

    public boolean hasWarnedAboutOverwork() {
        return hasWarnedAboutOverwork;
    }

    public void setWarnedAboutOverwork(boolean warned) {
        this.hasWarnedAboutOverwork = warned;
    }
}
