package simcli.world.interactables;

import simcli.engine.SimulationException;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;
import simcli.entities.models.SimState;
import simcli.ui.UIManager;

import java.util.Scanner;

/**
 * A piece of exercise equipment. When used, it reduces energy and hygiene
 * while occasionally increasing the Sim's fitness.
 */
public class WeightBench implements Interactable {
    private static final int ENERGY_COST = 25;
    private static final int HUNGER_COST = 25;

    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException {
        // Slide 5: TIRED state blocks exercise (Aligned with Proposal Slides)
        if (sim.getState() == SimState.TIRED) {
            throw new SimulationException(
                    sim.getName() + " is too tired to exercise! Get some sleep first.");
        }
        if (sim.getHunger().getValue() < 30) {
            throw new SimulationException(
                    sim.getName() + " is too hungry to work out. Eat something first!");
        }
        sim.setCurrentAction(ActionState.EXERCISING);
        UIManager.displayActionAnimation(sim);
        sim.getEnergy().decrease(ENERGY_COST);
        sim.getHunger().decrease(HUNGER_COST);
        UIManager.success(sim.getName() + " finishes a high-intensity full-body workout.  {R}-" + ENERGY_COST
                + " energy -" + HUNGER_COST + " hunger{/}");
    }

    @Override
    public String getObjectName() {
        return "Weight Bench";
    }
}