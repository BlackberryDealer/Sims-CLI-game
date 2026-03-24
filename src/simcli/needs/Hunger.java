package simcli.needs;

import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;

/**
 * Represents the immediate dietary requirements of a Sim.
 */
public class Hunger extends Need {

    /**
     * Constructs a Hunger need instance with a base decay of 5.
     */
    public Hunger() {
        super("Hunger", 5);
    }

    /**
     * Calculates Hunger decay dynamically based on ActionState.
     * Hunger decreases faster when working and slower when sleeping.
     * @param sim The referencing Sim whose Hunger is decaying.
     */
    @Override
    public void calculateDecay(Sim sim) {
        int decayAmt = this.getBaseDecayRate();
        if (sim.getCurrentAction() == ActionState.WORKING || sim.getCurrentAction() == ActionState.PLAYING) {
            decayAmt += 3; // Accelerated decay
        } else if (sim.getCurrentAction() == ActionState.SLEEPING) {
            decayAmt -= 3; // Muted decay
        }
        this.decrease(decayAmt);
    }
}