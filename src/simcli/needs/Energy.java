package simcli.needs;

import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;
import simcli.utils.GameConstants;

/**
 * Represents the Energy need of a Sim.
 */
public class Energy extends Need {
    /**
     * Constructor setting base Decay to 2.
     */
    public Energy() {
        super("Energy", GameConstants.ENERGY_BASE_DECAY_RATE);
    }

    /**
     * Calculates Energy decay. Restores energy when sleeping.
     * @param sim The referencing Sim.
     */
    @Override
    public void calculateDecay(Sim sim) {
        if (sim.getCurrentAction() == ActionState.SLEEPING) {
            this.increase(10); // Refresh when sleeping
        } else if (sim.getCurrentAction() == ActionState.WORKING) {
            this.decrease(this.getBaseDecayRate() + 2);
        } else {
            this.decrease(this.getBaseDecayRate());
        }
    }
}