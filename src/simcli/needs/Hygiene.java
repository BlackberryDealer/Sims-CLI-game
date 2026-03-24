package simcli.needs;

import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;
import simcli.utils.GameConstants;

/**
 * Represents the Hygiene component for the Sim.
 */
public class Hygiene extends Need {
    /**
     * Core hygiene constructor.
     */
    public Hygiene() {
        super("Hygiene", GameConstants.HYGIENE_BASE_DECAY_RATE);
    }

    /**
     * Calculates Hygiene decay based on action activity severity.
     * @param sim Sim owner.
     */
    @Override
    public void calculateDecay(Sim sim) {
        if (sim.getCurrentAction() == ActionState.WORKING) {
            this.decrease(this.getBaseDecayRate() + 4);
        } else {
            this.decrease(this.getBaseDecayRate());
        }
    }
}
