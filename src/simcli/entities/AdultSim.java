package simcli.entities;

import simcli.engine.SimulationException;

/**
 * Concrete class representing an employable, player-controlled Sim.
 */
public class AdultSim extends Sim {
    private Job career;
    private int jobTier;
    private int consecutiveDaysMissed;

    // Constructor Overloading
    public AdultSim(String name, int age, Job career) {
        super(name, age);
        this.career = career;
        this.jobTier = 1;
        this.consecutiveDaysMissed = 0;
    }

    public AdultSim(String name) {
        this(name, 21, Job.SOFTWARE_ENGINEER);
    }

    public Job getCareer() {
        return this.career;
    }

    @Override
    public void performActivity(String activityType) throws SimulationException {
        if (this.state == SimState.DEAD || this.state == SimState.CRITICAL) {
            throw new SimulationException(this.name + " is in critical condition and refuses to act.");
        }

        if (activityType.equalsIgnoreCase("Work")) {
            if (this.career == Job.UNEMPLOYED) {
                System.out.println(this.name + " is unemployed and cannot work!");
                return;
            }
            int dailyPay = this.career.getSalaryAtTier(this.jobTier);
            System.out.println(
                    this.name + " works a shift as a " + this.career.getTitle() + " and earns $" + dailyPay + "!");
            this.energy.decrease(this.career.getEnergyDrain());
            this.hunger.decrease(20);
            this.hygiene.decrease(30);
            this.setMoney(this.getMoney() + dailyPay);
            this.addTotalMoneyEarned(dailyPay);
            this.consecutiveDaysMissed = 0;
        } else {
            System.out.println(this.name + " is idling.");
        }
    }

    public void checkTruancy() {
        if (this.career == Job.UNEMPLOYED)
            return;
        this.consecutiveDaysMissed++;
        if (this.consecutiveDaysMissed > 3) {
            System.out.println("\n[WARNING] Oh no! " + this.name + " missed too many days of work and was fired from "
                    + this.career.getTitle() + ".");
            this.career = Job.UNEMPLOYED;
            this.jobTier = 1;
            this.consecutiveDaysMissed = 0;
        } else if (this.consecutiveDaysMissed > 0) {
            System.out.println("\n[WARNING] " + this.name + " missed work! Consecutive days missed: "
                    + this.consecutiveDaysMissed);
        }
    }

    public void promote() {
        if (this.career == Job.UNEMPLOYED)
            return;
        if (this.jobTier < this.career.getMaxTier()) {
            this.jobTier++;
            System.out.println("\n*** PROMOTION! " + this.name + " has been promoted to tier " + this.jobTier + " in "
                    + this.career.getTitle() + "! ***");
        }
    }

    public void changeJob(Job newJob) {
        this.career = newJob;
        this.jobTier = 1;
        this.consecutiveDaysMissed = 0;
        System.out.println(this.name + " has started a new job as a " + newJob.getTitle() + ".");
    }
}