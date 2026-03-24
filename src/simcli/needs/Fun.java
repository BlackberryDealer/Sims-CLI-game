package simcli.needs;

import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;

/**
 * Represents the Fun entertainment metric for the Sim.
 */
public class Fun extends Need {
    /** Constructor */
    public Fun() {
        super("Fun", 2);
    }

    /**
     * Overrides decay to adapt to playing and working.
     * @param sim The owner.
     */
    @Override
    public void calculateDecay(Sim sim) {
        if (sim.getCurrentAction() == ActionState.PLAYING) {
            this.increase(12);
        } else if (sim.getCurrentAction() == ActionState.WORKING) {
            this.decrease(this.getBaseDecayRate() + 4);
        } else {
            this.decrease(this.getBaseDecayRate());
        }
    }
}
