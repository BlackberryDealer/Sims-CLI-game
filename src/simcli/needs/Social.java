package simcli.needs;

import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;

/**
 * Represents the Social isolation need of a Sim.
 */
public class Social extends Need {
    /**
     * Constructor setting base Decay.
     */
    public Social() {
        super("Social", 5);
    }

    /**
     * Calculates Social decay. Regenerates when socially interacting.
     * @param sim The referencing Sim.
     */
    @Override
    public void calculateDecay(Sim sim) {
        if (sim.getCurrentAction() == ActionState.SOCIALIZING) {
            this.increase(15);
        } else {
            this.decrease(this.getBaseDecayRate());
        }
    }
}
