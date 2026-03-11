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
    public void renderHUD(Sim player, String locationName, int day, int tick, String timeOfDay, boolean inRoom,
            String roomName) {
        if (tick == 3) {
            System.out.println("\n[System: The screen clears between turns to keep the UI clean.]");
        }

        if (locationName.contains("Dorm") || locationName.contains("Home")) {
            AsciiArt.printHouse();
        } else if (locationName.contains("Supermarket") || locationName.contains("Market")) {
            AsciiArt.printStore();
        } else {
            System.out.println("   [" + locationName.toUpperCase() + "]");
        }

        if (inRoom) {
            System.out.println("\n--- DAY " + day + " | " + timeOfDay
                    + " (Tick " + tick + ") | Location: " + locationName
                    + " - Room: " + roomName + " ---");
        } else {
            System.out.println("\n--- DAY " + day + " | " + timeOfDay
                    + " (Tick " + tick + ") | Location: " + locationName
                    + " ---");
        }
    }

    @Override
    public void renderActions(List<Interactable> items, boolean isResidential) {
        UIManager.printActionGrid(items, isResidential);
    }
}
