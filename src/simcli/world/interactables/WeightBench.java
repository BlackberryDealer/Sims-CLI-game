package simcli.world.interactables;

import simcli.engine.SimulationException;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;
import simcli.entities.models.SimState;
import simcli.entities.models.SkillType;
import simcli.ui.UIManager;
import simcli.utils.GameConstants;

import java.util.Scanner;

/**
 * A piece of exercise equipment. When used, it costs energy and hunger
 * and trains the Sim's {@link SkillType#FITNESS} skill.
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
        // fitter Sims tire less from the same workout
        int energyCost = ENERGY_COST
                - sim.getSkillLevel(SkillType.FITNESS) * GameConstants.FITNESS_WORKOUT_ENERGY_SAVING;
        sim.getEnergy().decrease(energyCost);
        sim.getHunger().decrease(HUNGER_COST);
        sim.trainSkill(SkillType.FITNESS, GameConstants.WORKOUT_FITNESS_XP);
        UIManager.success(sim.getName() + " finishes a high-intensity full-body workout.  {R}-" + energyCost
                + " energy -" + HUNGER_COST + " hunger{/}  {G}+fitness{/} {k}(level "
                + sim.getSkillLevel(SkillType.FITNESS) + "){/}");
    }

    @Override
    public String getObjectName() {
        return "Weight Bench";
    }
}