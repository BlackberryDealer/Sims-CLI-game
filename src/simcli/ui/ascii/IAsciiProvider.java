package simcli.ui.ascii;

import simcli.entities.actors.Sim;
import simcli.world.Building;

/**
 * Contract that all ASCII art providers must implement to supply visual art 
 * for the terminal renderer. Used by the AsciiEngine to render the Sim's 
 * current state or location dynamically.
 */
public interface IAsciiProvider {
    String getAsciiArt(Sim player, Building location);  // all ascii art must have playername and location to fit scenario
}
