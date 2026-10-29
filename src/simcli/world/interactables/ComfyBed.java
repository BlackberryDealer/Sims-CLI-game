package simcli.world.interactables;

import simcli.entities.actors.Sim;
import simcli.utils.GameConstants;

/**
 * A better bed: the Sim wakes up happier ({@link GameConstants#COMFY_BED_FUN}
 * fun) and loses only half as much hunger overnight.
 */
public class ComfyBed extends Bed {

    @Override
    protected int overnightHunger(int hours) {
        return super.overnightHunger(hours) / 2;
    }

    @Override
    protected void afterSleep(Sim sim, int hours) {
        // Sim.sleep() charged the plain bed's hunger cost; give the difference back
        sim.getHunger().increase(super.overnightHunger(hours) - overnightHunger(hours));
        sim.getHappiness().increase(GameConstants.COMFY_BED_FUN);
    }

    @Override
    public String getObjectName() {
        return "Comfy Bed";
    }
}
