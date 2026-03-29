package simcli.ui.ascii;

import simcli.entities.actors.Sim;
import simcli.world.Building;

/**
 * UI component handing formatting or displaying of IAsciiProvider.
 */
public interface IAsciiProvider {
    String getAsciiArt(Sim player, Building location);
}
