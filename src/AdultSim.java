/**
 * Concrete class representing an employable, player-controlled Sim.
 */
public class AdultSim extends Sim {
    private String career;
    
    // Constructor Overloading
    public AdultSim(String name, int age, String career) {
        super(name, age);
        this.career = career;
    }
    
    public AdultSim(String name) {
        this(name, 21, "Unemployed"); 
    }
    
    @Override
    public void performActivity(String activityType) throws SimulationException {
        if (this.state == SimState.DEAD || this.state == SimState.CRITICAL) {
            throw new SimulationException(this.name + " is in critical condition and refuses to act.");
        }
        
        if (activityType.equalsIgnoreCase("Work")) {
            if (this.state == SimState.TIRED || this.state == SimState.HUNGRY) {
                throw new SimulationException(this.name + " is too miserable to go to work.");
            }
            System.out.println(this.name + " heads to the office to work as a " + this.career + ".");
            this.energy.decrease(30);
            this.hunger.decrease(20);
        } else {
            System.out.println(this.name + " is idling.");
        }
    }
}