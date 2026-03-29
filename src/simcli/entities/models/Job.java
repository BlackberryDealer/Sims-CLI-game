package simcli.entities.models;

import simcli.utils.GameConstants;

/**
 * Enumeration of all available careers in the simulation, each with salary,
 * working hours, energy drain, age limits, promotion multiplier, and max tier.
 *
 * <p>{@link #UNEMPLOYED} serves as the default/fallback state for Sims
 * without a job. Each career defines a {@link #getSalaryAtTier(int)} method
 * that computes earnings based on the promotion multiplier.</p>
 */
public enum Job {

    /** Default state — no employment, no salary. */
    UNEMPLOYED("Unemployed", 0, 0, 0, 0, 100, 1.0, 1),

    /** High-paying desk job with moderate energy drain. */
    SOFTWARE_ENGINEER("Software Engineer", 150, 4, 30, GameConstants.ADULT_AGE, GameConstants.ELDER_AGE, 1.2, 5),

    /** Hands-on technical work with higher energy drain. */
    HARDWARE_TECHNICIAN("Hardware Technician", 120, 5, 45, GameConstants.ADULT_AGE, 55, 1.1, 4),

    /** Flexible creative career with low energy requirements. */
    FREELANCE_PHOTOGRAPHER("Freelance Photographer", 80, 2, 15, GameConstants.ADULT_AGE, 80, GameConstants.BONUS_TIMES,
            3),

    /** Physical career with high energy drain but good pay. */
    PERSONAL_TRAINER("Personal Trainer", 90, 3, 60, GameConstants.ADULT_AGE, 40, 1.3, 3);

    /** The human-readable job title. */
    private final String title;

    /** Base salary earned per shift (before tier multiplier). */
    private final int baseSalary;

    /** Number of working hours per shift. */
    private final int workingHours;

    /** Energy points drained per shift. */
    private final int energyDrain;

    /** Minimum age required to hold this job. */
    private final int minAge;

    /** Maximum age before forced retirement from this job. */
    private final int maxAge;

    /** Salary multiplier applied per promotion tier. */
    private final double promotionMultiplier;

    /** The highest promotion tier achievable in this career. */
    private final int maxTier;

    /**
     * Constructs a new Job enum constant.
     *
     * @param title               the human-readable job title.
     * @param baseSalary          base salary per shift.
     * @param workingHours        hours required per shift.
     * @param energyDrain         energy cost per shift.
     * @param minAge              minimum eligible age.
     * @param maxAge              maximum eligible age.
     * @param promotionMultiplier salary multiplier per tier.
     * @param maxTier             highest achievable promotion tier.
     */
    Job(String title, int baseSalary, int workingHours, int energyDrain, int minAge, int maxAge,
            double promotionMultiplier, int maxTier) {
        this.title = title;
        this.baseSalary = baseSalary;
        this.workingHours = workingHours;
        this.energyDrain = energyDrain;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.promotionMultiplier = promotionMultiplier;
        this.maxTier = maxTier;
    }

    /**
     * Returns the human-readable job title.
     *
     * @return the job title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the base salary earned per shift (before tier multiplier).
     *
     * @return the base salary in Simoleons.
     */
    public int getBaseSalary() {
        return baseSalary;
    }

    /**
     * Returns the number of working hours per shift.
     *
     * @return working hours.
     */
    public int getWorkingHours() {
        return workingHours;
    }

    /**
     * Returns the energy points drained per shift.
     *
     * @return the energy drain amount.
     */
    public int getEnergyDrain() {
        return energyDrain;
    }

    /**
     * Returns the minimum age required to hold this job.
     *
     * @return the minimum eligible age.
     */
    public int getMinAge() {
        return minAge;
    }

    /**
     * Returns the maximum age before forced retirement.
     *
     * @return the maximum eligible age.
     */
    public int getMaxAge() {
        return maxAge;
    }

    /**
     * Returns the salary multiplier applied per promotion tier.
     *
     * @return the promotion multiplier.
     */
    public double getPromotionMultiplier() {
        return promotionMultiplier;
    }

    /**
     * Returns the highest achievable promotion tier for this career.
     *
     * @return the max tier.
     */
    public int getMaxTier() {
        return maxTier;
    }

    /**
     * Computes the salary at a given promotion tier using the formula:
     * {@code baseSalary * promotionMultiplier^(tier - 1)}.
     *
     * <p>The tier is clamped to {@code [1, maxTier]}. Returns 0 for
     * {@link #UNEMPLOYED}.</p>
     *
     * @param tier the promotion tier (1-indexed).
     * @return the computed salary in Simoleons.
     */
    public int getSalaryAtTier(int tier) {
        if (this == UNEMPLOYED)
            return 0;

        int safeTier = Math.max(1, Math.min(tier, this.maxTier));

        return (int) (baseSalary * Math.pow(promotionMultiplier, safeTier - 1));
    }
}
