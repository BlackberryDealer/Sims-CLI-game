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
        return "        o           \n" +
                "       /|\\  nom!    \n" +
                "       / \\          \n" +
                "  ____(_____)____   \n" +
                " |   [~BOWL~] f  |  \n" +
                " |_______________|  \n" +
                "   \" " + player.getName() + " is eating \"";
    }
}
