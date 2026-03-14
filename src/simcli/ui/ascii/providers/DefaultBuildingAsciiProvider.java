package simcli.ui.ascii.providers;

import simcli.entities.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

/**
 * DefaultBuildingAsciiProvider — 2D generic building for any location
 * that doesn't have its own dedicated provider.
 *
 * Visual: simple rectangular building front-view with location name on a sign.
 * Art style: simple 2D terminal ASCII, designed to fit ~50 columns.
 */
public class DefaultBuildingAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        String locName = (location != null) ? location.getName().toUpperCase() : "UNKNOWN";
        return "   __________________  \n" +
                "  |                  | \n" +
                "  |  [ " + padOrTrim(locName, 10) + " ]  | \n" +
                "  |                  | \n" +
                "  |  [door]  [win]   | \n" +
                "  |__________________|";
    }

    /** Pads or trims a string to a fixed display width. */
    private String padOrTrim(String s, int width) {
        if (s.length() >= width)
            return s.substring(0, width);
        return s + " ".repeat(width - s.length());
    }
}
