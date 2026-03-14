package simcli.ui.ascii.providers;

import simcli.entities.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

/**
 * HouseAsciiProvider — 2D front-view of a residential home.
 *
 * Single authoritative source of house ASCII art.
 * Replaces the duplicate printHouse() that existed in AsciiArt.java.
 * Art style: simple 2D terminal ASCII, designed to fit ~50 columns.
 */
public class HouseAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        String locName = (location != null) ? location.getName() : "Home";
        return "        /\\             \n" +
                "       /  \\            \n" +
                "      / ** \\     o     \n" +
                "     /______\\   /|\\    \n" +
                "     | [__] |   / \\    \n" +
                "     | |  | |          \n" +
                "     |_|__|_|          \n" +
                "   [ " + locName + " ]";
    }
}
