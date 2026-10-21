package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

/**
 * The Supermarket: a shop front with a striped awning and stocked windows.
 */
public class StoreAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        String fruit = "{R}ooo {Y}ooo {G}oo{/}";
        String bread = "{y}@ @ @ @ @ {/}";
        String shelf = "{y}=========={/}";
        return ""
                + "    {w}" + "_".repeat(46) + "{/}\n"
                + "   {w}|{/} {Y}S U P E R M A R K E T{/}             {G}* OPEN *{/}   {w}|{/}\n"
                + "   {w}|" + "_".repeat(46) + "|{/}\n"
                + "   " + "{r}\\/\\/{w}\\/\\/".repeat(6) + "{/}\n"
                + "   {w}|  " + "_".repeat(10) + "       " + "_".repeat(8) + "       " + "_".repeat(10) + "  |{/}\n"
                + "   {w}| {C}|{/}" + fruit + "{C}|{w}     |   ||   |     {C}|{/}" + bread + "{C}|{w} |{/}\n"
                + "   {w}| {C}|{/}" + shelf + "{C}|{w}     |   ||   |     {C}|{/}" + shelf + "{C}|{w} |{/}\n"
                + "   {w}|_{C}|__________|{w}_____|___||___|_____{C}|__________|{w}_|{/}\n"
                + "  {k}" + "~".repeat(50) + "{/}";
    }
}
