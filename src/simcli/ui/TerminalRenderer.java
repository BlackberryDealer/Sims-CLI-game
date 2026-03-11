package simcli.ui;

import simcli.entities.Sim;
import simcli.world.interactables.Interactable;
import java.util.List;

public class TerminalRenderer implements IRenderer {

    @Override
    public void clear() {
        UIManager.clearScreen();
    }

    @Override
    public void printHint() {
        UIManager.printHint();
    }

    @Override
    public void renderHUD(Sim player, String locationName, int day, String formattedTime, String timeOfDay, boolean inRoom,
            String roomName) {

        if (locationName.contains("Dorm") || locationName.contains("Home")) {
            AsciiArt.printHouse();
        } else if (locationName.contains("Supermarket") || locationName.contains("Market")) {
            AsciiArt.printStore();
        } else {
            System.out.println("   [" + locationName.toUpperCase() + "]");
        }

        if (inRoom) {
            System.out.println("\n--- DAY " + day + " | " + timeOfDay
                    + " (" + formattedTime + ") | Location: " + locationName
                    + " - Room: " + roomName + " ---");
        } else {
            System.out.println("\n--- DAY " + day + " | " + timeOfDay
                    + " (" + formattedTime + ") | Location: " + locationName
                    + " ---");
        }
    }

    @Override
    public void renderActions(List<Interactable> items, boolean isResidential) {
        UIManager.printActionGrid(items, isResidential);
    }
}
