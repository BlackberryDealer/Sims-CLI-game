package simcli.ui;

import simcli.entities.Sim;
import simcli.world.Building;
import simcli.world.interactables.Interactable;
import simcli.ui.ascii.AsciiEngine;
import java.util.List;

public class TerminalRenderer implements IRenderer {
    private AsciiEngine asciiEngine;

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
    public void renderHUD(Sim player, Building location, int day, String formattedTime, String timeOfDay, boolean inRoom,
            String roomName) {
        
        System.out.println(asciiEngine.render(player, location));

        String locationName = location != null ? location.getName() : "Unknown";

        if (inRoom) {
            System.out.println("\n--- DAY " + day + " | " + timeOfDay
                    + " (" + formattedTime + ") | Location: " + locationName
                    + " - Room: " + roomName + " ---");
        } else {
            System.out.println("\n--- DAY " + day + " | " + timeOfDay
                    + " (" + formattedTime + ") | Location: " + locationName
                    + " ---");
        }
        
        // Reset action to IDLE after rendering, so they dont permanently sleep on screen until next action
        player.setCurrentAction(simcli.entities.ActionState.IDLE);
    }

    @Override
    public void renderActions(List<Interactable> items, boolean isResidential) {
        UIManager.printActionGrid(items, isResidential);
    }
}
