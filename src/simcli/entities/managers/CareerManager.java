package simcli.entities.managers;

import simcli.entities.models.Job;

import simcli.engine.SimulationLogger;

/**
 * Manages a Sim's employment state: current job, tier/promotion level,
 * shift tracking, truancy detection, and daily resets.
 *
 * <p>Extracted from {@link simcli.entities.actors.Sim} to follow the
 * Single Responsibility Principle — the Sim class delegates all
 * career-related logic to this manager.</p>
 */
public class CareerManager {

    /** The Sim's current career. */
    private Job career;

    /** The current promotion tier within the career (1-indexed). */
    private int jobTier;

    /** The number of consecutive days the Sim has missed work. */
    private int consecutiveDaysMissed;

    /** The number of shifts worked during the current day. */
    private int shiftsWorkedToday;

    /** Whether the overwork warning has been displayed this cycle. */
    private boolean hasWarnedAboutOverwork;

    /**
     * Constructs a new CareerManager with the Sim starting as
     * {@link Job#UNEMPLOYED} at tier 1.
     */
    public CareerManager() {
        this.career = Job.UNEMPLOYED;
        this.jobTier = 1;
        this.consecutiveDaysMissed = 0;
        this.shiftsWorkedToday = 0;
        this.hasWarnedAboutOverwork = false;
    }

    /**
     * Constructs a new CareerManager with a pre-assigned career.
     *
     * @param job the starting career.
     */
    public CareerManager(Job job) {
        this();
        this.career = job;
    }

    /**
     * Checks whether the Sim has missed too many consecutive days of work.
     *
     * <p>If the counter exceeds 3, the Sim is automatically fired and
     * reverted to {@link Job#UNEMPLOYED}. Otherwise a warning is logged.</p>
     *
     * @param simName the Sim's display name (used in log messages).
     */
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

    /**
     * Promotes the Sim to the next tier in their current career, if
     * they have not yet reached the maximum tier.
     *
     * @param simName the Sim's display name (used in log messages).
     */
    public void promote(String simName) {
        if (this.career == Job.UNEMPLOYED)
            return;
        if (this.jobTier < this.career.getMaxTier()) {
            this.jobTier++;
            SimulationLogger.getInstance().log("\n*** PROMOTION! " + simName + " has been promoted to tier "
                    + this.jobTier + " in " + this.career.getTitle() + "! ***");
        }
    }

    /**
     * Switches the Sim to a new career, resetting tier and attendance counters.
     *
     * @param newJob  the new career to assign.
     * @param simName the Sim's display name (used in log messages).
     */
    public void changeJob(Job newJob, String simName) {
        this.career = newJob;
        this.jobTier = 1;
        this.consecutiveDaysMissed = 0;
        this.shiftsWorkedToday = 0;
        SimulationLogger.getInstance().log(simName + " has started a new job as a " + newJob.getTitle() + ".");
    }

    /**
     * Resets daily shift and overwork tracking at the start of a new day.
     *
     * @param zeroShifts {@code true} to also reset the shifts-worked counter.
     */
    public void resetDaily(boolean zeroShifts) {
        if (zeroShifts) {
            this.shiftsWorkedToday = 0;
        }
        this.hasWarnedAboutOverwork = false;
    }

    /**
     * Returns the Sim's current career.
     *
     * @return the current {@link Job}.
     */
    public Job getCareer() {
        return career;
    }

    /**
     * Sets the Sim's career directly. Used for save/load restoration.
     *
     * @param career the career to assign.
     */
    public void setCareer(Job career) {
        this.career = career;
    }

    /**
     * Returns the current promotion tier.
     *
     * @return the job tier (1-indexed).
     */
    public int getJobTier() {
        return jobTier;
    }

    /**
     * Sets the promotion tier directly. Used for save/load restoration.
     *
     * @param tier the tier to set.
     */
    public void setJobTier(int tier) {
        this.jobTier = tier;
    }

    /**
     * Returns the number of consecutive days of work missed.
     *
     * @return the consecutive-days-missed counter.
     */
    public int getConsecutiveDaysMissed() {
        return consecutiveDaysMissed;
    }

    /**
     * Sets the consecutive-days-missed counter directly.
     * Used for save/load restoration.
     *
     * @param days the number of consecutive days missed.
     */
    public void setConsecutiveDaysMissed(int days) {
        this.consecutiveDaysMissed = days;
    }

    /**
     * Returns the number of shifts worked during the current day.
     *
     * @return shifts worked today.
     */
    public int getShiftsWorkedToday() {
        return shiftsWorkedToday;
    }

    /**
     * Increments the shifts-worked-today counter by one.
     */
    public void incrementShiftsWorkedToday() {
        this.shiftsWorkedToday++;
    }

    /**
     * Returns whether the overwork warning has been displayed this cycle.
     *
     * @return {@code true} if the warning was already shown.
     */
    public boolean hasWarnedAboutOverwork() {
        return hasWarnedAboutOverwork;
    }

    /**
     * Sets the overwork warning flag.
     *
     * @param warned {@code true} to mark the warning as shown.
     */
    public void setWarnedAboutOverwork(boolean warned) {
        this.hasWarnedAboutOverwork = warned;
    }
}
