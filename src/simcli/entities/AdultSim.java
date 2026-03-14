package simcli.entities;

import simcli.engine.SimulationException;
import simcli.entities.lifecycle.AdultStage; // State Pattern: AdultSim starts in AdultStage

/**
 * Concrete class representing an employable, player-controlled Sim.
 */
public class AdultSim extends Sim {
    private Job career;
    private int jobTier;
    private int consecutiveDaysMissed;
    private int shiftsWorkedToday;
    private boolean hasWarnedAboutOverwork;

    // Constructor Overloading
    public AdultSim(String name, int age, Job career) {
        super(name, age);
        this.career = career;
        this.jobTier = 1;
        this.consecutiveDaysMissed = 0;
        this.shiftsWorkedToday = 0;
        this.hasWarnedAboutOverwork = false;
        // State Pattern: assign the AdultStage as this Sim's initial lifecycle "brain".
        // This means an AdultSim created from the start correctly reports canWork() ==
        // true.
        setLifeStage(new AdultStage());
    }

    public AdultSim(String name) {
        this(name, 21, Job.SOFTWARE_ENGINEER);
    }

    public Job getCareer() {
        return this.career;
    }

    public int getShiftsWorkedToday() {
        return shiftsWorkedToday;
    }

    public boolean hasWarnedAboutOverwork() {
        return hasWarnedAboutOverwork;
    }

    public void setWarnedAboutOverwork(boolean warned) {
        this.hasWarnedAboutOverwork = warned;
    }

    @Override
    public void performActivity(String activityType) throws SimulationException {
        if (this.state == SimState.DEAD || this.state == SimState.CRITICAL) {
            throw new SimulationException(this.name + " is in critical condition and refuses to act.");
        }

        if (activityType.equalsIgnoreCase("Work")) {
            if (this.career == Job.UNEMPLOYED) {
                simcli.ui.UIManager.printMessage(this.name + " is unemployed and cannot work!");
                return;
            }
            int dailyPay = this.career.getSalaryAtTier(this.jobTier);
            simcli.ui.UIManager.printMessage(
                    this.name + " works a shift as a " + this.career.getTitle() + " and earns $" + dailyPay + "!");

            int multiplier = 1 + this.shiftsWorkedToday;
            if (multiplier > 1) {
                simcli.ui.UIManager.printMessage(this.name + " feels the heavy strain of overworking!");
            }
            this.energy.decrease(this.career.getEnergyDrain() * multiplier);
            this.hunger.decrease(20 * multiplier);
            this.hygiene.decrease(30 * multiplier);

            this.setMoney(this.getMoney() + dailyPay);
            this.addTotalMoneyEarned(dailyPay);
            this.consecutiveDaysMissed = 0;
            this.shiftsWorkedToday++;
        } else {
            simcli.ui.UIManager.printMessage(this.name + " is idling.");
        }
    }

    public void checkTruancy() {
        if (this.career == Job.UNEMPLOYED)
            return;
        this.consecutiveDaysMissed++;
        if (this.consecutiveDaysMissed > 3) {
            simcli.ui.UIManager.printWarning("Oh no! " + this.name + " missed too many days of work and was fired from "
                    + this.career.getTitle() + ".");
            this.career = Job.UNEMPLOYED;
            this.jobTier = 1;
            this.consecutiveDaysMissed = 0;
            this.shiftsWorkedToday = 0;
        } else if (this.consecutiveDaysMissed > 0) {
            simcli.ui.UIManager
                    .printWarning(this.name + " missed work! Consecutive days missed: " + this.consecutiveDaysMissed);
        }
    }

    public void promote() {
        if (this.career == Job.UNEMPLOYED)
            return;
        if (this.jobTier < this.career.getMaxTier()) {
            this.jobTier++;
            simcli.ui.UIManager.printMessage("\n*** PROMOTION! " + this.name + " has been promoted to tier "
                    + this.jobTier + " in " + this.career.getTitle() + "! ***");
        }
    }

    public void changeJob(Job newJob) {
        this.career = newJob;
        this.jobTier = 1;
        this.consecutiveDaysMissed = 0;
        this.shiftsWorkedToday = 0;
        simcli.ui.UIManager.printMessage(this.name + " has started a new job as a " + newJob.getTitle() + ".");
    }

    @Override
    public void growOlderDaily() {
        super.growOlderDaily();
        this.shiftsWorkedToday = 0;
        this.hasWarnedAboutOverwork = false;
        if (this.age >= 65 && this.career != Job.UNEMPLOYED) {
            simcli.ui.UIManager.printMessage("\n*** RETIREMENT! " + this.name
                    + " has reached the retirement age of 65 and is officially retired. ***");
            this.changeJob(Job.UNEMPLOYED);
            this.setMoney(this.getMoney() + 1000); // 1000 Simoleon pension
        }
    }
}