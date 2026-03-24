package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

/**
 * StoreAsciiProvider — 2D front-view of a market/supermarket building.
 *
 * Single authoritative source of store ASCII art.
 * Replaces the duplicate printStore() that existed in AsciiArt.java.
 * Art style: simple 2D terminal ASCII, designed to fit ~50 columns.
 */
public class StoreAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        String locName = (location != null) ? location.getName() : "Market";
        return "  ___________________  \n" +
                " | *** SUPERMARKET ***|\n" +
                " |___________________|\n" +
                " | $ | fruits | $ |   \n" +
                " |___|________|___|   \n" +
                " | [   OPEN   ]   |   \n" +
                " |________________|   \n" +
                "   [ " + locName + " ]";
    }
}
