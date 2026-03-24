package simcli.world.interactables;

import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;
import simcli.entities.models.SkillType;
import simcli.entities.models.Trait;

public class Computer implements Interactable {
    private static final int ENERGY_COST = 15;
    private static final int HUNGER_COST = 10;
    private static final int FUN_GAIN = 5;
    private static final int LOGIC_GAIN = 15;

    @Override
    public void interact(Sim sim, java.util.Scanner scanner, simcli.engine.TimeManager timeManager) throws SimulationException {
        // Slide 5: TIRED state blocks studying (Aligned with Proposal Slides)
        if (sim.getState() == simcli.entities.models.SimState.TIRED) {
            throw new SimulationException(sim.getName() + " is too tired to study! Get some sleep first.");
        }
        
        sim.setCurrentAction(ActionState.STUDYING);
        SimulationLogger.logAnimation(sim);
        SimulationLogger.log(sim.getName() + " fires up the PC to study data structures and network protocols!");
        
        sim.getEnergy().decrease(ENERGY_COST);
        sim.getHunger().decrease(HUNGER_COST);
        sim.getFun().increase(FUN_GAIN);
        
        sim.getSkillManager().addSkillExperience(SkillType.LOGIC, LOGIC_GAIN, sim.getName(), sim.hasTrait(Trait.FAST_LEARNER));
    }

    @Override
    public String getObjectName() {
        return "Computer";
    }
}