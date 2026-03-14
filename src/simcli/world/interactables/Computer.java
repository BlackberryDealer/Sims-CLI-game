package simcli.world.interactables;

import simcli.engine.SimulationException;
import simcli.entities.Sim;

public class Computer implements Interactable {
    @Override
    public void interact(Sim sim, java.util.Scanner scanner, simcli.engine.TimeManager timeManager)
            throws SimulationException {
        if (sim.getEnergy().getValue() < 15) {
            throw new SimulationException(
                    sim.getName() + " is too exhausted to focus on algorithms and time complexity.");
        }
        sim.setCurrentAction(simcli.entities.ActionState.STUDYING);
        simcli.ui.UIManager.displayActionAnimation(sim);
        simcli.ui.UIManager
                .printMessage(sim.getName() + " fires up the PC to study data structures and network protocols.");
        sim.getEnergy().decrease(15);
    }

    @Override
    public String getObjectName() {
        return "Computer";
    }
}