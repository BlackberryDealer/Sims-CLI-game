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

    /**
     * Ticks the needs by triggering dynamic calculation and pushing states.
     * @param sim The specific Sim experiencing the tick.
     * @param ageMultiplier Historical age decay impact.
     * @param stageEnergyModifier Unique limits per lifestage.
     * @param simName The display name of the owner.
     * @param money The total cash of owner.
     */
    public void tick(Sim sim, double ageMultiplier, double stageEnergyModifier, String simName, int money) {
        if (this.state == SimState.DEAD) return;

        // Uses Polymorphism calculateDecay required by constraints
        this.hunger.calculateDecay(sim);
        this.energy.calculateDecay(sim);
        this.hygiene.calculateDecay(sim);
        this.fun.calculateDecay(sim);
        this.social.calculateDecay(sim);

        this.updateState();

        SimulationLogger.log("[" + simName + "] Hunger: " + this.hunger.getValue() +
                " | Energy: " + this.energy.getValue() +
                " | Social: " + this.social.getValue() +
                " | Hygiene: " + this.hygiene.getValue() +
                " | Fun: " + this.fun.getValue() +
                " | Cash: $" + money + " | Status: " + this.state);
    }

    /**
     * Resolves value thresholds into strict Enum definitions sequentially.
     */
    public void updateState() {
        if (this.hunger.getValue() <= 0) {
            this.state = SimState.DEAD;
        } else if (this.energy.getValue() <= 20) {
            this.state = SimState.TIRED;
        } else if (this.hunger.getValue() <= 30) {
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
