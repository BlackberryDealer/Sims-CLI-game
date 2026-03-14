package simcli.ui.ascii.providers;

import simcli.entities.Sim;
import simcli.world.Building;
import simcli.ui.ascii.IAsciiProvider;

public class StudyingAsciiProvider implements IAsciiProvider {
    @Override
    public String getAsciiArt(Sim player, Building location) {
        return "   [ " + player.getName() + " is studying and reading... ]";
    }
}
