package simcli.needs;

import simcli.entities.actors.Sim;

/**
 * Represents the overarching Happiness metric for a Sim.
 */
public class Happiness extends Need {
    /** Constructor */
    public Happiness() {
        super("Happiness", 2);
    }

    /**
     * Calculates happiness loss based on generic tick limits.
     * @param sim The owner.
     */
    @Override
    public void calculateDecay(Sim sim) {
        this.decrease(this.getBaseDecayRate());
    }
}
