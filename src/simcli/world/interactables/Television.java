package simcli.world.interactables;

import simcli.engine.SimulationException;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;
import simcli.ui.UIManager;
import simcli.utils.GameConstants;

import java.util.Scanner;

/**
 * A TV: an hour on the sofa for {@link GameConstants#TV_FUN} fun, the
 * easiest way to cheer a Sim up at home.
 */
public class Television implements Interactable {

    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException {
        sim.setCurrentAction(ActionState.PLAYING);
        UIManager.displayActionAnimation(sim);
        sim.getHappiness().increase(GameConstants.TV_FUN);
        UIManager.success(sim.getName() + " flops on the sofa and watches TV.  {G}+" + GameConstants.TV_FUN
                + " fun{/}");
    }

    @Override
    public String getObjectName() {
        return "TV";
    }
}
