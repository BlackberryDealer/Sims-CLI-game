package simcli.ui.ascii.providers;

import simcli.entities.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

public class StoreAsciiProvider implements IAsciiProvider {
    @Override
    public String getAsciiArt(Sim player, Building location) {
        return "   [MARKET]\n" +
               "   /______\\\n" +
               "   | OPEN |\n" +
               "   |  $$  |\n" +
               "   |______|";
    }
}
