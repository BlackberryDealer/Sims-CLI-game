package simcli.needs;

import simcli.entities.actors.Sim;
import simcli.entities.models.SimState;
import simcli.engine.SimulationLogger;

/**
 * Encapsulates management of Sim Needs properties and translates boundaries into SimState evaluations.
 */
public class NeedsTracker {
    private Need hunger;
    private Need energy;
    private Need hygiene;
    private Need fun;
    private Need social;
    private SimState state;
    private int health;
    private int starvingTicks;

    public NeedsTracker() {
        this.hunger = new Hunger();
        this.energy = new Energy();
        this.hygiene = new Hygiene();
        this.fun = new Fun();
        this.social = new Social();
        this.state = SimState.HEALTHY;
        this.health = 100;
        this.starvingTicks = 0;
    }

    public void tick(double ageMultiplier, double stageEnergyModifier, String simName) {
        if (this.state == SimState.DEAD) return;

        this.hunger.decay(ageMultiplier);
        this.energy.decay(ageMultiplier * stageEnergyModifier);
        this.hygiene.decay(ageMultiplier);
        this.fun.decay(ageMultiplier);
        this.social.decay(ageMultiplier);
        this.applyCrossPenalties();
        this.updateState();

        SimulationLogger.log(String.format("[%s] H:%d | E:%d | S:%d | Status: %s", 
            simName, 
            hunger.getValue(), 
            energy.getValue(), 
            social.getValue(), 
            this.state));
    }

    private void applyCrossPenalties() {
        if (this.hygiene.getValue() <= 10) {
            this.social.decrease(5);
        }
        if (this.fun.getValue() <= 15) {
            this.energy.decrease(3);
        }
        if (this.social.getValue() <= 10) {
            this.fun.decrease(3);
            this.energy.decrease(2);
        }
    }

    /**
     * Resolves value thresholds into strict Enum definitions sequentially.
     */
    public void updateState() {
        if (this.hunger.getValue() <= 0) {
            this.state = SimState.DEAD;
        } else if (this.energy.getValue() <= 20) {
            this.state = SimState.TIRED;
        } else if (this.hunger.getValue() <= simcli.utils.GameConstants.HUNGER_WARNING_LEVEL) {
            this.state = SimState.HUNGRY;
        } else {
            this.state = SimState.HEALTHY;
        }
    }

    public Need getHunger() { return hunger; }
    public Need getEnergy() { return energy; }
    public Need getHygiene() { return hygiene; }
    public Need getFun() { return fun; }
    public Need getSocial() { return social; }
    public SimState getState() { return state; }
    public void setState(SimState state) { this.state = state; }
    public int getHealth() { return health; }
    public int getStarvingTicks() { return starvingTicks; }
    public void setHealth(int health) { this.health = health; }
}
