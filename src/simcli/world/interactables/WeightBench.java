package simcli.world.interactables;

import simcli.engine.SimulationException;
import simcli.entities.actors.Sim;

public class WeightBench implements Interactable {
    private static final int ENERGY_COST = 25;
    private static final int HUNGER_COST = 25;

    @Override
    public void interact(Sim sim, java.util.Scanner scanner, simcli.engine.TimeManager timeManager)
            throws SimulationException {
        // Slide 5: TIRED state blocks exercise (Aligned with Proposal Slides)
        if (sim.getState() == simcli.entities.models.SimState.TIRED) {
            throw new SimulationException(
                    sim.getName() + " is too tired to exercise! Get some sleep first.");
        }
        if (sim.getHunger().getValue() < 30) {
            throw new SimulationException(
                    sim.getName() + " lacks the energy for a hypertrophy strength training session.");
        }
        sim.setCurrentAction(simcli.entities.models.ActionState.WORKING);
        simcli.ui.UIManager.displayActionAnimation(sim);
        simcli.ui.UIManager.printMessage(sim.getName() + " hits the gym for a high-intensity full-body workout.");
        sim.getEnergy().decrease(ENERGY_COST);
        sim.getHunger().decrease(HUNGER_COST);
    }

    @Override
    public String getObjectName() {
        return "Weight Bench";
    }
}