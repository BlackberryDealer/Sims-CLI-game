package simcli.entities;

import simcli.needs.*;
import simcli.engine.SimulationLogger;

public class NeedsTracker {
    private Need hunger;
    private Need energy;
    private Need hygiene;
    private Need happiness;
    private SimState state;
    private int starvingTicks;

    public NeedsTracker() {
        this.hunger = new Hunger();
        this.energy = new Energy();
        this.hygiene = new Hygiene();
        this.happiness = new Happiness();
        this.state = SimState.HEALTHY;
        this.starvingTicks = 0;
    }

    public void tick(double ageMultiplier, double stageEnergyModifier, String simName, int money) {
        if (this.state == SimState.DEAD) return;

        this.hunger.decay(ageMultiplier);
        this.energy.decay(ageMultiplier * stageEnergyModifier);
        this.hygiene.decay(ageMultiplier);
        this.happiness.decay(ageMultiplier);
        this.updateState();

        SimulationLogger.log("[" + simName + "] Hunger: " + this.hunger.getValue() +
                " | Energy: " + this.energy.getValue() +
                " | Hygiene: " + this.hygiene.getValue() +
                " | Happiness: " + this.happiness.getValue() +
                " | Cash: $" + money + " | Status: " + this.state);
    }

    public void updateState() {
        if (this.hunger.getValue() <= 0) {
            this.state = SimState.STARVING;
            this.starvingTicks++;
            if (this.starvingTicks > 3)
                this.state = SimState.DEAD;
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

    public Need getHunger() { return hunger; }
    public Need getEnergy() { return energy; }
    public Need getHygiene() { return hygiene; }
    public Need getHappiness() { return happiness; }
    public SimState getState() { return state; }
    public void setState(SimState state) { this.state = state; }
    public int getStarvingTicks() { return starvingTicks; }
    public void setStarvingTicks(int ticks) { this.starvingTicks = ticks; }
}
