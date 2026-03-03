package simcli.entities;

public enum Job {
    SOFTWARE_ENGINEER("Software Engineer", 150, 4, 30, 18, 65),
    HARDWARE_TECHNICIAN("Hardware Technician", 120, 5, 45, 18, 55),
    FREELANCE_PHOTOGRAPHER("Freelance Photographer", 80, 2, 15, 18, 80),
    PERSONAL_TRAINER("Personal Trainer", 90, 3, 60, 18, 40);

    private final String title;
    private final int salary;
    private final int workingHours;
    private final int energyDrain;
    private final int minAge;
    private final int maxAge;

    Job(String title, int salary, int workingHours, int energyDrain, int minAge, int maxAge) {
        this.title = title;
        this.salary = salary;
        this.workingHours = workingHours;
        this.energyDrain = energyDrain;
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    public String getTitle() { return title; }
    public int getSalary() { return salary; }
    public int getWorkingHours() { return workingHours; }
    public int getEnergyDrain() { return energyDrain; }
    public int getMinAge() { return minAge; }
    public int getMaxAge() { return maxAge; }
}