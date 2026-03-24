package simcli.ui;

import simcli.entities.actors.Sim;
import simcli.world.Building;
import simcli.world.interactables.Interactable;
import simcli.ui.ascii.AsciiEngine;

import java.util.List;

/**
 * Concrete implementation of {@link IRenderer} for terminal-based display.
 * All output is routed through {@link UIManager} for consistency.
 */
public class TerminalRenderer implements IRenderer {
    private final AsciiEngine asciiEngine;

    public TerminalRenderer() {
        this.asciiEngine = new AsciiEngine();
    }

    @Override
    public void clear() {
        UIManager.clearScreen();
    }

    @Override
    public void printHint() {
        UIManager.printHint();
    }

    @Override
    public void renderHUD(Sim player, Building location, int day, String formattedTime,
            String timeOfDay, boolean inRoom, String roomName) {
        
        UIManager.printMessage(asciiEngine.render(player, location));

        String locationName = location != null ? location.getName() : "Unknown";

        if (inRoom) {
            UIManager.printMessage("\n--- DAY " + day + " | " + timeOfDay
                    + " (" + formattedTime + ") | Location: " + locationName
                    + " - Room: " + roomName + " ---");
        } else {
            UIManager.printMessage("\n--- DAY " + day + " | " + timeOfDay
                    + " (" + formattedTime + ") | Location: " + locationName
                    + " ---");
        }
        
        // Reset action to IDLE after rendering so art doesn't persist next frame
        player.setCurrentAction(simcli.entities.actors.ActionState.IDLE);
    }

    @Override
    public void renderActions(List<Interactable> items, boolean isResidential) {
        UIManager.printActionGrid(items, isResidential);
    }
}
