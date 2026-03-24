package simcli.ui.ascii.providers;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

/**
 * EatingAsciiProvider — 2D side-view of a stickman Sim at a dining table.
 *
 * Visual: stickman seated at a table with a bowl of food and fork.
 * Art style: simple 2D terminal ASCII, designed to fit ~50 columns.
 */
public class EatingAsciiProvider implements IAsciiProvider {

    @Override
    public String getAsciiArt(Sim player, Building location) {
        return "              ,-------------------.\n" +
            "             ( Yummy!! )\n" +
            "        munch `-v-----------------'\n" +
            " ,---'. --------'\n" +
            " C.^o^|   munch\n" +
            " (_,-_)\n" +
            ",--`|-.\n" +
            "|\\    ]\\__n_\n" +
            "||`   '---E/   " +
                "   \" " + player.getName() + " is eating \"";
    }
}
