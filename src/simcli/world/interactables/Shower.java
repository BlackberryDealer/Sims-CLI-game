package simcli.world.interactables;

import simcli.engine.SimulationException;
import simcli.engine.TimeManager;
import simcli.entities.actors.Sim;
import simcli.entities.models.ActionState;
import simcli.needs.Need;
import simcli.ui.UIManager;

import java.util.Scanner;

/**
 * A bathroom interactable that fully restores a Sim's hygiene need
 * at the cost of a small amount of time.
 */
public class Shower implements Interactable {

    @Override
    public String getObjectName() {
        return "Shower";
    }

    @Override
    public void interact(Sim sim, Scanner scanner, TimeManager timeManager) throws SimulationException {
        sim.setCurrentAction(ActionState.BATHING);
        UIManager.displayActionAnimation(sim);
        sim.getHygiene().increase(50);
        UIManager.success(sim.getName() + " takes a long, refreshing shower.  {G}+50 hygiene{/} {k}(now "
                + sim.getHygiene().getValue() + "/" + Need.MAX_VALUE + "){/}");
    }
}
