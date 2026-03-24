package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
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
        return "    ) )        /\\\n" +
            "   =====      /  \\\n" +
            "  _|___|_____/ __ \\____________\n" +
            " |::::::::::/ |  | \\:::::::::::|\n" +
            " |:::::::::/  ====  \\::::::::::|\n" +
            " |::::::::/__________\\:::::::::|\n" +
            " |_________|  ____  |__________|\n" +
            "  | ______ | / || \\ | _______ |\n" +
            "  ||  |   || ====== ||   |   ||\n" +
            "  ||--+---|| |    | ||---+---||\n" +
            "  ||__|___|| |   o| ||___|___||\n" +
            "  |========| |____| |=========|\n" +
            " (^^-^^^^^-|________|-^^^--^^^)\n" +
            " (,, , ,, ,/________\\,,,, ,, ,)\n" +
            "','',,,,' /__________\\,,,',',;;"+
                "   [ " + locName + " ]";
    }
}
