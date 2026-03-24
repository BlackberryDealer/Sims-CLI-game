package simcli.needs;

import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;
import simcli.utils.GameConstants;

/**
 * Represents the Social isolation need of a Sim.
 */
public class Social extends Need {
    /**
     * Constructor setting base Decay.
     */
    public Social() {
        super("Social", GameConstants.SOCIAL_BASE_DECAY_RATE);
    }

    /**
     * Calculates Social decay. Regenerates when socially interacting.
     * @param sim The referencing Sim.
     */
    @Override
    public void calculateDecay(Sim sim) {
        if (sim.getCurrentAction() == ActionState.SOCIALIZING) {
            this.increase(GameConstants.SOCIAL_ADDED_AMOUNT);
        } else {
            this.decrease(this.getBaseDecayRate());
        }
    }
}
