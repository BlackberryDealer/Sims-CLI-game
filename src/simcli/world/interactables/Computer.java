package simcli.world.interactables;

import simcli.engine.SimulationException;
import simcli.engine.SimulationLogger;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;
import simcli.entities.models.SimState;
import simcli.entities.models.SkillType;
import simcli.utils.GameConstants;

import java.util.Scanner;

/**
 * A utility interactable where a Sim studies, gaining a little happiness
 * and training the {@link SkillType#LOGIC} skill.
 */
public class Computer implements Interactable {
    private static final int ENERGY_COST = 15;
    private static final int HUNGER_COST = 10;
    private static final int HAPPINESS_GAIN = 5;

    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException {
        // Slide 5: TIRED state blocks studying (Aligned with Proposal Slides)
        if (sim.getState() == SimState.TIRED) {
            throw new SimulationException(sim.getName() + " is too tired to study! Get some sleep first.");
        }
        
        sim.setCurrentAction(ActionState.STUDYING);
        SimulationLogger.getInstance().logAnimation(sim);
        SimulationLogger.getInstance().log(sim.getName() + " fires up the PC to study data structures and network protocols!");
        
        sim.getEnergy().decrease(ENERGY_COST);
        sim.getHunger().decrease(HUNGER_COST);
        sim.getHappiness().increase(HAPPINESS_GAIN);
        sim.trainSkill(SkillType.LOGIC, GameConstants.STUDY_LOGIC_XP);
        SimulationLogger.getInstance().log(sim.getName() + "'s Logic skill is now level "
                + sim.getSkillLevel(SkillType.LOGIC) + ".");
    }

    @Override
    public String getObjectName() {
        return "Computer";
    }
}