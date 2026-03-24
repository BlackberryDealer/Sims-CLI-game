package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

/**
 * SleepingAsciiProvider — 2D side-view of a stickman Sim lying in a bed.
 *
 * Visual: bed frame from the side with the Sim's head on a pillow, Z's floating
 * up.
 * Art style: simple 2D terminal ASCII, designed to fit ~50 columns.
 */
public class SleepingAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        return "  z Z z Z z             \n" +
                "  ___________________   \n" +
                " |  (o)  [==pillow==]|  \n" +
                " | --o-- ~~~~~~~~~~~  |  \n" +
                " |____________________|  \n" +
                " |__[   BED FRAME   ]_|  \n" +
                "   \" " + player.getName() + " is sleeping... \"";
    }
}
