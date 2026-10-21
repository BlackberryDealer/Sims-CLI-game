package simcli.ui.ascii;

import simcli.entities.actors.Sim;
import simcli.world.Building;

import java.util.List;

/**
 * Contract that all ASCII art providers must implement to supply visual art
 * for the terminal renderer. Used by the AsciiEngine to render the Sim's
 * current state or location dynamically.
 *
 * <p>Art may contain colour tags (see {@link simcli.ui.Style}).</p>
 */
public interface IAsciiProvider {

    /** The still image for this scene — shown in the HUD. */
    String getAsciiArt(Sim player, Building location);  // all ascii art must have playername and location to fit scenario

    /**
     * Animation frames for this scene, played once when the action happens.
     * Defaults to the still image only.
     */
    default List<String> getFrames(Sim player, Building location) {
        return List.of(getAsciiArt(player, location));
    }
}
