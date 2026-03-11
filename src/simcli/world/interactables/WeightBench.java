package simcli.world.interactables;
import simcli.engine.SimulationException;
import simcli.entities.Sim;

public class WeightBench implements Interactable {
    @Override
    public void interact(Sim sim, java.util.Scanner scanner, simcli.engine.TimeManager timeManager) throws SimulationException {
        if (sim.getHunger().getValue() < 30) {
            throw new SimulationException(sim.getName() + " lacks the energy for a hypertrophy strength training session.");
        }
        simcli.ui.UIManager.printMessage(sim.getName() + " hits the gym for a high-intensity full-body workout.");
        sim.getEnergy().decrease(25);
        sim.getHunger().decrease(25);
    }
    
    @Override
    public String getObjectName() { return "Weight Bench"; }
}