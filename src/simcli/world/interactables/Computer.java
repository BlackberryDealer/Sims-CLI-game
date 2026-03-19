package simcli.world.interactables;

import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.entities.Sim;
import simcli.entities.ActionState;
import simcli.entities.SkillType;
import simcli.entities.Trait;

public class Computer implements Interactable {
    @Override
    public void interact(Sim sim, java.util.Scanner scanner, simcli.engine.TimeManager timeManager) throws SimulationException {
        // Slide 5: TIRED state blocks studying
        if (sim.getState() == simcli.entities.SimState.TIRED) {
            throw new SimulationException(sim.getName() + " is too tired to study! Get some sleep first.");
        }
        if (sim.getEnergy().getValue() < 15) {
            throw new SimulationException(sim.getName() + " is too exhausted to focus on algorithms and time complexity.");
        }
        
        sim.setCurrentAction(ActionState.STUDYING);
        SimulationLogger.logAnimation(sim);
        SimulationLogger.log(sim.getName() + " fires up the PC to study data structures and network protocols!");
        
        sim.getEnergy().decrease(15);
        sim.getHunger().decrease(10);
        sim.getFun().increase(5);
        
        sim.getSkillManager().addSkillExperience(SkillType.LOGIC, 15, sim.getName(), sim.hasTrait(Trait.FAST_LEARNER));
    }

    @Override
    public String getObjectName() {
        return "Computer";
    }
}