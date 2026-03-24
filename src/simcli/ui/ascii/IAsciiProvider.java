package simcli.ui.ascii;

import simcli.entities.actors.Sim;
import simcli.world.Building;

public interface IAsciiProvider {
    String getAsciiArt(Sim player, Building location);
}
