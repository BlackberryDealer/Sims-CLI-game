package simcli.world.interactables;

import simcli.engine.SimulationException;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;
import simcli.entities.models.SimState;
import simcli.ui.UIManager;

import java.util.Scanner;

/**
 * Represents a WeightBench location or interactable object.
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
                    sim.getName() + " lacks the energy for a hypertrophy strength training session.");
        }
        sim.setCurrentAction(ActionState.WORKING);
        UIManager.displayActionAnimation(sim);
        UIManager.printMessage(sim.getName() + " hits the gym for a high-intensity full-body workout.");
        sim.getEnergy().decrease(ENERGY_COST);
        sim.getHunger().decrease(HUNGER_COST);
    }

    @Override
    public String getObjectName() {
        return "Weight Bench";
    }
}