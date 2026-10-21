package simcli.needs;

import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;
import simcli.utils.GameConstants;

/**
 * Represents the immediate dietary requirements of a Sim.
 */
public class Hunger extends Need {

    /**
     * Constructs a Hunger need instance with a base decay of 5.
     */
    public Hunger() {
        super("Hunger", GameConstants.HUNGER_BASE_DECAY_RATE);
    }

    /**
     * Calculates Hunger decay dynamically based on ActionState.
     * Hunger decreases faster when working and slower when sleeping.
     * @param sim The referencing Sim whose Hunger is decaying.
     */
    @Override
    public void calculateDecay(Sim sim, double multiplier) {
        int decayAmt = this.getBaseDecayRate();
        if (sim.getCurrentAction() == ActionState.WORKING || sim.getCurrentAction() == ActionState.PLAYING
                || sim.getCurrentAction() == ActionState.EXERCISING) {
            decayAmt += GameConstants.HUNGER_ACCELERATED_DECAY_RATE; // Accelerated decay
        } else if (sim.getCurrentAction() == ActionState.SLEEPING) {
            // Muted decay — never negative, so sleeping can't refill hunger.
            // (Sim.sleep() already applies the overnight hunger cost.)
            decayAmt = Math.max(0, decayAmt - GameConstants.HUNGER_DECELERATED_DECAY_RATE);
        }
        this.decrease((int) Math.round(decayAmt * multiplier));
    }
}