package simcli.entities;

public enum Job {
    SOFTWARE_ENGINEER("Software Engineer", 150),
    HARDWARE_TECHNICIAN("Hardware Technician", 120),
    FREELANCE_PHOTOGRAPHER("Freelance Photographer", 80),
    PERSONAL_TRAINER("Personal Trainer", 90);

    private final String title;
    private final int salary;

    Job(String title, int salary) {
        this.title = title;
        this.salary = salary;
    }

    public String getTitle() { return title; }
    public int getSalary() { return salary; }
}