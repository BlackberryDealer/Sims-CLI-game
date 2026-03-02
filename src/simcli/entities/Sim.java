package simcli.entities;
import simcli.engine.SimulationException;
import simcli.needs.Energy;
import simcli.needs.Hunger;
import simcli.needs.Need;

/**
 * Abstract base class representing any entity in the simulation.
 */
public abstract class Sim {
    protected String name;
    protected int age;
    protected Need hunger;
    protected Need energy;
    protected SimState state;
    
    protected static int globalSimCount = 0;
    
    public Sim(String name, int age) {
        this.name = name;
        this.age = age;
        this.hunger = new Hunger();
        this.energy = new Energy();
        this.state = SimState.HEALTHY;
        globalSimCount++;
    }
    
    public abstract void performActivity(String activityType) throws SimulationException;
    
    public void tick() {
        if (this.state == SimState.DEAD) return;
        
        this.hunger.decay();
        this.energy.decay();
        this.updateState();
        
        System.out.println("[" + this.name + "] Hunger: " + this.hunger.getValue() + 
                           " | Energy: " + this.energy.getValue() + 
                           " | Status: " + this.state);
    }
    
    protected void updateState() {
        if (this.hunger.getValue() == 0) {
            this.state = SimState.CRITICAL;
        } else if (this.hunger.getValue() <= 20) {
            this.state = SimState.HUNGRY;
        } else if (this.energy.getValue() <= 20) {
            this.state = SimState.TIRED;
        } else {
            this.state = SimState.HEALTHY;
        }
    }
    
    public String getName() { return this.name; }
    public Need getHunger() { return this.hunger; }
    public Need getEnergy() { return this.energy; }
    public SimState getState() { return this.state; }
}