package simcli.entities;
import simcli.engine.SimulationException;

/**
 * Concrete class representing an employable, player-controlled Sim.
 */
public class AdultSim extends Sim {
    private Job career;
    
    // Constructor Overloading
    public AdultSim(String name, int age, Job career) {
        super(name, age);
        this.career = career;
    }
    
    public AdultSim(String name) {
        this(name, 21, Job.SOFTWARE_ENGINEER); 
    }

    public Job getCareer() { return this.career; }
    
    @Override
    public void performActivity(String activityType) throws SimulationException {
        if (this.state == SimState.DEAD || this.state == SimState.CRITICAL) {
            throw new SimulationException(this.name + " is in critical condition and refuses to act.");
        }
        
        if (activityType.equalsIgnoreCase("Work")) {
            System.out.println(this.name + " works a shift as a " + this.career.getTitle() + " and earns $" + this.career.getSalary() + "!");
            this.energy.decrease(30);
            this.hunger.decrease(20);
            this.setMoney(this.getMoney() + this.career.getSalary());
        } else {
            System.out.println(this.name + " is idling.");
        }
    }
}