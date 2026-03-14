package simcli.ui.ascii.providers;

import simcli.entities.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

public class HouseAsciiProvider implements IAsciiProvider {
    @Override
    public String getAsciiArt(Sim player, Building location) {
        return "      ~+~\n" +
               "     /   \\\n" +
               "    /_____\\\n" +
               "    |  _  |\n" +
               "    | | | |\n" +
               "    |_|_|_|";
    }
}
