package simcli.ui.ascii.providers;

import simcli.entities.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

/**
 * SocializingAsciiProvider — 2D view of two stickman Sims talking face-to-face.
 *
 * Visual: two stickmen facing each other with speech bubbles between them.
 * Art style: simple 2D terminal ASCII, designed to fit ~50 columns.
 */
public class SocializingAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        return "  o         o          \n" +
                " /|\\  \" :D \"  /|\\     \n" +
                " / \\  ~~~~~~  / \\     \n" +
                "  |  speech!  |        \n" +
                "  |  bubbles  |        \n" +
                " " + player.getName() + "        Friend  \n" +
                "   \" " + player.getName() + " is socializing! \"";
    }
}
