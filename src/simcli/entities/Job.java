package simcli.entities;

public enum Job {
    UNEMPLOYED("Unemployed", 0, 0, 0, 0, 150, 1.0, 1),
    SOFTWARE_ENGINEER("Software Engineer", 150, 4, 30, 18, 70, 1.2, 5),
    HARDWARE_TECHNICIAN("Hardware Technician", 120, 5, 45, 18, 65, 1.1, 4),
    FREELANCE_PHOTOGRAPHER("Freelance Photographer", 80, 2, 15, 18, 99, 1.5, 3), // Elders can do this easily
    PERSONAL_TRAINER("Personal Trainer", 90, 3, 60, 18, 50, 1.3, 3);

    private final String title;
    private final int baseSalary;
    private final int workingHours;
    private final int energyDrain;
    private final int minAge;
    private final int maxAge;
    private final double promotionMultiplier;
    private final int maxTier;

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

    public String getTitle() {
        return title;
    }

    public int getBaseSalary() {
        return baseSalary;
    }

    public int getWorkingHours() {
        return workingHours;
    }

    public int getEnergyDrain() {
        return energyDrain;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public double getPromotionMultiplier() {
        return promotionMultiplier;
    }

    public int getMaxTier() {
        return maxTier;
    }

    public int getSalaryAtTier(int tier) {
    if (this == UNEMPLOYED) return 0;
    
    int safeTier = Math.max(1, Math.min(tier, this.maxTier));
    
    return (int) (baseSalary * Math.pow(promotionMultiplier, safeTier - 1));
    }
}