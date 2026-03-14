package simcli.ui.ascii.providers;

import simcli.entities.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

/**
 * StudyingAsciiProvider — 2D side-view of a stickman Sim reading at a desk.
 *
 * Visual: stickman leaning over an open book on a desk, stack of books beside
 * them.
 * Art style: simple 2D terminal ASCII, designed to fit ~50 columns.
 */
public class StudyingAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        return "          |            \n" +
                "         /|\\  o        \n" +
                "         / \\ /|\\       \n" +
                "   ______|__/ \\______  \n" +
                "  | BOOK | BOOK | :) | \n" +
                "  |______|______|____| \n" +
                "   \" " + player.getName() + " is studying! \"";
    }
}
