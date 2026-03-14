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
        simcli.entities.ActionState action = player.getCurrentAction();

        // Dynamically choose ASCII art based on ActionState and Location
        if (action == simcli.entities.ActionState.SLEEPING) {
            System.out.println("   [ ZzZzZz... " + player.getName() + " is Sleeping ]");
        } else if (action == simcli.entities.ActionState.EATING) {
            System.out.println("   [ Nom Nom Nom... ]");
        } else if (action == simcli.entities.ActionState.WORKING) {
            System.out.println("   [ " + player.getName() + " is Working Hard! ]");
        } else if (action == simcli.entities.ActionState.STUDYING) {
            System.out.println("   [ " + player.getName() + " is studying and reading... ]");
        } else if (action == simcli.entities.ActionState.SOCIALIZING) {
            System.out.println("   [ Blah blah blah... Chatting! ]");
        } else if (locationName.contains("Dorm") || locationName.contains("Home")) {
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
        
        // Reset action to IDLE after rendering, so they dont permanently sleep on screen until next action
        player.setCurrentAction(simcli.entities.ActionState.IDLE);
    }

    @Override
    public void renderActions(List<Interactable> items, boolean isResidential) {
        UIManager.printActionGrid(items, isResidential);
    }
}
