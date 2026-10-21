package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

/**
 * The Bookshop: a gabled shop with shelves of colourful spines in the window.
 */
public class BookshopAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        String spinesA = "{R}#{G}#{B}#{Y}#{M}#{C}#{R}#{/} {G}#{B}#{Y}#{M}#{C}#{R}#{G}#{B}#{Y}#{M}#{C}#";
        String spinesB = "{C}#{M}#{Y}#{B}#{G}#{R}#{C}#{/} {M}#{Y}#{B}#{G}#{R}#{C}#{M}#{Y}#{B}#{G}#{R}#";
        return ""
                + "        {y}" + "_".repeat(38) + "{/}\n"
                + "       {y}/" + " ".repeat(38) + "\\{/}\n"
                + "      {y}/      {W}~  T H E   B O O K S H O P  ~{y}     \\{/}\n"
                + "     {y}/" + "_".repeat(42) + "\\{/}\n"
                + "      {y}|  " + "_".repeat(19) + "     " + "_".repeat(11) + "   |{/}\n"
                + "      {y}| |" + spinesA + "{y}|   |     |     |  |{/}\n"
                + "      {y}| |" + spinesB + "{y}|   |    {Y}o{y}|     |  |{/}\n"
                + "      {y}|_|___________________|___|_____|_____|__|{/}\n"
                + "    {k}" + "~".repeat(46) + "{/}";
    }
}
