package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

/**
 * PlayingAsciiProvider — 2D view of a stickman Sim running and playing.
 *
 * Visual: stickman mid-run with arms raised. Used when ActionState == PLAYING.
 * Art style: simple 2D terminal ASCII, designed to fit ~50 columns.
 */
public class PlayingAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        return "     \\o/  wheee!       \n" +
                "      |     ->         \n" +
                "     / \\              \n" +
                "   ~  ~  ~  ~  ~  ~   \n" +
                "   (running outside)   \n" +
                "   \" " + player.getName() + " is playing! \"";
    }
}
