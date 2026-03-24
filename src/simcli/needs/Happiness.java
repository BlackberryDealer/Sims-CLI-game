package simcli.needs;

import simcli.entities.actors.Sim;
import simcli.utils.GameConstants;

/**
 * Represents the overarching Happiness metric for a Sim.
 */
public class Happiness extends Need {
    /** Constructor */
    public Happiness() {
        super("Happiness", GameConstants.HAPPINESS_BASE_DECAY_RATE);
    }

    /**
     * Calculates happiness loss based on generic tick limits.
     * @param sim The owner.
     */
    @Override
    public void calculateDecay(Sim sim, double multiplier) {
        this.decrease((int) Math.round(this.getBaseDecayRate() * multiplier));
    }
}
