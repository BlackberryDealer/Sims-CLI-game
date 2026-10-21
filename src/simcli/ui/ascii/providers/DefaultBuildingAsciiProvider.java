package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.ui.Style;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

/**
 * Fallback scene for any building without its own art: a plain building
 * whose sign is sized to fit the building's name.
 */
public class DefaultBuildingAsciiProvider implements IAsciiProvider {

    /** The facade rows are all exactly this wide so they stay aligned. */
    private static final String[] FACADE = {
            "[##]   ______   [##]",
            "       |    |       ",
            "_______|___o|_______",
    };

    @Override
    public String getAsciiArt(Sim player, Building location) {
        String name = (location != null) ? location.getName().toUpperCase() : "SOMEWHERE";
        if (name.length() > 40) {
            name = name.substring(0, 40);
        }
        int inner = Math.max(name.length() + 6, FACADE[0].length() + 4);
        int left = (inner - FACADE[0].length()) / 2;
        int right = inner - FACADE[0].length() - left;

        return ""
                + "    {w}." + "-".repeat(inner) + ".{/}\n"
                + "    {w}|{Y}" + Style.center(name, inner) + "{w}|{/}\n"
                + "    {w}|" + "_".repeat(inner) + "|{/}\n"
                + "    {w}|" + " ".repeat(left) + "{C}" + FACADE[0] + "{w}" + " ".repeat(right) + "|{/}\n"
                + "    {w}|" + " ".repeat(left) + FACADE[1] + " ".repeat(right) + "|{/}\n"
                + "    {w}|" + "_".repeat(left) + FACADE[2] + "_".repeat(right) + "|{/}\n"
                + "  {k}" + "~".repeat(inner + 6) + "{/}";
    }
}
