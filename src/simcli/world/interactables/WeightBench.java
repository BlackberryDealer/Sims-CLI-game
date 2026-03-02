package simcli.world.interactables;
import simcli.engine.SimulationException;
import simcli.entities.Sim;
import simcli.world.Interactable;

public class WeightBench implements Interactable {
    @Override
    public void interact(Sim sim) throws SimulationException {
        if (sim.getHunger().getValue() < 30) {
            throw new SimulationException(sim.getName() + " lacks the energy for a hypertrophy strength training session.");
        }
        System.out.println(sim.getName() + " hits the gym for a high-intensity full-body workout.");
        sim.getEnergy().decrease(25);
        sim.getHunger().decrease(25);
    }
    
    @Override
    public String getObjectName() { return "Weight Bench"; }
}