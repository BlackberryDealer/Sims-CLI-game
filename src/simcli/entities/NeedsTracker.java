package simcli.entities;

import simcli.needs.*;
import simcli.engine.SimulationLogger;

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

    public void tick(double ageMultiplier, double stageEnergyModifier, String simName, int money) {
        if (this.state == SimState.DEAD) return;

        this.hunger.decay(ageMultiplier);
        this.energy.decay(ageMultiplier * stageEnergyModifier);
        this.hygiene.decay(ageMultiplier);
        this.fun.decay(ageMultiplier);
        this.social.decay(ageMultiplier);
        this.updateState();

        SimulationLogger.log("[" + simName + "] Hunger: " + this.hunger.getValue() +
                " | Energy: " + this.energy.getValue() +
                " | Social: " + this.social.getValue() +
                " | Hygiene: " + this.hygiene.getValue() +
                " | Fun: " + this.fun.getValue() +
                " | Cash: $" + money + " | Status: " + this.state);
    }

    public void updateState() {
        if (this.hunger.getValue() <= 0) {
            this.state = SimState.CRITICAL;
            this.health -= 5;
            this.starvingTicks = 0;
            if (this.health <= 0)
                this.state = SimState.DEAD;
        } else {
            if (this.hunger.getValue() <= 10) {
                this.state = SimState.STARVING;
                this.starvingTicks++;
            } else {
                this.starvingTicks = 0;
                if (this.hunger.getValue() <= 20)
                    this.state = SimState.HUNGRY;
                else if (this.energy.getValue() <= 20)
                    this.state = SimState.TIRED;
                else
                    this.state = SimState.HEALTHY;
            }
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
